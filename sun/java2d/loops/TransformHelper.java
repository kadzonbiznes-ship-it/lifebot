/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import java.awt.Composite;
import java.awt.geom.AffineTransform;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.MaskBlit;
import sun.java2d.loops.RenderCache;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;

public class TransformHelper
extends GraphicsPrimitive {
    public static final String methodSignature = "TransformHelper(...)".toString();
    public static final int primTypeID = TransformHelper.makePrimTypeID();
    private static RenderCache helpercache = new RenderCache(10);

    public static TransformHelper locate(SurfaceType srctype) {
        return (TransformHelper)GraphicsPrimitiveMgr.locate(primTypeID, srctype, CompositeType.SrcNoEa, SurfaceType.IntArgbPre);
    }

    public static synchronized TransformHelper getFromCache(SurfaceType src) {
        Object o = helpercache.get(src, null, null);
        if (o != null) {
            return (TransformHelper)o;
        }
        TransformHelper helper = TransformHelper.locate(src);
        if (helper != null) {
            helpercache.put(src, null, null, helper);
        }
        return helper;
    }

    protected TransformHelper(SurfaceType srctype) {
        super(methodSignature, primTypeID, srctype, CompositeType.SrcNoEa, SurfaceType.IntArgbPre);
    }

    public TransformHelper(long pNativePrim, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public native void Transform(MaskBlit var1, SurfaceData var2, SurfaceData var3, Composite var4, Region var5, AffineTransform var6, int var7, int var8, int var9, int var10, int var11, int var12, int var13, int var14, int var15, int[] var16, int var17, int var18);

    @Override
    public GraphicsPrimitive traceWrap() {
        return new TraceTransformHelper(this);
    }

    private static class TraceTransformHelper
    extends TransformHelper {
        TransformHelper target;

        public TraceTransformHelper(TransformHelper target) {
            super(target.getSourceType());
            this.target = target;
        }

        @Override
        public GraphicsPrimitive traceWrap() {
            return this;
        }

        @Override
        public void Transform(MaskBlit output, SurfaceData src, SurfaceData dst, Composite comp, Region clip, AffineTransform itx, int txtype, int sx1, int sy1, int sx2, int sy2, int dx1, int dy1, int dx2, int dy2, int[] edges, int dxoff, int dyoff) {
            TraceTransformHelper.tracePrimitive(this.target);
            this.target.Transform(output, src, dst, comp, clip, itx, txtype, sx1, sy1, sx2, sy2, dx1, dy1, dx2, dy2, edges, dxoff, dyoff);
        }
    }
}

