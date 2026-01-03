/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.Point;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.TileObserver;
import java.awt.image.WritableRaster;

public interface WritableRenderedImage
extends RenderedImage {
    public void addTileObserver(TileObserver var1);

    public void removeTileObserver(TileObserver var1);

    public WritableRaster getWritableTile(int var1, int var2);

    public void releaseWritableTile(int var1, int var2);

    public boolean isTileWritable(int var1, int var2);

    public Point[] getWritableTileIndices();

    public boolean hasTileWriters();

    public void setData(Raster var1);
}

