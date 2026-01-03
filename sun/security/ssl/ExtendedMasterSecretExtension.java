/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLProtocolException;
import sun.security.ssl.Alert;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeAbsence;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.SSLConfiguration;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLStringizer;
import sun.security.ssl.ServerHandshakeContext;

final class ExtendedMasterSecretExtension {
    static final HandshakeProducer chNetworkProducer = new CHExtendedMasterSecretProducer();
    static final SSLExtension.ExtensionConsumer chOnLoadConsumer = new CHExtendedMasterSecretConsumer();
    static final HandshakeAbsence chOnLoadAbsence = new CHExtendedMasterSecretAbsence();
    static final HandshakeProducer shNetworkProducer = new SHExtendedMasterSecretProducer();
    static final SSLExtension.ExtensionConsumer shOnLoadConsumer = new SHExtendedMasterSecretConsumer();
    static final HandshakeAbsence shOnLoadAbsence = new SHExtendedMasterSecretAbsence();
    static final SSLStringizer emsStringizer = new ExtendedMasterSecretStringizer();

    ExtendedMasterSecretExtension() {
    }

    private static final class CHExtendedMasterSecretProducer
    implements HandshakeProducer {
        private CHExtendedMasterSecretProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!(chc.sslConfig.isAvailable(SSLExtension.CH_EXTENDED_MASTER_SECRET) && SSLConfiguration.useExtendedMasterSecret && chc.conContext.protocolVersion.useTLS10PlusSpec())) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable extended_master_secret extension", new Object[0]);
                }
                return null;
            }
            if (chc.handshakeSession == null || chc.handshakeSession.useExtendedMasterSecret) {
                byte[] extData = new byte[]{};
                chc.handshakeExtensions.put(SSLExtension.CH_EXTENDED_MASTER_SECRET, ExtendedMasterSecretSpec.NOMINAL);
                return extData;
            }
            return null;
        }
    }

    private static final class CHExtendedMasterSecretConsumer
    implements SSLExtension.ExtensionConsumer {
        private CHExtendedMasterSecretConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!(shc.sslConfig.isAvailable(SSLExtension.CH_EXTENDED_MASTER_SECRET) && SSLConfiguration.useExtendedMasterSecret && shc.negotiatedProtocol.useTLS10PlusSpec())) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable extension: " + SSLExtension.CH_EXTENDED_MASTER_SECRET.name, new Object[0]);
                }
                return;
            }
            ExtendedMasterSecretSpec spec = new ExtendedMasterSecretSpec(shc, buffer);
            if (shc.isResumption && shc.resumingSession != null && !shc.resumingSession.useExtendedMasterSecret) {
                shc.isResumption = false;
                shc.resumingSession = null;
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("abort session resumption which did not use Extended Master Secret extension", new Object[0]);
                }
            }
            shc.handshakeExtensions.put(SSLExtension.CH_EXTENDED_MASTER_SECRET, ExtendedMasterSecretSpec.NOMINAL);
        }
    }

    private static final class CHExtendedMasterSecretAbsence
    implements HandshakeAbsence {
        private CHExtendedMasterSecretAbsence() {
        }

        @Override
        public void absent(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.CH_EXTENDED_MASTER_SECRET) || !SSLConfiguration.useExtendedMasterSecret) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable extension: " + SSLExtension.CH_EXTENDED_MASTER_SECRET.name, new Object[0]);
                }
                return;
            }
            if (shc.negotiatedProtocol.useTLS10PlusSpec() && !SSLConfiguration.allowLegacyMasterSecret) {
                throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Extended Master Secret extension is required");
            }
            if (shc.isResumption && shc.resumingSession != null) {
                if (shc.resumingSession.useExtendedMasterSecret) {
                    throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Missing Extended Master Secret extension on session resumption");
                }
                if (!SSLConfiguration.allowLegacyResumption) {
                    throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Missing Extended Master Secret extension on session resumption");
                }
                shc.isResumption = false;
                shc.resumingSession = null;
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("abort session resumption, missing Extended Master Secret extension", new Object[0]);
                }
            }
        }
    }

    private static final class SHExtendedMasterSecretProducer
    implements HandshakeProducer {
        private SHExtendedMasterSecretProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (shc.handshakeSession.useExtendedMasterSecret) {
                byte[] extData = new byte[]{};
                shc.handshakeExtensions.put(SSLExtension.SH_EXTENDED_MASTER_SECRET, ExtendedMasterSecretSpec.NOMINAL);
                return extData;
            }
            return null;
        }
    }

    private static final class SHExtendedMasterSecretConsumer
    implements SSLExtension.ExtensionConsumer {
        private SHExtendedMasterSecretConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            ExtendedMasterSecretSpec requstedSpec = (ExtendedMasterSecretSpec)chc.handshakeExtensions.get(SSLExtension.CH_EXTENDED_MASTER_SECRET);
            if (requstedSpec == null) {
                throw chc.conContext.fatal(Alert.UNSUPPORTED_EXTENSION, "Server sent the extended_master_secret extension improperly");
            }
            ExtendedMasterSecretSpec spec = new ExtendedMasterSecretSpec(chc, buffer);
            if (chc.isResumption && chc.resumingSession != null && !chc.resumingSession.useExtendedMasterSecret) {
                throw chc.conContext.fatal(Alert.UNSUPPORTED_EXTENSION, "Server sent an unexpected extended_master_secret extension on session resumption");
            }
            chc.handshakeExtensions.put(SSLExtension.SH_EXTENDED_MASTER_SECRET, ExtendedMasterSecretSpec.NOMINAL);
        }
    }

    private static final class SHExtendedMasterSecretAbsence
    implements HandshakeAbsence {
        private SHExtendedMasterSecretAbsence() {
        }

        @Override
        public void absent(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (SSLConfiguration.useExtendedMasterSecret && !SSLConfiguration.allowLegacyMasterSecret) {
                throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Extended Master Secret extension is required");
            }
            if (chc.isResumption && chc.resumingSession != null) {
                if (chc.resumingSession.useExtendedMasterSecret) {
                    throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Missing Extended Master Secret extension on session resumption");
                }
                if (SSLConfiguration.useExtendedMasterSecret && !SSLConfiguration.allowLegacyResumption && chc.negotiatedProtocol.useTLS10PlusSpec()) {
                    throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Extended Master Secret extension is required");
                }
            }
        }
    }

    private static final class ExtendedMasterSecretStringizer
    implements SSLStringizer {
        private ExtendedMasterSecretStringizer() {
        }

        @Override
        public String toString(HandshakeContext hc, ByteBuffer buffer) {
            try {
                return new ExtendedMasterSecretSpec(hc, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }

    static final class ExtendedMasterSecretSpec
    implements SSLExtension.SSLExtensionSpec {
        static final ExtendedMasterSecretSpec NOMINAL = new ExtendedMasterSecretSpec();

        private ExtendedMasterSecretSpec() {
        }

        private ExtendedMasterSecretSpec(HandshakeContext hc, ByteBuffer m) throws IOException {
            if (m.hasRemaining()) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid extended_master_secret extension data: not empty"));
            }
        }

        public String toString() {
            return "<empty>";
        }
    }
}

