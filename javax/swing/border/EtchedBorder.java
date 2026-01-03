/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.border;

import com.sun.java.swing.SwingUtilities3;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.beans.ConstructorProperties;
import javax.swing.border.AbstractBorder;

public class EtchedBorder
extends AbstractBorder {
    public static final int RAISED = 0;
    public static final int LOWERED = 1;
    protected int etchType;
    protected Color highlight;
    protected Color shadow;

    public EtchedBorder() {
        this(1);
    }

    public EtchedBorder(int etchType) {
        this(etchType, null, null);
    }

    public EtchedBorder(Color highlight, Color shadow) {
        this(1, highlight, shadow);
    }

    @ConstructorProperties(value={"etchType", "highlightColor", "shadowColor"})
    public EtchedBorder(int etchType, Color highlight, Color shadow) {
        this.etchType = etchType;
        this.highlight = highlight;
        this.shadow = shadow;
    }

    private void paintBorderHighlight(Graphics g, Color c, int w, int h, int stkWidth) {
        g.setColor(c);
        g.drawRect(stkWidth / 2, stkWidth / 2, w - 2 * stkWidth, h - 2 * stkWidth);
    }

    private void paintBorderShadow(Graphics g, Color c, int w, int h, int stkWidth) {
        g.setColor(c);
        g.drawLine(3 * stkWidth / 2, h - 3 * stkWidth / 2, 3 * stkWidth / 2, 3 * stkWidth / 2);
        g.drawLine(3 * stkWidth / 2, 3 * stkWidth / 2, w - 3 * stkWidth / 2, 3 * stkWidth / 2);
        g.drawLine(stkWidth / 2, h - (stkWidth - stkWidth / 2), w - (stkWidth - stkWidth / 2), h - (stkWidth - stkWidth / 2));
        g.drawLine(w - (stkWidth - stkWidth / 2), h - (stkWidth - stkWidth / 2), w - (stkWidth - stkWidth / 2), stkWidth / 2);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        SwingUtilities3.paintBorder(c, g, x, y, width, height, this::paintUnscaledBorder);
    }

    private void paintUnscaledBorder(Component c, Graphics g, int w, int h, double scaleFactor) {
        int stkWidth = (int)Math.floor(scaleFactor);
        if (g instanceof Graphics2D) {
            ((Graphics2D)g).setStroke(new BasicStroke(stkWidth));
        }
        this.paintBorderShadow(g, this.etchType == 1 ? this.getHighlightColor(c) : this.getShadowColor(c), w, h, stkWidth);
        this.paintBorderHighlight(g, this.etchType == 1 ? this.getShadowColor(c) : this.getHighlightColor(c), w, h, stkWidth);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(2, 2, 2, 2);
        return insets;
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }

    public int getEtchType() {
        return this.etchType;
    }

    public Color getHighlightColor(Component c) {
        return this.highlight != null ? this.highlight : c.getBackground().brighter();
    }

    public Color getHighlightColor() {
        return this.highlight;
    }

    public Color getShadowColor(Component c) {
        return this.shadow != null ? this.shadow : c.getBackground().darker();
    }

    public Color getShadowColor() {
        return this.shadow;
    }
}

