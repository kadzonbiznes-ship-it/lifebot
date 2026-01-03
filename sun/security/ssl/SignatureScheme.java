/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.security.AlgorithmParameters;
import java.security.CryptoPrimitive;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.JsseJce;
import sun.security.ssl.NamedGroup;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.SSLAlgorithmConstraints;
import sun.security.ssl.SSLConfiguration;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLScope;
import sun.security.ssl.Utilities;
import sun.security.ssl.X509Authentication;
import sun.security.util.KeyUtil;
import sun.security.util.SignatureUtil;

enum SignatureScheme {
    ECDSA_SECP256R1_SHA256(1027, "ecdsa_secp256r1_sha256", "SHA256withECDSA", "EC", NamedGroup.SECP256_R1, ProtocolVersion.PROTOCOLS_TO_13),
    ECDSA_SECP384R1_SHA384(1283, "ecdsa_secp384r1_sha384", "SHA384withECDSA", "EC", NamedGroup.SECP384_R1, ProtocolVersion.PROTOCOLS_TO_13),
    ECDSA_SECP521R1_SHA512(1539, "ecdsa_secp521r1_sha512", "SHA512withECDSA", "EC", NamedGroup.SECP521_R1, ProtocolVersion.PROTOCOLS_TO_13),
    ED25519(2055, "ed25519", "Ed25519", "EdDSA", ProtocolVersion.PROTOCOLS_12_13),
    ED448(2056, "ed448", "Ed448", "EdDSA", ProtocolVersion.PROTOCOLS_12_13),
    RSA_PSS_RSAE_SHA256(2052, "rsa_pss_rsae_sha256", "RSASSA-PSS", "RSA", SigAlgParamSpec.RSA_PSS_SHA256, 528, ProtocolVersion.PROTOCOLS_12_13),
    RSA_PSS_RSAE_SHA384(2053, "rsa_pss_rsae_sha384", "RSASSA-PSS", "RSA", SigAlgParamSpec.RSA_PSS_SHA384, 784, ProtocolVersion.PROTOCOLS_12_13),
    RSA_PSS_RSAE_SHA512(2054, "rsa_pss_rsae_sha512", "RSASSA-PSS", "RSA", SigAlgParamSpec.RSA_PSS_SHA512, 1040, ProtocolVersion.PROTOCOLS_12_13),
    RSA_PSS_PSS_SHA256(2057, "rsa_pss_pss_sha256", "RSASSA-PSS", "RSASSA-PSS", SigAlgParamSpec.RSA_PSS_SHA256, 528, ProtocolVersion.PROTOCOLS_12_13),
    RSA_PSS_PSS_SHA384(2058, "rsa_pss_pss_sha384", "RSASSA-PSS", "RSASSA-PSS", SigAlgParamSpec.RSA_PSS_SHA384, 784, ProtocolVersion.PROTOCOLS_12_13),
    RSA_PSS_PSS_SHA512(2059, "rsa_pss_pss_sha512", "RSASSA-PSS", "RSASSA-PSS", SigAlgParamSpec.RSA_PSS_SHA512, 1040, ProtocolVersion.PROTOCOLS_12_13),
    RSA_PKCS1_SHA256(1025, "rsa_pkcs1_sha256", "SHA256withRSA", "RSA", null, null, 511, ProtocolVersion.PROTOCOLS_TO_13, ProtocolVersion.PROTOCOLS_TO_12),
    RSA_PKCS1_SHA384(1281, "rsa_pkcs1_sha384", "SHA384withRSA", "RSA", null, null, 768, ProtocolVersion.PROTOCOLS_TO_13, ProtocolVersion.PROTOCOLS_TO_12),
    RSA_PKCS1_SHA512(1537, "rsa_pkcs1_sha512", "SHA512withRSA", "RSA", null, null, 768, ProtocolVersion.PROTOCOLS_TO_13, ProtocolVersion.PROTOCOLS_TO_12),
    DSA_SHA256(1026, "dsa_sha256", "SHA256withDSA", "DSA", ProtocolVersion.PROTOCOLS_TO_12),
    ECDSA_SHA224(771, "ecdsa_sha224", "SHA224withECDSA", "EC", ProtocolVersion.PROTOCOLS_TO_12),
    RSA_SHA224(769, "rsa_sha224", "SHA224withRSA", "RSA", 511, ProtocolVersion.PROTOCOLS_TO_12),
    DSA_SHA224(770, "dsa_sha224", "SHA224withDSA", "DSA", ProtocolVersion.PROTOCOLS_TO_12),
    ECDSA_SHA1(515, "ecdsa_sha1", "SHA1withECDSA", "EC", ProtocolVersion.PROTOCOLS_TO_13),
    RSA_PKCS1_SHA1(513, "rsa_pkcs1_sha1", "SHA1withRSA", "RSA", null, null, 511, ProtocolVersion.PROTOCOLS_TO_13, ProtocolVersion.PROTOCOLS_TO_12),
    DSA_SHA1(514, "dsa_sha1", "SHA1withDSA", "DSA", ProtocolVersion.PROTOCOLS_TO_12),
    RSA_MD5(257, "rsa_md5", "MD5withRSA", "RSA", 511, ProtocolVersion.PROTOCOLS_TO_12);

    final int id;
    final String name;
    private final String algorithm;
    final String keyAlgorithm;
    private final SigAlgParamSpec signAlgParams;
    private final NamedGroup namedGroup;
    final int minimalKeySize;
    final List<ProtocolVersion> supportedProtocols;
    final List<ProtocolVersion> handshakeSupportedProtocols;
    final boolean isAvailable;
    private static final String[] hashAlgorithms;
    private static final String[] signatureAlgorithms;
    static final Set<SSLScope> HANDSHAKE_SCOPE;
    static final Set<SSLScope> CERTIFICATE_SCOPE;
    private static final Set<CryptoPrimitive> SIGNATURE_PRIMITIVE_SET;

    private SignatureScheme(int id, String name, String algorithm, String keyAlgorithm, ProtocolVersion[] supportedProtocols) {
        this(id, name, algorithm, keyAlgorithm, -1, supportedProtocols);
    }

    private SignatureScheme(int id, String name, String algorithm, String keyAlgorithm, int minimalKeySize, ProtocolVersion[] supportedProtocols) {
        this(id, name, algorithm, keyAlgorithm, null, minimalKeySize, supportedProtocols);
    }

    private SignatureScheme(int id, String name, String algorithm, String keyAlgorithm, SigAlgParamSpec signAlgParamSpec, int minimalKeySize, ProtocolVersion[] supportedProtocols) {
        this(id, name, algorithm, keyAlgorithm, signAlgParamSpec, null, minimalKeySize, supportedProtocols, supportedProtocols);
    }

    private SignatureScheme(int id, String name, String algorithm, String keyAlgorithm, NamedGroup namedGroup, ProtocolVersion[] supportedProtocols) {
        this(id, name, algorithm, keyAlgorithm, null, namedGroup, -1, supportedProtocols, supportedProtocols);
    }

    private SignatureScheme(int id, String name, String algorithm, String keyAlgorithm, SigAlgParamSpec signAlgParams, NamedGroup namedGroup, int minimalKeySize, ProtocolVersion[] supportedProtocols, ProtocolVersion[] handshakeSupportedProtocols) {
        boolean mediator;
        block7: {
            this.id = id;
            this.name = name;
            this.algorithm = algorithm;
            this.keyAlgorithm = keyAlgorithm;
            this.signAlgParams = signAlgParams;
            this.namedGroup = namedGroup;
            this.minimalKeySize = minimalKeySize;
            this.supportedProtocols = Arrays.asList(supportedProtocols);
            this.handshakeSupportedProtocols = Arrays.asList(handshakeSupportedProtocols);
            mediator = true;
            if ("EC".equals(keyAlgorithm)) {
                mediator = JsseJce.isEcAvailable();
            }
            if (mediator) {
                if (signAlgParams != null) {
                    mediator = signAlgParams.isAvailable;
                } else {
                    try {
                        Signature.getInstance(algorithm);
                    }
                    catch (Exception e) {
                        mediator = false;
                        if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) break block7;
                        SSLLogger.warning("Signature algorithm, " + algorithm + ", is not supported by the underlying providers", new Object[0]);
                    }
                }
            }
        }
        if (mediator && (id >> 8 & 0xFF) == 3 && Security.getProvider("SunMSCAPI") != null) {
            mediator = false;
        }
        this.isAvailable = mediator;
    }

    static SignatureScheme valueOf(int id) {
        for (SignatureScheme ss : SignatureScheme.values()) {
            if (ss.id != id) continue;
            return ss;
        }
        return null;
    }

    static String nameOf(int id) {
        for (SignatureScheme ss : SignatureScheme.values()) {
            if (ss.id != id) continue;
            return ss.name;
        }
        int hashId = id >> 8 & 0xFF;
        int signId = id & 0xFF;
        String hashName = hashId >= hashAlgorithms.length ? "UNDEFINED-HASH(" + hashId + ")" : hashAlgorithms[hashId];
        String signName = signId >= signatureAlgorithms.length ? "UNDEFINED-SIGNATURE(" + signId + ")" : signatureAlgorithms[signId];
        return signName + "_" + hashName;
    }

    static SignatureScheme nameOf(String signatureSchemeName) {
        for (SignatureScheme ss : SignatureScheme.values()) {
            if (!ss.name.equalsIgnoreCase(signatureSchemeName)) continue;
            return ss;
        }
        return null;
    }

    static int sizeInRecord() {
        return 2;
    }

    private boolean isPermitted(SSLAlgorithmConstraints constraints, Set<SSLScope> scopes) {
        return constraints.permits(this.name, scopes) && constraints.permits(this.keyAlgorithm, scopes) && constraints.permits(this.algorithm, scopes) && constraints.permits(SIGNATURE_PRIMITIVE_SET, this.name, null) && constraints.permits(SIGNATURE_PRIMITIVE_SET, this.keyAlgorithm, null) && constraints.permits(SIGNATURE_PRIMITIVE_SET, this.algorithm, this.signAlgParams != null ? this.signAlgParams.parameters : null) && (this.namedGroup == null || this.namedGroup.isPermitted(constraints));
    }

    static void updateHandshakeLocalSupportedAlgs(HandshakeContext hc) {
        if (hc.localSupportedSignAlgs != null && hc.localSupportedCertSignAlgs != null && (hc.negotiatedProtocol == null || hc.activeProtocols.size() == 1)) {
            return;
        }
        List<ProtocolVersion> protocols = hc.negotiatedProtocol != null ? List.of(hc.negotiatedProtocol) : hc.activeProtocols;
        hc.localSupportedSignAlgs = SignatureScheme.getSupportedAlgorithms(hc.sslConfig, hc.algorithmConstraints, protocols, HANDSHAKE_SCOPE);
        hc.localSupportedCertSignAlgs = SignatureScheme.getSupportedAlgorithms(hc.sslConfig, hc.algorithmConstraints, protocols, CERTIFICATE_SCOPE);
    }

    private static List<SignatureScheme> getSupportedAlgorithms(SSLConfiguration config, SSLAlgorithmConstraints constraints, List<ProtocolVersion> activeProtocols, Set<SSLScope> scopes) {
        LinkedList<SignatureScheme> supported = new LinkedList<SignatureScheme>();
        List<SignatureScheme> schemesToCheck = config.signatureSchemes == null ? Arrays.asList(SignatureScheme.values()) : SignatureScheme.namesOfAvailable(config.signatureSchemes);
        for (SignatureScheme ss : schemesToCheck) {
            if (!ss.isAvailable) {
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake,verbose")) continue;
                SSLLogger.finest("Ignore unsupported signature scheme: " + ss.name, new Object[0]);
                continue;
            }
            boolean isMatch = false;
            for (ProtocolVersion pv : activeProtocols) {
                if (!ss.isSupportedProtocol(pv, scopes)) continue;
                isMatch = true;
                break;
            }
            if (isMatch) {
                if (ss.isPermitted(constraints, scopes)) {
                    supported.add(ss);
                    continue;
                }
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake,verbose")) continue;
                SSLLogger.finest("Ignore disabled signature scheme: " + ss.name, new Object[0]);
                continue;
            }
            if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake,verbose")) continue;
            SSLLogger.finest("Ignore inactive signature scheme: " + ss.name, new Object[0]);
        }
        return supported;
    }

    static List<SignatureScheme> getSupportedAlgorithms(SSLConfiguration config, SSLAlgorithmConstraints constraints, ProtocolVersion protocolVersion, int[] algorithmIds, Set<SSLScope> scopes) {
        LinkedList<SignatureScheme> supported = new LinkedList<SignatureScheme>();
        for (int ssid : algorithmIds) {
            SignatureScheme ss = SignatureScheme.valueOf(ssid);
            if (ss == null) {
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
                SSLLogger.warning("Unsupported signature scheme: " + SignatureScheme.nameOf(ssid), new Object[0]);
                continue;
            }
            if ((config.signatureSchemes == null || Utilities.contains(config.signatureSchemes, ss.name)) && ss.isAllowed(constraints, protocolVersion, scopes)) {
                supported.add(ss);
                continue;
            }
            if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
            SSLLogger.warning("Unsupported signature scheme: " + ss.name, new Object[0]);
        }
        return supported;
    }

    static SignatureScheme getPreferableAlgorithm(SSLAlgorithmConstraints constraints, List<SignatureScheme> schemes, String keyAlgorithm, ProtocolVersion version) {
        for (SignatureScheme ss : schemes) {
            if (!keyAlgorithm.equalsIgnoreCase(ss.keyAlgorithm) || !ss.isAllowed(constraints, version, HANDSHAKE_SCOPE)) continue;
            return ss;
        }
        return null;
    }

    static Map.Entry<SignatureScheme, Signature> getSignerOfPreferableAlgorithm(SSLConfiguration sslConfig, SSLAlgorithmConstraints constraints, List<SignatureScheme> schemes, X509Authentication.X509Possession x509Possession, ProtocolVersion version) {
        PrivateKey signingKey = x509Possession.popPrivateKey;
        String keyAlgorithm = signingKey.getAlgorithm();
        int keySize = keyAlgorithm.equalsIgnoreCase("RSA") || keyAlgorithm.equalsIgnoreCase("RSASSA-PSS") ? KeyUtil.getKeySize(signingKey) : Integer.MAX_VALUE;
        for (SignatureScheme ss : schemes) {
            ECParameterSpec params;
            if (keySize < ss.minimalKeySize || !keyAlgorithm.equalsIgnoreCase(ss.keyAlgorithm) || !ss.isAllowed(constraints, version, HANDSHAKE_SCOPE)) continue;
            if (ss.namedGroup != null && ss.namedGroup.spec == NamedGroup.NamedGroupSpec.NAMED_GROUP_ECDHE) {
                Signature signer;
                params = x509Possession.getECParameterSpec();
                if (params != null && ss.namedGroup == NamedGroup.valueOf(params) && (signer = ss.getSigner(signingKey)) != null) {
                    return new AbstractMap.SimpleImmutableEntry<SignatureScheme, Signature>(ss, signer);
                }
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake,verbose")) continue;
                SSLLogger.finest("Ignore the signature algorithm (" + (Object)((Object)ss) + "), unsupported EC parameter spec: " + params, new Object[0]);
                continue;
            }
            if ("EC".equals(ss.keyAlgorithm)) {
                Signature signer;
                NamedGroup keyGroup;
                params = x509Possession.getECParameterSpec();
                if (params != null && (keyGroup = NamedGroup.valueOf(params)) != null && NamedGroup.isEnabled(sslConfig, keyGroup) && (signer = ss.getSigner(signingKey)) != null) {
                    return new AbstractMap.SimpleImmutableEntry<SignatureScheme, Signature>(ss, signer);
                }
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake,verbose")) continue;
                SSLLogger.finest("Ignore the legacy signature algorithm (" + (Object)((Object)ss) + "), unsupported EC parameter spec: " + params, new Object[0]);
                continue;
            }
            Signature signer = ss.getSigner(signingKey);
            if (signer == null) continue;
            return new AbstractMap.SimpleImmutableEntry<SignatureScheme, Signature>(ss, signer);
        }
        return null;
    }

    private boolean isSupportedProtocol(ProtocolVersion version, Set<SSLScope> scopes) {
        if (scopes != null && scopes.equals(HANDSHAKE_SCOPE)) {
            return this.handshakeSupportedProtocols.contains((Object)version);
        }
        return this.supportedProtocols.contains((Object)version);
    }

    private boolean isAllowed(SSLAlgorithmConstraints constraints, ProtocolVersion version, Set<SSLScope> scopes) {
        return this.isAvailable && this.isSupportedProtocol(version, scopes) && this.isPermitted(constraints, scopes);
    }

    static String[] getAlgorithmNames(Collection<SignatureScheme> schemes) {
        if (schemes != null) {
            ArrayList<String> names = new ArrayList<String>(schemes.size());
            for (SignatureScheme scheme : schemes) {
                names.add(scheme.algorithm);
            }
            return names.toArray(new String[0]);
        }
        return new String[0];
    }

    private static List<SignatureScheme> namesOfAvailable(String[] signatureSchemes) {
        if (signatureSchemes == null || signatureSchemes.length == 0) {
            return Collections.emptyList();
        }
        ArrayList<SignatureScheme> sss = new ArrayList<SignatureScheme>(signatureSchemes.length);
        for (String ss : signatureSchemes) {
            SignatureScheme scheme = SignatureScheme.nameOf(ss);
            if (scheme == null || !scheme.isAvailable) {
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake,verbose")) continue;
                SSLLogger.finest("Ignore the signature algorithm (" + ss + "), unsupported or unavailable", new Object[0]);
                continue;
            }
            sss.add(scheme);
        }
        return sss;
    }

    Signature getVerifier(PublicKey publicKey) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        if (!this.isAvailable) {
            return null;
        }
        Signature verifier = Signature.getInstance(this.algorithm);
        SignatureUtil.initVerifyWithParam(verifier, publicKey, this.signAlgParams != null ? this.signAlgParams.parameterSpec : null);
        return verifier;
    }

    private Signature getSigner(PrivateKey privateKey) {
        if (!this.isAvailable) {
            return null;
        }
        try {
            Signature signer = Signature.getInstance(this.algorithm);
            SignatureUtil.initSignWithParam(signer, privateKey, this.signAlgParams != null ? this.signAlgParams.parameterSpec : null, null);
            return signer;
        }
        catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException nsae) {
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake,verbose")) {
                SSLLogger.finest("Ignore unsupported signature algorithm (" + this.name + ")", nsae);
            }
            return null;
        }
    }

    static {
        hashAlgorithms = new String[]{"none", "md5", "sha1", "sha224", "sha256", "sha384", "sha512"};
        signatureAlgorithms = new String[]{"anonymous", "rsa", "dsa", "ecdsa"};
        HANDSHAKE_SCOPE = Set.of(SSLScope.HANDSHAKE_SIGNATURE);
        CERTIFICATE_SCOPE = Set.of(SSLScope.CERTIFICATE_SIGNATURE);
        SIGNATURE_PRIMITIVE_SET = Set.of(CryptoPrimitive.SIGNATURE);
    }

    static enum SigAlgParamSpec {
        RSA_PSS_SHA256("SHA-256", 32),
        RSA_PSS_SHA384("SHA-384", 48),
        RSA_PSS_SHA512("SHA-512", 64);

        private final AlgorithmParameterSpec parameterSpec;
        private final AlgorithmParameters parameters;
        private final boolean isAvailable;

        private SigAlgParamSpec(String hash, int saltLength) {
            boolean mediator;
            AlgorithmParameters pssParams;
            PSSParameterSpec pssParamSpec;
            block2: {
                pssParamSpec = new PSSParameterSpec(hash, "MGF1", new MGF1ParameterSpec(hash), saltLength, 1);
                pssParams = null;
                mediator = true;
                try {
                    Signature signer = Signature.getInstance("RSASSA-PSS");
                    signer.setParameter(pssParamSpec);
                    pssParams = signer.getParameters();
                }
                catch (RuntimeException | InvalidAlgorithmParameterException | NoSuchAlgorithmException exp) {
                    mediator = false;
                    if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) break block2;
                    SSLLogger.warning("RSASSA-PSS signature with " + hash + " is not supported by the underlying providers", exp);
                }
            }
            this.isAvailable = mediator;
            this.parameterSpec = mediator ? pssParamSpec : null;
            this.parameters = mediator ? pssParams : null;
        }
    }
}

