/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.jaxp;

import com.sun.org.apache.xerces.internal.dom.DOMImplementationImpl;
import com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;
import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator;
import com.sun.org.apache.xerces.internal.jaxp.DefaultValidationErrorHandler;
import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import com.sun.org.apache.xerces.internal.jaxp.JAXPConstants;
import com.sun.org.apache.xerces.internal.jaxp.JAXPValidatorComponent;
import com.sun.org.apache.xerces.internal.jaxp.SchemaValidatorConfiguration;
import com.sun.org.apache.xerces.internal.jaxp.UnparsedEntityHandler;
import com.sun.org.apache.xerces.internal.jaxp.validation.XSGrammarPoolContainer;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter;
import com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;
import com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;
import java.io.IOException;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.validation.Schema;
import jdk.xml.internal.JdkProperty;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public class DocumentBuilderImpl
extends DocumentBuilder
implements JAXPConstants {
    private static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";
    private static final String INCLUDE_IGNORABLE_WHITESPACE = "http://apache.org/xml/features/dom/include-ignorable-whitespace";
    private static final String CREATE_ENTITY_REF_NODES_FEATURE = "http://apache.org/xml/features/dom/create-entity-ref-nodes";
    private static final String INCLUDE_COMMENTS_FEATURE = "http://apache.org/xml/features/include-comments";
    private static final String CREATE_CDATA_NODES_FEATURE = "http://apache.org/xml/features/create-cdata-nodes";
    private static final String XINCLUDE_FEATURE = "http://apache.org/xml/features/xinclude";
    private static final String XMLSCHEMA_VALIDATION_FEATURE = "http://apache.org/xml/features/validation/schema";
    private static final String VALIDATION_FEATURE = "http://xml.org/sax/features/validation";
    private static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    private static final String XML_SECURITY_PROPERTY_MANAGER = "jdk.xml.xmlSecurityPropertyManager";
    public static final String ACCESS_EXTERNAL_DTD = "http://javax.xml.XMLConstants/property/accessExternalDTD";
    public static final String ACCESS_EXTERNAL_SCHEMA = "http://javax.xml.XMLConstants/property/accessExternalSchema";
    private final DOMParser domParser = new DOMParser();
    private final Schema grammar;
    private final XMLComponent fSchemaValidator;
    private final XMLComponentManager fSchemaValidatorComponentManager;
    private final ValidationManager fSchemaValidationManager;
    private final UnparsedEntityHandler fUnparsedEntityHandler;
    private final ErrorHandler fInitErrorHandler;
    private final EntityResolver fInitEntityResolver;
    private XMLSecurityManager fSecurityManager;
    private XMLSecurityPropertyManager fSecurityPropertyMgr;

    DocumentBuilderImpl(DocumentBuilderFactoryImpl dbf, Map<String, Object> dbfAttrs, Map<String, Boolean> features) throws SAXNotRecognizedException, SAXNotSupportedException {
        this(dbf, dbfAttrs, features, false);
    }

    DocumentBuilderImpl(DocumentBuilderFactoryImpl dbf, Map<String, Object> dbfAttrs, Map<String, Boolean> features, boolean secureProcessing) throws SAXNotRecognizedException, SAXNotSupportedException {
        Boolean temp;
        if (dbf.isValidating()) {
            this.fInitErrorHandler = new DefaultValidationErrorHandler(this.domParser.getXMLParserConfiguration().getLocale());
            this.setErrorHandler(this.fInitErrorHandler);
        } else {
            this.fInitErrorHandler = this.domParser.getErrorHandler();
        }
        this.domParser.setFeature(VALIDATION_FEATURE, dbf.isValidating());
        this.domParser.setFeature(NAMESPACES_FEATURE, dbf.isNamespaceAware());
        this.domParser.setFeature(INCLUDE_IGNORABLE_WHITESPACE, !dbf.isIgnoringElementContentWhitespace());
        this.domParser.setFeature(CREATE_ENTITY_REF_NODES_FEATURE, !dbf.isExpandEntityReferences());
        this.domParser.setFeature(INCLUDE_COMMENTS_FEATURE, !dbf.isIgnoringComments());
        this.domParser.setFeature(CREATE_CDATA_NODES_FEATURE, !dbf.isCoalescing());
        if (dbf.isXIncludeAware()) {
            this.domParser.setFeature(XINCLUDE_FEATURE, true);
        }
        this.fSecurityPropertyMgr = dbf.fSecurityPropertyMgr;
        this.domParser.setProperty(XML_SECURITY_PROPERTY_MANAGER, this.fSecurityPropertyMgr);
        this.fSecurityManager = new XMLSecurityManager(secureProcessing);
        this.domParser.setProperty(SECURITY_MANAGER, this.fSecurityManager);
        if (secureProcessing && features != null && (temp = features.get("http://javax.xml.XMLConstants/feature/secure-processing")) != null && temp.booleanValue()) {
            this.fSecurityPropertyMgr.setValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_DTD, XMLSecurityPropertyManager.State.FSP, "");
            this.fSecurityPropertyMgr.setValue(XMLSecurityPropertyManager.Property.ACCESS_EXTERNAL_SCHEMA, XMLSecurityPropertyManager.State.FSP, "");
        }
        this.grammar = dbf.getSchema();
        if (this.grammar != null) {
            XMLParserConfiguration config = this.domParser.getXMLParserConfiguration();
            XMLDocumentFilter validatorComponent = null;
            if (this.grammar instanceof XSGrammarPoolContainer) {
                validatorComponent = new XMLSchemaValidator();
                this.fSchemaValidationManager = new ValidationManager();
                this.fUnparsedEntityHandler = new UnparsedEntityHandler(this.fSchemaValidationManager);
                config.setDTDHandler(this.fUnparsedEntityHandler);
                this.fUnparsedEntityHandler.setDTDHandler(this.domParser);
                this.domParser.setDTDSource(this.fUnparsedEntityHandler);
                this.fSchemaValidatorComponentManager = new SchemaValidatorConfiguration(config, (XSGrammarPoolContainer)((Object)this.grammar), this.fSchemaValidationManager);
            } else {
                validatorComponent = new JAXPValidatorComponent(this.grammar.newValidatorHandler());
                this.fSchemaValidationManager = null;
                this.fUnparsedEntityHandler = null;
                this.fSchemaValidatorComponentManager = config;
            }
            config.addRecognizedFeatures(validatorComponent.getRecognizedFeatures());
            config.addRecognizedProperties(validatorComponent.getRecognizedProperties());
            this.setFeatures(features);
            config.setDocumentHandler(validatorComponent);
            ((XMLDocumentSource)validatorComponent).setDocumentHandler(this.domParser);
            this.domParser.setDocumentSource(validatorComponent);
            this.fSchemaValidator = validatorComponent;
        } else {
            this.fSchemaValidationManager = null;
            this.fUnparsedEntityHandler = null;
            this.fSchemaValidatorComponentManager = null;
            this.fSchemaValidator = null;
            this.setFeatures(features);
        }
        this.setDocumentBuilderFactoryAttributes(dbfAttrs);
        this.fInitEntityResolver = this.domParser.getEntityResolver();
    }

    private void setFeatures(Map<String, Boolean> features) throws SAXNotSupportedException, SAXNotRecognizedException {
        if (features != null) {
            for (Map.Entry<String, Boolean> entry : features.entrySet()) {
                this.domParser.setFeature(entry.getKey(), entry.getValue());
            }
        }
    }

    private void setDocumentBuilderFactoryAttributes(Map<String, Object> dbfAttrs) throws SAXNotSupportedException, SAXNotRecognizedException {
        if (dbfAttrs == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : dbfAttrs.entrySet()) {
            String name = entry.getKey();
            Object val = entry.getValue();
            if (val instanceof Boolean) {
                this.domParser.setFeature(name, (Boolean)val);
                continue;
            }
            if ("http://java.sun.com/xml/jaxp/properties/schemaLanguage".equals(name)) {
                if (!"http://www.w3.org/2001/XMLSchema".equals(val) || !this.isValidating()) continue;
                this.domParser.setFeature(XMLSCHEMA_VALIDATION_FEATURE, true);
                this.domParser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
                continue;
            }
            if ("http://java.sun.com/xml/jaxp/properties/schemaSource".equals(name)) {
                if (!this.isValidating()) continue;
                String value = (String)dbfAttrs.get("http://java.sun.com/xml/jaxp/properties/schemaLanguage");
                if (value != null && "http://www.w3.org/2001/XMLSchema".equals(value)) {
                    this.domParser.setProperty(name, val);
                    continue;
                }
                throw new IllegalArgumentException(DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "jaxp-order-not-supported", new Object[]{"http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://java.sun.com/xml/jaxp/properties/schemaSource"}));
            }
            if (this.fSecurityManager != null && this.fSecurityManager.setLimit(name, JdkProperty.State.APIPROPERTY, val) || this.fSecurityPropertyMgr != null && this.fSecurityPropertyMgr.setValue(name, XMLSecurityPropertyManager.State.APIPROPERTY, val)) continue;
            this.domParser.setProperty(name, val);
        }
    }

    @Override
    public Document newDocument() {
        return new DocumentImpl();
    }

    @Override
    public DOMImplementation getDOMImplementation() {
        return DOMImplementationImpl.getDOMImplementation();
    }

    @Override
    public Document parse(InputSource is) throws SAXException, IOException {
        if (is == null) {
            throw new IllegalArgumentException(DOMMessageFormatter.formatMessage("http://www.w3.org/dom/DOMTR", "jaxp-null-input-source", null));
        }
        if (this.fSchemaValidator != null) {
            if (this.fSchemaValidationManager != null) {
                this.fSchemaValidationManager.reset();
                this.fUnparsedEntityHandler.reset();
            }
            this.resetSchemaValidator();
        }
        this.domParser.parse(is);
        Document doc = this.domParser.getDocument();
        this.domParser.dropDocumentReferences();
        return doc;
    }

    @Override
    public boolean isNamespaceAware() {
        try {
            return this.domParser.getFeature(NAMESPACES_FEATURE);
        }
        catch (SAXException x) {
            throw new IllegalStateException(x.getMessage());
        }
    }

    @Override
    public boolean isValidating() {
        try {
            return this.domParser.getFeature(VALIDATION_FEATURE);
        }
        catch (SAXException x) {
            throw new IllegalStateException(x.getMessage());
        }
    }

    @Override
    public boolean isXIncludeAware() {
        try {
            return this.domParser.getFeature(XINCLUDE_FEATURE);
        }
        catch (SAXException exc) {
            return false;
        }
    }

    @Override
    public void setEntityResolver(EntityResolver er) {
        this.domParser.setEntityResolver(er);
    }

    @Override
    public void setErrorHandler(ErrorHandler eh) {
        this.domParser.setErrorHandler(eh);
    }

    @Override
    public Schema getSchema() {
        return this.grammar;
    }

    @Override
    public void reset() {
        if (this.domParser.getErrorHandler() != this.fInitErrorHandler) {
            this.domParser.setErrorHandler(this.fInitErrorHandler);
        }
        if (this.domParser.getEntityResolver() != this.fInitEntityResolver) {
            this.domParser.setEntityResolver(this.fInitEntityResolver);
        }
    }

    DOMParser getDOMParser() {
        return this.domParser;
    }

    private void resetSchemaValidator() throws SAXException {
        try {
            this.fSchemaValidator.reset(this.fSchemaValidatorComponentManager);
        }
        catch (XMLConfigurationException e) {
            throw new SAXException(e);
        }
    }
}

