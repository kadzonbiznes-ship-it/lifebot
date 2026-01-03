/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.internal.stream.events;

import java.io.IOException;
import java.io.Writer;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public abstract class DummyEvent
implements XMLEvent {
    private static DummyLocation nowhere = new DummyLocation();
    private int fEventType;
    protected Location fLocation = nowhere;

    public DummyEvent() {
    }

    public DummyEvent(int i) {
        this.fEventType = i;
    }

    @Override
    public int getEventType() {
        return this.fEventType;
    }

    protected void setEventType(int eventType) {
        this.fEventType = eventType;
    }

    @Override
    public boolean isStartElement() {
        return this.fEventType == 1;
    }

    @Override
    public boolean isEndElement() {
        return this.fEventType == 2;
    }

    @Override
    public boolean isEntityReference() {
        return this.fEventType == 9;
    }

    @Override
    public boolean isProcessingInstruction() {
        return this.fEventType == 3;
    }

    public boolean isCharacterData() {
        return this.fEventType == 4;
    }

    @Override
    public boolean isStartDocument() {
        return this.fEventType == 7;
    }

    @Override
    public boolean isEndDocument() {
        return this.fEventType == 8;
    }

    @Override
    public Location getLocation() {
        return this.fLocation;
    }

    void setLocation(Location loc) {
        this.fLocation = loc == null ? nowhere : loc;
    }

    @Override
    public Characters asCharacters() {
        return (Characters)((Object)this);
    }

    @Override
    public EndElement asEndElement() {
        return (EndElement)((Object)this);
    }

    @Override
    public StartElement asStartElement() {
        return (StartElement)((Object)this);
    }

    @Override
    public QName getSchemaType() {
        return null;
    }

    @Override
    public boolean isAttribute() {
        return this.fEventType == 10;
    }

    @Override
    public boolean isCharacters() {
        return this.fEventType == 4;
    }

    @Override
    public boolean isNamespace() {
        return this.fEventType == 13;
    }

    @Override
    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
        try {
            this.writeAsEncodedUnicodeEx(writer);
        }
        catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    protected abstract void writeAsEncodedUnicodeEx(Writer var1) throws IOException, XMLStreamException;

    protected void charEncode(Writer writer, String data) throws IOException {
        if (data == null || data == "") {
            return;
        }
        int start = 0;
        int len = data.length();
        block6: for (int i = 0; i < len; ++i) {
            switch (data.charAt(i)) {
                case '<': {
                    writer.write(data, start, i - start);
                    writer.write("&lt;");
                    start = i + 1;
                    continue block6;
                }
                case '&': {
                    writer.write(data, start, i - start);
                    writer.write("&amp;");
                    start = i + 1;
                    continue block6;
                }
                case '>': {
                    writer.write(data, start, i - start);
                    writer.write("&gt;");
                    start = i + 1;
                    continue block6;
                }
                case '\"': {
                    writer.write(data, start, i - start);
                    writer.write("&quot;");
                    start = i + 1;
                }
            }
        }
        writer.write(data, start, len - start);
    }

    static class DummyLocation
    implements Location {
        @Override
        public int getCharacterOffset() {
            return -1;
        }

        @Override
        public int getColumnNumber() {
            return -1;
        }

        @Override
        public int getLineNumber() {
            return -1;
        }

        @Override
        public String getPublicId() {
            return null;
        }

        @Override
        public String getSystemId() {
            return null;
        }
    }
}

