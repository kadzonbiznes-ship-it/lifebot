/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.internal.stream.events;

import com.sun.org.apache.xerces.internal.util.NamespaceContextWrapper;
import com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import com.sun.xml.internal.stream.events.AttributeImpl;
import com.sun.xml.internal.stream.events.CharacterEvent;
import com.sun.xml.internal.stream.events.CommentEvent;
import com.sun.xml.internal.stream.events.DTDEvent;
import com.sun.xml.internal.stream.events.DummyEvent;
import com.sun.xml.internal.stream.events.EndDocumentEvent;
import com.sun.xml.internal.stream.events.EndElementEvent;
import com.sun.xml.internal.stream.events.EntityDeclarationImpl;
import com.sun.xml.internal.stream.events.EntityReferenceEvent;
import com.sun.xml.internal.stream.events.NamespaceImpl;
import com.sun.xml.internal.stream.events.ProcessingInstructionEvent;
import com.sun.xml.internal.stream.events.StartDocumentEvent;
import com.sun.xml.internal.stream.events.StartElementEvent;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.stream.util.XMLEventConsumer;

public class XMLEventAllocatorImpl
implements XMLEventAllocator {
    @Override
    public XMLEvent allocate(XMLStreamReader xMLStreamReader) throws XMLStreamException {
        if (xMLStreamReader == null) {
            throw new XMLStreamException("Reader cannot be null");
        }
        return this.getXMLEvent(xMLStreamReader);
    }

    @Override
    public void allocate(XMLStreamReader xMLStreamReader, XMLEventConsumer xMLEventConsumer) throws XMLStreamException {
        XMLEvent currentEvent = this.getXMLEvent(xMLStreamReader);
        if (currentEvent != null) {
            xMLEventConsumer.add(currentEvent);
        }
    }

    @Override
    public XMLEventAllocator newInstance() {
        return new XMLEventAllocatorImpl();
    }

    XMLEvent getXMLEvent(XMLStreamReader streamReader) {
        DummyEvent event = null;
        int eventType = streamReader.getEventType();
        switch (eventType) {
            case 1: {
                StartElementEvent startElementEvent = new StartElementEvent(this.getQName(streamReader));
                this.fillAttributes(startElementEvent, streamReader);
                if (((Boolean)streamReader.getProperty("javax.xml.stream.isNamespaceAware")).booleanValue()) {
                    this.fillNamespaceAttributes(startElementEvent, streamReader);
                    this.setNamespaceContext(startElementEvent, streamReader);
                }
                startElementEvent.setLocation(streamReader.getLocation());
                event = startElementEvent;
                break;
            }
            case 2: {
                EndElementEvent endElementEvent = new EndElementEvent(this.getQName(streamReader));
                endElementEvent.setLocation(streamReader.getLocation());
                if (((Boolean)streamReader.getProperty("javax.xml.stream.isNamespaceAware")).booleanValue()) {
                    this.fillNamespaceAttributes(endElementEvent, streamReader);
                }
                event = endElementEvent;
                break;
            }
            case 3: {
                ProcessingInstructionEvent piEvent = new ProcessingInstructionEvent(streamReader.getPITarget(), streamReader.getPIData());
                piEvent.setLocation(streamReader.getLocation());
                event = piEvent;
                break;
            }
            case 4: {
                CharacterEvent cDataEvent = new CharacterEvent(streamReader.getText());
                cDataEvent.setLocation(streamReader.getLocation());
                event = cDataEvent;
                break;
            }
            case 5: {
                CommentEvent commentEvent = new CommentEvent(streamReader.getText());
                commentEvent.setLocation(streamReader.getLocation());
                event = commentEvent;
                break;
            }
            case 7: {
                StartDocumentEvent sdEvent = new StartDocumentEvent();
                sdEvent.setVersion(streamReader.getVersion());
                sdEvent.setEncoding(streamReader.getEncoding());
                if (streamReader.getCharacterEncodingScheme() != null) {
                    sdEvent.setDeclaredEncoding(true);
                } else {
                    sdEvent.setDeclaredEncoding(false);
                }
                sdEvent.setStandalone(streamReader.isStandalone(), streamReader.standaloneSet());
                sdEvent.setLocation(streamReader.getLocation());
                event = sdEvent;
                break;
            }
            case 8: {
                EndDocumentEvent endDocumentEvent = new EndDocumentEvent();
                endDocumentEvent.setLocation(streamReader.getLocation());
                event = endDocumentEvent;
                break;
            }
            case 9: {
                EntityReferenceEvent entityEvent = new EntityReferenceEvent(streamReader.getLocalName(), new EntityDeclarationImpl(streamReader.getLocalName(), streamReader.getText()));
                entityEvent.setLocation(streamReader.getLocation());
                event = entityEvent;
                break;
            }
            case 10: {
                event = null;
                break;
            }
            case 11: {
                List notations;
                DTDEvent dtdEvent = new DTDEvent(streamReader.getText());
                dtdEvent.setLocation(streamReader.getLocation());
                List entities = (List)streamReader.getProperty("javax.xml.stream.entities");
                if (entities != null && entities.size() != 0) {
                    dtdEvent.setEntities(entities);
                }
                if ((notations = (List)streamReader.getProperty("javax.xml.stream.notations")) != null && !notations.isEmpty()) {
                    dtdEvent.setNotations(notations);
                }
                event = dtdEvent;
                break;
            }
            case 12: {
                CharacterEvent cDataEvent = new CharacterEvent(streamReader.getText(), true);
                cDataEvent.setLocation(streamReader.getLocation());
                event = cDataEvent;
                break;
            }
            case 6: {
                CharacterEvent spaceEvent = new CharacterEvent(streamReader.getText(), false, true);
                spaceEvent.setLocation(streamReader.getLocation());
                event = spaceEvent;
                break;
            }
        }
        return event;
    }

    protected XMLEvent getNextEvent(XMLStreamReader streamReader) throws XMLStreamException {
        streamReader.next();
        return this.getXMLEvent(streamReader);
    }

    protected void fillAttributes(StartElementEvent event, XMLStreamReader xmlr) {
        int len = xmlr.getAttributeCount();
        QName qname = null;
        AttributeImpl attr = null;
        Object nattr = null;
        for (int i = 0; i < len; ++i) {
            qname = xmlr.getAttributeName(i);
            attr = new AttributeImpl();
            attr.setName(qname);
            attr.setAttributeType(xmlr.getAttributeType(i));
            attr.setSpecified(xmlr.isAttributeSpecified(i));
            attr.setValue(xmlr.getAttributeValue(i));
            event.addAttribute(attr);
        }
    }

    protected void fillNamespaceAttributes(StartElementEvent event, XMLStreamReader xmlr) {
        int count = xmlr.getNamespaceCount();
        String uri = null;
        String prefix = null;
        NamespaceImpl attr = null;
        for (int i = 0; i < count; ++i) {
            uri = xmlr.getNamespaceURI(i);
            prefix = xmlr.getNamespacePrefix(i);
            if (prefix == null) {
                prefix = "";
            }
            attr = new NamespaceImpl(prefix, uri);
            event.addNamespaceAttribute(attr);
        }
    }

    protected void fillNamespaceAttributes(EndElementEvent event, XMLStreamReader xmlr) {
        int count = xmlr.getNamespaceCount();
        String uri = null;
        String prefix = null;
        NamespaceImpl attr = null;
        for (int i = 0; i < count; ++i) {
            uri = xmlr.getNamespaceURI(i);
            prefix = xmlr.getNamespacePrefix(i);
            if (prefix == null) {
                prefix = "";
            }
            attr = new NamespaceImpl(prefix, uri);
            event.addNamespace(attr);
        }
    }

    private void setNamespaceContext(StartElementEvent event, XMLStreamReader xmlr) {
        NamespaceContextWrapper contextWrapper = (NamespaceContextWrapper)xmlr.getNamespaceContext();
        NamespaceSupport ns = new NamespaceSupport(contextWrapper.getNamespaceContext());
        event.setNamespaceContext(new NamespaceContextWrapper(ns));
    }

    private QName getQName(XMLStreamReader xmlr) {
        return new QName(xmlr.getNamespaceURI(), xmlr.getLocalName(), xmlr.getPrefix());
    }
}

