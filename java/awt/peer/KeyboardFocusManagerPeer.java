/*
 * Decompiled with CFR 0.152.
 */
package java.awt.peer;

import java.awt.Component;
import java.awt.Window;

public interface KeyboardFocusManagerPeer {
    public void setCurrentFocusedWindow(Window var1);

    public Window getCurrentFocusedWindow();

    public void setCurrentFocusOwner(Component var1);

    public Component getCurrentFocusOwner();

    public void clearGlobalFocusOwner(Window var1);
}

