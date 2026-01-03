/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTEvent;
import java.awt.ActiveEvent;
import java.awt.Toolkit;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

class SentEvent
extends AWTEvent
implements ActiveEvent {
    private static final long serialVersionUID = -383615247028828931L;
    static final int ID = 1007;
    boolean dispatched;
    private AWTEvent nested;
    private AppContext toNotify;

    SentEvent() {
        this((AWTEvent)null);
    }

    SentEvent(AWTEvent nested) {
        this(nested, null);
    }

    SentEvent(AWTEvent nested, AppContext toNotify) {
        super(nested != null ? nested.getSource() : Toolkit.getDefaultToolkit(), 1007);
        this.nested = nested;
        this.toNotify = toNotify;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void dispatch() {
        try {
            if (this.nested != null) {
                Toolkit.getEventQueue().dispatchEvent(this.nested);
            }
        }
        finally {
            this.dispatched = true;
            if (this.toNotify != null) {
                SunToolkit.postEvent(this.toNotify, new SentEvent());
            }
            SentEvent sentEvent = this;
            synchronized (sentEvent) {
                this.notifyAll();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void dispose() {
        this.dispatched = true;
        if (this.toNotify != null) {
            SunToolkit.postEvent(this.toNotify, new SentEvent());
        }
        SentEvent sentEvent = this;
        synchronized (sentEvent) {
            this.notifyAll();
        }
    }
}

