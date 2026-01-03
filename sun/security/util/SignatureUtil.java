/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.interfaces.EdECKey;
import java.security.interfaces.EdECPrivateKey;
import java.security.interfaces.RSAKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Locale;
import jdk.internal.access.SharedSecrets;
import sun.security.rsa.RSAUtil;
import sun.security.util.DerValue;
import sun.security.util.KeyUtil;
import sun.security.util.KnownOIDs;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;

public class SignatureUtil {
    private static String checkName(String algName) {
        if ((algName = algName.toUpperCase(Locale.ENGLISH)).contains(".")) {
            KnownOIDs ko;
            if (algName.startsWith("OID.")) {
                algName = algName.substring(4);
            }
            if ((ko = KnownOIDs.findMatch(algName)) != null) {
                return ko.stdName().toUpperCase(Locale.ENGLISH);
            }
        }
        return algName;
    }

    private static AlgorithmParameters createAlgorithmParameters(String algName, byte[] paramBytes) throws ProviderException {
        try {
            AlgorithmParameters result = AlgorithmParameters.getInstance(algName);
            result.init(paramBytes);
            return result;
        }
        catch (IOException | NoSuchAlgorithmException e) {
            throw new ProviderException(e);
        }
    }

    public static AlgorithmParameterSpec getParamSpec(String sigName, AlgorithmParameters params) throws ProviderException {
        AlgorithmParameterSpec paramSpec = null;
        if (params != null) {
            sigName = SignatureUtil.checkName(sigName);
            if (params.getAlgorithm().contains(".")) {
                try {
                    params = SignatureUtil.createAlgorithmParameters(sigName, params.getEncoded());
                }
                catch (IOException e) {
                    throw new ProviderException(e);
                }
            }
            if (sigName.contains("RSA")) {
                paramSpec = RSAUtil.getParamSpec(params);
            } else if (sigName.contains("ECDSA")) {
                try {
                    paramSpec = params.getParameterSpec(ECParameterSpec.class);
                }
                catch (Exception e) {
                    throw new ProviderException("Error handling EC parameters", e);
                }
            } else {
                throw new ProviderException("Unrecognized algorithm for signature parameters " + sigName);
            }
        }
        return paramSpec;
    }

    public static AlgorithmParameterSpec getParamSpec(String sigName, byte[] paramBytes) throws ProviderException {
        AlgorithmParameterSpec paramSpec = null;
        if (paramBytes != null) {
            if ((sigName = SignatureUtil.checkName(sigName)).contains("RSA")) {
                AlgorithmParameters params = SignatureUtil.createAlgorithmParameters(sigName, paramBytes);
                paramSpec = RSAUtil.getParamSpec(params);
            } else {
                if (sigName.contains("ECDSA")) {
                    return null;
                }
                throw new ProviderException("Unrecognized algorithm for signature parameters " + sigName);
            }
        }
        return paramSpec;
    }

    public static void initVerifyWithParam(Signature s, PublicKey key, AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException, InvalidKeyException {
        SharedSecrets.getJavaSecuritySignatureAccess().initVerify(s, key, params);
    }

    public static void initVerifyWithParam(Signature s, Certificate cert, AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException, InvalidKeyException {
        SharedSecrets.getJavaSecuritySignatureAccess().initVerify(s, cert, params);
    }

    public static void initSignWithParam(Signature s, PrivateKey key, AlgorithmParameterSpec params, SecureRandom sr) throws InvalidAlgorithmParameterException, InvalidKeyException {
        SharedSecrets.getJavaSecuritySignatureAccess().initSign(s, key, params, sr);
    }

    public static AlgorithmId getDigestAlgInPkcs7SignerInfo(Signature signer, String sigalg, PrivateKey privateKey, boolean directsign) throws NoSuchAlgorithmException {
        AlgorithmId digAlgID;
        String kAlg = privateKey.getAlgorithm();
        if (privateKey instanceof EdECPrivateKey || kAlg.equalsIgnoreCase("Ed25519") || kAlg.equalsIgnoreCase("Ed448")) {
            if (privateKey instanceof EdECPrivateKey) {
                kAlg = ((EdECPrivateKey)privateKey).getParams().getName();
            }
            switch (kAlg.toUpperCase(Locale.ENGLISH)) {
                case "ED25519": {
                    digAlgID = EdDSADigestAlgHolder.sha512;
                    break;
                }
                case "ED448": {
                    if (directsign) {
                        digAlgID = EdDSADigestAlgHolder.shake256;
                        break;
                    }
                    digAlgID = EdDSADigestAlgHolder.shake256$512;
                    break;
                }
                default: {
                    throw new AssertionError((Object)("Unknown curve name: " + kAlg));
                }
            }
        } else if (sigalg.equalsIgnoreCase("RSASSA-PSS")) {
            try {
                digAlgID = AlgorithmId.get(signer.getParameters().getParameterSpec(PSSParameterSpec.class).getDigestAlgorithm());
            }
            catch (InvalidParameterSpecException e) {
                throw new AssertionError("Should not happen", e);
            }
        } else {
            digAlgID = AlgorithmId.get(SignatureUtil.extractDigestAlgFromDwithE(sigalg));
        }
        return digAlgID;
    }

    public static String extractDigestAlgFromDwithE(String signatureAlgorithm) {
        int with = (signatureAlgorithm = signatureAlgorithm.toUpperCase(Locale.ENGLISH)).indexOf("WITH");
        if (with > 0) {
            return signatureAlgorithm.substring(0, with);
        }
        throw new IllegalArgumentException("Unknown algorithm: " + signatureAlgorithm);
    }

    public static String extractKeyAlgFromDwithE(String signatureAlgorithm) {
        int and;
        signatureAlgorithm = signatureAlgorithm.toUpperCase(Locale.ENGLISH);
        int with = signatureAlgorithm.indexOf("WITH");
        String keyAlgorithm = null;
        if (with > 0 && (keyAlgorithm = (and = signatureAlgorithm.indexOf("AND", with + 4)) > 0 ? signatureAlgorithm.substring(with + 4, and) : signatureAlgorithm.substring(with + 4)).equalsIgnoreCase("ECDSA")) {
            keyAlgorithm = "EC";
        }
        return keyAlgorithm;
    }

    public static AlgorithmParameterSpec getDefaultParamSpec(String sigAlg, Key k) {
        if ((sigAlg = SignatureUtil.checkName(sigAlg)).equals("RSASSA-PSS")) {
            AlgorithmParameterSpec spec;
            if (k instanceof RSAKey && (spec = ((RSAKey)((Object)k)).getParams()) instanceof PSSParameterSpec) {
                return spec;
            }
            switch (SignatureUtil.ifcFfcStrength(KeyUtil.getKeySize(k))) {
                case "SHA256": {
                    return PSSParamsHolder.PSS_256_SPEC;
                }
                case "SHA384": {
                    return PSSParamsHolder.PSS_384_SPEC;
                }
                case "SHA512": {
                    return PSSParamsHolder.PSS_512_SPEC;
                }
            }
            throw new AssertionError((Object)"Should not happen");
        }
        return null;
    }

    public static Signature fromKey(String sigAlg, PrivateKey key, String provider) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        Signature sigEngine = provider == null || provider.isEmpty() ? Signature.getInstance(sigAlg) : Signature.getInstance(sigAlg, provider);
        return SignatureUtil.autoInitInternal(sigAlg, key, sigEngine);
    }

    public static Signature fromKey(String sigAlg, PrivateKey key, Provider provider) throws NoSuchAlgorithmException, InvalidKeyException {
        Signature sigEngine = provider == null ? Signature.getInstance(sigAlg) : Signature.getInstance(sigAlg, provider);
        return SignatureUtil.autoInitInternal(sigAlg, key, sigEngine);
    }

    private static Signature autoInitInternal(String alg, PrivateKey key, Signature s) throws InvalidKeyException {
        AlgorithmParameterSpec params = SignatureUtil.getDefaultParamSpec(alg, key);
        try {
            SignatureUtil.initSignWithParam(s, key, params, null);
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new AssertionError("Should not happen", e);
        }
        return s;
    }

    public static AlgorithmId fromSignature(Signature sigEngine, PrivateKey key) throws SignatureException {
        try {
            if (key instanceof EdECKey) {
                return AlgorithmId.get(((EdECKey)((Object)key)).getParams().getName());
            }
            AlgorithmParameters params = null;
            try {
                params = sigEngine.getParameters();
            }
            catch (UnsupportedOperationException unsupportedOperationException) {
                // empty catch block
            }
            if (params != null) {
                return AlgorithmId.get(sigEngine.getParameters());
            }
            String sigAlg = sigEngine.getAlgorithm();
            if (sigAlg.equalsIgnoreCase("EdDSA")) {
                sigAlg = key.getAlgorithm();
            }
            return AlgorithmId.get(sigAlg);
        }
        catch (NoSuchAlgorithmException e) {
            throw new SignatureException("Cannot derive AlgorithmIdentifier", e);
        }
    }

    public static void checkKeyAndSigAlgMatch(PrivateKey key, String sAlg) {
        String kAlg = key.getAlgorithm().toUpperCase(Locale.ENGLISH);
        switch (sAlg = SignatureUtil.checkName(sAlg)) {
            case "RSASSA-PSS": {
                if (kAlg.equals("RSASSA-PSS") || kAlg.equals("RSA")) break;
                throw new IllegalArgumentException("key algorithm not compatible with signature algorithm");
            }
            case "EDDSA": {
                if (kAlg.equals("EDDSA") || kAlg.equals("ED448") || kAlg.equals("ED25519")) break;
                throw new IllegalArgumentException("key algorithm not compatible with signature algorithm");
            }
            case "ED25519": 
            case "ED448": {
                String groupName;
                if (!(key instanceof EdECKey ? !sAlg.equals(groupName = ((EdECKey)((Object)key)).getParams().getName().toUpperCase(Locale.US)) : !kAlg.equals("EDDSA") && !kAlg.equals(sAlg))) break;
                throw new IllegalArgumentException("key algorithm not compatible with signature algorithm");
            }
            default: {
                if (!sAlg.contains("WITH") || !(sAlg.endsWith("WITHRSA") && !kAlg.equals("RSA") || sAlg.endsWith("WITHECDSA") && !kAlg.equals("EC")) && (!sAlg.endsWith("WITHDSA") || kAlg.equals("DSA"))) break;
                throw new IllegalArgumentException("key algorithm not compatible with signature algorithm");
            }
        }
    }

    public static String getDefaultSigAlgForKey(PrivateKey k) {
        String kAlg;
        return switch (kAlg = k.getAlgorithm().toUpperCase(Locale.ENGLISH)) {
            case "DSA" -> "SHA256withDSA";
            case "RSA" -> SignatureUtil.ifcFfcStrength(KeyUtil.getKeySize(k)) + "withRSA";
            case "EC" -> SignatureUtil.ecStrength(KeyUtil.getKeySize(k)) + "withECDSA";
            case "EDDSA" -> {
                if (k instanceof EdECPrivateKey) {
                    yield ((EdECPrivateKey)k).getParams().getName();
                }
                yield kAlg;
            }
            case "RSASSA-PSS", "ED25519", "ED448" -> kAlg;
            default -> null;
        };
    }

    private static String ecStrength(int bitLength) {
        if (bitLength >= 512) {
            return "SHA512";
        }
        return "SHA384";
    }

    private static String ifcFfcStrength(int bitLength) {
        if (bitLength > 7680) {
            return "SHA512";
        }
        return bitLength >= 624 ? "SHA384" : "SHA256";
    }

    public static class EdDSADigestAlgHolder {
        public static final AlgorithmId sha512;
        public static final AlgorithmId shake256;
        public static final AlgorithmId shake256$512;

        static {
            try {
                sha512 = new AlgorithmId(ObjectIdentifier.of(KnownOIDs.SHA_512));
                shake256 = new AlgorithmId(ObjectIdentifier.of(KnownOIDs.SHAKE256));
                shake256$512 = new AlgorithmId(ObjectIdentifier.of(KnownOIDs.SHAKE256_LEN), new DerValue(2, new byte[]{2, 0}));
            }
            catch (IOException e) {
                throw new AssertionError("Should not happen", e);
            }
        }
    }

    private static class PSSParamsHolder {
        static final PSSParameterSpec PSS_256_SPEC = new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1);
        static final PSSParameterSpec PSS_384_SPEC = new PSSParameterSpec("SHA-384", "MGF1", MGF1ParameterSpec.SHA384, 48, 1);
        static final PSSParameterSpec PSS_512_SPEC = new PSSParameterSpec("SHA-512", "MGF1", MGF1ParameterSpec.SHA512, 64, 1);

        private PSSParamsHolder() {
        }
    }
}

