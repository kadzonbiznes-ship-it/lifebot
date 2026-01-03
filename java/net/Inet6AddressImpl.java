/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet4AddressImpl;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetAddressImpl;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.spi.InetAddressResolver;
import java.util.Enumeration;

final class Inet6AddressImpl
implements InetAddressImpl {
    private InetAddress anyLocalAddress;
    private InetAddress loopbackAddress;

    Inet6AddressImpl() {
    }

    @Override
    public native String getLocalHostName() throws UnknownHostException;

    @Override
    public InetAddress[] lookupAllHostAddr(String hostname, InetAddressResolver.LookupPolicy lookupPolicy) throws UnknownHostException {
        return this.lookupAllHostAddr(hostname, lookupPolicy.characteristics());
    }

    private native InetAddress[] lookupAllHostAddr(String var1, int var2) throws UnknownHostException;

    @Override
    public native String getHostByAddr(byte[] var1) throws UnknownHostException;

    private native boolean isReachable0(byte[] var1, int var2, int var3, byte[] var4, int var5, int var6) throws IOException;

    @Override
    public boolean isReachable(InetAddress addr, int timeout, NetworkInterface netif, int ttl) throws IOException {
        byte[] ifaddr = null;
        int scope = -1;
        int netif_scope = -1;
        if (netif != null) {
            Enumeration<InetAddress> it = netif.getInetAddresses();
            while (it.hasMoreElements()) {
                InetAddress inetaddr = it.nextElement();
                if (!inetaddr.getClass().isInstance(addr)) continue;
                ifaddr = inetaddr.getAddress();
                if (!(inetaddr instanceof Inet6Address)) break;
                netif_scope = ((Inet6Address)inetaddr).getScopeId();
                break;
            }
            if (ifaddr == null) {
                return false;
            }
        }
        if (addr instanceof Inet6Address) {
            scope = ((Inet6Address)addr).getScopeId();
        }
        return this.isReachable0(addr.getAddress(), scope, timeout, ifaddr, ttl, netif_scope);
    }

    @Override
    public synchronized InetAddress anyLocalAddress() {
        if (this.anyLocalAddress == null) {
            int flags = InetAddress.PLATFORM_LOOKUP_POLICY.characteristics();
            if (InetAddress.ipv6AddressesFirst(flags) || InetAddress.systemAddressesOrder(flags)) {
                this.anyLocalAddress = new Inet6Address();
                this.anyLocalAddress.holder().hostName = "::";
            } else {
                this.anyLocalAddress = new Inet4AddressImpl().anyLocalAddress();
            }
        }
        return this.anyLocalAddress;
    }

    @Override
    public synchronized InetAddress loopbackAddress() {
        if (this.loopbackAddress == null) {
            int flags = InetAddress.PLATFORM_LOOKUP_POLICY.characteristics();
            boolean preferIPv6Address = InetAddress.ipv6AddressesFirst(flags) || InetAddress.systemAddressesOrder(flags);
            for (int i = 0; i < 2; ++i) {
                InetAddress address = i == (preferIPv6Address ? 0 : 1) ? new Inet6Address("localhost", new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}) : new Inet4Address("localhost", new byte[]{127, 0, 0, 1});
                if (i == 0) {
                    this.loopbackAddress = address;
                }
                try {
                    if (!NetworkInterface.isBoundInetAddress(address)) {
                    }
                }
                catch (SocketException e) {}
                continue;
                this.loopbackAddress = address;
                break;
            }
        }
        return this.loopbackAddress;
    }
}

