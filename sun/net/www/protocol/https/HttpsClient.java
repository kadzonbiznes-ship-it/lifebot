/*
 * Decompiled with CFR 0.152.
 */
package sun.net.www.protocol.https;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import sun.net.www.http.HttpClient;
import sun.net.www.protocol.http.HttpURLConnection;
import sun.security.action.GetPropertyAction;
import sun.security.ssl.SSLSocketImpl;
import sun.security.util.HostnameChecker;
import sun.util.logging.PlatformLogger;

final class HttpsClient
extends HttpClient
implements HandshakeCompletedListener {
    private static final int httpsPortNumber = 443;
    private static final String defaultHVCanonicalName = "javax.net.ssl.HttpsURLConnection.DefaultHostnameVerifier";
    private HostnameVerifier hv;
    private SSLSocketFactory sslSocketFactory;
    private SSLSession session;

    @Override
    protected int getDefaultPort() {
        return 443;
    }

    private String[] getCipherSuites() {
        String[] ciphers;
        String cipherString = GetPropertyAction.privilegedGetProperty("https.cipherSuites");
        if (cipherString == null || cipherString.isEmpty()) {
            ciphers = null;
        } else {
            ArrayList<String> v = new ArrayList<String>();
            StringTokenizer tokenizer = new StringTokenizer(cipherString, ",");
            while (tokenizer.hasMoreTokens()) {
                v.add(tokenizer.nextToken());
            }
            ciphers = new String[v.size()];
            for (int i = 0; i < ciphers.length; ++i) {
                ciphers[i] = (String)v.get(i);
            }
        }
        return ciphers;
    }

    private String[] getProtocols() {
        String[] protocols;
        String protocolString = GetPropertyAction.privilegedGetProperty("https.protocols");
        if (protocolString == null || protocolString.isEmpty()) {
            protocols = null;
        } else {
            ArrayList<String> v = new ArrayList<String>();
            StringTokenizer tokenizer = new StringTokenizer(protocolString, ",");
            while (tokenizer.hasMoreTokens()) {
                v.add(tokenizer.nextToken());
            }
            protocols = new String[v.size()];
            for (int i = 0; i < protocols.length; ++i) {
                protocols[i] = (String)v.get(i);
            }
        }
        return protocols;
    }

    private String getUserAgent() {
        String userAgent = GetPropertyAction.privilegedGetProperty("https.agent");
        if (userAgent == null || userAgent.isEmpty()) {
            userAgent = "JSSE";
        }
        return userAgent;
    }

    private HttpsClient(SSLSocketFactory sf, URL url) throws IOException {
        this(sf, url, (String)null, -1);
    }

    HttpsClient(SSLSocketFactory sf, URL url, String proxyHost, int proxyPort) throws IOException {
        this(sf, url, proxyHost, proxyPort, -1);
    }

    HttpsClient(SSLSocketFactory sf, URL url, String proxyHost, int proxyPort, int connectTimeout) throws IOException {
        this(sf, url, proxyHost == null ? null : HttpClient.newHttpProxy(proxyHost, proxyPort, "https"), connectTimeout);
    }

    HttpsClient(SSLSocketFactory sf, URL url, Proxy proxy, int connectTimeout) throws IOException {
        PlatformLogger logger = HttpURLConnection.getHttpLogger();
        if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
            logger.finest("Creating new HttpsClient with url:" + url + " and proxy:" + proxy + " with connect timeout:" + connectTimeout);
        }
        this.proxy = proxy;
        this.setSSLSocketFactory(sf);
        this.proxyDisabled = true;
        this.host = url.getHost();
        this.url = url;
        this.port = url.getPort();
        if (this.port == -1) {
            this.port = this.getDefaultPort();
        }
        this.setConnectTimeout(connectTimeout);
        this.openServer();
    }

    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv, HttpURLConnection httpuc) throws IOException {
        return HttpsClient.New(sf, url, hv, true, httpuc);
    }

    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv, boolean useCache, HttpURLConnection httpuc) throws IOException {
        return HttpsClient.New(sf, url, hv, (String)null, -1, useCache, httpuc);
    }

    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv, String proxyHost, int proxyPort, HttpURLConnection httpuc) throws IOException {
        return HttpsClient.New(sf, url, hv, proxyHost, proxyPort, true, httpuc);
    }

    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv, String proxyHost, int proxyPort, boolean useCache, HttpURLConnection httpuc) throws IOException {
        return HttpsClient.New(sf, url, hv, proxyHost, proxyPort, useCache, -1, httpuc);
    }

    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv, String proxyHost, int proxyPort, boolean useCache, int connectTimeout, HttpURLConnection httpuc) throws IOException {
        return HttpsClient.New(sf, url, hv, proxyHost == null ? null : HttpClient.newHttpProxy(proxyHost, proxyPort, "https"), useCache, connectTimeout, httpuc);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv, Proxy p, boolean useCache, int connectTimeout, HttpURLConnection httpuc) throws IOException {
        if (p == null) {
            p = Proxy.NO_PROXY;
        }
        if ((logger = HttpURLConnection.getHttpLogger()).isLoggable(PlatformLogger.Level.FINEST)) {
            logger.finest("Looking for HttpClient for URL " + url + " and proxy value of " + p);
        }
        ret = null;
        if (useCache) {
            ret = (HttpsClient)HttpsClient.kac.get(url, sf);
            if (ret != null && httpuc != null && httpuc.streaming() && "POST".equals(httpuc.getRequestMethod()) && !ret.available()) {
                ret = null;
            }
            if (ret != null) {
                ak = httpuc == null ? null : httpuc.getAuthCache();
                v0 = compatible = (ret.proxy != null && ret.proxy.equals(p) != false || ret.proxy == null && p == Proxy.NO_PROXY) && Objects.equals(ret.getAuthCache(), ak) != false;
                if (compatible) {
                    ret.lock();
                    try {
                        ret.cachedHttpClient = true;
                        if (!HttpsClient.$assertionsDisabled && !ret.inCache) {
                            throw new AssertionError();
                        }
                        ret.inCache = false;
                        if (httpuc != null && ret.needsTunneling()) {
                            httpuc.setTunnelState(HttpURLConnection.TunnelState.TUNNELING);
                        }
                        if (!logger.isLoggable(PlatformLogger.Level.FINEST)) ** GOTO lbl40
                        logger.finest("KeepAlive stream retrieved from the cache, " + ret);
                    }
                    finally {
                        ret.unlock();
                    }
                } else {
                    ret.lock();
                    try {
                        if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
                            logger.finest("Not returning this connection to cache: " + ret);
                        }
                        ret.inCache = false;
                        ret.closeServer();
                    }
                    finally {
                        ret.unlock();
                    }
                    ret = null;
                }
            }
        }
lbl40:
        // 7 sources

        if (ret == null) {
            ret = new HttpsClient(sf, url, p, connectTimeout);
            if (httpuc != null) {
                ret.authcache = httpuc.getAuthCache();
            }
        } else {
            security = System.getSecurityManager();
            if (security != null) {
                if (ret.proxy == Proxy.NO_PROXY || ret.proxy == null) {
                    security.checkConnect(InetAddress.getByName(url.getHost()).getHostAddress(), url.getPort());
                } else {
                    security.checkConnect(url.getHost(), url.getPort());
                }
            }
            ret.url = url;
        }
        ret.setHostnameVerifier(hv);
        return ret;
    }

    void setHostnameVerifier(HostnameVerifier hv) {
        this.hv = hv;
    }

    void setSSLSocketFactory(SSLSocketFactory sf) {
        this.sslSocketFactory = sf;
    }

    SSLSocketFactory getSSLSocketFactory() {
        return this.sslSocketFactory;
    }

    @Override
    protected Socket createSocket() throws IOException {
        try {
            return this.sslSocketFactory.createSocket();
        }
        catch (SocketException se) {
            Throwable t = se.getCause();
            if (t instanceof UnsupportedOperationException) {
                return super.createSocket();
            }
            throw se;
        }
    }

    @Override
    public void closeServer() {
        try {
            this.serverSocket.setSoTimeout(1);
        }
        catch (Exception exception) {
            // empty catch block
        }
        super.closeServer();
    }

    @Override
    public boolean needsTunneling() {
        return this.proxy != null && this.proxy.type() != Proxy.Type.DIRECT && this.proxy.type() != Proxy.Type.SOCKS;
    }

    @Override
    public void afterConnect() throws IOException, UnknownHostException {
        if (!this.isCachedConnection()) {
            SSLSocket s = null;
            SSLSocketFactory factory = this.sslSocketFactory;
            try {
                if (!(this.serverSocket instanceof SSLSocket)) {
                    s = (SSLSocket)factory.createSocket(this.serverSocket, this.host, this.port, true);
                } else {
                    s = (SSLSocket)this.serverSocket;
                    if (s instanceof SSLSocketImpl) {
                        ((SSLSocketImpl)s).setHost(this.host);
                    }
                }
            }
            catch (IOException ex) {
                try {
                    s = (SSLSocket)factory.createSocket(this.host, this.port);
                }
                catch (IOException ignored) {
                    throw ex;
                }
            }
            String[] protocols = this.getProtocols();
            String[] ciphers = this.getCipherSuites();
            if (protocols != null) {
                s.setEnabledProtocols(protocols);
            }
            if (ciphers != null) {
                s.setEnabledCipherSuites(ciphers);
            }
            s.addHandshakeCompletedListener(this);
            boolean needToCheckSpoofing = true;
            String identification = s.getSSLParameters().getEndpointIdentificationAlgorithm();
            if (identification != null && identification.length() != 0) {
                if (identification.equalsIgnoreCase("HTTPS")) {
                    needToCheckSpoofing = false;
                }
            } else {
                boolean isDefaultHostnameVerifier = false;
                if (this.hv != null) {
                    String canonicalName = this.hv.getClass().getCanonicalName();
                    if (canonicalName != null && canonicalName.equalsIgnoreCase(defaultHVCanonicalName)) {
                        isDefaultHostnameVerifier = true;
                    }
                } else {
                    isDefaultHostnameVerifier = true;
                }
                if (isDefaultHostnameVerifier) {
                    SSLParameters parameters = s.getSSLParameters();
                    parameters.setEndpointIdentificationAlgorithm("HTTPS");
                    if (!(s instanceof SSLSocketImpl)) {
                        parameters.setServerNames(List.of(new SNIHostName(this.host)));
                    }
                    s.setSSLParameters(parameters);
                    needToCheckSpoofing = false;
                }
            }
            s.startHandshake();
            this.session = s.getSession();
            this.serverSocket = s;
            try {
                this.serverOutput = new PrintStream((OutputStream)new BufferedOutputStream(this.serverSocket.getOutputStream()), false, encoding);
            }
            catch (UnsupportedEncodingException e) {
                throw new InternalError(encoding + " encoding not found");
            }
            if (needToCheckSpoofing) {
                this.checkURLSpoofing(this.hv);
            }
        } else {
            this.session = ((SSLSocket)this.serverSocket).getSession();
        }
    }

    private void checkURLSpoofing(HostnameVerifier hostnameVerifier) throws IOException {
        String host = this.url.getHost();
        if (host != null && host.startsWith("[") && host.endsWith("]")) {
            host = host.substring(1, host.length() - 1);
        }
        Certificate[] peerCerts = null;
        String cipher = this.session.getCipherSuite();
        try {
            HostnameChecker checker = HostnameChecker.getInstance((byte)1);
            peerCerts = this.session.getPeerCertificates();
            if (!(peerCerts[0] instanceof X509Certificate)) {
                throw new SSLPeerUnverifiedException("");
            }
            X509Certificate peerCert = (X509Certificate)peerCerts[0];
            checker.match(host, peerCert);
            return;
        }
        catch (SSLPeerUnverifiedException sSLPeerUnverifiedException) {
        }
        catch (CertificateException certificateException) {
            // empty catch block
        }
        if (cipher != null && cipher.contains("_anon_")) {
            return;
        }
        if (hostnameVerifier != null && hostnameVerifier.verify(host, this.session)) {
            return;
        }
        this.serverSocket.close();
        this.session.invalidate();
        throw new IOException("HTTPS hostname wrong:  should be <" + this.url.getHost() + ">");
    }

    @Override
    protected void putInKeepAliveCache() {
        this.lock();
        try {
            if (this.inCache) {
                assert (false) : "Duplicate put to keep alive cache";
                return;
            }
            this.inCache = true;
            kac.put(this.url, this.sslSocketFactory, this);
        }
        finally {
            this.unlock();
        }
    }

    @Override
    public void closeIdleConnection() {
        HttpClient http = kac.get(this.url, this.sslSocketFactory);
        if (http != null) {
            http.closeServer();
        }
    }

    String getCipherSuite() {
        return this.session.getCipherSuite();
    }

    public Certificate[] getLocalCertificates() {
        return this.session.getLocalCertificates();
    }

    Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
        return this.session.getPeerCertificates();
    }

    Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        Principal principal;
        try {
            principal = this.session.getPeerPrincipal();
        }
        catch (AbstractMethodError e) {
            Certificate[] certs = this.session.getPeerCertificates();
            principal = ((X509Certificate)certs[0]).getSubjectX500Principal();
        }
        return principal;
    }

    Principal getLocalPrincipal() {
        Principal principal;
        block2: {
            try {
                principal = this.session.getLocalPrincipal();
            }
            catch (AbstractMethodError e) {
                principal = null;
                Certificate[] certs = this.session.getLocalCertificates();
                if (certs == null) break block2;
                principal = ((X509Certificate)certs[0]).getSubjectX500Principal();
            }
        }
        return principal;
    }

    SSLSession getSSLSession() {
        return this.session;
    }

    @Override
    public void handshakeCompleted(HandshakeCompletedEvent event) {
        this.session = event.getSession();
    }

    @Override
    public String getProxyHostUsed() {
        if (!this.needsTunneling()) {
            return null;
        }
        return super.getProxyHostUsed();
    }

    @Override
    public int getProxyPortUsed() {
        return this.proxy == null || this.proxy.type() == Proxy.Type.DIRECT || this.proxy.type() == Proxy.Type.SOCKS ? -1 : ((InetSocketAddress)this.proxy.address()).getPort();
    }
}

