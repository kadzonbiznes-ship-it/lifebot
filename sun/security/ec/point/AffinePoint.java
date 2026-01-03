/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec.point;

import java.security.spec.ECPoint;
import java.util.Objects;
import sun.security.util.math.ImmutableIntegerModuloP;
import sun.security.util.math.IntegerFieldModuloP;

public class AffinePoint {
    private final ImmutableIntegerModuloP x;
    private final ImmutableIntegerModuloP y;

    public AffinePoint(ImmutableIntegerModuloP x, ImmutableIntegerModuloP y) {
        this.x = x;
        this.y = y;
    }

    public static AffinePoint fromECPoint(ECPoint ecPoint, IntegerFieldModuloP field) {
        return new AffinePoint(field.getElement(ecPoint.getAffineX()), field.getElement(ecPoint.getAffineY()));
    }

    public ECPoint toECPoint() {
        return new ECPoint(this.x.asBigInteger(), this.y.asBigInteger());
    }

    public ImmutableIntegerModuloP getX() {
        return this.x;
    }

    public ImmutableIntegerModuloP getY() {
        return this.y;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof AffinePoint)) {
            return false;
        }
        AffinePoint p = (AffinePoint)obj;
        boolean xEquals = this.x.asBigInteger().equals(p.x.asBigInteger());
        boolean yEquals = this.y.asBigInteger().equals(p.y.asBigInteger());
        return xEquals && yEquals;
    }

    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }

    public String toString() {
        return "(" + this.x.asBigInteger().toString() + "," + this.y.asBigInteger().toString() + ")";
    }
}

