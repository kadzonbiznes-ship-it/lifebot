/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import javax.swing.text.BadLocationException;
import javax.swing.text.GlyphView;
import javax.swing.text.ParagraphView;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.SegmentCache;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;
import javax.swing.text.View;

class GlyphPainter1
extends GlyphView.GlyphPainter {
    FontMetrics metrics;

    GlyphPainter1() {
    }

    @Override
    public float getSpan(GlyphView v, int p0, int p1, TabExpander e, float x) {
        this.sync(v);
        Segment text = v.getText(p0, p1);
        int[] justificationData = this.getJustificationData(v);
        int width = Utilities.getTabbedTextWidth((View)v, text, this.metrics, (int)x, e, p0, justificationData);
        SegmentCache.releaseSharedSegment(text);
        return width;
    }

    @Override
    public float getHeight(GlyphView v) {
        this.sync(v);
        return this.metrics.getHeight();
    }

    @Override
    public float getAscent(GlyphView v) {
        this.sync(v);
        return this.metrics.getAscent();
    }

    @Override
    public float getDescent(GlyphView v) {
        this.sync(v);
        return this.metrics.getDescent();
    }

    @Override
    public void paint(GlyphView v, Graphics g, Shape a, int p0, int p1) {
        Segment text;
        this.sync(v);
        TabExpander expander = v.getTabExpander();
        Rectangle alloc = a instanceof Rectangle ? (Rectangle)a : a.getBounds();
        float x = alloc.x;
        int p = v.getStartOffset();
        int[] justificationData = this.getJustificationData(v);
        if (p != p0) {
            text = v.getText(p, p0);
            float width = Utilities.getTabbedTextWidth((View)v, text, this.metrics, x, expander, p, justificationData);
            x += width;
            SegmentCache.releaseSharedSegment(text);
        }
        float y = alloc.y + this.metrics.getHeight() - this.metrics.getDescent();
        text = v.getText(p0, p1);
        g.setFont(this.metrics.getFont());
        Utilities.drawTabbedText(v, text, x, y, g, expander, p0, justificationData, true);
        SegmentCache.releaseSharedSegment(text);
    }

    @Override
    public Shape modelToView(GlyphView v, int pos, Position.Bias bias, Shape a) throws BadLocationException {
        this.sync(v);
        Rectangle alloc = a instanceof Rectangle ? (Rectangle)a : a.getBounds();
        int p0 = v.getStartOffset();
        int p1 = v.getEndOffset();
        TabExpander expander = v.getTabExpander();
        if (pos == p1) {
            return new Rectangle(alloc.x + alloc.width, alloc.y, 0, this.metrics.getHeight());
        }
        if (pos >= p0 && pos <= p1) {
            Segment text = v.getText(p0, pos);
            int[] justificationData = this.getJustificationData(v);
            int width = Utilities.getTabbedTextWidth((View)v, text, this.metrics, alloc.x, expander, p0, justificationData);
            SegmentCache.releaseSharedSegment(text);
            return new Rectangle(alloc.x + width, alloc.y, 0, this.metrics.getHeight());
        }
        throw new BadLocationException("modelToView - can't convert", p1);
    }

    @Override
    public int viewToModel(GlyphView v, float x, float y, Shape a, Position.Bias[] biasReturn) {
        this.sync(v);
        Rectangle alloc = a instanceof Rectangle ? (Rectangle)a : a.getBounds();
        int p0 = v.getStartOffset();
        int p1 = v.getEndOffset();
        TabExpander expander = v.getTabExpander();
        Segment text = v.getText(p0, p1);
        int[] justificationData = this.getJustificationData(v);
        int offs = Utilities.getTabbedTextOffset((View)v, text, this.metrics, (float)alloc.x, x, expander, p0, justificationData);
        SegmentCache.releaseSharedSegment(text);
        int retValue = p0 + offs;
        if (retValue == p1) {
            --retValue;
        }
        biasReturn[0] = Position.Bias.Forward;
        return retValue;
    }

    @Override
    public int getBoundedPosition(GlyphView v, int p0, float x, float len) {
        this.sync(v);
        TabExpander expander = v.getTabExpander();
        Segment s = v.getText(p0, v.getEndOffset());
        int[] justificationData = this.getJustificationData(v);
        int index = Utilities.getTabbedTextOffset(v, s, this.metrics, x, x + len, expander, p0, false, justificationData, true);
        SegmentCache.releaseSharedSegment(s);
        int p1 = p0 + index;
        return p1;
    }

    void sync(GlyphView v) {
        Font f = v.getFont();
        FontMetrics fm = null;
        Container c = v.getContainer();
        if (c != null) {
            fm = c.getFontMetrics(f);
        }
        if (this.metrics == null || !f.equals(this.metrics.getFont()) || !this.metrics.equals(fm)) {
            this.metrics = c != null ? fm : Toolkit.getDefaultToolkit().getFontMetrics(f);
        }
    }

    private int[] getJustificationData(GlyphView v) {
        View parent = v.getParent();
        int[] ret = null;
        if (parent instanceof ParagraphView.Row) {
            ParagraphView.Row row = (ParagraphView.Row)parent;
            ret = row.justificationData;
        }
        return ret;
    }
}

