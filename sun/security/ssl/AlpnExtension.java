/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.Security;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLProtocolException;
import javax.net.ssl.SSLSocket;
import sun.security.ssl.Alert;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeAbsence;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.Record;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLStringizer;
import sun.security.ssl.ServerHandshakeContext;

final class AlpnExtension {
    static final HandshakeProducer chNetworkProducer = new CHAlpnProducer();
    static final SSLExtension.ExtensionConsumer chOnLoadConsumer = new CHAlpnConsumer();
    static final HandshakeAbsence chOnLoadAbsence = new CHAlpnAbsence();
    static final HandshakeProducer shNetworkProducer = new SHAlpnProducer();
    static final SSLExtension.ExtensionConsumer shOnLoadConsumer = new SHAlpnConsumer();
    static final HandshakeAbsence shOnLoadAbsence = new SHAlpnAbsence();
    static final HandshakeProducer eeNetworkProducer = new SHAlpnProducer();
    static final SSLExtension.ExtensionConsumer eeOnLoadConsumer = new SHAlpnConsumer();
    static final HandshakeAbsence eeOnLoadAbsence = new SHAlpnAbsence();
    static final SSLStringizer alpnStringizer = new AlpnStringizer();
    static final Charset alpnCharset;

    AlpnExtension() {
    }

    static {
        String alpnCharsetString = AccessController.doPrivileged(() -> Security.getProperty("jdk.tls.alpnCharset"));
        if (alpnCharsetString == null || alpnCharsetString.length() == 0) {
            alpnCharsetString = "ISO_8859_1";
        }
        alpnCharset = Charset.forName(alpnCharsetString);
    }

    private static final class CHAlpnProducer
    implements HandshakeProducer {
        static final int MAX_AP_LENGTH = 255;
        static final int MAX_AP_LIST_LENGTH = 65535;

        private CHAlpnProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.CH_ALPN)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.info("Ignore client unavailable extension: " + SSLExtension.CH_ALPN.name, new Object[0]);
                }
                chc.applicationProtocol = "";
                chc.conContext.applicationProtocol = "";
                return null;
            }
            Object[] laps = chc.sslConfig.applicationProtocols;
            if (laps == null || laps.length == 0) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.info("No available application protocols", new Object[0]);
                }
                return null;
            }
            int listLength = 0;
            for (String string : laps) {
                int length = string.getBytes(alpnCharset).length;
                if (length == 0) {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                        SSLLogger.severe("Application protocol name cannot be empty", new Object[0]);
                    }
                    throw chc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Application protocol name cannot be empty");
                }
                if (length > 255) {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                        SSLLogger.severe("Application protocol name (" + string + ") exceeds the size limit (" + 255 + " bytes)", new Object[0]);
                    }
                    throw chc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Application protocol name (" + string + ") exceeds the size limit (" + 255 + " bytes)");
                }
                if ((listLength += length + 1) <= 65535) continue;
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.severe("The configured application protocols (" + Arrays.toString(laps) + ") exceed the size limit (" + 65535 + " bytes)", new Object[0]);
                }
                throw chc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "The configured application protocols (" + Arrays.toString(laps) + ") exceed the size limit (" + 65535 + " bytes)");
            }
            byte[] extData = new byte[listLength + 2];
            ByteBuffer m = ByteBuffer.wrap(extData);
            Record.putInt16(m, listLength);
            for (String string : laps) {
                Record.putBytes8(m, string.getBytes(alpnCharset));
            }
            chc.handshakeExtensions.put(SSLExtension.CH_ALPN, new AlpnSpec(chc.sslConfig.applicationProtocols));
            return extData;
        }
    }

    private static final class CHAlpnConsumer
    implements SSLExtension.ExtensionConsumer {
        private CHAlpnConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            boolean noAlpnProtocols;
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.CH_ALPN)) {
                shc.applicationProtocol = "";
                shc.conContext.applicationProtocol = "";
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.info("Ignore server unavailable extension: " + SSLExtension.CH_ALPN.name, new Object[0]);
                }
                return;
            }
            boolean noAPSelector = shc.conContext.transport instanceof SSLEngine ? shc.sslConfig.engineAPSelector == null : shc.sslConfig.socketAPSelector == null;
            boolean bl = noAlpnProtocols = shc.sslConfig.applicationProtocols == null || shc.sslConfig.applicationProtocols.length == 0;
            if (noAPSelector && noAlpnProtocols) {
                shc.applicationProtocol = "";
                shc.conContext.applicationProtocol = "";
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore server unenabled extension: " + SSLExtension.CH_ALPN.name, new Object[0]);
                }
                return;
            }
            AlpnSpec spec = new AlpnSpec(shc, buffer);
            if (noAPSelector) {
                List<String> protocolNames = spec.applicationProtocols;
                boolean matched = false;
                for (String ap : shc.sslConfig.applicationProtocols) {
                    if (!protocolNames.contains(ap)) continue;
                    shc.applicationProtocol = ap;
                    shc.conContext.applicationProtocol = ap;
                    matched = true;
                    break;
                }
                if (!matched) {
                    throw shc.conContext.fatal(Alert.NO_APPLICATION_PROTOCOL, "No matching application layer protocol values");
                }
            }
            shc.handshakeExtensions.put(SSLExtension.CH_ALPN, spec);
        }
    }

    private static final class CHAlpnAbsence
    implements HandshakeAbsence {
        private CHAlpnAbsence() {
        }

        @Override
        public void absent(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            shc.applicationProtocol = "";
            shc.conContext.applicationProtocol = "";
        }
    }

    private static final class SHAlpnProducer
    implements HandshakeProducer {
        private SHAlpnProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            AlpnSpec requestedAlps = (AlpnSpec)shc.handshakeExtensions.get(SSLExtension.CH_ALPN);
            if (requestedAlps == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable extension: " + SSLExtension.SH_ALPN.name, new Object[0]);
                }
                shc.applicationProtocol = "";
                shc.conContext.applicationProtocol = "";
                return null;
            }
            List<String> alps = requestedAlps.applicationProtocols;
            if (shc.conContext.transport instanceof SSLEngine) {
                if (shc.sslConfig.engineAPSelector != null) {
                    SSLEngine engine = (SSLEngine)((Object)shc.conContext.transport);
                    shc.applicationProtocol = shc.sslConfig.engineAPSelector.apply(engine, alps);
                    if (shc.applicationProtocol == null || !shc.applicationProtocol.isEmpty() && !alps.contains(shc.applicationProtocol)) {
                        throw shc.conContext.fatal(Alert.NO_APPLICATION_PROTOCOL, "No matching application layer protocol values");
                    }
                }
            } else if (shc.sslConfig.socketAPSelector != null) {
                SSLSocket socket = (SSLSocket)((Object)shc.conContext.transport);
                shc.applicationProtocol = shc.sslConfig.socketAPSelector.apply(socket, alps);
                if (shc.applicationProtocol == null || !shc.applicationProtocol.isEmpty() && !alps.contains(shc.applicationProtocol)) {
                    throw shc.conContext.fatal(Alert.NO_APPLICATION_PROTOCOL, "No matching application layer protocol values");
                }
            }
            if (shc.applicationProtocol == null || shc.applicationProtocol.isEmpty()) {
                shc.applicationProtocol = "";
                shc.conContext.applicationProtocol = "";
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("Ignore, no negotiated application layer protocol", new Object[0]);
                }
                return null;
            }
            byte[] bytes = shc.applicationProtocol.getBytes(alpnCharset);
            int listLen = bytes.length + 1;
            byte[] extData = new byte[listLen + 2];
            ByteBuffer m = ByteBuffer.wrap(extData);
            Record.putInt16(m, listLen);
            Record.putBytes8(m, bytes);
            shc.conContext.applicationProtocol = shc.applicationProtocol;
            shc.handshakeExtensions.remove(SSLExtension.CH_ALPN);
            return extData;
        }
    }

    private static final class SHAlpnConsumer
    implements SSLExtension.ExtensionConsumer {
        private SHAlpnConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            AlpnSpec requestedAlps = (AlpnSpec)chc.handshakeExtensions.get(SSLExtension.CH_ALPN);
            if (requestedAlps == null || requestedAlps.applicationProtocols.isEmpty()) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected " + SSLExtension.CH_ALPN.name + " extension");
            }
            AlpnSpec spec = new AlpnSpec(chc, buffer);
            if (spec.applicationProtocols.size() != 1) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Invalid " + SSLExtension.CH_ALPN.name + " extension: Only one application protocol name is allowed in ServerHello message");
            }
            if (!requestedAlps.applicationProtocols.containsAll(spec.applicationProtocols)) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Invalid " + SSLExtension.CH_ALPN.name + " extension: Only client specified application protocol is allowed in ServerHello message");
            }
            chc.conContext.applicationProtocol = chc.applicationProtocol = spec.applicationProtocols.get(0);
            chc.handshakeExtensions.remove(SSLExtension.CH_ALPN);
        }
    }

    private static final class SHAlpnAbsence
    implements HandshakeAbsence {
        private SHAlpnAbsence() {
        }

        @Override
        public void absent(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            chc.applicationProtocol = "";
            chc.conContext.applicationProtocol = "";
        }
    }

    private static final class AlpnStringizer
    implements SSLStringizer {
        private AlpnStringizer() {
        }

        @Override
        public String toString(HandshakeContext hc, ByteBuffer buffer) {
            try {
                return new AlpnSpec(hc, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }

    static final class AlpnSpec
    implements SSLExtension.SSLExtensionSpec {
        final List<String> applicationProtocols;

        private AlpnSpec(String[] applicationProtocols) {
            this.applicationProtocols = List.of(applicationProtocols);
        }

        private AlpnSpec(HandshakeContext hc, ByteBuffer buffer) throws IOException {
            if (buffer.remaining() < 2) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid application_layer_protocol_negotiation: insufficient data (length=" + buffer.remaining() + ")"));
            }
            int listLen = Record.getInt16(buffer);
            if (listLen < 2 || listLen != buffer.remaining()) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid application_layer_protocol_negotiation: incorrect list length (length=" + listLen + ")"));
            }
            LinkedList<String> protocolNames = new LinkedList<String>();
            while (buffer.hasRemaining()) {
                byte[] bytes = Record.getBytes8(buffer);
                if (bytes.length == 0) {
                    throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid application_layer_protocol_negotiation extension: empty application protocol name"));
                }
                String appProtocol = new String(bytes, alpnCharset);
                protocolNames.add(appProtocol);
            }
            this.applicationProtocols = Collections.unmodifiableList(protocolNames);
        }

        public String toString() {
            return this.applicationProtocols.toString();
        }
    }
}

