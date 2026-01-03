/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import java.awt.geom.Path2D;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.d3d.D3DContext;
import sun.java2d.d3d.D3DSurfaceData;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.pipe.BufferedRenderPipe;
import sun.java2d.pipe.ParallelogramPipe;
import sun.java2d.pipe.RenderQueue;
import sun.java2d.pipe.SpanIterator;

class D3DRenderer
extends BufferedRenderPipe {
    D3DRenderer(RenderQueue rq) {
        super(rq);
    }

    @Override
    protected void validateContext(SunGraphics2D sg2d) {
        int ctxflags = sg2d.paint.getTransparency() == 1 ? 1 : 0;
        D3DSurfaceData dstData = SurfaceData.convertTo(D3DSurfaceData.class, sg2d.surfaceData);
        D3DContext.validateContext(dstData, dstData, sg2d.getCompClip(), sg2d.composite, null, sg2d.paint, sg2d, ctxflags);
    }

    @Override
    protected void validateContextAA(SunGraphics2D sg2d) {
        int ctxflags = 0;
        D3DSurfaceData dstData = SurfaceData.convertTo(D3DSurfaceData.class, sg2d.surfaceData);
        D3DContext.validateContext(dstData, dstData, sg2d.getCompClip(), sg2d.composite, null, sg2d.paint, sg2d, ctxflags);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void copyArea(SunGraphics2D sg2d, int x, int y, int w, int h, int dx, int dy) {
        this.rq.lock();
        try {
            int ctxflags = sg2d.surfaceData.getTransparency() == 1 ? 1 : 0;
            D3DSurfaceData dstData = SurfaceData.convertTo(D3DSurfaceData.class, sg2d.surfaceData);
            D3DContext.validateContext(dstData, dstData, sg2d.getCompClip(), sg2d.composite, null, null, null, ctxflags);
            this.rq.ensureCapacity(28);
            this.buf.putInt(30);
            this.buf.putInt(x).putInt(y).putInt(w).putInt(h);
            this.buf.putInt(dx).putInt(dy);
        }
        finally {
            this.rq.unlock();
        }
    }

    @Override
    protected native void drawPoly(int[] var1, int[] var2, int var3, boolean var4, int var5, int var6);

    D3DRenderer traceWrap() {
        return new Tracer(this);
    }

    private static class Tracer
    extends D3DRenderer {
        private D3DRenderer d3dr;

        Tracer(D3DRenderer d3dr) {
            super(d3dr.rq);
            this.d3dr = d3dr;
        }

        @Override
        public ParallelogramPipe getAAParallelogramPipe() {
            final ParallelogramPipe realpipe = this.d3dr.getAAParallelogramPipe();
            return new ParallelogramPipe(){

                @Override
                public void fillParallelogram(SunGraphics2D sg2d, double ux1, double uy1, double ux2, double uy2, double x, double y, double dx1, double dy1, double dx2, double dy2) {
                    GraphicsPrimitive.tracePrimitive("D3DFillAAParallelogram");
                    realpipe.fillParallelogram(sg2d, ux1, uy1, ux2, uy2, x, y, dx1, dy1, dx2, dy2);
                }

                @Override
                public void drawParallelogram(SunGraphics2D sg2d, double ux1, double uy1, double ux2, double uy2, double x, double y, double dx1, double dy1, double dx2, double dy2, double lw1, double lw2) {
                    GraphicsPrimitive.tracePrimitive("D3DDrawAAParallelogram");
                    realpipe.drawParallelogram(sg2d, ux1, uy1, ux2, uy2, x, y, dx1, dy1, dx2, dy2, lw1, lw2);
                }
            };
        }

        @Override
        protected void validateContext(SunGraphics2D sg2d) {
            this.d3dr.validateContext(sg2d);
        }

        @Override
        public void drawLine(SunGraphics2D sg2d, int x1, int y1, int x2, int y2) {
            GraphicsPrimitive.tracePrimitive("D3DDrawLine");
            this.d3dr.drawLine(sg2d, x1, y1, x2, y2);
        }

        @Override
        public void drawRect(SunGraphics2D sg2d, int x, int y, int w, int h) {
            GraphicsPrimitive.tracePrimitive("D3DDrawRect");
            this.d3dr.drawRect(sg2d, x, y, w, h);
        }

        @Override
        protected void drawPoly(SunGraphics2D sg2d, int[] xPoints, int[] yPoints, int nPoints, boolean isClosed) {
            GraphicsPrimitive.tracePrimitive("D3DDrawPoly");
            this.d3dr.drawPoly(sg2d, xPoints, yPoints, nPoints, isClosed);
        }

        @Override
        public void fillRect(SunGraphics2D sg2d, int x, int y, int w, int h) {
            GraphicsPrimitive.tracePrimitive("D3DFillRect");
            this.d3dr.fillRect(sg2d, x, y, w, h);
        }

        @Override
        protected void drawPath(SunGraphics2D sg2d, Path2D.Float p2df, int transx, int transy) {
            GraphicsPrimitive.tracePrimitive("D3DDrawPath");
            this.d3dr.drawPath(sg2d, p2df, transx, transy);
        }

        @Override
        protected void fillPath(SunGraphics2D sg2d, Path2D.Float p2df, int transx, int transy) {
            GraphicsPrimitive.tracePrimitive("D3DFillPath");
            this.d3dr.fillPath(sg2d, p2df, transx, transy);
        }

        @Override
        protected void fillSpans(SunGraphics2D sg2d, SpanIterator si, int transx, int transy) {
            GraphicsPrimitive.tracePrimitive("D3DFillSpans");
            this.d3dr.fillSpans(sg2d, si, transx, transy);
        }

        @Override
        public void fillParallelogram(SunGraphics2D sg2d, double ux1, double uy1, double ux2, double uy2, double x, double y, double dx1, double dy1, double dx2, double dy2) {
            GraphicsPrimitive.tracePrimitive("D3DFillParallelogram");
            this.d3dr.fillParallelogram(sg2d, ux1, uy1, ux2, uy2, x, y, dx1, dy1, dx2, dy2);
        }

        @Override
        public void drawParallelogram(SunGraphics2D sg2d, double ux1, double uy1, double ux2, double uy2, double x, double y, double dx1, double dy1, double dx2, double dy2, double lw1, double lw2) {
            GraphicsPrimitive.tracePrimitive("D3DDrawParallelogram");
            this.d3dr.drawParallelogram(sg2d, ux1, uy1, ux2, uy2, x, y, dx1, dy1, dx2, dy2, lw1, lw2);
        }

        @Override
        public void copyArea(SunGraphics2D sg2d, int x, int y, int w, int h, int dx, int dy) {
            GraphicsPrimitive.tracePrimitive("D3DCopyArea");
            this.d3dr.copyArea(sg2d, x, y, w, h, dx, dy);
        }
    }
}

