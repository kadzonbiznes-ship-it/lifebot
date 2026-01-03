/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.ui.components;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class RoundedPanel
extends JPanel {
    private final int borderRadius;

    public RoundedPanel(int borderRadius) {
        this.borderRadius = borderRadius;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(this.getBackground());
        g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), this.borderRadius, this.borderRadius);
        g2.dispose();
    }
}

