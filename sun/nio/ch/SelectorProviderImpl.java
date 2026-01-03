/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.IOException;
import java.net.ProtocolFamily;
import java.net.StandardProtocolFamily;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Objects;
import sun.nio.ch.DatagramChannelImpl;
import sun.nio.ch.Net;
import sun.nio.ch.PipeImpl;
import sun.nio.ch.ServerSocketChannelImpl;
import sun.nio.ch.SocketChannelImpl;
import sun.nio.ch.UnixDomainSockets;

public abstract class SelectorProviderImpl
extends SelectorProvider {
    @Override
    public DatagramChannel openDatagramChannel() throws IOException {
        return new DatagramChannelImpl((SelectorProvider)this, true);
    }

    public DatagramChannel openUninterruptibleDatagramChannel() throws IOException {
        return new DatagramChannelImpl((SelectorProvider)this, false);
    }

    @Override
    public DatagramChannel openDatagramChannel(ProtocolFamily family) throws IOException {
        return new DatagramChannelImpl(this, family, true);
    }

    @Override
    public Pipe openPipe() throws IOException {
        return new PipeImpl(this);
    }

    @Override
    public abstract AbstractSelector openSelector() throws IOException;

    @Override
    public ServerSocketChannel openServerSocketChannel() throws IOException {
        return new ServerSocketChannelImpl(this);
    }

    @Override
    public SocketChannel openSocketChannel() throws IOException {
        return new SocketChannelImpl(this);
    }

    @Override
    public SocketChannel openSocketChannel(ProtocolFamily family) throws IOException {
        Objects.requireNonNull(family, "'family' is null");
        if (family == StandardProtocolFamily.INET6 && !Net.isIPv6Available()) {
            throw new UnsupportedOperationException("IPv6 not available");
        }
        if (family == StandardProtocolFamily.INET || family == StandardProtocolFamily.INET6) {
            return new SocketChannelImpl(this, family);
        }
        if (family == StandardProtocolFamily.UNIX && UnixDomainSockets.isSupported()) {
            return new SocketChannelImpl(this, family);
        }
        throw new UnsupportedOperationException("Protocol family not supported");
    }

    @Override
    public ServerSocketChannel openServerSocketChannel(ProtocolFamily family) throws IOException {
        Objects.requireNonNull(family, "'family' is null");
        if (family == StandardProtocolFamily.INET6 && !Net.isIPv6Available()) {
            throw new UnsupportedOperationException("IPv6 not available");
        }
        if (family == StandardProtocolFamily.INET || family == StandardProtocolFamily.INET6) {
            return new ServerSocketChannelImpl(this, family);
        }
        if (family == StandardProtocolFamily.UNIX && UnixDomainSockets.isSupported()) {
            return new ServerSocketChannelImpl(this, family);
        }
        throw new UnsupportedOperationException("Protocol family not supported");
    }
}

