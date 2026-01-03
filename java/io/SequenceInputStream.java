/*
 * Decompiled with CFR 0.152.
 */
package java.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;

public class SequenceInputStream
extends InputStream {
    private final Enumeration<? extends InputStream> e;
    private InputStream in;

    public SequenceInputStream(Enumeration<? extends InputStream> e) {
        this.e = e;
        this.peekNextStream();
    }

    public SequenceInputStream(InputStream s1, InputStream s2) {
        this(Collections.enumeration(Arrays.asList(s1, s2)));
    }

    final void nextStream() throws IOException {
        if (this.in != null) {
            this.in.close();
        }
        this.peekNextStream();
    }

    private void peekNextStream() {
        if (this.e.hasMoreElements()) {
            this.in = this.e.nextElement();
            if (this.in == null) {
                throw new NullPointerException();
            }
        } else {
            this.in = null;
        }
    }

    @Override
    public int available() throws IOException {
        if (this.in == null) {
            return 0;
        }
        return this.in.available();
    }

    @Override
    public int read() throws IOException {
        while (this.in != null) {
            int c = this.in.read();
            if (c != -1) {
                return c;
            }
            this.nextStream();
        }
        return -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (this.in == null) {
            return -1;
        }
        if (b == null) {
            throw new NullPointerException();
        }
        Objects.checkFromIndexSize(off, len, b.length);
        if (len == 0) {
            return 0;
        }
        do {
            int n;
            if ((n = this.in.read(b, off, len)) > 0) {
                return n;
            }
            this.nextStream();
        } while (this.in != null);
        return -1;
    }

    @Override
    public void close() throws IOException {
        IOException ioe = null;
        while (this.in != null) {
            try {
                this.in.close();
            }
            catch (IOException e) {
                if (ioe == null) {
                    ioe = e;
                }
                ioe.addSuppressed(e);
            }
            this.peekNextStream();
        }
        if (ioe != null) {
            throw ioe;
        }
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        Objects.requireNonNull(out, "out");
        if (this.getClass() == SequenceInputStream.class) {
            long transferred = 0L;
            while (this.in != null) {
                if (transferred < Long.MAX_VALUE) {
                    try {
                        transferred = Math.addExact(transferred, this.in.transferTo(out));
                    }
                    catch (ArithmeticException ignore) {
                        return Long.MAX_VALUE;
                    }
                }
                this.nextStream();
            }
            return transferred;
        }
        return super.transferTo(out);
    }
}

