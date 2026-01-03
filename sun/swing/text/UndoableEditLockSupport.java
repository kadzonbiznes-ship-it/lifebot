/*
 * Decompiled with CFR 0.152.
 */
package sun.swing.text;

import javax.swing.undo.UndoableEdit;

public interface UndoableEditLockSupport
extends UndoableEdit {
    public void lockEdit();

    public void unlockEdit();
}

