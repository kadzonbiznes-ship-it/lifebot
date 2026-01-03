/*
 * Decompiled with CFR 0.152.
 */
package org.w3c.dom.events;

import org.w3c.dom.DOMException;
import org.w3c.dom.events.Event;

public interface DocumentEvent {
    public Event createEvent(String var1) throws DOMException;
}

