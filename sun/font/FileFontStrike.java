/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import sun.font.CharToGlyphMapper;
import sun.font.FileFont;
import sun.font.FontScaler;
import sun.font.FontStrikeDesc;
import sun.font.FontStrikeDisposer;
import sun.font.FontUtilities;
import sun.font.NativeStrike;
import sun.font.NullFontScaler;
import sun.font.PhysicalStrike;
import sun.font.StrikeCache;
import sun.font.StrikeMetrics;
import sun.font.TrueTypeFont;

public class FileFontStrike
extends PhysicalStrike {
    static final int INVISIBLE_GLYPHS = 65534;
    private FileFont fileFont;
    private static final int UNINITIALISED = 0;
    private static final int INTARRAY = 1;
    private static final int LONGARRAY = 2;
    private static final int SEGINTARRAY = 3;
    private static final int SEGLONGARRAY = 4;
    private volatile int glyphCacheFormat = 0;
    private static final int SEGSHIFT = 5;
    private static final int SEGSIZE = 32;
    private boolean segmentedCache;
    private int[][] segIntGlyphImages;
    private long[][] segLongGlyphImages;
    private float[] horizontalAdvances;
    private float[][] segHorizontalAdvances;
    ConcurrentHashMap<Integer, Rectangle2D.Float> boundsMap;
    SoftReference<ConcurrentHashMap<Integer, Point2D.Float>> glyphMetricsMapRef;
    AffineTransform invertDevTx;
    boolean useNatives;
    NativeStrike[] nativeStrikes;
    static final int MAX_IMAGE_SIZE = 100;
    private int intPtSize;
    private static boolean isXPorLater = false;
    private WeakReference<ConcurrentHashMap<Integer, GeneralPath>> outlineMapRef;

    private static native boolean initNative();

    FileFontStrike(FileFont fileFont, FontStrikeDesc desc) {
        super(fileFont, desc);
        this.fileFont = fileFont;
        if (desc.style != fileFont.style) {
            if ((desc.style & 2) == 2 && (fileFont.style & 2) == 0) {
                this.algoStyle = true;
                this.italic = 0.7f;
            }
            if ((desc.style & 1) == 1 && (fileFont.style & 1) == 0) {
                this.algoStyle = true;
                this.boldness = 1.33f;
            }
        }
        double[] matrix = new double[4];
        AffineTransform at = desc.glyphTx;
        at.getMatrix(matrix);
        if (!desc.devTx.isIdentity() && desc.devTx.getType() != 1) {
            try {
                this.invertDevTx = desc.devTx.createInverse();
            }
            catch (NoninvertibleTransformException noninvertibleTransformException) {
                // empty catch block
            }
        }
        this.pScalerContext = Double.isNaN(matrix[0]) || Double.isNaN(matrix[1]) || Double.isNaN(matrix[2]) || Double.isNaN(matrix[3]) || fileFont.getScaler() == null ? NullFontScaler.getNullScalerContext() : fileFont.getScaler().createScalerContext(matrix, desc.aaHint, desc.fmHint, this.boldness, this.italic);
        this.mapper = fileFont.getMapper();
        int numGlyphs = this.mapper.getNumGlyphs();
        float ptSize = (float)matrix[3];
        int iSize = this.intPtSize = (int)ptSize;
        boolean isSimpleTx = (at.getType() & 0x7C) == 0;
        boolean bl = this.segmentedCache = numGlyphs > 256 || numGlyphs > 64 && (!isSimpleTx || ptSize != (float)iSize || iSize < 6 || iSize > 36);
        if (this.pScalerContext == 0L) {
            this.disposer = new FontStrikeDisposer(fileFont, desc);
            this.initGlyphCache();
            this.pScalerContext = NullFontScaler.getNullScalerContext();
            return;
        }
        if (FontUtilities.isWindows && isXPorLater && !FontUtilities.useJDKScaler && !GraphicsEnvironment.isHeadless() && !fileFont.useJavaRasterizer && (desc.aaHint == 4 || desc.aaHint == 5) && matrix[1] == 0.0 && matrix[2] == 0.0 && matrix[0] == matrix[3] && matrix[0] >= 3.0 && matrix[0] <= 100.0 && !((TrueTypeFont)fileFont).useEmbeddedBitmapsForSize(this.intPtSize)) {
            this.useNatives = true;
        }
        if (FontUtilities.isLogging() && FontUtilities.isWindows) {
            FontUtilities.logInfo("Strike for " + String.valueOf(fileFont) + " at size = " + this.intPtSize + " use natives = " + this.useNatives + " useJavaRasteriser = " + fileFont.useJavaRasterizer + " AAHint = " + desc.aaHint + " Has Embedded bitmaps = " + ((TrueTypeFont)fileFont).useEmbeddedBitmapsForSize(this.intPtSize));
        }
        this.disposer = new FontStrikeDisposer(fileFont, desc, this.pScalerContext);
        double maxSz = 48.0;
        boolean bl2 = this.getImageWithAdvance = Math.abs(at.getScaleX()) <= maxSz && Math.abs(at.getScaleY()) <= maxSz && Math.abs(at.getShearX()) <= maxSz && Math.abs(at.getShearY()) <= maxSz;
        if (!this.getImageWithAdvance) {
            if (!this.segmentedCache) {
                this.horizontalAdvances = new float[numGlyphs];
                for (int i = 0; i < numGlyphs; ++i) {
                    this.horizontalAdvances[i] = Float.MAX_VALUE;
                }
            } else {
                int numSegments = (numGlyphs + 32 - 1) / 32;
                this.segHorizontalAdvances = new float[numSegments][];
            }
        }
    }

    @Override
    public int getNumGlyphs() {
        return this.fileFont.getNumGlyphs();
    }

    long getGlyphImageFromNative(int glyphCode) {
        if (FontUtilities.isWindows) {
            return this.getGlyphImageFromWindows(glyphCode);
        }
        return this.getGlyphImageFromX11(glyphCode);
    }

    private native long _getGlyphImageFromWindows(String var1, int var2, int var3, int var4, boolean var5, int var6);

    long getGlyphImageFromWindows(int glyphCode) {
        int size;
        int style;
        String family = this.fileFont.getFamilyName(null);
        long ptr = this._getGlyphImageFromWindows(family, style = this.desc.style & 1 | this.desc.style & 2 | this.fileFont.getStyle(), size = this.intPtSize, glyphCode, this.desc.fmHint == 2, ((TrueTypeFont)this.fileFont).fontDataSize);
        if (ptr != 0L) {
            float advance = this.getGlyphAdvance(glyphCode, false);
            StrikeCache.unsafe.putFloat(ptr + (long)StrikeCache.xAdvanceOffset, advance);
            return ptr;
        }
        if (FontUtilities.isLogging()) {
            FontUtilities.logWarning("Failed to render glyph using GDI: code=" + glyphCode + ", fontFamily=" + family + ", style=" + style + ", size=" + size);
        }
        return this.fileFont.getGlyphImage(this.pScalerContext, glyphCode);
    }

    long getGlyphImageFromX11(int glyphCode) {
        char charCode = this.fileFont.glyphToCharMap[glyphCode];
        for (int i = 0; i < this.nativeStrikes.length; ++i) {
            long glyphPtr;
            CharToGlyphMapper mapper = this.fileFont.nativeFonts[i].getMapper();
            int gc = mapper.charToGlyph(charCode) & 0xFFFF;
            if (gc == mapper.getMissingGlyphCode() || (glyphPtr = this.nativeStrikes[i].getGlyphImagePtrNoCache(gc)) == 0L) continue;
            return glyphPtr;
        }
        return this.fileFont.getGlyphImage(this.pScalerContext, glyphCode);
    }

    @Override
    long getGlyphImagePtr(int glyphCode) {
        if (glyphCode >= 65534) {
            return StrikeCache.invisibleGlyphPtr;
        }
        long glyphPtr = 0L;
        glyphPtr = this.getCachedGlyphPtr(glyphCode);
        if (glyphPtr != 0L) {
            return glyphPtr;
        }
        if (this.useNatives && (glyphPtr = this.getGlyphImageFromNative(glyphCode)) == 0L && FontUtilities.isLogging()) {
            FontUtilities.logInfo("Strike for " + String.valueOf(this.fileFont) + " at size = " + this.intPtSize + " couldn't get native glyph for code = " + glyphCode);
        }
        if (glyphPtr == 0L) {
            glyphPtr = this.fileFont.getGlyphImage(this.pScalerContext, glyphCode);
        }
        return this.setCachedGlyphPtr(glyphCode, glyphPtr);
    }

    @Override
    void getGlyphImagePtrs(int[] glyphCodes, long[] images, int len) {
        for (int i = 0; i < len; ++i) {
            int glyphCode = glyphCodes[i];
            if (glyphCode >= 65534) {
                images[i] = StrikeCache.invisibleGlyphPtr;
                continue;
            }
            images[i] = this.getCachedGlyphPtr(glyphCode);
            if (images[i] != 0L) continue;
            long glyphPtr = 0L;
            if (this.useNatives) {
                glyphPtr = this.getGlyphImageFromNative(glyphCode);
            }
            if (glyphPtr == 0L) {
                glyphPtr = this.fileFont.getGlyphImage(this.pScalerContext, glyphCode);
            }
            images[i] = this.setCachedGlyphPtr(glyphCode, glyphPtr);
        }
    }

    @Override
    int getSlot0GlyphImagePtrs(int[] glyphCodes, long[] images, int len) {
        int convertedCnt = 0;
        for (int i = 0; i < len; ++i) {
            int glyphCode = glyphCodes[i];
            if (glyphCode >>> 24 != 0) {
                return convertedCnt;
            }
            ++convertedCnt;
            if (glyphCode >= 65534) {
                images[i] = StrikeCache.invisibleGlyphPtr;
                continue;
            }
            images[i] = this.getCachedGlyphPtr(glyphCode);
            if (images[i] != 0L) continue;
            long glyphPtr = 0L;
            if (this.useNatives) {
                glyphPtr = this.getGlyphImageFromNative(glyphCode);
            }
            if (glyphPtr == 0L) {
                glyphPtr = this.fileFont.getGlyphImage(this.pScalerContext, glyphCode);
            }
            images[i] = this.setCachedGlyphPtr(glyphCode, glyphPtr);
        }
        return convertedCnt;
    }

    long getCachedGlyphPtr(int glyphCode) {
        try {
            return this.getCachedGlyphPtrInternal(glyphCode);
        }
        catch (Exception e) {
            NullFontScaler nullScaler = (NullFontScaler)FontScaler.getNullScaler();
            long nullSC = NullFontScaler.getNullScalerContext();
            return nullScaler.getGlyphImage(nullSC, glyphCode);
        }
    }

    private long getCachedGlyphPtrInternal(int glyphCode) {
        switch (this.glyphCacheFormat) {
            case 1: {
                return (long)this.intGlyphImages[glyphCode] & 0xFFFFFFFFL;
            }
            case 3: {
                int segIndex = glyphCode >> 5;
                if (this.segIntGlyphImages[segIndex] != null) {
                    int subIndex = glyphCode % 32;
                    return (long)this.segIntGlyphImages[segIndex][subIndex] & 0xFFFFFFFFL;
                }
                return 0L;
            }
            case 2: {
                return this.longGlyphImages[glyphCode];
            }
            case 4: {
                int segIndex = glyphCode >> 5;
                if (this.segLongGlyphImages[segIndex] != null) {
                    int subIndex = glyphCode % 32;
                    return this.segLongGlyphImages[segIndex][subIndex];
                }
                return 0L;
            }
        }
        return 0L;
    }

    private synchronized long setCachedGlyphPtr(int glyphCode, long glyphPtr) {
        try {
            return this.setCachedGlyphPtrInternal(glyphCode, glyphPtr);
        }
        catch (Exception e) {
            switch (this.glyphCacheFormat) {
                case 1: 
                case 3: {
                    StrikeCache.freeIntPointer((int)glyphPtr);
                    break;
                }
                case 2: 
                case 4: {
                    StrikeCache.freeLongPointer(glyphPtr);
                }
            }
            NullFontScaler nullScaler = (NullFontScaler)FontScaler.getNullScaler();
            long nullSC = NullFontScaler.getNullScalerContext();
            return nullScaler.getGlyphImage(nullSC, glyphCode);
        }
    }

    private long setCachedGlyphPtrInternal(int glyphCode, long glyphPtr) {
        switch (this.glyphCacheFormat) {
            case 1: {
                if (this.intGlyphImages[glyphCode] == 0) {
                    this.intGlyphImages[glyphCode] = (int)glyphPtr;
                    return glyphPtr;
                }
                StrikeCache.freeIntPointer((int)glyphPtr);
                return (long)this.intGlyphImages[glyphCode] & 0xFFFFFFFFL;
            }
            case 3: {
                int segIndex = glyphCode >> 5;
                int subIndex = glyphCode % 32;
                if (this.segIntGlyphImages[segIndex] == null) {
                    this.segIntGlyphImages[segIndex] = new int[32];
                }
                if (this.segIntGlyphImages[segIndex][subIndex] == 0) {
                    this.segIntGlyphImages[segIndex][subIndex] = (int)glyphPtr;
                    return glyphPtr;
                }
                StrikeCache.freeIntPointer((int)glyphPtr);
                return (long)this.segIntGlyphImages[segIndex][subIndex] & 0xFFFFFFFFL;
            }
            case 2: {
                if (this.longGlyphImages[glyphCode] == 0L) {
                    this.longGlyphImages[glyphCode] = glyphPtr;
                    return glyphPtr;
                }
                StrikeCache.freeLongPointer(glyphPtr);
                return this.longGlyphImages[glyphCode];
            }
            case 4: {
                int segIndex = glyphCode >> 5;
                int subIndex = glyphCode % 32;
                if (this.segLongGlyphImages[segIndex] == null) {
                    this.segLongGlyphImages[segIndex] = new long[32];
                }
                if (this.segLongGlyphImages[segIndex][subIndex] == 0L) {
                    this.segLongGlyphImages[segIndex][subIndex] = glyphPtr;
                    return glyphPtr;
                }
                StrikeCache.freeLongPointer(glyphPtr);
                return this.segLongGlyphImages[segIndex][subIndex];
            }
        }
        this.initGlyphCache();
        return this.setCachedGlyphPtr(glyphCode, glyphPtr);
    }

    private synchronized void initGlyphCache() {
        int numGlyphs = this.mapper.getNumGlyphs();
        int tmpFormat = 0;
        if (this.segmentedCache) {
            int numSegments = (numGlyphs + 32 - 1) / 32;
            if (longAddresses) {
                tmpFormat = 4;
                this.segLongGlyphImages = new long[numSegments][];
                this.disposer.segLongGlyphImages = this.segLongGlyphImages;
            } else {
                tmpFormat = 3;
                this.segIntGlyphImages = new int[numSegments][];
                this.disposer.segIntGlyphImages = this.segIntGlyphImages;
            }
        } else if (longAddresses) {
            tmpFormat = 2;
            this.longGlyphImages = new long[numGlyphs];
            this.disposer.longGlyphImages = this.longGlyphImages;
        } else {
            tmpFormat = 1;
            this.intGlyphImages = new int[numGlyphs];
            this.disposer.intGlyphImages = this.intGlyphImages;
        }
        this.glyphCacheFormat = tmpFormat;
    }

    @Override
    float getGlyphAdvance(int glyphCode) {
        return this.getGlyphAdvance(glyphCode, true);
    }

    private float getGlyphAdvance(int glyphCode, boolean getUserAdv) {
        int segIndex;
        float[] subArray;
        float advance;
        if (glyphCode >= 65534) {
            return 0.0f;
        }
        if (this.horizontalAdvances != null) {
            advance = this.horizontalAdvances[glyphCode];
            if (advance != Float.MAX_VALUE) {
                if (!getUserAdv && this.invertDevTx != null) {
                    Point2D.Float metrics = new Point2D.Float(advance, 0.0f);
                    this.desc.devTx.deltaTransform(metrics, metrics);
                    return metrics.x;
                }
                return advance;
            }
        } else if (this.segmentedCache && this.segHorizontalAdvances != null && (subArray = this.segHorizontalAdvances[segIndex = glyphCode >> 5]) != null && (advance = subArray[glyphCode % 32]) != Float.MAX_VALUE) {
            if (!getUserAdv && this.invertDevTx != null) {
                Point2D.Float metrics = new Point2D.Float(advance, 0.0f);
                this.desc.devTx.deltaTransform(metrics, metrics);
                return metrics.x;
            }
            return advance;
        }
        if (!getUserAdv && this.invertDevTx != null) {
            Point2D.Float metrics = new Point2D.Float();
            this.fileFont.getGlyphMetrics(this.pScalerContext, glyphCode, metrics);
            return metrics.x;
        }
        if (this.invertDevTx != null || !getUserAdv) {
            advance = this.getGlyphMetrics((int)glyphCode, (boolean)getUserAdv).x;
        } else {
            long glyphPtr = this.getImageWithAdvance ? this.getGlyphImagePtr(glyphCode) : this.getCachedGlyphPtr(glyphCode);
            advance = glyphPtr != 0L ? StrikeCache.unsafe.getFloat(glyphPtr + (long)StrikeCache.xAdvanceOffset) : this.fileFont.getGlyphAdvance(this.pScalerContext, glyphCode);
        }
        if (this.horizontalAdvances != null) {
            this.horizontalAdvances[glyphCode] = advance;
        } else if (this.segmentedCache && this.segHorizontalAdvances != null) {
            int segIndex2 = glyphCode >> 5;
            int subIndex = glyphCode % 32;
            if (this.segHorizontalAdvances[segIndex2] == null) {
                this.segHorizontalAdvances[segIndex2] = new float[32];
                for (int i = 0; i < 32; ++i) {
                    this.segHorizontalAdvances[segIndex2][i] = Float.MAX_VALUE;
                }
            }
            this.segHorizontalAdvances[segIndex2][subIndex] = advance;
        }
        return advance;
    }

    @Override
    float getCodePointAdvance(int cp) {
        return this.getGlyphAdvance(this.mapper.charToGlyph(cp));
    }

    @Override
    void getGlyphImageBounds(int glyphCode, Point2D.Float pt, Rectangle result) {
        int minx;
        if (this.intPtSize > 100) {
            Rectangle2D.Float obds = this.getGlyphOutlineBounds(glyphCode);
            if (obds.isEmpty()) {
                Rectangle bds = this.getGlyphOutline(glyphCode, pt.x, pt.y).getBounds();
                result.setBounds(bds);
            } else {
                result.x = (int)Math.floor((double)pt.x + obds.getX() + 0.5);
                result.y = (int)Math.floor((double)pt.y + obds.getY() + 0.5);
                result.width = (int)Math.floor(obds.getWidth() + 0.5);
                result.height = (int)Math.floor(obds.getHeight() + 0.5);
            }
            return;
        }
        long ptr = this.getGlyphImagePtr(glyphCode);
        if (ptr == 0L) {
            result.x = (int)Math.floor(pt.x + 0.5f);
            result.y = (int)Math.floor(pt.y + 0.5f);
            result.height = 0;
            result.width = 0;
            return;
        }
        float topLeftX = StrikeCache.unsafe.getFloat(ptr + (long)StrikeCache.topLeftXOffset);
        float topLeftY = StrikeCache.unsafe.getFloat(ptr + (long)StrikeCache.topLeftYOffset);
        result.x = (int)Math.floor(pt.x + topLeftX + 0.5f);
        result.y = (int)Math.floor(pt.y + topLeftY + 0.5f);
        result.width = StrikeCache.unsafe.getShort(ptr + (long)StrikeCache.widthOffset) & 0xFFFF;
        result.height = StrikeCache.unsafe.getShort(ptr + (long)StrikeCache.heightOffset) & 0xFFFF;
        if ((this.desc.aaHint == 4 || this.desc.aaHint == 5) && topLeftX <= -2.0f && (minx = this.getGlyphImageMinX(ptr, result.x)) > result.x) {
            ++result.x;
            --result.width;
        }
    }

    private int getGlyphImageMinX(long ptr, int origMinX) {
        char width = StrikeCache.unsafe.getChar(ptr + (long)StrikeCache.widthOffset);
        int height = StrikeCache.unsafe.getChar(ptr + (long)StrikeCache.heightOffset);
        char rowBytes = StrikeCache.unsafe.getChar(ptr + (long)StrikeCache.rowBytesOffset);
        if (rowBytes == width) {
            return origMinX;
        }
        long pixelData = StrikeCache.unsafe.getAddress(ptr + (long)StrikeCache.pixelDataOffset);
        if (pixelData == 0L) {
            return origMinX;
        }
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < 3; ++x) {
                if (StrikeCache.unsafe.getByte(pixelData + (long)(y * rowBytes) + (long)x) == 0) continue;
                return origMinX;
            }
        }
        return origMinX + 1;
    }

    @Override
    StrikeMetrics getFontMetrics() {
        if (this.strikeMetrics == null) {
            this.strikeMetrics = this.fileFont.getFontMetrics(this.pScalerContext);
            if (this.invertDevTx != null) {
                this.strikeMetrics.convertToUserSpace(this.invertDevTx);
            }
        }
        return this.strikeMetrics;
    }

    @Override
    Point2D.Float getGlyphMetrics(int glyphCode) {
        return this.getGlyphMetrics(glyphCode, true);
    }

    private Point2D.Float getGlyphMetrics(int glyphCode, boolean getImage) {
        Point2D.Float metrics = new Point2D.Float();
        if (glyphCode >= 65534) {
            return metrics;
        }
        long glyphPtr = this.getImageWithAdvance && getImage ? this.getGlyphImagePtr(glyphCode) : this.getCachedGlyphPtr(glyphCode);
        if (glyphPtr != 0L) {
            metrics = new Point2D.Float();
            metrics.x = StrikeCache.unsafe.getFloat(glyphPtr + (long)StrikeCache.xAdvanceOffset);
            metrics.y = StrikeCache.unsafe.getFloat(glyphPtr + (long)StrikeCache.yAdvanceOffset);
            if (this.invertDevTx != null) {
                this.invertDevTx.deltaTransform(metrics, metrics);
            }
        } else {
            Integer key = glyphCode;
            Point2D.Float value = null;
            ConcurrentHashMap<Integer, Point2D.Float> glyphMetricsMap = null;
            if (this.glyphMetricsMapRef != null) {
                glyphMetricsMap = this.glyphMetricsMapRef.get();
            }
            if (glyphMetricsMap != null && (value = glyphMetricsMap.get(key)) != null) {
                metrics.x = value.x;
                metrics.y = value.y;
                return metrics;
            }
            if (value == null) {
                this.fileFont.getGlyphMetrics(this.pScalerContext, glyphCode, metrics);
                if (this.invertDevTx != null) {
                    this.invertDevTx.deltaTransform(metrics, metrics);
                }
                value = new Point2D.Float(metrics.x, metrics.y);
                if (glyphMetricsMap == null) {
                    glyphMetricsMap = new ConcurrentHashMap();
                    this.glyphMetricsMapRef = new SoftReference<ConcurrentHashMap<Integer, Point2D.Float>>(glyphMetricsMap);
                }
                glyphMetricsMap.put(key, value);
            }
        }
        return metrics;
    }

    @Override
    Point2D.Float getCharMetrics(char ch) {
        return this.getGlyphMetrics(this.mapper.charToGlyph(ch));
    }

    @Override
    Rectangle2D.Float getGlyphOutlineBounds(int glyphCode) {
        Integer key;
        Rectangle2D.Float bounds;
        if (this.boundsMap == null) {
            this.boundsMap = new ConcurrentHashMap();
        }
        if ((bounds = this.boundsMap.get(key = Integer.valueOf(glyphCode))) == null) {
            bounds = this.fileFont.getGlyphOutlineBounds(this.pScalerContext, glyphCode);
            this.boundsMap.put(key, bounds);
        }
        return bounds;
    }

    public Rectangle2D getOutlineBounds(int glyphCode) {
        return this.fileFont.getGlyphOutlineBounds(this.pScalerContext, glyphCode);
    }

    @Override
    GeneralPath getGlyphOutline(int glyphCode, float x, float y) {
        Path2D.Float gp = null;
        ConcurrentHashMap<Integer, Path2D.Float> outlineMap = null;
        if (this.outlineMapRef != null && (outlineMap = (ConcurrentHashMap<Integer, Path2D.Float>)this.outlineMapRef.get()) != null) {
            gp = (GeneralPath)outlineMap.get(glyphCode);
        }
        if (gp == null) {
            gp = this.fileFont.getGlyphOutline(this.pScalerContext, glyphCode, 0.0f, 0.0f);
            if (outlineMap == null) {
                outlineMap = new ConcurrentHashMap<Integer, Path2D.Float>();
                this.outlineMapRef = new WeakReference(outlineMap);
            }
            outlineMap.put(glyphCode, gp);
        }
        gp = (GeneralPath)gp.clone();
        if (x != 0.0f || y != 0.0f) {
            gp.transform(AffineTransform.getTranslateInstance(x, y));
        }
        return gp;
    }

    @Override
    GeneralPath getGlyphVectorOutline(int[] glyphs, float x, float y) {
        return this.fileFont.getGlyphVectorOutline(this.pScalerContext, glyphs, glyphs.length, x, y);
    }

    @Override
    protected void adjustPoint(Point2D.Float pt) {
        if (this.invertDevTx != null) {
            this.invertDevTx.deltaTransform(pt, pt);
        }
    }

    static {
        if (FontUtilities.isWindows && !FontUtilities.useJDKScaler && !GraphicsEnvironment.isHeadless()) {
            isXPorLater = FileFontStrike.initNative();
        }
    }
}

