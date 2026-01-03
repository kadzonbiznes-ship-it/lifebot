/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.io.ObjectStreamException;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

public class FocusEvent
extends ComponentEvent {
    public static final int FOCUS_FIRST = 1004;
    public static final int FOCUS_LAST = 1005;
    public static final int FOCUS_GAINED = 1004;
    public static final int FOCUS_LOST = 1005;
    private final Cause cause;
    boolean temporary;
    transient Component opposite;
    private static final long serialVersionUID = 523753786457416396L;

    public FocusEvent(Component source, int id, boolean temporary, Component opposite) {
        this(source, id, temporary, opposite, Cause.UNKNOWN);
    }

    public FocusEvent(Component source, int id, boolean temporary, Component opposite, Cause cause) {
        super(source, id);
        if (cause == null) {
            throw new IllegalArgumentException("null cause");
        }
        this.temporary = temporary;
        this.opposite = opposite;
        this.cause = cause;
    }

    public FocusEvent(Component source, int id, boolean temporary) {
        this(source, id, temporary, null);
    }

    public FocusEvent(Component source, int id) {
        this(source, id, false);
    }

    public boolean isTemporary() {
        return this.temporary;
    }

    public Component getOppositeComponent() {
        if (this.opposite == null) {
            return null;
        }
        return SunToolkit.targetToAppContext(this.opposite) == AppContext.getAppContext() ? this.opposite : null;
    }

    @Override
    public String paramString() {
        return (switch (this.id) {
            case 1004 -> "FOCUS_GAINED";
            case 1005 -> "FOCUS_LOST";
            default -> "unknown type";
        }) + (this.temporary ? ",temporary" : ",permanent") + ",opposite=" + String.valueOf(this.getOppositeComponent()) + ",cause=" + String.valueOf((Object)this.getCause());
    }

    public final Cause getCause() {
        return this.cause;
    }

    Object readResolve() throws ObjectStreamException {
        if (this.cause != null) {
            return this;
        }
        FocusEvent focusEvent = new FocusEvent(new Component(){}, this.getID(), this.isTemporary(), this.getOppositeComponent());
        focusEvent.setSource(null);
        focusEvent.consumed = this.consumed;
        AWTAccessor.AWTEventAccessor accessor = AWTAccessor.getAWTEventAccessor();
        accessor.setBData(focusEvent, accessor.getBData(this));
        return focusEvent;
    }

    public static enum Cause {
        UNKNOWN,
        MOUSE_EVENT,
        TRAVERSAL,
        TRAVERSAL_UP,
        TRAVERSAL_DOWN,
        TRAVERSAL_FORWARD,
        TRAVERSAL_BACKWARD,
        ROLLBACK,
        UNEXPECTED,
        ACTIVATION,
        CLEAR_GLOBAL_FOCUS_OWNER;

    }
}

