/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import sun.awt.image.PNGImageDecoder;

class PNGFilterInputStream
extends FilterInputStream {
    PNGImageDecoder owner;
    public InputStream underlyingInputStream;

    public PNGFilterInputStream(PNGImageDecoder owner, InputStream is) {
        super(is);
        this.underlyingInputStream = this.in;
        this.owner = owner;
    }

    @Override
    public int available() throws IOException {
        return this.owner.limit - this.owner.pos + this.in.available();
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int read() throws IOException {
        if (this.owner.chunkLength <= 0 && !this.owner.getData()) {
            return -1;
        }
        --this.owner.chunkLength;
        return this.owner.inbuf[this.owner.chunkStart++] & 0xFF;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int st, int len) throws IOException {
        if (this.owner.chunkLength <= 0 && !this.owner.getData()) {
            return -1;
        }
        if (this.owner.chunkLength < len) {
            len = this.owner.chunkLength;
        }
        System.arraycopy(this.owner.inbuf, this.owner.chunkStart, b, st, len);
        this.owner.chunkLength -= len;
        this.owner.chunkStart += len;
        return len;
    }

    @Override
    public long skip(long n) throws IOException {
        int i = 0;
        while ((long)i < n && this.read() >= 0) {
            ++i;
        }
        return i;
    }
}

