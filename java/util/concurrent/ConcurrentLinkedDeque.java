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
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.Helpers;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ConcurrentLinkedDeque<E>
extends AbstractCollection<E>
implements Deque<E>,
Serializable {
    private static final long serialVersionUID = 876323262645176354L;
    private volatile transient Node<E> head;
    private volatile transient Node<E> tail;
    private static final Node<Object> PREV_TERMINATOR = new Node();
    private static final Node<Object> NEXT_TERMINATOR;
    private static final int HOPS = 2;
    private static final VarHandle HEAD;
    private static final VarHandle TAIL;
    private static final VarHandle PREV;
    private static final VarHandle NEXT;
    private static final VarHandle ITEM;

    Node<E> prevTerminator() {
        return PREV_TERMINATOR;
    }

    Node<E> nextTerminator() {
        return NEXT_TERMINATOR;
    }

    static <E> Node<E> newNode(E item) {
        Node node = new Node();
        ITEM.set(node, item);
        return node;
    }

    private void linkFirst(E e) {
        Node<E> p;
        Node<E> h;
        Node<E> newNode = ConcurrentLinkedDeque.newNode(Objects.requireNonNull(e));
        block0: while (true) {
            p = h = this.head;
            while (true) {
                Node q;
                if ((q = p.prev) != null) {
                    p = q;
                    q = p.prev;
                    if (q != null) {
                        p = h != (h = this.head) ? h : q;
                        continue;
                    }
                }
                if (p.next == p) continue block0;
                NEXT.set(newNode, p);
                if (PREV.compareAndSet(p, null, newNode)) break block0;
            }
            break;
        }
        if (p != h) {
            HEAD.weakCompareAndSet(this, h, newNode);
        }
    }

    private void linkLast(E e) {
        Node<E> p;
        Node<E> t;
        Node<E> newNode = ConcurrentLinkedDeque.newNode(Objects.requireNonNull(e));
        block0: while (true) {
            p = t = this.tail;
            while (true) {
                Node q;
                if ((q = p.next) != null) {
                    p = q;
                    q = p.next;
                    if (q != null) {
                        p = t != (t = this.tail) ? t : q;
                        continue;
                    }
                }
                if (p.prev == p) continue block0;
                PREV.set(newNode, p);
                if (NEXT.compareAndSet(p, null, newNode)) break block0;
            }
            break;
        }
        if (p != t) {
            TAIL.weakCompareAndSet(this, t, newNode);
        }
    }

    void unlink(Node<E> x) {
        Node prev = x.prev;
        Node next = x.next;
        if (prev == null) {
            this.unlinkFirst(x, next);
        } else if (next == null) {
            this.unlinkLast(x, prev);
        } else {
            boolean isLast;
            Node activeSucc;
            Node q;
            boolean isFirst;
            Node activePred;
            int hops = 1;
            Node p = prev;
            while (true) {
                if (p.item != null) {
                    activePred = p;
                    isFirst = false;
                    break;
                }
                q = p.prev;
                if (q == null) {
                    if (p.next == p) {
                        return;
                    }
                    activePred = p;
                    isFirst = true;
                    break;
                }
                if (p == q) {
                    return;
                }
                p = q;
                ++hops;
            }
            p = next;
            while (true) {
                if (p.item != null) {
                    activeSucc = p;
                    isLast = false;
                    break;
                }
                q = p.next;
                if (q == null) {
                    if (p.prev == p) {
                        return;
                    }
                    activeSucc = p;
                    isLast = true;
                    break;
                }
                if (p == q) {
                    return;
                }
                p = q;
                ++hops;
            }
            if (hops < 2 && isFirst | isLast) {
                return;
            }
            this.skipDeletedSuccessors(activePred);
            this.skipDeletedPredecessors(activeSucc);
            if (isFirst | isLast && activePred.next == activeSucc && activeSucc.prev == activePred && (isFirst ? activePred.prev == null : activePred.item != null) && (isLast ? activeSucc.next == null : activeSucc.item != null)) {
                this.updateHead();
                this.updateTail();
                PREV.setRelease(x, isFirst ? this.prevTerminator() : x);
                NEXT.setRelease(x, isLast ? this.nextTerminator() : x);
            }
        }
    }

    private void unlinkFirst(Node<E> first, Node<E> next) {
        Node<E> o = null;
        Node<E> p = next;
        while (true) {
            Node q;
            if (p.item != null || (q = p.next) == null) {
                if (o != null && p.prev != p && NEXT.compareAndSet(first, next, p)) {
                    this.skipDeletedPredecessors(p);
                    if (first.prev == null && (p.next == null || p.item != null) && p.prev == first) {
                        this.updateHead();
                        this.updateTail();
                        NEXT.setRelease(o, o);
                        PREV.setRelease(o, this.prevTerminator());
                    }
                }
                return;
            }
            if (p == q) {
                return;
            }
            o = p;
            p = q;
        }
    }

    private void unlinkLast(Node<E> last, Node<E> prev) {
        Node<E> o = null;
        Node<E> p = prev;
        while (true) {
            Node q;
            if (p.item != null || (q = p.prev) == null) {
                if (o != null && p.next != p && PREV.compareAndSet(last, prev, p)) {
                    this.skipDeletedSuccessors(p);
                    if (last.next == null && (p.prev == null || p.item != null) && p.next == last) {
                        this.updateHead();
                        this.updateTail();
                        PREV.setRelease(o, o);
                        NEXT.setRelease(o, this.nextTerminator());
                    }
                }
                return;
            }
            if (p == q) {
                return;
            }
            o = p;
            p = q;
        }
    }

    /*
     * Unable to fully structure code
     */
    private final void updateHead() {
        block0: while (true) {
            h = this.head;
            if (h.item != null || (p = h.prev) == null) break;
            while (true) {
                block5: {
                    block4: {
                        if ((q = p.prev) == null) break block4;
                        p = q;
                        q = p.prev;
                        if (q != null) break block5;
                    }
                    if (!ConcurrentLinkedDeque.HEAD.compareAndSet(this, h, p)) continue block0;
                    return;
                }
                if (h == this.head) ** break;
                continue block0;
                p = q;
            }
            break;
        }
    }

    /*
     * Unable to fully structure code
     */
    private final void updateTail() {
        block0: while (true) {
            t = this.tail;
            if (t.item != null || (p = t.next) == null) break;
            while (true) {
                block5: {
                    block4: {
                        if ((q = p.next) == null) break block4;
                        p = q;
                        q = p.next;
                        if (q != null) break block5;
                    }
                    if (!ConcurrentLinkedDeque.TAIL.compareAndSet(this, t, p)) continue block0;
                    return;
                }
                if (t == this.tail) ** break;
                continue block0;
                p = q;
            }
            break;
        }
    }

    private void skipDeletedPredecessors(Node<E> x) {
        block0: do {
            Node prev;
            Node p = prev = x.prev;
            while (p.item == null) {
                Node q = p.prev;
                if (q == null) {
                    if (p.next != p) break;
                    continue block0;
                }
                if (p == q) continue block0;
                p = q;
            }
            if (prev != p && !PREV.compareAndSet(x, prev, p)) continue;
            return;
        } while (x.item != null || x.next == null);
    }

    private void skipDeletedSuccessors(Node<E> x) {
        block0: do {
            Node next;
            Node p = next = x.next;
            while (p.item == null) {
                Node q = p.next;
                if (q == null) {
                    if (p.prev != p) break;
                    continue block0;
                }
                if (p == q) continue block0;
                p = q;
            }
            if (next != p && !NEXT.compareAndSet(x, next, p)) continue;
            return;
        } while (x.item != null || x.prev == null);
    }

    final Node<E> succ(Node<E> p) {
        if (p == (p = p.next)) {
            p = this.first();
        }
        return p;
    }

    final Node<E> pred(Node<E> p) {
        if (p == (p = p.prev)) {
            p = this.last();
        }
        return p;
    }

    Node<E> first() {
        Node<E> h;
        Node<E> p;
        block0: do {
            Node q;
            p = h = this.head;
            while ((q = p.prev) != null) {
                p = q;
                q = p.prev;
                if (q == null) continue block0;
                p = h != (h = this.head) ? h : q;
            }
        } while (p != h && !HEAD.compareAndSet(this, h, p));
        return p;
    }

    Node<E> last() {
        Node<E> t;
        Node<E> p;
        block0: do {
            Node q;
            p = t = this.tail;
            while ((q = p.next) != null) {
                p = q;
                q = p.next;
                if (q == null) continue block0;
                p = t != (t = this.tail) ? t : q;
            }
        } while (p != t && !TAIL.compareAndSet(this, t, p));
        return p;
    }

    private E screenNullResult(E v) {
        if (v == null) {
            throw new NoSuchElementException();
        }
        return v;
    }

    public ConcurrentLinkedDeque() {
        this.tail = new Node();
        this.head = this.tail;
    }

    public ConcurrentLinkedDeque(Collection<? extends E> c) {
        Node<E> h = null;
        Node<E> t = null;
        for (E e : c) {
            Node<E> newNode = ConcurrentLinkedDeque.newNode(Objects.requireNonNull(e));
            if (h == null) {
                h = t = newNode;
                continue;
            }
            NEXT.set(t, newNode);
            PREV.set(newNode, t);
            t = newNode;
        }
        this.initHeadTail(h, t);
    }

    private void initHeadTail(Node<E> h, Node<E> t) {
        if (h == t) {
            if (h == null) {
                t = new Node();
                h = t;
            } else {
                Node newNode = new Node();
                NEXT.set(t, newNode);
                PREV.set(newNode, t);
                t = newNode;
            }
        }
        this.head = h;
        this.tail = t;
    }

    @Override
    public void addFirst(E e) {
        this.linkFirst(e);
    }

    @Override
    public void addLast(E e) {
        this.linkLast(e);
    }

    @Override
    public boolean offerFirst(E e) {
        this.linkFirst(e);
        return true;
    }

    @Override
    public boolean offerLast(E e) {
        this.linkLast(e);
        return true;
    }

    @Override
    public E peekFirst() {
        Object item;
        block0: while (true) {
            Node<E> first;
            Node<E> p = first = this.first();
            while ((item = p.item) == null) {
                if (p == (p = p.next)) continue block0;
                if (p != null) continue;
            }
            if (first.prev == null) break;
        }
        return item;
    }

    @Override
    public E peekLast() {
        Object item;
        block0: while (true) {
            Node<E> last;
            Node<E> p = last = this.last();
            while ((item = p.item) == null) {
                if (p == (p = p.prev)) continue block0;
                if (p != null) continue;
            }
            if (last.next == null) break;
        }
        return item;
    }

    @Override
    public E getFirst() {
        return this.screenNullResult(this.peekFirst());
    }

    @Override
    public E getLast() {
        return this.screenNullResult(this.peekLast());
    }

    @Override
    public E pollFirst() {
        block0: while (true) {
            Node<E> first;
            Node<E> p = first = this.first();
            do {
                Object item;
                if ((item = p.item) != null) {
                    if (first.prev != null) continue block0;
                    if (ITEM.compareAndSet(p, item, null)) {
                        this.unlink(p);
                        return item;
                    }
                }
                if (p == (p = p.next)) continue block0;
            } while (p != null);
            if (first.prev == null) break;
        }
        return null;
    }

    @Override
    public E pollLast() {
        block0: while (true) {
            Node<E> last;
            Node<E> p = last = this.last();
            do {
                Object item;
                if ((item = p.item) != null) {
                    if (last.next != null) continue block0;
                    if (ITEM.compareAndSet(p, item, null)) {
                        this.unlink(p);
                        return item;
                    }
                }
                if (p == (p = p.prev)) continue block0;
            } while (p != null);
            if (last.next == null) break;
        }
        return null;
    }

    @Override
    public E removeFirst() {
        return this.screenNullResult(this.pollFirst());
    }

    @Override
    public E removeLast() {
        return this.screenNullResult(this.pollLast());
    }

    @Override
    public boolean offer(E e) {
        return this.offerLast(e);
    }

    @Override
    public boolean add(E e) {
        return this.offerLast(e);
    }

    @Override
    public E poll() {
        return this.pollFirst();
    }

    @Override
    public E peek() {
        return this.peekFirst();
    }

    @Override
    public E remove() {
        return this.removeFirst();
    }

    @Override
    public E pop() {
        return this.removeFirst();
    }

    @Override
    public E element() {
        return this.getFirst();
    }

    @Override
    public void push(E e) {
        this.addFirst(e);
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        Objects.requireNonNull(o);
        Node<E> p = this.first();
        while (p != null) {
            Object item = p.item;
            if (item != null && o.equals(item) && ITEM.compareAndSet(p, item, null)) {
                this.unlink(p);
                return true;
            }
            p = this.succ(p);
        }
        return false;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        Objects.requireNonNull(o);
        Node<E> p = this.last();
        while (p != null) {
            Object item = p.item;
            if (item != null && o.equals(item) && ITEM.compareAndSet(p, item, null)) {
                this.unlink(p);
                return true;
            }
            p = this.pred(p);
        }
        return false;
    }

    @Override
    public boolean contains(Object o) {
        if (o != null) {
            Node<E> p = this.first();
            while (p != null) {
                Object item = p.item;
                if (item != null && o.equals(item)) {
                    return true;
                }
                p = this.succ(p);
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.peekFirst() == null;
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
    public boolean remove(Object o) {
        return this.removeFirstOccurrence(o);
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
            Node<E> newNode = ConcurrentLinkedDeque.newNode(Objects.requireNonNull(e));
            if (beginningOfTheEnd == null) {
                beginningOfTheEnd = last = newNode;
                continue;
            }
            NEXT.set(last, newNode);
            PREV.set(newNode, last);
            last = newNode;
        }
        if (beginningOfTheEnd == null) {
            return false;
        }
        block1: while (true) {
            Node<E> p = t = this.tail;
            while (true) {
                Node q;
                if ((q = p.next) != null) {
                    p = q;
                    q = p.next;
                    if (q != null) {
                        p = t != (t = this.tail) ? t : q;
                        continue;
                    }
                }
                if (p.prev == p) continue block1;
                PREV.set(beginningOfTheEnd, p);
                if (NEXT.compareAndSet(p, null, beginningOfTheEnd)) break block1;
            }
            break;
        }
        if (!TAIL.weakCompareAndSet(this, t, last)) {
            t = this.tail;
            if (last.next == null) {
                TAIL.weakCompareAndSet(this, t, last);
            }
        }
        return true;
    }

    @Override
    public void clear() {
        while (this.pollFirst() != null) {
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
        if (a == null) {
            throw new NullPointerException();
        }
        return this.toArrayInternal(a);
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new DescendingItr();
    }

    @Override
    public Spliterator<E> spliterator() {
        return new CLDSpliterator();
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
            Node<Object> newNode = ConcurrentLinkedDeque.newNode(item);
            if (h == null) {
                h = t = newNode;
                continue;
            }
            NEXT.set(t, newNode);
            PREV.set(newNode, t);
            t = newNode;
        }
        this.initHeadTail(h, t);
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

    private boolean bulkRemove(Predicate<? super E> filter) {
        boolean removed = false;
        Node<E> p = this.first();
        while (p != null) {
            Node<E> succ = this.succ(p);
            Object item = p.item;
            if (item != null && filter.test(item) && ITEM.compareAndSet(p, item, null)) {
                this.unlink(p);
                removed = true;
            }
            p = succ;
        }
        return removed;
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        Node<E> p = this.first();
        while (p != null) {
            Object item = p.item;
            if (item != null) {
                action.accept(item);
            }
            p = this.succ(p);
        }
    }

    static {
        ConcurrentLinkedDeque.PREV_TERMINATOR.next = PREV_TERMINATOR;
        NEXT_TERMINATOR = new Node();
        ConcurrentLinkedDeque.NEXT_TERMINATOR.prev = NEXT_TERMINATOR;
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            HEAD = l.findVarHandle(ConcurrentLinkedDeque.class, "head", Node.class);
            TAIL = l.findVarHandle(ConcurrentLinkedDeque.class, "tail", Node.class);
            PREV = l.findVarHandle(Node.class, "prev", Node.class);
            NEXT = l.findVarHandle(Node.class, "next", Node.class);
            ITEM = l.findVarHandle(Node.class, "item", Object.class);
        }
        catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    static final class Node<E> {
        volatile Node<E> prev;
        volatile E item;
        volatile Node<E> next;

        Node() {
        }
    }

    private class Itr
    extends AbstractItr {
        Itr() {
        }

        @Override
        Node<E> startNode() {
            return ConcurrentLinkedDeque.this.first();
        }

        @Override
        Node<E> nextNode(Node<E> p) {
            return ConcurrentLinkedDeque.this.succ(p);
        }
    }

    private class DescendingItr
    extends AbstractItr {
        DescendingItr() {
        }

        @Override
        Node<E> startNode() {
            return ConcurrentLinkedDeque.this.last();
        }

        @Override
        Node<E> nextNode(Node<E> p) {
            return ConcurrentLinkedDeque.this.pred(p);
        }
    }

    final class CLDSpliterator
    implements Spliterator<E> {
        static final int MAX_BATCH = 0x2000000;
        Node<E> current;
        int batch;
        boolean exhausted;

        CLDSpliterator() {
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
                p = ConcurrentLinkedDeque.this.first();
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
                do {
                    Object e;
                    if ((e = p.item) != null) {
                        action.accept(e);
                    }
                    if (p != (p = p.next)) continue;
                    p = ConcurrentLinkedDeque.this.first();
                } while (p != null);
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
                    p = ConcurrentLinkedDeque.this.first();
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
                p = ConcurrentLinkedDeque.this.first();
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

    private abstract class AbstractItr
    implements Iterator<E> {
        private Node<E> nextNode;
        private E nextItem;
        private Node<E> lastRet;

        abstract Node<E> startNode();

        abstract Node<E> nextNode(Node<E> var1);

        AbstractItr() {
            this.advance();
        }

        private void advance() {
            Node p;
            this.lastRet = this.nextNode;
            Node node = p = this.nextNode == null ? this.startNode() : this.nextNode(this.nextNode);
            while (true) {
                if (p == null) {
                    this.nextNode = null;
                    this.nextItem = null;
                    break;
                }
                Object item = p.item;
                if (item != null) {
                    this.nextNode = p;
                    this.nextItem = item;
                    break;
                }
                p = this.nextNode(p);
            }
        }

        @Override
        public boolean hasNext() {
            return this.nextItem != null;
        }

        @Override
        public E next() {
            Object item = this.nextItem;
            if (item == null) {
                throw new NoSuchElementException();
            }
            this.advance();
            return item;
        }

        @Override
        public void remove() {
            Node l = this.lastRet;
            if (l == null) {
                throw new IllegalStateException();
            }
            l.item = null;
            ConcurrentLinkedDeque.this.unlink(l);
            this.lastRet = null;
        }
    }
}

