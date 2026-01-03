/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.lang.reflect.Array;
import java.util.Comparator;

class TimSort<T> {
    private static final int MIN_MERGE = 32;
    private final T[] a;
    private final Comparator<? super T> c;
    private static final int MIN_GALLOP = 7;
    private int minGallop = 7;
    private static final int INITIAL_TMP_STORAGE_LENGTH = 256;
    private T[] tmp;
    private int tmpBase;
    private int tmpLen;
    private int stackSize = 0;
    private final int[] runBase;
    private final int[] runLen;

    private TimSort(T[] a, Comparator<? super T> c, T[] work, int workBase, int workLen) {
        int tlen;
        this.a = a;
        this.c = c;
        int len = a.length;
        int n = tlen = len < 512 ? len >>> 1 : 256;
        if (work == null || workLen < tlen || workBase + tlen > work.length) {
            Object[] newArray = (Object[])Array.newInstance(a.getClass().getComponentType(), tlen);
            this.tmp = newArray;
            this.tmpBase = 0;
            this.tmpLen = tlen;
        } else {
            this.tmp = work;
            this.tmpBase = workBase;
            this.tmpLen = workLen;
        }
        int stackLen = len < 120 ? 5 : (len < 1542 ? 10 : (len < 119151 ? 24 : 49));
        this.runBase = new int[stackLen];
        this.runLen = new int[stackLen];
    }

    static <T> void sort(T[] a, int lo, int hi, Comparator<? super T> c, T[] work, int workBase, int workLen) {
        int runLen;
        assert (c != null && a != null && lo >= 0 && lo <= hi && hi <= a.length);
        int nRemaining = hi - lo;
        if (nRemaining < 2) {
            return;
        }
        if (nRemaining < 32) {
            int initRunLen = TimSort.countRunAndMakeAscending(a, lo, hi, c);
            TimSort.binarySort(a, lo, hi, lo + initRunLen, c);
            return;
        }
        TimSort<T> ts = new TimSort<T>(a, c, work, workBase, workLen);
        int minRun = TimSort.minRunLength(nRemaining);
        do {
            if ((runLen = TimSort.countRunAndMakeAscending(a, lo, hi, c)) < minRun) {
                int force = nRemaining <= minRun ? nRemaining : minRun;
                TimSort.binarySort(a, lo, lo + force, lo + runLen, c);
                runLen = force;
            }
            ts.pushRun(lo, runLen);
            ts.mergeCollapse();
            lo += runLen;
        } while ((nRemaining -= runLen) != 0);
        assert (lo == hi);
        ts.mergeForceCollapse();
        assert (ts.stackSize == 1);
    }

    private static <T> void binarySort(T[] a, int lo, int hi, int start, Comparator<? super T> c) {
        assert (lo <= start && start <= hi);
        if (start == lo) {
            ++start;
        }
        while (start < hi) {
            T pivot = a[start];
            int left = lo;
            int right = start;
            assert (left <= right);
            while (left < right) {
                int mid = left + right >>> 1;
                if (c.compare(pivot, a[mid]) < 0) {
                    right = mid;
                    continue;
                }
                left = mid + 1;
            }
            assert (left == right);
            int n = start - left;
            switch (n) {
                case 2: {
                    a[left + 2] = a[left + 1];
                }
                case 1: {
                    a[left + 1] = a[left];
                    break;
                }
                default: {
                    System.arraycopy(a, left, a, left + 1, n);
                }
            }
            a[left] = pivot;
            ++start;
        }
    }

    private static <T> int countRunAndMakeAscending(T[] a, int lo, int hi, Comparator<? super T> c) {
        assert (lo < hi);
        int runHi = lo + 1;
        if (runHi == hi) {
            return 1;
        }
        if (c.compare(a[runHi++], a[lo]) < 0) {
            while (runHi < hi && c.compare(a[runHi], a[runHi - 1]) < 0) {
                ++runHi;
            }
            TimSort.reverseRange(a, lo, runHi);
        } else {
            while (runHi < hi && c.compare(a[runHi], a[runHi - 1]) >= 0) {
                ++runHi;
            }
        }
        return runHi - lo;
    }

    private static void reverseRange(Object[] a, int lo, int hi) {
        --hi;
        while (lo < hi) {
            Object t = a[lo];
            a[lo++] = a[hi];
            a[hi--] = t;
        }
    }

    private static int minRunLength(int n) {
        assert (n >= 0);
        int r = 0;
        while (n >= 32) {
            r |= n & 1;
            n >>= 1;
        }
        return n + r;
    }

    private void pushRun(int runBase, int runLen) {
        this.runBase[this.stackSize] = runBase;
        this.runLen[this.stackSize] = runLen;
        ++this.stackSize;
    }

    private void mergeCollapse() {
        while (this.stackSize > 1) {
            int n = this.stackSize - 2;
            if (n > 0 && this.runLen[n - 1] <= this.runLen[n] + this.runLen[n + 1] || n > 1 && this.runLen[n - 2] <= this.runLen[n] + this.runLen[n - 1]) {
                if (this.runLen[n - 1] < this.runLen[n + 1]) {
                    --n;
                }
            } else if (n < 0 || this.runLen[n] > this.runLen[n + 1]) break;
            this.mergeAt(n);
        }
    }

    private void mergeForceCollapse() {
        while (this.stackSize > 1) {
            int n = this.stackSize - 2;
            if (n > 0 && this.runLen[n - 1] < this.runLen[n + 1]) {
                --n;
            }
            this.mergeAt(n);
        }
    }

    private void mergeAt(int i) {
        assert (this.stackSize >= 2);
        assert (i >= 0);
        assert (i == this.stackSize - 2 || i == this.stackSize - 3);
        int base1 = this.runBase[i];
        int len1 = this.runLen[i];
        int base2 = this.runBase[i + 1];
        int len2 = this.runLen[i + 1];
        assert (len1 > 0 && len2 > 0);
        assert (base1 + len1 == base2);
        this.runLen[i] = len1 + len2;
        if (i == this.stackSize - 3) {
            this.runBase[i + 1] = this.runBase[i + 2];
            this.runLen[i + 1] = this.runLen[i + 2];
        }
        --this.stackSize;
        int k = TimSort.gallopRight(this.a[base2], this.a, base1, len1, 0, this.c);
        assert (k >= 0);
        base1 += k;
        if ((len1 -= k) == 0) {
            return;
        }
        len2 = TimSort.gallopLeft(this.a[base1 + len1 - 1], this.a, base2, len2, len2 - 1, this.c);
        assert (len2 >= 0);
        if (len2 == 0) {
            return;
        }
        if (len1 <= len2) {
            this.mergeLo(base1, len1, base2, len2);
        } else {
            this.mergeHi(base1, len1, base2, len2);
        }
    }

    private static <T> int gallopLeft(T key, T[] a, int base, int len, int hint, Comparator<? super T> c) {
        assert (len > 0 && hint >= 0 && hint < len);
        int lastOfs = 0;
        int ofs = 1;
        if (c.compare(key, a[base + hint]) > 0) {
            maxOfs = len - hint;
            while (ofs < maxOfs && c.compare(key, a[base + hint + ofs]) > 0) {
                lastOfs = ofs;
                if ((ofs = (ofs << 1) + 1) > 0) continue;
                ofs = maxOfs;
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        } else {
            maxOfs = hint + 1;
            while (ofs < maxOfs && c.compare(key, a[base + hint - ofs]) <= 0) {
                lastOfs = ofs;
                if ((ofs = (ofs << 1) + 1) > 0) continue;
                ofs = maxOfs;
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmp = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmp;
        }
        assert (-1 <= lastOfs && lastOfs < ofs && ofs <= len);
        ++lastOfs;
        while (lastOfs < ofs) {
            int m = lastOfs + (ofs - lastOfs >>> 1);
            if (c.compare(key, a[base + m]) > 0) {
                lastOfs = m + 1;
                continue;
            }
            ofs = m;
        }
        assert (lastOfs == ofs);
        return ofs;
    }

    private static <T> int gallopRight(T key, T[] a, int base, int len, int hint, Comparator<? super T> c) {
        assert (len > 0 && hint >= 0 && hint < len);
        int ofs = 1;
        int lastOfs = 0;
        if (c.compare(key, a[base + hint]) < 0) {
            maxOfs = hint + 1;
            while (ofs < maxOfs && c.compare(key, a[base + hint - ofs]) < 0) {
                lastOfs = ofs;
                if ((ofs = (ofs << 1) + 1) > 0) continue;
                ofs = maxOfs;
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            int tmp = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmp;
        } else {
            maxOfs = len - hint;
            while (ofs < maxOfs && c.compare(key, a[base + hint + ofs]) >= 0) {
                lastOfs = ofs;
                if ((ofs = (ofs << 1) + 1) > 0) continue;
                ofs = maxOfs;
            }
            if (ofs > maxOfs) {
                ofs = maxOfs;
            }
            lastOfs += hint;
            ofs += hint;
        }
        assert (-1 <= lastOfs && lastOfs < ofs && ofs <= len);
        ++lastOfs;
        while (lastOfs < ofs) {
            int m = lastOfs + (ofs - lastOfs >>> 1);
            if (c.compare(key, a[base + m]) < 0) {
                ofs = m;
                continue;
            }
            lastOfs = m + 1;
        }
        assert (lastOfs == ofs);
        return ofs;
    }

    private void mergeLo(int base1, int len1, int base2, int len2) {
        assert (len1 > 0 && len2 > 0 && base1 + len1 == base2);
        T[] a = this.a;
        T[] tmp = this.ensureCapacity(len1);
        int cursor1 = this.tmpBase;
        int cursor2 = base2;
        int dest = base1;
        System.arraycopy(a, base1, tmp, cursor1, len1);
        a[dest++] = a[cursor2++];
        if (--len2 == 0) {
            System.arraycopy(tmp, cursor1, a, dest, len1);
            return;
        }
        if (len1 == 1) {
            System.arraycopy(a, cursor2, a, dest, len2);
            a[dest + len2] = tmp[cursor1];
            return;
        }
        Comparator<T> c = this.c;
        int minGallop = this.minGallop;
        block0: while (true) {
            int count1 = 0;
            int count2 = 0;
            do {
                assert (len1 > 1 && len2 > 0);
                if (c.compare(a[cursor2], tmp[cursor1]) < 0) {
                    a[dest++] = a[cursor2++];
                    ++count2;
                    count1 = 0;
                    if (--len2 != 0) continue;
                    break block0;
                }
                a[dest++] = tmp[cursor1++];
                ++count1;
                count2 = 0;
                if (--len1 == 1) break block0;
            } while ((count1 | count2) < minGallop);
            do {
                assert (len1 > 1 && len2 > 0);
                count1 = TimSort.gallopRight(a[cursor2], tmp, cursor1, len1, 0, c);
                if (count1 != 0) {
                    System.arraycopy(tmp, cursor1, a, dest, count1);
                    dest += count1;
                    cursor1 += count1;
                    if ((len1 -= count1) <= 1) break block0;
                }
                a[dest++] = a[cursor2++];
                if (--len2 == 0) break block0;
                count2 = TimSort.gallopLeft(tmp[cursor1], a, cursor2, len2, 0, c);
                if (count2 != 0) {
                    System.arraycopy(a, cursor2, a, dest, count2);
                    dest += count2;
                    cursor2 += count2;
                    if ((len2 -= count2) == 0) break block0;
                }
                a[dest++] = tmp[cursor1++];
                if (--len1 == 1) break block0;
                --minGallop;
            } while (count1 >= 7 | count2 >= 7);
            if (minGallop < 0) {
                minGallop = 0;
            }
            minGallop += 2;
        }
        int n = this.minGallop = minGallop < 1 ? 1 : minGallop;
        if (len1 == 1) {
            assert (len2 > 0);
            System.arraycopy(a, cursor2, a, dest, len2);
            a[dest + len2] = tmp[cursor1];
        } else {
            if (len1 == 0) {
                throw new IllegalArgumentException("Comparison method violates its general contract!");
            }
            assert (len2 == 0);
            assert (len1 > 1);
            System.arraycopy(tmp, cursor1, a, dest, len1);
        }
    }

    private void mergeHi(int base1, int len1, int base2, int len2) {
        assert (len1 > 0 && len2 > 0 && base1 + len1 == base2);
        T[] a = this.a;
        T[] tmp = this.ensureCapacity(len2);
        int tmpBase = this.tmpBase;
        System.arraycopy(a, base2, tmp, tmpBase, len2);
        int cursor1 = base1 + len1 - 1;
        int cursor2 = tmpBase + len2 - 1;
        int dest = base2 + len2 - 1;
        a[dest--] = a[cursor1--];
        if (--len1 == 0) {
            System.arraycopy(tmp, tmpBase, a, dest - (len2 - 1), len2);
            return;
        }
        if (len2 == 1) {
            System.arraycopy(a, (cursor1 -= len1) + 1, a, (dest -= len1) + 1, len1);
            a[dest] = tmp[cursor2];
            return;
        }
        Comparator<T> c = this.c;
        int minGallop = this.minGallop;
        block0: while (true) {
            int count1 = 0;
            int count2 = 0;
            do {
                assert (len1 > 0 && len2 > 1);
                if (c.compare(tmp[cursor2], a[cursor1]) < 0) {
                    a[dest--] = a[cursor1--];
                    ++count1;
                    count2 = 0;
                    if (--len1 != 0) continue;
                    break block0;
                }
                a[dest--] = tmp[cursor2--];
                ++count2;
                count1 = 0;
                if (--len2 == 1) break block0;
            } while ((count1 | count2) < minGallop);
            do {
                assert (len1 > 0 && len2 > 1);
                count1 = len1 - TimSort.gallopRight(tmp[cursor2], a, base1, len1, len1 - 1, c);
                if (count1 != 0) {
                    System.arraycopy(a, (cursor1 -= count1) + 1, a, (dest -= count1) + 1, count1);
                    if ((len1 -= count1) == 0) break block0;
                }
                a[dest--] = tmp[cursor2--];
                if (--len2 == 1) break block0;
                count2 = len2 - TimSort.gallopLeft(a[cursor1], tmp, tmpBase, len2, len2 - 1, c);
                if (count2 != 0) {
                    System.arraycopy(tmp, (cursor2 -= count2) + 1, a, (dest -= count2) + 1, count2);
                    if ((len2 -= count2) <= 1) break block0;
                }
                a[dest--] = a[cursor1--];
                if (--len1 == 0) break block0;
                --minGallop;
            } while (count1 >= 7 | count2 >= 7);
            if (minGallop < 0) {
                minGallop = 0;
            }
            minGallop += 2;
        }
        int n = this.minGallop = minGallop < 1 ? 1 : minGallop;
        if (len2 == 1) {
            assert (len1 > 0);
            System.arraycopy(a, (cursor1 -= len1) + 1, a, (dest -= len1) + 1, len1);
            a[dest] = tmp[cursor2];
        } else {
            if (len2 == 0) {
                throw new IllegalArgumentException("Comparison method violates its general contract!");
            }
            assert (len1 == 0);
            assert (len2 > 0);
            System.arraycopy(tmp, tmpBase, a, dest - (len2 - 1), len2);
        }
    }

    private T[] ensureCapacity(int minCapacity) {
        if (this.tmpLen < minCapacity) {
            int newSize = -1 >>> Integer.numberOfLeadingZeros(minCapacity);
            newSize = ++newSize < 0 ? minCapacity : Math.min(newSize, this.a.length >>> 1);
            Object[] newArray = (Object[])Array.newInstance(this.a.getClass().getComponentType(), newSize);
            this.tmp = newArray;
            this.tmpLen = newSize;
            this.tmpBase = 0;
        }
        return this.tmp;
    }
}

