/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import sun.font.CharToGlyphMapper;
import sun.font.CompositeFont;
import sun.font.FontUtilities;

public class CompositeGlyphMapper
extends CharToGlyphMapper {
    public static final int SLOTMASK = -16777216;
    public static final int GLYPHMASK = 0xFFFFFF;
    public static final int NBLOCKS = 216;
    public static final int BLOCKSZ = 256;
    public static final int MAXUNICODE = 55296;
    CompositeFont font;
    CharToGlyphMapper[] slotMappers;
    int[][] glyphMaps;
    private boolean hasExcludes;

    public CompositeGlyphMapper(CompositeFont compFont) {
        this.font = compFont;
        this.initMapper();
        this.hasExcludes = compFont.exclusionRanges != null && compFont.maxIndices != null;
    }

    public int compositeGlyphCode(int slot, int glyphCode) {
        return slot << 24 | glyphCode & 0xFFFFFF;
    }

    private void initMapper() {
        if (this.missingGlyph == -1) {
            if (this.glyphMaps == null) {
                this.glyphMaps = new int[216][];
            }
            this.slotMappers = new CharToGlyphMapper[this.font.numSlots];
            this.missingGlyph = this.font.getSlotFont(0).getMissingGlyphCode();
            this.missingGlyph = this.compositeGlyphCode(0, this.missingGlyph);
        }
    }

    private int getCachedGlyphCode(int unicode) {
        if (unicode >= 55296) {
            return -1;
        }
        int[] gmap = this.glyphMaps[unicode >> 8];
        if (gmap == null) {
            return -1;
        }
        return gmap[unicode & 0xFF];
    }

    private void setCachedGlyphCode(int unicode, int glyphCode) {
        if (unicode >= 55296) {
            return;
        }
        int index0 = unicode >> 8;
        if (this.glyphMaps[index0] == null) {
            this.glyphMaps[index0] = new int[256];
            for (int i = 0; i < 256; ++i) {
                this.glyphMaps[index0][i] = -1;
            }
        }
        this.glyphMaps[index0][unicode & 0xFF] = glyphCode;
    }

    private CharToGlyphMapper getSlotMapper(int slot) {
        CharToGlyphMapper mapper = this.slotMappers[slot];
        if (mapper == null) {
            this.slotMappers[slot] = mapper = this.font.getSlotFont(slot).getMapper();
        }
        return mapper;
    }

    private int convertToGlyph(int unicode) {
        for (int slot = 0; slot < this.font.numSlots; ++slot) {
            CharToGlyphMapper mapper;
            int glyphCode;
            if (this.hasExcludes && this.font.isExcludedChar(slot, unicode) || (glyphCode = (mapper = this.getSlotMapper(slot)).charToGlyph(unicode)) == mapper.getMissingGlyphCode()) continue;
            glyphCode = this.compositeGlyphCode(slot, glyphCode);
            this.setCachedGlyphCode(unicode, glyphCode);
            return glyphCode;
        }
        return this.missingGlyph;
    }

    @Override
    public int getNumGlyphs() {
        int numGlyphs = 0;
        for (int slot = 0; slot < 1; ++slot) {
            CharToGlyphMapper mapper = this.slotMappers[slot];
            if (mapper == null) {
                this.slotMappers[slot] = mapper = this.font.getSlotFont(slot).getMapper();
            }
            numGlyphs += mapper.getNumGlyphs();
        }
        return numGlyphs;
    }

    @Override
    public int charToGlyph(int unicode) {
        int glyphCode = this.getCachedGlyphCode(unicode);
        if (glyphCode == -1) {
            glyphCode = this.convertToGlyph(unicode);
        }
        return glyphCode;
    }

    public int charToGlyph(int unicode, int prefSlot) {
        CharToGlyphMapper mapper;
        int glyphCode;
        if (prefSlot >= 0 && (glyphCode = (mapper = this.getSlotMapper(prefSlot)).charToGlyph(unicode)) != mapper.getMissingGlyphCode()) {
            return this.compositeGlyphCode(prefSlot, glyphCode);
        }
        return this.charToGlyph(unicode);
    }

    @Override
    public int charToGlyph(char unicode) {
        int glyphCode = this.getCachedGlyphCode(unicode);
        if (glyphCode == -1) {
            glyphCode = this.convertToGlyph(unicode);
        }
        return glyphCode;
    }

    @Override
    public boolean charsToGlyphsNS(int count, char[] unicodes, int[] glyphs) {
        for (int i = 0; i < count; ++i) {
            int gc;
            char low;
            int code = unicodes[i];
            if (code >= 55296 && code <= 56319 && i < count - 1 && (low = unicodes[i + 1]) >= '\udc00' && low <= '\udfff') {
                code = (code - 55296) * 1024 + low - 56320 + 65536;
                glyphs[i + 1] = 65535;
            }
            if ((gc = (glyphs[i] = this.getCachedGlyphCode(code))) == -1) {
                glyphs[i] = this.convertToGlyph(code);
            }
            if (code < 768) continue;
            if (FontUtilities.isComplexCharCode(code) || CharToGlyphMapper.isVariationSelector(code)) {
                return true;
            }
            if (code < 65536) continue;
            ++i;
        }
        return false;
    }

    @Override
    public void charsToGlyphs(int count, char[] unicodes, int[] glyphs) {
        for (int i = 0; i < count; ++i) {
            char low;
            int code = unicodes[i];
            if (code >= 55296 && code <= 56319 && i < count - 1 && (low = unicodes[i + 1]) >= '\udc00' && low <= '\udfff') {
                glyphs[i] = this.getCachedGlyphCode(code = (code - 55296) * 1024 + low - 56320 + 65536);
                int gc = glyphs[i];
                if (gc == -1) {
                    glyphs[i] = this.convertToGlyph(code);
                }
                glyphs[++i] = 65535;
                continue;
            }
            glyphs[i] = this.getCachedGlyphCode(code);
            int gc = glyphs[i];
            if (gc != -1) continue;
            glyphs[i] = this.convertToGlyph(code);
        }
    }

    @Override
    public void charsToGlyphs(int count, int[] unicodes, int[] glyphs) {
        for (int i = 0; i < count; ++i) {
            int code = unicodes[i];
            glyphs[i] = this.getCachedGlyphCode(code);
            if (glyphs[i] != -1) continue;
            glyphs[i] = this.convertToGlyph(code);
        }
    }
}

