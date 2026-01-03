/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import sun.security.ssl.Alert;
import sun.security.ssl.CertificateStatus;
import sun.security.ssl.ChangeCipherSpec;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.ContentType;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeOutStream;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.ServerHandshakeContext;

final class ServerHelloDone {
    static final SSLConsumer handshakeConsumer = new ServerHelloDoneConsumer();
    static final HandshakeProducer handshakeProducer = new ServerHelloDoneProducer();

    ServerHelloDone() {
    }

    private static final class ServerHelloDoneConsumer
    implements SSLConsumer {
        private ServerHelloDoneConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
            SSLHandshake[] probableHandshakeMessages;
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            SSLConsumer certStatCons = (SSLConsumer)chc.handshakeConsumers.remove(SSLHandshake.CERTIFICATE_STATUS.id);
            if (certStatCons != null) {
                CertificateStatus.handshakeAbsence.absent(context, null);
            }
            chc.handshakeConsumers.clear();
            ServerHelloDoneMessage shdm = new ServerHelloDoneMessage(chc, message);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Consuming ServerHelloDone handshake message", shdm);
            }
            chc.handshakeProducers.put(SSLHandshake.CLIENT_KEY_EXCHANGE.id, SSLHandshake.CLIENT_KEY_EXCHANGE);
            chc.handshakeProducers.put(SSLHandshake.FINISHED.id, SSLHandshake.FINISHED);
            for (SSLHandshake hs : probableHandshakeMessages = new SSLHandshake[]{SSLHandshake.CERTIFICATE, SSLHandshake.CLIENT_KEY_EXCHANGE, SSLHandshake.CERTIFICATE_VERIFY, SSLHandshake.FINISHED}) {
                HandshakeProducer handshakeProducer = (HandshakeProducer)chc.handshakeProducers.remove(hs.id);
                if (handshakeProducer == null) continue;
                handshakeProducer.produce(context, null);
            }
        }
    }

    private static final class ServerHelloDoneProducer
    implements HandshakeProducer {
        private ServerHelloDoneProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            ServerHelloDoneMessage shdm = new ServerHelloDoneMessage(shc);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced ServerHelloDone handshake message", shdm);
            }
            shdm.write(shc.handshakeOutput);
            shc.handshakeOutput.flush();
            shc.handshakeConsumers.put(SSLHandshake.CLIENT_KEY_EXCHANGE.id, SSLHandshake.CLIENT_KEY_EXCHANGE);
            shc.conContext.consumers.put(ContentType.CHANGE_CIPHER_SPEC.id, ChangeCipherSpec.t10Consumer);
            shc.handshakeConsumers.put(SSLHandshake.FINISHED.id, SSLHandshake.FINISHED);
            return null;
        }
    }

    static final class ServerHelloDoneMessage
    extends SSLHandshake.HandshakeMessage {
        ServerHelloDoneMessage(HandshakeContext handshakeContext) {
            super(handshakeContext);
        }

        ServerHelloDoneMessage(HandshakeContext handshakeContext, ByteBuffer m) throws IOException {
            super(handshakeContext);
            if (m.hasRemaining()) {
                throw handshakeContext.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Error parsing ServerHelloDone message: not empty");
            }
        }

        @Override
        public SSLHandshake handshakeType() {
            return SSLHandshake.SERVER_HELLO_DONE;
        }

        @Override
        public int messageLength() {
            return 0;
        }

        @Override
        public void send(HandshakeOutStream s) throws IOException {
        }

        public String toString() {
            return "<empty>";
        }
    }
}

