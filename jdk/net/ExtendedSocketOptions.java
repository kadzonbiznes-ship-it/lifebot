/*
 * Decompiled with CFR 0.152.
 */
package jdk.net;

import java.io.FileDescriptor;
import java.net.SocketException;
import java.net.SocketOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import jdk.internal.access.JavaIOFileDescriptorAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.util.OperatingSystem;
import jdk.net.UnixDomainPrincipal;

public final class ExtendedSocketOptions {
    public static final SocketOption<Boolean> TCP_QUICKACK = new ExtSocketOption<Boolean>("TCP_QUICKACK", Boolean.class);
    public static final SocketOption<Integer> TCP_KEEPIDLE = new ExtSocketOption<Integer>("TCP_KEEPIDLE", Integer.class);
    public static final SocketOption<Integer> TCP_KEEPINTERVAL = new ExtSocketOption<Integer>("TCP_KEEPINTERVAL", Integer.class);
    public static final SocketOption<Integer> TCP_KEEPCOUNT = new ExtSocketOption<Integer>("TCP_KEEPCOUNT", Integer.class);
    public static final SocketOption<Integer> SO_INCOMING_NAPI_ID = new ExtSocketOption<Integer>("SO_INCOMING_NAPI_ID", Integer.class);
    public static final SocketOption<UnixDomainPrincipal> SO_PEERCRED = new ExtSocketOption<UnixDomainPrincipal>("SO_PEERCRED", UnixDomainPrincipal.class);
    public static final SocketOption<Boolean> IP_DONTFRAGMENT = new ExtSocketOption<Boolean>("IP_DONTFRAGMENT", Boolean.class);
    private static final PlatformSocketOptions platformSocketOptions = PlatformSocketOptions.get();
    private static final boolean quickAckSupported = platformSocketOptions.quickAckSupported();
    private static final boolean keepAliveOptSupported = platformSocketOptions.keepAliveOptionsSupported();
    private static final boolean peerCredentialsSupported = platformSocketOptions.peerCredentialsSupported();
    private static final boolean incomingNapiIdOptSupported = platformSocketOptions.incomingNapiIdSupported();
    private static final boolean ipDontFragmentSupported = platformSocketOptions.ipDontFragmentSupported();
    private static final Set<SocketOption<?>> extendedOptions = ExtendedSocketOptions.options();
    private static final JavaIOFileDescriptorAccess fdAccess;

    private ExtendedSocketOptions() {
    }

    static Set<SocketOption<?>> options() {
        HashSet<SocketOption<Object>> options = new HashSet<SocketOption<Object>>();
        if (quickAckSupported) {
            options.add(TCP_QUICKACK);
        }
        if (incomingNapiIdOptSupported) {
            options.add(SO_INCOMING_NAPI_ID);
        }
        if (keepAliveOptSupported) {
            options.addAll(Set.of(TCP_KEEPCOUNT, TCP_KEEPIDLE, TCP_KEEPINTERVAL));
        }
        if (peerCredentialsSupported) {
            options.add(SO_PEERCRED);
        }
        if (ipDontFragmentSupported) {
            options.add(IP_DONTFRAGMENT);
        }
        return Collections.unmodifiableSet(options);
    }

    private static void setQuickAckOption(FileDescriptor fd, boolean enable) throws SocketException {
        platformSocketOptions.setQuickAck(fdAccess.get(fd), enable);
    }

    private static Object getSoPeerCred(FileDescriptor fd) throws SocketException {
        return platformSocketOptions.getSoPeerCred(fdAccess.get(fd));
    }

    private static Object getQuickAckOption(FileDescriptor fd) throws SocketException {
        return platformSocketOptions.getQuickAck(fdAccess.get(fd));
    }

    private static void setTcpKeepAliveProbes(FileDescriptor fd, int value) throws SocketException {
        platformSocketOptions.setTcpKeepAliveProbes(fdAccess.get(fd), value);
    }

    private static void setTcpKeepAliveTime(FileDescriptor fd, int value) throws SocketException {
        platformSocketOptions.setTcpKeepAliveTime(fdAccess.get(fd), value);
    }

    private static void setIpDontFragment(FileDescriptor fd, boolean value, boolean isIPv6) throws SocketException {
        platformSocketOptions.setIpDontFragment(fdAccess.get(fd), value, isIPv6);
    }

    private static void setTcpKeepAliveIntvl(FileDescriptor fd, int value) throws SocketException {
        platformSocketOptions.setTcpKeepAliveIntvl(fdAccess.get(fd), value);
    }

    private static int getTcpKeepAliveProbes(FileDescriptor fd) throws SocketException {
        return platformSocketOptions.getTcpKeepAliveProbes(fdAccess.get(fd));
    }

    private static boolean getIpDontFragment(FileDescriptor fd, boolean isIPv6) throws SocketException {
        return platformSocketOptions.getIpDontFragment(fdAccess.get(fd), isIPv6);
    }

    private static int getTcpKeepAliveTime(FileDescriptor fd) throws SocketException {
        return platformSocketOptions.getTcpKeepAliveTime(fdAccess.get(fd));
    }

    private static int getTcpKeepAliveIntvl(FileDescriptor fd) throws SocketException {
        return platformSocketOptions.getTcpKeepAliveIntvl(fdAccess.get(fd));
    }

    private static int getIncomingNapiId(FileDescriptor fd) throws SocketException {
        return platformSocketOptions.getIncomingNapiId(fdAccess.get(fd));
    }

    static {
        sun.net.ext.ExtendedSocketOptions.register(new sun.net.ext.ExtendedSocketOptions((Set)extendedOptions){

            @Override
            public void setOption(FileDescriptor fd, SocketOption<?> option, Object value, boolean isIPv6) throws SocketException {
                if (fd == null || !fd.valid()) {
                    throw new SocketException("socket closed");
                }
                if (option == TCP_QUICKACK) {
                    ExtendedSocketOptions.setQuickAckOption(fd, (Boolean)value);
                } else if (option == TCP_KEEPCOUNT) {
                    ExtendedSocketOptions.setTcpKeepAliveProbes(fd, (Integer)value);
                } else if (option == IP_DONTFRAGMENT) {
                    ExtendedSocketOptions.setIpDontFragment(fd, (Boolean)value, isIPv6);
                } else if (option == TCP_KEEPIDLE) {
                    ExtendedSocketOptions.setTcpKeepAliveTime(fd, (Integer)value);
                } else if (option == TCP_KEEPINTERVAL) {
                    ExtendedSocketOptions.setTcpKeepAliveIntvl(fd, (Integer)value);
                } else {
                    if (option == SO_INCOMING_NAPI_ID) {
                        if (!incomingNapiIdOptSupported) {
                            throw new UnsupportedOperationException("Attempt to set unsupported option " + String.valueOf(option));
                        }
                        throw new SocketException("Attempt to set read only option " + String.valueOf(option));
                    }
                    if (option == SO_PEERCRED) {
                        throw new SocketException("SO_PEERCRED cannot be set ");
                    }
                    throw new InternalError("Unexpected option " + String.valueOf(option));
                }
            }

            @Override
            public Object getOption(FileDescriptor fd, SocketOption<?> option, boolean isIPv6) throws SocketException {
                if (fd == null || !fd.valid()) {
                    throw new SocketException("socket closed");
                }
                if (option == TCP_QUICKACK) {
                    return ExtendedSocketOptions.getQuickAckOption(fd);
                }
                if (option == TCP_KEEPCOUNT) {
                    return ExtendedSocketOptions.getTcpKeepAliveProbes(fd);
                }
                if (option == IP_DONTFRAGMENT) {
                    return ExtendedSocketOptions.getIpDontFragment(fd, isIPv6);
                }
                if (option == TCP_KEEPIDLE) {
                    return ExtendedSocketOptions.getTcpKeepAliveTime(fd);
                }
                if (option == TCP_KEEPINTERVAL) {
                    return ExtendedSocketOptions.getTcpKeepAliveIntvl(fd);
                }
                if (option == SO_PEERCRED) {
                    return ExtendedSocketOptions.getSoPeerCred(fd);
                }
                if (option == SO_INCOMING_NAPI_ID) {
                    return ExtendedSocketOptions.getIncomingNapiId(fd);
                }
                throw new InternalError("Unexpected option " + String.valueOf(option));
            }
        });
        fdAccess = SharedSecrets.getJavaIOFileDescriptorAccess();
    }

    static class PlatformSocketOptions {
        private static final PlatformSocketOptions instance = PlatformSocketOptions.create();

        protected PlatformSocketOptions() {
        }

        private static PlatformSocketOptions newInstance(String cn) {
            try {
                Class<?> c = Class.forName(cn);
                return (PlatformSocketOptions)c.getConstructor(new Class[0]).newInstance(new Object[0]);
            }
            catch (ReflectiveOperationException x) {
                throw new AssertionError((Object)x);
            }
        }

        private static PlatformSocketOptions create() {
            return switch (OperatingSystem.current()) {
                case OperatingSystem.LINUX -> PlatformSocketOptions.newInstance("jdk.net.LinuxSocketOptions");
                case OperatingSystem.MACOS -> PlatformSocketOptions.newInstance("jdk.net.MacOSXSocketOptions");
                case OperatingSystem.WINDOWS -> PlatformSocketOptions.newInstance("jdk.net.WindowsSocketOptions");
                case OperatingSystem.AIX -> PlatformSocketOptions.newInstance("jdk.net.AIXSocketOptions");
                default -> new PlatformSocketOptions();
            };
        }

        static PlatformSocketOptions get() {
            return instance;
        }

        boolean peerCredentialsSupported() {
            return false;
        }

        void setQuickAck(int fd, boolean on) throws SocketException {
            throw new UnsupportedOperationException("unsupported TCP_QUICKACK option");
        }

        boolean getQuickAck(int fd) throws SocketException {
            throw new UnsupportedOperationException("unsupported TCP_QUICKACK option");
        }

        boolean quickAckSupported() {
            return false;
        }

        boolean keepAliveOptionsSupported() {
            return false;
        }

        boolean ipDontFragmentSupported() {
            return false;
        }

        void setTcpKeepAliveProbes(int fd, int value) throws SocketException {
            throw new UnsupportedOperationException("unsupported TCP_KEEPCNT option");
        }

        void setTcpKeepAliveTime(int fd, int value) throws SocketException {
            throw new UnsupportedOperationException("unsupported TCP_KEEPIDLE option");
        }

        UnixDomainPrincipal getSoPeerCred(int fd) throws SocketException {
            throw new UnsupportedOperationException("unsupported SO_PEERCRED option");
        }

        void setTcpKeepAliveIntvl(int fd, int value) throws SocketException {
            throw new UnsupportedOperationException("unsupported TCP_KEEPINTVL option");
        }

        void setIpDontFragment(int fd, boolean value, boolean isIPv6) throws SocketException {
            throw new UnsupportedOperationException("unsupported IP_DONTFRAGMENT option");
        }

        boolean getIpDontFragment(int fd, boolean isIPv6) throws SocketException {
            throw new UnsupportedOperationException("unsupported IP_DONTFRAGMENT option");
        }

        int getTcpKeepAliveProbes(int fd) throws SocketException {
            throw new UnsupportedOperationException("unsupported TCP_KEEPCNT option");
        }

        int getTcpKeepAliveTime(int fd) throws SocketException {
            throw new UnsupportedOperationException("unsupported TCP_KEEPIDLE option");
        }

        int getTcpKeepAliveIntvl(int fd) throws SocketException {
            throw new UnsupportedOperationException("unsupported TCP_KEEPINTVL option");
        }

        boolean incomingNapiIdSupported() {
            return false;
        }

        int getIncomingNapiId(int fd) throws SocketException {
            throw new UnsupportedOperationException("unsupported SO_INCOMING_NAPI_ID socket option");
        }
    }

    private static class ExtSocketOption<T>
    implements SocketOption<T> {
        private final String name;
        private final Class<T> type;

        ExtSocketOption(String name, Class<T> type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public Class<T> type() {
            return this.type;
        }

        public String toString() {
            return this.name;
        }
    }
}

