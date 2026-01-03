/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import sun.java2d.SunGraphics2D;
import sun.java2d.pipe.PixelDrawPipe;
import sun.java2d.pipe.PixelFillPipe;
import sun.java2d.pipe.ShapeDrawPipe;

public class PixelToShapeConverter
implements PixelDrawPipe,
PixelFillPipe {
    ShapeDrawPipe outpipe;

    public PixelToShapeConverter(ShapeDrawPipe pipe) {
        this.outpipe = pipe;
    }

    @Override
    public void drawLine(SunGraphics2D sg, int x1, int y1, int x2, int y2) {
        this.outpipe.draw(sg, new Line2D.Float(x1, y1, x2, y2));
    }

    @Override
    public void drawRect(SunGraphics2D sg, int x, int y, int w, int h) {
        this.outpipe.draw(sg, new Rectangle(x, y, w, h));
    }

    @Override
    public void fillRect(SunGraphics2D sg, int x, int y, int w, int h) {
        this.outpipe.fill(sg, new Rectangle(x, y, w, h));
    }

    @Override
    public void drawRoundRect(SunGraphics2D sg, int x, int y, int w, int h, int aW, int aH) {
        this.outpipe.draw(sg, new RoundRectangle2D.Float(x, y, w, h, aW, aH));
    }

    @Override
    public void fillRoundRect(SunGraphics2D sg, int x, int y, int w, int h, int aW, int aH) {
        this.outpipe.fill(sg, new RoundRectangle2D.Float(x, y, w, h, aW, aH));
    }

    @Override
    public void drawOval(SunGraphics2D sg, int x, int y, int w, int h) {
        this.outpipe.draw(sg, new Ellipse2D.Float(x, y, w, h));
    }

    @Override
    public void fillOval(SunGraphics2D sg, int x, int y, int w, int h) {
        this.outpipe.fill(sg, new Ellipse2D.Float(x, y, w, h));
    }

    @Override
    public void drawArc(SunGraphics2D sg, int x, int y, int w, int h, int start, int extent) {
        this.outpipe.draw(sg, new Arc2D.Float(x, y, w, h, start, extent, 0));
    }

    @Override
    public void fillArc(SunGraphics2D sg, int x, int y, int w, int h, int start, int extent) {
        this.outpipe.fill(sg, new Arc2D.Float(x, y, w, h, start, extent, 2));
    }

    private Shape makePoly(int[] xPoints, int[] yPoints, int nPoints, boolean close) {
        GeneralPath gp = new GeneralPath(0);
        if (nPoints > 0) {
            gp.moveTo(xPoints[0], yPoints[0]);
            for (int i = 1; i < nPoints; ++i) {
                gp.lineTo(xPoints[i], yPoints[i]);
            }
            if (close) {
                gp.closePath();
            }
        }
        return gp;
    }

    @Override
    public void drawPolyline(SunGraphics2D sg, int[] xPoints, int[] yPoints, int nPoints) {
        this.outpipe.draw(sg, this.makePoly(xPoints, yPoints, nPoints, false));
    }

    @Override
    public void drawPolygon(SunGraphics2D sg, int[] xPoints, int[] yPoints, int nPoints) {
        this.outpipe.draw(sg, this.makePoly(xPoints, yPoints, nPoints, true));
    }

    @Override
    public void fillPolygon(SunGraphics2D sg, int[] xPoints, int[] yPoints, int nPoints) {
        this.outpipe.fill(sg, this.makePoly(xPoints, yPoints, nPoints, true));
    }
}

