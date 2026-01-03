/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.jaxp;

import com.sun.org.apache.xerces.internal.jaxp.SAXParserImpl;
import com.sun.org.apache.xerces.internal.util.SAXMessageFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public class SAXParserFactoryImpl
extends SAXParserFactory {
    private static final String VALIDATION_FEATURE = "http://xml.org/sax/features/validation";
    private static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";
    private static final String XINCLUDE_FEATURE = "http://apache.org/xml/features/xinclude";
    private Map<String, Boolean> features;
    private Schema grammar;
    private boolean isXIncludeAware;
    private boolean fSecureProcess = true;

    @Override
    public SAXParser newSAXParser() throws ParserConfigurationException {
        SAXParserImpl saxParserImpl;
        try {
            saxParserImpl = new SAXParserImpl(this, this.features, this.fSecureProcess);
        }
        catch (SAXException se) {
            throw new ParserConfigurationException(se.getMessage());
        }
        return saxParserImpl;
    }

    private SAXParserImpl newSAXParserImpl() throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        SAXParserImpl saxParserImpl;
        try {
            saxParserImpl = new SAXParserImpl(this, this.features);
        }
        catch (SAXNotSupportedException e) {
            throw e;
        }
        catch (SAXNotRecognizedException e) {
            throw e;
        }
        catch (SAXException se) {
            throw new ParserConfigurationException(se.getMessage());
        }
        return saxParserImpl;
    }

    @Override
    public void setFeature(String name, boolean value) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException();
        }
        if (name.equals("http://javax.xml.XMLConstants/feature/secure-processing")) {
            if (System.getSecurityManager() != null && !value) {
                throw new ParserConfigurationException(SAXMessageFormatter.formatMessage(null, "jaxp-secureprocessing-feature", null));
            }
            this.fSecureProcess = value;
            this.putInFeatures(name, value);
            return;
        }
        this.putInFeatures(name, value);
        try {
            this.newSAXParserImpl();
        }
        catch (SAXNotSupportedException e) {
            this.features.remove(name);
            throw e;
        }
        catch (SAXNotRecognizedException e) {
            this.features.remove(name);
            throw e;
        }
    }

    @Override
    public boolean getFeature(String name) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException();
        }
        if (name.equals("http://javax.xml.XMLConstants/feature/secure-processing")) {
            return this.fSecureProcess;
        }
        return this.newSAXParserImpl().getXMLReader().getFeature(name);
    }

    @Override
    public Schema getSchema() {
        return this.grammar;
    }

    @Override
    public void setSchema(Schema grammar) {
        this.grammar = grammar;
    }

    @Override
    public boolean isXIncludeAware() {
        return this.getFromFeatures(XINCLUDE_FEATURE);
    }

    @Override
    public void setXIncludeAware(boolean state) {
        this.putInFeatures(XINCLUDE_FEATURE, state);
    }

    @Override
    public void setValidating(boolean validating) {
        this.putInFeatures(VALIDATION_FEATURE, validating);
    }

    @Override
    public boolean isValidating() {
        return this.getFromFeatures(VALIDATION_FEATURE);
    }

    private void putInFeatures(String name, boolean value) {
        if (this.features == null) {
            this.features = new HashMap<String, Boolean>();
        }
        this.features.put(name, value ? Boolean.TRUE : Boolean.FALSE);
    }

    private boolean getFromFeatures(String name) {
        if (this.features == null) {
            return false;
        }
        Boolean value = this.features.get(name);
        return value == null ? false : value;
    }

    @Override
    public boolean isNamespaceAware() {
        return this.getFromFeatures(NAMESPACES_FEATURE);
    }

    @Override
    public void setNamespaceAware(boolean awareness) {
        this.putInFeatures(NAMESPACES_FEATURE, awareness);
    }
}

