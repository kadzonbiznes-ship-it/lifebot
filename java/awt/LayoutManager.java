/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

public interface LayoutManager {
    public void addLayoutComponent(String var1, Component var2);

    public void removeLayoutComponent(Component var1);

    public Dimension preferredLayoutSize(Container var1);

    public Dimension minimumLayoutSize(Container var1);

    public void layoutContainer(Container var1);
}

