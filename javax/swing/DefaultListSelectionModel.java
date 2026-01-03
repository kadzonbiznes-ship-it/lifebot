/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.beans.Transient;
import java.io.Serializable;
import java.util.BitSet;
import java.util.EventListener;
import javax.swing.ListSelectionModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class DefaultListSelectionModel
implements ListSelectionModel,
Cloneable,
Serializable {
    private static final int MIN = -1;
    private static final int MAX = Integer.MAX_VALUE;
    private int selectionMode = 2;
    private int minIndex = Integer.MAX_VALUE;
    private int maxIndex = -1;
    private int anchorIndex = -1;
    private int leadIndex = -1;
    private int firstAdjustedIndex = Integer.MAX_VALUE;
    private int lastAdjustedIndex = -1;
    private boolean isAdjusting = false;
    private int firstChangedIndex = Integer.MAX_VALUE;
    private int lastChangedIndex = -1;
    private BitSet value = new BitSet(32);
    protected EventListenerList listenerList = new EventListenerList();
    protected boolean leadAnchorNotificationEnabled = true;

    @Override
    public int getMinSelectionIndex() {
        return this.isSelectionEmpty() ? -1 : this.minIndex;
    }

    @Override
    public int getMaxSelectionIndex() {
        return this.maxIndex;
    }

    @Override
    public boolean getValueIsAdjusting() {
        return this.isAdjusting;
    }

    @Override
    public int getSelectionMode() {
        return this.selectionMode;
    }

    @Override
    public void setSelectionMode(int selectionMode) {
        int oldMode = this.selectionMode;
        switch (selectionMode) {
            case 0: 
            case 1: 
            case 2: {
                this.selectionMode = selectionMode;
                break;
            }
            default: {
                throw new IllegalArgumentException("invalid selectionMode");
            }
        }
        if (oldMode > this.selectionMode) {
            if (this.selectionMode == 0) {
                if (!this.isSelectionEmpty()) {
                    this.setSelectionInterval(this.minIndex, this.minIndex);
                }
            } else if (this.selectionMode == 1 && !this.isSelectionEmpty()) {
                int selectionEndindex = this.minIndex;
                while (this.value.get(selectionEndindex + 1)) {
                    ++selectionEndindex;
                }
                this.setSelectionInterval(this.minIndex, selectionEndindex);
            }
        }
    }

    @Override
    public boolean isSelectedIndex(int index) {
        return index < this.minIndex || index > this.maxIndex ? false : this.value.get(index);
    }

    @Override
    public boolean isSelectionEmpty() {
        return this.minIndex > this.maxIndex;
    }

    @Override
    public void addListSelectionListener(ListSelectionListener l) {
        this.listenerList.add(ListSelectionListener.class, l);
    }

    @Override
    public void removeListSelectionListener(ListSelectionListener l) {
        this.listenerList.remove(ListSelectionListener.class, l);
    }

    public ListSelectionListener[] getListSelectionListeners() {
        return (ListSelectionListener[])this.listenerList.getListeners(ListSelectionListener.class);
    }

    protected void fireValueChanged(boolean isAdjusting) {
        if (this.lastChangedIndex == -1) {
            return;
        }
        int oldFirstChangedIndex = this.firstChangedIndex;
        int oldLastChangedIndex = this.lastChangedIndex;
        this.firstChangedIndex = Integer.MAX_VALUE;
        this.lastChangedIndex = -1;
        this.fireValueChanged(oldFirstChangedIndex, oldLastChangedIndex, isAdjusting);
    }

    protected void fireValueChanged(int firstIndex, int lastIndex) {
        this.fireValueChanged(firstIndex, lastIndex, this.getValueIsAdjusting());
    }

    protected void fireValueChanged(int firstIndex, int lastIndex, boolean isAdjusting) {
        Object[] listeners = this.listenerList.getListenerList();
        ListSelectionEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ListSelectionListener.class) continue;
            if (e == null) {
                e = new ListSelectionEvent(this, firstIndex, lastIndex, isAdjusting);
            }
            ((ListSelectionListener)listeners[i + 1]).valueChanged(e);
        }
    }

    private void fireValueChanged() {
        if (this.lastAdjustedIndex == -1) {
            return;
        }
        if (this.getValueIsAdjusting()) {
            this.firstChangedIndex = Math.min(this.firstChangedIndex, this.firstAdjustedIndex);
            this.lastChangedIndex = Math.max(this.lastChangedIndex, this.lastAdjustedIndex);
        }
        int oldFirstAdjustedIndex = this.firstAdjustedIndex;
        int oldLastAdjustedIndex = this.lastAdjustedIndex;
        this.firstAdjustedIndex = Integer.MAX_VALUE;
        this.lastAdjustedIndex = -1;
        this.fireValueChanged(oldFirstAdjustedIndex, oldLastAdjustedIndex);
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        return this.listenerList.getListeners(listenerType);
    }

    private void markAsDirty(int r) {
        if (r == -1) {
            return;
        }
        this.firstAdjustedIndex = Math.min(this.firstAdjustedIndex, r);
        this.lastAdjustedIndex = Math.max(this.lastAdjustedIndex, r);
    }

    private void set(int r) {
        if (this.value.get(r)) {
            return;
        }
        this.value.set(r);
        this.markAsDirty(r);
        this.minIndex = Math.min(this.minIndex, r);
        this.maxIndex = Math.max(this.maxIndex, r);
    }

    private void clear(int r) {
        if (!this.value.get(r)) {
            return;
        }
        this.value.clear(r);
        this.markAsDirty(r);
        if (r == this.minIndex && this.minIndex < Integer.MAX_VALUE) {
            ++this.minIndex;
            while (this.minIndex <= this.maxIndex && !this.value.get(this.minIndex)) {
                ++this.minIndex;
            }
        }
        if (r == this.maxIndex) {
            --this.maxIndex;
            while (this.minIndex <= this.maxIndex && !this.value.get(this.maxIndex)) {
                --this.maxIndex;
            }
        }
        if (this.isSelectionEmpty()) {
            this.minIndex = Integer.MAX_VALUE;
            this.maxIndex = -1;
        }
    }

    public void setLeadAnchorNotificationEnabled(boolean flag) {
        this.leadAnchorNotificationEnabled = flag;
    }

    public boolean isLeadAnchorNotificationEnabled() {
        return this.leadAnchorNotificationEnabled;
    }

    private void updateLeadAnchorIndices(int anchorIndex, int leadIndex) {
        if (this.leadAnchorNotificationEnabled) {
            if (this.anchorIndex != anchorIndex) {
                this.markAsDirty(this.anchorIndex);
                this.markAsDirty(anchorIndex);
            }
            if (this.leadIndex != leadIndex) {
                this.markAsDirty(this.leadIndex);
                this.markAsDirty(leadIndex);
            }
        }
        this.anchorIndex = anchorIndex;
        this.leadIndex = leadIndex;
    }

    private boolean contains(int a, int b, int i) {
        return i >= a && i <= b;
    }

    private void changeSelection(int clearMin, int clearMax, int setMin, int setMax, boolean clearFirst) {
        for (int i = Math.min(setMin, clearMin); i <= Math.max(setMax, clearMax); ++i) {
            boolean shouldClear = this.contains(clearMin, clearMax, i);
            boolean shouldSet = this.contains(setMin, setMax, i);
            if (shouldSet && shouldClear) {
                if (clearFirst) {
                    shouldClear = false;
                } else {
                    shouldSet = false;
                }
            }
            if (shouldSet) {
                this.set(i);
            }
            if (shouldClear) {
                this.clear(i);
            }
            if (i == Integer.MAX_VALUE) break;
        }
        this.fireValueChanged();
    }

    private void changeSelection(int clearMin, int clearMax, int setMin, int setMax) {
        this.changeSelection(clearMin, clearMax, setMin, setMax, true);
    }

    @Override
    public void clearSelection() {
        this.removeSelectionIntervalImpl(this.minIndex, this.maxIndex, false);
    }

    @Override
    public void setSelectionInterval(int index0, int index1) {
        if (index0 == -1 || index1 == -1) {
            return;
        }
        if (this.getSelectionMode() == 0) {
            index0 = index1;
        }
        this.updateLeadAnchorIndices(index0, index1);
        int clearMin = this.minIndex;
        int clearMax = this.maxIndex;
        int setMin = Math.min(index0, index1);
        int setMax = Math.max(index0, index1);
        this.changeSelection(clearMin, clearMax, setMin, setMax);
    }

    @Override
    public void addSelectionInterval(int index0, int index1) {
        if (index0 == -1 || index1 == -1) {
            return;
        }
        if (this.getSelectionMode() == 0) {
            this.setSelectionInterval(index0, index1);
            return;
        }
        this.updateLeadAnchorIndices(index0, index1);
        int clearMin = Integer.MAX_VALUE;
        int clearMax = -1;
        int setMin = Math.min(index0, index1);
        int setMax = Math.max(index0, index1);
        if (this.getSelectionMode() == 1 && (setMax < this.minIndex - 1 || setMin > this.maxIndex + 1)) {
            this.setSelectionInterval(index0, index1);
            return;
        }
        this.changeSelection(clearMin, clearMax, setMin, setMax);
    }

    @Override
    public void removeSelectionInterval(int index0, int index1) {
        this.removeSelectionIntervalImpl(index0, index1, true);
    }

    private void removeSelectionIntervalImpl(int index0, int index1, boolean changeLeadAnchor) {
        if (index0 == -1 || index1 == -1) {
            return;
        }
        if (changeLeadAnchor) {
            this.updateLeadAnchorIndices(index0, index1);
        }
        int clearMin = Math.min(index0, index1);
        int clearMax = Math.max(index0, index1);
        int setMin = Integer.MAX_VALUE;
        int setMax = -1;
        if (this.getSelectionMode() != 2 && clearMin > this.minIndex && clearMax < this.maxIndex) {
            clearMax = this.maxIndex;
        }
        this.changeSelection(clearMin, clearMax, setMin, setMax);
    }

    private void setState(int index, boolean state) {
        if (state) {
            this.set(index);
        } else {
            this.clear(index);
        }
    }

    @Override
    public void insertIndexInterval(int index, int length, boolean before) {
        int anchorIndex;
        if (length < 0 || index < 0) {
            throw new IndexOutOfBoundsException("index or length is negative");
        }
        if (index == Integer.MAX_VALUE || length == 0) {
            return;
        }
        int insMinIndex = before ? index : index + 1;
        int insMaxIndex = insMinIndex + length >= 0 ? insMinIndex + length - 1 : Integer.MAX_VALUE;
        for (int i = Math.min(this.maxIndex, Integer.MAX_VALUE - length); i >= insMinIndex; --i) {
            this.setState(i + length, this.value.get(i));
        }
        boolean setInsertedValues = this.getSelectionMode() == 0 ? false : this.value.get(index);
        for (int i = insMaxIndex; i >= insMinIndex; --i) {
            this.setState(i, setInsertedValues);
        }
        int leadIndex = this.leadIndex;
        if (leadIndex > index || before && leadIndex == index) {
            leadIndex = this.leadIndex + length;
        }
        if ((anchorIndex = this.anchorIndex) > index || before && anchorIndex == index) {
            anchorIndex = this.anchorIndex + length;
        }
        if (leadIndex != this.leadIndex || anchorIndex != this.anchorIndex) {
            this.updateLeadAnchorIndices(anchorIndex, leadIndex);
        }
        this.fireValueChanged();
    }

    @Override
    public void removeIndexInterval(int index0, int index1) {
        int anchorIndex;
        if (index0 < 0 || index1 < 0) {
            throw new IndexOutOfBoundsException("index is negative");
        }
        int rmMinIndex = Math.min(index0, index1);
        int rmMaxIndex = Math.max(index0, index1);
        if (rmMinIndex == 0 && rmMaxIndex == Integer.MAX_VALUE) {
            for (int i = Integer.MAX_VALUE; i >= 0; --i) {
                this.setState(i, false);
            }
            if (this.anchorIndex != -1 || this.leadIndex != -1) {
                this.updateLeadAnchorIndices(-1, -1);
            }
            return;
        }
        int gapLength = rmMaxIndex - rmMinIndex + 1;
        for (int i = rmMinIndex; i >= 0 && i <= this.maxIndex; ++i) {
            this.setState(i, i <= Integer.MAX_VALUE - gapLength && i + gapLength >= this.minIndex && this.value.get(i + gapLength));
        }
        int leadIndex = this.leadIndex;
        if (leadIndex != 0 || rmMinIndex != 0) {
            if (leadIndex > rmMaxIndex) {
                leadIndex = this.leadIndex - gapLength;
            } else if (leadIndex >= rmMinIndex) {
                leadIndex = rmMinIndex - 1;
            }
        }
        if ((anchorIndex = this.anchorIndex) != 0 || rmMinIndex != 0) {
            if (anchorIndex > rmMaxIndex) {
                anchorIndex = this.anchorIndex - gapLength;
            } else if (anchorIndex >= rmMinIndex) {
                anchorIndex = rmMinIndex - 1;
            }
        }
        if (leadIndex != this.leadIndex || anchorIndex != this.anchorIndex) {
            this.updateLeadAnchorIndices(anchorIndex, leadIndex);
        }
        this.fireValueChanged();
    }

    @Override
    public void setValueIsAdjusting(boolean isAdjusting) {
        if (isAdjusting != this.isAdjusting) {
            this.isAdjusting = isAdjusting;
            this.fireValueChanged(isAdjusting);
        }
    }

    public String toString() {
        String s = (this.getValueIsAdjusting() ? "~" : "=") + this.value.toString();
        return this.getClass().getName() + " " + this.hashCode() + " " + s;
    }

    public Object clone() throws CloneNotSupportedException {
        DefaultListSelectionModel clone = (DefaultListSelectionModel)super.clone();
        clone.value = (BitSet)this.value.clone();
        clone.listenerList = new EventListenerList();
        return clone;
    }

    @Override
    @Transient
    public int getAnchorSelectionIndex() {
        return this.anchorIndex;
    }

    @Override
    @Transient
    public int getLeadSelectionIndex() {
        return this.leadIndex;
    }

    @Override
    public void setAnchorSelectionIndex(int anchorIndex) {
        this.updateLeadAnchorIndices(anchorIndex, this.leadIndex);
        this.fireValueChanged();
    }

    public void moveLeadSelectionIndex(int leadIndex) {
        if (leadIndex == -1 && this.anchorIndex != -1) {
            return;
        }
        this.updateLeadAnchorIndices(this.anchorIndex, leadIndex);
        this.fireValueChanged();
    }

    @Override
    public void setLeadSelectionIndex(int leadIndex) {
        int anchorIndex = this.anchorIndex;
        if (leadIndex == -1) {
            if (anchorIndex == -1) {
                this.updateLeadAnchorIndices(anchorIndex, leadIndex);
                this.fireValueChanged();
            }
            return;
        }
        if (anchorIndex == -1) {
            return;
        }
        if (this.leadIndex == -1) {
            this.leadIndex = leadIndex;
        }
        boolean shouldSelect = this.value.get(this.anchorIndex);
        if (this.getSelectionMode() == 0) {
            anchorIndex = leadIndex;
            shouldSelect = true;
        }
        int oldMin = Math.min(this.anchorIndex, this.leadIndex);
        int oldMax = Math.max(this.anchorIndex, this.leadIndex);
        int newMin = Math.min(anchorIndex, leadIndex);
        int newMax = Math.max(anchorIndex, leadIndex);
        this.updateLeadAnchorIndices(anchorIndex, leadIndex);
        if (shouldSelect) {
            this.changeSelection(oldMin, oldMax, newMin, newMax);
        } else {
            this.changeSelection(newMin, newMax, oldMin, oldMax, false);
        }
    }
}

