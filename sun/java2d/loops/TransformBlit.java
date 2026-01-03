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
import sun.java2d.loops.RenderCache;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;

public class TransformBlit
extends GraphicsPrimitive {
    public static final String methodSignature = "TransformBlit(...)".toString();
    public static final int primTypeID = TransformBlit.makePrimTypeID();
    private static RenderCache blitcache = new RenderCache(10);

    public static TransformBlit locate(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        return (TransformBlit)GraphicsPrimitiveMgr.locate(primTypeID, srctype, comptype, dsttype);
    }

    public static TransformBlit getFromCache(SurfaceType src, CompositeType comp, SurfaceType dst) {
        Object o = blitcache.get(src, comp, dst);
        if (o != null) {
            return (TransformBlit)o;
        }
        TransformBlit blit = TransformBlit.locate(src, comp, dst);
        if (blit != null) {
            blitcache.put(src, comp, dst, blit);
        }
        return blit;
    }

    protected TransformBlit(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public TransformBlit(long pNativePrim, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public native void Transform(SurfaceData var1, SurfaceData var2, Composite var3, Region var4, AffineTransform var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12);

    @Override
    public GraphicsPrimitive traceWrap() {
        return new TraceTransformBlit(this);
    }

    private static class TraceTransformBlit
    extends TransformBlit {
        TransformBlit target;

        public TraceTransformBlit(TransformBlit target) {
            super(target.getSourceType(), target.getCompositeType(), target.getDestType());
            this.target = target;
        }

        @Override
        public GraphicsPrimitive traceWrap() {
            return this;
        }

        @Override
        public void Transform(SurfaceData src, SurfaceData dst, Composite comp, Region clip, AffineTransform at, int hint, int srcx, int srcy, int dstx, int dsty, int width, int height) {
            TraceTransformBlit.tracePrimitive(this.target);
            this.target.Transform(src, dst, comp, clip, at, hint, srcx, srcy, dstx, dsty, width, height);
        }
    }
}

