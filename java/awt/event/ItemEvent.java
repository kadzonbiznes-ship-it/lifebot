/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.AWTEvent;
import java.awt.ItemSelectable;

public class ItemEvent
extends AWTEvent {
    public static final int ITEM_FIRST = 701;
    public static final int ITEM_LAST = 701;
    public static final int ITEM_STATE_CHANGED = 701;
    public static final int SELECTED = 1;
    public static final int DESELECTED = 2;
    Object item;
    int stateChange;
    private static final long serialVersionUID = -608708132447206933L;

    public ItemEvent(ItemSelectable source, int id, Object item, int stateChange) {
        super(source, id);
        this.item = item;
        this.stateChange = stateChange;
    }

    public ItemSelectable getItemSelectable() {
        return (ItemSelectable)this.source;
    }

    public Object getItem() {
        return this.item;
    }

    public int getStateChange() {
        return this.stateChange;
    }

    @Override
    public String paramString() {
        return (switch (this.id) {
            case 701 -> "ITEM_STATE_CHANGED";
            default -> "unknown type";
        }) + ",item=" + String.valueOf(this.item) + ",stateChange=" + (switch (this.stateChange) {
            case 1 -> "SELECTED";
            case 2 -> "DESELECTED";
            default -> "unknown type";
        });
    }
}

