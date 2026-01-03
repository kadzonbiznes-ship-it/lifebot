/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLProtocolException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;
import sun.security.ssl.Alert;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.ClientAuthType;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ClientHello;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.HandshakeOutStream;
import sun.security.ssl.HandshakeProducer;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.Record;
import sun.security.ssl.SSLConfiguration;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLExtension;
import sun.security.ssl.SSLExtensions;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLPossession;
import sun.security.ssl.SSLTransport;
import sun.security.ssl.ServerHandshakeContext;
import sun.security.ssl.SignatureScheme;
import sun.security.ssl.StatusResponseManager;
import sun.security.ssl.Utilities;
import sun.security.ssl.X509Authentication;

final class CertificateMessage {
    static final SSLConsumer t12HandshakeConsumer = new T12CertificateConsumer();
    static final HandshakeProducer t12HandshakeProducer = new T12CertificateProducer();
    static final SSLConsumer t13HandshakeConsumer = new T13CertificateConsumer();
    static final HandshakeProducer t13HandshakeProducer = new T13CertificateProducer();

    CertificateMessage() {
    }

    private static Alert getCertificateAlert(ClientHandshakeContext chc, CertificateException cexc) {
        Alert alert = Alert.CERTIFICATE_UNKNOWN;
        Throwable baseCause = cexc.getCause();
        if (baseCause instanceof CertPathValidatorException) {
            CertPathValidatorException cpve = (CertPathValidatorException)baseCause;
            CertPathValidatorException.Reason reason = cpve.getReason();
            if (reason == CertPathValidatorException.BasicReason.REVOKED) {
                alert = chc.staplingActive ? Alert.BAD_CERT_STATUS_RESPONSE : Alert.CERTIFICATE_REVOKED;
            } else if (reason == CertPathValidatorException.BasicReason.UNDETERMINED_REVOCATION_STATUS) {
                alert = chc.staplingActive ? Alert.BAD_CERT_STATUS_RESPONSE : Alert.CERTIFICATE_UNKNOWN;
            } else if (reason == CertPathValidatorException.BasicReason.EXPIRED) {
                alert = Alert.CERTIFICATE_EXPIRED;
            } else if (reason == CertPathValidatorException.BasicReason.INVALID_SIGNATURE || reason == CertPathValidatorException.BasicReason.NOT_YET_VALID) {
                alert = Alert.BAD_CERTIFICATE;
            } else if (reason == CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED) {
                String exMsg;
                alert = Alert.UNSUPPORTED_CERTIFICATE;
                if (chc.negotiatedProtocol != null && chc.negotiatedProtocol.useTLS13PlusSpec() && ((exMsg = cexc.getMessage().toUpperCase()).contains("MD5WITH") || exMsg.contains("SHA1WITH"))) {
                    alert = Alert.BAD_CERTIFICATE;
                }
            }
        }
        return alert;
    }

    static final class T12CertificateConsumer
    implements SSLConsumer {
        private T12CertificateConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
            HandshakeContext hc = (HandshakeContext)context;
            hc.handshakeConsumers.remove(SSLHandshake.CERTIFICATE.id);
            T12CertificateMessage cm = new T12CertificateMessage(hc, message);
            if (hc.sslConfig.isClientMode) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Consuming server Certificate handshake message", cm);
                }
                this.onCertificate((ClientHandshakeContext)context, cm);
            } else {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Consuming client Certificate handshake message", cm);
                }
                this.onCertificate((ServerHandshakeContext)context, cm);
            }
        }

        private void onCertificate(ServerHandshakeContext shc, T12CertificateMessage certificateMessage) throws IOException {
            List<byte[]> encodedCerts = certificateMessage.encodedCertChain;
            if (encodedCerts == null || encodedCerts.isEmpty()) {
                shc.handshakeConsumers.remove(SSLHandshake.CERTIFICATE_VERIFY.id);
                if (shc.sslConfig.clientAuthType != ClientAuthType.CLIENT_AUTH_REQUESTED) {
                    throw shc.conContext.fatal(Alert.BAD_CERTIFICATE, "Empty client certificate chain");
                }
                return;
            }
            X509Certificate[] x509Certs = new X509Certificate[encodedCerts.size()];
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                int i = 0;
                for (byte[] encodedCert : encodedCerts) {
                    x509Certs[i++] = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(encodedCert));
                }
            }
            catch (CertificateException ce) {
                throw shc.conContext.fatal(Alert.BAD_CERTIFICATE, "Failed to parse client certificates", ce);
            }
            T12CertificateConsumer.checkClientCerts(shc, x509Certs);
            shc.handshakeCredentials.add(new X509Authentication.X509Credentials(x509Certs[0].getPublicKey(), x509Certs));
            shc.handshakeSession.setPeerCertificates(x509Certs);
        }

        private void onCertificate(ClientHandshakeContext chc, T12CertificateMessage certificateMessage) throws IOException {
            String identityAlg;
            List<byte[]> encodedCerts = certificateMessage.encodedCertChain;
            if (encodedCerts == null || encodedCerts.isEmpty()) {
                throw chc.conContext.fatal(Alert.BAD_CERTIFICATE, "Empty server certificate chain");
            }
            X509Certificate[] x509Certs = new X509Certificate[encodedCerts.size()];
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                int i = 0;
                for (byte[] encodedCert : encodedCerts) {
                    x509Certs[i++] = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(encodedCert));
                }
            }
            catch (CertificateException ce) {
                throw chc.conContext.fatal(Alert.BAD_CERTIFICATE, "Failed to parse server certificates", ce);
            }
            if (!(chc.reservedServerCerts == null || chc.handshakeSession.useExtendedMasterSecret || (identityAlg = chc.sslConfig.identificationProtocol) != null && !identityAlg.isEmpty() || T12CertificateConsumer.isIdentityEquivalent(x509Certs[0], chc.reservedServerCerts[0]))) {
                throw chc.conContext.fatal(Alert.BAD_CERTIFICATE, "server certificate change is restricted during renegotiation");
            }
            if (chc.staplingActive) {
                chc.deferredCerts = x509Certs;
            } else {
                T12CertificateConsumer.checkServerCerts(chc, x509Certs);
            }
            chc.handshakeCredentials.add(new X509Authentication.X509Credentials(x509Certs[0].getPublicKey(), x509Certs));
            chc.handshakeSession.setPeerCertificates(x509Certs);
        }

        private static boolean isIdentityEquivalent(X509Certificate thisCert, X509Certificate prevCert) {
            Collection<List<?>> prevSubjectAltNames;
            Collection<List<?>> thisSubjectAltNames;
            block9: {
                block8: {
                    if (thisCert.equals(prevCert)) {
                        return true;
                    }
                    thisSubjectAltNames = null;
                    try {
                        thisSubjectAltNames = thisCert.getSubjectAlternativeNames();
                    }
                    catch (CertificateParsingException cpe) {
                        if (!SSLLogger.isOn || !SSLLogger.isOn("handshake")) break block8;
                        SSLLogger.fine("Attempt to obtain subjectAltNames extension failed!", new Object[0]);
                    }
                }
                prevSubjectAltNames = null;
                try {
                    prevSubjectAltNames = prevCert.getSubjectAlternativeNames();
                }
                catch (CertificateParsingException cpe) {
                    if (!SSLLogger.isOn || !SSLLogger.isOn("handshake")) break block9;
                    SSLLogger.fine("Attempt to obtain subjectAltNames extension failed!", new Object[0]);
                }
            }
            if (thisSubjectAltNames != null && prevSubjectAltNames != null) {
                Collection<String> thisSubAltIPAddrs = T12CertificateConsumer.getSubjectAltNames(thisSubjectAltNames, 7);
                Collection<String> prevSubAltIPAddrs = T12CertificateConsumer.getSubjectAltNames(prevSubjectAltNames, 7);
                if (thisSubAltIPAddrs != null && prevSubAltIPAddrs != null && T12CertificateConsumer.isEquivalent(thisSubAltIPAddrs, prevSubAltIPAddrs)) {
                    return true;
                }
                Collection<String> thisSubAltDnsNames = T12CertificateConsumer.getSubjectAltNames(thisSubjectAltNames, 2);
                Collection<String> prevSubAltDnsNames = T12CertificateConsumer.getSubjectAltNames(prevSubjectAltNames, 2);
                if (thisSubAltDnsNames != null && prevSubAltDnsNames != null && T12CertificateConsumer.isEquivalent(thisSubAltDnsNames, prevSubAltDnsNames)) {
                    return true;
                }
            }
            X500Principal thisSubject = thisCert.getSubjectX500Principal();
            X500Principal prevSubject = prevCert.getSubjectX500Principal();
            X500Principal thisIssuer = thisCert.getIssuerX500Principal();
            X500Principal prevIssuer = prevCert.getIssuerX500Principal();
            return !thisSubject.getName().isEmpty() && !prevSubject.getName().isEmpty() && thisSubject.equals(prevSubject) && thisIssuer.equals(prevIssuer);
        }

        private static Collection<String> getSubjectAltNames(Collection<List<?>> subjectAltNames, int type) {
            HashSet subAltDnsNames = null;
            for (List<?> subjectAltName : subjectAltNames) {
                String subAltDnsName;
                int subjectAltNameType = (Integer)subjectAltName.get(0);
                if (subjectAltNameType != type || (subAltDnsName = (String)subjectAltName.get(1)) == null || subAltDnsName.isEmpty()) continue;
                if (subAltDnsNames == null) {
                    subAltDnsNames = HashSet.newHashSet(subjectAltNames.size());
                }
                subAltDnsNames.add(subAltDnsName);
            }
            return subAltDnsNames;
        }

        private static boolean isEquivalent(Collection<String> thisSubAltNames, Collection<String> prevSubAltNames) {
            for (String thisSubAltName : thisSubAltNames) {
                for (String prevSubAltName : prevSubAltNames) {
                    if (!thisSubAltName.equalsIgnoreCase(prevSubAltName)) continue;
                    return true;
                }
            }
            return false;
        }

        static void checkServerCerts(ClientHandshakeContext chc, X509Certificate[] certs) throws IOException {
            X509TrustManager tm = chc.sslContext.getX509TrustManager();
            String keyExchangeString = chc.negotiatedCipherSuite.keyExchange == CipherSuite.KeyExchange.K_RSA_EXPORT || chc.negotiatedCipherSuite.keyExchange == CipherSuite.KeyExchange.K_DHE_RSA_EXPORT ? CipherSuite.KeyExchange.K_RSA.name : chc.negotiatedCipherSuite.keyExchange.name;
            try {
                if (tm instanceof X509ExtendedTrustManager) {
                    SSLTransport sSLTransport = chc.conContext.transport;
                    if (sSLTransport instanceof SSLEngine) {
                        SSLEngine engine = (SSLEngine)((Object)sSLTransport);
                        ((X509ExtendedTrustManager)tm).checkServerTrusted((X509Certificate[])certs.clone(), keyExchangeString, engine);
                    } else {
                        SSLSocket socket = (SSLSocket)((Object)chc.conContext.transport);
                        ((X509ExtendedTrustManager)tm).checkServerTrusted((X509Certificate[])certs.clone(), keyExchangeString, socket);
                    }
                } else {
                    throw new CertificateException("Improper X509TrustManager implementation");
                }
                chc.handshakeSession.setPeerCertificates(certs);
            }
            catch (CertificateException ce) {
                throw chc.conContext.fatal(CertificateMessage.getCertificateAlert(chc, ce), ce);
            }
        }

        private static void checkClientCerts(ServerHandshakeContext shc, X509Certificate[] certs) throws IOException {
            block14: {
                String keyAlgorithm;
                X509TrustManager tm = shc.sslContext.getX509TrustManager();
                PublicKey key = certs[0].getPublicKey();
                String authType = switch (keyAlgorithm = key.getAlgorithm()) {
                    case "RSA", "DSA", "EC", "RSASSA-PSS" -> keyAlgorithm;
                    default -> "UNKNOWN";
                };
                try {
                    if (tm instanceof X509ExtendedTrustManager) {
                        SSLTransport sSLTransport = shc.conContext.transport;
                        if (sSLTransport instanceof SSLEngine) {
                            SSLEngine engine = (SSLEngine)((Object)sSLTransport);
                            ((X509ExtendedTrustManager)tm).checkClientTrusted((X509Certificate[])certs.clone(), authType, engine);
                        } else {
                            SSLSocket socket = (SSLSocket)((Object)shc.conContext.transport);
                            ((X509ExtendedTrustManager)tm).checkClientTrusted((X509Certificate[])certs.clone(), authType, socket);
                        }
                        break block14;
                    }
                    throw new CertificateException("Improper X509TrustManager implementation");
                }
                catch (CertificateException ce) {
                    throw shc.conContext.fatal(Alert.CERTIFICATE_UNKNOWN, ce);
                }
            }
        }
    }

    private static final class T12CertificateProducer
    implements HandshakeProducer {
        private T12CertificateProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            HandshakeContext hc = (HandshakeContext)context;
            if (hc.sslConfig.isClientMode) {
                return this.onProduceCertificate((ClientHandshakeContext)context, message);
            }
            return this.onProduceCertificate((ServerHandshakeContext)context, message);
        }

        private byte[] onProduceCertificate(ServerHandshakeContext shc, SSLHandshake.HandshakeMessage message) throws IOException {
            X509Authentication.X509Possession x509Possession = null;
            for (SSLPossession possession : shc.handshakePossessions) {
                if (!(possession instanceof X509Authentication.X509Possession)) continue;
                x509Possession = (X509Authentication.X509Possession)possession;
                break;
            }
            if (x509Possession == null) {
                throw shc.conContext.fatal(Alert.INTERNAL_ERROR, "No expected X.509 certificate for server authentication");
            }
            shc.handshakeSession.setLocalPrivateKey(x509Possession.popPrivateKey);
            shc.handshakeSession.setLocalCertificates(x509Possession.popCerts);
            T12CertificateMessage cm = new T12CertificateMessage((HandshakeContext)shc, x509Possession.popCerts);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced server Certificate handshake message", cm);
            }
            cm.write(shc.handshakeOutput);
            shc.handshakeOutput.flush();
            return null;
        }

        private byte[] onProduceCertificate(ClientHandshakeContext chc, SSLHandshake.HandshakeMessage message) throws IOException {
            X509Authentication.X509Possession x509Possession = null;
            for (SSLPossession possession : chc.handshakePossessions) {
                if (!(possession instanceof X509Authentication.X509Possession)) continue;
                x509Possession = (X509Authentication.X509Possession)possession;
                break;
            }
            if (x509Possession == null) {
                if (chc.negotiatedProtocol.useTLS10PlusSpec()) {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                        SSLLogger.fine("No X.509 certificate for client authentication, use empty Certificate message instead", new Object[0]);
                    }
                    x509Possession = new X509Authentication.X509Possession(null, new X509Certificate[0]);
                } else {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                        SSLLogger.fine("No X.509 certificate for client authentication, send a no_certificate alert", new Object[0]);
                    }
                    chc.conContext.warning(Alert.NO_CERTIFICATE);
                    return null;
                }
            }
            chc.handshakeSession.setLocalPrivateKey(x509Possession.popPrivateKey);
            if (x509Possession.popCerts != null && x509Possession.popCerts.length != 0) {
                chc.handshakeSession.setLocalCertificates(x509Possession.popCerts);
            } else {
                chc.handshakeSession.setLocalCertificates(null);
            }
            T12CertificateMessage cm = new T12CertificateMessage((HandshakeContext)chc, x509Possession.popCerts);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced client Certificate handshake message", cm);
            }
            cm.write(chc.handshakeOutput);
            chc.handshakeOutput.flush();
            return null;
        }
    }

    private static final class T13CertificateConsumer
    implements SSLConsumer {
        private T13CertificateConsumer() {
        }

        @Override
        public void consume(ConnectionContext context, ByteBuffer message) throws IOException {
            HandshakeContext hc = (HandshakeContext)context;
            hc.handshakeConsumers.remove(SSLHandshake.CERTIFICATE.id);
            if (hc.handshakeConsumers.containsKey(SSLHandshake.ENCRYPTED_EXTENSIONS.id)) {
                throw hc.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected Certificate handshake message");
            }
            T13CertificateMessage cm = new T13CertificateMessage(hc, message);
            if (hc.sslConfig.isClientMode) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Consuming server Certificate handshake message", cm);
                }
                this.onConsumeCertificate((ClientHandshakeContext)context, cm);
            } else {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("Consuming client Certificate handshake message", cm);
                }
                this.onConsumeCertificate((ServerHandshakeContext)context, cm);
            }
        }

        private void onConsumeCertificate(ServerHandshakeContext shc, T13CertificateMessage certificateMessage) throws IOException {
            if (certificateMessage.certEntries == null || certificateMessage.certEntries.isEmpty()) {
                shc.handshakeConsumers.remove(SSLHandshake.CERTIFICATE_VERIFY.id);
                if (shc.sslConfig.clientAuthType == ClientAuthType.CLIENT_AUTH_REQUIRED) {
                    throw shc.conContext.fatal(Alert.BAD_CERTIFICATE, "Empty client certificate chain");
                }
                return;
            }
            X509Certificate[] cliCerts = T13CertificateConsumer.checkClientCerts(shc, certificateMessage.certEntries);
            shc.handshakeCredentials.add(new X509Authentication.X509Credentials(cliCerts[0].getPublicKey(), cliCerts));
            shc.handshakeSession.setPeerCertificates(cliCerts);
        }

        private void onConsumeCertificate(ClientHandshakeContext chc, T13CertificateMessage certificateMessage) throws IOException {
            if (certificateMessage.certEntries == null || certificateMessage.certEntries.isEmpty()) {
                throw chc.conContext.fatal(Alert.BAD_CERTIFICATE, "Empty server certificate chain");
            }
            SSLExtension[] enabledExtensions = chc.sslConfig.getEnabledExtensions(SSLHandshake.CERTIFICATE);
            for (CertificateEntry certEnt : certificateMessage.certEntries) {
                certEnt.extensions.consumeOnLoad(chc, enabledExtensions);
            }
            X509Certificate[] srvCerts = T13CertificateConsumer.checkServerCerts(chc, certificateMessage.certEntries);
            chc.handshakeCredentials.add(new X509Authentication.X509Credentials(srvCerts[0].getPublicKey(), srvCerts));
            chc.handshakeSession.setPeerCertificates(srvCerts);
        }

        private static X509Certificate[] checkClientCerts(ServerHandshakeContext shc, List<CertificateEntry> certEntries) throws IOException {
            X509Certificate[] certs = new X509Certificate[certEntries.size()];
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                int i = 0;
                for (CertificateEntry entry : certEntries) {
                    certs[i++] = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(entry.encoded));
                }
            }
            catch (CertificateException ce) {
                throw shc.conContext.fatal(Alert.BAD_CERTIFICATE, "Failed to parse client certificates", ce);
            }
            String keyAlgorithm = certs[0].getPublicKey().getAlgorithm();
            String authType = switch (keyAlgorithm) {
                case "RSA", "DSA", "EC", "RSASSA-PSS" -> keyAlgorithm;
                default -> "UNKNOWN";
            };
            try {
                X509TrustManager tm = shc.sslContext.getX509TrustManager();
                if (tm instanceof X509ExtendedTrustManager) {
                    SSLTransport sSLTransport = shc.conContext.transport;
                    if (sSLTransport instanceof SSLEngine) {
                        SSLEngine engine = (SSLEngine)((Object)sSLTransport);
                        ((X509ExtendedTrustManager)tm).checkClientTrusted((X509Certificate[])certs.clone(), authType, engine);
                    } else {
                        SSLSocket socket = (SSLSocket)((Object)shc.conContext.transport);
                        ((X509ExtendedTrustManager)tm).checkClientTrusted((X509Certificate[])certs.clone(), authType, socket);
                    }
                } else {
                    throw new CertificateException("Improper X509TrustManager implementation");
                }
                shc.handshakeSession.setPeerCertificates(certs);
            }
            catch (CertificateException ce) {
                throw shc.conContext.fatal(Alert.CERTIFICATE_UNKNOWN, ce);
            }
            return certs;
        }

        private static X509Certificate[] checkServerCerts(ClientHandshakeContext chc, List<CertificateEntry> certEntries) throws IOException {
            X509Certificate[] certs = new X509Certificate[certEntries.size()];
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                int i = 0;
                for (CertificateEntry certificateEntry : certEntries) {
                    certs[i++] = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(certificateEntry.encoded));
                }
            }
            catch (CertificateException ce) {
                throw chc.conContext.fatal(Alert.BAD_CERTIFICATE, "Failed to parse server certificates", ce);
            }
            String authType = "UNKNOWN";
            try {
                X509TrustManager tm = chc.sslContext.getX509TrustManager();
                if (tm instanceof X509ExtendedTrustManager) {
                    SSLTransport sSLTransport = chc.conContext.transport;
                    if (sSLTransport instanceof SSLEngine) {
                        SSLEngine engine = (SSLEngine)((Object)sSLTransport);
                        ((X509ExtendedTrustManager)tm).checkServerTrusted((X509Certificate[])certs.clone(), authType, engine);
                    } else {
                        SSLSocket sSLSocket = (SSLSocket)((Object)chc.conContext.transport);
                        ((X509ExtendedTrustManager)tm).checkServerTrusted((X509Certificate[])certs.clone(), authType, sSLSocket);
                    }
                } else {
                    throw new CertificateException("Improper X509TrustManager implementation");
                }
                chc.handshakeSession.setPeerCertificates(certs);
            }
            catch (CertificateException ce) {
                throw chc.conContext.fatal(CertificateMessage.getCertificateAlert(chc, ce), ce);
            }
            return certs;
        }
    }

    private static final class T13CertificateProducer
    implements HandshakeProducer {
        private T13CertificateProducer() {
        }

        @Override
        public byte[] produce(ConnectionContext context, SSLHandshake.HandshakeMessage message) throws IOException {
            HandshakeContext hc = (HandshakeContext)context;
            if (hc.sslConfig.isClientMode) {
                return this.onProduceCertificate((ClientHandshakeContext)context, message);
            }
            return this.onProduceCertificate((ServerHandshakeContext)context, message);
        }

        private byte[] onProduceCertificate(ServerHandshakeContext shc, SSLHandshake.HandshakeMessage message) throws IOException {
            T13CertificateMessage cm;
            ClientHello.ClientHelloMessage clientHello = (ClientHello.ClientHelloMessage)message;
            SSLPossession pos = T13CertificateProducer.choosePossession(shc, clientHello);
            if (pos == null) {
                throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "No available authentication scheme");
            }
            if (!(pos instanceof X509Authentication.X509Possession)) {
                throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "No X.509 certificate for server authentication");
            }
            X509Authentication.X509Possession x509Possession = (X509Authentication.X509Possession)pos;
            X509Certificate[] localCerts = x509Possession.popCerts;
            if (localCerts == null || localCerts.length == 0) {
                throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "No X.509 certificate for server authentication");
            }
            shc.handshakePossessions.add(x509Possession);
            shc.handshakeSession.setLocalPrivateKey(x509Possession.popPrivateKey);
            shc.handshakeSession.setLocalCertificates(localCerts);
            try {
                cm = new T13CertificateMessage((HandshakeContext)shc, new byte[0], localCerts);
            }
            catch (CertificateException | SSLException ce) {
                throw shc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Failed to produce server Certificate message", ce);
            }
            shc.stapleParams = StatusResponseManager.processStapling(shc);
            shc.staplingActive = shc.stapleParams != null;
            SSLExtension[] enabledCTExts = shc.sslConfig.getEnabledExtensions(SSLHandshake.CERTIFICATE, Arrays.asList(ProtocolVersion.PROTOCOLS_OF_13));
            Iterator<CertificateEntry> iterator = cm.certEntries.iterator();
            while (iterator.hasNext()) {
                CertificateEntry certEnt;
                shc.currentCertEntry = certEnt = iterator.next();
                certEnt.extensions.produce(shc, enabledCTExts);
            }
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced server Certificate message", cm);
            }
            cm.write(shc.handshakeOutput);
            shc.handshakeOutput.flush();
            return null;
        }

        private static SSLPossession choosePossession(HandshakeContext hc, ClientHello.ClientHelloMessage clientHello) {
            if (hc.peerRequestedCertSignSchemes == null || hc.peerRequestedCertSignSchemes.isEmpty()) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.warning("No signature_algorithms(_cert) in ClientHello", new Object[0]);
                }
                return null;
            }
            String[] supportedKeyTypes = (String[])hc.peerRequestedCertSignSchemes.stream().map(ss -> ss.keyAlgorithm).distinct().filter(ka -> SignatureScheme.getPreferableAlgorithm(hc.algorithmConstraints, hc.peerRequestedSignatureSchemes, ka, hc.negotiatedProtocol) != null || SSLLogger.logWarning("ssl,handshake", "Unable to produce CertificateVerify for key algorithm: " + ka)).filter(ka -> X509Authentication.valueOfKeyAlgorithm(ka) != null || SSLLogger.logWarning("ssl,handshake", "Unsupported key algorithm: " + ka)).toArray(String[]::new);
            SSLPossession pos = X509Authentication.createPossession(hc, supportedKeyTypes);
            if (pos == null && SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.warning("No available authentication scheme", new Object[0]);
            }
            return pos;
        }

        private byte[] onProduceCertificate(ClientHandshakeContext chc, SSLHandshake.HandshakeMessage message) throws IOException {
            T13CertificateMessage cm;
            X509Certificate[] localCerts;
            ClientHello.ClientHelloMessage clientHello = (ClientHello.ClientHelloMessage)message;
            SSLPossession pos = T13CertificateProducer.choosePossession(chc, clientHello);
            if (pos == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                    SSLLogger.fine("No available client authentication scheme", new Object[0]);
                }
                localCerts = new X509Certificate[]{};
            } else {
                chc.handshakePossessions.add(pos);
                if (!(pos instanceof X509Authentication.X509Possession)) {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                        SSLLogger.fine("No X.509 certificate for client authentication", new Object[0]);
                    }
                    localCerts = new X509Certificate[]{};
                } else {
                    X509Authentication.X509Possession x509Possession = (X509Authentication.X509Possession)pos;
                    localCerts = x509Possession.popCerts;
                    chc.handshakeSession.setLocalPrivateKey(x509Possession.popPrivateKey);
                }
            }
            if (localCerts != null && localCerts.length != 0) {
                chc.handshakeSession.setLocalCertificates(localCerts);
            } else {
                chc.handshakeSession.setLocalCertificates(null);
            }
            try {
                cm = new T13CertificateMessage((HandshakeContext)chc, chc.certRequestContext, localCerts);
            }
            catch (CertificateException | SSLException ce) {
                throw chc.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Failed to produce client Certificate message", ce);
            }
            if (SSLLogger.isOn && SSLLogger.isOn("ssl,handshake")) {
                SSLLogger.fine("Produced client Certificate message", cm);
            }
            cm.write(chc.handshakeOutput);
            chc.handshakeOutput.flush();
            return null;
        }
    }

    static final class T13CertificateMessage
    extends SSLHandshake.HandshakeMessage {
        private final byte[] requestContext;
        private final List<CertificateEntry> certEntries;

        T13CertificateMessage(HandshakeContext context, byte[] requestContext, X509Certificate[] certificates) throws SSLException, CertificateException {
            super(context);
            this.requestContext = (byte[])requestContext.clone();
            this.certEntries = new LinkedList<CertificateEntry>();
            for (X509Certificate cert : certificates) {
                byte[] encoded = cert.getEncoded();
                SSLExtensions extensions = new SSLExtensions(this);
                this.certEntries.add(new CertificateEntry(encoded, extensions));
            }
        }

        T13CertificateMessage(HandshakeContext handshakeContext, byte[] requestContext, List<CertificateEntry> certificates) {
            super(handshakeContext);
            this.requestContext = (byte[])requestContext.clone();
            this.certEntries = certificates;
        }

        T13CertificateMessage(HandshakeContext handshakeContext, ByteBuffer m) throws IOException {
            super(handshakeContext);
            if (m.remaining() < 4) {
                throw new SSLProtocolException("Invalid Certificate message: insufficient data (length=" + m.remaining() + ")");
            }
            this.requestContext = Record.getBytes8(m);
            if (m.remaining() < 3) {
                throw new SSLProtocolException("Invalid Certificate message: insufficient certificate entries data (length=" + m.remaining() + ")");
            }
            int listLen = Record.getInt24(m);
            if (listLen != m.remaining()) {
                throw new SSLProtocolException("Invalid Certificate message: incorrect list length (length=" + listLen + ")");
            }
            SSLExtension[] enabledExtensions = handshakeContext.sslConfig.getEnabledExtensions(SSLHandshake.CERTIFICATE);
            LinkedList<CertificateEntry> certList = new LinkedList<CertificateEntry>();
            while (m.hasRemaining()) {
                byte[] encodedCert = Record.getBytes24(m);
                if (encodedCert.length == 0) {
                    throw new SSLProtocolException("Invalid Certificate message: empty cert_data");
                }
                SSLExtensions extensions = new SSLExtensions(this, m, enabledExtensions);
                certList.add(new CertificateEntry(encodedCert, extensions));
                if (certList.size() <= SSLConfiguration.maxCertificateChainLength) continue;
                throw new SSLProtocolException("The certificate chain length (" + certList.size() + ") exceeds the maximum allowed length (" + SSLConfiguration.maxCertificateChainLength + ")");
            }
            this.certEntries = Collections.unmodifiableList(certList);
        }

        @Override
        public SSLHandshake handshakeType() {
            return SSLHandshake.CERTIFICATE;
        }

        @Override
        public int messageLength() {
            int msgLen = 4 + this.requestContext.length;
            for (CertificateEntry entry : this.certEntries) {
                msgLen += entry.getEncodedSize();
            }
            return msgLen;
        }

        @Override
        public void send(HandshakeOutStream hos) throws IOException {
            int entryListLen = 0;
            for (CertificateEntry entry : this.certEntries) {
                entryListLen += entry.getEncodedSize();
            }
            hos.putBytes8(this.requestContext);
            hos.putInt24(entryListLen);
            for (CertificateEntry entry : this.certEntries) {
                hos.putBytes24(entry.encoded);
                if (entry.extensions.length() == 0) {
                    hos.putInt16(0);
                    continue;
                }
                entry.extensions.send(hos);
            }
        }

        public String toString() {
            MessageFormat messageFormat = new MessageFormat("\"Certificate\": '{'\n  \"certificate_request_context\": \"{0}\",\n  \"certificate_list\": [{1}\n]\n'}'", Locale.ENGLISH);
            StringBuilder builder = new StringBuilder(512);
            for (CertificateEntry entry : this.certEntries) {
                builder.append(entry.toString());
            }
            Object[] messageFields = new Object[]{Utilities.toHexString(this.requestContext), Utilities.indent(builder.toString())};
            return messageFormat.format(messageFields);
        }
    }

    static final class CertificateEntry {
        final byte[] encoded;
        private final SSLExtensions extensions;

        CertificateEntry(byte[] encoded, SSLExtensions extensions) {
            this.encoded = encoded;
            this.extensions = extensions;
        }

        private int getEncodedSize() {
            int extLen = this.extensions.length();
            if (extLen == 0) {
                extLen = 2;
            }
            return 3 + this.encoded.length + extLen;
        }

        public String toString() {
            Object x509Certs;
            MessageFormat messageFormat = new MessageFormat("\n'{'\n{0}\n  \"extensions\": '{'\n{1}\n  '}'\n'}',", Locale.ENGLISH);
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                x509Certs = cf.generateCertificate(new ByteArrayInputStream(this.encoded));
            }
            catch (CertificateException ce) {
                x509Certs = this.encoded;
            }
            Object[] messageFields = new Object[]{SSLLogger.toString(x509Certs), Utilities.indent(this.extensions.toString(), "    ")};
            return messageFormat.format(messageFields);
        }
    }

    static final class T12CertificateMessage
    extends SSLHandshake.HandshakeMessage {
        final List<byte[]> encodedCertChain;

        T12CertificateMessage(HandshakeContext handshakeContext, X509Certificate[] certChain) throws SSLException {
            super(handshakeContext);
            ArrayList<byte[]> encodedCerts = new ArrayList<byte[]>(certChain.length);
            for (X509Certificate cert : certChain) {
                try {
                    encodedCerts.add(cert.getEncoded());
                }
                catch (CertificateEncodingException cee) {
                    throw handshakeContext.conContext.fatal(Alert.INTERNAL_ERROR, "Could not encode certificate (" + cert.getSubjectX500Principal() + ")", cee);
                }
            }
            this.encodedCertChain = encodedCerts;
        }

        T12CertificateMessage(HandshakeContext handshakeContext, ByteBuffer m) throws IOException {
            super(handshakeContext);
            int listLen;
            if (listLen > m.remaining()) {
                throw handshakeContext.conContext.fatal(Alert.ILLEGAL_PARAMETER, "Error parsing certificate message:no sufficient data");
            }
            if (listLen > 0) {
                byte[] encodedCert;
                LinkedList<byte[]> encodedCerts = new LinkedList<byte[]>();
                for (listLen = Record.getInt24(m); listLen > 0; listLen -= 3 + encodedCert.length) {
                    int maxAllowedChainLength;
                    encodedCert = Record.getBytes24(m);
                    encodedCerts.add(encodedCert);
                    int n = maxAllowedChainLength = handshakeContext.sslConfig.isClientMode ? SSLConfiguration.maxInboundServerCertChainLen : SSLConfiguration.maxInboundClientCertChainLen;
                    if (encodedCerts.size() <= maxAllowedChainLength) continue;
                    throw new SSLProtocolException("The certificate chain length (" + encodedCerts.size() + ") exceeds the maximum allowed length (" + maxAllowedChainLength + ")");
                }
                this.encodedCertChain = encodedCerts;
            } else {
                this.encodedCertChain = Collections.emptyList();
            }
        }

        @Override
        public SSLHandshake handshakeType() {
            return SSLHandshake.CERTIFICATE;
        }

        @Override
        public int messageLength() {
            int msgLen = 3;
            for (byte[] encodedCert : this.encodedCertChain) {
                msgLen += encodedCert.length + 3;
            }
            return msgLen;
        }

        @Override
        public void send(HandshakeOutStream hos) throws IOException {
            int listLen = 0;
            for (byte[] encodedCert : this.encodedCertChain) {
                listLen += encodedCert.length + 3;
            }
            hos.putInt24(listLen);
            for (byte[] encodedCert : this.encodedCertChain) {
                hos.putBytes24(encodedCert);
            }
        }

        public String toString() {
            int i;
            if (this.encodedCertChain.isEmpty()) {
                return "\"Certificates\": <empty list>";
            }
            Object[] x509Certs = new Object[this.encodedCertChain.size()];
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                i = 0;
                for (byte[] encodedCert : this.encodedCertChain) {
                    Object obj;
                    try {
                        obj = cf.generateCertificate(new ByteArrayInputStream(encodedCert));
                    }
                    catch (CertificateException ce) {
                        obj = encodedCert;
                    }
                    x509Certs[i++] = obj;
                }
            }
            catch (CertificateException ce) {
                i = 0;
                for (byte[] encodedCert : this.encodedCertChain) {
                    x509Certs[i++] = encodedCert;
                }
            }
            MessageFormat messageFormat = new MessageFormat("\"Certificates\": [\n{0}\n]", Locale.ENGLISH);
            Object[] messageFields = new Object[]{SSLLogger.toString(x509Certs)};
            return messageFormat.format(messageFields);
        }
    }
}

