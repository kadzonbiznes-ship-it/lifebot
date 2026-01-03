/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import sun.java2d.SunGraphics2D;
import sun.java2d.loops.ProcessPath;
import sun.java2d.pipe.LoopPipe;
import sun.java2d.pipe.ParallelogramPipe;
import sun.java2d.pipe.PixelDrawPipe;
import sun.java2d.pipe.PixelFillPipe;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.RenderBuffer;
import sun.java2d.pipe.RenderQueue;
import sun.java2d.pipe.ShapeDrawPipe;
import sun.java2d.pipe.ShapeSpanIterator;
import sun.java2d.pipe.SpanIterator;

public abstract class BufferedRenderPipe
implements PixelDrawPipe,
PixelFillPipe,
ShapeDrawPipe,
ParallelogramPipe {
    ParallelogramPipe aapgrampipe = new AAParallelogramPipe();
    static final int BYTES_PER_POLY_POINT = 8;
    static final int BYTES_PER_SCANLINE = 12;
    static final int BYTES_PER_SPAN = 16;
    protected RenderQueue rq;
    protected RenderBuffer buf;
    private BufferedDrawHandler drawHandler;

    public BufferedRenderPipe(RenderQueue rq) {
        this.rq = rq;
        this.buf = rq.getBuffer();
        this.drawHandler = new BufferedDrawHandler();
    }

    public ParallelogramPipe getAAParallelogramPipe() {
        return this.aapgrampipe;
    }

    protected abstract void validateContext(SunGraphics2D var1);

    protected abstract void validateContextAA(SunGraphics2D var1);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawLine(SunGraphics2D sg2d, int x1, int y1, int x2, int y2) {
        int transx = sg2d.transX;
        int transy = sg2d.transY;
        this.rq.lock();
        try {
            this.validateContext(sg2d);
            this.rq.ensureCapacity(20);
            this.buf.putInt(10);
            this.buf.putInt(x1 + transx);
            this.buf.putInt(y1 + transy);
            this.buf.putInt(x2 + transx);
            this.buf.putInt(y2 + transy);
        }
        finally {
            this.rq.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawRect(SunGraphics2D sg2d, int x, int y, int width, int height) {
        this.rq.lock();
        try {
            this.validateContext(sg2d);
            this.rq.ensureCapacity(20);
            this.buf.putInt(11);
            this.buf.putInt(x + sg2d.transX);
            this.buf.putInt(y + sg2d.transY);
            this.buf.putInt(width);
            this.buf.putInt(height);
        }
        finally {
            this.rq.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void fillRect(SunGraphics2D sg2d, int x, int y, int width, int height) {
        this.rq.lock();
        try {
            this.validateContext(sg2d);
            this.rq.ensureCapacity(20);
            this.buf.putInt(20);
            this.buf.putInt(x + sg2d.transX);
            this.buf.putInt(y + sg2d.transY);
            this.buf.putInt(width);
            this.buf.putInt(height);
        }
        finally {
            this.rq.unlock();
        }
    }

    @Override
    public void drawRoundRect(SunGraphics2D sg2d, int x, int y, int width, int height, int arcWidth, int arcHeight) {
        this.draw(sg2d, new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight));
    }

    @Override
    public void fillRoundRect(SunGraphics2D sg2d, int x, int y, int width, int height, int arcWidth, int arcHeight) {
        this.fill(sg2d, new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight));
    }

    @Override
    public void drawOval(SunGraphics2D sg2d, int x, int y, int width, int height) {
        this.draw(sg2d, new Ellipse2D.Float(x, y, width, height));
    }

    @Override
    public void fillOval(SunGraphics2D sg2d, int x, int y, int width, int height) {
        this.fill(sg2d, new Ellipse2D.Float(x, y, width, height));
    }

    @Override
    public void drawArc(SunGraphics2D sg2d, int x, int y, int width, int height, int startAngle, int arcAngle) {
        this.draw(sg2d, new Arc2D.Float(x, y, width, height, startAngle, arcAngle, 0));
    }

    @Override
    public void fillArc(SunGraphics2D sg2d, int x, int y, int width, int height, int startAngle, int arcAngle) {
        this.fill(sg2d, new Arc2D.Float(x, y, width, height, startAngle, arcAngle, 2));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void drawPoly(final SunGraphics2D sg2d, final int[] xPoints, final int[] yPoints, final int nPoints, final boolean isClosed) {
        if (xPoints == null || yPoints == null) {
            throw new NullPointerException("coordinate array");
        }
        if (xPoints.length < nPoints || yPoints.length < nPoints) {
            throw new ArrayIndexOutOfBoundsException("coordinate array");
        }
        if (nPoints < 2) {
            return;
        }
        if (nPoints == 2 && !isClosed) {
            this.drawLine(sg2d, xPoints[0], yPoints[0], xPoints[1], yPoints[1]);
            return;
        }
        this.rq.lock();
        try {
            this.validateContext(sg2d);
            int pointBytesRequired = nPoints * 8;
            int totalBytesRequired = 20 + pointBytesRequired;
            if (totalBytesRequired <= this.buf.capacity()) {
                if (totalBytesRequired > this.buf.remaining()) {
                    this.rq.flushNow();
                }
                this.buf.putInt(12);
                this.buf.putInt(nPoints);
                this.buf.putInt(isClosed ? 1 : 0);
                this.buf.putInt(sg2d.transX);
                this.buf.putInt(sg2d.transY);
                this.buf.put(xPoints, 0, nPoints);
                this.buf.put(yPoints, 0, nPoints);
            } else {
                this.rq.flushAndInvokeNow(new Runnable(){
                    final /* synthetic */ BufferedRenderPipe this$0;
                    {
                        this.this$0 = this$0;
                    }

                    @Override
                    public void run() {
                        this.this$0.drawPoly(xPoints, yPoints, nPoints, isClosed, sg2d.transX, sg2d.transY);
                    }
                });
            }
        }
        finally {
            this.rq.unlock();
        }
    }

    protected abstract void drawPoly(int[] var1, int[] var2, int var3, boolean var4, int var5, int var6);

    @Override
    public void drawPolyline(SunGraphics2D sg2d, int[] xPoints, int[] yPoints, int nPoints) {
        this.drawPoly(sg2d, xPoints, yPoints, nPoints, false);
    }

    @Override
    public void drawPolygon(SunGraphics2D sg2d, int[] xPoints, int[] yPoints, int nPoints) {
        this.drawPoly(sg2d, xPoints, yPoints, nPoints, true);
    }

    @Override
    public void fillPolygon(SunGraphics2D sg2d, int[] xPoints, int[] yPoints, int nPoints) {
        this.fill(sg2d, new Polygon(xPoints, yPoints, nPoints));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void drawPath(SunGraphics2D sg2d, Path2D.Float p2df, int transx, int transy) {
        this.rq.lock();
        try {
            this.validateContext(sg2d);
            this.drawHandler.validate(sg2d);
            ProcessPath.drawPath(this.drawHandler, p2df, transx, transy);
        }
        finally {
            this.rq.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void fillPath(SunGraphics2D sg2d, Path2D.Float p2df, int transx, int transy) {
        this.rq.lock();
        try {
            this.validateContext(sg2d);
            this.drawHandler.validate(sg2d);
            this.drawHandler.startFillPath();
            ProcessPath.fillPath(this.drawHandler, p2df, transx, transy);
            this.drawHandler.endFillPath();
        }
        finally {
            this.rq.unlock();
        }
    }

    private native int fillSpans(RenderQueue var1, long var2, int var4, int var5, SpanIterator var6, long var7, int var9, int var10);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void fillSpans(SunGraphics2D sg2d, SpanIterator si, int transx, int transy) {
        this.rq.lock();
        try {
            this.validateContext(sg2d);
            this.rq.ensureCapacity(24);
            int newpos = this.fillSpans(this.rq, this.buf.getAddress(), this.buf.position(), this.buf.capacity(), si, si.getNativeIterator(), transx, transy);
            this.buf.position(newpos);
        }
        finally {
            this.rq.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void fillParallelogram(SunGraphics2D sg2d, double ux1, double uy1, double ux2, double uy2, double x, double y, double dx1, double dy1, double dx2, double dy2) {
        this.rq.lock();
        try {
            this.validateContext(sg2d);
            this.rq.ensureCapacity(28);
            this.buf.putInt(22);
            this.buf.putFloat((float)x);
            this.buf.putFloat((float)y);
            this.buf.putFloat((float)dx1);
            this.buf.putFloat((float)dy1);
            this.buf.putFloat((float)dx2);
            this.buf.putFloat((float)dy2);
        }
        finally {
            this.rq.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawParallelogram(SunGraphics2D sg2d, double ux1, double uy1, double ux2, double uy2, double x, double y, double dx1, double dy1, double dx2, double dy2, double lw1, double lw2) {
        this.rq.lock();
        try {
            this.validateContext(sg2d);
            this.rq.ensureCapacity(36);
            this.buf.putInt(15);
            this.buf.putFloat((float)x);
            this.buf.putFloat((float)y);
            this.buf.putFloat((float)dx1);
            this.buf.putFloat((float)dy1);
            this.buf.putFloat((float)dx2);
            this.buf.putFloat((float)dy2);
            this.buf.putFloat((float)lw1);
            this.buf.putFloat((float)lw2);
        }
        finally {
            this.rq.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void draw(SunGraphics2D sg2d, Shape s) {
        if (sg2d.strokeState == 0) {
            int transy;
            int transx;
            Path2D.Float p2df;
            if (s instanceof Polygon && sg2d.transformState < 3) {
                Polygon p = (Polygon)s;
                this.drawPolygon(sg2d, p.xpoints, p.ypoints, p.npoints);
                return;
            }
            if (sg2d.transformState <= 1) {
                p2df = s instanceof Path2D.Float ? (Path2D.Float)s : new Path2D.Float(s);
                transx = sg2d.transX;
                transy = sg2d.transY;
            } else {
                p2df = new Path2D.Float(s, sg2d.transform);
                transx = 0;
                transy = 0;
            }
            this.drawPath(sg2d, p2df, transx, transy);
        } else if (sg2d.strokeState < 3) {
            ShapeSpanIterator si = LoopPipe.getStrokeSpans(sg2d, s);
            try {
                this.fillSpans(sg2d, si, 0, 0);
            }
            finally {
                si.dispose();
            }
        } else {
            this.fill(sg2d, sg2d.stroke.createStrokedShape(s));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void fill(SunGraphics2D sg2d, Shape s) {
        int transy;
        int transx;
        AffineTransform at;
        if (sg2d.strokeState == 0) {
            int transy2;
            int transx2;
            Path2D.Float p2df;
            if (sg2d.transformState <= 1) {
                p2df = s instanceof Path2D.Float ? (Path2D.Float)s : new Path2D.Float(s);
                transx2 = sg2d.transX;
                transy2 = sg2d.transY;
            } else {
                p2df = new Path2D.Float(s, sg2d.transform);
                transx2 = 0;
                transy2 = 0;
            }
            this.fillPath(sg2d, p2df, transx2, transy2);
            return;
        }
        if (sg2d.transformState <= 1) {
            at = null;
            transx = sg2d.transX;
            transy = sg2d.transY;
        } else {
            at = sg2d.transform;
            transy = 0;
            transx = 0;
        }
        ShapeSpanIterator ssi = LoopPipe.getFillSSI(sg2d);
        try {
            Region clip = sg2d.getCompClip();
            ssi.setOutputAreaXYXY(clip.getLoX() - transx, clip.getLoY() - transy, clip.getHiX() - transx, clip.getHiY() - transy);
            ssi.appendPath(s.getPathIterator(at));
            this.fillSpans(sg2d, ssi, transx, transy);
        }
        finally {
            ssi.dispose();
        }
    }

    private class AAParallelogramPipe
    implements ParallelogramPipe {
        private AAParallelogramPipe() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void fillParallelogram(SunGraphics2D sg2d, double ux1, double uy1, double ux2, double uy2, double x, double y, double dx1, double dy1, double dx2, double dy2) {
            BufferedRenderPipe.this.rq.lock();
            try {
                BufferedRenderPipe.this.validateContextAA(sg2d);
                BufferedRenderPipe.this.rq.ensureCapacity(28);
                BufferedRenderPipe.this.buf.putInt(23);
                BufferedRenderPipe.this.buf.putFloat((float)x);
                BufferedRenderPipe.this.buf.putFloat((float)y);
                BufferedRenderPipe.this.buf.putFloat((float)dx1);
                BufferedRenderPipe.this.buf.putFloat((float)dy1);
                BufferedRenderPipe.this.buf.putFloat((float)dx2);
                BufferedRenderPipe.this.buf.putFloat((float)dy2);
            }
            finally {
                BufferedRenderPipe.this.rq.unlock();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void drawParallelogram(SunGraphics2D sg2d, double ux1, double uy1, double ux2, double uy2, double x, double y, double dx1, double dy1, double dx2, double dy2, double lw1, double lw2) {
            BufferedRenderPipe.this.rq.lock();
            try {
                BufferedRenderPipe.this.validateContextAA(sg2d);
                BufferedRenderPipe.this.rq.ensureCapacity(36);
                BufferedRenderPipe.this.buf.putInt(16);
                BufferedRenderPipe.this.buf.putFloat((float)x);
                BufferedRenderPipe.this.buf.putFloat((float)y);
                BufferedRenderPipe.this.buf.putFloat((float)dx1);
                BufferedRenderPipe.this.buf.putFloat((float)dy1);
                BufferedRenderPipe.this.buf.putFloat((float)dx2);
                BufferedRenderPipe.this.buf.putFloat((float)dy2);
                BufferedRenderPipe.this.buf.putFloat((float)lw1);
                BufferedRenderPipe.this.buf.putFloat((float)lw2);
            }
            finally {
                BufferedRenderPipe.this.rq.unlock();
            }
        }
    }

    private class BufferedDrawHandler
    extends ProcessPath.DrawHandler {
        private int scanlineCount;
        private int scanlineCountIndex;
        private int remainingScanlines;

        BufferedDrawHandler() {
            super(0, 0, 0, 0);
        }

        void validate(SunGraphics2D sg2d) {
            Region clip = sg2d.getCompClip();
            this.setBounds(clip.getLoX(), clip.getLoY(), clip.getHiX(), clip.getHiY(), sg2d.strokeHint);
        }

        @Override
        public void drawLine(int x1, int y1, int x2, int y2) {
            BufferedRenderPipe.this.rq.ensureCapacity(20);
            BufferedRenderPipe.this.buf.putInt(10);
            BufferedRenderPipe.this.buf.putInt(x1);
            BufferedRenderPipe.this.buf.putInt(y1);
            BufferedRenderPipe.this.buf.putInt(x2);
            BufferedRenderPipe.this.buf.putInt(y2);
        }

        @Override
        public void drawPixel(int x, int y) {
            BufferedRenderPipe.this.rq.ensureCapacity(12);
            BufferedRenderPipe.this.buf.putInt(13);
            BufferedRenderPipe.this.buf.putInt(x);
            BufferedRenderPipe.this.buf.putInt(y);
        }

        private void resetFillPath() {
            BufferedRenderPipe.this.buf.putInt(14);
            this.scanlineCountIndex = BufferedRenderPipe.this.buf.position();
            BufferedRenderPipe.this.buf.putInt(0);
            this.scanlineCount = 0;
            this.remainingScanlines = BufferedRenderPipe.this.buf.remaining() / 12;
        }

        private void updateScanlineCount() {
            BufferedRenderPipe.this.buf.putInt(this.scanlineCountIndex, this.scanlineCount);
        }

        public void startFillPath() {
            BufferedRenderPipe.this.rq.ensureCapacity(20);
            this.resetFillPath();
        }

        @Override
        public void drawScanline(int x1, int x2, int y) {
            if (this.remainingScanlines == 0) {
                this.updateScanlineCount();
                BufferedRenderPipe.this.rq.flushNow();
                this.resetFillPath();
            }
            BufferedRenderPipe.this.buf.putInt(x1);
            BufferedRenderPipe.this.buf.putInt(x2);
            BufferedRenderPipe.this.buf.putInt(y);
            ++this.scanlineCount;
            --this.remainingScanlines;
        }

        public void endFillPath() {
            this.updateScanlineCount();
        }
    }
}

