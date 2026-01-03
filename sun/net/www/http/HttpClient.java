/*
 * Decompiled with CFR 0.152.
 */
package sun.net.www.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.CacheRequest;
import java.net.CookieHandler;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;
import sun.net.NetworkClient;
import sun.net.util.ProxyUtil;
import sun.net.www.HeaderParser;
import sun.net.www.MessageHeader;
import sun.net.www.MeteredStream;
import sun.net.www.ParseUtil;
import sun.net.www.URLConnection;
import sun.net.www.http.ChunkedInputStream;
import sun.net.www.http.HttpCapture;
import sun.net.www.http.HttpCaptureInputStream;
import sun.net.www.http.HttpCaptureOutputStream;
import sun.net.www.http.KeepAliveCache;
import sun.net.www.http.KeepAliveStream;
import sun.net.www.http.PosterOutputStream;
import sun.net.www.protocol.http.AuthCacheImpl;
import sun.net.www.protocol.http.HttpURLConnection;
import sun.security.action.GetPropertyAction;
import sun.util.logging.PlatformLogger;

public class HttpClient
extends NetworkClient {
    private final ReentrantLock clientLock = new ReentrantLock();
    protected boolean cachedHttpClient = false;
    protected boolean inCache;
    MessageHeader requests;
    PosterOutputStream poster = null;
    boolean streaming;
    boolean failedOnce = false;
    private boolean ignoreContinue = true;
    private static final int HTTP_CONTINUE = 100;
    static final int httpPortNumber = 80;
    protected boolean proxyDisabled;
    public boolean usingProxy = false;
    protected String host;
    protected int port;
    protected static KeepAliveCache kac = new KeepAliveCache();
    private static boolean keepAliveProp = true;
    private static boolean retryPostProp = true;
    private static final boolean cacheNTLMProp;
    private static final boolean cacheSPNEGOProp;
    protected volatile AuthCacheImpl authcache;
    volatile boolean keepingAlive;
    volatile boolean disableKeepAlive;
    int keepAliveConnections = -1;
    int keepAliveTimeout = 0;
    private CacheRequest cacheRequest = null;
    protected URL url;
    public boolean reuse = false;
    private HttpCapture capture = null;
    private static final String spnegoCBT;
    private static final PlatformLogger logger;

    protected int getDefaultPort() {
        return 80;
    }

    private static int getDefaultPort(String proto) {
        if ("http".equalsIgnoreCase(proto)) {
            return 80;
        }
        if ("https".equalsIgnoreCase(proto)) {
            return 443;
        }
        return -1;
    }

    private static void logFinest(String msg) {
        if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
            logger.finest(msg);
        }
    }

    private static void logError(String msg) {
        if (logger.isLoggable(PlatformLogger.Level.SEVERE)) {
            logger.severe(msg);
        }
    }

    @Deprecated
    public static synchronized void resetProperties() {
    }

    int getKeepAliveTimeout() {
        return this.keepAliveTimeout;
    }

    static String normalizeCBT(String s) {
        if (s == null || s.equals("never")) {
            return "never";
        }
        if (s.equals("always") || s.startsWith("domain:")) {
            return s;
        }
        HttpClient.logError("Unexpected value for \"jdk.https.negotiate.cbt\" system property");
        return "never";
    }

    public boolean getHttpKeepAliveSet() {
        return keepAliveProp;
    }

    public String getSpnegoCBT() {
        return spnegoCBT;
    }

    protected HttpClient() {
    }

    private HttpClient(URL url) throws IOException {
        this(url, null, -1, false);
    }

    protected HttpClient(URL url, boolean proxyDisabled) throws IOException {
        this(url, null, -1, proxyDisabled);
    }

    public HttpClient(URL url, String proxyHost, int proxyPort) throws IOException {
        this(url, proxyHost, proxyPort, false);
    }

    protected HttpClient(URL url, Proxy p, int to) throws IOException {
        this.proxy = p == null ? Proxy.NO_PROXY : ProxyUtil.copyProxy(p);
        this.host = url.getHost();
        this.url = url;
        this.port = url.getPort();
        if (this.port == -1) {
            this.port = this.getDefaultPort();
        }
        this.setConnectTimeout(to);
        this.capture = HttpCapture.getCapture(url);
        this.openServer();
    }

    protected static Proxy newHttpProxy(String proxyHost, int proxyPort, String proto) {
        if (proxyHost == null || proto == null) {
            return Proxy.NO_PROXY;
        }
        int pport = proxyPort < 0 ? HttpClient.getDefaultPort(proto) : proxyPort;
        InetSocketAddress saddr = InetSocketAddress.createUnresolved(proxyHost, pport);
        return new Proxy(Proxy.Type.HTTP, saddr);
    }

    private HttpClient(URL url, String proxyHost, int proxyPort, boolean proxyDisabled) throws IOException {
        this(url, proxyDisabled ? Proxy.NO_PROXY : HttpClient.newHttpProxy(proxyHost, proxyPort, "http"), -1);
    }

    public HttpClient(URL url, String proxyHost, int proxyPort, boolean proxyDisabled, int to) throws IOException {
        this(url, proxyDisabled ? Proxy.NO_PROXY : HttpClient.newHttpProxy(proxyHost, proxyPort, "http"), to);
    }

    public static HttpClient New(URL url) throws IOException {
        return HttpClient.New(url, Proxy.NO_PROXY, -1, true, null);
    }

    public static HttpClient New(URL url, boolean useCache) throws IOException {
        return HttpClient.New(url, Proxy.NO_PROXY, -1, useCache, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static HttpClient New(URL url, Proxy p, int to, boolean useCache, HttpURLConnection httpuc) throws IOException {
        p = p == null ? Proxy.NO_PROXY : ProxyUtil.copyProxy(p);
        HttpClient ret = null;
        if (useCache) {
            ret = kac.get(url, null);
            if (ret != null && httpuc != null && httpuc.streaming() && "POST".equals(httpuc.getRequestMethod()) && !ret.available()) {
                ret.inCache = false;
                ret.closeServer();
                ret = null;
            }
            if (ret != null) {
                boolean compatible;
                AuthCacheImpl ak = httpuc == null ? AuthCacheImpl.getDefault() : httpuc.getAuthCache();
                boolean bl = compatible = Objects.equals(ret.proxy, p) && Objects.equals(ret.getAuthCache(), ak);
                if (compatible) {
                    ret.lock();
                    try {
                        ret.cachedHttpClient = true;
                        assert (ret.inCache);
                        ret.inCache = false;
                        if (httpuc != null && ret.needsTunneling()) {
                            httpuc.setTunnelState(HttpURLConnection.TunnelState.TUNNELING);
                        }
                        HttpClient.logFinest("KeepAlive stream retrieved from the cache, " + ret);
                    }
                    finally {
                        ret.unlock();
                    }
                }
                ret.lock();
                try {
                    ret.inCache = false;
                    ret.closeServer();
                }
                finally {
                    ret.unlock();
                }
                ret = null;
            }
        }
        if (ret == null) {
            ret = new HttpClient(url, p, to);
            if (httpuc != null) {
                ret.authcache = httpuc.getAuthCache();
            }
        } else {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                if (ret.proxy == Proxy.NO_PROXY || ret.proxy == null) {
                    security.checkConnect(InetAddress.getByName(url.getHost()).getHostAddress(), url.getPort());
                } else {
                    security.checkConnect(url.getHost(), url.getPort());
                }
            }
            ret.url = url;
        }
        return ret;
    }

    public static HttpClient New(URL url, Proxy p, int to, HttpURLConnection httpuc) throws IOException {
        return HttpClient.New(url, p, to, true, httpuc);
    }

    public static HttpClient New(URL url, String proxyHost, int proxyPort, boolean useCache) throws IOException {
        return HttpClient.New(url, HttpClient.newHttpProxy(proxyHost, proxyPort, "http"), -1, useCache, null);
    }

    public static HttpClient New(URL url, String proxyHost, int proxyPort, boolean useCache, int to, HttpURLConnection httpuc) throws IOException {
        return HttpClient.New(url, HttpClient.newHttpProxy(proxyHost, proxyPort, "http"), to, useCache, httpuc);
    }

    public final AuthCacheImpl getAuthCache() {
        return this.authcache == null ? AuthCacheImpl.getDefault() : this.authcache;
    }

    public void finished() {
        if (this.reuse) {
            return;
        }
        --this.keepAliveConnections;
        this.poster = null;
        if (this.keepAliveConnections > 0 && this.isKeepingAlive() && !this.serverOutput.checkError()) {
            this.putInKeepAliveCache();
        } else {
            this.closeServer();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected boolean available() {
        boolean available = true;
        int old = -1;
        this.lock();
        try {
            try {
                old = this.serverSocket.getSoTimeout();
                this.serverSocket.setSoTimeout(1);
                BufferedInputStream tmpbuf = new BufferedInputStream(this.serverSocket.getInputStream());
                int r = tmpbuf.read();
                if (r == -1) {
                    HttpClient.logFinest("HttpClient.available(): read returned -1: not available");
                    available = false;
                }
            }
            catch (SocketTimeoutException e) {
                HttpClient.logFinest("HttpClient.available(): SocketTimeout: its available");
            }
            finally {
                if (old != -1) {
                    this.serverSocket.setSoTimeout(old);
                }
            }
        }
        catch (IOException e) {
            HttpClient.logFinest("HttpClient.available(): SocketException: not available");
            available = false;
        }
        finally {
            this.unlock();
        }
        return available;
    }

    protected void putInKeepAliveCache() {
        this.lock();
        try {
            if (this.inCache) {
                assert (false) : "Duplicate put to keep alive cache";
                return;
            }
            this.inCache = true;
            kac.put(this.url, null, this);
        }
        finally {
            this.unlock();
        }
    }

    protected boolean isInKeepAliveCache() {
        this.lock();
        try {
            boolean bl = this.inCache;
            return bl;
        }
        finally {
            this.unlock();
        }
    }

    public void closeIdleConnection() {
        HttpClient http = kac.get(this.url, null);
        if (http != null) {
            http.closeServer();
        }
    }

    @Override
    public void openServer(String server, int port) throws IOException {
        this.serverSocket = this.doConnect(server, port);
        try {
            OutputStream out = this.serverSocket.getOutputStream();
            if (this.capture != null) {
                out = new HttpCaptureOutputStream(out, this.capture);
            }
            this.serverOutput = new PrintStream((OutputStream)new BufferedOutputStream(out), false, encoding);
        }
        catch (UnsupportedEncodingException e) {
            throw new InternalError(encoding + " encoding not found", e);
        }
        this.serverSocket.setTcpNoDelay(true);
    }

    public boolean needsTunneling() {
        return false;
    }

    public boolean isCachedConnection() {
        this.lock();
        try {
            boolean bl = this.cachedHttpClient;
            return bl;
        }
        finally {
            this.unlock();
        }
    }

    public void afterConnect() throws IOException, UnknownHostException {
    }

    private void privilegedOpenServer(final InetSocketAddress server) throws IOException {
        assert (this.clientLock.isHeldByCurrentThread());
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>(this){
                final /* synthetic */ HttpClient this$0;
                {
                    this.this$0 = this$0;
                }

                @Override
                public Void run() throws IOException {
                    this.this$0.openServer(server.getHostString(), server.getPort());
                    return null;
                }
            });
        }
        catch (PrivilegedActionException pae) {
            throw (IOException)pae.getException();
        }
    }

    private void superOpenServer(String proxyHost, int proxyPort) throws IOException, UnknownHostException {
        super.openServer(proxyHost, proxyPort);
    }

    protected void openServer() throws IOException {
        SecurityManager security = System.getSecurityManager();
        this.lock();
        try {
            if (security != null) {
                security.checkConnect(this.host, this.port);
            }
            if (this.keepingAlive) {
                return;
            }
            if (this.url.getProtocol().equals("http") || this.url.getProtocol().equals("https")) {
                if (this.proxy != null && this.proxy.type() == Proxy.Type.HTTP) {
                    URLConnection.setProxiedHost(this.host);
                    this.privilegedOpenServer((InetSocketAddress)this.proxy.address());
                    this.usingProxy = true;
                    return;
                }
                this.openServer(this.host, this.port);
                this.usingProxy = false;
                return;
            }
            if (this.proxy != null && this.proxy.type() == Proxy.Type.HTTP) {
                URLConnection.setProxiedHost(this.host);
                this.privilegedOpenServer((InetSocketAddress)this.proxy.address());
                this.usingProxy = true;
                return;
            }
            super.openServer(this.host, this.port);
            this.usingProxy = false;
            return;
        }
        finally {
            this.unlock();
        }
    }

    public String getURLFile() throws IOException {
        String fileName;
        if (this.usingProxy && !this.proxyDisabled) {
            StringBuilder result = new StringBuilder(128);
            result.append(this.url.getProtocol());
            result.append(":");
            if (this.url.getAuthority() != null && !this.url.getAuthority().isEmpty()) {
                result.append("//");
                result.append(this.url.getAuthority());
            }
            if (this.url.getPath() != null) {
                result.append(this.url.getPath());
            }
            if (this.url.getQuery() != null) {
                result.append('?');
                result.append(this.url.getQuery());
            }
            fileName = result.toString();
        } else {
            fileName = this.url.getFile();
            if (fileName == null || fileName.isEmpty()) {
                fileName = "/";
            } else if (fileName.charAt(0) == '?') {
                fileName = "/" + fileName;
            }
        }
        if (fileName.indexOf(10) == -1) {
            return fileName;
        }
        throw new MalformedURLException("Illegal character in URL");
    }

    @Deprecated
    public void writeRequests(MessageHeader head) {
        this.requests = head;
        this.requests.print(this.serverOutput);
        this.serverOutput.flush();
    }

    public void writeRequests(MessageHeader head, PosterOutputStream pos) throws IOException {
        this.requests = head;
        this.requests.print(this.serverOutput);
        this.poster = pos;
        if (this.poster != null) {
            this.poster.writeTo(this.serverOutput);
        }
        this.serverOutput.flush();
    }

    public void writeRequests(MessageHeader head, PosterOutputStream pos, boolean streaming) throws IOException {
        this.streaming = streaming;
        this.writeRequests(head, pos);
    }

    public boolean parseHTTP(MessageHeader responses, HttpURLConnection httpuc) throws IOException {
        try {
            this.serverInput = this.serverSocket.getInputStream();
            if (this.capture != null) {
                this.serverInput = new HttpCaptureInputStream(this.serverInput, this.capture);
            }
            this.serverInput = new BufferedInputStream(this.serverInput);
            return this.parseHTTPHeader(responses, httpuc);
        }
        catch (SocketTimeoutException stex) {
            if (this.ignoreContinue) {
                this.closeServer();
            }
            throw stex;
        }
        catch (IOException e) {
            this.closeServer();
            this.cachedHttpClient = false;
            if (!this.failedOnce && this.requests != null) {
                Thread thread = Thread.currentThread();
                boolean doNotRetry = thread.isVirtual() && thread.isInterrupted();
                this.failedOnce = true;
                if (!(this.getRequestMethod().equals("CONNECT") || this.streaming || doNotRetry || httpuc.getRequestMethod().equals("POST") && !retryPostProp)) {
                    this.openServer();
                    this.checkTunneling(httpuc);
                    this.afterConnect();
                    this.writeRequests(this.requests, this.poster);
                    return this.parseHTTP(responses, httpuc);
                }
            }
            throw e;
        }
    }

    private void checkTunneling(HttpURLConnection httpuc) throws IOException {
        if (this.needsTunneling()) {
            MessageHeader origRequests = this.requests;
            PosterOutputStream origPoster = this.poster;
            httpuc.doTunneling();
            this.requests = origRequests;
            this.poster = origPoster;
        }
    }

    private boolean parseHTTPHeader(MessageHeader responses, HttpURLConnection httpuc) throws IOException {
        int nread;
        int r;
        this.keepAliveConnections = -1;
        this.keepAliveTimeout = 0;
        boolean ret = false;
        byte[] b = new byte[8];
        this.serverInput.mark(10);
        for (nread = 0; nread < 8 && (r = this.serverInput.read(b, nread, 8 - nread)) >= 0; nread += r) {
        }
        String keep = null;
        String authenticate = null;
        ret = b[0] == 72 && b[1] == 84 && b[2] == 84 && b[3] == 80 && b[4] == 47 && b[5] == 49 && b[6] == 46;
        this.serverInput.reset();
        if (ret) {
            boolean canKeepAlive;
            URI uri;
            responses.parseHeader(this.serverInput);
            CookieHandler cookieHandler = httpuc.getCookieHandler();
            if (cookieHandler != null && (uri = ParseUtil.toURI(this.url)) != null) {
                cookieHandler.put(uri, responses.getHeaders());
            }
            if (this.usingProxy) {
                keep = responses.findValue("Proxy-Connection");
                authenticate = responses.findValue("Proxy-Authenticate");
            }
            if (keep == null) {
                keep = responses.findValue("Connection");
                authenticate = responses.findValue("WWW-Authenticate");
            }
            boolean bl = canKeepAlive = !this.disableKeepAlive;
            if (!(!canKeepAlive || cacheNTLMProp && cacheSPNEGOProp || authenticate == null)) {
                authenticate = authenticate.toLowerCase(Locale.US);
                if (!cacheNTLMProp) {
                    canKeepAlive &= !authenticate.startsWith("ntlm ");
                }
                if (!cacheSPNEGOProp) {
                    canKeepAlive &= !authenticate.startsWith("negotiate ");
                    canKeepAlive &= !authenticate.startsWith("kerberos ");
                }
            }
            this.disableKeepAlive |= !canKeepAlive;
            if (keep != null && keep.toLowerCase(Locale.US).equals("keep-alive")) {
                if (this.disableKeepAlive) {
                    this.keepAliveConnections = 1;
                } else {
                    OptionalInt timeout;
                    HeaderParser p = new HeaderParser(responses.findValue("Keep-Alive"));
                    this.keepAliveConnections = p.findInt("max", this.usingProxy ? 50 : 5);
                    if (this.keepAliveConnections < 0) {
                        int n = this.keepAliveConnections = this.usingProxy ? 50 : 5;
                    }
                    if ((timeout = p.findInt("timeout")).isEmpty()) {
                        this.keepAliveTimeout = -1;
                    } else {
                        this.keepAliveTimeout = timeout.getAsInt();
                        if (this.keepAliveTimeout < 0) {
                            this.keepAliveTimeout = -1;
                        } else if (this.keepAliveTimeout == 0) {
                            this.keepAliveTimeout = -2;
                        }
                    }
                }
            } else if (b[7] != 48) {
                this.keepAliveConnections = keep != null || this.disableKeepAlive ? 1 : 5;
            }
        } else {
            if (nread != 8) {
                if (!this.failedOnce && this.requests != null) {
                    this.failedOnce = true;
                    if (!(this.getRequestMethod().equals("CONNECT") || this.streaming || httpuc.getRequestMethod().equals("POST") && !retryPostProp)) {
                        this.closeServer();
                        this.cachedHttpClient = false;
                        this.openServer();
                        this.checkTunneling(httpuc);
                        this.afterConnect();
                        this.writeRequests(this.requests, this.poster);
                        return this.parseHTTP(responses, httpuc);
                    }
                }
                throw new SocketException("Unexpected end of file from server");
            }
            responses.set("Content-type", "unknown/unknown");
        }
        int code = -1;
        try {
            String resp = responses.getValue(0);
            int ind = resp.indexOf(32);
            while (resp.charAt(ind) == ' ') {
                ++ind;
            }
            code = Integer.parseInt(resp, ind, ind + 3, 10);
        }
        catch (Exception resp) {
            // empty catch block
        }
        if (code == 101) {
            this.closeServer();
            HttpClient.logFinest("Closed connection due to unexpected 101 response");
            responses.reset();
            throw new ProtocolException("Unexpected 101 response from server");
        }
        if (code == 100 && this.ignoreContinue || code >= 102 && code <= 199) {
            HttpClient.logFinest("Ignoring interim informational 1xx response: " + code);
            responses.reset();
            return this.parseHTTPHeader(responses, httpuc);
        }
        long cl = -1L;
        String te = responses.findValue("Transfer-Encoding");
        if (te != null && te.equalsIgnoreCase("chunked")) {
            this.serverInput = new ChunkedInputStream(this.serverInput, this, responses);
            if (this.keepAliveConnections <= 1) {
                this.keepAliveConnections = 1;
                this.keepingAlive = false;
            } else {
                this.keepingAlive = !this.disableKeepAlive;
            }
            this.failedOnce = false;
        } else {
            String requestLine;
            String cls = responses.findValue("content-length");
            if (cls != null) {
                try {
                    cl = Long.parseLong(cls);
                }
                catch (NumberFormatException e) {
                    cl = -1L;
                }
            }
            if ((requestLine = this.requests.getKey(0)) != null && requestLine.startsWith("HEAD") || code == 304 || code == 204) {
                cl = 0L;
            }
            if (this.keepAliveConnections > 1 && (cl >= 0L || code == 304 || code == 204)) {
                this.keepingAlive = !this.disableKeepAlive;
                this.failedOnce = false;
            } else if (this.keepingAlive) {
                this.keepingAlive = false;
            }
        }
        if (cl > 0L) {
            boolean useKeepAliveStream;
            boolean bl = useKeepAliveStream = this.isKeepingAlive() || this.disableKeepAlive;
            if (useKeepAliveStream) {
                HttpClient.logFinest("KeepAlive stream used: " + this.url);
                this.serverInput = new KeepAliveStream(this.serverInput, cl, this);
                this.failedOnce = false;
            } else {
                this.serverInput = new MeteredStream(this.serverInput, cl);
            }
        }
        return ret;
    }

    public InputStream getInputStream() {
        this.lock();
        try {
            InputStream inputStream = this.serverInput;
            return inputStream;
        }
        finally {
            this.unlock();
        }
    }

    public OutputStream getOutputStream() {
        return this.serverOutput;
    }

    public String toString() {
        return this.getClass().getName() + "(" + this.url + ")";
    }

    public final boolean isKeepingAlive() {
        return this.getHttpKeepAliveSet() && this.keepingAlive;
    }

    public void setCacheRequest(CacheRequest cacheRequest) {
        this.cacheRequest = cacheRequest;
    }

    CacheRequest getCacheRequest() {
        return this.cacheRequest;
    }

    String getRequestMethod() {
        String requestLine;
        if (this.requests != null && (requestLine = this.requests.getKey(0)) != null) {
            return requestLine.split("\\s+")[0];
        }
        return "";
    }

    public void setDoNotRetry(boolean value) {
        this.failedOnce = value;
    }

    public void setIgnoreContinue(boolean value) {
        this.ignoreContinue = value;
    }

    @Override
    public void closeServer() {
        try {
            this.keepingAlive = false;
            this.serverSocket.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public String getProxyHostUsed() {
        if (!this.usingProxy) {
            return null;
        }
        return ((InetSocketAddress)this.proxy.address()).getHostString();
    }

    public boolean getUsingProxy() {
        return this.usingProxy;
    }

    public int getProxyPortUsed() {
        if (this.usingProxy) {
            return ((InetSocketAddress)this.proxy.address()).getPort();
        }
        return -1;
    }

    public final void lock() {
        this.clientLock.lock();
    }

    public final void unlock() {
        this.clientLock.unlock();
    }

    static {
        logger = HttpURLConnection.getHttpLogger();
        Properties props = GetPropertyAction.privilegedGetProperties();
        String keepAlive = props.getProperty("http.keepAlive");
        String retryPost = props.getProperty("sun.net.http.retryPost");
        String cacheNTLM = props.getProperty("jdk.ntlm.cache");
        String cacheSPNEGO = props.getProperty("jdk.spnego.cache");
        String s = props.getProperty("jdk.https.negotiate.cbt");
        spnegoCBT = HttpClient.normalizeCBT(s);
        keepAliveProp = keepAlive != null ? Boolean.parseBoolean(keepAlive) : true;
        retryPostProp = retryPost != null ? Boolean.parseBoolean(retryPost) : true;
        cacheNTLMProp = cacheNTLM != null ? Boolean.parseBoolean(cacheNTLM) : true;
        cacheSPNEGOProp = cacheSPNEGO != null ? Boolean.parseBoolean(cacheSPNEGO) : true;
    }
}

