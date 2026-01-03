/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import sun.font.CharToGlyphMapper;
import sun.font.Font2DHandle;
import sun.font.FontFamily;
import sun.font.FontStrike;
import sun.font.FontStrikeDesc;
import sun.font.StrikeCache;
import sun.font.StrikeMetrics;

public abstract class Font2D {
    public static final int FONT_CONFIG_RANK = 2;
    public static final int JRE_RANK = 2;
    public static final int TTF_RANK = 3;
    public static final int TYPE1_RANK = 4;
    public static final int NATIVE_RANK = 5;
    public static final int UNKNOWN_RANK = 6;
    public static final int DEFAULT_RANK = 4;
    private static final String[] boldNames = new String[]{"bold", "demibold", "demi-bold", "demi bold", "negreta", "demi"};
    private static final String[] italicNames = new String[]{"italic", "cursiva", "oblique", "inclined"};
    private static final String[] boldItalicNames = new String[]{"bolditalic", "bold-italic", "bold italic", "boldoblique", "bold-oblique", "bold oblique", "demibold italic", "negreta cursiva", "demi oblique"};
    private static final FontRenderContext DEFAULT_FRC = new FontRenderContext(null, false, false);
    public Font2DHandle handle;
    protected String familyName;
    protected String fullName;
    protected int style = 0;
    protected FontFamily family;
    protected int fontRank = 4;
    protected CharToGlyphMapper mapper;
    protected ConcurrentHashMap<FontStrikeDesc, Reference<FontStrike>> strikeCache = new ConcurrentHashMap();
    protected Reference<FontStrike> lastFontStrike = new WeakReference<Object>(null);
    private int strikeCacheMax = 0;
    private boolean useWeak;
    public static final int FWIDTH_NORMAL = 5;
    public static final int FWEIGHT_NORMAL = 400;
    public static final int FWEIGHT_BOLD = 700;

    void setUseWeakRefs(boolean weak, int maxStrikes) {
        this.useWeak = weak;
        this.strikeCacheMax = weak && maxStrikes > 0 ? maxStrikes : 0;
    }

    public int getStyle() {
        return this.style;
    }

    protected void setStyle() {
        int i;
        String fName = this.fullName.toLowerCase();
        for (i = 0; i < boldItalicNames.length; ++i) {
            if (!fName.contains(boldItalicNames[i])) continue;
            this.style = 3;
            return;
        }
        for (i = 0; i < italicNames.length; ++i) {
            if (!fName.contains(italicNames[i])) continue;
            this.style = 2;
            return;
        }
        for (i = 0; i < boldNames.length; ++i) {
            if (!fName.contains(boldNames[i])) continue;
            this.style = 1;
            return;
        }
    }

    public int getWidth() {
        return 5;
    }

    public int getWeight() {
        if ((this.style & 1) != 0) {
            return 700;
        }
        return 400;
    }

    int getRank() {
        return this.fontRank;
    }

    void setRank(int rank) {
        this.fontRank = rank;
    }

    abstract CharToGlyphMapper getMapper();

    protected int getValidatedGlyphCode(int glyphCode) {
        if (glyphCode < 0 || glyphCode >= this.getMapper().getNumGlyphs()) {
            glyphCode = this.getMapper().getMissingGlyphCode();
        }
        return glyphCode;
    }

    abstract FontStrike createStrike(FontStrikeDesc var1);

    public FontStrike getStrike(Font font) {
        FontStrike strike = this.lastFontStrike.get();
        if (strike != null) {
            return strike;
        }
        return this.getStrike(font, DEFAULT_FRC);
    }

    public FontStrike getStrike(Font font, AffineTransform devTx, int aa, int fm) {
        double ptSize = font.getSize2D();
        AffineTransform glyphTx = (AffineTransform)devTx.clone();
        glyphTx.scale(ptSize, ptSize);
        if (font.isTransformed()) {
            glyphTx.concatenate(font.getTransform());
        }
        if (glyphTx.getTranslateX() != 0.0 || glyphTx.getTranslateY() != 0.0) {
            glyphTx.setTransform(glyphTx.getScaleX(), glyphTx.getShearY(), glyphTx.getShearX(), glyphTx.getScaleY(), 0.0, 0.0);
        }
        FontStrikeDesc desc = new FontStrikeDesc(devTx, glyphTx, font.getStyle(), aa, fm);
        return this.getStrike(desc, false);
    }

    public FontStrike getStrike(Font font, AffineTransform devTx, AffineTransform glyphTx, int aa, int fm) {
        FontStrikeDesc desc = new FontStrikeDesc(devTx, glyphTx, font.getStyle(), aa, fm);
        return this.getStrike(desc, false);
    }

    public FontStrike getStrike(Font font, FontRenderContext frc) {
        AffineTransform at = frc.getTransform();
        double ptSize = font.getSize2D();
        at.scale(ptSize, ptSize);
        if (font.isTransformed()) {
            at.concatenate(font.getTransform());
            if (at.getTranslateX() != 0.0 || at.getTranslateY() != 0.0) {
                at.setTransform(at.getScaleX(), at.getShearY(), at.getShearX(), at.getScaleY(), 0.0, 0.0);
            }
        }
        int aa = FontStrikeDesc.getAAHintIntVal(this, font, frc);
        int fm = FontStrikeDesc.getFMHintIntVal(frc.getFractionalMetricsHint());
        FontStrikeDesc desc = new FontStrikeDesc(frc.getTransform(), at, font.getStyle(), aa, fm);
        return this.getStrike(desc, false);
    }

    void updateLastStrikeRef(FontStrike strike) {
        this.lastFontStrike.clear();
        this.lastFontStrike = this.useWeak ? new WeakReference<FontStrike>(strike) : new SoftReference<FontStrike>(strike);
    }

    FontStrike getStrike(FontStrikeDesc desc) {
        return this.getStrike(desc, true);
    }

    private FontStrike getStrike(FontStrikeDesc desc, boolean copy) {
        FontStrike strike = this.lastFontStrike.get();
        if (strike != null && desc.equals(strike.desc)) {
            return strike;
        }
        Reference<FontStrike> strikeRef = this.strikeCache.get(desc);
        if (strikeRef != null && (strike = strikeRef.get()) != null) {
            this.updateLastStrikeRef(strike);
            StrikeCache.refStrike(strike);
            return strike;
        }
        if (copy) {
            desc = new FontStrikeDesc(desc);
        }
        strike = this.createStrike(desc);
        int txType = desc.glyphTx.getType();
        strikeRef = this.useWeak || txType == 32 || (txType & 0x10) != 0 && this.strikeCache.size() > 10 ? StrikeCache.getStrikeRef(strike, true) : StrikeCache.getStrikeRef(strike, this.useWeak);
        this.strikeCache.put(desc, strikeRef);
        this.updateLastStrikeRef(strike);
        StrikeCache.refStrike(strike);
        return strike;
    }

    public void getFontMetrics(Font font, AffineTransform at, Object aaHint, Object fmHint, float[] metrics) {
        int aa = FontStrikeDesc.getAAHintIntVal(aaHint, this, font.getSize());
        int fm = FontStrikeDesc.getFMHintIntVal(fmHint);
        FontStrike strike = this.getStrike(font, at, aa, fm);
        StrikeMetrics strikeMetrics = strike.getFontMetrics();
        metrics[0] = strikeMetrics.getAscent();
        metrics[1] = strikeMetrics.getDescent();
        metrics[2] = strikeMetrics.getLeading();
        metrics[3] = strikeMetrics.getMaxAdvance();
        this.getStyleMetrics(font.getSize2D(), metrics, 4);
    }

    public void getStyleMetrics(float pointSize, float[] metrics, int offset) {
        metrics[offset] = -metrics[0] / 2.5f;
        metrics[offset + 1] = pointSize / 12.0f;
        metrics[offset + 2] = metrics[offset + 1] / 1.5f;
        metrics[offset + 3] = metrics[offset + 1];
    }

    public void getFontMetrics(Font font, FontRenderContext frc, float[] metrics) {
        StrikeMetrics strikeMetrics = this.getStrike(font, frc).getFontMetrics();
        metrics[0] = strikeMetrics.getAscent();
        metrics[1] = strikeMetrics.getDescent();
        metrics[2] = strikeMetrics.getLeading();
        metrics[3] = strikeMetrics.getMaxAdvance();
    }

    protected byte[] getTableBytes(int tag) {
        return null;
    }

    protected long getPlatformNativeFontPtr() {
        return 0L;
    }

    protected long getUnitsPerEm() {
        return 2048L;
    }

    boolean supportsEncoding(String encoding) {
        return false;
    }

    public boolean canDoStyle(int style) {
        return style == this.style;
    }

    public boolean useAAForPtSize(int ptsize) {
        return true;
    }

    public boolean hasSupplementaryChars() {
        return false;
    }

    public String getPostscriptName() {
        return this.fullName;
    }

    public String getFontName(Locale l) {
        return this.fullName;
    }

    public String getFamilyName(Locale l) {
        return this.familyName;
    }

    public int getNumGlyphs() {
        return this.getMapper().getNumGlyphs();
    }

    public int charToGlyph(int wchar) {
        return this.getMapper().charToGlyph(wchar);
    }

    public int charToVariationGlyph(int wchar, int variationSelector) {
        return this.getMapper().charToVariationGlyph(wchar, variationSelector);
    }

    public int getMissingGlyphCode() {
        return this.getMapper().getMissingGlyphCode();
    }

    public boolean canDisplay(char c) {
        return this.getMapper().canDisplay(c);
    }

    public boolean canDisplay(int cp) {
        return this.getMapper().canDisplay(cp);
    }

    public byte getBaselineFor(char c) {
        return 0;
    }

    public float getItalicAngle(Font font, AffineTransform at, Object aaHint, Object fmHint) {
        int aa = FontStrikeDesc.getAAHintIntVal(aaHint, this, 12);
        int fm = FontStrikeDesc.getFMHintIntVal(fmHint);
        FontStrike strike = this.getStrike(font, at, aa, fm);
        StrikeMetrics metrics = strike.getFontMetrics();
        if (metrics.ascentY == 0.0f || metrics.ascentX == 0.0f) {
            return 0.0f;
        }
        return metrics.ascentX / -metrics.ascentY;
    }
}

