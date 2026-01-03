/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.io.Serializable;
import java.util.EventListener;
import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public class DefaultBoundedRangeModel
implements BoundedRangeModel,
Serializable {
    protected transient ChangeEvent changeEvent = null;
    protected EventListenerList listenerList = new EventListenerList();
    private int value = 0;
    private int extent = 0;
    private int min = 0;
    private int max = 100;
    private boolean isAdjusting = false;

    public DefaultBoundedRangeModel() {
    }

    public DefaultBoundedRangeModel(int value, int extent, int min, int max) {
        if (max < min || value < min || value + extent < value || value + extent > max) {
            throw new IllegalArgumentException("invalid range properties");
        }
        this.value = value;
        this.extent = extent;
        this.min = min;
        this.max = max;
    }

    @Override
    public int getValue() {
        return this.value;
    }

    @Override
    public int getExtent() {
        return this.extent;
    }

    @Override
    public int getMinimum() {
        return this.min;
    }

    @Override
    public int getMaximum() {
        return this.max;
    }

    @Override
    public void setValue(int n) {
        int newValue = Math.max(n = Math.min(n, Integer.MAX_VALUE - this.extent), this.min);
        if (newValue + this.extent > this.max) {
            newValue = this.max - this.extent;
        }
        this.setRangeProperties(newValue, this.extent, this.min, this.max, this.isAdjusting);
    }

    @Override
    public void setExtent(int n) {
        int newExtent = Math.max(0, n);
        if (this.value + newExtent > this.max) {
            newExtent = this.max - this.value;
        }
        this.setRangeProperties(this.value, newExtent, this.min, this.max, this.isAdjusting);
    }

    @Override
    public void setMinimum(int n) {
        int newMax = Math.max(n, this.max);
        int newValue = Math.max(n, this.value);
        int newExtent = 0;
        newExtent = (long)newMax - (long)newValue > (long)newMax ? this.extent : Math.min(newMax - newValue, this.extent);
        this.setRangeProperties(newValue, newExtent, n, newMax, this.isAdjusting);
    }

    @Override
    public void setMaximum(int n) {
        int newMin = Math.min(n, this.min);
        int newExtent = Math.min(n - newMin, this.extent);
        int newValue = Math.min(n - newExtent, this.value);
        this.setRangeProperties(newValue, newExtent, newMin, n, this.isAdjusting);
    }

    @Override
    public void setValueIsAdjusting(boolean b) {
        this.setRangeProperties(this.value, this.extent, this.min, this.max, b);
    }

    @Override
    public boolean getValueIsAdjusting() {
        return this.isAdjusting;
    }

    @Override
    public void setRangeProperties(int newValue, int newExtent, int newMin, int newMax, boolean adjusting) {
        boolean isChange;
        if (newMin > newMax) {
            newMin = newMax;
        }
        if (newValue > newMax) {
            newMax = newValue;
        }
        if (newValue < newMin) {
            newMin = newValue;
        }
        if ((long)newExtent + (long)newValue > (long)newMax) {
            newExtent = newMax - newValue;
        }
        if (newExtent < 0) {
            newExtent = 0;
        }
        boolean bl = isChange = newValue != this.value || newExtent != this.extent || newMin != this.min || newMax != this.max || adjusting != this.isAdjusting;
        if (isChange) {
            this.value = newValue;
            this.extent = newExtent;
            this.min = newMin;
            this.max = newMax;
            this.isAdjusting = adjusting;
            this.fireStateChanged();
        }
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

    public String toString() {
        String modelString = "value=" + this.getValue() + ", extent=" + this.getExtent() + ", min=" + this.getMinimum() + ", max=" + this.getMaximum() + ", adj=" + this.getValueIsAdjusting();
        return this.getClass().getName() + "[" + modelString + "]";
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        return this.listenerList.getListeners(listenerType);
    }
}

