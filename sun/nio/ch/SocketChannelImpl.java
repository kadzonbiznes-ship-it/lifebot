/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProtocolFamily;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.net.SocketTimeoutException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.NoConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
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
import sun.net.util.SocketExceptions;
import sun.nio.ch.DirectBuffer;
import sun.nio.ch.ExtendedSocketOption;
import sun.nio.ch.IOStatus;
import sun.nio.ch.IOUtil;
import sun.nio.ch.NativeDispatcher;
import sun.nio.ch.NativeThread;
import sun.nio.ch.Net;
import sun.nio.ch.Poller;
import sun.nio.ch.SelChImpl;
import sun.nio.ch.SelectionKeyImpl;
import sun.nio.ch.SocketAdaptor;
import sun.nio.ch.SocketDispatcher;
import sun.nio.ch.UnixDomainSockets;
import sun.nio.ch.Util;

class SocketChannelImpl
extends SocketChannel
implements SelChImpl {
    private static final NativeDispatcher nd = new SocketDispatcher();
    private final ProtocolFamily family;
    private final FileDescriptor fd;
    private final int fdVal;
    private final ReentrantLock readLock = new ReentrantLock();
    private final ReentrantLock writeLock = new ReentrantLock();
    private final Object stateLock = new Object();
    private volatile boolean isInputClosed;
    private volatile boolean isOutputClosed;
    private boolean connectionReset;
    private boolean isReuseAddress;
    private static final int ST_UNCONNECTED = 0;
    private static final int ST_CONNECTIONPENDING = 1;
    private static final int ST_CONNECTED = 2;
    private static final int ST_CLOSING = 3;
    private static final int ST_CLOSED = 4;
    private volatile int state;
    private long readerThread;
    private long writerThread;
    private SocketAddress localAddress;
    private SocketAddress remoteAddress;
    private Socket socket;
    private volatile boolean forcedNonBlocking;

    SocketChannelImpl(SelectorProvider sp) throws IOException {
        this(sp, Net.isIPv6Available() ? StandardProtocolFamily.INET6 : StandardProtocolFamily.INET);
    }

    SocketChannelImpl(SelectorProvider sp, ProtocolFamily family) throws IOException {
        super(sp);
        Objects.requireNonNull(family, "'family' is null");
        if (family != StandardProtocolFamily.INET && family != StandardProtocolFamily.INET6 && family != StandardProtocolFamily.UNIX) {
            throw new UnsupportedOperationException("Protocol family not supported");
        }
        if (family == StandardProtocolFamily.INET6 && !Net.isIPv6Available()) {
            throw new UnsupportedOperationException("IPv6 not available");
        }
        this.family = family;
        this.fd = family == StandardProtocolFamily.UNIX ? UnixDomainSockets.socket() : Net.socket(family, true);
        this.fdVal = IOUtil.fdVal(this.fd);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    SocketChannelImpl(SelectorProvider sp, ProtocolFamily family, FileDescriptor fd, SocketAddress remoteAddress) throws IOException {
        super(sp);
        this.family = family;
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        Object object = this.stateLock;
        synchronized (object) {
            this.localAddress = family == StandardProtocolFamily.UNIX ? UnixDomainSockets.localAddress(fd) : Net.localAddress(fd);
            this.remoteAddress = remoteAddress;
            this.state = 2;
        }
    }

    boolean isNetSocket() {
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

    private void ensureOpenAndConnected() throws ClosedChannelException {
        int state = this.state;
        if (state < 2) {
            throw new NotYetConnectedException();
        }
        if (state > 2) {
            throw new ClosedChannelException();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Socket socket() {
        Object object = this.stateLock;
        synchronized (object) {
            if (this.socket == null) {
                if (this.isNetSocket()) {
                    this.socket = SocketAdaptor.create(this);
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
    public SocketAddress getRemoteAddress() throws IOException {
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpen();
            return this.remoteAddress;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public <T> SocketChannel setOption(SocketOption<T> name, T value) throws IOException {
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
            if (this.isNetSocket()) {
                if (name == StandardSocketOptions.IP_TOS) {
                    Net.setIpSocketOption(this.fd, this.family, name, value);
                    return this;
                }
                if (name == StandardSocketOptions.SO_REUSEADDR && Net.useExclusiveBind()) {
                    this.isReuseAddress = (Boolean)value;
                    return this;
                }
            }
            Net.setSocketOption(this.fd, name, value);
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
            if (this.isNetSocket()) {
                if (name == StandardSocketOptions.IP_TOS) {
                    return (T)Net.getSocketOption(this.fd, this.family, name);
                }
                if (name == StandardSocketOptions.SO_REUSEADDR && Net.useExclusiveBind()) {
                    return (T)Boolean.valueOf(this.isReuseAddress);
                }
            }
            return (T)Net.getSocketOption(this.fd, name);
        }
    }

    @Override
    public final Set<SocketOption<?>> supportedOptions() {
        if (this.isUnixSocket()) {
            return DefaultOptionsHolder.defaultUnixOptions;
        }
        return DefaultOptionsHolder.defaultInetOptions;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void beginRead(boolean blocking) throws ClosedChannelException {
        if (blocking) {
            this.begin();
            Object object = this.stateLock;
            synchronized (object) {
                this.ensureOpen();
                this.readerThread = NativeThread.current();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void endRead(boolean blocking, boolean completed) throws AsynchronousCloseException {
        if (blocking) {
            Object object = this.stateLock;
            synchronized (object) {
                this.readerThread = 0L;
                if (this.state == 3) {
                    this.tryFinishClose();
                }
            }
            this.end(completed);
        }
    }

    private void throwConnectionReset() throws SocketException {
        throw new SocketException("Connection reset");
    }

    /*
     * Exception decompiling
     */
    @Override
    public int read(ByteBuffer buf) throws IOException {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 3 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    /*
     * Exception decompiling
     */
    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 3 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void beginWrite(boolean blocking) throws ClosedChannelException {
        if (blocking) {
            this.begin();
            Object object = this.stateLock;
            synchronized (object) {
                this.ensureOpen();
                if (this.isOutputClosed) {
                    throw new ClosedChannelException();
                }
                this.writerThread = NativeThread.current();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void endWrite(boolean blocking, boolean completed) throws AsynchronousCloseException {
        if (blocking) {
            Object object = this.stateLock;
            synchronized (object) {
                this.writerThread = 0L;
                if (this.state == 3) {
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
    public int write(ByteBuffer buf) throws IOException {
        Objects.requireNonNull(buf);
        this.writeLock.lock();
        try {
            this.ensureOpenAndConnected();
            boolean blocking = this.isBlocking();
            int n = 0;
            try {
                this.beginWrite(blocking);
                this.configureSocketNonBlockingIfVirtualThread();
                n = IOUtil.write(this.fd, buf, -1L, nd);
                if (blocking) {
                    while (IOStatus.okayToRetry(n) && this.isOpen()) {
                        this.park(Net.POLLOUT);
                        n = IOUtil.write(this.fd, buf, -1L, nd);
                    }
                }
                this.endWrite(blocking, n > 0);
            }
            catch (Throwable throwable) {
                this.endWrite(blocking, n > 0);
                if (n <= 0 && this.isOutputClosed) {
                    throw new AsynchronousCloseException();
                }
                throw throwable;
            }
            if (n <= 0 && this.isOutputClosed) {
                throw new AsynchronousCloseException();
            }
            int n2 = IOStatus.normalize(n);
            return n2;
        }
        finally {
            this.writeLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, srcs.length);
        this.writeLock.lock();
        try {
            this.ensureOpenAndConnected();
            boolean blocking = this.isBlocking();
            long n = 0L;
            try {
                this.beginWrite(blocking);
                this.configureSocketNonBlockingIfVirtualThread();
                n = IOUtil.write(this.fd, srcs, offset, length, nd);
                if (blocking) {
                    while (IOStatus.okayToRetry(n) && this.isOpen()) {
                        this.park(Net.POLLOUT);
                        n = IOUtil.write(this.fd, srcs, offset, length, nd);
                    }
                }
                this.endWrite(blocking, n > 0L);
            }
            catch (Throwable throwable) {
                this.endWrite(blocking, n > 0L);
                if (n <= 0L && this.isOutputClosed) {
                    throw new AsynchronousCloseException();
                }
                throw throwable;
            }
            if (n <= 0L && this.isOutputClosed) {
                throw new AsynchronousCloseException();
            }
            long l = IOStatus.normalize(n);
            return l;
        }
        finally {
            this.writeLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    int sendOutOfBandData(byte b) throws IOException {
        this.writeLock.lock();
        try {
            this.ensureOpenAndConnected();
            boolean blocking = this.isBlocking();
            int n = 0;
            try {
                this.beginWrite(blocking);
                this.configureSocketNonBlockingIfVirtualThread();
                while ((n = Net.sendOOB(this.fd, b)) == -3 && this.isOpen()) {
                }
                if (blocking && n == -2) {
                    throw new SocketException("No buffer space available");
                }
                this.endWrite(blocking, n > 0);
            }
            catch (Throwable throwable) {
                this.endWrite(blocking, n > 0);
                if (n <= 0 && this.isOutputClosed) {
                    throw new AsynchronousCloseException();
                }
                throw throwable;
            }
            if (n <= 0 && this.isOutputClosed) {
                throw new AsynchronousCloseException();
            }
            int n2 = IOStatus.normalize(n);
            return n2;
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {
        this.readLock.lock();
        try {
            this.writeLock.lock();
            try {
                this.lockedConfigureBlocking(block);
            }
            finally {
                this.writeLock.unlock();
            }
        }
        finally {
            this.readLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void lockedConfigureBlocking(boolean block) throws IOException {
        assert (this.readLock.isHeldByCurrentThread() || this.writeLock.isHeldByCurrentThread());
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
        assert (this.readLock.isHeldByCurrentThread() || this.writeLock.isHeldByCurrentThread());
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
        assert (this.readLock.isHeldByCurrentThread() || this.writeLock.isHeldByCurrentThread());
        if (!this.forcedNonBlocking && Thread.currentThread().isVirtual()) {
            Object object = this.stateLock;
            synchronized (object) {
                this.ensureOpen();
                IOUtil.configureBlocking(this.fd, false);
                this.forcedNonBlocking = true;
            }
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    SocketAddress remoteAddress() {
        Object object = this.stateLock;
        synchronized (object) {
            return this.remoteAddress;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public SocketChannel bind(SocketAddress local) throws IOException {
        this.readLock.lock();
        try {
            this.writeLock.lock();
            try {
                Object object = this.stateLock;
                synchronized (object) {
                    this.ensureOpen();
                    if (this.state == 1) {
                        throw new ConnectionPendingException();
                    }
                    if (this.localAddress != null) {
                        throw new AlreadyBoundException();
                    }
                    this.localAddress = this.isUnixSocket() ? this.unixBind(local) : this.netBind(local);
                }
            }
            finally {
                this.writeLock.unlock();
            }
        }
        finally {
            this.readLock.unlock();
        }
        return this;
    }

    private SocketAddress unixBind(SocketAddress local) throws IOException {
        UnixDomainSockets.checkPermission();
        if (local == null) {
            return UnixDomainSockets.unnamed();
        }
        Path path = UnixDomainSockets.checkAddress(local).getPath();
        if (path.toString().isEmpty()) {
            return UnixDomainSockets.unnamed();
        }
        UnixDomainSockets.bind(this.fd, path);
        return UnixDomainSockets.localAddress(this.fd);
    }

    private SocketAddress netBind(SocketAddress local) throws IOException {
        InetSocketAddress isa = local == null ? new InetSocketAddress(Net.anyLocalAddress(this.family), 0) : Net.checkAddress(local, this.family);
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkListen(isa.getPort());
        }
        NetHooks.beforeTcpBind(this.fd, isa.getAddress(), isa.getPort());
        Net.bind(this.family, this.fd, isa.getAddress(), isa.getPort());
        return Net.localAddress(this.fd);
    }

    @Override
    public boolean isConnected() {
        return this.state == 2;
    }

    @Override
    public boolean isConnectionPending() {
        return this.state == 1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void beginConnect(boolean blocking, SocketAddress sa) throws IOException {
        if (blocking) {
            this.begin();
        }
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpen();
            int state = this.state;
            if (state == 2) {
                throw new AlreadyConnectedException();
            }
            if (state == 1) {
                throw new ConnectionPendingException();
            }
            assert (state == 0);
            this.state = 1;
            if (this.isNetSocket() && this.localAddress == null) {
                InetSocketAddress isa = (InetSocketAddress)sa;
                NetHooks.beforeTcpConnect(this.fd, isa.getAddress(), isa.getPort());
            }
            this.remoteAddress = sa;
            if (blocking) {
                this.readerThread = NativeThread.current();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void endConnect(boolean blocking, boolean completed) throws IOException {
        this.endRead(blocking, completed);
        if (completed) {
            Object object = this.stateLock;
            synchronized (object) {
                if (this.state == 1) {
                    this.localAddress = this.isUnixSocket() ? UnixDomainSockets.localAddress(this.fd) : Net.localAddress(this.fd);
                    this.state = 2;
                }
            }
        }
    }

    private SocketAddress checkRemote(SocketAddress sa) {
        InetAddress address;
        if (this.isUnixSocket()) {
            UnixDomainSockets.checkPermission();
            return UnixDomainSockets.checkAddress(sa);
        }
        InetSocketAddress isa = Net.checkAddress(sa, this.family);
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkConnect(isa.getAddress().getHostAddress(), isa.getPort());
        }
        if ((address = isa.getAddress()).isAnyLocalAddress()) {
            int port = isa.getPort();
            if (address instanceof Inet4Address) {
                return new InetSocketAddress(Net.inet4LoopbackAddress(), port);
            }
            assert (this.family == StandardProtocolFamily.INET6);
            return new InetSocketAddress(Net.inet6LoopbackAddress(), port);
        }
        return isa;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive exception aggregation
     */
    @Override
    public boolean connect(SocketAddress remote) throws IOException {
        SocketAddress sa = this.checkRemote(remote);
        try {
            this.readLock.lock();
            try {
                this.writeLock.lock();
                try {
                    boolean blocking = this.isBlocking();
                    boolean connected = false;
                    try {
                        this.beginConnect(blocking, sa);
                        this.configureSocketNonBlockingIfVirtualThread();
                        int n = this.isUnixSocket() ? UnixDomainSockets.connect(this.fd, sa) : Net.connect(this.family, this.fd, sa);
                        if (n > 0) {
                            connected = true;
                        } else if (blocking) {
                            assert (IOStatus.okayToRetry(n));
                            boolean polled = false;
                            while (!polled && this.isOpen()) {
                                this.park(Net.POLLOUT);
                                polled = Net.pollConnectNow(this.fd);
                            }
                            connected = polled && this.isOpen();
                        }
                    }
                    finally {
                        this.endConnect(blocking, connected);
                    }
                    boolean bl = connected;
                    this.writeLock.unlock();
                    return bl;
                }
                catch (Throwable throwable) {
                    this.writeLock.unlock();
                    throw throwable;
                }
            }
            finally {
                this.readLock.unlock();
            }
        }
        catch (IOException ioe) {
            this.close();
            throw SocketExceptions.of(ioe, sa);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void beginFinishConnect(boolean blocking) throws ClosedChannelException {
        if (blocking) {
            this.begin();
        }
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpen();
            if (this.state != 1) {
                throw new NoConnectionPendingException();
            }
            if (blocking) {
                this.readerThread = NativeThread.current();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void endFinishConnect(boolean blocking, boolean completed) throws IOException {
        this.endRead(blocking, completed);
        if (completed) {
            Object object = this.stateLock;
            synchronized (object) {
                if (this.state == 1) {
                    this.localAddress = this.isUnixSocket() ? UnixDomainSockets.localAddress(this.fd) : Net.localAddress(this.fd);
                    this.state = 2;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive exception aggregation
     */
    @Override
    public boolean finishConnect() throws IOException {
        try {
            this.readLock.lock();
            try {
                block16: {
                    this.writeLock.lock();
                    if (!this.isConnected()) break block16;
                    boolean bl = true;
                    this.writeLock.unlock();
                    return bl;
                }
                try {
                    boolean blocking = this.isBlocking();
                    boolean connected = false;
                    try {
                        this.beginFinishConnect(blocking);
                        boolean polled = Net.pollConnectNow(this.fd);
                        if (blocking) {
                            while (!polled && this.isOpen()) {
                                this.park(Net.POLLOUT);
                                polled = Net.pollConnectNow(this.fd);
                            }
                        }
                        connected = polled && this.isOpen();
                    }
                    finally {
                        this.endFinishConnect(blocking, connected);
                    }
                    assert ((blocking && connected) ^ !blocking);
                    boolean bl = connected;
                    this.writeLock.unlock();
                    return bl;
                }
                catch (Throwable throwable) {
                    this.writeLock.unlock();
                    throw throwable;
                }
            }
            finally {
                this.readLock.unlock();
            }
        }
        catch (IOException ioe) {
            this.close();
            throw SocketExceptions.of(ioe, this.remoteAddress);
        }
    }

    private boolean tryClose() throws IOException {
        assert (Thread.holdsLock(this.stateLock) && this.state == 3);
        if (this.readerThread == 0L && this.writerThread == 0L && !this.isRegistered()) {
            this.state = 4;
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
            assert (this.state < 3);
            boolean connected = this.state == 2;
            this.state = 3;
            if (connected && Net.shouldShutdownWriteBeforeClose()) {
                try {
                    SocketOption<Integer> SO_LINGER = StandardSocketOptions.SO_LINGER;
                    if ((Integer)Net.getSocketOption(this.fd, SO_LINGER) != 0) {
                        Net.shutdown(this.fd, 1);
                    }
                }
                catch (IOException SO_LINGER) {
                    // empty catch block
                }
            }
            if (!this.tryClose()) {
                long reader = this.readerThread;
                long writer = this.writerThread;
                if (NativeThread.isVirtualThread(reader) || NativeThread.isVirtualThread(writer)) {
                    Poller.stopPoll(this.fdVal);
                }
                if (NativeThread.isNativeThread(reader) || NativeThread.isNativeThread(writer)) {
                    nd.preClose(this.fd);
                    if (NativeThread.isNativeThread(reader)) {
                        NativeThread.signal(reader);
                    }
                    if (NativeThread.isNativeThread(writer)) {
                        NativeThread.signal(writer);
                    }
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void implCloseNonBlockingMode() throws IOException {
        boolean connected;
        Object object = this.stateLock;
        synchronized (object) {
            assert (this.state < 3);
            connected = this.state == 2;
            this.state = 3;
        }
        this.readLock.lock();
        this.readLock.unlock();
        this.writeLock.lock();
        this.writeLock.unlock();
        object = this.stateLock;
        synchronized (object) {
            if (this.state == 3 && !this.tryClose() && connected && this.isRegistered()) {
                try {
                    SocketOption<Integer> opt = StandardSocketOptions.SO_LINGER;
                    int interval = (Integer)Net.getSocketOption(this.fd, Net.UNSPEC, opt);
                    if (interval != 0) {
                        if (interval > 0) {
                            Net.setSocketOption(this.fd, Net.UNSPEC, opt, -1);
                        }
                        Net.shutdown(this.fd, 1);
                    }
                }
                catch (IOException iOException) {
                    // empty catch block
                }
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
        this.readLock.lock();
        this.readLock.unlock();
        this.writeLock.lock();
        this.writeLock.unlock();
        Object object = this.stateLock;
        synchronized (object) {
            if (this.state == 3) {
                this.tryFinishClose();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public SocketChannel shutdownInput() throws IOException {
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpen();
            if (!this.isConnected()) {
                throw new NotYetConnectedException();
            }
            if (!this.isInputClosed) {
                Net.shutdown(this.fd, 0);
                long reader = this.readerThread;
                if (NativeThread.isVirtualThread(reader)) {
                    Poller.stopPoll(this.fdVal, Net.POLLIN);
                } else if (NativeThread.isNativeThread(reader)) {
                    NativeThread.signal(reader);
                }
                this.isInputClosed = true;
            }
            return this;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public SocketChannel shutdownOutput() throws IOException {
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpen();
            if (!this.isConnected()) {
                throw new NotYetConnectedException();
            }
            if (!this.isOutputClosed) {
                Net.shutdown(this.fd, 1);
                long writer = this.writerThread;
                if (NativeThread.isVirtualThread(writer)) {
                    Poller.stopPoll(this.fdVal, Net.POLLOUT);
                } else if (NativeThread.isNativeThread(writer)) {
                    NativeThread.signal(writer);
                }
                this.isOutputClosed = true;
            }
            return this;
        }
    }

    boolean isInputOpen() {
        return !this.isInputClosed;
    }

    boolean isOutputOpen() {
        return !this.isOutputClosed;
    }

    private boolean finishTimedConnect(long nanos) throws IOException {
        long startNanos = System.nanoTime();
        boolean polled = Net.pollConnectNow(this.fd);
        while (!polled && this.isOpen()) {
            long remainingNanos = nanos - (System.nanoTime() - startNanos);
            if (remainingNanos <= 0L) {
                throw new SocketTimeoutException("Connect timed out");
            }
            this.park(Net.POLLOUT, remainingNanos);
            polled = Net.pollConnectNow(this.fd);
        }
        return polled && this.isOpen();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void blockingConnect(SocketAddress remote, long nanos) throws IOException {
        SocketAddress sa = this.checkRemote(remote);
        try {
            this.readLock.lock();
            try {
                this.writeLock.lock();
                try {
                    if (!this.isBlocking()) {
                        throw new IllegalBlockingModeException();
                    }
                    boolean connected = false;
                    try {
                        this.beginConnect(true, sa);
                        this.lockedConfigureBlocking(false);
                        try {
                            int n = this.isUnixSocket() ? UnixDomainSockets.connect(this.fd, sa) : Net.connect(this.family, this.fd, sa);
                            connected = n > 0 ? true : this.finishTimedConnect(nanos);
                        }
                        finally {
                            this.tryLockedConfigureBlocking(true);
                        }
                    }
                    finally {
                        this.endConnect(true, connected);
                    }
                }
                finally {
                    this.writeLock.unlock();
                }
            }
            finally {
                this.readLock.unlock();
            }
        }
        catch (IOException ioe) {
            this.close();
            throw SocketExceptions.of(ioe, sa);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int tryRead(byte[] b, int off, int len) throws IOException {
        ByteBuffer dst = Util.getTemporaryDirectBuffer(len);
        assert (dst.position() == 0);
        try {
            int n = nd.read(this.fd, ((DirectBuffer)((Object)dst)).address(), len);
            if (n > 0) {
                dst.get(b, off, n);
            }
            int n2 = n;
            return n2;
        }
        finally {
            Util.offerFirstTemporaryDirectBuffer(dst);
        }
    }

    private int timedRead(byte[] b, int off, int len, long nanos) throws IOException {
        long startNanos = System.nanoTime();
        int n = this.tryRead(b, off, len);
        while (n == -2 && this.isOpen()) {
            long remainingNanos = nanos - (System.nanoTime() - startNanos);
            if (remainingNanos <= 0L) {
                throw new SocketTimeoutException("Read timed out");
            }
            this.park(Net.POLLIN, remainingNanos);
            n = this.tryRead(b, off, len);
        }
        return n;
    }

    /*
     * Exception decompiling
     */
    int blockingRead(byte[] b, int off, int len, long nanos) throws IOException {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 3 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int tryWrite(byte[] b, int off, int len) throws IOException {
        ByteBuffer src = Util.getTemporaryDirectBuffer(len);
        assert (src.position() == 0);
        try {
            src.put(b, off, len);
            int n = nd.write(this.fd, ((DirectBuffer)((Object)src)).address(), len);
            return n;
        }
        finally {
            Util.offerFirstTemporaryDirectBuffer(src);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void blockingWriteFully(byte[] b, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, b.length);
        if (len == 0) {
            return;
        }
        this.writeLock.lock();
        try {
            this.ensureOpenAndConnected();
            if (!this.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
            int pos = off;
            int end = off + len;
            try {
                this.beginWrite(true);
                this.configureSocketNonBlockingIfVirtualThread();
                while (pos < end && this.isOpen()) {
                    int size = end - pos;
                    int n = this.tryWrite(b, pos, size);
                    while (IOStatus.okayToRetry(n) && this.isOpen()) {
                        this.park(Net.POLLOUT);
                        n = this.tryWrite(b, pos, size);
                    }
                    if (n <= 0) continue;
                    pos += n;
                }
                this.endWrite(true, pos >= end);
            }
            catch (Throwable throwable) {
                this.endWrite(true, pos >= end);
                throw throwable;
            }
        }
        finally {
            this.writeLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    int available() throws IOException {
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpenAndConnected();
            if (this.isInputClosed) {
                return 0;
            }
            return Net.available(this.fd);
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
        boolean connected = this.isConnected();
        if ((ops & Net.POLLIN) != 0 && (intOps & 1) != 0 && connected) {
            newOps |= 1;
        }
        if ((ops & Net.POLLCONN) != 0 && (intOps & 8) != 0 && this.isConnectionPending()) {
            newOps |= 8;
        }
        if ((ops & Net.POLLOUT) != 0 && (intOps & 4) != 0 && connected) {
            newOps |= 4;
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
        if ((ops & 1) != 0) {
            newOps |= Net.POLLIN;
        }
        if ((ops & 4) != 0) {
            newOps |= Net.POLLOUT;
        }
        if ((ops & 8) != 0) {
            newOps |= Net.POLLCONN;
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
        sb.append(this.getClass().getSuperclass().getName());
        sb.append('[');
        if (!this.isOpen()) {
            sb.append("closed");
        } else {
            Object object = this.stateLock;
            synchronized (object) {
                switch (this.state) {
                    case 0: {
                        sb.append("unconnected");
                        break;
                    }
                    case 1: {
                        sb.append("connection-pending");
                        break;
                    }
                    case 2: {
                        sb.append("connected");
                        if (this.isInputClosed) {
                            sb.append(" ishut");
                        }
                        if (!this.isOutputClosed) break;
                        sb.append(" oshut");
                    }
                }
                SocketAddress addr = this.localAddress();
                if (addr != null) {
                    sb.append(" local=");
                    if (this.isUnixSocket()) {
                        sb.append(UnixDomainSockets.getRevealedLocalAddressAsString(addr));
                    } else {
                        sb.append(Net.getRevealedLocalAddressAsString(addr));
                    }
                }
                if (this.remoteAddress() != null) {
                    sb.append(" remote=");
                    sb.append(this.remoteAddress().toString());
                }
            }
        }
        sb.append(']');
        return sb.toString();
    }

    private static class DefaultOptionsHolder {
        static final Set<SocketOption<?>> defaultInetOptions = DefaultOptionsHolder.defaultInetOptions();
        static final Set<SocketOption<?>> defaultUnixOptions = DefaultOptionsHolder.defaultUnixOptions();

        private DefaultOptionsHolder() {
        }

        private static Set<SocketOption<?>> defaultInetOptions() {
            HashSet set = new HashSet();
            set.add(StandardSocketOptions.SO_SNDBUF);
            set.add(StandardSocketOptions.SO_RCVBUF);
            set.add(StandardSocketOptions.SO_KEEPALIVE);
            set.add(StandardSocketOptions.SO_REUSEADDR);
            if (Net.isReusePortAvailable()) {
                set.add(StandardSocketOptions.SO_REUSEPORT);
            }
            set.add(StandardSocketOptions.SO_LINGER);
            set.add(StandardSocketOptions.TCP_NODELAY);
            set.add(StandardSocketOptions.IP_TOS);
            set.add(ExtendedSocketOption.SO_OOBINLINE);
            set.addAll(ExtendedSocketOptions.clientSocketOptions());
            return Collections.unmodifiableSet(set);
        }

        private static Set<SocketOption<?>> defaultUnixOptions() {
            HashSet set = new HashSet();
            set.add(StandardSocketOptions.SO_SNDBUF);
            set.add(StandardSocketOptions.SO_RCVBUF);
            set.add(StandardSocketOptions.SO_LINGER);
            set.addAll(ExtendedSocketOptions.unixDomainSocketOptions());
            return Collections.unmodifiableSet(set);
        }
    }
}

