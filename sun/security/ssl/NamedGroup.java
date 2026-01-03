/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.security.AlgorithmConstraints;
import java.security.AlgorithmParameters;
import java.security.CryptoPrimitive;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.NamedParameterSpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHParameterSpec;
import sun.security.action.GetPropertyAction;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.DHKeyExchange;
import sun.security.ssl.ECDHKeyExchange;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.JsseJce;
import sun.security.ssl.NamedGroupPossession;
import sun.security.ssl.PredefinedDHParameterSpecs;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.SSLConfiguration;
import sun.security.ssl.SSLCredentials;
import sun.security.ssl.SSLKeyDerivation;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLPossession;
import sun.security.ssl.XDHKeyExchange;
import sun.security.util.CurveDB;

enum NamedGroup {
    SECT163_K1(1, "sect163k1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("sect163k1")),
    SECT163_R1(2, "sect163r1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("sect163r1")),
    SECT163_R2(3, "sect163r2", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("sect163r2")),
    SECT193_R1(4, "sect193r1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("sect193r1")),
    SECT193_R2(5, "sect193r2", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("sect193r2")),
    SECT233_K1(6, "sect233k1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("sect233k1")),
    SECT233_R1(7, "sect233r1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("sect233r1")),
    SECT239_K1(8, "sect239k1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("sect239k1")),
    SECT283_K1(9, "sect283k1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("sect283k1")),
    SECT283_R1(10, "sect283r1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("sect283r1")),
    SECT409_K1(11, "sect409k1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("sect409k1")),
    SECT409_R1(12, "sect409r1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("sect409r1")),
    SECT571_K1(13, "sect571k1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("sect571k1")),
    SECT571_R1(14, "sect571r1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("sect571r1")),
    SECP160_K1(15, "secp160k1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("secp160k1")),
    SECP160_R1(16, "secp160r1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("secp160r1")),
    SECP160_R2(17, "secp160r2", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("secp160r2")),
    SECP192_K1(18, "secp192k1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("secp192k1")),
    SECP192_R1(19, "secp192r1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("secp192r1")),
    SECP224_K1(20, "secp224k1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("secp224k1")),
    SECP224_R1(21, "secp224r1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("secp224r1")),
    SECP256_K1(22, "secp256k1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_12, CurveDB.lookup("secp256k1")),
    SECP256_R1(23, "secp256r1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_13, CurveDB.lookup("secp256r1")),
    SECP384_R1(24, "secp384r1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_13, CurveDB.lookup("secp384r1")),
    SECP521_R1(25, "secp521r1", NamedGroupSpec.NAMED_GROUP_ECDHE, ProtocolVersion.PROTOCOLS_TO_13, CurveDB.lookup("secp521r1")),
    X25519(29, "x25519", NamedGroupSpec.NAMED_GROUP_XDH, ProtocolVersion.PROTOCOLS_TO_13, NamedParameterSpec.X25519),
    X448(30, "x448", NamedGroupSpec.NAMED_GROUP_XDH, ProtocolVersion.PROTOCOLS_TO_13, NamedParameterSpec.X448),
    FFDHE_2048(256, "ffdhe2048", NamedGroupSpec.NAMED_GROUP_FFDHE, ProtocolVersion.PROTOCOLS_TO_13, PredefinedDHParameterSpecs.ffdheParams.get(2048)),
    FFDHE_3072(257, "ffdhe3072", NamedGroupSpec.NAMED_GROUP_FFDHE, ProtocolVersion.PROTOCOLS_TO_13, PredefinedDHParameterSpecs.ffdheParams.get(3072)),
    FFDHE_4096(258, "ffdhe4096", NamedGroupSpec.NAMED_GROUP_FFDHE, ProtocolVersion.PROTOCOLS_TO_13, PredefinedDHParameterSpecs.ffdheParams.get(4096)),
    FFDHE_6144(259, "ffdhe6144", NamedGroupSpec.NAMED_GROUP_FFDHE, ProtocolVersion.PROTOCOLS_TO_13, PredefinedDHParameterSpecs.ffdheParams.get(6144)),
    FFDHE_8192(260, "ffdhe8192", NamedGroupSpec.NAMED_GROUP_FFDHE, ProtocolVersion.PROTOCOLS_TO_13, PredefinedDHParameterSpecs.ffdheParams.get(8192)),
    ARBITRARY_PRIME(65281, "arbitrary_explicit_prime_curves", NamedGroupSpec.NAMED_GROUP_ARBITRARY, ProtocolVersion.PROTOCOLS_TO_12, null),
    ARBITRARY_CHAR2(65282, "arbitrary_explicit_char2_curves", NamedGroupSpec.NAMED_GROUP_ARBITRARY, ProtocolVersion.PROTOCOLS_TO_12, null);

    final int id;
    final String name;
    final NamedGroupSpec spec;
    final ProtocolVersion[] supportedProtocols;
    final String algorithm;
    final AlgorithmParameterSpec keAlgParamSpec;
    final AlgorithmParameters keAlgParams;
    final boolean isAvailable;
    private static final Set<CryptoPrimitive> KEY_AGREEMENT_PRIMITIVE_SET;

    private NamedGroup(int id, String name, NamedGroupSpec namedGroupSpec, ProtocolVersion[] supportedProtocols, AlgorithmParameterSpec keAlgParamSpec) {
        boolean mediator;
        AlgorithmParameters algParams;
        block8: {
            this.id = id;
            this.name = name;
            this.spec = namedGroupSpec;
            this.algorithm = namedGroupSpec.algorithm;
            this.supportedProtocols = supportedProtocols;
            this.keAlgParamSpec = keAlgParamSpec;
            algParams = null;
            boolean bl = mediator = keAlgParamSpec != null;
            if (mediator && namedGroupSpec == NamedGroupSpec.NAMED_GROUP_ECDHE) {
                mediator = JsseJce.isEcAvailable();
            }
            if (mediator) {
                try {
                    algParams = AlgorithmParameters.getInstance(namedGroupSpec.algorithm);
                    algParams.init(keAlgParamSpec);
                }
                catch (NoSuchAlgorithmException | InvalidParameterSpecException exp) {
                    if (namedGroupSpec != NamedGroupSpec.NAMED_GROUP_XDH) {
                        mediator = false;
                        if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                            SSLLogger.warning("No AlgorithmParameters for " + name, exp);
                        }
                    }
                    algParams = null;
                    try {
                        KeyAgreement.getInstance(name);
                    }
                    catch (NoSuchAlgorithmException nsae) {
                        mediator = false;
                        if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) break block8;
                        SSLLogger.warning("No AlgorithmParameters for " + name, nsae);
                    }
                }
            }
        }
        this.isAvailable = mediator;
        this.keAlgParams = mediator ? algParams : null;
    }

    static NamedGroup valueOf(int id) {
        for (NamedGroup group : NamedGroup.values()) {
            if (group.id != id) continue;
            return group;
        }
        return null;
    }

    static NamedGroup valueOf(ECParameterSpec params) {
        for (NamedGroup ng : NamedGroup.values()) {
            if (ng.spec != NamedGroupSpec.NAMED_GROUP_ECDHE || params != ng.keAlgParamSpec && ng.keAlgParamSpec != CurveDB.lookup(params)) continue;
            return ng;
        }
        return null;
    }

    static NamedGroup valueOf(DHParameterSpec params) {
        for (NamedGroup ng : NamedGroup.values()) {
            DHParameterSpec ngParams;
            if (ng.spec != NamedGroupSpec.NAMED_GROUP_FFDHE || !(ngParams = (DHParameterSpec)ng.keAlgParamSpec).getP().equals(params.getP()) || !ngParams.getG().equals(params.getG())) continue;
            return ng;
        }
        return null;
    }

    static NamedGroup nameOf(String name) {
        for (NamedGroup group : NamedGroup.values()) {
            if (!group.name.equalsIgnoreCase(name)) continue;
            return group;
        }
        return null;
    }

    static String nameOf(int id) {
        for (NamedGroup group : NamedGroup.values()) {
            if (group.id != id) continue;
            return group.name;
        }
        return "UNDEFINED-NAMED-GROUP(" + id + ")";
    }

    public static List<NamedGroup> namesOf(String[] namedGroups) {
        if (namedGroups == null) {
            return null;
        }
        if (namedGroups.length == 0) {
            return List.of();
        }
        ArrayList<NamedGroup> ngs = new ArrayList<NamedGroup>(namedGroups.length);
        for (String ss : namedGroups) {
            NamedGroup ng = NamedGroup.nameOf(ss);
            if (ng == null || !ng.isAvailable) {
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake,verbose")) continue;
                SSLLogger.finest("Ignore the named group (" + ss + "), unsupported or unavailable", new Object[0]);
                continue;
            }
            ngs.add(ng);
        }
        return Collections.unmodifiableList(ngs);
    }

    static boolean isActivatable(SSLConfiguration sslConfig, AlgorithmConstraints constraints, NamedGroupSpec type) {
        boolean hasFFDHEGroups = false;
        for (String ng : sslConfig.namedGroups) {
            NamedGroup namedGroup = NamedGroup.nameOf(ng);
            if (namedGroup == null || !namedGroup.isAvailable || namedGroup.spec != type) continue;
            if (namedGroup.isPermitted(constraints)) {
                return true;
            }
            if (hasFFDHEGroups || type != NamedGroupSpec.NAMED_GROUP_FFDHE) continue;
            hasFFDHEGroups = true;
        }
        return !hasFFDHEGroups && type == NamedGroupSpec.NAMED_GROUP_FFDHE;
    }

    static boolean isActivatable(SSLConfiguration sslConfig, AlgorithmConstraints constraints, NamedGroup namedGroup) {
        if (!namedGroup.isAvailable || !NamedGroup.isEnabled(sslConfig, namedGroup)) {
            return false;
        }
        return namedGroup.isPermitted(constraints);
    }

    static boolean isEnabled(SSLConfiguration sslConfig, NamedGroup namedGroup) {
        for (String ng : sslConfig.namedGroups) {
            if (!namedGroup.name.equalsIgnoreCase(ng)) continue;
            return true;
        }
        return false;
    }

    static NamedGroup getPreferredGroup(SSLConfiguration sslConfig, ProtocolVersion negotiatedProtocol, AlgorithmConstraints constraints, NamedGroupSpec[] types) {
        for (String name : sslConfig.namedGroups) {
            NamedGroup ng = NamedGroup.nameOf(name);
            if (ng == null || !ng.isAvailable || !NamedGroupSpec.arrayContains(types, ng.spec) || !ng.isAvailable(negotiatedProtocol) || !ng.isPermitted(constraints)) continue;
            return ng;
        }
        return null;
    }

    static NamedGroup getPreferredGroup(SSLConfiguration sslConfig, ProtocolVersion negotiatedProtocol, AlgorithmConstraints constraints, NamedGroupSpec[] types, List<NamedGroup> requestedNamedGroups) {
        for (NamedGroup namedGroup : requestedNamedGroups) {
            if (!namedGroup.isAvailable || !NamedGroupSpec.arrayContains(types, namedGroup.spec) || !namedGroup.isAvailable(negotiatedProtocol) || !NamedGroup.isEnabled(sslConfig, namedGroup) || !namedGroup.isPermitted(constraints)) continue;
            return namedGroup;
        }
        return null;
    }

    boolean isAvailable(List<ProtocolVersion> protocolVersions) {
        if (this.isAvailable) {
            for (ProtocolVersion pv : this.supportedProtocols) {
                if (!protocolVersions.contains((Object)pv)) continue;
                return true;
            }
        }
        return false;
    }

    boolean isAvailable(ProtocolVersion protocolVersion) {
        if (this.isAvailable) {
            for (ProtocolVersion pv : this.supportedProtocols) {
                if (protocolVersion != pv) continue;
                return true;
            }
        }
        return false;
    }

    boolean isSupported(List<CipherSuite> cipherSuites) {
        for (CipherSuite cs : cipherSuites) {
            boolean isMatch = this.isAvailable(cs.supportedProtocols);
            if (!isMatch || cs.keyExchange != null && !NamedGroupSpec.arrayContains(cs.keyExchange.groupTypes, this.spec)) continue;
            return true;
        }
        return false;
    }

    boolean isPermitted(AlgorithmConstraints constraints) {
        return constraints.permits(KEY_AGREEMENT_PRIMITIVE_SET, this.name, null) && constraints.permits(KEY_AGREEMENT_PRIMITIVE_SET, this.algorithm, this.keAlgParams);
    }

    byte[] encodePossessionPublicKey(NamedGroupPossession namedGroupPossession) {
        return this.spec.encodePossessionPublicKey(namedGroupPossession);
    }

    SSLCredentials decodeCredentials(byte[] encoded) throws IOException, GeneralSecurityException {
        return this.spec.decodeCredentials(this, encoded);
    }

    SSLPossession createPossession(SecureRandom random) {
        return this.spec.createPossession(this, random);
    }

    SSLKeyDerivation createKeyDerivation(HandshakeContext hc) throws IOException {
        return this.spec.createKeyDerivation(hc);
    }

    static {
        KEY_AGREEMENT_PRIMITIVE_SET = Collections.unmodifiableSet(EnumSet.of(CryptoPrimitive.KEY_AGREEMENT));
    }

    static enum NamedGroupSpec implements NamedGroupScheme
    {
        NAMED_GROUP_ECDHE("EC", ECDHEScheme.instance),
        NAMED_GROUP_FFDHE("DiffieHellman", FFDHEScheme.instance),
        NAMED_GROUP_XDH("XDH", XDHScheme.instance),
        NAMED_GROUP_ARBITRARY("EC", null),
        NAMED_GROUP_NONE("", null);

        private final String algorithm;
        private final NamedGroupScheme scheme;

        private NamedGroupSpec(String algorithm, NamedGroupScheme scheme) {
            this.algorithm = algorithm;
            this.scheme = scheme;
        }

        boolean isSupported(List<CipherSuite> cipherSuites) {
            for (CipherSuite cs : cipherSuites) {
                if (cs.keyExchange != null && !NamedGroupSpec.arrayContains(cs.keyExchange.groupTypes, this)) continue;
                return true;
            }
            return false;
        }

        static boolean arrayContains(NamedGroupSpec[] namedGroupTypes, NamedGroupSpec namedGroupType) {
            for (NamedGroupSpec ng : namedGroupTypes) {
                if (ng != namedGroupType) continue;
                return true;
            }
            return false;
        }

        @Override
        public byte[] encodePossessionPublicKey(NamedGroupPossession namedGroupPossession) {
            if (this.scheme != null) {
                return this.scheme.encodePossessionPublicKey(namedGroupPossession);
            }
            return null;
        }

        @Override
        public SSLCredentials decodeCredentials(NamedGroup ng, byte[] encoded) throws IOException, GeneralSecurityException {
            if (this.scheme != null) {
                return this.scheme.decodeCredentials(ng, encoded);
            }
            return null;
        }

        @Override
        public SSLPossession createPossession(NamedGroup ng, SecureRandom random) {
            if (this.scheme != null) {
                return this.scheme.createPossession(ng, random);
            }
            return null;
        }

        @Override
        public SSLKeyDerivation createKeyDerivation(HandshakeContext hc) throws IOException {
            if (this.scheme != null) {
                return this.scheme.createKeyDerivation(hc);
            }
            return null;
        }
    }

    static final class SupportedGroups {
        static final String[] namedGroups;

        SupportedGroups() {
        }

        static {
            ArrayList<String> groupList;
            String property = GetPropertyAction.privilegedGetProperty("jdk.tls.namedGroups");
            if (property != null && !property.isEmpty() && property.length() > 1 && property.charAt(0) == '\"' && property.charAt(property.length() - 1) == '\"') {
                property = property.substring(1, property.length() - 1);
            }
            if (property != null && !property.isEmpty()) {
                String[] groups = property.split(",");
                groupList = new ArrayList<String>(groups.length);
                for (String group : groups) {
                    NamedGroup namedGroup;
                    if ((group = group.trim()).isEmpty() || (namedGroup = NamedGroup.nameOf(group)) == null || !namedGroup.isAvailable) continue;
                    groupList.add(namedGroup.name);
                }
                if (groupList.isEmpty()) {
                    throw new IllegalArgumentException("System property jdk.tls.namedGroups(" + property + ") contains no supported named groups");
                }
            } else {
                NamedGroup[] groups = new NamedGroup[]{X25519, SECP256_R1, SECP384_R1, SECP521_R1, X448, FFDHE_2048, FFDHE_3072, FFDHE_4096, FFDHE_6144, FFDHE_8192};
                groupList = new ArrayList(groups.length);
                for (NamedGroup group : groups) {
                    if (!group.isAvailable) continue;
                    groupList.add(group.name);
                }
                if (groupList.isEmpty() && SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.warning("No default named groups", new Object[0]);
                }
            }
            namedGroups = groupList.toArray(new String[0]);
        }
    }

    private static class XDHScheme
    implements NamedGroupScheme {
        private static final XDHScheme instance = new XDHScheme();

        private XDHScheme() {
        }

        @Override
        public byte[] encodePossessionPublicKey(NamedGroupPossession poss) {
            return ((XDHKeyExchange.XDHEPossession)poss).encode();
        }

        @Override
        public SSLCredentials decodeCredentials(NamedGroup ng, byte[] encoded) throws IOException, GeneralSecurityException {
            return XDHKeyExchange.XDHECredentials.valueOf(ng, encoded);
        }

        @Override
        public SSLPossession createPossession(NamedGroup ng, SecureRandom random) {
            return new XDHKeyExchange.XDHEPossession(ng, random);
        }

        @Override
        public SSLKeyDerivation createKeyDerivation(HandshakeContext hc) throws IOException {
            return XDHKeyExchange.xdheKAGenerator.createKeyDerivation(hc);
        }
    }

    private static class ECDHEScheme
    implements NamedGroupScheme {
        private static final ECDHEScheme instance = new ECDHEScheme();

        private ECDHEScheme() {
        }

        @Override
        public byte[] encodePossessionPublicKey(NamedGroupPossession namedGroupPossession) {
            return ((ECDHKeyExchange.ECDHEPossession)namedGroupPossession).encode();
        }

        @Override
        public SSLCredentials decodeCredentials(NamedGroup ng, byte[] encoded) throws IOException, GeneralSecurityException {
            return ECDHKeyExchange.ECDHECredentials.valueOf(ng, encoded);
        }

        @Override
        public SSLPossession createPossession(NamedGroup ng, SecureRandom random) {
            return new ECDHKeyExchange.ECDHEPossession(ng, random);
        }

        @Override
        public SSLKeyDerivation createKeyDerivation(HandshakeContext hc) throws IOException {
            return ECDHKeyExchange.ecdheKAGenerator.createKeyDerivation(hc);
        }
    }

    private static class FFDHEScheme
    implements NamedGroupScheme {
        private static final FFDHEScheme instance = new FFDHEScheme();

        private FFDHEScheme() {
        }

        @Override
        public byte[] encodePossessionPublicKey(NamedGroupPossession namedGroupPossession) {
            return namedGroupPossession.encode();
        }

        @Override
        public SSLCredentials decodeCredentials(NamedGroup ng, byte[] encoded) throws IOException, GeneralSecurityException {
            return DHKeyExchange.DHECredentials.valueOf(ng, encoded);
        }

        @Override
        public SSLPossession createPossession(NamedGroup ng, SecureRandom random) {
            return new DHKeyExchange.DHEPossession(ng, random);
        }

        @Override
        public SSLKeyDerivation createKeyDerivation(HandshakeContext hc) throws IOException {
            return DHKeyExchange.kaGenerator.createKeyDerivation(hc);
        }
    }

    private static interface NamedGroupScheme {
        public byte[] encodePossessionPublicKey(NamedGroupPossession var1);

        public SSLCredentials decodeCredentials(NamedGroup var1, byte[] var2) throws IOException, GeneralSecurityException;

        public SSLPossession createPossession(NamedGroup var1, SecureRandom var2);

        public SSLKeyDerivation createKeyDerivation(HandshakeContext var1) throws IOException;
    }
}

