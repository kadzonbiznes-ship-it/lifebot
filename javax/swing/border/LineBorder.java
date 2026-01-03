/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.border;

import com.sun.java.swing.SwingUtilities3;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.beans.ConstructorProperties;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;

public class LineBorder
extends AbstractBorder {
    private static Border blackLine;
    private static Border grayLine;
    protected int thickness;
    protected Color lineColor;
    protected boolean roundedCorners;

    public static Border createBlackLineBorder() {
        if (blackLine == null) {
            blackLine = new LineBorder(Color.black, 1);
        }
        return blackLine;
    }

    public static Border createGrayLineBorder() {
        if (grayLine == null) {
            grayLine = new LineBorder(Color.gray, 1);
        }
        return grayLine;
    }

    public LineBorder(Color color) {
        this(color, 1, false);
    }

    public LineBorder(Color color, int thickness) {
        this(color, thickness, false);
    }

    @ConstructorProperties(value={"lineColor", "thickness", "roundedCorners"})
    public LineBorder(Color color, int thickness, boolean roundedCorners) {
        this.lineColor = color;
        this.thickness = thickness;
        this.roundedCorners = roundedCorners;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        SwingUtilities3.paintBorder(c, g, x, y, width, height, this::paintUnscaledBorder);
    }

    private void paintUnscaledBorder(Component c, Graphics g, int w, int h, double scaleFactor) {
        if (this.thickness > 0 && g instanceof Graphics2D) {
            RectangularShape inner;
            RectangularShape outer;
            Graphics2D g2d = (Graphics2D)g;
            Color oldColor = g2d.getColor();
            g2d.setColor(this.lineColor);
            int offs = this.thickness * (int)scaleFactor;
            int size = offs + offs;
            if (this.roundedCorners) {
                float arc = 0.2f * (float)offs;
                outer = new RoundRectangle2D.Float(0.0f, 0.0f, w, h, offs, offs);
                inner = new RoundRectangle2D.Float(offs, offs, w - size, h - size, arc, arc);
            } else {
                outer = new Rectangle2D.Float(0.0f, 0.0f, w, h);
                inner = new Rectangle2D.Float(offs, offs, w - size, h - size);
            }
            Path2D.Float path = new Path2D.Float(0);
            path.append(outer, false);
            path.append(inner, false);
            g2d.fill(path);
            g2d.setColor(oldColor);
        }
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(this.thickness, this.thickness, this.thickness, this.thickness);
        return insets;
    }

    public Color getLineColor() {
        return this.lineColor;
    }

    public int getThickness() {
        return this.thickness;
    }

    public boolean getRoundedCorners() {
        return this.roundedCorners;
    }

    @Override
    public boolean isBorderOpaque() {
        return !this.roundedCorners;
    }
}

