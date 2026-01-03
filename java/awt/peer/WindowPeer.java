/*
 * Decompiled with CFR 0.152.
 */
package java.awt.peer;

import java.awt.Dialog;
import java.awt.peer.ContainerPeer;

public interface WindowPeer
extends ContainerPeer {
    public void toFront();

    public void toBack();

    public void updateAlwaysOnTopState();

    public void updateFocusableWindowState();

    public void setModalBlocked(Dialog var1, boolean var2);

    public void updateMinimumSize();

    public void updateIconImages();

    public void setOpacity(float var1);

    public void setOpaque(boolean var1);

    public void updateWindow();

    public void repositionSecurityWarning();
}

