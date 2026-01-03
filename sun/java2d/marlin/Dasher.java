/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import sun.java2d.marlin.ArrayCacheDouble;
import sun.java2d.marlin.DPathConsumer2D;
import sun.java2d.marlin.FloatMath;
import sun.java2d.marlin.Helpers;
import sun.java2d.marlin.MarlinConst;
import sun.java2d.marlin.MarlinProperties;
import sun.java2d.marlin.RendererContext;
import sun.java2d.marlin.TransformingPathConsumer2D;

final class Dasher
implements TransformingPathConsumer2D.StartFlagPathConsumer2D,
MarlinConst {
    static final int REC_LIMIT = 16;
    static final double CURVE_LEN_ERR = MarlinProperties.getCurveLengthError();
    static final double MIN_T_INC = 1.52587890625E-5;
    static final double EPS = 1.0E-6;
    static final double MAX_CYCLES = 1.6E7;
    private DPathConsumer2D out;
    private double[] dash;
    private int dashLen;
    private double startPhase;
    private boolean startDashOn;
    private int startIdx;
    private boolean starting;
    private boolean needsMoveTo;
    private int idx;
    private boolean dashOn;
    private double phase;
    private double sx0;
    private double sy0;
    private double cx0;
    private double cy0;
    private final double[] curCurvepts;
    final RendererContext rdrCtx;
    boolean recycleDashes;
    private double[] firstSegmentsBuffer;
    private int firstSegidx;
    final ArrayCacheDouble.Reference dashes_ref;
    final ArrayCacheDouble.Reference firstSegmentsBuffer_ref;
    private double[] clipRect;
    private int cOutCode = 0;
    private boolean subdivide = DO_CLIP_SUBDIVIDER;
    private final LengthIterator li = new LengthIterator();
    private final TransformingPathConsumer2D.CurveClipSplitter curveSplitter;
    private double cycleLen;
    private boolean outside;
    private double totalSkipLen;

    Dasher(RendererContext rdrCtx) {
        this.rdrCtx = rdrCtx;
        this.dashes_ref = rdrCtx.newDirtyDoubleArrayRef(256);
        this.firstSegmentsBuffer_ref = rdrCtx.newDirtyDoubleArrayRef(256);
        this.firstSegmentsBuffer = this.firstSegmentsBuffer_ref.initial;
        this.curCurvepts = new double[16];
        this.curveSplitter = rdrCtx.curveClipSplitter;
    }

    Dasher init(DPathConsumer2D out, double[] dash, int dashLen, double phase, boolean recycleDashes) {
        this.out = out;
        int sidx = 0;
        this.dashOn = true;
        double sum = 0.0;
        for (int i = 0; i < dashLen; ++i) {
            sum += dash[i];
        }
        this.cycleLen = sum;
        double cycles = phase / sum;
        if (phase < 0.0) {
            if (-cycles >= 1.6E7) {
                phase = 0.0;
            } else {
                int fullcycles = FloatMath.floor_int(-cycles);
                if ((fullcycles & dashLen & 1) != 0) {
                    this.dashOn = !this.dashOn;
                }
                phase += (double)fullcycles * sum;
                while (phase < 0.0) {
                    if (--sidx < 0) {
                        sidx = dashLen - 1;
                    }
                    phase += dash[sidx];
                    this.dashOn = !this.dashOn;
                }
            }
        } else if (phase > 0.0) {
            if (cycles >= 1.6E7) {
                phase = 0.0;
            } else {
                int fullcycles = FloatMath.floor_int(cycles);
                if ((fullcycles & dashLen & 1) != 0) {
                    this.dashOn = !this.dashOn;
                }
                phase -= (double)fullcycles * sum;
                while (true) {
                    double d;
                    double d2 = dash[sidx];
                    if (!(phase >= d)) break;
                    phase -= d2;
                    sidx = (sidx + 1) % dashLen;
                    this.dashOn = !this.dashOn;
                }
            }
        }
        this.dash = dash;
        this.dashLen = dashLen;
        this.phase = phase;
        this.startPhase = phase;
        this.startDashOn = this.dashOn;
        this.startIdx = sidx;
        this.starting = true;
        this.needsMoveTo = false;
        this.firstSegidx = 0;
        this.recycleDashes = recycleDashes;
        if (this.rdrCtx.doClip) {
            this.clipRect = this.rdrCtx.clipRect;
        } else {
            this.clipRect = null;
            this.cOutCode = 0;
        }
        return this;
    }

    void dispose() {
        if (this.recycleDashes && this.dashes_ref.doCleanRef(this.dash)) {
            this.dash = this.dashes_ref.putArray(this.dash);
        }
        if (this.firstSegmentsBuffer_ref.doCleanRef(this.firstSegmentsBuffer)) {
            this.firstSegmentsBuffer = this.firstSegmentsBuffer_ref.putArray(this.firstSegmentsBuffer);
        }
    }

    double[] copyDashArray(float[] dashes) {
        double[] newDashes;
        int len = dashes.length;
        if (len <= 256) {
            newDashes = this.dashes_ref.initial;
        } else {
            if (DO_STATS) {
                this.rdrCtx.stats.stat_array_dasher_dasher.add(len);
            }
            newDashes = this.dashes_ref.getArray(len);
        }
        for (int i = 0; i < len; ++i) {
            newDashes[i] = dashes[i];
        }
        return newDashes;
    }

    @Override
    public void moveTo(double x0, double y0) {
        if (this.firstSegidx != 0) {
            this.out.moveTo(this.sx0, this.sy0);
            this.emitFirstSegments();
        }
        this.needsMoveTo = true;
        this.idx = this.startIdx;
        this.dashOn = this.startDashOn;
        this.phase = this.startPhase;
        this.cx0 = x0;
        this.cy0 = y0;
        this.sx0 = x0;
        this.sy0 = y0;
        this.starting = true;
        if (this.clipRect != null) {
            int outcode;
            this.cOutCode = outcode = Helpers.outcode(x0, y0, this.clipRect);
            this.outside = false;
            this.totalSkipLen = 0.0;
        }
    }

    private void emitSeg(double[] buf, int off, int type) {
        switch (type) {
            case 4: {
                this.out.lineTo(buf[off], buf[off + 1]);
                return;
            }
            case 8: {
                this.out.curveTo(buf[off], buf[off + 1], buf[off + 2], buf[off + 3], buf[off + 4], buf[off + 5]);
                return;
            }
            case 6: {
                this.out.quadTo(buf[off], buf[off + 1], buf[off + 2], buf[off + 3]);
                return;
            }
        }
    }

    private void emitFirstSegments() {
        int type;
        double[] fSegBuf = this.firstSegmentsBuffer;
        int len = this.firstSegidx;
        for (int i = 0; i < len; i += type - 1) {
            type = (int)fSegBuf[i];
            this.emitSeg(fSegBuf, i + 1, type);
        }
        this.firstSegidx = 0;
    }

    private void goTo(double[] pts, int off, int type, boolean on) {
        int index = off + type;
        double x = pts[index - 4];
        double y = pts[index - 3];
        if (on) {
            if (this.starting) {
                this.goTo_starting(pts, off, type);
            } else {
                if (this.needsMoveTo) {
                    this.needsMoveTo = false;
                    this.out.moveTo(this.cx0, this.cy0);
                }
                this.emitSeg(pts, off, type);
            }
        } else {
            if (this.starting) {
                this.starting = false;
            }
            this.needsMoveTo = true;
        }
        this.cx0 = x;
        this.cy0 = y;
    }

    private void goTo_starting(double[] pts, int off, int type) {
        int segIdx = this.firstSegidx;
        int len = type - 1;
        double[] buf = this.firstSegmentsBuffer;
        if (segIdx + len > buf.length) {
            if (DO_STATS) {
                this.rdrCtx.stats.stat_array_dasher_firstSegmentsBuffer.add(segIdx + len);
            }
            this.firstSegmentsBuffer = buf = this.firstSegmentsBuffer_ref.widenArray(buf, segIdx, segIdx + len);
        }
        buf[segIdx++] = type;
        if (--len == 2) {
            buf[segIdx] = pts[off];
            buf[segIdx + 1] = pts[off + 1];
        } else {
            System.arraycopy(pts, off, buf, segIdx, len);
        }
        this.firstSegidx = segIdx + len;
    }

    @Override
    public void setStartFlag(boolean first) {
        this.rdrCtx.firstFlags = first ? (this.rdrCtx.firstFlags &= 3) : (this.rdrCtx.firstFlags |= 4);
    }

    public void setMonotonizerStartFlag(boolean first) {
        this.rdrCtx.firstFlags = first ? (this.rdrCtx.firstFlags &= 5) : (this.rdrCtx.firstFlags |= 2);
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
                    this.skipLineTo(x1, y1);
                    return;
                }
            }
            this.cOutCode = outcode1;
            if (this.outside) {
                this.outside = false;
                this.skipLen();
            }
        }
        this._lineTo(x1, y1);
    }

    private void _lineTo(double x1, double y1) {
        double dx = x1 - this.cx0;
        double dy = y1 - this.cy0;
        double len = dx * dx + dy * dy;
        if (len == 0.0) {
            return;
        }
        len = Math.sqrt(len);
        double cx = dx / len;
        double cy = dy / len;
        double[] _curCurvepts = this.curCurvepts;
        double[] _dash = this.dash;
        int _dashLen = this.dashLen;
        int _idx = this.idx;
        boolean _dashOn = this.dashOn;
        double _phase = this.phase;
        while (true) {
            double leftInThisDashSegment;
            double rem;
            if ((rem = len - (leftInThisDashSegment = _dash[_idx] - _phase)) <= 1.0E-6) {
                _curCurvepts[0] = x1;
                _curCurvepts[1] = y1;
                this.goTo(_curCurvepts, 0, 4, _dashOn);
                _phase += len;
                if (!(Math.abs(rem) <= 1.0E-6)) break;
                _phase = 0.0;
                _idx = (_idx + 1) % _dashLen;
                _dashOn = !_dashOn;
                break;
            }
            _curCurvepts[0] = this.cx0 + leftInThisDashSegment * cx;
            _curCurvepts[1] = this.cy0 + leftInThisDashSegment * cy;
            this.goTo(_curCurvepts, 0, 4, _dashOn);
            len = rem;
            _idx = (_idx + 1) % _dashLen;
            _dashOn = !_dashOn;
            _phase = 0.0;
        }
        this.idx = _idx;
        this.dashOn = _dashOn;
        this.phase = _phase;
    }

    private void skipLineTo(double x1, double y1) {
        double dx = x1 - this.cx0;
        double dy = y1 - this.cy0;
        double len = dx * dx + dy * dy;
        if (len != 0.0) {
            len = Math.sqrt(len);
        }
        this.outside = true;
        this.totalSkipLen += len;
        this.needsMoveTo = true;
        this.starting = false;
        this.cx0 = x1;
        this.cy0 = y1;
    }

    public void skipLen() {
        double len = this.totalSkipLen;
        this.totalSkipLen = 0.0;
        double[] _dash = this.dash;
        int _dashLen = this.dashLen;
        int _idx = this.idx;
        boolean _dashOn = this.dashOn;
        double _phase = this.phase;
        long fullcycles = (long)Math.floor(len / this.cycleLen) - 2L;
        if (fullcycles > 0L) {
            len -= this.cycleLen * (double)fullcycles;
            long iterations = fullcycles * (long)_dashLen;
            _idx = (int)(iterations + (long)_idx) % _dashLen;
            boolean bl = _dashOn = (iterations + (_dashOn ? 1L : 0L) & 1L) == 1L;
        }
        while (true) {
            double leftInThisDashSegment;
            double rem;
            if ((rem = len - (leftInThisDashSegment = _dash[_idx] - _phase)) <= 1.0E-6) {
                _phase += len;
                if (!(Math.abs(rem) <= 1.0E-6)) break;
                _phase = 0.0;
                _idx = (_idx + 1) % _dashLen;
                _dashOn = !_dashOn;
                break;
            }
            len = rem;
            _idx = (_idx + 1) % _dashLen;
            _dashOn = !_dashOn;
            _phase = 0.0;
        }
        this.idx = _idx;
        this.dashOn = _dashOn;
        this.phase = _phase;
    }

    private void somethingTo(int type) {
        double[] _curCurvepts = this.curCurvepts;
        if (Helpers.isPointCurve(_curCurvepts, type)) {
            return;
        }
        LengthIterator _li = this.li;
        double[] _dash = this.dash;
        int _dashLen = this.dashLen;
        _li.initializeIterationOnCurve(_curCurvepts, type);
        int _idx = this.idx;
        boolean _dashOn = this.dashOn;
        double _phase = this.phase;
        int curCurveoff = 0;
        double prevT = 0.0;
        double leftInThisDashSegment = _dash[_idx] - _phase;
        while (true) {
            double d;
            double t = _li.next(leftInThisDashSegment);
            if (!(d < 1.0)) break;
            if (t != 0.0) {
                Helpers.subdivideAt((t - prevT) / (1.0 - prevT), _curCurvepts, curCurveoff, _curCurvepts, 0, type);
                prevT = t;
                this.goTo(_curCurvepts, 2, type, _dashOn);
                curCurveoff = type;
            }
            _idx = (_idx + 1) % _dashLen;
            _dashOn = !_dashOn;
            _phase = 0.0;
            leftInThisDashSegment = _dash[_idx];
        }
        this.goTo(_curCurvepts, curCurveoff + 2, type, _dashOn);
        _phase += _li.lastSegLen();
        if (_phase + 1.0E-6 >= _dash[_idx]) {
            _phase = 0.0;
            _idx = (_idx + 1) % _dashLen;
            _dashOn = !_dashOn;
        }
        this.idx = _idx;
        this.dashOn = _dashOn;
        this.phase = _phase;
        _li.reset();
    }

    private void skipSomethingTo(int type) {
        double[] _curCurvepts = this.curCurvepts;
        if (Helpers.isPointCurve(_curCurvepts, type)) {
            return;
        }
        LengthIterator _li = this.li;
        _li.initializeIterationOnCurve(_curCurvepts, type);
        double len = _li.totalLength();
        this.outside = true;
        this.totalSkipLen += len;
        this.needsMoveTo = true;
        this.starting = false;
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
                    this.skipCurveTo(x1, y1, x2, y2, x3, y3);
                    return;
                }
            }
            this.cOutCode = outcode3;
            if (this.outside) {
                this.outside = false;
                this.skipLen();
            }
        }
        this._curveTo(x1, y1, x2, y2, x3, y3);
    }

    private void _curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
        double[] _curCurvepts = this.curCurvepts;
        TransformingPathConsumer2D.CurveBasicMonotonizer monotonizer = this.rdrCtx.monotonizer.curve(this.cx0, this.cy0, x1, y1, x2, y2, x3, y3);
        int nSplits = monotonizer.nbSplits;
        double[] mid = monotonizer.middle;
        int i = 0;
        int off = 0;
        while (i <= nSplits) {
            System.arraycopy(mid, off, _curCurvepts, 0, 8);
            this.somethingTo(8);
            if (i == 0) {
                this.setMonotonizerStartFlag(false);
            }
            ++i;
            off += 6;
        }
        this.setMonotonizerStartFlag(true);
    }

    private void skipCurveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
        double[] _curCurvepts = this.curCurvepts;
        _curCurvepts[0] = this.cx0;
        _curCurvepts[1] = this.cy0;
        _curCurvepts[2] = x1;
        _curCurvepts[3] = y1;
        _curCurvepts[4] = x2;
        _curCurvepts[5] = y2;
        _curCurvepts[6] = x3;
        _curCurvepts[7] = y3;
        this.skipSomethingTo(8);
        this.cx0 = x3;
        this.cy0 = y3;
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
                    this.skipQuadTo(x1, y1, x2, y2);
                    return;
                }
            }
            this.cOutCode = outcode2;
            if (this.outside) {
                this.outside = false;
                this.skipLen();
            }
        }
        this._quadTo(x1, y1, x2, y2);
    }

    private void _quadTo(double x1, double y1, double x2, double y2) {
        double[] _curCurvepts = this.curCurvepts;
        TransformingPathConsumer2D.CurveBasicMonotonizer monotonizer = this.rdrCtx.monotonizer.quad(this.cx0, this.cy0, x1, y1, x2, y2);
        int nSplits = monotonizer.nbSplits;
        double[] mid = monotonizer.middle;
        int i = 0;
        int off = 0;
        while (i <= nSplits) {
            System.arraycopy(mid, off, _curCurvepts, 0, 8);
            this.somethingTo(6);
            if (i == 0) {
                this.setMonotonizerStartFlag(false);
            }
            ++i;
            off += 4;
        }
        this.setMonotonizerStartFlag(true);
    }

    private void skipQuadTo(double x1, double y1, double x2, double y2) {
        double[] _curCurvepts = this.curCurvepts;
        _curCurvepts[0] = this.cx0;
        _curCurvepts[1] = this.cy0;
        _curCurvepts[2] = x1;
        _curCurvepts[3] = y1;
        _curCurvepts[4] = x2;
        _curCurvepts[5] = y2;
        this.skipSomethingTo(6);
        this.cx0 = x2;
        this.cy0 = y2;
    }

    @Override
    public void closePath() {
        if (this.cx0 != this.sx0 || this.cy0 != this.sy0) {
            this.lineTo(this.sx0, this.sy0);
        }
        if (this.firstSegidx != 0) {
            if (!this.dashOn || this.needsMoveTo) {
                this.out.moveTo(this.sx0, this.sy0);
            }
            this.emitFirstSegments();
        }
        this.moveTo(this.sx0, this.sy0);
    }

    @Override
    public void pathDone() {
        if (this.firstSegidx != 0) {
            this.out.moveTo(this.sx0, this.sy0);
            this.emitFirstSegments();
        }
        this.out.pathDone();
        this.dispose();
    }

    @Override
    public long getNativeConsumer() {
        throw new InternalError("Dasher does not use a native consumer");
    }

    static final class LengthIterator {
        private final double[][] recCurveStack;
        private final boolean[] sidesRight;
        private int curveType;
        private double nextT;
        private double lenAtNextT;
        private double lastT;
        private double lenAtLastT;
        private double lenAtLastSplit;
        private double lastSegLen;
        private int recLevel;
        private boolean done = true;
        private final double[] curLeafCtrlPolyLengths = new double[3];
        private int cachedHaveLowAcceleration = -1;
        private final double[] nextRoots = new double[4];
        private final double[] flatLeafCoefCache = new double[]{0.0, 0.0, -1.0, 0.0};

        LengthIterator() {
            this.recCurveStack = new double[17][8];
            this.sidesRight = new boolean[16];
            this.nextT = Double.MAX_VALUE;
            this.lenAtNextT = Double.MAX_VALUE;
            this.lenAtLastSplit = Double.MIN_VALUE;
            this.recLevel = Integer.MIN_VALUE;
            this.lastSegLen = Double.MAX_VALUE;
        }

        void reset() {
        }

        void initializeIterationOnCurve(double[] pts, int type) {
            System.arraycopy(pts, 0, this.recCurveStack[0], 0, 8);
            this.curveType = type;
            this.recLevel = 0;
            this.lastT = 0.0;
            this.lenAtLastT = 0.0;
            this.nextT = 0.0;
            this.lenAtNextT = 0.0;
            this.goLeft();
            this.lenAtLastSplit = 0.0;
            if (this.recLevel > 0) {
                this.sidesRight[0] = false;
                this.done = false;
            } else {
                this.sidesRight[0] = true;
                this.done = true;
            }
            this.lastSegLen = 0.0;
        }

        private boolean haveLowAcceleration(double err) {
            if (this.cachedHaveLowAcceleration == -1) {
                double errLen3;
                double len3;
                double len1 = this.curLeafCtrlPolyLengths[0];
                double len2 = this.curLeafCtrlPolyLengths[1];
                if (!Helpers.within(len1, len2, err * len2)) {
                    this.cachedHaveLowAcceleration = 0;
                    return false;
                }
                if (!(this.curveType != 8 || Helpers.within(len2, len3 = this.curLeafCtrlPolyLengths[2], errLen3 = err * len3) && Helpers.within(len1, len3, errLen3))) {
                    this.cachedHaveLowAcceleration = 0;
                    return false;
                }
                this.cachedHaveLowAcceleration = 1;
                return true;
            }
            return this.cachedHaveLowAcceleration == 1;
        }

        double next(double len) {
            double targetLength = this.lenAtLastSplit + len;
            while (this.lenAtNextT < targetLength) {
                if (this.done) {
                    this.lastSegLen = this.lenAtNextT - this.lenAtLastSplit;
                    return 1.0;
                }
                this.goToNextLeaf();
            }
            this.lenAtLastSplit = targetLength;
            double leaflen = this.lenAtNextT - this.lenAtLastT;
            double t = (targetLength - this.lenAtLastT) / leaflen;
            if (!this.haveLowAcceleration(0.05)) {
                double d;
                double c;
                double b;
                double a;
                int n;
                double[] _flatLeafCoefCache = this.flatLeafCoefCache;
                if (_flatLeafCoefCache[2] < 0.0) {
                    double x = this.curLeafCtrlPolyLengths[0];
                    double y = x + this.curLeafCtrlPolyLengths[1];
                    if (this.curveType == 8) {
                        double z = y + this.curLeafCtrlPolyLengths[2];
                        _flatLeafCoefCache[0] = 3.0 * (x - y) + z;
                        _flatLeafCoefCache[1] = 3.0 * (y - 2.0 * x);
                        _flatLeafCoefCache[2] = 3.0 * x;
                        _flatLeafCoefCache[3] = -z;
                    } else if (this.curveType == 6) {
                        _flatLeafCoefCache[0] = 0.0;
                        _flatLeafCoefCache[1] = y - 2.0 * x;
                        _flatLeafCoefCache[2] = 2.0 * x;
                        _flatLeafCoefCache[3] = -y;
                    }
                }
                if ((n = Helpers.cubicRootsInAB(a = _flatLeafCoefCache[0], b = _flatLeafCoefCache[1], c = _flatLeafCoefCache[2], d = t * _flatLeafCoefCache[3], this.nextRoots, 0, 0.0, 1.0)) == 1) {
                    t = this.nextRoots[0];
                }
            }
            if ((t = t * (this.nextT - this.lastT) + this.lastT) >= 1.0) {
                t = 1.0;
                this.done = true;
            }
            this.lastSegLen = len;
            return t;
        }

        double totalLength() {
            while (!this.done) {
                this.goToNextLeaf();
            }
            this.reset();
            return this.lenAtNextT;
        }

        double lastSegLen() {
            return this.lastSegLen;
        }

        private void goToNextLeaf() {
            boolean[] _sides = this.sidesRight;
            int _recLevel = this.recLevel;
            --_recLevel;
            while (_sides[_recLevel]) {
                if (_recLevel == 0) {
                    this.recLevel = 0;
                    this.done = true;
                    return;
                }
                --_recLevel;
            }
            _sides[_recLevel] = true;
            System.arraycopy(this.recCurveStack[_recLevel++], 0, this.recCurveStack[_recLevel], 0, 8);
            this.recLevel = _recLevel;
            this.goLeft();
        }

        private void goLeft() {
            double len = this.onLeaf();
            if (len >= 0.0) {
                this.lastT = this.nextT;
                this.lenAtLastT = this.lenAtNextT;
                this.nextT += (double)(1 << 16 - this.recLevel) * 1.52587890625E-5;
                this.lenAtNextT += len;
                this.flatLeafCoefCache[2] = -1.0;
                this.cachedHaveLowAcceleration = -1;
            } else {
                Helpers.subdivide(this.recCurveStack[this.recLevel], this.recCurveStack[this.recLevel + 1], this.recCurveStack[this.recLevel], this.curveType);
                this.sidesRight[this.recLevel] = false;
                ++this.recLevel;
                this.goLeft();
            }
        }

        private double onLeaf() {
            double[] curve = this.recCurveStack[this.recLevel];
            int _curveType = this.curveType;
            double polyLen = 0.0;
            double x0 = curve[0];
            double y0 = curve[1];
            for (int i = 2; i < _curveType; i += 2) {
                double x1 = curve[i];
                double y1 = curve[i + 1];
                double len = Helpers.linelen(x0, y0, x1, y1);
                polyLen += len;
                this.curLeafCtrlPolyLengths[(i >> 1) - 1] = len;
                x0 = x1;
                y0 = y1;
            }
            double lineLen = Helpers.linelen(curve[0], curve[1], x0, y0);
            if (polyLen - lineLen < CURVE_LEN_ERR || this.recLevel == 16) {
                return (polyLen + lineLen) / 2.0;
            }
            return -1.0;
        }
    }
}

