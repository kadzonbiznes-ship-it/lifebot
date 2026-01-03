/*
 * Decompiled with CFR 0.152.
 */
package java.util.concurrent;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DelayQueue<E extends Delayed>
extends AbstractQueue<E>
implements BlockingQueue<E> {
    private final transient ReentrantLock lock = new ReentrantLock();
    private final PriorityQueue<E> q = new PriorityQueue();
    private Thread leader;
    private final Condition available = this.lock.newCondition();

    public DelayQueue() {
    }

    public DelayQueue(Collection<? extends E> c) {
        this.addAll(c);
    }

    @Override
    public boolean add(E e) {
        return this.offer(e);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean offer(E e) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.q.offer(e);
            if (this.q.peek() == e) {
                this.leader = null;
                this.available.signal();
            }
            boolean bl = true;
            return bl;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void put(E e) {
        this.offer(e);
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) {
        return this.offer(e);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public E poll() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Delayed first = (Delayed)this.q.peek();
            Delayed delayed = first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0L ? null : (Delayed)this.q.poll();
            return (E)delayed;
        }
        finally {
            lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    @Override
    public E take() throws InterruptedException {
        lock = this.lock;
        lock.lockInterruptibly();
        block7: while (true) {
            while (true) lbl-1000:
            // 4 sources

            {
                if ((first = (Delayed)this.q.peek()) == null) {
                    this.available.await();
                    continue block7;
                }
                delay = first.getDelay(TimeUnit.NANOSECONDS);
                if (delay <= 0L) {
                    var5_4 = (Delayed)this.q.poll();
                    return (E)var5_4;
                }
                first = null;
                if (this.leader != null) {
                    this.available.await();
                    continue;
                }
                this.leader = thisThread = Thread.currentThread();
                try {
                    this.available.awaitNanos(delay);
                }
                finally {
                    if (this.leader != thisThread) continue;
                    this.leader = null;
                    continue;
                }
                break;
            }
            break;
        }
        ** GOTO lbl-1000
        finally {
            if (this.leader == null && this.q.peek() != null) {
                this.available.signal();
            }
            lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        nanos = unit.toNanos(timeout);
        lock = this.lock;
        lock.lockInterruptibly();
        while (true) lbl-1000:
        // 5 sources

        {
            if ((first = (Delayed)this.q.peek()) == null) {
                if (nanos <= 0L) {
                    var8_7 = null;
                    return var8_7;
                }
                nanos = this.available.awaitNanos(nanos);
                continue;
            }
            delay = first.getDelay(TimeUnit.NANOSECONDS);
            if (delay <= 0L) {
                var10_8 = (Delayed)this.q.poll();
                return (E)var10_8;
            }
            if (nanos <= 0L) {
                var10_8 = null;
                return (E)var10_8;
            }
            first = null;
            if (nanos < delay || this.leader != null) {
                nanos = this.available.awaitNanos(nanos);
                continue;
            }
            this.leader = thisThread = Thread.currentThread();
            try {
                timeLeft = this.available.awaitNanos(delay);
                nanos -= delay - timeLeft;
            }
            finally {
                if (this.leader != thisThread) continue;
                this.leader = null;
                continue;
            }
            break;
        }
        ** GOTO lbl-1000
        finally {
            if (this.leader == null && this.q.peek() != null) {
                this.available.signal();
            }
            lock.unlock();
        }
    }

    @Override
    public E remove() {
        return (E)((Delayed)super.remove());
    }

    @Override
    public E peek() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Delayed delayed = (Delayed)this.q.peek();
            return (E)delayed;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = this.q.size();
            return n;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return this.drainTo(c, Integer.MAX_VALUE);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        Objects.requireNonNull(c);
        if (c == this) {
            throw new IllegalArgumentException();
        }
        if (maxElements <= 0) {
            return 0;
        }
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Delayed first;
            int n;
            for (n = 0; n < maxElements && (first = (Delayed)this.q.peek()) != null && first.getDelay(TimeUnit.NANOSECONDS) <= 0L; ++n) {
                c.add(first);
                this.q.poll();
            }
            int n2 = n;
            return n2;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.q.clear();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Object[] toArray() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] objectArray = this.q.toArray();
            return objectArray;
        }
        finally {
            lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public <T> T[] toArray(T[] a) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            T[] TArray = this.q.toArray(a);
            return TArray;
        }
        finally {
            lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean remove(Object o) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            boolean bl = this.q.remove(o);
            return bl;
        }
        finally {
            lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void removeEQ(Object o) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Iterator<E> it = this.q.iterator();
            while (it.hasNext()) {
                if (o != it.next()) continue;
                it.remove();
                break;
            }
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr(this.toArray());
    }

    private class Itr
    implements Iterator<E> {
        final Object[] array;
        int cursor;
        int lastRet = -1;

        Itr(Object[] array) {
            this.array = array;
        }

        @Override
        public boolean hasNext() {
            return this.cursor < this.array.length;
        }

        @Override
        public E next() {
            if (this.cursor >= this.array.length) {
                throw new NoSuchElementException();
            }
            this.lastRet = this.cursor++;
            return (Delayed)this.array[this.lastRet];
        }

        @Override
        public void remove() {
            if (this.lastRet < 0) {
                throw new IllegalStateException();
            }
            DelayQueue.this.removeEQ(this.array[this.lastRet]);
            this.lastRet = -1;
        }
    }
}

