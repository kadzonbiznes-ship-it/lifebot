/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import sun.java2d.marlin.MarlinProperties;

interface MarlinConst {
    public static final boolean ENABLE_LOGS = MarlinProperties.isLoggingEnabled();
    public static final boolean USE_LOGGER = ENABLE_LOGS && MarlinProperties.isUseLogger();
    public static final boolean LOG_CREATE_CONTEXT = ENABLE_LOGS && MarlinProperties.isLogCreateContext();
    public static final boolean LOG_UNSAFE_MALLOC = ENABLE_LOGS && MarlinProperties.isLogUnsafeMalloc();
    public static final boolean DO_CHECK_UNSAFE = false;
    public static final boolean DO_STATS = ENABLE_LOGS && MarlinProperties.isDoStats();
    public static final boolean DO_MONITORS = false;
    public static final boolean DO_CHECKS;
    public static final boolean DO_AA_RANGE_CHECK = false;
    public static final boolean DO_LOG_WIDEN_ARRAY;
    public static final boolean DO_LOG_OVERSIZE;
    public static final boolean DO_TRACE;
    public static final boolean DO_FLUSH_STATS = true;
    public static final boolean DO_FLUSH_MONITORS = true;
    public static final boolean USE_DUMP_THREAD = false;
    public static final long DUMP_INTERVAL = 5000L;
    public static final boolean DO_CLEAN_DIRTY = false;
    public static final boolean USE_SIMPLIFIER;
    public static final boolean USE_PATH_SIMPLIFIER;
    public static final boolean DO_CLIP_SUBDIVIDER;
    public static final boolean DO_LOG_BOUNDS;
    public static final boolean DO_LOG_CLIP;
    public static final int INITIAL_PIXEL_WIDTH;
    public static final int INITIAL_PIXEL_HEIGHT;
    public static final int INITIAL_ARRAY = 256;
    public static final int INITIAL_AA_ARRAY;
    public static final int INITIAL_EDGES_COUNT;
    public static final int INITIAL_EDGES_CAPACITY;
    public static final byte BYTE_0 = 0;
    public static final int SUBPIXEL_LG_POSITIONS_X;
    public static final int SUBPIXEL_LG_POSITIONS_Y;
    public static final int MIN_SUBPIXEL_LG_POSITIONS;
    public static final int SUBPIXEL_POSITIONS_X;
    public static final int SUBPIXEL_POSITIONS_Y;
    public static final float MIN_SUBPIXELS;
    public static final int MAX_AA_ALPHA;
    public static final int TILE_H_LG;
    public static final int TILE_H;
    public static final int TILE_W_LG;
    public static final int TILE_W;
    public static final int BLOCK_SIZE_LG;
    public static final int BLOCK_SIZE;
    public static final int WIND_EVEN_ODD = 0;
    public static final int WIND_NON_ZERO = 1;
    public static final int JOIN_MITER = 0;
    public static final int JOIN_ROUND = 1;
    public static final int JOIN_BEVEL = 2;
    public static final int CAP_BUTT = 0;
    public static final int CAP_ROUND = 1;
    public static final int CAP_SQUARE = 2;
    public static final int OUTCODE_TOP = 1;
    public static final int OUTCODE_BOTTOM = 2;
    public static final int OUTCODE_LEFT = 4;
    public static final int OUTCODE_RIGHT = 8;
    public static final int OUTCODE_MASK_T_B = 3;
    public static final int OUTCODE_MASK_L_R = 12;
    public static final int OUTCODE_MASK_T_B_L_R = 15;

    static {
        boolean bl = DO_CHECKS = ENABLE_LOGS && MarlinProperties.isDoChecks();
        if (ENABLE_LOGS) {
            // empty if block
        }
        DO_LOG_WIDEN_ARRAY = false;
        if (ENABLE_LOGS) {
            // empty if block
        }
        DO_LOG_OVERSIZE = false;
        if (ENABLE_LOGS) {
            // empty if block
        }
        DO_TRACE = false;
        USE_SIMPLIFIER = MarlinProperties.isUseSimplifier();
        USE_PATH_SIMPLIFIER = MarlinProperties.isUsePathSimplifier();
        DO_CLIP_SUBDIVIDER = MarlinProperties.isDoClipSubdivider();
        if (ENABLE_LOGS) {
            // empty if block
        }
        DO_LOG_BOUNDS = false;
        if (ENABLE_LOGS) {
            // empty if block
        }
        DO_LOG_CLIP = false;
        INITIAL_PIXEL_WIDTH = MarlinProperties.getInitialPixelWidth();
        INITIAL_PIXEL_HEIGHT = MarlinProperties.getInitialPixelHeight();
        INITIAL_AA_ARRAY = INITIAL_PIXEL_WIDTH;
        INITIAL_EDGES_COUNT = MarlinProperties.getInitialEdges();
        INITIAL_EDGES_CAPACITY = INITIAL_EDGES_COUNT * 24;
        SUBPIXEL_LG_POSITIONS_X = MarlinProperties.getSubPixel_Log2_X();
        SUBPIXEL_LG_POSITIONS_Y = MarlinProperties.getSubPixel_Log2_Y();
        MIN_SUBPIXEL_LG_POSITIONS = Math.min(SUBPIXEL_LG_POSITIONS_X, SUBPIXEL_LG_POSITIONS_Y);
        SUBPIXEL_POSITIONS_X = 1 << SUBPIXEL_LG_POSITIONS_X;
        SUBPIXEL_POSITIONS_Y = 1 << SUBPIXEL_LG_POSITIONS_Y;
        MIN_SUBPIXELS = 1 << MIN_SUBPIXEL_LG_POSITIONS;
        MAX_AA_ALPHA = SUBPIXEL_POSITIONS_X * SUBPIXEL_POSITIONS_Y;
        TILE_H_LG = MarlinProperties.getTileSize_Log2();
        TILE_H = 1 << TILE_H_LG;
        TILE_W_LG = MarlinProperties.getTileWidth_Log2();
        TILE_W = 1 << TILE_W_LG;
        BLOCK_SIZE_LG = MarlinProperties.getBlockSize_Log2();
        BLOCK_SIZE = 1 << BLOCK_SIZE_LG;
    }
}

