/*
 * Decompiled with CFR 0.152.
 */
package java.nio.channels;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.util.Objects;

public abstract class FileLock
implements AutoCloseable {
    private final Channel channel;
    private final long position;
    private final long size;
    private final boolean shared;

    protected FileLock(FileChannel channel, long position, long size, boolean shared) {
        Objects.requireNonNull(channel, "Null channel");
        if (position < 0L) {
            throw new IllegalArgumentException("Negative position");
        }
        if (size < 0L) {
            throw new IllegalArgumentException("Negative size");
        }
        if (position + size < 0L) {
            throw new IllegalArgumentException("Negative position + size");
        }
        this.channel = channel;
        this.position = position;
        this.size = size;
        this.shared = shared;
    }

    protected FileLock(AsynchronousFileChannel channel, long position, long size, boolean shared) {
        Objects.requireNonNull(channel, "Null channel");
        if (position < 0L) {
            throw new IllegalArgumentException("Negative position");
        }
        if (size < 0L) {
            throw new IllegalArgumentException("Negative size");
        }
        if (position + size < 0L) {
            throw new IllegalArgumentException("Negative position + size");
        }
        this.channel = channel;
        this.position = position;
        this.size = size;
        this.shared = shared;
    }

    public final FileChannel channel() {
        return this.channel instanceof FileChannel ? (FileChannel)this.channel : null;
    }

    public Channel acquiredBy() {
        return this.channel;
    }

    public final long position() {
        return this.position;
    }

    public final long size() {
        return this.size;
    }

    public final boolean isShared() {
        return this.shared;
    }

    public final boolean overlaps(long position, long size) {
        if (size < 0L) {
            return false;
        }
        if (this.position + this.size <= position) {
            return false;
        }
        if (size > 0L) {
            try {
                if (Math.addExact(position, size) <= this.position) {
                    return false;
                }
            }
            catch (ArithmeticException arithmeticException) {
                // empty catch block
            }
        }
        return true;
    }

    public abstract boolean isValid();

    public abstract void release() throws IOException;

    @Override
    public final void close() throws IOException {
        this.release();
    }

    public final String toString() {
        return this.getClass().getName() + "[" + this.position + ":" + this.size + " " + (this.shared ? "shared" : "exclusive") + " " + (this.isValid() ? "valid" : "invalid") + "]";
    }
}

