/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.util.internal.SuppressJava6Requirement
 */
package io.netty.util.internal;

import io.netty.util.internal.SuppressJava6Requirement;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.Enumeration;

public final class SocketUtils {
    private static final Enumeration<Object> EMPTY = Collections.enumeration(Collections.emptyList());

    private SocketUtils() {
    }

    private static <T> Enumeration<T> empty() {
        return EMPTY;
    }

    public static void connect(Socket socket, SocketAddress remoteAddress, int timeout) throws IOException {
        try {
            AccessController.doPrivileged(new /* Unavailable Anonymous Inner Class!! */);
        }
        catch (PrivilegedActionException e) {
            throw (IOException)e.getCause();
        }
    }

    public static void bind(Socket socket, SocketAddress bindpoint) throws IOException {
        try {
            AccessController.doPrivileged(new /* Unavailable Anonymous Inner Class!! */);
        }
        catch (PrivilegedActionException e) {
            throw (IOException)e.getCause();
        }
    }

    public static boolean connect(final SocketChannel socketChannel, final SocketAddress remoteAddress) throws IOException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>(){

                @Override
                public Boolean run() throws IOException {
                    return socketChannel.connect(remoteAddress);
                }
            });
        }
        catch (PrivilegedActionException e) {
            throw (IOException)e.getCause();
        }
    }

    @SuppressJava6Requirement(reason="Usage guarded by java version check")
    public static void bind(SocketChannel socketChannel, SocketAddress address) throws IOException {
        try {
            AccessController.doPrivileged(new /* Unavailable Anonymous Inner Class!! */);
        }
        catch (PrivilegedActionException e) {
            throw (IOException)e.getCause();
        }
    }

    public static SocketChannel accept(ServerSocketChannel serverSocketChannel) throws IOException {
        try {
            return (SocketChannel)AccessController.doPrivileged(new /* Unavailable Anonymous Inner Class!! */);
        }
        catch (PrivilegedActionException e) {
            throw (IOException)e.getCause();
        }
    }

    @SuppressJava6Requirement(reason="Usage guarded by java version check")
    public static void bind(DatagramChannel networkChannel, SocketAddress address) throws IOException {
        try {
            AccessController.doPrivileged(new /* Unavailable Anonymous Inner Class!! */);
        }
        catch (PrivilegedActionException e) {
            throw (IOException)e.getCause();
        }
    }

    public static SocketAddress localSocketAddress(ServerSocket socket) {
        return (SocketAddress)AccessController.doPrivileged(new /* Unavailable Anonymous Inner Class!! */);
    }

    public static InetAddress addressByName(final String hostname) throws UnknownHostException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<InetAddress>(){

                @Override
                public InetAddress run() throws UnknownHostException {
                    return InetAddress.getByName(hostname);
                }
            });
        }
        catch (PrivilegedActionException e) {
            throw (UnknownHostException)e.getCause();
        }
    }

    public static InetAddress[] allAddressesByName(String hostname) throws UnknownHostException {
        try {
            return (InetAddress[])AccessController.doPrivileged(new /* Unavailable Anonymous Inner Class!! */);
        }
        catch (PrivilegedActionException e) {
            throw (UnknownHostException)e.getCause();
        }
    }

    public static InetSocketAddress socketAddress(String hostname, int port) {
        return (InetSocketAddress)AccessController.doPrivileged(new /* Unavailable Anonymous Inner Class!! */);
    }

    public static Enumeration<InetAddress> addressesFromNetworkInterface(final NetworkInterface intf) {
        Enumeration<InetAddress> addresses = AccessController.doPrivileged(new PrivilegedAction<Enumeration<InetAddress>>(){

            @Override
            public Enumeration<InetAddress> run() {
                return intf.getInetAddresses();
            }
        });
        if (addresses == null) {
            return SocketUtils.empty();
        }
        return addresses;
    }

    @SuppressJava6Requirement(reason="Usage guarded by java version check")
    public static InetAddress loopbackAddress() {
        return (InetAddress)AccessController.doPrivileged(new /* Unavailable Anonymous Inner Class!! */);
    }

    public static byte[] hardwareAddressFromNetworkInterface(final NetworkInterface intf) throws SocketException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<byte[]>(){

                @Override
                public byte[] run() throws SocketException {
                    return intf.getHardwareAddress();
                }
            });
        }
        catch (PrivilegedActionException e) {
            throw (SocketException)e.getCause();
        }
    }
}

