/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.Locale;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import sun.security.ssl.Alert;
import sun.security.ssl.Authenticator;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeOutStream;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.PostHandshakeContext;
import sun.security.ssl.SSLCipher;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLKeyDerivation;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLProducer;
import sun.security.ssl.SSLTrafficKeyDerivation;

final class KeyUpdate {
    static final SSLProducer kickstartProducer = new KeyUpdateKickstartProducer();
    static final SSLConsumer handshakeConsumer = new KeyUpdateConsumer();
    static final HandshakeProducer handshakeProducer = new KeyUpdateProducer();

    KeyUpdate() {
    }

    private static final class KeyUpdateKickstartProducer
    implements SSLProducer {
        private KeyUpdateKickstartProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context) throws IOException {
            PostHandshakeContext hc = (PostHandshakeContext)context;
            return handshakeProducer.produce(context, new KeyUpdateMessage(hc, hc.conContext.isInboundClosed() ? KeyUpdateRequest.NOTREQUESTED : KeyUpdateRequest.REQUESTED));
        }
    }

    private static final class KeyUpdateConsumer
    implements SSLConsumer {
        private KeyUpdateConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
            SSLTrafficKeyDerivation kdg;
            PostHandshakeContext hc = (PostHandshakeContext)context;
            KeyUpdateMessage km = new KeyUpdateMessage(hc, message);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Consuming KeyUpdate post-handshake message", km);
            }
            if ((kdg = SSLTrafficKeyDerivation.valueOf(hc.conContext.protocolVersion)) == null) {
                throw hc.conContext.fatal(Alert.INTERNAL_ERROR, "Not supported key derivation: " + (Object)((Object)hc.conContext.protocolVersion));
            }
            SSLKeyDerivation skd = kdg.createKeyDerivation(hc, hc.conContext.inputRecord.readCipher.baseSecret);
            if (skd == null) {
                throw hc.conContext.fatal(Alert.INTERNAL_ERROR, "no key derivation");
            }
            SecretKey nplus1 = skd.deriveKey("TlsUpdateNplus1", null);
            SSLKeyDerivation kd = kdg.createKeyDerivation(hc, nplus1);
            SecretKey key = kd.deriveKey("TlsKey", null);
            IvParameterSpec ivSpec = new IvParameterSpec(kd.deriveKey("TlsIv", null).getEncoded());
            try {
                SSLCipher.SSLReadCipher rc = hc.negotiatedCipherSuite.bulkCipher.createReadCipher(Authenticator.valueOf(hc.conContext.protocolVersion), hc.conContext.protocolVersion, key, ivSpec, hc.sslContext.getSecureRandom());
                if (rc == null) {
                    throw hc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Illegal cipher suite (" + (Object)((Object)hc.negotiatedCipherSuite) + ") and protocol version (" + (Object)((Object)hc.negotiatedProtocol) + ")");
                }
                rc.baseSecret = nplus1;
                hc.conContext.inputRecord.changeReadCiphers(rc);
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.fine("KeyUpdate: read key updated", new Object[0]);
                }
            }
            catch (GeneralSecurityException gse) {
                throw hc.conContext.fatal(Alert.INTERNAL_ERROR, "Failure to derive read secrets", gse);
            }
            if (km.status == KeyUpdateRequest.REQUESTED) {
                handshakeProducer.produce(hc, new KeyUpdateMessage(hc, KeyUpdateRequest.NOTREQUESTED));
                return;
            }
            hc.conContext.finishPostHandshake();
        }
    }

    private static final class KeyUpdateProducer
    implements HandshakeProducer {
        private KeyUpdateProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            SSLCipher.SSLWriteCipher wc;
            SSLTrafficKeyDerivation kdg;
            PostHandshakeContext hc = (PostHandshakeContext)context;
            KeyUpdateMessage km = (KeyUpdateMessage)message;
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced KeyUpdate post-handshake message", km);
            }
            if ((kdg = SSLTrafficKeyDerivation.valueOf(hc.conContext.protocolVersion)) == null) {
                throw hc.conContext.fatal(Alert.INTERNAL_ERROR, "Not supported key derivation: " + (Object)((Object)hc.conContext.protocolVersion));
            }
            SSLKeyDerivation skd = kdg.createKeyDerivation(hc, hc.conContext.outputRecord.writeCipher.baseSecret);
            if (skd == null) {
                throw hc.conContext.fatal(Alert.INTERNAL_ERROR, "no key derivation");
            }
            SecretKey nplus1 = skd.deriveKey("TlsUpdateNplus1", null);
            SSLKeyDerivation kd = kdg.createKeyDerivation(hc, nplus1);
            SecretKey key = kd.deriveKey("TlsKey", null);
            IvParameterSpec ivSpec = new IvParameterSpec(kd.deriveKey("TlsIv", null).getEncoded());
            try {
                wc = hc.negotiatedCipherSuite.bulkCipher.createWriteCipher(Authenticator.valueOf(hc.conContext.protocolVersion), hc.conContext.protocolVersion, key, ivSpec, hc.sslContext.getSecureRandom());
            }
            catch (GeneralSecurityException gse) {
                throw hc.conContext.fatal(Alert.INTERNAL_ERROR, "Failure to derive write secrets", gse);
            }
            if (wc == null) {
                throw hc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Illegal cipher suite (" + (Object)((Object)hc.negotiatedCipherSuite) + ") and protocol version (" + (Object)((Object)hc.negotiatedProtocol) + ")");
            }
            wc.baseSecret = nplus1;
            hc.conContext.outputRecord.changeWriteCiphers(wc, km.status.id);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                SSLLogger.fine("KeyUpdate: write key updated", new Object[0]);
            }
            hc.conContext.finishPostHandshake();
            return null;
        }
    }

    static enum KeyUpdateRequest {
        NOTREQUESTED(0, "update_not_requested"),
        REQUESTED(1, "update_requested");

        final byte id;
        final String name;

        private KeyUpdateRequest(byte id, String name) {
            this.id = id;
            this.name = name;
        }

        static KeyUpdateRequest valueOf(byte id) {
            for (KeyUpdateRequest kur : KeyUpdateRequest.values()) {
                if (kur.id != id) continue;
                return kur;
            }
            return null;
        }

        static String nameOf(byte id) {
            for (KeyUpdateRequest kur : KeyUpdateRequest.values()) {
                if (kur.id != id) continue;
                return kur.name;
            }
            return "<UNKNOWN KeyUpdateRequest TYPE: " + (id & 0xFF) + ">";
        }
    }

    static final class KeyUpdateMessage
    extends SSLHandshake.HandshakeMessage {
        private final KeyUpdateRequest status;

        KeyUpdateMessage(PostHandshakeContext context, KeyUpdateRequest status) {
            super(context);
            this.status = status;
        }

        KeyUpdateMessage(PostHandshakeContext context, ByteBuffer m) throws IOException {
            super(context);
            if (m.remaining() != 1) {
                throw context.conContext.fatal(Alert.ILLEGAL_PARAMETER, "KeyUpdate has an unexpected length of " + m.remaining());
            }
            byte request = m.get();
            this.status = KeyUpdateRequest.valueOf(request);
            if (this.status == null) {
                throw context.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Invalid KeyUpdate message value: " + KeyUpdateRequest.nameOf(request));
            }
        }

        @Override
        public SSLHandshake handshakeType() {
            return SSLHandshake.KEY_UPDATE;
        }

        @Override
        public int messageLength() {
            return 1;
        }

        @Override
        public void send(HandshakeOutStream s) throws IOException {
            s.putInt8(this.status.id);
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"KeyUpdate\": '{'\n  \"request_update\": {0}\n'}'", Locale.ENGLISH);
            Object[] messageFields = new Object[]{this.status.name};
            return messageFormat.format(messageFields);
        }
    }
}

