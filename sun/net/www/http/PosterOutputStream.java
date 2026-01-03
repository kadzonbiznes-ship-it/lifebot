/*
 * Decompiled with CFR 0.152.
 */
package sun.net.www.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PosterOutputStream
extends ByteArrayOutputStream {
    private boolean closed;

    public PosterOutputStream() {
        super(256);
    }

    @Override
    public synchronized void write(int b) {
        if (this.closed) {
            return;
        }
        super.write(b);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {
        if (this.closed) {
            return;
        }
        super.write(b, off, len);
    }

    @Override
    public synchronized void reset() {
        if (this.closed) {
            return;
        }
        super.reset();
    }

    @Override
    public synchronized void close() throws IOException {
        this.closed = true;
        super.close();
    }
}

