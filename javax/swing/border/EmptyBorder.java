/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.border;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.beans.ConstructorProperties;
import java.io.Serializable;
import javax.swing.border.AbstractBorder;

public class EmptyBorder
extends AbstractBorder
implements Serializable {
    protected int left;
    protected int right;
    protected int top;
    protected int bottom;

    public EmptyBorder(int top, int left, int bottom, int right) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    @ConstructorProperties(value={"borderInsets"})
    public EmptyBorder(Insets borderInsets) {
        this.top = borderInsets.top;
        this.right = borderInsets.right;
        this.bottom = borderInsets.bottom;
        this.left = borderInsets.left;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = this.left;
        insets.top = this.top;
        insets.right = this.right;
        insets.bottom = this.bottom;
        return insets;
    }

    public Insets getBorderInsets() {
        return new Insets(this.top, this.left, this.bottom, this.right);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}

