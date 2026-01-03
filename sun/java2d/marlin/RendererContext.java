/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import java.awt.geom.Path2D;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;
import sun.awt.geom.PathConsumer2D;
import sun.java2d.ReentrantContext;
import sun.java2d.marlin.ArrayCacheByte;
import sun.java2d.marlin.ArrayCacheConst;
import sun.java2d.marlin.ArrayCacheDouble;
import sun.java2d.marlin.ArrayCacheInt;
import sun.java2d.marlin.ArrayCacheIntClean;
import sun.java2d.marlin.CollinearSimplifier;
import sun.java2d.marlin.Curve;
import sun.java2d.marlin.DMarlinRenderingEngine;
import sun.java2d.marlin.DPQSSorterContext;
import sun.java2d.marlin.DPathConsumer2D;
import sun.java2d.marlin.Dasher;
import sun.java2d.marlin.MarlinCache;
import sun.java2d.marlin.MarlinConst;
import sun.java2d.marlin.MarlinTileGenerator;
import sun.java2d.marlin.MarlinUtils;
import sun.java2d.marlin.MergeSort;
import sun.java2d.marlin.OffHeapArray;
import sun.java2d.marlin.PathSimplifier;
import sun.java2d.marlin.Renderer;
import sun.java2d.marlin.RendererStats;
import sun.java2d.marlin.Stroker;
import sun.java2d.marlin.TransformingPathConsumer2D;

final class RendererContext
extends ReentrantContext
implements MarlinConst {
    private static final AtomicInteger CTX_COUNT = new AtomicInteger(1);
    private final Object cleanerObj;
    boolean dirty = false;
    final double[] double6 = new double[6];
    final Curve curve = new Curve();
    final DMarlinRenderingEngine.NormalizingPathIterator nPCPathIterator;
    final DMarlinRenderingEngine.NormalizingPathIterator nPQPathIterator;
    final TransformingPathConsumer2D transformerPC2D;
    private WeakReference<Path2D.Double> refPath2D = null;
    final Renderer renderer;
    final Stroker stroker;
    final CollinearSimplifier simplifier = new CollinearSimplifier();
    final PathSimplifier pathSimplifier = new PathSimplifier();
    final Dasher dasher;
    final MarlinTileGenerator ptg;
    final MarlinCache cache;
    int stroking = 0;
    boolean doRender = false;
    boolean doClip = false;
    boolean closedPath = false;
    final double[] clipRect = new double[4];
    double clipInvScale = 0.0;
    final TransformingPathConsumer2D.CurveBasicMonotonizer monotonizer;
    int firstFlags = 0;
    final TransformingPathConsumer2D.CurveClipSplitter curveClipSplitter;
    final DPQSSorterContext sorterCtx;
    private final ArrayCacheIntClean cleanIntCache = new ArrayCacheIntClean(5);
    private final ArrayCacheInt dirtyIntCache = new ArrayCacheInt(5);
    private final ArrayCacheDouble dirtyDoubleCache = new ArrayCacheDouble(4);
    private final ArrayCacheByte dirtyByteCache = new ArrayCacheByte(2);
    final RendererStats stats;
    final PathConsumer2DAdapter p2dAdapter = new PathConsumer2DAdapter();

    static RendererContext createContext() {
        return new RendererContext("ctx" + CTX_COUNT.getAndIncrement());
    }

    RendererContext(String name) {
        if (LOG_CREATE_CONTEXT) {
            MarlinUtils.logInfo("new RendererContext = " + name);
        }
        this.cleanerObj = new Object();
        if (DO_STATS) {
            this.stats = RendererStats.createInstance(this.cleanerObj, name);
            this.stats.cacheStats = new ArrayCacheConst.CacheStats[]{this.cleanIntCache.stats, this.dirtyIntCache.stats, this.dirtyDoubleCache.stats, this.dirtyByteCache.stats};
        } else {
            this.stats = null;
        }
        this.nPCPathIterator = new DMarlinRenderingEngine.NormalizingPathIterator.NearestPixelCenter(this.double6);
        this.nPQPathIterator = new DMarlinRenderingEngine.NormalizingPathIterator.NearestPixelQuarter(this.double6);
        this.monotonizer = new TransformingPathConsumer2D.CurveBasicMonotonizer(this);
        this.curveClipSplitter = new TransformingPathConsumer2D.CurveClipSplitter(this);
        this.transformerPC2D = new TransformingPathConsumer2D(this);
        this.cache = new MarlinCache(this);
        this.renderer = new Renderer(this);
        this.ptg = new MarlinTileGenerator(this.stats, this.renderer, this.cache);
        this.stroker = new Stroker(this);
        this.dasher = new Dasher(this);
        this.sorterCtx = MergeSort.USE_DPQS ? new DPQSSorterContext() : null;
    }

    void dispose() {
        if (DO_STATS) {
            if (this.stats.totalOffHeap > this.stats.totalOffHeapMax) {
                this.stats.totalOffHeapMax = this.stats.totalOffHeap;
            }
            this.stats.totalOffHeap = 0L;
        }
        this.stroking = 0;
        this.doRender = false;
        this.doClip = false;
        this.closedPath = false;
        this.clipInvScale = 0.0;
        this.firstFlags = 0;
        if (this.dirty) {
            this.nPCPathIterator.dispose();
            this.nPQPathIterator.dispose();
            this.dasher.dispose();
            this.stroker.dispose();
            this.dirty = false;
        }
    }

    Path2D.Double getPath2D() {
        Path2D.Double p2d;
        Path2D.Double double_ = p2d = this.refPath2D != null ? (Path2D.Double)this.refPath2D.get() : null;
        if (p2d == null) {
            p2d = new Path2D.Double(1, INITIAL_EDGES_COUNT);
            this.refPath2D = new WeakReference<Path2D.Double>(p2d);
        }
        p2d.reset();
        return p2d;
    }

    RendererStats stats() {
        return this.stats;
    }

    OffHeapArray newOffHeapArray(long initialSize) {
        if (DO_STATS) {
            this.stats.totalOffHeapInitial += initialSize;
        }
        return new OffHeapArray(this.cleanerObj, initialSize);
    }

    ArrayCacheIntClean.Reference newCleanIntArrayRef(int initialSize) {
        return this.cleanIntCache.createRef(initialSize);
    }

    ArrayCacheInt.Reference newDirtyIntArrayRef(int initialSize) {
        return this.dirtyIntCache.createRef(initialSize);
    }

    ArrayCacheDouble.Reference newDirtyDoubleArrayRef(int initialSize) {
        return this.dirtyDoubleCache.createRef(initialSize);
    }

    ArrayCacheByte.Reference newDirtyByteArrayRef(int initialSize) {
        return this.dirtyByteCache.createRef(initialSize);
    }

    static final class PathConsumer2DAdapter
    implements DPathConsumer2D {
        private PathConsumer2D out;

        PathConsumer2DAdapter() {
        }

        PathConsumer2DAdapter init(PathConsumer2D out) {
            this.out = out;
            return this;
        }

        @Override
        public void moveTo(double x0, double y0) {
            this.out.moveTo((float)x0, (float)y0);
        }

        @Override
        public void lineTo(double x1, double y1) {
            this.out.lineTo((float)x1, (float)y1);
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
        public void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
            this.out.curveTo((float)x1, (float)y1, (float)x2, (float)y2, (float)x3, (float)y3);
        }

        @Override
        public void quadTo(double x1, double y1, double x2, double y2) {
            this.out.quadTo((float)x1, (float)y1, (float)x2, (float)y2);
        }

        @Override
        public long getNativeConsumer() {
            throw new InternalError("Not using a native peer");
        }
    }
}

