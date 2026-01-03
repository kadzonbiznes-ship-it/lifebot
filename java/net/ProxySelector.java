/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import sun.security.util.SecurityConstants;

public abstract class ProxySelector {
    private static volatile ProxySelector theProxySelector;

    public static ProxySelector getDefault() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.GET_PROXYSELECTOR_PERMISSION);
        }
        return theProxySelector;
    }

    public static void setDefault(ProxySelector ps) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.SET_PROXYSELECTOR_PERMISSION);
        }
        theProxySelector = ps;
    }

    public abstract List<Proxy> select(URI var1);

    public abstract void connectFailed(URI var1, SocketAddress var2, IOException var3);

    public static ProxySelector of(InetSocketAddress proxyAddress) {
        return new StaticProxySelector(proxyAddress);
    }

    static {
        try {
            Class<?> c = Class.forName("sun.net.spi.DefaultProxySelector");
            if (c != null && ProxySelector.class.isAssignableFrom(c)) {
                ProxySelector tmp;
                theProxySelector = tmp = (ProxySelector)c.newInstance();
            }
        }
        catch (Exception e) {
            theProxySelector = null;
        }
    }

    static class StaticProxySelector
    extends ProxySelector {
        private static final List<Proxy> NO_PROXY_LIST = List.of(Proxy.NO_PROXY);
        final List<Proxy> list;

        StaticProxySelector(InetSocketAddress address) {
            Proxy p = address == null ? Proxy.NO_PROXY : new Proxy(Proxy.Type.HTTP, address);
            this.list = List.of(p);
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException e) {
            if (uri == null || sa == null || e == null) {
                throw new IllegalArgumentException("Arguments can't be null.");
            }
        }

        @Override
        public List<Proxy> select(URI uri) {
            if (uri == null) {
                throw new IllegalArgumentException("URI can't be null");
            }
            String scheme = uri.getScheme();
            if (scheme == null) {
                throw new IllegalArgumentException("protocol can't be null");
            }
            if (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) {
                return this.list;
            }
            return NO_PROXY_LIST;
        }
    }
}

