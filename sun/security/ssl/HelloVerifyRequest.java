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
import sun.security.ssl.ClientHello;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeOutStream;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.HelloCookieManager;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.Record;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.ServerHandshakeContext;
import sun.security.ssl.Utilities;

final class HelloVerifyRequest {
    static final SSLConsumer handshakeConsumer = new HelloVerifyRequestConsumer();
    static final HandshakeProducer handshakeProducer = new HelloVerifyRequestProducer();

    HelloVerifyRequest() {
    }

    private static final class HelloVerifyRequestConsumer
    implements SSLConsumer {
        private HelloVerifyRequestConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            chc.handshakeConsumers.remove(SSLHandshake.HELLO_VERIFY_REQUEST.id);
            if (!chc.handshakeConsumers.isEmpty()) {
                chc.handshakeConsumers.remove(SSLHandshake.SERVER_HELLO.id);
            }
            if (!chc.handshakeConsumers.isEmpty()) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "No more message expected before HelloVerifyRequest is processed");
            }
            chc.handshakeHash.finish();
            HelloVerifyRequestMessage hvrm = new HelloVerifyRequestMessage((HandshakeContext)chc, message);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Consuming HelloVerifyRequest handshake message", hvrm);
            }
            chc.initialClientHelloMsg.setHelloCookie(hvrm.cookie);
            SSLHandshake.CLIENT_HELLO.produce(context, hvrm);
        }
    }

    private static final class HelloVerifyRequestProducer
    implements HandshakeProducer {
        private HelloVerifyRequestProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            shc.handshakeProducers.remove(SSLHandshake.HELLO_VERIFY_REQUEST.id);
            HelloVerifyRequestMessage hvrm = new HelloVerifyRequestMessage((HandshakeContext)shc, message);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced HelloVerifyRequest handshake message", hvrm);
            }
            hvrm.write(shc.handshakeOutput);
            shc.handshakeOutput.flush();
            shc.handshakeHash.finish();
            shc.handshakeExtensions.clear();
            shc.handshakeConsumers.put(SSLHandshake.CLIENT_HELLO.id, SSLHandshake.CLIENT_HELLO);
            return null;
        }
    }

    static final class HelloVerifyRequestMessage
    extends SSLHandshake.HandshakeMessage {
        final int serverVersion;
        final byte[] cookie;

        HelloVerifyRequestMessage(HandshakeContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            super(context);
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            ClientHello.ClientHelloMessage clientHello = (ClientHello.ClientHelloMessage)message;
            HelloCookieManager hcMgr = shc.sslContext.getHelloCookieManager(ProtocolVersion.DTLS10);
            this.serverVersion = shc.clientHelloVersion;
            this.cookie = hcMgr.createCookie(shc, clientHello);
        }

        HelloVerifyRequestMessage(HandshakeContext context, ByteBuffer m) throws IOException {
            super(context);
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (m.remaining() < 3) {
                throw chc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Invalid HelloVerifyRequest: no sufficient data");
            }
            byte major = m.get();
            byte minor = m.get();
            this.serverVersion = (major & 0xFF) << 8 | minor & 0xFF;
            this.cookie = Record.getBytes8(m);
        }

        @Override
        public SSLHandshake handshakeType() {
            return SSLHandshake.HELLO_VERIFY_REQUEST;
        }

        @Override
        public int messageLength() {
            return 3 + this.cookie.length;
        }

        @Override
        public void send(HandshakeOutStream hos) throws IOException {
            hos.putInt8((byte)(this.serverVersion >>> 8 & 0xFF));
            hos.putInt8((byte)(this.serverVersion & 0xFF));
            hos.putBytes8(this.cookie);
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"HelloVerifyRequest\": '{'\n  \"server version\"      : \"{0}\",\n  \"cookie\"              : \"{1}\",\n'}'", Locale.ENGLISH);
            Object[] messageFields = new Object[]{ProtocolVersion.nameOf(this.serverVersion), Utilities.toHexString(this.cookie)};
            return messageFormat.format(messageFields);
        }
    }
}

