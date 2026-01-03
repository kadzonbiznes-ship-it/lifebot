/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface Shape {
    public Rectangle getBounds();

    public Rectangle2D getBounds2D();

    public boolean contains(double var1, double var3);

    public boolean contains(Point2D var1);

    public boolean intersects(double var1, double var3, double var5, double var7);

    public boolean intersects(Rectangle2D var1);

    public boolean contains(double var1, double var3, double var5, double var7);

    public boolean contains(Rectangle2D var1);

    public PathIterator getPathIterator(AffineTransform var1);

    public PathIterator getPathIterator(AffineTransform var1, double var2);
}

