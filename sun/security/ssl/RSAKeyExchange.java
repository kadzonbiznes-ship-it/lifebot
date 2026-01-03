/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLHandshakeException;
import sun.security.internal.spec.TlsRsaPremasterSecretParameterSpec;
import sun.security.ssl.Alert;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.EphemeralKeyManager;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.SSLCredentials;
import sun.security.ssl.SSLKeyAgreementGenerator;
import sun.security.ssl.SSLKeyDerivation;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLMasterKeyDerivation;
import sun.security.ssl.SSLPossession;
import sun.security.ssl.SSLPossessionGenerator;
import sun.security.ssl.ServerHandshakeContext;
import sun.security.util.KeyUtil;

final class RSAKeyExchange {
    static final SSLPossessionGenerator poGenerator = new EphemeralRSAPossessionGenerator();
    static final SSLKeyAgreementGenerator kaGenerator = new RSAKAGenerator();

    RSAKeyExchange() {
    }

    private static final class EphemeralRSAPossessionGenerator
    implements SSLPossessionGenerator {
        private EphemeralRSAPossessionGenerator() {
        }

        @Override
        public SSLPossession createPossession(HandshakeContext context) {
            try {
                EphemeralKeyManager ekm = context.sslContext.getEphemeralKeyManager();
                KeyPair kp = ekm.getRSAKeyPair(true, context.sslContext.getSecureRandom());
                if (kp != null) {
                    return new EphemeralRSAPossession(kp.getPrivate(), (RSAPublicKey)kp.getPublic());
                }
                return null;
            }
            catch (RuntimeException rte) {
                return null;
            }
        }
    }

    private static final class RSAKAGenerator
    implements SSLKeyAgreementGenerator {
        private RSAKAGenerator() {
        }

        @Override
        public SSLKeyDerivation createKeyDerivation(HandshakeContext context) throws IOException {
            RSAPremasterSecret premaster = null;
            if (context instanceof ClientHandshakeContext) {
                for (SSLPossession possession : context.handshakePossessions) {
                    if (!(possession instanceof RSAPremasterSecret)) continue;
                    premaster = (RSAPremasterSecret)possession;
                    break;
                }
            } else {
                for (SSLCredentials credential : context.handshakeCredentials) {
                    if (!(credential instanceof RSAPremasterSecret)) continue;
                    premaster = (RSAPremasterSecret)credential;
                    break;
                }
            }
            if (premaster == null) {
                throw context.conContext.fatal(Alert.HANDSHAKE_FAILURE, "No sufficient RSA key agreement parameters negotiated");
            }
            return new RSAKAKeyDerivation(context, premaster.premasterSecret);
        }

        private static final class RSAKAKeyDerivation
        implements SSLKeyDerivation {
            private final HandshakeContext context;
            private final SecretKey preMasterSecret;

            RSAKAKeyDerivation(HandshakeContext context, SecretKey preMasterSecret) {
                this.context = context;
                this.preMasterSecret = preMasterSecret;
            }

            @Override
            public SecretKey deriveKey(String algorithm, AlgorithmParameterSpec params) throws IOException {
                SSLMasterKeyDerivation mskd = SSLMasterKeyDerivation.valueOf(this.context.negotiatedProtocol);
                if (mskd == null) {
                    throw new SSLHandshakeException("No expected master key derivation for protocol: " + this.context.negotiatedProtocol.name);
                }
                SSLKeyDerivation kd = mskd.createKeyDerivation(this.context, this.preMasterSecret);
                return kd.deriveKey("MasterSecret", params);
            }
        }
    }

    static final class RSAPremasterSecret
    implements SSLPossession,
    SSLCredentials {
        final SecretKey premasterSecret;

        RSAPremasterSecret(SecretKey premasterSecret) {
            this.premasterSecret = premasterSecret;
        }

        byte[] getEncoded(PublicKey publicKey, SecureRandom secureRandom) throws GeneralSecurityException {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(3, (Key)publicKey, secureRandom);
            return cipher.wrap(this.premasterSecret);
        }

        static RSAPremasterSecret createPremasterSecret(ClientHandshakeContext chc) throws GeneralSecurityException {
            String algorithm = chc.negotiatedProtocol.useTLS12PlusSpec() ? "SunTls12RsaPremasterSecret" : "SunTlsRsaPremasterSecret";
            KeyGenerator kg = KeyGenerator.getInstance(algorithm);
            TlsRsaPremasterSecretParameterSpec spec = new TlsRsaPremasterSecretParameterSpec(chc.clientHelloVersion, chc.negotiatedProtocol.id);
            kg.init(spec, chc.sslContext.getSecureRandom());
            return new RSAPremasterSecret(kg.generateKey());
        }

        static RSAPremasterSecret decode(ServerHandshakeContext shc, PrivateKey privateKey, byte[] encrypted) throws GeneralSecurityException {
            SecretKey preMaster;
            boolean needFailover;
            byte[] encoded = null;
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            try {
                cipher.init(4, (Key)privateKey, new TlsRsaPremasterSecretParameterSpec(shc.clientHelloVersion, shc.negotiatedProtocol.id), shc.sslContext.getSecureRandom());
                needFailover = !KeyUtil.isOracleJCEProvider(cipher.getProvider().getName());
            }
            catch (UnsupportedOperationException | InvalidKeyException iue) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("The Cipher provider " + RSAPremasterSecret.safeProviderName(cipher) + " caused exception: " + iue.getMessage(), new Object[0]);
                }
                needFailover = true;
            }
            if (needFailover) {
                cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(2, privateKey);
                boolean failed = false;
                try {
                    encoded = cipher.doFinal(encrypted);
                }
                catch (BadPaddingException bpe) {
                    failed = true;
                }
                encoded = KeyUtil.checkTlsPreMasterSecretKey(shc.clientHelloVersion, shc.negotiatedProtocol.id, shc.sslContext.getSecureRandom(), encoded, failed);
                preMaster = RSAPremasterSecret.generatePremasterSecret(shc.clientHelloVersion, shc.negotiatedProtocol.id, encoded, shc.sslContext.getSecureRandom());
            } else {
                preMaster = (SecretKey)cipher.unwrap(encrypted, "TlsRsaPremasterSecret", 3);
            }
            return new RSAPremasterSecret(preMaster);
        }

        private static String safeProviderName(Cipher cipher) {
            try {
                return cipher.getProvider().toString();
            }
            catch (Exception e) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Retrieving The Cipher provider name caused exception ", e);
                }
                try {
                    return cipher.toString() + " (provider name not available)";
                }
                catch (Exception e2) {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                        SSLLogger.fine("Retrieving The Cipher name caused exception ", e2);
                    }
                    return "(cipher/provider names not available)";
                }
            }
        }

        private static SecretKey generatePremasterSecret(int clientVersion, int serverVersion, byte[] encodedSecret, SecureRandom generator) throws GeneralSecurityException {
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Generating a premaster secret", new Object[0]);
            }
            try {
                String s = clientVersion >= ProtocolVersion.TLS12.id ? "SunTls12RsaPremasterSecret" : "SunTlsRsaPremasterSecret";
                KeyGenerator kg = KeyGenerator.getInstance(s);
                kg.init(new TlsRsaPremasterSecretParameterSpec(clientVersion, serverVersion, encodedSecret), generator);
                return kg.generateKey();
            }
            catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException iae) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("RSA premaster secret generation error", iae);
                }
                throw new GeneralSecurityException("Could not generate premaster secret", iae);
            }
        }
    }

    static final class EphemeralRSACredentials
    implements SSLCredentials {
        final RSAPublicKey popPublicKey;

        EphemeralRSACredentials(RSAPublicKey popPublicKey) {
            this.popPublicKey = popPublicKey;
        }
    }

    static final class EphemeralRSAPossession
    implements SSLPossession {
        final RSAPublicKey popPublicKey;
        final PrivateKey popPrivateKey;

        EphemeralRSAPossession(PrivateKey popPrivateKey, RSAPublicKey popPublicKey) {
            this.popPublicKey = popPublicKey;
            this.popPrivateKey = popPrivateKey;
        }
    }
}

