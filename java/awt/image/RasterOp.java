/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public interface RasterOp {
    public WritableRaster filter(Raster var1, WritableRaster var2);

    public Rectangle2D getBounds2D(Raster var1);

    public WritableRaster createCompatibleDestRaster(Raster var1);

    public Point2D getPoint2D(Point2D var1, Point2D var2);

    public RenderingHints getRenderingHints();
}

