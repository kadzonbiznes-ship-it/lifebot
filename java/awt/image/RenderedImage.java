/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Vector;

public interface RenderedImage {
    public Vector<RenderedImage> getSources();

    public Object getProperty(String var1);

    public String[] getPropertyNames();

    public ColorModel getColorModel();

    public SampleModel getSampleModel();

    public int getWidth();

    public int getHeight();

    public int getMinX();

    public int getMinY();

    public int getNumXTiles();

    public int getNumYTiles();

    public int getMinTileX();

    public int getMinTileY();

    public int getTileWidth();

    public int getTileHeight();

    public int getTileGridXOffset();

    public int getTileGridYOffset();

    public Raster getTile(int var1, int var2);

    public Raster getData();

    public Raster getData(Rectangle var1);

    public WritableRaster copyData(WritableRaster var1);
}

