/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Locale;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLProtocolException;
import sun.security.ssl.ClientAuthType;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.TransportContext;

enum Alert {
    CLOSE_NOTIFY(0, "close_notify", false),
    UNEXPECTED_MESSAGE(10, "unexpected_message", false),
    BAD_RECORD_MAC(20, "bad_record_mac", false),
    DECRYPTION_FAILED(21, "decryption_failed", false),
    RECORD_OVERFLOW(22, "record_overflow", false),
    DECOMPRESSION_FAILURE(30, "decompression_failure", false),
    HANDSHAKE_FAILURE(40, "handshake_failure", true),
    NO_CERTIFICATE(41, "no_certificate", true),
    BAD_CERTIFICATE(42, "bad_certificate", true),
    UNSUPPORTED_CERTIFICATE(43, "unsupported_certificate", true),
    CERTIFICATE_REVOKED(44, "certificate_revoked", true),
    CERTIFICATE_EXPIRED(45, "certificate_expired", true),
    CERTIFICATE_UNKNOWN(46, "certificate_unknown", true),
    ILLEGAL_PARAMETER(47, "illegal_parameter", true),
    UNKNOWN_CA(48, "unknown_ca", true),
    ACCESS_DENIED(49, "access_denied", true),
    DECODE_ERROR(50, "decode_error", true),
    DECRYPT_ERROR(51, "decrypt_error", true),
    EXPORT_RESTRICTION(60, "export_restriction", true),
    PROTOCOL_VERSION(70, "protocol_version", true),
    INSUFFICIENT_SECURITY(71, "insufficient_security", true),
    INTERNAL_ERROR(80, "internal_error", false),
    INAPPROPRIATE_FALLBACK(86, "inappropriate_fallback", false),
    USER_CANCELED(90, "user_canceled", false),
    NO_RENEGOTIATION(100, "no_renegotiation", true),
    MISSING_EXTENSION(109, "missing_extension", true),
    UNSUPPORTED_EXTENSION(110, "unsupported_extension", true),
    CERT_UNOBTAINABLE(111, "certificate_unobtainable", true),
    UNRECOGNIZED_NAME(112, "unrecognized_name", true),
    BAD_CERT_STATUS_RESPONSE(113, "bad_certificate_status_response", true),
    BAD_CERT_HASH_VALUE(114, "bad_certificate_hash_value", true),
    UNKNOWN_PSK_IDENTITY(115, "unknown_psk_identity", true),
    CERTIFICATE_REQUIRED(116, "certificate_required", true),
    NO_APPLICATION_PROTOCOL(120, "no_application_protocol", true);

    final byte id;
    final String description;
    final boolean handshakeOnly;
    static final SSLConsumer alertConsumer;

    private Alert(byte id, String description, boolean handshakeOnly) {
        this.id = id;
        this.description = description;
        this.handshakeOnly = handshakeOnly;
    }

    static Alert valueOf(byte id) {
        for (Alert al : Alert.values()) {
            if (al.id != id) continue;
            return al;
        }
        return null;
    }

    static String nameOf(byte id) {
        for (Alert al : Alert.values()) {
            if (al.id != id) continue;
            return al.description;
        }
        return "UNKNOWN ALERT (" + (id & 0xFF) + ")";
    }

    SSLException createSSLException(String reason) {
        return this.createSSLException(reason, null);
    }

    SSLException createSSLException(String reason, Throwable cause) {
        if (reason == null) {
            String string = reason = cause != null ? cause.getMessage() : "";
        }
        if (cause instanceof IOException) {
            return new SSLException(reason, cause);
        }
        if (this == UNEXPECTED_MESSAGE) {
            return new SSLProtocolException(reason, cause);
        }
        if (this.handshakeOnly) {
            return new SSLHandshakeException(reason, cause);
        }
        return new SSLException(reason, cause);
    }

    static {
        alertConsumer = new AlertConsumer();
    }

    private static final class AlertConsumer
    implements SSLConsumer {
        private AlertConsumer() {
        }

        /*
         * Enabled aggressive block sorting
         */
        @Override
        public void consume(ConnectionContext context, ByteBuffer m) throws IOException {
            String diagnostic;
            TransportContext tc = (TransportContext)context;
            AlertMessage am = new AlertMessage(tc, m);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                SSLLogger.fine("Received alert message", am);
            }
            Level level = Level.valueOf(am.level);
            Alert alert = Alert.valueOf(am.id);
            if (alert == CLOSE_NOTIFY) {
                tc.isInputCloseNotified = true;
                tc.closeInbound();
                if (tc.peerUserCanceled) {
                    tc.closeOutbound();
                    return;
                }
                if (tc.handshakeContext == null) return;
                throw tc.fatal(UNEXPECTED_MESSAGE, "Received close_notify during handshake");
            }
            if (alert == USER_CANCELED) {
                if (level != Level.WARNING) throw tc.fatal(alert, "Received fatal close_notify alert", true, null);
                tc.peerUserCanceled = true;
                return;
            }
            if (level == Level.WARNING && alert != null) {
                if (!alert.handshakeOnly) return;
                if (tc.handshakeContext == null) return;
                if (tc.sslConfig.isClientMode) throw tc.fatal(HANDSHAKE_FAILURE, "received handshake warning: " + alert.description);
                if (alert != NO_CERTIFICATE) throw tc.fatal(HANDSHAKE_FAILURE, "received handshake warning: " + alert.description);
                if (tc.sslConfig.clientAuthType != ClientAuthType.CLIENT_AUTH_REQUESTED) {
                    throw tc.fatal(HANDSHAKE_FAILURE, "received handshake warning: " + alert.description);
                }
                tc.handshakeContext.handshakeConsumers.remove(SSLHandshake.CERTIFICATE.id);
                tc.handshakeContext.handshakeConsumers.remove(SSLHandshake.CERTIFICATE_VERIFY.id);
                return;
            }
            if (alert == null) {
                alert = UNEXPECTED_MESSAGE;
                diagnostic = "Unknown alert description (" + am.id + ")";
                throw tc.fatal(alert, diagnostic, true, null);
            }
            diagnostic = "Received fatal alert: " + alert.description;
            throw tc.fatal(alert, diagnostic, true, null);
        }
    }

    private static final class AlertMessage {
        private final byte level;
        private final byte id;

        AlertMessage(TransportContext context, ByteBuffer m) throws IOException {
            if (m.remaining() != 2) {
                throw context.fatal(ILLEGAL_PARAMETER, "Invalid Alert message: no sufficient data");
            }
            this.level = m.get();
            this.id = m.get();
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"Alert\": '{'\n  \"level\"      : \"{0}\",\n  \"description\": \"{1}\"\n'}'", Locale.ENGLISH);
            Object[] messageFields = new Object[]{Level.nameOf(this.level), Alert.nameOf(this.id)};
            return messageFormat.format(messageFields);
        }
    }

    static enum Level {
        WARNING(1, "warning"),
        FATAL(2, "fatal");

        final byte level;
        final String description;

        private Level(byte level, String description) {
            this.level = level;
            this.description = description;
        }

        static Level valueOf(byte level) {
            for (Level lv : Level.values()) {
                if (lv.level != level) continue;
                return lv;
            }
            return null;
        }

        static String nameOf(byte level) {
            for (Level lv : Level.values()) {
                if (lv.level != level) continue;
                return lv.description;
            }
            return "UNKNOWN ALERT LEVEL (" + (level & 0xFF) + ")";
        }
    }
}

