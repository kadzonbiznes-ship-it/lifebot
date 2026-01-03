/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec.ed;

import java.math.BigInteger;
import java.util.function.Function;
import sun.security.ec.ed.EdECOperations;
import sun.security.ec.point.AffinePoint;
import sun.security.ec.point.MutablePoint;
import sun.security.ec.point.Point;
import sun.security.ec.point.ProjectivePoint;
import sun.security.util.math.ImmutableIntegerModuloP;
import sun.security.util.math.IntegerFieldModuloP;
import sun.security.util.math.IntegerModuloP;
import sun.security.util.math.MutableIntegerModuloP;
import sun.security.util.math.SmallValue;

public class Ed448Operations
extends EdECOperations {
    private final SmallValue two;
    private final ImmutableIntegerModuloP d;
    private final ProjectivePoint.Immutable basePoint;
    private static final BigInteger TWO = BigInteger.valueOf(2L);
    private static final BigInteger THREE = BigInteger.valueOf(3L);
    private static final BigInteger FIVE = BigInteger.valueOf(5L);
    private final BigInteger sizeMinus3;

    public Ed448Operations(ImmutableIntegerModuloP d, BigInteger baseX, BigInteger baseY) {
        this.two = d.getField().getSmallValue(2);
        this.d = d;
        this.basePoint = this.of(new AffinePoint(d.getField().getElement(baseX), d.getField().getElement(baseY)));
        this.sizeMinus3 = d.getField().getSize().subtract(THREE);
    }

    @Override
    public Point basePointMultiply(byte[] scalar) {
        return this.setProduct(this.basePoint.mutable(), scalar);
    }

    @Override
    protected ProjectivePoint.Immutable getNeutral() {
        IntegerFieldModuloP field = this.d.getField();
        return new ProjectivePoint.Immutable(field.get0(), field.get1(), field.get1());
    }

    @Override
    protected MutablePoint setSum(MutablePoint p1, MutablePoint p2, MutableIntegerModuloP t1, MutableIntegerModuloP t2, MutableIntegerModuloP t3) {
        ProjectivePoint.Mutable ehp1 = (ProjectivePoint.Mutable)p1;
        ProjectivePoint.Mutable ehp2 = (ProjectivePoint.Mutable)p2;
        return this.setSum(ehp1, ehp2, t1, t2, t3);
    }

    @Override
    protected MutablePoint setDouble(MutablePoint p, MutableIntegerModuloP t1, MutableIntegerModuloP t2) {
        ProjectivePoint.Mutable ehp = (ProjectivePoint.Mutable)p;
        return this.setDouble(ehp, t1, t2);
    }

    @Override
    public ProjectivePoint.Immutable of(AffinePoint p) {
        return new ProjectivePoint.Immutable(p.getX(), p.getY(), p.getX().getField().get1());
    }

    @Override
    public <T extends Throwable> AffinePoint decodeAffinePoint(Function<String, T> exception, int xLSB, IntegerModuloP y) throws T {
        ImmutableIntegerModuloP y2 = y.square();
        ImmutableIntegerModuloP u = y2.subtract(this.d.getField().get1());
        MutableIntegerModuloP v = this.d.mutable().setProduct(y2).setDifference(this.d.getField().get1());
        ImmutableIntegerModuloP u5v3pow = u.pow(FIVE).multiply(v.pow(THREE)).pow(this.sizeMinus3.shiftRight(2));
        MutableIntegerModuloP x = v.mutable().setProduct(u.pow(THREE)).setProduct(u5v3pow);
        v.setProduct(x).setProduct(x);
        if (!v.asBigInteger().equals(u.asBigInteger())) {
            throw (Throwable)exception.apply("Invalid point");
        }
        if (x.asBigInteger().equals(BigInteger.ZERO) && xLSB == 1) {
            throw (Throwable)exception.apply("Invalid point");
        }
        if (xLSB != (x.asBigInteger().intValue() & 1)) {
            x.setAdditiveInverse();
        }
        return new AffinePoint(x.fixed(), y.fixed());
    }

    ProjectivePoint.Mutable setSum(ProjectivePoint.Mutable p1, ProjectivePoint.Mutable p2, MutableIntegerModuloP t1, MutableIntegerModuloP t2, MutableIntegerModuloP t3) {
        t1.setValue((IntegerModuloP)p1.getX()).setProduct((IntegerModuloP)p2.getX());
        t2.setValue((IntegerModuloP)p2.getX()).setSum((IntegerModuloP)p2.getY());
        ((MutableIntegerModuloP)p1.getX()).setSum((IntegerModuloP)p1.getY()).setProduct(t2);
        ((MutableIntegerModuloP)p1.getZ()).setProduct((IntegerModuloP)p2.getZ());
        ((MutableIntegerModuloP)p1.getY()).setProduct((IntegerModuloP)p2.getY());
        t3.setValue(this.d).setProduct(t1).setProduct((IntegerModuloP)p1.getY());
        ((MutableIntegerModuloP)p1.getX()).setDifference(t1).setDifference((IntegerModuloP)p1.getY());
        ((MutableIntegerModuloP)p1.getY()).setDifference(t1);
        t1.setValue((IntegerModuloP)p1.getZ()).setSquare();
        t2.setValue(t1).setDifference(t3);
        t1.setSum(t3);
        ((MutableIntegerModuloP)p1.getX()).setProduct(t2).setProduct((IntegerModuloP)p1.getZ());
        ((MutableIntegerModuloP)p1.getY()).setProduct(t1).setProduct((IntegerModuloP)p1.getZ());
        ((MutableIntegerModuloP)p1.getZ()).setValue(t2.multiply(t1));
        return p1;
    }

    protected ProjectivePoint.Mutable setDouble(ProjectivePoint.Mutable p, MutableIntegerModuloP t1, MutableIntegerModuloP t2) {
        t2.setValue((IntegerModuloP)p.getX()).setSquare();
        ((MutableIntegerModuloP)p.getX()).setSum((IntegerModuloP)p.getY()).setSquare();
        ((MutableIntegerModuloP)p.getY()).setSquare();
        ((MutableIntegerModuloP)p.getZ()).setSquare();
        t1.setValue(t2).setSum((IntegerModuloP)p.getY());
        t2.setDifference((IntegerModuloP)p.getY());
        ((MutableIntegerModuloP)p.getY()).setValue(t1).setProduct(t2);
        ((MutableIntegerModuloP)p.getZ()).setProduct(this.two);
        ((MutableIntegerModuloP)p.getZ()).setAdditiveInverse().setSum(t1);
        ((MutableIntegerModuloP)p.getX()).setDifference(t1).setProduct((IntegerModuloP)p.getZ());
        ((MutableIntegerModuloP)p.getZ()).setProduct(t1);
        return p;
    }
}

