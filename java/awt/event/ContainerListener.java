/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.event.ContainerEvent;
import java.util.EventListener;

public interface ContainerListener
extends EventListener {
    public void componentAdded(ContainerEvent var1);

    public void componentRemoved(ContainerEvent var1);
}

