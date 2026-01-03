/*
 * Decompiled with CFR 0.152.
 */
package java.nio.channels;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProtocolFamily;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Objects;

public abstract class SocketChannel
extends AbstractSelectableChannel
implements ByteChannel,
ScatteringByteChannel,
GatheringByteChannel,
NetworkChannel {
    protected SocketChannel(SelectorProvider provider) {
        super(provider);
    }

    public static SocketChannel open() throws IOException {
        return SelectorProvider.provider().openSocketChannel();
    }

    public static SocketChannel open(ProtocolFamily family) throws IOException {
        return SelectorProvider.provider().openSocketChannel(Objects.requireNonNull(family));
    }

    public static SocketChannel open(SocketAddress remote) throws IOException {
        SocketChannel sc;
        Objects.requireNonNull(remote);
        if (remote instanceof InetSocketAddress) {
            sc = SocketChannel.open();
        } else if (remote instanceof UnixDomainSocketAddress) {
            sc = SocketChannel.open(StandardProtocolFamily.UNIX);
        } else {
            throw new UnsupportedAddressTypeException();
        }
        try {
            sc.connect(remote);
        }
        catch (Throwable x) {
            try {
                sc.close();
            }
            catch (Throwable suppressed) {
                x.addSuppressed(suppressed);
            }
            throw x;
        }
        assert (sc.isConnected());
        return sc;
    }

    @Override
    public final int validOps() {
        return 13;
    }

    @Override
    public abstract SocketChannel bind(SocketAddress var1) throws IOException;

    @Override
    public abstract <T> SocketChannel setOption(SocketOption<T> var1, T var2) throws IOException;

    public abstract SocketChannel shutdownInput() throws IOException;

    public abstract SocketChannel shutdownOutput() throws IOException;

    public abstract Socket socket();

    public abstract boolean isConnected();

    public abstract boolean isConnectionPending();

    public abstract boolean connect(SocketAddress var1) throws IOException;

    public abstract boolean finishConnect() throws IOException;

    public abstract SocketAddress getRemoteAddress() throws IOException;

    @Override
    public abstract int read(ByteBuffer var1) throws IOException;

    @Override
    public abstract long read(ByteBuffer[] var1, int var2, int var3) throws IOException;

    @Override
    public final long read(ByteBuffer[] dsts) throws IOException {
        return this.read(dsts, 0, dsts.length);
    }

    @Override
    public abstract int write(ByteBuffer var1) throws IOException;

    @Override
    public abstract long write(ByteBuffer[] var1, int var2, int var3) throws IOException;

    @Override
    public final long write(ByteBuffer[] srcs) throws IOException {
        return this.write(srcs, 0, srcs.length);
    }

    @Override
    public abstract SocketAddress getLocalAddress() throws IOException;
}

