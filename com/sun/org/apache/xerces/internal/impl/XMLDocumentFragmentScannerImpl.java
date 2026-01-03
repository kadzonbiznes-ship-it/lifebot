/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.impl;

import com.sun.org.apache.xerces.internal.impl.ExternalSubsetResolver;
import com.sun.org.apache.xerces.internal.impl.PropertyManager;
import com.sun.org.apache.xerces.internal.impl.XMLEntityHandler;
import com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import com.sun.org.apache.xerces.internal.impl.XMLScanner;
import com.sun.org.apache.xerces.internal.impl.io.MalformedByteSequenceException;
import com.sun.org.apache.xerces.internal.util.AugmentationsImpl;
import com.sun.org.apache.xerces.internal.util.XMLAttributesIteratorImpl;
import com.sun.org.apache.xerces.internal.util.XMLChar;
import com.sun.org.apache.xerces.internal.util.XMLStringBuffer;
import com.sun.org.apache.xerces.internal.util.XMLSymbols;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import com.sun.org.apache.xerces.internal.xni.Augmentations;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XMLString;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentScanner;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import com.sun.xml.internal.stream.XMLBufferListener;
import com.sun.xml.internal.stream.XMLEntityStorage;
import com.sun.xml.internal.stream.dtd.DTDGrammarUtil;
import java.io.CharConversionException;
import java.io.EOFException;
import java.io.IOException;
import jdk.xml.internal.JdkConstants;
import jdk.xml.internal.JdkXmlUtils;
import jdk.xml.internal.SecuritySupport;

public class XMLDocumentFragmentScannerImpl
extends XMLScanner
implements XMLDocumentScanner,
XMLComponent,
XMLEntityHandler,
XMLBufferListener {
    protected int fElementAttributeLimit;
    protected int fXMLNameLimit;
    protected ExternalSubsetResolver fExternalSubsetResolver;
    protected static final int SCANNER_STATE_START_OF_MARKUP = 21;
    protected static final int SCANNER_STATE_CONTENT = 22;
    protected static final int SCANNER_STATE_PI = 23;
    protected static final int SCANNER_STATE_DOCTYPE = 24;
    protected static final int SCANNER_STATE_XML_DECL = 25;
    protected static final int SCANNER_STATE_ROOT_ELEMENT = 26;
    protected static final int SCANNER_STATE_COMMENT = 27;
    protected static final int SCANNER_STATE_REFERENCE = 28;
    protected static final int SCANNER_STATE_ATTRIBUTE = 29;
    protected static final int SCANNER_STATE_ATTRIBUTE_VALUE = 30;
    protected static final int SCANNER_STATE_END_OF_INPUT = 33;
    protected static final int SCANNER_STATE_TERMINATED = 34;
    protected static final int SCANNER_STATE_CDATA = 35;
    protected static final int SCANNER_STATE_TEXT_DECL = 36;
    protected static final int SCANNER_STATE_CHARACTER_DATA = 37;
    protected static final int SCANNER_STATE_START_ELEMENT_TAG = 38;
    protected static final int SCANNER_STATE_END_ELEMENT_TAG = 39;
    protected static final int SCANNER_STATE_CHAR_REFERENCE = 40;
    protected static final int SCANNER_STATE_BUILT_IN_REFS = 41;
    protected static final String NOTIFY_BUILTIN_REFS = "http://apache.org/xml/features/scanner/notify-builtin-refs";
    protected static final String ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
    protected static final String STANDARD_URI_CONFORMANT = "http://apache.org/xml/features/standard-uri-conformant";
    protected static final String CREATE_ENTITY_REF_NODES = "http://apache.org/xml/features/dom/create-entity-ref-nodes";
    private static final String XML_SECURITY_PROPERTY_MANAGER = "jdk.xml.xmlSecurityPropertyManager";
    static final String EXTERNAL_ACCESS_DEFAULT = "all";
    private static final String[] RECOGNIZED_FEATURES = new String[]{"http://xml.org/sax/features/namespaces", "http://xml.org/sax/features/validation", "http://apache.org/xml/features/scanner/notify-builtin-refs", "http://apache.org/xml/features/scanner/notify-char-refs", "report-cdata-event", "http://javax.xml.XMLConstants/feature/useCatalog"};
    private static final Boolean[] FEATURE_DEFAULTS = new Boolean[]{Boolean.TRUE, null, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, JdkXmlUtils.USE_CATALOG_DEFAULT};
    private static final String[] RECOGNIZED_PROPERTIES = new String[]{"http://apache.org/xml/properties/internal/symbol-table", "http://apache.org/xml/properties/internal/error-reporter", "http://apache.org/xml/properties/internal/entity-manager", "jdk.xml.xmlSecurityPropertyManager", JdkXmlUtils.CATALOG_DEFER, JdkXmlUtils.CATALOG_FILES, JdkXmlUtils.CATALOG_PREFER, JdkXmlUtils.CATALOG_RESOLVE, "jdk.xml.cdataChunkSize"};
    private static final Object[] PROPERTY_DEFAULTS = new Object[]{null, null, null, null, null, null, null, null, JdkConstants.CDATA_CHUNK_SIZE_DEFAULT};
    private static final char[] CDATA = new char[]{'[', 'C', 'D', 'A', 'T', 'A', '['};
    static final char[] XMLDECL = new char[]{'<', '?', 'x', 'm', 'l'};
    private static final boolean DEBUG_SCANNER_STATE = false;
    private static final boolean DEBUG_DISPATCHER = false;
    protected static final boolean DEBUG_START_END_ELEMENT = false;
    protected static final boolean DEBUG = false;
    protected XMLDocumentHandler fDocumentHandler;
    protected int fScannerLastState;
    protected XMLEntityStorage fEntityStore;
    protected int[] fEntityStack = new int[4];
    protected int fMarkupDepth;
    protected boolean fEmptyElement;
    protected boolean fReadingAttributes = false;
    protected int fScannerState;
    protected boolean fInScanContent = false;
    protected boolean fLastSectionWasCData = false;
    protected boolean fCDataStart = false;
    protected boolean fInCData = false;
    protected boolean fCDataEnd = false;
    protected boolean fLastSectionWasEntityReference = false;
    protected boolean fLastSectionWasCharacterData = false;
    protected boolean fHasExternalDTD;
    protected boolean fStandaloneSet;
    protected boolean fStandalone;
    protected String fVersion;
    protected QName fCurrentElement;
    protected ElementStack fElementStack = new ElementStack();
    protected ElementStack2 fElementStack2 = new ElementStack2();
    protected String fPITarget;
    protected XMLString fPIData = new XMLString();
    protected boolean fNotifyBuiltInRefs = false;
    protected boolean fSupportDTD = true;
    protected boolean fReplaceEntityReferences = true;
    protected boolean fSupportExternalEntities = false;
    protected boolean fReportCdataEvent = false;
    protected boolean fIsCoalesce = false;
    protected String fDeclaredEncoding = null;
    protected boolean fDisallowDoctype = false;
    protected boolean fCreateEntityRefNodes = false;
    private int fChunkSize;
    protected String fAccessExternalDTD = "all";
    protected boolean fStrictURI;
    protected Driver fDriver;
    protected Driver fContentDriver = this.createContentDriver();
    protected QName fElementQName = new QName();
    protected QName fAttributeQName = new QName();
    protected XMLAttributesIteratorImpl fAttributes = new XMLAttributesIteratorImpl();
    protected XMLString fTempString = new XMLString();
    protected XMLString fTempString2 = new XMLString();
    private final String[] fStrings = new String[3];
    protected XMLStringBuffer fStringBuffer = new XMLStringBuffer();
    protected XMLStringBuffer fStringBuffer2 = new XMLStringBuffer();
    protected XMLStringBuffer fContentBuffer = new XMLStringBuffer();
    private final char[] fSingleChar = new char[1];
    private String fCurrentEntityName = null;
    protected boolean fScanToEnd = false;
    protected DTDGrammarUtil dtdGrammarUtil = null;
    protected boolean fAddDefaultAttr = false;
    protected boolean foundBuiltInRefs = false;
    protected boolean builtInRefCharacterHandled = false;
    static final short MAX_DEPTH_LIMIT = 5;
    static final short ELEMENT_ARRAY_LENGTH = 200;
    static final short MAX_POINTER_AT_A_DEPTH = 4;
    static final boolean DEBUG_SKIP_ALGORITHM = false;
    String[] fElementArray = new String[200];
    short fLastPointerLocation = 0;
    short fElementPointer = 0;
    short[][] fPointerInfo = new short[5][4];
    protected String fElementRawname;
    protected boolean fShouldSkip = false;
    protected boolean fAdd = false;
    protected boolean fSkip = false;
    private Augmentations fTempAugmentations = null;
    protected boolean fUsebuffer;

    @Override
    public void setInputSource(XMLInputSource inputSource) throws IOException {
        this.fEntityManager.setEntityHandler(this);
        this.fEntityManager.startEntity(false, "$fragment$", inputSource, false, true);
    }

    @Override
    public boolean scanDocument(boolean complete) throws IOException, XNIException {
        this.fEntityManager.setEntityHandler(this);
        int event = this.next();
        block16: do {
            switch (event) {
                case 7: {
                    break;
                }
                case 1: {
                    break;
                }
                case 4: {
                    this.fEntityScanner.checkNodeCount(this.fEntityScanner.fCurrentEntity);
                    this.fDocumentHandler.characters(this.getCharacterData(), null);
                    break;
                }
                case 6: {
                    break;
                }
                case 9: {
                    this.fEntityScanner.checkNodeCount(this.fEntityScanner.fCurrentEntity);
                    break;
                }
                case 3: {
                    this.fEntityScanner.checkNodeCount(this.fEntityScanner.fCurrentEntity);
                    this.fDocumentHandler.processingInstruction(this.getPITarget(), this.getPIData(), null);
                    break;
                }
                case 5: {
                    this.fEntityScanner.checkNodeCount(this.fEntityScanner.fCurrentEntity);
                    this.fDocumentHandler.comment(this.getCharacterData(), null);
                    break;
                }
                case 11: {
                    break;
                }
                case 12: {
                    this.fEntityScanner.checkNodeCount(this.fEntityScanner.fCurrentEntity);
                    if (this.fCDataStart) {
                        this.fDocumentHandler.startCDATA(null);
                        this.fCDataStart = false;
                        this.fInCData = true;
                    }
                    this.fDocumentHandler.characters(this.getCharacterData(), null);
                    if (!this.fCDataEnd) continue block16;
                    this.fDocumentHandler.endCDATA(null);
                    this.fCDataEnd = false;
                    break;
                }
                case 14: {
                    break;
                }
                case 15: {
                    break;
                }
                case 13: {
                    break;
                }
                case 10: {
                    break;
                }
                case 2: {
                    break;
                }
                default: {
                    return false;
                }
            }
        } while ((event = this.next()) != 8 && complete);
        if (event == 8) {
            this.fDocumentHandler.endDocument(null);
            return false;
        }
        return true;
    }

    public QName getElementQName() {
        if (this.fScannerLastState == 2) {
            this.fElementQName.setValues(this.fElementStack.getLastPoppedElement());
        }
        return this.fElementQName;
    }

    @Override
    public int next() throws IOException, XNIException {
        return this.fDriver.next();
    }

    @Override
    public void reset(XMLComponentManager componentManager) throws XMLConfigurationException {
        super.reset(componentManager);
        this.fReportCdataEvent = componentManager.getFeature("report-cdata-event", true);
        this.fSecurityManager = (XMLSecurityManager)componentManager.getProperty("http://apache.org/xml/properties/security-manager", null);
        this.fNotifyBuiltInRefs = componentManager.getFeature(NOTIFY_BUILTIN_REFS, false);
        this.fCreateEntityRefNodes = componentManager.getFeature(CREATE_ENTITY_REF_NODES, this.fCreateEntityRefNodes);
        Object resolver = componentManager.getProperty(ENTITY_RESOLVER, null);
        this.fExternalSubsetResolver = resolver instanceof ExternalSubsetResolver ? (ExternalSubsetResolver)resolver : null;
        this.fReadingAttributes = false;
        this.fSupportExternalEntities = true;
        this.fReplaceEntityReferences = true;
        this.fIsCoalesce = false;
        this.setScannerState(22);
        this.setDriver(this.fContentDriver);
        XMLSecurityPropertyManager spm = (XMLSecurityPropertyManager)componentManager.getProperty(XML_SECURITY_PROPERTY_MANAGER, null);
        this.fAccessExternalDTD = spm.getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD);
        this.fStrictURI = componentManager.getFeature(STANDARD_URI_CONFORMANT, false);
        this.fChunkSize = JdkXmlUtils.getValue(componentManager.getProperty("jdk.xml.cdataChunkSize"), JdkConstants.CDATA_CHUNK_SIZE_DEFAULT);
        this.resetCommon();
    }

    @Override
    public void reset(PropertyManager propertyManager) {
        Boolean coalesce;
        super.reset(propertyManager);
        this.fNamespaces = (Boolean)propertyManager.getProperty("javax.xml.stream.isNamespaceAware");
        this.fNotifyBuiltInRefs = false;
        Boolean bo = (Boolean)propertyManager.getProperty("javax.xml.stream.isReplacingEntityReferences");
        this.fReplaceEntityReferences = bo;
        bo = (Boolean)propertyManager.getProperty("javax.xml.stream.isSupportingExternalEntities");
        this.fSupportExternalEntities = bo;
        Boolean cdata = (Boolean)propertyManager.getProperty("http://java.sun.com/xml/stream/properties/report-cdata-event");
        if (cdata != null) {
            this.fReportCdataEvent = cdata;
        }
        if ((coalesce = (Boolean)propertyManager.getProperty("javax.xml.stream.isCoalescing")) != null) {
            this.fIsCoalesce = coalesce;
        }
        this.fReportCdataEvent = this.fIsCoalesce ? false : this.fReportCdataEvent;
        this.fReplaceEntityReferences = this.fIsCoalesce ? true : this.fReplaceEntityReferences;
        XMLSecurityPropertyManager spm = (XMLSecurityPropertyManager)propertyManager.getProperty(XML_SECURITY_PROPERTY_MANAGER);
        this.fAccessExternalDTD = spm.getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD);
        this.fSecurityManager = (XMLSecurityManager)propertyManager.getProperty("http://apache.org/xml/properties/security-manager");
        this.fChunkSize = JdkXmlUtils.getValue(propertyManager.getProperty("jdk.xml.cdataChunkSize"), JdkConstants.CDATA_CHUNK_SIZE_DEFAULT);
        this.resetCommon();
    }

    void resetCommon() {
        this.fMarkupDepth = 0;
        this.fCurrentElement = null;
        this.fElementStack.clear();
        this.fHasExternalDTD = false;
        this.fStandaloneSet = false;
        this.fStandalone = false;
        this.fInScanContent = false;
        this.fShouldSkip = false;
        this.fAdd = false;
        this.fSkip = false;
        this.fEntityStore = this.fEntityManager.getEntityStore();
        this.dtdGrammarUtil = null;
        if (this.fSecurityManager != null) {
            this.fElementAttributeLimit = this.fSecurityManager.getLimit(XMLSecurityManager.Limit.ELEMENT_ATTRIBUTE_LIMIT);
            this.fXMLNameLimit = this.fSecurityManager.getLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT);
        } else {
            this.fElementAttributeLimit = 0;
            this.fXMLNameLimit = XMLSecurityManager.Limit.MAX_NAME_LIMIT.defaultValue();
        }
        this.fLimitAnalyzer = this.fEntityManager.fLimitAnalyzer;
    }

    @Override
    public String[] getRecognizedFeatures() {
        return (String[])RECOGNIZED_FEATURES.clone();
    }

    @Override
    public void setFeature(String featureId, boolean state) throws XMLConfigurationException {
        String feature;
        super.setFeature(featureId, state);
        if (featureId.startsWith("http://apache.org/xml/features/") && (feature = featureId.substring("http://apache.org/xml/features/".length())).equals("scanner/notify-builtin-refs")) {
            this.fNotifyBuiltInRefs = state;
        }
    }

    @Override
    public String[] getRecognizedProperties() {
        return (String[])RECOGNIZED_PROPERTIES.clone();
    }

    @Override
    public void setProperty(String propertyId, Object value) throws XMLConfigurationException {
        super.setProperty(propertyId, value);
        if (propertyId.startsWith("http://apache.org/xml/properties/")) {
            int suffixLength = propertyId.length() - "http://apache.org/xml/properties/".length();
            if (suffixLength == "internal/entity-manager".length() && propertyId.endsWith("internal/entity-manager")) {
                this.fEntityManager = (XMLEntityManager)value;
                return;
            }
            if (suffixLength == "internal/entity-resolver".length() && propertyId.endsWith("internal/entity-resolver")) {
                this.fExternalSubsetResolver = value instanceof ExternalSubsetResolver ? (ExternalSubsetResolver)value : null;
                return;
            }
        }
        if (propertyId.startsWith("http://apache.org/xml/properties/")) {
            String property = propertyId.substring("http://apache.org/xml/properties/".length());
            if (property.equals("internal/entity-manager")) {
                this.fEntityManager = (XMLEntityManager)value;
            }
            return;
        }
        if (propertyId.equals(XML_SECURITY_PROPERTY_MANAGER)) {
            XMLSecurityPropertyManager spm = (XMLSecurityPropertyManager)value;
            this.fAccessExternalDTD = spm.getValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD);
        }
    }

    @Override
    public Boolean getFeatureDefault(String featureId) {
        for (int i = 0; i < RECOGNIZED_FEATURES.length; ++i) {
            if (!RECOGNIZED_FEATURES[i].equals(featureId)) continue;
            return FEATURE_DEFAULTS[i];
        }
        return null;
    }

    @Override
    public Object getPropertyDefault(String propertyId) {
        for (int i = 0; i < RECOGNIZED_PROPERTIES.length; ++i) {
            if (!RECOGNIZED_PROPERTIES[i].equals(propertyId)) continue;
            return PROPERTY_DEFAULTS[i];
        }
        return null;
    }

    @Override
    public void setDocumentHandler(XMLDocumentHandler documentHandler) {
        this.fDocumentHandler = documentHandler;
    }

    @Override
    public XMLDocumentHandler getDocumentHandler() {
        return this.fDocumentHandler;
    }

    @Override
    public void startEntity(String name, XMLResourceIdentifier identifier, String encoding, Augmentations augs) throws XNIException {
        if (this.fEntityDepth == this.fEntityStack.length) {
            int[] entityarray = new int[this.fEntityStack.length * 2];
            System.arraycopy(this.fEntityStack, 0, entityarray, 0, this.fEntityStack.length);
            this.fEntityStack = entityarray;
        }
        this.fEntityStack[this.fEntityDepth] = this.fMarkupDepth;
        super.startEntity(name, identifier, encoding, augs);
        if (this.fStandalone && this.fEntityStore.isEntityDeclInExternalSubset(name)) {
            this.reportFatalError("MSG_REFERENCE_TO_EXTERNALLY_DECLARED_ENTITY_WHEN_STANDALONE", new Object[]{name});
        }
        if (this.fDocumentHandler != null && !this.fScanningAttribute && !name.equals("[xml]")) {
            this.fDocumentHandler.startGeneralEntity(name, identifier, encoding, augs);
        }
    }

    @Override
    public void endEntity(String name, Augmentations augs) throws IOException, XNIException {
        super.endEntity(name, augs);
        if (this.fMarkupDepth != this.fEntityStack[this.fEntityDepth]) {
            this.reportFatalError("MarkupEntityMismatch", null);
        }
        if (this.fDocumentHandler != null && !this.fScanningAttribute && !name.equals("[xml]")) {
            this.fDocumentHandler.endGeneralEntity(name, augs);
        }
    }

    protected Driver createContentDriver() {
        return new FragmentContentDriver();
    }

    protected void scanXMLDeclOrTextDecl(boolean scanningTextDecl) throws IOException, XNIException {
        super.scanXMLDeclOrTextDecl(scanningTextDecl, this.fStrings);
        --this.fMarkupDepth;
        String version = this.fStrings[0];
        String encoding = this.fStrings[1];
        String standalone = this.fStrings[2];
        this.fDeclaredEncoding = encoding;
        this.fStandaloneSet = standalone != null;
        this.fStandalone = this.fStandaloneSet && standalone.equals("yes");
        this.fEntityManager.setStandalone(this.fStandalone);
        if (this.fDocumentHandler != null) {
            if (scanningTextDecl) {
                this.fDocumentHandler.textDecl(version, encoding, null);
            } else {
                this.fDocumentHandler.xmlDecl(version, encoding, standalone, null);
            }
        }
        if (version != null) {
            this.fEntityScanner.setVersion(version);
            this.fEntityScanner.setXMLVersion(version);
        }
        if (encoding != null && !this.fEntityScanner.getCurrentEntity().isEncodingExternallySpecified()) {
            this.fEntityScanner.setEncoding(encoding);
        }
    }

    public String getPITarget() {
        return this.fPITarget;
    }

    public XMLStringBuffer getPIData() {
        return this.fContentBuffer;
    }

    public XMLString getCharacterData() {
        if (this.fUsebuffer) {
            return this.fContentBuffer;
        }
        return this.fTempString;
    }

    @Override
    protected void scanPIData(String target, XMLStringBuffer data) throws IOException, XNIException {
        super.scanPIData(target, data);
        this.fPITarget = target;
        --this.fMarkupDepth;
    }

    protected void scanComment() throws IOException, XNIException {
        this.fContentBuffer.clear();
        this.scanComment(this.fContentBuffer);
        this.fUsebuffer = true;
        --this.fMarkupDepth;
    }

    public String getComment() {
        return this.fContentBuffer.toString();
    }

    void addElement(String rawname) {
        if (this.fElementPointer < 200) {
            short column;
            this.fElementArray[this.fElementPointer] = rawname;
            if (this.fElementStack.fDepth < 5 && (column = this.storePointerForADepth(this.fElementPointer)) > 0) {
                short pointer = this.getElementPointer((short)this.fElementStack.fDepth, (short)(column - 1));
                if (rawname == this.fElementArray[pointer]) {
                    this.fShouldSkip = true;
                    this.fLastPointerLocation = pointer;
                    this.resetPointer((short)this.fElementStack.fDepth, column);
                    this.fElementArray[this.fElementPointer] = null;
                    return;
                }
                this.fShouldSkip = false;
            }
            this.fElementPointer = (short)(this.fElementPointer + 1);
        }
    }

    void resetPointer(short depth, short column) {
        this.fPointerInfo[depth][column] = 0;
    }

    short storePointerForADepth(short elementPointer) {
        short depth = (short)this.fElementStack.fDepth;
        for (short i = 0; i < 4; i = (short)(i + 1)) {
            if (!this.canStore(depth, i)) continue;
            this.fPointerInfo[depth][i] = elementPointer;
            return i;
        }
        return -1;
    }

    boolean canStore(short depth, short column) {
        return this.fPointerInfo[depth][column] == 0;
    }

    short getElementPointer(short depth, short column) {
        return this.fPointerInfo[depth][column];
    }

    boolean skipFromTheBuffer(String rawname) throws IOException {
        if (this.fEntityScanner.skipString(rawname)) {
            char c = (char)this.fEntityScanner.peekChar();
            if (c == ' ' || c == '/' || c == '>') {
                this.fElementRawname = rawname;
                return true;
            }
            return false;
        }
        return false;
    }

    boolean skipQElement(String rawname) throws IOException {
        int c = this.fEntityScanner.getChar(rawname.length());
        if (XMLChar.isName(c)) {
            return false;
        }
        return this.fEntityScanner.skipString(rawname);
    }

    protected boolean skipElement() throws IOException {
        if (!this.fShouldSkip) {
            return false;
        }
        if (this.fLastPointerLocation != 0) {
            String rawname = this.fElementArray[this.fLastPointerLocation + 1];
            if (rawname != null && this.skipFromTheBuffer(rawname)) {
                this.fLastPointerLocation = (short)(this.fLastPointerLocation + 1);
                return true;
            }
            this.fLastPointerLocation = 0;
        }
        return this.fShouldSkip && this.skipElement((short)0);
    }

    boolean skipElement(short column) throws IOException {
        short depth = (short)this.fElementStack.fDepth;
        if (depth > 5) {
            this.fShouldSkip = false;
            return false;
        }
        for (short i = column; i < 4; i = (short)(i + 1)) {
            short pointer = this.getElementPointer(depth, i);
            if (pointer == 0) {
                this.fShouldSkip = false;
                return false;
            }
            if (this.fElementArray[pointer] == null || !this.skipFromTheBuffer(this.fElementArray[pointer])) continue;
            this.fLastPointerLocation = pointer;
            this.fShouldSkip = true;
            return true;
        }
        this.fShouldSkip = false;
        return false;
    }

    protected boolean scanStartElement() throws IOException, XNIException {
        Object name;
        if (this.fSkip && !this.fAdd) {
            name = this.fElementStack.getNext();
            this.fSkip = this.fEntityScanner.skipString(((QName)name).rawname);
            if (this.fSkip) {
                this.fElementStack.push();
                this.fElementQName = name;
            } else {
                this.fElementStack.reposition();
            }
        }
        if (!this.fSkip || this.fAdd) {
            this.fElementQName = this.fElementStack.nextElement();
            if (this.fNamespaces) {
                this.fEntityScanner.scanQName(this.fElementQName, XMLScanner.NameType.ELEMENTSTART);
            } else {
                name = this.fEntityScanner.scanName(XMLScanner.NameType.ELEMENTSTART);
                this.fElementQName.setValues(null, (String)name, (String)name, null);
            }
        }
        if (this.fAdd) {
            this.fElementStack.matchElement(this.fElementQName);
        }
        this.fCurrentElement = this.fElementQName;
        String rawname = this.fElementQName.rawname;
        this.fEmptyElement = false;
        this.fAttributes.removeAllAttributes();
        this.checkDepth(rawname);
        if (!this.seekCloseOfStartTag()) {
            this.fReadingAttributes = true;
            this.fAttributeCacheUsedCount = 0;
            this.fStringBufferIndex = 0;
            this.fAddDefaultAttr = true;
            do {
                this.scanAttribute(this.fAttributes);
                if (this.fSecurityManager == null || this.fSecurityManager.isNoLimit(this.fElementAttributeLimit) || this.fAttributes.getLength() <= this.fElementAttributeLimit) continue;
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "ElementAttributeLimit", new Object[]{rawname, this.fElementAttributeLimit}, (short)2);
            } while (!this.seekCloseOfStartTag());
            this.fReadingAttributes = false;
        }
        if (this.fEmptyElement) {
            --this.fMarkupDepth;
            if (this.fMarkupDepth < this.fEntityStack[this.fEntityDepth - 1]) {
                this.reportFatalError("ElementEntityMismatch", new Object[]{this.fCurrentElement.rawname});
            }
            if (this.fDocumentHandler != null) {
                this.fDocumentHandler.emptyElement(this.fElementQName, this.fAttributes, null);
            }
            this.fElementStack.popElement();
        } else {
            if (this.dtdGrammarUtil != null) {
                this.dtdGrammarUtil.startElement(this.fElementQName, this.fAttributes);
            }
            if (this.fDocumentHandler != null) {
                this.fDocumentHandler.startElement(this.fElementQName, this.fAttributes, null);
            }
        }
        return this.fEmptyElement;
    }

    protected boolean seekCloseOfStartTag() throws IOException, XNIException {
        boolean sawSpace = this.fEntityScanner.skipSpaces();
        int c = this.fEntityScanner.peekChar();
        if (c == 62) {
            this.fEntityScanner.scanChar(null);
            return true;
        }
        if (c == 47) {
            this.fEntityScanner.scanChar(null);
            if (!this.fEntityScanner.skipChar(62, XMLScanner.NameType.ELEMENTEND)) {
                this.reportFatalError("ElementUnterminated", new Object[]{this.fElementQName.rawname});
            }
            this.fEmptyElement = true;
            return true;
        }
        if (!(this.isValidNameStartChar(c) && sawSpace || this.isValidNameStartHighSurrogate(c) && sawSpace)) {
            this.reportFatalError("ElementUnterminated", new Object[]{this.fElementQName.rawname});
        }
        return false;
    }

    public boolean hasAttributes() {
        return this.fAttributes.getLength() > 0;
    }

    public XMLAttributesIteratorImpl getAttributeIterator() {
        if (this.dtdGrammarUtil != null && this.fAddDefaultAttr) {
            this.dtdGrammarUtil.addDTDDefaultAttrs(this.fElementQName, this.fAttributes);
            this.fAddDefaultAttr = false;
        }
        return this.fAttributes;
    }

    public boolean standaloneSet() {
        return this.fStandaloneSet;
    }

    public boolean isStandAlone() {
        return this.fStandalone;
    }

    protected void scanAttribute(XMLAttributes attributes) throws IOException, XNIException {
        if (this.fNamespaces) {
            this.fEntityScanner.scanQName(this.fAttributeQName, XMLScanner.NameType.ATTRIBUTENAME);
        } else {
            String name = this.fEntityScanner.scanName(XMLScanner.NameType.ATTRIBUTENAME);
            this.fAttributeQName.setValues(null, name, name, null);
        }
        this.fEntityScanner.skipSpaces();
        if (!this.fEntityScanner.skipChar(61, XMLScanner.NameType.ATTRIBUTE)) {
            this.reportFatalError("EqRequiredInAttribute", new Object[]{this.fCurrentElement.rawname, this.fAttributeQName.rawname});
        }
        this.fEntityScanner.skipSpaces();
        int attIndex = 0;
        boolean isVC = this.fHasExternalDTD && !this.fStandalone;
        XMLString tmpStr = this.getString();
        this.scanAttributeValue(tmpStr, this.fTempString2, this.fAttributeQName.rawname, attributes, attIndex, isVC, this.fCurrentElement.rawname, false);
        int oldLen = attributes.getLength();
        attIndex = attributes.addAttribute(this.fAttributeQName, XMLSymbols.fCDATASymbol, null);
        if (oldLen == attributes.getLength()) {
            this.reportFatalError("AttributeNotUnique", new Object[]{this.fCurrentElement.rawname, this.fAttributeQName.rawname});
        }
        attributes.setValue(attIndex, null, tmpStr);
        attributes.setSpecified(attIndex, true);
    }

    protected int scanContent(XMLStringBuffer content) throws IOException, XNIException {
        this.fTempString.length = 0;
        int c = this.fEntityScanner.scanContent(this.fTempString);
        content.append(this.fTempString);
        this.fTempString.length = 0;
        if (c == 13) {
            this.fEntityScanner.scanChar(null);
            content.append((char)c);
            c = -1;
        } else if (c == 93) {
            content.append((char)this.fEntityScanner.scanChar(null));
            this.fInScanContent = true;
            if (this.fEntityScanner.skipChar(93, null)) {
                content.append(']');
                while (this.fEntityScanner.skipChar(93, null)) {
                    content.append(']');
                }
                if (this.fEntityScanner.skipChar(62, null)) {
                    this.reportFatalError("CDEndInContent", null);
                }
            }
            this.fInScanContent = false;
            c = -1;
        }
        if (this.fDocumentHandler == null || content.length > 0) {
            // empty if block
        }
        return c;
    }

    protected boolean scanCDATASection(XMLStringBuffer contentBuffer, boolean complete) throws IOException, XNIException {
        if (this.fDocumentHandler != null) {
            // empty if block
        }
        while (true) {
            if (!this.fEntityScanner.scanData("]]>", contentBuffer, this.fChunkSize)) {
                this.fInCData = false;
                this.fCDataEnd = true;
                --this.fMarkupDepth;
                break;
            }
            int c = this.fEntityScanner.peekChar();
            if (c != -1 && this.isInvalidLiteral(c)) {
                if (XMLChar.isHighSurrogate(c)) {
                    this.scanSurrogates(contentBuffer);
                } else {
                    this.reportFatalError("InvalidCharInCDSect", new Object[]{Integer.toString(c, 16)});
                    this.fEntityScanner.scanChar(null);
                }
            } else {
                this.fInCData = true;
                this.fCDataEnd = false;
                break;
            }
            if (this.fDocumentHandler == null) continue;
        }
        return true;
    }

    protected int scanEndElement() throws IOException, XNIException {
        QName endElementName = this.fElementStack.popElement();
        String rawname = endElementName.rawname;
        if (!this.fEntityScanner.skipString(endElementName.rawname)) {
            this.reportFatalError("ETagRequired", new Object[]{rawname});
        }
        this.fEntityScanner.skipSpaces();
        if (!this.fEntityScanner.skipChar(62, XMLScanner.NameType.ELEMENTEND)) {
            this.reportFatalError("ETagUnterminated", new Object[]{rawname});
        }
        --this.fMarkupDepth;
        --this.fMarkupDepth;
        if (this.fMarkupDepth < this.fEntityStack[this.fEntityDepth - 1]) {
            this.reportFatalError("ElementEntityMismatch", new Object[]{rawname});
        }
        if (this.fDocumentHandler != null) {
            this.fDocumentHandler.endElement(endElementName, null);
        }
        if (this.dtdGrammarUtil != null) {
            this.dtdGrammarUtil.endElement(endElementName);
        }
        return this.fMarkupDepth;
    }

    protected void scanCharReference() throws IOException, XNIException {
        this.fStringBuffer2.clear();
        int ch = this.scanCharReferenceValue(this.fStringBuffer2, null);
        --this.fMarkupDepth;
        if (ch != -1 && this.fDocumentHandler != null) {
            if (this.fNotifyCharRefs) {
                this.fDocumentHandler.startGeneralEntity(this.fCharRefLiteral, null, null, null);
            }
            Augmentations augs = null;
            if (this.fValidation && ch <= 32) {
                if (this.fTempAugmentations != null) {
                    this.fTempAugmentations.removeAllItems();
                } else {
                    this.fTempAugmentations = new AugmentationsImpl();
                }
                augs = this.fTempAugmentations;
                augs.putItem("CHAR_REF_PROBABLE_WS", Boolean.TRUE);
            }
            if (this.fNotifyCharRefs) {
                this.fDocumentHandler.endGeneralEntity(this.fCharRefLiteral, null);
            }
        }
    }

    protected void scanEntityReference(XMLStringBuffer content) throws IOException, XNIException {
        String name = this.fEntityScanner.scanName(XMLScanner.NameType.REFERENCE);
        if (name == null) {
            this.reportFatalError("NameRequiredInReference", null);
            return;
        }
        if (!this.fEntityScanner.skipChar(59, XMLScanner.NameType.REFERENCE)) {
            this.reportFatalError("SemicolonRequiredInReference", new Object[]{name});
        }
        if (this.fEntityStore.isUnparsedEntity(name)) {
            this.reportFatalError("ReferenceToUnparsedEntity", new Object[]{name});
        }
        --this.fMarkupDepth;
        this.fCurrentEntityName = name;
        if (name == fAmpSymbol) {
            this.handleCharacter('&', fAmpSymbol, content);
            this.fScannerState = 41;
            return;
        }
        if (name == fLtSymbol) {
            this.handleCharacter('<', fLtSymbol, content);
            this.fScannerState = 41;
            return;
        }
        if (name == fGtSymbol) {
            this.handleCharacter('>', fGtSymbol, content);
            this.fScannerState = 41;
            return;
        }
        if (name == fQuotSymbol) {
            this.handleCharacter('\"', fQuotSymbol, content);
            this.fScannerState = 41;
            return;
        }
        if (name == fAposSymbol) {
            this.handleCharacter('\'', fAposSymbol, content);
            this.fScannerState = 41;
            return;
        }
        boolean isEE = this.fEntityStore.isExternalEntity(name);
        if (isEE && !this.fSupportExternalEntities || !isEE && !this.fReplaceEntityReferences || this.foundBuiltInRefs) {
            this.fScannerState = 28;
            return;
        }
        if (!this.fEntityStore.isDeclaredEntity(name)) {
            if (!this.fSupportDTD && this.fReplaceEntityReferences) {
                this.reportFatalError("EntityNotDeclared", new Object[]{name});
                return;
            }
            if (this.fHasExternalDTD && !this.fStandalone) {
                if (this.fValidation) {
                    this.fErrorReporter.reportError(this.fEntityScanner, "http://www.w3.org/TR/1998/REC-xml-19980210", "EntityNotDeclared", new Object[]{name}, (short)1);
                }
            } else {
                this.reportFatalError("EntityNotDeclared", new Object[]{name});
            }
        }
        if (this.fCreateEntityRefNodes) {
            this.fDocumentHandler.startGeneralEntity(name, null, null, null);
        } else {
            this.fEntityManager.startEntity(true, name, false);
        }
    }

    void checkDepth(String elementName) {
        this.fLimitAnalyzer.addValue(XMLSecurityManager.Limit.MAX_ELEMENT_DEPTH_LIMIT, elementName, this.fElementStack.fDepth);
        if (this.fSecurityManager.isOverLimit(XMLSecurityManager.Limit.MAX_ELEMENT_DEPTH_LIMIT, this.fLimitAnalyzer)) {
            this.fSecurityManager.debugPrint(this.fLimitAnalyzer);
            this.reportFatalError("MaxElementDepthLimit", new Object[]{elementName, this.fLimitAnalyzer.getTotalValue(XMLSecurityManager.Limit.MAX_ELEMENT_DEPTH_LIMIT), this.fSecurityManager.getLimit(XMLSecurityManager.Limit.MAX_ELEMENT_DEPTH_LIMIT), "maxElementDepth"});
        }
    }

    private void handleCharacter(char c, String entity, XMLStringBuffer content) throws XNIException {
        this.foundBuiltInRefs = true;
        this.checkEntityLimit(false, this.fEntityScanner.fCurrentEntity.name, 1);
        content.append(c);
        if (this.fDocumentHandler != null) {
            this.fSingleChar[0] = c;
            if (this.fNotifyBuiltInRefs) {
                this.fDocumentHandler.startGeneralEntity(entity, null, null, null);
            }
            this.fTempString.setValues(this.fSingleChar, 0, 1);
            if (!this.fIsCoalesce) {
                this.fDocumentHandler.characters(this.fTempString, null);
                this.builtInRefCharacterHandled = true;
            }
            if (this.fNotifyBuiltInRefs) {
                this.fDocumentHandler.endGeneralEntity(entity, null);
            }
        }
    }

    protected final void setScannerState(int state) {
        this.fScannerState = state;
    }

    protected final void setDriver(Driver driver) {
        this.fDriver = driver;
    }

    protected String getScannerStateName(int state) {
        switch (state) {
            case 24: {
                return "SCANNER_STATE_DOCTYPE";
            }
            case 26: {
                return "SCANNER_STATE_ROOT_ELEMENT";
            }
            case 21: {
                return "SCANNER_STATE_START_OF_MARKUP";
            }
            case 27: {
                return "SCANNER_STATE_COMMENT";
            }
            case 23: {
                return "SCANNER_STATE_PI";
            }
            case 22: {
                return "SCANNER_STATE_CONTENT";
            }
            case 28: {
                return "SCANNER_STATE_REFERENCE";
            }
            case 33: {
                return "SCANNER_STATE_END_OF_INPUT";
            }
            case 34: {
                return "SCANNER_STATE_TERMINATED";
            }
            case 35: {
                return "SCANNER_STATE_CDATA";
            }
            case 36: {
                return "SCANNER_STATE_TEXT_DECL";
            }
            case 29: {
                return "SCANNER_STATE_ATTRIBUTE";
            }
            case 30: {
                return "SCANNER_STATE_ATTRIBUTE_VALUE";
            }
            case 38: {
                return "SCANNER_STATE_START_ELEMENT_TAG";
            }
            case 39: {
                return "SCANNER_STATE_END_ELEMENT_TAG";
            }
            case 37: {
                return "SCANNER_STATE_CHARACTER_DATA";
            }
        }
        return "??? (" + state + ")";
    }

    public String getEntityName() {
        return this.fCurrentEntityName;
    }

    public String getDriverName(Driver driver) {
        return "null";
    }

    String checkAccess(String systemId, String allowedProtocols) throws IOException {
        String baseSystemId = this.fEntityScanner.getBaseSystemId();
        String expandedSystemId = XMLEntityManager.expandSystemId(systemId, baseSystemId, this.fStrictURI);
        return SecuritySupport.checkAccess(expandedSystemId, allowedProtocols, EXTERNAL_ACCESS_DEFAULT);
    }

    static void pr(String str) {
        System.out.println(str);
    }

    protected XMLString getString() {
        if (this.fAttributeCacheUsedCount < this.initialCacheCount || this.fAttributeCacheUsedCount < this.attributeValueCache.size()) {
            return (XMLString)this.attributeValueCache.get(this.fAttributeCacheUsedCount++);
        }
        XMLString str = new XMLString();
        ++this.fAttributeCacheUsedCount;
        this.attributeValueCache.add(str);
        return str;
    }

    @Override
    public void refresh() {
        this.refresh(0);
    }

    @Override
    public void refresh(int refreshPosition) {
        if (this.fReadingAttributes) {
            this.fAttributes.refresh();
        }
        if (this.fScannerState == 37) {
            this.bufferContent();
        }
    }

    private void bufferContent() {
        this.fContentBuffer.append(this.fTempString);
        this.fTempString.length = 0;
        this.fUsebuffer = true;
    }

    protected class ElementStack {
        protected QName[] fElements;
        protected int[] fInt = new int[20];
        protected int fDepth;
        protected int fCount;
        protected int fPosition;
        protected int fMark;
        protected int fLastDepth;

        public ElementStack() {
            this.fElements = new QName[20];
            for (int i = 0; i < this.fElements.length; ++i) {
                this.fElements[i] = new QName();
            }
        }

        public QName pushElement(QName element) {
            if (this.fDepth == this.fElements.length) {
                QName[] array = new QName[this.fElements.length * 2];
                System.arraycopy(this.fElements, 0, array, 0, this.fDepth);
                this.fElements = array;
                for (int i = this.fDepth; i < this.fElements.length; ++i) {
                    this.fElements[i] = new QName();
                }
            }
            this.fElements[this.fDepth].setValues(element);
            return this.fElements[this.fDepth++];
        }

        public QName getNext() {
            if (this.fPosition == this.fCount) {
                this.fPosition = this.fMark;
            }
            return this.fElements[this.fPosition];
        }

        public void push() {
            this.fInt[++this.fDepth] = this.fPosition++;
        }

        public boolean matchElement(QName element) {
            boolean match = false;
            if (this.fLastDepth > this.fDepth && this.fDepth <= 3) {
                if (element.rawname == this.fElements[this.fDepth - 1].rawname) {
                    XMLDocumentFragmentScannerImpl.this.fAdd = false;
                    this.fPosition = this.fMark = this.fDepth - 1;
                    match = true;
                    --this.fCount;
                } else {
                    XMLDocumentFragmentScannerImpl.this.fAdd = true;
                }
            }
            if (match) {
                ++this.fPosition;
            } else {
                this.fInt[this.fDepth] = this.fCount - 1;
            }
            if (this.fCount == this.fElements.length) {
                XMLDocumentFragmentScannerImpl.this.fSkip = false;
                XMLDocumentFragmentScannerImpl.this.fAdd = false;
                this.reposition();
                return false;
            }
            this.fLastDepth = this.fDepth;
            return match;
        }

        public QName nextElement() {
            if (XMLDocumentFragmentScannerImpl.this.fSkip) {
                ++this.fDepth;
                return this.fElements[this.fCount++];
            }
            if (this.fDepth == this.fElements.length) {
                QName[] array = new QName[this.fElements.length * 2];
                System.arraycopy(this.fElements, 0, array, 0, this.fDepth);
                this.fElements = array;
                for (int i = this.fDepth; i < this.fElements.length; ++i) {
                    this.fElements[i] = new QName();
                }
            }
            return this.fElements[this.fDepth++];
        }

        public QName popElement() {
            if (XMLDocumentFragmentScannerImpl.this.fSkip || XMLDocumentFragmentScannerImpl.this.fAdd) {
                return this.fElements[this.fInt[this.fDepth--]];
            }
            return this.fElements[--this.fDepth];
        }

        public void reposition() {
            for (int i = 2; i <= this.fDepth; ++i) {
                this.fElements[i - 1] = this.fElements[this.fInt[i]];
            }
        }

        public void clear() {
            this.fDepth = 0;
            this.fLastDepth = 0;
            this.fCount = 0;
            this.fMark = 1;
            this.fPosition = 1;
        }

        public QName getLastPoppedElement() {
            return this.fElements[this.fDepth];
        }
    }

    protected class ElementStack2 {
        protected QName[] fQName = new QName[20];
        protected int fDepth;
        protected int fCount;
        protected int fPosition;
        protected int fMark;
        protected int fLastDepth;

        public ElementStack2() {
            for (int i = 0; i < this.fQName.length; ++i) {
                this.fQName[i] = new QName();
            }
            this.fPosition = 1;
            this.fMark = 1;
        }

        public void resize() {
            int oldLength = this.fQName.length;
            QName[] tmp = new QName[oldLength * 2];
            System.arraycopy(this.fQName, 0, tmp, 0, oldLength);
            this.fQName = tmp;
            for (int i = oldLength; i < this.fQName.length; ++i) {
                this.fQName[i] = new QName();
            }
        }

        public boolean matchElement(QName element) {
            boolean match = false;
            if (this.fLastDepth > this.fDepth && this.fDepth <= 2) {
                if (element.rawname == this.fQName[this.fDepth].rawname) {
                    XMLDocumentFragmentScannerImpl.this.fAdd = false;
                    this.fMark = this.fDepth - 1;
                    this.fPosition = this.fMark + 1;
                    match = true;
                    --this.fCount;
                } else {
                    XMLDocumentFragmentScannerImpl.this.fAdd = true;
                }
            }
            this.fLastDepth = this.fDepth++;
            return match;
        }

        public QName nextElement() {
            if (this.fCount == this.fQName.length) {
                XMLDocumentFragmentScannerImpl.this.fShouldSkip = false;
                XMLDocumentFragmentScannerImpl.this.fAdd = false;
                return this.fQName[--this.fCount];
            }
            return this.fQName[this.fCount++];
        }

        public QName getNext() {
            if (this.fPosition == this.fCount) {
                this.fPosition = this.fMark;
            }
            return this.fQName[this.fPosition++];
        }

        public int popElement() {
            return this.fDepth--;
        }

        public void clear() {
            this.fLastDepth = 0;
            this.fDepth = 0;
            this.fCount = 0;
            this.fMark = 1;
            this.fPosition = 1;
        }
    }

    protected static interface Driver {
        public int next() throws IOException, XNIException;
    }

    protected class FragmentContentDriver
    implements Driver {
        protected FragmentContentDriver() {
        }

        private void startOfMarkup() throws IOException {
            ++XMLDocumentFragmentScannerImpl.this.fMarkupDepth;
            int ch = XMLDocumentFragmentScannerImpl.this.fEntityScanner.peekChar();
            if (XMLDocumentFragmentScannerImpl.this.isValidNameStartChar(ch) || XMLDocumentFragmentScannerImpl.this.isValidNameStartHighSurrogate(ch)) {
                XMLDocumentFragmentScannerImpl.this.setScannerState(38);
            } else {
                switch (ch) {
                    case 63: {
                        XMLDocumentFragmentScannerImpl.this.setScannerState(23);
                        XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(ch, null);
                        break;
                    }
                    case 33: {
                        XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(ch, null);
                        if (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(45, null)) {
                            if (!XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(45, XMLScanner.NameType.COMMENT)) {
                                XMLDocumentFragmentScannerImpl.this.reportFatalError("InvalidCommentStart", null);
                            }
                            XMLDocumentFragmentScannerImpl.this.setScannerState(27);
                            break;
                        }
                        if (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipString(CDATA)) {
                            XMLDocumentFragmentScannerImpl.this.fCDataStart = true;
                            XMLDocumentFragmentScannerImpl.this.setScannerState(35);
                            break;
                        }
                        if (this.scanForDoctypeHook()) break;
                        XMLDocumentFragmentScannerImpl.this.reportFatalError("MarkupNotRecognizedInContent", null);
                        break;
                    }
                    case 47: {
                        XMLDocumentFragmentScannerImpl.this.setScannerState(39);
                        XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(ch, XMLScanner.NameType.ELEMENTEND);
                        break;
                    }
                    default: {
                        XMLDocumentFragmentScannerImpl.this.reportFatalError("MarkupNotRecognizedInContent", null);
                    }
                }
            }
        }

        private void startOfContent() throws IOException {
            if (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(60, null)) {
                XMLDocumentFragmentScannerImpl.this.setScannerState(21);
            } else if (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(38, XMLScanner.NameType.REFERENCE)) {
                XMLDocumentFragmentScannerImpl.this.setScannerState(28);
            } else {
                XMLDocumentFragmentScannerImpl.this.setScannerState(37);
            }
        }

        public void decideSubState() throws IOException {
            while (XMLDocumentFragmentScannerImpl.this.fScannerState == 22 || XMLDocumentFragmentScannerImpl.this.fScannerState == 21) {
                switch (XMLDocumentFragmentScannerImpl.this.fScannerState) {
                    case 22: {
                        this.startOfContent();
                        break;
                    }
                    case 21: {
                        this.startOfMarkup();
                    }
                }
            }
        }

        /*
         * Unable to fully structure code
         */
        @Override
        public int next() throws IOException, XNIException {
            try {
                block17: while (true) {
                    if (XMLDocumentFragmentScannerImpl.this.fScannerState == 22) {
                        ch = XMLDocumentFragmentScannerImpl.this.fEntityScanner.peekChar();
                        if (ch == 60) {
                            XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanChar(null);
                            XMLDocumentFragmentScannerImpl.this.setScannerState(21);
                        } else if (ch == 38) {
                            XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanChar(XMLScanner.NameType.REFERENCE);
                            XMLDocumentFragmentScannerImpl.this.setScannerState(28);
                        } else {
                            XMLDocumentFragmentScannerImpl.this.setScannerState(37);
                        }
                    }
                    if (XMLDocumentFragmentScannerImpl.this.fScannerState == 21) {
                        this.startOfMarkup();
                    }
                    if (XMLDocumentFragmentScannerImpl.this.fIsCoalesce) {
                        XMLDocumentFragmentScannerImpl.this.fUsebuffer = true;
                        if (XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData) {
                            if (XMLDocumentFragmentScannerImpl.this.fScannerState != 35 && XMLDocumentFragmentScannerImpl.this.fScannerState != 28 && XMLDocumentFragmentScannerImpl.this.fScannerState != 37) {
                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData = false;
                                return 4;
                            }
                        } else if ((XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData || XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference) && XMLDocumentFragmentScannerImpl.this.fScannerState != 35 && XMLDocumentFragmentScannerImpl.this.fScannerState != 28 && XMLDocumentFragmentScannerImpl.this.fScannerState != 37) {
                            XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData = false;
                            XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference = false;
                            return 4;
                        }
                    }
                    switch (XMLDocumentFragmentScannerImpl.this.fScannerState) {
                        case 7: {
                            return 7;
                        }
                        case 38: {
                            XMLDocumentFragmentScannerImpl.this.fEmptyElement = XMLDocumentFragmentScannerImpl.this.scanStartElement();
                            if (XMLDocumentFragmentScannerImpl.this.fEmptyElement) {
                                XMLDocumentFragmentScannerImpl.this.setScannerState(39);
                            } else {
                                XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                            }
                            return 1;
                        }
                        case 37: {
                            v0 = XMLDocumentFragmentScannerImpl.this.fUsebuffer = XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference != false || XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData != false || XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData != false;
                            if (XMLDocumentFragmentScannerImpl.this.fIsCoalesce && (XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference || XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData || XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData)) {
                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference = false;
                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData = false;
                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData = true;
                                XMLDocumentFragmentScannerImpl.this.fUsebuffer = true;
                            } else {
                                XMLDocumentFragmentScannerImpl.this.fContentBuffer.clear();
                            }
                            XMLDocumentFragmentScannerImpl.this.fTempString.length = 0;
                            c = XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanContent(XMLDocumentFragmentScannerImpl.this.fTempString);
                            if (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(60, null)) {
                                if (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(47, XMLScanner.NameType.ELEMENTEND)) {
                                    ++XMLDocumentFragmentScannerImpl.this.fMarkupDepth;
                                    XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData = false;
                                    XMLDocumentFragmentScannerImpl.this.setScannerState(39);
                                } else if (XMLChar.isNameStart(XMLDocumentFragmentScannerImpl.this.fEntityScanner.peekChar())) {
                                    ++XMLDocumentFragmentScannerImpl.this.fMarkupDepth;
                                    XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData = false;
                                    XMLDocumentFragmentScannerImpl.this.setScannerState(38);
                                } else {
                                    XMLDocumentFragmentScannerImpl.this.setScannerState(21);
                                    if (XMLDocumentFragmentScannerImpl.this.fIsCoalesce) {
                                        XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData = true;
                                        XMLDocumentFragmentScannerImpl.this.bufferContent();
                                        continue block17;
                                    }
                                }
                                if (XMLDocumentFragmentScannerImpl.this.fUsebuffer) {
                                    XMLDocumentFragmentScannerImpl.this.bufferContent();
                                }
                                if (XMLDocumentFragmentScannerImpl.this.dtdGrammarUtil != null && XMLDocumentFragmentScannerImpl.this.dtdGrammarUtil.isIgnorableWhiteSpace(XMLDocumentFragmentScannerImpl.this.fContentBuffer)) {
                                    return 6;
                                }
                                return 4;
                            }
                            XMLDocumentFragmentScannerImpl.this.bufferContent();
                            if (c == 13) {
                                XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanChar(null);
                                XMLDocumentFragmentScannerImpl.this.fUsebuffer = true;
                                XMLDocumentFragmentScannerImpl.this.fContentBuffer.append((char)c);
                                c = -1;
                            } else if (c == 93) {
                                XMLDocumentFragmentScannerImpl.this.fUsebuffer = true;
                                XMLDocumentFragmentScannerImpl.this.fContentBuffer.append((char)XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanChar(null));
                                XMLDocumentFragmentScannerImpl.this.fInScanContent = true;
                                if (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(93, null)) {
                                    XMLDocumentFragmentScannerImpl.this.fContentBuffer.append(']');
                                    while (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(93, null)) {
                                        XMLDocumentFragmentScannerImpl.this.fContentBuffer.append(']');
                                    }
                                    if (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(62, null)) {
                                        XMLDocumentFragmentScannerImpl.this.reportFatalError("CDEndInContent", null);
                                    }
                                }
                                c = -1;
                                XMLDocumentFragmentScannerImpl.this.fInScanContent = false;
                            }
                            do {
                                if (c != 60) ** GOTO lbl98
                                XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanChar(null);
                                XMLDocumentFragmentScannerImpl.this.setScannerState(21);
                                ** GOTO lbl116
lbl98:
                                // 1 sources

                                if (c != 38) ** GOTO lbl103
                                XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanChar(XMLScanner.NameType.REFERENCE);
                                XMLDocumentFragmentScannerImpl.this.setScannerState(28);
                                ** GOTO lbl116
lbl103:
                                // 1 sources

                                if (c == -1 || !XMLDocumentFragmentScannerImpl.this.isInvalidLiteral(c)) ** GOTO lbl113
                                if (XMLChar.isHighSurrogate(c)) {
                                    XMLDocumentFragmentScannerImpl.this.scanSurrogates(XMLDocumentFragmentScannerImpl.this.fContentBuffer);
                                    XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                                } else {
                                    XMLDocumentFragmentScannerImpl.this.reportFatalError("InvalidCharInContent", new Object[]{Integer.toString(c, 16)});
                                    XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanChar(null);
                                }
                                ** GOTO lbl116
lbl113:
                                // 1 sources

                                c = XMLDocumentFragmentScannerImpl.this.scanContent(XMLDocumentFragmentScannerImpl.this.fContentBuffer);
                            } while (XMLDocumentFragmentScannerImpl.this.fIsCoalesce);
                            XMLDocumentFragmentScannerImpl.this.setScannerState(22);
lbl116:
                            // 5 sources

                            if (XMLDocumentFragmentScannerImpl.this.fIsCoalesce) {
                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData = true;
                                continue block17;
                            }
                            if (XMLDocumentFragmentScannerImpl.this.dtdGrammarUtil != null && XMLDocumentFragmentScannerImpl.this.dtdGrammarUtil.isIgnorableWhiteSpace(XMLDocumentFragmentScannerImpl.this.fContentBuffer)) {
                                return 6;
                            }
                            return 4;
                        }
                        case 39: {
                            if (XMLDocumentFragmentScannerImpl.this.fEmptyElement) {
                                XMLDocumentFragmentScannerImpl.this.fEmptyElement = false;
                                XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                                return XMLDocumentFragmentScannerImpl.this.fMarkupDepth == 0 && this.elementDepthIsZeroHook() != false ? 2 : 2;
                            }
                            if (XMLDocumentFragmentScannerImpl.this.scanEndElement() == 0 && this.elementDepthIsZeroHook()) {
                                return 2;
                            }
                            XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                            return 2;
                        }
                        case 27: {
                            XMLDocumentFragmentScannerImpl.this.scanComment();
                            XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                            return 5;
                        }
                        case 23: {
                            XMLDocumentFragmentScannerImpl.this.fContentBuffer.clear();
                            XMLDocumentFragmentScannerImpl.this.scanPI(XMLDocumentFragmentScannerImpl.this.fContentBuffer);
                            XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                            return 3;
                        }
                        case 35: {
                            if (XMLDocumentFragmentScannerImpl.this.fIsCoalesce && (XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference || XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData || XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData)) {
                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData = true;
                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference = false;
                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData = false;
                            } else {
                                XMLDocumentFragmentScannerImpl.this.fContentBuffer.clear();
                            }
                            XMLDocumentFragmentScannerImpl.this.fUsebuffer = true;
                            XMLDocumentFragmentScannerImpl.this.scanCDATASection(XMLDocumentFragmentScannerImpl.this.fContentBuffer, true);
                            if (!XMLDocumentFragmentScannerImpl.this.fCDataEnd) {
                                XMLDocumentFragmentScannerImpl.this.setScannerState(35);
                            } else {
                                XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                            }
                            if (XMLDocumentFragmentScannerImpl.this.fIsCoalesce) {
                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData = true;
                                continue block17;
                            }
                            if (XMLDocumentFragmentScannerImpl.this.fReportCdataEvent) {
                                return 12;
                            }
                            return 4;
                        }
                        case 28: {
                            ++XMLDocumentFragmentScannerImpl.this.fMarkupDepth;
                            XMLDocumentFragmentScannerImpl.this.foundBuiltInRefs = false;
                            if (XMLDocumentFragmentScannerImpl.this.fIsCoalesce && (XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference || XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData || XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData)) {
                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference = true;
                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasCData = false;
                                XMLDocumentFragmentScannerImpl.this.fLastSectionWasCharacterData = false;
                            } else {
                                XMLDocumentFragmentScannerImpl.this.fContentBuffer.clear();
                            }
                            XMLDocumentFragmentScannerImpl.this.fUsebuffer = true;
                            if (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipChar(35, XMLScanner.NameType.REFERENCE)) {
                                XMLDocumentFragmentScannerImpl.this.scanCharReferenceValue(XMLDocumentFragmentScannerImpl.this.fContentBuffer, null);
                                --XMLDocumentFragmentScannerImpl.this.fMarkupDepth;
                                if (!XMLDocumentFragmentScannerImpl.this.fIsCoalesce) {
                                    XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                                    return 4;
                                }
                            } else {
                                XMLDocumentFragmentScannerImpl.this.scanEntityReference(XMLDocumentFragmentScannerImpl.this.fContentBuffer);
                                if (XMLDocumentFragmentScannerImpl.this.fScannerState == 41 && !XMLDocumentFragmentScannerImpl.this.fIsCoalesce) {
                                    XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                                    if (XMLDocumentFragmentScannerImpl.this.builtInRefCharacterHandled) {
                                        XMLDocumentFragmentScannerImpl.this.builtInRefCharacterHandled = false;
                                        return 9;
                                    }
                                    return 4;
                                }
                                if (XMLDocumentFragmentScannerImpl.this.fScannerState == 36) {
                                    XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference = true;
                                    continue block17;
                                }
                                if (XMLDocumentFragmentScannerImpl.this.fScannerState == 28) {
                                    XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                                    if (XMLDocumentFragmentScannerImpl.this.fReplaceEntityReferences && XMLDocumentFragmentScannerImpl.this.fEntityStore.isDeclaredEntity(XMLDocumentFragmentScannerImpl.this.fCurrentEntityName)) continue block17;
                                    return 9;
                                }
                            }
                            XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                            XMLDocumentFragmentScannerImpl.this.fLastSectionWasEntityReference = true;
                            continue block17;
                        }
                        case 36: {
                            if (XMLDocumentFragmentScannerImpl.this.fEntityScanner.skipString("<?xml")) {
                                ++XMLDocumentFragmentScannerImpl.this.fMarkupDepth;
                                if (XMLDocumentFragmentScannerImpl.this.isValidNameChar(XMLDocumentFragmentScannerImpl.this.fEntityScanner.peekChar())) {
                                    XMLDocumentFragmentScannerImpl.this.fStringBuffer.clear();
                                    XMLDocumentFragmentScannerImpl.this.fStringBuffer.append("xml");
                                    if (XMLDocumentFragmentScannerImpl.this.fNamespaces) {
                                        while (XMLDocumentFragmentScannerImpl.this.isValidNCName(XMLDocumentFragmentScannerImpl.this.fEntityScanner.peekChar())) {
                                            XMLDocumentFragmentScannerImpl.this.fStringBuffer.append((char)XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanChar(null));
                                        }
                                    } else {
                                        while (XMLDocumentFragmentScannerImpl.this.isValidNameChar(XMLDocumentFragmentScannerImpl.this.fEntityScanner.peekChar())) {
                                            XMLDocumentFragmentScannerImpl.this.fStringBuffer.append((char)XMLDocumentFragmentScannerImpl.this.fEntityScanner.scanChar(null));
                                        }
                                    }
                                    target = XMLDocumentFragmentScannerImpl.this.fSymbolTable.addSymbol(XMLDocumentFragmentScannerImpl.this.fStringBuffer.ch, XMLDocumentFragmentScannerImpl.this.fStringBuffer.offset, XMLDocumentFragmentScannerImpl.this.fStringBuffer.length);
                                    XMLDocumentFragmentScannerImpl.this.fContentBuffer.clear();
                                    XMLDocumentFragmentScannerImpl.this.scanPIData(target, XMLDocumentFragmentScannerImpl.this.fContentBuffer);
                                } else {
                                    XMLDocumentFragmentScannerImpl.this.scanXMLDeclOrTextDecl(true);
                                }
                            }
                            XMLDocumentFragmentScannerImpl.this.fEntityManager.fCurrentEntity.mayReadChunks = true;
                            XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                            continue block17;
                        }
                        case 26: {
                            if (this.scanRootElementHook()) {
                                XMLDocumentFragmentScannerImpl.this.fEmptyElement = true;
                                return 1;
                            }
                            XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                            return 1;
                        }
                        case 40: {
                            XMLDocumentFragmentScannerImpl.this.fContentBuffer.clear();
                            XMLDocumentFragmentScannerImpl.this.scanCharReferenceValue(XMLDocumentFragmentScannerImpl.this.fContentBuffer, null);
                            --XMLDocumentFragmentScannerImpl.this.fMarkupDepth;
                            XMLDocumentFragmentScannerImpl.this.setScannerState(22);
                            return 4;
                        }
                    }
                    break;
                }
                throw new XNIException("Scanner State " + XMLDocumentFragmentScannerImpl.this.fScannerState + " not Recognized ");
            }
            catch (MalformedByteSequenceException e) {
                XMLDocumentFragmentScannerImpl.this.fErrorReporter.reportError(e.getDomain(), e.getKey(), e.getArguments(), (short)2, e);
                return -1;
            }
            catch (CharConversionException e) {
                XMLDocumentFragmentScannerImpl.this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "CharConversionFailure", null, (short)2, e);
                return -1;
            }
            catch (EOFException e) {
                this.endOfFileHook(e);
                return -1;
            }
        }

        protected boolean scanForDoctypeHook() throws IOException, XNIException {
            return false;
        }

        protected boolean elementDepthIsZeroHook() throws IOException, XNIException {
            return false;
        }

        protected boolean scanRootElementHook() throws IOException, XNIException {
            return false;
        }

        protected void endOfFileHook(EOFException e) throws IOException, XNIException {
            if (XMLDocumentFragmentScannerImpl.this.fMarkupDepth != 0) {
                XMLDocumentFragmentScannerImpl.this.reportFatalError("PrematureEOF", null);
            }
        }
    }

    protected static final class Element {
        public QName qname;
        public char[] fRawname;
        public Element next;

        public Element(QName qname, Element next) {
            this.qname.setValues(qname);
            this.fRawname = qname.rawname.toCharArray();
            this.next = next;
        }
    }
}

