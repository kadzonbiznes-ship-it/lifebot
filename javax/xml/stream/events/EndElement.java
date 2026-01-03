/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.stream.events;

import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.XMLEvent;

public interface EndElement
extends XMLEvent {
    public QName getName();

    public Iterator<Namespace> getNamespaces();
}

