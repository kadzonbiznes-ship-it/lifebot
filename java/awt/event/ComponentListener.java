/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.event.ComponentEvent;
import java.util.EventListener;

public interface ComponentListener
extends EventListener {
    public void componentResized(ComponentEvent var1);

    public void componentMoved(ComponentEvent var1);

    public void componentShown(ComponentEvent var1);

    public void componentHidden(ComponentEvent var1);
}

