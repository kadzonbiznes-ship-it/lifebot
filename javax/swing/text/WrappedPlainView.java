/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.lang.ref.SoftReference;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.PlainView;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.SegmentCache;
import javax.swing.text.StateInvariantError;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class WrappedPlainView
extends BoxView
implements TabExpander {
    FontMetrics metrics;
    Segment lineBuffer;
    boolean widthChanging;
    int tabBase;
    float tabSize;
    boolean wordWrap;
    int sel0;
    int sel1;
    Color unselected;
    Color selected;
    private final boolean drawLineOverridden = PlainView.getFPMethodOverridden(this.getClass(), "drawLine", PlainView.FPMethodArgs.IIGNN);
    private final boolean drawSelectedTextOverridden = PlainView.getFPMethodOverridden(this.getClass(), "drawSelectedText", PlainView.FPMethodArgs.GNNII);
    private final boolean drawUnselectedTextOverridden = PlainView.getFPMethodOverridden(this.getClass(), "drawUnselectedText", PlainView.FPMethodArgs.GNNII);
    private final boolean useFloatingPointAPI = this.drawUnselectedTextOverridden || this.drawSelectedTextOverridden;

    public WrappedPlainView(Element elem) {
        this(elem, false);
    }

    public WrappedPlainView(Element elem, boolean wordWrap) {
        super(elem, 1);
        this.wordWrap = wordWrap;
    }

    protected int getTabSize() {
        Integer i = (Integer)this.getDocument().getProperty("tabSize");
        int size = i != null ? i : 8;
        return size;
    }

    @Deprecated(since="9")
    protected void drawLine(int p0, int p1, Graphics g, int x, int y) {
        this.drawLineImpl(p0, p1, g, x, y, false);
    }

    private void drawLineImpl(int p0, int p1, Graphics g, float x, float y, boolean useFPAPI) {
        Element lineMap = this.getElement();
        Element line = lineMap.getElement(lineMap.getElementIndex(p0));
        try {
            if (line.isLeaf()) {
                this.drawText(line, p0, p1, g, x, y);
            } else {
                int lastIdx = line.getElementIndex(p1);
                for (int idx = line.getElementIndex(p0); idx <= lastIdx; ++idx) {
                    Element elem = line.getElement(idx);
                    int start = Math.max(elem.getStartOffset(), p0);
                    int end = Math.min(elem.getEndOffset(), p1);
                    x = this.drawText(elem, start, end, g, x, y);
                }
            }
        }
        catch (BadLocationException e) {
            throw new StateInvariantError("Can't render: " + p0 + "," + p1);
        }
    }

    protected void drawLine(int p0, int p1, Graphics2D g, float x, float y) {
        this.drawLineImpl(p0, p1, g, x, y, true);
    }

    private float drawText(Element elem, int p0, int p1, Graphics g, float x, float y) throws BadLocationException {
        p1 = Math.min(this.getDocument().getLength(), p1);
        AttributeSet attr = elem.getAttributes();
        if (Utilities.isComposedTextAttributeDefined(attr)) {
            g.setColor(this.unselected);
            x = Utilities.drawComposedText((View)this, attr, g, x, y, p0 - elem.getStartOffset(), p1 - elem.getStartOffset());
        } else if (this.sel0 == this.sel1 || this.selected == this.unselected) {
            x = this.callDrawUnselectedText(g, x, y, p0, p1);
        } else if (p0 >= this.sel0 && p0 <= this.sel1 && p1 >= this.sel0 && p1 <= this.sel1) {
            x = this.callDrawSelectedText(g, x, y, p0, p1);
        } else if (this.sel0 >= p0 && this.sel0 <= p1) {
            if (this.sel1 >= p0 && this.sel1 <= p1) {
                x = this.callDrawUnselectedText(g, x, y, p0, this.sel0);
                x = this.callDrawSelectedText(g, x, y, this.sel0, this.sel1);
                x = this.callDrawUnselectedText(g, x, y, this.sel1, p1);
            } else {
                x = this.callDrawUnselectedText(g, x, y, p0, this.sel0);
                x = this.callDrawSelectedText(g, x, y, this.sel0, p1);
            }
        } else if (this.sel1 >= p0 && this.sel1 <= p1) {
            x = this.callDrawSelectedText(g, x, y, p0, this.sel1);
            x = this.callDrawUnselectedText(g, x, y, this.sel1, p1);
        } else {
            x = this.callDrawUnselectedText(g, x, y, p0, p1);
        }
        return x;
    }

    @Deprecated(since="9")
    protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
        return (int)this.drawUnselectedTextImpl(g, x, y, p0, p1, false);
    }

    private float callDrawUnselectedText(Graphics g, float x, float y, int p0, int p1) throws BadLocationException {
        return this.drawUnselectedTextOverridden && g instanceof Graphics2D ? this.drawUnselectedText((Graphics2D)g, x, y, p0, p1) : (float)this.drawUnselectedText(g, (int)x, (int)y, p0, p1);
    }

    private float drawUnselectedTextImpl(Graphics g, float x, float y, int p0, int p1, boolean useFPAPI) throws BadLocationException {
        g.setColor(this.unselected);
        Document doc = this.getDocument();
        Segment segment = SegmentCache.getSharedSegment();
        doc.getText(p0, p1 - p0, segment);
        float ret = Utilities.drawTabbedText(this, segment, x, y, g, this, p0, null, useFPAPI);
        SegmentCache.releaseSharedSegment(segment);
        return ret;
    }

    protected float drawUnselectedText(Graphics2D g, float x, float y, int p0, int p1) throws BadLocationException {
        return this.drawUnselectedTextImpl(g, x, y, p0, p1, true);
    }

    @Deprecated(since="9")
    protected int drawSelectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
        return (int)this.drawSelectedTextImpl(g, x, y, p0, p1, false);
    }

    private float callDrawSelectedText(Graphics g, float x, float y, int p0, int p1) throws BadLocationException {
        return this.drawSelectedTextOverridden && g instanceof Graphics2D ? this.drawSelectedText((Graphics2D)g, x, y, p0, p1) : (float)this.drawSelectedText(g, (int)x, (int)y, p0, p1);
    }

    private float drawSelectedTextImpl(Graphics g, float x, float y, int p0, int p1, boolean useFPAPI) throws BadLocationException {
        g.setColor(this.selected);
        Document doc = this.getDocument();
        Segment segment = SegmentCache.getSharedSegment();
        doc.getText(p0, p1 - p0, segment);
        float ret = Utilities.drawTabbedText(this, segment, x, y, g, this, p0, null, useFPAPI);
        SegmentCache.releaseSharedSegment(segment);
        return ret;
    }

    protected float drawSelectedText(Graphics2D g, float x, float y, int p0, int p1) throws BadLocationException {
        return this.drawSelectedTextImpl(g, x, y, p0, p1, true);
    }

    protected final Segment getLineBuffer() {
        if (this.lineBuffer == null) {
            this.lineBuffer = new Segment();
        }
        return this.lineBuffer;
    }

    protected int calculateBreakPosition(int p0, int p1) {
        Segment segment = SegmentCache.getSharedSegment();
        this.loadText(segment, p0, p1);
        int currentWidth = this.getWidth();
        int p = this.wordWrap ? p0 + Utilities.getBreakLocation(segment, this.metrics, (float)this.tabBase, (float)(this.tabBase + currentWidth), (TabExpander)this, p0) : p0 + Utilities.getTabbedTextOffset(segment, this.metrics, (float)this.tabBase, (float)(this.tabBase + currentWidth), (TabExpander)this, p0, false);
        SegmentCache.releaseSharedSegment(segment);
        return p;
    }

    @Override
    protected void loadChildren(ViewFactory f) {
        Element e = this.getElement();
        int n = e.getElementCount();
        if (n > 0) {
            View[] added = new View[n];
            for (int i = 0; i < n; ++i) {
                added[i] = new WrappedLine(e.getElement(i));
            }
            this.replace(0, 0, added);
        }
    }

    void updateChildren(DocumentEvent e, Shape a) {
        Element elem = this.getElement();
        DocumentEvent.ElementChange ec = e.getChange(elem);
        if (ec != null) {
            Element[] removedElems = ec.getChildrenRemoved();
            Element[] addedElems = ec.getChildrenAdded();
            View[] added = new View[addedElems.length];
            for (int i = 0; i < addedElems.length; ++i) {
                added[i] = new WrappedLine(addedElems[i]);
            }
            this.replace(ec.getIndex(), removedElems.length, added);
            if (a != null) {
                this.preferenceChanged(null, true, true);
                this.getContainer().repaint();
            }
        }
        this.updateMetrics();
    }

    final void loadText(Segment segment, int p0, int p1) {
        try {
            Document doc = this.getDocument();
            doc.getText(p0, p1 - p0, segment);
        }
        catch (BadLocationException bl) {
            throw new StateInvariantError("Can't get line text");
        }
    }

    final void updateMetrics() {
        Container host = this.getContainer();
        Font f = host.getFont();
        this.metrics = host.getFontMetrics(f);
        if (this.useFloatingPointAPI) {
            FontRenderContext frc = this.metrics.getFontRenderContext();
            float tabWidth = (float)f.getStringBounds("m", frc).getWidth();
            this.tabSize = (float)this.getTabSize() * tabWidth;
        } else {
            this.tabSize = this.getTabSize() * this.metrics.charWidth('m');
        }
    }

    @Override
    public float nextTabStop(float x, int tabOffset) {
        if (this.tabSize == 0.0f) {
            return x;
        }
        int ntabs = (int)((x - (float)this.tabBase) / this.tabSize);
        return (float)this.tabBase + (float)(ntabs + 1) * this.tabSize;
    }

    @Override
    public void paint(Graphics g, Shape a) {
        Rectangle alloc = (Rectangle)a;
        this.tabBase = alloc.x;
        JTextComponent host = (JTextComponent)this.getContainer();
        this.sel0 = host.getSelectionStart();
        this.sel1 = host.getSelectionEnd();
        this.unselected = host.isEnabled() ? host.getForeground() : host.getDisabledTextColor();
        Caret c = host.getCaret();
        this.selected = c.isSelectionVisible() && host.getHighlighter() != null ? host.getSelectedTextColor() : this.unselected;
        g.setFont(host.getFont());
        super.paint(g, a);
    }

    @Override
    public void setSize(float width, float height) {
        this.updateMetrics();
        if ((int)width != this.getWidth()) {
            this.preferenceChanged(null, true, true);
            this.widthChanging = true;
        }
        super.setSize(width, height);
        this.widthChanging = false;
    }

    @Override
    public float getPreferredSpan(int axis) {
        this.updateMetrics();
        return super.getPreferredSpan(axis);
    }

    @Override
    public float getMinimumSpan(int axis) {
        this.updateMetrics();
        return super.getMinimumSpan(axis);
    }

    @Override
    public float getMaximumSpan(int axis) {
        this.updateMetrics();
        return super.getMaximumSpan(axis);
    }

    @Override
    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        this.updateChildren(e, a);
        Rectangle alloc = a != null && this.isAllocationValid() ? this.getInsideAllocation(a) : null;
        int pos = e.getOffset();
        View v = this.getViewAtPosition(pos, alloc);
        if (v != null) {
            v.insertUpdate(e, alloc, f);
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        this.updateChildren(e, a);
        Rectangle alloc = a != null && this.isAllocationValid() ? this.getInsideAllocation(a) : null;
        int pos = e.getOffset();
        View v = this.getViewAtPosition(pos, alloc);
        if (v != null) {
            v.removeUpdate(e, alloc, f);
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        this.updateChildren(e, a);
    }

    class WrappedLine
    extends View {
        int lineCount;
        SoftReference<int[]> lineCache;

        WrappedLine(Element elem) {
            super(elem);
            this.lineCache = null;
            this.lineCount = -1;
        }

        @Override
        public float getPreferredSpan(int axis) {
            switch (axis) {
                case 0: {
                    float width = WrappedPlainView.this.getWidth();
                    if (width == 2.1474836E9f) {
                        return 100.0f;
                    }
                    return width;
                }
                case 1: {
                    if (this.lineCount < 0 || WrappedPlainView.this.widthChanging) {
                        this.breakLines(this.getStartOffset());
                    }
                    return this.lineCount * WrappedPlainView.this.metrics.getHeight();
                }
            }
            throw new IllegalArgumentException("Invalid axis: " + axis);
        }

        @Override
        public void paint(Graphics g, Shape a) {
            Rectangle alloc = (Rectangle)a;
            int y = alloc.y + WrappedPlainView.this.metrics.getAscent();
            int x = alloc.x;
            JTextComponent host = (JTextComponent)this.getContainer();
            Highlighter h = host.getHighlighter();
            LayeredHighlighter dh = h instanceof LayeredHighlighter ? (LayeredHighlighter)h : null;
            int start = this.getStartOffset();
            int end = this.getEndOffset();
            int p0 = start;
            int[] lineEnds = this.getLineEnds();
            boolean useDrawLineFP = WrappedPlainView.this.drawLineOverridden && g instanceof Graphics2D;
            for (int i = 0; i < this.lineCount; ++i) {
                int p1;
                int n = p1 = lineEnds == null ? end : start + lineEnds[i];
                if (dh != null) {
                    int hOffset = p1 == end ? p1 - 1 : p1;
                    dh.paintLayeredHighlights(g, p0, hOffset, a, host, this);
                }
                if (useDrawLineFP) {
                    WrappedPlainView.this.drawLine(p0, p1, (Graphics2D)g, (float)x, (float)y);
                } else {
                    WrappedPlainView.this.drawLine(p0, p1, g, x, y);
                }
                p0 = p1;
                y += WrappedPlainView.this.metrics.getHeight();
            }
        }

        @Override
        public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
            Rectangle alloc = a.getBounds();
            alloc.height = WrappedPlainView.this.metrics.getHeight();
            alloc.width = 1;
            int p0 = this.getStartOffset();
            if (pos < p0 || pos > this.getEndOffset()) {
                throw new BadLocationException("Position out of range", pos);
            }
            int testP = b == Position.Bias.Forward ? pos : Math.max(p0, pos - 1);
            int line = 0;
            int[] lineEnds = this.getLineEnds();
            if (lineEnds != null) {
                line = this.findLine(testP - p0);
                if (line > 0) {
                    p0 += lineEnds[line - 1];
                }
                alloc.y += alloc.height * line;
            }
            if (pos > p0) {
                Segment segment = SegmentCache.getSharedSegment();
                WrappedPlainView.this.loadText(segment, p0, pos);
                float x = alloc.x;
                x += Utilities.getTabbedTextWidth(segment, WrappedPlainView.this.metrics, x, (TabExpander)WrappedPlainView.this, p0);
                SegmentCache.releaseSharedSegment(segment);
                return new Rectangle2D.Float(x, alloc.y, alloc.width, alloc.height);
            }
            return alloc;
        }

        @Override
        public int viewToModel(float fx, float fy, Shape a, Position.Bias[] bias) {
            int p1;
            int line;
            bias[0] = Position.Bias.Forward;
            Rectangle alloc = (Rectangle)a;
            int x = (int)fx;
            int y = (int)fy;
            if (y < alloc.y) {
                return this.getStartOffset();
            }
            if (y > alloc.y + alloc.height) {
                return this.getEndOffset() - 1;
            }
            alloc.height = WrappedPlainView.this.metrics.getHeight();
            int n = line = alloc.height > 0 ? (y - alloc.y) / alloc.height : this.lineCount - 1;
            if (line >= this.lineCount) {
                return this.getEndOffset() - 1;
            }
            int p0 = this.getStartOffset();
            if (this.lineCount == 1) {
                p1 = this.getEndOffset();
            } else {
                int[] lineEnds = this.getLineEnds();
                p1 = p0 + lineEnds[line];
                if (line > 0) {
                    p0 += lineEnds[line - 1];
                }
            }
            if (x < alloc.x) {
                return p0;
            }
            if (x > alloc.x + alloc.width) {
                return p1 - 1;
            }
            Segment segment = SegmentCache.getSharedSegment();
            WrappedPlainView.this.loadText(segment, p0, p1);
            int n2 = Utilities.getTabbedTextOffset(segment, WrappedPlainView.this.metrics, (float)alloc.x, (float)x, (TabExpander)WrappedPlainView.this, p0, false);
            SegmentCache.releaseSharedSegment(segment);
            return Math.min(p0 + n2, p1 - 1);
        }

        @Override
        public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            this.update(e, a);
        }

        @Override
        public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            this.update(e, a);
        }

        private void update(DocumentEvent ev, Shape a) {
            int oldCount = this.lineCount;
            this.breakLines(ev.getOffset());
            if (oldCount != this.lineCount) {
                WrappedPlainView.this.preferenceChanged(this, false, true);
                this.getContainer().repaint();
            } else if (a != null) {
                Container c = this.getContainer();
                Rectangle alloc = (Rectangle)a;
                c.repaint(alloc.x, alloc.y, alloc.width, alloc.height);
            }
        }

        final int[] getLineEnds() {
            if (this.lineCache == null) {
                return null;
            }
            int[] lineEnds = this.lineCache.get();
            if (lineEnds == null) {
                return this.breakLines(this.getStartOffset());
            }
            return lineEnds;
        }

        final int[] breakLines(int startPos) {
            int maxCapacity;
            int[] lineEnds;
            int[] oldLineEnds = lineEnds = this.lineCache == null ? null : this.lineCache.get();
            int start = this.getStartOffset();
            int lineIndex = 0;
            if (lineEnds != null && (lineIndex = this.findLine(startPos - start)) > 0) {
                --lineIndex;
            }
            int p0 = lineIndex == 0 ? start : start + lineEnds[lineIndex - 1];
            int p1 = this.getEndOffset();
            while (p0 < p1) {
                int p = WrappedPlainView.this.calculateBreakPosition(p0, p1);
                int n = p0 = p == p0 ? ++p : p;
                if (lineIndex == 0 && p0 >= p1) {
                    this.lineCache = null;
                    lineEnds = null;
                    lineIndex = 1;
                    break;
                }
                if (lineEnds == null || lineIndex >= lineEnds.length) {
                    double growFactor = (double)(p1 - start) / (double)(p0 - start);
                    int newSize = (int)Math.ceil((double)(lineIndex + 1) * growFactor);
                    newSize = Math.max(newSize, lineIndex + 2);
                    int[] tmp = new int[newSize];
                    if (lineEnds != null) {
                        System.arraycopy(lineEnds, 0, tmp, 0, lineIndex);
                    }
                    lineEnds = tmp;
                }
                lineEnds[lineIndex++] = p0 - start;
            }
            this.lineCount = lineIndex;
            if (this.lineCount > 1 && lineEnds.length > (maxCapacity = this.lineCount + this.lineCount / 3)) {
                int[] tmp = new int[maxCapacity];
                System.arraycopy(lineEnds, 0, tmp, 0, this.lineCount);
                lineEnds = tmp;
            }
            if (lineEnds != null && lineEnds != oldLineEnds) {
                this.lineCache = new SoftReference<int[]>(lineEnds);
            }
            return lineEnds;
        }

        private int findLine(int offset) {
            int[] lineEnds = this.lineCache.get();
            if (offset < lineEnds[0]) {
                return 0;
            }
            if (offset > lineEnds[this.lineCount - 1]) {
                return this.lineCount;
            }
            return this.findLine(lineEnds, offset, 0, this.lineCount - 1);
        }

        private int findLine(int[] array, int offset, int min, int max) {
            if (max - min <= 1) {
                return max;
            }
            int mid = (max + min) / 2;
            return offset < array[mid] ? this.findLine(array, offset, min, mid) : this.findLine(array, offset, mid, max);
        }
    }
}

