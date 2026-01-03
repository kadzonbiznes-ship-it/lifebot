/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.util;

import com.sun.org.apache.xerces.internal.util.XMLAttributesImpl;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class XMLAttributesIteratorImpl
extends XMLAttributesImpl
implements Iterator<XMLAttributesImpl.Attribute> {
    protected int fCurrent = 0;
    protected XMLAttributesImpl.Attribute fLastReturnedItem;

    @Override
    public boolean hasNext() {
        return this.fCurrent < this.getLength();
    }

    @Override
    public XMLAttributesImpl.Attribute next() {
        if (this.hasNext()) {
            this.fLastReturnedItem = this.fAttributes[this.fCurrent++];
            return this.fLastReturnedItem;
        }
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
        if (this.fLastReturnedItem != this.fAttributes[this.fCurrent - 1]) {
            throw new IllegalStateException();
        }
        this.removeAttributeAt(this.fCurrent--);
    }

    @Override
    public void removeAllAttributes() {
        super.removeAllAttributes();
        this.fCurrent = 0;
    }
}

