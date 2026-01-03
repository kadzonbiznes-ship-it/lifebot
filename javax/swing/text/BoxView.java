/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.SizeRequirements;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.CompositeView;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class BoxView
extends CompositeView {
    int majorAxis;
    int majorSpan;
    int minorSpan;
    boolean majorReqValid;
    boolean minorReqValid;
    SizeRequirements majorRequest;
    SizeRequirements minorRequest;
    boolean majorAllocValid;
    int[] majorOffsets;
    int[] majorSpans;
    boolean minorAllocValid;
    int[] minorOffsets;
    int[] minorSpans;
    Rectangle tempRect = new Rectangle();

    public BoxView(Element elem, int axis) {
        super(elem);
        this.majorAxis = axis;
        this.majorOffsets = new int[0];
        this.majorSpans = new int[0];
        this.majorReqValid = false;
        this.majorAllocValid = false;
        this.minorOffsets = new int[0];
        this.minorSpans = new int[0];
        this.minorReqValid = false;
        this.minorAllocValid = false;
    }

    public int getAxis() {
        return this.majorAxis;
    }

    public void setAxis(int axis) {
        boolean axisChanged = axis != this.majorAxis;
        this.majorAxis = axis;
        if (axisChanged) {
            this.preferenceChanged(null, true, true);
        }
    }

    public void layoutChanged(int axis) {
        if (axis == this.majorAxis) {
            this.majorAllocValid = false;
        } else {
            this.minorAllocValid = false;
        }
    }

    protected boolean isLayoutValid(int axis) {
        if (axis == this.majorAxis) {
            return this.majorAllocValid;
        }
        return this.minorAllocValid;
    }

    protected void paintChild(Graphics g, Rectangle alloc, int index) {
        View child = this.getView(index);
        child.paint(g, alloc);
    }

    @Override
    public void replace(int index, int length, View[] elems) {
        super.replace(index, length, elems);
        int nInserted = elems != null ? elems.length : 0;
        this.majorOffsets = this.updateLayoutArray(this.majorOffsets, index, nInserted);
        this.majorSpans = this.updateLayoutArray(this.majorSpans, index, nInserted);
        this.majorReqValid = false;
        this.majorAllocValid = false;
        this.minorOffsets = this.updateLayoutArray(this.minorOffsets, index, nInserted);
        this.minorSpans = this.updateLayoutArray(this.minorSpans, index, nInserted);
        this.minorReqValid = false;
        this.minorAllocValid = false;
    }

    int[] updateLayoutArray(int[] oldArray, int offset, int nInserted) {
        int n = this.getViewCount();
        int[] newArray = new int[n];
        System.arraycopy(oldArray, 0, newArray, 0, offset);
        System.arraycopy(oldArray, offset, newArray, offset + nInserted, n - nInserted - offset);
        return newArray;
    }

    @Override
    protected void forwardUpdate(DocumentEvent.ElementChange ec, DocumentEvent e, Shape a, ViewFactory f) {
        boolean wasValid = this.isLayoutValid(this.majorAxis);
        super.forwardUpdate(ec, e, a, f);
        if (wasValid && !this.isLayoutValid(this.majorAxis)) {
            Container c = this.getContainer();
            if (a != null && c != null) {
                int pos = e.getOffset();
                int index = this.getViewIndexAtPosition(pos);
                Rectangle alloc = this.getInsideAllocation(a);
                if (this.majorAxis == 0) {
                    alloc.x += this.majorOffsets[index];
                    alloc.width -= this.majorOffsets[index];
                } else {
                    alloc.y += this.minorOffsets[index];
                    alloc.height -= this.minorOffsets[index];
                }
                c.repaint(alloc.x, alloc.y, alloc.width, alloc.height);
            }
        }
    }

    @Override
    public void preferenceChanged(View child, boolean width, boolean height) {
        boolean minorChanged;
        boolean majorChanged = this.majorAxis == 0 ? width : height;
        boolean bl = minorChanged = this.majorAxis == 0 ? height : width;
        if (majorChanged) {
            this.majorReqValid = false;
            this.majorAllocValid = false;
        }
        if (minorChanged) {
            this.minorReqValid = false;
            this.minorAllocValid = false;
        }
        super.preferenceChanged(child, width, height);
    }

    @Override
    public int getResizeWeight(int axis) {
        this.checkRequests(axis);
        if (axis == this.majorAxis ? this.majorRequest.preferred != this.majorRequest.minimum || this.majorRequest.preferred != this.majorRequest.maximum : this.minorRequest.preferred != this.minorRequest.minimum || this.minorRequest.preferred != this.minorRequest.maximum) {
            return 1;
        }
        return 0;
    }

    void setSpanOnAxis(int axis, float span) {
        if (axis == this.majorAxis) {
            if (this.majorSpan != (int)span) {
                this.majorAllocValid = false;
            }
            if (!this.majorAllocValid) {
                this.majorSpan = (int)span;
                this.checkRequests(this.majorAxis);
                this.layoutMajorAxis(this.majorSpan, axis, this.majorOffsets, this.majorSpans);
                this.majorAllocValid = true;
                this.updateChildSizes();
            }
        } else {
            if ((int)span != this.minorSpan) {
                this.minorAllocValid = false;
            }
            if (!this.minorAllocValid) {
                this.minorSpan = (int)span;
                this.checkRequests(axis);
                this.layoutMinorAxis(this.minorSpan, axis, this.minorOffsets, this.minorSpans);
                this.minorAllocValid = true;
                this.updateChildSizes();
            }
        }
    }

    void updateChildSizes() {
        int n = this.getViewCount();
        if (this.majorAxis == 0) {
            for (int i = 0; i < n; ++i) {
                View v = this.getView(i);
                v.setSize(this.majorSpans[i], this.minorSpans[i]);
            }
        } else {
            for (int i = 0; i < n; ++i) {
                View v = this.getView(i);
                v.setSize(this.minorSpans[i], this.majorSpans[i]);
            }
        }
    }

    float getSpanOnAxis(int axis) {
        if (axis == this.majorAxis) {
            return this.majorSpan;
        }
        return this.minorSpan;
    }

    @Override
    public void setSize(float width, float height) {
        this.layout(Math.max(0, (int)(width - (float)this.getLeftInset() - (float)this.getRightInset())), Math.max(0, (int)(height - (float)this.getTopInset() - (float)this.getBottomInset())));
    }

    @Override
    public void paint(Graphics g, Shape allocation) {
        Rectangle alloc = allocation instanceof Rectangle ? (Rectangle)allocation : allocation.getBounds();
        int n = this.getViewCount();
        int x = alloc.x + this.getLeftInset();
        int y = alloc.y + this.getTopInset();
        Rectangle clip = g.getClipBounds();
        for (int i = 0; i < n; ++i) {
            this.tempRect.x = x + this.getOffset(0, i);
            this.tempRect.y = y + this.getOffset(1, i);
            this.tempRect.width = this.getSpan(0, i);
            this.tempRect.height = this.getSpan(1, i);
            int trx0 = this.tempRect.x;
            int trx1 = trx0 + this.tempRect.width;
            int try0 = this.tempRect.y;
            int try1 = try0 + this.tempRect.height;
            int crx0 = clip.x;
            int crx1 = crx0 + clip.width;
            int cry0 = clip.y;
            int cry1 = cry0 + clip.height;
            if (trx1 < crx0 || try1 < cry0 || crx1 < trx0 || cry1 < try0) continue;
            this.paintChild(g, this.tempRect, i);
        }
    }

    @Override
    public Shape getChildAllocation(int index, Shape a) {
        if (a != null) {
            Shape ca = super.getChildAllocation(index, a);
            if (ca != null && !this.isAllocationValid()) {
                Rectangle r;
                Rectangle rectangle = r = ca instanceof Rectangle ? (Rectangle)ca : ca.getBounds();
                if (r.width == 0 && r.height == 0) {
                    return null;
                }
            }
            return ca;
        }
        return null;
    }

    @Override
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        if (!this.isAllocationValid()) {
            Rectangle alloc = a.getBounds();
            this.setSize(alloc.width, alloc.height);
        }
        return super.modelToView(pos, a, b);
    }

    @Override
    public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
        if (!this.isAllocationValid()) {
            Rectangle alloc = a.getBounds();
            this.setSize(alloc.width, alloc.height);
        }
        return super.viewToModel(x, y, a, bias);
    }

    @Override
    public float getAlignment(int axis) {
        this.checkRequests(axis);
        if (axis == this.majorAxis) {
            return this.majorRequest.alignment;
        }
        return this.minorRequest.alignment;
    }

    @Override
    public float getPreferredSpan(int axis) {
        float marginSpan;
        this.checkRequests(axis);
        float f = marginSpan = axis == 0 ? (float)(this.getLeftInset() + this.getRightInset()) : (float)(this.getTopInset() + this.getBottomInset());
        if (axis == this.majorAxis) {
            return (float)this.majorRequest.preferred + marginSpan;
        }
        return (float)this.minorRequest.preferred + marginSpan;
    }

    @Override
    public float getMinimumSpan(int axis) {
        float marginSpan;
        this.checkRequests(axis);
        float f = marginSpan = axis == 0 ? (float)(this.getLeftInset() + this.getRightInset()) : (float)(this.getTopInset() + this.getBottomInset());
        if (axis == this.majorAxis) {
            return (float)this.majorRequest.minimum + marginSpan;
        }
        return (float)this.minorRequest.minimum + marginSpan;
    }

    @Override
    public float getMaximumSpan(int axis) {
        float marginSpan;
        this.checkRequests(axis);
        float f = marginSpan = axis == 0 ? (float)(this.getLeftInset() + this.getRightInset()) : (float)(this.getTopInset() + this.getBottomInset());
        if (axis == this.majorAxis) {
            return (float)this.majorRequest.maximum + marginSpan;
        }
        return (float)this.minorRequest.maximum + marginSpan;
    }

    protected boolean isAllocationValid() {
        return this.majorAllocValid && this.minorAllocValid;
    }

    @Override
    protected boolean isBefore(int x, int y, Rectangle innerAlloc) {
        if (this.majorAxis == 0) {
            return x < innerAlloc.x;
        }
        return y < innerAlloc.y;
    }

    @Override
    protected boolean isAfter(int x, int y, Rectangle innerAlloc) {
        if (this.majorAxis == 0) {
            return x > innerAlloc.width + innerAlloc.x;
        }
        return y > innerAlloc.height + innerAlloc.y;
    }

    @Override
    protected View getViewAtPoint(int x, int y, Rectangle alloc) {
        int n = this.getViewCount();
        if (this.majorAxis == 0) {
            if (x < alloc.x + this.majorOffsets[0]) {
                this.childAllocation(0, alloc);
                return this.getView(0);
            }
            for (int i = 0; i < n; ++i) {
                if (x >= alloc.x + this.majorOffsets[i]) continue;
                this.childAllocation(i - 1, alloc);
                return this.getView(i - 1);
            }
            this.childAllocation(n - 1, alloc);
            return this.getView(n - 1);
        }
        if (y < alloc.y + this.majorOffsets[0]) {
            this.childAllocation(0, alloc);
            return this.getView(0);
        }
        for (int i = 0; i < n; ++i) {
            if (y >= alloc.y + this.majorOffsets[i]) continue;
            this.childAllocation(i - 1, alloc);
            return this.getView(i - 1);
        }
        this.childAllocation(n - 1, alloc);
        return this.getView(n - 1);
    }

    @Override
    protected void childAllocation(int index, Rectangle alloc) {
        alloc.x += this.getOffset(0, index);
        alloc.y += this.getOffset(1, index);
        alloc.width = this.getSpan(0, index);
        alloc.height = this.getSpan(1, index);
    }

    protected void layout(int width, int height) {
        this.setSpanOnAxis(0, width);
        this.setSpanOnAxis(1, height);
    }

    public int getWidth() {
        int span = this.majorAxis == 0 ? this.majorSpan : this.minorSpan;
        return span += this.getLeftInset() - this.getRightInset();
    }

    public int getHeight() {
        int span = this.majorAxis == 1 ? this.majorSpan : this.minorSpan;
        return span += this.getTopInset() - this.getBottomInset();
    }

    protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
        long preferred = 0L;
        int n = this.getViewCount();
        for (int i = 0; i < n; ++i) {
            View v = this.getView(i);
            spans[i] = (int)v.getPreferredSpan(axis);
            preferred += (long)spans[i];
        }
        long desiredAdjustment = (long)targetSpan - preferred;
        float adjustmentFactor = 0.0f;
        int[] diffs = null;
        if (desiredAdjustment != 0L) {
            long totalSpan = 0L;
            diffs = new int[n];
            for (int i = 0; i < n; ++i) {
                int tmp;
                View v = this.getView(i);
                if (desiredAdjustment < 0L) {
                    tmp = (int)v.getMinimumSpan(axis);
                    diffs[i] = spans[i] - tmp;
                } else {
                    tmp = (int)v.getMaximumSpan(axis);
                    diffs[i] = tmp - spans[i];
                }
                totalSpan += (long)tmp;
            }
            float maximumAdjustment = Math.abs(totalSpan - preferred);
            adjustmentFactor = (float)desiredAdjustment / maximumAdjustment;
            adjustmentFactor = Math.min(adjustmentFactor, 1.0f);
            adjustmentFactor = Math.max(adjustmentFactor, -1.0f);
        }
        int totalOffset = 0;
        for (int i = 0; i < n; ++i) {
            offsets[i] = totalOffset;
            if (desiredAdjustment != 0L) {
                float adjF = adjustmentFactor * (float)diffs[i];
                int n2 = i;
                spans[n2] = spans[n2] + Math.round(adjF);
            }
            totalOffset = (int)Math.min((long)totalOffset + (long)spans[i], Integer.MAX_VALUE);
        }
    }

    protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
        int n = this.getViewCount();
        for (int i = 0; i < n; ++i) {
            View v = this.getView(i);
            int max = (int)v.getMaximumSpan(axis);
            if (max < targetSpan) {
                float align = v.getAlignment(axis);
                offsets[i] = (int)((float)(targetSpan - max) * align);
                spans[i] = max;
                continue;
            }
            int min = (int)v.getMinimumSpan(axis);
            offsets[i] = 0;
            spans[i] = Math.max(min, targetSpan);
        }
    }

    protected SizeRequirements calculateMajorAxisRequirements(int axis, SizeRequirements r) {
        float min = 0.0f;
        float pref = 0.0f;
        float max = 0.0f;
        int n = this.getViewCount();
        for (int i = 0; i < n; ++i) {
            View v = this.getView(i);
            min += v.getMinimumSpan(axis);
            pref += v.getPreferredSpan(axis);
            max += v.getMaximumSpan(axis);
        }
        if (r == null) {
            r = new SizeRequirements();
        }
        r.alignment = 0.5f;
        r.minimum = (int)min;
        r.preferred = (int)pref;
        r.maximum = (int)max;
        return r;
    }

    protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
        int min = 0;
        long pref = 0L;
        int max = Integer.MAX_VALUE;
        int n = this.getViewCount();
        for (int i = 0; i < n; ++i) {
            View v = this.getView(i);
            min = Math.max((int)v.getMinimumSpan(axis), min);
            pref = Math.max((long)((int)v.getPreferredSpan(axis)), pref);
            max = Math.max((int)v.getMaximumSpan(axis), max);
        }
        if (r == null) {
            r = new SizeRequirements();
            r.alignment = 0.5f;
        }
        r.preferred = (int)pref;
        r.minimum = min;
        r.maximum = max;
        return r;
    }

    void checkRequests(int axis) {
        if (axis != 0 && axis != 1) {
            throw new IllegalArgumentException("Invalid axis: " + axis);
        }
        if (axis == this.majorAxis) {
            if (!this.majorReqValid) {
                this.majorRequest = this.calculateMajorAxisRequirements(axis, this.majorRequest);
                this.majorReqValid = true;
            }
        } else if (!this.minorReqValid) {
            this.minorRequest = this.calculateMinorAxisRequirements(axis, this.minorRequest);
            this.minorReqValid = true;
        }
    }

    protected void baselineLayout(int targetSpan, int axis, int[] offsets, int[] spans) {
        int totalAscent = (int)((float)targetSpan * this.getAlignment(axis));
        int totalDescent = targetSpan - totalAscent;
        int n = this.getViewCount();
        for (int i = 0; i < n; ++i) {
            float viewSpan;
            View v = this.getView(i);
            float align = v.getAlignment(axis);
            if (v.getResizeWeight(axis) > 0) {
                float minSpan = v.getMinimumSpan(axis);
                float maxSpan = v.getMaximumSpan(axis);
                if (align == 0.0f) {
                    viewSpan = Math.max(Math.min(maxSpan, (float)totalDescent), minSpan);
                } else if (align == 1.0f) {
                    viewSpan = Math.max(Math.min(maxSpan, (float)totalAscent), minSpan);
                } else {
                    float fitSpan = Math.min((float)totalAscent / align, (float)totalDescent / (1.0f - align));
                    viewSpan = Math.max(Math.min(maxSpan, fitSpan), minSpan);
                }
            } else {
                viewSpan = v.getPreferredSpan(axis);
            }
            offsets[i] = totalAscent - (int)(viewSpan * align);
            spans[i] = (int)viewSpan;
        }
    }

    protected SizeRequirements baselineRequirements(int axis, SizeRequirements r) {
        SizeRequirements totalAscent = new SizeRequirements();
        SizeRequirements totalDescent = new SizeRequirements();
        if (r == null) {
            r = new SizeRequirements();
        }
        r.alignment = 0.5f;
        int n = this.getViewCount();
        for (int i = 0; i < n; ++i) {
            View v = this.getView(i);
            float align = v.getAlignment(axis);
            float span = v.getPreferredSpan(axis);
            int ascent = (int)(align * span);
            int descent = (int)(span - (float)ascent);
            totalAscent.preferred = Math.max(ascent, totalAscent.preferred);
            totalDescent.preferred = Math.max(descent, totalDescent.preferred);
            if (v.getResizeWeight(axis) > 0) {
                span = v.getMinimumSpan(axis);
                ascent = (int)(align * span);
                descent = (int)(span - (float)ascent);
                totalAscent.minimum = Math.max(ascent, totalAscent.minimum);
                totalDescent.minimum = Math.max(descent, totalDescent.minimum);
                span = v.getMaximumSpan(axis);
                ascent = (int)(align * span);
                descent = (int)(span - (float)ascent);
                totalAscent.maximum = Math.max(ascent, totalAscent.maximum);
                totalDescent.maximum = Math.max(descent, totalDescent.maximum);
                continue;
            }
            totalAscent.minimum = Math.max(ascent, totalAscent.minimum);
            totalDescent.minimum = Math.max(descent, totalDescent.minimum);
            totalAscent.maximum = Math.max(ascent, totalAscent.maximum);
            totalDescent.maximum = Math.max(descent, totalDescent.maximum);
        }
        r.preferred = (int)Math.min((long)totalAscent.preferred + (long)totalDescent.preferred, Integer.MAX_VALUE);
        if (r.preferred > 0) {
            r.alignment = (float)totalAscent.preferred / (float)r.preferred;
        }
        if (r.alignment == 0.0f) {
            r.minimum = totalDescent.minimum;
            r.maximum = totalDescent.maximum;
        } else if (r.alignment == 1.0f) {
            r.minimum = totalAscent.minimum;
            r.maximum = totalAscent.maximum;
        } else {
            r.minimum = Math.round(Math.max((float)totalAscent.minimum / r.alignment, (float)totalDescent.minimum / (1.0f - r.alignment)));
            r.maximum = Math.round(Math.min((float)totalAscent.maximum / r.alignment, (float)totalDescent.maximum / (1.0f - r.alignment)));
        }
        return r;
    }

    protected int getOffset(int axis, int childIndex) {
        int[] offsets = axis == this.majorAxis ? this.majorOffsets : this.minorOffsets;
        return offsets[childIndex];
    }

    protected int getSpan(int axis, int childIndex) {
        int[] spans = axis == this.majorAxis ? this.majorSpans : this.minorSpans;
        return spans[childIndex];
    }

    @Override
    protected boolean flipEastAndWestAtEnds(int position, Position.Bias bias) {
        View v;
        int testPos;
        int index;
        if (this.majorAxis == 1 && (index = this.getViewIndexAtPosition(testPos = bias == Position.Bias.Backward ? Math.max(0, position - 1) : position)) != -1 && (v = this.getView(index)) instanceof CompositeView) {
            CompositeView compositeView = (CompositeView)v;
            return compositeView.flipEastAndWestAtEnds(position, bias);
        }
        return false;
    }
}

