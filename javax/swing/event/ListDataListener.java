/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.event;

import java.util.EventListener;
import javax.swing.event.ListDataEvent;

public interface ListDataListener
extends EventListener {
    public void intervalAdded(ListDataEvent var1);

    public void intervalRemoved(ListDataEvent var1);

    public void contentsChanged(ListDataEvent var1);
}

