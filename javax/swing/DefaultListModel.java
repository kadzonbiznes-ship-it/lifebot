/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.AbstractListModel;

public class DefaultListModel<E>
extends AbstractListModel<E> {
    private Vector<E> delegate = new Vector();

    @Override
    public int getSize() {
        return this.delegate.size();
    }

    @Override
    public E getElementAt(int index) {
        return this.delegate.elementAt(index);
    }

    public void copyInto(Object[] anArray) {
        this.delegate.copyInto(anArray);
    }

    public void trimToSize() {
        this.delegate.trimToSize();
    }

    public void ensureCapacity(int minCapacity) {
        this.delegate.ensureCapacity(minCapacity);
    }

    public void setSize(int newSize) {
        int oldSize = this.delegate.size();
        this.delegate.setSize(newSize);
        if (oldSize > newSize) {
            this.fireIntervalRemoved(this, newSize, oldSize - 1);
        } else if (oldSize < newSize) {
            this.fireIntervalAdded(this, oldSize, newSize - 1);
        }
    }

    public int capacity() {
        return this.delegate.capacity();
    }

    public int size() {
        return this.delegate.size();
    }

    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    public Enumeration<E> elements() {
        return this.delegate.elements();
    }

    public boolean contains(Object elem) {
        return this.delegate.contains(elem);
    }

    public int indexOf(Object elem) {
        return this.delegate.indexOf(elem);
    }

    public int indexOf(Object elem, int index) {
        return this.delegate.indexOf(elem, index);
    }

    public int lastIndexOf(Object elem) {
        return this.delegate.lastIndexOf(elem);
    }

    public int lastIndexOf(Object elem, int index) {
        return this.delegate.lastIndexOf(elem, index);
    }

    public E elementAt(int index) {
        return this.delegate.elementAt(index);
    }

    public E firstElement() {
        return this.delegate.firstElement();
    }

    public E lastElement() {
        return this.delegate.lastElement();
    }

    public void setElementAt(E element, int index) {
        this.delegate.setElementAt(element, index);
        this.fireContentsChanged(this, index, index);
    }

    public void removeElementAt(int index) {
        this.delegate.removeElementAt(index);
        this.fireIntervalRemoved(this, index, index);
    }

    public void insertElementAt(E element, int index) {
        this.delegate.insertElementAt(element, index);
        this.fireIntervalAdded(this, index, index);
    }

    public void addElement(E element) {
        int index = this.delegate.size();
        this.delegate.addElement(element);
        this.fireIntervalAdded(this, index, index);
    }

    public boolean removeElement(Object obj) {
        int index = this.indexOf(obj);
        boolean rv = this.delegate.removeElement(obj);
        if (index >= 0) {
            this.fireIntervalRemoved(this, index, index);
        }
        return rv;
    }

    public void removeAllElements() {
        int index1 = this.delegate.size() - 1;
        this.delegate.removeAllElements();
        if (index1 >= 0) {
            this.fireIntervalRemoved(this, 0, index1);
        }
    }

    public String toString() {
        return this.delegate.toString();
    }

    public Object[] toArray() {
        Object[] rv = new Object[this.delegate.size()];
        this.delegate.copyInto(rv);
        return rv;
    }

    public E get(int index) {
        return this.delegate.elementAt(index);
    }

    public E set(int index, E element) {
        E rv = this.delegate.elementAt(index);
        this.delegate.setElementAt(element, index);
        this.fireContentsChanged(this, index, index);
        return rv;
    }

    public void add(int index, E element) {
        this.delegate.insertElementAt(element, index);
        this.fireIntervalAdded(this, index, index);
    }

    public E remove(int index) {
        E rv = this.delegate.elementAt(index);
        this.delegate.removeElementAt(index);
        this.fireIntervalRemoved(this, index, index);
        return rv;
    }

    public void clear() {
        int index1 = this.delegate.size() - 1;
        this.delegate.removeAllElements();
        if (index1 >= 0) {
            this.fireIntervalRemoved(this, 0, index1);
        }
    }

    public void removeRange(int fromIndex, int toIndex) {
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex must be <= toIndex");
        }
        for (int i = toIndex; i >= fromIndex; --i) {
            this.delegate.removeElementAt(i);
        }
        this.fireIntervalRemoved(this, fromIndex, toIndex);
    }

    public void addAll(Collection<? extends E> c) {
        if (c.isEmpty()) {
            return;
        }
        int startIndex = this.getSize();
        this.delegate.addAll(c);
        this.fireIntervalAdded(this, startIndex, this.getSize() - 1);
    }

    public void addAll(int index, Collection<? extends E> c) {
        if (index < 0 || index > this.getSize()) {
            throw new ArrayIndexOutOfBoundsException("index out of range: " + index);
        }
        if (c.isEmpty()) {
            return;
        }
        this.delegate.addAll(index, c);
        this.fireIntervalAdded(this, index, index + c.size() - 1);
    }
}

