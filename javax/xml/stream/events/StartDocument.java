/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.stream.events;

import javax.xml.stream.events.XMLEvent;

public interface StartDocument
extends XMLEvent {
    public String getSystemId();

    public String getCharacterEncodingScheme();

    public boolean encodingSet();

    public boolean isStandalone();

    public boolean standaloneSet();

    public String getVersion();
}

