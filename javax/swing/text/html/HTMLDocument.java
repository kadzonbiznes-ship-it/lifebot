/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html;

import java.awt.font.TextAttribute;
import java.io.IOException;
import java.io.StringReader;
import java.lang.invoke.CallSite;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.DefaultButtonModel;
import javax.swing.JToggleButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.GapContent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.text.html.Map;
import javax.swing.text.html.Option;
import javax.swing.text.html.OptionComboBoxModel;
import javax.swing.text.html.OptionListModel;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.html.TextAreaDocument;
import javax.swing.undo.UndoableEdit;
import sun.swing.SwingUtilities2;

public class HTMLDocument
extends DefaultStyledDocument {
    private boolean frameDocument = false;
    private boolean preservesUnknownTags = true;
    private HashMap<String, ButtonGroup> radioButtonGroupsMap;
    static final String TokenThreshold = "token threshold";
    private static final int MaxThreshold = 10000;
    private static final int StepThreshold = 5;
    public static final String AdditionalComments = "AdditionalComments";
    static final String StyleType = "StyleType";
    URL base;
    boolean hasBaseTag = false;
    private String baseTarget = null;
    private HTMLEditorKit.Parser parser;
    private static AttributeSet contentAttributeSet;
    static String MAP_PROPERTY;
    private static char[] NEWLINE;
    private boolean insertInBody = false;
    private static final String I18NProperty = "i18n";

    public HTMLDocument() {
        this((AbstractDocument.Content)new GapContent(4096), new StyleSheet());
    }

    public HTMLDocument(StyleSheet styles) {
        this((AbstractDocument.Content)new GapContent(4096), styles);
    }

    public HTMLDocument(AbstractDocument.Content c, StyleSheet styles) {
        super(c, styles);
    }

    public HTMLEditorKit.ParserCallback getReader(int pos) {
        Object desc = this.getProperty("stream");
        if (desc instanceof URL) {
            this.setBase((URL)desc);
        }
        HTMLReader reader = new HTMLReader(pos);
        return reader;
    }

    public HTMLEditorKit.ParserCallback getReader(int pos, int popDepth, int pushDepth, HTML.Tag insertTag) {
        return this.getReader(pos, popDepth, pushDepth, insertTag, true);
    }

    HTMLEditorKit.ParserCallback getReader(int pos, int popDepth, int pushDepth, HTML.Tag insertTag, boolean insertInsertTag) {
        Object desc = this.getProperty("stream");
        if (desc instanceof URL) {
            this.setBase((URL)desc);
        }
        HTMLReader reader = new HTMLReader(pos, popDepth, pushDepth, insertTag, insertInsertTag, false, true);
        return reader;
    }

    public URL getBase() {
        return this.base;
    }

    public void setBase(URL u) {
        this.base = u;
        this.getStyleSheet().setBase(u);
    }

    @Override
    protected void insert(int offset, DefaultStyledDocument.ElementSpec[] data) throws BadLocationException {
        super.insert(offset, data);
    }

    @Override
    protected void insertUpdate(AbstractDocument.DefaultDocumentEvent chng, AttributeSet attr) {
        if (attr == null) {
            attr = contentAttributeSet;
        } else if (attr.isDefined(StyleConstants.ComposedTextAttribute)) {
            ((MutableAttributeSet)attr).addAttributes(contentAttributeSet);
        }
        if (attr.isDefined("CR")) {
            ((MutableAttributeSet)attr).removeAttribute("CR");
        }
        super.insertUpdate(chng, attr);
    }

    @Override
    protected void create(DefaultStyledDocument.ElementSpec[] data) {
        super.create(data);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setParagraphAttributes(int offset, int length, AttributeSet s, boolean replace) {
        try {
            this.writeLock();
            int end = Math.min(offset + length, this.getLength());
            Element e = this.getParagraphElement(offset);
            offset = e.getStartOffset();
            e = this.getParagraphElement(end);
            length = Math.max(0, e.getEndOffset() - offset);
            AbstractDocument.DefaultDocumentEvent changes = new AbstractDocument.DefaultDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE);
            AttributeSet sCopy = s.copyAttributes();
            int lastEnd = Integer.MAX_VALUE;
            int pos = offset;
            while (pos <= end) {
                Element paragraph = this.getParagraphElement(pos);
                lastEnd = lastEnd == paragraph.getEndOffset() ? ++lastEnd : paragraph.getEndOffset();
                MutableAttributeSet attr = (MutableAttributeSet)paragraph.getAttributes();
                changes.addEdit(new DefaultStyledDocument.AttributeUndoableEdit(paragraph, sCopy, replace));
                if (replace) {
                    attr.removeAttributes(attr);
                }
                attr.addAttributes(s);
                pos = lastEnd;
            }
            changes.end();
            this.fireChangedUpdate(changes);
            this.fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
        }
        finally {
            this.writeUnlock();
        }
    }

    public StyleSheet getStyleSheet() {
        return (StyleSheet)this.getAttributeContext();
    }

    public Iterator getIterator(HTML.Tag t) {
        if (t.isBlock()) {
            return null;
        }
        return new LeafIterator(t, this);
    }

    @Override
    protected Element createLeafElement(Element parent, AttributeSet a, int p0, int p1) {
        return new RunElement(parent, a, p0, p1);
    }

    @Override
    protected Element createBranchElement(Element parent, AttributeSet a) {
        return new BlockElement(parent, a);
    }

    @Override
    protected AbstractDocument.AbstractElement createDefaultRoot() {
        this.writeLock();
        SimpleAttributeSet a = new SimpleAttributeSet();
        a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.HTML);
        BlockElement html = new BlockElement(null, a.copyAttributes());
        a.removeAttributes(a);
        a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.BODY);
        BlockElement body = new BlockElement((Element)html, a.copyAttributes());
        a.removeAttributes(a);
        a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.P);
        this.getStyleSheet().addCSSAttributeFromHTML(a, CSS.Attribute.MARGIN_TOP, "0");
        BlockElement paragraph = new BlockElement((Element)body, a.copyAttributes());
        a.removeAttributes(a);
        a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
        RunElement brk = new RunElement((Element)paragraph, (AttributeSet)a, 0, 1);
        Element[] buff = new Element[]{brk};
        paragraph.replace(0, 0, buff);
        buff[0] = paragraph;
        body.replace(0, 0, buff);
        buff[0] = body;
        html.replace(0, 0, buff);
        this.writeUnlock();
        return html;
    }

    public void setTokenThreshold(int n) {
        this.putProperty(TokenThreshold, n);
    }

    public int getTokenThreshold() {
        Integer i = (Integer)this.getProperty(TokenThreshold);
        if (i != null) {
            return i;
        }
        return Integer.MAX_VALUE;
    }

    public void setPreservesUnknownTags(boolean preservesTags) {
        this.preservesUnknownTags = preservesTags;
    }

    public boolean getPreservesUnknownTags() {
        return this.preservesUnknownTags;
    }

    public void processHTMLFrameHyperlinkEvent(HTMLFrameHyperlinkEvent e) {
        String frameName = e.getTarget();
        Element element = e.getSourceElement();
        String urlStr = e.getURL().toString();
        if (frameName.equals("_self")) {
            this.updateFrame(element, urlStr);
        } else if (frameName.equals("_parent")) {
            this.updateFrameSet(element.getParentElement(), urlStr);
        } else {
            Element targetElement = this.findFrame(frameName);
            if (targetElement != null) {
                this.updateFrame(targetElement, urlStr);
            }
        }
    }

    private Element findFrame(String frameName) {
        String frameTarget;
        AttributeSet attr;
        Element next;
        ElementIterator it = new ElementIterator(this);
        while (!((next = it.next()) == null || HTMLDocument.matchNameAttribute(attr = next.getAttributes(), HTML.Tag.FRAME) && (frameTarget = (String)attr.getAttribute(HTML.Attribute.NAME)) != null && frameTarget.equals(frameName))) {
        }
        return next;
    }

    static boolean matchNameAttribute(AttributeSet attr, HTML.Tag tag) {
        HTML.Tag name;
        Object o = attr.getAttribute(StyleConstants.NameAttribute);
        return o instanceof HTML.Tag && (name = (HTML.Tag)o) == tag;
    }

    private void updateFrameSet(Element element, String url) {
        try {
            int startOffset = element.getStartOffset();
            int endOffset = Math.min(this.getLength(), element.getEndOffset());
            Object html = "<frame";
            if (url != null) {
                html = (String)html + " src=\"" + url + "\"";
            }
            html = (String)html + ">";
            this.installParserIfNecessary();
            this.setOuterHTML(element, (String)html);
        }
        catch (IOException | BadLocationException exception) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void updateFrame(Element element, String url) {
        try {
            this.writeLock();
            AbstractDocument.DefaultDocumentEvent changes = new AbstractDocument.DefaultDocumentEvent(element.getStartOffset(), 1, DocumentEvent.EventType.CHANGE);
            AttributeSet sCopy = element.getAttributes().copyAttributes();
            MutableAttributeSet attr = (MutableAttributeSet)element.getAttributes();
            changes.addEdit(new DefaultStyledDocument.AttributeUndoableEdit(element, sCopy, false));
            attr.removeAttribute(HTML.Attribute.SRC);
            attr.addAttribute(HTML.Attribute.SRC, url);
            changes.end();
            this.fireChangedUpdate(changes);
            this.fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
        }
        finally {
            this.writeUnlock();
        }
    }

    boolean isFrameDocument() {
        return this.frameDocument;
    }

    void setFrameDocumentState(boolean frameDoc) {
        this.frameDocument = frameDoc;
    }

    void addMap(Map map) {
        String name = map.getName();
        if (name != null) {
            Hashtable<CallSite, Map> maps = this.getProperty(MAP_PROPERTY);
            if (maps == null) {
                maps = new Hashtable<CallSite, Map>(11);
                this.putProperty(MAP_PROPERTY, maps);
            }
            if (maps instanceof Hashtable) {
                Hashtable<CallSite, Map> tmp = maps;
                tmp.put((CallSite)((Object)("#" + name)), map);
            }
        }
    }

    void removeMap(Map map) {
        Object maps;
        String name = map.getName();
        if (name != null && (maps = this.getProperty(MAP_PROPERTY)) instanceof Hashtable) {
            ((Hashtable)maps).remove("#" + name);
        }
    }

    Map getMap(String name) {
        Object maps;
        if (name != null && (maps = this.getProperty(MAP_PROPERTY)) instanceof Hashtable) {
            Hashtable hashtable = (Hashtable)maps;
            return (Map)hashtable.get(name);
        }
        return null;
    }

    Enumeration<Object> getMaps() {
        Object maps = this.getProperty(MAP_PROPERTY);
        if (maps instanceof Hashtable) {
            Hashtable tmp = (Hashtable)maps;
            return tmp.elements();
        }
        return null;
    }

    void setDefaultStyleSheetType(String contentType) {
        this.putProperty(StyleType, contentType);
    }

    String getDefaultStyleSheetType() {
        String retValue = (String)this.getProperty(StyleType);
        if (retValue == null) {
            return "text/css";
        }
        return retValue;
    }

    public void setParser(HTMLEditorKit.Parser parser) {
        this.parser = parser;
        this.putProperty("__PARSER__", null);
    }

    public HTMLEditorKit.Parser getParser() {
        Object p = this.getProperty("__PARSER__");
        if (p instanceof HTMLEditorKit.Parser) {
            return (HTMLEditorKit.Parser)p;
        }
        return this.parser;
    }

    public void setInnerHTML(Element elem, String htmlText) throws BadLocationException, IOException {
        this.verifyParser();
        if (elem != null && elem.isLeaf()) {
            throw new IllegalArgumentException("Can not set inner HTML of a leaf");
        }
        if (elem != null && htmlText != null) {
            int oldCount = elem.getElementCount();
            int insertPosition = elem.getStartOffset();
            this.insertHTML(elem, elem.getStartOffset(), htmlText, true);
            if (elem.getElementCount() > oldCount) {
                this.removeElements(elem, elem.getElementCount() - oldCount, oldCount);
            }
        }
    }

    public void setOuterHTML(Element elem, String htmlText) throws BadLocationException, IOException {
        this.verifyParser();
        if (elem != null && elem.getParentElement() != null && htmlText != null) {
            boolean wantsNewline;
            int start = elem.getStartOffset();
            int end = elem.getEndOffset();
            int startLength = this.getLength();
            boolean bl = wantsNewline = !elem.isLeaf();
            if (!(wantsNewline || end <= startLength && this.getText(end - 1, 1).charAt(0) != NEWLINE[0])) {
                wantsNewline = true;
            }
            Element parent = elem.getParentElement();
            int oldCount = parent.getElementCount();
            this.insertHTML(parent, start, htmlText, wantsNewline);
            int newLength = this.getLength();
            if (oldCount != parent.getElementCount()) {
                int removeIndex = parent.getElementIndex(start + newLength - startLength);
                this.removeElements(parent, removeIndex, 1);
            }
        }
    }

    public void insertAfterStart(Element elem, String htmlText) throws BadLocationException, IOException {
        this.verifyParser();
        if (elem == null || htmlText == null) {
            return;
        }
        if (elem.isLeaf()) {
            throw new IllegalArgumentException("Can not insert HTML after start of a leaf");
        }
        this.insertHTML(elem, elem.getStartOffset(), htmlText, false);
    }

    public void insertBeforeEnd(Element elem, String htmlText) throws BadLocationException, IOException {
        this.verifyParser();
        if (elem != null && elem.isLeaf()) {
            throw new IllegalArgumentException("Can not set inner HTML before end of leaf");
        }
        if (elem != null) {
            int offset = elem.getEndOffset();
            if (elem.getElement(elem.getElementIndex(offset - 1)).isLeaf() && this.getText(offset - 1, 1).charAt(0) == NEWLINE[0]) {
                --offset;
            }
            this.insertHTML(elem, offset, htmlText, false);
        }
    }

    public void insertBeforeStart(Element elem, String htmlText) throws BadLocationException, IOException {
        Element parent;
        this.verifyParser();
        if (elem != null && (parent = elem.getParentElement()) != null) {
            this.insertHTML(parent, elem.getStartOffset(), htmlText, false);
        }
    }

    public void insertAfterEnd(Element elem, String htmlText) throws BadLocationException, IOException {
        Element parent;
        this.verifyParser();
        if (elem != null && (parent = elem.getParentElement()) != null) {
            int offset;
            if (HTML.Tag.BODY.name.equals(parent.getName())) {
                this.insertInBody = true;
            }
            if ((offset = elem.getEndOffset()) > this.getLength() + 1) {
                --offset;
            } else if (elem.isLeaf() && this.getText(offset - 1, 1).charAt(0) == NEWLINE[0]) {
                --offset;
            }
            this.insertHTML(parent, offset, htmlText, false);
            if (this.insertInBody) {
                this.insertInBody = false;
            }
        }
    }

    public Element getElement(String id) {
        if (id == null) {
            return null;
        }
        return this.getElement(this.getDefaultRootElement(), HTML.Attribute.ID, id, true);
    }

    public Element getElement(Element e, Object attribute, Object value) {
        return this.getElement(e, attribute, value, true);
    }

    private Element getElement(Element e, Object attribute, Object value, boolean searchLeafAttributes) {
        block4: {
            Enumeration<?> names;
            AttributeSet attr;
            block3: {
                attr = e.getAttributes();
                if (attr != null && attr.isDefined(attribute) && value.equals(attr.getAttribute(attribute))) {
                    return e;
                }
                if (e.isLeaf()) break block3;
                int maxCounter = e.getElementCount();
                for (int counter = 0; counter < maxCounter; ++counter) {
                    Element retValue = this.getElement(e.getElement(counter), attribute, value, searchLeafAttributes);
                    if (retValue == null) continue;
                    return retValue;
                }
                break block4;
            }
            if (!searchLeafAttributes || attr == null || (names = attr.getAttributeNames()) == null) break block4;
            while (names.hasMoreElements()) {
                AttributeSet check;
                Object name = names.nextElement();
                if (!(name instanceof HTML.Tag) || !(attr.getAttribute(name) instanceof AttributeSet) || !(check = (AttributeSet)attr.getAttribute(name)).isDefined(attribute) || !value.equals(check.getAttribute(attribute))) continue;
                return e;
            }
        }
        return null;
    }

    private void verifyParser() {
        if (this.getParser() == null) {
            throw new IllegalStateException("No HTMLEditorKit.Parser");
        }
    }

    private void installParserIfNecessary() {
        if (this.getParser() == null) {
            this.setParser(new HTMLEditorKit().getParser());
        }
    }

    private void insertHTML(Element parent, int offset, String html, boolean wantsTrailingNewline) throws BadLocationException, IOException {
        HTMLEditorKit.Parser parser;
        if (parent != null && html != null && (parser = this.getParser()) != null) {
            int lastOffset = Math.max(0, offset - 1);
            Element charElement = this.getCharacterElement(lastOffset);
            Element commonParent = parent;
            int pop = 0;
            int push = 0;
            if (parent.getStartOffset() > lastOffset) {
                while (commonParent != null && commonParent.getStartOffset() > lastOffset) {
                    commonParent = commonParent.getParentElement();
                    ++push;
                }
                if (commonParent == null) {
                    throw new BadLocationException("No common parent", offset);
                }
            }
            while (charElement != null && charElement != commonParent) {
                ++pop;
                charElement = charElement.getParentElement();
            }
            if (charElement != null) {
                HTMLReader reader = new HTMLReader(offset, pop - 1, push, null, false, true, wantsTrailingNewline);
                parser.parse(new StringReader(html), reader, true);
                reader.flush();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void removeElements(Element e, int index, int count) throws BadLocationException {
        this.writeLock();
        try {
            int start = e.getElement(index).getStartOffset();
            int end = e.getElement(index + count - 1).getEndOffset();
            if (end > this.getLength()) {
                this.removeElementsAtEnd(e, index, count, start, end);
            } else {
                this.removeElements(e, index, count, start, end);
            }
        }
        finally {
            this.writeUnlock();
        }
    }

    private void removeElementsAtEnd(Element e, int index, int count, int start, int end) throws BadLocationException {
        boolean isLeaf = e.getElement(index - 1).isLeaf();
        AbstractDocument.DefaultDocumentEvent dde = new AbstractDocument.DefaultDocumentEvent(start - 1, end - start + 1, DocumentEvent.EventType.REMOVE);
        if (isLeaf) {
            Element endE = this.getCharacterElement(this.getLength());
            --index;
            if (endE.getParentElement() != e) {
                this.replace(dde, e, index, ++count, start, end, true, true);
            } else {
                this.replace(dde, e, index, count, start, end, true, false);
            }
        } else {
            Element newLineE = e.getElement(index - 1);
            while (!newLineE.isLeaf()) {
                newLineE = newLineE.getElement(newLineE.getElementCount() - 1);
            }
            newLineE = newLineE.getParentElement();
            this.replace(dde, e, index, count, start, end, false, false);
            this.replace(dde, newLineE, newLineE.getElementCount() - 1, 1, start, end, true, true);
        }
        this.postRemoveUpdate(dde);
        dde.end();
        this.fireRemoveUpdate(dde);
        this.fireUndoableEditUpdate(new UndoableEditEvent(this, dde));
    }

    private void replace(AbstractDocument.DefaultDocumentEvent dde, Element e, int index, int count, int start, int end, boolean remove, boolean create) throws BadLocationException {
        UndoableEdit u;
        AttributeSet attrs = e.getElement(index).getAttributes();
        Element[] removed = new Element[count];
        for (int counter = 0; counter < count; ++counter) {
            removed[counter] = e.getElement(counter + index);
        }
        if (remove && (u = this.getContent().remove(start - 1, end - start)) != null) {
            dde.addEdit(u);
        }
        Element[] added = create ? new Element[]{this.createLeafElement(e, attrs, start - 1, start)} : new Element[]{};
        dde.addEdit(new AbstractDocument.ElementEdit(e, index, removed, added));
        ((AbstractDocument.BranchElement)e).replace(index, removed.length, added);
    }

    private void removeElements(Element e, int index, int count, int start, int end) throws BadLocationException {
        Element[] removed = new Element[count];
        Element[] added = new Element[]{};
        for (int counter = 0; counter < count; ++counter) {
            removed[counter] = e.getElement(counter + index);
        }
        AbstractDocument.DefaultDocumentEvent dde = new AbstractDocument.DefaultDocumentEvent(start, end - start, DocumentEvent.EventType.REMOVE);
        ((AbstractDocument.BranchElement)e).replace(index, removed.length, added);
        dde.addEdit(new AbstractDocument.ElementEdit(e, index, removed, added));
        UndoableEdit u = this.getContent().remove(start, end - start);
        if (u != null) {
            dde.addEdit(u);
        }
        this.postRemoveUpdate(dde);
        dde.end();
        this.fireRemoveUpdate(dde);
        if (u != null) {
            this.fireUndoableEditUpdate(new UndoableEditEvent(this, dde));
        }
    }

    void obtainLock() {
        this.writeLock();
    }

    void releaseLock() {
        this.writeUnlock();
    }

    @Override
    protected void fireChangedUpdate(DocumentEvent e) {
        super.fireChangedUpdate(e);
    }

    @Override
    protected void fireUndoableEditUpdate(UndoableEditEvent e) {
        super.fireUndoableEditUpdate(e);
    }

    boolean hasBaseTag() {
        return this.hasBaseTag;
    }

    String getBaseTarget() {
        return this.baseTarget;
    }

    static {
        MAP_PROPERTY = "__MAP__";
        contentAttributeSet = new SimpleAttributeSet();
        ((MutableAttributeSet)contentAttributeSet).addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
        NEWLINE = new char[1];
        HTMLDocument.NEWLINE[0] = 10;
    }

    public class HTMLReader
    extends HTMLEditorKit.ParserCallback {
        private boolean receivedEndHTML;
        private int flushCount;
        private boolean insertAfterImplied;
        private boolean wantsTrailingNewline;
        int threshold;
        int offset;
        boolean inParagraph = false;
        boolean impliedP = false;
        boolean inPre = false;
        boolean inTextArea = false;
        TextAreaDocument textAreaDocument = null;
        boolean inTitle = false;
        boolean lastWasNewline = true;
        boolean emptyAnchor;
        boolean midInsert;
        boolean inBody;
        HTML.Tag insertTag;
        boolean insertInsertTag;
        boolean foundInsertTag;
        int insertTagDepthDelta;
        int popDepth;
        int pushDepth;
        Map lastMap;
        boolean inStyle = false;
        String defaultStyle;
        Vector<Object> styles;
        boolean inHead = false;
        boolean isStyleCSS;
        boolean emptyDocument;
        AttributeSet styleAttributes;
        Option option;
        protected Vector<DefaultStyledDocument.ElementSpec> parseBuffer = new Vector();
        protected MutableAttributeSet charAttr = new TaggedAttributeSet();
        Stack<AttributeSet> charAttrStack = new Stack();
        Hashtable<HTML.Tag, TagAction> tagMap;
        int inBlock = 0;
        private HTML.Tag nextTagAfterPImplied = null;

        public HTMLReader(int offset) {
            this(offset, 0, 0, null);
        }

        public HTMLReader(int offset, int popDepth, int pushDepth, HTML.Tag insertTag) {
            this(offset, popDepth, pushDepth, insertTag, true, false, true);
        }

        HTMLReader(int offset, int popDepth, int pushDepth, HTML.Tag insertTag, boolean insertInsertTag, boolean insertAfterImplied, boolean wantsTrailingNewline) {
            this.emptyDocument = HTMLDocument.this.getLength() == 0;
            this.isStyleCSS = "text/css".equals(HTMLDocument.this.getDefaultStyleSheetType());
            this.offset = offset;
            this.threshold = HTMLDocument.this.getTokenThreshold();
            this.tagMap = new Hashtable(57);
            TagAction na = new TagAction(this);
            BlockAction ba = new BlockAction();
            ParagraphAction pa = new ParagraphAction();
            CharacterAction ca = new CharacterAction();
            SpecialAction sa = new SpecialAction();
            FormAction fa = new FormAction();
            HiddenAction ha = new HiddenAction();
            ConvertAction conv = new ConvertAction();
            this.tagMap.put(HTML.Tag.A, new AnchorAction());
            this.tagMap.put(HTML.Tag.ADDRESS, ca);
            this.tagMap.put(HTML.Tag.APPLET, ha);
            this.tagMap.put(HTML.Tag.AREA, new AreaAction());
            this.tagMap.put(HTML.Tag.B, conv);
            this.tagMap.put(HTML.Tag.BASE, new BaseAction());
            this.tagMap.put(HTML.Tag.BASEFONT, ca);
            this.tagMap.put(HTML.Tag.BIG, ca);
            this.tagMap.put(HTML.Tag.BLOCKQUOTE, ba);
            this.tagMap.put(HTML.Tag.BODY, ba);
            this.tagMap.put(HTML.Tag.BR, sa);
            this.tagMap.put(HTML.Tag.CAPTION, ba);
            this.tagMap.put(HTML.Tag.CENTER, ba);
            this.tagMap.put(HTML.Tag.CITE, ca);
            this.tagMap.put(HTML.Tag.CODE, ca);
            this.tagMap.put(HTML.Tag.DD, ba);
            this.tagMap.put(HTML.Tag.DFN, ca);
            this.tagMap.put(HTML.Tag.DIR, ba);
            this.tagMap.put(HTML.Tag.DIV, ba);
            this.tagMap.put(HTML.Tag.DL, ba);
            this.tagMap.put(HTML.Tag.DT, pa);
            this.tagMap.put(HTML.Tag.EM, ca);
            this.tagMap.put(HTML.Tag.FONT, conv);
            this.tagMap.put(HTML.Tag.FORM, new FormTagAction());
            this.tagMap.put(HTML.Tag.FRAME, sa);
            this.tagMap.put(HTML.Tag.FRAMESET, ba);
            this.tagMap.put(HTML.Tag.H1, pa);
            this.tagMap.put(HTML.Tag.H2, pa);
            this.tagMap.put(HTML.Tag.H3, pa);
            this.tagMap.put(HTML.Tag.H4, pa);
            this.tagMap.put(HTML.Tag.H5, pa);
            this.tagMap.put(HTML.Tag.H6, pa);
            this.tagMap.put(HTML.Tag.HEAD, new HeadAction());
            this.tagMap.put(HTML.Tag.HR, sa);
            this.tagMap.put(HTML.Tag.HTML, ba);
            this.tagMap.put(HTML.Tag.I, conv);
            this.tagMap.put(HTML.Tag.IMG, sa);
            this.tagMap.put(HTML.Tag.INPUT, fa);
            this.tagMap.put(HTML.Tag.ISINDEX, new IsindexAction());
            this.tagMap.put(HTML.Tag.KBD, ca);
            this.tagMap.put(HTML.Tag.LI, ba);
            this.tagMap.put(HTML.Tag.LINK, new LinkAction());
            this.tagMap.put(HTML.Tag.MAP, new MapAction());
            this.tagMap.put(HTML.Tag.MENU, ba);
            this.tagMap.put(HTML.Tag.META, new MetaAction());
            this.tagMap.put(HTML.Tag.NOBR, ca);
            this.tagMap.put(HTML.Tag.NOFRAMES, ba);
            this.tagMap.put(HTML.Tag.OBJECT, sa);
            this.tagMap.put(HTML.Tag.OL, ba);
            this.tagMap.put(HTML.Tag.OPTION, fa);
            this.tagMap.put(HTML.Tag.P, pa);
            this.tagMap.put(HTML.Tag.PARAM, new ObjectAction());
            this.tagMap.put(HTML.Tag.PRE, new PreAction());
            this.tagMap.put(HTML.Tag.SAMP, ca);
            this.tagMap.put(HTML.Tag.SCRIPT, ha);
            this.tagMap.put(HTML.Tag.SELECT, fa);
            this.tagMap.put(HTML.Tag.SMALL, ca);
            this.tagMap.put(HTML.Tag.SPAN, new ConvertSpanAction());
            this.tagMap.put(HTML.Tag.STRIKE, conv);
            this.tagMap.put(HTML.Tag.S, conv);
            this.tagMap.put(HTML.Tag.STRONG, ca);
            this.tagMap.put(HTML.Tag.STYLE, new StyleAction());
            this.tagMap.put(HTML.Tag.SUB, conv);
            this.tagMap.put(HTML.Tag.SUP, conv);
            this.tagMap.put(HTML.Tag.TABLE, ba);
            this.tagMap.put(HTML.Tag.TD, ba);
            this.tagMap.put(HTML.Tag.TEXTAREA, fa);
            this.tagMap.put(HTML.Tag.TH, ba);
            this.tagMap.put(HTML.Tag.TITLE, new TitleAction());
            this.tagMap.put(HTML.Tag.TR, ba);
            this.tagMap.put(HTML.Tag.TT, ca);
            this.tagMap.put(HTML.Tag.U, conv);
            this.tagMap.put(HTML.Tag.UL, ba);
            this.tagMap.put(HTML.Tag.VAR, ca);
            if (insertTag != null) {
                this.insertTag = insertTag;
                this.popDepth = popDepth;
                this.pushDepth = pushDepth;
                this.insertInsertTag = insertInsertTag;
                this.foundInsertTag = false;
            } else {
                this.foundInsertTag = true;
            }
            if (insertAfterImplied) {
                this.popDepth = popDepth;
                this.pushDepth = pushDepth;
                this.insertAfterImplied = true;
                this.foundInsertTag = false;
                this.midInsert = false;
                this.insertInsertTag = true;
                this.wantsTrailingNewline = wantsTrailingNewline;
            } else {
                boolean bl = this.midInsert = !this.emptyDocument && insertTag == null;
                if (this.midInsert) {
                    this.generateEndsSpecsForMidInsert();
                }
            }
            if (!this.emptyDocument && !this.midInsert) {
                HTML.Tag tagToInsertInto;
                int i;
                int targetOffset = Math.max(this.offset - 1, 0);
                Element elem = HTMLDocument.this.getCharacterElement(targetOffset);
                for (i = 0; i <= this.popDepth; ++i) {
                    elem = elem.getParentElement();
                }
                for (i = 0; i < this.pushDepth; ++i) {
                    int index = elem.getElementIndex(this.offset);
                    elem = elem.getElement(index);
                }
                AttributeSet attrs = elem.getAttributes();
                if (attrs != null && (tagToInsertInto = (HTML.Tag)attrs.getAttribute(StyleConstants.NameAttribute)) != null) {
                    this.inParagraph = tagToInsertInto.isParagraph();
                }
            }
        }

        private void generateEndsSpecsForMidInsert() {
            int count = this.heightToElementWithName(HTML.Tag.BODY, Math.max(0, this.offset - 1));
            boolean joinNext = false;
            if (count == -1 && this.offset > 0 && (count = this.heightToElementWithName(HTML.Tag.BODY, this.offset)) != -1) {
                count = this.depthTo(this.offset - 1) - 1;
                joinNext = true;
            }
            if (count == -1) {
                throw new RuntimeException("Must insert new content into body element-");
            }
            if (count != -1) {
                try {
                    if (!joinNext && this.offset > 0 && !HTMLDocument.this.getText(this.offset - 1, 1).equals("\n")) {
                        SimpleAttributeSet newAttrs = new SimpleAttributeSet();
                        newAttrs.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
                        DefaultStyledDocument.ElementSpec spec = new DefaultStyledDocument.ElementSpec(newAttrs, 3, NEWLINE, 0, 1);
                        this.parseBuffer.addElement(spec);
                    }
                }
                catch (BadLocationException newAttrs) {
                    // empty catch block
                }
                while (count-- > 0) {
                    this.parseBuffer.addElement(new DefaultStyledDocument.ElementSpec(null, 2));
                }
                if (joinNext) {
                    DefaultStyledDocument.ElementSpec spec = new DefaultStyledDocument.ElementSpec(null, 1);
                    spec.setDirection((short)5);
                    this.parseBuffer.addElement(spec);
                }
            }
        }

        private int depthTo(int offset) {
            Element e = HTMLDocument.this.getDefaultRootElement();
            int count = 0;
            while (!e.isLeaf()) {
                ++count;
                e = e.getElement(e.getElementIndex(offset));
            }
            return count;
        }

        private int heightToElementWithName(Object name, int offset) {
            Element e;
            int count = 0;
            for (e = HTMLDocument.this.getCharacterElement(offset).getParentElement(); e != null && e.getAttributes().getAttribute(StyleConstants.NameAttribute) != name; e = e.getParentElement()) {
                ++count;
            }
            return e == null ? -1 : count;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void adjustEndElement() {
            int length = HTMLDocument.this.getLength();
            if (length == 0) {
                return;
            }
            HTMLDocument.this.obtainLock();
            try {
                Element[] pPath = this.getPathTo(length - 1);
                int pLength = pPath.length;
                if (pLength > 1 && pPath[1].getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.BODY && pPath[1].getEndOffset() == length) {
                    String lastText = HTMLDocument.this.getText(length - 1, 1);
                    Element[] added = new Element[]{};
                    Element[] removed = new Element[1];
                    int index = pPath[0].getElementIndex(length);
                    removed[0] = pPath[0].getElement(index);
                    ((AbstractDocument.BranchElement)pPath[0]).replace(index, 1, added);
                    AbstractDocument.ElementEdit firstEdit = new AbstractDocument.ElementEdit(pPath[0], index, removed, added);
                    SimpleAttributeSet sas = new SimpleAttributeSet();
                    sas.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
                    sas.addAttribute("CR", Boolean.TRUE);
                    added = new Element[]{HTMLDocument.this.createLeafElement(pPath[pLength - 1], sas, length, length + 1)};
                    index = pPath[pLength - 1].getElementCount();
                    ((AbstractDocument.BranchElement)pPath[pLength - 1]).replace(index, 0, added);
                    AbstractDocument.DefaultDocumentEvent event = new AbstractDocument.DefaultDocumentEvent(length, 1, DocumentEvent.EventType.CHANGE);
                    event.addEdit(new AbstractDocument.ElementEdit(pPath[pLength - 1], index, new Element[0], added));
                    event.addEdit(firstEdit);
                    event.end();
                    HTMLDocument.this.fireChangedUpdate(event);
                    HTMLDocument.this.fireUndoableEditUpdate(new UndoableEditEvent(this, event));
                    if (lastText.equals("\n")) {
                        event = new AbstractDocument.DefaultDocumentEvent(length - 1, 1, DocumentEvent.EventType.REMOVE);
                        HTMLDocument.this.removeUpdate(event);
                        UndoableEdit u = HTMLDocument.this.getContent().remove(length - 1, 1);
                        if (u != null) {
                            event.addEdit(u);
                        }
                        HTMLDocument.this.postRemoveUpdate(event);
                        event.end();
                        HTMLDocument.this.fireRemoveUpdate(event);
                        HTMLDocument.this.fireUndoableEditUpdate(new UndoableEditEvent(this, event));
                    }
                }
            }
            catch (BadLocationException badLocationException) {
            }
            finally {
                HTMLDocument.this.releaseLock();
            }
        }

        private Element[] getPathTo(int offset) {
            ArrayList<Element> elements = new ArrayList<Element>();
            Element e = HTMLDocument.this.getDefaultRootElement();
            while (!e.isLeaf()) {
                elements.add(e);
                e = e.getElement(e.getElementIndex(offset));
            }
            Element[] retValue = elements.toArray(new Element[0]);
            return retValue;
        }

        @Override
        public void flush() throws BadLocationException {
            if (this.emptyDocument && !this.insertAfterImplied) {
                if (HTMLDocument.this.getLength() > 0 || this.parseBuffer.size() > 0) {
                    this.flushBuffer(true);
                    this.adjustEndElement();
                }
            } else {
                this.flushBuffer(true);
            }
        }

        @Override
        public void handleText(char[] data, int pos) {
            if (this.receivedEndHTML || this.midInsert && !this.inBody) {
                return;
            }
            if (HTMLDocument.this.getProperty(HTMLDocument.I18NProperty).equals(Boolean.FALSE)) {
                Object d = HTMLDocument.this.getProperty(TextAttribute.RUN_DIRECTION);
                if (d != null && d.equals(TextAttribute.RUN_DIRECTION_RTL)) {
                    HTMLDocument.this.putProperty(HTMLDocument.I18NProperty, Boolean.TRUE);
                } else if (SwingUtilities2.isComplexLayout(data, 0, data.length)) {
                    HTMLDocument.this.putProperty(HTMLDocument.I18NProperty, Boolean.TRUE);
                }
            }
            if (this.inTextArea) {
                this.textAreaContent(data);
            } else if (this.inPre) {
                this.preContent(data);
            } else if (this.inTitle) {
                HTMLDocument.this.putProperty("title", new String(data));
            } else if (this.option != null) {
                this.option.setLabel(new String(data));
            } else if (this.inStyle) {
                if (this.styles != null) {
                    this.styles.addElement(new String(data));
                }
            } else if (this.inBlock > 0) {
                if (!this.foundInsertTag && this.insertAfterImplied) {
                    this.foundInsertTag(false);
                    this.foundInsertTag = true;
                    this.impliedP = !HTMLDocument.this.insertInBody;
                    this.inParagraph = this.impliedP;
                }
                if (data.length >= 1) {
                    this.addContent(data, 0, data.length);
                }
            }
        }

        @Override
        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            if (this.receivedEndHTML) {
                return;
            }
            if (this.midInsert && !this.inBody) {
                if (t == HTML.Tag.BODY) {
                    this.inBody = true;
                    ++this.inBlock;
                }
                return;
            }
            if (!this.inBody && t == HTML.Tag.BODY) {
                this.inBody = true;
            }
            if (this.isStyleCSS && a.isDefined(HTML.Attribute.STYLE)) {
                String decl = (String)a.getAttribute(HTML.Attribute.STYLE);
                a.removeAttribute(HTML.Attribute.STYLE);
                this.styleAttributes = HTMLDocument.this.getStyleSheet().getDeclaration(decl);
                a.addAttributes(this.styleAttributes);
            } else {
                this.styleAttributes = null;
            }
            TagAction action = this.tagMap.get(t);
            if (action != null) {
                action.start(t, a);
            }
        }

        @Override
        public void handleComment(char[] data, int pos) {
            TagAction action;
            if (this.receivedEndHTML) {
                this.addExternalComment(new String(data));
                return;
            }
            if (this.inStyle) {
                if (this.styles != null) {
                    this.styles.addElement(new String(data));
                }
            } else if (HTMLDocument.this.getPreservesUnknownTags()) {
                if (this.inBlock == 0 && (this.foundInsertTag || this.insertTag != HTML.Tag.COMMENT)) {
                    this.addExternalComment(new String(data));
                    return;
                }
                SimpleAttributeSet sas = new SimpleAttributeSet();
                sas.addAttribute(HTML.Attribute.COMMENT, new String(data));
                this.addSpecialElement(HTML.Tag.COMMENT, sas);
            }
            if ((action = this.tagMap.get(HTML.Tag.COMMENT)) != null) {
                action.start(HTML.Tag.COMMENT, new SimpleAttributeSet());
                action.end(HTML.Tag.COMMENT);
            }
        }

        private void addExternalComment(String comment) {
            Vector comments = HTMLDocument.this.getProperty(HTMLDocument.AdditionalComments);
            if (comments != null && !(comments instanceof Vector)) {
                return;
            }
            if (comments == null) {
                comments = new Vector();
                HTMLDocument.this.putProperty(HTMLDocument.AdditionalComments, comments);
            }
            Vector v = comments;
            v.addElement(comment);
        }

        @Override
        public void handleEndTag(HTML.Tag t, int pos) {
            TagAction action;
            if (this.receivedEndHTML || this.midInsert && !this.inBody) {
                return;
            }
            if (t == HTML.Tag.HTML) {
                this.receivedEndHTML = true;
            }
            if (t == HTML.Tag.BODY) {
                this.inBody = false;
                if (this.midInsert) {
                    --this.inBlock;
                }
            }
            if ((action = this.tagMap.get(t)) != null) {
                action.end(t);
            }
        }

        @Override
        public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            if (this.receivedEndHTML || this.midInsert && !this.inBody) {
                return;
            }
            if (this.isStyleCSS && a.isDefined(HTML.Attribute.STYLE)) {
                String decl = (String)a.getAttribute(HTML.Attribute.STYLE);
                a.removeAttribute(HTML.Attribute.STYLE);
                this.styleAttributes = HTMLDocument.this.getStyleSheet().getDeclaration(decl);
                a.addAttributes(this.styleAttributes);
            } else {
                this.styleAttributes = null;
            }
            TagAction action = this.tagMap.get(t);
            if (action != null) {
                action.start(t, a);
                action.end(t);
            } else if (HTMLDocument.this.getPreservesUnknownTags()) {
                this.addSpecialElement(t, a);
            }
        }

        @Override
        public void handleEndOfLineString(String eol) {
            if (this.emptyDocument && eol != null) {
                HTMLDocument.this.putProperty("__EndOfLine__", eol);
            }
        }

        protected void registerTag(HTML.Tag t, TagAction a) {
            this.tagMap.put(t, a);
        }

        protected void pushCharacterStyle() {
            this.charAttrStack.push(this.charAttr.copyAttributes());
        }

        protected void popCharacterStyle() {
            if (!this.charAttrStack.empty()) {
                this.charAttr = (MutableAttributeSet)this.charAttrStack.peek();
                this.charAttrStack.pop();
            }
        }

        protected void textAreaContent(char[] data) {
            try {
                this.textAreaDocument.insertString(this.textAreaDocument.getLength(), new String(data), null);
            }
            catch (BadLocationException badLocationException) {
                // empty catch block
            }
        }

        protected void preContent(char[] data) {
            int last = 0;
            for (int i = 0; i < data.length; ++i) {
                if (data[i] != '\n') continue;
                this.addContent(data, last, i - last + 1);
                this.blockClose(HTML.Tag.IMPLIED);
                SimpleAttributeSet a = new SimpleAttributeSet();
                a.addAttribute(CSS.Attribute.WHITE_SPACE, "pre");
                this.blockOpen(HTML.Tag.IMPLIED, a);
                last = i + 1;
            }
            if (last < data.length) {
                this.addContent(data, last, data.length - last);
            }
        }

        protected void blockOpen(HTML.Tag t, MutableAttributeSet attr) {
            if (this.impliedP) {
                this.blockClose(HTML.Tag.IMPLIED);
            }
            ++this.inBlock;
            if (!this.canInsertTag(t, attr, true)) {
                return;
            }
            if (attr.isDefined(IMPLIED)) {
                attr.removeAttribute(IMPLIED);
            }
            this.lastWasNewline = false;
            attr.addAttribute(StyleConstants.NameAttribute, t);
            DefaultStyledDocument.ElementSpec es = new DefaultStyledDocument.ElementSpec(attr.copyAttributes(), 1);
            this.parseBuffer.addElement(es);
        }

        protected void blockClose(HTML.Tag t) {
            DefaultStyledDocument.ElementSpec prev;
            --this.inBlock;
            if (!this.foundInsertTag) {
                return;
            }
            if (!this.lastWasNewline) {
                this.pushCharacterStyle();
                this.charAttr.addAttribute("CR", Boolean.TRUE);
                this.addContent(NEWLINE, 0, 1, true);
                this.popCharacterStyle();
                this.lastWasNewline = true;
            }
            if (this.impliedP) {
                this.impliedP = false;
                this.inParagraph = false;
                if (t != HTML.Tag.IMPLIED) {
                    this.blockClose(HTML.Tag.IMPLIED);
                }
            }
            DefaultStyledDocument.ElementSpec elementSpec = prev = this.parseBuffer.size() > 0 ? this.parseBuffer.lastElement() : null;
            if (prev != null && prev.getType() == 1) {
                char[] one = new char[]{' '};
                this.addContent(one, 0, 1);
            }
            DefaultStyledDocument.ElementSpec es = new DefaultStyledDocument.ElementSpec(null, 2);
            this.parseBuffer.addElement(es);
        }

        protected void addContent(char[] data, int offs, int length) {
            this.addContent(data, offs, length, true);
        }

        protected void addContent(char[] data, int offs, int length, boolean generateImpliedPIfNecessary) {
            if (!this.foundInsertTag) {
                return;
            }
            if (generateImpliedPIfNecessary && !this.inParagraph && !this.inPre) {
                this.blockOpen(HTML.Tag.IMPLIED, new SimpleAttributeSet());
                this.inParagraph = true;
                this.impliedP = true;
            }
            this.emptyAnchor = false;
            this.charAttr.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
            AttributeSet a = this.charAttr.copyAttributes();
            DefaultStyledDocument.ElementSpec es = new DefaultStyledDocument.ElementSpec(a, 3, data, offs, length);
            this.parseBuffer.addElement(es);
            if (this.parseBuffer.size() > this.threshold) {
                if (this.threshold <= 10000) {
                    this.threshold *= 5;
                }
                try {
                    this.flushBuffer(false);
                }
                catch (BadLocationException badLocationException) {
                    // empty catch block
                }
            }
            if (length > 0) {
                this.lastWasNewline = data[offs + length - 1] == '\n';
            }
        }

        protected void addSpecialElement(HTML.Tag t, MutableAttributeSet a) {
            if (t != HTML.Tag.FRAME && !this.inParagraph && !this.inPre) {
                this.nextTagAfterPImplied = t;
                this.blockOpen(HTML.Tag.IMPLIED, new SimpleAttributeSet());
                this.nextTagAfterPImplied = null;
                this.inParagraph = true;
                this.impliedP = true;
            }
            if (!this.canInsertTag(t, a, t.isBlock())) {
                return;
            }
            if (a.isDefined(IMPLIED)) {
                a.removeAttribute(IMPLIED);
            }
            this.emptyAnchor = false;
            a.addAttributes(this.charAttr);
            a.addAttribute(StyleConstants.NameAttribute, t);
            char[] one = new char[]{' '};
            DefaultStyledDocument.ElementSpec es = new DefaultStyledDocument.ElementSpec(a.copyAttributes(), 3, one, 0, 1);
            this.parseBuffer.addElement(es);
            if (t == HTML.Tag.FRAME) {
                this.lastWasNewline = true;
            }
        }

        void flushBuffer(boolean endOfStream) throws BadLocationException {
            int oldLength = HTMLDocument.this.getLength();
            int size = this.parseBuffer.size();
            if (endOfStream && (this.insertTag != null || this.insertAfterImplied) && size > 0) {
                this.adjustEndSpecsForPartialInsert();
                size = this.parseBuffer.size();
            }
            Object[] spec = new DefaultStyledDocument.ElementSpec[size];
            this.parseBuffer.copyInto(spec);
            if (oldLength == 0 && this.insertTag == null && !this.insertAfterImplied) {
                HTMLDocument.this.create((DefaultStyledDocument.ElementSpec[])spec);
            } else {
                HTMLDocument.this.insert(this.offset, (DefaultStyledDocument.ElementSpec[])spec);
            }
            this.parseBuffer.removeAllElements();
            this.offset += HTMLDocument.this.getLength() - oldLength;
            ++this.flushCount;
        }

        private void adjustEndSpecsForPartialInsert() {
            int size = this.parseBuffer.size();
            if (this.insertTagDepthDelta < 0) {
                for (int removeCounter = this.insertTagDepthDelta; removeCounter < 0 && size >= 0 && this.parseBuffer.elementAt(size - 1).getType() == 2; ++removeCounter) {
                    this.parseBuffer.removeElementAt(--size);
                }
            }
            if (!(this.flushCount != 0 || this.insertAfterImplied && this.wantsTrailingNewline)) {
                int index = 0;
                if (this.pushDepth > 0 && this.parseBuffer.elementAt(0).getType() == 3) {
                    ++index;
                }
                int cCount = 0;
                int cStart = index += this.popDepth + this.pushDepth;
                while (index < size && this.parseBuffer.elementAt(index).getType() == 3) {
                    ++index;
                    ++cCount;
                }
                if (cCount > 1) {
                    char[] lastText;
                    while (index < size && this.parseBuffer.elementAt(index).getType() == 2) {
                        ++index;
                    }
                    if (index == size && (lastText = this.parseBuffer.elementAt(cStart + cCount - 1).getArray()).length == 1 && lastText[0] == NEWLINE[0]) {
                        index = cStart + cCount - 1;
                        while (size > index) {
                            this.parseBuffer.removeElementAt(--size);
                        }
                    }
                }
            }
            if (this.wantsTrailingNewline) {
                for (int counter = this.parseBuffer.size() - 1; counter >= 0; --counter) {
                    DefaultStyledDocument.ElementSpec spec = this.parseBuffer.elementAt(counter);
                    if (spec.getType() != 3) continue;
                    if (spec.getArray()[spec.getLength() - 1] == '\n') break;
                    SimpleAttributeSet attrs = new SimpleAttributeSet();
                    attrs.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
                    this.parseBuffer.insertElementAt(new DefaultStyledDocument.ElementSpec(attrs, 3, NEWLINE, 0, 1), counter + 1);
                    break;
                }
            }
        }

        void addCSSRules(String rules) {
            StyleSheet ss = HTMLDocument.this.getStyleSheet();
            ss.addRule(rules);
        }

        void linkCSSStyleSheet(String href) {
            URL url;
            try {
                URL uRL = url = new URL(HTMLDocument.this.base, href);
            }
            catch (MalformedURLException mfe) {
                try {
                    URL uRL = url = new URL(href);
                }
                catch (MalformedURLException mfe2) {
                    url = null;
                }
            }
            if (url != null) {
                HTMLDocument.this.getStyleSheet().importStyleSheet(url);
            }
        }

        private boolean canInsertTag(HTML.Tag t, AttributeSet attr, boolean isBlockTag) {
            if (!this.foundInsertTag) {
                boolean nextTagIsInsertTag;
                boolean needPImplied;
                boolean bl = needPImplied = t == HTML.Tag.IMPLIED && !this.inParagraph && !this.inPre;
                if (needPImplied && this.nextTagAfterPImplied != null ? this.insertTag != null && (!(nextTagIsInsertTag = this.isInsertTag(this.nextTagAfterPImplied)) || !this.insertInsertTag) : this.insertTag != null && !this.isInsertTag(t) || this.insertAfterImplied && (attr == null || attr.isDefined(IMPLIED) || t == HTML.Tag.IMPLIED)) {
                    return false;
                }
                this.foundInsertTag(isBlockTag);
                if (!this.insertInsertTag) {
                    return false;
                }
            }
            return true;
        }

        private boolean isInsertTag(HTML.Tag tag) {
            return this.insertTag == tag;
        }

        private void foundInsertTag(boolean isBlockTag) {
            int counter;
            this.foundInsertTag = true;
            if (!(this.insertAfterImplied || this.popDepth <= 0 && this.pushDepth <= 0)) {
                try {
                    if (this.offset == 0 || !HTMLDocument.this.getText(this.offset - 1, 1).equals("\n")) {
                        SimpleAttributeSet newAttrs = null;
                        boolean joinP = true;
                        if (this.offset != 0) {
                            Element charElement = HTMLDocument.this.getCharacterElement(this.offset - 1);
                            AttributeSet attrs = charElement.getAttributes();
                            if (attrs.isDefined(StyleConstants.ComposedTextAttribute)) {
                                joinP = false;
                            } else {
                                HTML.Tag tag;
                                Object name = attrs.getAttribute(StyleConstants.NameAttribute);
                                if (name instanceof HTML.Tag && ((tag = (HTML.Tag)name) == HTML.Tag.IMG || tag == HTML.Tag.HR || tag == HTML.Tag.COMMENT || tag instanceof HTML.UnknownTag)) {
                                    joinP = false;
                                }
                            }
                        }
                        if (!joinP) {
                            newAttrs = new SimpleAttributeSet();
                            newAttrs.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
                        }
                        DefaultStyledDocument.ElementSpec es = new DefaultStyledDocument.ElementSpec(newAttrs, 3, NEWLINE, 0, NEWLINE.length);
                        if (joinP) {
                            es.setDirection((short)4);
                        }
                        this.parseBuffer.addElement(es);
                    }
                }
                catch (BadLocationException newAttrs) {
                    // empty catch block
                }
            }
            for (counter = 0; counter < this.popDepth; ++counter) {
                this.parseBuffer.addElement(new DefaultStyledDocument.ElementSpec(null, 2));
            }
            for (counter = 0; counter < this.pushDepth; ++counter) {
                DefaultStyledDocument.ElementSpec es = new DefaultStyledDocument.ElementSpec(null, 1);
                es.setDirection((short)5);
                this.parseBuffer.addElement(es);
            }
            this.insertTagDepthDelta = this.depthTo(Math.max(0, this.offset - 1)) - this.popDepth + this.pushDepth - this.inBlock;
            if (isBlockTag) {
                ++this.insertTagDepthDelta;
            } else {
                --this.insertTagDepthDelta;
                this.inParagraph = true;
                this.lastWasNewline = false;
            }
        }

        public class TagAction {
            public TagAction(HTMLReader this$1) {
            }

            public void start(HTML.Tag t, MutableAttributeSet a) {
            }

            public void end(HTML.Tag t) {
            }
        }

        public class BlockAction
        extends TagAction {
            public BlockAction() {
                super(HTMLReader.this);
            }

            @Override
            public void start(HTML.Tag t, MutableAttributeSet attr) {
                HTMLReader.this.blockOpen(t, attr);
            }

            @Override
            public void end(HTML.Tag t) {
                HTMLReader.this.blockClose(t);
            }
        }

        public class ParagraphAction
        extends BlockAction {
            @Override
            public void start(HTML.Tag t, MutableAttributeSet a) {
                super.start(t, a);
                HTMLReader.this.inParagraph = true;
            }

            @Override
            public void end(HTML.Tag t) {
                super.end(t);
                HTMLReader.this.inParagraph = false;
            }
        }

        public class CharacterAction
        extends TagAction {
            public CharacterAction() {
                super(HTMLReader.this);
            }

            @Override
            public void start(HTML.Tag t, MutableAttributeSet attr) {
                HTMLReader.this.pushCharacterStyle();
                if (!HTMLReader.this.foundInsertTag) {
                    boolean insert = HTMLReader.this.canInsertTag(t, attr, false);
                    if (HTMLReader.this.foundInsertTag && !HTMLReader.this.inParagraph) {
                        HTMLReader.this.impliedP = true;
                        HTMLReader.this.inParagraph = true;
                    }
                    if (!insert) {
                        return;
                    }
                }
                if (attr.isDefined(HTMLEditorKit.ParserCallback.IMPLIED)) {
                    attr.removeAttribute(HTMLEditorKit.ParserCallback.IMPLIED);
                }
                HTMLReader.this.charAttr.addAttribute(t, attr.copyAttributes());
                if (HTMLReader.this.styleAttributes != null) {
                    HTMLReader.this.charAttr.addAttributes(HTMLReader.this.styleAttributes);
                }
                this.convertAttributes(t, attr);
            }

            @Override
            public void end(HTML.Tag t) {
                HTMLReader.this.popCharacterStyle();
            }

            void convertAttributes(HTML.Tag t, MutableAttributeSet attr) {
            }
        }

        public class SpecialAction
        extends TagAction {
            public SpecialAction() {
                super(HTMLReader.this);
            }

            @Override
            public void start(HTML.Tag t, MutableAttributeSet a) {
                HTMLReader.this.addSpecialElement(t, a);
            }
        }

        public class FormAction
        extends SpecialAction {
            Object selectModel;
            int optionCount;

            @Override
            public void start(HTML.Tag t, MutableAttributeSet attr) {
                if (t == HTML.Tag.INPUT) {
                    String type = (String)attr.getAttribute(HTML.Attribute.TYPE);
                    if (type == null) {
                        type = "text";
                        attr.addAttribute(HTML.Attribute.TYPE, "text");
                    }
                    this.setModel(type, attr);
                } else if (t == HTML.Tag.TEXTAREA) {
                    HTMLReader.this.inTextArea = true;
                    HTMLReader.this.textAreaDocument = new TextAreaDocument();
                    attr.addAttribute(StyleConstants.ModelAttribute, HTMLReader.this.textAreaDocument);
                } else if (t == HTML.Tag.SELECT) {
                    boolean multiple;
                    int size = HTML.getIntegerAttributeValue(attr, HTML.Attribute.SIZE, 1);
                    boolean bl = multiple = attr.getAttribute(HTML.Attribute.MULTIPLE) != null;
                    if (size > 1 || multiple) {
                        OptionListModel m = new OptionListModel();
                        if (multiple) {
                            m.setSelectionMode(2);
                        }
                        this.selectModel = m;
                    } else {
                        this.selectModel = new OptionComboBoxModel();
                    }
                    attr.addAttribute(StyleConstants.ModelAttribute, this.selectModel);
                }
                if (t == HTML.Tag.OPTION) {
                    HTMLReader.this.option = new Option(attr);
                    if (this.selectModel instanceof OptionListModel) {
                        OptionListModel m = (OptionListModel)this.selectModel;
                        m.addElement(HTMLReader.this.option);
                        if (HTMLReader.this.option.isSelected()) {
                            m.addSelectionInterval(this.optionCount, this.optionCount);
                            m.setInitialSelection(this.optionCount);
                        }
                    } else if (this.selectModel instanceof OptionComboBoxModel) {
                        OptionComboBoxModel m = (OptionComboBoxModel)this.selectModel;
                        m.addElement(HTMLReader.this.option);
                        if (HTMLReader.this.option.isSelected()) {
                            m.setSelectedItem(HTMLReader.this.option);
                            m.setInitialSelection(HTMLReader.this.option);
                        }
                    }
                    ++this.optionCount;
                } else {
                    super.start(t, attr);
                }
            }

            @Override
            public void end(HTML.Tag t) {
                if (t == HTML.Tag.OPTION) {
                    HTMLReader.this.option = null;
                } else {
                    if (t == HTML.Tag.SELECT) {
                        this.selectModel = null;
                        this.optionCount = 0;
                    } else if (t == HTML.Tag.TEXTAREA) {
                        HTMLReader.this.inTextArea = false;
                        HTMLReader.this.textAreaDocument.storeInitialText();
                    }
                    super.end(t);
                }
            }

            void setModel(String type, MutableAttributeSet attr) {
                if (type.equals("submit") || type.equals("reset") || type.equals("image")) {
                    attr.addAttribute(StyleConstants.ModelAttribute, new DefaultButtonModel());
                } else if (type.equals("text") || type.equals("password")) {
                    int maxLength = HTML.getIntegerAttributeValue(attr, HTML.Attribute.MAXLENGTH, -1);
                    PlainDocument doc = maxLength > 0 ? new FixedLengthDocument(maxLength) : new PlainDocument();
                    String value = (String)attr.getAttribute(HTML.Attribute.VALUE);
                    try {
                        doc.insertString(0, value, null);
                    }
                    catch (BadLocationException badLocationException) {
                        // empty catch block
                    }
                    attr.addAttribute(StyleConstants.ModelAttribute, doc);
                } else if (type.equals("file")) {
                    attr.addAttribute(StyleConstants.ModelAttribute, new PlainDocument());
                } else if (type.equals("checkbox") || type.equals("radio")) {
                    JToggleButton.ToggleButtonModel model = new JToggleButton.ToggleButtonModel();
                    if (type.equals("radio")) {
                        ButtonGroup radioButtonGroup;
                        String name = (String)attr.getAttribute(HTML.Attribute.NAME);
                        if (HTMLDocument.this.radioButtonGroupsMap == null) {
                            HTMLDocument.this.radioButtonGroupsMap = new HashMap();
                        }
                        if ((radioButtonGroup = HTMLDocument.this.radioButtonGroupsMap.get(name)) == null) {
                            radioButtonGroup = new ButtonGroup();
                            HTMLDocument.this.radioButtonGroupsMap.put(name, radioButtonGroup);
                        }
                        model.setGroup(radioButtonGroup);
                    }
                    boolean checked = attr.getAttribute(HTML.Attribute.CHECKED) != null;
                    model.setSelected(checked);
                    attr.addAttribute(StyleConstants.ModelAttribute, model);
                }
            }
        }

        public class HiddenAction
        extends TagAction {
            public HiddenAction() {
                super(HTMLReader.this);
            }

            @Override
            public void start(HTML.Tag t, MutableAttributeSet a) {
                HTMLReader.this.addSpecialElement(t, a);
            }

            @Override
            public void end(HTML.Tag t) {
                if (!this.isEmpty(t)) {
                    SimpleAttributeSet a = new SimpleAttributeSet();
                    a.addAttribute(HTML.Attribute.ENDTAG, "true");
                    HTMLReader.this.addSpecialElement(t, a);
                }
            }

            boolean isEmpty(HTML.Tag t) {
                return t != HTML.Tag.APPLET && t != HTML.Tag.SCRIPT;
            }
        }

        final class ConvertAction
        extends CharacterAction {
            ConvertAction() {
            }

            @Override
            void convertAttributes(HTML.Tag t, MutableAttributeSet attr) {
                StyleSheet sheet = HTMLDocument.this.getStyleSheet();
                if (t == HTML.Tag.B) {
                    sheet.addCSSAttribute(HTMLReader.this.charAttr, CSS.Attribute.FONT_WEIGHT, "bold");
                } else if (t == HTML.Tag.I) {
                    sheet.addCSSAttribute(HTMLReader.this.charAttr, CSS.Attribute.FONT_STYLE, "italic");
                } else if (t == HTML.Tag.U) {
                    Object v = HTMLReader.this.charAttr.getAttribute(CSS.Attribute.TEXT_DECORATION);
                    String value = "underline";
                    value = v != null ? value + "," + v.toString() : value;
                    sheet.addCSSAttribute(HTMLReader.this.charAttr, CSS.Attribute.TEXT_DECORATION, value);
                } else if (t == HTML.Tag.STRIKE || t == HTML.Tag.S) {
                    Object v = HTMLReader.this.charAttr.getAttribute(CSS.Attribute.TEXT_DECORATION);
                    String value = "line-through";
                    value = v != null ? value + "," + v.toString() : value;
                    sheet.addCSSAttribute(HTMLReader.this.charAttr, CSS.Attribute.TEXT_DECORATION, value);
                } else if (t == HTML.Tag.SUP) {
                    Object v = HTMLReader.this.charAttr.getAttribute(CSS.Attribute.VERTICAL_ALIGN);
                    String value = "sup";
                    value = v != null ? value + "," + v.toString() : value;
                    sheet.addCSSAttribute(HTMLReader.this.charAttr, CSS.Attribute.VERTICAL_ALIGN, value);
                } else if (t == HTML.Tag.SUB) {
                    Object v = HTMLReader.this.charAttr.getAttribute(CSS.Attribute.VERTICAL_ALIGN);
                    String value = "sub";
                    value = v != null ? value + "," + v.toString() : value;
                    sheet.addCSSAttribute(HTMLReader.this.charAttr, CSS.Attribute.VERTICAL_ALIGN, value);
                } else if (t == HTML.Tag.FONT) {
                    String size;
                    String face;
                    String color = (String)attr.getAttribute(HTML.Attribute.COLOR);
                    if (color != null) {
                        sheet.addCSSAttribute(HTMLReader.this.charAttr, CSS.Attribute.COLOR, color);
                    }
                    if ((face = (String)attr.getAttribute(HTML.Attribute.FACE)) != null) {
                        sheet.addCSSAttribute(HTMLReader.this.charAttr, CSS.Attribute.FONT_FAMILY, face);
                    }
                    if ((size = (String)attr.getAttribute(HTML.Attribute.SIZE)) != null) {
                        sheet.addCSSAttributeFromHTML(HTMLReader.this.charAttr, CSS.Attribute.FONT_SIZE, size);
                    }
                }
            }
        }

        class AnchorAction
        extends CharacterAction {
            AnchorAction() {
            }

            @Override
            public void start(HTML.Tag t, MutableAttributeSet attr) {
                HTMLReader.this.emptyAnchor = true;
                super.start(t, attr);
            }

            @Override
            public void end(HTML.Tag t) {
                if (HTMLReader.this.emptyAnchor) {
                    char[] one = new char[]{'\n'};
                    HTMLReader.this.addContent(one, 0, 1);
                }
                super.end(t);
            }
        }

        class AreaAction
        extends TagAction {
            AreaAction() {
                super(HTMLReader.this);
            }

            @Override
            public void start(HTML.Tag t, MutableAttributeSet a) {
                if (HTMLReader.this.lastMap != null) {
                    HTMLReader.this.lastMap.addArea(a.copyAttributes());
                }
            }

            @Override
            public void end(HTML.Tag t) {
            }
        }

        class BaseAction
        extends TagAction {
            BaseAction() {
                super(HTMLReader.this);
            }

            @Override
            public void start(HTML.Tag t, MutableAttributeSet attr) {
                String href = (String)attr.getAttribute(HTML.Attribute.HREF);
                if (href != null) {
                    try {
                        URL newBase = new URL(HTMLDocument.this.base, href);
                        HTMLDocument.this.setBase(newBase);
                        HTMLDocument.this.hasBaseTag = true;
                    }
                    catch (MalformedURLException malformedURLException) {
                        // empty catch block
                    }
                }
                HTMLDocument.this.baseTarget = (String)attr.getAttribute(HTML.Attribute.TARGET);
            }
        }

        private class FormTagAction
        extends BlockAction {
            private FormTagAction() {
            }

            @Override
            public void start(HTML.Tag t, MutableAttributeSet attr) {
                super.start(t, attr);
                HTMLDocument.this.radioButtonGroupsMap = new HashMap();
            }

            @Override
            public void end(HTML.Tag t) {
                super.end(t);
                HTMLDocument.this.radioButtonGroupsMap = null;
            }
        }

        class HeadAction
        extends BlockAction {
            HeadAction() {
            }

            @Override
            public void start(HTML.Tag t, MutableAttributeSet a) {
                HTMLReader.this.inHead = true;
                if (HTMLReader.this.insertTag == null && !HTMLReader.this.insertAfterImplied || HTMLReader.this.insertTag == HTML.Tag.HEAD || HTMLReader.this.insertAfterImplied && (HTMLReader.this.foundInsertTag || !a.isDefined(HTMLEditorKit.ParserCallback.IMPLIED))) {
                    super.start(t, a);
                }
            }

            @Override
            public void end(HTML.Tag t) {
                HTMLReader.this.inStyle = false;
                HTMLReader.this.inHead = false;
                if (HTMLReader.this.styles != null) {
                    boolean isDefaultCSS = HTMLReader.this.isStyleCSS;
                    int counter = 0;
                    int maxCounter = HTMLReader.this.styles.size();
                    while (counter < maxCounter) {
                        String type;
                        boolean isCSS;
                        Object value = HTMLReader.this.styles.elementAt(counter);
                        if (value == HTML.Tag.LINK) {
                            this.handleLink((AttributeSet)HTMLReader.this.styles.elementAt(++counter));
                            ++counter;
                            continue;
                        }
                        boolean bl = isCSS = (type = (String)HTMLReader.this.styles.elementAt(++counter)) == null ? isDefaultCSS : type.equals("text/css");
                        while (++counter < maxCounter && HTMLReader.this.styles.elementAt(counter) instanceof String) {
                            if (!isCSS) continue;
                            HTMLReader.this.addCSSRules((String)HTMLReader.this.styles.elementAt(counter));
                        }
                    }
                }
                if (HTMLReader.this.insertTag == null && !HTMLReader.this.insertAfterImplied || HTMLReader.this.insertTag == HTML.Tag.HEAD || HTMLReader.this.insertAfterImplied && HTMLReader.this.foundInsertTag) {
                    super.end(t);
                }
            }

            boolean isEmpty(HTML.Tag t) {
                return false;
            }

            private void handleLink(AttributeSet attr) {
                String type = (String)attr.getAttribute(HTML.Attribute.TYPE);
                if (type == null) {
                    type = HTMLDocument.this.getDefaultStyleSheetType();
                }
                if (type.equals("text/css")) {
                    String rel = (String)attr.getAttribute(HTML.Attribute.REL);
                    String title = (String)attr.getAttribute(HTML.Attribute.TITLE);
                    String media = (String)attr.getAttribute(HTML.Attribute.MEDIA);
                    media = media == null ? "all" : media.toLowerCase();
                    if (rel != null) {
                        rel = rel.toLowerCase();
                        if ((media.contains("all") || media.contains("screen")) && (rel.equals("stylesheet") || rel.equals("alternate stylesheet") && title.equals(HTMLReader.this.defaultStyle))) {
                            HTMLReader.this.linkCSSStyleSheet((String)attr.getAttribute(HTML.Attribute.HREF));
                        }
                    }
                }
            }
        }

        public class IsindexAction
        extends TagAction {
            public IsindexAction() {
                super(HTMLReader.this);
            }

            @Override
            public void start(HTML.Tag t, MutableAttributeSet a) {
                HTMLReader.this.blockOpen(HTML.Tag.IMPLIED, new SimpleAttributeSet());
                HTMLReader.this.addSpecialElement(t, a);
                HTMLReader.this.blockClose(HTML.Tag.IMPLIED);
            }
        }

        class LinkAction
        extends HiddenAction {
            LinkAction() {
            }

            @Override
            public void start(HTML.Tag t, MutableAttributeSet a) {
                String rel = (String)a.getAttribute(HTML.Attribute.REL);
                if (rel != null && ((rel = rel.toLowerCase()).equals("stylesheet") || rel.equals("alternate stylesheet"))) {
                    if (HTMLReader.this.styles == null) {
                        HTMLReader.this.styles = new Vector(3);
                    }
                    HTMLReader.this.styles.addElement(t);
                    HTMLReader.this.styles.addElement(a.copyAttributes());
                }
                super.start(t, a);
            }
        }

        class MapAction
        extends TagAction {
            MapAction() {
                super(HTMLReader.this);
            }

            @Override
            public void start(HTML.Tag t, MutableAttributeSet a) {
                HTMLReader.this.lastMap = new Map((String)a.getAttribute(HTML.Attribute.NAME));
                HTMLDocument.this.addMap(HTMLReader.this.lastMap);
            }

            @Override
            public void end(HTML.Tag t) {
            }
        }

        class MetaAction
        extends HiddenAction {
            MetaAction() {
            }

            @Override
            public void start(HTML.Tag t, MutableAttributeSet a) {
                Object equiv = a.getAttribute(HTML.Attribute.HTTPEQUIV);
                if (equiv != null) {
                    if ((equiv = ((String)equiv).toLowerCase()).equals("content-style-type")) {
                        String value = (String)a.getAttribute(HTML.Attribute.CONTENT);
                        HTMLDocument.this.setDefaultStyleSheetType(value);
                        HTMLReader.this.isStyleCSS = "text/css".equals(HTMLDocument.this.getDefaultStyleSheetType());
                    } else if (equiv.equals("default-style")) {
                        HTMLReader.this.defaultStyle = (String)a.getAttribute(HTML.Attribute.CONTENT);
                    }
                }
                super.start(t, a);
            }

            @Override
            boolean isEmpty(HTML.Tag t) {
                return true;
            }
        }

        class ObjectAction
        extends SpecialAction {
            ObjectAction() {
            }

            @Override
            public void start(HTML.Tag t, MutableAttributeSet a) {
                if (t == HTML.Tag.PARAM) {
                    this.addParameter(a);
                } else {
                    super.start(t, a);
                }
            }

            @Override
            public void end(HTML.Tag t) {
                if (t != HTML.Tag.PARAM) {
                    super.end(t);
                }
            }

            void addParameter(AttributeSet a) {
                String name = (String)a.getAttribute(HTML.Attribute.NAME);
                String value = (String)a.getAttribute(HTML.Attribute.VALUE);
                if (name != null && value != null) {
                    DefaultStyledDocument.ElementSpec objSpec = HTMLReader.this.parseBuffer.lastElement();
                    MutableAttributeSet objAttr = (MutableAttributeSet)objSpec.getAttributes();
                    objAttr.addAttribute(name, value);
                }
            }
        }

        public class PreAction
        extends BlockAction {
            @Override
            public void start(HTML.Tag t, MutableAttributeSet attr) {
                HTMLReader.this.inPre = true;
                HTMLReader.this.blockOpen(t, attr);
                attr.addAttribute(CSS.Attribute.WHITE_SPACE, "pre");
                HTMLReader.this.blockOpen(HTML.Tag.IMPLIED, attr);
            }

            @Override
            public void end(HTML.Tag t) {
                HTMLReader.this.blockClose(HTML.Tag.IMPLIED);
                HTMLReader.this.inPre = false;
                HTMLReader.this.blockClose(t);
            }
        }

        final class ConvertSpanAction
        extends CharacterAction {
            ConvertSpanAction() {
            }

            @Override
            void convertAttributes(HTML.Tag t, MutableAttributeSet attr) {
                Object newDecoration = attr.getAttribute(CSS.Attribute.TEXT_DECORATION);
                Object previousDecoration = HTMLReader.this.charAttrStack.peek().getAttribute(CSS.Attribute.TEXT_DECORATION);
                if (newDecoration != null && !"none".equals(newDecoration.toString()) && previousDecoration != null && !"none".equals(previousDecoration.toString())) {
                    StyleSheet sheet = HTMLDocument.this.getStyleSheet();
                    sheet.addCSSAttribute(HTMLReader.this.charAttr, CSS.Attribute.TEXT_DECORATION, CSS.mergeTextDecoration(String.valueOf(newDecoration) + "," + String.valueOf(previousDecoration)).toString());
                }
            }
        }

        class StyleAction
        extends TagAction {
            StyleAction() {
                super(HTMLReader.this);
            }

            @Override
            public void start(HTML.Tag t, MutableAttributeSet a) {
                if (HTMLReader.this.inHead) {
                    if (HTMLReader.this.styles == null) {
                        HTMLReader.this.styles = new Vector(3);
                    }
                    HTMLReader.this.styles.addElement(t);
                    HTMLReader.this.styles.addElement(a.getAttribute(HTML.Attribute.TYPE));
                    HTMLReader.this.inStyle = true;
                }
            }

            @Override
            public void end(HTML.Tag t) {
                HTMLReader.this.inStyle = false;
            }

            boolean isEmpty(HTML.Tag t) {
                return false;
            }
        }

        class TitleAction
        extends HiddenAction {
            TitleAction() {
            }

            @Override
            public void start(HTML.Tag t, MutableAttributeSet attr) {
                HTMLReader.this.inTitle = true;
                super.start(t, attr);
            }

            @Override
            public void end(HTML.Tag t) {
                HTMLReader.this.inTitle = false;
                super.end(t);
            }

            @Override
            boolean isEmpty(HTML.Tag t) {
                return false;
            }
        }
    }

    static class LeafIterator
    extends Iterator {
        private int endOffset;
        private HTML.Tag tag;
        private ElementIterator pos;

        LeafIterator(HTML.Tag t, Document doc) {
            this.tag = t;
            this.pos = new ElementIterator(doc);
            this.endOffset = 0;
            this.next();
        }

        @Override
        public AttributeSet getAttributes() {
            Element elem = this.pos.current();
            if (elem != null) {
                AttributeSet a = (AttributeSet)elem.getAttributes().getAttribute(this.tag);
                if (a == null) {
                    a = elem.getAttributes();
                }
                return a;
            }
            return null;
        }

        @Override
        public int getStartOffset() {
            Element elem = this.pos.current();
            if (elem != null) {
                return elem.getStartOffset();
            }
            return -1;
        }

        @Override
        public int getEndOffset() {
            return this.endOffset;
        }

        @Override
        public void next() {
            this.nextLeaf(this.pos);
            while (this.isValid()) {
                AttributeSet a;
                Element elem = this.pos.current();
                if (elem.getStartOffset() >= this.endOffset && ((a = this.pos.current().getAttributes()).isDefined(this.tag) || a.getAttribute(StyleConstants.NameAttribute) == this.tag)) {
                    this.setEndOffset();
                    break;
                }
                this.nextLeaf(this.pos);
            }
        }

        @Override
        public HTML.Tag getTag() {
            return this.tag;
        }

        @Override
        public boolean isValid() {
            return this.pos.current() != null;
        }

        void nextLeaf(ElementIterator iter) {
            Element e;
            iter.next();
            while (iter.current() != null && !(e = iter.current()).isLeaf()) {
                iter.next();
            }
        }

        void setEndOffset() {
            Element e;
            AttributeSet a1;
            AttributeSet a0 = this.getAttributes();
            this.endOffset = this.pos.current().getEndOffset();
            ElementIterator fwd = (ElementIterator)this.pos.clone();
            this.nextLeaf(fwd);
            while (fwd.current() != null && (a1 = (AttributeSet)(e = fwd.current()).getAttributes().getAttribute(this.tag)) != null && a1.equals(a0)) {
                this.endOffset = e.getEndOffset();
                this.nextLeaf(fwd);
            }
        }
    }

    public class RunElement
    extends AbstractDocument.LeafElement {
        public RunElement(Element parent, AttributeSet a, int offs0, int offs1) {
            super(parent, a, offs0, offs1);
        }

        @Override
        public String getName() {
            Object o = this.getAttribute(StyleConstants.NameAttribute);
            if (o != null) {
                return o.toString();
            }
            return super.getName();
        }

        @Override
        public AttributeSet getResolveParent() {
            return null;
        }
    }

    public class BlockElement
    extends AbstractDocument.BranchElement {
        public BlockElement(Element parent, AttributeSet a) {
            super(parent, a);
        }

        @Override
        public String getName() {
            Object o = this.getAttribute(StyleConstants.NameAttribute);
            if (o != null) {
                return o.toString();
            }
            return super.getName();
        }

        @Override
        public AttributeSet getResolveParent() {
            return null;
        }
    }

    private static class FixedLengthDocument
    extends PlainDocument {
        private int maxLength;

        public FixedLengthDocument(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
            if (str != null && str.length() + this.getLength() <= this.maxLength) {
                super.insertString(offset, str, a);
            }
        }
    }

    static class TaggedAttributeSet
    extends SimpleAttributeSet {
        TaggedAttributeSet() {
        }
    }

    public static abstract class Iterator {
        protected Iterator() {
        }

        public abstract AttributeSet getAttributes();

        public abstract int getStartOffset();

        public abstract int getEndOffset();

        public abstract void next();

        public abstract boolean isValid();

        public abstract HTML.Tag getTag();
    }
}

