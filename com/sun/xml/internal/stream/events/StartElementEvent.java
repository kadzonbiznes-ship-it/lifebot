/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.internal.stream.events;

import com.sun.xml.internal.stream.events.DummyEvent;
import com.sun.xml.internal.stream.util.ReadOnlyIterator;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;

public class StartElementEvent
extends DummyEvent
implements StartElement {
    private Map<QName, Attribute> fAttributes;
    private List<Namespace> fNamespaces;
    private NamespaceContext fNamespaceContext = null;
    private QName fQName;

    public StartElementEvent(String prefix, String uri, String localpart) {
        this(new QName(uri, localpart, prefix));
    }

    public StartElementEvent(QName qname) {
        this.fQName = qname;
        this.init();
    }

    public StartElementEvent(StartElement startelement) {
        this(startelement.getName());
        this.addAttributes(startelement.getAttributes());
        this.addNamespaceAttributes(startelement.getNamespaces());
    }

    protected final void init() {
        this.setEventType(1);
        this.fAttributes = new HashMap<QName, Attribute>();
        this.fNamespaces = new ArrayList<Namespace>();
    }

    @Override
    public QName getName() {
        return this.fQName;
    }

    public void setName(QName qname) {
        this.fQName = qname;
    }

    @Override
    public Iterator<Attribute> getAttributes() {
        if (this.fAttributes != null) {
            Collection<Attribute> coll = this.fAttributes.values();
            return new ReadOnlyIterator<Attribute>(coll.iterator());
        }
        return new ReadOnlyIterator<Attribute>();
    }

    @Override
    public Iterator<Namespace> getNamespaces() {
        if (this.fNamespaces != null) {
            return new ReadOnlyIterator<Namespace>(this.fNamespaces.iterator());
        }
        return new ReadOnlyIterator<Namespace>();
    }

    @Override
    public Attribute getAttributeByName(QName qname) {
        if (qname == null) {
            return null;
        }
        return this.fAttributes.get(qname);
    }

    public String getNamespace() {
        return this.fQName.getNamespaceURI();
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (this.getNamespace() != null && this.fQName.getPrefix().equals(prefix)) {
            return this.getNamespace();
        }
        if (this.fNamespaceContext != null) {
            return this.fNamespaceContext.getNamespaceURI(prefix);
        }
        return null;
    }

    public String toString() {
        StringBuilder startElement = new StringBuilder();
        startElement.append("<");
        startElement.append(this.nameAsString());
        if (this.fAttributes != null) {
            Iterator<Attribute> it = this.getAttributes();
            while (it.hasNext()) {
                Attribute attr = it.next();
                startElement.append(" ");
                startElement.append(attr.toString());
            }
        }
        if (this.fNamespaces != null) {
            for (Namespace ns : this.fNamespaces) {
                startElement.append(" ");
                startElement.append(ns.toString());
            }
        }
        startElement.append(">");
        return startElement.toString();
    }

    public String nameAsString() {
        if ("".equals(this.fQName.getNamespaceURI())) {
            return this.fQName.getLocalPart();
        }
        if (this.fQName.getPrefix() != null) {
            return "['" + this.fQName.getNamespaceURI() + "']:" + this.fQName.getPrefix() + ":" + this.fQName.getLocalPart();
        }
        return "['" + this.fQName.getNamespaceURI() + "']:" + this.fQName.getLocalPart();
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return this.fNamespaceContext;
    }

    public void setNamespaceContext(NamespaceContext nc) {
        this.fNamespaceContext = nc;
    }

    @Override
    protected void writeAsEncodedUnicodeEx(Writer writer) throws IOException {
        writer.write(this.toString());
    }

    void addAttribute(Attribute attr) {
        if (attr.isNamespace()) {
            this.fNamespaces.add((Namespace)attr);
        } else {
            this.fAttributes.put(attr.getName(), attr);
        }
    }

    final void addAttributes(Iterator<? extends Attribute> attrs) {
        if (attrs == null) {
            return;
        }
        while (attrs.hasNext()) {
            Attribute attr = attrs.next();
            this.fAttributes.put(attr.getName(), attr);
        }
    }

    void addNamespaceAttribute(Namespace attr) {
        if (attr == null) {
            return;
        }
        this.fNamespaces.add(attr);
    }

    final void addNamespaceAttributes(Iterator<? extends Namespace> attrs) {
        if (attrs == null) {
            return;
        }
        while (attrs.hasNext()) {
            Namespace attr = attrs.next();
            this.fNamespaces.add(attr);
        }
    }
}

