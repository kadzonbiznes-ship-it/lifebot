/*
 * Decompiled with CFR 0.152.
 */
package java.security.spec;

import java.math.BigInteger;

public class ECPoint {
    private final BigInteger x;
    private final BigInteger y;
    public static final ECPoint POINT_INFINITY = new ECPoint();

    private ECPoint() {
        this.x = null;
        this.y = null;
    }

    public ECPoint(BigInteger x, BigInteger y) {
        if (x == null || y == null) {
            throw new NullPointerException("affine coordinate x or y is null");
        }
        this.x = x;
        this.y = y;
    }

    public BigInteger getAffineX() {
        return this.x;
    }

    public BigInteger getAffineY() {
        return this.y;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (this == POINT_INFINITY) {
            return false;
        }
        if (!(obj instanceof ECPoint)) return false;
        ECPoint other = (ECPoint)obj;
        if (!this.x.equals(other.x)) return false;
        if (!this.y.equals(other.y)) return false;
        return true;
    }

    public int hashCode() {
        if (this == POINT_INFINITY) {
            return 0;
        }
        return this.x.hashCode() << 5 + this.y.hashCode();
    }
}

