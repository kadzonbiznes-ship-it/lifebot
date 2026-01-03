/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.event;

import java.util.EventListener;
import javax.swing.event.ChangeEvent;

public interface CellEditorListener
extends EventListener {
    public void editingStopped(ChangeEvent var1);

    public void editingCanceled(ChangeEvent var1);
}

