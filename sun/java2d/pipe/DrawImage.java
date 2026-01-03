/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.awt.image.VolatileImage;
import java.awt.image.WritableRaster;
import sun.awt.image.BytePackedRaster;
import sun.awt.image.ImageRepresentation;
import sun.awt.image.SurfaceManager;
import sun.awt.image.ToolkitImage;
import sun.java2d.InvalidPipeException;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.BlitBg;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.MaskBlit;
import sun.java2d.loops.ScaledBlit;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.TransformHelper;
import sun.java2d.pipe.DrawImagePipe;
import sun.java2d.pipe.Region;

public class DrawImage
implements DrawImagePipe {
    private static final double MAX_TX_ERROR = 1.0E-4;

    public boolean copyImage(SunGraphics2D sg, Image img, int x, int y, Color bgColor) {
        int imgw = img.getWidth(null);
        int imgh = img.getHeight(null);
        if (DrawImage.isSimpleTranslate(sg)) {
            return this.renderImageCopy(sg, img, bgColor, x + sg.transX, y + sg.transY, 0, 0, imgw, imgh);
        }
        AffineTransform atfm = sg.transform;
        if ((x | y) != 0) {
            atfm = new AffineTransform(atfm);
            atfm.translate(x, y);
        }
        this.transformImage(sg, img, atfm, sg.interpolationType, 0, 0, imgw, imgh, bgColor);
        return true;
    }

    public boolean copyImage(SunGraphics2D sg, Image img, int dx, int dy, int sx, int sy, int w, int h, Color bgColor) {
        if (DrawImage.isSimpleTranslate(sg)) {
            return this.renderImageCopy(sg, img, bgColor, dx + sg.transX, dy + sg.transY, sx, sy, w, h);
        }
        this.scaleImage(sg, img, dx, dy, dx + w, dy + h, sx, sy, sx + w, sy + h, bgColor);
        return true;
    }

    public boolean scaleImage(SunGraphics2D sg, Image img, int x, int y, int width, int height, Color bgColor) {
        double dy2;
        double dx2;
        double dy1;
        double dx1;
        int imgw = img.getWidth(null);
        int imgh = img.getHeight(null);
        if (width > 0 && height > 0 && DrawImage.isSimpleTranslate(sg) && this.renderImageScale(sg, img, bgColor, sg.interpolationType, 0, 0, imgw, imgh, dx1 = (double)(x + sg.transX), dy1 = (double)(y + sg.transY), dx2 = dx1 + (double)width, dy2 = dy1 + (double)height)) {
            return true;
        }
        AffineTransform atfm = sg.transform;
        if ((x | y) != 0 || width != imgw || height != imgh) {
            atfm = new AffineTransform(atfm);
            atfm.translate(x, y);
            atfm.scale((double)width / (double)imgw, (double)height / (double)imgh);
        }
        this.transformImage(sg, img, atfm, sg.interpolationType, 0, 0, imgw, imgh, bgColor);
        return true;
    }

    protected void transformImage(SunGraphics2D sg, Image img, int x, int y, AffineTransform extraAT, int interpType) {
        boolean checkfinalxform;
        int txtype = extraAT.getType();
        int imgw = img.getWidth(null);
        int imgh = img.getHeight(null);
        if (sg.transformState <= 2 && (txtype == 0 || txtype == 1)) {
            double tx = extraAT.getTranslateX();
            double ty = extraAT.getTranslateY();
            int itx = (int)Math.floor((tx += sg.transform.getTranslateX()) + 0.5);
            int ity = (int)Math.floor((ty += sg.transform.getTranslateY()) + 0.5);
            if (interpType == 1 || DrawImage.closeToInteger(itx, tx) && DrawImage.closeToInteger(ity, ty)) {
                this.renderImageCopy(sg, img, null, x + itx, y + ity, 0, 0, imgw, imgh);
                return;
            }
            checkfinalxform = false;
        } else if (sg.transformState <= 3 && (txtype & 0x78) == 0) {
            double[] coords = new double[]{0.0, 0.0, imgw, imgh};
            extraAT.transform(coords, 0, coords, 0, 2);
            coords[0] = coords[0] + (double)x;
            coords[1] = coords[1] + (double)y;
            coords[2] = coords[2] + (double)x;
            coords[3] = coords[3] + (double)y;
            sg.transform.transform(coords, 0, coords, 0, 2);
            if (this.tryCopyOrScale(sg, img, 0, 0, imgw, imgh, null, interpType, coords)) {
                return;
            }
            checkfinalxform = false;
        } else {
            checkfinalxform = true;
        }
        AffineTransform tx = new AffineTransform(sg.transform);
        tx.translate(x, y);
        tx.concatenate(extraAT);
        if (checkfinalxform) {
            this.transformImage(sg, img, tx, interpType, 0, 0, imgw, imgh, null);
        } else {
            this.renderImageXform(sg, img, tx, interpType, 0, 0, imgw, imgh, null);
        }
    }

    protected void transformImage(SunGraphics2D sg, Image img, AffineTransform tx, int interpType, int sx1, int sy1, int sx2, int sy2, Color bgColor) {
        double[] coords = new double[6];
        coords[2] = sx2 - sx1;
        coords[3] = coords[5] = (double)(sy2 - sy1);
        tx.transform(coords, 0, coords, 0, 3);
        if (Math.abs(coords[0] - coords[4]) < 1.0E-4 && Math.abs(coords[3] - coords[5]) < 1.0E-4 && this.tryCopyOrScale(sg, img, sx1, sy1, sx2, sy2, bgColor, interpType, coords)) {
            return;
        }
        this.renderImageXform(sg, img, tx, interpType, sx1, sy1, sx2, sy2, bgColor);
    }

    protected boolean tryCopyOrScale(SunGraphics2D sg, Image img, int sx1, int sy1, int sx2, int sy2, Color bgColor, int interpType, double[] coords) {
        double dx1 = coords[0];
        double dy1 = coords[1];
        double dx2 = coords[2];
        double dy2 = coords[3];
        double dw = dx2 - dx1;
        double dh = dy2 - dy1;
        if (dx1 < -2.147483648E9 || dx1 > 2.147483647E9 || dy1 < -2.147483648E9 || dy1 > 2.147483647E9 || dx2 < -2.147483648E9 || dx2 > 2.147483647E9 || dy2 < -2.147483648E9 || dy2 > 2.147483647E9) {
            return false;
        }
        if (DrawImage.closeToInteger(sx2 - sx1, dw) && DrawImage.closeToInteger(sy2 - sy1, dh)) {
            int idx = (int)Math.floor(dx1 + 0.5);
            int idy = (int)Math.floor(dy1 + 0.5);
            if (interpType == 1 || DrawImage.closeToInteger(idx, dx1) && DrawImage.closeToInteger(idy, dy1)) {
                this.renderImageCopy(sg, img, bgColor, idx, idy, sx1, sy1, sx2 - sx1, sy2 - sy1);
                return true;
            }
        }
        return dw > 0.0 && dh > 0.0 && this.renderImageScale(sg, img, bgColor, interpType, sx1, sy1, sx2, sy2, dx1, dy1, dx2, dy2);
    }

    private BufferedImage makeBufferedImage(Image img, Color bgColor, int type, int sx1, int sy1, int sx2, int sy2) {
        int width = sx2 - sx1;
        int height = sy2 - sy1;
        BufferedImage bimg = new BufferedImage(width, height, type);
        SunGraphics2D g2d = (SunGraphics2D)bimg.createGraphics();
        g2d.setComposite(AlphaComposite.Src);
        bimg.setAccelerationPriority(0.0f);
        if (bgColor != null) {
            g2d.setColor(bgColor);
            g2d.fillRect(0, 0, width, height);
            g2d.setComposite(AlphaComposite.SrcOver);
        }
        g2d.copyImage(img, 0, 0, sx1, sy1, width, height, null, null);
        g2d.dispose();
        return bimg;
    }

    protected void renderImageXform(SunGraphics2D sg, Image img, AffineTransform tx, int interpType, int sx1, int sy1, int sx2, int sy2, Color bgColor) {
        MaskBlit maskblit;
        SurfaceType srcType;
        TransformHelper helper;
        double ddy2;
        double ddx2;
        AffineTransform itx;
        try {
            itx = tx.createInverse();
            double[] mat = new double[6];
            itx.getMatrix(mat);
            for (double d : mat) {
                if (Double.isFinite(d)) continue;
                return;
            }
        }
        catch (NoninvertibleTransformException ignored) {
            return;
        }
        double[] coords = new double[8];
        coords[2] = coords[6] = (double)(sx2 - sx1);
        coords[5] = coords[7] = (double)(sy2 - sy1);
        tx.transform(coords, 0, coords, 0, 4);
        double ddx1 = ddx2 = coords[0];
        double ddy1 = ddy2 = coords[1];
        for (int i = 2; i < coords.length; i += 2) {
            double d = coords[i];
            if (ddx1 > d) {
                ddx1 = d;
            } else if (ddx2 < d) {
                ddx2 = d;
            }
            d = coords[i + 1];
            if (ddy1 > d) {
                ddy1 = d;
                continue;
            }
            if (!(ddy2 < d)) continue;
            ddy2 = d;
        }
        Region clip = sg.getCompClip();
        int dx1 = Math.max((int)Math.floor(ddx1), clip.getLoX());
        int dy1 = Math.max((int)Math.floor(ddy1), clip.getLoY());
        int dx2 = Math.min((int)Math.ceil(ddx2), clip.getHiX());
        int dy2 = Math.min((int)Math.ceil(ddy2), clip.getHiY());
        if (dx2 <= dx1 || dy2 <= dy1) {
            return;
        }
        SurfaceData dstData = sg.surfaceData;
        SurfaceData srcData = dstData.getSourceSurfaceData(img, 4, sg.imageComp, bgColor);
        if (srcData == null && (srcData = dstData.getSourceSurfaceData(img = this.getBufferedImage(img), 4, sg.imageComp, bgColor)) == null) {
            return;
        }
        if (DrawImage.isBgOperation(srcData, bgColor)) {
            int bgAlpha = bgColor.getAlpha();
            int type = bgAlpha == 255 ? 1 : 2;
            img = this.makeBufferedImage(img, bgColor, type, sx1, sy1, sx2, sy2);
            sx2 -= sx1;
            sy2 -= sy1;
            sy1 = 0;
            sx1 = 0;
            srcData = dstData.getSourceSurfaceData(img, 4, sg.imageComp, bgColor);
        }
        if ((helper = TransformHelper.getFromCache(srcType = srcData.getSurfaceType())) == null) {
            int type = srcData.getTransparency() == 1 ? 1 : 2;
            img = this.makeBufferedImage(img, null, type, sx1, sy1, sx2, sy2);
            sx2 -= sx1;
            sy2 -= sy1;
            sy1 = 0;
            sx1 = 0;
            srcData = dstData.getSourceSurfaceData(img, 4, sg.imageComp, null);
            srcType = srcData.getSurfaceType();
            helper = TransformHelper.getFromCache(srcType);
        }
        SurfaceType dstType = dstData.getSurfaceType();
        if (sg.compositeState <= 1 && (maskblit = MaskBlit.getFromCache(SurfaceType.IntArgbPre, sg.imageComp, dstType)).getNativePrim() != 0L) {
            helper.Transform(maskblit, srcData, dstData, sg.composite, clip, itx, interpType, sx1, sy1, sx2, sy2, dx1, dy1, dx2, dy2, null, 0, 0);
            return;
        }
        int w = dx2 - dx1;
        int h = dy2 - dy1;
        BufferedImage tmpimg = new BufferedImage(w, h, 3);
        SurfaceData tmpData = SurfaceData.getPrimarySurfaceData(tmpimg);
        SurfaceType tmpType = tmpData.getSurfaceType();
        MaskBlit tmpmaskblit = MaskBlit.getFromCache(SurfaceType.IntArgbPre, CompositeType.SrcNoEa, tmpType);
        int[] edges = new int[h * 2 + 2];
        helper.Transform(tmpmaskblit, srcData, tmpData, AlphaComposite.Src, null, itx, interpType, sx1, sy1, sx2, sy2, 0, 0, w, h, edges, dx1, dy1);
        Region region = Region.getInstance(dx1, dy1, dx2, dy2, edges);
        clip = clip.getIntersection(region);
        Blit blit = Blit.getFromCache(tmpType, sg.imageComp, dstType);
        blit.Blit(tmpData, dstData, sg.composite, clip, 0, 0, dx1, dy1, w, h);
    }

    protected boolean renderImageCopy(SunGraphics2D sg, Image img, Color bgColor, int dx, int dy, int sx, int sy, int w, int h) {
        Region clip = sg.getCompClip();
        SurfaceData dstData = sg.surfaceData;
        int attempts = 0;
        SurfaceData srcData;
        while ((srcData = dstData.getSourceSurfaceData(img, 0, sg.imageComp, bgColor)) != null) {
            try {
                DrawImage.blitSurfaceData(sg, clip, srcData, dstData, sx, sy, dx, dy, w, h, bgColor);
                return true;
            }
            catch (NullPointerException e) {
                if (!SurfaceData.isNull(dstData) && !SurfaceData.isNull(srcData)) {
                    throw e;
                }
                return false;
            }
            catch (InvalidPipeException e) {
                clip = sg.getCompClip();
                if (!SurfaceData.isNull(dstData = sg.surfaceData) && !SurfaceData.isNull(srcData) && ++attempts <= 1) continue;
                return false;
            }
            break;
        }
        return false;
    }

    protected boolean renderImageScale(SunGraphics2D sg, Image img, Color bgColor, int interpType, int sx1, int sy1, int sx2, int sy2, double dx1, double dy1, double dx2, double dy2) {
        if (interpType != 1) {
            return false;
        }
        Region clip = sg.getCompClip();
        SurfaceData dstData = sg.surfaceData;
        int attempts = 0;
        SurfaceData srcData;
        while ((srcData = dstData.getSourceSurfaceData(img, 3, sg.imageComp, bgColor)) != null && !DrawImage.isBgOperation(srcData, bgColor)) {
            try {
                SurfaceType srcType = srcData.getSurfaceType();
                SurfaceType dstType = dstData.getSurfaceType();
                return this.scaleSurfaceData(sg, clip, srcData, dstData, srcType, dstType, sx1, sy1, sx2, sy2, dx1, dy1, dx2, dy2);
            }
            catch (NullPointerException e) {
                if (!SurfaceData.isNull(dstData)) {
                    throw e;
                }
                return false;
            }
            catch (InvalidPipeException e) {
                clip = sg.getCompClip();
                if (!SurfaceData.isNull(dstData = sg.surfaceData) && !SurfaceData.isNull(srcData) && ++attempts <= 1) continue;
                return false;
            }
            break;
        }
        return false;
    }

    public boolean scaleImage(SunGraphics2D sg, Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgColor) {
        double ddy2;
        double ddx2;
        double ddy1;
        double ddx1;
        int dstY;
        int dstH;
        int dstX;
        int dstW;
        int srcY;
        int srcH;
        int srcX;
        int srcW;
        boolean srcWidthFlip = false;
        boolean srcHeightFlip = false;
        boolean dstWidthFlip = false;
        boolean dstHeightFlip = false;
        if (sx2 > sx1) {
            srcW = sx2 - sx1;
            srcX = sx1;
        } else {
            srcWidthFlip = true;
            srcW = sx1 - sx2;
            srcX = sx2;
        }
        if (sy2 > sy1) {
            srcH = sy2 - sy1;
            srcY = sy1;
        } else {
            srcHeightFlip = true;
            srcH = sy1 - sy2;
            srcY = sy2;
        }
        if (dx2 > dx1) {
            dstW = dx2 - dx1;
            dstX = dx1;
        } else {
            dstW = dx1 - dx2;
            dstWidthFlip = true;
            dstX = dx2;
        }
        if (dy2 > dy1) {
            dstH = dy2 - dy1;
            dstY = dy1;
        } else {
            dstH = dy1 - dy2;
            dstHeightFlip = true;
            dstY = dy2;
        }
        if (srcW <= 0 || srcH <= 0) {
            return true;
        }
        if (srcWidthFlip == dstWidthFlip && srcHeightFlip == dstHeightFlip && DrawImage.isSimpleTranslate(sg) && this.renderImageScale(sg, img, bgColor, sg.interpolationType, srcX, srcY, srcX + srcW, srcY + srcH, ddx1 = (double)(dstX + sg.transX), ddy1 = (double)(dstY + sg.transY), ddx2 = ddx1 + (double)dstW, ddy2 = ddy1 + (double)dstH)) {
            return true;
        }
        AffineTransform atfm = new AffineTransform(sg.transform);
        atfm.translate(dx1, dy1);
        double m00 = (double)(dx2 - dx1) / (double)(sx2 - sx1);
        double m11 = (double)(dy2 - dy1) / (double)(sy2 - sy1);
        atfm.scale(m00, m11);
        atfm.translate(srcX - sx1, srcY - sy1);
        double scaleX = SurfaceManager.getImageScaleX(img);
        double scaleY = SurfaceManager.getImageScaleY(img);
        int imgW = (int)Math.ceil((double)img.getWidth(null) * scaleX);
        int imgH = (int)Math.ceil((double)img.getHeight(null) * scaleY);
        srcH += srcY;
        if ((srcW += srcX) > imgW) {
            srcW = imgW;
        }
        if (srcH > imgH) {
            srcH = imgH;
        }
        if (srcX < 0) {
            atfm.translate(-srcX, 0.0);
            srcX = 0;
        }
        if (srcY < 0) {
            atfm.translate(0.0, -srcY);
            srcY = 0;
        }
        if (srcX >= srcW || srcY >= srcH) {
            return true;
        }
        this.transformImage(sg, img, atfm, sg.interpolationType, srcX, srcY, srcW, srcH, bgColor);
        return true;
    }

    public static boolean closeToInteger(int i, double d) {
        return Math.abs(d - (double)i) < 1.0E-4;
    }

    public static boolean isSimpleTranslate(SunGraphics2D sg) {
        int ts = sg.transformState;
        if (ts <= 1) {
            return true;
        }
        if (ts >= 3) {
            return false;
        }
        return sg.interpolationType == 1;
    }

    protected static boolean isBgOperation(SurfaceData srcData, Color bgColor) {
        return srcData == null || bgColor != null && srcData.getTransparency() != 1;
    }

    protected BufferedImage getBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage)img;
        }
        return ((VolatileImage)img).getSnapshot();
    }

    private ColorModel getTransformColorModel(SunGraphics2D sg, BufferedImage bImg, AffineTransform tx) {
        boolean needTrans;
        ColorModel cm;
        ColorModel dstCM = cm = bImg.getColorModel();
        if (tx.isIdentity()) {
            return dstCM;
        }
        int type = tx.getType();
        boolean bl = needTrans = (type & 0x38) != 0;
        if (!needTrans && type != 1 && type != 0) {
            double[] mtx = new double[4];
            tx.getMatrix(mtx);
            boolean bl2 = needTrans = mtx[0] != (double)((int)mtx[0]) || mtx[3] != (double)((int)mtx[3]);
        }
        if (sg.renderHint != 2) {
            if (cm instanceof IndexColorModel) {
                WritableRaster raster = bImg.getRaster();
                IndexColorModel icm = (IndexColorModel)cm;
                if (needTrans && cm.getTransparency() == 1) {
                    if (raster instanceof BytePackedRaster) {
                        dstCM = ColorModel.getRGBdefault();
                    } else {
                        double[] matrix = new double[6];
                        tx.getMatrix(matrix);
                        if (matrix[1] != 0.0 || matrix[2] != 0.0 || matrix[4] != 0.0 || matrix[5] != 0.0) {
                            int mapSize = icm.getMapSize();
                            if (mapSize < 256) {
                                int[] cmap = new int[mapSize + 1];
                                icm.getRGBs(cmap);
                                cmap[mapSize] = 0;
                                dstCM = new IndexColorModel(icm.getPixelSize(), mapSize + 1, cmap, 0, true, mapSize, 0);
                            } else {
                                dstCM = ColorModel.getRGBdefault();
                            }
                        }
                    }
                }
            } else if (needTrans && cm.getTransparency() == 1) {
                dstCM = ColorModel.getRGBdefault();
            }
        } else if (cm instanceof IndexColorModel || needTrans && cm.getTransparency() == 1) {
            dstCM = ColorModel.getRGBdefault();
        }
        return dstCM;
    }

    private static void blitSurfaceData(SunGraphics2D sg, Region clip, SurfaceData srcData, SurfaceData dstData, int sx, int sy, int dx, int dy, int w, int h, Color bgColor) {
        CompositeType comp = sg.imageComp;
        if (CompositeType.SrcOverNoEa.equals(comp) && (srcData.getTransparency() == 1 || bgColor != null && bgColor.getTransparency() == 1)) {
            comp = CompositeType.SrcNoEa;
        }
        if (srcData == dstData && sx == dx && sy == dy && CompositeType.SrcNoEa.equals(comp)) {
            return;
        }
        Rectangle dst = new Rectangle(dx, dy, w, h).intersection(dstData.getBounds());
        if (dst.isEmpty()) {
            return;
        }
        sx += dst.x - dx;
        sy += dst.y - dy;
        SurfaceType srcType = srcData.getSurfaceType();
        SurfaceType dstType = dstData.getSurfaceType();
        if (!DrawImage.isBgOperation(srcData, bgColor)) {
            Blit blit = Blit.getFromCache(srcType, comp, dstType);
            blit.Blit(srcData, dstData, sg.composite, clip, sx, sy, dst.x, dst.y, dst.width, dst.height);
        } else {
            BlitBg blit = BlitBg.getFromCache(srcType, comp, dstType);
            blit.BlitBg(srcData, dstData, sg.composite, clip, bgColor.getRGB(), sx, sy, dst.x, dst.y, dst.width, dst.height);
        }
    }

    protected boolean scaleSurfaceData(SunGraphics2D sg, Region clipRegion, SurfaceData srcData, SurfaceData dstData, SurfaceType srcType, SurfaceType dstType, int sx1, int sy1, int sx2, int sy2, double dx1, double dy1, double dx2, double dy2) {
        ScaledBlit blit;
        CompositeType comp = sg.imageComp;
        if (CompositeType.SrcOverNoEa.equals(comp) && srcData.getTransparency() == 1) {
            comp = CompositeType.SrcNoEa;
        }
        if ((blit = ScaledBlit.getFromCache(srcType, comp, dstType)) != null) {
            blit.Scale(srcData, dstData, sg.composite, clipRegion, sx1, sy1, sx2, sy2, dx1, dy1, dx2, dy2);
            return true;
        }
        return false;
    }

    protected static boolean imageReady(ToolkitImage sunimg, ImageObserver observer) {
        if (sunimg.hasError()) {
            if (observer != null) {
                observer.imageUpdate(sunimg, 192, -1, -1, -1, -1);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean copyImage(SunGraphics2D sg, Image img, int x, int y, Color bgColor, ImageObserver observer) {
        if (!(img instanceof ToolkitImage)) {
            return this.copyImage(sg, img, x, y, bgColor);
        }
        ToolkitImage sunimg = (ToolkitImage)img;
        if (!DrawImage.imageReady(sunimg, observer)) {
            return false;
        }
        ImageRepresentation ir = sunimg.getImageRep();
        return ir.drawToBufImage(sg, sunimg, x, y, bgColor, observer);
    }

    @Override
    public boolean copyImage(SunGraphics2D sg, Image img, int dx, int dy, int sx, int sy, int w, int h, Color bgColor, ImageObserver observer) {
        if (!(img instanceof ToolkitImage)) {
            return this.copyImage(sg, img, dx, dy, sx, sy, w, h, bgColor);
        }
        ToolkitImage sunimg = (ToolkitImage)img;
        if (!DrawImage.imageReady(sunimg, observer)) {
            return false;
        }
        ImageRepresentation ir = sunimg.getImageRep();
        return ir.drawToBufImage(sg, sunimg, dx, dy, dx + w, dy + h, sx, sy, sx + w, sy + h, bgColor, observer);
    }

    @Override
    public boolean scaleImage(SunGraphics2D sg, Image img, int x, int y, int width, int height, Color bgColor, ImageObserver observer) {
        if (!(img instanceof ToolkitImage)) {
            return this.scaleImage(sg, img, x, y, width, height, bgColor);
        }
        ToolkitImage sunimg = (ToolkitImage)img;
        if (!DrawImage.imageReady(sunimg, observer)) {
            return false;
        }
        ImageRepresentation ir = sunimg.getImageRep();
        return ir.drawToBufImage(sg, sunimg, x, y, width, height, bgColor, observer);
    }

    @Override
    public boolean scaleImage(SunGraphics2D sg, Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgColor, ImageObserver observer) {
        if (!(img instanceof ToolkitImage)) {
            return this.scaleImage(sg, img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgColor);
        }
        ToolkitImage sunimg = (ToolkitImage)img;
        if (!DrawImage.imageReady(sunimg, observer)) {
            return false;
        }
        ImageRepresentation ir = sunimg.getImageRep();
        return ir.drawToBufImage(sg, sunimg, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgColor, observer);
    }

    @Override
    public boolean transformImage(SunGraphics2D sg, Image img, AffineTransform atfm, ImageObserver observer) {
        if (!(img instanceof ToolkitImage)) {
            this.transformImage(sg, img, 0, 0, atfm, sg.interpolationType);
            return true;
        }
        ToolkitImage sunimg = (ToolkitImage)img;
        if (!DrawImage.imageReady(sunimg, observer)) {
            return false;
        }
        ImageRepresentation ir = sunimg.getImageRep();
        return ir.drawToBufImage(sg, sunimg, atfm, observer);
    }

    @Override
    public void transformImage(SunGraphics2D sg, BufferedImage img, BufferedImageOp op, int x, int y) {
        if (op != null) {
            if (op instanceof AffineTransformOp) {
                AffineTransformOp atop = (AffineTransformOp)op;
                this.transformImage(sg, img, x, y, atop.getTransform(), atop.getInterpolationType());
                return;
            }
            img = op.filter(img, null);
        }
        this.copyImage(sg, img, x, y, null);
    }
}

