/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import sun.java2d.marlin.Helpers;

final class Curve {
    double ax;
    double ay;
    double bx;
    double by;
    double cx;
    double cy;
    double dx;
    double dy;
    double dax;
    double day;
    double dbx;
    double dby;

    Curve() {
    }

    void set(double[] points, int type) {
        if (type == 8) {
            this.set(points[0], points[1], points[2], points[3], points[4], points[5], points[6], points[7]);
        } else if (type == 4) {
            this.set(points[0], points[1], points[2], points[3]);
        } else {
            this.set(points[0], points[1], points[2], points[3], points[4], points[5]);
        }
    }

    void set(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double dx32 = 3.0 * (x3 - x2);
        double dy32 = 3.0 * (y3 - y2);
        double dx21 = 3.0 * (x2 - x1);
        double dy21 = 3.0 * (y2 - y1);
        this.ax = x4 - x1 - dx32;
        this.ay = y4 - y1 - dy32;
        this.bx = dx32 - dx21;
        this.by = dy32 - dy21;
        this.cx = dx21;
        this.cy = dy21;
        this.dx = x1;
        this.dy = y1;
        this.dax = 3.0 * this.ax;
        this.day = 3.0 * this.ay;
        this.dbx = 2.0 * this.bx;
        this.dby = 2.0 * this.by;
    }

    void set(double x1, double y1, double x2, double y2, double x3, double y3) {
        double dx21 = x2 - x1;
        double dy21 = y2 - y1;
        this.ax = 0.0;
        this.ay = 0.0;
        this.bx = x3 - x2 - dx21;
        this.by = y3 - y2 - dy21;
        this.cx = 2.0 * dx21;
        this.cy = 2.0 * dy21;
        this.dx = x1;
        this.dy = y1;
        this.dax = 0.0;
        this.day = 0.0;
        this.dbx = 2.0 * this.bx;
        this.dby = 2.0 * this.by;
    }

    void set(double x1, double y1, double x2, double y2) {
        double dx21 = x2 - x1;
        double dy21 = y2 - y1;
        this.ax = 0.0;
        this.ay = 0.0;
        this.bx = 0.0;
        this.by = 0.0;
        this.cx = dx21;
        this.cy = dy21;
        this.dx = x1;
        this.dy = y1;
        this.dax = 0.0;
        this.day = 0.0;
        this.dbx = 0.0;
        this.dby = 0.0;
    }

    int dxRoots(double[] roots, int off) {
        return Helpers.quadraticRoots(this.dax, this.dbx, this.cx, roots, off);
    }

    int dyRoots(double[] roots, int off) {
        return Helpers.quadraticRoots(this.day, this.dby, this.cy, roots, off);
    }

    int infPoints(double[] pts, int off) {
        double a = this.dax * this.dby - this.dbx * this.day;
        double b = 2.0 * (this.cy * this.dax - this.day * this.cx);
        double c = this.cy * this.dbx - this.cx * this.dby;
        return Helpers.quadraticRoots(a, b, c, pts, off);
    }

    int xPoints(double[] ts, int off, double x) {
        return Helpers.cubicRootsInAB(this.ax, this.bx, this.cx, this.dx - x, ts, off, 0.0, 1.0);
    }

    int yPoints(double[] ts, int off, double y) {
        return Helpers.cubicRootsInAB(this.ay, this.by, this.cy, this.dy - y, ts, off, 0.0, 1.0);
    }

    private int perpendiculardfddf(double[] pts, int off) {
        assert (pts.length >= off + 4);
        double a = 2.0 * (this.dax * this.dax + this.day * this.day);
        double b = 3.0 * (this.dax * this.dbx + this.day * this.dby);
        double c = 2.0 * (this.dax * this.cx + this.day * this.cy) + this.dbx * this.dbx + this.dby * this.dby;
        double d = this.dbx * this.cx + this.dby * this.cy;
        return Helpers.cubicRootsInAB(a, b, c, d, pts, off, 0.0, 1.0);
    }

    int rootsOfROCMinusW(double[] roots, int off, double w2, double err) {
        assert (off <= 6 && roots.length >= 10);
        int ret = off;
        int end = off + this.perpendiculardfddf(roots, off);
        roots[end] = 1.0;
        double t0 = 0.0;
        double ft0 = this.ROCsq(t0) - w2;
        for (int i = off; i <= end; ++i) {
            double t1 = roots[i];
            double ft1 = this.ROCsq(t1) - w2;
            if (ft0 == 0.0) {
                roots[ret++] = t0;
            } else if (ft1 * ft0 < 0.0) {
                roots[ret++] = this.falsePositionROCsqMinusX(t0, t1, w2, err);
            }
            t0 = t1;
            ft0 = ft1;
        }
        return ret - off;
    }

    private static double eliminateInf(double x) {
        return x == Double.POSITIVE_INFINITY ? Double.MAX_VALUE : (x == Double.NEGATIVE_INFINITY ? Double.MIN_VALUE : x);
    }

    private double falsePositionROCsqMinusX(double t0, double t1, double w2, double err) {
        int iterLimit = 100;
        int side = 0;
        double t = t1;
        double ft = Curve.eliminateInf(this.ROCsq(t) - w2);
        double s = t0;
        double fs = Curve.eliminateInf(this.ROCsq(s) - w2);
        double r = s;
        for (int i = 0; i < 100 && Math.abs(t - s) > err * Math.abs(t + s); ++i) {
            r = (fs * t - ft * s) / (fs - ft);
            double fr = this.ROCsq(r) - w2;
            if (Curve.sameSign(fr, ft)) {
                ft = fr;
                t = r;
                if (side < 0) {
                    fs /= (double)(1 << -side);
                    --side;
                    continue;
                }
                side = -1;
                continue;
            }
            if (!(fr * fs > 0.0)) break;
            fs = fr;
            s = r;
            if (side > 0) {
                ft /= (double)(1 << side);
                ++side;
                continue;
            }
            side = 1;
        }
        return r;
    }

    private static boolean sameSign(double x, double y) {
        return x < 0.0 && y < 0.0 || x > 0.0 && y > 0.0;
    }

    private double ROCsq(double t) {
        double dx = t * (t * this.dax + this.dbx) + this.cx;
        double dy = t * (t * this.day + this.dby) + this.cy;
        double ddx = 2.0 * this.dax * t + this.dbx;
        double ddy = 2.0 * this.day * t + this.dby;
        double dx2dy2 = dx * dx + dy * dy;
        double ddx2ddy2 = ddx * ddx + ddy * ddy;
        double ddxdxddydy = ddx * dx + ddy * dy;
        return dx2dy2 * (dx2dy2 * dx2dy2 / (dx2dy2 * ddx2ddy2 - ddxdxddydy * ddxdxddydy));
    }
}

