/*
 * Decompiled with CFR 0.152.
 */
package java.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;
import jdk.internal.misc.InternalLock;

public class PushbackInputStream
extends FilterInputStream {
    private final InternalLock closeLock;
    protected byte[] buf;
    protected int pos;

    private void ensureOpen() throws IOException {
        if (this.in == null) {
            throw new IOException("Stream closed");
        }
    }

    public PushbackInputStream(InputStream in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException("size <= 0");
        }
        this.buf = new byte[size];
        this.pos = size;
        this.closeLock = this.getClass() == PushbackInputStream.class ? InternalLock.newLockOrNull() : null;
    }

    public PushbackInputStream(InputStream in) {
        this(in, 1);
    }

    @Override
    public int read() throws IOException {
        this.ensureOpen();
        if (this.pos < this.buf.length) {
            return this.buf[this.pos++] & 0xFF;
        }
        return super.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        this.ensureOpen();
        if (b == null) {
            throw new NullPointerException();
        }
        Objects.checkFromIndexSize(off, len, b.length);
        if (len == 0) {
            return 0;
        }
        int avail = this.buf.length - this.pos;
        if (avail > 0) {
            if (len < avail) {
                avail = len;
            }
            System.arraycopy(this.buf, this.pos, b, off, avail);
            this.pos += avail;
            off += avail;
            len -= avail;
        }
        if (len > 0) {
            if ((len = super.read(b, off, len)) == -1) {
                return avail == 0 ? -1 : avail;
            }
            return avail + len;
        }
        return avail;
    }

    public void unread(int b) throws IOException {
        this.ensureOpen();
        if (this.pos == 0) {
            throw new IOException("Push back buffer is full");
        }
        this.buf[--this.pos] = (byte)b;
    }

    public void unread(byte[] b, int off, int len) throws IOException {
        this.ensureOpen();
        if (len > this.pos) {
            throw new IOException("Push back buffer is full");
        }
        this.pos -= len;
        System.arraycopy(b, off, this.buf, this.pos, len);
    }

    public void unread(byte[] b) throws IOException {
        this.unread(b, 0, b.length);
    }

    @Override
    public int available() throws IOException {
        this.ensureOpen();
        int n = this.buf.length - this.pos;
        int avail = super.available();
        return n > Integer.MAX_VALUE - avail ? Integer.MAX_VALUE : n + avail;
    }

    @Override
    public long skip(long n) throws IOException {
        this.ensureOpen();
        if (n <= 0L) {
            return 0L;
        }
        long pskip = this.buf.length - this.pos;
        if (pskip > 0L) {
            if (n < pskip) {
                pskip = n;
            }
            this.pos += (int)pskip;
            n -= pskip;
        }
        if (n > 0L) {
            pskip += super.skip(n);
        }
        return pskip;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void mark(int readlimit) {
    }

    @Override
    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() throws IOException {
        if (this.closeLock != null) {
            this.closeLock.lock();
            try {
                this.implClose();
            }
            finally {
                this.closeLock.unlock();
            }
        }
        PushbackInputStream pushbackInputStream = this;
        synchronized (pushbackInputStream) {
            this.implClose();
        }
    }

    private void implClose() throws IOException {
        if (this.in != null) {
            this.in.close();
            this.in = null;
            this.buf = null;
        }
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        Objects.requireNonNull(out, "out");
        this.ensureOpen();
        if (this.getClass() == PushbackInputStream.class) {
            int avail = this.buf.length - this.pos;
            if (avail > 0) {
                byte[] buffer = Arrays.copyOfRange(this.buf, this.pos, this.buf.length);
                out.write(buffer);
                this.pos = buffer.length;
            }
            try {
                return Math.addExact((long)avail, this.in.transferTo(out));
            }
            catch (ArithmeticException ignore) {
                return Long.MAX_VALUE;
            }
        }
        return super.transferTo(out);
    }
}

