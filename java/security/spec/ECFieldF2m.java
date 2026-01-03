/*
 * Decompiled with CFR 0.152.
 */
package java.security.spec;

import java.math.BigInteger;
import java.security.spec.ECField;
import java.util.Arrays;

public class ECFieldF2m
implements ECField {
    private final int m;
    private final int[] ks;
    private BigInteger rp;

    public ECFieldF2m(int m) {
        if (m <= 0) {
            throw new IllegalArgumentException("m is not positive");
        }
        this.m = m;
        this.ks = null;
        this.rp = null;
    }

    public ECFieldF2m(int m, BigInteger rp) {
        this.m = m;
        this.rp = rp;
        if (m <= 0) {
            throw new IllegalArgumentException("m is not positive");
        }
        int bitCount = this.rp.bitCount();
        if (!this.rp.testBit(0) || !this.rp.testBit(m) || bitCount != 3 && bitCount != 5) {
            throw new IllegalArgumentException("rp does not represent a valid reduction polynomial");
        }
        BigInteger temp = this.rp.clearBit(0).clearBit(m);
        this.ks = new int[bitCount - 2];
        for (int i = this.ks.length - 1; i >= 0; --i) {
            int index;
            this.ks[i] = index = temp.getLowestSetBit();
            temp = temp.clearBit(index);
        }
    }

    public ECFieldF2m(int m, int[] ks) {
        this.m = m;
        this.ks = (int[])ks.clone();
        if (m <= 0) {
            throw new IllegalArgumentException("m is not positive");
        }
        if (this.ks.length != 1 && this.ks.length != 3) {
            throw new IllegalArgumentException("length of ks is neither 1 nor 3");
        }
        for (int i = 0; i < this.ks.length; ++i) {
            if (this.ks[i] < 1 || this.ks[i] > m - 1) {
                throw new IllegalArgumentException("ks[" + i + "] is out of range");
            }
            if (i == 0 || this.ks[i] < this.ks[i - 1]) continue;
            throw new IllegalArgumentException("values in ks are not in descending order");
        }
        this.rp = BigInteger.ONE;
        this.rp = this.rp.setBit(m);
        for (int j = 0; j < this.ks.length; ++j) {
            this.rp = this.rp.setBit(this.ks[j]);
        }
    }

    @Override
    public int getFieldSize() {
        return this.m;
    }

    public int getM() {
        return this.m;
    }

    public BigInteger getReductionPolynomial() {
        return this.rp;
    }

    public int[] getMidTermsOfReductionPolynomial() {
        if (this.ks == null) {
            return null;
        }
        return (int[])this.ks.clone();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ECFieldF2m)) return false;
        ECFieldF2m other = (ECFieldF2m)obj;
        if (this.m != other.m) return false;
        if (!Arrays.equals(this.ks, other.ks)) return false;
        return true;
    }

    public int hashCode() {
        int value = this.m << 5;
        return value += this.rp == null ? 0 : this.rp.hashCode();
    }
}

