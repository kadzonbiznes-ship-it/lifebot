/*
 * Decompiled with CFR 0.152.
 */
package com.sun.org.apache.xerces.internal.xni;

public class XMLString {
    public static final int DEFAULT_SIZE = 32;
    public char[] ch;
    public int offset;
    public int length;

    public XMLString() {
    }

    public XMLString(char[] ch, int offset, int length) {
        this.setValues(ch, offset, length);
    }

    public XMLString(XMLString string) {
        this.setValues(string);
    }

    public void setValues(char[] ch, int offset, int length) {
        this.ch = ch;
        this.offset = offset;
        this.length = length;
    }

    public void setValues(XMLString s) {
        this.setValues(s.ch, s.offset, s.length);
    }

    public void clear() {
        this.ch = null;
        this.offset = 0;
        this.length = -1;
    }

    public boolean equals(char[] ch, int offset, int length) {
        if (ch == null) {
            return false;
        }
        if (this.length != length) {
            return false;
        }
        for (int i = 0; i < length; ++i) {
            if (this.ch[this.offset + i] == ch[offset + i]) continue;
            return false;
        }
        return true;
    }

    public boolean equals(String s) {
        if (s == null) {
            return false;
        }
        if (this.length != s.length()) {
            return false;
        }
        for (int i = 0; i < this.length; ++i) {
            if (this.ch[this.offset + i] == s.charAt(i)) continue;
            return false;
        }
        return true;
    }

    public String toString() {
        return this.length > 0 ? new String(this.ch, this.offset, this.length) : "";
    }

    public void append(char c) {
        if (this.length + 1 > this.ch.length) {
            int newLength = this.ch.length * 2;
            if (newLength < this.ch.length + 32) {
                newLength = this.ch.length + 32;
            }
            char[] tmp = new char[newLength];
            System.arraycopy(this.ch, 0, tmp, 0, this.length);
            this.ch = tmp;
        }
        this.ch[this.length] = c;
        ++this.length;
    }

    public void append(String s) {
        int length = s.length();
        if (this.length + length > this.ch.length) {
            int newLength = this.ch.length * 2;
            if (newLength < this.ch.length + length + 32) {
                newLength = this.ch.length + length + 32;
            }
            char[] newch = new char[newLength];
            System.arraycopy(this.ch, 0, newch, 0, this.length);
            this.ch = newch;
        }
        s.getChars(0, length, this.ch, this.length);
        this.length += length;
    }

    public void append(char[] ch, int offset, int length) {
        if (this.length + length > this.ch.length) {
            int newLength = this.ch.length * 2;
            if (newLength < this.ch.length + length + 32) {
                newLength = this.ch.length + length + 32;
            }
            char[] newch = new char[newLength];
            System.arraycopy(this.ch, 0, newch, 0, this.length);
            this.ch = newch;
        }
        if (ch != null && length > 0) {
            System.arraycopy(ch, offset, this.ch, this.length, length);
            this.length += length;
        }
    }

    public void append(XMLString s) {
        this.append(s.ch, s.offset, s.length);
    }
}

