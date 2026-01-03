/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.ProviderException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLHandshakeException;
import sun.security.internal.spec.TlsKeyMaterialParameterSpec;
import sun.security.internal.spec.TlsKeyMaterialSpec;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.CipherType;
import sun.security.ssl.HKDF;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.Record;
import sun.security.ssl.SSLCipher;
import sun.security.ssl.SSLKeyDerivation;
import sun.security.ssl.SSLKeyDerivationGenerator;

enum SSLTrafficKeyDerivation implements SSLKeyDerivationGenerator
{
    SSL30("kdf_ssl30", new S30TrafficKeyDerivationGenerator()),
    TLS10("kdf_tls10", new T10TrafficKeyDerivationGenerator()),
    TLS12("kdf_tls12", new T12TrafficKeyDerivationGenerator()),
    TLS13("kdf_tls13", new T13TrafficKeyDerivationGenerator());

    final String name;
    final SSLKeyDerivationGenerator keyDerivationGenerator;

    private SSLTrafficKeyDerivation(String name, SSLKeyDerivationGenerator keyDerivationGenerator) {
        this.name = name;
        this.keyDerivationGenerator = keyDerivationGenerator;
    }

    static SSLTrafficKeyDerivation valueOf(ProtocolVersion protocolVersion) {
        switch (protocolVersion) {
            case SSL30: {
                return SSL30;
            }
            case TLS10: 
            case TLS11: 
            case DTLS10: {
                return TLS10;
            }
            case TLS12: 
            case DTLS12: {
                return TLS12;
            }
            case TLS13: {
                return TLS13;
            }
        }
        return null;
    }

    @Override
    public SSLKeyDerivation createKeyDerivation(HandshakeContext context, SecretKey secretKey) throws IOException {
        return this.keyDerivationGenerator.createKeyDerivation(context, secretKey);
    }

    private static final class S30TrafficKeyDerivationGenerator
    implements SSLKeyDerivationGenerator {
        private S30TrafficKeyDerivationGenerator() {
        }

        @Override
        public SSLKeyDerivation createKeyDerivation(HandshakeContext context, SecretKey secretKey) throws IOException {
            return new LegacyTrafficKeyDerivation(context, secretKey);
        }
    }

    private static final class T10TrafficKeyDerivationGenerator
    implements SSLKeyDerivationGenerator {
        private T10TrafficKeyDerivationGenerator() {
        }

        @Override
        public SSLKeyDerivation createKeyDerivation(HandshakeContext context, SecretKey secretKey) throws IOException {
            return new LegacyTrafficKeyDerivation(context, secretKey);
        }
    }

    private static final class T12TrafficKeyDerivationGenerator
    implements SSLKeyDerivationGenerator {
        private T12TrafficKeyDerivationGenerator() {
        }

        @Override
        public SSLKeyDerivation createKeyDerivation(HandshakeContext context, SecretKey secretKey) throws IOException {
            return new LegacyTrafficKeyDerivation(context, secretKey);
        }
    }

    private static final class T13TrafficKeyDerivationGenerator
    implements SSLKeyDerivationGenerator {
        private T13TrafficKeyDerivationGenerator() {
        }

        @Override
        public SSLKeyDerivation createKeyDerivation(HandshakeContext context, SecretKey secretKey) throws IOException {
            return new T13TrafficKeyDerivation(context, secretKey);
        }
    }

    static final class LegacyTrafficKeyDerivation
    implements SSLKeyDerivation {
        private final TlsKeyMaterialSpec keyMaterialSpec;

        LegacyTrafficKeyDerivation(HandshakeContext context, SecretKey masterSecret) {
            CipherSuite.HashAlg hashAlg;
            String keyMaterialAlg;
            CipherSuite cipherSuite = context.negotiatedCipherSuite;
            ProtocolVersion protocolVersion = context.negotiatedProtocol;
            int hashSize = cipherSuite.macAlg.size;
            boolean is_exportable = cipherSuite.exportable;
            SSLCipher cipher = cipherSuite.bulkCipher;
            int expandedKeySize = is_exportable ? cipher.expandedKeySize : 0;
            byte majorVersion = protocolVersion.major;
            byte minorVersion = protocolVersion.minor;
            if (protocolVersion.isDTLS) {
                if (protocolVersion.id == ProtocolVersion.DTLS10.id) {
                    majorVersion = ProtocolVersion.TLS11.major;
                    minorVersion = ProtocolVersion.TLS11.minor;
                    keyMaterialAlg = "SunTlsKeyMaterial";
                    hashAlg = CipherSuite.HashAlg.H_NONE;
                } else {
                    majorVersion = ProtocolVersion.TLS12.major;
                    minorVersion = ProtocolVersion.TLS12.minor;
                    keyMaterialAlg = "SunTls12KeyMaterial";
                    hashAlg = cipherSuite.hashAlg;
                }
            } else if (protocolVersion.id >= ProtocolVersion.TLS12.id) {
                keyMaterialAlg = "SunTls12KeyMaterial";
                hashAlg = cipherSuite.hashAlg;
            } else {
                keyMaterialAlg = "SunTlsKeyMaterial";
                hashAlg = CipherSuite.HashAlg.H_NONE;
            }
            int ivSize = cipher.ivSize;
            if (cipher.cipherType == CipherType.AEAD_CIPHER) {
                ivSize = cipher.fixedIvSize;
            } else if (cipher.cipherType == CipherType.BLOCK_CIPHER && protocolVersion.useTLS11PlusSpec()) {
                ivSize = 0;
            }
            TlsKeyMaterialParameterSpec spec = new TlsKeyMaterialParameterSpec(masterSecret, majorVersion & 0xFF, minorVersion & 0xFF, context.clientHelloRandom.randomBytes, context.serverHelloRandom.randomBytes, cipher.algorithm, cipher.keySize, expandedKeySize, ivSize, hashSize, hashAlg.name, hashAlg.hashLength, hashAlg.blockSize);
            try {
                KeyGenerator kg = KeyGenerator.getInstance(keyMaterialAlg);
                kg.init(spec);
                this.keyMaterialSpec = (TlsKeyMaterialSpec)kg.generateKey();
            }
            catch (GeneralSecurityException e) {
                throw new ProviderException(e);
            }
        }

        SecretKey getTrafficKey(String algorithm) {
            switch (algorithm) {
                case "clientMacKey": {
                    return this.keyMaterialSpec.getClientMacKey();
                }
                case "serverMacKey": {
                    return this.keyMaterialSpec.getServerMacKey();
                }
                case "clientWriteKey": {
                    return this.keyMaterialSpec.getClientCipherKey();
                }
                case "serverWriteKey": {
                    return this.keyMaterialSpec.getServerCipherKey();
                }
                case "clientWriteIv": {
                    IvParameterSpec cliIvSpec = this.keyMaterialSpec.getClientIv();
                    return cliIvSpec == null ? null : new SecretKeySpec(cliIvSpec.getIV(), "TlsIv");
                }
                case "serverWriteIv": {
                    IvParameterSpec srvIvSpec = this.keyMaterialSpec.getServerIv();
                    return srvIvSpec == null ? null : new SecretKeySpec(srvIvSpec.getIV(), "TlsIv");
                }
            }
            return null;
        }

        @Override
        public SecretKey deriveKey(String algorithm, AlgorithmParameterSpec params) throws IOException {
            return this.getTrafficKey(algorithm);
        }
    }

    private static enum KeySchedule {
        TlsKey("key", false),
        TlsIv("iv", true),
        TlsUpdateNplus1("traffic upd", false);

        private final byte[] label;
        private final boolean isIv;

        private KeySchedule(String label, boolean isIv) {
            this.label = ("tls13 " + label).getBytes();
            this.isIv = isIv;
        }

        int getKeyLength(CipherSuite cs) {
            if (this == TlsUpdateNplus1) {
                return cs.hashAlg.hashLength;
            }
            return this.isIv ? cs.bulkCipher.ivSize : cs.bulkCipher.keySize;
        }

        String getAlgorithm(CipherSuite cs, String algorithm) {
            return this.isIv ? algorithm : cs.bulkCipher.algorithm;
        }
    }

    static final class T13TrafficKeyDerivation
    implements SSLKeyDerivation {
        private final CipherSuite cs;
        private final SecretKey secret;

        T13TrafficKeyDerivation(HandshakeContext context, SecretKey secret) {
            this.secret = secret;
            this.cs = context.negotiatedCipherSuite;
        }

        @Override
        public SecretKey deriveKey(String algorithm, AlgorithmParameterSpec params) throws IOException {
            KeySchedule ks = KeySchedule.valueOf(algorithm);
            try {
                HKDF hkdf = new HKDF(this.cs.hashAlg.name);
                byte[] hkdfInfo = T13TrafficKeyDerivation.createHkdfInfo(ks.label, ks.getKeyLength(this.cs));
                return hkdf.expand(this.secret, hkdfInfo, ks.getKeyLength(this.cs), ks.getAlgorithm(this.cs, algorithm));
            }
            catch (GeneralSecurityException gse) {
                throw new SSLHandshakeException("Could not generate secret", gse);
            }
        }

        private static byte[] createHkdfInfo(byte[] label, int length) {
            byte[] info = new byte[4 + label.length];
            ByteBuffer m = ByteBuffer.wrap(info);
            try {
                Record.putInt16(m, length);
                Record.putBytes8(m, label);
                Record.putInt8(m, 0);
            }
            catch (IOException ioe) {
                throw new RuntimeException("Unexpected exception", ioe);
            }
            return info;
        }
    }
}

