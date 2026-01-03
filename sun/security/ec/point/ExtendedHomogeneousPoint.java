/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec.point;

import sun.security.ec.point.AffinePoint;
import sun.security.ec.point.ImmutablePoint;
import sun.security.ec.point.MutablePoint;
import sun.security.ec.point.Point;
import sun.security.util.math.ImmutableIntegerModuloP;
import sun.security.util.math.IntegerFieldModuloP;
import sun.security.util.math.IntegerModuloP;
import sun.security.util.math.MutableIntegerModuloP;

public abstract class ExtendedHomogeneousPoint<T extends IntegerModuloP>
implements Point {
    protected final T x;
    protected final T y;
    protected final T t;
    protected final T z;

    protected ExtendedHomogeneousPoint(T x, T y, T t, T z) {
        this.x = x;
        this.y = y;
        this.t = t;
        this.z = z;
    }

    @Override
    public IntegerFieldModuloP getField() {
        return this.x.getField();
    }

    @Override
    public Immutable fixed() {
        return new Immutable(this.x.fixed(), this.y.fixed(), this.t.fixed(), this.z.fixed());
    }

    @Override
    public Mutable mutable() {
        return new Mutable(this.x.mutable(), this.y.mutable(), this.t.mutable(), this.z.mutable());
    }

    public T getX() {
        return this.x;
    }

    public T getY() {
        return this.y;
    }

    public T getT() {
        return this.t;
    }

    public T getZ() {
        return this.z;
    }

    @Override
    public AffinePoint asAffine() {
        ImmutableIntegerModuloP zInv = this.z.multiplicativeInverse();
        return new AffinePoint(this.x.multiply(zInv), this.y.multiply(zInv));
    }

    private static <T1 extends IntegerModuloP, T2 extends IntegerModuloP> boolean affineEquals(ExtendedHomogeneousPoint<T1> p1, ExtendedHomogeneousPoint<T2> p2) {
        MutableIntegerModuloP x1 = p1.getX().mutable().setProduct((IntegerModuloP)p2.getZ());
        MutableIntegerModuloP x2 = p2.getX().mutable().setProduct((IntegerModuloP)p1.getZ());
        if (!x1.asBigInteger().equals(x2.asBigInteger())) {
            return false;
        }
        MutableIntegerModuloP y1 = p1.getY().mutable().setProduct((IntegerModuloP)p2.getZ());
        MutableIntegerModuloP y2 = p2.getY().mutable().setProduct((IntegerModuloP)p1.getZ());
        return y1.asBigInteger().equals(y2.asBigInteger());
    }

    @Override
    public boolean affineEquals(Point p) {
        if (p instanceof ExtendedHomogeneousPoint) {
            ExtendedHomogeneousPoint ehp = (ExtendedHomogeneousPoint)p;
            return ExtendedHomogeneousPoint.affineEquals(this, ehp);
        }
        return this.asAffine().equals(p.asAffine());
    }

    public static class Immutable
    extends ExtendedHomogeneousPoint<ImmutableIntegerModuloP>
    implements ImmutablePoint {
        public Immutable(ImmutableIntegerModuloP x, ImmutableIntegerModuloP y, ImmutableIntegerModuloP t, ImmutableIntegerModuloP z) {
            super(x, y, t, z);
        }
    }

    public static class Mutable
    extends ExtendedHomogeneousPoint<MutableIntegerModuloP>
    implements MutablePoint {
        public Mutable(MutableIntegerModuloP x, MutableIntegerModuloP y, MutableIntegerModuloP t, MutableIntegerModuloP z) {
            super(x, y, t, z);
        }

        @Override
        public Mutable conditionalSet(Point p, int set) {
            if (!(p instanceof ExtendedHomogeneousPoint)) {
                throw new RuntimeException("Incompatible point");
            }
            ExtendedHomogeneousPoint ehp = (ExtendedHomogeneousPoint)p;
            return this.conditionalSet(ehp, set);
        }

        private <T extends IntegerModuloP> Mutable conditionalSet(ExtendedHomogeneousPoint<T> ehp, int set) {
            ((MutableIntegerModuloP)this.x).conditionalSet((IntegerModuloP)ehp.x, set);
            ((MutableIntegerModuloP)this.y).conditionalSet((IntegerModuloP)ehp.y, set);
            ((MutableIntegerModuloP)this.t).conditionalSet((IntegerModuloP)ehp.t, set);
            ((MutableIntegerModuloP)this.z).conditionalSet((IntegerModuloP)ehp.z, set);
            return this;
        }

        @Override
        public Mutable setValue(AffinePoint p) {
            ((MutableIntegerModuloP)this.x).setValue(p.getX());
            ((MutableIntegerModuloP)this.y).setValue(p.getY());
            ((MutableIntegerModuloP)this.t).setValue(p.getX()).setProduct(p.getY());
            ((MutableIntegerModuloP)this.z).setValue(p.getX().getField().get1());
            return this;
        }

        @Override
        public Mutable setValue(Point p) {
            ExtendedHomogeneousPoint ehp = (ExtendedHomogeneousPoint)p;
            return this.setValue(ehp);
        }

        private <T extends IntegerModuloP> Mutable setValue(ExtendedHomogeneousPoint<T> ehp) {
            ((MutableIntegerModuloP)this.x).setValue((IntegerModuloP)ehp.x);
            ((MutableIntegerModuloP)this.y).setValue((IntegerModuloP)ehp.y);
            ((MutableIntegerModuloP)this.t).setValue((IntegerModuloP)ehp.t);
            ((MutableIntegerModuloP)this.z).setValue((IntegerModuloP)ehp.z);
            return this;
        }
    }
}

