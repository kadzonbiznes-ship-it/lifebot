/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ProtocolFamily;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.SocketTimeoutException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import sun.net.NetHooks;
import sun.net.ext.ExtendedSocketOptions;
import sun.nio.ch.IOStatus;
import sun.nio.ch.IOUtil;
import sun.nio.ch.NativeDispatcher;
import sun.nio.ch.NativeThread;
import sun.nio.ch.Net;
import sun.nio.ch.Poller;
import sun.nio.ch.SelChImpl;
import sun.nio.ch.SelectionKeyImpl;
import sun.nio.ch.ServerSocketAdaptor;
import sun.nio.ch.SocketChannelImpl;
import sun.nio.ch.SocketDispatcher;
import sun.nio.ch.UnixDomainSockets;

class ServerSocketChannelImpl
extends ServerSocketChannel
implements SelChImpl {
    private static final NativeDispatcher nd = new SocketDispatcher();
    private final ProtocolFamily family;
    private final FileDescriptor fd;
    private final int fdVal;
    private final ReentrantLock acceptLock = new ReentrantLock();
    private final Object stateLock = new Object();
    private static final int ST_INUSE = 0;
    private static final int ST_CLOSING = 1;
    private static final int ST_CLOSED = 2;
    private int state;
    private long thread;
    private SocketAddress localAddress;
    private boolean isReuseAddress;
    private ServerSocket socket;
    private volatile boolean forcedNonBlocking;

    ServerSocketChannelImpl(SelectorProvider sp) throws IOException {
        this(sp, Net.isIPv6Available() ? StandardProtocolFamily.INET6 : StandardProtocolFamily.INET);
    }

    ServerSocketChannelImpl(SelectorProvider sp, ProtocolFamily family) throws IOException {
        super(sp);
        Objects.requireNonNull(family, "'family' is null");
        if (family != StandardProtocolFamily.INET && family != StandardProtocolFamily.INET6 && family != StandardProtocolFamily.UNIX) {
            throw new UnsupportedOperationException("Protocol family not supported");
        }
        if (family == StandardProtocolFamily.INET6 && !Net.isIPv6Available()) {
            throw new UnsupportedOperationException("IPv6 not available");
        }
        this.family = family;
        this.fd = family == StandardProtocolFamily.UNIX ? UnixDomainSockets.socket() : Net.serverSocket(family, true);
        this.fdVal = IOUtil.fdVal(this.fd);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    ServerSocketChannelImpl(SelectorProvider sp, ProtocolFamily family, FileDescriptor fd, boolean bound) throws IOException {
        super(sp);
        this.family = family == StandardProtocolFamily.UNIX ? StandardProtocolFamily.UNIX : (Net.isIPv6Available() ? StandardProtocolFamily.INET6 : StandardProtocolFamily.INET);
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        if (bound) {
            Object object = this.stateLock;
            synchronized (object) {
                this.localAddress = family == StandardProtocolFamily.UNIX ? UnixDomainSockets.localAddress(fd) : Net.localAddress(fd);
            }
        }
    }

    private boolean isNetSocket() {
        return this.family == StandardProtocolFamily.INET || this.family == StandardProtocolFamily.INET6;
    }

    boolean isUnixSocket() {
        return this.family == StandardProtocolFamily.UNIX;
    }

    private void ensureOpen() throws ClosedChannelException {
        if (!this.isOpen()) {
            throw new ClosedChannelException();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ServerSocket socket() {
        Object object = this.stateLock;
        synchronized (object) {
            if (this.socket == null) {
                if (this.isNetSocket()) {
                    this.socket = ServerSocketAdaptor.create(this);
                } else {
                    throw new UnsupportedOperationException("Not supported");
                }
            }
            return this.socket;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public SocketAddress getLocalAddress() throws IOException {
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpen();
            if (this.isUnixSocket()) {
                return UnixDomainSockets.getRevealedLocalAddress(this.localAddress);
            }
            return Net.getRevealedLocalAddress(this.localAddress);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public <T> ServerSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
        Objects.requireNonNull(name);
        if (!this.supportedOptions().contains(name)) {
            throw new UnsupportedOperationException("'" + name + "' not supported");
        }
        if (!name.type().isInstance(value)) {
            throw new IllegalArgumentException("Invalid value '" + value + "'");
        }
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpen();
            if (this.isNetSocket() && name == StandardSocketOptions.SO_REUSEADDR && Net.useExclusiveBind()) {
                this.isReuseAddress = (Boolean)value;
            } else {
                Net.setSocketOption(this.fd, Net.UNSPEC, name, value);
            }
            return this;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        Objects.requireNonNull(name);
        if (!this.supportedOptions().contains(name)) {
            throw new UnsupportedOperationException("'" + name + "' not supported");
        }
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpen();
            if (this.isNetSocket() && name == StandardSocketOptions.SO_REUSEADDR && Net.useExclusiveBind()) {
                return (T)Boolean.valueOf(this.isReuseAddress);
            }
            return (T)Net.getSocketOption(this.fd, Net.UNSPEC, name);
        }
    }

    @Override
    public final Set<SocketOption<?>> supportedOptions() {
        if (this.isUnixSocket()) {
            return DefaultOptionsHolder.defaultUnixDomainOptions;
        }
        return DefaultOptionsHolder.defaultInetOptions;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ServerSocketChannel bind(SocketAddress local, int backlog) throws IOException {
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpen();
            if (this.localAddress != null) {
                throw new AlreadyBoundException();
            }
            this.localAddress = this.isUnixSocket() ? this.unixBind(local, backlog) : this.netBind(local, backlog);
        }
        return this;
    }

    private SocketAddress unixBind(SocketAddress local, int backlog) throws IOException {
        UnixDomainSockets.checkPermission();
        if (local == null) {
            boolean bound = false;
            for (int attempts = 0; attempts < 10 && !bound; ++attempts) {
                try {
                    Path path = UnixDomainSockets.generateTempName().getPath();
                    UnixDomainSockets.bind(this.fd, path);
                    bound = true;
                    continue;
                }
                catch (BindException bindException) {
                    // empty catch block
                }
            }
            if (!bound) {
                throw new BindException("Could not bind to temporary name");
            }
        } else {
            Path path = UnixDomainSockets.checkAddress(local).getPath();
            UnixDomainSockets.bind(this.fd, path);
        }
        Net.listen(this.fd, backlog < 1 ? 50 : backlog);
        return UnixDomainSockets.localAddress(this.fd);
    }

    private SocketAddress netBind(SocketAddress local, int backlog) throws IOException {
        InetSocketAddress isa = local == null ? new InetSocketAddress(Net.anyLocalAddress(this.family), 0) : Net.checkAddress(local, this.family);
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkListen(isa.getPort());
        }
        NetHooks.beforeTcpBind(this.fd, isa.getAddress(), isa.getPort());
        Net.bind(this.family, this.fd, isa.getAddress(), isa.getPort());
        Net.listen(this.fd, backlog < 1 ? 50 : backlog);
        return Net.localAddress(this.fd);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void begin(boolean blocking) throws ClosedChannelException {
        if (blocking) {
            this.begin();
        }
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpen();
            if (this.localAddress == null) {
                throw new NotYetBoundException();
            }
            if (blocking) {
                this.thread = NativeThread.current();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void end(boolean blocking, boolean completed) throws AsynchronousCloseException {
        if (blocking) {
            Object object = this.stateLock;
            synchronized (object) {
                this.thread = 0L;
                if (this.state == 1) {
                    this.tryFinishClose();
                }
            }
            this.end(completed);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public SocketChannel accept() throws IOException {
        int n = 0;
        FileDescriptor newfd = new FileDescriptor();
        SocketAddress[] saa = new SocketAddress[1];
        this.acceptLock.lock();
        try {
            boolean blocking = this.isBlocking();
            try {
                this.begin(blocking);
                this.configureSocketNonBlockingIfVirtualThread();
                n = this.implAccept(this.fd, newfd, saa);
                if (blocking) {
                    while (IOStatus.okayToRetry(n) && this.isOpen()) {
                        this.park(Net.POLLIN);
                        n = this.implAccept(this.fd, newfd, saa);
                    }
                }
                this.end(blocking, n > 0);
            }
            catch (Throwable throwable) {
                this.end(blocking, n > 0);
                assert (IOStatus.check(n));
                throw throwable;
            }
            assert (IOStatus.check(n));
        }
        finally {
            this.acceptLock.unlock();
        }
        if (n > 0) {
            return this.finishAccept(newfd, saa[0]);
        }
        return null;
    }

    private int implAccept(FileDescriptor fd, FileDescriptor newfd, SocketAddress[] saa) throws IOException {
        if (this.isUnixSocket()) {
            UnixDomainSockets.checkPermission();
            String[] pa = new String[1];
            int n = UnixDomainSockets.accept(fd, newfd, pa);
            if (n > 0) {
                saa[0] = UnixDomainSocketAddress.of(pa[0]);
            }
            return n;
        }
        InetSocketAddress[] issa = new InetSocketAddress[1];
        int n = Net.accept(fd, newfd, issa);
        if (n > 0) {
            saa[0] = issa[0];
        }
        return n;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    SocketChannel blockingAccept(long nanos) throws IOException {
        int n = 0;
        FileDescriptor newfd = new FileDescriptor();
        SocketAddress[] saa = new SocketAddress[1];
        this.acceptLock.lock();
        try {
            if (!this.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
            try {
                this.begin(true);
                this.lockedConfigureBlocking(false);
                try {
                    long startNanos = System.nanoTime();
                    n = this.implAccept(this.fd, newfd, saa);
                    while (n == -2 && this.isOpen()) {
                        long remainingNanos = nanos - (System.nanoTime() - startNanos);
                        if (remainingNanos <= 0L) {
                            throw new SocketTimeoutException("Accept timed out");
                        }
                        this.park(Net.POLLIN, remainingNanos);
                        n = this.implAccept(this.fd, newfd, saa);
                    }
                }
                finally {
                    this.tryLockedConfigureBlocking(true);
                }
                this.end(true, n > 0);
            }
            catch (Throwable throwable) {
                this.end(true, n > 0);
                throw throwable;
            }
        }
        finally {
            this.acceptLock.unlock();
        }
        assert (n > 0);
        return this.finishAccept(newfd, saa[0]);
    }

    private SocketChannel finishAccept(FileDescriptor newfd, SocketAddress sa) throws IOException {
        try {
            SecurityManager sm;
            IOUtil.configureBlocking(newfd, true);
            if (this.isNetSocket() && (sm = System.getSecurityManager()) != null) {
                InetSocketAddress isa = (InetSocketAddress)sa;
                sm.checkAccept(isa.getAddress().getHostAddress(), isa.getPort());
            }
            return new SocketChannelImpl(this.provider(), this.family, newfd, sa);
        }
        catch (Exception e) {
            nd.close(newfd);
            throw e;
        }
    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {
        this.acceptLock.lock();
        try {
            this.lockedConfigureBlocking(block);
        }
        finally {
            this.acceptLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void lockedConfigureBlocking(boolean block) throws IOException {
        assert (this.acceptLock.isHeldByCurrentThread());
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpen();
            if (!this.forcedNonBlocking) {
                IOUtil.configureBlocking(this.fd, block);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean tryLockedConfigureBlocking(boolean block) throws IOException {
        assert (this.acceptLock.isHeldByCurrentThread());
        Object object = this.stateLock;
        synchronized (object) {
            if (!this.forcedNonBlocking && this.isOpen()) {
                IOUtil.configureBlocking(this.fd, block);
                return true;
            }
            return false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void configureSocketNonBlockingIfVirtualThread() throws IOException {
        assert (this.acceptLock.isHeldByCurrentThread());
        if (!this.forcedNonBlocking && Thread.currentThread().isVirtual()) {
            Object object = this.stateLock;
            synchronized (object) {
                this.ensureOpen();
                IOUtil.configureBlocking(this.fd, false);
                this.forcedNonBlocking = true;
            }
        }
    }

    private boolean tryClose() throws IOException {
        assert (Thread.holdsLock(this.stateLock) && this.state == 1);
        if (this.thread == 0L && !this.isRegistered()) {
            this.state = 2;
            nd.close(this.fd);
            return true;
        }
        return false;
    }

    private void tryFinishClose() {
        try {
            this.tryClose();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void implCloseBlockingMode() throws IOException {
        Object object = this.stateLock;
        synchronized (object) {
            long th;
            assert (this.state < 1);
            this.state = 1;
            if (!this.tryClose() && (th = this.thread) != 0L) {
                if (NativeThread.isVirtualThread(th)) {
                    Poller.stopPoll(this.fdVal);
                } else {
                    nd.preClose(this.fd);
                    NativeThread.signal(th);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void implCloseNonBlockingMode() throws IOException {
        Object object = this.stateLock;
        synchronized (object) {
            assert (this.state < 1);
            this.state = 1;
        }
        this.acceptLock.lock();
        this.acceptLock.unlock();
        object = this.stateLock;
        synchronized (object) {
            if (this.state == 1) {
                this.tryClose();
            }
        }
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException {
        assert (!this.isOpen());
        if (this.isBlocking()) {
            this.implCloseBlockingMode();
        } else {
            this.implCloseNonBlockingMode();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void kill() {
        this.acceptLock.lock();
        this.acceptLock.unlock();
        Object object = this.stateLock;
        synchronized (object) {
            if (this.state == 1) {
                this.tryFinishClose();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    boolean isBound() {
        Object object = this.stateLock;
        synchronized (object) {
            return this.localAddress != null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    SocketAddress localAddress() {
        Object object = this.stateLock;
        synchronized (object) {
            return this.localAddress;
        }
    }

    public boolean translateReadyOps(int ops, int initialOps, SelectionKeyImpl ski) {
        int intOps = ski.nioInterestOps();
        int oldOps = ski.nioReadyOps();
        int newOps = initialOps;
        if ((ops & Net.POLLNVAL) != 0) {
            return false;
        }
        if ((ops & (Net.POLLERR | Net.POLLHUP)) != 0) {
            newOps = intOps;
            ski.nioReadyOps(newOps);
            return (newOps & ~oldOps) != 0;
        }
        if ((ops & Net.POLLIN) != 0 && (intOps & 0x10) != 0) {
            newOps |= 0x10;
        }
        ski.nioReadyOps(newOps);
        return (newOps & ~oldOps) != 0;
    }

    @Override
    public boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl ski) {
        return this.translateReadyOps(ops, ski.nioReadyOps(), ski);
    }

    @Override
    public boolean translateAndSetReadyOps(int ops, SelectionKeyImpl ski) {
        return this.translateReadyOps(ops, 0, ski);
    }

    @Override
    public int translateInterestOps(int ops) {
        int newOps = 0;
        if ((ops & 0x10) != 0) {
            newOps |= Net.POLLIN;
        }
        return newOps;
    }

    @Override
    public FileDescriptor getFD() {
        return this.fd;
    }

    @Override
    public int getFDVal() {
        return this.fdVal;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName());
        sb.append('[');
        if (!this.isOpen()) {
            sb.append("closed");
        } else {
            Object object = this.stateLock;
            synchronized (object) {
                SocketAddress addr = this.localAddress;
                if (addr == null) {
                    sb.append("unbound");
                } else if (this.isUnixSocket()) {
                    sb.append(UnixDomainSockets.getRevealedLocalAddressAsString(addr));
                } else {
                    sb.append(Net.getRevealedLocalAddressAsString(addr));
                }
            }
        }
        sb.append(']');
        return sb.toString();
    }

    private static class DefaultOptionsHolder {
        static final Set<SocketOption<?>> defaultInetOptions = DefaultOptionsHolder.defaultInetOptions();
        static final Set<SocketOption<?>> defaultUnixDomainOptions = DefaultOptionsHolder.defaultUnixDomainOptions();

        private DefaultOptionsHolder() {
        }

        private static Set<SocketOption<?>> defaultInetOptions() {
            HashSet set = new HashSet();
            set.add(StandardSocketOptions.SO_RCVBUF);
            set.add(StandardSocketOptions.SO_REUSEADDR);
            if (Net.isReusePortAvailable()) {
                set.add(StandardSocketOptions.SO_REUSEPORT);
            }
            set.addAll(ExtendedSocketOptions.serverSocketOptions());
            return Collections.unmodifiableSet(set);
        }

        private static Set<SocketOption<?>> defaultUnixDomainOptions() {
            HashSet<SocketOption<Integer>> set = new HashSet<SocketOption<Integer>>();
            set.add(StandardSocketOptions.SO_RCVBUF);
            return Collections.unmodifiableSet(set);
        }
    }
}

