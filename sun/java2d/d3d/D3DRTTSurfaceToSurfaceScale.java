/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import java.awt.Composite;
import sun.java2d.SurfaceData;
import sun.java2d.d3d.D3DBlitLoops;
import sun.java2d.d3d.D3DSurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.ScaledBlit;
import sun.java2d.pipe.Region;

class D3DRTTSurfaceToSurfaceScale
extends ScaledBlit {
    D3DRTTSurfaceToSurfaceScale() {
        super(D3DSurfaceData.D3DSurfaceRTT, CompositeType.AnyAlpha, D3DSurfaceData.D3DSurface);
    }

    @Override
    public void Scale(SurfaceData src, SurfaceData dst, Composite comp, Region clip, int sx1, int sy1, int sx2, int sy2, double dx1, double dy1, double dx2, double dy2) {
        D3DBlitLoops.IsoBlit(src, dst, null, null, comp, clip, null, 1, sx1, sy1, sx2, sy2, dx1, dy1, dx2, dy2, true);
    }
}

