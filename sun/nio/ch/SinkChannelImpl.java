/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.SocketOption;
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

class SinkChannelImpl
extends Pipe.SinkChannel
implements SelChImpl {
    private final SocketChannelImpl sc;

    @Override
    public FileDescriptor getFD() {
        return this.sc.getFD();
    }

    @Override
    public int getFDVal() {
        return this.sc.getFDVal();
    }

    SinkChannelImpl(SelectorProvider sp, SocketChannel sc) {
        super(sp);
        this.sc = (SocketChannelImpl)sc;
    }

    boolean isNetSocket() {
        return this.sc.isNetSocket();
    }

    <T> void setOption(SocketOption<T> name, T value) throws IOException {
        this.sc.setOption((SocketOption)name, (Object)value);
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
        if ((ops & Net.POLLOUT) != 0 && (intOps & 4) != 0) {
            newOps |= 4;
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
        if ((ops & 4) != 0) {
            newOps |= Net.POLLOUT;
        }
        return newOps;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        try {
            return this.sc.write(src);
        }
        catch (AsynchronousCloseException x) {
            this.close();
            throw x;
        }
    }

    @Override
    public long write(ByteBuffer[] srcs) throws IOException {
        try {
            return this.sc.write(srcs);
        }
        catch (AsynchronousCloseException x) {
            this.close();
            throw x;
        }
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, srcs.length);
        try {
            return this.write(Util.subsequence(srcs, offset, length));
        }
        catch (AsynchronousCloseException x) {
            this.close();
            throw x;
        }
    }
}

