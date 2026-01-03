/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.AlphaComposite;
import java.awt.Composite;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.MaskFill;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.RenderBuffer;
import sun.java2d.pipe.RenderQueue;

public abstract class BufferedMaskFill
extends MaskFill {
    protected final RenderQueue rq;

    protected BufferedMaskFill(RenderQueue rq, SurfaceType srcType, CompositeType compType, SurfaceType dstType) {
        super(srcType, compType, dstType);
        this.rq = rq;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void MaskFill(SunGraphics2D sg2d, SurfaceData sData, Composite comp, final int x, final int y, final int w, final int h, final byte[] mask, final int maskoff, final int maskscan) {
        AlphaComposite acomp = (AlphaComposite)comp;
        if (acomp.getRule() != 3) {
            comp = AlphaComposite.SrcOver;
        }
        this.rq.lock();
        try {
            this.validateContext(sg2d, comp, 2);
            int maskBytesRequired = mask != null ? mask.length + 3 & 0xFFFFFFFC : 0;
            int totalBytesRequired = 32 + maskBytesRequired;
            RenderBuffer buf = this.rq.getBuffer();
            if (totalBytesRequired <= buf.capacity()) {
                if (totalBytesRequired > buf.remaining()) {
                    this.rq.flushNow();
                }
                buf.putInt(32);
                buf.putInt(x).putInt(y).putInt(w).putInt(h);
                buf.putInt(maskoff);
                buf.putInt(maskscan);
                buf.putInt(maskBytesRequired);
                if (mask != null) {
                    int padding = maskBytesRequired - mask.length;
                    buf.put(mask);
                    if (padding != 0) {
                        buf.position(buf.position() + padding);
                    }
                }
            } else {
                this.rq.flushAndInvokeNow(new Runnable(){
                    final /* synthetic */ BufferedMaskFill this$0;
                    {
                        this.this$0 = this$0;
                    }

                    @Override
                    public void run() {
                        this.this$0.maskFill(x, y, w, h, maskoff, maskscan, mask.length, mask);
                    }
                });
            }
        }
        finally {
            this.rq.unlock();
        }
    }

    protected abstract void maskFill(int var1, int var2, int var3, int var4, int var5, int var6, int var7, byte[] var8);

    protected abstract void validateContext(SunGraphics2D var1, Composite var2, int var3);
}

