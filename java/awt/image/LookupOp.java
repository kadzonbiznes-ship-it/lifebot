/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.LookupTable;
import java.awt.image.Raster;
import java.awt.image.RasterOp;
import java.awt.image.ShortLookupTable;
import java.awt.image.WritableRaster;
import sun.awt.image.ImagingLib;

public class LookupOp
implements BufferedImageOp,
RasterOp {
    private LookupTable ltable;
    private int numComponents;
    RenderingHints hints;

    public LookupOp(LookupTable lookup, RenderingHints hints) {
        this.ltable = lookup;
        this.hints = hints;
        this.numComponents = this.ltable.getNumComponents();
    }

    public final LookupTable getTable() {
        return this.ltable;
    }

    @Override
    public final BufferedImage filter(BufferedImage src, BufferedImage dst) {
        ColorModel dstCM;
        ColorModel srcCM = src.getColorModel();
        int numBands = srcCM.getNumColorComponents();
        if (srcCM instanceof IndexColorModel) {
            throw new IllegalArgumentException("LookupOp cannot be performed on an indexed image");
        }
        int numComponents = this.ltable.getNumComponents();
        if (numComponents != 1 && numComponents != srcCM.getNumComponents() && numComponents != srcCM.getNumColorComponents()) {
            throw new IllegalArgumentException("Number of arrays in the  lookup table (" + numComponents + " is not compatible with the  src image: " + String.valueOf(src));
        }
        boolean needToConvert = false;
        int width = src.getWidth();
        int height = src.getHeight();
        if (dst == null) {
            dst = this.createCompatibleDestImage(src, null);
            dstCM = srcCM;
        } else {
            if (width != dst.getWidth()) {
                throw new IllegalArgumentException("Src width (" + width + ") not equal to dst width (" + dst.getWidth() + ")");
            }
            if (height != dst.getHeight()) {
                throw new IllegalArgumentException("Src height (" + height + ") not equal to dst height (" + dst.getHeight() + ")");
            }
            dstCM = dst.getColorModel();
            if (srcCM.getColorSpace().getType() != dstCM.getColorSpace().getType()) {
                needToConvert = true;
                dst = this.createCompatibleDestImage(src, null);
            }
        }
        BufferedImage origDst = dst;
        if (ImagingLib.filter(this, src, dst) == null) {
            int dstNumBands;
            WritableRaster srcRaster = src.getRaster();
            WritableRaster dstRaster = dst.getRaster();
            if (srcCM.hasAlpha() && (numBands - 1 == numComponents || numComponents == 1)) {
                int minx = srcRaster.getMinX();
                int miny = srcRaster.getMinY();
                int[] bands = new int[numBands - 1];
                for (int i = 0; i < numBands - 1; ++i) {
                    bands[i] = i;
                }
                srcRaster = srcRaster.createWritableChild(minx, miny, srcRaster.getWidth(), srcRaster.getHeight(), minx, miny, bands);
            }
            if (dstCM.hasAlpha() && ((dstNumBands = dstRaster.getNumBands()) - 1 == numComponents || numComponents == 1)) {
                int minx = dstRaster.getMinX();
                int miny = dstRaster.getMinY();
                int[] bands = new int[numBands - 1];
                for (int i = 0; i < numBands - 1; ++i) {
                    bands[i] = i;
                }
                dstRaster = dstRaster.createWritableChild(minx, miny, dstRaster.getWidth(), dstRaster.getHeight(), minx, miny, bands);
            }
            this.filter(srcRaster, dstRaster);
        }
        if (needToConvert) {
            ColorConvertOp ccop = new ColorConvertOp(this.hints);
            ccop.filter(dst, origDst);
        }
        return origDst;
    }

    @Override
    public final WritableRaster filter(Raster src, WritableRaster dst) {
        int numBands = src.getNumBands();
        int height = src.getHeight();
        int width = src.getWidth();
        int[] srcPix = new int[numBands];
        if (dst == null) {
            dst = this.createCompatibleDestRaster(src);
        } else if (height != dst.getHeight() || width != dst.getWidth()) {
            throw new IllegalArgumentException("Width or height of Rasters do not match");
        }
        int dstLength = dst.getNumBands();
        if (numBands != dstLength) {
            throw new IllegalArgumentException("Number of channels in the src (" + numBands + ") does not match number of channels in the destination (" + dstLength + ")");
        }
        int numComponents = this.ltable.getNumComponents();
        if (numComponents != 1 && numComponents != src.getNumBands()) {
            throw new IllegalArgumentException("Number of arrays in the  lookup table (" + numComponents + " is not compatible with the  src Raster: " + String.valueOf(src));
        }
        if (ImagingLib.filter(this, src, dst) != null) {
            return dst;
        }
        if (this.ltable instanceof ByteLookupTable) {
            this.byteFilter((ByteLookupTable)this.ltable, src, dst, width, height, numBands);
        } else if (this.ltable instanceof ShortLookupTable) {
            this.shortFilter((ShortLookupTable)this.ltable, src, dst, width, height, numBands);
        } else {
            int sminX = src.getMinX();
            int sY = src.getMinY();
            int dminX = dst.getMinX();
            int dY = dst.getMinY();
            int y = 0;
            while (y < height) {
                int sX = sminX;
                int dX = dminX;
                int x = 0;
                while (x < width) {
                    src.getPixel(sX, sY, srcPix);
                    this.ltable.lookupPixel(srcPix, srcPix);
                    dst.setPixel(dX, dY, srcPix);
                    ++x;
                    ++sX;
                    ++dX;
                }
                ++y;
                ++sY;
                ++dY;
            }
        }
        return dst;
    }

    @Override
    public final Rectangle2D getBounds2D(BufferedImage src) {
        return this.getBounds2D(src.getRaster());
    }

    @Override
    public final Rectangle2D getBounds2D(Raster src) {
        return src.getBounds();
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        BufferedImage image;
        int w = src.getWidth();
        int h = src.getHeight();
        int transferType = 0;
        if (destCM == null) {
            ColorModel cm = src.getColorModel();
            WritableRaster raster = src.getRaster();
            if (cm instanceof ComponentColorModel) {
                DataBuffer db = raster.getDataBuffer();
                boolean hasAlpha = cm.hasAlpha();
                boolean isPre = cm.isAlphaPremultiplied();
                int trans = cm.getTransparency();
                int[] nbits = null;
                if (this.ltable instanceof ByteLookupTable) {
                    if (db.getDataType() == 1) {
                        if (hasAlpha) {
                            nbits = new int[2];
                            nbits[1] = trans == 2 ? 1 : 8;
                        } else {
                            nbits = new int[]{8};
                        }
                    }
                } else if (this.ltable instanceof ShortLookupTable) {
                    transferType = 1;
                    if (db.getDataType() == 0) {
                        if (hasAlpha) {
                            nbits = new int[2];
                            nbits[1] = trans == 2 ? 1 : 16;
                        } else {
                            nbits = new int[]{16};
                        }
                    }
                }
                if (nbits != null) {
                    cm = new ComponentColorModel(cm.getColorSpace(), nbits, hasAlpha, isPre, trans, transferType);
                }
            }
            image = new BufferedImage(cm, cm.createCompatibleWritableRaster(w, h), cm.isAlphaPremultiplied(), null);
        } else {
            image = new BufferedImage(destCM, destCM.createCompatibleWritableRaster(w, h), destCM.isAlphaPremultiplied(), null);
        }
        return image;
    }

    @Override
    public WritableRaster createCompatibleDestRaster(Raster src) {
        return src.createCompatibleWritableRaster();
    }

    @Override
    public final Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null) {
            dstPt = new Point2D.Float();
        }
        dstPt.setLocation(srcPt.getX(), srcPt.getY());
        return dstPt;
    }

    @Override
    public final RenderingHints getRenderingHints() {
        return this.hints;
    }

    private void byteFilter(ByteLookupTable lookup, Raster src, WritableRaster dst, int width, int height, int numBands) {
        int[] srcPix = null;
        byte[][] table = lookup.getTable();
        int offset = lookup.getOffset();
        int step = 1;
        if (table.length == 1) {
            step = 0;
        }
        int len = table[0].length;
        for (int y = 0; y < height; ++y) {
            int tidx = 0;
            int band = 0;
            while (band < numBands) {
                srcPix = src.getSamples(0, y, width, 1, band, srcPix);
                for (int x = 0; x < width; ++x) {
                    int index = srcPix[x] - offset;
                    if (index < 0 || index > len) {
                        throw new IllegalArgumentException("index (" + index + "(out of range:  srcPix[" + x + "]=" + srcPix[x] + " offset=" + offset);
                    }
                    srcPix[x] = table[tidx][index];
                }
                dst.setSamples(0, y, width, 1, band, srcPix);
                ++band;
                tidx += step;
            }
        }
    }

    private void shortFilter(ShortLookupTable lookup, Raster src, WritableRaster dst, int width, int height, int numBands) {
        int[] srcPix = null;
        short[][] table = lookup.getTable();
        int offset = lookup.getOffset();
        int step = 1;
        if (table.length == 1) {
            step = 0;
        }
        int x = 0;
        int y = 0;
        int maxShort = 65535;
        for (y = 0; y < height; ++y) {
            int tidx = 0;
            int band = 0;
            while (band < numBands) {
                srcPix = src.getSamples(0, y, width, 1, band, srcPix);
                for (x = 0; x < width; ++x) {
                    int index = srcPix[x] - offset;
                    if (index < 0 || index > maxShort) {
                        throw new IllegalArgumentException("index out of range " + index + " x is " + x + "srcPix[x]=" + srcPix[x] + " offset=" + offset);
                    }
                    srcPix[x] = table[tidx][index];
                }
                dst.setSamples(0, y, width, 1, band, srcPix);
                ++band;
                tidx += step;
            }
        }
    }
}

