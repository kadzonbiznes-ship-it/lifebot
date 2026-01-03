/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.AlphaComposite;
import java.awt.Composite;
import sun.font.GlyphList;
import sun.java2d.SunGraphics2D;
import sun.java2d.pipe.GlyphListPipe;
import sun.java2d.pipe.RenderBuffer;
import sun.java2d.pipe.RenderQueue;

public abstract class BufferedTextPipe
extends GlyphListPipe {
    private static final int BYTES_PER_GLYPH_IMAGE = 8;
    private static final int BYTES_PER_GLYPH_POSITION = 8;
    private static final int OFFSET_CONTRAST = 8;
    private static final int OFFSET_RGBORDER = 2;
    private static final int OFFSET_SUBPIXPOS = 1;
    private static final int OFFSET_POSITIONS = 0;
    protected final RenderQueue rq;

    private static int createPackedParams(SunGraphics2D sg2d, GlyphList gl) {
        return (gl.usePositions() ? 1 : 0) << 0 | (gl.isSubPixPos() ? 1 : 0) << 1 | (gl.isRGBOrder() ? 1 : 0) << 2 | (sg2d.lcdTextContrast & 0xFF) << 8;
    }

    protected BufferedTextPipe(RenderQueue rq) {
        this.rq = rq;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void drawGlyphList(SunGraphics2D sg2d, GlyphList gl) {
        Composite comp = sg2d.composite;
        if (comp == AlphaComposite.Src) {
            comp = AlphaComposite.SrcOver;
        }
        this.rq.lock();
        try {
            this.validateContext(sg2d, comp);
            this.enqueueGlyphList(sg2d, gl);
        }
        finally {
            this.rq.unlock();
        }
    }

    private void enqueueGlyphList(final SunGraphics2D sg2d, final GlyphList gl) {
        RenderBuffer buf = this.rq.getBuffer();
        final int totalGlyphs = gl.getNumGlyphs();
        int glyphBytesRequired = totalGlyphs * 8;
        int posBytesRequired = gl.usePositions() ? totalGlyphs * 8 : 0;
        int totalBytesRequired = 24 + glyphBytesRequired + posBytesRequired;
        final long[] images = gl.getImages();
        final float glyphListOrigX = gl.getX() + 0.5f;
        final float glyphListOrigY = gl.getY() + 0.5f;
        this.rq.addReference(gl.getStrike());
        if (totalBytesRequired <= buf.capacity()) {
            if (totalBytesRequired > buf.remaining()) {
                this.rq.flushNow();
            }
            this.rq.ensureAlignment(20);
            buf.putInt(40);
            buf.putInt(totalGlyphs);
            buf.putInt(BufferedTextPipe.createPackedParams(sg2d, gl));
            buf.putFloat(glyphListOrigX);
            buf.putFloat(glyphListOrigY);
            buf.put(images, 0, totalGlyphs);
            if (gl.usePositions()) {
                float[] positions = gl.getPositions();
                buf.put(positions, 0, 2 * totalGlyphs);
            }
        } else {
            this.rq.flushAndInvokeNow(new Runnable(){
                final /* synthetic */ BufferedTextPipe this$0;
                {
                    this.this$0 = this$0;
                }

                @Override
                public void run() {
                    this.this$0.drawGlyphList(totalGlyphs, gl.usePositions(), gl.isSubPixPos(), gl.isRGBOrder(), sg2d.lcdTextContrast, glyphListOrigX, glyphListOrigY, images, gl.getPositions());
                }
            });
        }
    }

    protected abstract void drawGlyphList(int var1, boolean var2, boolean var3, boolean var4, int var5, float var6, float var7, long[] var8, float[] var9);

    protected abstract void validateContext(SunGraphics2D var1, Composite var2);
}

