/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.KeyRep;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.InvalidParameterSpecException;
import sun.security.util.ECParameters;
import sun.security.util.ECUtil;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X509Key;

public final class ECPublicKeyImpl
extends X509Key
implements ECPublicKey {
    private static final long serialVersionUID = -2462037275160462289L;
    private ECPoint w;
    private ECParameterSpec params;

    ECPublicKeyImpl(ECPoint w, ECParameterSpec params) throws InvalidKeyException {
        this.w = w;
        this.params = params;
        this.algid = new AlgorithmId(AlgorithmId.EC_oid, ECParameters.getAlgorithmParameters(params));
        this.key = ECUtil.encodePoint(w, params.getCurve());
    }

    ECPublicKeyImpl(byte[] encoded) throws InvalidKeyException {
        this.decode(encoded);
    }

    @Override
    public String getAlgorithm() {
        return "EC";
    }

    @Override
    public ECPoint getW() {
        return this.w;
    }

    @Override
    public ECParameterSpec getParams() {
        return this.params;
    }

    public byte[] getEncodedPublicValue() {
        return (byte[])this.key.clone();
    }

    @Override
    protected void parseKeyBits() throws InvalidKeyException {
        AlgorithmParameters algParams = this.algid.getParameters();
        if (algParams == null) {
            throw new InvalidKeyException("EC domain parameters must be encoded in the algorithm identifier");
        }
        try {
            this.params = algParams.getParameterSpec(ECParameterSpec.class);
            this.w = ECUtil.decodePoint(this.key, this.params.getCurve());
        }
        catch (IOException | InvalidParameterSpecException e) {
            throw new InvalidKeyException("Invalid EC key", e);
        }
    }

    @Override
    public String toString() {
        return "Sun EC public key, " + this.params.getCurve().getField().getFieldSize() + " bits\n  public x coord: " + String.valueOf(this.w.getAffineX()) + "\n  public y coord: " + String.valueOf(this.w.getAffineY()) + "\n  parameters: " + String.valueOf(this.params);
    }

    private Object writeReplace() throws ObjectStreamException {
        return new KeyRep(KeyRep.Type.PUBLIC, this.getAlgorithm(), this.getFormat(), this.getEncoded());
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException("ECPublicKeyImpl keys are not directly deserializable");
    }
}

