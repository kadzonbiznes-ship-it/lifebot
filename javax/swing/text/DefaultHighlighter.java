/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Vector;
import javax.swing.SwingUtilities;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.View;

public class DefaultHighlighter
extends LayeredHighlighter {
    private static final Highlighter.Highlight[] noHighlights = new Highlighter.Highlight[0];
    private Vector<HighlightInfo> highlights = new Vector();
    private JTextComponent component;
    private boolean drawsLayeredHighlights = true;
    private SafeDamager safeDamager = new SafeDamager();
    public static final LayeredHighlighter.LayerPainter DefaultPainter = new DefaultHighlightPainter(null);

    @Override
    public void paint(Graphics g) {
        int len = this.highlights.size();
        for (int i = 0; i < len; ++i) {
            HighlightInfo info = this.highlights.elementAt(i);
            if (info instanceof LayeredHighlightInfo) continue;
            Rectangle a = this.component.getBounds();
            Insets insets = this.component.getInsets();
            a.x = insets.left;
            a.y = insets.top;
            a.width -= insets.left + insets.right;
            a.height -= insets.top + insets.bottom;
            while (i < len) {
                info = this.highlights.elementAt(i);
                if (!(info instanceof LayeredHighlightInfo)) {
                    Highlighter.HighlightPainter p = info.getPainter();
                    p.paint(g, info.getStartOffset(), info.getEndOffset(), a, this.component);
                }
                ++i;
            }
        }
    }

    @Override
    public void install(JTextComponent c) {
        this.component = c;
        this.removeAllHighlights();
    }

    @Override
    public void deinstall(JTextComponent c) {
        this.component = null;
    }

    @Override
    public Object addHighlight(int p0, int p1, Highlighter.HighlightPainter p) throws BadLocationException {
        if (p0 < 0) {
            throw new BadLocationException("Invalid start offset", p0);
        }
        if (p1 < p0) {
            throw new BadLocationException("Invalid end offset", p1);
        }
        Document doc = this.component.getDocument();
        HighlightInfo i = this.getDrawsLayeredHighlights() && p instanceof LayeredHighlighter.LayerPainter ? new LayeredHighlightInfo() : new HighlightInfo();
        i.painter = p;
        i.p0 = doc.createPosition(p0);
        i.p1 = doc.createPosition(p1);
        this.highlights.addElement(i);
        this.safeDamageRange(p0, p1);
        return i;
    }

    @Override
    public void removeHighlight(Object tag) {
        if (tag instanceof LayeredHighlightInfo) {
            LayeredHighlightInfo lhi = (LayeredHighlightInfo)tag;
            if (lhi.width > 0 && lhi.height > 0) {
                this.component.repaint(lhi.x, lhi.y, lhi.width, lhi.height);
            }
        } else {
            HighlightInfo info = (HighlightInfo)tag;
            this.safeDamageRange(info.p0, info.p1);
        }
        this.highlights.removeElement(tag);
    }

    @Override
    public void removeAllHighlights() {
        int len;
        TextUI mapper = this.component.getUI();
        if (this.getDrawsLayeredHighlights()) {
            int len2 = this.highlights.size();
            if (len2 != 0) {
                int minX = 0;
                int minY = 0;
                int maxX = 0;
                int maxY = 0;
                int p0 = -1;
                int p1 = -1;
                for (int i = 0; i < len2; ++i) {
                    HighlightInfo hi = this.highlights.elementAt(i);
                    if (hi instanceof LayeredHighlightInfo) {
                        LayeredHighlightInfo info = (LayeredHighlightInfo)hi;
                        minX = Math.min(minX, info.x);
                        minY = Math.min(minY, info.y);
                        maxX = Math.max(maxX, info.x + info.width);
                        maxY = Math.max(maxY, info.y + info.height);
                        continue;
                    }
                    if (p0 == -1) {
                        p0 = hi.p0.getOffset();
                        p1 = hi.p1.getOffset();
                        continue;
                    }
                    p0 = Math.min(p0, hi.p0.getOffset());
                    p1 = Math.max(p1, hi.p1.getOffset());
                }
                if (minX != maxX && minY != maxY) {
                    this.component.repaint(minX, minY, maxX - minX, maxY - minY);
                }
                if (p0 != -1) {
                    try {
                        this.safeDamageRange(p0, p1);
                    }
                    catch (BadLocationException badLocationException) {
                        // empty catch block
                    }
                }
                this.highlights.removeAllElements();
            }
        } else if (mapper != null && (len = this.highlights.size()) != 0) {
            int p0 = Integer.MAX_VALUE;
            int p1 = 0;
            for (int i = 0; i < len; ++i) {
                HighlightInfo info = this.highlights.elementAt(i);
                p0 = Math.min(p0, info.p0.getOffset());
                p1 = Math.max(p1, info.p1.getOffset());
            }
            try {
                this.safeDamageRange(p0, p1);
            }
            catch (BadLocationException badLocationException) {
                // empty catch block
            }
            this.highlights.removeAllElements();
        }
    }

    @Override
    public void changeHighlight(Object tag, int p0, int p1) throws BadLocationException {
        if (p0 < 0) {
            throw new BadLocationException("Invalid beginning of the range", p0);
        }
        if (p1 < p0) {
            throw new BadLocationException("Invalid end of the range", p1);
        }
        Document doc = this.component.getDocument();
        if (tag instanceof LayeredHighlightInfo) {
            LayeredHighlightInfo lhi = (LayeredHighlightInfo)tag;
            if (lhi.width > 0 && lhi.height > 0) {
                this.component.repaint(lhi.x, lhi.y, lhi.width, lhi.height);
            }
            lhi.height = 0;
            lhi.width = 0;
            lhi.p0 = doc.createPosition(p0);
            lhi.p1 = doc.createPosition(p1);
            this.safeDamageRange(p0, p1);
        } else {
            HighlightInfo info = (HighlightInfo)tag;
            int oldP0 = info.p0.getOffset();
            int oldP1 = info.p1.getOffset();
            if (p0 == oldP0) {
                this.safeDamageRange(Math.min(oldP1, p1), Math.max(oldP1, p1));
            } else if (p1 == oldP1) {
                this.safeDamageRange(Math.min(p0, oldP0), Math.max(p0, oldP0));
            } else {
                this.safeDamageRange(oldP0, oldP1);
                this.safeDamageRange(p0, p1);
            }
            info.p0 = doc.createPosition(p0);
            info.p1 = doc.createPosition(p1);
        }
    }

    @Override
    public Highlighter.Highlight[] getHighlights() {
        int size = this.highlights.size();
        if (size == 0) {
            return noHighlights;
        }
        Object[] h = new Highlighter.Highlight[size];
        this.highlights.copyInto(h);
        return h;
    }

    @Override
    public void paintLayeredHighlights(Graphics g, int p0, int p1, Shape viewBounds, JTextComponent editor, View view) {
        for (int counter = this.highlights.size() - 1; counter >= 0; --counter) {
            HighlightInfo tag = this.highlights.elementAt(counter);
            if (!(tag instanceof LayeredHighlightInfo)) continue;
            LayeredHighlightInfo lhi = (LayeredHighlightInfo)tag;
            int start = lhi.getStartOffset();
            int end = lhi.getEndOffset();
            if ((p0 >= start || p1 <= start) && (p0 < start || p0 >= end)) continue;
            lhi.paintLayeredHighlights(g, p0, p1, viewBounds, editor, view);
        }
    }

    private void safeDamageRange(Position p0, Position p1) {
        this.safeDamager.damageRange(p0, p1);
    }

    private void safeDamageRange(int a0, int a1) throws BadLocationException {
        Document doc = this.component.getDocument();
        this.safeDamageRange(doc.createPosition(a0), doc.createPosition(a1));
    }

    public void setDrawsLayeredHighlights(boolean newValue) {
        this.drawsLayeredHighlights = newValue;
    }

    public boolean getDrawsLayeredHighlights() {
        return this.drawsLayeredHighlights;
    }

    class SafeDamager
    implements Runnable {
        private Vector<Position> p0 = new Vector(10);
        private Vector<Position> p1 = new Vector(10);
        private Document lastDoc = null;

        SafeDamager() {
        }

        @Override
        public synchronized void run() {
            TextUI mapper;
            if (DefaultHighlighter.this.component != null && (mapper = DefaultHighlighter.this.component.getUI()) != null && this.lastDoc == DefaultHighlighter.this.component.getDocument()) {
                int len = this.p0.size();
                for (int i = 0; i < len; ++i) {
                    mapper.damageRange(DefaultHighlighter.this.component, this.p0.get(i).getOffset(), this.p1.get(i).getOffset());
                }
            }
            this.p0.clear();
            this.p1.clear();
            this.lastDoc = null;
        }

        public synchronized void damageRange(Position pos0, Position pos1) {
            if (DefaultHighlighter.this.component == null) {
                this.p0.clear();
                this.lastDoc = null;
                return;
            }
            boolean addToQueue = this.p0.isEmpty();
            Document curDoc = DefaultHighlighter.this.component.getDocument();
            if (curDoc != this.lastDoc) {
                if (!this.p0.isEmpty()) {
                    this.p0.clear();
                    this.p1.clear();
                }
                this.lastDoc = curDoc;
            }
            this.p0.add(pos0);
            this.p1.add(pos1);
            if (addToQueue) {
                SwingUtilities.invokeLater(this);
            }
        }
    }

    static class HighlightInfo
    implements Highlighter.Highlight {
        Position p0;
        Position p1;
        Highlighter.HighlightPainter painter;

        HighlightInfo() {
        }

        @Override
        public int getStartOffset() {
            return this.p0.getOffset();
        }

        @Override
        public int getEndOffset() {
            return this.p1.getOffset();
        }

        @Override
        public Highlighter.HighlightPainter getPainter() {
            return this.painter;
        }
    }

    static class LayeredHighlightInfo
    extends HighlightInfo {
        int x;
        int y;
        int width;
        int height;

        LayeredHighlightInfo() {
        }

        void union(Shape bounds) {
            if (bounds == null) {
                return;
            }
            Rectangle alloc = bounds instanceof Rectangle ? (Rectangle)bounds : bounds.getBounds();
            if (this.width == 0 || this.height == 0) {
                this.x = alloc.x;
                this.y = alloc.y;
                this.width = alloc.width;
                this.height = alloc.height;
            } else {
                this.width = Math.max(this.x + this.width, alloc.x + alloc.width);
                this.height = Math.max(this.y + this.height, alloc.y + alloc.height);
                this.x = Math.min(this.x, alloc.x);
                this.width -= this.x;
                this.y = Math.min(this.y, alloc.y);
                this.height -= this.y;
            }
        }

        void paintLayeredHighlights(Graphics g, int p0, int p1, Shape viewBounds, JTextComponent editor, View view) {
            int start = this.getStartOffset();
            int end = this.getEndOffset();
            p0 = Math.max(start, p0);
            p1 = Math.min(end, p1);
            this.union(((LayeredHighlighter.LayerPainter)this.painter).paintLayer(g, p0, p1, viewBounds, editor, view));
        }
    }

    public static class DefaultHighlightPainter
    extends LayeredHighlighter.LayerPainter {
        private Color color;

        public DefaultHighlightPainter(Color c) {
            this.color = c;
        }

        public Color getColor() {
            return this.color;
        }

        @Override
        public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
            Rectangle alloc = bounds.getBounds();
            try {
                TextUI mapper = c.getUI();
                Rectangle p0 = mapper.modelToView(c, offs0);
                Rectangle p1 = mapper.modelToView(c, offs1);
                Color color = this.getColor();
                if (color == null) {
                    g.setColor(c.getSelectionColor());
                } else {
                    g.setColor(color);
                }
                if (p0.y == p1.y) {
                    Rectangle r = p0.union(p1);
                    g.fillRect(r.x, r.y, r.width, r.height);
                } else {
                    int p0ToMarginWidth = alloc.x + alloc.width - p0.x;
                    g.fillRect(p0.x, p0.y, p0ToMarginWidth, p0.height);
                    if (p0.y + p0.height != p1.y) {
                        g.fillRect(alloc.x, p0.y + p0.height, alloc.width, p1.y - (p0.y + p0.height));
                    }
                    g.fillRect(alloc.x, p1.y, p1.x - alloc.x, p1.height);
                }
            }
            catch (BadLocationException badLocationException) {
                // empty catch block
            }
        }

        @Override
        public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
            Rectangle r;
            Color color = this.getColor();
            if (color == null) {
                g.setColor(c.getSelectionColor());
            } else {
                g.setColor(color);
            }
            if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
                r = bounds instanceof Rectangle ? (Rectangle)bounds : bounds.getBounds();
            } else {
                try {
                    Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);
                    r = shape instanceof Rectangle ? (Rectangle)shape : shape.getBounds();
                }
                catch (BadLocationException e) {
                    r = null;
                }
            }
            if (r != null) {
                r.width = Math.max(r.width, 1);
                g.fillRect(r.x, r.y, r.width, r.height);
            }
            return r;
        }
    }
}

