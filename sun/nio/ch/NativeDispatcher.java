/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;

abstract class NativeDispatcher {
    NativeDispatcher() {
    }

    abstract int read(FileDescriptor var1, long var2, int var4) throws IOException;

    boolean needsPositionLock() {
        return false;
    }

    int pread(FileDescriptor fd, long address, int len, long position) throws IOException {
        throw new IOException("Operation Unsupported");
    }

    abstract long readv(FileDescriptor var1, long var2, int var4) throws IOException;

    abstract int write(FileDescriptor var1, long var2, int var4) throws IOException;

    int pwrite(FileDescriptor fd, long address, int len, long position) throws IOException {
        throw new IOException("Operation Unsupported");
    }

    abstract long writev(FileDescriptor var1, long var2, int var4) throws IOException;

    abstract void close(FileDescriptor var1) throws IOException;

    void preClose(FileDescriptor fd) throws IOException {
    }

    void dup(FileDescriptor fd1, FileDescriptor fd2) throws IOException {
        throw new UnsupportedOperationException();
    }
}

