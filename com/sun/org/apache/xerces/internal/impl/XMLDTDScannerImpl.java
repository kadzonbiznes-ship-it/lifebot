/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.impl;

import com.sun.org.apache.xerces.internal.impl.PropertyManager;
import com.sun.org.apache.xerces.internal.impl.XMLEntityHandler;
import com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import com.sun.org.apache.xerces.internal.impl.XMLScanner;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.util.XMLAttributesImpl;
import com.sun.org.apache.xerces.internal.util.XMLChar;
import com.sun.org.apache.xerces.internal.util.XMLStringBuffer;
import com.sun.org.apache.xerces.internal.utils.XMLLimitAnalyzer;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.xni.Augmentations;
import com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler;
import com.sun.org.apache.xerces.internal.xni.XMLDTDHandler;
import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XMLString;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDTDScanner;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import com.sun.xml.internal.stream.dtd.nonvalidating.DTDGrammar;
import java.io.EOFException;
import java.io.IOException;

public class XMLDTDScannerImpl
extends XMLScanner
implements XMLDTDScanner,
XMLComponent,
XMLEntityHandler {
    protected static final int SCANNER_STATE_END_OF_INPUT = 0;
    protected static final int SCANNER_STATE_TEXT_DECL = 1;
    protected static final int SCANNER_STATE_MARKUP_DECL = 2;
    private static final String[] RECOGNIZED_FEATURES = new String[]{"http://xml.org/sax/features/validation", "http://apache.org/xml/features/scanner/notify-char-refs"};
    private static final Boolean[] FEATURE_DEFAULTS = new Boolean[]{null, Boolean.FALSE};
    private static final String[] RECOGNIZED_PROPERTIES = new String[]{"http://apache.org/xml/properties/internal/symbol-table", "http://apache.org/xml/properties/internal/error-reporter", "http://apache.org/xml/properties/internal/entity-manager"};
    private static final Object[] PROPERTY_DEFAULTS = new Object[]{null, null, null};
    private static final boolean DEBUG_SCANNER_STATE = false;
    public XMLDTDHandler fDTDHandler = null;
    protected XMLDTDContentModelHandler fDTDContentModelHandler;
    protected int fScannerState;
    protected boolean fStandalone;
    protected boolean fSeenExternalDTD;
    protected boolean fSeenExternalPE;
    private boolean fStartDTDCalled;
    private XMLAttributesImpl fAttributes = new XMLAttributesImpl();
    private int[] fContentStack = new int[5];
    private int fContentDepth;
    private int[] fPEStack = new int[5];
    private boolean[] fPEReport = new boolean[5];
    private int fPEDepth;
    private int fMarkUpDepth;
    private int fExtEntityDepth;
    private int fIncludeSectDepth;
    private String[] fStrings = new String[3];
    private XMLString fString = new XMLString();
    private XMLStringBuffer fStringBuffer = new XMLStringBuffer();
    private XMLStringBuffer fStringBuffer2 = new XMLStringBuffer();
    private XMLString fLiteral = new XMLString();
    private XMLString fLiteral2 = new XMLString();
    private String[] fEnumeration = new String[5];
    private int fEnumerationCount;
    private XMLStringBuffer fIgnoreConditionalBuffer = new XMLStringBuffer(128);
    DTDGrammar nvGrammarInfo = null;
    boolean nonValidatingMode = false;

    public XMLDTDScannerImpl() {
    }

    public XMLDTDScannerImpl(SymbolTable symbolTable, XMLErrorReporter errorReporter, XMLEntityManager entityManager) {
        this.fSymbolTable = symbolTable;
        this.fErrorReporter = errorReporter;
        this.fEntityManager = entityManager;
        entityManager.setProperty("http://apache.org/xml/properties/internal/symbol-table", this.fSymbolTable);
    }

    @Override
    public void setInputSource(XMLInputSource inputSource) throws IOException {
        if (inputSource == null) {
            if (this.fDTDHandler != null) {
                this.fDTDHandler.startDTD(null, null);
                this.fDTDHandler.endDTD(null);
            }
            if (this.nonValidatingMode) {
                this.nvGrammarInfo.startDTD(null, null);
                this.nvGrammarInfo.endDTD(null);
            }
            return;
        }
        this.fEntityManager.setEntityHandler(this);
        this.fEntityManager.startDTDEntity(inputSource);
    }

    @Override
    public void setLimitAnalyzer(XMLLimitAnalyzer limitAnalyzer) {
        this.fLimitAnalyzer = limitAnalyzer;
    }

    @Override
    public boolean scanDTDExternalSubset(boolean complete) throws IOException, XNIException {
        this.fEntityManager.setEntityHandler(this);
        if (this.fScannerState == 1) {
            this.fSeenExternalDTD = true;
            boolean textDecl = this.scanTextDecl();
            if (this.fScannerState == 0) {
                return false;
            }
            this.setScannerState(2);
            if (textDecl && !complete) {
                return true;
            }
        }
        do {
            if (this.scanDecls(complete)) continue;
            return false;
        } while (complete);
        return true;
    }

    @Override
    public boolean scanDTDInternalSubset(boolean complete, boolean standalone, boolean hasExternalSubset) throws IOException, XNIException {
        this.fEntityScanner = this.fEntityManager.getEntityScanner();
        this.fEntityManager.setEntityHandler(this);
        this.fStandalone = standalone;
        if (this.fScannerState == 1) {
            if (this.fDTDHandler != null) {
                this.fDTDHandler.startDTD(this.fEntityScanner, null);
                this.fStartDTDCalled = true;
            }
            if (this.nonValidatingMode) {
                this.fStartDTDCalled = true;
                this.nvGrammarInfo.startDTD(this.fEntityScanner, null);
            }
            this.setScannerState(2);
        }
        do {
            if (this.scanDecls(complete)) continue;
            if (this.fDTDHandler != null && !hasExternalSubset) {
                this.fDTDHandler.endDTD(null);
            }
            if (this.nonValidatingMode && !hasExternalSubset) {
                this.nvGrammarInfo.endDTD(null);
            }
            this.setScannerState(1);
            this.fLimitAnalyzer.reset(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT);
            this.fLimitAnalyzer.reset(XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT);
            return false;
        } while (complete);
        return true;
    }

    @Override
    public boolean skipDTD(boolean supportDTD) throws IOException {
        if (supportDTD) {
            return false;
        }
        this.fStringBuffer.clear();
        while (this.fEntityScanner.scanData("]", this.fStringBuffer, 0)) {
            int c = this.fEntityScanner.peekChar();
            if (c == -1) continue;
            if (XMLChar.isHighSurrogate(c)) {
                this.scanSurrogates(this.fStringBuffer);
            }
            if (!this.isInvalidLiteral(c)) continue;
            this.reportFatalError("InvalidCharInDTD", new Object[]{Integer.toHexString(c)});
            this.fEntityScanner.scanChar(null);
        }
        --this.fEntityScanner.fCurrentEntity.position;
        return true;
    }

    @Override
    public void reset(XMLComponentManager componentManager) throws XMLConfigurationException {
        super.reset(componentManager);
        this.init();
    }

    @Override
    public void reset() {
        super.reset();
        this.init();
    }

    @Override
    public void reset(PropertyManager props) {
        this.setPropertyManager(props);
        super.reset(props);
        this.init();
        this.nonValidatingMode = true;
        this.nvGrammarInfo = new DTDGrammar(this.fSymbolTable);
    }

    @Override
    public String[] getRecognizedFeatures() {
        return (String[])RECOGNIZED_FEATURES.clone();
    }

    @Override
    public String[] getRecognizedProperties() {
        return (String[])RECOGNIZED_PROPERTIES.clone();
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
    public void setDTDHandler(XMLDTDHandler dtdHandler) {
        this.fDTDHandler = dtdHandler;
    }

    @Override
    public XMLDTDHandler getDTDHandler() {
        return this.fDTDHandler;
    }

    @Override
    public void setDTDContentModelHandler(XMLDTDContentModelHandler dtdContentModelHandler) {
        this.fDTDContentModelHandler = dtdContentModelHandler;
    }

    @Override
    public XMLDTDContentModelHandler getDTDContentModelHandler() {
        return this.fDTDContentModelHandler;
    }

    @Override
    public void startEntity(String name, XMLResourceIdentifier identifier, String encoding, Augmentations augs) throws XNIException {
        super.startEntity(name, identifier, encoding, augs);
        boolean dtdEntity = name.equals("[dtd]");
        if (dtdEntity) {
            if (this.fDTDHandler != null && !this.fStartDTDCalled) {
                this.fDTDHandler.startDTD(this.fEntityScanner, null);
            }
            if (this.fDTDHandler != null) {
                this.fDTDHandler.startExternalSubset(identifier, null);
            }
            this.fEntityManager.startExternalSubset();
            this.fEntityStore.startExternalSubset();
            ++this.fExtEntityDepth;
        } else if (name.charAt(0) == '%') {
            this.pushPEStack(this.fMarkUpDepth, this.fReportEntity);
            if (this.fEntityScanner.isExternal()) {
                ++this.fExtEntityDepth;
            }
        }
        if (this.fDTDHandler != null && !dtdEntity && this.fReportEntity) {
            this.fDTDHandler.startParameterEntity(name, identifier, encoding, null);
        }
    }

    @Override
    public void endEntity(String name, Augmentations augs) throws XNIException, IOException {
        super.endEntity(name, augs);
        if (this.fScannerState == 0) {
            return;
        }
        boolean dtdEntity = name.equals("[dtd]");
        boolean reportEntity = this.fReportEntity;
        if (name.startsWith("%")) {
            reportEntity = this.peekReportEntity();
            int startMarkUpDepth = this.popPEStack();
            if (startMarkUpDepth == 0 && startMarkUpDepth < this.fMarkUpDepth) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "ILL_FORMED_PARAMETER_ENTITY_WHEN_USED_IN_DECL", new Object[]{this.fEntityManager.fCurrentEntity.name}, (short)2);
            }
            if (startMarkUpDepth != this.fMarkUpDepth) {
                reportEntity = false;
                if (this.fValidation) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "ImproperDeclarationNesting", new Object[]{name}, (short)1);
                }
            }
            if (this.fEntityScanner.isExternal()) {
                --this.fExtEntityDepth;
            }
            if (this.fDTDHandler != null && reportEntity) {
                this.fDTDHandler.endParameterEntity(name, null);
            }
        }
        if (dtdEntity) {
            if (this.fIncludeSectDepth != 0) {
                this.reportFatalError("IncludeSectUnterminated", null);
            }
            this.fScannerState = 0;
            this.fEntityManager.endExternalSubset();
            this.fEntityStore.endExternalSubset();
            if (this.fDTDHandler != null) {
                this.fDTDHandler.endExternalSubset(null);
                this.fDTDHandler.endDTD(null);
            }
            --this.fExtEntityDepth;
        }
        if (augs != null && Boolean.TRUE.equals(augs.getItem("LAST_ENTITY")) && (this.fMarkUpDepth != 0 || this.fExtEntityDepth != 0 || this.fIncludeSectDepth != 0)) {
            throw new EOFException();
        }
    }

    protected final void setScannerState(int state) {
        this.fScannerState = state;
    }

    private static String getScannerStateName(int state) {
        return "??? (" + state + ")";
    }

    protected final boolean scanningInternalSubset() {
        return this.fExtEntityDepth == 0;
    }

    protected void startPE(String name, boolean literal) throws IOException, XNIException {
        int depth = this.fPEDepth;
        String pName = "%" + name;
        if (this.fValidation && !this.fEntityStore.isDeclaredEntity(pName)) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "EntityNotDeclared", new Object[]{name}, (short)1);
        }
        this.fEntityManager.startEntity(false, this.fSymbolTable.addSymbol(pName), literal);
        if (depth != this.fPEDepth && this.fEntityScanner.isExternal()) {
            this.scanTextDecl();
        }
    }

    protected final boolean scanTextDecl() throws IOException, XNIException {
        boolean textDecl = false;
        if (this.fEntityScanner.skipString("<?xml")) {
            ++this.fMarkUpDepth;
            if (this.isValidNameChar(this.fEntityScanner.peekChar())) {
                this.fStringBuffer.clear();
                this.fStringBuffer.append("xml");
                while (this.isValidNameChar(this.fEntityScanner.peekChar())) {
                    this.fStringBuffer.append((char)this.fEntityScanner.scanChar(null));
                }
                String target = this.fSymbolTable.addSymbol(this.fStringBuffer.ch, this.fStringBuffer.offset, this.fStringBuffer.length);
                this.scanPIData(target, this.fString);
            } else {
                String version = null;
                String encoding = null;
                this.scanXMLDeclOrTextDecl(true, this.fStrings);
                textDecl = true;
                --this.fMarkUpDepth;
                version = this.fStrings[0];
                encoding = this.fStrings[1];
                this.fEntityScanner.setEncoding(encoding);
                if (this.fDTDHandler != null) {
                    this.fDTDHandler.textDecl(version, encoding, null);
                }
            }
        }
        this.fEntityManager.fCurrentEntity.mayReadChunks = true;
        return textDecl;
    }

    protected final void scanPIData(String target, XMLString data) throws IOException, XNIException {
        --this.fMarkUpDepth;
        if (this.fDTDHandler != null) {
            this.fDTDHandler.processingInstruction(target, data, null);
        }
    }

    protected final void scanComment() throws IOException, XNIException {
        this.fReportEntity = false;
        this.scanComment(this.fStringBuffer);
        --this.fMarkUpDepth;
        if (this.fDTDHandler != null) {
            this.fDTDHandler.comment(this.fStringBuffer, null);
        }
        this.fReportEntity = true;
    }

    protected final void scanElementDecl() throws IOException, XNIException {
        String name;
        this.fReportEntity = false;
        if (!this.skipSeparator(true, !this.scanningInternalSubset())) {
            this.reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ELEMENT_TYPE_IN_ELEMENTDECL", null);
        }
        if ((name = this.fEntityScanner.scanName(XMLScanner.NameType.ELEMENTSTART)) == null) {
            this.reportFatalError("MSG_ELEMENT_TYPE_REQUIRED_IN_ELEMENTDECL", null);
        }
        if (!this.skipSeparator(true, !this.scanningInternalSubset())) {
            this.reportFatalError("MSG_SPACE_REQUIRED_BEFORE_CONTENTSPEC_IN_ELEMENTDECL", new Object[]{name});
        }
        if (this.fDTDContentModelHandler != null) {
            this.fDTDContentModelHandler.startContentModel(name, null);
        }
        String contentModel = null;
        this.fReportEntity = true;
        if (this.fEntityScanner.skipString("EMPTY")) {
            contentModel = "EMPTY";
            if (this.fDTDContentModelHandler != null) {
                this.fDTDContentModelHandler.empty(null);
            }
        } else if (this.fEntityScanner.skipString("ANY")) {
            contentModel = "ANY";
            if (this.fDTDContentModelHandler != null) {
                this.fDTDContentModelHandler.any(null);
            }
        } else {
            if (!this.fEntityScanner.skipChar(40, null)) {
                this.reportFatalError("MSG_OPEN_PAREN_OR_ELEMENT_TYPE_REQUIRED_IN_CHILDREN", new Object[]{name});
            }
            if (this.fDTDContentModelHandler != null) {
                this.fDTDContentModelHandler.startGroup(null);
            }
            this.fStringBuffer.clear();
            this.fStringBuffer.append('(');
            ++this.fMarkUpDepth;
            this.skipSeparator(false, !this.scanningInternalSubset());
            if (this.fEntityScanner.skipString("#PCDATA")) {
                this.scanMixed(name);
            } else {
                this.scanChildren(name);
            }
            contentModel = this.fStringBuffer.toString();
        }
        if (this.fDTDContentModelHandler != null) {
            this.fDTDContentModelHandler.endContentModel(null);
        }
        this.fReportEntity = false;
        this.skipSeparator(false, !this.scanningInternalSubset());
        if (!this.fEntityScanner.skipChar(62, null)) {
            this.reportFatalError("ElementDeclUnterminated", new Object[]{name});
        }
        this.fReportEntity = true;
        --this.fMarkUpDepth;
        if (this.fDTDHandler != null) {
            this.fDTDHandler.elementDecl(name, contentModel, null);
        }
        if (this.nonValidatingMode) {
            this.nvGrammarInfo.elementDecl(name, contentModel, null);
        }
    }

    private final void scanMixed(String elName) throws IOException, XNIException {
        String childName = null;
        this.fStringBuffer.append("#PCDATA");
        if (this.fDTDContentModelHandler != null) {
            this.fDTDContentModelHandler.pcdata(null);
        }
        this.skipSeparator(false, !this.scanningInternalSubset());
        while (this.fEntityScanner.skipChar(124, null)) {
            this.fStringBuffer.append('|');
            if (this.fDTDContentModelHandler != null) {
                this.fDTDContentModelHandler.separator((short)0, null);
            }
            this.skipSeparator(false, !this.scanningInternalSubset());
            childName = this.fEntityScanner.scanName(XMLScanner.NameType.ENTITY);
            if (childName == null) {
                this.reportFatalError("MSG_ELEMENT_TYPE_REQUIRED_IN_MIXED_CONTENT", new Object[]{elName});
            }
            this.fStringBuffer.append(childName);
            if (this.fDTDContentModelHandler != null) {
                this.fDTDContentModelHandler.element(childName, null);
            }
            this.skipSeparator(false, !this.scanningInternalSubset());
        }
        if (this.fEntityScanner.skipString(")*")) {
            this.fStringBuffer.append(")*");
            if (this.fDTDContentModelHandler != null) {
                this.fDTDContentModelHandler.endGroup(null);
                this.fDTDContentModelHandler.occurrence((short)3, null);
            }
        } else if (childName != null) {
            this.reportFatalError("MixedContentUnterminated", new Object[]{elName});
        } else if (this.fEntityScanner.skipChar(41, null)) {
            this.fStringBuffer.append(')');
            if (this.fDTDContentModelHandler != null) {
                this.fDTDContentModelHandler.endGroup(null);
            }
        } else {
            this.reportFatalError("MSG_CLOSE_PAREN_REQUIRED_IN_CHILDREN", new Object[]{elName});
        }
        --this.fMarkUpDepth;
    }

    private final void scanChildren(String elName) throws IOException, XNIException {
        this.fContentDepth = 0;
        this.pushContentStack(0);
        int currentOp = 0;
        while (true) {
            block23: {
                short oc;
                if (this.fEntityScanner.skipChar(40, null)) {
                    ++this.fMarkUpDepth;
                    this.fStringBuffer.append('(');
                    if (this.fDTDContentModelHandler != null) {
                        this.fDTDContentModelHandler.startGroup(null);
                    }
                    this.pushContentStack(currentOp);
                    currentOp = 0;
                    this.skipSeparator(false, !this.scanningInternalSubset());
                    continue;
                }
                this.skipSeparator(false, !this.scanningInternalSubset());
                String childName = this.fEntityScanner.scanName(XMLScanner.NameType.ELEMENTSTART);
                if (childName == null) {
                    this.reportFatalError("MSG_OPEN_PAREN_OR_ELEMENT_TYPE_REQUIRED_IN_CHILDREN", new Object[]{elName});
                    return;
                }
                if (this.fDTDContentModelHandler != null) {
                    this.fDTDContentModelHandler.element(childName, null);
                }
                this.fStringBuffer.append(childName);
                int c = this.fEntityScanner.peekChar();
                if (c == 63 || c == 42 || c == 43) {
                    if (this.fDTDContentModelHandler != null) {
                        oc = c == 63 ? (short)2 : (c == 42 ? (short)3 : 4);
                        this.fDTDContentModelHandler.occurrence(oc, null);
                    }
                    this.fEntityScanner.scanChar(null);
                    this.fStringBuffer.append((char)c);
                }
                do {
                    this.skipSeparator(false, !this.scanningInternalSubset());
                    c = this.fEntityScanner.peekChar();
                    if (c == 44 && currentOp != 124) {
                        currentOp = c;
                        if (this.fDTDContentModelHandler != null) {
                            this.fDTDContentModelHandler.separator((short)1, null);
                        }
                        this.fEntityScanner.scanChar(null);
                        this.fStringBuffer.append(',');
                        break block23;
                    }
                    if (c == 124 && currentOp != 44) {
                        currentOp = c;
                        if (this.fDTDContentModelHandler != null) {
                            this.fDTDContentModelHandler.separator((short)0, null);
                        }
                        this.fEntityScanner.scanChar(null);
                        this.fStringBuffer.append('|');
                        break block23;
                    }
                    if (c != 41) {
                        this.reportFatalError("MSG_CLOSE_PAREN_REQUIRED_IN_CHILDREN", new Object[]{elName});
                    }
                    if (this.fDTDContentModelHandler != null) {
                        this.fDTDContentModelHandler.endGroup(null);
                    }
                    currentOp = this.popContentStack();
                    if (this.fEntityScanner.skipString(")?")) {
                        this.fStringBuffer.append(")?");
                        if (this.fDTDContentModelHandler != null) {
                            oc = 2;
                            this.fDTDContentModelHandler.occurrence(oc, null);
                        }
                    } else if (this.fEntityScanner.skipString(")+")) {
                        this.fStringBuffer.append(")+");
                        if (this.fDTDContentModelHandler != null) {
                            oc = 4;
                            this.fDTDContentModelHandler.occurrence(oc, null);
                        }
                    } else if (this.fEntityScanner.skipString(")*")) {
                        this.fStringBuffer.append(")*");
                        if (this.fDTDContentModelHandler != null) {
                            oc = 3;
                            this.fDTDContentModelHandler.occurrence(oc, null);
                        }
                    } else {
                        this.fEntityScanner.scanChar(null);
                        this.fStringBuffer.append(')');
                    }
                    --this.fMarkUpDepth;
                } while (this.fContentDepth != 0);
                return;
            }
            this.skipSeparator(false, !this.scanningInternalSubset());
        }
    }

    protected final void scanAttlistDecl() throws IOException, XNIException {
        String elName;
        this.fReportEntity = false;
        if (!this.skipSeparator(true, !this.scanningInternalSubset())) {
            this.reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ELEMENT_TYPE_IN_ATTLISTDECL", null);
        }
        if ((elName = this.fEntityScanner.scanName(XMLScanner.NameType.ELEMENTSTART)) == null) {
            this.reportFatalError("MSG_ELEMENT_TYPE_REQUIRED_IN_ATTLISTDECL", null);
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.startAttlist(elName, null);
        }
        if (!this.skipSeparator(true, !this.scanningInternalSubset())) {
            if (this.fEntityScanner.skipChar(62, null)) {
                if (this.fDTDHandler != null) {
                    this.fDTDHandler.endAttlist(null);
                }
                --this.fMarkUpDepth;
                return;
            }
            this.reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ATTRIBUTE_NAME_IN_ATTDEF", new Object[]{elName});
        }
        while (!this.fEntityScanner.skipChar(62, null)) {
            String name = this.fEntityScanner.scanName(XMLScanner.NameType.ATTRIBUTENAME);
            if (name == null) {
                this.reportFatalError("AttNameRequiredInAttDef", new Object[]{elName});
            }
            if (!this.skipSeparator(true, !this.scanningInternalSubset())) {
                this.reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ATTTYPE_IN_ATTDEF", new Object[]{elName, name});
            }
            String type = this.scanAttType(elName, name);
            if (!this.skipSeparator(true, !this.scanningInternalSubset())) {
                this.reportFatalError("MSG_SPACE_REQUIRED_BEFORE_DEFAULTDECL_IN_ATTDEF", new Object[]{elName, name});
            }
            String defaultType = this.scanAttDefaultDecl(elName, name, type, this.fLiteral, this.fLiteral2);
            String[] enumr = null;
            if ((this.fDTDHandler != null || this.nonValidatingMode) && this.fEnumerationCount != 0) {
                enumr = new String[this.fEnumerationCount];
                System.arraycopy(this.fEnumeration, 0, enumr, 0, this.fEnumerationCount);
            }
            if (defaultType != null && (defaultType.equals("#REQUIRED") || defaultType.equals("#IMPLIED"))) {
                if (this.fDTDHandler != null) {
                    this.fDTDHandler.attributeDecl(elName, name, type, enumr, defaultType, null, null, null);
                }
                if (this.nonValidatingMode) {
                    this.nvGrammarInfo.attributeDecl(elName, name, type, enumr, defaultType, null, null, null);
                }
            } else {
                if (this.fDTDHandler != null) {
                    this.fDTDHandler.attributeDecl(elName, name, type, enumr, defaultType, this.fLiteral, this.fLiteral2, null);
                }
                if (this.nonValidatingMode) {
                    this.nvGrammarInfo.attributeDecl(elName, name, type, enumr, defaultType, this.fLiteral, this.fLiteral2, null);
                }
            }
            this.skipSeparator(false, !this.scanningInternalSubset());
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.endAttlist(null);
        }
        --this.fMarkUpDepth;
        this.fReportEntity = true;
    }

    private final String scanAttType(String elName, String atName) throws IOException, XNIException {
        String type = null;
        this.fEnumerationCount = 0;
        if (this.fEntityScanner.skipString("CDATA")) {
            type = "CDATA";
        } else if (this.fEntityScanner.skipString("IDREFS")) {
            type = "IDREFS";
        } else if (this.fEntityScanner.skipString("IDREF")) {
            type = "IDREF";
        } else if (this.fEntityScanner.skipString("ID")) {
            type = "ID";
        } else if (this.fEntityScanner.skipString("ENTITY")) {
            type = "ENTITY";
        } else if (this.fEntityScanner.skipString("ENTITIES")) {
            type = "ENTITIES";
        } else if (this.fEntityScanner.skipString("NMTOKENS")) {
            type = "NMTOKENS";
        } else if (this.fEntityScanner.skipString("NMTOKEN")) {
            type = "NMTOKEN";
        } else if (this.fEntityScanner.skipString("NOTATION")) {
            int c;
            type = "NOTATION";
            if (!this.skipSeparator(true, !this.scanningInternalSubset())) {
                this.reportFatalError("MSG_SPACE_REQUIRED_AFTER_NOTATION_IN_NOTATIONTYPE", new Object[]{elName, atName});
            }
            if ((c = this.fEntityScanner.scanChar(null)) != 40) {
                this.reportFatalError("MSG_OPEN_PAREN_REQUIRED_IN_NOTATIONTYPE", new Object[]{elName, atName});
            }
            ++this.fMarkUpDepth;
            do {
                this.skipSeparator(false, !this.scanningInternalSubset());
                String aName = this.fEntityScanner.scanName(XMLScanner.NameType.ATTRIBUTENAME);
                if (aName == null) {
                    this.reportFatalError("MSG_NAME_REQUIRED_IN_NOTATIONTYPE", new Object[]{elName, atName});
                }
                this.ensureEnumerationSize(this.fEnumerationCount + 1);
                this.fEnumeration[this.fEnumerationCount++] = aName;
                this.skipSeparator(false, !this.scanningInternalSubset());
            } while ((c = this.fEntityScanner.scanChar(null)) == 124);
            if (c != 41) {
                this.reportFatalError("NotationTypeUnterminated", new Object[]{elName, atName});
            }
            --this.fMarkUpDepth;
        } else {
            type = "ENUMERATION";
            int c = this.fEntityScanner.scanChar(null);
            if (c != 40) {
                this.reportFatalError("AttTypeRequiredInAttDef", new Object[]{elName, atName});
            }
            ++this.fMarkUpDepth;
            do {
                this.skipSeparator(false, !this.scanningInternalSubset());
                String token = this.fEntityScanner.scanNmtoken();
                if (token == null) {
                    this.reportFatalError("MSG_NMTOKEN_REQUIRED_IN_ENUMERATION", new Object[]{elName, atName});
                }
                this.ensureEnumerationSize(this.fEnumerationCount + 1);
                this.fEnumeration[this.fEnumerationCount++] = token;
                this.skipSeparator(false, !this.scanningInternalSubset());
            } while ((c = this.fEntityScanner.scanChar(null)) == 124);
            if (c != 41) {
                this.reportFatalError("EnumerationUnterminated", new Object[]{elName, atName});
            }
            --this.fMarkUpDepth;
        }
        return type;
    }

    protected final String scanAttDefaultDecl(String elName, String atName, String type, XMLString defaultVal, XMLString nonNormalizedDefaultVal) throws IOException, XNIException {
        String defaultType = null;
        this.fString.clear();
        defaultVal.clear();
        if (this.fEntityScanner.skipString("#REQUIRED")) {
            defaultType = "#REQUIRED";
        } else if (this.fEntityScanner.skipString("#IMPLIED")) {
            defaultType = "#IMPLIED";
        } else {
            if (this.fEntityScanner.skipString("#FIXED")) {
                defaultType = "#FIXED";
                if (!this.skipSeparator(true, !this.scanningInternalSubset())) {
                    this.reportFatalError("MSG_SPACE_REQUIRED_AFTER_FIXED_IN_DEFAULTDECL", new Object[]{elName, atName});
                }
            }
            boolean isVC = !this.fStandalone && (this.fSeenExternalDTD || this.fSeenExternalPE);
            this.scanAttributeValue(defaultVal, nonNormalizedDefaultVal, atName, this.fAttributes, 0, isVC, elName, false);
        }
        return defaultType;
    }

    private final void scanEntityDecl() throws IOException, XNIException {
        Object name;
        boolean isPEDecl = false;
        boolean sawPERef = false;
        this.fReportEntity = false;
        if (this.fEntityScanner.skipSpaces()) {
            if (!this.fEntityScanner.skipChar(37, XMLScanner.NameType.REFERENCE)) {
                isPEDecl = false;
            } else if (this.skipSeparator(true, !this.scanningInternalSubset())) {
                isPEDecl = true;
            } else if (this.scanningInternalSubset()) {
                this.reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ENTITY_NAME_IN_ENTITYDECL", null);
                isPEDecl = true;
            } else if (this.fEntityScanner.peekChar() == 37) {
                this.skipSeparator(false, !this.scanningInternalSubset());
                isPEDecl = true;
            } else {
                sawPERef = true;
            }
        } else if (this.scanningInternalSubset() || !this.fEntityScanner.skipChar(37, XMLScanner.NameType.REFERENCE)) {
            this.reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ENTITY_NAME_IN_ENTITYDECL", null);
            isPEDecl = false;
        } else if (this.fEntityScanner.skipSpaces()) {
            this.reportFatalError("MSG_SPACE_REQUIRED_BEFORE_PERCENT_IN_PEDECL", null);
            isPEDecl = false;
        } else {
            sawPERef = true;
        }
        if (sawPERef) {
            while (true) {
                String peName;
                if ((peName = this.fEntityScanner.scanName(XMLScanner.NameType.REFERENCE)) == null) {
                    this.reportFatalError("NameRequiredInPEReference", null);
                } else if (!this.fEntityScanner.skipChar(59, XMLScanner.NameType.REFERENCE)) {
                    this.reportFatalError("SemicolonRequiredInPEReference", new Object[]{peName});
                } else {
                    this.startPE(peName, false);
                }
                this.fEntityScanner.skipSpaces();
                if (!this.fEntityScanner.skipChar(37, XMLScanner.NameType.REFERENCE)) break;
                if (isPEDecl) continue;
                if (this.skipSeparator(true, !this.scanningInternalSubset())) {
                    isPEDecl = true;
                    break;
                }
                isPEDecl = this.fEntityScanner.skipChar(37, XMLScanner.NameType.REFERENCE);
            }
        }
        if ((name = this.fEntityScanner.scanName(XMLScanner.NameType.ENTITY)) == null) {
            this.reportFatalError("MSG_ENTITY_NAME_REQUIRED_IN_ENTITYDECL", null);
        }
        if (!this.skipSeparator(true, !this.scanningInternalSubset())) {
            this.reportFatalError("MSG_SPACE_REQUIRED_AFTER_ENTITY_NAME_IN_ENTITYDECL", new Object[]{name});
        }
        this.scanExternalID(this.fStrings, false);
        String systemId = this.fStrings[0];
        String publicId = this.fStrings[1];
        if (isPEDecl && systemId != null) {
            this.fSeenExternalPE = true;
        }
        String notation = null;
        boolean sawSpace = this.skipSeparator(true, !this.scanningInternalSubset());
        if (!isPEDecl && this.fEntityScanner.skipString("NDATA")) {
            if (!sawSpace) {
                this.reportFatalError("MSG_SPACE_REQUIRED_BEFORE_NDATA_IN_UNPARSED_ENTITYDECL", new Object[]{name});
            }
            if (!this.skipSeparator(true, !this.scanningInternalSubset())) {
                this.reportFatalError("MSG_SPACE_REQUIRED_BEFORE_NOTATION_NAME_IN_UNPARSED_ENTITYDECL", new Object[]{name});
            }
            if ((notation = this.fEntityScanner.scanName(XMLScanner.NameType.NOTATION)) == null) {
                this.reportFatalError("MSG_NOTATION_NAME_REQUIRED_FOR_UNPARSED_ENTITYDECL", new Object[]{name});
            }
        }
        if (systemId == null) {
            this.scanEntityValue((String)name, isPEDecl, this.fLiteral, this.fLiteral2);
            this.fStringBuffer.clear();
            this.fStringBuffer2.clear();
            this.fStringBuffer.append(this.fLiteral.ch, this.fLiteral.offset, this.fLiteral.length);
            this.fStringBuffer2.append(this.fLiteral2.ch, this.fLiteral2.offset, this.fLiteral2.length);
        }
        this.skipSeparator(false, !this.scanningInternalSubset());
        if (!this.fEntityScanner.skipChar(62, null)) {
            this.reportFatalError("EntityDeclUnterminated", new Object[]{name});
        }
        --this.fMarkUpDepth;
        if (isPEDecl) {
            name = "%" + (String)name;
        }
        if (systemId != null) {
            String baseSystemId = this.fEntityScanner.getBaseSystemId();
            if (notation != null) {
                this.fEntityStore.addUnparsedEntity((String)name, publicId, systemId, baseSystemId, notation);
            } else {
                this.fEntityStore.addExternalEntity((String)name, publicId, systemId, baseSystemId);
            }
            if (this.fDTDHandler != null) {
                this.fResourceIdentifier.setValues(publicId, systemId, baseSystemId, XMLEntityManager.expandSystemId(systemId, baseSystemId));
                if (notation != null) {
                    this.fDTDHandler.unparsedEntityDecl((String)name, this.fResourceIdentifier, notation, null);
                } else {
                    this.fDTDHandler.externalEntityDecl((String)name, this.fResourceIdentifier, null);
                }
            }
        } else {
            this.fEntityStore.addInternalEntity((String)name, this.fStringBuffer.toString());
            if (this.fDTDHandler != null) {
                this.fDTDHandler.internalEntityDecl((String)name, this.fStringBuffer, this.fStringBuffer2, null);
            }
        }
        this.fReportEntity = true;
    }

    protected final void scanEntityValue(String entityName, boolean isPEDecl, XMLString value, XMLString nonNormalizedValue) throws IOException, XNIException {
        int quote = this.fEntityScanner.scanChar(null);
        if (quote != 39 && quote != 34) {
            this.reportFatalError("OpenQuoteMissingInDecl", null);
        }
        int entityDepth = this.fEntityDepth;
        XMLString literal = this.fString;
        XMLString literal2 = this.fString;
        int countChar = 0;
        if (this.fLimitAnalyzer == null) {
            this.fLimitAnalyzer = this.fEntityManager.fLimitAnalyzer;
        }
        this.fLimitAnalyzer.startEntity(entityName);
        if (this.fEntityScanner.scanLiteral(quote, this.fString, false) != quote) {
            this.fStringBuffer.clear();
            this.fStringBuffer2.clear();
            do {
                countChar = 0;
                int offset = this.fStringBuffer.length;
                this.fStringBuffer.append(this.fString);
                this.fStringBuffer2.append(this.fString);
                if (this.fEntityScanner.skipChar(38, XMLScanner.NameType.REFERENCE)) {
                    if (this.fEntityScanner.skipChar(35, XMLScanner.NameType.REFERENCE)) {
                        this.fStringBuffer2.append("&#");
                        this.scanCharReferenceValue(this.fStringBuffer, this.fStringBuffer2);
                    } else {
                        this.fStringBuffer.append('&');
                        this.fStringBuffer2.append('&');
                        String eName = this.fEntityScanner.scanName(XMLScanner.NameType.REFERENCE);
                        if (eName == null) {
                            this.reportFatalError("NameRequiredInReference", null);
                        } else {
                            this.fStringBuffer.append(eName);
                            this.fStringBuffer2.append(eName);
                        }
                        if (!this.fEntityScanner.skipChar(59, XMLScanner.NameType.REFERENCE)) {
                            this.reportFatalError("SemicolonRequiredInReference", new Object[]{eName});
                        } else {
                            this.fStringBuffer.append(';');
                            this.fStringBuffer2.append(';');
                        }
                    }
                } else if (this.fEntityScanner.skipChar(37, XMLScanner.NameType.REFERENCE)) {
                    do {
                        this.fStringBuffer2.append('%');
                        String peName = this.fEntityScanner.scanName(XMLScanner.NameType.REFERENCE);
                        if (peName == null) {
                            this.reportFatalError("NameRequiredInPEReference", null);
                        } else if (!this.fEntityScanner.skipChar(59, XMLScanner.NameType.REFERENCE)) {
                            this.reportFatalError("SemicolonRequiredInPEReference", new Object[]{peName});
                        } else {
                            if (this.scanningInternalSubset()) {
                                this.reportFatalError("PEReferenceWithinMarkup", new Object[]{peName});
                            }
                            this.fStringBuffer2.append(peName);
                            this.fStringBuffer2.append(';');
                        }
                        this.startPE(peName, true);
                        this.fEntityScanner.skipSpaces();
                    } while (this.fEntityScanner.skipChar(37, XMLScanner.NameType.REFERENCE));
                } else {
                    int c = this.fEntityScanner.peekChar();
                    if (XMLChar.isHighSurrogate(c)) {
                        ++countChar;
                        this.scanSurrogates(this.fStringBuffer2);
                    } else if (this.isInvalidLiteral(c)) {
                        this.reportFatalError("InvalidCharInLiteral", new Object[]{Integer.toHexString(c)});
                        this.fEntityScanner.scanChar(null);
                    } else if (c != quote || entityDepth != this.fEntityDepth) {
                        this.fStringBuffer.append((char)c);
                        this.fStringBuffer2.append((char)c);
                        this.fEntityScanner.scanChar(null);
                    }
                }
                this.checkEntityLimit(isPEDecl, entityName, this.fStringBuffer.length - offset + countChar);
            } while (this.fEntityScanner.scanLiteral(quote, this.fString, false) != quote);
            this.checkEntityLimit(isPEDecl, entityName, this.fString.length);
            this.fStringBuffer.append(this.fString);
            this.fStringBuffer2.append(this.fString);
            literal = this.fStringBuffer;
            literal2 = this.fStringBuffer2;
        } else {
            this.checkEntityLimit(isPEDecl, entityName, literal);
        }
        value.setValues(literal);
        nonNormalizedValue.setValues(literal2);
        if (this.fLimitAnalyzer != null) {
            if (isPEDecl) {
                this.fLimitAnalyzer.endEntity(XMLSecurityManager.Limit.PARAMETER_ENTITY_SIZE_LIMIT, entityName);
            } else {
                this.fLimitAnalyzer.endEntity(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT, entityName);
            }
        }
        if (!this.fEntityScanner.skipChar(quote, null)) {
            this.reportFatalError("CloseQuoteMissingInDecl", null);
        }
    }

    private final void scanNotationDecl() throws IOException, XNIException {
        String name;
        this.fReportEntity = false;
        if (!this.skipSeparator(true, !this.scanningInternalSubset())) {
            this.reportFatalError("MSG_SPACE_REQUIRED_BEFORE_NOTATION_NAME_IN_NOTATIONDECL", null);
        }
        if ((name = this.fEntityScanner.scanName(XMLScanner.NameType.NOTATION)) == null) {
            this.reportFatalError("MSG_NOTATION_NAME_REQUIRED_IN_NOTATIONDECL", null);
        }
        if (!this.skipSeparator(true, !this.scanningInternalSubset())) {
            this.reportFatalError("MSG_SPACE_REQUIRED_AFTER_NOTATION_NAME_IN_NOTATIONDECL", new Object[]{name});
        }
        this.scanExternalID(this.fStrings, true);
        String systemId = this.fStrings[0];
        String publicId = this.fStrings[1];
        String baseSystemId = this.fEntityScanner.getBaseSystemId();
        if (systemId == null && publicId == null) {
            this.reportFatalError("ExternalIDorPublicIDRequired", new Object[]{name});
        }
        this.skipSeparator(false, !this.scanningInternalSubset());
        if (!this.fEntityScanner.skipChar(62, null)) {
            this.reportFatalError("NotationDeclUnterminated", new Object[]{name});
        }
        --this.fMarkUpDepth;
        this.fResourceIdentifier.setValues(publicId, systemId, baseSystemId, XMLEntityManager.expandSystemId(systemId, baseSystemId));
        if (this.nonValidatingMode) {
            this.nvGrammarInfo.notationDecl(name, this.fResourceIdentifier, null);
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.notationDecl(name, this.fResourceIdentifier, null);
        }
        this.fReportEntity = true;
    }

    private final void scanConditionalSect(int currPEDepth) throws IOException, XNIException {
        this.fReportEntity = false;
        this.skipSeparator(false, !this.scanningInternalSubset());
        if (this.fEntityScanner.skipString("INCLUDE")) {
            this.skipSeparator(false, !this.scanningInternalSubset());
            if (currPEDepth != this.fPEDepth && this.fValidation) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "INVALID_PE_IN_CONDITIONAL", new Object[]{this.fEntityManager.fCurrentEntity.name}, (short)1);
            }
            if (!this.fEntityScanner.skipChar(91, null)) {
                this.reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD", null);
            }
            if (this.fDTDHandler != null) {
                this.fDTDHandler.startConditional((short)0, null);
            }
            ++this.fIncludeSectDepth;
            this.fReportEntity = true;
        } else {
            if (this.fEntityScanner.skipString("IGNORE")) {
                this.skipSeparator(false, !this.scanningInternalSubset());
                if (currPEDepth != this.fPEDepth && this.fValidation) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "INVALID_PE_IN_CONDITIONAL", new Object[]{this.fEntityManager.fCurrentEntity.name}, (short)1);
                }
                if (this.fDTDHandler != null) {
                    this.fDTDHandler.startConditional((short)1, null);
                }
                if (!this.fEntityScanner.skipChar(91, null)) {
                    this.reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD", null);
                }
                this.fReportEntity = true;
                int initialDepth = ++this.fIncludeSectDepth;
                if (this.fDTDHandler != null) {
                    this.fIgnoreConditionalBuffer.clear();
                }
                while (true) {
                    if (this.fEntityScanner.skipChar(60, null)) {
                        if (this.fDTDHandler != null) {
                            this.fIgnoreConditionalBuffer.append('<');
                        }
                        if (!this.fEntityScanner.skipChar(33, null)) continue;
                        if (this.fEntityScanner.skipChar(91, null)) {
                            if (this.fDTDHandler != null) {
                                this.fIgnoreConditionalBuffer.append("![");
                            }
                            ++this.fIncludeSectDepth;
                            continue;
                        }
                        if (this.fDTDHandler == null) continue;
                        this.fIgnoreConditionalBuffer.append("!");
                        continue;
                    }
                    if (this.fEntityScanner.skipChar(93, null)) {
                        if (this.fDTDHandler != null) {
                            this.fIgnoreConditionalBuffer.append(']');
                        }
                        if (!this.fEntityScanner.skipChar(93, null)) continue;
                        if (this.fDTDHandler != null) {
                            this.fIgnoreConditionalBuffer.append(']');
                        }
                        while (this.fEntityScanner.skipChar(93, null)) {
                            if (this.fDTDHandler == null) continue;
                            this.fIgnoreConditionalBuffer.append(']');
                        }
                        if (!this.fEntityScanner.skipChar(62, null)) continue;
                        if (this.fIncludeSectDepth-- == initialDepth) {
                            --this.fMarkUpDepth;
                            if (this.fDTDHandler != null) {
                                this.fLiteral.setValues(this.fIgnoreConditionalBuffer.ch, 0, this.fIgnoreConditionalBuffer.length - 2);
                                this.fDTDHandler.ignoredCharacters(this.fLiteral, null);
                                this.fDTDHandler.endConditional(null);
                            }
                            return;
                        }
                        if (this.fDTDHandler == null) continue;
                        this.fIgnoreConditionalBuffer.append('>');
                        continue;
                    }
                    int c = this.fEntityScanner.scanChar(null);
                    if (this.fScannerState == 0) {
                        this.reportFatalError("IgnoreSectUnterminated", null);
                        return;
                    }
                    if (this.fDTDHandler == null) continue;
                    this.fIgnoreConditionalBuffer.append((char)c);
                }
            }
            this.reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD", null);
        }
    }

    protected final boolean scanDecls(boolean complete) throws IOException, XNIException {
        this.skipSeparator(false, true);
        boolean again = true;
        while (again && this.fScannerState == 2) {
            again = complete;
            if (this.fEntityScanner.skipChar(60, null)) {
                ++this.fMarkUpDepth;
                if (this.fEntityScanner.skipChar(63, null)) {
                    this.fStringBuffer.clear();
                    this.scanPI(this.fStringBuffer);
                    --this.fMarkUpDepth;
                } else if (this.fEntityScanner.skipChar(33, null)) {
                    if (this.fEntityScanner.skipChar(45, null)) {
                        if (!this.fEntityScanner.skipChar(45, null)) {
                            this.reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD", null);
                        } else {
                            this.scanComment();
                        }
                    } else if (this.fEntityScanner.skipString("ELEMENT")) {
                        this.scanElementDecl();
                    } else if (this.fEntityScanner.skipString("ATTLIST")) {
                        this.scanAttlistDecl();
                    } else if (this.fEntityScanner.skipString("ENTITY")) {
                        this.scanEntityDecl();
                    } else if (this.fEntityScanner.skipString("NOTATION")) {
                        this.scanNotationDecl();
                    } else if (this.fEntityScanner.skipChar(91, null) && !this.scanningInternalSubset()) {
                        this.scanConditionalSect(this.fPEDepth);
                    } else {
                        --this.fMarkUpDepth;
                        this.reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD", null);
                    }
                } else {
                    --this.fMarkUpDepth;
                    this.reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD", null);
                }
            } else if (this.fIncludeSectDepth > 0 && this.fEntityScanner.skipChar(93, null)) {
                if (!this.fEntityScanner.skipChar(93, null) || !this.fEntityScanner.skipChar(62, null)) {
                    this.reportFatalError("IncludeSectUnterminated", null);
                }
                if (this.fDTDHandler != null) {
                    this.fDTDHandler.endConditional(null);
                }
                --this.fIncludeSectDepth;
                --this.fMarkUpDepth;
            } else {
                if (this.scanningInternalSubset() && this.fEntityScanner.peekChar() == 93) {
                    return false;
                }
                if (!this.fEntityScanner.skipSpaces()) {
                    this.reportFatalError("MSG_MARKUP_NOT_RECOGNIZED_IN_DTD", null);
                }
            }
            this.skipSeparator(false, true);
        }
        return this.fScannerState != 0;
    }

    private boolean skipSeparator(boolean spaceRequired, boolean lookForPERefs) throws IOException, XNIException {
        int depth = this.fPEDepth;
        boolean sawSpace = this.fEntityScanner.skipSpaces();
        if (!lookForPERefs || !this.fEntityScanner.skipChar(37, XMLScanner.NameType.REFERENCE)) {
            return !spaceRequired || sawSpace || depth != this.fPEDepth;
        }
        do {
            String name;
            if ((name = this.fEntityScanner.scanName(XMLScanner.NameType.ENTITY)) == null) {
                this.reportFatalError("NameRequiredInPEReference", null);
            } else if (!this.fEntityScanner.skipChar(59, XMLScanner.NameType.REFERENCE)) {
                this.reportFatalError("SemicolonRequiredInPEReference", new Object[]{name});
            }
            this.startPE(name, false);
            this.fEntityScanner.skipSpaces();
        } while (this.fEntityScanner.skipChar(37, XMLScanner.NameType.REFERENCE));
        return true;
    }

    private final void pushContentStack(int c) {
        if (this.fContentStack.length == this.fContentDepth) {
            int[] newStack = new int[this.fContentDepth * 2];
            System.arraycopy(this.fContentStack, 0, newStack, 0, this.fContentDepth);
            this.fContentStack = newStack;
        }
        this.fContentStack[this.fContentDepth++] = c;
    }

    private final int popContentStack() {
        return this.fContentStack[--this.fContentDepth];
    }

    private final void pushPEStack(int depth, boolean report) {
        if (this.fPEStack.length == this.fPEDepth) {
            int[] newIntStack = new int[this.fPEDepth * 2];
            System.arraycopy(this.fPEStack, 0, newIntStack, 0, this.fPEDepth);
            this.fPEStack = newIntStack;
            boolean[] newBooleanStack = new boolean[this.fPEDepth * 2];
            System.arraycopy(this.fPEReport, 0, newBooleanStack, 0, this.fPEDepth);
            this.fPEReport = newBooleanStack;
        }
        this.fPEReport[this.fPEDepth] = report;
        this.fPEStack[this.fPEDepth++] = depth;
    }

    private final int popPEStack() {
        return this.fPEStack[--this.fPEDepth];
    }

    private final boolean peekReportEntity() {
        return this.fPEReport[this.fPEDepth - 1];
    }

    private final void ensureEnumerationSize(int size) {
        if (this.fEnumeration.length == size) {
            String[] newEnum = new String[size * 2];
            System.arraycopy(this.fEnumeration, 0, newEnum, 0, size);
            this.fEnumeration = newEnum;
        }
    }

    private void init() {
        this.fStartDTDCalled = false;
        this.fExtEntityDepth = 0;
        this.fIncludeSectDepth = 0;
        this.fMarkUpDepth = 0;
        this.fPEDepth = 0;
        this.fStandalone = false;
        this.fSeenExternalDTD = false;
        this.fSeenExternalPE = false;
        this.setScannerState(1);
        this.fLimitAnalyzer = this.fEntityManager.fLimitAnalyzer;
        this.fSecurityManager = this.fEntityManager.fSecurityManager;
    }

    public DTDGrammar getGrammar() {
        return this.nvGrammarInfo;
    }
}

