/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.stream.util;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

public interface XMLEventAllocator {
    public XMLEventAllocator newInstance();

    public XMLEvent allocate(XMLStreamReader var1) throws XMLStreamException;

    public void allocate(XMLStreamReader var1, XMLEventConsumer var2) throws XMLStreamException;
}

