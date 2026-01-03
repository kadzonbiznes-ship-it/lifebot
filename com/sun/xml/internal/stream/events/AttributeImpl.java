/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.internal.stream.events;

import com.sun.xml.internal.stream.events.DummyEvent;
import java.io.IOException;
import java.io.Writer;
import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;

public class AttributeImpl
extends DummyEvent
implements Attribute {
    private String fValue;
    private String fNonNormalizedvalue;
    private QName fQName;
    private String fAttributeType = "CDATA";
    private boolean fIsSpecified;

    public AttributeImpl() {
        this.init();
    }

    public AttributeImpl(String name, String value) {
        this.init();
        this.fQName = new QName(name);
        this.fValue = value;
    }

    public AttributeImpl(String prefix, String name, String value) {
        this(prefix, null, name, value, null, null, false);
    }

    public AttributeImpl(String prefix, String uri, String localPart, String value, String type) {
        this(prefix, uri, localPart, value, null, type, false);
    }

    public AttributeImpl(String prefix, String uri, String localPart, String value, String nonNormalizedvalue, String type, boolean isSpecified) {
        this(new QName(uri, localPart, prefix), value, nonNormalizedvalue, type, isSpecified);
    }

    public AttributeImpl(QName qname, String value, String nonNormalizedvalue, String type, boolean isSpecified) {
        this.init();
        this.fQName = qname;
        this.fValue = value;
        if (type != null && !type.isEmpty()) {
            this.fAttributeType = type;
        }
        this.fNonNormalizedvalue = nonNormalizedvalue;
        this.fIsSpecified = isSpecified;
    }

    public String toString() {
        if (this.fQName.getPrefix() != null && this.fQName.getPrefix().length() > 0) {
            return this.fQName.getPrefix() + ":" + this.fQName.getLocalPart() + "='" + this.fValue + "'";
        }
        return this.fQName.getLocalPart() + "='" + this.fValue + "'";
    }

    public void setName(QName name) {
        this.fQName = name;
    }

    @Override
    public QName getName() {
        return this.fQName;
    }

    public void setValue(String value) {
        this.fValue = value;
    }

    @Override
    public String getValue() {
        return this.fValue;
    }

    public void setNonNormalizedValue(String nonNormalizedvalue) {
        this.fNonNormalizedvalue = nonNormalizedvalue;
    }

    public String getNonNormalizedValue() {
        return this.fNonNormalizedvalue;
    }

    public void setAttributeType(String attributeType) {
        this.fAttributeType = attributeType;
    }

    @Override
    public String getDTDType() {
        return this.fAttributeType;
    }

    public void setSpecified(boolean isSpecified) {
        this.fIsSpecified = isSpecified;
    }

    @Override
    public boolean isSpecified() {
        return this.fIsSpecified;
    }

    @Override
    protected void writeAsEncodedUnicodeEx(Writer writer) throws IOException {
        writer.write(this.toString());
    }

    protected void init() {
        this.setEventType(10);
    }
}

