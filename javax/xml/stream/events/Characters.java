/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.stream.events;

import javax.xml.stream.events.XMLEvent;

public interface Characters
extends XMLEvent {
    public String getData();

    public boolean isWhiteSpace();

    public boolean isCData();

    public boolean isIgnorableWhiteSpace();
}

