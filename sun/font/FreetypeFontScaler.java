/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import sun.font.Font2D;
import sun.font.FontManagerNativeLibrary;
import sun.font.FontScaler;
import sun.font.FontScalerException;
import sun.font.NullFontScaler;
import sun.font.StrikeMetrics;
import sun.font.Type1Font;

class FreetypeFontScaler
extends FontScaler {
    private static final int TRUETYPE_FONT = 1;
    private static final int TYPE1_FONT = 2;

    private static native void initIDs(Class<?> var0);

    private void invalidateScaler() throws FontScalerException {
        this.nativeScaler = 0L;
        this.font = null;
        throw new FontScalerException();
    }

    public FreetypeFontScaler(Font2D font, int indexInCollection, boolean supportsCJK, int filesize) {
        int fonttype = 1;
        if (font instanceof Type1Font) {
            fonttype = 2;
        }
        this.nativeScaler = this.initNativeScaler(font, fonttype, indexInCollection, supportsCJK, filesize);
        this.font = new WeakReference<Font2D>(font);
    }

    @Override
    synchronized StrikeMetrics getFontMetrics(long pScalerContext) throws FontScalerException {
        if (this.nativeScaler != 0L) {
            return this.getFontMetricsNative((Font2D)this.font.get(), pScalerContext, this.nativeScaler);
        }
        return FontScaler.getNullScaler().getFontMetrics(0L);
    }

    @Override
    synchronized float getGlyphAdvance(long pScalerContext, int glyphCode) throws FontScalerException {
        if (this.nativeScaler != 0L) {
            return this.getGlyphAdvanceNative((Font2D)this.font.get(), pScalerContext, this.nativeScaler, glyphCode);
        }
        return FontScaler.getNullScaler().getGlyphAdvance(0L, glyphCode);
    }

    @Override
    synchronized void getGlyphMetrics(long pScalerContext, int glyphCode, Point2D.Float metrics) throws FontScalerException {
        if (this.nativeScaler != 0L) {
            this.getGlyphMetricsNative((Font2D)this.font.get(), pScalerContext, this.nativeScaler, glyphCode, metrics);
            return;
        }
        FontScaler.getNullScaler().getGlyphMetrics(0L, glyphCode, metrics);
    }

    @Override
    synchronized long getGlyphImage(long pScalerContext, int glyphCode) throws FontScalerException {
        if (this.nativeScaler != 0L) {
            return this.getGlyphImageNative((Font2D)this.font.get(), pScalerContext, this.nativeScaler, glyphCode);
        }
        return FontScaler.getNullScaler().getGlyphImage(0L, glyphCode);
    }

    @Override
    synchronized Rectangle2D.Float getGlyphOutlineBounds(long pScalerContext, int glyphCode) throws FontScalerException {
        if (this.nativeScaler != 0L) {
            return this.getGlyphOutlineBoundsNative((Font2D)this.font.get(), pScalerContext, this.nativeScaler, glyphCode);
        }
        return FontScaler.getNullScaler().getGlyphOutlineBounds(0L, glyphCode);
    }

    @Override
    synchronized GeneralPath getGlyphOutline(long pScalerContext, int glyphCode, float x, float y) throws FontScalerException {
        if (this.nativeScaler != 0L) {
            return this.getGlyphOutlineNative((Font2D)this.font.get(), pScalerContext, this.nativeScaler, glyphCode, x, y);
        }
        return FontScaler.getNullScaler().getGlyphOutline(0L, glyphCode, x, y);
    }

    @Override
    synchronized GeneralPath getGlyphVectorOutline(long pScalerContext, int[] glyphs, int numGlyphs, float x, float y) throws FontScalerException {
        if (this.nativeScaler != 0L) {
            return this.getGlyphVectorOutlineNative((Font2D)this.font.get(), pScalerContext, this.nativeScaler, glyphs, numGlyphs, x, y);
        }
        return FontScaler.getNullScaler().getGlyphVectorOutline(0L, glyphs, numGlyphs, x, y);
    }

    @Override
    public synchronized void dispose() {
        if (this.nativeScaler != 0L) {
            this.disposeNativeScaler((Font2D)this.font.get(), this.nativeScaler);
            this.nativeScaler = 0L;
        }
    }

    @Override
    public synchronized void disposeScaler() {
        if (this.nativeScaler != 0L) {
            new Thread(null, () -> this.dispose(), "free scaler", 0L, false).start();
        }
    }

    @Override
    synchronized int getNumGlyphs() throws FontScalerException {
        if (this.nativeScaler != 0L) {
            return this.getNumGlyphsNative(this.nativeScaler);
        }
        return FontScaler.getNullScaler().getNumGlyphs();
    }

    @Override
    synchronized int getMissingGlyphCode() throws FontScalerException {
        if (this.nativeScaler != 0L) {
            return this.getMissingGlyphCodeNative(this.nativeScaler);
        }
        return FontScaler.getNullScaler().getMissingGlyphCode();
    }

    @Override
    synchronized int getGlyphCode(char charCode) throws FontScalerException {
        if (this.nativeScaler != 0L) {
            return this.getGlyphCodeNative((Font2D)this.font.get(), this.nativeScaler, charCode);
        }
        return FontScaler.getNullScaler().getGlyphCode(charCode);
    }

    @Override
    synchronized Point2D.Float getGlyphPoint(long pScalerContext, int glyphCode, int ptNumber) throws FontScalerException {
        if (this.nativeScaler != 0L) {
            return this.getGlyphPointNative((Font2D)this.font.get(), pScalerContext, this.nativeScaler, glyphCode, ptNumber);
        }
        return FontScaler.getNullScaler().getGlyphPoint(pScalerContext, glyphCode, ptNumber);
    }

    @Override
    synchronized long getUnitsPerEm() {
        return this.getUnitsPerEMNative(this.nativeScaler);
    }

    @Override
    synchronized long createScalerContext(double[] matrix, int aa, int fm, float boldness, float italic) {
        if (this.nativeScaler != 0L) {
            return this.createScalerContextNative(this.nativeScaler, matrix, aa, fm, boldness, italic);
        }
        return NullFontScaler.getNullScalerContext();
    }

    private native long initNativeScaler(Font2D var1, int var2, int var3, boolean var4, int var5);

    private native StrikeMetrics getFontMetricsNative(Font2D var1, long var2, long var4);

    private native float getGlyphAdvanceNative(Font2D var1, long var2, long var4, int var6);

    private native void getGlyphMetricsNative(Font2D var1, long var2, long var4, int var6, Point2D.Float var7);

    private native long getGlyphImageNative(Font2D var1, long var2, long var4, int var6);

    private native Rectangle2D.Float getGlyphOutlineBoundsNative(Font2D var1, long var2, long var4, int var6);

    private native GeneralPath getGlyphOutlineNative(Font2D var1, long var2, long var4, int var6, float var7, float var8);

    private native GeneralPath getGlyphVectorOutlineNative(Font2D var1, long var2, long var4, int[] var6, int var7, float var8, float var9);

    private native Point2D.Float getGlyphPointNative(Font2D var1, long var2, long var4, int var6, int var7);

    private native void disposeNativeScaler(Font2D var1, long var2);

    private native int getGlyphCodeNative(Font2D var1, long var2, char var4);

    private native int getNumGlyphsNative(long var1);

    private native int getMissingGlyphCodeNative(long var1);

    private native long getUnitsPerEMNative(long var1);

    private native long createScalerContextNative(long var1, double[] var3, int var4, int var5, float var6, float var7);

    @Override
    void invalidateScalerContext(long pScalerContext) {
    }

    static {
        FontManagerNativeLibrary.load();
        FreetypeFontScaler.initIDs(FreetypeFontScaler.class);
    }
}

