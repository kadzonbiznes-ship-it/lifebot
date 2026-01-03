/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 */
package oshi.software.os;

import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface NetworkParams {
    public String getHostName();

    public String getDomainName();

    public String[] getDnsServers();

    public String getIpv4DefaultGateway();

    public String getIpv6DefaultGateway();
}

