/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.SurfaceType;

public class FillParallelogram
extends GraphicsPrimitive {
    public static final String methodSignature = "FillParallelogram(...)".toString();
    public static final int primTypeID = FillParallelogram.makePrimTypeID();

    public static FillParallelogram locate(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        return (FillParallelogram)GraphicsPrimitiveMgr.locate(primTypeID, srctype, comptype, dsttype);
    }

    protected FillParallelogram(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public FillParallelogram(long pNativePrim, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public native void FillParallelogram(SunGraphics2D var1, SurfaceData var2, double var3, double var5, double var7, double var9, double var11, double var13);

    @Override
    public GraphicsPrimitive traceWrap() {
        return new TraceFillParallelogram(this);
    }

    private static class TraceFillParallelogram
    extends FillParallelogram {
        FillParallelogram target;

        public TraceFillParallelogram(FillParallelogram target) {
            super(target.getSourceType(), target.getCompositeType(), target.getDestType());
            this.target = target;
        }

        @Override
        public GraphicsPrimitive traceWrap() {
            return this;
        }

        @Override
        public void FillParallelogram(SunGraphics2D sg2d, SurfaceData dest, double x0, double y0, double dx1, double dy1, double dx2, double dy2) {
            TraceFillParallelogram.tracePrimitive(this.target);
            this.target.FillParallelogram(sg2d, dest, x0, y0, dx1, dy1, dx2, dy2);
        }
    }
}

