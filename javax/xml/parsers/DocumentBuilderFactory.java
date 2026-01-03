/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.parsers;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.FactoryFinder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

public abstract class DocumentBuilderFactory {
    private static final String DEFAULT_IMPL = "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl";
    private boolean validating = false;
    private boolean namespaceAware = false;
    private boolean whitespace = false;
    private boolean expandEntityRef = true;
    private boolean ignoreComments = false;
    private boolean coalescing = false;

    protected DocumentBuilderFactory() {
    }

    public static DocumentBuilderFactory newDefaultNSInstance() {
        return DocumentBuilderFactory.makeNSAware(new DocumentBuilderFactoryImpl());
    }

    public static DocumentBuilderFactory newNSInstance() {
        return DocumentBuilderFactory.makeNSAware(FactoryFinder.find(DocumentBuilderFactory.class, DEFAULT_IMPL));
    }

    public static DocumentBuilderFactory newNSInstance(String factoryClassName, ClassLoader classLoader) {
        return DocumentBuilderFactory.makeNSAware(FactoryFinder.newInstance(DocumentBuilderFactory.class, factoryClassName, classLoader, false));
    }

    public static DocumentBuilderFactory newDefaultInstance() {
        return new DocumentBuilderFactoryImpl();
    }

    public static DocumentBuilderFactory newInstance() {
        return FactoryFinder.find(DocumentBuilderFactory.class, DEFAULT_IMPL);
    }

    public static DocumentBuilderFactory newInstance(String factoryClassName, ClassLoader classLoader) {
        return FactoryFinder.newInstance(DocumentBuilderFactory.class, factoryClassName, classLoader, false);
    }

    private static DocumentBuilderFactory makeNSAware(DocumentBuilderFactory dbf) {
        dbf.setNamespaceAware(true);
        return dbf;
    }

    public abstract DocumentBuilder newDocumentBuilder() throws ParserConfigurationException;

    public void setNamespaceAware(boolean awareness) {
        this.namespaceAware = awareness;
    }

    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    public void setIgnoringElementContentWhitespace(boolean whitespace) {
        this.whitespace = whitespace;
    }

    public void setExpandEntityReferences(boolean expandEntityRef) {
        this.expandEntityRef = expandEntityRef;
    }

    public void setIgnoringComments(boolean ignoreComments) {
        this.ignoreComments = ignoreComments;
    }

    public void setCoalescing(boolean coalescing) {
        this.coalescing = coalescing;
    }

    public boolean isNamespaceAware() {
        return this.namespaceAware;
    }

    public boolean isValidating() {
        return this.validating;
    }

    public boolean isIgnoringElementContentWhitespace() {
        return this.whitespace;
    }

    public boolean isExpandEntityReferences() {
        return this.expandEntityRef;
    }

    public boolean isIgnoringComments() {
        return this.ignoreComments;
    }

    public boolean isCoalescing() {
        return this.coalescing;
    }

    public abstract void setAttribute(String var1, Object var2) throws IllegalArgumentException;

    public abstract Object getAttribute(String var1) throws IllegalArgumentException;

    public abstract void setFeature(String var1, boolean var2) throws ParserConfigurationException;

    public abstract boolean getFeature(String var1) throws ParserConfigurationException;

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

