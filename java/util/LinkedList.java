/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class LinkedList<E>
extends AbstractSequentialList<E>
implements List<E>,
Deque<E>,
Cloneable,
Serializable {
    transient int size = 0;
    transient Node<E> first;
    transient Node<E> last;
    private static final long serialVersionUID = 876323262645176354L;

    public LinkedList() {
    }

    public LinkedList(Collection<? extends E> c) {
        this();
        this.addAll(c);
    }

    private void linkFirst(E e) {
        Node<E> f = this.first;
        Node<E> newNode = new Node<E>(null, e, f);
        this.first = newNode;
        if (f == null) {
            this.last = newNode;
        } else {
            f.prev = newNode;
        }
        ++this.size;
        ++this.modCount;
    }

    void linkLast(E e) {
        Node<E> l = this.last;
        Node<E> newNode = new Node<E>(l, e, null);
        this.last = newNode;
        if (l == null) {
            this.first = newNode;
        } else {
            l.next = newNode;
        }
        ++this.size;
        ++this.modCount;
    }

    void linkBefore(E e, Node<E> succ) {
        Node pred = succ.prev;
        Node newNode = new Node(pred, e, succ);
        succ.prev = newNode;
        if (pred == null) {
            this.first = newNode;
        } else {
            pred.next = newNode;
        }
        ++this.size;
        ++this.modCount;
    }

    private E unlinkFirst(Node<E> f) {
        Object element = f.item;
        Node next = f.next;
        f.item = null;
        f.next = null;
        this.first = next;
        if (next == null) {
            this.last = null;
        } else {
            next.prev = null;
        }
        --this.size;
        ++this.modCount;
        return element;
    }

    private E unlinkLast(Node<E> l) {
        Object element = l.item;
        Node prev = l.prev;
        l.item = null;
        l.prev = null;
        this.last = prev;
        if (prev == null) {
            this.first = null;
        } else {
            prev.next = null;
        }
        --this.size;
        ++this.modCount;
        return element;
    }

    E unlink(Node<E> x) {
        Object element = x.item;
        Node next = x.next;
        Node prev = x.prev;
        if (prev == null) {
            this.first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }
        if (next == null) {
            this.last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }
        x.item = null;
        --this.size;
        ++this.modCount;
        return element;
    }

    @Override
    public E getFirst() {
        Node<E> f = this.first;
        if (f == null) {
            throw new NoSuchElementException();
        }
        return f.item;
    }

    @Override
    public E getLast() {
        Node<E> l = this.last;
        if (l == null) {
            throw new NoSuchElementException();
        }
        return l.item;
    }

    @Override
    public E removeFirst() {
        Node<E> f = this.first;
        if (f == null) {
            throw new NoSuchElementException();
        }
        return this.unlinkFirst(f);
    }

    @Override
    public E removeLast() {
        Node<E> l = this.last;
        if (l == null) {
            throw new NoSuchElementException();
        }
        return this.unlinkLast(l);
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
    public boolean contains(Object o) {
        return this.indexOf(o) >= 0;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean add(E e) {
        this.linkLast(e);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (o == null) {
            Node<E> x = this.first;
            while (x != null) {
                if (x.item == null) {
                    this.unlink(x);
                    return true;
                }
                x = x.next;
            }
        } else {
            Node<E> x = this.first;
            while (x != null) {
                if (o.equals(x.item)) {
                    this.unlink(x);
                    return true;
                }
                x = x.next;
            }
        }
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return this.addAll(this.size, c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        Node<Object> pred;
        Node<E> succ;
        this.checkPositionIndex(index);
        Object[] a = c.toArray();
        int numNew = a.length;
        if (numNew == 0) {
            return false;
        }
        if (index == this.size) {
            succ = null;
            pred = this.last;
        } else {
            succ = this.node(index);
            pred = succ.prev;
        }
        Object[] objectArray = a;
        int n = objectArray.length;
        for (int i = 0; i < n; ++i) {
            Object o;
            Object e = o = objectArray[i];
            Node<Object> newNode = new Node<Object>(pred, e, null);
            if (pred == null) {
                this.first = newNode;
            } else {
                pred.next = newNode;
            }
            pred = newNode;
        }
        if (succ == null) {
            this.last = pred;
        } else {
            pred.next = succ;
            succ.prev = pred;
        }
        this.size += numNew;
        ++this.modCount;
        return true;
    }

    @Override
    public void clear() {
        Node<E> x = this.first;
        while (x != null) {
            Node next = x.next;
            x.item = null;
            x.next = null;
            x.prev = null;
            x = next;
        }
        this.last = null;
        this.first = null;
        this.size = 0;
        ++this.modCount;
    }

    @Override
    public E get(int index) {
        this.checkElementIndex(index);
        return this.node((int)index).item;
    }

    @Override
    public E set(int index, E element) {
        this.checkElementIndex(index);
        Node<E> x = this.node(index);
        Object oldVal = x.item;
        x.item = element;
        return oldVal;
    }

    @Override
    public void add(int index, E element) {
        this.checkPositionIndex(index);
        if (index == this.size) {
            this.linkLast(element);
        } else {
            this.linkBefore(element, this.node(index));
        }
    }

    @Override
    public E remove(int index) {
        this.checkElementIndex(index);
        return this.unlink(this.node(index));
    }

    private boolean isElementIndex(int index) {
        return index >= 0 && index < this.size;
    }

    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= this.size;
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + this.size;
    }

    private void checkElementIndex(int index) {
        if (!this.isElementIndex(index)) {
            throw new IndexOutOfBoundsException(this.outOfBoundsMsg(index));
        }
    }

    private void checkPositionIndex(int index) {
        if (!this.isPositionIndex(index)) {
            throw new IndexOutOfBoundsException(this.outOfBoundsMsg(index));
        }
    }

    Node<E> node(int index) {
        if (index < this.size >> 1) {
            Node<E> x = this.first;
            for (int i = 0; i < index; ++i) {
                x = x.next;
            }
            return x;
        }
        Node<E> x = this.last;
        for (int i = this.size - 1; i > index; --i) {
            x = x.prev;
        }
        return x;
    }

    @Override
    public int indexOf(Object o) {
        int index = 0;
        if (o == null) {
            Node<E> x = this.first;
            while (x != null) {
                if (x.item == null) {
                    return index;
                }
                ++index;
                x = x.next;
            }
        } else {
            Node<E> x = this.first;
            while (x != null) {
                if (o.equals(x.item)) {
                    return index;
                }
                ++index;
                x = x.next;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        int index = this.size;
        if (o == null) {
            Node<E> x = this.last;
            while (x != null) {
                --index;
                if (x.item == null) {
                    return index;
                }
                x = x.prev;
            }
        } else {
            Node<E> x = this.last;
            while (x != null) {
                --index;
                if (o.equals(x.item)) {
                    return index;
                }
                x = x.prev;
            }
        }
        return -1;
    }

    @Override
    public E peek() {
        Node<E> f = this.first;
        return f == null ? null : (E)f.item;
    }

    @Override
    public E element() {
        return this.getFirst();
    }

    @Override
    public E poll() {
        Node<E> f = this.first;
        return f == null ? null : (E)this.unlinkFirst(f);
    }

    @Override
    public E remove() {
        return this.removeFirst();
    }

    @Override
    public boolean offer(E e) {
        return this.add(e);
    }

    @Override
    public boolean offerFirst(E e) {
        this.addFirst(e);
        return true;
    }

    @Override
    public boolean offerLast(E e) {
        this.addLast(e);
        return true;
    }

    @Override
    public E peekFirst() {
        Node<E> f = this.first;
        return f == null ? null : (E)f.item;
    }

    @Override
    public E peekLast() {
        Node<E> l = this.last;
        return l == null ? null : (E)l.item;
    }

    @Override
    public E pollFirst() {
        Node<E> f = this.first;
        return f == null ? null : (E)this.unlinkFirst(f);
    }

    @Override
    public E pollLast() {
        Node<E> l = this.last;
        return l == null ? null : (E)this.unlinkLast(l);
    }

    @Override
    public void push(E e) {
        this.addFirst(e);
    }

    @Override
    public E pop() {
        return this.removeFirst();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return this.remove(o);
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        if (o == null) {
            Node<E> x = this.last;
            while (x != null) {
                if (x.item == null) {
                    this.unlink(x);
                    return true;
                }
                x = x.prev;
            }
        } else {
            Node<E> x = this.last;
            while (x != null) {
                if (o.equals(x.item)) {
                    this.unlink(x);
                    return true;
                }
                x = x.prev;
            }
        }
        return false;
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        this.checkPositionIndex(index);
        return new ListItr(index);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    private LinkedList<E> superClone() {
        try {
            return (LinkedList)super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    public Object clone() {
        LinkedList clone = this.superClone();
        clone.last = null;
        clone.first = null;
        clone.size = 0;
        clone.modCount = 0;
        Node<E> x = this.first;
        while (x != null) {
            clone.add(x.item);
            x = x.next;
        }
        return clone;
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[this.size];
        int i = 0;
        Node<E> x = this.first;
        while (x != null) {
            result[i++] = x.item;
            x = x.next;
        }
        return result;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < this.size) {
            a = (Object[])Array.newInstance(a.getClass().getComponentType(), this.size);
        }
        int i = 0;
        T[] result = a;
        Node<E> x = this.first;
        while (x != null) {
            result[i++] = x.item;
            x = x.next;
        }
        if (a.length > this.size) {
            a[this.size] = null;
        }
        return a;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(this.size);
        Node<E> x = this.first;
        while (x != null) {
            s.writeObject(x.item);
            x = x.next;
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int size = s.readInt();
        for (int i = 0; i < size; ++i) {
            this.linkLast(s.readObject());
        }
    }

    @Override
    public Spliterator<E> spliterator() {
        return new LLSpliterator(this, -1, 0);
    }

    @Override
    public LinkedList<E> reversed() {
        return new ReverseOrderLinkedListView(this, super.reversed(), Deque.super.reversed());
    }

    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }

    private class ListItr
    implements ListIterator<E> {
        private Node<E> lastReturned;
        private Node<E> next;
        private int nextIndex;
        private int expectedModCount;

        ListItr(int index) {
            this.expectedModCount = LinkedList.this.modCount;
            this.next = index == LinkedList.this.size ? null : LinkedList.this.node(index);
            this.nextIndex = index;
        }

        @Override
        public boolean hasNext() {
            return this.nextIndex < LinkedList.this.size;
        }

        @Override
        public E next() {
            this.checkForComodification();
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            this.lastReturned = this.next;
            this.next = this.next.next;
            ++this.nextIndex;
            return this.lastReturned.item;
        }

        @Override
        public boolean hasPrevious() {
            return this.nextIndex > 0;
        }

        @Override
        public E previous() {
            this.checkForComodification();
            if (!this.hasPrevious()) {
                throw new NoSuchElementException();
            }
            this.next = this.next == null ? LinkedList.this.last : this.next.prev;
            this.lastReturned = this.next;
            --this.nextIndex;
            return this.lastReturned.item;
        }

        @Override
        public int nextIndex() {
            return this.nextIndex;
        }

        @Override
        public int previousIndex() {
            return this.nextIndex - 1;
        }

        @Override
        public void remove() {
            this.checkForComodification();
            if (this.lastReturned == null) {
                throw new IllegalStateException();
            }
            Node lastNext = this.lastReturned.next;
            LinkedList.this.unlink(this.lastReturned);
            if (this.next == this.lastReturned) {
                this.next = lastNext;
            } else {
                --this.nextIndex;
            }
            this.lastReturned = null;
            ++this.expectedModCount;
        }

        @Override
        public void set(E e) {
            if (this.lastReturned == null) {
                throw new IllegalStateException();
            }
            this.checkForComodification();
            this.lastReturned.item = e;
        }

        @Override
        public void add(E e) {
            this.checkForComodification();
            this.lastReturned = null;
            if (this.next == null) {
                LinkedList.this.linkLast(e);
            } else {
                LinkedList.this.linkBefore(e, this.next);
            }
            ++this.nextIndex;
            ++this.expectedModCount;
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            while (LinkedList.this.modCount == this.expectedModCount && this.nextIndex < LinkedList.this.size) {
                action.accept(this.next.item);
                this.lastReturned = this.next;
                this.next = this.next.next;
                ++this.nextIndex;
            }
            this.checkForComodification();
        }

        final void checkForComodification() {
            if (LinkedList.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    private class DescendingIterator
    implements Iterator<E> {
        private final ListItr itr;

        private DescendingIterator() {
            this.itr = new ListItr(LinkedList.this.size());
        }

        @Override
        public boolean hasNext() {
            return this.itr.hasPrevious();
        }

        @Override
        public E next() {
            return this.itr.previous();
        }

        @Override
        public void remove() {
            this.itr.remove();
        }
    }

    static final class LLSpliterator<E>
    implements Spliterator<E> {
        static final int BATCH_UNIT = 1024;
        static final int MAX_BATCH = 0x2000000;
        final LinkedList<E> list;
        Node<E> current;
        int est;
        int expectedModCount;
        int batch;

        LLSpliterator(LinkedList<E> list, int est, int expectedModCount) {
            this.list = list;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getEst() {
            int s = this.est;
            if (s < 0) {
                LinkedList<E> lst = this.list;
                if (lst == null) {
                    this.est = 0;
                    s = 0;
                } else {
                    this.expectedModCount = lst.modCount;
                    this.current = lst.first;
                    s = this.est = lst.size;
                }
            }
            return s;
        }

        @Override
        public long estimateSize() {
            return this.getEst();
        }

        @Override
        public Spliterator<E> trySplit() {
            Node<E> p;
            int s = this.getEst();
            if (s > 1 && (p = this.current) != null) {
                int n = this.batch + 1024;
                if (n > s) {
                    n = s;
                }
                if (n > 0x2000000) {
                    n = 0x2000000;
                }
                Object[] a = new Object[n];
                int j = 0;
                do {
                    a[j++] = p.item;
                } while ((p = p.next) != null && j < n);
                this.current = p;
                this.batch = j;
                this.est = s - j;
                return Spliterators.spliterator(a, 0, j, 16);
            }
            return null;
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) {
                throw new NullPointerException();
            }
            int n = this.getEst();
            if (n > 0 && (p = this.current) != null) {
                this.current = null;
                this.est = 0;
                do {
                    Object e = p.item;
                    p = p.next;
                    action.accept(e);
                } while (p != null && --n > 0);
            }
            if (this.list.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) {
                throw new NullPointerException();
            }
            if (this.getEst() > 0 && (p = this.current) != null) {
                --this.est;
                Object e = p.item;
                this.current = p.next;
                action.accept(e);
                if (this.list.modCount != this.expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                return true;
            }
            return false;
        }

        @Override
        public int characteristics() {
            return 16464;
        }
    }

    static class ReverseOrderLinkedListView<E>
    extends LinkedList<E>
    implements Externalizable {
        final LinkedList<E> list;
        final List<E> rlist;
        final Deque<E> rdeque;

        ReverseOrderLinkedListView(LinkedList<E> list, List<E> rlist, Deque<E> rdeque) {
            this.list = list;
            this.rlist = rlist;
            this.rdeque = rdeque;
        }

        @Override
        public String toString() {
            return this.rlist.toString();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return this.rlist.retainAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return this.rlist.removeAll(c);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return this.rlist.containsAll(c);
        }

        @Override
        public boolean isEmpty() {
            return this.rlist.isEmpty();
        }

        @Override
        public Stream<E> parallelStream() {
            return this.rlist.parallelStream();
        }

        @Override
        public Stream<E> stream() {
            return this.rlist.stream();
        }

        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            return this.rlist.removeIf(filter);
        }

        @Override
        public <T> T[] toArray(IntFunction<T[]> generator) {
            return this.rlist.toArray(generator);
        }

        @Override
        public void forEach(Consumer<? super E> action) {
            this.rlist.forEach(action);
        }

        @Override
        public Iterator<E> iterator() {
            return this.rlist.iterator();
        }

        @Override
        public int hashCode() {
            return this.rlist.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return this.rlist.equals(o);
        }

        @Override
        public List<E> subList(int fromIndex, int toIndex) {
            return this.rlist.subList(fromIndex, toIndex);
        }

        @Override
        public ListIterator<E> listIterator() {
            return this.rlist.listIterator();
        }

        @Override
        public void sort(Comparator<? super E> c) {
            this.rlist.sort(c);
        }

        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            this.rlist.replaceAll(operator);
        }

        @Override
        public LinkedList<E> reversed() {
            return this.list;
        }

        @Override
        public Spliterator<E> spliterator() {
            return this.rlist.spliterator();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return this.rlist.toArray(a);
        }

        @Override
        public Object[] toArray() {
            return this.rlist.toArray();
        }

        @Override
        public Iterator<E> descendingIterator() {
            return this.rdeque.descendingIterator();
        }

        @Override
        public ListIterator<E> listIterator(int index) {
            return this.rlist.listIterator(index);
        }

        @Override
        public boolean removeLastOccurrence(Object o) {
            return this.rdeque.removeLastOccurrence(o);
        }

        @Override
        public boolean removeFirstOccurrence(Object o) {
            return this.rdeque.removeFirstOccurrence(o);
        }

        @Override
        public E pop() {
            return this.rdeque.pop();
        }

        @Override
        public void push(E e) {
            this.rdeque.push(e);
        }

        @Override
        public E pollLast() {
            return this.rdeque.pollLast();
        }

        @Override
        public E pollFirst() {
            return this.rdeque.pollFirst();
        }

        @Override
        public E peekLast() {
            return this.rdeque.peekLast();
        }

        @Override
        public E peekFirst() {
            return this.rdeque.peekFirst();
        }

        @Override
        public boolean offerLast(E e) {
            return this.rdeque.offerLast(e);
        }

        @Override
        public boolean offerFirst(E e) {
            return this.rdeque.offerFirst(e);
        }

        @Override
        public boolean offer(E e) {
            return this.rdeque.offer(e);
        }

        @Override
        public E remove() {
            return this.rdeque.remove();
        }

        @Override
        public E poll() {
            return this.rdeque.poll();
        }

        @Override
        public E element() {
            return this.rdeque.element();
        }

        @Override
        public E peek() {
            return this.rdeque.peek();
        }

        @Override
        public int lastIndexOf(Object o) {
            return this.rlist.lastIndexOf(o);
        }

        @Override
        public int indexOf(Object o) {
            return this.rlist.indexOf(o);
        }

        @Override
        public E remove(int index) {
            return this.rlist.remove(index);
        }

        @Override
        public void add(int index, E element) {
            this.rlist.add(index, element);
        }

        @Override
        public E set(int index, E element) {
            return this.rlist.set(index, element);
        }

        @Override
        public E get(int index) {
            return this.rlist.get(index);
        }

        @Override
        public void clear() {
            this.rlist.clear();
        }

        @Override
        public boolean addAll(int index, Collection<? extends E> c) {
            return this.rlist.addAll(index, c);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            return this.rlist.addAll(c);
        }

        @Override
        public boolean remove(Object o) {
            return this.rlist.remove(o);
        }

        @Override
        public boolean add(E e) {
            return this.rlist.add(e);
        }

        @Override
        public int size() {
            return this.rlist.size();
        }

        @Override
        public boolean contains(Object o) {
            return this.rlist.contains(o);
        }

        @Override
        public void addLast(E e) {
            this.rdeque.addLast(e);
        }

        @Override
        public void addFirst(E e) {
            this.rdeque.addFirst(e);
        }

        @Override
        public E removeLast() {
            return this.rdeque.removeLast();
        }

        @Override
        public E removeFirst() {
            return this.rdeque.removeFirst();
        }

        @Override
        public E getLast() {
            return this.rdeque.getLast();
        }

        @Override
        public E getFirst() {
            return this.rdeque.getFirst();
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            throw new InvalidObjectException("not serializable");
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            throw new InvalidObjectException("not serializable");
        }
    }
}

