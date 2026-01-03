/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.accessibility.AccessibleContext;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JViewport;
import javax.swing.SizeRequirements;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.plaf.TextUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.Highlighter;
import javax.swing.text.IconView;
import javax.swing.text.JTextComponent;
import javax.swing.text.LabelView;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TextAction;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.AccessibleHTML;
import javax.swing.text.html.BRView;
import javax.swing.text.html.BlockView;
import javax.swing.text.html.CSS;
import javax.swing.text.html.CommentView;
import javax.swing.text.html.FormView;
import javax.swing.text.html.FrameSetView;
import javax.swing.text.html.FrameView;
import javax.swing.text.html.HRuleView;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.text.html.HTMLWriter;
import javax.swing.text.html.HiddenTagView;
import javax.swing.text.html.ImageView;
import javax.swing.text.html.InlineView;
import javax.swing.text.html.IsindexView;
import javax.swing.text.html.LineView;
import javax.swing.text.html.ListView;
import javax.swing.text.html.Map;
import javax.swing.text.html.MinimalHTMLWriter;
import javax.swing.text.html.NoFramesView;
import javax.swing.text.html.ObjectView;
import javax.swing.text.html.ParagraphView;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.html.TableView;
import javax.swing.text.html.parser.ParserDelegator;
import sun.awt.AppContext;
import sun.swing.SwingAccessor;

public class HTMLEditorKit
extends StyledEditorKit
implements Accessible {
    private JEditorPane theEditor;
    public static final String DEFAULT_CSS = "default.css";
    private AccessibleContext accessibleContext;
    private static final Cursor MoveCursor = Cursor.getPredefinedCursor(12);
    private static final Cursor DefaultCursor = Cursor.getPredefinedCursor(0);
    private static final ViewFactory defaultFactory = new HTMLFactory();
    MutableAttributeSet input;
    private static final Object DEFAULT_STYLES_KEY = new Object();
    private LinkController linkHandler = new LinkController();
    private static Parser defaultParser = null;
    private Cursor defaultCursor = DefaultCursor;
    private Cursor linkCursor = MoveCursor;
    private boolean isAutoFormSubmission = true;
    public static final String BOLD_ACTION = "html-bold-action";
    public static final String ITALIC_ACTION = "html-italic-action";
    public static final String PARA_INDENT_LEFT = "html-para-indent-left";
    public static final String PARA_INDENT_RIGHT = "html-para-indent-right";
    public static final String FONT_CHANGE_BIGGER = "html-font-bigger";
    public static final String FONT_CHANGE_SMALLER = "html-font-smaller";
    public static final String COLOR_ACTION = "html-color-action";
    public static final String LOGICAL_STYLE_ACTION = "html-logical-style-action";
    public static final String IMG_ALIGN_TOP = "html-image-align-top";
    public static final String IMG_ALIGN_MIDDLE = "html-image-align-middle";
    public static final String IMG_ALIGN_BOTTOM = "html-image-align-bottom";
    public static final String IMG_BORDER = "html-image-border";
    private static final String INSERT_TABLE_HTML = "<table border=1><tr><td></td></tr></table>";
    private static final String INSERT_UL_HTML = "<ul><li></li></ul>";
    private static final String INSERT_OL_HTML = "<ol><li></li></ol>";
    private static final String INSERT_HR_HTML = "<hr>";
    private static final String INSERT_PRE_HTML = "<pre></pre>";
    private static final NavigateLinkAction nextLinkAction = new NavigateLinkAction("next-link-action");
    private static final NavigateLinkAction previousLinkAction = new NavigateLinkAction("previous-link-action");
    private static final ActivateLinkAction activateLinkAction = new ActivateLinkAction("activate-link-action");
    private static final Action[] defaultActions = new Action[]{new InsertHTMLTextAction("InsertTable", "<table border=1><tr><td></td></tr></table>", HTML.Tag.BODY, HTML.Tag.TABLE), new InsertHTMLTextAction("InsertTableRow", "<table border=1><tr><td></td></tr></table>", HTML.Tag.TABLE, HTML.Tag.TR, HTML.Tag.BODY, HTML.Tag.TABLE), new InsertHTMLTextAction("InsertTableDataCell", "<table border=1><tr><td></td></tr></table>", HTML.Tag.TR, HTML.Tag.TD, HTML.Tag.BODY, HTML.Tag.TABLE), new InsertHTMLTextAction("InsertUnorderedList", "<ul><li></li></ul>", HTML.Tag.BODY, HTML.Tag.UL), new InsertHTMLTextAction("InsertUnorderedListItem", "<ul><li></li></ul>", HTML.Tag.UL, HTML.Tag.LI, HTML.Tag.BODY, HTML.Tag.UL), new InsertHTMLTextAction("InsertOrderedList", "<ol><li></li></ol>", HTML.Tag.BODY, HTML.Tag.OL), new InsertHTMLTextAction("InsertOrderedListItem", "<ol><li></li></ol>", HTML.Tag.OL, HTML.Tag.LI, HTML.Tag.BODY, HTML.Tag.OL), new InsertHRAction(), new InsertHTMLTextAction("InsertPre", "<pre></pre>", HTML.Tag.BODY, HTML.Tag.PRE), nextLinkAction, previousLinkAction, activateLinkAction, new BeginAction("caret-begin", false), new BeginAction("selection-begin", true)};
    private boolean foundLink = false;
    private int prevHypertextOffset = -1;
    private Object linkNavigationTag;

    @Override
    public String getContentType() {
        return "text/html";
    }

    @Override
    public ViewFactory getViewFactory() {
        return defaultFactory;
    }

    @Override
    public Document createDefaultDocument() {
        StyleSheet styles = this.getStyleSheet();
        StyleSheet ss = new StyleSheet();
        ss.addStyleSheet(styles);
        HTMLDocument doc = new HTMLDocument(ss);
        doc.setParser(this.getParser());
        doc.setAsynchronousLoadPriority(4);
        doc.setTokenThreshold(100);
        return doc;
    }

    private Parser ensureParser(HTMLDocument doc) throws IOException {
        Parser p = doc.getParser();
        if (p == null) {
            p = this.getParser();
        }
        if (p == null) {
            throw new IOException("Can't load parser");
        }
        return p;
    }

    @Override
    public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {
        if (doc instanceof HTMLDocument) {
            HTMLDocument hdoc = (HTMLDocument)doc;
            if (pos > doc.getLength()) {
                throw new BadLocationException("Invalid location", pos);
            }
            Parser p = this.ensureParser(hdoc);
            ParserCallback receiver = hdoc.getReader(pos);
            Boolean ignoreCharset = (Boolean)doc.getProperty("IgnoreCharsetDirective");
            p.parse(in, receiver, ignoreCharset == null ? false : ignoreCharset);
            receiver.flush();
        } else {
            super.read(in, doc, pos);
        }
    }

    public void insertHTML(HTMLDocument doc, int offset, String html, int popDepth, int pushDepth, HTML.Tag insertTag) throws BadLocationException, IOException {
        if (offset > doc.getLength()) {
            throw new BadLocationException("Invalid location", offset);
        }
        Parser p = this.ensureParser(doc);
        ParserCallback receiver = doc.getReader(offset, popDepth, pushDepth, insertTag);
        Boolean ignoreCharset = (Boolean)doc.getProperty("IgnoreCharsetDirective");
        p.parse(new StringReader(html), receiver, ignoreCharset == null ? false : ignoreCharset);
        receiver.flush();
    }

    @Override
    public void write(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {
        if (doc instanceof HTMLDocument) {
            HTMLWriter w = new HTMLWriter(out, (HTMLDocument)doc, pos, len);
            w.write();
        } else if (doc instanceof StyledDocument) {
            MinimalHTMLWriter w = new MinimalHTMLWriter(out, (StyledDocument)doc, pos, len);
            w.write();
        } else {
            super.write(out, doc, pos, len);
        }
    }

    @Override
    public void install(JEditorPane c) {
        c.addMouseListener(this.linkHandler);
        c.addMouseMotionListener(this.linkHandler);
        c.addCaretListener(nextLinkAction);
        super.install(c);
        this.theEditor = c;
    }

    @Override
    public void deinstall(JEditorPane c) {
        c.removeMouseListener(this.linkHandler);
        c.removeMouseMotionListener(this.linkHandler);
        c.removeCaretListener(nextLinkAction);
        super.deinstall(c);
        this.theEditor = null;
    }

    public void setStyleSheet(StyleSheet s) {
        if (s == null) {
            AppContext.getAppContext().remove(DEFAULT_STYLES_KEY);
        } else {
            AppContext.getAppContext().put(DEFAULT_STYLES_KEY, s);
        }
    }

    public StyleSheet getStyleSheet() {
        AppContext appContext = AppContext.getAppContext();
        StyleSheet defaultStyles = (StyleSheet)appContext.get(DEFAULT_STYLES_KEY);
        if (defaultStyles == null) {
            defaultStyles = new StyleSheet();
            appContext.put(DEFAULT_STYLES_KEY, defaultStyles);
            try (InputStream is = HTMLEditorKit.getResourceAsStream(DEFAULT_CSS);
                 InputStreamReader isr = new InputStreamReader(is, StandardCharsets.ISO_8859_1);
                 BufferedReader r = new BufferedReader(isr);){
                defaultStyles.loadRules(r, null);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return defaultStyles;
    }

    static InputStream getResourceAsStream(final String name) {
        return AccessController.doPrivileged(new PrivilegedAction<InputStream>(){

            @Override
            public InputStream run() {
                return HTMLEditorKit.class.getResourceAsStream(name);
            }
        });
    }

    @Override
    public Action[] getActions() {
        return TextAction.augmentList(super.getActions(), defaultActions);
    }

    @Override
    protected void createInputAttributes(Element element, MutableAttributeSet set) {
        set.removeAttributes(set);
        set.addAttributes(element.getAttributes());
        set.removeAttribute(StyleConstants.ComposedTextAttribute);
        Object o = set.getAttribute(StyleConstants.NameAttribute);
        if (o instanceof HTML.Tag) {
            HTML.Tag tag = (HTML.Tag)o;
            if (tag == HTML.Tag.IMG) {
                set.removeAttribute(HTML.Attribute.SRC);
                set.removeAttribute(HTML.Attribute.HEIGHT);
                set.removeAttribute(HTML.Attribute.WIDTH);
                set.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
            } else if (tag == HTML.Tag.HR || tag == HTML.Tag.BR) {
                set.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
            } else if (tag == HTML.Tag.COMMENT) {
                set.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
                set.removeAttribute(HTML.Attribute.COMMENT);
            } else if (tag == HTML.Tag.INPUT) {
                set.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
                set.removeAttribute(HTML.Tag.INPUT);
            } else if (tag instanceof HTML.UnknownTag) {
                set.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
                set.removeAttribute(HTML.Attribute.ENDTAG);
            }
        }
    }

    @Override
    public MutableAttributeSet getInputAttributes() {
        if (this.input == null) {
            this.input = this.getStyleSheet().addStyle(null, null);
        }
        return this.input;
    }

    public void setDefaultCursor(Cursor cursor) {
        this.defaultCursor = cursor;
    }

    public Cursor getDefaultCursor() {
        return this.defaultCursor;
    }

    public void setLinkCursor(Cursor cursor) {
        this.linkCursor = cursor;
    }

    public Cursor getLinkCursor() {
        return this.linkCursor;
    }

    public boolean isAutoFormSubmission() {
        return this.isAutoFormSubmission;
    }

    public void setAutoFormSubmission(boolean isAuto) {
        this.isAutoFormSubmission = isAuto;
    }

    @Override
    public Object clone() {
        HTMLEditorKit o = (HTMLEditorKit)super.clone();
        if (o != null) {
            o.input = null;
            o.linkHandler = new LinkController();
        }
        return o;
    }

    protected Parser getParser() {
        if (defaultParser == null) {
            defaultParser = new ParserDelegator();
        }
        return defaultParser;
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (this.theEditor == null) {
            return null;
        }
        if (this.accessibleContext == null) {
            AccessibleHTML a = new AccessibleHTML(this.theEditor);
            this.accessibleContext = a.getAccessibleContext();
        }
        return this.accessibleContext;
    }

    private static Object getAttrValue(AttributeSet attr, HTML.Attribute key) {
        Enumeration<?> names = attr.getAttributeNames();
        while (names.hasMoreElements()) {
            Object nextKey = names.nextElement();
            Object nextVal = attr.getAttribute(nextKey);
            if (nextVal instanceof AttributeSet) {
                Object value = HTMLEditorKit.getAttrValue((AttributeSet)nextVal, key);
                if (value == null) continue;
                return value;
            }
            if (nextKey != key) continue;
            return nextVal;
        }
        return null;
    }

    private static int getBodyElementStart(JTextComponent comp) {
        Element rootElement = comp.getDocument().getRootElements()[0];
        for (int i = 0; i < rootElement.getElementCount(); ++i) {
            Element currElement = rootElement.getElement(i);
            if (!"body".equals(currElement.getName())) continue;
            return currElement.getStartOffset();
        }
        return 0;
    }

    public static class LinkController
    extends MouseAdapter
    implements MouseMotionListener,
    Serializable {
        private Element curElem = null;
        private boolean curElemImage = false;
        private String href = null;
        private transient Position.Bias[] bias = new Position.Bias[1];
        private int curOffset;

        @Override
        public void mouseClicked(MouseEvent e) {
            Point pt;
            int pos;
            JEditorPane editor = (JEditorPane)e.getSource();
            if (!editor.isEditable() && editor.isEnabled() && SwingUtilities.isLeftMouseButton(e) && (pos = editor.viewToModel(pt = new Point(e.getX(), e.getY()))) >= 0) {
                this.activateLink(pos, editor, e);
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            JEditorPane editor = (JEditorPane)e.getSource();
            if (!editor.isEnabled()) {
                return;
            }
            HTMLEditorKit kit = (HTMLEditorKit)editor.getEditorKit();
            boolean adjustCursor = true;
            Cursor newCursor = kit.getDefaultCursor();
            if (!editor.isEditable()) {
                Point pt = new Point(e.getX(), e.getY());
                int pos = editor.getUI().viewToModel(editor, pt, this.bias);
                if (this.bias[0] == Position.Bias.Backward && pos > 0) {
                    --pos;
                }
                if (pos >= 0 && editor.getDocument() instanceof HTMLDocument) {
                    HTMLDocument hdoc = (HTMLDocument)editor.getDocument();
                    Element elem = hdoc.getCharacterElement(pos);
                    if (!this.doesElementContainLocation(editor, elem, pos, e.getX(), e.getY())) {
                        elem = null;
                    }
                    if (this.curElem != elem || this.curElemImage) {
                        Element lastElem = this.curElem;
                        this.curElem = elem;
                        String href = null;
                        this.curElemImage = false;
                        if (elem != null) {
                            AttributeSet a = elem.getAttributes();
                            AttributeSet anchor = (AttributeSet)a.getAttribute(HTML.Tag.A);
                            if (anchor == null) {
                                boolean bl = this.curElemImage = a.getAttribute(StyleConstants.NameAttribute) == HTML.Tag.IMG;
                                if (this.curElemImage) {
                                    href = this.getMapHREF(editor, hdoc, elem, a, pos, e.getX(), e.getY());
                                }
                            } else {
                                href = (String)anchor.getAttribute(HTML.Attribute.HREF);
                            }
                        }
                        if (href != this.href) {
                            this.fireEvents(editor, hdoc, href, lastElem, e);
                            this.href = href;
                            if (href != null) {
                                newCursor = kit.getLinkCursor();
                            }
                        } else {
                            adjustCursor = false;
                        }
                    } else {
                        adjustCursor = false;
                    }
                    this.curOffset = pos;
                }
            }
            if (adjustCursor && editor.getCursor() != newCursor) {
                editor.setCursor(newCursor);
            }
        }

        private String getMapHREF(JEditorPane html, HTMLDocument hdoc, Element elem, AttributeSet attr, int offset, int x, int y) {
            String s;
            Map m;
            Object useMap = attr.getAttribute(HTML.Attribute.USEMAP);
            if (useMap instanceof String && (m = hdoc.getMap(s = (String)useMap)) != null && offset < hdoc.getLength()) {
                AttributeSet area;
                Rectangle bounds;
                TextUI ui = html.getUI();
                try {
                    Rectangle lBounds = ui.modelToView(html, offset, Position.Bias.Forward);
                    Rectangle rBounds = ui.modelToView(html, offset + 1, Position.Bias.Backward);
                    bounds = lBounds;
                    bounds.add(rBounds);
                }
                catch (BadLocationException ble) {
                    bounds = null;
                }
                if (bounds != null && (area = m.getArea(x - bounds.x, y - bounds.y, bounds.width, bounds.height)) != null) {
                    return (String)area.getAttribute(HTML.Attribute.HREF);
                }
            }
            return null;
        }

        private boolean doesElementContainLocation(JEditorPane editor, Element e, int offset, int x, int y) {
            if (e != null && offset > 0 && e.getStartOffset() == offset) {
                try {
                    TextUI ui = editor.getUI();
                    Rectangle r1 = ui.modelToView(editor, offset, Position.Bias.Forward);
                    if (r1 == null) {
                        return false;
                    }
                    Rectangle r2 = ui.modelToView(editor, e.getEndOffset(), Position.Bias.Backward);
                    if (r2 != null) {
                        r1.add(r2);
                    }
                    return r1.contains(x, y);
                }
                catch (BadLocationException badLocationException) {
                    // empty catch block
                }
            }
            return true;
        }

        protected void activateLink(int pos, JEditorPane editor) {
            this.activateLink(pos, editor, null);
        }

        void activateLink(int pos, JEditorPane html, MouseEvent mouseEvent) {
            Document doc = html.getDocument();
            if (doc instanceof HTMLDocument) {
                HTMLDocument hdoc = (HTMLDocument)doc;
                Element e = hdoc.getCharacterElement(pos);
                AttributeSet a = e.getAttributes();
                AttributeSet anchor = (AttributeSet)a.getAttribute(HTML.Tag.A);
                HyperlinkEvent linkEvent = null;
                int x = -1;
                int y = -1;
                if (mouseEvent != null) {
                    x = mouseEvent.getX();
                    y = mouseEvent.getY();
                }
                this.href = anchor == null ? this.getMapHREF(html, hdoc, e, a, pos, x, y) : (String)anchor.getAttribute(HTML.Attribute.HREF);
                if (this.href != null) {
                    linkEvent = this.createHyperlinkEvent(html, hdoc, this.href, anchor, e, mouseEvent);
                }
                if (linkEvent != null) {
                    html.fireHyperlinkUpdate(linkEvent);
                }
            }
        }

        HyperlinkEvent createHyperlinkEvent(JEditorPane html, HTMLDocument hdoc, String href, AttributeSet anchor, Element element, MouseEvent mouseEvent) {
            HyperlinkEvent linkEvent;
            URL u;
            try {
                URL base = hdoc.getBase();
                URL _unused = u = new URL(base, href);
                if (href != null && "file".equals(u.getProtocol()) && href.startsWith("#")) {
                    String baseFile = base.getFile();
                    String newFile = u.getFile();
                    if (baseFile != null && newFile != null && !newFile.startsWith(baseFile)) {
                        URL uRL = u = new URL(base, baseFile + href);
                    }
                }
            }
            catch (MalformedURLException m) {
                u = null;
            }
            if (!hdoc.isFrameDocument()) {
                linkEvent = new HyperlinkEvent(html, HyperlinkEvent.EventType.ACTIVATED, u, href, element, mouseEvent);
            } else {
                String target;
                String string = target = anchor != null ? (String)anchor.getAttribute(HTML.Attribute.TARGET) : null;
                if (target == null || target.isEmpty()) {
                    target = hdoc.getBaseTarget();
                }
                if (target == null || target.isEmpty()) {
                    target = "_self";
                }
                linkEvent = new HTMLFrameHyperlinkEvent(html, HyperlinkEvent.EventType.ACTIVATED, u, href, element, mouseEvent, target);
            }
            return linkEvent;
        }

        void fireEvents(JEditorPane editor, HTMLDocument doc, String href, Element lastElem, MouseEvent mouseEvent) {
            Serializable exit;
            URL u;
            if (this.href != null) {
                try {
                    URL uRL = u = new URL(doc.getBase(), this.href);
                }
                catch (MalformedURLException m) {
                    u = null;
                }
                exit = new HyperlinkEvent(editor, HyperlinkEvent.EventType.EXITED, u, this.href, lastElem, mouseEvent);
                editor.fireHyperlinkUpdate((HyperlinkEvent)exit);
            }
            if (href != null) {
                try {
                    exit = u = new URL(doc.getBase(), href);
                }
                catch (MalformedURLException m) {
                    u = null;
                }
                HyperlinkEvent entered = new HyperlinkEvent(editor, HyperlinkEvent.EventType.ENTERED, u, href, this.curElem, mouseEvent);
                editor.fireHyperlinkUpdate(entered);
            }
        }
    }

    public static abstract class Parser {
        protected Parser() {
        }

        public abstract void parse(Reader var1, ParserCallback var2, boolean var3) throws IOException;
    }

    public static class ParserCallback {
        public static final Object IMPLIED = "_implied_";

        public void flush() throws BadLocationException {
        }

        public void handleText(char[] data, int pos) {
        }

        public void handleComment(char[] data, int pos) {
        }

        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        }

        public void handleEndTag(HTML.Tag t, int pos) {
        }

        public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        }

        public void handleError(String errorMsg, int pos) {
        }

        public void handleEndOfLineString(String eol) {
        }
    }

    static class NavigateLinkAction
    extends TextAction
    implements CaretListener {
        private static final FocusHighlightPainter focusPainter = new FocusHighlightPainter(null);
        private final boolean focusBack;

        public NavigateLinkAction(String actionName) {
            super(actionName);
            this.focusBack = "previous-link-action".equals(actionName);
        }

        @Override
        public void caretUpdate(CaretEvent e) {
            JTextComponent comp;
            HTMLEditorKit kit;
            Object src = e.getSource();
            if (src instanceof JTextComponent && (kit = this.getHTMLEditorKit(comp = (JTextComponent)src)) != null && kit.foundLink) {
                kit.foundLink = false;
                comp.getAccessibleContext().firePropertyChange("AccessibleHypertextOffset", kit.prevHypertextOffset, e.getDot());
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Element nextElement;
            JTextComponent comp = this.getTextComponent(e);
            if (comp == null || comp.isEditable()) {
                return;
            }
            Document doc = comp.getDocument();
            HTMLEditorKit kit = this.getHTMLEditorKit(comp);
            if (doc == null || kit == null) {
                return;
            }
            ElementIterator ei = new ElementIterator(doc);
            int currentOffset = comp.getCaretPosition();
            int prevStartOffset = -1;
            int prevEndOffset = -1;
            while ((nextElement = ei.next()) != null) {
                String name = nextElement.getName();
                AttributeSet attr = nextElement.getAttributes();
                Object href = HTMLEditorKit.getAttrValue(attr, HTML.Attribute.HREF);
                if (!name.equals(HTML.Tag.OBJECT.toString()) && href == null) continue;
                int elementOffset = nextElement.getStartOffset();
                if (this.focusBack) {
                    if (elementOffset >= currentOffset && prevStartOffset >= 0) {
                        kit.foundLink = true;
                        comp.setCaretPosition(prevStartOffset);
                        this.moveCaretPosition(comp, kit, prevStartOffset, prevEndOffset);
                        kit.prevHypertextOffset = prevStartOffset;
                        return;
                    }
                } else if (elementOffset > currentOffset) {
                    kit.foundLink = true;
                    comp.setCaretPosition(elementOffset);
                    this.moveCaretPosition(comp, kit, elementOffset, nextElement.getEndOffset());
                    kit.prevHypertextOffset = elementOffset;
                    return;
                }
                prevStartOffset = nextElement.getStartOffset();
                prevEndOffset = nextElement.getEndOffset();
            }
            if (this.focusBack && prevStartOffset >= 0) {
                kit.foundLink = true;
                comp.setCaretPosition(prevStartOffset);
                this.moveCaretPosition(comp, kit, prevStartOffset, prevEndOffset);
                kit.prevHypertextOffset = prevStartOffset;
            }
        }

        private void moveCaretPosition(JTextComponent comp, HTMLEditorKit kit, int mark, int dot) {
            Highlighter h = comp.getHighlighter();
            if (h != null) {
                int p0 = Math.min(dot, mark);
                int p1 = Math.max(dot, mark);
                try {
                    if (kit.linkNavigationTag != null) {
                        h.changeHighlight(kit.linkNavigationTag, p0, p1);
                    } else {
                        kit.linkNavigationTag = h.addHighlight(p0, p1, focusPainter);
                    }
                }
                catch (BadLocationException badLocationException) {
                    // empty catch block
                }
            }
        }

        private HTMLEditorKit getHTMLEditorKit(JTextComponent comp) {
            EditorKit kit;
            if (comp instanceof JEditorPane && (kit = ((JEditorPane)comp).getEditorKit()) instanceof HTMLEditorKit) {
                return (HTMLEditorKit)kit;
            }
            return null;
        }

        static class FocusHighlightPainter
        extends DefaultHighlighter.DefaultHighlightPainter {
            FocusHighlightPainter(Color color) {
                super(color);
            }

            @Override
            public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
                Color color = this.getColor();
                if (color == null) {
                    g.setColor(c.getSelectionColor());
                } else {
                    g.setColor(color);
                }
                if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
                    Rectangle alloc = bounds instanceof Rectangle ? (Rectangle)bounds : bounds.getBounds();
                    g.drawRect(alloc.x, alloc.y, alloc.width - 1, alloc.height);
                    return alloc;
                }
                try {
                    Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);
                    Rectangle r = shape instanceof Rectangle ? (Rectangle)shape : shape.getBounds();
                    g.drawRect(r.x, r.y, r.width - 1, r.height);
                    return r;
                }
                catch (BadLocationException badLocationException) {
                    return null;
                }
            }
        }
    }

    public static class HTMLFactory
    implements ViewFactory {
        @Override
        public View create(Element elem) {
            String nm;
            Object o;
            AttributeSet attrs = elem.getAttributes();
            Object elementName = attrs.getAttribute("$ename");
            Object object = o = elementName != null ? null : attrs.getAttribute(StyleConstants.NameAttribute);
            if (o instanceof HTML.Tag) {
                HTML.Tag kind = (HTML.Tag)o;
                if (kind == HTML.Tag.CONTENT) {
                    return new InlineView(elem);
                }
                if (kind == HTML.Tag.IMPLIED) {
                    String ws = (String)elem.getAttributes().getAttribute(CSS.Attribute.WHITE_SPACE);
                    if ("pre".equals(ws)) {
                        return new LineView(elem);
                    }
                    return new ParagraphView(elem);
                }
                if (kind == HTML.Tag.P || kind == HTML.Tag.H1 || kind == HTML.Tag.H2 || kind == HTML.Tag.H3 || kind == HTML.Tag.H4 || kind == HTML.Tag.H5 || kind == HTML.Tag.H6 || kind == HTML.Tag.DT) {
                    return new ParagraphView(elem);
                }
                if (kind == HTML.Tag.MENU || kind == HTML.Tag.DIR || kind == HTML.Tag.UL || kind == HTML.Tag.OL) {
                    return new ListView(elem);
                }
                if (kind == HTML.Tag.BODY) {
                    return new BodyBlockView(elem);
                }
                if (kind == HTML.Tag.HTML) {
                    return new BlockView(elem, 1);
                }
                if (kind == HTML.Tag.LI || kind == HTML.Tag.CENTER || kind == HTML.Tag.DL || kind == HTML.Tag.DD || kind == HTML.Tag.DIV || kind == HTML.Tag.BLOCKQUOTE || kind == HTML.Tag.PRE || kind == HTML.Tag.FORM) {
                    return new BlockView(elem, 1);
                }
                if (kind == HTML.Tag.NOFRAMES) {
                    return new NoFramesView(elem, 1);
                }
                if (kind == HTML.Tag.IMG) {
                    return new ImageView(elem);
                }
                if (kind == HTML.Tag.ISINDEX) {
                    return new IsindexView(elem);
                }
                if (kind == HTML.Tag.HR) {
                    return new HRuleView(elem);
                }
                if (kind == HTML.Tag.BR) {
                    return new BRView(elem);
                }
                if (kind == HTML.Tag.TABLE) {
                    return new TableView(elem);
                }
                if (kind == HTML.Tag.INPUT || kind == HTML.Tag.SELECT || kind == HTML.Tag.TEXTAREA) {
                    return new FormView(elem);
                }
                if (kind == HTML.Tag.OBJECT) {
                    if (SwingAccessor.getAllowHTMLObject().booleanValue()) {
                        return new ObjectView(elem);
                    }
                    return new ObjectView(elem, false);
                }
                if (kind == HTML.Tag.FRAMESET) {
                    if (elem.getAttributes().isDefined(HTML.Attribute.ROWS)) {
                        return new FrameSetView(elem, 1);
                    }
                    if (elem.getAttributes().isDefined(HTML.Attribute.COLS)) {
                        return new FrameSetView(elem, 0);
                    }
                    throw new RuntimeException("Can't build a" + String.valueOf(kind) + ", " + String.valueOf(elem) + ":no ROWS or COLS defined.");
                }
                if (kind == HTML.Tag.FRAME) {
                    return new FrameView(elem);
                }
                if (kind instanceof HTML.UnknownTag) {
                    return new HiddenTagView(elem);
                }
                if (kind == HTML.Tag.COMMENT) {
                    return new CommentView(elem);
                }
                if (kind == HTML.Tag.HEAD) {
                    return new BlockView(this, elem, 0){

                        @Override
                        public float getPreferredSpan(int axis) {
                            return 0.0f;
                        }

                        @Override
                        public float getMinimumSpan(int axis) {
                            return 0.0f;
                        }

                        @Override
                        public float getMaximumSpan(int axis) {
                            return 0.0f;
                        }

                        @Override
                        protected void loadChildren(ViewFactory f) {
                        }

                        @Override
                        public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
                            return a;
                        }

                        @Override
                        public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a, int direction, Position.Bias[] biasRet) {
                            return this.getElement().getEndOffset();
                        }
                    };
                }
                if (kind == HTML.Tag.TITLE || kind == HTML.Tag.META || kind == HTML.Tag.LINK || kind == HTML.Tag.STYLE || kind == HTML.Tag.SCRIPT || kind == HTML.Tag.AREA || kind == HTML.Tag.MAP || kind == HTML.Tag.PARAM || kind == HTML.Tag.APPLET) {
                    return new HiddenTagView(elem);
                }
            }
            String string = nm = elementName != null ? (String)elementName : elem.getName();
            if (nm != null) {
                if (nm.equals("content")) {
                    return new LabelView(elem);
                }
                if (nm.equals("paragraph")) {
                    return new ParagraphView(elem);
                }
                if (nm.equals("section")) {
                    return new BoxView(elem, 1);
                }
                if (nm.equals("component")) {
                    return new ComponentView(elem);
                }
                if (nm.equals("icon")) {
                    return new IconView(elem);
                }
            }
            return new LabelView(elem);
        }

        static class BodyBlockView
        extends BlockView
        implements ComponentListener {
            private Reference<JViewport> cachedViewPort = null;
            private boolean isListening = false;
            private int viewVisibleWidth = Integer.MAX_VALUE;
            private int componentVisibleWidth = Integer.MAX_VALUE;

            public BodyBlockView(Element elem) {
                super(elem, 1);
            }

            @Override
            protected SizeRequirements calculateMajorAxisRequirements(int axis, SizeRequirements r) {
                r = super.calculateMajorAxisRequirements(axis, r);
                r.maximum = Integer.MAX_VALUE;
                return r;
            }

            @Override
            protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
                Container container;
                Container container2 = this.getContainer();
                if (container2 instanceof JEditorPane && (container = container2.getParent()) instanceof JViewport) {
                    JViewport viewPort = (JViewport)container;
                    if (this.cachedViewPort != null) {
                        cachedObject = this.cachedViewPort.get();
                        if (cachedObject != null) {
                            if (cachedObject != viewPort) {
                                cachedObject.removeComponentListener(this);
                            }
                        } else {
                            this.cachedViewPort = null;
                        }
                    }
                    if (this.cachedViewPort == null) {
                        viewPort.addComponentListener(this);
                        this.cachedViewPort = new WeakReference<JViewport>(viewPort);
                    }
                    this.componentVisibleWidth = viewPort.getExtentSize().width;
                    if (this.componentVisibleWidth > 0) {
                        Insets insets = container2.getInsets();
                        this.viewVisibleWidth = this.componentVisibleWidth - insets.left - this.getLeftInset();
                        targetSpan = Math.min(targetSpan, this.viewVisibleWidth);
                    }
                } else if (this.cachedViewPort != null) {
                    cachedObject = this.cachedViewPort.get();
                    if (cachedObject != null) {
                        cachedObject.removeComponentListener(this);
                    }
                    this.cachedViewPort = null;
                }
                super.layoutMinorAxis(targetSpan, axis, offsets, spans);
            }

            @Override
            public void setParent(View parent) {
                if (parent == null && this.cachedViewPort != null) {
                    JViewport cachedObject = this.cachedViewPort.get();
                    if (cachedObject != null) {
                        cachedObject.removeComponentListener(this);
                    }
                    this.cachedViewPort = null;
                }
                super.setParent(parent);
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void componentResized(ComponentEvent e) {
                Document doc;
                if (!(e.getSource() instanceof JViewport)) {
                    return;
                }
                JViewport viewPort = (JViewport)e.getSource();
                if (this.componentVisibleWidth != viewPort.getExtentSize().width && (doc = this.getDocument()) instanceof AbstractDocument) {
                    AbstractDocument document = (AbstractDocument)this.getDocument();
                    document.readLock();
                    try {
                        this.layoutChanged(0);
                        this.preferenceChanged(null, true, true);
                    }
                    finally {
                        document.readUnlock();
                    }
                }
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }
        }
    }

    static class ActivateLinkAction
    extends TextAction {
        public ActivateLinkAction(String actionName) {
            super(actionName);
        }

        private void activateLink(String href, HTMLDocument doc, JEditorPane editor, int offset) {
            try {
                URL page = (URL)doc.getProperty("stream");
                URL url = new URL(page, href);
                HyperlinkEvent linkEvent = new HyperlinkEvent(editor, HyperlinkEvent.EventType.ACTIVATED, url, url.toExternalForm(), doc.getCharacterElement(offset));
                editor.fireHyperlinkUpdate(linkEvent);
            }
            catch (MalformedURLException malformedURLException) {
                // empty catch block
            }
        }

        private void doObjectAction(JEditorPane editor, Element elem) {
            AccessibleAction aa;
            AccessibleContext ac;
            ObjectView objectView;
            Component comp;
            View view = this.getView(editor, elem);
            if (view instanceof ObjectView && (comp = (objectView = (ObjectView)view).getComponent()) instanceof Accessible && (ac = comp.getAccessibleContext()) != null && (aa = ac.getAccessibleAction()) != null) {
                aa.doAccessibleAction(0);
            }
        }

        private View getRootView(JEditorPane editor) {
            return editor.getUI().getRootView(editor);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private View getView(JEditorPane editor, Element elem) {
            Object lock = this.lock(editor);
            try {
                View rootView = this.getRootView(editor);
                int start = elem.getStartOffset();
                if (rootView != null) {
                    View view = this.getView(rootView, elem, start);
                    return view;
                }
                View view = null;
                return view;
            }
            finally {
                this.unlock(lock);
            }
        }

        private View getView(View parent, Element elem, int start) {
            if (parent.getElement() == elem) {
                return parent;
            }
            int index = parent.getViewIndex(start, Position.Bias.Forward);
            if (index != -1 && index < parent.getViewCount()) {
                return this.getView(parent.getView(index), elem, start);
            }
            return null;
        }

        private Object lock(JEditorPane editor) {
            Document document = editor.getDocument();
            if (document instanceof AbstractDocument) {
                ((AbstractDocument)document).readLock();
                return document;
            }
            return null;
        }

        private void unlock(Object key) {
            if (key != null) {
                ((AbstractDocument)key).readUnlock();
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Element currentElement;
            JTextComponent c = this.getTextComponent(e);
            if (c.isEditable() || !(c instanceof JEditorPane)) {
                return;
            }
            JEditorPane editor = (JEditorPane)c;
            Document d = editor.getDocument();
            if (!(d instanceof HTMLDocument)) {
                return;
            }
            HTMLDocument doc = (HTMLDocument)d;
            ElementIterator ei = new ElementIterator(doc);
            int currentOffset = editor.getCaretPosition();
            Object urlString = null;
            Object objString = null;
            while ((currentElement = ei.next()) != null) {
                Object obj;
                String name = currentElement.getName();
                AttributeSet attr = currentElement.getAttributes();
                Object href = HTMLEditorKit.getAttrValue(attr, HTML.Attribute.HREF);
                if (href != null) {
                    if (currentOffset < currentElement.getStartOffset() || currentOffset > currentElement.getEndOffset()) continue;
                    this.activateLink((String)href, doc, editor, currentOffset);
                    return;
                }
                if (!name.equals(HTML.Tag.OBJECT.toString()) || (obj = HTMLEditorKit.getAttrValue(attr, HTML.Attribute.CLASSID)) == null || currentOffset < currentElement.getStartOffset() || currentOffset > currentElement.getEndOffset()) continue;
                this.doObjectAction(editor, currentElement);
                return;
            }
        }
    }

    public static class InsertHTMLTextAction
    extends HTMLTextAction {
        protected String html;
        protected HTML.Tag parentTag;
        protected HTML.Tag addTag;
        protected HTML.Tag alternateParentTag;
        protected HTML.Tag alternateAddTag;
        boolean adjustSelection;

        public InsertHTMLTextAction(String name, String html, HTML.Tag parentTag, HTML.Tag addTag) {
            this(name, html, parentTag, addTag, null, null);
        }

        public InsertHTMLTextAction(String name, String html, HTML.Tag parentTag, HTML.Tag addTag, HTML.Tag alternateParentTag, HTML.Tag alternateAddTag) {
            this(name, html, parentTag, addTag, alternateParentTag, alternateAddTag, true);
        }

        InsertHTMLTextAction(String name, String html, HTML.Tag parentTag, HTML.Tag addTag, HTML.Tag alternateParentTag, HTML.Tag alternateAddTag, boolean adjustSelection) {
            super(name);
            this.html = html;
            this.parentTag = parentTag;
            this.addTag = addTag;
            this.alternateParentTag = alternateParentTag;
            this.alternateAddTag = alternateAddTag;
            this.adjustSelection = adjustSelection;
        }

        protected void insertHTML(JEditorPane editor, HTMLDocument doc, int offset, String html, int popDepth, int pushDepth, HTML.Tag addTag) {
            try {
                this.getHTMLEditorKit(editor).insertHTML(doc, offset, html, popDepth, pushDepth, addTag);
            }
            catch (IOException | BadLocationException e) {
                throw new RuntimeException("Unable to insert: " + String.valueOf(e));
            }
        }

        protected void insertAtBoundary(JEditorPane editor, HTMLDocument doc, int offset, Element insertElement, String html, HTML.Tag parentTag, HTML.Tag addTag) {
            this.insertAtBoundry(editor, doc, offset, insertElement, html, parentTag, addTag);
        }

        @Deprecated
        protected void insertAtBoundry(JEditorPane editor, HTMLDocument doc, int offset, Element insertElement, String html, HTML.Tag parentTag, HTML.Tag addTag) {
            Element commonParent;
            Element e;
            boolean isFirst;
            boolean bl = isFirst = offset == 0;
            if (offset > 0 || insertElement == null) {
                for (e = doc.getDefaultRootElement(); e != null && e.getStartOffset() != offset && !e.isLeaf(); e = e.getElement(e.getElementIndex(offset))) {
                }
                commonParent = e != null ? e.getParentElement() : null;
            } else {
                commonParent = insertElement;
            }
            if (commonParent != null) {
                int pops = 0;
                int pushes = 0;
                if (isFirst && insertElement != null) {
                    e = commonParent;
                    while (e != null && !e.isLeaf()) {
                        e = e.getElement(e.getElementIndex(offset));
                        ++pops;
                    }
                } else {
                    e = commonParent;
                    --offset;
                    while (e != null && !e.isLeaf()) {
                        e = e.getElement(e.getElementIndex(offset));
                        ++pops;
                    }
                    e = commonParent;
                    ++offset;
                    while (e != null && e != insertElement) {
                        e = e.getElement(e.getElementIndex(offset));
                        ++pushes;
                    }
                }
                pops = Math.max(0, pops - 1);
                this.insertHTML(editor, doc, offset, html, pops, pushes, addTag);
            }
        }

        boolean insertIntoTag(JEditorPane editor, HTMLDocument doc, int offset, HTML.Tag tag, HTML.Tag addTag) {
            int depth;
            Element e = this.findElementMatchingTag(doc, offset, tag);
            if (e != null && e.getStartOffset() == offset) {
                this.insertAtBoundary(editor, doc, offset, e, this.html, tag, addTag);
                return true;
            }
            if (offset > 0 && (depth = this.elementCountToTag(doc, offset - 1, tag)) != -1) {
                this.insertHTML(editor, doc, offset, this.html, depth, 0, addTag);
                return true;
            }
            return false;
        }

        void adjustSelection(JEditorPane pane, HTMLDocument doc, int startOffset, int oldLength) {
            int newLength = doc.getLength();
            if (newLength != oldLength && startOffset < newLength) {
                if (startOffset > 0) {
                    String text;
                    try {
                        text = doc.getText(startOffset - 1, 1);
                    }
                    catch (BadLocationException ble) {
                        text = null;
                    }
                    if (text != null && text.length() > 0 && text.charAt(0) == '\n') {
                        pane.select(startOffset, startOffset);
                    } else {
                        pane.select(startOffset + 1, startOffset + 1);
                    }
                } else {
                    pane.select(1, 1);
                }
            }
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            JEditorPane editor = this.getEditor(ae);
            if (editor != null) {
                HTMLDocument doc = this.getHTMLDocument(editor);
                int offset = editor.getSelectionStart();
                int length = doc.getLength();
                boolean inserted = !this.insertIntoTag(editor, doc, offset, this.parentTag, this.addTag) && this.alternateParentTag != null ? this.insertIntoTag(editor, doc, offset, this.alternateParentTag, this.alternateAddTag) : true;
                if (this.adjustSelection && inserted) {
                    this.adjustSelection(editor, doc, offset, length);
                }
            }
        }
    }

    static class InsertHRAction
    extends InsertHTMLTextAction {
        InsertHRAction() {
            super("InsertHR", HTMLEditorKit.INSERT_HR_HTML, null, HTML.Tag.IMPLIED, null, null, false);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            int offset;
            HTMLDocument doc;
            Element paragraph;
            JEditorPane editor = this.getEditor(ae);
            if (editor != null && (paragraph = (doc = this.getHTMLDocument(editor)).getParagraphElement(offset = editor.getSelectionStart())).getParentElement() != null) {
                this.parentTag = (HTML.Tag)paragraph.getParentElement().getAttributes().getAttribute(StyleConstants.NameAttribute);
                super.actionPerformed(ae);
            }
        }
    }

    static class BeginAction
    extends TextAction {
        private boolean select;

        BeginAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = this.getTextComponent(e);
            int bodyStart = HTMLEditorKit.getBodyElementStart(target);
            if (target != null) {
                if (this.select) {
                    target.moveCaretPosition(bodyStart);
                } else {
                    target.setCaretPosition(bodyStart);
                }
            }
        }
    }

    public static abstract class HTMLTextAction
    extends StyledEditorKit.StyledTextAction {
        public HTMLTextAction(String name) {
            super(name);
        }

        protected HTMLDocument getHTMLDocument(JEditorPane e) {
            Document d = e.getDocument();
            if (d instanceof HTMLDocument) {
                return (HTMLDocument)d;
            }
            throw new IllegalArgumentException("document must be HTMLDocument");
        }

        protected HTMLEditorKit getHTMLEditorKit(JEditorPane e) {
            EditorKit k = e.getEditorKit();
            if (k instanceof HTMLEditorKit) {
                return (HTMLEditorKit)k;
            }
            throw new IllegalArgumentException("EditorKit must be HTMLEditorKit");
        }

        protected Element[] getElementsAt(HTMLDocument doc, int offset) {
            return this.getElementsAt(doc.getDefaultRootElement(), offset, 0);
        }

        private Element[] getElementsAt(Element parent, int offset, int depth) {
            if (parent.isLeaf()) {
                Element[] retValue = new Element[depth + 1];
                retValue[depth] = parent;
                return retValue;
            }
            Element[] retValue = this.getElementsAt(parent.getElement(parent.getElementIndex(offset)), offset, depth + 1);
            retValue[depth] = parent;
            return retValue;
        }

        protected int elementCountToTag(HTMLDocument doc, int offset, HTML.Tag tag) {
            int depth = -1;
            Element e = doc.getCharacterElement(offset);
            while (e != null && e.getAttributes().getAttribute(StyleConstants.NameAttribute) != tag) {
                e = e.getParentElement();
                ++depth;
            }
            if (e == null) {
                return -1;
            }
            return depth;
        }

        protected Element findElementMatchingTag(HTMLDocument doc, int offset, HTML.Tag tag) {
            Element lastMatch = null;
            for (Element e = doc.getDefaultRootElement(); e != null; e = e.getElement(e.getElementIndex(offset))) {
                if (e.getAttributes().getAttribute(StyleConstants.NameAttribute) != tag) continue;
                lastMatch = e;
            }
            return lastMatch;
        }
    }
}

