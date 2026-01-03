/*
 * Decompiled with CFR 0.152.
 */
package java.util.concurrent.locks;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReadWriteLock;
import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ReservedStackAccess;

public class StampedLock
implements Serializable {
    private static final long serialVersionUID = -6001602636862214147L;
    private static final int LG_READERS = 7;
    private static final long RUNIT = 1L;
    private static final long WBIT = 128L;
    private static final long RBITS = 127L;
    private static final long RFULL = 126L;
    private static final long ABITS = 255L;
    private static final long SBITS = -128L;
    private static final long RSAFE = -193L;
    private static final long ORIGIN = 256L;
    private static final long INTERRUPTED = 1L;
    static final int WAITING = 1;
    static final int CANCELLED = Integer.MIN_VALUE;
    private volatile transient Node head;
    private volatile transient Node tail;
    transient ReadLockView readLockView;
    transient WriteLockView writeLockView;
    transient ReadWriteLockView readWriteLockView;
    private volatile transient long state = 256L;
    private transient int readerOverflow;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long STATE = U.objectFieldOffset(StampedLock.class, "state");
    private static final long HEAD = U.objectFieldOffset(StampedLock.class, "head");
    private static final long TAIL = U.objectFieldOffset(StampedLock.class, "tail");

    private boolean casState(long expect, long update) {
        return U.compareAndSetLong(this, STATE, expect, update);
    }

    @ReservedStackAccess
    private long tryAcquireWrite() {
        long nextState;
        long s = this.state;
        if ((s & 0xFFL) == 0L && this.casState(s, nextState = s | 0x80L)) {
            U.storeStoreFence();
            return nextState;
        }
        return 0L;
    }

    @ReservedStackAccess
    private long tryAcquireRead() {
        long nextState;
        while (true) {
            long s;
            long m;
            if ((m = (s = this.state) & 0xFFL) < 126L) {
                nextState = s + 1L;
                if (!this.casState(s, nextState)) continue;
                return nextState;
            }
            if (m == 128L) {
                return 0L;
            }
            nextState = this.tryIncReaderOverflow(s);
            if (nextState != 0L) break;
        }
        return nextState;
    }

    private static long unlockWriteState(long s) {
        return (s += 128L) == 0L ? 256L : s;
    }

    private long releaseWrite(long s) {
        long nextState = this.state = StampedLock.unlockWriteState(s);
        StampedLock.signalNext(this.head);
        return nextState;
    }

    @ReservedStackAccess
    public long writeLock() {
        long nextState;
        long s = U.getLongOpaque(this, STATE) & 0xFFFFFFFFFFFFFF00L;
        if (this.casState(s, nextState = s | 0x80L)) {
            U.storeStoreFence();
            return nextState;
        }
        return this.acquireWrite(false, false, 0L);
    }

    public long tryWriteLock() {
        return this.tryAcquireWrite();
    }

    public long tryWriteLock(long time, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(time);
        if (!Thread.interrupted()) {
            long nextState = this.tryAcquireWrite();
            if (nextState != 0L) {
                return nextState;
            }
            if (nanos <= 0L) {
                return 0L;
            }
            nextState = this.acquireWrite(true, true, System.nanoTime() + nanos);
            if (nextState != 1L) {
                return nextState;
            }
        }
        throw new InterruptedException();
    }

    public long writeLockInterruptibly() throws InterruptedException {
        long nextState;
        if (!(Thread.interrupted() || (nextState = this.tryAcquireWrite()) == 0L && (nextState = this.acquireWrite(true, false, 0L)) == 1L)) {
            return nextState;
        }
        throw new InterruptedException();
    }

    @ReservedStackAccess
    public long readLock() {
        long nextState;
        long s = U.getLongOpaque(this, STATE) & 0xFFFFFFFFFFFFFF3FL;
        if (this.casState(s, nextState = s + 1L)) {
            return nextState;
        }
        return this.acquireRead(false, false, 0L);
    }

    public long tryReadLock() {
        return this.tryAcquireRead();
    }

    public long tryReadLock(long time, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(time);
        if (!Thread.interrupted()) {
            long nextState;
            if (this.tail == this.head && (nextState = this.tryAcquireRead()) != 0L) {
                return nextState;
            }
            if (nanos <= 0L) {
                return 0L;
            }
            nextState = this.acquireRead(true, true, System.nanoTime() + nanos);
            if (nextState != 1L) {
                return nextState;
            }
        }
        throw new InterruptedException();
    }

    public long readLockInterruptibly() throws InterruptedException {
        long nextState;
        if (!(Thread.interrupted() || (nextState = this.tryAcquireRead()) == 0L && (nextState = this.acquireRead(true, false, 0L)) == 1L)) {
            return nextState;
        }
        throw new InterruptedException();
    }

    public long tryOptimisticRead() {
        long s = this.state;
        return (s & 0x80L) == 0L ? s & 0xFFFFFFFFFFFFFF80L : 0L;
    }

    public boolean validate(long stamp) {
        U.loadFence();
        return (stamp & 0xFFFFFFFFFFFFFF80L) == (this.state & 0xFFFFFFFFFFFFFF80L);
    }

    @ReservedStackAccess
    public void unlockWrite(long stamp) {
        if (this.state != stamp || (stamp & 0x80L) == 0L) {
            throw new IllegalMonitorStateException();
        }
        this.releaseWrite(stamp);
    }

    @ReservedStackAccess
    public void unlockRead(long stamp) {
        if ((stamp & 0x7FL) != 0L) {
            long m;
            long s;
            while (((s = this.state) & 0xFFFFFFFFFFFFFF80L) == (stamp & 0xFFFFFFFFFFFFFF80L) && (m = s & 0x7FL) != 0L) {
                if (m < 126L) {
                    if (!this.casState(s, s - 1L)) continue;
                    if (m == 1L) {
                        StampedLock.signalNext(this.head);
                    }
                    return;
                }
                if (this.tryDecReaderOverflow(s) == 0L) continue;
                return;
            }
        }
        throw new IllegalMonitorStateException();
    }

    public void unlock(long stamp) {
        if ((stamp & 0x80L) != 0L) {
            this.unlockWrite(stamp);
        } else {
            this.unlockRead(stamp);
        }
    }

    public long tryConvertToWriteLock(long stamp) {
        long s;
        long a = stamp & 0xFFL;
        while (((s = this.state) & 0xFFFFFFFFFFFFFF80L) == (stamp & 0xFFFFFFFFFFFFFF80L)) {
            long nextState;
            long m = s & 0xFFL;
            if (m == 0L) {
                if (a != 0L) break;
                nextState = s | 0x80L;
                if (!this.casState(s, nextState)) continue;
                U.storeStoreFence();
                return nextState;
            }
            if (m == 128L) {
                if (a != m) break;
                return stamp;
            }
            if (m != 1L || a == 0L) break;
            nextState = s - 1L + 128L;
            if (!this.casState(s, nextState)) continue;
            return nextState;
        }
        return 0L;
    }

    public long tryConvertToReadLock(long stamp) {
        long s;
        while (((s = this.state) & 0xFFFFFFFFFFFFFF80L) == (stamp & 0xFFFFFFFFFFFFFF80L)) {
            long nextState;
            long a = stamp & 0xFFL;
            if (a >= 128L) {
                if (s != stamp) break;
                nextState = this.state = StampedLock.unlockWriteState(s) + 1L;
                StampedLock.signalNext(this.head);
                return nextState;
            }
            if (a == 0L) {
                if (!((s & 0xFFL) < 126L ? this.casState(s, nextState = s + 1L) : (nextState = this.tryIncReaderOverflow(s)) != 0L)) continue;
                return nextState;
            }
            if ((s & 0xFFL) == 0L) break;
            return stamp;
        }
        return 0L;
    }

    public long tryConvertToOptimisticRead(long stamp) {
        long s;
        U.loadFence();
        while (((s = this.state) & 0xFFFFFFFFFFFFFF80L) == (stamp & 0xFFFFFFFFFFFFFF80L)) {
            long nextState;
            long a = stamp & 0xFFL;
            if (a >= 128L) {
                if (s != stamp) break;
                return this.releaseWrite(s);
            }
            if (a == 0L) {
                return stamp;
            }
            long m = s & 0xFFL;
            if (m == 0L) break;
            if (m < 126L) {
                nextState = s - 1L;
                if (!this.casState(s, nextState)) continue;
                if (m == 1L) {
                    StampedLock.signalNext(this.head);
                }
                return nextState & 0xFFFFFFFFFFFFFF80L;
            }
            nextState = this.tryDecReaderOverflow(s);
            if (nextState == 0L) continue;
            return nextState & 0xFFFFFFFFFFFFFF80L;
        }
        return 0L;
    }

    @ReservedStackAccess
    public boolean tryUnlockWrite() {
        long s = this.state;
        if ((s & 0x80L) != 0L) {
            this.releaseWrite(s);
            return true;
        }
        return false;
    }

    @ReservedStackAccess
    public boolean tryUnlockRead() {
        long s;
        long m;
        while ((m = (s = this.state) & 0xFFL) != 0L && m < 128L) {
            if (m < 126L) {
                if (!this.casState(s, s - 1L)) continue;
                if (m == 1L) {
                    StampedLock.signalNext(this.head);
                }
                return true;
            }
            if (this.tryDecReaderOverflow(s) == 0L) continue;
            return true;
        }
        return false;
    }

    private int getReadLockCount(long s) {
        long readers = s & 0x7FL;
        if (readers >= 126L) {
            readers = 126L + (long)this.readerOverflow;
        }
        return (int)readers;
    }

    public boolean isWriteLocked() {
        return (this.state & 0x80L) != 0L;
    }

    public boolean isReadLocked() {
        return (this.state & 0x7FL) != 0L;
    }

    public static boolean isWriteLockStamp(long stamp) {
        return (stamp & 0xFFL) == 128L;
    }

    public static boolean isReadLockStamp(long stamp) {
        return (stamp & 0x7FL) != 0L;
    }

    public static boolean isLockStamp(long stamp) {
        return (stamp & 0xFFL) != 0L;
    }

    public static boolean isOptimisticReadStamp(long stamp) {
        return (stamp & 0xFFL) == 0L && stamp != 0L;
    }

    public int getReadLockCount() {
        return this.getReadLockCount(this.state);
    }

    public String toString() {
        long s = this.state;
        return super.toString() + ((s & 0xFFL) == 0L ? "[Unlocked]" : ((s & 0x80L) != 0L ? "[Write-locked]" : "[Read-locks:" + this.getReadLockCount(s) + "]"));
    }

    public Lock asReadLock() {
        ReadLockView v = this.readLockView;
        if (v != null) {
            return v;
        }
        this.readLockView = new ReadLockView();
        return this.readLockView;
    }

    public Lock asWriteLock() {
        WriteLockView v = this.writeLockView;
        if (v != null) {
            return v;
        }
        this.writeLockView = new WriteLockView();
        return this.writeLockView;
    }

    public ReadWriteLock asReadWriteLock() {
        ReadWriteLockView v = this.readWriteLockView;
        if (v != null) {
            return v;
        }
        this.readWriteLockView = new ReadWriteLockView();
        return this.readWriteLockView;
    }

    final void unstampedUnlockWrite() {
        long s = this.state;
        if ((s & 0x80L) == 0L) {
            throw new IllegalMonitorStateException();
        }
        this.releaseWrite(s);
    }

    final void unstampedUnlockRead() {
        long s;
        long m;
        while ((m = (s = this.state) & 0x7FL) > 0L) {
            if (m < 126L) {
                if (!this.casState(s, s - 1L)) continue;
                if (m == 1L) {
                    StampedLock.signalNext(this.head);
                }
                return;
            }
            if (this.tryDecReaderOverflow(s) == 0L) continue;
            return;
        }
        throw new IllegalMonitorStateException();
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.state = 256L;
    }

    private long tryIncReaderOverflow(long s) {
        if ((s & 0xFFL) != 126L) {
            Thread.onSpinWait();
        } else if (this.casState(s, s | 0x7FL)) {
            ++this.readerOverflow;
            this.state = s;
            return this.state;
        }
        return 0L;
    }

    private long tryDecReaderOverflow(long s) {
        if ((s & 0xFFL) != 126L) {
            Thread.onSpinWait();
        } else if (this.casState(s, s | 0x7FL)) {
            long nextState;
            int r = this.readerOverflow;
            if (r > 0) {
                this.readerOverflow = r - 1;
                nextState = s;
            } else {
                nextState = s - 1L;
            }
            this.state = nextState;
            return this.state;
        }
        return 0L;
    }

    static final void signalNext(Node h) {
        Node s;
        if (h != null && (s = h.next) != null && s.status > 0) {
            s.getAndUnsetStatus(1);
            LockSupport.unpark(s.waiter);
        }
    }

    private static void signalCowaiters(ReaderNode node) {
        if (node != null) {
            ReaderNode c;
            while ((c = node.cowaiters) != null) {
                if (!node.casCowaiters(c, c.cowaiters)) continue;
                LockSupport.unpark(c.waiter);
            }
        }
    }

    private boolean casTail(Node c, Node v) {
        return U.compareAndSetReference(this, TAIL, c, v);
    }

    private void tryInitializeHead() {
        WriterNode h = new WriterNode();
        if (U.compareAndSetReference(this, HEAD, null, h)) {
            this.tail = h;
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private long acquireWrite(boolean interruptible, boolean timed, long time) {
        int spins = 0;
        int postSpins = 0;
        boolean interrupted = false;
        boolean first = false;
        WriterNode node = null;
        Node pred = null;
        while (true) {
            long nextState;
            long s;
            if (!first && (pred = node == null ? null : node.prev) != null && !(first = this.head == pred)) {
                if (pred.status < 0) {
                    this.cleanQueue();
                    continue;
                }
                if (pred.prev == null) {
                    Thread.onSpinWait();
                    continue;
                }
            }
            if ((first || pred == null) && ((s = this.state) & 0xFFL) == 0L && this.casState(s, nextState = s | 0x80L)) {
                U.storeStoreFence();
                if (!first) return nextState;
                node.prev = null;
                this.head = node;
                pred.next = null;
                node.waiter = null;
                if (!interrupted) return nextState;
                Thread.currentThread().interrupt();
                return nextState;
            }
            if (node == null) {
                node = new WriterNode();
                continue;
            }
            if (pred == null) {
                Node t = this.tail;
                node.setPrevRelaxed(t);
                if (t == null) {
                    this.tryInitializeHead();
                    continue;
                }
                if (!this.casTail(t, node)) {
                    node.setPrevRelaxed(null);
                    continue;
                }
                t.next = node;
                continue;
            }
            if (first && spins != 0) {
                spins = (byte)(spins - 1);
                Thread.onSpinWait();
                continue;
            }
            if (node.status == 0) {
                if (node.waiter == null) {
                    node.waiter = Thread.currentThread();
                }
                node.status = 1;
                continue;
            }
            byte by = (byte)(postSpins << 1 | 1);
            postSpins = by;
            spins = by;
            if (!timed) {
                LockSupport.park(this);
            } else {
                long nanos = time - System.nanoTime();
                if (nanos <= 0L) return this.cancelAcquire(node, interrupted);
                LockSupport.parkNanos(this, nanos);
            }
            node.clearStatus();
            if ((interrupted |= Thread.interrupted()) && interruptible) return this.cancelAcquire(node, interrupted);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private long acquireRead(boolean interruptible, boolean timed, long time) {
        boolean interrupted = false;
        ReaderNode node = null;
        while (true) {
            long nextState;
            Node tailPred = null;
            Node t = this.tail;
            if ((t == null || (tailPred = t.prev) == null) && (nextState = this.tryAcquireRead()) != 0L) {
                return nextState;
            }
            if (t == null) {
                this.tryInitializeHead();
                continue;
            }
            if (tailPred == null || !(t instanceof ReaderNode)) {
                if (node == null) {
                    node = new ReaderNode();
                }
                if (this.tail != t) continue;
                node.setPrevRelaxed(t);
                if (this.casTail(t, node)) break;
                node.setPrevRelaxed(null);
                continue;
            }
            ReaderNode leader = (ReaderNode)t;
            if (leader != this.tail) continue;
            boolean attached = false;
            while (leader.status >= 0 && leader.prev != null) {
                if (node == null) {
                    node = new ReaderNode();
                    continue;
                }
                if (node.waiter == null) {
                    node.waiter = Thread.currentThread();
                    continue;
                }
                if (!attached) {
                    ReaderNode c = leader.cowaiters;
                    node.setCowaitersRelaxed(c);
                    attached = leader.casCowaiters(c, node);
                    if (attached) continue;
                    node.setCowaitersRelaxed(null);
                    continue;
                }
                long nanos = 0L;
                if (!timed) {
                    LockSupport.park(this);
                } else {
                    nanos = time - System.nanoTime();
                    if (nanos > 0L) {
                        LockSupport.parkNanos(this, nanos);
                    }
                }
                if ((!(interrupted |= Thread.interrupted()) || !interruptible) && (!timed || nanos > 0L)) continue;
                return this.cancelCowaiter(node, leader, interrupted);
            }
            if (node != null) {
                node.waiter = null;
            }
            long ns = this.tryAcquireRead();
            StampedLock.signalCowaiters(leader);
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
            if (ns != 0L) {
                return ns;
            }
            node = null;
        }
        t.next = node;
        int spins = 0;
        int postSpins = 0;
        boolean first = false;
        Node pred = null;
        while (true) {
            long nextState;
            if (!first && (pred = node.prev) != null && !(first = this.head == pred)) {
                if (pred.status < 0) {
                    this.cleanQueue();
                    continue;
                }
                if (pred.prev == null) {
                    Thread.onSpinWait();
                    continue;
                }
            }
            if ((first || pred == null) && (nextState = this.tryAcquireRead()) != 0L) {
                if (first) {
                    node.prev = null;
                    this.head = node;
                    pred.next = null;
                    node.waiter = null;
                }
                StampedLock.signalCowaiters(node);
                if (!interrupted) return nextState;
                Thread.currentThread().interrupt();
                return nextState;
            }
            if (first && spins != 0) {
                spins = (byte)(spins - 1);
                Thread.onSpinWait();
                continue;
            }
            if (node.status == 0) {
                if (node.waiter == null) {
                    node.waiter = Thread.currentThread();
                }
                node.status = 1;
                continue;
            }
            byte by = (byte)(postSpins << 1 | 1);
            postSpins = by;
            spins = by;
            if (!timed) {
                LockSupport.park(this);
            } else {
                long nanos = time - System.nanoTime();
                if (nanos <= 0L) return this.cancelAcquire(node, interrupted);
                LockSupport.parkNanos(this, nanos);
            }
            node.clearStatus();
            if ((interrupted |= Thread.interrupted()) && interruptible) return this.cancelAcquire(node, interrupted);
        }
    }

    private void cleanQueue() {
        block0: while (true) {
            Node q = this.tail;
            Node s = null;
            while (true) {
                Node p;
                if (q == null || (p = q.prev) == null) {
                    return;
                }
                if (s != null ? s.prev != q || s.status < 0 : this.tail != q) continue block0;
                if (q.status < 0) {
                    if (!(s == null ? this.casTail(q, p) : s.casPrev(q, p)) || q.prev != p) continue block0;
                    p.casNext(q, s);
                    if (p.prev != null) continue block0;
                    StampedLock.signalNext(p);
                    continue block0;
                }
                Node n = p.next;
                if (n != q) {
                    if (n == null || q.prev != p || q.status < 0) continue block0;
                    p.casNext(n, q);
                    if (p.prev != null) continue block0;
                    StampedLock.signalNext(p);
                    continue block0;
                }
                s = q;
                q = q.prev;
            }
            break;
        }
    }

    private void unlinkCowaiter(ReaderNode node, ReaderNode leader) {
        if (leader != null) {
            block0: while (leader.prev != null && leader.status >= 0) {
                ReaderNode p = leader;
                ReaderNode q;
                while ((q = p.cowaiters) != null) {
                    if (q == node) {
                        p.casCowaiters(q, q.cowaiters);
                        continue block0;
                    }
                    p = q;
                }
                return;
            }
        }
    }

    private long cancelAcquire(Node node, boolean interrupted) {
        if (node != null) {
            node.waiter = null;
            node.status = Integer.MIN_VALUE;
            this.cleanQueue();
            if (node instanceof ReaderNode) {
                StampedLock.signalCowaiters((ReaderNode)node);
            }
        }
        return interrupted || Thread.interrupted() ? 1L : 0L;
    }

    private long cancelCowaiter(ReaderNode node, ReaderNode leader, boolean interrupted) {
        if (node != null) {
            node.waiter = null;
            node.status = Integer.MIN_VALUE;
            this.unlinkCowaiter(node, leader);
        }
        return interrupted || Thread.interrupted() ? 1L : 0L;
    }

    static {
        Class<LockSupport> clazz = LockSupport.class;
    }

    static abstract class Node {
        volatile Node prev;
        volatile Node next;
        Thread waiter;
        volatile int status;
        private static final long STATUS = U.objectFieldOffset(Node.class, "status");
        private static final long NEXT = U.objectFieldOffset(Node.class, "next");
        private static final long PREV = U.objectFieldOffset(Node.class, "prev");

        Node() {
        }

        final boolean casPrev(Node c, Node v) {
            return U.weakCompareAndSetReference(this, PREV, c, v);
        }

        final boolean casNext(Node c, Node v) {
            return U.weakCompareAndSetReference(this, NEXT, c, v);
        }

        final int getAndUnsetStatus(int v) {
            return U.getAndBitwiseAndInt(this, STATUS, ~v);
        }

        final void setPrevRelaxed(Node p) {
            U.putReference(this, PREV, p);
        }

        final void setStatusRelaxed(int s) {
            U.putInt(this, STATUS, s);
        }

        final void clearStatus() {
            U.putIntOpaque(this, STATUS, 0);
        }
    }

    final class ReadLockView
    implements Lock {
        ReadLockView() {
        }

        @Override
        public void lock() {
            StampedLock.this.readLock();
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            StampedLock.this.readLockInterruptibly();
        }

        @Override
        public boolean tryLock() {
            return StampedLock.this.tryReadLock() != 0L;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return StampedLock.this.tryReadLock(time, unit) != 0L;
        }

        @Override
        public void unlock() {
            StampedLock.this.unstampedUnlockRead();
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
    }

    final class WriteLockView
    implements Lock {
        WriteLockView() {
        }

        @Override
        public void lock() {
            StampedLock.this.writeLock();
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            StampedLock.this.writeLockInterruptibly();
        }

        @Override
        public boolean tryLock() {
            return StampedLock.this.tryWriteLock() != 0L;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return StampedLock.this.tryWriteLock(time, unit) != 0L;
        }

        @Override
        public void unlock() {
            StampedLock.this.unstampedUnlockWrite();
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
    }

    final class ReadWriteLockView
    implements ReadWriteLock {
        ReadWriteLockView() {
        }

        @Override
        public Lock readLock() {
            return StampedLock.this.asReadLock();
        }

        @Override
        public Lock writeLock() {
            return StampedLock.this.asWriteLock();
        }
    }

    static final class ReaderNode
    extends Node {
        volatile ReaderNode cowaiters;
        private static final long COWAITERS = U.objectFieldOffset(ReaderNode.class, "cowaiters");

        ReaderNode() {
        }

        final boolean casCowaiters(ReaderNode c, ReaderNode v) {
            return U.weakCompareAndSetReference(this, COWAITERS, c, v);
        }

        final void setCowaitersRelaxed(ReaderNode p) {
            U.putReference(this, COWAITERS, p);
        }
    }

    static final class WriterNode
    extends Node {
        WriterNode() {
        }
    }
}

