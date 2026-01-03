/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.SizeRequirements;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

public class BlockView
extends BoxView {
    private AttributeSet attr;
    private StyleSheet.BoxPainter painter;
    private CSS.LengthValue cssWidth;
    private CSS.LengthValue cssHeight;

    public BlockView(Element elem, int axis) {
        super(elem, axis);
    }

    @Override
    public void setParent(View parent) {
        super.setParent(parent);
        if (parent != null) {
            this.setPropertiesFromAttributes();
        }
    }

    @Override
    protected SizeRequirements calculateMajorAxisRequirements(int axis, SizeRequirements r) {
        if (r == null) {
            r = new SizeRequirements();
        }
        if (!BlockView.spanSetFromAttributes(axis, r, this.cssWidth, this.cssHeight)) {
            r = super.calculateMajorAxisRequirements(axis, r);
        } else {
            SizeRequirements parentR = super.calculateMajorAxisRequirements(axis, null);
            int margin = axis == 0 ? this.getLeftInset() + this.getRightInset() : this.getTopInset() + this.getBottomInset();
            r.minimum -= margin;
            r.preferred -= margin;
            r.maximum -= margin;
            this.constrainSize(axis, r, parentR);
        }
        return r;
    }

    @Override
    protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
        Object o;
        if (r == null) {
            r = new SizeRequirements();
        }
        if (!BlockView.spanSetFromAttributes(axis, r, this.cssWidth, this.cssHeight)) {
            r = super.calculateMinorAxisRequirements(axis, r);
        } else {
            SizeRequirements parentR = super.calculateMinorAxisRequirements(axis, null);
            int margin = axis == 0 ? this.getLeftInset() + this.getRightInset() : this.getTopInset() + this.getBottomInset();
            r.minimum -= margin;
            r.preferred -= margin;
            r.maximum -= margin;
            this.constrainSize(axis, r, parentR);
        }
        if (axis == 0 && (o = this.getAttributes().getAttribute(CSS.Attribute.TEXT_ALIGN)) != null) {
            String align = o.toString();
            r.alignment = align.equals("center") ? 0.5f : (align.equals("right") ? 1.0f : 0.0f);
        }
        return r;
    }

    boolean isPercentage(int axis, AttributeSet a) {
        if (axis == 0) {
            if (this.cssWidth != null) {
                return this.cssWidth.isPercentage();
            }
        } else if (this.cssHeight != null) {
            return this.cssHeight.isPercentage();
        }
        return false;
    }

    static boolean spanSetFromAttributes(int axis, SizeRequirements r, CSS.LengthValue cssWidth, CSS.LengthValue cssHeight) {
        if (axis == 0) {
            if (cssWidth != null && !cssWidth.isPercentage()) {
                r.preferred = r.maximum = (int)cssWidth.getValue();
                r.minimum = r.maximum;
                return true;
            }
        } else if (cssHeight != null && !cssHeight.isPercentage()) {
            r.preferred = r.maximum = (int)cssHeight.getValue();
            r.minimum = r.maximum;
            return true;
        }
        return false;
    }

    @Override
    protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
        int n = this.getViewCount();
        CSS.Attribute key = axis == 0 ? CSS.Attribute.WIDTH : CSS.Attribute.HEIGHT;
        for (int i = 0; i < n; ++i) {
            View v = this.getView(i);
            int min = (int)v.getMinimumSpan(axis);
            AttributeSet a = v.getAttributes();
            CSS.LengthValue lv = (CSS.LengthValue)a.getAttribute(key);
            int max = lv != null && lv.isPercentage() ? (min = Math.max((int)lv.getValue(targetSpan), min)) : (int)v.getMaximumSpan(axis);
            if (max < targetSpan) {
                float align = v.getAlignment(axis);
                offsets[i] = (int)((float)(targetSpan - max) * align);
                spans[i] = max;
                continue;
            }
            offsets[i] = 0;
            spans[i] = Math.max(min, targetSpan);
        }
    }

    @Override
    public void paint(Graphics g, Shape allocation) {
        Rectangle a = (Rectangle)allocation;
        this.painter.paint(g, a.x, a.y, a.width, a.height, this);
        super.paint(g, a);
    }

    @Override
    public AttributeSet getAttributes() {
        if (this.attr == null) {
            StyleSheet sheet = this.getStyleSheet();
            this.attr = sheet.getViewAttributes(this);
        }
        return this.attr;
    }

    @Override
    public int getResizeWeight(int axis) {
        switch (axis) {
            case 0: {
                return 1;
            }
            case 1: {
                return 0;
            }
        }
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }

    @Override
    public float getAlignment(int axis) {
        switch (axis) {
            case 0: {
                return 0.0f;
            }
            case 1: {
                if (this.getViewCount() == 0) {
                    return 0.0f;
                }
                float span = this.getPreferredSpan(1);
                View v = this.getView(0);
                float above = v.getPreferredSpan(1);
                float a = (int)span != 0 ? above * v.getAlignment(1) / span : 0.0f;
                return a;
            }
        }
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }

    @Override
    public void changedUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
        super.changedUpdate(changes, a, f);
        int pos = changes.getOffset();
        if (pos <= this.getStartOffset() && pos + changes.getLength() >= this.getEndOffset()) {
            this.setPropertiesFromAttributes();
        }
    }

    @Override
    public float getPreferredSpan(int axis) {
        return super.getPreferredSpan(axis);
    }

    @Override
    public float getMinimumSpan(int axis) {
        return super.getMinimumSpan(axis);
    }

    @Override
    public float getMaximumSpan(int axis) {
        return super.getMaximumSpan(axis);
    }

    protected void setPropertiesFromAttributes() {
        StyleSheet sheet = this.getStyleSheet();
        this.attr = sheet.getViewAttributes(this);
        this.painter = sheet.getBoxPainter(this.attr);
        if (this.attr != null) {
            this.setInsets((short)this.painter.getInset(1, this), (short)this.painter.getInset(2, this), (short)this.painter.getInset(3, this), (short)this.painter.getInset(4, this));
        }
        this.cssWidth = (CSS.LengthValue)this.attr.getAttribute(CSS.Attribute.WIDTH);
        this.cssHeight = (CSS.LengthValue)this.attr.getAttribute(CSS.Attribute.HEIGHT);
    }

    protected StyleSheet getStyleSheet() {
        HTMLDocument doc = (HTMLDocument)this.getDocument();
        return doc.getStyleSheet();
    }

    private void constrainSize(int axis, SizeRequirements want, SizeRequirements min) {
        if (min.minimum > want.minimum) {
            want.minimum = want.preferred = min.minimum;
            want.maximum = Math.max(want.maximum, min.maximum);
        }
    }
}

