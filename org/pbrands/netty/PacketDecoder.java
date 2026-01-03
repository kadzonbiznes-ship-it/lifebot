/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import org.pbrands.netty.Packet;

public class PacketDecoder
extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 5) {
            return;
        }
        in.markReaderIndex();
        byte packetType = in.readByte();
        int length = in.readInt();
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }
        ByteBuf payload = in.readBytes(length);
        Packet packet = new Packet(packetType, payload);
        out.add(packet);
    }
}

