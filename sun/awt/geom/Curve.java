/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.geom;

import java.awt.geom.IllegalPathStateException;
import java.awt.geom.PathIterator;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;
import sun.awt.geom.Crossings;
import sun.awt.geom.Order0;
import sun.awt.geom.Order1;
import sun.awt.geom.Order2;
import sun.awt.geom.Order3;

public abstract class Curve {
    public static final int INCREASING = 1;
    public static final int DECREASING = -1;
    protected int direction;
    public static final int RECT_INTERSECTS = Integer.MIN_VALUE;
    public static final double TMIN = 0.001;

    public static void insertMove(Vector<Curve> curves, double x, double y) {
        curves.add(new Order0(x, y));
    }

    public static void insertLine(Vector<Curve> curves, double x0, double y0, double x1, double y1) {
        if (y0 < y1) {
            curves.add(new Order1(x0, y0, x1, y1, 1));
        } else if (y0 > y1) {
            curves.add(new Order1(x1, y1, x0, y0, -1));
        }
    }

    public static void insertQuad(Vector<Curve> curves, double x0, double y0, double[] coords) {
        double y1 = coords[3];
        if (y0 > y1) {
            Order2.insert(curves, coords, coords[2], y1, coords[0], coords[1], x0, y0, -1);
        } else {
            if (y0 == y1 && y0 == coords[1]) {
                return;
            }
            Order2.insert(curves, coords, x0, y0, coords[0], coords[1], coords[2], y1, 1);
        }
    }

    public static void insertCubic(Vector<Curve> curves, double x0, double y0, double[] coords) {
        double y1 = coords[5];
        if (y0 > y1) {
            Order3.insert(curves, coords, coords[4], y1, coords[2], coords[3], coords[0], coords[1], x0, y0, -1);
        } else {
            if (y0 == y1 && y0 == coords[1] && y0 == coords[3]) {
                return;
            }
            Order3.insert(curves, coords, x0, y0, coords[0], coords[1], coords[2], coords[3], coords[4], y1, 1);
        }
    }

    public static int pointCrossingsForPath(PathIterator pi, double px, double py) {
        if (pi.isDone()) {
            return 0;
        }
        double[] coords = new double[6];
        if (pi.currentSegment(coords) != 0) {
            throw new IllegalPathStateException("missing initial moveto in path definition");
        }
        pi.next();
        double movx = coords[0];
        double movy = coords[1];
        double curx = movx;
        double cury = movy;
        int crossings = 0;
        while (!pi.isDone()) {
            switch (pi.currentSegment(coords)) {
                case 0: {
                    if (cury != movy) {
                        crossings += Curve.pointCrossingsForLine(px, py, curx, cury, movx, movy);
                    }
                    movx = curx = coords[0];
                    movy = cury = coords[1];
                    break;
                }
                case 1: {
                    double endx = coords[0];
                    double endy = coords[1];
                    crossings += Curve.pointCrossingsForLine(px, py, curx, cury, endx, endy);
                    curx = endx;
                    cury = endy;
                    break;
                }
                case 2: {
                    double endx = coords[2];
                    double endy = coords[3];
                    crossings += Curve.pointCrossingsForQuad(px, py, curx, cury, coords[0], coords[1], endx, endy, 0);
                    curx = endx;
                    cury = endy;
                    break;
                }
                case 3: {
                    double endx = coords[4];
                    double endy = coords[5];
                    crossings += Curve.pointCrossingsForCubic(px, py, curx, cury, coords[0], coords[1], coords[2], coords[3], endx, endy, 0);
                    curx = endx;
                    cury = endy;
                    break;
                }
                case 4: {
                    if (cury != movy) {
                        crossings += Curve.pointCrossingsForLine(px, py, curx, cury, movx, movy);
                    }
                    curx = movx;
                    cury = movy;
                }
            }
            pi.next();
        }
        if (cury != movy) {
            crossings += Curve.pointCrossingsForLine(px, py, curx, cury, movx, movy);
        }
        return crossings;
    }

    public static int pointCrossingsForLine(double px, double py, double x0, double y0, double x1, double y1) {
        if (py < y0 && py < y1) {
            return 0;
        }
        if (py >= y0 && py >= y1) {
            return 0;
        }
        if (px >= x0 && px >= x1) {
            return 0;
        }
        if (px < x0 && px < x1) {
            return y0 < y1 ? 1 : -1;
        }
        double xintercept = x0 + (py - y0) * (x1 - x0) / (y1 - y0);
        if (px >= xintercept) {
            return 0;
        }
        return y0 < y1 ? 1 : -1;
    }

    public static int pointCrossingsForQuad(double px, double py, double x0, double y0, double xc, double yc, double x1, double y1, int level) {
        if (py < y0 && py < yc && py < y1) {
            return 0;
        }
        if (py >= y0 && py >= yc && py >= y1) {
            return 0;
        }
        if (px >= x0 && px >= xc && px >= x1) {
            return 0;
        }
        if (px < x0 && px < xc && px < x1) {
            if (py >= y0) {
                if (py < y1) {
                    return 1;
                }
            } else if (py >= y1) {
                return -1;
            }
            return 0;
        }
        if (level > 52) {
            return Curve.pointCrossingsForLine(px, py, x0, y0, x1, y1);
        }
        double x0c = (x0 + xc) / 2.0;
        double y0c = (y0 + yc) / 2.0;
        double xc1 = (xc + x1) / 2.0;
        double yc1 = (yc + y1) / 2.0;
        xc = (x0c + xc1) / 2.0;
        yc = (y0c + yc1) / 2.0;
        if (Double.isNaN(xc) || Double.isNaN(yc)) {
            return 0;
        }
        return Curve.pointCrossingsForQuad(px, py, x0, y0, x0c, y0c, xc, yc, level + 1) + Curve.pointCrossingsForQuad(px, py, xc, yc, xc1, yc1, x1, y1, level + 1);
    }

    public static int pointCrossingsForCubic(double px, double py, double x0, double y0, double xc0, double yc0, double xc1, double yc1, double x1, double y1, int level) {
        if (py < y0 && py < yc0 && py < yc1 && py < y1) {
            return 0;
        }
        if (py >= y0 && py >= yc0 && py >= yc1 && py >= y1) {
            return 0;
        }
        if (px >= x0 && px >= xc0 && px >= xc1 && px >= x1) {
            return 0;
        }
        if (px < x0 && px < xc0 && px < xc1 && px < x1) {
            if (py >= y0) {
                if (py < y1) {
                    return 1;
                }
            } else if (py >= y1) {
                return -1;
            }
            return 0;
        }
        if (level > 52) {
            return Curve.pointCrossingsForLine(px, py, x0, y0, x1, y1);
        }
        double xmid = (xc0 + xc1) / 2.0;
        double ymid = (yc0 + yc1) / 2.0;
        xc0 = (x0 + xc0) / 2.0;
        yc0 = (y0 + yc0) / 2.0;
        xc1 = (xc1 + x1) / 2.0;
        yc1 = (yc1 + y1) / 2.0;
        double xc0m = (xc0 + xmid) / 2.0;
        double yc0m = (yc0 + ymid) / 2.0;
        double xmc1 = (xmid + xc1) / 2.0;
        double ymc1 = (ymid + yc1) / 2.0;
        xmid = (xc0m + xmc1) / 2.0;
        ymid = (yc0m + ymc1) / 2.0;
        if (Double.isNaN(xmid) || Double.isNaN(ymid)) {
            return 0;
        }
        return Curve.pointCrossingsForCubic(px, py, x0, y0, xc0, yc0, xc0m, yc0m, xmid, ymid, level + 1) + Curve.pointCrossingsForCubic(px, py, xmid, ymid, xmc1, ymc1, xc1, yc1, x1, y1, level + 1);
    }

    public static int rectCrossingsForPath(PathIterator pi, double rxmin, double rymin, double rxmax, double rymax) {
        double movy;
        double movx;
        if (rxmax <= rxmin || rymax <= rymin) {
            return 0;
        }
        if (pi.isDone()) {
            return 0;
        }
        double[] coords = new double[6];
        if (pi.currentSegment(coords) != 0) {
            throw new IllegalPathStateException("missing initial moveto in path definition");
        }
        pi.next();
        double curx = movx = coords[0];
        double cury = movy = coords[1];
        int crossings = 0;
        while (crossings != Integer.MIN_VALUE && !pi.isDone()) {
            switch (pi.currentSegment(coords)) {
                case 0: {
                    if (curx != movx || cury != movy) {
                        crossings = Curve.rectCrossingsForLine(crossings, rxmin, rymin, rxmax, rymax, curx, cury, movx, movy);
                    }
                    movx = curx = coords[0];
                    movy = cury = coords[1];
                    break;
                }
                case 1: {
                    double endx = coords[0];
                    double endy = coords[1];
                    crossings = Curve.rectCrossingsForLine(crossings, rxmin, rymin, rxmax, rymax, curx, cury, endx, endy);
                    curx = endx;
                    cury = endy;
                    break;
                }
                case 2: {
                    double endx = coords[2];
                    double endy = coords[3];
                    crossings = Curve.rectCrossingsForQuad(crossings, rxmin, rymin, rxmax, rymax, curx, cury, coords[0], coords[1], endx, endy, 0);
                    curx = endx;
                    cury = endy;
                    break;
                }
                case 3: {
                    double endx = coords[4];
                    double endy = coords[5];
                    crossings = Curve.rectCrossingsForCubic(crossings, rxmin, rymin, rxmax, rymax, curx, cury, coords[0], coords[1], coords[2], coords[3], endx, endy, 0);
                    curx = endx;
                    cury = endy;
                    break;
                }
                case 4: {
                    if (curx != movx || cury != movy) {
                        crossings = Curve.rectCrossingsForLine(crossings, rxmin, rymin, rxmax, rymax, curx, cury, movx, movy);
                    }
                    curx = movx;
                    cury = movy;
                }
            }
            pi.next();
        }
        if (crossings != Integer.MIN_VALUE && (curx != movx || cury != movy)) {
            crossings = Curve.rectCrossingsForLine(crossings, rxmin, rymin, rxmax, rymax, curx, cury, movx, movy);
        }
        return crossings;
    }

    public static int rectCrossingsForLine(int crossings, double rxmin, double rymin, double rxmax, double rymax, double x0, double y0, double x1, double y1) {
        if (y0 >= rymax && y1 >= rymax) {
            return crossings;
        }
        if (y0 <= rymin && y1 <= rymin) {
            return crossings;
        }
        if (x0 <= rxmin && x1 <= rxmin) {
            return crossings;
        }
        if (x0 >= rxmax && x1 >= rxmax) {
            if (y0 < y1) {
                if (y0 <= rymin) {
                    ++crossings;
                }
                if (y1 >= rymax) {
                    ++crossings;
                }
            } else if (y1 < y0) {
                if (y1 <= rymin) {
                    --crossings;
                }
                if (y0 >= rymax) {
                    --crossings;
                }
            }
            return crossings;
        }
        if (x0 > rxmin && x0 < rxmax && y0 > rymin && y0 < rymax || x1 > rxmin && x1 < rxmax && y1 > rymin && y1 < rymax) {
            return Integer.MIN_VALUE;
        }
        double xi0 = x0;
        if (y0 < rymin) {
            xi0 += (rymin - y0) * (x1 - x0) / (y1 - y0);
        } else if (y0 > rymax) {
            xi0 += (rymax - y0) * (x1 - x0) / (y1 - y0);
        }
        double xi1 = x1;
        if (y1 < rymin) {
            xi1 += (rymin - y1) * (x0 - x1) / (y0 - y1);
        } else if (y1 > rymax) {
            xi1 += (rymax - y1) * (x0 - x1) / (y0 - y1);
        }
        if (xi0 <= rxmin && xi1 <= rxmin) {
            return crossings;
        }
        if (xi0 >= rxmax && xi1 >= rxmax) {
            if (y0 < y1) {
                if (y0 <= rymin) {
                    ++crossings;
                }
                if (y1 >= rymax) {
                    ++crossings;
                }
            } else if (y1 < y0) {
                if (y1 <= rymin) {
                    --crossings;
                }
                if (y0 >= rymax) {
                    --crossings;
                }
            }
            return crossings;
        }
        return Integer.MIN_VALUE;
    }

    public static int rectCrossingsForQuad(int crossings, double rxmin, double rymin, double rxmax, double rymax, double x0, double y0, double xc, double yc, double x1, double y1, int level) {
        if (y0 >= rymax && yc >= rymax && y1 >= rymax) {
            return crossings;
        }
        if (y0 <= rymin && yc <= rymin && y1 <= rymin) {
            return crossings;
        }
        if (x0 <= rxmin && xc <= rxmin && x1 <= rxmin) {
            return crossings;
        }
        if (x0 >= rxmax && xc >= rxmax && x1 >= rxmax) {
            if (y0 < y1) {
                if (y0 <= rymin && y1 > rymin) {
                    ++crossings;
                }
                if (y0 < rymax && y1 >= rymax) {
                    ++crossings;
                }
            } else if (y1 < y0) {
                if (y1 <= rymin && y0 > rymin) {
                    --crossings;
                }
                if (y1 < rymax && y0 >= rymax) {
                    --crossings;
                }
            }
            return crossings;
        }
        if (x0 < rxmax && x0 > rxmin && y0 < rymax && y0 > rymin || x1 < rxmax && x1 > rxmin && y1 < rymax && y1 > rymin) {
            return Integer.MIN_VALUE;
        }
        if (level > 52) {
            return Curve.rectCrossingsForLine(crossings, rxmin, rymin, rxmax, rymax, x0, y0, x1, y1);
        }
        double x0c = (x0 + xc) / 2.0;
        double y0c = (y0 + yc) / 2.0;
        double xc1 = (xc + x1) / 2.0;
        double yc1 = (yc + y1) / 2.0;
        xc = (x0c + xc1) / 2.0;
        yc = (y0c + yc1) / 2.0;
        if (Double.isNaN(xc) || Double.isNaN(yc)) {
            return 0;
        }
        if ((crossings = Curve.rectCrossingsForQuad(crossings, rxmin, rymin, rxmax, rymax, x0, y0, x0c, y0c, xc, yc, level + 1)) != Integer.MIN_VALUE) {
            crossings = Curve.rectCrossingsForQuad(crossings, rxmin, rymin, rxmax, rymax, xc, yc, xc1, yc1, x1, y1, level + 1);
        }
        return crossings;
    }

    public static int rectCrossingsForCubic(int crossings, double rxmin, double rymin, double rxmax, double rymax, double x0, double y0, double xc0, double yc0, double xc1, double yc1, double x1, double y1, int level) {
        if (y0 >= rymax && yc0 >= rymax && yc1 >= rymax && y1 >= rymax) {
            return crossings;
        }
        if (y0 <= rymin && yc0 <= rymin && yc1 <= rymin && y1 <= rymin) {
            return crossings;
        }
        if (x0 <= rxmin && xc0 <= rxmin && xc1 <= rxmin && x1 <= rxmin) {
            return crossings;
        }
        if (x0 >= rxmax && xc0 >= rxmax && xc1 >= rxmax && x1 >= rxmax) {
            if (y0 < y1) {
                if (y0 <= rymin && y1 > rymin) {
                    ++crossings;
                }
                if (y0 < rymax && y1 >= rymax) {
                    ++crossings;
                }
            } else if (y1 < y0) {
                if (y1 <= rymin && y0 > rymin) {
                    --crossings;
                }
                if (y1 < rymax && y0 >= rymax) {
                    --crossings;
                }
            }
            return crossings;
        }
        if (x0 > rxmin && x0 < rxmax && y0 > rymin && y0 < rymax || x1 > rxmin && x1 < rxmax && y1 > rymin && y1 < rymax) {
            return Integer.MIN_VALUE;
        }
        if (level > 52) {
            return Curve.rectCrossingsForLine(crossings, rxmin, rymin, rxmax, rymax, x0, y0, x1, y1);
        }
        double xmid = (xc0 + xc1) / 2.0;
        double ymid = (yc0 + yc1) / 2.0;
        xc0 = (x0 + xc0) / 2.0;
        yc0 = (y0 + yc0) / 2.0;
        xc1 = (xc1 + x1) / 2.0;
        yc1 = (yc1 + y1) / 2.0;
        double xc0m = (xc0 + xmid) / 2.0;
        double yc0m = (yc0 + ymid) / 2.0;
        double xmc1 = (xmid + xc1) / 2.0;
        double ymc1 = (ymid + yc1) / 2.0;
        xmid = (xc0m + xmc1) / 2.0;
        ymid = (yc0m + ymc1) / 2.0;
        if (Double.isNaN(xmid) || Double.isNaN(ymid)) {
            return 0;
        }
        if ((crossings = Curve.rectCrossingsForCubic(crossings, rxmin, rymin, rxmax, rymax, x0, y0, xc0, yc0, xc0m, yc0m, xmid, ymid, level + 1)) != Integer.MIN_VALUE) {
            crossings = Curve.rectCrossingsForCubic(crossings, rxmin, rymin, rxmax, rymax, xmid, ymid, xmc1, ymc1, xc1, yc1, x1, y1, level + 1);
        }
        return crossings;
    }

    public static void accumulateExtremaBoundsForQuad(double[] bounds, int boundsOffset, double x1, double ctrlX, double x2, double[] coeff, double[] deriv_coeff) {
        if (ctrlX < bounds[boundsOffset] || ctrlX > bounds[boundsOffset + 1]) {
            double dx21 = ctrlX - x1;
            coeff[2] = x2 - ctrlX - dx21;
            coeff[1] = 2.0 * dx21;
            coeff[0] = x1;
            deriv_coeff[0] = coeff[1];
            deriv_coeff[1] = 2.0 * coeff[2];
            double t = -deriv_coeff[0] / deriv_coeff[1];
            if (t > 0.0 && t < 1.0) {
                double v = coeff[0] + t * (coeff[1] + t * coeff[2]);
                double margin = Math.ulp(Math.abs(coeff[0]) + Math.abs(coeff[1]) + Math.abs(coeff[2]));
                if (v - margin < bounds[boundsOffset]) {
                    bounds[boundsOffset] = v - margin;
                }
                if (v + margin > bounds[boundsOffset + 1]) {
                    bounds[boundsOffset + 1] = v + margin;
                }
            }
        }
    }

    public static void accumulateExtremaBoundsForCubic(double[] bounds, int boundsOffset, double x1, double ctrlX1, double ctrlX2, double x2, double[] coeff, double[] deriv_coeff) {
        if (ctrlX1 < bounds[boundsOffset] || ctrlX1 > bounds[boundsOffset + 1] || ctrlX2 < bounds[boundsOffset] || ctrlX2 > bounds[boundsOffset + 1]) {
            double dx32 = 3.0 * (ctrlX2 - ctrlX1);
            double dx21 = 3.0 * (ctrlX1 - x1);
            coeff[3] = x2 - x1 - dx32;
            coeff[2] = dx32 - dx21;
            coeff[1] = dx21;
            coeff[0] = x1;
            deriv_coeff[0] = coeff[1];
            deriv_coeff[1] = 2.0 * coeff[2];
            deriv_coeff[2] = 3.0 * coeff[3];
            double[] tExtrema = deriv_coeff;
            int tExtremaCount = QuadCurve2D.solveQuadratic(deriv_coeff, tExtrema);
            if (tExtremaCount > 0) {
                double margin = Math.ulp(Math.abs(coeff[0]) + Math.abs(coeff[1]) + Math.abs(coeff[2]) + Math.abs(coeff[3]));
                for (int i = 0; i < tExtremaCount; ++i) {
                    double t = tExtrema[i];
                    if (!(t > 0.0) || !(t < 1.0)) continue;
                    double v = coeff[0] + t * (coeff[1] + t * (coeff[2] + t * coeff[3]));
                    if (v - margin < bounds[boundsOffset]) {
                        bounds[boundsOffset] = v - margin;
                    }
                    if (!(v + margin > bounds[boundsOffset + 1])) continue;
                    bounds[boundsOffset + 1] = v + margin;
                }
            }
        }
    }

    public Curve(int direction) {
        this.direction = direction;
    }

    public final int getDirection() {
        return this.direction;
    }

    public final Curve getWithDirection(int direction) {
        return this.direction == direction ? this : this.getReversedCurve();
    }

    public static double round(double v) {
        return v;
    }

    public static int orderof(double x1, double x2) {
        if (x1 < x2) {
            return -1;
        }
        if (x1 > x2) {
            return 1;
        }
        return 0;
    }

    public static long signeddiffbits(double y1, double y2) {
        return Double.doubleToLongBits(y1) - Double.doubleToLongBits(y2);
    }

    public static long diffbits(double y1, double y2) {
        return Math.abs(Double.doubleToLongBits(y1) - Double.doubleToLongBits(y2));
    }

    public static double prev(double v) {
        return Double.longBitsToDouble(Double.doubleToLongBits(v) - 1L);
    }

    public static double next(double v) {
        return Double.longBitsToDouble(Double.doubleToLongBits(v) + 1L);
    }

    public String toString() {
        return "Curve[" + this.getOrder() + ", (" + Curve.round(this.getX0()) + ", " + Curve.round(this.getY0()) + "), " + this.controlPointString() + "(" + Curve.round(this.getX1()) + ", " + Curve.round(this.getY1()) + "), " + (this.direction == 1 ? "D" : "U") + "]";
    }

    public String controlPointString() {
        return "";
    }

    public abstract int getOrder();

    public abstract double getXTop();

    public abstract double getYTop();

    public abstract double getXBot();

    public abstract double getYBot();

    public abstract double getXMin();

    public abstract double getXMax();

    public abstract double getX0();

    public abstract double getY0();

    public abstract double getX1();

    public abstract double getY1();

    public abstract double XforY(double var1);

    public abstract double TforY(double var1);

    public abstract double XforT(double var1);

    public abstract double YforT(double var1);

    public abstract double dXforT(double var1, int var3);

    public abstract double dYforT(double var1, int var3);

    public abstract double nextVertical(double var1, double var3);

    public int crossingsFor(double x, double y) {
        if (y >= this.getYTop() && y < this.getYBot() && x < this.getXMax() && (x < this.getXMin() || x < this.XforY(y))) {
            return 1;
        }
        return 0;
    }

    public boolean accumulateCrossings(Crossings c) {
        double tend;
        double yend;
        double tstart;
        double ystart;
        double xhi = c.getXHi();
        if (this.getXMin() >= xhi) {
            return false;
        }
        double xlo = c.getXLo();
        double ylo = c.getYLo();
        double yhi = c.getYHi();
        double y0 = this.getYTop();
        double y1 = this.getYBot();
        if (y0 < ylo) {
            if (y1 <= ylo) {
                return false;
            }
            ystart = ylo;
            tstart = this.TforY(ylo);
        } else {
            if (y0 >= yhi) {
                return false;
            }
            ystart = y0;
            tstart = 0.0;
        }
        if (y1 > yhi) {
            yend = yhi;
            tend = this.TforY(yhi);
        } else {
            yend = y1;
            tend = 1.0;
        }
        boolean hitLo = false;
        boolean hitHi = false;
        while (true) {
            double x;
            if ((x = this.XforT(tstart)) < xhi) {
                if (hitHi || x > xlo) {
                    return true;
                }
                hitLo = true;
            } else {
                if (hitLo) {
                    return true;
                }
                hitHi = true;
            }
            if (tstart >= tend) break;
            tstart = this.nextVertical(tstart, tend);
        }
        if (hitLo) {
            c.record(ystart, yend, this.direction);
        }
        return false;
    }

    public abstract void enlarge(Rectangle2D var1);

    public Curve getSubCurve(double ystart, double yend) {
        return this.getSubCurve(ystart, yend, this.direction);
    }

    public abstract Curve getReversedCurve();

    public abstract Curve getSubCurve(double var1, double var3, int var5);

    public int compareTo(Curve that, double[] yrange) {
        double t1;
        double t0;
        double yt0;
        double s1;
        double y0 = yrange[0];
        double y1 = yrange[1];
        if ((y1 = Math.min(Math.min(y1, this.getYBot()), that.getYBot())) <= yrange[0]) {
            System.err.println("this == " + String.valueOf(this));
            System.err.println("that == " + String.valueOf(that));
            System.out.println("target range = " + yrange[0] + "=>" + yrange[1]);
            throw new InternalError("backstepping from " + yrange[0] + " to " + y1);
        }
        yrange[1] = y1;
        if (this.getXMax() <= that.getXMin()) {
            if (this.getXMin() == that.getXMax()) {
                return 0;
            }
            return -1;
        }
        if (this.getXMin() >= that.getXMax()) {
            return 1;
        }
        double s0 = this.TforY(y0);
        double ys0 = this.YforT(s0);
        if (ys0 < y0) {
            s0 = this.refineTforY(s0, ys0, y0);
            ys0 = this.YforT(s0);
        }
        if (this.YforT(s1 = this.TforY(y1)) < y0) {
            s1 = this.refineTforY(s1, this.YforT(s1), y0);
        }
        if ((yt0 = that.YforT(t0 = that.TforY(y0))) < y0) {
            t0 = that.refineTforY(t0, yt0, y0);
            yt0 = that.YforT(t0);
        }
        if (that.YforT(t1 = that.TforY(y1)) < y0) {
            t1 = that.refineTforY(t1, that.YforT(t1), y0);
        }
        double xs0 = this.XforT(s0);
        double xt0 = that.XforT(t0);
        double scale = Math.max(Math.abs(y0), Math.abs(y1));
        double ymin = Math.max(scale * 1.0E-14, 1.0E-300);
        if (this.fairlyClose(xs0, xt0)) {
            double y;
            double bump = ymin;
            double maxbump = Math.min(ymin * 1.0E13, (y1 - y0) * 0.1);
            for (y = y0 + bump; y <= y1; y += bump) {
                double newy;
                if (this.fairlyClose(this.XforY(y), that.XforY(y))) {
                    double d;
                    bump *= 2.0;
                    if (!(d > maxbump)) continue;
                    bump = maxbump;
                    continue;
                }
                y -= bump;
                while (!((newy = y + (bump /= 2.0)) <= y)) {
                    if (!this.fairlyClose(this.XforY(newy), that.XforY(newy))) continue;
                    y = newy;
                }
                break;
            }
            if (y > y0) {
                if (y < y1) {
                    yrange[1] = y;
                }
                return 0;
            }
        }
        if (ymin <= 0.0) {
            System.out.println("ymin = " + ymin);
        }
        while (s0 < s1 && t0 < t1) {
            double sh = this.nextVertical(s0, s1);
            double xsh = this.XforT(sh);
            double ysh = this.YforT(sh);
            double th = that.nextVertical(t0, t1);
            double xth = that.XforT(th);
            double yth = that.YforT(th);
            try {
                if (this.findIntersect(that, yrange, ymin, 0, 0, s0, xs0, ys0, sh, xsh, ysh, t0, xt0, yt0, th, xth, yth)) {
                    break;
                }
            }
            catch (Throwable t) {
                System.err.println("Error: " + String.valueOf(t));
                System.err.println("y range was " + yrange[0] + "=>" + yrange[1]);
                System.err.println("s y range is " + ys0 + "=>" + ysh);
                System.err.println("t y range is " + yt0 + "=>" + yth);
                System.err.println("ymin is " + ymin);
                return 0;
            }
            if (ysh < yth) {
                if (ysh > yrange[0]) {
                    if (!(ysh < yrange[1])) break;
                    yrange[1] = ysh;
                    break;
                }
                s0 = sh;
                xs0 = xsh;
                ys0 = ysh;
                continue;
            }
            if (yth > yrange[0]) {
                if (!(yth < yrange[1])) break;
                yrange[1] = yth;
                break;
            }
            t0 = th;
            xt0 = xth;
            yt0 = yth;
        }
        double ymid = (yrange[0] + yrange[1]) / 2.0;
        return Curve.orderof(this.XforY(ymid), that.XforY(ymid));
    }

    public boolean findIntersect(Curve that, double[] yrange, double ymin, int slevel, int tlevel, double s0, double xs0, double ys0, double s1, double xs1, double ys1, double t0, double xt0, double yt0, double t1, double xt1, double yt1) {
        if (ys0 > yt1 || yt0 > ys1) {
            return false;
        }
        if (Math.min(xs0, xs1) > Math.max(xt0, xt1) || Math.max(xs0, xs1) < Math.min(xt0, xt1)) {
            return false;
        }
        if (s1 - s0 > 0.001) {
            double s = (s0 + s1) / 2.0;
            double xs = this.XforT(s);
            double ys = this.YforT(s);
            if (s == s0 || s == s1) {
                System.out.println("s0 = " + s0);
                System.out.println("s1 = " + s1);
                throw new InternalError("no s progress!");
            }
            if (t1 - t0 > 0.001) {
                double t = (t0 + t1) / 2.0;
                double xt = that.XforT(t);
                double yt = that.YforT(t);
                if (t == t0 || t == t1) {
                    System.out.println("t0 = " + t0);
                    System.out.println("t1 = " + t1);
                    throw new InternalError("no t progress!");
                }
                if (ys >= yt0 && yt >= ys0 && this.findIntersect(that, yrange, ymin, slevel + 1, tlevel + 1, s0, xs0, ys0, s, xs, ys, t0, xt0, yt0, t, xt, yt)) {
                    return true;
                }
                if (ys >= yt && this.findIntersect(that, yrange, ymin, slevel + 1, tlevel + 1, s0, xs0, ys0, s, xs, ys, t, xt, yt, t1, xt1, yt1)) {
                    return true;
                }
                if (yt >= ys && this.findIntersect(that, yrange, ymin, slevel + 1, tlevel + 1, s, xs, ys, s1, xs1, ys1, t0, xt0, yt0, t, xt, yt)) {
                    return true;
                }
                if (ys1 >= yt && yt1 >= ys && this.findIntersect(that, yrange, ymin, slevel + 1, tlevel + 1, s, xs, ys, s1, xs1, ys1, t, xt, yt, t1, xt1, yt1)) {
                    return true;
                }
            } else {
                if (ys >= yt0 && this.findIntersect(that, yrange, ymin, slevel + 1, tlevel, s0, xs0, ys0, s, xs, ys, t0, xt0, yt0, t1, xt1, yt1)) {
                    return true;
                }
                if (yt1 >= ys && this.findIntersect(that, yrange, ymin, slevel + 1, tlevel, s, xs, ys, s1, xs1, ys1, t0, xt0, yt0, t1, xt1, yt1)) {
                    return true;
                }
            }
        } else if (t1 - t0 > 0.001) {
            double t = (t0 + t1) / 2.0;
            double xt = that.XforT(t);
            double yt = that.YforT(t);
            if (t == t0 || t == t1) {
                System.out.println("t0 = " + t0);
                System.out.println("t1 = " + t1);
                throw new InternalError("no t progress!");
            }
            if (yt >= ys0 && this.findIntersect(that, yrange, ymin, slevel, tlevel + 1, s0, xs0, ys0, s1, xs1, ys1, t0, xt0, yt0, t, xt, yt)) {
                return true;
            }
            if (ys1 >= yt && this.findIntersect(that, yrange, ymin, slevel, tlevel + 1, s0, xs0, ys0, s1, xs1, ys1, t, xt, yt, t1, xt1, yt1)) {
                return true;
            }
        } else {
            double xlk = xs1 - xs0;
            double ylk = ys1 - ys0;
            double xnm = xt1 - xt0;
            double ynm = yt1 - yt0;
            double xmk = xt0 - xs0;
            double ymk = yt0 - ys0;
            double det = xnm * ylk - ynm * xlk;
            if (det != 0.0) {
                double detinv = 1.0 / det;
                double s = (xnm * ymk - ynm * xmk) * detinv;
                double t = (xlk * ymk - ylk * xmk) * detinv;
                if (s >= 0.0 && s <= 1.0 && t >= 0.0 && t <= 1.0) {
                    double y;
                    s = s0 + s * (s1 - s0);
                    t = t0 + t * (t1 - t0);
                    if (s < 0.0 || s > 1.0 || t < 0.0 || t > 1.0) {
                        System.out.println("Uh oh!");
                    }
                    if ((y = (this.YforT(s) + that.YforT(t)) / 2.0) <= yrange[1] && y > yrange[0]) {
                        yrange[1] = y;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public double refineTforY(double t0, double yt0, double y0) {
        double t1 = 1.0;
        while (true) {
            double th;
            if ((th = (t0 + t1) / 2.0) == t0 || th == t1) {
                return t1;
            }
            double y = this.YforT(th);
            if (y < y0) {
                t0 = th;
                yt0 = y;
                continue;
            }
            if (!(y > y0)) break;
            t1 = th;
        }
        return t1;
    }

    public boolean fairlyClose(double v1, double v2) {
        return Math.abs(v1 - v2) < Math.max(Math.abs(v1), Math.abs(v2)) * 1.0E-10;
    }

    public abstract int getSegment(double[] var1);
}

