/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.util;

import com.sun.org.apache.xerces.internal.xni.XMLString;

public class XMLStringBuffer
extends XMLString {
    public XMLStringBuffer() {
        this(32);
    }

    public XMLStringBuffer(int size) {
        this.ch = new char[size];
    }

    public XMLStringBuffer(char c) {
        this(1);
        this.append(c);
    }

    public XMLStringBuffer(String s) {
        this(s.length());
        this.append(s);
    }

    public XMLStringBuffer(char[] ch, int offset, int length) {
        this(length);
        this.append(ch, offset, length);
    }

    public XMLStringBuffer(XMLString s) {
        this(s.length);
        this.append(s);
    }

    @Override
    public void clear() {
        this.offset = 0;
        this.length = 0;
    }
}

