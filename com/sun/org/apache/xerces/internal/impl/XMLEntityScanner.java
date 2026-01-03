/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.impl;

import com.sun.org.apache.xerces.internal.impl.PropertyManager;
import com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import com.sun.org.apache.xerces.internal.impl.XMLScanner;
import com.sun.org.apache.xerces.internal.impl.io.ASCIIReader;
import com.sun.org.apache.xerces.internal.impl.io.UCSReader;
import com.sun.org.apache.xerces.internal.impl.io.UTF8Reader;
import com.sun.org.apache.xerces.internal.util.EncodingMap;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.util.XMLChar;
import com.sun.org.apache.xerces.internal.util.XMLStringBuffer;
import com.sun.org.apache.xerces.internal.utils.XMLLimitAnalyzer;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XMLLocator;
import com.sun.org.apache.xerces.internal.xni.XMLString;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.xml.internal.stream.Entity;
import com.sun.xml.internal.stream.XMLBufferListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Locale;

public class XMLEntityScanner
implements XMLLocator {
    protected Entity.ScannedEntity fCurrentEntity = null;
    protected int fBufferSize = 8192;
    protected XMLEntityManager fEntityManager;
    protected XMLSecurityManager fSecurityManager = null;
    protected XMLLimitAnalyzer fLimitAnalyzer = null;
    private static final boolean DEBUG_ENCODINGS = false;
    private ArrayList<XMLBufferListener> listeners = new ArrayList();
    private static final boolean[] VALID_NAMES;
    private static final boolean DEBUG_BUFFER = false;
    private static final boolean DEBUG_SKIP_STRING = false;
    private static final EOFException END_OF_DOCUMENT_ENTITY;
    protected SymbolTable fSymbolTable = null;
    protected XMLErrorReporter fErrorReporter = null;
    int[] whiteSpaceLookup = new int[100];
    int whiteSpaceLen = 0;
    boolean whiteSpaceInfoNeeded = true;
    protected boolean fAllowJavaEncodings;
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    protected static final String ALLOW_JAVA_ENCODINGS = "http://apache.org/xml/features/allow-java-encodings";
    protected PropertyManager fPropertyManager = null;
    boolean isExternal = false;
    protected boolean xmlVersionSetExplicitly = false;
    boolean detectingVersion = false;
    int offset = 0;
    int newlines = 0;
    boolean counted = false;

    public XMLEntityScanner() {
    }

    public XMLEntityScanner(PropertyManager propertyManager, XMLEntityManager entityManager) {
        this.fEntityManager = entityManager;
        this.reset(propertyManager);
    }

    public final void setBufferSize(int size) {
        this.fBufferSize = size;
    }

    public void reset(PropertyManager propertyManager) {
        this.fSymbolTable = (SymbolTable)propertyManager.getProperty(SYMBOL_TABLE);
        this.fErrorReporter = (XMLErrorReporter)propertyManager.getProperty(ERROR_REPORTER);
        this.resetCommon();
    }

    public void reset(XMLComponentManager componentManager) throws XMLConfigurationException {
        this.fAllowJavaEncodings = componentManager.getFeature(ALLOW_JAVA_ENCODINGS, false);
        this.fSymbolTable = (SymbolTable)componentManager.getProperty(SYMBOL_TABLE);
        this.fErrorReporter = (XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);
        this.resetCommon();
    }

    public final void reset(SymbolTable symbolTable, XMLEntityManager entityManager, XMLErrorReporter reporter) {
        this.fCurrentEntity = null;
        this.fSymbolTable = symbolTable;
        this.fEntityManager = entityManager;
        this.fErrorReporter = reporter;
        this.fLimitAnalyzer = this.fEntityManager.fLimitAnalyzer;
        this.fSecurityManager = this.fEntityManager.fSecurityManager;
    }

    private void resetCommon() {
        this.fCurrentEntity = null;
        this.whiteSpaceLen = 0;
        this.whiteSpaceInfoNeeded = true;
        this.listeners.clear();
        this.fLimitAnalyzer = this.fEntityManager.fLimitAnalyzer;
        this.fSecurityManager = this.fEntityManager.fSecurityManager;
    }

    @Override
    public final String getXMLVersion() {
        if (this.fCurrentEntity != null) {
            return this.fCurrentEntity.xmlVersion;
        }
        return null;
    }

    public final void setXMLVersion(String xmlVersion) {
        this.xmlVersionSetExplicitly = true;
        this.fCurrentEntity.xmlVersion = xmlVersion;
    }

    public final void setCurrentEntity(Entity.ScannedEntity scannedEntity) {
        this.fCurrentEntity = scannedEntity;
        if (this.fCurrentEntity != null) {
            this.isExternal = this.fCurrentEntity.isExternal();
        }
    }

    public Entity.ScannedEntity getCurrentEntity() {
        return this.fCurrentEntity;
    }

    @Override
    public final String getBaseSystemId() {
        return this.fCurrentEntity != null && this.fCurrentEntity.entityLocation != null ? this.fCurrentEntity.entityLocation.getExpandedSystemId() : null;
    }

    public void setBaseSystemId(String systemId) {
    }

    @Override
    public final int getLineNumber() {
        return this.fCurrentEntity != null ? this.fCurrentEntity.lineNumber : -1;
    }

    public void setLineNumber(int line) {
    }

    @Override
    public final int getColumnNumber() {
        return this.fCurrentEntity != null ? this.fCurrentEntity.columnNumber : -1;
    }

    public void setColumnNumber(int col) {
    }

    @Override
    public final int getCharacterOffset() {
        return this.fCurrentEntity != null ? this.fCurrentEntity.fTotalCountTillLastLoad + this.fCurrentEntity.position : -1;
    }

    @Override
    public final String getExpandedSystemId() {
        return this.fCurrentEntity != null && this.fCurrentEntity.entityLocation != null ? this.fCurrentEntity.entityLocation.getExpandedSystemId() : null;
    }

    public void setExpandedSystemId(String systemId) {
    }

    @Override
    public final String getLiteralSystemId() {
        return this.fCurrentEntity != null && this.fCurrentEntity.entityLocation != null ? this.fCurrentEntity.entityLocation.getLiteralSystemId() : null;
    }

    public void setLiteralSystemId(String systemId) {
    }

    @Override
    public final String getPublicId() {
        return this.fCurrentEntity != null && this.fCurrentEntity.entityLocation != null ? this.fCurrentEntity.entityLocation.getPublicId() : null;
    }

    public void setPublicId(String publicId) {
    }

    public void setVersion(String version) {
        this.fCurrentEntity.version = version;
    }

    public String getVersion() {
        if (this.fCurrentEntity != null) {
            return this.fCurrentEntity.version;
        }
        return null;
    }

    @Override
    public final String getEncoding() {
        if (this.fCurrentEntity != null) {
            return this.fCurrentEntity.encoding;
        }
        return null;
    }

    public final void setEncoding(String encoding) throws IOException {
        if (!(this.fCurrentEntity.stream == null || this.fCurrentEntity.encoding != null && this.fCurrentEntity.encoding.equals(encoding))) {
            if (this.fCurrentEntity.encoding != null && this.fCurrentEntity.encoding.startsWith("UTF-16")) {
                String ENCODING = encoding.toUpperCase(Locale.ENGLISH);
                if (ENCODING.equals("UTF-16")) {
                    return;
                }
                if (ENCODING.equals("ISO-10646-UCS-4")) {
                    this.fCurrentEntity.reader = this.fCurrentEntity.encoding.equals("UTF-16BE") ? new UCSReader(this.fCurrentEntity.stream, 8) : new UCSReader(this.fCurrentEntity.stream, 4);
                    return;
                }
                if (ENCODING.equals("ISO-10646-UCS-2")) {
                    this.fCurrentEntity.reader = this.fCurrentEntity.encoding.equals("UTF-16BE") ? new UCSReader(this.fCurrentEntity.stream, 2) : new UCSReader(this.fCurrentEntity.stream, 1);
                    return;
                }
            }
            this.fCurrentEntity.reader = this.createReader(this.fCurrentEntity.stream, encoding, null);
            this.fCurrentEntity.encoding = encoding;
        }
    }

    public final boolean isExternal() {
        return this.fCurrentEntity.isExternal();
    }

    public int getChar(int relative) throws IOException {
        if (this.arrangeCapacity(relative + 1, false)) {
            return this.fCurrentEntity.ch[this.fCurrentEntity.position + relative];
        }
        return -1;
    }

    public int peekChar() throws IOException {
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            this.load(0, true, true);
        }
        int c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
        if (this.isExternal) {
            return c != 13 ? c : 10;
        }
        return c;
    }

    protected int scanChar(XMLScanner.NameType nt) throws IOException {
        int c;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            this.load(0, true, true);
        }
        this.offset = this.fCurrentEntity.position;
        if ((c = this.fCurrentEntity.ch[this.fCurrentEntity.position++]) == 10 || c == 13 && this.isExternal) {
            ++this.fCurrentEntity.lineNumber;
            this.fCurrentEntity.columnNumber = 1;
            if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                this.invokeListeners(1);
                this.fCurrentEntity.ch[0] = (char)c;
                this.load(1, true, false);
                this.offset = 0;
            }
            if (c == 13 && this.isExternal && this.fCurrentEntity.position < this.fCurrentEntity.count) {
                if (this.fCurrentEntity.ch[this.fCurrentEntity.position++] != '\n') {
                    --this.fCurrentEntity.position;
                }
                c = 10;
            }
        }
        ++this.fCurrentEntity.columnNumber;
        if (!this.detectingVersion) {
            this.checkEntityLimit(nt, this.fCurrentEntity, this.offset, this.fCurrentEntity.position - this.offset);
        }
        return c;
    }

    protected String scanNmtoken() throws IOException {
        int length;
        char c;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            this.load(0, true, true);
        }
        this.offset = this.fCurrentEntity.position;
        boolean vc = false;
        while (vc = (c = this.fCurrentEntity.ch[this.fCurrentEntity.position]) < '\u007f' ? VALID_NAMES[c] : XMLChar.isName(c)) {
            if (++this.fCurrentEntity.position != this.fCurrentEntity.count) continue;
            length = this.fCurrentEntity.position - this.offset;
            this.invokeListeners(length);
            if (length == this.fCurrentEntity.fBufferSize) {
                char[] tmp = new char[this.fCurrentEntity.fBufferSize * 2];
                System.arraycopy(this.fCurrentEntity.ch, this.offset, tmp, 0, length);
                this.fCurrentEntity.ch = tmp;
                this.fCurrentEntity.fBufferSize *= 2;
            } else {
                System.arraycopy(this.fCurrentEntity.ch, this.offset, this.fCurrentEntity.ch, 0, length);
            }
            this.offset = 0;
            if (!this.load(length, false, false)) continue;
            break;
        }
        length = this.fCurrentEntity.position - this.offset;
        this.fCurrentEntity.columnNumber += length;
        String symbol = null;
        if (length > 0) {
            symbol = this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, this.offset, length);
        }
        return symbol;
    }

    protected String scanName(XMLScanner.NameType nt) throws IOException {
        String symbol;
        int length;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            this.load(0, true, true);
        }
        this.offset = this.fCurrentEntity.position++;
        if (XMLChar.isNameStart(this.fCurrentEntity.ch[this.offset])) {
            char c;
            if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                this.invokeListeners(1);
                this.fCurrentEntity.ch[0] = this.fCurrentEntity.ch[this.offset];
                this.offset = 0;
                if (this.load(1, false, false)) {
                    ++this.fCurrentEntity.columnNumber;
                    String symbol2 = this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, 0, 1);
                    return symbol2;
                }
            }
            boolean vc = false;
            while (vc = (c = this.fCurrentEntity.ch[this.fCurrentEntity.position]) < '\u007f' ? VALID_NAMES[c] : XMLChar.isName(c)) {
                length = this.checkBeforeLoad(this.fCurrentEntity, this.offset, this.offset);
                if (length <= 0) continue;
                this.offset = 0;
                if (!this.load(length, false, false)) continue;
                break;
            }
        }
        length = this.fCurrentEntity.position - this.offset;
        this.fCurrentEntity.columnNumber += length;
        if (length > 0) {
            this.checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, this.fCurrentEntity, this.offset, length);
            this.checkEntityLimit(nt, this.fCurrentEntity, this.offset, length);
            symbol = this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, this.offset, length);
        } else {
            symbol = null;
        }
        return symbol;
    }

    protected boolean scanQName(QName qname, XMLScanner.NameType nt) throws IOException {
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            this.load(0, true, true);
        }
        this.offset = this.fCurrentEntity.position++;
        if (XMLChar.isNameStart(this.fCurrentEntity.ch[this.offset])) {
            int length;
            char c;
            if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                this.invokeListeners(1);
                this.fCurrentEntity.ch[0] = this.fCurrentEntity.ch[this.offset];
                this.offset = 0;
                if (this.load(1, false, false)) {
                    ++this.fCurrentEntity.columnNumber;
                    String name = this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, 0, 1);
                    qname.setValues(null, name, name, null);
                    this.checkEntityLimit(nt, this.fCurrentEntity, 0, 1);
                    return true;
                }
            }
            int index = -1;
            boolean vc = false;
            while (vc = (c = this.fCurrentEntity.ch[this.fCurrentEntity.position]) < '\u007f' ? VALID_NAMES[c] : XMLChar.isName(c)) {
                if (c == ':') {
                    if (index != -1) break;
                    index = this.fCurrentEntity.position;
                    this.checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, this.fCurrentEntity, this.offset, index - this.offset);
                }
                if ((length = this.checkBeforeLoad(this.fCurrentEntity, this.offset, index)) <= 0) continue;
                if (index != -1) {
                    index -= this.offset;
                }
                this.offset = 0;
                if (!this.load(length, false, false)) continue;
                break;
            }
            length = this.fCurrentEntity.position - this.offset;
            this.fCurrentEntity.columnNumber += length;
            if (length > 0) {
                String prefix = null;
                String localpart = null;
                String rawname = this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, this.offset, length);
                if (index != -1) {
                    int prefixLength = index - this.offset;
                    this.checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, this.fCurrentEntity, this.offset, prefixLength);
                    prefix = this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, this.offset, prefixLength);
                    int len = length - prefixLength - 1;
                    int startLocal = index + 1;
                    if (!XMLChar.isNCNameStart(this.fCurrentEntity.ch[startLocal])) {
                        this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "IllegalQName", new Object[]{rawname}, (short)2);
                    }
                    this.checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, this.fCurrentEntity, index + 1, len);
                    localpart = this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, index + 1, len);
                } else {
                    localpart = rawname;
                    this.checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, this.fCurrentEntity, this.offset, length);
                }
                qname.setValues(prefix, localpart, rawname, null);
                this.checkEntityLimit(nt, this.fCurrentEntity, this.offset, length);
                return true;
            }
        }
        return false;
    }

    protected int checkBeforeLoad(Entity.ScannedEntity entity, int offset, int nameOffset) throws IOException {
        int length = 0;
        if (++entity.position == entity.count) {
            int nameLength = length = entity.position - offset;
            if (nameOffset != -1) {
                nameLength = length - (nameOffset -= offset);
            } else {
                nameOffset = offset;
            }
            this.checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, entity, nameOffset, nameLength);
            this.invokeListeners(length);
            if (length == entity.ch.length) {
                char[] tmp = new char[entity.fBufferSize * 2];
                System.arraycopy(entity.ch, offset, tmp, 0, length);
                entity.ch = tmp;
                entity.fBufferSize *= 2;
            } else {
                System.arraycopy(entity.ch, offset, entity.ch, 0, length);
            }
        }
        return length;
    }

    protected void checkEntityLimit(XMLScanner.NameType nt, Entity.ScannedEntity entity, int offset, int length) {
        if (entity == null || !entity.isGE) {
            return;
        }
        if (nt != XMLScanner.NameType.REFERENCE) {
            this.checkLimit(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT, entity, offset, length);
        }
        if (nt == XMLScanner.NameType.ELEMENTSTART || nt == XMLScanner.NameType.ATTRIBUTENAME) {
            this.checkNodeCount(entity);
        }
    }

    protected void checkNodeCount(Entity.ScannedEntity entity) {
        if (entity != null && entity.isGE) {
            this.checkLimit(XMLSecurityManager.Limit.ENTITY_REPLACEMENT_LIMIT, entity, 0, 1);
        }
    }

    protected void checkLimit(XMLSecurityManager.Limit limit, Entity.ScannedEntity entity, int offset, int length) {
        this.fLimitAnalyzer.addValue(limit, entity.name, length);
        if (this.fSecurityManager.isOverLimit(limit, this.fLimitAnalyzer)) {
            Object[] objectArray;
            this.fSecurityManager.debugPrint(this.fLimitAnalyzer);
            if (limit == XMLSecurityManager.Limit.ENTITY_REPLACEMENT_LIMIT) {
                Object[] objectArray2 = new Object[3];
                objectArray2[0] = this.fLimitAnalyzer.getValue(limit);
                objectArray2[1] = this.fSecurityManager.getLimit(limit);
                objectArray = objectArray2;
                objectArray2[2] = this.fSecurityManager.getStateLiteral(limit);
            } else {
                Object[] objectArray3 = new Object[4];
                objectArray3[0] = entity.name;
                objectArray3[1] = this.fLimitAnalyzer.getValue(limit);
                objectArray3[2] = this.fSecurityManager.getLimit(limit);
                objectArray = objectArray3;
                objectArray3[3] = this.fSecurityManager.getStateLiteral(limit);
            }
            Object[] e = objectArray;
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", limit.key(), e, (short)2);
        }
        if (this.fSecurityManager.isOverLimit(XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT, this.fLimitAnalyzer)) {
            this.fSecurityManager.debugPrint(this.fLimitAnalyzer);
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "TotalEntitySizeLimit", new Object[]{this.fLimitAnalyzer.getTotalValue(XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT), this.fSecurityManager.getLimit(XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT), this.fSecurityManager.getStateLiteral(XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT)}, (short)2);
        }
    }

    protected int scanContent(XMLString content) throws IOException {
        int c;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            this.load(0, true, true);
        } else if (this.fCurrentEntity.position == this.fCurrentEntity.count - 1) {
            this.invokeListeners(1);
            this.fCurrentEntity.ch[0] = this.fCurrentEntity.ch[this.fCurrentEntity.count - 1];
            this.load(1, false, false);
            this.fCurrentEntity.position = 0;
        }
        if (this.normalizeNewlines((short)1, content, false, false, null)) {
            return -1;
        }
        while (this.fCurrentEntity.position < this.fCurrentEntity.count) {
            if (XMLChar.isContent(c = this.fCurrentEntity.ch[this.fCurrentEntity.position++])) continue;
            --this.fCurrentEntity.position;
            break;
        }
        int length = this.fCurrentEntity.position - this.offset;
        this.fCurrentEntity.columnNumber += length - this.newlines;
        if (!this.counted) {
            this.checkEntityLimit(null, this.fCurrentEntity, this.offset, length);
        }
        content.setValues(this.fCurrentEntity.ch, this.offset, length);
        if (this.fCurrentEntity.position != this.fCurrentEntity.count) {
            c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
            if (c == 13 && this.isExternal) {
                c = 10;
            }
        } else {
            c = -1;
        }
        return c;
    }

    protected int scanLiteral(int quote, XMLString content, boolean isNSURI) throws IOException {
        int c;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            this.load(0, true, true);
        } else if (this.fCurrentEntity.position == this.fCurrentEntity.count - 1) {
            this.invokeListeners(1);
            this.fCurrentEntity.ch[0] = this.fCurrentEntity.ch[this.fCurrentEntity.count - 1];
            this.load(1, false, false);
            this.fCurrentEntity.position = 0;
        }
        if (this.whiteSpaceInfoNeeded) {
            this.whiteSpaceLen = 0;
        }
        if (this.normalizeNewlines((short)1, content, false, true, null)) {
            return -1;
        }
        while (this.fCurrentEntity.position < this.fCurrentEntity.count && ((c = this.fCurrentEntity.ch[this.fCurrentEntity.position]) != quote || this.fCurrentEntity.literal && !this.isExternal) && c != 37 && XMLChar.isContent(c) && (c != 13 || this.isExternal)) {
            if (this.whiteSpaceInfoNeeded && c == 9) {
                this.storeWhiteSpace(this.fCurrentEntity.position);
            }
            ++this.fCurrentEntity.position;
        }
        int length = this.fCurrentEntity.position - this.offset;
        this.fCurrentEntity.columnNumber += length - this.newlines;
        this.checkEntityLimit(null, this.fCurrentEntity, this.offset, length);
        if (isNSURI) {
            this.checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, this.fCurrentEntity, this.offset, length);
        }
        content.setValues(this.fCurrentEntity.ch, this.offset, length);
        if (this.fCurrentEntity.position != this.fCurrentEntity.count) {
            c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
            if (c == quote && this.fCurrentEntity.literal) {
                c = -1;
            }
        } else {
            c = -1;
        }
        return c;
    }

    void storeWhiteSpace(int whiteSpacePos) {
        if (this.whiteSpaceLen >= this.whiteSpaceLookup.length) {
            int[] tmp = new int[this.whiteSpaceLookup.length + 100];
            System.arraycopy(this.whiteSpaceLookup, 0, tmp, 0, this.whiteSpaceLookup.length);
            this.whiteSpaceLookup = tmp;
        }
        this.whiteSpaceLookup[this.whiteSpaceLen++] = whiteSpacePos;
    }

    protected boolean scanData(String delimiter, XMLStringBuffer buffer, int chunkLimit) throws IOException {
        boolean done = false;
        int delimLen = delimiter.length();
        char charAt0 = delimiter.charAt(0);
        do {
            int length;
            if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                this.load(0, true, false);
            }
            boolean bNextEntity = false;
            while (this.fCurrentEntity.position > this.fCurrentEntity.count - delimLen && !bNextEntity) {
                System.arraycopy(this.fCurrentEntity.ch, this.fCurrentEntity.position, this.fCurrentEntity.ch, 0, this.fCurrentEntity.count - this.fCurrentEntity.position);
                bNextEntity = this.load(this.fCurrentEntity.count - this.fCurrentEntity.position, false, false);
                this.fCurrentEntity.position = 0;
                this.fCurrentEntity.startPosition = 0;
            }
            if (this.fCurrentEntity.position > this.fCurrentEntity.count - delimLen) {
                int length2 = this.fCurrentEntity.count - this.fCurrentEntity.position;
                this.checkEntityLimit(XMLScanner.NameType.COMMENT, this.fCurrentEntity, this.fCurrentEntity.position, length2);
                buffer.append(this.fCurrentEntity.ch, this.fCurrentEntity.position, length2);
                this.fCurrentEntity.columnNumber += this.fCurrentEntity.count;
                this.fCurrentEntity.baseCharOffset += this.fCurrentEntity.position - this.fCurrentEntity.startPosition;
                this.fCurrentEntity.position = this.fCurrentEntity.count;
                this.fCurrentEntity.startPosition = this.fCurrentEntity.count;
                this.load(0, true, false);
                return false;
            }
            if (this.normalizeNewlines((short)1, buffer, true, false, XMLScanner.NameType.COMMENT)) {
                return true;
            }
            block2: while (this.fCurrentEntity.position < this.fCurrentEntity.count) {
                char c;
                if ((c = this.fCurrentEntity.ch[this.fCurrentEntity.position++]) == charAt0) {
                    int delimOffset = this.fCurrentEntity.position - 1;
                    for (int i = 1; i < delimLen; ++i) {
                        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                            this.fCurrentEntity.position -= i;
                            break block2;
                        }
                        c = this.fCurrentEntity.ch[this.fCurrentEntity.position++];
                        if (delimiter.charAt(i) == c) continue;
                        this.fCurrentEntity.position -= i;
                        break;
                    }
                    if (this.fCurrentEntity.position == delimOffset + delimLen) {
                        done = true;
                        break;
                    }
                } else {
                    if (c == '\n' || this.isExternal && c == '\r') {
                        --this.fCurrentEntity.position;
                        break;
                    }
                    if (XMLChar.isInvalid(c)) {
                        --this.fCurrentEntity.position;
                        length = this.fCurrentEntity.position - this.offset;
                        this.fCurrentEntity.columnNumber += length - this.newlines;
                        this.checkEntityLimit(XMLScanner.NameType.COMMENT, this.fCurrentEntity, this.offset, length);
                        buffer.append(this.fCurrentEntity.ch, this.offset, length);
                        return true;
                    }
                }
                if (chunkLimit <= 0 || buffer.length + this.fCurrentEntity.position - this.offset < chunkLimit) continue;
            }
            length = this.fCurrentEntity.position - this.offset;
            this.fCurrentEntity.columnNumber += length - this.newlines;
            this.checkEntityLimit(XMLScanner.NameType.COMMENT, this.fCurrentEntity, this.offset, length);
            if (done) {
                length -= delimLen;
            }
            buffer.append(this.fCurrentEntity.ch, this.offset, length);
        } while ((chunkLimit <= 0 || buffer.length < chunkLimit) && !done && chunkLimit == 0);
        return !done;
    }

    protected boolean skipChar(int c, XMLScanner.NameType nt) throws IOException {
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            this.load(0, true, true);
        }
        this.offset = this.fCurrentEntity.position;
        char cc = this.fCurrentEntity.ch[this.fCurrentEntity.position];
        if (cc == c) {
            ++this.fCurrentEntity.position;
            if (c == 10) {
                ++this.fCurrentEntity.lineNumber;
                this.fCurrentEntity.columnNumber = 1;
            } else {
                ++this.fCurrentEntity.columnNumber;
            }
            this.checkEntityLimit(nt, this.fCurrentEntity, this.offset, this.fCurrentEntity.position - this.offset);
            return true;
        }
        return false;
    }

    public boolean isSpace(char ch) {
        return ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r';
    }

    protected boolean skipSpaces() throws IOException {
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            this.load(0, true, true);
        }
        if (this.fCurrentEntity == null) {
            return false;
        }
        char c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
        this.offset = this.fCurrentEntity.position - 1;
        if (XMLChar.isSpace(c)) {
            do {
                boolean entityChanged = false;
                if (c == '\n' || this.isExternal && c == '\r') {
                    ++this.fCurrentEntity.lineNumber;
                    this.fCurrentEntity.columnNumber = 1;
                    if (this.fCurrentEntity.position == this.fCurrentEntity.count - 1) {
                        this.invokeListeners(1);
                        this.fCurrentEntity.ch[0] = c;
                        entityChanged = this.load(1, true, false);
                        if (!entityChanged) {
                            this.fCurrentEntity.position = 0;
                        } else if (this.fCurrentEntity == null) {
                            return true;
                        }
                    }
                    if (c == '\r' && this.isExternal && this.fCurrentEntity.ch[++this.fCurrentEntity.position] != '\n') {
                        --this.fCurrentEntity.position;
                    }
                } else {
                    ++this.fCurrentEntity.columnNumber;
                }
                this.checkEntityLimit(null, this.fCurrentEntity, this.offset, this.fCurrentEntity.position - this.offset);
                this.offset = this.fCurrentEntity.position++;
                if (!entityChanged) {
                    // empty if block
                }
                if (this.fCurrentEntity.position != this.fCurrentEntity.count) continue;
                this.load(0, true, true);
                if (this.fCurrentEntity != null) continue;
                return true;
            } while (XMLChar.isSpace(c = this.fCurrentEntity.ch[this.fCurrentEntity.position]));
            return true;
        }
        return false;
    }

    public boolean arrangeCapacity(int length) throws IOException {
        return this.arrangeCapacity(length, false);
    }

    public boolean arrangeCapacity(int length, boolean changeEntity) throws IOException {
        if (this.fCurrentEntity.count - this.fCurrentEntity.position >= length) {
            return true;
        }
        boolean entityChanged = false;
        while (this.fCurrentEntity.count - this.fCurrentEntity.position < length) {
            if (this.fCurrentEntity.ch.length - this.fCurrentEntity.position < length) {
                this.invokeListeners(0);
                System.arraycopy(this.fCurrentEntity.ch, this.fCurrentEntity.position, this.fCurrentEntity.ch, 0, this.fCurrentEntity.count - this.fCurrentEntity.position);
                this.fCurrentEntity.count -= this.fCurrentEntity.position;
                this.fCurrentEntity.position = 0;
            }
            if (this.fCurrentEntity.count - this.fCurrentEntity.position >= length) continue;
            int pos = this.fCurrentEntity.position;
            this.invokeListeners(pos);
            entityChanged = this.load(this.fCurrentEntity.count, changeEntity, false);
            this.fCurrentEntity.position = pos;
            if (!entityChanged) continue;
            break;
        }
        return this.fCurrentEntity.count - this.fCurrentEntity.position >= length;
    }

    protected boolean skipString(String s) throws IOException {
        int length = s.length();
        if (this.arrangeCapacity(length, false)) {
            int beforeSkip = this.fCurrentEntity.position;
            int afterSkip = this.fCurrentEntity.position + length - 1;
            int i = length - 1;
            while (s.charAt(i--) == this.fCurrentEntity.ch[afterSkip]) {
                if (afterSkip-- != beforeSkip) continue;
                this.fCurrentEntity.position += length;
                this.fCurrentEntity.columnNumber += length;
                if (!this.detectingVersion) {
                    this.checkEntityLimit(null, this.fCurrentEntity, beforeSkip, length);
                }
                return true;
            }
        }
        return false;
    }

    protected boolean skipString(char[] s) throws IOException {
        int length = s.length;
        if (this.arrangeCapacity(length, false)) {
            int beforeSkip = this.fCurrentEntity.position;
            for (int i = 0; i < length; ++i) {
                if (this.fCurrentEntity.ch[beforeSkip++] == s[i]) continue;
                return false;
            }
            this.fCurrentEntity.position += length;
            this.fCurrentEntity.columnNumber += length;
            if (!this.detectingVersion) {
                this.checkEntityLimit(null, this.fCurrentEntity, beforeSkip, length);
            }
            return true;
        }
        return false;
    }

    final boolean load(int offset, boolean changeEntity, boolean notify) throws IOException {
        if (notify) {
            this.invokeListeners(offset);
        }
        this.fCurrentEntity.fTotalCountTillLastLoad += this.fCurrentEntity.fLastCount;
        int length = this.fCurrentEntity.ch.length - offset;
        if (!this.fCurrentEntity.mayReadChunks && length > 64) {
            length = 64;
        }
        int count = this.fCurrentEntity.reader.read(this.fCurrentEntity.ch, offset, length);
        boolean entityChanged = false;
        if (count != -1) {
            if (count != 0) {
                this.fCurrentEntity.fLastCount = count;
                this.fCurrentEntity.count = count + offset;
                this.fCurrentEntity.position = offset;
            }
        } else {
            this.fCurrentEntity.count = offset;
            this.fCurrentEntity.position = offset;
            entityChanged = true;
            if (changeEntity) {
                this.fEntityManager.endEntity();
                if (this.fCurrentEntity == null) {
                    throw END_OF_DOCUMENT_ENTITY;
                }
                if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                    this.load(0, true, false);
                }
            }
        }
        return entityChanged;
    }

    protected Reader createReader(InputStream inputStream, String encoding, Boolean isBigEndian) throws IOException {
        String javaEncoding;
        String ENCODING;
        if (encoding == null) {
            encoding = "UTF-8";
        }
        if ((ENCODING = encoding.toUpperCase(Locale.ENGLISH)).equals("UTF-8")) {
            return new UTF8Reader(inputStream, this.fCurrentEntity.fBufferSize, this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210"), this.fErrorReporter.getLocale());
        }
        if (ENCODING.equals("US-ASCII")) {
            return new ASCIIReader(inputStream, this.fCurrentEntity.fBufferSize, this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210"), this.fErrorReporter.getLocale());
        }
        if (ENCODING.equals("ISO-10646-UCS-4")) {
            if (isBigEndian != null) {
                boolean isBE = isBigEndian;
                if (isBE) {
                    return new UCSReader(inputStream, 8);
                }
                return new UCSReader(inputStream, 4);
            }
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingByteOrderUnsupported", new Object[]{encoding}, (short)2);
        }
        if (ENCODING.equals("ISO-10646-UCS-2")) {
            if (isBigEndian != null) {
                boolean isBE = isBigEndian;
                if (isBE) {
                    return new UCSReader(inputStream, 2);
                }
                return new UCSReader(inputStream, 1);
            }
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingByteOrderUnsupported", new Object[]{encoding}, (short)2);
        }
        boolean validIANA = XMLChar.isValidIANAEncoding(encoding);
        boolean validJava = XMLChar.isValidJavaEncoding(encoding);
        if (!validIANA || this.fAllowJavaEncodings && !validJava) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingDeclInvalid", new Object[]{encoding}, (short)2);
            encoding = "ISO-8859-1";
        }
        if ((javaEncoding = EncodingMap.getIANA2JavaMapping(ENCODING)) == null) {
            if (this.fAllowJavaEncodings) {
                javaEncoding = encoding;
            } else {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingDeclInvalid", new Object[]{encoding}, (short)2);
                javaEncoding = "ISO8859_1";
            }
        } else if (javaEncoding.equals("ASCII")) {
            return new ASCIIReader(inputStream, this.fBufferSize, this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210"), this.fErrorReporter.getLocale());
        }
        return new InputStreamReader(inputStream, javaEncoding);
    }

    protected Object[] getEncodingName(byte[] b4, int count) {
        if (count < 2) {
            return new Object[]{"UTF-8", null};
        }
        int b0 = b4[0] & 0xFF;
        int b1 = b4[1] & 0xFF;
        if (b0 == 254 && b1 == 255) {
            return new Object[]{"UTF-16BE", true};
        }
        if (b0 == 255 && b1 == 254) {
            return new Object[]{"UTF-16LE", false};
        }
        if (count < 3) {
            return new Object[]{"UTF-8", null};
        }
        int b2 = b4[2] & 0xFF;
        if (b0 == 239 && b1 == 187 && b2 == 191) {
            return new Object[]{"UTF-8", null};
        }
        if (count < 4) {
            return new Object[]{"UTF-8", null};
        }
        int b3 = b4[3] & 0xFF;
        if (b0 == 0 && b1 == 0 && b2 == 0 && b3 == 60) {
            return new Object[]{"ISO-10646-UCS-4", true};
        }
        if (b0 == 60 && b1 == 0 && b2 == 0 && b3 == 0) {
            return new Object[]{"ISO-10646-UCS-4", false};
        }
        if (b0 == 0 && b1 == 0 && b2 == 60 && b3 == 0) {
            return new Object[]{"ISO-10646-UCS-4", null};
        }
        if (b0 == 0 && b1 == 60 && b2 == 0 && b3 == 0) {
            return new Object[]{"ISO-10646-UCS-4", null};
        }
        if (b0 == 0 && b1 == 60 && b2 == 0 && b3 == 63) {
            return new Object[]{"UTF-16BE", true};
        }
        if (b0 == 60 && b1 == 0 && b2 == 63 && b3 == 0) {
            return new Object[]{"UTF-16LE", false};
        }
        if (b0 == 76 && b1 == 111 && b2 == 167 && b3 == 148) {
            return new Object[]{"CP037", null};
        }
        return new Object[]{"UTF-8", null};
    }

    final void print() {
    }

    public void registerListener(XMLBufferListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public void invokeListeners(int loadPos) {
        for (int i = 0; i < this.listeners.size(); ++i) {
            this.listeners.get(i).refresh(loadPos);
        }
    }

    protected final boolean skipDeclSpaces() throws IOException {
        char c;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            this.load(0, true, false);
        }
        if (XMLChar.isSpace(c = this.fCurrentEntity.ch[this.fCurrentEntity.position])) {
            boolean external = this.fCurrentEntity.isExternal();
            do {
                boolean entityChanged = false;
                if (c == '\n' || external && c == '\r') {
                    ++this.fCurrentEntity.lineNumber;
                    this.fCurrentEntity.columnNumber = 1;
                    if (this.fCurrentEntity.position == this.fCurrentEntity.count - 1) {
                        this.fCurrentEntity.ch[0] = c;
                        entityChanged = this.load(1, true, false);
                        if (!entityChanged) {
                            this.fCurrentEntity.position = 0;
                        }
                    }
                    if (c == '\r' && external && this.fCurrentEntity.ch[++this.fCurrentEntity.position] != '\n') {
                        --this.fCurrentEntity.position;
                    }
                } else {
                    ++this.fCurrentEntity.columnNumber;
                }
                if (!entityChanged) {
                    ++this.fCurrentEntity.position;
                }
                if (this.fCurrentEntity.position != this.fCurrentEntity.count) continue;
                this.load(0, true, false);
            } while (XMLChar.isSpace(c = this.fCurrentEntity.ch[this.fCurrentEntity.position]));
            return true;
        }
        return false;
    }

    protected boolean normalizeNewlines(short version, XMLString buffer, boolean append, boolean storeWS, XMLScanner.NameType nt) throws IOException {
        this.offset = this.fCurrentEntity.position;
        char c = this.fCurrentEntity.ch[this.offset];
        this.newlines = 0;
        this.counted = false;
        if (c == '\n' || c == '\r' || version == 2 && (c == '\u0085' || c == '\u2028') && this.isExternal) {
            do {
                if ((c = this.fCurrentEntity.ch[this.fCurrentEntity.position++]) == '\n' || c == '\r' || version == 2 && (c == '\u0085' || c == '\u2028')) {
                    ++this.newlines;
                    ++this.fCurrentEntity.lineNumber;
                    this.fCurrentEntity.columnNumber = 1;
                    if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                        this.checkEntityLimit(nt, this.fCurrentEntity, this.offset, this.newlines);
                        this.offset = 0;
                        this.fCurrentEntity.position = this.newlines;
                        if (this.load(this.newlines, false, true)) {
                            this.counted = true;
                            break;
                        }
                    }
                    if (c != '\r' || !this.isExternal) continue;
                    char cc = this.fCurrentEntity.ch[this.fCurrentEntity.position];
                    if (cc == '\n' || version == 2 && cc == '\u0085') {
                        ++this.fCurrentEntity.position;
                        ++this.offset;
                        continue;
                    }
                    ++this.newlines;
                    continue;
                }
                --this.fCurrentEntity.position;
                break;
            } while (this.fCurrentEntity.position < this.fCurrentEntity.count - 1);
            for (int i = this.offset; i < this.fCurrentEntity.position; ++i) {
                this.fCurrentEntity.ch[i] = 10;
                if (!storeWS) continue;
                this.storeWhiteSpace(i);
            }
            int length = this.fCurrentEntity.position - this.offset;
            if (this.fCurrentEntity.position == this.fCurrentEntity.count - 1) {
                this.checkEntityLimit(nt, this.fCurrentEntity, this.offset, length);
                if (append) {
                    buffer.append(this.fCurrentEntity.ch, this.offset, length);
                } else {
                    buffer.setValues(this.fCurrentEntity.ch, this.offset, length);
                }
                return true;
            }
        }
        return false;
    }

    static {
        int i;
        VALID_NAMES = new boolean[127];
        END_OF_DOCUMENT_ENTITY = new EOFException(){
            private static final long serialVersionUID = 980337771224675268L;

            @Override
            public Throwable fillInStackTrace() {
                return this;
            }
        };
        for (i = 65; i <= 90; ++i) {
            XMLEntityScanner.VALID_NAMES[i] = true;
        }
        for (i = 97; i <= 122; ++i) {
            XMLEntityScanner.VALID_NAMES[i] = true;
        }
        for (i = 48; i <= 57; ++i) {
            XMLEntityScanner.VALID_NAMES[i] = true;
        }
        XMLEntityScanner.VALID_NAMES[45] = true;
        XMLEntityScanner.VALID_NAMES[46] = true;
        XMLEntityScanner.VALID_NAMES[58] = true;
        XMLEntityScanner.VALID_NAMES[95] = true;
    }
}

