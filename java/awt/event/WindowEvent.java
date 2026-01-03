/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.Window;
import java.awt.event.ComponentEvent;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

public class WindowEvent
extends ComponentEvent {
    public static final int WINDOW_FIRST = 200;
    public static final int WINDOW_OPENED = 200;
    public static final int WINDOW_CLOSING = 201;
    public static final int WINDOW_CLOSED = 202;
    public static final int WINDOW_ICONIFIED = 203;
    public static final int WINDOW_DEICONIFIED = 204;
    public static final int WINDOW_ACTIVATED = 205;
    public static final int WINDOW_DEACTIVATED = 206;
    public static final int WINDOW_GAINED_FOCUS = 207;
    public static final int WINDOW_LOST_FOCUS = 208;
    public static final int WINDOW_STATE_CHANGED = 209;
    public static final int WINDOW_LAST = 209;
    transient Window opposite;
    int oldState;
    int newState;
    private static final long serialVersionUID = -1567959133147912127L;

    public WindowEvent(Window source, int id, Window opposite, int oldState, int newState) {
        super(source, id);
        this.opposite = opposite;
        this.oldState = oldState;
        this.newState = newState;
    }

    public WindowEvent(Window source, int id, Window opposite) {
        this(source, id, opposite, 0, 0);
    }

    public WindowEvent(Window source, int id, int oldState, int newState) {
        this(source, id, null, oldState, newState);
    }

    public WindowEvent(Window source, int id) {
        this(source, id, null, 0, 0);
    }

    public Window getWindow() {
        return this.source instanceof Window ? (Window)this.source : null;
    }

    public Window getOppositeWindow() {
        if (this.opposite == null) {
            return null;
        }
        return SunToolkit.targetToAppContext(this.opposite) == AppContext.getAppContext() ? this.opposite : null;
    }

    public int getOldState() {
        return this.oldState;
    }

    public int getNewState() {
        return this.newState;
    }

    @Override
    public String paramString() {
        Object typeStr = switch (this.id) {
            case 200 -> "WINDOW_OPENED";
            case 201 -> "WINDOW_CLOSING";
            case 202 -> "WINDOW_CLOSED";
            case 203 -> "WINDOW_ICONIFIED";
            case 204 -> "WINDOW_DEICONIFIED";
            case 205 -> "WINDOW_ACTIVATED";
            case 206 -> "WINDOW_DEACTIVATED";
            case 207 -> "WINDOW_GAINED_FOCUS";
            case 208 -> "WINDOW_LOST_FOCUS";
            case 209 -> "WINDOW_STATE_CHANGED";
            default -> "unknown type";
        };
        typeStr = (String)typeStr + ",opposite=" + String.valueOf(this.getOppositeWindow()) + ",oldState=" + this.oldState + ",newState=" + this.newState;
        return typeStr;
    }
}

