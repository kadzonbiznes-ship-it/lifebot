/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.map;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TileCache {
    private static final Logger logger = LoggerFactory.getLogger(TileCache.class);
    private final Path cacheDir;

    public TileCache() {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData == null) {
            throw new RuntimeException("LOCALAPPDATA not set");
        }
        this.cacheDir = Path.of(localAppData, "LifeBot", "data", "mta-maps", "textures");
        try {
            Files.createDirectories(this.cacheDir, new FileAttribute[0]);
            logger.info("Tile cache directory: {}", (Object)this.cacheDir);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to create tile cache directory", e);
        }
    }

    public boolean hasTile(int x, int y) {
        return this.getTilePath(x, y).toFile().exists();
    }

    public Path getTilePath(int x, int y) {
        return this.cacheDir.resolve(String.format("tile_%d_%d.jpg", x, y));
    }

    public File getTileFile(int x, int y) {
        return this.getTilePath(x, y).toFile();
    }

    public byte[] loadTile(int x, int y) throws IOException {
        Path tilePath = this.getTilePath(x, y);
        if (!tilePath.toFile().exists()) {
            return null;
        }
        return Files.readAllBytes(tilePath);
    }

    public void saveTile(int x, int y, byte[] data) throws IOException {
        Path tilePath = this.getTilePath(x, y);
        Files.write(tilePath, data, new OpenOption[0]);
        logger.debug("Saved tile ({},{}) to cache: {} bytes", x, y, data.length);
    }

    public int getCachedTileCount() {
        File[] files = this.cacheDir.toFile().listFiles((dir, name) -> name.startsWith("tile_") && name.endsWith(".jpg"));
        return files != null ? files.length : 0;
    }

    public void clearCache() {
        File[] files = this.cacheDir.toFile().listFiles((dir, name) -> name.startsWith("tile_") && name.endsWith(".jpg"));
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        logger.info("Cleared tile cache");
    }
}

