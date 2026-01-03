/*
 * Decompiled with CFR 0.152.
 */
package java.awt.peer;

import java.awt.MenuBar;
import java.awt.Rectangle;
import java.awt.peer.WindowPeer;

public interface FramePeer
extends WindowPeer {
    public void setTitle(String var1);

    public void setMenuBar(MenuBar var1);

    public void setResizable(boolean var1);

    public void setState(int var1);

    public int getState();

    public void setMaximizedBounds(Rectangle var1);

    public void setBoundsPrivate(int var1, int var2, int var3, int var4);

    public Rectangle getBoundsPrivate();

    public void emulateActivation(boolean var1);
}

