/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import java.awt.Composite;
import sun.java2d.SurfaceData;
import sun.java2d.d3d.D3DSurfaceData;
import sun.java2d.d3d.D3DVolatileSurfaceManager;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.ScaledBlit;
import sun.java2d.pipe.Region;
import sun.java2d.windows.GDIWindowSurfaceData;

class D3DSurfaceToGDIWindowSurfaceScale
extends ScaledBlit {
    D3DSurfaceToGDIWindowSurfaceScale() {
        super(D3DSurfaceData.D3DSurface, CompositeType.AnyAlpha, GDIWindowSurfaceData.AnyGdi);
    }

    @Override
    public void Scale(SurfaceData src, SurfaceData dst, Composite comp, Region clip, int sx1, int sy1, int sx2, int sy2, double dx1, double dy1, double dx2, double dy2) {
        D3DVolatileSurfaceManager.handleVItoScreenOp(src, dst);
    }
}

