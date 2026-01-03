/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import sun.awt.Win32FontManager;
import sun.font.FontManager;

final class PlatformFontInfo {
    PlatformFontInfo() {
    }

    static FontManager createFontManager() {
        return new Win32FontManager();
    }
}

