/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.Icon;
import javax.swing.border.EmptyBorder;

public class MatteBorder
extends EmptyBorder {
    protected Color color;
    protected Icon tileIcon;

    public MatteBorder(int top, int left, int bottom, int right, Color matteColor) {
        super(top, left, bottom, right);
        this.color = matteColor;
    }

    public MatteBorder(Insets borderInsets, Color matteColor) {
        super(borderInsets);
        this.color = matteColor;
    }

    public MatteBorder(int top, int left, int bottom, int right, Icon tileIcon) {
        super(top, left, bottom, right);
        this.tileIcon = tileIcon;
    }

    public MatteBorder(Insets borderInsets, Icon tileIcon) {
        super(borderInsets);
        this.tileIcon = tileIcon;
    }

    public MatteBorder(Icon tileIcon) {
        this(-1, -1, -1, -1, tileIcon);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Insets insets = this.getBorderInsets(c);
        Color oldColor = g.getColor();
        g.translate(x, y);
        if (this.tileIcon != null) {
            Color color = this.color = this.tileIcon.getIconWidth() == -1 ? Color.gray : null;
        }
        if (this.color != null) {
            g.setColor(this.color);
            g.fillRect(0, 0, width - insets.right, insets.top);
            g.fillRect(0, insets.top, insets.left, height - insets.top);
            g.fillRect(insets.left, height - insets.bottom, width - insets.left, insets.bottom);
            g.fillRect(width - insets.right, 0, insets.right, height - insets.bottom);
        } else if (this.tileIcon != null) {
            int tileW = this.tileIcon.getIconWidth();
            int tileH = this.tileIcon.getIconHeight();
            this.paintEdge(c, g, 0, 0, width - insets.right, insets.top, tileW, tileH);
            this.paintEdge(c, g, 0, insets.top, insets.left, height - insets.top, tileW, tileH);
            this.paintEdge(c, g, insets.left, height - insets.bottom, width - insets.left, insets.bottom, tileW, tileH);
            this.paintEdge(c, g, width - insets.right, 0, insets.right, height - insets.bottom, tileW, tileH);
        }
        g.translate(-x, -y);
        g.setColor(oldColor);
    }

    private void paintEdge(Component c, Graphics g, int x, int y, int width, int height, int tileW, int tileH) {
        g = g.create(x, y, width, height);
        int sY = -(y % tileH);
        for (x = -(x % tileW); x < width; x += tileW) {
            for (y = sY; y < height; y += tileH) {
                this.tileIcon.paintIcon(c, g, x, y);
            }
        }
        g.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        return this.computeInsets(insets);
    }

    @Override
    public Insets getBorderInsets() {
        return this.computeInsets(new Insets(0, 0, 0, 0));
    }

    private Insets computeInsets(Insets insets) {
        if (this.tileIcon != null && this.top == -1 && this.bottom == -1 && this.left == -1 && this.right == -1) {
            int h;
            int w = this.tileIcon.getIconWidth();
            insets.top = h = this.tileIcon.getIconHeight();
            insets.right = w;
            insets.bottom = h;
            insets.left = w;
        } else {
            insets.left = this.left;
            insets.top = this.top;
            insets.right = this.right;
            insets.bottom = this.bottom;
        }
        return insets;
    }

    public Color getMatteColor() {
        return this.color;
    }

    public Icon getTileIcon() {
        return this.tileIcon;
    }

    @Override
    public boolean isBorderOpaque() {
        return this.color != null;
    }
}

