/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.AlphaComposite;
import java.awt.Composite;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.MaskBlit;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.RenderBuffer;
import sun.java2d.pipe.RenderQueue;

public abstract class BufferedMaskBlit
extends MaskBlit {
    private static final int ST_INT_ARGB = 0;
    private static final int ST_INT_ARGB_PRE = 1;
    private static final int ST_INT_RGB = 2;
    private static final int ST_INT_BGR = 3;
    private final RenderQueue rq;
    private final int srcTypeVal;
    private Blit blitop;

    protected BufferedMaskBlit(RenderQueue rq, SurfaceType srcType, CompositeType compType, SurfaceType dstType) {
        super(srcType, compType, dstType);
        this.rq = rq;
        if (srcType == SurfaceType.IntArgb) {
            this.srcTypeVal = 0;
        } else if (srcType == SurfaceType.IntArgbPre) {
            this.srcTypeVal = 1;
        } else if (srcType == SurfaceType.IntRgb) {
            this.srcTypeVal = 2;
        } else if (srcType == SurfaceType.IntBgr) {
            this.srcTypeVal = 3;
        } else {
            throw new InternalError("unrecognized source surface type");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void MaskBlit(SurfaceData src, SurfaceData dst, Composite comp, Region clip, int srcx, int srcy, int dstx, int dsty, int width, int height, byte[] mask, int maskoff, int maskscan) {
        if (width <= 0 || height <= 0) {
            return;
        }
        if (mask == null) {
            if (this.blitop == null) {
                this.blitop = Blit.getFromCache(src.getSurfaceType(), CompositeType.AnyAlpha, this.getDestType());
            }
            this.blitop.Blit(src, dst, comp, clip, srcx, srcy, dstx, dsty, width, height);
            return;
        }
        AlphaComposite acomp = (AlphaComposite)comp;
        if (acomp.getRule() != 3) {
            comp = AlphaComposite.SrcOver;
        }
        this.rq.lock();
        try {
            this.validateContext(dst, comp, clip);
            RenderBuffer buf = this.rq.getBuffer();
            int totalBytesRequired = 20 + width * height * 4;
            this.rq.ensureCapacity(totalBytesRequired);
            int newpos = this.enqueueTile(buf.getAddress(), buf.position(), src, src.getNativeOps(), this.srcTypeVal, mask, mask.length, maskoff, maskscan, srcx, srcy, dstx, dsty, width, height);
            buf.position(newpos);
        }
        finally {
            this.rq.unlock();
        }
    }

    private native int enqueueTile(long var1, int var3, SurfaceData var4, long var5, int var7, byte[] var8, int var9, int var10, int var11, int var12, int var13, int var14, int var15, int var16, int var17);

    protected abstract void validateContext(SurfaceData var1, Composite var2, Region var3);
}

