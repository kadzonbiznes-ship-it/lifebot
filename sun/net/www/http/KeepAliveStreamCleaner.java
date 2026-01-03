/*
 * Decompiled with CFR 0.152.
 */
package sun.net.www.http;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import sun.net.NetProperties;
import sun.net.www.http.HttpClient;
import sun.net.www.http.KeepAliveCleanerEntry;
import sun.net.www.http.KeepAliveStream;

class KeepAliveStreamCleaner
extends LinkedList<KeepAliveCleanerEntry>
implements Runnable {
    protected static int MAX_DATA_REMAINING;
    protected static int MAX_CAPACITY;
    protected static final int TIMEOUT = 5000;
    private static final int MAX_RETRIES = 5;
    private final ReentrantLock queueLock = new ReentrantLock();
    private final Condition waiter = this.queueLock.newCondition();

    KeepAliveStreamCleaner() {
    }

    final void signalAll() {
        this.waiter.signalAll();
    }

    final void lock() {
        this.queueLock.lock();
    }

    final void unlock() {
        this.queueLock.unlock();
    }

    @Override
    public boolean offer(KeepAliveCleanerEntry e) {
        if (this.size() >= MAX_CAPACITY) {
            return false;
        }
        return super.offer(e);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        KeepAliveCleanerEntry kace = null;
        do {
            try {
                this.lock();
                try {
                    long before = System.currentTimeMillis();
                    long timeout = 5000L;
                    while ((kace = (KeepAliveCleanerEntry)this.poll()) == null) {
                        this.waiter.await(timeout, TimeUnit.MILLISECONDS);
                        long after = System.currentTimeMillis();
                        long elapsed = after - before;
                        if (elapsed > timeout) {
                            kace = (KeepAliveCleanerEntry)this.poll();
                            break;
                        }
                        before = after;
                        timeout -= elapsed;
                    }
                }
                finally {
                    this.unlock();
                }
                if (kace == null) break;
                KeepAliveStream kas = kace.getKeepAliveStream();
                if (kas == null) continue;
                kas.lock();
                try {
                    HttpClient hc = kace.getHttpClient();
                    try {
                        if (hc != null && !hc.isInKeepAliveCache()) {
                            int oldTimeout = hc.getReadTimeout();
                            hc.setReadTimeout(5000);
                            long remainingToRead = kas.remainingToRead();
                            if (remainingToRead > 0L) {
                                long n = 0L;
                                int retries = 0;
                                while (n < remainingToRead && retries < 5) {
                                    if ((n = kas.skip(remainingToRead -= n)) != 0L) continue;
                                    ++retries;
                                }
                                remainingToRead -= n;
                            }
                            if (remainingToRead == 0L) {
                                hc.setReadTimeout(oldTimeout);
                                hc.finished();
                            } else {
                                hc.closeServer();
                            }
                        }
                    }
                    catch (IOException ioe) {
                        hc.closeServer();
                    }
                    finally {
                        kas.setClosed();
                    }
                }
                finally {
                    kas.unlock();
                }
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        } while (kace != null);
    }

    static {
        int maxCapacity;
        int maxData;
        MAX_DATA_REMAINING = 512;
        MAX_CAPACITY = 10;
        String maxDataKey = "http.KeepAlive.remainingData";
        MAX_DATA_REMAINING = maxData = AccessController.doPrivileged(new PrivilegedAction<Integer>(){

            @Override
            public Integer run() {
                return NetProperties.getInteger("http.KeepAlive.remainingData", MAX_DATA_REMAINING);
            }
        }) * 1024;
        String maxCapacityKey = "http.KeepAlive.queuedConnections";
        MAX_CAPACITY = maxCapacity = AccessController.doPrivileged(new PrivilegedAction<Integer>(){

            @Override
            public Integer run() {
                return NetProperties.getInteger("http.KeepAlive.queuedConnections", MAX_CAPACITY);
            }
        }).intValue();
    }
}

