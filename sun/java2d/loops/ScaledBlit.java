/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import java.awt.Composite;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.RenderCache;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;

public class ScaledBlit
extends GraphicsPrimitive {
    public static final String methodSignature = "ScaledBlit(...)".toString();
    public static final int primTypeID = ScaledBlit.makePrimTypeID();
    private static RenderCache blitcache = new RenderCache(20);

    public static ScaledBlit locate(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        return (ScaledBlit)GraphicsPrimitiveMgr.locate(primTypeID, srctype, comptype, dsttype);
    }

    public static ScaledBlit getFromCache(SurfaceType src, CompositeType comp, SurfaceType dst) {
        Object o = blitcache.get(src, comp, dst);
        if (o != null) {
            return (ScaledBlit)o;
        }
        ScaledBlit blit = ScaledBlit.locate(src, comp, dst);
        if (blit != null) {
            blitcache.put(src, comp, dst, blit);
        }
        return blit;
    }

    protected ScaledBlit(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public ScaledBlit(long pNativePrim, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public native void Scale(SurfaceData var1, SurfaceData var2, Composite var3, Region var4, int var5, int var6, int var7, int var8, double var9, double var11, double var13, double var15);

    @Override
    public GraphicsPrimitive traceWrap() {
        return new TraceScaledBlit(this);
    }

    private static class TraceScaledBlit
    extends ScaledBlit {
        ScaledBlit target;

        public TraceScaledBlit(ScaledBlit target) {
            super(target.getSourceType(), target.getCompositeType(), target.getDestType());
            this.target = target;
        }

        @Override
        public GraphicsPrimitive traceWrap() {
            return this;
        }

        @Override
        public void Scale(SurfaceData src, SurfaceData dst, Composite comp, Region clip, int sx1, int sy1, int sx2, int sy2, double dx1, double dy1, double dx2, double dy2) {
            TraceScaledBlit.tracePrimitive(this.target);
            this.target.Scale(src, dst, comp, clip, sx1, sy1, sx2, sy2, dx1, dy1, dx2, dy2);
        }
    }
}

