/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.StringReader;
import java.net.URL;
import java.security.AccessController;
import javax.swing.JComponent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;
import javax.swing.text.html.StyleSheet;
import sun.security.action.GetBooleanAction;
import sun.swing.SwingAccessor;
import sun.swing.SwingUtilities2;

public class BasicHTML {
    private static final String htmlDisable = "html.disable";
    public static final String propertyKey = "html";
    public static final String documentBaseKey = "html.base";
    private static BasicEditorKit basicHTMLFactory;
    private static ViewFactory basicHTMLViewFactory;
    private static final String styleChanges = "p { margin-top: 0; margin-bottom: 0; margin-left: 0; margin-right: 0 }body { margin-top: 0; margin-bottom: 0; margin-left: 0; margin-right: 0 }";

    public static View createHTMLView(JComponent c, String html) {
        BasicEditorKit kit = BasicHTML.getFactory();
        Document doc = kit.createDefaultDocument(c.getFont(), c.getForeground());
        Object base = c.getClientProperty(documentBaseKey);
        if (base instanceof URL) {
            ((HTMLDocument)doc).setBase((URL)base);
        }
        StringReader r = new StringReader(html);
        try {
            kit.read(r, doc, 0);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        ViewFactory f = kit.getViewFactory();
        View hview = f.create(doc.getDefaultRootElement());
        Renderer v = new Renderer(c, f, hview);
        return v;
    }

    public static int getHTMLBaseline(View view, int w, int h) {
        if (w < 0 || h < 0) {
            throw new IllegalArgumentException("Width and height must be >= 0");
        }
        if (view instanceof Renderer) {
            return BasicHTML.getBaseline(view.getView(0), w, h);
        }
        return -1;
    }

    static int getBaseline(JComponent c, int y, int ascent, int w, int h) {
        View view = (View)c.getClientProperty(propertyKey);
        if (view != null) {
            int baseline = BasicHTML.getHTMLBaseline(view, w, h);
            if (baseline < 0) {
                return baseline;
            }
            return y + baseline;
        }
        return y + ascent;
    }

    static int getBaseline(View view, int w, int h) {
        if (BasicHTML.hasParagraph(view)) {
            view.setSize(w, h);
            return BasicHTML.getBaseline(view, new Rectangle(0, 0, w, h));
        }
        return -1;
    }

    private static int getBaseline(View view, Shape bounds) {
        if (view.getViewCount() == 0) {
            return -1;
        }
        AttributeSet attributes = view.getElement().getAttributes();
        Object name = null;
        if (attributes != null) {
            name = attributes.getAttribute(StyleConstants.NameAttribute);
        }
        int index = 0;
        if (name == HTML.Tag.HTML && view.getViewCount() > 1) {
            ++index;
        }
        if ((bounds = view.getChildAllocation(index, bounds)) == null) {
            return -1;
        }
        View child = view.getView(index);
        if (view instanceof ParagraphView) {
            Rectangle rect = bounds instanceof Rectangle ? (Rectangle)bounds : bounds.getBounds();
            return rect.y + (int)((float)rect.height * child.getAlignment(1));
        }
        return BasicHTML.getBaseline(child, bounds);
    }

    private static boolean hasParagraph(View view) {
        if (view instanceof ParagraphView) {
            return true;
        }
        if (view.getViewCount() == 0) {
            return false;
        }
        AttributeSet attributes = view.getElement().getAttributes();
        Object name = null;
        if (attributes != null) {
            name = attributes.getAttribute(StyleConstants.NameAttribute);
        }
        int index = 0;
        if (name == HTML.Tag.HTML && view.getViewCount() > 1) {
            index = 1;
        }
        return BasicHTML.hasParagraph(view.getView(index));
    }

    public static boolean isHTMLString(String s) {
        if (s != null && s.length() >= 6 && s.charAt(0) == '<' && s.charAt(5) == '>') {
            String tag = s.substring(1, 5);
            return tag.equalsIgnoreCase(propertyKey);
        }
        return false;
    }

    public static void updateRenderer(JComponent c, String text) {
        View value = null;
        View oldValue = (View)c.getClientProperty(propertyKey);
        Boolean htmlDisabled = (Boolean)c.getClientProperty(htmlDisable);
        if (!Boolean.TRUE.equals(htmlDisabled) && BasicHTML.isHTMLString(text)) {
            value = BasicHTML.createHTMLView(c, text);
        }
        if (value != oldValue && oldValue != null) {
            for (int i = 0; i < oldValue.getViewCount(); ++i) {
                oldValue.getView(i).setParent(null);
            }
        }
        c.putClientProperty(propertyKey, value);
        String currentAccessibleNameProperty = (String)c.getClientProperty("AccessibleName");
        String previousParsedText = null;
        if (currentAccessibleNameProperty != null && oldValue != null) {
            try {
                previousParsedText = oldValue.getDocument().getText(0, oldValue.getDocument().getLength()).strip();
            }
            catch (BadLocationException badLocationException) {
                // empty catch block
            }
        }
        if (currentAccessibleNameProperty == null || currentAccessibleNameProperty.equals(previousParsedText)) {
            String parsedText = null;
            if (value != null) {
                try {
                    parsedText = value.getDocument().getText(0, value.getDocument().getLength()).strip();
                }
                catch (BadLocationException badLocationException) {
                    // empty catch block
                }
            }
            c.putClientProperty("AccessibleName", parsedText);
        }
    }

    static BasicEditorKit getFactory() {
        if (basicHTMLFactory == null) {
            basicHTMLViewFactory = new BasicHTMLViewFactory();
            basicHTMLFactory = new BasicEditorKit();
        }
        return basicHTMLFactory;
    }

    static class BasicEditorKit
    extends HTMLEditorKit {
        private static StyleSheet defaultStyles;

        BasicEditorKit() {
        }

        @Override
        public StyleSheet getStyleSheet() {
            if (defaultStyles == null) {
                defaultStyles = new StyleSheet();
                StringReader r = new StringReader(BasicHTML.styleChanges);
                try {
                    defaultStyles.loadRules(r, null);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                r.close();
                defaultStyles.addStyleSheet(super.getStyleSheet());
            }
            return defaultStyles;
        }

        public Document createDefaultDocument(Font defaultFont, Color foreground) {
            StyleSheet styles = this.getStyleSheet();
            StyleSheet ss = new StyleSheet();
            ss.addStyleSheet(styles);
            BasicDocument doc = new BasicDocument(ss, defaultFont, foreground);
            doc.setAsynchronousLoadPriority(Integer.MAX_VALUE);
            doc.setPreservesUnknownTags(false);
            return doc;
        }

        @Override
        public ViewFactory getViewFactory() {
            return basicHTMLViewFactory;
        }
    }

    static class Renderer
    extends View {
        private int width;
        private View view;
        private ViewFactory factory;
        private JComponent host;

        Renderer(JComponent c, ViewFactory f, View v) {
            super(null);
            this.host = c;
            this.factory = f;
            this.view = v;
            this.view.setParent(this);
            this.setSize(this.view.getPreferredSpan(0), this.view.getPreferredSpan(1));
        }

        @Override
        public AttributeSet getAttributes() {
            return null;
        }

        @Override
        public float getPreferredSpan(int axis) {
            if (axis == 0) {
                return this.width;
            }
            return this.view.getPreferredSpan(axis);
        }

        @Override
        public float getMinimumSpan(int axis) {
            return this.view.getMinimumSpan(axis);
        }

        @Override
        public float getMaximumSpan(int axis) {
            return 2.1474836E9f;
        }

        @Override
        public void preferenceChanged(View child, boolean width, boolean height) {
            this.host.revalidate();
            this.host.repaint();
        }

        @Override
        public float getAlignment(int axis) {
            return this.view.getAlignment(axis);
        }

        @Override
        public void paint(Graphics g, Shape allocation) {
            Rectangle alloc = allocation.getBounds();
            this.view.setSize(alloc.width, alloc.height);
            this.view.paint(g, allocation);
        }

        @Override
        public void setParent(View parent) {
            throw new Error("Can't set parent on root view");
        }

        @Override
        public int getViewCount() {
            return 1;
        }

        @Override
        public View getView(int n) {
            return this.view;
        }

        @Override
        public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
            return this.view.modelToView(pos, a, b);
        }

        @Override
        public Shape modelToView(int p0, Position.Bias b0, int p1, Position.Bias b1, Shape a) throws BadLocationException {
            return this.view.modelToView(p0, b0, p1, b1, a);
        }

        @Override
        public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
            return this.view.viewToModel(x, y, a, bias);
        }

        @Override
        public Document getDocument() {
            return this.view.getDocument();
        }

        @Override
        public int getStartOffset() {
            return this.view.getStartOffset();
        }

        @Override
        public int getEndOffset() {
            return this.view.getEndOffset();
        }

        @Override
        public Element getElement() {
            return this.view.getElement();
        }

        @Override
        public void setSize(float width, float height) {
            this.width = (int)width;
            this.view.setSize(width, height);
        }

        @Override
        public Container getContainer() {
            return this.host;
        }

        @Override
        public ViewFactory getViewFactory() {
            return this.factory;
        }
    }

    static class BasicHTMLViewFactory
    extends HTMLEditorKit.HTMLFactory {
        private static Boolean useOV = null;

        BasicHTMLViewFactory() {
        }

        @Override
        public View create(Element elem) {
            View view = null;
            try {
                BasicHTMLViewFactory.setAllowHTMLObject();
                view = super.create(elem);
            }
            finally {
                BasicHTMLViewFactory.clearAllowHTMLObject();
            }
            if (view instanceof ImageView) {
                ((ImageView)view).setLoadsSynchronously(true);
            }
            return view;
        }

        private static void setAllowHTMLObject() {
            if (useOV == null) {
                useOV = AccessController.doPrivileged(new GetBooleanAction("swing.html.object"));
            }
            SwingAccessor.setAllowHTMLObject(useOV);
        }

        private static void clearAllowHTMLObject() {
            SwingAccessor.setAllowHTMLObject(null);
        }
    }

    static class BasicDocument
    extends HTMLDocument {
        BasicDocument(StyleSheet s, Font defaultFont, Color foreground) {
            super(s);
            this.setPreservesUnknownTags(false);
            this.setFontAndColor(defaultFont, foreground);
        }

        private void setFontAndColor(Font font, Color fg) {
            this.getStyleSheet().addRule(SwingUtilities2.displayPropertiesToCSS(font, fg));
        }
    }
}

