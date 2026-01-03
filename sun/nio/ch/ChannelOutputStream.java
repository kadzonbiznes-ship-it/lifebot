/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;

class ChannelOutputStream
extends OutputStream {
    private final WritableByteChannel ch;
    private ByteBuffer bb;
    private byte[] bs;
    private byte[] b1;

    ChannelOutputStream(WritableByteChannel ch) {
        this.ch = ch;
    }

    WritableByteChannel channel() {
        return this.ch;
    }

    private void writeFully(ByteBuffer bb) throws IOException {
        while (bb.remaining() > 0) {
            int n = this.ch.write(bb);
            if (n > 0) continue;
            throw new RuntimeException("no bytes written");
        }
    }

    @Override
    public synchronized void write(int b) throws IOException {
        if (this.b1 == null) {
            this.b1 = new byte[1];
        }
        this.b1[0] = (byte)b;
        this.write(this.b1);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized void write(byte[] bs, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, bs.length);
        if (len == 0) {
            return;
        }
        ByteBuffer bb = this.bs == bs ? this.bb : ByteBuffer.wrap(bs);
        bb.limit(Math.min(off + len, bb.capacity()));
        bb.position(off);
        this.bb = bb;
        this.bs = bs;
        Object object = this.ch;
        if (object instanceof SelectableChannel) {
            SelectableChannel sc = (SelectableChannel)object;
            object = sc.blockingLock();
            synchronized (object) {
                if (!sc.isBlocking()) {
                    throw new IllegalBlockingModeException();
                }
                this.writeFully(bb);
            }
        } else {
            this.writeFully(bb);
        }
    }

    @Override
    public void close() throws IOException {
        this.ch.close();
    }
}

