/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import jdk.internal.misc.Unsafe;

public final class Inet6Address
extends InetAddress {
    static final int INADDRSZ = 16;
    private final transient Inet6AddressHolder holder6;
    private static final long serialVersionUID = 6880410070516793377L;
    private static final ObjectStreamField[] serialPersistentFields;
    private static final Unsafe UNSAFE;
    private static final long FIELDS_OFFSET;
    private static final int INT16SZ = 2;

    Inet6Address() {
        this.holder.init(null, 2);
        this.holder6 = new Inet6AddressHolder();
    }

    Inet6Address(String hostName, byte[] addr, int scope_id) {
        this.holder.init(hostName, 2);
        this.holder6 = new Inet6AddressHolder();
        this.holder6.init(addr, scope_id);
    }

    Inet6Address(String hostName, byte[] addr) {
        this.holder6 = new Inet6AddressHolder();
        try {
            this.initif(hostName, addr, null);
        }
        catch (UnknownHostException unknownHostException) {
            // empty catch block
        }
    }

    Inet6Address(String hostName, byte[] addr, NetworkInterface nif) throws UnknownHostException {
        this.holder6 = new Inet6AddressHolder();
        this.initif(hostName, addr, nif);
    }

    Inet6Address(String hostName, byte[] addr, String ifname) throws UnknownHostException {
        this.holder6 = new Inet6AddressHolder();
        this.initstr(hostName, addr, ifname);
    }

    public static Inet6Address getByAddress(String host, byte[] addr, NetworkInterface nif) throws UnknownHostException {
        if (host != null && !host.isEmpty() && host.charAt(0) == '[' && host.charAt(host.length() - 1) == ']') {
            host = host.substring(1, host.length() - 1);
        }
        if (addr != null && addr.length == 16) {
            return new Inet6Address(host, addr, nif);
        }
        throw new UnknownHostException("addr is of illegal length");
    }

    public static Inet6Address getByAddress(String host, byte[] addr, int scope_id) throws UnknownHostException {
        if (host != null && !host.isEmpty() && host.charAt(0) == '[' && host.charAt(host.length() - 1) == ']') {
            host = host.substring(1, host.length() - 1);
        }
        if (addr != null && addr.length == 16) {
            return new Inet6Address(host, addr, scope_id);
        }
        throw new UnknownHostException("addr is of illegal length");
    }

    private void initstr(String hostName, byte[] addr, String ifname) throws UnknownHostException {
        try {
            NetworkInterface nif = NetworkInterface.getByName(ifname);
            if (nif == null) {
                throw new UnknownHostException("no such interface " + ifname);
            }
            this.initif(hostName, addr, nif);
        }
        catch (SocketException e) {
            throw new UnknownHostException("SocketException thrown" + ifname);
        }
    }

    private void initif(String hostName, byte[] addr, NetworkInterface nif) throws UnknownHostException {
        int family = -1;
        this.holder6.init(addr, nif);
        if (addr.length == 16) {
            family = 2;
        }
        this.holder.init(hostName, family);
    }

    private static boolean isDifferentLocalAddressType(byte[] thisAddr, byte[] otherAddr) {
        if (Inet6Address.isLinkLocalAddress(thisAddr) && !Inet6Address.isLinkLocalAddress(otherAddr)) {
            return false;
        }
        return !Inet6Address.isSiteLocalAddress(thisAddr) || Inet6Address.isSiteLocalAddress(otherAddr);
    }

    private static int deriveNumericScope(byte[] thisAddr, NetworkInterface ifc) throws UnknownHostException {
        Enumeration<InetAddress> addresses = ifc.getInetAddresses();
        while (addresses.hasMoreElements()) {
            Inet6Address ia6_addr;
            InetAddress addr = addresses.nextElement();
            if (!(addr instanceof Inet6Address) || !Inet6Address.isDifferentLocalAddressType(thisAddr, (ia6_addr = (Inet6Address)addr).getAddress())) continue;
            return ia6_addr.getScopeId();
        }
        throw new UnknownHostException("no scope_id found");
    }

    private int deriveNumericScope(String ifname) throws UnknownHostException {
        Enumeration<NetworkInterface> en;
        try {
            en = NetworkInterface.getNetworkInterfaces();
        }
        catch (SocketException e) {
            throw new UnknownHostException("could not enumerate local network interfaces");
        }
        while (en.hasMoreElements()) {
            NetworkInterface ifc = en.nextElement();
            if (!ifc.getName().equals(ifname)) continue;
            return Inet6Address.deriveNumericScope(this.holder6.ipaddress, ifc);
        }
        throw new UnknownHostException("No matching address found for interface : " + ifname);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        NetworkInterface scope_ifname = null;
        ObjectInputStream.GetField gf = s.readFields();
        byte[] ipaddress = (byte[])gf.get("ipaddress", new byte[0]);
        int scope_id = gf.get("scope_id", -1);
        boolean scope_id_set = gf.get("scope_id_set", false);
        boolean scope_ifname_set = gf.get("scope_ifname_set", false);
        String ifname = (String)gf.get("ifname", null);
        if (ifname != null && !ifname.isEmpty()) {
            try {
                scope_ifname = NetworkInterface.getByName(ifname);
                if (scope_ifname == null) {
                    scope_id_set = false;
                    scope_ifname_set = false;
                    scope_id = 0;
                } else {
                    scope_ifname_set = true;
                    try {
                        scope_id = Inet6Address.deriveNumericScope(ipaddress, scope_ifname);
                    }
                    catch (UnknownHostException unknownHostException) {}
                }
            }
            catch (SocketException socketException) {
                // empty catch block
            }
        }
        if ((ipaddress = (byte[])ipaddress.clone()).length != 16) {
            throw new InvalidObjectException("invalid address length: " + ipaddress.length);
        }
        if (this.holder.getFamily() != 2) {
            throw new InvalidObjectException("invalid address family type");
        }
        Inet6AddressHolder h = new Inet6AddressHolder(ipaddress, scope_id, scope_id_set, scope_ifname, scope_ifname_set);
        UNSAFE.putReference(this, FIELDS_OFFSET, h);
    }

    private synchronized void writeObject(ObjectOutputStream s) throws IOException {
        String ifname = null;
        if (this.holder6.scope_ifname != null) {
            ifname = this.holder6.scope_ifname.getName();
            this.holder6.scope_ifname_set = true;
        }
        ObjectOutputStream.PutField pfields = s.putFields();
        pfields.put("ipaddress", this.holder6.ipaddress);
        pfields.put("scope_id", this.holder6.scope_id);
        pfields.put("scope_id_set", this.holder6.scope_id_set);
        pfields.put("scope_ifname_set", this.holder6.scope_ifname_set);
        pfields.put("ifname", ifname);
        s.writeFields();
    }

    @Override
    public boolean isMulticastAddress() {
        return this.holder6.isMulticastAddress();
    }

    @Override
    public boolean isAnyLocalAddress() {
        return this.holder6.isAnyLocalAddress();
    }

    @Override
    public boolean isLoopbackAddress() {
        return this.holder6.isLoopbackAddress();
    }

    @Override
    public boolean isLinkLocalAddress() {
        return this.holder6.isLinkLocalAddress();
    }

    static boolean isLinkLocalAddress(byte[] ipaddress) {
        return (ipaddress[0] & 0xFF) == 254 && (ipaddress[1] & 0xC0) == 128;
    }

    @Override
    public boolean isSiteLocalAddress() {
        return this.holder6.isSiteLocalAddress();
    }

    static boolean isSiteLocalAddress(byte[] ipaddress) {
        return (ipaddress[0] & 0xFF) == 254 && (ipaddress[1] & 0xC0) == 192;
    }

    @Override
    public boolean isMCGlobal() {
        return this.holder6.isMCGlobal();
    }

    @Override
    public boolean isMCNodeLocal() {
        return this.holder6.isMCNodeLocal();
    }

    @Override
    public boolean isMCLinkLocal() {
        return this.holder6.isMCLinkLocal();
    }

    @Override
    public boolean isMCSiteLocal() {
        return this.holder6.isMCSiteLocal();
    }

    @Override
    public boolean isMCOrgLocal() {
        return this.holder6.isMCOrgLocal();
    }

    @Override
    public byte[] getAddress() {
        return (byte[])this.holder6.ipaddress.clone();
    }

    byte[] addressBytes() {
        return this.holder6.ipaddress;
    }

    public int getScopeId() {
        return this.holder6.scope_id;
    }

    public NetworkInterface getScopedInterface() {
        return this.holder6.scope_ifname;
    }

    @Override
    public String getHostAddress() {
        return this.holder6.getHostAddress();
    }

    @Override
    public int hashCode() {
        return this.holder6.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Inet6Address) {
            Inet6Address inetAddr = (Inet6Address)obj;
            return this.holder6.equals(inetAddr.holder6);
        }
        return false;
    }

    public boolean isIPv4CompatibleAddress() {
        return this.holder6.isIPv4CompatibleAddress();
    }

    static String numericToTextFormat(byte[] src) {
        StringBuilder sb = new StringBuilder(39);
        for (int i = 0; i < 8; ++i) {
            sb.append(Integer.toHexString(src[i << 1] << 8 & 0xFF00 | src[(i << 1) + 1] & 0xFF));
            if (i >= 7) continue;
            sb.append(":");
        }
        return sb.toString();
    }

    private static native void init();

    static {
        Inet6Address.init();
        serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("ipaddress", byte[].class), new ObjectStreamField("scope_id", Integer.TYPE), new ObjectStreamField("scope_id_set", Boolean.TYPE), new ObjectStreamField("scope_ifname_set", Boolean.TYPE), new ObjectStreamField("ifname", String.class)};
        UNSAFE = Unsafe.getUnsafe();
        FIELDS_OFFSET = UNSAFE.objectFieldOffset(Inet6Address.class, "holder6");
    }

    private static class Inet6AddressHolder {
        byte[] ipaddress;
        int scope_id;
        boolean scope_id_set;
        NetworkInterface scope_ifname;
        boolean scope_ifname_set;

        private Inet6AddressHolder() {
            this.ipaddress = new byte[16];
        }

        private Inet6AddressHolder(byte[] ipaddress, int scope_id, boolean scope_id_set, NetworkInterface ifname, boolean scope_ifname_set) {
            this.ipaddress = ipaddress;
            this.scope_id = scope_id;
            this.scope_id_set = scope_id_set;
            this.scope_ifname_set = scope_ifname_set;
            this.scope_ifname = ifname;
        }

        void setAddr(byte[] addr) {
            if (addr.length == 16) {
                System.arraycopy(addr, 0, this.ipaddress, 0, 16);
            }
        }

        void init(byte[] addr, int scope_id) {
            this.setAddr(addr);
            if (scope_id >= 0) {
                this.scope_id = scope_id;
                this.scope_id_set = true;
            }
        }

        void init(byte[] addr, NetworkInterface nif) throws UnknownHostException {
            this.setAddr(addr);
            if (nif != null) {
                this.scope_id = Inet6Address.deriveNumericScope(this.ipaddress, nif);
                this.scope_id_set = true;
                this.scope_ifname = nif;
                this.scope_ifname_set = true;
            }
        }

        String getHostAddress() {
            String s = Inet6Address.numericToTextFormat(this.ipaddress);
            if (this.scope_ifname != null) {
                s = s + "%" + this.scope_ifname.getName();
            } else if (this.scope_id_set) {
                s = s + "%" + this.scope_id;
            }
            return s;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Inet6AddressHolder)) {
                return false;
            }
            Inet6AddressHolder that = (Inet6AddressHolder)o;
            return Arrays.equals(this.ipaddress, that.ipaddress);
        }

        public int hashCode() {
            if (this.ipaddress != null) {
                int hash = 0;
                int i = 0;
                while (i < 16) {
                    int component = 0;
                    for (int j = 0; j < 4 && i < 16; ++j, ++i) {
                        component = (component << 8) + this.ipaddress[i];
                    }
                    hash += component;
                }
                return hash;
            }
            return 0;
        }

        boolean isIPv4CompatibleAddress() {
            return this.ipaddress[0] == 0 && this.ipaddress[1] == 0 && this.ipaddress[2] == 0 && this.ipaddress[3] == 0 && this.ipaddress[4] == 0 && this.ipaddress[5] == 0 && this.ipaddress[6] == 0 && this.ipaddress[7] == 0 && this.ipaddress[8] == 0 && this.ipaddress[9] == 0 && this.ipaddress[10] == 0 && this.ipaddress[11] == 0;
        }

        boolean isMulticastAddress() {
            return (this.ipaddress[0] & 0xFF) == 255;
        }

        boolean isAnyLocalAddress() {
            byte test = 0;
            for (int i = 0; i < 16; ++i) {
                test = (byte)(test | this.ipaddress[i]);
            }
            return test == 0;
        }

        boolean isLoopbackAddress() {
            byte test = 0;
            for (int i = 0; i < 15; ++i) {
                test = (byte)(test | this.ipaddress[i]);
            }
            return test == 0 && this.ipaddress[15] == 1;
        }

        boolean isLinkLocalAddress() {
            return (this.ipaddress[0] & 0xFF) == 254 && (this.ipaddress[1] & 0xC0) == 128;
        }

        boolean isSiteLocalAddress() {
            return (this.ipaddress[0] & 0xFF) == 254 && (this.ipaddress[1] & 0xC0) == 192;
        }

        boolean isMCGlobal() {
            return (this.ipaddress[0] & 0xFF) == 255 && (this.ipaddress[1] & 0xF) == 14;
        }

        boolean isMCNodeLocal() {
            return (this.ipaddress[0] & 0xFF) == 255 && (this.ipaddress[1] & 0xF) == 1;
        }

        boolean isMCLinkLocal() {
            return (this.ipaddress[0] & 0xFF) == 255 && (this.ipaddress[1] & 0xF) == 2;
        }

        boolean isMCSiteLocal() {
            return (this.ipaddress[0] & 0xFF) == 255 && (this.ipaddress[1] & 0xF) == 5;
        }

        boolean isMCOrgLocal() {
            return (this.ipaddress[0] & 0xFF) == 255 && (this.ipaddress[1] & 0xF) == 8;
        }
    }
}

