/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util.math;

import java.math.BigInteger;
import sun.security.util.math.ImmutableIntegerModuloP;
import sun.security.util.math.IntegerFieldModuloP;
import sun.security.util.math.MutableIntegerModuloP;
import sun.security.util.math.intpoly.IntegerPolynomialP256;
import sun.security.util.math.intpoly.P256OrderField;

public interface IntegerModuloP {
    public IntegerFieldModuloP getField();

    public BigInteger asBigInteger();

    public ImmutableIntegerModuloP fixed();

    public MutableIntegerModuloP mutable();

    public ImmutableIntegerModuloP add(IntegerModuloP var1);

    public ImmutableIntegerModuloP additiveInverse();

    public ImmutableIntegerModuloP multiply(IntegerModuloP var1);

    default public byte[] addModPowerTwo(IntegerModuloP b, int len) {
        byte[] result = new byte[len];
        this.addModPowerTwo(b, result);
        return result;
    }

    public void addModPowerTwo(IntegerModuloP var1, byte[] var2);

    default public byte[] asByteArray(int len) {
        byte[] result = new byte[len];
        this.asByteArray(result);
        return result;
    }

    public void asByteArray(byte[] var1);

    public long[] getLimbs();

    default public ImmutableIntegerModuloP multiplicativeInverse() {
        return MultiplicativeInverser.of(this.getField().getSize()).inverse(this);
    }

    default public ImmutableIntegerModuloP subtract(IntegerModuloP b) {
        return this.add(b.additiveInverse());
    }

    default public ImmutableIntegerModuloP square() {
        return this.multiply(this);
    }

    default public ImmutableIntegerModuloP pow(BigInteger b) {
        MutableIntegerModuloP y = this.getField().get1().mutable();
        MutableIntegerModuloP x = this.mutable();
        int bitLength = b.bitLength();
        for (int bit = 0; bit < bitLength; ++bit) {
            if (b.testBit(bit)) {
                y.setProduct(x);
            }
            x.setSquare();
        }
        return y.fixed();
    }

    public static sealed interface MultiplicativeInverser {
        public static MultiplicativeInverser of(BigInteger m) {
            if (m.equals(IntegerPolynomialP256.MODULUS)) {
                return Secp256R1.instance;
            }
            if (m.equals(P256OrderField.MODULUS)) {
                return Secp256R1Field.instance;
            }
            return new Default(m);
        }

        public ImmutableIntegerModuloP inverse(IntegerModuloP var1);

        public static final class Secp256R1
        implements MultiplicativeInverser {
            private static final Secp256R1 instance = new Secp256R1();

            @Override
            public ImmutableIntegerModuloP inverse(IntegerModuloP imp) {
                MutableIntegerModuloP t = imp.mutable();
                MutableIntegerModuloP v = null;
                MutableIntegerModuloP w = null;
                block11: for (int i = 0; i < 31; ++i) {
                    t.setSquare();
                    switch (i) {
                        case 0: {
                            t.setProduct(imp);
                            v = t.mutable();
                            continue block11;
                        }
                        case 4: {
                            t.setProduct(v);
                            w = t.mutable();
                            continue block11;
                        }
                        case 12: 
                        case 28: {
                            t.setProduct(w);
                            w = t.mutable();
                            continue block11;
                        }
                        case 2: 
                        case 6: 
                        case 14: 
                        case 30: {
                            t.setProduct(v);
                        }
                    }
                }
                MutableIntegerModuloP d = t.mutable();
                block12: for (int i = 32; i < 256; ++i) {
                    d.setSquare();
                    switch (i) {
                        case 191: 
                        case 223: {
                            d.setProduct(t);
                            continue block12;
                        }
                        case 253: {
                            d.setProduct(w);
                            continue block12;
                        }
                        case 63: 
                        case 255: {
                            d.setProduct(imp);
                        }
                    }
                }
                return d.fixed();
            }
        }

        public static final class Secp256R1Field
        implements MultiplicativeInverser {
            private static final Secp256R1Field instance = new Secp256R1Field();
            private static final BigInteger b = P256OrderField.MODULUS.subtract(BigInteger.TWO);

            @Override
            public ImmutableIntegerModuloP inverse(IntegerModuloP imp) {
                int i;
                IntegerModuloP[] w = new IntegerModuloP[4];
                w[0] = imp.fixed();
                MutableIntegerModuloP t = imp.mutable();
                for (int i2 = 1; i2 < 4; ++i2) {
                    t.setSquare();
                    t.setProduct(imp);
                    w[i2] = t.fixed();
                }
                MutableIntegerModuloP d = null;
                block6: for (i = 4; i < 32; ++i) {
                    t.setSquare();
                    switch (i) {
                        case 7: {
                            t.setProduct(w[3]);
                            d = t.mutable();
                            continue block6;
                        }
                        case 15: {
                            t.setProduct(d);
                            d = t.mutable();
                            continue block6;
                        }
                        case 31: {
                            t.setProduct(d);
                        }
                    }
                }
                d = t.mutable();
                for (i = 32; i < 128; ++i) {
                    d.setSquare();
                    if (i != 95 && i != 127) continue;
                    d.setProduct(t);
                }
                int k = -1;
                for (int i3 = 127; i3 >= 0; --i3) {
                    if (b.testBit(i3)) {
                        if (k == w.length - 2) {
                            d.setSquare();
                            d.setProduct(w[w.length - 1]);
                            k = -1;
                            continue;
                        }
                        ++k;
                        d.setSquare();
                        continue;
                    }
                    if (k >= 0) {
                        d.setProduct(w[k]);
                        k = -1;
                    }
                    d.setSquare();
                }
                return d.fixed();
            }
        }

        public static final class Default
        implements MultiplicativeInverser {
            private final BigInteger b;

            Default(BigInteger b) {
                this.b = b.subtract(BigInteger.TWO);
            }

            @Override
            public ImmutableIntegerModuloP inverse(IntegerModuloP imp) {
                MutableIntegerModuloP y = imp.getField().get1().mutable();
                MutableIntegerModuloP x = imp.mutable();
                int bitLength = this.b.bitLength();
                for (int bit = 0; bit < bitLength; ++bit) {
                    if (this.b.testBit(bit)) {
                        y.setProduct(x);
                    }
                    x.setSquare();
                }
                return y.fixed();
            }
        }
    }
}

