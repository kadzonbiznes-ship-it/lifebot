/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.event.KeyEvent;
import java.util.EventListener;

public interface KeyListener
extends EventListener {
    public void keyTyped(KeyEvent var1);

    public void keyPressed(KeyEvent var1);

    public void keyReleased(KeyEvent var1);
}

