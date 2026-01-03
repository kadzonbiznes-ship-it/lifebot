/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.lang.ref.WeakReference;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.MaskBlit;
import sun.java2d.loops.RenderCache;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.SpanIterator;

public class Blit
extends GraphicsPrimitive {
    public static final String methodSignature = "Blit(...)".toString();
    public static final int primTypeID = Blit.makePrimTypeID();
    private static RenderCache blitcache = new RenderCache(20);

    public static Blit locate(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        return (Blit)GraphicsPrimitiveMgr.locate(primTypeID, srctype, comptype, dsttype);
    }

    public static Blit getFromCache(SurfaceType src, CompositeType comp, SurfaceType dst) {
        Object o = blitcache.get(src, comp, dst);
        if (o != null) {
            return (Blit)o;
        }
        Blit blit = Blit.locate(src, comp, dst);
        if (blit == null) {
            System.out.println("blit loop not found for:");
            System.out.println("src:  " + String.valueOf(src));
            System.out.println("comp: " + String.valueOf(comp));
            System.out.println("dst:  " + String.valueOf(dst));
        } else {
            blitcache.put(src, comp, dst, blit);
        }
        return blit;
    }

    protected Blit(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public Blit(long pNativePrim, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public native void Blit(SurfaceData var1, SurfaceData var2, Composite var3, Region var4, int var5, int var6, int var7, int var8, int var9, int var10);

    @Override
    protected GraphicsPrimitive makePrimitive(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        if (comptype.isDerivedFrom(CompositeType.Xor)) {
            GeneralXorBlit gxb = new GeneralXorBlit(srctype, comptype, dsttype);
            this.setupGeneralBinaryOp(gxb);
            return gxb;
        }
        if (comptype.isDerivedFrom(CompositeType.AnyAlpha)) {
            return new GeneralMaskBlit(srctype, comptype, dsttype);
        }
        return AnyBlit.instance;
    }

    @Override
    public GraphicsPrimitive traceWrap() {
        return new TraceBlit(this);
    }

    static {
        GraphicsPrimitiveMgr.GeneralPrimitives.register(new Blit(null, null, null));
    }

    private static class GeneralXorBlit
    extends Blit
    implements GraphicsPrimitive.GeneralBinaryOp {
        Blit convertsrc;
        Blit convertdst;
        Blit performop;
        Blit convertresult;
        WeakReference<SurfaceData> srcTmp;
        WeakReference<SurfaceData> dstTmp;

        public GeneralXorBlit(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
            super(srctype, comptype, dsttype);
        }

        @Override
        public void setPrimitives(Blit srcconverter, Blit dstconverter, GraphicsPrimitive genericop, Blit resconverter) {
            this.convertsrc = srcconverter;
            this.convertdst = dstconverter;
            this.performop = (Blit)genericop;
            this.convertresult = resconverter;
        }

        @Override
        public synchronized void Blit(SurfaceData srcData, SurfaceData dstData, Composite comp, Region clip, int srcx, int srcy, int dstx, int dsty, int width, int height) {
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
                src = GeneralXorBlit.convertFrom(this.convertsrc, srcData, srcx, srcy, width, height, cachedSrc);
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
                dst = GeneralXorBlit.convertFrom(this.convertdst, dstData, dstx, dsty, width, height, cachedDst);
                dx = 0;
                dy = 0;
                opclip = null;
                if (dst != cachedDst) {
                    this.dstTmp = new WeakReference<SurfaceData>(dst);
                }
            }
            this.performop.Blit(src, dst, comp, opclip, sx, sy, dx, dy, width, height);
            if (this.convertresult != null) {
                GeneralXorBlit.convertTo(this.convertresult, dst, dstData, clip, dstx, dsty, width, height);
            }
        }
    }

    private static class GeneralMaskBlit
    extends Blit {
        MaskBlit performop;

        public GeneralMaskBlit(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
            super(srctype, comptype, dsttype);
            this.performop = MaskBlit.locate(srctype, comptype, dsttype);
        }

        @Override
        public void Blit(SurfaceData srcData, SurfaceData dstData, Composite comp, Region clip, int srcx, int srcy, int dstx, int dsty, int width, int height) {
            this.performop.MaskBlit(srcData, dstData, comp, clip, srcx, srcy, dstx, dsty, width, height, null, 0, 0);
        }
    }

    private static class AnyBlit
    extends Blit {
        public static AnyBlit instance = new AnyBlit();

        public AnyBlit() {
            super(SurfaceType.Any, CompositeType.Any, SurfaceType.Any);
        }

        @Override
        public void Blit(SurfaceData srcData, SurfaceData dstData, Composite comp, Region clip, int srcx, int srcy, int dstx, int dsty, int width, int height) {
            ColorModel srcCM = srcData.getColorModel();
            ColorModel dstCM = dstData.getColorModel();
            CompositeContext ctx = comp.createContext(srcCM, dstCM, new RenderingHints(null));
            Raster srcRas = srcData.getRaster(srcx, srcy, width, height);
            WritableRaster dstRas = (WritableRaster)dstData.getRaster(dstx, dsty, width, height);
            if (clip == null) {
                clip = Region.getInstanceXYWH(dstx, dsty, width, height);
            }
            int[] span = new int[]{dstx, dsty, dstx + width, dsty + height};
            SpanIterator si = clip.getSpanIterator(span);
            srcx -= dstx;
            srcy -= dsty;
            while (si.nextSpan(span)) {
                int w = span[2] - span[0];
                int h = span[3] - span[1];
                Raster tmpSrcRas = srcRas.createChild(srcx + span[0], srcy + span[1], w, h, 0, 0, null);
                WritableRaster tmpDstRas = dstRas.createWritableChild(span[0], span[1], w, h, 0, 0, null);
                ctx.compose(tmpSrcRas, tmpDstRas, tmpDstRas);
            }
            ctx.dispose();
        }
    }

    private static class TraceBlit
    extends Blit {
        Blit target;

        public TraceBlit(Blit target) {
            super(target.getSourceType(), target.getCompositeType(), target.getDestType());
            this.target = target;
        }

        @Override
        public GraphicsPrimitive traceWrap() {
            return this;
        }

        @Override
        public void Blit(SurfaceData src, SurfaceData dst, Composite comp, Region clip, int srcx, int srcy, int dstx, int dsty, int width, int height) {
            TraceBlit.tracePrimitive(this.target);
            this.target.Blit(src, dst, comp, clip, srcx, srcy, dstx, dsty, width, height);
        }
    }
}

