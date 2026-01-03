/*
 * Decompiled with CFR 0.152.
 */
package java.net.spi;

import java.net.spi.InetAddressResolver;
import sun.net.ResolverProviderConfiguration;

public abstract class InetAddressResolverProvider {
    private static final RuntimePermission INET_ADDRESS_RESOLVER_PERMISSION = new RuntimePermission("inetAddressResolverProvider");

    public abstract InetAddressResolver get(Configuration var1);

    public abstract String name();

    protected InetAddressResolverProvider() {
        this(InetAddressResolverProvider.checkPermission());
    }

    private InetAddressResolverProvider(Void unused) {
    }

    private static Void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(INET_ADDRESS_RESOLVER_PERMISSION);
        }
        return null;
    }

    public static sealed interface Configuration
    permits ResolverProviderConfiguration {
        public InetAddressResolver builtinResolver();

        public String lookupLocalHostName();
    }
}

