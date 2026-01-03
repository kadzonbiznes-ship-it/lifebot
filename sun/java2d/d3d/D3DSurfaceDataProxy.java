/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import java.awt.Color;
import sun.java2d.InvalidPipeException;
import sun.java2d.SurfaceData;
import sun.java2d.SurfaceDataProxy;
import sun.java2d.d3d.D3DGraphicsConfig;
import sun.java2d.d3d.D3DGraphicsDevice;
import sun.java2d.d3d.D3DSurfaceData;
import sun.java2d.loops.CompositeType;

public class D3DSurfaceDataProxy
extends SurfaceDataProxy {
    D3DGraphicsConfig d3dgc;
    int transparency;

    public static SurfaceDataProxy createProxy(SurfaceData srcData, D3DGraphicsConfig dstConfig) {
        if (srcData instanceof D3DSurfaceData) {
            return UNCACHED;
        }
        return new D3DSurfaceDataProxy(dstConfig, srcData.getTransparency());
    }

    public D3DSurfaceDataProxy(D3DGraphicsConfig d3dgc, int transparency) {
        this.d3dgc = d3dgc;
        this.transparency = transparency;
        this.activateDisplayListener();
    }

    @Override
    public SurfaceData validateSurfaceData(SurfaceData srcData, SurfaceData cachedData, int w, int h) {
        block3: {
            if (cachedData == null || cachedData.isSurfaceLost()) {
                try {
                    cachedData = this.d3dgc.createManagedSurface(w, h, this.transparency);
                }
                catch (InvalidPipeException e) {
                    if (D3DGraphicsDevice.isD3DAvailable()) break block3;
                    this.invalidate();
                    this.flush();
                    return null;
                }
            }
        }
        return cachedData;
    }

    @Override
    public boolean isSupportedOperation(SurfaceData srcData, int txtype, CompositeType comp, Color bgColor) {
        return bgColor == null || this.transparency == 1;
    }
}

