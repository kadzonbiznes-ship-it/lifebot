/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.Objects;

public class FontUtil {
    public static final Font NUNITO_REGULAR;
    public static final Font NUNITO_BOLD;
    public static final Font NUNITO_BLACK;

    static {
        try {
            NUNITO_REGULAR = Font.createFont(0, Objects.requireNonNull(FontUtil.class.getResourceAsStream("/fonts/Nunito-Regular.ttf"))).deriveFont(13.0f);
            NUNITO_BOLD = Font.createFont(0, Objects.requireNonNull(FontUtil.class.getResourceAsStream("/fonts/Nunito-Bold.ttf"))).deriveFont(13.0f);
            NUNITO_BLACK = Font.createFont(0, Objects.requireNonNull(FontUtil.class.getResourceAsStream("/fonts/Nunito-Black.ttf"))).deriveFont(13.0f);
        }
        catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}

