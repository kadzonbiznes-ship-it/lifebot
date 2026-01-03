/*
 * Decompiled with CFR 0.152.
 */
package java.io;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Objects;

public class CharArrayWriter
extends Writer {
    protected char[] buf;
    protected int count;

    public CharArrayWriter() {
        this(32);
    }

    public CharArrayWriter(int initialSize) {
        if (initialSize < 0) {
            throw new IllegalArgumentException("Negative initial size: " + initialSize);
        }
        this.buf = new char[initialSize];
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(int c) {
        Object object = this.lock;
        synchronized (object) {
            int newcount = this.count + 1;
            if (newcount > this.buf.length) {
                this.buf = Arrays.copyOf(this.buf, Math.max(this.buf.length << 1, newcount));
            }
            this.buf[this.count] = (char)c;
            this.count = newcount;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(char[] c, int off, int len) {
        Objects.checkFromIndexSize(off, len, c.length);
        if (len == 0) {
            return;
        }
        Object object = this.lock;
        synchronized (object) {
            int newcount = this.count + len;
            if (newcount > this.buf.length) {
                this.buf = Arrays.copyOf(this.buf, Math.max(this.buf.length << 1, newcount));
            }
            System.arraycopy(c, off, this.buf, this.count, len);
            this.count = newcount;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(String str, int off, int len) {
        Object object = this.lock;
        synchronized (object) {
            int newcount = this.count + len;
            if (newcount > this.buf.length) {
                this.buf = Arrays.copyOf(this.buf, Math.max(this.buf.length << 1, newcount));
            }
            str.getChars(off, off + len, this.buf, this.count);
            this.count = newcount;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void writeTo(Writer out) throws IOException {
        Object object = this.lock;
        synchronized (object) {
            out.write(this.buf, 0, this.count);
        }
    }

    @Override
    public CharArrayWriter append(CharSequence csq) {
        String s = String.valueOf(csq);
        this.write(s, 0, s.length());
        return this;
    }

    @Override
    public CharArrayWriter append(CharSequence csq, int start, int end) {
        if (csq == null) {
            csq = "null";
        }
        return this.append(csq.subSequence(start, end));
    }

    @Override
    public CharArrayWriter append(char c) {
        this.write(c);
        return this;
    }

    public void reset() {
        this.count = 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public char[] toCharArray() {
        Object object = this.lock;
        synchronized (object) {
            return Arrays.copyOf(this.buf, this.count);
        }
    }

    public int size() {
        return this.count;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String toString() {
        Object object = this.lock;
        synchronized (object) {
            return new String(this.buf, 0, this.count);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}

