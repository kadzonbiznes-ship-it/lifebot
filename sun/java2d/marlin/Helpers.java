/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import java.util.Arrays;
import sun.java2d.marlin.ArrayCacheByte;
import sun.java2d.marlin.ArrayCacheDouble;
import sun.java2d.marlin.ArrayCacheInt;
import sun.java2d.marlin.Curve;
import sun.java2d.marlin.DPathConsumer2D;
import sun.java2d.marlin.MarlinConst;
import sun.java2d.marlin.RendererContext;
import sun.java2d.marlin.stats.Histogram;
import sun.java2d.marlin.stats.StatLong;

final class Helpers
implements MarlinConst {
    private static final double EPS = 1.0E-9;

    private Helpers() {
        throw new Error("This is a non instantiable class");
    }

    static boolean within(double x, double y) {
        return Helpers.within(x, y, 1.0E-9);
    }

    static boolean within(double x, double y, double err) {
        return Helpers.withinD(y - x, err);
    }

    static boolean withinD(double d, double err) {
        return d <= err && d >= -err;
    }

    static boolean withinD(double dx, double dy, double err) {
        assert (err > 0.0) : "";
        return Helpers.withinD(dx, err) && Helpers.withinD(dy, err);
    }

    static boolean isPointCurve(double[] curve, int type) {
        return Helpers.isPointCurve(curve, type, 1.0E-9);
    }

    static boolean isPointCurve(double[] curve, int type, double err) {
        for (int i = 2; i < type; ++i) {
            if (Helpers.within(curve[i], curve[i - 2], err)) continue;
            return false;
        }
        return true;
    }

    static double evalCubic(double a, double b, double c, double d, double t) {
        return t * (t * (t * a + b) + c) + d;
    }

    static double evalQuad(double a, double b, double c, double t) {
        return t * (t * a + b) + c;
    }

    static int quadraticRoots(double a, double b, double c, double[] zeroes, int off) {
        int ret = off;
        if (a != 0.0) {
            double d = b * b - 4.0 * a * c;
            if (d > 0.0) {
                d = Math.sqrt(d);
                if (b < 0.0) {
                    d = -d;
                }
                double q = (b + d) / -2.0;
                zeroes[ret++] = q / a;
                if (q != 0.0) {
                    zeroes[ret++] = c / q;
                }
            } else if (d == 0.0) {
                zeroes[ret++] = -b / (2.0 * a);
            }
        } else if (b != 0.0) {
            zeroes[ret++] = -c / b;
        }
        return ret - off;
    }

    static int cubicRootsInAB(double d, double a, double b, double c, double[] pts, int off, double A, double B) {
        int num;
        double p;
        double cb_p;
        double sub;
        double sq_A;
        double q;
        double D;
        if (d == 0.0) {
            int num2 = Helpers.quadraticRoots(a, b, c, pts, off);
            return Helpers.filterOutNotInAB(pts, off, num2, A, B) - off;
        }
        if (Helpers.within(D = (q = 0.5 * (0.07407407407407407 * (a /= d) * (sq_A = a * a) - (sub = 0.3333333333333333 * a) * (b /= d) + (c /= d))) * q + (cb_p = (p = 0.3333333333333333 * (-0.3333333333333333 * sq_A + b)) * p * p), 0.0)) {
            if (Helpers.within(q, 0.0)) {
                pts[off] = -sub;
                num = 1;
            } else {
                double u = Math.cbrt(-q);
                pts[off] = 2.0 * u - sub;
                pts[off + 1] = -u - sub;
                num = 2;
            }
        } else if (D < 0.0) {
            double phi = 0.3333333333333333 * Math.acos(-q / Math.sqrt(-cb_p));
            double t = 2.0 * Math.sqrt(-p);
            pts[off] = t * Math.cos(phi) - sub;
            pts[off + 1] = -t * Math.cos(phi + 1.0471975511965976) - sub;
            pts[off + 2] = -t * Math.cos(phi - 1.0471975511965976) - sub;
            num = 3;
        } else {
            double sqrt_D = Math.sqrt(D);
            double u = Math.cbrt(sqrt_D - q);
            double v = -Math.cbrt(sqrt_D + q);
            pts[off] = u + v - sub;
            num = 1;
        }
        return Helpers.filterOutNotInAB(pts, off, num, A, B) - off;
    }

    static int filterOutNotInAB(double[] nums, int off, int len, double a, double b) {
        int ret = off;
        int end = off + len;
        for (int i = off; i < end; ++i) {
            if (!(nums[i] >= a) || !(nums[i] < b)) continue;
            nums[ret++] = nums[i];
        }
        return ret;
    }

    static double fastLineLen(double x0, double y0, double x1, double y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        return Math.abs(dx) + Math.abs(dy);
    }

    static double linelen(double x0, double y0, double x1, double y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        return Math.sqrt(dx * dx + dy * dy);
    }

    static double fastQuadLen(double x0, double y0, double x1, double y1, double x2, double y2) {
        double dx1 = x1 - x0;
        double dx2 = x2 - x1;
        double dy1 = y1 - y0;
        double dy2 = y2 - y1;
        return Math.abs(dx1) + Math.abs(dx2) + Math.abs(dy1) + Math.abs(dy2);
    }

    static double quadlen(double x0, double y0, double x1, double y1, double x2, double y2) {
        return (Helpers.linelen(x0, y0, x1, y1) + Helpers.linelen(x1, y1, x2, y2) + Helpers.linelen(x0, y0, x2, y2)) / 2.0;
    }

    static double fastCurvelen(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3) {
        double dx1 = x1 - x0;
        double dx2 = x2 - x1;
        double dx3 = x3 - x2;
        double dy1 = y1 - y0;
        double dy2 = y2 - y1;
        double dy3 = y3 - y2;
        return Math.abs(dx1) + Math.abs(dx2) + Math.abs(dx3) + Math.abs(dy1) + Math.abs(dy2) + Math.abs(dy3);
    }

    static double curvelen(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3) {
        return (Helpers.linelen(x0, y0, x1, y1) + Helpers.linelen(x1, y1, x2, y2) + Helpers.linelen(x2, y2, x3, y3) + Helpers.linelen(x0, y0, x3, y3)) / 2.0;
    }

    static int findSubdivPoints(Curve c, double[] pts, double[] ts, int type, double w2) {
        double x12 = pts[2] - pts[0];
        double y12 = pts[3] - pts[1];
        if (y12 != 0.0 && x12 != 0.0) {
            double hypot = Math.sqrt(x12 * x12 + y12 * y12);
            double cos = x12 / hypot;
            double sin = y12 / hypot;
            double x1 = cos * pts[0] + sin * pts[1];
            double y1 = cos * pts[1] - sin * pts[0];
            double x2 = cos * pts[2] + sin * pts[3];
            double y2 = cos * pts[3] - sin * pts[2];
            double x3 = cos * pts[4] + sin * pts[5];
            double y3 = cos * pts[5] - sin * pts[4];
            switch (type) {
                case 8: {
                    double x4 = cos * pts[6] + sin * pts[7];
                    double y4 = cos * pts[7] - sin * pts[6];
                    c.set(x1, y1, x2, y2, x3, y3, x4, y4);
                    break;
                }
                case 6: {
                    c.set(x1, y1, x2, y2, x3, y3);
                    break;
                }
            }
        } else {
            c.set(pts, type);
        }
        int ret = 0;
        ret += c.dxRoots(ts, ret);
        ret += c.dyRoots(ts, ret);
        if (type == 8) {
            ret += c.infPoints(ts, ret);
        }
        ret += c.rootsOfROCMinusW(ts, ret, w2, 1.0E-4);
        ret = Helpers.filterOutNotInAB(ts, 0, ret, 1.0E-4, 0.9999);
        Helpers.isort(ts, ret);
        return ret;
    }

    static int findClipPoints(Curve curve, double[] pts, double[] ts, int type, int outCodeOR, double[] clipRect) {
        curve.set(pts, type);
        int ret = 0;
        if ((outCodeOR & 4) != 0) {
            ret += curve.xPoints(ts, ret, clipRect[2]);
        }
        if ((outCodeOR & 8) != 0) {
            ret += curve.xPoints(ts, ret, clipRect[3]);
        }
        if ((outCodeOR & 1) != 0) {
            ret += curve.yPoints(ts, ret, clipRect[0]);
        }
        if ((outCodeOR & 2) != 0) {
            ret += curve.yPoints(ts, ret, clipRect[1]);
        }
        Helpers.isort(ts, ret);
        return ret;
    }

    static void subdivide(double[] src, double[] left, double[] right, int type) {
        switch (type) {
            case 8: {
                Helpers.subdivideCubic(src, left, right);
                return;
            }
            case 6: {
                Helpers.subdivideQuad(src, left, right);
                return;
            }
        }
        throw new InternalError("Unsupported curve type");
    }

    static void isort(double[] a, int len) {
        for (int i = 1; i < len; ++i) {
            double ai = a[i];
            for (int j = i - 1; j >= 0 && a[j] > ai; --j) {
                a[j + 1] = a[j];
            }
            a[j + 1] = ai;
        }
    }

    static void subdivideCubic(double[] src, double[] left, double[] right) {
        double x1 = src[0];
        double y1 = src[1];
        double cx1 = src[2];
        double cy1 = src[3];
        double cx2 = src[4];
        double cy2 = src[5];
        double x2 = src[6];
        double y2 = src[7];
        left[0] = x1;
        left[1] = y1;
        right[6] = x2;
        right[7] = y2;
        x1 = (x1 + cx1) / 2.0;
        y1 = (y1 + cy1) / 2.0;
        x2 = (x2 + cx2) / 2.0;
        y2 = (y2 + cy2) / 2.0;
        double cx = (cx1 + cx2) / 2.0;
        double cy = (cy1 + cy2) / 2.0;
        cx1 = (x1 + cx) / 2.0;
        cy1 = (y1 + cy) / 2.0;
        cx2 = (x2 + cx) / 2.0;
        cy2 = (y2 + cy) / 2.0;
        cx = (cx1 + cx2) / 2.0;
        cy = (cy1 + cy2) / 2.0;
        left[2] = x1;
        left[3] = y1;
        left[4] = cx1;
        left[5] = cy1;
        left[6] = cx;
        left[7] = cy;
        right[0] = cx;
        right[1] = cy;
        right[2] = cx2;
        right[3] = cy2;
        right[4] = x2;
        right[5] = y2;
    }

    static void subdivideCubicAt(double t, double[] src, int offS, double[] pts, int offL, int offR) {
        double x1 = src[offS];
        double y1 = src[offS + 1];
        double cx1 = src[offS + 2];
        double cy1 = src[offS + 3];
        double cx2 = src[offS + 4];
        double cy2 = src[offS + 5];
        double x2 = src[offS + 6];
        double y2 = src[offS + 7];
        pts[offL] = x1;
        pts[offL + 1] = y1;
        pts[offR + 6] = x2;
        pts[offR + 7] = y2;
        x1 += t * (cx1 - x1);
        y1 += t * (cy1 - y1);
        x2 = cx2 + t * (x2 - cx2);
        y2 = cy2 + t * (y2 - cy2);
        double cx = cx1 + t * (cx2 - cx1);
        double cy = cy1 + t * (cy2 - cy1);
        cx1 = x1 + t * (cx - x1);
        cy1 = y1 + t * (cy - y1);
        cx2 = cx + t * (x2 - cx);
        cy2 = cy + t * (y2 - cy);
        cx = cx1 + t * (cx2 - cx1);
        cy = cy1 + t * (cy2 - cy1);
        pts[offL + 2] = x1;
        pts[offL + 3] = y1;
        pts[offL + 4] = cx1;
        pts[offL + 5] = cy1;
        pts[offL + 6] = cx;
        pts[offL + 7] = cy;
        pts[offR] = cx;
        pts[offR + 1] = cy;
        pts[offR + 2] = cx2;
        pts[offR + 3] = cy2;
        pts[offR + 4] = x2;
        pts[offR + 5] = y2;
    }

    static void subdivideQuad(double[] src, double[] left, double[] right) {
        double x1 = src[0];
        double y1 = src[1];
        double cx = src[2];
        double cy = src[3];
        double x2 = src[4];
        double y2 = src[5];
        left[0] = x1;
        left[1] = y1;
        right[4] = x2;
        right[5] = y2;
        x1 = (x1 + cx) / 2.0;
        y1 = (y1 + cy) / 2.0;
        x2 = (x2 + cx) / 2.0;
        y2 = (y2 + cy) / 2.0;
        cx = (x1 + x2) / 2.0;
        cy = (y1 + y2) / 2.0;
        left[2] = x1;
        left[3] = y1;
        left[4] = cx;
        left[5] = cy;
        right[0] = cx;
        right[1] = cy;
        right[2] = x2;
        right[3] = y2;
    }

    static void subdivideQuadAt(double t, double[] src, int offS, double[] pts, int offL, int offR) {
        double x1 = src[offS];
        double y1 = src[offS + 1];
        double cx = src[offS + 2];
        double cy = src[offS + 3];
        double x2 = src[offS + 4];
        double y2 = src[offS + 5];
        pts[offL] = x1;
        pts[offL + 1] = y1;
        pts[offR + 4] = x2;
        pts[offR + 5] = y2;
        x1 += t * (cx - x1);
        y1 += t * (cy - y1);
        x2 = cx + t * (x2 - cx);
        y2 = cy + t * (y2 - cy);
        cx = x1 + t * (x2 - x1);
        cy = y1 + t * (y2 - y1);
        pts[offL + 2] = x1;
        pts[offL + 3] = y1;
        pts[offL + 4] = cx;
        pts[offL + 5] = cy;
        pts[offR] = cx;
        pts[offR + 1] = cy;
        pts[offR + 2] = x2;
        pts[offR + 3] = y2;
    }

    static void subdivideLineAt(double t, double[] src, int offS, double[] pts, int offL, int offR) {
        double x1 = src[offS];
        double y1 = src[offS + 1];
        double x2 = src[offS + 2];
        double y2 = src[offS + 3];
        pts[offL] = x1;
        pts[offL + 1] = y1;
        pts[offR + 2] = x2;
        pts[offR + 3] = y2;
        x1 += t * (x2 - x1);
        y1 += t * (y2 - y1);
        pts[offL + 2] = x1;
        pts[offL + 3] = y1;
        pts[offR] = x1;
        pts[offR + 1] = y1;
    }

    static void subdivideAt(double t, double[] src, int offS, double[] pts, int offL, int type) {
        if (type == 8) {
            Helpers.subdivideCubicAt(t, src, offS, pts, offL, offL + type);
        } else if (type == 4) {
            Helpers.subdivideLineAt(t, src, offS, pts, offL, offL + type);
        } else {
            Helpers.subdivideQuadAt(t, src, offS, pts, offL, offL + type);
        }
    }

    static int outcode(double x, double y, double[] clipRect) {
        int code = y < clipRect[0] ? 1 : (y >= clipRect[1] ? 2 : 0);
        if (x < clipRect[2]) {
            code |= 4;
        } else if (x >= clipRect[3]) {
            code |= 8;
        }
        return code;
    }

    static final class IndexStack {
        private static final int INITIAL_COUNT = MarlinConst.INITIAL_EDGES_COUNT >> 2;
        private int end;
        private int[] indices;
        private final ArrayCacheInt.Reference indices_ref;
        private int indicesUseMark;
        private final StatLong stat_idxstack_indices;
        private final Histogram hist_idxstack_indices;
        private final StatLong stat_array_idxstack_indices;

        IndexStack(RendererContext rdrCtx) {
            this(rdrCtx, null, null, null);
        }

        IndexStack(RendererContext rdrCtx, StatLong stat_idxstack_indices, Histogram hist_idxstack_indices, StatLong stat_array_idxstack_indices) {
            this.indices_ref = rdrCtx.newDirtyIntArrayRef(INITIAL_COUNT);
            this.indices = this.indices_ref.initial;
            this.end = 0;
            if (MarlinConst.DO_STATS) {
                this.indicesUseMark = 0;
            }
            this.stat_idxstack_indices = stat_idxstack_indices;
            this.hist_idxstack_indices = hist_idxstack_indices;
            this.stat_array_idxstack_indices = stat_array_idxstack_indices;
        }

        void dispose() {
            this.end = 0;
            if (MarlinConst.DO_STATS) {
                this.stat_idxstack_indices.add(this.indicesUseMark);
                this.hist_idxstack_indices.add(this.indicesUseMark);
                this.indicesUseMark = 0;
            }
            if (this.indices_ref.doCleanRef(this.indices)) {
                this.indices = this.indices_ref.putArray(this.indices);
            }
        }

        boolean isEmpty() {
            return this.end == 0;
        }

        void reset() {
            this.end = 0;
        }

        void push(int v) {
            int nc;
            int[] _values = this.indices;
            if ((nc = this.end--) != 0 && _values[nc - 1] == v) {
                return;
            }
            if (_values.length <= nc) {
                if (MarlinConst.DO_STATS) {
                    this.stat_array_idxstack_indices.add(nc + 1);
                }
                this.indices = _values = this.indices_ref.widenArray(_values, nc, nc + 1);
            }
            _values[this.end++] = v;
            if (MarlinConst.DO_STATS && this.end > this.indicesUseMark) {
                this.indicesUseMark = this.end;
            }
        }

        void pullAll(double[] points, DPathConsumer2D io, boolean moveFirst) {
            int j;
            int nc = this.end;
            if (nc == 0) {
                return;
            }
            int[] _values = this.indices;
            int i = 0;
            if (moveFirst) {
                j = _values[i] << 1;
                io.moveTo(points[j], points[j + 1]);
                ++i;
            }
            while (i < nc) {
                j = _values[i] << 1;
                io.lineTo(points[j], points[j + 1]);
                ++i;
            }
            this.end = 0;
        }
    }

    static final class PolyStack {
        private static final byte TYPE_LINETO = 0;
        private static final byte TYPE_QUADTO = 1;
        private static final byte TYPE_CUBICTO = 2;
        private static final int INITIAL_CURVES_COUNT = MarlinConst.INITIAL_EDGES_COUNT << 1;
        private static final int INITIAL_TYPES_COUNT = MarlinConst.INITIAL_EDGES_COUNT;
        double[] curves;
        int end;
        byte[] curveTypes;
        int numCurves;
        final ArrayCacheDouble.Reference curves_ref;
        final ArrayCacheByte.Reference curveTypes_ref;
        int curveTypesUseMark;
        int curvesUseMark;
        private final StatLong stat_polystack_types;
        private final StatLong stat_polystack_curves;
        private final Histogram hist_polystack_curves;
        private final StatLong stat_array_polystack_curves;
        private final StatLong stat_array_polystack_curveTypes;

        PolyStack(RendererContext rdrCtx) {
            this(rdrCtx, null, null, null, null, null);
        }

        PolyStack(RendererContext rdrCtx, StatLong stat_polystack_types, StatLong stat_polystack_curves, Histogram hist_polystack_curves, StatLong stat_array_polystack_curves, StatLong stat_array_polystack_curveTypes) {
            this.curves_ref = rdrCtx.newDirtyDoubleArrayRef(INITIAL_CURVES_COUNT);
            this.curves = this.curves_ref.initial;
            this.curveTypes_ref = rdrCtx.newDirtyByteArrayRef(INITIAL_TYPES_COUNT);
            this.curveTypes = this.curveTypes_ref.initial;
            this.numCurves = 0;
            this.end = 0;
            if (MarlinConst.DO_STATS) {
                this.curveTypesUseMark = 0;
                this.curvesUseMark = 0;
            }
            this.stat_polystack_types = stat_polystack_types;
            this.stat_polystack_curves = stat_polystack_curves;
            this.hist_polystack_curves = hist_polystack_curves;
            this.stat_array_polystack_curves = stat_array_polystack_curves;
            this.stat_array_polystack_curveTypes = stat_array_polystack_curveTypes;
        }

        void dispose() {
            this.end = 0;
            this.numCurves = 0;
            if (MarlinConst.DO_STATS) {
                this.stat_polystack_types.add(this.curveTypesUseMark);
                this.stat_polystack_curves.add(this.curvesUseMark);
                this.hist_polystack_curves.add(this.curvesUseMark);
                this.curveTypesUseMark = 0;
                this.curvesUseMark = 0;
            }
            if (this.curves_ref.doCleanRef(this.curves)) {
                this.curves = this.curves_ref.putArray(this.curves);
            }
            if (this.curveTypes_ref.doCleanRef(this.curveTypes)) {
                this.curveTypes = this.curveTypes_ref.putArray(this.curveTypes);
            }
        }

        private void ensureSpace(int n) {
            if (this.curves.length - this.end < n) {
                if (MarlinConst.DO_STATS) {
                    this.stat_array_polystack_curves.add(this.end + n);
                }
                this.curves = this.curves_ref.widenArray(this.curves, this.end, this.end + n);
            }
            if (this.curveTypes.length <= this.numCurves) {
                if (MarlinConst.DO_STATS) {
                    this.stat_array_polystack_curveTypes.add(this.numCurves + 1);
                }
                this.curveTypes = this.curveTypes_ref.widenArray(this.curveTypes, this.numCurves, this.numCurves + 1);
            }
        }

        void pushCubic(double x0, double y0, double x1, double y1, double x2, double y2) {
            this.ensureSpace(6);
            this.curveTypes[this.numCurves++] = 2;
            double[] _curves = this.curves;
            int e = this.end;
            _curves[e++] = x2;
            _curves[e++] = y2;
            _curves[e++] = x1;
            _curves[e++] = y1;
            _curves[e++] = x0;
            _curves[e++] = y0;
            this.end = e;
        }

        void pushQuad(double x0, double y0, double x1, double y1) {
            this.ensureSpace(4);
            this.curveTypes[this.numCurves++] = 1;
            double[] _curves = this.curves;
            int e = this.end;
            _curves[e++] = x1;
            _curves[e++] = y1;
            _curves[e++] = x0;
            _curves[e++] = y0;
            this.end = e;
        }

        void pushLine(double x, double y) {
            this.ensureSpace(2);
            this.curveTypes[this.numCurves++] = 0;
            this.curves[this.end++] = x;
            this.curves[this.end++] = y;
        }

        void pullAll(DPathConsumer2D io) {
            int nc = this.numCurves;
            if (nc == 0) {
                return;
            }
            if (MarlinConst.DO_STATS) {
                if (this.numCurves > this.curveTypesUseMark) {
                    this.curveTypesUseMark = this.numCurves;
                }
                if (this.end > this.curvesUseMark) {
                    this.curvesUseMark = this.end;
                }
            }
            byte[] _curveTypes = this.curveTypes;
            double[] _curves = this.curves;
            int e = 0;
            block5: for (int i = 0; i < nc; ++i) {
                switch (_curveTypes[i]) {
                    case 0: {
                        io.lineTo(_curves[e], _curves[e + 1]);
                        e += 2;
                        continue block5;
                    }
                    case 2: {
                        io.curveTo(_curves[e], _curves[e + 1], _curves[e + 2], _curves[e + 3], _curves[e + 4], _curves[e + 5]);
                        e += 6;
                        continue block5;
                    }
                    case 1: {
                        io.quadTo(_curves[e], _curves[e + 1], _curves[e + 2], _curves[e + 3]);
                        e += 4;
                        continue block5;
                    }
                }
            }
            this.numCurves = 0;
            this.end = 0;
        }

        void popAll(DPathConsumer2D io) {
            int nc = this.numCurves;
            if (nc == 0) {
                return;
            }
            if (MarlinConst.DO_STATS) {
                if (this.numCurves > this.curveTypesUseMark) {
                    this.curveTypesUseMark = this.numCurves;
                }
                if (this.end > this.curvesUseMark) {
                    this.curvesUseMark = this.end;
                }
            }
            byte[] _curveTypes = this.curveTypes;
            double[] _curves = this.curves;
            int e = this.end;
            block5: while (nc != 0) {
                switch (_curveTypes[--nc]) {
                    case 0: {
                        io.lineTo(_curves[e -= 2], _curves[e + 1]);
                        continue block5;
                    }
                    case 2: {
                        io.curveTo(_curves[e -= 6], _curves[e + 1], _curves[e + 2], _curves[e + 3], _curves[e + 4], _curves[e + 5]);
                        continue block5;
                    }
                    case 1: {
                        io.quadTo(_curves[e -= 4], _curves[e + 1], _curves[e + 2], _curves[e + 3]);
                        continue block5;
                    }
                }
            }
            this.numCurves = 0;
            this.end = 0;
        }

        public String toString() {
            StringBuilder ret = new StringBuilder();
            int nc = this.numCurves;
            int last = this.end;
            while (nc != 0) {
                int len;
                switch (this.curveTypes[--nc]) {
                    case 0: {
                        len = 2;
                        ret.append("line: ");
                        break;
                    }
                    case 1: {
                        len = 4;
                        ret.append("quad: ");
                        break;
                    }
                    case 2: {
                        len = 6;
                        ret.append("cubic: ");
                        break;
                    }
                    default: {
                        len = 0;
                    }
                }
                ret.append(Arrays.toString(Arrays.copyOfRange(this.curves, last -= len, last + len))).append("\n");
            }
            return ret.toString();
        }
    }
}

