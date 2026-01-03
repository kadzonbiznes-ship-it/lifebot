/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec;

import java.math.BigInteger;
import java.security.ProviderException;
import java.security.spec.ECField;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import java.util.Map;
import java.util.Optional;
import sun.security.ec.point.AffinePoint;
import sun.security.ec.point.ImmutablePoint;
import sun.security.ec.point.MutablePoint;
import sun.security.ec.point.Point;
import sun.security.ec.point.ProjectivePoint;
import sun.security.util.ArrayUtil;
import sun.security.util.CurveDB;
import sun.security.util.KnownOIDs;
import sun.security.util.math.ImmutableIntegerModuloP;
import sun.security.util.math.IntegerFieldModuloP;
import sun.security.util.math.IntegerModuloP;
import sun.security.util.math.MutableIntegerModuloP;
import sun.security.util.math.SmallValue;
import sun.security.util.math.intpoly.IntegerPolynomialP256;
import sun.security.util.math.intpoly.IntegerPolynomialP384;
import sun.security.util.math.intpoly.IntegerPolynomialP521;
import sun.security.util.math.intpoly.P256OrderField;
import sun.security.util.math.intpoly.P384OrderField;
import sun.security.util.math.intpoly.P521OrderField;

public class ECOperations {
    private static final ECOperations secp256r1Ops = new ECOperations(IntegerPolynomialP256.ONE.getElement(CurveDB.lookup(KnownOIDs.secp256r1.value()).getCurve().getB()), P256OrderField.ONE);
    static final Map<BigInteger, IntegerFieldModuloP> fields = Map.of(IntegerPolynomialP256.MODULUS, IntegerPolynomialP256.ONE, IntegerPolynomialP384.MODULUS, IntegerPolynomialP384.ONE, IntegerPolynomialP521.MODULUS, IntegerPolynomialP521.ONE);
    static final Map<BigInteger, IntegerFieldModuloP> orderFields = Map.of(P256OrderField.MODULUS, P256OrderField.ONE, P384OrderField.MODULUS, P384OrderField.ONE, P521OrderField.MODULUS, P521OrderField.ONE);
    final ImmutableIntegerModuloP b;
    final SmallValue one;
    final SmallValue two;
    final SmallValue three;
    final SmallValue four;
    final ProjectivePoint.Immutable neutral;
    private final IntegerFieldModuloP orderField;

    public static Optional<ECOperations> forParameters(ECParameterSpec params) {
        EllipticCurve curve = params.getCurve();
        ECField eCField = curve.getField();
        if (!(eCField instanceof ECFieldFp)) {
            return Optional.empty();
        }
        ECFieldFp primeField = (ECFieldFp)eCField;
        BigInteger three = BigInteger.valueOf(3L);
        if (!primeField.getP().subtract(curve.getA()).equals(three)) {
            return Optional.empty();
        }
        IntegerFieldModuloP field = fields.get(primeField.getP());
        if (field == null) {
            return Optional.empty();
        }
        IntegerFieldModuloP orderField = orderFields.get(params.getOrder());
        if (orderField == null) {
            return Optional.empty();
        }
        ImmutableIntegerModuloP b = field.getElement(curve.getB());
        ECOperations ecOps = new ECOperations(b, orderField);
        return Optional.of(ecOps);
    }

    public ECOperations(IntegerModuloP b, IntegerFieldModuloP orderField) {
        this.b = b.fixed();
        this.orderField = orderField;
        this.one = b.getField().getSmallValue(1);
        this.two = b.getField().getSmallValue(2);
        this.three = b.getField().getSmallValue(3);
        this.four = b.getField().getSmallValue(4);
        IntegerFieldModuloP field = b.getField();
        this.neutral = new ProjectivePoint.Immutable(field.get0(), field.get1(), field.get0());
    }

    public IntegerFieldModuloP getField() {
        return this.b.getField();
    }

    public IntegerFieldModuloP getOrderField() {
        return this.orderField;
    }

    protected ProjectivePoint.Immutable getNeutral() {
        return this.neutral;
    }

    public boolean isNeutral(Point p) {
        ProjectivePoint pp = (ProjectivePoint)p;
        Object z = pp.getZ();
        IntegerFieldModuloP field = z.getField();
        int byteLength = (field.getSize().bitLength() + 7) / 8;
        byte[] zBytes = z.asByteArray(byteLength);
        return ECOperations.allZero(zBytes);
    }

    byte[] seedToScalar(byte[] seedBytes) throws IntermediateValueException {
        int seedBits = this.orderField.getSize().bitLength() + 64;
        if (seedBytes.length * 8 < seedBits) {
            throw new ProviderException("Incorrect seed length: " + seedBytes.length * 8 + " < " + seedBits);
        }
        int lastByteBits = seedBits % 8;
        if (lastByteBits != 0) {
            int lastByteIndex = seedBits / 8;
            byte mask = (byte)(255 >>> 8 - lastByteBits);
            int n = lastByteIndex;
            seedBytes[n] = (byte)(seedBytes[n] & mask);
        }
        int seedLength = (seedBits + 7) / 8;
        ImmutableIntegerModuloP scalarElem = this.orderField.getElement(seedBytes, 0, seedLength, (byte)0);
        int scalarLength = (this.orderField.getSize().bitLength() + 7) / 8;
        byte[] scalarArr = new byte[scalarLength];
        scalarElem.asByteArray(scalarArr);
        if (ECOperations.allZero(scalarArr)) {
            throw new IntermediateValueException();
        }
        return scalarArr;
    }

    public static boolean allZero(byte[] arr) {
        byte acc = 0;
        for (int i = 0; i < arr.length; ++i) {
            acc = (byte)(acc | arr[i]);
        }
        return acc == 0;
    }

    public MutablePoint multiply(AffinePoint affineP, byte[] s) {
        return PointMultiplier.of(this, affineP).pointMultiply(s);
    }

    public MutablePoint multiply(ECPoint ecPoint, byte[] s) {
        return PointMultiplier.of(this, ecPoint).pointMultiply(s);
    }

    private void setDouble(ProjectivePoint.Mutable p, MutableIntegerModuloP t0, MutableIntegerModuloP t1, MutableIntegerModuloP t2, MutableIntegerModuloP t3, MutableIntegerModuloP t4) {
        t0.setValue((IntegerModuloP)p.getX()).setSquare();
        t1.setValue((IntegerModuloP)p.getY()).setSquare();
        t2.setValue((IntegerModuloP)p.getZ()).setSquare();
        t3.setValue((IntegerModuloP)p.getX()).setProduct((IntegerModuloP)p.getY());
        t4.setValue((IntegerModuloP)p.getY()).setProduct((IntegerModuloP)p.getZ());
        t3.setSum(t3);
        ((MutableIntegerModuloP)p.getZ()).setProduct((IntegerModuloP)p.getX());
        ((MutableIntegerModuloP)p.getZ()).setProduct(this.two);
        ((MutableIntegerModuloP)p.getY()).setValue(t2).setProduct(this.b);
        ((MutableIntegerModuloP)p.getY()).setDifference((IntegerModuloP)p.getZ());
        ((MutableIntegerModuloP)p.getY()).setProduct(this.three);
        ((MutableIntegerModuloP)p.getX()).setValue(t1).setDifference((IntegerModuloP)p.getY());
        ((MutableIntegerModuloP)p.getY()).setSum(t1);
        ((MutableIntegerModuloP)p.getY()).setProduct((IntegerModuloP)p.getX());
        ((MutableIntegerModuloP)p.getX()).setProduct(t3);
        t2.setProduct(this.three);
        ((MutableIntegerModuloP)p.getZ()).setProduct(this.b);
        ((MutableIntegerModuloP)p.getZ()).setDifference(t2);
        ((MutableIntegerModuloP)p.getZ()).setDifference(t0);
        ((MutableIntegerModuloP)p.getZ()).setProduct(this.three);
        t0.setProduct(this.three);
        t0.setDifference(t2);
        t0.setProduct((IntegerModuloP)p.getZ());
        ((MutableIntegerModuloP)p.getY()).setSum(t0);
        t4.setSum(t4);
        ((MutableIntegerModuloP)p.getZ()).setProduct(t4);
        ((MutableIntegerModuloP)p.getX()).setDifference((IntegerModuloP)p.getZ());
        ((MutableIntegerModuloP)p.getZ()).setValue(t4).setProduct(t1);
        ((MutableIntegerModuloP)p.getZ()).setProduct(this.four);
    }

    public void setSum(MutablePoint p, AffinePoint p2) {
        ImmutableIntegerModuloP zero = p.getField().get0();
        MutableIntegerModuloP t0 = zero.mutable();
        MutableIntegerModuloP t1 = zero.mutable();
        MutableIntegerModuloP t2 = zero.mutable();
        MutableIntegerModuloP t3 = zero.mutable();
        MutableIntegerModuloP t4 = zero.mutable();
        this.setSum((ProjectivePoint.Mutable)p, p2, t0, t1, t2, t3, t4);
    }

    private void setSum(ProjectivePoint.Mutable p, AffinePoint p2, MutableIntegerModuloP t0, MutableIntegerModuloP t1, MutableIntegerModuloP t2, MutableIntegerModuloP t3, MutableIntegerModuloP t4) {
        t0.setValue((IntegerModuloP)p.getX()).setProduct(p2.getX());
        t1.setValue((IntegerModuloP)p.getY()).setProduct(p2.getY());
        t3.setValue(p2.getX()).setSum(p2.getY());
        t4.setValue((IntegerModuloP)p.getX()).setSum((IntegerModuloP)p.getY());
        t3.setProduct(t4);
        t4.setValue(t0).setSum(t1);
        t3.setDifference(t4);
        t4.setValue(p2.getY()).setProduct((IntegerModuloP)p.getZ());
        t4.setSum((IntegerModuloP)p.getY());
        ((MutableIntegerModuloP)p.getY()).setValue(p2.getX()).setProduct((IntegerModuloP)p.getZ());
        ((MutableIntegerModuloP)p.getY()).setSum((IntegerModuloP)p.getX());
        t2.setValue((IntegerModuloP)p.getZ());
        ((MutableIntegerModuloP)p.getZ()).setProduct(this.b);
        ((MutableIntegerModuloP)p.getX()).setValue((IntegerModuloP)p.getY()).setDifference((IntegerModuloP)p.getZ());
        ((MutableIntegerModuloP)p.getX()).setProduct(this.three);
        ((MutableIntegerModuloP)p.getZ()).setValue(t1).setDifference((IntegerModuloP)p.getX());
        ((MutableIntegerModuloP)p.getX()).setSum(t1);
        ((MutableIntegerModuloP)p.getY()).setProduct(this.b);
        t2.setProduct(this.three);
        ((MutableIntegerModuloP)p.getY()).setDifference(t2);
        ((MutableIntegerModuloP)p.getY()).setDifference(t0);
        ((MutableIntegerModuloP)p.getY()).setProduct(this.three);
        t0.setProduct(this.three);
        t0.setDifference(t2);
        t1.setValue(t4).setProduct((IntegerModuloP)p.getY());
        t2.setValue(t0).setProduct((IntegerModuloP)p.getY());
        ((MutableIntegerModuloP)p.getY()).setValue((IntegerModuloP)p.getX()).setProduct((IntegerModuloP)p.getZ());
        ((MutableIntegerModuloP)p.getY()).setSum(t2);
        ((MutableIntegerModuloP)p.getX()).setProduct(t3);
        ((MutableIntegerModuloP)p.getX()).setDifference(t1);
        ((MutableIntegerModuloP)p.getZ()).setProduct(t4);
        t3.setProduct(t0);
        ((MutableIntegerModuloP)p.getZ()).setSum(t3);
    }

    private void setSum(ProjectivePoint.Mutable p, ProjectivePoint.Mutable p2, MutableIntegerModuloP t0, MutableIntegerModuloP t1, MutableIntegerModuloP t2, MutableIntegerModuloP t3, MutableIntegerModuloP t4) {
        t0.setValue((IntegerModuloP)p.getX()).setProduct((IntegerModuloP)p2.getX());
        t1.setValue((IntegerModuloP)p.getY()).setProduct((IntegerModuloP)p2.getY());
        t2.setValue((IntegerModuloP)p.getZ()).setProduct((IntegerModuloP)p2.getZ());
        t3.setValue((IntegerModuloP)p.getX()).setSum((IntegerModuloP)p.getY());
        t4.setValue((IntegerModuloP)p2.getX()).setSum((IntegerModuloP)p2.getY());
        t3.setProduct(t4);
        t4.setValue(t0).setSum(t1);
        t3.setDifference(t4);
        t4.setValue((IntegerModuloP)p.getY()).setSum((IntegerModuloP)p.getZ());
        ((MutableIntegerModuloP)p.getY()).setValue((IntegerModuloP)p2.getY()).setSum((IntegerModuloP)p2.getZ());
        t4.setProduct((IntegerModuloP)p.getY());
        ((MutableIntegerModuloP)p.getY()).setValue(t1).setSum(t2);
        t4.setDifference((IntegerModuloP)p.getY());
        ((MutableIntegerModuloP)p.getX()).setSum((IntegerModuloP)p.getZ());
        ((MutableIntegerModuloP)p.getY()).setValue((IntegerModuloP)p2.getX()).setSum((IntegerModuloP)p2.getZ());
        ((MutableIntegerModuloP)p.getX()).setProduct((IntegerModuloP)p.getY());
        ((MutableIntegerModuloP)p.getY()).setValue(t0).setSum(t2);
        ((MutableIntegerModuloP)p.getY()).setAdditiveInverse().setSum((IntegerModuloP)p.getX());
        ((MutableIntegerModuloP)p.getZ()).setValue(t2).setProduct(this.b);
        ((MutableIntegerModuloP)p.getX()).setValue((IntegerModuloP)p.getY()).setDifference((IntegerModuloP)p.getZ());
        ((MutableIntegerModuloP)p.getX()).setProduct(this.three);
        ((MutableIntegerModuloP)p.getZ()).setValue(t1).setDifference((IntegerModuloP)p.getX());
        ((MutableIntegerModuloP)p.getX()).setSum(t1);
        ((MutableIntegerModuloP)p.getY()).setProduct(this.b);
        t2.setProduct(this.three);
        ((MutableIntegerModuloP)p.getY()).setDifference(t2);
        ((MutableIntegerModuloP)p.getY()).setDifference(t0);
        ((MutableIntegerModuloP)p.getY()).setProduct(this.three);
        t0.setProduct(this.three);
        t0.setDifference(t2);
        t1.setValue(t4).setProduct((IntegerModuloP)p.getY());
        t2.setValue(t0).setProduct((IntegerModuloP)p.getY());
        ((MutableIntegerModuloP)p.getY()).setValue((IntegerModuloP)p.getX()).setProduct((IntegerModuloP)p.getZ());
        ((MutableIntegerModuloP)p.getY()).setSum(t2);
        ((MutableIntegerModuloP)p.getX()).setProduct(t3);
        ((MutableIntegerModuloP)p.getX()).setDifference(t1);
        ((MutableIntegerModuloP)p.getZ()).setProduct(t4);
        t3.setProduct(t0);
        ((MutableIntegerModuloP)p.getZ()).setSum(t3);
    }

    public boolean checkOrder(ECPoint point) {
        BigInteger x = point.getAffineX();
        BigInteger y = point.getAffineY();
        IntegerFieldModuloP field = this.getField();
        AffinePoint ap = new AffinePoint(field.getElement(x), field.getElement(y));
        byte[] scalar = this.orderField.getSize().toByteArray();
        ArrayUtil.reverse(scalar);
        return this.isNeutral(this.multiply(ap, scalar));
    }

    static class IntermediateValueException
    extends Exception {
        private static final long serialVersionUID = 1L;

        IntermediateValueException() {
        }
    }

    static sealed interface PointMultiplier {
        public static final Map<ECPoint, PointMultiplier> multipliers = Map.of(Secp256R1GeneratorMultiplier.generator, Secp256R1GeneratorMultiplier.multiplier);

        public ProjectivePoint.Mutable pointMultiply(byte[] var1);

        public static PointMultiplier of(ECOperations ecOps, AffinePoint affPoint) {
            PointMultiplier multiplier = multipliers.get(affPoint.toECPoint());
            if (multiplier == null) {
                multiplier = new Default(ecOps, affPoint);
            }
            return multiplier;
        }

        public static PointMultiplier of(ECOperations ecOps, ECPoint ecPoint) {
            PointMultiplier multiplier = multipliers.get(ecPoint);
            if (multiplier == null) {
                AffinePoint affPoint = AffinePoint.fromECPoint(ecPoint, ecOps.getField());
                multiplier = new Default(ecOps, affPoint);
            }
            return multiplier;
        }

        private static void lookup(ProjectivePoint.Immutable[] ips, int index, ProjectivePoint.Mutable result) {
            for (int i = 0; i < 16; ++i) {
                int xor = index ^ i;
                int bit3 = (xor & 8) >>> 3;
                int bit2 = (xor & 4) >>> 2;
                int bit1 = (xor & 2) >>> 1;
                int bit0 = xor & 1;
                int inverse = bit0 | bit1 | bit2 | bit3;
                int set = 1 - inverse;
                ProjectivePoint.Immutable pi = ips[i];
                result.conditionalSet((Point)pi, set);
            }
        }

        public static final class Default
        implements PointMultiplier {
            private final AffinePoint affineP;
            private final ECOperations ecOps;

            private Default(ECOperations ecOps, AffinePoint affineP) {
                this.ecOps = ecOps;
                this.affineP = affineP;
            }

            @Override
            public ProjectivePoint.Mutable pointMultiply(byte[] s) {
                IntegerFieldModuloP field = this.affineP.getX().getField();
                ImmutableIntegerModuloP zero = field.get0();
                MutableIntegerModuloP t0 = zero.mutable();
                MutableIntegerModuloP t1 = zero.mutable();
                MutableIntegerModuloP t2 = zero.mutable();
                MutableIntegerModuloP t3 = zero.mutable();
                MutableIntegerModuloP t4 = zero.mutable();
                ProjectivePoint.Mutable result = new ProjectivePoint.Mutable(field);
                ((MutableIntegerModuloP)result.getY()).setValue(field.get1().mutable());
                ProjectivePoint.Immutable[] pointMultiples = new ProjectivePoint.Immutable[16];
                pointMultiples[0] = result.fixed();
                ProjectivePoint.Mutable ps = new ProjectivePoint.Mutable(field);
                ps.setValue(this.affineP);
                pointMultiples[1] = ps.fixed();
                for (int i = 2; i < 16; ++i) {
                    this.ecOps.setSum(ps, this.affineP, t0, t1, t2, t3, t4);
                    pointMultiples[i] = ps.fixed();
                }
                MutablePoint lookupResult = ps.mutable();
                for (int i = s.length - 1; i >= 0; --i) {
                    this.double4(result, t0, t1, t2, t3, t4);
                    int high = (0xFF & s[i]) >>> 4;
                    PointMultiplier.lookup(pointMultiples, high, (ProjectivePoint.Mutable)lookupResult);
                    this.ecOps.setSum(result, (ProjectivePoint.Mutable)lookupResult, t0, t1, t2, t3, t4);
                    this.double4(result, t0, t1, t2, t3, t4);
                    int low = 0xF & s[i];
                    PointMultiplier.lookup(pointMultiples, low, (ProjectivePoint.Mutable)lookupResult);
                    this.ecOps.setSum(result, (ProjectivePoint.Mutable)lookupResult, t0, t1, t2, t3, t4);
                }
                return result;
            }

            private void double4(ProjectivePoint.Mutable p, MutableIntegerModuloP t0, MutableIntegerModuloP t1, MutableIntegerModuloP t2, MutableIntegerModuloP t3, MutableIntegerModuloP t4) {
                for (int i = 0; i < 4; ++i) {
                    this.ecOps.setDouble(p, t0, t1, t2, t3, t4);
                }
            }
        }

        public static final class Secp256R1GeneratorMultiplier
        implements PointMultiplier {
            private static final ECPoint generator = CurveDB.P_256.getGenerator();
            private static final PointMultiplier multiplier = new Secp256R1GeneratorMultiplier();
            private static final ImmutableIntegerModuloP zero = IntegerPolynomialP256.ONE.get0();
            private static final ImmutableIntegerModuloP one = IntegerPolynomialP256.ONE.get1();

            @Override
            public ProjectivePoint.Mutable pointMultiply(byte[] s) {
                MutableIntegerModuloP t0 = zero.mutable();
                MutableIntegerModuloP t1 = zero.mutable();
                MutableIntegerModuloP t2 = zero.mutable();
                MutableIntegerModuloP t3 = zero.mutable();
                MutableIntegerModuloP t4 = zero.mutable();
                ProjectivePoint.Mutable d = new ProjectivePoint.Mutable(zero.mutable(), one.mutable(), zero.mutable());
                MutablePoint r = d.mutable();
                for (int i = 15; i >= 0; --i) {
                    secp256r1Ops.setDouble(d, t0, t1, t2, t3, t4);
                    for (int j = 3; j >= 0; --j) {
                        int pos = i + j * 16;
                        int index = Secp256R1GeneratorMultiplier.bit(s, pos + 192) << 3 | Secp256R1GeneratorMultiplier.bit(s, pos + 128) << 2 | Secp256R1GeneratorMultiplier.bit(s, pos + 64) << 1 | Secp256R1GeneratorMultiplier.bit(s, pos);
                        PointMultiplier.lookup(P256.points[j], index, (ProjectivePoint.Mutable)r);
                        secp256r1Ops.setSum(d, (ProjectivePoint.Mutable)r, t0, t1, t2, t3, t4);
                    }
                }
                return d;
            }

            private static int bit(byte[] k, int i) {
                return k[i >> 3] >> (i & 7) & 1;
            }

            private static final class P256 {
                private static final ProjectivePoint.Immutable[][] points = new ProjectivePoint.Immutable[4][16];

                private P256() {
                }

                private static void verifyTables(BigInteger[] base) {
                    for (int d = 0; d < 4; ++d) {
                        for (int w = 0; w < 16; ++w) {
                            BigInteger bi = base[w];
                            if (d != 0) {
                                bi = bi.multiply(BigInteger.TWO.pow(d * 16));
                            }
                            if (w == 0) continue;
                            byte[] s = new byte[32];
                            byte[] b = bi.toByteArray();
                            ArrayUtil.reverse(b);
                            System.arraycopy(b, 0, s, 0, b.length);
                            ProjectivePoint.Mutable m = multiplier.pointMultiply(s);
                            ImmutablePoint v = m.setValue(m.asAffine()).fixed();
                            if (((ImmutableIntegerModuloP)((ProjectivePoint)((Object)v)).getX()).asBigInteger().equals(((ImmutableIntegerModuloP)points[d][w].getX()).asBigInteger()) && ((ImmutableIntegerModuloP)((ProjectivePoint)((Object)v)).getY()).asBigInteger().equals(((ImmutableIntegerModuloP)points[d][w].getY()).asBigInteger())) continue;
                            throw new RuntimeException();
                        }
                    }
                }

                static {
                    BigInteger[] factors = new BigInteger[]{BigInteger.ONE, BigInteger.TWO.pow(64), BigInteger.TWO.pow(128), BigInteger.TWO.pow(192)};
                    BigInteger[] base = new BigInteger[16];
                    base[0] = BigInteger.ZERO;
                    base[1] = BigInteger.ONE;
                    base[2] = factors[1];
                    for (int i = 3; i < 16; ++i) {
                        base[i] = BigInteger.ZERO;
                        for (int k = 0; k < 4; ++k) {
                            if ((i >>> k & 1) == 0) continue;
                            base[i] = base[i].add(factors[k]);
                        }
                    }
                    for (int d = 0; d < 4; ++d) {
                        for (int w = 0; w < 16; ++w) {
                            BigInteger bi = base[w];
                            if (d != 0) {
                                bi = bi.multiply(BigInteger.TWO.pow(d * 16));
                            }
                            if (w == 0) {
                                P256.points[d][0] = new ProjectivePoint.Immutable(zero.fixed(), one.fixed(), zero.fixed());
                                continue;
                            }
                            Default multiplier = new Default(secp256r1Ops, AffinePoint.fromECPoint(generator, zero.getField()));
                            byte[] s = bi.toByteArray();
                            ArrayUtil.reverse(s);
                            ProjectivePoint.Mutable m = multiplier.pointMultiply(s);
                            P256.points[d][w] = m.setValue(m.asAffine()).fixed();
                        }
                    }
                    if (ECOperations.class.desiredAssertionStatus()) {
                        P256.verifyTables(base);
                    }
                }
            }
        }
    }
}

