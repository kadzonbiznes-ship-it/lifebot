/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.parsers;

import com.sun.org.apache.xerces.internal.impl.XML11DTDScannerImpl;
import com.sun.org.apache.xerces.internal.impl.XML11DocumentScannerImpl;
import com.sun.org.apache.xerces.internal.impl.XML11NSDocumentScannerImpl;
import com.sun.org.apache.xerces.internal.impl.XMLDTDScannerImpl;
import com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl;
import com.sun.org.apache.xerces.internal.impl.XMLEntityHandler;
import com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import com.sun.org.apache.xerces.internal.impl.XMLNSDocumentScannerImpl;
import com.sun.org.apache.xerces.internal.impl.XMLVersionDetector;
import com.sun.org.apache.xerces.internal.impl.dtd.XML11DTDProcessor;
import com.sun.org.apache.xerces.internal.impl.dtd.XML11DTDValidator;
import com.sun.org.apache.xerces.internal.impl.dtd.XML11NSDTDValidator;
import com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDProcessor;
import com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator;
import com.sun.org.apache.xerces.internal.impl.dtd.XMLNSDTDValidator;
import com.sun.org.apache.xerces.internal.impl.dv.DTDDVFactory;
import com.sun.org.apache.xerces.internal.impl.msg.XMLMessageFormatter;
import com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator;
import com.sun.org.apache.xerces.internal.impl.xs.XSMessageFormatter;
import com.sun.org.apache.xerces.internal.parsers.XML11Configurable;
import com.sun.org.apache.xerces.internal.util.FeatureState;
import com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings;
import com.sun.org.apache.xerces.internal.util.PropertyState;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler;
import com.sun.org.apache.xerces.internal.xni.XMLDTDHandler;
import com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import com.sun.org.apache.xerces.internal.xni.XMLLocator;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDTDScanner;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentScanner;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import com.sun.org.apache.xerces.internal.xni.parser.XMLPullParserConfiguration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.xml.catalog.CatalogFeatures;
import jdk.xml.internal.JdkConstants;
import jdk.xml.internal.JdkXmlUtils;

public class XML11Configuration
extends ParserConfigurationSettings
implements XMLPullParserConfiguration,
XML11Configurable {
    protected static final String XML11_DATATYPE_VALIDATOR_FACTORY = "com.sun.org.apache.xerces.internal.impl.dv.dtd.XML11DTDDVFactoryImpl";
    protected static final String WARN_ON_DUPLICATE_ATTDEF = "http://apache.org/xml/features/validation/warn-on-duplicate-attdef";
    protected static final String WARN_ON_DUPLICATE_ENTITYDEF = "http://apache.org/xml/features/warn-on-duplicate-entitydef";
    protected static final String WARN_ON_UNDECLARED_ELEMDEF = "http://apache.org/xml/features/validation/warn-on-undeclared-elemdef";
    protected static final String ALLOW_JAVA_ENCODINGS = "http://apache.org/xml/features/allow-java-encodings";
    protected static final String CONTINUE_AFTER_FATAL_ERROR = "http://apache.org/xml/features/continue-after-fatal-error";
    protected static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    protected static final String NOTIFY_BUILTIN_REFS = "http://apache.org/xml/features/scanner/notify-builtin-refs";
    protected static final String NOTIFY_CHAR_REFS = "http://apache.org/xml/features/scanner/notify-char-refs";
    protected static final String NORMALIZE_DATA = "http://apache.org/xml/features/validation/schema/normalized-value";
    protected static final String SCHEMA_ELEMENT_DEFAULT = "http://apache.org/xml/features/validation/schema/element-default";
    protected static final String SCHEMA_AUGMENT_PSVI = "http://apache.org/xml/features/validation/schema/augment-psvi";
    protected static final String XMLSCHEMA_VALIDATION = "http://apache.org/xml/features/validation/schema";
    protected static final String XMLSCHEMA_FULL_CHECKING = "http://apache.org/xml/features/validation/schema-full-checking";
    protected static final String GENERATE_SYNTHETIC_ANNOTATIONS = "http://apache.org/xml/features/generate-synthetic-annotations";
    protected static final String VALIDATE_ANNOTATIONS = "http://apache.org/xml/features/validate-annotations";
    protected static final String HONOUR_ALL_SCHEMALOCATIONS = "http://apache.org/xml/features/honour-all-schemaLocations";
    protected static final String NAMESPACE_GROWTH = "http://apache.org/xml/features/namespace-growth";
    protected static final String TOLERATE_DUPLICATES = "http://apache.org/xml/features/internal/tolerate-duplicates";
    protected static final String USE_GRAMMAR_POOL_ONLY = "http://apache.org/xml/features/internal/validation/schema/use-grammar-pool-only";
    protected static final String VALIDATION = "http://xml.org/sax/features/validation";
    protected static final String NAMESPACES = "http://xml.org/sax/features/namespaces";
    protected static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
    protected static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
    protected static final String IGNORE_XSI_TYPE = "http://apache.org/xml/features/validation/schema/ignore-xsi-type-until-elemdecl";
    protected static final String ID_IDREF_CHECKING = "http://apache.org/xml/features/validation/id-idref-checking";
    protected static final String UNPARSED_ENTITY_CHECKING = "http://apache.org/xml/features/validation/unparsed-entity-checking";
    protected static final String IDENTITY_CONSTRAINT_CHECKING = "http://apache.org/xml/features/validation/identity-constraint-checking";
    protected static final String XML_STRING = "http://xml.org/sax/properties/xml-string";
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    protected static final String ERROR_HANDLER = "http://apache.org/xml/properties/internal/error-handler";
    protected static final String ENTITY_RESOLVER = "http://apache.org/xml/properties/internal/entity-resolver";
    protected static final String SCHEMA_VALIDATOR = "http://apache.org/xml/properties/internal/validator/schema";
    protected static final String SCHEMA_LOCATION = "http://apache.org/xml/properties/schema/external-schemaLocation";
    protected static final String SCHEMA_NONS_LOCATION = "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation";
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    protected static final String ENTITY_MANAGER = "http://apache.org/xml/properties/internal/entity-manager";
    protected static final String DOCUMENT_SCANNER = "http://apache.org/xml/properties/internal/document-scanner";
    protected static final String DTD_SCANNER = "http://apache.org/xml/properties/internal/dtd-scanner";
    protected static final String XMLGRAMMAR_POOL = "http://apache.org/xml/properties/internal/grammar-pool";
    protected static final String DTD_PROCESSOR = "http://apache.org/xml/properties/internal/dtd-processor";
    protected static final String DTD_VALIDATOR = "http://apache.org/xml/properties/internal/validator/dtd";
    protected static final String NAMESPACE_BINDER = "http://apache.org/xml/properties/internal/namespace-binder";
    protected static final String DATATYPE_VALIDATOR_FACTORY = "http://apache.org/xml/properties/internal/datatype-validator-factory";
    protected static final String VALIDATION_MANAGER = "http://apache.org/xml/properties/internal/validation-manager";
    protected static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    protected static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    protected static final String ROOT_TYPE_DEF = "http://apache.org/xml/properties/validation/schema/root-type-definition";
    protected static final String ROOT_ELEMENT_DECL = "http://apache.org/xml/properties/validation/schema/root-element-declaration";
    protected static final String LOCALE = "http://apache.org/xml/properties/locale";
    protected static final String SCHEMA_DV_FACTORY = "http://apache.org/xml/properties/internal/validation/schema/dv-factory";
    private static final String XML_SECURITY_PROPERTY_MANAGER = "jdk.xml.xmlSecurityPropertyManager";
    private static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    protected static final boolean PRINT_EXCEPTION_STACK_TRACE = false;
    protected SymbolTable fSymbolTable;
    protected XMLInputSource fInputSource;
    protected ValidationManager fValidationManager;
    protected XMLVersionDetector fVersionDetector;
    protected XMLLocator fLocator;
    protected Locale fLocale;
    protected List<XMLComponent> fComponents = new ArrayList<XMLComponent>();
    protected List<XMLComponent> fXML11Components = new ArrayList<XMLComponent>();
    protected List<XMLComponent> fCommonComponents = new ArrayList<XMLComponent>();
    protected XMLDocumentHandler fDocumentHandler;
    protected XMLDTDHandler fDTDHandler;
    protected XMLDTDContentModelHandler fDTDContentModelHandler;
    protected XMLDocumentSource fLastComponent;
    protected boolean fParseInProgress = false;
    protected boolean fConfigUpdated = false;
    protected DTDDVFactory fDatatypeValidatorFactory;
    protected XMLNSDocumentScannerImpl fNamespaceScanner;
    protected XMLDocumentScannerImpl fNonNSScanner;
    protected XMLDTDValidator fDTDValidator;
    protected XMLDTDValidator fNonNSDTDValidator;
    protected XMLDTDScanner fDTDScanner;
    protected XMLDTDProcessor fDTDProcessor;
    protected DTDDVFactory fXML11DatatypeFactory = null;
    protected XML11NSDocumentScannerImpl fXML11NSDocScanner = null;
    protected XML11DocumentScannerImpl fXML11DocScanner = null;
    protected XML11NSDTDValidator fXML11NSDTDValidator = null;
    protected XML11DTDValidator fXML11DTDValidator = null;
    protected XML11DTDScannerImpl fXML11DTDScanner = null;
    protected XML11DTDProcessor fXML11DTDProcessor = null;
    protected XMLGrammarPool fGrammarPool;
    protected XMLErrorReporter fErrorReporter;
    protected XMLEntityManager fEntityManager;
    protected XMLSchemaValidator fSchemaValidator;
    protected XMLDocumentScanner fCurrentScanner;
    protected DTDDVFactory fCurrentDVFactory;
    protected XMLDTDScanner fCurrentDTDScanner;
    private boolean f11Initialized = false;
    private boolean fSymbolTableProvided = false;
    private boolean fSymbolTableJustInitialized = true;

    public XML11Configuration() {
        this(null, null, null);
    }

    public XML11Configuration(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    }

    public XML11Configuration(SymbolTable symbolTable, XMLGrammarPool grammarPool) {
        this(symbolTable, grammarPool, null);
    }

    public XML11Configuration(SymbolTable symbolTable, XMLGrammarPool grammarPool, XMLComponentManager parentSettings) {
        super(parentSettings);
        String[] recognizedFeatures = new String[]{CONTINUE_AFTER_FATAL_ERROR, LOAD_EXTERNAL_DTD, VALIDATION, NAMESPACES, NORMALIZE_DATA, SCHEMA_ELEMENT_DEFAULT, SCHEMA_AUGMENT_PSVI, GENERATE_SYNTHETIC_ANNOTATIONS, VALIDATE_ANNOTATIONS, HONOUR_ALL_SCHEMALOCATIONS, IGNORE_XSI_TYPE, ID_IDREF_CHECKING, IDENTITY_CONSTRAINT_CHECKING, UNPARSED_ENTITY_CHECKING, NAMESPACE_GROWTH, TOLERATE_DUPLICATES, USE_GRAMMAR_POOL_ONLY, XMLSCHEMA_VALIDATION, XMLSCHEMA_FULL_CHECKING, EXTERNAL_GENERAL_ENTITIES, EXTERNAL_PARAMETER_ENTITIES, "http://apache.org/xml/features/internal/parser-settings", "http://javax.xml.XMLConstants/feature/secure-processing", "http://javax.xml.XMLConstants/feature/useCatalog", "jdk.xml.resetSymbolTable", "jdk.xml.overrideDefaultParser"};
        this.addRecognizedFeatures(recognizedFeatures);
        this.fFeatures.put(VALIDATION, Boolean.FALSE);
        this.fFeatures.put(NAMESPACES, Boolean.TRUE);
        this.fFeatures.put(EXTERNAL_GENERAL_ENTITIES, Boolean.TRUE);
        this.fFeatures.put(EXTERNAL_PARAMETER_ENTITIES, Boolean.TRUE);
        this.fFeatures.put(CONTINUE_AFTER_FATAL_ERROR, Boolean.FALSE);
        this.fFeatures.put(LOAD_EXTERNAL_DTD, Boolean.TRUE);
        this.fFeatures.put(SCHEMA_ELEMENT_DEFAULT, Boolean.TRUE);
        this.fFeatures.put(NORMALIZE_DATA, Boolean.TRUE);
        this.fFeatures.put(SCHEMA_AUGMENT_PSVI, Boolean.TRUE);
        this.fFeatures.put(GENERATE_SYNTHETIC_ANNOTATIONS, Boolean.FALSE);
        this.fFeatures.put(VALIDATE_ANNOTATIONS, Boolean.FALSE);
        this.fFeatures.put(HONOUR_ALL_SCHEMALOCATIONS, Boolean.FALSE);
        this.fFeatures.put(IGNORE_XSI_TYPE, Boolean.FALSE);
        this.fFeatures.put(ID_IDREF_CHECKING, Boolean.TRUE);
        this.fFeatures.put(IDENTITY_CONSTRAINT_CHECKING, Boolean.TRUE);
        this.fFeatures.put(UNPARSED_ENTITY_CHECKING, Boolean.TRUE);
        this.fFeatures.put(NAMESPACE_GROWTH, Boolean.FALSE);
        this.fFeatures.put(TOLERATE_DUPLICATES, Boolean.FALSE);
        this.fFeatures.put(USE_GRAMMAR_POOL_ONLY, Boolean.FALSE);
        this.fFeatures.put("http://apache.org/xml/features/internal/parser-settings", Boolean.TRUE);
        this.fFeatures.put("http://javax.xml.XMLConstants/feature/secure-processing", Boolean.TRUE);
        this.fFeatures.put("http://javax.xml.XMLConstants/feature/useCatalog", JdkXmlUtils.USE_CATALOG_DEFAULT);
        this.fFeatures.put("jdk.xml.resetSymbolTable", JdkConstants.RESET_SYMBOL_TABLE_DEFAULT);
        this.fFeatures.put("jdk.xml.overrideDefaultParser", JdkConstants.OVERRIDE_PARSER_DEFAULT);
        String[] recognizedProperties = new String[]{SYMBOL_TABLE, ERROR_HANDLER, ENTITY_RESOLVER, ERROR_REPORTER, ENTITY_MANAGER, DOCUMENT_SCANNER, DTD_SCANNER, DTD_PROCESSOR, DTD_VALIDATOR, DATATYPE_VALIDATOR_FACTORY, VALIDATION_MANAGER, SCHEMA_VALIDATOR, XML_STRING, XMLGRAMMAR_POOL, JAXP_SCHEMA_SOURCE, JAXP_SCHEMA_LANGUAGE, SCHEMA_LOCATION, SCHEMA_NONS_LOCATION, ROOT_TYPE_DEF, ROOT_ELEMENT_DECL, LOCALE, SCHEMA_DV_FACTORY, SECURITY_MANAGER, XML_SECURITY_PROPERTY_MANAGER, JdkXmlUtils.CATALOG_DEFER, JdkXmlUtils.CATALOG_FILES, JdkXmlUtils.CATALOG_PREFER, JdkXmlUtils.CATALOG_RESOLVE, "jdk.xml.cdataChunkSize"};
        this.addRecognizedProperties(recognizedProperties);
        this.fSymbolTableProvided = symbolTable != null;
        this.fSymbolTable = !this.fSymbolTableProvided ? new SymbolTable() : symbolTable;
        this.fProperties.put(SYMBOL_TABLE, this.fSymbolTable);
        this.fGrammarPool = grammarPool;
        if (this.fGrammarPool != null) {
            this.fProperties.put(XMLGRAMMAR_POOL, this.fGrammarPool);
        }
        this.fEntityManager = new XMLEntityManager();
        this.fProperties.put(ENTITY_MANAGER, this.fEntityManager);
        this.addCommonComponent(this.fEntityManager);
        this.fErrorReporter = new XMLErrorReporter();
        this.fErrorReporter.setDocumentLocator(this.fEntityManager.getEntityScanner());
        this.fProperties.put(ERROR_REPORTER, this.fErrorReporter);
        this.addCommonComponent(this.fErrorReporter);
        this.fNamespaceScanner = new XMLNSDocumentScannerImpl();
        this.fProperties.put(DOCUMENT_SCANNER, this.fNamespaceScanner);
        this.addComponent(this.fNamespaceScanner);
        this.fDTDScanner = new XMLDTDScannerImpl();
        this.fProperties.put(DTD_SCANNER, this.fDTDScanner);
        this.addComponent((XMLComponent)((Object)this.fDTDScanner));
        this.fDTDProcessor = new XMLDTDProcessor();
        this.fProperties.put(DTD_PROCESSOR, this.fDTDProcessor);
        this.addComponent(this.fDTDProcessor);
        this.fDTDValidator = new XMLNSDTDValidator();
        this.fProperties.put(DTD_VALIDATOR, this.fDTDValidator);
        this.addComponent(this.fDTDValidator);
        this.fDatatypeValidatorFactory = DTDDVFactory.getInstance();
        this.fProperties.put(DATATYPE_VALIDATOR_FACTORY, this.fDatatypeValidatorFactory);
        this.fValidationManager = new ValidationManager();
        this.fProperties.put(VALIDATION_MANAGER, this.fValidationManager);
        this.fVersionDetector = new XMLVersionDetector();
        if (this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210") == null) {
            XMLMessageFormatter xmft = new XMLMessageFormatter();
            this.fErrorReporter.putMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210", xmft);
            this.fErrorReporter.putMessageFormatter("http://www.w3.org/TR/1999/REC-xml-names-19990114", xmft);
        }
        try {
            this.setLocale(Locale.getDefault());
        }
        catch (XNIException xNIException) {
            // empty catch block
        }
        for (CatalogFeatures.Feature f : CatalogFeatures.Feature.values()) {
            this.fProperties.put(f.getPropertyName(), null);
        }
        this.setProperty("jdk.xml.cdataChunkSize", JdkConstants.CDATA_CHUNK_SIZE_DEFAULT);
        this.fConfigUpdated = false;
    }

    @Override
    public void setInputSource(XMLInputSource inputSource) throws XMLConfigurationException, IOException {
        this.fInputSource = inputSource;
    }

    @Override
    public void setLocale(Locale locale) throws XNIException {
        this.fLocale = locale;
        this.fErrorReporter.setLocale(locale);
    }

    @Override
    public void setDocumentHandler(XMLDocumentHandler documentHandler) {
        this.fDocumentHandler = documentHandler;
        if (this.fLastComponent != null) {
            this.fLastComponent.setDocumentHandler(this.fDocumentHandler);
            if (this.fDocumentHandler != null) {
                this.fDocumentHandler.setDocumentSource(this.fLastComponent);
            }
        }
    }

    @Override
    public XMLDocumentHandler getDocumentHandler() {
        return this.fDocumentHandler;
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
    public void setDTDContentModelHandler(XMLDTDContentModelHandler handler) {
        this.fDTDContentModelHandler = handler;
    }

    @Override
    public XMLDTDContentModelHandler getDTDContentModelHandler() {
        return this.fDTDContentModelHandler;
    }

    @Override
    public void setEntityResolver(XMLEntityResolver resolver) {
        this.fProperties.put(ENTITY_RESOLVER, resolver);
    }

    @Override
    public XMLEntityResolver getEntityResolver() {
        return (XMLEntityResolver)this.fProperties.get(ENTITY_RESOLVER);
    }

    @Override
    public void setErrorHandler(XMLErrorHandler errorHandler) {
        this.fProperties.put(ERROR_HANDLER, errorHandler);
    }

    @Override
    public XMLErrorHandler getErrorHandler() {
        return (XMLErrorHandler)this.fProperties.get(ERROR_HANDLER);
    }

    @Override
    public void cleanup() {
        this.fEntityManager.closeReaders();
    }

    @Override
    public void parse(XMLInputSource source) throws XNIException, IOException {
        if (this.fParseInProgress) {
            throw new XNIException("FWK005 parse may not be called while parsing.");
        }
        this.fParseInProgress = true;
        try {
            this.setInputSource(source);
            this.parse(true);
        }
        catch (XNIException ex) {
            throw ex;
        }
        catch (IOException ex) {
            throw ex;
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XNIException(ex);
        }
        finally {
            this.fParseInProgress = false;
            this.cleanup();
        }
    }

    @Override
    public boolean parse(boolean complete) throws XNIException, IOException {
        if (this.fInputSource != null) {
            try {
                this.fValidationManager.reset();
                this.fVersionDetector.reset(this);
                this.fConfigUpdated = true;
                this.resetSymbolTable();
                this.resetCommon();
                short version = this.fVersionDetector.determineDocVersion(this.fInputSource);
                if (version == 2) {
                    this.initXML11Components();
                    this.configureXML11Pipeline();
                    this.resetXML11();
                } else {
                    this.configurePipeline();
                    this.reset();
                }
                this.fConfigUpdated = false;
                this.fVersionDetector.startDocumentParsing((XMLEntityHandler)((Object)this.fCurrentScanner), version);
                this.fInputSource = null;
            }
            catch (IOException | RuntimeException ex) {
                throw ex;
            }
            catch (Exception ex) {
                throw new XNIException(ex);
            }
        }
        try {
            return this.fCurrentScanner.scanDocument(complete);
        }
        catch (IOException | RuntimeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new XNIException(ex);
        }
    }

    @Override
    public FeatureState getFeatureState(String featureId) throws XMLConfigurationException {
        if (featureId.equals("http://apache.org/xml/features/internal/parser-settings")) {
            return FeatureState.is(this.fConfigUpdated);
        }
        return super.getFeatureState(featureId);
    }

    @Override
    public void setFeature(String featureId, boolean state) throws XMLConfigurationException {
        this.fConfigUpdated = true;
        for (XMLComponent c : this.fComponents) {
            c.setFeature(featureId, state);
        }
        for (XMLComponent c : this.fCommonComponents) {
            c.setFeature(featureId, state);
        }
        for (XMLComponent c : this.fXML11Components) {
            try {
                c.setFeature(featureId, state);
            }
            catch (Exception exception) {}
        }
        super.setFeature(featureId, state);
    }

    @Override
    public PropertyState getPropertyState(String propertyId) throws XMLConfigurationException {
        if (LOCALE.equals(propertyId)) {
            return PropertyState.is(this.getLocale());
        }
        return super.getPropertyState(propertyId);
    }

    @Override
    public void setProperty(String propertyId, Object value) throws XMLConfigurationException {
        this.fConfigUpdated = true;
        if (LOCALE.equals(propertyId)) {
            this.setLocale((Locale)value);
        }
        for (XMLComponent c : this.fComponents) {
            c.setProperty(propertyId, value);
        }
        for (XMLComponent c : this.fCommonComponents) {
            c.setProperty(propertyId, value);
        }
        for (XMLComponent c : this.fXML11Components) {
            try {
                c.setProperty(propertyId, value);
            }
            catch (Exception exception) {}
        }
        super.setProperty(propertyId, value);
    }

    @Override
    public Locale getLocale() {
        return this.fLocale;
    }

    protected void reset() throws XNIException {
        int count = this.fComponents.size();
        for (int i = 0; i < count; ++i) {
            XMLComponent c = this.fComponents.get(i);
            c.reset(this);
        }
    }

    protected void resetCommon() throws XNIException {
        int count = this.fCommonComponents.size();
        for (int i = 0; i < count; ++i) {
            XMLComponent c = this.fCommonComponents.get(i);
            c.reset(this);
        }
    }

    protected void resetXML11() throws XNIException {
        int count = this.fXML11Components.size();
        for (int i = 0; i < count; ++i) {
            XMLComponent c = this.fXML11Components.get(i);
            c.reset(this);
        }
    }

    protected void configureXML11Pipeline() {
        if (this.fCurrentDVFactory != this.fXML11DatatypeFactory) {
            this.fCurrentDVFactory = this.fXML11DatatypeFactory;
            this.setProperty(DATATYPE_VALIDATOR_FACTORY, this.fCurrentDVFactory);
        }
        if (this.fCurrentDTDScanner != this.fXML11DTDScanner) {
            this.fCurrentDTDScanner = this.fXML11DTDScanner;
            this.setProperty(DTD_SCANNER, this.fCurrentDTDScanner);
            this.setProperty(DTD_PROCESSOR, this.fXML11DTDProcessor);
        }
        this.fXML11DTDScanner.setDTDHandler(this.fXML11DTDProcessor);
        this.fXML11DTDProcessor.setDTDSource(this.fXML11DTDScanner);
        this.fXML11DTDProcessor.setDTDHandler(this.fDTDHandler);
        if (this.fDTDHandler != null) {
            this.fDTDHandler.setDTDSource(this.fXML11DTDProcessor);
        }
        this.fXML11DTDScanner.setDTDContentModelHandler(this.fXML11DTDProcessor);
        this.fXML11DTDProcessor.setDTDContentModelSource(this.fXML11DTDScanner);
        this.fXML11DTDProcessor.setDTDContentModelHandler(this.fDTDContentModelHandler);
        if (this.fDTDContentModelHandler != null) {
            this.fDTDContentModelHandler.setDTDContentModelSource(this.fXML11DTDProcessor);
        }
        if (this.fFeatures.get(NAMESPACES) == Boolean.TRUE) {
            if (this.fCurrentScanner != this.fXML11NSDocScanner) {
                this.fCurrentScanner = this.fXML11NSDocScanner;
                this.setProperty(DOCUMENT_SCANNER, this.fXML11NSDocScanner);
                this.setProperty(DTD_VALIDATOR, this.fXML11NSDTDValidator);
            }
            this.fXML11NSDocScanner.setDTDValidator(this.fXML11NSDTDValidator);
            this.fXML11NSDocScanner.setDocumentHandler(this.fXML11NSDTDValidator);
            this.fXML11NSDTDValidator.setDocumentSource(this.fXML11NSDocScanner);
            this.fXML11NSDTDValidator.setDocumentHandler(this.fDocumentHandler);
            if (this.fDocumentHandler != null) {
                this.fDocumentHandler.setDocumentSource(this.fXML11NSDTDValidator);
            }
            this.fLastComponent = this.fXML11NSDTDValidator;
        } else {
            if (this.fXML11DocScanner == null) {
                this.fXML11DocScanner = new XML11DocumentScannerImpl();
                this.addXML11Component(this.fXML11DocScanner);
                this.fXML11DTDValidator = new XML11DTDValidator();
                this.addXML11Component(this.fXML11DTDValidator);
            }
            if (this.fCurrentScanner != this.fXML11DocScanner) {
                this.fCurrentScanner = this.fXML11DocScanner;
                this.setProperty(DOCUMENT_SCANNER, this.fXML11DocScanner);
                this.setProperty(DTD_VALIDATOR, this.fXML11DTDValidator);
            }
            this.fXML11DocScanner.setDocumentHandler(this.fXML11DTDValidator);
            this.fXML11DTDValidator.setDocumentSource(this.fXML11DocScanner);
            this.fXML11DTDValidator.setDocumentHandler(this.fDocumentHandler);
            if (this.fDocumentHandler != null) {
                this.fDocumentHandler.setDocumentSource(this.fXML11DTDValidator);
            }
            this.fLastComponent = this.fXML11DTDValidator;
        }
        if (this.fFeatures.get(XMLSCHEMA_VALIDATION) == Boolean.TRUE) {
            if (this.fSchemaValidator == null) {
                this.fSchemaValidator = new XMLSchemaValidator();
                this.setProperty(SCHEMA_VALIDATOR, this.fSchemaValidator);
                this.addCommonComponent(this.fSchemaValidator);
                this.fSchemaValidator.reset(this);
                if (this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/xml-schema-1") == null) {
                    XSMessageFormatter xmft = new XSMessageFormatter();
                    this.fErrorReporter.putMessageFormatter("http://www.w3.org/TR/xml-schema-1", xmft);
                }
            }
            this.fLastComponent.setDocumentHandler(this.fSchemaValidator);
            this.fSchemaValidator.setDocumentSource(this.fLastComponent);
            this.fSchemaValidator.setDocumentHandler(this.fDocumentHandler);
            if (this.fDocumentHandler != null) {
                this.fDocumentHandler.setDocumentSource(this.fSchemaValidator);
            }
            this.fLastComponent = this.fSchemaValidator;
        }
    }

    protected void configurePipeline() {
        if (this.fCurrentDVFactory != this.fDatatypeValidatorFactory) {
            this.fCurrentDVFactory = this.fDatatypeValidatorFactory;
            this.setProperty(DATATYPE_VALIDATOR_FACTORY, this.fCurrentDVFactory);
        }
        if (this.fCurrentDTDScanner != this.fDTDScanner) {
            this.fCurrentDTDScanner = this.fDTDScanner;
            this.setProperty(DTD_SCANNER, this.fCurrentDTDScanner);
            this.setProperty(DTD_PROCESSOR, this.fDTDProcessor);
        }
        this.fDTDScanner.setDTDHandler(this.fDTDProcessor);
        this.fDTDProcessor.setDTDSource(this.fDTDScanner);
        this.fDTDProcessor.setDTDHandler(this.fDTDHandler);
        if (this.fDTDHandler != null) {
            this.fDTDHandler.setDTDSource(this.fDTDProcessor);
        }
        this.fDTDScanner.setDTDContentModelHandler(this.fDTDProcessor);
        this.fDTDProcessor.setDTDContentModelSource(this.fDTDScanner);
        this.fDTDProcessor.setDTDContentModelHandler(this.fDTDContentModelHandler);
        if (this.fDTDContentModelHandler != null) {
            this.fDTDContentModelHandler.setDTDContentModelSource(this.fDTDProcessor);
        }
        if (this.fFeatures.get(NAMESPACES) == Boolean.TRUE) {
            if (this.fCurrentScanner != this.fNamespaceScanner) {
                this.fCurrentScanner = this.fNamespaceScanner;
                this.setProperty(DOCUMENT_SCANNER, this.fNamespaceScanner);
                this.setProperty(DTD_VALIDATOR, this.fDTDValidator);
            }
            this.fNamespaceScanner.setDTDValidator(this.fDTDValidator);
            this.fNamespaceScanner.setDocumentHandler(this.fDTDValidator);
            this.fDTDValidator.setDocumentSource(this.fNamespaceScanner);
            this.fDTDValidator.setDocumentHandler(this.fDocumentHandler);
            if (this.fDocumentHandler != null) {
                this.fDocumentHandler.setDocumentSource(this.fDTDValidator);
            }
            this.fLastComponent = this.fDTDValidator;
        } else {
            if (this.fNonNSScanner == null) {
                this.fNonNSScanner = new XMLDocumentScannerImpl();
                this.fNonNSDTDValidator = new XMLDTDValidator();
                this.addComponent(this.fNonNSScanner);
                this.addComponent(this.fNonNSDTDValidator);
            }
            if (this.fCurrentScanner != this.fNonNSScanner) {
                this.fCurrentScanner = this.fNonNSScanner;
                this.setProperty(DOCUMENT_SCANNER, this.fNonNSScanner);
                this.setProperty(DTD_VALIDATOR, this.fNonNSDTDValidator);
            }
            this.fNonNSScanner.setDocumentHandler(this.fNonNSDTDValidator);
            this.fNonNSDTDValidator.setDocumentSource(this.fNonNSScanner);
            this.fNonNSDTDValidator.setDocumentHandler(this.fDocumentHandler);
            if (this.fDocumentHandler != null) {
                this.fDocumentHandler.setDocumentSource(this.fNonNSDTDValidator);
            }
            this.fLastComponent = this.fNonNSDTDValidator;
        }
        if (this.fFeatures.get(XMLSCHEMA_VALIDATION) == Boolean.TRUE) {
            if (this.fSchemaValidator == null) {
                this.fSchemaValidator = new XMLSchemaValidator();
                this.setProperty(SCHEMA_VALIDATOR, this.fSchemaValidator);
                this.addCommonComponent(this.fSchemaValidator);
                this.fSchemaValidator.reset(this);
                if (this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/xml-schema-1") == null) {
                    XSMessageFormatter xmft = new XSMessageFormatter();
                    this.fErrorReporter.putMessageFormatter("http://www.w3.org/TR/xml-schema-1", xmft);
                }
            }
            this.fLastComponent.setDocumentHandler(this.fSchemaValidator);
            this.fSchemaValidator.setDocumentSource(this.fLastComponent);
            this.fSchemaValidator.setDocumentHandler(this.fDocumentHandler);
            if (this.fDocumentHandler != null) {
                this.fDocumentHandler.setDocumentSource(this.fSchemaValidator);
            }
            this.fLastComponent = this.fSchemaValidator;
        }
    }

    @Override
    protected FeatureState checkFeature(String featureId) throws XMLConfigurationException {
        if (featureId.startsWith("http://apache.org/xml/features/")) {
            int suffixLength = featureId.length() - "http://apache.org/xml/features/".length();
            if (suffixLength == "validation/dynamic".length() && featureId.endsWith("validation/dynamic")) {
                return FeatureState.RECOGNIZED;
            }
            if (suffixLength == "validation/default-attribute-values".length() && featureId.endsWith("validation/default-attribute-values")) {
                return FeatureState.NOT_SUPPORTED;
            }
            if (suffixLength == "validation/validate-content-models".length() && featureId.endsWith("validation/validate-content-models")) {
                return FeatureState.NOT_SUPPORTED;
            }
            if (suffixLength == "nonvalidating/load-dtd-grammar".length() && featureId.endsWith("nonvalidating/load-dtd-grammar")) {
                return FeatureState.RECOGNIZED;
            }
            if (suffixLength == "nonvalidating/load-external-dtd".length() && featureId.endsWith("nonvalidating/load-external-dtd")) {
                return FeatureState.RECOGNIZED;
            }
            if (suffixLength == "validation/validate-datatypes".length() && featureId.endsWith("validation/validate-datatypes")) {
                return FeatureState.NOT_SUPPORTED;
            }
            if (suffixLength == "validation/schema".length() && featureId.endsWith("validation/schema")) {
                return FeatureState.RECOGNIZED;
            }
            if (suffixLength == "validation/schema-full-checking".length() && featureId.endsWith("validation/schema-full-checking")) {
                return FeatureState.RECOGNIZED;
            }
            if (suffixLength == "validation/schema/normalized-value".length() && featureId.endsWith("validation/schema/normalized-value")) {
                return FeatureState.RECOGNIZED;
            }
            if (suffixLength == "validation/schema/element-default".length() && featureId.endsWith("validation/schema/element-default")) {
                return FeatureState.RECOGNIZED;
            }
            if (suffixLength == "internal/parser-settings".length() && featureId.endsWith("internal/parser-settings")) {
                return FeatureState.NOT_SUPPORTED;
            }
        }
        return super.checkFeature(featureId);
    }

    @Override
    protected PropertyState checkProperty(String propertyId) throws XMLConfigurationException {
        int suffixLength;
        if (propertyId.startsWith("http://apache.org/xml/properties/")) {
            suffixLength = propertyId.length() - "http://apache.org/xml/properties/".length();
            if (suffixLength == "internal/dtd-scanner".length() && propertyId.endsWith("internal/dtd-scanner")) {
                return PropertyState.RECOGNIZED;
            }
            if (suffixLength == "schema/external-schemaLocation".length() && propertyId.endsWith("schema/external-schemaLocation")) {
                return PropertyState.RECOGNIZED;
            }
            if (suffixLength == "schema/external-noNamespaceSchemaLocation".length() && propertyId.endsWith("schema/external-noNamespaceSchemaLocation")) {
                return PropertyState.RECOGNIZED;
            }
        }
        if (propertyId.startsWith("http://java.sun.com/xml/jaxp/properties/") && (suffixLength = propertyId.length() - "http://java.sun.com/xml/jaxp/properties/".length()) == "schemaSource".length() && propertyId.endsWith("schemaSource")) {
            return PropertyState.RECOGNIZED;
        }
        if (propertyId.startsWith("http://xml.org/sax/properties/") && (suffixLength = propertyId.length() - "http://xml.org/sax/properties/".length()) == "xml-string".length() && propertyId.endsWith("xml-string")) {
            return PropertyState.NOT_SUPPORTED;
        }
        return super.checkProperty(propertyId);
    }

    protected void addComponent(XMLComponent component) {
        if (this.fComponents.contains(component)) {
            return;
        }
        this.fComponents.add(component);
        this.addRecognizedParamsAndSetDefaults(component);
    }

    protected void addCommonComponent(XMLComponent component) {
        if (this.fCommonComponents.contains(component)) {
            return;
        }
        this.fCommonComponents.add(component);
        this.addRecognizedParamsAndSetDefaults(component);
    }

    protected void addXML11Component(XMLComponent component) {
        if (this.fXML11Components.contains(component)) {
            return;
        }
        this.fXML11Components.add(component);
        this.addRecognizedParamsAndSetDefaults(component);
    }

    protected void addRecognizedParamsAndSetDefaults(XMLComponent component) {
        int i;
        String[] recognizedFeatures = component.getRecognizedFeatures();
        this.addRecognizedFeatures(recognizedFeatures);
        String[] recognizedProperties = component.getRecognizedProperties();
        this.addRecognizedProperties(recognizedProperties);
        if (recognizedFeatures != null) {
            for (i = 0; i < recognizedFeatures.length; ++i) {
                String featureId = recognizedFeatures[i];
                Boolean state = component.getFeatureDefault(featureId);
                if (state == null || this.fFeatures.containsKey(featureId)) continue;
                this.fFeatures.put(featureId, state);
                this.fConfigUpdated = true;
            }
        }
        if (recognizedProperties != null) {
            for (i = 0; i < recognizedProperties.length; ++i) {
                String propertyId = recognizedProperties[i];
                Object value = component.getPropertyDefault(propertyId);
                if (value == null || this.fProperties.containsKey(propertyId)) continue;
                this.fProperties.put(propertyId, value);
                this.fConfigUpdated = true;
            }
        }
    }

    private void initXML11Components() {
        if (!this.f11Initialized) {
            this.fXML11DatatypeFactory = DTDDVFactory.getInstance(XML11_DATATYPE_VALIDATOR_FACTORY);
            this.fXML11DTDScanner = new XML11DTDScannerImpl();
            this.addXML11Component(this.fXML11DTDScanner);
            this.fXML11DTDProcessor = new XML11DTDProcessor();
            this.addXML11Component(this.fXML11DTDProcessor);
            this.fXML11NSDocScanner = new XML11NSDocumentScannerImpl();
            this.addXML11Component(this.fXML11NSDocScanner);
            this.fXML11NSDTDValidator = new XML11NSDTDValidator();
            this.addXML11Component(this.fXML11NSDTDValidator);
            this.f11Initialized = true;
        }
    }

    private void resetSymbolTable() {
        if (((Boolean)this.fFeatures.get("jdk.xml.resetSymbolTable")).booleanValue() && !this.fSymbolTableProvided) {
            if (this.fSymbolTableJustInitialized) {
                this.fSymbolTableJustInitialized = false;
            } else {
                this.fSymbolTable = new SymbolTable();
                this.fProperties.put(SYMBOL_TABLE, this.fSymbolTable);
            }
        }
    }

    FeatureState getFeatureState0(String featureId) throws XMLConfigurationException {
        return super.getFeatureState(featureId);
    }
}

