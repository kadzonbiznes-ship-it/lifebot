/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.stream.events;

import javax.xml.namespace.QName;
import javax.xml.stream.events.XMLEvent;

public interface Attribute
extends XMLEvent {
    public QName getName();

    public String getValue();

    public String getDTDType();

    public boolean isSpecified();
}

