/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public abstract class AbstractSequentialList<E>
extends AbstractList<E> {
    protected AbstractSequentialList() {
    }

    @Override
    public E get(int index) {
        try {
            return this.listIterator(index).next();
        }
        catch (NoSuchElementException exc) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    @Override
    public E set(int index, E element) {
        try {
            ListIterator<E> e = this.listIterator(index);
            E oldVal = e.next();
            e.set(element);
            return oldVal;
        }
        catch (NoSuchElementException exc) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    @Override
    public void add(int index, E element) {
        try {
            this.listIterator(index).add(element);
        }
        catch (NoSuchElementException exc) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    @Override
    public E remove(int index) {
        try {
            ListIterator<E> e = this.listIterator(index);
            E outCast = e.next();
            e.remove();
            return outCast;
        }
        catch (NoSuchElementException exc) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        try {
            boolean modified = false;
            ListIterator<E> e1 = this.listIterator(index);
            for (E e : c) {
                e1.add(e);
                modified = true;
            }
            return modified;
        }
        catch (NoSuchElementException exc) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    @Override
    public Iterator<E> iterator() {
        return this.listIterator();
    }

    @Override
    public abstract ListIterator<E> listIterator(int var1);
}

