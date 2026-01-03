/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.Rectangle;
import java.awt.Shape;
import sun.java2d.SunGraphics2D;
import sun.java2d.pipe.CompositePipe;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.RegionIterator;
import sun.java2d.pipe.ShapeSpanIterator;

public class SpanClipRenderer
implements CompositePipe {
    CompositePipe outpipe;
    static Class<?> RegionClass = Region.class;
    static Class<?> RegionIteratorClass = RegionIterator.class;

    static native void initIDs(Class<?> var0, Class<?> var1);

    public SpanClipRenderer(CompositePipe pipe) {
        this.outpipe = pipe;
    }

    @Override
    public Object startSequence(SunGraphics2D sg, Shape s, Rectangle devR, int[] abox) {
        RegionIterator ri = sg.clipRegion.getIterator();
        return new SCRcontext(ri, this.outpipe.startSequence(sg, s, devR, abox));
    }

    @Override
    public boolean needTile(Object ctx, int x, int y, int w, int h) {
        SCRcontext context = (SCRcontext)ctx;
        return this.outpipe.needTile(context.outcontext, x, y, w, h);
    }

    public void renderPathTile(Object ctx, byte[] atile, int offset, int tsize, int x, int y, int w, int h, ShapeSpanIterator sr) {
        this.renderPathTile(ctx, atile, offset, tsize, x, y, w, h);
    }

    @Override
    public void renderPathTile(Object ctx, byte[] atile, int offset, int tsize, int x, int y, int w, int h) {
        SCRcontext context = (SCRcontext)ctx;
        RegionIterator ri = context.iterator.createCopy();
        int[] band = context.band;
        band[0] = x;
        band[1] = y;
        band[2] = x + w;
        band[3] = y + h;
        if (atile == null) {
            int size = w * h;
            atile = context.tile;
            if (atile != null && atile.length < size) {
                atile = null;
            }
            if (atile == null) {
                atile = new byte[size];
                context.tile = atile;
            }
            offset = 0;
            tsize = w;
            this.fillTile(ri, atile, offset, tsize, band);
        } else {
            this.eraseTile(ri, atile, offset, tsize, band);
        }
        if (band[2] > band[0] && band[3] > band[1]) {
            this.outpipe.renderPathTile(context.outcontext, atile, offset += (band[1] - y) * tsize + (band[0] - x), tsize, band[0], band[1], band[2] - band[0], band[3] - band[1]);
        }
    }

    public native void fillTile(RegionIterator var1, byte[] var2, int var3, int var4, int[] var5);

    public native void eraseTile(RegionIterator var1, byte[] var2, int var3, int var4, int[] var5);

    @Override
    public void skipTile(Object ctx, int x, int y) {
        SCRcontext context = (SCRcontext)ctx;
        this.outpipe.skipTile(context.outcontext, x, y);
    }

    @Override
    public void endSequence(Object ctx) {
        SCRcontext context = (SCRcontext)ctx;
        this.outpipe.endSequence(context.outcontext);
    }

    static {
        SpanClipRenderer.initIDs(RegionClass, RegionIteratorClass);
    }

    static class SCRcontext {
        RegionIterator iterator;
        Object outcontext;
        int[] band;
        byte[] tile;

        public SCRcontext(RegionIterator ri, Object outctx) {
            this.iterator = ri;
            this.outcontext = outctx;
            this.band = new int[4];
        }
    }
}

