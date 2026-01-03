/*
 * Decompiled with CFR 0.152.
 */
package sun.security.rsa;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.ProviderException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import sun.security.pkcs.PKCS8Key;
import sun.security.rsa.RSAKeyFactory;
import sun.security.rsa.RSAPrivateKeyImpl;
import sun.security.rsa.RSAUtil;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public final class RSAPrivateCrtKeyImpl
extends PKCS8Key
implements RSAPrivateCrtKey {
    private static final long serialVersionUID = -1326088454257084918L;
    private BigInteger n;
    private BigInteger e;
    private BigInteger d;
    private BigInteger p;
    private BigInteger q;
    private BigInteger pe;
    private BigInteger qe;
    private BigInteger coeff;
    private final transient RSAUtil.KeyType type;
    private final transient AlgorithmParameterSpec keyParams;

    public static RSAPrivateKey newKey(RSAUtil.KeyType type, String format, byte[] encoded) throws InvalidKeyException {
        if (encoded == null || encoded.length == 0) {
            throw new InvalidKeyException("Missing key encoding");
        }
        switch (format) {
            case "PKCS#8": {
                RSAPrivateCrtKeyImpl key = new RSAPrivateCrtKeyImpl(encoded);
                RSAKeyFactory.checkKeyAlgo(key, type.keyAlgo);
                if (RSAPrivateCrtKeyImpl.checkComponents(key)) {
                    return key;
                }
                return new RSAPrivateKeyImpl(key.type, key.keyParams, key.getModulus(), key.getPrivateExponent());
            }
            case "PKCS#1": {
                try {
                    BigInteger[] comps = RSAPrivateCrtKeyImpl.parseASN1(encoded);
                    if (comps[1].signum() == 0 || comps[3].signum() == 0 || comps[4].signum() == 0 || comps[5].signum() == 0 || comps[6].signum() == 0 || comps[7].signum() == 0) {
                        return new RSAPrivateKeyImpl(type, null, comps[0], comps[2]);
                    }
                    return new RSAPrivateCrtKeyImpl(type, null, comps[0], comps[1], comps[2], comps[3], comps[4], comps[5], comps[6], comps[7]);
                }
                catch (IOException ioe) {
                    throw new InvalidKeyException("Invalid PKCS#1 encoding", ioe);
                }
            }
        }
        throw new InvalidKeyException("Unsupported RSA Private(Crt)Key format: " + format);
    }

    static boolean checkComponents(RSAPrivateCrtKey key) {
        return key.getPublicExponent().signum() != 0 && key.getPrimeExponentP().signum() != 0 && key.getPrimeExponentQ().signum() != 0 && key.getPrimeP().signum() != 0 && key.getPrimeQ().signum() != 0 && key.getCrtCoefficient().signum() != 0;
    }

    public static RSAPrivateKey newKey(RSAUtil.KeyType type, AlgorithmParameterSpec params, BigInteger n, BigInteger e, BigInteger d, BigInteger p, BigInteger q, BigInteger pe, BigInteger qe, BigInteger coeff) throws InvalidKeyException {
        if (e.signum() == 0 || p.signum() == 0 || q.signum() == 0 || pe.signum() == 0 || qe.signum() == 0 || coeff.signum() == 0) {
            return new RSAPrivateKeyImpl(type, params, n, d);
        }
        return new RSAPrivateCrtKeyImpl(type, params, n, e, d, p, q, pe, qe, coeff);
    }

    private RSAPrivateCrtKeyImpl(byte[] encoded) throws InvalidKeyException {
        super(encoded);
        this.parseKeyBits();
        RSAKeyFactory.checkRSAProviderKeyLengths(this.n.bitLength(), this.e);
        try {
            Object[] o = RSAUtil.getTypeAndParamSpec(this.algid);
            this.type = (RSAUtil.KeyType)((Object)o[0]);
            this.keyParams = (AlgorithmParameterSpec)o[1];
        }
        catch (ProviderException e) {
            throw new InvalidKeyException(e);
        }
    }

    RSAPrivateCrtKeyImpl(RSAUtil.KeyType type, AlgorithmParameterSpec keyParams, BigInteger n, BigInteger e, BigInteger d, BigInteger p, BigInteger q, BigInteger pe, BigInteger qe, BigInteger coeff) throws InvalidKeyException {
        RSAKeyFactory.checkRSAProviderKeyLengths(n.bitLength(), e);
        this.n = n;
        this.e = e;
        this.d = d;
        this.p = p;
        this.q = q;
        this.pe = pe;
        this.qe = qe;
        this.coeff = coeff;
        try {
            this.algid = RSAUtil.createAlgorithmId(type, keyParams);
        }
        catch (ProviderException exc) {
            throw new InvalidKeyException(exc);
        }
        this.type = type;
        this.keyParams = keyParams;
        byte[][] nbytes = new byte[][]{n.toByteArray(), e.toByteArray(), d.toByteArray(), p.toByteArray(), q.toByteArray(), pe.toByteArray(), qe.toByteArray(), coeff.toByteArray()};
        DerOutputStream out = new DerOutputStream(nbytes[0].length + nbytes[1].length + nbytes[2].length + nbytes[3].length + nbytes[4].length + nbytes[5].length + nbytes[6].length + nbytes[7].length + 100);
        out.putInteger(0);
        out.putInteger(nbytes[0]);
        out.putInteger(nbytes[1]);
        out.putInteger(nbytes[2]);
        out.putInteger(nbytes[3]);
        out.putInteger(nbytes[4]);
        out.putInteger(nbytes[5]);
        out.putInteger(nbytes[6]);
        out.putInteger(nbytes[7]);
        Arrays.fill(nbytes[2], (byte)0);
        Arrays.fill(nbytes[3], (byte)0);
        Arrays.fill(nbytes[4], (byte)0);
        Arrays.fill(nbytes[5], (byte)0);
        Arrays.fill(nbytes[6], (byte)0);
        Arrays.fill(nbytes[7], (byte)0);
        DerValue val = DerValue.wrap((byte)48, out);
        this.key = val.toByteArray();
        val.clear();
    }

    @Override
    public String getAlgorithm() {
        return this.type.keyAlgo;
    }

    @Override
    public BigInteger getModulus() {
        return this.n;
    }

    @Override
    public BigInteger getPublicExponent() {
        return this.e;
    }

    @Override
    public BigInteger getPrivateExponent() {
        return this.d;
    }

    @Override
    public BigInteger getPrimeP() {
        return this.p;
    }

    @Override
    public BigInteger getPrimeQ() {
        return this.q;
    }

    @Override
    public BigInteger getPrimeExponentP() {
        return this.pe;
    }

    @Override
    public BigInteger getPrimeExponentQ() {
        return this.qe;
    }

    @Override
    public BigInteger getCrtCoefficient() {
        return this.coeff;
    }

    @Override
    public AlgorithmParameterSpec getParams() {
        return this.keyParams;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static BigInteger[] parseASN1(byte[] raw) throws IOException {
        DerValue derValue = new DerValue(raw);
        try {
            if (derValue.tag != 48) {
                throw new IOException("Not a SEQUENCE");
            }
            int version = derValue.data.getInteger();
            if (version != 0) {
                throw new IOException("Version must be 0");
            }
            BigInteger[] result = new BigInteger[8];
            for (int i = 0; i < result.length; ++i) {
                result[i] = derValue.data.getPositiveBigInteger();
            }
            if (derValue.data.available() != 0) {
                throw new IOException("Extra data available");
            }
            BigInteger[] bigIntegerArray = result;
            return bigIntegerArray;
        }
        finally {
            derValue.clear();
        }
    }

    private void parseKeyBits() throws InvalidKeyException {
        try {
            BigInteger[] comps = RSAPrivateCrtKeyImpl.parseASN1(this.key);
            this.n = comps[0];
            this.e = comps[1];
            this.d = comps[2];
            this.p = comps[3];
            this.q = comps[4];
            this.pe = comps[5];
            this.qe = comps[6];
            this.coeff = comps[7];
        }
        catch (IOException e) {
            throw new InvalidKeyException("Invalid RSA private key", e);
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException("RSAPrivateCrtKeyImpl keys are not directly deserializable");
    }
}

