/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.Component;
import javax.swing.JList;

public interface ListCellRenderer<E> {
    public Component getListCellRendererComponent(JList<? extends E> var1, E var2, int var3, boolean var4, boolean var5);
}

