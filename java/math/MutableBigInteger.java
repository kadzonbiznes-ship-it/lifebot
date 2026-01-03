/*
 * Decompiled with CFR 0.152.
 */
package java.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.SignedMutableBigInteger;
import java.util.Arrays;

class MutableBigInteger {
    int[] value;
    int intLen;
    int offset = 0;
    static final MutableBigInteger ONE = new MutableBigInteger(1);
    static final int KNUTH_POW2_THRESH_LEN = 6;
    static final int KNUTH_POW2_THRESH_ZEROS = 3;

    MutableBigInteger() {
        this.value = new int[1];
        this.intLen = 0;
    }

    MutableBigInteger(int val) {
        this.value = new int[1];
        this.intLen = 1;
        this.value[0] = val;
    }

    MutableBigInteger(int[] val) {
        this.value = val;
        this.intLen = val.length;
    }

    MutableBigInteger(BigInteger b) {
        this.intLen = b.mag.length;
        this.value = Arrays.copyOf(b.mag, this.intLen);
    }

    MutableBigInteger(MutableBigInteger val) {
        this.intLen = val.intLen;
        this.value = Arrays.copyOfRange(val.value, val.offset, val.offset + this.intLen);
    }

    private void ones(int n) {
        if (n > this.value.length) {
            this.value = new int[n];
        }
        Arrays.fill(this.value, -1);
        this.offset = 0;
        this.intLen = n;
    }

    private int[] getMagnitudeArray() {
        if (this.offset > 0 || this.value.length != this.intLen) {
            int[] tmp = Arrays.copyOfRange(this.value, this.offset, this.offset + this.intLen);
            Arrays.fill(this.value, 0);
            this.offset = 0;
            this.intLen = tmp.length;
            this.value = tmp;
        }
        return this.value;
    }

    private long toLong() {
        assert (this.intLen <= 2) : "this MutableBigInteger exceeds the range of long";
        if (this.intLen == 0) {
            return 0L;
        }
        long d = (long)this.value[this.offset] & 0xFFFFFFFFL;
        return this.intLen == 2 ? d << 32 | (long)this.value[this.offset + 1] & 0xFFFFFFFFL : d;
    }

    BigInteger toBigInteger(int sign) {
        if (this.intLen == 0 || sign == 0) {
            return BigInteger.ZERO;
        }
        return new BigInteger(this.getMagnitudeArray(), sign);
    }

    BigInteger toBigInteger() {
        this.normalize();
        return this.toBigInteger(this.isZero() ? 0 : 1);
    }

    BigDecimal toBigDecimal(int sign, int scale) {
        if (this.intLen == 0 || sign == 0) {
            return BigDecimal.zeroValueOf(scale);
        }
        int[] mag = this.getMagnitudeArray();
        int len = mag.length;
        int d = mag[0];
        if (len > 2 || d < 0 && len == 2) {
            return new BigDecimal(new BigInteger(mag, sign), Long.MIN_VALUE, scale, 0);
        }
        long v = len == 2 ? (long)mag[1] & 0xFFFFFFFFL | ((long)d & 0xFFFFFFFFL) << 32 : (long)d & 0xFFFFFFFFL;
        return BigDecimal.valueOf(sign == -1 ? -v : v, scale);
    }

    long toCompactValue(int sign) {
        if (this.intLen == 0 || sign == 0) {
            return 0L;
        }
        int[] mag = this.getMagnitudeArray();
        int len = mag.length;
        int d = mag[0];
        if (len > 2 || d < 0 && len == 2) {
            return Long.MIN_VALUE;
        }
        long v = len == 2 ? (long)mag[1] & 0xFFFFFFFFL | ((long)d & 0xFFFFFFFFL) << 32 : (long)d & 0xFFFFFFFFL;
        return sign == -1 ? -v : v;
    }

    void clear() {
        this.intLen = 0;
        this.offset = 0;
        int n = this.value.length;
        for (int index = 0; index < n; ++index) {
            this.value[index] = 0;
        }
    }

    void reset() {
        this.intLen = 0;
        this.offset = 0;
    }

    final int compare(MutableBigInteger b) {
        int blen = b.intLen;
        if (this.intLen < blen) {
            return -1;
        }
        if (this.intLen > blen) {
            return 1;
        }
        int[] bval = b.value;
        int i = this.offset;
        int j = b.offset;
        while (i < this.intLen + this.offset) {
            int b1 = this.value[i] + Integer.MIN_VALUE;
            int b2 = bval[j] + Integer.MIN_VALUE;
            if (b1 < b2) {
                return -1;
            }
            if (b1 > b2) {
                return 1;
            }
            ++i;
            ++j;
        }
        return 0;
    }

    private int compareShifted(MutableBigInteger b, int ints) {
        int alen = this.intLen - ints;
        int blen = b.intLen;
        if (alen < blen) {
            return -1;
        }
        if (alen > blen) {
            return 1;
        }
        int[] bval = b.value;
        int i = this.offset;
        int j = b.offset;
        while (i < alen + this.offset) {
            int b1 = this.value[i] + Integer.MIN_VALUE;
            int b2 = bval[j] + Integer.MIN_VALUE;
            if (b1 < b2) {
                return -1;
            }
            if (b1 > b2) {
                return 1;
            }
            ++i;
            ++j;
        }
        return 0;
    }

    final int compareHalf(MutableBigInteger b) {
        int blen = b.intLen;
        int len = this.intLen;
        if (len <= 0) {
            return blen <= 0 ? 0 : -1;
        }
        if (len > blen) {
            return 1;
        }
        if (len < blen - 1) {
            return -1;
        }
        int[] bval = b.value;
        int bstart = 0;
        int carry = 0;
        if (len != blen) {
            if (bval[bstart] == 1) {
                ++bstart;
                carry = Integer.MIN_VALUE;
            } else {
                return -1;
            }
        }
        int[] val = this.value;
        int i = this.offset;
        int j = bstart;
        while (i < len + this.offset) {
            int bv;
            long hb;
            long v;
            if ((v = (long)val[i++] & 0xFFFFFFFFL) != (hb = (long)(((bv = bval[j++]) >>> 1) + carry) & 0xFFFFFFFFL)) {
                return v < hb ? -1 : 1;
            }
            carry = (bv & 1) << 31;
        }
        return carry == 0 ? 0 : -1;
    }

    private final int getLowestSetBit() {
        int j;
        if (this.intLen == 0) {
            return -1;
        }
        for (j = this.intLen - 1; j > 0 && this.value[j + this.offset] == 0; --j) {
        }
        int b = this.value[j + this.offset];
        if (b == 0) {
            return -1;
        }
        return (this.intLen - 1 - j << 5) + Integer.numberOfTrailingZeros(b);
    }

    private final int getInt(int index) {
        return this.value[this.offset + index];
    }

    private final long getLong(int index) {
        return (long)this.value[this.offset + index] & 0xFFFFFFFFL;
    }

    final void normalize() {
        if (this.intLen == 0) {
            this.offset = 0;
            return;
        }
        int index = this.offset;
        if (this.value[index] != 0) {
            return;
        }
        int indexBound = index + this.intLen;
        while (++index < indexBound && this.value[index] == 0) {
        }
        int numZeros = index - this.offset;
        this.intLen -= numZeros;
        this.offset = this.intLen == 0 ? 0 : this.offset + numZeros;
    }

    private final void ensureCapacity(int len) {
        if (this.value.length < len) {
            this.value = new int[len];
            this.offset = 0;
            this.intLen = len;
        }
    }

    int[] toIntArray() {
        int[] result = new int[this.intLen];
        for (int i = 0; i < this.intLen; ++i) {
            result[i] = this.value[this.offset + i];
        }
        return result;
    }

    void setInt(int index, int val) {
        this.value[this.offset + index] = val;
    }

    void setValue(int[] val, int length) {
        this.value = val;
        this.intLen = length;
        this.offset = 0;
    }

    void copyValue(MutableBigInteger src) {
        int len = src.intLen;
        if (this.value.length < len) {
            this.value = new int[len];
        }
        System.arraycopy(src.value, src.offset, this.value, 0, len);
        this.intLen = len;
        this.offset = 0;
    }

    void copyValue(int[] val) {
        int len = val.length;
        if (this.value.length < len) {
            this.value = new int[len];
        }
        System.arraycopy(val, 0, this.value, 0, len);
        this.intLen = len;
        this.offset = 0;
    }

    boolean isOne() {
        return this.intLen == 1 && this.value[this.offset] == 1;
    }

    boolean isZero() {
        return this.intLen == 0;
    }

    boolean isEven() {
        return this.intLen == 0 || (this.value[this.offset + this.intLen - 1] & 1) == 0;
    }

    boolean isOdd() {
        return this.isZero() ? false : (this.value[this.offset + this.intLen - 1] & 1) == 1;
    }

    boolean isNormal() {
        if (this.intLen + this.offset > this.value.length) {
            return false;
        }
        if (this.intLen == 0) {
            return true;
        }
        return this.value[this.offset] != 0;
    }

    public String toString() {
        BigInteger b = this.toBigInteger(1);
        return b.toString();
    }

    void safeRightShift(int n) {
        if (n / 32 >= this.intLen) {
            this.reset();
        } else {
            this.rightShift(n);
        }
    }

    void rightShift(int n) {
        if (this.intLen == 0) {
            return;
        }
        int nInts = n >>> 5;
        int nBits = n & 0x1F;
        this.intLen -= nInts;
        if (nBits == 0) {
            return;
        }
        int bitsInHighWord = BigInteger.bitLengthForInt(this.value[this.offset]);
        if (nBits >= bitsInHighWord) {
            this.primitiveLeftShift(32 - nBits);
            --this.intLen;
        } else {
            this.primitiveRightShift(nBits);
        }
    }

    void safeLeftShift(int n) {
        if (n > 0) {
            this.leftShift(n);
        }
    }

    void leftShift(int n) {
        if (this.intLen == 0) {
            return;
        }
        int nInts = n >>> 5;
        int nBits = n & 0x1F;
        int bitsInHighWord = BigInteger.bitLengthForInt(this.value[this.offset]);
        if (n <= 32 - bitsInHighWord) {
            this.primitiveLeftShift(nBits);
            return;
        }
        int newLen = this.intLen + nInts + 1;
        if (nBits <= 32 - bitsInHighWord) {
            --newLen;
        }
        if (this.value.length < newLen) {
            int[] result = new int[newLen];
            for (int i = 0; i < this.intLen; ++i) {
                result[i] = this.value[this.offset + i];
            }
            this.setValue(result, newLen);
        } else if (this.value.length - this.offset >= newLen) {
            for (int i = 0; i < newLen - this.intLen; ++i) {
                this.value[this.offset + this.intLen + i] = 0;
            }
        } else {
            int i;
            for (i = 0; i < this.intLen; ++i) {
                this.value[i] = this.value[this.offset + i];
            }
            for (i = this.intLen; i < newLen; ++i) {
                this.value[i] = 0;
            }
            this.offset = 0;
        }
        this.intLen = newLen;
        if (nBits == 0) {
            return;
        }
        if (nBits <= 32 - bitsInHighWord) {
            this.primitiveLeftShift(nBits);
        } else {
            this.primitiveRightShift(32 - nBits);
        }
    }

    private int divadd(int[] a, int[] result, int offset) {
        long carry = 0L;
        for (int j = a.length - 1; j >= 0; --j) {
            long sum = ((long)a[j] & 0xFFFFFFFFL) + ((long)result[j + offset] & 0xFFFFFFFFL) + carry;
            result[j + offset] = (int)sum;
            carry = sum >>> 32;
        }
        return (int)carry;
    }

    private int mulsub(int[] q, int[] a, int x, int len, int offset) {
        long xLong = (long)x & 0xFFFFFFFFL;
        long carry = 0L;
        offset += len;
        for (int j = len - 1; j >= 0; --j) {
            long product = ((long)a[j] & 0xFFFFFFFFL) * xLong + carry;
            long difference = (long)q[offset] - product;
            q[offset--] = (int)difference;
            carry = (product >>> 32) + (long)((difference & 0xFFFFFFFFL) > ((long)(~((int)product)) & 0xFFFFFFFFL) ? 1 : 0);
        }
        return (int)carry;
    }

    private int mulsubBorrow(int[] q, int[] a, int x, int len, int offset) {
        long xLong = (long)x & 0xFFFFFFFFL;
        long carry = 0L;
        offset += len;
        for (int j = len - 1; j >= 0; --j) {
            long difference;
            long product;
            carry = (product >>> 32) + (long)(((difference = (long)q[offset--] - (product = ((long)a[j] & 0xFFFFFFFFL) * xLong + carry)) & 0xFFFFFFFFL) > ((long)(~((int)product)) & 0xFFFFFFFFL) ? 1 : 0);
        }
        return (int)carry;
    }

    private final void primitiveRightShift(int n) {
        int i;
        int[] val = this.value;
        int n2 = 32 - n;
        int c = val[i];
        for (i = this.offset + this.intLen - 1; i > this.offset; --i) {
            int b = c;
            c = val[i - 1];
            val[i] = c << n2 | b >>> n;
        }
        int n3 = this.offset;
        val[n3] = val[n3] >>> n;
    }

    private final void primitiveLeftShift(int n) {
        int i;
        int[] val = this.value;
        int n2 = 32 - n;
        int c = val[i];
        int m = i + this.intLen - 1;
        for (i = this.offset; i < m; ++i) {
            int b = c;
            c = val[i + 1];
            val[i] = b << n | c >>> n2;
        }
        int n3 = this.offset + this.intLen - 1;
        val[n3] = val[n3] << n;
    }

    private BigInteger getLower(int n) {
        int len;
        if (this.isZero()) {
            return BigInteger.ZERO;
        }
        if (this.intLen < n) {
            return this.toBigInteger(1);
        }
        for (len = n; len > 0 && this.value[this.offset + this.intLen - len] == 0; --len) {
        }
        int sign = len > 0 ? 1 : 0;
        return new BigInteger(Arrays.copyOfRange(this.value, this.offset + this.intLen - len, this.offset + this.intLen), sign);
    }

    private void keepLower(int n) {
        if (this.intLen >= n) {
            this.offset += this.intLen - n;
            this.intLen = n;
        }
    }

    void add(MutableBigInteger addend) {
        long sum;
        int x = this.intLen;
        int y = addend.intLen;
        int resultLen = this.intLen > addend.intLen ? this.intLen : addend.intLen;
        int[] result = this.value.length < resultLen ? new int[resultLen] : this.value;
        int rstart = result.length - 1;
        long carry = 0L;
        while (x > 0 && y > 0) {
            sum = ((long)this.value[--x + this.offset] & 0xFFFFFFFFL) + ((long)addend.value[--y + addend.offset] & 0xFFFFFFFFL) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }
        while (x > 0) {
            if (carry == 0L && result == this.value && rstart == --x + this.offset) {
                return;
            }
            sum = ((long)this.value[x + this.offset] & 0xFFFFFFFFL) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }
        while (y > 0) {
            sum = ((long)addend.value[--y + addend.offset] & 0xFFFFFFFFL) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }
        if (carry > 0L) {
            if (result.length < ++resultLen) {
                int[] temp = new int[resultLen];
                System.arraycopy(result, 0, temp, 1, result.length);
                temp[0] = 1;
                result = temp;
            } else {
                result[rstart--] = 1;
            }
        }
        this.value = result;
        this.intLen = resultLen;
        this.offset = result.length - resultLen;
    }

    void addShifted(MutableBigInteger addend, int n) {
        long sum;
        int bval;
        if (addend.isZero()) {
            return;
        }
        int x = this.intLen;
        int y = addend.intLen + n;
        int resultLen = this.intLen > y ? this.intLen : y;
        int[] result = this.value.length < resultLen ? new int[resultLen] : this.value;
        int rstart = result.length - 1;
        long carry = 0L;
        while (x > 0 && y > 0) {
            bval = --y + addend.offset < addend.value.length ? addend.value[y + addend.offset] : 0;
            sum = ((long)this.value[--x + this.offset] & 0xFFFFFFFFL) + ((long)bval & 0xFFFFFFFFL) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }
        while (x > 0) {
            if (carry == 0L && result == this.value && rstart == --x + this.offset) {
                return;
            }
            sum = ((long)this.value[x + this.offset] & 0xFFFFFFFFL) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }
        while (y > 0) {
            bval = --y + addend.offset < addend.value.length ? addend.value[y + addend.offset] : 0;
            sum = ((long)bval & 0xFFFFFFFFL) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }
        if (carry > 0L) {
            if (result.length < ++resultLen) {
                int[] temp = new int[resultLen];
                System.arraycopy(result, 0, temp, 1, result.length);
                temp[0] = 1;
                result = temp;
            } else {
                result[rstart--] = 1;
            }
        }
        this.value = result;
        this.intLen = resultLen;
        this.offset = result.length - resultLen;
    }

    void addDisjoint(MutableBigInteger addend, int n) {
        int[] result;
        int resultLen;
        if (addend.isZero()) {
            return;
        }
        int x = this.intLen;
        int y = addend.intLen + n;
        int n2 = resultLen = this.intLen > y ? this.intLen : y;
        if (this.value.length < resultLen) {
            result = new int[resultLen];
        } else {
            result = this.value;
            Arrays.fill(this.value, this.offset + this.intLen, this.value.length, 0);
        }
        int rstart = result.length - 1;
        System.arraycopy(this.value, this.offset, result, rstart + 1 - x, x);
        int len = Math.min(y -= x, addend.value.length - addend.offset);
        System.arraycopy(addend.value, addend.offset, result, (rstart -= x) + 1 - y, len);
        for (int i = rstart + 1 - y + len; i < rstart + 1; ++i) {
            result[i] = 0;
        }
        this.value = result;
        this.intLen = resultLen;
        this.offset = result.length - resultLen;
    }

    void addLower(MutableBigInteger addend, int n) {
        MutableBigInteger a = new MutableBigInteger(addend);
        if (a.offset + a.intLen >= n) {
            a.offset = a.offset + a.intLen - n;
            a.intLen = n;
        }
        a.normalize();
        this.add(a);
    }

    int subtract(MutableBigInteger b) {
        int resultLen;
        MutableBigInteger a = this;
        int[] result = this.value;
        int sign = a.compare(b);
        if (sign == 0) {
            this.reset();
            return 0;
        }
        if (sign < 0) {
            MutableBigInteger tmp = a;
            a = b;
            b = tmp;
        }
        if (result.length < (resultLen = a.intLen)) {
            result = new int[resultLen];
        }
        long diff = 0L;
        int x = a.intLen;
        int y = b.intLen;
        int rstart = result.length - 1;
        while (y > 0) {
            diff = ((long)a.value[--x + a.offset] & 0xFFFFFFFFL) - ((long)b.value[--y + b.offset] & 0xFFFFFFFFL) + (diff >> 32);
            result[rstart--] = (int)diff;
        }
        while (x > 0) {
            diff = ((long)a.value[--x + a.offset] & 0xFFFFFFFFL) + (diff >> 32);
            result[rstart--] = (int)diff;
        }
        this.value = result;
        this.intLen = resultLen;
        this.offset = this.value.length - resultLen;
        this.normalize();
        return sign;
    }

    private int difference(MutableBigInteger b) {
        MutableBigInteger a = this;
        int sign = a.compare(b);
        if (sign == 0) {
            return 0;
        }
        if (sign < 0) {
            MutableBigInteger tmp = a;
            a = b;
            b = tmp;
        }
        long diff = 0L;
        int x = a.intLen;
        int y = b.intLen;
        while (y > 0) {
            diff = ((long)a.value[a.offset + --x] & 0xFFFFFFFFL) - ((long)b.value[b.offset + --y] & 0xFFFFFFFFL) + (diff >> 32);
            a.value[a.offset + x] = (int)diff;
        }
        while (diff < 0L && x > 0) {
            diff = ((long)a.value[a.offset + --x] & 0xFFFFFFFFL) + (diff >> 32);
            a.value[a.offset + x] = (int)diff;
        }
        a.normalize();
        return sign;
    }

    void multiply(MutableBigInteger y, MutableBigInteger z) {
        int xLen = this.intLen;
        int yLen = y.intLen;
        int newLen = xLen + yLen;
        if (z.value.length < newLen) {
            z.value = new int[newLen];
        }
        z.offset = 0;
        z.intLen = newLen;
        long carry = 0L;
        int j = yLen - 1;
        int k = yLen + xLen - 1;
        while (j >= 0) {
            long product = ((long)y.value[j + y.offset] & 0xFFFFFFFFL) * ((long)this.value[xLen - 1 + this.offset] & 0xFFFFFFFFL) + carry;
            z.value[k] = (int)product;
            carry = product >>> 32;
            --j;
            --k;
        }
        z.value[xLen - 1] = (int)carry;
        for (int i = xLen - 2; i >= 0; --i) {
            carry = 0L;
            int j2 = yLen - 1;
            int k2 = yLen + i;
            while (j2 >= 0) {
                long product = ((long)y.value[j2 + y.offset] & 0xFFFFFFFFL) * ((long)this.value[i + this.offset] & 0xFFFFFFFFL) + ((long)z.value[k2] & 0xFFFFFFFFL) + carry;
                z.value[k2] = (int)product;
                carry = product >>> 32;
                --j2;
                --k2;
            }
            z.value[i] = (int)carry;
        }
        z.normalize();
    }

    void mul(int y, MutableBigInteger z) {
        if (y == 1) {
            z.copyValue(this);
            return;
        }
        if (y == 0) {
            z.clear();
            return;
        }
        long ylong = (long)y & 0xFFFFFFFFL;
        int[] zval = z.value.length < this.intLen + 1 ? new int[this.intLen + 1] : z.value;
        long carry = 0L;
        for (int i = this.intLen - 1; i >= 0; --i) {
            long product = ylong * ((long)this.value[i + this.offset] & 0xFFFFFFFFL) + carry;
            zval[i + 1] = (int)product;
            carry = product >>> 32;
        }
        if (carry == 0L) {
            z.offset = 1;
            z.intLen = this.intLen;
        } else {
            z.offset = 0;
            z.intLen = this.intLen + 1;
            zval[0] = (int)carry;
        }
        z.value = zval;
    }

    int divideOneWord(int divisor, MutableBigInteger quotient) {
        long divisorLong = (long)divisor & 0xFFFFFFFFL;
        if (this.intLen == 1) {
            long dividendValue = (long)this.value[this.offset] & 0xFFFFFFFFL;
            int q = (int)(dividendValue / divisorLong);
            int r = (int)(dividendValue - (long)q * divisorLong);
            quotient.value[0] = q;
            quotient.intLen = q == 0 ? 0 : 1;
            quotient.offset = 0;
            return r;
        }
        if (quotient.value.length < this.intLen) {
            quotient.value = new int[this.intLen];
        }
        quotient.offset = 0;
        quotient.intLen = this.intLen;
        int shift = Integer.numberOfLeadingZeros(divisor);
        int rem = this.value[this.offset];
        long remLong = (long)rem & 0xFFFFFFFFL;
        if (remLong < divisorLong) {
            quotient.value[0] = 0;
        } else {
            quotient.value[0] = (int)(remLong / divisorLong);
            rem = (int)(remLong - (long)quotient.value[0] * divisorLong);
            remLong = (long)rem & 0xFFFFFFFFL;
        }
        int xlen = this.intLen;
        while (--xlen > 0) {
            int q;
            long dividendEstimate = remLong << 32 | (long)this.value[this.offset + this.intLen - xlen] & 0xFFFFFFFFL;
            if (dividendEstimate >= 0L) {
                q = (int)(dividendEstimate / divisorLong);
                rem = (int)(dividendEstimate - (long)q * divisorLong);
            } else {
                long tmp = MutableBigInteger.divWord(dividendEstimate, divisor);
                q = (int)(tmp & 0xFFFFFFFFL);
                rem = (int)(tmp >>> 32);
            }
            quotient.value[this.intLen - xlen] = q;
            remLong = (long)rem & 0xFFFFFFFFL;
        }
        quotient.normalize();
        if (shift > 0) {
            return rem % divisor;
        }
        return rem;
    }

    MutableBigInteger divide(MutableBigInteger b, MutableBigInteger quotient) {
        return this.divide(b, quotient, true);
    }

    MutableBigInteger divide(MutableBigInteger b, MutableBigInteger quotient, boolean needRemainder) {
        if (b.intLen < 80 || this.intLen - b.intLen < 40) {
            return this.divideKnuth(b, quotient, needRemainder);
        }
        return this.divideAndRemainderBurnikelZiegler(b, quotient);
    }

    MutableBigInteger divideKnuth(MutableBigInteger b, MutableBigInteger quotient) {
        return this.divideKnuth(b, quotient, true);
    }

    MutableBigInteger divideKnuth(MutableBigInteger b, MutableBigInteger quotient, boolean needRemainder) {
        int trailingZeroBits;
        if (b.intLen == 0) {
            throw new ArithmeticException("BigInteger divide by zero");
        }
        if (this.intLen == 0) {
            quotient.offset = 0;
            quotient.intLen = 0;
            return needRemainder ? new MutableBigInteger() : null;
        }
        int cmp = this.compare(b);
        if (cmp < 0) {
            quotient.offset = 0;
            quotient.intLen = 0;
            return needRemainder ? new MutableBigInteger(this) : null;
        }
        if (cmp == 0) {
            quotient.intLen = 1;
            quotient.value[0] = 1;
            quotient.offset = 0;
            return needRemainder ? new MutableBigInteger() : null;
        }
        quotient.clear();
        if (b.intLen == 1) {
            int r = this.divideOneWord(b.value[b.offset], quotient);
            if (needRemainder) {
                if (r == 0) {
                    return new MutableBigInteger();
                }
                return new MutableBigInteger(r);
            }
            return null;
        }
        if (this.intLen >= 6 && (trailingZeroBits = Math.min(this.getLowestSetBit(), b.getLowestSetBit())) >= 96) {
            MutableBigInteger a = new MutableBigInteger(this);
            b = new MutableBigInteger(b);
            a.rightShift(trailingZeroBits);
            b.rightShift(trailingZeroBits);
            MutableBigInteger r = a.divideKnuth(b, quotient);
            r.leftShift(trailingZeroBits);
            return r;
        }
        return this.divideMagnitude(b, quotient, needRemainder);
    }

    MutableBigInteger divideAndRemainderBurnikelZiegler(MutableBigInteger b, MutableBigInteger quotient) {
        MutableBigInteger ri;
        int r = this.intLen;
        int s = b.intLen;
        quotient.intLen = 0;
        quotient.offset = 0;
        if (r < s) {
            return this;
        }
        int m = 1 << 32 - Integer.numberOfLeadingZeros(s / 80);
        int j = (s + m - 1) / m;
        int n = j * m;
        long n32 = 32L * (long)n;
        int sigma = (int)Math.max(0L, n32 - b.bitLength());
        MutableBigInteger bShifted = new MutableBigInteger(b);
        bShifted.safeLeftShift(sigma);
        MutableBigInteger aShifted = new MutableBigInteger(this);
        aShifted.safeLeftShift(sigma);
        int t = (int)((aShifted.bitLength() + n32) / n32);
        if (t < 2) {
            t = 2;
        }
        MutableBigInteger a1 = aShifted.getBlock(t - 1, t, n);
        MutableBigInteger z = aShifted.getBlock(t - 2, t, n);
        z.addDisjoint(a1, n);
        MutableBigInteger qi = new MutableBigInteger();
        for (int i = t - 2; i > 0; --i) {
            ri = z.divide2n1n(bShifted, qi);
            z = aShifted.getBlock(i - 1, t, n);
            z.addDisjoint(ri, n);
            quotient.addShifted(qi, i * n);
        }
        ri = z.divide2n1n(bShifted, qi);
        quotient.add(qi);
        ri.rightShift(sigma);
        return ri;
    }

    private MutableBigInteger divide2n1n(MutableBigInteger b, MutableBigInteger quotient) {
        int n = b.intLen;
        if (n % 2 != 0 || n < 80) {
            return this.divideKnuth(b, quotient);
        }
        MutableBigInteger aUpper = new MutableBigInteger(this);
        aUpper.safeRightShift(32 * (n / 2));
        this.keepLower(n / 2);
        MutableBigInteger q1 = new MutableBigInteger();
        MutableBigInteger r1 = aUpper.divide3n2n(b, q1);
        this.addDisjoint(r1, n / 2);
        MutableBigInteger r2 = this.divide3n2n(b, quotient);
        quotient.addDisjoint(q1, n / 2);
        return r2;
    }

    private MutableBigInteger divide3n2n(MutableBigInteger b, MutableBigInteger quotient) {
        MutableBigInteger d;
        MutableBigInteger r;
        int n = b.intLen / 2;
        MutableBigInteger a12 = new MutableBigInteger(this);
        a12.safeRightShift(32 * n);
        MutableBigInteger b1 = new MutableBigInteger(b);
        b1.safeRightShift(n * 32);
        BigInteger b2 = b.getLower(n);
        if (this.compareShifted(b, n) < 0) {
            r = a12.divide2n1n(b1, quotient);
            d = new MutableBigInteger(quotient.toBigInteger().multiply(b2));
        } else {
            quotient.ones(n);
            a12.add(b1);
            b1.leftShift(32 * n);
            a12.subtract(b1);
            r = a12;
            d = new MutableBigInteger(b2);
            d.leftShift(32 * n);
            d.subtract(new MutableBigInteger(b2));
        }
        r.leftShift(32 * n);
        r.addLower(this, n);
        while (r.compare(d) < 0) {
            r.add(b);
            quotient.subtract(ONE);
        }
        r.subtract(d);
        return r;
    }

    private MutableBigInteger getBlock(int index, int numBlocks, int blockLength) {
        int blockStart = index * blockLength;
        if (blockStart >= this.intLen) {
            return new MutableBigInteger();
        }
        int blockEnd = index == numBlocks - 1 ? this.intLen : (index + 1) * blockLength;
        if (blockEnd > this.intLen) {
            return new MutableBigInteger();
        }
        int[] newVal = Arrays.copyOfRange(this.value, this.offset + this.intLen - blockEnd, this.offset + this.intLen - blockStart);
        return new MutableBigInteger(newVal);
    }

    long bitLength() {
        if (this.intLen == 0) {
            return 0L;
        }
        return (long)this.intLen * 32L - (long)Integer.numberOfLeadingZeros(this.value[this.offset]);
    }

    long divide(long v, MutableBigInteger quotient) {
        if (v == 0L) {
            throw new ArithmeticException("BigInteger divide by zero");
        }
        if (this.intLen == 0) {
            quotient.offset = 0;
            quotient.intLen = 0;
            return 0L;
        }
        if (v < 0L) {
            v = -v;
        }
        int d = (int)(v >>> 32);
        quotient.clear();
        if (d == 0) {
            return (long)this.divideOneWord((int)v, quotient) & 0xFFFFFFFFL;
        }
        return this.divideLongMagnitude(v, quotient).toLong();
    }

    private static void copyAndShift(int[] src, int srcFrom, int srcLen, int[] dst, int dstFrom, int shift) {
        int n2 = 32 - shift;
        int c = src[srcFrom];
        for (int i = 0; i < srcLen - 1; ++i) {
            int b = c;
            c = src[++srcFrom];
            dst[dstFrom + i] = b << shift | c >>> n2;
        }
        dst[dstFrom + srcLen - 1] = c << shift;
    }

    private MutableBigInteger divideMagnitude(MutableBigInteger div, MutableBigInteger quotient, boolean needRemainder) {
        MutableBigInteger rem;
        int[] divisor;
        int shift = Integer.numberOfLeadingZeros(div.value[div.offset]);
        int dlen = div.intLen;
        if (shift > 0) {
            divisor = new int[dlen];
            MutableBigInteger.copyAndShift(div.value, div.offset, dlen, divisor, 0, shift);
            if (Integer.numberOfLeadingZeros(this.value[this.offset]) >= shift) {
                remarr = new int[this.intLen + 1];
                rem = new MutableBigInteger(remarr);
                rem.intLen = this.intLen;
                rem.offset = 1;
                MutableBigInteger.copyAndShift(this.value, this.offset, this.intLen, remarr, 1, shift);
            } else {
                remarr = new int[this.intLen + 2];
                rem = new MutableBigInteger(remarr);
                rem.intLen = this.intLen + 1;
                rem.offset = 1;
                int rFrom = this.offset;
                int c = 0;
                int n2 = 32 - shift;
                int i = 1;
                while (i < this.intLen + 1) {
                    int b = c;
                    c = this.value[rFrom];
                    remarr[i] = b << shift | c >>> n2;
                    ++i;
                    ++rFrom;
                }
                remarr[this.intLen + 1] = c << shift;
            }
        } else {
            divisor = Arrays.copyOfRange(div.value, div.offset, div.offset + div.intLen);
            rem = new MutableBigInteger(new int[this.intLen + 1]);
            System.arraycopy(this.value, this.offset, rem.value, 1, this.intLen);
            rem.intLen = this.intLen;
            rem.offset = 1;
        }
        int nlen = rem.intLen;
        int limit = nlen - dlen + 1;
        if (quotient.value.length < limit) {
            quotient.value = new int[limit];
            quotient.offset = 0;
        }
        quotient.intLen = limit;
        int[] q = quotient.value;
        rem.offset = 0;
        rem.value[0] = 0;
        ++rem.intLen;
        int dh = divisor[0];
        long dhLong = (long)dh & 0xFFFFFFFFL;
        int dl = divisor[1];
        for (int j = 0; j < limit - 1; ++j) {
            long nl;
            long rs;
            long estProduct;
            int qhat = 0;
            int qrem = 0;
            boolean skipCorrection = false;
            int nh = rem.value[j + rem.offset];
            int nh2 = nh + Integer.MIN_VALUE;
            int nm = rem.value[j + 1 + rem.offset];
            if (nh == dh) {
                qhat = -1;
                qrem = nh + nm;
                skipCorrection = qrem + Integer.MIN_VALUE < nh2;
            } else {
                long nChunk = (long)nh << 32 | (long)nm & 0xFFFFFFFFL;
                if (nChunk >= 0L) {
                    qhat = (int)(nChunk / dhLong);
                    qrem = (int)(nChunk - (long)qhat * dhLong);
                } else {
                    long tmp = MutableBigInteger.divWord(nChunk, dh);
                    qhat = (int)(tmp & 0xFFFFFFFFL);
                    qrem = (int)(tmp >>> 32);
                }
            }
            if (qhat == 0) continue;
            if (!skipCorrection && this.unsignedLongCompare(estProduct = ((long)dl & 0xFFFFFFFFL) * ((long)qhat & 0xFFFFFFFFL), rs = ((long)qrem & 0xFFFFFFFFL) << 32 | (nl = (long)rem.value[j + 2 + rem.offset] & 0xFFFFFFFFL))) {
                --qhat;
                if (((long)(qrem = (int)(((long)qrem & 0xFFFFFFFFL) + dhLong)) & 0xFFFFFFFFL) >= dhLong && this.unsignedLongCompare(estProduct -= (long)dl & 0xFFFFFFFFL, rs = ((long)qrem & 0xFFFFFFFFL) << 32 | nl)) {
                    --qhat;
                }
            }
            rem.value[j + rem.offset] = 0;
            int borrow = this.mulsub(rem.value, divisor, qhat, dlen, j + rem.offset);
            if (borrow + Integer.MIN_VALUE > nh2) {
                this.divadd(divisor, rem.value, j + 1 + rem.offset);
            }
            q[j] = --qhat;
        }
        int qhat = 0;
        int qrem = 0;
        boolean skipCorrection = false;
        int nh = rem.value[limit - 1 + rem.offset];
        int nh2 = nh + Integer.MIN_VALUE;
        int nm = rem.value[limit + rem.offset];
        if (nh == dh) {
            qhat = -1;
            qrem = nh + nm;
            skipCorrection = qrem + Integer.MIN_VALUE < nh2;
        } else {
            long nChunk = (long)nh << 32 | (long)nm & 0xFFFFFFFFL;
            if (nChunk >= 0L) {
                qhat = (int)(nChunk / dhLong);
                qrem = (int)(nChunk - (long)qhat * dhLong);
            } else {
                long tmp = MutableBigInteger.divWord(nChunk, dh);
                qhat = (int)(tmp & 0xFFFFFFFFL);
                qrem = (int)(tmp >>> 32);
            }
        }
        if (qhat != 0) {
            long nl;
            long rs;
            long estProduct;
            if (!skipCorrection && this.unsignedLongCompare(estProduct = ((long)dl & 0xFFFFFFFFL) * ((long)qhat & 0xFFFFFFFFL), rs = ((long)qrem & 0xFFFFFFFFL) << 32 | (nl = (long)rem.value[limit + 1 + rem.offset] & 0xFFFFFFFFL))) {
                --qhat;
                if (((long)(qrem = (int)(((long)qrem & 0xFFFFFFFFL) + dhLong)) & 0xFFFFFFFFL) >= dhLong && this.unsignedLongCompare(estProduct -= (long)dl & 0xFFFFFFFFL, rs = ((long)qrem & 0xFFFFFFFFL) << 32 | nl)) {
                    --qhat;
                }
            }
            rem.value[limit - 1 + rem.offset] = 0;
            int borrow = needRemainder ? this.mulsub(rem.value, divisor, qhat, dlen, limit - 1 + rem.offset) : this.mulsubBorrow(rem.value, divisor, qhat, dlen, limit - 1 + rem.offset);
            if (borrow + Integer.MIN_VALUE > nh2 && needRemainder) {
                this.divadd(divisor, rem.value, limit - 1 + 1 + rem.offset);
            }
            q[limit - 1] = --qhat;
        }
        if (needRemainder) {
            if (shift > 0) {
                rem.rightShift(shift);
            }
            rem.normalize();
        }
        quotient.normalize();
        return needRemainder ? rem : null;
    }

    private MutableBigInteger divideLongMagnitude(long ldivisor, MutableBigInteger quotient) {
        MutableBigInteger rem = new MutableBigInteger(new int[this.intLen + 1]);
        System.arraycopy(this.value, this.offset, rem.value, 1, this.intLen);
        rem.intLen = this.intLen;
        rem.offset = 1;
        int nlen = rem.intLen;
        int limit = nlen - 2 + 1;
        if (quotient.value.length < limit) {
            quotient.value = new int[limit];
            quotient.offset = 0;
        }
        quotient.intLen = limit;
        int[] q = quotient.value;
        int shift = Long.numberOfLeadingZeros(ldivisor);
        if (shift > 0) {
            ldivisor <<= shift;
            rem.leftShift(shift);
        }
        if (rem.intLen == nlen) {
            rem.offset = 0;
            rem.value[0] = 0;
            ++rem.intLen;
        }
        int dh = (int)(ldivisor >>> 32);
        long dhLong = (long)dh & 0xFFFFFFFFL;
        int dl = (int)(ldivisor & 0xFFFFFFFFL);
        for (int j = 0; j < limit; ++j) {
            long nl;
            long rs;
            long estProduct;
            int qhat = 0;
            int qrem = 0;
            boolean skipCorrection = false;
            int nh = rem.value[j + rem.offset];
            int nh2 = nh + Integer.MIN_VALUE;
            int nm = rem.value[j + 1 + rem.offset];
            if (nh == dh) {
                qhat = -1;
                qrem = nh + nm;
                skipCorrection = qrem + Integer.MIN_VALUE < nh2;
            } else {
                long nChunk = (long)nh << 32 | (long)nm & 0xFFFFFFFFL;
                if (nChunk >= 0L) {
                    qhat = (int)(nChunk / dhLong);
                    qrem = (int)(nChunk - (long)qhat * dhLong);
                } else {
                    long tmp = MutableBigInteger.divWord(nChunk, dh);
                    qhat = (int)(tmp & 0xFFFFFFFFL);
                    qrem = (int)(tmp >>> 32);
                }
            }
            if (qhat == 0) continue;
            if (!skipCorrection && this.unsignedLongCompare(estProduct = ((long)dl & 0xFFFFFFFFL) * ((long)qhat & 0xFFFFFFFFL), rs = ((long)qrem & 0xFFFFFFFFL) << 32 | (nl = (long)rem.value[j + 2 + rem.offset] & 0xFFFFFFFFL))) {
                --qhat;
                if (((long)(qrem = (int)(((long)qrem & 0xFFFFFFFFL) + dhLong)) & 0xFFFFFFFFL) >= dhLong && this.unsignedLongCompare(estProduct -= (long)dl & 0xFFFFFFFFL, rs = ((long)qrem & 0xFFFFFFFFL) << 32 | nl)) {
                    --qhat;
                }
            }
            rem.value[j + rem.offset] = 0;
            int borrow = this.mulsubLong(rem.value, dh, dl, qhat, j + rem.offset);
            if (borrow + Integer.MIN_VALUE > nh2) {
                this.divaddLong(dh, dl, rem.value, j + 1 + rem.offset);
            }
            q[j] = --qhat;
        }
        if (shift > 0) {
            rem.rightShift(shift);
        }
        quotient.normalize();
        rem.normalize();
        return rem;
    }

    private int divaddLong(int dh, int dl, int[] result, int offset) {
        long carry = 0L;
        long sum = ((long)dl & 0xFFFFFFFFL) + ((long)result[1 + offset] & 0xFFFFFFFFL);
        result[1 + offset] = (int)sum;
        sum = ((long)dh & 0xFFFFFFFFL) + ((long)result[offset] & 0xFFFFFFFFL) + carry;
        result[offset] = (int)sum;
        carry = sum >>> 32;
        return (int)carry;
    }

    private int mulsubLong(int[] q, int dh, int dl, int x, int offset) {
        long xLong = (long)x & 0xFFFFFFFFL;
        long product = ((long)dl & 0xFFFFFFFFL) * xLong;
        long difference = (long)q[offset += 2] - product;
        q[offset--] = (int)difference;
        long carry = (product >>> 32) + (long)((difference & 0xFFFFFFFFL) > ((long)(~((int)product)) & 0xFFFFFFFFL) ? 1 : 0);
        product = ((long)dh & 0xFFFFFFFFL) * xLong + carry;
        difference = (long)q[offset] - product;
        q[offset--] = (int)difference;
        carry = (product >>> 32) + (long)((difference & 0xFFFFFFFFL) > ((long)(~((int)product)) & 0xFFFFFFFFL) ? 1 : 0);
        return (int)carry;
    }

    private boolean unsignedLongCompare(long one, long two) {
        return one + Long.MIN_VALUE > two + Long.MIN_VALUE;
    }

    static long divWord(long n, int d) {
        long dLong = (long)d & 0xFFFFFFFFL;
        if (dLong == 1L) {
            long q = (int)n;
            long r = 0L;
            return r << 32 | q & 0xFFFFFFFFL;
        }
        long q = (n >>> 1) / (dLong >>> 1);
        long r = n - q * dLong;
        while (r < 0L) {
            r += dLong;
            --q;
        }
        while (r >= dLong) {
            r -= dLong;
            ++q;
        }
        return r << 32 | q & 0xFFFFFFFFL;
    }

    MutableBigInteger sqrt() {
        if (this.isZero()) {
            return new MutableBigInteger(0);
        }
        if (this.value.length == 1 && ((long)this.value[0] & 0xFFFFFFFFL) < 4L) {
            return ONE;
        }
        if (this.bitLength() <= 63L) {
            long v = new BigInteger(this.value, 1).longValueExact();
            long xk = (long)Math.floor(Math.sqrt(v));
            while (true) {
                long xk1;
                if ((xk1 = (xk + v / xk) / 2L) >= xk) {
                    return new MutableBigInteger(new int[]{(int)(xk >>> 32), (int)(xk & 0xFFFFFFFFL)});
                }
                xk = xk1;
            }
        }
        int bitLength = (int)this.bitLength();
        if ((long)bitLength != this.bitLength()) {
            throw new ArithmeticException("bitLength() integer overflow");
        }
        int shift = bitLength - 63;
        if (shift % 2 == 1) {
            ++shift;
        }
        MutableBigInteger xk = new MutableBigInteger(this);
        xk.rightShift(shift);
        xk.normalize();
        double d = new BigInteger(xk.value, 1).doubleValue();
        BigInteger bi = BigInteger.valueOf((long)Math.ceil(Math.sqrt(d)));
        xk = new MutableBigInteger(bi.mag);
        xk.leftShift(shift / 2);
        MutableBigInteger xk1 = new MutableBigInteger();
        while (true) {
            this.divide(xk, xk1, false);
            xk1.add(xk);
            xk1.rightShift(1);
            if (xk1.compare(xk) >= 0) {
                return xk;
            }
            xk.copyValue(xk1);
            xk1.reset();
        }
    }

    MutableBigInteger hybridGCD(MutableBigInteger b) {
        MutableBigInteger a = this;
        MutableBigInteger q = new MutableBigInteger();
        while (b.intLen != 0) {
            if (Math.abs(a.intLen - b.intLen) < 2) {
                return a.binaryGCD(b);
            }
            MutableBigInteger r = a.divide(b, q);
            a = b;
            b = r;
        }
        return a;
    }

    private MutableBigInteger binaryGCD(MutableBigInteger v) {
        int lb;
        int tsign;
        int s2;
        int k;
        MutableBigInteger u = this;
        MutableBigInteger r = new MutableBigInteger();
        int s1 = u.getLowestSetBit();
        int n = k = s1 < (s2 = v.getLowestSetBit()) ? s1 : s2;
        if (k != 0) {
            u.rightShift(k);
            v.rightShift(k);
        }
        boolean uOdd = k == s1;
        MutableBigInteger t = uOdd ? v : u;
        int n2 = tsign = uOdd ? -1 : 1;
        while ((lb = t.getLowestSetBit()) >= 0) {
            t.rightShift(lb);
            if (tsign > 0) {
                u = t;
            } else {
                v = t;
            }
            if (u.intLen < 2 && v.intLen < 2) {
                int x = u.value[u.offset];
                int y = v.value[v.offset];
                r.value[0] = x = MutableBigInteger.binaryGcd(x, y);
                r.intLen = 1;
                r.offset = 0;
                if (k > 0) {
                    r.leftShift(k);
                }
                return r;
            }
            tsign = u.difference(v);
            if (tsign == 0) break;
            t = tsign >= 0 ? u : v;
        }
        if (k > 0) {
            u.leftShift(k);
        }
        return u;
    }

    static int binaryGcd(int a, int b) {
        int t;
        if (b == 0) {
            return a;
        }
        if (a == 0) {
            return b;
        }
        int aZeros = Integer.numberOfTrailingZeros(a);
        int bZeros = Integer.numberOfTrailingZeros(b);
        a >>>= aZeros;
        b >>>= bZeros;
        int n = t = aZeros < bZeros ? aZeros : bZeros;
        while (a != b) {
            if (a + Integer.MIN_VALUE > b + Integer.MIN_VALUE) {
                a -= b;
                a >>>= Integer.numberOfTrailingZeros(a);
                continue;
            }
            b -= a;
            b >>>= Integer.numberOfTrailingZeros(b);
        }
        return a << t;
    }

    MutableBigInteger mutableModInverse(MutableBigInteger p) {
        if (p.isOdd()) {
            return this.modInverse(p);
        }
        if (this.isEven()) {
            throw new ArithmeticException("BigInteger not invertible.");
        }
        int powersOf2 = p.getLowestSetBit();
        MutableBigInteger oddMod = new MutableBigInteger(p);
        oddMod.rightShift(powersOf2);
        if (oddMod.isOne()) {
            return this.modInverseMP2(powersOf2);
        }
        MutableBigInteger oddPart = this.modInverse(oddMod);
        MutableBigInteger evenPart = this.modInverseMP2(powersOf2);
        MutableBigInteger y1 = MutableBigInteger.modInverseBP2(oddMod, powersOf2);
        MutableBigInteger y2 = oddMod.modInverseMP2(powersOf2);
        MutableBigInteger temp1 = new MutableBigInteger();
        MutableBigInteger temp2 = new MutableBigInteger();
        MutableBigInteger result = new MutableBigInteger();
        oddPart.leftShift(powersOf2);
        oddPart.multiply(y1, result);
        oddPart.clear();
        evenPart.multiply(oddMod, temp1);
        temp1.multiply(y2, temp2);
        result.add(temp2);
        return result.divide(p, temp1);
    }

    MutableBigInteger modInverseMP2(int k) {
        if (this.isEven()) {
            throw new ArithmeticException("Non-invertible. (GCD != 1)");
        }
        if (k > 64) {
            return this.euclidModInverse(k);
        }
        int t = MutableBigInteger.inverseMod32(this.value[this.offset + this.intLen - 1]);
        if (k < 33) {
            t = k == 32 ? t : t & (1 << k) - 1;
            return new MutableBigInteger(t);
        }
        long pLong = (long)this.value[this.offset + this.intLen - 1] & 0xFFFFFFFFL;
        if (this.intLen > 1) {
            pLong |= (long)this.value[this.offset + this.intLen - 2] << 32;
        }
        long tLong = (long)t & 0xFFFFFFFFL;
        tLong *= 2L - pLong * tLong;
        tLong = k == 64 ? tLong : tLong & (1L << k) - 1L;
        MutableBigInteger result = new MutableBigInteger(new int[2]);
        result.value[0] = (int)(tLong >>> 32);
        result.value[1] = (int)tLong;
        result.intLen = 2;
        result.normalize();
        return result;
    }

    static int inverseMod32(int val) {
        int t = val;
        t *= 2 - val * t;
        t *= 2 - val * t;
        t *= 2 - val * t;
        t *= 2 - val * t;
        return t;
    }

    static long inverseMod64(long val) {
        long t = val;
        t *= 2L - val * t;
        t *= 2L - val * t;
        t *= 2L - val * t;
        t *= 2L - val * t;
        t *= 2L - val * t;
        assert (t * val == 1L);
        return t;
    }

    static MutableBigInteger modInverseBP2(MutableBigInteger mod, int k) {
        return MutableBigInteger.fixup(new MutableBigInteger(1), new MutableBigInteger(mod), k);
    }

    private MutableBigInteger modInverse(MutableBigInteger mod) {
        int trailingZeros;
        MutableBigInteger p = new MutableBigInteger(mod);
        MutableBigInteger f = new MutableBigInteger(this);
        MutableBigInteger g = new MutableBigInteger(p);
        SignedMutableBigInteger c = new SignedMutableBigInteger(1);
        SignedMutableBigInteger d = new SignedMutableBigInteger();
        MutableBigInteger temp = null;
        SignedMutableBigInteger sTemp = null;
        int k = 0;
        if (f.isEven()) {
            trailingZeros = f.getLowestSetBit();
            f.rightShift(trailingZeros);
            d.leftShift(trailingZeros);
            k = trailingZeros;
        }
        while (!f.isOne()) {
            if (f.isZero()) {
                throw new ArithmeticException("BigInteger not invertible.");
            }
            if (f.compare(g) < 0) {
                temp = f;
                f = g;
                g = temp;
                sTemp = d;
                d = c;
                c = sTemp;
            }
            if (((f.value[f.offset + f.intLen - 1] ^ g.value[g.offset + g.intLen - 1]) & 3) == 0) {
                f.subtract(g);
                c.signedSubtract(d);
            } else {
                f.add(g);
                c.signedAdd(d);
            }
            trailingZeros = f.getLowestSetBit();
            f.rightShift(trailingZeros);
            d.leftShift(trailingZeros);
            k += trailingZeros;
        }
        if (c.compare(p) >= 0) {
            MutableBigInteger remainder = c.divide(p, new MutableBigInteger());
            c.copyValue(remainder);
        }
        if (c.sign < 0) {
            c.signedAdd(p);
        }
        return MutableBigInteger.fixup(c, p, k);
    }

    static MutableBigInteger fixup(MutableBigInteger c, MutableBigInteger p, int k) {
        MutableBigInteger temp = new MutableBigInteger();
        int r = -MutableBigInteger.inverseMod32(p.value[p.offset + p.intLen - 1]);
        int numWords = k >> 5;
        for (int i = 0; i < numWords; ++i) {
            int v = r * c.value[c.offset + c.intLen - 1];
            p.mul(v, temp);
            c.add(temp);
            --c.intLen;
        }
        int numBits = k & 0x1F;
        if (numBits != 0) {
            int v = r * c.value[c.offset + c.intLen - 1];
            p.mul(v &= (1 << numBits) - 1, temp);
            c.add(temp);
            c.rightShift(numBits);
        }
        if (c.compare(p) >= 0) {
            c = c.divide(p, new MutableBigInteger());
        }
        return c;
    }

    MutableBigInteger euclidModInverse(int k) {
        MutableBigInteger b = new MutableBigInteger(1);
        b.leftShift(k);
        MutableBigInteger mod = new MutableBigInteger(b);
        MutableBigInteger a = new MutableBigInteger(this);
        MutableBigInteger q = new MutableBigInteger();
        MutableBigInteger r = b.divide(a, q);
        MutableBigInteger swapper = b;
        b = r;
        r = swapper;
        MutableBigInteger t1 = new MutableBigInteger(q);
        MutableBigInteger t0 = new MutableBigInteger(1);
        MutableBigInteger temp = new MutableBigInteger();
        while (!b.isOne()) {
            r = a.divide(b, q);
            if (r.intLen == 0) {
                throw new ArithmeticException("BigInteger not invertible.");
            }
            a = swapper = r;
            if (q.intLen == 1) {
                t1.mul(q.value[q.offset], temp);
            } else {
                q.multiply(t1, temp);
            }
            swapper = q;
            q = temp;
            temp = swapper;
            t0.add(q);
            if (a.isOne()) {
                return t0;
            }
            r = b.divide(a, q);
            if (r.intLen == 0) {
                throw new ArithmeticException("BigInteger not invertible.");
            }
            swapper = b;
            b = r;
            if (q.intLen == 1) {
                t0.mul(q.value[q.offset], temp);
            } else {
                q.multiply(t0, temp);
            }
            swapper = q;
            q = temp;
            temp = swapper;
            t1.add(q);
        }
        mod.subtract(t1);
        return mod;
    }
}

