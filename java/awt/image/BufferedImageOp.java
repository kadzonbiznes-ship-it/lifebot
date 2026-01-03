/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

public interface BufferedImageOp {
    public BufferedImage filter(BufferedImage var1, BufferedImage var2);

    public Rectangle2D getBounds2D(BufferedImage var1);

    public BufferedImage createCompatibleDestImage(BufferedImage var1, ColorModel var2);

    public Point2D getPoint2D(Point2D var1, Point2D var2);

    public RenderingHints getRenderingHints();
}

