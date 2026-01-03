/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.Font;
import java.util.Locale;
import java.util.TreeMap;
import sun.font.FontManager;

public interface FontManagerForSGE
extends FontManager {
    public Font[] getCreatedFonts();

    public TreeMap<String, String> getCreatedFontFamilyNames();

    public Font[] getAllInstalledFonts();

    public String[] getInstalledFontFamilyNames(Locale var1);

    public void useAlternateFontforJALocales();
}

