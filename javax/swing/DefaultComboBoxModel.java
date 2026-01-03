/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.io.Serializable;
import java.util.Collection;
import java.util.Vector;
import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;

public class DefaultComboBoxModel<E>
extends AbstractListModel<E>
implements MutableComboBoxModel<E>,
Serializable {
    Vector<E> objects;
    Object selectedObject;

    public DefaultComboBoxModel() {
        this.objects = new Vector();
    }

    public DefaultComboBoxModel(E[] items) {
        this.objects = new Vector(items.length);
        int c = items.length;
        for (int i = 0; i < c; ++i) {
            this.objects.addElement(items[i]);
        }
        if (this.getSize() > 0) {
            this.selectedObject = this.getElementAt(0);
        }
    }

    public DefaultComboBoxModel(Vector<E> v) {
        this.objects = v;
        if (this.getSize() > 0) {
            this.selectedObject = this.getElementAt(0);
        }
    }

    @Override
    public void setSelectedItem(Object anObject) {
        if (this.selectedObject != null && !this.selectedObject.equals(anObject) || this.selectedObject == null && anObject != null) {
            this.selectedObject = anObject;
            this.fireContentsChanged(this, -1, -1);
        }
    }

    @Override
    public Object getSelectedItem() {
        return this.selectedObject;
    }

    @Override
    public int getSize() {
        return this.objects.size();
    }

    @Override
    public E getElementAt(int index) {
        if (index >= 0 && index < this.objects.size()) {
            return this.objects.elementAt(index);
        }
        return null;
    }

    public int getIndexOf(Object anObject) {
        return this.objects.indexOf(anObject);
    }

    @Override
    public void addElement(E anObject) {
        this.objects.addElement(anObject);
        this.fireIntervalAdded(this, this.objects.size() - 1, this.objects.size() - 1);
        if (this.objects.size() == 1 && this.selectedObject == null && anObject != null) {
            this.setSelectedItem(anObject);
        }
    }

    @Override
    public void insertElementAt(E anObject, int index) {
        this.objects.insertElementAt(anObject, index);
        this.fireIntervalAdded(this, index, index);
    }

    @Override
    public void removeElementAt(int index) {
        if (this.getElementAt(index) == this.selectedObject) {
            if (index == 0) {
                this.setSelectedItem(this.getSize() == 1 ? null : this.getElementAt(index + 1));
            } else {
                this.setSelectedItem(this.getElementAt(index - 1));
            }
        }
        this.objects.removeElementAt(index);
        this.fireIntervalRemoved(this, index, index);
    }

    @Override
    public void removeElement(Object anObject) {
        int index = this.objects.indexOf(anObject);
        if (index != -1) {
            this.removeElementAt(index);
        }
    }

    public void removeAllElements() {
        if (this.objects.size() > 0) {
            int firstIndex = 0;
            int lastIndex = this.objects.size() - 1;
            this.objects.removeAllElements();
            this.selectedObject = null;
            this.fireIntervalRemoved(this, firstIndex, lastIndex);
        } else {
            this.selectedObject = null;
        }
    }

    public void addAll(Collection<? extends E> c) {
        if (c.isEmpty()) {
            return;
        }
        int startIndex = this.getSize();
        this.objects.addAll(c);
        this.fireIntervalAdded(this, startIndex, this.getSize() - 1);
    }

    public void addAll(int index, Collection<? extends E> c) {
        if (index < 0 || index > this.getSize()) {
            throw new ArrayIndexOutOfBoundsException("index out of range: " + index);
        }
        if (c.isEmpty()) {
            return;
        }
        this.objects.addAll(index, c);
        this.fireIntervalAdded(this, index, index + c.size() - 1);
    }
}

