/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.TreeMap;

public class TreeSet<E>
extends AbstractSet<E>
implements NavigableSet<E>,
Cloneable,
Serializable {
    private transient NavigableMap<E, Object> m;
    private static final Object PRESENT = new Object();
    private static final long serialVersionUID = -2479143000061671589L;

    TreeSet(NavigableMap<E, Object> m) {
        this.m = m;
    }

    public TreeSet() {
        this(new TreeMap());
    }

    public TreeSet(Comparator<? super E> comparator) {
        this(new TreeMap(comparator));
    }

    public TreeSet(Collection<? extends E> c) {
        this();
        this.addAll(c);
    }

    public TreeSet(SortedSet<E> s) {
        this(s.comparator());
        this.addAll(s);
    }

    @Override
    public Iterator<E> iterator() {
        return this.m.navigableKeySet().iterator();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return this.m.descendingKeySet().iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new TreeSet<E>(this.m.descendingMap());
    }

    @Override
    public int size() {
        return this.m.size();
    }

    @Override
    public boolean isEmpty() {
        return this.m.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.m.containsKey(o);
    }

    @Override
    public boolean add(E e) {
        return this.m.put(e, PRESENT) == null;
    }

    @Override
    public boolean remove(Object o) {
        return this.m.remove(o) == PRESENT;
    }

    @Override
    public void clear() {
        this.m.clear();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        NavigableMap<E, Object> navigableMap;
        if (this.m.size() == 0 && c.size() > 0 && c instanceof SortedSet && (navigableMap = this.m) instanceof TreeMap) {
            TreeMap map = (TreeMap)navigableMap;
            SortedSet set = (SortedSet)c;
            if (Objects.equals(set.comparator(), map.comparator())) {
                map.addAllForTreeSet(set, PRESENT);
                return true;
            }
        }
        return super.addAll(c);
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return new TreeSet<E>(this.m.subMap(fromElement, fromInclusive, toElement, toInclusive));
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new TreeSet<E>(this.m.headMap(toElement, inclusive));
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new TreeSet<E>(this.m.tailMap(fromElement, inclusive));
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return this.subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return this.headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return this.tailSet(fromElement, true);
    }

    @Override
    public Comparator<? super E> comparator() {
        return this.m.comparator();
    }

    @Override
    public E first() {
        return (E)this.m.firstKey();
    }

    @Override
    public E last() {
        return (E)this.m.lastKey();
    }

    @Override
    public E lower(E e) {
        return this.m.lowerKey(e);
    }

    @Override
    public E floor(E e) {
        return this.m.floorKey(e);
    }

    @Override
    public E ceiling(E e) {
        return this.m.ceilingKey(e);
    }

    @Override
    public E higher(E e) {
        return this.m.higherKey(e);
    }

    @Override
    public E pollFirst() {
        Map.Entry<E, Object> e = this.m.pollFirstEntry();
        return e == null ? null : (E)e.getKey();
    }

    @Override
    public E pollLast() {
        Map.Entry<E, Object> e = this.m.pollLastEntry();
        return e == null ? null : (E)e.getKey();
    }

    @Override
    public void addFirst(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addLast(E e) {
        throw new UnsupportedOperationException();
    }

    public Object clone() {
        TreeSet clone;
        try {
            clone = (TreeSet)super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        clone.m = new TreeMap<E, Object>(this.m);
        return clone;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeObject(this.m.comparator());
        s.writeInt(this.m.size());
        for (Object e : this.m.keySet()) {
            s.writeObject(e);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        Comparator c = (Comparator)s.readObject();
        TreeMap<E, Object> tm = new TreeMap<E, Object>(c);
        this.m = tm;
        int size = s.readInt();
        tm.readTreeSet(size, s, PRESENT);
    }

    @Override
    public Spliterator<E> spliterator() {
        return TreeMap.keySpliteratorFor(this.m);
    }
}

