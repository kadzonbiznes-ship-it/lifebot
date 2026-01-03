/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import sun.security.ssl.Alert;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeOutStream;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLProducer;
import sun.security.ssl.ServerHandshakeContext;

final class HelloRequest {
    static final SSLProducer kickstartProducer = new HelloRequestKickstartProducer();
    static final SSLConsumer handshakeConsumer = new HelloRequestConsumer();
    static final HandshakeProducer handshakeProducer = new HelloRequestProducer();

    HelloRequest() {
    }

    private static final class HelloRequestKickstartProducer
    implements SSLProducer {
        private HelloRequestKickstartProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            HelloRequestMessage hrm = new HelloRequestMessage(shc);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced HelloRequest handshake message", hrm);
            }
            hrm.write(shc.handshakeOutput);
            shc.handshakeOutput.flush();
            shc.handshakeConsumers.put(SSLHandshake.CLIENT_HELLO.id, SSLHandshake.CLIENT_HELLO);
            return null;
        }
    }

    private static final class HelloRequestConsumer
    implements SSLConsumer {
        private HelloRequestConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            HelloRequestMessage hrm = new HelloRequestMessage(chc, message);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Consuming HelloRequest handshake message", hrm);
            }
            if (!chc.kickstartMessageDelivered) {
                if (!chc.conContext.secureRenegotiation && !HandshakeContext.allowUnsafeRenegotiation) {
                    throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Unsafe renegotiation is not allowed");
                }
                if (!chc.conContext.secureRenegotiation && SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("Continue with insecure renegotiation", new Object[0]);
                }
                chc.handshakeProducers.put(SSLHandshake.CLIENT_HELLO.id, SSLHandshake.CLIENT_HELLO);
                SSLHandshake.CLIENT_HELLO.produce(context, hrm);
            } else if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Ignore HelloRequest, handshaking is in progress", new Object[0]);
            }
        }
    }

    private static final class HelloRequestProducer
    implements HandshakeProducer {
        private HelloRequestProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            HelloRequestMessage hrm = new HelloRequestMessage(shc);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced HelloRequest handshake message", hrm);
            }
            hrm.write(shc.handshakeOutput);
            shc.handshakeOutput.flush();
            shc.handshakeConsumers.put(SSLHandshake.CLIENT_HELLO.id, SSLHandshake.CLIENT_HELLO);
            return null;
        }
    }

    static final class HelloRequestMessage
    extends SSLHandshake.HandshakeMessage {
        HelloRequestMessage(HandshakeContext handshakeContext) {
            super(handshakeContext);
        }

        HelloRequestMessage(HandshakeContext handshakeContext, ByteBuffer m) throws IOException {
            super(handshakeContext);
            if (m.hasRemaining()) {
                throw handshakeContext.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Error parsing HelloRequest message: not empty");
            }
        }

        @Override
        public SSLHandshake handshakeType() {
            return SSLHandshake.HELLO_REQUEST;
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

