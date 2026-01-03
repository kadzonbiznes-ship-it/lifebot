/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Arrays;
import java.util.HashSet;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public abstract class CompositeView
extends View {
    private static View[] ZERO = new View[0];
    private View[] children = new View[1];
    private int nchildren = 0;
    private short left;
    private short right;
    private short top;
    private short bottom;
    private Rectangle childAlloc = new Rectangle();

    public CompositeView(Element elem) {
        super(elem);
    }

    protected void loadChildren(ViewFactory f) {
        if (f == null) {
            return;
        }
        Element e = this.getElement();
        int n = e.getElementCount();
        if (n > 0) {
            View[] added = new View[n];
            for (int i = 0; i < n; ++i) {
                added[i] = f.create(e.getElement(i));
            }
            this.replace(0, 0, added);
        }
    }

    @Override
    public void setParent(View parent) {
        super.setParent(parent);
        if (parent != null && this.nchildren == 0) {
            ViewFactory f = this.getViewFactory();
            this.loadChildren(f);
        }
    }

    @Override
    public int getViewCount() {
        return this.nchildren;
    }

    @Override
    public View getView(int n) {
        return this.children[n];
    }

    @Override
    public void replace(int offset, int length, View[] views) {
        if (views == null) {
            views = ZERO;
        }
        HashSet<View> set = new HashSet<View>(Arrays.asList(views));
        for (int i = offset; i < offset + length; ++i) {
            View child = this.children[i];
            if (child.getParent() == this && !set.contains(child)) {
                this.children[i].setParent(null);
            }
            this.children[i] = null;
        }
        int delta = views.length - length;
        int src = offset + length;
        int nmove = this.nchildren - src;
        int dest = src + delta;
        if (this.nchildren + delta >= this.children.length) {
            int newLength = Math.max(2 * this.children.length, this.nchildren + delta);
            View[] newChildren = new View[newLength];
            System.arraycopy(this.children, 0, newChildren, 0, offset);
            System.arraycopy(views, 0, newChildren, offset, views.length);
            System.arraycopy(this.children, src, newChildren, dest, nmove);
            this.children = newChildren;
        } else {
            System.arraycopy(this.children, src, this.children, dest, nmove);
            System.arraycopy(views, 0, this.children, offset, views.length);
        }
        this.nchildren += delta;
        for (int i = 0; i < views.length; ++i) {
            views[i].setParent(this);
        }
    }

    @Override
    public Shape getChildAllocation(int index, Shape a) {
        Rectangle alloc = this.getInsideAllocation(a);
        this.childAllocation(index, alloc);
        return alloc;
    }

    @Override
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        View v;
        int testPos;
        boolean isBackward = b == Position.Bias.Backward;
        int n = testPos = isBackward ? Math.max(0, pos - 1) : pos;
        if (isBackward && testPos < this.getStartOffset()) {
            return null;
        }
        int vIndex = this.getViewIndexAtPosition(testPos);
        if (vIndex != -1 && vIndex < this.getViewCount() && (v = this.getView(vIndex)) != null && testPos >= v.getStartOffset() && testPos < v.getEndOffset()) {
            Shape childShape = this.getChildAllocation(vIndex, a);
            if (childShape == null) {
                return null;
            }
            Shape retShape = v.modelToView(pos, childShape, b);
            if (retShape == null && v.getEndOffset() == pos && ++vIndex < this.getViewCount()) {
                v = this.getView(vIndex);
                retShape = v.modelToView(pos, this.getChildAllocation(vIndex, a), b);
            }
            return retShape;
        }
        throw new BadLocationException("Position not represented by view", pos);
    }

    @Override
    public Shape modelToView(int p0, Position.Bias b0, int p1, Position.Bias b1, Shape a) throws BadLocationException {
        Rectangle r1;
        View v1;
        Rectangle alloc;
        Rectangle r0;
        if (p0 == this.getStartOffset() && p1 == this.getEndOffset()) {
            return a;
        }
        View v0 = this.getViewAtPosition(b0 == Position.Bias.Backward ? Math.max(0, p0 - 1) : p0, r0 = new Rectangle(alloc = this.getInsideAllocation(a)));
        if (v0 == (v1 = this.getViewAtPosition(b1 == Position.Bias.Backward ? Math.max(0, p1 - 1) : p1, r1 = new Rectangle(alloc)))) {
            if (v0 == null) {
                return a;
            }
            return v0.modelToView(p0, b0, p1, b1, r0);
        }
        int viewCount = this.getViewCount();
        for (int counter = 0; counter < viewCount; ++counter) {
            View endView;
            Rectangle retRect;
            View v = this.getView(counter);
            if (v != v0 && v != v1) continue;
            Rectangle tempRect = new Rectangle();
            if (v == v0) {
                retRect = v0.modelToView(p0, b0, v0.getEndOffset(), Position.Bias.Backward, r0).getBounds();
                endView = v1;
            } else {
                retRect = v1.modelToView(v1.getStartOffset(), Position.Bias.Forward, p1, b1, r1).getBounds();
                endView = v0;
            }
            while (++counter < viewCount && (v = this.getView(counter)) != endView) {
                tempRect.setBounds(alloc);
                this.childAllocation(counter, tempRect);
                retRect.add(tempRect);
            }
            if (endView != null) {
                Shape endShape = endView == v1 ? v1.modelToView(v1.getStartOffset(), Position.Bias.Forward, p1, b1, r1) : v0.modelToView(p0, b0, v0.getEndOffset(), Position.Bias.Backward, r0);
                if (endShape instanceof Rectangle) {
                    retRect.add((Rectangle)endShape);
                } else {
                    retRect.add(endShape.getBounds());
                }
            }
            return retRect;
        }
        throw new BadLocationException("Position not represented by view", p0);
    }

    @Override
    public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
        Rectangle alloc = this.getInsideAllocation(a);
        if (this.isBefore((int)x, (int)y, alloc)) {
            int retValue = -1;
            try {
                retValue = this.getNextVisualPositionFrom(-1, Position.Bias.Forward, a, 3, bias);
            }
            catch (IllegalArgumentException | BadLocationException exception) {
                // empty catch block
            }
            if (retValue == -1) {
                retValue = this.getStartOffset();
                bias[0] = Position.Bias.Forward;
            }
            return retValue;
        }
        if (this.isAfter((int)x, (int)y, alloc)) {
            int retValue = -1;
            try {
                retValue = this.getNextVisualPositionFrom(-1, Position.Bias.Forward, a, 7, bias);
            }
            catch (IllegalArgumentException | BadLocationException exception) {
                // empty catch block
            }
            if (retValue == -1) {
                retValue = this.getEndOffset() - 1;
                bias[0] = Position.Bias.Forward;
            }
            return retValue;
        }
        View v = this.getViewAtPoint((int)x, (int)y, alloc);
        if (v != null) {
            return v.viewToModel(x, y, alloc, bias);
        }
        return -1;
    }

    @Override
    public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a, int direction, Position.Bias[] biasRet) throws BadLocationException {
        if (pos < -1 || pos > this.getDocument().getLength()) {
            throw new BadLocationException("invalid position", pos);
        }
        Rectangle alloc = this.getInsideAllocation(a);
        switch (direction) {
            case 1: {
                return this.getNextNorthSouthVisualPositionFrom(pos, b, a, direction, biasRet);
            }
            case 5: {
                return this.getNextNorthSouthVisualPositionFrom(pos, b, a, direction, biasRet);
            }
            case 3: {
                return this.getNextEastWestVisualPositionFrom(pos, b, a, direction, biasRet);
            }
            case 7: {
                return this.getNextEastWestVisualPositionFrom(pos, b, a, direction, biasRet);
            }
        }
        throw new IllegalArgumentException("Bad direction: " + direction);
    }

    @Override
    public int getViewIndex(int pos, Position.Bias b) {
        if (b == Position.Bias.Backward) {
            --pos;
        }
        if (pos >= this.getStartOffset() && pos < this.getEndOffset()) {
            return this.getViewIndexAtPosition(pos);
        }
        return -1;
    }

    protected abstract boolean isBefore(int var1, int var2, Rectangle var3);

    protected abstract boolean isAfter(int var1, int var2, Rectangle var3);

    protected abstract View getViewAtPoint(int var1, int var2, Rectangle var3);

    protected abstract void childAllocation(int var1, Rectangle var2);

    protected View getViewAtPosition(int pos, Rectangle a) {
        int index = this.getViewIndexAtPosition(pos);
        if (index >= 0 && index < this.getViewCount()) {
            View v = this.getView(index);
            if (a != null) {
                this.childAllocation(index, a);
            }
            return v;
        }
        return null;
    }

    protected int getViewIndexAtPosition(int pos) {
        Element elem = this.getElement();
        return elem.getElementIndex(pos);
    }

    protected Rectangle getInsideAllocation(Shape a) {
        if (a != null) {
            Rectangle alloc = a instanceof Rectangle ? (Rectangle)a : a.getBounds();
            this.childAlloc.setBounds(alloc);
            this.childAlloc.x += this.getLeftInset();
            this.childAlloc.y += this.getTopInset();
            this.childAlloc.width -= this.getLeftInset() + this.getRightInset();
            this.childAlloc.height -= this.getTopInset() + this.getBottomInset();
            return this.childAlloc;
        }
        return null;
    }

    protected void setParagraphInsets(AttributeSet attr) {
        this.top = (short)StyleConstants.getSpaceAbove(attr);
        this.left = (short)StyleConstants.getLeftIndent(attr);
        this.bottom = (short)StyleConstants.getSpaceBelow(attr);
        this.right = (short)StyleConstants.getRightIndent(attr);
    }

    protected void setInsets(short top, short left, short bottom, short right) {
        this.top = top;
        this.left = left;
        this.right = right;
        this.bottom = bottom;
    }

    protected short getLeftInset() {
        return this.left;
    }

    protected short getRightInset() {
        return this.right;
    }

    protected short getTopInset() {
        return this.top;
    }

    protected short getBottomInset() {
        return this.bottom;
    }

    protected int getNextNorthSouthVisualPositionFrom(int pos, Position.Bias b, Shape a, int direction, Position.Bias[] biasRet) throws BadLocationException {
        if (pos < -1 || pos > this.getDocument().getLength()) {
            throw new BadLocationException("invalid position", pos);
        }
        return Utilities.getNextVisualPositionFrom(this, pos, b, a, direction, biasRet);
    }

    protected int getNextEastWestVisualPositionFrom(int pos, Position.Bias b, Shape a, int direction, Position.Bias[] biasRet) throws BadLocationException {
        if (pos < -1 || pos > this.getDocument().getLength()) {
            throw new BadLocationException("invalid position", pos);
        }
        return Utilities.getNextVisualPositionFrom(this, pos, b, a, direction, biasRet);
    }

    protected boolean flipEastAndWestAtEnds(int position, Position.Bias bias) {
        return false;
    }
}

