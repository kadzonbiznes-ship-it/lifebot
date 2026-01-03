/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.jaxp;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderImpl;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.sun.org.apache.xerces.internal.util.SAXMessageFormatter;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import jdk.xml.internal.JdkProperty;
import jdk.xml.internal.XMLSecurityManager;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public class DocumentBuilderFactoryImpl
extends DocumentBuilderFactory {
    private Map<String, Object> attributes;
    private Map<String, Boolean> features;
    private Schema grammar;
    private boolean isXIncludeAware;
    private boolean fSecureProcess = true;
    XMLSecurityManager fSecurityManager = null;
    XMLSecurityPropertyManager fSecurityPropertyMgr = null;

    public DocumentBuilderFactoryImpl() {
        this(null, null);
    }

    public DocumentBuilderFactoryImpl(XMLSecurityManager xsm, XMLSecurityPropertyManager xspm) {
        this.fSecurityManager = xsm == null ? new XMLSecurityManager(true) : xsm;
        this.fSecurityPropertyMgr = xspm == null ? new XMLSecurityPropertyManager() : xspm;
    }

    @Override
    public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        if (this.grammar != null && this.attributes != null) {
            if (this.attributes.containsKey("http://java.sun.com/xml/jaxp/properties/schemaLanguage")) {
                throw new ParserConfigurationException(SAXMessageFormatter.formatMessage(null, "schema-already-specified", new Object[]{"http://java.sun.com/xml/jaxp/properties/schemaLanguage"}));
            }
            if (this.attributes.containsKey("http://java.sun.com/xml/jaxp/properties/schemaSource")) {
                throw new ParserConfigurationException(SAXMessageFormatter.formatMessage(null, "schema-already-specified", new Object[]{"http://java.sun.com/xml/jaxp/properties/schemaSource"}));
            }
        }
        try {
            return new DocumentBuilderImpl(this, this.attributes, this.features, this.fSecureProcess);
        }
        catch (SAXException se) {
            throw new ParserConfigurationException(se.getMessage());
        }
    }

    @Override
    public void setAttribute(String name, Object value) throws IllegalArgumentException {
        String pName;
        if (value == null) {
            if (this.attributes != null) {
                this.attributes.remove(name);
            }
            return;
        }
        if (this.attributes == null) {
            this.attributes = new HashMap<String, Object>();
        }
        if ((pName = this.fSecurityManager.find(name)) != null) {
            this.fSecurityManager.setLimit(name, JdkProperty.State.APIPROPERTY, value);
            this.attributes.put(pName, this.fSecurityManager.getLimitAsString(pName));
            return;
        }
        pName = this.fSecurityPropertyMgr.find(name);
        if (pName != null) {
            this.attributes.put(pName, value);
            return;
        }
        this.attributes.put(name, value);
        try {
            new DocumentBuilderImpl(this, this.attributes, this.features);
        }
        catch (Exception e) {
            this.attributes.remove(name);
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public Object getAttribute(String name) throws IllegalArgumentException {
        Object val;
        String pName = this.fSecurityManager.find(name);
        if (pName != null) {
            return this.fSecurityManager.getLimitAsString(pName);
        }
        pName = this.fSecurityPropertyMgr.find(name);
        if (pName != null) {
            return this.attributes.get(pName);
        }
        if (this.attributes != null && (val = this.attributes.get(name)) != null) {
            return val;
        }
        DOMParser domParser = null;
        try {
            domParser = new DocumentBuilderImpl(this, this.attributes, this.features).getDOMParser();
            return domParser.getProperty(name);
        }
        catch (SAXException se1) {
            try {
                boolean result = domParser.getFeature(name);
                return result ? Boolean.TRUE : Boolean.FALSE;
            }
            catch (SAXException se2) {
                throw new IllegalArgumentException(se1.getMessage());
            }
        }
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
        return this.isXIncludeAware;
    }

    @Override
    public void setXIncludeAware(boolean state) {
        this.isXIncludeAware = state;
    }

    @Override
    public boolean getFeature(String name) throws ParserConfigurationException {
        Boolean val;
        if (name.equals("http://javax.xml.XMLConstants/feature/secure-processing")) {
            return this.fSecureProcess;
        }
        if (this.features != null && (val = this.features.get(name)) != null) {
            return val;
        }
        try {
            DOMParser domParser = new DocumentBuilderImpl(this, this.attributes, this.features).getDOMParser();
            return domParser.getFeature(name);
        }
        catch (SAXException e) {
            throw new ParserConfigurationException(e.getMessage());
        }
    }

    @Override
    public void setFeature(String name, boolean value) throws ParserConfigurationException {
        if (this.features == null) {
            this.features = new HashMap<String, Boolean>();
        }
        if (name.equals("http://javax.xml.XMLConstants/feature/secure-processing")) {
            if (System.getSecurityManager() != null && !value) {
                throw new ParserConfigurationException(SAXMessageFormatter.formatMessage(null, "jaxp-secureprocessing-feature", null));
            }
            this.fSecureProcess = value;
            this.features.put(name, value ? Boolean.TRUE : Boolean.FALSE);
            return;
        }
        this.features.put(name, value ? Boolean.TRUE : Boolean.FALSE);
        try {
            new DocumentBuilderImpl(this, this.attributes, this.features);
        }
        catch (SAXNotSupportedException e) {
            this.features.remove(name);
            throw new ParserConfigurationException(e.getMessage());
        }
        catch (SAXNotRecognizedException e) {
            this.features.remove(name);
            throw new ParserConfigurationException(e.getMessage());
        }
    }
}

