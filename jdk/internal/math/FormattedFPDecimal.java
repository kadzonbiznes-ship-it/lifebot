/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.math;

import jdk.internal.math.DoubleToDecimal;
import jdk.internal.math.MathUtils;

public final class FormattedFPDecimal {
    public static final char SCIENTIFIC = 'e';
    public static final char PLAIN = 'f';
    public static final char GENERAL = 'g';
    private long f;
    private int e;
    private int n;
    private char[] digits;
    private char[] exp;

    private FormattedFPDecimal() {
    }

    public static FormattedFPDecimal valueOf(double v, int prec, char form) {
        FormattedFPDecimal fd = new FormattedFPDecimal();
        DoubleToDecimal.split(v, fd);
        return switch (form) {
            case 'e' -> fd.scientific(prec);
            case 'f' -> fd.plain(prec);
            case 'g' -> fd.general(prec);
            default -> throw new IllegalArgumentException(String.format("unsupported form '%c'", Character.valueOf(form)));
        };
    }

    public void set(long f, int e, int n) {
        this.f = f;
        this.e = e;
        this.n = n;
    }

    public char[] getExponent() {
        return this.exp;
    }

    public char[] getMantissa() {
        return this.digits;
    }

    public int getExponentRounded() {
        return this.n + this.e - 1;
    }

    private FormattedFPDecimal plain(int prec) {
        this.round((long)(this.n + this.e) + (long)prec);
        return this.plainChars();
    }

    private FormattedFPDecimal plainChars() {
        if (this.e >= 0) {
            this.plainCharsPureInteger();
        } else if (this.n + this.e > 0) {
            this.plainCharsMixed();
        } else {
            this.plainCharsPureFraction();
        }
        return this;
    }

    private void plainCharsPureInteger() {
        this.digits = new char[this.n + this.e];
        this.fillWithZeros(this.n, this.n + this.e);
        this.fillWithDigits(this.f, 0, this.n);
    }

    private void plainCharsMixed() {
        this.digits = new char[this.n + 1];
        long x = this.fillWithDigits(this.f, this.n + 1 + this.e, this.n + 1);
        this.digits[this.n + this.e] = 46;
        this.fillWithDigits(x, 0, this.n + this.e);
    }

    private void plainCharsPureFraction() {
        this.digits = new char[2 - this.e];
        long x = this.f;
        this.fillWithDigits(x, 2 - this.e - this.n, 2 - this.e);
        this.fillWithZeros(0, 2 - this.e - this.n);
        this.digits[1] = 46;
    }

    private FormattedFPDecimal scientific(int prec) {
        this.round((long)prec + 1L);
        return this.scientificChars(prec);
    }

    private FormattedFPDecimal scientificChars(int prec) {
        if (prec != 0) {
            this.scientificCharsWithFraction();
        } else {
            this.scientificCharsNoFraction();
        }
        this.expChars();
        return this;
    }

    private void scientificCharsWithFraction() {
        this.digits = new char[1 + this.n];
        long x = this.fillWithDigits(this.f, 2, 1 + this.n);
        this.digits[1] = 46;
        this.digits[0] = FormattedFPDecimal.toDigit(x);
    }

    private void scientificCharsNoFraction() {
        this.digits = new char[1];
        this.digits[0] = FormattedFPDecimal.toDigit(this.f);
    }

    private FormattedFPDecimal general(int prec) {
        this.round(prec);
        int er = this.getExponentRounded();
        if (-4 <= er && er < prec) {
            this.plainChars();
        } else {
            this.scientificChars(prec - 1);
        }
        return this;
    }

    private void expChars() {
        int q;
        int er = this.getExponentRounded();
        int aer = Math.abs(er);
        this.exp = new char[aer >= 100 ? 4 : 3];
        if (aer >= 100) {
            q = aer / 10;
            this.exp[3] = FormattedFPDecimal.toDigit(aer - 10 * q);
            aer = q;
        }
        q = aer / 10;
        this.exp[2] = FormattedFPDecimal.toDigit(aer - 10 * q);
        this.exp[1] = FormattedFPDecimal.toDigit(q);
        this.exp[0] = er >= 0 ? 43 : 45;
    }

    private void round(long pp) {
        if (this.n == 0 || pp < 0L) {
            this.f = 0L;
            this.e = 0;
            this.n = 1;
            return;
        }
        if (pp >= (long)this.n) {
            return;
        }
        int p = (int)pp;
        this.e += this.n - p;
        long pow10 = MathUtils.pow10(this.n - p);
        this.f = (this.f + (pow10 >> 1)) / pow10;
        if (p == 0) {
            this.n = 1;
            if (this.f == 0L) {
                this.e = 0;
            }
            return;
        }
        this.n = p;
        if (this.f == MathUtils.pow10(p)) {
            this.f /= 10L;
            ++this.e;
        }
    }

    private long fillWithDigits(long x, int from, int to) {
        while (to > from) {
            long q = x / 10L;
            this.digits[--to] = FormattedFPDecimal.toDigit(x - q * 10L);
            x = q;
        }
        return x;
    }

    private void fillWithZeros(int from, int to) {
        while (to > from) {
            this.digits[--to] = 48;
        }
    }

    private static char toDigit(long d) {
        return FormattedFPDecimal.toDigit((int)d);
    }

    private static char toDigit(int d) {
        return (char)(d + 48);
    }
}

