/*
 * Decompiled with CFR 0.152.
 */
package sun.net.www.protocol.https;

import java.io.IOException;
import java.net.Authenticator;
import java.net.Proxy;
import java.net.SecureCacheResponse;
import java.net.URL;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import sun.net.www.http.HttpClient;
import sun.net.www.protocol.http.Handler;
import sun.net.www.protocol.http.HttpCallerInfo;
import sun.net.www.protocol.http.HttpURLConnection;
import sun.net.www.protocol.https.HttpsClient;

public abstract class AbstractDelegateHttpsURLConnection
extends HttpURLConnection {
    protected AbstractDelegateHttpsURLConnection(URL url, Handler handler) throws IOException {
        this(url, null, handler);
    }

    protected AbstractDelegateHttpsURLConnection(URL url, Proxy p, Handler handler) throws IOException {
        super(url, p, handler);
    }

    protected abstract SSLSocketFactory getSSLSocketFactory();

    protected abstract HostnameVerifier getHostnameVerifier();

    @Override
    public void setNewClient(URL url) throws IOException {
        this.setNewClient(url, false);
    }

    @Override
    public void setNewClient(URL url, boolean useCache) throws IOException {
        int readTimeout = this.getReadTimeout();
        this.http = HttpsClient.New(this.getSSLSocketFactory(), url, this.getHostnameVerifier(), null, -1, useCache, this.getConnectTimeout(), this);
        this.http.setReadTimeout(readTimeout);
        ((HttpsClient)this.http).afterConnect();
    }

    @Override
    public void setProxiedClient(URL url, String proxyHost, int proxyPort) throws IOException {
        this.setProxiedClient(url, proxyHost, proxyPort, false);
    }

    @Override
    public void setProxiedClient(URL url, String proxyHost, int proxyPort, boolean useCache) throws IOException {
        this.proxiedConnect(url, proxyHost, proxyPort, useCache);
        if (!this.http.isCachedConnection()) {
            this.doTunneling();
        }
        ((HttpsClient)this.http).afterConnect();
    }

    @Override
    protected void proxiedConnect(URL url, String proxyHost, int proxyPort, boolean useCache) throws IOException {
        if (this.connected) {
            return;
        }
        int readTimeout = this.getReadTimeout();
        this.http = HttpsClient.New(this.getSSLSocketFactory(), url, this.getHostnameVerifier(), proxyHost, proxyPort, useCache, this.getConnectTimeout(), this);
        this.http.setReadTimeout(readTimeout);
        this.connected = true;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean conn) {
        this.connected = conn;
    }

    @Override
    public void connect() throws IOException {
        if (this.connected) {
            return;
        }
        this.plainConnect();
        if (this.cachedResponse != null) {
            return;
        }
        if (!this.http.isCachedConnection() && this.http.needsTunneling()) {
            this.doTunneling();
        }
        ((HttpsClient)this.http).afterConnect();
    }

    @Override
    protected HttpClient getNewHttpClient(URL url, Proxy p, int connectTimeout) throws IOException {
        return HttpsClient.New(this.getSSLSocketFactory(), url, this.getHostnameVerifier(), p, true, connectTimeout, (HttpURLConnection)this);
    }

    @Override
    protected HttpClient getNewHttpClient(URL url, Proxy p, int connectTimeout, boolean useCache) throws IOException {
        return HttpsClient.New(this.getSSLSocketFactory(), url, this.getHostnameVerifier(), p, useCache, connectTimeout, (HttpURLConnection)this);
    }

    public String getCipherSuite() {
        if (this.cachedResponse != null) {
            return ((SecureCacheResponse)this.cachedResponse).getCipherSuite();
        }
        if (this.http == null) {
            throw new IllegalStateException("connection not yet open");
        }
        return ((HttpsClient)this.http).getCipherSuite();
    }

    public Certificate[] getLocalCertificates() {
        if (this.cachedResponse != null) {
            List<Certificate> l = ((SecureCacheResponse)this.cachedResponse).getLocalCertificateChain();
            if (l == null) {
                return null;
            }
            return l.toArray(new Certificate[0]);
        }
        if (this.http == null) {
            throw new IllegalStateException("connection not yet open");
        }
        return ((HttpsClient)this.http).getLocalCertificates();
    }

    public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
        if (this.cachedResponse != null) {
            List<Certificate> l = ((SecureCacheResponse)this.cachedResponse).getServerCertificateChain();
            if (l == null) {
                return null;
            }
            return l.toArray(new Certificate[0]);
        }
        if (this.http == null) {
            throw new IllegalStateException("connection not yet open");
        }
        return ((HttpsClient)this.http).getServerCertificates();
    }

    Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        if (this.cachedResponse != null) {
            return ((SecureCacheResponse)this.cachedResponse).getPeerPrincipal();
        }
        if (this.http == null) {
            throw new IllegalStateException("connection not yet open");
        }
        return ((HttpsClient)this.http).getPeerPrincipal();
    }

    Principal getLocalPrincipal() {
        if (this.cachedResponse != null) {
            return ((SecureCacheResponse)this.cachedResponse).getLocalPrincipal();
        }
        if (this.http == null) {
            throw new IllegalStateException("connection not yet open");
        }
        return ((HttpsClient)this.http).getLocalPrincipal();
    }

    SSLSession getSSLSession() {
        Optional<SSLSession> option;
        if (this.cachedResponse != null && (option = ((SecureCacheResponse)this.cachedResponse).getSSLSession()).isPresent()) {
            return option.orElseThrow();
        }
        if (this.http == null) {
            throw new IllegalStateException("connection not yet open");
        }
        return ((HttpsClient)this.http).getSSLSession();
    }

    private boolean useExtendedCallerInfo(URL url) {
        HttpsClient https = (HttpsClient)this.http;
        if (https.getSSLSession() == null) {
            return false;
        }
        String prop = this.http.getSpnegoCBT();
        if (prop.equals("never")) {
            return false;
        }
        String target = url.getHost();
        if (prop.startsWith("domain:")) {
            String[] domains;
            for (String domain : domains = prop.substring(7).split(",")) {
                if (target.equalsIgnoreCase(domain)) {
                    return true;
                }
                if (!domain.startsWith("*.") || !target.regionMatches(true, target.length() - domain.length() + 1, domain, 1, domain.length() - 1)) continue;
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    protected HttpCallerInfo getHttpCallerInfo(URL url, String proxy, int port, Authenticator authenticator) {
        if (!this.useExtendedCallerInfo(url)) {
            return super.getHttpCallerInfo(url, proxy, port, authenticator);
        }
        HttpsClient https = (HttpsClient)this.http;
        try {
            Certificate[] certs = https.getServerCertificates();
            Certificate certificate = certs[0];
            if (certificate instanceof X509Certificate) {
                X509Certificate x509Cert = (X509Certificate)certificate;
                return new HttpCallerInfo(url, proxy, port, x509Cert, authenticator);
            }
        }
        catch (SSLPeerUnverifiedException sSLPeerUnverifiedException) {
            // empty catch block
        }
        return super.getHttpCallerInfo(url, proxy, port, authenticator);
    }

    @Override
    protected HttpCallerInfo getHttpCallerInfo(URL url, Authenticator authenticator) {
        if (!this.useExtendedCallerInfo(url)) {
            return super.getHttpCallerInfo(url, authenticator);
        }
        HttpsClient https = (HttpsClient)this.http;
        try {
            Certificate[] certs = https.getServerCertificates();
            Certificate certificate = certs[0];
            if (certificate instanceof X509Certificate) {
                X509Certificate x509Cert = (X509Certificate)certificate;
                return new HttpCallerInfo(url, x509Cert, authenticator);
            }
        }
        catch (SSLPeerUnverifiedException sSLPeerUnverifiedException) {
            // empty catch block
        }
        return super.getHttpCallerInfo(url, authenticator);
    }
}

