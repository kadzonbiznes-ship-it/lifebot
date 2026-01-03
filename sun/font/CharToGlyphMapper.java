/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

public abstract class CharToGlyphMapper {
    public static final int HI_SURROGATE_START = 55296;
    public static final int HI_SURROGATE_END = 56319;
    public static final int LO_SURROGATE_START = 56320;
    public static final int LO_SURROGATE_END = 57343;
    public static final int VS_START = 65024;
    public static final int VS_END = 65039;
    public static final int VSS_START = 917760;
    public static final int VSS_END = 918015;
    public static final int UNINITIALIZED_GLYPH = -1;
    public static final int INVISIBLE_GLYPH_ID = 65535;
    public static final int INVISIBLE_GLYPHS = 65534;
    protected int missingGlyph = -1;

    public int getMissingGlyphCode() {
        return this.missingGlyph;
    }

    public boolean canDisplay(char ch) {
        int glyph = this.charToGlyph(ch);
        return glyph != this.missingGlyph;
    }

    public boolean canDisplay(int cp) {
        int glyph = this.charToGlyph(cp);
        return glyph != this.missingGlyph;
    }

    public int charToGlyph(char unicode) {
        char[] chars = new char[1];
        int[] glyphs = new int[1];
        chars[0] = unicode;
        this.charsToGlyphs(1, chars, glyphs);
        return glyphs[0];
    }

    public int charToGlyph(int unicode) {
        int[] chars = new int[1];
        int[] glyphs = new int[1];
        chars[0] = unicode;
        this.charsToGlyphs(1, chars, glyphs);
        return glyphs[0];
    }

    public int charToVariationGlyph(int unicode, int variationSelector) {
        return this.charToGlyph(unicode);
    }

    public abstract int getNumGlyphs();

    public abstract void charsToGlyphs(int var1, char[] var2, int[] var3);

    public abstract boolean charsToGlyphsNS(int var1, char[] var2, int[] var3);

    public abstract void charsToGlyphs(int var1, int[] var2, int[] var3);

    public static boolean isVariationSelector(int charCode) {
        return charCode >= 917760 && charCode <= 918015 || charCode >= 65024 && charCode <= 65039;
    }
}

