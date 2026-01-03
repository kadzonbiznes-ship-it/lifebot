/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.parsers;

import com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl;
import javax.xml.parsers.FactoryFinder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.validation.Schema;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public abstract class SAXParserFactory {
    private static final String DEFAULT_IMPL = "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl";
    private boolean validating = false;
    private boolean namespaceAware = false;

    protected SAXParserFactory() {
    }

    public static SAXParserFactory newDefaultNSInstance() {
        return SAXParserFactory.makeNSAware(new SAXParserFactoryImpl());
    }

    public static SAXParserFactory newNSInstance() {
        return SAXParserFactory.makeNSAware(FactoryFinder.find(SAXParserFactory.class, DEFAULT_IMPL));
    }

    public static SAXParserFactory newNSInstance(String factoryClassName, ClassLoader classLoader) {
        return SAXParserFactory.makeNSAware(FactoryFinder.newInstance(SAXParserFactory.class, factoryClassName, classLoader, false));
    }

    public static SAXParserFactory newDefaultInstance() {
        return new SAXParserFactoryImpl();
    }

    public static SAXParserFactory newInstance() {
        return FactoryFinder.find(SAXParserFactory.class, DEFAULT_IMPL);
    }

    public static SAXParserFactory newInstance(String factoryClassName, ClassLoader classLoader) {
        return FactoryFinder.newInstance(SAXParserFactory.class, factoryClassName, classLoader, false);
    }

    private static SAXParserFactory makeNSAware(SAXParserFactory spf) {
        spf.setNamespaceAware(true);
        return spf;
    }

    public abstract SAXParser newSAXParser() throws ParserConfigurationException, SAXException;

    public void setNamespaceAware(boolean awareness) {
        this.namespaceAware = awareness;
    }

    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    public boolean isNamespaceAware() {
        return this.namespaceAware;
    }

    public boolean isValidating() {
        return this.validating;
    }

    public abstract void setFeature(String var1, boolean var2) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException;

    public abstract boolean getFeature(String var1) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException;

    public Schema getSchema() {
        throw new UnsupportedOperationException("This parser does not support specification \"" + this.getClass().getPackage().getSpecificationTitle() + "\" version \"" + this.getClass().getPackage().getSpecificationVersion() + "\"");
    }

    public void setSchema(Schema schema) {
        throw new UnsupportedOperationException("This parser does not support specification \"" + this.getClass().getPackage().getSpecificationTitle() + "\" version \"" + this.getClass().getPackage().getSpecificationVersion() + "\"");
    }

    public void setXIncludeAware(boolean state) {
        if (state) {
            throw new UnsupportedOperationException(" setXIncludeAware is not supported on this JAXP implementation or earlier: " + String.valueOf(this.getClass()));
        }
    }

    public boolean isXIncludeAware() {
        throw new UnsupportedOperationException("This parser does not support specification \"" + this.getClass().getPackage().getSpecificationTitle() + "\" version \"" + this.getClass().getPackage().getSpecificationVersion() + "\"");
    }
}

