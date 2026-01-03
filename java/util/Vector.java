/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import jdk.internal.util.ArraysSupport;

public class Vector<E>
extends AbstractList<E>
implements List<E>,
RandomAccess,
Cloneable,
Serializable {
    protected Object[] elementData;
    protected int elementCount;
    protected int capacityIncrement;
    private static final long serialVersionUID = -2767605614048989439L;

    public Vector(int initialCapacity, int capacityIncrement) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
        this.elementData = new Object[initialCapacity];
        this.capacityIncrement = capacityIncrement;
    }

    public Vector(int initialCapacity) {
        this(initialCapacity, 0);
    }

    public Vector() {
        this(10);
    }

    public Vector(Collection<? extends E> c) {
        Object[] a = c.toArray();
        this.elementCount = a.length;
        this.elementData = c.getClass() == ArrayList.class ? a : Arrays.copyOf(a, this.elementCount, Object[].class);
    }

    public synchronized void copyInto(Object[] anArray) {
        System.arraycopy(this.elementData, 0, anArray, 0, this.elementCount);
    }

    public synchronized void trimToSize() {
        ++this.modCount;
        int oldCapacity = this.elementData.length;
        if (this.elementCount < oldCapacity) {
            this.elementData = Arrays.copyOf(this.elementData, this.elementCount);
        }
    }

    public synchronized void ensureCapacity(int minCapacity) {
        if (minCapacity > 0) {
            ++this.modCount;
            if (minCapacity > this.elementData.length) {
                this.grow(minCapacity);
            }
        }
    }

    private Object[] grow(int minCapacity) {
        int oldCapacity = this.elementData.length;
        int newCapacity = ArraysSupport.newLength(oldCapacity, minCapacity - oldCapacity, this.capacityIncrement > 0 ? this.capacityIncrement : oldCapacity);
        this.elementData = Arrays.copyOf(this.elementData, newCapacity);
        return this.elementData;
    }

    private Object[] grow() {
        return this.grow(this.elementCount + 1);
    }

    public synchronized void setSize(int newSize) {
        ++this.modCount;
        if (newSize > this.elementData.length) {
            this.grow(newSize);
        }
        Object[] es = this.elementData;
        int to = this.elementCount;
        for (int i = newSize; i < to; ++i) {
            es[i] = null;
        }
        this.elementCount = newSize;
    }

    public synchronized int capacity() {
        return this.elementData.length;
    }

    @Override
    public synchronized int size() {
        return this.elementCount;
    }

    @Override
    public synchronized boolean isEmpty() {
        return this.elementCount == 0;
    }

    public Enumeration<E> elements() {
        return new Enumeration<E>(){
            int count = 0;

            @Override
            public boolean hasMoreElements() {
                return this.count < Vector.this.elementCount;
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public E nextElement() {
                Vector vector = Vector.this;
                synchronized (vector) {
                    if (this.count < Vector.this.elementCount) {
                        return Vector.this.elementData(this.count++);
                    }
                }
                throw new NoSuchElementException("Vector Enumeration");
            }
        };
    }

    @Override
    public boolean contains(Object o) {
        return this.indexOf(o, 0) >= 0;
    }

    @Override
    public int indexOf(Object o) {
        return this.indexOf(o, 0);
    }

    public synchronized int indexOf(Object o, int index) {
        if (o == null) {
            for (int i = index; i < this.elementCount; ++i) {
                if (this.elementData[i] != null) continue;
                return i;
            }
        } else {
            for (int i = index; i < this.elementCount; ++i) {
                if (!o.equals(this.elementData[i])) continue;
                return i;
            }
        }
        return -1;
    }

    @Override
    public synchronized int lastIndexOf(Object o) {
        return this.lastIndexOf(o, this.elementCount - 1);
    }

    public synchronized int lastIndexOf(Object o, int index) {
        if (index >= this.elementCount) {
            throw new IndexOutOfBoundsException(index + " >= " + this.elementCount);
        }
        if (o == null) {
            for (int i = index; i >= 0; --i) {
                if (this.elementData[i] != null) continue;
                return i;
            }
        } else {
            for (int i = index; i >= 0; --i) {
                if (!o.equals(this.elementData[i])) continue;
                return i;
            }
        }
        return -1;
    }

    public synchronized E elementAt(int index) {
        if (index >= this.elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + this.elementCount);
        }
        return this.elementData(index);
    }

    public synchronized E firstElement() {
        if (this.elementCount == 0) {
            throw new NoSuchElementException();
        }
        return this.elementData(0);
    }

    public synchronized E lastElement() {
        if (this.elementCount == 0) {
            throw new NoSuchElementException();
        }
        return this.elementData(this.elementCount - 1);
    }

    public synchronized void setElementAt(E obj, int index) {
        if (index >= this.elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + this.elementCount);
        }
        this.elementData[index] = obj;
    }

    public synchronized void removeElementAt(int index) {
        if (index >= this.elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + this.elementCount);
        }
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        int j = this.elementCount - index - 1;
        if (j > 0) {
            System.arraycopy(this.elementData, index + 1, this.elementData, index, j);
        }
        ++this.modCount;
        --this.elementCount;
        this.elementData[this.elementCount] = null;
    }

    public synchronized void insertElementAt(E obj, int index) {
        if (index > this.elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " > " + this.elementCount);
        }
        ++this.modCount;
        int s = this.elementCount;
        Object[] elementData = this.elementData;
        if (s == elementData.length) {
            elementData = this.grow();
        }
        System.arraycopy(elementData, index, elementData, index + 1, s - index);
        elementData[index] = obj;
        this.elementCount = s + 1;
    }

    public synchronized void addElement(E obj) {
        ++this.modCount;
        this.add(obj, this.elementData, this.elementCount);
    }

    public synchronized boolean removeElement(Object obj) {
        ++this.modCount;
        int i = this.indexOf(obj);
        if (i >= 0) {
            this.removeElementAt(i);
            return true;
        }
        return false;
    }

    public synchronized void removeAllElements() {
        Object[] es = this.elementData;
        int to = this.elementCount;
        this.elementCount = 0;
        for (int i = 0; i < to; ++i) {
            es[i] = null;
        }
        ++this.modCount;
    }

    public synchronized Object clone() {
        try {
            Vector v = (Vector)super.clone();
            v.elementData = Arrays.copyOf(this.elementData, this.elementCount);
            v.modCount = 0;
            return v;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    @Override
    public synchronized Object[] toArray() {
        return Arrays.copyOf(this.elementData, this.elementCount);
    }

    @Override
    public synchronized <T> T[] toArray(T[] a) {
        if (a.length < this.elementCount) {
            return Arrays.copyOf(this.elementData, this.elementCount, a.getClass());
        }
        System.arraycopy(this.elementData, 0, a, 0, this.elementCount);
        if (a.length > this.elementCount) {
            a[this.elementCount] = null;
        }
        return a;
    }

    E elementData(int index) {
        return (E)this.elementData[index];
    }

    static <E> E elementAt(Object[] es, int index) {
        return (E)es[index];
    }

    @Override
    public synchronized E get(int index) {
        if (index >= this.elementCount) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return this.elementData(index);
    }

    @Override
    public synchronized E set(int index, E element) {
        if (index >= this.elementCount) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        E oldValue = this.elementData(index);
        this.elementData[index] = element;
        return oldValue;
    }

    private void add(E e, Object[] elementData, int s) {
        if (s == elementData.length) {
            elementData = this.grow();
        }
        elementData[s] = e;
        this.elementCount = s + 1;
    }

    @Override
    public synchronized boolean add(E e) {
        ++this.modCount;
        this.add(e, this.elementData, this.elementCount);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        return this.removeElement(o);
    }

    @Override
    public void add(int index, E element) {
        this.insertElementAt(element, index);
    }

    @Override
    public synchronized E remove(int index) {
        ++this.modCount;
        if (index >= this.elementCount) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        E oldValue = this.elementData(index);
        int numMoved = this.elementCount - index - 1;
        if (numMoved > 0) {
            System.arraycopy(this.elementData, index + 1, this.elementData, index, numMoved);
        }
        this.elementData[--this.elementCount] = null;
        return oldValue;
    }

    @Override
    public void clear() {
        this.removeAllElements();
    }

    @Override
    public synchronized boolean containsAll(Collection<?> c) {
        return super.containsAll(c);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        Object[] a = c.toArray();
        ++this.modCount;
        int numNew = a.length;
        if (numNew == 0) {
            return false;
        }
        Vector vector = this;
        synchronized (vector) {
            Object[] elementData = this.elementData;
            int s = this.elementCount;
            if (numNew > elementData.length - s) {
                elementData = this.grow(s + numNew);
            }
            System.arraycopy(a, 0, elementData, s, numNew);
            this.elementCount = s + numNew;
            return true;
        }
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

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        return this.bulkRemove(filter);
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

    private synchronized boolean bulkRemove(Predicate<? super E> filter) {
        int i;
        int expectedModCount = this.modCount;
        Object[] es = this.elementData;
        int end = this.elementCount;
        for (i = 0; i < end && !filter.test(Vector.elementAt(es, i)); ++i) {
        }
        if (i < end) {
            int beg = i;
            long[] deathRow = Vector.nBits(end - beg);
            deathRow[0] = 1L;
            for (i = beg + 1; i < end; ++i) {
                if (!filter.test(Vector.elementAt(es, i))) continue;
                Vector.setBit(deathRow, i - beg);
            }
            if (this.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            ++this.modCount;
            int w = beg;
            for (i = beg; i < end; ++i) {
                if (!Vector.isClear(deathRow, i - beg)) continue;
                es[w++] = es[i];
            }
            for (i = this.elementCount = w; i < end; ++i) {
                es[i] = null;
            }
            return true;
        }
        if (this.modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        return false;
    }

    @Override
    public synchronized boolean addAll(int index, Collection<? extends E> c) {
        int numMoved;
        if (index < 0 || index > this.elementCount) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        Object[] a = c.toArray();
        ++this.modCount;
        int numNew = a.length;
        if (numNew == 0) {
            return false;
        }
        Object[] elementData = this.elementData;
        int s = this.elementCount;
        if (numNew > elementData.length - s) {
            elementData = this.grow(s + numNew);
        }
        if ((numMoved = s - index) > 0) {
            System.arraycopy(elementData, index, elementData, index + numNew, numMoved);
        }
        System.arraycopy(a, 0, elementData, index, numNew);
        this.elementCount = s + numNew;
        return true;
    }

    @Override
    public synchronized boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public synchronized int hashCode() {
        return super.hashCode();
    }

    @Override
    public synchronized String toString() {
        return super.toString();
    }

    @Override
    public synchronized List<E> subList(int fromIndex, int toIndex) {
        return Collections.synchronizedList(super.subList(fromIndex, toIndex), this);
    }

    @Override
    protected synchronized void removeRange(int fromIndex, int toIndex) {
        ++this.modCount;
        this.shiftTailOverGap(this.elementData, fromIndex, toIndex);
    }

    private void shiftTailOverGap(Object[] es, int lo, int hi) {
        System.arraycopy(es, hi, es, lo, this.elementCount - hi);
        int to = this.elementCount;
        for (int i = this.elementCount -= hi - lo; i < to; ++i) {
            es[i] = null;
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField gfields = in.readFields();
        int count = gfields.get("elementCount", 0);
        Object[] data = (Object[])gfields.get("elementData", null);
        if (count < 0 || data == null || count > data.length) {
            throw new StreamCorruptedException("Inconsistent vector internals");
        }
        this.elementCount = count;
        this.elementData = (Object[])data.clone();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        Object[] data;
        ObjectOutputStream.PutField fields = s.putFields();
        Vector vector = this;
        synchronized (vector) {
            fields.put("capacityIncrement", this.capacityIncrement);
            fields.put("elementCount", this.elementCount);
            data = (Object[])this.elementData.clone();
        }
        fields.put("elementData", data);
        s.writeFields();
    }

    @Override
    public synchronized ListIterator<E> listIterator(int index) {
        if (index < 0 || index > this.elementCount) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        return new ListItr(index);
    }

    @Override
    public synchronized ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    @Override
    public synchronized Iterator<E> iterator() {
        return new Itr();
    }

    @Override
    public synchronized void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        int expectedModCount = this.modCount;
        Object[] es = this.elementData;
        int size = this.elementCount;
        for (int i = 0; this.modCount == expectedModCount && i < size; ++i) {
            action.accept(Vector.elementAt(es, i));
        }
        if (this.modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    @Override
    public synchronized void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        int expectedModCount = this.modCount;
        Object[] es = this.elementData;
        int size = this.elementCount;
        for (int i = 0; this.modCount == expectedModCount && i < size; ++i) {
            es[i] = operator.apply(Vector.elementAt(es, i));
        }
        if (this.modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        ++this.modCount;
    }

    @Override
    public synchronized void sort(Comparator<? super E> c) {
        int expectedModCount = this.modCount;
        Arrays.sort(this.elementData, 0, this.elementCount, c);
        if (this.modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        ++this.modCount;
    }

    @Override
    public Spliterator<E> spliterator() {
        return new VectorSpliterator(null, 0, -1, 0);
    }

    void checkInvariants() {
    }

    final class ListItr
    extends Itr
    implements ListIterator<E> {
        ListItr(int index) {
            this.cursor = index;
        }

        @Override
        public boolean hasPrevious() {
            return this.cursor != 0;
        }

        @Override
        public int nextIndex() {
            return this.cursor;
        }

        @Override
        public int previousIndex() {
            return this.cursor - 1;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public E previous() {
            Vector vector = Vector.this;
            synchronized (vector) {
                this.checkForComodification();
                int i = this.cursor - 1;
                if (i < 0) {
                    throw new NoSuchElementException();
                }
                this.cursor = i;
                this.lastRet = i;
                return Vector.this.elementData(this.lastRet);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void set(E e) {
            if (this.lastRet == -1) {
                throw new IllegalStateException();
            }
            Vector vector = Vector.this;
            synchronized (vector) {
                this.checkForComodification();
                Vector.this.set(this.lastRet, e);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void add(E e) {
            int i = this.cursor;
            Vector vector = Vector.this;
            synchronized (vector) {
                this.checkForComodification();
                Vector.this.add(i, e);
                this.expectedModCount = Vector.this.modCount;
            }
            this.cursor = i + 1;
            this.lastRet = -1;
        }
    }

    private class Itr
    implements Iterator<E> {
        int cursor;
        int lastRet = -1;
        int expectedModCount;

        private Itr() {
            this.expectedModCount = Vector.this.modCount;
        }

        @Override
        public boolean hasNext() {
            return this.cursor != Vector.this.elementCount;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public E next() {
            Vector vector = Vector.this;
            synchronized (vector) {
                this.checkForComodification();
                int i = this.cursor;
                if (i >= Vector.this.elementCount) {
                    throw new NoSuchElementException();
                }
                this.cursor = i + 1;
                this.lastRet = i;
                return Vector.this.elementData(this.lastRet);
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void remove() {
            if (this.lastRet == -1) {
                throw new IllegalStateException();
            }
            Vector vector = Vector.this;
            synchronized (vector) {
                this.checkForComodification();
                Vector.this.remove(this.lastRet);
                this.expectedModCount = Vector.this.modCount;
            }
            this.cursor = this.lastRet;
            this.lastRet = -1;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            Vector vector = Vector.this;
            synchronized (vector) {
                int size = Vector.this.elementCount;
                int i = this.cursor;
                if (i >= size) {
                    return;
                }
                Object[] es = Vector.this.elementData;
                if (i >= es.length) {
                    throw new ConcurrentModificationException();
                }
                while (i < size && Vector.this.modCount == this.expectedModCount) {
                    action.accept(Vector.elementAt(es, i++));
                }
                this.cursor = i;
                this.lastRet = i - 1;
                this.checkForComodification();
            }
        }

        final void checkForComodification() {
            if (Vector.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    final class VectorSpliterator
    implements Spliterator<E> {
        private Object[] array;
        private int index;
        private int fence;
        private int expectedModCount;

        VectorSpliterator(Object[] array, int origin, int fence, int expectedModCount) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private int getFence() {
            int hi = this.fence;
            if (hi < 0) {
                Vector vector = Vector.this;
                synchronized (vector) {
                    this.array = Vector.this.elementData;
                    this.expectedModCount = Vector.this.modCount;
                    hi = this.fence = Vector.this.elementCount;
                }
            }
            return hi;
        }

        @Override
        public Spliterator<E> trySplit() {
            VectorSpliterator vectorSpliterator;
            int lo = this.index;
            int hi = this.getFence();
            int mid = lo + hi >>> 1;
            if (lo >= mid) {
                vectorSpliterator = null;
            } else {
                this.index = mid;
                VectorSpliterator vectorSpliterator2 = new VectorSpliterator(this.array, lo, this.index, this.expectedModCount);
                vectorSpliterator = vectorSpliterator2;
            }
            return vectorSpliterator;
        }

        @Override
        public boolean tryAdvance(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            int i = this.index;
            if (this.getFence() > i) {
                this.index = i + 1;
                action.accept(this.array[i]);
                if (Vector.this.modCount != this.expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                return true;
            }
            return false;
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            int hi = this.getFence();
            Object[] a = this.array;
            this.index = hi;
            for (int i = this.index; i < hi; ++i) {
                action.accept(a[i]);
            }
            if (Vector.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public long estimateSize() {
            return this.getFence() - this.index;
        }

        @Override
        public int characteristics() {
            return 16464;
        }
    }
}

