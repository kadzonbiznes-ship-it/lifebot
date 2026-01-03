/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.border;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.beans.ConstructorProperties;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;

public class CompoundBorder
extends AbstractBorder {
    protected Border outsideBorder;
    protected Border insideBorder;

    public CompoundBorder() {
        this.outsideBorder = null;
        this.insideBorder = null;
    }

    @ConstructorProperties(value={"outsideBorder", "insideBorder"})
    public CompoundBorder(Border outsideBorder, Border insideBorder) {
        this.outsideBorder = outsideBorder;
        this.insideBorder = insideBorder;
    }

    @Override
    public boolean isBorderOpaque() {
        return !(this.outsideBorder != null && !this.outsideBorder.isBorderOpaque() || this.insideBorder != null && !this.insideBorder.isBorderOpaque());
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int px = x;
        int py = y;
        int pw = width;
        int ph = height;
        if (this.outsideBorder != null) {
            this.outsideBorder.paintBorder(c, g, px, py, pw, ph);
            Insets nextInsets = this.outsideBorder.getBorderInsets(c);
            px += nextInsets.left;
            py += nextInsets.top;
            pw = pw - nextInsets.right - nextInsets.left;
            ph = ph - nextInsets.bottom - nextInsets.top;
        }
        if (this.insideBorder != null) {
            this.insideBorder.paintBorder(c, g, px, py, pw, ph);
        }
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        Insets nextInsets;
        insets.bottom = 0;
        insets.right = 0;
        insets.left = 0;
        insets.top = 0;
        if (this.outsideBorder != null) {
            nextInsets = this.outsideBorder.getBorderInsets(c);
            insets.top += nextInsets.top;
            insets.left += nextInsets.left;
            insets.right += nextInsets.right;
            insets.bottom += nextInsets.bottom;
        }
        if (this.insideBorder != null) {
            nextInsets = this.insideBorder.getBorderInsets(c);
            insets.top += nextInsets.top;
            insets.left += nextInsets.left;
            insets.right += nextInsets.right;
            insets.bottom += nextInsets.bottom;
        }
        return insets;
    }

    public Border getOutsideBorder() {
        return this.outsideBorder;
    }

    public Border getInsideBorder() {
        return this.insideBorder;
    }
}

