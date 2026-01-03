/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.geometry.size.AngleUnit
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.attributes;

import com.github.weisj.jsvg.attributes.HasMatchName;
import com.github.weisj.jsvg.attributes.paint.PaintParser;
import com.github.weisj.jsvg.attributes.paint.SVGPaint;
import com.github.weisj.jsvg.geometry.size.AngleUnit;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.Unit;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.parser.SeparatorMode;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AttributeParser {
    @NotNull
    private final PaintParser paintParser;
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");
    private static final Pattern TRANSFORM_PATTERN = Pattern.compile("\\w+\\([^)]*\\)");

    public AttributeParser(@NotNull PaintParser paintParser) {
        this.paintParser = paintParser;
    }

    @Contract(value="_,!null -> !null")
    @Nullable
    public Length parseLength(@Nullable String value, @Nullable Length fallback) {
        if (value == null) {
            return fallback;
        }
        Unit unit = Unit.Raw;
        String lower = value.toLowerCase(Locale.ENGLISH);
        for (Unit u : Unit.units()) {
            if (!lower.endsWith(u.suffix())) continue;
            unit = u;
            break;
        }
        String str = lower.substring(0, lower.length() - unit.suffix().length());
        try {
            return unit.valueOf(Float.parseFloat(str));
        }
        catch (NumberFormatException e) {
            return fallback;
        }
    }

    public float parsePercentage(@Nullable String value, float fallback) {
        return this.parsePercentage(value, fallback, 0.0f, 1.0f);
    }

    public float parsePercentage(@Nullable String value, float fallback, float min, float max) {
        if (value == null) {
            return fallback;
        }
        try {
            float parsed = value.endsWith("%") ? Float.parseFloat(value.substring(0, value.length() - 1)) / 100.0f : Float.parseFloat(value);
            return Math.max(min, Math.min(max, parsed));
        }
        catch (NumberFormatException e) {
            return fallback;
        }
    }

    public int parseInt(@Nullable String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
            return fallback;
        }
    }

    public float parseFloat(@Nullable String value, float fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Float.parseFloat(value);
        }
        catch (NumberFormatException e) {
            return fallback;
        }
    }

    public double parseDouble(@Nullable String value, double fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException e) {
            return fallback;
        }
    }

    public float parseAngle(@Nullable String value, float fallback) {
        if (value == null) {
            return fallback;
        }
        AngleUnit unit = AngleUnit.Raw;
        String lower = value.toLowerCase(Locale.ENGLISH);
        for (AngleUnit u : AngleUnit.units()) {
            if (!lower.endsWith(u.suffix())) continue;
            unit = u;
            break;
        }
        String str = lower.substring(0, lower.length() - unit.suffix().length());
        try {
            return unit.toRadians(Float.parseFloat(str), AngleUnit.Deg);
        }
        catch (NumberFormatException e) {
            return fallback;
        }
    }

    public Length[] parseLengthList(@Nullable String value) {
        if (value != null && value.equalsIgnoreCase("none")) {
            return new Length[0];
        }
        String[] values = this.parseStringList(value, SeparatorMode.COMMA_AND_WHITESPACE);
        Length[] ret = new Length[values.length];
        for (int i = 0; i < ret.length; ++i) {
            Length length = this.parseLength(values[i], null);
            if (length == null) {
                return new Length[0];
            }
            ret[i] = length;
        }
        return ret;
    }

    public float[] parseFloatList(@Nullable String value) {
        String[] values = this.parseStringList(value, SeparatorMode.COMMA_AND_WHITESPACE);
        float[] ret = new float[values.length];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = this.parseFloat(values[i], 0.0f);
        }
        return ret;
    }

    public double[] parseDoubleList(@Nullable String value) {
        String[] values = this.parseStringList(value, SeparatorMode.COMMA_AND_WHITESPACE);
        double[] ret = new double[values.length];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = this.parseDouble(values[i], 0.0);
        }
        return ret;
    }

    @NotNull
    public String[] parseStringList(@Nullable String value, SeparatorMode separatorMode) {
        int i;
        if (value == null || value.isEmpty()) {
            return new String[0];
        }
        ArrayList<String> list = new ArrayList<String>();
        int max = value.length();
        int start = 0;
        boolean inWhiteSpace = false;
        for (i = 0; i < max; ++i) {
            char c = value.charAt(i);
            if (Character.isWhitespace(c)) {
                if (!inWhiteSpace && separatorMode != SeparatorMode.COMMA_ONLY && i - start > 0) {
                    list.add(value.substring(start, i));
                    start = i + 1;
                }
                inWhiteSpace = true;
                continue;
            }
            inWhiteSpace = false;
            if (c != ',' || separatorMode == SeparatorMode.WHITESPACE_ONLY) continue;
            list.add(value.substring(start, i));
            start = i + 1;
        }
        if (i - start > 0) {
            list.add(value.substring(start, i));
        }
        return list.toArray(new String[0]);
    }

    @Nullable
    public SVGPaint parsePaint(@Nullable String value, @NotNull AttributeNode attributeNode) {
        return this.paintParser.parsePaint(value, attributeNode);
    }

    @NotNull
    public <E extends Enum<E>> E parseEnum(@Nullable String value, @NotNull E fallback) {
        E e = this.parseEnum(value, fallback.getDeclaringClass());
        if (e == null) {
            return fallback;
        }
        return e;
    }

    @Nullable
    public <E extends Enum<E>> E parseEnum(@Nullable String value, @NotNull Class<E> enumType) {
        if (value == null) {
            return null;
        }
        for (Enum enumConstant : (Enum[])enumType.getEnumConstants()) {
            String name;
            String string = name = enumConstant instanceof HasMatchName ? ((HasMatchName)((Object)enumConstant)).matchName() : enumConstant.name();
            if (!name.equalsIgnoreCase(value)) continue;
            return (E)enumConstant;
        }
        return null;
    }

    @NotNull
    private String removeWhiteSpace(@NotNull String value) {
        return WHITESPACE_PATTERN.matcher(value).replaceAll("");
    }

    @Nullable
    public String parseUrl(@Nullable String value) {
        if (value == null) {
            return null;
        }
        if (!value.startsWith("url(") || !value.endsWith(")")) {
            return this.removeWhiteSpace(value);
        }
        return this.removeWhiteSpace(value.substring(4, value.length() - 1));
    }

    @Nullable
    public AffineTransform parseTransform(@Nullable String value) {
        if (value == null) {
            return null;
        }
        Matcher transformMatcher = TRANSFORM_PATTERN.matcher(value);
        AffineTransform transform = new AffineTransform();
        while (transformMatcher.find()) {
            String group = transformMatcher.group();
            try {
                this.parseSingleTransform(group, transform);
            }
            catch (Exception e) {
                throw new IllegalArgumentException("Illegal transform definition '" + value + "' encountered error while parsing '" + group + "'", e);
            }
        }
        return transform;
    }

    private void parseSingleTransform(@NotNull String value, @NotNull AffineTransform tx) {
        int first = value.indexOf(40);
        int last = value.lastIndexOf(41);
        String command = value.substring(0, value.indexOf(40)).toLowerCase(Locale.ENGLISH);
        double[] values = this.parseDoubleList(value.substring(first + 1, last));
        switch (command) {
            case "matrix": {
                tx.concatenate(new AffineTransform(values));
                break;
            }
            case "translate": {
                if (values.length == 1) {
                    tx.translate(values[0], 0.0);
                    break;
                }
                tx.translate(values[0], values[1]);
                break;
            }
            case "translatex": {
                tx.translate(values[0], 0.0);
                break;
            }
            case "translatey": {
                tx.translate(0.0, values[0]);
                break;
            }
            case "scale": {
                if (values.length == 1) {
                    tx.scale(values[0], values[0]);
                    break;
                }
                tx.scale(values[0], values[1]);
                break;
            }
            case "scalex": {
                tx.scale(values[0], 1.0);
                break;
            }
            case "scaley": {
                tx.scale(1.0, values[0]);
                break;
            }
            case "rotate": {
                if (values.length > 2) {
                    tx.rotate(Math.toRadians(values[0]), values[1], values[2]);
                    break;
                }
                tx.rotate(Math.toRadians(values[0]));
                break;
            }
            case "skewx": {
                tx.shear(Math.tan(Math.toRadians(values[0])), 0.0);
                break;
            }
            case "skewy": {
                tx.shear(0.0, Math.tan(Math.toRadians(values[0])));
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown transform type: " + command);
            }
        }
    }

    @NotNull
    public PaintParser paintParser() {
        return this.paintParser;
    }
}

