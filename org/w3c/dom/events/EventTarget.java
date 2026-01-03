/*
 * Decompiled with CFR 0.152.
 */
package org.w3c.dom.events;

import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventException;
import org.w3c.dom.events.EventListener;

public interface EventTarget {
    public void addEventListener(String var1, EventListener var2, boolean var3);

    public void removeEventListener(String var1, EventListener var2, boolean var3);

    public boolean dispatchEvent(Event var1) throws EventException;
}

