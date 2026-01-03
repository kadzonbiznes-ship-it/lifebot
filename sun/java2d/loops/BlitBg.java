/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import sun.awt.image.BufImgSurfaceData;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.FillRect;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.RenderCache;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;

public class BlitBg
extends GraphicsPrimitive {
    public static final String methodSignature = "BlitBg(...)".toString();
    public static final int primTypeID = BlitBg.makePrimTypeID();
    private static RenderCache blitcache = new RenderCache(20);

    public static BlitBg locate(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        return (BlitBg)GraphicsPrimitiveMgr.locate(primTypeID, srctype, comptype, dsttype);
    }

    public static BlitBg getFromCache(SurfaceType src, CompositeType comp, SurfaceType dst) {
        Object o = blitcache.get(src, comp, dst);
        if (o != null) {
            return (BlitBg)o;
        }
        BlitBg blit = BlitBg.locate(src, comp, dst);
        if (blit == null) {
            System.out.println("blitbg loop not found for:");
            System.out.println("src:  " + String.valueOf(src));
            System.out.println("comp: " + String.valueOf(comp));
            System.out.println("dst:  " + String.valueOf(dst));
        } else {
            blitcache.put(src, comp, dst, blit);
        }
        return blit;
    }

    protected BlitBg(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public BlitBg(long pNativePrim, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public native void BlitBg(SurfaceData var1, SurfaceData var2, Composite var3, Region var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11);

    @Override
    protected GraphicsPrimitive makePrimitive(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        return new General(srctype, comptype, dsttype);
    }

    @Override
    public GraphicsPrimitive traceWrap() {
        return new TraceBlitBg(this);
    }

    static {
        GraphicsPrimitiveMgr.GeneralPrimitives.register(new BlitBg(null, null, null));
    }

    private static class General
    extends BlitBg {
        CompositeType compositeType;
        private static Font defaultFont = new Font("Dialog", 0, 12);

        public General(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
            super(srctype, comptype, dsttype);
            this.compositeType = comptype;
        }

        @Override
        public void BlitBg(SurfaceData srcData, SurfaceData dstData, Composite comp, Region clip, int bgArgb, int srcx, int srcy, int dstx, int dsty, int width, int height) {
            boolean bgHasAlpha;
            ColorModel dstModel = dstData.getColorModel();
            boolean bl = bgHasAlpha = bgArgb >>> 24 != 255;
            if (!dstModel.hasAlpha() && bgHasAlpha) {
                dstModel = ColorModel.getRGBdefault();
            }
            WritableRaster wr = dstModel.createCompatibleWritableRaster(width, height);
            boolean isPremult = dstModel.isAlphaPremultiplied();
            BufferedImage bimg = new BufferedImage(dstModel, wr, isPremult, null);
            SurfaceData tmpData = BufImgSurfaceData.createData(bimg);
            Color bgColor = new Color(bgArgb, bgHasAlpha);
            SunGraphics2D sg2d = new SunGraphics2D(tmpData, bgColor, bgColor, defaultFont);
            FillRect fillop = FillRect.locate(SurfaceType.AnyColor, CompositeType.SrcNoEa, tmpData.getSurfaceType());
            Blit combineop = Blit.getFromCache(srcData.getSurfaceType(), CompositeType.SrcOverNoEa, tmpData.getSurfaceType());
            Blit blitop = Blit.getFromCache(tmpData.getSurfaceType(), this.compositeType, dstData.getSurfaceType());
            fillop.FillRect(sg2d, tmpData, 0, 0, width, height);
            combineop.Blit(srcData, tmpData, AlphaComposite.SrcOver, null, srcx, srcy, 0, 0, width, height);
            blitop.Blit(tmpData, dstData, comp, clip, 0, 0, dstx, dsty, width, height);
        }
    }

    private static class TraceBlitBg
    extends BlitBg {
        BlitBg target;

        public TraceBlitBg(BlitBg target) {
            super(target.getSourceType(), target.getCompositeType(), target.getDestType());
            this.target = target;
        }

        @Override
        public GraphicsPrimitive traceWrap() {
            return this;
        }

        @Override
        public void BlitBg(SurfaceData src, SurfaceData dst, Composite comp, Region clip, int bgColor, int srcx, int srcy, int dstx, int dsty, int width, int height) {
            TraceBlitBg.tracePrimitive(this.target);
            this.target.BlitBg(src, dst, comp, clip, bgColor, srcx, srcy, dstx, dsty, width, height);
        }
    }
}

