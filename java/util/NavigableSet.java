/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;

public interface NavigableSet<E>
extends SortedSet<E> {
    public E lower(E var1);

    public E floor(E var1);

    public E ceiling(E var1);

    public E higher(E var1);

    public E pollFirst();

    public E pollLast();

    @Override
    public Iterator<E> iterator();

    public NavigableSet<E> descendingSet();

    public Iterator<E> descendingIterator();

    public NavigableSet<E> subSet(E var1, boolean var2, E var3, boolean var4);

    public NavigableSet<E> headSet(E var1, boolean var2);

    public NavigableSet<E> tailSet(E var1, boolean var2);

    @Override
    public SortedSet<E> subSet(E var1, E var2);

    @Override
    public SortedSet<E> headSet(E var1);

    @Override
    public SortedSet<E> tailSet(E var1);

    @Override
    default public E removeFirst() {
        if (this.isEmpty()) {
            throw new NoSuchElementException();
        }
        return this.pollFirst();
    }

    @Override
    default public E removeLast() {
        if (this.isEmpty()) {
            throw new NoSuchElementException();
        }
        return this.pollLast();
    }

    @Override
    default public NavigableSet<E> reversed() {
        return this.descendingSet();
    }
}

