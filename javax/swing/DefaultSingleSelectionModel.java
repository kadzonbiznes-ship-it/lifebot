/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.io.Serializable;
import java.util.EventListener;
import javax.swing.SingleSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public class DefaultSingleSelectionModel
implements SingleSelectionModel,
Serializable {
    protected transient ChangeEvent changeEvent = null;
    protected EventListenerList listenerList = new EventListenerList();
    private int index = -1;

    @Override
    public int getSelectedIndex() {
        return this.index;
    }

    @Override
    public void setSelectedIndex(int index) {
        if (this.index != index) {
            this.index = index;
            this.fireStateChanged();
        }
    }

    @Override
    public void clearSelection() {
        this.setSelectedIndex(-1);
    }

    @Override
    public boolean isSelected() {
        boolean ret = false;
        if (this.getSelectedIndex() != -1) {
            ret = true;
        }
        return ret;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        this.listenerList.add(ChangeListener.class, l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        this.listenerList.remove(ChangeListener.class, l);
    }

    public ChangeListener[] getChangeListeners() {
        return (ChangeListener[])this.listenerList.getListeners(ChangeListener.class);
    }

    protected void fireStateChanged() {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ChangeListener.class) continue;
            if (this.changeEvent == null) {
                this.changeEvent = new ChangeEvent(this);
            }
            ((ChangeListener)listeners[i + 1]).stateChanged(this.changeEvent);
        }
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        return this.listenerList.getListeners(listenerType);
    }
}

