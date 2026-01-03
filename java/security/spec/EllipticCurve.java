/*
 * Decompiled with CFR 0.152.
 */
package java.security.spec;

import java.math.BigInteger;
import java.security.spec.ECField;
import java.security.spec.ECFieldF2m;
import java.security.spec.ECFieldFp;

public class EllipticCurve {
    private final ECField field;
    private final BigInteger a;
    private final BigInteger b;
    private final byte[] seed;

    private static void checkValidity(ECField field, BigInteger c, String cName) {
        if (field instanceof ECFieldFp) {
            BigInteger p = ((ECFieldFp)field).getP();
            if (p.compareTo(c) != 1) {
                throw new IllegalArgumentException(cName + " is too large");
            }
            if (c.signum() < 0) {
                throw new IllegalArgumentException(cName + " is negative");
            }
        } else if (field instanceof ECFieldF2m) {
            int m = ((ECFieldF2m)field).getM();
            if (c.bitLength() > m) {
                throw new IllegalArgumentException(cName + " is too large");
            }
        }
    }

    public EllipticCurve(ECField field, BigInteger a, BigInteger b) {
        this(field, a, b, null);
    }

    public EllipticCurve(ECField field, BigInteger a, BigInteger b, byte[] seed) {
        if (field == null) {
            throw new NullPointerException("field is null");
        }
        if (a == null) {
            throw new NullPointerException("first coefficient is null");
        }
        if (b == null) {
            throw new NullPointerException("second coefficient is null");
        }
        EllipticCurve.checkValidity(field, a, "first coefficient");
        EllipticCurve.checkValidity(field, b, "second coefficient");
        this.field = field;
        this.a = a;
        this.b = b;
        this.seed = (byte[])(seed != null ? (byte[])seed.clone() : null);
    }

    public ECField getField() {
        return this.field;
    }

    public BigInteger getA() {
        return this.a;
    }

    public BigInteger getB() {
        return this.b;
    }

    public byte[] getSeed() {
        if (this.seed == null) {
            return null;
        }
        return (byte[])this.seed.clone();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EllipticCurve)) return false;
        EllipticCurve other = (EllipticCurve)obj;
        if (!this.field.equals(other.field)) return false;
        if (!this.a.equals(other.a)) return false;
        if (!this.b.equals(other.b)) return false;
        return true;
    }

    public int hashCode() {
        return this.field.hashCode() << 6 + (this.a.hashCode() << 4) + (this.b.hashCode() << 2);
    }
}

