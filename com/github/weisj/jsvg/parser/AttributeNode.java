/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.attributes.filter.DefaultFilterChannel
 *  com.github.weisj.jsvg.attributes.filter.FilterChannelKey$StringKey
 *  com.github.weisj.jsvg.parser.css.StyleSheet
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser;

import com.github.weisj.jsvg.attributes.AttributeParser;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.attributes.filter.DefaultFilterChannel;
import com.github.weisj.jsvg.attributes.filter.FilterChannelKey;
import com.github.weisj.jsvg.attributes.paint.PaintParser;
import com.github.weisj.jsvg.attributes.paint.SVGPaint;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.Unit;
import com.github.weisj.jsvg.nodes.ClipPath;
import com.github.weisj.jsvg.nodes.Mask;
import com.github.weisj.jsvg.nodes.filter.Filter;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.parser.LoadHelper;
import com.github.weisj.jsvg.parser.ParsedElement;
import com.github.weisj.jsvg.parser.ResourceLoader;
import com.github.weisj.jsvg.parser.SeparatorMode;
import com.github.weisj.jsvg.parser.css.StyleSheet;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AttributeNode {
    private static final Length TopOrLeft = new Length(Unit.PERCENTAGE, 0.0f);
    private static final Length Center = new Length(Unit.PERCENTAGE, 50.0f);
    private static final Length BottomOrRight = new Length(Unit.PERCENTAGE, 100.0f);
    @NotNull
    private final String tagName;
    @NotNull
    private final Map<String, String> attributes;
    @Nullable
    private final AttributeNode parent;
    @NotNull
    private final @NotNull Map<@NotNull String, @NotNull Object> namedElements;
    @NotNull
    private final @NotNull List<@NotNull StyleSheet> styleSheets;
    @NotNull
    private final LoadHelper loadHelper;

    public AttributeNode(@NotNull String tagName, @NotNull Map<String, String> attributes, @Nullable AttributeNode parent, @NotNull @NotNull Map<@NotNull String, @NotNull Object> namedElements, @NotNull @NotNull List<@NotNull StyleSheet> styleSheets, @NotNull LoadHelper loadHelper) {
        this.tagName = tagName;
        this.attributes = attributes;
        this.parent = parent;
        this.namedElements = namedElements;
        this.styleSheets = styleSheets;
        this.loadHelper = loadHelper;
    }

    void prepareForNodeBuilding(@NotNull ParsedElement parsedElement) {
        HashMap<String, String> styleSheetAttributes = new HashMap<String, String>();
        AttributeNode.preprocessAttributes(this.attributes, styleSheetAttributes);
        List<StyleSheet> sheets = this.styleSheets();
        for (int i = sheets.size() - 1; i >= 0; --i) {
            StyleSheet sheet = sheets.get(i);
            sheet.forEachMatchingRule(parsedElement, p -> {
                if (!styleSheetAttributes.containsKey(p.name())) {
                    styleSheetAttributes.put(p.name(), p.value());
                }
            });
        }
        this.attributes.putAll(styleSheetAttributes);
    }

    private static boolean isBlank(@NotNull String s) {
        return s.trim().isEmpty();
    }

    private static void preprocessAttributes(@NotNull Map<String, String> attributes, @NotNull Map<String, String> styleAttributes) {
        String styleStr = attributes.get("style");
        if (styleStr != null && !AttributeNode.isBlank(styleStr)) {
            String[] styles;
            for (String style : styles = styleStr.split(";")) {
                if (AttributeNode.isBlank(style)) continue;
                String[] styleDef = style.split(":", 2);
                styleAttributes.put(styleDef[0].trim().toLowerCase(Locale.ENGLISH), styleDef[1].trim());
            }
        }
    }

    @NotNull
    Map<String, Object> namedElements() {
        return this.namedElements;
    }

    @NotNull
    @NotNull List<@NotNull StyleSheet> styleSheets() {
        return this.styleSheets;
    }

    @Nullable
    private <T> T getElementById(@NotNull Class<T> type, @Nullable String id) {
        if (id == null) {
            return null;
        }
        Object node = this.namedElements.get(id);
        if (node instanceof ParsedElement) {
            node = ((ParsedElement)node).nodeEnsuringBuildStatus();
        }
        return type.isInstance(node) ? (T)type.cast(node) : null;
    }

    @Nullable
    private <T> T getElementByUrl(@NotNull Class<T> type, @Nullable String value) {
        String url = this.loadHelper.attributeParser().parseUrl(value);
        if (url != null && url.startsWith("#")) {
            url = url.substring(1);
        }
        return this.getElementById(type, url);
    }

    @Nullable
    public <T> T getElementByHref(@NotNull Class<T> type, @Nullable String value) {
        if (value == null) {
            return null;
        }
        return this.getElementByUrl(type, value);
    }

    @Nullable
    public <T> T getElementByHref(@NotNull Class<T> type, @NotNull Category category, @Nullable String value) {
        T element = this.getElementByHref(type, value);
        if (element == null) {
            return null;
        }
        for (Category cat : element.getClass().getAnnotation(ElementCategories.class).value()) {
            if (cat != category) continue;
            return element;
        }
        return null;
    }

    @NotNull
    public Map<String, String> attributes() {
        return this.attributes;
    }

    @NotNull
    public String tagName() {
        return this.tagName;
    }

    public boolean tagIsOneOf(String ... tags) {
        for (String tag : tags) {
            if (!this.tagName.equals(tag)) continue;
            return true;
        }
        return false;
    }

    @Nullable
    public AttributeNode parent() {
        return this.parent;
    }

    @Nullable
    public String getValue(@NotNull String key) {
        return this.attributes.get(key);
    }

    @NotNull
    public Color getColor(@NotNull String key) {
        return this.getColor(key, PaintParser.DEFAULT_COLOR);
    }

    @Contract(value="_,!null -> !null")
    @Nullable
    public Color getColor(@NotNull String key, @Nullable Color fallback) {
        String value = this.getValue(key);
        if (value == null) {
            return fallback;
        }
        Color c = this.loadHelper.attributeParser().paintParser().parseColor(value.toLowerCase(Locale.ENGLISH), this);
        return c != null ? c : fallback;
    }

    @NotNull
    public SVGPaint getPaint(@NotNull String key, @NotNull SVGPaint fallback) {
        SVGPaint paint = this.getPaint(key);
        return paint != null ? paint : fallback;
    }

    @Nullable
    public SVGPaint getPaint(@NotNull String key) {
        String value = this.getValue(key);
        SVGPaint paint = this.getElementByUrl(SVGPaint.class, value);
        if (paint != null) {
            return paint;
        }
        return this.loadHelper.attributeParser().parsePaint(value, this);
    }

    @Nullable
    public Length getLength(@NotNull String key) {
        return this.getLengthInternal(key, null);
    }

    @NotNull
    public Length getLength(@NotNull String key, float fallback) {
        return this.getLength(key, Unit.Raw.valueOf(fallback));
    }

    @NotNull
    public Length getLength(@NotNull String key, @NotNull Length fallback) {
        return this.getLengthInternal(key, fallback);
    }

    @Contract(value="_,!null -> !null")
    @Nullable
    private Length getLengthInternal(@NotNull String key, @Nullable Length fallback) {
        return this.loadHelper.attributeParser().parseLength(this.getValue(key), fallback);
    }

    @NotNull
    public Length getHorizontalReferenceLength(@NotNull String key) {
        return this.parseReferenceLength(key, "left", "right");
    }

    @NotNull
    public Length getVerticalReferenceLength(@NotNull String key) {
        return this.parseReferenceLength(key, "top", "bottom");
    }

    @NotNull
    private Length parseReferenceLength(@NotNull String key, @NotNull String topLeft, @NotNull String bottomRight) {
        String value = this.getValue(key);
        if (topLeft.equals(value)) {
            return TopOrLeft;
        }
        if ("center".equals(value)) {
            return Center;
        }
        if (bottomRight.equals(value)) {
            return BottomOrRight;
        }
        return this.loadHelper.attributeParser().parseLength(value, Length.ZERO);
    }

    public float getPercentage(@NotNull String key, float fallback) {
        return this.loadHelper.attributeParser().parsePercentage(this.getValue(key), fallback);
    }

    @NotNull
    public Length[] getLengthList(@NotNull String key) {
        return this.loadHelper.attributeParser().parseLengthList(this.getValue(key));
    }

    public float[] getFloatList(@NotNull String key) {
        return this.loadHelper.attributeParser().parseFloatList(this.getValue(key));
    }

    public double[] getDoubleList(@NotNull String key) {
        return this.loadHelper.attributeParser().parseDoubleList(this.getValue(key));
    }

    @NotNull
    public <E extends Enum<E>> E getEnum(@NotNull String key, @NotNull E fallback) {
        return this.loadHelper.attributeParser().parseEnum(this.getValue(key), fallback);
    }

    @Nullable
    public <E extends Enum<E>> E getEnumNullable(@NotNull String key, @NotNull Class<E> enumType) {
        return this.loadHelper.attributeParser().parseEnum(this.getValue(key), enumType);
    }

    @Nullable
    public ClipPath getClipPath() {
        return this.getElementByUrl(ClipPath.class, this.getValue("clip-path"));
    }

    @Nullable
    public Mask getMask() {
        return this.getElementByUrl(Mask.class, this.getValue("mask"));
    }

    @Nullable
    public Filter getFilter() {
        return this.getElementByUrl(Filter.class, this.getValue("filter"));
    }

    @NotNull
    public FilterChannelKey getFilterChannelKey(@NotNull String key, @NotNull DefaultFilterChannel fallback) {
        String in = this.getValue(key);
        if (in == null) {
            return fallback;
        }
        return new FilterChannelKey.StringKey(in);
    }

    @Nullable
    public AffineTransform parseTransform(@NotNull String key) {
        return this.loadHelper.attributeParser().parseTransform(this.getValue(key));
    }

    public boolean hasAttribute(@NotNull String name) {
        return this.attributes.containsKey(name);
    }

    @NotNull
    public String[] getStringList(@NotNull String name) {
        return this.getStringList(name, SeparatorMode.COMMA_AND_WHITESPACE);
    }

    @NotNull
    public String[] getStringList(@NotNull String name, SeparatorMode separatorMode) {
        return this.loadHelper.attributeParser().parseStringList(this.getValue(name), separatorMode);
    }

    public float getFloat(@NotNull String name, float fallback) {
        return this.loadHelper.attributeParser().parseFloat(this.getValue(name), fallback);
    }

    public float getNonNegativeFloat(@NotNull String name, float fallback) {
        float value = this.getFloat(name, fallback);
        if (Float.isFinite(value) && value < 0.0f) {
            return fallback;
        }
        return value;
    }

    public int getInt(@NotNull String key, int fallback) {
        return this.loadHelper.attributeParser().parseInt(this.getValue(key), fallback);
    }

    @Nullable
    public String getHref() {
        String href = this.getValue("href");
        if (href == null) {
            return this.getValue("xlink:href");
        }
        return href;
    }

    @Nullable
    public ViewBox getViewBox() {
        float[] viewBoxCords = this.getFloatList("viewBox");
        return viewBoxCords.length == 4 ? new ViewBox(viewBoxCords) : null;
    }

    @NotNull
    public AttributeParser parser() {
        return this.loadHelper.attributeParser();
    }

    @NotNull
    public ResourceLoader resourceLoader() {
        return this.loadHelper.resourceLoader();
    }
}

