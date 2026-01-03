/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.net.NetworkInterface;
import java.net.SocketOption;

public final class StandardSocketOptions {
    public static final SocketOption<Boolean> SO_BROADCAST = new StdSocketOption<Boolean>("SO_BROADCAST", Boolean.class);
    public static final SocketOption<Boolean> SO_KEEPALIVE = new StdSocketOption<Boolean>("SO_KEEPALIVE", Boolean.class);
    public static final SocketOption<Integer> SO_SNDBUF = new StdSocketOption<Integer>("SO_SNDBUF", Integer.class);
    public static final SocketOption<Integer> SO_RCVBUF = new StdSocketOption<Integer>("SO_RCVBUF", Integer.class);
    public static final SocketOption<Boolean> SO_REUSEADDR = new StdSocketOption<Boolean>("SO_REUSEADDR", Boolean.class);
    public static final SocketOption<Boolean> SO_REUSEPORT = new StdSocketOption<Boolean>("SO_REUSEPORT", Boolean.class);
    public static final SocketOption<Integer> SO_LINGER = new StdSocketOption<Integer>("SO_LINGER", Integer.class);
    public static final SocketOption<Integer> IP_TOS = new StdSocketOption<Integer>("IP_TOS", Integer.class);
    public static final SocketOption<NetworkInterface> IP_MULTICAST_IF = new StdSocketOption<NetworkInterface>("IP_MULTICAST_IF", NetworkInterface.class);
    public static final SocketOption<Integer> IP_MULTICAST_TTL = new StdSocketOption<Integer>("IP_MULTICAST_TTL", Integer.class);
    public static final SocketOption<Boolean> IP_MULTICAST_LOOP = new StdSocketOption<Boolean>("IP_MULTICAST_LOOP", Boolean.class);
    public static final SocketOption<Boolean> TCP_NODELAY = new StdSocketOption<Boolean>("TCP_NODELAY", Boolean.class);

    private StandardSocketOptions() {
    }

    private static class StdSocketOption<T>
    implements SocketOption<T> {
        private final String name;
        private final Class<T> type;

        StdSocketOption(String name, Class<T> type) {
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

