/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentEvent;

public class ContainerEvent
extends ComponentEvent {
    public static final int CONTAINER_FIRST = 300;
    public static final int CONTAINER_LAST = 301;
    public static final int COMPONENT_ADDED = 300;
    public static final int COMPONENT_REMOVED = 301;
    Component child;
    private static final long serialVersionUID = -4114942250539772041L;

    public ContainerEvent(Component source, int id, Component child) {
        super(source, id);
        this.child = child;
    }

    public Container getContainer() {
        return this.source instanceof Container ? (Container)this.source : null;
    }

    public Component getChild() {
        return this.child;
    }

    @Override
    public String paramString() {
        return (switch (this.id) {
            case 300 -> "COMPONENT_ADDED";
            case 301 -> "COMPONENT_REMOVED";
            default -> "unknown type";
        }) + ",child=" + this.child.getName();
    }
}

