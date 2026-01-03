/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import sun.java2d.marlin.ArrayCacheConst;
import sun.java2d.marlin.MarlinConst;
import sun.java2d.marlin.MarlinUtils;

final class ArrayCacheDouble {
    private final int bucketCapacity;
    private WeakReference<Bucket[]> refBuckets = null;
    final ArrayCacheConst.CacheStats stats;

    ArrayCacheDouble(int bucketCapacity) {
        this.bucketCapacity = bucketCapacity;
        this.stats = MarlinConst.DO_STATS ? new ArrayCacheConst.CacheStats("ArrayCacheDouble(Dirty)") : null;
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

    static double[] createArray(int length) {
        return new double[length];
    }

    static void fill(double[] array, int fromIndex, int toIndex, double value) {
        Arrays.fill(array, fromIndex, toIndex, value);
        if (MarlinConst.DO_CHECKS) {
            ArrayCacheDouble.check(array, fromIndex, toIndex, value);
        }
    }

    static void check(double[] array, int fromIndex, int toIndex, double value) {
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
        private final double[][] arrays;
        private final ArrayCacheConst.BucketStats stats;

        Bucket(int arraySize, int capacity, ArrayCacheConst.BucketStats stats) {
            this.arraySize = arraySize;
            this.stats = stats;
            this.arrays = new double[capacity][];
        }

        double[] getArray() {
            if (MarlinConst.DO_STATS) {
                ++this.stats.getOp;
            }
            if (this.tail != 0) {
                double[] array = this.arrays[--this.tail];
                this.arrays[this.tail] = null;
                return array;
            }
            if (MarlinConst.DO_STATS) {
                ++this.stats.createOp;
            }
            return ArrayCacheDouble.createArray(this.arraySize);
        }

        void putArray(double[] array) {
            if (MarlinConst.DO_CHECKS && array.length != this.arraySize) {
                MarlinUtils.logInfo("ArrayCacheDouble(Dirty): bad length = " + array.length);
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
                MarlinUtils.logInfo("ArrayCacheDouble(Dirty): array capacity exceeded !");
            }
        }
    }

    static final class Reference {
        final double[] initial;
        private final ArrayCacheDouble cache;

        Reference(ArrayCacheDouble cache, int initialSize) {
            this.cache = cache;
            this.initial = ArrayCacheDouble.createArray(initialSize);
            if (MarlinConst.DO_STATS) {
                cache.stats.totalInitial += (long)initialSize;
            }
        }

        double[] getArray(int length) {
            if (length <= ArrayCacheConst.MAX_ARRAY_SIZE) {
                return this.cache.getCacheBucket(length).getArray();
            }
            if (MarlinConst.DO_STATS) {
                ++this.cache.stats.oversize;
            }
            if (MarlinConst.DO_LOG_OVERSIZE) {
                MarlinUtils.logInfo("ArrayCacheDouble(Dirty): getArray[oversize]: length=\t" + length);
            }
            return ArrayCacheDouble.createArray(length);
        }

        double[] widenArray(double[] array, int usedSize, int needSize) {
            int length = array.length;
            if (MarlinConst.DO_CHECKS && length >= needSize) {
                return array;
            }
            if (MarlinConst.DO_STATS) {
                ++this.cache.stats.resize;
            }
            double[] res = this.getArray(ArrayCacheConst.getNewSize(usedSize, needSize));
            System.arraycopy(array, 0, res, 0, usedSize);
            this.putArray(array, 0, usedSize);
            if (MarlinConst.DO_LOG_WIDEN_ARRAY) {
                MarlinUtils.logInfo("ArrayCacheDouble(Dirty): widenArray[" + res.length + "]: usedSize=\t" + usedSize + "\tlength=\t" + length + "\tneeded length=\t" + needSize);
            }
            return res;
        }

        boolean doCleanRef(double[] array) {
            return array != this.initial;
        }

        double[] putArray(double[] array) {
            return this.putArray(array, 0, array.length);
        }

        double[] putArray(double[] array, int fromIndex, int toIndex) {
            if (array.length <= ArrayCacheConst.MAX_ARRAY_SIZE && array != this.initial) {
                this.cache.getCacheBucket(array.length).putArray(array);
            }
            return this.initial;
        }
    }
}

