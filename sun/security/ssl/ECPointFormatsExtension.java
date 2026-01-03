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
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.NamedGroup;
import sun.security.ssl.Record;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLStringizer;
import sun.security.ssl.ServerHandshakeContext;

final class ECPointFormatsExtension {
    static final HandshakeProducer chNetworkProducer = new CHECPointFormatsProducer();
    static final SSLExtension.ExtensionConsumer chOnLoadConsumer = new CHECPointFormatsConsumer();
    static final SSLExtension.ExtensionConsumer shOnLoadConsumer = new SHECPointFormatsConsumer();
    static final SSLStringizer epfStringizer = new ECPointFormatsStringizer();

    ECPointFormatsExtension() {
    }

    private static final class CHECPointFormatsProducer
    implements HandshakeProducer {
        private CHECPointFormatsProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.CH_EC_POINT_FORMATS)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable ec_point_formats extension", new Object[0]);
                }
                return null;
            }
            if (NamedGroup.NamedGroupSpec.NAMED_GROUP_ECDHE.isSupported(chc.activeCipherSuites)) {
                byte[] extData = new byte[]{1, 0};
                chc.handshakeExtensions.put(SSLExtension.CH_EC_POINT_FORMATS, ECPointFormatsSpec.DEFAULT);
                return extData;
            }
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Need no ec_point_formats extension", new Object[0]);
            }
            return null;
        }
    }

    private static final class CHECPointFormatsConsumer
    implements SSLExtension.ExtensionConsumer {
        private CHECPointFormatsConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.CH_EC_POINT_FORMATS)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable ec_point_formats extension", new Object[0]);
                }
                return;
            }
            ECPointFormatsSpec spec = new ECPointFormatsSpec(shc, buffer);
            if (!spec.hasUncompressedFormat()) {
                throw shc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Invalid ec_point_formats extension data: peer does not support uncompressed points");
            }
            shc.handshakeExtensions.put(SSLExtension.CH_EC_POINT_FORMATS, spec);
        }
    }

    private static final class SHECPointFormatsConsumer
    implements SSLExtension.ExtensionConsumer {
        private SHECPointFormatsConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            ECPointFormatsSpec requestedSpec = (ECPointFormatsSpec)chc.handshakeExtensions.get(SSLExtension.CH_EC_POINT_FORMATS);
            if (requestedSpec == null) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected ec_point_formats extension in ServerHello");
            }
            ECPointFormatsSpec spec = new ECPointFormatsSpec(chc, buffer);
            if (!spec.hasUncompressedFormat()) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Invalid ec_point_formats extension data: peer does not support uncompressed points");
            }
            chc.handshakeExtensions.put(SSLExtension.CH_EC_POINT_FORMATS, spec);
        }
    }

    private static final class ECPointFormatsStringizer
    implements SSLStringizer {
        private ECPointFormatsStringizer() {
        }

        @Override
        public String toString(HandshakeContext hc, ByteBuffer buffer) {
            try {
                return new ECPointFormatsSpec(hc, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }

    private static enum ECPointFormat {
        UNCOMPRESSED(0, "uncompressed"),
        ANSIX962_COMPRESSED_PRIME(1, "ansiX962_compressed_prime"),
        FMT_ANSIX962_COMPRESSED_CHAR2(2, "ansiX962_compressed_char2");

        final byte id;
        final String name;

        private ECPointFormat(byte id, String name) {
            this.id = id;
            this.name = name;
        }

        static String nameOf(int id) {
            for (ECPointFormat pf : ECPointFormat.values()) {
                if (pf.id != id) continue;
                return pf.name;
            }
            return "UNDEFINED-EC-POINT-FORMAT(" + id + ")";
        }
    }

    static class ECPointFormatsSpec
    implements SSLExtension.SSLExtensionSpec {
        static final ECPointFormatsSpec DEFAULT = new ECPointFormatsSpec(new byte[]{ECPointFormat.UNCOMPRESSED.id});
        final byte[] formats;

        ECPointFormatsSpec(byte[] formats) {
            this.formats = formats;
        }

        private ECPointFormatsSpec(HandshakeContext hc, ByteBuffer m) throws IOException {
            if (!m.hasRemaining()) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid ec_point_formats extension: insufficient data"));
            }
            this.formats = Record.getBytes8(m);
        }

        private boolean hasUncompressedFormat() {
            for (byte format : this.formats) {
                if (format != ECPointFormat.UNCOMPRESSED.id) continue;
                return true;
            }
            return false;
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"formats\": '['{0}']'", Locale.ENGLISH);
            if (this.formats == null || this.formats.length == 0) {
                Object[] messageFields = new Object[]{"<no EC point format specified>"};
                return messageFormat.format(messageFields);
            }
            StringBuilder builder = new StringBuilder(512);
            boolean isFirst = true;
            for (byte pf : this.formats) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    builder.append(", ");
                }
                builder.append(ECPointFormat.nameOf(pf));
            }
            Object[] messageFields = new Object[]{builder.toString()};
            return messageFormat.format(messageFields);
        }
    }
}

