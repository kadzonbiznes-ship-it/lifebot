/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetAddressImpl;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.net.spi.InetAddressResolver;
import java.util.Enumeration;

final class Inet4AddressImpl
implements InetAddressImpl {
    private InetAddress anyLocalAddress;
    private InetAddress loopbackAddress;

    Inet4AddressImpl() {
    }

    @Override
    public native String getLocalHostName() throws UnknownHostException;

    @Override
    public InetAddress[] lookupAllHostAddr(String hostname, InetAddressResolver.LookupPolicy lookupPolicy) throws UnknownHostException {
        if ((lookupPolicy.characteristics() & 1) == 0) {
            throw new UnknownHostException(hostname);
        }
        return this.lookupAllHostAddr(hostname);
    }

    private native InetAddress[] lookupAllHostAddr(String var1) throws UnknownHostException;

    @Override
    public native String getHostByAddr(byte[] var1) throws UnknownHostException;

    private native boolean isReachable0(byte[] var1, int var2, byte[] var3, int var4) throws IOException;

    @Override
    public synchronized InetAddress anyLocalAddress() {
        if (this.anyLocalAddress == null) {
            this.anyLocalAddress = new Inet4Address();
            this.anyLocalAddress.holder().hostName = "0.0.0.0";
        }
        return this.anyLocalAddress;
    }

    @Override
    public synchronized InetAddress loopbackAddress() {
        if (this.loopbackAddress == null) {
            byte[] loopback = new byte[]{127, 0, 0, 1};
            this.loopbackAddress = new Inet4Address("localhost", loopback);
        }
        return this.loopbackAddress;
    }

    @Override
    public boolean isReachable(InetAddress addr, int timeout, NetworkInterface netif, int ttl) throws IOException {
        byte[] ifaddr = null;
        if (netif != null) {
            Enumeration<InetAddress> it = netif.getInetAddresses();
            InetAddress inetaddr = null;
            while (!(inetaddr instanceof Inet4Address) && it.hasMoreElements()) {
                inetaddr = it.nextElement();
            }
            if (inetaddr instanceof Inet4Address) {
                ifaddr = inetaddr.getAddress();
            }
        }
        return this.isReachable0(addr.getAddress(), timeout, ifaddr, ttl);
    }
}

