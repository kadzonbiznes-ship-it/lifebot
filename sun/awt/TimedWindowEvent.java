/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.Window;
import java.awt.event.WindowEvent;

public class TimedWindowEvent
extends WindowEvent {
    private long time;

    public long getWhen() {
        return this.time;
    }

    public TimedWindowEvent(Window source, int id, Window opposite, long time) {
        super(source, id, opposite);
        this.time = time;
    }

    public TimedWindowEvent(Window source, int id, Window opposite, int oldState, int newState, long time) {
        super(source, id, opposite, oldState, newState);
        this.time = time;
    }
}

