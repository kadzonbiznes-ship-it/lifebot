/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.MaskFill;
import sun.java2d.loops.SurfaceType;

public class FillRect
extends GraphicsPrimitive {
    public static final String methodSignature = "FillRect(...)".toString();
    public static final int primTypeID = FillRect.makePrimTypeID();

    public static FillRect locate(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        return (FillRect)GraphicsPrimitiveMgr.locate(primTypeID, srctype, comptype, dsttype);
    }

    protected FillRect(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public FillRect(long pNativePrim, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public native void FillRect(SunGraphics2D var1, SurfaceData var2, int var3, int var4, int var5, int var6);

    @Override
    protected GraphicsPrimitive makePrimitive(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        return new General(srctype, comptype, dsttype);
    }

    @Override
    public GraphicsPrimitive traceWrap() {
        return new TraceFillRect(this);
    }

    static {
        GraphicsPrimitiveMgr.GeneralPrimitives.register(new FillRect(null, null, null));
    }

    public static class General
    extends FillRect {
        public MaskFill fillop;

        public General(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
            super(srctype, comptype, dsttype);
            this.fillop = MaskFill.locate(srctype, comptype, dsttype);
        }

        @Override
        public void FillRect(SunGraphics2D sg2d, SurfaceData dest, int x, int y, int w, int h) {
            this.fillop.MaskFill(sg2d, dest, sg2d.composite, x, y, w, h, null, 0, 0);
        }
    }

    private static class TraceFillRect
    extends FillRect {
        FillRect target;

        public TraceFillRect(FillRect target) {
            super(target.getSourceType(), target.getCompositeType(), target.getDestType());
            this.target = target;
        }

        @Override
        public GraphicsPrimitive traceWrap() {
            return this;
        }

        @Override
        public void FillRect(SunGraphics2D sg2d, SurfaceData dest, int x, int y, int w, int h) {
            TraceFillRect.tracePrimitive(this.target);
            this.target.FillRect(sg2d, dest, x, y, w, h);
        }
    }
}

