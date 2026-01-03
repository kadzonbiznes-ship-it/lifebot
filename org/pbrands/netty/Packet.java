/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import lombok.Generated;

public class Packet {
    private final byte opcode;
    private final ByteBuf payload;

    public Packet(byte opcode, ByteBuf payload) {
        this.opcode = opcode;
        this.payload = payload;
    }

    public ChannelFuture writeAndFlush(Channel ctx) {
        return ctx.writeAndFlush(this.getPacket());
    }

    public ChannelFuture writeAndFlush(ChannelHandlerContext ctx) {
        return ctx.writeAndFlush(this.getPacket());
    }

    public ChannelFuture write(Channel ctx) {
        return ctx.write(this.getPayload());
    }

    public ChannelFuture write(ChannelHandlerContext ctx) {
        return ctx.write(this.getPayload());
    }

    public ByteBuf getPacket() {
        ByteBuf packet = Unpooled.buffer();
        packet.writeByte(this.opcode);
        packet.writeInt(this.payload.readableBytes());
        packet.writeBytes(this.payload);
        return packet;
    }

    @Generated
    public byte getOpcode() {
        return this.opcode;
    }

    @Generated
    public ByteBuf getPayload() {
        return this.payload;
    }
}

