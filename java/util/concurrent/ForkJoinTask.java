/*
 * Decompiled with CFR 0.152.
 */
package java.util.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;
import jdk.internal.misc.Unsafe;

public abstract class ForkJoinTask<V>
implements Future<V>,
Serializable {
    static final int DONE = Integer.MIN_VALUE;
    static final int ABNORMAL = 65536;
    static final int THROWN = 131072;
    static final int SMASK = 65535;
    static final int UNCOMPENSATE = 65536;
    static final int POOLSUBMIT = 262144;
    static final int RAN = 1;
    static final int INTERRUPTIBLE = 2;
    static final int TIMED = 4;
    volatile int status;
    private volatile transient Aux aux;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long STATUS = U.objectFieldOffset(ForkJoinTask.class, "status");
    private static final long AUX = U.objectFieldOffset(ForkJoinTask.class, "aux");
    private static final long serialVersionUID = -7721805057305804111L;

    private int getAndBitwiseOrStatus(int v) {
        return U.getAndBitwiseOrInt(this, STATUS, v);
    }

    private boolean casStatus(int c, int v) {
        return U.compareAndSetInt(this, STATUS, c, v);
    }

    private boolean casAux(Aux c, Aux v) {
        return U.compareAndSetReference(this, AUX, c, v);
    }

    final void markPoolSubmission() {
        this.getAndBitwiseOrStatus(262144);
    }

    private void signalWaiters() {
        Aux a;
        while ((a = this.aux) != null && a.ex == null) {
            if (!this.casAux(a, null)) continue;
            while (a != null) {
                Thread t = a.thread;
                if (t != Thread.currentThread() && t != null) {
                    LockSupport.unpark(t);
                }
                a = a.next;
            }
            break block0;
        }
    }

    private int setDone() {
        int s = this.getAndBitwiseOrStatus(Integer.MIN_VALUE) | Integer.MIN_VALUE;
        this.signalWaiters();
        return s;
    }

    private int trySetCancelled() {
        int s;
        while ((s = this.status) >= 0 && !this.casStatus(s, s |= 0x80010000)) {
        }
        this.signalWaiters();
        return s;
    }

    final int trySetThrown(Throwable ex) {
        int s;
        Aux h = new Aux(Thread.currentThread(), ex);
        Aux p = null;
        boolean installed = false;
        while ((s = this.status) >= 0) {
            Aux a;
            if (!installed && ((a = this.aux) == null || a.ex == null) && (installed = this.casAux(a, h))) {
                p = a;
            }
            if (!installed || !this.casStatus(s, s |= 0x80030000)) continue;
        }
        while (p != null) {
            LockSupport.unpark(p.thread);
            p = p.next;
        }
        return s;
    }

    int trySetException(Throwable ex) {
        return this.trySetThrown(ex);
    }

    static boolean isExceptionalStatus(int s) {
        return (s & 0x20000) != 0;
    }

    final int doExec() {
        int s = this.status;
        if (s >= 0) {
            boolean completed;
            try {
                completed = this.exec();
            }
            catch (Throwable rex) {
                s = this.trySetException(rex);
                completed = false;
            }
            if (completed) {
                s = this.setDone();
            }
        }
        return s;
    }

    private int awaitDone(int how, long deadline) {
        int s;
        block25: {
            Aux a;
            ForkJoinPool p;
            ForkJoinPool.WorkQueue q = null;
            boolean timed = (how & 4) != 0;
            boolean owned = false;
            boolean uncompensate = false;
            Thread t = Thread.currentThread();
            if (t instanceof ForkJoinWorkerThread) {
                owned = true;
                ForkJoinWorkerThread wt = (ForkJoinWorkerThread)t;
                q = wt.workQueue;
                p = wt.pool;
            } else {
                p = ForkJoinPool.common;
                if (p != null && (how & 0x40000) == 0) {
                    q = p.externalQueue();
                }
            }
            if (q != null && p != null) {
                if (this instanceof CountedCompleter) {
                    s = p.helpComplete(this, q, owned, timed);
                } else if ((how & 1) != 0 || (s = q.tryRemoveAndExec(this, owned)) >= 0) {
                    int n = s = owned ? p.helpJoin(this, q, timed) : 0;
                }
                if (s < 0) {
                    return s;
                }
                if (s == 65536) {
                    uncompensate = true;
                }
            }
            Aux node = null;
            long ns = 0L;
            boolean interrupted = false;
            boolean queued = false;
            while ((s = this.status) >= 0) {
                if (node == null) {
                    node = new Aux(Thread.currentThread(), null);
                    continue;
                }
                if (!queued) {
                    a = this.aux;
                    if (a != null && a.ex != null || !(queued = this.casAux(node.next = a, node))) continue;
                    LockSupport.setCurrentBlocker(this);
                    continue;
                }
                if (timed && (ns = deadline - System.nanoTime()) <= 0L) {
                    s = 0;
                    break;
                }
                if (Thread.interrupted()) {
                    interrupted = true;
                    if ((how & 0x40000) != 0 && p != null && p.runState < 0) {
                        ForkJoinTask.cancelIgnoringExceptions(this);
                        continue;
                    }
                    if ((how & 2) == 0) continue;
                    s = 65536;
                    break;
                }
                s = this.status;
                if (s < 0) break;
                if (timed) {
                    LockSupport.parkNanos(ns);
                    continue;
                }
                LockSupport.park();
            }
            if (uncompensate) {
                p.uncompensate();
            }
            if (queued) {
                LockSupport.setCurrentBlocker(null);
                if (s >= 0) {
                    block1: while ((a = this.aux) != null && a.ex == null) {
                        Aux next;
                        Aux trail = null;
                        do {
                            next = a.next;
                            if (a == node) {
                                if (trail != null) {
                                    trail.casNext(trail, next);
                                    continue block1;
                                }
                                if (!this.casAux(a, next)) continue block1;
                                break block25;
                            }
                            trail = a;
                        } while ((a = next) != null);
                        break;
                    }
                } else {
                    this.signalWaiters();
                    if (interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        return s;
    }

    static final void cancelIgnoringExceptions(Future<?> t) {
        if (t != null) {
            try {
                t.cancel(true);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    private Throwable getThrowableException() {
        Throwable ex;
        Aux a = this.aux;
        if (a == null) {
            ex = null;
        } else {
            ex = a.ex;
            if (ex != null && a.thread != Thread.currentThread()) {
                try {
                    Constructor<?> noArgCtor = null;
                    Constructor<?> oneArgCtor = null;
                    for (Constructor<?> c : ex.getClass().getConstructors()) {
                        Class<?>[] ps = c.getParameterTypes();
                        if (ps.length == 0) {
                            noArgCtor = c;
                            continue;
                        }
                        if (ps.length != 1 || ps[0] != Throwable.class) continue;
                        oneArgCtor = c;
                        break;
                    }
                    if (oneArgCtor != null) {
                        ex = (Throwable)oneArgCtor.newInstance(ex);
                    } else if (noArgCtor != null) {
                        Throwable rx = (Throwable)noArgCtor.newInstance(new Object[0]);
                        rx.initCause(ex);
                        ex = rx;
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        return ex;
    }

    private Throwable getException(int s) {
        Throwable ex = null;
        if ((s & 0x10000) != 0 && (ex = this.getThrowableException()) == null) {
            ex = new CancellationException();
        }
        return ex;
    }

    private void reportException(int s) {
        ForkJoinTask.uncheckedThrow(this.getThrowableException());
    }

    private void reportExecutionException(int s) {
        Exception ex = null;
        if (s == 65536) {
            ex = new InterruptedException();
        } else if (s >= 0) {
            ex = new TimeoutException();
        } else {
            Throwable rx = this.getThrowableException();
            if (rx != null) {
                ex = new ExecutionException(rx);
            }
        }
        ForkJoinTask.uncheckedThrow(ex);
    }

    static void rethrow(Throwable ex) {
        ForkJoinTask.uncheckedThrow(ex);
    }

    static <T extends Throwable> void uncheckedThrow(Throwable t) throws T {
        if (t == null) {
            t = new CancellationException();
        }
        throw t;
    }

    public final ForkJoinTask<V> fork() {
        ForkJoinPool.WorkQueue q;
        ForkJoinPool p;
        U.storeStoreFence();
        Thread t = Thread.currentThread();
        if (t instanceof ForkJoinWorkerThread) {
            ForkJoinWorkerThread wt = (ForkJoinWorkerThread)t;
            p = wt.pool;
            q = wt.workQueue;
        } else {
            p = ForkJoinPool.common;
            q = p.submissionQueue(false);
        }
        q.push(this, p, true);
        return this;
    }

    public final V join() {
        int s = this.status;
        if (s >= 0) {
            s = this.awaitDone(s & 0x40000, 0L);
        }
        if ((s & 0x10000) != 0) {
            this.reportException(s);
        }
        return this.getRawResult();
    }

    public final V invoke() {
        int s = this.doExec();
        if (s >= 0) {
            s = this.awaitDone(1, 0L);
        }
        if ((s & 0x10000) != 0) {
            this.reportException(s);
        }
        return this.getRawResult();
    }

    public static void invokeAll(ForkJoinTask<?> t1, ForkJoinTask<?> t2) {
        if (t1 == null || t2 == null) {
            throw new NullPointerException();
        }
        t2.fork();
        int s1 = t1.doExec();
        if (s1 >= 0) {
            s1 = t1.awaitDone(1, 0L);
        }
        if ((s1 & 0x10000) != 0) {
            ForkJoinTask.cancelIgnoringExceptions(t2);
            t1.reportException(s1);
        } else {
            int s2 = t2.status;
            if (s2 >= 0) {
                s2 = t2.awaitDone(0, 0L);
            }
            if ((s2 & 0x10000) != 0) {
                t2.reportException(s2);
            }
        }
    }

    public static void invokeAll(ForkJoinTask<?> ... tasks) {
        int s;
        ForkJoinTask<?> t;
        int last;
        int i;
        Throwable ex = null;
        for (i = last = tasks.length - 1; i >= 0; --i) {
            t = tasks[i];
            if (t == null) {
                ex = new NullPointerException();
                break;
            }
            if (i == 0) {
                s = t.doExec();
                if (s >= 0) {
                    s = t.awaitDone(1, 0L);
                }
                if ((s & 0x10000) == 0) break;
                ex = t.getException(s);
                break;
            }
            t.fork();
        }
        if (ex == null) {
            for (i = 1; i <= last; ++i) {
                t = tasks[i];
                if (t == null) continue;
                s = t.status;
                if (s >= 0) {
                    s = t.awaitDone(0, 0L);
                }
                if ((s & 0x10000) != 0 && (ex = t.getException(s)) != null) break;
            }
        }
        if (ex != null) {
            for (i = 1; i <= last; ++i) {
                ForkJoinTask.cancelIgnoringExceptions(tasks[i]);
            }
            ForkJoinTask.rethrow(ex);
        }
    }

    public static <T extends ForkJoinTask<?>> Collection<T> invokeAll(Collection<T> tasks) {
        int s;
        ForkJoinTask t;
        int last;
        int i;
        if (!(tasks instanceof RandomAccess) || !(tasks instanceof List)) {
            ForkJoinTask.invokeAll(tasks.toArray(new ForkJoinTask[0]));
            return tasks;
        }
        List ts = (List)tasks;
        Throwable ex = null;
        for (i = last = ts.size() - 1; i >= 0; --i) {
            t = (ForkJoinTask)ts.get(i);
            if (t == null) {
                ex = new NullPointerException();
                break;
            }
            if (i == 0) {
                s = t.doExec();
                if (s >= 0) {
                    s = t.awaitDone(1, 0L);
                }
                if ((s & 0x10000) == 0) break;
                ex = t.getException(s);
                break;
            }
            t.fork();
        }
        if (ex == null) {
            for (i = 1; i <= last; ++i) {
                t = (ForkJoinTask)ts.get(i);
                if (t == null) continue;
                s = t.status;
                if (s >= 0) {
                    s = t.awaitDone(0, 0L);
                }
                if ((s & 0x10000) != 0 && (ex = t.getException(s)) != null) break;
            }
        }
        if (ex != null) {
            for (i = 1; i <= last; ++i) {
                ForkJoinTask.cancelIgnoringExceptions((Future)ts.get(i));
            }
            ForkJoinTask.rethrow(ex);
        }
        return tasks;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return (this.trySetCancelled() & 0x30000) == 65536;
    }

    @Override
    public final boolean isDone() {
        return this.status < 0;
    }

    @Override
    public final boolean isCancelled() {
        return (this.status & 0x30000) == 65536;
    }

    public final boolean isCompletedAbnormally() {
        return (this.status & 0x10000) != 0;
    }

    public final boolean isCompletedNormally() {
        return (this.status & 0x80010000) == Integer.MIN_VALUE;
    }

    @Override
    public Future.State state() {
        int s = this.status;
        return s >= 0 ? Future.State.RUNNING : ((s & 0x80010000) == Integer.MIN_VALUE ? Future.State.SUCCESS : ((s & 0x30000) == 196608 ? Future.State.FAILED : Future.State.CANCELLED));
    }

    @Override
    public V resultNow() {
        if (!this.isCompletedNormally()) {
            throw new IllegalStateException();
        }
        return this.getRawResult();
    }

    @Override
    public Throwable exceptionNow() {
        if ((this.status & 0x30000) != 196608) {
            throw new IllegalStateException();
        }
        return this.getThrowableException();
    }

    public final Throwable getException() {
        return this.getException(this.status);
    }

    public void completeExceptionally(Throwable ex) {
        this.trySetException(ex instanceof RuntimeException || ex instanceof Error ? ex : new RuntimeException(ex));
    }

    public void complete(V value) {
        try {
            this.setRawResult(value);
        }
        catch (Throwable rex) {
            this.trySetException(rex);
            return;
        }
        this.setDone();
    }

    public final void quietlyComplete() {
        this.setDone();
    }

    @Override
    public final V get() throws InterruptedException, ExecutionException {
        int s;
        if (Thread.interrupted()) {
            s = 65536;
        } else {
            s = this.status;
            if (s >= 0) {
                s = this.awaitDone(s & 0x40000 | 2, 0L);
            }
        }
        if ((s & 0x10000) != 0) {
            this.reportExecutionException(s);
        }
        return this.getRawResult();
    }

    @Override
    public final V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        int s;
        long nanos = unit.toNanos(timeout);
        if (Thread.interrupted()) {
            s = 65536;
        } else {
            s = this.status;
            if (s >= 0 && nanos > 0L) {
                s = this.awaitDone(s & 0x40000 | 2 | 4, nanos + System.nanoTime());
            }
        }
        if (s >= 0 || (s & 0x10000) != 0) {
            this.reportExecutionException(s);
        }
        return this.getRawResult();
    }

    public final void quietlyJoin() {
        int s = this.status;
        if (s >= 0) {
            this.awaitDone(s & 0x40000, 0L);
        }
    }

    public final void quietlyInvoke() {
        int s = this.doExec();
        if (s >= 0) {
            this.awaitDone(1, 0L);
        }
    }

    public final boolean quietlyJoin(long timeout, TimeUnit unit) throws InterruptedException {
        int s;
        long nanos = unit.toNanos(timeout);
        if (Thread.interrupted()) {
            s = 65536;
        } else {
            s = this.status;
            if (s >= 0 && nanos > 0L) {
                s = this.awaitDone(s & 0x40000 | 2 | 4, nanos + System.nanoTime());
            }
        }
        if (s == 65536) {
            throw new InterruptedException();
        }
        return s < 0;
    }

    public final boolean quietlyJoinUninterruptibly(long timeout, TimeUnit unit) {
        long nanos = unit.toNanos(timeout);
        int s = this.status;
        if (s >= 0 && nanos > 0L) {
            s = this.awaitDone(s & 0x40000 | 4, nanos + System.nanoTime());
        }
        return s < 0;
    }

    public static void helpQuiesce() {
        ForkJoinPool.helpQuiescePool(null, Long.MAX_VALUE, false);
    }

    public void reinitialize() {
        this.aux = null;
        this.status = 0;
    }

    public static ForkJoinPool getPool() {
        Thread t = Thread.currentThread();
        return t instanceof ForkJoinWorkerThread ? ((ForkJoinWorkerThread)t).pool : null;
    }

    public static boolean inForkJoinPool() {
        return Thread.currentThread() instanceof ForkJoinWorkerThread;
    }

    public boolean tryUnfork() {
        Thread t = Thread.currentThread();
        boolean owned = t instanceof ForkJoinWorkerThread;
        ForkJoinPool.WorkQueue q = owned ? ((ForkJoinWorkerThread)t).workQueue : ForkJoinPool.commonQueue();
        return q != null && q.tryUnpush(this, owned);
    }

    public static int getQueuedTaskCount() {
        Thread t = Thread.currentThread();
        ForkJoinPool.WorkQueue q = t instanceof ForkJoinWorkerThread ? ((ForkJoinWorkerThread)t).workQueue : ForkJoinPool.commonQueue();
        return q == null ? 0 : q.queueSize();
    }

    public static int getSurplusQueuedTaskCount() {
        return ForkJoinPool.getSurplusQueuedTaskCount();
    }

    public abstract V getRawResult();

    protected abstract void setRawResult(V var1);

    protected abstract boolean exec();

    protected static ForkJoinTask<?> peekNextLocalTask() {
        Thread t = Thread.currentThread();
        ForkJoinPool.WorkQueue q = t instanceof ForkJoinWorkerThread ? ((ForkJoinWorkerThread)t).workQueue : ForkJoinPool.commonQueue();
        return q == null ? null : q.peek();
    }

    protected static ForkJoinTask<?> pollNextLocalTask() {
        Thread t = Thread.currentThread();
        return t instanceof ForkJoinWorkerThread ? ((ForkJoinWorkerThread)t).workQueue.nextLocalTask() : null;
    }

    protected static ForkJoinTask<?> pollTask() {
        ForkJoinTask<?> forkJoinTask;
        Thread t = Thread.currentThread();
        if (t instanceof ForkJoinWorkerThread) {
            ForkJoinWorkerThread w = (ForkJoinWorkerThread)t;
            forkJoinTask = w.pool.nextTaskFor(w.workQueue);
        } else {
            forkJoinTask = null;
        }
        return forkJoinTask;
    }

    protected static ForkJoinTask<?> pollSubmission() {
        Thread t = Thread.currentThread();
        return t instanceof ForkJoinWorkerThread ? ((ForkJoinWorkerThread)t).pool.pollSubmission() : null;
    }

    public final short getForkJoinTaskTag() {
        return (short)this.status;
    }

    public final short setForkJoinTaskTag(short newValue) {
        int s;
        while (!this.casStatus(s = this.status, s & 0xFFFF0000 | newValue & 0xFFFF)) {
        }
        return (short)s;
    }

    public final boolean compareAndSetForkJoinTaskTag(short expect, short update) {
        int s;
        do {
            if ((short)(s = this.status) == expect) continue;
            return false;
        } while (!this.casStatus(s, s & 0xFFFF0000 | update & 0xFFFF));
        return true;
    }

    public static ForkJoinTask<?> adapt(Runnable runnable) {
        return new AdaptedRunnableAction(runnable);
    }

    public static <T> ForkJoinTask<T> adapt(Runnable runnable, T result) {
        return new AdaptedRunnable<T>(runnable, result);
    }

    public static <T> ForkJoinTask<T> adapt(Callable<? extends T> callable) {
        return new AdaptedCallable<T>(callable);
    }

    public static <T> ForkJoinTask<T> adaptInterruptible(Callable<? extends T> callable) {
        return new AdaptedInterruptibleCallable<T>(callable);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        Aux a = this.aux;
        s.writeObject(a == null ? null : a.ex);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        Object ex = s.readObject();
        if (ex != null) {
            this.trySetThrown((Throwable)ex);
        }
    }

    static {
        Class<LockSupport> dep1 = LockSupport.class;
        Class<Aux> clazz = Aux.class;
    }

    static final class Aux {
        final Thread thread;
        final Throwable ex;
        Aux next;
        private static final Unsafe U = Unsafe.getUnsafe();
        private static final long NEXT = U.objectFieldOffset(Aux.class, "next");

        Aux(Thread thread, Throwable ex) {
            this.thread = thread;
            this.ex = ex;
        }

        final boolean casNext(Aux c, Aux v) {
            return U.compareAndSetReference(this, NEXT, c, v);
        }
    }

    static final class AdaptedRunnableAction
    extends ForkJoinTask<Void>
    implements RunnableFuture<Void> {
        final Runnable runnable;
        private static final long serialVersionUID = 5232453952276885070L;

        AdaptedRunnableAction(Runnable runnable) {
            Objects.requireNonNull(runnable);
            this.runnable = runnable;
        }

        @Override
        public final Void getRawResult() {
            return null;
        }

        @Override
        public final void setRawResult(Void v) {
        }

        @Override
        public final boolean exec() {
            this.runnable.run();
            return true;
        }

        @Override
        public final void run() {
            this.invoke();
        }

        public String toString() {
            return super.toString() + "[Wrapped task = " + this.runnable + "]";
        }
    }

    static final class AdaptedRunnable<T>
    extends ForkJoinTask<T>
    implements RunnableFuture<T> {
        final Runnable runnable;
        T result;
        private static final long serialVersionUID = 5232453952276885070L;

        AdaptedRunnable(Runnable runnable, T result) {
            Objects.requireNonNull(runnable);
            this.runnable = runnable;
            this.result = result;
        }

        @Override
        public final T getRawResult() {
            return this.result;
        }

        @Override
        public final void setRawResult(T v) {
            this.result = v;
        }

        @Override
        public final boolean exec() {
            this.runnable.run();
            return true;
        }

        @Override
        public final void run() {
            this.invoke();
        }

        public String toString() {
            return super.toString() + "[Wrapped task = " + this.runnable + "]";
        }
    }

    static final class AdaptedCallable<T>
    extends ForkJoinTask<T>
    implements RunnableFuture<T> {
        final Callable<? extends T> callable;
        T result;
        private static final long serialVersionUID = 2838392045355241008L;

        AdaptedCallable(Callable<? extends T> callable) {
            Objects.requireNonNull(callable);
            this.callable = callable;
        }

        @Override
        public final T getRawResult() {
            return this.result;
        }

        @Override
        public final void setRawResult(T v) {
            this.result = v;
        }

        @Override
        public final boolean exec() {
            try {
                this.result = this.callable.call();
                return true;
            }
            catch (RuntimeException rex) {
                throw rex;
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public final void run() {
            this.invoke();
        }

        public String toString() {
            return super.toString() + "[Wrapped task = " + this.callable + "]";
        }
    }

    static final class AdaptedInterruptibleCallable<T>
    extends InterruptibleTask<T> {
        final Callable<? extends T> callable;
        T result;
        private static final long serialVersionUID = 2838392045355241008L;

        AdaptedInterruptibleCallable(Callable<? extends T> callable) {
            Objects.requireNonNull(callable);
            this.callable = callable;
        }

        @Override
        public final T getRawResult() {
            return this.result;
        }

        @Override
        public final void setRawResult(T v) {
            this.result = v;
        }

        @Override
        final T compute() throws Exception {
            return this.callable.call();
        }

        @Override
        final Object adaptee() {
            return this.callable;
        }
    }

    static final class RunnableExecuteAction
    extends InterruptibleTask<Void> {
        final Runnable runnable;
        private static final long serialVersionUID = 5232453952276885070L;

        RunnableExecuteAction(Runnable runnable) {
            Objects.requireNonNull(runnable);
            this.runnable = runnable;
        }

        @Override
        public final Void getRawResult() {
            return null;
        }

        @Override
        public final void setRawResult(Void v) {
        }

        @Override
        final Void compute() {
            this.runnable.run();
            return null;
        }

        @Override
        final Object adaptee() {
            return this.runnable;
        }

        void onAuxExceptionSet(Throwable ex) {
            Thread t = Thread.currentThread();
            Thread.UncaughtExceptionHandler h = t.getUncaughtExceptionHandler();
            if (h != null) {
                try {
                    h.uncaughtException(t, ex);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
    }
}

