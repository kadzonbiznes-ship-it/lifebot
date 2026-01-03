/*
 * Decompiled with CFR 0.152.
 */
package java.security.spec;

import java.math.BigInteger;
import java.security.spec.ECField;

public class ECFieldFp
implements ECField {
    private final BigInteger p;

    public ECFieldFp(BigInteger p) {
        if (p.signum() != 1) {
            throw new IllegalArgumentException("p is not positive");
        }
        this.p = p;
    }

    @Override
    public int getFieldSize() {
        return this.p.bitLength();
    }

    public BigInteger getP() {
        return this.p;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ECFieldFp)) return false;
        ECFieldFp other = (ECFieldFp)obj;
        if (!this.p.equals(other.p)) return false;
        return true;
    }

    public int hashCode() {
        return this.p.hashCode();
    }
}

