/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public interface LayoutManager2
extends LayoutManager {
    public void addLayoutComponent(Component var1, Object var2);

    public Dimension maximumLayoutSize(Container var1);

    public float getLayoutAlignmentX(Container var1);

    public float getLayoutAlignmentY(Container var1);

    public void invalidateLayout(Container var1);
}

