/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.Rectangle;
import java.awt.Shape;
import sun.font.GlyphList;
import sun.java2d.SunGraphics2D;
import sun.java2d.pipe.CompositePipe;
import sun.java2d.pipe.GlyphListPipe;
import sun.java2d.pipe.Region;

public class TextRenderer
extends GlyphListPipe {
    CompositePipe outpipe;

    public TextRenderer(CompositePipe pipe) {
        this.outpipe = pipe;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void drawGlyphList(SunGraphics2D sg2d, GlyphList gl) {
        int num = gl.getNumGlyphs();
        Region clipRegion = sg2d.getCompClip();
        int cx1 = clipRegion.getLoX();
        int cy1 = clipRegion.getLoY();
        int cx2 = clipRegion.getHiX();
        int cy2 = clipRegion.getHiY();
        Object ctx = null;
        try {
            gl.startGlyphIteration();
            int[] bounds = gl.getBounds(num);
            Rectangle r = new Rectangle(bounds[0], bounds[1], bounds[2] - bounds[0], bounds[3] - bounds[1]);
            Shape s = sg2d.untransformShape(r);
            ctx = this.outpipe.startSequence(sg2d, s, r, bounds);
            for (int i = 0; i < num; ++i) {
                gl.setGlyphIndex(i);
                int[] metrics = gl.getMetrics();
                int gx1 = metrics[0];
                int gy1 = metrics[1];
                int w = metrics[2];
                int gx2 = gx1 + w;
                int gy2 = gy1 + metrics[3];
                int off = 0;
                if (gx1 < cx1) {
                    off = cx1 - gx1;
                    gx1 = cx1;
                }
                if (gy1 < cy1) {
                    off += (cy1 - gy1) * w;
                    gy1 = cy1;
                }
                if (gx2 > cx2) {
                    gx2 = cx2;
                }
                if (gy2 > cy2) {
                    gy2 = cy2;
                }
                if (gx2 > gx1 && gy2 > gy1 && !gl.isColorGlyph(i) && this.outpipe.needTile(ctx, gx1, gy1, gx2 - gx1, gy2 - gy1)) {
                    byte[] alpha = gl.getGrayBits();
                    this.outpipe.renderPathTile(ctx, alpha, off, w, gx1, gy1, gx2 - gx1, gy2 - gy1);
                    continue;
                }
                this.outpipe.skipTile(ctx, gx1, gy1);
            }
            if (ctx != null) {
                this.outpipe.endSequence(ctx);
            }
        }
        catch (Throwable throwable) {
            if (ctx != null) {
                this.outpipe.endSequence(ctx);
            }
            throw throwable;
        }
    }
}

