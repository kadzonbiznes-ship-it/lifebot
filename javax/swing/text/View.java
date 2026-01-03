/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Utilities;
import javax.swing.text.ViewFactory;

public abstract class View
implements SwingConstants {
    public static final int BadBreakWeight = 0;
    public static final int GoodBreakWeight = 1000;
    public static final int ExcellentBreakWeight = 2000;
    public static final int ForcedBreakWeight = 3000;
    public static final int X_AXIS = 0;
    public static final int Y_AXIS = 1;
    static final Position.Bias[] sharedBiasReturn = new Position.Bias[1];
    private View parent;
    private Element elem;
    int firstUpdateIndex;
    int lastUpdateIndex;

    public View(Element elem) {
        this.elem = elem;
    }

    public View getParent() {
        return this.parent;
    }

    public boolean isVisible() {
        return true;
    }

    public abstract float getPreferredSpan(int var1);

    public float getMinimumSpan(int axis) {
        int w = this.getResizeWeight(axis);
        if (w == 0) {
            return this.getPreferredSpan(axis);
        }
        return 0.0f;
    }

    public float getMaximumSpan(int axis) {
        int w = this.getResizeWeight(axis);
        if (w == 0) {
            return this.getPreferredSpan(axis);
        }
        return 2.1474836E9f;
    }

    public void preferenceChanged(View child, boolean width, boolean height) {
        View parent = this.getParent();
        if (parent != null) {
            parent.preferenceChanged(this, width, height);
        }
    }

    public float getAlignment(int axis) {
        return 0.5f;
    }

    public abstract void paint(Graphics var1, Shape var2);

    public void setParent(View parent) {
        if (parent == null) {
            for (int i = 0; i < this.getViewCount(); ++i) {
                if (this.getView(i).getParent() != this) continue;
                this.getView(i).setParent(null);
            }
        }
        this.parent = parent;
    }

    public int getViewCount() {
        return 0;
    }

    public View getView(int n) {
        return null;
    }

    public void removeAll() {
        this.replace(0, this.getViewCount(), null);
    }

    public void remove(int i) {
        this.replace(i, 1, null);
    }

    public void insert(int offs, View v) {
        View[] one = new View[]{v};
        this.replace(offs, 0, one);
    }

    public void append(View v) {
        View[] one = new View[]{v};
        this.replace(this.getViewCount(), 0, one);
    }

    public void replace(int offset, int length, View[] views) {
    }

    public int getViewIndex(int pos, Position.Bias b) {
        return -1;
    }

    public Shape getChildAllocation(int index, Shape a) {
        return null;
    }

    public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a, int direction, Position.Bias[] biasRet) throws BadLocationException {
        if (pos < -1 || pos > this.getDocument().getLength()) {
            throw new BadLocationException("Invalid position", pos);
        }
        biasRet[0] = Position.Bias.Forward;
        switch (direction) {
            case 1: 
            case 5: {
                Rectangle loc;
                if (pos == -1) {
                    pos = direction == 1 ? Math.max(0, this.getEndOffset() - 1) : this.getStartOffset();
                    break;
                }
                JTextComponent target = (JTextComponent)this.getContainer();
                Caret c = target != null ? target.getCaret() : null;
                Point mcp = c != null ? c.getMagicCaretPosition() : null;
                int x = mcp == null ? ((loc = target.modelToView(pos)) == null ? 0 : loc.x) : mcp.x;
                if (direction == 1) {
                    pos = Utilities.getPositionAbove(target, pos, x);
                    break;
                }
                pos = Utilities.getPositionBelow(target, pos, x);
                break;
            }
            case 7: {
                if (pos == -1) {
                    pos = Math.max(0, this.getEndOffset() - 1);
                    break;
                }
                pos = Math.max(0, pos - 1);
                break;
            }
            case 3: {
                if (pos == -1) {
                    pos = this.getStartOffset();
                    break;
                }
                pos = Math.min(pos + 1, this.getDocument().getLength());
                break;
            }
            default: {
                throw new IllegalArgumentException("Bad direction: " + direction);
            }
        }
        return pos;
    }

    public abstract Shape modelToView(int var1, Shape var2, Position.Bias var3) throws BadLocationException;

    public Shape modelToView(int p0, Position.Bias b0, int p1, Position.Bias b1, Shape a) throws BadLocationException {
        Rectangle r1;
        Shape s1;
        Shape s0 = this.modelToView(p0, a, b0);
        if (p1 == this.getEndOffset()) {
            try {
                s1 = this.modelToView(p1, a, b1);
            }
            catch (BadLocationException ble) {
                s1 = null;
            }
            if (s1 == null) {
                Rectangle alloc = a instanceof Rectangle ? (Rectangle)a : a.getBounds();
                s1 = new Rectangle(alloc.x + alloc.width - 1, alloc.y, 1, alloc.height);
            }
        } else {
            s1 = this.modelToView(p1, a, b1);
        }
        Rectangle r0 = s0.getBounds();
        Rectangle rectangle = r1 = s1 instanceof Rectangle ? (Rectangle)s1 : s1.getBounds();
        if (r0.y != r1.y) {
            Rectangle alloc = a instanceof Rectangle ? (Rectangle)a : a.getBounds();
            r0.x = alloc.x;
            r0.width = alloc.width;
        }
        r0.add(r1);
        return r0;
    }

    public abstract int viewToModel(float var1, float var2, Shape var3, Position.Bias[] var4);

    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        if (this.getViewCount() > 0) {
            Element elem = this.getElement();
            DocumentEvent.ElementChange ec = e.getChange(elem);
            if (ec != null && !this.updateChildren(ec, e, f)) {
                ec = null;
            }
            this.forwardUpdate(ec, e, a, f);
            this.updateLayout(ec, e, a);
        }
    }

    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        if (this.getViewCount() > 0) {
            Element elem = this.getElement();
            DocumentEvent.ElementChange ec = e.getChange(elem);
            if (ec != null && !this.updateChildren(ec, e, f)) {
                ec = null;
            }
            this.forwardUpdate(ec, e, a, f);
            this.updateLayout(ec, e, a);
        }
    }

    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        if (this.getViewCount() > 0) {
            Element elem = this.getElement();
            DocumentEvent.ElementChange ec = e.getChange(elem);
            if (ec != null && !this.updateChildren(ec, e, f)) {
                ec = null;
            }
            this.forwardUpdate(ec, e, a, f);
            this.updateLayout(ec, e, a);
        }
    }

    public Document getDocument() {
        return this.elem.getDocument();
    }

    public int getStartOffset() {
        return this.elem.getStartOffset();
    }

    public int getEndOffset() {
        return this.elem.getEndOffset();
    }

    public Element getElement() {
        return this.elem;
    }

    public Graphics getGraphics() {
        Container c = this.getContainer();
        return c.getGraphics();
    }

    public AttributeSet getAttributes() {
        return this.elem.getAttributes();
    }

    public View breakView(int axis, int offset, float pos, float len) {
        return this;
    }

    public View createFragment(int p0, int p1) {
        return this;
    }

    public int getBreakWeight(int axis, float pos, float len) {
        if (len > this.getPreferredSpan(axis)) {
            return 1000;
        }
        return 0;
    }

    public int getResizeWeight(int axis) {
        return 0;
    }

    public void setSize(float width, float height) {
    }

    public Container getContainer() {
        View v = this.getParent();
        return v != null ? v.getContainer() : null;
    }

    public ViewFactory getViewFactory() {
        View v = this.getParent();
        return v != null ? v.getViewFactory() : null;
    }

    public String getToolTipText(float x, float y, Shape allocation) {
        int viewIndex = this.getViewIndex(x, y, allocation);
        if (viewIndex >= 0) {
            Rectangle rect;
            Rectangle rectangle = rect = (allocation = this.getChildAllocation(viewIndex, allocation)) instanceof Rectangle ? (Rectangle)allocation : allocation.getBounds();
            if (rect.contains(x, y)) {
                return this.getView(viewIndex).getToolTipText(x, y, allocation);
            }
        }
        return null;
    }

    public int getViewIndex(float x, float y, Shape allocation) {
        for (int counter = this.getViewCount() - 1; counter >= 0; --counter) {
            Rectangle rect;
            Shape childAllocation = this.getChildAllocation(counter, allocation);
            if (childAllocation == null) continue;
            Rectangle rectangle = rect = childAllocation instanceof Rectangle ? (Rectangle)childAllocation : childAllocation.getBounds();
            if (!rect.contains(x, y)) continue;
            return counter;
        }
        return -1;
    }

    protected boolean updateChildren(DocumentEvent.ElementChange ec, DocumentEvent e, ViewFactory f) {
        Element[] removedElems = ec.getChildrenRemoved();
        Element[] addedElems = ec.getChildrenAdded();
        View[] added = null;
        if (addedElems != null) {
            added = new View[addedElems.length];
            for (int i = 0; i < addedElems.length; ++i) {
                added[i] = f.create(addedElems[i]);
            }
        }
        int nremoved = 0;
        int index = ec.getIndex();
        if (removedElems != null) {
            nremoved = removedElems.length;
        }
        this.replace(index, nremoved, added);
        return true;
    }

    protected void forwardUpdate(DocumentEvent.ElementChange ec, DocumentEvent e, Shape a, ViewFactory f) {
        Element[] addedElems;
        int hole0;
        this.calculateUpdateIndexes(e);
        int hole1 = hole0 = this.lastUpdateIndex + 1;
        Element[] elementArray = addedElems = ec != null ? ec.getChildrenAdded() : null;
        if (addedElems != null && addedElems.length > 0) {
            hole0 = ec.getIndex();
            hole1 = hole0 + addedElems.length - 1;
        }
        for (int i = this.firstUpdateIndex; i <= this.lastUpdateIndex; ++i) {
            View v;
            if (i >= hole0 && i <= hole1 || (v = this.getView(i)) == null) continue;
            Shape childAlloc = this.getChildAllocation(i, a);
            this.forwardUpdateToView(v, e, childAlloc, f);
        }
    }

    void calculateUpdateIndexes(DocumentEvent e) {
        View v;
        int pos = e.getOffset();
        this.firstUpdateIndex = this.getViewIndex(pos, Position.Bias.Forward);
        if (this.firstUpdateIndex == -1 && e.getType() == DocumentEvent.EventType.REMOVE && pos >= this.getEndOffset()) {
            this.firstUpdateIndex = this.getViewCount() - 1;
        }
        this.lastUpdateIndex = this.firstUpdateIndex;
        View view = v = this.firstUpdateIndex >= 0 ? this.getView(this.firstUpdateIndex) : null;
        if (v != null && v.getStartOffset() == pos && pos > 0) {
            this.firstUpdateIndex = Math.max(this.firstUpdateIndex - 1, 0);
        }
        if (e.getType() != DocumentEvent.EventType.REMOVE) {
            this.lastUpdateIndex = this.getViewIndex(pos + e.getLength(), Position.Bias.Forward);
            if (this.lastUpdateIndex < 0) {
                this.lastUpdateIndex = this.getViewCount() - 1;
            }
        }
        this.firstUpdateIndex = Math.max(this.firstUpdateIndex, 0);
    }

    void updateAfterChange() {
    }

    protected void forwardUpdateToView(View v, DocumentEvent e, Shape a, ViewFactory f) {
        DocumentEvent.EventType type = e.getType();
        if (type == DocumentEvent.EventType.INSERT) {
            v.insertUpdate(e, a, f);
        } else if (type == DocumentEvent.EventType.REMOVE) {
            v.removeUpdate(e, a, f);
        } else {
            v.changedUpdate(e, a, f);
        }
    }

    protected void updateLayout(DocumentEvent.ElementChange ec, DocumentEvent e, Shape a) {
        if (ec != null && a != null) {
            this.preferenceChanged(null, true, true);
            Container host = this.getContainer();
            if (host != null) {
                host.repaint();
            }
        }
    }

    @Deprecated
    public Shape modelToView(int pos, Shape a) throws BadLocationException {
        return this.modelToView(pos, a, Position.Bias.Forward);
    }

    @Deprecated
    public int viewToModel(float x, float y, Shape a) {
        View.sharedBiasReturn[0] = Position.Bias.Forward;
        return this.viewToModel(x, y, a, sharedBiasReturn);
    }
}

