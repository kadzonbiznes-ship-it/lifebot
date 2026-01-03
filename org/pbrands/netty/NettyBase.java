/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.pbrands.util.UUIDUtil
 */
package org.pbrands.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.Generated;
import org.pbrands.model.PacketType;
import org.pbrands.model.ProductType;
import org.pbrands.netty.ClientInitializer;
import org.pbrands.netty.ConnectionStatus;
import org.pbrands.netty.Packet;
import org.pbrands.netty.handler.ClientHandler;
import org.pbrands.util.HWID;
import org.pbrands.util.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NettyBase<T extends ChannelInboundHandlerAdapter> {
    private static final Logger logger = LoggerFactory.getLogger(NettyBase.class);
    protected ConnectionStatus connectionStatus;
    protected int connectionTimeout = 2500;
    protected final String host;
    protected final int port;
    protected String token;
    protected final String hwid;
    protected final ProductType productType;
    protected Bootstrap bootstrap;
    protected Channel channel;
    protected final EventLoopGroup group;
    protected T clientHandler;

    public NettyBase(String host, int port, String token, String hwid, ProductType productType) {
        this.host = host;
        this.port = port;
        this.token = token;
        this.hwid = hwid;
        this.productType = productType;
        this.group = new NioEventLoopGroup();
    }

    public void setToken(String token) {
        this.token = token;
    }

    public abstract boolean reconnectOnFailure();

    public abstract T createHandler();

    public void setConnectionTimeout(int connectionTimeout) {
        if (connectionTimeout <= 0) {
            throw new IllegalArgumentException("Connection timeout must be greater than 0");
        }
        this.connectionTimeout = connectionTimeout;
    }

    protected void configureBootstrap(Bootstrap bootstrap) {
    }

    protected void onConnected() {
    }

    public ChannelFuture start() throws InterruptedException {
        this.bootstrap = new Bootstrap();
        this.clientHandler = this.createHandler();
        ((Bootstrap)((Bootstrap)((Bootstrap)((Bootstrap)this.bootstrap.group(this.group)).channel(NioSocketChannel.class)).option(ChannelOption.TCP_NODELAY, true)).option(ChannelOption.SO_KEEPALIVE, true)).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.connectionTimeout);
        this.configureBootstrap(this.bootstrap);
        this.bootstrap.handler(new ClientInitializer<T>(this.token, this.hwid, this.productType, this.clientHandler));
        ChannelFuture connectFuture = this.bootstrap.connect(this.host, this.port).await();
        return this.getChannelFuture(connectFuture);
    }

    public ChannelFuture reconnect() throws InterruptedException {
        this.disconnect();
        this.clientHandler = this.createHandler();
        String currentHwid = HWID.generate();
        this.bootstrap.handler(new ClientInitializer<T>(this.token, currentHwid, this.productType, this.clientHandler));
        ChannelFuture connectFuture = this.bootstrap.connect(this.host, this.port).await();
        return this.getChannelFuture(connectFuture);
    }

    protected ChannelFuture getChannelFuture(ChannelFuture connectFuture) {
        if (connectFuture.isSuccess()) {
            this.channel = connectFuture.channel();
            this.connectionStatus = ConnectionStatus.CONNECTED;
            logger.info("Connected to {}:{}", (Object)this.host, (Object)this.port);
            this.onConnected();
            this.channel.closeFuture().addListener((GenericFutureListener<? extends Future<? super Void>>)((GenericFutureListener<Future>)cf -> {
                this.connectionStatus = ConnectionStatus.DISCONNECTED;
                logger.info("Disconnected from {}:{}", (Object)this.host, (Object)this.port);
                if (this.reconnectOnFailure()) {
                    this.scheduleReconnect();
                }
            }));
        } else {
            logger.error("Failed to connect to {}:{}", (Object)this.host, (Object)this.port);
            if (this.reconnectOnFailure()) {
                this.scheduleReconnect();
            }
        }
        return connectFuture;
    }

    protected void scheduleReconnect() {
        this.group.schedule(() -> {
            this.connectionStatus = ConnectionStatus.RECONNECTING;
            logger.info("Reconnecting to {}:{}", (Object)this.host, (Object)this.port);
            try {
                this.reconnect();
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, (long)this.connectionTimeout, TimeUnit.MILLISECONDS);
    }

    public boolean isConnected() {
        return this.channel != null && this.channel.isActive();
    }

    public ChannelFuture disconnect() {
        if (this.channel != null && this.channel.isOpen()) {
            try {
                return this.channel.close().await();
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public final CompletableFuture<Long> sendPing() {
        CompletableFuture<Long> future = new CompletableFuture<Long>();
        UUID uuid = UUID.randomUUID();
        ByteBuf payload = Unpooled.buffer();
        byte[] uuidBytes = UUIDUtil.getIdAsByte((UUID)uuid);
        payload.writeBytes(uuidBytes);
        Packet packet = new Packet(PacketType.PING.getOpcode(), payload);
        ((ClientHandler)this.clientHandler).registerPing(uuid, System.currentTimeMillis(), future);
        packet.writeAndFlush(this.channel);
        return future;
    }

    public String generateRequestId() {
        return UUID.randomUUID().toString();
    }

    @Generated
    public ConnectionStatus getConnectionStatus() {
        return this.connectionStatus;
    }

    @Generated
    public String getHwid() {
        return this.hwid;
    }

    @Generated
    public Channel getChannel() {
        return this.channel;
    }

    @Generated
    public T getClientHandler() {
        return this.clientHandler;
    }
}

