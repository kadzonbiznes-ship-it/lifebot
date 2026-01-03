/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.BindException;
import java.net.NetPermission;
import java.net.SocketAddress;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import sun.nio.ch.IOUtil;
import sun.nio.ch.UnixDomainSocketsUtil;
import sun.nio.fs.AbstractFileSystemProvider;
import sun.nio.fs.DefaultFileSystemProvider;

class UnixDomainSockets {
    private static final boolean supported;
    private static final String tempDir;
    private static final NetPermission accessUnixDomainSocket;
    private static final Random random;

    private UnixDomainSockets() {
    }

    static boolean isSupported() {
        return supported;
    }

    static void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(accessUnixDomainSocket);
        }
    }

    static UnixDomainSocketAddress getRevealedLocalAddress(SocketAddress sa) {
        UnixDomainSocketAddress addr = (UnixDomainSocketAddress)sa;
        try {
            UnixDomainSockets.checkPermission();
        }
        catch (SecurityException e) {
            addr = UnixDomainSockets.unnamed();
        }
        return addr;
    }

    static UnixDomainSocketAddress localAddress(FileDescriptor fd) throws IOException {
        String path = new String(UnixDomainSockets.localAddress0(fd), UnixDomainSocketsUtil.getCharset());
        return UnixDomainSocketAddress.of(path);
    }

    private static native byte[] localAddress0(FileDescriptor var0) throws IOException;

    static String getRevealedLocalAddressAsString(SocketAddress sa) {
        return System.getSecurityManager() != null ? sa.toString() : "";
    }

    static UnixDomainSocketAddress checkAddress(SocketAddress sa) {
        if (sa == null) {
            throw new NullPointerException();
        }
        if (!(sa instanceof UnixDomainSocketAddress)) {
            throw new UnsupportedAddressTypeException();
        }
        return (UnixDomainSocketAddress)sa;
    }

    static byte[] getPathBytes(Path path) {
        FileSystemProvider provider = FileSystems.getDefault().provider();
        return ((AbstractFileSystemProvider)provider).getSunPathForSocketFile(path);
    }

    static FileDescriptor socket() throws IOException {
        return IOUtil.newFD(UnixDomainSockets.socket0());
    }

    static void bind(FileDescriptor fd, Path addr) throws IOException {
        byte[] path = UnixDomainSockets.getPathBytes(addr);
        if (path.length == 0) {
            throw new BindException("Server socket cannot bind to unnamed address");
        }
        UnixDomainSockets.bind0(fd, path);
    }

    private static Random getRandom() {
        try {
            return SecureRandom.getInstance("NativePRNGNonBlocking");
        }
        catch (NoSuchAlgorithmException e) {
            return new SecureRandom();
        }
    }

    static UnixDomainSocketAddress generateTempName() throws IOException {
        String dir = tempDir;
        if (dir == null) {
            throw new BindException("Could not locate temporary directory for sockets");
        }
        int rnd = random.nextInt(Integer.MAX_VALUE);
        try {
            Path path = Path.of(dir, "socket_" + rnd);
            if (path.getFileSystem().provider() != DefaultFileSystemProvider.instance()) {
                throw new UnsupportedOperationException("Unix Domain Sockets not supported on non-default file system");
            }
            return UnixDomainSocketAddress.of(path);
        }
        catch (InvalidPathException e) {
            throw new BindException("Invalid temporary directory");
        }
    }

    static int connect(FileDescriptor fd, SocketAddress sa) throws IOException {
        return UnixDomainSockets.connect(fd, ((UnixDomainSocketAddress)sa).getPath());
    }

    static int connect(FileDescriptor fd, Path path) throws IOException {
        return UnixDomainSockets.connect0(fd, UnixDomainSockets.getPathBytes(path));
    }

    static int accept(FileDescriptor fd, FileDescriptor newfd, String[] paths) throws IOException {
        Object[] array = new Object[1];
        int n = UnixDomainSockets.accept0(fd, newfd, array);
        if (n > 0) {
            byte[] bytes = (byte[])array[0];
            paths[0] = new String(bytes, UnixDomainSocketsUtil.getCharset());
        }
        return n;
    }

    static UnixDomainSocketAddress unnamed() {
        return UnnamedHolder.UNNAMED;
    }

    private static native boolean init();

    private static native int socket0() throws IOException;

    private static native void bind0(FileDescriptor var0, byte[] var1) throws IOException;

    private static native int connect0(FileDescriptor var0, byte[] var1) throws IOException;

    private static native int accept0(FileDescriptor var0, FileDescriptor var1, Object[] var2) throws IOException;

    static {
        tempDir = UnixDomainSocketsUtil.getTempDir();
        accessUnixDomainSocket = new NetPermission("accessUnixDomainSocket");
        random = UnixDomainSockets.getRandom();
        IOUtil.load();
        supported = UnixDomainSockets.init();
    }

    private static class UnnamedHolder {
        static final UnixDomainSocketAddress UNNAMED = UnixDomainSocketAddress.of("");

        private UnnamedHolder() {
        }
    }
}

