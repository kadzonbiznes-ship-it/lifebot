/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.pbrands.model.DisconnectReason
 *  org.pbrands.netty.handler.FishingBotClientHandler$PingRequest
 *  org.pbrands.util.UUIDUtil
 */
package org.pbrands.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.awt.Toolkit;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import org.pbrands.model.DisconnectReason;
import org.pbrands.model.PacketType;
import org.pbrands.netty.Packet;
import org.pbrands.netty.handler.ClientHandler;
import org.pbrands.netty.handler.FishingBotClientHandler;
import org.pbrands.util.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FishingBotClientHandler
extends ChannelInboundHandlerAdapter
implements ClientHandler {
    private static final Logger logger = LoggerFactory.getLogger(FishingBotClientHandler.class);
    private final Map<UUID, PingRequest> pingFutures = new ConcurrentHashMap<UUID, PingRequest>();
    private CompletableFuture<byte[]> dllFuture;
    private CompletableFuture<String> dllKeyFuture;
    private CompletableFuture<byte[]> dllUberFuture;
    private CompletableFuture<String> dllUberKeyFuture;
    private CompletableFuture<byte[]> loaderDllFuture;
    private static Consumer<byte[]> encryptionKeyCallback;
    private static BiConsumer<Integer, Channel> remoteLogCallback;

    public static void setEncryptionKeyCallback(Consumer<byte[]> callback) {
        encryptionKeyCallback = callback;
    }

    public static void setRemoteLogCallback(BiConsumer<Integer, Channel> callback) {
        remoteLogCallback = callback;
    }

    @Override
    public void registerPing(UUID uuid, long sentTimestamp, CompletableFuture<Long> future) {
        this.pingFutures.put(uuid, new PingRequest(future, sentTimestamp));
    }

    @Override
    public void registerDLLFuture(CompletableFuture<byte[]> future) {
        this.dllFuture = future;
    }

    @Override
    public void registerDLLKeyFuture(CompletableFuture<String> future) {
        this.dllKeyFuture = future;
    }

    @Override
    public void registerDLLUberFuture(CompletableFuture<byte[]> future) {
        this.dllUberFuture = future;
    }

    @Override
    public void registerDLLUberKeyFuture(CompletableFuture<String> future) {
        this.dllUberKeyFuture = future;
    }

    @Override
    public void registerLoaderDLLFuture(CompletableFuture<byte[]> future) {
        this.loaderDllFuture = future;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Packet packet = (Packet)msg;
        byte opcode = packet.getOpcode();
        PacketType packetType = PacketType.fromOpcode(opcode);
        if (packetType == null) {
            logger.error("Unknown packet type: {}", (Object)opcode);
            return;
        }
        ByteBuf payload = packet.getPayload();
        switch (packetType) {
            case AUTH: {
                boolean success = payload.readBoolean();
                if (success) {
                    int keyLen;
                    logger.info("Successfully authenticated");
                    int userId = 0;
                    if (payload.readableBytes() >= 4) {
                        userId = payload.readInt();
                        logger.debug("Received userId: {}", (Object)userId);
                        if (remoteLogCallback != null) {
                            remoteLogCallback.accept(userId, ctx.channel());
                        }
                    }
                    if (payload.readableBytes() >= 4 && (keyLen = payload.readInt()) > 0 && payload.readableBytes() >= keyLen) {
                        byte[] encryptionKey = new byte[keyLen];
                        payload.readBytes(encryptionKey);
                        logger.debug("Received encryption key ({} bytes)", (Object)keyLen);
                        if (encryptionKeyCallback != null) {
                            encryptionKeyCallback.accept(encryptionKey);
                        }
                    }
                    return;
                }
                byte reasonCode = payload.readByte();
                DisconnectReason reason = DisconnectReason.fromOpcode((int)reasonCode);
                if (reason == null) {
                    logger.error("Unknown reason: {}", (Object)reasonCode);
                    return;
                }
                logger.info("Disconnected! Reason: {}", (Object)reason);
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(null, "Roz\u0142\u0105czono z serwerem!\nPow\u00f3d: " + reason.getReason(), "B\u0142\u0105d po\u0142\u0105czenia", 0);
                System.exit(reason.getOpcode());
                break;
            }
            case PING: {
                if (payload.readableBytes() < 16) {
                    logger.error("PING received too short");
                    return;
                }
                byte[] uuidBytes = new byte[16];
                payload.readBytes(uuidBytes);
                UUID uuid = UUIDUtil.fromBytes((byte[])uuidBytes);
                PingRequest req = this.pingFutures.remove(uuid);
                if (req != null) {
                    long pongTimestamp = System.currentTimeMillis();
                    long pingTime = pongTimestamp - req.sentTimestamp;
                    req.future.complete(pingTime);
                    break;
                }
                logger.error("Received PING request, but no future was registered.");
                break;
            }
            case DLL: {
                if (payload.readableBytes() < 4) {
                    logger.error("DLL received too short");
                    return;
                }
                int dllLength = payload.readInt();
                if (payload.readableBytes() < dllLength) {
                    logger.error("DLL received too short");
                    return;
                }
                byte[] dllBytes = new byte[dllLength];
                payload.readBytes(dllBytes);
                if (this.dllFuture != null) {
                    this.dllFuture.complete(dllBytes);
                    this.dllFuture = null;
                    break;
                }
                logger.error("Received DLL file, but no future was registered.");
                break;
            }
            case DLL_KEY: {
                String key = payload.toString(StandardCharsets.UTF_8);
                if (this.dllKeyFuture != null) {
                    this.dllKeyFuture.complete(key);
                    this.dllKeyFuture = null;
                    break;
                }
                logger.error("Received DLL key, but no future was registered.");
                break;
            }
            case DLL_UBER: {
                if (payload.readableBytes() < 4) {
                    logger.error("DLL Uber received too short");
                    return;
                }
                int dllLength = payload.readInt();
                if (payload.readableBytes() < dllLength) {
                    logger.error("DLL Uber received too short");
                    return;
                }
                byte[] dllBytes = new byte[dllLength];
                payload.readBytes(dllBytes);
                if (this.dllUberFuture != null) {
                    this.dllUberFuture.complete(dllBytes);
                    this.dllUberFuture = null;
                    break;
                }
                logger.error("Received DLL Uber file, but no future was registered.");
                break;
            }
            case DLL_UBER_KEY: {
                String key = payload.toString(StandardCharsets.UTF_8);
                if (this.dllUberKeyFuture != null) {
                    this.dllUberKeyFuture.complete(key);
                    this.dllUberKeyFuture = null;
                    break;
                }
                logger.error("Received DLL Uber key, but no future was registered.");
                break;
            }
            case LOADER_DLL: {
                if (payload.readableBytes() < 4) {
                    logger.error("LOADER_DLL received too short");
                    return;
                }
                int dllLength = payload.readInt();
                if (payload.readableBytes() < dllLength) {
                    logger.error("LOADER_DLL received too short");
                    return;
                }
                byte[] dllBytes = new byte[dllLength];
                payload.readBytes(dllBytes);
                if (this.loaderDllFuture != null) {
                    this.loaderDllFuture.complete(dllBytes);
                    this.loaderDllFuture = null;
                    break;
                }
                logger.error("Received LOADER_DLL file, but no future was registered.");
                break;
            }
            default: {
                logger.error("Unknown packet type: {}", (Object)opcode);
            }
        }
        payload.release();
    }
}

