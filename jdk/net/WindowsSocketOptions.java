/*
 * Decompiled with CFR 0.152.
 */
package jdk.net;

import java.net.SocketException;
import java.security.AccessController;
import jdk.net.ExtendedSocketOptions;

class WindowsSocketOptions
extends ExtendedSocketOptions.PlatformSocketOptions {
    @Override
    boolean ipDontFragmentSupported() {
        return true;
    }

    @Override
    boolean keepAliveOptionsSupported() {
        return WindowsSocketOptions.keepAliveOptionsSupported0();
    }

    @Override
    void setIpDontFragment(int fd, boolean value, boolean isIPv6) throws SocketException {
        WindowsSocketOptions.setIpDontFragment0(fd, value, isIPv6);
    }

    @Override
    boolean getIpDontFragment(int fd, boolean isIPv6) throws SocketException {
        return WindowsSocketOptions.getIpDontFragment0(fd, isIPv6);
    }

    @Override
    void setTcpKeepAliveProbes(int fd, int value) throws SocketException {
        WindowsSocketOptions.setTcpKeepAliveProbes0(fd, value);
    }

    @Override
    int getTcpKeepAliveProbes(int fd) throws SocketException {
        return WindowsSocketOptions.getTcpKeepAliveProbes0(fd);
    }

    @Override
    void setTcpKeepAliveTime(int fd, int value) throws SocketException {
        WindowsSocketOptions.setTcpKeepAliveTime0(fd, value);
    }

    @Override
    int getTcpKeepAliveTime(int fd) throws SocketException {
        return WindowsSocketOptions.getTcpKeepAliveTime0(fd);
    }

    @Override
    void setTcpKeepAliveIntvl(int fd, int value) throws SocketException {
        WindowsSocketOptions.setTcpKeepAliveIntvl0(fd, value);
    }

    @Override
    int getTcpKeepAliveIntvl(int fd) throws SocketException {
        return WindowsSocketOptions.getTcpKeepAliveIntvl0(fd);
    }

    private static native boolean keepAliveOptionsSupported0();

    private static native void setIpDontFragment0(int var0, boolean var1, boolean var2) throws SocketException;

    private static native boolean getIpDontFragment0(int var0, boolean var1) throws SocketException;

    private static native void setTcpKeepAliveProbes0(int var0, int var1) throws SocketException;

    private static native int getTcpKeepAliveProbes0(int var0) throws SocketException;

    private static native void setTcpKeepAliveTime0(int var0, int var1) throws SocketException;

    private static native int getTcpKeepAliveTime0(int var0) throws SocketException;

    private static native void setTcpKeepAliveIntvl0(int var0, int var1) throws SocketException;

    private static native int getTcpKeepAliveIntvl0(int var0) throws SocketException;

    static {
        if (System.getSecurityManager() == null) {
            System.loadLibrary("extnet");
        } else {
            AccessController.doPrivileged(() -> {
                System.loadLibrary("extnet");
                return null;
            });
        }
    }
}

