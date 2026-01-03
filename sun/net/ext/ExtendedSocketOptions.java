/*
 * Decompiled with CFR 0.152.
 */
package sun.net.ext;

import java.io.FileDescriptor;
import java.net.SocketException;
import java.net.SocketOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class ExtendedSocketOptions {
    public static final short SOCK_STREAM = 1;
    public static final short SOCK_DGRAM = 2;
    private final Set<SocketOption<?>> options;
    private final Set<SocketOption<?>> datagramOptions;
    private final Set<SocketOption<?>> clientStreamOptions;
    private final Set<SocketOption<?>> serverStreamOptions;
    private final Set<SocketOption<?>> unixDomainClientOptions;
    private static volatile ExtendedSocketOptions instance;

    public final boolean isOptionSupported(SocketOption<?> option) {
        return this.options().contains(option);
    }

    public final Set<SocketOption<?>> options() {
        return this.options;
    }

    public static Set<SocketOption<?>> serverSocketOptions() {
        return ExtendedSocketOptions.getInstance().options0((short)1, true);
    }

    public static Set<SocketOption<?>> clientSocketOptions() {
        return ExtendedSocketOptions.getInstance().options0((short)1, false);
    }

    private final Set<SocketOption<?>> unixDomainClientOptions() {
        return this.unixDomainClientOptions;
    }

    public static Set<SocketOption<?>> unixDomainSocketOptions() {
        return ExtendedSocketOptions.getInstance().unixDomainClientOptions();
    }

    public static Set<SocketOption<?>> datagramSocketOptions() {
        return ExtendedSocketOptions.getInstance().options0((short)2, false);
    }

    private static boolean isDatagramOption(SocketOption<?> option) {
        return !option.name().startsWith("TCP_") && !ExtendedSocketOptions.isUnixDomainOption(option);
    }

    private static boolean isUnixDomainOption(SocketOption<?> option) {
        return option.name().equals("SO_PEERCRED");
    }

    private static boolean isStreamOption(SocketOption<?> option, boolean server) {
        return !option.name().startsWith("UDP_") && !ExtendedSocketOptions.isUnixDomainOption(option) && !option.name().equals("IP_DONTFRAGMENT");
    }

    private Set<SocketOption<?>> options0(short type, boolean server) {
        switch (type) {
            case 2: {
                return this.datagramOptions;
            }
            case 1: {
                if (server) {
                    return this.serverStreamOptions;
                }
                return this.clientStreamOptions;
            }
        }
        throw new IllegalArgumentException("Invalid socket option type");
    }

    public abstract void setOption(FileDescriptor var1, SocketOption<?> var2, Object var3, boolean var4) throws SocketException;

    public abstract Object getOption(FileDescriptor var1, SocketOption<?> var2, boolean var3) throws SocketException;

    protected ExtendedSocketOptions(Set<SocketOption<?>> options) {
        this.options = options;
        HashSet datagramOptions = new HashSet();
        HashSet serverStreamOptions = new HashSet();
        HashSet clientStreamOptions = new HashSet();
        HashSet unixDomainClientOptions = new HashSet();
        for (SocketOption<?> option : options) {
            if (ExtendedSocketOptions.isDatagramOption(option)) {
                datagramOptions.add(option);
            }
            if (ExtendedSocketOptions.isStreamOption(option, true)) {
                serverStreamOptions.add(option);
            }
            if (ExtendedSocketOptions.isStreamOption(option, false)) {
                clientStreamOptions.add(option);
            }
            if (!ExtendedSocketOptions.isUnixDomainOption(option)) continue;
            unixDomainClientOptions.add(option);
        }
        this.datagramOptions = Set.copyOf(datagramOptions);
        this.serverStreamOptions = Set.copyOf(serverStreamOptions);
        this.clientStreamOptions = Set.copyOf(clientStreamOptions);
        this.unixDomainClientOptions = Set.copyOf(unixDomainClientOptions);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static ExtendedSocketOptions getInstance() {
        ExtendedSocketOptions ext = instance;
        if (ext != null) {
            return ext;
        }
        try {
            Class<?> c = Class.forName("jdk.net.ExtendedSocketOptions");
            return instance;
        }
        catch (ClassNotFoundException e) {
            Class<ExtendedSocketOptions> clazz = ExtendedSocketOptions.class;
            synchronized (ExtendedSocketOptions.class) {
                ext = instance;
                instance = new NoExtendedSocketOptions();
                if (ext == null) return instance;
                // ** MonitorExit[var2_3] (shouldn't be in output)
                return ext;
            }
        }
    }

    public static synchronized void register(ExtendedSocketOptions extOptions) {
        if (instance != null) {
            throw new InternalError("Attempting to reregister extended options");
        }
        instance = extOptions;
    }

    static final class NoExtendedSocketOptions
    extends ExtendedSocketOptions {
        NoExtendedSocketOptions() {
            super(Collections.emptySet());
        }

        @Override
        public void setOption(FileDescriptor fd, SocketOption<?> option, Object value, boolean isIPv6) throws SocketException {
            throw new UnsupportedOperationException("no extended options: " + option.name());
        }

        @Override
        public Object getOption(FileDescriptor fd, SocketOption<?> option, boolean isIPv6) throws SocketException {
            throw new UnsupportedOperationException("no extended options: " + option.name());
        }
    }
}

