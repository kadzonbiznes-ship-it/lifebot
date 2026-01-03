/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.event.MouseEvent;
import java.util.EventListener;

public interface MouseListener
extends EventListener {
    public void mouseClicked(MouseEvent var1);

    public void mousePressed(MouseEvent var1);

    public void mouseReleased(MouseEvent var1);

    public void mouseEntered(MouseEvent var1);

    public void mouseExited(MouseEvent var1);
}

