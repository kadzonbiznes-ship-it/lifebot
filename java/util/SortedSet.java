/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.util.Comparator;
import java.util.ReverseOrderSortedSetView;
import java.util.SequencedSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

public interface SortedSet<E>
extends Set<E>,
SequencedSet<E> {
    public Comparator<? super E> comparator();

    public SortedSet<E> subSet(E var1, E var2);

    public SortedSet<E> headSet(E var1);

    public SortedSet<E> tailSet(E var1);

    public E first();

    public E last();

    @Override
    default public Spliterator<E> spliterator() {
        return new Spliterators.IteratorSpliterator<E>(this, 21){

            @Override
            public Comparator<? super E> getComparator() {
                return SortedSet.this.comparator();
            }
        };
    }

    @Override
    default public void addFirst(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    default public void addLast(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    default public E getFirst() {
        return this.first();
    }

    @Override
    default public E getLast() {
        return this.last();
    }

    @Override
    default public E removeFirst() {
        E e = this.first();
        this.remove(e);
        return e;
    }

    @Override
    default public E removeLast() {
        E e = this.last();
        this.remove(e);
        return e;
    }

    @Override
    default public SortedSet<E> reversed() {
        return ReverseOrderSortedSetView.of(this);
    }
}

