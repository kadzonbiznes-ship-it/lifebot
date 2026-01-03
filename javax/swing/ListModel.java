/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import javax.swing.event.ListDataListener;

public interface ListModel<E> {
    public int getSize();

    public E getElementAt(int var1);

    public void addListDataListener(ListDataListener var1);

    public void removeListDataListener(ListDataListener var1);
}

