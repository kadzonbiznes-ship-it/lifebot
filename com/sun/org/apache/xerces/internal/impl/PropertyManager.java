/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.impl;

import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import com.sun.xml.internal.stream.StaxEntityResolverWrapper;
import java.util.HashMap;
import javax.xml.catalog.CatalogFeatures;
import javax.xml.stream.XMLResolver;
import jdk.xml.internal.JdkConstants;
import jdk.xml.internal.JdkProperty;
import jdk.xml.internal.JdkXmlUtils;

public class PropertyManager {
    public static final String STAX_NOTATIONS = "javax.xml.stream.notations";
    public static final String STAX_ENTITIES = "javax.xml.stream.entities";
    private static final String STRING_INTERNING = "http://xml.org/sax/features/string-interning";
    private static final String SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    private static final String XML_SECURITY_PROPERTY_MANAGER = "jdk.xml.xmlSecurityPropertyManager";
    HashMap<String, Object> supportedProps = new HashMap();
    private XMLSecurityManager fSecurityManager;
    private XMLSecurityPropertyManager fSecurityPropertyMgr;
    public static final int CONTEXT_READER = 1;
    public static final int CONTEXT_WRITER = 2;

    public PropertyManager(int context) {
        switch (context) {
            case 1: {
                this.initConfigurableReaderProperties();
                break;
            }
            case 2: {
                this.initWriterProps();
            }
        }
    }

    public PropertyManager(PropertyManager propertyManager) {
        HashMap<String, Object> properties = propertyManager.getProperties();
        this.supportedProps.putAll(properties);
        this.fSecurityManager = (XMLSecurityManager)this.getProperty(SECURITY_MANAGER);
        this.fSecurityPropertyMgr = (XMLSecurityPropertyManager)this.getProperty(XML_SECURITY_PROPERTY_MANAGER);
    }

    private HashMap<String, Object> getProperties() {
        return this.supportedProps;
    }

    private void initConfigurableReaderProperties() {
        this.supportedProps.put("javax.xml.stream.isNamespaceAware", Boolean.TRUE);
        this.supportedProps.put("javax.xml.stream.isValidating", Boolean.FALSE);
        this.supportedProps.put("javax.xml.stream.isReplacingEntityReferences", Boolean.TRUE);
        this.supportedProps.put("javax.xml.stream.isSupportingExternalEntities", Boolean.TRUE);
        this.supportedProps.put("javax.xml.stream.isCoalescing", Boolean.FALSE);
        this.supportedProps.put("javax.xml.stream.supportDTD", Boolean.TRUE);
        this.supportedProps.put("javax.xml.stream.reporter", null);
        this.supportedProps.put("javax.xml.stream.resolver", null);
        this.supportedProps.put("javax.xml.stream.allocator", null);
        this.supportedProps.put(STAX_NOTATIONS, null);
        this.supportedProps.put(STRING_INTERNING, true);
        this.supportedProps.put("http://apache.org/xml/features/allow-java-encodings", true);
        this.supportedProps.put("add-namespacedecl-as-attrbiute", Boolean.FALSE);
        this.supportedProps.put("http://java.sun.com/xml/stream/properties/reader-in-defined-state", true);
        this.supportedProps.put("reuse-instance", true);
        this.supportedProps.put("http://java.sun.com/xml/stream/properties/report-cdata-event", false);
        this.supportedProps.put("http://java.sun.com/xml/stream/properties/ignore-external-dtd", Boolean.FALSE);
        this.supportedProps.put("http://apache.org/xml/features/validation/warn-on-duplicate-attdef", false);
        this.supportedProps.put("http://apache.org/xml/features/warn-on-duplicate-entitydef", false);
        this.supportedProps.put("http://apache.org/xml/features/validation/warn-on-undeclared-elemdef", false);
        this.fSecurityManager = new XMLSecurityManager(true);
        this.supportedProps.put(SECURITY_MANAGER, this.fSecurityManager);
        this.fSecurityPropertyMgr = new XMLSecurityPropertyManager();
        this.supportedProps.put(XML_SECURITY_PROPERTY_MANAGER, this.fSecurityPropertyMgr);
        this.supportedProps.put("http://javax.xml.XMLConstants/feature/useCatalog", JdkXmlUtils.USE_CATALOG_DEFAULT);
        for (CatalogFeatures.Feature f : CatalogFeatures.Feature.values()) {
            this.supportedProps.put(f.getPropertyName(), null);
        }
        this.supportedProps.put("jdk.xml.cdataChunkSize", JdkConstants.CDATA_CHUNK_SIZE_DEFAULT);
    }

    private void initWriterProps() {
        this.supportedProps.put("javax.xml.stream.isRepairingNamespaces", Boolean.FALSE);
        this.supportedProps.put("escapeCharacters", Boolean.TRUE);
        this.supportedProps.put("reuse-instance", true);
    }

    public boolean containsProperty(String property) {
        return this.supportedProps.containsKey(property) || this.fSecurityManager != null && this.fSecurityManager.getIndex(property) > -1 || this.fSecurityPropertyMgr != null && this.fSecurityPropertyMgr.getIndex(property) > -1;
    }

    public Object getProperty(String property) {
        String propertyValue;
        String string = propertyValue = this.fSecurityManager != null ? this.fSecurityManager.getLimitAsString(property) : null;
        if (propertyValue == null) {
            propertyValue = this.fSecurityPropertyMgr != null ? this.fSecurityPropertyMgr.getValue(property) : null;
        }
        return propertyValue != null ? propertyValue : this.supportedProps.get(property);
    }

    public void setProperty(String property, Object value) {
        String equivalentProperty = null;
        if (property.equals("javax.xml.stream.isNamespaceAware")) {
            equivalentProperty = "http://apache.org/xml/features/namespaces";
        } else if (property.equals("javax.xml.stream.isValidating")) {
            if (value instanceof Boolean && ((Boolean)value).booleanValue()) {
                throw new IllegalArgumentException("true value of isValidating not supported");
            }
        } else if (property.equals(STRING_INTERNING)) {
            if (value instanceof Boolean && !((Boolean)value).booleanValue()) {
                throw new IllegalArgumentException("false value of http://xml.org/sax/features/string-interningfeature is not supported");
            }
        } else if (property.equals("javax.xml.stream.resolver")) {
            this.supportedProps.put("http://apache.org/xml/properties/internal/stax-entity-resolver", new StaxEntityResolverWrapper((XMLResolver)value));
        }
        if (property.equals(SECURITY_MANAGER)) {
            this.fSecurityManager = XMLSecurityManager.convert(value, this.fSecurityManager);
            this.supportedProps.put(SECURITY_MANAGER, this.fSecurityManager);
            return;
        }
        if (property.equals(XML_SECURITY_PROPERTY_MANAGER)) {
            this.fSecurityPropertyMgr = value == null ? new XMLSecurityPropertyManager() : (XMLSecurityPropertyManager)value;
            this.supportedProps.put(XML_SECURITY_PROPERTY_MANAGER, this.fSecurityPropertyMgr);
            return;
        }
        if (!(this.fSecurityManager != null && this.fSecurityManager.setLimit(property, JdkProperty.State.APIPROPERTY, value) || this.fSecurityPropertyMgr != null && this.fSecurityPropertyMgr.setValue(property, XMLSecurityPropertyManager.State.APIPROPERTY, value))) {
            this.supportedProps.put(property, value);
        }
        if (equivalentProperty != null) {
            this.supportedProps.put(equivalentProperty, value);
        }
    }

    public String toString() {
        return this.supportedProps.toString();
    }
}

