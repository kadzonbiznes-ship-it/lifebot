/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.dnd;

import java.awt.Component;
import java.awt.event.MouseEvent;
import sun.awt.dnd.SunDropTargetContextPeer;

public final class SunDropTargetEvent
extends MouseEvent {
    public static final int MOUSE_DROPPED = 502;
    private final SunDropTargetContextPeer.EventDispatcher dispatcher;

    public SunDropTargetEvent(Component source, int id, int x, int y, SunDropTargetContextPeer.EventDispatcher d) {
        super(source, id, System.currentTimeMillis(), 0, x, y, 0, 0, 0, false, 0);
        this.dispatcher = d;
        this.dispatcher.registerEvent(this);
    }

    public void dispatch() {
        try {
            this.dispatcher.dispatchEvent(this);
        }
        finally {
            this.dispose();
        }
    }

    @Override
    public void consume() {
        boolean was_consumed = this.isConsumed();
        super.consume();
        if (!was_consumed && this.isConsumed()) {
            this.dispose();
        }
    }

    public void dispose() {
        this.dispatcher.unregisterEvent(this);
    }

    public SunDropTargetContextPeer.EventDispatcher getDispatcher() {
        return this.dispatcher;
    }

    @Override
    public String paramString() {
        String typeStr = null;
        switch (this.id) {
            case 502: {
                typeStr = "MOUSE_DROPPED";
                break;
            }
            default: {
                return super.paramString();
            }
        }
        return typeStr + ",(" + this.getX() + "," + this.getY() + ")";
    }
}

