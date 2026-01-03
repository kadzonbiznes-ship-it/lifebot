/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public interface UndoableEdit {
    public void undo() throws CannotUndoException;

    public boolean canUndo();

    public void redo() throws CannotRedoException;

    public boolean canRedo();

    public void die();

    public boolean addEdit(UndoableEdit var1);

    public boolean replaceEdit(UndoableEdit var1);

    public boolean isSignificant();

    public String getPresentationName();

    public String getUndoPresentationName();

    public String getRedoPresentationName();
}

