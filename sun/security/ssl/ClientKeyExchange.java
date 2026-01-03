/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import sun.security.ssl.Alert;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLKeyExchange;
import sun.security.ssl.ServerHandshakeContext;

final class ClientKeyExchange {
    static final SSLConsumer handshakeConsumer = new ClientKeyExchangeConsumer();
    static final HandshakeProducer handshakeProducer = new ClientKeyExchangeProducer();

    ClientKeyExchange() {
    }

    private static final class ClientKeyExchangeConsumer
    implements SSLConsumer {
        private ClientKeyExchangeConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            shc.handshakeConsumers.remove(SSLHandshake.CLIENT_KEY_EXCHANGE.id);
            if (shc.handshakeConsumers.containsKey(SSLHandshake.CERTIFICATE.id)) {
                throw shc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected ClientKeyExchange handshake message.");
            }
            SSLKeyExchange ke = SSLKeyExchange.valueOf(shc.negotiatedCipherSuite.keyExchange, shc.negotiatedProtocol);
            if (ke != null) {
                for (Map.Entry<Byte, SSLConsumer> hc : ke.getHandshakeConsumers(shc)) {
                    if (hc.getKey() != SSLHandshake.CLIENT_KEY_EXCHANGE.id) continue;
                    hc.getValue().consume(context, message);
                    return;
                }
            }
            throw shc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected ClientKeyExchange handshake message.");
        }
    }

    private static final class ClientKeyExchangeProducer
    implements HandshakeProducer {
        private ClientKeyExchangeProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            SSLKeyExchange ke = SSLKeyExchange.valueOf(chc.negotiatedCipherSuite.keyExchange, chc.negotiatedProtocol);
            if (ke != null) {
                for (Map.Entry<Byte, HandshakeProducer> hp : ke.getHandshakeProducers(chc)) {
                    if (hp.getKey() != SSLHandshake.CLIENT_KEY_EXCHANGE.id) continue;
                    return hp.getValue().produce(context, message);
                }
            }
            throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected ClientKeyExchange handshake message.");
        }
    }
}

