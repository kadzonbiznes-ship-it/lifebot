/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.ui.latest;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import javax.swing.JLabel;
import org.pbrands.model.UserType;

public class RoundedAvatarLabel
extends JLabel {
    private Image avatarImage;
    private final UserType userType;
    private static final Color PRIMARY_COLOR = new Color(41, 182, 246);

    public RoundedAvatarLabel(UserType userType) {
        this.userType = userType;
        this.setPreferredSize(new Dimension(80, 80));
        this.setOpaque(false);
    }

    public void setAvatarImage(Image image) {
        this.avatarImage = image;
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        int size = 56;
        int x = (this.getWidth() - size) / 2;
        int y = (this.getHeight() - size) / 2;
        Color glowColor = new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 30);
        g2.setColor(glowColor);
        g2.fillOval(x - 2, y - 2, size + 4, size + 4);
        g2.setColor(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 20));
        g2.fillOval(x - 6, y - 6, size + 12, size + 12);
        g2.setColor(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 10));
        g2.fillOval(x - 10, y - 10, size + 20, size + 20);
        if (this.avatarImage != null) {
            g2.setClip(new Ellipse2D.Double(x, y, size, size));
            g2.drawImage(this.avatarImage, x, y, size, size, null);
        } else {
            g2.setColor(Color.GRAY);
            g2.fillOval(x, y, size, size);
        }
        g2.setClip(null);
        g2.setColor(PRIMARY_COLOR);
        g2.setStroke(new BasicStroke(3.0f));
        g2.draw(new Ellipse2D.Double(x, y, size, size));
        g2.dispose();
    }
}

