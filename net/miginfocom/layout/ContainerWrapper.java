/*
 * Decompiled with CFR 0.152.
 */
package net.miginfocom.layout;

import net.miginfocom.layout.ComponentWrapper;

public interface ContainerWrapper
extends ComponentWrapper {
    public ComponentWrapper[] getComponents();

    public int getComponentCount();

    public Object getLayout();

    public boolean isLeftToRight();

    public void paintDebugCell(int var1, int var2, int var3, int var4);
}

