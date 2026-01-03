/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import sun.security.action.GetIntegerAction;
import sun.security.action.GetPropertyAction;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLSessionImpl;
import sun.security.ssl.SessionId;
import sun.security.ssl.SessionTicketExtension;
import sun.security.util.Cache;

final class SSLSessionContextImpl
implements SSLSessionContext {
    private static final int DEFAULT_MAX_CACHE_SIZE = 20480;
    static final int DEFAULT_SESSION_TIMEOUT = 86400;
    private final Cache<SessionId, SSLSessionImpl> sessionCache;
    private final Cache<String, SSLSessionImpl> sessionHostPortCache;
    private int cacheLimit;
    private int timeout = 86400;
    private int currentKeyID;
    private final Map<Integer, SessionTicketExtension.StatelessKey> keyHashMap;
    private boolean statelessSession = true;

    SSLSessionContextImpl(boolean server) {
        this.cacheLimit = this.getDefaults(server);
        this.sessionCache = Cache.newSoftMemoryCache(this.cacheLimit, this.timeout);
        this.sessionHostPortCache = Cache.newSoftMemoryCache(this.cacheLimit, this.timeout);
        if (server) {
            this.keyHashMap = new ConcurrentHashMap<Integer, SessionTicketExtension.StatelessKey>();
            this.currentKeyID = new Random(System.nanoTime()).nextInt();
        } else {
            this.keyHashMap = Map.of();
        }
    }

    boolean statelessEnabled() {
        return this.statelessSession;
    }

    @Override
    public SSLSession getSession(byte[] sessionId) {
        if (sessionId == null) {
            throw new NullPointerException("session id cannot be null");
        }
        SSLSessionImpl sess = this.sessionCache.get(new SessionId(sessionId));
        if (!this.isTimedout(sess)) {
            return sess;
        }
        return null;
    }

    @Override
    public Enumeration<byte[]> getIds() {
        SessionCacheVisitor scVisitor = new SessionCacheVisitor();
        this.sessionCache.accept(scVisitor);
        return scVisitor.getSessionIds();
    }

    @Override
    public void setSessionTimeout(int seconds) throws IllegalArgumentException {
        if (seconds < 0) {
            throw new IllegalArgumentException();
        }
        if (this.timeout != seconds) {
            this.sessionCache.setTimeout(seconds);
            this.sessionHostPortCache.setTimeout(seconds);
            this.timeout = seconds;
        }
    }

    @Override
    public int getSessionTimeout() {
        return this.timeout;
    }

    @Override
    public void setSessionCacheSize(int size) throws IllegalArgumentException {
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        if (this.cacheLimit != size) {
            this.sessionCache.setCapacity(size);
            this.sessionHostPortCache.setCapacity(size);
            this.cacheLimit = size;
        }
    }

    @Override
    public int getSessionCacheSize() {
        return this.cacheLimit;
    }

    private void cleanupStatelessKeys() {
        Iterator<Map.Entry<Integer, SessionTicketExtension.StatelessKey>> it = this.keyHashMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, SessionTicketExtension.StatelessKey> entry = it.next();
            SessionTicketExtension.StatelessKey k = entry.getValue();
            if (!k.isInvalid(this)) continue;
            it.remove();
            try {
                k.key.destroy();
            }
            catch (Exception exception) {}
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    SessionTicketExtension.StatelessKey getKey(HandshakeContext hc) {
        SessionTicketExtension.StatelessKey ssk = this.keyHashMap.get(this.currentKeyID);
        if (ssk != null && !ssk.isExpired()) {
            return ssk;
        }
        SSLSessionContextImpl sSLSessionContextImpl = this;
        synchronized (sSLSessionContextImpl) {
            ssk = this.keyHashMap.get(this.currentKeyID);
            if (ssk != null && !ssk.isExpired()) {
                return ssk;
            }
            int newID = this.currentKeyID + 1;
            ssk = new SessionTicketExtension.StatelessKey(hc, newID);
            this.keyHashMap.put(newID, ssk);
            this.currentKeyID = newID;
        }
        this.cleanupStatelessKeys();
        return ssk;
    }

    SessionTicketExtension.StatelessKey getKey(int id) {
        return this.keyHashMap.get(id);
    }

    SSLSessionImpl get(byte[] id) {
        return (SSLSessionImpl)this.getSession(id);
    }

    SSLSessionImpl pull(byte[] id) {
        if (id != null) {
            return this.sessionCache.pull(new SessionId(id));
        }
        return null;
    }

    SSLSessionImpl get(String hostname, int port) {
        if (hostname == null && port == -1) {
            return null;
        }
        SSLSessionImpl sess = this.sessionHostPortCache.get(SSLSessionContextImpl.getKey(hostname, port));
        if (!this.isTimedout(sess)) {
            return sess;
        }
        return null;
    }

    private static String getKey(String hostname, int port) {
        return (hostname + ":" + port).toLowerCase(Locale.ENGLISH);
    }

    void put(SSLSessionImpl s) {
        this.sessionCache.put(s.getSessionId(), s);
        if (s.getPeerHost() != null && s.getPeerPort() != -1) {
            this.sessionHostPortCache.put(SSLSessionContextImpl.getKey(s.getPeerHost(), s.getPeerPort()), s);
        }
        s.setContext(this);
    }

    void remove(SessionId key) {
        SSLSessionImpl s = this.sessionCache.get(key);
        if (s != null) {
            this.sessionCache.remove(key);
            this.sessionHostPortCache.remove(SSLSessionContextImpl.getKey(s.getPeerHost(), s.getPeerPort()));
        }
    }

    private int getDefaults(boolean server) {
        block12: {
            try {
                int defaultCacheLimit;
                block11: {
                    String s;
                    String st = server ? GetPropertyAction.privilegedGetProperty("jdk.tls.server.enableSessionTicketExtension", "true") : GetPropertyAction.privilegedGetProperty("jdk.tls.client.enableSessionTicketExtension", "true");
                    if (st.compareToIgnoreCase("false") == 0) {
                        this.statelessSession = false;
                    }
                    if ((s = GetPropertyAction.privilegedGetProperty("jdk.tls.server.sessionTicketTimeout")) != null) {
                        try {
                            int t = Integer.parseInt(s);
                            if (t < 0 || t > 604800) {
                                this.timeout = 86400;
                                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                                    SSLLogger.warning("Invalid timeout given jdk.tls.server.sessionTicketTimeout: " + t + ".  Set to default value " + this.timeout, new Object[0]);
                                }
                            } else {
                                this.timeout = t;
                            }
                        }
                        catch (NumberFormatException e) {
                            this.setSessionTimeout(86400);
                            if (!SSLLogger.isOn || !SSLLogger.isOn("ssl")) break block11;
                            SSLLogger.warning("Invalid timeout for jdk.tls.server.sessionTicketTimeout: " + s + ".  Set to default value " + this.timeout, new Object[0]);
                        }
                    }
                }
                if ((defaultCacheLimit = GetIntegerAction.privilegedGetProperty("javax.net.ssl.sessionCacheSize", 20480).intValue()) >= 0) {
                    return defaultCacheLimit;
                }
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.warning("invalid System Property javax.net.ssl.sessionCacheSize, use the default session cache size (20480) instead", new Object[0]);
                }
            }
            catch (Exception e) {
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl")) break block12;
                SSLLogger.warning("the System Property javax.net.ssl.sessionCacheSize is not available, use the default value (20480) instead", new Object[0]);
            }
        }
        return 20480;
    }

    private boolean isTimedout(SSLSession sess) {
        if (this.timeout == 0) {
            return false;
        }
        if (sess != null && sess.getCreationTime() + (long)this.timeout * 1000L <= System.currentTimeMillis()) {
            sess.invalidate();
            return true;
        }
        return false;
    }

    private final class SessionCacheVisitor
    implements Cache.CacheVisitor<SessionId, SSLSessionImpl> {
        ArrayList<byte[]> ids = null;

        private SessionCacheVisitor() {
        }

        @Override
        public void visit(Map<SessionId, SSLSessionImpl> map) {
            this.ids = new ArrayList(map.size());
            for (SessionId key : map.keySet()) {
                SSLSessionImpl value = map.get(key);
                if (SSLSessionContextImpl.this.isTimedout(value)) continue;
                this.ids.add(key.getId());
            }
        }

        Enumeration<byte[]> getSessionIds() {
            return this.ids != null ? Collections.enumeration(this.ids) : Collections.emptyEnumeration();
        }
    }
}

