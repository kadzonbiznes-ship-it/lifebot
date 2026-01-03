/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Proxy {
    private Type type;
    private SocketAddress sa;
    public static final Proxy NO_PROXY = new Proxy();

    private Proxy() {
        this.type = Type.DIRECT;
        this.sa = null;
    }

    public Proxy(Type type, SocketAddress sa) {
        if (type == Type.DIRECT || !(sa instanceof InetSocketAddress)) {
            throw new IllegalArgumentException("type " + (Object)((Object)type) + " is not compatible with address " + sa);
        }
        this.type = type;
        this.sa = sa;
    }

    public Type type() {
        return this.type;
    }

    public SocketAddress address() {
        return this.sa;
    }

    public String toString() {
        if (this.type() == Type.DIRECT) {
            return "DIRECT";
        }
        return (Object)((Object)this.type()) + " @ " + this.address();
    }

    public final boolean equals(Object obj) {
        if (!(obj instanceof Proxy)) {
            return false;
        }
        Proxy p = (Proxy)obj;
        if (p.type() == this.type()) {
            if (this.address() == null) {
                return p.address() == null;
            }
            return this.address().equals(p.address());
        }
        return false;
    }

    public final int hashCode() {
        if (this.address() == null) {
            return this.type().hashCode();
        }
        return this.type().hashCode() + this.address().hashCode();
    }

    public static enum Type {
        DIRECT,
        HTTP,
        SOCKS;

    }
}

