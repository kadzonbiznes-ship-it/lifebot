/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Locale;
import javax.net.ssl.SSLProtocolException;
import sun.security.ssl.Alert;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ClientHello;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeConsumer;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.HelloCookieManager;
import sun.security.ssl.Record;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLStringizer;
import sun.security.ssl.ServerHandshakeContext;
import sun.security.ssl.ServerHello;
import sun.security.ssl.Utilities;
import sun.security.util.HexDumpEncoder;

public class CookieExtension {
    static final HandshakeProducer chNetworkProducer = new CHCookieProducer();
    static final SSLExtension.ExtensionConsumer chOnLoadConsumer = new CHCookieConsumer();
    static final HandshakeConsumer chOnTradeConsumer = new CHCookieUpdate();
    static final HandshakeProducer hrrNetworkProducer = new HRRCookieProducer();
    static final SSLExtension.ExtensionConsumer hrrOnLoadConsumer = new HRRCookieConsumer();
    static final HandshakeProducer hrrNetworkReproducer = new HRRCookieReproducer();
    static final CookieStringizer cookieStringizer = new CookieStringizer();

    private static final class CHCookieProducer
    implements HandshakeProducer {
        private CHCookieProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.CH_COOKIE)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable cookie extension", new Object[0]);
                }
                return null;
            }
            CookieSpec spec = (CookieSpec)chc.handshakeExtensions.get(SSLExtension.HRR_COOKIE);
            if (spec != null && spec.cookie.length != 0) {
                byte[] extData = new byte[spec.cookie.length + 2];
                ByteBuffer m = ByteBuffer.wrap(extData);
                Record.putBytes16(m, spec.cookie);
                return extData;
            }
            return null;
        }
    }

    private static final class CHCookieConsumer
    implements SSLExtension.ExtensionConsumer {
        private CHCookieConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.CH_COOKIE)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable cookie extension", new Object[0]);
                }
                return;
            }
            CookieSpec spec = new CookieSpec(shc, buffer);
            shc.handshakeExtensions.put(SSLExtension.CH_COOKIE, spec);
        }
    }

    private static final class CHCookieUpdate
    implements HandshakeConsumer {
        private CHCookieUpdate() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            ClientHello.ClientHelloMessage clientHello = (ClientHello.ClientHelloMessage)message;
            CookieSpec spec = (CookieSpec)shc.handshakeExtensions.get(SSLExtension.CH_COOKIE);
            if (spec == null) {
                return;
            }
            HelloCookieManager hcm = shc.sslContext.getHelloCookieManager(shc.negotiatedProtocol);
            if (!hcm.isCookieValid(shc, clientHello, spec.cookie)) {
                throw shc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "unrecognized cookie");
            }
        }
    }

    private static final class HRRCookieProducer
    implements HandshakeProducer {
        private HRRCookieProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            ServerHello.ServerHelloMessage hrrm = (ServerHello.ServerHelloMessage)message;
            if (!shc.sslConfig.isAvailable(SSLExtension.HRR_COOKIE)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable cookie extension", new Object[0]);
                }
                return null;
            }
            HelloCookieManager hcm = shc.sslContext.getHelloCookieManager(shc.negotiatedProtocol);
            byte[] cookie = hcm.createCookie(shc, hrrm.clientHello);
            byte[] extData = new byte[cookie.length + 2];
            ByteBuffer m = ByteBuffer.wrap(extData);
            Record.putBytes16(m, cookie);
            return extData;
        }
    }

    private static final class HRRCookieConsumer
    implements SSLExtension.ExtensionConsumer {
        private HRRCookieConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.HRR_COOKIE)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable cookie extension", new Object[0]);
                }
                return;
            }
            CookieSpec spec = new CookieSpec(chc, buffer);
            chc.handshakeExtensions.put(SSLExtension.HRR_COOKIE, spec);
        }
    }

    private static final class HRRCookieReproducer
    implements HandshakeProducer {
        private HRRCookieReproducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.HRR_COOKIE)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable cookie extension", new Object[0]);
                }
                return null;
            }
            CookieSpec spec = (CookieSpec)shc.handshakeExtensions.get(SSLExtension.CH_COOKIE);
            if (spec != null && spec.cookie.length != 0) {
                byte[] extData = new byte[spec.cookie.length + 2];
                ByteBuffer m = ByteBuffer.wrap(extData);
                Record.putBytes16(m, spec.cookie);
                return extData;
            }
            return null;
        }
    }

    private static final class CookieStringizer
    implements SSLStringizer {
        private CookieStringizer() {
        }

        @Override
        public String toString(HandshakeContext hc, ByteBuffer buffer) {
            try {
                return new CookieSpec(hc, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }

    static class CookieSpec
    implements SSLExtension.SSLExtensionSpec {
        final byte[] cookie;

        private CookieSpec(HandshakeContext hc, ByteBuffer m) throws IOException {
            if (m.remaining() < 3) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid cookie extension: insufficient data"));
            }
            this.cookie = Record.getBytes16(m);
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"cookie\": '{'\n{0}\n'}',", Locale.ENGLISH);
            HexDumpEncoder hexEncoder = new HexDumpEncoder();
            Object[] messageFields = new Object[]{Utilities.indent(hexEncoder.encode(this.cookie))};
            return messageFormat.format(messageFields);
        }
    }
}

