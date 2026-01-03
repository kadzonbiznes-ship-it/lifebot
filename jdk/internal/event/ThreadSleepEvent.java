/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.event;

import jdk.internal.event.Event;

public final class ThreadSleepEvent
extends Event {
    private static final ThreadSleepEvent EVENT = new ThreadSleepEvent();
    public long time;

    public static boolean isTurnedOn() {
        return EVENT.isEnabled();
    }
}

