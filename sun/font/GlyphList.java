/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.font.GlyphVector;
import java.util.concurrent.atomic.AtomicBoolean;
import sun.font.ColorGlyphSurfaceData;
import sun.font.FontUtilities;
import sun.font.StandardGlyphVector;
import sun.font.StrikeCache;
import sun.java2d.SurfaceData;
import sun.java2d.loops.FontInfo;

public final class GlyphList {
    private static final int MINGRAYLENGTH = 1024;
    private static final int MAXGRAYLENGTH = 8192;
    private static final int DEFAULT_LENGTH = 32;
    int glyphindex;
    int[] metrics;
    byte[] graybits;
    Object strikelist;
    int len = 0;
    int maxLen = 0;
    int maxPosLen = 0;
    int[] glyphData;
    char[] chData;
    long[] images;
    float[] positions;
    float x;
    float y;
    float gposx;
    float gposy;
    boolean usePositions;
    boolean lcdRGBOrder;
    boolean lcdSubPixPos;
    private static final GlyphList reusableGL = new GlyphList();
    private static final AtomicBoolean inUse = new AtomicBoolean();
    private ColorGlyphSurfaceData glyphSurfaceData;

    void ensureCapacity(int len) {
        if (len < 0) {
            len = 0;
        }
        if (this.usePositions && len > this.maxPosLen) {
            this.positions = new float[len * 2 + 2];
            this.maxPosLen = len;
        }
        if (this.maxLen == 0 || len > this.maxLen) {
            this.glyphData = new int[len];
            this.chData = new char[len];
            this.images = new long[len];
            this.maxLen = len;
        }
    }

    private GlyphList() {
    }

    public static GlyphList getInstance() {
        if (inUse.compareAndSet(false, true)) {
            return reusableGL;
        }
        return new GlyphList();
    }

    public boolean setFromString(FontInfo info, String str, float x, float y) {
        this.x = x;
        this.y = y;
        this.strikelist = info.fontStrike;
        this.lcdRGBOrder = info.lcdRGBOrder;
        this.lcdSubPixPos = info.lcdSubPixPos;
        this.len = str.length();
        this.ensureCapacity(this.len);
        str.getChars(0, this.len, this.chData, 0);
        return this.mapChars(info, this.len);
    }

    public boolean setFromChars(FontInfo info, char[] chars, int off, int alen, float x, float y) {
        this.x = x;
        this.y = y;
        this.strikelist = info.fontStrike;
        this.lcdRGBOrder = info.lcdRGBOrder;
        this.lcdSubPixPos = info.lcdSubPixPos;
        this.len = alen;
        this.len = alen < 0 ? 0 : alen;
        this.ensureCapacity(this.len);
        System.arraycopy(chars, off, this.chData, 0, this.len);
        return this.mapChars(info, this.len);
    }

    private boolean mapChars(FontInfo info, int len) {
        if (info.font2D.getMapper().charsToGlyphsNS(len, this.chData, this.glyphData)) {
            return false;
        }
        info.fontStrike.getGlyphImagePtrs(this.glyphData, this.images, len);
        this.glyphindex = -1;
        return true;
    }

    public void setFromGlyphVector(FontInfo info, GlyphVector gv, float x, float y) {
        this.x = x;
        this.y = y;
        this.lcdRGBOrder = info.lcdRGBOrder;
        this.lcdSubPixPos = info.lcdSubPixPos;
        StandardGlyphVector sgv = StandardGlyphVector.getStandardGV(gv, info);
        this.usePositions = sgv.needsPositions(info.devTx);
        this.len = sgv.getNumGlyphs();
        this.ensureCapacity(this.len);
        this.strikelist = sgv.setupGlyphImages(this.images, this.usePositions ? this.positions : null, info.devTx);
        this.glyphindex = -1;
    }

    public void startGlyphIteration() {
        if (this.glyphindex >= 0) {
            throw new InternalError("glyph iteration restarted");
        }
        if (this.metrics == null) {
            this.metrics = new int[5];
        }
        this.gposx = this.x + 0.5f;
        this.gposy = this.y + 0.5f;
    }

    public int[] getBounds(int endGlyphIndex) {
        this.fillBounds(this.metrics, endGlyphIndex);
        return this.metrics;
    }

    public void setGlyphIndex(int i) {
        this.glyphindex = i;
        if (this.images[i] == 0L) {
            this.metrics[0] = (int)this.gposx;
            this.metrics[1] = (int)this.gposy;
            this.metrics[2] = 0;
            this.metrics[3] = 0;
            this.metrics[4] = 0;
            return;
        }
        float gx = StrikeCache.unsafe.getFloat(this.images[i] + (long)StrikeCache.topLeftXOffset);
        float gy = StrikeCache.unsafe.getFloat(this.images[i] + (long)StrikeCache.topLeftYOffset);
        if (this.usePositions) {
            this.metrics[0] = (int)Math.floor(this.positions[i << 1] + this.gposx + gx);
            this.metrics[1] = (int)Math.floor(this.positions[(i << 1) + 1] + this.gposy + gy);
        } else {
            this.metrics[0] = (int)Math.floor(this.gposx + gx);
            this.metrics[1] = (int)Math.floor(this.gposy + gy);
            this.gposx += StrikeCache.unsafe.getFloat(this.images[i] + (long)StrikeCache.xAdvanceOffset);
            this.gposy += StrikeCache.unsafe.getFloat(this.images[i] + (long)StrikeCache.yAdvanceOffset);
        }
        this.metrics[2] = StrikeCache.unsafe.getChar(this.images[i] + (long)StrikeCache.widthOffset);
        this.metrics[3] = StrikeCache.unsafe.getChar(this.images[i] + (long)StrikeCache.heightOffset);
        this.metrics[4] = StrikeCache.unsafe.getChar(this.images[i] + (long)StrikeCache.rowBytesOffset);
    }

    public int[] getMetrics() {
        return this.metrics;
    }

    public byte[] getGrayBits() {
        int len = this.metrics[4] * this.metrics[3];
        if (this.graybits == null) {
            this.graybits = new byte[Math.max(len, 1024)];
        } else if (len > this.graybits.length) {
            this.graybits = new byte[len];
        }
        if (this.images[this.glyphindex] == 0L) {
            return this.graybits;
        }
        long pixelDataAddress = StrikeCache.unsafe.getAddress(this.images[this.glyphindex] + (long)StrikeCache.pixelDataOffset);
        if (pixelDataAddress == 0L) {
            return this.graybits;
        }
        for (int i = 0; i < len; ++i) {
            this.graybits[i] = StrikeCache.unsafe.getByte(pixelDataAddress + (long)i);
        }
        return this.graybits;
    }

    public long[] getImages() {
        return this.images;
    }

    public boolean usePositions() {
        return this.usePositions;
    }

    public float[] getPositions() {
        return this.positions;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public Object getStrike() {
        return this.strikelist;
    }

    public boolean isSubPixPos() {
        return this.lcdSubPixPos;
    }

    public boolean isRGBOrder() {
        return this.lcdRGBOrder;
    }

    public void dispose() {
        if (this == reusableGL) {
            if (this.graybits != null && this.graybits.length > 8192) {
                this.graybits = null;
            }
            this.usePositions = false;
            this.strikelist = null;
            inUse.set(false);
        }
    }

    public int getNumGlyphs() {
        return this.len;
    }

    private void fillBounds(int[] bounds, int endGlyphIndex) {
        int xOffset = StrikeCache.topLeftXOffset;
        int yOffset = StrikeCache.topLeftYOffset;
        int wOffset = StrikeCache.widthOffset;
        int hOffset = StrikeCache.heightOffset;
        int xAdvOffset = StrikeCache.xAdvanceOffset;
        int yAdvOffset = StrikeCache.yAdvanceOffset;
        int startGlyphIndex = this.glyphindex + 1;
        if (startGlyphIndex >= endGlyphIndex) {
            bounds[3] = 0;
            bounds[2] = 0;
            bounds[1] = 0;
            bounds[0] = 0;
            return;
        }
        float by0 = Float.POSITIVE_INFINITY;
        float bx0 = Float.POSITIVE_INFINITY;
        float by1 = Float.NEGATIVE_INFINITY;
        float bx1 = Float.NEGATIVE_INFINITY;
        int posIndex = startGlyphIndex << 1;
        float glx = this.gposx;
        float gly = this.gposy;
        for (int i = startGlyphIndex; i < endGlyphIndex; ++i) {
            float gy0;
            float gx0;
            if (this.images[i] == 0L) continue;
            float gx = StrikeCache.unsafe.getFloat(this.images[i] + (long)xOffset);
            float gy = StrikeCache.unsafe.getFloat(this.images[i] + (long)yOffset);
            char gw = StrikeCache.unsafe.getChar(this.images[i] + (long)wOffset);
            char gh = StrikeCache.unsafe.getChar(this.images[i] + (long)hOffset);
            if (this.usePositions) {
                gx0 = this.positions[posIndex++] + gx + glx;
                gy0 = this.positions[posIndex++] + gy + gly;
            } else {
                gx0 = glx + gx;
                gy0 = gly + gy;
                glx += StrikeCache.unsafe.getFloat(this.images[i] + (long)xAdvOffset);
                gly += StrikeCache.unsafe.getFloat(this.images[i] + (long)yAdvOffset);
            }
            float gx1 = gx0 + (float)gw;
            float gy1 = gy0 + (float)gh;
            if (bx0 > gx0) {
                bx0 = gx0;
            }
            if (by0 > gy0) {
                by0 = gy0;
            }
            if (bx1 < gx1) {
                bx1 = gx1;
            }
            if (!(by1 < gy1)) continue;
            by1 = gy1;
        }
        bounds[0] = (int)Math.floor(bx0);
        bounds[1] = (int)Math.floor(by0);
        bounds[2] = (int)Math.floor(bx1);
        bounds[3] = (int)Math.floor(by1);
    }

    public static boolean canContainColorGlyphs() {
        return FontUtilities.isMacOSX;
    }

    public boolean isColorGlyph(int glyphIndex) {
        char width = StrikeCache.unsafe.getChar(this.images[glyphIndex] + (long)StrikeCache.widthOffset);
        char rowBytes = StrikeCache.unsafe.getChar(this.images[glyphIndex] + (long)StrikeCache.rowBytesOffset);
        return rowBytes == width * 4;
    }

    public SurfaceData getColorGlyphData() {
        if (this.glyphSurfaceData == null) {
            this.glyphSurfaceData = new ColorGlyphSurfaceData();
        }
        this.glyphSurfaceData.setCurrentGlyph(this.images[this.glyphindex]);
        return this.glyphSurfaceData;
    }
}

