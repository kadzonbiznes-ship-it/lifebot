/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Vector;
import javax.swing.SizeRequirements;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.CompositeView;
import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.Position;
import javax.swing.text.TabExpander;
import javax.swing.text.TabableView;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public abstract class FlowView
extends BoxView {
    protected int layoutSpan = Integer.MAX_VALUE;
    protected View layoutPool;
    protected FlowStrategy strategy = new FlowStrategy();

    public FlowView(Element elem, int axis) {
        super(elem, axis);
    }

    public int getFlowAxis() {
        if (this.getAxis() == 1) {
            return 0;
        }
        return 1;
    }

    public int getFlowSpan(int index) {
        return this.layoutSpan;
    }

    public int getFlowStart(int index) {
        return 0;
    }

    protected abstract View createRow();

    @Override
    protected void loadChildren(ViewFactory f) {
        if (this.layoutPool == null) {
            this.layoutPool = new LogicalView(this.getElement());
        }
        this.layoutPool.setParent(this);
        this.strategy.insertUpdate(this, null, null);
    }

    @Override
    protected int getViewIndexAtPosition(int pos) {
        if (pos >= this.getStartOffset() && pos < this.getEndOffset()) {
            for (int counter = 0; counter < this.getViewCount(); ++counter) {
                View v = this.getView(counter);
                if (pos < v.getStartOffset() || pos >= v.getEndOffset()) continue;
                return counter;
            }
        }
        return -1;
    }

    @Override
    protected void layout(int width, int height) {
        int faxis = this.getFlowAxis();
        int newSpan = faxis == 0 ? width : height;
        if (this.layoutSpan != newSpan) {
            this.layoutChanged(faxis);
            this.layoutChanged(this.getAxis());
            this.layoutSpan = newSpan;
        }
        if (!this.isLayoutValid(faxis)) {
            int heightAxis = this.getAxis();
            int oldFlowHeight = heightAxis == 0 ? this.getWidth() : this.getHeight();
            this.strategy.layout(this);
            int newFlowHeight = (int)this.getPreferredSpan(heightAxis);
            if (oldFlowHeight != newFlowHeight) {
                Container host;
                View p = this.getParent();
                if (p != null) {
                    p.preferenceChanged(this, heightAxis == 0, heightAxis == 1);
                }
                if ((host = this.getContainer()) != null) {
                    host.repaint();
                }
            }
        }
        super.layout(width, height);
    }

    @Override
    protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
        if (r == null) {
            r = new SizeRequirements();
        }
        float pref = this.layoutPool.getPreferredSpan(axis);
        float min = this.layoutPool.getMinimumSpan(axis);
        r.minimum = (int)min;
        r.preferred = Math.max(r.minimum, (int)pref);
        r.maximum = Integer.MAX_VALUE;
        r.alignment = 0.5f;
        return r;
    }

    @Override
    public void insertUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
        this.layoutPool.insertUpdate(changes, a, f);
        this.strategy.insertUpdate(this, changes, this.getInsideAllocation(a));
    }

    @Override
    public void removeUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
        this.layoutPool.removeUpdate(changes, a, f);
        this.strategy.removeUpdate(this, changes, this.getInsideAllocation(a));
    }

    @Override
    public void changedUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
        this.layoutPool.changedUpdate(changes, a, f);
        this.strategy.changedUpdate(this, changes, this.getInsideAllocation(a));
    }

    @Override
    public void setParent(View parent) {
        super.setParent(parent);
        if (parent == null && this.layoutPool != null) {
            this.layoutPool.setParent(null);
        }
    }

    public static class FlowStrategy {
        Position damageStart = null;
        Vector<View> viewBuffer;

        void addDamage(FlowView fv, int offset) {
            block3: {
                if (offset >= fv.getStartOffset() && offset < fv.getEndOffset() && (this.damageStart == null || offset < this.damageStart.getOffset())) {
                    try {
                        this.damageStart = fv.getDocument().createPosition(offset);
                    }
                    catch (BadLocationException e) {
                        if ($assertionsDisabled) break block3;
                        throw new AssertionError();
                    }
                }
            }
        }

        void unsetDamage() {
            this.damageStart = null;
        }

        public void insertUpdate(FlowView fv, DocumentEvent e, Rectangle alloc) {
            if (e != null) {
                this.addDamage(fv, e.getOffset());
            }
            if (alloc != null) {
                Container host = fv.getContainer();
                if (host != null) {
                    host.repaint(alloc.x, alloc.y, alloc.width, alloc.height);
                }
            } else {
                fv.preferenceChanged(null, true, true);
            }
        }

        public void removeUpdate(FlowView fv, DocumentEvent e, Rectangle alloc) {
            this.addDamage(fv, e.getOffset());
            if (alloc != null) {
                Container host = fv.getContainer();
                if (host != null) {
                    host.repaint(alloc.x, alloc.y, alloc.width, alloc.height);
                }
            } else {
                fv.preferenceChanged(null, true, true);
            }
        }

        public void changedUpdate(FlowView fv, DocumentEvent e, Rectangle alloc) {
            this.addDamage(fv, e.getOffset());
            if (alloc != null) {
                Container host = fv.getContainer();
                if (host != null) {
                    host.repaint(alloc.x, alloc.y, alloc.width, alloc.height);
                }
            } else {
                fv.preferenceChanged(null, true, true);
            }
        }

        protected View getLogicalView(FlowView fv) {
            return fv.layoutPool;
        }

        public void layout(FlowView fv) {
            int p0;
            int rowIndex;
            View pool = this.getLogicalView(fv);
            int p1 = fv.getEndOffset();
            if (fv.majorAllocValid) {
                if (this.damageStart == null) {
                    return;
                }
                int offset = this.damageStart.getOffset();
                while ((rowIndex = fv.getViewIndexAtPosition(offset)) < 0) {
                    --offset;
                }
                if (rowIndex > 0) {
                    --rowIndex;
                }
                p0 = fv.getView(rowIndex).getStartOffset();
            } else {
                rowIndex = 0;
                p0 = fv.getStartOffset();
            }
            this.reparentViews(pool, p0);
            this.viewBuffer = new Vector(10, 10);
            int rowCount = fv.getViewCount();
            while (p0 < p1) {
                if (rowIndex >= rowCount) {
                    row = fv.createRow();
                    fv.append(row);
                } else {
                    row = fv.getView(rowIndex);
                }
                p0 = this.layoutRow(fv, rowIndex, p0);
                ++rowIndex;
            }
            this.viewBuffer = null;
            if (rowIndex < rowCount) {
                fv.replace(rowIndex, rowCount - rowIndex, null);
            }
            this.unsetDamage();
        }

        protected int layoutRow(FlowView fv, int rowIndex, int pos) {
            View v;
            View row = fv.getView(rowIndex);
            float x = fv.getFlowStart(rowIndex);
            float spanLeft = fv.getFlowSpan(rowIndex);
            int end = fv.getEndOffset();
            TabExpander te = fv instanceof TabExpander ? (TabExpander)((Object)fv) : null;
            int flowAxis = fv.getFlowAxis();
            int breakWeight = 0;
            float breakX = 0.0f;
            float breakSpan = 0.0f;
            int breakIndex = -1;
            int n = 0;
            this.viewBuffer.clear();
            while (pos < end && spanLeft >= 0.0f && (v = this.createView(fv, pos, (int)spanLeft, rowIndex)) != null) {
                float chunkSpan;
                int bw = v.getBreakWeight(flowAxis, x, spanLeft);
                if (bw >= 3000) {
                    View w = v.breakView(flowAxis, pos, x, spanLeft);
                    if (w != null) {
                        this.viewBuffer.add(w);
                        break;
                    }
                    if (n != 0) break;
                    this.viewBuffer.add(v);
                    break;
                }
                if (bw >= breakWeight && bw > 0) {
                    breakWeight = bw;
                    breakX = x;
                    breakSpan = spanLeft;
                    breakIndex = n;
                }
                if ((chunkSpan = flowAxis == 0 && v instanceof TabableView ? ((TabableView)((Object)v)).getTabbedSpan(x, te) : v.getPreferredSpan(flowAxis)) > spanLeft && breakIndex >= 0) {
                    if (breakIndex < n) {
                        v = this.viewBuffer.get(breakIndex);
                    }
                    for (int i = n - 1; i >= breakIndex; --i) {
                        this.viewBuffer.remove(i);
                    }
                    v = v.breakView(flowAxis, v.getStartOffset(), breakX, breakSpan);
                }
                spanLeft -= chunkSpan;
                x += chunkSpan;
                this.viewBuffer.add(v);
                pos = v.getEndOffset();
                ++n;
            }
            View[] views = new View[this.viewBuffer.size()];
            this.viewBuffer.toArray(views);
            row.replace(0, row.getViewCount(), views);
            return views.length > 0 ? row.getEndOffset() : pos;
        }

        protected void adjustRow(FlowView fv, int rowIndex, int desiredSpan, int x) {
            View tmpView;
            View v;
            int flowAxis = fv.getFlowAxis();
            View r = fv.getView(rowIndex);
            int n = r.getViewCount();
            int span = 0;
            int bestWeight = 0;
            int bestSpan = 0;
            int bestIndex = -1;
            for (int i = 0; i < n; ++i) {
                int spanLeft;
                v = r.getView(i);
                int w = v.getBreakWeight(flowAxis, x + span, spanLeft = desiredSpan - span);
                if (w >= bestWeight && w > 0) {
                    bestWeight = w;
                    bestIndex = i;
                    bestSpan = span;
                    if (w >= 3000) break;
                }
                span = (int)((float)span + v.getPreferredSpan(flowAxis));
            }
            if (bestIndex < 0) {
                return;
            }
            int spanLeft = desiredSpan - bestSpan;
            v = r.getView(bestIndex);
            v = v.breakView(flowAxis, v.getStartOffset(), x + bestSpan, spanLeft);
            View[] va = new View[]{v};
            View lv = this.getLogicalView(fv);
            int p0 = r.getView(bestIndex).getStartOffset();
            int p1 = r.getEndOffset();
            for (int i = 0; i < lv.getViewCount() && (tmpView = lv.getView(i)).getEndOffset() <= p1; ++i) {
                if (tmpView.getStartOffset() < p0) continue;
                tmpView.setParent(lv);
            }
            r.replace(bestIndex, n - bestIndex, va);
        }

        void reparentViews(View pool, int startPos) {
            int n = pool.getViewIndex(startPos, Position.Bias.Forward);
            if (n >= 0) {
                for (int i = n; i < pool.getViewCount(); ++i) {
                    pool.getView(i).setParent(pool);
                }
            }
        }

        protected View createView(FlowView fv, int startOffset, int spanLeft, int rowIndex) {
            int childIndex;
            View lv = this.getLogicalView(fv);
            View v = lv.getView(childIndex = lv.getViewIndex(startOffset, Position.Bias.Forward));
            if (startOffset == v.getStartOffset()) {
                return v;
            }
            v = v.createFragment(startOffset, v.getEndOffset());
            return v;
        }
    }

    static class LogicalView
    extends CompositeView {
        LogicalView(Element elem) {
            super(elem);
        }

        @Override
        protected int getViewIndexAtPosition(int pos) {
            Element elem = this.getElement();
            if (elem.isLeaf()) {
                return 0;
            }
            return super.getViewIndexAtPosition(pos);
        }

        @Override
        protected void loadChildren(ViewFactory f) {
            Element elem = this.getElement();
            if (elem.isLeaf()) {
                LabelView v = new LabelView(elem);
                this.append(v);
            } else {
                super.loadChildren(f);
            }
        }

        @Override
        public AttributeSet getAttributes() {
            View p = this.getParent();
            return p != null ? p.getAttributes() : null;
        }

        @Override
        public float getPreferredSpan(int axis) {
            float maxpref = 0.0f;
            float pref = 0.0f;
            int n = this.getViewCount();
            for (int i = 0; i < n; ++i) {
                View v = this.getView(i);
                pref += v.getPreferredSpan(axis);
                if (v.getBreakWeight(axis, 0.0f, 2.1474836E9f) < 3000) continue;
                maxpref = Math.max(maxpref, pref);
                pref = 0.0f;
            }
            maxpref = Math.max(maxpref, pref);
            return maxpref;
        }

        @Override
        public float getMinimumSpan(int axis) {
            float maxmin = 0.0f;
            float min = 0.0f;
            boolean nowrap = false;
            int n = this.getViewCount();
            for (int i = 0; i < n; ++i) {
                View v = this.getView(i);
                if (v.getBreakWeight(axis, 0.0f, 2.1474836E9f) == 0) {
                    min += v.getPreferredSpan(axis);
                    nowrap = true;
                } else if (nowrap) {
                    maxmin = Math.max(min, maxmin);
                    nowrap = false;
                    min = 0.0f;
                }
                if (!(v instanceof ComponentView)) continue;
                maxmin = Math.max(maxmin, v.getMinimumSpan(axis));
            }
            maxmin = Math.max(maxmin, min);
            return maxmin;
        }

        @Override
        protected void forwardUpdateToView(View v, DocumentEvent e, Shape a, ViewFactory f) {
            View parent = v.getParent();
            v.setParent(this);
            super.forwardUpdateToView(v, e, a, f);
            v.setParent(parent);
        }

        @Override
        protected void forwardUpdate(DocumentEvent.ElementChange ec, DocumentEvent e, Shape a, ViewFactory f) {
            super.forwardUpdate(ec, e, a, f);
            DocumentEvent.EventType type = e.getType();
            if (type == DocumentEvent.EventType.INSERT || type == DocumentEvent.EventType.REMOVE) {
                this.firstUpdateIndex = Math.min(this.lastUpdateIndex + 1, this.getViewCount() - 1);
                this.lastUpdateIndex = Math.max(this.getViewCount() - 1, 0);
                for (int i = this.firstUpdateIndex; i <= this.lastUpdateIndex; ++i) {
                    View v = this.getView(i);
                    if (v == null) continue;
                    v.updateAfterChange();
                }
            }
        }

        @Override
        public void paint(Graphics g, Shape allocation) {
        }

        @Override
        protected boolean isBefore(int x, int y, Rectangle alloc) {
            return false;
        }

        @Override
        protected boolean isAfter(int x, int y, Rectangle alloc) {
            return false;
        }

        @Override
        protected View getViewAtPoint(int x, int y, Rectangle alloc) {
            return null;
        }

        @Override
        protected void childAllocation(int index, Rectangle a) {
        }
    }
}

