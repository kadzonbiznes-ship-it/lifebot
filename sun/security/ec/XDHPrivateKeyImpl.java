/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.security.InvalidKeyException;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.interfaces.XECPrivateKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.NamedParameterSpec;
import java.util.Optional;
import sun.security.ec.XDHPublicKeyImpl;
import sun.security.ec.XECOperations;
import sun.security.ec.XECParameters;
import sun.security.pkcs.PKCS8Key;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;

public final class XDHPrivateKeyImpl
extends PKCS8Key
implements XECPrivateKey {
    private static final long serialVersionUID = 1L;
    private final NamedParameterSpec paramSpec;
    private byte[] k;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    XDHPrivateKeyImpl(XECParameters params, byte[] k) throws InvalidKeyException {
        this.paramSpec = new NamedParameterSpec(params.getName());
        this.k = (byte[])k.clone();
        this.algid = new AlgorithmId(params.getOid());
        DerValue val = new DerValue(4, k);
        try {
            this.key = val.toByteArray();
        }
        finally {
            val.clear();
        }
        this.checkLength(params);
    }

    XDHPrivateKeyImpl(byte[] encoded) throws InvalidKeyException {
        super(encoded);
        XECParameters params = XECParameters.get(InvalidKeyException::new, this.algid);
        this.paramSpec = new NamedParameterSpec(params.getName());
        try {
            DerInputStream derStream = new DerInputStream(this.key);
            this.k = derStream.getOctetString();
        }
        catch (IOException ex) {
            throw new InvalidKeyException(ex);
        }
        this.checkLength(params);
    }

    void checkLength(XECParameters params) throws InvalidKeyException {
        if (params.getBytes() != this.k.length) {
            throw new InvalidKeyException("key length must be " + params.getBytes());
        }
    }

    public byte[] getK() {
        return (byte[])this.k.clone();
    }

    @Override
    public String getAlgorithm() {
        return "XDH";
    }

    @Override
    public AlgorithmParameterSpec getParams() {
        return this.paramSpec;
    }

    @Override
    public Optional<byte[]> getScalar() {
        return Optional.of(this.getK());
    }

    @Override
    public PublicKey calculatePublicKey() {
        XECParameters params = this.paramSpec.getName().equals("X25519") ? XECParameters.X25519 : XECParameters.X448;
        try {
            return new XDHPublicKeyImpl(params, new XECOperations(params).computePublic((byte[])this.k.clone()));
        }
        catch (InvalidKeyException e) {
            throw new ProviderException("Unexpected error calculating public key", e);
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException("XDHPrivateKeyImpl keys are not directly deserializable");
    }
}

