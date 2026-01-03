/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import sun.font.Font2D;
import sun.font.FontManager;
import sun.font.FontManagerFactory;
import sun.font.FontScalerException;
import sun.font.FreetypeFontScaler;
import sun.font.NullFontScaler;
import sun.font.StrikeMetrics;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;

public abstract class FontScaler
implements DisposerRecord {
    private static FontScaler nullScaler = null;
    protected WeakReference<Font2D> font = null;
    protected long nativeScaler = 0L;
    protected boolean disposed = false;

    public static FontScaler getScaler(Font2D font, int indexInCollection, boolean supportsCJK, int filesize) {
        FontScaler scaler = null;
        try {
            scaler = new FreetypeFontScaler(font, indexInCollection, supportsCJK, filesize);
            Disposer.addObjectRecord(font, scaler);
        }
        catch (Throwable e) {
            scaler = FontScaler.getNullScaler();
            FontManager fm = FontManagerFactory.getInstance();
            fm.deRegisterBadFont(font);
        }
        return scaler;
    }

    public static synchronized FontScaler getNullScaler() {
        if (nullScaler == null) {
            nullScaler = new NullFontScaler();
        }
        return nullScaler;
    }

    abstract StrikeMetrics getFontMetrics(long var1) throws FontScalerException;

    abstract float getGlyphAdvance(long var1, int var3) throws FontScalerException;

    abstract void getGlyphMetrics(long var1, int var3, Point2D.Float var4) throws FontScalerException;

    abstract long getGlyphImage(long var1, int var3) throws FontScalerException;

    abstract Rectangle2D.Float getGlyphOutlineBounds(long var1, int var3) throws FontScalerException;

    abstract GeneralPath getGlyphOutline(long var1, int var3, float var4, float var5) throws FontScalerException;

    abstract GeneralPath getGlyphVectorOutline(long var1, int[] var3, int var4, float var5, float var6) throws FontScalerException;

    @Override
    public void dispose() {
    }

    public void disposeScaler() {
    }

    abstract int getNumGlyphs() throws FontScalerException;

    abstract int getMissingGlyphCode() throws FontScalerException;

    abstract int getGlyphCode(char var1) throws FontScalerException;

    abstract Point2D.Float getGlyphPoint(long var1, int var3, int var4) throws FontScalerException;

    abstract long getUnitsPerEm();

    abstract long createScalerContext(double[] var1, int var2, int var3, float var4, float var5);

    abstract void invalidateScalerContext(long var1);
}

