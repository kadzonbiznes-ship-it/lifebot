/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ImagingOpException;
import java.awt.image.IndexColorModel;
import java.awt.image.Kernel;
import java.awt.image.Raster;
import java.awt.image.RasterOp;
import java.awt.image.WritableRaster;
import sun.awt.image.ImagingLib;

public class ConvolveOp
implements BufferedImageOp,
RasterOp {
    Kernel kernel;
    int edgeHint;
    RenderingHints hints;
    public static final int EDGE_ZERO_FILL = 0;
    public static final int EDGE_NO_OP = 1;

    public ConvolveOp(Kernel kernel, int edgeCondition, RenderingHints hints) {
        this.kernel = kernel;
        this.edgeHint = edgeCondition;
        this.hints = hints;
    }

    public ConvolveOp(Kernel kernel) {
        this.kernel = kernel;
        this.edgeHint = 0;
    }

    public int getEdgeCondition() {
        return this.edgeHint;
    }

    public final Kernel getKernel() {
        return (Kernel)this.kernel.clone();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final BufferedImage filter(BufferedImage src, BufferedImage dst) {
        if (src == null) {
            throw new NullPointerException("src image is null");
        }
        if (src == dst) {
            throw new IllegalArgumentException("src image cannot be the same as the dst image");
        }
        boolean needToConvert = false;
        ColorModel srcCM = src.getColorModel();
        BufferedImage origDst = dst;
        if (srcCM instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel)srcCM;
            src = icm.convertToIntDiscrete(src.getRaster(), false);
            srcCM = src.getColorModel();
        }
        if (dst == null) {
            dst = this.createCompatibleDestImage(src, null);
            ColorModel dstCM = srcCM;
            origDst = dst;
        } else {
            ColorModel dstCM = dst.getColorModel();
            if (srcCM.getColorSpace().getType() != dstCM.getColorSpace().getType()) {
                needToConvert = true;
                dst = this.createCompatibleDestImage(src, null);
                dstCM = dst.getColorModel();
            } else if (dstCM instanceof IndexColorModel) {
                dst = this.createCompatibleDestImage(src, null);
                dstCM = dst.getColorModel();
            }
        }
        if (ImagingLib.filter(this, src, dst) == null) {
            throw new ImagingOpException("Unable to convolve src image");
        }
        if (needToConvert) {
            ColorConvertOp ccop = new ColorConvertOp(this.hints);
            ccop.filter(dst, origDst);
        } else if (origDst != dst) {
            Graphics2D g = origDst.createGraphics();
            try {
                g.drawImage((Image)dst, 0, 0, null);
            }
            finally {
                g.dispose();
            }
        }
        return origDst;
    }

    @Override
    public final WritableRaster filter(Raster src, WritableRaster dst) {
        if (dst == null) {
            dst = this.createCompatibleDestRaster(src);
        } else {
            if (src == dst) {
                throw new IllegalArgumentException("src image cannot be the same as the dst image");
            }
            if (src.getNumBands() != dst.getNumBands()) {
                throw new ImagingOpException("Different number of bands in src  and dst Rasters");
            }
        }
        if (ImagingLib.filter(this, src, dst) == null) {
            throw new ImagingOpException("Unable to convolve src image");
        }
        return dst;
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        int w = src.getWidth();
        int h = src.getHeight();
        WritableRaster wr = null;
        if (destCM == null) {
            destCM = src.getColorModel();
            if (destCM instanceof IndexColorModel) {
                destCM = ColorModel.getRGBdefault();
            } else {
                wr = src.getData().createCompatibleWritableRaster(w, h);
            }
        }
        if (wr == null) {
            wr = destCM.createCompatibleWritableRaster(w, h);
        }
        BufferedImage image = new BufferedImage(destCM, wr, destCM.isAlphaPremultiplied(), null);
        return image;
    }

    @Override
    public WritableRaster createCompatibleDestRaster(Raster src) {
        return src.createCompatibleWritableRaster();
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
}

