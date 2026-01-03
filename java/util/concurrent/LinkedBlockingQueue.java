/*
 * Decompiled with CFR 0.152.
 */
package java.util.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Helpers;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LinkedBlockingQueue<E>
extends AbstractQueue<E>
implements BlockingQueue<E>,
Serializable {
    private static final long serialVersionUID = -6903933977591709194L;
    private final int capacity;
    private final AtomicInteger count = new AtomicInteger();
    transient Node<E> head;
    private transient Node<E> last;
    private final ReentrantLock takeLock = new ReentrantLock();
    private final Condition notEmpty = this.takeLock.newCondition();
    private final ReentrantLock putLock = new ReentrantLock();
    private final Condition notFull = this.putLock.newCondition();

    private void signalNotEmpty() {
        ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            this.notEmpty.signal();
        }
        finally {
            takeLock.unlock();
        }
    }

    private void signalNotFull() {
        ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            this.notFull.signal();
        }
        finally {
            putLock.unlock();
        }
    }

    private void enqueue(Node<E> node) {
        this.last.next = node;
        this.last = this.last.next;
    }

    private E dequeue() {
        Node<E> h = this.head;
        Node first = h.next;
        h.next = h;
        this.head = first;
        Object x = first.item;
        first.item = null;
        return x;
    }

    void fullyLock() {
        this.putLock.lock();
        this.takeLock.lock();
    }

    void fullyUnlock() {
        this.takeLock.unlock();
        this.putLock.unlock();
    }

    public LinkedBlockingQueue() {
        this(Integer.MAX_VALUE);
    }

    public LinkedBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }
        this.capacity = capacity;
        this.head = new Node<Object>(null);
        this.last = this.head;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public LinkedBlockingQueue(Collection<? extends E> c) {
        this(Integer.MAX_VALUE);
        ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            int n = 0;
            for (E e : c) {
                if (e == null) {
                    throw new NullPointerException();
                }
                if (n == this.capacity) {
                    throw new IllegalStateException("Queue full");
                }
                this.enqueue(new Node<E>(e));
                ++n;
            }
            this.count.set(n);
        }
        finally {
            putLock.unlock();
        }
    }

    @Override
    public int size() {
        return this.count.get();
    }

    @Override
    public int remainingCapacity() {
        return this.capacity - this.count.get();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void put(E e) throws InterruptedException {
        int c;
        if (e == null) {
            throw new NullPointerException();
        }
        Node<E> node = new Node<E>(e);
        ReentrantLock putLock = this.putLock;
        AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        try {
            while (count.get() == this.capacity) {
                this.notFull.await();
            }
            this.enqueue(node);
            c = count.getAndIncrement();
            if (c + 1 < this.capacity) {
                this.notFull.signal();
            }
        }
        finally {
            putLock.unlock();
        }
        if (c == 0) {
            this.signalNotEmpty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        int c;
        if (e == null) {
            throw new NullPointerException();
        }
        long nanos = unit.toNanos(timeout);
        ReentrantLock putLock = this.putLock;
        AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        try {
            while (count.get() == this.capacity) {
                if (nanos <= 0L) {
                    boolean bl = false;
                    return bl;
                }
                nanos = this.notFull.awaitNanos(nanos);
            }
            this.enqueue(new Node<E>(e));
            c = count.getAndIncrement();
            if (c + 1 < this.capacity) {
                this.notFull.signal();
            }
        }
        finally {
            putLock.unlock();
        }
        if (c == 0) {
            this.signalNotEmpty();
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean offer(E e) {
        int c;
        if (e == null) {
            throw new NullPointerException();
        }
        AtomicInteger count = this.count;
        if (count.get() == this.capacity) {
            return false;
        }
        Node<E> node = new Node<E>(e);
        ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            if (count.get() == this.capacity) {
                boolean bl = false;
                return bl;
            }
            this.enqueue(node);
            c = count.getAndIncrement();
            if (c + 1 < this.capacity) {
                this.notFull.signal();
            }
        }
        finally {
            putLock.unlock();
        }
        if (c == 0) {
            this.signalNotEmpty();
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public E take() throws InterruptedException {
        int c;
        E x;
        AtomicInteger count = this.count;
        ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            while (count.get() == 0) {
                this.notEmpty.await();
            }
            x = this.dequeue();
            c = count.getAndDecrement();
            if (c > 1) {
                this.notEmpty.signal();
            }
        }
        finally {
            takeLock.unlock();
        }
        if (c == this.capacity) {
            this.signalNotFull();
        }
        return x;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        int c;
        E x;
        long nanos = unit.toNanos(timeout);
        AtomicInteger count = this.count;
        ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            while (count.get() == 0) {
                if (nanos <= 0L) {
                    E e = null;
                    return e;
                }
                nanos = this.notEmpty.awaitNanos(nanos);
            }
            x = this.dequeue();
            c = count.getAndDecrement();
            if (c > 1) {
                this.notEmpty.signal();
            }
        }
        finally {
            takeLock.unlock();
        }
        if (c == this.capacity) {
            this.signalNotFull();
        }
        return x;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public E poll() {
        int c;
        E x;
        AtomicInteger count = this.count;
        if (count.get() == 0) {
            return null;
        }
        ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            if (count.get() == 0) {
                E e = null;
                return e;
            }
            x = this.dequeue();
            c = count.getAndDecrement();
            if (c > 1) {
                this.notEmpty.signal();
            }
        }
        finally {
            takeLock.unlock();
        }
        if (c == this.capacity) {
            this.signalNotFull();
        }
        return x;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public E peek() {
        AtomicInteger count = this.count;
        if (count.get() == 0) {
            return null;
        }
        ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            E e = count.get() > 0 ? (E)this.head.next.item : null;
            return e;
        }
        finally {
            takeLock.unlock();
        }
    }

    void unlink(Node<E> p, Node<E> pred) {
        p.item = null;
        pred.next = p.next;
        if (this.last == p) {
            this.last = pred;
        }
        if (this.count.getAndDecrement() == this.capacity) {
            this.notFull.signal();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean remove(Object o) {
        if (o == null) {
            return false;
        }
        this.fullyLock();
        try {
            Node<E> pred = this.head;
            Node p = pred.next;
            while (p != null) {
                if (o.equals(p.item)) {
                    this.unlink(p, pred);
                    boolean bl = true;
                    return bl;
                }
                pred = p;
                p = p.next;
            }
            boolean bl = false;
            return bl;
        }
        finally {
            this.fullyUnlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        this.fullyLock();
        try {
            Node p = this.head.next;
            while (p != null) {
                if (o.equals(p.item)) {
                    boolean bl = true;
                    return bl;
                }
                p = p.next;
            }
            boolean bl = false;
            return bl;
        }
        finally {
            this.fullyUnlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Object[] toArray() {
        this.fullyLock();
        try {
            int size = this.count.get();
            Object[] a = new Object[size];
            int k = 0;
            Node p = this.head.next;
            while (p != null) {
                a[k++] = p.item;
                p = p.next;
            }
            Object[] objectArray = a;
            return objectArray;
        }
        finally {
            this.fullyUnlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public <T> T[] toArray(T[] a) {
        this.fullyLock();
        try {
            int size = this.count.get();
            if (a.length < size) {
                a = (Object[])Array.newInstance(a.getClass().getComponentType(), size);
            }
            int k = 0;
            Node p = this.head.next;
            while (p != null) {
                a[k++] = p.item;
                p = p.next;
            }
            if (a.length > k) {
                a[k] = null;
            }
            Object[] objectArray = a;
            return objectArray;
        }
        finally {
            this.fullyUnlock();
        }
    }

    @Override
    public String toString() {
        return Helpers.collectionToString(this);
    }

    @Override
    public void clear() {
        this.fullyLock();
        try {
            Node p;
            Node<E> h = this.head;
            while ((p = h.next) != null) {
                h.next = h;
                p.item = null;
                h = p;
            }
            this.head = this.last;
            if (this.count.getAndSet(0) == this.capacity) {
                this.notFull.signal();
            }
        }
        finally {
            this.fullyUnlock();
        }
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return this.drainTo(c, Integer.MAX_VALUE);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        Objects.requireNonNull(c);
        if (c == this) {
            throw new IllegalArgumentException();
        }
        if (maxElements <= 0) {
            return 0;
        }
        boolean signalNotFull = false;
        ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            int n;
            block11: {
                int i;
                int n2 = Math.min(maxElements, this.count.get());
                Node<E> h = this.head;
                try {
                    for (i = 0; i < n2; ++i) {
                        Node p = h.next;
                        c.add(p.item);
                        p.item = null;
                        h.next = h;
                        h = p;
                    }
                    n = n2;
                    if (i <= 0) break block11;
                    this.head = h;
                    signalNotFull = this.count.getAndAdd(-i) == this.capacity;
                }
                catch (Throwable throwable) {
                    if (i > 0) {
                        this.head = h;
                        signalNotFull = this.count.getAndAdd(-i) == this.capacity;
                    }
                    throw throwable;
                }
            }
            return n;
        }
        finally {
            takeLock.unlock();
            if (signalNotFull) {
                this.signalNotFull();
            }
        }
    }

    Node<E> succ(Node<E> p) {
        if (p == (p = p.next)) {
            p = this.head.next;
        }
        return p;
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    @Override
    public Spliterator<E> spliterator() {
        return new LBQSpliterator();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        this.forEachFrom(action, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void forEachFrom(Consumer<? super E> action, Node<E> p) {
        int n;
        int batchSize = 64;
        Object[] es = null;
        int len = 0;
        do {
            this.fullyLock();
            try {
                if (es == null) {
                    if (p == null) {
                        p = this.head.next;
                    }
                    Node<E> q = p;
                    while (q != null && (q.item == null || ++len != 64)) {
                        q = this.succ(q);
                    }
                    es = new Object[len];
                }
                n = 0;
                while (p != null && n < len) {
                    es[n] = p.item;
                    if (es[n] != null) {
                        ++n;
                    }
                    p = this.succ(p);
                }
            }
            finally {
                this.fullyUnlock();
            }
            for (int i = 0; i < n; ++i) {
                Object e = es[i];
                action.accept(e);
            }
        } while (n > 0 && p != null);
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

    Node<E> findPred(Node<E> p, Node<E> ancestor) {
        Node q;
        if (ancestor.item == null) {
            ancestor = this.head;
        }
        while ((q = ancestor.next) != p) {
            ancestor = q;
        }
        return ancestor;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean bulkRemove(Predicate<? super E> filter) {
        int n;
        boolean removed = false;
        Node p = null;
        Node<E> ancestor = this.head;
        Node[] nodes = null;
        int len = 0;
        do {
            int i;
            this.fullyLock();
            try {
                if (nodes == null) {
                    Node q = p = this.head.next;
                    while (q != null && (q.item == null || ++len != 64)) {
                        q = this.succ(q);
                    }
                    nodes = new Node[len];
                }
                n = 0;
                while (p != null && n < len) {
                    nodes[n++] = p;
                    p = this.succ(p);
                }
            }
            finally {
                this.fullyUnlock();
            }
            long deathRow = 0L;
            for (i = 0; i < n; ++i) {
                Object e = nodes[i].item;
                if (e == null || !filter.test(e)) continue;
                deathRow |= 1L << i;
            }
            if (deathRow == 0L) continue;
            this.fullyLock();
            try {
                for (i = 0; i < n; ++i) {
                    if ((deathRow & 1L << i) != 0L) {
                        Node q = nodes[i];
                        if (q.item != null) {
                            ancestor = this.findPred(q, ancestor);
                            this.unlink(q, ancestor);
                            removed = true;
                        }
                    }
                    nodes[i] = null;
                }
            }
            finally {
                this.fullyUnlock();
            }
        } while (n > 0 && p != null);
        return removed;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        this.fullyLock();
        try {
            s.defaultWriteObject();
            Node p = this.head.next;
            while (p != null) {
                s.writeObject(p.item);
                p = p.next;
            }
            s.writeObject(null);
        }
        finally {
            this.fullyUnlock();
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        Object item;
        s.defaultReadObject();
        this.count.set(0);
        this.head = new Node<Object>(null);
        this.last = this.head;
        while ((item = s.readObject()) != null) {
            this.add(item);
        }
    }

    static class Node<E> {
        E item;
        Node<E> next;

        Node(E x) {
            this.item = x;
        }
    }

    private class Itr
    implements Iterator<E> {
        private Node<E> next;
        private E nextItem;
        private Node<E> lastRet;
        private Node<E> ancestor;

        Itr() {
            LinkedBlockingQueue.this.fullyLock();
            try {
                this.next = LinkedBlockingQueue.this.head.next;
                if (this.next != null) {
                    this.nextItem = this.next.item;
                }
            }
            finally {
                LinkedBlockingQueue.this.fullyUnlock();
            }
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public E next() {
            Node p = this.next;
            if (p == null) {
                throw new NoSuchElementException();
            }
            this.lastRet = p;
            Object x = this.nextItem;
            LinkedBlockingQueue.this.fullyLock();
            try {
                Object e = null;
                p = p.next;
                while (p != null) {
                    Object e2 = p.item;
                    e = e2;
                    if (e2 != null) break;
                    p = LinkedBlockingQueue.this.succ(p);
                }
                this.next = p;
                this.nextItem = e;
            }
            finally {
                LinkedBlockingQueue.this.fullyUnlock();
            }
            return x;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            int n;
            Objects.requireNonNull(action);
            Node p = this.next;
            if (p == null) {
                return;
            }
            this.lastRet = p;
            this.next = null;
            int batchSize = 64;
            Object[] es = null;
            int len = 1;
            do {
                LinkedBlockingQueue.this.fullyLock();
                try {
                    if (es == null) {
                        Node q = p = p.next;
                        while (q != null && (q.item == null || ++len != 64)) {
                            q = LinkedBlockingQueue.this.succ(q);
                        }
                        es = new Object[len];
                        es[0] = this.nextItem;
                        this.nextItem = null;
                        n = 1;
                    } else {
                        n = 0;
                    }
                    while (p != null && n < len) {
                        es[n] = p.item;
                        if (es[n] != null) {
                            this.lastRet = p;
                            ++n;
                        }
                        p = LinkedBlockingQueue.this.succ(p);
                    }
                }
                finally {
                    LinkedBlockingQueue.this.fullyUnlock();
                }
                for (int i = 0; i < n; ++i) {
                    Object e = es[i];
                    action.accept(e);
                }
            } while (n > 0 && p != null);
        }

        @Override
        public void remove() {
            Node p = this.lastRet;
            if (p == null) {
                throw new IllegalStateException();
            }
            this.lastRet = null;
            LinkedBlockingQueue.this.fullyLock();
            try {
                if (p.item != null) {
                    if (this.ancestor == null) {
                        this.ancestor = LinkedBlockingQueue.this.head;
                    }
                    this.ancestor = LinkedBlockingQueue.this.findPred(p, this.ancestor);
                    LinkedBlockingQueue.this.unlink(p, this.ancestor);
                }
            }
            finally {
                LinkedBlockingQueue.this.fullyUnlock();
            }
        }
    }

    private final class LBQSpliterator
    implements Spliterator<E> {
        static final int MAX_BATCH = 0x2000000;
        Node<E> current;
        int batch;
        boolean exhausted;
        long est;

        LBQSpliterator() {
            this.est = LinkedBlockingQueue.this.size();
        }

        @Override
        public long estimateSize() {
            return this.est;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public Spliterator<E> trySplit() {
            Node h;
            if (!(this.exhausted || (h = this.current) == null && (h = LinkedBlockingQueue.this.head.next) == null || h.next == null)) {
                int n = this.batch = Math.min(this.batch + 1, 0x2000000);
                Object[] a = new Object[n];
                int i = 0;
                Node p = this.current;
                LinkedBlockingQueue.this.fullyLock();
                try {
                    if (p != null || (p = LinkedBlockingQueue.this.head.next) != null) {
                        while (p != null && i < n) {
                            a[i] = p.item;
                            if (a[i] != null) {
                                ++i;
                            }
                            p = LinkedBlockingQueue.this.succ(p);
                        }
                    }
                }
                finally {
                    LinkedBlockingQueue.this.fullyUnlock();
                }
                this.current = p;
                if (this.current == null) {
                    this.est = 0L;
                    this.exhausted = true;
                } else if ((this.est -= (long)i) < 0L) {
                    this.est = 0L;
                }
                if (i > 0) {
                    return Spliterators.spliterator(a, 0, i, 4368);
                }
            }
            return null;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean tryAdvance(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            if (!this.exhausted) {
                Object e = null;
                LinkedBlockingQueue.this.fullyLock();
                try {
                    Node p = this.current;
                    if (p != null || (p = LinkedBlockingQueue.this.head.next) != null) {
                        do {
                            e = p.item;
                            p = LinkedBlockingQueue.this.succ(p);
                        } while (e == null && p != null);
                    }
                    if ((this.current = p) == null) {
                        this.exhausted = true;
                    }
                }
                finally {
                    LinkedBlockingQueue.this.fullyUnlock();
                }
                if (e != null) {
                    action.accept(e);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            if (!this.exhausted) {
                this.exhausted = true;
                Node p = this.current;
                this.current = null;
                LinkedBlockingQueue.this.forEachFrom(action, p);
            }
        }

        @Override
        public int characteristics() {
            return 4368;
        }
    }
}

