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
import sun.security.ssl.HandshakeAbsence;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.Record;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLStringizer;
import sun.security.ssl.ServerHandshakeContext;

final class PskKeyExchangeModesExtension {
    static final HandshakeProducer chNetworkProducer = new PskKeyExchangeModesProducer();
    static final SSLExtension.ExtensionConsumer chOnLoadConsumer = new PskKeyExchangeModesConsumer();
    static final HandshakeAbsence chOnLoadAbsence = new PskKeyExchangeModesOnLoadAbsence();
    static final HandshakeAbsence chOnTradeAbsence = new PskKeyExchangeModesOnTradeAbsence();
    static final SSLStringizer pkemStringizer = new PskKeyExchangeModesStringizer();

    PskKeyExchangeModesExtension() {
    }

    private static final class PskKeyExchangeModesProducer
    implements HandshakeProducer {
        private PskKeyExchangeModesProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.PSK_KEY_EXCHANGE_MODES)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("Ignore unavailable psk_key_exchange_modes extension", new Object[0]);
                }
                return null;
            }
            byte[] extData = new byte[]{1, 1};
            chc.handshakeExtensions.put(SSLExtension.PSK_KEY_EXCHANGE_MODES, PskKeyExchangeModesSpec.DEFAULT);
            return extData;
        }
    }

    private static final class PskKeyExchangeModesConsumer
    implements SSLExtension.ExtensionConsumer {
        private PskKeyExchangeModesConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.PSK_KEY_EXCHANGE_MODES)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable psk_key_exchange_modes extension", new Object[0]);
                }
                if (shc.isResumption && shc.resumingSession != null) {
                    shc.isResumption = false;
                    shc.resumingSession = null;
                }
                return;
            }
            PskKeyExchangeModesSpec spec = new PskKeyExchangeModesSpec(shc, buffer);
            shc.handshakeExtensions.put(SSLExtension.PSK_KEY_EXCHANGE_MODES, spec);
            if (shc.isResumption && !spec.contains(PskKeyExchangeMode.PSK_DHE_KE)) {
                shc.isResumption = false;
                shc.resumingSession = null;
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("abort session resumption, no supported psk_dhe_ke PSK key exchange mode", new Object[0]);
                }
            }
        }
    }

    private static final class PskKeyExchangeModesOnLoadAbsence
    implements HandshakeAbsence {
        private PskKeyExchangeModesOnLoadAbsence() {
        }

        @Override
        public void absent(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (shc.isResumption) {
                shc.isResumption = false;
                shc.resumingSession = null;
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("abort session resumption, no supported psk_dhe_ke PSK key exchange mode", new Object[0]);
                }
            }
        }
    }

    private static final class PskKeyExchangeModesOnTradeAbsence
    implements HandshakeAbsence {
        private PskKeyExchangeModesOnTradeAbsence() {
        }

        @Override
        public void absent(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            SSLExtension.SSLExtensionSpec spec = (SSLExtension.SSLExtensionSpec)shc.handshakeExtensions.get(SSLExtension.CH_PRE_SHARED_KEY);
            if (spec != null) {
                throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "pre_shared_key key extension is offered without a psk_key_exchange_modes extension");
            }
        }
    }

    private static final class PskKeyExchangeModesStringizer
    implements SSLStringizer {
        private PskKeyExchangeModesStringizer() {
        }

        @Override
        public String toString(HandshakeContext hc, ByteBuffer buffer) {
            try {
                return new PskKeyExchangeModesSpec(hc, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }

    static final class PskKeyExchangeModesSpec
    implements SSLExtension.SSLExtensionSpec {
        private static final PskKeyExchangeModesSpec DEFAULT = new PskKeyExchangeModesSpec(new byte[]{PskKeyExchangeMode.PSK_DHE_KE.id});
        final byte[] modes;

        PskKeyExchangeModesSpec(byte[] modes) {
            this.modes = modes;
        }

        PskKeyExchangeModesSpec(HandshakeContext hc, ByteBuffer m) throws IOException {
            if (m.remaining() < 2) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid psk_key_exchange_modes extension: insufficient data"));
            }
            this.modes = Record.getBytes8(m);
        }

        boolean contains(PskKeyExchangeMode mode) {
            if (this.modes != null) {
                for (byte m : this.modes) {
                    if (mode.id != m) continue;
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"ke_modes\": '['{0}']'", Locale.ENGLISH);
            if (this.modes == null || this.modes.length == 0) {
                Object[] messageFields = new Object[]{"<no PSK key exchange modes specified>"};
                return messageFormat.format(messageFields);
            }
            StringBuilder builder = new StringBuilder(64);
            boolean isFirst = true;
            for (byte mode : this.modes) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    builder.append(", ");
                }
                builder.append(PskKeyExchangeMode.nameOf(mode));
            }
            Object[] messageFields = new Object[]{builder.toString()};
            return messageFormat.format(messageFields);
        }
    }

    static enum PskKeyExchangeMode {
        PSK_KE(0, "psk_ke"),
        PSK_DHE_KE(1, "psk_dhe_ke");

        final byte id;
        final String name;

        private PskKeyExchangeMode(byte id, String name) {
            this.id = id;
            this.name = name;
        }

        static PskKeyExchangeMode valueOf(byte id) {
            for (PskKeyExchangeMode pkem : PskKeyExchangeMode.values()) {
                if (pkem.id != id) continue;
                return pkem;
            }
            return null;
        }

        static String nameOf(byte id) {
            for (PskKeyExchangeMode pkem : PskKeyExchangeMode.values()) {
                if (pkem.id != id) continue;
                return pkem.name;
            }
            return "<UNKNOWN PskKeyExchangeMode TYPE: " + (id & 0xFF) + ">";
        }
    }
}

