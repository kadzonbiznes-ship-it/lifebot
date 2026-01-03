/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.math;

import java.math.BigInteger;
import java.util.Arrays;
import jdk.internal.misc.CDS;

public class FDBigInteger {
    static final int[] SMALL_5_POW;
    static final long[] LONG_5_POW;
    private static final int MAX_FIVE_POW = 340;
    private static final FDBigInteger[] POW_5_CACHE;
    public static final FDBigInteger ZERO;
    private static Object[] archivedCaches;
    private static final long LONG_MASK = 0xFFFFFFFFL;
    private int[] data;
    private int offset;
    private int nWords;
    private boolean isImmutable = false;

    private FDBigInteger(int[] data, int offset) {
        this.data = data;
        this.offset = offset;
        this.nWords = data.length;
        this.trimLeadingZeros();
    }

    public FDBigInteger(long lValue, char[] digits, int kDigits, int nDigits) {
        int v;
        int n = Math.max((nDigits + 8) / 9, 2);
        this.data = new int[n];
        this.data[0] = (int)lValue;
        this.data[1] = (int)(lValue >>> 32);
        this.offset = 0;
        this.nWords = 2;
        int i = kDigits;
        int limit = nDigits - 5;
        while (i < limit) {
            int ilim = i + 5;
            v = digits[i++] - 48;
            while (i < ilim) {
                v = 10 * v + digits[i++] - 48;
            }
            this.multAddMe(100000, v);
        }
        int factor = 1;
        v = 0;
        while (i < nDigits) {
            v = 10 * v + digits[i++] - 48;
            factor *= 10;
        }
        if (factor != 1) {
            this.multAddMe(factor, v);
        }
        this.trimLeadingZeros();
    }

    public static FDBigInteger valueOfPow52(int p5, int p2) {
        if (p5 != 0) {
            if (p2 == 0) {
                return FDBigInteger.big5pow(p5);
            }
            if (p5 < SMALL_5_POW.length) {
                int pow5 = SMALL_5_POW[p5];
                int wordcount = p2 >> 5;
                int bitcount = p2 & 0x1F;
                if (bitcount == 0) {
                    return new FDBigInteger(new int[]{pow5}, wordcount);
                }
                return new FDBigInteger(new int[]{pow5 << bitcount, pow5 >>> 32 - bitcount}, wordcount);
            }
            return FDBigInteger.big5pow(p5).leftShift(p2);
        }
        return FDBigInteger.valueOfPow2(p2);
    }

    public static FDBigInteger valueOfMulPow52(long value, int p5, int p2) {
        assert (p5 >= 0) : p5;
        assert (p2 >= 0) : p2;
        int v0 = (int)value;
        int v1 = (int)(value >>> 32);
        int wordcount = p2 >> 5;
        int bitcount = p2 & 0x1F;
        if (p5 != 0) {
            int[] r;
            if (p5 < SMALL_5_POW.length) {
                long pow5 = (long)SMALL_5_POW[p5] & 0xFFFFFFFFL;
                long carry = ((long)v0 & 0xFFFFFFFFL) * pow5;
                v0 = (int)carry;
                carry >>>= 32;
                carry = ((long)v1 & 0xFFFFFFFFL) * pow5 + carry;
                v1 = (int)carry;
                int v2 = (int)(carry >>> 32);
                if (bitcount == 0) {
                    return new FDBigInteger(new int[]{v0, v1, v2}, wordcount);
                }
                return new FDBigInteger(new int[]{v0 << bitcount, v1 << bitcount | v0 >>> 32 - bitcount, v2 << bitcount | v1 >>> 32 - bitcount, v2 >>> 32 - bitcount}, wordcount);
            }
            FDBigInteger pow5 = FDBigInteger.big5pow(p5);
            if (v1 == 0) {
                r = new int[pow5.nWords + 1 + (p2 != 0 ? 1 : 0)];
                FDBigInteger.mult(pow5.data, pow5.nWords, v0, r);
            } else {
                r = new int[pow5.nWords + 2 + (p2 != 0 ? 1 : 0)];
                FDBigInteger.mult(pow5.data, pow5.nWords, v0, v1, r);
            }
            return new FDBigInteger(r, pow5.offset).leftShift(p2);
        }
        if (p2 != 0) {
            if (bitcount == 0) {
                return new FDBigInteger(new int[]{v0, v1}, wordcount);
            }
            return new FDBigInteger(new int[]{v0 << bitcount, v1 << bitcount | v0 >>> 32 - bitcount, v1 >>> 32 - bitcount}, wordcount);
        }
        return new FDBigInteger(new int[]{v0, v1}, 0);
    }

    private static FDBigInteger valueOfPow2(int p2) {
        int wordcount = p2 >> 5;
        int bitcount = p2 & 0x1F;
        return new FDBigInteger(new int[]{1 << bitcount}, wordcount);
    }

    private void trimLeadingZeros() {
        int i = this.nWords;
        if (i > 0 && this.data[--i] == 0) {
            while (i > 0 && this.data[i - 1] == 0) {
                --i;
            }
            this.nWords = i;
            if (i == 0) {
                this.offset = 0;
            }
        }
    }

    public int getNormalizationBias() {
        if (this.nWords == 0) {
            throw new IllegalArgumentException("Zero value cannot be normalized");
        }
        int zeros = Integer.numberOfLeadingZeros(this.data[this.nWords - 1]);
        return zeros < 4 ? 28 + zeros : zeros - 4;
    }

    private static void leftShift(int[] src, int idx, int[] result, int bitcount, int anticount, int prev) {
        int v;
        while (idx > 0) {
            v = prev << bitcount;
            prev = src[idx - 1];
            result[idx] = v |= prev >>> anticount;
            --idx;
        }
        result[0] = v = prev << bitcount;
    }

    public FDBigInteger leftShift(int shift) {
        if (shift == 0 || this.nWords == 0) {
            return this;
        }
        int wordcount = shift >> 5;
        int bitcount = shift & 0x1F;
        if (this.isImmutable) {
            int[] result;
            if (bitcount == 0) {
                return new FDBigInteger(Arrays.copyOf(this.data, this.nWords), this.offset + wordcount);
            }
            int idx = this.nWords - 1;
            int prev = this.data[idx];
            int anticount = 32 - bitcount;
            int hi = prev >>> anticount;
            if (hi != 0) {
                result = new int[this.nWords + 1];
                result[this.nWords] = hi;
            } else {
                result = new int[this.nWords];
            }
            FDBigInteger.leftShift(this.data, idx, result, bitcount, anticount, prev);
            return new FDBigInteger(result, this.offset + wordcount);
        }
        if (bitcount != 0) {
            int anticount = 32 - bitcount;
            if (this.data[0] << bitcount == 0) {
                int v;
                int idx;
                int prev = this.data[idx];
                for (idx = 0; idx < this.nWords - 1; ++idx) {
                    v = prev >>> anticount;
                    prev = this.data[idx + 1];
                    this.data[idx] = v |= prev << bitcount;
                }
                this.data[idx] = v = prev >>> anticount;
                if (v == 0) {
                    --this.nWords;
                }
                ++this.offset;
            } else {
                int idx = this.nWords - 1;
                int prev = this.data[idx];
                int hi = prev >>> anticount;
                int[] result = this.data;
                int[] src = this.data;
                if (hi != 0) {
                    if (this.nWords == this.data.length) {
                        this.data = result = new int[this.nWords + 1];
                    }
                    result[this.nWords++] = hi;
                }
                FDBigInteger.leftShift(src, idx, result, bitcount, anticount, prev);
            }
        }
        this.offset += wordcount;
        return this;
    }

    private int size() {
        return this.nWords + this.offset;
    }

    public int quoRemIteration(FDBigInteger S) throws IllegalArgumentException {
        int sSize;
        assert (!this.isImmutable) : "cannot modify immutable value";
        int thSize = this.size();
        if (thSize < (sSize = S.size())) {
            int p = FDBigInteger.multAndCarryBy10(this.data, this.nWords, this.data);
            if (p != 0) {
                this.data[this.nWords++] = p;
            } else {
                this.trimLeadingZeros();
            }
            return 0;
        }
        if (thSize > sSize) {
            throw new IllegalArgumentException("disparate values");
        }
        long q = ((long)this.data[this.nWords - 1] & 0xFFFFFFFFL) / ((long)S.data[S.nWords - 1] & 0xFFFFFFFFL);
        long diff = this.multDiffMe(q, S);
        if (diff != 0L) {
            long sum = 0L;
            int tStart = S.offset - this.offset;
            int[] sd = S.data;
            int[] td = this.data;
            while (sum == 0L) {
                int sIndex = 0;
                for (int tIndex = tStart; tIndex < this.nWords; ++tIndex) {
                    td[tIndex] = (int)(sum += ((long)td[tIndex] & 0xFFFFFFFFL) + ((long)sd[sIndex] & 0xFFFFFFFFL));
                    sum >>>= 32;
                    ++sIndex;
                }
                assert (sum == 0L || sum == 1L) : sum;
                --q;
            }
        }
        int p = FDBigInteger.multAndCarryBy10(this.data, this.nWords, this.data);
        assert (p == 0) : p;
        this.trimLeadingZeros();
        return (int)q;
    }

    public FDBigInteger multBy10() {
        if (this.nWords == 0) {
            return this;
        }
        if (this.isImmutable) {
            int[] res = new int[this.nWords + 1];
            res[this.nWords] = FDBigInteger.multAndCarryBy10(this.data, this.nWords, res);
            return new FDBigInteger(res, this.offset);
        }
        int p = FDBigInteger.multAndCarryBy10(this.data, this.nWords, this.data);
        if (p != 0) {
            if (this.nWords == this.data.length) {
                if (this.data[0] == 0) {
                    System.arraycopy(this.data, 1, this.data, 0, --this.nWords);
                    ++this.offset;
                } else {
                    this.data = Arrays.copyOf(this.data, this.data.length + 1);
                }
            }
            this.data[this.nWords++] = p;
        } else {
            this.trimLeadingZeros();
        }
        return this;
    }

    public FDBigInteger multByPow52(int p5, int p2) {
        if (this.nWords == 0) {
            return this;
        }
        FDBigInteger res = this;
        if (p5 != 0) {
            int extraSize;
            int n = extraSize = p2 != 0 ? 1 : 0;
            if (p5 < SMALL_5_POW.length) {
                int[] r = new int[this.nWords + 1 + extraSize];
                FDBigInteger.mult(this.data, this.nWords, SMALL_5_POW[p5], r);
                res = new FDBigInteger(r, this.offset);
            } else {
                FDBigInteger pow5 = FDBigInteger.big5pow(p5);
                int[] r = new int[this.nWords + pow5.size() + extraSize];
                FDBigInteger.mult(this.data, this.nWords, pow5.data, pow5.nWords, r);
                res = new FDBigInteger(r, this.offset + pow5.offset);
            }
        }
        return res.leftShift(p2);
    }

    private static void mult(int[] s1, int s1Len, int[] s2, int s2Len, int[] dst) {
        for (int i = 0; i < s1Len; ++i) {
            long v = (long)s1[i] & 0xFFFFFFFFL;
            long p = 0L;
            for (int j = 0; j < s2Len; ++j) {
                dst[i + j] = (int)(p += ((long)dst[i + j] & 0xFFFFFFFFL) + v * ((long)s2[j] & 0xFFFFFFFFL));
                p >>>= 32;
            }
            dst[i + s2Len] = (int)p;
        }
    }

    public FDBigInteger leftInplaceSub(FDBigInteger subtrahend) {
        assert (this.size() >= subtrahend.size()) : "result should be positive";
        FDBigInteger minuend = this.isImmutable ? new FDBigInteger((int[])this.data.clone(), this.offset) : this;
        int offsetDiff = subtrahend.offset - minuend.offset;
        int[] sData = subtrahend.data;
        int[] mData = minuend.data;
        int subLen = subtrahend.nWords;
        int minLen = minuend.nWords;
        if (offsetDiff < 0) {
            int rLen = minLen - offsetDiff;
            if (rLen < mData.length) {
                System.arraycopy(mData, 0, mData, -offsetDiff, minLen);
                Arrays.fill(mData, 0, -offsetDiff, 0);
            } else {
                int[] r = new int[rLen];
                System.arraycopy(mData, 0, r, -offsetDiff, minLen);
                mData = r;
                minuend.data = r;
            }
            minuend.offset = subtrahend.offset;
            minuend.nWords = minLen = rLen;
            offsetDiff = 0;
        }
        long borrow = 0L;
        int mIndex = offsetDiff;
        for (int sIndex = 0; sIndex < subLen && mIndex < minLen; ++sIndex, ++mIndex) {
            long diff = ((long)mData[mIndex] & 0xFFFFFFFFL) - ((long)sData[sIndex] & 0xFFFFFFFFL) + borrow;
            mData[mIndex] = (int)diff;
            borrow = diff >> 32;
        }
        while (borrow != 0L && mIndex < minLen) {
            long diff = ((long)mData[mIndex] & 0xFFFFFFFFL) + borrow;
            mData[mIndex] = (int)diff;
            borrow = diff >> 32;
            ++mIndex;
        }
        assert (borrow == 0L) : borrow;
        minuend.trimLeadingZeros();
        return minuend;
    }

    public FDBigInteger rightInplaceSub(FDBigInteger subtrahend) {
        int sIndex;
        assert (this.size() >= subtrahend.size()) : "result should be positive";
        FDBigInteger minuend = this;
        if (subtrahend.isImmutable) {
            subtrahend = new FDBigInteger((int[])subtrahend.data.clone(), subtrahend.offset);
        }
        int offsetDiff = minuend.offset - subtrahend.offset;
        int[] sData = subtrahend.data;
        int[] mData = minuend.data;
        int subLen = subtrahend.nWords;
        int minLen = minuend.nWords;
        if (offsetDiff < 0) {
            rLen = minLen;
            if (rLen < sData.length) {
                System.arraycopy(sData, 0, sData, -offsetDiff, subLen);
                Arrays.fill(sData, 0, -offsetDiff, 0);
            } else {
                int[] r = new int[rLen];
                System.arraycopy(sData, 0, r, -offsetDiff, subLen);
                sData = r;
                subtrahend.data = r;
            }
            subtrahend.offset = minuend.offset;
            subLen -= offsetDiff;
            offsetDiff = 0;
        } else {
            rLen = minLen + offsetDiff;
            if (rLen >= sData.length) {
                subtrahend.data = sData = Arrays.copyOf(sData, rLen);
            }
        }
        long borrow = 0L;
        for (sIndex = 0; sIndex < offsetDiff; ++sIndex) {
            long diff = 0L - ((long)sData[sIndex] & 0xFFFFFFFFL) + borrow;
            sData[sIndex] = (int)diff;
            borrow = diff >> 32;
        }
        for (int mIndex = 0; mIndex < minLen; ++mIndex) {
            long diff = ((long)mData[mIndex] & 0xFFFFFFFFL) - ((long)sData[sIndex] & 0xFFFFFFFFL) + borrow;
            sData[sIndex] = (int)diff;
            borrow = diff >> 32;
            ++sIndex;
        }
        assert (borrow == 0L) : borrow;
        subtrahend.nWords = sIndex;
        subtrahend.trimLeadingZeros();
        return subtrahend;
    }

    private static int checkZeroTail(int[] a, int from) {
        while (from > 0) {
            if (a[--from] == 0) continue;
            return 1;
        }
        return 0;
    }

    public int cmp(FDBigInteger other) {
        int aSize = this.nWords + this.offset;
        int bSize = other.nWords + other.offset;
        if (aSize > bSize) {
            return 1;
        }
        if (aSize < bSize) {
            return -1;
        }
        int aLen = this.nWords;
        int bLen = other.nWords;
        while (aLen > 0 && bLen > 0) {
            int b;
            int a;
            if ((a = this.data[--aLen]) == (b = other.data[--bLen])) continue;
            return ((long)a & 0xFFFFFFFFL) < ((long)b & 0xFFFFFFFFL) ? -1 : 1;
        }
        if (aLen > 0) {
            return FDBigInteger.checkZeroTail(this.data, aLen);
        }
        if (bLen > 0) {
            return -FDBigInteger.checkZeroTail(other.data, bLen);
        }
        return 0;
    }

    public int cmpPow52(int p5, int p2) {
        if (p5 == 0) {
            int wordcount = p2 >> 5;
            int bitcount = p2 & 0x1F;
            int size = this.nWords + this.offset;
            if (size > wordcount + 1) {
                return 1;
            }
            if (size < wordcount + 1) {
                return -1;
            }
            int a = this.data[this.nWords - 1];
            int b = 1 << bitcount;
            if (a != b) {
                return ((long)a & 0xFFFFFFFFL) < ((long)b & 0xFFFFFFFFL) ? -1 : 1;
            }
            return FDBigInteger.checkZeroTail(this.data, this.nWords - 1);
        }
        return this.cmp(FDBigInteger.big5pow(p5).leftShift(p2));
    }

    public int addAndCmp(FDBigInteger x, FDBigInteger y) {
        int sSize;
        int bSize;
        FDBigInteger small;
        FDBigInteger big;
        int ySize;
        int xSize = x.size();
        if (xSize >= (ySize = y.size())) {
            big = x;
            small = y;
            bSize = xSize;
            sSize = ySize;
        } else {
            big = y;
            small = x;
            bSize = ySize;
            sSize = xSize;
        }
        int thSize = this.size();
        if (bSize == 0) {
            return thSize == 0 ? 0 : 1;
        }
        if (sSize == 0) {
            return this.cmp(big);
        }
        if (bSize > thSize) {
            return -1;
        }
        if (bSize + 1 < thSize) {
            return 1;
        }
        long top = (long)big.data[big.nWords - 1] & 0xFFFFFFFFL;
        if (sSize == bSize) {
            top += (long)small.data[small.nWords - 1] & 0xFFFFFFFFL;
        }
        if (top >>> 32 == 0L) {
            if (top + 1L >>> 32 == 0L) {
                if (bSize < thSize) {
                    return 1;
                }
                long v = (long)this.data[this.nWords - 1] & 0xFFFFFFFFL;
                if (v < top) {
                    return -1;
                }
                if (v > top + 1L) {
                    return 1;
                }
            }
        } else {
            if (bSize + 1 > thSize) {
                return -1;
            }
            long v = (long)this.data[this.nWords - 1] & 0xFFFFFFFFL;
            if (v < (top >>>= 32)) {
                return -1;
            }
            if (v > top + 1L) {
                return 1;
            }
        }
        return this.cmp(big.add(small));
    }

    public void makeImmutable() {
        this.isImmutable = true;
    }

    private FDBigInteger mult(int i) {
        if (this.nWords == 0) {
            return this;
        }
        int[] r = new int[this.nWords + 1];
        FDBigInteger.mult(this.data, this.nWords, i, r);
        return new FDBigInteger(r, this.offset);
    }

    private FDBigInteger mult(FDBigInteger other) {
        if (this.nWords == 0) {
            return this;
        }
        if (this.size() == 1) {
            return other.mult(this.data[0]);
        }
        if (other.nWords == 0) {
            return other;
        }
        if (other.size() == 1) {
            return this.mult(other.data[0]);
        }
        int[] r = new int[this.nWords + other.nWords];
        FDBigInteger.mult(this.data, this.nWords, other.data, other.nWords, r);
        return new FDBigInteger(r, this.offset + other.offset);
    }

    private FDBigInteger add(FDBigInteger other) {
        int i;
        int smallLen;
        FDBigInteger small;
        int bigLen;
        FDBigInteger big;
        int oSize;
        int tSize = this.size();
        if (tSize >= (oSize = other.size())) {
            big = this;
            bigLen = tSize;
            small = other;
            smallLen = oSize;
        } else {
            big = other;
            bigLen = oSize;
            small = this;
            smallLen = tSize;
        }
        int[] r = new int[bigLen + 1];
        long carry = 0L;
        for (i = 0; i < smallLen; ++i) {
            r[i] = (int)(carry += (i < big.offset ? 0L : (long)big.data[i - big.offset] & 0xFFFFFFFFL) + (i < small.offset ? 0L : (long)small.data[i - small.offset] & 0xFFFFFFFFL));
            carry >>= 32;
        }
        while (i < bigLen) {
            r[i] = (int)(carry += i < big.offset ? 0L : (long)big.data[i - big.offset] & 0xFFFFFFFFL);
            carry >>= 32;
            ++i;
        }
        r[bigLen] = (int)carry;
        return new FDBigInteger(r, 0);
    }

    private void multAddMe(int iv, int addend) {
        long v = (long)iv & 0xFFFFFFFFL;
        long p = v * ((long)this.data[0] & 0xFFFFFFFFL) + ((long)addend & 0xFFFFFFFFL);
        this.data[0] = (int)p;
        p >>>= 32;
        for (int i = 1; i < this.nWords; ++i) {
            this.data[i] = (int)(p += v * ((long)this.data[i] & 0xFFFFFFFFL));
            p >>>= 32;
        }
        if (p != 0L) {
            this.data[this.nWords++] = (int)p;
        }
    }

    private long multDiffMe(long q, FDBigInteger S) {
        long diff = 0L;
        if (q != 0L) {
            int deltaSize = S.offset - this.offset;
            if (deltaSize >= 0) {
                int[] sd = S.data;
                int[] td = this.data;
                int sIndex = 0;
                int tIndex = deltaSize;
                while (sIndex < S.nWords) {
                    td[tIndex] = (int)(diff += ((long)td[tIndex] & 0xFFFFFFFFL) - q * ((long)sd[sIndex] & 0xFFFFFFFFL));
                    diff >>= 32;
                    ++sIndex;
                    ++tIndex;
                }
            } else {
                int rIndex;
                deltaSize = -deltaSize;
                int[] rd = new int[this.nWords + deltaSize];
                int sIndex = 0;
                int[] sd = S.data;
                for (rIndex = 0; rIndex < deltaSize && sIndex < S.nWords; ++sIndex, ++rIndex) {
                    rd[rIndex] = (int)(diff -= q * ((long)sd[sIndex] & 0xFFFFFFFFL));
                    diff >>= 32;
                }
                int tIndex = 0;
                int[] td = this.data;
                while (sIndex < S.nWords) {
                    rd[rIndex] = (int)(diff += ((long)td[tIndex] & 0xFFFFFFFFL) - q * ((long)sd[sIndex] & 0xFFFFFFFFL));
                    diff >>= 32;
                    ++sIndex;
                    ++tIndex;
                    ++rIndex;
                }
                this.nWords += deltaSize;
                this.offset -= deltaSize;
                this.data = rd;
            }
        }
        return diff;
    }

    private static int multAndCarryBy10(int[] src, int srcLen, int[] dst) {
        long carry = 0L;
        for (int i = 0; i < srcLen; ++i) {
            long product = ((long)src[i] & 0xFFFFFFFFL) * 10L + carry;
            dst[i] = (int)product;
            carry = product >>> 32;
        }
        return (int)carry;
    }

    private static void mult(int[] src, int srcLen, int value, int[] dst) {
        long val = (long)value & 0xFFFFFFFFL;
        long carry = 0L;
        for (int i = 0; i < srcLen; ++i) {
            long product = ((long)src[i] & 0xFFFFFFFFL) * val + carry;
            dst[i] = (int)product;
            carry = product >>> 32;
        }
        dst[srcLen] = (int)carry;
    }

    private static void mult(int[] src, int srcLen, int v0, int v1, int[] dst) {
        long product;
        int j;
        long v = (long)v0 & 0xFFFFFFFFL;
        long carry = 0L;
        for (j = 0; j < srcLen; ++j) {
            product = v * ((long)src[j] & 0xFFFFFFFFL) + carry;
            dst[j] = (int)product;
            carry = product >>> 32;
        }
        dst[srcLen] = (int)carry;
        v = (long)v1 & 0xFFFFFFFFL;
        carry = 0L;
        for (j = 0; j < srcLen; ++j) {
            product = ((long)dst[j + 1] & 0xFFFFFFFFL) + v * ((long)src[j] & 0xFFFFFFFFL) + carry;
            dst[j + 1] = (int)product;
            carry = product >>> 32;
        }
        dst[srcLen + 1] = (int)carry;
    }

    private static FDBigInteger big5pow(int p) {
        assert (p >= 0) : p;
        if (p < 340) {
            return POW_5_CACHE[p];
        }
        return FDBigInteger.big5powRec(p);
    }

    private static FDBigInteger big5powRec(int p) {
        if (p < 340) {
            return POW_5_CACHE[p];
        }
        int q = p >> 1;
        int r = p - q;
        FDBigInteger bigq = FDBigInteger.big5powRec(q);
        if (r < SMALL_5_POW.length) {
            return bigq.mult(SMALL_5_POW[r]);
        }
        return bigq.mult(FDBigInteger.big5powRec(r));
    }

    public String toHexString() {
        int i;
        if (this.nWords == 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder((this.nWords + this.offset) * 8);
        for (i = this.nWords - 1; i >= 0; --i) {
            String subStr = Integer.toHexString(this.data[i]);
            for (int j = subStr.length(); j < 8; ++j) {
                sb.append('0');
            }
            sb.append(subStr);
        }
        for (i = this.offset; i > 0; --i) {
            sb.append("00000000");
        }
        return sb.toString();
    }

    public BigInteger toBigInteger() {
        byte[] magnitude = new byte[this.nWords * 4 + 1];
        for (int i = 0; i < this.nWords; ++i) {
            int w = this.data[i];
            magnitude[magnitude.length - 4 * i - 1] = (byte)w;
            magnitude[magnitude.length - 4 * i - 2] = (byte)(w >> 8);
            magnitude[magnitude.length - 4 * i - 3] = (byte)(w >> 16);
            magnitude[magnitude.length - 4 * i - 4] = (byte)(w >> 24);
        }
        return new BigInteger(magnitude).shiftLeft(this.offset * 32);
    }

    public String toString() {
        return this.toBigInteger().toString();
    }

    static {
        CDS.initializeFromArchive(FDBigInteger.class);
        Object[] caches = archivedCaches;
        if (caches == null) {
            int i;
            long[] long5pow = new long[]{1L, 5L, 25L, 125L, 625L, 3125L, 15625L, 78125L, 390625L, 1953125L, 9765625L, 48828125L, 244140625L, 1220703125L, 6103515625L, 30517578125L, 152587890625L, 762939453125L, 3814697265625L, 19073486328125L, 95367431640625L, 476837158203125L, 2384185791015625L, 11920928955078125L, 59604644775390625L, 298023223876953125L, 1490116119384765625L};
            int[] small5pow = new int[]{1, 5, 25, 125, 625, 3125, 15625, 78125, 390625, 1953125, 9765625, 48828125, 244140625, 1220703125};
            FDBigInteger[] pow5cache = new FDBigInteger[340];
            for (i = 0; i < small5pow.length; ++i) {
                FDBigInteger pow5 = new FDBigInteger(new int[]{small5pow[i]}, 0);
                pow5.makeImmutable();
                pow5cache[i] = pow5;
            }
            FDBigInteger prev = pow5cache[i - 1];
            while (i < 340) {
                pow5cache[i] = prev = prev.mult(5);
                prev.makeImmutable();
                ++i;
            }
            FDBigInteger zero = new FDBigInteger(new int[0], 0);
            zero.makeImmutable();
            caches = new Object[]{small5pow, long5pow, pow5cache, zero};
            archivedCaches = caches;
        }
        SMALL_5_POW = (int[])caches[0];
        LONG_5_POW = (long[])caches[1];
        POW_5_CACHE = (FDBigInteger[])caches[2];
        ZERO = (FDBigInteger)caches[3];
    }
}

