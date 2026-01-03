/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.event;

import java.util.EventListener;
import javax.swing.event.AncestorEvent;

public interface AncestorListener
extends EventListener {
    public void ancestorAdded(AncestorEvent var1);

    public void ancestorRemoved(AncestorEvent var1);

    public void ancestorMoved(AncestorEvent var1);
}

