/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.DrawParallelogram;
import sun.java2d.loops.FillParallelogram;
import sun.java2d.loops.FillSpans;
import sun.java2d.pipe.LoopBasedPipe;
import sun.java2d.pipe.ParallelogramPipe;
import sun.java2d.pipe.PixelDrawPipe;
import sun.java2d.pipe.PixelFillPipe;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.RenderingEngine;
import sun.java2d.pipe.ShapeDrawPipe;
import sun.java2d.pipe.ShapeSpanIterator;
import sun.java2d.pipe.SpanIterator;

public class LoopPipe
implements PixelDrawPipe,
PixelFillPipe,
ParallelogramPipe,
ShapeDrawPipe,
LoopBasedPipe {
    static final RenderingEngine RenderEngine = RenderingEngine.getInstance();

    @Override
    public void drawLine(SunGraphics2D sg2d, int x1, int y1, int x2, int y2) {
        int tX = sg2d.transX;
        int tY = sg2d.transY;
        sg2d.loops.drawLineLoop.DrawLine(sg2d, sg2d.getSurfaceData(), x1 + tX, y1 + tY, x2 + tX, y2 + tY);
    }

    @Override
    public void drawRect(SunGraphics2D sg2d, int x, int y, int width, int height) {
        sg2d.loops.drawRectLoop.DrawRect(sg2d, sg2d.getSurfaceData(), x + sg2d.transX, y + sg2d.transY, width, height);
    }

    @Override
    public void drawRoundRect(SunGraphics2D sg2d, int x, int y, int width, int height, int arcWidth, int arcHeight) {
        sg2d.shapepipe.draw(sg2d, new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight));
    }

    @Override
    public void drawOval(SunGraphics2D sg2d, int x, int y, int width, int height) {
        sg2d.shapepipe.draw(sg2d, new Ellipse2D.Float(x, y, width, height));
    }

    @Override
    public void drawArc(SunGraphics2D sg2d, int x, int y, int width, int height, int startAngle, int arcAngle) {
        sg2d.shapepipe.draw(sg2d, new Arc2D.Float(x, y, width, height, startAngle, arcAngle, 0));
    }

    @Override
    public void drawPolyline(SunGraphics2D sg2d, int[] xPoints, int[] yPoints, int nPoints) {
        int[] nPointsArray = new int[]{nPoints};
        sg2d.loops.drawPolygonsLoop.DrawPolygons(sg2d, sg2d.getSurfaceData(), xPoints, yPoints, nPointsArray, 1, sg2d.transX, sg2d.transY, false);
    }

    @Override
    public void drawPolygon(SunGraphics2D sg2d, int[] xPoints, int[] yPoints, int nPoints) {
        int[] nPointsArray = new int[]{nPoints};
        sg2d.loops.drawPolygonsLoop.DrawPolygons(sg2d, sg2d.getSurfaceData(), xPoints, yPoints, nPointsArray, 1, sg2d.transX, sg2d.transY, true);
    }

    @Override
    public void fillRect(SunGraphics2D sg2d, int x, int y, int width, int height) {
        sg2d.loops.fillRectLoop.FillRect(sg2d, sg2d.getSurfaceData(), x + sg2d.transX, y + sg2d.transY, width, height);
    }

    @Override
    public void fillRoundRect(SunGraphics2D sg2d, int x, int y, int width, int height, int arcWidth, int arcHeight) {
        sg2d.shapepipe.fill(sg2d, new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight));
    }

    @Override
    public void fillOval(SunGraphics2D sg2d, int x, int y, int width, int height) {
        sg2d.shapepipe.fill(sg2d, new Ellipse2D.Float(x, y, width, height));
    }

    @Override
    public void fillArc(SunGraphics2D sg2d, int x, int y, int width, int height, int startAngle, int arcAngle) {
        sg2d.shapepipe.fill(sg2d, new Arc2D.Float(x, y, width, height, startAngle, arcAngle, 2));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void fillPolygon(SunGraphics2D sg2d, int[] xPoints, int[] yPoints, int nPoints) {
        ShapeSpanIterator sr = LoopPipe.getFillSSI(sg2d);
        try {
            sr.setOutputArea(sg2d.getCompClip());
            sr.appendPoly(xPoints, yPoints, nPoints, sg2d.transX, sg2d.transY);
            LoopPipe.fillSpans(sg2d, sr);
        }
        finally {
            sr.dispose();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void draw(SunGraphics2D sg2d, Shape s) {
        if (sg2d.strokeState == 0) {
            int transY;
            int transX;
            Path2D.Float p2df;
            if (sg2d.transformState <= 1) {
                p2df = s instanceof Path2D.Float ? (Path2D.Float)s : new Path2D.Float(s);
                transX = sg2d.transX;
                transY = sg2d.transY;
            } else {
                p2df = new Path2D.Float(s, sg2d.transform);
                transX = 0;
                transY = 0;
            }
            sg2d.loops.drawPathLoop.DrawPath(sg2d, sg2d.getSurfaceData(), transX, transY, p2df);
            return;
        }
        if (sg2d.strokeState == 3) {
            this.fill(sg2d, sg2d.stroke.createStrokedShape(s));
            return;
        }
        ShapeSpanIterator sr = LoopPipe.getStrokeSpans(sg2d, s);
        try {
            LoopPipe.fillSpans(sg2d, sr);
        }
        finally {
            sr.dispose();
        }
    }

    public static ShapeSpanIterator getFillSSI(SunGraphics2D sg2d) {
        boolean adjust = sg2d.stroke instanceof BasicStroke && sg2d.strokeHint != 2;
        return new ShapeSpanIterator(adjust);
    }

    public static ShapeSpanIterator getStrokeSpans(SunGraphics2D sg2d, Shape s) {
        ShapeSpanIterator sr = new ShapeSpanIterator(false);
        try {
            Region clip = sg2d.getCompClip();
            sr.setOutputArea(clip);
            sr.setRule(1);
            BasicStroke bs = (BasicStroke)sg2d.stroke;
            boolean thin = sg2d.strokeState <= 1;
            boolean normalize = sg2d.strokeHint != 2;
            RenderEngine.strokeTo(s, sg2d.transform, clip, bs, thin, normalize, false, sr);
        }
        catch (Throwable t) {
            sr.dispose();
            sr = null;
            throw new InternalError("Unable to Stroke shape (" + t.getMessage() + ")", t);
        }
        return sr;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void fill(SunGraphics2D sg2d, Shape s) {
        if (sg2d.strokeState == 0) {
            int transY;
            int transX;
            Path2D.Float p2df;
            if (sg2d.transformState <= 1) {
                p2df = s instanceof Path2D.Float ? (Path2D.Float)s : new Path2D.Float(s);
                transX = sg2d.transX;
                transY = sg2d.transY;
            } else {
                p2df = new Path2D.Float(s, sg2d.transform);
                transX = 0;
                transY = 0;
            }
            sg2d.loops.fillPathLoop.FillPath(sg2d, sg2d.getSurfaceData(), transX, transY, p2df);
            return;
        }
        ShapeSpanIterator sr = LoopPipe.getFillSSI(sg2d);
        try {
            sr.setOutputArea(sg2d.getCompClip());
            AffineTransform at = sg2d.transformState == 0 ? null : sg2d.transform;
            sr.appendPath(s.getPathIterator(at));
            LoopPipe.fillSpans(sg2d, sr);
        }
        finally {
            sr.dispose();
        }
    }

    private static void fillSpans(SunGraphics2D sg2d, SpanIterator si) {
        if (sg2d.clipState == 2) {
            si = sg2d.clipRegion.filter(si);
        } else {
            FillSpans fs = sg2d.loops.fillSpansLoop;
            if (fs != null) {
                fs.FillSpans(sg2d, sg2d.getSurfaceData(), si);
                return;
            }
        }
        int[] spanbox = new int[4];
        SurfaceData sd = sg2d.getSurfaceData();
        while (si.nextSpan(spanbox)) {
            int x = spanbox[0];
            int y = spanbox[1];
            int w = spanbox[2] - x;
            int h = spanbox[3] - y;
            sg2d.loops.fillRectLoop.FillRect(sg2d, sd, x, y, w, h);
        }
    }

    @Override
    public void fillParallelogram(SunGraphics2D sg2d, double ux1, double uy1, double ux2, double uy2, double x, double y, double dx1, double dy1, double dx2, double dy2) {
        FillParallelogram fp = sg2d.loops.fillParallelogramLoop;
        fp.FillParallelogram(sg2d, sg2d.getSurfaceData(), x, y, dx1, dy1, dx2, dy2);
    }

    @Override
    public void drawParallelogram(SunGraphics2D sg2d, double ux1, double uy1, double ux2, double uy2, double x, double y, double dx1, double dy1, double dx2, double dy2, double lw1, double lw2) {
        DrawParallelogram dp = sg2d.loops.drawParallelogramLoop;
        dp.DrawParallelogram(sg2d, sg2d.getSurfaceData(), x, y, dx1, dy1, dx2, dy2, lw1, lw2);
    }
}

