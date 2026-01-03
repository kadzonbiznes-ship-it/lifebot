/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import jdk.internal.access.SharedSecrets;
import jdk.internal.util.ArraysSupport;

public class PriorityQueue<E>
extends AbstractQueue<E>
implements Serializable {
    private static final long serialVersionUID = -7720805057305804111L;
    private static final int DEFAULT_INITIAL_CAPACITY = 11;
    transient Object[] queue;
    int size;
    private final Comparator<? super E> comparator;
    transient int modCount;

    public PriorityQueue() {
        this(11, null);
    }

    public PriorityQueue(int initialCapacity) {
        this(initialCapacity, null);
    }

    public PriorityQueue(Comparator<? super E> comparator) {
        this(11, comparator);
    }

    public PriorityQueue(int initialCapacity, Comparator<? super E> comparator) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException();
        }
        this.queue = new Object[initialCapacity];
        this.comparator = comparator;
    }

    public PriorityQueue(Collection<? extends E> c) {
        if (c instanceof SortedSet) {
            SortedSet ss = (SortedSet)c;
            this.comparator = ss.comparator();
            this.initElementsFromCollection(ss);
        } else if (c instanceof PriorityQueue) {
            PriorityQueue pq = (PriorityQueue)c;
            this.comparator = pq.comparator();
            this.initFromPriorityQueue(pq);
        } else {
            this.comparator = null;
            this.initFromCollection(c);
        }
    }

    public PriorityQueue(PriorityQueue<? extends E> c) {
        this.comparator = c.comparator();
        this.initFromPriorityQueue(c);
    }

    public PriorityQueue(SortedSet<? extends E> c) {
        this.comparator = c.comparator();
        this.initElementsFromCollection(c);
    }

    private static Object[] ensureNonEmpty(Object[] es) {
        return es.length > 0 ? es : new Object[1];
    }

    private void initFromPriorityQueue(PriorityQueue<? extends E> c) {
        if (c.getClass() == PriorityQueue.class) {
            this.queue = PriorityQueue.ensureNonEmpty(c.toArray());
            this.size = c.size();
        } else {
            this.initFromCollection(c);
        }
    }

    private void initElementsFromCollection(Collection<? extends E> c) {
        Object[] es = c.toArray();
        int len = es.length;
        if (c.getClass() != ArrayList.class) {
            es = Arrays.copyOf(es, len, Object[].class);
        }
        if (len == 1 || this.comparator != null) {
            for (Object e : es) {
                if (e != null) continue;
                throw new NullPointerException();
            }
        }
        this.queue = PriorityQueue.ensureNonEmpty(es);
        this.size = len;
    }

    private void initFromCollection(Collection<? extends E> c) {
        this.initElementsFromCollection(c);
        this.heapify();
    }

    private void grow(int minCapacity) {
        int oldCapacity;
        int newCapacity = ArraysSupport.newLength(oldCapacity, minCapacity - oldCapacity, (oldCapacity = this.queue.length) < 64 ? oldCapacity + 2 : oldCapacity >> 1);
        this.queue = Arrays.copyOf(this.queue, newCapacity);
    }

    @Override
    public boolean add(E e) {
        return this.offer(e);
    }

    @Override
    public boolean offer(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        ++this.modCount;
        int i = this.size;
        if (i >= this.queue.length) {
            this.grow(i + 1);
        }
        this.siftUp(i, e);
        this.size = i + 1;
        return true;
    }

    @Override
    public E peek() {
        return (E)this.queue[0];
    }

    private int indexOf(Object o) {
        if (o != null) {
            Object[] es = this.queue;
            int n = this.size;
            for (int i = 0; i < n; ++i) {
                if (!o.equals(es[i])) continue;
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean remove(Object o) {
        int i = this.indexOf(o);
        if (i == -1) {
            return false;
        }
        this.removeAt(i);
        return true;
    }

    void removeEq(Object o) {
        Object[] es = this.queue;
        int n = this.size;
        for (int i = 0; i < n; ++i) {
            if (o != es[i]) continue;
            this.removeAt(i);
            break;
        }
    }

    @Override
    public boolean contains(Object o) {
        return this.indexOf(o) >= 0;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(this.queue, this.size);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        int size = this.size;
        if (a.length < size) {
            return Arrays.copyOf(this.queue, size, a.getClass());
        }
        System.arraycopy(this.queue, 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void clear() {
        ++this.modCount;
        Object[] es = this.queue;
        int n = this.size;
        for (int i = 0; i < n; ++i) {
            es[i] = null;
        }
        this.size = 0;
    }

    @Override
    public E poll() {
        Object[] es = this.queue;
        Object result = this.queue[0];
        if (result != null) {
            ++this.modCount;
            int n = --this.size;
            Object x = es[this.size];
            es[n] = null;
            if (n > 0) {
                Comparator<? super E> cmp = this.comparator;
                if (cmp == null) {
                    PriorityQueue.siftDownComparable(0, x, es, n);
                } else {
                    PriorityQueue.siftDownUsingComparator(0, x, es, n, cmp);
                }
            }
        }
        return (E)result;
    }

    E removeAt(int i) {
        int s;
        Object[] es = this.queue;
        ++this.modCount;
        if ((s = --this.size) == i) {
            es[i] = null;
        } else {
            Object moved = es[s];
            es[s] = null;
            this.siftDown(i, moved);
            if (es[i] == moved) {
                this.siftUp(i, moved);
                if (es[i] != moved) {
                    return (E)moved;
                }
            }
        }
        return null;
    }

    private void siftUp(int k, E x) {
        if (this.comparator != null) {
            PriorityQueue.siftUpUsingComparator(k, x, this.queue, this.comparator);
        } else {
            PriorityQueue.siftUpComparable(k, x, this.queue);
        }
    }

    private static <T> void siftUpComparable(int k, T x, Object[] es) {
        int parent;
        Object e;
        Comparable key = (Comparable)x;
        while (k > 0 && key.compareTo(e = es[parent = k - 1 >>> 1]) < 0) {
            es[k] = e;
            k = parent;
        }
        es[k] = key;
    }

    private static <T> void siftUpUsingComparator(int k, T x, Object[] es, Comparator<? super T> cmp) {
        int parent;
        Object e;
        while (k > 0 && cmp.compare(x, e = es[parent = k - 1 >>> 1]) < 0) {
            es[k] = e;
            k = parent;
        }
        es[k] = x;
    }

    private void siftDown(int k, E x) {
        if (this.comparator != null) {
            PriorityQueue.siftDownUsingComparator(k, x, this.queue, this.size, this.comparator);
        } else {
            PriorityQueue.siftDownComparable(k, x, this.queue, this.size);
        }
    }

    private static <T> void siftDownComparable(int k, T x, Object[] es, int n) {
        Comparable key = (Comparable)x;
        int half = n >>> 1;
        while (k < half) {
            int child = (k << 1) + 1;
            Object c = es[child];
            int right = child + 1;
            if (right < n && ((Comparable)c).compareTo(es[right]) > 0) {
                child = right;
                c = es[child];
            }
            if (key.compareTo(c) <= 0) break;
            es[k] = c;
            k = child;
        }
        es[k] = key;
    }

    private static <T> void siftDownUsingComparator(int k, T x, Object[] es, int n, Comparator<? super T> cmp) {
        int half = n >>> 1;
        while (k < half) {
            int child = (k << 1) + 1;
            Object c = es[child];
            int right = child + 1;
            if (right < n && cmp.compare(c, es[right]) > 0) {
                child = right;
                c = es[child];
            }
            if (cmp.compare(x, c) <= 0) break;
            es[k] = c;
            k = child;
        }
        es[k] = x;
    }

    private void heapify() {
        Object[] es = this.queue;
        int n = this.size;
        Comparator<? super E> cmp = this.comparator;
        if (cmp == null) {
            for (i = (n >>> 1) - 1; i >= 0; --i) {
                PriorityQueue.siftDownComparable(i, es[i], es, n);
            }
        } else {
            while (i >= 0) {
                PriorityQueue.siftDownUsingComparator(i, es[i], es, n, cmp);
                --i;
            }
        }
    }

    public Comparator<? super E> comparator() {
        return this.comparator;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(Math.max(2, this.size + 1));
        Object[] es = this.queue;
        int n = this.size;
        for (int i = 0; i < n; ++i) {
            s.writeObject(es[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        s.readInt();
        SharedSecrets.getJavaObjectInputStreamAccess().checkArray(s, Object[].class, this.size);
        this.queue = new Object[Math.max(this.size, 1)];
        Object[] es = this.queue;
        int n = this.size;
        for (int i = 0; i < n; ++i) {
            es[i] = s.readObject();
        }
        this.heapify();
    }

    @Override
    public final Spliterator<E> spliterator() {
        return new PriorityQueueSpliterator(0, -1, 0);
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

    private boolean bulkRemove(Predicate<? super E> filter) {
        int i;
        int expectedModCount = ++this.modCount;
        Object[] es = this.queue;
        int end = this.size;
        for (i = 0; i < end && !filter.test(es[i]); ++i) {
        }
        if (i >= end) {
            if (this.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            return false;
        }
        int beg = i;
        long[] deathRow = PriorityQueue.nBits(end - beg);
        deathRow[0] = 1L;
        for (i = beg + 1; i < end; ++i) {
            if (!filter.test(es[i])) continue;
            PriorityQueue.setBit(deathRow, i - beg);
        }
        if (this.modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        int w = beg;
        for (i = beg; i < end; ++i) {
            if (!PriorityQueue.isClear(deathRow, i - beg)) continue;
            es[w++] = es[i];
        }
        for (i = this.size = w; i < end; ++i) {
            es[i] = null;
        }
        this.heapify();
        return true;
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        int expectedModCount = this.modCount;
        Object[] es = this.queue;
        int n = this.size;
        for (int i = 0; i < n; ++i) {
            action.accept(es[i]);
        }
        if (expectedModCount != this.modCount) {
            throw new ConcurrentModificationException();
        }
    }

    private final class Itr
    implements Iterator<E> {
        private int cursor;
        private int lastRet = -1;
        private ArrayDeque<E> forgetMeNot;
        private E lastRetElt;
        private int expectedModCount;

        Itr() {
            this.expectedModCount = PriorityQueue.this.modCount;
        }

        @Override
        public boolean hasNext() {
            return this.cursor < PriorityQueue.this.size || this.forgetMeNot != null && !this.forgetMeNot.isEmpty();
        }

        @Override
        public E next() {
            if (this.expectedModCount != PriorityQueue.this.modCount) {
                throw new ConcurrentModificationException();
            }
            if (this.cursor < PriorityQueue.this.size) {
                this.lastRet = this.cursor++;
                return PriorityQueue.this.queue[this.lastRet];
            }
            if (this.forgetMeNot != null) {
                this.lastRet = -1;
                this.lastRetElt = this.forgetMeNot.poll();
                if (this.lastRetElt != null) {
                    return this.lastRetElt;
                }
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            if (this.expectedModCount != PriorityQueue.this.modCount) {
                throw new ConcurrentModificationException();
            }
            if (this.lastRet != -1) {
                Object moved = PriorityQueue.this.removeAt(this.lastRet);
                this.lastRet = -1;
                if (moved == null) {
                    --this.cursor;
                } else {
                    if (this.forgetMeNot == null) {
                        this.forgetMeNot = new ArrayDeque();
                    }
                    this.forgetMeNot.add(moved);
                }
            } else if (this.lastRetElt != null) {
                PriorityQueue.this.removeEq(this.lastRetElt);
                this.lastRetElt = null;
            } else {
                throw new IllegalStateException();
            }
            this.expectedModCount = PriorityQueue.this.modCount;
        }
    }

    final class PriorityQueueSpliterator
    implements Spliterator<E> {
        private int index;
        private int fence;
        private int expectedModCount;

        PriorityQueueSpliterator(int origin, int fence, int expectedModCount) {
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence() {
            int hi = this.fence;
            if (hi < 0) {
                this.expectedModCount = PriorityQueue.this.modCount;
                hi = this.fence = PriorityQueue.this.size;
            }
            return hi;
        }

        public PriorityQueueSpliterator trySplit() {
            PriorityQueueSpliterator priorityQueueSpliterator;
            int lo = this.index;
            int hi = this.getFence();
            int mid = lo + hi >>> 1;
            if (lo >= mid) {
                priorityQueueSpliterator = null;
            } else {
                this.index = mid;
                PriorityQueueSpliterator priorityQueueSpliterator2 = new PriorityQueueSpliterator(lo, this.index, this.expectedModCount);
                priorityQueueSpliterator = priorityQueueSpliterator2;
            }
            return priorityQueueSpliterator;
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Object e;
            int hi;
            if (action == null) {
                throw new NullPointerException();
            }
            if (this.fence < 0) {
                this.fence = PriorityQueue.this.size;
                this.expectedModCount = PriorityQueue.this.modCount;
            }
            Object[] es = PriorityQueue.this.queue;
            this.index = hi = this.fence;
            for (int i = this.index; i < hi && (e = es[i]) != null; ++i) {
                action.accept(e);
            }
            if (PriorityQueue.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super E> action) {
            int i;
            if (action == null) {
                throw new NullPointerException();
            }
            if (this.fence < 0) {
                this.fence = PriorityQueue.this.size;
                this.expectedModCount = PriorityQueue.this.modCount;
            }
            if ((i = this.index) < this.fence) {
                this.index = i + 1;
                Object e = PriorityQueue.this.queue[i];
                if (e == null || PriorityQueue.this.modCount != this.expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                action.accept(e);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return this.getFence() - this.index;
        }

        @Override
        public int characteristics() {
            return 16704;
        }
    }
}

