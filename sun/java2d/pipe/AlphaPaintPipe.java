/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.lang.ref.WeakReference;
import sun.awt.image.BufImgSurfaceData;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.MaskBlit;
import sun.java2d.pipe.CompositePipe;

public class AlphaPaintPipe
implements CompositePipe {
    static WeakReference<Raster> cachedLastRaster;
    static WeakReference<ColorModel> cachedLastColorModel;
    static WeakReference<SurfaceData> cachedLastData;
    private static final int TILE_SIZE = 32;

    @Override
    public Object startSequence(SunGraphics2D sg, Shape s, Rectangle devR, int[] abox) {
        PaintContext paintContext = sg.paint.createContext(sg.getDeviceColorModel(), devR, s.getBounds2D(), sg.cloneTransform(), sg.getRenderingHints());
        return new TileContext(sg, paintContext);
    }

    @Override
    public boolean needTile(Object context, int x, int y, int w, int h) {
        return true;
    }

    @Override
    public void renderPathTile(Object ctx, byte[] atile, int offset, int tilesize, int x, int y, int w, int h) {
        TileContext context = (TileContext)ctx;
        PaintContext paintCtxt = context.paintCtxt;
        SunGraphics2D sg = context.sunG2D;
        SurfaceData dstData = context.dstData;
        SurfaceData srcData = null;
        Raster lastRas = null;
        if (context.lastData != null && context.lastRaster != null) {
            srcData = (SurfaceData)context.lastData.get();
            lastRas = (Raster)context.lastRaster.get();
            if (srcData == null || lastRas == null) {
                srcData = null;
                lastRas = null;
            }
        }
        ColorModel paintModel = context.paintModel;
        for (int rely = 0; rely < h; rely += 32) {
            int ty = y + rely;
            int th = Math.min(h - rely, 32);
            for (int relx = 0; relx < w; relx += 32) {
                CompositeType comptype;
                int tx = x + relx;
                int tw = Math.min(w - relx, 32);
                Raster srcRaster = paintCtxt.getRaster(tx, ty, tw, th);
                if (srcRaster.getMinX() != 0 || srcRaster.getMinY() != 0) {
                    srcRaster = srcRaster.createTranslatedChild(0, 0);
                }
                if (lastRas != srcRaster) {
                    lastRas = srcRaster;
                    context.lastRaster = new WeakReference<Raster>(lastRas);
                    BufferedImage bImg = new BufferedImage(paintModel, (WritableRaster)srcRaster, paintModel.isAlphaPremultiplied(), null);
                    srcData = BufImgSurfaceData.createData(bImg);
                    context.lastData = new WeakReference<SurfaceData>(srcData);
                    context.lastMask = null;
                    context.lastBlit = null;
                }
                if (atile == null) {
                    if (context.lastBlit == null) {
                        comptype = sg.imageComp;
                        if (CompositeType.SrcOverNoEa.equals(comptype) && paintModel.getTransparency() == 1) {
                            comptype = CompositeType.SrcNoEa;
                        }
                        context.lastBlit = Blit.getFromCache(srcData.getSurfaceType(), comptype, dstData.getSurfaceType());
                    }
                    context.lastBlit.Blit(srcData, dstData, sg.composite, null, 0, 0, tx, ty, tw, th);
                    continue;
                }
                if (context.lastMask == null) {
                    comptype = sg.imageComp;
                    if (CompositeType.SrcOverNoEa.equals(comptype) && paintModel.getTransparency() == 1) {
                        comptype = CompositeType.SrcNoEa;
                    }
                    context.lastMask = MaskBlit.getFromCache(srcData.getSurfaceType(), comptype, dstData.getSurfaceType());
                }
                int toff = offset + rely * tilesize + relx;
                context.lastMask.MaskBlit(srcData, dstData, sg.composite, null, 0, 0, tx, ty, tw, th, atile, toff, tilesize);
            }
        }
    }

    @Override
    public void skipTile(Object context, int x, int y) {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void endSequence(Object ctx) {
        TileContext context = (TileContext)ctx;
        if (context.paintCtxt != null) {
            context.paintCtxt.dispose();
        }
        Class<AlphaPaintPipe> clazz = AlphaPaintPipe.class;
        synchronized (AlphaPaintPipe.class) {
            if (context.lastData != null) {
                cachedLastRaster = context.lastRaster;
                if (cachedLastColorModel == null || cachedLastColorModel.get() != context.paintModel) {
                    cachedLastColorModel = new WeakReference<ColorModel>(context.paintModel);
                }
                cachedLastData = context.lastData;
            }
            // ** MonitorExit[var3_3] (shouldn't be in output)
            return;
        }
    }

    static class TileContext {
        SunGraphics2D sunG2D;
        PaintContext paintCtxt;
        ColorModel paintModel;
        WeakReference<Raster> lastRaster;
        WeakReference<SurfaceData> lastData;
        MaskBlit lastMask;
        Blit lastBlit;
        SurfaceData dstData;

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public TileContext(SunGraphics2D sg, PaintContext pc) {
            this.sunG2D = sg;
            this.paintCtxt = pc;
            this.paintModel = pc.getColorModel();
            this.dstData = sg.getSurfaceData();
            Class<AlphaPaintPipe> clazz = AlphaPaintPipe.class;
            synchronized (AlphaPaintPipe.class) {
                if (cachedLastColorModel != null && cachedLastColorModel.get() == this.paintModel) {
                    this.lastRaster = cachedLastRaster;
                    this.lastData = cachedLastData;
                }
                // ** MonitorExit[var3_3] (shouldn't be in output)
                return;
            }
        }
    }
}

