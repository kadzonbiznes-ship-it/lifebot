/*
 * Decompiled with CFR 0.152.
 */
package sun.net.www.protocol.http;

import sun.net.www.protocol.http.AuthCacheValue;

public interface AuthCache {
    public void put(String var1, AuthCacheValue var2);

    public AuthCacheValue get(String var1, String var2);

    public void remove(String var1, AuthCacheValue var2);
}

