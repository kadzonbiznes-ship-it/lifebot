/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Locale;
import sun.security.ssl.Alert;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeOutStream;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLExtensions;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.ServerHandshakeContext;
import sun.security.ssl.Utilities;

final class EncryptedExtensions {
    static final HandshakeProducer handshakeProducer = new EncryptedExtensionsProducer();
    static final SSLConsumer handshakeConsumer = new EncryptedExtensionsConsumer();

    EncryptedExtensions() {
    }

    private static final class EncryptedExtensionsProducer
    implements HandshakeProducer {
        private EncryptedExtensionsProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            EncryptedExtensionsMessage eem = new EncryptedExtensionsMessage(shc);
            SSLExtension[] extTypes = shc.sslConfig.getEnabledExtensions(SSLHandshake.ENCRYPTED_EXTENSIONS, shc.negotiatedProtocol);
            eem.extensions.produce(shc, extTypes);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced EncryptedExtensions message", eem);
            }
            eem.write(shc.handshakeOutput);
            shc.handshakeOutput.flush();
            return null;
        }
    }

    private static final class EncryptedExtensionsConsumer
    implements SSLConsumer {
        private EncryptedExtensionsConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            chc.handshakeConsumers.remove(SSLHandshake.ENCRYPTED_EXTENSIONS.id);
            EncryptedExtensionsMessage eem = new EncryptedExtensionsMessage(chc, message);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Consuming EncryptedExtensions handshake message", eem);
            }
            SSLExtension[] extTypes = chc.sslConfig.getEnabledExtensions(SSLHandshake.ENCRYPTED_EXTENSIONS);
            eem.extensions.consumeOnLoad(chc, extTypes);
            eem.extensions.consumeOnTrade(chc, extTypes);
        }
    }

    static final class EncryptedExtensionsMessage
    extends SSLHandshake.HandshakeMessage {
        private final SSLExtensions extensions;

        EncryptedExtensionsMessage(HandshakeContext handshakeContext) {
            super(handshakeContext);
            this.extensions = new SSLExtensions(this);
        }

        EncryptedExtensionsMessage(HandshakeContext handshakeContext, ByteBuffer m) throws IOException {
            super(handshakeContext);
            if (m.remaining() < 2) {
                throw handshakeContext.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Invalid EncryptedExtensions handshake message: no sufficient data");
            }
            SSLExtension[] encryptedExtensions = handshakeContext.sslConfig.getEnabledExtensions(SSLHandshake.ENCRYPTED_EXTENSIONS);
            this.extensions = new SSLExtensions(this, m, encryptedExtensions);
        }

        @Override
        SSLHandshake handshakeType() {
            return SSLHandshake.ENCRYPTED_EXTENSIONS;
        }

        @Override
        int messageLength() {
            int extLen = this.extensions.length();
            if (extLen == 0) {
                extLen = 2;
            }
            return extLen;
        }

        @Override
        void send(HandshakeOutStream hos) throws IOException {
            if (this.extensions.length() == 0) {
                hos.putInt16(0);
            } else {
                this.extensions.send(hos);
            }
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"EncryptedExtensions\": [\n{0}\n]", Locale.ENGLISH);
            Object[] messageFields = new Object[]{Utilities.indent(this.extensions.toString())};
            return messageFormat.format(messageFields);
        }
    }
}

