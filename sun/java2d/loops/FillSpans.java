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
import sun.java2d.pipe.SpanIterator;

public class FillSpans
extends GraphicsPrimitive {
    public static final String methodSignature = "FillSpans(...)".toString();
    public static final int primTypeID = FillSpans.makePrimTypeID();

    public static FillSpans locate(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        return (FillSpans)GraphicsPrimitiveMgr.locate(primTypeID, srctype, comptype, dsttype);
    }

    protected FillSpans(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public FillSpans(long pNativePrim, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    private native void FillSpans(SunGraphics2D var1, SurfaceData var2, int var3, long var4, SpanIterator var6);

    public void FillSpans(SunGraphics2D sg2d, SurfaceData dest, SpanIterator si) {
        this.FillSpans(sg2d, dest, sg2d.pixel, si.getNativeIterator(), si);
    }

    @Override
    public GraphicsPrimitive traceWrap() {
        return new TraceFillSpans(this);
    }

    private static class TraceFillSpans
    extends FillSpans {
        FillSpans target;

        public TraceFillSpans(FillSpans target) {
            super(target.getSourceType(), target.getCompositeType(), target.getDestType());
            this.target = target;
        }

        @Override
        public GraphicsPrimitive traceWrap() {
            return this;
        }

        @Override
        public void FillSpans(SunGraphics2D sg2d, SurfaceData dest, SpanIterator si) {
            TraceFillSpans.tracePrimitive(this.target);
            this.target.FillSpans(sg2d, dest, si);
        }
    }
}

