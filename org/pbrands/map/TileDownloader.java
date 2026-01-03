/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import org.pbrands.map.TileCache;
import org.pbrands.model.PacketType;
import org.pbrands.netty.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TileDownloader {
    private static final Logger logger = LoggerFactory.getLogger(TileDownloader.class);
    private final TileCache cache;
    private final Channel channel;
    private final Map<String, CompletableFuture<byte[]>> pendingDownloads = new ConcurrentHashMap<String, CompletableFuture<byte[]>>();
    private BiConsumer<Integer, Integer> progressCallback;

    public TileDownloader(Channel channel) {
        this.channel = channel;
        this.cache = new TileCache();
    }

    public void setProgressCallback(BiConsumer<Integer, Integer> callback) {
        this.progressCallback = callback;
    }

    public byte[] getTile(int x, int y) throws Exception {
        if (this.cache.hasTile(x, y)) {
            return this.cache.loadTile(x, y);
        }
        return this.downloadTile(x, y).get(30L, TimeUnit.SECONDS);
    }

    public CompletableFuture<byte[]> downloadTile(int x, int y) {
        String key = x + "_" + y;
        CompletableFuture<byte[]> existing = this.pendingDownloads.get(key);
        if (existing != null) {
            return existing;
        }
        CompletableFuture<byte[]> future = new CompletableFuture<byte[]>();
        this.pendingDownloads.put(key, future);
        ByteBuf payload = Unpooled.buffer();
        payload.writeInt(x);
        payload.writeInt(y);
        Packet packet = new Packet(PacketType.DOWNLOAD_MAP_TILE.getOpcode(), payload);
        packet.writeAndFlush(this.channel);
        logger.debug("Requesting tile ({},{})", (Object)x, (Object)y);
        return future;
    }

    public void onTileReceived(int x, int y, byte[] data) {
        String key = x + "_" + y;
        try {
            this.cache.saveTile(x, y, data);
        }
        catch (IOException e) {
            logger.error("Failed to cache tile ({},{})", x, y, e);
        }
        CompletableFuture<byte[]> future = this.pendingDownloads.remove(key);
        if (future != null) {
            future.complete(data);
        }
        if (this.progressCallback != null) {
            this.progressCallback.accept(this.cache.getCachedTileCount(), 100);
        }
    }

    public int countMissingTiles(int tileCountX, int tileCountY) {
        int missing = 0;
        for (int x = 0; x < tileCountX; ++x) {
            for (int y = 0; y < tileCountY; ++y) {
                if (this.cache.hasTile(x, y)) continue;
                ++missing;
            }
        }
        return missing;
    }

    public CompletableFuture<Void> downloadAllMissingTiles(int tileCountX, int tileCountY) {
        CompletableFuture[] futures = new CompletableFuture[tileCountX * tileCountY];
        int idx = 0;
        for (int x = 0; x < tileCountX; ++x) {
            for (int y = 0; y < tileCountY; ++y) {
                futures[idx++] = !this.cache.hasTile(x, y) ? this.downloadTile(x, y) : CompletableFuture.completedFuture(null);
            }
        }
        return CompletableFuture.allOf(futures);
    }

    public TileCache getCache() {
        return this.cache;
    }
}

