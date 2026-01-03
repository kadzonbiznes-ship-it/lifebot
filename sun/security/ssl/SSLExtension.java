/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;
import sun.security.action.GetPropertyAction;
import sun.security.ssl.AlpnExtension;
import sun.security.ssl.CertSignAlgsExtension;
import sun.security.ssl.CertStatusExtension;
import sun.security.ssl.CertificateAuthoritiesExtension;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.CookieExtension;
import sun.security.ssl.ECPointFormatsExtension;
import sun.security.ssl.ExtendedMasterSecretExtension;
import sun.security.ssl.HandshakeAbsence;
import sun.security.ssl.HandshakeConsumer;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.KeyShareExtension;
import sun.security.ssl.MaxFragExtension;
import sun.security.ssl.PreSharedKeyExtension;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.PskKeyExchangeModesExtension;
import sun.security.ssl.RenegoInfoExtension;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLStringizer;
import sun.security.ssl.ServerNameExtension;
import sun.security.ssl.SessionTicketExtension;
import sun.security.ssl.SignatureAlgorithmsExtension;
import sun.security.ssl.SupportedGroupsExtension;
import sun.security.ssl.SupportedVersionsExtension;
import sun.security.ssl.Utilities;
import sun.security.util.HexDumpEncoder;

enum SSLExtension implements SSLStringizer
{
    CH_SERVER_NAME(0, "server_name", SSLHandshake.CLIENT_HELLO, ProtocolVersion.PROTOCOLS_TO_13, ServerNameExtension.chNetworkProducer, ServerNameExtension.chOnLoadConsumer, null, null, null, ServerNameExtension.chStringizer),
    SH_SERVER_NAME(0, "server_name", SSLHandshake.SERVER_HELLO, ProtocolVersion.PROTOCOLS_TO_12, ServerNameExtension.shNetworkProducer, ServerNameExtension.shOnLoadConsumer, null, null, null, ServerNameExtension.shStringizer),
    EE_SERVER_NAME(0, "server_name", SSLHandshake.ENCRYPTED_EXTENSIONS, ProtocolVersion.PROTOCOLS_OF_13, ServerNameExtension.eeNetworkProducer, ServerNameExtension.eeOnLoadConsumer, null, null, null, ServerNameExtension.shStringizer),
    CH_MAX_FRAGMENT_LENGTH(1, "max_fragment_length", SSLHandshake.CLIENT_HELLO, ProtocolVersion.PROTOCOLS_TO_13, MaxFragExtension.chNetworkProducer, MaxFragExtension.chOnLoadConsumer, null, null, null, MaxFragExtension.maxFragLenStringizer),
    SH_MAX_FRAGMENT_LENGTH(1, "max_fragment_length", SSLHandshake.SERVER_HELLO, ProtocolVersion.PROTOCOLS_TO_12, MaxFragExtension.shNetworkProducer, MaxFragExtension.shOnLoadConsumer, null, MaxFragExtension.shOnTradeConsumer, null, MaxFragExtension.maxFragLenStringizer),
    EE_MAX_FRAGMENT_LENGTH(1, "max_fragment_length", SSLHandshake.ENCRYPTED_EXTENSIONS, ProtocolVersion.PROTOCOLS_OF_13, MaxFragExtension.eeNetworkProducer, MaxFragExtension.eeOnLoadConsumer, null, MaxFragExtension.eeOnTradeConsumer, null, MaxFragExtension.maxFragLenStringizer),
    CLIENT_CERTIFICATE_URL(2, "client_certificate_url"),
    TRUSTED_CA_KEYS(3, "trusted_ca_keys"),
    TRUNCATED_HMAC(4, "truncated_hmac"),
    CH_STATUS_REQUEST(5, "status_request", SSLHandshake.CLIENT_HELLO, ProtocolVersion.PROTOCOLS_TO_13, CertStatusExtension.chNetworkProducer, CertStatusExtension.chOnLoadConsumer, null, null, null, CertStatusExtension.certStatusReqStringizer),
    SH_STATUS_REQUEST(5, "status_request", SSLHandshake.SERVER_HELLO, ProtocolVersion.PROTOCOLS_TO_12, CertStatusExtension.shNetworkProducer, CertStatusExtension.shOnLoadConsumer, null, null, null, CertStatusExtension.certStatusReqStringizer),
    CR_STATUS_REQUEST(5, "status_request"),
    CT_STATUS_REQUEST(5, "status_request", SSLHandshake.CERTIFICATE, ProtocolVersion.PROTOCOLS_OF_13, CertStatusExtension.ctNetworkProducer, CertStatusExtension.ctOnLoadConsumer, null, null, null, CertStatusExtension.certStatusRespStringizer),
    USER_MAPPING(6, "user_mapping"),
    CLIENT_AUTHZ(7, "client_authz"),
    SERVER_AUTHZ(8, "server_authz"),
    CERT_TYPE(9, "cert_type"),
    CH_SUPPORTED_GROUPS(10, "supported_groups", SSLHandshake.CLIENT_HELLO, ProtocolVersion.PROTOCOLS_TO_13, SupportedGroupsExtension.chNetworkProducer, SupportedGroupsExtension.chOnLoadConsumer, null, null, SupportedGroupsExtension.chOnTradAbsence, SupportedGroupsExtension.sgsStringizer),
    EE_SUPPORTED_GROUPS(10, "supported_groups", SSLHandshake.ENCRYPTED_EXTENSIONS, ProtocolVersion.PROTOCOLS_OF_13, SupportedGroupsExtension.eeNetworkProducer, SupportedGroupsExtension.eeOnLoadConsumer, null, null, null, SupportedGroupsExtension.sgsStringizer),
    CH_EC_POINT_FORMATS(11, "ec_point_formats", SSLHandshake.CLIENT_HELLO, ProtocolVersion.PROTOCOLS_TO_12, ECPointFormatsExtension.chNetworkProducer, ECPointFormatsExtension.chOnLoadConsumer, null, null, null, ECPointFormatsExtension.epfStringizer),
    SH_EC_POINT_FORMATS(11, "ec_point_formats", SSLHandshake.SERVER_HELLO, ProtocolVersion.PROTOCOLS_TO_12, null, ECPointFormatsExtension.shOnLoadConsumer, null, null, null, ECPointFormatsExtension.epfStringizer),
    SRP(12, "srp"),
    USE_SRTP(14, "use_srtp"),
    HEARTBEAT(14, "heartbeat"),
    CH_ALPN(16, "application_layer_protocol_negotiation", SSLHandshake.CLIENT_HELLO, ProtocolVersion.PROTOCOLS_TO_13, AlpnExtension.chNetworkProducer, AlpnExtension.chOnLoadConsumer, AlpnExtension.chOnLoadAbsence, null, null, AlpnExtension.alpnStringizer),
    SH_ALPN(16, "application_layer_protocol_negotiation", SSLHandshake.SERVER_HELLO, ProtocolVersion.PROTOCOLS_TO_12, AlpnExtension.shNetworkProducer, AlpnExtension.shOnLoadConsumer, AlpnExtension.shOnLoadAbsence, null, null, AlpnExtension.alpnStringizer),
    EE_ALPN(16, "application_layer_protocol_negotiation", SSLHandshake.ENCRYPTED_EXTENSIONS, ProtocolVersion.PROTOCOLS_OF_13, AlpnExtension.shNetworkProducer, AlpnExtension.shOnLoadConsumer, AlpnExtension.shOnLoadAbsence, null, null, AlpnExtension.alpnStringizer),
    CH_STATUS_REQUEST_V2(17, "status_request_v2", SSLHandshake.CLIENT_HELLO, ProtocolVersion.PROTOCOLS_TO_12, CertStatusExtension.chV2NetworkProducer, CertStatusExtension.chV2OnLoadConsumer, null, null, null, CertStatusExtension.certStatusReqV2Stringizer),
    SH_STATUS_REQUEST_V2(17, "status_request_v2", SSLHandshake.SERVER_HELLO, ProtocolVersion.PROTOCOLS_TO_12, CertStatusExtension.shV2NetworkProducer, CertStatusExtension.shV2OnLoadConsumer, null, null, null, CertStatusExtension.certStatusReqV2Stringizer),
    SIGNED_CERT_TIMESTAMP(18, "signed_certificate_timestamp"),
    CLIENT_CERT_TYPE(19, "client_certificate_type"),
    SERVER_CERT_TYPE(20, "server_certificate_type"),
    PADDING(21, "padding"),
    ENCRYPT_THEN_MAC(22, "encrypt_then_mac"),
    CH_EXTENDED_MASTER_SECRET(23, "extended_master_secret", SSLHandshake.CLIENT_HELLO, ProtocolVersion.PROTOCOLS_TO_12, ExtendedMasterSecretExtension.chNetworkProducer, ExtendedMasterSecretExtension.chOnLoadConsumer, ExtendedMasterSecretExtension.chOnLoadAbsence, null, null, ExtendedMasterSecretExtension.emsStringizer),
    SH_EXTENDED_MASTER_SECRET(23, "extended_master_secret", SSLHandshake.SERVER_HELLO, ProtocolVersion.PROTOCOLS_TO_12, ExtendedMasterSecretExtension.shNetworkProducer, ExtendedMasterSecretExtension.shOnLoadConsumer, ExtendedMasterSecretExtension.shOnLoadAbsence, null, null, ExtendedMasterSecretExtension.emsStringizer),
    TOKEN_BINDING(24, "token_binding"),
    CACHED_INFO(25, "cached_info"),
    CH_SESSION_TICKET(35, "session_ticket", SSLHandshake.CLIENT_HELLO, ProtocolVersion.PROTOCOLS_10_12, SessionTicketExtension.chNetworkProducer, SessionTicketExtension.chOnLoadConsumer, null, null, null, SessionTicketExtension.steStringizer),
    SH_SESSION_TICKET(35, "session_ticket", SSLHandshake.SERVER_HELLO, ProtocolVersion.PROTOCOLS_10_12, SessionTicketExtension.shNetworkProducer, SessionTicketExtension.shOnLoadConsumer, null, null, null, SessionTicketExtension.steStringizer),
    CH_SIGNATURE_ALGORITHMS(13, "signature_algorithms", SSLHandshake.CLIENT_HELLO, ProtocolVersion.PROTOCOLS_12_13, SignatureAlgorithmsExtension.chNetworkProducer, SignatureAlgorithmsExtension.chOnLoadConsumer, SignatureAlgorithmsExtension.chOnLoadAbsence, SignatureAlgorithmsExtension.chOnTradeConsumer, SignatureAlgorithmsExtension.chOnTradeAbsence, SignatureAlgorithmsExtension.ssStringizer),
    CR_SIGNATURE_ALGORITHMS(13, "signature_algorithms", SSLHandshake.CERTIFICATE_REQUEST, ProtocolVersion.PROTOCOLS_OF_13, SignatureAlgorithmsExtension.crNetworkProducer, SignatureAlgorithmsExtension.crOnLoadConsumer, SignatureAlgorithmsExtension.crOnLoadAbsence, SignatureAlgorithmsExtension.crOnTradeConsumer, null, SignatureAlgorithmsExtension.ssStringizer),
    CH_EARLY_DATA(42, "early_data"),
    EE_EARLY_DATA(42, "early_data"),
    NST_EARLY_DATA(42, "early_data"),
    CH_SUPPORTED_VERSIONS(43, "supported_versions", SSLHandshake.CLIENT_HELLO, ProtocolVersion.PROTOCOLS_TO_13, SupportedVersionsExtension.chNetworkProducer, SupportedVersionsExtension.chOnLoadConsumer, null, null, null, SupportedVersionsExtension.chStringizer),
    SH_SUPPORTED_VERSIONS(43, "supported_versions", SSLHandshake.SERVER_HELLO, ProtocolVersion.PROTOCOLS_OF_13, SupportedVersionsExtension.shNetworkProducer, SupportedVersionsExtension.shOnLoadConsumer, null, null, null, SupportedVersionsExtension.shStringizer),
    HRR_SUPPORTED_VERSIONS(43, "supported_versions", SSLHandshake.HELLO_RETRY_REQUEST, ProtocolVersion.PROTOCOLS_OF_13, SupportedVersionsExtension.hrrNetworkProducer, SupportedVersionsExtension.hrrOnLoadConsumer, null, null, null, SupportedVersionsExtension.hrrStringizer),
    MH_SUPPORTED_VERSIONS(43, "supported_versions", SSLHandshake.MESSAGE_HASH, ProtocolVersion.PROTOCOLS_OF_13, SupportedVersionsExtension.hrrReproducer, null, null, null, null, SupportedVersionsExtension.hrrStringizer),
    CH_COOKIE(44, "cookie", SSLHandshake.CLIENT_HELLO, ProtocolVersion.PROTOCOLS_OF_13, CookieExtension.chNetworkProducer, CookieExtension.chOnLoadConsumer, null, CookieExtension.chOnTradeConsumer, null, CookieExtension.cookieStringizer),
    HRR_COOKIE(44, "cookie", SSLHandshake.HELLO_RETRY_REQUEST, ProtocolVersion.PROTOCOLS_OF_13, CookieExtension.hrrNetworkProducer, CookieExtension.hrrOnLoadConsumer, null, null, null, CookieExtension.cookieStringizer),
    MH_COOKIE(44, "cookie", SSLHandshake.MESSAGE_HASH, ProtocolVersion.PROTOCOLS_OF_13, CookieExtension.hrrNetworkReproducer, null, null, null, null, CookieExtension.cookieStringizer),
    PSK_KEY_EXCHANGE_MODES(45, "psk_key_exchange_modes", SSLHandshake.CLIENT_HELLO, ProtocolVersion.PROTOCOLS_OF_13, PskKeyExchangeModesExtension.chNetworkProducer, PskKeyExchangeModesExtension.chOnLoadConsumer, PskKeyExchangeModesExtension.chOnLoadAbsence, null, PskKeyExchangeModesExtension.chOnTradeAbsence, PskKeyExchangeModesExtension.pkemStringizer),
    CH_CERTIFICATE_AUTHORITIES(47, "certificate_authorities", SSLHandshake.CLIENT_HELLO, ProtocolVersion.PROTOCOLS_OF_13, CertificateAuthoritiesExtension.chNetworkProducer, CertificateAuthoritiesExtension.chOnLoadConsumer, null, null, null, CertificateAuthoritiesExtension.ssStringizer),
    CR_CERTIFICATE_AUTHORITIES(47, "certificate_authorities", SSLHandshake.CERTIFICATE_REQUEST, ProtocolVersion.PROTOCOLS_OF_13, CertificateAuthoritiesExtension.crNetworkProducer, CertificateAuthoritiesExtension.crOnLoadConsumer, null, null, null, CertificateAuthoritiesExtension.ssStringizer),
    OID_FILTERS(48, "oid_filters"),
    POST_HANDSHAKE_AUTH(48, "post_handshake_auth"),
    CH_SIGNATURE_ALGORITHMS_CERT(50, "signature_algorithms_cert", SSLHandshake.CLIENT_HELLO, ProtocolVersion.PROTOCOLS_12_13, CertSignAlgsExtension.chNetworkProducer, CertSignAlgsExtension.chOnLoadConsumer, null, CertSignAlgsExtension.chOnTradeConsumer, null, CertSignAlgsExtension.ssStringizer),
    CR_SIGNATURE_ALGORITHMS_CERT(50, "signature_algorithms_cert", SSLHandshake.CERTIFICATE_REQUEST, ProtocolVersion.PROTOCOLS_OF_13, CertSignAlgsExtension.crNetworkProducer, CertSignAlgsExtension.crOnLoadConsumer, null, CertSignAlgsExtension.crOnTradeConsumer, null, CertSignAlgsExtension.ssStringizer),
    CH_KEY_SHARE(51, "key_share", SSLHandshake.CLIENT_HELLO, ProtocolVersion.PROTOCOLS_OF_13, KeyShareExtension.chNetworkProducer, KeyShareExtension.chOnLoadConsumer, null, null, KeyShareExtension.chOnTradAbsence, KeyShareExtension.chStringizer),
    SH_KEY_SHARE(51, "key_share", SSLHandshake.SERVER_HELLO, ProtocolVersion.PROTOCOLS_OF_13, KeyShareExtension.shNetworkProducer, KeyShareExtension.shOnLoadConsumer, KeyShareExtension.shOnLoadAbsence, null, null, KeyShareExtension.shStringizer),
    HRR_KEY_SHARE(51, "key_share", SSLHandshake.HELLO_RETRY_REQUEST, ProtocolVersion.PROTOCOLS_OF_13, KeyShareExtension.hrrNetworkProducer, KeyShareExtension.hrrOnLoadConsumer, null, null, null, KeyShareExtension.hrrStringizer),
    MH_KEY_SHARE(51, "key_share", SSLHandshake.MESSAGE_HASH, ProtocolVersion.PROTOCOLS_OF_13, KeyShareExtension.hrrNetworkReproducer, null, null, null, null, KeyShareExtension.hrrStringizer),
    CH_RENEGOTIATION_INFO(65281, "renegotiation_info", SSLHandshake.CLIENT_HELLO, ProtocolVersion.PROTOCOLS_TO_12, RenegoInfoExtension.chNetworkProducer, RenegoInfoExtension.chOnLoadConsumer, RenegoInfoExtension.chOnLoadAbsence, null, null, RenegoInfoExtension.rniStringizer),
    SH_RENEGOTIATION_INFO(65281, "renegotiation_info", SSLHandshake.SERVER_HELLO, ProtocolVersion.PROTOCOLS_TO_12, RenegoInfoExtension.shNetworkProducer, RenegoInfoExtension.shOnLoadConsumer, RenegoInfoExtension.shOnLoadAbsence, null, null, RenegoInfoExtension.rniStringizer),
    CH_PRE_SHARED_KEY(41, "pre_shared_key", SSLHandshake.CLIENT_HELLO, ProtocolVersion.PROTOCOLS_OF_13, PreSharedKeyExtension.chNetworkProducer, PreSharedKeyExtension.chOnLoadConsumer, PreSharedKeyExtension.chOnLoadAbsence, PreSharedKeyExtension.chOnTradeConsumer, PreSharedKeyExtension.chOnTradAbsence, PreSharedKeyExtension.chStringizer),
    SH_PRE_SHARED_KEY(41, "pre_shared_key", SSLHandshake.SERVER_HELLO, ProtocolVersion.PROTOCOLS_OF_13, PreSharedKeyExtension.shNetworkProducer, PreSharedKeyExtension.shOnLoadConsumer, PreSharedKeyExtension.shOnLoadAbsence, null, null, PreSharedKeyExtension.shStringizer);

    final int id;
    final SSLHandshake handshakeType;
    final String name;
    final ProtocolVersion[] supportedProtocols;
    final HandshakeProducer networkProducer;
    final ExtensionConsumer onLoadConsumer;
    final HandshakeAbsence onLoadAbsence;
    final HandshakeConsumer onTradeConsumer;
    final HandshakeAbsence onTradeAbsence;
    final SSLStringizer stringizer;

    private SSLExtension(int id, String name) {
        this.id = id;
        this.handshakeType = SSLHandshake.NOT_APPLICABLE;
        this.name = name;
        this.supportedProtocols = new ProtocolVersion[0];
        this.networkProducer = null;
        this.onLoadConsumer = null;
        this.onLoadAbsence = null;
        this.onTradeConsumer = null;
        this.onTradeAbsence = null;
        this.stringizer = null;
    }

    private SSLExtension(int id, String name, SSLHandshake handshakeType, ProtocolVersion[] supportedProtocols, HandshakeProducer producer, ExtensionConsumer onLoadConsumer, HandshakeAbsence onLoadAbsence, HandshakeConsumer onTradeConsumer, HandshakeAbsence onTradeAbsence, SSLStringizer stringize) {
        this.id = id;
        this.handshakeType = handshakeType;
        this.name = name;
        this.supportedProtocols = supportedProtocols;
        this.networkProducer = producer;
        this.onLoadConsumer = onLoadConsumer;
        this.onLoadAbsence = onLoadAbsence;
        this.onTradeConsumer = onTradeConsumer;
        this.onTradeAbsence = onTradeAbsence;
        this.stringizer = stringize;
    }

    static SSLExtension valueOf(SSLHandshake handshakeType, int extensionType) {
        for (SSLExtension ext : SSLExtension.values()) {
            if (ext.id != extensionType || ext.handshakeType != handshakeType) continue;
            return ext;
        }
        return null;
    }

    static String nameOf(int extensionType) {
        for (SSLExtension ext : SSLExtension.values()) {
            if (ext.id != extensionType) continue;
            return ext.name;
        }
        return "unknown extension";
    }

    static boolean isConsumable(int extensionType) {
        for (SSLExtension ext : SSLExtension.values()) {
            if (ext.id != extensionType || ext.onLoadConsumer == null) continue;
            return true;
        }
        return false;
    }

    public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
        if (this.networkProducer != null) {
            return this.networkProducer.produce(context, message);
        }
        throw new UnsupportedOperationException("Not yet supported extension producing.");
    }

    public void consumeOnLoad(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
        if (this.onLoadConsumer == null) {
            throw new UnsupportedOperationException("Not yet supported extension loading.");
        }
        this.onLoadConsumer.consume(context, message, buffer);
    }

    public void consumeOnTrade(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
        if (this.onTradeConsumer == null) {
            throw new UnsupportedOperationException("Not yet supported extension processing.");
        }
        this.onTradeConsumer.consume(context, message);
    }

    void absentOnLoad(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
        if (this.onLoadAbsence == null) {
            throw new UnsupportedOperationException("Not yet supported extension absence processing.");
        }
        this.onLoadAbsence.absent(context, message);
    }

    void absentOnTrade(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
        if (this.onTradeAbsence == null) {
            throw new UnsupportedOperationException("Not yet supported extension absence processing.");
        }
        this.onTradeAbsence.absent(context, message);
    }

    public boolean isAvailable(ProtocolVersion protocolVersion) {
        for (ProtocolVersion supportedProtocol : this.supportedProtocols) {
            if (supportedProtocol != protocolVersion) continue;
            return true;
        }
        return false;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String toString(HandshakeContext handshakeContext, ByteBuffer byteBuffer) {
        String extData;
        MessageFormat messageFormat = new MessageFormat("\"{0} ({1})\": '{'\n{2}\n'}'", Locale.ENGLISH);
        if (this.stringizer == null) {
            HexDumpEncoder hexEncoder = new HexDumpEncoder();
            extData = hexEncoder.encode(byteBuffer.duplicate());
        } else {
            extData = this.stringizer.toString(handshakeContext, byteBuffer);
        }
        Object[] messageFields = new Object[]{this.name, this.id, Utilities.indent(extData)};
        return messageFormat.format(messageFields);
    }

    private static Collection<String> getDisabledExtensions(String propertyName) {
        String property = GetPropertyAction.privilegedGetProperty(propertyName);
        if (SSLLogger.isOn && SSLLogger.isOn("ssl,sslctx")) {
            SSLLogger.fine("System property " + propertyName + " is set to '" + property + "'", new Object[0]);
        }
        if (property != null && !property.isEmpty() && property.length() > 1 && property.charAt(0) == '\"' && property.charAt(property.length() - 1) == '\"') {
            property = property.substring(1, property.length() - 1);
        }
        if (property != null && !property.isEmpty()) {
            String[] extensionNames = property.split(",");
            ArrayList<String> extensions = new ArrayList<String>(extensionNames.length);
            for (String extension : extensionNames) {
                if ((extension = extension.trim()).isEmpty()) continue;
                extensions.add(extension);
            }
            return extensions;
        }
        return Collections.emptyList();
    }

    static interface ExtensionConsumer {
        public void consume(ConnectionContext var1, SSLHandshake.HandshakeMessage var2, ByteBuffer var3) throws IOException;
    }

    static final class ServerExtensions {
        static final Collection<SSLExtension> defaults;

        ServerExtensions() {
        }

        static {
            Collection<String> serverDisabledExtensions = SSLExtension.getDisabledExtensions("jdk.tls.server.disableExtensions");
            LinkedList<SSLExtension> extensions = new LinkedList<SSLExtension>();
            for (SSLExtension extension : SSLExtension.values()) {
                if (extension.handshakeType == SSLHandshake.NOT_APPLICABLE || serverDisabledExtensions.contains(extension.name)) continue;
                extensions.add(extension);
            }
            defaults = Collections.unmodifiableCollection(extensions);
        }
    }

    static final class ClientExtensions {
        static final Collection<SSLExtension> defaults;

        ClientExtensions() {
        }

        static {
            boolean enableExtension;
            boolean enableExtension2;
            Collection<String> clientDisabledExtensions = SSLExtension.getDisabledExtensions("jdk.tls.client.disableExtensions");
            LinkedList<SSLExtension> extensions = new LinkedList<SSLExtension>();
            for (SSLExtension extension : SSLExtension.values()) {
                if (extension.handshakeType == SSLHandshake.NOT_APPLICABLE || clientDisabledExtensions.contains(extension.name)) continue;
                extensions.add(extension);
            }
            if (extensions.contains(CH_SERVER_NAME) && !(enableExtension2 = Utilities.getBooleanProperty("jsse.enableSNIExtension", true))) {
                extensions.remove(CH_SERVER_NAME);
            }
            if (extensions.contains(CH_MAX_FRAGMENT_LENGTH)) {
                boolean enableExtension3;
                boolean bl = enableExtension3 = Utilities.getBooleanProperty("jsse.enableMFLNExtension", false) || Utilities.getBooleanProperty("jsse.enableMFLExtension", false);
                if (!enableExtension3) {
                    extensions.remove(CH_MAX_FRAGMENT_LENGTH);
                }
            }
            if (extensions.contains(CH_CERTIFICATE_AUTHORITIES) && !(enableExtension = Utilities.getBooleanProperty("jdk.tls.client.enableCAExtension", false))) {
                extensions.remove(CH_CERTIFICATE_AUTHORITIES);
            }
            defaults = Collections.unmodifiableCollection(extensions);
        }
    }

    static interface SSLExtensionSpec {
    }
}

