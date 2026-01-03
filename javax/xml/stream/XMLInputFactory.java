/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.stream;

import com.sun.xml.internal.stream.XMLInputFactoryImpl;
import java.io.InputStream;
import java.io.Reader;
import javax.xml.stream.EventFilter;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.FactoryFinder;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;

public abstract class XMLInputFactory {
    public static final String IS_NAMESPACE_AWARE = "javax.xml.stream.isNamespaceAware";
    public static final String IS_VALIDATING = "javax.xml.stream.isValidating";
    public static final String IS_COALESCING = "javax.xml.stream.isCoalescing";
    public static final String IS_REPLACING_ENTITY_REFERENCES = "javax.xml.stream.isReplacingEntityReferences";
    public static final String IS_SUPPORTING_EXTERNAL_ENTITIES = "javax.xml.stream.isSupportingExternalEntities";
    public static final String SUPPORT_DTD = "javax.xml.stream.supportDTD";
    public static final String REPORTER = "javax.xml.stream.reporter";
    public static final String RESOLVER = "javax.xml.stream.resolver";
    public static final String ALLOCATOR = "javax.xml.stream.allocator";
    static final String DEFAULIMPL = "com.sun.xml.internal.stream.XMLInputFactoryImpl";

    protected XMLInputFactory() {
    }

    public static XMLInputFactory newDefaultFactory() {
        return new XMLInputFactoryImpl();
    }

    public static XMLInputFactory newInstance() throws FactoryConfigurationError {
        return FactoryFinder.find(XMLInputFactory.class, DEFAULIMPL);
    }

    public static XMLInputFactory newFactory() throws FactoryConfigurationError {
        return FactoryFinder.find(XMLInputFactory.class, DEFAULIMPL);
    }

    @Deprecated(since="1.7")
    public static XMLInputFactory newInstance(String factoryId, ClassLoader classLoader) throws FactoryConfigurationError {
        return FactoryFinder.find(XMLInputFactory.class, factoryId, classLoader, null);
    }

    public static XMLInputFactory newFactory(String factoryId, ClassLoader classLoader) throws FactoryConfigurationError {
        return FactoryFinder.find(XMLInputFactory.class, factoryId, classLoader, null);
    }

    public abstract XMLStreamReader createXMLStreamReader(Reader var1) throws XMLStreamException;

    public abstract XMLStreamReader createXMLStreamReader(Source var1) throws XMLStreamException;

    public abstract XMLStreamReader createXMLStreamReader(InputStream var1) throws XMLStreamException;

    public abstract XMLStreamReader createXMLStreamReader(InputStream var1, String var2) throws XMLStreamException;

    public abstract XMLStreamReader createXMLStreamReader(String var1, InputStream var2) throws XMLStreamException;

    public abstract XMLStreamReader createXMLStreamReader(String var1, Reader var2) throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(Reader var1) throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(String var1, Reader var2) throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(XMLStreamReader var1) throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(Source var1) throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(InputStream var1) throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(InputStream var1, String var2) throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(String var1, InputStream var2) throws XMLStreamException;

    public abstract XMLStreamReader createFilteredReader(XMLStreamReader var1, StreamFilter var2) throws XMLStreamException;

    public abstract XMLEventReader createFilteredReader(XMLEventReader var1, EventFilter var2) throws XMLStreamException;

    public abstract XMLResolver getXMLResolver();

    public abstract void setXMLResolver(XMLResolver var1);

    public abstract XMLReporter getXMLReporter();

    public abstract void setXMLReporter(XMLReporter var1);

    public abstract void setProperty(String var1, Object var2) throws IllegalArgumentException;

    public abstract Object getProperty(String var1) throws IllegalArgumentException;

    public abstract boolean isPropertySupported(String var1);

    public abstract void setEventAllocator(XMLEventAllocator var1);

    public abstract XMLEventAllocator getEventAllocator();
}

