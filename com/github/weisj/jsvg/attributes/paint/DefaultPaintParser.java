/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.attributes.paint.DefaultPaintParser$ColorLookup
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.attributes.paint;

import com.github.weisj.jsvg.attributes.AttributeParser;
import com.github.weisj.jsvg.attributes.paint.AwtSVGPaint;
import com.github.weisj.jsvg.attributes.paint.DefaultPaintParser;
import com.github.weisj.jsvg.attributes.paint.PaintParser;
import com.github.weisj.jsvg.attributes.paint.SVGPaint;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.parser.SeparatorMode;
import java.awt.Color;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * Exception performing whole class analysis ignored.
 */
public final class DefaultPaintParser
implements PaintParser {
    private static final Logger LOGGER = Logger.getLogger(DefaultPaintParser.class.getName());

    @Override
    @Nullable
    public Color parseColor(@NotNull String value, @NotNull AttributeNode node) {
        if (value.isEmpty()) {
            return null;
        }
        try {
            if (value.charAt(0) == '#') {
                int rgba = -16777216;
                switch (value.length()) {
                    case 4: {
                        rgba = this.parseHex(new char[]{value.charAt(1), value.charAt(1), value.charAt(2), value.charAt(2), value.charAt(3), value.charAt(3), 'F', 'F'});
                        break;
                    }
                    case 5: {
                        rgba = this.parseHex(new char[]{value.charAt(1), value.charAt(1), value.charAt(2), value.charAt(2), value.charAt(3), value.charAt(3), value.charAt(4), value.charAt(4)});
                        break;
                    }
                    case 7: {
                        rgba = this.parseHex(new char[]{value.charAt(1), value.charAt(2), value.charAt(3), value.charAt(4), value.charAt(5), value.charAt(6), 'F', 'F'});
                        break;
                    }
                    case 9: {
                        rgba = this.parseHex(value.substring(1).toCharArray());
                        break;
                    }
                }
                return new Color(rgba, true);
            }
            if (value.length() > 3 && value.substring(0, 3).equalsIgnoreCase("rgb")) {
                boolean isRgba = value.length() > 4 && (value.charAt(3) == 'a' || value.charAt(3) == 'A');
                int startIndex = isRgba ? 5 : 4;
                String[] values = node.parser().parseStringList(value.substring(startIndex, value.length() - 1), SeparatorMode.COMMA_AND_WHITESPACE);
                isRgba = isRgba && values.length >= 4;
                AttributeParser parser = node.parser();
                return new Color(this.parseColorComponent(values[0], false, parser), this.parseColorComponent(values[1], false, parser), this.parseColorComponent(values[2], false, parser), isRgba ? this.parseColorComponent(values[3], true, parser) : 255);
            }
            return (Color)ColorLookup.access$000().get(value.toLowerCase(Locale.ENGLISH));
        }
        catch (Exception e) {
            LOGGER.log(Level.INFO, e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Nullable
    public SVGPaint parsePaint(@Nullable String value, @NotNull AttributeNode node) {
        if (value == null) {
            return null;
        }
        String lower = value.toLowerCase(Locale.ENGLISH);
        if ("none".equals(lower) || "transparent".equals(lower)) {
            return SVGPaint.NONE;
        }
        if ("currentcolor".equals(lower)) {
            return SVGPaint.CURRENT_COLOR;
        }
        if ("context-fill".equals(lower)) {
            return SVGPaint.CONTEXT_FILL;
        }
        if ("context-stroke".equals(lower)) {
            return SVGPaint.CONTEXT_STROKE;
        }
        Color color = this.parseColor(lower, node);
        if (color == null) {
            return null;
        }
        return new AwtSVGPaint(color);
    }

    private int parseColorComponent(String value, boolean percentage, @NotNull AttributeParser parser) {
        float parsed;
        if (value.endsWith("%")) {
            parsed = parser.parseFloat(value.substring(0, value.length() - 1), 0.0f);
            parsed /= 100.0f;
            parsed *= 255.0f;
        } else {
            parsed = parser.parseFloat(value, 0.0f);
            if (percentage) {
                parsed *= 255.0f;
            }
        }
        return Math.min(255, Math.max(0, (int)parsed));
    }

    private int parseHex(char[] chars) {
        int r = this.charToColorInt(chars[0]) << 4 | this.charToColorInt(chars[1]);
        int g = this.charToColorInt(chars[2]) << 4 | this.charToColorInt(chars[3]);
        int b = this.charToColorInt(chars[4]) << 4 | this.charToColorInt(chars[5]);
        int a = this.charToColorInt(chars[6]) << 4 | this.charToColorInt(chars[7]);
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    private int charToColorInt(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c >= 'a' && c <= 'z') {
            return c - 97 + 10;
        }
        if (c >= 'A' && c <= 'Z') {
            return c - 65 + 10;
        }
        return 0;
    }
}

