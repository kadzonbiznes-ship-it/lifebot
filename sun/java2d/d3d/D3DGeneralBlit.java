/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import java.awt.Composite;
import java.lang.ref.WeakReference;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;

class D3DGeneralBlit
extends Blit {
    private final Blit performop;
    private WeakReference<SurfaceData> srcTmp;

    D3DGeneralBlit(SurfaceType dstType, CompositeType compType, Blit performop) {
        super(SurfaceType.Any, compType, dstType);
        this.performop = performop;
    }

    @Override
    public synchronized void Blit(SurfaceData src, SurfaceData dst, Composite comp, Region clip, int sx, int sy, int dx, int dy, int w, int h) {
        Blit convertsrc = Blit.getFromCache(src.getSurfaceType(), CompositeType.SrcNoEa, SurfaceType.IntArgbPre);
        SurfaceData cachedSrc = null;
        if (this.srcTmp != null) {
            cachedSrc = (SurfaceData)this.srcTmp.get();
        }
        src = D3DGeneralBlit.convertFrom(convertsrc, src, sx, sy, w, h, cachedSrc, 3);
        this.performop.Blit(src, dst, comp, clip, 0, 0, dx, dy, w, h);
        if (src != cachedSrc) {
            this.srcTmp = new WeakReference<SurfaceData>(src);
        }
    }
}

