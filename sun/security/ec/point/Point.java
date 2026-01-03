/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec.point;

import sun.security.ec.point.AffinePoint;
import sun.security.ec.point.ImmutablePoint;
import sun.security.ec.point.MutablePoint;
import sun.security.util.math.IntegerFieldModuloP;

public interface Point {
    public IntegerFieldModuloP getField();

    public AffinePoint asAffine();

    public boolean affineEquals(Point var1);

    public ImmutablePoint fixed();

    public MutablePoint mutable();
}

