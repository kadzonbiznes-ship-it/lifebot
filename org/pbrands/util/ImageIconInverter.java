/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class ImageIconInverter {
    public static ImageIcon invertImageIcon(ImageIcon icon) {
        int width = icon.getIconWidth();
        int height = icon.getIconHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, 2);
        Graphics2D g = bufferedImage.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int rgba = bufferedImage.getRGB(x, y);
                Color col = new Color(rgba, true);
                int invertedRed = 255 - col.getRed();
                int invertedGreen = 255 - col.getGreen();
                int invertedBlue = 255 - col.getBlue();
                Color invertedColor = new Color(invertedRed, invertedGreen, invertedBlue, col.getAlpha());
                bufferedImage.setRGB(x, y, invertedColor.getRGB());
            }
        }
        return new ImageIcon(bufferedImage);
    }
}

