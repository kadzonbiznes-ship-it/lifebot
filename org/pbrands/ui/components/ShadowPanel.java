/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;
import javax.swing.JPanel;

public class ShadowPanel
extends JPanel {
    private final int cornerRadius;
    private final int shadowSize;

    public ShadowPanel(int width, int height, int cornerRadius, int shadowSize) {
        this.cornerRadius = cornerRadius;
        this.shadowSize = shadowSize;
        this.setPreferredSize(new Dimension(width + 2 * shadowSize, height + 2 * shadowSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = this.getWidth() - this.shadowSize;
        int height = this.getHeight() - this.shadowSize;
        int x = this.shadowSize;
        int y = this.shadowSize;
        BufferedImage shadowImage = this.createShadowImage(width, height, this.cornerRadius, this.shadowSize);
        g2.drawImage((Image)shadowImage, 0, 0, this);
        g2.setColor(this.getBackground());
        g2.fillRoundRect(x, y, width, height, this.cornerRadius, this.cornerRadius);
        g2.dispose();
    }

    private BufferedImage createShadowImage(int width, int height, int cornerRadius, int shadowSize) {
        BufferedImage shadowImage = new BufferedImage(width + 2 * shadowSize, height + 2 * shadowSize, 2);
        Graphics2D g2 = shadowImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, 64));
        g2.fillRoundRect(shadowSize, shadowSize, width, height, cornerRadius, cornerRadius);
        float[] kernel = new float[shadowSize * shadowSize];
        Arrays.fill(kernel, 1.0f / (float)(shadowSize * shadowSize));
        ConvolveOp shadowOp = new ConvolveOp(new Kernel(shadowSize, shadowSize, kernel));
        shadowImage = shadowOp.filter(shadowImage, null);
        g2.dispose();
        return shadowImage;
    }
}

