/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.impl.dtd;

import com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import com.sun.org.apache.xerces.internal.impl.dtd.DTDGrammar;
import com.sun.org.apache.xerces.internal.impl.dtd.DTDGrammarBucket;
import com.sun.org.apache.xerces.internal.impl.dtd.XMLContentSpec;
import com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator;
import com.sun.org.apache.xerces.internal.impl.dtd.XMLEntityDecl;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.util.XMLChar;
import com.sun.org.apache.xerces.internal.util.XMLSymbols;
import com.sun.org.apache.xerces.internal.xni.Augmentations;
import com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler;
import com.sun.org.apache.xerces.internal.xni.XMLDTDHandler;
import com.sun.org.apache.xerces.internal.xni.XMLLocator;
import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XMLString;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDTDContentModelFilter;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDTDContentModelSource;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDTDFilter;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

public class XMLDTDProcessor
implements XMLComponent,
XMLDTDFilter,
XMLDTDContentModelFilter {
    private static final int TOP_LEVEL_SCOPE = -1;
    protected static final String VALIDATION = "http://xml.org/sax/features/validation";
    protected static final String NOTIFY_CHAR_REFS = "http://apache.org/xml/features/scanner/notify-char-refs";
    protected static final String WARN_ON_DUPLICATE_ATTDEF = "http://apache.org/xml/features/validation/warn-on-duplicate-attdef";
    protected static final String WARN_ON_UNDECLARED_ELEMDEF = "http://apache.org/xml/features/validation/warn-on-undeclared-elemdef";
    protected static final String PARSER_SETTINGS = "http://apache.org/xml/features/internal/parser-settings";
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    protected static final String GRAMMAR_POOL = "http://apache.org/xml/properties/internal/grammar-pool";
    protected static final String DTD_VALIDATOR = "http://apache.org/xml/properties/internal/validator/dtd";
    private static final String[] RECOGNIZED_FEATURES = new String[]{"http://xml.org/sax/features/validation", "http://apache.org/xml/features/validation/warn-on-duplicate-attdef", "http://apache.org/xml/features/validation/warn-on-undeclared-elemdef", "http://apache.org/xml/features/scanner/notify-char-refs"};
    private static final Boolean[] FEATURE_DEFAULTS = new Boolean[]{null, Boolean.FALSE, Boolean.FALSE, null};
    private static final String[] RECOGNIZED_PROPERTIES = new String[]{"http://apache.org/xml/properties/internal/symbol-table", "http://apache.org/xml/properties/internal/error-reporter", "http://apache.org/xml/properties/internal/grammar-pool", "http://apache.org/xml/properties/internal/validator/dtd"};
    private static final Object[] PROPERTY_DEFAULTS = new Object[]{null, null, null, null};
    protected boolean fValidation;
    protected boolean fDTDValidation;
    protected boolean fWarnDuplicateAttdef;
    protected boolean fWarnOnUndeclaredElemdef;
    protected SymbolTable fSymbolTable;
    protected XMLErrorReporter fErrorReporter;
    protected DTDGrammarBucket fGrammarBucket;
    protected XMLDTDValidator fValidator;
    protected XMLGrammarPool fGrammarPool;
    protected Locale fLocale;
    protected XMLDTDHandler fDTDHandler;
    protected XMLDTDSource fDTDSource;
    protected XMLDTDContentModelHandler fDTDContentModelHandler;
    protected XMLDTDContentModelSource fDTDContentModelSource;
    protected DTDGrammar fDTDGrammar;
    private boolean fPerformValidation;
    protected boolean fInDTDIgnore;
    private boolean fMixed;
    private final XMLEntityDecl fEntityDecl = new XMLEntityDecl();
    private final Map<String, String> fNDataDeclNotations = new HashMap<String, String>();
    private String fDTDElementDeclName = null;
    private final List<String> fMixedElementTypes = new ArrayList<String>();
    private final List<String> fDTDElementDecls = new ArrayList<String>();
    private Map<String, String> fTableOfIDAttributeNames;
    private Map<String, String> fTableOfNOTATIONAttributeNames;
    private Map<String, String> fNotationEnumVals;

    @Override
    public void reset(XMLComponentManager componentManager) throws XMLConfigurationException {
        boolean parser_settings = componentManager.getFeature(PARSER_SETTINGS, true);
        if (!parser_settings) {
            this.reset();
            return;
        }
        this.fValidation = componentManager.getFeature(VALIDATION, false);
        this.fDTDValidation = !componentManager.getFeature("http://apache.org/xml/features/validation/schema", false);
        this.fWarnDuplicateAttdef = componentManager.getFeature(WARN_ON_DUPLICATE_ATTDEF, false);
        this.fWarnOnUndeclaredElemdef = componentManager.getFeature(WARN_ON_UNDECLARED_ELEMDEF, false);
        this.fErrorReporter = (XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);
        this.fSymbolTable = (SymbolTable)componentManager.getProperty(SYMBOL_TABLE);
        this.fGrammarPool = (XMLGrammarPool)componentManager.getProperty(GRAMMAR_POOL, null);
        try {
            this.fValidator = (XMLDTDValidator)componentManager.getProperty(DTD_VALIDATOR, null);
        }
        catch (ClassCastException e) {
            this.fValidator = null;
        }
        this.fGrammarBucket = this.fValidator != null ? this.fValidator.getGrammarBucket() : null;
        this.reset();
    }

    protected void reset() {
        this.fDTDGrammar = null;
        this.fInDTDIgnore = false;
        this.fNDataDeclNotations.clear();
        if (this.fValidation) {
            if (this.fNotationEnumVals == null) {
                this.fNotationEnumVals = new HashMap<String, String>();
            }
            this.fNotationEnumVals.clear();
            this.fTableOfIDAttributeNames = new HashMap<String, String>();
            this.fTableOfNOTATIONAttributeNames = new HashMap<String, String>();
        }
    }

    @Override
    public String[] getRecognizedFeatures() {
        return (String[])RECOGNIZED_FEATURES.clone();
    }

    @Override
    public void setFeature(String featureId, boolean state) throws XMLConfigurationException {
    }

    @Override
    public String[] getRecognizedProperties() {
        return (String[])RECOGNIZED_PROPERTIES.clone();
    }

    @Override
    public void setProperty(String propertyId, Object value) throws XMLConfigurationException {
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
    public void startExternalSubset(XMLResourceIdentifier identifier, Augmentations augs) throws XNIException {
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.startExternalSubset(identifier, augs);
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.startExternalSubset(identifier, augs);
        }
    }

    @Override
    public void endExternalSubset(Augmentations augs) throws XNIException {
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.endExternalSubset(augs);
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.endExternalSubset(augs);
        }
    }

    protected static void checkStandaloneEntityRef(String name, DTDGrammar grammar, XMLEntityDecl tempEntityDecl, XMLErrorReporter errorReporter) throws XNIException {
        int entIndex = grammar.getEntityDeclIndex(name);
        if (entIndex > -1) {
            grammar.getEntityDecl(entIndex, tempEntityDecl);
            if (tempEntityDecl.inExternal) {
                errorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_REFERENCE_TO_EXTERNALLY_DECLARED_ENTITY_WHEN_STANDALONE", new Object[]{name}, (short)1);
            }
        }
    }

    @Override
    public void comment(XMLString text, Augmentations augs) throws XNIException {
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.comment(text, augs);
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.comment(text, augs);
        }
    }

    @Override
    public void processingInstruction(String target, XMLString data, Augmentations augs) throws XNIException {
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.processingInstruction(target, data, augs);
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.processingInstruction(target, data, augs);
        }
    }

    @Override
    public void startDTD(XMLLocator locator, Augmentations augs) throws XNIException {
        this.fNDataDeclNotations.clear();
        this.fDTDElementDecls.clear();
        if (!this.fGrammarBucket.getActiveGrammar().isImmutable()) {
            this.fDTDGrammar = this.fGrammarBucket.getActiveGrammar();
        }
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.startDTD(locator, augs);
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.startDTD(locator, augs);
        }
    }

    @Override
    public void ignoredCharacters(XMLString text, Augmentations augs) throws XNIException {
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.ignoredCharacters(text, augs);
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.ignoredCharacters(text, augs);
        }
    }

    @Override
    public void textDecl(String version, String encoding, Augmentations augs) throws XNIException {
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.textDecl(version, encoding, augs);
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.textDecl(version, encoding, augs);
        }
    }

    @Override
    public void startParameterEntity(String name, XMLResourceIdentifier identifier, String encoding, Augmentations augs) throws XNIException {
        if (this.fPerformValidation && this.fDTDGrammar != null && this.fGrammarBucket.getStandalone()) {
            XMLDTDProcessor.checkStandaloneEntityRef(name, this.fDTDGrammar, this.fEntityDecl, this.fErrorReporter);
        }
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.startParameterEntity(name, identifier, encoding, augs);
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.startParameterEntity(name, identifier, encoding, augs);
        }
    }

    @Override
    public void endParameterEntity(String name, Augmentations augs) throws XNIException {
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.endParameterEntity(name, augs);
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.endParameterEntity(name, augs);
        }
    }

    @Override
    public void elementDecl(String name, String contentModel, Augmentations augs) throws XNIException {
        if (this.fValidation) {
            if (this.fDTDElementDecls.contains(name)) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_ELEMENT_ALREADY_DECLARED", new Object[]{name}, (short)1);
            } else {
                this.fDTDElementDecls.add(name);
            }
        }
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.elementDecl(name, contentModel, augs);
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.elementDecl(name, contentModel, augs);
        }
    }

    @Override
    public void startAttlist(String elementName, Augmentations augs) throws XNIException {
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.startAttlist(elementName, augs);
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.startAttlist(elementName, augs);
        }
    }

    @Override
    public void attributeDecl(String elementName, String attributeName, String type, String[] enumeration, String defaultType, XMLString defaultValue, XMLString nonNormalizedDefaultValue, Augmentations augs) throws XNIException {
        if (type != XMLSymbols.fCDATASymbol && defaultValue != null) {
            this.normalizeDefaultAttrValue(defaultValue);
        }
        if (this.fValidation) {
            int elementIndex;
            boolean duplicateAttributeDef = false;
            DTDGrammar grammar = this.fDTDGrammar != null ? this.fDTDGrammar : this.fGrammarBucket.getActiveGrammar();
            if (grammar.getAttributeDeclIndex(elementIndex = grammar.getElementDeclIndex(elementName), attributeName) != -1) {
                duplicateAttributeDef = true;
                if (this.fWarnDuplicateAttdef) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_DUPLICATE_ATTRIBUTE_DEFINITION", new Object[]{elementName, attributeName}, (short)0);
                }
            }
            if (type == XMLSymbols.fIDSymbol) {
                if (defaultValue != null && defaultValue.length != 0 && (defaultType == null || defaultType != XMLSymbols.fIMPLIEDSymbol && defaultType != XMLSymbols.fREQUIREDSymbol)) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "IDDefaultTypeInvalid", new Object[]{attributeName}, (short)1);
                }
                if (!this.fTableOfIDAttributeNames.containsKey(elementName)) {
                    this.fTableOfIDAttributeNames.put(elementName, attributeName);
                } else if (!duplicateAttributeDef) {
                    String previousIDAttributeName = this.fTableOfIDAttributeNames.get(elementName);
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_MORE_THAN_ONE_ID_ATTRIBUTE", new Object[]{elementName, previousIDAttributeName, attributeName}, (short)1);
                }
            }
            if (type == XMLSymbols.fNOTATIONSymbol) {
                for (int i = 0; i < enumeration.length; ++i) {
                    this.fNotationEnumVals.put(enumeration[i], attributeName);
                }
                if (!this.fTableOfNOTATIONAttributeNames.containsKey(elementName)) {
                    this.fTableOfNOTATIONAttributeNames.put(elementName, attributeName);
                } else if (!duplicateAttributeDef) {
                    String previousNOTATIONAttributeName = this.fTableOfNOTATIONAttributeNames.get(elementName);
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_MORE_THAN_ONE_NOTATION_ATTRIBUTE", new Object[]{elementName, previousNOTATIONAttributeName, attributeName}, (short)1);
                }
            }
            if (type == XMLSymbols.fENUMERATIONSymbol || type == XMLSymbols.fNOTATIONSymbol) {
                block1: for (int i = 0; i < enumeration.length; ++i) {
                    for (int j = i + 1; j < enumeration.length; ++j) {
                        if (!enumeration[i].equals(enumeration[j])) continue;
                        this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", type == XMLSymbols.fENUMERATIONSymbol ? "MSG_DISTINCT_TOKENS_IN_ENUMERATION" : "MSG_DISTINCT_NOTATION_IN_ENUMERATION", new Object[]{elementName, enumeration[i], attributeName}, (short)1);
                        break block1;
                    }
                }
            }
            boolean ok = true;
            if (defaultValue != null && (defaultType == null || defaultType != null && defaultType == XMLSymbols.fFIXEDSymbol)) {
                String value = defaultValue.toString();
                if (type == XMLSymbols.fNMTOKENSSymbol || type == XMLSymbols.fENTITIESSymbol || type == XMLSymbols.fIDREFSSymbol) {
                    StringTokenizer tokenizer = new StringTokenizer(value, " ");
                    if (tokenizer.hasMoreTokens()) {
                        do {
                            String nmtoken = tokenizer.nextToken();
                            if (type == XMLSymbols.fNMTOKENSSymbol) {
                                if (this.isValidNmtoken(nmtoken)) continue;
                                ok = false;
                            } else {
                                if (type != XMLSymbols.fENTITIESSymbol && type != XMLSymbols.fIDREFSSymbol || this.isValidName(nmtoken)) continue;
                                ok = false;
                            }
                            break;
                        } while (tokenizer.hasMoreTokens());
                    }
                } else {
                    if (type == XMLSymbols.fENTITYSymbol || type == XMLSymbols.fIDSymbol || type == XMLSymbols.fIDREFSymbol || type == XMLSymbols.fNOTATIONSymbol) {
                        if (!this.isValidName(value)) {
                            ok = false;
                        }
                    } else if (!(type != XMLSymbols.fNMTOKENSymbol && type != XMLSymbols.fENUMERATIONSymbol || this.isValidNmtoken(value))) {
                        ok = false;
                    }
                    if (type == XMLSymbols.fNOTATIONSymbol || type == XMLSymbols.fENUMERATIONSymbol) {
                        ok = false;
                        for (int i = 0; i < enumeration.length; ++i) {
                            if (!defaultValue.equals(enumeration[i])) continue;
                            ok = true;
                        }
                    }
                }
                if (!ok) {
                    this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_ATT_DEFAULT_INVALID", new Object[]{attributeName, value}, (short)1);
                }
            }
        }
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.attributeDecl(elementName, attributeName, type, enumeration, defaultType, defaultValue, nonNormalizedDefaultValue, augs);
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.attributeDecl(elementName, attributeName, type, enumeration, defaultType, defaultValue, nonNormalizedDefaultValue, augs);
        }
    }

    @Override
    public void endAttlist(Augmentations augs) throws XNIException {
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.endAttlist(augs);
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.endAttlist(augs);
        }
    }

    @Override
    public void internalEntityDecl(String name, XMLString text, XMLString nonNormalizedText, Augmentations augs) throws XNIException {
        DTDGrammar grammar = this.fDTDGrammar != null ? this.fDTDGrammar : this.fGrammarBucket.getActiveGrammar();
        int index = grammar.getEntityDeclIndex(name);
        if (index == -1) {
            if (this.fDTDGrammar != null) {
                this.fDTDGrammar.internalEntityDecl(name, text, nonNormalizedText, augs);
            }
            if (this.fDTDHandler != null) {
                this.fDTDHandler.internalEntityDecl(name, text, nonNormalizedText, augs);
            }
        }
    }

    @Override
    public void externalEntityDecl(String name, XMLResourceIdentifier identifier, Augmentations augs) throws XNIException {
        DTDGrammar grammar = this.fDTDGrammar != null ? this.fDTDGrammar : this.fGrammarBucket.getActiveGrammar();
        int index = grammar.getEntityDeclIndex(name);
        if (index == -1) {
            if (this.fDTDGrammar != null) {
                this.fDTDGrammar.externalEntityDecl(name, identifier, augs);
            }
            if (this.fDTDHandler != null) {
                this.fDTDHandler.externalEntityDecl(name, identifier, augs);
            }
        }
    }

    @Override
    public void unparsedEntityDecl(String name, XMLResourceIdentifier identifier, String notation, Augmentations augs) throws XNIException {
        if (this.fValidation) {
            this.fNDataDeclNotations.put(name, notation);
        }
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.unparsedEntityDecl(name, identifier, notation, augs);
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.unparsedEntityDecl(name, identifier, notation, augs);
        }
    }

    @Override
    public void notationDecl(String name, XMLResourceIdentifier identifier, Augmentations augs) throws XNIException {
        if (this.fValidation) {
            DTDGrammar grammar;
            DTDGrammar dTDGrammar = grammar = this.fDTDGrammar != null ? this.fDTDGrammar : this.fGrammarBucket.getActiveGrammar();
            if (grammar.getNotationDeclIndex(name) != -1) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "UniqueNotationName", new Object[]{name}, (short)1);
            }
        }
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.notationDecl(name, identifier, augs);
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.notationDecl(name, identifier, augs);
        }
    }

    @Override
    public void startConditional(short type, Augmentations augs) throws XNIException {
        boolean bl = this.fInDTDIgnore = type == 1;
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.startConditional(type, augs);
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.startConditional(type, augs);
        }
    }

    @Override
    public void endConditional(Augmentations augs) throws XNIException {
        this.fInDTDIgnore = false;
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.endConditional(augs);
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.endConditional(augs);
        }
    }

    @Override
    public void endDTD(Augmentations augs) throws XNIException {
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.endDTD(augs);
            if (this.fGrammarPool != null) {
                this.fGrammarPool.cacheGrammars("http://www.w3.org/TR/REC-xml", new Grammar[]{this.fDTDGrammar});
            }
        }
        if (this.fValidation) {
            String notation;
            DTDGrammar grammar = this.fDTDGrammar != null ? this.fDTDGrammar : this.fGrammarBucket.getActiveGrammar();
            for (Map.Entry<String, String> entry : this.fNDataDeclNotations.entrySet()) {
                notation = entry.getValue();
                if (grammar.getNotationDeclIndex(notation) != -1) continue;
                String entity = entry.getKey();
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_NOTATION_NOT_DECLARED_FOR_UNPARSED_ENTITYDECL", new Object[]{entity, notation}, (short)1);
            }
            for (Map.Entry<String, String> entry : this.fNotationEnumVals.entrySet()) {
                notation = entry.getKey();
                if (grammar.getNotationDeclIndex(notation) != -1) continue;
                String attributeName = entry.getValue();
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "MSG_NOTATION_NOT_DECLARED_FOR_NOTATIONTYPE_ATTRIBUTE", new Object[]{attributeName, notation}, (short)1);
            }
            for (Map.Entry<String, String> entry : this.fTableOfNOTATIONAttributeNames.entrySet()) {
                String elementName = entry.getKey();
                int elementIndex = grammar.getElementDeclIndex(elementName);
                if (grammar.getContentSpecType(elementIndex) != 1) continue;
                String attributeName = entry.getValue();
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "NoNotationOnEmptyElement", new Object[]{elementName, attributeName}, (short)1);
            }
            this.fTableOfIDAttributeNames = null;
            this.fTableOfNOTATIONAttributeNames = null;
            if (this.fWarnOnUndeclaredElemdef) {
                this.checkDeclaredElements(grammar);
            }
        }
        if (this.fDTDHandler != null) {
            this.fDTDHandler.endDTD(augs);
        }
    }

    @Override
    public void setDTDSource(XMLDTDSource source) {
        this.fDTDSource = source;
    }

    @Override
    public XMLDTDSource getDTDSource() {
        return this.fDTDSource;
    }

    @Override
    public void setDTDContentModelSource(XMLDTDContentModelSource source) {
        this.fDTDContentModelSource = source;
    }

    @Override
    public XMLDTDContentModelSource getDTDContentModelSource() {
        return this.fDTDContentModelSource;
    }

    @Override
    public void startContentModel(String elementName, Augmentations augs) throws XNIException {
        if (this.fValidation) {
            this.fDTDElementDeclName = elementName;
            this.fMixedElementTypes.clear();
        }
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.startContentModel(elementName, augs);
        }
        if (this.fDTDContentModelHandler != null) {
            this.fDTDContentModelHandler.startContentModel(elementName, augs);
        }
    }

    @Override
    public void any(Augmentations augs) throws XNIException {
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.any(augs);
        }
        if (this.fDTDContentModelHandler != null) {
            this.fDTDContentModelHandler.any(augs);
        }
    }

    @Override
    public void empty(Augmentations augs) throws XNIException {
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.empty(augs);
        }
        if (this.fDTDContentModelHandler != null) {
            this.fDTDContentModelHandler.empty(augs);
        }
    }

    @Override
    public void startGroup(Augmentations augs) throws XNIException {
        this.fMixed = false;
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.startGroup(augs);
        }
        if (this.fDTDContentModelHandler != null) {
            this.fDTDContentModelHandler.startGroup(augs);
        }
    }

    @Override
    public void pcdata(Augmentations augs) {
        this.fMixed = true;
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.pcdata(augs);
        }
        if (this.fDTDContentModelHandler != null) {
            this.fDTDContentModelHandler.pcdata(augs);
        }
    }

    @Override
    public void element(String elementName, Augmentations augs) throws XNIException {
        if (this.fMixed && this.fValidation) {
            if (this.fMixedElementTypes.contains(elementName)) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "DuplicateTypeInMixedContent", new Object[]{this.fDTDElementDeclName, elementName}, (short)1);
            } else {
                this.fMixedElementTypes.add(elementName);
            }
        }
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.element(elementName, augs);
        }
        if (this.fDTDContentModelHandler != null) {
            this.fDTDContentModelHandler.element(elementName, augs);
        }
    }

    @Override
    public void separator(short separator, Augmentations augs) throws XNIException {
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.separator(separator, augs);
        }
        if (this.fDTDContentModelHandler != null) {
            this.fDTDContentModelHandler.separator(separator, augs);
        }
    }

    @Override
    public void occurrence(short occurrence, Augmentations augs) throws XNIException {
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.occurrence(occurrence, augs);
        }
        if (this.fDTDContentModelHandler != null) {
            this.fDTDContentModelHandler.occurrence(occurrence, augs);
        }
    }

    @Override
    public void endGroup(Augmentations augs) throws XNIException {
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.endGroup(augs);
        }
        if (this.fDTDContentModelHandler != null) {
            this.fDTDContentModelHandler.endGroup(augs);
        }
    }

    @Override
    public void endContentModel(Augmentations augs) throws XNIException {
        if (this.fDTDGrammar != null) {
            this.fDTDGrammar.endContentModel(augs);
        }
        if (this.fDTDContentModelHandler != null) {
            this.fDTDContentModelHandler.endContentModel(augs);
        }
    }

    private boolean normalizeDefaultAttrValue(XMLString value) {
        boolean skipSpace = true;
        int current = value.offset;
        int end = value.offset + value.length;
        for (int i = value.offset; i < end; ++i) {
            if (value.ch[i] == ' ') {
                if (skipSpace) continue;
                value.ch[current++] = 32;
                skipSpace = true;
                continue;
            }
            if (current != i) {
                value.ch[current] = value.ch[i];
            }
            ++current;
            skipSpace = false;
        }
        if (current != end) {
            if (skipSpace) {
                --current;
            }
            value.length = current - value.offset;
            return true;
        }
        return false;
    }

    protected boolean isValidNmtoken(String nmtoken) {
        return XMLChar.isValidNmtoken(nmtoken);
    }

    protected boolean isValidName(String name) {
        return XMLChar.isValidName(name);
    }

    private void checkDeclaredElements(DTDGrammar grammar) {
        int elementIndex = grammar.getFirstElementDeclIndex();
        XMLContentSpec contentSpec = new XMLContentSpec();
        while (elementIndex >= 0) {
            short type = grammar.getContentSpecType(elementIndex);
            if (type == 3 || type == 2) {
                this.checkDeclaredElements(grammar, elementIndex, grammar.getContentSpecIndex(elementIndex), contentSpec);
            }
            elementIndex = grammar.getNextElementDeclIndex(elementIndex);
        }
    }

    private void checkDeclaredElements(DTDGrammar grammar, int elementIndex, int contentSpecIndex, XMLContentSpec contentSpec) {
        grammar.getContentSpec(contentSpecIndex, contentSpec);
        if (contentSpec.type == 0) {
            String value = (String)contentSpec.value;
            if (value != null && grammar.getElementDeclIndex(value) == -1) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "UndeclaredElementInContentSpec", new Object[]{grammar.getElementDeclName((int)elementIndex).rawname, value}, (short)0);
            }
        } else if (contentSpec.type == 4 || contentSpec.type == 5) {
            int leftNode = ((int[])contentSpec.value)[0];
            int rightNode = ((int[])contentSpec.otherValue)[0];
            this.checkDeclaredElements(grammar, elementIndex, leftNode, contentSpec);
            this.checkDeclaredElements(grammar, elementIndex, rightNode, contentSpec);
        } else if (contentSpec.type == 2 || contentSpec.type == 1 || contentSpec.type == 3) {
            int leftNode = ((int[])contentSpec.value)[0];
            this.checkDeclaredElements(grammar, elementIndex, leftNode, contentSpec);
        }
    }
}

