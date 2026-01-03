/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import jdk.internal.access.JavaIOFileDescriptorAccess;
import jdk.internal.access.SharedSecrets;
import sun.nio.ch.IOUtil;
import sun.nio.ch.NativeDispatcher;

class SocketDispatcher
extends NativeDispatcher {
    private static final JavaIOFileDescriptorAccess fdAccess = SharedSecrets.getJavaIOFileDescriptorAccess();

    SocketDispatcher() {
    }

    @Override
    int read(FileDescriptor fd, long address, int len) throws IOException {
        return SocketDispatcher.read0(fd, address, len);
    }

    @Override
    long readv(FileDescriptor fd, long address, int len) throws IOException {
        return SocketDispatcher.readv0(fd, address, len);
    }

    @Override
    int write(FileDescriptor fd, long address, int len) throws IOException {
        return SocketDispatcher.write0(fd, address, len);
    }

    @Override
    long writev(FileDescriptor fd, long address, int len) throws IOException {
        return SocketDispatcher.writev0(fd, address, len);
    }

    @Override
    void preClose(FileDescriptor fd) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    void close(FileDescriptor fd) throws IOException {
        SocketDispatcher.invalidateAndClose(fd);
    }

    static void invalidateAndClose(FileDescriptor fd) throws IOException {
        assert (fd.valid());
        int fdVal = fdAccess.get(fd);
        fdAccess.set(fd, -1);
        SocketDispatcher.close0(fdVal);
    }

    private static native int read0(FileDescriptor var0, long var1, int var3) throws IOException;

    private static native long readv0(FileDescriptor var0, long var1, int var3) throws IOException;

    private static native int write0(FileDescriptor var0, long var1, int var3) throws IOException;

    private static native long writev0(FileDescriptor var0, long var1, int var3) throws IOException;

    private static native void close0(int var0) throws IOException;

    static {
        IOUtil.load();
    }
}

