/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.pbrands.logic.communication.RecognitionResult
 *  org.pbrands.model.DisconnectReason
 *  org.pbrands.netty.handler.MiningBotClientHandler$PingRequest
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
import org.pbrands.logic.communication.RecognitionResult;
import org.pbrands.logic.listeners.CoalPriceListener;
import org.pbrands.model.DisconnectReason;
import org.pbrands.model.PacketType;
import org.pbrands.netty.Packet;
import org.pbrands.netty.handler.ClientHandler;
import org.pbrands.netty.handler.MiningBotClientHandler;
import org.pbrands.util.Log;
import org.pbrands.util.UUIDUtil;

public class MiningBotClientHandler
extends ChannelInboundHandlerAdapter
implements ClientHandler {
    private static BiConsumer<String, Integer> disconnectCallback;
    private static Consumer<byte[]> encryptionKeyCallback;
    private static BiConsumer<Integer, Channel> remoteLogCallback;
    private static Consumer<String> roleCallback;
    private static volatile String authenticatedRole;
    private final Map<UUID, PingRequest> pingFutures = new ConcurrentHashMap<UUID, PingRequest>();
    private CompletableFuture<RecognitionResult> imageRecognitionFuture;
    private CompletableFuture<byte[]> dllFuture;
    private CompletableFuture<String> dllKeyFuture;
    private CompletableFuture<byte[]> dllUberFuture;
    private CompletableFuture<String> dllUberKeyFuture;
    private CompletableFuture<byte[]> loaderDllFuture;
    private CompletableFuture<byte[]> nativeDownloadFuture;
    private CompletableFuture<Double> ocrDigitFuture;

    public static void setDisconnectCallback(BiConsumer<String, Integer> callback) {
        disconnectCallback = callback;
    }

    public static void setEncryptionKeyCallback(Consumer<byte[]> callback) {
        encryptionKeyCallback = callback;
    }

    public static void setRemoteLogCallback(BiConsumer<Integer, Channel> callback) {
        remoteLogCallback = callback;
    }

    public static void setRoleCallback(Consumer<String> callback) {
        roleCallback = callback;
    }

    public static String getAuthenticatedRole() {
        return authenticatedRole;
    }

    public static boolean isAdmin() {
        return "ROLE_ADMIN".equals(authenticatedRole);
    }

    public static boolean isBetaTester() {
        return "ROLE_BETA_USER".equals(authenticatedRole);
    }

    @Override
    public void registerPing(UUID uuid, long sentTimestamp, CompletableFuture<Long> future) {
        this.pingFutures.put(uuid, new PingRequest(future, sentTimestamp));
    }

    public void registerImageRecognition(CompletableFuture<RecognitionResult> future) {
        this.imageRecognitionFuture = future;
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

    public void registerNativeDownloadFuture(CompletableFuture<byte[]> future) {
        this.nativeDownloadFuture = future;
    }

    public void registerOcrDigitFuture(CompletableFuture<Double> future) {
        this.ocrDigitFuture = future;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Packet packet = (Packet)msg;
        byte opcode = packet.getOpcode();
        PacketType packetType = PacketType.fromOpcode(opcode);
        if (packetType == null) {
            Log.error("Unknown packet type: {}", opcode);
            return;
        }
        ByteBuf payload = packet.getPayload();
        switch (packetType) {
            case AUTH: {
                boolean success = payload.readBoolean();
                if (success) {
                    Log.info("Successfully authenticated");
                    try {
                        int keyLen;
                        String role;
                        int tokenLen = payload.readInt();
                        payload.skipBytes(tokenLen);
                        int roleLen = payload.readInt();
                        byte[] roleBytes = new byte[roleLen];
                        payload.readBytes(roleBytes);
                        authenticatedRole = role = new String(roleBytes, StandardCharsets.UTF_8);
                        Log.info("Authenticated with role: {}", role);
                        if (roleCallback != null) {
                            roleCallback.accept(role);
                        }
                        int usernameLen = payload.readInt();
                        payload.skipBytes(usernameLen);
                        payload.readLong();
                        int pfpLen = payload.readInt();
                        if (pfpLen > 0) {
                            payload.skipBytes(pfpLen);
                        }
                        int userId = 0;
                        if (payload.readableBytes() >= 4) {
                            userId = payload.readInt();
                            Log.debug("Received userId: {}", userId);
                            if (remoteLogCallback != null) {
                                remoteLogCallback.accept(userId, ctx.channel());
                            }
                        }
                        if (payload.readableBytes() >= 4 && (keyLen = payload.readInt()) > 0 && payload.readableBytes() >= keyLen) {
                            byte[] encryptionKey = new byte[keyLen];
                            payload.readBytes(encryptionKey);
                            Log.debug("Received encryption key ({} bytes)", keyLen);
                            if (encryptionKeyCallback != null) {
                                encryptionKeyCallback.accept(encryptionKey);
                            }
                        }
                    }
                    catch (Exception e) {
                        Log.warn("Failed to read full auth response (older server version?): {}", e.getMessage());
                    }
                    return;
                }
                byte reasonCode = payload.readByte();
                DisconnectReason reason = DisconnectReason.fromOpcode((int)reasonCode);
                if (reason == null) {
                    Log.error("Unknown reason: {}", reasonCode);
                    return;
                }
                Log.info("Disconnected! Reason: {}", reason);
                Toolkit.getDefaultToolkit().beep();
                if (disconnectCallback != null) {
                    disconnectCallback.accept(reason.getReason(), Integer.valueOf(reason.getOpcode()));
                    break;
                }
                JOptionPane.showMessageDialog(null, "Roz\u0142\u0105czono z serwerem!\nPow\u00f3d: " + reason.getReason(), "B\u0142\u0105d po\u0142\u0105czenia", 0);
                System.exit(reason.getOpcode());
                break;
            }
            case PING: {
                if (payload.readableBytes() < 16) {
                    Log.error("PING received too short");
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
                Log.error("Received PING request, but no future was registered.");
                break;
            }
            case DIG_LEVEL: {
                Log.info("Received DIG_LEVEL packet");
                break;
            }
            case COAL_PRICE: {
                double coalPrice = payload.readDouble();
                for (CoalPriceListener listener : CoalPriceListener.listeners) {
                    listener.coalPriceUpdated(coalPrice);
                }
                break;
            }
            case IMAGE_RECOGNITION: {
                int requestIdLength = payload.readInt();
                byte[] requestIdBytes = new byte[requestIdLength];
                payload.readBytes(requestIdBytes);
                String requestId = new String(requestIdBytes, StandardCharsets.UTF_8);
                char recognized = (char)payload.readByte();
                RecognitionResult recognitionResult = new RecognitionResult(requestId, recognized);
                if (this.imageRecognitionFuture != null) {
                    this.imageRecognitionFuture.complete(recognitionResult);
                    this.imageRecognitionFuture = null;
                    break;
                }
                Log.error("Received image recognition, but no future was registered.");
                break;
            }
            case DLL: {
                if (payload.readableBytes() < 4) {
                    Log.error("DLL received too short");
                    return;
                }
                int dllLength = payload.readInt();
                if (payload.readableBytes() < dllLength) {
                    Log.error("DLL received too short");
                    return;
                }
                byte[] dllBytes = new byte[dllLength];
                payload.readBytes(dllBytes);
                if (this.dllFuture != null) {
                    this.dllFuture.complete(dllBytes);
                    this.dllFuture = null;
                    break;
                }
                Log.error("Received DLL file, but no future was registered.");
                break;
            }
            case DLL_KEY: {
                String key = payload.toString(StandardCharsets.UTF_8);
                if (this.dllKeyFuture != null) {
                    this.dllKeyFuture.complete(key);
                    this.dllKeyFuture = null;
                    break;
                }
                Log.error("Received DLL key, but no future was registered.");
                break;
            }
            case DLL_UBER: {
                if (payload.readableBytes() < 4) {
                    Log.error("DLL Uber received too short");
                    return;
                }
                int dllLength = payload.readInt();
                if (payload.readableBytes() < dllLength) {
                    Log.error("DLL Uber received too short");
                    return;
                }
                byte[] dllBytes = new byte[dllLength];
                payload.readBytes(dllBytes);
                if (this.dllUberFuture != null) {
                    this.dllUberFuture.complete(dllBytes);
                    this.dllUberFuture = null;
                    break;
                }
                Log.error("Received DLL Uber file, but no future was registered.");
                break;
            }
            case DLL_UBER_KEY: {
                String key = payload.toString(StandardCharsets.UTF_8);
                if (this.dllUberKeyFuture != null) {
                    this.dllUberKeyFuture.complete(key);
                    this.dllUberKeyFuture = null;
                    break;
                }
                Log.error("Received DLL Uber key, but no future was registered.");
                break;
            }
            case LOADER_DLL: {
                if (payload.readableBytes() < 4) {
                    Log.error("LOADER_DLL received too short");
                    return;
                }
                int dllLength = payload.readInt();
                if (payload.readableBytes() < dllLength) {
                    Log.error("LOADER_DLL received too short");
                    return;
                }
                byte[] dllBytes = new byte[dllLength];
                payload.readBytes(dllBytes);
                if (this.loaderDllFuture != null) {
                    this.loaderDllFuture.complete(dllBytes);
                    this.loaderDllFuture = null;
                    break;
                }
                Log.error("Received LOADER_DLL file, but no future was registered.");
                break;
            }
            case DOWNLOAD_NATIVE: {
                if (payload.readableBytes() < 4) {
                    Log.error("DOWNLOAD_NATIVE received too short");
                    return;
                }
                int nativeLength = payload.readInt();
                if (nativeLength == 0) {
                    if (this.nativeDownloadFuture != null) {
                        this.nativeDownloadFuture.complete(null);
                        this.nativeDownloadFuture = null;
                    }
                    return;
                }
                if (payload.readableBytes() < nativeLength) {
                    Log.error("DOWNLOAD_NATIVE payload too short, expected {} bytes", nativeLength);
                    return;
                }
                byte[] nativeBytes = new byte[nativeLength];
                payload.readBytes(nativeBytes);
                if (this.nativeDownloadFuture != null) {
                    this.nativeDownloadFuture.complete(nativeBytes);
                    this.nativeDownloadFuture = null;
                    Log.info("Received native library: {} bytes", nativeLength);
                    break;
                }
                Log.error("Received DOWNLOAD_NATIVE file, but no future was registered.");
                break;
            }
            case OCR_DIGIT: {
                if (payload.readableBytes() < 8) {
                    Log.error("OCR_DIGIT response too short");
                    if (this.ocrDigitFuture != null) {
                        this.ocrDigitFuture.complete(-1.0);
                        this.ocrDigitFuture = null;
                    }
                    return;
                }
                double value = payload.readDouble();
                if (this.ocrDigitFuture != null) {
                    this.ocrDigitFuture.complete(value);
                    this.ocrDigitFuture = null;
                    break;
                }
                Log.warn("Received OCR_DIGIT response but no future was registered");
                break;
            }
            default: {
                Log.error("Unknown packet type: {}", opcode);
            }
        }
        payload.release();
    }

    static {
        authenticatedRole = null;
    }
}

