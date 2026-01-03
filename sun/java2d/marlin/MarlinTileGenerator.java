/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import java.util.Arrays;
import jdk.internal.misc.Unsafe;
import sun.java2d.marlin.FloatMath;
import sun.java2d.marlin.MarlinCache;
import sun.java2d.marlin.MarlinConst;
import sun.java2d.marlin.MarlinUtils;
import sun.java2d.marlin.OffHeapArray;
import sun.java2d.marlin.Renderer;
import sun.java2d.marlin.RendererStats;
import sun.java2d.pipe.AATileGenerator;

final class MarlinTileGenerator
implements AATileGenerator,
MarlinConst {
    private static final boolean DISABLE_BLEND = false;
    private static final int MAX_TILE_ALPHA_SUM = TILE_W * TILE_H * MAX_AA_ALPHA;
    private static final int TH_AA_ALPHA_FILL_EMPTY = (MAX_AA_ALPHA + 1) / 3;
    private static final int TH_AA_ALPHA_FILL_FULL = (MAX_AA_ALPHA + 1) * 2 / 3;
    private static final int FILL_TILE_W = TILE_W >> 1;
    private final Renderer renderer;
    private final MarlinCache cache;
    private int x;
    private int y;
    final RendererStats rdrStats;

    MarlinTileGenerator(RendererStats stats, Renderer r, MarlinCache cache) {
        this.rdrStats = stats;
        this.renderer = r;
        this.cache = cache;
    }

    MarlinTileGenerator init() {
        this.x = this.cache.bboxX0;
        this.y = this.cache.bboxY0;
        return this;
    }

    @Override
    public void dispose() {
        this.cache.dispose();
        this.renderer.dispose();
    }

    void getBbox(int[] bbox) {
        bbox[0] = this.cache.bboxX0;
        bbox[1] = this.cache.bboxY0;
        bbox[2] = this.cache.bboxX1;
        bbox[3] = this.cache.bboxY1;
    }

    @Override
    public int getTileWidth() {
        return TILE_W;
    }

    @Override
    public int getTileHeight() {
        return TILE_H;
    }

    @Override
    public int getTypicalAlpha() {
        int alpha;
        int al = this.cache.alphaSumInTile(this.x);
        int n = al == 0 ? 0 : (alpha = al == MAX_TILE_ALPHA_SUM ? 255 : 128);
        if (DO_STATS) {
            this.rdrStats.hist_tile_generator_alpha.add(alpha);
        }
        return alpha;
    }

    @Override
    public void nextTile() {
        if ((this.x += TILE_W) >= this.cache.bboxX1) {
            this.x = this.cache.bboxX0;
            this.y += TILE_H;
            if (this.y < this.cache.bboxY1) {
                this.renderer.endRendering(this.y);
            }
        }
    }

    @Override
    public void getAlpha(byte[] tile, int offset, int rowstride) {
        if (this.cache.useRLE) {
            this.getAlphaRLE(tile, offset, rowstride);
        } else {
            this.getAlphaNoRLE(tile, offset, rowstride);
        }
    }

    private void getAlphaNoRLE(byte[] tile, int offset, int rowstride) {
        MarlinCache _cache = this.cache;
        long[] rowAAChunkIndex = _cache.rowAAChunkIndex;
        int[] rowAAx0 = _cache.rowAAx0;
        int[] rowAAx1 = _cache.rowAAx1;
        int x0 = this.x;
        int x1 = FloatMath.min(x0 + TILE_W, _cache.bboxX1);
        boolean y0 = false;
        int y1 = FloatMath.min(this.y + TILE_H, _cache.bboxY1) - this.y;
        if (DO_LOG_BOUNDS) {
            MarlinUtils.logInfo("getAlpha = [" + x0 + " ... " + x1 + "[ [0 ... " + y1 + "[");
        }
        Unsafe _unsafe = OffHeapArray.UNSAFE;
        long SIZE2 = 1L;
        long addr_rowAA = _cache.rowAAChunk.address;
        int skipRowPixels = rowstride - (x1 - x0);
        int idx = offset;
        for (int cy = 0; cy < y1; ++cy) {
            int aax0;
            int cx = x0;
            int aax1 = rowAAx1[cy];
            if (aax1 > x0 && (aax0 = rowAAx0[cy]) < x1) {
                int end;
                cx = aax0;
                if (cx <= x0) {
                    cx = x0;
                } else {
                    for (end = x0; end < cx; ++end) {
                        tile[idx++] = 0;
                    }
                }
                long addr = addr_rowAA + rowAAChunkIndex[cy] + (long)(cx - aax0);
                int n = end = aax1 <= x1 ? aax1 : x1;
                while (cx < end) {
                    tile[idx++] = _unsafe.getByte(addr);
                    ++addr;
                    ++cx;
                }
            }
            while (cx < x1) {
                tile[idx++] = 0;
                ++cx;
            }
            if (DO_TRACE) {
                for (int i = idx - (x1 - x0); i < idx; ++i) {
                    System.out.print(MarlinTileGenerator.hex(tile[i], 2));
                }
                System.out.println();
            }
            idx += skipRowPixels;
        }
        this.nextTile();
    }

    private void getAlphaRLE(byte[] tile, int offset, int rowstride) {
        byte refVal;
        int clearTile;
        int area;
        MarlinCache _cache = this.cache;
        long[] rowAAChunkIndex = _cache.rowAAChunkIndex;
        int[] rowAAx0 = _cache.rowAAx0;
        int[] rowAAx1 = _cache.rowAAx1;
        int[] rowAAEnc = _cache.rowAAEnc;
        long[] rowAALen = _cache.rowAALen;
        long[] rowAAPos = _cache.rowAAPos;
        int x0 = this.x;
        int x1 = FloatMath.min(x0 + TILE_W, _cache.bboxX1);
        int w = x1 - x0;
        boolean y0 = false;
        int y1 = FloatMath.min(this.y + TILE_H, _cache.bboxY1) - this.y;
        if (DO_LOG_BOUNDS) {
            MarlinUtils.logInfo("getAlpha = [" + x0 + " ... " + x1 + "[ [0 ... " + y1 + "[");
        }
        if (w >= FILL_TILE_W && (area = w * y1) > 64) {
            int alphaSum = this.cache.alphaSumInTile(x0);
            if (alphaSum < area * TH_AA_ALPHA_FILL_EMPTY) {
                clearTile = 1;
                refVal = 0;
            } else if (alphaSum > area * TH_AA_ALPHA_FILL_FULL) {
                clearTile = 2;
                refVal = -1;
            } else {
                clearTile = 0;
                refVal = 0;
            }
        } else {
            clearTile = 0;
            refVal = 0;
        }
        Unsafe _unsafe = OffHeapArray.UNSAFE;
        long SIZE_BYTE = 1L;
        long SIZE_INT = 4L;
        long addr_rowAA = _cache.rowAAChunk.address;
        int skipRowPixels = rowstride - w;
        int idx = offset;
        switch (clearTile) {
            case 1: {
                Arrays.fill(tile, offset, offset + y1 * rowstride, refVal);
                for (int cy = 0; cy < y1; ++cy) {
                    int cx = x0;
                    if (rowAAEnc[cy] == 0) {
                        int aax0;
                        int aax1 = rowAAx1[cy];
                        if (aax1 > x0 && (aax0 = rowAAx0[cy]) < x1) {
                            int end;
                            cx = aax0;
                            if (cx <= x0) {
                                cx = x0;
                            } else {
                                idx += cx - x0;
                            }
                            addr = addr_rowAA + rowAAChunkIndex[cy] + (long)(cx - aax0);
                            int n = end = aax1 <= x1 ? aax1 : x1;
                            while (cx < end) {
                                tile[idx++] = _unsafe.getByte(addr);
                                ++addr;
                                ++cx;
                            }
                        }
                    } else if (rowAAx1[cy] > x0) {
                        cx = rowAAx0[cy];
                        if (cx > x1) {
                            cx = x1;
                        }
                        if (cx > x0) {
                            idx += cx - x0;
                        }
                        long addr_row = addr_rowAA + rowAAChunkIndex[cy];
                        long addr_end = addr_row + rowAALen[cy];
                        addr = addr_row + rowAAPos[cy];
                        long last_addr = 0L;
                        while (cx < x1 && addr < addr_end) {
                            int runLen;
                            int rx1;
                            last_addr = addr;
                            int packed = _unsafe.getInt(addr);
                            int cx1 = packed >> 8;
                            addr += 4L;
                            int rx0 = cx;
                            if (rx0 < x0) {
                                rx0 = x0;
                            }
                            if ((rx1 = (cx = cx1)) > x1) {
                                rx1 = x1;
                                cx = x1;
                            }
                            if ((runLen = rx1 - rx0) <= 0) continue;
                            if ((packed &= 0xFF) == 0) {
                                idx += runLen;
                                continue;
                            }
                            byte val = (byte)packed;
                            do {
                                tile[idx++] = val;
                            } while (--runLen > 0);
                        }
                        if (last_addr != 0L) {
                            rowAAx0[cy] = cx;
                            rowAAPos[cy] = last_addr - addr_row;
                        }
                    }
                    if (cx < x1) {
                        idx += x1 - cx;
                    }
                    if (DO_TRACE) {
                        for (int i = idx - (x1 - x0); i < idx; ++i) {
                            System.out.print(MarlinTileGenerator.hex(tile[i], 2));
                        }
                        System.out.println();
                    }
                    idx += skipRowPixels;
                }
                break;
            }
            default: {
                for (int cy = 0; cy < y1; ++cy) {
                    int cx = x0;
                    if (rowAAEnc[cy] == 0) {
                        int aax0;
                        int aax1 = rowAAx1[cy];
                        if (aax1 > x0 && (aax0 = rowAAx0[cy]) < x1) {
                            cx = aax0;
                            if (cx <= x0) {
                                cx = x0;
                            } else {
                                for (end = x0; end < cx; ++end) {
                                    tile[idx++] = 0;
                                }
                            }
                            addr = addr_rowAA + rowAAChunkIndex[cy] + (long)(cx - aax0);
                            int n = end = aax1 <= x1 ? aax1 : x1;
                            while (cx < end) {
                                tile[idx++] = _unsafe.getByte(addr);
                                ++addr;
                                ++cx;
                            }
                        }
                    } else if (rowAAx1[cy] > x0) {
                        cx = rowAAx0[cy];
                        if (cx > x1) {
                            cx = x1;
                        }
                        for (end = x0; end < cx; ++end) {
                            tile[idx++] = 0;
                        }
                        long addr_row = addr_rowAA + rowAAChunkIndex[cy];
                        long addr_end = addr_row + rowAALen[cy];
                        addr = addr_row + rowAAPos[cy];
                        long last_addr = 0L;
                        while (cx < x1 && addr < addr_end) {
                            int runLen;
                            int rx1;
                            last_addr = addr;
                            int packed = _unsafe.getInt(addr);
                            int cx1 = packed >> 8;
                            addr += 4L;
                            int rx0 = cx;
                            if (rx0 < x0) {
                                rx0 = x0;
                            }
                            if ((rx1 = (cx = cx1)) > x1) {
                                rx1 = x1;
                                cx = x1;
                            }
                            if ((runLen = rx1 - rx0) <= 0) continue;
                            byte val = (byte)(packed &= 0xFF);
                            do {
                                tile[idx++] = val;
                            } while (--runLen > 0);
                        }
                        if (last_addr != 0L) {
                            rowAAx0[cy] = cx;
                            rowAAPos[cy] = last_addr - addr_row;
                        }
                    }
                    while (cx < x1) {
                        tile[idx++] = 0;
                        ++cx;
                    }
                    if (DO_TRACE) {
                        for (int i = idx - (x1 - x0); i < idx; ++i) {
                            System.out.print(MarlinTileGenerator.hex(tile[i], 2));
                        }
                        System.out.println();
                    }
                    idx += skipRowPixels;
                }
                break;
            }
            case 2: {
                Arrays.fill(tile, offset, offset + y1 * rowstride, refVal);
                for (int cy = 0; cy < y1; ++cy) {
                    int cx = x0;
                    if (rowAAEnc[cy] == 0) {
                        int aax0;
                        int aax1 = rowAAx1[cy];
                        if (aax1 > x0 && (aax0 = rowAAx0[cy]) < x1) {
                            cx = aax0;
                            if (cx <= x0) {
                                cx = x0;
                            } else {
                                for (end = x0; end < cx; ++end) {
                                    tile[idx++] = 0;
                                }
                            }
                            addr = addr_rowAA + rowAAChunkIndex[cy] + (long)(cx - aax0);
                            int n = end = aax1 <= x1 ? aax1 : x1;
                            while (cx < end) {
                                tile[idx++] = _unsafe.getByte(addr);
                                ++addr;
                                ++cx;
                            }
                        }
                    } else if (rowAAx1[cy] > x0) {
                        cx = rowAAx0[cy];
                        if (cx > x1) {
                            cx = x1;
                        }
                        for (end = x0; end < cx; ++end) {
                            tile[idx++] = 0;
                        }
                        long addr_row = addr_rowAA + rowAAChunkIndex[cy];
                        long addr_end = addr_row + rowAALen[cy];
                        addr = addr_row + rowAAPos[cy];
                        long last_addr = 0L;
                        while (cx < x1 && addr < addr_end) {
                            int runLen;
                            int rx1;
                            last_addr = addr;
                            int packed = _unsafe.getInt(addr);
                            int cx1 = packed >> 8;
                            addr += 4L;
                            int rx0 = cx;
                            if (rx0 < x0) {
                                rx0 = x0;
                            }
                            if ((rx1 = (cx = cx1)) > x1) {
                                rx1 = x1;
                                cx = x1;
                            }
                            if ((runLen = rx1 - rx0) <= 0) continue;
                            if ((packed &= 0xFF) == 255) {
                                idx += runLen;
                                continue;
                            }
                            byte val = (byte)packed;
                            do {
                                tile[idx++] = val;
                            } while (--runLen > 0);
                        }
                        if (last_addr != 0L) {
                            rowAAx0[cy] = cx;
                            rowAAPos[cy] = last_addr - addr_row;
                        }
                    }
                    while (cx < x1) {
                        tile[idx++] = 0;
                        ++cx;
                    }
                    if (DO_TRACE) {
                        for (int i = idx - (x1 - x0); i < idx; ++i) {
                            System.out.print(MarlinTileGenerator.hex(tile[i], 2));
                        }
                        System.out.println();
                    }
                    idx += skipRowPixels;
                }
            }
        }
        this.nextTile();
    }

    static String hex(int v, int d) {
        StringBuilder s = new StringBuilder(Integer.toHexString(v));
        while (s.length() < d) {
            s.insert(0, "0");
        }
        return s.substring(0, d);
    }

    static {
        if (MAX_TILE_ALPHA_SUM <= 0) {
            throw new IllegalStateException("Invalid MAX_TILE_ALPHA_SUM: " + MAX_TILE_ALPHA_SUM);
        }
        if (DO_TRACE) {
            MarlinUtils.logInfo("MAX_AA_ALPHA           : " + MAX_AA_ALPHA);
            MarlinUtils.logInfo("TH_AA_ALPHA_FILL_EMPTY : " + TH_AA_ALPHA_FILL_EMPTY);
            MarlinUtils.logInfo("TH_AA_ALPHA_FILL_FULL  : " + TH_AA_ALPHA_FILL_FULL);
            MarlinUtils.logInfo("FILL_TILE_W            : " + FILL_TILE_W);
        }
    }
}

