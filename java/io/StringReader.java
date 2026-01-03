/*
 * Decompiled with CFR 0.152.
 */
package java.io;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

public class StringReader
extends Reader {
    private final int length;
    private String str;
    private int next = 0;
    private int mark = 0;

    public StringReader(String s) {
        this.length = s.length();
        this.str = s;
    }

    private void ensureOpen() throws IOException {
        if (this.str == null) {
            throw new IOException("Stream closed");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int read() throws IOException {
        Object object = this.lock;
        synchronized (object) {
            this.ensureOpen();
            if (this.next >= this.length) {
                return -1;
            }
            return this.str.charAt(this.next++);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        Object object = this.lock;
        synchronized (object) {
            this.ensureOpen();
            Objects.checkFromIndexSize(off, len, cbuf.length);
            if (len == 0) {
                return 0;
            }
            if (this.next >= this.length) {
                return -1;
            }
            int n = Math.min(this.length - this.next, len);
            this.str.getChars(this.next, this.next + n, cbuf, off);
            this.next += n;
            return n;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public long skip(long n) throws IOException {
        Object object = this.lock;
        synchronized (object) {
            this.ensureOpen();
            if (this.next >= this.length) {
                return 0L;
            }
            long r = Math.min((long)(this.length - this.next), n);
            r = Math.max((long)(-this.next), r);
            this.next += (int)r;
            return r;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean ready() throws IOException {
        Object object = this.lock;
        synchronized (object) {
            this.ensureOpen();
            return true;
        }
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void mark(int readAheadLimit) throws IOException {
        if (readAheadLimit < 0) {
            throw new IllegalArgumentException("Read-ahead limit < 0");
        }
        Object object = this.lock;
        synchronized (object) {
            this.ensureOpen();
            this.mark = this.next;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void reset() throws IOException {
        Object object = this.lock;
        synchronized (object) {
            this.ensureOpen();
            this.next = this.mark;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() {
        Object object = this.lock;
        synchronized (object) {
            this.str = null;
        }
    }
}

