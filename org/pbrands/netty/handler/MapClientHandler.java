/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.pbrands.model.DisconnectReason
 *  org.pbrands.netty.handler.MapClientHandler$PingRequest
 *  org.pbrands.util.UUIDUtil
 */
package org.pbrands.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.awt.Toolkit;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import javax.swing.JOptionPane;
import org.pbrands.map.Map;
import org.pbrands.map.Marker;
import org.pbrands.map.MarkerType;
import org.pbrands.map.TileDownloader;
import org.pbrands.model.DisconnectReason;
import org.pbrands.model.MapDefinition;
import org.pbrands.model.PacketType;
import org.pbrands.netty.Packet;
import org.pbrands.netty.handler.ClientHandler;
import org.pbrands.netty.handler.MapClientHandler;
import org.pbrands.util.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapClientHandler
extends ChannelInboundHandlerAdapter
implements ClientHandler {
    private static final Logger logger = LoggerFactory.getLogger(MapClientHandler.class);
    private final Map map;
    private static BiConsumer<Integer, Channel> remoteLogCallback;
    private final java.util.Map<UUID, PingRequest> pingFutures = new ConcurrentHashMap<UUID, PingRequest>();
    private static TileDownloader tileDownloader;

    public static void setRemoteLogCallback(BiConsumer<Integer, Channel> callback) {
        remoteLogCallback = callback;
    }

    public MapClientHandler(Map map) {
        this.map = map;
    }

    @Override
    public void registerPing(UUID uuid, long sentTimestamp, CompletableFuture<Long> future) {
        this.pingFutures.put(uuid, new PingRequest(future, sentTimestamp));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
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
                    logger.info("Successfully authenticated");
                    if (payload.readableBytes() >= 4) {
                        int userId = payload.readInt();
                        logger.debug("Received userId: {}", (Object)userId);
                        if (remoteLogCallback != null) {
                            remoteLogCallback.accept(userId, ctx.channel());
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
            case MAP_LIST: {
                int count = payload.readInt();
                List<MapDefinition> uuid = this.map.getAvailableMaps();
                synchronized (uuid) {
                    this.map.getAvailableMaps().clear();
                    for (int i = 0; i < count; ++i) {
                        int mapId = payload.readInt();
                        int nameLength = payload.readInt();
                        byte[] nameBytes = new byte[nameLength];
                        payload.readBytes(nameBytes);
                        String name = new String(nameBytes, StandardCharsets.UTF_8);
                        int maxCollectibles = payload.readInt();
                        this.map.getAvailableMaps().add(new MapDefinition(mapId, name, maxCollectibles));
                    }
                    Collections.reverse(this.map.getAvailableMaps());
                    break;
                }
            }
            case ADD_MARKER: {
                int markerId = payload.readInt();
                boolean creator = payload.readBoolean();
                int markerTypeId = payload.readInt();
                float x = payload.readFloat();
                float y = payload.readFloat();
                int descriptionBytesLength = payload.readInt();
                byte[] descriptionBytes = new byte[descriptionBytesLength];
                payload.readBytes(descriptionBytes);
                int likeCount = payload.readInt();
                int dislikeCount = payload.readInt();
                boolean underground = payload.readBoolean();
                boolean collected = payload.readBoolean();
                boolean liked = payload.readBoolean();
                boolean disliked = payload.readBoolean();
                MarkerType markerTypeById = this.map.getMarkerTypeById(markerTypeId);
                Marker marker = new Marker(markerId, creator, markerTypeById, x, y, new String(descriptionBytes), underground);
                marker.setLikeCount(likeCount);
                marker.setDislikeCount(dislikeCount);
                marker.setUnderground(underground);
                marker.setCollected(collected);
                marker.setLiked(liked);
                marker.setDisliked(disliked);
                this.map.addMarker(marker);
                break;
            }
            case REMOVE_MARKER: {
                int markerId = payload.readInt();
                this.map.removeMarker(markerId);
                break;
            }
            case MARKER_TYPE: {
                int id = payload.readInt();
                int markerTypeNameBytesLength = payload.readInt();
                byte[] nameBytes = new byte[markerTypeNameBytesLength];
                payload.readBytes(nameBytes);
                int width = payload.readInt();
                int height = payload.readInt();
                int resourceImagePathBytesLength = payload.readInt();
                byte[] resourceImagePathBytes = new byte[resourceImagePathBytesLength];
                payload.readBytes(resourceImagePathBytes);
                MarkerType markerType = new MarkerType(id, new String(nameBytes), width, height, new String(resourceImagePathBytes));
                this.map.addMarkerType(markerType);
                break;
            }
            case MAP_TILE_DATA: {
                int x = payload.readInt();
                int y = payload.readInt();
                int dataLength = payload.readInt();
                byte[] data = new byte[dataLength];
                payload.readBytes(data);
                logger.debug("Received tile ({},{}) - {} bytes", x, y, dataLength);
                if (tileDownloader == null) break;
                tileDownloader.onTileReceived(x, y, data);
                break;
            }
            default: {
                logger.error("Unknown packet type: {}", (Object)opcode);
            }
        }
        payload.release();
    }

    public static void setTileDownloader(TileDownloader downloader) {
        tileDownloader = downloader;
    }
}

