/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.security.AlgorithmConstraints;
import java.security.CryptoPrimitive;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.EnumSet;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLHandshakeException;
import sun.security.ssl.Alert;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.KAKeyDerivation;
import sun.security.ssl.NamedGroup;
import sun.security.ssl.NamedGroupCredentials;
import sun.security.ssl.NamedGroupPossession;
import sun.security.ssl.SSLCredentials;
import sun.security.ssl.SSLKeyAgreementGenerator;
import sun.security.ssl.SSLKeyDerivation;
import sun.security.ssl.SSLPossession;
import sun.security.ssl.SSLPossessionGenerator;
import sun.security.ssl.ServerHandshakeContext;
import sun.security.ssl.X509Authentication;
import sun.security.ssl.XDHKeyExchange;
import sun.security.util.ECUtil;

final class ECDHKeyExchange {
    static final SSLPossessionGenerator poGenerator = new ECDHEPossessionGenerator();
    static final SSLKeyAgreementGenerator ecdhKAGenerator = new ECDHKAGenerator();
    static final SSLKeyAgreementGenerator ecdheKAGenerator = new ECDHEKAGenerator();
    static final SSLKeyAgreementGenerator ecdheXdhKAGenerator = new ECDHEXDHKAGenerator();

    ECDHKeyExchange() {
    }

    private static final class ECDHEPossessionGenerator
    implements SSLPossessionGenerator {
        private ECDHEPossessionGenerator() {
        }

        @Override
        public SSLPossession createPossession(HandshakeContext context) {
            NamedGroup preferableNamedGroup = context.clientRequestedNamedGroups != null && !context.clientRequestedNamedGroups.isEmpty() ? NamedGroup.getPreferredGroup(context.sslConfig, context.negotiatedProtocol, context.algorithmConstraints, new NamedGroup.NamedGroupSpec[]{NamedGroup.NamedGroupSpec.NAMED_GROUP_ECDHE, NamedGroup.NamedGroupSpec.NAMED_GROUP_XDH}, context.clientRequestedNamedGroups) : NamedGroup.getPreferredGroup(context.sslConfig, context.negotiatedProtocol, context.algorithmConstraints, new NamedGroup.NamedGroupSpec[]{NamedGroup.NamedGroupSpec.NAMED_GROUP_ECDHE, NamedGroup.NamedGroupSpec.NAMED_GROUP_XDH});
            if (preferableNamedGroup != null) {
                return preferableNamedGroup.createPossession(context.sslContext.getSecureRandom());
            }
            return null;
        }
    }

    private static final class ECDHKAGenerator
    implements SSLKeyAgreementGenerator {
        private ECDHKAGenerator() {
        }

        @Override
        public SSLKeyDerivation createKeyDerivation(HandshakeContext context) throws IOException {
            if (context instanceof ServerHandshakeContext) {
                return this.createServerKeyDerivation((ServerHandshakeContext)context);
            }
            return this.createClientKeyDerivation((ClientHandshakeContext)context);
        }

        private SSLKeyDerivation createServerKeyDerivation(ServerHandshakeContext shc) throws IOException {
            X509Authentication.X509Possession x509Possession = null;
            ECDHECredentials ecdheCredentials = null;
            for (SSLPossession poss : shc.handshakePossessions) {
                ECParameterSpec params;
                if (!(poss instanceof X509Authentication.X509Possession) || (params = ((X509Authentication.X509Possession)poss).getECParameterSpec()) == null) continue;
                NamedGroup ng = NamedGroup.valueOf(params);
                if (ng == null) {
                    throw shc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Unsupported EC server cert for ECDH key exchange");
                }
                for (SSLCredentials cred : shc.handshakeCredentials) {
                    if (!(cred instanceof ECDHECredentials) || !ng.equals((Object)((ECDHECredentials)cred).namedGroup)) continue;
                    ecdheCredentials = (ECDHECredentials)cred;
                    break;
                }
                if (ecdheCredentials == null) continue;
                x509Possession = (X509Authentication.X509Possession)poss;
                break;
            }
            if (x509Possession == null) {
                throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "No sufficient ECDHE key agreement parameters negotiated");
            }
            return new KAKeyDerivation("ECDH", shc, x509Possession.popPrivateKey, ecdheCredentials.popPublicKey);
        }

        private SSLKeyDerivation createClientKeyDerivation(ClientHandshakeContext chc) throws IOException {
            ECDHEPossession ecdhePossession = null;
            X509Authentication.X509Credentials x509Credentials = null;
            for (SSLPossession poss : chc.handshakePossessions) {
                if (!(poss instanceof ECDHEPossession)) continue;
                NamedGroup ng = ((ECDHEPossession)poss).namedGroup;
                for (SSLCredentials cred : chc.handshakeCredentials) {
                    PublicKey publicKey;
                    if (!(cred instanceof X509Authentication.X509Credentials) || !(publicKey = ((X509Authentication.X509Credentials)cred).popPublicKey).getAlgorithm().equals("EC")) continue;
                    ECParameterSpec params = ((ECPublicKey)publicKey).getParams();
                    NamedGroup namedGroup = NamedGroup.valueOf(params);
                    if (namedGroup == null) {
                        throw chc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Unsupported EC server cert for ECDH key exchange");
                    }
                    if (!ng.equals((Object)namedGroup)) continue;
                    x509Credentials = (X509Authentication.X509Credentials)cred;
                    break;
                }
                if (x509Credentials == null) continue;
                ecdhePossession = (ECDHEPossession)poss;
                break;
            }
            if (ecdhePossession == null) {
                throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "No sufficient ECDH key agreement parameters negotiated");
            }
            return new KAKeyDerivation("ECDH", chc, ecdhePossession.privateKey, x509Credentials.popPublicKey);
        }
    }

    private static final class ECDHEKAGenerator
    implements SSLKeyAgreementGenerator {
        private ECDHEKAGenerator() {
        }

        @Override
        public SSLKeyDerivation createKeyDerivation(HandshakeContext context) throws IOException {
            ECDHEPossession ecdhePossession = null;
            ECDHECredentials ecdheCredentials = null;
            for (SSLPossession poss : context.handshakePossessions) {
                if (!(poss instanceof ECDHEPossession)) continue;
                NamedGroup ng = ((ECDHEPossession)poss).namedGroup;
                for (SSLCredentials cred : context.handshakeCredentials) {
                    if (!(cred instanceof ECDHECredentials) || !ng.equals((Object)((ECDHECredentials)cred).namedGroup)) continue;
                    ecdheCredentials = (ECDHECredentials)cred;
                    break;
                }
                if (ecdheCredentials == null) continue;
                ecdhePossession = (ECDHEPossession)poss;
                break;
            }
            if (ecdhePossession == null) {
                throw context.conContext.fatal(Alert.HANDSHAKE_FAILURE, "No sufficient ECDHE key agreement parameters negotiated");
            }
            return new KAKeyDerivation("ECDH", context, ecdhePossession.privateKey, ecdheCredentials.popPublicKey);
        }
    }

    private static final class ECDHEXDHKAGenerator
    implements SSLKeyAgreementGenerator {
        private ECDHEXDHKAGenerator() {
        }

        @Override
        public SSLKeyDerivation createKeyDerivation(HandshakeContext context) throws IOException {
            NamedGroupPossession namedGroupPossession = null;
            NamedGroupCredentials namedGroupCredentials = null;
            NamedGroup namedGroup = null;
            block4: for (SSLPossession poss : context.handshakePossessions) {
                for (SSLCredentials cred : context.handshakeCredentials) {
                    if ((!(poss instanceof ECDHEPossession) || !(cred instanceof ECDHECredentials)) && (!(poss instanceof XDHKeyExchange.XDHEPossession) || !(cred instanceof XDHKeyExchange.XDHECredentials))) continue;
                    NamedGroupPossession p = (NamedGroupPossession)poss;
                    NamedGroupCredentials c = (NamedGroupCredentials)cred;
                    if (p.getNamedGroup() != c.getNamedGroup()) continue;
                    namedGroup = p.getNamedGroup();
                    namedGroupPossession = p;
                    namedGroupCredentials = c;
                    break block4;
                }
            }
            if (namedGroupPossession == null) {
                throw context.conContext.fatal(Alert.HANDSHAKE_FAILURE, "No sufficient ECDHE/XDH key agreement parameters negotiated");
            }
            return new KAKeyDerivation(switch (namedGroup.spec) {
                case NamedGroup.NamedGroupSpec.NAMED_GROUP_ECDHE -> "ECDH";
                case NamedGroup.NamedGroupSpec.NAMED_GROUP_XDH -> "XDH";
                default -> throw new RuntimeException("Unexpected named group type");
            }, context, namedGroupPossession.getPrivateKey(), namedGroupCredentials.getPublicKey());
        }
    }

    static final class ECDHEPossession
    implements NamedGroupPossession {
        final PrivateKey privateKey;
        final ECPublicKey publicKey;
        final NamedGroup namedGroup;

        ECDHEPossession(NamedGroup namedGroup, SecureRandom random) {
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
                kpg.initialize(namedGroup.keAlgParamSpec, random);
                KeyPair kp = kpg.generateKeyPair();
                this.privateKey = kp.getPrivate();
                this.publicKey = (ECPublicKey)kp.getPublic();
            }
            catch (GeneralSecurityException e) {
                throw new RuntimeException("Could not generate ECDH keypair", e);
            }
            this.namedGroup = namedGroup;
        }

        ECDHEPossession(ECDHECredentials credentials, SecureRandom random) {
            ECParameterSpec params = credentials.popPublicKey.getParams();
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
                kpg.initialize(params, random);
                KeyPair kp = kpg.generateKeyPair();
                this.privateKey = kp.getPrivate();
                this.publicKey = (ECPublicKey)kp.getPublic();
            }
            catch (GeneralSecurityException e) {
                throw new RuntimeException("Could not generate ECDH keypair", e);
            }
            this.namedGroup = credentials.namedGroup;
        }

        @Override
        public byte[] encode() {
            return ECUtil.encodePoint(this.publicKey.getW(), this.publicKey.getParams().getCurve());
        }

        SecretKey getAgreedSecret(PublicKey peerPublicKey) throws SSLHandshakeException {
            try {
                KeyAgreement ka = KeyAgreement.getInstance("ECDH");
                ka.init(this.privateKey);
                ka.doPhase(peerPublicKey, true);
                return ka.generateSecret("TlsPremasterSecret");
            }
            catch (GeneralSecurityException e) {
                throw new SSLHandshakeException("Could not generate secret", e);
            }
        }

        SecretKey getAgreedSecret(byte[] encodedPoint) throws SSLHandshakeException {
            try {
                ECParameterSpec params = this.publicKey.getParams();
                ECPoint point = ECUtil.decodePoint(encodedPoint, params.getCurve());
                KeyFactory kf = KeyFactory.getInstance("EC");
                ECPublicKeySpec spec = new ECPublicKeySpec(point, params);
                PublicKey peerPublicKey = kf.generatePublic(spec);
                return this.getAgreedSecret(peerPublicKey);
            }
            catch (IOException | GeneralSecurityException e) {
                throw new SSLHandshakeException("Could not generate secret", e);
            }
        }

        void checkConstraints(AlgorithmConstraints constraints, byte[] encodedPoint) throws SSLHandshakeException {
            try {
                ECParameterSpec params = this.publicKey.getParams();
                ECPoint point = ECUtil.decodePoint(encodedPoint, params.getCurve());
                ECPublicKeySpec spec = new ECPublicKeySpec(point, params);
                KeyFactory kf = KeyFactory.getInstance("EC");
                ECPublicKey pubKey = (ECPublicKey)kf.generatePublic(spec);
                if (!constraints.permits(EnumSet.of(CryptoPrimitive.KEY_AGREEMENT), pubKey)) {
                    throw new SSLHandshakeException("ECPublicKey does not comply to algorithm constraints");
                }
            }
            catch (IOException | GeneralSecurityException e) {
                throw new SSLHandshakeException("Could not generate ECPublicKey", e);
            }
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

    static final class ECDHECredentials
    implements NamedGroupCredentials {
        final ECPublicKey popPublicKey;
        final NamedGroup namedGroup;

        ECDHECredentials(ECPublicKey popPublicKey, NamedGroup namedGroup) {
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

        static ECDHECredentials valueOf(NamedGroup namedGroup, byte[] encodedPoint) throws IOException, GeneralSecurityException {
            if (namedGroup.spec != NamedGroup.NamedGroupSpec.NAMED_GROUP_ECDHE) {
                throw new RuntimeException("Credentials decoding:  Not ECDHE named group");
            }
            if (encodedPoint == null || encodedPoint.length == 0) {
                return null;
            }
            ECParameterSpec parameters = (ECParameterSpec)namedGroup.keAlgParamSpec;
            ECPoint point = ECUtil.decodePoint(encodedPoint, parameters.getCurve());
            KeyFactory factory = KeyFactory.getInstance("EC");
            ECPublicKey publicKey = (ECPublicKey)factory.generatePublic(new ECPublicKeySpec(point, parameters));
            return new ECDHECredentials(publicKey, namedGroup);
        }
    }
}

