/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.undo;

import java.io.Serializable;
import javax.swing.UIManager;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

public class AbstractUndoableEdit
implements UndoableEdit,
Serializable {
    protected static final String UndoName = "Undo";
    protected static final String RedoName = "Redo";
    boolean hasBeenDone = true;
    boolean alive = true;

    @Override
    public void die() {
        this.alive = false;
    }

    @Override
    public void undo() throws CannotUndoException {
        if (!this.canUndo()) {
            throw new CannotUndoException();
        }
        this.hasBeenDone = false;
    }

    @Override
    public boolean canUndo() {
        return this.alive && this.hasBeenDone;
    }

    @Override
    public void redo() throws CannotRedoException {
        if (!this.canRedo()) {
            throw new CannotRedoException();
        }
        this.hasBeenDone = true;
    }

    @Override
    public boolean canRedo() {
        return this.alive && !this.hasBeenDone;
    }

    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        return false;
    }

    @Override
    public boolean replaceEdit(UndoableEdit anEdit) {
        return false;
    }

    @Override
    public boolean isSignificant() {
        return true;
    }

    @Override
    public String getPresentationName() {
        return "";
    }

    @Override
    public String getUndoPresentationName() {
        Object name = this.getPresentationName();
        name = !"".equals(name) ? UIManager.getString("AbstractUndoableEdit.undoText") + " " + (String)name : UIManager.getString("AbstractUndoableEdit.undoText");
        return name;
    }

    @Override
    public String getRedoPresentationName() {
        Object name = this.getPresentationName();
        name = !"".equals(name) ? UIManager.getString("AbstractUndoableEdit.redoText") + " " + (String)name : UIManager.getString("AbstractUndoableEdit.redoText");
        return name;
    }

    public String toString() {
        return super.toString() + " hasBeenDone: " + this.hasBeenDone + " alive: " + this.alive;
    }
}

