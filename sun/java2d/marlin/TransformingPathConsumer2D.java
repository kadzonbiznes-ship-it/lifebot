/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.Arrays;
import sun.java2d.marlin.Curve;
import sun.java2d.marlin.DPathConsumer2D;
import sun.java2d.marlin.Helpers;
import sun.java2d.marlin.MarlinConst;
import sun.java2d.marlin.MarlinProperties;
import sun.java2d.marlin.MarlinUtils;
import sun.java2d.marlin.RendererContext;

final class TransformingPathConsumer2D {
    static final double CLIP_RECT_PADDING = 0.25;
    private final RendererContext rdrCtx;
    private final ClosedPathDetector cpDetector;
    private final PathClipFilter pathClipper;
    private final Path2DWrapper wp_Path2DWrapper = new Path2DWrapper();
    private final DeltaScaleFilter dt_DeltaScaleFilter = new DeltaScaleFilter();
    private final DeltaTransformFilter dt_DeltaTransformFilter = new DeltaTransformFilter();
    private final DeltaScaleFilter iv_DeltaScaleFilter = new DeltaScaleFilter();
    private final DeltaTransformFilter iv_DeltaTransformFilter = new DeltaTransformFilter();
    private final PathTracer tracerInput = new PathTracer("[Input]");
    private final PathTracer tracerCPDetector = new PathTracer("ClosedPathDetector");
    private final PathTracer tracerFiller = new PathTracer("Filler");
    private final PathTracer tracerStroker = new PathTracer("Stroker");
    private final PathTracer tracerDasher = new PathTracer("Dasher");

    TransformingPathConsumer2D(RendererContext rdrCtx) {
        this.rdrCtx = rdrCtx;
        this.cpDetector = new ClosedPathDetector(rdrCtx);
        this.pathClipper = new PathClipFilter(rdrCtx);
    }

    DPathConsumer2D wrapPath2D(Path2D.Double p2d) {
        return this.wp_Path2DWrapper.init(p2d);
    }

    DPathConsumer2D traceInput(DPathConsumer2D out) {
        return this.tracerInput.init(out);
    }

    DPathConsumer2D traceClosedPathDetector(DPathConsumer2D out) {
        return this.tracerCPDetector.init(out);
    }

    DPathConsumer2D traceFiller(DPathConsumer2D out) {
        return this.tracerFiller.init(out);
    }

    DPathConsumer2D traceStroker(DPathConsumer2D out) {
        return this.tracerStroker.init(out);
    }

    DPathConsumer2D traceDasher(DPathConsumer2D out) {
        return this.tracerDasher.init(out);
    }

    DPathConsumer2D detectClosedPath(DPathConsumer2D out) {
        return this.cpDetector.init(out);
    }

    DPathConsumer2D pathClipper(DPathConsumer2D out) {
        return this.pathClipper.init(out);
    }

    DPathConsumer2D deltaTransformConsumer(DPathConsumer2D out, AffineTransform at) {
        if (at == null) {
            return out;
        }
        double mxx = at.getScaleX();
        double mxy = at.getShearX();
        double myx = at.getShearY();
        double myy = at.getScaleY();
        if (mxy == 0.0 && myx == 0.0) {
            if (mxx == 1.0 && myy == 1.0) {
                return out;
            }
            if (this.rdrCtx.doClip) {
                this.rdrCtx.clipInvScale = TransformingPathConsumer2D.adjustClipScale(this.rdrCtx.clipRect, mxx, myy);
            }
            return this.dt_DeltaScaleFilter.init(out, mxx, myy);
        }
        if (this.rdrCtx.doClip) {
            this.rdrCtx.clipInvScale = TransformingPathConsumer2D.adjustClipInverseDelta(this.rdrCtx.clipRect, mxx, mxy, myx, myy);
        }
        return this.dt_DeltaTransformFilter.init(out, mxx, mxy, myx, myy);
    }

    private static double adjustClipScale(double[] clipRect, double mxx, double myy) {
        double scaleY = 1.0 / myy;
        clipRect[0] = clipRect[0] * scaleY;
        clipRect[1] = clipRect[1] * scaleY;
        if (clipRect[1] < clipRect[0]) {
            double tmp = clipRect[0];
            clipRect[0] = clipRect[1];
            clipRect[1] = tmp;
        }
        double scaleX = 1.0 / mxx;
        clipRect[2] = clipRect[2] * scaleX;
        clipRect[3] = clipRect[3] * scaleX;
        if (clipRect[3] < clipRect[2]) {
            double tmp = clipRect[2];
            clipRect[2] = clipRect[3];
            clipRect[3] = tmp;
        }
        if (MarlinConst.DO_LOG_CLIP) {
            MarlinUtils.logInfo("clipRect (ClipScale): " + Arrays.toString(clipRect));
        }
        return 0.5 * (Math.abs(scaleX) + Math.abs(scaleY));
    }

    private static double adjustClipInverseDelta(double[] clipRect, double mxx, double mxy, double myx, double myy) {
        double ymax;
        double xmax;
        double det = mxx * myy - mxy * myx;
        double imxx = myy / det;
        double imxy = -mxy / det;
        double imyx = -myx / det;
        double imyy = mxx / det;
        double x = clipRect[2] * imxx + clipRect[0] * imxy;
        double y = clipRect[2] * imyx + clipRect[0] * imyy;
        double xmin = xmax = x;
        double ymin = ymax = y;
        x = clipRect[3] * imxx + clipRect[0] * imxy;
        y = clipRect[3] * imyx + clipRect[0] * imyy;
        if (x < xmin) {
            xmin = x;
        } else if (x > xmax) {
            xmax = x;
        }
        if (y < ymin) {
            ymin = y;
        } else if (y > ymax) {
            ymax = y;
        }
        x = clipRect[2] * imxx + clipRect[1] * imxy;
        y = clipRect[2] * imyx + clipRect[1] * imyy;
        if (x < xmin) {
            xmin = x;
        } else if (x > xmax) {
            xmax = x;
        }
        if (y < ymin) {
            ymin = y;
        } else if (y > ymax) {
            ymax = y;
        }
        x = clipRect[3] * imxx + clipRect[1] * imxy;
        y = clipRect[3] * imyx + clipRect[1] * imyy;
        if (x < xmin) {
            xmin = x;
        } else if (x > xmax) {
            xmax = x;
        }
        if (y < ymin) {
            ymin = y;
        } else if (y > ymax) {
            ymax = y;
        }
        clipRect[0] = ymin;
        clipRect[1] = ymax;
        clipRect[2] = xmin;
        clipRect[3] = xmax;
        if (MarlinConst.DO_LOG_CLIP) {
            MarlinUtils.logInfo("clipRect (ClipInverseDelta): " + Arrays.toString(clipRect));
        }
        double scaleX = Math.sqrt(imxx * imxx + imxy * imxy);
        double scaleY = Math.sqrt(imyx * imyx + imyy * imyy);
        return 0.5 * (scaleX + scaleY);
    }

    DPathConsumer2D inverseDeltaTransformConsumer(DPathConsumer2D out, AffineTransform at) {
        if (at == null) {
            return out;
        }
        double mxx = at.getScaleX();
        double mxy = at.getShearX();
        double myx = at.getShearY();
        double myy = at.getScaleY();
        if (mxy == 0.0 && myx == 0.0) {
            if (mxx == 1.0 && myy == 1.0) {
                return out;
            }
            return this.iv_DeltaScaleFilter.init(out, 1.0 / mxx, 1.0 / myy);
        }
        double det = mxx * myy - mxy * myx;
        return this.iv_DeltaTransformFilter.init(out, myy / det, -mxy / det, -myx / det, mxx / det);
    }

    static final class Path2DWrapper
    implements DPathConsumer2D {
        private Path2D.Double p2d;

        Path2DWrapper() {
        }

        Path2DWrapper init(Path2D.Double p2d) {
            this.p2d = p2d;
            return this;
        }

        @Override
        public void moveTo(double x0, double y0) {
            this.p2d.moveTo(x0, y0);
        }

        @Override
        public void lineTo(double x1, double y1) {
            this.p2d.lineTo(x1, y1);
        }

        @Override
        public void closePath() {
            this.p2d.closePath();
        }

        @Override
        public void pathDone() {
        }

        @Override
        public void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
            this.p2d.curveTo(x1, y1, x2, y2, x3, y3);
        }

        @Override
        public void quadTo(double x1, double y1, double x2, double y2) {
            this.p2d.quadTo(x1, y1, x2, y2);
        }

        @Override
        public long getNativeConsumer() {
            throw new InternalError("Not using a native peer");
        }
    }

    static final class DeltaScaleFilter
    implements DPathConsumer2D {
        private DPathConsumer2D out;
        private double sx;
        private double sy;

        DeltaScaleFilter() {
        }

        DeltaScaleFilter init(DPathConsumer2D out, double mxx, double myy) {
            this.out = out;
            this.sx = mxx;
            this.sy = myy;
            return this;
        }

        @Override
        public void moveTo(double x0, double y0) {
            this.out.moveTo(x0 * this.sx, y0 * this.sy);
        }

        @Override
        public void lineTo(double x1, double y1) {
            this.out.lineTo(x1 * this.sx, y1 * this.sy);
        }

        @Override
        public void quadTo(double x1, double y1, double x2, double y2) {
            this.out.quadTo(x1 * this.sx, y1 * this.sy, x2 * this.sx, y2 * this.sy);
        }

        @Override
        public void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
            this.out.curveTo(x1 * this.sx, y1 * this.sy, x2 * this.sx, y2 * this.sy, x3 * this.sx, y3 * this.sy);
        }

        @Override
        public void closePath() {
            this.out.closePath();
        }

        @Override
        public void pathDone() {
            this.out.pathDone();
        }

        @Override
        public long getNativeConsumer() {
            return 0L;
        }
    }

    static final class DeltaTransformFilter
    implements DPathConsumer2D {
        private DPathConsumer2D out;
        private double mxx;
        private double mxy;
        private double myx;
        private double myy;

        DeltaTransformFilter() {
        }

        DeltaTransformFilter init(DPathConsumer2D out, double mxx, double mxy, double myx, double myy) {
            this.out = out;
            this.mxx = mxx;
            this.mxy = mxy;
            this.myx = myx;
            this.myy = myy;
            return this;
        }

        @Override
        public void moveTo(double x0, double y0) {
            this.out.moveTo(x0 * this.mxx + y0 * this.mxy, x0 * this.myx + y0 * this.myy);
        }

        @Override
        public void lineTo(double x1, double y1) {
            this.out.lineTo(x1 * this.mxx + y1 * this.mxy, x1 * this.myx + y1 * this.myy);
        }

        @Override
        public void quadTo(double x1, double y1, double x2, double y2) {
            this.out.quadTo(x1 * this.mxx + y1 * this.mxy, x1 * this.myx + y1 * this.myy, x2 * this.mxx + y2 * this.mxy, x2 * this.myx + y2 * this.myy);
        }

        @Override
        public void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
            this.out.curveTo(x1 * this.mxx + y1 * this.mxy, x1 * this.myx + y1 * this.myy, x2 * this.mxx + y2 * this.mxy, x2 * this.myx + y2 * this.myy, x3 * this.mxx + y3 * this.mxy, x3 * this.myx + y3 * this.myy);
        }

        @Override
        public void closePath() {
            this.out.closePath();
        }

        @Override
        public void pathDone() {
            this.out.pathDone();
        }

        @Override
        public long getNativeConsumer() {
            return 0L;
        }
    }

    static final class PathTracer
    implements DPathConsumer2D {
        private final String prefix;
        private DPathConsumer2D out;

        PathTracer(String name) {
            this.prefix = name + ": ";
        }

        PathTracer init(DPathConsumer2D out) {
            this.out = out;
            return this;
        }

        @Override
        public void moveTo(double x0, double y0) {
            this.log("p.moveTo(" + x0 + ", " + y0 + ");");
            this.out.moveTo(x0, y0);
        }

        @Override
        public void lineTo(double x1, double y1) {
            this.log("p.lineTo(" + x1 + ", " + y1 + ");");
            this.out.lineTo(x1, y1);
        }

        @Override
        public void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
            this.log("p.curveTo(" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ", " + x3 + ", " + y3 + ");");
            this.out.curveTo(x1, y1, x2, y2, x3, y3);
        }

        @Override
        public void quadTo(double x1, double y1, double x2, double y2) {
            this.log("p.quadTo(" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ");");
            this.out.quadTo(x1, y1, x2, y2);
        }

        @Override
        public void closePath() {
            this.log("p.closePath();");
            this.out.closePath();
        }

        @Override
        public void pathDone() {
            this.log("p.pathDone();");
            this.out.pathDone();
        }

        private void log(String message) {
            MarlinUtils.logInfo(this.prefix + message);
        }

        @Override
        public long getNativeConsumer() {
            throw new InternalError("Not using a native peer");
        }
    }

    static final class ClosedPathDetector
    implements DPathConsumer2D {
        private final RendererContext rdrCtx;
        private final Helpers.PolyStack stack;
        private DPathConsumer2D out;

        ClosedPathDetector(RendererContext rdrCtx) {
            this.rdrCtx = rdrCtx;
            this.stack = rdrCtx.stats != null ? new Helpers.PolyStack(rdrCtx, rdrCtx.stats.stat_cpd_polystack_types, rdrCtx.stats.stat_cpd_polystack_curves, rdrCtx.stats.hist_cpd_polystack_curves, rdrCtx.stats.stat_array_cpd_polystack_curves, rdrCtx.stats.stat_array_cpd_polystack_types) : new Helpers.PolyStack(rdrCtx);
        }

        ClosedPathDetector init(DPathConsumer2D out) {
            this.out = out;
            return this;
        }

        void dispose() {
            this.stack.dispose();
        }

        @Override
        public void pathDone() {
            this.finish(false);
            this.out.pathDone();
            this.dispose();
        }

        @Override
        public void closePath() {
            this.finish(true);
            this.out.closePath();
        }

        @Override
        public void moveTo(double x0, double y0) {
            this.finish(false);
            this.out.moveTo(x0, y0);
        }

        private void finish(boolean closed) {
            this.rdrCtx.closedPath = closed;
            this.stack.pullAll(this.out);
        }

        @Override
        public void lineTo(double x1, double y1) {
            this.stack.pushLine(x1, y1);
        }

        @Override
        public void curveTo(double x3, double y3, double x2, double y2, double x1, double y1) {
            this.stack.pushCubic(x1, y1, x2, y2, x3, y3);
        }

        @Override
        public void quadTo(double x2, double y2, double x1, double y1) {
            this.stack.pushQuad(x1, y1, x2, y2);
        }

        @Override
        public long getNativeConsumer() {
            throw new InternalError("Not using a native peer");
        }
    }

    static final class PathClipFilter
    implements StartFlagPathConsumer2D {
        private static final boolean TRACE = false;
        private static final int MOVE_TO = 0;
        private static final int DRAWING_OP_TO = 1;
        private static final int CLOSE = 2;
        private DPathConsumer2D out;
        private int prev;
        private final double[] clipRect;
        private final double[] corners = new double[8];
        private boolean init_corners = false;
        private final Helpers.IndexStack stack;
        private int cOutCode = 0;
        private int sOutCode = 0;
        private int gOutCode = 15;
        private boolean outside = false;
        private double sx0;
        private double sy0;
        private double cx0;
        private double cy0;
        private boolean subdivide = MarlinConst.DO_CLIP_SUBDIVIDER;
        private final CurveClipSplitter curveSplitter;

        PathClipFilter(RendererContext rdrCtx) {
            this.clipRect = rdrCtx.clipRect;
            this.curveSplitter = rdrCtx.curveClipSplitter;
            this.stack = rdrCtx.stats != null ? new Helpers.IndexStack(rdrCtx, rdrCtx.stats.stat_pcf_idxstack_indices, rdrCtx.stats.hist_pcf_idxstack_indices, rdrCtx.stats.stat_array_pcf_idxstack_indices) : new Helpers.IndexStack(rdrCtx);
        }

        PathClipFilter init(DPathConsumer2D out) {
            this.out = out;
            if (MarlinConst.DO_CLIP_SUBDIVIDER) {
                this.curveSplitter.init();
            }
            this.init_corners = true;
            this.gOutCode = 15;
            this.prev = 2;
            return this;
        }

        void dispose() {
            this.stack.dispose();
        }

        private void finishPath() {
            if (this.gOutCode == 0) {
                this.finish();
            } else {
                this.outside = false;
                this.stack.reset();
            }
        }

        private void finish() {
            this.outside = false;
            if (!this.stack.isEmpty()) {
                if (this.init_corners) {
                    this.init_corners = false;
                    double[] _corners = this.corners;
                    double[] _clipRect = this.clipRect;
                    _corners[0] = _clipRect[2];
                    _corners[1] = _clipRect[0];
                    _corners[2] = _clipRect[2];
                    _corners[3] = _clipRect[1];
                    _corners[4] = _clipRect[3];
                    _corners[5] = _clipRect[0];
                    _corners[6] = _clipRect[3];
                    _corners[7] = _clipRect[1];
                }
                this.stack.pullAll(this.corners, this.out, this.prev == 0);
                this.prev = 1;
            }
        }

        @Override
        public void pathDone() {
            this._closePath();
            this.out.pathDone();
            this.prev = 2;
            this.dispose();
        }

        @Override
        public void closePath() {
            this._closePath();
            if (this.prev == 1) {
                this.out.closePath();
            }
            this.prev = this.sOutCode != 0 ? 0 : 2;
            this.cOutCode = this.sOutCode;
            this.cx0 = this.sx0;
            this.cy0 = this.sy0;
        }

        private void _closePath() {
            int orCode;
            boolean prevOutside = this.outside;
            if (prevOutside) {
                this.finishPath();
            }
            if (this.prev == 1 && (orCode = this.cOutCode | this.sOutCode) != 0 && (this.cx0 != this.sx0 || this.cy0 != this.sy0)) {
                this.outside = prevOutside;
                this.lineTo(this.sx0, this.sy0);
                if (this.outside) {
                    this.finishPath();
                }
            }
        }

        @Override
        public void moveTo(double x0, double y0) {
            int outcode;
            this._closePath();
            this.prev = 0;
            this.cOutCode = outcode = Helpers.outcode(x0, y0, this.clipRect);
            this.sOutCode = outcode;
            this.cx0 = x0;
            this.cy0 = y0;
            this.sx0 = x0;
            this.sy0 = y0;
        }

        @Override
        public void setStartFlag(boolean first) {
        }

        @Override
        public void lineTo(double xe, double ye) {
            int outcode0 = this.cOutCode;
            int outcode1 = Helpers.outcode(xe, ye, this.clipRect);
            int orCode = outcode0 | outcode1;
            if (orCode != 0) {
                int sideCode = outcode0 & outcode1;
                if (sideCode == 0) {
                    if (this.subdivide) {
                        this.subdivide = false;
                        boolean ret = this.curveSplitter.splitLine(this.cx0, this.cy0, xe, ye, orCode, this);
                        this.subdivide = true;
                        if (ret) {
                            return;
                        }
                    }
                } else {
                    this.cOutCode = outcode1;
                    this.gOutCode &= sideCode;
                    this.outside = true;
                    this.cx0 = xe;
                    this.cy0 = ye;
                    this.clip(sideCode, outcode0, outcode1);
                    return;
                }
            }
            this.cOutCode = outcode1;
            this.gOutCode = 0;
            if (this.outside) {
                this.finish();
                if (outcode0 != 0) {
                    if (this.prev == 0) {
                        this.out.moveTo(this.cx0, this.cy0);
                    } else {
                        this.out.lineTo(this.cx0, this.cy0);
                    }
                    this.prev = 1;
                }
            }
            if (this.prev == 0) {
                this.out.moveTo(this.cx0, this.cy0);
            }
            this.prev = 1;
            this.out.lineTo(xe, ye);
            this.cx0 = xe;
            this.cy0 = ye;
        }

        private void clip(int sideCode, int outcode0, int outcode1) {
            if (outcode0 != outcode1 && (sideCode & 0xC) != 0) {
                int mergeCode = outcode0 | outcode1;
                int tbCode = mergeCode & 3;
                int lrCode = mergeCode & 0xC;
                int off = lrCode == 4 ? 0 : 2;
                switch (tbCode) {
                    case 1: {
                        this.stack.push(off);
                        return;
                    }
                    case 2: {
                        this.stack.push(off + 1);
                        return;
                    }
                }
                if ((outcode0 & 1) != 0) {
                    this.stack.push(off);
                    this.stack.push(off + 1);
                } else {
                    this.stack.push(off + 1);
                    this.stack.push(off);
                }
            }
        }

        @Override
        public void curveTo(double x1, double y1, double x2, double y2, double xe, double ye) {
            int outcode3;
            int outcode2;
            int outcode0 = this.cOutCode;
            int outcode1 = Helpers.outcode(x1, y1, this.clipRect);
            int orCode = outcode0 | outcode1 | (outcode2 = Helpers.outcode(x2, y2, this.clipRect)) | (outcode3 = Helpers.outcode(xe, ye, this.clipRect));
            if (orCode != 0) {
                int sideCode = outcode0 & outcode1 & outcode2 & outcode3;
                if (sideCode == 0) {
                    if (this.subdivide) {
                        this.subdivide = false;
                        boolean ret = this.curveSplitter.splitCurve(this.cx0, this.cy0, x1, y1, x2, y2, xe, ye, orCode, this);
                        this.subdivide = true;
                        if (ret) {
                            return;
                        }
                    }
                } else {
                    this.cOutCode = outcode3;
                    this.gOutCode &= sideCode;
                    this.outside = true;
                    this.cx0 = xe;
                    this.cy0 = ye;
                    this.clip(sideCode, outcode0, outcode3);
                    return;
                }
            }
            this.cOutCode = outcode3;
            this.gOutCode = 0;
            if (this.outside) {
                this.finish();
                if (outcode0 != 0) {
                    if (this.prev == 0) {
                        this.out.moveTo(this.cx0, this.cy0);
                    } else {
                        this.out.lineTo(this.cx0, this.cy0);
                    }
                    this.prev = 1;
                }
            }
            if (this.prev == 0) {
                this.out.moveTo(this.cx0, this.cy0);
            }
            this.prev = 1;
            this.out.curveTo(x1, y1, x2, y2, xe, ye);
            this.cx0 = xe;
            this.cy0 = ye;
        }

        @Override
        public void quadTo(double x1, double y1, double xe, double ye) {
            int outcode2;
            int outcode0 = this.cOutCode;
            int outcode1 = Helpers.outcode(x1, y1, this.clipRect);
            int orCode = outcode0 | outcode1 | (outcode2 = Helpers.outcode(xe, ye, this.clipRect));
            if (orCode != 0) {
                int sideCode = outcode0 & outcode1 & outcode2;
                if (sideCode == 0) {
                    if (this.subdivide) {
                        this.subdivide = false;
                        boolean ret = this.curveSplitter.splitQuad(this.cx0, this.cy0, x1, y1, xe, ye, orCode, this);
                        this.subdivide = true;
                        if (ret) {
                            return;
                        }
                    }
                } else {
                    this.cOutCode = outcode2;
                    this.gOutCode &= sideCode;
                    this.outside = true;
                    this.cx0 = xe;
                    this.cy0 = ye;
                    this.clip(sideCode, outcode0, outcode2);
                    return;
                }
            }
            this.cOutCode = outcode2;
            this.gOutCode = 0;
            if (this.outside) {
                this.finish();
                if (outcode0 != 0) {
                    if (this.prev == 0) {
                        this.out.moveTo(this.cx0, this.cy0);
                    } else {
                        this.out.lineTo(this.cx0, this.cy0);
                    }
                    this.prev = 1;
                }
            }
            if (this.prev == 0) {
                this.out.moveTo(this.cx0, this.cy0);
            }
            this.prev = 1;
            this.out.quadTo(x1, y1, xe, ye);
            this.cx0 = xe;
            this.cy0 = ye;
        }

        @Override
        public long getNativeConsumer() {
            throw new InternalError("Not using a native peer");
        }
    }

    static final class CurveBasicMonotonizer {
        private static final int MAX_N_CURVES = 11;
        private double lw2;
        int nbSplits;
        final double[] middle = new double[68];
        private final double[] subdivTs = new double[10];
        private final Curve curve;

        CurveBasicMonotonizer(RendererContext rdrCtx) {
            this.curve = rdrCtx.curve;
        }

        void init(double lineWidth) {
            this.lw2 = lineWidth * lineWidth / 4.0;
        }

        CurveBasicMonotonizer curve(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3) {
            double[] mid = this.middle;
            mid[0] = x0;
            mid[1] = y0;
            mid[2] = x1;
            mid[3] = y1;
            mid[4] = x2;
            mid[5] = y2;
            mid[6] = x3;
            mid[7] = y3;
            double[] subTs = this.subdivTs;
            int nSplits = Helpers.findSubdivPoints(this.curve, mid, subTs, 8, this.lw2);
            double prevT = 0.0;
            int i = 0;
            int off = 0;
            while (i < nSplits) {
                double t = subTs[i];
                Helpers.subdivideCubicAt((t - prevT) / (1.0 - prevT), mid, off, mid, off, off + 6);
                prevT = t;
                ++i;
                off += 6;
            }
            this.nbSplits = nSplits;
            return this;
        }

        CurveBasicMonotonizer quad(double x0, double y0, double x1, double y1, double x2, double y2) {
            double[] mid = this.middle;
            mid[0] = x0;
            mid[1] = y0;
            mid[2] = x1;
            mid[3] = y1;
            mid[4] = x2;
            mid[5] = y2;
            double[] subTs = this.subdivTs;
            int nSplits = Helpers.findSubdivPoints(this.curve, mid, subTs, 6, this.lw2);
            double prevt = 0.0;
            int i = 0;
            int off = 0;
            while (i < nSplits) {
                double t = subTs[i];
                Helpers.subdivideQuadAt((t - prevt) / (1.0 - prevt), mid, off, mid, off, off + 4);
                prevt = t;
                ++i;
                off += 4;
            }
            this.nbSplits = nSplits;
            return this;
        }
    }

    static final class CurveClipSplitter {
        static final double LEN_TH = MarlinProperties.getSubdividerMinLength();
        static final boolean DO_CHECK_LENGTH = LEN_TH > 0.0;
        private static final boolean TRACE = false;
        private static final int MAX_N_CURVES = 12;
        private final RendererContext rdrCtx;
        private double minLength;
        final double[] clipRect;
        final double[] clipRectPad = new double[4];
        private boolean init_clipRectPad = false;
        final double[] middle = new double[98];
        private final double[] subdivTs = new double[12];
        private final Curve curve;

        CurveClipSplitter(RendererContext rdrCtx) {
            this.rdrCtx = rdrCtx;
            this.clipRect = rdrCtx.clipRect;
            this.curve = rdrCtx.curve;
        }

        void init() {
            this.init_clipRectPad = true;
            if (DO_CHECK_LENGTH) {
                double d = this.minLength = this.rdrCtx.clipInvScale == 0.0 ? LEN_TH : LEN_TH * this.rdrCtx.clipInvScale;
                if (MarlinConst.DO_LOG_CLIP) {
                    MarlinUtils.logInfo("CurveClipSplitter.minLength = " + this.minLength);
                }
            }
        }

        private void initPaddedClip() {
            double[] _clipRect = this.clipRect;
            double[] _clipRectPad = this.clipRectPad;
            _clipRectPad[0] = _clipRect[0] - 0.25;
            _clipRectPad[1] = _clipRect[1] + 0.25;
            _clipRectPad[2] = _clipRect[2] - 0.25;
            _clipRectPad[3] = _clipRect[3] + 0.25;
        }

        boolean splitLine(double x0, double y0, double x1, double y1, int outCodeOR, StartFlagPathConsumer2D out) {
            if (DO_CHECK_LENGTH && Helpers.fastLineLen(x0, y0, x1, y1) <= this.minLength) {
                return false;
            }
            double[] mid = this.middle;
            mid[0] = x0;
            mid[1] = y0;
            mid[2] = x1;
            mid[3] = y1;
            return this.subdivideAtIntersections(4, outCodeOR, out);
        }

        boolean splitQuad(double x0, double y0, double x1, double y1, double x2, double y2, int outCodeOR, StartFlagPathConsumer2D out) {
            if (DO_CHECK_LENGTH && Helpers.fastQuadLen(x0, y0, x1, y1, x2, y2) <= this.minLength) {
                return false;
            }
            double[] mid = this.middle;
            mid[0] = x0;
            mid[1] = y0;
            mid[2] = x1;
            mid[3] = y1;
            mid[4] = x2;
            mid[5] = y2;
            return this.subdivideAtIntersections(6, outCodeOR, out);
        }

        boolean splitCurve(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3, int outCodeOR, StartFlagPathConsumer2D out) {
            if (DO_CHECK_LENGTH && Helpers.fastCurvelen(x0, y0, x1, y1, x2, y2, x3, y3) <= this.minLength) {
                return false;
            }
            double[] mid = this.middle;
            mid[0] = x0;
            mid[1] = y0;
            mid[2] = x1;
            mid[3] = y1;
            mid[4] = x2;
            mid[5] = y2;
            mid[6] = x3;
            mid[7] = y3;
            return this.subdivideAtIntersections(8, outCodeOR, out);
        }

        private boolean subdivideAtIntersections(int type, int outCodeOR, StartFlagPathConsumer2D out) {
            int nSplits;
            double[] mid = this.middle;
            double[] subTs = this.subdivTs;
            if (this.init_clipRectPad) {
                this.init_clipRectPad = false;
                this.initPaddedClip();
            }
            if ((nSplits = Helpers.findClipPoints(this.curve, mid, subTs, type, outCodeOR, this.clipRectPad)) == 0) {
                return false;
            }
            double prevT = 0.0;
            int i = 0;
            int off = 0;
            while (i < nSplits) {
                double t = subTs[i];
                Helpers.subdivideAt((t - prevT) / (1.0 - prevT), mid, off, mid, off, type);
                prevT = t;
                ++i;
                off += type;
            }
            i = 0;
            off = 0;
            while (i <= nSplits) {
                CurveClipSplitter.emitCurrent(type, mid, off, out);
                if (i == 0) {
                    out.setStartFlag(false);
                }
                ++i;
                off += type;
            }
            out.setStartFlag(true);
            return true;
        }

        static void emitCurrent(int type, double[] pts, int off, StartFlagPathConsumer2D out) {
            if (type == 8) {
                out.curveTo(pts[off + 2], pts[off + 3], pts[off + 4], pts[off + 5], pts[off + 6], pts[off + 7]);
            } else if (type == 4) {
                out.lineTo(pts[off + 2], pts[off + 3]);
            } else {
                out.quadTo(pts[off + 2], pts[off + 3], pts[off + 4], pts[off + 5]);
            }
        }
    }

    static interface StartFlagPathConsumer2D
    extends DPathConsumer2D {
        public void setStartFlag(boolean var1);
    }
}

