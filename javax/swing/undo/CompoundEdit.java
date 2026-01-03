/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.undo;

import java.util.Enumeration;
import java.util.Vector;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

public class CompoundEdit
extends AbstractUndoableEdit {
    boolean inProgress = true;
    protected Vector<UndoableEdit> edits = new Vector();

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        int i = this.edits.size();
        while (i-- > 0) {
            UndoableEdit e = this.edits.elementAt(i);
            e.undo();
        }
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        Enumeration<UndoableEdit> cursor = this.edits.elements();
        while (cursor.hasMoreElements()) {
            cursor.nextElement().redo();
        }
    }

    protected UndoableEdit lastEdit() {
        int count = this.edits.size();
        if (count > 0) {
            return this.edits.elementAt(count - 1);
        }
        return null;
    }

    @Override
    public void die() {
        int size = this.edits.size();
        for (int i = size - 1; i >= 0; --i) {
            UndoableEdit e = this.edits.elementAt(i);
            e.die();
        }
        super.die();
    }

    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        if (!this.inProgress) {
            return false;
        }
        UndoableEdit last = this.lastEdit();
        if (last == null) {
            this.edits.addElement(anEdit);
        } else if (!last.addEdit(anEdit)) {
            if (anEdit.replaceEdit(last)) {
                this.edits.removeElementAt(this.edits.size() - 1);
            }
            this.edits.addElement(anEdit);
        }
        return true;
    }

    public void end() {
        this.inProgress = false;
    }

    @Override
    public boolean canUndo() {
        return !this.isInProgress() && super.canUndo();
    }

    @Override
    public boolean canRedo() {
        return !this.isInProgress() && super.canRedo();
    }

    public boolean isInProgress() {
        return this.inProgress;
    }

    @Override
    public boolean isSignificant() {
        Enumeration<UndoableEdit> cursor = this.edits.elements();
        while (cursor.hasMoreElements()) {
            if (!cursor.nextElement().isSignificant()) continue;
            return true;
        }
        return false;
    }

    @Override
    public String getPresentationName() {
        UndoableEdit last = this.lastEdit();
        if (last != null) {
            return last.getPresentationName();
        }
        return super.getPresentationName();
    }

    @Override
    public String getUndoPresentationName() {
        UndoableEdit last = this.lastEdit();
        if (last != null) {
            return last.getUndoPresentationName();
        }
        return super.getUndoPresentationName();
    }

    @Override
    public String getRedoPresentationName() {
        UndoableEdit last = this.lastEdit();
        if (last != null) {
            return last.getRedoPresentationName();
        }
        return super.getRedoPresentationName();
    }

    @Override
    public String toString() {
        return super.toString() + " inProgress: " + this.inProgress + " edits: " + String.valueOf(this.edits);
    }
}

