/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec.ed;

import java.math.BigInteger;
import java.util.function.Function;
import sun.security.ec.ed.EdECOperations;
import sun.security.ec.point.AffinePoint;
import sun.security.ec.point.ExtendedHomogeneousPoint;
import sun.security.ec.point.MutablePoint;
import sun.security.ec.point.Point;
import sun.security.util.math.ImmutableIntegerModuloP;
import sun.security.util.math.IntegerFieldModuloP;
import sun.security.util.math.IntegerModuloP;
import sun.security.util.math.MutableIntegerModuloP;
import sun.security.util.math.SmallValue;

public class Ed25519Operations
extends EdECOperations {
    private final SmallValue two;
    private final ImmutableIntegerModuloP d;
    private final ExtendedHomogeneousPoint.Immutable basePoint;
    private static final BigInteger TWO = BigInteger.valueOf(2L);
    private static final BigInteger SEVEN = BigInteger.valueOf(7L);
    private final BigInteger sizeMinus5;

    public Ed25519Operations(ImmutableIntegerModuloP d, BigInteger baseX, BigInteger baseY) {
        this.two = d.getField().getSmallValue(2);
        this.d = d;
        this.basePoint = this.of(new AffinePoint(d.getField().getElement(baseX), d.getField().getElement(baseY)));
        this.sizeMinus5 = d.getField().getSize().subtract(BigInteger.valueOf(5L));
    }

    @Override
    public Point basePointMultiply(byte[] scalar) {
        return this.setProduct(this.basePoint.mutable(), scalar);
    }

    @Override
    protected ExtendedHomogeneousPoint.Immutable getNeutral() {
        IntegerFieldModuloP field = this.d.getField();
        return new ExtendedHomogeneousPoint.Immutable(field.get0(), field.get1(), field.get0(), field.get1());
    }

    @Override
    protected MutablePoint setSum(MutablePoint p1, MutablePoint p2, MutableIntegerModuloP t1, MutableIntegerModuloP t2, MutableIntegerModuloP t3) {
        ExtendedHomogeneousPoint.Mutable ehp1 = (ExtendedHomogeneousPoint.Mutable)p1;
        ExtendedHomogeneousPoint.Mutable ehp2 = (ExtendedHomogeneousPoint.Mutable)p2;
        return this.setSum(ehp1, ehp2, t1, t2, t3);
    }

    @Override
    protected MutablePoint setDouble(MutablePoint p, MutableIntegerModuloP t1, MutableIntegerModuloP t2) {
        ExtendedHomogeneousPoint.Mutable ehp = (ExtendedHomogeneousPoint.Mutable)p;
        return this.setDouble(ehp, t1, t2);
    }

    @Override
    public ExtendedHomogeneousPoint.Immutable of(AffinePoint p) {
        return new ExtendedHomogeneousPoint.Immutable(p.getX(), p.getY(), p.getX().multiply(p.getY()), p.getX().getField().get1());
    }

    @Override
    public <T extends Throwable> AffinePoint decodeAffinePoint(Function<String, T> exception, int xLSB, IntegerModuloP y) throws T {
        IntegerFieldModuloP field = this.d.getField();
        BigInteger p = field.getSize();
        ImmutableIntegerModuloP y2 = y.square();
        ImmutableIntegerModuloP u = y2.subtract(field.get1());
        MutableIntegerModuloP v = this.d.mutable().setProduct(y2).setSum(field.get1());
        MutableIntegerModuloP x = u.mutable().setProduct(v.pow(BigInteger.valueOf(3L)));
        ImmutableIntegerModuloP uv7pow = u.multiply(v.pow(SEVEN)).pow(this.sizeMinus5.shiftRight(3));
        x.setProduct(uv7pow);
        v.setProduct(x).setProduct(x);
        BigInteger bigVX2 = v.asBigInteger();
        if (!bigVX2.equals(u.asBigInteger())) {
            if (bigVX2.equals(u.additiveInverse().asBigInteger())) {
                BigInteger exp = p.subtract(BigInteger.ONE).shiftRight(2);
                ImmutableIntegerModuloP twoPow = field.getElement(TWO.modPow(exp, p));
                x.setProduct(twoPow);
            } else {
                throw (Throwable)exception.apply("Invalid point");
            }
        }
        if (x.asBigInteger().equals(BigInteger.ZERO) && xLSB == 1) {
            throw (Throwable)exception.apply("Invalid point");
        }
        if (xLSB != (x.asBigInteger().intValue() & 1)) {
            x.setAdditiveInverse();
        }
        return new AffinePoint(x.fixed(), y.fixed());
    }

    ExtendedHomogeneousPoint.Mutable setSum(ExtendedHomogeneousPoint.Mutable p1, ExtendedHomogeneousPoint.Mutable p2, MutableIntegerModuloP t1, MutableIntegerModuloP t2, MutableIntegerModuloP t3) {
        t1.setValue((IntegerModuloP)p2.getY()).setDifference((IntegerModuloP)p2.getX());
        t2.setValue((IntegerModuloP)p1.getY()).setDifference((IntegerModuloP)p1.getX()).setProduct(t1);
        t1.setValue((IntegerModuloP)p2.getY()).setSum((IntegerModuloP)p2.getX());
        t3.setValue((IntegerModuloP)p1.getY()).setSum((IntegerModuloP)p1.getX()).setProduct(t1);
        ((MutableIntegerModuloP)p1.getX()).setValue(t3).setDifference(t2);
        t3.setSum(t2);
        t2.setValue(this.d).setSum(this.d).setProduct((IntegerModuloP)p1.getT()).setProduct((IntegerModuloP)p2.getT());
        t1.setValue((IntegerModuloP)p1.getZ()).setProduct((IntegerModuloP)p2.getZ()).setProduct(this.two);
        ((MutableIntegerModuloP)p1.getY()).setValue(t1).setSum(t2);
        ((MutableIntegerModuloP)p1.getZ()).setValue(t1).setDifference(t2);
        ((MutableIntegerModuloP)p1.getT()).setValue((IntegerModuloP)p1.getX()).setProduct(t3);
        ((MutableIntegerModuloP)p1.getX()).setProduct((IntegerModuloP)p1.getZ());
        ((MutableIntegerModuloP)p1.getZ()).setProduct((IntegerModuloP)p1.getY());
        ((MutableIntegerModuloP)p1.getY()).setProduct(t3);
        return p1;
    }

    protected ExtendedHomogeneousPoint.Mutable setDouble(ExtendedHomogeneousPoint.Mutable p, MutableIntegerModuloP t1, MutableIntegerModuloP t2) {
        t1.setValue((IntegerModuloP)p.getX()).setSum((IntegerModuloP)p.getY()).setSquare();
        ((MutableIntegerModuloP)p.getX()).setSquare();
        ((MutableIntegerModuloP)p.getY()).setSquare();
        t2.setValue((IntegerModuloP)p.getX()).setSum((IntegerModuloP)p.getY());
        ((MutableIntegerModuloP)p.getZ()).setSquare().setProduct(this.two);
        ((MutableIntegerModuloP)p.getT()).setValue(t2).setDifference(t1);
        t1.setValue((IntegerModuloP)p.getX()).setDifference((IntegerModuloP)p.getY());
        ((MutableIntegerModuloP)p.getZ()).setSum(t1);
        ((MutableIntegerModuloP)p.getX()).setValue((IntegerModuloP)p.getT()).setProduct((IntegerModuloP)p.getZ());
        ((MutableIntegerModuloP)p.getY()).setValue(t1).setProduct(t2);
        ((MutableIntegerModuloP)p.getT()).setProduct(t2);
        ((MutableIntegerModuloP)p.getZ()).setProduct(t1);
        return p;
    }
}

