/*
 * Decompiled with CFR 0.152.
 */
package java.awt.geom;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.LineIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

public abstract class Line2D
implements Shape,
Cloneable {
    protected Line2D() {
    }

    public abstract double getX1();

    public abstract double getY1();

    public abstract Point2D getP1();

    public abstract double getX2();

    public abstract double getY2();

    public abstract Point2D getP2();

    public abstract void setLine(double var1, double var3, double var5, double var7);

    public void setLine(Point2D p1, Point2D p2) {
        this.setLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    public void setLine(Line2D l) {
        this.setLine(l.getX1(), l.getY1(), l.getX2(), l.getY2());
    }

    public static int relativeCCW(double x1, double y1, double x2, double y2, double px, double py) {
        double ccw = (px -= x1) * (y2 -= y1) - (py -= y1) * (x2 -= x1);
        if (ccw == 0.0 && (ccw = px * x2 + py * y2) > 0.0 && (ccw = (px -= x2) * x2 + (py -= y2) * y2) < 0.0) {
            ccw = 0.0;
        }
        return ccw < 0.0 ? -1 : (ccw > 0.0 ? 1 : 0);
    }

    public int relativeCCW(double px, double py) {
        return Line2D.relativeCCW(this.getX1(), this.getY1(), this.getX2(), this.getY2(), px, py);
    }

    public int relativeCCW(Point2D p) {
        return Line2D.relativeCCW(this.getX1(), this.getY1(), this.getX2(), this.getY2(), p.getX(), p.getY());
    }

    public static boolean linesIntersect(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        return Line2D.relativeCCW(x1, y1, x2, y2, x3, y3) * Line2D.relativeCCW(x1, y1, x2, y2, x4, y4) <= 0 && Line2D.relativeCCW(x3, y3, x4, y4, x1, y1) * Line2D.relativeCCW(x3, y3, x4, y4, x2, y2) <= 0;
    }

    public boolean intersectsLine(double x1, double y1, double x2, double y2) {
        return Line2D.linesIntersect(x1, y1, x2, y2, this.getX1(), this.getY1(), this.getX2(), this.getY2());
    }

    public boolean intersectsLine(Line2D l) {
        return Line2D.linesIntersect(l.getX1(), l.getY1(), l.getX2(), l.getY2(), this.getX1(), this.getY1(), this.getX2(), this.getY2());
    }

    public static double ptSegDistSq(double x1, double y1, double x2, double y2, double px, double py) {
        double projlenSq;
        double lenSq;
        double dotprod = (px -= x1) * (x2 -= x1) + (py -= y1) * (y2 -= y1);
        if ((lenSq = px * px + py * py - (projlenSq = dotprod <= 0.0 ? 0.0 : ((dotprod = (px = x2 - px) * x2 + (py = y2 - py) * y2) <= 0.0 ? 0.0 : dotprod * dotprod / (x2 * x2 + y2 * y2)))) < 0.0) {
            lenSq = 0.0;
        }
        return lenSq;
    }

    public static double ptSegDist(double x1, double y1, double x2, double y2, double px, double py) {
        return Math.sqrt(Line2D.ptSegDistSq(x1, y1, x2, y2, px, py));
    }

    public double ptSegDistSq(double px, double py) {
        return Line2D.ptSegDistSq(this.getX1(), this.getY1(), this.getX2(), this.getY2(), px, py);
    }

    public double ptSegDistSq(Point2D pt) {
        return Line2D.ptSegDistSq(this.getX1(), this.getY1(), this.getX2(), this.getY2(), pt.getX(), pt.getY());
    }

    public double ptSegDist(double px, double py) {
        return Line2D.ptSegDist(this.getX1(), this.getY1(), this.getX2(), this.getY2(), px, py);
    }

    public double ptSegDist(Point2D pt) {
        return Line2D.ptSegDist(this.getX1(), this.getY1(), this.getX2(), this.getY2(), pt.getX(), pt.getY());
    }

    public static double ptLineDistSq(double x1, double y1, double x2, double y2, double px, double py) {
        double dotprod;
        double projlenSq;
        double lenSq;
        if ((lenSq = (px -= x1) * px + (py -= y1) * py - (projlenSq = (dotprod = px * (x2 -= x1) + py * (y2 -= y1)) * dotprod / (x2 * x2 + y2 * y2))) < 0.0) {
            lenSq = 0.0;
        }
        return lenSq;
    }

    public static double ptLineDist(double x1, double y1, double x2, double y2, double px, double py) {
        return Math.sqrt(Line2D.ptLineDistSq(x1, y1, x2, y2, px, py));
    }

    public double ptLineDistSq(double px, double py) {
        return Line2D.ptLineDistSq(this.getX1(), this.getY1(), this.getX2(), this.getY2(), px, py);
    }

    public double ptLineDistSq(Point2D pt) {
        return Line2D.ptLineDistSq(this.getX1(), this.getY1(), this.getX2(), this.getY2(), pt.getX(), pt.getY());
    }

    public double ptLineDist(double px, double py) {
        return Line2D.ptLineDist(this.getX1(), this.getY1(), this.getX2(), this.getY2(), px, py);
    }

    public double ptLineDist(Point2D pt) {
        return Line2D.ptLineDist(this.getX1(), this.getY1(), this.getX2(), this.getY2(), pt.getX(), pt.getY());
    }

    @Override
    public boolean contains(double x, double y) {
        return false;
    }

    @Override
    public boolean contains(Point2D p) {
        return false;
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return this.intersects(new Rectangle2D.Double(x, y, w, h));
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return r.intersectsLine(this.getX1(), this.getY1(), this.getX2(), this.getY2());
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return false;
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return false;
    }

    @Override
    public Rectangle getBounds() {
        return this.getBounds2D().getBounds();
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return new LineIterator(this, at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new LineIterator(this, at);
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    public static class Double
    extends Line2D
    implements Serializable {
        public double x1;
        public double y1;
        public double x2;
        public double y2;
        private static final long serialVersionUID = 7979627399746467499L;

        public Double() {
        }

        public Double(double x1, double y1, double x2, double y2) {
            this.setLine(x1, y1, x2, y2);
        }

        public Double(Point2D p1, Point2D p2) {
            this.setLine(p1, p2);
        }

        @Override
        public double getX1() {
            return this.x1;
        }

        @Override
        public double getY1() {
            return this.y1;
        }

        @Override
        public Point2D getP1() {
            return new Point2D.Double(this.x1, this.y1);
        }

        @Override
        public double getX2() {
            return this.x2;
        }

        @Override
        public double getY2() {
            return this.y2;
        }

        @Override
        public Point2D getP2() {
            return new Point2D.Double(this.x2, this.y2);
        }

        @Override
        public void setLine(double x1, double y1, double x2, double y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        @Override
        public Rectangle2D getBounds2D() {
            double h;
            double y;
            double w;
            double x;
            if (this.x1 < this.x2) {
                x = this.x1;
                w = this.x2 - this.x1;
            } else {
                x = this.x2;
                w = this.x1 - this.x2;
            }
            if (this.y1 < this.y2) {
                y = this.y1;
                h = this.y2 - this.y1;
            } else {
                y = this.y2;
                h = this.y1 - this.y2;
            }
            return new Rectangle2D.Double(x, y, w, h);
        }
    }

    public static class Float
    extends Line2D
    implements Serializable {
        public float x1;
        public float y1;
        public float x2;
        public float y2;
        private static final long serialVersionUID = 6161772511649436349L;

        public Float() {
        }

        public Float(float x1, float y1, float x2, float y2) {
            this.setLine(x1, y1, x2, y2);
        }

        public Float(Point2D p1, Point2D p2) {
            this.setLine(p1, p2);
        }

        @Override
        public double getX1() {
            return this.x1;
        }

        @Override
        public double getY1() {
            return this.y1;
        }

        @Override
        public Point2D getP1() {
            return new Point2D.Float(this.x1, this.y1);
        }

        @Override
        public double getX2() {
            return this.x2;
        }

        @Override
        public double getY2() {
            return this.y2;
        }

        @Override
        public Point2D getP2() {
            return new Point2D.Float(this.x2, this.y2);
        }

        @Override
        public void setLine(double x1, double y1, double x2, double y2) {
            this.x1 = (float)x1;
            this.y1 = (float)y1;
            this.x2 = (float)x2;
            this.y2 = (float)y2;
        }

        public void setLine(float x1, float y1, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        @Override
        public Rectangle2D getBounds2D() {
            float h;
            float y;
            float w;
            float x;
            if (this.x1 < this.x2) {
                x = this.x1;
                w = this.x2 - this.x1;
            } else {
                x = this.x2;
                w = this.x1 - this.x2;
            }
            if (this.y1 < this.y2) {
                y = this.y1;
                h = this.y2 - this.y1;
            } else {
                y = this.y2;
                h = this.y1 - this.y2;
            }
            return new Rectangle2D.Float(x, y, w, h);
        }
    }
}

