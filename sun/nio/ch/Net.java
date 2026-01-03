/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ProtocolFamily;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Objects;
import sun.net.ext.ExtendedSocketOptions;
import sun.net.util.IPAddressUtil;
import sun.nio.ch.IOUtil;
import sun.nio.ch.OptionKey;
import sun.nio.ch.SocketOptionRegistry;
import sun.security.action.GetPropertyAction;

public class Net {
    static final ProtocolFamily UNSPEC;
    private static final boolean EXCLUSIVE_BIND;
    private static final boolean FAST_LOOPBACK;
    private static final boolean IPV6_AVAILABLE;
    private static final boolean SO_REUSEPORT_AVAILABLE;
    private static final boolean SHUTDOWN_WRITE_BEFORE_CLOSE;
    private static final InetAddress ANY_LOCAL_INET4ADDRESS;
    private static final InetAddress ANY_LOCAL_INET6ADDRESS;
    private static final InetAddress INET4_LOOPBACK_ADDRESS;
    private static final InetAddress INET6_LOOPBACK_ADDRESS;
    static final ExtendedSocketOptions EXTENDED_OPTIONS;
    public static final int SHUT_RD = 0;
    public static final int SHUT_WR = 1;
    public static final int SHUT_RDWR = 2;
    public static final short POLLIN;
    public static final short POLLOUT;
    public static final short POLLERR;
    public static final short POLLHUP;
    public static final short POLLNVAL;
    public static final short POLLCONN;

    private Net() {
    }

    static boolean isIPv6Available() {
        return IPV6_AVAILABLE;
    }

    static boolean isReusePortAvailable() {
        return SO_REUSEPORT_AVAILABLE;
    }

    static boolean useExclusiveBind() {
        return EXCLUSIVE_BIND;
    }

    static boolean shouldShutdownWriteBeforeClose() {
        return SHUTDOWN_WRITE_BEFORE_CLOSE;
    }

    static boolean shouldSetBothIPv4AndIPv6Options() {
        return Net.shouldSetBothIPv4AndIPv6Options0();
    }

    static boolean canIPv6SocketJoinIPv4Group() {
        return Net.canIPv6SocketJoinIPv4Group0();
    }

    static boolean canJoin6WithIPv4Group() {
        return Net.canJoin6WithIPv4Group0();
    }

    static boolean canUseIPv6OptionsWithIPv4LocalAddress() {
        return Net.canUseIPv6OptionsWithIPv4LocalAddress0();
    }

    public static InetSocketAddress checkAddress(SocketAddress sa) {
        Objects.requireNonNull(sa);
        if (!(sa instanceof InetSocketAddress)) {
            throw new UnsupportedAddressTypeException();
        }
        InetSocketAddress isa = (InetSocketAddress)sa;
        if (isa.isUnresolved()) {
            throw new UnresolvedAddressException();
        }
        InetAddress addr = isa.getAddress();
        if (!(addr instanceof Inet4Address) && !(addr instanceof Inet6Address)) {
            throw new IllegalArgumentException("Invalid address type: " + addr.getClass().getName());
        }
        return isa;
    }

    static InetSocketAddress checkAddress(SocketAddress sa, ProtocolFamily family) {
        InetAddress addr;
        InetSocketAddress isa = Net.checkAddress(sa);
        if (family == StandardProtocolFamily.INET && !((addr = isa.getAddress()) instanceof Inet4Address)) {
            throw new UnsupportedAddressTypeException();
        }
        return isa;
    }

    static InetSocketAddress asInetSocketAddress(SocketAddress sa) {
        if (!(sa instanceof InetSocketAddress)) {
            throw new UnsupportedAddressTypeException();
        }
        InetSocketAddress isa = (InetSocketAddress)sa;
        return isa;
    }

    static void translateToSocketException(Exception x) throws SocketException {
        if (x instanceof SocketException) {
            SocketException se = (SocketException)x;
            throw se;
        }
        Exception nx = x;
        if (x instanceof ClosedChannelException) {
            nx = Net.newSocketException("Socket is closed");
        } else if (x instanceof NotYetConnectedException) {
            nx = Net.newSocketException("Socket is not connected");
        } else if (x instanceof AlreadyBoundException) {
            nx = Net.newSocketException("Already bound");
        } else if (x instanceof NotYetBoundException) {
            nx = Net.newSocketException("Socket is not bound yet");
        } else if (x instanceof UnsupportedAddressTypeException) {
            nx = Net.newSocketException("Unsupported address type");
        } else if (x instanceof UnresolvedAddressException) {
            nx = Net.newSocketException("Unresolved address");
        } else if (x instanceof IOException) {
            nx = Net.newSocketException(x.getMessage());
        }
        if (nx != x) {
            nx.initCause(x);
        }
        if (nx instanceof SocketException) {
            SocketException se = (SocketException)nx;
            throw se;
        }
        if (nx instanceof RuntimeException) {
            RuntimeException re = (RuntimeException)nx;
            throw re;
        }
        throw new Error("Untranslated exception", nx);
    }

    private static SocketException newSocketException(String msg) {
        return new SocketException(msg);
    }

    static void translateException(Exception x, boolean unknownHostForUnresolved) throws IOException {
        if (x instanceof IOException) {
            IOException ioe = (IOException)x;
            throw ioe;
        }
        if (unknownHostForUnresolved && x instanceof UnresolvedAddressException) {
            throw new UnknownHostException();
        }
        Net.translateToSocketException(x);
    }

    static void translateException(Exception x) throws IOException {
        Net.translateException(x, false);
    }

    static InetSocketAddress getRevealedLocalAddress(SocketAddress sa) {
        InetSocketAddress isa = (InetSocketAddress)sa;
        SecurityManager sm = System.getSecurityManager();
        if (isa != null && sm != null) {
            try {
                sm.checkConnect(isa.getAddress().getHostAddress(), -1);
            }
            catch (SecurityException e) {
                isa = Net.getLoopbackAddress(isa.getPort());
            }
        }
        return isa;
    }

    static String getRevealedLocalAddressAsString(SocketAddress sa) {
        InetSocketAddress isa = (InetSocketAddress)sa;
        if (System.getSecurityManager() == null) {
            return isa.toString();
        }
        return Net.getLoopbackAddress(isa.getPort()).toString();
    }

    private static InetSocketAddress getLoopbackAddress(int port) {
        return new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
    }

    static InetAddress inet4LoopbackAddress() {
        return INET4_LOOPBACK_ADDRESS;
    }

    static InetAddress inet6LoopbackAddress() {
        return INET6_LOOPBACK_ADDRESS;
    }

    static InetAddress anyLocalAddress(ProtocolFamily family) {
        if (family == StandardProtocolFamily.INET) {
            return ANY_LOCAL_INET4ADDRESS;
        }
        if (family == StandardProtocolFamily.INET6) {
            return ANY_LOCAL_INET6ADDRESS;
        }
        throw new IllegalArgumentException();
    }

    static Inet4Address anyInet4Address(final NetworkInterface interf) {
        return AccessController.doPrivileged(new PrivilegedAction<Inet4Address>(){

            @Override
            public Inet4Address run() {
                Enumeration<InetAddress> addrs = interf.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (!(addr instanceof Inet4Address)) continue;
                    Inet4Address inet4Address = (Inet4Address)addr;
                    return inet4Address;
                }
                return null;
            }
        });
    }

    static int inet4AsInt(InetAddress ia) {
        if (ia instanceof Inet4Address) {
            byte[] addr = ia.getAddress();
            int address = addr[3] & 0xFF;
            address |= addr[2] << 8 & 0xFF00;
            address |= addr[1] << 16 & 0xFF0000;
            return address |= addr[0] << 24 & 0xFF000000;
        }
        throw Net.shouldNotReachHere();
    }

    static InetAddress inet4FromInt(int address) {
        byte[] addr = new byte[]{(byte)(address >>> 24 & 0xFF), (byte)(address >>> 16 & 0xFF), (byte)(address >>> 8 & 0xFF), (byte)(address & 0xFF)};
        try {
            return InetAddress.getByAddress(addr);
        }
        catch (UnknownHostException uhe) {
            throw Net.shouldNotReachHere();
        }
    }

    static byte[] inet6AsByteArray(InetAddress ia) {
        if (ia instanceof Inet6Address) {
            return ia.getAddress();
        }
        if (ia instanceof Inet4Address) {
            byte[] ip4address = ia.getAddress();
            byte[] address = new byte[16];
            address[10] = -1;
            address[11] = -1;
            address[12] = ip4address[0];
            address[13] = ip4address[1];
            address[14] = ip4address[2];
            address[15] = ip4address[3];
            return address;
        }
        throw Net.shouldNotReachHere();
    }

    static void setSocketOption(FileDescriptor fd, SocketOption<?> name, Object value) throws IOException {
        Net.setSocketOption(fd, UNSPEC, name, value);
    }

    static void setSocketOption(FileDescriptor fd, ProtocolFamily family, SocketOption<?> name, Object value) throws IOException {
        boolean b;
        int i;
        boolean isIPv6;
        if (value == null) {
            throw new IllegalArgumentException("Invalid option value");
        }
        Class<?> type = name.type();
        boolean bl = isIPv6 = family == StandardProtocolFamily.INET6;
        if (EXTENDED_OPTIONS.isOptionSupported(name)) {
            EXTENDED_OPTIONS.setOption(fd, name, value, isIPv6);
            return;
        }
        if (type != Integer.class && type != Boolean.class) {
            throw Net.shouldNotReachHere();
        }
        if ((name == StandardSocketOptions.SO_RCVBUF || name == StandardSocketOptions.SO_SNDBUF) && (i = ((Integer)value).intValue()) < 0) {
            throw new IllegalArgumentException("Invalid send/receive buffer size");
        }
        if (name == StandardSocketOptions.SO_LINGER) {
            i = (Integer)value;
            if (i < 0) {
                value = -1;
            }
            if (i > 65535) {
                value = 65535;
            }
        }
        if (name == StandardSocketOptions.IP_TOS && ((i = ((Integer)value).intValue()) < 0 || i > 255)) {
            throw new IllegalArgumentException("Invalid IP_TOS value");
        }
        if (name == StandardSocketOptions.IP_MULTICAST_TTL && ((i = ((Integer)value).intValue()) < 0 || i > 255)) {
            throw new IllegalArgumentException("Invalid TTL/hop value");
        }
        OptionKey key = SocketOptionRegistry.findOption(name, family);
        if (key == null) {
            throw new AssertionError((Object)"Option not found");
        }
        int arg = type == Integer.class ? (Integer)value : ((b = ((Boolean)value).booleanValue()) ? 1 : 0);
        boolean mayNeedConversion = family == UNSPEC;
        Net.setIntOption0(fd, mayNeedConversion, key.level(), key.name(), arg, isIPv6);
    }

    static <T> void setIpSocketOption(FileDescriptor fd, ProtocolFamily family, SocketOption<T> opt, T value) throws IOException {
        Net.setSocketOption(fd, family, opt, value);
        if (family == StandardProtocolFamily.INET6 && Net.shouldSetBothIPv4AndIPv6Options()) {
            try {
                Net.setSocketOption(fd, StandardProtocolFamily.INET, opt, value);
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    }

    static Object getSocketOption(FileDescriptor fd, SocketOption<?> name) throws IOException {
        return Net.getSocketOption(fd, UNSPEC, name);
    }

    static Object getSocketOption(FileDescriptor fd, ProtocolFamily family, SocketOption<?> name) throws IOException {
        Class<?> type = name.type();
        if (EXTENDED_OPTIONS.isOptionSupported(name)) {
            boolean isIPv6 = family == StandardProtocolFamily.INET6;
            return EXTENDED_OPTIONS.getOption(fd, name, isIPv6);
        }
        if (type != Integer.class && type != Boolean.class) {
            throw Net.shouldNotReachHere();
        }
        OptionKey key = SocketOptionRegistry.findOption(name, family);
        if (key == null) {
            throw new AssertionError((Object)"Option not found");
        }
        boolean mayNeedConversion = family == UNSPEC;
        int value = Net.getIntOption0(fd, mayNeedConversion, key.level(), key.name());
        if (type == Integer.class) {
            return value;
        }
        return value == 0 ? Boolean.FALSE : Boolean.TRUE;
    }

    private static boolean isFastTcpLoopbackRequested() {
        String loopbackProp = GetPropertyAction.privilegedGetProperty("jdk.net.useFastTcpLoopback", "false");
        return loopbackProp.isEmpty() || Boolean.parseBoolean(loopbackProp);
    }

    private static native boolean isIPv6Available0();

    private static native boolean isReusePortAvailable0();

    private static native int isExclusiveBindAvailable();

    private static native boolean shouldShutdownWriteBeforeClose0();

    private static native boolean shouldSetBothIPv4AndIPv6Options0();

    private static native boolean canIPv6SocketJoinIPv4Group0();

    private static native boolean canJoin6WithIPv4Group0();

    private static native boolean canUseIPv6OptionsWithIPv4LocalAddress0();

    static FileDescriptor socket(boolean stream) throws IOException {
        return Net.socket(UNSPEC, stream);
    }

    static FileDescriptor socket(ProtocolFamily family, boolean stream) throws IOException {
        boolean preferIPv6 = Net.isIPv6Available() && family != StandardProtocolFamily.INET;
        return IOUtil.newFD(Net.socket0(preferIPv6, stream, false, FAST_LOOPBACK));
    }

    static FileDescriptor serverSocket(boolean stream) {
        return Net.serverSocket(UNSPEC, stream);
    }

    static FileDescriptor serverSocket(ProtocolFamily family, boolean stream) {
        boolean preferIPv6 = Net.isIPv6Available() && family != StandardProtocolFamily.INET;
        return IOUtil.newFD(Net.socket0(preferIPv6, stream, true, FAST_LOOPBACK));
    }

    private static native int socket0(boolean var0, boolean var1, boolean var2, boolean var3);

    public static void bind(FileDescriptor fd, InetAddress addr, int port) throws IOException {
        Net.bind(UNSPEC, fd, addr, port);
    }

    static void bind(ProtocolFamily family, FileDescriptor fd, InetAddress addr, int port) throws IOException {
        boolean preferIPv6;
        boolean bl = preferIPv6 = Net.isIPv6Available() && family != StandardProtocolFamily.INET;
        if (addr.isLinkLocalAddress()) {
            addr = IPAddressUtil.toScopedAddress(addr);
        }
        Net.bind0(fd, preferIPv6, EXCLUSIVE_BIND, addr, port);
    }

    private static native void bind0(FileDescriptor var0, boolean var1, boolean var2, InetAddress var3, int var4) throws IOException;

    static native void listen(FileDescriptor var0, int var1) throws IOException;

    static int connect(FileDescriptor fd, InetAddress remote, int remotePort) throws IOException {
        return Net.connect(UNSPEC, fd, remote, remotePort);
    }

    static int connect(ProtocolFamily family, FileDescriptor fd, InetAddress remote, int remotePort) throws IOException {
        if (remote.isLinkLocalAddress()) {
            remote = IPAddressUtil.toScopedAddress(remote);
        }
        boolean preferIPv6 = Net.isIPv6Available() && family != StandardProtocolFamily.INET;
        return Net.connect0(preferIPv6, fd, remote, remotePort);
    }

    static int connect(ProtocolFamily family, FileDescriptor fd, SocketAddress remote) throws IOException {
        InetSocketAddress isa = (InetSocketAddress)remote;
        return Net.connect(family, fd, isa.getAddress(), isa.getPort());
    }

    private static native int connect0(boolean var0, FileDescriptor var1, InetAddress var2, int var3) throws IOException;

    public static native int accept(FileDescriptor var0, FileDescriptor var1, InetSocketAddress[] var2) throws IOException;

    static native void shutdown(FileDescriptor var0, int var1) throws IOException;

    private static native int localPort(FileDescriptor var0) throws IOException;

    private static native InetAddress localInetAddress(FileDescriptor var0) throws IOException;

    public static InetSocketAddress localAddress(FileDescriptor fd) throws IOException {
        return new InetSocketAddress(Net.localInetAddress(fd), Net.localPort(fd));
    }

    private static native int remotePort(FileDescriptor var0) throws IOException;

    private static native InetAddress remoteInetAddress(FileDescriptor var0) throws IOException;

    static InetSocketAddress remoteAddress(FileDescriptor fd) throws IOException {
        return new InetSocketAddress(Net.remoteInetAddress(fd), Net.remotePort(fd));
    }

    private static native int getIntOption0(FileDescriptor var0, boolean var1, int var2, int var3) throws IOException;

    private static native void setIntOption0(FileDescriptor var0, boolean var1, int var2, int var3, int var4, boolean var5) throws IOException;

    static native int poll(FileDescriptor var0, int var1, long var2) throws IOException;

    static int pollNow(FileDescriptor fd, int events) throws IOException {
        return Net.poll(fd, events, 0L);
    }

    public static native boolean pollConnect(FileDescriptor var0, long var1) throws IOException;

    static boolean pollConnectNow(FileDescriptor fd) throws IOException {
        return Net.pollConnect(fd, 0L);
    }

    static native int available(FileDescriptor var0) throws IOException;

    static native int sendOOB(FileDescriptor var0, byte var1) throws IOException;

    static native boolean discardOOB(FileDescriptor var0) throws IOException;

    static int join4(FileDescriptor fd, int group, int interf, int source) throws IOException {
        return Net.joinOrDrop4(true, fd, group, interf, source);
    }

    static void drop4(FileDescriptor fd, int group, int interf, int source) throws IOException {
        Net.joinOrDrop4(false, fd, group, interf, source);
    }

    private static native int joinOrDrop4(boolean var0, FileDescriptor var1, int var2, int var3, int var4) throws IOException;

    static int block4(FileDescriptor fd, int group, int interf, int source) throws IOException {
        return Net.blockOrUnblock4(true, fd, group, interf, source);
    }

    static void unblock4(FileDescriptor fd, int group, int interf, int source) throws IOException {
        Net.blockOrUnblock4(false, fd, group, interf, source);
    }

    private static native int blockOrUnblock4(boolean var0, FileDescriptor var1, int var2, int var3, int var4) throws IOException;

    static int join6(FileDescriptor fd, byte[] group, int index, byte[] source) throws IOException {
        return Net.joinOrDrop6(true, fd, group, index, source);
    }

    static void drop6(FileDescriptor fd, byte[] group, int index, byte[] source) throws IOException {
        Net.joinOrDrop6(false, fd, group, index, source);
    }

    private static native int joinOrDrop6(boolean var0, FileDescriptor var1, byte[] var2, int var3, byte[] var4) throws IOException;

    static int block6(FileDescriptor fd, byte[] group, int index, byte[] source) throws IOException {
        return Net.blockOrUnblock6(true, fd, group, index, source);
    }

    static void unblock6(FileDescriptor fd, byte[] group, int index, byte[] source) throws IOException {
        Net.blockOrUnblock6(false, fd, group, index, source);
    }

    static native int blockOrUnblock6(boolean var0, FileDescriptor var1, byte[] var2, int var3, byte[] var4) throws IOException;

    static native void setInterface4(FileDescriptor var0, int var1) throws IOException;

    static native int getInterface4(FileDescriptor var0) throws IOException;

    static native void setInterface6(FileDescriptor var0, int var1) throws IOException;

    static native int getInterface6(FileDescriptor var0) throws IOException;

    private static native void initIDs();

    static native short pollinValue();

    static native short polloutValue();

    static native short pollerrValue();

    static native short pollhupValue();

    static native short pollnvalValue();

    static native short pollconnValue();

    private static AssertionError shouldNotReachHere() {
        return new AssertionError((Object)"Should not reach here");
    }

    static {
        String exclBindProp;
        UNSPEC = new ProtocolFamily(){

            @Override
            public String name() {
                return "UNSPEC";
            }
        };
        try {
            ANY_LOCAL_INET4ADDRESS = Net.inet4FromInt(0);
            assert (ANY_LOCAL_INET4ADDRESS instanceof Inet4Address && ANY_LOCAL_INET4ADDRESS.isAnyLocalAddress());
            ANY_LOCAL_INET6ADDRESS = InetAddress.getByAddress(new byte[16]);
            assert (ANY_LOCAL_INET6ADDRESS instanceof Inet6Address && ANY_LOCAL_INET6ADDRESS.isAnyLocalAddress());
            INET4_LOOPBACK_ADDRESS = Net.inet4FromInt(2130706433);
            assert (INET4_LOOPBACK_ADDRESS instanceof Inet4Address && INET4_LOOPBACK_ADDRESS.isLoopbackAddress());
            byte[] bytes = new byte[16];
            bytes[15] = 1;
            INET6_LOOPBACK_ADDRESS = InetAddress.getByAddress(bytes);
            assert (INET6_LOOPBACK_ADDRESS instanceof Inet6Address && INET6_LOOPBACK_ADDRESS.isLoopbackAddress());
        }
        catch (Exception e) {
            throw new InternalError(e);
        }
        EXTENDED_OPTIONS = ExtendedSocketOptions.getInstance();
        IOUtil.load();
        Net.initIDs();
        POLLIN = Net.pollinValue();
        POLLOUT = Net.polloutValue();
        POLLERR = Net.pollerrValue();
        POLLHUP = Net.pollhupValue();
        POLLNVAL = Net.pollnvalValue();
        POLLCONN = Net.pollconnValue();
        int availLevel = Net.isExclusiveBindAvailable();
        EXCLUSIVE_BIND = availLevel >= 0 ? ((exclBindProp = GetPropertyAction.privilegedGetProperty("sun.net.useExclusiveBind")) != null ? exclBindProp.isEmpty() || Boolean.parseBoolean(exclBindProp) : availLevel == 1) : false;
        FAST_LOOPBACK = Net.isFastTcpLoopbackRequested();
        IPV6_AVAILABLE = Net.isIPv6Available0();
        SO_REUSEPORT_AVAILABLE = Net.isReusePortAvailable0();
        SHUTDOWN_WRITE_BEFORE_CLOSE = Net.shouldShutdownWriteBeforeClose0();
    }
}

