/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import javax.net.ssl.SSLProtocolException;
import sun.security.ssl.Alert;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ClientHello;
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
import sun.security.ssl.Utilities;
import sun.security.util.ByteArrays;

final class RenegoInfoExtension {
    static final HandshakeProducer chNetworkProducer = new CHRenegotiationInfoProducer();
    static final SSLExtension.ExtensionConsumer chOnLoadConsumer = new CHRenegotiationInfoConsumer();
    static final HandshakeAbsence chOnLoadAbsence = new CHRenegotiationInfoAbsence();
    static final HandshakeProducer shNetworkProducer = new SHRenegotiationInfoProducer();
    static final SSLExtension.ExtensionConsumer shOnLoadConsumer = new SHRenegotiationInfoConsumer();
    static final HandshakeAbsence shOnLoadAbsence = new SHRenegotiationInfoAbsence();
    static final SSLStringizer rniStringizer = new RenegotiationInfoStringizer();

    RenegoInfoExtension() {
    }

    private static final class CHRenegotiationInfoProducer
    implements HandshakeProducer {
        private CHRenegotiationInfoProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.CH_RENEGOTIATION_INFO)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable renegotiation_info extension", new Object[0]);
                }
                return null;
            }
            if (!chc.conContext.isNegotiated) {
                if (chc.activeCipherSuites.contains((Object)CipherSuite.TLS_EMPTY_RENEGOTIATION_INFO_SCSV)) {
                    return null;
                }
                byte[] extData = new byte[]{0};
                chc.handshakeExtensions.put(SSLExtension.CH_RENEGOTIATION_INFO, RenegotiationInfoSpec.NOMINAL);
                return extData;
            }
            if (chc.conContext.secureRenegotiation) {
                byte[] extData = new byte[chc.conContext.clientVerifyData.length + 1];
                ByteBuffer m = ByteBuffer.wrap(extData);
                Record.putBytes8(m, chc.conContext.clientVerifyData);
                chc.handshakeExtensions.put(SSLExtension.CH_RENEGOTIATION_INFO, RenegotiationInfoSpec.NOMINAL);
                return extData;
            }
            if (HandshakeContext.allowUnsafeRenegotiation) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("Using insecure renegotiation", new Object[0]);
                }
                return null;
            }
            throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "insecure renegotiation is not allowed");
        }
    }

    private static final class CHRenegotiationInfoConsumer
    implements SSLExtension.ExtensionConsumer {
        private CHRenegotiationInfoConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.CH_RENEGOTIATION_INFO)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable extension: " + SSLExtension.CH_RENEGOTIATION_INFO.name, new Object[0]);
                }
                return;
            }
            RenegotiationInfoSpec spec = new RenegotiationInfoSpec(shc, buffer);
            if (!shc.conContext.isNegotiated) {
                if (spec.renegotiatedConnection.length != 0) {
                    throw shc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Invalid renegotiation_info extension data: not empty");
                }
                shc.conContext.secureRenegotiation = true;
            } else {
                if (!shc.conContext.secureRenegotiation) {
                    throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "The renegotiation_info is present in a insecure renegotiation");
                }
                if (!MessageDigest.isEqual(shc.conContext.clientVerifyData, spec.renegotiatedConnection)) {
                    throw shc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Invalid renegotiation_info extension data: incorrect verify data in ClientHello");
                }
            }
            shc.handshakeExtensions.put(SSLExtension.CH_RENEGOTIATION_INFO, RenegotiationInfoSpec.NOMINAL);
        }
    }

    private static final class CHRenegotiationInfoAbsence
    implements HandshakeAbsence {
        private CHRenegotiationInfoAbsence() {
        }

        @Override
        public void absent(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            ClientHello.ClientHelloMessage clientHello = (ClientHello.ClientHelloMessage)message;
            if (!shc.conContext.isNegotiated) {
                for (int id : clientHello.cipherSuiteIds) {
                    if (id != CipherSuite.TLS_EMPTY_RENEGOTIATION_INFO_SCSV.id) continue;
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                        SSLLogger.finest("Safe renegotiation, using the SCSV signaling", new Object[0]);
                    }
                    shc.conContext.secureRenegotiation = true;
                    return;
                }
                if (!HandshakeContext.allowLegacyHelloMessages) {
                    throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Failed to negotiate the use of secure renegotiation");
                }
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("Warning: No renegotiation indication in ClientHello, allow legacy ClientHello", new Object[0]);
                }
                shc.conContext.secureRenegotiation = false;
            } else {
                if (shc.conContext.secureRenegotiation) {
                    throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Inconsistent secure renegotiation indication");
                }
                if (HandshakeContext.allowUnsafeRenegotiation) {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                        SSLLogger.warning("Using insecure renegotiation", new Object[0]);
                    }
                } else {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                        SSLLogger.fine("Terminate insecure renegotiation", new Object[0]);
                    }
                    throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Unsafe renegotiation is not allowed");
                }
            }
        }
    }

    private static final class SHRenegotiationInfoProducer
    implements HandshakeProducer {
        private SHRenegotiationInfoProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            RenegotiationInfoSpec requestedSpec = (RenegotiationInfoSpec)shc.handshakeExtensions.get(SSLExtension.CH_RENEGOTIATION_INFO);
            if (requestedSpec == null && !shc.conContext.secureRenegotiation) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.finest("Ignore unavailable renegotiation_info extension", new Object[0]);
                }
                return null;
            }
            if (!shc.conContext.secureRenegotiation) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.finest("No secure renegotiation has been negotiated", new Object[0]);
                }
                return null;
            }
            if (!shc.conContext.isNegotiated) {
                byte[] extData = new byte[]{0};
                shc.handshakeExtensions.put(SSLExtension.SH_RENEGOTIATION_INFO, RenegotiationInfoSpec.NOMINAL);
                return extData;
            }
            int infoLen = shc.conContext.clientVerifyData.length + shc.conContext.serverVerifyData.length;
            byte[] extData = new byte[infoLen + 1];
            ByteBuffer m = ByteBuffer.wrap(extData);
            Record.putInt8(m, infoLen);
            m.put(shc.conContext.clientVerifyData);
            m.put(shc.conContext.serverVerifyData);
            shc.handshakeExtensions.put(SSLExtension.SH_RENEGOTIATION_INFO, RenegotiationInfoSpec.NOMINAL);
            return extData;
        }
    }

    private static final class SHRenegotiationInfoConsumer
    implements SSLExtension.ExtensionConsumer {
        private SHRenegotiationInfoConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            RenegotiationInfoSpec requestedSpec = (RenegotiationInfoSpec)chc.handshakeExtensions.get(SSLExtension.CH_RENEGOTIATION_INFO);
            if (requestedSpec == null && !chc.activeCipherSuites.contains((Object)CipherSuite.TLS_EMPTY_RENEGOTIATION_INFO_SCSV)) {
                throw chc.conContext.fatal(Alert.INTERNAL_ERROR, "Missing renegotiation_info and SCSV detected in ClientHello");
            }
            RenegotiationInfoSpec spec = new RenegotiationInfoSpec(chc, buffer);
            if (!chc.conContext.isNegotiated) {
                if (spec.renegotiatedConnection.length != 0) {
                    throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Invalid renegotiation_info in ServerHello: not empty renegotiated_connection");
                }
                chc.conContext.secureRenegotiation = true;
            } else {
                int infoLen = chc.conContext.clientVerifyData.length + chc.conContext.serverVerifyData.length;
                if (spec.renegotiatedConnection.length != infoLen) {
                    throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Invalid renegotiation_info in ServerHello: invalid renegotiated_connection length (" + spec.renegotiatedConnection.length + ")");
                }
                byte[] cvd = chc.conContext.clientVerifyData;
                if (!ByteArrays.isEqual(spec.renegotiatedConnection, 0, cvd.length, cvd, 0, cvd.length)) {
                    throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Invalid renegotiation_info in ServerHello: unmatched client_verify_data value");
                }
                byte[] svd = chc.conContext.serverVerifyData;
                if (!ByteArrays.isEqual(spec.renegotiatedConnection, cvd.length, infoLen, svd, 0, svd.length)) {
                    throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Invalid renegotiation_info in ServerHello: unmatched server_verify_data value");
                }
            }
            chc.handshakeExtensions.put(SSLExtension.SH_RENEGOTIATION_INFO, RenegotiationInfoSpec.NOMINAL);
        }
    }

    private static final class SHRenegotiationInfoAbsence
    implements HandshakeAbsence {
        private SHRenegotiationInfoAbsence() {
        }

        @Override
        public void absent(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            RenegotiationInfoSpec requestedSpec = (RenegotiationInfoSpec)chc.handshakeExtensions.get(SSLExtension.CH_RENEGOTIATION_INFO);
            if (requestedSpec == null && !chc.activeCipherSuites.contains((Object)CipherSuite.TLS_EMPTY_RENEGOTIATION_INFO_SCSV)) {
                throw chc.conContext.fatal(Alert.INTERNAL_ERROR, "Missing renegotiation_info and SCSV detected in ClientHello");
            }
            if (!chc.conContext.isNegotiated) {
                if (!HandshakeContext.allowLegacyHelloMessages) {
                    throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Failed to negotiate the use of secure renegotiation");
                }
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("Warning: No renegotiation indication in ServerHello, allow legacy ServerHello", new Object[0]);
                }
                chc.conContext.secureRenegotiation = false;
            } else {
                if (chc.conContext.secureRenegotiation) {
                    throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Inconsistent secure renegotiation indication");
                }
                if (HandshakeContext.allowUnsafeRenegotiation) {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                        SSLLogger.warning("Using insecure renegotiation", new Object[0]);
                    }
                } else {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                        SSLLogger.fine("Terminate insecure renegotiation", new Object[0]);
                    }
                    throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Unsafe renegotiation is not allowed");
                }
            }
        }
    }

    private static final class RenegotiationInfoStringizer
    implements SSLStringizer {
        private RenegotiationInfoStringizer() {
        }

        @Override
        public String toString(HandshakeContext hc, ByteBuffer buffer) {
            try {
                return new RenegotiationInfoSpec(hc, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }

    static final class RenegotiationInfoSpec
    implements SSLExtension.SSLExtensionSpec {
        static final RenegotiationInfoSpec NOMINAL = new RenegotiationInfoSpec(new byte[0]);
        private final byte[] renegotiatedConnection;

        private RenegotiationInfoSpec(byte[] renegotiatedConnection) {
            this.renegotiatedConnection = Arrays.copyOf(renegotiatedConnection, renegotiatedConnection.length);
        }

        private RenegotiationInfoSpec(HandshakeContext hc, ByteBuffer m) throws IOException {
            if (!m.hasRemaining() || m.remaining() < 1) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid renegotiation_info extension data: insufficient data"));
            }
            this.renegotiatedConnection = Record.getBytes8(m);
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"renegotiated connection\": '['{0}']'", Locale.ENGLISH);
            Object[] messageFields = this.renegotiatedConnection.length == 0 ? new Object[]{"<no renegotiated connection>"} : new Object[]{Utilities.toHexString(this.renegotiatedConnection)};
            return messageFormat.format(messageFields);
        }
    }
}

