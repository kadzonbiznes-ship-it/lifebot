/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.windows;

import java.awt.Composite;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;
import sun.java2d.windows.GDIWindowSurfaceData;

public class GDIBlitLoops
extends Blit {
    int rmask;
    int gmask;
    int bmask;
    boolean indexed = false;

    public static void register() {
        GraphicsPrimitive[] primitives = new GraphicsPrimitive[]{new GDIBlitLoops(SurfaceType.IntRgb, GDIWindowSurfaceData.AnyGdi), new GDIBlitLoops(SurfaceType.Ushort555Rgb, GDIWindowSurfaceData.AnyGdi, 31744, 992, 31), new GDIBlitLoops(SurfaceType.Ushort565Rgb, GDIWindowSurfaceData.AnyGdi, 63488, 2016, 31), new GDIBlitLoops(SurfaceType.ThreeByteBgr, GDIWindowSurfaceData.AnyGdi), new GDIBlitLoops(SurfaceType.ByteIndexedOpaque, GDIWindowSurfaceData.AnyGdi, true), new GDIBlitLoops(SurfaceType.Index8Gray, GDIWindowSurfaceData.AnyGdi, true), new GDIBlitLoops(SurfaceType.ByteGray, GDIWindowSurfaceData.AnyGdi)};
        GraphicsPrimitiveMgr.register(primitives);
    }

    public GDIBlitLoops(SurfaceType srcType, SurfaceType dstType) {
        this(srcType, dstType, 0, 0, 0);
    }

    public GDIBlitLoops(SurfaceType srcType, SurfaceType dstType, boolean indexed) {
        this(srcType, dstType, 0, 0, 0);
        this.indexed = indexed;
    }

    public GDIBlitLoops(SurfaceType srcType, SurfaceType dstType, int rmask, int gmask, int bmask) {
        super(srcType, CompositeType.SrcNoEa, dstType);
        this.rmask = rmask;
        this.gmask = gmask;
        this.bmask = bmask;
    }

    public native void nativeBlit(SurfaceData var1, SurfaceData var2, Region var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12, boolean var13);

    @Override
    public void Blit(SurfaceData src, SurfaceData dst, Composite comp, Region clip, int sx, int sy, int dx, int dy, int w, int h) {
        this.nativeBlit(src, dst, clip, sx, sy, dx, dy, w, h, this.rmask, this.gmask, this.bmask, this.indexed);
    }
}

