/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.event;

import java.util.EventObject;
import javax.swing.undo.UndoableEdit;

public class UndoableEditEvent
extends EventObject {
    private UndoableEdit myEdit;

    public UndoableEditEvent(Object source, UndoableEdit edit) {
        super(source);
        this.myEdit = edit;
    }

    public UndoableEdit getEdit() {
        return this.myEdit;
    }
}

