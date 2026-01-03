/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import java.awt.Composite;
import sun.java2d.SurfaceData;
import sun.java2d.d3d.D3DBlitLoops;
import sun.java2d.d3d.D3DSurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.pipe.Region;

class D3DSurfaceToSurfaceBlit
extends Blit {
    D3DSurfaceToSurfaceBlit() {
        super(D3DSurfaceData.D3DSurface, CompositeType.AnyAlpha, D3DSurfaceData.D3DSurface);
    }

    @Override
    public void Blit(SurfaceData src, SurfaceData dst, Composite comp, Region clip, int sx, int sy, int dx, int dy, int w, int h) {
        D3DBlitLoops.IsoBlit(src, dst, null, null, comp, clip, null, 1, sx, sy, sx + w, sy + h, dx, dy, dx + w, dy + h, false);
    }
}

