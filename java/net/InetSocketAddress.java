/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import jdk.internal.misc.Unsafe;

public class InetSocketAddress
extends SocketAddress {
    private final transient InetSocketAddressHolder holder;
    private static final long serialVersionUID = 5076001401234631237L;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("hostname", String.class), new ObjectStreamField("addr", InetAddress.class), new ObjectStreamField("port", Integer.TYPE)};
    private static final Unsafe UNSAFE = Unsafe.getUnsafe();
    private static final long FIELDS_OFFSET = UNSAFE.objectFieldOffset(InetSocketAddress.class, "holder");

    private static int checkPort(int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port out of range:" + port);
        }
        return port;
    }

    private static String checkHost(String hostname) {
        if (hostname == null) {
            throw new IllegalArgumentException("hostname can't be null");
        }
        return hostname;
    }

    public InetSocketAddress(int port) {
        this(InetAddress.anyLocalAddress(), port);
    }

    public InetSocketAddress(InetAddress addr, int port) {
        this.holder = new InetSocketAddressHolder(null, addr == null ? InetAddress.anyLocalAddress() : addr, InetSocketAddress.checkPort(port));
    }

    public InetSocketAddress(String hostname, int port) {
        InetSocketAddress.checkHost(hostname);
        InetAddress addr = null;
        String host = null;
        try {
            addr = InetAddress.getByName(hostname);
        }
        catch (UnknownHostException e) {
            host = hostname;
        }
        this.holder = new InetSocketAddressHolder(host, addr, InetSocketAddress.checkPort(port));
    }

    private InetSocketAddress(int port, String hostname) {
        this.holder = new InetSocketAddressHolder(hostname, null, port);
    }

    public static InetSocketAddress createUnresolved(String host, int port) {
        return new InetSocketAddress(InetSocketAddress.checkPort(port), InetSocketAddress.checkHost(host));
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField pfields = out.putFields();
        pfields.put("hostname", this.holder.hostname);
        pfields.put("addr", this.holder.addr);
        pfields.put("port", this.holder.port);
        out.writeFields();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField oisFields = in.readFields();
        String oisHostname = (String)oisFields.get("hostname", null);
        InetAddress oisAddr = (InetAddress)oisFields.get("addr", null);
        int oisPort = oisFields.get("port", -1);
        InetSocketAddress.checkPort(oisPort);
        if (oisHostname == null && oisAddr == null) {
            throw new InvalidObjectException("hostname and addr can't both be null");
        }
        InetSocketAddressHolder h = new InetSocketAddressHolder(oisHostname, oisAddr, oisPort);
        UNSAFE.putReference(this, FIELDS_OFFSET, h);
    }

    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("Stream data required");
    }

    public final int getPort() {
        return this.holder.getPort();
    }

    public final InetAddress getAddress() {
        return this.holder.getAddress();
    }

    public final String getHostName() {
        return this.holder.getHostName();
    }

    public final String getHostString() {
        return this.holder.getHostString();
    }

    public final boolean isUnresolved() {
        return this.holder.isUnresolved();
    }

    public String toString() {
        return this.holder.toString();
    }

    public final boolean equals(Object obj) {
        if (obj instanceof InetSocketAddress) {
            InetSocketAddress addr = (InetSocketAddress)obj;
            return this.holder.equals(addr.holder);
        }
        return false;
    }

    public final int hashCode() {
        return this.holder.hashCode();
    }

    private static final class InetSocketAddressHolder {
        private final String hostname;
        private final InetAddress addr;
        private final int port;

        private InetSocketAddressHolder(String hostname, InetAddress addr, int port) {
            this.hostname = hostname;
            this.addr = addr;
            this.port = port;
        }

        private int getPort() {
            return this.port;
        }

        private InetAddress getAddress() {
            return this.addr;
        }

        private String getHostName() {
            if (this.hostname != null) {
                return this.hostname;
            }
            if (this.addr != null) {
                return this.addr.getHostName();
            }
            return null;
        }

        private String getHostString() {
            if (this.hostname != null) {
                return this.hostname;
            }
            if (this.addr != null) {
                if (this.addr.holder().getHostName() != null) {
                    return this.addr.holder().getHostName();
                }
                return this.addr.getHostAddress();
            }
            return null;
        }

        private boolean isUnresolved() {
            return this.addr == null;
        }

        public String toString() {
            String formatted;
            if (this.isUnresolved()) {
                formatted = this.hostname + "/<unresolved>";
            } else {
                formatted = this.addr.toString();
                if (this.addr instanceof Inet6Address) {
                    int i = formatted.lastIndexOf("/");
                    formatted = formatted.substring(0, i + 1) + "[" + formatted.substring(i + 1) + "]";
                }
            }
            return formatted + ":" + this.port;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof InetSocketAddressHolder)) {
                return false;
            }
            InetSocketAddressHolder that = (InetSocketAddressHolder)obj;
            boolean sameIP = this.addr != null ? this.addr.equals(that.addr) : (this.hostname != null ? that.addr == null && this.hostname.equalsIgnoreCase(that.hostname) : that.addr == null && that.hostname == null);
            return sameIP && this.port == that.port;
        }

        public final int hashCode() {
            if (this.addr != null) {
                return this.addr.hashCode() + this.port;
            }
            if (this.hostname != null) {
                return this.hostname.toLowerCase(Locale.ROOT).hashCode() + this.port;
            }
            return this.port;
        }
    }
}

