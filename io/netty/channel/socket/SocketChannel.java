/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.socket.ServerSocketChannel
 */
package io.netty.channel.socket;

import io.netty.channel.socket.DuplexChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannelConfig;
import java.net.InetSocketAddress;

public interface SocketChannel
extends DuplexChannel {
    public ServerSocketChannel parent();

    @Override
    public SocketChannelConfig config();

    @Override
    public InetSocketAddress localAddress();

    @Override
    public InetSocketAddress remoteAddress();
}

