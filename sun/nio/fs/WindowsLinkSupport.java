/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.fs;

import java.io.IOError;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.NotLinkException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import jdk.internal.misc.Unsafe;
import sun.nio.fs.NativeBuffer;
import sun.nio.fs.NativeBuffers;
import sun.nio.fs.WindowsException;
import sun.nio.fs.WindowsFileAttributes;
import sun.nio.fs.WindowsFileSystem;
import sun.nio.fs.WindowsNativeDispatcher;
import sun.nio.fs.WindowsPath;

class WindowsLinkSupport {
    private static final Unsafe unsafe = Unsafe.getUnsafe();

    private WindowsLinkSupport() {
    }

    static void createSymbolicLink(String link, String target, int flags) throws WindowsException {
        try {
            WindowsNativeDispatcher.CreateSymbolicLink(link, target, flags);
        }
        catch (WindowsException x) {
            block5: {
                if (x.lastError() == 1314) {
                    flags |= 2;
                    try {
                        WindowsNativeDispatcher.CreateSymbolicLink(link, target, flags);
                        return;
                    }
                    catch (WindowsException y) {
                        int lastError = y.lastError();
                        if (lastError == 1314 || lastError == 87) break block5;
                        throw y;
                    }
                }
            }
            throw x;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static String readLink(WindowsPath path) throws IOException {
        long handle = 0L;
        try {
            handle = path.openForReadAttributeAccess(false);
        }
        catch (WindowsException x) {
            x.rethrowAsIOException(path);
        }
        try {
            String string = WindowsLinkSupport.readLinkImpl(handle);
            return string;
        }
        finally {
            WindowsNativeDispatcher.CloseHandle(handle);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static String getFinalPath(WindowsPath input) throws IOException {
        long h = 0L;
        try {
            h = input.openForReadAttributeAccess(true);
        }
        catch (WindowsException x) {
            x.rethrowAsIOException(input);
        }
        try {
            String x = WindowsLinkSupport.stripPrefix(WindowsNativeDispatcher.GetFinalPathNameByHandle(h));
            return x;
        }
        catch (WindowsException x) {
            if (x.lastError() != 124) {
                x.rethrowAsIOException(input);
            }
        }
        finally {
            WindowsNativeDispatcher.CloseHandle(h);
        }
        return null;
    }

    static String getFinalPath(WindowsPath input, boolean followLinks) throws IOException {
        WindowsFileSystem fs = input.getFileSystem();
        try {
            if (!followLinks) {
                return input.getPathForWin32Calls();
            }
            if (!WindowsFileAttributes.get(input, false).isSymbolicLink()) {
                return input.getPathForWin32Calls();
            }
        }
        catch (WindowsException x) {
            x.rethrowAsIOException(input);
        }
        String result = WindowsLinkSupport.getFinalPath(input);
        if (result != null) {
            return result;
        }
        WindowsPath target = input;
        int linkCount = 0;
        do {
            try {
                WindowsFileAttributes attrs = WindowsFileAttributes.get(target, false);
                if (!attrs.isSymbolicLink()) {
                    return target.getPathForWin32Calls();
                }
            }
            catch (WindowsException x) {
                x.rethrowAsIOException(target);
            }
            WindowsPath link = WindowsPath.createFromNormalizedPath(fs, WindowsLinkSupport.readLink(target));
            WindowsPath parent = target.getParent();
            if (parent == null) {
                final WindowsPath t = target;
                target = AccessController.doPrivileged(new PrivilegedAction<WindowsPath>(){

                    @Override
                    public WindowsPath run() {
                        return t.toAbsolutePath();
                    }
                });
                parent = target.getParent();
            }
            target = parent.resolve(link);
        } while (++linkCount < 32);
        throw new FileSystemException(input.getPathForExceptionMessage(), null, "Too many links");
    }

    static String getRealPath(WindowsPath input, boolean resolveLinks) throws IOException {
        int start;
        WindowsFileSystem fs = input.getFileSystem();
        String path = null;
        try {
            path = input.toAbsolutePath().toString();
        }
        catch (IOError x) {
            throw (IOException)x.getCause();
        }
        if (path.indexOf(46) >= 0) {
            try {
                path = WindowsNativeDispatcher.GetFullPathName(path);
            }
            catch (WindowsException x) {
                x.rethrowAsIOException(input);
            }
        }
        StringBuilder sb = new StringBuilder(path.length());
        char c0 = path.charAt(0);
        char c1 = path.charAt(1);
        if ((c0 <= 'z' && c0 >= 'a' || c0 <= 'Z' && c0 >= 'A') && c1 == ':' && path.charAt(2) == '\\') {
            sb.append(Character.toUpperCase(c0));
            sb.append(":\\");
            start = 3;
        } else if (c0 == '\\' && c1 == '\\') {
            int last = path.length() - 1;
            int pos = path.indexOf(92, 2);
            if (pos == -1 || pos == last) {
                throw new FileSystemException(input.getPathForExceptionMessage(), null, "UNC has invalid share");
            }
            if ((pos = path.indexOf(92, pos + 1)) < 0) {
                pos = last;
                sb.append(path).append("\\");
            } else {
                sb.append(path, 0, pos + 1);
            }
            start = pos + 1;
        } else {
            throw new AssertionError((Object)"path type not recognized");
        }
        if (start >= path.length()) {
            String result = sb.toString();
            try {
                WindowsNativeDispatcher.GetFileAttributes(result);
            }
            catch (WindowsException x) {
                x.rethrowAsIOException(path);
            }
            return result;
        }
        int curr = start;
        while (curr < path.length()) {
            int next = path.indexOf(92, curr);
            int end = next == -1 ? path.length() : next;
            String search = sb.toString() + path.substring(curr, end);
            try {
                WindowsNativeDispatcher.FirstFile fileData = WindowsNativeDispatcher.FindFirstFile(WindowsPath.addPrefixIfNeeded(search));
                WindowsNativeDispatcher.FindClose(fileData.handle());
                if (resolveLinks && WindowsFileAttributes.isReparsePoint(fileData.attributes())) {
                    String result = WindowsLinkSupport.getFinalPath(input);
                    if (result == null) {
                        WindowsPath resolved = WindowsLinkSupport.resolveAllLinks(WindowsPath.createFromNormalizedPath(fs, path));
                        result = WindowsLinkSupport.getRealPath(resolved, false);
                    }
                    return result;
                }
                sb.append(fileData.name());
                if (next != -1) {
                    sb.append('\\');
                }
            }
            catch (WindowsException e) {
                e.rethrowAsIOException(path);
            }
            curr = end + 1;
        }
        return sb.toString();
    }

    private static String readLinkImpl(long handle) throws IOException {
        int size = 16384;
        try (NativeBuffer buffer = NativeBuffers.getNativeBuffer(size);){
            try {
                WindowsNativeDispatcher.DeviceIoControlGetReparsePoint(handle, buffer.address(), size);
            }
            catch (WindowsException x) {
                if (x.lastError() == 4390) {
                    throw new NotLinkException(null, null, x.errorString());
                }
                x.rethrowAsIOException((String)null);
            }
            boolean OFFSETOF_REPARSETAG = false;
            int OFFSETOF_PATHOFFSET = 8;
            int OFFSETOF_PATHLENGTH = 10;
            int OFFSETOF_PATHBUFFER = 20;
            int tag = (int)unsafe.getLong(buffer.address() + 0L);
            if (tag != -1610612724) {
                throw new NotLinkException(null, null, "Reparse point is not a symbolic link");
            }
            short nameOffset = unsafe.getShort(buffer.address() + 8L);
            short nameLengthInBytes = unsafe.getShort(buffer.address() + 10L);
            if (nameLengthInBytes % 2 != 0) {
                throw new FileSystemException(null, null, "Symbolic link corrupted");
            }
            char[] name = new char[nameLengthInBytes / 2];
            unsafe.copyMemory(null, buffer.address() + 20L + (long)nameOffset, name, Unsafe.ARRAY_CHAR_BASE_OFFSET, nameLengthInBytes);
            String target = WindowsLinkSupport.stripPrefix(new String(name));
            if (target.isEmpty()) {
                throw new IOException("Symbolic link target is invalid");
            }
            String string = target;
            return string;
        }
    }

    private static WindowsPath resolveAllLinks(WindowsPath path) throws IOException {
        assert (path.isAbsolute());
        WindowsFileSystem fs = path.getFileSystem();
        int linkCount = 0;
        int elem = 0;
        while (elem < path.getNameCount()) {
            WindowsPath current = path.getRoot().resolve(path.subpath(0, elem + 1));
            WindowsFileAttributes attrs = null;
            try {
                attrs = WindowsFileAttributes.get(current, false);
            }
            catch (WindowsException x) {
                x.rethrowAsIOException(current);
            }
            if (attrs.isSymbolicLink()) {
                if (++linkCount > 32) {
                    throw new IOException("Too many links");
                }
                WindowsPath target = WindowsPath.createFromNormalizedPath(fs, WindowsLinkSupport.readLink(current));
                WindowsPath remainder = null;
                int count = path.getNameCount();
                if (elem + 1 < count) {
                    remainder = path.subpath(elem + 1, count);
                }
                path = current.getParent().resolve(target);
                try {
                    String full = WindowsNativeDispatcher.GetFullPathName(path.toString());
                    if (!full.equals(path.toString())) {
                        path = WindowsPath.createFromNormalizedPath(fs, full);
                    }
                }
                catch (WindowsException x) {
                    x.rethrowAsIOException(path);
                }
                if (remainder != null) {
                    path = path.resolve(remainder);
                }
                elem = 0;
                continue;
            }
            ++elem;
        }
        return path;
    }

    private static String stripPrefix(String path) {
        if (path.startsWith("\\\\?\\")) {
            path = path.startsWith("\\\\?\\UNC\\") ? "\\" + path.substring(7) : path.substring(4);
            return path;
        }
        if (path.startsWith("\\??\\")) {
            path = path.startsWith("\\??\\UNC\\") ? "\\" + path.substring(7) : path.substring(4);
            return path;
        }
        return path;
    }
}

