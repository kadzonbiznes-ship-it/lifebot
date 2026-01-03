/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ImagingOpException;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.RasterOp;
import java.awt.image.WritableRaster;
import sun.awt.image.ImagingLib;

public class AffineTransformOp
implements BufferedImageOp,
RasterOp {
    private AffineTransform xform;
    RenderingHints hints;
    public static final int TYPE_NEAREST_NEIGHBOR = 1;
    public static final int TYPE_BILINEAR = 2;
    public static final int TYPE_BICUBIC = 3;
    int interpolationType = 1;

    public AffineTransformOp(AffineTransform xform, RenderingHints hints) {
        this.validateTransform(xform);
        this.xform = (AffineTransform)xform.clone();
        this.hints = hints;
        if (hints != null) {
            Object value = hints.get(RenderingHints.KEY_INTERPOLATION);
            if (value == null) {
                value = hints.get(RenderingHints.KEY_RENDERING);
                if (value == RenderingHints.VALUE_RENDER_SPEED) {
                    this.interpolationType = 1;
                } else if (value == RenderingHints.VALUE_RENDER_QUALITY) {
                    this.interpolationType = 2;
                }
            } else if (value == RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR) {
                this.interpolationType = 1;
            } else if (value == RenderingHints.VALUE_INTERPOLATION_BILINEAR) {
                this.interpolationType = 2;
            } else if (value == RenderingHints.VALUE_INTERPOLATION_BICUBIC) {
                this.interpolationType = 3;
            }
        } else {
            this.interpolationType = 1;
        }
    }

    public AffineTransformOp(AffineTransform xform, int interpolationType) {
        this.validateTransform(xform);
        this.xform = (AffineTransform)xform.clone();
        switch (interpolationType) {
            case 1: 
            case 2: 
            case 3: {
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown interpolation type: " + interpolationType);
            }
        }
        this.interpolationType = interpolationType;
    }

    public final int getInterpolationType() {
        return this.interpolationType;
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
        if (dst == null) {
            dst = this.createCompatibleDestImage(src, null);
            ColorModel dstCM = srcCM;
            origDst = dst;
        } else {
            ColorModel dstCM = dst.getColorModel();
            if (srcCM.getColorSpace().getType() != dstCM.getColorSpace().getType()) {
                boolean needTrans;
                int type = this.xform.getType();
                boolean bl = needTrans = (type & 0x38) != 0;
                if (!needTrans && type != 1 && type != 0) {
                    double[] mtx = new double[4];
                    this.xform.getMatrix(mtx);
                    boolean bl2 = needTrans = mtx[0] != (double)((int)mtx[0]) || mtx[3] != (double)((int)mtx[3]);
                }
                if (needTrans && srcCM.getTransparency() == 1) {
                    ColorConvertOp ccop = new ColorConvertOp(this.hints);
                    BufferedImage tmpSrc = null;
                    int sw = src.getWidth();
                    int sh = src.getHeight();
                    if (dstCM.getTransparency() == 1) {
                        tmpSrc = new BufferedImage(sw, sh, 2);
                    } else {
                        WritableRaster r = dstCM.createCompatibleWritableRaster(sw, sh);
                        tmpSrc = new BufferedImage(dstCM, r, dstCM.isAlphaPremultiplied(), null);
                    }
                    src = ccop.filter(src, tmpSrc);
                } else {
                    needToConvert = true;
                    dst = this.createCompatibleDestImage(src, null);
                }
            }
        }
        if (this.interpolationType != 1 && dst.getColorModel() instanceof IndexColorModel) {
            dst = new BufferedImage(dst.getWidth(), dst.getHeight(), 2);
        }
        if (ImagingLib.filter(this, src, dst) == null) {
            throw new ImagingOpException("Unable to transform src image");
        }
        if (needToConvert) {
            ColorConvertOp ccop = new ColorConvertOp(this.hints);
            ccop.filter(dst, origDst);
        } else if (origDst != dst) {
            Graphics2D g = origDst.createGraphics();
            try {
                g.setComposite(AlphaComposite.Src);
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
        if (src == null) {
            throw new NullPointerException("src image is null");
        }
        if (dst == null) {
            dst = this.createCompatibleDestRaster(src);
        }
        if (src == dst) {
            throw new IllegalArgumentException("src image cannot be the same as the dst image");
        }
        if (src.getNumBands() != dst.getNumBands()) {
            throw new IllegalArgumentException("Number of src bands (" + src.getNumBands() + ") does not match number of  dst bands (" + dst.getNumBands() + ")");
        }
        if (ImagingLib.filter(this, src, dst) == null) {
            throw new ImagingOpException("Unable to transform src image");
        }
        return dst;
    }

    @Override
    public final Rectangle2D getBounds2D(BufferedImage src) {
        return this.getBounds2D(src.getRaster());
    }

    @Override
    public final Rectangle2D getBounds2D(Raster src) {
        int w = src.getWidth();
        int h = src.getHeight();
        float[] pts = new float[]{0.0f, 0.0f, w, 0.0f, w, h, 0.0f, h};
        this.xform.transform(pts, 0, pts, 0, 4);
        float fmaxX = pts[0];
        float fmaxY = pts[1];
        float fminX = pts[0];
        float fminY = pts[1];
        for (int i = 2; i < 8; i += 2) {
            if (pts[i] > fmaxX) {
                fmaxX = pts[i];
            } else if (pts[i] < fminX) {
                fminX = pts[i];
            }
            if (pts[i + 1] > fmaxY) {
                fmaxY = pts[i + 1];
                continue;
            }
            if (!(pts[i + 1] < fminY)) continue;
            fminY = pts[i + 1];
        }
        return new Rectangle2D.Float(fminX, fminY, fmaxX - fminX, fmaxY - fminY);
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        BufferedImage image;
        Rectangle r = this.getBounds2D(src).getBounds();
        int w = r.x + r.width;
        int h = r.y + r.height;
        if (w <= 0) {
            throw new RasterFormatException("Transformed width (" + w + ") is less than or equal to 0.");
        }
        if (h <= 0) {
            throw new RasterFormatException("Transformed height (" + h + ") is less than or equal to 0.");
        }
        if (destCM == null) {
            ColorModel cm = src.getColorModel();
            image = this.interpolationType != 1 && (cm instanceof IndexColorModel || cm.getTransparency() == 1) ? new BufferedImage(w, h, 2) : new BufferedImage(cm, src.getRaster().createCompatibleWritableRaster(w, h), cm.isAlphaPremultiplied(), null);
        } else {
            image = new BufferedImage(destCM, destCM.createCompatibleWritableRaster(w, h), destCM.isAlphaPremultiplied(), null);
        }
        return image;
    }

    @Override
    public WritableRaster createCompatibleDestRaster(Raster src) {
        Rectangle2D r = this.getBounds2D(src);
        return src.createCompatibleWritableRaster((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight());
    }

    @Override
    public final Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        return this.xform.transform(srcPt, dstPt);
    }

    public final AffineTransform getTransform() {
        return (AffineTransform)this.xform.clone();
    }

    @Override
    public final RenderingHints getRenderingHints() {
        if (this.hints == null) {
            this.hints = new RenderingHints(RenderingHints.KEY_INTERPOLATION, switch (this.interpolationType) {
                case 1 -> RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                case 2 -> RenderingHints.VALUE_INTERPOLATION_BILINEAR;
                case 3 -> RenderingHints.VALUE_INTERPOLATION_BICUBIC;
                default -> throw new InternalError("Unknown interpolation type " + this.interpolationType);
            });
        }
        return this.hints;
    }

    void validateTransform(AffineTransform xform) {
        if (Math.abs(xform.getDeterminant()) <= Double.MIN_VALUE) {
            throw new ImagingOpException("Unable to invert transform " + String.valueOf(xform));
        }
    }
}

