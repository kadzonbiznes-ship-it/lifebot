/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.stream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

public interface XMLStreamReader
extends XMLStreamConstants {
    public Object getProperty(String var1) throws IllegalArgumentException;

    public int next() throws XMLStreamException;

    public void require(int var1, String var2, String var3) throws XMLStreamException;

    public String getElementText() throws XMLStreamException;

    public int nextTag() throws XMLStreamException;

    public boolean hasNext() throws XMLStreamException;

    public void close() throws XMLStreamException;

    public String getNamespaceURI(String var1);

    public boolean isStartElement();

    public boolean isEndElement();

    public boolean isCharacters();

    public boolean isWhiteSpace();

    public String getAttributeValue(String var1, String var2);

    public int getAttributeCount();

    public QName getAttributeName(int var1);

    public String getAttributeNamespace(int var1);

    public String getAttributeLocalName(int var1);

    public String getAttributePrefix(int var1);

    public String getAttributeType(int var1);

    public String getAttributeValue(int var1);

    public boolean isAttributeSpecified(int var1);

    public int getNamespaceCount();

    public String getNamespacePrefix(int var1);

    public String getNamespaceURI(int var1);

    public NamespaceContext getNamespaceContext();

    public int getEventType();

    public String getText();

    public char[] getTextCharacters();

    public int getTextCharacters(int var1, char[] var2, int var3, int var4) throws XMLStreamException;

    public int getTextStart();

    public int getTextLength();

    public String getEncoding();

    public boolean hasText();

    public Location getLocation();

    public QName getName();

    public String getLocalName();

    public boolean hasName();

    public String getNamespaceURI();

    public String getPrefix();

    public String getVersion();

    public boolean isStandalone();

    public boolean standaloneSet();

    public String getCharacterEncodingScheme();

    public String getPITarget();

    public String getPIData();
}

