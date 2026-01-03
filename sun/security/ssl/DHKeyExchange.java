/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import sun.security.action.GetPropertyAction;
import sun.security.ssl.Alert;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.KAKeyDerivation;
import sun.security.ssl.NamedGroup;
import sun.security.ssl.NamedGroupCredentials;
import sun.security.ssl.NamedGroupPossession;
import sun.security.ssl.PredefinedDHParameterSpecs;
import sun.security.ssl.SSLCredentials;
import sun.security.ssl.SSLKeyAgreementGenerator;
import sun.security.ssl.SSLKeyDerivation;
import sun.security.ssl.SSLPossession;
import sun.security.ssl.SSLPossessionGenerator;
import sun.security.ssl.ServerHandshakeContext;
import sun.security.ssl.Utilities;
import sun.security.ssl.X509Authentication;
import sun.security.util.KeyUtil;

final class DHKeyExchange {
    static final SSLPossessionGenerator poGenerator = new DHEPossessionGenerator(false);
    static final SSLPossessionGenerator poExportableGenerator = new DHEPossessionGenerator(true);
    static final SSLKeyAgreementGenerator kaGenerator = new DHEKAGenerator();

    DHKeyExchange() {
    }

    private static final class DHEPossessionGenerator
    implements SSLPossessionGenerator {
        private static final boolean useSmartEphemeralDHKeys;
        private static final boolean useLegacyEphemeralDHKeys;
        private static final int customizedDHKeySize;
        private final boolean exportable;

        private DHEPossessionGenerator(boolean exportable) {
            this.exportable = exportable;
        }

        @Override
        public SSLPossession createPossession(HandshakeContext context) {
            int keySize;
            NamedGroup preferableNamedGroup;
            if (!useLegacyEphemeralDHKeys && context.clientRequestedNamedGroups != null && !context.clientRequestedNamedGroups.isEmpty() && (preferableNamedGroup = NamedGroup.getPreferredGroup(context.sslConfig, context.negotiatedProtocol, context.algorithmConstraints, new NamedGroup.NamedGroupSpec[]{NamedGroup.NamedGroupSpec.NAMED_GROUP_FFDHE}, context.clientRequestedNamedGroups)) != null) {
                return new DHEPossession(preferableNamedGroup, context.sslContext.getSecureRandom());
            }
            int n = keySize = this.exportable ? 512 : 2048;
            if (!this.exportable) {
                if (useLegacyEphemeralDHKeys) {
                    keySize = 768;
                } else if (useSmartEphemeralDHKeys) {
                    PrivateKey key = null;
                    ServerHandshakeContext shc = (ServerHandshakeContext)context;
                    if (shc.interimAuthn instanceof X509Authentication.X509Possession) {
                        key = ((X509Authentication.X509Possession)shc.interimAuthn).popPrivateKey;
                    }
                    if (key != null) {
                        int ks = KeyUtil.getKeySize(key);
                        keySize = ks <= 1024 ? 1024 : 2048;
                    }
                } else if (customizedDHKeySize > 0) {
                    keySize = customizedDHKeySize;
                }
            }
            return new DHEPossession(keySize, context.sslContext.getSecureRandom());
        }

        static {
            String property = GetPropertyAction.privilegedGetProperty("jdk.tls.ephemeralDHKeySize");
            if (property == null || property.isEmpty()) {
                useLegacyEphemeralDHKeys = false;
                useSmartEphemeralDHKeys = false;
                customizedDHKeySize = -1;
            } else if ("matched".equals(property)) {
                useLegacyEphemeralDHKeys = false;
                useSmartEphemeralDHKeys = true;
                customizedDHKeySize = -1;
            } else if ("legacy".equals(property)) {
                useLegacyEphemeralDHKeys = true;
                useSmartEphemeralDHKeys = false;
                customizedDHKeySize = -1;
            } else {
                useLegacyEphemeralDHKeys = false;
                useSmartEphemeralDHKeys = false;
                try {
                    customizedDHKeySize = Integer.parseUnsignedInt(property);
                    if (customizedDHKeySize < 1024 || customizedDHKeySize > 8192 || (customizedDHKeySize & 0x3F) != 0) {
                        throw new IllegalArgumentException("Unsupported customized DH key size: " + customizedDHKeySize + ". The key size must be multiple of 64, and range from 1024 to 8192 (inclusive)");
                    }
                }
                catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("Invalid system property jdk.tls.ephemeralDHKeySize");
                }
            }
        }
    }

    private static final class DHEKAGenerator
    implements SSLKeyAgreementGenerator {
        private static final DHEKAGenerator instance = new DHEKAGenerator();

        private DHEKAGenerator() {
        }

        @Override
        public SSLKeyDerivation createKeyDerivation(HandshakeContext context) throws IOException {
            DHEPossession dhePossession = null;
            DHECredentials dheCredentials = null;
            for (SSLPossession poss : context.handshakePossessions) {
                if (!(poss instanceof DHEPossession)) continue;
                DHEPossession dhep = (DHEPossession)poss;
                for (SSLCredentials cred : context.handshakeCredentials) {
                    if (!(cred instanceof DHECredentials)) continue;
                    DHECredentials dhec = (DHECredentials)cred;
                    if (dhep.namedGroup != null && dhec.namedGroup != null) {
                        if (!dhep.namedGroup.equals((Object)dhec.namedGroup)) continue;
                        dheCredentials = (DHECredentials)cred;
                        break;
                    }
                    DHParameterSpec pps = dhep.publicKey.getParams();
                    DHParameterSpec cps = dhec.popPublicKey.getParams();
                    if (!pps.getP().equals(cps.getP()) || !pps.getG().equals(cps.getG())) continue;
                    dheCredentials = (DHECredentials)cred;
                    break;
                }
                if (dheCredentials == null) continue;
                dhePossession = (DHEPossession)poss;
                break;
            }
            if (dhePossession == null) {
                throw context.conContext.fatal(Alert.HANDSHAKE_FAILURE, "No sufficient DHE key agreement parameters negotiated");
            }
            return new KAKeyDerivation("DiffieHellman", context, dhePossession.privateKey, dheCredentials.popPublicKey);
        }
    }

    static final class DHEPossession
    implements NamedGroupPossession {
        final PrivateKey privateKey;
        final DHPublicKey publicKey;
        final NamedGroup namedGroup;

        DHEPossession(NamedGroup namedGroup, SecureRandom random) {
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");
                kpg.initialize(namedGroup.keAlgParamSpec, random);
                KeyPair kp = this.generateDHKeyPair(kpg);
                if (kp == null) {
                    throw new RuntimeException("Could not generate DH keypair");
                }
                this.privateKey = kp.getPrivate();
                this.publicKey = (DHPublicKey)kp.getPublic();
            }
            catch (GeneralSecurityException gse) {
                throw new RuntimeException("Could not generate DH keypair", gse);
            }
            this.namedGroup = namedGroup;
        }

        DHEPossession(int keyLength, SecureRandom random) {
            DHParameterSpec params = PredefinedDHParameterSpecs.definedParams.get(keyLength);
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");
                if (params != null) {
                    kpg.initialize(params, random);
                } else {
                    kpg.initialize(keyLength, random);
                }
                KeyPair kp = this.generateDHKeyPair(kpg);
                if (kp == null) {
                    throw new RuntimeException("Could not generate DH keypair of " + keyLength + " bits");
                }
                this.privateKey = kp.getPrivate();
                this.publicKey = (DHPublicKey)kp.getPublic();
            }
            catch (GeneralSecurityException gse) {
                throw new RuntimeException("Could not generate DH keypair", gse);
            }
            this.namedGroup = NamedGroup.valueOf(this.publicKey.getParams());
        }

        DHEPossession(DHECredentials credentials, SecureRandom random) {
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");
                kpg.initialize(credentials.popPublicKey.getParams(), random);
                KeyPair kp = this.generateDHKeyPair(kpg);
                if (kp == null) {
                    throw new RuntimeException("Could not generate DH keypair");
                }
                this.privateKey = kp.getPrivate();
                this.publicKey = (DHPublicKey)kp.getPublic();
            }
            catch (GeneralSecurityException gse) {
                throw new RuntimeException("Could not generate DH keypair", gse);
            }
            this.namedGroup = credentials.namedGroup;
        }

        private KeyPair generateDHKeyPair(KeyPairGenerator kpg) throws GeneralSecurityException {
            boolean doExtraValidation = !KeyUtil.isOracleJCEProvider(kpg.getProvider().getName());
            boolean isRecovering = false;
            for (int i = 0; i <= 2; ++i) {
                KeyPair kp = kpg.generateKeyPair();
                if (doExtraValidation) {
                    DHPublicKeySpec spec = DHEPossession.getDHPublicKeySpec(kp.getPublic());
                    try {
                        KeyUtil.validate(spec);
                    }
                    catch (InvalidKeyException ivke) {
                        if (isRecovering) {
                            throw ivke;
                        }
                        isRecovering = true;
                        continue;
                    }
                }
                return kp;
            }
            return null;
        }

        private static DHPublicKeySpec getDHPublicKeySpec(PublicKey key) {
            if (key instanceof DHPublicKey) {
                DHPublicKey dhKey = (DHPublicKey)key;
                DHParameterSpec params = dhKey.getParams();
                return new DHPublicKeySpec(dhKey.getY(), params.getP(), params.getG());
            }
            try {
                KeyFactory factory = KeyFactory.getInstance("DiffieHellman");
                return factory.getKeySpec(key, DHPublicKeySpec.class);
            }
            catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new RuntimeException("Unable to get DHPublicKeySpec", e);
            }
        }

        @Override
        public byte[] encode() {
            byte[] encoded = Utilities.toByteArray(this.publicKey.getY());
            int pSize = KeyUtil.getKeySize(this.publicKey) + 7 >>> 3;
            if (pSize > 0 && encoded.length < pSize) {
                byte[] buffer = new byte[pSize];
                System.arraycopy(encoded, 0, buffer, pSize - encoded.length, encoded.length);
                encoded = buffer;
            }
            return encoded;
        }

        @Override
        public PublicKey getPublicKey() {
            return this.publicKey;
        }

        @Override
        public NamedGroup getNamedGroup() {
            return this.namedGroup;
        }

        @Override
        public PrivateKey getPrivateKey() {
            return this.privateKey;
        }
    }

    static final class DHECredentials
    implements NamedGroupCredentials {
        final DHPublicKey popPublicKey;
        final NamedGroup namedGroup;

        DHECredentials(DHPublicKey popPublicKey, NamedGroup namedGroup) {
            this.popPublicKey = popPublicKey;
            this.namedGroup = namedGroup;
        }

        @Override
        public PublicKey getPublicKey() {
            return this.popPublicKey;
        }

        @Override
        public NamedGroup getNamedGroup() {
            return this.namedGroup;
        }

        static DHECredentials valueOf(NamedGroup ng, byte[] encodedPublic) throws IOException, GeneralSecurityException {
            if (ng.spec != NamedGroup.NamedGroupSpec.NAMED_GROUP_FFDHE) {
                throw new RuntimeException("Credentials decoding:  Not FFDHE named group");
            }
            if (encodedPublic == null || encodedPublic.length == 0) {
                return null;
            }
            DHParameterSpec params = (DHParameterSpec)ng.keAlgParamSpec;
            KeyFactory kf = KeyFactory.getInstance("DiffieHellman");
            DHPublicKeySpec spec = new DHPublicKeySpec(new BigInteger(1, encodedPublic), params.getP(), params.getG());
            DHPublicKey publicKey = (DHPublicKey)kf.generatePublic(spec);
            return new DHECredentials(publicKey, ng);
        }
    }
}

