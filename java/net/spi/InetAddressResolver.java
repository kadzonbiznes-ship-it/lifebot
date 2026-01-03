/*
 * Decompiled with CFR 0.152.
 */
package java.net.spi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.stream.Stream;

public interface InetAddressResolver {
    public Stream<InetAddress> lookupByName(String var1, LookupPolicy var2) throws UnknownHostException;

    public String lookupByAddress(byte[] var1) throws UnknownHostException;

    public static final class LookupPolicy {
        public static final int IPV4 = 1;
        public static final int IPV6 = 2;
        public static final int IPV4_FIRST = 4;
        public static final int IPV6_FIRST = 8;
        private final int characteristics;

        private LookupPolicy(int characteristics) {
            this.characteristics = characteristics;
        }

        public static LookupPolicy of(int characteristics) {
            if ((characteristics & 1) == 0 && (characteristics & 2) == 0) {
                throw new IllegalArgumentException("No address type specified");
            }
            if ((characteristics & 4) != 0 && (characteristics & 8) != 0) {
                throw new IllegalArgumentException("Addresses order cannot be determined");
            }
            if ((characteristics & 4) != 0 && (characteristics & 1) == 0) {
                throw new IllegalArgumentException("Addresses order and type do not match");
            }
            if ((characteristics & 8) != 0 && (characteristics & 2) == 0) {
                throw new IllegalArgumentException("Addresses order and type do not match");
            }
            return new LookupPolicy(characteristics);
        }

        public int characteristics() {
            return this.characteristics;
        }
    }
}

