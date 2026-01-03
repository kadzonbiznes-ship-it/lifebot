/*
 * Decompiled with CFR 0.152.
 */
package java.text;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import jdk.internal.math.FloatingDecimal;

final class DigitList
implements Cloneable {
    public static final int MAX_COUNT = 19;
    public int decimalAt = 0;
    public int count = 0;
    public char[] digits = new char[19];
    private char[] data;
    private RoundingMode roundingMode = RoundingMode.HALF_EVEN;
    private boolean isNegative = false;
    private static final char[] LONG_MIN_REP = "9223372036854775808".toCharArray();
    private StringBuilder tempBuilder;

    DigitList() {
    }

    boolean isZero() {
        for (int i = 0; i < this.count; ++i) {
            if (this.digits[i] == '0') continue;
            return false;
        }
        return true;
    }

    void setRoundingMode(RoundingMode r) {
        this.roundingMode = r;
    }

    public void clear() {
        this.decimalAt = 0;
        this.count = 0;
    }

    public void append(char digit) {
        if (this.count == this.digits.length) {
            char[] data = new char[this.count + 100];
            System.arraycopy(this.digits, 0, data, 0, this.count);
            this.digits = data;
        }
        this.digits[this.count++] = digit;
    }

    public final double getDouble() {
        if (this.count == 0) {
            return 0.0;
        }
        return Double.parseDouble(this.getStringBuilder().append('.').append(this.digits, 0, this.count).append('E').append(this.decimalAt).toString());
    }

    public final long getLong() {
        if (this.count == 0) {
            return 0L;
        }
        if (this.isLongMIN_VALUE()) {
            return Long.MIN_VALUE;
        }
        StringBuilder temp = this.getStringBuilder();
        temp.append(this.digits, 0, this.count);
        for (int i = this.count; i < this.decimalAt; ++i) {
            temp.append('0');
        }
        return Long.parseLong(temp.toString());
    }

    public final BigDecimal getBigDecimal() {
        if (this.count == 0) {
            if (this.decimalAt == 0) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal("0E" + this.decimalAt);
        }
        if (this.decimalAt == this.count) {
            return new BigDecimal(this.digits, 0, this.count);
        }
        return new BigDecimal(this.digits, 0, this.count).scaleByPowerOfTen(this.decimalAt - this.count);
    }

    boolean fitsIntoLong(boolean isPositive, boolean ignoreNegativeZero) {
        while (this.count > 0 && this.digits[this.count - 1] == '0') {
            --this.count;
        }
        if (this.count == 0) {
            return isPositive || ignoreNegativeZero;
        }
        if (this.decimalAt < this.count || this.decimalAt > 19) {
            return false;
        }
        if (this.decimalAt < 19) {
            return true;
        }
        for (int i = 0; i < this.count; ++i) {
            char dig = this.digits[i];
            char max = LONG_MIN_REP[i];
            if (dig > max) {
                return false;
            }
            if (dig >= max) continue;
            return true;
        }
        if (this.count < this.decimalAt) {
            return true;
        }
        return !isPositive;
    }

    final void set(boolean isNegative, double source, int maximumFractionDigits) {
        this.set(isNegative, source, maximumFractionDigits, true);
    }

    final void set(boolean isNegative, double source, int maximumDigits, boolean fixedPoint) {
        FloatingDecimal.BinaryToASCIIConverter fdConverter = FloatingDecimal.getBinaryToASCIIConverter(source);
        boolean hasBeenRoundedUp = fdConverter.digitsRoundedUp();
        boolean valueExactAsDecimal = fdConverter.decimalDigitsExact();
        assert (!fdConverter.isExceptional());
        String digitsString = fdConverter.toJavaFormatString();
        this.set(isNegative, digitsString, hasBeenRoundedUp, valueExactAsDecimal, maximumDigits, fixedPoint);
    }

    private void set(boolean isNegative, String s, boolean roundedUp, boolean valueExactAsDecimal, int maximumDigits, boolean fixedPoint) {
        this.isNegative = isNegative;
        int len = s.length();
        char[] source = this.getDataChars(len);
        s.getChars(0, len, source, 0);
        this.decimalAt = -1;
        this.count = 0;
        int exponent = 0;
        int leadingZerosAfterDecimal = 0;
        boolean nonZeroDigitSeen = false;
        int i = 0;
        while (i < len) {
            char c;
            if ((c = source[i++]) == '.') {
                this.decimalAt = this.count;
                continue;
            }
            if (c == 'e' || c == 'E') {
                exponent = DigitList.parseInt(source, i, len);
                break;
            }
            if (!nonZeroDigitSeen) {
                boolean bl = nonZeroDigitSeen = c != '0';
                if (!nonZeroDigitSeen && this.decimalAt != -1) {
                    ++leadingZerosAfterDecimal;
                }
            }
            if (!nonZeroDigitSeen) continue;
            this.digits[this.count++] = c;
        }
        if (this.decimalAt == -1) {
            this.decimalAt = this.count;
        }
        if (nonZeroDigitSeen) {
            this.decimalAt += exponent - leadingZerosAfterDecimal;
        }
        if (fixedPoint) {
            if (-this.decimalAt > maximumDigits) {
                this.count = 0;
                return;
            }
            if (-this.decimalAt == maximumDigits) {
                if (this.shouldRoundUp(0, roundedUp, valueExactAsDecimal)) {
                    this.count = 1;
                    ++this.decimalAt;
                    this.digits[0] = 49;
                } else {
                    this.count = 0;
                }
                return;
            }
        }
        while (this.count > 1 && this.digits[this.count - 1] == '0') {
            --this.count;
        }
        this.round(fixedPoint ? maximumDigits + this.decimalAt : maximumDigits, roundedUp, valueExactAsDecimal);
    }

    private final void round(int maximumDigits, boolean alreadyRounded, boolean valueExactAsDecimal) {
        if (maximumDigits >= 0 && maximumDigits < this.count) {
            if (this.shouldRoundUp(maximumDigits, alreadyRounded, valueExactAsDecimal)) {
                do {
                    if (--maximumDigits < 0) {
                        this.digits[0] = 49;
                        ++this.decimalAt;
                        maximumDigits = 0;
                        break;
                    }
                    int n = maximumDigits;
                    this.digits[n] = (char)(this.digits[n] + '\u0001');
                } while (this.digits[maximumDigits] > '9');
                ++maximumDigits;
            }
            this.count = maximumDigits;
            while (this.count > 1 && this.digits[this.count - 1] == '0') {
                --this.count;
            }
        }
    }

    private boolean shouldRoundUp(int maximumDigits, boolean alreadyRounded, boolean valueExactAsDecimal) {
        if (maximumDigits < this.count) {
            switch (this.roundingMode) {
                case UP: {
                    for (int i = maximumDigits; i < this.count; ++i) {
                        if (this.digits[i] == '0') continue;
                        return true;
                    }
                    break;
                }
                case DOWN: {
                    break;
                }
                case CEILING: {
                    for (int i = maximumDigits; i < this.count; ++i) {
                        if (this.digits[i] == '0') continue;
                        return !this.isNegative;
                    }
                    break;
                }
                case FLOOR: {
                    for (int i = maximumDigits; i < this.count; ++i) {
                        if (this.digits[i] == '0') continue;
                        return this.isNegative;
                    }
                    break;
                }
                case HALF_UP: 
                case HALF_DOWN: {
                    if (this.digits[maximumDigits] > '5') {
                        return true;
                    }
                    if (this.digits[maximumDigits] != '5') break;
                    if (maximumDigits != this.count - 1) {
                        return true;
                    }
                    if (valueExactAsDecimal) {
                        return this.roundingMode == RoundingMode.HALF_UP;
                    }
                    return !alreadyRounded;
                }
                case HALF_EVEN: {
                    if (this.digits[maximumDigits] > '5') {
                        return true;
                    }
                    if (this.digits[maximumDigits] != '5') break;
                    if (maximumDigits == this.count - 1) {
                        if (alreadyRounded) {
                            return false;
                        }
                        if (!valueExactAsDecimal) {
                            return true;
                        }
                        return maximumDigits > 0 && this.digits[maximumDigits - 1] % 2 != 0;
                    }
                    for (int i = maximumDigits + 1; i < this.count; ++i) {
                        if (this.digits[i] == '0') continue;
                        return true;
                    }
                    break;
                }
                case UNNECESSARY: {
                    for (int i = maximumDigits; i < this.count; ++i) {
                        if (this.digits[i] == '0') continue;
                        throw new ArithmeticException("Rounding needed with the rounding mode being set to RoundingMode.UNNECESSARY");
                    }
                    break;
                }
                default: {
                    assert (false);
                    break;
                }
            }
        }
        return false;
    }

    final void set(boolean isNegative, long source) {
        this.set(isNegative, source, 0);
    }

    final void set(boolean isNegative, long source, int maximumDigits) {
        this.isNegative = isNegative;
        if (source <= 0L) {
            if (source == Long.MIN_VALUE) {
                this.count = 19;
                this.decimalAt = 19;
                System.arraycopy(LONG_MIN_REP, 0, this.digits, 0, this.count);
            } else {
                this.count = 0;
                this.decimalAt = 0;
            }
        } else {
            int left = 19;
            while (source > 0L) {
                this.digits[--left] = (char)(48L + source % 10L);
                source /= 10L;
            }
            this.decimalAt = 19 - left;
            int right = 18;
            while (this.digits[right] == '0') {
                --right;
            }
            this.count = right - left + 1;
            System.arraycopy(this.digits, left, this.digits, 0, this.count);
        }
        if (maximumDigits > 0) {
            this.round(maximumDigits, false, true);
        }
    }

    final void set(boolean isNegative, BigDecimal source, int maximumDigits, boolean fixedPoint) {
        String s = source.toString();
        this.extendDigits(s.length());
        this.set(isNegative, s, false, true, maximumDigits, fixedPoint);
    }

    final void set(boolean isNegative, BigInteger source, int maximumDigits) {
        int right;
        this.isNegative = isNegative;
        String s = source.toString();
        int len = s.length();
        this.extendDigits(len);
        s.getChars(0, len, this.digits, 0);
        this.decimalAt = len;
        for (right = len - 1; right >= 0 && this.digits[right] == '0'; --right) {
        }
        this.count = right + 1;
        if (maximumDigits > 0) {
            this.round(maximumDigits, false, true);
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DigitList)) {
            return false;
        }
        DigitList other = (DigitList)obj;
        if (this.count != other.count || this.decimalAt != other.decimalAt) {
            return false;
        }
        for (int i = 0; i < this.count; ++i) {
            if (this.digits[i] == other.digits[i]) continue;
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hashcode = this.decimalAt;
        for (int i = 0; i < this.count; ++i) {
            hashcode = hashcode * 37 + this.digits[i];
        }
        return hashcode;
    }

    public Object clone() {
        try {
            DigitList other = (DigitList)super.clone();
            char[] newDigits = new char[this.digits.length];
            System.arraycopy(this.digits, 0, newDigits, 0, this.digits.length);
            other.digits = newDigits;
            other.tempBuilder = null;
            return other;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    private boolean isLongMIN_VALUE() {
        if (this.decimalAt != this.count || this.count != 19) {
            return false;
        }
        for (int i = 0; i < this.count; ++i) {
            if (this.digits[i] == LONG_MIN_REP[i]) continue;
            return false;
        }
        return true;
    }

    private static final int parseInt(char[] str, int offset, int strLen) {
        boolean positive = true;
        char c = str[offset];
        if (c == '-') {
            positive = false;
            ++offset;
        } else if (c == '+') {
            ++offset;
        }
        int value = 0;
        while (offset < strLen && (c = str[offset++]) >= '0' && c <= '9') {
            value = value * 10 + (c - 48);
        }
        return positive ? value : -value;
    }

    public String toString() {
        if (this.isZero()) {
            return "0";
        }
        return this.getStringBuilder().append("0.").append(this.digits, 0, this.count).append("x10^").append(this.decimalAt).toString();
    }

    private StringBuilder getStringBuilder() {
        if (this.tempBuilder == null) {
            this.tempBuilder = new StringBuilder(19);
        } else {
            this.tempBuilder.setLength(0);
        }
        return this.tempBuilder;
    }

    private void extendDigits(int len) {
        if (len > this.digits.length) {
            this.digits = new char[len];
        }
    }

    private final char[] getDataChars(int length) {
        if (this.data == null || this.data.length < length) {
            this.data = new char[length];
        }
        return this.data;
    }
}

