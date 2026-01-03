/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.FontFormatException;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import sun.font.CreatedFontTracker;
import sun.font.FileFontStrike;
import sun.font.Font2D;
import sun.font.FontScaler;
import sun.font.FontScalerException;
import sun.font.FontStrike;
import sun.font.FontStrikeDesc;
import sun.font.NativeFont;
import sun.font.PhysicalFont;
import sun.font.StrikeMetrics;
import sun.font.SunFontManager;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;

public abstract class FileFont
extends PhysicalFont {
    protected boolean useJavaRasterizer = true;
    protected int fileSize;
    protected FontScaler scaler;
    protected NativeFont[] nativeFonts;
    protected char[] glyphToCharMap;

    FileFont(String platname, Object nativeNames) throws FontFormatException {
        super(platname, nativeNames);
    }

    @Override
    FontStrike createStrike(FontStrikeDesc desc) {
        return new FileFontStrike(this, desc);
    }

    protected abstract void close();

    abstract ByteBuffer readBlock(int var1, int var2);

    @Override
    public boolean canDoStyle(int style) {
        return true;
    }

    static void setFileToRemove(List<Font2D> fonts, File file, int cnt, CreatedFontTracker tracker) {
        CreatedFontFileDisposerRecord dr = new CreatedFontFileDisposerRecord(file, cnt, tracker);
        for (Font2D f : fonts) {
            Disposer.addObjectRecord(f, dr);
        }
    }

    synchronized void deregisterFontAndClearStrikeCache() {
        SunFontManager fm = SunFontManager.getInstance();
        fm.deRegisterBadFont(this);
        for (Reference strikeRef : this.strikeCache.values()) {
            FileFontStrike strike;
            if (strikeRef == null || (strike = (FileFontStrike)strikeRef.get()) == null || strike.pScalerContext == 0L) continue;
            this.scaler.invalidateScalerContext(strike.pScalerContext);
        }
        if (this.scaler != null) {
            this.scaler.disposeScaler();
        }
        this.scaler = FontScaler.getNullScaler();
    }

    @Override
    StrikeMetrics getFontMetrics(long pScalerContext) {
        try {
            return this.getScaler().getFontMetrics(pScalerContext);
        }
        catch (FontScalerException fe) {
            this.scaler = FontScaler.getNullScaler();
            return this.getFontMetrics(pScalerContext);
        }
    }

    @Override
    float getGlyphAdvance(long pScalerContext, int glyphCode) {
        try {
            return this.getScaler().getGlyphAdvance(pScalerContext, glyphCode);
        }
        catch (FontScalerException fe) {
            this.scaler = FontScaler.getNullScaler();
            return this.getGlyphAdvance(pScalerContext, glyphCode);
        }
    }

    @Override
    void getGlyphMetrics(long pScalerContext, int glyphCode, Point2D.Float metrics) {
        try {
            this.getScaler().getGlyphMetrics(pScalerContext, glyphCode, metrics);
        }
        catch (FontScalerException fe) {
            this.scaler = FontScaler.getNullScaler();
            this.getGlyphMetrics(pScalerContext, glyphCode, metrics);
        }
    }

    @Override
    long getGlyphImage(long pScalerContext, int glyphCode) {
        try {
            return this.getScaler().getGlyphImage(pScalerContext, glyphCode);
        }
        catch (FontScalerException fe) {
            this.scaler = FontScaler.getNullScaler();
            return this.getGlyphImage(pScalerContext, glyphCode);
        }
    }

    @Override
    Rectangle2D.Float getGlyphOutlineBounds(long pScalerContext, int glyphCode) {
        try {
            return this.getScaler().getGlyphOutlineBounds(pScalerContext, glyphCode);
        }
        catch (FontScalerException fe) {
            this.scaler = FontScaler.getNullScaler();
            return this.getGlyphOutlineBounds(pScalerContext, glyphCode);
        }
    }

    @Override
    GeneralPath getGlyphOutline(long pScalerContext, int glyphCode, float x, float y) {
        try {
            return this.getScaler().getGlyphOutline(pScalerContext, glyphCode, x, y);
        }
        catch (FontScalerException fe) {
            this.scaler = FontScaler.getNullScaler();
            return this.getGlyphOutline(pScalerContext, glyphCode, x, y);
        }
    }

    @Override
    GeneralPath getGlyphVectorOutline(long pScalerContext, int[] glyphs, int numGlyphs, float x, float y) {
        try {
            return this.getScaler().getGlyphVectorOutline(pScalerContext, glyphs, numGlyphs, x, y);
        }
        catch (FontScalerException fe) {
            this.scaler = FontScaler.getNullScaler();
            return this.getGlyphVectorOutline(pScalerContext, glyphs, numGlyphs, x, y);
        }
    }

    protected abstract FontScaler getScaler();

    @Override
    protected long getUnitsPerEm() {
        return this.getScaler().getUnitsPerEm();
    }

    protected String getPublicFileName() {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return this.platName;
        }
        boolean canReadProperty = true;
        try {
            sm.checkPropertyAccess("java.io.tmpdir");
        }
        catch (SecurityException e) {
            canReadProperty = false;
        }
        if (canReadProperty) {
            return this.platName;
        }
        final File f = new File(this.platName);
        Boolean isTmpFile = Boolean.FALSE;
        try {
            isTmpFile = AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>(){

                @Override
                public Boolean run() {
                    File tmp = new File(System.getProperty("java.io.tmpdir"));
                    try {
                        String tpath = tmp.getCanonicalPath();
                        String fpath = f.getCanonicalPath();
                        return fpath == null || fpath.startsWith(tpath);
                    }
                    catch (IOException e) {
                        return Boolean.TRUE;
                    }
                }
            });
        }
        catch (PrivilegedActionException e) {
            isTmpFile = Boolean.TRUE;
        }
        return isTmpFile != false ? "temp file" : this.platName;
    }

    private static class CreatedFontFileDisposerRecord
    implements DisposerRecord {
        File fontFile = null;
        int count = 0;
        CreatedFontTracker tracker;

        private CreatedFontFileDisposerRecord(File file, int cnt, CreatedFontTracker tracker) {
            this.fontFile = file;
            this.count = cnt > 0 ? cnt : 1;
            this.tracker = tracker;
        }

        @Override
        public void dispose() {
            AccessController.doPrivileged(new PrivilegedAction<Object>(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public Object run() {
                    File file = fontFile;
                    synchronized (file) {
                        --count;
                        if (count > 0) {
                            return null;
                        }
                    }
                    if (fontFile != null) {
                        try {
                            if (tracker != null) {
                                tracker.subBytes((int)fontFile.length());
                            }
                            fontFile.delete();
                            SunFontManager.getInstance().tmpFontFiles.remove(fontFile);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    return null;
                }
            });
        }
    }
}

