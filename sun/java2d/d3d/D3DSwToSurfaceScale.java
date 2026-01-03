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
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;

class D3DSwToSurfaceScale
extends ScaledBlit {
    private int typeval;

    D3DSwToSurfaceScale(SurfaceType srcType, int typeval) {
        super(srcType, CompositeType.AnyAlpha, D3DSurfaceData.D3DSurface);
        this.typeval = typeval;
    }

    @Override
    public void Scale(SurfaceData src, SurfaceData dst, Composite comp, Region clip, int sx1, int sy1, int sx2, int sy2, double dx1, double dy1, double dx2, double dy2) {
        D3DBlitLoops.Blit(src, dst, comp, clip, null, 1, sx1, sy1, sx2, sy2, dx1, dy1, dx2, dy2, this.typeval, false);
    }
}

