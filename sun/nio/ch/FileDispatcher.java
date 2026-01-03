/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.SelectableChannel;
import sun.nio.ch.NativeDispatcher;

abstract class FileDispatcher
extends NativeDispatcher {
    public static final int NO_LOCK = -1;
    public static final int LOCKED = 0;
    public static final int RET_EX_LOCK = 1;
    public static final int INTERRUPTED = 2;

    FileDispatcher() {
    }

    abstract long seek(FileDescriptor var1, long var2) throws IOException;

    abstract int force(FileDescriptor var1, boolean var2) throws IOException;

    abstract int truncate(FileDescriptor var1, long var2) throws IOException;

    abstract long size(FileDescriptor var1) throws IOException;

    abstract int lock(FileDescriptor var1, boolean var2, long var3, long var5, boolean var7) throws IOException;

    abstract void release(FileDescriptor var1, long var2, long var4) throws IOException;

    abstract FileDescriptor duplicateForMapping(FileDescriptor var1) throws IOException;

    abstract boolean canTransferToDirectly(SelectableChannel var1);

    abstract boolean transferToDirectlyNeedsPositionLock();

    abstract boolean canTransferToFromOverlappedMap();

    abstract long allocationGranularity();

    abstract long map(FileDescriptor var1, int var2, long var3, long var5, boolean var7) throws IOException;

    abstract int unmap(long var1, long var3);

    abstract int maxDirectTransferSize();

    abstract long transferTo(FileDescriptor var1, long var2, long var4, FileDescriptor var6, boolean var7);

    abstract long transferFrom(FileDescriptor var1, FileDescriptor var2, long var3, long var5, boolean var7);

    abstract int setDirectIO(FileDescriptor var1, String var2);
}

