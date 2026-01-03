/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.SizeRequirements;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import javax.swing.text.html.BlockView;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

public class ParagraphView
extends javax.swing.text.ParagraphView {
    private AttributeSet attr;
    private StyleSheet.BoxPainter painter;
    private CSS.LengthValue cssWidth;
    private CSS.LengthValue cssHeight;

    public ParagraphView(Element elem) {
        super(elem);
    }

    @Override
    public void setParent(View parent) {
        super.setParent(parent);
        if (parent != null) {
            this.setPropertiesFromAttributes();
        }
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
    protected void setPropertiesFromAttributes() {
        StyleSheet sheet = this.getStyleSheet();
        this.attr = sheet.getViewAttributes(this);
        this.painter = sheet.getBoxPainter(this.attr);
        if (this.attr != null) {
            super.setPropertiesFromAttributes();
            this.setInsets((short)this.painter.getInset(1, this), (short)this.painter.getInset(2, this), (short)this.painter.getInset(3, this), (short)this.painter.getInset(4, this));
            Object o = this.attr.getAttribute(CSS.Attribute.TEXT_ALIGN);
            if (o != null) {
                String ta = o.toString();
                if (ta.equals("left")) {
                    this.setJustification(0);
                } else if (ta.equals("center")) {
                    this.setJustification(1);
                } else if (ta.equals("right")) {
                    this.setJustification(2);
                } else if (ta.equals("justify")) {
                    this.setJustification(3);
                }
            }
            this.cssWidth = (CSS.LengthValue)this.attr.getAttribute(CSS.Attribute.WIDTH);
            this.cssHeight = (CSS.LengthValue)this.attr.getAttribute(CSS.Attribute.HEIGHT);
        }
    }

    protected StyleSheet getStyleSheet() {
        HTMLDocument doc = (HTMLDocument)this.getDocument();
        return doc.getStyleSheet();
    }

    @Override
    protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
        if (BlockView.spanSetFromAttributes(axis, r = super.calculateMinorAxisRequirements(axis, r), this.cssWidth, this.cssHeight)) {
            int margin = axis == 0 ? this.getLeftInset() + this.getRightInset() : this.getTopInset() + this.getBottomInset();
            r.minimum -= margin;
            r.preferred -= margin;
            r.maximum -= margin;
        }
        return r;
    }

    @Override
    public boolean isVisible() {
        View v;
        int n = this.getLayoutViewCount() - 1;
        for (int i = 0; i < n; ++i) {
            View v2 = this.getLayoutView(i);
            if (!v2.isVisible()) continue;
            return true;
        }
        if (n > 0 && (v = this.getLayoutView(n)).getEndOffset() - v.getStartOffset() == 1) {
            return false;
        }
        if (this.getStartOffset() == this.getDocument().getLength()) {
            boolean editable = false;
            Container c = this.getContainer();
            if (c instanceof JTextComponent) {
                editable = ((JTextComponent)c).isEditable();
            }
            if (!editable) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void paint(Graphics g, Shape a) {
        if (a == null) {
            return;
        }
        Rectangle r = a instanceof Rectangle ? (Rectangle)a : a.getBounds();
        this.painter.paint(g, r.x, r.y, r.width, r.height, this);
        super.paint(g, a);
    }

    @Override
    public float getPreferredSpan(int axis) {
        if (!this.isVisible()) {
            return 0.0f;
        }
        return super.getPreferredSpan(axis);
    }

    @Override
    public float getMinimumSpan(int axis) {
        if (!this.isVisible()) {
            return 0.0f;
        }
        return super.getMinimumSpan(axis);
    }

    @Override
    public float getMaximumSpan(int axis) {
        if (!this.isVisible()) {
            return 0.0f;
        }
        return super.getMaximumSpan(axis);
    }
}

