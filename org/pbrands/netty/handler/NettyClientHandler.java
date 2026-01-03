/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.pbrands.model.DisconnectReason
 *  org.pbrands.ui.CustomMessageDialog
 *  org.pbrands.ui.MaintenanceDialog
 *  org.pbrands.util.UUIDUtil
 */
package org.pbrands.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.awt.Frame;
import java.awt.Toolkit;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.SwingUtilities;
import org.pbrands.model.DisconnectReason;
import org.pbrands.model.PacketType;
import org.pbrands.netty.NettyClient;
import org.pbrands.netty.Packet;
import org.pbrands.netty.handler.ClientHandler;
import org.pbrands.ui.CustomMessageDialog;
import org.pbrands.ui.MaintenanceDialog;
import org.pbrands.ui.main.LoaderWindow;
import org.pbrands.util.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class NettyClientHandler
extends ChannelInboundHandlerAdapter
implements ClientHandler {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);
    private final Map<UUID, PingRequest> pingFutures = new ConcurrentHashMap<UUID, PingRequest>();
    private CompletableFuture<byte[]> dllFuture;
    private CompletableFuture<String> dllKeyFuture;
    private CompletableFuture<byte[]> dllUberFuture;
    private CompletableFuture<String> dllUberKeyFuture;
    private CompletableFuture<LoginResult> loginFuture;
    private CompletableFuture<LoginResult> authFuture;
    private CompletableFuture<String> productsFuture;
    private CompletableFuture<byte[]> productDownloadFuture;
    private CompletableFuture<byte[]> interceptionFuture;
    private CompletableFuture<NettyClient.EncryptedProductResult> encryptedProductFuture;
    private String pendingEncryptionKey;

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

    public void registerLoginFuture(CompletableFuture<LoginResult> future) {
        this.loginFuture = future;
    }

    public void registerAuthFuture(CompletableFuture<LoginResult> future) {
        this.authFuture = future;
    }

    public void registerProductsFuture(CompletableFuture<String> future) {
        this.productsFuture = future;
    }

    public void registerProductDownloadFuture(CompletableFuture<byte[]> future) {
        this.productDownloadFuture = future;
    }

    public void registerInterceptionFuture(CompletableFuture<byte[]> future) {
        this.interceptionFuture = future;
    }

    public void registerEncryptedProductFuture(CompletableFuture<NettyClient.EncryptedProductResult> future) {
        this.encryptedProductFuture = future;
        this.pendingEncryptionKey = null;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Channel inactive, cancelling all futures");
        this.cancelAllFutures("Connection closed");
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception caught in NettyClientHandler", cause);
        this.cancelAllFutures("Exception: " + cause.getMessage());
        ctx.close();
    }

    private void cancelAllFutures(String reason) {
        if (this.loginFuture != null) {
            this.loginFuture.complete(new LoginResult(false, null, null, null, 0L, null, "B\u0142\u0105d po\u0142\u0105czenia: " + reason));
            this.loginFuture = null;
        }
        if (this.authFuture != null) {
            this.authFuture.complete(new LoginResult(false, null, null, null, 0L, null, "B\u0142\u0105d po\u0142\u0105czenia: " + reason));
            this.authFuture = null;
        }
        if (this.productsFuture != null) {
            this.productsFuture.completeExceptionally(new RuntimeException(reason));
            this.productsFuture = null;
        }
        if (this.productDownloadFuture != null) {
            this.productDownloadFuture.completeExceptionally(new RuntimeException(reason));
            this.productDownloadFuture = null;
        }
        if (this.interceptionFuture != null) {
            this.interceptionFuture.completeExceptionally(new RuntimeException(reason));
            this.interceptionFuture = null;
        }
        if (this.dllFuture != null) {
            this.dllFuture.completeExceptionally(new RuntimeException(reason));
            this.dllFuture = null;
        }
        if (this.dllKeyFuture != null) {
            this.dllKeyFuture.completeExceptionally(new RuntimeException(reason));
            this.dllKeyFuture = null;
        }
        if (this.dllUberFuture != null) {
            this.dllUberFuture.completeExceptionally(new RuntimeException(reason));
            this.dllUberFuture = null;
        }
        if (this.dllUberKeyFuture != null) {
            this.dllUberKeyFuture.completeExceptionally(new RuntimeException(reason));
            this.dllUberKeyFuture = null;
        }
        this.pingFutures.values().forEach(req -> req.future().completeExceptionally(new RuntimeException(reason)));
        this.pingFutures.clear();
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
                Object reasonMessage;
                boolean success = payload.readBoolean();
                logger.info("AUTH packet received. Success: {}", (Object)success);
                if (success) {
                    logger.info("Successfully authenticated");
                    int tokenLen = payload.readInt();
                    String token = payload.readCharSequence(tokenLen, StandardCharsets.UTF_8).toString();
                    int roleLen = payload.readInt();
                    String role = payload.readCharSequence(roleLen, StandardCharsets.UTF_8).toString();
                    int usernameLen = payload.readInt();
                    String username = payload.readCharSequence(usernameLen, StandardCharsets.UTF_8).toString();
                    long subscriptionExpiry = payload.readLong();
                    int pfpLen = payload.readInt();
                    byte[] pfpBytes = new byte[pfpLen];
                    payload.readBytes(pfpBytes);
                    if (this.authFuture != null) {
                        this.authFuture.complete(new LoginResult(true, token, role, username, subscriptionExpiry, pfpBytes, null));
                        this.authFuture = null;
                    }
                    return;
                }
                if (payload.readableBytes() >= 4) {
                    int reasonLength = payload.readInt();
                    if (payload.readableBytes() >= reasonLength) {
                        byte[] reasonBytes = new byte[reasonLength];
                        payload.readBytes(reasonBytes);
                        reasonMessage = new String(reasonBytes, StandardCharsets.UTF_8);
                    } else {
                        reasonMessage = "B\u0142\u0105d protoko\u0142u: Niepe\u0142ny pakiet danych.";
                    }
                } else {
                    byte reasonOrdinal;
                    DisconnectReason reason;
                    reasonMessage = payload.readableBytes() == 1 ? ((reason = DisconnectReason.fromOpcode((int)(reasonOrdinal = payload.readByte()))) != null ? reason.getReason() : "B\u0142\u0105d logowania (Kod b\u0142\u0119du: " + reasonOrdinal + ")") : "Nieznany b\u0142\u0105d logowania.";
                }
                if (this.authFuture != null) {
                    this.authFuture.complete(new LoginResult(false, null, null, null, 0L, null, (String)reasonMessage));
                    this.authFuture = null;
                    break;
                }
                logger.info("Disconnected! Reason: {}", reasonMessage);
                Toolkit.getDefaultToolkit().beep();
                Object finalReasonMessage = reasonMessage;
                SwingUtilities.invokeLater(() -> NettyClientHandler.lambda$channelRead$1((String)finalReasonMessage));
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
            case LOADER_DLL: {
                if (payload.readableBytes() < 4) {
                    logger.error("LOADER_DLL received too short");
                    return;
                }
                int dllLength = payload.readInt();
                if (dllLength == 0) {
                    if (this.dllFuture != null) {
                        this.dllFuture.complete(new byte[0]);
                        this.dllFuture = null;
                    }
                    return;
                }
                if (payload.readableBytes() < dllLength) {
                    logger.error("LOADER_DLL received too short");
                    return;
                }
                byte[] dllBytes = new byte[dllLength];
                payload.readBytes(dllBytes);
                if (this.dllFuture != null) {
                    this.dllFuture.complete(dllBytes);
                    this.dllFuture = null;
                    break;
                }
                logger.error("Received LOADER_DLL file, but no future was registered.");
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
            case LOGIN: {
                logger.info("LOGIN packet received.");
                boolean success = payload.readBoolean();
                logger.info("Login success: {}", (Object)success);
                if (success) {
                    int tokenLen = payload.readInt();
                    String token = payload.readCharSequence(tokenLen, StandardCharsets.UTF_8).toString();
                    int roleLen = payload.readInt();
                    String role = payload.readCharSequence(roleLen, StandardCharsets.UTF_8).toString();
                    int usernameLen = payload.readInt();
                    String username = payload.readCharSequence(usernameLen, StandardCharsets.UTF_8).toString();
                    long subscriptionExpiry = payload.readLong();
                    int pfpLen = payload.readInt();
                    byte[] pfpBytes = new byte[pfpLen];
                    payload.readBytes(pfpBytes);
                    if (this.loginFuture != null) {
                        this.loginFuture.complete(new LoginResult(true, token, role, username, subscriptionExpiry, pfpBytes, null));
                        this.loginFuture = null;
                        break;
                    }
                    logger.warn("Received LOGIN response but no future registered.");
                    break;
                }
                int errorLen = payload.readInt();
                String error = payload.readCharSequence(errorLen, StandardCharsets.UTF_8).toString();
                if (this.loginFuture != null) {
                    this.loginFuture.complete(new LoginResult(false, null, null, null, 0L, null, error));
                    this.loginFuture = null;
                    break;
                }
                logger.warn("Received LOGIN error response but no future registered.");
                break;
            }
            case FETCH_PRODUCTS: {
                logger.info("FETCH_PRODUCTS packet received.");
                int jsonLen = payload.readInt();
                String json = payload.readCharSequence(jsonLen, StandardCharsets.UTF_8).toString();
                if (this.productsFuture != null) {
                    this.productsFuture.complete(json);
                    this.productsFuture = null;
                    break;
                }
                logger.warn("Received FETCH_PRODUCTS response but no future registered.");
                break;
            }
            case DOWNLOAD_PRODUCT: {
                if (payload.readableBytes() < 4) {
                    logger.error("DOWNLOAD_PRODUCT received too short");
                    return;
                }
                int fileLength = payload.readInt();
                if (payload.readableBytes() < fileLength) {
                    logger.error("DOWNLOAD_PRODUCT received too short");
                    return;
                }
                byte[] fileBytes = new byte[fileLength];
                payload.readBytes(fileBytes);
                if (this.productDownloadFuture != null) {
                    this.productDownloadFuture.complete(fileBytes);
                    this.productDownloadFuture = null;
                    break;
                }
                logger.error("Received DOWNLOAD_PRODUCT file, but no future was registered.");
                break;
            }
            case DOWNLOAD_INTERCEPTION: {
                if (payload.readableBytes() < 4) {
                    logger.error("DOWNLOAD_INTERCEPTION received too short");
                    return;
                }
                int fileLength = payload.readInt();
                if (payload.readableBytes() < fileLength) {
                    logger.error("DOWNLOAD_INTERCEPTION received too short");
                    return;
                }
                byte[] fileBytes = new byte[fileLength];
                payload.readBytes(fileBytes);
                if (this.interceptionFuture != null) {
                    this.interceptionFuture.complete(fileBytes);
                    this.interceptionFuture = null;
                    break;
                }
                logger.error("Received DOWNLOAD_INTERCEPTION file, but no future was registered.");
                break;
            }
            case ENCRYPTED_PRODUCT_KEY: {
                if (payload.readableBytes() < 4) {
                    logger.error("ENCRYPTED_PRODUCT_KEY received too short");
                    return;
                }
                int keyLen = payload.readInt();
                if (payload.readableBytes() < keyLen) {
                    logger.error("ENCRYPTED_PRODUCT_KEY key data too short");
                    return;
                }
                this.pendingEncryptionKey = payload.readCharSequence(keyLen, StandardCharsets.UTF_8).toString();
                logger.info("Received encryption key ({} chars)", (Object)this.pendingEncryptionKey.length());
                break;
            }
            case DOWNLOAD_ENCRYPTED_PRODUCT: {
                if (payload.readableBytes() < 4) {
                    logger.error("DOWNLOAD_ENCRYPTED_PRODUCT received too short");
                    return;
                }
                int fileLength = payload.readInt();
                if (payload.readableBytes() < fileLength) {
                    logger.error("DOWNLOAD_ENCRYPTED_PRODUCT data too short");
                    return;
                }
                byte[] encryptedBytes = new byte[fileLength];
                payload.readBytes(encryptedBytes);
                if (this.encryptedProductFuture != null && this.pendingEncryptionKey != null) {
                    this.encryptedProductFuture.complete(new NettyClient.EncryptedProductResult(this.pendingEncryptionKey, encryptedBytes));
                    this.encryptedProductFuture = null;
                    this.pendingEncryptionKey = null;
                    logger.info("Received encrypted product ({} bytes)", (Object)encryptedBytes.length);
                    break;
                }
                logger.error("Received DOWNLOAD_ENCRYPTED_PRODUCT but no future or key registered.");
                break;
            }
            default: {
                logger.error("Unknown packet type: {}", (Object)opcode);
            }
        }
        payload.release();
    }

    private static /* synthetic */ void lambda$channelRead$1(String finalReasonMessage) {
        if (MaintenanceDialog.isMaintenanceMessage((String)finalReasonMessage)) {
            MaintenanceDialog.show((Frame)LoaderWindow.instance, (String)finalReasonMessage);
        } else {
            CustomMessageDialog.showMessage((Frame)LoaderWindow.instance, (String)"B\u0142\u0105d po\u0142\u0105czenia", (String)("Roz\u0142\u0105czono z serwerem!\nPow\u00f3d: " + finalReasonMessage), (boolean)true);
        }
        System.exit(0);
    }

    private record PingRequest(CompletableFuture<Long> future, long sentTimestamp) {
    }

    public record LoginResult(boolean success, String token, String role, String username, long subscriptionExpiry, byte[] profilePicture, String error) {
    }
}

