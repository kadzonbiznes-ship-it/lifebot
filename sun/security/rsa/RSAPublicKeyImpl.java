/*
 * Decompiled with CFR 0.152.
 */
package sun.security.rsa;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyRep;
import java.security.ProviderException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import sun.security.rsa.RSAKeyFactory;
import sun.security.rsa.RSAUtil;
import sun.security.util.BitArray;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.X509Key;

public final class RSAPublicKeyImpl
extends X509Key
implements RSAPublicKey {
    private static final long serialVersionUID = 2644735423591199609L;
    private static final BigInteger THREE = BigInteger.valueOf(3L);
    private BigInteger n;
    private BigInteger e;
    private final transient RSAUtil.KeyType type;
    private final transient AlgorithmParameterSpec keyParams;

    public static RSAPublicKey newKey(RSAUtil.KeyType type, String format, byte[] encoded) throws InvalidKeyException {
        RSAPublicKeyImpl key;
        switch (format) {
            case "X.509": {
                key = new RSAPublicKeyImpl(encoded);
                RSAKeyFactory.checkKeyAlgo(key, type.keyAlgo);
                break;
            }
            case "PKCS#1": {
                try {
                    BigInteger[] comps = RSAPublicKeyImpl.parseASN1(encoded);
                    key = new RSAPublicKeyImpl(type, null, comps[0], comps[1]);
                    break;
                }
                catch (IOException ioe) {
                    throw new InvalidKeyException("Invalid PKCS#1 encoding", ioe);
                }
            }
            default: {
                throw new InvalidKeyException("Unsupported RSA PublicKey format: " + format);
            }
        }
        return key;
    }

    public static RSAPublicKey newKey(RSAUtil.KeyType type, AlgorithmParameterSpec params, BigInteger n, BigInteger e) throws InvalidKeyException {
        return new RSAPublicKeyImpl(type, params, n, e);
    }

    RSAPublicKeyImpl(RSAUtil.KeyType type, AlgorithmParameterSpec keyParams, BigInteger n, BigInteger e) throws InvalidKeyException {
        RSAKeyFactory.checkRSAProviderKeyLengths(n.bitLength(), e);
        RSAPublicKeyImpl.checkExponentRange(n, e);
        this.n = n;
        this.e = e;
        try {
            this.algid = RSAUtil.createAlgorithmId(type, keyParams);
        }
        catch (ProviderException pe) {
            throw new InvalidKeyException(pe);
        }
        this.type = type;
        this.keyParams = keyParams;
        DerOutputStream out = new DerOutputStream();
        out.putInteger(n);
        out.putInteger(e);
        byte[] keyArray = new DerValue(48, out.toByteArray()).toByteArray();
        this.setKey(new BitArray(keyArray.length * 8, keyArray));
    }

    private RSAPublicKeyImpl(byte[] encoded) throws InvalidKeyException {
        if (encoded == null || encoded.length == 0) {
            throw new InvalidKeyException("Missing key encoding");
        }
        this.decode(encoded);
        RSAKeyFactory.checkRSAProviderKeyLengths(this.n.bitLength(), this.e);
        RSAPublicKeyImpl.checkExponentRange(this.n, this.e);
        try {
            Object[] o = RSAUtil.getTypeAndParamSpec(this.algid);
            this.type = (RSAUtil.KeyType)((Object)o[0]);
            this.keyParams = (AlgorithmParameterSpec)o[1];
        }
        catch (ProviderException e) {
            throw new InvalidKeyException(e);
        }
    }

    static void checkExponentRange(BigInteger mod, BigInteger exp) throws InvalidKeyException {
        if (exp.compareTo(mod) >= 0) {
            throw new InvalidKeyException("exponent is larger than modulus");
        }
        if (exp.compareTo(THREE) < 0) {
            throw new InvalidKeyException("exponent is smaller than 3");
        }
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
    public AlgorithmParameterSpec getParams() {
        return this.keyParams;
    }

    private static BigInteger[] parseASN1(byte[] raw) throws IOException {
        DerValue derValue = new DerValue(raw);
        if (derValue.tag != 48) {
            throw new IOException("Not a SEQUENCE");
        }
        BigInteger[] result = new BigInteger[]{derValue.data.getPositiveBigInteger(), derValue.data.getPositiveBigInteger()};
        if (derValue.data.available() != 0) {
            throw new IOException("Extra data available");
        }
        return result;
    }

    @Override
    protected void parseKeyBits() throws InvalidKeyException {
        try {
            BigInteger[] comps = RSAPublicKeyImpl.parseASN1(this.getKey().toByteArray());
            this.n = comps[0];
            this.e = comps[1];
        }
        catch (IOException e) {
            throw new InvalidKeyException("Invalid RSA public key", e);
        }
    }

    @Override
    public String toString() {
        return "Sun " + this.type.keyAlgo + " public key, " + this.n.bitLength() + " bits\n  params: " + this.keyParams + "\n  modulus: " + this.n + "\n  public exponent: " + this.e;
    }

    private Object writeReplace() throws ObjectStreamException {
        return new KeyRep(KeyRep.Type.PUBLIC, this.getAlgorithm(), this.getFormat(), this.getEncoded());
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException("RSAPublicKeyImpl keys are not directly deserializable");
    }
}

