/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.d3d.D3DBufImgOps;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.TransformBlit;
import sun.java2d.pipe.DrawImage;

public class D3DDrawImage
extends DrawImage {
    @Override
    protected void renderImageXform(SunGraphics2D sg, Image img, AffineTransform tx, int interpType, int sx1, int sy1, int sx2, int sy2, Color bgColor) {
        SurfaceType dstType;
        SurfaceType srcType;
        TransformBlit blit;
        SurfaceData dstData;
        SurfaceData srcData;
        if (interpType != 3 && (srcData = (dstData = sg.surfaceData).getSourceSurfaceData(img, 4, sg.imageComp, bgColor)) != null && !D3DDrawImage.isBgOperation(srcData, bgColor) && (blit = TransformBlit.getFromCache(srcType = srcData.getSurfaceType(), sg.imageComp, dstType = dstData.getSurfaceType())) != null) {
            blit.Transform(srcData, dstData, sg.composite, sg.getCompClip(), tx, interpType, sx1, sy1, 0, 0, sx2 - sx1, sy2 - sy1);
            return;
        }
        super.renderImageXform(sg, img, tx, interpType, sx1, sy1, sx2, sy2, bgColor);
    }

    @Override
    public void transformImage(SunGraphics2D sg, BufferedImage img, BufferedImageOp op, int x, int y) {
        if (op != null) {
            if (op instanceof AffineTransformOp) {
                AffineTransformOp atop = (AffineTransformOp)op;
                this.transformImage(sg, img, x, y, atop.getTransform(), atop.getInterpolationType());
                return;
            }
            if (D3DBufImgOps.renderImageWithOp(sg, img, op, x, y)) {
                return;
            }
            img = op.filter(img, null);
        }
        this.copyImage(sg, img, x, y, null);
    }
}

