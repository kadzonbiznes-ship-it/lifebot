/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Color;
import java.awt.Component;
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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Objects;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.SegmentCache;
import javax.swing.text.StateInvariantError;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class PlainView
extends View
implements TabExpander {
    protected FontMetrics metrics;
    Element longLine;
    Font font;
    Segment lineBuffer;
    float tabSize;
    int tabBase;
    int sel0;
    int sel1;
    Color unselected;
    Color selected;
    int firstLineOffset;
    private static SoftReference<HashMap<FPMethodItem, Boolean>> methodsOverriddenMapRef;
    final boolean drawLineOverridden = PlainView.getFPMethodOverridden(this.getClass(), "drawLine", FPMethodArgs.IGNN);
    final boolean drawSelectedTextOverridden = PlainView.getFPMethodOverridden(this.getClass(), "drawSelectedText", FPMethodArgs.GNNII);
    final boolean drawUnselectedTextOverridden = PlainView.getFPMethodOverridden(this.getClass(), "drawUnselectedText", FPMethodArgs.GNNII);
    final boolean useFloatingPointAPI = this.drawUnselectedTextOverridden || this.drawSelectedTextOverridden;

    public PlainView(Element elem) {
        super(elem);
    }

    protected int getTabSize() {
        Integer i = (Integer)this.getDocument().getProperty("tabSize");
        int size = i != null ? i : 8;
        return size;
    }

    @Deprecated(since="9")
    protected void drawLine(int lineIndex, Graphics g, int x, int y) {
        this.drawLineImpl(lineIndex, g, x, y);
    }

    private void drawLineImpl(int lineIndex, Graphics g, float x, float y) {
        Element line = this.getElement().getElement(lineIndex);
        try {
            if (line.isLeaf()) {
                this.drawElement(lineIndex, line, g, x, y);
            } else {
                int count = line.getElementCount();
                for (int i = 0; i < count; ++i) {
                    Element elem = line.getElement(i);
                    x = this.drawElement(lineIndex, elem, g, x, y);
                }
            }
        }
        catch (BadLocationException e) {
            throw new StateInvariantError("Can't render line: " + lineIndex);
        }
    }

    protected void drawLine(int lineIndex, Graphics2D g, float x, float y) {
        this.drawLineImpl(lineIndex, g, x, y);
    }

    private float drawElement(int lineIndex, Element elem, Graphics g, float x, float y) throws BadLocationException {
        AttributeSet attr;
        int p0 = elem.getStartOffset();
        int p1 = elem.getEndOffset();
        p1 = Math.min(this.getDocument().getLength(), p1);
        if (lineIndex == 0) {
            x += (float)this.firstLineOffset;
        }
        if (Utilities.isComposedTextAttributeDefined(attr = elem.getAttributes())) {
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
        Segment s = SegmentCache.getSharedSegment();
        doc.getText(p0, p1 - p0, s);
        float ret = Utilities.drawTabbedText(this, s, x, y, g, this, p0, null, useFPAPI);
        SegmentCache.releaseSharedSegment(s);
        return ret;
    }

    protected float drawUnselectedText(Graphics2D g, float x, float y, int p0, int p1) throws BadLocationException {
        return this.drawUnselectedTextImpl(g, x, y, p0, p1, true);
    }

    @Deprecated(since="9")
    protected int drawSelectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
        return (int)this.drawSelectedTextImpl(g, x, y, p0, p1, false);
    }

    float callDrawSelectedText(Graphics g, float x, float y, int p0, int p1) throws BadLocationException {
        return this.drawSelectedTextOverridden && g instanceof Graphics2D ? this.drawSelectedText((Graphics2D)g, x, y, p0, p1) : (float)this.drawSelectedText(g, (int)x, (int)y, p0, p1);
    }

    private float drawSelectedTextImpl(Graphics g, float x, float y, int p0, int p1, boolean useFPAPI) throws BadLocationException {
        g.setColor(this.selected);
        Document doc = this.getDocument();
        Segment s = SegmentCache.getSharedSegment();
        doc.getText(p0, p1 - p0, s);
        float ret = Utilities.drawTabbedText(this, s, x, y, g, this, p0, null, useFPAPI);
        SegmentCache.releaseSharedSegment(s);
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

    protected void updateMetrics() {
        FontMetrics fm;
        Container host = this.getContainer();
        Font f = host.getFont();
        FontMetrics fontMetrics = fm = this.font == null ? null : host.getFontMetrics(this.font);
        if (this.font != f || !Objects.equals(this.metrics, fm)) {
            this.calculateLongestLine();
            if (this.useFloatingPointAPI) {
                FontRenderContext frc = this.metrics.getFontRenderContext();
                float tabWidth = (float)this.font.getStringBounds("m", frc).getWidth();
                this.tabSize = (float)this.getTabSize() * tabWidth;
            } else {
                this.tabSize = this.getTabSize() * this.metrics.charWidth('m');
            }
        }
    }

    @Override
    public float getPreferredSpan(int axis) {
        this.updateMetrics();
        switch (axis) {
            case 0: {
                return this.getLineWidth(this.longLine);
            }
            case 1: {
                return this.getElement().getElementCount() * this.metrics.getHeight();
            }
        }
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }

    @Override
    public void paint(Graphics g, Shape a) {
        int linesTotal;
        int linesAbove;
        int linesBelow;
        Shape originalA = a;
        a = this.adjustPaintRegion(a);
        Rectangle alloc = (Rectangle)a;
        this.tabBase = alloc.x;
        JTextComponent host = (JTextComponent)this.getContainer();
        Highlighter h = host.getHighlighter();
        g.setFont(host.getFont());
        this.sel0 = host.getSelectionStart();
        this.sel1 = host.getSelectionEnd();
        this.unselected = host.isEnabled() ? host.getForeground() : host.getDisabledTextColor();
        Caret c = host.getCaret();
        this.selected = c.isSelectionVisible() && h != null ? host.getSelectedTextColor() : this.unselected;
        this.updateMetrics();
        Rectangle clip = g.getClipBounds();
        int fontHeight = this.metrics.getHeight();
        int heightBelow = alloc.y + alloc.height - (clip.y + clip.height);
        int heightAbove = clip.y - alloc.y;
        if (fontHeight > 0) {
            linesBelow = Math.max(0, heightBelow / fontHeight);
            linesAbove = Math.max(0, heightAbove / fontHeight);
            linesTotal = alloc.height / fontHeight;
            if (alloc.height % fontHeight != 0) {
                ++linesTotal;
            }
        } else {
            linesTotal = 0;
            linesAbove = 0;
            linesBelow = 0;
        }
        Rectangle lineArea = this.lineToRect(a, linesAbove);
        int y = lineArea.y + this.metrics.getAscent();
        int x = lineArea.x;
        Element map = this.getElement();
        int lineCount = map.getElementCount();
        int endLine = Math.min(lineCount, linesTotal - linesBelow);
        --lineCount;
        LayeredHighlighter dh = h instanceof LayeredHighlighter ? (LayeredHighlighter)h : null;
        for (int line = linesAbove; line < endLine; ++line) {
            if (dh != null) {
                Element lineElement = map.getElement(line);
                if (line == lineCount) {
                    dh.paintLayeredHighlights(g, lineElement.getStartOffset(), lineElement.getEndOffset(), originalA, host, this);
                } else {
                    dh.paintLayeredHighlights(g, lineElement.getStartOffset(), lineElement.getEndOffset() - 1, originalA, host, this);
                }
            }
            if (this.drawLineOverridden && g instanceof Graphics2D) {
                this.drawLine(line, (Graphics2D)g, (float)x, (float)y);
            } else {
                this.drawLine(line, g, x, y);
            }
            y += fontHeight;
            if (line != 0) continue;
            x -= this.firstLineOffset;
        }
    }

    Shape adjustPaintRegion(Shape a) {
        return a;
    }

    @Override
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        Document doc = this.getDocument();
        Element map = this.getElement();
        int lineIndex = map.getElementIndex(pos);
        if (lineIndex < 0) {
            return this.lineToRect(a, 0);
        }
        Rectangle lineArea = this.lineToRect(a, lineIndex);
        this.tabBase = lineArea.x;
        Element line = map.getElement(lineIndex);
        int p0 = line.getStartOffset();
        Segment s = SegmentCache.getSharedSegment();
        doc.getText(p0, pos - p0, s);
        if (this.useFloatingPointAPI) {
            float xOffs = Utilities.getTabbedTextWidth(s, this.metrics, (float)this.tabBase, (TabExpander)this, p0);
            SegmentCache.releaseSharedSegment(s);
            return new Rectangle2D.Float((float)lineArea.x + xOffs, lineArea.y, 1.0f, this.metrics.getHeight());
        }
        int xOffs = Utilities.getTabbedTextWidth(s, this.metrics, this.tabBase, (TabExpander)this, p0);
        SegmentCache.releaseSharedSegment(s);
        lineArea.x += xOffs;
        lineArea.width = 1;
        lineArea.height = this.metrics.getHeight();
        return lineArea;
    }

    @Override
    public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
        int lineIndex;
        bias[0] = Position.Bias.Forward;
        Rectangle alloc = a.getBounds();
        Document doc = this.getDocument();
        if (y < (float)alloc.y) {
            return this.getStartOffset();
        }
        if (y > (float)(alloc.y + alloc.height)) {
            return this.getEndOffset() - 1;
        }
        Element map = doc.getDefaultRootElement();
        int fontHeight = this.metrics.getHeight();
        int n = lineIndex = fontHeight > 0 ? (int)Math.abs((y - (float)alloc.y) / (float)fontHeight) : map.getElementCount() - 1;
        if (lineIndex >= map.getElementCount()) {
            return this.getEndOffset() - 1;
        }
        Element line = map.getElement(lineIndex);
        boolean dx = false;
        if (lineIndex == 0) {
            alloc.x += this.firstLineOffset;
            alloc.width -= this.firstLineOffset;
        }
        if (x < (float)alloc.x) {
            return line.getStartOffset();
        }
        if (x > (float)(alloc.x + alloc.width)) {
            return line.getEndOffset() - 1;
        }
        try {
            int p0 = line.getStartOffset();
            int p1 = line.getEndOffset() - 1;
            Segment s = SegmentCache.getSharedSegment();
            doc.getText(p0, p1 - p0, s);
            this.tabBase = alloc.x;
            int offs = p0 + Utilities.getTabbedTextOffset(s, this.metrics, (float)this.tabBase, x, (TabExpander)this, p0, true);
            SegmentCache.releaseSharedSegment(s);
            return offs;
        }
        catch (BadLocationException e) {
            return -1;
        }
    }

    @Override
    public void insertUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
        this.updateDamage(changes, a, f);
    }

    @Override
    public void removeUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
        this.updateDamage(changes, a, f);
    }

    @Override
    public void changedUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
        this.updateDamage(changes, a, f);
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        this.updateMetrics();
    }

    @Override
    public float nextTabStop(float x, int tabOffset) {
        if (this.tabSize == 0.0f) {
            return x;
        }
        int ntabs = (int)((x - (float)this.tabBase) / this.tabSize);
        return (float)this.tabBase + (float)(ntabs + 1) * this.tabSize;
    }

    protected void updateDamage(DocumentEvent changes, Shape a, ViewFactory f) {
        Element[] removed;
        Container host = this.getContainer();
        this.updateMetrics();
        Element elem = this.getElement();
        DocumentEvent.ElementChange ec = changes.getChange(elem);
        Element[] added = ec != null ? ec.getChildrenAdded() : null;
        Element[] elementArray = removed = ec != null ? ec.getChildrenRemoved() : null;
        if (added != null && added.length > 0 || removed != null && removed.length > 0) {
            if (added != null) {
                int currWide = this.getLineWidth(this.longLine);
                for (int i = 0; i < added.length; ++i) {
                    int w = this.getLineWidth(added[i]);
                    if (w <= currWide) continue;
                    currWide = w;
                    this.longLine = added[i];
                }
            }
            if (removed != null) {
                for (int i = 0; i < removed.length; ++i) {
                    if (removed[i] != this.longLine) continue;
                    this.calculateLongestLine();
                    break;
                }
            }
            this.preferenceChanged(null, true, true);
            host.repaint();
        } else {
            Element map = this.getElement();
            int line = map.getElementIndex(changes.getOffset());
            this.damageLineRange(line, line, a, host);
            if (changes.getType() == DocumentEvent.EventType.INSERT) {
                int w = this.getLineWidth(this.longLine);
                Element e = map.getElement(line);
                if (e == this.longLine) {
                    this.preferenceChanged(null, true, false);
                } else if (this.getLineWidth(e) > w) {
                    this.longLine = e;
                    this.preferenceChanged(null, true, false);
                }
            } else if (changes.getType() == DocumentEvent.EventType.REMOVE && map.getElement(line) == this.longLine) {
                this.calculateLongestLine();
                this.preferenceChanged(null, true, false);
            }
        }
    }

    protected void damageLineRange(int line0, int line1, Shape a, Component host) {
        if (a != null) {
            Rectangle area0 = this.lineToRect(a, line0);
            Rectangle area1 = this.lineToRect(a, line1);
            if (area0 != null && area1 != null) {
                Rectangle damage = area0.union(area1);
                host.repaint(damage.x, damage.y, damage.width, damage.height);
            } else {
                host.repaint();
            }
        }
    }

    protected Rectangle lineToRect(Shape a, int line) {
        Rectangle r = null;
        this.updateMetrics();
        if (this.metrics != null) {
            Rectangle alloc = a.getBounds();
            if (line == 0) {
                alloc.x += this.firstLineOffset;
                alloc.width -= this.firstLineOffset;
            }
            r = new Rectangle(alloc.x, alloc.y + line * this.metrics.getHeight(), alloc.width, this.metrics.getHeight());
        }
        return r;
    }

    private void calculateLongestLine() {
        Container c = this.getContainer();
        this.font = c.getFont();
        this.metrics = c.getFontMetrics(this.font);
        Document doc = this.getDocument();
        Element lines = this.getElement();
        int n = lines.getElementCount();
        int maxWidth = -1;
        for (int i = 0; i < n; ++i) {
            Element line = lines.getElement(i);
            int w = this.getLineWidth(line);
            if (w <= maxWidth) continue;
            maxWidth = w;
            this.longLine = line;
        }
    }

    private int getLineWidth(Element line) {
        int w;
        if (line == null) {
            return 0;
        }
        int p0 = line.getStartOffset();
        int p1 = line.getEndOffset();
        Segment s = SegmentCache.getSharedSegment();
        try {
            line.getDocument().getText(p0, p1 - p0, s);
            w = Utilities.getTabbedTextWidth(s, this.metrics, this.tabBase, (TabExpander)this, p0);
        }
        catch (BadLocationException ble) {
            w = 0;
        }
        SegmentCache.releaseSharedSegment(s);
        return w;
    }

    static boolean getFPMethodOverridden(Class<?> cls, String method, FPMethodArgs methodArgs) {
        FPMethodItem key;
        Boolean isFPMethodOverridden;
        boolean initialized;
        HashMap<FPMethodItem, Boolean> map = null;
        boolean bl = initialized = methodsOverriddenMapRef != null && (map = methodsOverriddenMapRef.get()) != null;
        if (!initialized) {
            map = new HashMap();
            methodsOverriddenMapRef = new SoftReference<HashMap<FPMethodItem, Boolean>>(map);
        }
        if ((isFPMethodOverridden = map.get(key = new FPMethodItem(cls, method))) == null) {
            isFPMethodOverridden = PlainView.checkFPMethodOverridden(cls, method, methodArgs);
            map.put(key, isFPMethodOverridden);
        }
        return isFPMethodOverridden;
    }

    private static boolean checkFPMethodOverridden(final Class<?> className, final String methodName, final FPMethodArgs methodArgs) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

            @Override
            public Boolean run() {
                return PlainView.isFPMethodOverridden(methodName, className, methodArgs.getMethodArguments(false), methodArgs.getMethodArguments(true));
            }
        });
    }

    private static boolean isFPMethodOverridden(String method, Class<?> cls, Class<?>[] intTypes, Class<?>[] fpTypes) {
        Module thisModule = PlainView.class.getModule();
        while (!thisModule.equals(cls.getModule())) {
            try {
                cls.getDeclaredMethod(method, fpTypes);
                return true;
            }
            catch (Exception e1) {
                try {
                    cls.getDeclaredMethod(method, intTypes);
                    return false;
                }
                catch (Exception e2) {
                    cls = cls.getSuperclass();
                }
            }
        }
        return true;
    }

    static enum FPMethodArgs {
        IGNN,
        IIGNN,
        GNNII,
        GNNC;


        public Class<?>[] getMethodArguments(boolean isFPType) {
            Class<Number> N = isFPType ? Float.TYPE : Integer.TYPE;
            Class G = isFPType ? Graphics2D.class : Graphics.class;
            switch (this.ordinal()) {
                case 0: {
                    return new Class[]{Integer.TYPE, G, N, N};
                }
                case 1: {
                    return new Class[]{Integer.TYPE, Integer.TYPE, G, N, N};
                }
                case 2: {
                    return new Class[]{G, N, N, Integer.TYPE, Integer.TYPE};
                }
                case 3: {
                    return new Class[]{G, N, N, Character.TYPE};
                }
            }
            throw new RuntimeException("Unknown method arguments!");
        }
    }

    private static class FPMethodItem {
        final Class<?> className;
        final String methodName;

        public FPMethodItem(Class<?> className, String methodName) {
            this.className = className;
            this.methodName = methodName;
        }

        public boolean equals(Object obj) {
            if (obj instanceof FPMethodItem) {
                FPMethodItem that = (FPMethodItem)obj;
                return this.className.equals(that.className) && this.methodName.equals(that.methodName);
            }
            return false;
        }

        public int hashCode() {
            return 31 * this.methodName.hashCode() + this.className.hashCode();
        }
    }
}

