/*
 * Decompiled with CFR 0.152.
 */
package java.util.concurrent.locks;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReadWriteLock;
import jdk.internal.vm.annotation.ReservedStackAccess;

public class ReentrantReadWriteLock
implements ReadWriteLock,
Serializable {
    private static final long serialVersionUID = -6992448646407690164L;
    private final ReadLock readerLock;
    private final WriteLock writerLock;
    final Sync sync;

    public ReentrantReadWriteLock() {
        this(false);
    }

    public ReentrantReadWriteLock(boolean fair) {
        this.sync = fair ? new FairSync() : new NonfairSync();
        this.readerLock = new ReadLock(this);
        this.writerLock = new WriteLock(this);
    }

    @Override
    public WriteLock writeLock() {
        return this.writerLock;
    }

    @Override
    public ReadLock readLock() {
        return this.readerLock;
    }

    public final boolean isFair() {
        return this.sync instanceof FairSync;
    }

    protected Thread getOwner() {
        return this.sync.getOwner();
    }

    public int getReadLockCount() {
        return this.sync.getReadLockCount();
    }

    public boolean isWriteLocked() {
        return this.sync.isWriteLocked();
    }

    public boolean isWriteLockedByCurrentThread() {
        return this.sync.isHeldExclusively();
    }

    public int getWriteHoldCount() {
        return this.sync.getWriteHoldCount();
    }

    public int getReadHoldCount() {
        return this.sync.getReadHoldCount();
    }

    protected Collection<Thread> getQueuedWriterThreads() {
        return this.sync.getExclusiveQueuedThreads();
    }

    protected Collection<Thread> getQueuedReaderThreads() {
        return this.sync.getSharedQueuedThreads();
    }

    public final boolean hasQueuedThreads() {
        return this.sync.hasQueuedThreads();
    }

    public final boolean hasQueuedThread(Thread thread) {
        return this.sync.isQueued(thread);
    }

    public final int getQueueLength() {
        return this.sync.getQueueLength();
    }

    protected Collection<Thread> getQueuedThreads() {
        return this.sync.getQueuedThreads();
    }

    public boolean hasWaiters(Condition condition) {
        if (condition == null) {
            throw new NullPointerException();
        }
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject)) {
            throw new IllegalArgumentException("not owner");
        }
        return this.sync.hasWaiters((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    public int getWaitQueueLength(Condition condition) {
        if (condition == null) {
            throw new NullPointerException();
        }
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject)) {
            throw new IllegalArgumentException("not owner");
        }
        return this.sync.getWaitQueueLength((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    protected Collection<Thread> getWaitingThreads(Condition condition) {
        if (condition == null) {
            throw new NullPointerException();
        }
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject)) {
            throw new IllegalArgumentException("not owner");
        }
        return this.sync.getWaitingThreads((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    public String toString() {
        int c = this.sync.getCount();
        int w = Sync.exclusiveCount(c);
        int r = Sync.sharedCount(c);
        return super.toString() + "[Write locks = " + w + ", Read locks = " + r + "]";
    }

    static final class FairSync
    extends Sync {
        private static final long serialVersionUID = -2274990926593161451L;

        FairSync() {
        }

        @Override
        final boolean writerShouldBlock() {
            return this.hasQueuedPredecessors();
        }

        @Override
        final boolean readerShouldBlock() {
            return this.hasQueuedPredecessors();
        }
    }

    static final class NonfairSync
    extends Sync {
        private static final long serialVersionUID = -8159625535654395037L;

        NonfairSync() {
        }

        @Override
        final boolean writerShouldBlock() {
            return false;
        }

        @Override
        final boolean readerShouldBlock() {
            return this.apparentlyFirstQueuedIsExclusive();
        }
    }

    static abstract class Sync
    extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 6317671515068378041L;
        static final int SHARED_SHIFT = 16;
        static final int SHARED_UNIT = 65536;
        static final int MAX_COUNT = 65535;
        static final int EXCLUSIVE_MASK = 65535;
        private transient ThreadLocalHoldCounter readHolds = new ThreadLocalHoldCounter();
        private transient HoldCounter cachedHoldCounter;
        private transient Thread firstReader;
        private transient int firstReaderHoldCount;

        static int sharedCount(int c) {
            return c >>> 16;
        }

        static int exclusiveCount(int c) {
            return c & 0xFFFF;
        }

        Sync() {
            this.setState(this.getState());
        }

        abstract boolean readerShouldBlock();

        abstract boolean writerShouldBlock();

        @Override
        @ReservedStackAccess
        protected final boolean tryRelease(int releases) {
            boolean free;
            if (!this.isHeldExclusively()) {
                throw new IllegalMonitorStateException();
            }
            int nextc = this.getState() - releases;
            boolean bl = free = Sync.exclusiveCount(nextc) == 0;
            if (free) {
                this.setExclusiveOwnerThread(null);
            }
            this.setState(nextc);
            return free;
        }

        @Override
        @ReservedStackAccess
        protected final boolean tryAcquire(int acquires) {
            Thread current = Thread.currentThread();
            int c = this.getState();
            int w = Sync.exclusiveCount(c);
            if (c != 0) {
                if (w == 0 || current != this.getExclusiveOwnerThread()) {
                    return false;
                }
                if (w + Sync.exclusiveCount(acquires) > 65535) {
                    throw new Error("Maximum lock count exceeded");
                }
                this.setState(c + acquires);
                return true;
            }
            if (this.writerShouldBlock() || !this.compareAndSetState(c, c + acquires)) {
                return false;
            }
            this.setExclusiveOwnerThread(current);
            return true;
        }

        @Override
        @ReservedStackAccess
        protected final boolean tryReleaseShared(int unused) {
            int nextc;
            int c;
            Thread current = Thread.currentThread();
            if (this.firstReader == current) {
                if (this.firstReaderHoldCount == 1) {
                    this.firstReader = null;
                } else {
                    --this.firstReaderHoldCount;
                }
            } else {
                int count;
                HoldCounter rh = this.cachedHoldCounter;
                if (rh == null || rh.tid != LockSupport.getThreadId(current)) {
                    rh = (HoldCounter)this.readHolds.get();
                }
                if ((count = rh.count) <= 1) {
                    this.readHolds.remove();
                    if (count <= 0) {
                        throw Sync.unmatchedUnlockException();
                    }
                }
                --rh.count;
            }
            while (!this.compareAndSetState(c = this.getState(), nextc = c - 65536)) {
            }
            return nextc == 0;
        }

        private static IllegalMonitorStateException unmatchedUnlockException() {
            return new IllegalMonitorStateException("attempt to unlock read lock, not locked by current thread");
        }

        @Override
        @ReservedStackAccess
        protected final int tryAcquireShared(int unused) {
            Thread current = Thread.currentThread();
            int c = this.getState();
            if (Sync.exclusiveCount(c) != 0 && this.getExclusiveOwnerThread() != current) {
                return -1;
            }
            int r = Sync.sharedCount(c);
            if (!this.readerShouldBlock() && r < 65535 && this.compareAndSetState(c, c + 65536)) {
                if (r == 0) {
                    this.firstReader = current;
                    this.firstReaderHoldCount = 1;
                } else if (this.firstReader == current) {
                    ++this.firstReaderHoldCount;
                } else {
                    HoldCounter rh = this.cachedHoldCounter;
                    if (rh == null || rh.tid != LockSupport.getThreadId(current)) {
                        this.cachedHoldCounter = rh = (HoldCounter)this.readHolds.get();
                    } else if (rh.count == 0) {
                        this.readHolds.set(rh);
                    }
                    ++rh.count;
                }
                return 1;
            }
            return this.fullTryAcquireShared(current);
        }

        final int fullTryAcquireShared(Thread current) {
            int c;
            HoldCounter rh = null;
            do {
                if (Sync.exclusiveCount(c = this.getState()) != 0) {
                    if (this.getExclusiveOwnerThread() != current) {
                        return -1;
                    }
                } else if (this.readerShouldBlock() && this.firstReader != current) {
                    if (rh == null && ((rh = this.cachedHoldCounter) == null || rh.tid != LockSupport.getThreadId(current))) {
                        rh = (HoldCounter)this.readHolds.get();
                        if (rh.count == 0) {
                            this.readHolds.remove();
                        }
                    }
                    if (rh.count == 0) {
                        return -1;
                    }
                }
                if (Sync.sharedCount(c) != 65535) continue;
                throw new Error("Maximum lock count exceeded");
            } while (!this.compareAndSetState(c, c + 65536));
            if (Sync.sharedCount(c) == 0) {
                this.firstReader = current;
                this.firstReaderHoldCount = 1;
            } else if (this.firstReader == current) {
                ++this.firstReaderHoldCount;
            } else {
                if (rh == null) {
                    rh = this.cachedHoldCounter;
                }
                if (rh == null || rh.tid != LockSupport.getThreadId(current)) {
                    rh = (HoldCounter)this.readHolds.get();
                } else if (rh.count == 0) {
                    this.readHolds.set(rh);
                }
                ++rh.count;
                this.cachedHoldCounter = rh;
            }
            return 1;
        }

        @ReservedStackAccess
        final boolean tryWriteLock() {
            Thread current = Thread.currentThread();
            int c = this.getState();
            if (c != 0) {
                int w = Sync.exclusiveCount(c);
                if (w == 0 || current != this.getExclusiveOwnerThread()) {
                    return false;
                }
                if (w == 65535) {
                    throw new Error("Maximum lock count exceeded");
                }
            }
            if (!this.compareAndSetState(c, c + 1)) {
                return false;
            }
            this.setExclusiveOwnerThread(current);
            return true;
        }

        @ReservedStackAccess
        final boolean tryReadLock() {
            int r;
            int c;
            Thread current = Thread.currentThread();
            do {
                if (Sync.exclusiveCount(c = this.getState()) != 0 && this.getExclusiveOwnerThread() != current) {
                    return false;
                }
                r = Sync.sharedCount(c);
                if (r != 65535) continue;
                throw new Error("Maximum lock count exceeded");
            } while (!this.compareAndSetState(c, c + 65536));
            if (r == 0) {
                this.firstReader = current;
                this.firstReaderHoldCount = 1;
            } else if (this.firstReader == current) {
                ++this.firstReaderHoldCount;
            } else {
                HoldCounter rh = this.cachedHoldCounter;
                if (rh == null || rh.tid != LockSupport.getThreadId(current)) {
                    this.cachedHoldCounter = rh = (HoldCounter)this.readHolds.get();
                } else if (rh.count == 0) {
                    this.readHolds.set(rh);
                }
                ++rh.count;
            }
            return true;
        }

        @Override
        protected final boolean isHeldExclusively() {
            return this.getExclusiveOwnerThread() == Thread.currentThread();
        }

        final AbstractQueuedSynchronizer.ConditionObject newCondition() {
            return new AbstractQueuedSynchronizer.ConditionObject(this);
        }

        final Thread getOwner() {
            return Sync.exclusiveCount(this.getState()) == 0 ? null : this.getExclusiveOwnerThread();
        }

        final int getReadLockCount() {
            return Sync.sharedCount(this.getState());
        }

        final boolean isWriteLocked() {
            return Sync.exclusiveCount(this.getState()) != 0;
        }

        final int getWriteHoldCount() {
            return this.isHeldExclusively() ? Sync.exclusiveCount(this.getState()) : 0;
        }

        final int getReadHoldCount() {
            if (this.getReadLockCount() == 0) {
                return 0;
            }
            Thread current = Thread.currentThread();
            if (this.firstReader == current) {
                return this.firstReaderHoldCount;
            }
            HoldCounter rh = this.cachedHoldCounter;
            if (rh != null && rh.tid == LockSupport.getThreadId(current)) {
                return rh.count;
            }
            int count = ((HoldCounter)this.readHolds.get()).count;
            if (count == 0) {
                this.readHolds.remove();
            }
            return count;
        }

        private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
            s.defaultReadObject();
            this.readHolds = new ThreadLocalHoldCounter();
            this.setState(0);
        }

        final int getCount() {
            return this.getState();
        }

        static final class ThreadLocalHoldCounter
        extends ThreadLocal<HoldCounter> {
            ThreadLocalHoldCounter() {
            }

            @Override
            public HoldCounter initialValue() {
                return new HoldCounter();
            }
        }

        static final class HoldCounter {
            int count;
            final long tid = LockSupport.getThreadId(Thread.currentThread());

            HoldCounter() {
            }
        }
    }

    public static class ReadLock
    implements Lock,
    Serializable {
        private static final long serialVersionUID = -5992448646407690164L;
        private final Sync sync;

        protected ReadLock(ReentrantReadWriteLock lock) {
            this.sync = lock.sync;
        }

        @Override
        public void lock() {
            this.sync.acquireShared(1);
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            this.sync.acquireSharedInterruptibly(1);
        }

        @Override
        public boolean tryLock() {
            return this.sync.tryReadLock();
        }

        @Override
        public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
            return this.sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
        }

        @Override
        public void unlock() {
            this.sync.releaseShared(1);
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        public String toString() {
            int r = this.sync.getReadLockCount();
            return super.toString() + "[Read locks = " + r + "]";
        }
    }

    public static class WriteLock
    implements Lock,
    Serializable {
        private static final long serialVersionUID = -4992448646407690164L;
        private final Sync sync;

        protected WriteLock(ReentrantReadWriteLock lock) {
            this.sync = lock.sync;
        }

        @Override
        public void lock() {
            this.sync.acquire(1);
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            this.sync.acquireInterruptibly(1);
        }

        @Override
        public boolean tryLock() {
            return this.sync.tryWriteLock();
        }

        @Override
        public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
            return this.sync.tryAcquireNanos(1, unit.toNanos(timeout));
        }

        @Override
        public void unlock() {
            this.sync.release(1);
        }

        @Override
        public Condition newCondition() {
            return this.sync.newCondition();
        }

        public String toString() {
            Thread o = this.sync.getOwner();
            return super.toString() + (o == null ? "[Unlocked]" : "[Locked by thread " + o.getName() + "]");
        }

        public boolean isHeldByCurrentThread() {
            return this.sync.isHeldExclusively();
        }

        public int getHoldCount() {
            return this.sync.getWriteHoldCount();
        }
    }
}

