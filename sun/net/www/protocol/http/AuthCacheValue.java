/*
 * Decompiled with CFR 0.152.
 */
package sun.net.www.protocol.http;

import java.net.PasswordAuthentication;
import sun.net.www.protocol.http.AuthScheme;

public abstract class AuthCacheValue {
    AuthCacheValue() {
    }

    abstract Type getAuthType();

    abstract AuthScheme getAuthScheme();

    abstract String getHost();

    abstract int getPort();

    abstract String getRealm();

    abstract String getPath();

    abstract String getProtocolScheme();

    abstract PasswordAuthentication credentials();

    public static enum Type {
        Proxy,
        Server;

    }
}

