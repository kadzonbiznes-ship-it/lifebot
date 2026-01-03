/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.net.HttpConnectSocketImpl;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketImplFactory;
import java.net.SocketOption;
import java.net.SocketTimeoutException;
import java.net.SocksSocketImpl;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import sun.net.ApplicationProxy;
import sun.security.util.SecurityConstants;

public class Socket
implements Closeable {
    private static final VarHandle STATE;
    private static final VarHandle IN;
    private static final VarHandle OUT;
    private volatile SocketImpl impl;
    private static final int SOCKET_CREATED = 1;
    private static final int BOUND = 2;
    private static final int CONNECTED = 4;
    private static final int CLOSED = 8;
    private static final int SHUT_IN = 512;
    private static final int SHUT_OUT = 1024;
    private volatile int state;
    private final Object socketLock = new Object();
    private volatile InputStream in;
    private volatile OutputStream out;
    private static volatile SocketImplFactory factory;
    private volatile Set<SocketOption<?>> options;

    private int getAndBitwiseOrState(int mask) {
        return STATE.getAndBitwiseOr(this, mask);
    }

    private static boolean isBound(int s) {
        return (s & 2) != 0;
    }

    private static boolean isConnected(int s) {
        return (s & 4) != 0;
    }

    private static boolean isClosed(int s) {
        return (s & 8) != 0;
    }

    private static boolean isInputShutdown(int s) {
        return (s & 0x200) != 0;
    }

    private static boolean isOutputShutdown(int s) {
        return (s & 0x400) != 0;
    }

    private Socket(Void unused, SocketImpl impl) {
        if (impl != null) {
            this.impl = impl;
        }
    }

    public Socket() {
        this.impl = Socket.createImpl();
    }

    public Socket(Proxy proxy) {
        if (proxy == null) {
            throw new IllegalArgumentException("Invalid Proxy");
        }
        Proxy p = proxy == Proxy.NO_PROXY ? Proxy.NO_PROXY : ApplicationProxy.create(proxy);
        Proxy.Type type = p.type();
        if (type == Proxy.Type.SOCKS || type == Proxy.Type.HTTP) {
            SecurityManager security = System.getSecurityManager();
            InetSocketAddress epoint = (InetSocketAddress)p.address();
            if (epoint.getAddress() != null) {
                this.checkAddress(epoint.getAddress(), "Socket");
            }
            if (security != null) {
                if (epoint.isUnresolved()) {
                    epoint = new InetSocketAddress(epoint.getHostName(), epoint.getPort());
                }
                if (epoint.isUnresolved()) {
                    security.checkConnect(epoint.getHostName(), epoint.getPort());
                } else {
                    security.checkConnect(epoint.getAddress().getHostAddress(), epoint.getPort());
                }
            }
            Object delegate = SocketImpl.createPlatformSocketImpl(false);
            this.impl = type == Proxy.Type.SOCKS ? new SocksSocketImpl(p, (SocketImpl)delegate) : new HttpConnectSocketImpl(p, (SocketImpl)delegate, this);
        } else if (p == Proxy.NO_PROXY) {
            SocketImplFactory factory = Socket.factory;
            this.impl = factory == null ? SocketImpl.createPlatformSocketImpl(false) : factory.createSocketImpl();
        } else {
            throw new IllegalArgumentException("Invalid Proxy");
        }
    }

    protected Socket(SocketImpl impl) throws SocketException {
        this(Socket.checkPermission(impl), impl);
    }

    private static Void checkPermission(SocketImpl impl) {
        SecurityManager sm;
        if (impl != null && (sm = System.getSecurityManager()) != null) {
            sm.checkPermission(SecurityConstants.SET_SOCKETIMPL_PERMISSION);
        }
        return null;
    }

    public Socket(String host, int port) throws UnknownHostException, IOException {
        this(host != null ? new InetSocketAddress(host, port) : new InetSocketAddress(InetAddress.getByName(null), port), null, true);
    }

    public Socket(InetAddress address, int port) throws IOException {
        this(address != null ? new InetSocketAddress(address, port) : null, null, true);
    }

    public Socket(String host, int port, InetAddress localAddr, int localPort) throws IOException {
        this(host != null ? new InetSocketAddress(host, port) : new InetSocketAddress(InetAddress.getByName(null), port), new InetSocketAddress(localAddr, localPort), true);
    }

    public Socket(InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException {
        this(address != null ? new InetSocketAddress(address, port) : null, new InetSocketAddress(localAddr, localPort), true);
    }

    @Deprecated
    public Socket(String host, int port, boolean stream) throws IOException {
        this(host != null ? new InetSocketAddress(host, port) : new InetSocketAddress(InetAddress.getByName(null), port), null, stream);
    }

    @Deprecated
    public Socket(InetAddress host, int port, boolean stream) throws IOException {
        this(host != null ? new InetSocketAddress(host, port) : null, new InetSocketAddress(0), stream);
    }

    private Socket(SocketAddress address, SocketAddress localAddr, boolean stream) throws IOException {
        Objects.requireNonNull(address);
        SocketImpl impl = Socket.createImpl();
        impl.create(stream);
        this.impl = impl;
        this.state = 1;
        try {
            if (localAddr != null) {
                this.bind(localAddr);
            }
            this.connect(address);
        }
        catch (IOException | IllegalArgumentException | SecurityException e) {
            try {
                this.close();
            }
            catch (IOException ce) {
                e.addSuppressed(ce);
            }
            throw e;
        }
    }

    private static SocketImpl createImpl() {
        SocketImplFactory factory = Socket.factory;
        if (factory != null) {
            return factory.createSocketImpl();
        }
        Object delegate = SocketImpl.createPlatformSocketImpl(false);
        return new SocksSocketImpl((SocketImpl)delegate);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private SocketImpl getImpl() throws SocketException {
        if ((this.state & 1) == 0) {
            Object object = this.socketLock;
            synchronized (object) {
                int s = this.state;
                if ((s & 1) == 0) {
                    if (Socket.isClosed(s)) {
                        throw new SocketException("Socket is closed");
                    }
                    SocketImpl impl = this.impl;
                    if (impl == null) {
                        this.impl = impl = Socket.createImpl();
                    }
                    try {
                        impl.create(true);
                    }
                    catch (SocketException e) {
                        throw e;
                    }
                    catch (IOException e) {
                        throw new SocketException(e.getMessage(), e);
                    }
                    this.getAndBitwiseOrState(1);
                }
            }
        }
        return this.impl;
    }

    SocketImpl impl() {
        return this.impl;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void setConnectedImpl(SocketImpl si) throws SocketException {
        Object object = this.socketLock;
        synchronized (object) {
            if ((this.state & 8) != 0) {
                throw new SocketException("Socket is closed");
            }
            SocketImpl previous = this.impl;
            this.impl = si;
            this.state = 7;
            if (previous != null) {
                this.in = null;
                this.out = null;
                previous.closeQuietly();
            }
        }
    }

    void setImpl(SocketImpl si) {
        this.impl = si;
    }

    void setConnected() {
        this.getAndBitwiseOrState(7);
    }

    public void connect(SocketAddress endpoint) throws IOException {
        this.connect(endpoint, 0);
    }

    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        if (endpoint == null) {
            throw new IllegalArgumentException("connect: The address can't be null");
        }
        if (timeout < 0) {
            throw new IllegalArgumentException("connect: timeout can't be negative");
        }
        int s = this.state;
        if (Socket.isClosed(s)) {
            throw new SocketException("Socket is closed");
        }
        if (Socket.isConnected(s)) {
            throw new SocketException("already connected");
        }
        if (!(endpoint instanceof InetSocketAddress)) {
            throw new IllegalArgumentException("Unsupported address type");
        }
        InetSocketAddress epoint = (InetSocketAddress)endpoint;
        InetAddress addr = epoint.getAddress();
        int port = epoint.getPort();
        this.checkAddress(addr, "connect");
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            if (epoint.isUnresolved()) {
                security.checkConnect(epoint.getHostName(), port);
            } else {
                security.checkConnect(addr.getHostAddress(), port);
            }
        }
        try {
            this.getImpl().connect(epoint, timeout);
        }
        catch (SocketTimeoutException e) {
            throw e;
        }
        catch (InterruptedIOException e) {
            Thread thread = Thread.currentThread();
            if (thread.isVirtual() && thread.isInterrupted()) {
                this.close();
                throw new SocketException("Closed by interrupt");
            }
            throw e;
        }
        this.getAndBitwiseOrState(6);
    }

    public void bind(SocketAddress bindpoint) throws IOException {
        int s = this.state;
        if (Socket.isClosed(s)) {
            throw new SocketException("Socket is closed");
        }
        if (Socket.isBound(s)) {
            throw new SocketException("Already bound");
        }
        if (bindpoint != null && !(bindpoint instanceof InetSocketAddress)) {
            throw new IllegalArgumentException("Unsupported address type");
        }
        InetSocketAddress epoint = (InetSocketAddress)bindpoint;
        if (epoint != null && epoint.isUnresolved()) {
            throw new SocketException("Unresolved address");
        }
        if (epoint == null) {
            epoint = new InetSocketAddress(0);
        }
        InetAddress addr = epoint.getAddress();
        int port = epoint.getPort();
        this.checkAddress(addr, "bind");
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkListen(port);
        }
        this.getImpl().bind(addr, port);
        this.getAndBitwiseOrState(2);
    }

    private void checkAddress(InetAddress addr, String op) {
        if (addr == null) {
            return;
        }
        if (!(addr instanceof Inet4Address) && !(addr instanceof Inet6Address)) {
            throw new IllegalArgumentException(op + ": invalid address type");
        }
    }

    public InetAddress getInetAddress() {
        if (!this.isConnected()) {
            return null;
        }
        try {
            return this.getImpl().getInetAddress();
        }
        catch (SocketException socketException) {
            return null;
        }
    }

    public InetAddress getLocalAddress() {
        if (!this.isBound()) {
            return InetAddress.anyLocalAddress();
        }
        InetAddress in = null;
        try {
            in = (InetAddress)this.getImpl().getOption(15);
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkConnect(in.getHostAddress(), -1);
            }
            if (in.isAnyLocalAddress()) {
                in = InetAddress.anyLocalAddress();
            }
        }
        catch (SecurityException e) {
            in = InetAddress.getLoopbackAddress();
        }
        catch (Exception e) {
            in = InetAddress.anyLocalAddress();
        }
        return in;
    }

    public int getPort() {
        if (!this.isConnected()) {
            return 0;
        }
        try {
            return this.getImpl().getPort();
        }
        catch (SocketException socketException) {
            return -1;
        }
    }

    public int getLocalPort() {
        if (!this.isBound()) {
            return -1;
        }
        try {
            return this.getImpl().getLocalPort();
        }
        catch (SocketException socketException) {
            return -1;
        }
    }

    public SocketAddress getRemoteSocketAddress() {
        if (!this.isConnected()) {
            return null;
        }
        return new InetSocketAddress(this.getInetAddress(), this.getPort());
    }

    public SocketAddress getLocalSocketAddress() {
        if (!this.isBound()) {
            return null;
        }
        return new InetSocketAddress(this.getLocalAddress(), this.getLocalPort());
    }

    public SocketChannel getChannel() {
        return null;
    }

    public InputStream getInputStream() throws IOException {
        int s = this.state;
        if (Socket.isClosed(s)) {
            throw new SocketException("Socket is closed");
        }
        if (!Socket.isConnected(s)) {
            throw new SocketException("Socket is not connected");
        }
        if (Socket.isInputShutdown(s)) {
            throw new SocketException("Socket input is shutdown");
        }
        InputStream in = this.in;
        if (in == null && !IN.compareAndSet(this, null, in = new SocketInputStream(this, this.impl.getInputStream()))) {
            in = this.in;
        }
        return in;
    }

    public OutputStream getOutputStream() throws IOException {
        int s = this.state;
        if (Socket.isClosed(s)) {
            throw new SocketException("Socket is closed");
        }
        if (!Socket.isConnected(s)) {
            throw new SocketException("Socket is not connected");
        }
        if (Socket.isOutputShutdown(s)) {
            throw new SocketException("Socket output is shutdown");
        }
        OutputStream out = this.out;
        if (out == null && !OUT.compareAndSet(this, null, out = new SocketOutputStream(this, this.impl.getOutputStream()))) {
            out = this.out;
        }
        return out;
    }

    public void setTcpNoDelay(boolean on) throws SocketException {
        if (this.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        this.getImpl().setOption(1, (Object)on);
    }

    public boolean getTcpNoDelay() throws SocketException {
        if (this.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        return (Boolean)this.getImpl().getOption(1);
    }

    public void setSoLinger(boolean on, int linger) throws SocketException {
        if (this.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        if (!on) {
            this.getImpl().setOption(128, (Object)on);
        } else {
            if (linger < 0) {
                throw new IllegalArgumentException("invalid value for SO_LINGER");
            }
            if (linger > 65535) {
                linger = 65535;
            }
            this.getImpl().setOption(128, (Object)linger);
        }
    }

    public int getSoLinger() throws SocketException {
        if (this.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        Object o = this.getImpl().getOption(128);
        if (o instanceof Integer) {
            Integer i = (Integer)o;
            return i;
        }
        return -1;
    }

    public void sendUrgentData(int data) throws IOException {
        if (!this.getImpl().supportsUrgentData()) {
            throw new SocketException("Urgent data not supported");
        }
        this.getImpl().sendUrgentData(data);
    }

    public void setOOBInline(boolean on) throws SocketException {
        if (this.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        this.getImpl().setOption(4099, (Object)on);
    }

    public boolean getOOBInline() throws SocketException {
        if (this.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        return (Boolean)this.getImpl().getOption(4099);
    }

    public void setSoTimeout(int timeout) throws SocketException {
        if (this.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout can't be negative");
        }
        this.getImpl().setOption(4102, (Object)timeout);
    }

    public int getSoTimeout() throws SocketException {
        if (this.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        Object o = this.getImpl().getOption(4102);
        if (o instanceof Integer) {
            Integer i = (Integer)o;
            return i;
        }
        return 0;
    }

    public void setSendBufferSize(int size) throws SocketException {
        if (size <= 0) {
            throw new IllegalArgumentException("negative send size");
        }
        if (this.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        this.getImpl().setOption(4097, (Object)size);
    }

    public int getSendBufferSize() throws SocketException {
        if (this.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        int result = 0;
        Object o = this.getImpl().getOption(4097);
        if (o instanceof Integer) {
            Integer i = (Integer)o;
            result = i;
        }
        return result;
    }

    public void setReceiveBufferSize(int size) throws SocketException {
        if (size <= 0) {
            throw new IllegalArgumentException("invalid receive size");
        }
        if (this.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        this.getImpl().setOption(4098, (Object)size);
    }

    public int getReceiveBufferSize() throws SocketException {
        if (this.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        int result = 0;
        Object o = this.getImpl().getOption(4098);
        if (o instanceof Integer) {
            Integer i = (Integer)o;
            result = i;
        }
        return result;
    }

    public void setKeepAlive(boolean on) throws SocketException {
        if (this.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        this.getImpl().setOption(8, (Object)on);
    }

    public boolean getKeepAlive() throws SocketException {
        if (this.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        return (Boolean)this.getImpl().getOption(8);
    }

    public void setTrafficClass(int tc) throws SocketException {
        if (tc < 0 || tc > 255) {
            throw new IllegalArgumentException("tc is not in range 0 -- 255");
        }
        if (this.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        this.getImpl().setOption(3, (Object)tc);
    }

    public int getTrafficClass() throws SocketException {
        return (Integer)this.getImpl().getOption(3);
    }

    public void setReuseAddress(boolean on) throws SocketException {
        if (this.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        this.getImpl().setOption(4, (Object)on);
    }

    public boolean getReuseAddress() throws SocketException {
        if (this.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        return (Boolean)this.getImpl().getOption(4);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() throws IOException {
        Object object = this.socketLock;
        synchronized (object) {
            int s;
            if ((this.state & 8) == 0 && ((s = this.getAndBitwiseOrState(8)) & 9) == 1) {
                this.impl.close();
            }
        }
    }

    public void shutdownInput() throws IOException {
        int s = this.state;
        if (Socket.isClosed(s)) {
            throw new SocketException("Socket is closed");
        }
        if (!Socket.isConnected(s)) {
            throw new SocketException("Socket is not connected");
        }
        if (Socket.isInputShutdown(s)) {
            throw new SocketException("Socket input is already shutdown");
        }
        this.getImpl().shutdownInput();
        this.getAndBitwiseOrState(512);
    }

    public void shutdownOutput() throws IOException {
        int s = this.state;
        if (Socket.isClosed(s)) {
            throw new SocketException("Socket is closed");
        }
        if (!Socket.isConnected(s)) {
            throw new SocketException("Socket is not connected");
        }
        if (Socket.isOutputShutdown(s)) {
            throw new SocketException("Socket output is already shutdown");
        }
        this.getImpl().shutdownOutput();
        this.getAndBitwiseOrState(1024);
    }

    public String toString() {
        try {
            if (this.isConnected()) {
                return "Socket[addr=" + this.getImpl().getInetAddress() + ",port=" + this.getImpl().getPort() + ",localport=" + this.getImpl().getLocalPort() + "]";
            }
        }
        catch (SocketException socketException) {
            // empty catch block
        }
        return "Socket[unconnected]";
    }

    public boolean isConnected() {
        return Socket.isConnected(this.state);
    }

    public boolean isBound() {
        return Socket.isBound(this.state);
    }

    public boolean isClosed() {
        return Socket.isClosed(this.state);
    }

    public boolean isInputShutdown() {
        return Socket.isInputShutdown(this.state);
    }

    public boolean isOutputShutdown() {
        return Socket.isOutputShutdown(this.state);
    }

    static SocketImplFactory socketImplFactory() {
        return factory;
    }

    @Deprecated(since="17")
    public static synchronized void setSocketImplFactory(SocketImplFactory fac) throws IOException {
        if (factory != null) {
            throw new SocketException("factory already defined");
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        factory = fac;
    }

    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
    }

    public <T> Socket setOption(SocketOption<T> name, T value) throws IOException {
        Objects.requireNonNull(name);
        if (this.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        this.getImpl().setOption(name, value);
        return this;
    }

    public <T> T getOption(SocketOption<T> name) throws IOException {
        Objects.requireNonNull(name);
        if (this.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        return this.getImpl().getOption(name);
    }

    public Set<SocketOption<?>> supportedOptions() {
        Set<SocketOption<?>> so = this.options;
        if (so != null) {
            return so;
        }
        try {
            SocketImpl impl = this.getImpl();
            this.options = Collections.unmodifiableSet(impl.supportedOptions());
        }
        catch (IOException e) {
            this.options = Collections.emptySet();
        }
        return this.options;
    }

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            STATE = l.findVarHandle(Socket.class, "state", Integer.TYPE);
            IN = l.findVarHandle(Socket.class, "in", InputStream.class);
            OUT = l.findVarHandle(Socket.class, "out", OutputStream.class);
        }
        catch (Exception e) {
            throw new InternalError(e);
        }
    }

    private static class SocketInputStream
    extends InputStream {
        private final Socket parent;
        private final InputStream in;

        SocketInputStream(Socket parent, InputStream in) {
            this.parent = parent;
            this.in = in;
        }

        @Override
        public int read() throws IOException {
            byte[] a = new byte[1];
            int n = this.read(a, 0, 1);
            return n > 0 ? a[0] & 0xFF : -1;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            try {
                return this.in.read(b, off, len);
            }
            catch (SocketTimeoutException e) {
                throw e;
            }
            catch (InterruptedIOException e) {
                Thread thread = Thread.currentThread();
                if (thread.isVirtual() && thread.isInterrupted()) {
                    this.close();
                    throw new SocketException("Closed by interrupt");
                }
                throw e;
            }
        }

        @Override
        public int available() throws IOException {
            return this.in.available();
        }

        @Override
        public void close() throws IOException {
            this.parent.close();
        }
    }

    private static class SocketOutputStream
    extends OutputStream {
        private final Socket parent;
        private final OutputStream out;

        SocketOutputStream(Socket parent, OutputStream out) {
            this.parent = parent;
            this.out = out;
        }

        @Override
        public void write(int b) throws IOException {
            byte[] a = new byte[]{(byte)b};
            this.write(a, 0, 1);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            try {
                this.out.write(b, off, len);
            }
            catch (InterruptedIOException e) {
                Thread thread = Thread.currentThread();
                if (thread.isVirtual() && thread.isInterrupted()) {
                    this.close();
                    throw new SocketException("Closed by interrupt");
                }
                throw e;
            }
        }

        @Override
        public void close() throws IOException {
            this.parent.close();
        }
    }
}

