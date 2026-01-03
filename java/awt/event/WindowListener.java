/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.event.WindowEvent;
import java.util.EventListener;

public interface WindowListener
extends EventListener {
    public void windowOpened(WindowEvent var1);

    public void windowClosing(WindowEvent var1);

    public void windowClosed(WindowEvent var1);

    public void windowIconified(WindowEvent var1);

    public void windowDeiconified(WindowEvent var1);

    public void windowActivated(WindowEvent var1);

    public void windowDeactivated(WindowEvent var1);
}

