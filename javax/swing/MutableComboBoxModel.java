/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import javax.swing.ComboBoxModel;

public interface MutableComboBoxModel<E>
extends ComboBoxModel<E> {
    public void addElement(E var1);

    public void removeElement(Object var1);

    public void insertElementAt(E var1, int var2);

    public void removeElementAt(int var1);
}

