/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import java.util.Arrays;
import sun.java2d.marlin.MarlinConst;
import sun.java2d.marlin.MarlinUtils;

public final class ArrayCacheConst
implements MarlinConst {
    static final int BUCKETS = 8;
    static final int MIN_ARRAY_SIZE = 4096;
    static final int MAX_ARRAY_SIZE;
    static final int THRESHOLD_SMALL_ARRAY_SIZE = 0x400000;
    static final int THRESHOLD_ARRAY_SIZE;
    static final long THRESHOLD_HUGE_ARRAY_SIZE;
    static final int[] ARRAY_SIZES;

    private ArrayCacheConst() {
    }

    static int getBucket(int length) {
        for (int i = 0; i < ARRAY_SIZES.length; ++i) {
            if (length > ARRAY_SIZES[i]) continue;
            return i;
        }
        return -1;
    }

    public static int getNewSize(int curSize, int needSize) {
        if (needSize < 0) {
            throw new ArrayIndexOutOfBoundsException("array exceeds maximum capacity !");
        }
        assert (curSize >= 0);
        int initial = curSize;
        int size = initial > THRESHOLD_ARRAY_SIZE ? initial + (initial >> 1) : initial << 1;
        if (size < needSize) {
            size = (needSize >> 12) + 1 << 12;
        }
        if (size < 0) {
            size = Integer.MAX_VALUE;
        }
        return size;
    }

    public static long getNewLargeSize(long curSize, long needSize) {
        if (needSize >> 31 != 0L) {
            throw new ArrayIndexOutOfBoundsException("array exceeds maximum capacity !");
        }
        assert (curSize >= 0L);
        long size = curSize > THRESHOLD_HUGE_ARRAY_SIZE ? curSize + (curSize >> 2) : (curSize > (long)THRESHOLD_ARRAY_SIZE ? curSize + (curSize >> 1) : (curSize > 0x400000L ? curSize << 1 : curSize << 2));
        if (size < needSize) {
            size = (needSize >> 12) + 1L << 12;
        }
        if (size > Integer.MAX_VALUE) {
            size = Integer.MAX_VALUE;
        }
        return size;
    }

    static {
        ARRAY_SIZES = new int[8];
        int arraySize = 4096;
        int inc_lg = 2;
        int i = 0;
        while (i < 8) {
            ArrayCacheConst.ARRAY_SIZES[i] = arraySize;
            if (DO_TRACE) {
                MarlinUtils.logInfo("arraySize[" + i + "]: " + arraySize);
            }
            if (arraySize >= 0x400000) {
                inc_lg = 1;
            }
            ++i;
            arraySize <<= inc_lg;
        }
        MAX_ARRAY_SIZE = arraySize >> inc_lg;
        if (MAX_ARRAY_SIZE <= 0) {
            throw new IllegalStateException("Invalid max array size !");
        }
        THRESHOLD_ARRAY_SIZE = 0x1000000;
        THRESHOLD_HUGE_ARRAY_SIZE = 0x3000000L;
        if (DO_STATS) {
            MarlinUtils.logInfo("ArrayCache.BUCKETS        = 8");
            MarlinUtils.logInfo("ArrayCache.MIN_ARRAY_SIZE = 4096");
            MarlinUtils.logInfo("ArrayCache.MAX_ARRAY_SIZE = " + MAX_ARRAY_SIZE);
            MarlinUtils.logInfo("ArrayCache.ARRAY_SIZES = " + Arrays.toString(ARRAY_SIZES));
            MarlinUtils.logInfo("ArrayCache.THRESHOLD_ARRAY_SIZE = " + THRESHOLD_ARRAY_SIZE);
            MarlinUtils.logInfo("ArrayCache.THRESHOLD_HUGE_ARRAY_SIZE = " + THRESHOLD_HUGE_ARRAY_SIZE);
        }
    }

    static final class BucketStats {
        int getOp = 0;
        int createOp = 0;
        int returnOp = 0;
        int maxSize = 0;

        BucketStats() {
        }

        void reset() {
            this.getOp = 0;
            this.createOp = 0;
            this.returnOp = 0;
            this.maxSize = 0;
        }

        void updateMaxSize(int size) {
            if (size > this.maxSize) {
                this.maxSize = size;
            }
        }
    }

    static final class CacheStats {
        final String name;
        final BucketStats[] bucketStats;
        int resize = 0;
        int oversize = 0;
        long totalInitial = 0L;

        CacheStats(String name) {
            this.name = name;
            this.bucketStats = new BucketStats[8];
            for (int i = 0; i < 8; ++i) {
                this.bucketStats[i] = new BucketStats();
            }
        }

        void reset() {
            this.resize = 0;
            this.oversize = 0;
            for (int i = 0; i < 8; ++i) {
                this.bucketStats[i].reset();
            }
        }

        long dumpStats() {
            long totalCacheBytes = 0L;
            if (MarlinConst.DO_STATS) {
                BucketStats s;
                int i;
                for (i = 0; i < 8; ++i) {
                    s = this.bucketStats[i];
                    if (s.maxSize == 0) continue;
                    totalCacheBytes += (long)(this.getByteFactor() * (s.maxSize * ARRAY_SIZES[i]));
                }
                if (this.totalInitial != 0L || totalCacheBytes != 0L || this.resize != 0 || this.oversize != 0) {
                    MarlinUtils.logInfo(this.name + ": resize: " + this.resize + " - oversize: " + this.oversize + " - initial: " + this.getTotalInitialBytes() + " bytes (" + this.totalInitial + " elements) - cache: " + totalCacheBytes + " bytes");
                }
                if (totalCacheBytes != 0L) {
                    MarlinUtils.logInfo(this.name + ": usage stats:");
                    for (i = 0; i < 8; ++i) {
                        s = this.bucketStats[i];
                        if (s.getOp == 0) continue;
                        MarlinUtils.logInfo("  Bucket[" + ARRAY_SIZES[i] + "]: get: " + s.getOp + " - put: " + s.returnOp + " - create: " + s.createOp + " :: max size: " + s.maxSize);
                    }
                }
            }
            return totalCacheBytes;
        }

        private int getByteFactor() {
            int factor = 1;
            if (this.name.contains("Int") || this.name.contains("Float")) {
                factor = 4;
            } else if (this.name.contains("Double")) {
                factor = 8;
            }
            return factor;
        }

        long getTotalInitialBytes() {
            return (long)this.getByteFactor() * this.totalInitial;
        }
    }
}

