/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import java.awt.Composite;
import java.lang.ref.WeakReference;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.RenderCache;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;

public class MaskBlit
extends GraphicsPrimitive {
    public static final String methodSignature = "MaskBlit(...)".toString();
    public static final int primTypeID = MaskBlit.makePrimTypeID();
    private static RenderCache blitcache = new RenderCache(20);

    public static MaskBlit locate(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        return (MaskBlit)GraphicsPrimitiveMgr.locate(primTypeID, srctype, comptype, dsttype);
    }

    public static MaskBlit getFromCache(SurfaceType src, CompositeType comp, SurfaceType dst) {
        Object o = blitcache.get(src, comp, dst);
        if (o != null) {
            return (MaskBlit)o;
        }
        MaskBlit blit = MaskBlit.locate(src, comp, dst);
        if (blit == null) {
            System.out.println("mask blit loop not found for:");
            System.out.println("src:  " + String.valueOf(src));
            System.out.println("comp: " + String.valueOf(comp));
            System.out.println("dst:  " + String.valueOf(dst));
        } else {
            blitcache.put(src, comp, dst, blit);
        }
        return blit;
    }

    protected MaskBlit(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public MaskBlit(long pNativePrim, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public native void MaskBlit(SurfaceData var1, SurfaceData var2, Composite var3, Region var4, int var5, int var6, int var7, int var8, int var9, int var10, byte[] var11, int var12, int var13);

    @Override
    protected GraphicsPrimitive makePrimitive(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        if (CompositeType.Xor.equals(comptype)) {
            throw new InternalError("Cannot construct MaskBlit for XOR mode");
        }
        General ob = new General(srctype, comptype, dsttype);
        this.setupGeneralBinaryOp(ob);
        return ob;
    }

    @Override
    public GraphicsPrimitive traceWrap() {
        return new TraceMaskBlit(this);
    }

    static {
        GraphicsPrimitiveMgr.GeneralPrimitives.register(new MaskBlit(null, null, null));
    }

    private static class General
    extends MaskBlit
    implements GraphicsPrimitive.GeneralBinaryOp {
        Blit convertsrc;
        Blit convertdst;
        MaskBlit performop;
        Blit convertresult;
        WeakReference<SurfaceData> srcTmp;
        WeakReference<SurfaceData> dstTmp;

        public General(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
            super(srctype, comptype, dsttype);
        }

        @Override
        public void setPrimitives(Blit srcconverter, Blit dstconverter, GraphicsPrimitive genericop, Blit resconverter) {
            this.convertsrc = srcconverter;
            this.convertdst = dstconverter;
            this.performop = (MaskBlit)genericop;
            this.convertresult = resconverter;
        }

        @Override
        public synchronized void MaskBlit(SurfaceData srcData, SurfaceData dstData, Composite comp, Region clip, int srcx, int srcy, int dstx, int dsty, int width, int height, byte[] mask, int offset, int scan) {
            Region opclip;
            int dy;
            int dx;
            SurfaceData dst;
            int sy;
            int sx;
            SurfaceData src;
            if (this.convertsrc == null) {
                src = srcData;
                sx = srcx;
                sy = srcy;
            } else {
                SurfaceData cachedSrc = null;
                if (this.srcTmp != null) {
                    cachedSrc = (SurfaceData)this.srcTmp.get();
                }
                src = General.convertFrom(this.convertsrc, srcData, srcx, srcy, width, height, cachedSrc);
                sx = 0;
                sy = 0;
                if (src != cachedSrc) {
                    this.srcTmp = new WeakReference<SurfaceData>(src);
                }
            }
            if (this.convertdst == null) {
                dst = dstData;
                dx = dstx;
                dy = dsty;
                opclip = clip;
            } else {
                SurfaceData cachedDst = null;
                if (this.dstTmp != null) {
                    cachedDst = (SurfaceData)this.dstTmp.get();
                }
                dst = General.convertFrom(this.convertdst, dstData, dstx, dsty, width, height, cachedDst);
                dx = 0;
                dy = 0;
                opclip = null;
                if (dst != cachedDst) {
                    this.dstTmp = new WeakReference<SurfaceData>(dst);
                }
            }
            this.performop.MaskBlit(src, dst, comp, opclip, sx, sy, dx, dy, width, height, mask, offset, scan);
            if (this.convertresult != null) {
                General.convertTo(this.convertresult, dst, dstData, clip, dstx, dsty, width, height);
            }
        }
    }

    private static class TraceMaskBlit
    extends MaskBlit {
        MaskBlit target;

        public TraceMaskBlit(MaskBlit target) {
            super(target.getNativePrim(), target.getSourceType(), target.getCompositeType(), target.getDestType());
            this.target = target;
        }

        @Override
        public GraphicsPrimitive traceWrap() {
            return this;
        }

        @Override
        public void MaskBlit(SurfaceData src, SurfaceData dst, Composite comp, Region clip, int srcx, int srcy, int dstx, int dsty, int width, int height, byte[] mask, int maskoff, int maskscan) {
            TraceMaskBlit.tracePrimitive(this.target);
            this.target.MaskBlit(src, dst, comp, clip, srcx, srcy, dstx, dsty, width, height, mask, maskoff, maskscan);
        }
    }
}

