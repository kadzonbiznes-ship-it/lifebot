/*
 * Decompiled with CFR 0.152.
 */
package sun.net.util;

import java.net.Proxy;
import sun.net.ApplicationProxy;

public final class ProxyUtil {
    private ProxyUtil() {
    }

    public static Proxy copyProxy(Proxy proxy) {
        return proxy == null || proxy.getClass() == Proxy.class || proxy instanceof ApplicationProxy ? proxy : new Proxy(proxy.type(), proxy.address());
    }
}

