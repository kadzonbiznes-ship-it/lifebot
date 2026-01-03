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
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.Record;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLStringizer;
import sun.security.ssl.ServerHandshakeContext;

final class SupportedVersionsExtension {
    static final HandshakeProducer chNetworkProducer = new CHSupportedVersionsProducer();
    static final SSLExtension.ExtensionConsumer chOnLoadConsumer = new CHSupportedVersionsConsumer();
    static final SSLStringizer chStringizer = new CHSupportedVersionsStringizer();
    static final HandshakeProducer shNetworkProducer = new SHSupportedVersionsProducer();
    static final SSLExtension.ExtensionConsumer shOnLoadConsumer = new SHSupportedVersionsConsumer();
    static final SSLStringizer shStringizer = new SHSupportedVersionsStringizer();
    static final HandshakeProducer hrrNetworkProducer = new HRRSupportedVersionsProducer();
    static final SSLExtension.ExtensionConsumer hrrOnLoadConsumer = new HRRSupportedVersionsConsumer();
    static final HandshakeProducer hrrReproducer = new HRRSupportedVersionsReproducer();
    static final SSLStringizer hrrStringizer = new SHSupportedVersionsStringizer();

    SupportedVersionsExtension() {
    }

    private static final class CHSupportedVersionsProducer
    implements HandshakeProducer {
        private CHSupportedVersionsProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.CH_SUPPORTED_VERSIONS)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable extension: " + SSLExtension.CH_SUPPORTED_VERSIONS.name, new Object[0]);
                }
                return null;
            }
            int[] protocols = new int[chc.activeProtocols.size()];
            int verLen = protocols.length * 2;
            byte[] extData = new byte[verLen + 1];
            extData[0] = (byte)(verLen & 0xFF);
            int i = 0;
            int j = 1;
            for (ProtocolVersion pv : chc.activeProtocols) {
                protocols[i++] = pv.id;
                extData[j++] = pv.major;
                extData[j++] = pv.minor;
            }
            chc.handshakeExtensions.put(SSLExtension.CH_SUPPORTED_VERSIONS, new CHSupportedVersionsSpec(protocols));
            return extData;
        }
    }

    private static final class CHSupportedVersionsConsumer
    implements SSLExtension.ExtensionConsumer {
        private CHSupportedVersionsConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.CH_SUPPORTED_VERSIONS)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable extension: " + SSLExtension.CH_SUPPORTED_VERSIONS.name, new Object[0]);
                }
                return;
            }
            CHSupportedVersionsSpec spec = new CHSupportedVersionsSpec(shc, buffer);
            shc.handshakeExtensions.put(SSLExtension.CH_SUPPORTED_VERSIONS, spec);
        }
    }

    private static final class CHSupportedVersionsStringizer
    implements SSLStringizer {
        private CHSupportedVersionsStringizer() {
        }

        @Override
        public String toString(HandshakeContext hc, ByteBuffer buffer) {
            try {
                return new CHSupportedVersionsSpec(hc, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }

    private static final class SHSupportedVersionsProducer
    implements HandshakeProducer {
        private SHSupportedVersionsProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            CHSupportedVersionsSpec svs = (CHSupportedVersionsSpec)shc.handshakeExtensions.get(SSLExtension.CH_SUPPORTED_VERSIONS);
            if (svs == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("Ignore unavailable supported_versions extension", new Object[0]);
                }
                return null;
            }
            if (!shc.sslConfig.isAvailable(SSLExtension.SH_SUPPORTED_VERSIONS)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable extension: " + SSLExtension.SH_SUPPORTED_VERSIONS.name, new Object[0]);
                }
                return null;
            }
            byte[] extData = new byte[]{shc.negotiatedProtocol.major, shc.negotiatedProtocol.minor};
            shc.handshakeExtensions.put(SSLExtension.SH_SUPPORTED_VERSIONS, new SHSupportedVersionsSpec(shc.negotiatedProtocol));
            return extData;
        }
    }

    private static final class SHSupportedVersionsConsumer
    implements SSLExtension.ExtensionConsumer {
        private SHSupportedVersionsConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.SH_SUPPORTED_VERSIONS)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable extension: " + SSLExtension.SH_SUPPORTED_VERSIONS.name, new Object[0]);
                }
                return;
            }
            SHSupportedVersionsSpec spec = new SHSupportedVersionsSpec(chc, buffer);
            chc.handshakeExtensions.put(SSLExtension.SH_SUPPORTED_VERSIONS, spec);
        }
    }

    private static final class SHSupportedVersionsStringizer
    implements SSLStringizer {
        private SHSupportedVersionsStringizer() {
        }

        @Override
        public String toString(HandshakeContext hc, ByteBuffer buffer) {
            try {
                return new SHSupportedVersionsSpec(hc, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }

    private static final class HRRSupportedVersionsProducer
    implements HandshakeProducer {
        private HRRSupportedVersionsProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.HRR_SUPPORTED_VERSIONS)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable extension: " + SSLExtension.HRR_SUPPORTED_VERSIONS.name, new Object[0]);
                }
                return null;
            }
            byte[] extData = new byte[]{shc.negotiatedProtocol.major, shc.negotiatedProtocol.minor};
            shc.handshakeExtensions.put(SSLExtension.HRR_SUPPORTED_VERSIONS, new SHSupportedVersionsSpec(shc.negotiatedProtocol));
            return extData;
        }
    }

    private static final class HRRSupportedVersionsConsumer
    implements SSLExtension.ExtensionConsumer {
        private HRRSupportedVersionsConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.HRR_SUPPORTED_VERSIONS)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable extension: " + SSLExtension.HRR_SUPPORTED_VERSIONS.name, new Object[0]);
                }
                return;
            }
            SHSupportedVersionsSpec spec = new SHSupportedVersionsSpec(chc, buffer);
            chc.handshakeExtensions.put(SSLExtension.HRR_SUPPORTED_VERSIONS, spec);
        }
    }

    private static final class HRRSupportedVersionsReproducer
    implements HandshakeProducer {
        private HRRSupportedVersionsReproducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.HRR_SUPPORTED_VERSIONS)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("[Reproduce] Ignore unavailable extension: " + SSLExtension.HRR_SUPPORTED_VERSIONS.name, new Object[0]);
                }
                return null;
            }
            byte[] extData = new byte[]{shc.negotiatedProtocol.major, shc.negotiatedProtocol.minor};
            return extData;
        }
    }

    static final class SHSupportedVersionsSpec
    implements SSLExtension.SSLExtensionSpec {
        final int selectedVersion;

        private SHSupportedVersionsSpec(ProtocolVersion selectedVersion) {
            this.selectedVersion = selectedVersion.id;
        }

        private SHSupportedVersionsSpec(HandshakeContext hc, ByteBuffer m) throws IOException {
            if (m.remaining() != 2) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid supported_versions: insufficient data"));
            }
            byte major = m.get();
            byte minor = m.get();
            this.selectedVersion = (major & 0xFF) << 8 | minor & 0xFF;
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"selected version\": '['{0}']'", Locale.ENGLISH);
            Object[] messageFields = new Object[]{ProtocolVersion.nameOf(this.selectedVersion)};
            return messageFormat.format(messageFields);
        }
    }

    static final class CHSupportedVersionsSpec
    implements SSLExtension.SSLExtensionSpec {
        final int[] requestedProtocols;

        private CHSupportedVersionsSpec(int[] requestedProtocols) {
            this.requestedProtocols = requestedProtocols;
        }

        private CHSupportedVersionsSpec(HandshakeContext hc, ByteBuffer m) throws IOException {
            if (m.remaining() < 3) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid supported_versions extension: insufficient data"));
            }
            byte[] vbs = Record.getBytes8(m);
            if (m.hasRemaining()) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid supported_versions extension: unknown extra data"));
            }
            if (vbs.length == 0 || (vbs.length & 1) != 0) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid supported_versions extension: incomplete data"));
            }
            int[] protocols = new int[vbs.length >> 1];
            int i = 0;
            int j = 0;
            while (i < vbs.length) {
                byte major = vbs[i++];
                byte minor = vbs[i++];
                protocols[j++] = (major & 0xFF) << 8 | minor & 0xFF;
            }
            this.requestedProtocols = protocols;
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"versions\": '['{0}']'", Locale.ENGLISH);
            if (this.requestedProtocols == null || this.requestedProtocols.length == 0) {
                Object[] messageFields = new Object[]{"<no supported version specified>"};
                return messageFormat.format(messageFields);
            }
            StringBuilder builder = new StringBuilder(512);
            boolean isFirst = true;
            for (int pv : this.requestedProtocols) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    builder.append(", ");
                }
                builder.append(ProtocolVersion.nameOf(pv));
            }
            Object[] messageFields = new Object[]{builder.toString()};
            return messageFormat.format(messageFields);
        }
    }
}

