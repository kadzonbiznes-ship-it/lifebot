/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import java.awt.Composite;
import sun.java2d.SurfaceData;
import sun.java2d.d3d.D3DContext;
import sun.java2d.d3d.D3DRenderQueue;
import sun.java2d.d3d.D3DSurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.BufferedMaskBlit;
import sun.java2d.pipe.Region;

class D3DMaskBlit
extends BufferedMaskBlit {
    static void register() {
        GraphicsPrimitive[] primitives = new GraphicsPrimitive[]{new D3DMaskBlit(SurfaceType.IntArgb, CompositeType.SrcOver), new D3DMaskBlit(SurfaceType.IntArgbPre, CompositeType.SrcOver), new D3DMaskBlit(SurfaceType.IntRgb, CompositeType.SrcOver), new D3DMaskBlit(SurfaceType.IntRgb, CompositeType.SrcNoEa), new D3DMaskBlit(SurfaceType.IntBgr, CompositeType.SrcOver), new D3DMaskBlit(SurfaceType.IntBgr, CompositeType.SrcNoEa)};
        GraphicsPrimitiveMgr.register(primitives);
    }

    private D3DMaskBlit(SurfaceType srcType, CompositeType compType) {
        super(D3DRenderQueue.getInstance(), srcType, compType, D3DSurfaceData.D3DSurface);
    }

    @Override
    protected void validateContext(SurfaceData dstData, Composite comp, Region clip) {
        D3DSurfaceData d3dDst = (D3DSurfaceData)dstData;
        D3DContext.validateContext(d3dDst, d3dDst, clip, comp, null, null, null, 0);
    }
}

