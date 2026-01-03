/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.NodeSupplier;
import com.github.weisj.jsvg.parser.ParserProvider;
import com.github.weisj.jsvg.parser.ResourceLoader;
import com.github.weisj.jsvg.parser.SVGDocumentBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StaxSVGLoader {
    private static final Logger LOGGER = Logger.getLogger(StaxSVGLoader.class.getName());
    @NotNull
    private final NodeSupplier nodeSupplier;
    @NotNull
    private final XMLInputFactory xmlInputFactory;

    public StaxSVGLoader(@NotNull NodeSupplier nodeSupplier) {
        this(nodeSupplier, StaxSVGLoader.createDefaultFactory());
    }

    @NotNull
    private static XMLInputFactory createDefaultFactory() {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty("javax.xml.stream.supportDTD", false);
        factory.setProperty("javax.xml.stream.isReplacingEntityReferences", false);
        factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
        return factory;
    }

    public StaxSVGLoader(@NotNull NodeSupplier nodeSupplier, @NotNull XMLInputFactory factory) {
        this.nodeSupplier = nodeSupplier;
        this.xmlInputFactory = factory;
    }

    @Nullable
    private XMLEventReader createReader(@Nullable InputStream inputStream) {
        try {
            return this.xmlInputFactory.createXMLEventReader(inputStream);
        }
        catch (XMLStreamException e) {
            LOGGER.log(Level.SEVERE, "Error while creating XMLEventReader.", e);
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Nullable
    public SVGDocument load(@Nullable InputStream inputStream, @NotNull ParserProvider parserProvider, @NotNull ResourceLoader resourceLoader) throws IOException, XMLStreamException {
        if (inputStream == null) {
            return null;
        }
        XMLEventReader reader = this.createReader(inputStream);
        if (reader == null) {
            return null;
        }
        try {
            SVGDocumentBuilder builder = new SVGDocumentBuilder(parserProvider, resourceLoader, this.nodeSupplier);
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                switch (event.getEventType()) {
                    case 7: {
                        builder.startDocument();
                        break;
                    }
                    case 8: {
                        builder.endDocument();
                        break;
                    }
                    case 1: {
                        StartElement element = event.asStartElement();
                        HashMap<String, String> attributes = new HashMap<String, String>();
                        element.getAttributes().forEachRemaining(attr -> attributes.put(StaxSVGLoader.qualifiedName(attr.getName()), attr.getValue().trim()));
                        if (builder.startElement(StaxSVGLoader.qualifiedName(element.getName()), attributes)) break;
                        StaxSVGLoader.skipElement(reader);
                        break;
                    }
                    case 2: {
                        builder.endElement(StaxSVGLoader.qualifiedName(event.asEndElement().getName()));
                        break;
                    }
                    case 4: 
                    case 12: {
                        char[] data = event.asCharacters().getData().toCharArray();
                        builder.addTextContent(data, 0, data.length);
                        break;
                    }
                }
            }
            SVGDocument sVGDocument = builder.build();
            return sVGDocument;
        }
        catch (XMLStreamException e) {
            LOGGER.log(Level.SEVERE, "Error while parsing SVG.", e);
        }
        finally {
            reader.close();
            inputStream.close();
        }
        return null;
    }

    private static void skipElement(@NotNull XMLEventReader reader) throws XMLStreamException {
        int elementCount = 1;
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                ++elementCount;
            } else if (event.isEndElement()) {
                --elementCount;
            }
            if (elementCount != 0) continue;
            return;
        }
    }

    private static String qualifiedName(@NotNull QName name) {
        String prefix = name.getPrefix();
        String localName = name.getLocalPart();
        if (prefix == null) {
            return localName;
        }
        if (prefix.isEmpty()) {
            return localName;
        }
        return prefix + ":" + localName;
    }
}

