/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import sun.security.ssl.Alert;
import sun.security.ssl.CertificateStatus;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLKeyExchange;
import sun.security.ssl.ServerHandshakeContext;

final class ServerKeyExchange {
    static final SSLConsumer handshakeConsumer = new ServerKeyExchangeConsumer();
    static final HandshakeProducer handshakeProducer = new ServerKeyExchangeProducer();

    ServerKeyExchange() {
    }

    private static final class ServerKeyExchangeConsumer
    implements SSLConsumer {
        private ServerKeyExchangeConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
            SSLKeyExchange ke;
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            chc.handshakeConsumers.remove(SSLHandshake.SERVER_KEY_EXCHANGE.id);
            if (chc.receivedCertReq) {
                chc.receivedCertReq = false;
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected ServerKeyExchange handshake message");
            }
            SSLConsumer certStatCons = (SSLConsumer)chc.handshakeConsumers.remove(SSLHandshake.CERTIFICATE_STATUS.id);
            if (certStatCons != null) {
                CertificateStatus.handshakeAbsence.absent(context, null);
            }
            if ((ke = SSLKeyExchange.valueOf(chc.negotiatedCipherSuite.keyExchange, chc.negotiatedProtocol)) != null) {
                for (Map.Entry<Byte, SSLConsumer> hc : ke.getHandshakeConsumers(chc)) {
                    if (hc.getKey() != SSLHandshake.SERVER_KEY_EXCHANGE.id) continue;
                    hc.getValue().consume(context, message);
                    return;
                }
            }
            throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected ServerKeyExchange handshake message.");
        }
    }

    private static final class ServerKeyExchangeProducer
    implements HandshakeProducer {
        private ServerKeyExchangeProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            SSLKeyExchange ke = SSLKeyExchange.valueOf(shc.negotiatedCipherSuite.keyExchange, shc.negotiatedProtocol);
            if (ke != null) {
                for (Map.Entry<Byte, HandshakeProducer> hc : ke.getHandshakeProducers(shc)) {
                    if (hc.getKey() != SSLHandshake.SERVER_KEY_EXCHANGE.id) continue;
                    return hc.getValue().produce(context, message);
                }
            }
            throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "No ServerKeyExchange handshake message can be produced.");
        }
    }
}

