/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.util.EventListenerProxy;

public class AWTEventListenerProxy
extends EventListenerProxy<AWTEventListener>
implements AWTEventListener {
    private final long eventMask;

    public AWTEventListenerProxy(long eventMask, AWTEventListener listener) {
        super(listener);
        this.eventMask = eventMask;
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        ((AWTEventListener)this.getListener()).eventDispatched(event);
    }

    public long getEventMask() {
        return this.eventMask;
    }
}

