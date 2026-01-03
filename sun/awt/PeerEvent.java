/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.event.InvocationEvent;

public class PeerEvent
extends InvocationEvent {
    public static final long PRIORITY_EVENT = 1L;
    public static final long ULTIMATE_PRIORITY_EVENT = 2L;
    public static final long LOW_PRIORITY_EVENT = 4L;
    private long flags;

    public PeerEvent(Object source, Runnable runnable, long flags) {
        this(source, runnable, null, false, flags);
    }

    public PeerEvent(Object source, Runnable runnable, Object notifier, boolean catchExceptions, long flags) {
        super(source, runnable, notifier, catchExceptions);
        this.flags = flags;
    }

    public long getFlags() {
        return this.flags;
    }

    public PeerEvent coalesceEvents(PeerEvent newEvent) {
        return null;
    }
}

