/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.internal.stream;

import com.sun.org.apache.xerces.internal.impl.PropertyManager;
import com.sun.org.apache.xerces.internal.impl.XMLStreamFilterImpl;
import com.sun.org.apache.xerces.internal.impl.XMLStreamReaderImpl;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import com.sun.xml.internal.stream.EventFilterSupport;
import com.sun.xml.internal.stream.XMLEventReaderImpl;
import java.io.InputStream;
import java.io.Reader;
import javax.xml.stream.EventFilter;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

public class XMLInputFactoryImpl
extends XMLInputFactory {
    private PropertyManager fPropertyManager = new PropertyManager(1);
    private static final boolean DEBUG = false;
    private XMLStreamReaderImpl fTempReader = null;
    boolean fPropertyChanged = false;
    boolean fReuseInstance = false;

    void initEventReader() {
        this.fPropertyChanged = true;
    }

    @Override
    public XMLEventReader createXMLEventReader(InputStream inputstream) throws XMLStreamException {
        this.initEventReader();
        return new XMLEventReaderImpl(this.createXMLStreamReader(inputstream));
    }

    @Override
    public XMLEventReader createXMLEventReader(Reader reader) throws XMLStreamException {
        this.initEventReader();
        return new XMLEventReaderImpl(this.createXMLStreamReader(reader));
    }

    @Override
    public XMLEventReader createXMLEventReader(Source source) throws XMLStreamException {
        this.initEventReader();
        return new XMLEventReaderImpl(this.createXMLStreamReader(source));
    }

    @Override
    public XMLEventReader createXMLEventReader(String systemId, InputStream inputstream) throws XMLStreamException {
        this.initEventReader();
        return new XMLEventReaderImpl(this.createXMLStreamReader(systemId, inputstream));
    }

    @Override
    public XMLEventReader createXMLEventReader(InputStream stream, String encoding) throws XMLStreamException {
        this.initEventReader();
        return new XMLEventReaderImpl(this.createXMLStreamReader(stream, encoding));
    }

    @Override
    public XMLEventReader createXMLEventReader(String systemId, Reader reader) throws XMLStreamException {
        this.initEventReader();
        return new XMLEventReaderImpl(this.createXMLStreamReader(systemId, reader));
    }

    @Override
    public XMLEventReader createXMLEventReader(XMLStreamReader reader) throws XMLStreamException {
        return new XMLEventReaderImpl(reader);
    }

    @Override
    public XMLStreamReader createXMLStreamReader(InputStream inputstream) throws XMLStreamException {
        XMLInputSource inputSource = new XMLInputSource(null, null, null, inputstream, null);
        return this.getXMLStreamReaderImpl(inputSource);
    }

    @Override
    public XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
        XMLInputSource inputSource = new XMLInputSource(null, null, null, reader, null);
        return this.getXMLStreamReaderImpl(inputSource);
    }

    @Override
    public XMLStreamReader createXMLStreamReader(String systemId, Reader reader) throws XMLStreamException {
        XMLInputSource inputSource = new XMLInputSource(null, systemId, null, reader, null);
        return this.getXMLStreamReaderImpl(inputSource);
    }

    @Override
    public XMLStreamReader createXMLStreamReader(Source source) throws XMLStreamException {
        return new XMLStreamReaderImpl(this.jaxpSourcetoXMLInputSource(source), new PropertyManager(this.fPropertyManager));
    }

    @Override
    public XMLStreamReader createXMLStreamReader(String systemId, InputStream inputstream) throws XMLStreamException {
        XMLInputSource inputSource = new XMLInputSource(null, systemId, null, inputstream, null);
        return this.getXMLStreamReaderImpl(inputSource);
    }

    @Override
    public XMLStreamReader createXMLStreamReader(InputStream inputstream, String encoding) throws XMLStreamException {
        XMLInputSource inputSource = new XMLInputSource(null, null, null, inputstream, encoding);
        return this.getXMLStreamReaderImpl(inputSource);
    }

    @Override
    public XMLEventAllocator getEventAllocator() {
        return (XMLEventAllocator)this.getProperty("javax.xml.stream.allocator");
    }

    @Override
    public XMLReporter getXMLReporter() {
        return (XMLReporter)this.fPropertyManager.getProperty("javax.xml.stream.reporter");
    }

    @Override
    public XMLResolver getXMLResolver() {
        Object object = this.fPropertyManager.getProperty("javax.xml.stream.resolver");
        return (XMLResolver)object;
    }

    @Override
    public void setXMLReporter(XMLReporter xmlreporter) {
        this.fPropertyManager.setProperty("javax.xml.stream.reporter", xmlreporter);
    }

    @Override
    public void setXMLResolver(XMLResolver xmlresolver) {
        this.fPropertyManager.setProperty("javax.xml.stream.resolver", xmlresolver);
    }

    @Override
    public XMLEventReader createFilteredReader(XMLEventReader reader, EventFilter filter) throws XMLStreamException {
        return new EventFilterSupport(reader, filter);
    }

    @Override
    public XMLStreamReader createFilteredReader(XMLStreamReader reader, StreamFilter filter) throws XMLStreamException {
        if (reader != null && filter != null) {
            return new XMLStreamFilterImpl(reader, filter);
        }
        return null;
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("Property not supported");
        }
        if (this.fPropertyManager.containsProperty(name)) {
            return this.fPropertyManager.getProperty(name);
        }
        throw new IllegalArgumentException("Property not supported");
    }

    @Override
    public boolean isPropertySupported(String name) {
        if (name == null) {
            return false;
        }
        return this.fPropertyManager.containsProperty(name);
    }

    @Override
    public void setEventAllocator(XMLEventAllocator allocator) {
        this.fPropertyManager.setProperty("javax.xml.stream.allocator", allocator);
    }

    @Override
    public void setProperty(String name, Object value) throws IllegalArgumentException {
        if (name == null || value == null || !this.fPropertyManager.containsProperty(name)) {
            throw new IllegalArgumentException("Property " + name + " is not supported");
        }
        if (name == "reuse-instance" || name.equals("reuse-instance")) {
            this.fReuseInstance = (Boolean)value;
        } else {
            this.fPropertyChanged = true;
        }
        this.fPropertyManager.setProperty(name, value);
    }

    XMLStreamReader getXMLStreamReaderImpl(XMLInputSource inputSource) throws XMLStreamException {
        if (this.fTempReader == null) {
            this.fPropertyChanged = false;
            this.fTempReader = new XMLStreamReaderImpl(inputSource, new PropertyManager(this.fPropertyManager));
            return this.fTempReader;
        }
        if (this.fReuseInstance && this.fTempReader.canReuse() && !this.fPropertyChanged) {
            this.fTempReader.reset();
            this.fTempReader.setInputSource(inputSource);
            this.fPropertyChanged = false;
            return this.fTempReader;
        }
        this.fPropertyChanged = false;
        this.fTempReader = new XMLStreamReaderImpl(inputSource, new PropertyManager(this.fPropertyManager));
        return this.fTempReader;
    }

    XMLInputSource jaxpSourcetoXMLInputSource(Source source) {
        if (source instanceof StreamSource) {
            StreamSource stSource = (StreamSource)source;
            String systemId = stSource.getSystemId();
            String publicId = stSource.getPublicId();
            InputStream istream = stSource.getInputStream();
            Reader reader = stSource.getReader();
            if (istream != null) {
                return new XMLInputSource(publicId, systemId, null, istream, null);
            }
            if (reader != null) {
                return new XMLInputSource(publicId, systemId, null, reader, null);
            }
            return new XMLInputSource(publicId, systemId, null, false);
        }
        throw new UnsupportedOperationException("Cannot create XMLStreamReader or XMLEventReader from a " + source.getClass().getName());
    }
}

