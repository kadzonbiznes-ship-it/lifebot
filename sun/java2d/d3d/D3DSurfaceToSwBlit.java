/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import java.awt.Composite;
import java.lang.ref.WeakReference;
import sun.java2d.SurfaceData;
import sun.java2d.d3d.D3DContext;
import sun.java2d.d3d.D3DRenderQueue;
import sun.java2d.d3d.D3DSurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.RenderBuffer;

class D3DSurfaceToSwBlit
extends Blit {
    private int typeval;
    private WeakReference<SurfaceData> srcTmp;

    D3DSurfaceToSwBlit(SurfaceType dstType, int typeval) {
        super(D3DSurfaceData.D3DSurface, CompositeType.SrcNoEa, dstType);
        this.typeval = typeval;
    }

    private synchronized void complexClipBlit(SurfaceData src, SurfaceData dst, Composite comp, Region clip, int sx, int sy, int dx, int dy, int w, int h) {
        SurfaceData cachedSrc = null;
        if (this.srcTmp != null) {
            cachedSrc = (SurfaceData)this.srcTmp.get();
        }
        int type = 2;
        src = D3DSurfaceToSwBlit.convertFrom(this, src, sx, sy, w, h, cachedSrc, 2);
        Blit performop = Blit.getFromCache(src.getSurfaceType(), CompositeType.SrcNoEa, dst.getSurfaceType());
        performop.Blit(src, dst, comp, clip, 0, 0, dx, dy, w, h);
        if (src != cachedSrc) {
            this.srcTmp = new WeakReference<SurfaceData>(src);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void Blit(SurfaceData src, SurfaceData dst, Composite comp, Region clip, int sx, int sy, int dx, int dy, int w, int h) {
        if (clip != null) {
            if ((clip = clip.getIntersectionXYWH(dx, dy, w, h)).isEmpty()) {
                return;
            }
            sx += clip.getLoX() - dx;
            sy += clip.getLoY() - dy;
            dx = clip.getLoX();
            dy = clip.getLoY();
            w = clip.getWidth();
            h = clip.getHeight();
            if (!clip.isRectangular()) {
                this.complexClipBlit(src, dst, comp, clip, sx, sy, dx, dy, w, h);
                return;
            }
        }
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            rq.addReference(dst);
            RenderBuffer buf = rq.getBuffer();
            D3DContext.setScratchSurface(((D3DSurfaceData)src).getContext());
            rq.ensureCapacityAndAlignment(48, 32);
            buf.putInt(34);
            buf.putInt(sx).putInt(sy);
            buf.putInt(dx).putInt(dy);
            buf.putInt(w).putInt(h);
            buf.putInt(this.typeval);
            buf.putLong(src.getNativeOps());
            buf.putLong(dst.getNativeOps());
            rq.flushNow();
        }
        finally {
            rq.unlock();
        }
    }
}

