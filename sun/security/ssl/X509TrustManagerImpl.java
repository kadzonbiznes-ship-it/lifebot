/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.net.Socket;
import java.security.AlgorithmConstraints;
import java.security.cert.CertificateException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.SSLAlgorithmConstraints;
import sun.security.ssl.SSLLogger;
import sun.security.util.AnchorCertificates;
import sun.security.util.HostnameChecker;
import sun.security.validator.Validator;

final class X509TrustManagerImpl
extends X509ExtendedTrustManager
implements X509TrustManager {
    private final String validatorType;
    private final Collection<X509Certificate> trustedCerts;
    private final PKIXBuilderParameters pkixParams;
    private volatile Validator clientValidator;
    private volatile Validator serverValidator;
    private final ReentrantLock validatorLock = new ReentrantLock();

    X509TrustManagerImpl(String validatorType, Collection<X509Certificate> trustedCerts) {
        this.validatorType = validatorType;
        this.pkixParams = null;
        if (trustedCerts == null) {
            trustedCerts = Collections.emptySet();
        }
        this.trustedCerts = trustedCerts;
        if (SSLLogger.isOn && SSLLogger.isOn("ssl,trustmanager")) {
            SSLLogger.fine("adding as trusted certificates", trustedCerts.toArray(new X509Certificate[0]));
        }
    }

    X509TrustManagerImpl(String validatorType, PKIXBuilderParameters params) {
        this.validatorType = validatorType;
        this.pkixParams = params;
        Validator v = this.getValidator("tls server");
        this.trustedCerts = v.getTrustedCertificates();
        this.serverValidator = v;
        if (SSLLogger.isOn && SSLLogger.isOn("ssl,trustmanager")) {
            SSLLogger.fine("adding as trusted certificates", this.trustedCerts.toArray(new X509Certificate[0]));
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        this.checkTrusted(chain, authType, (Socket)null, true);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        this.checkTrusted(chain, authType, (Socket)null, false);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        X509Certificate[] certsArray = new X509Certificate[this.trustedCerts.size()];
        this.trustedCerts.toArray(certsArray);
        return certsArray;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        this.checkTrusted(chain, authType, socket, true);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        this.checkTrusted(chain, authType, socket, false);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        this.checkTrusted(chain, authType, engine, true);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        this.checkTrusted(chain, authType, engine, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private Validator checkTrustedInit(X509Certificate[] chain, String authType, boolean checkClientTrusted) {
        Validator v;
        if (chain == null || chain.length == 0) {
            throw new IllegalArgumentException("null or zero-length certificate chain");
        }
        if (authType == null || authType.isEmpty()) {
            throw new IllegalArgumentException("null or zero-length authentication type");
        }
        if (checkClientTrusted) {
            v = this.clientValidator;
            if (v != null) return v;
            this.validatorLock.lock();
            try {
                v = this.clientValidator;
                if (v != null) return v;
                this.clientValidator = v = this.getValidator("tls client");
                return v;
            }
            finally {
                this.validatorLock.unlock();
            }
        }
        v = this.serverValidator;
        if (v != null) return v;
        this.validatorLock.lock();
        try {
            v = this.serverValidator;
            if (v != null) return v;
            this.serverValidator = v = this.getValidator("tls server");
            return v;
        }
        finally {
            this.validatorLock.unlock();
        }
    }

    private void checkTrusted(X509Certificate[] chain, String authType, Socket socket, boolean checkClientTrusted) throws CertificateException {
        X509Certificate[] trustedChain;
        Validator v = this.checkTrustedInit(chain, authType, checkClientTrusted);
        if (socket != null && socket.isConnected() && socket instanceof SSLSocket) {
            AlgorithmConstraints constraints;
            SSLSocket sslSocket = (SSLSocket)socket;
            SSLSession session = sslSocket.getHandshakeSession();
            if (session == null) {
                throw new CertificateException("No handshake session");
            }
            boolean isExtSession = session instanceof ExtendedSSLSession;
            if (isExtSession && ProtocolVersion.useTLS12PlusSpec(session.getProtocol())) {
                ExtendedSSLSession extSession = (ExtendedSSLSession)session;
                String[] localSupportedSignAlgs = extSession.getLocalSupportedSignatureAlgorithms();
                constraints = SSLAlgorithmConstraints.forSocket(sslSocket, localSupportedSignAlgs, false);
            } else {
                constraints = SSLAlgorithmConstraints.forSocket(sslSocket, false);
            }
            List<byte[]> responseList = Collections.emptyList();
            if (!checkClientTrusted && isExtSession) {
                responseList = ((ExtendedSSLSession)session).getStatusResponses();
            }
            trustedChain = v.validate(chain, null, responseList, constraints, checkClientTrusted ? null : authType);
            String identityAlg = sslSocket.getSSLParameters().getEndpointIdentificationAlgorithm();
            if (identityAlg != null && !identityAlg.isEmpty()) {
                X509TrustManagerImpl.checkIdentity(session, trustedChain, identityAlg, checkClientTrusted);
            }
        } else {
            trustedChain = v.validate(chain, null, Collections.emptyList(), null, checkClientTrusted ? null : authType);
        }
        if (SSLLogger.isOn && SSLLogger.isOn("ssl,trustmanager")) {
            SSLLogger.fine("Found trusted certificate", trustedChain[trustedChain.length - 1]);
        }
    }

    private void checkTrusted(X509Certificate[] chain, String authType, SSLEngine engine, boolean checkClientTrusted) throws CertificateException {
        X509Certificate[] trustedChain;
        Validator v = this.checkTrustedInit(chain, authType, checkClientTrusted);
        if (engine != null) {
            AlgorithmConstraints constraints;
            SSLSession session = engine.getHandshakeSession();
            if (session == null) {
                throw new CertificateException("No handshake session");
            }
            boolean isExtSession = session instanceof ExtendedSSLSession;
            if (isExtSession && ProtocolVersion.useTLS12PlusSpec(session.getProtocol())) {
                ExtendedSSLSession extSession = (ExtendedSSLSession)session;
                String[] localSupportedSignAlgs = extSession.getLocalSupportedSignatureAlgorithms();
                constraints = SSLAlgorithmConstraints.forEngine(engine, localSupportedSignAlgs, false);
            } else {
                constraints = SSLAlgorithmConstraints.forEngine(engine, false);
            }
            List<byte[]> responseList = Collections.emptyList();
            if (!checkClientTrusted && isExtSession) {
                responseList = ((ExtendedSSLSession)session).getStatusResponses();
            }
            trustedChain = v.validate(chain, null, responseList, constraints, checkClientTrusted ? null : authType);
            String identityAlg = engine.getSSLParameters().getEndpointIdentificationAlgorithm();
            if (identityAlg != null && !identityAlg.isEmpty()) {
                X509TrustManagerImpl.checkIdentity(session, trustedChain, identityAlg, checkClientTrusted);
            }
        } else {
            trustedChain = v.validate(chain, null, Collections.emptyList(), null, checkClientTrusted ? null : authType);
        }
        if (SSLLogger.isOn && SSLLogger.isOn("ssl,trustmanager")) {
            SSLLogger.fine("Found trusted certificate", trustedChain[trustedChain.length - 1]);
        }
    }

    private Validator getValidator(String variant) {
        Validator v = this.pkixParams == null ? Validator.getInstance(this.validatorType, variant, this.trustedCerts) : Validator.getInstance(this.validatorType, variant, this.pkixParams);
        return v;
    }

    private static String getHostNameInSNI(List<SNIServerName> sniNames) {
        SNIHostName hostname = null;
        for (SNIServerName sniName : sniNames) {
            if (sniName.getType() != 0) continue;
            if (sniName instanceof SNIHostName) {
                hostname = (SNIHostName)sniName;
                break;
            }
            try {
                hostname = new SNIHostName(sniName.getEncoded());
                break;
            }
            catch (IllegalArgumentException iae) {
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl,trustmanager")) break;
                SSLLogger.fine("Illegal server name: " + sniName, new Object[0]);
                break;
            }
        }
        if (hostname != null) {
            return hostname.getAsciiName();
        }
        return null;
    }

    static List<SNIServerName> getRequestedServerNames(Socket socket) {
        if (socket != null && socket.isConnected() && socket instanceof SSLSocket) {
            return X509TrustManagerImpl.getRequestedServerNames(((SSLSocket)socket).getHandshakeSession());
        }
        return Collections.emptyList();
    }

    static List<SNIServerName> getRequestedServerNames(SSLEngine engine) {
        if (engine != null) {
            return X509TrustManagerImpl.getRequestedServerNames(engine.getHandshakeSession());
        }
        return Collections.emptyList();
    }

    private static List<SNIServerName> getRequestedServerNames(SSLSession session) {
        if (session instanceof ExtendedSSLSession) {
            return ((ExtendedSSLSession)session).getRequestedServerNames();
        }
        return Collections.emptyList();
    }

    static void checkIdentity(SSLSession session, X509Certificate[] trustedChain, String algorithm, boolean checkClientTrusted) throws CertificateException {
        String peerHost;
        boolean identifiable;
        boolean chainsToPublicCA;
        block8: {
            List<SNIServerName> sniNames;
            String sniHostName;
            chainsToPublicCA = AnchorCertificates.contains(trustedChain[trustedChain.length - 1]);
            identifiable = false;
            peerHost = session.getPeerHost();
            if (peerHost != null && peerHost.endsWith(".")) {
                peerHost = peerHost.substring(0, peerHost.length() - 1);
            }
            if (!checkClientTrusted && (sniHostName = X509TrustManagerImpl.getHostNameInSNI(sniNames = X509TrustManagerImpl.getRequestedServerNames(session))) != null) {
                try {
                    X509TrustManagerImpl.checkIdentity(sniHostName, trustedChain[0], algorithm, chainsToPublicCA);
                    identifiable = true;
                }
                catch (CertificateException ce) {
                    if (!sniHostName.equalsIgnoreCase(peerHost)) break block8;
                    throw ce;
                }
            }
        }
        if (!identifiable) {
            try {
                X509TrustManagerImpl.checkIdentity(peerHost, trustedChain[0], algorithm, chainsToPublicCA);
            }
            catch (CertificateException ce) {
                if (checkClientTrusted && "HTTPS".equalsIgnoreCase(algorithm)) {
                    throw new CertificateException("Endpoint Identification Algorithm HTTPS is not supported on the server side");
                }
                throw ce;
            }
        }
    }

    static void checkIdentity(String hostname, X509Certificate cert, String algorithm) throws CertificateException {
        X509TrustManagerImpl.checkIdentity(hostname, cert, algorithm, false);
    }

    private static void checkIdentity(String hostname, X509Certificate cert, String algorithm, boolean chainsToPublicCA) throws CertificateException {
        if (algorithm != null && !algorithm.isEmpty()) {
            if (hostname != null && hostname.startsWith("[") && hostname.endsWith("]")) {
                hostname = hostname.substring(1, hostname.length() - 1);
            }
            if (algorithm.equalsIgnoreCase("HTTPS")) {
                HostnameChecker.getInstance((byte)1).match(hostname, cert, chainsToPublicCA);
            } else if (algorithm.equalsIgnoreCase("LDAP") || algorithm.equalsIgnoreCase("LDAPS")) {
                HostnameChecker.getInstance((byte)2).match(hostname, cert, chainsToPublicCA);
            } else {
                throw new CertificateException("Unknown identification algorithm: " + algorithm);
            }
        }
    }
}

