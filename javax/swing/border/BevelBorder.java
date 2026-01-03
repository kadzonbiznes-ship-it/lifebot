/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.beans.ConstructorProperties;
import javax.swing.border.AbstractBorder;

public class BevelBorder
extends AbstractBorder {
    public static final int RAISED = 0;
    public static final int LOWERED = 1;
    protected int bevelType;
    protected Color highlightOuter;
    protected Color highlightInner;
    protected Color shadowInner;
    protected Color shadowOuter;

    public BevelBorder(int bevelType) {
        this.bevelType = bevelType;
    }

    public BevelBorder(int bevelType, Color highlight, Color shadow) {
        this(bevelType, highlight.brighter(), highlight, shadow, shadow.brighter());
    }

    @ConstructorProperties(value={"bevelType", "highlightOuterColor", "highlightInnerColor", "shadowOuterColor", "shadowInnerColor"})
    public BevelBorder(int bevelType, Color highlightOuterColor, Color highlightInnerColor, Color shadowOuterColor, Color shadowInnerColor) {
        this(bevelType);
        this.highlightOuter = highlightOuterColor;
        this.highlightInner = highlightInnerColor;
        this.shadowOuter = shadowOuterColor;
        this.shadowInner = shadowInnerColor;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (this.bevelType == 0) {
            this.paintRaisedBevel(c, g, x, y, width, height);
        } else if (this.bevelType == 1) {
            this.paintLoweredBevel(c, g, x, y, width, height);
        }
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(2, 2, 2, 2);
        return insets;
    }

    public Color getHighlightOuterColor(Component c) {
        Color highlight = this.getHighlightOuterColor();
        return highlight != null ? highlight : c.getBackground().brighter().brighter();
    }

    public Color getHighlightInnerColor(Component c) {
        Color highlight = this.getHighlightInnerColor();
        return highlight != null ? highlight : c.getBackground().brighter();
    }

    public Color getShadowInnerColor(Component c) {
        Color shadow = this.getShadowInnerColor();
        return shadow != null ? shadow : c.getBackground().darker();
    }

    public Color getShadowOuterColor(Component c) {
        Color shadow = this.getShadowOuterColor();
        return shadow != null ? shadow : c.getBackground().darker().darker();
    }

    public Color getHighlightOuterColor() {
        return this.highlightOuter;
    }

    public Color getHighlightInnerColor() {
        return this.highlightInner;
    }

    public Color getShadowInnerColor() {
        return this.shadowInner;
    }

    public Color getShadowOuterColor() {
        return this.shadowOuter;
    }

    public int getBevelType() {
        return this.bevelType;
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }

    protected void paintRaisedBevel(Component c, Graphics g, int x, int y, int width, int height) {
        Color oldColor = g.getColor();
        int h = height;
        int w = width;
        g.translate(x, y);
        g.setColor(this.getHighlightOuterColor(c));
        g.drawLine(0, 0, 0, h - 2);
        g.drawLine(1, 0, w - 2, 0);
        g.setColor(this.getHighlightInnerColor(c));
        g.drawLine(1, 1, 1, h - 3);
        g.drawLine(2, 1, w - 3, 1);
        g.setColor(this.getShadowOuterColor(c));
        g.drawLine(0, h - 1, w - 1, h - 1);
        g.drawLine(w - 1, 0, w - 1, h - 2);
        g.setColor(this.getShadowInnerColor(c));
        g.drawLine(1, h - 2, w - 2, h - 2);
        g.drawLine(w - 2, 1, w - 2, h - 3);
        g.translate(-x, -y);
        g.setColor(oldColor);
    }

    protected void paintLoweredBevel(Component c, Graphics g, int x, int y, int width, int height) {
        Color oldColor = g.getColor();
        int h = height;
        int w = width;
        g.translate(x, y);
        g.setColor(this.getShadowInnerColor(c));
        g.drawLine(0, 0, 0, h - 1);
        g.drawLine(1, 0, w - 1, 0);
        g.setColor(this.getShadowOuterColor(c));
        g.drawLine(1, 1, 1, h - 2);
        g.drawLine(2, 1, w - 2, 1);
        g.setColor(this.getHighlightOuterColor(c));
        g.drawLine(1, h - 1, w - 1, h - 1);
        g.drawLine(w - 1, 1, w - 1, h - 2);
        g.setColor(this.getHighlightInnerColor(c));
        g.drawLine(2, h - 2, w - 2, h - 2);
        g.drawLine(w - 2, 2, w - 2, h - 3);
        g.translate(-x, -y);
        g.setColor(oldColor);
    }
}

