/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.CompositePipe;
import sun.java2d.pipe.LoopBasedPipe;
import sun.java2d.pipe.LoopPipe;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.ShapeDrawPipe;
import sun.java2d.pipe.ShapeSpanIterator;
import sun.java2d.pipe.SpanIterator;

public abstract class SpanShapeRenderer
implements ShapeDrawPipe {
    public static final int NON_RECTILINEAR_TRANSFORM_MASK = 48;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void draw(SunGraphics2D sg, Shape s) {
        if (sg.stroke instanceof BasicStroke) {
            ShapeSpanIterator sr = LoopPipe.getStrokeSpans(sg, s);
            try {
                this.renderSpans(sg, sg.getCompClip(), s, sr);
            }
            finally {
                sr.dispose();
            }
        } else {
            this.fill(sg, sg.stroke.createStrokedShape(s));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void fill(SunGraphics2D sg, Shape s) {
        if (s instanceof Rectangle2D && (sg.transform.getType() & 0x30) == 0) {
            this.renderRect(sg, (Rectangle2D)s);
            return;
        }
        Region clipRegion = sg.getCompClip();
        ShapeSpanIterator sr = LoopPipe.getFillSSI(sg);
        try {
            sr.setOutputArea(clipRegion);
            sr.appendPath(s.getPathIterator(sg.transform));
            this.renderSpans(sg, clipRegion, s, sr);
        }
        finally {
            sr.dispose();
        }
    }

    public abstract Object startSequence(SunGraphics2D var1, Shape var2, Rectangle var3, int[] var4);

    public abstract void renderBox(Object var1, int var2, int var3, int var4, int var5);

    public abstract void endSequence(Object var1);

    public void renderRect(SunGraphics2D sg, Rectangle2D r) {
        double t;
        double[] corners = new double[]{r.getX(), r.getY(), r.getWidth(), r.getHeight()};
        corners[2] = corners[2] + corners[0];
        corners[3] = corners[3] + corners[1];
        if (corners[2] <= corners[0] || corners[3] <= corners[1]) {
            return;
        }
        sg.transform.transform(corners, 0, corners, 0, 2);
        if (corners[2] < corners[0]) {
            t = corners[2];
            corners[2] = corners[0];
            corners[0] = t;
        }
        if (corners[3] < corners[1]) {
            t = corners[3];
            corners[3] = corners[1];
            corners[1] = t;
        }
        int[] abox = new int[]{(int)corners[0], (int)corners[1], (int)corners[2], (int)corners[3]};
        Rectangle devR = new Rectangle(abox[0], abox[1], abox[2] - abox[0], abox[3] - abox[1]);
        Region clipRegion = sg.getCompClip();
        clipRegion.clipBoxToBounds(abox);
        if (abox[0] >= abox[2] || abox[1] >= abox[3]) {
            return;
        }
        Object context = this.startSequence(sg, r, devR, abox);
        if (clipRegion.isRectangular()) {
            this.renderBox(context, abox[0], abox[1], abox[2] - abox[0], abox[3] - abox[1]);
        } else {
            SpanIterator sr = clipRegion.getSpanIterator(abox);
            while (sr.nextSpan(abox)) {
                this.renderBox(context, abox[0], abox[1], abox[2] - abox[0], abox[3] - abox[1]);
            }
        }
        this.endSequence(context);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderSpans(SunGraphics2D sg, Region clipRegion, Shape s, ShapeSpanIterator sr) {
        Rectangle devR;
        int[] abox;
        Object context;
        block6: {
            context = null;
            abox = new int[4];
            try {
                sr.getPathBox(abox);
                devR = new Rectangle(abox[0], abox[1], abox[2] - abox[0], abox[3] - abox[1]);
                clipRegion.clipBoxToBounds(abox);
                if (abox[0] < abox[2] && abox[1] < abox[3]) break block6;
                if (context != null) {
                    this.endSequence(context);
                }
                return;
            }
            catch (Throwable throwable) {
                if (context != null) {
                    this.endSequence(context);
                }
                throw throwable;
            }
        }
        sr.intersectClipBox(abox[0], abox[1], abox[2], abox[3]);
        context = this.startSequence(sg, s, devR, abox);
        this.spanClipLoop(context, sr, clipRegion, abox);
        if (context != null) {
            this.endSequence(context);
        }
    }

    public void spanClipLoop(Object ctx, SpanIterator sr, Region r, int[] abox) {
        if (!r.isRectangular()) {
            sr = r.filter(sr);
        }
        while (sr.nextSpan(abox)) {
            int x = abox[0];
            int y = abox[1];
            this.renderBox(ctx, x, y, abox[2] - x, abox[3] - y);
        }
    }

    public static class Simple
    extends SpanShapeRenderer
    implements LoopBasedPipe {
        @Override
        public Object startSequence(SunGraphics2D sg, Shape s, Rectangle devR, int[] bbox) {
            return sg;
        }

        @Override
        public void renderBox(Object ctx, int x, int y, int w, int h) {
            SunGraphics2D sg2d = (SunGraphics2D)ctx;
            SurfaceData sd = sg2d.getSurfaceData();
            sg2d.loops.fillRectLoop.FillRect(sg2d, sd, x, y, w, h);
        }

        @Override
        public void endSequence(Object ctx) {
        }
    }

    public static class Composite
    extends SpanShapeRenderer {
        CompositePipe comppipe;

        public Composite(CompositePipe pipe) {
            this.comppipe = pipe;
        }

        @Override
        public Object startSequence(SunGraphics2D sg, Shape s, Rectangle devR, int[] bbox) {
            return this.comppipe.startSequence(sg, s, devR, bbox);
        }

        @Override
        public void renderBox(Object ctx, int x, int y, int w, int h) {
            this.comppipe.renderPathTile(ctx, null, 0, w, x, y, w, h);
        }

        @Override
        public void endSequence(Object ctx) {
            this.comppipe.endSequence(ctx);
        }
    }
}

