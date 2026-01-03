/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.io.ObjectStreamException;
import java.net.InetAddress;

public final class Inet4Address
extends InetAddress {
    static final int INADDRSZ = 4;
    private static final long serialVersionUID = 3286316764910316507L;

    Inet4Address() {
        this.holder().hostName = null;
        this.holder().address = 0;
        this.holder().family = 1;
    }

    Inet4Address(String hostName, byte[] addr) {
        this.holder().hostName = hostName;
        this.holder().family = 1;
        if (addr != null && addr.length == 4) {
            int address = addr[3] & 0xFF;
            address |= addr[2] << 8 & 0xFF00;
            address |= addr[1] << 16 & 0xFF0000;
            this.holder().address = address |= addr[0] << 24 & 0xFF000000;
        }
        this.holder().originalHostName = hostName;
    }

    Inet4Address(String hostName, int address) {
        this.holder().hostName = hostName;
        this.holder().family = 1;
        this.holder().address = address;
        this.holder().originalHostName = hostName;
    }

    private Object writeReplace() throws ObjectStreamException {
        InetAddress inet = new InetAddress();
        inet.holder().hostName = this.holder().getHostName();
        inet.holder().address = this.holder().getAddress();
        inet.holder().family = 2;
        return inet;
    }

    @Override
    public boolean isMulticastAddress() {
        return (this.holder().getAddress() & 0xF0000000) == -536870912;
    }

    @Override
    public boolean isAnyLocalAddress() {
        return this.holder().getAddress() == 0;
    }

    @Override
    public boolean isLoopbackAddress() {
        byte[] byteAddr = this.getAddress();
        return byteAddr[0] == 127;
    }

    @Override
    public boolean isLinkLocalAddress() {
        int address = this.holder().getAddress();
        return (address >>> 24 & 0xFF) == 169 && (address >>> 16 & 0xFF) == 254;
    }

    @Override
    public boolean isSiteLocalAddress() {
        int address = this.holder().getAddress();
        return (address >>> 24 & 0xFF) == 10 || (address >>> 24 & 0xFF) == 172 && (address >>> 16 & 0xF0) == 16 || (address >>> 24 & 0xFF) == 192 && (address >>> 16 & 0xFF) == 168;
    }

    @Override
    public boolean isMCGlobal() {
        byte[] byteAddr = this.getAddress();
        return (byteAddr[0] & 0xFF) >= 224 && (byteAddr[0] & 0xFF) <= 238 && ((byteAddr[0] & 0xFF) != 224 || byteAddr[1] != 0 || byteAddr[2] != 0);
    }

    @Override
    public boolean isMCNodeLocal() {
        return false;
    }

    @Override
    public boolean isMCLinkLocal() {
        int address = this.holder().getAddress();
        return (address >>> 24 & 0xFF) == 224 && (address >>> 16 & 0xFF) == 0 && (address >>> 8 & 0xFF) == 0;
    }

    @Override
    public boolean isMCSiteLocal() {
        int address = this.holder().getAddress();
        return (address >>> 24 & 0xFF) == 239 && (address >>> 16 & 0xFF) == 255;
    }

    @Override
    public boolean isMCOrgLocal() {
        int address = this.holder().getAddress();
        return (address >>> 24 & 0xFF) == 239 && (address >>> 16 & 0xFF) >= 192 && (address >>> 16 & 0xFF) <= 195;
    }

    @Override
    public byte[] getAddress() {
        int address = this.holder().getAddress();
        byte[] addr = new byte[]{(byte)(address >>> 24 & 0xFF), (byte)(address >>> 16 & 0xFF), (byte)(address >>> 8 & 0xFF), (byte)(address & 0xFF)};
        return addr;
    }

    int addressValue() {
        return this.holder().getAddress();
    }

    @Override
    public String getHostAddress() {
        return Inet4Address.numericToTextFormat(this.getAddress());
    }

    @Override
    public int hashCode() {
        return this.holder().getAddress();
    }

    @Override
    public boolean equals(Object obj) {
        Inet4Address inet4Address;
        return obj instanceof Inet4Address && (inet4Address = (Inet4Address)obj).holder().getAddress() == this.holder().getAddress();
    }

    static String numericToTextFormat(byte[] src) {
        return (src[0] & 0xFF) + "." + (src[1] & 0xFF) + "." + (src[2] & 0xFF) + "." + (src[3] & 0xFF);
    }

    private static native void init();

    static {
        Inet4Address.init();
    }
}

