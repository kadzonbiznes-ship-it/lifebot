/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import java.util.Arrays;
import sun.java2d.marlin.Curve;
import sun.java2d.marlin.DPathConsumer2D;
import sun.java2d.marlin.Helpers;
import sun.java2d.marlin.MarlinConst;
import sun.java2d.marlin.MarlinProperties;
import sun.java2d.marlin.MarlinUtils;
import sun.java2d.marlin.RendererContext;
import sun.java2d.marlin.TransformingPathConsumer2D;

final class Stroker
implements TransformingPathConsumer2D.StartFlagPathConsumer2D,
MarlinConst {
    private static final int MOVE_TO = 0;
    private static final int DRAWING_OP_TO = 1;
    private static final int CLOSE = 2;
    private static final double JOIN_ERROR = MarlinProperties.getStrokerJoinError();
    private static final double ROUND_JOIN_ERROR = 8.0 * JOIN_ERROR;
    private static final int JOIN_STYLE = MarlinProperties.getStrokerJoinStyle();
    private static final double C = 4.0 * (Math.sqrt(2.0) - 1.0) / 3.0;
    private static final double SQRT_2 = Math.sqrt(2.0);
    private DPathConsumer2D out;
    private int capStyle;
    private int joinStyle;
    private double lineWidth2;
    private double invHalfLineWidth2Sq;
    private final double[] offset0 = new double[2];
    private final double[] offset1 = new double[2];
    private final double[] offset2 = new double[2];
    private final double[] miter = new double[2];
    private double miterLimitSq;
    private double joinLimitMinSq;
    private int prev;
    private double sx0;
    private double sy0;
    private double sdx;
    private double sdy;
    private double cx0;
    private double cy0;
    private double cdx;
    private double cdy;
    private double smx;
    private double smy;
    private double cmx;
    private double cmy;
    private final Helpers.PolyStack reverse;
    private final double[] lp = new double[8];
    private final double[] rp = new double[8];
    final RendererContext rdrCtx;
    final Curve curve;
    private double[] clipRect;
    private int cOutCode = 0;
    private int sOutCode = 0;
    private boolean opened = false;
    private boolean capStart = false;
    private boolean monotonize;
    private boolean subdivide = false;
    private final TransformingPathConsumer2D.CurveClipSplitter curveSplitter;

    Stroker(RendererContext rdrCtx) {
        this.rdrCtx = rdrCtx;
        this.reverse = rdrCtx.stats != null ? new Helpers.PolyStack(rdrCtx, rdrCtx.stats.stat_str_polystack_types, rdrCtx.stats.stat_str_polystack_curves, rdrCtx.stats.hist_str_polystack_curves, rdrCtx.stats.stat_array_str_polystack_curves, rdrCtx.stats.stat_array_str_polystack_types) : new Helpers.PolyStack(rdrCtx);
        this.curve = rdrCtx.curve;
        this.curveSplitter = rdrCtx.curveClipSplitter;
    }

    Stroker init(DPathConsumer2D pc2d, double lineWidth, int capStyle, int joinStyle, double miterLimit, boolean subdivideCurves) {
        this.out = pc2d;
        this.lineWidth2 = lineWidth / 2.0;
        this.invHalfLineWidth2Sq = 1.0 / (2.0 * this.lineWidth2 * this.lineWidth2);
        this.monotonize = subdivideCurves;
        this.capStyle = capStyle;
        this.joinStyle = JOIN_STYLE != -1 ? JOIN_STYLE : joinStyle;
        double miterScaledLimit = 0.0;
        if (joinStyle == 0) {
            miterScaledLimit = miterLimit * this.lineWidth2;
            this.miterLimitSq = miterScaledLimit * miterScaledLimit;
            if (this.rdrCtx.doRender) {
                limitMin = (this.rdrCtx.clipInvScale == 0.0 ? JOIN_ERROR : JOIN_ERROR * this.rdrCtx.clipInvScale) + this.lineWidth2;
                this.joinLimitMinSq = limitMin * limitMin;
            } else {
                this.joinLimitMinSq = 0.0;
            }
        } else if (joinStyle == 1) {
            if (this.rdrCtx.doRender) {
                limitMin = this.rdrCtx.clipInvScale == 0.0 ? ROUND_JOIN_ERROR : ROUND_JOIN_ERROR * this.rdrCtx.clipInvScale;
                this.joinLimitMinSq = limitMin * this.lineWidth2;
            } else {
                this.joinLimitMinSq = 0.0;
            }
        }
        this.prev = 2;
        this.rdrCtx.stroking = 1;
        if (this.rdrCtx.doClip) {
            double margin = this.lineWidth2;
            if (capStyle == 2) {
                margin *= SQRT_2;
            }
            if (joinStyle == 0 && margin < miterScaledLimit) {
                margin = miterScaledLimit;
            }
            double[] _clipRect = this.rdrCtx.clipRect;
            _clipRect[0] = _clipRect[0] - margin;
            _clipRect[1] = _clipRect[1] + margin;
            _clipRect[2] = _clipRect[2] - margin;
            _clipRect[3] = _clipRect[3] + margin;
            this.clipRect = _clipRect;
            if (MarlinConst.DO_LOG_CLIP) {
                MarlinUtils.logInfo("clipRect (stroker): " + Arrays.toString(this.rdrCtx.clipRect));
            }
            if (DO_CLIP_SUBDIVIDER) {
                this.subdivide = subdivideCurves;
                this.curveSplitter.init();
            } else {
                this.subdivide = false;
            }
        } else {
            this.clipRect = null;
            this.cOutCode = 0;
            this.sOutCode = 0;
        }
        return this;
    }

    void disableClipping() {
        this.clipRect = null;
        this.cOutCode = 0;
        this.sOutCode = 0;
    }

    void dispose() {
        this.reverse.dispose();
        this.opened = false;
        this.capStart = false;
    }

    private static void computeOffset(double lx, double ly, double w, double[] m) {
        double len = lx * lx + ly * ly;
        if (len == 0.0) {
            m[0] = 0.0;
            m[1] = 0.0;
        } else {
            len = Math.sqrt(len);
            m[0] = ly * w / len;
            m[1] = -(lx * w) / len;
        }
    }

    private static boolean isCW(double dx1, double dy1, double dx2, double dy2) {
        return dx1 * dy2 <= dy1 * dx2;
    }

    private void mayDrawRoundJoin(double cx, double cy, double omx, double omy, double mx, double my, boolean rev) {
        if (omx == 0.0 && omy == 0.0 || mx == 0.0 && my == 0.0) {
            return;
        }
        double domx = omx - mx;
        double domy = omy - my;
        double lenSq = domx * domx + domy * domy;
        if (lenSq < this.joinLimitMinSq) {
            return;
        }
        if (rev) {
            omx = -omx;
            omy = -omy;
            mx = -mx;
            my = -my;
        }
        this.drawRoundJoin(cx, cy, omx, omy, mx, my, rev);
    }

    private void drawRoundJoin(double cx, double cy, double omx, double omy, double mx, double my, boolean rev) {
        double cosext = omx * mx + omy * my;
        if (cosext >= 0.0) {
            this.drawBezApproxForArc(cx, cy, omx, omy, mx, my, rev);
        } else {
            double nx = my - omy;
            double ny = omx - mx;
            double nlen = Math.sqrt(nx * nx + ny * ny);
            double scale = this.lineWidth2 / nlen;
            double mmx = nx * scale;
            double mmy = ny * scale;
            if (rev) {
                mmx = -mmx;
                mmy = -mmy;
            }
            this.drawBezApproxForArc(cx, cy, omx, omy, mmx, mmy, rev);
            this.drawBezApproxForArc(cx, cy, mmx, mmy, mx, my, rev);
        }
    }

    private void drawBezApproxForArc(double cx, double cy, double omx, double omy, double mx, double my, boolean rev) {
        double cosext2 = (omx * mx + omy * my) * this.invHalfLineWidth2Sq;
        if (cosext2 >= 0.5) {
            return;
        }
        double cv = 1.3333333333333333 * Math.sqrt(0.5 - cosext2) / (1.0 + Math.sqrt(cosext2 + 0.5));
        if (rev) {
            cv = -cv;
        }
        double x1 = cx + omx;
        double y1 = cy + omy;
        double x2 = x1 - cv * omy;
        double y2 = y1 + cv * omx;
        double x4 = cx + mx;
        double y4 = cy + my;
        double x3 = x4 + cv * my;
        double y3 = y4 - cv * mx;
        this.emitCurveTo(x1, y1, x2, y2, x3, y3, x4, y4, rev);
    }

    private void drawRoundCap(double cx, double cy, double mx, double my) {
        double Cmx = C * mx;
        double Cmy = C * my;
        this.emitCurveTo(cx + mx - Cmy, cy + my + Cmx, cx - my + Cmx, cy + mx + Cmy, cx - my, cy + mx);
        this.emitCurveTo(cx - my - Cmx, cy + mx - Cmy, cx - mx - Cmy, cy - my + Cmx, cx - mx, cy - my);
    }

    private static void computeMiter(double x0, double y0, double x1, double y1, double x0p, double y0p, double x1p, double y1p, double[] m) {
        double x10 = x1 - x0;
        double y10 = y1 - y0;
        double x10p = x1p - x0p;
        double y10p = y1p - y0p;
        double den = x10 * y10p - x10p * y10;
        double t = x10p * (y0 - y0p) - y10p * (x0 - x0p);
        m[0] = x0 + (t /= den) * x10;
        m[1] = y0 + t * y10;
    }

    private static void safeComputeMiter(double x0, double y0, double x1, double y1, double x0p, double y0p, double x1p, double y1p, double[] m) {
        double x10 = x1 - x0;
        double y10p = y1p - y0p;
        double x10p = x1p - x0p;
        double y10 = y1 - y0;
        double den = x10 * y10p - x10p * y10;
        if (den == 0.0) {
            m[2] = (x0 + x0p) / 2.0;
            m[3] = (y0 + y0p) / 2.0;
        } else {
            double t = x10p * (y0 - y0p) - y10p * (x0 - x0p);
            m[2] = x0 + (t /= den) * x10;
            m[3] = y0 + t * y10;
        }
    }

    private void drawMiter(double pdx, double pdy, double x0, double y0, double dx, double dy, double omx, double omy, double mx, double my, boolean rev) {
        if (mx == omx && my == omy || pdx == 0.0 && pdy == 0.0 || dx == 0.0 && dy == 0.0) {
            return;
        }
        if (rev) {
            omx = -omx;
            omy = -omy;
            mx = -mx;
            my = -my;
        }
        Stroker.computeMiter(x0 - pdx + omx, y0 - pdy + omy, x0 + omx, y0 + omy, dx + x0 + mx, dy + y0 + my, x0 + mx, y0 + my, this.miter);
        double miterX = this.miter[0];
        double miterY = this.miter[1];
        double lenSq = (miterX - x0) * (miterX - x0) + (miterY - y0) * (miterY - y0);
        if (lenSq < this.miterLimitSq && lenSq >= this.joinLimitMinSq) {
            this.emitLineTo(miterX, miterY, rev);
        }
    }

    @Override
    public void moveTo(double x0, double y0) {
        this._moveTo(x0, y0, this.cOutCode);
        this.sx0 = x0;
        this.sy0 = y0;
        this.sdx = 1.0;
        this.sdy = 0.0;
        this.opened = false;
        this.capStart = false;
        if (this.clipRect != null) {
            int outcode;
            this.cOutCode = outcode = Helpers.outcode(x0, y0, this.clipRect);
            this.sOutCode = outcode;
        }
    }

    private void _moveTo(double x0, double y0, int outcode) {
        if (this.prev == 0) {
            this.cx0 = x0;
            this.cy0 = y0;
        } else {
            if (this.prev == 1) {
                this.finish(outcode);
            }
            this.prev = 0;
            this.cx0 = x0;
            this.cy0 = y0;
            this.cdx = 1.0;
            this.cdy = 0.0;
        }
    }

    @Override
    public void setStartFlag(boolean first) {
        this.rdrCtx.firstFlags = first ? (this.rdrCtx.firstFlags &= 6) : (this.rdrCtx.firstFlags |= 1);
    }

    @Override
    public void lineTo(double x1, double y1) {
        int outcode0 = this.cOutCode;
        if (this.clipRect != null) {
            int outcode1 = Helpers.outcode(x1, y1, this.clipRect);
            int orCode = outcode0 | outcode1;
            if (orCode != 0) {
                int sideCode = outcode0 & outcode1;
                if (sideCode == 0) {
                    if (this.subdivide) {
                        this.subdivide = false;
                        boolean ret = this.curveSplitter.splitLine(this.cx0, this.cy0, x1, y1, orCode, this);
                        this.subdivide = true;
                        if (ret) {
                            return;
                        }
                    }
                } else {
                    this.cOutCode = outcode1;
                    this._moveTo(x1, y1, outcode0);
                    this.opened = true;
                    return;
                }
            }
            this.cOutCode = outcode1;
        }
        double dx = x1 - this.cx0;
        double dy = y1 - this.cy0;
        if (dx == 0.0 && dy == 0.0) {
            if (this.prev == 1) {
                return;
            }
            dx = 1.0;
        }
        Stroker.computeOffset(dx, dy, this.lineWidth2, this.offset0);
        double mx = this.offset0[0];
        double my = this.offset0[1];
        this.drawJoin(this.cdx, this.cdy, this.cx0, this.cy0, dx, dy, this.cmx, this.cmy, mx, my, outcode0);
        this.emitLineTo(this.cx0 + mx, this.cy0 + my);
        this.emitLineTo(x1 + mx, y1 + my);
        this.emitLineToRev(this.cx0 - mx, this.cy0 - my);
        this.emitLineToRev(x1 - mx, y1 - my);
        this.prev = 1;
        this.cx0 = x1;
        this.cy0 = y1;
        this.cdx = dx;
        this.cdy = dy;
        this.cmx = mx;
        this.cmy = my;
    }

    @Override
    public void closePath() {
        if (this.prev != 1 && !this.opened) {
            if (this.prev == 2) {
                return;
            }
            this.emitMoveTo(this.cx0, this.cy0 - this.lineWidth2);
            this.sdx = 1.0;
            this.sdy = 0.0;
            this.cdx = 1.0;
            this.cdy = 0.0;
            this.smx = 0.0;
            this.smy = -this.lineWidth2;
            this.cmx = 0.0;
            this.cmy = -this.lineWidth2;
            this.finish(this.cOutCode);
            return;
        }
        if ((this.sOutCode & this.cOutCode) == 0) {
            if (this.cx0 != this.sx0 || this.cy0 != this.sy0) {
                this.lineTo(this.sx0, this.sy0);
            }
            if (this.sOutCode == 0) {
                this.drawJoin(this.cdx, this.cdy, this.cx0, this.cy0, this.sdx, this.sdy, this.cmx, this.cmy, this.smx, this.smy, this.sOutCode);
                this.emitLineTo(this.sx0 + this.smx, this.sy0 + this.smy);
                if (this.opened) {
                    this.emitLineTo(this.sx0 - this.smx, this.sy0 - this.smy);
                } else {
                    this.emitMoveTo(this.sx0 - this.smx, this.sy0 - this.smy);
                }
            }
        }
        this.emitReverse();
        this.prev = 2;
        this.cx0 = this.sx0;
        this.cy0 = this.sy0;
        this.cOutCode = this.sOutCode;
        if (this.opened) {
            this.opened = false;
        } else {
            this.emitClose();
        }
    }

    private void emitReverse() {
        this.reverse.popAll(this.out);
    }

    @Override
    public void pathDone() {
        if (this.prev == 1) {
            this.finish(this.cOutCode);
        }
        this.out.pathDone();
        this.prev = 2;
        this.dispose();
    }

    private void finish(int outcode) {
        if (this.rdrCtx.closedPath) {
            this.emitReverse();
        } else {
            if (outcode == 0) {
                if (this.capStyle == 1) {
                    this.drawRoundCap(this.cx0, this.cy0, this.cmx, this.cmy);
                } else if (this.capStyle == 2) {
                    this.emitLineTo(this.cx0 - this.cmy + this.cmx, this.cy0 + this.cmx + this.cmy);
                    this.emitLineTo(this.cx0 - this.cmy - this.cmx, this.cy0 + this.cmx - this.cmy);
                }
            }
            this.emitReverse();
            if (!this.capStart) {
                this.capStart = true;
                if (this.sOutCode == 0) {
                    if (this.capStyle == 1) {
                        this.drawRoundCap(this.sx0, this.sy0, -this.smx, -this.smy);
                    } else if (this.capStyle == 2) {
                        this.emitLineTo(this.sx0 + this.smy - this.smx, this.sy0 - this.smx - this.smy);
                        this.emitLineTo(this.sx0 + this.smy + this.smx, this.sy0 - this.smx + this.smy);
                    }
                }
            }
        }
        this.emitClose();
    }

    private void emitMoveTo(double x0, double y0) {
        this.out.moveTo(x0, y0);
    }

    private void emitLineTo(double x1, double y1) {
        this.out.lineTo(x1, y1);
    }

    private void emitLineToRev(double x1, double y1) {
        this.reverse.pushLine(x1, y1);
    }

    private void emitLineTo(double x1, double y1, boolean rev) {
        if (rev) {
            this.emitLineToRev(x1, y1);
        } else {
            this.emitLineTo(x1, y1);
        }
    }

    private void emitQuadTo(double x1, double y1, double x2, double y2) {
        this.out.quadTo(x1, y1, x2, y2);
    }

    private void emitQuadToRev(double x0, double y0, double x1, double y1) {
        this.reverse.pushQuad(x0, y0, x1, y1);
    }

    private void emitCurveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
        this.out.curveTo(x1, y1, x2, y2, x3, y3);
    }

    private void emitCurveToRev(double x0, double y0, double x1, double y1, double x2, double y2) {
        this.reverse.pushCubic(x0, y0, x1, y1, x2, y2);
    }

    private void emitCurveTo(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3, boolean rev) {
        if (rev) {
            this.reverse.pushCubic(x0, y0, x1, y1, x2, y2);
        } else {
            this.out.curveTo(x1, y1, x2, y2, x3, y3);
        }
    }

    private void emitClose() {
        this.out.closePath();
    }

    private void drawJoin(double pdx, double pdy, double x0, double y0, double dx, double dy, double omx, double omy, double mx, double my, int outcode) {
        if (this.prev != 1) {
            this.prev = 1;
            this.emitMoveTo(x0 + mx, y0 + my);
            if (!this.opened) {
                this.sdx = dx;
                this.sdy = dy;
                this.smx = mx;
                this.smy = my;
            }
        } else if (this.rdrCtx.firstFlags == 0) {
            boolean cw = Stroker.isCW(pdx, pdy, dx, dy);
            if (outcode == 0) {
                if (this.joinStyle == 0) {
                    this.drawMiter(pdx, pdy, x0, y0, dx, dy, omx, omy, mx, my, cw);
                } else if (this.joinStyle == 1) {
                    this.mayDrawRoundJoin(x0, y0, omx, omy, mx, my, cw);
                }
            }
            this.emitLineTo(x0, y0, !cw);
        }
    }

    private int getLineOffsets(double x1, double y1, double x2, double y2, double[] left, double[] right) {
        Stroker.computeOffset(x2 - x1, y2 - y1, this.lineWidth2, this.offset0);
        double mx = this.offset0[0];
        double my = this.offset0[1];
        left[0] = x1 + mx;
        left[1] = y1 + my;
        left[2] = x2 + mx;
        left[3] = y2 + my;
        right[0] = x1 - mx;
        right[1] = y1 - my;
        right[2] = x2 - mx;
        right[3] = y2 - my;
        return 4;
    }

    private int computeOffsetCubic(double[] pts, int off, double[] leftOff, double[] rightOff) {
        double y3p;
        double x3p;
        double y2p;
        double x2p;
        double x1 = pts[off];
        double y1 = pts[off + 1];
        double x2 = pts[off + 2];
        double y2 = pts[off + 3];
        double x3 = pts[off + 4];
        double y3 = pts[off + 5];
        double x4 = pts[off + 6];
        double y4 = pts[off + 7];
        double dx1 = x2 - x1;
        double dy1 = y2 - y1;
        double dx4 = x4 - x3;
        double dy4 = y4 - y3;
        boolean p1eqp2 = Helpers.withinD(dx1, dy1, 6.0 * Math.ulp(y2));
        boolean p3eqp4 = Helpers.withinD(dx4, dy4, 6.0 * Math.ulp(y4));
        if (p1eqp2 && p3eqp4) {
            return this.getLineOffsets(x1, y1, x4, y4, leftOff, rightOff);
        }
        if (p1eqp2) {
            dx1 = x3 - x1;
            dy1 = y3 - y1;
        } else if (p3eqp4) {
            dx4 = x4 - x2;
            dy4 = y4 - y2;
        }
        double dotsq = dx1 * dx4 + dy1 * dy4;
        dotsq *= dotsq;
        double l1sq = dx1 * dx1 + dy1 * dy1;
        double l4sq = dx4 * dx4 + dy4 * dy4;
        if (Helpers.within(dotsq, l1sq * l4sq, 4.0 * Math.ulp(dotsq))) {
            return this.getLineOffsets(x1, y1, x4, y4, leftOff, rightOff);
        }
        double xm = (x1 + x4 + 3.0 * (x2 + x3)) / 8.0;
        double ym = (y1 + y4 + 3.0 * (y2 + y3)) / 8.0;
        double dxm = x3 + x4 - (x1 + x2);
        double dym = y3 + y4 - (y1 + y2);
        Stroker.computeOffset(dx1, dy1, this.lineWidth2, this.offset0);
        Stroker.computeOffset(dxm, dym, this.lineWidth2, this.offset1);
        Stroker.computeOffset(dx4, dy4, this.lineWidth2, this.offset2);
        double x1p = x1 + this.offset0[0];
        double y1p = y1 + this.offset0[1];
        double xi = xm + this.offset1[0];
        double yi = ym + this.offset1[1];
        double x4p = x4 + this.offset2[0];
        double y4p = y4 + this.offset2[1];
        double invdet43 = 4.0 / (3.0 * (dx1 * dy4 - dy1 * dx4));
        double two_pi_m_p1_m_p4x = 2.0 * xi - (x1p + x4p);
        double two_pi_m_p1_m_p4y = 2.0 * yi - (y1p + y4p);
        double c1 = invdet43 * (dy4 * two_pi_m_p1_m_p4x - dx4 * two_pi_m_p1_m_p4y);
        double c2 = invdet43 * (dx1 * two_pi_m_p1_m_p4y - dy1 * two_pi_m_p1_m_p4x);
        if (c1 * c2 > 0.0) {
            x2p = x2 + this.offset1[0];
            y2p = y2 + this.offset1[1];
            x3p = x3 + this.offset1[0];
            y3p = y3 + this.offset1[1];
            Stroker.safeComputeMiter(x1p, y1p, x1p + dx1, y1p + dy1, x2p, y2p, x2p - dxm, y2p - dym, leftOff);
            x2p = leftOff[2];
            y2p = leftOff[3];
            Stroker.safeComputeMiter(x4p, y4p, x4p + dx4, y4p + dy4, x3p, y3p, x3p - dxm, y3p - dym, leftOff);
            x3p = leftOff[2];
            y3p = leftOff[3];
        } else {
            x2p = x1p + c1 * dx1;
            y2p = y1p + c1 * dy1;
            x3p = x4p + c2 * dx4;
            y3p = y4p + c2 * dy4;
        }
        leftOff[0] = x1p;
        leftOff[1] = y1p;
        leftOff[2] = x2p;
        leftOff[3] = y2p;
        leftOff[4] = x3p;
        leftOff[5] = y3p;
        leftOff[6] = x4p;
        leftOff[7] = y4p;
        x1p = x1 - this.offset0[0];
        y1p = y1 - this.offset0[1];
        xi = xm - this.offset1[0];
        yi = ym - this.offset1[1];
        x4p = x4 - this.offset2[0];
        y4p = y4 - this.offset2[1];
        two_pi_m_p1_m_p4x = 2.0 * xi - (x1p + x4p);
        two_pi_m_p1_m_p4y = 2.0 * yi - (y1p + y4p);
        c1 = invdet43 * (dy4 * two_pi_m_p1_m_p4x - dx4 * two_pi_m_p1_m_p4y);
        c2 = invdet43 * (dx1 * two_pi_m_p1_m_p4y - dy1 * two_pi_m_p1_m_p4x);
        if (c1 * c2 > 0.0) {
            x2p = x2 - this.offset1[0];
            y2p = y2 - this.offset1[1];
            x3p = x3 - this.offset1[0];
            y3p = y3 - this.offset1[1];
            Stroker.safeComputeMiter(x1p, y1p, x1p + dx1, y1p + dy1, x2p, y2p, x2p - dxm, y2p - dym, rightOff);
            x2p = rightOff[2];
            y2p = rightOff[3];
            Stroker.safeComputeMiter(x4p, y4p, x4p + dx4, y4p + dy4, x3p, y3p, x3p - dxm, y3p - dym, rightOff);
            x3p = rightOff[2];
            y3p = rightOff[3];
        } else {
            x2p = x1p + c1 * dx1;
            y2p = y1p + c1 * dy1;
            x3p = x4p + c2 * dx4;
            y3p = y4p + c2 * dy4;
        }
        rightOff[0] = x1p;
        rightOff[1] = y1p;
        rightOff[2] = x2p;
        rightOff[3] = y2p;
        rightOff[4] = x3p;
        rightOff[5] = y3p;
        rightOff[6] = x4p;
        rightOff[7] = y4p;
        return 8;
    }

    private int computeOffsetQuad(double[] pts, int off, double[] leftOff, double[] rightOff) {
        double x1 = pts[off];
        double y1 = pts[off + 1];
        double x2 = pts[off + 2];
        double y2 = pts[off + 3];
        double x3 = pts[off + 4];
        double y3 = pts[off + 5];
        double dx12 = x2 - x1;
        double dy12 = y2 - y1;
        double dx23 = x3 - x2;
        double dy23 = y3 - y2;
        boolean p1eqp2 = Helpers.withinD(dx12, dy12, 6.0 * Math.ulp(y2));
        boolean p2eqp3 = Helpers.withinD(dx23, dy23, 6.0 * Math.ulp(y3));
        if (p1eqp2 || p2eqp3) {
            return this.getLineOffsets(x1, y1, x3, y3, leftOff, rightOff);
        }
        double dotsq = dx12 * dx23 + dy12 * dy23;
        double l1sq = dx12 * dx12 + dy12 * dy12;
        double l3sq = dx23 * dx23 + dy23 * dy23;
        if (Helpers.within(dotsq *= dotsq, l1sq * l3sq, 4.0 * Math.ulp(dotsq))) {
            return this.getLineOffsets(x1, y1, x3, y3, leftOff, rightOff);
        }
        Stroker.computeOffset(dx12, dy12, this.lineWidth2, this.offset0);
        Stroker.computeOffset(dx23, dy23, this.lineWidth2, this.offset1);
        double x1p = x1 + this.offset0[0];
        double y1p = y1 + this.offset0[1];
        double x3p = x3 + this.offset1[0];
        double y3p = y3 + this.offset1[1];
        Stroker.safeComputeMiter(x1p, y1p, x1p + dx12, y1p + dy12, x3p, y3p, x3p - dx23, y3p - dy23, leftOff);
        leftOff[0] = x1p;
        leftOff[1] = y1p;
        leftOff[4] = x3p;
        leftOff[5] = y3p;
        x1p = x1 - this.offset0[0];
        y1p = y1 - this.offset0[1];
        x3p = x3 - this.offset1[0];
        y3p = y3 - this.offset1[1];
        Stroker.safeComputeMiter(x1p, y1p, x1p + dx12, y1p + dy12, x3p, y3p, x3p - dx23, y3p - dy23, rightOff);
        rightOff[0] = x1p;
        rightOff[1] = y1p;
        rightOff[4] = x3p;
        rightOff[5] = y3p;
        return 6;
    }

    @Override
    public void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
        int outcode0 = this.cOutCode;
        if (this.clipRect != null) {
            int outcode3;
            int outcode2;
            int outcode1 = Helpers.outcode(x1, y1, this.clipRect);
            int orCode = outcode0 | outcode1 | (outcode2 = Helpers.outcode(x2, y2, this.clipRect)) | (outcode3 = Helpers.outcode(x3, y3, this.clipRect));
            if (orCode != 0) {
                int sideCode = outcode0 & outcode1 & outcode2 & outcode3;
                if (sideCode == 0) {
                    if (this.subdivide) {
                        this.subdivide = false;
                        boolean ret = this.curveSplitter.splitCurve(this.cx0, this.cy0, x1, y1, x2, y2, x3, y3, orCode, this);
                        this.subdivide = true;
                        if (ret) {
                            return;
                        }
                    }
                } else {
                    this.cOutCode = outcode3;
                    this._moveTo(x3, y3, outcode0);
                    this.opened = true;
                    return;
                }
            }
            this.cOutCode = outcode3;
        }
        this._curveTo(x1, y1, x2, y2, x3, y3, outcode0);
    }

    private void _curveTo(double x1, double y1, double x2, double y2, double x3, double y3, int outcode0) {
        double[] mid;
        double len;
        double dxs = x1 - this.cx0;
        double dys = y1 - this.cy0;
        double dxf = x3 - x2;
        double dyf = y3 - y2;
        if (dxs == 0.0 && dys == 0.0) {
            dxs = x2 - this.cx0;
            dys = y2 - this.cy0;
            if (dxs == 0.0 && dys == 0.0) {
                dxs = x3 - this.cx0;
                dys = y3 - this.cy0;
            }
        }
        if (dxf == 0.0 && dyf == 0.0) {
            dxf = x3 - x1;
            dyf = y3 - y1;
            if (dxf == 0.0 && dyf == 0.0) {
                dxf = x3 - this.cx0;
                dyf = y3 - this.cy0;
            }
        }
        if (dxs == 0.0 && dys == 0.0) {
            if (this.clipRect != null) {
                this.cOutCode = outcode0;
            }
            this.lineTo(this.cx0, this.cy0);
            return;
        }
        if (Math.abs(dxs) < 0.1 && Math.abs(dys) < 0.1) {
            len = Math.sqrt(dxs * dxs + dys * dys);
            dxs /= len;
            dys /= len;
        }
        if (Math.abs(dxf) < 0.1 && Math.abs(dyf) < 0.1) {
            len = Math.sqrt(dxf * dxf + dyf * dyf);
            dxf /= len;
            dyf /= len;
        }
        Stroker.computeOffset(dxs, dys, this.lineWidth2, this.offset0);
        this.drawJoin(this.cdx, this.cdy, this.cx0, this.cy0, dxs, dys, this.cmx, this.cmy, this.offset0[0], this.offset0[1], outcode0);
        int nSplits = 0;
        double[] l = this.lp;
        if (this.monotonize) {
            TransformingPathConsumer2D.CurveBasicMonotonizer monotonizer = this.rdrCtx.monotonizer.curve(this.cx0, this.cy0, x1, y1, x2, y2, x3, y3);
            nSplits = monotonizer.nbSplits;
            mid = monotonizer.middle;
        } else {
            mid = l;
            mid[0] = this.cx0;
            mid[1] = this.cy0;
            mid[2] = x1;
            mid[3] = y1;
            mid[4] = x2;
            mid[5] = y2;
            mid[6] = x3;
            mid[7] = y3;
        }
        double[] r = this.rp;
        int kind = 0;
        int i = 0;
        int off = 0;
        while (i <= nSplits) {
            kind = this.computeOffsetCubic(mid, off, l, r);
            this.emitLineTo(l[0], l[1]);
            switch (kind) {
                case 8: {
                    this.emitCurveTo(l[2], l[3], l[4], l[5], l[6], l[7]);
                    this.emitCurveToRev(r[0], r[1], r[2], r[3], r[4], r[5]);
                    break;
                }
                case 4: {
                    this.emitLineTo(l[2], l[3]);
                    this.emitLineToRev(r[0], r[1]);
                    break;
                }
            }
            this.emitLineToRev(r[kind - 2], r[kind - 1]);
            ++i;
            off += 6;
        }
        this.prev = 1;
        this.cx0 = x3;
        this.cy0 = y3;
        this.cdx = dxf;
        this.cdy = dyf;
        this.cmx = (l[kind - 2] - r[kind - 2]) / 2.0;
        this.cmy = (l[kind - 1] - r[kind - 1]) / 2.0;
    }

    @Override
    public void quadTo(double x1, double y1, double x2, double y2) {
        int outcode0 = this.cOutCode;
        if (this.clipRect != null) {
            int outcode2;
            int outcode1 = Helpers.outcode(x1, y1, this.clipRect);
            int orCode = outcode0 | outcode1 | (outcode2 = Helpers.outcode(x2, y2, this.clipRect));
            if (orCode != 0) {
                int sideCode = outcode0 & outcode1 & outcode2;
                if (sideCode == 0) {
                    if (this.subdivide) {
                        this.subdivide = false;
                        boolean ret = this.curveSplitter.splitQuad(this.cx0, this.cy0, x1, y1, x2, y2, orCode, this);
                        this.subdivide = true;
                        if (ret) {
                            return;
                        }
                    }
                } else {
                    this.cOutCode = outcode2;
                    this._moveTo(x2, y2, outcode0);
                    this.opened = true;
                    return;
                }
            }
            this.cOutCode = outcode2;
        }
        this._quadTo(x1, y1, x2, y2, outcode0);
    }

    private void _quadTo(double x1, double y1, double x2, double y2, int outcode0) {
        double[] mid;
        double len;
        double dxs = x1 - this.cx0;
        double dys = y1 - this.cy0;
        double dxf = x2 - x1;
        double dyf = y2 - y1;
        if (dxs == 0.0 && dys == 0.0 || dxf == 0.0 && dyf == 0.0) {
            dxs = dxf = x2 - this.cx0;
            dys = dyf = y2 - this.cy0;
        }
        if (dxs == 0.0 && dys == 0.0) {
            if (this.clipRect != null) {
                this.cOutCode = outcode0;
            }
            this.lineTo(this.cx0, this.cy0);
            return;
        }
        if (Math.abs(dxs) < 0.1 && Math.abs(dys) < 0.1) {
            len = Math.sqrt(dxs * dxs + dys * dys);
            dxs /= len;
            dys /= len;
        }
        if (Math.abs(dxf) < 0.1 && Math.abs(dyf) < 0.1) {
            len = Math.sqrt(dxf * dxf + dyf * dyf);
            dxf /= len;
            dyf /= len;
        }
        Stroker.computeOffset(dxs, dys, this.lineWidth2, this.offset0);
        this.drawJoin(this.cdx, this.cdy, this.cx0, this.cy0, dxs, dys, this.cmx, this.cmy, this.offset0[0], this.offset0[1], outcode0);
        int nSplits = 0;
        double[] l = this.lp;
        if (this.monotonize) {
            TransformingPathConsumer2D.CurveBasicMonotonizer monotonizer = this.rdrCtx.monotonizer.quad(this.cx0, this.cy0, x1, y1, x2, y2);
            nSplits = monotonizer.nbSplits;
            mid = monotonizer.middle;
        } else {
            mid = l;
            mid[0] = this.cx0;
            mid[1] = this.cy0;
            mid[2] = x1;
            mid[3] = y1;
            mid[4] = x2;
            mid[5] = y2;
        }
        double[] r = this.rp;
        int kind = 0;
        int i = 0;
        int off = 0;
        while (i <= nSplits) {
            kind = this.computeOffsetQuad(mid, off, l, r);
            this.emitLineTo(l[0], l[1]);
            switch (kind) {
                case 6: {
                    this.emitQuadTo(l[2], l[3], l[4], l[5]);
                    this.emitQuadToRev(r[0], r[1], r[2], r[3]);
                    break;
                }
                case 4: {
                    this.emitLineTo(l[2], l[3]);
                    this.emitLineToRev(r[0], r[1]);
                    break;
                }
            }
            this.emitLineToRev(r[kind - 2], r[kind - 1]);
            ++i;
            off += 4;
        }
        this.prev = 1;
        this.cx0 = x2;
        this.cy0 = y2;
        this.cdx = dxf;
        this.cdy = dyf;
        this.cmx = (l[kind - 2] - r[kind - 2]) / 2.0;
        this.cmy = (l[kind - 1] - r[kind - 1]) / 2.0;
    }

    @Override
    public long getNativeConsumer() {
        throw new InternalError("Stroker doesn't use a native consumer");
    }
}

