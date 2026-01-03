/*
 * Decompiled with CFR 0.152.
 */
package java.io;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

public class StringWriter
extends Writer {
    private final StringBuffer buf;

    public StringWriter() {
        this.buf = new StringBuffer();
        this.lock = this.buf;
    }

    public StringWriter(int initialSize) {
        if (initialSize < 0) {
            throw new IllegalArgumentException("Negative buffer size");
        }
        this.buf = new StringBuffer(initialSize);
        this.lock = this.buf;
    }

    @Override
    public void write(int c) {
        this.buf.append((char)c);
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        Objects.checkFromIndexSize(off, len, cbuf.length);
        if (len == 0) {
            return;
        }
        this.buf.append(cbuf, off, len);
    }

    @Override
    public void write(String str) {
        this.buf.append(str);
    }

    @Override
    public void write(String str, int off, int len) {
        this.buf.append(str, off, off + len);
    }

    @Override
    public StringWriter append(CharSequence csq) {
        this.write(String.valueOf(csq));
        return this;
    }

    @Override
    public StringWriter append(CharSequence csq, int start, int end) {
        if (csq == null) {
            csq = "null";
        }
        return this.append(csq.subSequence(start, end));
    }

    @Override
    public StringWriter append(char c) {
        this.write(c);
        return this;
    }

    public String toString() {
        return this.buf.toString();
    }

    public StringBuffer getBuffer() {
        return this.buf;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws IOException {
    }
}

