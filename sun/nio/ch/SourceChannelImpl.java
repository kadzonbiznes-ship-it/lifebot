/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.Pipe;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Objects;
import sun.nio.ch.Net;
import sun.nio.ch.SelChImpl;
import sun.nio.ch.SelectionKeyImpl;
import sun.nio.ch.SocketChannelImpl;
import sun.nio.ch.Util;

class SourceChannelImpl
extends Pipe.SourceChannel
implements SelChImpl {
    private final SocketChannel sc;

    @Override
    public FileDescriptor getFD() {
        return ((SocketChannelImpl)this.sc).getFD();
    }

    @Override
    public int getFDVal() {
        return ((SocketChannelImpl)this.sc).getFDVal();
    }

    SourceChannelImpl(SelectorProvider sp, SocketChannel sc) {
        super(sp);
        this.sc = sc;
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException {
        if (!this.isRegistered()) {
            this.kill();
        }
    }

    @Override
    public void kill() throws IOException {
        this.sc.close();
    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {
        this.sc.configureBlocking(block);
    }

    public boolean translateReadyOps(int ops, int initialOps, SelectionKeyImpl ski) {
        int intOps = ski.nioInterestOps();
        int oldOps = ski.nioReadyOps();
        int newOps = initialOps;
        if ((ops & Net.POLLNVAL) != 0) {
            throw new Error("POLLNVAL detected");
        }
        if ((ops & (Net.POLLERR | Net.POLLHUP)) != 0) {
            newOps = intOps;
            ski.nioReadyOps(newOps);
            return (newOps & ~oldOps) != 0;
        }
        if ((ops & Net.POLLIN) != 0 && (intOps & 1) != 0) {
            newOps |= 1;
        }
        ski.nioReadyOps(newOps);
        return (newOps & ~oldOps) != 0;
    }

    @Override
    public boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl ski) {
        return this.translateReadyOps(ops, ski.nioReadyOps(), ski);
    }

    @Override
    public boolean translateAndSetReadyOps(int ops, SelectionKeyImpl ski) {
        return this.translateReadyOps(ops, 0, ski);
    }

    @Override
    public int translateInterestOps(int ops) {
        int newOps = 0;
        if ((ops & 1) != 0) {
            newOps |= Net.POLLIN;
        }
        return newOps;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        try {
            return this.sc.read(dst);
        }
        catch (AsynchronousCloseException x) {
            this.close();
            throw x;
        }
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, dsts.length);
        try {
            return this.read(Util.subsequence(dsts, offset, length));
        }
        catch (AsynchronousCloseException x) {
            this.close();
            throw x;
        }
    }

    @Override
    public long read(ByteBuffer[] dsts) throws IOException {
        try {
            return this.sc.read(dsts);
        }
        catch (AsynchronousCloseException x) {
            this.close();
            throw x;
        }
    }
}

