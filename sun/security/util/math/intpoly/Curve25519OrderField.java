/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util.math.intpoly;

import java.math.BigInteger;
import sun.security.util.math.intpoly.IntegerPolynomial;

public final class Curve25519OrderField
extends IntegerPolynomial {
    private static final int BITS_PER_LIMB = 26;
    private static final int NUM_LIMBS = 10;
    private static final int MAX_ADDS = 1;
    public static final BigInteger MODULUS = Curve25519OrderField.evaluateModulus();
    private static final long CARRY_ADD = 0x2000000L;
    private static final int LIMB_MASK = 0x3FFFFFF;
    public static final Curve25519OrderField ONE = new Curve25519OrderField();

    private Curve25519OrderField() {
        super(26, 10, 1, MODULUS);
    }

    private static BigInteger evaluateModulus() {
        BigInteger result = BigInteger.valueOf(2L).pow(252);
        result = result.add(BigInteger.valueOf(16110573L));
        result = result.add(BigInteger.valueOf(10012311L).shiftLeft(26));
        result = result.add(BigInteger.valueOf(30238081L).shiftLeft(52));
        result = result.subtract(BigInteger.valueOf(8746018L).shiftLeft(78));
        result = result.add(BigInteger.valueOf(1367802L).shiftLeft(104));
        return result;
    }

    @Override
    protected void reduceIn(long[] limbs, long v, int i) {
        long t0 = -16110573L * v;
        int n = i - 10;
        limbs[n] = limbs[n] + (t0 << 8 & 0x3FFFFFFL);
        int n2 = i - 9;
        limbs[n2] = limbs[n2] + (t0 >> 18);
        long t1 = -10012311L * v;
        int n3 = i - 9;
        limbs[n3] = limbs[n3] + (t1 << 8 & 0x3FFFFFFL);
        int n4 = i - 8;
        limbs[n4] = limbs[n4] + (t1 >> 18);
        long t2 = -30238081L * v;
        int n5 = i - 8;
        limbs[n5] = limbs[n5] + (t2 << 8 & 0x3FFFFFFL);
        int n6 = i - 7;
        limbs[n6] = limbs[n6] + (t2 >> 18);
        long t3 = 8746018L * v;
        int n7 = i - 7;
        limbs[n7] = limbs[n7] + (t3 << 8 & 0x3FFFFFFL);
        int n8 = i - 6;
        limbs[n8] = limbs[n8] + (t3 >> 18);
        long t4 = -1367802L * v;
        int n9 = i - 6;
        limbs[n9] = limbs[n9] + (t4 << 8 & 0x3FFFFFFL);
        int n10 = i - 5;
        limbs[n10] = limbs[n10] + (t4 >> 18);
    }

    @Override
    protected void finalCarryReduceLast(long[] limbs) {
        long c = limbs[9] >> 18;
        limbs[9] = limbs[9] - (c << 18);
        long t5 = -16110573L * c;
        limbs[0] = limbs[0] + t5;
        t5 = -10012311L * c;
        limbs[1] = limbs[1] + t5;
        t5 = -30238081L * c;
        limbs[2] = limbs[2] + t5;
        t5 = 8746018L * c;
        limbs[3] = limbs[3] + t5;
        t5 = -1367802L * c;
        limbs[4] = limbs[4] + t5;
    }

    private void carryReduce(long[] r, long c0, long c1, long c2, long c3, long c4, long c5, long c6, long c7, long c8, long c9, long c10, long c11, long c12, long c13, long c14, long c15, long c16, long c17, long c18) {
        long c19 = 0L;
        long t0 = c0 + 0x2000000L >> 26;
        c0 -= t0 << 26;
        c1 += t0;
        t0 = c1 + 0x2000000L >> 26;
        c1 -= t0 << 26;
        c2 += t0;
        t0 = c2 + 0x2000000L >> 26;
        c2 -= t0 << 26;
        c3 += t0;
        t0 = c3 + 0x2000000L >> 26;
        c3 -= t0 << 26;
        c4 += t0;
        t0 = c4 + 0x2000000L >> 26;
        c4 -= t0 << 26;
        c5 += t0;
        t0 = c5 + 0x2000000L >> 26;
        c5 -= t0 << 26;
        c6 += t0;
        t0 = c6 + 0x2000000L >> 26;
        c6 -= t0 << 26;
        c7 += t0;
        t0 = c7 + 0x2000000L >> 26;
        c7 -= t0 << 26;
        c8 += t0;
        t0 = c8 + 0x2000000L >> 26;
        c8 -= t0 << 26;
        c9 += t0;
        t0 = c9 + 0x2000000L >> 26;
        c9 -= t0 << 26;
        c10 += t0;
        t0 = c10 + 0x2000000L >> 26;
        c10 -= t0 << 26;
        c11 += t0;
        t0 = c11 + 0x2000000L >> 26;
        c11 -= t0 << 26;
        c12 += t0;
        t0 = c12 + 0x2000000L >> 26;
        c12 -= t0 << 26;
        c13 += t0;
        t0 = c13 + 0x2000000L >> 26;
        c13 -= t0 << 26;
        c14 += t0;
        t0 = c14 + 0x2000000L >> 26;
        c14 -= t0 << 26;
        c15 += t0;
        t0 = c15 + 0x2000000L >> 26;
        c15 -= t0 << 26;
        c16 += t0;
        t0 = c16 + 0x2000000L >> 26;
        c16 -= t0 << 26;
        c17 += t0;
        t0 = c17 + 0x2000000L >> 26;
        c17 -= t0 << 26;
        c18 += t0;
        t0 = c18 + 0x2000000L >> 26;
        this.carryReduce0(r, c0, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17, c18 -= t0 << 26, c19 += t0);
    }

    void carryReduce0(long[] r, long c0, long c1, long c2, long c3, long c4, long c5, long c6, long c7, long c8, long c9, long c10, long c11, long c12, long c13, long c14, long c15, long c16, long c17, long c18, long c19) {
        long t0 = -16110573L * c19;
        c9 += t0 << 8 & 0x3FFFFFFL;
        c10 += t0 >> 18;
        t0 = -10012311L * c19;
        c10 += t0 << 8 & 0x3FFFFFFL;
        c11 += t0 >> 18;
        t0 = -30238081L * c19;
        c11 += t0 << 8 & 0x3FFFFFFL;
        c12 += t0 >> 18;
        t0 = 8746018L * c19;
        c12 += t0 << 8 & 0x3FFFFFFL;
        c13 += t0 >> 18;
        t0 = -1367802L * c19;
        c13 += t0 << 8 & 0x3FFFFFFL;
        c14 += t0 >> 18;
        t0 = -16110573L * c18;
        c8 += t0 << 8 & 0x3FFFFFFL;
        c9 += t0 >> 18;
        t0 = -10012311L * c18;
        c9 += t0 << 8 & 0x3FFFFFFL;
        c10 += t0 >> 18;
        t0 = -30238081L * c18;
        c10 += t0 << 8 & 0x3FFFFFFL;
        c11 += t0 >> 18;
        t0 = 8746018L * c18;
        c11 += t0 << 8 & 0x3FFFFFFL;
        c12 += t0 >> 18;
        t0 = -1367802L * c18;
        c12 += t0 << 8 & 0x3FFFFFFL;
        c13 += t0 >> 18;
        t0 = -16110573L * c17;
        c7 += t0 << 8 & 0x3FFFFFFL;
        c8 += t0 >> 18;
        t0 = -10012311L * c17;
        c8 += t0 << 8 & 0x3FFFFFFL;
        c9 += t0 >> 18;
        t0 = -30238081L * c17;
        c9 += t0 << 8 & 0x3FFFFFFL;
        c10 += t0 >> 18;
        t0 = 8746018L * c17;
        c10 += t0 << 8 & 0x3FFFFFFL;
        c11 += t0 >> 18;
        t0 = -1367802L * c17;
        c11 += t0 << 8 & 0x3FFFFFFL;
        c12 += t0 >> 18;
        t0 = -16110573L * c16;
        c6 += t0 << 8 & 0x3FFFFFFL;
        c7 += t0 >> 18;
        t0 = -10012311L * c16;
        c7 += t0 << 8 & 0x3FFFFFFL;
        c8 += t0 >> 18;
        t0 = -30238081L * c16;
        c8 += t0 << 8 & 0x3FFFFFFL;
        c9 += t0 >> 18;
        t0 = 8746018L * c16;
        c9 += t0 << 8 & 0x3FFFFFFL;
        c10 += t0 >> 18;
        t0 = -1367802L * c16;
        c10 += t0 << 8 & 0x3FFFFFFL;
        c11 += t0 >> 18;
        t0 = -16110573L * c15;
        c5 += t0 << 8 & 0x3FFFFFFL;
        c6 += t0 >> 18;
        t0 = -10012311L * c15;
        c6 += t0 << 8 & 0x3FFFFFFL;
        c7 += t0 >> 18;
        t0 = -30238081L * c15;
        c7 += t0 << 8 & 0x3FFFFFFL;
        c8 += t0 >> 18;
        t0 = 8746018L * c15;
        c8 += t0 << 8 & 0x3FFFFFFL;
        c9 += t0 >> 18;
        t0 = -1367802L * c15;
        c9 += t0 << 8 & 0x3FFFFFFL;
        c10 += t0 >> 18;
        t0 = -16110573L * c14;
        c4 += t0 << 8 & 0x3FFFFFFL;
        c5 += t0 >> 18;
        t0 = -10012311L * c14;
        c5 += t0 << 8 & 0x3FFFFFFL;
        c6 += t0 >> 18;
        t0 = -30238081L * c14;
        c6 += t0 << 8 & 0x3FFFFFFL;
        c7 += t0 >> 18;
        t0 = 8746018L * c14;
        c7 += t0 << 8 & 0x3FFFFFFL;
        c8 += t0 >> 18;
        t0 = -1367802L * c14;
        c8 += t0 << 8 & 0x3FFFFFFL;
        c9 += t0 >> 18;
        t0 = -16110573L * c13;
        c3 += t0 << 8 & 0x3FFFFFFL;
        c4 += t0 >> 18;
        t0 = -10012311L * c13;
        c4 += t0 << 8 & 0x3FFFFFFL;
        c5 += t0 >> 18;
        t0 = -30238081L * c13;
        c5 += t0 << 8 & 0x3FFFFFFL;
        c6 += t0 >> 18;
        t0 = 8746018L * c13;
        c6 += t0 << 8 & 0x3FFFFFFL;
        c7 += t0 >> 18;
        t0 = -1367802L * c13;
        c7 += t0 << 8 & 0x3FFFFFFL;
        c8 += t0 >> 18;
        t0 = -16110573L * c12;
        c2 += t0 << 8 & 0x3FFFFFFL;
        c3 += t0 >> 18;
        t0 = -10012311L * c12;
        c3 += t0 << 8 & 0x3FFFFFFL;
        c4 += t0 >> 18;
        t0 = -30238081L * c12;
        c4 += t0 << 8 & 0x3FFFFFFL;
        c5 += t0 >> 18;
        t0 = 8746018L * c12;
        c5 += t0 << 8 & 0x3FFFFFFL;
        c6 += t0 >> 18;
        t0 = -1367802L * c12;
        c6 += t0 << 8 & 0x3FFFFFFL;
        c7 += t0 >> 18;
        t0 = -16110573L * c11;
        c1 += t0 << 8 & 0x3FFFFFFL;
        c2 += t0 >> 18;
        t0 = -10012311L * c11;
        c2 += t0 << 8 & 0x3FFFFFFL;
        c3 += t0 >> 18;
        t0 = -30238081L * c11;
        c3 += t0 << 8 & 0x3FFFFFFL;
        c4 += t0 >> 18;
        t0 = 8746018L * c11;
        c4 += t0 << 8 & 0x3FFFFFFL;
        c5 += t0 >> 18;
        t0 = -1367802L * c11;
        c5 += t0 << 8 & 0x3FFFFFFL;
        c6 += t0 >> 18;
        t0 = -16110573L * c10;
        c0 += t0 << 8 & 0x3FFFFFFL;
        c1 += t0 >> 18;
        t0 = -10012311L * c10;
        c1 += t0 << 8 & 0x3FFFFFFL;
        c2 += t0 >> 18;
        t0 = -30238081L * c10;
        c2 += t0 << 8 & 0x3FFFFFFL;
        c3 += t0 >> 18;
        t0 = 8746018L * c10;
        c3 += t0 << 8 & 0x3FFFFFFL;
        c4 += t0 >> 18;
        t0 = -1367802L * c10;
        c10 = 0L;
        this.carryReduce1(r, c0, c1, c2, c3, c4 += t0 << 8 & 0x3FFFFFFL, c5 += t0 >> 18, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17, c18, c19);
    }

    void carryReduce1(long[] r, long c0, long c1, long c2, long c3, long c4, long c5, long c6, long c7, long c8, long c9, long c10, long c11, long c12, long c13, long c14, long c15, long c16, long c17, long c18, long c19) {
        long t0 = c0 + 0x2000000L >> 26;
        c0 -= t0 << 26;
        c1 += t0;
        t0 = c1 + 0x2000000L >> 26;
        c1 -= t0 << 26;
        c2 += t0;
        t0 = c2 + 0x2000000L >> 26;
        c2 -= t0 << 26;
        c3 += t0;
        t0 = c3 + 0x2000000L >> 26;
        c3 -= t0 << 26;
        c4 += t0;
        t0 = c4 + 0x2000000L >> 26;
        c4 -= t0 << 26;
        c5 += t0;
        t0 = c5 + 0x2000000L >> 26;
        c5 -= t0 << 26;
        c6 += t0;
        t0 = c6 + 0x2000000L >> 26;
        c6 -= t0 << 26;
        c7 += t0;
        t0 = c7 + 0x2000000L >> 26;
        c7 -= t0 << 26;
        c8 += t0;
        t0 = c8 + 0x2000000L >> 26;
        c8 -= t0 << 26;
        c9 += t0;
        t0 = c9 + 0x2000000L >> 26;
        this.carryReduce2(r, c0, c1, c2, c3, c4, c5, c6, c7, c8, c9 -= t0 << 26, c10 += t0, c11, c12, c13, c14, c15, c16, c17, c18, c19);
    }

    void carryReduce2(long[] r, long c0, long c1, long c2, long c3, long c4, long c5, long c6, long c7, long c8, long c9, long c10, long c11, long c12, long c13, long c14, long c15, long c16, long c17, long c18, long c19) {
        long t0 = -16110573L * c10;
        c0 += t0 << 8 & 0x3FFFFFFL;
        c1 += t0 >> 18;
        t0 = -10012311L * c10;
        c1 += t0 << 8 & 0x3FFFFFFL;
        c2 += t0 >> 18;
        t0 = -30238081L * c10;
        c2 += t0 << 8 & 0x3FFFFFFL;
        c3 += t0 >> 18;
        t0 = 8746018L * c10;
        c3 += t0 << 8 & 0x3FFFFFFL;
        c4 += t0 >> 18;
        t0 = -1367802L * c10;
        c4 += t0 << 8 & 0x3FFFFFFL;
        c5 += t0 >> 18;
        t0 = c0 + 0x2000000L >> 26;
        c0 -= t0 << 26;
        c1 += t0;
        t0 = c1 + 0x2000000L >> 26;
        c1 -= t0 << 26;
        c2 += t0;
        t0 = c2 + 0x2000000L >> 26;
        c2 -= t0 << 26;
        c3 += t0;
        t0 = c3 + 0x2000000L >> 26;
        c3 -= t0 << 26;
        c4 += t0;
        t0 = c4 + 0x2000000L >> 26;
        c4 -= t0 << 26;
        c5 += t0;
        t0 = c5 + 0x2000000L >> 26;
        c5 -= t0 << 26;
        c6 += t0;
        t0 = c6 + 0x2000000L >> 26;
        c6 -= t0 << 26;
        c7 += t0;
        t0 = c7 + 0x2000000L >> 26;
        c7 -= t0 << 26;
        c8 += t0;
        t0 = c8 + 0x2000000L >> 26;
        c8 -= t0 << 26;
        c9 += t0;
        r[0] = c0;
        r[1] = c1;
        r[2] = c2;
        r[3] = c3;
        r[4] = c4;
        r[5] = c5;
        r[6] = c6;
        r[7] = c7;
        r[8] = c8;
        r[9] = c9;
    }

    private void carryReduce(long[] r, long c0, long c1, long c2, long c3, long c4, long c5, long c6, long c7, long c8, long c9) {
        long c10 = 0L;
        long t0 = c0 + 0x2000000L >> 26;
        c0 -= t0 << 26;
        c1 += t0;
        t0 = c1 + 0x2000000L >> 26;
        c1 -= t0 << 26;
        c2 += t0;
        t0 = c2 + 0x2000000L >> 26;
        c2 -= t0 << 26;
        c3 += t0;
        t0 = c3 + 0x2000000L >> 26;
        c3 -= t0 << 26;
        c4 += t0;
        t0 = c4 + 0x2000000L >> 26;
        c4 -= t0 << 26;
        c5 += t0;
        t0 = c5 + 0x2000000L >> 26;
        c5 -= t0 << 26;
        c6 += t0;
        t0 = c6 + 0x2000000L >> 26;
        c6 -= t0 << 26;
        c7 += t0;
        t0 = c7 + 0x2000000L >> 26;
        c7 -= t0 << 26;
        c8 += t0;
        t0 = c8 + 0x2000000L >> 26;
        c8 -= t0 << 26;
        c9 += t0;
        t0 = c9 + 0x2000000L >> 26;
        this.carryReduce0(r, c0, c1, c2, c3, c4, c5, c6, c7, c8, c9 -= t0 << 26, c10 += t0);
    }

    void carryReduce0(long[] r, long c0, long c1, long c2, long c3, long c4, long c5, long c6, long c7, long c8, long c9, long c10) {
        long t0 = -16110573L * c10;
        c0 += t0 << 8 & 0x3FFFFFFL;
        c1 += t0 >> 18;
        t0 = -10012311L * c10;
        c1 += t0 << 8 & 0x3FFFFFFL;
        c2 += t0 >> 18;
        t0 = -30238081L * c10;
        c2 += t0 << 8 & 0x3FFFFFFL;
        c3 += t0 >> 18;
        t0 = 8746018L * c10;
        c3 += t0 << 8 & 0x3FFFFFFL;
        c4 += t0 >> 18;
        t0 = -1367802L * c10;
        c4 += t0 << 8 & 0x3FFFFFFL;
        c5 += t0 >> 18;
        t0 = c0 + 0x2000000L >> 26;
        c0 -= t0 << 26;
        c1 += t0;
        t0 = c1 + 0x2000000L >> 26;
        c1 -= t0 << 26;
        c2 += t0;
        t0 = c2 + 0x2000000L >> 26;
        c2 -= t0 << 26;
        c3 += t0;
        t0 = c3 + 0x2000000L >> 26;
        c3 -= t0 << 26;
        c4 += t0;
        t0 = c4 + 0x2000000L >> 26;
        c4 -= t0 << 26;
        c5 += t0;
        t0 = c5 + 0x2000000L >> 26;
        c5 -= t0 << 26;
        c6 += t0;
        t0 = c6 + 0x2000000L >> 26;
        c6 -= t0 << 26;
        c7 += t0;
        t0 = c7 + 0x2000000L >> 26;
        c7 -= t0 << 26;
        c8 += t0;
        t0 = c8 + 0x2000000L >> 26;
        c8 -= t0 << 26;
        c9 += t0;
        r[0] = c0;
        r[1] = c1;
        r[2] = c2;
        r[3] = c3;
        r[4] = c4;
        r[5] = c5;
        r[6] = c6;
        r[7] = c7;
        r[8] = c8;
        r[9] = c9;
    }

    @Override
    protected void mult(long[] a, long[] b, long[] r) {
        long c0 = a[0] * b[0];
        long c1 = a[0] * b[1] + a[1] * b[0];
        long c2 = a[0] * b[2] + a[1] * b[1] + a[2] * b[0];
        long c3 = a[0] * b[3] + a[1] * b[2] + a[2] * b[1] + a[3] * b[0];
        long c4 = a[0] * b[4] + a[1] * b[3] + a[2] * b[2] + a[3] * b[1] + a[4] * b[0];
        long c5 = a[0] * b[5] + a[1] * b[4] + a[2] * b[3] + a[3] * b[2] + a[4] * b[1] + a[5] * b[0];
        long c6 = a[0] * b[6] + a[1] * b[5] + a[2] * b[4] + a[3] * b[3] + a[4] * b[2] + a[5] * b[1] + a[6] * b[0];
        long c7 = a[0] * b[7] + a[1] * b[6] + a[2] * b[5] + a[3] * b[4] + a[4] * b[3] + a[5] * b[2] + a[6] * b[1] + a[7] * b[0];
        long c8 = a[0] * b[8] + a[1] * b[7] + a[2] * b[6] + a[3] * b[5] + a[4] * b[4] + a[5] * b[3] + a[6] * b[2] + a[7] * b[1] + a[8] * b[0];
        long c9 = a[0] * b[9] + a[1] * b[8] + a[2] * b[7] + a[3] * b[6] + a[4] * b[5] + a[5] * b[4] + a[6] * b[3] + a[7] * b[2] + a[8] * b[1] + a[9] * b[0];
        long c10 = a[1] * b[9] + a[2] * b[8] + a[3] * b[7] + a[4] * b[6] + a[5] * b[5] + a[6] * b[4] + a[7] * b[3] + a[8] * b[2] + a[9] * b[1];
        long c11 = a[2] * b[9] + a[3] * b[8] + a[4] * b[7] + a[5] * b[6] + a[6] * b[5] + a[7] * b[4] + a[8] * b[3] + a[9] * b[2];
        long c12 = a[3] * b[9] + a[4] * b[8] + a[5] * b[7] + a[6] * b[6] + a[7] * b[5] + a[8] * b[4] + a[9] * b[3];
        long c13 = a[4] * b[9] + a[5] * b[8] + a[6] * b[7] + a[7] * b[6] + a[8] * b[5] + a[9] * b[4];
        long c14 = a[5] * b[9] + a[6] * b[8] + a[7] * b[7] + a[8] * b[6] + a[9] * b[5];
        long c15 = a[6] * b[9] + a[7] * b[8] + a[8] * b[7] + a[9] * b[6];
        long c16 = a[7] * b[9] + a[8] * b[8] + a[9] * b[7];
        long c17 = a[8] * b[9] + a[9] * b[8];
        long c18 = a[9] * b[9];
        this.carryReduce(r, c0, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17, c18);
    }

    @Override
    protected void reduce(long[] a) {
        this.carryReduce(a, a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7], a[8], a[9]);
    }

    @Override
    protected void square(long[] a, long[] r) {
        long c0 = a[0] * a[0];
        long c1 = 2L * (a[0] * a[1]);
        long c2 = 2L * (a[0] * a[2]) + a[1] * a[1];
        long c3 = 2L * (a[0] * a[3] + a[1] * a[2]);
        long c4 = 2L * (a[0] * a[4] + a[1] * a[3]) + a[2] * a[2];
        long c5 = 2L * (a[0] * a[5] + a[1] * a[4] + a[2] * a[3]);
        long c6 = 2L * (a[0] * a[6] + a[1] * a[5] + a[2] * a[4]) + a[3] * a[3];
        long c7 = 2L * (a[0] * a[7] + a[1] * a[6] + a[2] * a[5] + a[3] * a[4]);
        long c8 = 2L * (a[0] * a[8] + a[1] * a[7] + a[2] * a[6] + a[3] * a[5]) + a[4] * a[4];
        long c9 = 2L * (a[0] * a[9] + a[1] * a[8] + a[2] * a[7] + a[3] * a[6] + a[4] * a[5]);
        long c10 = 2L * (a[1] * a[9] + a[2] * a[8] + a[3] * a[7] + a[4] * a[6]) + a[5] * a[5];
        long c11 = 2L * (a[2] * a[9] + a[3] * a[8] + a[4] * a[7] + a[5] * a[6]);
        long c12 = 2L * (a[3] * a[9] + a[4] * a[8] + a[5] * a[7]) + a[6] * a[6];
        long c13 = 2L * (a[4] * a[9] + a[5] * a[8] + a[6] * a[7]);
        long c14 = 2L * (a[5] * a[9] + a[6] * a[8]) + a[7] * a[7];
        long c15 = 2L * (a[6] * a[9] + a[7] * a[8]);
        long c16 = 2L * (a[7] * a[9]) + a[8] * a[8];
        long c17 = 2L * (a[8] * a[9]);
        long c18 = a[9] * a[9];
        this.carryReduce(r, c0, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17, c18);
    }
}

