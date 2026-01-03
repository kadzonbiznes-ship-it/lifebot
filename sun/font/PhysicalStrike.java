/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.geom.Point2D;
import java.util.concurrent.ConcurrentHashMap;
import sun.font.CharToGlyphMapper;
import sun.font.FontStrike;
import sun.font.FontStrikeDesc;
import sun.font.PhysicalFont;
import sun.font.StrikeCache;
import sun.font.StrikeMetrics;

public abstract class PhysicalStrike
extends FontStrike {
    static final long INTMASK = 0xFFFFFFFFL;
    static boolean longAddresses;
    private PhysicalFont physicalFont;
    protected CharToGlyphMapper mapper;
    protected long pScalerContext;
    protected long[] longGlyphImages;
    protected int[] intGlyphImages;
    ConcurrentHashMap<Integer, Point2D.Float> glyphPointMapCache;
    protected boolean getImageWithAdvance;
    protected static final int complexTX = 124;

    PhysicalStrike(PhysicalFont physicalFont, FontStrikeDesc desc) {
        this.physicalFont = physicalFont;
        this.desc = desc;
    }

    protected PhysicalStrike() {
    }

    @Override
    public int getNumGlyphs() {
        return this.physicalFont.getNumGlyphs();
    }

    @Override
    StrikeMetrics getFontMetrics() {
        if (this.strikeMetrics == null) {
            this.strikeMetrics = this.physicalFont.getFontMetrics(this.pScalerContext);
        }
        return this.strikeMetrics;
    }

    @Override
    float getCodePointAdvance(int cp) {
        return this.getGlyphAdvance(this.physicalFont.getMapper().charToGlyph(cp));
    }

    @Override
    Point2D.Float getCharMetrics(char ch) {
        return this.getGlyphMetrics(this.physicalFont.getMapper().charToGlyph(ch));
    }

    int getSlot0GlyphImagePtrs(int[] glyphCodes, long[] images, int len) {
        return 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    Point2D.Float getGlyphPoint(int glyphCode, int ptNumber) {
        Point2D.Float gp = null;
        Integer ptKey = glyphCode << 16 | ptNumber;
        if (this.glyphPointMapCache == null) {
            PhysicalStrike physicalStrike = this;
            synchronized (physicalStrike) {
                if (this.glyphPointMapCache == null) {
                    this.glyphPointMapCache = new ConcurrentHashMap();
                }
            }
        } else {
            gp = this.glyphPointMapCache.get(ptKey);
        }
        if (gp == null) {
            gp = this.physicalFont.getGlyphPoint(this.pScalerContext, glyphCode, ptNumber);
            this.adjustPoint(gp);
            this.glyphPointMapCache.put(ptKey, gp);
        }
        return gp;
    }

    protected void adjustPoint(Point2D.Float pt) {
    }

    static {
        switch (StrikeCache.nativeAddressSize) {
            case 8: {
                longAddresses = true;
                break;
            }
            case 4: {
                longAddresses = false;
                break;
            }
            default: {
                throw new RuntimeException("Unexpected address size");
            }
        }
    }
}

