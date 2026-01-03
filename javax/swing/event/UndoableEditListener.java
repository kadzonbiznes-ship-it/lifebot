/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.event;

import java.util.EventListener;
import javax.swing.event.UndoableEditEvent;

public interface UndoableEditListener
extends EventListener {
    public void undoableEditHappened(UndoableEditEvent var1);
}

