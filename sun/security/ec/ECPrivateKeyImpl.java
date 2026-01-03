/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import sun.security.ec.ECOperations;
import sun.security.ec.ECPublicKeyImpl;
import sun.security.ec.point.AffinePoint;
import sun.security.ec.point.MutablePoint;
import sun.security.pkcs.PKCS8Key;
import sun.security.util.ArrayUtil;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ECParameters;
import sun.security.util.ECUtil;
import sun.security.x509.AlgorithmId;

public final class ECPrivateKeyImpl
extends PKCS8Key
implements ECPrivateKey {
    private static final long serialVersionUID = 88695385615075129L;
    private BigInteger s;
    private byte[] arrayS;
    private ECParameterSpec params;

    ECPrivateKeyImpl(byte[] encoded) throws InvalidKeyException {
        super(encoded);
        this.parseKeyBits();
    }

    ECPrivateKeyImpl(BigInteger s, ECParameterSpec params) throws InvalidKeyException {
        this.s = s;
        this.params = params;
        this.makeEncoding(s);
    }

    ECPrivateKeyImpl(byte[] s, ECParameterSpec params) throws InvalidKeyException {
        this.arrayS = (byte[])s.clone();
        this.params = params;
        this.makeEncoding(s);
    }

    private void makeEncoding(byte[] s) throws InvalidKeyException {
        this.algid = new AlgorithmId(AlgorithmId.EC_oid, ECParameters.getAlgorithmParameters(this.params));
        DerOutputStream out = new DerOutputStream();
        out.putInteger(1);
        byte[] privBytes = (byte[])s.clone();
        ArrayUtil.reverse(privBytes);
        out.putOctetString(privBytes);
        Arrays.fill(privBytes, (byte)0);
        DerValue val = DerValue.wrap((byte)48, out);
        this.key = val.toByteArray();
        val.clear();
    }

    private void makeEncoding(BigInteger s) throws InvalidKeyException {
        this.algid = new AlgorithmId(AlgorithmId.EC_oid, ECParameters.getAlgorithmParameters(this.params));
        byte[] sArr = s.toByteArray();
        int numOctets = (this.params.getOrder().bitLength() + 7) / 8;
        byte[] sOctets = new byte[numOctets];
        int inPos = Math.max(sArr.length - sOctets.length, 0);
        int outPos = Math.max(sOctets.length - sArr.length, 0);
        int length = Math.min(sArr.length, sOctets.length);
        System.arraycopy(sArr, inPos, sOctets, outPos, length);
        Arrays.fill(sArr, (byte)0);
        DerOutputStream out = new DerOutputStream();
        out.putInteger(1);
        out.putOctetString(sOctets);
        Arrays.fill(sOctets, (byte)0);
        DerValue val = DerValue.wrap((byte)48, out);
        this.key = val.toByteArray();
        val.clear();
    }

    @Override
    public String getAlgorithm() {
        return "EC";
    }

    @Override
    public BigInteger getS() {
        if (this.s == null) {
            byte[] arrCopy = (byte[])this.arrayS.clone();
            ArrayUtil.reverse(arrCopy);
            this.s = new BigInteger(1, arrCopy);
            Arrays.fill(arrCopy, (byte)0);
        }
        return this.s;
    }

    private byte[] getArrayS0() {
        if (this.arrayS == null) {
            this.arrayS = ECUtil.sArray(this.getS(), this.params);
        }
        return this.arrayS;
    }

    public byte[] getArrayS() {
        return (byte[])this.getArrayS0().clone();
    }

    @Override
    public ECParameterSpec getParams() {
        return this.params;
    }

    private void parseKeyBits() throws InvalidKeyException {
        try {
            DerInputStream in = new DerInputStream(this.key);
            DerValue derValue = in.getDerValue();
            if (derValue.tag != 48) {
                throw new IOException("Not a SEQUENCE");
            }
            DerInputStream data = derValue.data;
            int version = data.getInteger();
            if (version != 1) {
                throw new IOException("Version must be 1");
            }
            byte[] privData = data.getOctetString();
            ArrayUtil.reverse(privData);
            this.arrayS = privData;
            while (data.available() != 0) {
                DerValue value = data.getDerValue();
                if (value.isContextSpecific((byte)0) || value.isContextSpecific((byte)1)) continue;
                throw new InvalidKeyException("Unexpected value: " + String.valueOf(value));
            }
            AlgorithmParameters algParams = this.algid.getParameters();
            if (algParams == null) {
                throw new InvalidKeyException("EC domain parameters must be encoded in the algorithm identifier");
            }
            this.params = algParams.getParameterSpec(ECParameterSpec.class);
        }
        catch (IOException | InvalidParameterSpecException e) {
            throw new InvalidKeyException("Invalid EC private key", e);
        }
    }

    @Override
    public PublicKey calculatePublicKey() {
        ECParameterSpec ecParams = this.getParams();
        ECOperations ops = ECOperations.forParameters(ecParams).orElseThrow(ProviderException::new);
        MutablePoint pub = ops.multiply(ecParams.getGenerator(), this.getArrayS0());
        AffinePoint affPub = pub.asAffine();
        ECPoint w = new ECPoint(affPub.getX().asBigInteger(), affPub.getY().asBigInteger());
        try {
            return new ECPublicKeyImpl(w, ecParams);
        }
        catch (InvalidKeyException e) {
            throw new ProviderException("Unexpected error calculating public key", e);
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException("ECPrivateKeyImpl keys are not directly deserializable");
    }
}

