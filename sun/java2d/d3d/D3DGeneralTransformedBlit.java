/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import java.awt.Composite;
import java.awt.geom.AffineTransform;
import java.lang.ref.WeakReference;
import sun.java2d.SurfaceData;
import sun.java2d.d3d.D3DSurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.TransformBlit;
import sun.java2d.pipe.Region;

final class D3DGeneralTransformedBlit
extends TransformBlit {
    private final TransformBlit performop;
    private WeakReference<SurfaceData> srcTmp;

    D3DGeneralTransformedBlit(TransformBlit performop) {
        super(SurfaceType.Any, CompositeType.AnyAlpha, D3DSurfaceData.D3DSurface);
        this.performop = performop;
    }

    @Override
    public synchronized void Transform(SurfaceData src, SurfaceData dst, Composite comp, Region clip, AffineTransform at, int hint, int srcx, int srcy, int dstx, int dsty, int width, int height) {
        Blit convertsrc = Blit.getFromCache(src.getSurfaceType(), CompositeType.SrcNoEa, SurfaceType.IntArgbPre);
        SurfaceData cachedSrc = this.srcTmp != null ? (SurfaceData)this.srcTmp.get() : null;
        src = D3DGeneralTransformedBlit.convertFrom(convertsrc, src, srcx, srcy, width, height, cachedSrc, 3);
        this.performop.Transform(src, dst, comp, clip, at, hint, 0, 0, dstx, dsty, width, height);
        if (src != cachedSrc) {
            this.srcTmp = new WeakReference<SurfaceData>(src);
        }
    }
}

