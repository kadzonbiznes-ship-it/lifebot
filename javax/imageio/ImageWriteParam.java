/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio;

import java.awt.Dimension;
import java.util.Locale;
import javax.imageio.IIOParam;

public class ImageWriteParam
extends IIOParam {
    public static final int MODE_DISABLED = 0;
    public static final int MODE_DEFAULT = 1;
    public static final int MODE_EXPLICIT = 2;
    public static final int MODE_COPY_FROM_METADATA = 3;
    private static final int MAX_MODE = 3;
    protected boolean canWriteTiles = false;
    protected int tilingMode = 3;
    protected Dimension[] preferredTileSizes = null;
    protected boolean tilingSet = false;
    protected int tileWidth = 0;
    protected int tileHeight = 0;
    protected boolean canOffsetTiles = false;
    protected int tileGridXOffset = 0;
    protected int tileGridYOffset = 0;
    protected boolean canWriteProgressive = false;
    protected int progressiveMode = 3;
    protected boolean canWriteCompressed = false;
    protected int compressionMode = 3;
    protected String[] compressionTypes = null;
    protected String compressionType = null;
    protected float compressionQuality = 1.0f;
    protected Locale locale = null;

    protected ImageWriteParam() {
    }

    public ImageWriteParam(Locale locale) {
        this.locale = locale;
    }

    private static Dimension[] clonePreferredTileSizes(Dimension[] sizes) {
        if (sizes == null) {
            return null;
        }
        Dimension[] temp = new Dimension[sizes.length];
        for (int i = 0; i < sizes.length; ++i) {
            temp[i] = new Dimension(sizes[i]);
        }
        return temp;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public boolean canWriteTiles() {
        return this.canWriteTiles;
    }

    public boolean canOffsetTiles() {
        return this.canOffsetTiles;
    }

    public void setTilingMode(int mode) {
        if (!this.canWriteTiles()) {
            throw new UnsupportedOperationException("Tiling not supported!");
        }
        if (mode < 0 || mode > 3) {
            throw new IllegalArgumentException("Illegal value for mode!");
        }
        this.tilingMode = mode;
        if (mode == 2) {
            this.unsetTiling();
        }
    }

    public int getTilingMode() {
        if (!this.canWriteTiles()) {
            throw new UnsupportedOperationException("Tiling not supported");
        }
        return this.tilingMode;
    }

    public Dimension[] getPreferredTileSizes() {
        if (!this.canWriteTiles()) {
            throw new UnsupportedOperationException("Tiling not supported");
        }
        return ImageWriteParam.clonePreferredTileSizes(this.preferredTileSizes);
    }

    public void setTiling(int tileWidth, int tileHeight, int tileGridXOffset, int tileGridYOffset) {
        boolean tilesOffset;
        if (!this.canWriteTiles()) {
            throw new UnsupportedOperationException("Tiling not supported!");
        }
        if (this.getTilingMode() != 2) {
            throw new IllegalStateException("Tiling mode not MODE_EXPLICIT!");
        }
        if (tileWidth <= 0 || tileHeight <= 0) {
            throw new IllegalArgumentException("tile dimensions are non-positive!");
        }
        boolean bl = tilesOffset = tileGridXOffset != 0 || tileGridYOffset != 0;
        if (!this.canOffsetTiles() && tilesOffset) {
            throw new UnsupportedOperationException("Can't offset tiles!");
        }
        if (this.preferredTileSizes != null) {
            boolean ok = true;
            for (int i = 0; i < this.preferredTileSizes.length; i += 2) {
                Dimension min = this.preferredTileSizes[i];
                Dimension max = this.preferredTileSizes[i + 1];
                if (tileWidth >= min.width && tileWidth <= max.width && tileHeight >= min.height && tileHeight <= max.height) continue;
                ok = false;
                break;
            }
            if (!ok) {
                throw new IllegalArgumentException("Illegal tile size!");
            }
        }
        this.tilingSet = true;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.tileGridXOffset = tileGridXOffset;
        this.tileGridYOffset = tileGridYOffset;
    }

    public void unsetTiling() {
        if (!this.canWriteTiles()) {
            throw new UnsupportedOperationException("Tiling not supported!");
        }
        if (this.getTilingMode() != 2) {
            throw new IllegalStateException("Tiling mode not MODE_EXPLICIT!");
        }
        this.tilingSet = false;
        this.tileWidth = 0;
        this.tileHeight = 0;
        this.tileGridXOffset = 0;
        this.tileGridYOffset = 0;
    }

    public int getTileWidth() {
        if (!this.canWriteTiles()) {
            throw new UnsupportedOperationException("Tiling not supported!");
        }
        if (this.getTilingMode() != 2) {
            throw new IllegalStateException("Tiling mode not MODE_EXPLICIT!");
        }
        if (!this.tilingSet) {
            throw new IllegalStateException("Tiling parameters not set!");
        }
        return this.tileWidth;
    }

    public int getTileHeight() {
        if (!this.canWriteTiles()) {
            throw new UnsupportedOperationException("Tiling not supported!");
        }
        if (this.getTilingMode() != 2) {
            throw new IllegalStateException("Tiling mode not MODE_EXPLICIT!");
        }
        if (!this.tilingSet) {
            throw new IllegalStateException("Tiling parameters not set!");
        }
        return this.tileHeight;
    }

    public int getTileGridXOffset() {
        if (!this.canWriteTiles()) {
            throw new UnsupportedOperationException("Tiling not supported!");
        }
        if (this.getTilingMode() != 2) {
            throw new IllegalStateException("Tiling mode not MODE_EXPLICIT!");
        }
        if (!this.tilingSet) {
            throw new IllegalStateException("Tiling parameters not set!");
        }
        return this.tileGridXOffset;
    }

    public int getTileGridYOffset() {
        if (!this.canWriteTiles()) {
            throw new UnsupportedOperationException("Tiling not supported!");
        }
        if (this.getTilingMode() != 2) {
            throw new IllegalStateException("Tiling mode not MODE_EXPLICIT!");
        }
        if (!this.tilingSet) {
            throw new IllegalStateException("Tiling parameters not set!");
        }
        return this.tileGridYOffset;
    }

    public boolean canWriteProgressive() {
        return this.canWriteProgressive;
    }

    public void setProgressiveMode(int mode) {
        if (!this.canWriteProgressive()) {
            throw new UnsupportedOperationException("Progressive output not supported");
        }
        if (mode < 0 || mode > 3) {
            throw new IllegalArgumentException("Illegal value for mode!");
        }
        if (mode == 2) {
            throw new IllegalArgumentException("MODE_EXPLICIT not supported for progressive output");
        }
        this.progressiveMode = mode;
    }

    public int getProgressiveMode() {
        if (!this.canWriteProgressive()) {
            throw new UnsupportedOperationException("Progressive output not supported");
        }
        return this.progressiveMode;
    }

    public boolean canWriteCompressed() {
        return this.canWriteCompressed;
    }

    public void setCompressionMode(int mode) {
        if (!this.canWriteCompressed()) {
            throw new UnsupportedOperationException("Compression not supported.");
        }
        if (mode < 0 || mode > 3) {
            throw new IllegalArgumentException("Illegal value for mode!");
        }
        this.compressionMode = mode;
        if (mode == 2) {
            this.unsetCompression();
        }
    }

    public int getCompressionMode() {
        if (!this.canWriteCompressed()) {
            throw new UnsupportedOperationException("Compression not supported.");
        }
        return this.compressionMode;
    }

    public String[] getCompressionTypes() {
        if (!this.canWriteCompressed()) {
            throw new UnsupportedOperationException("Compression not supported");
        }
        if (this.compressionTypes == null) {
            return null;
        }
        return (String[])this.compressionTypes.clone();
    }

    public void setCompressionType(String compressionType) {
        if (!this.canWriteCompressed()) {
            throw new UnsupportedOperationException("Compression not supported");
        }
        if (this.getCompressionMode() != 2) {
            throw new IllegalStateException("Compression mode not MODE_EXPLICIT!");
        }
        String[] legalTypes = this.getCompressionTypes();
        if (legalTypes == null) {
            throw new UnsupportedOperationException("No settable compression types");
        }
        if (compressionType != null) {
            boolean found = false;
            if (legalTypes != null) {
                for (int i = 0; i < legalTypes.length; ++i) {
                    if (!compressionType.equals(legalTypes[i])) continue;
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Unknown compression type!");
            }
        }
        this.compressionType = compressionType;
    }

    public String getCompressionType() {
        if (!this.canWriteCompressed()) {
            throw new UnsupportedOperationException("Compression not supported.");
        }
        if (this.getCompressionMode() != 2) {
            throw new IllegalStateException("Compression mode not MODE_EXPLICIT!");
        }
        return this.compressionType;
    }

    public void unsetCompression() {
        if (!this.canWriteCompressed()) {
            throw new UnsupportedOperationException("Compression not supported");
        }
        if (this.getCompressionMode() != 2) {
            throw new IllegalStateException("Compression mode not MODE_EXPLICIT!");
        }
        this.compressionType = null;
        this.compressionQuality = 1.0f;
    }

    public String getLocalizedCompressionTypeName() {
        if (!this.canWriteCompressed()) {
            throw new UnsupportedOperationException("Compression not supported.");
        }
        if (this.getCompressionMode() != 2) {
            throw new IllegalStateException("Compression mode not MODE_EXPLICIT!");
        }
        if (this.getCompressionType() == null) {
            throw new IllegalStateException("No compression type set!");
        }
        return this.getCompressionType();
    }

    public boolean isCompressionLossless() {
        if (!this.canWriteCompressed()) {
            throw new UnsupportedOperationException("Compression not supported");
        }
        if (this.getCompressionMode() != 2) {
            throw new IllegalStateException("Compression mode not MODE_EXPLICIT!");
        }
        if (this.getCompressionTypes() != null && this.getCompressionType() == null) {
            throw new IllegalStateException("No compression type set!");
        }
        return true;
    }

    public void setCompressionQuality(float quality) {
        if (!this.canWriteCompressed()) {
            throw new UnsupportedOperationException("Compression not supported");
        }
        if (this.getCompressionMode() != 2) {
            throw new IllegalStateException("Compression mode not MODE_EXPLICIT!");
        }
        if (this.getCompressionTypes() != null && this.getCompressionType() == null) {
            throw new IllegalStateException("No compression type set!");
        }
        if (quality < 0.0f || quality > 1.0f) {
            throw new IllegalArgumentException("Quality out of bounds!");
        }
        this.compressionQuality = quality;
    }

    public float getCompressionQuality() {
        if (!this.canWriteCompressed()) {
            throw new UnsupportedOperationException("Compression not supported.");
        }
        if (this.getCompressionMode() != 2) {
            throw new IllegalStateException("Compression mode not MODE_EXPLICIT!");
        }
        if (this.getCompressionTypes() != null && this.getCompressionType() == null) {
            throw new IllegalStateException("No compression type set!");
        }
        return this.compressionQuality;
    }

    public float getBitRate(float quality) {
        if (!this.canWriteCompressed()) {
            throw new UnsupportedOperationException("Compression not supported.");
        }
        if (this.getCompressionMode() != 2) {
            throw new IllegalStateException("Compression mode not MODE_EXPLICIT!");
        }
        if (this.getCompressionTypes() != null && this.getCompressionType() == null) {
            throw new IllegalStateException("No compression type set!");
        }
        if (quality < 0.0f || quality > 1.0f) {
            throw new IllegalArgumentException("Quality out of bounds!");
        }
        return -1.0f;
    }

    public String[] getCompressionQualityDescriptions() {
        if (!this.canWriteCompressed()) {
            throw new UnsupportedOperationException("Compression not supported.");
        }
        if (this.getCompressionMode() != 2) {
            throw new IllegalStateException("Compression mode not MODE_EXPLICIT!");
        }
        if (this.getCompressionTypes() != null && this.getCompressionType() == null) {
            throw new IllegalStateException("No compression type set!");
        }
        return null;
    }

    public float[] getCompressionQualityValues() {
        if (!this.canWriteCompressed()) {
            throw new UnsupportedOperationException("Compression not supported.");
        }
        if (this.getCompressionMode() != 2) {
            throw new IllegalStateException("Compression mode not MODE_EXPLICIT!");
        }
        if (this.getCompressionTypes() != null && this.getCompressionType() == null) {
            throw new IllegalStateException("No compression type set!");
        }
        return null;
    }
}

