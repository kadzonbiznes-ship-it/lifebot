/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.net.DefaultInterface;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetPermission;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import jdk.internal.loader.BootLoader;

public final class NetworkInterface {
    private String name;
    private String displayName;
    private int index;
    private InetAddress[] addrs;
    private InterfaceAddress[] bindings;
    private NetworkInterface[] childs;
    private NetworkInterface parent = null;
    private boolean virtual = false;
    private static final NetworkInterface defaultInterface;

    NetworkInterface() {
    }

    NetworkInterface(String name, int index, InetAddress[] addrs) {
        this.name = name;
        this.index = index;
        this.addrs = addrs;
    }

    public String getName() {
        return this.name;
    }

    public Enumeration<InetAddress> getInetAddresses() {
        return NetworkInterface.enumerationFromArray(this.getCheckedInetAddresses());
    }

    public Stream<InetAddress> inetAddresses() {
        return NetworkInterface.streamFromArray(this.getCheckedInetAddresses());
    }

    private InetAddress[] getCheckedInetAddresses() {
        InetAddress[] local_addrs = new InetAddress[this.addrs.length];
        boolean trusted = true;
        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            try {
                sec.checkPermission(new NetPermission("getNetworkInformation"));
            }
            catch (SecurityException e) {
                trusted = false;
            }
        }
        int i = 0;
        for (int j = 0; j < this.addrs.length; ++j) {
            try {
                if (!trusted) {
                    sec.checkConnect(this.addrs[j].getHostAddress(), -1);
                }
                local_addrs[i++] = this.addrs[j];
                continue;
            }
            catch (SecurityException securityException) {
                // empty catch block
            }
        }
        return Arrays.copyOf(local_addrs, i);
    }

    public List<InterfaceAddress> getInterfaceAddresses() {
        ArrayList<InterfaceAddress> lst = new ArrayList<InterfaceAddress>(1);
        if (this.bindings != null) {
            SecurityManager sec = System.getSecurityManager();
            for (int j = 0; j < this.bindings.length; ++j) {
                try {
                    if (sec != null) {
                        sec.checkConnect(this.bindings[j].getAddress().getHostAddress(), -1);
                    }
                    lst.add(this.bindings[j]);
                    continue;
                }
                catch (SecurityException securityException) {
                    // empty catch block
                }
            }
        }
        return lst;
    }

    public Enumeration<NetworkInterface> getSubInterfaces() {
        return NetworkInterface.enumerationFromArray(this.childs);
    }

    public Stream<NetworkInterface> subInterfaces() {
        return NetworkInterface.streamFromArray(this.childs);
    }

    public NetworkInterface getParent() {
        return this.parent;
    }

    public int getIndex() {
        return this.index;
    }

    public String getDisplayName() {
        return "".equals(this.displayName) ? null : this.displayName;
    }

    public static NetworkInterface getByName(String name) throws SocketException {
        if (name == null) {
            throw new NullPointerException();
        }
        return NetworkInterface.getByName0(name);
    }

    public static NetworkInterface getByIndex(int index) throws SocketException {
        if (index < 0) {
            throw new IllegalArgumentException("Interface index can't be negative");
        }
        return NetworkInterface.getByIndex0(index);
    }

    public static NetworkInterface getByInetAddress(InetAddress addr) throws SocketException {
        if (addr == null) {
            throw new NullPointerException();
        }
        if (addr.holder.family == 1) {
            if (!(addr instanceof Inet4Address)) {
                throw new IllegalArgumentException("invalid family type: " + addr.holder.family);
            }
        } else if (addr.holder.family == 2) {
            if (!(addr instanceof Inet6Address)) {
                throw new IllegalArgumentException("invalid family type: " + addr.holder.family);
            }
        } else {
            throw new IllegalArgumentException("invalid address type: " + addr);
        }
        return NetworkInterface.getByInetAddress0(addr);
    }

    public static Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException {
        NetworkInterface[] netifs = NetworkInterface.getAll();
        if (netifs != null && netifs.length > 0) {
            return NetworkInterface.enumerationFromArray(netifs);
        }
        throw new SocketException("No network interfaces configured");
    }

    public static Stream<NetworkInterface> networkInterfaces() throws SocketException {
        NetworkInterface[] netifs = NetworkInterface.getAll();
        if (netifs != null && netifs.length > 0) {
            return NetworkInterface.streamFromArray(netifs);
        }
        throw new SocketException("No network interfaces configured");
    }

    static boolean isBoundInetAddress(InetAddress addr) throws SocketException {
        return NetworkInterface.boundInetAddress0(addr);
    }

    private static <T> Enumeration<T> enumerationFromArray(final T[] a) {
        return new Enumeration<T>(){
            int i = 0;

            @Override
            public T nextElement() {
                if (this.i < a.length) {
                    return a[this.i++];
                }
                throw new NoSuchElementException();
            }

            @Override
            public boolean hasMoreElements() {
                return this.i < a.length;
            }
        };
    }

    private static <T> Stream<T> streamFromArray(T[] a) {
        return StreamSupport.stream(Spliterators.spliterator(a, 1281), false);
    }

    private static native NetworkInterface[] getAll() throws SocketException;

    private static native NetworkInterface getByName0(String var0) throws SocketException;

    private static native NetworkInterface getByIndex0(int var0) throws SocketException;

    private static native boolean boundInetAddress0(InetAddress var0) throws SocketException;

    private static native NetworkInterface getByInetAddress0(InetAddress var0) throws SocketException;

    public boolean isUp() throws SocketException {
        return NetworkInterface.isUp0(this.name, this.index);
    }

    public boolean isLoopback() throws SocketException {
        return NetworkInterface.isLoopback0(this.name, this.index);
    }

    public boolean isPointToPoint() throws SocketException {
        return NetworkInterface.isP2P0(this.name, this.index);
    }

    public boolean supportsMulticast() throws SocketException {
        return NetworkInterface.supportsMulticast0(this.name, this.index);
    }

    public byte[] getHardwareAddress() throws SocketException {
        block5: {
            SecurityManager sec = System.getSecurityManager();
            if (sec != null) {
                try {
                    sec.checkPermission(new NetPermission("getNetworkInformation"));
                }
                catch (SecurityException e) {
                    if (this.getInetAddresses().hasMoreElements()) break block5;
                    return null;
                }
            }
        }
        if (NetworkInterface.isLoopback0(this.name, this.index)) {
            return null;
        }
        for (InetAddress addr : this.addrs) {
            if (!(addr instanceof Inet4Address)) continue;
            return NetworkInterface.getMacAddr0(((Inet4Address)addr).getAddress(), this.name, this.index);
        }
        return NetworkInterface.getMacAddr0(null, this.name, this.index);
    }

    public int getMTU() throws SocketException {
        return NetworkInterface.getMTU0(this.name, this.index);
    }

    public boolean isVirtual() {
        return this.virtual;
    }

    private static native boolean isUp0(String var0, int var1) throws SocketException;

    private static native boolean isLoopback0(String var0, int var1) throws SocketException;

    private static native boolean supportsMulticast0(String var0, int var1) throws SocketException;

    private static native boolean isP2P0(String var0, int var1) throws SocketException;

    private static native byte[] getMacAddr0(byte[] var0, String var1, int var2) throws SocketException;

    private static native int getMTU0(String var0, int var1) throws SocketException;

    public boolean equals(Object obj) {
        if (!(obj instanceof NetworkInterface)) {
            return false;
        }
        NetworkInterface that = (NetworkInterface)obj;
        if (this.name != null ? !this.name.equals(that.name) : that.name != null) {
            return false;
        }
        if (this.addrs == null) {
            return that.addrs == null;
        }
        if (that.addrs == null) {
            return false;
        }
        if (this.addrs.length != that.addrs.length) {
            return false;
        }
        InetAddress[] thatAddrs = that.addrs;
        int count = thatAddrs.length;
        for (int i = 0; i < count; ++i) {
            boolean found = false;
            for (int j = 0; j < count; ++j) {
                if (!this.addrs[i].equals(thatAddrs[j])) continue;
                found = true;
                break;
            }
            if (found) continue;
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.name == null ? 0 : this.name.hashCode();
    }

    public String toString() {
        String result = "name:";
        result = result + (this.name == null ? "null" : this.name);
        if (this.displayName != null) {
            result = result + " (" + this.displayName + ")";
        }
        return result;
    }

    private static native void init();

    static NetworkInterface getDefault() {
        return defaultInterface;
    }

    static {
        BootLoader.loadLibrary("net");
        NetworkInterface.init();
        defaultInterface = DefaultInterface.getDefault();
    }
}

