/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.fs;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import jdk.internal.access.JavaIOFileDescriptorAccess;
import jdk.internal.access.SharedSecrets;
import sun.nio.ch.FileChannelImpl;
import sun.nio.ch.ThreadPool;
import sun.nio.ch.WindowsAsynchronousFileChannelImpl;
import sun.nio.fs.ExtendedOptions;
import sun.nio.fs.WindowsException;
import sun.nio.fs.WindowsFileAttributes;
import sun.nio.fs.WindowsNativeDispatcher;

class WindowsChannelFactory {
    private static final JavaIOFileDescriptorAccess fdAccess = SharedSecrets.getJavaIOFileDescriptorAccess();
    static final OpenOption OPEN_REPARSE_POINT = new OpenOption(){};

    private WindowsChannelFactory() {
    }

    static FileChannel newFileChannel(String pathForWindows, String pathToCheck, Set<? extends OpenOption> options, long pSecurityDescriptor) throws WindowsException {
        Flags flags = Flags.toFlags(options);
        if (!flags.read && !flags.write) {
            if (flags.append) {
                flags.write = true;
            } else {
                flags.read = true;
            }
        }
        if (flags.read && flags.append) {
            throw new IllegalArgumentException("READ + APPEND not allowed");
        }
        if (flags.append && flags.truncateExisting) {
            throw new IllegalArgumentException("APPEND + TRUNCATE_EXISTING not allowed");
        }
        FileDescriptor fdObj = WindowsChannelFactory.open(pathForWindows, pathToCheck, flags, pSecurityDescriptor);
        return FileChannelImpl.open(fdObj, pathForWindows, flags.read, flags.write, flags.direct, null);
    }

    static AsynchronousFileChannel newAsynchronousFileChannel(String pathForWindows, String pathToCheck, Set<? extends OpenOption> options, long pSecurityDescriptor, ThreadPool pool) throws IOException {
        FileDescriptor fdObj;
        Flags flags = Flags.toFlags(options);
        flags.overlapped = true;
        if (!flags.read && !flags.write) {
            flags.read = true;
        }
        if (flags.append) {
            throw new UnsupportedOperationException("APPEND not allowed");
        }
        try {
            fdObj = WindowsChannelFactory.open(pathForWindows, pathToCheck, flags, pSecurityDescriptor);
        }
        catch (WindowsException x) {
            x.rethrowAsIOException(pathForWindows);
            return null;
        }
        try {
            return WindowsAsynchronousFileChannelImpl.open(fdObj, flags.read, flags.write, pool);
        }
        catch (IOException x) {
            fdAccess.close(fdObj);
            throw x;
        }
    }

    private static FileDescriptor open(String pathForWindows, String pathToCheck, Flags flags, long pSecurityDescriptor) throws WindowsException {
        long handle;
        int dwCreationDisposition;
        block31: {
            SecurityManager sm;
            boolean truncateAfterOpen = false;
            int dwDesiredAccess = 0;
            if (flags.read) {
                dwDesiredAccess |= Integer.MIN_VALUE;
            }
            if (flags.write) {
                dwDesiredAccess |= 0x40000000;
            }
            int dwShareMode = 0;
            if (flags.shareRead) {
                dwShareMode |= 1;
            }
            if (flags.shareWrite) {
                dwShareMode |= 2;
            }
            if (flags.shareDelete) {
                dwShareMode |= 4;
            }
            int dwFlagsAndAttributes = 128;
            dwCreationDisposition = 3;
            if (flags.write) {
                if (flags.createNew) {
                    dwCreationDisposition = 1;
                    dwFlagsAndAttributes |= 0x200000;
                } else {
                    if (flags.create) {
                        dwCreationDisposition = 4;
                    }
                    if (flags.truncateExisting) {
                        if (dwCreationDisposition == 4) {
                            truncateAfterOpen = true;
                        } else {
                            dwCreationDisposition = 5;
                        }
                    }
                }
            }
            if (flags.dsync || flags.sync) {
                dwFlagsAndAttributes |= Integer.MIN_VALUE;
            }
            if (flags.overlapped) {
                dwFlagsAndAttributes |= 0x40000000;
            }
            if (flags.deleteOnClose) {
                dwFlagsAndAttributes |= 0x4000000;
            }
            boolean okayToFollowLinks = true;
            if (dwCreationDisposition != 1 && (flags.noFollowLinks || flags.openReparsePoint || flags.deleteOnClose)) {
                if (flags.noFollowLinks || flags.deleteOnClose) {
                    okayToFollowLinks = false;
                }
                dwFlagsAndAttributes |= 0x200000;
            }
            if (pathToCheck != null && (sm = System.getSecurityManager()) != null) {
                if (flags.read) {
                    sm.checkRead(pathToCheck);
                }
                if (flags.write) {
                    sm.checkWrite(pathToCheck);
                }
                if (flags.deleteOnClose) {
                    sm.checkDelete(pathToCheck);
                }
            }
            handle = WindowsNativeDispatcher.CreateFile(pathForWindows, dwDesiredAccess, dwShareMode, pSecurityDescriptor, dwCreationDisposition, dwFlagsAndAttributes);
            if (!okayToFollowLinks) {
                try {
                    if (WindowsFileAttributes.readAttributes(handle).isSymbolicLink()) {
                        throw new WindowsException("File is symbolic link");
                    }
                }
                catch (WindowsException x) {
                    WindowsNativeDispatcher.CloseHandle(handle);
                    throw x;
                }
            }
            if (truncateAfterOpen) {
                try {
                    WindowsNativeDispatcher.SetEndOfFile(handle);
                }
                catch (WindowsException x) {
                    if (WindowsNativeDispatcher.GetFileSizeEx(handle) == 0L) break block31;
                    WindowsNativeDispatcher.CloseHandle(handle);
                    throw x;
                }
            }
        }
        if (dwCreationDisposition == 1 && flags.sparse) {
            try {
                WindowsNativeDispatcher.DeviceIoControlSetSparse(handle);
            }
            catch (WindowsException x) {
                // empty catch block
            }
        }
        FileDescriptor fdObj = new FileDescriptor();
        fdAccess.setHandle(fdObj, handle);
        fdAccess.setAppend(fdObj, flags.append);
        fdAccess.registerCleanup(fdObj);
        return fdObj;
    }

    private static class Flags {
        boolean read;
        boolean write;
        boolean append;
        boolean truncateExisting;
        boolean create;
        boolean createNew;
        boolean deleteOnClose;
        boolean sparse;
        boolean overlapped;
        boolean sync;
        boolean dsync;
        boolean direct;
        boolean shareRead = true;
        boolean shareWrite = true;
        boolean shareDelete = true;
        boolean noFollowLinks;
        boolean openReparsePoint;

        private Flags() {
        }

        static Flags toFlags(Set<? extends OpenOption> options) {
            Flags flags = new Flags();
            block12: for (OpenOption openOption : options) {
                if (openOption instanceof StandardOpenOption) {
                    switch ((StandardOpenOption)openOption) {
                        case READ: {
                            flags.read = true;
                            continue block12;
                        }
                        case WRITE: {
                            flags.write = true;
                            continue block12;
                        }
                        case APPEND: {
                            flags.append = true;
                            continue block12;
                        }
                        case TRUNCATE_EXISTING: {
                            flags.truncateExisting = true;
                            continue block12;
                        }
                        case CREATE: {
                            flags.create = true;
                            continue block12;
                        }
                        case CREATE_NEW: {
                            flags.createNew = true;
                            continue block12;
                        }
                        case DELETE_ON_CLOSE: {
                            flags.deleteOnClose = true;
                            continue block12;
                        }
                        case SPARSE: {
                            flags.sparse = true;
                            continue block12;
                        }
                        case SYNC: {
                            flags.sync = true;
                            continue block12;
                        }
                        case DSYNC: {
                            flags.dsync = true;
                            continue block12;
                        }
                    }
                    throw new UnsupportedOperationException();
                }
                if (openOption == LinkOption.NOFOLLOW_LINKS) {
                    flags.noFollowLinks = true;
                    continue;
                }
                if (openOption == OPEN_REPARSE_POINT) {
                    flags.openReparsePoint = true;
                    continue;
                }
                if (ExtendedOptions.NOSHARE_READ.matches(openOption)) {
                    flags.shareRead = false;
                    continue;
                }
                if (ExtendedOptions.NOSHARE_WRITE.matches(openOption)) {
                    flags.shareWrite = false;
                    continue;
                }
                if (ExtendedOptions.NOSHARE_DELETE.matches(openOption)) {
                    flags.shareDelete = false;
                    continue;
                }
                if (ExtendedOptions.DIRECT.matches(openOption)) {
                    flags.direct = true;
                    continue;
                }
                if (openOption == null) {
                    throw new NullPointerException();
                }
                throw new UnsupportedOperationException();
            }
            return flags;
        }
    }
}

