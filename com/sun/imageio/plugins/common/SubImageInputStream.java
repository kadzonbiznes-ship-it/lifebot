/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.common;

import java.io.IOException;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

public final class SubImageInputStream
extends ImageInputStreamImpl {
    ImageInputStream stream;
    long startingPos;
    int startingLength;
    int length;

    public SubImageInputStream(ImageInputStream stream, int length) throws IOException {
        this.stream = stream;
        this.startingPos = stream.getStreamPosition();
        this.startingLength = this.length = length;
    }

    @Override
    public int read() throws IOException {
        if (this.length == 0) {
            return -1;
        }
        --this.length;
        return this.stream.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (this.length == 0) {
            return -1;
        }
        len = Math.min(len, this.length);
        int bytes = this.stream.read(b, off, len);
        this.length -= bytes;
        return bytes;
    }

    @Override
    public long length() {
        return this.startingLength;
    }

    @Override
    public void seek(long pos) throws IOException {
        this.stream.seek(pos - this.startingPos);
        this.streamPos = pos;
    }

    @Override
    protected void finalize() throws Throwable {
    }
}

