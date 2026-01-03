/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import javax.swing.event.ChangeListener;

public interface SingleSelectionModel {
    public int getSelectedIndex();

    public void setSelectedIndex(int var1);

    public void clearSelection();

    public boolean isSelected();

    public void addChangeListener(ChangeListener var1);

    public void removeChangeListener(ChangeListener var1);
}

