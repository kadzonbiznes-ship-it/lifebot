/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.util;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

public class IdentityArrayList<E>
extends AbstractList<E>
implements List<E>,
RandomAccess {
    private transient Object[] elementData;
    private int size;

    public IdentityArrayList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
        this.elementData = new Object[initialCapacity];
    }

    public IdentityArrayList() {
        this(10);
    }

    public IdentityArrayList(Collection<? extends E> c) {
        this.elementData = c.toArray();
        this.size = this.elementData.length;
        if (this.elementData.getClass() != Object[].class) {
            this.elementData = Arrays.copyOf(this.elementData, this.size, Object[].class);
        }
    }

    public void trimToSize() {
        ++this.modCount;
        int oldCapacity = this.elementData.length;
        if (this.size < oldCapacity) {
            this.elementData = Arrays.copyOf(this.elementData, this.size);
        }
    }

    public void ensureCapacity(int minCapacity) {
        ++this.modCount;
        int oldCapacity = this.elementData.length;
        if (minCapacity > oldCapacity) {
            Object[] oldData = this.elementData;
            int newCapacity = oldCapacity * 3 / 2 + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            this.elementData = Arrays.copyOf(this.elementData, newCapacity);
        }
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return this.indexOf(o) >= 0;
    }

    @Override
    public int indexOf(Object o) {
        for (int i = 0; i < this.size; ++i) {
            if (o != this.elementData[i]) continue;
            return i;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        for (int i = this.size - 1; i >= 0; --i) {
            if (o != this.elementData[i]) continue;
            return i;
        }
        return -1;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(this.elementData, this.size);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < this.size) {
            return Arrays.copyOf(this.elementData, this.size, a.getClass());
        }
        System.arraycopy(this.elementData, 0, a, 0, this.size);
        if (a.length > this.size) {
            a[this.size] = null;
        }
        return a;
    }

    @Override
    public E get(int index) {
        this.rangeCheck(index);
        Object rv = this.elementData[index];
        return (E)rv;
    }

    @Override
    public E set(int index, E element) {
        this.rangeCheck(index);
        Object oldValue = this.elementData[index];
        this.elementData[index] = element;
        return (E)oldValue;
    }

    @Override
    public boolean add(E e) {
        this.ensureCapacity(this.size + 1);
        this.elementData[this.size++] = e;
        return true;
    }

    @Override
    public void add(int index, E element) {
        this.rangeCheckForAdd(index);
        this.ensureCapacity(this.size + 1);
        System.arraycopy(this.elementData, index, this.elementData, index + 1, this.size - index);
        this.elementData[index] = element;
        ++this.size;
    }

    @Override
    public E remove(int index) {
        this.rangeCheck(index);
        ++this.modCount;
        Object oldValue = this.elementData[index];
        int numMoved = this.size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(this.elementData, index + 1, this.elementData, index, numMoved);
        }
        this.elementData[--this.size] = null;
        return (E)oldValue;
    }

    @Override
    public boolean remove(Object o) {
        for (int index = 0; index < this.size; ++index) {
            if (o != this.elementData[index]) continue;
            this.fastRemove(index);
            return true;
        }
        return false;
    }

    private void fastRemove(int index) {
        ++this.modCount;
        int numMoved = this.size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(this.elementData, index + 1, this.elementData, index, numMoved);
        }
        this.elementData[--this.size] = null;
    }

    @Override
    public void clear() {
        ++this.modCount;
        for (int i = 0; i < this.size; ++i) {
            this.elementData[i] = null;
        }
        this.size = 0;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        Object[] a = c.toArray();
        int numNew = a.length;
        this.ensureCapacity(this.size + numNew);
        System.arraycopy(a, 0, this.elementData, this.size, numNew);
        this.size += numNew;
        return numNew != 0;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        this.rangeCheckForAdd(index);
        Object[] a = c.toArray();
        int numNew = a.length;
        this.ensureCapacity(this.size + numNew);
        int numMoved = this.size - index;
        if (numMoved > 0) {
            System.arraycopy(this.elementData, index, this.elementData, index + numNew, numMoved);
        }
        System.arraycopy(a, 0, this.elementData, index, numNew);
        this.size += numNew;
        return numNew != 0;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        ++this.modCount;
        int numMoved = this.size - toIndex;
        System.arraycopy(this.elementData, toIndex, this.elementData, fromIndex, numMoved);
        int newSize = this.size - (toIndex - fromIndex);
        while (this.size != newSize) {
            this.elementData[--this.size] = null;
        }
    }

    private void rangeCheck(int index) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException(this.outOfBoundsMsg(index));
        }
    }

    private void rangeCheckForAdd(int index) {
        if (index > this.size || index < 0) {
            throw new IndexOutOfBoundsException(this.outOfBoundsMsg(index));
        }
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + this.size;
    }
}

