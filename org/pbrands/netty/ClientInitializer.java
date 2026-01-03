/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.pbrands.model.ChannelAttributes;
import org.pbrands.model.ProductType;
import org.pbrands.netty.PacketDecoder;
import org.pbrands.netty.handler.ClientAuthHandler;

public class ClientInitializer<T extends ChannelInboundHandlerAdapter>
extends ChannelInitializer<SocketChannel> {
    private final String token;
    private final String hwid;
    private final ProductType productType;
    private final T clientHandler;

    public ClientInitializer(String token, String hwid, ProductType productType, T clientHandler) {
        this.token = token;
        this.hwid = hwid;
        this.productType = productType;
        this.clientHandler = clientHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.attr(ChannelAttributes.TOKEN).set(this.token);
        ch.attr(ChannelAttributes.HWID).set(this.hwid);
        ch.attr(ChannelAttributes.PRODUCT_ID).set(this.productType);
        ch.pipeline().addLast(new PacketDecoder());
        ch.pipeline().addLast(new ClientAuthHandler());
        ch.pipeline().addLast(new ChannelHandler[]{this.clientHandler});
    }
}

