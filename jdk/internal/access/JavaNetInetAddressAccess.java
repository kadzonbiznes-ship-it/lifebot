/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.access;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

public interface JavaNetInetAddressAccess {
    public String getOriginalHostName(InetAddress var1);

    public int addressValue(Inet4Address var1);

    public byte[] addressBytes(Inet6Address var1);
}

