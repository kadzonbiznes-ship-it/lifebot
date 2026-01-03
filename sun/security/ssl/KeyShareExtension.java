/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.AlgorithmConstraints;
import java.security.CryptoPrimitive;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.net.ssl.SSLProtocolException;
import sun.security.ssl.Alert;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeAbsence;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.NamedGroup;
import sun.security.ssl.NamedGroupCredentials;
import sun.security.ssl.NamedGroupPossession;
import sun.security.ssl.Record;
import sun.security.ssl.SSLCredentials;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLKeyExchange;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLPossession;
import sun.security.ssl.SSLStringizer;
import sun.security.ssl.ServerHandshakeContext;
import sun.security.ssl.Utilities;
import sun.security.util.HexDumpEncoder;

final class KeyShareExtension {
    static final HandshakeProducer chNetworkProducer = new CHKeyShareProducer();
    static final SSLExtension.ExtensionConsumer chOnLoadConsumer = new CHKeyShareConsumer();
    static final HandshakeAbsence chOnTradAbsence = new CHKeyShareOnTradeAbsence();
    static final SSLStringizer chStringizer = new CHKeyShareStringizer();
    static final HandshakeProducer shNetworkProducer = new SHKeyShareProducer();
    static final SSLExtension.ExtensionConsumer shOnLoadConsumer = new SHKeyShareConsumer();
    static final HandshakeAbsence shOnLoadAbsence = new SHKeyShareAbsence();
    static final SSLStringizer shStringizer = new SHKeyShareStringizer();
    static final HandshakeProducer hrrNetworkProducer = new HRRKeyShareProducer();
    static final SSLExtension.ExtensionConsumer hrrOnLoadConsumer = new HRRKeyShareConsumer();
    static final HandshakeProducer hrrNetworkReproducer = new HRRKeyShareReproducer();
    static final SSLStringizer hrrStringizer = new HRRKeyShareStringizer();

    KeyShareExtension() {
    }

    private static final class CHKeyShareProducer
    implements HandshakeProducer {
        private CHKeyShareProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            List<NamedGroup> namedGroups;
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.CH_KEY_SHARE)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable key_share extension", new Object[0]);
                }
                return null;
            }
            if (chc.serverSelectedNamedGroup != null) {
                namedGroups = List.of(chc.serverSelectedNamedGroup);
            } else {
                namedGroups = chc.clientRequestedNamedGroups;
                if (namedGroups == null || namedGroups.isEmpty()) {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                        SSLLogger.warning("Ignore key_share extension, no supported groups", new Object[0]);
                    }
                    return null;
                }
            }
            LinkedList<KeyShareEntry> keyShares = new LinkedList<KeyShareEntry>();
            EnumSet<NamedGroup.NamedGroupSpec> ngTypes = EnumSet.noneOf(NamedGroup.NamedGroupSpec.class);
            for (NamedGroup namedGroup : namedGroups) {
                byte[] keyExchangeData;
                if (ngTypes.contains(namedGroup.spec) || (keyExchangeData = CHKeyShareProducer.getShare(chc, namedGroup)) == null) continue;
                keyShares.add(new KeyShareEntry(namedGroup.id, keyExchangeData));
                ngTypes.add(namedGroup.spec);
                if (ngTypes.size() != 2) continue;
                break;
            }
            int listLen = 0;
            for (KeyShareEntry entry : keyShares) {
                listLen += entry.getEncodedSize();
            }
            byte[] byArray = new byte[listLen + 2];
            ByteBuffer m = ByteBuffer.wrap(byArray);
            Record.putInt16(m, listLen);
            for (KeyShareEntry entry : keyShares) {
                m.put(entry.getEncoded());
            }
            chc.handshakeExtensions.put(SSLExtension.CH_KEY_SHARE, new CHKeyShareSpec(keyShares));
            return byArray;
        }

        private static byte[] getShare(ClientHandshakeContext chc, NamedGroup ng) {
            SSLKeyExchange ke = SSLKeyExchange.valueOf(ng);
            if (ke == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("No key exchange for named group " + ng.name, new Object[0]);
                }
            } else {
                SSLPossession[] poses;
                for (SSLPossession pos : poses = ke.createPossessions(chc)) {
                    chc.handshakePossessions.add(pos);
                    if (!(pos instanceof NamedGroupPossession)) continue;
                    return pos.encode();
                }
            }
            return null;
        }
    }

    private static final class CHKeyShareConsumer
    implements SSLExtension.ExtensionConsumer {
        private CHKeyShareConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (shc.handshakeExtensions.containsKey(SSLExtension.CH_KEY_SHARE)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("The key_share extension has been loaded", new Object[0]);
                }
                return;
            }
            if (!shc.sslConfig.isAvailable(SSLExtension.CH_KEY_SHARE)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable key_share extension", new Object[0]);
                }
                return;
            }
            CHKeyShareSpec spec = new CHKeyShareSpec(shc, buffer);
            LinkedList<SSLCredentials> credentials = new LinkedList<SSLCredentials>();
            for (KeyShareEntry entry : spec.clientShares) {
                NamedGroup ng = NamedGroup.valueOf(entry.namedGroupId);
                if (ng == null || !NamedGroup.isActivatable(shc.sslConfig, (AlgorithmConstraints)shc.algorithmConstraints, ng)) {
                    if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
                    SSLLogger.fine("Ignore unsupported named group: " + NamedGroup.nameOf(entry.namedGroupId), new Object[0]);
                    continue;
                }
                try {
                    SSLCredentials kaCred = ng.decodeCredentials(entry.keyExchange);
                    if (shc.algorithmConstraints != null && kaCred instanceof NamedGroupCredentials) {
                        NamedGroupCredentials namedGroupCredentials = (NamedGroupCredentials)kaCred;
                        if (!shc.algorithmConstraints.permits(EnumSet.of(CryptoPrimitive.KEY_AGREEMENT), namedGroupCredentials.getPublicKey())) {
                            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                                SSLLogger.warning("key share entry of " + (Object)((Object)ng) + " does not  comply with algorithm constraints", new Object[0]);
                            }
                            kaCred = null;
                        }
                    }
                    if (kaCred == null) continue;
                    credentials.add(kaCred);
                }
                catch (GeneralSecurityException ex) {
                    if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
                    SSLLogger.warning("Cannot decode named group: " + NamedGroup.nameOf(entry.namedGroupId), new Object[0]);
                }
            }
            if (!credentials.isEmpty()) {
                shc.handshakeCredentials.addAll(credentials);
            } else {
                shc.handshakeProducers.put(SSLHandshake.HELLO_RETRY_REQUEST.id, SSLHandshake.HELLO_RETRY_REQUEST);
            }
            shc.handshakeExtensions.put(SSLExtension.CH_KEY_SHARE, spec);
        }
    }

    private static final class CHKeyShareOnTradeAbsence
    implements HandshakeAbsence {
        private CHKeyShareOnTradeAbsence() {
        }

        @Override
        public void absent(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (shc.negotiatedProtocol.useTLS13PlusSpec() && shc.handshakeExtensions.containsKey(SSLExtension.CH_SUPPORTED_GROUPS)) {
                throw shc.conContext.fatal(Alert.MISSING_EXTENSION, "No key_share extension to work with the supported_groups extension");
            }
        }
    }

    private static final class CHKeyShareStringizer
    implements SSLStringizer {
        private CHKeyShareStringizer() {
        }

        @Override
        public String toString(HandshakeContext handshakeContext, ByteBuffer buffer) {
            try {
                return new CHKeyShareSpec(handshakeContext, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }

    private static final class SHKeyShareProducer
    implements HandshakeProducer {
        private SHKeyShareProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            CHKeyShareSpec kss = (CHKeyShareSpec)shc.handshakeExtensions.get(SSLExtension.CH_KEY_SHARE);
            if (kss == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("Ignore, no client key_share extension", new Object[0]);
                }
                return null;
            }
            if (!shc.sslConfig.isAvailable(SSLExtension.SH_KEY_SHARE)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("Ignore, no available server key_share extension", new Object[0]);
                }
                return null;
            }
            if (shc.handshakeCredentials == null || shc.handshakeCredentials.isEmpty()) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("No available client key share entries", new Object[0]);
                }
                return null;
            }
            KeyShareEntry keyShare = null;
            for (SSLCredentials cd : shc.handshakeCredentials) {
                NamedGroup ng = null;
                if (cd instanceof NamedGroupCredentials) {
                    NamedGroupCredentials creds = (NamedGroupCredentials)cd;
                    ng = creds.getNamedGroup();
                }
                if (ng == null) continue;
                SSLKeyExchange ke = SSLKeyExchange.valueOf(ng);
                if (ke == null) {
                    if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) continue;
                    SSLLogger.warning("No key exchange for named group " + ng.name, new Object[0]);
                    continue;
                }
                SSLPossession[] poses = ke.createPossessions(shc);
                for (SSLPossession pos : poses) {
                    if (!(pos instanceof NamedGroupPossession)) continue;
                    shc.handshakeKeyExchange = ke;
                    shc.handshakePossessions.add(pos);
                    keyShare = new KeyShareEntry(ng.id, pos.encode());
                    break;
                }
                if (keyShare == null) continue;
                for (Map.Entry<Byte, HandshakeProducer> me : ke.getHandshakeProducers(shc)) {
                    shc.handshakeProducers.put((Byte)me.getKey(), (HandshakeProducer)me.getValue());
                }
            }
            if (keyShare == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("No available server key_share extension", new Object[0]);
                }
                return null;
            }
            byte[] extData = keyShare.getEncoded();
            SHKeyShareSpec spec = new SHKeyShareSpec(keyShare);
            shc.handshakeExtensions.put(SSLExtension.SH_KEY_SHARE, spec);
            return extData;
        }
    }

    private static final class SHKeyShareConsumer
    implements SSLExtension.ExtensionConsumer {
        private SHKeyShareConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (chc.clientRequestedNamedGroups == null || chc.clientRequestedNamedGroups.isEmpty()) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected key_share extension in ServerHello");
            }
            if (!chc.sslConfig.isAvailable(SSLExtension.SH_KEY_SHARE)) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unsupported key_share extension in ServerHello");
            }
            SHKeyShareSpec spec = new SHKeyShareSpec(chc, buffer);
            KeyShareEntry keyShare = spec.serverShare;
            NamedGroup ng = NamedGroup.valueOf(keyShare.namedGroupId);
            if (ng == null || !NamedGroup.isActivatable(chc.sslConfig, (AlgorithmConstraints)chc.algorithmConstraints, ng)) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unsupported named group: " + NamedGroup.nameOf(keyShare.namedGroupId));
            }
            SSLKeyExchange ke = SSLKeyExchange.valueOf(ng);
            if (ke == null) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "No key exchange for named group " + ng.name);
            }
            SSLCredentials credentials = null;
            try {
                SSLCredentials kaCred = ng.decodeCredentials(keyShare.keyExchange);
                if (chc.algorithmConstraints != null && kaCred instanceof NamedGroupCredentials) {
                    NamedGroupCredentials namedGroupCredentials = (NamedGroupCredentials)kaCred;
                    if (!chc.algorithmConstraints.permits(EnumSet.of(CryptoPrimitive.KEY_AGREEMENT), namedGroupCredentials.getPublicKey())) {
                        chc.conContext.fatal(Alert.INSUFFICIENT_SECURITY, "key share entry of " + (Object)((Object)ng) + " does not  comply with algorithm constraints");
                    }
                }
                if (kaCred != null) {
                    credentials = kaCred;
                }
            }
            catch (GeneralSecurityException ex) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Cannot decode named group: " + NamedGroup.nameOf(keyShare.namedGroupId));
            }
            if (credentials == null) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unsupported named group: " + ng.name);
            }
            chc.handshakeKeyExchange = ke;
            chc.handshakeCredentials.add(credentials);
            chc.handshakeExtensions.put(SSLExtension.SH_KEY_SHARE, spec);
        }
    }

    private static final class SHKeyShareAbsence
    implements HandshakeAbsence {
        private SHKeyShareAbsence() {
        }

        @Override
        public void absent(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (SSLLogger.isOn && SSLLogger.isOn("handshake")) {
                SSLLogger.fine("No key_share extension in ServerHello, cleanup the key shares if necessary", new Object[0]);
            }
            chc.handshakePossessions.clear();
        }
    }

    private static final class SHKeyShareStringizer
    implements SSLStringizer {
        private SHKeyShareStringizer() {
        }

        @Override
        public String toString(HandshakeContext handshakeContext, ByteBuffer buffer) {
            try {
                return new SHKeyShareSpec(handshakeContext, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }

    private static final class HRRKeyShareProducer
    implements HandshakeProducer {
        private HRRKeyShareProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.HRR_KEY_SHARE)) {
                throw shc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unsupported key_share extension in HelloRetryRequest");
            }
            if (shc.clientRequestedNamedGroups == null || shc.clientRequestedNamedGroups.isEmpty()) {
                throw shc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected key_share extension in HelloRetryRequest");
            }
            NamedGroup selectedGroup = null;
            for (NamedGroup ng : shc.clientRequestedNamedGroups) {
                if (!NamedGroup.isActivatable(shc.sslConfig, (AlgorithmConstraints)shc.algorithmConstraints, ng)) continue;
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("HelloRetryRequest selected named group: " + ng.name, new Object[0]);
                }
                selectedGroup = ng;
                break;
            }
            if (selectedGroup == null) {
                throw shc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "No common named group");
            }
            byte[] extdata = new byte[]{(byte)(selectedGroup.id >> 8 & 0xFF), (byte)(selectedGroup.id & 0xFF)};
            shc.serverSelectedNamedGroup = selectedGroup;
            shc.handshakeExtensions.put(SSLExtension.HRR_KEY_SHARE, new HRRKeyShareSpec(selectedGroup));
            return extdata;
        }
    }

    private static final class HRRKeyShareConsumer
    implements SSLExtension.ExtensionConsumer {
        private HRRKeyShareConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.HRR_KEY_SHARE)) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unsupported key_share extension in HelloRetryRequest");
            }
            if (chc.clientRequestedNamedGroups == null || chc.clientRequestedNamedGroups.isEmpty()) {
                throw chc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected key_share extension in HelloRetryRequest");
            }
            HRRKeyShareSpec spec = new HRRKeyShareSpec(chc, buffer);
            NamedGroup serverGroup = NamedGroup.valueOf(spec.selectedGroup);
            if (serverGroup == null) {
                throw chc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Unsupported HelloRetryRequest selected group: " + NamedGroup.nameOf(spec.selectedGroup));
            }
            if (!chc.clientRequestedNamedGroups.contains((Object)serverGroup)) {
                throw chc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Unexpected HelloRetryRequest selected group: " + serverGroup.name);
            }
            CHKeyShareSpec chKsSpec = (CHKeyShareSpec)chc.handshakeExtensions.get(SSLExtension.CH_KEY_SHARE);
            if (chKsSpec != null) {
                for (KeyShareEntry kse : chKsSpec.clientShares) {
                    if (serverGroup.id != kse.namedGroupId) continue;
                    throw chc.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Illegal HelloRetryRequest selected group: " + serverGroup.name);
                }
            } else {
                throw chc.conContext.fatal(Alert.INTERNAL_ERROR, "Unable to retrieve ClientHello key_share extension during HRR processing");
            }
            chc.serverSelectedNamedGroup = serverGroup;
            chc.handshakeExtensions.put(SSLExtension.HRR_KEY_SHARE, spec);
        }
    }

    private static final class HRRKeyShareReproducer
    implements HandshakeProducer {
        private HRRKeyShareReproducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.HRR_KEY_SHARE)) {
                throw shc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unsupported key_share extension in HelloRetryRequest");
            }
            CHKeyShareSpec spec = (CHKeyShareSpec)shc.handshakeExtensions.get(SSLExtension.CH_KEY_SHARE);
            if (spec != null && spec.clientShares != null && spec.clientShares.size() == 1) {
                int namedGroupId = spec.clientShares.get((int)0).namedGroupId;
                return new byte[]{(byte)(namedGroupId >> 8 & 0xFF), (byte)(namedGroupId & 0xFF)};
            }
            return null;
        }
    }

    private static final class HRRKeyShareStringizer
    implements SSLStringizer {
        private HRRKeyShareStringizer() {
        }

        @Override
        public String toString(HandshakeContext handshakeContext, ByteBuffer buffer) {
            try {
                return new HRRKeyShareSpec(handshakeContext, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }

    static final class HRRKeyShareSpec
    implements SSLExtension.SSLExtensionSpec {
        final int selectedGroup;

        HRRKeyShareSpec(NamedGroup serverGroup) {
            this.selectedGroup = serverGroup.id;
        }

        private HRRKeyShareSpec(HandshakeContext handshakeContext, ByteBuffer buffer) throws IOException {
            if (buffer.remaining() != 2) {
                throw handshakeContext.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid key_share extension: improper data (length=" + buffer.remaining() + ")"));
            }
            this.selectedGroup = Record.getInt16(buffer);
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"selected group\": '['{0}']'", Locale.ENGLISH);
            Object[] messageFields = new Object[]{NamedGroup.nameOf(this.selectedGroup)};
            return messageFormat.format(messageFields);
        }
    }

    static final class SHKeyShareSpec
    implements SSLExtension.SSLExtensionSpec {
        final KeyShareEntry serverShare;

        SHKeyShareSpec(KeyShareEntry serverShare) {
            this.serverShare = serverShare;
        }

        private SHKeyShareSpec(HandshakeContext handshakeContext, ByteBuffer buffer) throws IOException {
            if (buffer.remaining() < 5) {
                throw handshakeContext.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid key_share extension: insufficient data (length=" + buffer.remaining() + ")"));
            }
            int namedGroupId = Record.getInt16(buffer);
            byte[] keyExchange = Record.getBytes16(buffer);
            if (buffer.hasRemaining()) {
                throw handshakeContext.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid key_share extension: unknown extra data"));
            }
            this.serverShare = new KeyShareEntry(namedGroupId, keyExchange);
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"server_share\": '{'\n  \"named group\": {0}\n  \"key_exchange\": '{'\n{1}\n  '}'\n'}',", Locale.ENGLISH);
            HexDumpEncoder hexEncoder = new HexDumpEncoder();
            Object[] messageFields = new Object[]{NamedGroup.nameOf(this.serverShare.namedGroupId), Utilities.indent(hexEncoder.encode(this.serverShare.keyExchange), "    ")};
            return messageFormat.format(messageFields);
        }
    }

    static final class CHKeyShareSpec
    implements SSLExtension.SSLExtensionSpec {
        final List<KeyShareEntry> clientShares;

        private CHKeyShareSpec(List<KeyShareEntry> clientShares) {
            this.clientShares = clientShares;
        }

        private CHKeyShareSpec(HandshakeContext handshakeContext, ByteBuffer buffer) throws IOException {
            if (buffer.remaining() < 2) {
                throw handshakeContext.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid key_share extension: insufficient data (length=" + buffer.remaining() + ")"));
            }
            int listLen = Record.getInt16(buffer);
            if (listLen != buffer.remaining()) {
                throw handshakeContext.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid key_share extension: incorrect list length (length=" + listLen + ")"));
            }
            LinkedList<KeyShareEntry> keyShares = new LinkedList<KeyShareEntry>();
            while (buffer.hasRemaining()) {
                int namedGroupId = Record.getInt16(buffer);
                byte[] keyExchange = Record.getBytes16(buffer);
                if (keyExchange.length == 0) {
                    throw handshakeContext.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid key_share extension: empty key_exchange"));
                }
                keyShares.add(new KeyShareEntry(namedGroupId, keyExchange));
            }
            this.clientShares = Collections.unmodifiableList(keyShares);
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"client_shares\": '['{0}\n']'", Locale.ENGLISH);
            StringBuilder builder = new StringBuilder(512);
            for (KeyShareEntry entry : this.clientShares) {
                builder.append(entry.toString());
            }
            Object[] messageFields = new Object[]{Utilities.indent(builder.toString())};
            return messageFormat.format(messageFields);
        }
    }

    private static final class KeyShareEntry {
        final int namedGroupId;
        final byte[] keyExchange;

        private KeyShareEntry(int namedGroupId, byte[] keyExchange) {
            this.namedGroupId = namedGroupId;
            this.keyExchange = keyExchange;
        }

        private byte[] getEncoded() {
            byte[] buffer;
            block2: {
                buffer = new byte[this.keyExchange.length + 4];
                ByteBuffer m = ByteBuffer.wrap(buffer);
                try {
                    Record.putInt16(m, this.namedGroupId);
                    Record.putBytes16(m, this.keyExchange);
                }
                catch (IOException ioe) {
                    if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,handshake")) break block2;
                    SSLLogger.warning("Unlikely IOException", ioe);
                }
            }
            return buffer;
        }

        private int getEncodedSize() {
            return this.keyExchange.length + 4;
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\n'{'\n  \"named group\": {0}\n  \"key_exchange\": '{'\n{1}\n  '}'\n'}',", Locale.ENGLISH);
            HexDumpEncoder hexEncoder = new HexDumpEncoder();
            Object[] messageFields = new Object[]{NamedGroup.nameOf(this.namedGroupId), Utilities.indent(hexEncoder.encode(this.keyExchange), "    ")};
            return messageFormat.format(messageFields);
        }
    }
}

