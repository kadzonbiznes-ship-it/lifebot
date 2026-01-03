/*
 * Decompiled with CFR 0.152.
 */
package java.util.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class CountDownLatch {
    private final Sync sync;

    public CountDownLatch(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count < 0");
        }
        this.sync = new Sync(count);
    }

    public void await() throws InterruptedException {
        this.sync.acquireSharedInterruptibly(1);
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return this.sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    public void countDown() {
        this.sync.releaseShared(1);
    }

    public long getCount() {
        return this.sync.getCount();
    }

    public String toString() {
        return super.toString() + "[Count = " + this.sync.getCount() + "]";
    }

    private static final class Sync
    extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        Sync(int count) {
            this.setState(count);
        }

        int getCount() {
            return this.getState();
        }

        @Override
        protected int tryAcquireShared(int acquires) {
            return this.getState() == 0 ? 1 : -1;
        }

        @Override
        protected boolean tryReleaseShared(int releases) {
            int nextc;
            int c;
            do {
                if ((c = this.getState()) != 0) continue;
                return false;
            } while (!this.compareAndSetState(c, nextc = c - 1));
            return nextc == 0;
        }
    }
}

