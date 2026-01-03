/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import javax.swing.ListModel;

public interface ComboBoxModel<E>
extends ListModel<E> {
    public void setSelectedItem(Object var1);

    public Object getSelectedItem();
}

