/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.ProviderException;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.util.Arrays;
import java.util.Optional;
import sun.security.ec.ECOperations;
import sun.security.ec.point.AffinePoint;
import sun.security.ec.point.MutablePoint;
import sun.security.util.ArrayUtil;
import sun.security.util.math.ImmutableIntegerModuloP;
import sun.security.util.math.IntegerFieldModuloP;
import sun.security.util.math.IntegerModuloP;
import sun.security.util.math.MutableIntegerModuloP;

public class ECDSAOperations {
    private final ECOperations ecOps;
    private final AffinePoint basePoint;

    public ECDSAOperations(ECOperations ecOps, ECPoint basePoint) {
        this.ecOps = ecOps;
        this.basePoint = ECDSAOperations.toAffinePoint(basePoint, ecOps.getField());
    }

    public ECOperations getEcOperations() {
        return this.ecOps;
    }

    public AffinePoint basePointMultiply(byte[] scalar) {
        return this.ecOps.multiply(this.basePoint, scalar).asAffine();
    }

    public static AffinePoint toAffinePoint(ECPoint point, IntegerFieldModuloP field) {
        ImmutableIntegerModuloP affineX = field.getElement(point.getAffineX());
        ImmutableIntegerModuloP affineY = field.getElement(point.getAffineY());
        return new AffinePoint(affineX, affineY);
    }

    public static Optional<ECDSAOperations> forParameters(ECParameterSpec ecParams) {
        Optional<ECOperations> curveOps = ECOperations.forParameters(ecParams);
        return curveOps.map(ops -> new ECDSAOperations((ECOperations)ops, ecParams.getGenerator()));
    }

    public byte[] signDigest(byte[] privateKey, byte[] digest, Seed seed) throws ECOperations.IntermediateValueException {
        byte[] nonceArr = this.ecOps.seedToScalar(seed.getSeedValue());
        Nonce nonce = new Nonce(nonceArr);
        return this.signDigest(privateKey, digest, nonce);
    }

    public byte[] signDigest(byte[] privateKey, byte[] digest, Nonce nonce) throws ECOperations.IntermediateValueException {
        int length;
        IntegerFieldModuloP orderField = this.ecOps.getOrderField();
        int orderBits = orderField.getSize().bitLength();
        if (orderBits % 8 != 0 && orderBits < digest.length * 8) {
            throw new ProviderException("Invalid digest length");
        }
        byte[] k = nonce.getNonceValue();
        if (k.length != (length = (orderField.getSize().bitLength() + 7) / 8)) {
            throw new ProviderException("Incorrect nonce length");
        }
        MutablePoint R = this.ecOps.multiply(this.basePoint, k);
        ImmutableIntegerModuloP r = R.asAffine().getX();
        byte[] temp = new byte[length];
        r = ECDSAOperations.b2a(r, orderField, temp);
        byte[] result = new byte[2 * length];
        ArrayUtil.reverse(temp);
        System.arraycopy(temp, 0, result, 0, length);
        if (ECOperations.allZero(temp)) {
            throw new ECOperations.IntermediateValueException();
        }
        ImmutableIntegerModuloP dU = orderField.getElement(privateKey);
        int lengthE = Math.min(length, digest.length);
        byte[] E = new byte[lengthE];
        System.arraycopy(digest, 0, E, 0, lengthE);
        ArrayUtil.reverse(E);
        ImmutableIntegerModuloP e = orderField.getElement(E);
        ImmutableIntegerModuloP kElem = orderField.getElement(k);
        ImmutableIntegerModuloP kInv = kElem.multiplicativeInverse();
        MutableIntegerModuloP s = r.mutable();
        s.setProduct(dU).setSum(e).setProduct(kInv);
        s.asByteArray(temp);
        ArrayUtil.reverse(temp);
        System.arraycopy(temp, 0, result, length, length);
        if (ECOperations.allZero(temp)) {
            throw new ECOperations.IntermediateValueException();
        }
        return result;
    }

    public boolean verifySignedDigest(byte[] digest, byte[] sig, ECPoint pp) {
        byte[] s;
        byte[] r;
        IntegerFieldModuloP field = this.ecOps.getField();
        IntegerFieldModuloP orderField = this.ecOps.getOrderField();
        BigInteger mod = orderField.getSize();
        int length = (mod.bitLength() + 7) / 8;
        int encodeLength = sig.length / 2;
        if (sig.length % 2 != 0 || encodeLength > length) {
            return false;
        }
        if (encodeLength == length) {
            r = Arrays.copyOf(sig, length);
            s = Arrays.copyOfRange(sig, length, length * 2);
        } else {
            r = new byte[length];
            s = new byte[length];
            System.arraycopy(sig, 0, r, length - encodeLength, encodeLength);
            System.arraycopy(sig, encodeLength, s, length - encodeLength, encodeLength);
        }
        BigInteger rb = new BigInteger(1, r);
        BigInteger sb = new BigInteger(1, s);
        if (rb.signum() == 0 || sb.signum() == 0 || rb.compareTo(mod) >= 0 || sb.compareTo(mod) >= 0) {
            return false;
        }
        ArrayUtil.reverse(r);
        ArrayUtil.reverse(s);
        ImmutableIntegerModuloP ri = orderField.getElement(r);
        ImmutableIntegerModuloP si = orderField.getElement(s);
        int lengthE = Math.min(length, digest.length);
        byte[] E = new byte[lengthE];
        System.arraycopy(digest, 0, E, 0, lengthE);
        ArrayUtil.reverse(E);
        ImmutableIntegerModuloP e = orderField.getElement(E);
        ImmutableIntegerModuloP sInv = si.multiplicativeInverse();
        ImmutableIntegerModuloP u1 = e.multiply(sInv);
        ImmutableIntegerModuloP u2 = ri.multiply(sInv);
        byte[] temp1 = new byte[length];
        ECDSAOperations.b2a(u1, orderField, temp1);
        byte[] temp2 = new byte[length];
        ECDSAOperations.b2a(u2, orderField, temp2);
        MutablePoint p1 = this.ecOps.multiply(this.basePoint, temp1);
        MutablePoint p2 = this.ecOps.multiply(pp, temp2);
        this.ecOps.setSum(p1, p2.asAffine());
        ImmutableIntegerModuloP result = p1.asAffine().getX();
        ECDSAOperations.b2a(result, orderField, temp1);
        return MessageDigest.isEqual(temp1, r);
    }

    public static ImmutableIntegerModuloP b2a(IntegerModuloP b, IntegerFieldModuloP orderField, byte[] temp1) {
        b.asByteArray(temp1);
        ImmutableIntegerModuloP b2 = orderField.getElement(temp1);
        b2.asByteArray(temp1);
        return b2;
    }

    public static class Seed {
        private final byte[] seedValue;

        public Seed(byte[] seedValue) {
            this.seedValue = seedValue;
        }

        public byte[] getSeedValue() {
            return this.seedValue;
        }
    }

    public static class Nonce {
        private final byte[] nonceValue;

        public Nonce(byte[] nonceValue) {
            this.nonceValue = nonceValue;
        }

        public byte[] getNonceValue() {
            return this.nonceValue;
        }
    }
}

