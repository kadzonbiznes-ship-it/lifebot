/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SSLProtocolException;
import javax.security.auth.x500.X500Principal;
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

final class CertificateAuthoritiesExtension {
    static final HandshakeProducer chNetworkProducer = new CHCertificateAuthoritiesProducer();
    static final SSLExtension.ExtensionConsumer chOnLoadConsumer = new CHCertificateAuthoritiesConsumer();
    static final HandshakeProducer crNetworkProducer = new CRCertificateAuthoritiesProducer();
    static final SSLExtension.ExtensionConsumer crOnLoadConsumer = new CRCertificateAuthoritiesConsumer();
    static final SSLStringizer ssStringizer = new CertificateAuthoritiesStringizer();

    CertificateAuthoritiesExtension() {
    }

    private static final class CHCertificateAuthoritiesProducer
    implements HandshakeProducer {
        private CHCertificateAuthoritiesProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.CH_CERTIFICATE_AUTHORITIES)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable certificate_authorities extension", new Object[0]);
                }
                return null;
            }
            X509Certificate[] caCerts = chc.sslContext.getX509TrustManager().getAcceptedIssuers();
            if (caCerts.length == 0) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("No available certificate authorities", new Object[0]);
                }
                return null;
            }
            List<byte[]> encodedCAs = CertificateAuthoritiesSpec.getEncodedAuthorities(caCerts);
            if (encodedCAs.isEmpty()) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("The number of CAs exceeds the maximum size of the certificate_authorities extension", new Object[0]);
                }
                return null;
            }
            CertificateAuthoritiesSpec spec = new CertificateAuthoritiesSpec(encodedCAs);
            int vectorLen = 0;
            for (byte[] encoded : spec.authorities) {
                vectorLen += encoded.length + 2;
            }
            byte[] extData = new byte[vectorLen + 2];
            ByteBuffer m = ByteBuffer.wrap(extData);
            Record.putInt16(m, vectorLen);
            for (byte[] encoded : spec.authorities) {
                Record.putBytes16(m, encoded);
            }
            chc.handshakeExtensions.put(SSLExtension.CH_CERTIFICATE_AUTHORITIES, spec);
            return extData;
        }
    }

    private static final class CHCertificateAuthoritiesConsumer
    implements SSLExtension.ExtensionConsumer {
        private CHCertificateAuthoritiesConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.CH_CERTIFICATE_AUTHORITIES)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable certificate_authorities extension", new Object[0]);
                }
                return;
            }
            CertificateAuthoritiesSpec spec = new CertificateAuthoritiesSpec(shc, buffer);
            shc.peerSupportedAuthorities = spec.getAuthorities();
            shc.handshakeExtensions.put(SSLExtension.CH_CERTIFICATE_AUTHORITIES, spec);
        }
    }

    private static final class CRCertificateAuthoritiesProducer
    implements HandshakeProducer {
        private CRCertificateAuthoritiesProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            ServerHandshakeContext shc = (ServerHandshakeContext)context;
            if (!shc.sslConfig.isAvailable(SSLExtension.CR_CERTIFICATE_AUTHORITIES)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable certificate_authorities extension", new Object[0]);
                }
                return null;
            }
            X509Certificate[] caCerts = shc.sslContext.getX509TrustManager().getAcceptedIssuers();
            if (caCerts.length == 0) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("No available certificate authorities", new Object[0]);
                }
                return null;
            }
            List<byte[]> encodedCAs = CertificateAuthoritiesSpec.getEncodedAuthorities(caCerts);
            if (encodedCAs.isEmpty()) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("Too many certificate authorities to use the certificate_authorities extension", new Object[0]);
                }
                return null;
            }
            CertificateAuthoritiesSpec spec = new CertificateAuthoritiesSpec(encodedCAs);
            int vectorLen = 0;
            for (byte[] encoded : spec.authorities) {
                vectorLen += encoded.length + 2;
            }
            byte[] extData = new byte[vectorLen + 2];
            ByteBuffer m = ByteBuffer.wrap(extData);
            Record.putInt16(m, vectorLen);
            for (byte[] encoded : spec.authorities) {
                Record.putBytes16(m, encoded);
            }
            shc.handshakeExtensions.put(SSLExtension.CR_CERTIFICATE_AUTHORITIES, spec);
            return extData;
        }
    }

    private static final class CRCertificateAuthoritiesConsumer
    implements SSLExtension.ExtensionConsumer {
        private CRCertificateAuthoritiesConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, SSLHandshake.HandshakeMessage message, ByteBuffer buffer) throws IOException {
            ClientHandshakeContext chc = (ClientHandshakeContext)context;
            if (!chc.sslConfig.isAvailable(SSLExtension.CR_CERTIFICATE_AUTHORITIES)) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Ignore unavailable certificate_authorities extension", new Object[0]);
                }
                return;
            }
            CertificateAuthoritiesSpec spec = new CertificateAuthoritiesSpec(chc, buffer);
            chc.peerSupportedAuthorities = spec.getAuthorities();
            chc.handshakeExtensions.put(SSLExtension.CR_CERTIFICATE_AUTHORITIES, spec);
        }
    }

    private static final class CertificateAuthoritiesStringizer
    implements SSLStringizer {
        private CertificateAuthoritiesStringizer() {
        }

        @Override
        public String toString(HandshakeContext hc, ByteBuffer buffer) {
            try {
                return new CertificateAuthoritiesSpec(hc, buffer).toString();
            }
            catch (IOException ioe) {
                return ioe.getMessage();
            }
        }
    }

    static final class CertificateAuthoritiesSpec
    implements SSLExtension.SSLExtensionSpec {
        final List<byte[]> authorities;

        private CertificateAuthoritiesSpec(List<byte[]> authorities) {
            this.authorities = authorities;
        }

        private CertificateAuthoritiesSpec(HandshakeContext hc, ByteBuffer m) throws IOException {
            if (m.remaining() < 3) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, new SSLProtocolException("Invalid certificate_authorities extension: insufficient data"));
            }
            int listLen = Record.getInt16(m);
            if (listLen == 0) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, "Invalid certificate_authorities extension: no certificate authorities");
            }
            if (listLen > m.remaining()) {
                throw hc.conContext.fatal(Alert.DECODE_ERROR, "Invalid certificate_authorities extension: insufficient data");
            }
            this.authorities = new LinkedList<byte[]>();
            while (listLen > 0) {
                byte[] encoded = Record.getBytes16(m);
                listLen -= 2 + encoded.length;
                this.authorities.add(encoded);
            }
        }

        private static List<byte[]> getEncodedAuthorities(X509Certificate[] trustedCerts) {
            ArrayList<byte[]> authorities = new ArrayList<byte[]>(trustedCerts.length);
            int sizeAccount = 0;
            for (X509Certificate cert : trustedCerts) {
                X500Principal x500Principal = cert.getSubjectX500Principal();
                byte[] encodedPrincipal = x500Principal.getEncoded();
                if ((sizeAccount += encodedPrincipal.length) > 65535) {
                    return Collections.emptyList();
                }
                if (encodedPrincipal.length == 0) continue;
                authorities.add(encodedPrincipal);
            }
            return authorities;
        }

        X500Principal[] getAuthorities() {
            X500Principal[] principals = new X500Principal[this.authorities.size()];
            int i = 0;
            for (byte[] encoded : this.authorities) {
                principals[i++] = new X500Principal(encoded);
            }
            return principals;
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"certificate authorities\": '['\n{0}']'", Locale.ENGLISH);
            StringBuilder builder = new StringBuilder(512);
            for (byte[] encoded : this.authorities) {
                try {
                    X500Principal principal = new X500Principal(encoded);
                    builder.append(principal.toString());
                }
                catch (IllegalArgumentException iae) {
                    builder.append("unparseable distinguished name: " + iae);
                }
                builder.append("\n");
            }
            Object[] messageFields = new Object[]{Utilities.indent(builder.toString())};
            return messageFormat.format(messageFields);
        }
    }
}

