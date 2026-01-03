/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.stream.events;

import java.io.Writer;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

public interface XMLEvent
extends XMLStreamConstants {
    public int getEventType();

    public Location getLocation();

    public boolean isStartElement();

    public boolean isAttribute();

    public boolean isNamespace();

    public boolean isEndElement();

    public boolean isEntityReference();

    public boolean isProcessingInstruction();

    public boolean isCharacters();

    public boolean isStartDocument();

    public boolean isEndDocument();

    public StartElement asStartElement();

    public EndElement asEndElement();

    public Characters asCharacters();

    public QName getSchemaType();

    public void writeAsEncodedUnicode(Writer var1) throws XMLStreamException;
}

