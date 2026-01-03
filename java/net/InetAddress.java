/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.Inet4AddressImpl;
import java.net.Inet6Address;
import java.net.Inet6AddressImpl;
import java.net.InetAddressImpl;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.net.spi.InetAddressResolver;
import java.net.spi.InetAddressResolverProvider;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Scanner;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import jdk.internal.access.JavaNetInetAddressAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.loader.BootLoader;
import jdk.internal.misc.Blocker;
import jdk.internal.misc.Unsafe;
import jdk.internal.misc.VM;
import jdk.internal.vm.annotation.Stable;
import sun.net.InetAddressCachePolicy;
import sun.net.ResolverProviderConfiguration;
import sun.net.util.IPAddressUtil;
import sun.nio.cs.UTF_8;
import sun.security.action.GetPropertyAction;

public sealed class InetAddress
implements Serializable
permits Inet4Address, Inet6Address {
    static final int IPv4 = 1;
    static final int IPv6 = 2;
    final transient InetAddressHolder holder = new InetAddressHolder();
    @Stable
    private static volatile InetAddressResolver resolver;
    private static final InetAddressResolver BUILTIN_RESOLVER;
    private transient String canonicalHostName = null;
    private static final long serialVersionUID = 3286316764910316507L;
    private static final String PREFER_IPV4_STACK_VALUE;
    private static final String PREFER_IPV6_ADDRESSES_VALUE;
    private static final String HOSTS_FILE_NAME;
    private static final RuntimePermission INET_ADDRESS_RESOLVER_PERMISSION;
    private static final ReentrantLock RESOLVER_LOCK;
    private static volatile InetAddressResolver bootstrapResolver;
    private static final ConcurrentMap<String, Addresses> cache;
    private static final NavigableSet<CachedLookup> expirySet;
    static final InetAddressImpl impl;
    static final InetAddressResolver.LookupPolicy PLATFORM_LOOKUP_POLICY;
    private static volatile CachedLocalHost cachedLocalHost;
    private static final Unsafe UNSAFE;
    private static final long FIELDS_OFFSET;
    private static final ObjectStreamField[] serialPersistentFields;

    InetAddressHolder holder() {
        return this.holder;
    }

    private static final InetAddressResolver.LookupPolicy initializePlatformLookupPolicy() {
        boolean ipv4Available = InetAddress.isIPv4Available();
        if ("true".equals(PREFER_IPV4_STACK_VALUE) && ipv4Available) {
            return InetAddressResolver.LookupPolicy.of(1);
        }
        if (impl instanceof Inet4AddressImpl) {
            return InetAddressResolver.LookupPolicy.of(1);
        }
        if (!ipv4Available) {
            return InetAddressResolver.LookupPolicy.of(2);
        }
        if (PREFER_IPV6_ADDRESSES_VALUE != null) {
            if (PREFER_IPV6_ADDRESSES_VALUE.equalsIgnoreCase("true")) {
                return InetAddressResolver.LookupPolicy.of(11);
            }
            if (PREFER_IPV6_ADDRESSES_VALUE.equalsIgnoreCase("false")) {
                return InetAddressResolver.LookupPolicy.of(7);
            }
            if (PREFER_IPV6_ADDRESSES_VALUE.equalsIgnoreCase("system")) {
                return InetAddressResolver.LookupPolicy.of(3);
            }
        }
        return InetAddressResolver.LookupPolicy.of(7);
    }

    static boolean systemAddressesOrder(int lookupCharacteristics) {
        return (lookupCharacteristics & 0xC) == 0;
    }

    static boolean ipv4AddressesFirst(int lookupCharacteristics) {
        return (lookupCharacteristics & 4) != 0;
    }

    static boolean ipv6AddressesFirst(int lookupCharacteristics) {
        return (lookupCharacteristics & 8) != 0;
    }

    private static native boolean isIPv4Available();

    private static native boolean isIPv6Supported();

    private static InetAddressResolver resolver() {
        InetAddressResolver cns = resolver;
        if (cns != null) {
            return cns;
        }
        if (VM.isBooted()) {
            RESOLVER_LOCK.lock();
            boolean bootstrapSet = false;
            try {
                cns = resolver;
                if (cns != null) {
                    InetAddressResolver inetAddressResolver = cns;
                    return inetAddressResolver;
                }
                if (bootstrapResolver != null) {
                    InetAddressResolver inetAddressResolver = bootstrapResolver;
                    return inetAddressResolver;
                }
                bootstrapResolver = BUILTIN_RESOLVER;
                bootstrapSet = true;
                if (HOSTS_FILE_NAME != null) {
                    cns = BUILTIN_RESOLVER;
                } else if (System.getSecurityManager() != null) {
                    PrivilegedAction<InetAddressResolver> pa = InetAddress::loadResolver;
                    cns = AccessController.doPrivileged(pa, null, INET_ADDRESS_RESOLVER_PERMISSION);
                } else {
                    cns = InetAddress.loadResolver();
                }
                resolver = cns;
                InetAddressResolver inetAddressResolver = cns;
                return inetAddressResolver;
            }
            finally {
                if (bootstrapSet) {
                    bootstrapResolver = null;
                }
                RESOLVER_LOCK.unlock();
            }
        }
        return BUILTIN_RESOLVER;
    }

    private static InetAddressResolver loadResolver() {
        return ServiceLoader.load(InetAddressResolverProvider.class).findFirst().map(nsp -> nsp.get(InetAddress.builtinConfiguration())).orElse(BUILTIN_RESOLVER);
    }

    private static InetAddressResolverProvider.Configuration builtinConfiguration() {
        return new ResolverProviderConfiguration(BUILTIN_RESOLVER, () -> {
            try {
                return impl.getLocalHostName();
            }
            catch (UnknownHostException unknownHostException) {
                return "localhost";
            }
        });
    }

    InetAddress() {
    }

    private Object readResolve() throws ObjectStreamException {
        return new Inet4Address(this.holder().getHostName(), this.holder().getAddress());
    }

    public boolean isMulticastAddress() {
        return false;
    }

    public boolean isAnyLocalAddress() {
        return false;
    }

    public boolean isLoopbackAddress() {
        return false;
    }

    public boolean isLinkLocalAddress() {
        return false;
    }

    public boolean isSiteLocalAddress() {
        return false;
    }

    public boolean isMCGlobal() {
        return false;
    }

    public boolean isMCNodeLocal() {
        return false;
    }

    public boolean isMCLinkLocal() {
        return false;
    }

    public boolean isMCSiteLocal() {
        return false;
    }

    public boolean isMCOrgLocal() {
        return false;
    }

    public boolean isReachable(int timeout) throws IOException {
        return this.isReachable(null, 0, timeout);
    }

    public boolean isReachable(NetworkInterface netif, int ttl, int timeout) throws IOException {
        if (ttl < 0) {
            throw new IllegalArgumentException("ttl can't be negative");
        }
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout can't be negative");
        }
        return impl.isReachable(this, timeout, netif, ttl);
    }

    public String getHostName() {
        return this.getHostName(true);
    }

    String getHostName(boolean check) {
        if (this.holder().getHostName() == null) {
            this.holder().hostName = InetAddress.getHostFromNameService(this, check);
        }
        return this.holder().getHostName();
    }

    public String getCanonicalHostName() {
        String value = this.canonicalHostName;
        if (value == null) {
            this.canonicalHostName = value = InetAddress.getHostFromNameService(this, true);
        }
        return value;
    }

    private static String getHostFromNameService(InetAddress addr, boolean check) {
        String host;
        InetAddressResolver resolver = InetAddress.resolver();
        try {
            SecurityManager sec;
            host = resolver.lookupByAddress(addr.getAddress());
            if (check && (sec = System.getSecurityManager()) != null) {
                sec.checkConnect(host, -1);
            }
            InetAddress[] arr = InetAddress.getAllByName0(host, check);
            boolean ok = false;
            if (arr != null) {
                for (int i = 0; !ok && i < arr.length; ++i) {
                    ok = addr.equals(arr[i]);
                }
            }
            if (!ok) {
                host = addr.getHostAddress();
                return host;
            }
        }
        catch (RuntimeException | UnknownHostException e) {
            host = addr.getHostAddress();
        }
        return host;
    }

    public byte[] getAddress() {
        return null;
    }

    public String getHostAddress() {
        return null;
    }

    public int hashCode() {
        return -1;
    }

    public boolean equals(Object obj) {
        return false;
    }

    public String toString() {
        String hostName = this.holder().getHostName();
        return Objects.toString(hostName, "") + "/" + this.getHostAddress();
    }

    private static InetAddressResolver createBuiltinInetAddressResolver() {
        InetAddressResolver theResolver = HOSTS_FILE_NAME != null ? new HostsFileResolver(HOSTS_FILE_NAME) : new PlatformResolver();
        return theResolver;
    }

    public static InetAddress getByAddress(String host, byte[] addr) throws UnknownHostException {
        if (host != null && !host.isEmpty() && host.charAt(0) == '[' && host.charAt(host.length() - 1) == ']') {
            host = host.substring(1, host.length() - 1);
        }
        if (addr != null) {
            if (addr.length == 4) {
                return new Inet4Address(host, addr);
            }
            if (addr.length == 16) {
                byte[] newAddr = IPAddressUtil.convertFromIPv4MappedAddress(addr);
                if (newAddr != null) {
                    return new Inet4Address(host, newAddr);
                }
                return new Inet6Address(host, addr);
            }
        }
        throw new UnknownHostException("addr is of illegal length");
    }

    public static InetAddress getByName(String host) throws UnknownHostException {
        return InetAddress.getAllByName(host)[0];
    }

    public static InetAddress[] getAllByName(String host) throws UnknownHostException {
        if (host == null || host.isEmpty()) {
            InetAddress[] ret = new InetAddress[]{impl.loopbackAddress()};
            return ret;
        }
        InetAddress.validate(host);
        boolean ipv6Expected = false;
        if (host.charAt(0) == '[') {
            if (host.length() > 2 && host.charAt(host.length() - 1) == ']') {
                host = host.substring(1, host.length() - 1);
                ipv6Expected = true;
            } else {
                throw InetAddress.invalidIPv6LiteralException(host, false);
            }
        }
        if (IPAddressUtil.digit(host.charAt(0), 16) != -1 || host.charAt(0) == ':') {
            byte[] addr = null;
            int numericZone = -1;
            String ifname = null;
            if (!ipv6Expected) {
                try {
                    addr = IPAddressUtil.validateNumericFormatV4((String)host);
                }
                catch (IllegalArgumentException iae) {
                    UnknownHostException uhe = new UnknownHostException(host);
                    uhe.initCause(iae);
                    throw uhe;
                }
            }
            if (addr == null) {
                int pos = host.indexOf(37);
                if (pos != -1 && (numericZone = InetAddress.checkNumericZone(host)) == -1) {
                    ifname = host.substring(pos + 1);
                }
                if ((addr = IPAddressUtil.textToNumericFormatV6(host)) == null && (host.contains(":") || ipv6Expected)) {
                    throw InetAddress.invalidIPv6LiteralException(host, ipv6Expected);
                }
            }
            if (addr != null) {
                InetAddress[] ret = new InetAddress[1];
                if (addr.length == 4) {
                    if (numericZone != -1 || ifname != null) {
                        throw new UnknownHostException(host + ": invalid IPv4-mapped address");
                    }
                    ret[0] = new Inet4Address(null, addr);
                } else {
                    ret[0] = ifname != null ? new Inet6Address(null, addr, ifname) : new Inet6Address(null, addr, numericZone);
                }
                return ret;
            }
        } else if (ipv6Expected) {
            throw InetAddress.invalidIPv6LiteralException(host, true);
        }
        return InetAddress.getAllByName0(host, true, true);
    }

    private static UnknownHostException invalidIPv6LiteralException(String host, boolean wrapInBrackets) {
        String hostString = wrapInBrackets ? "[" + host + "]" : host;
        return new UnknownHostException(hostString + ": invalid IPv6 address literal");
    }

    public static InetAddress getLoopbackAddress() {
        return impl.loopbackAddress();
    }

    private static int checkNumericZone(String s) throws UnknownHostException {
        int percent = s.indexOf(37);
        int slen = s.length();
        int zone = 0;
        int multmax = 0xCCCCCCC;
        if (percent == -1) {
            return -1;
        }
        for (int i = percent + 1; i < slen; ++i) {
            char c = s.charAt(i);
            int digit = IPAddressUtil.parseAsciiDigit(c, 10);
            if (digit < 0) {
                return -1;
            }
            if (zone > multmax) {
                return -1;
            }
            if ((zone = zone * 10 + digit) >= 0) continue;
            return -1;
        }
        return zone;
    }

    static InetAddress[] getAllByName0(String host, boolean check) throws UnknownHostException {
        return InetAddress.getAllByName0(host, check, true);
    }

    private static InetAddress[] getAllByName0(String host, boolean check, boolean useCache) throws UnknownHostException {
        Addresses oldAddrs;
        Addresses addrs;
        SecurityManager security;
        if (check && (security = System.getSecurityManager()) != null) {
            security.checkConnect(host, -1);
        }
        long now = System.nanoTime();
        for (CachedLookup caddrs : expirySet) {
            if (!caddrs.tryRemoveExpiredAddress(now)) break;
        }
        if (useCache) {
            addrs = (Addresses)cache.get(host);
        } else {
            addrs = (Addresses)cache.remove(host);
            if (addrs != null) {
                if (addrs instanceof CachedLookup) {
                    expirySet.remove(addrs);
                }
                addrs = null;
            }
        }
        if (addrs == null && (oldAddrs = cache.putIfAbsent(host, addrs = new NameServiceAddresses(host))) != null) {
            addrs = oldAddrs;
        }
        return (InetAddress[])addrs.get().clone();
    }

    static InetAddress[] getAddressesFromNameService(String host) throws UnknownHostException {
        InetAddress[] result;
        Stream<InetAddress> addresses = null;
        UnknownHostException ex = null;
        InetAddressResolver resolver = InetAddress.resolver();
        try {
            addresses = resolver.lookupByName(host, PLATFORM_LOOKUP_POLICY);
        }
        catch (RuntimeException | UnknownHostException x) {
            if (host.equalsIgnoreCase("localhost")) {
                addresses = Stream.of(impl.loopbackAddress());
            }
            if (x instanceof UnknownHostException) {
                UnknownHostException uhe;
                ex = uhe = (UnknownHostException)x;
            }
            ex = new UnknownHostException();
            ex.initCause(x);
        }
        InetAddress[] inetAddressArray = result = addresses == null ? null : (InetAddress[])addresses.toArray(InetAddress[]::new);
        if (result == null || result.length == 0) {
            throw ex == null ? new UnknownHostException(host) : ex;
        }
        return result;
    }

    public static InetAddress getByAddress(byte[] addr) throws UnknownHostException {
        return InetAddress.getByAddress(null, addr);
    }

    public static InetAddress getLocalHost() throws UnknownHostException {
        SecurityManager security = System.getSecurityManager();
        try {
            InetAddress localAddr;
            CachedLocalHost clh = cachedLocalHost;
            if (clh != null && clh.expiryTime - System.nanoTime() >= 0L) {
                if (security != null) {
                    security.checkConnect(clh.host, -1);
                }
                return clh.addr;
            }
            String local = impl.getLocalHostName();
            if (security != null) {
                security.checkConnect(local, -1);
            }
            if (local.equals("localhost")) {
                localAddr = impl.loopbackAddress();
            } else {
                try {
                    localAddr = InetAddress.getAllByName0(local, false, false)[0];
                }
                catch (UnknownHostException uhe) {
                    UnknownHostException uhe2 = new UnknownHostException(local + ": " + uhe.getMessage());
                    uhe2.initCause(uhe);
                    throw uhe2;
                }
            }
            cachedLocalHost = new CachedLocalHost(local, localAddr);
            return localAddr;
        }
        catch (SecurityException e) {
            return impl.loopbackAddress();
        }
    }

    private static native void init();

    static InetAddress anyLocalAddress() {
        return impl.anyLocalAddress();
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField gf = s.readFields();
        String host = (String)gf.get("hostName", null);
        int address = gf.get("address", 0);
        int family = gf.get("family", 0);
        if (family != 1 && family != 2) {
            throw new InvalidObjectException("invalid address family type: " + family);
        }
        InetAddressHolder h = new InetAddressHolder(host, address, family);
        UNSAFE.putReference(this, FIELDS_OFFSET, h);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        ObjectOutputStream.PutField pf = s.putFields();
        pf.put("hostName", this.holder().getHostName());
        pf.put("address", this.holder().getAddress());
        pf.put("family", this.holder().getFamily());
        s.writeFields();
    }

    private static void validate(String host) throws UnknownHostException {
        if (host.indexOf(0) != -1) {
            throw new UnknownHostException("NUL character not allowed in hostname");
        }
    }

    static {
        PREFER_IPV4_STACK_VALUE = GetPropertyAction.privilegedGetProperty("java.net.preferIPv4Stack");
        PREFER_IPV6_ADDRESSES_VALUE = GetPropertyAction.privilegedGetProperty("java.net.preferIPv6Addresses");
        HOSTS_FILE_NAME = GetPropertyAction.privilegedGetProperty("jdk.net.hosts.file");
        BootLoader.loadLibrary("net");
        SharedSecrets.setJavaNetInetAddressAccess(new JavaNetInetAddressAccess(){

            @Override
            public String getOriginalHostName(InetAddress ia) {
                return ia.holder.getOriginalHostName();
            }

            @Override
            public int addressValue(Inet4Address inet4Address) {
                return inet4Address.addressValue();
            }

            @Override
            public byte[] addressBytes(Inet6Address inet6Address) {
                return inet6Address.addressBytes();
            }
        });
        InetAddress.init();
        INET_ADDRESS_RESOLVER_PERMISSION = new RuntimePermission("inetAddressResolverProvider");
        RESOLVER_LOCK = new ReentrantLock();
        cache = new ConcurrentHashMap<String, Addresses>();
        expirySet = new ConcurrentSkipListSet<CachedLookup>();
        impl = InetAddress.isIPv6Supported() ? new Inet6AddressImpl() : new Inet4AddressImpl();
        PLATFORM_LOOKUP_POLICY = InetAddress.initializePlatformLookupPolicy();
        BUILTIN_RESOLVER = InetAddress.createBuiltinInetAddressResolver();
        UNSAFE = Unsafe.getUnsafe();
        FIELDS_OFFSET = UNSAFE.objectFieldOffset(InetAddress.class, "holder");
        serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("hostName", String.class), new ObjectStreamField("address", Integer.TYPE), new ObjectStreamField("family", Integer.TYPE)};
    }

    static class InetAddressHolder {
        String originalHostName;
        String hostName;
        int address;
        int family;

        InetAddressHolder() {
        }

        InetAddressHolder(String hostName, int address, int family) {
            this.originalHostName = hostName;
            this.hostName = hostName;
            this.address = address;
            this.family = family;
        }

        void init(String hostName, int family) {
            this.originalHostName = hostName;
            this.hostName = hostName;
            if (family != -1) {
                this.family = family;
            }
        }

        String getHostName() {
            return this.hostName;
        }

        String getOriginalHostName() {
            return this.originalHostName;
        }

        int getAddress() {
            return this.address;
        }

        int getFamily() {
            return this.family;
        }
    }

    private static final class HostsFileResolver
    implements InetAddressResolver {
        private final String hostsFile;

        public HostsFileResolver(String hostsFileName) {
            this.hostsFile = hostsFileName;
        }

        @Override
        public String lookupByAddress(byte[] addr) throws UnknownHostException {
            String host = null;
            Objects.requireNonNull(addr);
            if (addr.length != 4 && addr.length != 16) {
                throw new IllegalArgumentException("Invalid address length");
            }
            try (Scanner hostsFileScanner = new Scanner(new File(this.hostsFile), (Charset)UTF_8.INSTANCE);){
                while (hostsFileScanner.hasNextLine()) {
                    String[] mapping;
                    String hostEntry = hostsFileScanner.nextLine();
                    if (hostEntry.startsWith("#") || (mapping = (hostEntry = this.removeComments(hostEntry)).split("\\s+")).length < 2 || !Arrays.equals(addr, this.createAddressByteArray(mapping[0]))) continue;
                    host = mapping[1];
                    break;
                }
            }
            catch (IOException e) {
                throw new UnknownHostException("Unable to resolve address " + Arrays.toString(addr) + " as hosts file " + this.hostsFile + " not found ");
            }
            if (host == null || host.isEmpty() || host.equals(" ")) {
                throw new UnknownHostException("Requested address " + Arrays.toString(addr) + " resolves to an invalid entry in hosts file " + this.hostsFile);
            }
            return host;
        }

        @Override
        public Stream<InetAddress> lookupByName(String host, InetAddressResolver.LookupPolicy lookupPolicy) throws UnknownHostException {
            Objects.requireNonNull(host);
            Objects.requireNonNull(lookupPolicy);
            ArrayList<InetAddress> inetAddresses = new ArrayList<InetAddress>();
            ArrayList<InetAddress> inet4Addresses = new ArrayList<InetAddress>();
            ArrayList<InetAddress> inet6Addresses = new ArrayList<InetAddress>();
            int flags = lookupPolicy.characteristics();
            boolean needIPv4 = (flags & 1) != 0;
            boolean needIPv6 = (flags & 2) != 0;
            try (Scanner hostsFileScanner = new Scanner(new File(this.hostsFile), (Charset)UTF_8.INSTANCE);){
                while (hostsFileScanner.hasNextLine()) {
                    byte[] addr;
                    String addrStr;
                    String hostEntry = hostsFileScanner.nextLine();
                    if (hostEntry.startsWith("#") || !(hostEntry = this.removeComments(hostEntry)).contains(host) || (addrStr = this.extractHostAddr(hostEntry, host)) == null || addrStr.isEmpty() || (addr = this.createAddressByteArray(addrStr)) == null) continue;
                    InetAddress address = InetAddress.getByAddress(host, addr);
                    inetAddresses.add(address);
                    if (address instanceof Inet4Address && needIPv4) {
                        inet4Addresses.add(address);
                    }
                    if (!(address instanceof Inet6Address) || !needIPv6) continue;
                    inet6Addresses.add(address);
                }
            }
            catch (IOException e) {
                throw new UnknownHostException("Unable to resolve host " + host + " as hosts file " + this.hostsFile + " not found ");
            }
            if (needIPv4 && !needIPv6) {
                this.checkResultsList(inet4Addresses, host);
                return inet4Addresses.stream();
            }
            if (!needIPv4 && needIPv6) {
                this.checkResultsList(inet6Addresses, host);
                return inet6Addresses.stream();
            }
            this.checkResultsList(inetAddresses, host);
            if (InetAddress.ipv6AddressesFirst(flags)) {
                return Stream.concat(inet6Addresses.stream(), inet4Addresses.stream());
            }
            if (InetAddress.ipv4AddressesFirst(flags)) {
                return Stream.concat(inet4Addresses.stream(), inet6Addresses.stream());
            }
            assert (InetAddress.systemAddressesOrder(flags));
            return inetAddresses.stream();
        }

        private void checkResultsList(List<InetAddress> addressesList, String hostName) throws UnknownHostException {
            if (addressesList.isEmpty()) {
                throw new UnknownHostException("Unable to resolve host " + hostName + " in hosts file " + this.hostsFile);
            }
        }

        private String removeComments(String hostsEntry) {
            String filteredEntry = hostsEntry;
            int hashIndex = hostsEntry.indexOf("#");
            if (hashIndex != -1) {
                filteredEntry = hostsEntry.substring(0, hashIndex);
            }
            return filteredEntry;
        }

        private byte[] createAddressByteArray(String addrStr) {
            byte[] addrArray;
            try {
                addrArray = IPAddressUtil.validateNumericFormatV4(addrStr, false);
            }
            catch (IllegalArgumentException iae) {
                return null;
            }
            if (addrArray == null) {
                addrArray = IPAddressUtil.textToNumericFormatV6(addrStr);
            }
            return addrArray;
        }

        private String extractHostAddr(String hostEntry, String host) {
            String[] mapping = hostEntry.split("\\s+");
            String hostAddr = null;
            if (mapping.length >= 2) {
                for (int i = 1; i < mapping.length; ++i) {
                    if (!mapping[i].equalsIgnoreCase(host)) continue;
                    hostAddr = mapping[0];
                }
            }
            return hostAddr;
        }
    }

    private static final class PlatformResolver
    implements InetAddressResolver {
        private PlatformResolver() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public Stream<InetAddress> lookupByName(String host, InetAddressResolver.LookupPolicy policy) throws UnknownHostException {
            InetAddress[] addrs;
            Objects.requireNonNull(host);
            Objects.requireNonNull(policy);
            InetAddress.validate(host);
            long comp = Blocker.begin();
            try {
                addrs = impl.lookupAllHostAddr(host, policy);
            }
            finally {
                Blocker.end(comp);
            }
            return Arrays.stream(addrs);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public String lookupByAddress(byte[] addr) throws UnknownHostException {
            Objects.requireNonNull(addr);
            if (addr.length != 4 && addr.length != 16) {
                throw new IllegalArgumentException("Invalid address length");
            }
            long comp = Blocker.begin();
            try {
                String string = impl.getHostByAddr(addr);
                return string;
            }
            finally {
                Blocker.end(comp);
            }
        }
    }

    private static class CachedLookup
    implements Addresses,
    Comparable<CachedLookup> {
        private static final AtomicLong seq = new AtomicLong();
        final String host;
        volatile InetAddress[] inetAddresses;
        volatile long expiryTime;
        final long id = seq.incrementAndGet();

        CachedLookup(String host, InetAddress[] inetAddresses, long expiryTime) {
            this.host = host;
            this.inetAddresses = inetAddresses;
            this.expiryTime = expiryTime;
        }

        @Override
        public InetAddress[] get() throws UnknownHostException {
            if (this.inetAddresses == null) {
                throw new UnknownHostException(this.host);
            }
            return this.inetAddresses;
        }

        @Override
        public int compareTo(CachedLookup other) {
            long diff = this.expiryTime - other.expiryTime;
            if (diff < 0L) {
                return -1;
            }
            if (diff > 0L) {
                return 1;
            }
            return Long.compare(this.id, other.id);
        }

        public boolean tryRemoveExpiredAddress(long now) {
            if (this.expiryTime - now < 0L) {
                if (expirySet.remove(this)) {
                    cache.remove(this.host, this);
                }
                return true;
            }
            return false;
        }
    }

    private static interface Addresses {
        public InetAddress[] get() throws UnknownHostException;
    }

    private static final class NameServiceAddresses
    implements Addresses {
        private final String host;
        private final ReentrantLock lookupLock = new ReentrantLock();

        NameServiceAddresses(String host) {
            this.host = host;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public InetAddress[] get() throws UnknownHostException {
            Addresses addresses;
            block12: {
                this.lookupLock.lock();
                try {
                    int cachePolicy;
                    UnknownHostException ex;
                    InetAddress[] inetAddresses;
                    addresses = cache.putIfAbsent(this.host, this);
                    if (addresses == null) {
                        addresses = this;
                    }
                    if (addresses != this) break block12;
                    try {
                        inetAddresses = InetAddress.getAddressesFromNameService(this.host);
                        ex = null;
                        cachePolicy = InetAddressCachePolicy.get();
                    }
                    catch (UnknownHostException uhe) {
                        inetAddresses = null;
                        ex = uhe;
                        cachePolicy = InetAddressCachePolicy.getNegative();
                    }
                    if (cachePolicy == 0) {
                        cache.remove(this.host, this);
                    } else {
                        CachedLookup cachedLookup;
                        long expiryTime;
                        long now = System.nanoTime();
                        long l = expiryTime = cachePolicy == -1 ? 0L : now + 1000000000L * (long)cachePolicy;
                        if (InetAddressCachePolicy.getStale() > 0 && ex == null && expiryTime > 0L) {
                            long refreshTime = expiryTime;
                            expiryTime = refreshTime + 1000000000L * (long)InetAddressCachePolicy.getStale();
                            cachedLookup = new ValidCachedLookup(this.host, inetAddresses, expiryTime, refreshTime);
                        } else {
                            cachedLookup = new CachedLookup(this.host, inetAddresses, expiryTime);
                        }
                        if (cache.replace(this.host, this, cachedLookup) && cachePolicy != -1) {
                            expirySet.add(cachedLookup);
                        }
                    }
                    if (inetAddresses == null || inetAddresses.length == 0) {
                        throw ex == null ? new UnknownHostException(this.host) : ex;
                    }
                    InetAddress[] inetAddressArray = inetAddresses;
                    return inetAddressArray;
                }
                finally {
                    this.lookupLock.unlock();
                }
            }
            return addresses.get();
        }
    }

    private static final class CachedLocalHost {
        final String host;
        final InetAddress addr;
        final long expiryTime = System.nanoTime() + 5000000000L;

        CachedLocalHost(String host, InetAddress addr) {
            this.host = host;
            this.addr = addr;
        }
    }

    private static final class ValidCachedLookup
    extends CachedLookup {
        private volatile long refreshTime;
        private volatile long staleTime;
        private final Lock lookupLock = new ReentrantLock();

        ValidCachedLookup(String host, InetAddress[] inetAddresses, long staleTime, long refreshTime) {
            super(host, inetAddresses, staleTime);
            this.refreshTime = refreshTime;
            this.staleTime = staleTime;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public InetAddress[] get() {
            long now = System.nanoTime();
            if (this.refreshTime - now < 0L && this.lookupLock.tryLock()) {
                try {
                    this.refreshTime = now + (long)InetAddressCachePolicy.get() * 1000000000L;
                    this.inetAddresses = InetAddress.getAddressesFromNameService(this.host);
                    this.staleTime = this.refreshTime + (long)InetAddressCachePolicy.getStale() * 1000000000L;
                }
                catch (UnknownHostException unknownHostException) {
                }
                finally {
                    this.lookupLock.unlock();
                }
            }
            return this.inetAddresses;
        }

        @Override
        public boolean tryRemoveExpiredAddress(long now) {
            if (this.expiryTime - now < 0L) {
                if (this.staleTime - now < 0L) {
                    return super.tryRemoveExpiredAddress(now);
                }
                if (expirySet.remove(this)) {
                    this.expiryTime = this.staleTime;
                    expirySet.add(this);
                }
            }
            return false;
        }
    }
}

