/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.impl;

import com.sun.org.apache.xerces.internal.impl.PropertyManager;
import com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import com.sun.org.apache.xerces.internal.impl.XMLEntityScanner;
import com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import com.sun.org.apache.xerces.internal.util.Status;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.util.XMLChar;
import com.sun.org.apache.xerces.internal.util.XMLResourceIdentifierImpl;
import com.sun.org.apache.xerces.internal.util.XMLStringBuffer;
import com.sun.org.apache.xerces.internal.utils.XMLLimitAnalyzer;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.xni.Augmentations;
import com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XMLString;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.xml.internal.stream.Entity;
import com.sun.xml.internal.stream.XMLEntityStorage;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.stream.events.XMLEvent;

public abstract class XMLScanner
implements XMLComponent {
    protected static final String NAMESPACES = "http://xml.org/sax/features/namespaces";
    protected static final String VALIDATION = "http://xml.org/sax/features/validation";
    protected static final String NOTIFY_CHAR_REFS = "http://apache.org/xml/features/scanner/notify-char-refs";
    protected static final String PARSER_SETTINGS = "http://apache.org/xml/features/internal/parser-settings";
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    protected static final String ENTITY_MANAGER = "http://apache.org/xml/properties/internal/entity-manager";
    private static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    protected static final boolean DEBUG_ATTR_NORMALIZATION = false;
    private boolean fNeedNonNormalizedValue = false;
    protected ArrayList<XMLString> attributeValueCache = new ArrayList();
    protected ArrayList<XMLStringBuffer> stringBufferCache = new ArrayList();
    protected int fStringBufferIndex = 0;
    protected boolean fAttributeCacheInitDone = false;
    protected int fAttributeCacheUsedCount = 0;
    protected boolean fValidation = false;
    protected boolean fNamespaces;
    protected boolean fNotifyCharRefs = false;
    protected boolean fParserSettings = true;
    protected PropertyManager fPropertyManager = null;
    protected SymbolTable fSymbolTable;
    protected XMLErrorReporter fErrorReporter;
    protected XMLEntityManager fEntityManager = null;
    protected XMLEntityStorage fEntityStore = null;
    protected XMLSecurityManager fSecurityManager = null;
    protected XMLLimitAnalyzer fLimitAnalyzer = null;
    protected XMLEvent fEvent;
    protected XMLEntityScanner fEntityScanner = null;
    protected int fEntityDepth;
    protected String fCharRefLiteral = null;
    protected boolean fScanningAttribute;
    protected boolean fReportEntity;
    protected static final String fVersionSymbol = "version".intern();
    protected static final String fEncodingSymbol = "encoding".intern();
    protected static final String fStandaloneSymbol = "standalone".intern();
    protected static final String fAmpSymbol = "amp".intern();
    protected static final String fLtSymbol = "lt".intern();
    protected static final String fGtSymbol = "gt".intern();
    protected static final String fQuotSymbol = "quot".intern();
    protected static final String fAposSymbol = "apos".intern();
    private XMLString fString = new XMLString();
    private XMLStringBuffer fStringBuffer = new XMLStringBuffer();
    private XMLStringBuffer fStringBuffer2 = new XMLStringBuffer();
    private XMLStringBuffer fStringBuffer3 = new XMLStringBuffer();
    protected XMLResourceIdentifierImpl fResourceIdentifier = new XMLResourceIdentifierImpl();
    int initialCacheCount = 6;

    @Override
    public void reset(XMLComponentManager componentManager) throws XMLConfigurationException {
        this.fParserSettings = componentManager.getFeature(PARSER_SETTINGS, true);
        if (!this.fParserSettings) {
            this.init();
            return;
        }
        this.fSymbolTable = (SymbolTable)componentManager.getProperty(SYMBOL_TABLE);
        this.fErrorReporter = (XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);
        this.fEntityManager = (XMLEntityManager)componentManager.getProperty(ENTITY_MANAGER);
        this.fSecurityManager = (XMLSecurityManager)componentManager.getProperty(SECURITY_MANAGER);
        this.fEntityStore = this.fEntityManager.getEntityStore();
        this.fValidation = componentManager.getFeature(VALIDATION, false);
        this.fNamespaces = componentManager.getFeature(NAMESPACES, true);
        this.fNotifyCharRefs = componentManager.getFeature(NOTIFY_CHAR_REFS, false);
        this.init();
    }

    protected void setPropertyManager(PropertyManager propertyManager) {
        this.fPropertyManager = propertyManager;
    }

    @Override
    public void setProperty(String propertyId, Object value) throws XMLConfigurationException {
        if (propertyId.startsWith("http://apache.org/xml/properties/")) {
            String property = propertyId.substring("http://apache.org/xml/properties/".length());
            if (property.equals("internal/symbol-table")) {
                this.fSymbolTable = (SymbolTable)value;
            } else if (property.equals("internal/error-reporter")) {
                this.fErrorReporter = (XMLErrorReporter)value;
            } else if (property.equals("internal/entity-manager")) {
                this.fEntityManager = (XMLEntityManager)value;
            }
        }
        if (propertyId.equals(SECURITY_MANAGER)) {
            this.fSecurityManager = (XMLSecurityManager)value;
        }
    }

    @Override
    public void setFeature(String featureId, boolean value) throws XMLConfigurationException {
        if (VALIDATION.equals(featureId)) {
            this.fValidation = value;
        } else if (NOTIFY_CHAR_REFS.equals(featureId)) {
            this.fNotifyCharRefs = value;
        }
    }

    public boolean getFeature(String featureId) throws XMLConfigurationException {
        if (VALIDATION.equals(featureId)) {
            return this.fValidation;
        }
        if (NOTIFY_CHAR_REFS.equals(featureId)) {
            return this.fNotifyCharRefs;
        }
        throw new XMLConfigurationException(Status.NOT_RECOGNIZED, featureId);
    }

    protected void reset() {
        this.init();
        this.fValidation = true;
        this.fNotifyCharRefs = false;
    }

    public void reset(PropertyManager propertyManager) {
        this.init();
        this.fSymbolTable = (SymbolTable)propertyManager.getProperty(SYMBOL_TABLE);
        this.fErrorReporter = (XMLErrorReporter)propertyManager.getProperty(ERROR_REPORTER);
        this.fEntityManager = (XMLEntityManager)propertyManager.getProperty(ENTITY_MANAGER);
        this.fEntityStore = this.fEntityManager.getEntityStore();
        this.fEntityScanner = this.fEntityManager.getEntityScanner();
        this.fSecurityManager = (XMLSecurityManager)propertyManager.getProperty(SECURITY_MANAGER);
        this.fValidation = false;
        this.fNotifyCharRefs = false;
    }

    protected void scanXMLDeclOrTextDecl(boolean scanningTextDecl, String[] pseudoAttributeValues) throws IOException, XNIException {
        String version = null;
        String encoding = null;
        String standalone = null;
        boolean STATE_VERSION = false;
        boolean STATE_ENCODING = true;
        int STATE_STANDALONE = 2;
        int STATE_DONE = 3;
        int state = 0;
        boolean dataFoundForTarget = false;
        boolean sawSpace = this.fEntityScanner.skipSpaces();
        Entity.ScannedEntity currEnt = this.fEntityManager.getCurrentEntity();
        boolean currLiteral = currEnt.literal;
        currEnt.literal = false;
        while (this.fEntityScanner.peekChar() != 63) {
            dataFoundForTarget = true;
            String name = this.scanPseudoAttribute(scanningTextDecl, this.fString);
            switch (state) {
                case 0: {
                    if (name.equals(fVersionSymbol)) {
                        if (!sawSpace) {
                            this.reportFatalError(scanningTextDecl ? "SpaceRequiredBeforeVersionInTextDecl" : "SpaceRequiredBeforeVersionInXMLDecl", null);
                        }
                        version = this.fString.toString();
                        state = 1;
                        if (!this.versionSupported(version)) {
                            this.reportFatalError("VersionNotSupported", new Object[]{version});
                        }
                        if (!version.equals("1.1")) break;
                        Entity.ScannedEntity top = this.fEntityManager.getTopLevelEntity();
                        if (top != null && (top.version == null || top.version.equals("1.0"))) {
                            this.reportFatalError("VersionMismatch", null);
                        }
                        this.fEntityManager.setScannerVersion((short)2);
                        break;
                    }
                    if (name.equals(fEncodingSymbol)) {
                        if (!scanningTextDecl) {
                            this.reportFatalError("VersionInfoRequired", null);
                        }
                        if (!sawSpace) {
                            this.reportFatalError(scanningTextDecl ? "SpaceRequiredBeforeEncodingInTextDecl" : "SpaceRequiredBeforeEncodingInXMLDecl", null);
                        }
                        encoding = this.fString.toString();
                        state = scanningTextDecl ? 3 : 2;
                        break;
                    }
                    if (scanningTextDecl) {
                        this.reportFatalError("EncodingDeclRequired", null);
                        break;
                    }
                    this.reportFatalError("VersionInfoRequired", null);
                    break;
                }
                case 1: {
                    if (name.equals(fEncodingSymbol)) {
                        if (!sawSpace) {
                            this.reportFatalError(scanningTextDecl ? "SpaceRequiredBeforeEncodingInTextDecl" : "SpaceRequiredBeforeEncodingInXMLDecl", null);
                        }
                        encoding = this.fString.toString();
                        state = scanningTextDecl ? 3 : 2;
                        break;
                    }
                    if (!scanningTextDecl && name.equals(fStandaloneSymbol)) {
                        if (!sawSpace) {
                            this.reportFatalError("SpaceRequiredBeforeStandalone", null);
                        }
                        standalone = this.fString.toString();
                        state = 3;
                        if (standalone.equals("yes") || standalone.equals("no")) break;
                        this.reportFatalError("SDDeclInvalid", new Object[]{standalone});
                        break;
                    }
                    this.reportFatalError("EncodingDeclRequired", null);
                    break;
                }
                case 2: {
                    if (name.equals(fStandaloneSymbol)) {
                        if (!sawSpace) {
                            this.reportFatalError("SpaceRequiredBeforeStandalone", null);
                        }
                        standalone = this.fString.toString();
                        state = 3;
                        if (standalone.equals("yes") || standalone.equals("no")) break;
                        this.reportFatalError("SDDeclInvalid", new Object[]{standalone});
                        break;
                    }
                    this.reportFatalError("SDDeclNameInvalid", null);
                    break;
                }
                default: {
                    this.reportFatalError("NoMorePseudoAttributes", null);
                }
            }
            sawSpace = this.fEntityScanner.skipSpaces();
        }
        if (currLiteral) {
            currEnt.literal = true;
        }
        if (scanningTextDecl && state != 3) {
            this.reportFatalError("MorePseudoAttributes", null);
        }
        if (scanningTextDecl) {
            if (!dataFoundForTarget && encoding == null) {
                this.reportFatalError("EncodingDeclRequired", null);
            }
        } else if (!dataFoundForTarget && version == null) {
            this.reportFatalError("VersionInfoRequired", null);
        }
        if (!this.fEntityScanner.skipChar(63, null)) {
            this.reportFatalError("XMLDeclUnterminated", null);
        }
        if (!this.fEntityScanner.skipChar(62, null)) {
            this.reportFatalError("XMLDeclUnterminated", null);
        }
        pseudoAttributeValues[0] = version;
        pseudoAttributeValues[1] = encoding;
        pseudoAttributeValues[2] = standalone;
    }

    protected String scanPseudoAttribute(boolean scanningTextDecl, XMLString value) throws IOException, XNIException {
        String name = this.scanPseudoAttributeName();
        if (name == null) {
            this.reportFatalError("PseudoAttrNameExpected", null);
        }
        this.fEntityScanner.skipSpaces();
        if (!this.fEntityScanner.skipChar(61, null)) {
            this.reportFatalError(scanningTextDecl ? "EqRequiredInTextDecl" : "EqRequiredInXMLDecl", new Object[]{name});
        }
        this.fEntityScanner.skipSpaces();
        int quote = this.fEntityScanner.peekChar();
        if (quote != 39 && quote != 34) {
            this.reportFatalError(scanningTextDecl ? "QuoteRequiredInTextDecl" : "QuoteRequiredInXMLDecl", new Object[]{name});
        }
        this.fEntityScanner.scanChar(NameType.ATTRIBUTE);
        int c = this.fEntityScanner.scanLiteral(quote, value, false);
        if (c != quote) {
            this.fStringBuffer2.clear();
            do {
                this.fStringBuffer2.append(value);
                if (c == -1) continue;
                if (c == 38 || c == 37 || c == 60 || c == 93) {
                    this.fStringBuffer2.append((char)this.fEntityScanner.scanChar(NameType.ATTRIBUTE));
                    continue;
                }
                if (XMLChar.isHighSurrogate(c)) {
                    this.scanSurrogates(this.fStringBuffer2);
                    continue;
                }
                if (!this.isInvalidLiteral(c)) continue;
                String key = scanningTextDecl ? "InvalidCharInTextDecl" : "InvalidCharInXMLDecl";
                this.reportFatalError(key, new Object[]{Integer.toString(c, 16)});
                this.fEntityScanner.scanChar(null);
            } while ((c = this.fEntityScanner.scanLiteral(quote, value, false)) != quote);
            this.fStringBuffer2.append(value);
            value.setValues(this.fStringBuffer2);
        }
        if (!this.fEntityScanner.skipChar(quote, null)) {
            this.reportFatalError(scanningTextDecl ? "CloseQuoteMissingInTextDecl" : "CloseQuoteMissingInXMLDecl", new Object[]{name});
        }
        return name;
    }

    private String scanPseudoAttributeName() throws IOException, XNIException {
        int ch = this.fEntityScanner.peekChar();
        switch (ch) {
            case 118: {
                if (!this.fEntityScanner.skipString(fVersionSymbol)) break;
                return fVersionSymbol;
            }
            case 101: {
                if (!this.fEntityScanner.skipString(fEncodingSymbol)) break;
                return fEncodingSymbol;
            }
            case 115: {
                if (!this.fEntityScanner.skipString(fStandaloneSymbol)) break;
                return fStandaloneSymbol;
            }
        }
        return null;
    }

    protected void scanPI(XMLStringBuffer data) throws IOException, XNIException {
        this.fReportEntity = false;
        String target = this.fEntityScanner.scanName(NameType.PI);
        if (target == null) {
            this.reportFatalError("PITargetRequired", null);
        }
        this.scanPIData(target, data);
        this.fReportEntity = true;
    }

    protected void scanPIData(String target, XMLStringBuffer data) throws IOException, XNIException {
        if (target.length() == 3) {
            char c0 = Character.toLowerCase(target.charAt(0));
            char c1 = Character.toLowerCase(target.charAt(1));
            char c2 = Character.toLowerCase(target.charAt(2));
            if (c0 == 'x' && c1 == 'm' && c2 == 'l') {
                this.reportFatalError("ReservedPITarget", null);
            }
        }
        if (!this.fEntityScanner.skipSpaces()) {
            if (this.fEntityScanner.skipString("?>")) {
                return;
            }
            this.reportFatalError("SpaceRequiredInPI", null);
        }
        if (this.fEntityScanner.scanData("?>", data, 0)) {
            do {
                int c;
                if ((c = this.fEntityScanner.peekChar()) == -1) continue;
                if (XMLChar.isHighSurrogate(c)) {
                    this.scanSurrogates(data);
                    continue;
                }
                if (!this.isInvalidLiteral(c)) continue;
                this.reportFatalError("InvalidCharInPI", new Object[]{Integer.toHexString(c)});
                this.fEntityScanner.scanChar(null);
            } while (this.fEntityScanner.scanData("?>", data, 0));
        }
    }

    protected void scanComment(XMLStringBuffer text) throws IOException, XNIException {
        text.clear();
        while (this.fEntityScanner.scanData("--", text, 0)) {
            int c = this.fEntityScanner.peekChar();
            if (c == -1) continue;
            if (XMLChar.isHighSurrogate(c)) {
                this.scanSurrogates(text);
                continue;
            }
            if (!this.isInvalidLiteral(c)) continue;
            this.reportFatalError("InvalidCharInComment", new Object[]{Integer.toHexString(c)});
            this.fEntityScanner.scanChar(NameType.COMMENT);
        }
        if (!this.fEntityScanner.skipChar(62, NameType.COMMENT)) {
            this.reportFatalError("DashDashInComment", null);
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    protected void scanAttributeValue(XMLString value, XMLString nonNormalizedValue, String atName, XMLAttributes attributes, int attrIndex, boolean checkEntities, String eleName, boolean isNSURI) throws IOException, XNIException {
        int cquote;
        int quote;
        block28: {
            XMLStringBuffer stringBuffer = null;
            quote = this.fEntityScanner.peekChar();
            if (quote != 39 && quote != 34) {
                this.reportFatalError("OpenQuoteExpected", new Object[]{eleName, atName});
            }
            this.fEntityScanner.scanChar(NameType.ATTRIBUTE);
            int entityDepth = this.fEntityDepth;
            int c = this.fEntityScanner.scanLiteral(quote, value, isNSURI);
            if (this.fNeedNonNormalizedValue) {
                this.fStringBuffer2.clear();
                this.fStringBuffer2.append(value);
            }
            if (this.fEntityScanner.whiteSpaceLen > 0) {
                this.normalizeWhitespace(value);
            }
            if (c == quote) break block28;
            this.fScanningAttribute = true;
            stringBuffer = this.getStringBuffer();
            stringBuffer.clear();
            do {
                block31: {
                    block35: {
                        block34: {
                            block33: {
                                block29: {
                                    String entityName;
                                    block32: {
                                        block30: {
                                            int ch;
                                            stringBuffer.append(value);
                                            if (c != 38) break block29;
                                            this.fEntityScanner.skipChar(38, NameType.REFERENCE);
                                            if (entityDepth == this.fEntityDepth && this.fNeedNonNormalizedValue) {
                                                this.fStringBuffer2.append('&');
                                            }
                                            if (!this.fEntityScanner.skipChar(35, NameType.REFERENCE)) break block30;
                                            if (entityDepth == this.fEntityDepth && this.fNeedNonNormalizedValue) {
                                                this.fStringBuffer2.append('#');
                                            }
                                            if ((ch = this.fNeedNonNormalizedValue ? this.scanCharReferenceValue(stringBuffer, this.fStringBuffer2) : this.scanCharReferenceValue(stringBuffer, null)) == -1) {
                                                // empty if block
                                            }
                                            break block31;
                                        }
                                        entityName = this.fEntityScanner.scanName(NameType.ENTITY);
                                        if (entityName == null) {
                                            this.reportFatalError("NameRequiredInReference", null);
                                        } else if (entityDepth == this.fEntityDepth && this.fNeedNonNormalizedValue) {
                                            this.fStringBuffer2.append(entityName);
                                        }
                                        if (!this.fEntityScanner.skipChar(59, NameType.REFERENCE)) {
                                            this.reportFatalError("SemicolonRequiredInReference", new Object[]{entityName});
                                        } else if (entityDepth == this.fEntityDepth && this.fNeedNonNormalizedValue) {
                                            this.fStringBuffer2.append(';');
                                        }
                                        if (!this.resolveCharacter(entityName, stringBuffer)) break block32;
                                        this.checkEntityLimit(false, this.fEntityScanner.fCurrentEntity.name, 1);
                                        break block31;
                                    }
                                    if (this.fEntityStore.isExternalEntity(entityName)) {
                                        this.reportFatalError("ReferenceToExternalEntity", new Object[]{entityName});
                                        break block31;
                                    } else {
                                        if (!this.fEntityStore.isDeclaredEntity(entityName)) {
                                            if (checkEntities) {
                                                if (this.fValidation) {
                                                    this.fErrorReporter.reportError(this.fEntityScanner, "http://www.w3.org/TR/1998/REC-xml-19980210", "EntityNotDeclared", new Object[]{entityName}, (short)1);
                                                }
                                            } else {
                                                this.reportFatalError("EntityNotDeclared", new Object[]{entityName});
                                            }
                                        }
                                        this.fEntityManager.startEntity(true, entityName, true);
                                    }
                                    break block31;
                                }
                                if (c != 60) break block33;
                                this.reportFatalError("LessthanInAttValue", new Object[]{eleName, atName});
                                this.fEntityScanner.scanChar(null);
                                if (entityDepth == this.fEntityDepth && this.fNeedNonNormalizedValue) {
                                    this.fStringBuffer2.append((char)c);
                                }
                                break block31;
                            }
                            if (c != 37 && c != 93) break block34;
                            this.fEntityScanner.scanChar(null);
                            stringBuffer.append((char)c);
                            if (entityDepth == this.fEntityDepth && this.fNeedNonNormalizedValue) {
                                this.fStringBuffer2.append((char)c);
                            }
                            break block31;
                        }
                        if (c == -1 || !XMLChar.isHighSurrogate(c)) break block35;
                        this.fStringBuffer3.clear();
                        if (this.scanSurrogates(this.fStringBuffer3)) {
                            stringBuffer.append(this.fStringBuffer3);
                            if (entityDepth == this.fEntityDepth && this.fNeedNonNormalizedValue) {
                                this.fStringBuffer2.append(this.fStringBuffer3);
                            }
                        }
                        break block31;
                    }
                    if (c != -1 && this.isInvalidLiteral(c)) {
                        this.reportFatalError("InvalidCharInAttValue", new Object[]{eleName, atName, Integer.toString(c, 16)});
                        this.fEntityScanner.scanChar(null);
                        if (entityDepth == this.fEntityDepth && this.fNeedNonNormalizedValue) {
                            this.fStringBuffer2.append((char)c);
                        }
                    }
                }
                c = this.fEntityScanner.scanLiteral(quote, value, isNSURI);
                if (entityDepth == this.fEntityDepth && this.fNeedNonNormalizedValue) {
                    this.fStringBuffer2.append(value);
                }
                if (this.fEntityScanner.whiteSpaceLen <= 0) continue;
                this.normalizeWhitespace(value);
            } while (c != quote || entityDepth != this.fEntityDepth);
            stringBuffer.append(value);
            value.setValues(stringBuffer);
            this.fScanningAttribute = false;
        }
        if (this.fNeedNonNormalizedValue) {
            nonNormalizedValue.setValues(this.fStringBuffer2);
        }
        if ((cquote = this.fEntityScanner.scanChar(NameType.ATTRIBUTE)) != quote) {
            this.reportFatalError("CloseQuoteExpected", new Object[]{eleName, atName});
        }
    }

    protected boolean resolveCharacter(String entityName, XMLStringBuffer stringBuffer) {
        if (entityName == fAmpSymbol) {
            stringBuffer.append('&');
            return true;
        }
        if (entityName == fAposSymbol) {
            stringBuffer.append('\'');
            return true;
        }
        if (entityName == fLtSymbol) {
            stringBuffer.append('<');
            return true;
        }
        if (entityName == fGtSymbol) {
            this.checkEntityLimit(false, this.fEntityScanner.fCurrentEntity.name, 1);
            stringBuffer.append('>');
            return true;
        }
        if (entityName == fQuotSymbol) {
            this.checkEntityLimit(false, this.fEntityScanner.fCurrentEntity.name, 1);
            stringBuffer.append('\"');
            return true;
        }
        return false;
    }

    protected void scanExternalID(String[] identifiers, boolean optionalSystemId) throws IOException, XNIException {
        String systemId = null;
        String publicId = null;
        if (this.fEntityScanner.skipString("PUBLIC")) {
            if (!this.fEntityScanner.skipSpaces()) {
                this.reportFatalError("SpaceRequiredAfterPUBLIC", null);
            }
            this.scanPubidLiteral(this.fString);
            publicId = this.fString.toString();
            if (!this.fEntityScanner.skipSpaces() && !optionalSystemId) {
                this.reportFatalError("SpaceRequiredBetweenPublicAndSystem", null);
            }
        }
        if (publicId != null || this.fEntityScanner.skipString("SYSTEM")) {
            int quote;
            if (publicId == null && !this.fEntityScanner.skipSpaces()) {
                this.reportFatalError("SpaceRequiredAfterSYSTEM", null);
            }
            if ((quote = this.fEntityScanner.peekChar()) != 39 && quote != 34) {
                if (publicId != null && optionalSystemId) {
                    identifiers[0] = null;
                    identifiers[1] = publicId;
                    return;
                }
                this.reportFatalError("QuoteRequiredInSystemID", null);
            }
            this.fEntityScanner.scanChar(null);
            XMLString ident = this.fString;
            if (this.fEntityScanner.scanLiteral(quote, ident, false) != quote) {
                this.fStringBuffer.clear();
                do {
                    this.fStringBuffer.append(ident);
                    int c = this.fEntityScanner.peekChar();
                    if (XMLChar.isMarkup(c) || c == 93) {
                        this.fStringBuffer.append((char)this.fEntityScanner.scanChar(null));
                        continue;
                    }
                    if (c == -1 || !this.isInvalidLiteral(c)) continue;
                    this.reportFatalError("InvalidCharInSystemID", new Object[]{Integer.toString(c, 16)});
                } while (this.fEntityScanner.scanLiteral(quote, ident, false) != quote);
                this.fStringBuffer.append(ident);
                ident = this.fStringBuffer;
            }
            systemId = ident.toString();
            if (!this.fEntityScanner.skipChar(quote, null)) {
                this.reportFatalError("SystemIDUnterminated", null);
            }
        }
        identifiers[0] = systemId;
        identifiers[1] = publicId;
    }

    protected boolean scanPubidLiteral(XMLString literal) throws IOException, XNIException {
        int quote = this.fEntityScanner.scanChar(null);
        if (quote != 39 && quote != 34) {
            this.reportFatalError("QuoteRequiredInPublicID", null);
            return false;
        }
        this.fStringBuffer.clear();
        boolean skipSpace = true;
        boolean dataok = true;
        while (true) {
            int c;
            if ((c = this.fEntityScanner.scanChar(null)) == 32 || c == 10 || c == 13) {
                if (skipSpace) continue;
                this.fStringBuffer.append(' ');
                skipSpace = true;
                continue;
            }
            if (c == quote) {
                if (skipSpace) {
                    --this.fStringBuffer.length;
                }
                break;
            }
            if (XMLChar.isPubid(c)) {
                this.fStringBuffer.append((char)c);
                skipSpace = false;
                continue;
            }
            if (c == -1) {
                this.reportFatalError("PublicIDUnterminated", null);
                return false;
            }
            dataok = false;
            this.reportFatalError("InvalidCharInPublicID", new Object[]{Integer.toHexString(c)});
        }
        literal.setValues(this.fStringBuffer);
        return dataok;
    }

    protected void normalizeWhitespace(XMLString value) {
        int j = 0;
        int[] buff = this.fEntityScanner.whiteSpaceLookup;
        int buffLen = this.fEntityScanner.whiteSpaceLen;
        int end = value.offset + value.length;
        for (int i = 0; i < buffLen; ++i) {
            j = buff[i];
            if (j >= end) continue;
            value.ch[j] = 32;
        }
    }

    public void startEntity(String name, XMLResourceIdentifier identifier, String encoding, Augmentations augs) throws XNIException {
        ++this.fEntityDepth;
        this.fEntityScanner = this.fEntityManager.getEntityScanner();
        this.fEntityStore = this.fEntityManager.getEntityStore();
    }

    public void endEntity(String name, Augmentations augs) throws IOException, XNIException {
        if (this.fEntityDepth > 0) {
            --this.fEntityDepth;
        }
    }

    protected int scanCharReferenceValue(XMLStringBuffer buf, XMLStringBuffer buf2) throws IOException, XNIException {
        int initLen = buf.length;
        boolean hex = false;
        if (this.fEntityScanner.skipChar(120, NameType.REFERENCE)) {
            if (buf2 != null) {
                buf2.append('x');
            }
            hex = true;
            this.fStringBuffer3.clear();
            digit = true;
            c = this.fEntityScanner.peekChar();
            boolean bl = digit = c >= 48 && c <= 57 || c >= 97 && c <= 102 || c >= 65 && c <= 70;
            if (digit) {
                if (buf2 != null) {
                    buf2.append((char)c);
                }
                this.fEntityScanner.scanChar(NameType.REFERENCE);
                this.fStringBuffer3.append((char)c);
                do {
                    boolean bl2 = digit = (c = this.fEntityScanner.peekChar()) >= 48 && c <= 57 || c >= 97 && c <= 102 || c >= 65 && c <= 70;
                    if (!digit) continue;
                    if (buf2 != null) {
                        buf2.append((char)c);
                    }
                    this.fEntityScanner.scanChar(NameType.REFERENCE);
                    this.fStringBuffer3.append((char)c);
                } while (digit);
            } else {
                this.reportFatalError("HexdigitRequiredInCharRef", null);
            }
        } else {
            this.fStringBuffer3.clear();
            digit = true;
            c = this.fEntityScanner.peekChar();
            boolean bl = digit = c >= 48 && c <= 57;
            if (digit) {
                if (buf2 != null) {
                    buf2.append((char)c);
                }
                this.fEntityScanner.scanChar(NameType.REFERENCE);
                this.fStringBuffer3.append((char)c);
                do {
                    boolean bl3 = digit = (c = this.fEntityScanner.peekChar()) >= 48 && c <= 57;
                    if (!digit) continue;
                    if (buf2 != null) {
                        buf2.append((char)c);
                    }
                    this.fEntityScanner.scanChar(NameType.REFERENCE);
                    this.fStringBuffer3.append((char)c);
                } while (digit);
            } else {
                this.reportFatalError("DigitRequiredInCharRef", null);
            }
        }
        if (!this.fEntityScanner.skipChar(59, NameType.REFERENCE)) {
            this.reportFatalError("SemicolonRequiredInCharRef", null);
        }
        if (buf2 != null) {
            buf2.append(';');
        }
        int value = -1;
        try {
            value = Integer.parseInt(this.fStringBuffer3.toString(), hex ? 16 : 10);
            if (this.isInvalid(value)) {
                StringBuffer errorBuf = new StringBuffer(this.fStringBuffer3.length + 1);
                if (hex) {
                    errorBuf.append('x');
                }
                errorBuf.append(this.fStringBuffer3.ch, this.fStringBuffer3.offset, this.fStringBuffer3.length);
                this.reportFatalError("InvalidCharRef", new Object[]{errorBuf.toString()});
            }
        }
        catch (NumberFormatException e) {
            StringBuffer errorBuf = new StringBuffer(this.fStringBuffer3.length + 1);
            if (hex) {
                errorBuf.append('x');
            }
            errorBuf.append(this.fStringBuffer3.ch, this.fStringBuffer3.offset, this.fStringBuffer3.length);
            this.reportFatalError("InvalidCharRef", new Object[]{errorBuf.toString()});
        }
        if (!XMLChar.isSupplemental(value)) {
            buf.append((char)value);
        } else {
            buf.append(XMLChar.highSurrogate(value));
            buf.append(XMLChar.lowSurrogate(value));
        }
        if (this.fNotifyCharRefs && value != -1) {
            String literal = "#" + (hex ? "x" : "") + this.fStringBuffer3.toString();
            if (!this.fScanningAttribute) {
                this.fCharRefLiteral = literal;
            }
        }
        if (this.fEntityScanner.fCurrentEntity.isGE) {
            this.checkEntityLimit(false, this.fEntityScanner.fCurrentEntity.name, buf.length - initLen);
        }
        return value;
    }

    protected boolean isInvalid(int value) {
        return XMLChar.isInvalid(value);
    }

    protected boolean isInvalidLiteral(int value) {
        return XMLChar.isInvalid(value);
    }

    protected boolean isValidNameChar(int value) {
        return XMLChar.isName(value);
    }

    protected boolean isValidNCName(int value) {
        return XMLChar.isNCName(value);
    }

    protected boolean isValidNameStartChar(int value) {
        return XMLChar.isNameStart(value);
    }

    protected boolean isValidNameStartHighSurrogate(int value) {
        return false;
    }

    protected boolean versionSupported(String version) {
        return version.equals("1.0") || version.equals("1.1");
    }

    protected boolean scanSurrogates(XMLStringBuffer buf) throws IOException, XNIException {
        int high = this.fEntityScanner.scanChar(null);
        int low = this.fEntityScanner.peekChar();
        if (!XMLChar.isLowSurrogate(low)) {
            this.reportFatalError("InvalidCharInContent", new Object[]{Integer.toString(high, 16)});
            return false;
        }
        this.fEntityScanner.scanChar(null);
        int c = XMLChar.supplemental((char)high, (char)low);
        if (this.isInvalid(c)) {
            this.reportFatalError("InvalidCharInContent", new Object[]{Integer.toString(c, 16)});
            return false;
        }
        buf.append((char)high);
        buf.append((char)low);
        return true;
    }

    protected void reportFatalError(String msgId, Object[] args) throws XNIException {
        this.fErrorReporter.reportError(this.fEntityScanner, "http://www.w3.org/TR/1998/REC-xml-19980210", msgId, args, (short)2);
    }

    private void init() {
        this.fEntityScanner = null;
        this.fEntityDepth = 0;
        this.fReportEntity = true;
        this.fResourceIdentifier.clear();
        if (!this.fAttributeCacheInitDone) {
            for (int i = 0; i < this.initialCacheCount; ++i) {
                this.attributeValueCache.add(new XMLString());
                this.stringBufferCache.add(new XMLStringBuffer());
            }
            this.fAttributeCacheInitDone = true;
        }
        this.fStringBufferIndex = 0;
        this.fAttributeCacheUsedCount = 0;
    }

    XMLStringBuffer getStringBuffer() {
        if (this.fStringBufferIndex < this.initialCacheCount || this.fStringBufferIndex < this.stringBufferCache.size()) {
            return this.stringBufferCache.get(this.fStringBufferIndex++);
        }
        XMLStringBuffer tmpObj = new XMLStringBuffer();
        ++this.fStringBufferIndex;
        this.stringBufferCache.add(tmpObj);
        return tmpObj;
    }

    void checkEntityLimit(boolean isPEDecl, String entityName, XMLString buffer) {
        this.checkEntityLimit(isPEDecl, entityName, buffer.length);
    }

    void checkEntityLimit(boolean isPEDecl, String entityName, int len) {
        if (this.fLimitAnalyzer == null) {
            this.fLimitAnalyzer = this.fEntityManager.fLimitAnalyzer;
        }
        if (isPEDecl) {
            this.fLimitAnalyzer.addValue(XMLSecurityManager.Limit.PARAMETER_ENTITY_SIZE_LIMIT, "%" + entityName, len);
            if (this.fSecurityManager.isOverLimit(XMLSecurityManager.Limit.PARAMETER_ENTITY_SIZE_LIMIT, this.fLimitAnalyzer)) {
                this.fSecurityManager.debugPrint(this.fLimitAnalyzer);
                this.reportFatalError("MaxEntitySizeLimit", new Object[]{"%" + entityName, this.fLimitAnalyzer.getValue(XMLSecurityManager.Limit.PARAMETER_ENTITY_SIZE_LIMIT), this.fSecurityManager.getLimit(XMLSecurityManager.Limit.PARAMETER_ENTITY_SIZE_LIMIT), this.fSecurityManager.getStateLiteral(XMLSecurityManager.Limit.PARAMETER_ENTITY_SIZE_LIMIT)});
            }
        } else {
            this.fLimitAnalyzer.addValue(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT, entityName, len);
            if (this.fSecurityManager.isOverLimit(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT, this.fLimitAnalyzer)) {
                this.fSecurityManager.debugPrint(this.fLimitAnalyzer);
                this.reportFatalError("MaxEntitySizeLimit", new Object[]{entityName, this.fLimitAnalyzer.getValue(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT), this.fSecurityManager.getLimit(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT), this.fSecurityManager.getStateLiteral(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT)});
            }
        }
        if (this.fSecurityManager.isOverLimit(XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT, this.fLimitAnalyzer)) {
            this.fSecurityManager.debugPrint(this.fLimitAnalyzer);
            this.reportFatalError("TotalEntitySizeLimit", new Object[]{this.fLimitAnalyzer.getTotalValue(XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT), this.fSecurityManager.getLimit(XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT), this.fSecurityManager.getStateLiteral(XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT)});
        }
    }

    public static enum NameType {
        ATTRIBUTE("attribute"),
        ATTRIBUTENAME("attribute name"),
        COMMENT("comment"),
        DOCTYPE("doctype"),
        ELEMENTSTART("startelement"),
        ELEMENTEND("endelement"),
        ENTITY("entity"),
        NOTATION("notation"),
        PI("pi"),
        REFERENCE("reference");

        final String literal;

        private NameType(String literal) {
            this.literal = literal;
        }

        String literal() {
            return this.literal;
        }
    }
}

