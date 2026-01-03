/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.event.ItemListener;

public interface ItemSelectable {
    public Object[] getSelectedObjects();

    public void addItemListener(ItemListener var1);

    public void removeItemListener(ItemListener var1);
}

