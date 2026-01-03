/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import org.pbrands.model.PacketType;
import org.pbrands.model.ProductType;
import org.pbrands.netty.NettyBase;
import org.pbrands.netty.Packet;
import org.pbrands.netty.handler.ClientHandler;
import org.pbrands.util.HWID;
import org.pbrands.util.HardwareInfoCollector;

public abstract class ProductNettyBase<T extends ChannelInboundHandlerAdapter>
extends NettyBase<T> {
    private final HardwareInfoCollector hardwareInfoCollector = new HardwareInfoCollector();

    public ProductNettyBase(String host, int port, String token, String hwid, ProductType productType) {
        super(host, port, token, hwid, productType);
    }

    @Override
    protected void onConnected() {
        this.selectProduct(this.productType);
        this.sendHardwareInfo();
    }

    public final ChannelFuture selectProduct(ProductType productType) {
        ByteBuf payload = Unpooled.buffer();
        payload.writeByte(productType.getProductId());
        Packet packet = new Packet(PacketType.PRODUCT.getOpcode(), payload);
        return packet.writeAndFlush(this.channel);
    }

    public final void sendHardwareInfo() {
        ByteBuf payload = Unpooled.buffer();
        String rawHWID = HWID.getRawHWID();
        byte[] rawHWIDBytes = rawHWID.getBytes();
        payload.writeInt(rawHWIDBytes.length);
        payload.writeBytes(rawHWIDBytes);
        String cpuInfo = this.hardwareInfoCollector.getCPUInfo();
        byte[] cpuBytes = cpuInfo.getBytes(StandardCharsets.UTF_8);
        payload.writeInt(cpuBytes.length);
        payload.writeBytes(cpuBytes);
        String gpuInfo = this.hardwareInfoCollector.getGPUInfo();
        byte[] gpuBytes = gpuInfo.getBytes(StandardCharsets.UTF_8);
        payload.writeInt(gpuBytes.length);
        payload.writeBytes(gpuBytes);
        int ram = (int)this.hardwareInfoCollector.getRAMSize();
        payload.writeInt(ram);
        Packet packet = new Packet(PacketType.HARDWARE_INFO.getOpcode(), payload);
        packet.writeAndFlush(this.channel);
    }

    public CompletableFuture<byte[]> requestDLL() {
        CompletableFuture<byte[]> future = new CompletableFuture<byte[]>();
        ByteBuf payload = Unpooled.buffer();
        Packet packet = new Packet(PacketType.DLL.getOpcode(), payload);
        ((ClientHandler)this.getClientHandler()).registerDLLFuture(future);
        packet.writeAndFlush(this.channel);
        return future;
    }

    public CompletableFuture<String> requestDLLKey() {
        CompletableFuture<String> future = new CompletableFuture<String>();
        ByteBuf payload = Unpooled.buffer();
        Packet packet = new Packet(PacketType.DLL_KEY.getOpcode(), payload);
        ((ClientHandler)this.getClientHandler()).registerDLLKeyFuture(future);
        packet.writeAndFlush(this.channel);
        return future;
    }

    public CompletableFuture<byte[]> requestDLLUber() {
        CompletableFuture<byte[]> future = new CompletableFuture<byte[]>();
        ByteBuf payload = Unpooled.buffer();
        Packet packet = new Packet(PacketType.DLL_UBER.getOpcode(), payload);
        ((ClientHandler)this.getClientHandler()).registerDLLUberFuture(future);
        packet.writeAndFlush(this.channel);
        return future;
    }

    public CompletableFuture<String> requestDLLUberKey() {
        CompletableFuture<String> future = new CompletableFuture<String>();
        ByteBuf payload = Unpooled.buffer();
        Packet packet = new Packet(PacketType.DLL_UBER_KEY.getOpcode(), payload);
        ((ClientHandler)this.getClientHandler()).registerDLLUberKeyFuture(future);
        packet.writeAndFlush(this.channel);
        return future;
    }

    public CompletableFuture<byte[]> requestLoaderDLL() {
        CompletableFuture<byte[]> future = new CompletableFuture<byte[]>();
        ByteBuf payload = Unpooled.buffer();
        Packet packet = new Packet(PacketType.LOADER_DLL.getOpcode(), payload);
        ((ClientHandler)this.getClientHandler()).registerLoaderDLLFuture(future);
        packet.writeAndFlush(this.channel);
        return future;
    }
}

