/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SSLProtocolException;
import sun.security.ssl.Alert;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeAbsence;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.NamedGroup;
import sun.security.ssl.Record;
import sun.security.ssl.SSLConfiguration;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLStringizer;
import sun.security.ssl.ServerHandshakeContext;

final class SupportedGroupsExtension {
    static final HandshakeProducer chNetworkProducer = new CHSupportedGroupsProducer();
    static final SSLExtension.ExtensionConsumer chOnLoadConsumer = new CHSupportedGroupsConsumer();
    static final HandshakeAbsence chOnTradAbsence = new CHSupportedGroupsOnTradeAbsence();
    static final SSLStringizer sgsStringizer = new SupportedGroupsStringizer();
    static final HandshakeProducer eeNetworkProducer = new EESupportedGroupsProducer();
    static final SSLExtension.ExtensionConsumer eeOnLoadConsumer = new EESupportedGroupsConsumer();

    SupportedGroupsExtension() {
    }

    private static final class CHSupportedGroupsProducer
    implements HandshakeProducer {
        private CHSupportedGroupsProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.CH_SUPPORTED_GROUPS)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable supported_groups extension", new Object[0]);
                }
                return null;
            }
            ArrayList<NamedGroup> namedGroups = new ArrayList<NamedGroup>(chc.sslConfig.namedGroups.length);
            for (String name : chc.sslConfig.namedGroups) {
                NamedGroup ng = NamedGroup.nameOf(name);
                if (ng == null) {
                    if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
                    SSLLogger.fine("Ignore unspecified named group: " + name, new Object[0]);
                    continue;
                }
                if (!SSLConfiguration.enableFFDHE && ng.spec == NamedGroup.NamedGroupSpec.NAMED_GROUP_FFDHE) continue;
                if (ng.isAvailable(chc.activeProtocols) && ng.isSupported(chc.activeCipherSuites) && ng.isPermitted(chc.algorithmConstraints)) {
                    namedGroups.add(ng);
                    continue;
                }
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
                SSLLogger.fine("Ignore inactive or disabled named group: " + ng.name, new Object[0]);
            }
            if (namedGroups.isEmpty()) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("no available named group", new Object[0]);
                }
                return null;
            }
            int vectorLen = namedGroups.size() << 1;
            byte[] extData = new byte[vectorLen + 2];
            ByteBuffer m = ByteBuffer.wrap(extData);
            Record.putInt16(m, vectorLen);
            for (NamedGroup namedGroup : namedGroups) {
                Record.putInt16(m, namedGroup.id);
            }
            chc.clientRequestedNamedGroups = Collections.unmodifiableList(namedGroups);
            chc.handshakeExtensions.put(SSLExtension.CH_SUPPORTED_GROUPS, new SupportedGroupsSpec(namedGroups));
            return extData;
        }
    }

    private static final class CHSupportedGroupsConsumer
    implements SSLExtension.ExtensionConsumer {
        private CHSupportedGroupsConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.CH_SUPPORTED_GROUPS)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable supported_groups extension", new Object[0]);
                }
                return;
            }
            SupportedGroupsSpec spec = new SupportedGroupsSpec(shc, buffer);
            LinkedList<NamedGroup> knownNamedGroups = new LinkedList<NamedGroup>();
            for (int id : spec.namedGroupsIds) {
                NamedGroup ng = NamedGroup.valueOf(id);
                if (ng == null) continue;
                knownNamedGroups.add(ng);
            }
            shc.clientRequestedNamedGroups = knownNamedGroups;
            shc.handshakeExtensions.put(SSLExtension.CH_SUPPORTED_GROUPS, spec);
        }
    }

    private static final class CHSupportedGroupsOnTradeAbsence
    implements HandshakeAbsence {
        private CHSupportedGroupsOnTradeAbsence() {
        }

        @Override
        public void absent(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (shc.negotiatedProtocol.useTLS13PlusSpec() && shc.handshakeExtensions.containsKey(SSLExtension.CH_KEY_SHARE)) {
                throw shc.conContext.fatal(Alert.MISSING_EXTENSION, "No supported_groups extension to work with the key_share extension");
            }
        }
    }

    private static final class SupportedGroupsStringizer
    implements SSLStringizer {
        private SupportedGroupsStringizer() {
        }

        @Override
        public String toString(HandshakeContext hc, ByteBuffer buffer) {
            try {
                return new SupportedGroupsSpec(hc, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }

    private static final class EESupportedGroupsProducer
    implements HandshakeProducer {
        private EESupportedGroupsProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.EE_SUPPORTED_GROUPS)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable supported_groups extension", new Object[0]);
                }
                return null;
            }
            ArrayList<NamedGroup> namedGroups = new ArrayList<NamedGroup>(shc.sslConfig.namedGroups.length);
            for (String name : shc.sslConfig.namedGroups) {
                NamedGroup ng = NamedGroup.nameOf(name);
                if (ng == null) {
                    if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
                    SSLLogger.fine("Ignore unspecified named group: " + (String)name, new Object[0]);
                    continue;
                }
                if (!SSLConfiguration.enableFFDHE && ng.spec == NamedGroup.NamedGroupSpec.NAMED_GROUP_FFDHE) continue;
                if (ng.isAvailable(shc.activeProtocols) && ng.isSupported(shc.activeCipherSuites) && ng.isPermitted(shc.algorithmConstraints)) {
                    namedGroups.add(ng);
                    continue;
                }
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
                SSLLogger.fine("Ignore inactive or disabled named group: " + ng.name, new Object[0]);
            }
            if (namedGroups.isEmpty()) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("no available named group", new Object[0]);
                }
                return null;
            }
            int vectorLen = namedGroups.size() << 1;
            byte[] extData = new byte[vectorLen + 2];
            ByteBuffer m = ByteBuffer.wrap(extData);
            Record.putInt16(m, vectorLen);
            for (NamedGroup namedGroup : namedGroups) {
                Record.putInt16(m, namedGroup.id);
            }
            shc.conContext.serverRequestedNamedGroups = Collections.unmodifiableList(namedGroups);
            SupportedGroupsSpec spec = new SupportedGroupsSpec(namedGroups);
            shc.handshakeExtensions.put(SSLExtension.EE_SUPPORTED_GROUPS, spec);
            return extData;
        }
    }

    private static final class EESupportedGroupsConsumer
    implements SSLExtension.ExtensionConsumer {
        private EESupportedGroupsConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.EE_SUPPORTED_GROUPS)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable supported_groups extension", new Object[0]);
                }
                return;
            }
            SupportedGroupsSpec spec = new SupportedGroupsSpec(chc, buffer);
            ArrayList<NamedGroup> knownNamedGroups = new ArrayList<NamedGroup>(spec.namedGroupsIds.length);
            for (int id : spec.namedGroupsIds) {
                NamedGroup ng = NamedGroup.valueOf(id);
                if (ng == null) continue;
                knownNamedGroups.add(ng);
            }
            chc.conContext.serverRequestedNamedGroups = knownNamedGroups;
            chc.handshakeExtensions.put(SSLExtension.EE_SUPPORTED_GROUPS, spec);
        }
    }

    static final class SupportedGroupsSpec
    implements SSLExtension.SSLExtensionSpec {
        final int[] namedGroupsIds;

        private SupportedGroupsSpec(List<NamedGroup> namedGroups) {
            this.namedGroupsIds = new int[namedGroups.size()];
            int i = 0;
            for (NamedGroup ng : namedGroups) {
                this.namedGroupsIds[i++] = ng.id;
            }
        }

        private SupportedGroupsSpec(HandshakeContext hc, ByteBuffer m) throws IOException {
            if (m.remaining() < 2) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid supported_groups extension: insufficient data"));
            }
            byte[] ngs = Record.getBytes16(m);
            if (m.hasRemaining()) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid supported_groups extension: unknown extra data"));
            }
            if (ngs.length == 0 || ngs.length % 2 != 0) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid supported_groups extension: incomplete data"));
            }
            int[] ids = new int[ngs.length / 2];
            int i = 0;
            int j = 0;
            while (i < ngs.length) {
                ids[j++] = (ngs[i++] & 0xFF) << 8 | ngs[i++] & 0xFF;
            }
            this.namedGroupsIds = ids;
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"named groups\": '['{0}']'", Locale.ENGLISH);
            if (this.namedGroupsIds == null || this.namedGroupsIds.length == 0) {
                Object[] messageFields = new Object[]{"<no supported named group specified>"};
                return messageFormat.format(messageFields);
            }
            StringBuilder builder = new StringBuilder(512);
            boolean isFirst = true;
            for (int ngid : this.namedGroupsIds) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    builder.append(", ");
                }
                builder.append(NamedGroup.nameOf(ngid));
            }
            Object[] messageFields = new Object[]{builder.toString()};
            return messageFormat.format(messageFields);
        }
    }
}

