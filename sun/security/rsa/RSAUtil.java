/*
 * Decompiled with CFR 0.152.
 */
package sun.security.rsa;

import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.PSSParameterSpec;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;

public class RSAUtil {
    private static void requireNonNull(Object obj, String msg) {
        if (obj == null) {
            throw new ProviderException(msg);
        }
    }

    public static AlgorithmParameterSpec checkParamsAgainstType(KeyType type, AlgorithmParameterSpec paramSpec) throws ProviderException {
        if (paramSpec == null) {
            return null;
        }
        Class<? extends AlgorithmParameterSpec> expCls = type.paramSpecCls;
        if (expCls == null) {
            throw new ProviderException("null params expected for " + type.keyAlgo);
        }
        if (!expCls.isInstance(paramSpec)) {
            throw new ProviderException(expCls + " expected for " + type.keyAlgo);
        }
        return paramSpec;
    }

    public static AlgorithmParameters getParams(KeyType type, AlgorithmParameterSpec spec) throws ProviderException {
        if (spec == null) {
            return null;
        }
        try {
            AlgorithmParameters params = AlgorithmParameters.getInstance(type.keyAlgo);
            params.init(spec);
            return params;
        }
        catch (NoSuchAlgorithmException | InvalidParameterSpecException ex) {
            throw new ProviderException(ex);
        }
    }

    public static AlgorithmId createAlgorithmId(KeyType type, AlgorithmParameterSpec paramSpec) throws ProviderException {
        RSAUtil.checkParamsAgainstType(type, paramSpec);
        ObjectIdentifier oid = type.oid;
        AlgorithmParameters params = RSAUtil.getParams(type, paramSpec);
        return new AlgorithmId(oid, params);
    }

    public static AlgorithmParameterSpec getParamSpec(AlgorithmParameters params) throws ProviderException {
        if (params == null) {
            return null;
        }
        String algName = params.getAlgorithm();
        KeyType type = KeyType.lookup(algName);
        Class<? extends AlgorithmParameterSpec> specCls = type.paramSpecCls;
        if (specCls == null) {
            throw new ProviderException("No params accepted for " + type.keyAlgo);
        }
        try {
            return params.getParameterSpec(specCls);
        }
        catch (InvalidParameterSpecException ex) {
            throw new ProviderException(ex);
        }
    }

    public static Object[] getTypeAndParamSpec(AlgorithmId algid) throws ProviderException {
        RSAUtil.requireNonNull(algid, "AlgorithmId should not be null");
        Object[] result = new Object[2];
        String algName = algid.getName();
        try {
            result[0] = KeyType.lookup(algName);
        }
        catch (ProviderException pe) {
            if (algName.contains("RSA")) {
                result[0] = KeyType.RSA;
            }
            throw pe;
        }
        result[1] = RSAUtil.getParamSpec(algid.getParameters());
        return result;
    }

    public static byte[] encodeSignature(ObjectIdentifier oid, byte[] digest) {
        DerOutputStream out = new DerOutputStream();
        new AlgorithmId(oid).encode(out);
        out.putOctetString(digest);
        DerValue result = new DerValue(48, out.toByteArray());
        return result.toByteArray();
    }

    static byte[] encodeSignatureWithoutNULL(ObjectIdentifier oid, byte[] digest) {
        DerOutputStream out = new DerOutputStream();
        out.write((byte)48, new DerOutputStream().putOID(oid));
        out.putOctetString(digest);
        DerValue result = new DerValue(48, out.toByteArray());
        return result.toByteArray();
    }

    public static enum KeyType {
        RSA("RSA", AlgorithmId.RSAEncryption_oid, null),
        PSS("RSASSA-PSS", AlgorithmId.RSASSA_PSS_oid, PSSParameterSpec.class);

        final String keyAlgo;
        final ObjectIdentifier oid;
        final Class<? extends AlgorithmParameterSpec> paramSpecCls;

        private KeyType(String keyAlgo, ObjectIdentifier oid, Class<? extends AlgorithmParameterSpec> paramSpecCls) {
            this.keyAlgo = keyAlgo;
            this.oid = oid;
            this.paramSpecCls = paramSpecCls;
        }

        public static KeyType lookup(String name) throws ProviderException {
            RSAUtil.requireNonNull(name, "Key algorithm should not be null");
            if (name.contains("PSS")) {
                return PSS;
            }
            if (name.contains("RSA")) {
                return RSA;
            }
            throw new ProviderException("Unsupported algorithm " + name);
        }
    }
}

