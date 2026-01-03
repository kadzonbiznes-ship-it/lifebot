/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EmptyStackException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;
import javax.swing.text.html.CSS;
import javax.swing.text.html.CSSBorder;
import javax.swing.text.html.CSSParser;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.MuxingAttributeSet;
import sun.swing.SwingUtilities2;

public class StyleSheet
extends StyleContext {
    private transient Object fontSizeInherit;
    private CSS css;
    private SelectorMapping selectorMapping;
    private Hashtable<String, ResolvedStyle> resolvedStyles;
    private Vector<StyleSheet> linkedStyleSheets;
    private URL base;
    static final int[] sizeMapDefault = new int[]{8, 10, 12, 14, 18, 24, 36};
    private int[] sizeMap = sizeMapDefault;
    private boolean w3cLengthUnits = false;

    public StyleSheet() {
        this.selectorMapping = new SelectorMapping(0);
        this.resolvedStyles = new Hashtable();
        if (this.css == null) {
            this.css = new CSS();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Style getRule(HTML.Tag t, Element e) {
        SearchBuffer sb = SearchBuffer.obtainSearchBuffer();
        try {
            Style style;
            AttributeSet attr;
            Vector searchContext = sb.getVector();
            for (Element p = e; p != null; p = p.getParentElement()) {
                searchContext.addElement(p);
            }
            int n = searchContext.size();
            StringBuffer cacheLookup = sb.getStringBuffer();
            for (int counter = n - 1; counter >= 1; --counter) {
                e = (Element)searchContext.elementAt(counter);
                attr = e.getAttributes();
                Object name = attr.getAttribute(StyleConstants.NameAttribute);
                String eName = name.toString();
                cacheLookup.append(eName);
                if (attr != null) {
                    if (attr.isDefined(HTML.Attribute.ID)) {
                        cacheLookup.append('#');
                        cacheLookup.append(attr.getAttribute(HTML.Attribute.ID));
                    } else if (attr.isDefined(HTML.Attribute.CLASS)) {
                        cacheLookup.append('.');
                        cacheLookup.append(attr.getAttribute(HTML.Attribute.CLASS));
                    }
                }
                cacheLookup.append(' ');
            }
            cacheLookup.append(t.toString());
            e = (Element)searchContext.elementAt(0);
            attr = e.getAttributes();
            if (e.isLeaf()) {
                Object testAttr = attr.getAttribute(t);
                attr = testAttr instanceof AttributeSet ? (AttributeSet)testAttr : null;
            }
            if (attr != null) {
                if (attr.isDefined(HTML.Attribute.ID)) {
                    cacheLookup.append('#');
                    cacheLookup.append(attr.getAttribute(HTML.Attribute.ID));
                } else if (attr.isDefined(HTML.Attribute.CLASS)) {
                    cacheLookup.append('.');
                    cacheLookup.append(attr.getAttribute(HTML.Attribute.CLASS));
                }
            }
            Style style2 = style = this.getResolvedStyle(cacheLookup.toString(), searchContext, t);
            return style2;
        }
        finally {
            SearchBuffer.releaseSearchBuffer(sb);
        }
    }

    public Style getRule(String selector) {
        if ((selector = this.cleanSelectorString(selector)) != null) {
            Style style = this.getResolvedStyle(selector);
            return style;
        }
        return null;
    }

    public void addRule(String rule) {
        if (rule != null) {
            String baseUnitsDisable = "BASE_SIZE_DISABLE";
            String baseUnits = "BASE_SIZE ";
            String w3cLengthUnitsEnable = "W3C_LENGTH_UNITS_ENABLE";
            String w3cLengthUnitsDisable = "W3C_LENGTH_UNITS_DISABLE";
            if (rule == "BASE_SIZE_DISABLE") {
                this.sizeMap = sizeMapDefault;
            } else if (rule.startsWith("BASE_SIZE ")) {
                this.rebaseSizeMap(Integer.parseInt(rule.substring("BASE_SIZE ".length())));
            } else if (rule == "W3C_LENGTH_UNITS_ENABLE") {
                this.w3cLengthUnits = true;
            } else if (rule == "W3C_LENGTH_UNITS_DISABLE") {
                this.w3cLengthUnits = false;
            } else {
                CssParser parser = new CssParser();
                try {
                    parser.parse(this.getBase(), new StringReader(rule), false, false);
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
        }
    }

    public AttributeSet getDeclaration(String decl) {
        if (decl == null) {
            return SimpleAttributeSet.EMPTY;
        }
        CssParser parser = new CssParser();
        return parser.parseDeclaration(decl);
    }

    public void loadRules(Reader in, URL ref) throws IOException {
        CssParser parser = new CssParser();
        parser.parse(ref, in, false, false);
    }

    public AttributeSet getViewAttributes(View v) {
        return new ViewAttributeSet(v);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeStyle(String nm) {
        Style aStyle = this.getStyle(nm);
        if (aStyle != null) {
            String selector = this.cleanSelectorString(nm);
            String[] selectors = this.getSimpleSelectors(selector);
            StyleSheet styleSheet = this;
            synchronized (styleSheet) {
                SelectorMapping mapping = this.getRootSelectorMapping();
                for (int i = selectors.length - 1; i >= 0; --i) {
                    mapping = mapping.getChildSelectorMapping(selectors[i], true);
                }
                Style rule = mapping.getStyle();
                if (rule != null) {
                    mapping.setStyle(null);
                    if (this.resolvedStyles.size() > 0) {
                        Enumeration<ResolvedStyle> values = this.resolvedStyles.elements();
                        while (values.hasMoreElements()) {
                            ResolvedStyle style = values.nextElement();
                            style.removeStyle(rule);
                        }
                    }
                }
            }
        }
        super.removeStyle(nm);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addStyleSheet(StyleSheet ss) {
        StyleSheet styleSheet = this;
        synchronized (styleSheet) {
            if (this.linkedStyleSheets == null) {
                this.linkedStyleSheets = new Vector();
            }
            if (!this.linkedStyleSheets.contains(ss)) {
                int index = 0;
                if (ss instanceof UIResource && this.linkedStyleSheets.size() > 1) {
                    index = this.linkedStyleSheets.size() - 1;
                }
                this.linkedStyleSheets.insertElementAt(ss, index);
                this.linkStyleSheetAt(ss, index);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeStyleSheet(StyleSheet ss) {
        StyleSheet styleSheet = this;
        synchronized (styleSheet) {
            int index;
            if (this.linkedStyleSheets != null && (index = this.linkedStyleSheets.indexOf(ss)) != -1) {
                this.linkedStyleSheets.removeElementAt(index);
                this.unlinkStyleSheet(ss, index);
                if (index == 0 && this.linkedStyleSheets.size() == 0) {
                    this.linkedStyleSheets = null;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public StyleSheet[] getStyleSheets() {
        Object[] retValue;
        StyleSheet styleSheet = this;
        synchronized (styleSheet) {
            if (this.linkedStyleSheets != null) {
                retValue = new StyleSheet[this.linkedStyleSheets.size()];
                this.linkedStyleSheets.copyInto(retValue);
            } else {
                retValue = null;
            }
        }
        return retValue;
    }

    public void importStyleSheet(URL url) {
        try (InputStream is = url.openStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader r = new BufferedReader(isr);){
            CssParser parser = new CssParser();
            parser.parse(url, r, false, true);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public void setBase(URL base) {
        this.base = base;
    }

    public URL getBase() {
        return this.base;
    }

    public void addCSSAttribute(MutableAttributeSet attr, CSS.Attribute key, String value) {
        this.css.addInternalCSSValue(attr, key, value);
    }

    public boolean addCSSAttributeFromHTML(MutableAttributeSet attr, CSS.Attribute key, String value) {
        Object iValue = this.css.getCssValue(key, value);
        if (iValue != null) {
            attr.addAttribute(key, iValue);
            return true;
        }
        return false;
    }

    public AttributeSet translateHTMLToCSS(AttributeSet htmlAttrSet) {
        AttributeSet cssAttrSet = this.css.translateHTMLToCSS(htmlAttrSet);
        Style cssStyleSet = this.addStyle(null, null);
        cssStyleSet.addAttributes(cssAttrSet);
        return cssStyleSet;
    }

    @Override
    public AttributeSet addAttribute(AttributeSet old, Object key, Object value) {
        if (this.css == null) {
            this.css = new CSS();
        }
        if (key instanceof StyleConstants) {
            CSS.Attribute cssKey;
            Object cssValue;
            HTML.Tag tag = HTML.getTagForStyleConstantsKey((StyleConstants)key);
            if (tag != null && old.isDefined(tag)) {
                old = this.removeAttribute(old, tag);
            }
            if ((cssValue = this.css.styleConstantsValueToCSSValue((StyleConstants)key, value)) != null && (cssKey = this.css.styleConstantsKeyToCSSKey((StyleConstants)key)) != null) {
                return super.addAttribute(old, cssKey, cssValue);
            }
        }
        return super.addAttribute(old, key, value);
    }

    @Override
    public AttributeSet addAttributes(AttributeSet old, AttributeSet attr) {
        if (!(attr instanceof HTMLDocument.TaggedAttributeSet)) {
            old = this.removeHTMLTags(old, attr);
        }
        return super.addAttributes(old, this.convertAttributeSet(attr));
    }

    @Override
    public AttributeSet removeAttribute(AttributeSet old, Object key) {
        if (key instanceof StyleConstants) {
            CSS.Attribute cssKey;
            HTML.Tag tag = HTML.getTagForStyleConstantsKey((StyleConstants)key);
            if (tag != null) {
                old = super.removeAttribute(old, tag);
            }
            if ((cssKey = this.css.styleConstantsKeyToCSSKey((StyleConstants)key)) != null) {
                return super.removeAttribute(old, cssKey);
            }
        }
        return super.removeAttribute(old, key);
    }

    @Override
    public AttributeSet removeAttributes(AttributeSet old, Enumeration<?> names) {
        return super.removeAttributes(old, names);
    }

    @Override
    public AttributeSet removeAttributes(AttributeSet old, AttributeSet attrs) {
        if (old != attrs) {
            old = this.removeHTMLTags(old, attrs);
        }
        return super.removeAttributes(old, this.convertAttributeSet(attrs));
    }

    @Override
    protected StyleContext.SmallAttributeSet createSmallAttributeSet(AttributeSet a) {
        return new SmallConversionSet(a);
    }

    @Override
    protected MutableAttributeSet createLargeAttributeSet(AttributeSet a) {
        return new LargeConversionSet(a);
    }

    private AttributeSet removeHTMLTags(AttributeSet old, AttributeSet attr) {
        if (!(attr instanceof LargeConversionSet) && !(attr instanceof SmallConversionSet)) {
            Enumeration<?> names = attr.getAttributeNames();
            while (names.hasMoreElements()) {
                HTML.Tag tag;
                Object key = names.nextElement();
                if (!(key instanceof StyleConstants) || (tag = HTML.getTagForStyleConstantsKey((StyleConstants)key)) == null || !old.isDefined(tag)) continue;
                old = super.removeAttribute(old, tag);
            }
        }
        return old;
    }

    AttributeSet convertAttributeSet(AttributeSet a) {
        if (a instanceof LargeConversionSet || a instanceof SmallConversionSet) {
            return a;
        }
        Enumeration<?> names = a.getAttributeNames();
        while (names.hasMoreElements()) {
            Object name = names.nextElement();
            if (!(name instanceof StyleConstants)) continue;
            LargeConversionSet converted = new LargeConversionSet();
            Enumeration<?> keys = a.getAttributeNames();
            while (keys.hasMoreElements()) {
                Object value;
                CSS.Attribute cssKey;
                Object key = keys.nextElement();
                Object cssValue = null;
                if (key instanceof StyleConstants && (cssKey = this.css.styleConstantsKeyToCSSKey((StyleConstants)key)) != null && (cssValue = this.css.styleConstantsValueToCSSValue((StyleConstants)key, value = a.getAttribute(key))) != null) {
                    converted.addAttribute(cssKey, cssValue);
                }
                if (cssValue != null) continue;
                converted.addAttribute(key, a.getAttribute(key));
            }
            return converted;
        }
        return a;
    }

    @Override
    public Font getFont(AttributeSet a) {
        return this.css.getFont(this, a, 12, this);
    }

    @Override
    public Color getForeground(AttributeSet a) {
        Color c = this.css.getColor(a, CSS.Attribute.COLOR);
        if (c == null) {
            return Color.black;
        }
        return c;
    }

    @Override
    public Color getBackground(AttributeSet a) {
        return this.css.getColor(a, CSS.Attribute.BACKGROUND_COLOR);
    }

    public BoxPainter getBoxPainter(AttributeSet a) {
        return new BoxPainter(a, this.css, this);
    }

    public ListPainter getListPainter(AttributeSet a) {
        return new ListPainter(a, this);
    }

    public void setBaseFontSize(int sz) {
        this.css.setBaseFontSize(sz);
    }

    public void setBaseFontSize(String size) {
        this.css.setBaseFontSize(size);
    }

    public static int getIndexOfSize(float pt) {
        return CSS.getIndexOfSize(pt, sizeMapDefault);
    }

    public float getPointSize(int index) {
        return this.css.getPointSize(index, this);
    }

    public float getPointSize(String size) {
        return this.css.getPointSize(size, this);
    }

    public Color stringToColor(String string) {
        return CSS.stringToColor(string);
    }

    ImageIcon getBackgroundImage(AttributeSet attr) {
        Object value = attr.getAttribute(CSS.Attribute.BACKGROUND_IMAGE);
        if (value != null) {
            return ((CSS.BackgroundImage)value).getImage(this.getBase());
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void addRule(String[] selector, AttributeSet declaration, boolean isLinked) {
        int n = selector.length;
        StringBuilder sb = new StringBuilder();
        sb.append(selector[0]);
        for (int counter = 1; counter < n; ++counter) {
            sb.append(' ');
            sb.append(selector[counter]);
        }
        String selectorName = sb.toString();
        Style rule = this.getStyle(selectorName);
        if (rule == null) {
            Style altRule = this.addStyle(selectorName, null);
            StyleSheet styleSheet = this;
            synchronized (styleSheet) {
                SelectorMapping mapping = this.getRootSelectorMapping();
                for (int i = n - 1; i >= 0; --i) {
                    mapping = mapping.getChildSelectorMapping(selector[i], true);
                }
                rule = mapping.getStyle();
                if (rule == null) {
                    rule = altRule;
                    mapping.setStyle(rule);
                    this.refreshResolvedRules(selectorName, selector, rule, mapping.getSpecificity());
                }
            }
        }
        if (isLinked) {
            rule = this.getLinkedStyle(rule);
        }
        rule.addAttributes(declaration);
    }

    private synchronized void linkStyleSheetAt(StyleSheet ss, int index) {
        if (this.resolvedStyles.size() > 0) {
            Enumeration<ResolvedStyle> values = this.resolvedStyles.elements();
            while (values.hasMoreElements()) {
                ResolvedStyle rule = values.nextElement();
                rule.insertExtendedStyleAt(ss.getRule(rule.getName()), index);
            }
        }
    }

    private synchronized void unlinkStyleSheet(StyleSheet ss, int index) {
        if (this.resolvedStyles.size() > 0) {
            Enumeration<ResolvedStyle> values = this.resolvedStyles.elements();
            while (values.hasMoreElements()) {
                ResolvedStyle rule = values.nextElement();
                rule.removeExtendedStyleAt(index);
            }
        }
    }

    String[] getSimpleSelectors(String selector) {
        selector = this.cleanSelectorString(selector);
        SearchBuffer sb = SearchBuffer.obtainSearchBuffer();
        Vector selectors = sb.getVector();
        int lastIndex = 0;
        int length = selector.length();
        while (lastIndex != -1) {
            int newIndex = selector.indexOf(32, lastIndex);
            if (newIndex != -1) {
                selectors.addElement(selector.substring(lastIndex, newIndex));
                if (++newIndex == length) {
                    lastIndex = -1;
                    continue;
                }
                lastIndex = newIndex;
                continue;
            }
            selectors.addElement(selector.substring(lastIndex));
            lastIndex = -1;
        }
        Object[] retValue = new String[selectors.size()];
        selectors.copyInto(retValue);
        SearchBuffer.releaseSearchBuffer(sb);
        return retValue;
    }

    String cleanSelectorString(String selector) {
        boolean lastWasSpace = true;
        int maxCounter = selector.length();
        block4: for (int counter = 0; counter < maxCounter; ++counter) {
            switch (selector.charAt(counter)) {
                case ' ': {
                    if (lastWasSpace) {
                        return this._cleanSelectorString(selector);
                    }
                    lastWasSpace = true;
                    continue block4;
                }
                case '\t': 
                case '\n': 
                case '\r': {
                    return this._cleanSelectorString(selector);
                }
                default: {
                    lastWasSpace = false;
                }
            }
        }
        if (lastWasSpace) {
            return this._cleanSelectorString(selector);
        }
        return selector;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String _cleanSelectorString(String selector) {
        SearchBuffer sb = SearchBuffer.obtainSearchBuffer();
        StringBuffer buff = sb.getStringBuffer();
        boolean lastWasSpace = true;
        int lastIndex = 0;
        char[] chars = selector.toCharArray();
        int numChars = chars.length;
        String retValue = null;
        try {
            block7: for (int counter = 0; counter < numChars; ++counter) {
                switch (chars[counter]) {
                    case ' ': {
                        if (!lastWasSpace) {
                            lastWasSpace = true;
                            if (lastIndex < counter) {
                                buff.append(chars, lastIndex, 1 + counter - lastIndex);
                            }
                        }
                        lastIndex = counter + 1;
                        continue block7;
                    }
                    case '\t': 
                    case '\n': 
                    case '\r': {
                        if (!lastWasSpace) {
                            lastWasSpace = true;
                            if (lastIndex < counter) {
                                buff.append(chars, lastIndex, counter - lastIndex);
                                buff.append(' ');
                            }
                        }
                        lastIndex = counter + 1;
                        continue block7;
                    }
                    default: {
                        lastWasSpace = false;
                    }
                }
            }
            if (lastWasSpace && buff.length() > 0) {
                buff.setLength(buff.length() - 1);
            } else if (lastIndex < numChars) {
                buff.append(chars, lastIndex, numChars - lastIndex);
            }
            retValue = buff.toString();
        }
        finally {
            SearchBuffer.releaseSearchBuffer(sb);
        }
        return retValue;
    }

    private SelectorMapping getRootSelectorMapping() {
        return this.selectorMapping;
    }

    static int getSpecificity(String selector) {
        int specificity = 0;
        boolean lastWasSpace = true;
        int maxCounter = selector.length();
        block5: for (int counter = 0; counter < maxCounter; ++counter) {
            switch (selector.charAt(counter)) {
                case '.': {
                    specificity += 100;
                    continue block5;
                }
                case '#': {
                    specificity += 10000;
                    continue block5;
                }
                case ' ': {
                    lastWasSpace = true;
                    continue block5;
                }
                default: {
                    if (!lastWasSpace) continue block5;
                    lastWasSpace = false;
                    ++specificity;
                }
            }
        }
        return specificity;
    }

    private Style getLinkedStyle(Style localStyle) {
        Style retStyle = (Style)localStyle.getResolveParent();
        if (retStyle == null) {
            retStyle = this.addStyle(null, null);
            localStyle.setResolveParent(retStyle);
        }
        return retStyle;
    }

    private synchronized Style getResolvedStyle(String selector, Vector<Element> elements, HTML.Tag t) {
        Style retStyle = this.resolvedStyles.get(selector);
        if (retStyle == null) {
            retStyle = this.createResolvedStyle(selector, elements, t);
        }
        return retStyle;
    }

    private synchronized Style getResolvedStyle(String selector) {
        Style retStyle = this.resolvedStyles.get(selector);
        if (retStyle == null) {
            retStyle = this.createResolvedStyle(selector);
        }
        return retStyle;
    }

    private void addSortedStyle(SelectorMapping mapping, Vector<SelectorMapping> elements) {
        int size = elements.size();
        if (size > 0) {
            int specificity = mapping.getSpecificity();
            for (int counter = 0; counter < size; ++counter) {
                if (specificity < elements.elementAt(counter).getSpecificity()) continue;
                elements.insertElementAt(mapping, counter);
                return;
            }
        }
        elements.addElement(mapping);
    }

    private synchronized void getStyles(SelectorMapping parentMapping, Vector<SelectorMapping> styles, String[] tags, String[] ids, String[] classes, int index, int numElements, HashSet<SelectorMapping> alreadyChecked) {
        if (!alreadyChecked.add(parentMapping)) {
            return;
        }
        Style style = parentMapping.getStyle();
        if (style != null) {
            this.addSortedStyle(parentMapping, styles);
        }
        for (int counter = index; counter < numElements; ++counter) {
            String tagString = tags[counter];
            if (tagString == null) continue;
            SelectorMapping childMapping = parentMapping.getChildSelectorMapping(tagString, false);
            if (childMapping != null) {
                this.getStyles(childMapping, styles, tags, ids, classes, counter + 1, numElements, alreadyChecked);
            }
            if (classes[counter] != null) {
                String className = classes[counter];
                childMapping = parentMapping.getChildSelectorMapping(tagString + "." + className, false);
                if (childMapping != null) {
                    this.getStyles(childMapping, styles, tags, ids, classes, counter + 1, numElements, alreadyChecked);
                }
                if ((childMapping = parentMapping.getChildSelectorMapping("." + className, false)) != null) {
                    this.getStyles(childMapping, styles, tags, ids, classes, counter + 1, numElements, alreadyChecked);
                }
            }
            if (ids[counter] == null) continue;
            String idName = ids[counter];
            childMapping = parentMapping.getChildSelectorMapping(tagString + "#" + idName, false);
            if (childMapping != null) {
                this.getStyles(childMapping, styles, tags, ids, classes, counter + 1, numElements, alreadyChecked);
            }
            if ((childMapping = parentMapping.getChildSelectorMapping("#" + idName, false)) == null) continue;
            this.getStyles(childMapping, styles, tags, ids, classes, counter + 1, numElements, alreadyChecked);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private synchronized Style createResolvedStyle(String selector, String[] tags, String[] ids, String[] classes) {
        SearchBuffer sb = SearchBuffer.obtainSearchBuffer();
        Vector tempVector = sb.getVector();
        HashSet<SelectorMapping> alreadyChecked = sb.getHashSet();
        try {
            int counter;
            SelectorMapping mapping = this.getRootSelectorMapping();
            int numElements = tags.length;
            String tagString = tags[0];
            SelectorMapping childMapping = mapping.getChildSelectorMapping(tagString, false);
            if (childMapping != null) {
                this.getStyles(childMapping, tempVector, tags, ids, classes, 1, numElements, alreadyChecked);
            }
            if (classes[0] != null) {
                String className = classes[0];
                childMapping = mapping.getChildSelectorMapping(tagString + "." + className, false);
                if (childMapping != null) {
                    this.getStyles(childMapping, tempVector, tags, ids, classes, 1, numElements, alreadyChecked);
                }
                if ((childMapping = mapping.getChildSelectorMapping("." + className, false)) != null) {
                    this.getStyles(childMapping, tempVector, tags, ids, classes, 1, numElements, alreadyChecked);
                }
            }
            if (ids[0] != null) {
                String idName = ids[0];
                childMapping = mapping.getChildSelectorMapping(tagString + "#" + idName, false);
                if (childMapping != null) {
                    this.getStyles(childMapping, tempVector, tags, ids, classes, 1, numElements, alreadyChecked);
                }
                if ((childMapping = mapping.getChildSelectorMapping("#" + idName, false)) != null) {
                    this.getStyles(childMapping, tempVector, tags, ids, classes, 1, numElements, alreadyChecked);
                }
            }
            int numLinkedSS = this.linkedStyleSheets != null ? this.linkedStyleSheets.size() : 0;
            int numStyles = tempVector.size();
            AttributeSet[] attrs = new AttributeSet[numStyles + numLinkedSS];
            for (counter = 0; counter < numStyles; ++counter) {
                attrs[counter] = ((SelectorMapping)tempVector.elementAt(counter)).getStyle();
            }
            for (counter = 0; counter < numLinkedSS; ++counter) {
                Style attr = this.linkedStyleSheets.elementAt(counter).getRule(selector);
                attrs[counter + numStyles] = attr == null ? SimpleAttributeSet.EMPTY : attr;
            }
            ResolvedStyle retStyle = new ResolvedStyle(selector, attrs, numStyles);
            this.resolvedStyles.put(selector, retStyle);
            ResolvedStyle resolvedStyle = retStyle;
            return resolvedStyle;
        }
        finally {
            SearchBuffer.releaseSearchBuffer(sb);
        }
    }

    private Style createResolvedStyle(String selector, Vector<Element> elements, HTML.Tag t) {
        int numElements = elements.size();
        String[] tags = new String[numElements];
        String[] ids = new String[numElements];
        String[] classes = new String[numElements];
        for (int counter = 0; counter < numElements; ++counter) {
            Element e = elements.elementAt(counter);
            AttributeSet attr = e.getAttributes();
            if (counter == 0 && e.isLeaf()) {
                Object testAttr = attr.getAttribute(t);
                attr = testAttr instanceof AttributeSet ? (AttributeSet)testAttr : null;
            }
            if (attr != null) {
                HTML.Tag tag = (HTML.Tag)attr.getAttribute(StyleConstants.NameAttribute);
                tags[counter] = tag != null ? tag.toString() : null;
                classes[counter] = attr.isDefined(HTML.Attribute.CLASS) ? attr.getAttribute(HTML.Attribute.CLASS).toString() : null;
                if (attr.isDefined(HTML.Attribute.ID)) {
                    ids[counter] = attr.getAttribute(HTML.Attribute.ID).toString();
                    continue;
                }
                ids[counter] = null;
                continue;
            }
            classes[counter] = null;
            ids[counter] = null;
            tags[counter] = null;
        }
        tags[0] = t.toString();
        return this.createResolvedStyle(selector, tags, ids, classes);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Style createResolvedStyle(String selector) {
        SearchBuffer sb = SearchBuffer.obtainSearchBuffer();
        Vector elements = sb.getVector();
        try {
            int dotIndex = 0;
            int poundIndex = 0;
            int lastIndex = 0;
            int length = selector.length();
            while (lastIndex < length) {
                int spaceIndex;
                if (dotIndex == lastIndex) {
                    dotIndex = selector.indexOf(46, lastIndex);
                }
                if (poundIndex == lastIndex) {
                    poundIndex = selector.indexOf(35, lastIndex);
                }
                if ((spaceIndex = selector.indexOf(32, lastIndex)) == -1) {
                    spaceIndex = length;
                }
                if (dotIndex != -1 && poundIndex != -1 && dotIndex < spaceIndex && poundIndex < spaceIndex) {
                    if (poundIndex < dotIndex) {
                        if (lastIndex == poundIndex) {
                            elements.addElement("");
                        } else {
                            elements.addElement(selector.substring(lastIndex, poundIndex));
                        }
                        if (dotIndex + 1 < spaceIndex) {
                            elements.addElement(selector.substring(dotIndex + 1, spaceIndex));
                        } else {
                            elements.addElement(null);
                        }
                        if (poundIndex + 1 == dotIndex) {
                            elements.addElement(null);
                        } else {
                            elements.addElement(selector.substring(poundIndex + 1, dotIndex));
                        }
                    } else if (poundIndex < spaceIndex) {
                        if (lastIndex == dotIndex) {
                            elements.addElement("");
                        } else {
                            elements.addElement(selector.substring(lastIndex, dotIndex));
                        }
                        if (dotIndex + 1 < poundIndex) {
                            elements.addElement(selector.substring(dotIndex + 1, poundIndex));
                        } else {
                            elements.addElement(null);
                        }
                        if (poundIndex + 1 == spaceIndex) {
                            elements.addElement(null);
                        } else {
                            elements.addElement(selector.substring(poundIndex + 1, spaceIndex));
                        }
                    }
                    dotIndex = poundIndex = spaceIndex + 1;
                } else if (dotIndex != -1 && dotIndex < spaceIndex) {
                    if (dotIndex == lastIndex) {
                        elements.addElement("");
                    } else {
                        elements.addElement(selector.substring(lastIndex, dotIndex));
                    }
                    if (dotIndex + 1 == spaceIndex) {
                        elements.addElement(null);
                    } else {
                        elements.addElement(selector.substring(dotIndex + 1, spaceIndex));
                    }
                    elements.addElement(null);
                    dotIndex = spaceIndex + 1;
                } else if (poundIndex != -1 && poundIndex < spaceIndex) {
                    if (poundIndex == lastIndex) {
                        elements.addElement("");
                    } else {
                        elements.addElement(selector.substring(lastIndex, poundIndex));
                    }
                    elements.addElement(null);
                    if (poundIndex + 1 == spaceIndex) {
                        elements.addElement(null);
                    } else {
                        elements.addElement(selector.substring(poundIndex + 1, spaceIndex));
                    }
                    poundIndex = spaceIndex + 1;
                } else {
                    elements.addElement(selector.substring(lastIndex, spaceIndex));
                    elements.addElement(null);
                    elements.addElement(null);
                }
                lastIndex = spaceIndex + 1;
            }
            int total = elements.size();
            int numTags = total / 3;
            String[] tags = new String[numTags];
            String[] ids = new String[numTags];
            String[] classes = new String[numTags];
            int index = 0;
            int eIndex = total - 3;
            while (index < numTags) {
                tags[index] = (String)elements.elementAt(eIndex);
                classes[index] = (String)elements.elementAt(eIndex + 1);
                ids[index] = (String)elements.elementAt(eIndex + 2);
                ++index;
                eIndex -= 3;
            }
            Style style = this.createResolvedStyle(selector, tags, ids, classes);
            return style;
        }
        finally {
            SearchBuffer.releaseSearchBuffer(sb);
        }
    }

    private synchronized void refreshResolvedRules(String selectorName, String[] selector, Style newStyle, int specificity) {
        if (this.resolvedStyles.size() > 0) {
            Enumeration<ResolvedStyle> values = this.resolvedStyles.elements();
            while (values.hasMoreElements()) {
                ResolvedStyle style = values.nextElement();
                if (!style.matches(selectorName)) continue;
                style.insertStyle(newStyle, specificity);
            }
        }
    }

    private Object fontSizeInherit() {
        if (this.fontSizeInherit == null) {
            this.fontSizeInherit = this.css.new CSS.FontSize().parseCssValue("100%");
        }
        return this.fontSizeInherit;
    }

    void rebaseSizeMap(int base) {
        int minimalFontSize = 4;
        this.sizeMap = new int[sizeMapDefault.length];
        for (int i = 0; i < sizeMapDefault.length; ++i) {
            this.sizeMap[i] = Math.max(base * sizeMapDefault[i] / sizeMapDefault[CSS.baseFontSizeIndex], 4);
        }
    }

    int[] getSizeMap() {
        return this.sizeMap;
    }

    boolean isW3CLengthUnits() {
        return this.w3cLengthUnits;
    }

    static class SelectorMapping
    implements Serializable {
        private int specificity;
        private Style style;
        private HashMap<String, SelectorMapping> children;

        public SelectorMapping(int specificity) {
            this.specificity = specificity;
        }

        public int getSpecificity() {
            return this.specificity;
        }

        public void setStyle(Style style) {
            this.style = style;
        }

        public Style getStyle() {
            return this.style;
        }

        public SelectorMapping getChildSelectorMapping(String selector, boolean create) {
            SelectorMapping retValue = null;
            if (this.children != null) {
                retValue = this.children.get(selector);
            } else if (create) {
                this.children = new HashMap(7);
            }
            if (retValue == null && create) {
                int specificity = this.getChildSpecificity(selector);
                retValue = this.createChildSelectorMapping(specificity);
                this.children.put(selector, retValue);
            }
            return retValue;
        }

        protected SelectorMapping createChildSelectorMapping(int specificity) {
            return new SelectorMapping(specificity);
        }

        protected int getChildSpecificity(String selector) {
            char firstChar = selector.charAt(0);
            int specificity = this.getSpecificity();
            if (firstChar == '.') {
                specificity += 100;
            } else if (firstChar == '#') {
                specificity += 10000;
            } else {
                ++specificity;
                if (selector.indexOf(46) != -1) {
                    specificity += 100;
                }
                if (selector.indexOf(35) != -1) {
                    specificity += 10000;
                }
            }
            return specificity;
        }
    }

    private static class SearchBuffer {
        static Stack<SearchBuffer> searchBuffers = new Stack();
        Vector vector = null;
        StringBuffer stringBuffer = null;
        HashSet<SelectorMapping> hashSet = null;

        private SearchBuffer() {
        }

        static SearchBuffer obtainSearchBuffer() {
            SearchBuffer sb;
            try {
                sb = !searchBuffers.empty() ? searchBuffers.pop() : new SearchBuffer();
            }
            catch (EmptyStackException ese) {
                sb = new SearchBuffer();
            }
            return sb;
        }

        static void releaseSearchBuffer(SearchBuffer sb) {
            sb.empty();
            searchBuffers.push(sb);
        }

        StringBuffer getStringBuffer() {
            if (this.stringBuffer == null) {
                this.stringBuffer = new StringBuffer();
            }
            return this.stringBuffer;
        }

        Vector getVector() {
            if (this.vector == null) {
                this.vector = new Vector();
            }
            return this.vector;
        }

        HashSet<SelectorMapping> getHashSet() {
            if (this.hashSet == null) {
                this.hashSet = new HashSet();
            }
            return this.hashSet;
        }

        void empty() {
            if (this.stringBuffer != null) {
                this.stringBuffer.setLength(0);
            }
            if (this.vector != null) {
                this.vector.removeAllElements();
            }
            if (this.hashSet != null) {
                this.hashSet.clear();
            }
        }
    }

    class CssParser
    implements CSSParser.CSSParserCallback {
        Vector<String[]> selectors = new Vector();
        Vector<String> selectorTokens = new Vector();
        String propertyName;
        MutableAttributeSet declaration = new SimpleAttributeSet();
        boolean parsingDeclaration;
        boolean isLink;
        URL base;
        CSSParser parser = new CSSParser();

        CssParser() {
        }

        public AttributeSet parseDeclaration(String string) {
            try {
                return this.parseDeclaration(new StringReader(string));
            }
            catch (IOException iOException) {
                return null;
            }
        }

        public AttributeSet parseDeclaration(Reader r) throws IOException {
            this.parse(this.base, r, true, false);
            return this.declaration.copyAttributes();
        }

        public void parse(URL base, Reader r, boolean parseDeclaration, boolean isLink) throws IOException {
            this.base = base;
            this.isLink = isLink;
            this.parsingDeclaration = parseDeclaration;
            this.declaration.removeAttributes(this.declaration);
            this.selectorTokens.removeAllElements();
            this.selectors.removeAllElements();
            this.propertyName = null;
            this.parser.parse(r, this, parseDeclaration);
        }

        @Override
        public void handleImport(String importString) {
            URL url = CSS.getURL(this.base, importString);
            if (url != null) {
                StyleSheet.this.importStyleSheet(url);
            }
        }

        @Override
        public void handleSelector(String selector) {
            if (!selector.startsWith(".") && !selector.startsWith("#")) {
                selector = selector.toLowerCase();
            }
            int length = selector.length();
            if (selector.endsWith(",")) {
                if (length > 1) {
                    selector = selector.substring(0, length - 1);
                    this.selectorTokens.addElement(selector);
                }
                this.addSelector();
            } else if (length > 0) {
                this.selectorTokens.addElement(selector);
            }
        }

        @Override
        public void startRule() {
            if (this.selectorTokens.size() > 0) {
                this.addSelector();
            }
            this.propertyName = null;
        }

        @Override
        public void handleProperty(String property) {
            this.propertyName = property;
        }

        @Override
        public void handleValue(String value) {
            if (this.propertyName != null && value != null && value.length() > 0) {
                CSS.Attribute cssKey = CSS.getAttribute(this.propertyName);
                if (cssKey != null) {
                    URL url;
                    if (cssKey == CSS.Attribute.LIST_STYLE_IMAGE && value != null && !value.equals("none") && (url = CSS.getURL(this.base, value)) != null) {
                        value = url.toString();
                    }
                    StyleSheet.this.addCSSAttribute(this.declaration, cssKey, value);
                }
                this.propertyName = null;
            }
        }

        @Override
        public void endRule() {
            int n = this.selectors.size();
            for (int i = 0; i < n; ++i) {
                String[] selector = this.selectors.elementAt(i);
                if (selector.length <= 0) continue;
                StyleSheet.this.addRule(selector, this.declaration, this.isLink);
            }
            this.declaration.removeAttributes(this.declaration);
            this.selectors.removeAllElements();
        }

        private void addSelector() {
            Object[] selector = new String[this.selectorTokens.size()];
            this.selectorTokens.copyInto(selector);
            this.selectors.addElement((String[])selector);
            this.selectorTokens.removeAllElements();
        }
    }

    class ViewAttributeSet
    extends MuxingAttributeSet {
        View host;

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        ViewAttributeSet(View v) {
            this.host = v;
            Document doc = v.getDocument();
            SearchBuffer sb = SearchBuffer.obtainSearchBuffer();
            Vector muxList = sb.getVector();
            try {
                if (doc instanceof HTMLDocument) {
                    StyleSheet styles = StyleSheet.this;
                    Element elem = v.getElement();
                    AttributeSet a = elem.getAttributes();
                    AttributeSet htmlAttr = styles.translateHTMLToCSS(a);
                    if (htmlAttr.getAttributeCount() != 0) {
                        muxList.addElement(htmlAttr);
                    }
                    if (elem.isLeaf()) {
                        Enumeration<?> keys = a.getAttributeNames();
                        while (keys.hasMoreElements()) {
                            Style cssRule;
                            AttributeSet attr;
                            Object o;
                            Object key = keys.nextElement();
                            if (!(key instanceof HTML.Tag) || key == HTML.Tag.A && (o = a.getAttribute(key)) instanceof AttributeSet && (attr = (AttributeSet)o).getAttribute(HTML.Attribute.HREF) == null || (cssRule = styles.getRule((HTML.Tag)key, elem)) == null) continue;
                            muxList.addElement(cssRule);
                        }
                    } else {
                        HTML.Tag t = (HTML.Tag)a.getAttribute(StyleConstants.NameAttribute);
                        Style cssRule = styles.getRule(t, elem);
                        if (cssRule != null) {
                            muxList.addElement(cssRule);
                        }
                    }
                }
                Object[] attrs = new AttributeSet[muxList.size()];
                muxList.copyInto(attrs);
                this.setAttributes((AttributeSet[])attrs);
            }
            finally {
                SearchBuffer.releaseSearchBuffer(sb);
            }
        }

        @Override
        public boolean isDefined(Object key) {
            CSS.Attribute cssKey;
            if (key instanceof StyleConstants && (cssKey = StyleSheet.this.css.styleConstantsKeyToCSSKey((StyleConstants)key)) != null) {
                key = cssKey;
            }
            return super.isDefined(key);
        }

        @Override
        public Object getAttribute(Object key) {
            CSS.Attribute cssKey;
            if (key instanceof StyleConstants && (cssKey = StyleSheet.this.css.styleConstantsKeyToCSSKey((StyleConstants)key)) != null) {
                Object value = this.doGetAttribute(cssKey);
                if (value instanceof CSS.FontSize) {
                    return ((CSS.FontSize)value).getValue(this, StyleSheet.this);
                }
                if (value instanceof CSS.CssValue) {
                    return ((CSS.CssValue)value).toStyleConstants((StyleConstants)key, this.host);
                }
            }
            return this.doGetAttribute(key);
        }

        private Object getTextDecoration(Object value) {
            AttributeSet parent = this.getResolveParent();
            if (parent == null) {
                return value;
            }
            Object parentValue = parent.getAttribute(CSS.Attribute.TEXT_DECORATION);
            return parentValue == null ? value : CSS.mergeTextDecoration(String.valueOf(value) + "," + String.valueOf(parentValue));
        }

        Object doGetAttribute(Object key) {
            AttributeSet parent;
            CSS.Attribute css;
            Object retValue = super.getAttribute(key);
            if (retValue != null) {
                if (key != CSS.Attribute.TEXT_DECORATION) {
                    return retValue;
                }
                return this.getTextDecoration(retValue);
            }
            if (key == CSS.Attribute.FONT_SIZE) {
                return StyleSheet.this.fontSizeInherit();
            }
            if (key instanceof CSS.Attribute && (css = (CSS.Attribute)key).isInherited() && (parent = this.getResolveParent()) != null) {
                return parent.getAttribute(key);
            }
            return null;
        }

        @Override
        public AttributeSet getResolveParent() {
            if (this.host == null) {
                return null;
            }
            View parent = this.host.getParent();
            return parent != null ? parent.getAttributes() : null;
        }
    }

    static class ResolvedStyle
    extends MuxingAttributeSet
    implements Serializable,
    Style {
        String name;
        private int extendedIndex;

        ResolvedStyle(String name, AttributeSet[] attrs, int extendedIndex) {
            super(attrs);
            this.name = name;
            this.extendedIndex = extendedIndex;
        }

        synchronized void insertStyle(Style style, int specificity) {
            int counter;
            AttributeSet[] attrs = this.getAttributes();
            int maxCounter = attrs.length;
            for (counter = 0; counter < this.extendedIndex && specificity <= StyleSheet.getSpecificity(((Style)attrs[counter]).getName()); ++counter) {
            }
            this.insertAttributeSetAt(style, counter);
            ++this.extendedIndex;
        }

        synchronized void removeStyle(Style style) {
            AttributeSet[] attrs = this.getAttributes();
            for (int counter = attrs.length - 1; counter >= 0; --counter) {
                if (attrs[counter] != style) continue;
                this.removeAttributeSetAt(counter);
                if (counter >= this.extendedIndex) break;
                --this.extendedIndex;
                break;
            }
        }

        synchronized void insertExtendedStyleAt(Style attr, int index) {
            this.insertAttributeSetAt(attr, this.extendedIndex + index);
        }

        synchronized void addExtendedStyle(Style attr) {
            this.insertAttributeSetAt(attr, this.getAttributes().length);
        }

        synchronized void removeExtendedStyleAt(int index) {
            this.removeAttributeSetAt(this.extendedIndex + index);
        }

        protected boolean matches(String selector) {
            int sLast = selector.length();
            if (sLast == 0) {
                return false;
            }
            int thisLast = this.name.length();
            int sCurrent = selector.lastIndexOf(32);
            int thisCurrent = this.name.lastIndexOf(32);
            if (sCurrent >= 0) {
                ++sCurrent;
            }
            if (thisCurrent >= 0) {
                ++thisCurrent;
            }
            if (!this.matches(selector, sCurrent, sLast, thisCurrent, thisLast)) {
                return false;
            }
            while (sCurrent != -1) {
                sLast = sCurrent - 1;
                if ((sCurrent = selector.lastIndexOf(32, sLast - 1)) >= 0) {
                    ++sCurrent;
                }
                boolean match = false;
                while (!match && thisCurrent != -1) {
                    thisLast = thisCurrent - 1;
                    if ((thisCurrent = this.name.lastIndexOf(32, thisLast - 1)) >= 0) {
                        ++thisCurrent;
                    }
                    match = this.matches(selector, sCurrent, sLast, thisCurrent, thisLast);
                }
                if (match) continue;
                return false;
            }
            return true;
        }

        boolean matches(String selector, int sCurrent, int sLast, int thisCurrent, int thisLast) {
            sCurrent = Math.max(sCurrent, 0);
            thisCurrent = Math.max(thisCurrent, 0);
            int thisDotIndex = this.boundedIndexOf(this.name, '.', thisCurrent, thisLast);
            int thisPoundIndex = this.boundedIndexOf(this.name, '#', thisCurrent, thisLast);
            int sDotIndex = this.boundedIndexOf(selector, '.', sCurrent, sLast);
            int sPoundIndex = this.boundedIndexOf(selector, '#', sCurrent, sLast);
            if (sDotIndex != -1) {
                if (thisDotIndex == -1) {
                    return false;
                }
                return !(sCurrent == sDotIndex ? thisLast - thisDotIndex != sLast - sDotIndex || !selector.regionMatches(sCurrent, this.name, thisDotIndex, thisLast - thisDotIndex) : sLast - sCurrent != thisLast - thisCurrent || !selector.regionMatches(sCurrent, this.name, thisCurrent, thisLast - thisCurrent));
            }
            if (sPoundIndex != -1) {
                if (thisPoundIndex == -1) {
                    return false;
                }
                return !(sCurrent == sPoundIndex ? thisLast - thisPoundIndex != sLast - sPoundIndex || !selector.regionMatches(sCurrent, this.name, thisPoundIndex, thisLast - thisPoundIndex) : sLast - sCurrent != thisLast - thisCurrent || !selector.regionMatches(sCurrent, this.name, thisCurrent, thisLast - thisCurrent));
            }
            if (thisDotIndex != -1) {
                return thisDotIndex - thisCurrent == sLast - sCurrent && selector.regionMatches(sCurrent, this.name, thisCurrent, thisDotIndex - thisCurrent);
            }
            if (thisPoundIndex != -1) {
                return thisPoundIndex - thisCurrent == sLast - sCurrent && selector.regionMatches(sCurrent, this.name, thisCurrent, thisPoundIndex - thisCurrent);
            }
            return thisLast - thisCurrent == sLast - sCurrent && selector.regionMatches(sCurrent, this.name, thisCurrent, thisLast - thisCurrent);
        }

        int boundedIndexOf(String string, char search, int start, int end) {
            int retValue = string.indexOf(search, start);
            if (retValue >= end) {
                return -1;
            }
            return retValue;
        }

        @Override
        public void addAttribute(Object name, Object value) {
        }

        @Override
        public void addAttributes(AttributeSet attributes) {
        }

        @Override
        public void removeAttribute(Object name) {
        }

        @Override
        public void removeAttributes(Enumeration<?> names) {
        }

        @Override
        public void removeAttributes(AttributeSet attributes) {
        }

        @Override
        public void setResolveParent(AttributeSet parent) {
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

        public ChangeListener[] getChangeListeners() {
            return new ChangeListener[0];
        }
    }

    class SmallConversionSet
    extends StyleContext.SmallAttributeSet {
        public SmallConversionSet(AttributeSet attrs) {
            super((StyleContext)StyleSheet.this, attrs);
        }

        @Override
        public boolean isDefined(Object key) {
            CSS.Attribute cssKey;
            if (key instanceof StyleConstants && (cssKey = StyleSheet.this.css.styleConstantsKeyToCSSKey((StyleConstants)key)) != null) {
                return super.isDefined(cssKey);
            }
            return super.isDefined(key);
        }

        @Override
        public Object getAttribute(Object key) {
            Object value;
            CSS.Attribute cssKey;
            if (key instanceof StyleConstants && (cssKey = StyleSheet.this.css.styleConstantsKeyToCSSKey((StyleConstants)key)) != null && (value = super.getAttribute(cssKey)) != null) {
                return StyleSheet.this.css.cssValueToStyleConstantsValue((StyleConstants)key, value);
            }
            return super.getAttribute(key);
        }
    }

    class LargeConversionSet
    extends SimpleAttributeSet {
        public LargeConversionSet(AttributeSet source) {
            super(source);
        }

        public LargeConversionSet() {
        }

        @Override
        public boolean isDefined(Object key) {
            CSS.Attribute cssKey;
            if (key instanceof StyleConstants && (cssKey = StyleSheet.this.css.styleConstantsKeyToCSSKey((StyleConstants)key)) != null) {
                return super.isDefined(cssKey);
            }
            return super.isDefined(key);
        }

        @Override
        public Object getAttribute(Object key) {
            Object value;
            CSS.Attribute cssKey;
            if (key instanceof StyleConstants && (cssKey = StyleSheet.this.css.styleConstantsKeyToCSSKey((StyleConstants)key)) != null && (value = super.getAttribute(cssKey)) != null) {
                return StyleSheet.this.css.cssValueToStyleConstantsValue((StyleConstants)key, value);
            }
            return super.getAttribute(key);
        }
    }

    public static final class BoxPainter
    implements Serializable {
        float topMargin;
        float bottomMargin;
        float leftMargin;
        float rightMargin;
        short marginFlags;
        Border border;
        Insets binsets;
        CSS css;
        StyleSheet ss;
        Color bg;
        BackgroundImagePainter bgPainter;

        BoxPainter(AttributeSet a, CSS css, StyleSheet ss) {
            this.ss = ss;
            this.css = css;
            this.border = this.getBorder(a);
            this.binsets = this.border.getBorderInsets(null);
            this.topMargin = this.getLength(CSS.Attribute.MARGIN_TOP, a);
            this.bottomMargin = this.getLength(CSS.Attribute.MARGIN_BOTTOM, a);
            this.leftMargin = this.getLength(CSS.Attribute.MARGIN_LEFT, a);
            this.rightMargin = this.getLength(CSS.Attribute.MARGIN_RIGHT, a);
            this.bg = ss.getBackground(a);
            if (ss.getBackgroundImage(a) != null) {
                this.bgPainter = new BackgroundImagePainter(a, css, ss);
            }
        }

        Border getBorder(AttributeSet a) {
            return new CSSBorder(a);
        }

        Color getBorderColor(AttributeSet a) {
            Color color = this.css.getColor(a, CSS.Attribute.BORDER_COLOR);
            if (color == null && (color = this.css.getColor(a, CSS.Attribute.COLOR)) == null) {
                return Color.black;
            }
            return color;
        }

        public float getInset(int side, View v) {
            AttributeSet a = v.getAttributes();
            float inset = 0.0f;
            switch (side) {
                case 2: {
                    inset += this.getOrientationMargin(HorizontalMargin.LEFT, this.leftMargin, a, BoxPainter.isLeftToRight(v));
                    inset += (float)this.binsets.left;
                    inset += this.getLength(CSS.Attribute.PADDING_LEFT, a);
                    break;
                }
                case 4: {
                    inset += this.getOrientationMargin(HorizontalMargin.RIGHT, this.rightMargin, a, BoxPainter.isLeftToRight(v));
                    inset += (float)this.binsets.right;
                    inset += this.getLength(CSS.Attribute.PADDING_RIGHT, a);
                    break;
                }
                case 1: {
                    inset += this.topMargin;
                    inset += (float)this.binsets.top;
                    inset += this.getLength(CSS.Attribute.PADDING_TOP, a);
                    break;
                }
                case 3: {
                    inset += this.bottomMargin;
                    inset += (float)this.binsets.bottom;
                    inset += this.getLength(CSS.Attribute.PADDING_BOTTOM, a);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Invalid side: " + side);
                }
            }
            return inset;
        }

        public void paint(Graphics g, float x, float y, float w, float h, View v) {
            float dx = 0.0f;
            float dy = 0.0f;
            float dw = 0.0f;
            float dh = 0.0f;
            AttributeSet a = v.getAttributes();
            boolean isLeftToRight = BoxPainter.isLeftToRight(v);
            float localLeftMargin = this.getOrientationMargin(HorizontalMargin.LEFT, this.leftMargin, a, isLeftToRight);
            float localRightMargin = this.getOrientationMargin(HorizontalMargin.RIGHT, this.rightMargin, a, isLeftToRight);
            if (!(v instanceof HTMLEditorKit.HTMLFactory.BodyBlockView)) {
                dx = localLeftMargin;
                dy = this.topMargin;
                dw = -(localLeftMargin + localRightMargin);
                dh = -(this.topMargin + this.bottomMargin);
            }
            if (this.bg != null) {
                g.setColor(this.bg);
                g.fillRect((int)(x + dx), (int)(y + dy), (int)(w + dw), (int)(h + dh));
            }
            if (this.bgPainter != null) {
                this.bgPainter.paint(g, x + dx, y + dy, w + dw, h + dh, v);
            }
            x += localLeftMargin;
            y += this.topMargin;
            w -= localLeftMargin + localRightMargin;
            h -= this.topMargin + this.bottomMargin;
            if (this.border instanceof BevelBorder) {
                int bw = (int)this.getLength(CSS.Attribute.BORDER_TOP_WIDTH, a);
                for (int i = bw - 1; i >= 0; --i) {
                    this.border.paintBorder(null, g, (int)x + i, (int)y + i, (int)w - 2 * i, (int)h - 2 * i);
                }
            } else {
                this.border.paintBorder(null, g, (int)x, (int)y, (int)w, (int)h);
            }
        }

        float getLength(CSS.Attribute key, AttributeSet a) {
            return this.css.getLength(a, key, this.ss);
        }

        static boolean isLeftToRight(View v) {
            Container container;
            boolean ret = true;
            if (BoxPainter.isOrientationAware(v) && v != null && (container = v.getContainer()) != null) {
                ret = container.getComponentOrientation().isLeftToRight();
            }
            return ret;
        }

        static boolean isOrientationAware(View v) {
            Object obj;
            AttributeSet attr;
            boolean ret = false;
            if (v != null && (attr = v.getElement().getAttributes()) != null && (obj = attr.getAttribute(StyleConstants.NameAttribute)) instanceof HTML.Tag && (obj == HTML.Tag.DIR || obj == HTML.Tag.MENU || obj == HTML.Tag.UL || obj == HTML.Tag.OL)) {
                ret = true;
            }
            return ret;
        }

        float getOrientationMargin(HorizontalMargin side, float cssMargin, AttributeSet a, boolean isLeftToRight) {
            float margin = cssMargin;
            float orientationMargin = cssMargin;
            Object cssMarginValue = null;
            switch (side.ordinal()) {
                case 1: {
                    orientationMargin = isLeftToRight ? this.getLength(CSS.Attribute.MARGIN_RIGHT_LTR, a) : this.getLength(CSS.Attribute.MARGIN_RIGHT_RTL, a);
                    cssMarginValue = a.getAttribute(CSS.Attribute.MARGIN_RIGHT);
                    break;
                }
                case 0: {
                    orientationMargin = isLeftToRight ? this.getLength(CSS.Attribute.MARGIN_LEFT_LTR, a) : this.getLength(CSS.Attribute.MARGIN_LEFT_RTL, a);
                    cssMarginValue = a.getAttribute(CSS.Attribute.MARGIN_LEFT);
                }
            }
            if (cssMarginValue == null && orientationMargin != -2.1474836E9f) {
                margin = orientationMargin;
            }
            return margin;
        }

        static enum HorizontalMargin {
            LEFT,
            RIGHT;

        }
    }

    public static final class ListPainter
    implements Serializable {
        static final char[][] romanChars = new char[][]{{'i', 'v'}, {'x', 'l'}, {'c', 'd'}, {'m', '?'}};
        private Rectangle paintRect;
        private boolean checkedForStart;
        private int start;
        private CSS.Value type;
        private StyleSheet ss = null;
        Icon img = null;
        private int bulletgap = 5;
        private boolean isLeftToRight;

        ListPainter(AttributeSet attr, StyleSheet ss) {
            this.ss = ss;
            String imgstr = (String)attr.getAttribute(CSS.Attribute.LIST_STYLE_IMAGE);
            this.type = null;
            if (imgstr != null && !imgstr.equals("none")) {
                String tmpstr = null;
                try {
                    StringTokenizer st = new StringTokenizer(imgstr, "()");
                    if (st.hasMoreTokens()) {
                        tmpstr = st.nextToken();
                    }
                    if (st.hasMoreTokens()) {
                        tmpstr = st.nextToken();
                    }
                    URL u = new URL(tmpstr);
                    this.img = new ImageIcon(u);
                }
                catch (MalformedURLException e) {
                    if (tmpstr != null && ss != null && ss.getBase() != null) {
                        try {
                            URL u = new URL(ss.getBase(), tmpstr);
                            this.img = new ImageIcon(u);
                        }
                        catch (MalformedURLException murle) {
                            this.img = null;
                        }
                    }
                    this.img = null;
                }
            }
            if (this.img == null) {
                this.type = (CSS.Value)attr.getAttribute(CSS.Attribute.LIST_STYLE_TYPE);
            }
            this.start = 1;
            this.paintRect = new Rectangle();
        }

        private CSS.Value getChildType(View childView) {
            CSS.Value childtype = (CSS.Value)childView.getAttributes().getAttribute(CSS.Attribute.LIST_STYLE_TYPE);
            if (childtype == null) {
                if (this.type == null) {
                    View v = childView.getParent();
                    HTMLDocument doc = (HTMLDocument)v.getDocument();
                    childtype = HTMLDocument.matchNameAttribute(v.getElement().getAttributes(), HTML.Tag.OL) ? CSS.Value.DECIMAL : CSS.Value.DISC;
                } else {
                    childtype = this.type;
                }
            }
            return childtype;
        }

        private void getStart(View parent) {
            Object startValue;
            AttributeSet attr;
            this.checkedForStart = true;
            Element element = parent.getElement();
            if (element != null && (attr = element.getAttributes()) != null && attr.isDefined(HTML.Attribute.START) && (startValue = attr.getAttribute(HTML.Attribute.START)) != null && startValue instanceof String) {
                try {
                    this.start = Integer.parseInt((String)startValue);
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }
        }

        private int getRenderIndex(View parentView, int childIndex) {
            if (!this.checkedForStart) {
                this.getStart(parentView);
            }
            int retIndex = childIndex;
            for (int counter = childIndex; counter >= 0; --counter) {
                Object value;
                AttributeSet as = parentView.getElement().getElement(counter).getAttributes();
                if (as.getAttribute(StyleConstants.NameAttribute) != HTML.Tag.LI) {
                    --retIndex;
                    continue;
                }
                if (!as.isDefined(HTML.Attribute.VALUE) || !((value = as.getAttribute(HTML.Attribute.VALUE)) instanceof String)) continue;
                String s = (String)value;
                try {
                    int iValue = Integer.parseInt(s);
                    return retIndex - counter + iValue;
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }
            return retIndex + this.start;
        }

        public void paint(Graphics g, float x, float y, float w, float h, View v, int item) {
            View pView;
            Object cName;
            View cv = v.getView(item);
            Container host = v.getContainer();
            Object name = cv.getElement().getAttributes().getAttribute(StyleConstants.NameAttribute);
            if (!(name instanceof HTML.Tag) || name != HTML.Tag.LI) {
                return;
            }
            this.isLeftToRight = host.getComponentOrientation().isLeftToRight();
            float align = 0.0f;
            if (cv.getViewCount() > 0 && ((cName = (pView = cv.getView(0)).getElement().getAttributes().getAttribute(StyleConstants.NameAttribute)) == HTML.Tag.P || cName == HTML.Tag.IMPLIED) && pView.getViewCount() > 0) {
                this.paintRect.setBounds((int)x, (int)y, (int)w, (int)h);
                Shape shape = cv.getChildAllocation(0, this.paintRect);
                if (shape != null && (shape = pView.getView(0).getChildAllocation(0, shape)) != null) {
                    Rectangle rect = shape instanceof Rectangle ? (Rectangle)shape : shape.getBounds();
                    align = pView.getView(0).getAlignment(1);
                    y = rect.y;
                    h = rect.height;
                }
            }
            Color c = host.isEnabled() ? (this.ss != null ? this.ss.getForeground(cv.getAttributes()) : host.getForeground()) : UIManager.getColor("textInactiveText");
            g.setColor(c);
            if (this.img != null) {
                this.drawIcon(g, (int)x, (int)y, (int)w, (int)h, align, host);
                return;
            }
            CSS.Value childtype = this.getChildType(cv);
            Font font = ((StyledDocument)cv.getDocument()).getFont(cv.getAttributes());
            if (font != null) {
                g.setFont(font);
            }
            if (childtype == CSS.Value.SQUARE || childtype == CSS.Value.CIRCLE || childtype == CSS.Value.DISC) {
                this.drawShape(g, childtype, (int)x, (int)y, (int)w, (int)h, align);
            } else if (childtype == CSS.Value.DECIMAL) {
                this.drawLetter(g, '1', (int)x, (int)y, (int)w, (int)h, align, this.getRenderIndex(v, item));
            } else if (childtype == CSS.Value.LOWER_ALPHA) {
                this.drawLetter(g, 'a', (int)x, (int)y, (int)w, (int)h, align, this.getRenderIndex(v, item));
            } else if (childtype == CSS.Value.UPPER_ALPHA) {
                this.drawLetter(g, 'A', (int)x, (int)y, (int)w, (int)h, align, this.getRenderIndex(v, item));
            } else if (childtype == CSS.Value.LOWER_ROMAN) {
                this.drawLetter(g, 'i', (int)x, (int)y, (int)w, (int)h, align, this.getRenderIndex(v, item));
            } else if (childtype == CSS.Value.UPPER_ROMAN) {
                this.drawLetter(g, 'I', (int)x, (int)y, (int)w, (int)h, align, this.getRenderIndex(v, item));
            }
        }

        void drawIcon(Graphics g, int ax, int ay, int aw, int ah, float align, Component c) {
            int gap = this.isLeftToRight ? -(this.img.getIconWidth() + this.bulletgap) : aw + this.bulletgap;
            int x = ax + gap;
            int y = Math.max(ay, ay + (int)(align * (float)ah) - this.img.getIconHeight());
            this.img.paintIcon(c, g, x, y);
        }

        void drawShape(Graphics g, CSS.Value type, int ax, int ay, int aw, int ah, float align) {
            Object origAA = ((Graphics2D)g).getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = g.getFont().getSize();
            int gap = this.isLeftToRight ? -(this.bulletgap + size / 3) : aw + this.bulletgap;
            int x = ax + gap;
            int y = Math.max(ay, ay + (int)Math.ceil(ah / 2));
            if (type == CSS.Value.SQUARE) {
                g.drawRect(x, y, size / 3, size / 3);
            } else if (type == CSS.Value.CIRCLE) {
                g.drawOval(x, y, size / 3, size / 3);
            } else {
                g.fillOval(x, y, size / 3, size / 3);
            }
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, origAA);
        }

        void drawLetter(Graphics g, char letter, int ax, int ay, int aw, int ah, float align, int index) {
            Object str = this.formatItemNum(index, letter);
            str = this.isLeftToRight ? (String)str + "." : "." + (String)str;
            FontMetrics fm = SwingUtilities2.getFontMetrics(null, g);
            int stringwidth = SwingUtilities2.stringWidth(null, fm, (String)str);
            int gap = this.isLeftToRight ? -(stringwidth + this.bulletgap) : aw + this.bulletgap;
            int x = ax + gap;
            int y = Math.max(ay + fm.getAscent(), ay + (int)((float)ah * align));
            SwingUtilities2.drawString(null, g, (String)str, x, y);
        }

        String formatItemNum(int itemNum, char type) {
            String formattedNum;
            String numStyle = "1";
            boolean uppercase = false;
            switch (type) {
                default: {
                    formattedNum = String.valueOf(itemNum);
                    break;
                }
                case 'A': {
                    uppercase = true;
                }
                case 'a': {
                    formattedNum = this.formatAlphaNumerals(itemNum);
                    break;
                }
                case 'I': {
                    uppercase = true;
                }
                case 'i': {
                    formattedNum = this.formatRomanNumerals(itemNum);
                }
            }
            if (uppercase) {
                formattedNum = formattedNum.toUpperCase();
            }
            return formattedNum;
        }

        String formatAlphaNumerals(int itemNum) {
            Object result = itemNum > 26 ? this.formatAlphaNumerals(itemNum / 26) + this.formatAlphaNumerals(itemNum % 26) : String.valueOf((char)(97 + itemNum - 1));
            return result;
        }

        String formatRomanNumerals(int num) {
            return this.formatRomanNumerals(0, num);
        }

        String formatRomanNumerals(int level, int num) {
            if (num < 10) {
                return this.formatRomanDigit(level, num);
            }
            return this.formatRomanNumerals(level + 1, num / 10) + this.formatRomanDigit(level, num % 10);
        }

        String formatRomanDigit(int level, int digit) {
            Object result = "";
            if (digit == 9) {
                result = (String)result + romanChars[level][0];
                result = (String)result + romanChars[level + 1][0];
                return result;
            }
            if (digit == 4) {
                result = (String)result + romanChars[level][0];
                result = (String)result + romanChars[level][1];
                return result;
            }
            if (digit >= 5) {
                result = (String)result + romanChars[level][1];
                digit -= 5;
            }
            for (int i = 0; i < digit; ++i) {
                result = (String)result + romanChars[level][0];
            }
            return result;
        }
    }

    static class BackgroundImagePainter
    implements Serializable {
        ImageIcon backgroundImage;
        float hPosition;
        float vPosition;
        short flags;
        private int paintX;
        private int paintY;
        private int paintMaxX;
        private int paintMaxY;

        BackgroundImagePainter(AttributeSet a, CSS css, StyleSheet ss) {
            CSS.Value repeats;
            this.backgroundImage = ss.getBackgroundImage(a);
            CSS.BackgroundPosition pos = (CSS.BackgroundPosition)a.getAttribute(CSS.Attribute.BACKGROUND_POSITION);
            if (pos != null) {
                this.hPosition = pos.getHorizontalPosition();
                this.vPosition = pos.getVerticalPosition();
                if (pos.isHorizontalPositionRelativeToSize()) {
                    this.flags = (short)(this.flags | 4);
                } else if (pos.isHorizontalPositionRelativeToFontSize()) {
                    this.hPosition *= (float)CSS.getFontSize(a, 12, ss);
                }
                if (pos.isVerticalPositionRelativeToSize()) {
                    this.flags = (short)(this.flags | 8);
                } else if (pos.isVerticalPositionRelativeToFontSize()) {
                    this.vPosition *= (float)CSS.getFontSize(a, 12, ss);
                }
            }
            if ((repeats = (CSS.Value)a.getAttribute(CSS.Attribute.BACKGROUND_REPEAT)) == null || repeats == CSS.Value.BACKGROUND_REPEAT) {
                this.flags = (short)(this.flags | 3);
            } else if (repeats == CSS.Value.BACKGROUND_REPEAT_X) {
                this.flags = (short)(this.flags | 1);
            } else if (repeats == CSS.Value.BACKGROUND_REPEAT_Y) {
                this.flags = (short)(this.flags | 2);
            }
        }

        void paint(Graphics g, float x, float y, float w, float h, View v) {
            Rectangle clip = g.getClipRect();
            if (clip != null) {
                g.clipRect((int)x, (int)y, (int)w, (int)h);
            }
            if ((this.flags & 3) == 0) {
                int width = this.backgroundImage.getIconWidth();
                int height = this.backgroundImage.getIconWidth();
                this.paintX = (this.flags & 4) == 4 ? (int)(x + w * this.hPosition - (float)width * this.hPosition) : (int)x + (int)this.hPosition;
                this.paintY = (this.flags & 8) == 8 ? (int)(y + h * this.vPosition - (float)height * this.vPosition) : (int)y + (int)this.vPosition;
                if (clip == null || this.paintX + width > clip.x && this.paintY + height > clip.y && this.paintX < clip.x + clip.width && this.paintY < clip.y + clip.height) {
                    this.backgroundImage.paintIcon(null, g, this.paintX, this.paintY);
                }
            } else {
                int width = this.backgroundImage.getIconWidth();
                int height = this.backgroundImage.getIconHeight();
                if (width > 0 && height > 0) {
                    this.paintX = (int)x;
                    this.paintY = (int)y;
                    this.paintMaxX = (int)(x + w);
                    this.paintMaxY = (int)(y + h);
                    if (this.updatePaintCoordinates(clip, width, height)) {
                        while (this.paintX < this.paintMaxX) {
                            for (int ySpot = this.paintY; ySpot < this.paintMaxY; ySpot += height) {
                                this.backgroundImage.paintIcon(null, g, this.paintX, ySpot);
                            }
                            this.paintX += width;
                        }
                    }
                }
            }
            if (clip != null) {
                g.setClip(clip.x, clip.y, clip.width, clip.height);
            }
        }

        private boolean updatePaintCoordinates(Rectangle clip, int width, int height) {
            if ((this.flags & 3) == 1) {
                this.paintMaxY = this.paintY + 1;
            } else if ((this.flags & 3) == 2) {
                this.paintMaxX = this.paintX + 1;
            }
            if (clip != null) {
                if ((this.flags & 3) == 1 && (this.paintY + height <= clip.y || this.paintY > clip.y + clip.height)) {
                    return false;
                }
                if ((this.flags & 3) == 2 && (this.paintX + width <= clip.x || this.paintX > clip.x + clip.width)) {
                    return false;
                }
                if ((this.flags & 1) == 1) {
                    if (clip.x + clip.width < this.paintMaxX) {
                        this.paintMaxX = (clip.x + clip.width - this.paintX) % width == 0 ? clip.x + clip.width : ((clip.x + clip.width - this.paintX) / width + 1) * width + this.paintX;
                    }
                    if (clip.x > this.paintX) {
                        this.paintX = (clip.x - this.paintX) / width * width + this.paintX;
                    }
                }
                if ((this.flags & 2) == 2) {
                    if (clip.y + clip.height < this.paintMaxY) {
                        this.paintMaxY = (clip.y + clip.height - this.paintY) % height == 0 ? clip.y + clip.height : ((clip.y + clip.height - this.paintY) / height + 1) * height + this.paintY;
                    }
                    if (clip.y > this.paintY) {
                        this.paintY = (clip.y - this.paintY) / height * height + this.paintY;
                    }
                }
            }
            return true;
        }
    }
}

