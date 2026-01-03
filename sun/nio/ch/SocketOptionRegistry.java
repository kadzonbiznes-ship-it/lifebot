/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.net.ProtocolFamily;
import java.net.SocketOption;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.util.HashMap;
import java.util.Map;
import sun.nio.ch.ExtendedSocketOption;
import sun.nio.ch.Net;
import sun.nio.ch.OptionKey;

class SocketOptionRegistry {
    private SocketOptionRegistry() {
    }

    public static OptionKey findOption(SocketOption<?> name, ProtocolFamily family) {
        RegistryKey key = new RegistryKey(name, family);
        return LazyInitialization.options.get(key);
    }

    private static class RegistryKey {
        private final SocketOption<?> name;
        private final ProtocolFamily family;

        RegistryKey(SocketOption<?> name, ProtocolFamily family) {
            this.name = name;
            this.family = family;
        }

        public int hashCode() {
            return this.name.hashCode() + this.family.hashCode();
        }

        public boolean equals(Object ob) {
            if (ob == null) {
                return false;
            }
            if (!(ob instanceof RegistryKey)) {
                return false;
            }
            RegistryKey other = (RegistryKey)ob;
            if (this.name != other.name) {
                return false;
            }
            return this.family == other.family;
        }
    }

    private static class LazyInitialization {
        static final Map<RegistryKey, OptionKey> options = LazyInitialization.options();

        private LazyInitialization() {
        }

        private static Map<RegistryKey, OptionKey> options() {
            HashMap<RegistryKey, OptionKey> map = new HashMap<RegistryKey, OptionKey>();
            map.put(new RegistryKey(StandardSocketOptions.SO_BROADCAST, Net.UNSPEC), new OptionKey(65535, 32));
            map.put(new RegistryKey(StandardSocketOptions.SO_KEEPALIVE, Net.UNSPEC), new OptionKey(65535, 8));
            map.put(new RegistryKey(StandardSocketOptions.SO_LINGER, Net.UNSPEC), new OptionKey(65535, 128));
            map.put(new RegistryKey(StandardSocketOptions.SO_SNDBUF, Net.UNSPEC), new OptionKey(65535, 4097));
            map.put(new RegistryKey(StandardSocketOptions.SO_RCVBUF, Net.UNSPEC), new OptionKey(65535, 4098));
            map.put(new RegistryKey(StandardSocketOptions.SO_REUSEADDR, Net.UNSPEC), new OptionKey(65535, 4));
            map.put(new RegistryKey(StandardSocketOptions.SO_REUSEPORT, Net.UNSPEC), new OptionKey(65535, 0));
            map.put(new RegistryKey(StandardSocketOptions.TCP_NODELAY, Net.UNSPEC), new OptionKey(6, 1));
            map.put(new RegistryKey(StandardSocketOptions.IP_TOS, StandardProtocolFamily.INET), new OptionKey(0, 3));
            map.put(new RegistryKey(StandardSocketOptions.IP_MULTICAST_IF, StandardProtocolFamily.INET), new OptionKey(0, 9));
            map.put(new RegistryKey(StandardSocketOptions.IP_MULTICAST_TTL, StandardProtocolFamily.INET), new OptionKey(0, 10));
            map.put(new RegistryKey(StandardSocketOptions.IP_MULTICAST_LOOP, StandardProtocolFamily.INET), new OptionKey(0, 11));
            map.put(new RegistryKey(StandardSocketOptions.IP_TOS, StandardProtocolFamily.INET6), new OptionKey(41, 39));
            map.put(new RegistryKey(StandardSocketOptions.IP_MULTICAST_IF, StandardProtocolFamily.INET6), new OptionKey(41, 9));
            map.put(new RegistryKey(StandardSocketOptions.IP_MULTICAST_TTL, StandardProtocolFamily.INET6), new OptionKey(41, 10));
            map.put(new RegistryKey(StandardSocketOptions.IP_MULTICAST_LOOP, StandardProtocolFamily.INET6), new OptionKey(41, 11));
            map.put(new RegistryKey(ExtendedSocketOption.SO_OOBINLINE, Net.UNSPEC), new OptionKey(65535, 256));
            return map;
        }
    }
}

