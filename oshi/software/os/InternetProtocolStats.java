/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 *  oshi.software.os.InternetProtocolStats$IPConnection
 *  oshi.software.os.InternetProtocolStats$TcpStats
 *  oshi.software.os.InternetProtocolStats$UdpStats
 */
package oshi.software.os;

import java.util.List;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.software.os.InternetProtocolStats;

@ThreadSafe
public interface InternetProtocolStats {
    public TcpStats getTCPv4Stats();

    public TcpStats getTCPv6Stats();

    public UdpStats getUDPv4Stats();

    public UdpStats getUDPv6Stats();

    public List<IPConnection> getConnections();
}

