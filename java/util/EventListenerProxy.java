/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.util.EventListener;

public abstract class EventListenerProxy<T extends EventListener>
implements EventListener {
    private final T listener;

    public EventListenerProxy(T listener) {
        this.listener = listener;
    }

    public T getListener() {
        return this.listener;
    }
}

