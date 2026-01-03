/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.impl;

import com.sun.org.apache.xerces.internal.impl.PropertyManager;
import com.sun.org.apache.xerces.internal.impl.XML11NSDocumentScannerImpl;
import com.sun.org.apache.xerces.internal.impl.XMLDTDScannerImpl;
import com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl;
import com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import com.sun.org.apache.xerces.internal.impl.XMLEntityScanner;
import com.sun.org.apache.xerces.internal.impl.XMLNSDocumentScannerImpl;
import com.sun.org.apache.xerces.internal.util.NamespaceContextWrapper;
import com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.util.XMLAttributesIteratorImpl;
import com.sun.org.apache.xerces.internal.util.XMLChar;
import com.sun.org.apache.xerces.internal.util.XMLStringBuffer;
import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import com.sun.xml.internal.stream.Entity;
import com.sun.xml.internal.stream.StaxErrorReporter;
import com.sun.xml.internal.stream.XMLEntityStorage;
import com.sun.xml.internal.stream.dtd.nonvalidating.DTDGrammar;
import com.sun.xml.internal.stream.dtd.nonvalidating.XMLNotationDecl;
import com.sun.xml.internal.stream.events.EntityDeclarationImpl;
import com.sun.xml.internal.stream.events.NotationDeclarationImpl;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.NotationDeclaration;

public class XMLStreamReaderImpl
implements XMLStreamReader {
    protected static final String ENTITY_MANAGER = "http://apache.org/xml/properties/internal/entity-manager";
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    protected static final String READER_IN_DEFINED_STATE = "http://java.sun.com/xml/stream/properties/reader-in-defined-state";
    private SymbolTable fSymbolTable = new SymbolTable();
    protected XMLDocumentScannerImpl fScanner = new XMLNSDocumentScannerImpl();
    protected NamespaceContextWrapper fNamespaceContextWrapper = new NamespaceContextWrapper((NamespaceSupport)this.fScanner.getNamespaceContext());
    protected XMLEntityManager fEntityManager = new XMLEntityManager();
    protected StaxErrorReporter fErrorReporter = new StaxErrorReporter();
    protected XMLEntityScanner fEntityScanner = null;
    protected XMLInputSource fInputSource = null;
    protected PropertyManager fPropertyManager = null;
    private int fEventType;
    static final boolean DEBUG = false;
    private boolean fReuse = true;
    private boolean fReaderInDefinedState = true;
    private String fDTDDecl = null;
    private String versionStr = null;

    public XMLStreamReaderImpl(InputStream inputStream, PropertyManager props) throws XMLStreamException {
        this.init(props);
        XMLInputSource inputSource = new XMLInputSource(null, null, null, inputStream, null);
        this.setInputSource(inputSource);
    }

    public XMLDocumentScannerImpl getScanner() {
        System.out.println("returning scanner");
        return this.fScanner;
    }

    public XMLStreamReaderImpl(String systemid, PropertyManager props) throws XMLStreamException {
        this.init(props);
        XMLInputSource inputSource = new XMLInputSource(null, systemid, null, false);
        this.setInputSource(inputSource);
    }

    public XMLStreamReaderImpl(InputStream inputStream, String encoding, PropertyManager props) throws XMLStreamException {
        this.init(props);
        XMLInputSource inputSource = new XMLInputSource(null, null, null, new BufferedInputStream(inputStream), encoding);
        this.setInputSource(inputSource);
    }

    public XMLStreamReaderImpl(Reader reader, PropertyManager props) throws XMLStreamException {
        this.init(props);
        XMLInputSource inputSource = new XMLInputSource(null, null, null, new BufferedReader(reader), null);
        this.setInputSource(inputSource);
    }

    public XMLStreamReaderImpl(XMLInputSource inputSource, PropertyManager props) throws XMLStreamException {
        this.init(props);
        this.setInputSource(inputSource);
    }

    public final void setInputSource(XMLInputSource inputSource) throws XMLStreamException {
        this.fReuse = false;
        try {
            this.fScanner.setInputSource(inputSource);
            if (this.fReaderInDefinedState) {
                this.fEventType = this.fScanner.next();
                if (this.versionStr == null) {
                    this.versionStr = this.getVersion();
                }
                if (this.fEventType == 7 && this.versionStr != null && this.versionStr.equals("1.1")) {
                    this.switchToXML11Scanner();
                }
            }
        }
        catch (IOException ex) {
            throw new XMLStreamException(ex);
        }
        catch (XNIException ex) {
            throw new XMLStreamException(ex.getMessage(), this.getLocation(), ex.getException());
        }
    }

    final void init(PropertyManager propertyManager) throws XMLStreamException {
        this.fPropertyManager = propertyManager;
        propertyManager.setProperty(SYMBOL_TABLE, this.fSymbolTable);
        propertyManager.setProperty(ERROR_REPORTER, this.fErrorReporter);
        propertyManager.setProperty(ENTITY_MANAGER, this.fEntityManager);
        this.reset();
    }

    public boolean canReuse() {
        return this.fReuse;
    }

    public void reset() {
        this.fReuse = true;
        this.fEventType = 0;
        this.fEntityManager.reset(this.fPropertyManager);
        this.fScanner.reset(this.fPropertyManager);
        this.fDTDDecl = null;
        this.fEntityScanner = this.fEntityManager.getEntityScanner();
        this.fReaderInDefinedState = (Boolean)this.fPropertyManager.getProperty(READER_IN_DEFINED_STATE);
        this.versionStr = null;
    }

    @Override
    public void close() throws XMLStreamException {
        this.fReuse = true;
    }

    @Override
    public String getCharacterEncodingScheme() {
        return this.fScanner.getCharacterEncodingScheme();
    }

    public int getColumnNumber() {
        return this.fEntityScanner.getColumnNumber();
    }

    @Override
    public String getEncoding() {
        return this.fEntityScanner.getEncoding();
    }

    @Override
    public int getEventType() {
        return this.fEventType;
    }

    public int getLineNumber() {
        return this.fEntityScanner.getLineNumber();
    }

    @Override
    public String getLocalName() {
        if (this.fEventType == 1 || this.fEventType == 2) {
            return this.fScanner.getElementQName().localpart;
        }
        if (this.fEventType == 9) {
            return this.fScanner.getEntityName();
        }
        throw new IllegalStateException("Method getLocalName() cannot be called for " + XMLStreamReaderImpl.getEventTypeString(this.fEventType) + " event.");
    }

    @Override
    public String getNamespaceURI() {
        if (this.fEventType == 1 || this.fEventType == 2) {
            return this.fScanner.getElementQName().uri;
        }
        return null;
    }

    @Override
    public String getPIData() {
        if (this.fEventType == 3) {
            return this.fScanner.getPIData().toString();
        }
        throw new IllegalStateException("Current state of the parser is " + XMLStreamReaderImpl.getEventTypeString(this.fEventType) + " But Expected state is 3");
    }

    @Override
    public String getPITarget() {
        if (this.fEventType == 3) {
            return this.fScanner.getPITarget();
        }
        throw new IllegalStateException("Current state of the parser is " + XMLStreamReaderImpl.getEventTypeString(this.fEventType) + " But Expected state is 3");
    }

    @Override
    public String getPrefix() {
        if (this.fEventType == 1 || this.fEventType == 2) {
            String prefix = this.fScanner.getElementQName().prefix;
            return prefix == null ? "" : prefix;
        }
        return null;
    }

    @Override
    public char[] getTextCharacters() {
        if (this.fEventType == 4 || this.fEventType == 5 || this.fEventType == 12 || this.fEventType == 6) {
            return this.fScanner.getCharacterData().ch;
        }
        throw new IllegalStateException("Current state = " + XMLStreamReaderImpl.getEventTypeString(this.fEventType) + " is not among the states " + XMLStreamReaderImpl.getEventTypeString(4) + " , " + XMLStreamReaderImpl.getEventTypeString(5) + " , " + XMLStreamReaderImpl.getEventTypeString(12) + " , " + XMLStreamReaderImpl.getEventTypeString(6) + " valid for getTextCharacters() ");
    }

    @Override
    public int getTextLength() {
        if (this.fEventType == 4 || this.fEventType == 5 || this.fEventType == 12 || this.fEventType == 6) {
            return this.fScanner.getCharacterData().length;
        }
        throw new IllegalStateException("Current state = " + XMLStreamReaderImpl.getEventTypeString(this.fEventType) + " is not among the states " + XMLStreamReaderImpl.getEventTypeString(4) + " , " + XMLStreamReaderImpl.getEventTypeString(5) + " , " + XMLStreamReaderImpl.getEventTypeString(12) + " , " + XMLStreamReaderImpl.getEventTypeString(6) + " valid for getTextLength() ");
    }

    @Override
    public int getTextStart() {
        if (this.fEventType == 4 || this.fEventType == 5 || this.fEventType == 12 || this.fEventType == 6) {
            return this.fScanner.getCharacterData().offset;
        }
        throw new IllegalStateException("Current state = " + XMLStreamReaderImpl.getEventTypeString(this.fEventType) + " is not among the states " + XMLStreamReaderImpl.getEventTypeString(4) + " , " + XMLStreamReaderImpl.getEventTypeString(5) + " , " + XMLStreamReaderImpl.getEventTypeString(12) + " , " + XMLStreamReaderImpl.getEventTypeString(6) + " valid for getTextStart() ");
    }

    public String getValue() {
        if (this.fEventType == 3) {
            return this.fScanner.getPIData().toString();
        }
        if (this.fEventType == 5) {
            return this.fScanner.getComment();
        }
        if (this.fEventType == 1 || this.fEventType == 2) {
            return this.fScanner.getElementQName().localpart;
        }
        if (this.fEventType == 4) {
            return this.fScanner.getCharacterData().toString();
        }
        return null;
    }

    @Override
    public String getVersion() {
        String version = this.fEntityScanner.getXMLVersion();
        return "1.0".equals(version) && !this.fEntityScanner.xmlVersionSetExplicitly ? null : version;
    }

    public boolean hasAttributes() {
        return this.fScanner.getAttributeIterator().getLength() > 0;
    }

    @Override
    public boolean hasName() {
        return this.fEventType == 1 || this.fEventType == 2;
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        if (this.fEventType == -1) {
            return false;
        }
        return this.fEventType != 8;
    }

    public boolean hasValue() {
        return this.fEventType == 1 || this.fEventType == 2 || this.fEventType == 9 || this.fEventType == 3 || this.fEventType == 5 || this.fEventType == 4;
    }

    @Override
    public boolean isEndElement() {
        return this.fEventType == 2;
    }

    @Override
    public boolean isStandalone() {
        return this.fScanner.isStandAlone();
    }

    @Override
    public boolean isStartElement() {
        return this.fEventType == 1;
    }

    @Override
    public boolean isWhiteSpace() {
        if (this.isCharacters() || this.fEventType == 12) {
            char[] ch = this.getTextCharacters();
            int start = this.getTextStart();
            int end = start + this.getTextLength();
            for (int i = start; i < end; ++i) {
                if (XMLChar.isSpace(ch[i])) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int next() throws XMLStreamException {
        if (!this.hasNext()) {
            if (this.fEventType != -1) {
                throw new NoSuchElementException("END_DOCUMENT reached: no more elements on the stream.");
            }
            throw new XMLStreamException("Error processing input source. The input stream is not complete.");
        }
        try {
            this.fEventType = this.fScanner.next();
            if (this.versionStr == null) {
                this.versionStr = this.getVersion();
            }
            if (this.fEventType == 7 && this.versionStr != null && this.versionStr.equals("1.1")) {
                this.switchToXML11Scanner();
            }
            if (this.fEventType == 4 || this.fEventType == 9 || this.fEventType == 3 || this.fEventType == 5 || this.fEventType == 12) {
                this.fEntityScanner.checkNodeCount(this.fEntityScanner.fCurrentEntity);
            }
            return this.fEventType;
        }
        catch (IOException ex) {
            Boolean isValidating;
            if (this.fScanner.fScannerState == 46 && (isValidating = (Boolean)this.fPropertyManager.getProperty("javax.xml.stream.isValidating")) != null && !isValidating.booleanValue()) {
                this.fEventType = 11;
                this.fScanner.setScannerState(43);
                this.fScanner.setDriver(this.fScanner.fPrologDriver);
                if (this.fDTDDecl == null || this.fDTDDecl.length() == 0) {
                    this.fDTDDecl = "<!-- Exception scanning External DTD Subset.  True contents of DTD cannot be determined.  Processing will continue as XMLInputFactory.IS_VALIDATING == false. -->";
                }
                return 11;
            }
            throw new XMLStreamException(ex.getMessage(), this.getLocation(), ex);
        }
        catch (XNIException ex) {
            throw new XMLStreamException(ex.getMessage(), this.getLocation(), ex.getException());
        }
    }

    private void switchToXML11Scanner() throws IOException {
        int oldEntityDepth = this.fScanner.fEntityDepth;
        NamespaceContext oldNamespaceContext = this.fScanner.fNamespaceContext;
        this.fScanner = new XML11NSDocumentScannerImpl();
        this.fScanner.reset(this.fPropertyManager);
        this.fScanner.setPropertyManager(this.fPropertyManager);
        this.fEntityScanner = this.fEntityManager.getEntityScanner();
        this.fEntityScanner.registerListener(this.fScanner);
        this.fEntityManager.fCurrentEntity.mayReadChunks = true;
        this.fScanner.setScannerState(7);
        this.fScanner.fEntityDepth = oldEntityDepth;
        this.fScanner.fNamespaceContext = oldNamespaceContext;
        this.fEventType = this.fScanner.next();
    }

    static final String getEventTypeString(int eventType) {
        switch (eventType) {
            case 1: {
                return "START_ELEMENT";
            }
            case 2: {
                return "END_ELEMENT";
            }
            case 3: {
                return "PROCESSING_INSTRUCTION";
            }
            case 4: {
                return "CHARACTERS";
            }
            case 5: {
                return "COMMENT";
            }
            case 7: {
                return "START_DOCUMENT";
            }
            case 8: {
                return "END_DOCUMENT";
            }
            case 9: {
                return "ENTITY_REFERENCE";
            }
            case 10: {
                return "ATTRIBUTE";
            }
            case 11: {
                return "DTD";
            }
            case 12: {
                return "CDATA";
            }
            case 6: {
                return "SPACE";
            }
        }
        return "UNKNOWN_EVENT_TYPE, " + String.valueOf(eventType);
    }

    @Override
    public int getAttributeCount() {
        if (this.fEventType == 1 || this.fEventType == 10) {
            return this.fScanner.getAttributeIterator().getLength();
        }
        throw new IllegalStateException("Current state is not among the states " + XMLStreamReaderImpl.getEventTypeString(1) + " , " + XMLStreamReaderImpl.getEventTypeString(10) + "valid for getAttributeCount()");
    }

    @Override
    public javax.xml.namespace.QName getAttributeName(int index) {
        if (this.fEventType == 1 || this.fEventType == 10) {
            return this.convertXNIQNametoJavaxQName(this.fScanner.getAttributeIterator().getQualifiedName(index));
        }
        throw new IllegalStateException("Current state is not among the states " + XMLStreamReaderImpl.getEventTypeString(1) + " , " + XMLStreamReaderImpl.getEventTypeString(10) + "valid for getAttributeName()");
    }

    @Override
    public String getAttributeLocalName(int index) {
        if (this.fEventType == 1 || this.fEventType == 10) {
            return this.fScanner.getAttributeIterator().getLocalName(index);
        }
        throw new IllegalStateException();
    }

    @Override
    public String getAttributeNamespace(int index) {
        if (this.fEventType == 1 || this.fEventType == 10) {
            return this.fScanner.getAttributeIterator().getURI(index);
        }
        throw new IllegalStateException("Current state is not among the states " + XMLStreamReaderImpl.getEventTypeString(1) + " , " + XMLStreamReaderImpl.getEventTypeString(10) + "valid for getAttributeNamespace()");
    }

    @Override
    public String getAttributePrefix(int index) {
        if (this.fEventType == 1 || this.fEventType == 10) {
            return this.fScanner.getAttributeIterator().getPrefix(index);
        }
        throw new IllegalStateException("Current state is not among the states " + XMLStreamReaderImpl.getEventTypeString(1) + " , " + XMLStreamReaderImpl.getEventTypeString(10) + "valid for getAttributePrefix()");
    }

    public javax.xml.namespace.QName getAttributeQName(int index) {
        if (this.fEventType == 1 || this.fEventType == 10) {
            String localName = this.fScanner.getAttributeIterator().getLocalName(index);
            String uri = this.fScanner.getAttributeIterator().getURI(index);
            return new javax.xml.namespace.QName(uri, localName);
        }
        throw new IllegalStateException("Current state is not among the states " + XMLStreamReaderImpl.getEventTypeString(1) + " , " + XMLStreamReaderImpl.getEventTypeString(10) + "valid for getAttributeQName()");
    }

    @Override
    public String getAttributeType(int index) {
        if (this.fEventType == 1 || this.fEventType == 10) {
            return this.fScanner.getAttributeIterator().getType(index);
        }
        throw new IllegalStateException("Current state is not among the states " + XMLStreamReaderImpl.getEventTypeString(1) + " , " + XMLStreamReaderImpl.getEventTypeString(10) + "valid for getAttributeType()");
    }

    @Override
    public String getAttributeValue(int index) {
        if (this.fEventType == 1 || this.fEventType == 10) {
            return this.fScanner.getAttributeIterator().getValue(index);
        }
        throw new IllegalStateException("Current state is not among the states " + XMLStreamReaderImpl.getEventTypeString(1) + " , " + XMLStreamReaderImpl.getEventTypeString(10) + "valid for getAttributeValue()");
    }

    @Override
    public String getAttributeValue(String namespaceURI, String localName) {
        if (this.fEventType == 1 || this.fEventType == 10) {
            XMLAttributesIteratorImpl attributes = this.fScanner.getAttributeIterator();
            if (namespaceURI == null) {
                return attributes.getValue(attributes.getIndexByLocalName(localName));
            }
            return this.fScanner.getAttributeIterator().getValue(namespaceURI.length() == 0 ? null : namespaceURI, localName);
        }
        throw new IllegalStateException("Current state is not among the states " + XMLStreamReaderImpl.getEventTypeString(1) + " , " + XMLStreamReaderImpl.getEventTypeString(10) + "valid for getAttributeValue()");
    }

    @Override
    public String getElementText() throws XMLStreamException {
        if (this.getEventType() != 1) {
            throw new XMLStreamException("parser must be on START_ELEMENT to read next text", this.getLocation());
        }
        int eventType = this.next();
        StringBuilder content = new StringBuilder();
        while (eventType != 2) {
            if (eventType == 4 || eventType == 12 || eventType == 6 || eventType == 9) {
                content.append(this.getText());
            } else if (eventType != 3 && eventType != 5) {
                if (eventType == 8) {
                    throw new XMLStreamException("unexpected end of document when reading element text content");
                }
                if (eventType == 1) {
                    throw new XMLStreamException("elementGetText() function expects text only elment but START_ELEMENT was encountered.", this.getLocation());
                }
                throw new XMLStreamException("Unexpected event type " + eventType, this.getLocation());
            }
            eventType = this.next();
        }
        return content.toString();
    }

    @Override
    public Location getLocation() {
        return new Location(){
            String _systemId;
            String _publicId;
            int _offset;
            int _columnNumber;
            int _lineNumber;
            {
                this._systemId = XMLStreamReaderImpl.this.fEntityScanner.getExpandedSystemId();
                this._publicId = XMLStreamReaderImpl.this.fEntityScanner.getPublicId();
                this._offset = XMLStreamReaderImpl.this.fEntityScanner.getCharacterOffset();
                this._columnNumber = XMLStreamReaderImpl.this.fEntityScanner.getColumnNumber();
                this._lineNumber = XMLStreamReaderImpl.this.fEntityScanner.getLineNumber();
            }

            public String getLocationURI() {
                return this._systemId;
            }

            @Override
            public int getCharacterOffset() {
                return this._offset;
            }

            @Override
            public int getColumnNumber() {
                return this._columnNumber;
            }

            @Override
            public int getLineNumber() {
                return this._lineNumber;
            }

            @Override
            public String getPublicId() {
                return this._publicId;
            }

            @Override
            public String getSystemId() {
                return this._systemId;
            }

            public String toString() {
                StringBuilder sbuffer = new StringBuilder();
                sbuffer.append("Line number = " + this.getLineNumber());
                sbuffer.append("\n");
                sbuffer.append("Column number = " + this.getColumnNumber());
                sbuffer.append("\n");
                sbuffer.append("System Id = " + this.getSystemId());
                sbuffer.append("\n");
                sbuffer.append("Public Id = " + this.getPublicId());
                sbuffer.append("\n");
                sbuffer.append("Location Uri= " + this.getLocationURI());
                sbuffer.append("\n");
                sbuffer.append("CharacterOffset = " + this.getCharacterOffset());
                sbuffer.append("\n");
                return sbuffer.toString();
            }
        };
    }

    @Override
    public javax.xml.namespace.QName getName() {
        if (this.fEventType == 1 || this.fEventType == 2) {
            return this.convertXNIQNametoJavaxQName(this.fScanner.getElementQName());
        }
        throw new IllegalStateException("Illegal to call getName() when event type is " + XMLStreamReaderImpl.getEventTypeString(this.fEventType) + ". Valid states are " + XMLStreamReaderImpl.getEventTypeString(1) + ", " + XMLStreamReaderImpl.getEventTypeString(2));
    }

    @Override
    public javax.xml.namespace.NamespaceContext getNamespaceContext() {
        return this.fNamespaceContextWrapper;
    }

    @Override
    public int getNamespaceCount() {
        if (this.fEventType == 1 || this.fEventType == 2 || this.fEventType == 13) {
            return this.fScanner.getNamespaceContext().getDeclaredPrefixCount();
        }
        throw new IllegalStateException("Current event state is " + XMLStreamReaderImpl.getEventTypeString(this.fEventType) + " is not among the states " + XMLStreamReaderImpl.getEventTypeString(1) + ", " + XMLStreamReaderImpl.getEventTypeString(2) + ", " + XMLStreamReaderImpl.getEventTypeString(13) + " valid for getNamespaceCount().");
    }

    @Override
    public String getNamespacePrefix(int index) {
        if (this.fEventType == 1 || this.fEventType == 2 || this.fEventType == 13) {
            String prefix = this.fScanner.getNamespaceContext().getDeclaredPrefixAt(index);
            return prefix.equals("") ? null : prefix;
        }
        throw new IllegalStateException("Current state " + XMLStreamReaderImpl.getEventTypeString(this.fEventType) + " is not among the states " + XMLStreamReaderImpl.getEventTypeString(1) + ", " + XMLStreamReaderImpl.getEventTypeString(2) + ", " + XMLStreamReaderImpl.getEventTypeString(13) + " valid for getNamespacePrefix().");
    }

    @Override
    public String getNamespaceURI(int index) {
        if (this.fEventType == 1 || this.fEventType == 2 || this.fEventType == 13) {
            return this.fScanner.getNamespaceContext().getURI(this.fScanner.getNamespaceContext().getDeclaredPrefixAt(index));
        }
        throw new IllegalStateException("Current state " + XMLStreamReaderImpl.getEventTypeString(this.fEventType) + " is not among the states " + XMLStreamReaderImpl.getEventTypeString(1) + ", " + XMLStreamReaderImpl.getEventTypeString(2) + ", " + XMLStreamReaderImpl.getEventTypeString(13) + " valid for getNamespaceURI().");
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        if (this.fPropertyManager != null) {
            if (name.equals("javax.xml.stream.notations")) {
                return this.getNotationDecls();
            }
            if (name.equals("javax.xml.stream.entities")) {
                return this.getEntityDecls();
            }
            return this.fPropertyManager.getProperty(name);
        }
        return null;
    }

    @Override
    public String getText() {
        if (this.fEventType == 4 || this.fEventType == 5 || this.fEventType == 12 || this.fEventType == 6) {
            return this.fScanner.getCharacterData().toString();
        }
        if (this.fEventType == 9) {
            String name = this.fScanner.getEntityName();
            if (name != null) {
                if (this.fScanner.foundBuiltInRefs) {
                    return this.fScanner.getCharacterData().toString();
                }
                XMLEntityStorage entityStore = this.fEntityManager.getEntityStore();
                Entity en = entityStore.getEntity(name);
                if (en == null) {
                    return null;
                }
                if (en.isExternal()) {
                    return ((Entity.ExternalEntity)en).entityLocation.getExpandedSystemId();
                }
                return ((Entity.InternalEntity)en).text;
            }
            return null;
        }
        if (this.fEventType == 11) {
            if (this.fDTDDecl != null) {
                return this.fDTDDecl;
            }
            XMLStringBuffer tmpBuffer = this.fScanner.getDTDDecl();
            this.fDTDDecl = tmpBuffer.toString();
            return this.fDTDDecl;
        }
        throw new IllegalStateException("Current state " + XMLStreamReaderImpl.getEventTypeString(this.fEventType) + " is not among the states" + XMLStreamReaderImpl.getEventTypeString(4) + ", " + XMLStreamReaderImpl.getEventTypeString(5) + ", " + XMLStreamReaderImpl.getEventTypeString(12) + ", " + XMLStreamReaderImpl.getEventTypeString(6) + ", " + XMLStreamReaderImpl.getEventTypeString(9) + ", " + XMLStreamReaderImpl.getEventTypeString(11) + " valid for getText() ");
    }

    @Override
    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        if (type != this.fEventType) {
            throw new XMLStreamException("Event type " + XMLStreamReaderImpl.getEventTypeString(type) + " specified did not match with current parser event " + XMLStreamReaderImpl.getEventTypeString(this.fEventType));
        }
        if (namespaceURI != null && !namespaceURI.equals(this.getNamespaceURI())) {
            throw new XMLStreamException("Namespace URI " + namespaceURI + " specified did not match with current namespace URI");
        }
        if (localName != null && !localName.equals(this.getLocalName())) {
            throw new XMLStreamException("LocalName " + localName + " specified did not match with current local name");
        }
    }

    @Override
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        if (target == null) {
            throw new NullPointerException("target char array can't be null");
        }
        if (targetStart < 0 || length < 0 || sourceStart < 0 || targetStart >= target.length || targetStart + length > target.length) {
            throw new IndexOutOfBoundsException();
        }
        int copiedLength = 0;
        int available = this.getTextLength() - sourceStart;
        if (available < 0) {
            throw new IndexOutOfBoundsException("sourceStart is greater thannumber of characters associated with this event");
        }
        copiedLength = available < length ? available : length;
        System.arraycopy(this.getTextCharacters(), this.getTextStart() + sourceStart, target, targetStart, copiedLength);
        return copiedLength;
    }

    @Override
    public boolean hasText() {
        if (this.fEventType == 4 || this.fEventType == 5 || this.fEventType == 12) {
            return this.fScanner.getCharacterData().length > 0;
        }
        if (this.fEventType == 9) {
            String name = this.fScanner.getEntityName();
            if (name != null) {
                if (this.fScanner.foundBuiltInRefs) {
                    return true;
                }
                XMLEntityStorage entityStore = this.fEntityManager.getEntityStore();
                Entity en = entityStore.getEntity(name);
                if (en == null) {
                    return false;
                }
                if (en.isExternal()) {
                    return ((Entity.ExternalEntity)en).entityLocation.getExpandedSystemId() != null;
                }
                return ((Entity.InternalEntity)en).text != null;
            }
            return false;
        }
        if (this.fEventType == 11) {
            return this.fScanner.fSeenDoctypeDecl;
        }
        return false;
    }

    @Override
    public boolean isAttributeSpecified(int index) {
        if (this.fEventType == 1 || this.fEventType == 10) {
            return this.fScanner.getAttributeIterator().isSpecified(index);
        }
        throw new IllegalStateException("Current state is not among the states " + XMLStreamReaderImpl.getEventTypeString(1) + " , " + XMLStreamReaderImpl.getEventTypeString(10) + "valid for isAttributeSpecified()");
    }

    @Override
    public boolean isCharacters() {
        return this.fEventType == 4;
    }

    @Override
    public int nextTag() throws XMLStreamException {
        int eventType = this.next();
        while (eventType == 4 && this.isWhiteSpace() || eventType == 12 && this.isWhiteSpace() || eventType == 6 || eventType == 3 || eventType == 5) {
            eventType = this.next();
        }
        if (eventType != 1 && eventType != 2) {
            throw new XMLStreamException("found: " + XMLStreamReaderImpl.getEventTypeString(eventType) + ", expected " + XMLStreamReaderImpl.getEventTypeString(1) + " or " + XMLStreamReaderImpl.getEventTypeString(2), this.getLocation());
        }
        return eventType;
    }

    @Override
    public boolean standaloneSet() {
        return this.fScanner.standaloneSet();
    }

    public javax.xml.namespace.QName convertXNIQNametoJavaxQName(QName qname) {
        if (qname == null) {
            return null;
        }
        if (qname.prefix == null) {
            return new javax.xml.namespace.QName(qname.uri, qname.localpart);
        }
        return new javax.xml.namespace.QName(qname.uri, qname.localpart, qname.prefix);
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix cannot be null.");
        }
        return this.fScanner.getNamespaceContext().getURI(this.fSymbolTable.addSymbol(prefix));
    }

    protected void setPropertyManager(PropertyManager propertyManager) {
        this.fPropertyManager = propertyManager;
        this.fScanner.setProperty("stax-properties", propertyManager);
        this.fScanner.setPropertyManager(propertyManager);
    }

    protected PropertyManager getPropertyManager() {
        return this.fPropertyManager;
    }

    static void pr(String str) {
        System.out.println(str);
    }

    protected List<EntityDeclaration> getEntityDecls() {
        if (this.fEventType == 11) {
            XMLEntityStorage entityStore = this.fEntityManager.getEntityStore();
            ArrayList<EntityDeclarationImpl> list = null;
            Map<String, Entity> entities = entityStore.getEntities();
            if (entities.size() > 0) {
                EntityDeclarationImpl decl = null;
                list = new ArrayList<EntityDeclarationImpl>(entities.size());
                for (Map.Entry<String, Entity> entry : entities.entrySet()) {
                    String key = entry.getKey();
                    Entity en = entry.getValue();
                    decl = new EntityDeclarationImpl();
                    decl.setEntityName(key);
                    if (en.isExternal()) {
                        decl.setXMLResourceIdentifier(((Entity.ExternalEntity)en).entityLocation);
                        decl.setNotationName(((Entity.ExternalEntity)en).notation);
                    } else {
                        decl.setEntityReplacementText(((Entity.InternalEntity)en).text);
                    }
                    list.add(decl);
                }
            }
            return list;
        }
        return null;
    }

    protected List<NotationDeclaration> getNotationDecls() {
        if (this.fEventType == 11) {
            if (this.fScanner.fDTDScanner == null) {
                return null;
            }
            DTDGrammar grammar = ((XMLDTDScannerImpl)this.fScanner.fDTDScanner).getGrammar();
            if (grammar == null) {
                return null;
            }
            List<XMLNotationDecl> notations = grammar.getNotationDecls();
            ArrayList<NotationDeclaration> list = new ArrayList<NotationDeclaration>();
            for (XMLNotationDecl notation : notations) {
                if (notation == null) continue;
                list.add(new NotationDeclarationImpl(notation));
            }
            return list;
        }
        return null;
    }
}

