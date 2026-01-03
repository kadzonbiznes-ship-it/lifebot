/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;

public class HierarchyEvent
extends AWTEvent {
    private static final long serialVersionUID = -5337576970038043990L;
    public static final int HIERARCHY_FIRST = 1400;
    public static final int HIERARCHY_CHANGED = 1400;
    public static final int ANCESTOR_MOVED = 1401;
    public static final int ANCESTOR_RESIZED = 1402;
    public static final int HIERARCHY_LAST = 1402;
    public static final int PARENT_CHANGED = 1;
    public static final int DISPLAYABILITY_CHANGED = 2;
    public static final int SHOWING_CHANGED = 4;
    Component changed;
    Container changedParent;
    long changeFlags;

    public HierarchyEvent(Component source, int id, Component changed, Container changedParent) {
        super(source, id);
        this.changed = changed;
        this.changedParent = changedParent;
    }

    public HierarchyEvent(Component source, int id, Component changed, Container changedParent, long changeFlags) {
        super(source, id);
        this.changed = changed;
        this.changedParent = changedParent;
        this.changeFlags = changeFlags;
    }

    public Component getComponent() {
        return this.source instanceof Component ? (Component)this.source : null;
    }

    public Component getChanged() {
        return this.changed;
    }

    public Container getChangedParent() {
        return this.changedParent;
    }

    public long getChangeFlags() {
        return this.changeFlags;
    }

    @Override
    public String paramString() {
        Object typeStr;
        switch (this.id) {
            case 1401: {
                typeStr = "ANCESTOR_MOVED (" + String.valueOf(this.changed) + "," + String.valueOf(this.changedParent) + ")";
                break;
            }
            case 1402: {
                typeStr = "ANCESTOR_RESIZED (" + String.valueOf(this.changed) + "," + String.valueOf(this.changedParent) + ")";
                break;
            }
            case 1400: {
                typeStr = "HIERARCHY_CHANGED (";
                boolean first = true;
                if ((this.changeFlags & 1L) != 0L) {
                    first = false;
                    typeStr = (String)typeStr + "PARENT_CHANGED";
                }
                if ((this.changeFlags & 2L) != 0L) {
                    if (first) {
                        first = false;
                    } else {
                        typeStr = (String)typeStr + ",";
                    }
                    typeStr = (String)typeStr + "DISPLAYABILITY_CHANGED";
                }
                if ((this.changeFlags & 4L) != 0L) {
                    if (first) {
                        first = false;
                    } else {
                        typeStr = (String)typeStr + ",";
                    }
                    typeStr = (String)typeStr + "SHOWING_CHANGED";
                }
                if (!first) {
                    typeStr = (String)typeStr + ",";
                }
                typeStr = (String)typeStr + String.valueOf(this.changed) + "," + String.valueOf(this.changedParent) + ")";
                break;
            }
            default: {
                typeStr = "unknown type";
            }
        }
        return typeStr;
    }
}

