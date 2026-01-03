/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import jdk.internal.misc.Unsafe;
import sun.java2d.marlin.ArrayCacheConst;
import sun.java2d.marlin.ArrayCacheInt;
import sun.java2d.marlin.ArrayCacheIntClean;
import sun.java2d.marlin.FloatMath;
import sun.java2d.marlin.MarlinConst;
import sun.java2d.marlin.MarlinProperties;
import sun.java2d.marlin.MarlinUtils;
import sun.java2d.marlin.OffHeapArray;
import sun.java2d.marlin.RendererContext;
import sun.java2d.marlin.RendererStats;

public final class MarlinCache
implements MarlinConst {
    static final boolean FORCE_RLE = MarlinProperties.isForceRLE();
    static final boolean FORCE_NO_RLE = MarlinProperties.isForceNoRLE();
    static final int RLE_MIN_WIDTH = Math.max(BLOCK_SIZE, MarlinProperties.getRLEMinWidth());
    static final int RLE_MAX_WIDTH = 0x800000;
    static final long INITIAL_CHUNK_ARRAY = TILE_H * INITIAL_PIXEL_WIDTH >> 2;
    static final byte[] ALPHA_MAP;
    static final OffHeapArray ALPHA_MAP_UNSAFE;
    int bboxX0;
    int bboxY0;
    int bboxX1;
    int bboxY1;
    final long[] rowAAChunkIndex = new long[TILE_H];
    final int[] rowAAx0 = new int[TILE_H];
    final int[] rowAAx1 = new int[TILE_H];
    final int[] rowAAEnc = new int[TILE_H];
    final long[] rowAALen = new long[TILE_H];
    final long[] rowAAPos = new long[TILE_H];
    final OffHeapArray rowAAChunk;
    long rowAAChunkPos;
    int[] touchedTile;
    final RendererStats rdrStats;
    private final ArrayCacheIntClean.Reference touchedTile_ref;
    int tileMin;
    int tileMax;
    boolean useRLE = false;

    MarlinCache(RendererContext rdrCtx) {
        this.rdrStats = rdrCtx.stats();
        this.rowAAChunk = rdrCtx.newOffHeapArray(INITIAL_CHUNK_ARRAY);
        this.touchedTile_ref = rdrCtx.newCleanIntArrayRef(256);
        this.touchedTile = this.touchedTile_ref.initial;
        this.tileMin = Integer.MAX_VALUE;
        this.tileMax = Integer.MIN_VALUE;
    }

    void init(int minx, int miny, int maxx, int maxy) {
        this.bboxX0 = minx;
        this.bboxY0 = miny;
        this.bboxX1 = maxx;
        this.bboxY1 = maxy;
        int width = maxx - minx;
        this.useRLE = FORCE_NO_RLE ? false : (FORCE_RLE ? true : width > RLE_MIN_WIDTH && width < 0x800000);
        int nxTiles = width + TILE_W >> TILE_W_LG;
        if (nxTiles > 256) {
            if (DO_STATS) {
                this.rdrStats.stat_array_marlincache_touchedTile.add(nxTiles);
            }
            this.touchedTile = this.touchedTile_ref.getArray(nxTiles);
        }
    }

    void dispose() {
        this.resetTileLine(0);
        if (DO_STATS) {
            this.rdrStats.totalOffHeap += this.rowAAChunk.length;
        }
        if (this.touchedTile_ref.doSetRef(this.touchedTile)) {
            this.touchedTile = this.touchedTile_ref.putArrayClean(this.touchedTile);
        }
        if (this.rowAAChunk.length != INITIAL_CHUNK_ARRAY) {
            this.rowAAChunk.resize(INITIAL_CHUNK_ARRAY);
        }
    }

    void resetTileLine(int pminY) {
        this.bboxY0 = pminY;
        if (DO_STATS) {
            this.rdrStats.stat_cache_rowAAChunk.add(this.rowAAChunkPos);
        }
        this.rowAAChunkPos = 0L;
        if (this.tileMin != Integer.MAX_VALUE) {
            if (DO_STATS) {
                this.rdrStats.stat_cache_tiles.add(this.tileMax - this.tileMin);
            }
            if (this.tileMax == 1) {
                this.touchedTile[0] = 0;
            } else {
                ArrayCacheInt.fill(this.touchedTile, this.tileMin, this.tileMax, 0);
            }
            this.tileMin = Integer.MAX_VALUE;
            this.tileMax = Integer.MIN_VALUE;
        }
    }

    void clearAARow(int y) {
        int row = y - this.bboxY0;
        this.rowAAx0[row] = 0;
        this.rowAAx1[row] = 0;
        this.rowAAEnc[row] = 0;
    }

    void copyAARowNoRLE(int[] alphaRow, int y, int px0, int px1) {
        long needSize;
        long pos;
        int px_bbox1 = FloatMath.min(px1, this.bboxX1);
        if (DO_LOG_BOUNDS) {
            MarlinUtils.logInfo("row = [" + px0 + " ... " + px_bbox1 + " (" + px1 + ") [ for y=" + y);
        }
        int row = y - this.bboxY0;
        this.rowAAx0[row] = px0;
        this.rowAAx1[row] = px_bbox1;
        this.rowAAEnc[row] = 0;
        this.rowAAChunkIndex[row] = pos = this.rowAAChunkPos;
        this.rowAAChunkPos = needSize = pos + (long)(px_bbox1 - px0 + 3 & 0xFFFFFFFC);
        OffHeapArray _rowAAChunk = this.rowAAChunk;
        if (_rowAAChunk.length < needSize) {
            this.expandRowAAChunk(needSize);
        }
        if (DO_STATS) {
            this.rdrStats.stat_cache_rowAA.add(px_bbox1 - px0);
        }
        int[] _touchedTile = this.touchedTile;
        int _TILE_SIZE_LG = TILE_W_LG;
        int from = px0 - this.bboxX0;
        int to = px_bbox1 - this.bboxX0;
        Unsafe _unsafe = OffHeapArray.UNSAFE;
        long SIZE_BYTE = 1L;
        long addr_alpha = MarlinCache.ALPHA_MAP_UNSAFE.address;
        long addr_off = _rowAAChunk.address + pos;
        int val = 0;
        for (int x = from; x < to; ++x) {
            if ((val += alphaRow[x]) == 0) {
                _unsafe.putByte(addr_off, (byte)0);
            } else {
                _unsafe.putByte(addr_off, _unsafe.getByte(addr_alpha + (long)val));
                int n = x >> _TILE_SIZE_LG;
                _touchedTile[n] = _touchedTile[n] + val;
            }
            ++addr_off;
        }
        int tx = from >> _TILE_SIZE_LG;
        if (tx < this.tileMin) {
            this.tileMin = tx;
        }
        if ((tx = (to - 1 >> _TILE_SIZE_LG) + 1) > this.tileMax) {
            this.tileMax = tx;
        }
        if (DO_LOG_BOUNDS) {
            MarlinUtils.logInfo("clear = [" + from + " ... " + to + "[");
        }
        ArrayCacheInt.fill(alphaRow, from, px1 + 1 - this.bboxX0, 0);
    }

    void copyAARowRLE_WithBlockFlags(int[] blkFlags, int[] alphaRow, int y, int px0, int px1) {
        int tx;
        int runLen;
        int _bboxX0 = this.bboxX0;
        int row = y - this.bboxY0;
        int from = px0 - _bboxX0;
        int px_bbox1 = FloatMath.min(px1, this.bboxX1);
        int to = px_bbox1 - _bboxX0;
        if (DO_LOG_BOUNDS) {
            MarlinUtils.logInfo("row = [" + px0 + " ... " + px_bbox1 + " (" + px1 + ") [ for y=" + y);
        }
        long initialPos = this.startRLERow(row, px0, px_bbox1);
        long needSize = initialPos + (long)(to - from << 2);
        OffHeapArray _rowAAChunk = this.rowAAChunk;
        if (_rowAAChunk.length < needSize) {
            this.expandRowAAChunk(needSize);
        }
        Unsafe _unsafe = OffHeapArray.UNSAFE;
        long SIZE_INT = 4L;
        long addr_alpha = MarlinCache.ALPHA_MAP_UNSAFE.address;
        long addr_off = _rowAAChunk.address + initialPos;
        int[] _touchedTile = this.touchedTile;
        int _TILE_SIZE_LG = TILE_W_LG;
        int _BLK_SIZE_LG = BLOCK_SIZE_LG;
        int blkW = from >> _BLK_SIZE_LG;
        int blkE = (to >> _BLK_SIZE_LG) + 1;
        blkFlags[blkE] = 0;
        int val = 0;
        int cx0 = from;
        int _MAX_VALUE = Integer.MAX_VALUE;
        int last_t0 = Integer.MAX_VALUE;
        int skip = 0;
        for (int t = blkW; t <= blkE; ++t) {
            if (blkFlags[t] != 0) {
                blkFlags[t] = 0;
                if (last_t0 != Integer.MAX_VALUE) continue;
                last_t0 = t;
                continue;
            }
            if (last_t0 != Integer.MAX_VALUE) {
                int blk_x0 = FloatMath.max(last_t0 << _BLK_SIZE_LG, from);
                last_t0 = Integer.MAX_VALUE;
                int blk_x1 = FloatMath.min((t << _BLK_SIZE_LG) + 1, to);
                for (int cx = blk_x0; cx < blk_x1; ++cx) {
                    int delta = alphaRow[cx];
                    if (delta == 0) continue;
                    alphaRow[cx] = 0;
                    if (cx != cx0) {
                        runLen = cx - cx0;
                        if (val == 0) {
                            _unsafe.putInt(addr_off, _bboxX0 + cx << 8);
                        } else {
                            _unsafe.putInt(addr_off, _bboxX0 + cx << 8 | _unsafe.getByte(addr_alpha + (long)val) & 0xFF);
                            if (runLen == 1) {
                                int n = cx0 >> _TILE_SIZE_LG;
                                _touchedTile[n] = _touchedTile[n] + val;
                            } else {
                                this.touchTile(cx0, val, cx, runLen, _touchedTile);
                            }
                        }
                        addr_off += 4L;
                        if (DO_STATS) {
                            this.rdrStats.hist_tile_generator_encoding_runLen.add(runLen);
                        }
                        cx0 = cx;
                    }
                    val += delta;
                }
                continue;
            }
            if (!DO_STATS) continue;
            ++skip;
        }
        runLen = to - cx0;
        if (val == 0) {
            _unsafe.putInt(addr_off, _bboxX0 + to << 8);
        } else {
            _unsafe.putInt(addr_off, _bboxX0 + to << 8 | _unsafe.getByte(addr_alpha + (long)val) & 0xFF);
            if (runLen == 1) {
                int n = cx0 >> _TILE_SIZE_LG;
                _touchedTile[n] = _touchedTile[n] + val;
            } else {
                this.touchTile(cx0, val, to, runLen, _touchedTile);
            }
        }
        addr_off += 4L;
        if (DO_STATS) {
            this.rdrStats.hist_tile_generator_encoding_runLen.add(runLen);
        }
        long len = addr_off - _rowAAChunk.address;
        this.rowAALen[row] = len - initialPos;
        this.rowAAChunkPos = len;
        if (DO_STATS) {
            this.rdrStats.stat_cache_rowAA.add(this.rowAALen[row]);
            this.rdrStats.hist_tile_generator_encoding_ratio.add(100 * skip / (blkE - blkW));
        }
        if ((tx = from >> _TILE_SIZE_LG) < this.tileMin) {
            this.tileMin = tx;
        }
        if ((tx = (to - 1 >> _TILE_SIZE_LG) + 1) > this.tileMax) {
            this.tileMax = tx;
        }
        alphaRow[to] = 0;
        if (DO_CHECKS) {
            ArrayCacheInt.check(blkFlags, blkW, blkE, 0);
            ArrayCacheInt.check(alphaRow, from, px1 + 1 - this.bboxX0, 0);
        }
    }

    long startRLERow(int row, int x0, int x1) {
        this.rowAAx0[row] = x0;
        this.rowAAx1[row] = x1;
        this.rowAAEnc[row] = 1;
        this.rowAAPos[row] = 0L;
        this.rowAAChunkIndex[row] = this.rowAAChunkPos;
        return this.rowAAChunkIndex[row];
    }

    private void expandRowAAChunk(long needSize) {
        if (DO_STATS) {
            this.rdrStats.stat_array_marlincache_rowAAChunk.add(needSize);
        }
        long newSize = ArrayCacheConst.getNewLargeSize(this.rowAAChunk.length, needSize);
        this.rowAAChunk.resize(newSize);
    }

    private void touchTile(int x0, int val, int x1, int runLen, int[] _touchedTile) {
        int _TILE_SIZE_LG = TILE_W_LG;
        int tx = x0 >> _TILE_SIZE_LG;
        if (tx == x1 >> _TILE_SIZE_LG) {
            int n = tx;
            _touchedTile[n] = _touchedTile[n] + val * runLen;
            return;
        }
        int tx1 = x1 - 1 >> _TILE_SIZE_LG;
        if (tx <= tx1) {
            int nextTileXCoord = tx + 1 << _TILE_SIZE_LG;
            int n = tx++;
            _touchedTile[n] = _touchedTile[n] + val * (nextTileXCoord - x0);
        }
        if (tx < tx1) {
            int tileVal = val << _TILE_SIZE_LG;
            while (tx < tx1) {
                int n = tx++;
                _touchedTile[n] = _touchedTile[n] + tileVal;
            }
        }
        if (tx == tx1) {
            int txXCoord = tx << _TILE_SIZE_LG;
            int nextTileXCoord = tx + 1 << _TILE_SIZE_LG;
            int lastXCoord = nextTileXCoord <= x1 ? nextTileXCoord : x1;
            int n = tx;
            _touchedTile[n] = _touchedTile[n] + val * (lastXCoord - txXCoord);
        }
    }

    int alphaSumInTile(int x) {
        return this.touchedTile[x - this.bboxX0 >> TILE_W_LG];
    }

    public String toString() {
        return "bbox = [" + this.bboxX0 + ", " + this.bboxY0 + " => " + this.bboxX1 + ", " + this.bboxY1 + "]\n";
    }

    private static byte[] buildAlphaMap(int maxalpha) {
        byte[] alMap = new byte[maxalpha << 1];
        int halfmaxalpha = maxalpha >> 2;
        for (int i = 0; i <= maxalpha; ++i) {
            alMap[i] = (byte)((i * 255 + halfmaxalpha) / maxalpha);
        }
        return alMap;
    }

    static {
        byte[] _ALPHA_MAP = MarlinCache.buildAlphaMap(MAX_AA_ALPHA);
        ALPHA_MAP_UNSAFE = new OffHeapArray(_ALPHA_MAP, _ALPHA_MAP.length);
        ALPHA_MAP = _ALPHA_MAP;
        Unsafe _unsafe = OffHeapArray.UNSAFE;
        long addr = MarlinCache.ALPHA_MAP_UNSAFE.address;
        for (int i = 0; i < _ALPHA_MAP.length; ++i) {
            _unsafe.putByte(addr + (long)i, _ALPHA_MAP[i]);
        }
    }
}

