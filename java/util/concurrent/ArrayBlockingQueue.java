/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.util.concurrent.ArrayBlockingQueue$Itrs.Node
 */
package java.util.concurrent;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Helpers;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ArrayBlockingQueue<E>
extends AbstractQueue<E>
implements BlockingQueue<E>,
Serializable {
    private static final long serialVersionUID = -817911632652898426L;
    final Object[] items;
    int takeIndex;
    int putIndex;
    int count;
    final ReentrantLock lock;
    private final Condition notEmpty;
    private final Condition notFull;
    transient Itrs itrs;

    static final int inc(int i, int modulus) {
        if (++i >= modulus) {
            i = 0;
        }
        return i;
    }

    static final int dec(int i, int modulus) {
        if (--i < 0) {
            i = modulus - 1;
        }
        return i;
    }

    final E itemAt(int i) {
        return (E)this.items[i];
    }

    static <E> E itemAt(Object[] items, int i) {
        return (E)items[i];
    }

    private void enqueue(E e) {
        Object[] items = this.items;
        items[this.putIndex] = e;
        if (++this.putIndex == items.length) {
            this.putIndex = 0;
        }
        ++this.count;
        this.notEmpty.signal();
    }

    private E dequeue() {
        Object[] items = this.items;
        Object e = items[this.takeIndex];
        items[this.takeIndex] = null;
        if (++this.takeIndex == items.length) {
            this.takeIndex = 0;
        }
        --this.count;
        if (this.itrs != null) {
            this.itrs.elementDequeued();
        }
        this.notFull.signal();
        return (E)e;
    }

    void removeAt(int removeIndex) {
        Object[] items = this.items;
        if (removeIndex == this.takeIndex) {
            items[this.takeIndex] = null;
            if (++this.takeIndex == items.length) {
                this.takeIndex = 0;
            }
            --this.count;
            if (this.itrs != null) {
                this.itrs.elementDequeued();
            }
        } else {
            int pred;
            int i = removeIndex;
            int putIndex = this.putIndex;
            while (true) {
                pred = i++;
                if (i == items.length) {
                    i = 0;
                }
                if (i == putIndex) break;
                items[pred] = items[i];
            }
            items[pred] = null;
            this.putIndex = pred;
            --this.count;
            if (this.itrs != null) {
                this.itrs.removedAt(removeIndex);
            }
        }
        this.notFull.signal();
    }

    public ArrayBlockingQueue(int capacity) {
        this(capacity, false);
    }

    public ArrayBlockingQueue(int capacity, boolean fair) {
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }
        this.items = new Object[capacity];
        this.lock = new ReentrantLock(fair);
        this.notEmpty = this.lock.newCondition();
        this.notFull = this.lock.newCondition();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ArrayBlockingQueue(int capacity, boolean fair, Collection<? extends E> c) {
        this(capacity, fair);
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] items = this.items;
            int i = 0;
            try {
                for (E e : c) {
                    items[i++] = Objects.requireNonNull(e);
                }
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                throw new IllegalArgumentException();
            }
            this.count = i;
            this.putIndex = i == capacity ? 0 : i;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public boolean add(E e) {
        return super.add(e);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean offer(E e) {
        Objects.requireNonNull(e);
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (this.count == this.items.length) {
                boolean bl = false;
                return bl;
            }
            this.enqueue(e);
            boolean bl = true;
            return bl;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void put(E e) throws InterruptedException {
        Objects.requireNonNull(e);
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (this.count == this.items.length) {
                this.notFull.await();
            }
            this.enqueue(e);
        }
        finally {
            lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        Objects.requireNonNull(e);
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (this.count == this.items.length) {
                if (nanos <= 0L) {
                    boolean bl = false;
                    return bl;
                }
                nanos = this.notFull.awaitNanos(nanos);
            }
            this.enqueue(e);
            boolean bl = true;
            return bl;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public E poll() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            E e = this.count == 0 ? null : (E)this.dequeue();
            return e;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public E take() throws InterruptedException {
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (this.count == 0) {
                this.notEmpty.await();
            }
            E e = this.dequeue();
            return e;
        }
        finally {
            lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (this.count == 0) {
                if (nanos <= 0L) {
                    E e = null;
                    return e;
                }
                nanos = this.notEmpty.awaitNanos(nanos);
            }
            E e = this.dequeue();
            return e;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public E peek() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            E e = this.itemAt(this.takeIndex);
            return e;
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
            int n = this.count;
            return n;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public int remainingCapacity() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = this.items.length - this.count;
            return n;
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
        if (o == null) {
            return false;
        }
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (this.count > 0) {
                int to;
                Object[] items = this.items;
                int i = this.takeIndex;
                int end = this.putIndex;
                int n = to = i < end ? end : items.length;
                while (true) {
                    if (i < to) {
                        if (o.equals(items[i])) {
                            this.removeAt(i);
                            boolean bl = true;
                            return bl;
                        }
                        ++i;
                        continue;
                    }
                    if (to == end) break;
                    i = 0;
                    to = end;
                }
            }
            boolean bl = false;
            return bl;
        }
        finally {
            lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (this.count > 0) {
                int to;
                Object[] items = this.items;
                int i = this.takeIndex;
                int end = this.putIndex;
                int n = to = i < end ? end : items.length;
                while (true) {
                    if (i < to) {
                        if (o.equals(items[i])) {
                            boolean bl = true;
                            return bl;
                        }
                        ++i;
                        continue;
                    }
                    if (to == end) break;
                    i = 0;
                    to = end;
                }
            }
            boolean bl = false;
            return bl;
        }
        finally {
            lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Object[] toArray() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] items = this.items;
            int end = this.takeIndex + this.count;
            Object[] a = Arrays.copyOfRange(items, this.takeIndex, end);
            if (end != this.putIndex) {
                System.arraycopy(items, 0, a, items.length - this.takeIndex, this.putIndex);
            }
            Object[] objectArray = a;
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
            Object[] items = this.items;
            int count = this.count;
            int firstLeg = Math.min(items.length - this.takeIndex, count);
            if (a.length < count) {
                a = Arrays.copyOfRange(items, this.takeIndex, this.takeIndex + count, a.getClass());
            } else {
                System.arraycopy(items, this.takeIndex, a, 0, firstLeg);
                if (a.length > count) {
                    a[count] = null;
                }
            }
            if (firstLeg < count) {
                System.arraycopy(items, 0, a, firstLeg, this.putIndex);
            }
            T[] TArray = a;
            return TArray;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return Helpers.collectionToString(this);
    }

    @Override
    public void clear() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int k = this.count;
            if (k > 0) {
                ArrayBlockingQueue.circularClear(this.items, this.takeIndex, this.putIndex);
                this.takeIndex = this.putIndex;
                this.count = 0;
                if (this.itrs != null) {
                    this.itrs.queueIsEmpty();
                }
                while (k > 0 && lock.hasWaiters(this.notFull)) {
                    this.notFull.signal();
                    --k;
                }
            }
        }
        finally {
            lock.unlock();
        }
    }

    private static void circularClear(Object[] items, int i, int end) {
        int to;
        int n = to = i < end ? end : items.length;
        while (true) {
            if (i < to) {
                items[i] = null;
                ++i;
                continue;
            }
            if (to == end) break;
            i = 0;
            to = end;
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
        Object[] items = this.items;
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n;
            block17: {
                int i;
                block18: {
                    int n2 = Math.min(maxElements, this.count);
                    int take = this.takeIndex;
                    try {
                        for (i = 0; i < n2; ++i) {
                            Object e = items[take];
                            c.add(e);
                            items[take] = null;
                            if (++take != items.length) continue;
                            take = 0;
                        }
                        n = n2;
                        if (i <= 0) break block17;
                        this.count -= i;
                        this.takeIndex = take;
                        if (this.itrs == null) break block18;
                        if (this.count == 0) {
                            this.itrs.queueIsEmpty();
                            break block18;
                        }
                        if (i <= take) break block18;
                        this.itrs.takeIndexWrapped();
                    }
                    catch (Throwable throwable) {
                        if (i > 0) {
                            this.count -= i;
                            this.takeIndex = take;
                            if (this.itrs != null) {
                                if (this.count == 0) {
                                    this.itrs.queueIsEmpty();
                                } else if (i > take) {
                                    this.itrs.takeIndexWrapped();
                                }
                            }
                            while (i > 0 && lock.hasWaiters(this.notFull)) {
                                this.notFull.signal();
                                --i;
                            }
                        }
                        throw throwable;
                    }
                }
                while (i > 0 && lock.hasWaiters(this.notFull)) {
                    this.notFull.signal();
                    --i;
                }
            }
            return n;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, 4368);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void forEach(Consumer<? super E> action) {
        block6: {
            Objects.requireNonNull(action);
            ReentrantLock lock = this.lock;
            lock.lock();
            try {
                int to;
                if (this.count <= 0) break block6;
                Object[] items = this.items;
                int i = this.takeIndex;
                int end = this.putIndex;
                int n = to = i < end ? end : items.length;
                while (true) {
                    if (i < to) {
                        action.accept(ArrayBlockingQueue.itemAt(items, i));
                        ++i;
                        continue;
                    }
                    if (to == end) {
                        break;
                    }
                    i = 0;
                    to = end;
                }
            }
            finally {
                lock.unlock();
            }
        }
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        return this.bulkRemove(filter);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return this.bulkRemove(e -> c.contains(e));
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return this.bulkRemove(e -> !c.contains(e));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean bulkRemove(Predicate<? super E> filter) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (this.itrs == null) {
                if (this.count > 0) {
                    int to;
                    Object[] items = this.items;
                    int i = this.takeIndex;
                    int end = this.putIndex;
                    int n = to = i < end ? end : items.length;
                    while (true) {
                        if (i < to) {
                            if (filter.test(ArrayBlockingQueue.itemAt(items, i))) {
                                boolean bl = this.bulkRemoveModified(filter, i);
                                return bl;
                            }
                            ++i;
                            continue;
                        }
                        if (to == end) break;
                        i = 0;
                        to = end;
                    }
                }
                boolean bl = false;
                return bl;
            }
        }
        finally {
            lock.unlock();
        }
        return super.removeIf(filter);
    }

    private static long[] nBits(int n) {
        return new long[(n - 1 >> 6) + 1];
    }

    private static void setBit(long[] bits, int i) {
        int n = i >> 6;
        bits[n] = bits[n] | 1L << i;
    }

    private static boolean isClear(long[] bits, int i) {
        return (bits[i >> 6] & 1L << i) == 0L;
    }

    private int distanceNonEmpty(int i, int j) {
        if ((j -= i) <= 0) {
            j += this.items.length;
        }
        return j;
    }

    private boolean bulkRemoveModified(Predicate<? super E> filter, int beg) {
        Object[] es = this.items;
        int capacity = this.items.length;
        int end = this.putIndex;
        long[] deathRow = ArrayBlockingQueue.nBits(this.distanceNonEmpty(beg, this.putIndex));
        deathRow[0] = 1L;
        int i = beg + 1;
        int to = i <= end ? end : es.length;
        int k = beg;
        while (true) {
            if (i < to) {
                if (filter.test(ArrayBlockingQueue.itemAt(es, i))) {
                    ArrayBlockingQueue.setBit(deathRow, i - k);
                }
                ++i;
                continue;
            }
            if (to == end) break;
            i = 0;
            to = end;
            k -= capacity;
        }
        int w = beg;
        int i2 = beg + 1;
        int to2 = i2 <= end ? end : es.length;
        int k2 = beg;
        while (true) {
            if (i2 < to2) {
                if (ArrayBlockingQueue.isClear(deathRow, i2 - k2)) {
                    es[w++] = es[i2];
                }
                ++i2;
                continue;
            }
            if (to2 == end) break;
            to2 = end;
            k2 -= capacity;
            for (i2 = 0; i2 < to2 && w < capacity; ++i2) {
                if (!ArrayBlockingQueue.isClear(deathRow, i2 - k2)) continue;
                es[w++] = es[i2];
            }
            if (i2 >= to2) {
                if (w != capacity) break;
                w = 0;
                break;
            }
            w = 0;
        }
        this.count -= this.distanceNonEmpty(w, end);
        this.putIndex = w;
        ArrayBlockingQueue.circularClear(es, this.putIndex, end);
        return true;
    }

    void checkInvariants() {
        if (!this.invariantsSatisfied()) {
            String detail = String.format("takeIndex=%d putIndex=%d count=%d capacity=%d items=%s", this.takeIndex, this.putIndex, this.count, this.items.length, Arrays.toString(this.items));
            System.err.println(detail);
            throw new AssertionError((Object)detail);
        }
    }

    private boolean invariantsSatisfied() {
        int capacity = this.items.length;
        return !(capacity <= 0 || this.items.getClass() != Object[].class || (this.takeIndex | this.putIndex | this.count) < 0 || this.takeIndex >= capacity || this.putIndex >= capacity || this.count > capacity || (this.putIndex - this.takeIndex - this.count) % capacity != 0 || this.count != 0 && this.items[this.takeIndex] == null || this.count != capacity && this.items[this.putIndex] != null || this.count != 0 && this.items[ArrayBlockingQueue.dec(this.putIndex, capacity)] == null);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (!this.invariantsSatisfied()) {
            throw new InvalidObjectException("invariants violated");
        }
    }

    class Itrs {
        int cycles;
        private java.util.concurrent.ArrayBlockingQueue$Itrs.Node head;
        private java.util.concurrent.ArrayBlockingQueue$Itrs.Node sweeper;
        private static final int SHORT_SWEEP_PROBES = 4;
        private static final int LONG_SWEEP_PROBES = 16;

        Itrs(Itr initial) {
            this.register(initial);
        }

        void doSomeSweeping(boolean tryHarder) {
            boolean passedGo;
            java.util.concurrent.ArrayBlockingQueue$Itrs.Node p;
            java.util.concurrent.ArrayBlockingQueue$Itrs.Node o;
            int probes = tryHarder ? 16 : 4;
            java.util.concurrent.ArrayBlockingQueue$Itrs.Node sweeper = this.sweeper;
            if (sweeper == null) {
                o = null;
                p = this.head;
                passedGo = true;
            } else {
                o = sweeper;
                p = o.next;
                passedGo = false;
            }
            while (probes > 0) {
                if (p == null) {
                    if (passedGo) break;
                    o = null;
                    p = this.head;
                    passedGo = true;
                }
                Itr it = (Itr)p.get();
                java.util.concurrent.ArrayBlockingQueue$Itrs.Node next = p.next;
                if (it == null || it.isDetached()) {
                    probes = 16;
                    p.clear();
                    p.next = null;
                    if (o == null) {
                        this.head = next;
                        if (next == null) {
                            ArrayBlockingQueue.this.itrs = null;
                            return;
                        }
                    } else {
                        o.next = next;
                    }
                } else {
                    o = p;
                }
                p = next;
                --probes;
            }
            this.sweeper = p == null ? null : o;
        }

        void register(Itr itr) {
            this.head = new Node(this, itr, (Node)this.head);
        }

        void takeIndexWrapped() {
            ++this.cycles;
            java.util.concurrent.ArrayBlockingQueue$Itrs.Node o = null;
            java.util.concurrent.ArrayBlockingQueue$Itrs.Node p = this.head;
            while (p != null) {
                Itr it = (Itr)p.get();
                java.util.concurrent.ArrayBlockingQueue$Itrs.Node next = p.next;
                if (it == null || it.takeIndexWrapped()) {
                    p.clear();
                    p.next = null;
                    if (o == null) {
                        this.head = next;
                    } else {
                        o.next = next;
                    }
                } else {
                    o = p;
                }
                p = next;
            }
            if (this.head == null) {
                ArrayBlockingQueue.this.itrs = null;
            }
        }

        void removedAt(int removedIndex) {
            java.util.concurrent.ArrayBlockingQueue$Itrs.Node o = null;
            java.util.concurrent.ArrayBlockingQueue$Itrs.Node p = this.head;
            while (p != null) {
                Itr it = (Itr)p.get();
                java.util.concurrent.ArrayBlockingQueue$Itrs.Node next = p.next;
                if (it == null || it.removedAt(removedIndex)) {
                    p.clear();
                    p.next = null;
                    if (o == null) {
                        this.head = next;
                    } else {
                        o.next = next;
                    }
                } else {
                    o = p;
                }
                p = next;
            }
            if (this.head == null) {
                ArrayBlockingQueue.this.itrs = null;
            }
        }

        void queueIsEmpty() {
            java.util.concurrent.ArrayBlockingQueue$Itrs.Node p = this.head;
            while (p != null) {
                Itr it = (Itr)p.get();
                if (it != null) {
                    p.clear();
                    it.shutdown();
                }
                p = p.next;
            }
            this.head = null;
            ArrayBlockingQueue.this.itrs = null;
        }

        void elementDequeued() {
            if (ArrayBlockingQueue.this.count == 0) {
                this.queueIsEmpty();
            } else if (ArrayBlockingQueue.this.takeIndex == 0) {
                this.takeIndexWrapped();
            }
        }

        private class Node
        extends WeakReference<Itr> {
            java.util.concurrent.ArrayBlockingQueue$Itrs.Node next;

            /*
             * WARNING - Possible parameter corruption
             */
            Node(Itr iterator, java.util.concurrent.ArrayBlockingQueue$Itrs.Node next) {
                super(iterator);
                this.next = next;
            }
        }
    }

    private class Itr
    implements Iterator<E> {
        private int cursor;
        private E nextItem;
        private int nextIndex;
        private E lastItem;
        private int lastRet = -1;
        private int prevTakeIndex;
        private int prevCycles;
        private static final int NONE = -1;
        private static final int REMOVED = -2;
        private static final int DETACHED = -3;

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        Itr() {
            ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                if (ArrayBlockingQueue.this.count == 0) {
                    this.cursor = -1;
                    this.nextIndex = -1;
                    this.prevTakeIndex = -3;
                } else {
                    int takeIndex;
                    this.prevTakeIndex = takeIndex = ArrayBlockingQueue.this.takeIndex;
                    this.nextIndex = takeIndex;
                    this.nextItem = ArrayBlockingQueue.this.itemAt(this.nextIndex);
                    this.cursor = this.incCursor(takeIndex);
                    if (ArrayBlockingQueue.this.itrs == null) {
                        ArrayBlockingQueue.this.itrs = new Itrs(this);
                    } else {
                        ArrayBlockingQueue.this.itrs.register(this);
                        ArrayBlockingQueue.this.itrs.doSomeSweeping(false);
                    }
                    this.prevCycles = ArrayBlockingQueue.this.itrs.cycles;
                }
            }
            finally {
                lock.unlock();
            }
        }

        boolean isDetached() {
            return this.prevTakeIndex < 0;
        }

        private int incCursor(int index) {
            if (++index == ArrayBlockingQueue.this.items.length) {
                index = 0;
            }
            if (index == ArrayBlockingQueue.this.putIndex) {
                index = -1;
            }
            return index;
        }

        private boolean invalidated(int index, int prevTakeIndex, long dequeues, int length) {
            if (index < 0) {
                return false;
            }
            int distance = index - prevTakeIndex;
            if (distance < 0) {
                distance += length;
            }
            return dequeues > (long)distance;
        }

        private void incorporateDequeues() {
            int cycles = ArrayBlockingQueue.this.itrs.cycles;
            int takeIndex = ArrayBlockingQueue.this.takeIndex;
            int prevCycles = this.prevCycles;
            int prevTakeIndex = this.prevTakeIndex;
            if (cycles != prevCycles || takeIndex != prevTakeIndex) {
                int len = ArrayBlockingQueue.this.items.length;
                long dequeues = (long)(cycles - prevCycles) * (long)len + (long)(takeIndex - prevTakeIndex);
                if (this.invalidated(this.lastRet, prevTakeIndex, dequeues, len)) {
                    this.lastRet = -2;
                }
                if (this.invalidated(this.nextIndex, prevTakeIndex, dequeues, len)) {
                    this.nextIndex = -2;
                }
                if (this.invalidated(this.cursor, prevTakeIndex, dequeues, len)) {
                    this.cursor = takeIndex;
                }
                if (this.cursor < 0 && this.nextIndex < 0 && this.lastRet < 0) {
                    this.detach();
                } else {
                    this.prevCycles = cycles;
                    this.prevTakeIndex = takeIndex;
                }
            }
        }

        private void detach() {
            if (this.prevTakeIndex >= 0) {
                this.prevTakeIndex = -3;
                ArrayBlockingQueue.this.itrs.doSomeSweeping(true);
            }
        }

        @Override
        public boolean hasNext() {
            if (this.nextItem != null) {
                return true;
            }
            this.noNext();
            return false;
        }

        private void noNext() {
            ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                if (!this.isDetached()) {
                    this.incorporateDequeues();
                    if (this.lastRet >= 0) {
                        this.lastItem = ArrayBlockingQueue.this.itemAt(this.lastRet);
                        this.detach();
                    }
                }
            }
            finally {
                lock.unlock();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public E next() {
            Object e = this.nextItem;
            if (e == null) {
                throw new NoSuchElementException();
            }
            ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                if (!this.isDetached()) {
                    this.incorporateDequeues();
                }
                this.lastRet = this.nextIndex;
                int cursor = this.cursor;
                if (cursor >= 0) {
                    this.nextIndex = cursor;
                    this.nextItem = ArrayBlockingQueue.this.itemAt(this.nextIndex);
                    this.cursor = this.incCursor(cursor);
                } else {
                    this.nextIndex = -1;
                    this.nextItem = null;
                    if (this.lastRet == -2) {
                        this.detach();
                    }
                }
            }
            finally {
                lock.unlock();
            }
            return e;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                int to;
                Object e = this.nextItem;
                if (e == null) {
                    return;
                }
                if (!this.isDetached()) {
                    this.incorporateDequeues();
                }
                action.accept(e);
                if (this.isDetached() || this.cursor < 0) {
                    return;
                }
                Object[] items = ArrayBlockingQueue.this.items;
                int i = this.cursor;
                int end = ArrayBlockingQueue.this.putIndex;
                int n = to = i < end ? end : items.length;
                while (true) {
                    if (i < to) {
                        action.accept(ArrayBlockingQueue.itemAt(items, i));
                        ++i;
                        continue;
                    }
                    if (to == end) {
                        break;
                    }
                    i = 0;
                    to = end;
                }
            }
            finally {
                this.lastRet = -1;
                this.nextIndex = -1;
                this.cursor = -1;
                this.lastItem = null;
                this.nextItem = null;
                this.detach();
                lock.unlock();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void remove() {
            ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                if (!this.isDetached()) {
                    this.incorporateDequeues();
                }
                int lastRet = this.lastRet;
                this.lastRet = -1;
                if (lastRet >= 0) {
                    if (!this.isDetached()) {
                        ArrayBlockingQueue.this.removeAt(lastRet);
                    } else {
                        Object lastItem = this.lastItem;
                        this.lastItem = null;
                        if (ArrayBlockingQueue.this.itemAt(lastRet) == lastItem) {
                            ArrayBlockingQueue.this.removeAt(lastRet);
                        }
                    }
                } else if (lastRet == -1) {
                    throw new IllegalStateException();
                }
                if (this.cursor < 0 && this.nextIndex < 0) {
                    this.detach();
                }
            }
            finally {
                lock.unlock();
            }
        }

        void shutdown() {
            this.cursor = -1;
            if (this.nextIndex >= 0) {
                this.nextIndex = -2;
            }
            if (this.lastRet >= 0) {
                this.lastRet = -2;
                this.lastItem = null;
            }
            this.prevTakeIndex = -3;
        }

        private int distance(int index, int prevTakeIndex, int length) {
            int distance = index - prevTakeIndex;
            if (distance < 0) {
                distance += length;
            }
            return distance;
        }

        boolean removedAt(int removedIndex) {
            int nextIndex;
            int lastRet;
            if (this.isDetached()) {
                return true;
            }
            int takeIndex = ArrayBlockingQueue.this.takeIndex;
            int prevTakeIndex = this.prevTakeIndex;
            int len = ArrayBlockingQueue.this.items.length;
            int removedDistance = len * (ArrayBlockingQueue.this.itrs.cycles - this.prevCycles + (removedIndex < takeIndex ? 1 : 0)) + (removedIndex - prevTakeIndex);
            int cursor = this.cursor;
            if (cursor >= 0) {
                int x = this.distance(cursor, prevTakeIndex, len);
                if (x == removedDistance) {
                    if (cursor == ArrayBlockingQueue.this.putIndex) {
                        cursor = -1;
                        this.cursor = -1;
                    }
                } else if (x > removedDistance) {
                    this.cursor = cursor = ArrayBlockingQueue.dec(cursor, len);
                }
            }
            if ((lastRet = this.lastRet) >= 0) {
                int x = this.distance(lastRet, prevTakeIndex, len);
                if (x == removedDistance) {
                    lastRet = -2;
                    this.lastRet = -2;
                } else if (x > removedDistance) {
                    this.lastRet = lastRet = ArrayBlockingQueue.dec(lastRet, len);
                }
            }
            if ((nextIndex = this.nextIndex) >= 0) {
                int x = this.distance(nextIndex, prevTakeIndex, len);
                if (x == removedDistance) {
                    nextIndex = -2;
                    this.nextIndex = -2;
                } else if (x > removedDistance) {
                    this.nextIndex = nextIndex = ArrayBlockingQueue.dec(nextIndex, len);
                }
            }
            if (cursor < 0 && nextIndex < 0 && lastRet < 0) {
                this.prevTakeIndex = -3;
                return true;
            }
            return false;
        }

        boolean takeIndexWrapped() {
            if (this.isDetached()) {
                return true;
            }
            if (ArrayBlockingQueue.this.itrs.cycles - this.prevCycles > 1) {
                this.shutdown();
                return true;
            }
            return false;
        }
    }
}

