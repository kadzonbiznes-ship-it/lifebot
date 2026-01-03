/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.util.concurrent.ForkJoinPool$InvokeAnyRoot
 *  java.util.concurrent.ForkJoinPool$InvokeAnyTask
 */
package java.util.concurrent;

import java.lang.reflect.Field;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import jdk.internal.access.JavaUtilConcurrentFJPAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.misc.Unsafe;
import jdk.internal.vm.SharedThreadContainer;
import jdk.internal.vm.annotation.Contended;

public class ForkJoinPool
extends AbstractExecutorService {
    static final long DEFAULT_KEEPALIVE = 60000L;
    static final long TIMEOUT_SLOP = 20L;
    static final int DEFAULT_COMMON_MAX_SPARES = 256;
    static final int INITIAL_QUEUE_CAPACITY = 64;
    static final int SWIDTH = 16;
    static final int SMASK = 65535;
    static final int MAX_CAP = Short.MAX_VALUE;
    static final int STOP = Integer.MIN_VALUE;
    static final int SHUTDOWN = 1;
    static final int TERMINATED = 2;
    static final int PARKED = -1;
    static final int FIFO = 65536;
    static final int SRC = 131072;
    static final int CLEAR_TLS = 262144;
    static final int TRIMMED = 524288;
    static final int ISCOMMON = 0x100000;
    static final int PRESET_SIZE = 0x200000;
    static final int UNCOMPENSATE = 65536;
    static final long SP_MASK = 0xFFFFFFFFL;
    static final long UC_MASK = -4294967296L;
    static final int RC_SHIFT = 48;
    static final long RC_UNIT = 0x1000000000000L;
    static final long RC_MASK = -281474976710656L;
    static final int TC_SHIFT = 32;
    static final long TC_UNIT = 0x100000000L;
    static final long TC_MASK = 0xFFFF00000000L;
    static final int SS_SEQ = 65536;
    static final int INACTIVE = Integer.MIN_VALUE;
    public static final ForkJoinWorkerThreadFactory defaultForkJoinWorkerThreadFactory;
    static final ForkJoinPool common;
    private static volatile int poolIds;
    static volatile RuntimePermission modifyThreadPermission;
    volatile long stealCount;
    volatile long threadIds;
    final long keepAlive;
    final long bounds;
    final int config;
    volatile int runState;
    WorkQueue[] queues;
    final ReentrantLock registrationLock;
    Condition termination;
    final String workerNamePrefix;
    final ForkJoinWorkerThreadFactory factory;
    final Thread.UncaughtExceptionHandler ueh;
    final Predicate<? super ForkJoinPool> saturate;
    final SharedThreadContainer container;
    @Contended(value="fjpctl")
    volatile long ctl;
    @Contended(value="fjpctl")
    int parallelism;
    private static final Unsafe U;
    private static final long CTL;
    private static final long RUNSTATE;
    private static final long PARALLELISM;
    private static final long THREADIDS;
    private static final Object POOLIDS_BASE;
    private static final long POOLIDS;

    private static void checkPermission() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            RuntimePermission perm = modifyThreadPermission;
            if (perm == null) {
                modifyThreadPermission = perm = new RuntimePermission("modifyThread");
            }
            security.checkPermission(perm);
        }
    }

    private boolean compareAndSetCtl(long c, long v) {
        return U.compareAndSetLong(this, CTL, c, v);
    }

    private long compareAndExchangeCtl(long c, long v) {
        return U.compareAndExchangeLong(this, CTL, c, v);
    }

    private long getAndAddCtl(long v) {
        return U.getAndAddLong(this, CTL, v);
    }

    private int getAndBitwiseOrRunState(int v) {
        return U.getAndBitwiseOrInt(this, RUNSTATE, v);
    }

    private long incrementThreadIds() {
        return U.getAndAddLong(this, THREADIDS, 1L);
    }

    private static int getAndAddPoolIds(int x) {
        return U.getAndAddInt(POOLIDS_BASE, POOLIDS, x);
    }

    private int getAndSetParallelism(int v) {
        return U.getAndSetInt(this, PARALLELISM, v);
    }

    private int getParallelismOpaque() {
        return U.getIntOpaque(this, PARALLELISM);
    }

    private boolean createWorker() {
        ForkJoinWorkerThreadFactory fac = this.factory;
        Throwable ex = null;
        ForkJoinWorkerThread wt = null;
        try {
            if (this.runState >= 0 && fac != null && (wt = fac.newThread(this)) != null) {
                this.container.start(wt);
                return true;
            }
        }
        catch (Throwable rex) {
            ex = rex;
        }
        this.deregisterWorker(wt, ex);
        return false;
    }

    final String nextWorkerThreadName() {
        String prefix = this.workerNamePrefix;
        long tid = this.incrementThreadIds() + 1L;
        if (prefix == null) {
            prefix = "ForkJoinPool.commonPool-worker-";
        }
        return prefix.concat(Long.toString(tid));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void registerWorker(WorkQueue w) {
        ThreadLocalRandom.localInit();
        int seed = ThreadLocalRandom.getProbe();
        ReentrantLock lock = this.registrationLock;
        int cfg = this.config & 0x10000;
        if (w != null && lock != null) {
            w.array = new ForkJoinTask[64];
            cfg |= w.config | 0x20000;
            w.stackPred = seed;
            int id = seed << 1 | 1;
            lock.lock();
            try {
                int n;
                WorkQueue[] qs = this.queues;
                if (this.queues != null && (n = qs.length) > 0) {
                    int k;
                    int m = n - 1;
                    for (k = n; qs[id &= m] != null && k > 0; k -= 2) {
                        id -= 2;
                    }
                    if (k == 0) {
                        id = n | 1;
                    }
                    w.phase = w.config = id | cfg;
                    if (id < n) {
                        qs[id] = w;
                    } else {
                        int j;
                        int an = n << 1;
                        int am = an - 1;
                        WorkQueue[] as = new WorkQueue[an];
                        as[id & am] = w;
                        for (j = 1; j < n; j += 2) {
                            as[j] = qs[j];
                        }
                        for (j = 0; j < n; j += 2) {
                            WorkQueue q = qs[j];
                            if (q == null) continue;
                            as[q.config & am] = q;
                        }
                        U.storeFence();
                        this.queues = as;
                    }
                }
            }
            finally {
                lock.unlock();
            }
        }
    }

    final void deregisterWorker(ForkJoinWorkerThread wt, Throwable ex) {
        WorkQueue w = wt == null ? null : wt.workQueue;
        int cfg = w == null ? 0 : w.config;
        long c = this.ctl;
        if ((cfg & 0x80000) == 0) {
            while (c != (c = this.compareAndExchangeCtl(c, 0xFFFF000000000000L & c - 0x1000000000000L | 0xFFFF00000000L & c - 0x100000000L | 0xFFFFFFFFL & c))) {
            }
        } else if ((int)c == 0) {
            cfg &= 0xFFFDFFFF;
        }
        if (!this.tryTerminate(false, false) && w != null) {
            long ns = (long)w.nsteals & 0xFFFFFFFFL;
            ReentrantLock lock = this.registrationLock;
            if (lock != null) {
                int i;
                int n;
                lock.lock();
                WorkQueue[] qs = this.queues;
                if (this.queues != null && (n = qs.length) > 0 && qs[i = cfg & n - 1] == w) {
                    qs[i] = null;
                }
                this.stealCount += ns;
                lock.unlock();
            }
            if ((cfg & 0x20000) != 0) {
                this.signalWork();
            }
        }
        if (ex != null) {
            if (w != null) {
                ForkJoinTask<?> t;
                w.access = Integer.MIN_VALUE;
                while ((t = w.nextLocalTask(0)) != null) {
                    ForkJoinTask.cancelIgnoringExceptions(t);
                }
            }
            ForkJoinTask.rethrow(ex);
        }
    }

    final void signalWork() {
        block2: {
            WorkQueue v;
            int sp;
            block3: {
                boolean create;
                long ac;
                long nc;
                int n;
                int pc = this.parallelism;
                long c = this.ctl;
                WorkQueue[] qs = this.queues;
                if ((short)(c >>> 48) >= pc || qs == null || (n = qs.length) <= 0) break block2;
                do {
                    create = false;
                    sp = (int)c & Integer.MAX_VALUE;
                    v = qs[sp & n - 1];
                    int deficit = pc - (short)(c >>> 32);
                    ac = c + 0x1000000000000L & 0xFFFF000000000000L;
                    if (sp != 0 && v != null) {
                        nc = (long)v.stackPred & 0xFFFFFFFFL | c & 0xFFFF00000000L;
                        continue;
                    }
                    if (deficit <= 0) break block2;
                    create = true;
                    nc = c + 0x100000000L & 0xFFFF00000000L;
                } while (c != (c = this.compareAndExchangeCtl(c, nc | ac)));
                if (!create) break block3;
                this.createWorker();
                break block2;
            }
            ForkJoinWorkerThread owner = v.owner;
            v.phase = sp;
            if (v.access != -1) break block2;
            LockSupport.unpark(owner);
        }
    }

    private WorkQueue reactivate() {
        block3: {
            int n;
            long c = this.ctl;
            WorkQueue[] qs = this.queues;
            if (this.queues != null && (n = qs.length) > 0) {
                int sp;
                long ac;
                WorkQueue v;
                do {
                    sp = (int)c & Integer.MAX_VALUE;
                    v = qs[sp & n - 1];
                    ac = 0xFFFFFFFF00000000L & c + 0x1000000000000L;
                    if (sp == 0 || v == null) break block3;
                } while (c != (c = this.compareAndExchangeCtl(c, (long)v.stackPred & 0xFFFFFFFFL | ac)));
                ForkJoinWorkerThread owner = v.owner;
                v.phase = sp;
                if (v.access == -1) {
                    LockSupport.unpark(owner);
                }
                return v;
            }
        }
        return null;
    }

    private boolean tryTrim(WorkQueue w) {
        if (w != null) {
            int pred = w.stackPred;
            long c = this.ctl;
            int sp = (int)c & Integer.MAX_VALUE;
            int cfg = w.config | 0x80000;
            if ((sp & 0xFFFF) == (cfg & 0xFFFF) && this.compareAndSetCtl(c, (long)pred & 0xFFFFFFFFL | 0xFFFFFFFF00000000L & c - 0x100000000L)) {
                w.config = cfg;
                w.phase = sp;
                return true;
            }
        }
        return false;
    }

    private boolean hasTasks(boolean submissionsOnly) {
        int sum;
        int step = submissionsOnly ? 2 : 1;
        int checkSum = 0;
        do {
            U.loadFence();
            WorkQueue[] qs = this.queues;
            int n = qs == null ? 0 : qs.length;
            sum = 0;
            for (int i = 0; i < n; i += step) {
                int s;
                WorkQueue q = qs[i];
                if (q == null) continue;
                if (q.access > 0 || (s = q.top) != q.base) {
                    return true;
                }
                sum += (s << 16) + i + 1;
            }
        } while (checkSum != (checkSum = sum));
        return false;
    }

    final void runWorker(WorkQueue w) {
        if (w != null) {
            int r = w.stackPred;
            int src = 0;
            do {
                r ^= r << 13;
                r ^= r >>> 17;
            } while ((src = this.scan(w, src, r ^= r << 5)) >= 0 || (src = this.awaitWork(w)) == 0);
            w.access = Integer.MIN_VALUE;
        }
    }

    private int scan(WorkQueue w, int prevSrc, int r) {
        WorkQueue[] qs = this.queues;
        int n = w == null || qs == null ? 0 : qs.length;
        int step = r >>> 16 | 1;
        int i = n;
        while (i > 0) {
            int j = r & n - 1;
            WorkQueue q = qs[j];
            if (q != null) {
                int cap;
                ForkJoinTask<?>[] a = q.array;
                if (q.array != null && (cap = a.length) > 0) {
                    int src = j | 0x20000;
                    int b = q.base;
                    int k = cap - 1 & b;
                    int nb = b + 1;
                    int nk = cap - 1 & nb;
                    ForkJoinTask<?> t = a[k];
                    U.loadFence();
                    if (q.base != b) {
                        return prevSrc;
                    }
                    if (t != null && WorkQueue.casSlotToNull(a, k, t)) {
                        q.base = nb;
                        w.source = src;
                        if (src + (src << 16) != prevSrc && q.base == nb && a[nk] != null) {
                            this.signalWork();
                        }
                        w.topLevelExec(t, q);
                        return src + (prevSrc << 16);
                    }
                    if (q.array != a || a[k] != null || a[nk] != null) {
                        return prevSrc;
                    }
                }
            }
            --i;
            r += step;
        }
        return -1;
    }

    private int awaitWork(WorkQueue w) {
        WorkQueue[] qs;
        long qc;
        if (w == null) {
            return -1;
        }
        int p = w.phase + 65536 & Integer.MAX_VALUE;
        boolean idle = false;
        if (this.runState < 0) {
            return -1;
        }
        long sp = (long)p & 0xFFFFFFFFL;
        long pc = this.ctl;
        w.phase = p | Integer.MIN_VALUE;
        do {
            w.stackPred = (int)pc;
        } while (pc != (pc = this.compareAndExchangeCtl(pc, qc = pc - 0x1000000000000L & 0xFFFFFFFF00000000L | sp)));
        if ((qc & 0xFFFF000000000000L) <= 0L) {
            if (this.hasTasks(true) && (w.phase >= 0 || this.reactivate() == w)) {
                return 0;
            }
            if (this.runState != 0 && this.tryTerminate(false, false)) {
                return -1;
            }
            idle = true;
        }
        int spins = ((qs = this.queues) == null ? 0 : (qs.length & 0xFFFF) << 1) | 0xF;
        while ((p = w.phase) < 0 && --spins > 0) {
            Thread.onSpinWait();
        }
        if (p < 0) {
            long deadline = idle ? this.keepAlive + System.currentTimeMillis() : 0L;
            LockSupport.setCurrentBlocker(this);
            while (true) {
                if (this.runState < 0) {
                    return -1;
                }
                w.access = -1;
                if (w.phase < 0) {
                    if (idle) {
                        LockSupport.parkUntil(deadline);
                    } else {
                        LockSupport.park();
                    }
                }
                w.access = 0;
                if (w.phase >= 0) {
                    LockSupport.setCurrentBlocker(null);
                    break;
                }
                Thread.interrupted();
                if (!idle || deadline - System.currentTimeMillis() >= 20L) continue;
                if (this.tryTrim(w)) {
                    return -1;
                }
                deadline += this.keepAlive;
            }
        }
        return 0;
    }

    private boolean canStop() {
        long c = this.ctl;
        while (this.runState >= 0) {
            if ((c & 0xFFFF000000000000L) > 0L || this.hasTasks(false)) {
                return false;
            }
            if (c != (c = this.ctl)) continue;
        }
        return true;
    }

    private ForkJoinTask<?> pollScan(boolean submissionsOnly) {
        int step;
        int r = ThreadLocalRandom.nextSecondarySeed();
        if (submissionsOnly) {
            r &= 0xFFFFFFFE;
        }
        int n = step = submissionsOnly ? 2 : 1;
        if (this.runState >= 0) {
            int n2;
            WorkQueue[] qs = this.queues;
            if (this.queues != null && (n2 = qs.length) > 0) {
                int i = n2;
                while (i > 0) {
                    ForkJoinTask<?> t;
                    WorkQueue q = qs[r & n2 - 1];
                    if (q != null && (t = q.poll(this)) != null) {
                        return t;
                    }
                    i -= step;
                    r += step;
                }
            }
        }
        return null;
    }

    private int tryCompensate(long c, boolean canSaturate) {
        Predicate<? super ForkJoinPool> sat;
        long b = this.bounds;
        int pc = this.parallelism;
        short minActive = (short)(b & 0xFFFFL);
        int maxTotal = (short)(b >>> 16) + pc;
        short active = (short)(c >>> 48);
        short total = (short)(c >>> 32);
        int sp = (int)c & Integer.MAX_VALUE;
        if (sp != 0 && active <= pc) {
            if (this.ctl == c) {
                long nc;
                WorkQueue v;
                int i;
                WorkQueue[] qs = this.queues;
                if (this.queues != null && qs.length > (i = sp & 0xFFFF) && (v = qs[i]) != null && this.compareAndSetCtl(c, nc = (long)v.stackPred & 0xFFFFFFFFL | 0xFFFFFFFF00000000L & c)) {
                    v.phase = sp;
                    LockSupport.unpark(v.owner);
                    return 65536;
                }
            }
            return -1;
        }
        if (active > minActive && total >= pc) {
            long nc = 0xFFFF000000000000L & c - 0x1000000000000L | 0xFFFFFFFFFFFFL & c;
            return this.compareAndSetCtl(c, nc) ? 65536 : -1;
        }
        if (total < maxTotal && total < Short.MAX_VALUE) {
            long nc = c + 0x100000000L & 0xFFFF00000000L | c & 0xFFFF0000FFFFFFFFL;
            return !this.compareAndSetCtl(c, nc) ? -1 : (!this.createWorker() ? 0 : 65536);
        }
        if (!this.compareAndSetCtl(c, c)) {
            return -1;
        }
        if (canSaturate || (sat = this.saturate) != null && sat.test(this)) {
            return 0;
        }
        throw new RejectedExecutionException("Thread limit exceeded replacing blocked worker");
    }

    final void uncompensate() {
        this.getAndAddCtl(0x1000000000000L);
    }

    final int helpJoin(ForkJoinTask<?> task, WorkQueue w, boolean timed) {
        if (w == null || task == null) {
            return 0;
        }
        int wsrc = w.source;
        int wid = w.config & 0xFFFF | 0x20000;
        int r = wid + 2;
        long sctl = 0L;
        boolean rescan = true;
        int s;
        block0: while ((s = task.status) >= 0) {
            if (!rescan && sctl == (sctl = this.ctl)) {
                if (this.runState < 0) {
                    return 0;
                }
                s = this.tryCompensate(sctl, timed);
                if (s >= 0) {
                    return s;
                }
            }
            rescan = false;
            WorkQueue[] qs = this.queues;
            int n = this.queues == null ? 0 : qs.length;
            int m = n - 1;
            int i = n >>> 1;
            while (true) {
                block15: {
                    if (i <= 0) continue block0;
                    int j = r & m;
                    WorkQueue q = qs[j];
                    if (q != null) {
                        int cap;
                        ForkJoinTask<?>[] a = q.array;
                        if (q.array != null && (cap = a.length) > 0) {
                            ForkJoinTask<?> t;
                            int nb;
                            int src = j | 0x20000;
                            while (true) {
                                int sq = q.source;
                                int b = q.base;
                                int k = cap - 1 & b;
                                nb = b + 1;
                                t = a[k];
                                U.loadFence();
                                boolean eligible = true;
                                int d = n;
                                int v = sq;
                                while (v != wid) {
                                    WorkQueue p;
                                    if (v == 0 || --d == 0 || (p = qs[v & m]) == null) {
                                        eligible = false;
                                        break;
                                    }
                                    v = p.source;
                                }
                                if (q.source != sq || q.base != b) continue;
                                s = task.status;
                                if (s < 0) {
                                    return s;
                                }
                                if (t == null) {
                                    if (a[k] != null) continue;
                                    if (!rescan && eligible && (q.array != a || q.top != b)) {
                                        rescan = true;
                                    }
                                    break block15;
                                }
                                if (t != task && !eligible) break block15;
                                if (WorkQueue.casSlotToNull(a, k, t)) break;
                            }
                            q.base = nb;
                            w.source = src;
                            t.doExec();
                            w.source = wsrc;
                            rescan = true;
                            continue block0;
                        }
                    }
                }
                --i;
                r += 2;
            }
            break;
        }
        return s;
    }

    final int helpComplete(ForkJoinTask<?> task, WorkQueue w, boolean owned, boolean timed) {
        if (w == null || task == null) {
            return 0;
        }
        int wsrc = w.source;
        int r = w.config;
        long sctl = 0L;
        boolean rescan = true;
        int s;
        block0: while ((s = w.helpComplete(task, owned, 0)) >= 0) {
            if (!rescan && sctl == (sctl = this.ctl)) {
                if (!owned || this.runState < 0) {
                    return 0;
                }
                s = this.tryCompensate(sctl, timed);
                if (s >= 0) {
                    return s;
                }
            }
            rescan = false;
            WorkQueue[] qs = this.queues;
            int n = this.queues == null ? 0 : qs.length;
            int m = n - 1;
            int i = n;
            while (true) {
                block14: {
                    if (i <= 0) continue block0;
                    int j = r & m;
                    WorkQueue q = qs[j];
                    if (q != null) {
                        int cap;
                        ForkJoinTask<?>[] a = q.array;
                        if (q.array != null && (cap = a.length) > 0) {
                            ForkJoinTask<?> t;
                            int nb;
                            int src = j | 0x20000;
                            int b = q.base;
                            while (true) {
                                int k = cap - 1 & b;
                                nb = b + 1;
                                t = a[k];
                                U.loadFence();
                                if (b != (b = q.base)) continue;
                                s = task.status;
                                if (s < 0) {
                                    return s;
                                }
                                if (t == null) {
                                    if (a[k] != null) continue;
                                    if (!(rescan || q.array == a && q.top == b)) {
                                        rescan = true;
                                    }
                                    break block14;
                                }
                                if (!(t instanceof CountedCompleter)) break block14;
                                CountedCompleter<?> f = (CountedCompleter<?>)t;
                                while (f != task) {
                                    f = f.completer;
                                    if (f != null) continue;
                                    break block14;
                                }
                                if (WorkQueue.casSlotToNull(a, k, t)) break;
                            }
                            q.base = nb;
                            w.source = src;
                            t.doExec();
                            w.source = wsrc;
                            rescan = true;
                            continue block0;
                        }
                    }
                }
                --i;
                ++r;
            }
            break;
        }
        return s;
    }

    private int helpQuiesce(WorkQueue w, long nanos, boolean interruptible) {
        int phase;
        long startTime = System.nanoTime();
        long parkTime = 0L;
        if (w == null || (phase = w.phase) < 0) {
            return 0;
        }
        int activePhase = phase;
        int inactivePhase = phase | Integer.MIN_VALUE;
        int wsrc = w.source;
        int r = 0;
        boolean locals = true;
        while (true) {
            if (this.runState < 0) {
                w.phase = activePhase;
                return 1;
            }
            if (locals) {
                ForkJoinTask<?> u;
                while ((u = w.nextLocalTask()) != null) {
                    u.doExec();
                }
            }
            boolean rescan = false;
            locals = false;
            boolean busy = false;
            WorkQueue[] qs = this.queues;
            int n = this.queues == null ? 0 : qs.length;
            int m = n - 1;
            int i = n;
            while (i > 0) {
                block15: {
                    int j = m & r;
                    WorkQueue q = qs[j];
                    if (q != null && q != w) {
                        ForkJoinTask<?> t;
                        int nb;
                        int src = j | 0x20000;
                        while (true) {
                            int cap;
                            ForkJoinTask<?>[] a = q.array;
                            int b = q.base;
                            if (a == null || (cap = a.length) <= 0) break block15;
                            int k = cap - 1 & b;
                            nb = b + 1;
                            int nk = cap - 1 & nb;
                            t = a[k];
                            U.loadFence();
                            if (q.base != b || q.array != a || a[k] != t) continue;
                            if (t == null) {
                                if (rescan) break block15;
                                if (a[nk] != null || q.top - b > 0) {
                                    rescan = true;
                                    break block15;
                                }
                                if (busy || q.owner == null || q.phase < 0) break block15;
                                busy = true;
                                break block15;
                            }
                            if (phase < 0) {
                                w.phase = phase = activePhase;
                                continue;
                            }
                            if (WorkQueue.casSlotToNull(a, k, t)) break;
                        }
                        q.base = nb;
                        w.source = src;
                        t.doExec();
                        w.source = wsrc;
                        locals = true;
                        rescan = true;
                        break;
                    }
                }
                --i;
                ++r;
            }
            if (rescan) continue;
            if (phase >= 0) {
                parkTime = 0L;
                w.phase = phase = inactivePhase;
                continue;
            }
            if (!busy) {
                w.phase = activePhase;
                return 1;
            }
            if (parkTime == 0L) {
                parkTime = 1024L;
                Thread.yield();
                continue;
            }
            boolean interrupted = interruptible && Thread.interrupted();
            if (interrupted || System.nanoTime() - startTime > nanos) {
                w.phase = activePhase;
                return interrupted ? -1 : 0;
            }
            LockSupport.parkNanos(this, parkTime);
            if (parkTime >= nanos >>> 8 || parkTime >= 0x100000L) continue;
            parkTime <<= 1;
        }
    }

    private int externalHelpQuiesce(long nanos, boolean interruptible) {
        long startTime = System.nanoTime();
        long parkTime = 0L;
        while (true) {
            ForkJoinTask<?> t;
            if ((t = this.pollScan(false)) != null) {
                t.doExec();
                parkTime = 0L;
                continue;
            }
            if (this.canStop()) {
                return 1;
            }
            if (parkTime == 0L) {
                parkTime = 1024L;
                Thread.yield();
                continue;
            }
            if (System.nanoTime() - startTime > nanos) {
                return 0;
            }
            if (interruptible && Thread.interrupted()) {
                return -1;
            }
            LockSupport.parkNanos(this, parkTime);
            if (parkTime >= nanos >>> 8 || parkTime >= 0x100000L) continue;
            parkTime <<= 1;
        }
    }

    static final int helpQuiescePool(ForkJoinPool pool, long nanos, boolean interruptible) {
        ForkJoinPool p;
        Thread t = Thread.currentThread();
        if (t instanceof ForkJoinWorkerThread) {
            ForkJoinWorkerThread wt = (ForkJoinWorkerThread)t;
            p = wt.pool;
            if (p != null && (p == pool || pool == null)) {
                return p.helpQuiesce(wt.workQueue, nanos, interruptible);
            }
        }
        if ((p = pool) != null || (p = common) != null) {
            return p.externalHelpQuiesce(nanos, interruptible);
        }
        return 0;
    }

    final ForkJoinTask<?> nextTaskFor(WorkQueue w) {
        ForkJoinTask<?> t;
        if (w == null || (t = w.nextLocalTask()) == null) {
            t = this.pollScan(false);
        }
        return t;
    }

    final WorkQueue submissionQueue(boolean isSubmit) {
        block7: {
            ReentrantLock lock = this.registrationLock;
            int r = ThreadLocalRandom.getProbe();
            if (r == 0) {
                ThreadLocalRandom.localInit();
                r = ThreadLocalRandom.getProbe();
            }
            if (lock != null) {
                WorkQueue q;
                int id = r << 1;
                while (true) {
                    int n;
                    WorkQueue[] qs = this.queues;
                    if (this.queues == null || (n = qs.length) <= 0) break block7;
                    int i = n - 1 & id;
                    q = qs[i];
                    if (q == null) {
                        WorkQueue w = new WorkQueue(null, id | 0x20000);
                        w.array = new ForkJoinTask[64];
                        lock.lock();
                        if (this.queues == qs && qs[i] == null) {
                            qs[i] = w;
                        }
                        lock.unlock();
                        continue;
                    }
                    if (q.getAndSetAccess(1) == 0) break;
                    r = ThreadLocalRandom.advanceProbe(r);
                    id = r << 1;
                }
                if (isSubmit && this.runState != 0) {
                    q.access = 0;
                } else {
                    return q;
                }
            }
        }
        throw new RejectedExecutionException();
    }

    /*
     * Unable to fully structure code
     */
    private <T> ForkJoinTask<T> poolSubmit(boolean signalIfEmpty, ForkJoinTask<T> task) {
        ForkJoinPool.U.storeStoreFence();
        if (task == null) {
            throw new NullPointerException();
        }
        t = Thread.currentThread();
        if (!(t instanceof ForkJoinWorkerThread)) ** GOTO lbl-1000
        wt = (ForkJoinWorkerThread)t;
        if (wt.pool == this) {
            q = wt.workQueue;
        } else lbl-1000:
        // 2 sources

        {
            task.markPoolSubmission();
            q = this.submissionQueue(true);
        }
        q.push(task, this, signalIfEmpty);
        return task;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static WorkQueue externalQueue(ForkJoinPool p) {
        int r = ThreadLocalRandom.getProbe();
        if (p == null) return null;
        WorkQueue[] qs = p.queues;
        if (p.queues == null) return null;
        int n = qs.length;
        if (n <= 0) return null;
        if (r == 0) return null;
        WorkQueue workQueue = qs[n - 1 & r << 1];
        return workQueue;
    }

    static WorkQueue commonQueue() {
        return ForkJoinPool.externalQueue(common);
    }

    final WorkQueue externalQueue() {
        return ForkJoinPool.externalQueue(this);
    }

    static void helpAsyncBlocker(Executor e, ManagedBlocker blocker) {
        WorkQueue w = null;
        Thread t = Thread.currentThread();
        if (t instanceof ForkJoinWorkerThread) {
            ForkJoinWorkerThread wt = (ForkJoinWorkerThread)t;
            if (wt.pool == e) {
                w = wt.workQueue;
            }
        } else if (e instanceof ForkJoinPool) {
            w = ((ForkJoinPool)e).externalQueue();
        }
        if (w != null) {
            w.helpAsyncBlocker(blocker);
        }
    }

    static int getSurplusQueuedTaskCount() {
        Thread t = Thread.currentThread();
        if (t instanceof ForkJoinWorkerThread) {
            WorkQueue q;
            ForkJoinWorkerThread wt = (ForkJoinWorkerThread)t;
            ForkJoinPool pool = wt.pool;
            if (pool != null && (q = wt.workQueue) != null) {
                int n = q.top - q.base;
                int p = pool.parallelism;
                short a = (short)(pool.ctl >>> 48);
                return n - (a > (p >>>= 1) ? 0 : (a > (p >>>= 1) ? 1 : (a > (p >>>= 1) ? 2 : (a > (p >>>= 1) ? 4 : 8))));
            }
        }
        return 0;
    }

    private boolean tryTerminate(boolean now, boolean enable) {
        ReentrantLock lock;
        int rs = this.runState;
        if (rs >= 0) {
            if ((this.config & 0x100000) != 0) {
                return false;
            }
            if (!now) {
                if ((rs & 1) == 0) {
                    if (!enable) {
                        return false;
                    }
                    this.getAndBitwiseOrRunState(1);
                }
                if (!this.canStop()) {
                    return false;
                }
            }
            this.getAndBitwiseOrRunState(-2147483647);
        }
        WorkQueue released = this.reactivate();
        short tc = (short)(this.ctl >>> 32);
        if (released == null && tc > 0) {
            Thread current = Thread.currentThread();
            WorkQueue w = current instanceof ForkJoinWorkerThread ? ((ForkJoinWorkerThread)current).workQueue : null;
            int r = w == null ? 0 : w.config + 1;
            WorkQueue[] qs = this.queues;
            int n = qs == null ? 0 : qs.length;
            for (int i = 0; i < n; ++i) {
                ForkJoinTask<?> t;
                ForkJoinWorkerThread thread;
                WorkQueue q = qs[r + i & n - 1];
                if (q == null || (thread = q.owner) == current || q.access == Integer.MIN_VALUE) continue;
                while ((t = q.poll(null)) != null) {
                    ForkJoinTask.cancelIgnoringExceptions(t);
                }
                if (thread == null || thread.isInterrupted()) continue;
                q.forcePhaseActive();
                try {
                    thread.interrupt();
                    continue;
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
        if ((tc <= 0 || (short)(this.ctl >>> 32) <= 0) && (this.getAndBitwiseOrRunState(2) & 2) == 0 && (lock = this.registrationLock) != null) {
            lock.lock();
            Condition cond = this.termination;
            if (cond != null) {
                cond.signalAll();
            }
            lock.unlock();
            this.container.close();
        }
        return true;
    }

    public ForkJoinPool() {
        this(Math.min(Short.MAX_VALUE, Runtime.getRuntime().availableProcessors()), defaultForkJoinWorkerThreadFactory, null, false, 0, Short.MAX_VALUE, 1, null, 60000L, TimeUnit.MILLISECONDS);
    }

    public ForkJoinPool(int parallelism) {
        this(parallelism, defaultForkJoinWorkerThreadFactory, null, false, 0, Short.MAX_VALUE, 1, null, 60000L, TimeUnit.MILLISECONDS);
    }

    public ForkJoinPool(int parallelism, ForkJoinWorkerThreadFactory factory, Thread.UncaughtExceptionHandler handler, boolean asyncMode) {
        this(parallelism, factory, handler, asyncMode, 0, Short.MAX_VALUE, 1, null, 60000L, TimeUnit.MILLISECONDS);
    }

    public ForkJoinPool(int parallelism, ForkJoinWorkerThreadFactory factory, Thread.UncaughtExceptionHandler handler, boolean asyncMode, int corePoolSize, int maximumPoolSize, int minimumRunnable, Predicate<? super ForkJoinPool> saturate, long keepAliveTime, TimeUnit unit) {
        ForkJoinPool.checkPermission();
        int p = parallelism;
        if (p <= 0 || p > Short.MAX_VALUE || p > maximumPoolSize || keepAliveTime <= 0L) {
            throw new IllegalArgumentException();
        }
        if (factory == null || unit == null) {
            throw new NullPointerException();
        }
        this.parallelism = p;
        this.factory = factory;
        this.ueh = handler;
        this.saturate = saturate;
        this.config = asyncMode ? 65536 : 0;
        this.keepAlive = Math.max(unit.toMillis(keepAliveTime), 20L);
        int corep = Math.clamp((long)corePoolSize, p, Short.MAX_VALUE);
        int maxSpares = Math.clamp((long)(maximumPoolSize - p), 0, Short.MAX_VALUE);
        int minAvail = Math.clamp((long)minimumRunnable, 0, Short.MAX_VALUE);
        this.bounds = (long)(minAvail & 0xFFFF) | (long)(maxSpares << 16) | (long)corep << 32;
        int size = 1 << 33 - Integer.numberOfLeadingZeros(p - 1);
        this.registrationLock = new ReentrantLock();
        this.queues = new WorkQueue[size];
        String pid = Integer.toString(ForkJoinPool.getAndAddPoolIds(1) + 1);
        String name = "ForkJoinPool-" + pid;
        this.workerNamePrefix = name + "-worker-";
        this.container = SharedThreadContainer.create(name);
    }

    private ForkJoinPool(byte forCommonPoolOnly) {
        int p;
        ForkJoinWorkerThreadFactory fac = defaultForkJoinWorkerThreadFactory;
        Thread.UncaughtExceptionHandler handler = null;
        int maxSpares = 256;
        int pc = 0;
        int preset = 0;
        try {
            String ms;
            String pp = System.getProperty("java.util.concurrent.ForkJoinPool.common.parallelism");
            if (pp != null) {
                pc = Math.max(0, Integer.parseInt(pp));
                preset = 0x200000;
            }
            if ((ms = System.getProperty("java.util.concurrent.ForkJoinPool.common.maximumSpares")) != null) {
                maxSpares = Math.clamp((long)Integer.parseInt(ms), 0, Short.MAX_VALUE);
            }
            String sf = System.getProperty("java.util.concurrent.ForkJoinPool.common.threadFactory");
            String sh = System.getProperty("java.util.concurrent.ForkJoinPool.common.exceptionHandler");
            if (sf != null || sh != null) {
                ClassLoader ldr = ClassLoader.getSystemClassLoader();
                if (sf != null) {
                    fac = (ForkJoinWorkerThreadFactory)ldr.loadClass(sf).getConstructor(new Class[0]).newInstance(new Object[0]);
                }
                if (sh != null) {
                    handler = (Thread.UncaughtExceptionHandler)ldr.loadClass(sh).getConstructor(new Class[0]).newInstance(new Object[0]);
                }
            }
        }
        catch (Exception pp) {
            // empty catch block
        }
        if (preset == 0) {
            pc = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        }
        int size = (p = Math.min(pc, Short.MAX_VALUE)) == 0 ? 1 : 1 << 33 - Integer.numberOfLeadingZeros(p - 1);
        this.parallelism = p;
        this.config = 0x100000 | preset;
        this.bounds = 1 | maxSpares << 16;
        this.factory = fac;
        this.ueh = handler;
        this.keepAlive = 60000L;
        this.saturate = null;
        this.workerNamePrefix = null;
        this.registrationLock = new ReentrantLock();
        this.queues = new WorkQueue[size];
        this.container = SharedThreadContainer.create("ForkJoinPool.commonPool");
    }

    public static ForkJoinPool commonPool() {
        return common;
    }

    public <T> T invoke(ForkJoinTask<T> task) {
        this.poolSubmit(true, task);
        return task.join();
    }

    public void execute(ForkJoinTask<?> task) {
        this.poolSubmit(true, task);
    }

    @Override
    public void execute(Runnable task) {
        this.poolSubmit(true, task instanceof ForkJoinTask ? (ForkJoinTask)((Object)task) : new ForkJoinTask.RunnableExecuteAction(task));
    }

    public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
        return this.poolSubmit(true, task);
    }

    public <T> ForkJoinTask<T> submit(Callable<T> task) {
        return this.poolSubmit(true, new ForkJoinTask.AdaptedCallable<T>(task));
    }

    public <T> ForkJoinTask<T> submit(Runnable task, T result) {
        return this.poolSubmit(true, new ForkJoinTask.AdaptedRunnable<T>(task, result));
    }

    public ForkJoinTask<?> submit(Runnable task) {
        return this.poolSubmit(true, task instanceof ForkJoinTask ? (ForkJoinTask)((Object)task) : new ForkJoinTask.AdaptedRunnableAction(task));
    }

    public <T> ForkJoinTask<T> externalSubmit(ForkJoinTask<T> task) {
        U.storeStoreFence();
        task.markPoolSubmission();
        WorkQueue q = this.submissionQueue(true);
        q.push(task, this, true);
        return task;
    }

    public <T> ForkJoinTask<T> lazySubmit(ForkJoinTask<T> task) {
        return this.poolSubmit(false, task);
    }

    public int setParallelism(int size) {
        if (size < 1 || size > Short.MAX_VALUE) {
            throw new IllegalArgumentException();
        }
        if ((this.config & 0x200000) != 0) {
            throw new UnsupportedOperationException("Cannot override System property");
        }
        ForkJoinPool.checkPermission();
        return this.getAndSetParallelism(size);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
        ArrayList<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
        try {
            for (Callable<T> t : tasks) {
                ForkJoinTask.AdaptedInterruptibleCallable<T> adaptedInterruptibleCallable = new ForkJoinTask.AdaptedInterruptibleCallable<T>(t);
                futures.add(adaptedInterruptibleCallable);
                this.poolSubmit(true, adaptedInterruptibleCallable);
            }
            for (int i = futures.size() - 1; i >= 0; --i) {
                ((ForkJoinTask)futures.get(i)).quietlyJoin();
            }
            return futures;
        }
        catch (Throwable t) {
            for (Future future : futures) {
                ForkJoinTask.cancelIgnoringExceptions(future);
            }
            throw t;
        }
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        ArrayList<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
        try {
            for (Callable<T> t : tasks) {
                ForkJoinTask.AdaptedInterruptibleCallable<T> adaptedInterruptibleCallable = new ForkJoinTask.AdaptedInterruptibleCallable<T>(t);
                futures.add(adaptedInterruptibleCallable);
                this.poolSubmit(true, adaptedInterruptibleCallable);
            }
            long startTime = System.nanoTime();
            long l = nanos;
            boolean timedOut = l < 0L;
            for (int i = futures.size() - 1; i >= 0; --i) {
                ForkJoinTask f = (ForkJoinTask)futures.get(i);
                if (f.isDone()) continue;
                if (!timedOut) {
                    boolean bl = timedOut = !f.quietlyJoin(l, TimeUnit.NANOSECONDS);
                }
                if (timedOut) {
                    ForkJoinTask.cancelIgnoringExceptions(f);
                    continue;
                }
                l = nanos - (System.nanoTime() - startTime);
            }
            return futures;
        }
        catch (Throwable t) {
            for (Future future : futures) {
                ForkJoinTask.cancelIgnoringExceptions(future);
            }
            throw t;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        int n = tasks.size();
        if (n <= 0) {
            throw new IllegalArgumentException();
        }
        InvokeAnyRoot root = new InvokeAnyRoot(n, this);
        ArrayList<InvokeAnyTask> fs = new ArrayList<InvokeAnyTask>(n);
        try {
            for (Callable<T> c : tasks) {
                if (c == null) {
                    throw new NullPointerException();
                }
                InvokeAnyTask f = new InvokeAnyTask(root, c);
                fs.add(f);
                this.poolSubmit(true, (ForkJoinTask<T>)f);
                if (!root.isDone()) continue;
                break;
            }
            Object object = root.get();
            return (T)object;
        }
        finally {
            for (InvokeAnyTask f : fs) {
                ForkJoinTask.cancelIgnoringExceptions(f);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long nanos = unit.toNanos(timeout);
        int n = tasks.size();
        if (n <= 0) {
            throw new IllegalArgumentException();
        }
        InvokeAnyRoot root = new InvokeAnyRoot(n, this);
        ArrayList<InvokeAnyTask> fs = new ArrayList<InvokeAnyTask>(n);
        try {
            for (Callable<T> c : tasks) {
                if (c == null) {
                    throw new NullPointerException();
                }
                InvokeAnyTask f = new InvokeAnyTask(root, c);
                fs.add(f);
                this.poolSubmit(true, (ForkJoinTask<T>)f);
                if (!root.isDone()) continue;
                break;
            }
            Object object = root.get(nanos, TimeUnit.NANOSECONDS);
            return (T)object;
        }
        finally {
            for (InvokeAnyTask f : fs) {
                ForkJoinTask.cancelIgnoringExceptions(f);
            }
        }
    }

    public ForkJoinWorkerThreadFactory getFactory() {
        return this.factory;
    }

    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return this.ueh;
    }

    public int getParallelism() {
        return Math.max(this.getParallelismOpaque(), 1);
    }

    public static int getCommonPoolParallelism() {
        return common.getParallelism();
    }

    public int getPoolSize() {
        return (short)(this.ctl >>> 32);
    }

    public boolean getAsyncMode() {
        return (this.config & 0x10000) != 0;
    }

    public int getRunningThreadCount() {
        int rc = 0;
        if ((this.runState & 2) == 0) {
            WorkQueue[] qs = this.queues;
            if (this.queues != null) {
                for (int i = 1; i < qs.length; i += 2) {
                    WorkQueue q = qs[i];
                    if (q == null || !q.isApparentlyUnblocked()) continue;
                    ++rc;
                }
            }
        }
        return rc;
    }

    public int getActiveThreadCount() {
        return Math.max((short)(this.ctl >>> 48), 0);
    }

    public boolean isQuiescent() {
        return this.canStop();
    }

    public long getStealCount() {
        long count = this.stealCount;
        WorkQueue[] qs = this.queues;
        if (this.queues != null) {
            for (int i = 1; i < qs.length; i += 2) {
                WorkQueue q = qs[i];
                if (q == null) continue;
                count += (long)q.nsteals & 0xFFFFFFFFL;
            }
        }
        return count;
    }

    public long getQueuedTaskCount() {
        int count = 0;
        if ((this.runState & 2) == 0) {
            WorkQueue[] qs = this.queues;
            if (this.queues != null) {
                for (int i = 1; i < qs.length; i += 2) {
                    WorkQueue q = qs[i];
                    if (q == null) continue;
                    count += q.queueSize();
                }
            }
        }
        return count;
    }

    public int getQueuedSubmissionCount() {
        int count = 0;
        if ((this.runState & 2) == 0) {
            WorkQueue[] qs = this.queues;
            if (this.queues != null) {
                for (int i = 0; i < qs.length; i += 2) {
                    WorkQueue q = qs[i];
                    if (q == null) continue;
                    count += q.queueSize();
                }
            }
        }
        return count;
    }

    public boolean hasQueuedSubmissions() {
        return this.hasTasks(true);
    }

    protected ForkJoinTask<?> pollSubmission() {
        return this.pollScan(true);
    }

    protected int drainTasksTo(Collection<? super ForkJoinTask<?>> c) {
        ForkJoinTask<?> t;
        int count = 0;
        while ((t = this.pollScan(false)) != null) {
            c.add(t);
            ++count;
        }
        return count;
    }

    public String toString() {
        int rs;
        long st = this.stealCount;
        long qt = 0L;
        long ss = 0L;
        int rc = 0;
        WorkQueue[] qs = this.queues;
        if (this.queues != null) {
            for (int i = 0; i < qs.length; ++i) {
                WorkQueue q = qs[i];
                if (q == null) continue;
                int size = q.queueSize();
                if ((i & 1) == 0) {
                    ss += (long)size;
                    continue;
                }
                qt += (long)size;
                st += (long)q.nsteals & 0xFFFFFFFFL;
                if (!q.isApparentlyUnblocked()) continue;
                ++rc;
            }
        }
        int pc = this.parallelism;
        long c = this.ctl;
        short tc = (short)(c >>> 32);
        short ac = (short)(c >>> 48);
        if (ac < 0) {
            ac = 0;
        }
        String level = ((rs = this.runState) & 2) != 0 ? "Terminated" : ((rs & Integer.MIN_VALUE) != 0 ? "Terminating" : ((rs & 1) != 0 ? "Shutting down" : "Running"));
        return super.toString() + "[" + level + ", parallelism = " + pc + ", size = " + tc + ", active = " + ac + ", running = " + rc + ", steals = " + st + ", tasks = " + qt + ", submissions = " + ss + "]";
    }

    @Override
    public void shutdown() {
        ForkJoinPool.checkPermission();
        this.tryTerminate(false, true);
    }

    @Override
    public List<Runnable> shutdownNow() {
        ForkJoinPool.checkPermission();
        this.tryTerminate(true, true);
        return Collections.emptyList();
    }

    @Override
    public boolean isTerminated() {
        return (this.runState & 2) != 0;
    }

    public boolean isTerminating() {
        return (this.runState & 0x80000002) == Integer.MIN_VALUE;
    }

    @Override
    public boolean isShutdown() {
        return this.runState != 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        boolean terminated;
        long nanos = unit.toNanos(timeout);
        if ((this.config & 0x100000) != 0) {
            if (ForkJoinPool.helpQuiescePool(this, nanos, true) < 0) {
                throw new InterruptedException();
            }
            terminated = false;
        } else {
            terminated = (this.runState & 2) != 0;
            if (!terminated) {
                this.tryTerminate(false, false);
                ReentrantLock lock = this.registrationLock;
                if (lock != null && !(terminated = (this.runState & 2) != 0)) {
                    lock.lock();
                    try {
                        Condition cond = this.termination;
                        if (cond == null) {
                            this.termination = cond = lock.newCondition();
                        }
                        while (!(terminated = (this.runState & 2) != 0) && nanos > 0L) {
                            nanos = cond.awaitNanos(nanos);
                        }
                    }
                    finally {
                        lock.unlock();
                    }
                }
            }
        }
        return terminated;
    }

    public boolean awaitQuiescence(long timeout, TimeUnit unit) {
        return ForkJoinPool.helpQuiescePool(this, unit.toNanos(timeout), false) > 0;
    }

    @Override
    public void close() {
        boolean terminated;
        if ((this.config & 0x100000) == 0 && !(terminated = this.tryTerminate(false, false))) {
            this.shutdown();
            boolean interrupted = false;
            while (!terminated) {
                try {
                    terminated = this.awaitTermination(1L, TimeUnit.DAYS);
                }
                catch (InterruptedException e) {
                    if (interrupted) continue;
                    this.shutdownNow();
                    interrupted = true;
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void managedBlock(ManagedBlocker blocker) throws InterruptedException {
        ForkJoinPool p;
        Thread t = Thread.currentThread();
        if (t instanceof ForkJoinWorkerThread && (p = ((ForkJoinWorkerThread)t).pool) != null) {
            p.compensatedBlock(blocker);
        } else {
            ForkJoinPool.unmanagedBlock(blocker);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void compensatedBlock(ManagedBlocker blocker) throws InterruptedException {
        if (blocker == null) {
            throw new NullPointerException();
        }
        while (true) {
            boolean done;
            long c = this.ctl;
            if (blocker.isReleasable()) return;
            int comp = this.tryCompensate(c, false);
            if (comp < 0) continue;
            long post = comp == 0 ? 0L : 0x1000000000000L;
            try {
                done = blocker.block();
            }
            finally {
                this.getAndAddCtl(post);
            }
            if (done) return;
        }
    }

    private long beginCompensatedBlock() {
        int comp;
        while ((comp = this.tryCompensate(this.ctl, false)) < 0) {
            Thread.onSpinWait();
        }
        return comp == 0 ? 0L : 0x1000000000000L;
    }

    void endCompensatedBlock(long post) {
        if (post > 0L) {
            this.getAndAddCtl(post);
        }
    }

    private static void unmanagedBlock(ManagedBlocker blocker) throws InterruptedException {
        if (blocker == null) {
            throw new NullPointerException();
        }
        while (!blocker.isReleasable() && !blocker.block()) {
        }
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new ForkJoinTask.AdaptedRunnable<T>(runnable, value);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new ForkJoinTask.AdaptedCallable<T>(callable);
    }

    static {
        U = Unsafe.getUnsafe();
        Class<ForkJoinPool> klass = ForkJoinPool.class;
        try {
            Field poolIdsField = klass.getDeclaredField("poolIds");
            POOLIDS_BASE = U.staticFieldBase(poolIdsField);
            POOLIDS = U.staticFieldOffset(poolIdsField);
        }
        catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
        CTL = U.objectFieldOffset(klass, "ctl");
        RUNSTATE = U.objectFieldOffset(klass, "runState");
        PARALLELISM = U.objectFieldOffset(klass, "parallelism");
        THREADIDS = U.objectFieldOffset(klass, "threadIds");
        defaultForkJoinWorkerThreadFactory = new DefaultForkJoinWorkerThreadFactory();
        common = System.getSecurityManager() == null ? new ForkJoinPool(0) : AccessController.doPrivileged(new PrivilegedAction<ForkJoinPool>(){

            @Override
            public ForkJoinPool run() {
                return new ForkJoinPool(0);
            }
        });
        ForkJoinPool p = common;
        SharedSecrets.setJavaUtilConcurrentFJPAccess(new JavaUtilConcurrentFJPAccess(){

            @Override
            public long beginCompensatedBlock(ForkJoinPool pool) {
                return pool.beginCompensatedBlock();
            }

            @Override
            public void endCompensatedBlock(ForkJoinPool pool, long post) {
                pool.endCompensatedBlock(post);
            }
        });
        Class<LockSupport> clazz = LockSupport.class;
    }

    public static interface ForkJoinWorkerThreadFactory {
        public ForkJoinWorkerThread newThread(ForkJoinPool var1);
    }

    static final class WorkQueue {
        int stackPred;
        int config;
        int base;
        ForkJoinTask<?>[] array;
        final ForkJoinWorkerThread owner;
        @Contended(value="w")
        int top;
        @Contended(value="w")
        volatile int access;
        @Contended(value="w")
        volatile int phase;
        @Contended(value="w")
        volatile int source;
        @Contended(value="w")
        int nsteals;
        private static final Unsafe U = Unsafe.getUnsafe();
        private static final long ACCESS;
        private static final long PHASE;
        private static final long ABASE;
        private static final int ASHIFT;

        static ForkJoinTask<?> getAndClearSlot(ForkJoinTask<?>[] a, int i) {
            return (ForkJoinTask)U.getAndSetReference(a, ((long)i << ASHIFT) + ABASE, null);
        }

        static boolean casSlotToNull(ForkJoinTask<?>[] a, int i, ForkJoinTask<?> c) {
            return U.compareAndSetReference(a, ((long)i << ASHIFT) + ABASE, c, null);
        }

        final void forcePhaseActive() {
            U.getAndBitwiseAndInt(this, PHASE, Integer.MAX_VALUE);
        }

        final int getAndSetAccess(int v) {
            return U.getAndSetInt(this, ACCESS, v);
        }

        final void releaseAccess() {
            U.putIntRelease(this, ACCESS, 0);
        }

        WorkQueue(ForkJoinWorkerThread owner, int config) {
            this.owner = owner;
            this.config = config;
            this.top = 1;
            this.base = 1;
        }

        final int getPoolIndex() {
            return (this.config & 0xFFFF) >>> 1;
        }

        final int queueSize() {
            int unused = this.access;
            return Math.max(this.top - this.base, 0);
        }

        final void push(ForkJoinTask<?> task, ForkJoinPool pool, boolean signalIfEmpty) {
            int cap;
            boolean resize = false;
            int s = this.top++;
            int b = this.base;
            ForkJoinTask<?>[] a = this.array;
            if (this.array != null && (cap = a.length) > 0) {
                int m = cap - 1;
                if (m == s - b) {
                    ForkJoinTask[] newArray;
                    resize = true;
                    int newCap = cap < 0x1000000 ? cap << 2 : cap << 1;
                    try {
                        newArray = new ForkJoinTask[newCap];
                    }
                    catch (Throwable ex) {
                        this.top = s;
                        this.access = 0;
                        throw new RejectedExecutionException("Queue capacity exceeded");
                    }
                    if (newCap > 0) {
                        int newMask = newCap - 1;
                        int k = s;
                        do {
                            newArray[k-- & newMask] = task;
                        } while ((task = WorkQueue.getAndClearSlot(a, k & m)) != null);
                    }
                    this.array = newArray;
                } else {
                    a[m & s] = task;
                }
                this.getAndSetAccess(0);
                if ((resize || a[m & s - 1] == null && signalIfEmpty) && pool != null) {
                    pool.signalWork();
                }
            }
        }

        final ForkJoinTask<?> nextLocalTask(int fifo) {
            int cap;
            ForkJoinTask<?> t = null;
            ForkJoinTask<?>[] a = this.array;
            int p = this.top;
            int s = p - 1;
            int b = this.base;
            if (p - b > 0 && a != null && (cap = a.length) > 0) {
                do {
                    int nb;
                    if (fifo == 0 || (nb = b + 1) == p) {
                        t = WorkQueue.getAndClearSlot(a, cap - 1 & s);
                        if (t == null) break;
                        this.top = s;
                        break;
                    }
                    t = WorkQueue.getAndClearSlot(a, cap - 1 & b);
                    if (t != null) {
                        this.base = nb;
                        break;
                    }
                    while (b == (b = this.base)) {
                        U.loadFence();
                        Thread.onSpinWait();
                    }
                } while (p - b > 0);
                U.storeStoreFence();
            }
            return t;
        }

        final ForkJoinTask<?> nextLocalTask() {
            return this.nextLocalTask(this.config & 0x10000);
        }

        final boolean tryUnpush(ForkJoinTask<?> task, boolean owned) {
            int s;
            int k;
            int cap;
            ForkJoinTask<?>[] a = this.array;
            int p = this.top;
            if (task != null && this.base != p && a != null && (cap = a.length) > 0 && a[k = cap - 1 & (s = p - 1)] == task && (owned || this.getAndSetAccess(1) == 0)) {
                if (this.top != p || a[k] != task || WorkQueue.getAndClearSlot(a, k) == null) {
                    this.access = 0;
                } else {
                    this.top = s;
                    this.access = 0;
                    return true;
                }
            }
            return false;
        }

        final ForkJoinTask<?> peek() {
            int cap;
            ForkJoinTask<?>[] a = this.array;
            int cfg = this.config;
            int p = this.top;
            int b = this.base;
            if (p != b && a != null && (cap = a.length) > 0) {
                if ((cfg & 0x10000) == 0) {
                    return a[cap - 1 & p - 1];
                }
                while (p - b > 0) {
                    ForkJoinTask<?> t = a[cap - 1 & b];
                    if (t != null) {
                        return t;
                    }
                    ++b;
                }
            }
            return null;
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        final ForkJoinTask<?> poll(ForkJoinPool pool) {
            int b = this.base;
            while (true) {
                int cap;
                ForkJoinTask<?>[] a = this.array;
                if (this.array == null || (cap = a.length) <= 0) return null;
                int k = cap - 1 & b;
                int nb = b + 1;
                int nk = cap - 1 & nb;
                ForkJoinTask<?> t = a[k];
                U.loadFence();
                if (b != (b = this.base)) continue;
                if (t != null && WorkQueue.casSlotToNull(a, k, t)) {
                    this.base = nb;
                    U.storeFence();
                    if (pool == null || a[nk] == null) return t;
                    pool.signalWork();
                    return t;
                }
                if (this.array == a && a[k] == null && a[nk] == null && this.top - b <= 0) return null;
            }
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        final ForkJoinTask<?> tryPoll() {
            int cap;
            int b = this.base;
            ForkJoinTask<?>[] a = this.array;
            if (this.array == null || (cap = a.length) <= 0) return null;
            while (true) {
                int k = cap - 1 & b;
                int nb = b + 1;
                ForkJoinTask<?> t = a[k];
                U.loadFence();
                if (b != (b = this.base)) continue;
                if (t != null) {
                    if (!WorkQueue.casSlotToNull(a, k, t)) return null;
                    this.base = nb;
                    U.storeStoreFence();
                    return t;
                }
                if (a[k] == null) return null;
            }
        }

        final void topLevelExec(ForkJoinTask<?> task, WorkQueue src) {
            int cfg = this.config;
            int fifo = cfg & 0x10000;
            int nstolen = 1;
            while (task != null) {
                task.doExec();
                task = this.nextLocalTask(fifo);
                if (task != null || src == null || (task = src.tryPoll()) == null) continue;
                ++nstolen;
            }
            this.nsteals += nstolen;
            this.source = 0;
            if ((cfg & 0x40000) != 0) {
                ThreadLocalRandom.eraseThreadLocals(Thread.currentThread());
            }
        }

        final int tryRemoveAndExec(ForkJoinTask<?> task, boolean owned) {
            int cap;
            ForkJoinTask<?>[] a = this.array;
            int p = this.top;
            int s = p - 1;
            int d = p - this.base;
            if (task != null && d > 0 && a != null && (cap = a.length) > 0) {
                int m = cap - 1;
                int i = s;
                while (true) {
                    int k;
                    ForkJoinTask<?> t;
                    if ((t = a[k = i & m]) == task) {
                        if (!owned && this.getAndSetAccess(1) != 0) break;
                        if (this.top != p || a[k] != task || WorkQueue.getAndClearSlot(a, k) == null) {
                            this.access = 0;
                            break;
                        }
                        if (i != s && i == this.base) {
                            this.base = i + 1;
                        } else {
                            int j = i;
                            while (j != s) {
                                a[j & m] = WorkQueue.getAndClearSlot(a, ++j & m);
                            }
                            this.top = s;
                        }
                        this.releaseAccess();
                        return task.doExec();
                    }
                    if (t == null || --d == 0) break;
                    --i;
                }
            }
            return 0;
        }

        final int helpComplete(ForkJoinTask<?> task, boolean owned, int limit) {
            int status = 0;
            if (task != null) {
                block0: do {
                    int p;
                    int s;
                    int k;
                    ForkJoinTask<?> t;
                    int cap;
                    if ((status = task.status) < 0) {
                        return status;
                    }
                    ForkJoinTask<?>[] a = this.array;
                    if (this.array == null || (cap = a.length) <= 0 || (t = a[k = cap - 1 & (s = (p = this.top) - 1)]) == null || !(t instanceof CountedCompleter)) break;
                    CountedCompleter<?> f = (CountedCompleter<?>)t;
                    while (f != task) {
                        f = f.completer;
                        if (f != null) continue;
                        break block0;
                    }
                    if (!owned && this.getAndSetAccess(1) != 0) break;
                    if (this.top != p || a[k] != t || WorkQueue.getAndClearSlot(a, k) == null) {
                        this.access = 0;
                        break;
                    }
                    this.top = s;
                    this.releaseAccess();
                    t.doExec();
                } while (limit == 0 || --limit != 0);
                status = task.status;
            }
            return status;
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        final void helpAsyncBlocker(ManagedBlocker blocker) {
            if (blocker == null) return;
            while (true) {
                int cap;
                int b = this.base;
                ForkJoinTask<?>[] a = this.array;
                if (this.array == null || (cap = a.length) <= 0 || b == this.top) return;
                int k = cap - 1 & b;
                int nb = b + 1;
                int nk = cap - 1 & nb;
                ForkJoinTask<?> t = a[k];
                U.loadFence();
                if (this.base != b) continue;
                if (blocker.isReleasable()) return;
                if (a[k] != t) continue;
                if (t != null) {
                    if (!(t instanceof CompletableFuture.AsynchronousCompletionTask)) return;
                    if (!WorkQueue.casSlotToNull(a, k, t)) continue;
                    this.base = nb;
                    U.storeStoreFence();
                    t.doExec();
                    continue;
                }
                if (a[nk] == null) return;
            }
        }

        final boolean isApparentlyUnblocked() {
            Thread.State s;
            ForkJoinWorkerThread wt;
            return this.access != Integer.MIN_VALUE && (wt = this.owner) != null && (s = wt.getState()) != Thread.State.BLOCKED && s != Thread.State.WAITING && s != Thread.State.TIMED_WAITING;
        }

        final void setClearThreadLocals() {
            this.config |= 0x40000;
        }

        static {
            Class<WorkQueue> klass = WorkQueue.class;
            ACCESS = U.objectFieldOffset(klass, "access");
            PHASE = U.objectFieldOffset(klass, "phase");
            Class<ForkJoinTask[]> aklass = ForkJoinTask[].class;
            ABASE = U.arrayBaseOffset(aklass);
            int scale = U.arrayIndexScale(aklass);
            ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
            if ((scale & scale - 1) != 0) {
                throw new Error("array index scale not a power of two");
            }
        }
    }

    public static interface ManagedBlocker {
        public boolean block() throws InterruptedException;

        public boolean isReleasable();
    }

    static final class DefaultForkJoinWorkerThreadFactory
    implements ForkJoinWorkerThreadFactory {
        static volatile AccessControlContext regularACC;
        static volatile AccessControlContext commonACC;

        DefaultForkJoinWorkerThreadFactory() {
        }

        @Override
        public final ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            boolean isCommon = pool.workerNamePrefix == null;
            SecurityManager sm = System.getSecurityManager();
            if (sm != null && isCommon) {
                return DefaultForkJoinWorkerThreadFactory.newCommonWithACC(pool);
            }
            return DefaultForkJoinWorkerThreadFactory.newRegularWithACC(pool);
        }

        static ForkJoinWorkerThread newRegularWithACC(final ForkJoinPool pool) {
            AccessControlContext acc = regularACC;
            if (acc == null) {
                Permissions ps = new Permissions();
                ps.add(new RuntimePermission("getClassLoader"));
                ps.add(new RuntimePermission("setContextClassLoader"));
                regularACC = acc = new AccessControlContext(new ProtectionDomain[]{new ProtectionDomain(null, ps)});
            }
            return AccessController.doPrivileged(new PrivilegedAction<ForkJoinWorkerThread>(){

                @Override
                public ForkJoinWorkerThread run() {
                    return new ForkJoinWorkerThread(null, pool, true, false);
                }
            }, acc);
        }

        static ForkJoinWorkerThread newCommonWithACC(final ForkJoinPool pool) {
            AccessControlContext acc = commonACC;
            if (acc == null) {
                Permissions ps = new Permissions();
                ps.add(new RuntimePermission("getClassLoader"));
                ps.add(new RuntimePermission("setContextClassLoader"));
                ps.add(new RuntimePermission("modifyThread"));
                ps.add(new RuntimePermission("enableContextClassLoaderOverride"));
                ps.add(new RuntimePermission("modifyThreadGroup"));
                commonACC = acc = new AccessControlContext(new ProtectionDomain[]{new ProtectionDomain(null, ps)});
            }
            return AccessController.doPrivileged(new PrivilegedAction<ForkJoinWorkerThread>(){

                @Override
                public ForkJoinWorkerThread run() {
                    return new ForkJoinWorkerThread.InnocuousForkJoinWorkerThread(pool);
                }
            }, acc);
        }
    }
}

