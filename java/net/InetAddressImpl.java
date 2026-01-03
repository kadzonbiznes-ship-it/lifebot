/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.io.IOException;
import java.net.Inet4AddressImpl;
import java.net.Inet6AddressImpl;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.net.spi.InetAddressResolver;

sealed interface InetAddressImpl
permits Inet4AddressImpl, Inet6AddressImpl {
    public String getLocalHostName() throws UnknownHostException;

    public InetAddress[] lookupAllHostAddr(String var1, InetAddressResolver.LookupPolicy var2) throws UnknownHostException;

    public String getHostByAddr(byte[] var1) throws UnknownHostException;

    public InetAddress anyLocalAddress();

    public InetAddress loopbackAddress();

    public boolean isReachable(InetAddress var1, int var2, NetworkInterface var3, int var4) throws IOException;
}

