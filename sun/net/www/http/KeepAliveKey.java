/*
 * Decompiled with CFR 0.152.
 */
package sun.net.www.http;

import java.net.URL;

class KeepAliveKey {
    private final String protocol;
    private final String host;
    private final int port;
    private final Object obj;

    public KeepAliveKey(URL url, Object obj) {
        this.protocol = url.getProtocol();
        this.host = url.getHost();
        this.port = url.getPort();
        this.obj = obj;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof KeepAliveKey)) {
            return false;
        }
        KeepAliveKey kae = (KeepAliveKey)obj;
        return this.host.equals(kae.host) && this.port == kae.port && this.protocol.equals(kae.protocol) && this.obj == kae.obj;
    }

    public int hashCode() {
        String str = this.protocol + this.host + this.port;
        return this.obj == null ? str.hashCode() : str.hashCode() + this.obj.hashCode();
    }
}

