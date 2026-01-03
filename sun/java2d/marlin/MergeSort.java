/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import java.util.Arrays;
import sun.java2d.marlin.DPQSSorterContext;
import sun.java2d.marlin.DualPivotQuicksort20191112Ext;
import sun.java2d.marlin.MarlinProperties;
import sun.java2d.marlin.MarlinUtils;

final class MergeSort {
    static final boolean USE_DPQS = MarlinProperties.isUseDPQS();
    static final String SORT_TYPE = USE_DPQS ? "DPQS_20191112" : "MERGE";
    static final int DPQS_THRESHOLD = 256;
    static final int DISABLE_ISORT_THRESHOLD = 1000;
    private static final boolean CHECK_SORTED = false;
    static final int INSERTION_SORT_THRESHOLD = 14;

    static void mergeSortNoCopy(int[] x, int[] y, int[] auxX, int[] auxY, int toIndex, int insertionSortIndex, boolean skipISort, DPQSSorterContext sorter, boolean useDPQS) {
        if (toIndex > x.length || toIndex > y.length || toIndex > auxX.length || toIndex > auxY.length) {
            throw new ArrayIndexOutOfBoundsException("bad arguments: toIndex=" + toIndex);
        }
        if (skipISort) {
            if (useDPQS) {
                DualPivotQuicksort20191112Ext.sort(sorter, x, auxX, y, auxY, 0, toIndex);
            } else {
                MergeSort.mergeSort(auxX, auxY, auxX, x, auxY, y, 0, toIndex);
            }
            return;
        }
        if (useDPQS) {
            DualPivotQuicksort20191112Ext.sort(sorter, auxX, x, auxY, y, insertionSortIndex, toIndex);
        } else {
            MergeSort.mergeSort(x, y, x, auxX, y, auxY, insertionSortIndex, toIndex);
        }
        int p = 0;
        int q = insertionSortIndex;
        for (int i = 0; i < toIndex; ++i) {
            if (q >= toIndex || p < insertionSortIndex && auxX[p] <= auxX[q]) {
                x[i] = auxX[p];
                y[i] = auxY[p];
                ++p;
                continue;
            }
            x[i] = auxX[q];
            y[i] = auxY[q];
            ++q;
        }
    }

    private static void mergeSort(int[] refX, int[] refY, int[] srcX, int[] dstX, int[] srcY, int[] dstY, int low, int high) {
        int length = high - low;
        if (length <= 14) {
            dstX[low] = refX[low];
            dstY[low] = refY[low];
            int i = low + 1;
            int j = low;
            while (i < high) {
                int x = refX[i];
                int y = refY[i];
                while (dstX[j] > x) {
                    dstX[j + 1] = dstX[j];
                    dstY[j + 1] = dstY[j];
                    if (j-- != low) continue;
                }
                dstX[j + 1] = x;
                dstY[j + 1] = y;
                j = i++;
            }
            return;
        }
        int mid = low + high >> 1;
        MergeSort.mergeSort(refX, refY, dstX, srcX, dstY, srcY, low, mid);
        MergeSort.mergeSort(refX, refY, dstX, srcX, dstY, srcY, mid, high);
        if (srcX[high - 1] <= srcX[low]) {
            int left = mid - low;
            int right = high - mid;
            int off = left != right ? 1 : 0;
            System.arraycopy(srcX, low, dstX, mid + off, left);
            System.arraycopy(srcX, mid, dstX, low, right);
            System.arraycopy(srcY, low, dstY, mid + off, left);
            System.arraycopy(srcY, mid, dstY, low, right);
            return;
        }
        if (srcX[mid - 1] <= srcX[mid]) {
            System.arraycopy(srcX, low, dstX, low, length);
            System.arraycopy(srcY, low, dstY, low, length);
            return;
        }
        int p = low;
        int q = mid;
        for (int i = low; i < high; ++i) {
            if (q >= high || p < mid && srcX[p] <= srcX[q]) {
                dstX[i] = srcX[p];
                dstY[i] = srcY[p];
                ++p;
                continue;
            }
            dstX[i] = srcX[q];
            dstY[i] = srcY[q];
            ++q;
        }
    }

    private MergeSort() {
    }

    private static void checkRange(int[] x, int lo, int hi) {
        for (int i = lo + 1; i < hi; ++i) {
            if (x[i - 1] <= x[i]) continue;
            MarlinUtils.logInfo("Bad sorted x [" + (i - 1) + "]" + Arrays.toString(Arrays.copyOf(x, hi)));
            return;
        }
    }

    static {
        MarlinUtils.logInfo("MergeSort: DPQS_THRESHOLD: 256");
        MarlinUtils.logInfo("MergeSort: DISABLE_ISORT_THRESHOLD: 1000");
    }
}

