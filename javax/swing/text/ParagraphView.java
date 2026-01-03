/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import javax.swing.SizeRequirements;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.FlowView;
import javax.swing.text.GlyphView;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.StateInvariantError;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabExpander;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.text.TabableView;
import javax.swing.text.TextLayoutStrategy;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class ParagraphView
extends FlowView
implements TabExpander {
    private int justification;
    private float lineSpacing;
    protected int firstLineIndent = 0;
    private int tabBase;
    static Class<?> i18nStrategy;
    static char[] tabChars;
    static char[] tabDecimalChars;

    public ParagraphView(Element elem) {
        super(elem, 1);
        this.setPropertiesFromAttributes();
        Document doc = elem.getDocument();
        Object i18nFlag = doc.getProperty("i18n");
        if (i18nFlag != null && i18nFlag.equals(Boolean.TRUE)) {
            try {
                this.strategy = new TextLayoutStrategy();
            }
            catch (Throwable e) {
                throw new StateInvariantError("ParagraphView: Can't create i18n strategy: " + e.getMessage());
            }
        }
    }

    protected void setJustification(int j) {
        this.justification = j;
    }

    protected void setLineSpacing(float ls) {
        this.lineSpacing = ls;
    }

    protected void setFirstLineIndent(float fi) {
        this.firstLineIndent = (int)fi;
    }

    protected void setPropertiesFromAttributes() {
        AttributeSet attr = this.getAttributes();
        if (attr != null) {
            Document doc;
            Object o;
            this.setParagraphInsets(attr);
            Integer a = (Integer)attr.getAttribute(StyleConstants.Alignment);
            int alignment = a == null ? ((o = (doc = this.getElement().getDocument()).getProperty(TextAttribute.RUN_DIRECTION)) != null && o.equals(TextAttribute.RUN_DIRECTION_RTL) ? 2 : 0) : a;
            this.setJustification(alignment);
            this.setLineSpacing(StyleConstants.getLineSpacing(attr));
            this.setFirstLineIndent(StyleConstants.getFirstLineIndent(attr));
        }
    }

    protected int getLayoutViewCount() {
        return this.layoutPool.getViewCount();
    }

    protected View getLayoutView(int index) {
        return this.layoutPool.getView(index);
    }

    @Override
    protected int getNextNorthSouthVisualPositionFrom(int pos, Position.Bias b, Shape a, int direction, Position.Bias[] biasRet) throws BadLocationException {
        int x;
        Point magicPoint;
        int vIndex;
        if (pos == -1) {
            vIndex = direction == 1 ? this.getViewCount() - 1 : 0;
        } else {
            vIndex = b == Position.Bias.Backward && pos > 0 ? this.getViewIndexAtPosition(pos - 1) : this.getViewIndexAtPosition(pos);
            if (direction == 1) {
                if (vIndex == 0) {
                    return -1;
                }
                --vIndex;
            } else if (++vIndex >= this.getViewCount()) {
                return -1;
            }
        }
        JTextComponent text = (JTextComponent)this.getContainer();
        Caret c = text.getCaret();
        Point point = magicPoint = c != null ? c.getMagicCaretPosition() : null;
        if (magicPoint == null) {
            Rectangle posBounds;
            try {
                posBounds = text.getUI().modelToView(text, pos, b);
            }
            catch (BadLocationException exc) {
                posBounds = null;
            }
            x = posBounds == null ? 0 : posBounds.getBounds().x;
        } else {
            x = magicPoint.x;
        }
        return this.getClosestPositionTo(pos, b, a, direction, biasRet, vIndex, x);
    }

    protected int getClosestPositionTo(int pos, Position.Bias b, Shape a, int direction, Position.Bias[] biasRet, int rowIndex, int x) throws BadLocationException {
        JTextComponent text = (JTextComponent)this.getContainer();
        Document doc = this.getDocument();
        View row = this.getView(rowIndex);
        int lastPos = -1;
        biasRet[0] = Position.Bias.Forward;
        int numViews = row.getViewCount();
        for (int vc = 0; vc < numViews; ++vc) {
            View v = row.getView(vc);
            int start = v.getStartOffset();
            boolean ltr = AbstractDocument.isLeftToRight(doc, start, start + 1);
            if (ltr) {
                int end = v.getEndOffset();
                for (lastPos = start; lastPos < end; ++lastPos) {
                    float xx = text.modelToView((int)lastPos).getBounds().x;
                    if (!(xx >= (float)x)) continue;
                    while (++lastPos < end && (float)text.modelToView((int)lastPos).getBounds().x == xx) {
                    }
                    return --lastPos;
                }
                --lastPos;
                continue;
            }
            for (lastPos = v.getEndOffset() - 1; lastPos >= start; --lastPos) {
                float xx = text.modelToView((int)lastPos).getBounds().x;
                if (!(xx >= (float)x)) continue;
                while (--lastPos >= start && (float)text.modelToView((int)lastPos).getBounds().x == xx) {
                }
                return ++lastPos;
            }
            ++lastPos;
        }
        if (lastPos == -1) {
            return this.getStartOffset();
        }
        return lastPos;
    }

    @Override
    protected boolean flipEastAndWestAtEnds(int position, Position.Bias bias) {
        Document doc = this.getDocument();
        return !AbstractDocument.isLeftToRight(doc, position = this.getStartOffset(), position + 1);
    }

    @Override
    public int getFlowSpan(int index) {
        View child = this.getView(index);
        int adjust = 0;
        if (child instanceof Row) {
            Row row = (Row)child;
            adjust = row.getLeftInset() + row.getRightInset();
        }
        return this.layoutSpan == Integer.MAX_VALUE ? this.layoutSpan : this.layoutSpan - adjust;
    }

    @Override
    public int getFlowStart(int index) {
        View child = this.getView(index);
        int adjust = 0;
        if (child instanceof Row) {
            Row row = (Row)child;
            adjust = row.getLeftInset();
        }
        return this.tabBase + adjust;
    }

    @Override
    protected View createRow() {
        return new Row(this.getElement());
    }

    @Override
    public float nextTabStop(float x, int tabOffset) {
        int offset;
        if (this.justification != 0) {
            return x + 10.0f;
        }
        x -= (float)this.tabBase;
        TabSet tabs = this.getTabSet();
        if (tabs == null) {
            return this.tabBase + ((int)x / 72 + 1) * 72;
        }
        TabStop tab = tabs.getTabAfter(x + 0.01f);
        if (tab == null) {
            return (float)this.tabBase + x + 5.0f;
        }
        int alignment = tab.getAlignment();
        switch (alignment) {
            default: {
                return (float)this.tabBase + tab.getPosition();
            }
            case 5: {
                return (float)this.tabBase + tab.getPosition();
            }
            case 1: 
            case 2: {
                offset = this.findOffsetToCharactersInString(tabChars, tabOffset + 1);
                break;
            }
            case 4: {
                offset = this.findOffsetToCharactersInString(tabDecimalChars, tabOffset + 1);
            }
        }
        if (offset == -1) {
            offset = this.getEndOffset();
        }
        float charsSize = this.getPartialSize(tabOffset + 1, offset);
        switch (alignment) {
            case 1: 
            case 4: {
                return (float)this.tabBase + Math.max(x, tab.getPosition() - charsSize);
            }
            case 2: {
                return (float)this.tabBase + Math.max(x, tab.getPosition() - charsSize / 2.0f);
            }
        }
        return x;
    }

    protected TabSet getTabSet() {
        return StyleConstants.getTabSet(this.getElement().getAttributes());
    }

    protected float getPartialSize(int startOffset, int endOffset) {
        float size = 0.0f;
        int numViews = this.getViewCount();
        int viewIndex = this.getElement().getElementIndex(startOffset);
        numViews = this.layoutPool.getViewCount();
        while (startOffset < endOffset && viewIndex < numViews) {
            View view = this.layoutPool.getView(viewIndex++);
            int viewEnd = view.getEndOffset();
            int tempEnd = Math.min(endOffset, viewEnd);
            if (view instanceof TabableView) {
                size += ((TabableView)((Object)view)).getPartialSpan(startOffset, tempEnd);
            } else if (startOffset == view.getStartOffset() && tempEnd == view.getEndOffset()) {
                size += view.getPreferredSpan(0);
            } else {
                return 0.0f;
            }
            startOffset = viewEnd;
        }
        return size;
    }

    protected int findOffsetToCharactersInString(char[] string, int start) {
        int stringLength = string.length;
        int end = this.getEndOffset();
        Segment seg = new Segment();
        try {
            this.getDocument().getText(start, end - start, seg);
        }
        catch (BadLocationException ble) {
            return -1;
        }
        int maxCounter = seg.offset + seg.count;
        for (int counter = seg.offset; counter < maxCounter; ++counter) {
            char currentChar = seg.array[counter];
            for (int subCounter = 0; subCounter < stringLength; ++subCounter) {
                if (currentChar != string[subCounter]) continue;
                return counter - seg.offset + start;
            }
        }
        return -1;
    }

    protected float getTabBase() {
        return this.tabBase;
    }

    @Override
    public void paint(Graphics g, Shape a) {
        Shape sh;
        Rectangle alloc = a instanceof Rectangle ? (Rectangle)a : a.getBounds();
        this.tabBase = alloc.x + this.getLeftInset();
        super.paint(g, a);
        if (this.firstLineIndent < 0 && (sh = this.getChildAllocation(0, a)) != null && sh.intersects(alloc)) {
            int x = alloc.x + this.getLeftInset() + this.firstLineIndent;
            int y = alloc.y + this.getTopInset();
            Rectangle clip = g.getClipBounds();
            this.tempRect.x = x + this.getOffset(0, 0);
            this.tempRect.y = y + this.getOffset(1, 0);
            this.tempRect.width = this.getSpan(0, 0) - this.firstLineIndent;
            this.tempRect.height = this.getSpan(1, 0);
            if (this.tempRect.intersects(clip)) {
                this.tempRect.x -= this.firstLineIndent;
                this.paintChild(g, this.tempRect, 0);
            }
        }
    }

    @Override
    public float getAlignment(int axis) {
        switch (axis) {
            case 1: {
                float a = 0.5f;
                if (this.getViewCount() != 0) {
                    int paragraphSpan = (int)this.getPreferredSpan(1);
                    View v = this.getView(0);
                    int rowSpan = (int)v.getPreferredSpan(1);
                    a = paragraphSpan != 0 ? (float)(rowSpan / 2) / (float)paragraphSpan : 0.0f;
                }
                return a;
            }
            case 0: {
                return 0.5f;
            }
        }
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }

    public View breakView(int axis, float len, Shape a) {
        if (axis == 1) {
            if (a != null) {
                Rectangle alloc = a.getBounds();
                this.setSize(alloc.width, alloc.height);
            }
            return this;
        }
        return this;
    }

    public int getBreakWeight(int axis, float len) {
        if (axis == 1) {
            return 0;
        }
        return 0;
    }

    @Override
    protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
        r = super.calculateMinorAxisRequirements(axis, r);
        float min = 0.0f;
        float glue = 0.0f;
        int n = this.getLayoutViewCount();
        for (int i = 0; i < n; ++i) {
            View v = this.getLayoutView(i);
            float span = v.getMinimumSpan(axis);
            if (v.getBreakWeight(axis, 0.0f, v.getMaximumSpan(axis)) > 0) {
                int p0 = v.getStartOffset();
                int p1 = v.getEndOffset();
                float start = this.findEdgeSpan(v, axis, p0, p0, p1);
                float end = this.findEdgeSpan(v, axis, p1, p0, p1);
                min = Math.max(min, Math.max(span, glue += start));
                glue = end;
                continue;
            }
            min = Math.max(min, glue += span);
        }
        r.minimum = Math.max(r.minimum, (int)min);
        r.preferred = Math.max(r.minimum, r.preferred);
        r.maximum = Math.max(r.preferred, r.maximum);
        return r;
    }

    private float findEdgeSpan(View v, int axis, int fp, int p0, int p1) {
        boolean breakable;
        int len = p1 - p0;
        if (len <= 1) {
            return v.getMinimumSpan(axis);
        }
        int mid = p0 + len / 2;
        boolean startEdge = mid > fp;
        View f = startEdge ? v.createFragment(fp, mid) : v.createFragment(mid, fp);
        boolean bl = breakable = f.getBreakWeight(axis, 0.0f, f.getMaximumSpan(axis)) > 0;
        if (breakable == startEdge) {
            p1 = mid;
        } else {
            p0 = mid;
        }
        return this.findEdgeSpan(f, axis, fp, p0, p1);
    }

    @Override
    public void changedUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
        this.setPropertiesFromAttributes();
        this.layoutChanged(0);
        this.layoutChanged(1);
        super.changedUpdate(changes, a, f);
    }

    static {
        tabChars = new char[1];
        ParagraphView.tabChars[0] = 9;
        tabDecimalChars = new char[2];
        ParagraphView.tabDecimalChars[0] = 9;
        ParagraphView.tabDecimalChars[1] = 46;
    }

    class Row
    extends BoxView {
        static final int SPACE_ADDON = 0;
        static final int SPACE_ADDON_LEFTOVER_END = 1;
        static final int START_JUSTIFIABLE = 2;
        static final int END_JUSTIFIABLE = 3;
        int[] justificationData;

        Row(Element elem) {
            super(elem, 0);
            this.justificationData = null;
        }

        @Override
        protected void loadChildren(ViewFactory f) {
        }

        @Override
        public AttributeSet getAttributes() {
            View p = this.getParent();
            return p != null ? p.getAttributes() : null;
        }

        @Override
        public float getAlignment(int axis) {
            if (axis == 0) {
                switch (ParagraphView.this.justification) {
                    case 0: {
                        return 0.0f;
                    }
                    case 2: {
                        return 1.0f;
                    }
                    case 1: {
                        return 0.5f;
                    }
                    case 3: {
                        float rv = 0.5f;
                        if (this.isJustifiableDocument()) {
                            rv = 0.0f;
                        }
                        return rv;
                    }
                }
            }
            return super.getAlignment(axis);
        }

        @Override
        public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
            Rectangle r = a.getBounds();
            View v = this.getViewAtPosition(pos, r);
            if (v != null && !v.getElement().isLeaf()) {
                return super.modelToView(pos, a, b);
            }
            r = a.getBounds();
            int height = r.height;
            int y = r.y;
            Shape loc = super.modelToView(pos, a, b);
            Rectangle2D bounds = loc.getBounds2D();
            bounds.setRect(bounds.getX(), y, bounds.getWidth(), height);
            return bounds;
        }

        @Override
        public int getStartOffset() {
            int offs = Integer.MAX_VALUE;
            int n = this.getViewCount();
            for (int i = 0; i < n; ++i) {
                View v = this.getView(i);
                offs = Math.min(offs, v.getStartOffset());
            }
            return offs;
        }

        @Override
        public int getEndOffset() {
            int offs = 0;
            int n = this.getViewCount();
            for (int i = 0; i < n; ++i) {
                View v = this.getView(i);
                offs = Math.max(offs, v.getEndOffset());
            }
            return offs;
        }

        @Override
        protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
            this.baselineLayout(targetSpan, axis, offsets, spans);
        }

        @Override
        protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
            return this.baselineRequirements(axis, r);
        }

        private boolean isLastRow() {
            View parent = this.getParent();
            return parent == null || this == parent.getView(parent.getViewCount() - 1);
        }

        private boolean isBrokenRow() {
            View lastView;
            boolean rv = false;
            int viewsCount = this.getViewCount();
            if (viewsCount > 0 && (lastView = this.getView(viewsCount - 1)).getBreakWeight(0, 0.0f, 0.0f) >= 3000) {
                rv = true;
            }
            return rv;
        }

        private boolean isJustifiableDocument() {
            return !Boolean.TRUE.equals(this.getDocument().getProperty("i18n"));
        }

        private boolean isJustifyEnabled() {
            boolean ret = ParagraphView.this.justification == 3;
            ret = ret && this.isJustifiableDocument();
            ret = ret && !this.isLastRow();
            ret = ret && !this.isBrokenRow();
            return ret;
        }

        @Override
        protected SizeRequirements calculateMajorAxisRequirements(int axis, SizeRequirements r) {
            int[] oldJustficationData = this.justificationData;
            this.justificationData = null;
            SizeRequirements ret = super.calculateMajorAxisRequirements(axis, r);
            if (this.isJustifyEnabled()) {
                this.justificationData = oldJustficationData;
            }
            return ret;
        }

        @Override
        protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
            int[] oldJustficationData = this.justificationData;
            this.justificationData = null;
            super.layoutMajorAxis(targetSpan, axis, offsets, spans);
            if (!this.isJustifyEnabled()) {
                return;
            }
            int currentSpan = 0;
            for (int span : spans) {
                currentSpan += span;
            }
            if (currentSpan == targetSpan) {
                return;
            }
            int extendableSpaces = 0;
            int startJustifiableContent = -1;
            int endJustifiableContent = -1;
            int lastLeadingSpaces = 0;
            int rowStartOffset = this.getStartOffset();
            int rowEndOffset = this.getEndOffset();
            int[] spaceMap = new int[rowEndOffset - rowStartOffset];
            for (int i = this.getViewCount() - 1; i >= 0; --i) {
                View view = this.getView(i);
                if (!(view instanceof GlyphView)) continue;
                GlyphView.JustificationInfo justificationInfo = ((GlyphView)view).getJustificationInfo(rowStartOffset);
                int viewStartOffset = view.getStartOffset();
                int offset = viewStartOffset - rowStartOffset;
                for (int j = 0; j < justificationInfo.spaceMap.length(); ++j) {
                    if (!justificationInfo.spaceMap.get(j)) continue;
                    spaceMap[j + offset] = 1;
                }
                if (startJustifiableContent > 0) {
                    if (justificationInfo.end >= 0) {
                        extendableSpaces += justificationInfo.trailingSpaces;
                    } else {
                        lastLeadingSpaces += justificationInfo.trailingSpaces;
                    }
                }
                if (justificationInfo.start >= 0) {
                    startJustifiableContent = justificationInfo.start + viewStartOffset;
                    extendableSpaces += lastLeadingSpaces;
                }
                if (justificationInfo.end >= 0 && endJustifiableContent < 0) {
                    endJustifiableContent = justificationInfo.end + viewStartOffset;
                }
                extendableSpaces += justificationInfo.contentSpaces;
                lastLeadingSpaces = justificationInfo.leadingSpaces;
                if (justificationInfo.hasTab) break;
            }
            if (extendableSpaces <= 0) {
                return;
            }
            int adjustment = targetSpan - currentSpan;
            int spaceAddon = extendableSpaces > 0 ? adjustment / extendableSpaces : 0;
            int spaceAddonLeftoverEnd = -1;
            int i = startJustifiableContent - rowStartOffset;
            int leftover = adjustment - spaceAddon * extendableSpaces;
            while (leftover > 0) {
                spaceAddonLeftoverEnd = i;
                leftover -= spaceMap[i];
                ++i;
            }
            if (spaceAddon > 0 || spaceAddonLeftoverEnd >= 0) {
                this.justificationData = oldJustficationData != null ? oldJustficationData : new int[4];
                this.justificationData[0] = spaceAddon;
                this.justificationData[1] = spaceAddonLeftoverEnd;
                this.justificationData[2] = startJustifiableContent - rowStartOffset;
                this.justificationData[3] = endJustifiableContent - rowStartOffset;
                super.layoutMajorAxis(targetSpan, axis, offsets, spans);
            }
        }

        @Override
        public float getMaximumSpan(int axis) {
            float ret = 0 == axis && this.isJustifyEnabled() ? Float.MAX_VALUE : super.getMaximumSpan(axis);
            return ret;
        }

        @Override
        protected int getViewIndexAtPosition(int pos) {
            if (pos < this.getStartOffset() || pos >= this.getEndOffset()) {
                return -1;
            }
            for (int counter = this.getViewCount() - 1; counter >= 0; --counter) {
                View v = this.getView(counter);
                if (pos < v.getStartOffset() || pos >= v.getEndOffset()) continue;
                return counter;
            }
            return -1;
        }

        @Override
        protected short getLeftInset() {
            int adjustment = 0;
            View parentView = this.getParent();
            if (parentView != null && this == parentView.getView(0)) {
                adjustment = ParagraphView.this.firstLineIndent;
            }
            return (short)(super.getLeftInset() + adjustment);
        }

        @Override
        protected short getBottomInset() {
            return (short)((float)super.getBottomInset() + (float)(this.minorRequest != null ? this.minorRequest.preferred : 0) * ParagraphView.this.lineSpacing);
        }
    }
}

