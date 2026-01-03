/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.parsers;

import com.sun.org.apache.xerces.internal.parsers.AbstractDOMParser;
import com.sun.org.apache.xerces.internal.parsers.XIncludeAwareParserConfiguration;
import com.sun.org.apache.xerces.internal.util.EntityResolver2Wrapper;
import com.sun.org.apache.xerces.internal.util.EntityResolverWrapper;
import com.sun.org.apache.xerces.internal.util.ErrorHandlerWrapper;
import com.sun.org.apache.xerces.internal.util.SAXMessageFormatter;
import com.sun.org.apache.xerces.internal.util.Status;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;
import java.io.CharConversionException;
import java.io.IOException;
import jdk.xml.internal.JdkProperty;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.helpers.LocatorImpl;

public class DOMParser
extends AbstractDOMParser {
    protected static final String USE_ENTITY_RESOLVER2 = "http://xml.org/sax/features/use-entity-resolver2";
    protected static final String REPORT_WHITESPACE = "http://java.sun.com/xml/schema/features/report-ignored-element-content-whitespace";
    private static final String XML_SECURITY_PROPERTY_MANAGER = "jdk.xml.xmlSecurityPropertyManager";
    private static final String[] RECOGNIZED_FEATURES = new String[]{"http://java.sun.com/xml/schema/features/report-ignored-element-content-whitespace"};
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    protected static final String XMLGRAMMAR_POOL = "http://apache.org/xml/properties/internal/grammar-pool";
    private static final String[] RECOGNIZED_PROPERTIES = new String[]{"http://apache.org/xml/properties/internal/symbol-table", "http://apache.org/xml/properties/internal/grammar-pool"};
    protected boolean fUseEntityResolver2 = true;

    public DOMParser(XMLParserConfiguration config) {
        super(config);
    }

    public DOMParser() {
        this(null, null);
    }

    public DOMParser(SymbolTable symbolTable) {
        this(symbolTable, null);
    }

    public DOMParser(SymbolTable symbolTable, XMLGrammarPool grammarPool) {
        super(new XIncludeAwareParserConfiguration());
        this.fConfiguration.addRecognizedProperties(RECOGNIZED_PROPERTIES);
        if (symbolTable != null) {
            this.fConfiguration.setProperty(SYMBOL_TABLE, symbolTable);
        }
        if (grammarPool != null) {
            this.fConfiguration.setProperty(XMLGRAMMAR_POOL, grammarPool);
        }
        this.fConfiguration.addRecognizedFeatures(RECOGNIZED_FEATURES);
    }

    public void parse(String systemId) throws SAXException, IOException {
        XMLInputSource source = new XMLInputSource(null, systemId, null, false);
        try {
            this.parse(source);
        }
        catch (XMLParseException e) {
            Exception ex = e.getException();
            if (ex == null || ex instanceof CharConversionException) {
                LocatorImpl locatorImpl = new LocatorImpl();
                locatorImpl.setPublicId(e.getPublicId());
                locatorImpl.setSystemId(e.getExpandedSystemId());
                locatorImpl.setLineNumber(e.getLineNumber());
                locatorImpl.setColumnNumber(e.getColumnNumber());
                throw ex == null ? new SAXParseException(e.getMessage(), locatorImpl) : new SAXParseException(e.getMessage(), locatorImpl, ex);
            }
            if (ex instanceof SAXException) {
                throw (SAXException)ex;
            }
            if (ex instanceof IOException) {
                throw (IOException)ex;
            }
            throw new SAXException(ex);
        }
        catch (XNIException e) {
            e.printStackTrace();
            Exception ex = e.getException();
            if (ex == null) {
                throw new SAXException(e.getMessage());
            }
            if (ex instanceof SAXException) {
                throw (SAXException)ex;
            }
            if (ex instanceof IOException) {
                throw (IOException)ex;
            }
            throw new SAXException(ex);
        }
    }

    public void parse(InputSource inputSource) throws SAXException, IOException {
        try {
            XMLInputSource xmlInputSource = new XMLInputSource(inputSource.getPublicId(), inputSource.getSystemId(), null, false);
            xmlInputSource.setByteStream(inputSource.getByteStream());
            xmlInputSource.setCharacterStream(inputSource.getCharacterStream());
            xmlInputSource.setEncoding(inputSource.getEncoding());
            this.parse(xmlInputSource);
        }
        catch (XMLParseException e) {
            Exception ex = e.getException();
            if (ex == null || ex instanceof CharConversionException) {
                LocatorImpl locatorImpl = new LocatorImpl();
                locatorImpl.setPublicId(e.getPublicId());
                locatorImpl.setSystemId(e.getExpandedSystemId());
                locatorImpl.setLineNumber(e.getLineNumber());
                locatorImpl.setColumnNumber(e.getColumnNumber());
                throw ex == null ? new SAXParseException(e.getMessage(), locatorImpl) : new SAXParseException(e.getMessage(), locatorImpl, ex);
            }
            if (ex instanceof SAXException) {
                throw (SAXException)ex;
            }
            if (ex instanceof IOException) {
                throw (IOException)ex;
            }
            throw new SAXException(ex);
        }
        catch (XNIException e) {
            Exception ex = e.getException();
            if (ex == null) {
                throw new SAXException(e.getMessage());
            }
            if (ex instanceof SAXException) {
                throw (SAXException)ex;
            }
            if (ex instanceof IOException) {
                throw (IOException)ex;
            }
            throw new SAXException(ex);
        }
    }

    public void setEntityResolver(EntityResolver resolver) {
        try {
            XMLEntityResolver xer = (XMLEntityResolver)this.fConfiguration.getProperty("http://apache.org/xml/properties/internal/entity-resolver");
            if (this.fUseEntityResolver2 && resolver instanceof EntityResolver2) {
                if (xer instanceof EntityResolver2Wrapper) {
                    EntityResolver2Wrapper er2w = (EntityResolver2Wrapper)xer;
                    er2w.setEntityResolver((EntityResolver2)resolver);
                } else {
                    this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/entity-resolver", new EntityResolver2Wrapper((EntityResolver2)resolver));
                }
            } else if (xer instanceof EntityResolverWrapper) {
                EntityResolverWrapper erw = (EntityResolverWrapper)xer;
                erw.setEntityResolver(resolver);
            } else {
                this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/entity-resolver", new EntityResolverWrapper(resolver));
            }
        }
        catch (XMLConfigurationException xMLConfigurationException) {
            // empty catch block
        }
    }

    public EntityResolver getEntityResolver() {
        EntityResolver entityResolver = null;
        try {
            XMLEntityResolver xmlEntityResolver = (XMLEntityResolver)this.fConfiguration.getProperty("http://apache.org/xml/properties/internal/entity-resolver");
            if (xmlEntityResolver != null) {
                if (xmlEntityResolver instanceof EntityResolverWrapper) {
                    entityResolver = ((EntityResolverWrapper)xmlEntityResolver).getEntityResolver();
                } else if (xmlEntityResolver instanceof EntityResolver2Wrapper) {
                    entityResolver = ((EntityResolver2Wrapper)xmlEntityResolver).getEntityResolver();
                }
            }
        }
        catch (XMLConfigurationException xMLConfigurationException) {
            // empty catch block
        }
        return entityResolver;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        try {
            XMLErrorHandler xeh = (XMLErrorHandler)this.fConfiguration.getProperty("http://apache.org/xml/properties/internal/error-handler");
            if (xeh instanceof ErrorHandlerWrapper) {
                ErrorHandlerWrapper ehw = (ErrorHandlerWrapper)xeh;
                ehw.setErrorHandler(errorHandler);
            } else {
                this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/error-handler", new ErrorHandlerWrapper(errorHandler));
            }
        }
        catch (XMLConfigurationException xMLConfigurationException) {
            // empty catch block
        }
    }

    public ErrorHandler getErrorHandler() {
        ErrorHandler errorHandler = null;
        try {
            XMLErrorHandler xmlErrorHandler = (XMLErrorHandler)this.fConfiguration.getProperty("http://apache.org/xml/properties/internal/error-handler");
            if (xmlErrorHandler != null && xmlErrorHandler instanceof ErrorHandlerWrapper) {
                errorHandler = ((ErrorHandlerWrapper)xmlErrorHandler).getErrorHandler();
            }
        }
        catch (XMLConfigurationException xMLConfigurationException) {
            // empty catch block
        }
        return errorHandler;
    }

    public void setFeature(String featureId, boolean state) throws SAXNotRecognizedException, SAXNotSupportedException {
        try {
            if (featureId.equals(USE_ENTITY_RESOLVER2)) {
                if (state != this.fUseEntityResolver2) {
                    this.fUseEntityResolver2 = state;
                    this.setEntityResolver(this.getEntityResolver());
                }
                return;
            }
            this.fConfiguration.setFeature(featureId, state);
        }
        catch (XMLConfigurationException e) {
            String identifier = e.getIdentifier();
            if (e.getType() == Status.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "feature-not-recognized", new Object[]{identifier}));
            }
            throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "feature-not-supported", new Object[]{identifier}));
        }
    }

    @Override
    public boolean getFeature(String featureId) throws SAXNotRecognizedException, SAXNotSupportedException {
        try {
            if (featureId.equals(USE_ENTITY_RESOLVER2)) {
                return this.fUseEntityResolver2;
            }
            return this.fConfiguration.getFeature(featureId);
        }
        catch (XMLConfigurationException e) {
            String identifier = e.getIdentifier();
            if (e.getType() == Status.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "feature-not-recognized", new Object[]{identifier}));
            }
            throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "feature-not-supported", new Object[]{identifier}));
        }
    }

    public void setProperty(String propertyId, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        int index;
        if (propertyId.equals("http://apache.org/xml/properties/security-manager")) {
            this.securityManager = XMLSecurityManager.convert(value, this.securityManager);
            this.setProperty0("http://apache.org/xml/properties/security-manager", this.securityManager);
            return;
        }
        if (propertyId.equals(XML_SECURITY_PROPERTY_MANAGER)) {
            this.securityPropertyManager = value == null ? new XMLSecurityPropertyManager() : (XMLSecurityPropertyManager)value;
            this.setProperty0(XML_SECURITY_PROPERTY_MANAGER, this.securityPropertyManager);
            return;
        }
        if (this.securityManager == null) {
            this.securityManager = new XMLSecurityManager(true);
            this.setProperty0("http://apache.org/xml/properties/security-manager", this.securityManager);
        }
        if (this.securityPropertyManager == null) {
            this.securityPropertyManager = new XMLSecurityPropertyManager();
            this.setProperty0(XML_SECURITY_PROPERTY_MANAGER, this.securityPropertyManager);
        }
        if ((index = this.securityPropertyManager.getIndex(propertyId)) > -1) {
            this.securityPropertyManager.setValue(index, XMLSecurityPropertyManager.State.APIPROPERTY, (String)value);
        } else if (!this.securityManager.setLimit(propertyId, JdkProperty.State.APIPROPERTY, value)) {
            this.setProperty0(propertyId, value);
        }
    }

    public void setProperty0(String propertyId, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        try {
            this.fConfiguration.setProperty(propertyId, value);
        }
        catch (XMLConfigurationException e) {
            String identifier = e.getIdentifier();
            if (e.getType() == Status.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "property-not-recognized", new Object[]{identifier}));
            }
            throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "property-not-supported", new Object[]{identifier}));
        }
    }

    public Object getProperty(String propertyId) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (propertyId.equals("http://apache.org/xml/properties/dom/current-element-node")) {
            boolean deferred = false;
            try {
                deferred = this.getFeature("http://apache.org/xml/features/dom/defer-node-expansion");
            }
            catch (XMLConfigurationException xMLConfigurationException) {
                // empty catch block
            }
            if (deferred) {
                throw new SAXNotSupportedException("Current element node cannot be queried when node expansion is deferred.");
            }
            return this.fCurrentNode != null && this.fCurrentNode.getNodeType() == 1 ? this.fCurrentNode : null;
        }
        try {
            XMLSecurityPropertyManager spm = (XMLSecurityPropertyManager)this.fConfiguration.getProperty(XML_SECURITY_PROPERTY_MANAGER);
            int index = spm.getIndex(propertyId);
            if (index > -1) {
                return spm.getValueByIndex(index);
            }
            return this.fConfiguration.getProperty(propertyId);
        }
        catch (XMLConfigurationException e) {
            String identifier = e.getIdentifier();
            if (e.getType() == Status.NOT_RECOGNIZED) {
                throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "property-not-recognized", new Object[]{identifier}));
            }
            throw new SAXNotSupportedException(SAXMessageFormatter.formatMessage(this.fConfiguration.getLocale(), "property-not-supported", new Object[]{identifier}));
        }
    }

    public XMLParserConfiguration getXMLParserConfiguration() {
        return this.fConfiguration;
    }
}

