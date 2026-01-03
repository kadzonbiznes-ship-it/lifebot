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

public abstract class ProjectivePoint<T extends IntegerModuloP>
implements Point {
    protected final T x;
    protected final T y;
    protected final T z;

    protected ProjectivePoint(T x, T y, T z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public IntegerFieldModuloP getField() {
        return this.x.getField();
    }

    @Override
    public Immutable fixed() {
        return new Immutable(this.x.fixed(), this.y.fixed(), this.z.fixed());
    }

    @Override
    public Mutable mutable() {
        return new Mutable(this.x.mutable(), this.y.mutable(), this.z.mutable());
    }

    public T getX() {
        return this.x;
    }

    public T getY() {
        return this.y;
    }

    public T getZ() {
        return this.z;
    }

    @Override
    public AffinePoint asAffine() {
        ImmutableIntegerModuloP zInv = this.z.multiplicativeInverse();
        return new AffinePoint(this.x.multiply(zInv), this.y.multiply(zInv));
    }

    private static <T1 extends IntegerModuloP, T2 extends IntegerModuloP> boolean affineEquals(ProjectivePoint<T1> p1, ProjectivePoint<T2> p2) {
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
        if (p instanceof ProjectivePoint) {
            ProjectivePoint pp = (ProjectivePoint)p;
            return ProjectivePoint.affineEquals(this, pp);
        }
        return this.asAffine().equals(p.asAffine());
    }

    public static class Immutable
    extends ProjectivePoint<ImmutableIntegerModuloP>
    implements ImmutablePoint {
        public Immutable(ImmutableIntegerModuloP x, ImmutableIntegerModuloP y, ImmutableIntegerModuloP z) {
            super(x, y, z);
        }
    }

    public static class Mutable
    extends ProjectivePoint<MutableIntegerModuloP>
    implements MutablePoint {
        public Mutable(MutableIntegerModuloP x, MutableIntegerModuloP y, MutableIntegerModuloP z) {
            super(x, y, z);
        }

        public Mutable(IntegerFieldModuloP field) {
            super(field.get0().mutable(), field.get0().mutable(), field.get0().mutable());
        }

        @Override
        public Mutable conditionalSet(Point p, int set) {
            if (!(p instanceof ProjectivePoint)) {
                throw new RuntimeException("Incompatible point");
            }
            ProjectivePoint pp = (ProjectivePoint)p;
            return this.conditionalSet(pp, set);
        }

        private <T extends IntegerModuloP> Mutable conditionalSet(ProjectivePoint<T> pp, int set) {
            ((MutableIntegerModuloP)this.x).conditionalSet((IntegerModuloP)pp.x, set);
            ((MutableIntegerModuloP)this.y).conditionalSet((IntegerModuloP)pp.y, set);
            ((MutableIntegerModuloP)this.z).conditionalSet((IntegerModuloP)pp.z, set);
            return this;
        }

        @Override
        public Mutable setValue(AffinePoint p) {
            ((MutableIntegerModuloP)this.x).setValue(p.getX());
            ((MutableIntegerModuloP)this.y).setValue(p.getY());
            ((MutableIntegerModuloP)this.z).setValue(p.getX().getField().get1());
            return this;
        }

        @Override
        public Mutable setValue(Point p) {
            if (!(p instanceof ProjectivePoint)) {
                throw new RuntimeException("Incompatible point");
            }
            ProjectivePoint pp = (ProjectivePoint)p;
            return this.setValue(pp);
        }

        private <T extends IntegerModuloP> Mutable setValue(ProjectivePoint<T> pp) {
            ((MutableIntegerModuloP)this.x).setValue((IntegerModuloP)pp.x);
            ((MutableIntegerModuloP)this.y).setValue((IntegerModuloP)pp.y);
            ((MutableIntegerModuloP)this.z).setValue((IntegerModuloP)pp.z);
            return this;
        }
    }
}

