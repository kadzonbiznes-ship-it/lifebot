/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.text.BreakIterator;
import java.util.BitSet;
import java.util.Locale;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.GlyphPainter1;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.SegmentCache;
import javax.swing.text.StateInvariantError;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabExpander;
import javax.swing.text.TabableView;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.WhitespaceBasedBreakIterator;
import sun.swing.SwingUtilities2;

public class GlyphView
extends View
implements TabableView,
Cloneable {
    private byte[] selections = null;
    int offset = 0;
    int length = 0;
    boolean impliedCR;
    boolean skipWidth;
    TabExpander expander;
    private float minimumSpan = -1.0f;
    private int[] breakSpots = null;
    int x;
    GlyphPainter painter;
    static GlyphPainter defaultPainter;
    private JustificationInfo justificationInfo = null;

    public GlyphView(Element elem) {
        super(elem);
        Element parent = elem.getParentElement();
        AttributeSet attr = elem.getAttributes();
        this.impliedCR = attr != null && attr.getAttribute("CR") != null && parent != null && parent.getElementCount() > 1;
        this.skipWidth = elem.getName().equals("br");
    }

    protected final Object clone() {
        Object o;
        try {
            o = super.clone();
        }
        catch (CloneNotSupportedException cnse) {
            o = null;
        }
        return o;
    }

    public GlyphPainter getGlyphPainter() {
        return this.painter;
    }

    public void setGlyphPainter(GlyphPainter p) {
        this.painter = p;
    }

    public Segment getText(int p0, int p1) {
        Segment text = SegmentCache.getSharedSegment();
        try {
            Document doc = this.getDocument();
            doc.getText(p0, p1 - p0, text);
        }
        catch (BadLocationException bl) {
            throw new StateInvariantError("GlyphView: Stale view: " + String.valueOf(bl));
        }
        return text;
    }

    public Color getBackground() {
        AttributeSet attr;
        Document doc = this.getDocument();
        if (doc instanceof StyledDocument && (attr = this.getAttributes()).isDefined(StyleConstants.Background)) {
            return ((StyledDocument)doc).getBackground(attr);
        }
        return null;
    }

    public Color getForeground() {
        Document doc = this.getDocument();
        if (doc instanceof StyledDocument) {
            AttributeSet attr = this.getAttributes();
            return ((StyledDocument)doc).getForeground(attr);
        }
        Container c = this.getContainer();
        if (c != null) {
            return c.getForeground();
        }
        return null;
    }

    public Font getFont() {
        Document doc = this.getDocument();
        if (doc instanceof StyledDocument) {
            AttributeSet attr = this.getAttributes();
            return ((StyledDocument)doc).getFont(attr);
        }
        Container c = this.getContainer();
        if (c != null) {
            return c.getFont();
        }
        return null;
    }

    public boolean isUnderline() {
        AttributeSet attr = this.getAttributes();
        return StyleConstants.isUnderline(attr);
    }

    public boolean isStrikeThrough() {
        AttributeSet attr = this.getAttributes();
        return StyleConstants.isStrikeThrough(attr);
    }

    public boolean isSubscript() {
        AttributeSet attr = this.getAttributes();
        return StyleConstants.isSubscript(attr);
    }

    public boolean isSuperscript() {
        AttributeSet attr = this.getAttributes();
        return StyleConstants.isSuperscript(attr);
    }

    public TabExpander getTabExpander() {
        return this.expander;
    }

    protected void checkPainter() {
        if (this.painter == null) {
            if (defaultPainter == null) {
                defaultPainter = new GlyphPainter1();
            }
            this.setGlyphPainter(defaultPainter.getPainter(this, this.getStartOffset(), this.getEndOffset()));
        }
    }

    @Override
    public float getTabbedSpan(float x, TabExpander e) {
        this.checkPainter();
        TabExpander old = this.expander;
        this.expander = e;
        if (this.expander != old) {
            this.preferenceChanged(null, true, false);
        }
        this.x = (int)x;
        int p0 = this.getStartOffset();
        int p1 = this.getEndOffset();
        float width = this.painter.getSpan(this, p0, p1, this.expander, x);
        return width;
    }

    @Override
    public float getPartialSpan(int p0, int p1) {
        this.checkPainter();
        float width = this.painter.getSpan(this, p0, p1, this.expander, this.x);
        return width;
    }

    @Override
    public int getStartOffset() {
        Element e = this.getElement();
        return this.length > 0 ? e.getStartOffset() + this.offset : e.getStartOffset();
    }

    @Override
    public int getEndOffset() {
        Element e = this.getElement();
        return this.length > 0 ? e.getStartOffset() + this.offset + this.length : e.getEndOffset();
    }

    private void initSelections(int p0, int p1) {
        int viewPosCount = p1 - p0 + 1;
        if (this.selections == null || viewPosCount > this.selections.length) {
            this.selections = new byte[viewPosCount];
            return;
        }
        int i = 0;
        while (i < viewPosCount) {
            this.selections[i++] = 0;
        }
    }

    @Override
    public void paint(Graphics g, Shape a) {
        JTextComponent tc;
        Highlighter h;
        this.checkPainter();
        boolean paintedText = false;
        Container c = this.getContainer();
        int p0 = this.getStartOffset();
        int p1 = this.getEndOffset();
        Rectangle alloc = a instanceof Rectangle ? (Rectangle)a : a.getBounds();
        Color bg = this.getBackground();
        Color fg = this.getForeground();
        if (c != null && !c.isEnabled()) {
            Color color = fg = c instanceof JTextComponent ? ((JTextComponent)c).getDisabledTextColor() : UIManager.getColor("textInactiveText");
        }
        if (bg != null) {
            g.setColor(bg);
            g.fillRect(alloc.x, alloc.y, alloc.width, alloc.height);
        }
        if (c instanceof JTextComponent && (h = (tc = (JTextComponent)c).getHighlighter()) instanceof LayeredHighlighter) {
            ((LayeredHighlighter)h).paintLayeredHighlights(g, p0, p1, a, tc, this);
        }
        if (Utilities.isComposedTextElement(this.getElement())) {
            Utilities.paintComposedText(g, a.getBounds(), this);
            paintedText = true;
        } else if (c instanceof JTextComponent) {
            Highlighter.Highlight[] h2;
            tc = (JTextComponent)c;
            Color selFG = tc.getSelectedTextColor();
            if (tc.getHighlighter() != null && selFG != null && !selFG.equals(fg) && (h2 = tc.getHighlighter().getHighlights()).length != 0) {
                boolean initialized = false;
                int viewSelectionCount = 0;
                for (int i = 0; i < h2.length; ++i) {
                    Highlighter.Highlight highlight = h2[i];
                    int hStart = highlight.getStartOffset();
                    int hEnd = highlight.getEndOffset();
                    if (hStart > p1 || hEnd < p0 || !SwingUtilities2.useSelectedTextColor(highlight, tc)) continue;
                    if (hStart <= p0 && hEnd >= p1) {
                        this.paintTextUsingColor(g, a, selFG, p0, p1);
                        paintedText = true;
                        break;
                    }
                    if (!initialized) {
                        this.initSelections(p0, p1);
                        initialized = true;
                    }
                    hStart = Math.max(p0, hStart);
                    hEnd = Math.min(p1, hEnd);
                    this.paintTextUsingColor(g, a, selFG, hStart, hEnd);
                    int n = hStart - p0;
                    this.selections[n] = (byte)(this.selections[n] + 1);
                    int n2 = hEnd - p0;
                    this.selections[n2] = (byte)(this.selections[n2] - 1);
                    ++viewSelectionCount;
                }
                if (!paintedText && viewSelectionCount > 0) {
                    int curPos = -1;
                    int startPos = 0;
                    int viewLen = p1 - p0;
                    while (curPos++ < viewLen) {
                        while (curPos < viewLen && this.selections[curPos] == 0) {
                            ++curPos;
                        }
                        if (startPos != curPos) {
                            this.paintTextUsingColor(g, a, fg, p0 + startPos, p0 + curPos);
                        }
                        int checkSum = 0;
                        while (curPos < viewLen && (checkSum += this.selections[curPos]) != 0) {
                            ++curPos;
                        }
                        startPos = curPos;
                    }
                    paintedText = true;
                }
            }
        }
        if (!paintedText) {
            this.paintTextUsingColor(g, a, fg, p0, p1);
        }
    }

    final void paintTextUsingColor(Graphics g, Shape a, Color c, int p0, int p1) {
        g.setColor(c);
        this.painter.paint(this, g, a, p0, p1);
        boolean underline = this.isUnderline();
        boolean strike = this.isStrikeThrough();
        if (underline || strike) {
            int yTmp;
            Rectangle alloc = a instanceof Rectangle ? (Rectangle)a : a.getBounds();
            View parent = this.getParent();
            if (parent != null && parent.getEndOffset() == p1) {
                Segment s = this.getText(p0, p1);
                while (Character.isWhitespace(s.last())) {
                    --p1;
                    --s.count;
                }
                SegmentCache.releaseSharedSegment(s);
            }
            int x0 = alloc.x;
            int p = this.getStartOffset();
            if (p != p0) {
                x0 += (int)this.painter.getSpan(this, p, p0, this.getTabExpander(), x0);
            }
            int x1 = x0 + (int)this.painter.getSpan(this, p0, p1, this.getTabExpander(), x0);
            int y = alloc.y + (int)(this.painter.getHeight(this) - this.painter.getDescent(this));
            if (underline) {
                yTmp = y + 1;
                g.drawLine(x0, yTmp, x1, yTmp);
            }
            if (strike) {
                yTmp = y - (int)(this.painter.getAscent(this) * 0.3f);
                g.drawLine(x0, yTmp, x1, yTmp);
            }
        }
    }

    @Override
    public float getMinimumSpan(int axis) {
        switch (axis) {
            case 0: {
                if (this.minimumSpan < 0.0f) {
                    this.minimumSpan = 0.0f;
                    int p0 = this.getStartOffset();
                    int p1 = this.getEndOffset();
                    while (p1 > p0) {
                        int breakSpot = this.getBreakSpot(p0, p1);
                        if (breakSpot == -1) {
                            breakSpot = p0;
                        }
                        this.minimumSpan = Math.max(this.minimumSpan, this.getPartialSpan(breakSpot, p1));
                        p1 = breakSpot - 1;
                    }
                }
                return this.minimumSpan;
            }
            case 1: {
                return super.getMinimumSpan(axis);
            }
        }
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }

    @Override
    public float getPreferredSpan(int axis) {
        if (this.impliedCR) {
            return 0.0f;
        }
        this.checkPainter();
        int p0 = this.getStartOffset();
        int p1 = this.getEndOffset();
        switch (axis) {
            case 0: {
                if (this.skipWidth) {
                    return 0.0f;
                }
                return this.painter.getSpan(this, p0, p1, this.expander, this.x);
            }
            case 1: {
                float h = this.painter.getHeight(this);
                if (this.isSuperscript()) {
                    h += h / 3.0f;
                }
                return h;
            }
        }
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }

    @Override
    public float getAlignment(int axis) {
        this.checkPainter();
        if (axis == 1) {
            boolean sup = this.isSuperscript();
            boolean sub = this.isSubscript();
            float h = this.painter.getHeight(this);
            float d = this.painter.getDescent(this);
            float a = this.painter.getAscent(this);
            float align = sup ? 1.0f : (sub ? (h > 0.0f ? (h - (d + a / 2.0f)) / h : 0.0f) : (h > 0.0f ? (h - d) / h : 0.0f));
            return align;
        }
        return super.getAlignment(axis);
    }

    @Override
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        this.checkPainter();
        return this.painter.modelToView(this, pos, b, a);
    }

    @Override
    public int viewToModel(float x, float y, Shape a, Position.Bias[] biasReturn) {
        this.checkPainter();
        return this.painter.viewToModel(this, x, y, a, biasReturn);
    }

    @Override
    public int getBreakWeight(int axis, float pos, float len) {
        if (axis == 0) {
            this.checkPainter();
            int p0 = this.getStartOffset();
            int p1 = this.painter.getBoundedPosition(this, p0, pos, len);
            return p1 == p0 ? 0 : (this.getBreakSpot(p0, p1) != -1 ? 2000 : 1000);
        }
        return super.getBreakWeight(axis, pos, len);
    }

    @Override
    public View breakView(int axis, int p0, float pos, float len) {
        if (axis == 0) {
            this.checkPainter();
            int p1 = this.painter.getBoundedPosition(this, p0, pos, len);
            int breakSpot = this.getBreakSpot(p0, p1);
            if (breakSpot != -1) {
                p1 = breakSpot;
            }
            if (p0 == this.getStartOffset() && p1 == this.getEndOffset()) {
                return this;
            }
            GlyphView v = (GlyphView)this.createFragment(p0, p1);
            v.x = (int)pos;
            return v;
        }
        return this;
    }

    private int getBreakSpot(int p0, int p1) {
        if (this.breakSpots == null) {
            int start = this.getStartOffset();
            int end = this.getEndOffset();
            int[] bs = new int[end + 1 - start];
            int ix = 0;
            Element parent = this.getElement().getParentElement();
            int pstart = parent == null ? start : parent.getStartOffset();
            int pend = parent == null ? end : parent.getEndOffset();
            Segment s = this.getText(pstart, pend);
            s.first();
            BreakIterator breaker = this.getBreaker();
            breaker.setText(s);
            int startFrom = end + (pend > end ? 1 : 0);
            while ((startFrom = breaker.preceding(s.offset + (startFrom - pstart)) + (pstart - s.offset)) > start) {
                bs[ix++] = startFrom;
            }
            SegmentCache.releaseSharedSegment(s);
            this.breakSpots = new int[ix];
            System.arraycopy(bs, 0, this.breakSpots, 0, ix);
        }
        int breakSpot = -1;
        for (int i = 0; i < this.breakSpots.length; ++i) {
            int bsp = this.breakSpots[i];
            if (bsp > p1) continue;
            if (bsp <= p0) break;
            breakSpot = bsp;
            break;
        }
        return breakSpot;
    }

    private BreakIterator getBreaker() {
        Document doc = this.getDocument();
        if (doc != null && Boolean.TRUE.equals(doc.getProperty(AbstractDocument.MultiByteProperty))) {
            Container c = this.getContainer();
            Locale locale = c == null ? Locale.getDefault() : c.getLocale();
            return BreakIterator.getLineInstance(locale);
        }
        return new WhitespaceBasedBreakIterator();
    }

    @Override
    public View createFragment(int p0, int p1) {
        this.checkPainter();
        Element elem = this.getElement();
        GlyphView v = (GlyphView)this.clone();
        v.offset = p0 - elem.getStartOffset();
        v.length = p1 - p0;
        v.painter = this.painter.getPainter(v, p0, p1);
        v.justificationInfo = null;
        return v;
    }

    @Override
    public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a, int direction, Position.Bias[] biasRet) throws BadLocationException {
        if (pos < -1 || pos > this.getDocument().getLength()) {
            throw new BadLocationException("invalid position", pos);
        }
        return this.painter.getNextVisualPositionFrom(this, pos, b, a, direction, biasRet);
    }

    @Override
    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        this.justificationInfo = null;
        this.breakSpots = null;
        this.minimumSpan = -1.0f;
        this.syncCR();
        this.preferenceChanged(null, true, false);
    }

    @Override
    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        this.justificationInfo = null;
        this.breakSpots = null;
        this.minimumSpan = -1.0f;
        this.syncCR();
        this.preferenceChanged(null, true, false);
    }

    @Override
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        this.minimumSpan = -1.0f;
        this.syncCR();
        this.preferenceChanged(null, true, true);
    }

    private void syncCR() {
        if (this.impliedCR) {
            Element parent = this.getElement().getParentElement();
            this.impliedCR = parent != null && parent.getElementCount() > 1;
        }
    }

    @Override
    void updateAfterChange() {
        this.breakSpots = null;
    }

    JustificationInfo getJustificationInfo(int rowStartOffset) {
        if (this.justificationInfo != null) {
            return this.justificationInfo;
        }
        boolean TRAILING = false;
        boolean CONTENT = true;
        int SPACES = 2;
        int startOffset = this.getStartOffset();
        int endOffset = this.getEndOffset();
        Segment segment = this.getText(startOffset, endOffset);
        int txtOffset = segment.offset;
        int txtEnd = segment.offset + segment.count - 1;
        int startContentPosition = txtEnd + 1;
        int endContentPosition = txtOffset - 1;
        int lastTabPosition = txtOffset - 1;
        int trailingSpaces = 0;
        int contentSpaces = 0;
        int leadingSpaces = 0;
        boolean hasTab = false;
        BitSet spaceMap = new BitSet(endOffset - startOffset + 1);
        int state = 0;
        for (int i = txtEnd; i >= txtOffset; --i) {
            if (' ' == segment.array[i]) {
                spaceMap.set(i - txtOffset);
                if (state == 0) {
                    ++trailingSpaces;
                    continue;
                }
                if (state == 1) {
                    state = 2;
                    leadingSpaces = 1;
                    continue;
                }
                if (state != 2) continue;
                ++leadingSpaces;
                continue;
            }
            if ('\t' == segment.array[i]) {
                hasTab = true;
                break;
            }
            if (state == 0) {
                if ('\n' != segment.array[i] && '\r' != segment.array[i]) {
                    state = 1;
                    endContentPosition = i;
                }
            } else if (state != 1 && state == 2) {
                contentSpaces += leadingSpaces;
                leadingSpaces = 0;
            }
            startContentPosition = i;
        }
        SegmentCache.releaseSharedSegment(segment);
        int startJustifiableContent = -1;
        if (startContentPosition < txtEnd) {
            startJustifiableContent = startContentPosition - txtOffset;
        }
        int endJustifiableContent = -1;
        if (endContentPosition > txtOffset) {
            endJustifiableContent = endContentPosition - txtOffset;
        }
        this.justificationInfo = new JustificationInfo(startJustifiableContent, endJustifiableContent, leadingSpaces, contentSpaces, trailingSpaces, hasTab, spaceMap);
        return this.justificationInfo;
    }

    static class JustificationInfo {
        final int start;
        final int end;
        final int leadingSpaces;
        final int contentSpaces;
        final int trailingSpaces;
        final boolean hasTab;
        final BitSet spaceMap;

        JustificationInfo(int start, int end, int leadingSpaces, int contentSpaces, int trailingSpaces, boolean hasTab, BitSet spaceMap) {
            this.start = start;
            this.end = end;
            this.leadingSpaces = leadingSpaces;
            this.contentSpaces = contentSpaces;
            this.trailingSpaces = trailingSpaces;
            this.hasTab = hasTab;
            this.spaceMap = spaceMap;
        }
    }

    public static abstract class GlyphPainter {
        protected GlyphPainter() {
        }

        public abstract float getSpan(GlyphView var1, int var2, int var3, TabExpander var4, float var5);

        public abstract float getHeight(GlyphView var1);

        public abstract float getAscent(GlyphView var1);

        public abstract float getDescent(GlyphView var1);

        public abstract void paint(GlyphView var1, Graphics var2, Shape var3, int var4, int var5);

        public abstract Shape modelToView(GlyphView var1, int var2, Position.Bias var3, Shape var4) throws BadLocationException;

        public abstract int viewToModel(GlyphView var1, float var2, float var3, Shape var4, Position.Bias[] var5);

        public abstract int getBoundedPosition(GlyphView var1, int var2, float var3, float var4);

        public GlyphPainter getPainter(GlyphView v, int p0, int p1) {
            return this;
        }

        public int getNextVisualPositionFrom(GlyphView v, int pos, Position.Bias b, Shape a, int direction, Position.Bias[] biasRet) throws BadLocationException {
            int startOffset = v.getStartOffset();
            int endOffset = v.getEndOffset();
            switch (direction) {
                case 1: 
                case 5: {
                    Point magicPoint;
                    if (pos != -1) {
                        return -1;
                    }
                    Container container = v.getContainer();
                    if (!(container instanceof JTextComponent)) break;
                    Caret c = ((JTextComponent)container).getCaret();
                    Point point = magicPoint = c != null ? c.getMagicCaretPosition() : null;
                    if (magicPoint == null) {
                        biasRet[0] = Position.Bias.Forward;
                        return startOffset;
                    }
                    int value = v.viewToModel(magicPoint.x, 0.0f, a, biasRet);
                    return value;
                }
                case 3: {
                    if (startOffset == v.getDocument().getLength()) {
                        if (pos == -1) {
                            biasRet[0] = Position.Bias.Forward;
                            return startOffset;
                        }
                        return -1;
                    }
                    if (pos == -1) {
                        biasRet[0] = Position.Bias.Forward;
                        return startOffset;
                    }
                    if (pos == endOffset) {
                        return -1;
                    }
                    if (++pos == endOffset) {
                        return -1;
                    }
                    biasRet[0] = Position.Bias.Forward;
                    return pos;
                }
                case 7: {
                    if (startOffset == v.getDocument().getLength()) {
                        if (pos == -1) {
                            biasRet[0] = Position.Bias.Forward;
                            return startOffset;
                        }
                        return -1;
                    }
                    if (pos == -1) {
                        biasRet[0] = Position.Bias.Forward;
                        return endOffset - 1;
                    }
                    if (pos == startOffset) {
                        return -1;
                    }
                    biasRet[0] = Position.Bias.Forward;
                    return pos - 1;
                }
                default: {
                    throw new IllegalArgumentException("Bad direction: " + direction);
                }
            }
            return pos;
        }
    }
}

