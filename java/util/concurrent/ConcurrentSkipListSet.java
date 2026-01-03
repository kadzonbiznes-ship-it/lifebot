/*
 * Decompiled with CFR 0.152.
 */
package java.util.concurrent;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ConcurrentSkipListSet<E>
extends AbstractSet<E>
implements NavigableSet<E>,
Cloneable,
Serializable {
    private static final long serialVersionUID = -2479143111061671589L;
    private final ConcurrentNavigableMap<E, Object> m;

    public ConcurrentSkipListSet() {
        this.m = new ConcurrentSkipListMap<E, Object>();
    }

    public ConcurrentSkipListSet(Comparator<? super E> comparator) {
        this.m = new ConcurrentSkipListMap<E, Object>(comparator);
    }

    public ConcurrentSkipListSet(Collection<? extends E> c) {
        this.m = new ConcurrentSkipListMap<E, Object>();
        this.addAll(c);
    }

    public ConcurrentSkipListSet(SortedSet<E> s) {
        this.m = new ConcurrentSkipListMap<E, Object>(s.comparator());
        this.addAll(s);
    }

    ConcurrentSkipListSet(ConcurrentNavigableMap<E, Object> m) {
        this.m = m;
    }

    public ConcurrentSkipListSet<E> clone() {
        try {
            ConcurrentSkipListSet clone = (ConcurrentSkipListSet)super.clone();
            clone.setMap(new ConcurrentSkipListMap<E, Object>(this.m));
            return clone;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
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
        return this.m.putIfAbsent(e, Boolean.TRUE) == null;
    }

    @Override
    public boolean remove(Object o) {
        return this.m.remove(o, Boolean.TRUE);
    }

    @Override
    public void clear() {
        this.m.clear();
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
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Set)) {
            return false;
        }
        Collection c = (Collection)o;
        try {
            return this.containsAll(c) && c.containsAll(this);
        }
        catch (ClassCastException | NullPointerException unused) {
            return false;
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object e : c) {
            if (!this.remove(e)) continue;
            modified = true;
        }
        return modified;
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
        Map.Entry e = this.m.pollFirstEntry();
        return e == null ? null : (E)e.getKey();
    }

    @Override
    public E pollLast() {
        Map.Entry e = this.m.pollLastEntry();
        return e == null ? null : (E)e.getKey();
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
    public void addFirst(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addLast(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return new ConcurrentSkipListSet<E>(this.m.subMap((Object)fromElement, fromInclusive, (Object)toElement, toInclusive));
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new ConcurrentSkipListSet<E>(this.m.headMap((Object)toElement, inclusive));
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new ConcurrentSkipListSet<E>(this.m.tailMap((Object)fromElement, inclusive));
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, E toElement) {
        return this.subSet(fromElement, true, toElement, false);
    }

    @Override
    public NavigableSet<E> headSet(E toElement) {
        return this.headSet(toElement, false);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement) {
        return this.tailSet(fromElement, true);
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ConcurrentSkipListSet<E>(this.m.descendingMap());
    }

    @Override
    public Spliterator<E> spliterator() {
        return this.m instanceof ConcurrentSkipListMap ? ((ConcurrentSkipListMap)this.m).keySpliterator() : (ConcurrentSkipListMap.SubMap)this.m.new ConcurrentSkipListMap.SubMap.SubMapKeyIterator();
    }

    private void setMap(ConcurrentNavigableMap<E, Object> map) {
        Field mapField = AccessController.doPrivileged(() -> {
            try {
                Field f = ConcurrentSkipListSet.class.getDeclaredField("m");
                f.setAccessible(true);
                return f;
            }
            catch (ReflectiveOperationException e) {
                throw new Error(e);
            }
        });
        try {
            mapField.set(this, map);
        }
        catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }
}

