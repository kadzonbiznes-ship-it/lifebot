/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.common;

import java.io.IOException;
import java.io.InputStream;
import javax.imageio.stream.ImageInputStream;

public class InputStreamAdapter
extends InputStream {
    ImageInputStream stream;

    public InputStreamAdapter(ImageInputStream stream) {
        this.stream = stream;
    }

    @Override
    public int read() throws IOException {
        return this.stream.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return this.stream.read(b, off, len);
    }
}

