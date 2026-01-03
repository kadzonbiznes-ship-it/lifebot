/*
 * Decompiled with CFR 0.152.
 */
package sun.net.www;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.ReentrantLock;
import sun.net.www.http.ChunkedInputStream;

public class MeteredStream
extends FilterInputStream {
    protected boolean closed = false;
    protected long expected;
    protected long count = 0L;
    protected long markedCount = 0L;
    protected int markLimit = -1;
    private final ReentrantLock readLock = new ReentrantLock();

    public MeteredStream(InputStream is, long expected) {
        super(is);
        this.expected = expected;
    }

    private final void justRead(long n) throws IOException {
        assert (this.isLockHeldByCurrentThread());
        if (n == -1L) {
            if (!this.isMarked()) {
                this.close();
            }
            return;
        }
        this.count += n;
        if (this.count - this.markedCount > (long)this.markLimit) {
            this.markLimit = -1;
        }
        if (this.isMarked()) {
            return;
        }
        if (this.expected > 0L && this.count >= this.expected) {
            this.close();
        }
    }

    private boolean isMarked() {
        assert (this.isLockHeldByCurrentThread());
        if (this.markLimit < 0) {
            return false;
        }
        return this.count - this.markedCount <= (long)this.markLimit;
    }

    @Override
    public int read() throws IOException {
        this.lock();
        try {
            if (this.closed) {
                int n = -1;
                return n;
            }
            int c = this.in.read();
            if (c != -1) {
                this.justRead(1L);
            } else {
                this.justRead(c);
            }
            int n = c;
            return n;
        }
        finally {
            this.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        this.lock();
        try {
            if (this.closed) {
                int n = -1;
                return n;
            }
            int n = this.in.read(b, off, len);
            this.justRead(n);
            int n2 = n;
            return n2;
        }
        finally {
            this.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public long skip(long n) throws IOException {
        this.lock();
        try {
            if (this.closed) {
                long l = 0L;
                return l;
            }
            if (this.in instanceof ChunkedInputStream) {
                n = this.in.skip(n);
            } else {
                long min = n > this.expected - this.count ? this.expected - this.count : n;
                n = this.in.skip(min);
            }
            this.justRead(n);
            long l = n;
            return l;
        }
        finally {
            this.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        this.lock();
        try {
            if (this.closed) {
                return;
            }
            this.closed = true;
            this.in.close();
        }
        finally {
            this.unlock();
        }
    }

    @Override
    public int available() throws IOException {
        this.lock();
        try {
            int n = this.closed ? 0 : this.in.available();
            return n;
        }
        finally {
            this.unlock();
        }
    }

    @Override
    public void mark(int readLimit) {
        this.lock();
        try {
            if (this.closed) {
                return;
            }
            super.mark(readLimit);
            this.markedCount = this.count;
            this.markLimit = readLimit;
        }
        finally {
            this.unlock();
        }
    }

    @Override
    public void reset() throws IOException {
        this.lock();
        try {
            if (this.closed) {
                return;
            }
            if (!this.isMarked()) {
                throw new IOException("Resetting to an invalid mark");
            }
            this.count = this.markedCount;
            super.reset();
        }
        finally {
            this.unlock();
        }
    }

    @Override
    public boolean markSupported() {
        this.lock();
        try {
            if (this.closed) {
                boolean bl = false;
                return bl;
            }
            boolean bl = super.markSupported();
            return bl;
        }
        finally {
            this.unlock();
        }
    }

    public final void lock() {
        this.readLock.lock();
    }

    public final void unlock() {
        this.readLock.unlock();
    }

    public final boolean isLockHeldByCurrentThread() {
        return this.readLock.isHeldByCurrentThread();
    }
}

