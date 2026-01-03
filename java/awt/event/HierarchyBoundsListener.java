/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.event.HierarchyEvent;
import java.util.EventListener;

public interface HierarchyBoundsListener
extends EventListener {
    public void ancestorMoved(HierarchyEvent var1);

    public void ancestorResized(HierarchyEvent var1);
}

