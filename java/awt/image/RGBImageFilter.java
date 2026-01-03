/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.image.ColorModel;
import java.awt.image.ImageFilter;
import java.awt.image.IndexColorModel;

public abstract class RGBImageFilter
extends ImageFilter {
    protected ColorModel origmodel;
    protected ColorModel newmodel;
    protected boolean canFilterIndexColorModel;

    protected RGBImageFilter() {
    }

    @Override
    public void setColorModel(ColorModel model) {
        if (this.canFilterIndexColorModel && model instanceof IndexColorModel) {
            IndexColorModel newcm = this.filterIndexColorModel((IndexColorModel)model);
            this.substituteColorModel(model, newcm);
            this.consumer.setColorModel(newcm);
        } else {
            this.consumer.setColorModel(ColorModel.getRGBdefault());
        }
    }

    public void substituteColorModel(ColorModel oldcm, ColorModel newcm) {
        this.origmodel = oldcm;
        this.newmodel = newcm;
    }

    public IndexColorModel filterIndexColorModel(IndexColorModel icm) {
        int mapsize = icm.getMapSize();
        byte[] r = new byte[mapsize];
        byte[] g = new byte[mapsize];
        byte[] b = new byte[mapsize];
        byte[] a = new byte[mapsize];
        icm.getReds(r);
        icm.getGreens(g);
        icm.getBlues(b);
        icm.getAlphas(a);
        int trans = icm.getTransparentPixel();
        boolean needalpha = false;
        for (int i = 0; i < mapsize; ++i) {
            int rgb = this.filterRGB(-1, -1, icm.getRGB(i));
            a[i] = (byte)(rgb >> 24);
            if (a[i] != -1 && i != trans) {
                needalpha = true;
            }
            r[i] = (byte)(rgb >> 16);
            g[i] = (byte)(rgb >> 8);
            b[i] = (byte)(rgb >> 0);
        }
        if (needalpha) {
            return new IndexColorModel(icm.getPixelSize(), mapsize, r, g, b, a);
        }
        return new IndexColorModel(icm.getPixelSize(), mapsize, r, g, b, trans);
    }

    public void filterRGBPixels(int x, int y, int w, int h, int[] pixels, int off, int scansize) {
        int index = off;
        for (int cy = 0; cy < h; ++cy) {
            for (int cx = 0; cx < w; ++cx) {
                pixels[index] = this.filterRGB(x + cx, y + cy, pixels[index]);
                ++index;
            }
            index += scansize - w;
        }
        this.consumer.setPixels(x, y, w, h, ColorModel.getRGBdefault(), pixels, off, scansize);
    }

    @Override
    public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
        if (model == this.origmodel) {
            this.consumer.setPixels(x, y, w, h, this.newmodel, pixels, off, scansize);
        } else {
            int[] filteredpixels = new int[w];
            int index = off;
            for (int cy = 0; cy < h; ++cy) {
                for (int cx = 0; cx < w; ++cx) {
                    filteredpixels[cx] = model.getRGB(pixels[index] & 0xFF);
                    ++index;
                }
                index += scansize - w;
                this.filterRGBPixels(x, y + cy, w, 1, filteredpixels, 0, w);
            }
        }
    }

    @Override
    public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
        if (model == this.origmodel) {
            this.consumer.setPixels(x, y, w, h, this.newmodel, pixels, off, scansize);
        } else {
            int[] filteredpixels = new int[w];
            int index = off;
            for (int cy = 0; cy < h; ++cy) {
                for (int cx = 0; cx < w; ++cx) {
                    filteredpixels[cx] = model.getRGB(pixels[index]);
                    ++index;
                }
                index += scansize - w;
                this.filterRGBPixels(x, y + cy, w, 1, filteredpixels, 0, w);
            }
        }
    }

    public abstract int filterRGB(int var1, int var2, int var3);
}

