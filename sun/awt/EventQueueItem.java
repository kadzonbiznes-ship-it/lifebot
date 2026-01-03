/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.AWTEvent;

public class EventQueueItem {
    public AWTEvent event;
    public EventQueueItem next;

    public EventQueueItem(AWTEvent evt) {
        this.event = evt;
    }
}

