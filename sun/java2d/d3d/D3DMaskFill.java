/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import java.awt.Composite;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.d3d.D3DContext;
import sun.java2d.d3d.D3DRenderQueue;
import sun.java2d.d3d.D3DSurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.BufferedMaskFill;

class D3DMaskFill
extends BufferedMaskFill {
    static void register() {
        GraphicsPrimitive[] primitives = new GraphicsPrimitive[]{new D3DMaskFill(SurfaceType.AnyColor, CompositeType.SrcOver), new D3DMaskFill(SurfaceType.OpaqueColor, CompositeType.SrcNoEa), new D3DMaskFill(SurfaceType.GradientPaint, CompositeType.SrcOver), new D3DMaskFill(SurfaceType.OpaqueGradientPaint, CompositeType.SrcNoEa), new D3DMaskFill(SurfaceType.LinearGradientPaint, CompositeType.SrcOver), new D3DMaskFill(SurfaceType.OpaqueLinearGradientPaint, CompositeType.SrcNoEa), new D3DMaskFill(SurfaceType.RadialGradientPaint, CompositeType.SrcOver), new D3DMaskFill(SurfaceType.OpaqueRadialGradientPaint, CompositeType.SrcNoEa), new D3DMaskFill(SurfaceType.TexturePaint, CompositeType.SrcOver), new D3DMaskFill(SurfaceType.OpaqueTexturePaint, CompositeType.SrcNoEa)};
        GraphicsPrimitiveMgr.register(primitives);
    }

    protected D3DMaskFill(SurfaceType srcType, CompositeType compType) {
        super(D3DRenderQueue.getInstance(), srcType, compType, D3DSurfaceData.D3DSurface);
    }

    @Override
    protected native void maskFill(int var1, int var2, int var3, int var4, int var5, int var6, int var7, byte[] var8);

    @Override
    protected void validateContext(SunGraphics2D sg2d, Composite comp, int ctxflags) {
        D3DSurfaceData dstData = SurfaceData.convertTo(D3DSurfaceData.class, sg2d.surfaceData);
        D3DContext.validateContext(dstData, dstData, sg2d.getCompClip(), comp, null, sg2d.paint, sg2d, ctxflags);
    }
}

