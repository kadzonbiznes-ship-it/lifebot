/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import sun.java2d.marlin.ArrayCacheConst;
import sun.java2d.marlin.MarlinConst;
import sun.java2d.marlin.MarlinUtils;

final class ArrayCacheByte {
    private final int bucketCapacity;
    private WeakReference<Bucket[]> refBuckets = null;
    final ArrayCacheConst.CacheStats stats;

    ArrayCacheByte(int bucketCapacity) {
        this.bucketCapacity = bucketCapacity;
        this.stats = MarlinConst.DO_STATS ? new ArrayCacheConst.CacheStats("ArrayCacheByte(Dirty)") : null;
    }

    Bucket getCacheBucket(int length) {
        int bucket = ArrayCacheConst.getBucket(length);
        return this.getBuckets()[bucket];
    }

    private Bucket[] getBuckets() {
        Bucket[] buckets;
        Bucket[] bucketArray = buckets = this.refBuckets != null ? (Bucket[])this.refBuckets.get() : null;
        if (buckets == null) {
            buckets = new Bucket[8];
            for (int i = 0; i < 8; ++i) {
                buckets[i] = new Bucket(ArrayCacheConst.ARRAY_SIZES[i], this.bucketCapacity, MarlinConst.DO_STATS ? this.stats.bucketStats[i] : null);
            }
            this.refBuckets = new WeakReference<Bucket[]>(buckets);
        }
        return buckets;
    }

    Reference createRef(int initialSize) {
        return new Reference(this, initialSize);
    }

    static byte[] createArray(int length) {
        return new byte[length];
    }

    static void fill(byte[] array, int fromIndex, int toIndex, byte value) {
        Arrays.fill(array, fromIndex, toIndex, value);
        if (MarlinConst.DO_CHECKS) {
            ArrayCacheByte.check(array, fromIndex, toIndex, value);
        }
    }

    static void check(byte[] array, int fromIndex, int toIndex, byte value) {
        if (MarlinConst.DO_CHECKS) {
            for (int i = 0; i < array.length; ++i) {
                if (array[i] == value) continue;
                MarlinUtils.logException("Invalid value at: " + i + " = " + array[i] + " from: " + fromIndex + " to: " + toIndex + "\n" + Arrays.toString(array), new Throwable());
                Arrays.fill(array, value);
                return;
            }
        }
    }

    static final class Bucket {
        private int tail = 0;
        private final int arraySize;
        private final byte[][] arrays;
        private final ArrayCacheConst.BucketStats stats;

        Bucket(int arraySize, int capacity, ArrayCacheConst.BucketStats stats) {
            this.arraySize = arraySize;
            this.stats = stats;
            this.arrays = new byte[capacity][];
        }

        byte[] getArray() {
            if (MarlinConst.DO_STATS) {
                ++this.stats.getOp;
            }
            if (this.tail != 0) {
                byte[] array = this.arrays[--this.tail];
                this.arrays[this.tail] = null;
                return array;
            }
            if (MarlinConst.DO_STATS) {
                ++this.stats.createOp;
            }
            return ArrayCacheByte.createArray(this.arraySize);
        }

        void putArray(byte[] array) {
            if (MarlinConst.DO_CHECKS && array.length != this.arraySize) {
                MarlinUtils.logInfo("ArrayCacheByte(Dirty): bad length = " + array.length);
                return;
            }
            if (MarlinConst.DO_STATS) {
                ++this.stats.returnOp;
            }
            if (this.arrays.length > this.tail) {
                this.arrays[this.tail++] = array;
                if (MarlinConst.DO_STATS) {
                    this.stats.updateMaxSize(this.tail);
                }
            } else if (MarlinConst.DO_CHECKS) {
                MarlinUtils.logInfo("ArrayCacheByte(Dirty): array capacity exceeded !");
            }
        }
    }

    static final class Reference {
        final byte[] initial;
        private final ArrayCacheByte cache;

        Reference(ArrayCacheByte cache, int initialSize) {
            this.cache = cache;
            this.initial = ArrayCacheByte.createArray(initialSize);
            if (MarlinConst.DO_STATS) {
                cache.stats.totalInitial += (long)initialSize;
            }
        }

        byte[] getArray(int length) {
            if (length <= ArrayCacheConst.MAX_ARRAY_SIZE) {
                return this.cache.getCacheBucket(length).getArray();
            }
            if (MarlinConst.DO_STATS) {
                ++this.cache.stats.oversize;
            }
            if (MarlinConst.DO_LOG_OVERSIZE) {
                MarlinUtils.logInfo("ArrayCacheByte(Dirty): getArray[oversize]: length=\t" + length);
            }
            return ArrayCacheByte.createArray(length);
        }

        byte[] widenArray(byte[] array, int usedSize, int needSize) {
            int length = array.length;
            if (MarlinConst.DO_CHECKS && length >= needSize) {
                return array;
            }
            if (MarlinConst.DO_STATS) {
                ++this.cache.stats.resize;
            }
            byte[] res = this.getArray(ArrayCacheConst.getNewSize(usedSize, needSize));
            System.arraycopy(array, 0, res, 0, usedSize);
            this.putArray(array, 0, usedSize);
            if (MarlinConst.DO_LOG_WIDEN_ARRAY) {
                MarlinUtils.logInfo("ArrayCacheByte(Dirty): widenArray[" + res.length + "]: usedSize=\t" + usedSize + "\tlength=\t" + length + "\tneeded length=\t" + needSize);
            }
            return res;
        }

        boolean doCleanRef(byte[] array) {
            return array != this.initial;
        }

        byte[] putArray(byte[] array) {
            return this.putArray(array, 0, array.length);
        }

        byte[] putArray(byte[] array, int fromIndex, int toIndex) {
            if (array.length <= ArrayCacheConst.MAX_ARRAY_SIZE && array != this.initial) {
                this.cache.getCacheBucket(array.length).putArray(array);
            }
            return this.initial;
        }
    }
}

