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
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.Helpers;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ConcurrentLinkedQueue<E>
extends AbstractQueue<E>
implements Queue<E>,
Serializable {
    private static final long serialVersionUID = 196745693267521676L;
    volatile transient Node<E> head;
    private volatile transient Node<E> tail;
    private static final int MAX_HOPS = 8;
    private static final VarHandle HEAD;
    private static final VarHandle TAIL;
    static final VarHandle ITEM;
    static final VarHandle NEXT;

    public ConcurrentLinkedQueue() {
        this.tail = new Node();
        this.head = this.tail;
    }

    public ConcurrentLinkedQueue(Collection<? extends E> c) {
        Node<E> h = null;
        Node<E> t = null;
        for (E e : c) {
            Node<E> newNode = new Node<E>(Objects.requireNonNull(e));
            if (h == null) {
                h = t = newNode;
                continue;
            }
            Node<E> node = t;
            t = newNode;
            node.appendRelaxed(t);
        }
        if (h == null) {
            h = t = new Node<E>();
        }
        this.head = h;
        this.tail = t;
    }

    @Override
    public boolean add(E e) {
        return this.offer(e);
    }

    final void updateHead(Node<E> h, Node<E> p) {
        if (h != p && HEAD.compareAndSet(this, h, p)) {
            NEXT.setRelease(h, h);
        }
    }

    final Node<E> succ(Node<E> p) {
        if (p == (p = p.next)) {
            p = this.head;
        }
        return p;
    }

    private boolean tryCasSuccessor(Node<E> pred, Node<E> c, Node<E> p) {
        if (pred != null) {
            return NEXT.compareAndSet(pred, c, p);
        }
        if (HEAD.compareAndSet(this, c, p)) {
            NEXT.setRelease(c, c);
            return true;
        }
        return false;
    }

    private Node<E> skipDeadNodes(Node<E> pred, Node<E> c, Node<E> p, Node<E> q) {
        if (q == null) {
            if (c == p) {
                return pred;
            }
            q = p;
        }
        return this.tryCasSuccessor(pred, c, q) && (pred == null || ITEM.get(pred) != null) ? pred : p;
    }

    @Override
    public boolean offer(E e) {
        Node<E> t;
        Node<E> newNode = new Node<E>(Objects.requireNonNull(e));
        Node<E> p = t = this.tail;
        while (true) {
            Node q;
            if ((q = p.next) == null) {
                if (!NEXT.compareAndSet(p, null, newNode)) continue;
                if (p != t) {
                    TAIL.weakCompareAndSet(this, t, newNode);
                }
                return true;
            }
            if (p == q) {
                p = t != (t = this.tail) ? t : this.head;
                continue;
            }
            p = p != t && t != (t = this.tail) ? t : q;
        }
    }

    @Override
    public E poll() {
        block0: while (true) {
            Node<Object> h;
            Node<Object> p = h = this.head;
            while (true) {
                Node q;
                Object item;
                if ((item = p.item) != null && p.casItem(item, null)) {
                    if (p != h) {
                        q = p.next;
                        this.updateHead(h, q != null ? q : p);
                    }
                    return item;
                }
                q = p.next;
                if (q == null) {
                    this.updateHead(h, p);
                    return null;
                }
                if (p == q) continue block0;
                p = q;
            }
            break;
        }
    }

    @Override
    public E peek() {
        block0: while (true) {
            Node<E> h;
            Node<E> p = h = this.head;
            while (true) {
                Node q;
                Object item;
                if ((item = p.item) != null || (q = p.next) == null) {
                    this.updateHead(h, p);
                    return item;
                }
                if (p == q) continue block0;
                p = q;
            }
            break;
        }
    }

    Node<E> first() {
        block0: while (true) {
            Node<E> h;
            Node<E> p = h = this.head;
            while (true) {
                Node q;
                boolean hasItem;
                boolean bl = hasItem = p.item != null;
                if (hasItem || (q = p.next) == null) {
                    this.updateHead(h, p);
                    return hasItem ? p : null;
                }
                if (p == q) continue block0;
                p = q;
            }
            break;
        }
    }

    @Override
    public boolean isEmpty() {
        return this.first() == null;
    }

    @Override
    public int size() {
        int count;
        block0: while (true) {
            count = 0;
            Node<E> p = this.first();
            while (p != null && (p.item == null || ++count != Integer.MAX_VALUE)) {
                if (p != (p = p.next)) continue;
                continue block0;
            }
            break;
        }
        return count;
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        block0: while (true) {
            Node<E> p = this.head;
            Node<E> pred = null;
            block1: while (p != null) {
                Node q = p.next;
                Object item = p.item;
                if (item != null) {
                    if (o.equals(item)) {
                        return true;
                    }
                    pred = p;
                    p = q;
                    continue;
                }
                Node<E> c = p;
                while (true) {
                    if (q == null || q.item != null) {
                        pred = this.skipDeadNodes(pred, c, p, q);
                        p = q;
                        continue block1;
                    }
                    if (p == (p = q)) continue block0;
                    q = p.next;
                }
            }
            break;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        if (o == null) {
            return false;
        }
        block0: while (true) {
            Node<Object> p = this.head;
            Node<Object> pred = null;
            block1: while (p != null) {
                Node q = p.next;
                Object item = p.item;
                if (item != null) {
                    if (o.equals(item) && p.casItem(item, null)) {
                        this.skipDeadNodes(pred, p, p, q);
                        return true;
                    }
                    pred = p;
                    p = q;
                    continue;
                }
                Node<Object> c = p;
                while (true) {
                    if (q == null || q.item != null) {
                        pred = this.skipDeadNodes(pred, c, p, q);
                        p = q;
                        continue block1;
                    }
                    if (p == (p = q)) continue block0;
                    q = p.next;
                }
            }
            break;
        }
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        Node<E> t;
        if (c == this) {
            throw new IllegalArgumentException();
        }
        Node<E> beginningOfTheEnd = null;
        Node<E> last = null;
        for (E e : c) {
            Node<E> newNode = new Node<E>(Objects.requireNonNull(e));
            if (beginningOfTheEnd == null) {
                beginningOfTheEnd = last = newNode;
                continue;
            }
            Node<E> node = last;
            last = newNode;
            node.appendRelaxed(last);
        }
        if (beginningOfTheEnd == null) {
            return false;
        }
        Node<E> p = t = this.tail;
        while (true) {
            Node q;
            if ((q = p.next) == null) {
                if (!NEXT.compareAndSet(p, null, beginningOfTheEnd)) continue;
                if (!TAIL.weakCompareAndSet(this, t, last)) {
                    t = this.tail;
                    if (last.next == null) {
                        TAIL.weakCompareAndSet(this, t, last);
                    }
                }
                return true;
            }
            if (p == q) {
                p = t != (t = this.tail) ? t : this.head;
                continue;
            }
            p = p != t && t != (t = this.tail) ? t : q;
        }
    }

    @Override
    public String toString() {
        int size;
        int charLength;
        String[] a = null;
        block0: while (true) {
            charLength = 0;
            size = 0;
            Node<E> p = this.first();
            while (p != null) {
                Object item = p.item;
                if (item != null) {
                    if (a == null) {
                        a = new String[4];
                    } else if (size == a.length) {
                        a = Arrays.copyOf(a, 2 * size);
                    }
                    String s = item.toString();
                    a[size++] = s;
                    charLength += s.length();
                }
                if (p != (p = p.next)) continue;
                continue block0;
            }
            break;
        }
        if (size == 0) {
            return "[]";
        }
        return Helpers.toString(a, size, charLength);
    }

    private Object[] toArrayInternal(Object[] a) {
        int size;
        Object[] x = a;
        block0: while (true) {
            size = 0;
            Node<E> p = this.first();
            while (p != null) {
                Object item = p.item;
                if (item != null) {
                    if (x == null) {
                        x = new Object[4];
                    } else if (size == x.length) {
                        x = Arrays.copyOf(x, 2 * (size + 4));
                    }
                    x[size++] = item;
                }
                if (p != (p = p.next)) continue;
                continue block0;
            }
            break;
        }
        if (x == null) {
            return new Object[0];
        }
        if (a != null && size <= a.length) {
            if (a != x) {
                System.arraycopy(x, 0, a, 0, size);
            }
            if (size < a.length) {
                a[size] = null;
            }
            return a;
        }
        return size == x.length ? x : Arrays.copyOf(x, size);
    }

    @Override
    public Object[] toArray() {
        return this.toArrayInternal(null);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        Objects.requireNonNull(a);
        return this.toArrayInternal(a);
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        Node<E> p = this.first();
        while (p != null) {
            Object item = p.item;
            if (item != null) {
                s.writeObject(item);
            }
            p = this.succ(p);
        }
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        Object item;
        s.defaultReadObject();
        Node<Object> h = null;
        Node<Object> t = null;
        while ((item = s.readObject()) != null) {
            Node<Object> newNode = new Node<Object>(item);
            if (h == null) {
                h = t = newNode;
                continue;
            }
            Node<Object> node = t;
            t = newNode;
            node.appendRelaxed(t);
        }
        if (h == null) {
            h = t = new Node<Object>();
        }
        this.head = h;
        this.tail = t;
    }

    @Override
    public Spliterator<E> spliterator() {
        return new CLQSpliterator();
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

    @Override
    public void clear() {
        this.bulkRemove(e -> true);
    }

    private boolean bulkRemove(Predicate<? super E> filter) {
        boolean removed = false;
        block0: while (true) {
            Node<Object> p;
            int hops = 8;
            Node<Object> c = p = this.head;
            Node<Object> pred = null;
            while (p != null) {
                Node q = p.next;
                Object item = p.item;
                boolean pAlive = item != null;
                if (pAlive && filter.test(item)) {
                    if (p.casItem(item, null)) {
                        removed = true;
                    }
                    pAlive = false;
                }
                if (pAlive || q == null || --hops == 0) {
                    if (c != p && !this.tryCasSuccessor(pred, c, c = p) || pAlive) {
                        hops = 8;
                        pred = p;
                        c = q;
                    }
                } else if (p == q) continue block0;
                p = q;
            }
            break;
        }
        return removed;
    }

    void forEachFrom(Consumer<? super E> action, Node<E> p) {
        Node<E> pred = null;
        block0: while (p != null) {
            Node q = p.next;
            Object item = p.item;
            if (item != null) {
                action.accept(item);
                pred = p;
                p = q;
                continue;
            }
            Node<E> c = p;
            while (true) {
                if (q == null || q.item != null) {
                    pred = this.skipDeadNodes(pred, c, p, q);
                    p = q;
                    continue block0;
                }
                if (p == (p = q)) {
                    pred = null;
                    p = this.head;
                    continue block0;
                }
                q = p.next;
            }
        }
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        this.forEachFrom(action, this.head);
    }

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            HEAD = l.findVarHandle(ConcurrentLinkedQueue.class, "head", Node.class);
            TAIL = l.findVarHandle(ConcurrentLinkedQueue.class, "tail", Node.class);
            ITEM = l.findVarHandle(Node.class, "item", Object.class);
            NEXT = l.findVarHandle(Node.class, "next", Node.class);
        }
        catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    static final class Node<E> {
        volatile E item;
        volatile Node<E> next;

        Node(E item) {
            ITEM.set(this, item);
        }

        Node() {
        }

        void appendRelaxed(Node<E> next) {
            NEXT.set(this, next);
        }

        boolean casItem(E cmp, E val) {
            return ITEM.compareAndSet(this, cmp, val);
        }
    }

    private class Itr
    implements Iterator<E> {
        private Node<E> nextNode;
        private E nextItem;
        private Node<E> lastRet;

        Itr() {
            Node p;
            Node h;
            block0: while (true) {
                p = h = ConcurrentLinkedQueue.this.head;
                while (true) {
                    Object item;
                    if ((item = p.item) != null) {
                        this.nextNode = p;
                        this.nextItem = item;
                        break block0;
                    }
                    Node q = p.next;
                    if (q == null) break block0;
                    if (p == q) continue block0;
                    p = q;
                }
                break;
            }
            ConcurrentLinkedQueue.this.updateHead(h, p);
        }

        @Override
        public boolean hasNext() {
            return this.nextItem != null;
        }

        @Override
        public E next() {
            Node pred = this.nextNode;
            if (pred == null) {
                throw new NoSuchElementException();
            }
            this.lastRet = pred;
            Object item = null;
            Node p = ConcurrentLinkedQueue.this.succ(pred);
            while (true) {
                block7: {
                    block6: {
                        if (p == null) break block6;
                        Object e = p.item;
                        item = e;
                        if (e == null) break block7;
                    }
                    this.nextNode = p;
                    Object x = this.nextItem;
                    this.nextItem = item;
                    return x;
                }
                Node q = ConcurrentLinkedQueue.this.succ(p);
                if (q != null) {
                    NEXT.compareAndSet(pred, p, q);
                }
                p = q;
            }
        }

        @Override
        public void remove() {
            Node l = this.lastRet;
            if (l == null) {
                throw new IllegalStateException();
            }
            l.item = null;
            this.lastRet = null;
        }
    }

    final class CLQSpliterator
    implements Spliterator<E> {
        static final int MAX_BATCH = 0x2000000;
        Node<E> current;
        int batch;
        boolean exhausted;

        CLQSpliterator() {
        }

        @Override
        public Spliterator<E> trySplit() {
            Node q;
            Node p = this.current();
            if (p == null || (q = p.next) == null) {
                return null;
            }
            int i = 0;
            int n = this.batch = Math.min(this.batch + 1, 0x2000000);
            Object[] a = null;
            do {
                Object e;
                if ((e = p.item) != null) {
                    if (a == null) {
                        a = new Object[n];
                    }
                    a[i++] = e;
                }
                if (p != (p = q)) continue;
                p = ConcurrentLinkedQueue.this.first();
            } while (p != null && (q = p.next) != null && i < n);
            this.setCurrent(p);
            return i == 0 ? null : Spliterators.spliterator(a, 0, i, 4368);
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            Node p = this.current();
            if (p != null) {
                this.current = null;
                this.exhausted = true;
                ConcurrentLinkedQueue.this.forEachFrom(action, p);
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            Node p = this.current();
            if (p != null) {
                Object e;
                do {
                    e = p.item;
                    if (p != (p = p.next)) continue;
                    p = ConcurrentLinkedQueue.this.first();
                } while (e == null && p != null);
                this.setCurrent(p);
                if (e != null) {
                    action.accept(e);
                    return true;
                }
            }
            return false;
        }

        private void setCurrent(Node<E> p) {
            this.current = p;
            if (this.current == null) {
                this.exhausted = true;
            }
        }

        private Node<E> current() {
            Node p = this.current;
            if (p == null && !this.exhausted) {
                p = ConcurrentLinkedQueue.this.first();
                this.setCurrent(p);
            }
            return p;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return 4368;
        }
    }
}

