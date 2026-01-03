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
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import sun.security.pkcs.PKCS8Key;
import sun.security.rsa.RSAKeyFactory;
import sun.security.rsa.RSAUtil;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public final class RSAPrivateKeyImpl
extends PKCS8Key
implements RSAPrivateKey {
    private static final long serialVersionUID = -33106691987952810L;
    private final BigInteger n;
    private final BigInteger d;
    private final transient RSAUtil.KeyType type;
    private final transient AlgorithmParameterSpec keyParams;

    RSAPrivateKeyImpl(RSAUtil.KeyType type, AlgorithmParameterSpec keyParams, BigInteger n, BigInteger d) throws InvalidKeyException {
        RSAKeyFactory.checkRSAProviderKeyLengths(n.bitLength(), null);
        this.n = n;
        this.d = d;
        try {
            this.algid = RSAUtil.createAlgorithmId(type, keyParams);
        }
        catch (ProviderException pe) {
            throw new InvalidKeyException(pe);
        }
        this.type = type;
        this.keyParams = keyParams;
        byte[] nbytes = n.toByteArray();
        byte[] dbytes = d.toByteArray();
        DerOutputStream out = new DerOutputStream(nbytes.length + dbytes.length + 50);
        out.putInteger(0);
        out.putInteger(nbytes);
        Arrays.fill(nbytes, (byte)0);
        out.putInteger(0);
        out.putInteger(dbytes);
        Arrays.fill(dbytes, (byte)0);
        out.putInteger(0);
        out.putInteger(0);
        out.putInteger(0);
        out.putInteger(0);
        out.putInteger(0);
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
    public BigInteger getPrivateExponent() {
        return this.d;
    }

    @Override
    public AlgorithmParameterSpec getParams() {
        return this.keyParams;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException("RSAPrivateKeyImpl keys are not directly deserializable");
    }
}

