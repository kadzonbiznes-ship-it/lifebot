/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.nio.charset.StandardCharsets;
import org.pbrands.model.ChannelAttributes;
import org.pbrands.model.PacketType;
import org.pbrands.netty.Packet;

public class ClientAuthHandler
extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String token = ctx.channel().attr(ChannelAttributes.TOKEN).get();
        String hwid = ctx.channel().attr(ChannelAttributes.HWID).get();
        if (token != null && !token.isEmpty()) {
            byte[] tokenBytes = token.getBytes(StandardCharsets.UTF_8);
            byte[] hwidBytes = hwid.getBytes(StandardCharsets.UTF_8);
            ByteBuf payload = Unpooled.buffer();
            payload.writeInt(tokenBytes.length);
            payload.writeBytes(tokenBytes);
            payload.writeInt(hwidBytes.length);
            payload.writeBytes(hwidBytes);
            Packet packet = new Packet(PacketType.AUTH.getOpcode(), payload);
            packet.writeAndFlush(ctx);
        }
        ctx.pipeline().remove(this);
    }
}

