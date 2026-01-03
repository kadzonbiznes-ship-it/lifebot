/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLProtocolException;
import sun.security.ssl.Alert;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.Record;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLStringizer;
import sun.security.ssl.ServerHandshakeContext;
import sun.security.ssl.Utilities;

final class ServerNameExtension {
    static final HandshakeProducer chNetworkProducer = new CHServerNameProducer();
    static final SSLExtension.ExtensionConsumer chOnLoadConsumer = new CHServerNameConsumer();
    static final SSLStringizer chStringizer = new CHServerNamesStringizer();
    static final HandshakeProducer shNetworkProducer = new SHServerNameProducer();
    static final SSLExtension.ExtensionConsumer shOnLoadConsumer = new SHServerNameConsumer();
    static final SSLStringizer shStringizer = new SHServerNamesStringizer();
    static final HandshakeProducer eeNetworkProducer = new EEServerNameProducer();
    static final SSLExtension.ExtensionConsumer eeOnLoadConsumer = new EEServerNameConsumer();

    ServerNameExtension() {
    }

    private static final class CHServerNameProducer
    implements HandshakeProducer {
        private CHServerNameProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.CH_SERVER_NAME)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("Ignore unavailable server_name extension", new Object[0]);
                }
                return null;
            }
            List<SNIServerName> serverNames = chc.isResumption && chc.resumingSession != null ? chc.resumingSession.getRequestedServerNames() : chc.sslConfig.serverNames;
            if (serverNames != null && !serverNames.isEmpty()) {
                int sniLen = 0;
                for (SNIServerName sniName : serverNames) {
                    sniLen += 3;
                    sniLen += sniName.getEncoded().length;
                }
                byte[] extData = new byte[sniLen + 2];
                ByteBuffer m = ByteBuffer.wrap(extData);
                Record.putInt16(m, sniLen);
                for (SNIServerName sniName : serverNames) {
                    Record.putInt8(m, sniName.getType());
                    Record.putBytes16(m, sniName.getEncoded());
                }
                chc.requestedServerNames = serverNames;
                chc.handshakeExtensions.put(SSLExtension.CH_SERVER_NAME, new CHServerNamesSpec(serverNames));
                return extData;
            }
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.warning("Unable to indicate server name", new Object[0]);
            }
            return null;
        }
    }

    private static final class CHServerNameConsumer
    implements SSLExtension.ExtensionConsumer {
        private CHServerNameConsumer() {
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.CH_SERVER_NAME)) {
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) return;
                SSLLogger.fine("Ignore unavailable extension: " + SSLExtension.CH_SERVER_NAME.name, new Object[0]);
                return;
            }
            CHServerNamesSpec spec = new CHServerNamesSpec(shc, buffer);
            shc.handshakeExtensions.put(SSLExtension.CH_SERVER_NAME, spec);
            SNIServerName sni = null;
            if (!shc.sslConfig.sniMatchers.isEmpty()) {
                sni = CHServerNameConsumer.chooseSni(shc.sslConfig.sniMatchers, spec.serverNames);
                if (sni == null) throw shc.conContext.fatal(Alert.UNRECOGNIZED_NAME, "Unrecognized server name indication");
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("server name indication (" + sni + ") is accepted", new Object[0]);
                }
            } else if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("no server name matchers, ignore server name indication", new Object[0]);
            }
            if (shc.isResumption && shc.resumingSession != null && !Objects.equals(sni, shc.resumingSession.serverNameIndication)) {
                shc.isResumption = false;
                shc.resumingSession = null;
                shc.handshakeExtensions.remove(SSLExtension.SH_PRE_SHARED_KEY);
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("abort session resumption, different server name indication used", new Object[0]);
                }
            }
            shc.requestedServerNames = spec.serverNames;
            shc.negotiatedServerName = sni;
        }

        private static SNIServerName chooseSni(Collection<SNIMatcher> matchers, List<SNIServerName> sniNames) {
            if (sniNames != null && !sniNames.isEmpty()) {
                block0: for (SNIMatcher matcher : matchers) {
                    int matcherType = matcher.getType();
                    for (SNIServerName sniName : sniNames) {
                        if (sniName.getType() != matcherType) continue;
                        if (!matcher.matches(sniName)) continue block0;
                        return sniName;
                    }
                }
            }
            return null;
        }
    }

    private static final class CHServerNamesStringizer
    implements SSLStringizer {
        private CHServerNamesStringizer() {
        }

        @Override
        public String toString(HandshakeContext hc, ByteBuffer buffer) {
            try {
                return new CHServerNamesSpec(hc, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }

    private static final class SHServerNameProducer
    implements HandshakeProducer {
        private SHServerNameProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            CHServerNamesSpec spec = (CHServerNamesSpec)shc.handshakeExtensions.get(SSLExtension.CH_SERVER_NAME);
            if (spec == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.finest("Ignore unavailable extension: " + SSLExtension.SH_SERVER_NAME.name, new Object[0]);
                }
                return null;
            }
            if (shc.isResumption || shc.negotiatedServerName == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.finest("No expected server name indication response", new Object[0]);
                }
                return null;
            }
            shc.handshakeExtensions.put(SSLExtension.SH_SERVER_NAME, SHServerNamesSpec.DEFAULT);
            return new byte[0];
        }
    }

    private static final class SHServerNameConsumer
    implements SSLExtension.ExtensionConsumer {
        private SHServerNameConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            CHServerNamesSpec spec = (CHServerNamesSpec)chc.handshakeExtensions.get(SSLExtension.CH_SERVER_NAME);
            if (spec == null) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected ServerHello server_name extension");
            }
            if (buffer.remaining() != 0) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Invalid ServerHello server_name extension");
            }
            chc.handshakeExtensions.put(SSLExtension.SH_SERVER_NAME, SHServerNamesSpec.DEFAULT);
            chc.negotiatedServerName = spec.serverNames.get(0);
        }
    }

    private static final class SHServerNamesStringizer
    implements SSLStringizer {
        private SHServerNamesStringizer() {
        }

        @Override
        public String toString(HandshakeContext hc, ByteBuffer buffer) {
            try {
                return new SHServerNamesSpec(hc, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }

    private static final class EEServerNameProducer
    implements HandshakeProducer {
        private EEServerNameProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            CHServerNamesSpec spec = (CHServerNamesSpec)shc.handshakeExtensions.get(SSLExtension.CH_SERVER_NAME);
            if (spec == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.finest("Ignore unavailable extension: " + SSLExtension.EE_SERVER_NAME.name, new Object[0]);
                }
                return null;
            }
            if (shc.isResumption || shc.negotiatedServerName == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.finest("No expected server name indication response", new Object[0]);
                }
                return null;
            }
            shc.handshakeExtensions.put(SSLExtension.EE_SERVER_NAME, SHServerNamesSpec.DEFAULT);
            return new byte[0];
        }
    }

    private static final class EEServerNameConsumer
    implements SSLExtension.ExtensionConsumer {
        private EEServerNameConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            CHServerNamesSpec spec = (CHServerNamesSpec)chc.handshakeExtensions.get(SSLExtension.CH_SERVER_NAME);
            if (spec == null) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected EncryptedExtensions server_name extension");
            }
            if (buffer.remaining() != 0) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Invalid EncryptedExtensions server_name extension");
            }
            chc.handshakeExtensions.put(SSLExtension.EE_SERVER_NAME, SHServerNamesSpec.DEFAULT);
            chc.negotiatedServerName = spec.serverNames.get(0);
        }
    }

    static final class SHServerNamesSpec
    implements SSLExtension.SSLExtensionSpec {
        static final SHServerNamesSpec DEFAULT = new SHServerNamesSpec();

        private SHServerNamesSpec() {
        }

        private SHServerNamesSpec(HandshakeContext hc, ByteBuffer buffer) throws IOException {
            if (buffer.remaining() != 0) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid ServerHello server_name extension: not empty"));
            }
        }

        public String toString() {
            return "<empty extension_data field>";
        }
    }

    static final class CHServerNamesSpec
    implements SSLExtension.SSLExtensionSpec {
        static final int NAME_HEADER_LENGTH = 3;
        final List<SNIServerName> serverNames;

        private CHServerNamesSpec(List<SNIServerName> serverNames) {
            this.serverNames = List.copyOf(serverNames);
        }

        private CHServerNamesSpec(HandshakeContext hc, ByteBuffer buffer) throws IOException {
            if (buffer.remaining() < 2) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid server_name extension: insufficient data"));
            }
            int sniLen = Record.getInt16(buffer);
            if (sniLen == 0 || sniLen != buffer.remaining()) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid server_name extension: incomplete data"));
            }
            LinkedHashMap<Integer, SNIServerName> sniMap = new LinkedHashMap<Integer, SNIServerName>();
            while (buffer.hasRemaining()) {
                SNIServerName serverName;
                int nameType = Record.getInt8(buffer);
                byte[] encoded = Record.getBytes16(buffer);
                if (nameType == 0) {
                    if (encoded.length == 0) {
                        throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Empty HostName in server_name extension"));
                    }
                    try {
                        serverName = new SNIHostName(encoded);
                    }
                    catch (IllegalArgumentException iae) {
                        SSLProtocolException spe = new SSLProtocolException("Illegal server name, type=host_name(" + nameType + "), name=" + new String(encoded, StandardCharsets.UTF_8) + ", value={" + Utilities.toHexString(encoded) + "}", iae);
                        throw hc.conContext.fatal(Alert.ILLEGAL_PARAMETER, spe);
                    }
                }
                try {
                    serverName = new UnknownServerName(nameType, encoded);
                }
                catch (IllegalArgumentException iae) {
                    SSLProtocolException spe = new SSLProtocolException("Illegal server name, type=(" + nameType + "), value={" + Utilities.toHexString(encoded) + "}", iae);
                    throw hc.conContext.fatal(Alert.ILLEGAL_PARAMETER, spe);
                }
                if (sniMap.put(serverName.getType(), serverName) == null) continue;
                throw hc.conContext.fatal(Alert.ILLEGAL_PARAMETER, new SSLProtocolException("Duplicated server name of type " + serverName.getType()));
            }
            this.serverNames = new ArrayList(sniMap.values());
        }

        public String toString() {
            if (this.serverNames == null || this.serverNames.isEmpty()) {
                return "<no server name indicator specified>";
            }
            StringBuilder builder = new StringBuilder(512);
            for (SNIServerName sn : this.serverNames) {
                builder.append(sn.toString());
                builder.append("\n");
            }
            return builder.toString();
        }

        private static class UnknownServerName
        extends SNIServerName {
            UnknownServerName(int code, byte[] encoded) {
                super(code, encoded);
            }
        }
    }
}

