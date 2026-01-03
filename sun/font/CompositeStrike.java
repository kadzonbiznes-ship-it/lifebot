/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import sun.font.CompositeFont;
import sun.font.FontStrike;
import sun.font.FontStrikeDesc;
import sun.font.FontStrikeDisposer;
import sun.font.PhysicalStrike;
import sun.font.StrikeMetrics;

public final class CompositeStrike
extends FontStrike {
    static final int SLOTMASK = 0xFFFFFF;
    private CompositeFont compFont;
    private PhysicalStrike[] strikes;
    int numGlyphs = 0;

    CompositeStrike(CompositeFont font2D, FontStrikeDesc desc) {
        this.compFont = font2D;
        this.desc = desc;
        this.disposer = new FontStrikeDisposer(this.compFont, desc);
        if (desc.style != this.compFont.style) {
            this.algoStyle = true;
            if ((desc.style & 1) == 1 && (this.compFont.style & 1) == 0) {
                this.boldness = 1.33f;
            }
            if ((desc.style & 2) == 2 && (this.compFont.style & 2) == 0) {
                this.italic = 0.7f;
            }
        }
        this.strikes = new PhysicalStrike[this.compFont.numSlots];
    }

    PhysicalStrike getStrikeForGlyph(int glyphCode) {
        return this.getStrikeForSlot(glyphCode >>> 24);
    }

    PhysicalStrike getStrikeForSlot(int slot) {
        PhysicalStrike strike;
        if (slot >= this.strikes.length) {
            slot = 0;
        }
        if ((strike = this.strikes[slot]) == null) {
            this.strikes[slot] = strike = (PhysicalStrike)this.compFont.getSlotFont(slot).getStrike(this.desc);
        }
        return strike;
    }

    @Override
    public int getNumGlyphs() {
        return this.compFont.getNumGlyphs();
    }

    @Override
    StrikeMetrics getFontMetrics() {
        if (this.strikeMetrics == null) {
            StrikeMetrics compMetrics = new StrikeMetrics();
            for (int s = 0; s < this.compFont.numMetricsSlots; ++s) {
                compMetrics.merge(this.getStrikeForSlot(s).getFontMetrics());
            }
            this.strikeMetrics = compMetrics;
        }
        return this.strikeMetrics;
    }

    @Override
    void getGlyphImagePtrs(int[] glyphCodes, long[] images, int len) {
        PhysicalStrike strike = this.getStrikeForSlot(0);
        int numptrs = strike.getSlot0GlyphImagePtrs(glyphCodes, images, len);
        if (numptrs == len) {
            return;
        }
        for (int i = numptrs; i < len; ++i) {
            strike = this.getStrikeForGlyph(glyphCodes[i]);
            images[i] = strike.getGlyphImagePtr(glyphCodes[i] & 0xFFFFFF);
        }
    }

    @Override
    long getGlyphImagePtr(int glyphCode) {
        PhysicalStrike strike = this.getStrikeForGlyph(glyphCode);
        return strike.getGlyphImagePtr(glyphCode & 0xFFFFFF);
    }

    @Override
    void getGlyphImageBounds(int glyphCode, Point2D.Float pt, Rectangle result) {
        PhysicalStrike strike = this.getStrikeForGlyph(glyphCode);
        strike.getGlyphImageBounds(glyphCode & 0xFFFFFF, pt, result);
    }

    @Override
    Point2D.Float getGlyphMetrics(int glyphCode) {
        PhysicalStrike strike = this.getStrikeForGlyph(glyphCode);
        return strike.getGlyphMetrics(glyphCode & 0xFFFFFF);
    }

    @Override
    Point2D.Float getCharMetrics(char ch) {
        return this.getGlyphMetrics(this.compFont.getMapper().charToGlyph(ch));
    }

    @Override
    float getGlyphAdvance(int glyphCode) {
        PhysicalStrike strike = this.getStrikeForGlyph(glyphCode);
        return strike.getGlyphAdvance(glyphCode & 0xFFFFFF);
    }

    @Override
    float getCodePointAdvance(int cp) {
        return this.getGlyphAdvance(this.compFont.getMapper().charToGlyph(cp));
    }

    @Override
    Rectangle2D.Float getGlyphOutlineBounds(int glyphCode) {
        PhysicalStrike strike = this.getStrikeForGlyph(glyphCode);
        return strike.getGlyphOutlineBounds(glyphCode & 0xFFFFFF);
    }

    @Override
    GeneralPath getGlyphOutline(int glyphCode, float x, float y) {
        PhysicalStrike strike = this.getStrikeForGlyph(glyphCode);
        GeneralPath path = strike.getGlyphOutline(glyphCode & 0xFFFFFF, x, y);
        if (path == null) {
            return new GeneralPath();
        }
        return path;
    }

    @Override
    GeneralPath getGlyphVectorOutline(int[] glyphs, float x, float y) {
        GeneralPath path = null;
        int glyphIndex = 0;
        while (glyphIndex < glyphs.length) {
            int start = glyphIndex;
            int slot = glyphs[glyphIndex] >>> 24;
            while (glyphIndex < glyphs.length && glyphs[glyphIndex + 1] >>> 24 == slot) {
                ++glyphIndex;
            }
            int tmpLen = glyphIndex - start + 1;
            int[] tmpGlyphs = new int[tmpLen];
            for (int i = 0; i < tmpLen; ++i) {
                tmpGlyphs[i] = glyphs[i] & 0xFFFFFF;
            }
            GeneralPath gp = this.getStrikeForSlot(slot).getGlyphVectorOutline(tmpGlyphs, x, y);
            if (path == null) {
                path = gp;
                continue;
            }
            if (gp == null) continue;
            path.append(gp, false);
        }
        if (path == null) {
            return new GeneralPath();
        }
        return path;
    }
}

