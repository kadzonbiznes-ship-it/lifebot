/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.XECPublicKey;
import java.security.spec.NamedParameterSpec;
import java.security.spec.XECPublicKeySpec;
import sun.security.ssl.Alert;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.KAKeyDerivation;
import sun.security.ssl.NamedGroup;
import sun.security.ssl.NamedGroupCredentials;
import sun.security.ssl.NamedGroupPossession;
import sun.security.ssl.SSLCredentials;
import sun.security.ssl.SSLKeyAgreementGenerator;
import sun.security.ssl.SSLKeyDerivation;
import sun.security.ssl.SSLPossession;
import sun.security.ssl.Utilities;
import sun.security.util.ECUtil;

final class XDHKeyExchange {
    static final SSLKeyAgreementGenerator xdheKAGenerator = new XDHEKAGenerator();

    XDHKeyExchange() {
    }

    private static final class XDHEKAGenerator
    implements SSLKeyAgreementGenerator {
        private XDHEKAGenerator() {
        }

        @Override
        public SSLKeyDerivation createKeyDerivation(HandshakeContext context) throws IOException {
            XDHEPossession xdhePossession = null;
            XDHECredentials xdheCredentials = null;
            for (SSLPossession poss : context.handshakePossessions) {
                if (!(poss instanceof XDHEPossession)) continue;
                NamedGroup ng = ((XDHEPossession)poss).namedGroup;
                for (SSLCredentials cred : context.handshakeCredentials) {
                    if (!(cred instanceof XDHECredentials) || !ng.equals((Object)((XDHECredentials)cred).namedGroup)) continue;
                    xdheCredentials = (XDHECredentials)cred;
                    break;
                }
                if (xdheCredentials == null) continue;
                xdhePossession = (XDHEPossession)poss;
                break;
            }
            if (xdhePossession == null || xdheCredentials == null) {
                context.conContext.fatal(Alert.HANDSHAKE_FAILURE, "No sufficient XDHE key agreement parameters negotiated");
            }
            return new KAKeyDerivation("XDH", context, xdhePossession.privateKey, xdheCredentials.popPublicKey);
        }
    }

    static final class XDHEPossession
    implements NamedGroupPossession {
        final PrivateKey privateKey;
        final XECPublicKey publicKey;
        final NamedGroup namedGroup;

        XDHEPossession(NamedGroup namedGroup, SecureRandom random) {
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance(namedGroup.algorithm);
                kpg.initialize(namedGroup.keAlgParamSpec, random);
                KeyPair kp = kpg.generateKeyPair();
                this.privateKey = kp.getPrivate();
                this.publicKey = (XECPublicKey)kp.getPublic();
            }
            catch (GeneralSecurityException e) {
                throw new RuntimeException("Could not generate XDH keypair", e);
            }
            this.namedGroup = namedGroup;
        }

        @Override
        public byte[] encode() {
            byte[] uBytes = ECUtil.trimZeroes(this.publicKey.getU().toByteArray());
            int expLength = switch (this.namedGroup) {
                case NamedGroup.X25519 -> 32;
                case NamedGroup.X448 -> 56;
                default -> throw new RuntimeException("Invalid XDH group");
            };
            if (uBytes.length > expLength) {
                throw new RuntimeException("Encoded XDH key too large");
            }
            if (uBytes.length != expLength) {
                byte[] tmp = new byte[expLength];
                System.arraycopy(uBytes, 0, tmp, expLength - uBytes.length, uBytes.length);
                uBytes = tmp;
            }
            Utilities.reverseBytes(uBytes);
            return uBytes;
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

    static final class XDHECredentials
    implements NamedGroupCredentials {
        final XECPublicKey popPublicKey;
        final NamedGroup namedGroup;

        XDHECredentials(XECPublicKey popPublicKey, NamedGroup namedGroup) {
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

        static XDHECredentials valueOf(NamedGroup namedGroup, byte[] encodedPoint) throws IOException, GeneralSecurityException {
            if (namedGroup.spec != NamedGroup.NamedGroupSpec.NAMED_GROUP_XDH) {
                throw new RuntimeException("Credentials decoding:  Not XDH named group");
            }
            if (encodedPoint == null || encodedPoint.length == 0) {
                return null;
            }
            byte[] uBytes = (byte[])encodedPoint.clone();
            Utilities.reverseBytes(uBytes);
            BigInteger u = new BigInteger(1, uBytes);
            XECPublicKeySpec xecPublicKeySpec = new XECPublicKeySpec(new NamedParameterSpec(namedGroup.name), u);
            KeyFactory factory = KeyFactory.getInstance(namedGroup.algorithm);
            XECPublicKey publicKey = (XECPublicKey)factory.generatePublic(xecPublicKeySpec);
            return new XDHECredentials(publicKey, namedGroup);
        }
    }
}

