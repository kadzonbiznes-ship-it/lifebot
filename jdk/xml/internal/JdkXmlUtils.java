/*
 * Decompiled with CFR 0.152.
 */
package jdk.xml.internal;

import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl;
import com.sun.org.apache.xerces.internal.util.ParserConfigurationSettings;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityPropertyManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import javax.xml.catalog.CatalogFeatures;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import jdk.xml.internal.JdkProperty;
import jdk.xml.internal.SecuritySupport;
import jdk.xml.internal.XMLSecurityManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class JdkXmlUtils {
    private static final String DOM_FACTORY_ID = "javax.xml.parsers.DocumentBuilderFactory";
    private static final String SAX_FACTORY_ID = "javax.xml.parsers.SAXParserFactory";
    private static final String SAX_DRIVER = "org.xml.sax.driver";
    public static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";
    public static final String NAMESPACE_PREFIXES_FEATURE = "http://xml.org/sax/features/namespace-prefixes";
    public static final String USE_CATALOG = "http://javax.xml.XMLConstants/feature/useCatalog";
    public static final String SP_USE_CATALOG = "javax.xml.useCatalog";
    public static final String CATALOG_FILES = CatalogFeatures.Feature.FILES.getPropertyName();
    public static final String CATALOG_DEFER = CatalogFeatures.Feature.DEFER.getPropertyName();
    public static final String CATALOG_PREFER = CatalogFeatures.Feature.PREFER.getPropertyName();
    public static final String CATALOG_RESOLVE = CatalogFeatures.Feature.RESOLVE.getPropertyName();
    public static final boolean USE_CATALOG_DEFAULT = SecuritySupport.getJAXPSystemProperty(Boolean.class, "javax.xml.useCatalog", "true");
    private static final SAXParserFactory defaultSAXFactory = JdkXmlUtils.getSAXFactory(false);

    public static int getValue(Object value, int defValue) {
        if (value == null) {
            return defValue;
        }
        if (value instanceof Number) {
            return ((Number)value).intValue();
        }
        if (value instanceof String) {
            return Integer.parseInt(String.valueOf(value));
        }
        throw new IllegalArgumentException("Unexpected class: " + String.valueOf(value.getClass()));
    }

    public static void setXMLReaderPropertyIfSupport(XMLReader reader, String property, Object value, boolean warn) {
        block2: {
            try {
                reader.setProperty(property, value);
            }
            catch (SAXNotRecognizedException | SAXNotSupportedException e) {
                if (!warn) break block2;
                XMLSecurityManager.printWarning(reader.getClass().getName(), property, e);
            }
        }
    }

    public static String getCatalogFeature(CatalogFeatures features, String name) {
        for (CatalogFeatures.Feature feature : CatalogFeatures.Feature.values()) {
            if (!feature.getPropertyName().equals(name)) continue;
            return features.get(feature);
        }
        return null;
    }

    public static CatalogFeatures getCatalogFeatures(String defer, String file, String prefer, String resolve) {
        CatalogFeatures.Builder builder = CatalogFeatures.builder();
        if (file != null) {
            builder = builder.with(CatalogFeatures.Feature.FILES, file);
        }
        if (prefer != null) {
            builder = builder.with(CatalogFeatures.Feature.PREFER, prefer);
        }
        if (defer != null) {
            builder = builder.with(CatalogFeatures.Feature.DEFER, defer);
        }
        if (resolve != null) {
            builder = builder.with(CatalogFeatures.Feature.RESOLVE, resolve);
        }
        return builder.build();
    }

    public static void catalogFeaturesConfig2Config(XMLComponentManager config1, ParserConfigurationSettings config2) {
        boolean supportCatalog = true;
        boolean useCatalog = config1.getFeature(USE_CATALOG);
        try {
            config2.setFeature(USE_CATALOG, useCatalog);
        }
        catch (XMLConfigurationException e) {
            supportCatalog = false;
        }
        if (supportCatalog && useCatalog) {
            try {
                for (CatalogFeatures.Feature f : CatalogFeatures.Feature.values()) {
                    config2.setProperty(f.getPropertyName(), config1.getProperty(f.getPropertyName()));
                }
            }
            catch (XMLConfigurationException xMLConfigurationException) {
                // empty catch block
            }
        }
    }

    public static void catalogFeaturesConfig2Reader(XMLComponentManager config, XMLReader reader) {
        boolean supportCatalog = true;
        boolean useCatalog = config.getFeature(USE_CATALOG);
        try {
            reader.setFeature(USE_CATALOG, useCatalog);
        }
        catch (SAXNotRecognizedException | SAXNotSupportedException e) {
            supportCatalog = false;
        }
        if (supportCatalog && useCatalog) {
            try {
                for (CatalogFeatures.Feature f : CatalogFeatures.Feature.values()) {
                    reader.setProperty(f.getPropertyName(), config.getProperty(f.getPropertyName()));
                }
            }
            catch (SAXNotRecognizedException | SAXNotSupportedException sAXException) {
                // empty catch block
            }
        }
    }

    public static XMLReader getXMLReader(boolean overrideDefaultParser, boolean secureProcessing) {
        XMLReader reader = null;
        String spSAXDriver = SecuritySupport.getSystemProperty(SAX_DRIVER);
        if (spSAXDriver != null) {
            reader = JdkXmlUtils.getXMLReaderWXMLReaderFactory();
        } else if (overrideDefaultParser) {
            reader = JdkXmlUtils.getXMLReaderWSAXFactory(overrideDefaultParser);
        }
        if (reader != null) {
            if (secureProcessing) {
                try {
                    reader.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", secureProcessing);
                }
                catch (SAXException e) {
                    XMLSecurityManager.printWarning(reader.getClass().getName(), "http://javax.xml.XMLConstants/feature/secure-processing", e);
                }
            }
            try {
                reader.setFeature(NAMESPACES_FEATURE, true);
                reader.setFeature(NAMESPACE_PREFIXES_FEATURE, false);
            }
            catch (SAXException sAXException) {
                // empty catch block
            }
            return reader;
        }
        SAXParserFactory saxFactory = defaultSAXFactory;
        try {
            reader = saxFactory.newSAXParser().getXMLReader();
        }
        catch (ParserConfigurationException | SAXException exception) {
            // empty catch block
        }
        return reader;
    }

    public static Document getDOMDocument() {
        try {
            DocumentBuilderFactory dbf = JdkXmlUtils.getDOMFactory(false);
            return dbf.newDocumentBuilder().newDocument();
        }
        catch (ParserConfigurationException parserConfigurationException) {
            return null;
        }
    }

    public static DocumentBuilderFactory getDOMFactory(boolean overrideDefaultParser) {
        return JdkXmlUtils.getDOMFactory(overrideDefaultParser, null, null);
    }

    public static DocumentBuilderFactory getDOMFactory(boolean overrideDefaultParser, XMLSecurityManager xsm, XMLSecurityPropertyManager xspm) {
        boolean override = overrideDefaultParser;
        String spDOMFactory = SecuritySupport.getJAXPSystemProperty(DOM_FACTORY_ID);
        if (spDOMFactory != null) {
            override = true;
        }
        DocumentBuilderFactory dbf = !override ? new DocumentBuilderFactoryImpl(xsm, xspm) : DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        return dbf;
    }

    public static SAXParserFactory getSAXFactory(boolean overrideDefaultParser) {
        boolean override = overrideDefaultParser;
        String spSAXFactory = SecuritySupport.getJAXPSystemProperty(SAX_FACTORY_ID);
        if (spSAXFactory != null && System.getSecurityManager() == null) {
            override = true;
        }
        SAXParserFactory factory = !override ? new SAXParserFactoryImpl() : SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory;
    }

    public static SAXTransformerFactory getSAXTransformFactory(boolean overrideDefaultParser) {
        SAXTransformerFactory tf = overrideDefaultParser ? (SAXTransformerFactory)SAXTransformerFactory.newInstance() : new TransformerFactoryImpl();
        try {
            tf.setFeature("jdk.xml.overrideDefaultParser", overrideDefaultParser);
        }
        catch (TransformerConfigurationException transformerConfigurationException) {
            // empty catch block
        }
        return tf;
    }

    public static String getDTDExternalDecl(String publicId, String systemId) {
        StringBuilder sb = new StringBuilder();
        if (null != publicId) {
            sb.append(" PUBLIC ");
            sb.append(JdkXmlUtils.quoteString(publicId));
        }
        if (null != systemId) {
            if (null == publicId) {
                sb.append(" SYSTEM ");
            } else {
                sb.append(" ");
            }
            sb.append(JdkXmlUtils.quoteString(systemId));
        }
        return sb.toString();
    }

    private static String quoteString(String s) {
        char c = s.indexOf(34) > -1 ? (char)'\'' : '\"';
        return c + s + c;
    }

    private static XMLReader getXMLReaderWSAXFactory(boolean overrideDefaultParser) {
        SAXParserFactory saxFactory = JdkXmlUtils.getSAXFactory(overrideDefaultParser);
        try {
            return saxFactory.newSAXParser().getXMLReader();
        }
        catch (ParserConfigurationException | SAXException ex) {
            return JdkXmlUtils.getXMLReaderWXMLReaderFactory();
        }
    }

    private static XMLReader getXMLReaderWXMLReaderFactory() {
        try {
            return XMLReaderFactory.createXMLReader();
        }
        catch (SAXException sAXException) {
            return null;
        }
    }

    public static boolean setProperty(XMLSecurityManager xsm, XMLSecurityPropertyManager xspm, String property, Object value) {
        if (xsm != null && xsm.find(property) != null) {
            return xsm.setLimit(property, JdkProperty.State.APIPROPERTY, value);
        }
        if (xspm != null && xspm.find(property) != null) {
            return xspm.setValue(property, XMLSecurityPropertyManager.State.APIPROPERTY, value);
        }
        return false;
    }
}

