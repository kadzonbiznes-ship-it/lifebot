/*
 * Decompiled with CFR 0.152.
 */
package javax.net.ssl;

import java.util.Enumeration;
import javax.net.ssl.SSLSession;

public interface SSLSessionContext {
    public SSLSession getSession(byte[] var1);

    public Enumeration<byte[]> getIds();

    public void setSessionTimeout(int var1);

    public int getSessionTimeout();

    public void setSessionCacheSize(int var1);

    public int getSessionCacheSize();
}

