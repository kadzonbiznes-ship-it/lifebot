/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.attributes.font.LengthFontSize
 *  com.github.weisj.jsvg.attributes.font.NumberFontWeight
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.attributes.font;

import com.github.weisj.jsvg.attributes.font.AttributeFontSpec;
import com.github.weisj.jsvg.attributes.font.FontSize;
import com.github.weisj.jsvg.attributes.font.FontStretch;
import com.github.weisj.jsvg.attributes.font.FontStyle;
import com.github.weisj.jsvg.attributes.font.FontWeight;
import com.github.weisj.jsvg.attributes.font.LengthFontSize;
import com.github.weisj.jsvg.attributes.font.NumberFontWeight;
import com.github.weisj.jsvg.attributes.font.PredefinedFontSize;
import com.github.weisj.jsvg.attributes.font.PredefinedFontWeight;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.parser.SeparatorMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FontParser {
    private FontParser() {
    }

    @NotNull
    public static AttributeFontSpec parseFontSpec(@NotNull AttributeNode node) {
        String[] fontFamilies = node.getStringList("font-family", SeparatorMode.COMMA_ONLY);
        @Nullable FontWeight weight = FontParser.parseWeight(node);
        @Nullable FontSize size = FontParser.parseFontSize(node);
        @Nullable Length sizeAdjust = FontParser.parseSizeAdjust(node);
        @Nullable FontStyle style = FontParser.parseFontStyle(node);
        float stretch = FontParser.parseStretch(node);
        return new AttributeFontSpec(fontFamilies, style, sizeAdjust, stretch, size, weight);
    }

    @Nullable
    public static FontWeight parseWeight(@NotNull AttributeNode node) {
        String fontWeightKey = "font-weight";
        FontWeight weight = node.getEnum("font-weight", PredefinedFontWeight.Number);
        if (weight == PredefinedFontWeight.Number) {
            weight = node.hasAttribute("font-weight") ? new NumberFontWeight(Math.max(1.0f, Math.min(1000.0f, node.getFloat("font-weight", 400.0f)))) : null;
        }
        return weight;
    }

    public static float parseStretch(@NotNull AttributeNode node) {
        FontStretch stretch = node.getEnum("font-stretch", FontStretch.Percentage);
        return stretch == FontStretch.Percentage ? node.parser().parsePercentage(node.getValue("font-stretch"), Float.NaN, 0.5f, 2.0f) : stretch.percentage();
    }

    @Nullable
    public static FontSize parseFontSize(@NotNull AttributeNode node) {
        FontSize fontSize = node.getEnum("font-size", PredefinedFontSize.Number);
        if (fontSize == PredefinedFontSize.Number) {
            Length size = node.getLength("font-size", Length.UNSPECIFIED);
            fontSize = size.isSpecified() ? new LengthFontSize(size) : null;
        }
        return fontSize;
    }

    @Nullable
    public static Length parseSizeAdjust(@NotNull AttributeNode node) {
        return node.getLength("font-size-adjust");
    }

    @Nullable
    public static FontStyle parseFontStyle(@NotNull AttributeNode node) {
        FontStyle style = null;
        String styleStr = node.getValue("font-style");
        if ("normal".equalsIgnoreCase(styleStr)) {
            style = FontStyle.normal();
        } else if ("italic".equalsIgnoreCase(styleStr)) {
            style = FontStyle.italic();
        } else if (styleStr != null && styleStr.startsWith("oblique")) {
            String[] comps = styleStr.split(" ", 2);
            style = comps.length == 2 ? new FontStyle.Oblique(node.parser().parseAngle(comps[1], FontStyle.Oblique.DEFAULT_ANGLE)) : FontStyle.oblique();
        }
        return style;
    }
}

