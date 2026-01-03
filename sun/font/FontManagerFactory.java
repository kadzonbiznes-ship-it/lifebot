/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import sun.font.FontManager;
import sun.font.PlatformFontInfo;

public final class FontManagerFactory {
    private static volatile FontManager instance;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static FontManager getInstance() {
        FontManager result = instance;
        if (result != null) return result;
        Class<FontManagerFactory> clazz = FontManagerFactory.class;
        synchronized (FontManagerFactory.class) {
            result = instance;
            if (result != null) return result;
            instance = result = PlatformFontInfo.createFontManager();
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return result;
        }
    }
}

