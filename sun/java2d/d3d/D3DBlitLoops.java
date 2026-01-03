/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import java.awt.Composite;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import sun.java2d.ScreenUpdateManager;
import sun.java2d.SurfaceData;
import sun.java2d.d3d.D3DBufImgOps;
import sun.java2d.d3d.D3DContext;
import sun.java2d.d3d.D3DGeneralBlit;
import sun.java2d.d3d.D3DGeneralTransformedBlit;
import sun.java2d.d3d.D3DRTTSurfaceToSurfaceBlit;
import sun.java2d.d3d.D3DRTTSurfaceToSurfaceScale;
import sun.java2d.d3d.D3DRTTSurfaceToSurfaceTransform;
import sun.java2d.d3d.D3DRenderQueue;
import sun.java2d.d3d.D3DScreenUpdateManager;
import sun.java2d.d3d.D3DSurfaceData;
import sun.java2d.d3d.D3DSurfaceToGDIWindowSurfaceBlit;
import sun.java2d.d3d.D3DSurfaceToGDIWindowSurfaceScale;
import sun.java2d.d3d.D3DSurfaceToGDIWindowSurfaceTransform;
import sun.java2d.d3d.D3DSurfaceToSurfaceBlit;
import sun.java2d.d3d.D3DSurfaceToSurfaceScale;
import sun.java2d.d3d.D3DSurfaceToSurfaceTransform;
import sun.java2d.d3d.D3DSurfaceToSwBlit;
import sun.java2d.d3d.D3DSwToSurfaceBlit;
import sun.java2d.d3d.D3DSwToSurfaceScale;
import sun.java2d.d3d.D3DSwToSurfaceTransform;
import sun.java2d.d3d.D3DSwToTextureBlit;
import sun.java2d.d3d.D3DTextureToSurfaceBlit;
import sun.java2d.d3d.D3DTextureToSurfaceScale;
import sun.java2d.d3d.D3DTextureToSurfaceTransform;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.RenderBuffer;
import sun.java2d.pipe.RenderQueue;

final class D3DBlitLoops {
    private static final int OFFSET_SRCTYPE = 16;
    private static final int OFFSET_HINT = 8;
    private static final int OFFSET_TEXTURE = 3;
    private static final int OFFSET_RTT = 2;
    private static final int OFFSET_XFORM = 1;
    private static final int OFFSET_ISOBLIT = 0;

    D3DBlitLoops() {
    }

    static void register() {
        D3DSwToSurfaceBlit blitIntArgbPreToSurface = new D3DSwToSurfaceBlit(SurfaceType.IntArgbPre, 1);
        D3DSwToTextureBlit blitIntArgbPreToTexture = new D3DSwToTextureBlit(SurfaceType.IntArgbPre, 1);
        D3DSwToSurfaceTransform transformBlitIntArgbPreToSurface = new D3DSwToSurfaceTransform(SurfaceType.IntArgbPre, 1);
        GraphicsPrimitive[] primitives = new GraphicsPrimitive[]{new D3DSurfaceToGDIWindowSurfaceBlit(), new D3DSurfaceToGDIWindowSurfaceScale(), new D3DSurfaceToGDIWindowSurfaceTransform(), new D3DSurfaceToSurfaceBlit(), new D3DSurfaceToSurfaceScale(), new D3DSurfaceToSurfaceTransform(), new D3DRTTSurfaceToSurfaceBlit(), new D3DRTTSurfaceToSurfaceScale(), new D3DRTTSurfaceToSurfaceTransform(), new D3DSurfaceToSwBlit(SurfaceType.IntArgb, 0), blitIntArgbPreToSurface, new D3DSwToSurfaceBlit(SurfaceType.IntArgb, 0), new D3DSwToSurfaceBlit(SurfaceType.IntRgb, 3), new D3DSwToSurfaceBlit(SurfaceType.IntBgr, 4), new D3DSwToSurfaceBlit(SurfaceType.ThreeByteBgr, 9), new D3DSwToSurfaceBlit(SurfaceType.Ushort565Rgb, 5), new D3DSwToSurfaceBlit(SurfaceType.Ushort555Rgb, 6), new D3DSwToSurfaceBlit(SurfaceType.ByteIndexed, 7), new D3DGeneralBlit(D3DSurfaceData.D3DSurface, CompositeType.AnyAlpha, blitIntArgbPreToSurface), new D3DSwToSurfaceScale(SurfaceType.IntArgb, 0), new D3DSwToSurfaceScale(SurfaceType.IntArgbPre, 1), new D3DSwToSurfaceScale(SurfaceType.IntRgb, 3), new D3DSwToSurfaceScale(SurfaceType.IntBgr, 4), new D3DSwToSurfaceScale(SurfaceType.ThreeByteBgr, 9), new D3DSwToSurfaceScale(SurfaceType.Ushort565Rgb, 5), new D3DSwToSurfaceScale(SurfaceType.Ushort555Rgb, 6), new D3DSwToSurfaceScale(SurfaceType.ByteIndexed, 7), new D3DSwToSurfaceTransform(SurfaceType.IntArgb, 0), new D3DSwToSurfaceTransform(SurfaceType.IntRgb, 3), new D3DSwToSurfaceTransform(SurfaceType.IntBgr, 4), new D3DSwToSurfaceTransform(SurfaceType.ThreeByteBgr, 9), new D3DSwToSurfaceTransform(SurfaceType.Ushort565Rgb, 5), new D3DSwToSurfaceTransform(SurfaceType.Ushort555Rgb, 6), new D3DSwToSurfaceTransform(SurfaceType.ByteIndexed, 7), transformBlitIntArgbPreToSurface, new D3DGeneralTransformedBlit(transformBlitIntArgbPreToSurface), new D3DTextureToSurfaceBlit(), new D3DTextureToSurfaceScale(), new D3DTextureToSurfaceTransform(), blitIntArgbPreToTexture, new D3DSwToTextureBlit(SurfaceType.IntRgb, 3), new D3DSwToTextureBlit(SurfaceType.IntArgb, 0), new D3DSwToTextureBlit(SurfaceType.IntBgr, 4), new D3DSwToTextureBlit(SurfaceType.ThreeByteBgr, 9), new D3DSwToTextureBlit(SurfaceType.Ushort565Rgb, 5), new D3DSwToTextureBlit(SurfaceType.Ushort555Rgb, 6), new D3DSwToTextureBlit(SurfaceType.ByteIndexed, 7), new D3DGeneralBlit(D3DSurfaceData.D3DTexture, CompositeType.SrcNoEa, blitIntArgbPreToTexture)};
        GraphicsPrimitiveMgr.register(primitives);
    }

    private static int createPackedParams(boolean isoblit, boolean texture, boolean rtt, boolean xform, int hint, int srctype) {
        return srctype << 16 | hint << 8 | (texture ? 1 : 0) << 3 | (rtt ? 1 : 0) << 2 | (xform ? 1 : 0) << 1 | (isoblit ? 1 : 0) << 0;
    }

    private static void enqueueBlit(RenderQueue rq, SurfaceData src, SurfaceData dst, int packedParams, int sx1, int sy1, int sx2, int sy2, double dx1, double dy1, double dx2, double dy2) {
        RenderBuffer buf = rq.getBuffer();
        rq.ensureCapacityAndAlignment(72, 24);
        buf.putInt(31);
        buf.putInt(packedParams);
        buf.putInt(sx1).putInt(sy1);
        buf.putInt(sx2).putInt(sy2);
        buf.putDouble(dx1).putDouble(dy1);
        buf.putDouble(dx2).putDouble(dy2);
        buf.putLong(src.getNativeOps());
        buf.putLong(dst.getNativeOps());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void Blit(SurfaceData srcData, SurfaceData dstData, Composite comp, Region clip, AffineTransform xform, int hint, int sx1, int sy1, int sx2, int sy2, double dx1, double dy1, double dx2, double dy2, int srctype, boolean texture) {
        int ctxflags = 0;
        if (srcData.getTransparency() == 1) {
            ctxflags |= 1;
        }
        D3DSurfaceData d3dDst = (D3DSurfaceData)dstData;
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            rq.addReference(srcData);
            if (texture) {
                D3DContext.setScratchSurface(d3dDst.getContext());
            } else {
                D3DContext.validateContext(d3dDst, d3dDst, clip, comp, xform, null, null, ctxflags);
            }
            int packedParams = D3DBlitLoops.createPackedParams(false, texture, false, xform != null, hint, srctype);
            D3DBlitLoops.enqueueBlit(rq, srcData, dstData, packedParams, sx1, sy1, sx2, sy2, dx1, dy1, dx2, dy2);
            rq.flushNow();
        }
        finally {
            rq.unlock();
        }
        if (d3dDst.getType() == 1) {
            D3DScreenUpdateManager mgr = (D3DScreenUpdateManager)ScreenUpdateManager.getInstance();
            mgr.runUpdateNow();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void IsoBlit(SurfaceData srcData, SurfaceData dstData, BufferedImage srcImg, BufferedImageOp biop, Composite comp, Region clip, AffineTransform xform, int hint, int sx1, int sy1, int sx2, int sy2, double dx1, double dy1, double dx2, double dy2, boolean texture) {
        int ctxflags = 0;
        if (srcData.getTransparency() == 1) {
            ctxflags |= 1;
        }
        D3DSurfaceData d3dDst = (D3DSurfaceData)dstData;
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        boolean rtt = false;
        rq.lock();
        try {
            D3DSurfaceData d3dSrc = (D3DSurfaceData)srcData;
            int srctype = d3dSrc.getType();
            D3DSurfaceData srcCtxData = d3dSrc;
            rtt = srctype != 3;
            D3DContext.validateContext(srcCtxData, d3dDst, clip, comp, xform, null, null, ctxflags);
            if (biop != null) {
                D3DBufImgOps.enableBufImgOp(rq, d3dSrc, srcImg, biop);
            }
            int packedParams = D3DBlitLoops.createPackedParams(true, texture, rtt, xform != null, hint, 0);
            D3DBlitLoops.enqueueBlit(rq, srcData, dstData, packedParams, sx1, sy1, sx2, sy2, dx1, dy1, dx2, dy2);
            if (biop != null) {
                D3DBufImgOps.disableBufImgOp(rq, biop);
            }
        }
        finally {
            rq.unlock();
        }
        if (rtt && d3dDst.getType() == 1) {
            D3DScreenUpdateManager mgr = (D3DScreenUpdateManager)ScreenUpdateManager.getInstance();
            mgr.runUpdateNow();
        }
    }
}

