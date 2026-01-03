/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;

public class PaintEvent
extends ComponentEvent {
    public static final int PAINT_FIRST = 800;
    public static final int PAINT_LAST = 801;
    public static final int PAINT = 800;
    public static final int UPDATE = 801;
    Rectangle updateRect;
    private static final long serialVersionUID = 1267492026433337593L;

    public PaintEvent(Component source, int id, Rectangle updateRect) {
        super(source, id);
        this.updateRect = updateRect;
    }

    public Rectangle getUpdateRect() {
        return this.updateRect;
    }

    public void setUpdateRect(Rectangle updateRect) {
        this.updateRect = updateRect;
    }

    @Override
    public String paramString() {
        return (switch (this.id) {
            case 800 -> "PAINT";
            case 801 -> "UPDATE";
            default -> "unknown type";
        }) + ",updateRect=" + (this.updateRect != null ? this.updateRect.toString() : "null");
    }
}

