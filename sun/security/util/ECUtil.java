/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECField;
import java.security.spec.ECFieldFp;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Objects;
import jdk.internal.access.SharedSecrets;
import sun.security.util.ArrayUtil;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ECKeySizeParameterSpec;

public final class ECUtil {
    public static byte[] sArray(BigInteger s, ECParameterSpec params) {
        byte[] arr = s.toByteArray();
        ArrayUtil.reverse(arr);
        int byteLength = (params.getOrder().bitLength() + 7) / 8;
        byte[] arrayS = new byte[byteLength];
        int length = Math.min(byteLength, arr.length);
        System.arraycopy(arr, 0, arrayS, 0, length);
        return arrayS;
    }

    public static ECPoint decodePoint(byte[] data, EllipticCurve curve) throws IOException {
        if (data.length == 0 || data[0] != 4) {
            throw new IOException("Only uncompressed point format supported");
        }
        int n = (data.length - 1) / 2;
        if (n != curve.getField().getFieldSize() + 7 >> 3) {
            throw new IOException("Point does not match field size");
        }
        byte[] xb = Arrays.copyOfRange(data, 1, 1 + n);
        byte[] yb = Arrays.copyOfRange(data, n + 1, n + 1 + n);
        return new ECPoint(new BigInteger(1, xb), new BigInteger(1, yb));
    }

    public static byte[] encodePoint(ECPoint point, EllipticCurve curve) {
        int n = curve.getField().getFieldSize() + 7 >> 3;
        byte[] xb = ECUtil.trimZeroes(point.getAffineX().toByteArray());
        byte[] yb = ECUtil.trimZeroes(point.getAffineY().toByteArray());
        if (xb.length > n || yb.length > n) {
            throw new RuntimeException("Point coordinates do not match field size");
        }
        byte[] b = new byte[1 + (n << 1)];
        b[0] = 4;
        System.arraycopy(xb, 0, b, n - xb.length + 1, xb.length);
        System.arraycopy(yb, 0, b, b.length - yb.length, yb.length);
        return b;
    }

    public static byte[] trimZeroes(byte[] b) {
        int i;
        for (i = 0; i < b.length - 1 && b[i] == 0; ++i) {
        }
        if (i == 0) {
            return b;
        }
        return Arrays.copyOfRange(b, i, b.length);
    }

    private static KeyFactory getKeyFactory() {
        try {
            return KeyFactory.getInstance("EC", "SunEC");
        }
        catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    public static ECPublicKey decodeX509ECPublicKey(byte[] encoded) throws InvalidKeySpecException {
        KeyFactory keyFactory = ECUtil.getKeyFactory();
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return (ECPublicKey)keyFactory.generatePublic(keySpec);
    }

    public static byte[] x509EncodeECPublicKey(ECPoint w, ECParameterSpec params) throws InvalidKeySpecException {
        KeyFactory keyFactory = ECUtil.getKeyFactory();
        ECPublicKeySpec keySpec = new ECPublicKeySpec(w, params);
        PublicKey key = keyFactory.generatePublic(keySpec);
        return key.getEncoded();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static ECPrivateKey decodePKCS8ECPrivateKey(byte[] encoded) throws InvalidKeySpecException {
        KeyFactory keyFactory = ECUtil.getKeyFactory();
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        try {
            ECPrivateKey eCPrivateKey = (ECPrivateKey)keyFactory.generatePrivate(keySpec);
            return eCPrivateKey;
        }
        finally {
            SharedSecrets.getJavaSecuritySpecAccess().clearEncodedKeySpec(keySpec);
        }
    }

    public static ECPrivateKey generateECPrivateKey(BigInteger s, ECParameterSpec params) throws InvalidKeySpecException {
        KeyFactory keyFactory = ECUtil.getKeyFactory();
        ECPrivateKeySpec keySpec = new ECPrivateKeySpec(s, params);
        return (ECPrivateKey)keyFactory.generatePrivate(keySpec);
    }

    public static AlgorithmParameters getECParameters(Provider p) {
        try {
            if (p != null) {
                return AlgorithmParameters.getInstance("EC", p);
            }
            return AlgorithmParameters.getInstance("EC");
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException(nsae);
        }
    }

    public static byte[] encodeECParameterSpec(Provider p, ECParameterSpec spec) {
        AlgorithmParameters parameters = ECUtil.getECParameters(p);
        try {
            parameters.init(spec);
        }
        catch (InvalidParameterSpecException ipse) {
            throw new RuntimeException("Not a known named curve: " + spec);
        }
        try {
            return parameters.getEncoded();
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static ECParameterSpec getECParameterSpec(Provider p, ECParameterSpec spec) {
        AlgorithmParameters parameters = ECUtil.getECParameters(p);
        try {
            parameters.init(spec);
            return parameters.getParameterSpec(ECParameterSpec.class);
        }
        catch (InvalidParameterSpecException ipse) {
            return null;
        }
    }

    public static ECParameterSpec getECParameterSpec(Provider p, byte[] params) throws IOException {
        AlgorithmParameters parameters = ECUtil.getECParameters(p);
        parameters.init(params);
        try {
            return parameters.getParameterSpec(ECParameterSpec.class);
        }
        catch (InvalidParameterSpecException ipse) {
            return null;
        }
    }

    public static ECParameterSpec getECParameterSpec(Provider p, String name) {
        AlgorithmParameters parameters = ECUtil.getECParameters(p);
        try {
            parameters.init(new ECGenParameterSpec(name));
            return parameters.getParameterSpec(ECParameterSpec.class);
        }
        catch (InvalidParameterSpecException ipse) {
            return null;
        }
    }

    public static ECParameterSpec getECParameterSpec(Provider p, int keySize) {
        AlgorithmParameters parameters = ECUtil.getECParameters(p);
        try {
            parameters.init(new ECKeySizeParameterSpec(keySize));
            return parameters.getParameterSpec(ECParameterSpec.class);
        }
        catch (InvalidParameterSpecException ipse) {
            return null;
        }
    }

    public static String getCurveName(Provider p, ECParameterSpec spec) {
        ECGenParameterSpec nameSpec;
        AlgorithmParameters parameters = ECUtil.getECParameters(p);
        try {
            parameters.init(spec);
            nameSpec = parameters.getParameterSpec(ECGenParameterSpec.class);
        }
        catch (InvalidParameterSpecException ipse) {
            return null;
        }
        if (nameSpec == null) {
            return null;
        }
        return nameSpec.getName();
    }

    public static boolean equals(ECParameterSpec spec1, ECParameterSpec spec2) {
        if (spec1 == spec2) {
            return true;
        }
        if (spec1 == null || spec2 == null) {
            return false;
        }
        return spec1.getCofactor() == spec2.getCofactor() && spec1.getOrder().equals(spec2.getOrder()) && spec1.getCurve().equals(spec2.getCurve()) && spec1.getGenerator().equals(spec2.getGenerator());
    }

    public static byte[] encodeSignature(byte[] signature) throws SignatureException {
        try {
            int n = signature.length >> 1;
            byte[] bytes = new byte[n];
            System.arraycopy(signature, 0, bytes, 0, n);
            BigInteger r = new BigInteger(1, bytes);
            System.arraycopy(signature, n, bytes, 0, n);
            BigInteger s = new BigInteger(1, bytes);
            DerOutputStream out = new DerOutputStream(signature.length + 10);
            out.putInteger(r);
            out.putInteger(s);
            DerValue result = new DerValue(48, out.toByteArray());
            return result.toByteArray();
        }
        catch (Exception e) {
            throw new SignatureException("Could not encode signature", e);
        }
    }

    public static byte[] decodeSignature(byte[] sig) throws SignatureException {
        try {
            DerInputStream in = new DerInputStream(sig, 0, sig.length, false);
            DerValue[] values = in.getSequence(2);
            if (values.length != 2 || in.available() != 0) {
                throw new IOException("Invalid encoding for signature");
            }
            BigInteger r = values[0].getPositiveBigInteger();
            BigInteger s = values[1].getPositiveBigInteger();
            byte[] rBytes = ECUtil.trimZeroes(r.toByteArray());
            byte[] sBytes = ECUtil.trimZeroes(s.toByteArray());
            int k = Math.max(rBytes.length, sBytes.length);
            byte[] result = new byte[k << 1];
            System.arraycopy(rBytes, 0, result, k - rBytes.length, rBytes.length);
            System.arraycopy(sBytes, 0, result, result.length - sBytes.length, sBytes.length);
            return result;
        }
        catch (Exception e) {
            throw new SignatureException("Invalid encoding for signature", e);
        }
    }

    public static ECPrivateKey checkPrivateKey(ECPrivateKey prv) throws InvalidKeyException {
        Objects.requireNonNull(prv, "Private key must be non-null");
        ECParameterSpec spec = prv.getParams();
        if (spec != null) {
            BigInteger order = spec.getOrder();
            BigInteger sVal = prv.getS();
            if (order != null && sVal != null && (sVal.compareTo(BigInteger.ZERO) <= 0 || sVal.compareTo(order) >= 0)) {
                throw new InvalidKeyException("The private key must be within the range [1, n - 1]");
            }
        }
        return prv;
    }

    public static void validatePublicKey(ECPoint point, ECParameterSpec spec) throws InvalidKeyException {
        BigInteger right;
        ECField eCField = spec.getCurve().getField();
        if (!(eCField instanceof ECFieldFp)) {
            throw new InvalidKeyException("Only curves over prime fields are supported");
        }
        ECFieldFp f = (ECFieldFp)eCField;
        BigInteger p = f.getP();
        if (point.equals(ECPoint.POINT_INFINITY)) {
            throw new InvalidKeyException("Public point is at infinity");
        }
        BigInteger x = point.getAffineX();
        if (x.signum() < 0 || x.compareTo(p) >= 0) {
            throw new InvalidKeyException("Public point x is not in the interval [0, p-1]");
        }
        BigInteger y = point.getAffineY();
        if (y.signum() < 0 || y.compareTo(p) >= 0) {
            throw new InvalidKeyException("Public point y is not in the interval [0, p-1]");
        }
        BigInteger left = y.modPow(BigInteger.TWO, p);
        if (!left.equals(right = x.pow(3).add(spec.getCurve().getA().multiply(x)).add(spec.getCurve().getB()).mod(p))) {
            throw new InvalidKeyException("Public point is not on the curve");
        }
    }

    private ECUtil() {
    }
}

