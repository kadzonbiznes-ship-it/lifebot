/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec.point;

import sun.security.ec.point.AffinePoint;
import sun.security.ec.point.Point;

public interface MutablePoint
extends Point {
    public MutablePoint setValue(AffinePoint var1);

    public MutablePoint setValue(Point var1);

    public MutablePoint conditionalSet(Point var1, int var2);
}

