/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.security.AccessController;
import java.util.Arrays;
import sun.awt.geom.PathConsumer2D;
import sun.java2d.ReentrantContextProvider;
import sun.java2d.ReentrantContextProviderCLQ;
import sun.java2d.ReentrantContextProviderTL;
import sun.java2d.marlin.DPathConsumer2D;
import sun.java2d.marlin.MarlinCache;
import sun.java2d.marlin.MarlinConst;
import sun.java2d.marlin.MarlinProperties;
import sun.java2d.marlin.MarlinTileGenerator;
import sun.java2d.marlin.MarlinUtils;
import sun.java2d.marlin.MergeSort;
import sun.java2d.marlin.Renderer;
import sun.java2d.marlin.RendererContext;
import sun.java2d.marlin.TransformingPathConsumer2D;
import sun.java2d.marlin.Version;
import sun.java2d.pipe.AATileGenerator;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.RenderingEngine;
import sun.security.action.GetPropertyAction;

public final class DMarlinRenderingEngine
extends RenderingEngine
implements MarlinConst {
    static final boolean DISABLE_2ND_STROKER_CLIPPING = true;
    static final boolean DO_TRACE_PATH = false;
    static final boolean DO_CLIP;
    static final boolean DO_CLIP_FILL = true;
    static final boolean DO_CLIP_RUNTIME_ENABLE;
    private static final float MIN_PEN_SIZE;
    static final double UPPER_BND = 1.7014117331926443E38;
    static final double LOWER_BND = -1.7014117331926443E38;
    private static final boolean USE_THREAD_LOCAL;
    static final int REF_TYPE;
    private static final ReentrantContextProvider<RendererContext> RDR_CTX_PROVIDER;
    private static boolean SETTINGS_LOGGED;

    public DMarlinRenderingEngine() {
        DMarlinRenderingEngine.logSettings(DMarlinRenderingEngine.class.getName());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Shape createStrokedShape(Shape src, float width, int caps, int join, float miterlimit, float[] dashes, float dashphase) {
        RendererContext rdrCtx = DMarlinRenderingEngine.getRendererContext();
        try {
            Path2D.Double p2d = rdrCtx.getPath2D();
            this.strokeTo(rdrCtx, src, null, width, NormMode.OFF, caps, join, miterlimit, dashes, dashphase, rdrCtx.transformerPC2D.wrapPath2D(p2d));
            Path2D.Double double_ = new Path2D.Double(p2d);
            return double_;
        }
        finally {
            DMarlinRenderingEngine.returnRendererContext(rdrCtx);
        }
    }

    @Override
    public void strokeTo(Shape src, AffineTransform at, BasicStroke bs, boolean thin, boolean normalize, boolean antialias, PathConsumer2D consumer) {
        this.strokeTo(src, at, null, bs, thin, normalize, antialias, consumer);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void strokeTo(Shape src, AffineTransform at, Region clip, BasicStroke bs, boolean thin, boolean normalize, boolean antialias, PathConsumer2D consumer) {
        AffineTransform _at;
        AffineTransform affineTransform = _at = at != null && !at.isIdentity() ? at : null;
        NormMode norm = normalize ? (antialias ? NormMode.ON_WITH_AA : NormMode.ON_NO_AA) : NormMode.OFF;
        RendererContext rdrCtx = DMarlinRenderingEngine.getRendererContext();
        try {
            if (clip != null && (DO_CLIP || DO_CLIP_RUNTIME_ENABLE && MarlinProperties.isDoClipAtRuntime())) {
                double[] clipRect = rdrCtx.clipRect;
                double rdrOffX = 0.25;
                double rdrOffY = 0.25;
                double margin = 0.001;
                clipRect[0] = (double)clip.getLoY() - 0.001 + 0.25;
                clipRect[1] = (double)(clip.getLoY() + clip.getHeight()) + 0.001 + 0.25;
                clipRect[2] = (double)clip.getLoX() - 0.001 + 0.25;
                clipRect[3] = (double)(clip.getLoX() + clip.getWidth()) + 0.001 + 0.25;
                if (MarlinConst.DO_LOG_CLIP) {
                    MarlinUtils.logInfo("clipRect (clip): " + Arrays.toString(rdrCtx.clipRect));
                }
                rdrCtx.doClip = true;
            }
            this.strokeTo(rdrCtx, src, _at, bs, thin, norm, antialias, rdrCtx.p2dAdapter.init(consumer));
        }
        finally {
            DMarlinRenderingEngine.returnRendererContext(rdrCtx);
        }
    }

    void strokeTo(RendererContext rdrCtx, Shape src, AffineTransform at, BasicStroke bs, boolean thin, NormMode normalize, boolean antialias, DPathConsumer2D pc2d) {
        double lw = thin ? (antialias ? this.userSpaceLineWidth(at, MIN_PEN_SIZE) : this.userSpaceLineWidth(at, 1.0)) : (double)bs.getLineWidth();
        this.strokeTo(rdrCtx, src, at, lw, normalize, bs.getEndCap(), bs.getLineJoin(), bs.getMiterLimit(), bs.getDashArray(), bs.getDashPhase(), pc2d);
    }

    private double userSpaceLineWidth(AffineTransform at, double lw) {
        double widthScale;
        if (at == null) {
            widthScale = 1.0;
        } else if ((at.getType() & 0x24) != 0) {
            widthScale = Math.sqrt(Math.abs(at.getDeterminant()));
        } else {
            double A = at.getScaleX();
            double C = at.getShearX();
            double B = at.getShearY();
            double D = at.getScaleY();
            double EA = A * A + B * B;
            double EB = 2.0 * (A * C + B * D);
            double EC2 = C * C + D * D;
            double hypot = Math.sqrt(EB * EB + (EA - EC2) * (EA - EC2));
            double widthsquared = (EA + EC2 + hypot) / 2.0;
            widthScale = Math.sqrt(widthsquared);
        }
        return lw / widthScale;
    }

    void strokeTo(RendererContext rdrCtx, Shape src, AffineTransform at, double width, NormMode norm, int caps, int join, float miterlimit, float[] dashes, float dashphase, DPathConsumer2D pc2d) {
        AffineTransform strokerat = null;
        int dashLen = -1;
        boolean recycleDashes = false;
        double[] dashesD = null;
        if (dashes != null) {
            recycleDashes = true;
            dashLen = dashes.length;
            dashesD = rdrCtx.dasher.copyDashArray(dashes);
        }
        if (at != null && !at.isIdentity()) {
            double a = at.getScaleX();
            double b = at.getShearX();
            double c = at.getShearY();
            double d = at.getScaleY();
            double det = a * d - c * b;
            if (Math.abs(det) <= 9.9E-324) {
                pc2d.moveTo(0.0, 0.0);
                pc2d.pathDone();
                return;
            }
            if (DMarlinRenderingEngine.nearZero(a * b + c * d) && DMarlinRenderingEngine.nearZero(a * a + c * c - (b * b + d * d))) {
                double scale = Math.sqrt(a * a + c * c);
                if (dashesD != null) {
                    int i = 0;
                    while (i < dashLen) {
                        int n = i++;
                        dashesD[n] = dashesD[n] * scale;
                    }
                    dashphase = (float)((double)dashphase * scale);
                }
                width *= scale;
            } else {
                strokerat = at;
            }
        } else {
            at = null;
        }
        TransformingPathConsumer2D transformerPC2D = rdrCtx.transformerPC2D;
        if (USE_SIMPLIFIER) {
            pc2d = rdrCtx.simplifier.init(pc2d);
        }
        pc2d = transformerPC2D.deltaTransformConsumer(pc2d, strokerat);
        pc2d = rdrCtx.stroker.init(pc2d, width, caps, join, miterlimit, dashesD == null);
        rdrCtx.monotonizer.init(width);
        if (dashesD != null) {
            pc2d = rdrCtx.dasher.init(pc2d, dashesD, dashLen, dashphase, recycleDashes);
            rdrCtx.stroker.disableClipping();
        } else if (rdrCtx.doClip && caps != 0) {
            pc2d = transformerPC2D.detectClosedPath(pc2d);
        }
        pc2d = transformerPC2D.inverseDeltaTransformConsumer(pc2d, strokerat);
        PathIterator pi = norm.getNormalizingPathIterator(rdrCtx, src.getPathIterator(at));
        DMarlinRenderingEngine.pathTo(rdrCtx, pi, pc2d);
    }

    private static boolean nearZero(double num) {
        return Math.abs(num) < 2.0 * Math.ulp(num);
    }

    private static void pathTo(RendererContext rdrCtx, PathIterator pi, DPathConsumer2D pc2d) {
        if (USE_PATH_SIMPLIFIER) {
            pc2d = rdrCtx.pathSimplifier.init(pc2d);
        }
        rdrCtx.dirty = true;
        DMarlinRenderingEngine.pathToLoop(rdrCtx.double6, pi, pc2d);
        rdrCtx.dirty = false;
    }

    private static void pathToLoop(double[] coords, PathIterator pi, DPathConsumer2D pc2d) {
        boolean subpathStarted = false;
        while (!pi.isDone()) {
            switch (pi.currentSegment(coords)) {
                case 0: {
                    if (!(coords[0] < 1.7014117331926443E38) || !(coords[0] > -1.7014117331926443E38) || !(coords[1] < 1.7014117331926443E38) || !(coords[1] > -1.7014117331926443E38)) break;
                    pc2d.moveTo(coords[0], coords[1]);
                    subpathStarted = true;
                    break;
                }
                case 1: {
                    if (!(coords[0] < 1.7014117331926443E38) || !(coords[0] > -1.7014117331926443E38) || !(coords[1] < 1.7014117331926443E38) || !(coords[1] > -1.7014117331926443E38)) break;
                    if (subpathStarted) {
                        pc2d.lineTo(coords[0], coords[1]);
                        break;
                    }
                    pc2d.moveTo(coords[0], coords[1]);
                    subpathStarted = true;
                    break;
                }
                case 2: {
                    if (!(coords[2] < 1.7014117331926443E38) || !(coords[2] > -1.7014117331926443E38) || !(coords[3] < 1.7014117331926443E38) || !(coords[3] > -1.7014117331926443E38)) break;
                    if (subpathStarted) {
                        if (coords[0] < 1.7014117331926443E38 && coords[0] > -1.7014117331926443E38 && coords[1] < 1.7014117331926443E38 && coords[1] > -1.7014117331926443E38) {
                            pc2d.quadTo(coords[0], coords[1], coords[2], coords[3]);
                            break;
                        }
                        pc2d.lineTo(coords[2], coords[3]);
                        break;
                    }
                    pc2d.moveTo(coords[2], coords[3]);
                    subpathStarted = true;
                    break;
                }
                case 3: {
                    if (!(coords[4] < 1.7014117331926443E38) || !(coords[4] > -1.7014117331926443E38) || !(coords[5] < 1.7014117331926443E38) || !(coords[5] > -1.7014117331926443E38)) break;
                    if (subpathStarted) {
                        if (coords[0] < 1.7014117331926443E38 && coords[0] > -1.7014117331926443E38 && coords[1] < 1.7014117331926443E38 && coords[1] > -1.7014117331926443E38 && coords[2] < 1.7014117331926443E38 && coords[2] > -1.7014117331926443E38 && coords[3] < 1.7014117331926443E38 && coords[3] > -1.7014117331926443E38) {
                            pc2d.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                            break;
                        }
                        pc2d.lineTo(coords[4], coords[5]);
                        break;
                    }
                    pc2d.moveTo(coords[4], coords[5]);
                    subpathStarted = true;
                    break;
                }
                case 4: {
                    if (!subpathStarted) break;
                    pc2d.closePath();
                    break;
                }
            }
            pi.next();
        }
        pc2d.pathDone();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public AATileGenerator getAATileGenerator(Shape s, AffineTransform at, Region clip, BasicStroke bs, boolean thin, boolean normalize, int[] bbox) {
        MarlinTileGenerator ptg = null;
        Renderer r = null;
        RendererContext rdrCtx = DMarlinRenderingEngine.getRendererContext();
        try {
            NormMode norm;
            if (DO_CLIP || DO_CLIP_RUNTIME_ENABLE && MarlinProperties.isDoClipAtRuntime()) {
                double[] clipRect = rdrCtx.clipRect;
                double rdrOffX = Renderer.RDR_OFFSET_X;
                double rdrOffY = Renderer.RDR_OFFSET_Y;
                double margin = 0.001;
                clipRect[0] = (double)clip.getLoY() - 0.001 + rdrOffY;
                clipRect[1] = (double)(clip.getLoY() + clip.getHeight()) + 0.001 + rdrOffY;
                clipRect[2] = (double)clip.getLoX() - 0.001 + rdrOffX;
                clipRect[3] = (double)(clip.getLoX() + clip.getWidth()) + 0.001 + rdrOffX;
                if (MarlinConst.DO_LOG_CLIP) {
                    MarlinUtils.logInfo("clipRect (clip): " + Arrays.toString(rdrCtx.clipRect));
                }
                rdrCtx.doClip = true;
            }
            AffineTransform _at = at != null && !at.isIdentity() ? at : null;
            NormMode normMode = norm = normalize ? NormMode.ON_WITH_AA : NormMode.OFF;
            if (bs == null) {
                PathIterator pi = norm.getNormalizingPathIterator(rdrCtx, s.getPathIterator(_at));
                DPathConsumer2D pc2d = r = rdrCtx.renderer.init(clip.getLoX(), clip.getLoY(), clip.getWidth(), clip.getHeight(), pi.getWindingRule());
                if (rdrCtx.doClip) {
                    pc2d = rdrCtx.transformerPC2D.pathClipper(pc2d);
                }
                DMarlinRenderingEngine.pathTo(rdrCtx, pi, pc2d);
            } else {
                r = rdrCtx.renderer.init(clip.getLoX(), clip.getLoY(), clip.getWidth(), clip.getHeight(), 1);
                this.strokeTo(rdrCtx, s, _at, bs, thin, norm, true, r);
            }
            if (r.endRendering()) {
                ptg = rdrCtx.ptg.init();
                ptg.getBbox(bbox);
                r = null;
            }
        }
        finally {
            if (r != null) {
                r.dispose();
            }
        }
        return ptg;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public AATileGenerator getAATileGenerator(double x, double y, double dx1, double dy1, double dx2, double dy2, double lw1, double lw2, Region clip, int[] bbox) {
        double ldy2;
        double ldx2;
        double ldy1;
        double ldx1;
        boolean innerpgram;
        boolean bl = innerpgram = lw1 > 0.0 && lw2 > 0.0;
        if (innerpgram) {
            ldx1 = dx1 * lw1;
            ldy1 = dy1 * lw1;
            ldx2 = dx2 * lw2;
            ldy2 = dy2 * lw2;
            x -= (ldx1 + ldx2) / 2.0;
            y -= (ldy1 + ldy2) / 2.0;
            dx1 += ldx1;
            dy1 += ldy1;
            dx2 += ldx2;
            dy2 += ldy2;
            if (lw1 > 1.0 && lw2 > 1.0) {
                innerpgram = false;
            }
        } else {
            ldy2 = 0.0;
            ldx2 = 0.0;
            ldy1 = 0.0;
            ldx1 = 0.0;
        }
        MarlinTileGenerator ptg = null;
        Renderer r = null;
        RendererContext rdrCtx = DMarlinRenderingEngine.getRendererContext();
        try {
            r = rdrCtx.renderer.init(clip.getLoX(), clip.getLoY(), clip.getWidth(), clip.getHeight(), 0);
            r.moveTo(x, y);
            r.lineTo(x + dx1, y + dy1);
            r.lineTo(x + dx1 + dx2, y + dy1 + dy2);
            r.lineTo(x + dx2, y + dy2);
            r.closePath();
            if (innerpgram) {
                r.moveTo(x += ldx1 + ldx2, y += ldy1 + ldy2);
                r.lineTo(x + (dx1 -= 2.0 * ldx1), y + (dy1 -= 2.0 * ldy1));
                r.lineTo(x + dx1 + (dx2 -= 2.0 * ldx2), y + dy1 + (dy2 -= 2.0 * ldy2));
                r.lineTo(x + dx2, y + dy2);
                r.closePath();
            }
            r.pathDone();
            if (r.endRendering()) {
                ptg = rdrCtx.ptg.init();
                ptg.getBbox(bbox);
                r = null;
            }
        }
        finally {
            if (r != null) {
                r.dispose();
            }
        }
        return ptg;
    }

    @Override
    public float getMinimumAAPenSize() {
        return MIN_PEN_SIZE;
    }

    private static void logSettings(String reClass) {
        if (SETTINGS_LOGGED) {
            return;
        }
        SETTINGS_LOGGED = true;
        String refType = switch (REF_TYPE) {
            default -> "hard";
            case 1 -> "soft";
            case 2 -> "weak";
        };
        MarlinUtils.logInfo("===============================================================================");
        MarlinUtils.logInfo("Marlin software rasterizer           = ENABLED");
        MarlinUtils.logInfo("Version                              = [" + Version.getVersion() + "]");
        MarlinUtils.logInfo("sun.java2d.renderer                  = " + reClass);
        MarlinUtils.logInfo("sun.java2d.renderer.useThreadLocal   = " + USE_THREAD_LOCAL);
        MarlinUtils.logInfo("sun.java2d.renderer.useRef           = " + refType);
        MarlinUtils.logInfo("sun.java2d.renderer.edges            = " + MarlinConst.INITIAL_EDGES_COUNT);
        MarlinUtils.logInfo("sun.java2d.renderer.pixelWidth       = " + MarlinConst.INITIAL_PIXEL_WIDTH);
        MarlinUtils.logInfo("sun.java2d.renderer.pixelHeight      = " + MarlinConst.INITIAL_PIXEL_HEIGHT);
        MarlinUtils.logInfo("sun.java2d.renderer.profile          = " + (MarlinProperties.isProfileQuality() ? "quality" : "speed"));
        MarlinUtils.logInfo("sun.java2d.renderer.subPixel_log2_X  = " + MarlinConst.SUBPIXEL_LG_POSITIONS_X);
        MarlinUtils.logInfo("sun.java2d.renderer.subPixel_log2_Y  = " + MarlinConst.SUBPIXEL_LG_POSITIONS_Y);
        MarlinUtils.logInfo("sun.java2d.renderer.tileSize_log2    = " + MarlinConst.TILE_H_LG);
        MarlinUtils.logInfo("sun.java2d.renderer.tileWidth_log2   = " + MarlinConst.TILE_W_LG);
        MarlinUtils.logInfo("sun.java2d.renderer.blockSize_log2   = " + MarlinConst.BLOCK_SIZE_LG);
        MarlinUtils.logInfo("sun.java2d.renderer.forceRLE         = " + MarlinProperties.isForceRLE());
        MarlinUtils.logInfo("sun.java2d.renderer.forceNoRLE       = " + MarlinProperties.isForceNoRLE());
        MarlinUtils.logInfo("sun.java2d.renderer.useTileFlags     = " + MarlinProperties.isUseTileFlags());
        MarlinUtils.logInfo("sun.java2d.renderer.useTileFlags.useHeuristics = " + MarlinProperties.isUseTileFlagsWithHeuristics());
        MarlinUtils.logInfo("sun.java2d.renderer.rleMinWidth      = " + MarlinCache.RLE_MIN_WIDTH);
        MarlinUtils.logInfo("sun.java2d.renderer.useSimplifier    = " + MarlinConst.USE_SIMPLIFIER);
        MarlinUtils.logInfo("sun.java2d.renderer.usePathSimplifier= " + MarlinConst.USE_PATH_SIMPLIFIER);
        MarlinUtils.logInfo("sun.java2d.renderer.pathSimplifier.pixTol = " + MarlinProperties.getPathSimplifierPixelTolerance());
        MarlinUtils.logInfo("sun.java2d.renderer.stroker.joinError= " + MarlinProperties.getStrokerJoinError());
        MarlinUtils.logInfo("sun.java2d.renderer.stroker.joinStyle= " + MarlinProperties.getStrokerJoinStyle());
        MarlinUtils.logInfo("sun.java2d.renderer.clip             = " + MarlinProperties.isDoClip());
        MarlinUtils.logInfo("sun.java2d.renderer.clip.runtime.enable = " + MarlinProperties.isDoClipRuntimeFlag());
        MarlinUtils.logInfo("sun.java2d.renderer.clip.subdivider  = " + MarlinProperties.isDoClipSubdivider());
        MarlinUtils.logInfo("sun.java2d.renderer.clip.subdivider.minLength = " + MarlinProperties.getSubdividerMinLength());
        MarlinUtils.logInfo("sun.java2d.renderer.doStats          = " + MarlinConst.DO_STATS);
        MarlinUtils.logInfo("sun.java2d.renderer.doMonitors       = false");
        MarlinUtils.logInfo("sun.java2d.renderer.doChecks         = " + MarlinConst.DO_CHECKS);
        MarlinUtils.logInfo("sun.java2d.renderer.skip_rdr         = " + MarlinProperties.isSkipRenderer());
        MarlinUtils.logInfo("sun.java2d.renderer.skip_pipe        = " + MarlinProperties.isSkipRenderTiles());
        MarlinUtils.logInfo("sun.java2d.renderer.useLogger        = " + MarlinConst.USE_LOGGER);
        MarlinUtils.logInfo("sun.java2d.renderer.logCreateContext = " + MarlinConst.LOG_CREATE_CONTEXT);
        MarlinUtils.logInfo("sun.java2d.renderer.logUnsafeMalloc  = " + MarlinConst.LOG_UNSAFE_MALLOC);
        MarlinUtils.logInfo("sun.java2d.renderer.curve_len_err    = " + MarlinProperties.getCurveLengthError());
        MarlinUtils.logInfo("sun.java2d.renderer.cubic_dec_d2     = " + MarlinProperties.getCubicDecD2());
        MarlinUtils.logInfo("sun.java2d.renderer.cubic_inc_d1     = " + MarlinProperties.getCubicIncD1());
        MarlinUtils.logInfo("sun.java2d.renderer.quad_dec_d2      = " + MarlinProperties.getQuadDecD2());
        MarlinUtils.logInfo("Renderer settings:");
        MarlinUtils.logInfo("SORT         = " + MergeSort.SORT_TYPE);
        MarlinUtils.logInfo("CUB_DEC_BND  = " + Renderer.CUB_DEC_BND);
        MarlinUtils.logInfo("CUB_INC_BND  = " + Renderer.CUB_INC_BND);
        MarlinUtils.logInfo("QUAD_DEC_BND = " + Renderer.QUAD_DEC_BND);
        MarlinUtils.logInfo("INITIAL_EDGES_CAPACITY               = " + MarlinConst.INITIAL_EDGES_CAPACITY);
        MarlinUtils.logInfo("INITIAL_CROSSING_COUNT               = " + Renderer.INITIAL_CROSSING_COUNT);
        MarlinUtils.logInfo("===============================================================================");
    }

    static RendererContext getRendererContext() {
        RendererContext rdrCtx = RDR_CTX_PROVIDER.acquire();
        return rdrCtx;
    }

    static void returnRendererContext(RendererContext rdrCtx) {
        rdrCtx.dispose();
        RDR_CTX_PROVIDER.release(rdrCtx);
    }

    static {
        String refType;
        DO_CLIP = MarlinProperties.isDoClip();
        DO_CLIP_RUNTIME_ENABLE = MarlinProperties.isDoClipRuntimeFlag();
        MIN_PEN_SIZE = 1.0f / MIN_SUBPIXELS;
        USE_THREAD_LOCAL = MarlinProperties.isUseThreadLocal();
        switch (refType = AccessController.doPrivileged(new GetPropertyAction("sun.java2d.renderer.useRef", "soft"))) {
            default: {
                REF_TYPE = 1;
                break;
            }
            case "weak": {
                REF_TYPE = 2;
                break;
            }
            case "hard": {
                REF_TYPE = 0;
            }
        }
        RDR_CTX_PROVIDER = USE_THREAD_LOCAL ? new ReentrantContextProviderTL<RendererContext>(REF_TYPE){

            @Override
            protected RendererContext newContext() {
                return RendererContext.createContext();
            }
        } : new ReentrantContextProviderCLQ<RendererContext>(REF_TYPE){

            @Override
            protected RendererContext newContext() {
                return RendererContext.createContext();
            }
        };
        SETTINGS_LOGGED = !ENABLE_LOGS;
    }

    private static enum NormMode {
        ON_WITH_AA{

            @Override
            PathIterator getNormalizingPathIterator(RendererContext rdrCtx, PathIterator src) {
                return rdrCtx.nPCPathIterator.init(src);
            }
        }
        ,
        ON_NO_AA{

            @Override
            PathIterator getNormalizingPathIterator(RendererContext rdrCtx, PathIterator src) {
                return rdrCtx.nPQPathIterator.init(src);
            }
        }
        ,
        OFF{

            @Override
            PathIterator getNormalizingPathIterator(RendererContext rdrCtx, PathIterator src) {
                return src;
            }
        };


        abstract PathIterator getNormalizingPathIterator(RendererContext var1, PathIterator var2);
    }

    static abstract class NormalizingPathIterator
    implements PathIterator {
        private PathIterator src;
        private double curx_adjust;
        private double cury_adjust;
        private double movx_adjust;
        private double movy_adjust;
        private final double[] tmp;

        NormalizingPathIterator(double[] tmp) {
            this.tmp = tmp;
        }

        final NormalizingPathIterator init(PathIterator src) {
            this.src = src;
            return this;
        }

        final void dispose() {
            this.src = null;
        }

        @Override
        public final int currentSegment(double[] coords) {
            double y_adjust;
            double x_adjust;
            int lastCoord;
            int type = this.src.currentSegment(coords);
            switch (type) {
                case 0: 
                case 1: {
                    lastCoord = 0;
                    break;
                }
                case 2: {
                    lastCoord = 2;
                    break;
                }
                case 3: {
                    lastCoord = 4;
                    break;
                }
                case 4: {
                    this.curx_adjust = this.movx_adjust;
                    this.cury_adjust = this.movy_adjust;
                    return type;
                }
                default: {
                    throw new InternalError("Unrecognized curve type");
                }
            }
            double coord = coords[lastCoord];
            coords[lastCoord] = x_adjust = this.normCoord(coord);
            x_adjust -= coord;
            coord = coords[lastCoord + 1];
            coords[lastCoord + 1] = y_adjust = this.normCoord(coord);
            y_adjust -= coord;
            switch (type) {
                case 0: {
                    this.movx_adjust = x_adjust;
                    this.movy_adjust = y_adjust;
                    break;
                }
                case 1: {
                    break;
                }
                case 2: {
                    coords[0] = coords[0] + (this.curx_adjust + x_adjust) / 2.0;
                    coords[1] = coords[1] + (this.cury_adjust + y_adjust) / 2.0;
                    break;
                }
                case 3: {
                    coords[0] = coords[0] + this.curx_adjust;
                    coords[1] = coords[1] + this.cury_adjust;
                    coords[2] = coords[2] + x_adjust;
                    coords[3] = coords[3] + y_adjust;
                    break;
                }
            }
            this.curx_adjust = x_adjust;
            this.cury_adjust = y_adjust;
            return type;
        }

        abstract double normCoord(double var1);

        @Override
        public final int currentSegment(float[] coords) {
            double[] _tmp = this.tmp;
            int type = this.currentSegment(_tmp);
            for (int i = 0; i < 6; ++i) {
                coords[i] = (float)_tmp[i];
            }
            return type;
        }

        @Override
        public final int getWindingRule() {
            return this.src.getWindingRule();
        }

        @Override
        public final boolean isDone() {
            if (this.src.isDone()) {
                this.dispose();
                return true;
            }
            return false;
        }

        @Override
        public final void next() {
            this.src.next();
        }

        static final class NearestPixelQuarter
        extends NormalizingPathIterator {
            NearestPixelQuarter(double[] tmp) {
                super(tmp);
            }

            @Override
            double normCoord(double coord) {
                return Math.floor(coord + 0.25) + 0.25;
            }
        }

        static final class NearestPixelCenter
        extends NormalizingPathIterator {
            NearestPixelCenter(double[] tmp) {
                super(tmp);
            }

            @Override
            double normCoord(double coord) {
                return Math.floor(coord) + 0.5;
            }
        }
    }
}

