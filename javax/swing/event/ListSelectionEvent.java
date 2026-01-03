/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.event;

import java.util.EventObject;

public class ListSelectionEvent
extends EventObject {
    private int firstIndex;
    private int lastIndex;
    private boolean isAdjusting;

    public ListSelectionEvent(Object source, int firstIndex, int lastIndex, boolean isAdjusting) {
        super(source);
        this.firstIndex = firstIndex;
        this.lastIndex = lastIndex;
        this.isAdjusting = isAdjusting;
    }

    public int getFirstIndex() {
        return this.firstIndex;
    }

    public int getLastIndex() {
        return this.lastIndex;
    }

    public boolean getValueIsAdjusting() {
        return this.isAdjusting;
    }

    @Override
    public String toString() {
        String properties = " source=" + String.valueOf(this.getSource()) + " firstIndex= " + this.firstIndex + " lastIndex= " + this.lastIndex + " isAdjusting= " + this.isAdjusting + " ";
        return this.getClass().getName() + "[" + properties + "]";
    }
}

