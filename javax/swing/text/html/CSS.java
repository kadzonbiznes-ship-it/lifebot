/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.ImageIcon;
import javax.swing.SizeRequirements;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.View;
import javax.swing.text.html.CSSBorder;
import javax.swing.text.html.HTML;
import javax.swing.text.html.StyleSheet;

public class CSS
implements Serializable {
    private static final Hashtable<String, Attribute> attributeMap;
    private static final Hashtable<String, Value> valueMap;
    private static final Hashtable<HTML.Attribute, Attribute[]> htmlAttrToCssAttrMap;
    private static final Hashtable<Object, Attribute> styleConstantToCssMap;
    private static final Hashtable<String, Value> htmlValueToCssValueMap;
    private static final Hashtable<String, Value> cssValueToInternalValueMap;
    private transient Hashtable<Object, Object> valueConvertor;
    private int baseFontSize = baseFontSizeIndex + 1;
    private transient StyleSheet styleSheet = null;
    static int baseFontSizeIndex;

    public CSS() {
        this.valueConvertor = new Hashtable();
        this.valueConvertor.put(Attribute.FONT_SIZE, new FontSize());
        this.valueConvertor.put(Attribute.FONT_FAMILY, new FontFamily());
        this.valueConvertor.put(Attribute.FONT_WEIGHT, new FontWeight());
        BorderStyle bs = new BorderStyle();
        this.valueConvertor.put(Attribute.BORDER_TOP_STYLE, bs);
        this.valueConvertor.put(Attribute.BORDER_RIGHT_STYLE, bs);
        this.valueConvertor.put(Attribute.BORDER_BOTTOM_STYLE, bs);
        this.valueConvertor.put(Attribute.BORDER_LEFT_STYLE, bs);
        ColorValue cv = new ColorValue();
        this.valueConvertor.put(Attribute.COLOR, cv);
        this.valueConvertor.put(Attribute.BACKGROUND_COLOR, cv);
        this.valueConvertor.put(Attribute.BORDER_TOP_COLOR, cv);
        this.valueConvertor.put(Attribute.BORDER_RIGHT_COLOR, cv);
        this.valueConvertor.put(Attribute.BORDER_BOTTOM_COLOR, cv);
        this.valueConvertor.put(Attribute.BORDER_LEFT_COLOR, cv);
        LengthValue lv = new LengthValue();
        this.valueConvertor.put(Attribute.MARGIN_TOP, lv);
        this.valueConvertor.put(Attribute.MARGIN_BOTTOM, lv);
        this.valueConvertor.put(Attribute.MARGIN_LEFT, lv);
        this.valueConvertor.put(Attribute.MARGIN_LEFT_LTR, lv);
        this.valueConvertor.put(Attribute.MARGIN_LEFT_RTL, lv);
        this.valueConvertor.put(Attribute.MARGIN_RIGHT, lv);
        this.valueConvertor.put(Attribute.MARGIN_RIGHT_LTR, lv);
        this.valueConvertor.put(Attribute.MARGIN_RIGHT_RTL, lv);
        this.valueConvertor.put(Attribute.PADDING_TOP, lv);
        this.valueConvertor.put(Attribute.PADDING_BOTTOM, lv);
        this.valueConvertor.put(Attribute.PADDING_LEFT, lv);
        this.valueConvertor.put(Attribute.PADDING_RIGHT, lv);
        BorderWidthValue bv = new BorderWidthValue(null, 0);
        this.valueConvertor.put(Attribute.BORDER_TOP_WIDTH, bv);
        this.valueConvertor.put(Attribute.BORDER_BOTTOM_WIDTH, bv);
        this.valueConvertor.put(Attribute.BORDER_LEFT_WIDTH, bv);
        this.valueConvertor.put(Attribute.BORDER_RIGHT_WIDTH, bv);
        LengthValue nlv = new LengthValue(true);
        this.valueConvertor.put(Attribute.TEXT_INDENT, nlv);
        this.valueConvertor.put(Attribute.WIDTH, lv);
        this.valueConvertor.put(Attribute.HEIGHT, lv);
        this.valueConvertor.put(Attribute.BORDER_SPACING, lv);
        StringValue sv = new StringValue();
        this.valueConvertor.put(Attribute.FONT_STYLE, sv);
        this.valueConvertor.put(Attribute.TEXT_DECORATION, sv);
        this.valueConvertor.put(Attribute.TEXT_ALIGN, sv);
        this.valueConvertor.put(Attribute.VERTICAL_ALIGN, sv);
        CssValueMapper valueMapper = new CssValueMapper();
        this.valueConvertor.put(Attribute.LIST_STYLE_TYPE, valueMapper);
        this.valueConvertor.put(Attribute.BACKGROUND_IMAGE, new BackgroundImage());
        this.valueConvertor.put(Attribute.BACKGROUND_POSITION, new BackgroundPosition());
        this.valueConvertor.put(Attribute.BACKGROUND_REPEAT, valueMapper);
        this.valueConvertor.put(Attribute.BACKGROUND_ATTACHMENT, valueMapper);
        CssValue generic = new CssValue();
        for (Attribute key : Attribute.allAttributes) {
            if (this.valueConvertor.get(key) != null) continue;
            this.valueConvertor.put(key, generic);
        }
    }

    void setBaseFontSize(int sz) {
        this.baseFontSize = sz < 1 ? 0 : (sz > 7 ? 7 : sz);
    }

    void setBaseFontSize(String size) {
        if (size != null) {
            if (size.startsWith("+")) {
                int relSize = Integer.parseInt(size.substring(1));
                this.setBaseFontSize(this.baseFontSize + relSize);
            } else if (size.startsWith("-")) {
                int relSize = -Integer.parseInt(size.substring(1));
                this.setBaseFontSize(this.baseFontSize + relSize);
            } else {
                this.setBaseFontSize(Integer.parseInt(size));
            }
        }
    }

    int getBaseFontSize() {
        return this.baseFontSize;
    }

    void addInternalCSSValue(MutableAttributeSet attr, Attribute key, String value) {
        if (key == Attribute.FONT) {
            ShorthandFontParser.parseShorthandFont(this, value, attr);
        } else if (key == Attribute.BACKGROUND) {
            ShorthandBackgroundParser.parseShorthandBackground(this, value, attr);
        } else if (key == Attribute.MARGIN) {
            ShorthandMarginParser.parseShorthandMargin(this, value, attr, Attribute.ALL_MARGINS);
        } else if (key == Attribute.PADDING) {
            ShorthandMarginParser.parseShorthandMargin(this, value, attr, Attribute.ALL_PADDING);
        } else if (key == Attribute.BORDER_WIDTH) {
            ShorthandMarginParser.parseShorthandMargin(this, value, attr, Attribute.ALL_BORDER_WIDTHS);
        } else if (key == Attribute.BORDER_COLOR) {
            ShorthandMarginParser.parseShorthandMargin(this, value, attr, Attribute.ALL_BORDER_COLORS);
        } else if (key == Attribute.BORDER_STYLE) {
            ShorthandMarginParser.parseShorthandMargin(this, value, attr, Attribute.ALL_BORDER_STYLES);
        } else if (key == Attribute.BORDER || key == Attribute.BORDER_TOP || key == Attribute.BORDER_RIGHT || key == Attribute.BORDER_BOTTOM || key == Attribute.BORDER_LEFT) {
            ShorthandBorderParser.parseShorthandBorder(attr, key, value);
        } else {
            Object iValue = this.getInternalCSSValue(key, value);
            if (iValue != null) {
                attr.addAttribute(key, iValue);
            }
        }
    }

    Object getInternalCSSValue(Attribute key, String value) {
        CssValue conv = (CssValue)this.valueConvertor.get(key);
        Object r = conv.parseCssValue(value);
        return r != null ? r : conv.parseCssValue(key.getDefaultValue());
    }

    static Object mergeTextDecoration(String value) {
        if (value.startsWith("none")) {
            return null;
        }
        boolean underline = value.contains("underline");
        boolean strikeThrough = value.contains("line-through");
        if (!underline && !strikeThrough) {
            return null;
        }
        String newValue = underline && strikeThrough ? "underline,line-through" : (underline ? "underline" : "line-through");
        return new StringValue().parseCssValue(newValue);
    }

    Attribute styleConstantsKeyToCSSKey(StyleConstants sc) {
        return styleConstantToCssMap.get(sc);
    }

    Object styleConstantsValueToCSSValue(StyleConstants sc, Object styleValue) {
        Attribute cssKey = this.styleConstantsKeyToCSSKey(sc);
        if (cssKey != null) {
            CssValue conv = (CssValue)this.valueConvertor.get(cssKey);
            return conv.fromStyleConstants(sc, styleValue);
        }
        return null;
    }

    Object cssValueToStyleConstantsValue(StyleConstants key, Object value) {
        if (value instanceof CssValue) {
            return ((CssValue)value).toStyleConstants(key, null);
        }
        return null;
    }

    Font getFont(StyleContext sc, AttributeSet a, int defaultSize, StyleSheet ss) {
        Font f;
        Object fs;
        FontFamily familyValue;
        String vAlign;
        ss = this.getStyleSheet(ss);
        int size = CSS.getFontSize(a, defaultSize, ss);
        StringValue vAlignV = (StringValue)a.getAttribute(Attribute.VERTICAL_ALIGN);
        if (vAlignV != null && ((vAlign = vAlignV.toString()).contains("sup") || vAlign.contains("sub"))) {
            size -= 2;
        }
        String family = (familyValue = (FontFamily)a.getAttribute(Attribute.FONT_FAMILY)) != null ? familyValue.getValue() : "SansSerif";
        int style = 0;
        FontWeight weightValue = (FontWeight)a.getAttribute(Attribute.FONT_WEIGHT);
        if (weightValue != null && weightValue.getValue() > 400) {
            style |= 1;
        }
        if ((fs = a.getAttribute(Attribute.FONT_STYLE)) != null && fs.toString().contains("italic")) {
            style |= 2;
        }
        if (family.equalsIgnoreCase("monospace")) {
            family = "Monospaced";
        }
        if ((f = sc.getFont(family, style, size)) == null || f.getFamily().equals("Dialog") && !family.equalsIgnoreCase("Dialog")) {
            family = "SansSerif";
            f = sc.getFont(family, style, size);
        }
        return f;
    }

    static int getFontSize(AttributeSet attr, int defaultSize, StyleSheet ss) {
        FontSize sizeValue = (FontSize)attr.getAttribute(Attribute.FONT_SIZE);
        return sizeValue != null ? sizeValue.getValue(attr, ss) : defaultSize;
    }

    Color getColor(AttributeSet a, Attribute key) {
        ColorValue cv = (ColorValue)a.getAttribute(key);
        if (cv != null) {
            return cv.getValue();
        }
        return null;
    }

    float getPointSize(String size, StyleSheet ss) {
        ss = this.getStyleSheet(ss);
        if (size != null) {
            if (size.startsWith("+")) {
                int relSize = Integer.parseInt(size.substring(1));
                return this.getPointSize(this.baseFontSize + relSize, ss);
            }
            if (size.startsWith("-")) {
                int relSize = -Integer.parseInt(size.substring(1));
                return this.getPointSize(this.baseFontSize + relSize, ss);
            }
            int absSize = Integer.parseInt(size);
            return this.getPointSize(absSize, ss);
        }
        return 0.0f;
    }

    float getLength(AttributeSet a, Attribute key, StyleSheet ss) {
        ss = this.getStyleSheet(ss);
        LengthValue lv = (LengthValue)a.getAttribute(key);
        boolean isW3CLengthUnits = ss == null ? false : ss.isW3CLengthUnits();
        float len = lv != null ? lv.getValue(isW3CLengthUnits) : 0.0f;
        return len;
    }

    AttributeSet translateHTMLToCSS(AttributeSet htmlAttrSet) {
        SimpleAttributeSet cssAttrSet = new SimpleAttributeSet();
        Element elem = (Element)((Object)htmlAttrSet);
        HTML.Tag tag = this.getHTMLTag(htmlAttrSet);
        if (tag == HTML.Tag.TD || tag == HTML.Tag.TH) {
            String pad;
            AttributeSet tableAttr = elem.getParentElement().getParentElement().getAttributes();
            int borderWidth = CSS.getTableBorder(tableAttr);
            if (borderWidth > 0) {
                this.translateAttribute(HTML.Attribute.BORDER, "1", cssAttrSet);
            }
            if ((pad = (String)tableAttr.getAttribute(HTML.Attribute.CELLPADDING)) != null) {
                LengthValue v = (LengthValue)this.getInternalCSSValue(Attribute.PADDING_TOP, pad);
                v.span = v.span < 0.0f ? 0.0f : v.span;
                cssAttrSet.addAttribute(Attribute.PADDING_TOP, v);
                cssAttrSet.addAttribute(Attribute.PADDING_BOTTOM, v);
                cssAttrSet.addAttribute(Attribute.PADDING_LEFT, v);
                cssAttrSet.addAttribute(Attribute.PADDING_RIGHT, v);
            }
        }
        if (elem.isLeaf()) {
            this.translateEmbeddedAttributes(htmlAttrSet, cssAttrSet);
        } else {
            this.translateAttributes(tag, htmlAttrSet, cssAttrSet);
        }
        if (tag == HTML.Tag.CAPTION) {
            Object v = htmlAttrSet.getAttribute(HTML.Attribute.ALIGN);
            if (v != null && (v.equals("top") || v.equals("bottom"))) {
                cssAttrSet.addAttribute(Attribute.CAPTION_SIDE, v);
                cssAttrSet.removeAttribute(Attribute.TEXT_ALIGN);
            } else {
                v = htmlAttrSet.getAttribute(HTML.Attribute.VALIGN);
                if (v != null) {
                    cssAttrSet.addAttribute(Attribute.CAPTION_SIDE, v);
                }
            }
        }
        return cssAttrSet;
    }

    private static int getTableBorder(AttributeSet tableAttr) {
        String borderValue = (String)tableAttr.getAttribute(HTML.Attribute.BORDER);
        if (borderValue == "#DEFAULT" || "".equals(borderValue)) {
            return 1;
        }
        try {
            return Integer.parseInt(borderValue);
        }
        catch (NumberFormatException e) {
            return 0;
        }
    }

    public static Attribute[] getAllAttributeKeys() {
        Attribute[] keys = new Attribute[Attribute.allAttributes.length];
        System.arraycopy(Attribute.allAttributes, 0, keys, 0, Attribute.allAttributes.length);
        return keys;
    }

    public static final Attribute getAttribute(String name) {
        return attributeMap.get(name);
    }

    static final Value getValue(String name) {
        return valueMap.get(name);
    }

    static URL getURL(URL base, String cssString) {
        URL url2;
        if (cssString == null) {
            return null;
        }
        if (cssString.startsWith("url(") && cssString.endsWith(")")) {
            cssString = cssString.substring(4, cssString.length() - 1);
        }
        try {
            url2 = new URL(cssString);
            if (url2 != null) {
                return url2;
            }
        }
        catch (MalformedURLException url2) {
            // empty catch block
        }
        if (base != null) {
            try {
                url2 = new URL(base, cssString);
                return url2;
            }
            catch (MalformedURLException malformedURLException) {
                // empty catch block
            }
        }
        return null;
    }

    static String colorToHex(Color color) {
        Object colorstr = "#";
        String str = Integer.toHexString(color.getRed());
        if (str.length() > 2) {
            str = str.substring(0, 2);
        } else {
            colorstr = str.length() < 2 ? (String)colorstr + "0" + str : (String)colorstr + str;
        }
        str = Integer.toHexString(color.getGreen());
        if (str.length() > 2) {
            str = str.substring(0, 2);
        } else {
            colorstr = str.length() < 2 ? (String)colorstr + "0" + str : (String)colorstr + str;
        }
        str = Integer.toHexString(color.getBlue());
        if (str.length() > 2) {
            str = str.substring(0, 2);
        } else {
            colorstr = str.length() < 2 ? (String)colorstr + "0" + str : (String)colorstr + str;
        }
        return colorstr;
    }

    static final Color hexToColor(String value) {
        Color c;
        int n = value.length();
        String digits = value.startsWith("#") ? value.substring(1, Math.min(value.length(), 7)) : value;
        if (digits.length() == 3) {
            String r = digits.substring(0, 1);
            String g = digits.substring(1, 2);
            String b = digits.substring(2, 3);
            digits = String.format("%s%s%s%s%s%s", r, r, g, g, b, b);
        }
        String hstr = "0x" + digits;
        try {
            c = Color.decode(hstr);
        }
        catch (NumberFormatException nfe) {
            c = null;
        }
        return c;
    }

    static Color stringToColor(String str) {
        if (str == null) {
            return null;
        }
        Color color = str.length() == 0 ? Color.black : (str.startsWith("rgb(") ? CSS.parseRGB(str) : (str.startsWith("rgba(") ? CSS.parseRGBA(str) : (str.charAt(0) == '#' ? CSS.hexToColor(str) : (str.equalsIgnoreCase("Black") ? CSS.hexToColor("#000000") : (str.equalsIgnoreCase("Silver") ? CSS.hexToColor("#C0C0C0") : (str.equalsIgnoreCase("Gray") ? CSS.hexToColor("#808080") : (str.equalsIgnoreCase("White") ? CSS.hexToColor("#FFFFFF") : (str.equalsIgnoreCase("Maroon") ? CSS.hexToColor("#800000") : (str.equalsIgnoreCase("Red") ? CSS.hexToColor("#FF0000") : (str.equalsIgnoreCase("Purple") ? CSS.hexToColor("#800080") : (str.equalsIgnoreCase("Fuchsia") ? CSS.hexToColor("#FF00FF") : (str.equalsIgnoreCase("Green") ? CSS.hexToColor("#008000") : (str.equalsIgnoreCase("Lime") ? CSS.hexToColor("#00FF00") : (str.equalsIgnoreCase("Olive") ? CSS.hexToColor("#808000") : (str.equalsIgnoreCase("Yellow") ? CSS.hexToColor("#FFFF00") : (str.equalsIgnoreCase("Navy") ? CSS.hexToColor("#000080") : (str.equalsIgnoreCase("Blue") ? CSS.hexToColor("#0000FF") : (str.equalsIgnoreCase("Teal") ? CSS.hexToColor("#008080") : (str.equalsIgnoreCase("Aqua") ? CSS.hexToColor("#00FFFF") : (str.equalsIgnoreCase("Orange") ? CSS.hexToColor("#FF8000") : CSS.hexToColor(str)))))))))))))))))))));
        return color;
    }

    private static Color parseRGB(String string) {
        int[] index = new int[]{4};
        int red = (int)CSS.getColorComponent(string, index);
        int green = (int)CSS.getColorComponent(string, index);
        int blue = (int)CSS.getColorComponent(string, index);
        return new Color(red, green, blue);
    }

    private static Color parseRGBA(String string) {
        int[] index = new int[]{4};
        float red = CSS.getColorComponent(string, index) / 255.0f;
        float green = CSS.getColorComponent(string, index) / 255.0f;
        float blue = CSS.getColorComponent(string, index) / 255.0f;
        float alpha = CSS.getColorComponent(string, index);
        return new Color(red, green, blue, alpha);
    }

    private static float getColorComponent(String string, int[] index) {
        char aChar;
        int length = string.length();
        while (index[0] < length && (aChar = string.charAt(index[0])) != '-' && !Character.isDigit(aChar) && aChar != '.') {
            index[0] = index[0] + 1;
        }
        int start = index[0];
        if (start < length && string.charAt(index[0]) == '-') {
            index[0] = index[0] + 1;
        }
        while (index[0] < length && Character.isDigit(string.charAt(index[0]))) {
            index[0] = index[0] + 1;
        }
        if (index[0] < length && string.charAt(index[0]) == '.') {
            index[0] = index[0] + 1;
            while (index[0] < length && Character.isDigit(string.charAt(index[0]))) {
                index[0] = index[0] + 1;
            }
        }
        if (start != index[0]) {
            try {
                float value = Float.parseFloat(string.substring(start, index[0]));
                if (index[0] < length && string.charAt(index[0]) == '%') {
                    index[0] = index[0] + 1;
                    value = value * 255.0f / 100.0f;
                }
                return Math.min(255.0f, Math.max(0.0f, value));
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return 0.0f;
    }

    static int getIndexOfSize(float pt, int[] sizeMap) {
        for (int i = 0; i < sizeMap.length; ++i) {
            if (!(pt <= (float)sizeMap[i])) continue;
            return i + 1;
        }
        return sizeMap.length;
    }

    static int getIndexOfSize(float pt, StyleSheet ss) {
        int[] sizeMap = ss != null ? ss.getSizeMap() : StyleSheet.sizeMapDefault;
        return CSS.getIndexOfSize(pt, sizeMap);
    }

    static String[] parseStrings(String value) {
        int length = value == null ? 0 : value.length();
        ArrayList<String> temp = new ArrayList<String>(4);
        for (int current = 0; current < length; ++current) {
            char ch;
            while (current < length && Character.isWhitespace(value.charAt(current))) {
                ++current;
            }
            int last = current;
            int inParentheses = 0;
            while (!(current >= length || Character.isWhitespace(ch = value.charAt(current)) && inParentheses <= 0)) {
                if (ch == '(') {
                    ++inParentheses;
                } else if (ch == ')') {
                    --inParentheses;
                }
                ++current;
            }
            if (last == current) continue;
            temp.add(value.substring(last, current));
        }
        String[] retValue = temp.toArray(new String[0]);
        return retValue;
    }

    float getPointSize(int index, StyleSheet ss) {
        int[] sizeMap;
        int[] nArray = sizeMap = (ss = this.getStyleSheet(ss)) != null ? ss.getSizeMap() : StyleSheet.sizeMapDefault;
        if (--index < 0) {
            return sizeMap[0];
        }
        if (index > sizeMap.length - 1) {
            return sizeMap[sizeMap.length - 1];
        }
        return sizeMap[index];
    }

    private void translateEmbeddedAttributes(AttributeSet htmlAttrSet, MutableAttributeSet cssAttrSet) {
        Enumeration<?> keys = htmlAttrSet.getAttributeNames();
        if (htmlAttrSet.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.HR) {
            this.translateAttributes(HTML.Tag.HR, htmlAttrSet, cssAttrSet);
        }
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (key instanceof HTML.Tag) {
                HTML.Tag tag = (HTML.Tag)key;
                Object o = htmlAttrSet.getAttribute(tag);
                if (!(o instanceof AttributeSet)) continue;
                AttributeSet as = (AttributeSet)o;
                this.translateAttributes(tag, as, cssAttrSet);
                continue;
            }
            if (!(key instanceof Attribute)) continue;
            cssAttrSet.addAttribute(key, htmlAttrSet.getAttribute(key));
        }
    }

    private void translateAttributes(HTML.Tag tag, AttributeSet htmlAttrSet, MutableAttributeSet cssAttrSet) {
        Enumeration<?> names = htmlAttrSet.getAttributeNames();
        while (names.hasMoreElements()) {
            Object name = names.nextElement();
            if (name instanceof HTML.Attribute) {
                HTML.Attribute key = (HTML.Attribute)name;
                if (key == HTML.Attribute.ALIGN) {
                    Object o;
                    Attribute cssAttr;
                    String htmlAttrValue = (String)htmlAttrSet.getAttribute(HTML.Attribute.ALIGN);
                    if (htmlAttrValue == null || (cssAttr = this.getCssAlignAttribute(tag, htmlAttrSet)) == null || (o = this.getCssValue(cssAttr, htmlAttrValue)) == null) continue;
                    cssAttrSet.addAttribute(cssAttr, o);
                    continue;
                }
                if (key == HTML.Attribute.SIZE && !this.isHTMLFontTag(tag)) continue;
                if (tag == HTML.Tag.TABLE && key == HTML.Attribute.BORDER) {
                    int borderWidth = CSS.getTableBorder(htmlAttrSet);
                    if (borderWidth <= 0) continue;
                    this.translateAttribute(HTML.Attribute.BORDER, Integer.toString(borderWidth), cssAttrSet);
                    continue;
                }
                this.translateAttribute(key, (String)htmlAttrSet.getAttribute(key), cssAttrSet);
                continue;
            }
            if (!(name instanceof Attribute)) continue;
            cssAttrSet.addAttribute(name, htmlAttrSet.getAttribute(name));
        }
    }

    private void translateAttribute(HTML.Attribute key, String htmlAttrValue, MutableAttributeSet cssAttrSet) {
        Attribute[] cssAttrList = this.getCssAttribute(key);
        if (cssAttrList == null || htmlAttrValue == null) {
            return;
        }
        for (Attribute cssAttr : cssAttrList) {
            Object o = this.getCssValue(cssAttr, htmlAttrValue);
            if (o == null) continue;
            cssAttrSet.addAttribute(cssAttr, o);
        }
    }

    Object getCssValue(Attribute cssAttr, String htmlAttrValue) {
        CssValue value = (CssValue)this.valueConvertor.get(cssAttr);
        Object o = value.parseHtmlValue(htmlAttrValue);
        return o;
    }

    private Attribute[] getCssAttribute(HTML.Attribute hAttr) {
        return htmlAttrToCssAttrMap.get(hAttr);
    }

    private Attribute getCssAlignAttribute(HTML.Tag tag, AttributeSet htmlAttrSet) {
        return Attribute.TEXT_ALIGN;
    }

    private HTML.Tag getHTMLTag(AttributeSet htmlAttrSet) {
        Object o = htmlAttrSet.getAttribute(StyleConstants.NameAttribute);
        if (o instanceof HTML.Tag) {
            HTML.Tag tag = (HTML.Tag)o;
            return tag;
        }
        return null;
    }

    private boolean isHTMLFontTag(HTML.Tag tag) {
        return tag != null && (tag == HTML.Tag.FONT || tag == HTML.Tag.BASEFONT);
    }

    private boolean isFloater(String alignValue) {
        return alignValue.equals("left") || alignValue.equals("right");
    }

    private boolean validTextAlignValue(String alignValue) {
        return this.isFloater(alignValue) || alignValue.equals("center");
    }

    static SizeRequirements calculateTiledRequirements(LayoutIterator iter, SizeRequirements r) {
        long minimum = 0L;
        long maximum = 0L;
        long preferred = 0L;
        int lastMargin = 0;
        int totalSpacing = 0;
        int n = iter.getCount();
        for (int i = 0; i < n; ++i) {
            iter.setIndex(i);
            int margin0 = lastMargin;
            int margin1 = (int)iter.getLeadingCollapseSpan();
            totalSpacing += Math.max(margin0, margin1);
            preferred += (long)((int)iter.getPreferredSpan(0.0f));
            minimum = (long)((float)minimum + iter.getMinimumSpan(0.0f));
            maximum = (long)((float)maximum + iter.getMaximumSpan(0.0f));
            lastMargin = (int)iter.getTrailingCollapseSpan();
        }
        totalSpacing += lastMargin;
        totalSpacing = (int)((float)totalSpacing + 2.0f * iter.getBorderWidth());
        minimum += (long)totalSpacing;
        preferred += (long)totalSpacing;
        maximum += (long)totalSpacing;
        if (r == null) {
            r = new SizeRequirements();
        }
        r.minimum = minimum > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)minimum;
        r.preferred = preferred > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)preferred;
        r.maximum = maximum > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)maximum;
        return r;
    }

    static void calculateTiledLayout(LayoutIterator iter, int targetSpan) {
        int adjustmentLevel;
        int i;
        long preferred = 0L;
        int lastMargin = 0;
        int totalSpacing = 0;
        int n = iter.getCount();
        int adjustmentWeightsCount = 3;
        long[] gain = new long[adjustmentWeightsCount];
        long[] loss = new long[adjustmentWeightsCount];
        for (i = 0; i < adjustmentWeightsCount; ++i) {
            loss[i] = 0L;
            gain[i] = 0L;
        }
        for (i = 0; i < n; ++i) {
            iter.setIndex(i);
            int margin0 = lastMargin;
            int margin1 = (int)iter.getLeadingCollapseSpan();
            iter.setOffset(Math.max(margin0, margin1));
            totalSpacing += iter.getOffset();
            long currentPreferred = (long)iter.getPreferredSpan(targetSpan);
            iter.setSpan((int)currentPreferred);
            preferred += currentPreferred;
            int n2 = iter.getAdjustmentWeight();
            gain[n2] = gain[n2] + ((long)iter.getMaximumSpan(targetSpan) - currentPreferred);
            int n3 = iter.getAdjustmentWeight();
            loss[n3] = loss[n3] + (currentPreferred - (long)iter.getMinimumSpan(targetSpan));
            lastMargin = (int)iter.getTrailingCollapseSpan();
        }
        totalSpacing += lastMargin;
        totalSpacing = (int)((float)totalSpacing + 2.0f * iter.getBorderWidth());
        for (i = 1; i < adjustmentWeightsCount; ++i) {
            int n4 = i;
            gain[n4] = gain[n4] + gain[i - 1];
            int n5 = i;
            loss[n5] = loss[n5] + loss[i - 1];
        }
        int allocated = targetSpan - totalSpacing;
        long desiredAdjustment = (long)allocated - preferred;
        long[] adjustmentsArray = desiredAdjustment > 0L ? gain : loss;
        desiredAdjustment = Math.abs(desiredAdjustment);
        for (adjustmentLevel = 0; adjustmentLevel <= 2 && adjustmentsArray[adjustmentLevel] < desiredAdjustment; ++adjustmentLevel) {
        }
        float adjustmentFactor = 0.0f;
        if (adjustmentLevel <= 2 && (desiredAdjustment -= adjustmentLevel > 0 ? adjustmentsArray[adjustmentLevel - 1] : 0L) != 0L) {
            float maximumAdjustment = adjustmentsArray[adjustmentLevel] - (adjustmentLevel > 0 ? adjustmentsArray[adjustmentLevel - 1] : 0L);
            adjustmentFactor = (float)desiredAdjustment / maximumAdjustment;
        }
        int totalOffset = (int)iter.getBorderWidth();
        for (int i2 = 0; i2 < n; ++i2) {
            iter.setIndex(i2);
            iter.setOffset(iter.getOffset() + totalOffset);
            if (iter.getAdjustmentWeight() < adjustmentLevel) {
                iter.setSpan((int)((long)allocated > preferred ? Math.floor(iter.getMaximumSpan(targetSpan)) : Math.ceil(iter.getMinimumSpan(targetSpan))));
            } else if (iter.getAdjustmentWeight() == adjustmentLevel) {
                int availableSpan = (long)allocated > preferred ? (int)iter.getMaximumSpan(targetSpan) - iter.getSpan() : iter.getSpan() - (int)iter.getMinimumSpan(targetSpan);
                int adj = (int)Math.floor(adjustmentFactor * (float)availableSpan);
                iter.setSpan(iter.getSpan() + ((long)allocated > preferred ? adj : -adj));
            }
            totalOffset = (int)Math.min((long)iter.getOffset() + (long)iter.getSpan(), Integer.MAX_VALUE);
        }
        int roundError = targetSpan - totalOffset - (int)iter.getTrailingCollapseSpan() - (int)iter.getBorderWidth();
        int adj = roundError > 0 ? 1 : -1;
        roundError *= adj;
        boolean canAdjust = true;
        while (roundError > 0 && canAdjust) {
            canAdjust = false;
            int offsetAdjust = 0;
            for (int i3 = 0; i3 < n; ++i3) {
                int boundGap;
                iter.setIndex(i3);
                iter.setOffset(iter.getOffset() + offsetAdjust);
                int curSpan = iter.getSpan();
                if (roundError <= 0) continue;
                int n6 = boundGap = adj > 0 ? (int)Math.floor(iter.getMaximumSpan(targetSpan)) - curSpan : curSpan - (int)Math.ceil(iter.getMinimumSpan(targetSpan));
                if (boundGap < 1) continue;
                canAdjust = true;
                iter.setSpan(curSpan + adj);
                offsetAdjust += adj;
                --roundError;
            }
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        Enumeration<Object> keys = this.valueConvertor.keys();
        s.writeInt(this.valueConvertor.size());
        if (keys != null) {
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = this.valueConvertor.get(key);
                if (!(key instanceof Serializable) && (key = StyleContext.getStaticAttributeKey(key)) == null) {
                    key = null;
                    value = null;
                } else if (!(value instanceof Serializable) && (value = StyleContext.getStaticAttributeKey(value)) == null) {
                    key = null;
                    value = null;
                }
                s.writeObject(key);
                s.writeObject(value);
            }
        }
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        ObjectInputStream.GetField f = s.readFields();
        int newBaseFontSize = f.get("baseFontSize", 0);
        this.setBaseFontSize(newBaseFontSize);
        int numValues = s.readInt();
        this.valueConvertor = new Hashtable();
        while (numValues-- > 0) {
            Object staticValue;
            Object key = s.readObject();
            Object value = s.readObject();
            Object staticKey = StyleContext.getStaticAttribute(key);
            if (staticKey != null) {
                key = staticKey;
            }
            if ((staticValue = StyleContext.getStaticAttribute(value)) != null) {
                value = staticValue;
            }
            if (key == null || value == null) continue;
            this.valueConvertor.put(key, value);
        }
    }

    private StyleSheet getStyleSheet(StyleSheet ss) {
        if (ss != null) {
            this.styleSheet = ss;
        }
        return this.styleSheet;
    }

    static {
        int i;
        attributeMap = new Hashtable();
        valueMap = new Hashtable();
        htmlAttrToCssAttrMap = new Hashtable(20);
        styleConstantToCssMap = new Hashtable(17);
        htmlValueToCssValueMap = new Hashtable(8);
        cssValueToInternalValueMap = new Hashtable(13);
        for (i = 0; i < Attribute.allAttributes.length; ++i) {
            attributeMap.put(Attribute.allAttributes[i].toString(), Attribute.allAttributes[i]);
        }
        for (i = 0; i < Value.allValues.length; ++i) {
            valueMap.put(Value.allValues[i].toString(), Value.allValues[i]);
        }
        htmlAttrToCssAttrMap.put(HTML.Attribute.COLOR, new Attribute[]{Attribute.COLOR});
        htmlAttrToCssAttrMap.put(HTML.Attribute.TEXT, new Attribute[]{Attribute.COLOR});
        htmlAttrToCssAttrMap.put(HTML.Attribute.CLEAR, new Attribute[]{Attribute.CLEAR});
        htmlAttrToCssAttrMap.put(HTML.Attribute.BACKGROUND, new Attribute[]{Attribute.BACKGROUND_IMAGE});
        htmlAttrToCssAttrMap.put(HTML.Attribute.BGCOLOR, new Attribute[]{Attribute.BACKGROUND_COLOR});
        htmlAttrToCssAttrMap.put(HTML.Attribute.WIDTH, new Attribute[]{Attribute.WIDTH});
        htmlAttrToCssAttrMap.put(HTML.Attribute.HEIGHT, new Attribute[]{Attribute.HEIGHT});
        htmlAttrToCssAttrMap.put(HTML.Attribute.BORDER, new Attribute[]{Attribute.BORDER_TOP_WIDTH, Attribute.BORDER_RIGHT_WIDTH, Attribute.BORDER_BOTTOM_WIDTH, Attribute.BORDER_LEFT_WIDTH});
        htmlAttrToCssAttrMap.put(HTML.Attribute.CELLPADDING, new Attribute[]{Attribute.PADDING});
        htmlAttrToCssAttrMap.put(HTML.Attribute.CELLSPACING, new Attribute[]{Attribute.BORDER_SPACING});
        htmlAttrToCssAttrMap.put(HTML.Attribute.MARGINWIDTH, new Attribute[]{Attribute.MARGIN_LEFT, Attribute.MARGIN_RIGHT});
        htmlAttrToCssAttrMap.put(HTML.Attribute.MARGINHEIGHT, new Attribute[]{Attribute.MARGIN_TOP, Attribute.MARGIN_BOTTOM});
        htmlAttrToCssAttrMap.put(HTML.Attribute.HSPACE, new Attribute[]{Attribute.PADDING_LEFT, Attribute.PADDING_RIGHT});
        htmlAttrToCssAttrMap.put(HTML.Attribute.VSPACE, new Attribute[]{Attribute.PADDING_BOTTOM, Attribute.PADDING_TOP});
        htmlAttrToCssAttrMap.put(HTML.Attribute.FACE, new Attribute[]{Attribute.FONT_FAMILY});
        htmlAttrToCssAttrMap.put(HTML.Attribute.SIZE, new Attribute[]{Attribute.FONT_SIZE});
        htmlAttrToCssAttrMap.put(HTML.Attribute.VALIGN, new Attribute[]{Attribute.VERTICAL_ALIGN});
        htmlAttrToCssAttrMap.put(HTML.Attribute.ALIGN, new Attribute[]{Attribute.VERTICAL_ALIGN, Attribute.TEXT_ALIGN, Attribute.FLOAT});
        htmlAttrToCssAttrMap.put(HTML.Attribute.TYPE, new Attribute[]{Attribute.LIST_STYLE_TYPE});
        htmlAttrToCssAttrMap.put(HTML.Attribute.NOWRAP, new Attribute[]{Attribute.WHITE_SPACE});
        styleConstantToCssMap.put(StyleConstants.FontFamily, Attribute.FONT_FAMILY);
        styleConstantToCssMap.put(StyleConstants.FontSize, Attribute.FONT_SIZE);
        styleConstantToCssMap.put(StyleConstants.Bold, Attribute.FONT_WEIGHT);
        styleConstantToCssMap.put(StyleConstants.Italic, Attribute.FONT_STYLE);
        styleConstantToCssMap.put(StyleConstants.Underline, Attribute.TEXT_DECORATION);
        styleConstantToCssMap.put(StyleConstants.StrikeThrough, Attribute.TEXT_DECORATION);
        styleConstantToCssMap.put(StyleConstants.Superscript, Attribute.VERTICAL_ALIGN);
        styleConstantToCssMap.put(StyleConstants.Subscript, Attribute.VERTICAL_ALIGN);
        styleConstantToCssMap.put(StyleConstants.Foreground, Attribute.COLOR);
        styleConstantToCssMap.put(StyleConstants.Background, Attribute.BACKGROUND_COLOR);
        styleConstantToCssMap.put(StyleConstants.FirstLineIndent, Attribute.TEXT_INDENT);
        styleConstantToCssMap.put(StyleConstants.LeftIndent, Attribute.MARGIN_LEFT);
        styleConstantToCssMap.put(StyleConstants.RightIndent, Attribute.MARGIN_RIGHT);
        styleConstantToCssMap.put(StyleConstants.SpaceAbove, Attribute.MARGIN_TOP);
        styleConstantToCssMap.put(StyleConstants.SpaceBelow, Attribute.MARGIN_BOTTOM);
        styleConstantToCssMap.put(StyleConstants.Alignment, Attribute.TEXT_ALIGN);
        htmlValueToCssValueMap.put("disc", Value.DISC);
        htmlValueToCssValueMap.put("square", Value.SQUARE);
        htmlValueToCssValueMap.put("circle", Value.CIRCLE);
        htmlValueToCssValueMap.put("1", Value.DECIMAL);
        htmlValueToCssValueMap.put("a", Value.LOWER_ALPHA);
        htmlValueToCssValueMap.put("A", Value.UPPER_ALPHA);
        htmlValueToCssValueMap.put("i", Value.LOWER_ROMAN);
        htmlValueToCssValueMap.put("I", Value.UPPER_ROMAN);
        cssValueToInternalValueMap.put("none", Value.NONE);
        cssValueToInternalValueMap.put("disc", Value.DISC);
        cssValueToInternalValueMap.put("square", Value.SQUARE);
        cssValueToInternalValueMap.put("circle", Value.CIRCLE);
        cssValueToInternalValueMap.put("decimal", Value.DECIMAL);
        cssValueToInternalValueMap.put("lower-roman", Value.LOWER_ROMAN);
        cssValueToInternalValueMap.put("upper-roman", Value.UPPER_ROMAN);
        cssValueToInternalValueMap.put("lower-alpha", Value.LOWER_ALPHA);
        cssValueToInternalValueMap.put("upper-alpha", Value.UPPER_ALPHA);
        cssValueToInternalValueMap.put("repeat", Value.BACKGROUND_REPEAT);
        cssValueToInternalValueMap.put("no-repeat", Value.BACKGROUND_NO_REPEAT);
        cssValueToInternalValueMap.put("repeat-x", Value.BACKGROUND_REPEAT_X);
        cssValueToInternalValueMap.put("repeat-y", Value.BACKGROUND_REPEAT_Y);
        cssValueToInternalValueMap.put("scroll", Value.BACKGROUND_SCROLL);
        cssValueToInternalValueMap.put("fixed", Value.BACKGROUND_FIXED);
        Object[] keys = Attribute.allAttributes;
        try {
            for (Object key : keys) {
                StyleContext.registerStaticAttributeKey(key);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        keys = Value.allValues;
        try {
            for (Object key : keys) {
                StyleContext.registerStaticAttributeKey(key);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        baseFontSizeIndex = 3;
    }

    public static final class Attribute {
        private String name;
        private String defaultValue;
        private boolean inherited;
        public static final Attribute BACKGROUND = new Attribute("background", null, false);
        public static final Attribute BACKGROUND_ATTACHMENT = new Attribute("background-attachment", "scroll", false);
        public static final Attribute BACKGROUND_COLOR = new Attribute("background-color", "transparent", false);
        public static final Attribute BACKGROUND_IMAGE = new Attribute("background-image", "none", false);
        public static final Attribute BACKGROUND_POSITION = new Attribute("background-position", "0% 0%", false);
        public static final Attribute BACKGROUND_REPEAT = new Attribute("background-repeat", "repeat", false);
        public static final Attribute BORDER = new Attribute("border", null, false);
        public static final Attribute BORDER_BOTTOM = new Attribute("border-bottom", null, false);
        public static final Attribute BORDER_BOTTOM_COLOR = new Attribute("border-bottom-color", null, false);
        public static final Attribute BORDER_BOTTOM_STYLE = new Attribute("border-bottom-style", "none", false);
        public static final Attribute BORDER_BOTTOM_WIDTH = new Attribute("border-bottom-width", "medium", false);
        public static final Attribute BORDER_COLOR = new Attribute("border-color", null, false);
        public static final Attribute BORDER_LEFT = new Attribute("border-left", null, false);
        public static final Attribute BORDER_LEFT_COLOR = new Attribute("border-left-color", null, false);
        public static final Attribute BORDER_LEFT_STYLE = new Attribute("border-left-style", "none", false);
        public static final Attribute BORDER_LEFT_WIDTH = new Attribute("border-left-width", "medium", false);
        public static final Attribute BORDER_RIGHT = new Attribute("border-right", null, false);
        public static final Attribute BORDER_RIGHT_COLOR = new Attribute("border-right-color", null, false);
        public static final Attribute BORDER_RIGHT_STYLE = new Attribute("border-right-style", "none", false);
        public static final Attribute BORDER_RIGHT_WIDTH = new Attribute("border-right-width", "medium", false);
        public static final Attribute BORDER_STYLE = new Attribute("border-style", "none", false);
        public static final Attribute BORDER_TOP = new Attribute("border-top", null, false);
        public static final Attribute BORDER_TOP_COLOR = new Attribute("border-top-color", null, false);
        public static final Attribute BORDER_TOP_STYLE = new Attribute("border-top-style", "none", false);
        public static final Attribute BORDER_TOP_WIDTH = new Attribute("border-top-width", "medium", false);
        public static final Attribute BORDER_WIDTH = new Attribute("border-width", "medium", false);
        public static final Attribute CLEAR = new Attribute("clear", "none", false);
        public static final Attribute COLOR = new Attribute("color", "black", true);
        public static final Attribute DISPLAY = new Attribute("display", "block", false);
        public static final Attribute FLOAT = new Attribute("float", "none", false);
        public static final Attribute FONT = new Attribute("font", null, true);
        public static final Attribute FONT_FAMILY = new Attribute("font-family", null, true);
        public static final Attribute FONT_SIZE = new Attribute("font-size", "medium", true);
        public static final Attribute FONT_STYLE = new Attribute("font-style", "normal", true);
        public static final Attribute FONT_VARIANT = new Attribute("font-variant", "normal", true);
        public static final Attribute FONT_WEIGHT = new Attribute("font-weight", "normal", true);
        public static final Attribute HEIGHT = new Attribute("height", "auto", false);
        public static final Attribute LETTER_SPACING = new Attribute("letter-spacing", "normal", true);
        public static final Attribute LINE_HEIGHT = new Attribute("line-height", "normal", true);
        public static final Attribute LIST_STYLE = new Attribute("list-style", null, true);
        public static final Attribute LIST_STYLE_IMAGE = new Attribute("list-style-image", "none", true);
        public static final Attribute LIST_STYLE_POSITION = new Attribute("list-style-position", "outside", true);
        public static final Attribute LIST_STYLE_TYPE = new Attribute("list-style-type", "disc", true);
        public static final Attribute MARGIN = new Attribute("margin", null, false);
        public static final Attribute MARGIN_BOTTOM = new Attribute("margin-bottom", "0", false);
        public static final Attribute MARGIN_LEFT = new Attribute("margin-left", "0", false);
        public static final Attribute MARGIN_RIGHT = new Attribute("margin-right", "0", false);
        static final Attribute MARGIN_LEFT_LTR = new Attribute("margin-left-ltr", Integer.toString(Integer.MIN_VALUE), false);
        static final Attribute MARGIN_LEFT_RTL = new Attribute("margin-left-rtl", Integer.toString(Integer.MIN_VALUE), false);
        static final Attribute MARGIN_RIGHT_LTR = new Attribute("margin-right-ltr", Integer.toString(Integer.MIN_VALUE), false);
        static final Attribute MARGIN_RIGHT_RTL = new Attribute("margin-right-rtl", Integer.toString(Integer.MIN_VALUE), false);
        public static final Attribute MARGIN_TOP = new Attribute("margin-top", "0", false);
        public static final Attribute PADDING = new Attribute("padding", null, false);
        public static final Attribute PADDING_BOTTOM = new Attribute("padding-bottom", "0", false);
        public static final Attribute PADDING_LEFT = new Attribute("padding-left", "0", false);
        public static final Attribute PADDING_RIGHT = new Attribute("padding-right", "0", false);
        public static final Attribute PADDING_TOP = new Attribute("padding-top", "0", false);
        public static final Attribute TEXT_ALIGN = new Attribute("text-align", null, true);
        public static final Attribute TEXT_DECORATION = new Attribute("text-decoration", "none", true);
        public static final Attribute TEXT_INDENT = new Attribute("text-indent", "0", true);
        public static final Attribute TEXT_TRANSFORM = new Attribute("text-transform", "none", true);
        public static final Attribute VERTICAL_ALIGN = new Attribute("vertical-align", "baseline", false);
        public static final Attribute WORD_SPACING = new Attribute("word-spacing", "normal", true);
        public static final Attribute WHITE_SPACE = new Attribute("white-space", "normal", true);
        public static final Attribute WIDTH = new Attribute("width", "auto", false);
        static final Attribute BORDER_SPACING = new Attribute("border-spacing", "0", true);
        static final Attribute CAPTION_SIDE = new Attribute("caption-side", "left", true);
        static final Attribute[] allAttributes = new Attribute[]{BACKGROUND, BACKGROUND_ATTACHMENT, BACKGROUND_COLOR, BACKGROUND_IMAGE, BACKGROUND_POSITION, BACKGROUND_REPEAT, BORDER, BORDER_BOTTOM, BORDER_BOTTOM_WIDTH, BORDER_COLOR, BORDER_LEFT, BORDER_LEFT_WIDTH, BORDER_RIGHT, BORDER_RIGHT_WIDTH, BORDER_STYLE, BORDER_TOP, BORDER_TOP_WIDTH, BORDER_WIDTH, BORDER_TOP_STYLE, BORDER_RIGHT_STYLE, BORDER_BOTTOM_STYLE, BORDER_LEFT_STYLE, BORDER_TOP_COLOR, BORDER_RIGHT_COLOR, BORDER_BOTTOM_COLOR, BORDER_LEFT_COLOR, CLEAR, COLOR, DISPLAY, FLOAT, FONT, FONT_FAMILY, FONT_SIZE, FONT_STYLE, FONT_VARIANT, FONT_WEIGHT, HEIGHT, LETTER_SPACING, LINE_HEIGHT, LIST_STYLE, LIST_STYLE_IMAGE, LIST_STYLE_POSITION, LIST_STYLE_TYPE, MARGIN, MARGIN_BOTTOM, MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP, PADDING, PADDING_BOTTOM, PADDING_LEFT, PADDING_RIGHT, PADDING_TOP, TEXT_ALIGN, TEXT_DECORATION, TEXT_INDENT, TEXT_TRANSFORM, VERTICAL_ALIGN, WORD_SPACING, WHITE_SPACE, WIDTH, BORDER_SPACING, CAPTION_SIDE, MARGIN_LEFT_LTR, MARGIN_LEFT_RTL, MARGIN_RIGHT_LTR, MARGIN_RIGHT_RTL};
        private static final Attribute[] ALL_MARGINS = new Attribute[]{MARGIN_TOP, MARGIN_RIGHT, MARGIN_BOTTOM, MARGIN_LEFT};
        private static final Attribute[] ALL_PADDING = new Attribute[]{PADDING_TOP, PADDING_RIGHT, PADDING_BOTTOM, PADDING_LEFT};
        private static final Attribute[] ALL_BORDER_WIDTHS = new Attribute[]{BORDER_TOP_WIDTH, BORDER_RIGHT_WIDTH, BORDER_BOTTOM_WIDTH, BORDER_LEFT_WIDTH};
        private static final Attribute[] ALL_BORDER_STYLES = new Attribute[]{BORDER_TOP_STYLE, BORDER_RIGHT_STYLE, BORDER_BOTTOM_STYLE, BORDER_LEFT_STYLE};
        private static final Attribute[] ALL_BORDER_COLORS = new Attribute[]{BORDER_TOP_COLOR, BORDER_RIGHT_COLOR, BORDER_BOTTOM_COLOR, BORDER_LEFT_COLOR};

        private Attribute(String name, String defaultValue, boolean inherited) {
            this.name = name;
            this.defaultValue = defaultValue;
            this.inherited = inherited;
        }

        public String toString() {
            return this.name;
        }

        public String getDefaultValue() {
            return this.defaultValue;
        }

        public boolean isInherited() {
            return this.inherited;
        }
    }

    class FontSize
    extends CssValue {
        float value;
        boolean index;
        LengthUnit lu;

        FontSize() {
        }

        int getValue(AttributeSet a, StyleSheet ss) {
            AttributeSet resolveParent;
            ss = CSS.this.getStyleSheet(ss);
            if (this.index) {
                return Math.round(CSS.this.getPointSize((int)this.value, ss));
            }
            if (this.lu == null) {
                return Math.round(this.value);
            }
            if (this.lu.type == 0) {
                boolean isW3CLengthUnits = ss == null ? false : ss.isW3CLengthUnits();
                return Math.round(this.lu.getValue(isW3CLengthUnits));
            }
            if (a != null && (resolveParent = a.getResolveParent()) != null) {
                int pValue = StyleConstants.getFontSize(resolveParent);
                float retValue = this.lu.type == 1 || this.lu.type == 3 ? this.lu.value * (float)pValue : this.lu.value + (float)pValue;
                return Math.round(retValue);
            }
            return 12;
        }

        @Override
        Object parseCssValue(String value) {
            FontSize fs = new FontSize();
            fs.svalue = value;
            try {
                if (value.equals("xx-small")) {
                    fs.value = 1.0f;
                    fs.index = true;
                } else if (value.equals("x-small")) {
                    fs.value = 2.0f;
                    fs.index = true;
                } else if (value.equals("small")) {
                    fs.value = 3.0f;
                    fs.index = true;
                } else if (value.equals("medium")) {
                    fs.value = 4.0f;
                    fs.index = true;
                } else if (value.equals("large")) {
                    fs.value = 5.0f;
                    fs.index = true;
                } else if (value.equals("x-large")) {
                    fs.value = 6.0f;
                    fs.index = true;
                } else if (value.equals("xx-large")) {
                    fs.value = 7.0f;
                    fs.index = true;
                } else {
                    fs.lu = new LengthUnit(value, 1, 1.0f);
                }
            }
            catch (NumberFormatException nfe) {
                fs = null;
            }
            return fs;
        }

        @Override
        Object parseHtmlValue(String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            FontSize fs = new FontSize();
            fs.svalue = value;
            try {
                int baseFontSize = CSS.this.getBaseFontSize();
                if (value.charAt(0) == '+') {
                    int relSize = Integer.parseInt(value.substring(1));
                    fs.value = baseFontSize + relSize;
                    fs.index = true;
                } else if (value.charAt(0) == '-') {
                    int relSize = -Integer.parseInt(value.substring(1));
                    fs.value = baseFontSize + relSize;
                    fs.index = true;
                } else {
                    fs.value = Integer.parseInt(value);
                    if (fs.value > 7.0f) {
                        fs.value = 7.0f;
                    } else if (fs.value < 0.0f) {
                        fs.value = 0.0f;
                    }
                    fs.index = true;
                }
            }
            catch (NumberFormatException nfe) {
                fs = null;
            }
            return fs;
        }

        @Override
        Object fromStyleConstants(StyleConstants key, Object value) {
            if (value instanceof Number) {
                FontSize fs = new FontSize();
                fs.value = CSS.getIndexOfSize(((Number)value).floatValue(), StyleSheet.sizeMapDefault);
                fs.svalue = Integer.toString((int)fs.value);
                fs.index = true;
                return fs;
            }
            return this.parseCssValue(value.toString());
        }

        @Override
        Object toStyleConstants(StyleConstants key, View v) {
            if (v != null) {
                return this.getValue(v.getAttributes(), null);
            }
            return this.getValue(null, null);
        }
    }

    static class FontFamily
    extends CssValue {
        String family;

        FontFamily() {
        }

        String getValue() {
            return this.family;
        }

        @Override
        Object parseCssValue(String value) {
            int cIndex = value.indexOf(44);
            FontFamily ff = new FontFamily();
            ff.svalue = value;
            ff.family = null;
            if (cIndex == -1) {
                this.setFontName(ff, value);
            } else {
                boolean done = false;
                int length = value.length();
                cIndex = 0;
                while (!done) {
                    while (cIndex < length && Character.isWhitespace(value.charAt(cIndex))) {
                        ++cIndex;
                    }
                    int lastIndex = cIndex;
                    if ((cIndex = value.indexOf(44, cIndex)) == -1) {
                        cIndex = length;
                    }
                    if (lastIndex < length) {
                        if (lastIndex != cIndex) {
                            int lastCharIndex = cIndex;
                            if (cIndex > 0 && value.charAt(cIndex - 1) == ' ') {
                                --lastCharIndex;
                            }
                            this.setFontName(ff, value.substring(lastIndex, lastCharIndex));
                            done = ff.family != null;
                        }
                        ++cIndex;
                        continue;
                    }
                    done = true;
                }
            }
            if (ff.family == null) {
                ff.family = "SansSerif";
            }
            return ff;
        }

        private void setFontName(FontFamily ff, String fontName) {
            ff.family = fontName;
        }

        @Override
        Object parseHtmlValue(String value) {
            return this.parseCssValue(value);
        }

        @Override
        Object fromStyleConstants(StyleConstants key, Object value) {
            return this.parseCssValue(value.toString());
        }

        @Override
        Object toStyleConstants(StyleConstants key, View v) {
            return this.family;
        }
    }

    static class FontWeight
    extends CssValue {
        int weight;

        FontWeight() {
        }

        int getValue() {
            return this.weight;
        }

        @Override
        Object parseCssValue(String value) {
            FontWeight fw = new FontWeight();
            fw.svalue = value;
            if (value.equals("bold")) {
                fw.weight = 700;
            } else if (value.equals("normal")) {
                fw.weight = 400;
            } else {
                try {
                    fw.weight = Integer.parseInt(value);
                }
                catch (NumberFormatException nfe) {
                    fw = null;
                }
            }
            return fw;
        }

        @Override
        Object fromStyleConstants(StyleConstants key, Object value) {
            if (value.equals(Boolean.TRUE)) {
                return this.parseCssValue("bold");
            }
            return this.parseCssValue("normal");
        }

        @Override
        Object toStyleConstants(StyleConstants key, View v) {
            return this.weight > 500 ? Boolean.TRUE : Boolean.FALSE;
        }

        boolean isBold() {
            return this.weight > 500;
        }
    }

    static class BorderStyle
    extends CssValue {
        private transient Value style;

        BorderStyle() {
        }

        Value getValue() {
            return this.style;
        }

        @Override
        Object parseCssValue(String value) {
            Value cssv = CSS.getValue(value);
            if (cssv != null && (cssv == Value.INSET || cssv == Value.OUTSET || cssv == Value.NONE || cssv == Value.DOTTED || cssv == Value.DASHED || cssv == Value.SOLID || cssv == Value.DOUBLE || cssv == Value.GROOVE || cssv == Value.RIDGE)) {
                BorderStyle bs = new BorderStyle();
                bs.svalue = value;
                bs.style = cssv;
                return bs;
            }
            return null;
        }

        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            if (this.style == null) {
                s.writeObject(null);
            } else {
                s.writeObject(this.style.toString());
            }
        }

        private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
            s.defaultReadObject();
            Object value = s.readObject();
            if (value != null) {
                this.style = CSS.getValue((String)value);
            }
        }
    }

    static class ColorValue
    extends CssValue {
        Color c;

        ColorValue() {
        }

        Color getValue() {
            return this.c;
        }

        @Override
        Object parseCssValue(String value) {
            Color c = CSS.stringToColor(value);
            if (c != null) {
                ColorValue cv = new ColorValue();
                cv.svalue = value;
                cv.c = c;
                return cv;
            }
            return null;
        }

        @Override
        Object parseHtmlValue(String value) {
            return this.parseCssValue(value);
        }

        @Override
        Object fromStyleConstants(StyleConstants key, Object value) {
            ColorValue colorValue = new ColorValue();
            colorValue.c = (Color)value;
            colorValue.svalue = CSS.colorToHex(colorValue.c);
            return colorValue;
        }

        @Override
        Object toStyleConstants(StyleConstants key, View v) {
            return this.c;
        }
    }

    static class LengthValue
    extends CssValue {
        boolean mayBeNegative;
        boolean percentage;
        float span;
        String units = null;

        LengthValue() {
            this(false);
        }

        LengthValue(boolean mayBeNegative) {
            this.mayBeNegative = mayBeNegative;
        }

        float getValue() {
            return this.getValue(false);
        }

        float getValue(boolean isW3CLengthUnits) {
            return this.getValue(0.0f, isW3CLengthUnits);
        }

        float getValue(float currentValue) {
            return this.getValue(currentValue, false);
        }

        float getValue(float currentValue, boolean isW3CLengthUnits) {
            if (this.percentage) {
                return this.span * currentValue;
            }
            return LengthUnit.getValue(this.span, this.units, isW3CLengthUnits);
        }

        boolean isPercentage() {
            return this.percentage;
        }

        @Override
        Object parseCssValue(String value) {
            LengthValue lv;
            try {
                float absolute = Float.parseFloat(value);
                lv = new LengthValue();
                lv.span = absolute;
            }
            catch (NumberFormatException nfe) {
                LengthUnit lu = new LengthUnit(value, 10, 0.0f);
                switch (lu.type) {
                    case 0: {
                        lv = new LengthValue();
                        lv.span = this.mayBeNegative ? lu.value : Math.max(0.0f, lu.value);
                        lv.units = lu.units;
                        break;
                    }
                    case 1: {
                        lv = new LengthValue();
                        lv.span = Math.max(0.0f, Math.min(1.0f, lu.value));
                        lv.percentage = true;
                        break;
                    }
                    default: {
                        return null;
                    }
                }
            }
            lv.svalue = value;
            return lv;
        }

        @Override
        Object parseHtmlValue(String value) {
            if (value.equals("#DEFAULT")) {
                value = "1";
            }
            return this.parseCssValue(value);
        }

        @Override
        Object fromStyleConstants(StyleConstants key, Object value) {
            LengthValue v = new LengthValue();
            v.svalue = value.toString();
            v.span = ((Float)value).floatValue();
            return v;
        }

        @Override
        Object toStyleConstants(StyleConstants key, View v) {
            return Float.valueOf(this.getValue(false));
        }
    }

    static class BorderWidthValue
    extends LengthValue {
        private static final float[] values = new float[]{1.0f, 2.0f, 4.0f};

        BorderWidthValue(String svalue, int index) {
            this.svalue = svalue;
            this.span = values[index];
            this.percentage = false;
        }

        @Override
        Object parseCssValue(String value) {
            if (value != null) {
                if (value.equals("thick")) {
                    return new BorderWidthValue(value, 2);
                }
                if (value.equals("medium")) {
                    return new BorderWidthValue(value, 1);
                }
                if (value.equals("thin")) {
                    return new BorderWidthValue(value, 0);
                }
            }
            return super.parseCssValue(value);
        }

        @Override
        Object parseHtmlValue(String value) {
            if (value == "#DEFAULT") {
                return this.parseCssValue("medium");
            }
            return this.parseCssValue(value);
        }
    }

    static class StringValue
    extends CssValue {
        StringValue() {
        }

        @Override
        Object parseCssValue(String value) {
            StringValue sv = new StringValue();
            sv.svalue = value;
            return sv;
        }

        @Override
        Object fromStyleConstants(StyleConstants key, Object value) {
            if (key == StyleConstants.Italic) {
                if (value.equals(Boolean.TRUE)) {
                    return this.parseCssValue("italic");
                }
                return this.parseCssValue("");
            }
            if (key == StyleConstants.Underline) {
                if (value.equals(Boolean.TRUE)) {
                    return this.parseCssValue("underline");
                }
                return this.parseCssValue("");
            }
            if (key == StyleConstants.Alignment) {
                int align = (Integer)value;
                return this.parseCssValue(switch (align) {
                    case 0 -> "left";
                    case 2 -> "right";
                    case 1 -> "center";
                    case 3 -> "justify";
                    default -> "left";
                });
            }
            if (key == StyleConstants.StrikeThrough) {
                if (value.equals(Boolean.TRUE)) {
                    return this.parseCssValue("line-through");
                }
                return this.parseCssValue("");
            }
            if (key == StyleConstants.Superscript) {
                if (value.equals(Boolean.TRUE)) {
                    return this.parseCssValue("super");
                }
                return this.parseCssValue("");
            }
            if (key == StyleConstants.Subscript) {
                if (value.equals(Boolean.TRUE)) {
                    return this.parseCssValue("sub");
                }
                return this.parseCssValue("");
            }
            return null;
        }

        @Override
        Object toStyleConstants(StyleConstants key, View v) {
            if (key == StyleConstants.Italic) {
                if (this.svalue.contains("italic")) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
            if (key == StyleConstants.Underline) {
                if (this.svalue.contains("underline")) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
            if (key == StyleConstants.Alignment) {
                if (this.svalue.equals("right")) {
                    return 2;
                }
                if (this.svalue.equals("center")) {
                    return 1;
                }
                if (this.svalue.equals("justify")) {
                    return 3;
                }
                return 0;
            }
            if (key == StyleConstants.StrikeThrough) {
                if (this.svalue.contains("line-through")) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
            if (key == StyleConstants.Superscript) {
                if (this.svalue.contains("super")) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
            if (key == StyleConstants.Subscript) {
                if (this.svalue.contains("sub")) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
            return null;
        }

        boolean isItalic() {
            return this.svalue.contains("italic");
        }

        boolean isStrike() {
            return this.svalue.contains("line-through");
        }

        boolean isUnderline() {
            return this.svalue.contains("underline");
        }

        boolean isSub() {
            return this.svalue.contains("sub");
        }

        boolean isSup() {
            return this.svalue.contains("sup");
        }
    }

    static class CssValueMapper
    extends CssValue {
        CssValueMapper() {
        }

        @Override
        Object parseCssValue(String value) {
            Value retValue = cssValueToInternalValueMap.get(value);
            if (retValue == null) {
                retValue = cssValueToInternalValueMap.get(value.toLowerCase());
            }
            return retValue;
        }

        @Override
        Object parseHtmlValue(String value) {
            Value retValue = htmlValueToCssValueMap.get(value);
            if (retValue == null) {
                retValue = htmlValueToCssValueMap.get(value.toLowerCase());
            }
            return retValue;
        }
    }

    static class BackgroundImage
    extends CssValue {
        private boolean loadedImage;
        private ImageIcon image;

        BackgroundImage() {
        }

        @Override
        Object parseCssValue(String value) {
            BackgroundImage retValue = new BackgroundImage();
            retValue.svalue = value;
            return retValue;
        }

        @Override
        Object parseHtmlValue(String value) {
            return this.parseCssValue(value);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        ImageIcon getImage(URL base) {
            if (!this.loadedImage) {
                BackgroundImage backgroundImage = this;
                synchronized (backgroundImage) {
                    if (!this.loadedImage) {
                        URL url = CSS.getURL(base, this.svalue);
                        this.loadedImage = true;
                        if (url != null) {
                            this.image = new ImageIcon();
                            Image tmpImg = Toolkit.getDefaultToolkit().createImage(url);
                            if (tmpImg != null) {
                                this.image.setImage(tmpImg);
                            }
                        }
                    }
                }
            }
            return this.image;
        }
    }

    static class BackgroundPosition
    extends CssValue {
        float horizontalPosition;
        float verticalPosition;
        short relative;

        BackgroundPosition() {
        }

        @Override
        Object parseCssValue(String value) {
            String[] strings = CSS.parseStrings(value);
            int count = strings.length;
            BackgroundPosition bp = new BackgroundPosition();
            bp.relative = (short)5;
            bp.svalue = value;
            if (count > 0) {
                int found = 0;
                int index = 0;
                while (index < count) {
                    String string;
                    if ((string = strings[index++]).equals("center")) {
                        found = (short)(found | 4);
                        continue;
                    }
                    if ((found & 1) == 0) {
                        if (string.equals("top")) {
                            found = (short)(found | 1);
                        } else if (string.equals("bottom")) {
                            found = (short)(found | 1);
                            bp.verticalPosition = 1.0f;
                            continue;
                        }
                    }
                    if ((found & 2) != 0) continue;
                    if (string.equals("left")) {
                        found = (short)(found | 2);
                        bp.horizontalPosition = 0.0f;
                        continue;
                    }
                    if (!string.equals("right")) continue;
                    found = (short)(found | 2);
                    bp.horizontalPosition = 1.0f;
                }
                if (found != 0) {
                    if (found & true) {
                        if ((found & 2) == 0) {
                            bp.horizontalPosition = 0.5f;
                        }
                    } else if ((found & 2) == 2) {
                        bp.verticalPosition = 0.5f;
                    } else {
                        bp.verticalPosition = 0.5f;
                        bp.horizontalPosition = 0.5f;
                    }
                } else {
                    LengthUnit lu = new LengthUnit(strings[0], 0, 0.0f);
                    if (lu.type == 0) {
                        bp.horizontalPosition = lu.value;
                        bp.relative = (short)(1 ^ bp.relative);
                    } else if (lu.type == 1) {
                        bp.horizontalPosition = lu.value;
                    } else if (lu.type == 3) {
                        bp.horizontalPosition = lu.value;
                        bp.relative = (short)(1 ^ bp.relative | 2);
                    }
                    if (count > 1) {
                        lu = new LengthUnit(strings[1], 0, 0.0f);
                        if (lu.type == 0) {
                            bp.verticalPosition = lu.value;
                            bp.relative = (short)(4 ^ bp.relative);
                        } else if (lu.type == 1) {
                            bp.verticalPosition = lu.value;
                        } else if (lu.type == 3) {
                            bp.verticalPosition = lu.value;
                            bp.relative = (short)(4 ^ bp.relative | 8);
                        }
                    } else {
                        bp.verticalPosition = 0.5f;
                    }
                }
            }
            return bp;
        }

        boolean isHorizontalPositionRelativeToSize() {
            return (this.relative & 1) == 1;
        }

        boolean isHorizontalPositionRelativeToFontSize() {
            return (this.relative & 2) == 2;
        }

        float getHorizontalPosition() {
            return this.horizontalPosition;
        }

        boolean isVerticalPositionRelativeToSize() {
            return (this.relative & 4) == 4;
        }

        boolean isVerticalPositionRelativeToFontSize() {
            return (this.relative & 8) == 8;
        }

        float getVerticalPosition() {
            return this.verticalPosition;
        }
    }

    static class CssValue
    implements Serializable {
        String svalue;

        CssValue() {
        }

        Object parseCssValue(String value) {
            return value;
        }

        Object parseHtmlValue(String value) {
            return this.parseCssValue(value);
        }

        Object fromStyleConstants(StyleConstants key, Object value) {
            return null;
        }

        Object toStyleConstants(StyleConstants key, View v) {
            return null;
        }

        public String toString() {
            return this.svalue;
        }
    }

    static class ShorthandFontParser {
        ShorthandFontParser() {
        }

        static void parseShorthandFont(CSS css, String value, MutableAttributeSet attr) {
            String[] strings = CSS.parseStrings(value);
            int count = strings.length;
            int index = 0;
            int found = 0;
            int maxC = Math.min(3, count);
            while (index < maxC) {
                if (!(found & true) && ShorthandFontParser.isFontStyle(strings[index])) {
                    css.addInternalCSSValue(attr, Attribute.FONT_STYLE, strings[index++]);
                    found = (short)(found | 1);
                    continue;
                }
                if ((found & 2) == 0 && ShorthandFontParser.isFontVariant(strings[index])) {
                    css.addInternalCSSValue(attr, Attribute.FONT_VARIANT, strings[index++]);
                    found = (short)(found | 2);
                    continue;
                }
                if ((found & 4) == 0 && ShorthandFontParser.isFontWeight(strings[index])) {
                    css.addInternalCSSValue(attr, Attribute.FONT_WEIGHT, strings[index++]);
                    found = (short)(found | 4);
                    continue;
                }
                if (!strings[index].equals("normal")) break;
                ++index;
            }
            if (!(found & true)) {
                css.addInternalCSSValue(attr, Attribute.FONT_STYLE, "normal");
            }
            if ((found & 2) == 0) {
                css.addInternalCSSValue(attr, Attribute.FONT_VARIANT, "normal");
            }
            if ((found & 4) == 0) {
                css.addInternalCSSValue(attr, Attribute.FONT_WEIGHT, "normal");
            }
            if (index < count) {
                String fontSize = strings[index];
                int slashIndex = fontSize.indexOf(47);
                if (slashIndex != -1) {
                    fontSize = fontSize.substring(0, slashIndex);
                    strings[index] = strings[index].substring(slashIndex);
                } else {
                    ++index;
                }
                css.addInternalCSSValue(attr, Attribute.FONT_SIZE, fontSize);
            } else {
                css.addInternalCSSValue(attr, Attribute.FONT_SIZE, "medium");
            }
            if (index < count && strings[index].startsWith("/")) {
                String lineHeight = null;
                if (strings[index].equals("/")) {
                    if (++index < count) {
                        lineHeight = strings[index++];
                    }
                } else {
                    lineHeight = strings[index++].substring(1);
                }
                if (lineHeight != null) {
                    css.addInternalCSSValue(attr, Attribute.LINE_HEIGHT, lineHeight);
                } else {
                    css.addInternalCSSValue(attr, Attribute.LINE_HEIGHT, "normal");
                }
            } else {
                css.addInternalCSSValue(attr, Attribute.LINE_HEIGHT, "normal");
            }
            if (index < count) {
                Object family = strings[index++];
                while (index < count) {
                    family = (String)family + " " + strings[index++];
                }
                css.addInternalCSSValue(attr, Attribute.FONT_FAMILY, (String)family);
            } else {
                css.addInternalCSSValue(attr, Attribute.FONT_FAMILY, "SansSerif");
            }
        }

        private static boolean isFontStyle(String string) {
            return string.equals("italic") || string.equals("oblique");
        }

        private static boolean isFontVariant(String string) {
            return string.equals("small-caps");
        }

        private static boolean isFontWeight(String string) {
            if (string.equals("bold") || string.equals("bolder") || string.equals("italic") || string.equals("lighter")) {
                return true;
            }
            return string.length() == 3 && string.charAt(0) >= '1' && string.charAt(0) <= '9' && string.charAt(1) == '0' && string.charAt(2) == '0';
        }
    }

    static class ShorthandBackgroundParser {
        ShorthandBackgroundParser() {
        }

        static void parseShorthandBackground(CSS css, String value, MutableAttributeSet attr) {
            String[] strings = CSS.parseStrings(value);
            int count = strings.length;
            int index = 0;
            int found = 0;
            while (index < count) {
                String string = strings[index++];
                if ((found & 1) == 0 && ShorthandBackgroundParser.isImage(string)) {
                    css.addInternalCSSValue(attr, Attribute.BACKGROUND_IMAGE, string);
                    found = (short)(found | 1);
                    continue;
                }
                if ((found & 2) == 0 && ShorthandBackgroundParser.isRepeat(string)) {
                    css.addInternalCSSValue(attr, Attribute.BACKGROUND_REPEAT, string);
                    found = (short)(found | 2);
                    continue;
                }
                if ((found & 4) == 0 && ShorthandBackgroundParser.isAttachment(string)) {
                    css.addInternalCSSValue(attr, Attribute.BACKGROUND_ATTACHMENT, string);
                    found = (short)(found | 4);
                    continue;
                }
                if ((found & 8) == 0 && ShorthandBackgroundParser.isPosition(string)) {
                    if (index < count && ShorthandBackgroundParser.isPosition(strings[index])) {
                        css.addInternalCSSValue(attr, Attribute.BACKGROUND_POSITION, string + " " + strings[index++]);
                    } else {
                        css.addInternalCSSValue(attr, Attribute.BACKGROUND_POSITION, string);
                    }
                    found = (short)(found | 8);
                    continue;
                }
                if ((found & 0x10) != 0 || !ShorthandBackgroundParser.isColor(string)) continue;
                css.addInternalCSSValue(attr, Attribute.BACKGROUND_COLOR, string);
                found = (short)(found | 0x10);
            }
            if (!(found & true)) {
                css.addInternalCSSValue(attr, Attribute.BACKGROUND_IMAGE, null);
            }
            if ((found & 2) == 0) {
                css.addInternalCSSValue(attr, Attribute.BACKGROUND_REPEAT, "repeat");
            }
            if ((found & 4) == 0) {
                css.addInternalCSSValue(attr, Attribute.BACKGROUND_ATTACHMENT, "scroll");
            }
            if ((found & 8) == 0) {
                css.addInternalCSSValue(attr, Attribute.BACKGROUND_POSITION, null);
            }
        }

        static boolean isImage(String string) {
            return string.startsWith("url(") && string.endsWith(")");
        }

        static boolean isRepeat(String string) {
            return string.equals("repeat-x") || string.equals("repeat-y") || string.equals("repeat") || string.equals("no-repeat");
        }

        static boolean isAttachment(String string) {
            return string.equals("fixed") || string.equals("scroll");
        }

        static boolean isPosition(String string) {
            return string.equals("top") || string.equals("bottom") || string.equals("left") || string.equals("right") || string.equals("center") || string.length() > 0 && Character.isDigit(string.charAt(0));
        }

        static boolean isColor(String string) {
            return CSS.stringToColor(string) != null;
        }
    }

    static class ShorthandMarginParser {
        ShorthandMarginParser() {
        }

        static void parseShorthandMargin(CSS css, String value, MutableAttributeSet attr, Attribute[] names) {
            String[] strings = CSS.parseStrings(value);
            int count = strings.length;
            boolean index = false;
            switch (count) {
                case 0: {
                    return;
                }
                case 1: {
                    for (int counter = 0; counter < 4; ++counter) {
                        css.addInternalCSSValue(attr, names[counter], strings[0]);
                    }
                    break;
                }
                case 2: {
                    css.addInternalCSSValue(attr, names[0], strings[0]);
                    css.addInternalCSSValue(attr, names[2], strings[0]);
                    css.addInternalCSSValue(attr, names[1], strings[1]);
                    css.addInternalCSSValue(attr, names[3], strings[1]);
                    break;
                }
                case 3: {
                    css.addInternalCSSValue(attr, names[0], strings[0]);
                    css.addInternalCSSValue(attr, names[1], strings[1]);
                    css.addInternalCSSValue(attr, names[2], strings[2]);
                    css.addInternalCSSValue(attr, names[3], strings[1]);
                    break;
                }
                default: {
                    for (int counter = 0; counter < 4; ++counter) {
                        css.addInternalCSSValue(attr, names[counter], strings[counter]);
                    }
                }
            }
        }
    }

    static class ShorthandBorderParser {
        static Attribute[] keys = new Attribute[]{Attribute.BORDER_TOP, Attribute.BORDER_RIGHT, Attribute.BORDER_BOTTOM, Attribute.BORDER_LEFT};

        ShorthandBorderParser() {
        }

        static void parseShorthandBorder(MutableAttributeSet attributes, Attribute key, String value) {
            int i;
            String[] strings;
            Object[] parts = new Object[CSSBorder.PARSERS.length];
            for (String s : strings = CSS.parseStrings(value)) {
                boolean valid = false;
                for (int i2 = 0; i2 < parts.length; ++i2) {
                    Object v = CSSBorder.PARSERS[i2].parseCssValue(s);
                    if (v == null) continue;
                    if (parts[i2] != null) break;
                    parts[i2] = v;
                    valid = true;
                    break;
                }
                if (valid) continue;
                return;
            }
            for (i = 0; i < parts.length; ++i) {
                if (parts[i] != null) continue;
                parts[i] = CSSBorder.DEFAULTS[i];
            }
            for (i = 0; i < keys.length; ++i) {
                if (key != Attribute.BORDER && key != keys[i]) continue;
                for (int k = 0; k < parts.length; ++k) {
                    attributes.addAttribute(CSSBorder.ATTRIBUTES[k][i], parts[k]);
                }
            }
        }
    }

    static final class Value {
        static final Value INHERITED = new Value("inherited");
        static final Value NONE = new Value("none");
        static final Value HIDDEN = new Value("hidden");
        static final Value DOTTED = new Value("dotted");
        static final Value DASHED = new Value("dashed");
        static final Value SOLID = new Value("solid");
        static final Value DOUBLE = new Value("double");
        static final Value GROOVE = new Value("groove");
        static final Value RIDGE = new Value("ridge");
        static final Value INSET = new Value("inset");
        static final Value OUTSET = new Value("outset");
        static final Value DISC = new Value("disc");
        static final Value CIRCLE = new Value("circle");
        static final Value SQUARE = new Value("square");
        static final Value DECIMAL = new Value("decimal");
        static final Value LOWER_ROMAN = new Value("lower-roman");
        static final Value UPPER_ROMAN = new Value("upper-roman");
        static final Value LOWER_ALPHA = new Value("lower-alpha");
        static final Value UPPER_ALPHA = new Value("upper-alpha");
        static final Value BACKGROUND_NO_REPEAT = new Value("no-repeat");
        static final Value BACKGROUND_REPEAT = new Value("repeat");
        static final Value BACKGROUND_REPEAT_X = new Value("repeat-x");
        static final Value BACKGROUND_REPEAT_Y = new Value("repeat-y");
        static final Value BACKGROUND_SCROLL = new Value("scroll");
        static final Value BACKGROUND_FIXED = new Value("fixed");
        private String name;
        static final Value[] allValues = new Value[]{INHERITED, NONE, DOTTED, DASHED, SOLID, DOUBLE, GROOVE, RIDGE, INSET, OUTSET, DISC, CIRCLE, SQUARE, DECIMAL, LOWER_ROMAN, UPPER_ROMAN, LOWER_ALPHA, UPPER_ALPHA, BACKGROUND_NO_REPEAT, BACKGROUND_REPEAT, BACKGROUND_REPEAT_X, BACKGROUND_REPEAT_Y, BACKGROUND_FIXED, BACKGROUND_FIXED};

        private Value(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    static interface LayoutIterator {
        public static final int WorstAdjustmentWeight = 2;

        public void setOffset(int var1);

        public int getOffset();

        public void setSpan(int var1);

        public int getSpan();

        public int getCount();

        public void setIndex(int var1);

        public float getMinimumSpan(float var1);

        public float getPreferredSpan(float var1);

        public float getMaximumSpan(float var1);

        public int getAdjustmentWeight();

        public float getBorderWidth();

        public float getLeadingCollapseSpan();

        public float getTrailingCollapseSpan();
    }

    static class LengthUnit
    implements Serializable {
        static Hashtable<String, Float> lengthMapping = new Hashtable(6);
        static Hashtable<String, Float> w3cLengthMapping = new Hashtable(6);
        short type;
        float value;
        String units = null;
        static final short UNINITIALIZED_LENGTH = 10;

        LengthUnit(String value, short defaultType, float defaultValue) {
            this.parse(value, defaultType, defaultValue);
        }

        void parse(String value, short defaultType, float defaultValue) {
            this.type = defaultType;
            this.value = defaultValue;
            int length = value.length();
            if (length < 1) {
                return;
            }
            if (value.charAt(length - 1) == '%') {
                try {
                    this.value = Float.parseFloat(value.substring(0, length - 1)) / 100.0f;
                    this.type = 1;
                }
                catch (NumberFormatException numberFormatException) {}
            } else if (length >= 2) {
                this.units = value.substring(length - 2, length);
                Float scale = lengthMapping.get(this.units);
                if (scale != null) {
                    try {
                        this.value = Float.parseFloat(value.substring(0, length - 2));
                        this.type = 0;
                    }
                    catch (NumberFormatException numberFormatException) {}
                } else if (this.units.equals("em") || this.units.equals("ex")) {
                    try {
                        this.value = Float.parseFloat(value.substring(0, length - 2));
                        this.type = (short)3;
                    }
                    catch (NumberFormatException numberFormatException) {}
                } else if (value.equals("larger")) {
                    this.value = 2.0f;
                    this.type = (short)2;
                } else if (value.equals("smaller")) {
                    this.value = -2.0f;
                    this.type = (short)2;
                } else {
                    try {
                        this.value = Float.parseFloat(value);
                        this.type = 0;
                    }
                    catch (NumberFormatException numberFormatException) {}
                }
            } else {
                try {
                    this.value = Float.parseFloat(value);
                    this.type = 0;
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }
        }

        float getValue(boolean w3cLengthUnits) {
            Float scaleFloat;
            Hashtable<String, Float> mapping = w3cLengthUnits ? w3cLengthMapping : lengthMapping;
            float scale = 1.0f;
            if (this.units != null && (scaleFloat = mapping.get(this.units)) != null) {
                scale = scaleFloat.floatValue();
            }
            return this.value * scale;
        }

        static float getValue(float value, String units, Boolean w3cLengthUnits) {
            Float scaleFloat;
            Hashtable<String, Float> mapping = w3cLengthUnits != false ? w3cLengthMapping : lengthMapping;
            float scale = 1.0f;
            if (units != null && (scaleFloat = mapping.get(units)) != null) {
                scale = scaleFloat.floatValue();
            }
            return value * scale;
        }

        public String toString() {
            return this.type + " " + this.value;
        }

        static {
            lengthMapping.put("pt", Float.valueOf(1.0f));
            lengthMapping.put("px", Float.valueOf(1.3f));
            lengthMapping.put("mm", Float.valueOf(2.83464f));
            lengthMapping.put("cm", Float.valueOf(28.3464f));
            lengthMapping.put("pc", Float.valueOf(12.0f));
            lengthMapping.put("in", Float.valueOf(72.0f));
            w3cLengthMapping.put("pt", Float.valueOf(1.3333334f));
            w3cLengthMapping.put("px", Float.valueOf(1.0f));
            w3cLengthMapping.put("mm", Float.valueOf(3.7795277f));
            w3cLengthMapping.put("cm", Float.valueOf(37.795277f));
            w3cLengthMapping.put("pc", Float.valueOf(16.0f));
            w3cLengthMapping.put("in", Float.valueOf(96.0f));
        }
    }
}

