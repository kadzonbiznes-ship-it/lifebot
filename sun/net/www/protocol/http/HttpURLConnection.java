/*
 * Decompiled with CFR 0.152.
 */
package sun.net.www.protocol.http;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.CookieHandler;
import java.net.HttpCookie;
import java.net.HttpRetryException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.ResponseCache;
import java.net.SecureCacheResponse;
import java.net.SocketPermission;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLPermission;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.locks.ReentrantLock;
import jdk.internal.access.JavaNetHttpCookieAccess;
import jdk.internal.access.SharedSecrets;
import sun.net.ApplicationProxy;
import sun.net.NetProperties;
import sun.net.util.IPAddressUtil;
import sun.net.util.ProxyUtil;
import sun.net.www.HeaderParser;
import sun.net.www.MessageHeader;
import sun.net.www.MeteredStream;
import sun.net.www.ParseUtil;
import sun.net.www.http.ChunkedInputStream;
import sun.net.www.http.ChunkedOutputStream;
import sun.net.www.http.HttpClient;
import sun.net.www.http.PosterOutputStream;
import sun.net.www.protocol.http.AuthCacheImpl;
import sun.net.www.protocol.http.AuthScheme;
import sun.net.www.protocol.http.AuthenticationHeader;
import sun.net.www.protocol.http.AuthenticationInfo;
import sun.net.www.protocol.http.BasicAuthentication;
import sun.net.www.protocol.http.DigestAuthentication;
import sun.net.www.protocol.http.Handler;
import sun.net.www.protocol.http.HttpAuthenticator;
import sun.net.www.protocol.http.HttpCallerInfo;
import sun.net.www.protocol.http.NTLMAuthenticationProxy;
import sun.net.www.protocol.http.NegotiateAuthentication;
import sun.security.action.GetIntegerAction;
import sun.security.action.GetPropertyAction;
import sun.util.logging.PlatformLogger;

public class HttpURLConnection
extends java.net.HttpURLConnection {
    static final String HTTP_CONNECT = "CONNECT";
    static final String version;
    public static final String userAgent;
    static final int defaultmaxRedirects = 20;
    static final int maxRedirects;
    static final boolean validateProxy;
    static final boolean validateServer;
    static final Set<String> disabledProxyingSchemes;
    static final Set<String> disabledTunnelingSchemes;
    private StreamingOutputStream strOutputStream;
    private static final String RETRY_MSG1 = "cannot retry due to proxy authentication, in streaming mode";
    private static final String RETRY_MSG2 = "cannot retry due to server authentication, in streaming mode";
    private static final String RETRY_MSG3 = "cannot retry due to redirection, in streaming mode";
    private static boolean enableESBuffer;
    private static int timeout4ESBuffer;
    private static int bufSize4ES;
    private static final int maxHeaderSize;
    private static final boolean allowRestrictedHeaders;
    private static final Set<String> restrictedHeaderSet;
    private static final String[] restrictedHeaders;
    static final String httpVersion = "HTTP/1.1";
    static final String acceptString = "*/*";
    private static final String[] EXCLUDE_HEADERS;
    private static final String[] EXCLUDE_HEADERS2;
    protected HttpClient http;
    protected Handler handler;
    protected Proxy instProxy;
    protected volatile Authenticator authenticator;
    protected volatile AuthCacheImpl authCache = AuthCacheImpl.getDefault();
    private CookieHandler cookieHandler;
    private final ResponseCache cacheHandler;
    private volatile boolean usingProxy;
    protected CacheResponse cachedResponse;
    private MessageHeader cachedHeaders;
    private InputStream cachedInputStream;
    protected PrintStream ps = null;
    private InputStream errorStream = null;
    private boolean setUserCookies = true;
    private String userCookies = null;
    private String userCookies2 = null;
    @Deprecated
    private static HttpAuthenticator defaultAuth;
    private MessageHeader requests;
    private MessageHeader userHeaders;
    private boolean connecting = false;
    String domain;
    DigestAuthentication.Parameters digestparams;
    AuthenticationInfo currentProxyCredentials = null;
    AuthenticationInfo currentServerCredentials = null;
    boolean needToCheck = true;
    private boolean doingNTLM2ndStage = false;
    private boolean doingNTLMp2ndStage = false;
    private boolean tryTransparentNTLMServer = true;
    private boolean tryTransparentNTLMProxy = true;
    private boolean useProxyResponseCode = false;
    private Object authObj;
    boolean isUserServerAuth;
    boolean isUserProxyAuth;
    String serverAuthKey;
    String proxyAuthKey;
    private MessageHeader responses;
    private InputStream inputStream = null;
    private PosterOutputStream poster = null;
    private boolean setRequests = false;
    private boolean failedOnce = false;
    private Exception rememberedException = null;
    private HttpClient reuseClient = null;
    private TunnelState tunnelState = TunnelState.NONE;
    private int connectTimeout = -1;
    private int readTimeout = -1;
    private SocketPermission socketPermission;
    private static final PlatformLogger logger;
    private final ReentrantLock connectionLock = new ReentrantLock();
    String requestURI = null;
    byte[] cdata = new byte[128];
    private static final String SET_COOKIE = "set-cookie";
    private static final String SET_COOKIE2 = "set-cookie2";
    private Map<String, List<String>> filteredHeaders;

    private static String getNetProperty(String name) {
        PrivilegedAction<String> pa = () -> NetProperties.get(name);
        return AccessController.doPrivileged(pa);
    }

    private static Set<String> schemesListToSet(String list) {
        String[] parts;
        if (list == null || list.isEmpty()) {
            return Collections.emptySet();
        }
        HashSet<String> s = new HashSet<String>();
        for (String part : parts = list.split("\\s*,\\s*")) {
            s.add(part.toLowerCase(Locale.ROOT));
        }
        return s;
    }

    private final void lock() {
        this.connectionLock.lock();
    }

    private final void unlock() {
        this.connectionLock.unlock();
    }

    public final boolean isLockHeldByCurrentThread() {
        return this.connectionLock.isHeldByCurrentThread();
    }

    private static PasswordAuthentication privilegedRequestPasswordAuthentication(final Authenticator authenticator, final String host, final InetAddress addr, final int port, final String protocol, final String prompt, final String scheme, final URL url, final Authenticator.RequestorType authType) {
        return AccessController.doPrivileged(new PrivilegedAction<PasswordAuthentication>(){

            @Override
            public PasswordAuthentication run() {
                if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
                    logger.finest("Requesting Authentication: host =" + host + " url = " + url);
                }
                PasswordAuthentication pass = Authenticator.requestPasswordAuthentication(authenticator, host, addr, port, protocol, prompt, scheme, url, authType);
                if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
                    logger.finest("Authentication returned: " + (pass != null ? pass.toString() : "null"));
                }
                return pass;
            }
        });
    }

    private boolean isRestrictedHeader(String key, String value) {
        if (allowRestrictedHeaders) {
            return false;
        }
        if (restrictedHeaderSet.contains(key = key.toLowerCase(Locale.ROOT))) {
            return !key.equals("connection") || !value.equalsIgnoreCase("close");
        }
        return key.startsWith("sec-");
    }

    private boolean isExternalMessageHeaderAllowed(String key, String value) {
        this.checkMessageHeader(key, value);
        return !this.isRestrictedHeader(key, value);
    }

    public static PlatformLogger getHttpLogger() {
        return logger;
    }

    public Object authObj() {
        return this.authObj;
    }

    public void authObj(Object authObj) {
        this.authObj = authObj;
    }

    @Override
    public void setAuthenticator(Authenticator auth) {
        this.lock();
        try {
            if (this.connecting || this.connected) {
                throw new IllegalStateException("Authenticator must be set before connecting");
            }
            this.authenticator = Objects.requireNonNull(auth);
            this.authCache = AuthCacheImpl.getAuthCacheFor(this.authenticator);
        }
        finally {
            this.unlock();
        }
    }

    public AuthCacheImpl getAuthCache() {
        return this.authCache;
    }

    private void checkMessageHeader(String key, String value) {
        int LF = 10;
        int index = key.indexOf(LF);
        int index1 = key.indexOf(58);
        if (index != -1 || index1 != -1) {
            throw new IllegalArgumentException("Illegal character(s) in message header field: " + key);
        }
        if (value == null) {
            return;
        }
        index = value.indexOf(LF);
        while (index != -1) {
            char c;
            if (++index < value.length() && ((c = value.charAt(index)) == ' ' || c == '\t')) {
                index = value.indexOf(LF, index);
                continue;
            }
            throw new IllegalArgumentException("Illegal character(s) in message header value: " + value);
        }
    }

    @Override
    public void setRequestMethod(String method) throws ProtocolException {
        this.lock();
        try {
            if (this.connecting) {
                throw new IllegalStateException("connect in progress");
            }
            super.setRequestMethod(method);
        }
        finally {
            this.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void writeRequests() throws IOException {
        assert (this.isLockHeldByCurrentThread());
        if (this.http.usingProxy && this.tunnelState() != TunnelState.TUNNELING) {
            this.setPreemptiveProxyAuthentication(this.requests);
        }
        if (!this.setRequests) {
            AuthenticationInfo sauth;
            String reqHost;
            String requestLine = this.method + " " + this.getRequestURI() + " " + httpVersion;
            int requestLineIndex = this.requests.getKey(requestLine);
            if (requestLineIndex != 0) {
                this.checkURLFile();
                this.requests.prepend(requestLine, null);
            }
            if (!this.getUseCaches()) {
                this.requests.setIfNotSet("Cache-Control", "no-cache");
                this.requests.setIfNotSet("Pragma", "no-cache");
            }
            this.requests.setIfNotSet("User-Agent", userAgent);
            int port = this.url.getPort();
            String host = HttpURLConnection.stripIPv6ZoneId(this.url.getHost());
            if (port != -1 && port != this.url.getDefaultPort()) {
                host = host + ":" + String.valueOf(port);
            }
            if ((reqHost = this.requests.findValue("Host")) == null || !reqHost.equalsIgnoreCase(host) && !this.checkSetHost()) {
                this.requests.set("Host", host);
            }
            this.requests.setIfNotSet("Accept", acceptString);
            if (this.http.getHttpKeepAliveSet()) {
                if (this.http.usingProxy && this.tunnelState() != TunnelState.TUNNELING) {
                    this.requests.setIfNotSet("Proxy-Connection", "keep-alive");
                } else {
                    this.requests.setIfNotSet("Connection", "keep-alive");
                }
            } else {
                this.requests.setIfNotSet("Connection", "close");
            }
            long modTime = this.getIfModifiedSince();
            if (modTime != 0L) {
                Date date = new Date(modTime);
                SimpleDateFormat fo = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
                fo.setTimeZone(TimeZone.getTimeZone("GMT"));
                this.requests.setIfNotSet("If-Modified-Since", fo.format(date));
            }
            if ((sauth = AuthenticationInfo.getServerAuth(this.url, this.authCache)) != null && sauth.supportsPreemptiveAuthorization()) {
                this.requests.setIfNotSet(sauth.getHeaderName(), sauth.getHeaderValue(this.url, this.method));
                this.currentServerCredentials = sauth;
            }
            if (!this.method.equals("PUT") && (this.poster != null || this.streaming())) {
                this.requests.setIfNotSet("Content-type", "application/x-www-form-urlencoded");
            }
            boolean chunked = false;
            if (this.streaming()) {
                if (this.chunkLength != -1) {
                    this.requests.set("Transfer-Encoding", "chunked");
                    chunked = true;
                } else if (this.fixedContentLengthLong != -1L) {
                    this.requests.set("Content-Length", String.valueOf(this.fixedContentLengthLong));
                } else if (this.fixedContentLength != -1) {
                    this.requests.set("Content-Length", String.valueOf(this.fixedContentLength));
                }
            } else if (this.poster != null) {
                PosterOutputStream posterOutputStream = this.poster;
                synchronized (posterOutputStream) {
                    this.poster.close();
                    this.requests.set("Content-Length", String.valueOf(this.poster.size()));
                }
            }
            if (!chunked && this.requests.findValue("Transfer-Encoding") != null) {
                this.requests.remove("Transfer-Encoding");
                if (logger.isLoggable(PlatformLogger.Level.WARNING)) {
                    logger.warning("use streaming mode for chunked encoding");
                }
            }
            this.setCookieHeader();
            this.setRequests = true;
        }
        if (logger.isLoggable(PlatformLogger.Level.FINE)) {
            logger.fine(this.requests.toString());
        }
        this.http.writeRequests(this.requests, this.poster, this.streaming());
        if (this.ps.checkError()) {
            String proxyHost = this.http.getProxyHostUsed();
            int proxyPort = this.http.getProxyPortUsed();
            this.disconnectInternal();
            if (this.failedOnce) {
                throw new IOException("Error writing to server");
            }
            this.failedOnce = true;
            if (proxyHost != null) {
                this.setProxiedClient(this.url, proxyHost, proxyPort);
            } else {
                this.setNewClient(this.url);
            }
            this.ps = (PrintStream)this.http.getOutputStream();
            this.connected = true;
            this.responses = new MessageHeader(maxHeaderSize);
            this.setRequests = false;
            this.writeRequests();
        }
    }

    private boolean checkSetHost() {
        String name;
        SecurityManager s = System.getSecurityManager();
        if (s != null && ((name = s.getClass().getName()).equals("sun.plugin2.applet.AWTAppletSecurityManager") || name.equals("sun.plugin2.applet.FXAppletSecurityManager") || name.equals("com.sun.javaws.security.JavaWebStartSecurity") || name.equals("sun.plugin.security.ActivatorSecurityManager"))) {
            int CHECK_SET_HOST = -2;
            try {
                s.checkConnect(this.url.toExternalForm(), CHECK_SET_HOST);
            }
            catch (SecurityException ex) {
                return false;
            }
        }
        return true;
    }

    private void checkURLFile() {
        String name;
        SecurityManager s = System.getSecurityManager();
        if (s != null && ((name = s.getClass().getName()).equals("sun.plugin2.applet.AWTAppletSecurityManager") || name.equals("sun.plugin2.applet.FXAppletSecurityManager") || name.equals("com.sun.javaws.security.JavaWebStartSecurity") || name.equals("sun.plugin.security.ActivatorSecurityManager"))) {
            int CHECK_SUBPATH = -3;
            try {
                s.checkConnect(this.url.toExternalForm(), CHECK_SUBPATH);
            }
            catch (SecurityException ex) {
                throw new SecurityException("denied access outside a permitted URL subpath", ex);
            }
        }
    }

    protected void setNewClient(URL url) throws IOException {
        this.setNewClient(url, false);
    }

    protected void setNewClient(URL url, boolean useCache) throws IOException {
        this.http = HttpClient.New(url, null, -1, useCache, this.connectTimeout, this);
        this.http.setReadTimeout(this.readTimeout);
    }

    protected void setProxiedClient(URL url, String proxyHost, int proxyPort) throws IOException {
        this.setProxiedClient(url, proxyHost, proxyPort, false);
    }

    protected void setProxiedClient(URL url, String proxyHost, int proxyPort, boolean useCache) throws IOException {
        this.proxiedConnect(url, proxyHost, proxyPort, useCache);
    }

    protected void proxiedConnect(URL url, String proxyHost, int proxyPort, boolean useCache) throws IOException {
        this.http = HttpClient.New(url, proxyHost, proxyPort, useCache, this.connectTimeout, this);
        this.http.setReadTimeout(this.readTimeout);
    }

    protected HttpURLConnection(URL u, Handler handler) throws IOException {
        this(u, null, handler);
    }

    private static String checkHost(String h) throws IOException {
        if (h != null && h.indexOf(10) > -1) {
            throw new MalformedURLException("Illegal character in host");
        }
        return h;
    }

    public HttpURLConnection(URL u, String host, int port) throws IOException {
        this(u, new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(HttpURLConnection.checkHost(host), port)));
    }

    public HttpURLConnection(URL u, Proxy p) throws IOException {
        this(u, p, new Handler());
    }

    private static URL checkURL(URL u) throws IOException {
        if (u != null && u.toExternalForm().indexOf(10) > -1) {
            throw new MalformedURLException("Illegal character in URL");
        }
        String s = IPAddressUtil.checkAuthority(u);
        if (s != null) {
            throw new MalformedURLException(s);
        }
        return u;
    }

    protected HttpURLConnection(URL u, Proxy p, Handler handler) throws IOException {
        super(HttpURLConnection.checkURL(u));
        this.requests = new MessageHeader();
        this.responses = new MessageHeader(maxHeaderSize);
        this.userHeaders = new MessageHeader();
        this.handler = handler;
        this.instProxy = ProxyUtil.copyProxy(p);
        if (this.instProxy instanceof ApplicationProxy) {
            try {
                this.cookieHandler = CookieHandler.getDefault();
            }
            catch (SecurityException securityException) {}
        } else {
            this.cookieHandler = AccessController.doPrivileged(new PrivilegedAction<CookieHandler>(this){

                @Override
                public CookieHandler run() {
                    return CookieHandler.getDefault();
                }
            });
        }
        this.cacheHandler = AccessController.doPrivileged(new PrivilegedAction<ResponseCache>(this){

            @Override
            public ResponseCache run() {
                return ResponseCache.getDefault();
            }
        });
    }

    @Deprecated
    public static void setDefaultAuthenticator(HttpAuthenticator a) {
        defaultAuth = a;
    }

    public static InputStream openConnectionCheckRedirects(URLConnection c) throws IOException {
        InputStream in;
        boolean redir;
        int redirects = 0;
        Authenticator a = null;
        do {
            HttpURLConnection http;
            int stat;
            if (c instanceof HttpURLConnection) {
                ((HttpURLConnection)c).setInstanceFollowRedirects(false);
                if (a == null) {
                    a = ((HttpURLConnection)c).authenticator;
                }
            }
            in = c.getInputStream();
            redir = false;
            if (!(c instanceof HttpURLConnection) || (stat = (http = (HttpURLConnection)c).getResponseCode()) < 300 || stat > 307 || stat == 306 || stat == 304) continue;
            URL base = http.getURL();
            String loc = http.getHeaderField("Location");
            URL target = null;
            if (loc != null) {
                target = HttpURLConnection.newURL(base, loc);
            }
            http.disconnect();
            if (target == null || !base.getProtocol().equals(target.getProtocol()) || base.getPort() != target.getPort() || !HttpURLConnection.hostsEqual(base, target) || redirects >= 5) {
                throw new SecurityException("illegal URL redirect");
            }
            redir = true;
            c = target.openConnection();
            if (a != null && c instanceof HttpURLConnection) {
                ((HttpURLConnection)c).setAuthenticator(a);
            }
            ++redirects;
        } while (redir);
        return in;
    }

    private static boolean hostsEqual(URL u1, URL u2) {
        final String h1 = u1.getHost();
        final String h2 = u2.getHost();
        if (h1 == null) {
            return h2 == null;
        }
        if (h2 == null) {
            return false;
        }
        if (h1.equalsIgnoreCase(h2)) {
            return true;
        }
        final boolean[] result = new boolean[]{false};
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Void run() {
                try {
                    InetAddress a1 = InetAddress.getByName(h1);
                    InetAddress a2 = InetAddress.getByName(h2);
                    result[0] = a1.equals(a2);
                }
                catch (SecurityException | UnknownHostException exception) {
                    // empty catch block
                }
                return null;
            }
        });
        return result[0];
    }

    @Override
    public void connect() throws IOException {
        this.lock();
        try {
            this.connecting = true;
        }
        finally {
            this.unlock();
        }
        this.plainConnect();
    }

    private boolean checkReuseConnection() {
        if (this.connected) {
            return true;
        }
        if (this.reuseClient != null) {
            this.http = this.reuseClient;
            this.http.setReadTimeout(this.getReadTimeout());
            this.http.reuse = false;
            this.reuseClient = null;
            this.connected = true;
            return true;
        }
        return false;
    }

    private String getHostAndPort(URL url) {
        String host;
        final String hostarg = host = url.getHost();
        try {
            host = AccessController.doPrivileged(new PrivilegedExceptionAction<String>(this){

                @Override
                public String run() throws IOException {
                    InetAddress addr = InetAddress.getByName(hostarg);
                    return addr.getHostAddress();
                }
            });
        }
        catch (PrivilegedActionException privilegedActionException) {
            // empty catch block
        }
        int port = url.getPort();
        if (port == -1) {
            String scheme = url.getProtocol();
            if ("http".equals(scheme)) {
                return host + ":80";
            }
            return host + ":443";
        }
        return host + ":" + Integer.toString(port);
    }

    protected void plainConnect() throws IOException {
        this.lock();
        try {
            if (this.connected) {
                return;
            }
        }
        finally {
            this.unlock();
        }
        SocketPermission p = this.URLtoSocketPermission(this.url);
        if (p != null) {
            try {
                AccessController.doPrivilegedWithCombiner(new PrivilegedExceptionAction<Object>(){

                    @Override
                    public Void run() throws IOException {
                        HttpURLConnection.this.plainConnect0();
                        return null;
                    }
                }, null, p);
            }
            catch (PrivilegedActionException e) {
                throw (IOException)e.getException();
            }
        } else {
            this.plainConnect0();
        }
    }

    SocketPermission URLtoSocketPermission(URL url) throws IOException {
        if (this.socketPermission != null) {
            return this.socketPermission;
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return null;
        }
        SocketPermission newPerm = new SocketPermission(this.getHostAndPort(url), "connect");
        String actions = this.getRequestMethod() + ":" + this.getUserSetHeaders().getHeaderNamesInList();
        String urlstring = url.getProtocol() + "://" + url.getAuthority() + url.getPath();
        URLPermission p = new URLPermission(urlstring, actions);
        try {
            sm.checkPermission(p);
            this.socketPermission = newPerm;
            return this.socketPermission;
        }
        catch (SecurityException securityException) {
            return null;
        }
    }

    protected void plainConnect0() throws IOException {
        if (this.cacheHandler != null && this.getUseCaches()) {
            try {
                URI uri = ParseUtil.toURI(this.url);
                if (uri != null) {
                    this.cachedResponse = this.cacheHandler.get(uri, this.getRequestMethod(), this.getUserSetHeaders().getHeaders());
                    if ("https".equalsIgnoreCase(uri.getScheme()) && !(this.cachedResponse instanceof SecureCacheResponse)) {
                        this.cachedResponse = null;
                    }
                    if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
                        logger.finest("Cache Request for " + uri + " / " + this.getRequestMethod());
                        logger.finest("From cache: " + (this.cachedResponse != null ? this.cachedResponse.toString() : "null"));
                    }
                    if (this.cachedResponse != null) {
                        this.cachedHeaders = this.mapToMessageHeader(this.cachedResponse.getHeaders());
                        this.cachedInputStream = this.cachedResponse.getBody();
                    }
                }
            }
            catch (IOException uri) {
                // empty catch block
            }
            if (this.cachedHeaders != null && this.cachedInputStream != null) {
                this.connected = true;
                return;
            }
            this.cachedResponse = null;
        }
        if (this.instProxy == null) {
            ProxySelector sel = AccessController.doPrivileged(new PrivilegedAction<ProxySelector>(this){

                @Override
                public ProxySelector run() {
                    return ProxySelector.getDefault();
                }
            });
            if (sel != null) {
                List<Proxy> proxies;
                URI uri = ParseUtil.toURI(this.url);
                if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
                    logger.finest("ProxySelector Request for " + uri);
                }
                try {
                    proxies = sel.select(uri);
                }
                catch (IllegalArgumentException iae) {
                    throw new IOException("Failed to select a proxy", iae);
                }
                Iterator<Proxy> it = proxies.iterator();
                while (it.hasNext()) {
                    Proxy p = ProxyUtil.copyProxy(it.next());
                    try {
                        if (!this.failedOnce) {
                            this.http = this.getNewHttpClient(this.url, p, this.connectTimeout);
                            this.http.setReadTimeout(this.readTimeout);
                        } else {
                            this.http = this.getNewHttpClient(this.url, p, this.connectTimeout, false);
                            this.http.setReadTimeout(this.readTimeout);
                        }
                        if (logger.isLoggable(PlatformLogger.Level.FINEST) && p != null) {
                            logger.finest("Proxy used: " + p.toString());
                        }
                        break;
                    }
                    catch (IOException ioex) {
                        if (p != Proxy.NO_PROXY) {
                            sel.connectFailed(uri, p.address(), ioex);
                            if (it.hasNext()) continue;
                            if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
                                logger.finest("Retrying with proxy: " + p.toString());
                            }
                            this.http = this.getNewHttpClient(this.url, p, this.connectTimeout, false);
                            this.http.setReadTimeout(this.readTimeout);
                            break;
                        }
                        throw ioex;
                    }
                }
            } else if (!this.failedOnce) {
                this.http = this.getNewHttpClient(this.url, null, this.connectTimeout);
                this.http.setReadTimeout(this.readTimeout);
            } else {
                this.http = this.getNewHttpClient(this.url, null, this.connectTimeout, false);
                this.http.setReadTimeout(this.readTimeout);
            }
        } else if (!this.failedOnce) {
            this.http = this.getNewHttpClient(this.url, this.instProxy, this.connectTimeout);
            this.http.setReadTimeout(this.readTimeout);
        } else {
            this.http = this.getNewHttpClient(this.url, this.instProxy, this.connectTimeout, false);
            this.http.setReadTimeout(this.readTimeout);
        }
        this.usingProxy = this.usingProxy || this.usingProxyInternal();
        this.ps = (PrintStream)this.http.getOutputStream();
        this.connected = true;
    }

    protected HttpClient getNewHttpClient(URL url, Proxy p, int connectTimeout) throws IOException {
        return HttpClient.New(url, p, connectTimeout, this);
    }

    protected HttpClient getNewHttpClient(URL url, Proxy p, int connectTimeout, boolean useCache) throws IOException {
        return HttpClient.New(url, p, connectTimeout, useCache, this);
    }

    private void expect100Continue() throws IOException {
        int oldTimeout = this.http.getReadTimeout();
        boolean timedOut = false;
        boolean tempTimeOutSet = false;
        if (oldTimeout <= 0 || oldTimeout > 5000) {
            if (logger.isLoggable(PlatformLogger.Level.FINE)) {
                logger.fine("Timeout currently set to " + oldTimeout + " temporarily setting it to 5 seconds");
            }
            this.http.setReadTimeout(5000);
            tempTimeOutSet = true;
        }
        try {
            this.http.parseHTTP(this.responses, this);
        }
        catch (SocketTimeoutException se) {
            if (logger.isLoggable(PlatformLogger.Level.FINE)) {
                logger.fine("SocketTimeoutException caught, will attempt to send body regardless");
            }
            timedOut = true;
        }
        if (!timedOut) {
            String resp = this.responses.getValue(0);
            if (resp != null && resp.startsWith("HTTP/")) {
                String[] sa = resp.split("\\s+");
                this.responseCode = -1;
                try {
                    if (sa.length > 1) {
                        this.responseCode = Integer.parseInt(sa[1]);
                    }
                    if (logger.isLoggable(PlatformLogger.Level.FINE)) {
                        logger.fine("response code received " + this.responseCode);
                    }
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }
            if (this.responseCode != 100) {
                throw new ProtocolException("Server rejected operation");
            }
        }
        if (tempTimeOutSet) {
            if (logger.isLoggable(PlatformLogger.Level.FINE)) {
                logger.fine("Restoring original timeout : " + oldTimeout);
            }
            this.http.setReadTimeout(oldTimeout);
        }
        this.http.setIgnoreContinue(true);
        if (logger.isLoggable(PlatformLogger.Level.FINE)) {
            logger.fine("Set Ignore Continue to true");
        }
        this.responseCode = -1;
        this.responses.reset();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        this.lock();
        try {
            this.connecting = true;
            SocketPermission p = this.URLtoSocketPermission(this.url);
            if (p != null) {
                try {
                    OutputStream outputStream = AccessController.doPrivilegedWithCombiner(new PrivilegedExceptionAction<OutputStream>(){

                        @Override
                        public OutputStream run() throws IOException {
                            return HttpURLConnection.this.getOutputStream0();
                        }
                    }, null, p);
                    return outputStream;
                }
                catch (PrivilegedActionException e) {
                    throw (IOException)e.getException();
                }
            }
            OutputStream outputStream = this.getOutputStream0();
            return outputStream;
        }
        finally {
            this.unlock();
        }
    }

    private OutputStream getOutputStream0() throws IOException {
        assert (this.isLockHeldByCurrentThread());
        try {
            if (!this.doOutput) {
                throw new ProtocolException("cannot write to a URLConnection if doOutput=false - call setDoOutput(true)");
            }
            if (this.method.equals("GET")) {
                this.method = "POST";
            }
            if ("TRACE".equals(this.method) && "http".equals(this.url.getProtocol())) {
                throw new ProtocolException("HTTP method TRACE doesn't support output");
            }
            if (this.inputStream != null) {
                throw new ProtocolException("Cannot write output after reading input.");
            }
            if (!this.checkReuseConnection()) {
                this.connect();
            }
            boolean expectContinue = false;
            String expects = this.requests.findValue("Expect");
            if ("100-Continue".equalsIgnoreCase(expects) && this.streaming()) {
                expectContinue = true;
            }
            if (this.streaming() && this.strOutputStream == null) {
                this.writeRequests();
            }
            if (expectContinue) {
                this.http.setIgnoreContinue(false);
                this.expect100Continue();
            }
            this.ps = (PrintStream)this.http.getOutputStream();
            if (this.streaming()) {
                if (this.strOutputStream == null) {
                    if (this.chunkLength != -1) {
                        this.strOutputStream = new StreamingOutputStream(new ChunkedOutputStream(this.ps, this.chunkLength), -1L);
                    } else {
                        long length = 0L;
                        if (this.fixedContentLengthLong != -1L) {
                            length = this.fixedContentLengthLong;
                        } else if (this.fixedContentLength != -1) {
                            length = this.fixedContentLength;
                        }
                        this.strOutputStream = new StreamingOutputStream(this.ps, length);
                    }
                }
                return this.strOutputStream;
            }
            if (this.poster == null) {
                this.poster = new PosterOutputStream();
            }
            return this.poster;
        }
        catch (ProtocolException e) {
            int i = this.responseCode;
            this.disconnectInternal();
            this.responseCode = i;
            throw e;
        }
        catch (IOException | RuntimeException e) {
            this.disconnectInternal();
            throw e;
        }
    }

    public boolean streaming() {
        return this.fixedContentLength != -1 || this.fixedContentLengthLong != -1L || this.chunkLength != -1;
    }

    private void setCookieHeader() throws IOException {
        if (this.cookieHandler != null) {
            assert (this.isLockHeldByCurrentThread());
            if (this.setUserCookies) {
                int k = this.requests.getKey("Cookie");
                if (k != -1) {
                    this.userCookies = this.requests.getValue(k);
                }
                if ((k = this.requests.getKey("Cookie2")) != -1) {
                    this.userCookies2 = this.requests.getValue(k);
                }
                this.setUserCookies = false;
            }
            this.requests.remove("Cookie");
            this.requests.remove("Cookie2");
            URI uri = ParseUtil.toURI(this.url);
            if (uri != null) {
                Map<String, List<String>> cookies;
                if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
                    logger.finest("CookieHandler request for " + uri);
                }
                if (!(cookies = this.cookieHandler.get(uri, this.requests.getHeaders(EXCLUDE_HEADERS))).isEmpty()) {
                    if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
                        logger.finest("Cookies retrieved: " + cookies.toString());
                    }
                    for (Map.Entry<String, List<String>> entry : cookies.entrySet()) {
                        List<String> l;
                        String key = entry.getKey();
                        if (!"Cookie".equalsIgnoreCase(key) && !"Cookie2".equalsIgnoreCase(key) || (l = entry.getValue()) == null || l.isEmpty()) continue;
                        this.requests.add(key, String.join((CharSequence)"; ", l));
                    }
                }
            }
            if (this.userCookies != null) {
                int k = this.requests.getKey("Cookie");
                if (k != -1) {
                    this.requests.set("Cookie", this.requests.getValue(k) + ";" + this.userCookies);
                } else {
                    this.requests.set("Cookie", this.userCookies);
                }
            }
            if (this.userCookies2 != null) {
                int k = this.requests.getKey("Cookie2");
                if (k != -1) {
                    this.requests.set("Cookie2", this.requests.getValue(k) + ";" + this.userCookies2);
                } else {
                    this.requests.set("Cookie2", this.userCookies2);
                }
            }
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        this.lock();
        try {
            this.connecting = true;
            SocketPermission p = this.URLtoSocketPermission(this.url);
            if (p != null) {
                try {
                    InputStream inputStream = AccessController.doPrivilegedWithCombiner(new PrivilegedExceptionAction<InputStream>(){

                        @Override
                        public InputStream run() throws IOException {
                            return HttpURLConnection.this.getInputStream0();
                        }
                    }, null, p);
                    return inputStream;
                }
                catch (PrivilegedActionException e) {
                    throw (IOException)e.getException();
                }
            }
            InputStream inputStream = this.getInputStream0();
            return inputStream;
        }
        finally {
            this.unlock();
        }
    }

    /*
     * Exception decompiling
     */
    private InputStream getInputStream0() throws IOException {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [14[DOLOOP]], but top level block is 6[TRYBLOCK]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    private IOException getChainedException(final IOException rememberedException) {
        try {
            final Object[] args = new Object[]{rememberedException.getMessage()};
            IOException chainedException = AccessController.doPrivileged(new PrivilegedExceptionAction<IOException>(this){

                @Override
                public IOException run() throws Exception {
                    return (IOException)rememberedException.getClass().getConstructor(String.class).newInstance(args);
                }
            });
            chainedException.initCause(rememberedException);
            return chainedException;
        }
        catch (Exception ignored) {
            return rememberedException;
        }
    }

    @Override
    public InputStream getErrorStream() {
        if (this.connected && this.responseCode >= 400) {
            if (this.errorStream != null) {
                return this.errorStream;
            }
            if (this.inputStream != null) {
                return this.inputStream;
            }
        }
        return null;
    }

    private AuthenticationInfo resetProxyAuthentication(AuthenticationInfo proxyAuthentication, AuthenticationHeader auth) throws IOException {
        assert (this.isLockHeldByCurrentThread());
        if (proxyAuthentication != null && proxyAuthentication.getAuthScheme() != AuthScheme.NTLM) {
            String raw = auth.raw();
            if (proxyAuthentication.isAuthorizationStale(raw)) {
                String value;
                if (proxyAuthentication instanceof DigestAuthentication) {
                    DigestAuthentication digestProxy = (DigestAuthentication)proxyAuthentication;
                    value = this.tunnelState() == TunnelState.SETUP ? digestProxy.getHeaderValue(HttpURLConnection.connectRequestURI(this.url), HTTP_CONNECT) : digestProxy.getHeaderValue(this.getRequestURI(), this.method);
                } else {
                    value = proxyAuthentication.getHeaderValue(this.url, this.method);
                }
                this.requests.set(proxyAuthentication.getHeaderName(), value);
                this.currentProxyCredentials = proxyAuthentication;
                return proxyAuthentication;
            }
            proxyAuthentication.removeFromCache(this.authCache);
        }
        this.currentProxyCredentials = proxyAuthentication = this.getHttpProxyAuthentication(auth);
        return proxyAuthentication;
    }

    TunnelState tunnelState() {
        return this.tunnelState;
    }

    public void setTunnelState(TunnelState tunnelState) {
        this.tunnelState = tunnelState;
    }

    public void doTunneling() throws IOException {
        this.lock();
        try {
            this.doTunneling0();
        }
        finally {
            this.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void doTunneling0() throws IOException {
        int retryTunnel = 0;
        String statusLine = "";
        int respCode = 0;
        AuthenticationInfo proxyAuthentication = null;
        String proxyHost = null;
        int proxyPort = -1;
        assert (this.isLockHeldByCurrentThread());
        MessageHeader savedRequests = this.requests;
        this.requests = new MessageHeader();
        boolean inNegotiateProxy = false;
        try {
            this.setTunnelState(TunnelState.SETUP);
            do {
                if (!this.checkReuseConnection()) {
                    this.proxiedConnect(this.url, proxyHost, proxyPort, false);
                }
                this.sendCONNECTRequest();
                this.responses.reset();
                this.http.parseHTTP(this.responses, this);
                if (logger.isLoggable(PlatformLogger.Level.FINE)) {
                    logger.fine(this.responses.toString());
                }
                if (this.responses.filterNTLMResponses("Proxy-Authenticate") && logger.isLoggable(PlatformLogger.Level.FINE)) {
                    logger.fine(">>>> Headers are filtered");
                    logger.fine(this.responses.toString());
                }
                statusLine = this.responses.getValue(0);
                StringTokenizer st = new StringTokenizer(statusLine);
                st.nextToken();
                respCode = Integer.parseInt(st.nextToken().trim());
                if (respCode == 407) {
                    boolean dontUseNegotiate = false;
                    Iterator<String> iter = this.responses.multiValueIterator("Proxy-Authenticate");
                    while (iter.hasNext()) {
                        String value = iter.next().trim();
                        if (!value.equalsIgnoreCase("Negotiate") && !value.equalsIgnoreCase("Kerberos")) continue;
                        if (!inNegotiateProxy) {
                            inNegotiateProxy = true;
                            break;
                        }
                        dontUseNegotiate = true;
                        this.doingNTLMp2ndStage = false;
                        proxyAuthentication = null;
                        break;
                    }
                    AuthenticationHeader authhdr = new AuthenticationHeader("Proxy-Authenticate", this.responses, this.getHttpCallerInfo(this.url, this.http.getProxyHostUsed(), this.http.getProxyPortUsed(), this.authenticator), dontUseNegotiate, disabledTunnelingSchemes);
                    if (!this.doingNTLMp2ndStage) {
                        if ((proxyAuthentication = this.resetProxyAuthentication(proxyAuthentication, authhdr)) != null) {
                            proxyHost = this.http.getProxyHostUsed();
                            proxyPort = this.http.getProxyPortUsed();
                            this.disconnectInternal();
                            ++retryTunnel;
                            continue;
                        }
                    } else {
                        String raw = this.responses.findValue("Proxy-Authenticate");
                        this.reset();
                        if (!proxyAuthentication.setHeaders(this, authhdr.headerParser(), raw)) {
                            this.disconnectInternal();
                            throw new IOException("Authentication failure");
                        }
                        this.authObj = null;
                        this.doingNTLMp2ndStage = false;
                        continue;
                    }
                }
                if (proxyAuthentication != null) {
                    proxyAuthentication.addToCache(this.authCache);
                }
                if (respCode == 200) {
                    this.setTunnelState(TunnelState.TUNNELING);
                    break;
                }
                this.disconnectInternal();
                this.setTunnelState(TunnelState.NONE);
                break;
            } while (retryTunnel < maxRedirects);
            if (retryTunnel >= maxRedirects || respCode != 200) {
                if (respCode != 407) {
                    this.responses.reset();
                }
                throw new IOException("Unable to tunnel through proxy. Proxy returns \"" + statusLine + "\"");
            }
        }
        finally {
            if (this.proxyAuthKey != null) {
                AuthenticationInfo.endAuthRequest(this.proxyAuthKey);
            }
            if (proxyAuthentication != null) {
                proxyAuthentication.disposeContext();
            }
        }
        this.requests = savedRequests;
        this.responses.reset();
    }

    protected HttpCallerInfo getHttpCallerInfo(URL url, String proxy, int port, Authenticator authenticator) {
        return new HttpCallerInfo(url, proxy, port, authenticator);
    }

    protected HttpCallerInfo getHttpCallerInfo(URL url, Authenticator authenticator) {
        return new HttpCallerInfo(url, authenticator);
    }

    static String connectRequestURI(URL url) {
        String host = url.getHost();
        int port = url.getPort();
        port = port != -1 ? port : url.getDefaultPort();
        return host + ":" + port;
    }

    private void sendCONNECTRequest() throws IOException {
        int port = this.url.getPort();
        this.requests.set(0, "CONNECT " + HttpURLConnection.connectRequestURI(this.url) + " " + httpVersion, null);
        this.requests.setIfNotSet("User-Agent", userAgent);
        String host = this.url.getHost();
        if (port != -1 && port != this.url.getDefaultPort()) {
            host = host + ":" + String.valueOf(port);
        }
        this.requests.setIfNotSet("Host", host);
        this.requests.setIfNotSet("Accept", acceptString);
        if (this.http.getHttpKeepAliveSet()) {
            this.requests.setIfNotSet("Proxy-Connection", "keep-alive");
        }
        this.setPreemptiveProxyAuthentication(this.requests);
        if (logger.isLoggable(PlatformLogger.Level.FINE)) {
            logger.fine(this.requests.toString());
        }
        this.http.writeRequests(this.requests, null);
    }

    private void setPreemptiveProxyAuthentication(MessageHeader requests) throws IOException {
        AuthenticationInfo pauth = AuthenticationInfo.getProxyAuth(this.http.getProxyHostUsed(), this.http.getProxyPortUsed(), this.authCache);
        if (pauth != null && pauth.supportsPreemptiveAuthorization()) {
            String value;
            if (pauth instanceof DigestAuthentication) {
                DigestAuthentication digestProxy = (DigestAuthentication)pauth;
                value = this.tunnelState() == TunnelState.SETUP ? digestProxy.getHeaderValue(HttpURLConnection.connectRequestURI(this.url), HTTP_CONNECT) : digestProxy.getHeaderValue(this.getRequestURI(), this.method);
            } else {
                value = pauth.getHeaderValue(this.url, this.method);
            }
            requests.set(pauth.getHeaderName(), value);
            this.currentProxyCredentials = pauth;
        }
    }

    private AuthenticationInfo getHttpProxyAuthentication(AuthenticationHeader authhdr) throws IOException {
        assert (this.isLockHeldByCurrentThread());
        Object ret = null;
        String raw = authhdr.raw();
        String host = this.http.getProxyHostUsed();
        int port = this.http.getProxyPortUsed();
        if (host != null && authhdr.isPresent()) {
            Object a;
            HeaderParser p = authhdr.headerParser();
            String realm = p.findValue("realm");
            String charset = p.findValue("charset");
            boolean isUTF8 = charset != null && charset.equalsIgnoreCase("UTF-8");
            String scheme = authhdr.scheme();
            AuthScheme authScheme = AuthScheme.UNKNOWN;
            if ("basic".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.BASIC;
            } else if ("digest".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.DIGEST;
            } else if ("ntlm".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.NTLM;
                this.doingNTLMp2ndStage = true;
            } else if ("Kerberos".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.KERBEROS;
                this.doingNTLMp2ndStage = true;
            } else if ("Negotiate".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.NEGOTIATE;
                this.doingNTLMp2ndStage = true;
            }
            if (realm == null) {
                realm = "";
            }
            this.proxyAuthKey = AuthenticationInfo.getProxyAuthKey(host, port, realm, authScheme);
            ret = AuthenticationInfo.getProxyAuth(this.proxyAuthKey, this.authCache);
            if (ret == null) {
                switch (authScheme) {
                    case BASIC: {
                        InetAddress addr = null;
                        try {
                            final String finalHost = host;
                            addr = AccessController.doPrivileged(new PrivilegedExceptionAction<InetAddress>(this){

                                @Override
                                public InetAddress run() throws UnknownHostException {
                                    return InetAddress.getByName(finalHost);
                                }
                            });
                        }
                        catch (PrivilegedActionException finalHost) {
                            // empty catch block
                        }
                        a = HttpURLConnection.privilegedRequestPasswordAuthentication(this.authenticator, host, addr, port, "http", realm, scheme, this.url, Authenticator.RequestorType.PROXY);
                        if (a == null) break;
                        ret = new BasicAuthentication(true, host, port, realm, (PasswordAuthentication)a, isUTF8);
                        break;
                    }
                    case DIGEST: {
                        a = HttpURLConnection.privilegedRequestPasswordAuthentication(this.authenticator, host, null, port, this.url.getProtocol(), realm, scheme, this.url, Authenticator.RequestorType.PROXY);
                        if (a == null) break;
                        DigestAuthentication.Parameters params = new DigestAuthentication.Parameters();
                        ret = new DigestAuthentication(true, host, port, realm, scheme, (PasswordAuthentication)a, params);
                        break;
                    }
                    case NTLM: {
                        if (!NTLMAuthenticationProxy.supported) break;
                        if (this.tryTransparentNTLMProxy) {
                            this.tryTransparentNTLMProxy = NTLMAuthenticationProxy.supportsTransparentAuth;
                            if (this.tryTransparentNTLMProxy && this.useProxyResponseCode) {
                                this.tryTransparentNTLMProxy = false;
                            }
                        }
                        a = null;
                        if (this.tryTransparentNTLMProxy) {
                            logger.finest("Trying Transparent NTLM authentication");
                        } else {
                            a = HttpURLConnection.privilegedRequestPasswordAuthentication(this.authenticator, host, null, port, this.url.getProtocol(), "", scheme, this.url, Authenticator.RequestorType.PROXY);
                            HttpURLConnection.validateNTLMCredentials((PasswordAuthentication)a);
                        }
                        if (this.tryTransparentNTLMProxy || !this.tryTransparentNTLMProxy && a != null) {
                            ret = NTLMAuthenticationProxy.proxy.create(true, host, port, (PasswordAuthentication)a);
                        }
                        this.tryTransparentNTLMProxy = false;
                        break;
                    }
                    case NEGOTIATE: {
                        ret = new NegotiateAuthentication(new HttpCallerInfo(authhdr.getHttpCallerInfo(), "Negotiate"));
                        break;
                    }
                    case KERBEROS: {
                        ret = new NegotiateAuthentication(new HttpCallerInfo(authhdr.getHttpCallerInfo(), "Kerberos"));
                        break;
                    }
                    case UNKNOWN: {
                        if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
                            logger.finest("Unknown/Unsupported authentication scheme: " + scheme);
                        }
                    }
                    default: {
                        throw new AssertionError((Object)"should not reach here");
                    }
                }
            }
            if (ret == null && defaultAuth != null && defaultAuth.schemeSupported(scheme)) {
                try {
                    URL u = new URL("http", host, port, "/");
                    a = defaultAuth.authString(u, scheme, realm);
                    if (a != null) {
                        ret = new BasicAuthentication(true, host, port, realm, (String)a);
                    }
                }
                catch (MalformedURLException malformedURLException) {
                    // empty catch block
                }
            }
            if (ret != null && !((AuthenticationInfo)ret).setHeaders(this, p, raw)) {
                ((AuthenticationInfo)ret).disposeContext();
                ret = null;
            }
        }
        if (logger.isLoggable(PlatformLogger.Level.FINER)) {
            logger.finer("Proxy Authentication for " + authhdr.toString() + " returned " + (ret != null ? ret.toString() : "null"));
        }
        return ret;
    }

    private AuthenticationInfo getServerAuthentication(AuthenticationHeader authhdr) throws IOException {
        assert (this.isLockHeldByCurrentThread());
        Object ret = null;
        String raw = authhdr.raw();
        if (authhdr.isPresent()) {
            Object a;
            int port;
            HeaderParser p = authhdr.headerParser();
            String realm = p.findValue("realm");
            String scheme = authhdr.scheme();
            String charset = p.findValue("charset");
            boolean isUTF8 = charset != null && charset.equalsIgnoreCase("UTF-8");
            AuthScheme authScheme = AuthScheme.UNKNOWN;
            if ("basic".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.BASIC;
            } else if ("digest".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.DIGEST;
            } else if ("ntlm".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.NTLM;
                this.doingNTLM2ndStage = true;
            } else if ("Kerberos".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.KERBEROS;
                this.doingNTLM2ndStage = true;
            } else if ("Negotiate".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.NEGOTIATE;
                this.doingNTLM2ndStage = true;
            }
            this.domain = p.findValue("domain");
            if (realm == null) {
                realm = "";
            }
            this.serverAuthKey = AuthenticationInfo.getServerAuthKey(this.url, realm, authScheme);
            ret = AuthenticationInfo.getServerAuth(this.serverAuthKey, this.authCache);
            InetAddress addr = null;
            if (ret == null) {
                try {
                    addr = InetAddress.getByName(this.url.getHost());
                }
                catch (UnknownHostException unknownHostException) {
                    // empty catch block
                }
            }
            if ((port = this.url.getPort()) == -1) {
                port = this.url.getDefaultPort();
            }
            if (ret == null) {
                switch (authScheme) {
                    case KERBEROS: {
                        ret = new NegotiateAuthentication(new HttpCallerInfo(authhdr.getHttpCallerInfo(), "Kerberos"));
                        break;
                    }
                    case NEGOTIATE: {
                        ret = new NegotiateAuthentication(new HttpCallerInfo(authhdr.getHttpCallerInfo(), "Negotiate"));
                        break;
                    }
                    case BASIC: {
                        a = HttpURLConnection.privilegedRequestPasswordAuthentication(this.authenticator, this.url.getHost(), addr, port, this.url.getProtocol(), realm, scheme, this.url, Authenticator.RequestorType.SERVER);
                        if (a == null) break;
                        ret = new BasicAuthentication(false, this.url, realm, (PasswordAuthentication)a, isUTF8);
                        break;
                    }
                    case DIGEST: {
                        a = HttpURLConnection.privilegedRequestPasswordAuthentication(this.authenticator, this.url.getHost(), addr, port, this.url.getProtocol(), realm, scheme, this.url, Authenticator.RequestorType.SERVER);
                        if (a == null) break;
                        this.digestparams = new DigestAuthentication.Parameters();
                        ret = new DigestAuthentication(false, this.url, realm, scheme, (PasswordAuthentication)a, this.digestparams);
                        break;
                    }
                    case NTLM: {
                        URL url1;
                        if (!NTLMAuthenticationProxy.supported) break;
                        try {
                            url1 = HttpURLConnection.newURL(this.url, "/");
                        }
                        catch (Exception e) {
                            url1 = this.url;
                        }
                        if (this.tryTransparentNTLMServer) {
                            this.tryTransparentNTLMServer = NTLMAuthenticationProxy.supportsTransparentAuth;
                            if (this.tryTransparentNTLMServer) {
                                this.tryTransparentNTLMServer = NTLMAuthenticationProxy.isTrustedSite(this.url);
                            }
                        }
                        a = null;
                        if (this.tryTransparentNTLMServer) {
                            logger.finest("Trying Transparent NTLM authentication");
                        } else {
                            a = HttpURLConnection.privilegedRequestPasswordAuthentication(this.authenticator, this.url.getHost(), addr, port, this.url.getProtocol(), "", scheme, this.url, Authenticator.RequestorType.SERVER);
                            HttpURLConnection.validateNTLMCredentials((PasswordAuthentication)a);
                        }
                        if (this.tryTransparentNTLMServer || !this.tryTransparentNTLMServer && a != null) {
                            ret = NTLMAuthenticationProxy.proxy.create(false, url1, (PasswordAuthentication)a);
                        }
                        this.tryTransparentNTLMServer = false;
                        break;
                    }
                    case UNKNOWN: {
                        if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
                            logger.finest("Unknown/Unsupported authentication scheme: " + scheme);
                        }
                    }
                    default: {
                        throw new AssertionError((Object)"should not reach here");
                    }
                }
            }
            if (ret == null && defaultAuth != null && defaultAuth.schemeSupported(scheme) && (a = defaultAuth.authString(this.url, scheme, realm)) != null) {
                ret = new BasicAuthentication(false, this.url, realm, (String)a);
            }
            if (ret != null && !((AuthenticationInfo)ret).setHeaders(this, p, raw)) {
                ((AuthenticationInfo)ret).disposeContext();
                ret = null;
            }
        }
        if (logger.isLoggable(PlatformLogger.Level.FINER)) {
            logger.finer("Server Authentication for " + authhdr.toString() + " returned " + (ret != null ? ret.toString() : "null"));
        }
        return ret;
    }

    private void checkResponseCredentials(boolean inClose) throws IOException {
        try {
            DigestAuthentication da;
            String raw;
            if (!this.needToCheck) {
                return;
            }
            if (validateProxy && this.currentProxyCredentials != null && this.currentProxyCredentials instanceof DigestAuthentication) {
                raw = this.responses.findValue("Proxy-Authentication-Info");
                if (inClose || raw != null) {
                    da = (DigestAuthentication)this.currentProxyCredentials;
                    da.checkResponse(raw, this.method, this.getRequestURI());
                    this.currentProxyCredentials.disposeContext();
                    this.currentProxyCredentials = null;
                }
            }
            if (validateServer && this.currentServerCredentials != null && this.currentServerCredentials instanceof DigestAuthentication) {
                raw = this.responses.findValue("Authentication-Info");
                if (inClose || raw != null) {
                    da = (DigestAuthentication)this.currentServerCredentials;
                    da.checkResponse(raw, this.method, this.url);
                    this.currentServerCredentials.disposeContext();
                    this.currentServerCredentials = null;
                }
            }
            if (this.currentServerCredentials == null && this.currentProxyCredentials == null) {
                this.needToCheck = false;
            }
        }
        catch (IOException e) {
            this.disconnectInternal();
            this.connected = false;
            throw e;
        }
    }

    String getRequestURI() throws IOException {
        if (this.requestURI == null) {
            this.requestURI = this.http.getURLFile();
        }
        return this.requestURI;
    }

    private boolean followRedirect() throws IOException {
        URL locUrl;
        if (!this.getInstanceFollowRedirects()) {
            return false;
        }
        final int stat = this.getResponseCode();
        if (stat < 300 || stat > 307 || stat == 306 || stat == 304) {
            return false;
        }
        final String loc = this.getHeaderField("Location");
        if (loc == null) {
            return false;
        }
        try {
            locUrl = HttpURLConnection.newURL(loc);
            if (!this.url.getProtocol().equalsIgnoreCase(locUrl.getProtocol())) {
                return false;
            }
        }
        catch (MalformedURLException mue) {
            locUrl = HttpURLConnection.newURL(this.url, loc);
        }
        final URL locUrl0 = locUrl;
        this.socketPermission = null;
        SocketPermission p = this.URLtoSocketPermission(locUrl);
        if (p != null) {
            try {
                return AccessController.doPrivilegedWithCombiner(new PrivilegedExceptionAction<Boolean>(this){
                    final /* synthetic */ HttpURLConnection this$0;
                    {
                        this.this$0 = this$0;
                    }

                    @Override
                    public Boolean run() throws IOException {
                        return this.this$0.followRedirect0(loc, stat, locUrl0);
                    }
                }, null, p);
            }
            catch (PrivilegedActionException e) {
                throw (IOException)e.getException();
            }
        }
        return this.followRedirect0(loc, stat, locUrl);
    }

    private boolean followRedirect0(String loc, int stat, URL locUrl) throws IOException {
        assert (this.isLockHeldByCurrentThread());
        this.disconnectInternal();
        if (this.streaming()) {
            throw new HttpRetryException(RETRY_MSG3, stat, loc);
        }
        if (logger.isLoggable(PlatformLogger.Level.FINE)) {
            logger.fine("Redirected from " + this.url + " to " + locUrl);
        }
        this.responses = new MessageHeader(maxHeaderSize);
        if (stat == 305) {
            String proxyHost = locUrl.getHost();
            int proxyPort = locUrl.getPort();
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkConnect(proxyHost, proxyPort);
            }
            this.setProxiedClient(this.url, proxyHost, proxyPort);
            this.requests.set(0, this.method + " " + this.getRequestURI() + " " + httpVersion, null);
            this.connected = true;
            this.useProxyResponseCode = true;
        } else {
            URL prevURL = this.url;
            this.url = locUrl;
            this.requestURI = null;
            if (this.method.equals("POST") && !Boolean.getBoolean("http.strictPostRedirect") && stat != 307) {
                this.requests = new MessageHeader();
                this.setRequests = false;
                super.setRequestMethod("GET");
                this.poster = null;
                if (!this.checkReuseConnection()) {
                    this.connect();
                }
                if (!HttpURLConnection.sameDestination(prevURL, this.url)) {
                    this.userCookies = null;
                    this.userCookies2 = null;
                }
            } else {
                if (!this.checkReuseConnection()) {
                    this.connect();
                }
                if (this.http != null) {
                    this.requests.set(0, this.method + " " + this.getRequestURI() + " " + httpVersion, null);
                    int port = this.url.getPort();
                    String host = HttpURLConnection.stripIPv6ZoneId(this.url.getHost());
                    if (port != -1 && port != this.url.getDefaultPort()) {
                        host = host + ":" + String.valueOf(port);
                    }
                    this.requests.set("Host", host);
                }
                if (!HttpURLConnection.sameDestination(prevURL, this.url)) {
                    this.userCookies = null;
                    this.userCookies2 = null;
                    this.requests.remove("Cookie");
                    this.requests.remove("Cookie2");
                    this.requests.remove("Authorization");
                    AuthenticationInfo sauth = AuthenticationInfo.getServerAuth(this.url, this.authCache);
                    if (sauth != null && sauth.supportsPreemptiveAuthorization()) {
                        this.requests.setIfNotSet(sauth.getHeaderName(), sauth.getHeaderValue(this.url, this.method));
                        this.currentServerCredentials = sauth;
                    }
                }
            }
        }
        return true;
    }

    private static boolean sameDestination(URL firstURL, URL secondURL) {
        int secondPort;
        assert (firstURL.getProtocol().equalsIgnoreCase(secondURL.getProtocol())) : "protocols not equal: " + firstURL + " - " + secondURL;
        if (!firstURL.getHost().equalsIgnoreCase(secondURL.getHost())) {
            return false;
        }
        int firstPort = firstURL.getPort();
        if (firstPort == -1) {
            firstPort = firstURL.getDefaultPort();
        }
        if ((secondPort = secondURL.getPort()) == -1) {
            secondPort = secondURL.getDefaultPort();
        }
        return firstPort == secondPort;
    }

    private void reset() throws IOException {
        this.http.reuse = true;
        this.reuseClient = this.http;
        InputStream is = this.http.getInputStream();
        if (!this.method.equals("HEAD") || this.tunnelState == TunnelState.SETUP) {
            block12: {
                try {
                    if (is instanceof ChunkedInputStream || is instanceof MeteredStream) {
                        while (is.read(this.cdata) > 0) {
                        }
                        break block12;
                    }
                    long cl = 0L;
                    int n = 0;
                    String cls = this.responses.findValue("Content-Length");
                    if (cls != null) {
                        try {
                            cl = Long.parseLong(cls);
                        }
                        catch (NumberFormatException e) {
                            cl = 0L;
                        }
                    }
                    for (long i = 0L; i < cl && (n = is.read(this.cdata)) != -1; i += (long)n) {
                    }
                }
                catch (IOException e) {
                    this.http.reuse = false;
                    this.reuseClient = null;
                    this.disconnectInternal();
                    return;
                }
            }
            try {
                if (is instanceof MeteredStream) {
                    is.close();
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        this.responseCode = -1;
        this.responses = new MessageHeader(maxHeaderSize);
        this.connected = false;
    }

    private void disconnectWeb() throws IOException {
        if (this.usingProxyInternal() && this.http.isKeepingAlive()) {
            this.responseCode = -1;
            this.reset();
        } else {
            this.disconnectInternal();
        }
    }

    private void disconnectInternal() {
        this.responseCode = -1;
        this.inputStream = null;
        if (this.http != null) {
            this.http.closeServer();
            this.http = null;
            this.connected = false;
        }
    }

    @Override
    public void disconnect() {
        this.responseCode = -1;
        if (this.http != null) {
            if (this.inputStream != null) {
                HttpClient hc = this.http;
                boolean ka = hc.isKeepingAlive();
                try {
                    this.inputStream.close();
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                if (ka) {
                    hc.closeIdleConnection();
                }
            } else {
                this.http.setDoNotRetry(true);
                this.http.closeServer();
            }
            this.http = null;
            this.connected = false;
        }
        this.cachedInputStream = null;
        if (this.cachedHeaders != null) {
            this.cachedHeaders.reset();
        }
    }

    boolean usingProxyInternal() {
        if (this.http != null) {
            return this.http.getProxyHostUsed() != null;
        }
        return false;
    }

    @Override
    public boolean usingProxy() {
        if (this.usingProxy || this.usingProxyInternal()) {
            return true;
        }
        if (this.instProxy != null) {
            return this.instProxy.type().equals((Object)Proxy.Type.HTTP);
        }
        return false;
    }

    private String filterHeaderField(String name, String value) {
        if (value == null) {
            return null;
        }
        if (SET_COOKIE.equalsIgnoreCase(name) || SET_COOKIE2.equalsIgnoreCase(name)) {
            if (this.cookieHandler == null || value.isEmpty()) {
                return value;
            }
            JavaNetHttpCookieAccess access = SharedSecrets.getJavaNetHttpCookieAccess();
            StringJoiner retValue = new StringJoiner(",");
            List<HttpCookie> cookies = access.parse(value);
            for (HttpCookie cookie : cookies) {
                if (cookie.isHttpOnly()) continue;
                retValue.add(access.header(cookie));
            }
            return retValue.toString();
        }
        return value;
    }

    private Map<String, List<String>> getFilteredHeaderFields() {
        if (this.filteredHeaders != null) {
            return this.filteredHeaders;
        }
        HashMap tmpMap = new HashMap();
        Map<String, List<String>> headers = this.cachedHeaders != null ? this.cachedHeaders.getHeaders() : this.responses.getHeaders();
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            String key = e.getKey();
            List<String> values = e.getValue();
            ArrayList<String> filteredVals = new ArrayList<String>();
            for (String value : values) {
                String fVal = this.filterHeaderField(key, value);
                if (fVal == null) continue;
                filteredVals.add(fVal);
            }
            if (filteredVals.isEmpty()) continue;
            tmpMap.put(key, Collections.unmodifiableList(filteredVals));
        }
        this.filteredHeaders = Collections.unmodifiableMap(tmpMap);
        return this.filteredHeaders;
    }

    @Override
    public String getHeaderField(String name) {
        try {
            this.getInputStream();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        if (this.cachedHeaders != null) {
            return this.filterHeaderField(name, this.cachedHeaders.findValue(name));
        }
        return this.filterHeaderField(name, this.responses.findValue(name));
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        try {
            this.getInputStream();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return this.getFilteredHeaderFields();
    }

    @Override
    public String getHeaderField(int n) {
        try {
            this.getInputStream();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        if (this.cachedHeaders != null) {
            return this.filterHeaderField(this.cachedHeaders.getKey(n), this.cachedHeaders.getValue(n));
        }
        return this.filterHeaderField(this.responses.getKey(n), this.responses.getValue(n));
    }

    @Override
    public String getHeaderFieldKey(int n) {
        try {
            this.getInputStream();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        if (this.cachedHeaders != null) {
            return this.cachedHeaders.getKey(n);
        }
        return this.responses.getKey(n);
    }

    @Override
    public void setRequestProperty(String key, String value) {
        this.lock();
        try {
            if (this.connected || this.connecting) {
                throw new IllegalStateException("Already connected");
            }
            if (key == null) {
                throw new NullPointerException("key is null");
            }
            if (this.isExternalMessageHeaderAllowed(key, value)) {
                this.requests.set(key, value);
                if (!key.equalsIgnoreCase("Content-Type")) {
                    this.userHeaders.set(key, value);
                }
            }
        }
        finally {
            this.unlock();
        }
    }

    MessageHeader getUserSetHeaders() {
        return this.userHeaders;
    }

    @Override
    public void addRequestProperty(String key, String value) {
        this.lock();
        try {
            if (this.connected || this.connecting) {
                throw new IllegalStateException("Already connected");
            }
            if (key == null) {
                throw new NullPointerException("key is null");
            }
            if (this.isExternalMessageHeaderAllowed(key, value)) {
                this.requests.add(key, value);
                if (!key.equalsIgnoreCase("Content-Type")) {
                    this.userHeaders.add(key, value);
                }
            }
        }
        finally {
            this.unlock();
        }
    }

    public void setAuthenticationProperty(String key, String value) {
        assert (this.isLockHeldByCurrentThread());
        this.checkMessageHeader(key, value);
        this.requests.set(key, value);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String getRequestProperty(String key) {
        this.lock();
        try {
            if (key == null) {
                String string = null;
                return string;
            }
            for (int i = 0; i < EXCLUDE_HEADERS.length; ++i) {
                if (!key.equalsIgnoreCase(EXCLUDE_HEADERS[i])) continue;
                String string = null;
                return string;
            }
            if (!this.setUserCookies) {
                if (key.equalsIgnoreCase("Cookie")) {
                    String string = this.userCookies;
                    return string;
                }
                if (key.equalsIgnoreCase("Cookie2")) {
                    String string = this.userCookies2;
                    return string;
                }
            }
            String string = this.requests.findValue(key);
            return string;
        }
        finally {
            this.unlock();
        }
    }

    @Override
    public Map<String, List<String>> getRequestProperties() {
        this.lock();
        try {
            if (this.connected) {
                throw new IllegalStateException("Already connected");
            }
            if (this.setUserCookies) {
                Map<String, List<String>> map = this.requests.getHeaders(EXCLUDE_HEADERS);
                return map;
            }
            HashMap<String, List<String>> userCookiesMap = null;
            if (this.userCookies != null || this.userCookies2 != null) {
                userCookiesMap = new HashMap<String, List<String>>();
                if (this.userCookies != null) {
                    userCookiesMap.put("Cookie", Arrays.asList(this.userCookies));
                }
                if (this.userCookies2 != null) {
                    userCookiesMap.put("Cookie2", Arrays.asList(this.userCookies2));
                }
            }
            Map<String, List<String>> map = this.requests.filterAndAddHeaders(EXCLUDE_HEADERS2, userCookiesMap);
            return map;
        }
        finally {
            this.unlock();
        }
    }

    @Override
    public void setConnectTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeouts can't be negative");
        }
        this.connectTimeout = timeout;
    }

    @Override
    public int getConnectTimeout() {
        return this.connectTimeout < 0 ? 0 : this.connectTimeout;
    }

    @Override
    public void setReadTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeouts can't be negative");
        }
        this.readTimeout = timeout;
    }

    @Override
    public int getReadTimeout() {
        return this.readTimeout < 0 ? 0 : this.readTimeout;
    }

    public CookieHandler getCookieHandler() {
        return this.cookieHandler;
    }

    String getMethod() {
        return this.method;
    }

    private MessageHeader mapToMessageHeader(Map<String, List<String>> map) {
        MessageHeader headers = new MessageHeader();
        if (map == null || map.isEmpty()) {
            return headers;
        }
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            for (String value : values) {
                if (key == null) {
                    headers.prepend(key, value);
                    continue;
                }
                headers.add(key, value);
            }
        }
        return headers;
    }

    static String stripIPv6ZoneId(String host) {
        if (host.charAt(0) != '[') {
            return host;
        }
        int i = host.lastIndexOf(37);
        if (i == -1) {
            return host;
        }
        return host.substring(0, i) + "]";
    }

    private static URL newURL(String spec) throws MalformedURLException {
        return new URL(spec);
    }

    private static URL newURL(URL context, String spec) throws MalformedURLException {
        return new URL(context, spec);
    }

    private static void validateNTLMCredentials(PasswordAuthentication pw) throws IOException {
        String username;
        if (pw == null) {
            return;
        }
        char[] password = pw.getPassword();
        if (password != null) {
            for (int i = 0; i < password.length; ++i) {
                if (password[i] != '\u0000') continue;
                throw new IOException("NUL character not allowed in NTLM password");
            }
        }
        if ((username = pw.getUserName()) != null && username.indexOf(0) != -1) {
            throw new IOException("NUL character not allowed in NTLM username or domain");
        }
    }

    static {
        enableESBuffer = false;
        timeout4ESBuffer = 0;
        bufSize4ES = 0;
        restrictedHeaders = new String[]{"Access-Control-Request-Headers", "Access-Control-Request-Method", "Connection", "Content-Length", "Content-Transfer-Encoding", "Host", "Keep-Alive", "Origin", "Trailer", "Transfer-Encoding", "Upgrade", "Via"};
        Properties props = GetPropertyAction.privilegedGetProperties();
        maxRedirects = GetIntegerAction.privilegedGetProperty("http.maxRedirects", 20);
        version = props.getProperty("java.version");
        String agent = props.getProperty("http.agent");
        agent = agent == null ? "Java/" + version : agent + " Java/" + version;
        userAgent = agent;
        String p = HttpURLConnection.getNetProperty("jdk.http.auth.tunneling.disabledSchemes");
        disabledTunnelingSchemes = HttpURLConnection.schemesListToSet(p);
        p = HttpURLConnection.getNetProperty("jdk.http.auth.proxying.disabledSchemes");
        disabledProxyingSchemes = HttpURLConnection.schemesListToSet(p);
        validateProxy = Boolean.parseBoolean(props.getProperty("http.auth.digest.validateProxy"));
        validateServer = Boolean.parseBoolean(props.getProperty("http.auth.digest.validateServer"));
        enableESBuffer = Boolean.parseBoolean(props.getProperty("sun.net.http.errorstream.enableBuffering"));
        timeout4ESBuffer = GetIntegerAction.privilegedGetProperty("sun.net.http.errorstream.timeout", 300);
        if (timeout4ESBuffer <= 0) {
            timeout4ESBuffer = 300;
        }
        if ((bufSize4ES = GetIntegerAction.privilegedGetProperty("sun.net.http.errorstream.bufferSize", 4096).intValue()) <= 0) {
            bufSize4ES = 4096;
        }
        if (!(allowRestrictedHeaders = Boolean.parseBoolean(props.getProperty("sun.net.http.allowRestrictedHeaders")))) {
            restrictedHeaderSet = HashSet.newHashSet(restrictedHeaders.length);
            for (int i = 0; i < restrictedHeaders.length; ++i) {
                restrictedHeaderSet.add(restrictedHeaders[i].toLowerCase(Locale.ROOT));
            }
        } else {
            restrictedHeaderSet = null;
        }
        int defMaxHeaderSize = 393216;
        String maxHeaderSizeStr = HttpURLConnection.getNetProperty("jdk.http.maxHeaderSize");
        int maxHeaderSizeVal = defMaxHeaderSize;
        if (maxHeaderSizeStr != null) {
            try {
                maxHeaderSizeVal = Integer.parseInt(maxHeaderSizeStr);
            }
            catch (NumberFormatException n) {
                maxHeaderSizeVal = defMaxHeaderSize;
            }
        }
        if (maxHeaderSizeVal < 0) {
            maxHeaderSizeVal = 0;
        }
        maxHeaderSize = maxHeaderSizeVal;
        EXCLUDE_HEADERS = new String[]{"Proxy-Authorization", "Authorization"};
        EXCLUDE_HEADERS2 = new String[]{"Proxy-Authorization", "Authorization", "Cookie", "Cookie2"};
        logger = PlatformLogger.getLogger("sun.net.www.protocol.http.HttpURLConnection");
    }

    public static enum TunnelState {
        NONE,
        SETUP,
        TUNNELING;

    }

    class StreamingOutputStream
    extends FilterOutputStream {
        long expected;
        long written;
        boolean closed;
        boolean error;
        IOException errorExcp;

        StreamingOutputStream(OutputStream os, long expectedLength) {
            super(os);
            this.expected = expectedLength;
            this.written = 0L;
            this.closed = false;
            this.error = false;
        }

        @Override
        public void write(int b) throws IOException {
            this.checkError();
            ++this.written;
            if (this.expected != -1L && this.written > this.expected) {
                throw new IOException("too many bytes written");
            }
            this.out.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            this.write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            this.checkError();
            this.written += (long)len;
            if (this.expected != -1L && this.written > this.expected) {
                this.out.close();
                throw new IOException("too many bytes written");
            }
            this.out.write(b, off, len);
        }

        void checkError() throws IOException {
            if (this.closed) {
                throw new IOException("Stream is closed");
            }
            if (this.error) {
                throw this.errorExcp;
            }
            if (this.out instanceof PrintStream ? ((PrintStream)this.out).checkError() : this.out instanceof ChunkedOutputStream && ((ChunkedOutputStream)this.out).checkError()) {
                throw new IOException("Error writing request body to server");
            }
        }

        boolean writtenOK() {
            return this.closed && !this.error;
        }

        @Override
        public void close() throws IOException {
            if (this.closed) {
                return;
            }
            this.closed = true;
            if (this.expected != -1L) {
                if (this.written != this.expected) {
                    this.error = true;
                    this.errorExcp = new IOException("insufficient data written");
                    this.out.close();
                    throw this.errorExcp;
                }
                super.flush();
            } else {
                super.close();
                OutputStream o = HttpURLConnection.this.http.getOutputStream();
                o.write(13);
                o.write(10);
                o.flush();
            }
        }
    }

    class HttpInputStream
    extends FilterInputStream {
        private CacheRequest cacheRequest;
        private OutputStream outputStream;
        private boolean marked;
        private int inCache;
        private int markCount;
        private boolean closed;
        private byte[] skipBuffer;
        private static final int SKIP_BUFFER_SIZE = 8096;

        public HttpInputStream(InputStream is) {
            super(is);
            this.marked = false;
            this.inCache = 0;
            this.markCount = 0;
            this.cacheRequest = null;
            this.outputStream = null;
        }

        public HttpInputStream(InputStream is, CacheRequest cacheRequest) {
            super(is);
            this.marked = false;
            this.inCache = 0;
            this.markCount = 0;
            this.cacheRequest = cacheRequest;
            try {
                this.outputStream = cacheRequest.getBody();
            }
            catch (IOException ioex) {
                this.cacheRequest.abort();
                this.cacheRequest = null;
                this.outputStream = null;
            }
        }

        @Override
        public synchronized void mark(int readlimit) {
            super.mark(readlimit);
            if (this.cacheRequest != null) {
                this.marked = true;
                this.markCount = 0;
            }
        }

        @Override
        public synchronized void reset() throws IOException {
            super.reset();
            if (this.cacheRequest != null) {
                this.marked = false;
                this.inCache += this.markCount;
            }
        }

        private void ensureOpen() throws IOException {
            if (this.closed) {
                throw new IOException("stream is closed");
            }
        }

        @Override
        public int read() throws IOException {
            this.ensureOpen();
            try {
                byte[] b = new byte[1];
                int ret = this.read(b);
                return ret == -1 ? ret : b[0] & 0xFF;
            }
            catch (IOException ioex) {
                if (this.cacheRequest != null) {
                    this.cacheRequest.abort();
                }
                throw ioex;
            }
        }

        @Override
        public int read(byte[] b) throws IOException {
            return this.read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            this.ensureOpen();
            try {
                int nWrite;
                int newLen = super.read(b, off, len);
                if (this.inCache > 0) {
                    if (this.inCache >= newLen) {
                        this.inCache -= newLen;
                        nWrite = 0;
                    } else {
                        nWrite = newLen - this.inCache;
                        this.inCache = 0;
                    }
                } else {
                    nWrite = newLen;
                }
                if (nWrite > 0 && this.outputStream != null) {
                    this.outputStream.write(b, off + (newLen - nWrite), nWrite);
                }
                if (this.marked) {
                    this.markCount += newLen;
                }
                return newLen;
            }
            catch (IOException ioex) {
                if (this.cacheRequest != null) {
                    this.cacheRequest.abort();
                }
                throw ioex;
            }
        }

        @Override
        public long skip(long n) throws IOException {
            int nr;
            this.ensureOpen();
            long remaining = n;
            if (this.skipBuffer == null) {
                this.skipBuffer = new byte[8096];
            }
            byte[] localSkipBuffer = this.skipBuffer;
            if (n <= 0L) {
                return 0L;
            }
            while (remaining > 0L && (nr = this.read(localSkipBuffer, 0, (int)Math.min(8096L, remaining))) >= 0) {
                remaining -= (long)nr;
            }
            return n - remaining;
        }

        @Override
        public void close() throws IOException {
            if (this.closed) {
                return;
            }
            try {
                if (this.outputStream != null) {
                    if (this.read() != -1) {
                        this.cacheRequest.abort();
                    } else {
                        this.outputStream.close();
                    }
                }
                super.close();
            }
            catch (IOException ioex) {
                if (this.cacheRequest != null) {
                    this.cacheRequest.abort();
                }
                throw ioex;
            }
            finally {
                this.closed = true;
                HttpURLConnection.this.http = null;
                HttpURLConnection.this.checkResponseCredentials(true);
            }
        }
    }

    static class ErrorStream
    extends InputStream {
        ByteBuffer buffer;
        InputStream is;

        private ErrorStream(ByteBuffer buf) {
            this.buffer = buf;
            this.is = null;
        }

        private ErrorStream(ByteBuffer buf, InputStream is) {
            this.buffer = buf;
            this.is = is;
        }

        public static InputStream getErrorStream(InputStream is, long cl, HttpClient http) {
            if (cl == 0L) {
                return null;
            }
            try {
                int oldTimeout = http.getReadTimeout();
                http.setReadTimeout(timeout4ESBuffer / 5);
                long expected = 0L;
                boolean isChunked = false;
                if (cl < 0L) {
                    expected = bufSize4ES;
                    isChunked = true;
                } else {
                    expected = cl;
                }
                if (expected <= (long)bufSize4ES) {
                    int exp = (int)expected;
                    byte[] buffer = new byte[exp];
                    int count = 0;
                    int time = 0;
                    int len = 0;
                    do {
                        try {
                            len = is.read(buffer, count, buffer.length - count);
                            if (len < 0) {
                                if (isChunked) break;
                                throw new IOException("the server closes before sending " + cl + " bytes of data");
                            }
                            count += len;
                        }
                        catch (SocketTimeoutException ex) {
                            time += timeout4ESBuffer / 5;
                        }
                    } while (count < exp && time < timeout4ESBuffer);
                    http.setReadTimeout(oldTimeout);
                    if (count == 0) {
                        return null;
                    }
                    if ((long)count == expected && !isChunked || isChunked && len < 0) {
                        is.close();
                        return new ErrorStream(ByteBuffer.wrap(buffer, 0, count));
                    }
                    return new ErrorStream(ByteBuffer.wrap(buffer, 0, count), is);
                }
                return null;
            }
            catch (IOException ioex) {
                return null;
            }
        }

        @Override
        public int available() throws IOException {
            if (this.is == null) {
                return this.buffer.remaining();
            }
            return this.buffer.remaining() + this.is.available();
        }

        @Override
        public int read() throws IOException {
            byte[] b = new byte[1];
            int ret = this.read(b);
            return ret == -1 ? ret : b[0] & 0xFF;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return this.read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int rem = this.buffer.remaining();
            if (rem > 0) {
                int ret = rem < len ? rem : len;
                this.buffer.get(b, off, ret);
                return ret;
            }
            if (this.is == null) {
                return -1;
            }
            return this.is.read(b, off, len);
        }

        @Override
        public void close() throws IOException {
            this.buffer = null;
            if (this.is != null) {
                this.is.close();
            }
        }
    }
}

