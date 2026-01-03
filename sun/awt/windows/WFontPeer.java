/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.windows;

import sun.awt.PlatformFont;
import sun.awt.windows.WFontConfiguration;

final class WFontPeer
extends PlatformFont {
    private String textComponentFontName;

    public WFontPeer(String name, int style) {
        super(name, style);
        if (this.fontConfig != null) {
            this.textComponentFontName = ((WFontConfiguration)this.fontConfig).getTextComponentFontName(this.familyName, style);
        }
    }

    @Override
    protected char getMissingGlyphCharacter() {
        return '\u2751';
    }

    private static native void initIDs();

    static {
        WFontPeer.initIDs();
    }
}

