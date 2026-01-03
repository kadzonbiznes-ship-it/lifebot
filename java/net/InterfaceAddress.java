/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Objects;

public class InterfaceAddress {
    private InetAddress address = null;
    private Inet4Address broadcast = null;
    private short maskLength = 0;

    InterfaceAddress() {
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public InetAddress getBroadcast() {
        return this.broadcast;
    }

    public short getNetworkPrefixLength() {
        return this.maskLength;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof InterfaceAddress)) return false;
        InterfaceAddress cmp = (InterfaceAddress)obj;
        if (!Objects.equals(this.address, cmp.address)) return false;
        if (!Objects.equals(this.broadcast, cmp.broadcast)) return false;
        if (this.maskLength != cmp.maskLength) return false;
        return true;
    }

    public int hashCode() {
        return this.address.hashCode() + (this.broadcast != null ? this.broadcast.hashCode() : 0) + this.maskLength;
    }

    public String toString() {
        return this.address + "/" + this.maskLength + " [" + this.broadcast + "]";
    }
}

