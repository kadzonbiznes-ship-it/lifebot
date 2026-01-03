/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import sun.nio.ch.AsynchronousFileChannelImpl;
import sun.nio.ch.FileChannelImpl;

public class FileLockImpl
extends FileLock {
    private volatile boolean invalid;

    FileLockImpl(FileChannel channel, long position, long size, boolean shared) {
        super(channel, position, size, shared);
    }

    FileLockImpl(AsynchronousFileChannel channel, long position, long size, boolean shared) {
        super(channel, position, size, shared);
    }

    @Override
    public boolean isValid() {
        return !this.invalid;
    }

    void invalidate() {
        assert (Thread.holdsLock(this));
        this.invalid = true;
    }

    @Override
    public synchronized void release() throws IOException {
        Channel ch = this.acquiredBy();
        if (!ch.isOpen()) {
            throw new ClosedChannelException();
        }
        if (this.isValid()) {
            if (ch instanceof FileChannelImpl) {
                ((FileChannelImpl)ch).release(this);
            } else if (ch instanceof AsynchronousFileChannelImpl) {
                ((AsynchronousFileChannelImpl)ch).release(this);
            } else {
                throw new AssertionError();
            }
            this.invalidate();
        }
    }
}

