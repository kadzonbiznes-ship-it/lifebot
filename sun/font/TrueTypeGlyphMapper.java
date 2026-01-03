/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.nio.ByteBuffer;
import sun.font.CMap;
import sun.font.CharToGlyphMapper;
import sun.font.FontUtilities;
import sun.font.SunFontManager;
import sun.font.TrueTypeFont;

public class TrueTypeGlyphMapper
extends CharToGlyphMapper {
    TrueTypeFont font;
    CMap cmap;
    int numGlyphs;

    public TrueTypeGlyphMapper(TrueTypeFont font) {
        this.font = font;
        try {
            this.cmap = CMap.initialize(font);
        }
        catch (Exception e) {
            this.cmap = null;
        }
        if (this.cmap == null) {
            this.handleBadCMAP();
        }
        this.missingGlyph = 0;
        ByteBuffer buffer = font.getTableBuffer(1835104368);
        if (buffer != null && buffer.capacity() >= 6) {
            this.numGlyphs = buffer.getChar(4);
        } else {
            this.handleBadCMAP();
        }
    }

    @Override
    public int getNumGlyphs() {
        return this.numGlyphs;
    }

    private char getGlyphFromCMAP(int charCode) {
        try {
            char glyphCode = this.cmap.getGlyph(charCode);
            if (glyphCode < this.numGlyphs || glyphCode >= '\ufffe') {
                return glyphCode;
            }
            if (FontUtilities.isLogging()) {
                FontUtilities.logWarning(String.valueOf(this.font) + " out of range glyph id=" + Integer.toHexString(glyphCode) + " for char " + Integer.toHexString(charCode));
            }
            return (char)this.missingGlyph;
        }
        catch (Exception e) {
            this.handleBadCMAP();
            return (char)this.missingGlyph;
        }
    }

    private char getGlyphFromCMAP(int charCode, int variationSelector) {
        if (variationSelector == 0) {
            return this.getGlyphFromCMAP(charCode);
        }
        try {
            char glyphCode = this.cmap.getVariationGlyph(charCode, variationSelector);
            if (glyphCode < this.numGlyphs || glyphCode >= '\ufffe') {
                return glyphCode;
            }
            if (FontUtilities.isLogging()) {
                FontUtilities.logWarning(String.valueOf(this.font) + " out of range glyph id=" + Integer.toHexString(glyphCode) + " for char " + Integer.toHexString(charCode) + " for vs " + Integer.toHexString(variationSelector));
            }
            return (char)this.missingGlyph;
        }
        catch (Exception e) {
            this.handleBadCMAP();
            return (char)this.missingGlyph;
        }
    }

    private void handleBadCMAP() {
        if (FontUtilities.isLogging()) {
            FontUtilities.logSevere("Null Cmap for " + String.valueOf(this.font) + "substituting for this font");
        }
        SunFontManager.getInstance().deRegisterBadFont(this.font);
        this.cmap = CMap.theNullCmap;
    }

    @Override
    public int charToGlyph(char unicode) {
        char glyph = this.getGlyphFromCMAP(unicode);
        return glyph;
    }

    @Override
    public int charToGlyph(int unicode) {
        char glyph = this.getGlyphFromCMAP(unicode);
        return glyph;
    }

    @Override
    public int charToVariationGlyph(int unicode, int variationSelector) {
        char glyph = this.getGlyphFromCMAP(unicode, variationSelector);
        return glyph;
    }

    @Override
    public void charsToGlyphs(int count, int[] unicodes, int[] glyphs) {
        for (int i = 0; i < count; ++i) {
            glyphs[i] = this.getGlyphFromCMAP(unicodes[i]);
        }
    }

    @Override
    public void charsToGlyphs(int count, char[] unicodes, int[] glyphs) {
        for (int i = 0; i < count; ++i) {
            char low;
            int code = unicodes[i];
            if (code >= 55296 && code <= 56319 && i < count - 1 && (low = unicodes[i + 1]) >= '\udc00' && low <= '\udfff') {
                code = (code - 55296) * 1024 + low - 56320 + 65536;
                glyphs[i] = this.getGlyphFromCMAP(code);
                glyphs[++i] = 65535;
                continue;
            }
            glyphs[i] = this.getGlyphFromCMAP(code);
        }
    }

    @Override
    public boolean charsToGlyphsNS(int count, char[] unicodes, int[] glyphs) {
        for (int i = 0; i < count; ++i) {
            char low;
            int code = unicodes[i];
            if (code >= 55296 && code <= 56319 && i < count - 1 && (low = unicodes[i + 1]) >= '\udc00' && low <= '\udfff') {
                code = (code - 55296) * 1024 + low - 56320 + 65536;
                glyphs[i + 1] = 65535;
            }
            glyphs[i] = this.getGlyphFromCMAP(code);
            if (code < 768) continue;
            if (FontUtilities.isComplexCharCode(code) || CharToGlyphMapper.isVariationSelector(code)) {
                return true;
            }
            if (code < 65536) continue;
            ++i;
        }
        return false;
    }

    boolean hasSupplementaryChars() {
        return this.cmap instanceof CMap.CMapFormat8 || this.cmap instanceof CMap.CMapFormat10 || this.cmap instanceof CMap.CMapFormat12;
    }
}

