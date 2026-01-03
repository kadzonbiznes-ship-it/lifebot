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

public class DrawParallelogram
extends GraphicsPrimitive {
    public static final String methodSignature = "DrawParallelogram(...)".toString();
    public static final int primTypeID = DrawParallelogram.makePrimTypeID();

    public static DrawParallelogram locate(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        return (DrawParallelogram)GraphicsPrimitiveMgr.locate(primTypeID, srctype, comptype, dsttype);
    }

    protected DrawParallelogram(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public DrawParallelogram(long pNativePrim, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public native void DrawParallelogram(SunGraphics2D var1, SurfaceData var2, double var3, double var5, double var7, double var9, double var11, double var13, double var15, double var17);

    @Override
    public GraphicsPrimitive traceWrap() {
        return new TraceDrawParallelogram(this);
    }

    private static class TraceDrawParallelogram
    extends DrawParallelogram {
        DrawParallelogram target;

        public TraceDrawParallelogram(DrawParallelogram target) {
            super(target.getSourceType(), target.getCompositeType(), target.getDestType());
            this.target = target;
        }

        @Override
        public GraphicsPrimitive traceWrap() {
            return this;
        }

        @Override
        public void DrawParallelogram(SunGraphics2D sg2d, SurfaceData dest, double x, double y, double dx1, double dy1, double dx2, double dy2, double lw1, double lw2) {
            TraceDrawParallelogram.tracePrimitive(this.target);
            this.target.DrawParallelogram(sg2d, dest, x, y, dx1, dy1, dx2, dy2, lw1, lw2);
        }
    }
}

