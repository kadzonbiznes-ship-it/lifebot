/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import java.awt.Composite;
import java.awt.image.BufferedImage;
import sun.awt.image.BufImgSurfaceData;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.FillRect;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.MaskBlit;
import sun.java2d.loops.RenderCache;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;

public class MaskFill
extends GraphicsPrimitive {
    public static final String methodSignature = "MaskFill(...)".toString();
    public static final String fillPgramSignature = "FillAAPgram(...)".toString();
    public static final String drawPgramSignature = "DrawAAPgram(...)".toString();
    public static final int primTypeID = MaskFill.makePrimTypeID();
    private static RenderCache fillcache = new RenderCache(10);

    public static MaskFill locate(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        return (MaskFill)GraphicsPrimitiveMgr.locate(primTypeID, srctype, comptype, dsttype);
    }

    public static MaskFill locatePrim(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        return (MaskFill)GraphicsPrimitiveMgr.locatePrim(primTypeID, srctype, comptype, dsttype);
    }

    public static MaskFill getFromCache(SurfaceType src, CompositeType comp, SurfaceType dst) {
        Object o = fillcache.get(src, comp, dst);
        if (o != null) {
            return (MaskFill)o;
        }
        MaskFill fill = MaskFill.locatePrim(src, comp, dst);
        if (fill != null) {
            fillcache.put(src, comp, dst, fill);
        }
        return fill;
    }

    protected MaskFill(String alternateSignature, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(alternateSignature, primTypeID, srctype, comptype, dsttype);
    }

    protected MaskFill(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public MaskFill(long pNativePrim, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public native void MaskFill(SunGraphics2D var1, SurfaceData var2, Composite var3, int var4, int var5, int var6, int var7, byte[] var8, int var9, int var10);

    public native void FillAAPgram(SunGraphics2D var1, SurfaceData var2, Composite var3, double var4, double var6, double var8, double var10, double var12, double var14);

    public native void DrawAAPgram(SunGraphics2D var1, SurfaceData var2, Composite var3, double var4, double var6, double var8, double var10, double var12, double var14, double var16, double var18);

    public boolean canDoParallelograms() {
        return this.getNativePrim() != 0L;
    }

    @Override
    protected GraphicsPrimitive makePrimitive(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        if (SurfaceType.OpaqueColor.equals(srctype) || SurfaceType.AnyColor.equals(srctype)) {
            if (CompositeType.Xor.equals(comptype)) {
                throw new InternalError("Cannot construct MaskFill for XOR mode");
            }
            return new General(srctype, comptype, dsttype);
        }
        throw new InternalError("MaskFill can only fill with colors");
    }

    @Override
    public GraphicsPrimitive traceWrap() {
        return new TraceMaskFill(this);
    }

    static {
        GraphicsPrimitiveMgr.GeneralPrimitives.register(new MaskFill(null, null, null));
    }

    private static class General
    extends MaskFill {
        FillRect fillop;
        MaskBlit maskop;

        public General(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
            super(srctype, comptype, dsttype);
            this.fillop = FillRect.locate(srctype, CompositeType.SrcNoEa, SurfaceType.IntArgb);
            this.maskop = MaskBlit.locate(SurfaceType.IntArgb, comptype, dsttype);
        }

        @Override
        public void MaskFill(SunGraphics2D sg2d, SurfaceData sData, Composite comp, int x, int y, int w, int h, byte[] mask, int offset, int scan) {
            BufferedImage dstBI = new BufferedImage(w, h, 2);
            SurfaceData tmpData = BufImgSurfaceData.createData(dstBI);
            Region clip = sg2d.clipRegion;
            sg2d.clipRegion = null;
            int pixel = sg2d.pixel;
            sg2d.pixel = tmpData.pixelFor(sg2d.getColor());
            this.fillop.FillRect(sg2d, tmpData, 0, 0, w, h);
            sg2d.pixel = pixel;
            sg2d.clipRegion = clip;
            this.maskop.MaskBlit(tmpData, sData, comp, null, 0, 0, x, y, w, h, mask, offset, scan);
        }
    }

    private static class TraceMaskFill
    extends MaskFill {
        MaskFill target;
        MaskFill fillPgramTarget;
        MaskFill drawPgramTarget;

        public TraceMaskFill(MaskFill target) {
            super(target.getSourceType(), target.getCompositeType(), target.getDestType());
            this.target = target;
            this.fillPgramTarget = new MaskFill(fillPgramSignature, target.getSourceType(), target.getCompositeType(), target.getDestType());
            this.drawPgramTarget = new MaskFill(drawPgramSignature, target.getSourceType(), target.getCompositeType(), target.getDestType());
        }

        @Override
        public GraphicsPrimitive traceWrap() {
            return this;
        }

        @Override
        public void MaskFill(SunGraphics2D sg2d, SurfaceData sData, Composite comp, int x, int y, int w, int h, byte[] mask, int maskoff, int maskscan) {
            TraceMaskFill.tracePrimitive(this.target);
            this.target.MaskFill(sg2d, sData, comp, x, y, w, h, mask, maskoff, maskscan);
        }

        @Override
        public void FillAAPgram(SunGraphics2D sg2d, SurfaceData sData, Composite comp, double x, double y, double dx1, double dy1, double dx2, double dy2) {
            TraceMaskFill.tracePrimitive(this.fillPgramTarget);
            this.target.FillAAPgram(sg2d, sData, comp, x, y, dx1, dy1, dx2, dy2);
        }

        @Override
        public void DrawAAPgram(SunGraphics2D sg2d, SurfaceData sData, Composite comp, double x, double y, double dx1, double dy1, double dx2, double dy2, double lw1, double lw2) {
            TraceMaskFill.tracePrimitive(this.drawPgramTarget);
            this.target.DrawAAPgram(sg2d, sData, comp, x, y, dx1, dy1, dx2, dy2, lw1, lw2);
        }

        @Override
        public boolean canDoParallelograms() {
            return this.target.canDoParallelograms();
        }
    }
}

