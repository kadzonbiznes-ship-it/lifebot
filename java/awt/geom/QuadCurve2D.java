/*
 * Decompiled with CFR 0.152.
 */
package java.awt.geom;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadIterator;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

public abstract class QuadCurve2D
implements Shape,
Cloneable {
    private static final int BELOW = -2;
    private static final int LOWEDGE = -1;
    private static final int INSIDE = 0;
    private static final int HIGHEDGE = 1;
    private static final int ABOVE = 2;

    protected QuadCurve2D() {
    }

    public abstract double getX1();

    public abstract double getY1();

    public abstract Point2D getP1();

    public abstract double getCtrlX();

    public abstract double getCtrlY();

    public abstract Point2D getCtrlPt();

    public abstract double getX2();

    public abstract double getY2();

    public abstract Point2D getP2();

    public abstract void setCurve(double var1, double var3, double var5, double var7, double var9, double var11);

    public void setCurve(double[] coords, int offset) {
        this.setCurve(coords[offset + 0], coords[offset + 1], coords[offset + 2], coords[offset + 3], coords[offset + 4], coords[offset + 5]);
    }

    public void setCurve(Point2D p1, Point2D cp, Point2D p2) {
        this.setCurve(p1.getX(), p1.getY(), cp.getX(), cp.getY(), p2.getX(), p2.getY());
    }

    public void setCurve(Point2D[] pts, int offset) {
        this.setCurve(pts[offset + 0].getX(), pts[offset + 0].getY(), pts[offset + 1].getX(), pts[offset + 1].getY(), pts[offset + 2].getX(), pts[offset + 2].getY());
    }

    public void setCurve(QuadCurve2D c) {
        this.setCurve(c.getX1(), c.getY1(), c.getCtrlX(), c.getCtrlY(), c.getX2(), c.getY2());
    }

    public static double getFlatnessSq(double x1, double y1, double ctrlx, double ctrly, double x2, double y2) {
        return Line2D.ptSegDistSq(x1, y1, x2, y2, ctrlx, ctrly);
    }

    public static double getFlatness(double x1, double y1, double ctrlx, double ctrly, double x2, double y2) {
        return Line2D.ptSegDist(x1, y1, x2, y2, ctrlx, ctrly);
    }

    public static double getFlatnessSq(double[] coords, int offset) {
        return Line2D.ptSegDistSq(coords[offset + 0], coords[offset + 1], coords[offset + 4], coords[offset + 5], coords[offset + 2], coords[offset + 3]);
    }

    public static double getFlatness(double[] coords, int offset) {
        return Line2D.ptSegDist(coords[offset + 0], coords[offset + 1], coords[offset + 4], coords[offset + 5], coords[offset + 2], coords[offset + 3]);
    }

    public double getFlatnessSq() {
        return Line2D.ptSegDistSq(this.getX1(), this.getY1(), this.getX2(), this.getY2(), this.getCtrlX(), this.getCtrlY());
    }

    public double getFlatness() {
        return Line2D.ptSegDist(this.getX1(), this.getY1(), this.getX2(), this.getY2(), this.getCtrlX(), this.getCtrlY());
    }

    public void subdivide(QuadCurve2D left, QuadCurve2D right) {
        QuadCurve2D.subdivide(this, left, right);
    }

    public static void subdivide(QuadCurve2D src, QuadCurve2D left, QuadCurve2D right) {
        double x1 = src.getX1();
        double y1 = src.getY1();
        double ctrlx = src.getCtrlX();
        double ctrly = src.getCtrlY();
        double x2 = src.getX2();
        double y2 = src.getY2();
        double ctrlx1 = (x1 + ctrlx) / 2.0;
        double ctrly1 = (y1 + ctrly) / 2.0;
        double ctrlx2 = (x2 + ctrlx) / 2.0;
        double ctrly2 = (y2 + ctrly) / 2.0;
        ctrlx = (ctrlx1 + ctrlx2) / 2.0;
        ctrly = (ctrly1 + ctrly2) / 2.0;
        if (left != null) {
            left.setCurve(x1, y1, ctrlx1, ctrly1, ctrlx, ctrly);
        }
        if (right != null) {
            right.setCurve(ctrlx, ctrly, ctrlx2, ctrly2, x2, y2);
        }
    }

    public static void subdivide(double[] src, int srcoff, double[] left, int leftoff, double[] right, int rightoff) {
        double x1 = src[srcoff + 0];
        double y1 = src[srcoff + 1];
        double ctrlx = src[srcoff + 2];
        double ctrly = src[srcoff + 3];
        double x2 = src[srcoff + 4];
        double y2 = src[srcoff + 5];
        if (left != null) {
            left[leftoff + 0] = x1;
            left[leftoff + 1] = y1;
        }
        if (right != null) {
            right[rightoff + 4] = x2;
            right[rightoff + 5] = y2;
        }
        x1 = (x1 + ctrlx) / 2.0;
        y1 = (y1 + ctrly) / 2.0;
        x2 = (x2 + ctrlx) / 2.0;
        y2 = (y2 + ctrly) / 2.0;
        ctrlx = (x1 + x2) / 2.0;
        ctrly = (y1 + y2) / 2.0;
        if (left != null) {
            left[leftoff + 2] = x1;
            left[leftoff + 3] = y1;
            left[leftoff + 4] = ctrlx;
            left[leftoff + 5] = ctrly;
        }
        if (right != null) {
            right[rightoff + 0] = ctrlx;
            right[rightoff + 1] = ctrly;
            right[rightoff + 2] = x2;
            right[rightoff + 3] = y2;
        }
    }

    public static int solveQuadratic(double[] eqn) {
        return QuadCurve2D.solveQuadratic(eqn, eqn);
    }

    public static int solveQuadratic(double[] eqn, double[] res) {
        double a = eqn[2];
        double b = eqn[1];
        double c = eqn[0];
        int roots = 0;
        if (a == 0.0) {
            if (b == 0.0) {
                return -1;
            }
            res[roots++] = -c / b;
        } else {
            double d = b * b - 4.0 * a * c;
            if (d < 0.0) {
                return 0;
            }
            d = Math.sqrt(d);
            if (b < 0.0) {
                d = -d;
            }
            double q = (b + d) / -2.0;
            res[roots++] = q / a;
            if (q != 0.0) {
                res[roots++] = c / q;
            }
        }
        return roots;
    }

    @Override
    public boolean contains(double x, double y) {
        double dyl;
        double dxl;
        double kx;
        double dy;
        double x1 = this.getX1();
        double y1 = this.getY1();
        double xc = this.getCtrlX();
        double yc = this.getCtrlY();
        double x2 = this.getX2();
        double dx = x - x1;
        double y2 = this.getY2();
        double ky = y1 - 2.0 * yc + y2;
        double t0 = (dx * ky - (dy = y - y1) * (kx = x1 - 2.0 * xc + x2)) / ((dxl = x2 - x1) * ky - (dyl = y2 - y1) * kx);
        if (t0 < 0.0 || t0 > 1.0 || t0 != t0) {
            return false;
        }
        double xb = kx * t0 * t0 + 2.0 * (xc - x1) * t0 + x1;
        double yb = ky * t0 * t0 + 2.0 * (yc - y1) * t0 + y1;
        double xl = dxl * t0 + x1;
        double yl = dyl * t0 + y1;
        return x >= xb && x < xl || x >= xl && x < xb || y >= yb && y < yl || y >= yl && y < yb;
    }

    @Override
    public boolean contains(Point2D p) {
        return this.contains(p.getX(), p.getY());
    }

    private static void fillEqn(double[] eqn, double val, double c1, double cp, double c2) {
        eqn[0] = c1 - val;
        eqn[1] = cp + cp - c1 - c1;
        eqn[2] = c1 - cp - cp + c2;
    }

    private static int evalQuadratic(double[] vals, int num, boolean include0, boolean include1, double[] inflect, double c1, double ctrl, double c2) {
        int j = 0;
        for (int i = 0; i < num; ++i) {
            double t = vals[i];
            if (!(include0 ? t >= 0.0 : t > 0.0) || !(include1 ? t <= 1.0 : t < 1.0) || inflect != null && inflect[1] + 2.0 * inflect[2] * t == 0.0) continue;
            double u = 1.0 - t;
            vals[j++] = c1 * u * u + 2.0 * ctrl * t * u + c2 * t * t;
        }
        return j;
    }

    private static int getTag(double coord, double low, double high) {
        if (coord <= low) {
            return coord < low ? -2 : -1;
        }
        if (coord >= high) {
            return coord > high ? 2 : 1;
        }
        return 0;
    }

    private static boolean inwards(int pttag, int opt1tag, int opt2tag) {
        switch (pttag) {
            default: {
                return false;
            }
            case -1: {
                return opt1tag >= 0 || opt2tag >= 0;
            }
            case 0: {
                return true;
            }
            case 1: 
        }
        return opt1tag <= 0 || opt2tag <= 0;
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        int c2tag;
        boolean yoverlap;
        if (w <= 0.0 || h <= 0.0) {
            return false;
        }
        double x1 = this.getX1();
        double y1 = this.getY1();
        int x1tag = QuadCurve2D.getTag(x1, x, x + w);
        int y1tag = QuadCurve2D.getTag(y1, y, y + h);
        if (x1tag == 0 && y1tag == 0) {
            return true;
        }
        double x2 = this.getX2();
        double y2 = this.getY2();
        int x2tag = QuadCurve2D.getTag(x2, x, x + w);
        int y2tag = QuadCurve2D.getTag(y2, y, y + h);
        if (x2tag == 0 && y2tag == 0) {
            return true;
        }
        double ctrlx = this.getCtrlX();
        double ctrly = this.getCtrlY();
        int ctrlxtag = QuadCurve2D.getTag(ctrlx, x, x + w);
        int ctrlytag = QuadCurve2D.getTag(ctrly, y, y + h);
        if (x1tag < 0 && x2tag < 0 && ctrlxtag < 0) {
            return false;
        }
        if (y1tag < 0 && y2tag < 0 && ctrlytag < 0) {
            return false;
        }
        if (x1tag > 0 && x2tag > 0 && ctrlxtag > 0) {
            return false;
        }
        if (y1tag > 0 && y2tag > 0 && ctrlytag > 0) {
            return false;
        }
        if (QuadCurve2D.inwards(x1tag, x2tag, ctrlxtag) && QuadCurve2D.inwards(y1tag, y2tag, ctrlytag)) {
            return true;
        }
        if (QuadCurve2D.inwards(x2tag, x1tag, ctrlxtag) && QuadCurve2D.inwards(y2tag, y1tag, ctrlytag)) {
            return true;
        }
        boolean xoverlap = x1tag * x2tag <= 0;
        boolean bl = yoverlap = y1tag * y2tag <= 0;
        if (x1tag == 0 && x2tag == 0 && yoverlap) {
            return true;
        }
        if (y1tag == 0 && y2tag == 0 && xoverlap) {
            return true;
        }
        double[] eqn = new double[3];
        double[] res = new double[3];
        if (!yoverlap) {
            QuadCurve2D.fillEqn(eqn, y1tag < 0 ? y : y + h, y1, ctrly, y2);
            return QuadCurve2D.solveQuadratic(eqn, res) == 2 && QuadCurve2D.evalQuadratic(res, 2, true, true, null, x1, ctrlx, x2) == 2 && QuadCurve2D.getTag(res[0], x, x + w) * QuadCurve2D.getTag(res[1], x, x + w) <= 0;
        }
        if (!xoverlap) {
            QuadCurve2D.fillEqn(eqn, x1tag < 0 ? x : x + w, x1, ctrlx, x2);
            return QuadCurve2D.solveQuadratic(eqn, res) == 2 && QuadCurve2D.evalQuadratic(res, 2, true, true, null, y1, ctrly, y2) == 2 && QuadCurve2D.getTag(res[0], y, y + h) * QuadCurve2D.getTag(res[1], y, y + h) <= 0;
        }
        double dx = x2 - x1;
        double dy = y2 - y1;
        double k = y2 * x1 - x2 * y1;
        int c1tag = y1tag == 0 ? x1tag : QuadCurve2D.getTag((k + dx * (y1tag < 0 ? y : y + h)) / dy, x, x + w);
        if (c1tag * (c2tag = y2tag == 0 ? x2tag : QuadCurve2D.getTag((k + dx * (y2tag < 0 ? y : y + h)) / dy, x, x + w)) <= 0) {
            return true;
        }
        c1tag = c1tag * x1tag <= 0 ? y1tag : y2tag;
        QuadCurve2D.fillEqn(eqn, c2tag < 0 ? x : x + w, x1, ctrlx, x2);
        int num = QuadCurve2D.solveQuadratic(eqn, res);
        QuadCurve2D.evalQuadratic(res, num, true, true, null, y1, ctrly, y2);
        c2tag = QuadCurve2D.getTag(res[0], y, y + h);
        return c1tag * c2tag <= 0;
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return this.intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        if (w <= 0.0 || h <= 0.0) {
            return false;
        }
        return this.contains(x, y) && this.contains(x + w, y) && this.contains(x + w, y + h) && this.contains(x, y + h);
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return this.contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public Rectangle2D getBounds2D() {
        return Path2D.getBounds2D(this.getPathIterator(null));
    }

    @Override
    public Rectangle getBounds() {
        return this.getBounds2D().getBounds();
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return new QuadIterator(this, at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new FlatteningPathIterator(this.getPathIterator(at), flatness);
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
    extends QuadCurve2D
    implements Serializable {
        public double x1;
        public double y1;
        public double ctrlx;
        public double ctrly;
        public double x2;
        public double y2;
        private static final long serialVersionUID = 4217149928428559721L;

        public Double() {
        }

        public Double(double x1, double y1, double ctrlx, double ctrly, double x2, double y2) {
            this.setCurve(x1, y1, ctrlx, ctrly, x2, y2);
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
        public double getCtrlX() {
            return this.ctrlx;
        }

        @Override
        public double getCtrlY() {
            return this.ctrly;
        }

        @Override
        public Point2D getCtrlPt() {
            return new Point2D.Double(this.ctrlx, this.ctrly);
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
        public void setCurve(double x1, double y1, double ctrlx, double ctrly, double x2, double y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.ctrlx = ctrlx;
            this.ctrly = ctrly;
            this.x2 = x2;
            this.y2 = y2;
        }
    }

    public static class Float
    extends QuadCurve2D
    implements Serializable {
        public float x1;
        public float y1;
        public float ctrlx;
        public float ctrly;
        public float x2;
        public float y2;
        private static final long serialVersionUID = -8511188402130719609L;

        public Float() {
        }

        public Float(float x1, float y1, float ctrlx, float ctrly, float x2, float y2) {
            this.setCurve(x1, y1, ctrlx, ctrly, x2, y2);
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
        public double getCtrlX() {
            return this.ctrlx;
        }

        @Override
        public double getCtrlY() {
            return this.ctrly;
        }

        @Override
        public Point2D getCtrlPt() {
            return new Point2D.Float(this.ctrlx, this.ctrly);
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
        public void setCurve(double x1, double y1, double ctrlx, double ctrly, double x2, double y2) {
            this.x1 = (float)x1;
            this.y1 = (float)y1;
            this.ctrlx = (float)ctrlx;
            this.ctrly = (float)ctrly;
            this.x2 = (float)x2;
            this.y2 = (float)y2;
        }

        public void setCurve(float x1, float y1, float ctrlx, float ctrly, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.ctrlx = ctrlx;
            this.ctrly = ctrly;
            this.x2 = x2;
            this.y2 = y2;
        }
    }
}

