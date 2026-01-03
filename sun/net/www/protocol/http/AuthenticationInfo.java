/*
 * Decompiled with CFR 0.152.
 */
package sun.net.www.protocol.http;

import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.AccessController;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import sun.net.www.HeaderParser;
import sun.net.www.protocol.http.AuthCacheImpl;
import sun.net.www.protocol.http.AuthCacheValue;
import sun.net.www.protocol.http.AuthScheme;
import sun.net.www.protocol.http.HttpURLConnection;
import sun.security.action.GetBooleanAction;

public abstract class AuthenticationInfo
extends AuthCacheValue
implements Cloneable {
    public static final char SERVER_AUTHENTICATION = 's';
    public static final char PROXY_AUTHENTICATION = 'p';
    static final boolean serializeAuth = AccessController.doPrivileged(new GetBooleanAction("http.auth.serializeRequests"));
    protected PasswordAuthentication pw;
    private static final HashMap<String, Thread> requests = new HashMap();
    private static final ReentrantLock requestLock = new ReentrantLock();
    private static final Condition requestFinished = requestLock.newCondition();
    char type;
    AuthScheme authScheme;
    String protocol;
    String host;
    int port;
    String realm;
    String path;

    @Override
    public PasswordAuthentication credentials() {
        return this.pw;
    }

    @Override
    public AuthCacheValue.Type getAuthType() {
        return this.type == 's' ? AuthCacheValue.Type.Server : AuthCacheValue.Type.Proxy;
    }

    @Override
    AuthScheme getAuthScheme() {
        return this.authScheme;
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public String getRealm() {
        return this.realm;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public String getProtocolScheme() {
        return this.protocol;
    }

    protected boolean useAuthCache() {
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static AuthenticationInfo requestAuthentication(String key, AuthCacheImpl acache, BiFunction<String, AuthCacheImpl, AuthenticationInfo> cachefunc) {
        AuthenticationInfo cached = cachefunc.apply(key, acache);
        if (cached != null || !serializeAuth) {
            return cached;
        }
        requestLock.lock();
        try {
            cached = cachefunc.apply(key, acache);
            if (cached != null) {
                AuthenticationInfo authenticationInfo = cached;
                return authenticationInfo;
            }
            Thread c = Thread.currentThread();
            Thread t = requests.putIfAbsent(key, c);
            if (t == null || t == c) {
                AuthenticationInfo authenticationInfo = null;
                return authenticationInfo;
            }
            while (requests.containsKey(key)) {
                requestFinished.awaitUninterruptibly();
            }
        }
        finally {
            requestLock.unlock();
        }
        return cachefunc.apply(key, acache);
    }

    private static void requestCompleted(String key) {
        requestLock.lock();
        try {
            Thread thread = requests.get(key);
            if (thread != null && thread == Thread.currentThread()) {
                boolean waspresent;
                boolean bl = waspresent = requests.remove(key) != null;
                assert (waspresent);
            }
            requestFinished.signalAll();
        }
        finally {
            requestLock.unlock();
        }
    }

    public AuthenticationInfo(char type, AuthScheme authScheme, String host, int port, String realm) {
        this.type = type;
        this.authScheme = authScheme;
        this.protocol = "";
        this.host = host.toLowerCase(Locale.ROOT);
        this.port = port;
        this.realm = realm;
        this.path = null;
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public AuthenticationInfo(char type, AuthScheme authScheme, URL url, String realm) {
        this.type = type;
        this.authScheme = authScheme;
        this.protocol = url.getProtocol().toLowerCase(Locale.ROOT);
        this.host = url.getHost().toLowerCase(Locale.ROOT);
        this.port = url.getPort();
        if (this.port == -1) {
            this.port = url.getDefaultPort();
        }
        this.realm = realm;
        String urlPath = url.getPath();
        this.path = urlPath.isEmpty() ? urlPath : AuthenticationInfo.reducePath(urlPath);
    }

    static String reducePath(String urlPath) {
        int sepIndex = urlPath.lastIndexOf(47);
        int targetSuffixIndex = urlPath.lastIndexOf(46);
        if (sepIndex != -1) {
            if (sepIndex < targetSuffixIndex) {
                return urlPath.substring(0, sepIndex + 1);
            }
            return urlPath;
        }
        return urlPath;
    }

    static AuthenticationInfo getServerAuth(URL url, AuthCacheImpl cache) {
        int port = url.getPort();
        if (port == -1) {
            port = url.getDefaultPort();
        }
        String key = "s:" + url.getProtocol().toLowerCase(Locale.ROOT) + ":" + url.getHost().toLowerCase(Locale.ROOT) + ":" + port;
        return AuthenticationInfo.getAuth(key, url, cache);
    }

    static String getServerAuthKey(URL url, String realm, AuthScheme scheme) {
        int port = url.getPort();
        if (port == -1) {
            port = url.getDefaultPort();
        }
        String key = "s:" + (Object)((Object)scheme) + ":" + url.getProtocol().toLowerCase(Locale.ROOT) + ":" + url.getHost().toLowerCase(Locale.ROOT) + ":" + port + ":" + realm;
        return key;
    }

    private static AuthenticationInfo getCachedServerAuth(String key, AuthCacheImpl cache) {
        return AuthenticationInfo.getAuth(key, null, cache);
    }

    static AuthenticationInfo getServerAuth(String key, AuthCacheImpl cache) {
        if (!serializeAuth) {
            return AuthenticationInfo.getCachedServerAuth(key, cache);
        }
        return AuthenticationInfo.requestAuthentication(key, cache, AuthenticationInfo::getCachedServerAuth);
    }

    static AuthenticationInfo getAuth(String key, URL url, AuthCacheImpl acache) {
        Objects.requireNonNull(acache);
        if (url == null) {
            return (AuthenticationInfo)acache.get(key, null);
        }
        return (AuthenticationInfo)acache.get(key, url.getPath());
    }

    static AuthenticationInfo getProxyAuth(String host, int port, AuthCacheImpl acache) {
        Objects.requireNonNull(acache);
        String key = "p::" + host.toLowerCase(Locale.ROOT) + ":" + port;
        AuthenticationInfo result = (AuthenticationInfo)acache.get(key, null);
        return result;
    }

    static String getProxyAuthKey(String host, int port, String realm, AuthScheme scheme) {
        String key = "p:" + (Object)((Object)scheme) + "::" + host.toLowerCase(Locale.ROOT) + ":" + port + ":" + realm;
        return key;
    }

    private static AuthenticationInfo getCachedProxyAuth(String key, AuthCacheImpl acache) {
        Objects.requireNonNull(acache);
        return (AuthenticationInfo)acache.get(key, null);
    }

    static AuthenticationInfo getProxyAuth(String key, AuthCacheImpl acache) {
        if (!serializeAuth) {
            return AuthenticationInfo.getCachedProxyAuth(key, acache);
        }
        return AuthenticationInfo.requestAuthentication(key, acache, AuthenticationInfo::getCachedProxyAuth);
    }

    void addToCache(AuthCacheImpl authcache) {
        Objects.requireNonNull(authcache);
        String key = this.cacheKey(true);
        if (this.useAuthCache()) {
            authcache.put(key, this);
            if (this.supportsPreemptiveAuthorization()) {
                authcache.put(this.cacheKey(false), this);
            }
        }
        AuthenticationInfo.endAuthRequest(key);
    }

    static void endAuthRequest(String key) {
        if (!serializeAuth) {
            return;
        }
        AuthenticationInfo.requestCompleted(key);
    }

    void removeFromCache(AuthCacheImpl authcache) {
        Objects.requireNonNull(authcache);
        authcache.remove(this.cacheKey(true), this);
        if (this.supportsPreemptiveAuthorization()) {
            authcache.remove(this.cacheKey(false), this);
        }
    }

    public abstract boolean supportsPreemptiveAuthorization();

    public String getHeaderName() {
        if (this.type == 's') {
            return "Authorization";
        }
        return "Proxy-authorization";
    }

    public abstract String getHeaderValue(URL var1, String var2);

    public abstract boolean setHeaders(HttpURLConnection var1, HeaderParser var2, String var3);

    public abstract boolean isAuthorizationStale(String var1);

    String cacheKey(boolean includeRealm) {
        if (includeRealm) {
            return this.type + ":" + (Object)((Object)this.authScheme) + ":" + this.protocol + ":" + this.host + ":" + this.port + ":" + this.realm;
        }
        return this.type + ":" + this.protocol + ":" + this.host + ":" + this.port;
    }

    public void disposeContext() {
    }
}

