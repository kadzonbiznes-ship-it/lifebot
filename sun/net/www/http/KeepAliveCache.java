/*
 * Decompiled with CFR 0.152.
 */
package sun.net.www.http;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import jdk.internal.misc.InnocuousThread;
import sun.net.www.http.HttpClient;
import sun.net.www.http.KeepAliveEntry;
import sun.net.www.http.KeepAliveKey;
import sun.net.www.protocol.http.HttpURLConnection;
import sun.security.action.GetIntegerAction;
import sun.util.logging.PlatformLogger;

public class KeepAliveCache
extends HashMap<KeepAliveKey, ClientVector>
implements Runnable {
    private static final long serialVersionUID = -2937172892064557949L;
    private static final String keepAliveProp = "http.keepAlive.time.";
    private static final int userKeepAliveServer;
    private static final int userKeepAliveProxy;
    static final PlatformLogger logger;
    static final int MAX_CONNECTIONS = 5;
    static int result;
    static final int LIFETIME = 5000;
    private final ReentrantLock cacheLock = new ReentrantLock();
    private Thread keepAliveTimer = null;

    static int getUserKeepAliveSeconds(String type) {
        int v = AccessController.doPrivileged(new GetIntegerAction(keepAliveProp + type, -1));
        return v < -1 ? -1 : v;
    }

    static int getMaxConnections() {
        if (result == -1 && (result = AccessController.doPrivileged(new GetIntegerAction("http.maxConnections", 5)).intValue()) <= 0) {
            result = 5;
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void put(URL url, Object obj, HttpClient http) {
        HttpClient oldClient = null;
        this.cacheLock.lock();
        try {
            KeepAliveKey key;
            ClientVector v;
            boolean startThread;
            boolean bl = startThread = this.keepAliveTimer == null;
            if (!startThread && !this.keepAliveTimer.isAlive()) {
                startThread = true;
            }
            if (startThread) {
                this.clear();
                final KeepAliveCache cache = this;
                AccessController.doPrivileged(new PrivilegedAction<Object>(){

                    @Override
                    public Void run() {
                        KeepAliveCache.this.keepAliveTimer = InnocuousThread.newSystemThread("Keep-Alive-Timer", cache);
                        KeepAliveCache.this.keepAliveTimer.setDaemon(true);
                        KeepAliveCache.this.keepAliveTimer.setPriority(8);
                        KeepAliveCache.this.keepAliveTimer.start();
                        return null;
                    }
                });
            }
            if ((v = (ClientVector)super.get(key = new KeepAliveKey(url, obj))) == null) {
                int keepAliveTimeout = http.getKeepAliveTimeout();
                if (keepAliveTimeout == 0) {
                    keepAliveTimeout = KeepAliveCache.getUserKeepAlive(http.getUsingProxy());
                    if (keepAliveTimeout == -1) {
                        keepAliveTimeout = 5;
                    }
                } else if (keepAliveTimeout == -1) {
                    keepAliveTimeout = KeepAliveCache.getUserKeepAlive(http.getUsingProxy());
                    if (keepAliveTimeout == -1) {
                        keepAliveTimeout = http.getUsingProxy() ? 60 : 5;
                    }
                } else if (keepAliveTimeout == -2) {
                    keepAliveTimeout = 0;
                }
                assert (keepAliveTimeout >= 0);
                if (keepAliveTimeout == 0) {
                    oldClient = http;
                } else {
                    v = new ClientVector(keepAliveTimeout * 1000);
                    v.put(http);
                    super.put(key, v);
                }
            } else {
                oldClient = v.put(http);
            }
        }
        finally {
            this.cacheLock.unlock();
        }
        if (oldClient != null) {
            oldClient.closeServer();
        }
    }

    private static int getUserKeepAlive(boolean isProxy) {
        return isProxy ? userKeepAliveProxy : userKeepAliveServer;
    }

    private void removeVector(KeepAliveKey k) {
        assert (this.cacheLock.isHeldByCurrentThread());
        super.remove(k);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public HttpClient get(URL url, Object obj) {
        this.cacheLock.lock();
        try {
            KeepAliveKey key = new KeepAliveKey(url, obj);
            ClientVector v = (ClientVector)super.get(key);
            if (v == null) {
                HttpClient httpClient = null;
                return httpClient;
            }
            HttpClient httpClient = v.get();
            return httpClient;
        }
        finally {
            this.cacheLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        do {
            try {
                Thread.sleep(5000L);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
            ArrayList<HttpClient> closeList = null;
            this.cacheLock.lock();
            try {
                if (this.isEmpty()) {
                    this.keepAliveTimer = null;
                    break;
                }
                long currentTime = System.currentTimeMillis();
                ArrayList<KeepAliveKey> keysToRemove = new ArrayList<KeepAliveKey>();
                for (KeepAliveKey key : this.keySet()) {
                    ClientVector v = (ClientVector)this.get(key);
                    KeepAliveEntry e = (KeepAliveEntry)v.peekLast();
                    while (e != null && currentTime - e.idleStartTime > (long)v.nap) {
                        v.pollLast();
                        if (closeList == null) {
                            closeList = new ArrayList<HttpClient>();
                        }
                        closeList.add(e.hc);
                        e = (KeepAliveEntry)v.peekLast();
                    }
                    if (!v.isEmpty()) continue;
                    keysToRemove.add(key);
                }
                for (KeepAliveKey key : keysToRemove) {
                    this.removeVector(key);
                }
            }
            finally {
                this.cacheLock.unlock();
                if (closeList != null) {
                    for (HttpClient hc : closeList) {
                        hc.closeServer();
                    }
                }
            }
        } while (this.keepAliveTimer == Thread.currentThread());
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        throw new NotSerializableException();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throw new NotSerializableException();
    }

    static {
        logger = HttpURLConnection.getHttpLogger();
        userKeepAliveServer = KeepAliveCache.getUserKeepAliveSeconds("server");
        userKeepAliveProxy = KeepAliveCache.getUserKeepAliveSeconds("proxy");
        result = -1;
    }

    class ClientVector
    extends ArrayDeque<KeepAliveEntry> {
        private static final long serialVersionUID = -8680532108106489459L;
        int nap;

        ClientVector(int nap) {
            this.nap = nap;
        }

        HttpClient get() {
            assert (KeepAliveCache.this.cacheLock.isHeldByCurrentThread());
            KeepAliveEntry e = (KeepAliveEntry)this.peekFirst();
            if (e == null) {
                return null;
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime - e.idleStartTime > (long)this.nap) {
                return null;
            }
            this.pollFirst();
            if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
                String msg = "cached HttpClient was idle for " + Long.toString(currentTime - e.idleStartTime);
                logger.finest(msg);
            }
            return e.hc;
        }

        HttpClient put(HttpClient h) {
            assert (KeepAliveCache.this.cacheLock.isHeldByCurrentThread());
            HttpClient staleClient = null;
            assert (KeepAliveCache.getMaxConnections() > 0);
            if (this.size() >= KeepAliveCache.getMaxConnections()) {
                staleClient = ((KeepAliveEntry)this.removeLast()).hc;
            }
            this.addFirst(new KeepAliveEntry(h, System.currentTimeMillis()));
            return staleClient;
        }

        private void writeObject(ObjectOutputStream stream) throws IOException {
            throw new NotSerializableException();
        }

        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            throw new NotSerializableException();
        }
    }
}

