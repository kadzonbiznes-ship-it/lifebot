/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import jdk.internal.misc.Unsafe;
import sun.java2d.marlin.ArrayCacheConst;
import sun.java2d.marlin.ArrayCacheInt;
import sun.java2d.marlin.ArrayCacheIntClean;
import sun.java2d.marlin.Curve;
import sun.java2d.marlin.DMarlinRenderingEngine;
import sun.java2d.marlin.DPQSSorterContext;
import sun.java2d.marlin.DPathConsumer2D;
import sun.java2d.marlin.FloatMath;
import sun.java2d.marlin.MarlinCache;
import sun.java2d.marlin.MarlinConst;
import sun.java2d.marlin.MarlinProperties;
import sun.java2d.marlin.MarlinUtils;
import sun.java2d.marlin.MergeSort;
import sun.java2d.marlin.OffHeapArray;
import sun.java2d.marlin.RendererContext;

final class Renderer
implements DPathConsumer2D,
MarlinConst {
    static final boolean DISABLE_RENDER = MarlinProperties.isSkipRenderer();
    static final boolean ENABLE_BLOCK_FLAGS = MarlinProperties.isUseTileFlags();
    static final boolean ENABLE_BLOCK_FLAGS_HEURISTICS = MarlinProperties.isUseTileFlagsWithHeuristics();
    private static final int ALL_BUT_LSB = -2;
    private static final int ERR_STEP_MAX = Integer.MAX_VALUE;
    private static final double POWER_2_TO_32 = 4.294967296E9;
    static final double SUBPIXEL_SCALE_X = SUBPIXEL_POSITIONS_X;
    static final double SUBPIXEL_SCALE_Y = SUBPIXEL_POSITIONS_Y;
    static final int SUBPIXEL_MASK_X = SUBPIXEL_POSITIONS_X - 1;
    static final int SUBPIXEL_MASK_Y = SUBPIXEL_POSITIONS_Y - 1;
    static final double RDR_OFFSET_X = 0.5 / SUBPIXEL_SCALE_X;
    static final double RDR_OFFSET_Y = 0.5 / SUBPIXEL_SCALE_Y;
    private static final int SUBPIXEL_TILE = TILE_H << SUBPIXEL_LG_POSITIONS_Y;
    static final int INITIAL_BUCKET_ARRAY = INITIAL_PIXEL_HEIGHT * SUBPIXEL_POSITIONS_Y;
    static final int INITIAL_CROSSING_COUNT = INITIAL_EDGES_COUNT >> 2;
    public static final long OFF_CURX_OR = 0L;
    public static final long OFF_ERROR = 0L + (long)OffHeapArray.SIZE_INT;
    public static final long OFF_BUMP_X = OFF_ERROR + (long)OffHeapArray.SIZE_INT;
    public static final long OFF_BUMP_ERR = OFF_BUMP_X + (long)OffHeapArray.SIZE_INT;
    public static final long OFF_NEXT = OFF_BUMP_ERR + (long)OffHeapArray.SIZE_INT;
    public static final long OFF_YMAX = OFF_NEXT + (long)OffHeapArray.SIZE_INT;
    public static final int SIZEOF_EDGE_BYTES = (int)(OFF_YMAX + (long)OffHeapArray.SIZE_INT);
    private static final double CUB_DEC_ERR_SUBPIX = (double)MarlinProperties.getCubicDecD2() * ((double)SUBPIXEL_POSITIONS_X / 8.0);
    private static final double CUB_INC_ERR_SUBPIX = (double)MarlinProperties.getCubicIncD1() * ((double)SUBPIXEL_POSITIONS_X / 8.0);
    public static final double SCALE_DY = (double)SUBPIXEL_POSITIONS_X / (double)SUBPIXEL_POSITIONS_Y;
    public static final double CUB_DEC_BND = 8.0 * CUB_DEC_ERR_SUBPIX;
    public static final double CUB_INC_BND = 8.0 * CUB_INC_ERR_SUBPIX;
    public static final int CUB_COUNT_LG = 2;
    private static final int CUB_COUNT = 4;
    private static final int CUB_COUNT_2 = 16;
    private static final int CUB_COUNT_3 = 64;
    private static final double CUB_INV_COUNT = 0.25;
    private static final double CUB_INV_COUNT_2 = 0.0625;
    private static final double CUB_INV_COUNT_3 = 0.015625;
    private static final double QUAD_DEC_ERR_SUBPIX = (double)MarlinProperties.getQuadDecD2() * ((double)SUBPIXEL_POSITIONS_X / 8.0);
    public static final double QUAD_DEC_BND = 8.0 * QUAD_DEC_ERR_SUBPIX;
    private int[] crossings;
    private int[] aux_crossings;
    private int edgeCount;
    private int[] edgePtrs;
    private int[] aux_edgePtrs;
    private int activeEdgeMaxUsed;
    private final ArrayCacheInt.Reference crossings_ref;
    private final ArrayCacheInt.Reference edgePtrs_ref;
    private final ArrayCacheInt.Reference aux_crossings_ref;
    private final ArrayCacheInt.Reference aux_edgePtrs_ref;
    private int edgeMinY = Integer.MAX_VALUE;
    private int edgeMaxY = Integer.MIN_VALUE;
    private double edgeMinX = Double.POSITIVE_INFINITY;
    private double edgeMaxX = Double.NEGATIVE_INFINITY;
    private final OffHeapArray edges;
    private int[] edgeBuckets;
    private int[] edgeBucketCounts;
    private int buckets_minY;
    private int buckets_maxY;
    private final ArrayCacheIntClean.Reference edgeBuckets_ref;
    private final ArrayCacheIntClean.Reference edgeBucketCounts_ref;
    final MarlinCache cache;
    private int boundsMinX;
    private int boundsMinY;
    private int boundsMaxX;
    private int boundsMaxY;
    private int windingRule;
    private double x0;
    private double y0;
    private double sx0;
    private double sy0;
    final RendererContext rdrCtx;
    private final Curve curve;
    private int[] alphaLine;
    private final ArrayCacheIntClean.Reference alphaLine_ref;
    private boolean enableBlkFlags = false;
    private boolean prevUseBlkFlags = false;
    private int[] blkFlags;
    private final ArrayCacheIntClean.Reference blkFlags_ref;
    private int bbox_spminX;
    private int bbox_spmaxX;
    private int bbox_spminY;
    private int bbox_spmaxY;

    private void quadBreakIntoLinesAndAdd(double x0, double y0, Curve c, double x2, double y2) {
        int count = 1;
        double maxDD = Math.abs(c.dbx) + Math.abs(c.dby) * SCALE_DY;
        double _DEC_BND = QUAD_DEC_BND;
        while (maxDD >= _DEC_BND) {
            maxDD /= 4.0;
            count <<= 1;
            if (!DO_STATS) continue;
            this.rdrCtx.stats.stat_rdr_quadBreak_dec.add(count);
        }
        int nL = count;
        if (count > 1) {
            double icount = 1.0 / (double)count;
            double icount2 = icount * icount;
            double ddx = c.dbx * icount2;
            double ddy = c.dby * icount2;
            double dx = c.bx * icount2 + c.cx * icount;
            double dy = c.by * icount2 + c.cy * icount;
            double x1 = x0;
            double y1 = y0;
            while (--count > 0) {
                this.addLine(x0, y0, x1 += dx, y1 += dy);
                x0 = x1;
                y0 = y1;
                dx += ddx;
                dy += ddy;
            }
        }
        this.addLine(x0, y0, x2, y2);
        if (DO_STATS) {
            this.rdrCtx.stats.stat_rdr_quadBreak.add(nL);
        }
    }

    private void curveBreakIntoLinesAndAdd(double x0, double y0, Curve c, double x3, double y3) {
        int count = 4;
        double icount = 0.25;
        double icount2 = 0.0625;
        double icount3 = 0.015625;
        double dddx = 2.0 * c.dax * 0.015625;
        double dddy = 2.0 * c.day * 0.015625;
        double ddx = dddx + c.dbx * 0.0625;
        double ddy = dddy + c.dby * 0.0625;
        double dx = c.ax * 0.015625 + c.bx * 0.0625 + c.cx * 0.25;
        double dy = c.ay * 0.015625 + c.by * 0.0625 + c.cy * 0.25;
        int nL = 0;
        double _DEC_BND = CUB_DEC_BND;
        double _INC_BND = CUB_INC_BND;
        double _SCALE_DY = SCALE_DY;
        double x1 = x0;
        double y1 = y0;
        while (count > 0) {
            while (count % 2 == 0 && Math.abs(ddx) + Math.abs(ddy) * _SCALE_DY <= _INC_BND) {
                dx = 2.0 * dx + ddx;
                dy = 2.0 * dy + ddy;
                ddx = 4.0 * (ddx + dddx);
                ddy = 4.0 * (ddy + dddy);
                dddx *= 8.0;
                dddy *= 8.0;
                count >>= 1;
                if (!DO_STATS) continue;
                this.rdrCtx.stats.stat_rdr_curveBreak_inc.add(count);
            }
            while (Math.abs(ddx) + Math.abs(ddy) * _SCALE_DY >= _DEC_BND) {
                ddx = ddx / 4.0 - (dddx /= 8.0);
                ddy = ddy / 4.0 - (dddy /= 8.0);
                dx = (dx - ddx) / 2.0;
                dy = (dy - ddy) / 2.0;
                count <<= 1;
                if (!DO_STATS) continue;
                this.rdrCtx.stats.stat_rdr_curveBreak_dec.add(count);
            }
            if (--count == 0) break;
            ddx += dddx;
            this.addLine(x0, y0, x1 += (dx += ddx), y1 += (dy += (ddy += dddy)));
            x0 = x1;
            y0 = y1;
        }
        this.addLine(x0, y0, x3, y3);
        if (DO_STATS) {
            this.rdrCtx.stats.stat_rdr_curveBreak.add(nL + 1);
        }
    }

    private void addLine(double x1, double y1, double x2, double y2) {
        double slope;
        int lastCrossing;
        int firstCrossing;
        if (DO_STATS) {
            this.rdrCtx.stats.stat_rdr_addLine.add(1);
        }
        int or = 1;
        if (y2 < y1) {
            or = 0;
            double tmp = y2;
            y2 = y1;
            y1 = tmp;
            tmp = x2;
            x2 = x1;
            x1 = tmp;
        }
        if ((firstCrossing = FloatMath.max(FloatMath.ceil_int(y1), this.boundsMinY)) >= (lastCrossing = FloatMath.min(FloatMath.ceil_int(y2), this.boundsMaxY))) {
            if (DO_STATS) {
                this.rdrCtx.stats.stat_rdr_addLine_skip.add(1);
            }
            return;
        }
        if (firstCrossing < this.edgeMinY) {
            this.edgeMinY = firstCrossing;
        }
        if (lastCrossing > this.edgeMaxY) {
            this.edgeMaxY = lastCrossing;
        }
        if ((slope = (x1 - x2) / (y1 - y2)) >= 0.0) {
            if (x1 < this.edgeMinX) {
                this.edgeMinX = x1;
            }
            if (x2 > this.edgeMaxX) {
                this.edgeMaxX = x2;
            }
        } else {
            if (x2 < this.edgeMinX) {
                this.edgeMinX = x2;
            }
            if (x1 > this.edgeMaxX) {
                this.edgeMaxX = x1;
            }
        }
        int _SIZEOF_EDGE_BYTES = SIZEOF_EDGE_BYTES;
        OffHeapArray _edges = this.edges;
        int edgePtr = _edges.used;
        if (_edges.length - (long)edgePtr < (long)_SIZEOF_EDGE_BYTES) {
            long edgeNewSize = ArrayCacheConst.getNewLargeSize(_edges.length, edgePtr + _SIZEOF_EDGE_BYTES);
            if (DO_STATS) {
                this.rdrCtx.stats.stat_rdr_edges_resizes.add(edgeNewSize);
            }
            _edges.resize(edgeNewSize);
        }
        Unsafe _unsafe = OffHeapArray.UNSAFE;
        long SIZE_INT = 4L;
        long addr = _edges.address + (long)edgePtr;
        double x1_intercept = x1 + ((double)firstCrossing - y1) * slope;
        long x1_fixed_biased = (long)(4.294967296E9 * x1_intercept) + Integer.MAX_VALUE;
        _unsafe.putInt(addr, (int)(x1_fixed_biased >> 31) & 0xFFFFFFFE | or);
        _unsafe.putInt(addr += 4L, (int)x1_fixed_biased >>> 1);
        long slope_fixed = (long)(4.294967296E9 * slope);
        _unsafe.putInt(addr += 4L, (int)(slope_fixed >> 31) & 0xFFFFFFFE);
        _unsafe.putInt(addr += 4L, (int)slope_fixed >>> 1);
        int[] _edgeBuckets = this.edgeBuckets;
        int[] _edgeBucketCounts = this.edgeBucketCounts;
        int _boundsMinY = this.boundsMinY;
        int bucketIdx = firstCrossing - _boundsMinY;
        _unsafe.putInt(addr += 4L, _edgeBuckets[bucketIdx]);
        _unsafe.putInt(addr += 4L, lastCrossing);
        _edgeBuckets[bucketIdx] = edgePtr;
        int n = bucketIdx;
        _edgeBucketCounts[n] = _edgeBucketCounts[n] + 2;
        int n2 = lastCrossing - _boundsMinY;
        _edgeBucketCounts[n2] = _edgeBucketCounts[n2] | 1;
        _edges.used += _SIZEOF_EDGE_BYTES;
    }

    Renderer(RendererContext rdrCtx) {
        this.rdrCtx = rdrCtx;
        this.curve = rdrCtx.curve;
        this.cache = rdrCtx.cache;
        this.edges = rdrCtx.newOffHeapArray(INITIAL_EDGES_CAPACITY);
        this.edgeBuckets_ref = rdrCtx.newCleanIntArrayRef(INITIAL_BUCKET_ARRAY);
        this.edgeBucketCounts_ref = rdrCtx.newCleanIntArrayRef(INITIAL_BUCKET_ARRAY);
        this.edgeBuckets = this.edgeBuckets_ref.initial;
        this.edgeBucketCounts = this.edgeBucketCounts_ref.initial;
        this.alphaLine_ref = rdrCtx.newCleanIntArrayRef(INITIAL_AA_ARRAY);
        this.alphaLine = this.alphaLine_ref.initial;
        this.crossings_ref = rdrCtx.newDirtyIntArrayRef(INITIAL_CROSSING_COUNT);
        this.aux_crossings_ref = rdrCtx.newDirtyIntArrayRef(INITIAL_CROSSING_COUNT);
        this.edgePtrs_ref = rdrCtx.newDirtyIntArrayRef(INITIAL_CROSSING_COUNT);
        this.aux_edgePtrs_ref = rdrCtx.newDirtyIntArrayRef(INITIAL_CROSSING_COUNT);
        this.crossings = this.crossings_ref.initial;
        this.aux_crossings = this.aux_crossings_ref.initial;
        this.edgePtrs = this.edgePtrs_ref.initial;
        this.aux_edgePtrs = this.aux_edgePtrs_ref.initial;
        this.blkFlags_ref = rdrCtx.newCleanIntArrayRef(256);
        this.blkFlags = this.blkFlags_ref.initial;
    }

    Renderer init(int pix_boundsX, int pix_boundsY, int pix_boundsWidth, int pix_boundsHeight, int windingRule) {
        int edgeBucketsLength;
        this.rdrCtx.doRender = true;
        this.windingRule = windingRule;
        this.boundsMinX = pix_boundsX << SUBPIXEL_LG_POSITIONS_X;
        this.boundsMaxX = pix_boundsX + pix_boundsWidth << SUBPIXEL_LG_POSITIONS_X;
        this.boundsMinY = pix_boundsY << SUBPIXEL_LG_POSITIONS_Y;
        this.boundsMaxY = pix_boundsY + pix_boundsHeight << SUBPIXEL_LG_POSITIONS_Y;
        if (DO_LOG_BOUNDS) {
            MarlinUtils.logInfo("boundsXY = [" + this.boundsMinX + " ... " + this.boundsMaxX + "[ [" + this.boundsMinY + " ... " + this.boundsMaxY + "[");
        }
        if ((edgeBucketsLength = this.boundsMaxY - this.boundsMinY + 1) > INITIAL_BUCKET_ARRAY) {
            if (DO_STATS) {
                this.rdrCtx.stats.stat_array_renderer_edgeBuckets.add(edgeBucketsLength);
                this.rdrCtx.stats.stat_array_renderer_edgeBucketCounts.add(edgeBucketsLength);
            }
            this.edgeBuckets = this.edgeBuckets_ref.getArray(edgeBucketsLength);
            this.edgeBucketCounts = this.edgeBucketCounts_ref.getArray(edgeBucketsLength);
        }
        this.edgeMinY = Integer.MAX_VALUE;
        this.edgeMaxY = Integer.MIN_VALUE;
        this.edgeMinX = Double.POSITIVE_INFINITY;
        this.edgeMaxX = Double.NEGATIVE_INFINITY;
        this.edgeCount = 0;
        this.activeEdgeMaxUsed = 0;
        this.edges.used = 0;
        return this;
    }

    void dispose() {
        if (DO_STATS) {
            this.rdrCtx.stats.stat_rdr_activeEdges.add(this.activeEdgeMaxUsed);
            this.rdrCtx.stats.stat_rdr_edges.add(this.edges.used);
            this.rdrCtx.stats.stat_rdr_edges_count.add(this.edges.used / SIZEOF_EDGE_BYTES);
            this.rdrCtx.stats.hist_rdr_edges_count.add(this.edges.used / SIZEOF_EDGE_BYTES);
            this.rdrCtx.stats.totalOffHeap += this.edges.length;
        }
        if (this.crossings_ref.doCleanRef(this.crossings)) {
            this.crossings = this.crossings_ref.putArray(this.crossings);
        }
        if (this.aux_crossings_ref.doCleanRef(this.aux_crossings)) {
            this.aux_crossings = this.aux_crossings_ref.putArray(this.aux_crossings);
        }
        if (this.edgePtrs_ref.doCleanRef(this.edgePtrs)) {
            this.edgePtrs = this.edgePtrs_ref.putArray(this.edgePtrs);
        }
        if (this.aux_edgePtrs_ref.doCleanRef(this.aux_edgePtrs)) {
            this.aux_edgePtrs = this.aux_edgePtrs_ref.putArray(this.aux_edgePtrs);
        }
        if (this.alphaLine_ref.doSetRef(this.alphaLine)) {
            this.alphaLine = this.alphaLine_ref.putArrayClean(this.alphaLine);
        }
        if (this.blkFlags_ref.doSetRef(this.blkFlags)) {
            this.blkFlags = this.blkFlags_ref.putArrayClean(this.blkFlags);
        }
        if (this.edgeMinY != Integer.MAX_VALUE) {
            if (this.rdrCtx.dirty) {
                this.buckets_minY = 0;
                this.buckets_maxY = this.boundsMaxY - this.boundsMinY;
            }
            this.edgeBuckets = this.edgeBuckets_ref.putArray(this.edgeBuckets, this.buckets_minY, this.buckets_maxY);
            this.edgeBucketCounts = this.edgeBucketCounts_ref.putArray(this.edgeBucketCounts, this.buckets_minY, this.buckets_maxY + 1);
        } else {
            if (this.edgeBuckets_ref.doSetRef(this.edgeBuckets)) {
                this.edgeBuckets = this.edgeBuckets_ref.putArrayClean(this.edgeBuckets);
            }
            if (this.edgeBucketCounts_ref.doSetRef(this.edgeBucketCounts)) {
                this.edgeBucketCounts = this.edgeBucketCounts_ref.putArrayClean(this.edgeBucketCounts);
            }
        }
        if (this.edges.length != (long)INITIAL_EDGES_CAPACITY) {
            this.edges.resize(INITIAL_EDGES_CAPACITY);
        }
        DMarlinRenderingEngine.returnRendererContext(this.rdrCtx);
    }

    private static double tosubpixx(double pix_x) {
        return SUBPIXEL_SCALE_X * pix_x;
    }

    private static double tosubpixy(double pix_y) {
        return SUBPIXEL_SCALE_Y * pix_y - 0.5;
    }

    @Override
    public void moveTo(double pix_x0, double pix_y0) {
        this.closePath();
        double sx = Renderer.tosubpixx(pix_x0);
        double sy = Renderer.tosubpixy(pix_y0);
        this.sx0 = sx;
        this.sy0 = sy;
        this.x0 = sx;
        this.y0 = sy;
    }

    @Override
    public void lineTo(double pix_x1, double pix_y1) {
        double x1 = Renderer.tosubpixx(pix_x1);
        double y1 = Renderer.tosubpixy(pix_y1);
        this.addLine(this.x0, this.y0, x1, y1);
        this.x0 = x1;
        this.y0 = y1;
    }

    @Override
    public void curveTo(double pix_x1, double pix_y1, double pix_x2, double pix_y2, double pix_x3, double pix_y3) {
        double xe = Renderer.tosubpixx(pix_x3);
        double ye = Renderer.tosubpixy(pix_y3);
        this.curve.set(this.x0, this.y0, Renderer.tosubpixx(pix_x1), Renderer.tosubpixy(pix_y1), Renderer.tosubpixx(pix_x2), Renderer.tosubpixy(pix_y2), xe, ye);
        this.curveBreakIntoLinesAndAdd(this.x0, this.y0, this.curve, xe, ye);
        this.x0 = xe;
        this.y0 = ye;
    }

    @Override
    public void quadTo(double pix_x1, double pix_y1, double pix_x2, double pix_y2) {
        double xe = Renderer.tosubpixx(pix_x2);
        double ye = Renderer.tosubpixy(pix_y2);
        this.curve.set(this.x0, this.y0, Renderer.tosubpixx(pix_x1), Renderer.tosubpixy(pix_y1), xe, ye);
        this.quadBreakIntoLinesAndAdd(this.x0, this.y0, this.curve, xe, ye);
        this.x0 = xe;
        this.y0 = ye;
    }

    @Override
    public void closePath() {
        if (this.x0 != this.sx0 || this.y0 != this.sy0) {
            this.addLine(this.x0, this.y0, this.sx0, this.sy0);
            this.x0 = this.sx0;
            this.y0 = this.sy0;
        }
    }

    @Override
    public void pathDone() {
        this.closePath();
    }

    @Override
    public long getNativeConsumer() {
        throw new InternalError("Renderer does not use a native consumer.");
    }

    private void _endRendering(int ymin, int ymax) {
        if (DISABLE_RENDER) {
            return;
        }
        int bboxx0 = this.bbox_spminX;
        int bboxx1 = this.bbox_spmaxX;
        boolean windingRuleEvenOdd = this.windingRule == 0;
        int[] _alpha = this.alphaLine;
        MarlinCache _cache = this.cache;
        OffHeapArray _edges = this.edges;
        int[] _edgeBuckets = this.edgeBuckets;
        int[] _edgeBucketCounts = this.edgeBucketCounts;
        int[] _crossings = this.crossings;
        int[] _edgePtrs = this.edgePtrs;
        int[] _aux_crossings = this.aux_crossings;
        int[] _aux_edgePtrs = this.aux_edgePtrs;
        long _OFF_ERROR = OFF_ERROR;
        long _OFF_BUMP_X = OFF_BUMP_X;
        long _OFF_BUMP_ERR = OFF_BUMP_ERR;
        long _OFF_NEXT = OFF_NEXT;
        long _OFF_YMAX = OFF_YMAX;
        int _ALL_BUT_LSB = -2;
        int _ERR_STEP_MAX = Integer.MAX_VALUE;
        Unsafe _unsafe = OffHeapArray.UNSAFE;
        long addr0 = _edges.address;
        int _SUBPIXEL_LG_POSITIONS_X = SUBPIXEL_LG_POSITIONS_X;
        int _SUBPIXEL_LG_POSITIONS_Y = SUBPIXEL_LG_POSITIONS_Y;
        int _SUBPIXEL_MASK_X = SUBPIXEL_MASK_X;
        int _SUBPIXEL_MASK_Y = SUBPIXEL_MASK_Y;
        int _SUBPIXEL_POSITIONS_X = SUBPIXEL_POSITIONS_X;
        int _MIN_VALUE = Integer.MIN_VALUE;
        int _MAX_VALUE = Integer.MAX_VALUE;
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int y = ymin;
        int bucket = y - this.boundsMinY;
        int numCrossings = this.edgeCount;
        int edgePtrsLen = _edgePtrs.length;
        int crossingsLen = _crossings.length;
        int _arrayMaxUsed = this.activeEdgeMaxUsed;
        int ptrLen = 0;
        int[] _blkFlags = this.blkFlags;
        int _BLK_SIZE_LG = BLOCK_SIZE_LG;
        int _BLK_SIZE = BLOCK_SIZE;
        boolean _enableBlkFlagsHeuristics = ENABLE_BLOCK_FLAGS_HEURISTICS && this.enableBlkFlags;
        boolean useBlkFlags = this.prevUseBlkFlags;
        int stroking = this.rdrCtx.stroking;
        int lastY = -1;
        DPQSSorterContext sorter = this.rdrCtx.sorterCtx;
        while (y < ymax) {
            int tmp;
            int ecur;
            int i;
            long addr;
            int bucketcount = _edgeBucketCounts[bucket];
            int prevNumCrossings = numCrossings;
            if (bucketcount != 0) {
                if (DO_STATS) {
                    this.rdrCtx.stats.stat_rdr_activeEdges_updates.add(prevNumCrossings);
                }
                if ((bucketcount & 1) != 0) {
                    addr = addr0 + _OFF_YMAX;
                    int newCount = 0;
                    for (i = 0; i < prevNumCrossings; ++i) {
                        ecur = _edgePtrs[i];
                        if (_unsafe.getInt(addr + (long)ecur) <= y) continue;
                        _edgePtrs[newCount++] = ecur;
                    }
                    prevNumCrossings = numCrossings = newCount;
                }
                if ((ptrLen = bucketcount >> 1) != 0) {
                    int ptrEnd;
                    if (DO_STATS) {
                        this.rdrCtx.stats.stat_rdr_activeEdges_adds.add(ptrLen);
                        if (ptrLen > 10) {
                            this.rdrCtx.stats.stat_rdr_activeEdges_adds_high.add(ptrLen);
                        }
                    }
                    if (edgePtrsLen < (ptrEnd = numCrossings + ptrLen)) {
                        if (DO_STATS) {
                            this.rdrCtx.stats.stat_array_renderer_edgePtrs.add(ptrEnd);
                        }
                        this.edgePtrs = _edgePtrs = this.edgePtrs_ref.widenArray(_edgePtrs, edgePtrsLen, ptrEnd);
                        edgePtrsLen = _edgePtrs.length;
                        this.aux_edgePtrs_ref.putArray(_aux_edgePtrs);
                        if (DO_STATS) {
                            this.rdrCtx.stats.stat_array_renderer_aux_edgePtrs.add(ptrEnd);
                        }
                        this.aux_edgePtrs = _aux_edgePtrs = this.aux_edgePtrs_ref.getArray(ArrayCacheConst.getNewSize(numCrossings, ptrEnd));
                    }
                    addr = addr0 + _OFF_NEXT;
                    ecur = _edgeBuckets[bucket];
                    while (numCrossings < ptrEnd) {
                        _edgePtrs[numCrossings] = ecur;
                        ecur = _unsafe.getInt(addr + (long)ecur);
                        ++numCrossings;
                    }
                    if (crossingsLen < numCrossings) {
                        this.crossings_ref.putArray(_crossings);
                        if (DO_STATS) {
                            this.rdrCtx.stats.stat_array_renderer_crossings.add(numCrossings);
                        }
                        this.crossings = _crossings = this.crossings_ref.getArray(numCrossings);
                        this.aux_crossings_ref.putArray(_aux_crossings);
                        if (DO_STATS) {
                            this.rdrCtx.stats.stat_array_renderer_aux_crossings.add(numCrossings);
                        }
                        this.aux_crossings = _aux_crossings = this.aux_crossings_ref.getArray(numCrossings);
                        crossingsLen = _crossings.length;
                    }
                    if (DO_STATS && numCrossings > _arrayMaxUsed) {
                        _arrayMaxUsed = numCrossings;
                    }
                }
            }
            if (numCrossings != 0) {
                int x1;
                int curx;
                if (numCrossings <= 40 || ptrLen <= 10 && numCrossings <= 1000) {
                    if (DO_STATS) {
                        this.rdrCtx.stats.hist_rdr_crossings.add(numCrossings);
                        this.rdrCtx.stats.hist_rdr_crossings_adds.add(ptrLen);
                    }
                    boolean useBinarySearch = numCrossings >= 20;
                    lastCross = Integer.MIN_VALUE;
                    for (i = 0; i < numCrossings; ++i) {
                        ecur = _edgePtrs[i];
                        addr = addr0 + (long)ecur;
                        cross = curx = _unsafe.getInt(addr);
                        err = _unsafe.getInt(addr + _OFF_ERROR) + _unsafe.getInt(addr + _OFF_BUMP_ERR);
                        _unsafe.putInt(addr, (curx += _unsafe.getInt(addr + _OFF_BUMP_X)) - (err >> 30 & 0xFFFFFFFE));
                        _unsafe.putInt(addr + _OFF_ERROR, err & Integer.MAX_VALUE);
                        if (DO_STATS) {
                            this.rdrCtx.stats.stat_rdr_crossings_updates.add(numCrossings);
                        }
                        if (cross < lastCross) {
                            if (DO_STATS) {
                                this.rdrCtx.stats.stat_rdr_crossings_sorts.add(i);
                            }
                            if (useBinarySearch && i >= prevNumCrossings) {
                                if (DO_STATS) {
                                    this.rdrCtx.stats.stat_rdr_crossings_bsearch.add(i);
                                }
                                int low = 0;
                                int high = i - 1;
                                do {
                                    int mid;
                                    if (_crossings[mid = low + high >> 1] < cross) {
                                        low = mid + 1;
                                        continue;
                                    }
                                    high = mid - 1;
                                } while (low <= high);
                                for (j = i - 1; j >= low; --j) {
                                    _crossings[j + 1] = _crossings[j];
                                    _edgePtrs[j + 1] = _edgePtrs[j];
                                }
                                _crossings[low] = cross;
                                _edgePtrs[low] = ecur;
                                continue;
                            }
                            j = i - 1;
                            _crossings[i] = _crossings[j];
                            _edgePtrs[i] = _edgePtrs[j];
                            while (--j >= 0 && _crossings[j] > cross) {
                                _crossings[j + 1] = _crossings[j];
                                _edgePtrs[j + 1] = _edgePtrs[j];
                            }
                            _crossings[j + 1] = cross;
                            _edgePtrs[j + 1] = ecur;
                            continue;
                        }
                        _crossings[i] = lastCross = cross;
                    }
                } else {
                    boolean useDPQS;
                    if (DO_STATS) {
                        this.rdrCtx.stats.stat_rdr_crossings_msorts.add(numCrossings);
                        this.rdrCtx.stats.hist_rdr_crossings_ratio.add(1000 * ptrLen / numCrossings);
                        this.rdrCtx.stats.hist_rdr_crossings_msorts.add(numCrossings);
                        this.rdrCtx.stats.hist_rdr_crossings_msorts_adds.add(ptrLen);
                    }
                    boolean skipISort = prevNumCrossings >= 1000;
                    boolean bl = useDPQS = MergeSort.USE_DPQS && (skipISort || ptrLen >= 256);
                    if (DO_STATS && useDPQS) {
                        this.rdrCtx.stats.stat_rdr_crossings_dpqs.add(skipISort ? numCrossings : ptrLen);
                    }
                    lastCross = Integer.MIN_VALUE;
                    for (i = 0; i < numCrossings; ++i) {
                        ecur = _edgePtrs[i];
                        addr = addr0 + (long)ecur;
                        cross = curx = _unsafe.getInt(addr);
                        err = _unsafe.getInt(addr + _OFF_ERROR) + _unsafe.getInt(addr + _OFF_BUMP_ERR);
                        _unsafe.putInt(addr, (curx += _unsafe.getInt(addr + _OFF_BUMP_X)) - (err >> 30 & 0xFFFFFFFE));
                        _unsafe.putInt(addr + _OFF_ERROR, err & Integer.MAX_VALUE);
                        if (DO_STATS) {
                            this.rdrCtx.stats.stat_rdr_crossings_updates.add(numCrossings);
                        }
                        if (skipISort) {
                            if (useDPQS) {
                                _crossings[i] = cross;
                                continue;
                            }
                            _aux_crossings[i] = cross;
                            _aux_edgePtrs[i] = ecur;
                            continue;
                        }
                        if (i >= prevNumCrossings) {
                            if (useDPQS) {
                                _aux_crossings[i] = cross;
                                _aux_edgePtrs[i] = ecur;
                                continue;
                            }
                            _crossings[i] = cross;
                            continue;
                        }
                        if (cross < lastCross) {
                            if (DO_STATS) {
                                this.rdrCtx.stats.stat_rdr_crossings_sorts.add(i);
                            }
                            j = i - 1;
                            _aux_crossings[i] = _aux_crossings[j];
                            _aux_edgePtrs[i] = _aux_edgePtrs[j];
                            while (--j >= 0 && _aux_crossings[j] > cross) {
                                _aux_crossings[j + 1] = _aux_crossings[j];
                                _aux_edgePtrs[j + 1] = _aux_edgePtrs[j];
                            }
                            _aux_crossings[j + 1] = cross;
                            _aux_edgePtrs[j + 1] = ecur;
                            continue;
                        }
                        _aux_crossings[i] = lastCross = cross;
                        _aux_edgePtrs[i] = ecur;
                    }
                    MergeSort.mergeSortNoCopy(_crossings, _edgePtrs, _aux_crossings, _aux_edgePtrs, numCrossings, prevNumCrossings, skipISort, sorter, useDPQS);
                }
                ptrLen = 0;
                int curxo = _crossings[0];
                int x0 = curxo >> 1;
                if (x0 < minX) {
                    minX = x0;
                }
                if ((x1 = _crossings[numCrossings - 1] >> 1) > maxX) {
                    maxX = x1;
                }
                int prev = curx = x0;
                int crorientation = ((curxo & 1) << 1) - 1;
                if (windingRuleEvenOdd) {
                    sum = crorientation;
                    for (i = 1; i < numCrossings; ++i) {
                        curxo = _crossings[i];
                        curx = curxo >> 1;
                        crorientation = ((curxo & 1) << 1) - 1;
                        if ((sum & 1) != 0) {
                            int n = x0 = prev > bboxx0 ? prev : bboxx0;
                            if (curx < bboxx1) {
                                x1 = curx;
                            } else {
                                x1 = bboxx1;
                                i = numCrossings;
                            }
                            if (x0 < x1) {
                                pix_x = (x0 -= bboxx0) >> _SUBPIXEL_LG_POSITIONS_X;
                                pix_xmaxm1 = (x1 -= bboxx0) - 1 >> _SUBPIXEL_LG_POSITIONS_X;
                                if (pix_x == pix_xmaxm1) {
                                    tmp = x1 - x0;
                                    int n2 = pix_x;
                                    _alpha[n2] = _alpha[n2] + tmp;
                                    int n3 = pix_x + 1;
                                    _alpha[n3] = _alpha[n3] - tmp;
                                    if (useBlkFlags) {
                                        _blkFlags[pix_x >> _BLK_SIZE_LG] = 1;
                                    }
                                } else {
                                    tmp = x0 & _SUBPIXEL_MASK_X;
                                    int n4 = pix_x;
                                    _alpha[n4] = _alpha[n4] + (_SUBPIXEL_POSITIONS_X - tmp);
                                    int n5 = pix_x + 1;
                                    _alpha[n5] = _alpha[n5] + tmp;
                                    pix_xmax = x1 >> _SUBPIXEL_LG_POSITIONS_X;
                                    tmp = x1 & _SUBPIXEL_MASK_X;
                                    int n6 = pix_xmax;
                                    _alpha[n6] = _alpha[n6] - (_SUBPIXEL_POSITIONS_X - tmp);
                                    int n7 = pix_xmax + 1;
                                    _alpha[n7] = _alpha[n7] - tmp;
                                    if (useBlkFlags) {
                                        _blkFlags[pix_x >> _BLK_SIZE_LG] = 1;
                                        _blkFlags[pix_xmax >> _BLK_SIZE_LG] = 1;
                                    }
                                }
                            }
                        }
                        sum += crorientation;
                        prev = curx;
                    }
                } else {
                    i = 1;
                    sum = 0;
                    while (true) {
                        if ((sum += crorientation) != 0) {
                            if (prev > curx) {
                                prev = curx;
                            }
                        } else {
                            int n = x0 = prev > bboxx0 ? prev : bboxx0;
                            if (curx < bboxx1) {
                                x1 = curx;
                            } else {
                                x1 = bboxx1;
                                i = numCrossings;
                            }
                            if (x0 < x1) {
                                pix_x = (x0 -= bboxx0) >> _SUBPIXEL_LG_POSITIONS_X;
                                pix_xmaxm1 = (x1 -= bboxx0) - 1 >> _SUBPIXEL_LG_POSITIONS_X;
                                if (pix_x == pix_xmaxm1) {
                                    tmp = x1 - x0;
                                    int n8 = pix_x;
                                    _alpha[n8] = _alpha[n8] + tmp;
                                    int n9 = pix_x + 1;
                                    _alpha[n9] = _alpha[n9] - tmp;
                                    if (useBlkFlags) {
                                        _blkFlags[pix_x >> _BLK_SIZE_LG] = 1;
                                    }
                                } else {
                                    tmp = x0 & _SUBPIXEL_MASK_X;
                                    int n10 = pix_x;
                                    _alpha[n10] = _alpha[n10] + (_SUBPIXEL_POSITIONS_X - tmp);
                                    int n11 = pix_x + 1;
                                    _alpha[n11] = _alpha[n11] + tmp;
                                    pix_xmax = x1 >> _SUBPIXEL_LG_POSITIONS_X;
                                    tmp = x1 & _SUBPIXEL_MASK_X;
                                    int n12 = pix_xmax;
                                    _alpha[n12] = _alpha[n12] - (_SUBPIXEL_POSITIONS_X - tmp);
                                    int n13 = pix_xmax + 1;
                                    _alpha[n13] = _alpha[n13] - tmp;
                                    if (useBlkFlags) {
                                        _blkFlags[pix_x >> _BLK_SIZE_LG] = 1;
                                        _blkFlags[pix_xmax >> _BLK_SIZE_LG] = 1;
                                    }
                                }
                            }
                            prev = Integer.MAX_VALUE;
                        }
                        if (i == numCrossings) break;
                        curxo = _crossings[i];
                        curx = curxo >> 1;
                        crorientation = ((curxo & 1) << 1) - 1;
                        ++i;
                    }
                }
            }
            if ((y & _SUBPIXEL_MASK_Y) == _SUBPIXEL_MASK_Y) {
                lastY = y >> _SUBPIXEL_LG_POSITIONS_Y;
                minX = FloatMath.max(minX, bboxx0) >> _SUBPIXEL_LG_POSITIONS_X;
                if ((maxX = FloatMath.min(maxX, bboxx1) >> _SUBPIXEL_LG_POSITIONS_X) >= minX) {
                    this.copyAARow(_alpha, lastY, minX, maxX + 1, useBlkFlags);
                    if (_enableBlkFlagsHeuristics) {
                        boolean bl = useBlkFlags = (maxX -= minX) > _BLK_SIZE && maxX > (numCrossings >> stroking) - 1 << _BLK_SIZE_LG;
                        if (DO_STATS) {
                            tmp = FloatMath.max(1, (numCrossings >> stroking) - 1);
                            this.rdrCtx.stats.hist_tile_generator_encoding_dist.add(maxX / tmp);
                        }
                    }
                } else {
                    _cache.clearAARow(lastY);
                }
                minX = Integer.MAX_VALUE;
                maxX = Integer.MIN_VALUE;
            }
            ++y;
            ++bucket;
        }
        --y;
        y >>= _SUBPIXEL_LG_POSITIONS_Y;
        minX = FloatMath.max(minX, bboxx0) >> _SUBPIXEL_LG_POSITIONS_X;
        if ((maxX = FloatMath.min(maxX, bboxx1) >> _SUBPIXEL_LG_POSITIONS_X) >= minX) {
            this.copyAARow(_alpha, y, minX, maxX + 1, useBlkFlags);
        } else if (y != lastY) {
            _cache.clearAARow(y);
        }
        this.edgeCount = numCrossings;
        this.prevUseBlkFlags = useBlkFlags;
        if (DO_STATS) {
            this.activeEdgeMaxUsed = _arrayMaxUsed;
        }
    }

    boolean endRendering() {
        int width;
        if (this.edgeMinY == Integer.MAX_VALUE) {
            return false;
        }
        int spminX = FloatMath.max(FloatMath.ceil_int(this.edgeMinX - 0.5), this.boundsMinX);
        int spmaxX = FloatMath.min(FloatMath.ceil_int(this.edgeMaxX - 0.5), this.boundsMaxX);
        int spminY = this.edgeMinY;
        int spmaxY = this.edgeMaxY;
        this.buckets_minY = spminY - this.boundsMinY;
        this.buckets_maxY = spmaxY - this.boundsMinY;
        if (DO_LOG_BOUNDS) {
            MarlinUtils.logInfo("edgesXY = [" + this.edgeMinX + " ... " + this.edgeMaxX + "[ [" + this.edgeMinY + " ... " + this.edgeMaxY + "[");
            MarlinUtils.logInfo("spXY    = [" + spminX + " ... " + spmaxX + "[ [" + spminY + " ... " + spmaxY + "[");
        }
        if (spminX >= spmaxX || spminY >= spmaxY) {
            return false;
        }
        int pminX = spminX >> SUBPIXEL_LG_POSITIONS_X;
        int pmaxX = spmaxX + SUBPIXEL_MASK_X >> SUBPIXEL_LG_POSITIONS_X;
        int pminY = spminY >> SUBPIXEL_LG_POSITIONS_Y;
        int pmaxY = spmaxY + SUBPIXEL_MASK_Y >> SUBPIXEL_LG_POSITIONS_Y;
        this.cache.init(pminX, pminY, pmaxX, pmaxY);
        if (ENABLE_BLOCK_FLAGS) {
            int blkLen;
            this.enableBlkFlags = this.cache.useRLE;
            boolean bl = this.prevUseBlkFlags = this.enableBlkFlags && !ENABLE_BLOCK_FLAGS_HEURISTICS;
            if (this.enableBlkFlags && (blkLen = (pmaxX - pminX >> BLOCK_SIZE_LG) + 2) > 256) {
                this.blkFlags = this.blkFlags_ref.getArray(blkLen);
            }
        }
        this.bbox_spminX = pminX << SUBPIXEL_LG_POSITIONS_X;
        this.bbox_spmaxX = pmaxX << SUBPIXEL_LG_POSITIONS_X;
        this.bbox_spminY = spminY;
        this.bbox_spmaxY = spmaxY;
        if (DO_LOG_BOUNDS) {
            MarlinUtils.logInfo("pXY       = [" + pminX + " ... " + pmaxX + "[ [" + pminY + " ... " + pmaxY + "[");
            MarlinUtils.logInfo("bbox_spXY = [" + this.bbox_spminX + " ... " + this.bbox_spmaxX + "[ [" + this.bbox_spminY + " ... " + this.bbox_spmaxY + "[");
        }
        if ((width = pmaxX - pminX + 2) > INITIAL_AA_ARRAY) {
            if (DO_STATS) {
                this.rdrCtx.stats.stat_array_renderer_alphaline.add(width);
            }
            this.alphaLine = this.alphaLine_ref.getArray(width);
        }
        this.endRendering(pminY);
        return true;
    }

    void endRendering(int pminY) {
        int spminY = pminY << SUBPIXEL_LG_POSITIONS_Y;
        int fixed_spminY = FloatMath.max(this.bbox_spminY, spminY);
        if (fixed_spminY < this.bbox_spmaxY) {
            int spmaxY = FloatMath.min(this.bbox_spmaxY, spminY + SUBPIXEL_TILE);
            this.cache.resetTileLine(pminY);
            this._endRendering(fixed_spminY, spmaxY);
        }
    }

    void copyAARow(int[] alphaRow, int pix_y, int pix_from, int pix_to, boolean useBlockFlags) {
        if (useBlockFlags) {
            if (DO_STATS) {
                this.rdrCtx.stats.hist_tile_generator_encoding.add(1);
            }
            this.cache.copyAARowRLE_WithBlockFlags(this.blkFlags, alphaRow, pix_y, pix_from, pix_to);
        } else {
            if (DO_STATS) {
                this.rdrCtx.stats.hist_tile_generator_encoding.add(0);
            }
            this.cache.copyAARowNoRLE(alphaRow, pix_y, pix_from, pix_to);
        }
    }
}

