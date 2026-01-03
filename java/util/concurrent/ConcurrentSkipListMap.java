/*
 * Decompiled with CFR 0.152.
 */
package java.util.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ConcurrentSkipListMap<K, V>
extends AbstractMap<K, V>
implements ConcurrentNavigableMap<K, V>,
Cloneable,
Serializable {
    private static final long serialVersionUID = -8627078645895051609L;
    final Comparator<? super K> comparator;
    private transient Index<K, V> head;
    private transient LongAdder adder;
    private transient KeySet<K, V> keySet;
    private transient Values<K, V> values;
    private transient EntrySet<K, V> entrySet;
    private transient SubMap<K, V> descendingMap;
    private static final int EQ = 1;
    private static final int LT = 2;
    private static final int GT = 0;
    private static final VarHandle HEAD;
    private static final VarHandle ADDER;
    private static final VarHandle NEXT;
    private static final VarHandle VAL;
    private static final VarHandle RIGHT;

    static int cpr(Comparator c, Object x, Object y) {
        return c != null ? c.compare(x, y) : ((Comparable)x).compareTo(y);
    }

    final Node<K, V> baseHead() {
        VarHandle.acquireFence();
        Index<K, V> h = this.head;
        return h == null ? null : h.node;
    }

    static <K, V> void unlinkNode(Node<K, V> b, Node<K, V> n) {
        if (b != null && n != null) {
            Node p;
            block2: {
                Node f;
                do {
                    if ((f = n.next) == null || f.key != null) continue;
                    p = f.next;
                    break block2;
                } while (!NEXT.compareAndSet(n, f, new Node<Object, Object>(null, null, f)));
                p = f;
            }
            NEXT.compareAndSet(b, n, p);
        }
    }

    private void addCount(long c) {
        LongAdder a;
        while ((a = this.adder) == null && !ADDER.compareAndSet(this, null, a = new LongAdder())) {
        }
        a.add(c);
    }

    final long getAdderCount() {
        LongAdder a;
        while ((a = this.adder) == null && !ADDER.compareAndSet(this, null, a = new LongAdder())) {
        }
        long c = a.sum();
        return c <= 0L ? 0L : c;
    }

    private Node<K, V> findPredecessor(Object key, Comparator<? super K> cmp) {
        VarHandle.acquireFence();
        Index<K, V> q = this.head;
        if (q == null || key == null) {
            return null;
        }
        while (true) {
            Index d;
            Index r;
            if ((r = q.right) != null) {
                Object k;
                Node p = r.node;
                if (p == null || (k = p.key) == null || p.val == null) {
                    RIGHT.compareAndSet(q, r, r.right);
                    continue;
                }
                if (ConcurrentSkipListMap.cpr(cmp, key, k) > 0) {
                    q = r;
                    continue;
                }
            }
            if ((d = q.down) == null) break;
            q = d;
        }
        return q.node;
    }

    private Node<K, V> findNode(Object key) {
        Node<Object, V> b;
        if (key == null) {
            throw new NullPointerException();
        }
        Comparator<? super K> cmp = this.comparator;
        block0: while ((b = this.findPredecessor(key, cmp)) != null) {
            Node n;
            while ((n = b.next) != null) {
                Object k = n.key;
                if (k == null) continue block0;
                Object v = n.val;
                if (v == null) {
                    ConcurrentSkipListMap.unlinkNode(b, n);
                    continue;
                }
                int c = ConcurrentSkipListMap.cpr(cmp, key, k);
                if (c > 0) {
                    b = n;
                    continue;
                }
                if (c != 0) break block0;
                return n;
            }
            break block0;
        }
        return null;
    }

    private V doGet(Object key) {
        V result;
        block10: {
            VarHandle.acquireFence();
            if (key == null) {
                throw new NullPointerException();
            }
            Comparator<? super K> cmp = this.comparator;
            result = null;
            Index<K, V> q = this.head;
            if (q != null) {
                int c;
                Object v;
                while (true) {
                    Index d;
                    Index r;
                    if ((r = q.right) != null) {
                        Object k;
                        Node p = r.node;
                        if (p == null || (k = p.key) == null || (v = p.val) == null) {
                            RIGHT.compareAndSet(q, r, r.right);
                            continue;
                        }
                        c = ConcurrentSkipListMap.cpr(cmp, key, k);
                        if (c > 0) {
                            q = r;
                            continue;
                        }
                        if (c == 0) {
                            result = v;
                            break block10;
                        }
                    }
                    if ((d = q.down) == null) break;
                    q = d;
                }
                Node b = q.node;
                if (b != null) {
                    Node n;
                    while ((n = b.next) != null) {
                        Object k = n.key;
                        v = n.val;
                        if (v == null || k == null || (c = ConcurrentSkipListMap.cpr(cmp, key, k)) > 0) {
                            b = n;
                            continue;
                        }
                        if (c != 0) break;
                        result = v;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private V doPut(K key, V value, boolean onlyIfAbsent) {
        Node<K, V> z;
        Index<Object, Object> h;
        int levels;
        if (key == null) {
            throw new NullPointerException();
        }
        Comparator<? super K> cmp = this.comparator;
        while (true) {
            block18: {
                Node<K, V> p;
                Node n;
                int c;
                Node<Object, Object> b;
                VarHandle.acquireFence();
                levels = 0;
                h = this.head;
                if (h == null) {
                    Node<Object, Object> base = new Node<Object, Object>(null, null, null);
                    h = new Index<Object, Object>(base, null, null);
                    b = HEAD.compareAndSet(this, null, h) ? base : null;
                } else {
                    Index<K, V> q = h;
                    while (true) {
                        Index d;
                        Index r;
                        if ((r = q.right) != null) {
                            Object k;
                            Node p2 = r.node;
                            if (p2 == null || (k = p2.key) == null || p2.val == null) {
                                RIGHT.compareAndSet(q, r, r.right);
                                continue;
                            }
                            if (ConcurrentSkipListMap.cpr(cmp, key, k) > 0) {
                                q = r;
                                continue;
                            }
                        }
                        if ((d = q.down) == null) break;
                        ++levels;
                        q = d;
                    }
                    b = q.node;
                }
                if (b == null) continue;
                z = null;
                do {
                    if ((n = b.next) == null) {
                        if (b.key == null) {
                            ConcurrentSkipListMap.cpr(cmp, key, key);
                        }
                        c = -1;
                        continue;
                    }
                    Object k = n.key;
                    if (k != null) {
                        Object v = n.val;
                        if (v == null) {
                            ConcurrentSkipListMap.unlinkNode(b, n);
                            c = 1;
                            continue;
                        }
                        c = ConcurrentSkipListMap.cpr(cmp, key, k);
                        if (c > 0) {
                            b = n;
                            continue;
                        }
                        if (c != 0 || !onlyIfAbsent && !VAL.compareAndSet(n, v, value)) continue;
                        return v;
                    }
                    break block18;
                } while (c >= 0 || !NEXT.compareAndSet(b, n, p = new Node<K, V>(key, value, n)));
                z = p;
            }
            if (z != null) break;
        }
        int lr = ThreadLocalRandom.nextSecondarySeed();
        if ((lr & 3) == 0) {
            int hr = ThreadLocalRandom.nextSecondarySeed();
            long rnd = (long)hr << 32 | (long)lr & 0xFFFFFFFFL;
            int skips = levels;
            Index<K, V> x = null;
            while (true) {
                x = new Index<K, V>(z, x, null);
                if (rnd >= 0L || --skips < 0) break;
                rnd <<= 1;
            }
            if (ConcurrentSkipListMap.addIndices(h, skips, x, cmp) && skips < 0 && this.head == h) {
                Index<K, V> hx = new Index<K, V>(z, x, null);
                Index nh = new Index(h.node, h, hx);
                HEAD.compareAndSet(this, h, nh);
            }
            if (z.val == null) {
                this.findPredecessor(key, cmp);
            }
        }
        this.addCount(1L);
        return null;
    }

    static <K, V> boolean addIndices(Index<K, V> q, int skips, Index<K, V> x, Comparator<? super K> cmp) {
        Object key;
        Node z;
        if (x != null && (z = x.node) != null && (key = z.key) != null && q != null) {
            boolean retrying = false;
            while (true) {
                int c;
                Index r;
                if ((r = q.right) != null) {
                    Object k;
                    Node p = r.node;
                    if (p == null || (k = p.key) == null || p.val == null) {
                        RIGHT.compareAndSet(q, r, r.right);
                        c = 0;
                    } else {
                        c = ConcurrentSkipListMap.cpr(cmp, key, k);
                        if (c > 0) {
                            q = r;
                        } else if (c == 0) {
                            break;
                        }
                    }
                } else {
                    c = -1;
                }
                if (c >= 0) continue;
                Index d = q.down;
                if (d != null && skips > 0) {
                    --skips;
                    q = d;
                    continue;
                }
                if (d != null && !retrying && !ConcurrentSkipListMap.addIndices(d, 0, x.down, cmp)) break;
                x.right = r;
                if (RIGHT.compareAndSet(q, r, x)) {
                    return true;
                }
                retrying = true;
            }
        }
        return false;
    }

    final V doRemove(Object key, Object value) {
        Node<Object, V> b;
        if (key == null) {
            throw new NullPointerException();
        }
        Comparator<? super K> cmp = this.comparator;
        V result = null;
        block0: while ((b = this.findPredecessor(key, cmp)) != null && result == null) {
            Node n;
            while ((n = b.next) != null) {
                Object k = n.key;
                if (k == null) continue block0;
                Object v = n.val;
                if (v == null) {
                    ConcurrentSkipListMap.unlinkNode(b, n);
                    continue;
                }
                int c = ConcurrentSkipListMap.cpr(cmp, key, k);
                if (c > 0) {
                    b = n;
                    continue;
                }
                if (c < 0 || value != null && !value.equals(v)) break block0;
                if (!VAL.compareAndSet(n, v, null)) continue;
                result = v;
                ConcurrentSkipListMap.unlinkNode(b, n);
                continue block0;
            }
            break block0;
        }
        if (result != null) {
            this.tryReduceLevel();
            this.addCount(-1L);
        }
        return result;
    }

    private void tryReduceLevel() {
        Index e;
        Index d;
        Index<K, V> h = this.head;
        if (h != null && h.right == null && (d = h.down) != null && d.right == null && (e = d.down) != null && e.right == null && HEAD.compareAndSet(this, h, d) && h.right != null) {
            HEAD.compareAndSet(this, d, h);
        }
    }

    final Node<K, V> findFirst() {
        Node<K, V> b = this.baseHead();
        if (b != null) {
            Node n;
            while ((n = b.next) != null) {
                if (n.val == null) {
                    ConcurrentSkipListMap.unlinkNode(b, n);
                    continue;
                }
                return n;
            }
        }
        return null;
    }

    final AbstractMap.SimpleImmutableEntry<K, V> findFirstEntry() {
        Node<K, V> b = this.baseHead();
        if (b != null) {
            Node n;
            while ((n = b.next) != null) {
                Object v = n.val;
                if (v == null) {
                    ConcurrentSkipListMap.unlinkNode(b, n);
                    continue;
                }
                return new AbstractMap.SimpleImmutableEntry(n.key, v);
            }
        }
        return null;
    }

    private AbstractMap.SimpleImmutableEntry<K, V> doRemoveFirstEntry() {
        Node<K, V> b = this.baseHead();
        if (b != null) {
            Node n;
            while ((n = b.next) != null) {
                Object v = n.val;
                if (v != null && !VAL.compareAndSet(n, v, null)) continue;
                Object k = n.key;
                ConcurrentSkipListMap.unlinkNode(b, n);
                if (v == null) continue;
                this.tryReduceLevel();
                this.findPredecessor(k, this.comparator);
                this.addCount(-1L);
                return new AbstractMap.SimpleImmutableEntry(k, v);
            }
        }
        return null;
    }

    /*
     * Unable to fully structure code
     */
    final Node<K, V> findLast() {
        block0: while (true) {
            VarHandle.acquireFence();
            q = this.head;
            if (q == null) break;
            while (true) {
                if ((r = q.right) != null) {
                    p = r.node;
                    if (p == null || p.val == null) {
                        ConcurrentSkipListMap.RIGHT.compareAndSet(q, r, r.right);
                        continue;
                    }
                    q = r;
                    continue;
                }
                d = q.down;
                if (d == null) break;
                q = d;
            }
            b = q.node;
            if (b == null) continue;
            while (true) {
                if ((n = b.next) == null) {
                    if (b.key == null) break block0;
                    return b;
                }
                if (n.key != null) ** break;
                continue block0;
                if (n.val == null) {
                    ConcurrentSkipListMap.unlinkNode(b, n);
                    continue;
                }
                b = n;
            }
            break;
        }
        return null;
    }

    final AbstractMap.SimpleImmutableEntry<K, V> findLastEntry() {
        Node<K, V> n;
        Object v;
        do {
            if ((n = this.findLast()) != null) continue;
            return null;
        } while ((v = n.val) == null);
        return new AbstractMap.SimpleImmutableEntry(n.key, v);
    }

    /*
     * Unable to fully structure code
     */
    private Map.Entry<K, V> doRemoveLastEntry() {
        block9: {
            block0: while (true) {
                VarHandle.acquireFence();
                q = this.head;
                if (q == null) break block9;
                while (true) {
                    if ((r = q.right) != null) {
                        p = r.node;
                        if (p == null || p.val == null) {
                            ConcurrentSkipListMap.RIGHT.compareAndSet(q, r, r.right);
                            continue;
                        }
                        if (p.next != null) {
                            q = r;
                            continue;
                        }
                    }
                    if ((d = q.down) == null) break;
                    q = d;
                }
                b = q.node;
                if (b == null) continue;
                while (true) {
                    if ((n = b.next) == null) {
                        if (b.key != null) continue block0;
                        break block9;
                    }
                    k = n.key;
                    if (k != null) ** break;
                    continue block0;
                    v = n.val;
                    if (v == null) {
                        ConcurrentSkipListMap.unlinkNode(b, n);
                        continue;
                    }
                    if (n.next != null) {
                        b = n;
                        continue;
                    }
                    if (ConcurrentSkipListMap.VAL.compareAndSet(n, v, null)) break block0;
                }
                break;
            }
            ConcurrentSkipListMap.unlinkNode(b, n);
            this.tryReduceLevel();
            this.findPredecessor(k, this.comparator);
            this.addCount(-1L);
            return new AbstractMap.SimpleImmutableEntry<K, V>(k, v);
        }
        return null;
    }

    final Node<K, V> findNear(K key, int rel, Comparator<? super K> cmp) {
        Node<Object, V> result;
        if (key == null) {
            throw new NullPointerException();
        }
        block0: while (true) {
            Node<Object, V> b;
            if ((b = this.findPredecessor(key, cmp)) == null) {
                result = null;
                break;
            }
            while (true) {
                Node n;
                if ((n = b.next) == null) {
                    result = (rel & 2) != 0 && b.key != null ? b : null;
                    break block0;
                }
                Object k = n.key;
                if (k == null) continue block0;
                if (n.val == null) {
                    ConcurrentSkipListMap.unlinkNode(b, n);
                    continue;
                }
                int c = ConcurrentSkipListMap.cpr(cmp, key, k);
                if (c == 0 && (rel & 1) != 0 || c < 0 && (rel & 2) == 0) {
                    result = n;
                    break block0;
                }
                if (c <= 0 && (rel & 2) != 0) {
                    result = b.key != null ? b : null;
                    break block0;
                }
                b = n;
            }
            break;
        }
        return result;
    }

    final AbstractMap.SimpleImmutableEntry<K, V> findNearEntry(K key, int rel, Comparator<? super K> cmp) {
        Node<? super K, V> n;
        Object v;
        do {
            if ((n = this.findNear(key, rel, cmp)) != null) continue;
            return null;
        } while ((v = n.val) == null);
        return new AbstractMap.SimpleImmutableEntry(n.key, v);
    }

    public ConcurrentSkipListMap() {
        this.comparator = null;
    }

    public ConcurrentSkipListMap(Comparator<? super K> comparator) {
        this.comparator = comparator;
    }

    public ConcurrentSkipListMap(Map<? extends K, ? extends V> m) {
        this.comparator = null;
        this.putAll(m);
    }

    public ConcurrentSkipListMap(SortedMap<K, ? extends V> m) {
        this.comparator = m.comparator();
        this.buildFromSorted(m);
    }

    @Override
    public ConcurrentSkipListMap<K, V> clone() {
        try {
            ConcurrentSkipListMap clone = (ConcurrentSkipListMap)super.clone();
            clone.keySet = null;
            clone.entrySet = null;
            clone.values = null;
            clone.descendingMap = null;
            clone.adder = null;
            clone.buildFromSorted(this);
            return clone;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    private void buildFromSorted(SortedMap<K, ? extends V> map) {
        if (map == null) {
            throw new NullPointerException();
        }
        Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
        Index[] preds = new Index[64];
        Node<Object, Object> bp = new Node<Object, Object>(null, null, null);
        Index<Object, Object> h = preds[0] = new Index<Object, Object>(bp, null, null);
        long count = 0L;
        while (it.hasNext()) {
            Map.Entry<K, V> e = it.next();
            K k = e.getKey();
            V v = e.getValue();
            if (k == null || v == null) {
                throw new NullPointerException();
            }
            Node<K, V> z = new Node<K, V>(k, v, null);
            bp.next = z;
            bp = bp.next;
            if ((++count & 3L) != 0L) continue;
            long m = count >>> 2;
            int i = 0;
            Index<K, V> idx = null;
            do {
                idx = new Index<K, V>(z, idx, null);
                Index q = preds[i];
                if (q == null) {
                    h = new Index<Object, Object>(h.node, h, idx);
                    preds[i] = h;
                    continue;
                }
                q.right = idx;
                preds[i] = q.right;
            } while (++i < preds.length && ((m >>>= 1) & 1L) != 0L);
        }
        if (count != 0L) {
            VarHandle.releaseFence();
            this.addCount(count);
            this.head = h;
            VarHandle.fullFence();
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        Node<K, V> b = this.baseHead();
        if (b != null) {
            Node n;
            while ((n = b.next) != null) {
                Object v = n.val;
                if (v != null) {
                    s.writeObject(n.key);
                    s.writeObject(v);
                }
                b = n;
            }
        }
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        Object k;
        s.defaultReadObject();
        Index[] preds = new Index[64];
        Node<Object, Object> bp = new Node<Object, Object>(null, null, null);
        Index<Object, Object> h = preds[0] = new Index<Object, Object>(bp, null, null);
        Comparator<? super K> cmp = this.comparator;
        Object prevKey = null;
        long count = 0L;
        while ((k = s.readObject()) != null) {
            Object v = s.readObject();
            if (v == null) {
                throw new NullPointerException();
            }
            if (prevKey != null && ConcurrentSkipListMap.cpr(cmp, prevKey, k) > 0) {
                throw new IllegalStateException("out of order");
            }
            prevKey = k;
            Node<Object, Object> z = new Node<Object, Object>(k, v, null);
            bp.next = z;
            bp = bp.next;
            if ((++count & 3L) != 0L) continue;
            long m = count >>> 2;
            int i = 0;
            Index<Object, Object> idx = null;
            do {
                idx = new Index<Object, Object>(z, idx, null);
                Index q = preds[i];
                if (q == null) {
                    h = new Index<Object, Object>(h.node, h, idx);
                    preds[i] = h;
                    continue;
                }
                q.right = idx;
                preds[i] = q.right;
            } while (++i < preds.length && ((m >>>= 1) & 1L) != 0L);
        }
        if (count != 0L) {
            VarHandle.releaseFence();
            this.addCount(count);
            this.head = h;
            VarHandle.fullFence();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        return this.doGet(key) != null;
    }

    @Override
    public V get(Object key) {
        return this.doGet(key);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        V v = this.doGet(key);
        return v == null ? defaultValue : v;
    }

    @Override
    public V put(K key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return this.doPut(key, value, false);
    }

    @Override
    public V remove(Object key) {
        return this.doRemove(key, null);
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
        Node<K, V> b = this.baseHead();
        if (b != null) {
            Node n;
            while ((n = b.next) != null) {
                Object v = n.val;
                if (v != null && value.equals(v)) {
                    return true;
                }
                b = n;
            }
        }
        return false;
    }

    @Override
    public int size() {
        long c;
        return this.baseHead() == null ? 0 : ((c = this.getAdderCount()) >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)c);
    }

    @Override
    public boolean isEmpty() {
        return this.findFirst() == null;
    }

    @Override
    public void clear() {
        Index<K, V> h;
        VarHandle.acquireFence();
        while ((h = this.head) != null) {
            Index r = h.right;
            if (r != null) {
                RIGHT.compareAndSet(h, r, null);
                continue;
            }
            Index d = h.down;
            if (d != null) {
                HEAD.compareAndSet(this, h, d);
                continue;
            }
            long count = 0L;
            Node b = h.node;
            if (b != null) {
                Node n;
                while ((n = b.next) != null) {
                    Object v = n.val;
                    if (v != null && VAL.compareAndSet(n, v, null)) {
                        --count;
                        v = null;
                    }
                    if (v != null) continue;
                    ConcurrentSkipListMap.unlinkNode(b, n);
                }
            }
            if (count == 0L) break;
            this.addCount(count);
        }
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        V r;
        if (key == null || mappingFunction == null) {
            throw new NullPointerException();
        }
        V v = this.doGet(key);
        if (v == null && (r = mappingFunction.apply(key)) != null) {
            V p = this.doPut(key, r, true);
            v = p == null ? r : p;
        }
        return v;
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Node<K, V> n;
        if (key == null || remappingFunction == null) {
            throw new NullPointerException();
        }
        while ((n = this.findNode(key)) != null) {
            Object v = n.val;
            if (v == null) continue;
            V r = remappingFunction.apply(key, v);
            if (r != null) {
                if (!VAL.compareAndSet(n, v, r)) continue;
                return r;
            }
            if (this.doRemove(key, v) == null) continue;
            break;
        }
        return null;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (key == null || remappingFunction == null) {
            throw new NullPointerException();
        }
        while (true) {
            V r;
            Node<K, V> n;
            if ((n = this.findNode(key)) == null) {
                r = remappingFunction.apply(key, null);
                if (r == null) return null;
                if (this.doPut(key, r, true) != null) continue;
                return r;
            }
            Object v = n.val;
            if (v == null) continue;
            r = remappingFunction.apply(key, v);
            if (r != null) {
                if (!VAL.compareAndSet(n, v, r)) continue;
                return r;
            }
            if (this.doRemove(key, v) != null) return null;
        }
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (key == null || value == null || remappingFunction == null) {
            throw new NullPointerException();
        }
        while (true) {
            Node<K, V> n;
            if ((n = this.findNode(key)) == null) {
                if (this.doPut(key, value, true) != null) continue;
                return value;
            }
            Object v = n.val;
            if (v == null) continue;
            V r = remappingFunction.apply(v, value);
            if (r != null) {
                if (!VAL.compareAndSet(n, v, r)) continue;
                return r;
            }
            if (this.doRemove(key, v) != null) break;
        }
        return null;
    }

    @Override
    public NavigableSet<K> keySet() {
        KeySet<K, V> ks = this.keySet;
        if (ks != null) {
            return ks;
        }
        this.keySet = new KeySet(this);
        return this.keySet;
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        KeySet<K, V> ks = this.keySet;
        if (ks != null) {
            return ks;
        }
        this.keySet = new KeySet(this);
        return this.keySet;
    }

    @Override
    public Collection<V> values() {
        Values<K, V> vs = this.values;
        if (vs != null) {
            return vs;
        }
        this.values = new Values(this);
        return this.values;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        EntrySet<K, V> es = this.entrySet;
        if (es != null) {
            return es;
        }
        this.entrySet = new EntrySet(this);
        return this.entrySet;
    }

    @Override
    public ConcurrentNavigableMap<K, V> descendingMap() {
        SubMap<K, V> dm = this.descendingMap;
        if (dm != null) {
            return dm;
        }
        this.descendingMap = new SubMap(this, null, false, null, false, true);
        return this.descendingMap;
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return this.descendingMap().navigableKeySet();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Map)) {
            return false;
        }
        Map m = (Map)o;
        try {
            Comparator<? super K> cmp = this.comparator;
            Iterator it = m.entrySet().iterator();
            if (m instanceof SortedMap && ((SortedMap)m).comparator() == cmp) {
                Node<K, V> b = this.baseHead();
                if (b != null) {
                    Node n;
                    while ((n = b.next) != null) {
                        Object k;
                        Object v = n.val;
                        if (v != null && (k = n.key) != null) {
                            if (!it.hasNext()) {
                                return false;
                            }
                            Map.Entry e = it.next();
                            Object mk = e.getKey();
                            Object mv = e.getValue();
                            if (mk == null || mv == null) {
                                return false;
                            }
                            try {
                                if (ConcurrentSkipListMap.cpr(cmp, k, mk) != 0) {
                                    return false;
                                }
                            }
                            catch (ClassCastException cce) {
                                return false;
                            }
                            if (!mv.equals(v)) {
                                return false;
                            }
                        }
                        b = n;
                    }
                }
                return !it.hasNext();
            }
            while (it.hasNext()) {
                V v;
                Map.Entry e = it.next();
                Object mk = e.getKey();
                Object mv = e.getValue();
                if (mk != null && mv != null && (v = this.get(mk)) != null && v.equals(mv)) continue;
                return false;
            }
            Node<K, V> b = this.baseHead();
            if (b != null) {
                Node n;
                while ((n = b.next) != null) {
                    Object mv;
                    Object k;
                    Object v = n.val;
                    if (!(v == null || (k = n.key) == null || (mv = m.get(k)) != null && mv.equals(v))) {
                        return false;
                    }
                    b = n;
                }
            }
            return true;
        }
        catch (ClassCastException | NullPointerException unused) {
            return false;
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return this.doPut(key, value, true);
    }

    @Override
    public boolean remove(Object key, Object value) {
        if (key == null) {
            throw new NullPointerException();
        }
        return value != null && this.doRemove(key, value) != null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        if (key == null || oldValue == null || newValue == null) {
            throw new NullPointerException();
        }
        while (true) {
            Node<K, V> n;
            if ((n = this.findNode(key)) == null) {
                return false;
            }
            Object v = n.val;
            if (v == null) continue;
            if (!oldValue.equals(v)) {
                return false;
            }
            if (VAL.compareAndSet(n, v, newValue)) break;
        }
        return true;
    }

    @Override
    public V replace(K key, V value) {
        Node<K, V> n;
        Object v;
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        do {
            if ((n = this.findNode(key)) != null) continue;
            return null;
        } while ((v = n.val) == null || !VAL.compareAndSet(n, v, value));
        return v;
    }

    @Override
    public Comparator<? super K> comparator() {
        return this.comparator;
    }

    @Override
    public K firstKey() {
        Node<K, V> n = this.findFirst();
        if (n == null) {
            throw new NoSuchElementException();
        }
        return n.key;
    }

    @Override
    public K lastKey() {
        Node<K, V> n = this.findLast();
        if (n == null) {
            throw new NoSuchElementException();
        }
        return n.key;
    }

    @Override
    public V putFirst(K k, V v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V putLast(K k, V v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConcurrentNavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        if (fromKey == null || toKey == null) {
            throw new NullPointerException();
        }
        return new SubMap(this, fromKey, fromInclusive, toKey, toInclusive, false);
    }

    @Override
    public ConcurrentNavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        if (toKey == null) {
            throw new NullPointerException();
        }
        return new SubMap(this, null, false, toKey, inclusive, false);
    }

    @Override
    public ConcurrentNavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        if (fromKey == null) {
            throw new NullPointerException();
        }
        return new SubMap(this, fromKey, inclusive, null, false, false);
    }

    @Override
    public ConcurrentNavigableMap<K, V> subMap(K fromKey, K toKey) {
        return this.subMap((Object)fromKey, true, (Object)toKey, false);
    }

    @Override
    public ConcurrentNavigableMap<K, V> headMap(K toKey) {
        return this.headMap((Object)toKey, false);
    }

    @Override
    public ConcurrentNavigableMap<K, V> tailMap(K fromKey) {
        return this.tailMap((Object)fromKey, true);
    }

    @Override
    public Map.Entry<K, V> lowerEntry(K key) {
        return this.findNearEntry(key, 2, this.comparator);
    }

    @Override
    public K lowerKey(K key) {
        Node<? super K, V> n = this.findNear(key, 2, this.comparator);
        return n == null ? null : (K)n.key;
    }

    @Override
    public Map.Entry<K, V> floorEntry(K key) {
        return this.findNearEntry(key, 3, this.comparator);
    }

    @Override
    public K floorKey(K key) {
        Node<? super K, V> n = this.findNear(key, 3, this.comparator);
        return n == null ? null : (K)n.key;
    }

    @Override
    public Map.Entry<K, V> ceilingEntry(K key) {
        return this.findNearEntry(key, 1, this.comparator);
    }

    @Override
    public K ceilingKey(K key) {
        Node<? super K, V> n = this.findNear(key, 1, this.comparator);
        return n == null ? null : (K)n.key;
    }

    @Override
    public Map.Entry<K, V> higherEntry(K key) {
        return this.findNearEntry(key, 0, this.comparator);
    }

    @Override
    public K higherKey(K key) {
        Node<? super K, V> n = this.findNear(key, 0, this.comparator);
        return n == null ? null : (K)n.key;
    }

    @Override
    public Map.Entry<K, V> firstEntry() {
        return this.findFirstEntry();
    }

    @Override
    public Map.Entry<K, V> lastEntry() {
        return this.findLastEntry();
    }

    @Override
    public Map.Entry<K, V> pollFirstEntry() {
        return this.doRemoveFirstEntry();
    }

    @Override
    public Map.Entry<K, V> pollLastEntry() {
        return this.doRemoveLastEntry();
    }

    static final <E> List<E> toList(Collection<E> c) {
        ArrayList<E> list = new ArrayList<E>();
        for (E e : c) {
            list.add(e);
        }
        return list;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action == null) {
            throw new NullPointerException();
        }
        Node<K, V> b = this.baseHead();
        if (b != null) {
            Node n;
            while ((n = b.next) != null) {
                Object v = n.val;
                if (v != null) {
                    action.accept(n.key, v);
                }
                b = n;
            }
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        if (function == null) {
            throw new NullPointerException();
        }
        Node<K, V> b = this.baseHead();
        if (b != null) {
            Node n;
            while ((n = b.next) != null) {
                Object v;
                while ((v = n.val) != null) {
                    V r = function.apply(n.key, v);
                    if (r == null) {
                        throw new NullPointerException();
                    }
                    if (!VAL.compareAndSet(n, v, r)) continue;
                    break;
                }
                b = n;
            }
        }
    }

    boolean removeEntryIf(Predicate<? super Map.Entry<K, V>> function) {
        if (function == null) {
            throw new NullPointerException();
        }
        boolean removed = false;
        Node<K, V> b = this.baseHead();
        if (b != null) {
            Node n;
            while ((n = b.next) != null) {
                Object k;
                AbstractMap.SimpleImmutableEntry e;
                Object v = n.val;
                if (v != null && function.test(e = new AbstractMap.SimpleImmutableEntry(k = n.key, v)) && this.remove(k, v)) {
                    removed = true;
                }
                b = n;
            }
        }
        return removed;
    }

    boolean removeValueIf(Predicate<? super V> function) {
        if (function == null) {
            throw new NullPointerException();
        }
        boolean removed = false;
        Node<K, V> b = this.baseHead();
        if (b != null) {
            Node n;
            while ((n = b.next) != null) {
                Object v = n.val;
                if (v != null && function.test(v) && this.remove(n.key, v)) {
                    removed = true;
                }
                b = n;
            }
        }
        return removed;
    }

    final KeySpliterator<K, V> keySpliterator() {
        long est;
        Node n;
        VarHandle.acquireFence();
        Index<K, V> h = this.head;
        if (h == null) {
            n = null;
            est = 0L;
        } else {
            n = h.node;
            est = this.getAdderCount();
        }
        return new KeySpliterator<Object, V>(this.comparator, h, n, null, est);
    }

    final ValueSpliterator<K, V> valueSpliterator() {
        long est;
        Node n;
        VarHandle.acquireFence();
        Index<K, V> h = this.head;
        if (h == null) {
            n = null;
            est = 0L;
        } else {
            n = h.node;
            est = this.getAdderCount();
        }
        return new ValueSpliterator<Object, V>(this.comparator, h, n, null, est);
    }

    final EntrySpliterator<K, V> entrySpliterator() {
        long est;
        Node n;
        VarHandle.acquireFence();
        Index<K, V> h = this.head;
        if (h == null) {
            n = null;
            est = 0L;
        } else {
            n = h.node;
            est = this.getAdderCount();
        }
        return new EntrySpliterator<Object, V>(this.comparator, h, n, null, est);
    }

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            HEAD = l.findVarHandle(ConcurrentSkipListMap.class, "head", Index.class);
            ADDER = l.findVarHandle(ConcurrentSkipListMap.class, "adder", LongAdder.class);
            NEXT = l.findVarHandle(Node.class, "next", Node.class);
            VAL = l.findVarHandle(Node.class, "val", Object.class);
            RIGHT = l.findVarHandle(Index.class, "right", Index.class);
        }
        catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    static final class Index<K, V> {
        final Node<K, V> node;
        final Index<K, V> down;
        Index<K, V> right;

        Index(Node<K, V> node, Index<K, V> down, Index<K, V> right) {
            this.node = node;
            this.down = down;
            this.right = right;
        }
    }

    static final class Node<K, V> {
        final K key;
        V val;
        Node<K, V> next;

        Node(K key, V value, Node<K, V> next) {
            this.key = key;
            this.val = value;
            this.next = next;
        }
    }

    static final class KeySet<K, V>
    extends AbstractSet<K>
    implements NavigableSet<K> {
        final ConcurrentNavigableMap<K, V> m;

        KeySet(ConcurrentNavigableMap<K, V> map) {
            this.m = map;
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
        public boolean remove(Object o) {
            return this.m.remove(o) != null;
        }

        @Override
        public void clear() {
            this.m.clear();
        }

        @Override
        public K lower(K e) {
            return this.m.lowerKey(e);
        }

        @Override
        public K floor(K e) {
            return this.m.floorKey(e);
        }

        @Override
        public K ceiling(K e) {
            return this.m.ceilingKey(e);
        }

        @Override
        public K higher(K e) {
            return this.m.higherKey(e);
        }

        @Override
        public Comparator<? super K> comparator() {
            return this.m.comparator();
        }

        @Override
        public K first() {
            return this.m.firstKey();
        }

        @Override
        public K last() {
            return this.m.lastKey();
        }

        @Override
        public K pollFirst() {
            Map.Entry e = this.m.pollFirstEntry();
            return e == null ? null : (K)e.getKey();
        }

        @Override
        public K pollLast() {
            Map.Entry e = this.m.pollLastEntry();
            return e == null ? null : (K)e.getKey();
        }

        @Override
        public Iterator<K> iterator() {
            return this.m instanceof ConcurrentSkipListMap ? new KeyIterator((ConcurrentSkipListMap)this.m) : (SubMap)this.m.new SubMap.SubMapKeyIterator();
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
        public Object[] toArray() {
            return ConcurrentSkipListMap.toList(this).toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return ConcurrentSkipListMap.toList(this).toArray(a);
        }

        @Override
        public Iterator<K> descendingIterator() {
            return this.descendingSet().iterator();
        }

        @Override
        public NavigableSet<K> subSet(K fromElement, boolean fromInclusive, K toElement, boolean toInclusive) {
            return new KeySet<K, V>(this.m.subMap((Object)fromElement, fromInclusive, (Object)toElement, toInclusive));
        }

        @Override
        public NavigableSet<K> headSet(K toElement, boolean inclusive) {
            return new KeySet<K, V>(this.m.headMap((Object)toElement, inclusive));
        }

        @Override
        public NavigableSet<K> tailSet(K fromElement, boolean inclusive) {
            return new KeySet<K, V>(this.m.tailMap((Object)fromElement, inclusive));
        }

        @Override
        public NavigableSet<K> subSet(K fromElement, K toElement) {
            return this.subSet(fromElement, true, toElement, false);
        }

        @Override
        public NavigableSet<K> headSet(K toElement) {
            return this.headSet(toElement, false);
        }

        @Override
        public NavigableSet<K> tailSet(K fromElement) {
            return this.tailSet(fromElement, true);
        }

        @Override
        public NavigableSet<K> descendingSet() {
            return new KeySet<K, V>(this.m.descendingMap());
        }

        @Override
        public Spliterator<K> spliterator() {
            return this.m instanceof ConcurrentSkipListMap ? ((ConcurrentSkipListMap)this.m).keySpliterator() : (SubMap)this.m.new SubMap.SubMapKeyIterator();
        }
    }

    static final class EntrySet<K, V>
    extends AbstractSet<Map.Entry<K, V>> {
        final ConcurrentNavigableMap<K, V> m;

        EntrySet(ConcurrentNavigableMap<K, V> map) {
            this.m = map;
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return this.m instanceof ConcurrentSkipListMap ? new EntryIterator((ConcurrentSkipListMap)this.m) : new SubMap.SubMapEntryIterator((SubMap)this.m);
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            Object v = this.m.get(e.getKey());
            return v != null && v.equals(e.getValue());
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            return this.m.remove(e.getKey(), e.getValue());
        }

        @Override
        public boolean isEmpty() {
            return this.m.isEmpty();
        }

        @Override
        public int size() {
            return this.m.size();
        }

        @Override
        public void clear() {
            this.m.clear();
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
        public Object[] toArray() {
            return ConcurrentSkipListMap.toList(this).toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return ConcurrentSkipListMap.toList(this).toArray(a);
        }

        @Override
        public Spliterator<Map.Entry<K, V>> spliterator() {
            return this.m instanceof ConcurrentSkipListMap ? ((ConcurrentSkipListMap)this.m).entrySpliterator() : new SubMap.SubMapEntryIterator((SubMap)this.m);
        }

        @Override
        public boolean removeIf(Predicate<? super Map.Entry<K, V>> filter) {
            if (filter == null) {
                throw new NullPointerException();
            }
            if (this.m instanceof ConcurrentSkipListMap) {
                return ((ConcurrentSkipListMap)this.m).removeEntryIf(filter);
            }
            SubMap.SubMapEntryIterator it = new SubMap.SubMapEntryIterator((SubMap)this.m);
            boolean removed = false;
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry)it.next();
                if (!filter.test(e) || !this.m.remove(e.getKey(), e.getValue())) continue;
                removed = true;
            }
            return removed;
        }
    }

    static final class Values<K, V>
    extends AbstractCollection<V> {
        final ConcurrentNavigableMap<K, V> m;

        Values(ConcurrentNavigableMap<K, V> map) {
            this.m = map;
        }

        @Override
        public Iterator<V> iterator() {
            return this.m instanceof ConcurrentSkipListMap ? new ValueIterator((ConcurrentSkipListMap)this.m) : new SubMap.SubMapValueIterator((SubMap)this.m);
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
            return this.m.containsValue(o);
        }

        @Override
        public void clear() {
            this.m.clear();
        }

        @Override
        public Object[] toArray() {
            return ConcurrentSkipListMap.toList(this).toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return ConcurrentSkipListMap.toList(this).toArray(a);
        }

        @Override
        public Spliterator<V> spliterator() {
            return this.m instanceof ConcurrentSkipListMap ? ((ConcurrentSkipListMap)this.m).valueSpliterator() : new SubMap.SubMapValueIterator((SubMap)this.m);
        }

        @Override
        public boolean removeIf(Predicate<? super V> filter) {
            if (filter == null) {
                throw new NullPointerException();
            }
            if (this.m instanceof ConcurrentSkipListMap) {
                return ((ConcurrentSkipListMap)this.m).removeValueIf(filter);
            }
            SubMap.SubMapEntryIterator it = new SubMap.SubMapEntryIterator((SubMap)this.m);
            boolean removed = false;
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry)it.next();
                Object v = e.getValue();
                if (!filter.test(v) || !this.m.remove(e.getKey(), v)) continue;
                removed = true;
            }
            return removed;
        }
    }

    static final class SubMap<K, V>
    extends AbstractMap<K, V>
    implements ConcurrentNavigableMap<K, V>,
    Serializable {
        private static final long serialVersionUID = -7647078645895051609L;
        final ConcurrentSkipListMap<K, V> m;
        private final K lo;
        private final K hi;
        private final boolean loInclusive;
        private final boolean hiInclusive;
        final boolean isDescending;
        private transient KeySet<K, V> keySetView;
        private transient Values<K, V> valuesView;
        private transient EntrySet<K, V> entrySetView;

        SubMap(ConcurrentSkipListMap<K, V> map, K fromKey, boolean fromInclusive, K toKey, boolean toInclusive, boolean isDescending) {
            Comparator cmp = map.comparator;
            if (fromKey != null && toKey != null && ConcurrentSkipListMap.cpr(cmp, fromKey, toKey) > 0) {
                throw new IllegalArgumentException("inconsistent range");
            }
            this.m = map;
            this.lo = fromKey;
            this.hi = toKey;
            this.loInclusive = fromInclusive;
            this.hiInclusive = toInclusive;
            this.isDescending = isDescending;
        }

        boolean tooLow(Object key, Comparator<? super K> cmp) {
            int c;
            return this.lo != null && ((c = ConcurrentSkipListMap.cpr(cmp, key, this.lo)) < 0 || c == 0 && !this.loInclusive);
        }

        boolean tooHigh(Object key, Comparator<? super K> cmp) {
            int c;
            return this.hi != null && ((c = ConcurrentSkipListMap.cpr(cmp, key, this.hi)) > 0 || c == 0 && !this.hiInclusive);
        }

        boolean inBounds(Object key, Comparator<? super K> cmp) {
            return !this.tooLow(key, cmp) && !this.tooHigh(key, cmp);
        }

        void checkKeyBounds(K key, Comparator<? super K> cmp) {
            if (key == null) {
                throw new NullPointerException();
            }
            if (!this.inBounds(key, cmp)) {
                throw new IllegalArgumentException("key out of range");
            }
        }

        boolean isBeforeEnd(Node<K, V> n, Comparator<? super K> cmp) {
            if (n == null) {
                return false;
            }
            if (this.hi == null) {
                return true;
            }
            Object k = n.key;
            if (k == null) {
                return true;
            }
            int c = ConcurrentSkipListMap.cpr(cmp, k, this.hi);
            return c < 0 || c == 0 && this.hiInclusive;
        }

        Node<K, V> loNode(Comparator<? super K> cmp) {
            if (this.lo == null) {
                return this.m.findFirst();
            }
            if (this.loInclusive) {
                return this.m.findNear((K)this.lo, 1, cmp);
            }
            return this.m.findNear((K)this.lo, 0, cmp);
        }

        Node<K, V> hiNode(Comparator<? super K> cmp) {
            if (this.hi == null) {
                return this.m.findLast();
            }
            if (this.hiInclusive) {
                return this.m.findNear((K)this.hi, 3, cmp);
            }
            return this.m.findNear((K)this.hi, 2, cmp);
        }

        K lowestKey() {
            Comparator cmp = this.m.comparator;
            Node n = this.loNode(cmp);
            if (this.isBeforeEnd(n, cmp)) {
                return n.key;
            }
            throw new NoSuchElementException();
        }

        K highestKey() {
            Object last;
            Comparator cmp = this.m.comparator;
            Node n = this.hiNode(cmp);
            if (n != null && this.inBounds(last = n.key, cmp)) {
                return last;
            }
            throw new NoSuchElementException();
        }

        Map.Entry<K, V> lowestEntry() {
            Node n;
            Object v;
            Comparator cmp = this.m.comparator;
            do {
                if ((n = this.loNode(cmp)) != null && this.isBeforeEnd(n, cmp)) continue;
                return null;
            } while ((v = n.val) == null);
            return new AbstractMap.SimpleImmutableEntry(n.key, v);
        }

        Map.Entry<K, V> highestEntry() {
            Node n;
            Object v;
            Comparator cmp = this.m.comparator;
            do {
                if ((n = this.hiNode(cmp)) != null && this.inBounds(n.key, cmp)) continue;
                return null;
            } while ((v = n.val) == null);
            return new AbstractMap.SimpleImmutableEntry(n.key, v);
        }

        Map.Entry<K, V> removeLowest() {
            Object k;
            V v;
            Comparator cmp = this.m.comparator;
            do {
                Node n;
                if ((n = this.loNode(cmp)) == null) {
                    return null;
                }
                k = n.key;
                if (this.inBounds(k, cmp)) continue;
                return null;
            } while ((v = this.m.doRemove(k, null)) == null);
            return new AbstractMap.SimpleImmutableEntry(k, v);
        }

        Map.Entry<K, V> removeHighest() {
            Object k;
            V v;
            Comparator cmp = this.m.comparator;
            do {
                Node n;
                if ((n = this.hiNode(cmp)) == null) {
                    return null;
                }
                k = n.key;
                if (this.inBounds(k, cmp)) continue;
                return null;
            } while ((v = this.m.doRemove(k, null)) == null);
            return new AbstractMap.SimpleImmutableEntry(k, v);
        }

        Map.Entry<K, V> getNearEntry(K key, int rel) {
            Comparator cmp = this.m.comparator;
            if (this.isDescending) {
                rel = (rel & 2) == 0 ? (rel |= 2) : (rel &= 0xFFFFFFFD);
            }
            if (this.tooLow(key, cmp)) {
                return (rel & 2) != 0 ? null : this.lowestEntry();
            }
            if (this.tooHigh(key, cmp)) {
                return (rel & 2) != 0 ? this.highestEntry() : null;
            }
            AbstractMap.SimpleImmutableEntry e = this.m.findNearEntry(key, rel, cmp);
            if (e == null || !this.inBounds(e.getKey(), cmp)) {
                return null;
            }
            return e;
        }

        K getNearKey(K key, int rel) {
            Node n;
            Comparator cmp = this.m.comparator;
            if (this.isDescending) {
                rel = (rel & 2) == 0 ? (rel |= 2) : (rel &= 0xFFFFFFFD);
            }
            if (this.tooLow(key, cmp)) {
                Node n2;
                if ((rel & 2) == 0 && this.isBeforeEnd(n2 = this.loNode(cmp), cmp)) {
                    return n2.key;
                }
                return null;
            }
            if (this.tooHigh(key, cmp)) {
                Object last;
                Node n3;
                if ((rel & 2) != 0 && (n3 = this.hiNode(cmp)) != null && this.inBounds(last = n3.key, cmp)) {
                    return last;
                }
                return null;
            }
            do {
                if ((n = this.m.findNear(key, rel, cmp)) != null && this.inBounds(n.key, cmp)) continue;
                return null;
            } while (n.val == null);
            return n.key;
        }

        @Override
        public boolean containsKey(Object key) {
            if (key == null) {
                throw new NullPointerException();
            }
            return this.inBounds(key, this.m.comparator) && this.m.containsKey(key);
        }

        @Override
        public V get(Object key) {
            if (key == null) {
                throw new NullPointerException();
            }
            return !this.inBounds(key, this.m.comparator) ? null : (V)this.m.get(key);
        }

        @Override
        public V put(K key, V value) {
            this.checkKeyBounds(key, this.m.comparator);
            return this.m.put(key, value);
        }

        @Override
        public V remove(Object key) {
            return !this.inBounds(key, this.m.comparator) ? null : (V)this.m.remove(key);
        }

        @Override
        public int size() {
            Comparator cmp = this.m.comparator;
            long count = 0L;
            Node<Object, V> n = this.loNode(cmp);
            while (this.isBeforeEnd(n, cmp)) {
                if (n.val != null) {
                    ++count;
                }
                n = n.next;
            }
            return count >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)count;
        }

        @Override
        public boolean isEmpty() {
            Comparator cmp = this.m.comparator;
            return !this.isBeforeEnd(this.loNode(cmp), cmp);
        }

        @Override
        public boolean containsValue(Object value) {
            if (value == null) {
                throw new NullPointerException();
            }
            Comparator cmp = this.m.comparator;
            Node<Object, V> n = this.loNode(cmp);
            while (this.isBeforeEnd(n, cmp)) {
                Object v = n.val;
                if (v != null && value.equals(v)) {
                    return true;
                }
                n = n.next;
            }
            return false;
        }

        @Override
        public void clear() {
            Comparator cmp = this.m.comparator;
            Node<Object, V> n = this.loNode(cmp);
            while (this.isBeforeEnd(n, cmp)) {
                if (n.val != null) {
                    this.m.remove(n.key);
                }
                n = n.next;
            }
        }

        @Override
        public V putIfAbsent(K key, V value) {
            this.checkKeyBounds(key, this.m.comparator);
            return this.m.putIfAbsent(key, value);
        }

        @Override
        public boolean remove(Object key, Object value) {
            return this.inBounds(key, this.m.comparator) && this.m.remove(key, value);
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            this.checkKeyBounds(key, this.m.comparator);
            return this.m.replace(key, oldValue, newValue);
        }

        @Override
        public V replace(K key, V value) {
            this.checkKeyBounds(key, this.m.comparator);
            return this.m.replace(key, value);
        }

        @Override
        public Comparator<? super K> comparator() {
            Comparator<K> cmp = this.m.comparator();
            if (this.isDescending) {
                return Collections.reverseOrder(cmp);
            }
            return cmp;
        }

        SubMap<K, V> newSubMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            Comparator cmp = this.m.comparator;
            if (this.isDescending) {
                K tk = fromKey;
                fromKey = toKey;
                toKey = tk;
                boolean ti = fromInclusive;
                fromInclusive = toInclusive;
                toInclusive = ti;
            }
            if (this.lo != null) {
                if (fromKey == null) {
                    fromKey = this.lo;
                    fromInclusive = this.loInclusive;
                } else {
                    int c = ConcurrentSkipListMap.cpr(cmp, fromKey, this.lo);
                    if (c < 0 || c == 0 && !this.loInclusive && fromInclusive) {
                        throw new IllegalArgumentException("key out of range");
                    }
                }
            }
            if (this.hi != null) {
                if (toKey == null) {
                    toKey = this.hi;
                    toInclusive = this.hiInclusive;
                } else {
                    int c = ConcurrentSkipListMap.cpr(cmp, toKey, this.hi);
                    if (c > 0 || c == 0 && !this.hiInclusive && toInclusive) {
                        throw new IllegalArgumentException("key out of range");
                    }
                }
            }
            return new SubMap<K, V>(this.m, fromKey, fromInclusive, toKey, toInclusive, this.isDescending);
        }

        @Override
        public SubMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            if (fromKey == null || toKey == null) {
                throw new NullPointerException();
            }
            return this.newSubMap(fromKey, fromInclusive, toKey, toInclusive);
        }

        @Override
        public SubMap<K, V> headMap(K toKey, boolean inclusive) {
            if (toKey == null) {
                throw new NullPointerException();
            }
            return this.newSubMap(null, false, toKey, inclusive);
        }

        @Override
        public SubMap<K, V> tailMap(K fromKey, boolean inclusive) {
            if (fromKey == null) {
                throw new NullPointerException();
            }
            return this.newSubMap(fromKey, inclusive, null, false);
        }

        @Override
        public SubMap<K, V> subMap(K fromKey, K toKey) {
            return this.subMap((Object)fromKey, true, (Object)toKey, false);
        }

        @Override
        public SubMap<K, V> headMap(K toKey) {
            return this.headMap((Object)toKey, false);
        }

        @Override
        public SubMap<K, V> tailMap(K fromKey) {
            return this.tailMap((Object)fromKey, true);
        }

        @Override
        public SubMap<K, V> descendingMap() {
            return new SubMap<K, V>(this.m, this.lo, this.loInclusive, this.hi, this.hiInclusive, !this.isDescending);
        }

        @Override
        public Map.Entry<K, V> ceilingEntry(K key) {
            return this.getNearEntry(key, 1);
        }

        @Override
        public K ceilingKey(K key) {
            return this.getNearKey(key, 1);
        }

        @Override
        public Map.Entry<K, V> lowerEntry(K key) {
            return this.getNearEntry(key, 2);
        }

        @Override
        public K lowerKey(K key) {
            return this.getNearKey(key, 2);
        }

        @Override
        public Map.Entry<K, V> floorEntry(K key) {
            return this.getNearEntry(key, 3);
        }

        @Override
        public K floorKey(K key) {
            return this.getNearKey(key, 3);
        }

        @Override
        public Map.Entry<K, V> higherEntry(K key) {
            return this.getNearEntry(key, 0);
        }

        @Override
        public K higherKey(K key) {
            return this.getNearKey(key, 0);
        }

        @Override
        public K firstKey() {
            return this.isDescending ? this.highestKey() : this.lowestKey();
        }

        @Override
        public K lastKey() {
            return this.isDescending ? this.lowestKey() : this.highestKey();
        }

        @Override
        public Map.Entry<K, V> firstEntry() {
            return this.isDescending ? this.highestEntry() : this.lowestEntry();
        }

        @Override
        public Map.Entry<K, V> lastEntry() {
            return this.isDescending ? this.lowestEntry() : this.highestEntry();
        }

        @Override
        public Map.Entry<K, V> pollFirstEntry() {
            return this.isDescending ? this.removeHighest() : this.removeLowest();
        }

        @Override
        public Map.Entry<K, V> pollLastEntry() {
            return this.isDescending ? this.removeLowest() : this.removeHighest();
        }

        @Override
        public NavigableSet<K> keySet() {
            KeySet<K, V> ks = this.keySetView;
            if (ks != null) {
                return ks;
            }
            this.keySetView = new KeySet(this);
            return this.keySetView;
        }

        @Override
        public NavigableSet<K> navigableKeySet() {
            KeySet<K, V> ks = this.keySetView;
            if (ks != null) {
                return ks;
            }
            this.keySetView = new KeySet(this);
            return this.keySetView;
        }

        @Override
        public Collection<V> values() {
            Values<K, V> vs = this.valuesView;
            if (vs != null) {
                return vs;
            }
            this.valuesView = new Values(this);
            return this.valuesView;
        }

        @Override
        public Set<Map.Entry<K, V>> entrySet() {
            EntrySet<K, V> es = this.entrySetView;
            if (es != null) {
                return es;
            }
            this.entrySetView = new EntrySet(this);
            return this.entrySetView;
        }

        @Override
        public NavigableSet<K> descendingKeySet() {
            return ((SubMap)this.descendingMap()).navigableKeySet();
        }

        final class SubMapEntryIterator
        extends SubMapIter<Map.Entry<K, V>> {
            SubMapEntryIterator(SubMap this$0) {
            }

            @Override
            public Map.Entry<K, V> next() {
                Node n = this.next;
                Object v = this.nextValue;
                this.advance();
                return new AbstractMap.SimpleImmutableEntry(n.key, v);
            }

            @Override
            public int characteristics() {
                return 1;
            }
        }

        final class SubMapKeyIterator
        extends SubMapIter<K> {
            SubMapKeyIterator() {
            }

            @Override
            public K next() {
                Node n = this.next;
                this.advance();
                return n.key;
            }

            @Override
            public int characteristics() {
                return 21;
            }

            @Override
            public final Comparator<? super K> getComparator() {
                return SubMap.this.comparator();
            }
        }

        final class SubMapValueIterator
        extends SubMapIter<V> {
            SubMapValueIterator(SubMap this$0) {
            }

            @Override
            public V next() {
                Object v = this.nextValue;
                this.advance();
                return v;
            }

            @Override
            public int characteristics() {
                return 0;
            }
        }

        abstract class SubMapIter<T>
        implements Iterator<T>,
        Spliterator<T> {
            Node<K, V> lastReturned;
            Node<K, V> next;
            V nextValue;

            SubMapIter() {
                block3: {
                    Object x;
                    VarHandle.acquireFence();
                    Comparator cmp = SubMap.this.m.comparator;
                    do {
                        Node node = this.next = SubMap.this.isDescending ? SubMap.this.hiNode(cmp) : SubMap.this.loNode(cmp);
                        if (this.next == null) break block3;
                    } while ((x = this.next.val) == null);
                    if (!SubMap.this.inBounds(this.next.key, cmp)) {
                        this.next = null;
                    } else {
                        this.nextValue = x;
                    }
                }
            }

            @Override
            public final boolean hasNext() {
                return this.next != null;
            }

            final void advance() {
                if (this.next == null) {
                    throw new NoSuchElementException();
                }
                this.lastReturned = this.next;
                if (SubMap.this.isDescending) {
                    this.descend();
                } else {
                    this.ascend();
                }
            }

            private void ascend() {
                block3: {
                    Object x;
                    Comparator cmp = SubMap.this.m.comparator;
                    do {
                        this.next = this.next.next;
                        if (this.next == null) break block3;
                    } while ((x = this.next.val) == null);
                    if (SubMap.this.tooHigh(this.next.key, cmp)) {
                        this.next = null;
                    } else {
                        this.nextValue = x;
                    }
                }
            }

            private void descend() {
                block3: {
                    Object x;
                    Comparator cmp = SubMap.this.m.comparator;
                    do {
                        this.next = SubMap.this.m.findNear(this.lastReturned.key, 2, cmp);
                        if (this.next == null) break block3;
                    } while ((x = this.next.val) == null);
                    if (SubMap.this.tooLow(this.next.key, cmp)) {
                        this.next = null;
                    } else {
                        this.nextValue = x;
                    }
                }
            }

            @Override
            public void remove() {
                Node l = this.lastReturned;
                if (l == null) {
                    throw new IllegalStateException();
                }
                SubMap.this.m.remove(l.key);
                this.lastReturned = null;
            }

            @Override
            public Spliterator<T> trySplit() {
                return null;
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                if (this.hasNext()) {
                    action.accept(this.next());
                    return true;
                }
                return false;
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                while (this.hasNext()) {
                    action.accept(this.next());
                }
            }

            @Override
            public long estimateSize() {
                return Long.MAX_VALUE;
            }
        }
    }

    static final class KeySpliterator<K, V>
    extends CSLMSpliterator<K, V>
    implements Spliterator<K> {
        KeySpliterator(Comparator<? super K> comparator, Index<K, V> row, Node<K, V> origin, K fence, long est) {
            super(comparator, row, origin, fence, est);
        }

        public KeySpliterator<K, V> trySplit() {
            Object ek;
            Comparator cmp = this.comparator;
            Object f = this.fence;
            Node e = this.current;
            if (e != null && (ek = e.key) != null) {
                Index q = this.row;
                while (q != null) {
                    Object sk;
                    Node n;
                    Node b;
                    Index s = q.right;
                    if (s != null && (b = s.node) != null && (n = b.next) != null && n.val != null && (sk = n.key) != null && ConcurrentSkipListMap.cpr(cmp, sk, ek) > 0 && (f == null || ConcurrentSkipListMap.cpr(cmp, sk, f) < 0)) {
                        this.current = n;
                        Index r = q.down;
                        this.row = s.right != null ? s : s.down;
                        this.est -= this.est >>> 2;
                        return new KeySpliterator(cmp, r, e, sk, this.est);
                    }
                    q = this.row = q.down;
                }
            }
            return null;
        }

        @Override
        public void forEachRemaining(Consumer<? super K> action) {
            Object k;
            if (action == null) {
                throw new NullPointerException();
            }
            Comparator cmp = this.comparator;
            Object f = this.fence;
            Node e = this.current;
            this.current = null;
            while (e != null && ((k = e.key) == null || f == null || ConcurrentSkipListMap.cpr(cmp, f, k) > 0)) {
                if (e.val != null) {
                    action.accept(k);
                }
                e = e.next;
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Comparator cmp = this.comparator;
            Object f = this.fence;
            Node e = this.current;
            while (e != null) {
                Object k = e.key;
                if (k != null && f != null && ConcurrentSkipListMap.cpr(cmp, f, k) <= 0) {
                    e = null;
                    break;
                }
                if (e.val != null) {
                    this.current = e.next;
                    action.accept(k);
                    return true;
                }
                e = e.next;
            }
            this.current = e;
            return false;
        }

        @Override
        public int characteristics() {
            return 4373;
        }

        @Override
        public final Comparator<? super K> getComparator() {
            return this.comparator;
        }
    }

    static final class ValueSpliterator<K, V>
    extends CSLMSpliterator<K, V>
    implements Spliterator<V> {
        ValueSpliterator(Comparator<? super K> comparator, Index<K, V> row, Node<K, V> origin, K fence, long est) {
            super(comparator, row, origin, fence, est);
        }

        public ValueSpliterator<K, V> trySplit() {
            Object ek;
            Comparator cmp = this.comparator;
            Object f = this.fence;
            Node e = this.current;
            if (e != null && (ek = e.key) != null) {
                Index q = this.row;
                while (q != null) {
                    Object sk;
                    Node n;
                    Node b;
                    Index s = q.right;
                    if (s != null && (b = s.node) != null && (n = b.next) != null && n.val != null && (sk = n.key) != null && ConcurrentSkipListMap.cpr(cmp, sk, ek) > 0 && (f == null || ConcurrentSkipListMap.cpr(cmp, sk, f) < 0)) {
                        this.current = n;
                        Index r = q.down;
                        this.row = s.right != null ? s : s.down;
                        this.est -= this.est >>> 2;
                        return new ValueSpliterator(cmp, r, e, sk, this.est);
                    }
                    q = this.row = q.down;
                }
            }
            return null;
        }

        @Override
        public void forEachRemaining(Consumer<? super V> action) {
            Object k;
            if (action == null) {
                throw new NullPointerException();
            }
            Comparator cmp = this.comparator;
            Object f = this.fence;
            Node e = this.current;
            this.current = null;
            while (e != null && ((k = e.key) == null || f == null || ConcurrentSkipListMap.cpr(cmp, f, k) > 0)) {
                Object v = e.val;
                if (v != null) {
                    action.accept(v);
                }
                e = e.next;
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super V> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Comparator cmp = this.comparator;
            Object f = this.fence;
            Node e = this.current;
            while (e != null) {
                Object k = e.key;
                if (k != null && f != null && ConcurrentSkipListMap.cpr(cmp, f, k) <= 0) {
                    e = null;
                    break;
                }
                Object v = e.val;
                if (v != null) {
                    this.current = e.next;
                    action.accept(v);
                    return true;
                }
                e = e.next;
            }
            this.current = e;
            return false;
        }

        @Override
        public int characteristics() {
            return 4368;
        }
    }

    static final class EntrySpliterator<K, V>
    extends CSLMSpliterator<K, V>
    implements Spliterator<Map.Entry<K, V>> {
        EntrySpliterator(Comparator<? super K> comparator, Index<K, V> row, Node<K, V> origin, K fence, long est) {
            super(comparator, row, origin, fence, est);
        }

        public EntrySpliterator<K, V> trySplit() {
            Object ek;
            Comparator cmp = this.comparator;
            Object f = this.fence;
            Node e = this.current;
            if (e != null && (ek = e.key) != null) {
                Index q = this.row;
                while (q != null) {
                    Object sk;
                    Node n;
                    Node b;
                    Index s = q.right;
                    if (s != null && (b = s.node) != null && (n = b.next) != null && n.val != null && (sk = n.key) != null && ConcurrentSkipListMap.cpr(cmp, sk, ek) > 0 && (f == null || ConcurrentSkipListMap.cpr(cmp, sk, f) < 0)) {
                        this.current = n;
                        Index r = q.down;
                        this.row = s.right != null ? s : s.down;
                        this.est -= this.est >>> 2;
                        return new EntrySpliterator(cmp, r, e, sk, this.est);
                    }
                    q = this.row = q.down;
                }
            }
            return null;
        }

        @Override
        public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
            Object k;
            if (action == null) {
                throw new NullPointerException();
            }
            Comparator cmp = this.comparator;
            Object f = this.fence;
            Node e = this.current;
            this.current = null;
            while (e != null && ((k = e.key) == null || f == null || ConcurrentSkipListMap.cpr(cmp, f, k) > 0)) {
                Object v = e.val;
                if (v != null) {
                    action.accept(new AbstractMap.SimpleImmutableEntry(k, v));
                }
                e = e.next;
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Comparator cmp = this.comparator;
            Object f = this.fence;
            Node e = this.current;
            while (e != null) {
                Object k = e.key;
                if (k != null && f != null && ConcurrentSkipListMap.cpr(cmp, f, k) <= 0) {
                    e = null;
                    break;
                }
                Object v = e.val;
                if (v != null) {
                    this.current = e.next;
                    action.accept(new AbstractMap.SimpleImmutableEntry(k, v));
                    return true;
                }
                e = e.next;
            }
            this.current = e;
            return false;
        }

        @Override
        public int characteristics() {
            return 4373;
        }

        @Override
        public final Comparator<Map.Entry<K, V>> getComparator() {
            if (this.comparator != null) {
                return Map.Entry.comparingByKey(this.comparator);
            }
            return (Comparator & Serializable)(e1, e2) -> {
                Comparable k1 = (Comparable)e1.getKey();
                return k1.compareTo(e2.getKey());
            };
        }
    }

    static abstract class CSLMSpliterator<K, V> {
        final Comparator<? super K> comparator;
        final K fence;
        Index<K, V> row;
        Node<K, V> current;
        long est;

        CSLMSpliterator(Comparator<? super K> comparator, Index<K, V> row, Node<K, V> origin, K fence, long est) {
            this.comparator = comparator;
            this.row = row;
            this.current = origin;
            this.fence = fence;
            this.est = est;
        }

        public final long estimateSize() {
            return this.est;
        }
    }

    final class EntryIterator
    extends Iter<Map.Entry<K, V>> {
        EntryIterator(ConcurrentSkipListMap this$0) {
        }

        @Override
        public Map.Entry<K, V> next() {
            Node n = this.next;
            if (n == null) {
                throw new NoSuchElementException();
            }
            Object k = n.key;
            Object v = this.nextValue;
            this.advance(n);
            return new AbstractMap.SimpleImmutableEntry(k, v);
        }
    }

    final class KeyIterator
    extends Iter<K> {
        KeyIterator(ConcurrentSkipListMap this$0) {
        }

        @Override
        public K next() {
            Node n = this.next;
            if (n == null) {
                throw new NoSuchElementException();
            }
            Object k = n.key;
            this.advance(n);
            return k;
        }
    }

    final class ValueIterator
    extends Iter<V> {
        ValueIterator(ConcurrentSkipListMap this$0) {
        }

        @Override
        public V next() {
            Object v = this.nextValue;
            if (v == null) {
                throw new NoSuchElementException();
            }
            this.advance(this.next);
            return v;
        }
    }

    abstract class Iter<T>
    implements Iterator<T> {
        Node<K, V> lastReturned;
        Node<K, V> next;
        V nextValue;

        Iter() {
            this.advance(ConcurrentSkipListMap.this.baseHead());
        }

        @Override
        public final boolean hasNext() {
            return this.next != null;
        }

        final void advance(Node<K, V> b) {
            Node n = null;
            Object v = null;
            this.lastReturned = b;
            if (this.lastReturned != null) {
                while ((n = b.next) != null) {
                    Object v2 = n.val;
                    v = v2;
                    if (v2 != null) break;
                    b = n;
                }
            }
            this.nextValue = v;
            this.next = n;
        }

        @Override
        public final void remove() {
            Object k;
            Node n = this.lastReturned;
            if (n == null || (k = n.key) == null) {
                throw new IllegalStateException();
            }
            ConcurrentSkipListMap.this.remove(k);
            this.lastReturned = null;
        }
    }
}

