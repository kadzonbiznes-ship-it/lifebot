/*
 * Decompiled with CFR 0.152.
 */
package java.nio.channels;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ProtocolFamily;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.MulticastChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Objects;

public abstract class DatagramChannel
extends AbstractSelectableChannel
implements ByteChannel,
ScatteringByteChannel,
GatheringByteChannel,
MulticastChannel {
    protected DatagramChannel(SelectorProvider provider) {
        super(provider);
    }

    public static DatagramChannel open() throws IOException {
        return SelectorProvider.provider().openDatagramChannel();
    }

    public static DatagramChannel open(ProtocolFamily family) throws IOException {
        return SelectorProvider.provider().openDatagramChannel(Objects.requireNonNull(family));
    }

    @Override
    public final int validOps() {
        return 5;
    }

    @Override
    public abstract DatagramChannel bind(SocketAddress var1) throws IOException;

    @Override
    public abstract <T> DatagramChannel setOption(SocketOption<T> var1, T var2) throws IOException;

    public abstract DatagramSocket socket();

    public abstract boolean isConnected();

    public abstract DatagramChannel connect(SocketAddress var1) throws IOException;

    public abstract DatagramChannel disconnect() throws IOException;

    public abstract SocketAddress getRemoteAddress() throws IOException;

    public abstract SocketAddress receive(ByteBuffer var1) throws IOException;

    public abstract int send(ByteBuffer var1, SocketAddress var2) throws IOException;

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

