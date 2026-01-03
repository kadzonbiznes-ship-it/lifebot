/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import java.awt.Composite;
import sun.java2d.SurfaceData;
import sun.java2d.d3d.D3DSurfaceData;
import sun.java2d.d3d.D3DVolatileSurfaceManager;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.pipe.Region;
import sun.java2d.windows.GDIWindowSurfaceData;

class D3DSurfaceToGDIWindowSurfaceBlit
extends Blit {
    D3DSurfaceToGDIWindowSurfaceBlit() {
        super(D3DSurfaceData.D3DSurface, CompositeType.AnyAlpha, GDIWindowSurfaceData.AnyGdi);
    }

    @Override
    public void Blit(SurfaceData src, SurfaceData dst, Composite comp, Region clip, int sx, int sy, int dx, int dy, int w, int h) {
        D3DVolatileSurfaceManager.handleVItoScreenOp(src, dst);
    }
}

