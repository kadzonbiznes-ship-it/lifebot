/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.event.WindowEvent;
import java.util.EventListener;

public interface WindowFocusListener
extends EventListener {
    public void windowGainedFocus(WindowEvent var1);

    public void windowLostFocus(WindowEvent var1);
}

