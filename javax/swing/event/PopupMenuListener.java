/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.event;

import java.util.EventListener;
import javax.swing.event.PopupMenuEvent;

public interface PopupMenuListener
extends EventListener {
    public void popupMenuWillBecomeVisible(PopupMenuEvent var1);

    public void popupMenuWillBecomeInvisible(PopupMenuEvent var1);

    public void popupMenuCanceled(PopupMenuEvent var1);
}

