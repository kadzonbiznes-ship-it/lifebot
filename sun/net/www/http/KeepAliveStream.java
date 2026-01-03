/*
 * Decompiled with CFR 0.152.
 */
package sun.net.www.http;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import jdk.internal.misc.InnocuousThread;
import sun.net.www.MeteredStream;
import sun.net.www.http.HttpClient;
import sun.net.www.http.Hurryable;
import sun.net.www.http.KeepAliveCleanerEntry;
import sun.net.www.http.KeepAliveStreamCleaner;

public class KeepAliveStream
extends MeteredStream
implements Hurryable {
    HttpClient hc;
    boolean hurried;
    protected volatile boolean queuedForCleanup = false;
    private static final KeepAliveStreamCleaner queue = new KeepAliveStreamCleaner();
    private static Thread cleanerThread;

    public KeepAliveStream(InputStream is, long expected, HttpClient hc) {
        super(is, expected);
        this.hc = hc;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() throws IOException {
        if (this.queuedForCleanup) {
            return;
        }
        this.lock();
        try {
            if (this.closed || this.queuedForCleanup) {
                return;
            }
            try {
                if (this.expected > this.count) {
                    long nskip = this.expected - this.count;
                    if (nskip <= (long)this.available()) {
                        while ((nskip = this.expected - this.count) > 0L && this.skip(Math.min(nskip, (long)this.available())) > 0L) {
                        }
                    } else if (this.expected <= (long)KeepAliveStreamCleaner.MAX_DATA_REMAINING && !this.hurried) {
                        KeepAliveStream.queueForCleanup(new KeepAliveCleanerEntry(this, this.hc));
                    } else {
                        this.hc.closeServer();
                    }
                }
                if (!(this.closed || this.hurried || this.queuedForCleanup)) {
                    this.hc.finished();
                }
            }
            finally {
                if (!this.queuedForCleanup) {
                    this.in = null;
                    this.hc = null;
                    this.closed = true;
                }
            }
        }
        finally {
            this.unlock();
        }
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void mark(int limit) {
    }

    @Override
    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean hurry() {
        this.lock();
        try {
            if (this.closed || this.count >= this.expected) {
                boolean bl = false;
                return bl;
            }
            if ((long)this.in.available() < this.expected - this.count) {
                boolean bl = false;
                return bl;
            }
            int size = (int)(this.expected - this.count);
            byte[] buf = new byte[size];
            DataInputStream dis = new DataInputStream(this.in);
            dis.readFully(buf);
            this.in = new ByteArrayInputStream(buf);
            this.hurried = true;
            boolean bl = true;
            return bl;
        }
        catch (IOException e) {
            boolean bl = false;
            return bl;
        }
        finally {
            this.unlock();
        }
    }

    private static void queueForCleanup(KeepAliveCleanerEntry kace) {
        queue.lock();
        try {
            boolean startCleanupThread;
            if (!kace.getQueuedForCleanup()) {
                if (!queue.offer(kace)) {
                    kace.getHttpClient().closeServer();
                    return;
                }
                kace.setQueuedForCleanup();
                queue.signalAll();
            }
            boolean bl = startCleanupThread = cleanerThread == null;
            if (!startCleanupThread && !cleanerThread.isAlive()) {
                startCleanupThread = true;
            }
            if (startCleanupThread) {
                AccessController.doPrivileged(new PrivilegedAction<Void>(){

                    @Override
                    public Void run() {
                        cleanerThread = InnocuousThread.newSystemThread("Keep-Alive-SocketCleaner", queue);
                        cleanerThread.setDaemon(true);
                        cleanerThread.setPriority(8);
                        cleanerThread.start();
                        return null;
                    }
                });
            }
        }
        finally {
            queue.unlock();
        }
    }

    protected long remainingToRead() {
        assert (this.isLockHeldByCurrentThread());
        return this.expected - this.count;
    }

    protected void setClosed() {
        assert (this.isLockHeldByCurrentThread());
        this.in = null;
        this.hc = null;
        this.closed = true;
    }
}

