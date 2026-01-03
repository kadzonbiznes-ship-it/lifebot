/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.event;

import java.util.EventListener;
import javax.swing.event.MenuKeyEvent;

public interface MenuKeyListener
extends EventListener {
    public void menuKeyTyped(MenuKeyEvent var1);

    public void menuKeyPressed(MenuKeyEvent var1);

    public void menuKeyReleased(MenuKeyEvent var1);
}

