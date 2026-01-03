/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.net.ssl.SSLException;
import sun.security.ssl.Alert;
import sun.security.ssl.Authenticator;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.CipherType;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.ContentType;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.SSLCipher;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLKeyDerivation;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLTrafficKeyDerivation;
import sun.security.ssl.TransportContext;

final class ChangeCipherSpec {
    static final SSLConsumer t10Consumer = new T10ChangeCipherSpecConsumer();
    static final HandshakeProducer t10Producer = new T10ChangeCipherSpecProducer();
    static final SSLConsumer t13Consumer = new T13ChangeCipherSpecConsumer();

    ChangeCipherSpec() {
    }

    private static final class T10ChangeCipherSpecConsumer
    implements SSLConsumer {
        private T10ChangeCipherSpecConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
            SSLCipher.SSLReadCipher readCipher;
            TransportContext tc = (TransportContext)context;
            tc.consumers.remove(ContentType.CHANGE_CIPHER_SPEC.id);
            if (message.remaining() != 1 || message.get() != 1) {
                throw tc.fatal(Alert.UNEXPECTED_MESSAGE, "Malformed or unexpected ChangeCipherSpec message");
            }
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Consuming ChangeCipherSpec message", new Object[0]);
            }
            if (tc.handshakeContext == null) {
                throw tc.fatal(Alert.HANDSHAKE_FAILURE, "Unexpected ChangeCipherSpec message");
            }
            HandshakeContext hc = tc.handshakeContext;
            if (hc.handshakeKeyDerivation == null) {
                throw tc.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected ChangeCipherSpec message");
            }
            SSLKeyDerivation kd = hc.handshakeKeyDerivation;
            if (kd instanceof SSLTrafficKeyDerivation.LegacyTrafficKeyDerivation) {
                Authenticator readAuthenticator;
                SSLTrafficKeyDerivation.LegacyTrafficKeyDerivation tkd = (SSLTrafficKeyDerivation.LegacyTrafficKeyDerivation)kd;
                CipherSuite ncs = hc.negotiatedCipherSuite;
                if (ncs.bulkCipher.cipherType == CipherType.AEAD_CIPHER) {
                    readAuthenticator = Authenticator.valueOf(hc.negotiatedProtocol);
                } else {
                    try {
                        readAuthenticator = Authenticator.valueOf(hc.negotiatedProtocol, ncs.macAlg, tkd.getTrafficKey(hc.sslConfig.isClientMode ? "serverMacKey" : "clientMacKey"));
                    }
                    catch (InvalidKeyException | NoSuchAlgorithmException e) {
                        throw new SSLException("Algorithm missing:  ", e);
                    }
                }
                SecretKey readKey = tkd.getTrafficKey(hc.sslConfig.isClientMode ? "serverWriteKey" : "clientWriteKey");
                SecretKey readIv = tkd.getTrafficKey(hc.sslConfig.isClientMode ? "serverWriteIv" : "clientWriteIv");
                IvParameterSpec iv = readIv == null ? null : new IvParameterSpec(readIv.getEncoded());
                try {
                    readCipher = ncs.bulkCipher.createReadCipher(readAuthenticator, hc.negotiatedProtocol, readKey, iv, hc.sslContext.getSecureRandom());
                }
                catch (GeneralSecurityException gse) {
                    throw new SSLException("Algorithm missing:  ", gse);
                }
                if (readCipher == null) {
                    throw hc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Illegal cipher suite (" + (Object)((Object)hc.negotiatedCipherSuite) + ") and protocol version (" + (Object)((Object)hc.negotiatedProtocol) + ")");
                }
            } else {
                throw new UnsupportedOperationException("Not supported.");
            }
            tc.inputRecord.changeReadCiphers(readCipher);
        }
    }

    private static final class T10ChangeCipherSpecProducer
    implements HandshakeProducer {
        private T10ChangeCipherSpecProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            SSLCipher.SSLWriteCipher writeCipher;
            Authenticator writeAuthenticator;
            HandshakeContext hc = (HandshakeContext)context;
            SSLKeyDerivation kd = hc.handshakeKeyDerivation;
            if (!(kd instanceof SSLTrafficKeyDerivation.LegacyTrafficKeyDerivation)) {
                throw new UnsupportedOperationException("Not supported.");
            }
            SSLTrafficKeyDerivation.LegacyTrafficKeyDerivation tkd = (SSLTrafficKeyDerivation.LegacyTrafficKeyDerivation)kd;
            CipherSuite ncs = hc.negotiatedCipherSuite;
            if (ncs.bulkCipher.cipherType == CipherType.AEAD_CIPHER) {
                writeAuthenticator = Authenticator.valueOf(hc.negotiatedProtocol);
            } else {
                try {
                    writeAuthenticator = Authenticator.valueOf(hc.negotiatedProtocol, ncs.macAlg, tkd.getTrafficKey(hc.sslConfig.isClientMode ? "clientMacKey" : "serverMacKey"));
                }
                catch (InvalidKeyException | NoSuchAlgorithmException e) {
                    throw new SSLException("Algorithm missing:  ", e);
                }
            }
            SecretKey writeKey = tkd.getTrafficKey(hc.sslConfig.isClientMode ? "clientWriteKey" : "serverWriteKey");
            SecretKey writeIv = tkd.getTrafficKey(hc.sslConfig.isClientMode ? "clientWriteIv" : "serverWriteIv");
            IvParameterSpec iv = writeIv == null ? null : new IvParameterSpec(writeIv.getEncoded());
            try {
                writeCipher = ncs.bulkCipher.createWriteCipher(writeAuthenticator, hc.negotiatedProtocol, writeKey, iv, hc.sslContext.getSecureRandom());
            }
            catch (GeneralSecurityException gse) {
                throw new SSLException("Algorithm missing:  ", gse);
            }
            if (writeCipher == null) {
                throw hc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Illegal cipher suite (" + (Object)((Object)ncs) + ") and protocol version (" + (Object)((Object)hc.negotiatedProtocol) + ")");
            }
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced ChangeCipherSpec message", new Object[0]);
            }
            hc.conContext.outputRecord.changeWriteCiphers(writeCipher, true);
            return null;
        }
    }

    private static final class T13ChangeCipherSpecConsumer
    implements SSLConsumer {
        private T13ChangeCipherSpecConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
            TransportContext tc = (TransportContext)context;
            tc.consumers.remove(ContentType.CHANGE_CIPHER_SPEC.id);
            if (message.remaining() != 1 || message.get() != 1) {
                throw tc.fatal(Alert.UNEXPECTED_MESSAGE, "Malformed or unexpected ChangeCipherSpec message");
            }
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Consuming ChangeCipherSpec message", new Object[0]);
            }
        }
    }
}

