/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.SelectableChannel;
import jdk.internal.access.JavaIOFileDescriptorAccess;
import jdk.internal.access.SharedSecrets;
import sun.nio.ch.FileDispatcher;
import sun.nio.ch.IOUtil;
import sun.security.action.GetPropertyAction;

class FileDispatcherImpl
extends FileDispatcher {
    private static final int MAP_INVALID = -1;
    private static final int MAP_RO = 0;
    private static final int MAP_RW = 1;
    private static final int MAP_PV = 2;
    private static final long ALLOCATION_GRANULARITY;
    private static final int MAX_DIRECT_TRANSFER_SIZE;
    private static final boolean FAST_FILE_TRANSFER;
    private static final JavaIOFileDescriptorAccess fdAccess;

    FileDispatcherImpl() {
    }

    @Override
    boolean needsPositionLock() {
        return true;
    }

    @Override
    int read(FileDescriptor fd, long address, int len) throws IOException {
        return FileDispatcherImpl.read0(fd, address, len);
    }

    @Override
    int pread(FileDescriptor fd, long address, int len, long position) throws IOException {
        return FileDispatcherImpl.pread0(fd, address, len, position);
    }

    @Override
    long readv(FileDescriptor fd, long address, int len) throws IOException {
        return FileDispatcherImpl.readv0(fd, address, len);
    }

    @Override
    int write(FileDescriptor fd, long address, int len) throws IOException {
        return FileDispatcherImpl.write0(fd, address, len, fdAccess.getAppend(fd));
    }

    @Override
    int pwrite(FileDescriptor fd, long address, int len, long position) throws IOException {
        return FileDispatcherImpl.pwrite0(fd, address, len, position);
    }

    @Override
    long writev(FileDescriptor fd, long address, int len) throws IOException {
        return FileDispatcherImpl.writev0(fd, address, len, fdAccess.getAppend(fd));
    }

    @Override
    long seek(FileDescriptor fd, long offset) throws IOException {
        return FileDispatcherImpl.seek0(fd, offset);
    }

    @Override
    int force(FileDescriptor fd, boolean metaData) throws IOException {
        return FileDispatcherImpl.force0(fd, metaData);
    }

    @Override
    int truncate(FileDescriptor fd, long size) throws IOException {
        return FileDispatcherImpl.truncate0(fd, size);
    }

    @Override
    long size(FileDescriptor fd) throws IOException {
        return FileDispatcherImpl.size0(fd);
    }

    @Override
    int lock(FileDescriptor fd, boolean blocking, long pos, long size, boolean shared) throws IOException {
        return FileDispatcherImpl.lock0(fd, blocking, pos, size, shared);
    }

    @Override
    void release(FileDescriptor fd, long pos, long size) throws IOException {
        FileDispatcherImpl.release0(fd, pos, size);
    }

    @Override
    void close(FileDescriptor fd) throws IOException {
        fdAccess.close(fd);
    }

    @Override
    FileDescriptor duplicateForMapping(FileDescriptor fd) throws IOException {
        FileDescriptor result = new FileDescriptor();
        long handle = FileDispatcherImpl.duplicateHandle(fdAccess.getHandle(fd));
        fdAccess.setHandle(result, handle);
        fdAccess.registerCleanup(result);
        return result;
    }

    @Override
    boolean canTransferToDirectly(SelectableChannel sc) {
        return FAST_FILE_TRANSFER && sc.isBlocking();
    }

    @Override
    boolean transferToDirectlyNeedsPositionLock() {
        return true;
    }

    @Override
    boolean canTransferToFromOverlappedMap() {
        return true;
    }

    @Override
    long allocationGranularity() {
        return ALLOCATION_GRANULARITY;
    }

    @Override
    long map(FileDescriptor fd, int prot, long position, long length, boolean isSync) throws IOException {
        return FileDispatcherImpl.map0(fd, prot, position, length, isSync);
    }

    @Override
    int unmap(long address, long length) {
        return FileDispatcherImpl.unmap0(address, length);
    }

    @Override
    int maxDirectTransferSize() {
        return MAX_DIRECT_TRANSFER_SIZE;
    }

    @Override
    long transferTo(FileDescriptor src, long position, long count, FileDescriptor dst, boolean append) {
        return FileDispatcherImpl.transferTo0(src, position, count, dst, append);
    }

    @Override
    long transferFrom(FileDescriptor src, FileDescriptor dst, long position, long count, boolean append) {
        return -4L;
    }

    @Override
    int setDirectIO(FileDescriptor fd, String path) {
        int result = -1;
        String filePath = path.substring(0, path.lastIndexOf(File.separator));
        CharBuffer buffer = CharBuffer.allocate(filePath.length());
        buffer.put(filePath);
        try {
            result = FileDispatcherImpl.setDirect0(fd, buffer);
        }
        catch (IOException e) {
            throw new UnsupportedOperationException("Error setting up DirectIO", e);
        }
        return result;
    }

    static boolean isFastFileTransferRequested() {
        String fileTransferProp = GetPropertyAction.privilegedGetProperty("jdk.nio.enableFastFileTransfer", "false");
        return fileTransferProp.isEmpty() ? true : Boolean.parseBoolean(fileTransferProp);
    }

    static native int read0(FileDescriptor var0, long var1, int var3) throws IOException;

    static native int pread0(FileDescriptor var0, long var1, int var3, long var4) throws IOException;

    static native long readv0(FileDescriptor var0, long var1, int var3) throws IOException;

    static native int write0(FileDescriptor var0, long var1, int var3, boolean var4) throws IOException;

    static native int pwrite0(FileDescriptor var0, long var1, int var3, long var4) throws IOException;

    static native long writev0(FileDescriptor var0, long var1, int var3, boolean var4) throws IOException;

    static native long seek0(FileDescriptor var0, long var1) throws IOException;

    static native int force0(FileDescriptor var0, boolean var1) throws IOException;

    static native int truncate0(FileDescriptor var0, long var1) throws IOException;

    static native long size0(FileDescriptor var0) throws IOException;

    static native int lock0(FileDescriptor var0, boolean var1, long var2, long var4, boolean var6) throws IOException;

    static native void release0(FileDescriptor var0, long var1, long var3) throws IOException;

    static native void close0(FileDescriptor var0) throws IOException;

    static native long duplicateHandle(long var0) throws IOException;

    static native long allocationGranularity0();

    static native long map0(FileDescriptor var0, int var1, long var2, long var4, boolean var6) throws IOException;

    static native int unmap0(long var0, long var2);

    static native int maxDirectTransferSize0();

    static native long transferTo0(FileDescriptor var0, long var1, long var3, FileDescriptor var5, boolean var6);

    static native int setDirect0(FileDescriptor var0, CharBuffer var1) throws IOException;

    static {
        fdAccess = SharedSecrets.getJavaIOFileDescriptorAccess();
        IOUtil.load();
        FAST_FILE_TRANSFER = FileDispatcherImpl.isFastFileTransferRequested();
        ALLOCATION_GRANULARITY = FileDispatcherImpl.allocationGranularity0();
        MAX_DIRECT_TRANSFER_SIZE = FileDispatcherImpl.maxDirectTransferSize0();
    }
}

