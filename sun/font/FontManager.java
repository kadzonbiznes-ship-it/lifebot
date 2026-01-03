/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import sun.font.CreatedFontTracker;
import sun.font.Font2D;
import sun.font.Font2DHandle;

public interface FontManager {
    public static final int NO_FALLBACK = 0;
    public static final int PHYSICAL_FALLBACK = 1;
    public static final int LOGICAL_FALLBACK = 2;

    public boolean registerFont(Font var1);

    public void deRegisterBadFont(Font2D var1);

    public Font2D findFont2D(String var1, int var2, int var3);

    public Font2D[] createFont2D(File var1, int var2, boolean var3, boolean var4, CreatedFontTracker var5) throws FontFormatException;

    public Font2DHandle getNewComposite(String var1, int var2, Font2DHandle var3);

    public void preferLocaleFonts();

    public void preferProportionalFonts();
}

