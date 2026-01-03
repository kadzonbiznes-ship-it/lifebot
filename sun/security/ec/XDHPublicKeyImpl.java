/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ec;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyRep;
import java.security.interfaces.XECPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;
import sun.security.ec.XECParameters;
import sun.security.util.BitArray;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X509Key;

public final class XDHPublicKeyImpl
extends X509Key
implements XECPublicKey {
    private static final long serialVersionUID = 1L;
    private final BigInteger u;
    private final NamedParameterSpec paramSpec;

    XDHPublicKeyImpl(XECParameters params, BigInteger u) throws InvalidKeyException {
        this.paramSpec = new NamedParameterSpec(params.getName());
        this.algid = new AlgorithmId(params.getOid());
        this.u = u.mod(params.getP());
        byte[] u_arr = this.u.toByteArray();
        XDHPublicKeyImpl.reverse(u_arr);
        u_arr = Arrays.copyOf(u_arr, params.getBytes());
        this.setKey(new BitArray(u_arr.length * 8, u_arr));
        this.checkLength(params);
    }

    XDHPublicKeyImpl(byte[] encoded) throws InvalidKeyException {
        this.decode(encoded);
        XECParameters params = XECParameters.get(InvalidKeyException::new, this.algid);
        this.paramSpec = new NamedParameterSpec(params.getName());
        byte[] u_arr = this.getKey().toByteArray();
        XDHPublicKeyImpl.reverse(u_arr);
        int bitsMod8 = params.getBits() % 8;
        if (bitsMod8 != 0) {
            byte mask = (byte)((1 << bitsMod8) - 1);
            u_arr[0] = (byte)(u_arr[0] & mask);
        }
        this.u = new BigInteger(1, u_arr);
        this.checkLength(params);
    }

    void checkLength(XECParameters params) throws InvalidKeyException {
        if (params.getBytes() * 8 != this.getKey().length()) {
            throw new InvalidKeyException("key length must be " + params.getBytes());
        }
    }

    @Override
    public BigInteger getU() {
        return this.u;
    }

    @Override
    public AlgorithmParameterSpec getParams() {
        return this.paramSpec;
    }

    @Override
    public String getAlgorithm() {
        return "XDH";
    }

    private Object writeReplace() throws ObjectStreamException {
        return new KeyRep(KeyRep.Type.PUBLIC, this.getAlgorithm(), this.getFormat(), this.getEncoded());
    }

    private static void swap(byte[] arr, int i, int j) {
        byte tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    private static void reverse(byte[] arr) {
        int i = 0;
        for (int j = arr.length - 1; i < j; ++i, --j) {
            XDHPublicKeyImpl.swap(arr, i, j);
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException("XDHPublicKeyImpl keys are not directly deserializable");
    }
}

