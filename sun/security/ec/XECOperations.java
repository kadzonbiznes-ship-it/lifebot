/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec;

import java.math.BigInteger;
import java.security.ProviderException;
import java.security.SecureRandom;
import sun.security.ec.XECParameters;
import sun.security.util.math.ImmutableIntegerModuloP;
import sun.security.util.math.IntegerFieldModuloP;
import sun.security.util.math.IntegerModuloP;
import sun.security.util.math.MutableIntegerModuloP;
import sun.security.util.math.SmallValue;
import sun.security.util.math.intpoly.IntegerPolynomial25519;
import sun.security.util.math.intpoly.IntegerPolynomial448;

public class XECOperations {
    private final XECParameters params;
    private final IntegerFieldModuloP field;
    private final ImmutableIntegerModuloP zero;
    private final ImmutableIntegerModuloP one;
    private final SmallValue a24;
    private final ImmutableIntegerModuloP basePoint;

    public XECOperations(XECParameters c) {
        this.params = c;
        BigInteger p = this.params.getP();
        this.field = XECOperations.getIntegerFieldModulo(p);
        this.zero = this.field.getElement(BigInteger.ZERO).fixed();
        this.one = this.field.get1().fixed();
        this.a24 = this.field.getSmallValue(this.params.getA24());
        this.basePoint = this.field.getElement(BigInteger.valueOf(c.getBasePoint()));
    }

    public XECParameters getParameters() {
        return this.params;
    }

    public byte[] generatePrivate(SecureRandom random) {
        byte[] result = new byte[this.params.getBytes()];
        random.nextBytes(result);
        return result;
    }

    public BigInteger computePublic(byte[] k) {
        this.pruneK(k);
        return this.pointMultiply(k, this.basePoint).asBigInteger();
    }

    public byte[] encodedPointMultiply(byte[] k, BigInteger u) {
        this.pruneK(k);
        ImmutableIntegerModuloP elemU = this.field.getElement(u);
        return this.pointMultiply(k, elemU).asByteArray(this.params.getBytes());
    }

    public byte[] encodedPointMultiply(byte[] k, byte[] u) {
        this.pruneK(k);
        ImmutableIntegerModuloP elemU = this.decodeU(u);
        return this.pointMultiply(k, elemU).asByteArray(this.params.getBytes());
    }

    private ImmutableIntegerModuloP decodeU(byte[] u, int bits) {
        XECOperations.maskHighOrder(u, bits);
        return this.field.getElement(u);
    }

    private static byte maskHighOrder(byte[] arr, int bits) {
        int lastByteIndex = arr.length - 1;
        byte bitsMod8 = (byte)(bits % 8);
        byte highBits = bitsMod8 == 0 ? (byte)8 : (byte)bitsMod8;
        byte msbMaskOff = (byte)((1 << highBits) - 1);
        int n = lastByteIndex;
        arr[n] = (byte)(arr[n] & msbMaskOff);
        return highBits;
    }

    private static void pruneK(byte[] k, int bits, int logCofactor) {
        int lastByteIndex = k.length - 1;
        byte highBits = XECOperations.maskHighOrder(k, bits);
        byte msbMaskOn = (byte)(1 << highBits - 1);
        int n = lastByteIndex;
        k[n] = (byte)(k[n] | msbMaskOn);
        byte lsbMaskOff = (byte)(255 << logCofactor);
        k[0] = (byte)(k[0] & lsbMaskOff);
    }

    private void pruneK(byte[] k) {
        XECOperations.pruneK(k, this.params.getBits(), this.params.getLogCofactor());
    }

    private ImmutableIntegerModuloP decodeU(byte[] u) {
        return this.decodeU(u, this.params.getBits());
    }

    private static void cswap(int swap, MutableIntegerModuloP x1, MutableIntegerModuloP x2) {
        x1.conditionalSwapWith(x2, swap);
    }

    private static IntegerFieldModuloP getIntegerFieldModulo(BigInteger p) {
        if (p.equals(IntegerPolynomial25519.MODULUS)) {
            return IntegerPolynomial25519.ONE;
        }
        if (p.equals(IntegerPolynomial448.MODULUS)) {
            return IntegerPolynomial448.ONE;
        }
        throw new ProviderException("Unsupported prime: " + p.toString());
    }

    private int bitAt(byte[] arr, int index) {
        int byteIndex = index / 8;
        int bitIndex = index % 8;
        return (arr[byteIndex] & 1 << bitIndex) >> bitIndex;
    }

    private IntegerModuloP pointMultiply(byte[] k, ImmutableIntegerModuloP u) {
        ImmutableIntegerModuloP x_1 = u;
        MutableIntegerModuloP x_2 = this.one.mutable();
        MutableIntegerModuloP z_2 = this.zero.mutable();
        MutableIntegerModuloP x_3 = u.mutable();
        MutableIntegerModuloP z_3 = this.one.mutable();
        int swap = 0;
        MutableIntegerModuloP m1 = this.zero.mutable();
        MutableIntegerModuloP DA = this.zero.mutable();
        MutableIntegerModuloP E = this.zero.mutable();
        MutableIntegerModuloP a24_times_E = this.zero.mutable();
        for (int t = this.params.getBits() - 1; t >= 0; --t) {
            int k_t = this.bitAt(k, t);
            XECOperations.cswap(swap ^= k_t, x_2, x_3);
            XECOperations.cswap(swap, z_2, z_3);
            swap = k_t;
            m1.setValue(x_2).setSum(z_2);
            DA.setValue(x_3).setDifference(z_3).setProduct(m1);
            m1.setSquare();
            x_2.setDifference(z_2);
            x_3.setSum(z_3).setProduct(x_2);
            x_2.setSquare();
            E.setValue(m1).setDifference(x_2);
            a24_times_E.setValue(E);
            a24_times_E.setProduct(this.a24);
            x_2.setProduct(m1);
            z_2.setValue(m1).setSum(a24_times_E).setProduct(E);
            z_3.setValue(DA).setDifference(x_3).setSquare().setProduct(x_1);
            x_3.setSum(DA).setSquare();
        }
        XECOperations.cswap(swap, x_2, x_3);
        XECOperations.cswap(swap, z_2, z_3);
        return x_2.setProduct(z_2.multiplicativeInverse());
    }
}

