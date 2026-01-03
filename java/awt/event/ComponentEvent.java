/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Rectangle;

public class ComponentEvent
extends AWTEvent {
    public static final int COMPONENT_FIRST = 100;
    public static final int COMPONENT_LAST = 103;
    public static final int COMPONENT_MOVED = 100;
    public static final int COMPONENT_RESIZED = 101;
    public static final int COMPONENT_SHOWN = 102;
    public static final int COMPONENT_HIDDEN = 103;
    private static final long serialVersionUID = 8101406823902992965L;

    public ComponentEvent(Component source, int id) {
        super(source, id);
    }

    public Component getComponent() {
        return this.source instanceof Component ? (Component)this.source : null;
    }

    @Override
    public String paramString() {
        Rectangle b = this.source != null ? ((Component)this.source).getBounds() : null;
        return switch (this.id) {
            case 102 -> "COMPONENT_SHOWN";
            case 103 -> "COMPONENT_HIDDEN";
            case 100 -> "COMPONENT_MOVED (" + b.x + "," + b.y + " " + b.width + "x" + b.height + ")";
            case 101 -> "COMPONENT_RESIZED (" + b.x + "," + b.y + " " + b.width + "x" + b.height + ")";
            default -> "unknown type";
        };
    }
}

