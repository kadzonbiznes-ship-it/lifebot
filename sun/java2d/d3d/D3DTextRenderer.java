/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import java.awt.Composite;
import sun.font.GlyphList;
import sun.java2d.SunGraphics2D;
import sun.java2d.d3d.D3DContext;
import sun.java2d.d3d.D3DSurfaceData;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.pipe.BufferedTextPipe;
import sun.java2d.pipe.RenderQueue;

class D3DTextRenderer
extends BufferedTextPipe {
    D3DTextRenderer(RenderQueue rq) {
        super(rq);
    }

    @Override
    protected native void drawGlyphList(int var1, boolean var2, boolean var3, boolean var4, int var5, float var6, float var7, long[] var8, float[] var9);

    @Override
    protected void validateContext(SunGraphics2D sg2d, Composite comp) {
        D3DSurfaceData d3dDst = (D3DSurfaceData)sg2d.surfaceData;
        D3DContext.validateContext(d3dDst, d3dDst, sg2d.getCompClip(), comp, null, sg2d.paint, sg2d, 0);
    }

    D3DTextRenderer traceWrap() {
        return new Tracer(this);
    }

    private static class Tracer
    extends D3DTextRenderer {
        Tracer(D3DTextRenderer d3dtr) {
            super(d3dtr.rq);
        }

        @Override
        protected void drawGlyphList(SunGraphics2D sg2d, GlyphList gl) {
            GraphicsPrimitive.tracePrimitive("D3DDrawGlyphs");
            super.drawGlyphList(sg2d, gl);
        }
    }
}

