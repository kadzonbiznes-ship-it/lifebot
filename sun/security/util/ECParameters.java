/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.AlgorithmParametersSpi;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import sun.security.util.CurveDB;
import sun.security.util.DerValue;
import sun.security.util.ECKeySizeParameterSpec;
import sun.security.util.NamedCurve;
import sun.security.util.ObjectIdentifier;

public final class ECParameters
extends AlgorithmParametersSpi {
    private NamedCurve namedCurve;

    public static AlgorithmParameters getAlgorithmParameters(ECParameterSpec spec) throws InvalidKeyException {
        try {
            AlgorithmParameters params = AlgorithmParameters.getInstance("EC", "SunEC");
            params.init(spec);
            return params;
        }
        catch (GeneralSecurityException e) {
            throw new InvalidKeyException("EC parameters error", e);
        }
    }

    @Override
    protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
        if (paramSpec == null) {
            throw new InvalidParameterSpecException("paramSpec must not be null");
        }
        if (paramSpec instanceof NamedCurve) {
            this.namedCurve = (NamedCurve)paramSpec;
            return;
        }
        if (paramSpec instanceof ECParameterSpec) {
            this.namedCurve = CurveDB.lookup((ECParameterSpec)paramSpec);
        } else if (paramSpec instanceof ECGenParameterSpec) {
            String name = ((ECGenParameterSpec)paramSpec).getName();
            this.namedCurve = CurveDB.lookup(name);
        } else if (paramSpec instanceof ECKeySizeParameterSpec) {
            int keySize = ((ECKeySizeParameterSpec)paramSpec).getKeySize();
            this.namedCurve = CurveDB.lookup(keySize);
        } else {
            throw new InvalidParameterSpecException("Only ECParameterSpec, ECGenParameterSpec and ECKeySizeParameterSpec supported");
        }
        if (this.namedCurve == null) {
            throw new InvalidParameterSpecException("Not a supported curve: " + paramSpec);
        }
    }

    @Override
    protected void engineInit(byte[] params) throws IOException {
        DerValue encodedParams = new DerValue(params);
        if (encodedParams.tag == 6) {
            ObjectIdentifier oid = encodedParams.getOID();
            NamedCurve spec = CurveDB.lookup(oid.toString());
            if (spec == null) {
                throw new IOException("Unknown named curve: " + oid);
            }
            this.namedCurve = spec;
            return;
        }
        throw new IOException("Only named ECParameters supported");
    }

    @Override
    protected void engineInit(byte[] params, String decodingMethod) throws IOException {
        this.engineInit(params);
    }

    @Override
    protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> spec) throws InvalidParameterSpecException {
        if (spec.isAssignableFrom(ECParameterSpec.class)) {
            return (T)((AlgorithmParameterSpec)spec.cast(this.namedCurve));
        }
        if (spec.isAssignableFrom(ECGenParameterSpec.class)) {
            String name = this.namedCurve.getObjectId();
            return (T)((AlgorithmParameterSpec)spec.cast(new ECGenParameterSpec(name)));
        }
        if (spec.isAssignableFrom(ECKeySizeParameterSpec.class)) {
            int keySize = this.namedCurve.getCurve().getField().getFieldSize();
            return (T)((AlgorithmParameterSpec)spec.cast(new ECKeySizeParameterSpec(keySize)));
        }
        throw new InvalidParameterSpecException("Only ECParameterSpec, ECGenParameterSpec and ECKeySizeParameterSpec supported");
    }

    @Override
    protected byte[] engineGetEncoded() throws IOException {
        return this.namedCurve.getEncoded();
    }

    @Override
    protected byte[] engineGetEncoded(String encodingMethod) throws IOException {
        return this.engineGetEncoded();
    }

    @Override
    protected String engineToString() {
        if (this.namedCurve == null) {
            return "Not initialized";
        }
        return this.namedCurve.toString();
    }
}

