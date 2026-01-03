/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.ref.Cleaner;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProtocolFamily;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketOption;
import java.net.SocketTimeoutException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import jdk.internal.access.JavaIOFileDescriptorAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.ref.CleanerFactory;
import sun.net.ConnectionResetException;
import sun.net.NetHooks;
import sun.net.PlatformSocketImpl;
import sun.net.ResourceManager;
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
import sun.nio.ch.SocketDispatcher;
import sun.nio.ch.Util;

public final class NioSocketImpl
extends SocketImpl
implements PlatformSocketImpl {
    private static final NativeDispatcher nd = new SocketDispatcher();
    private static final int MAX_BUFFER_SIZE = 131072;
    private final boolean server;
    private final ReentrantLock readLock = new ReentrantLock();
    private final ReentrantLock writeLock = new ReentrantLock();
    private final Object stateLock = new Object();
    private static final int ST_NEW = 0;
    private static final int ST_UNCONNECTED = 1;
    private static final int ST_CONNECTING = 2;
    private static final int ST_CONNECTED = 3;
    private static final int ST_CLOSING = 4;
    private static final int ST_CLOSED = 5;
    private volatile int state;
    private boolean stream;
    private Cleaner.Cleanable cleaner;
    private volatile boolean nonBlocking;
    private long readerThread;
    private long writerThread;
    private boolean isReuseAddress;
    private volatile int timeout;
    private volatile boolean isInputClosed;
    private volatile boolean isOutputClosed;
    private boolean readEOF;
    private boolean connectionReset;
    private static volatile Set<SocketOption<?>> clientSocketOptions;
    private static volatile Set<SocketOption<?>> serverSocketOptions;
    private static final JavaIOFileDescriptorAccess JIOFDA;

    public NioSocketImpl(boolean server) {
        this.server = server;
    }

    private boolean isOpen() {
        return this.state < 4;
    }

    private void ensureOpen() throws SocketException {
        int state = this.state;
        if (state == 0) {
            throw new SocketException("Socket not created");
        }
        if (state >= 4) {
            throw new SocketException("Socket closed");
        }
    }

    private void ensureOpenAndConnected() throws SocketException {
        int state = this.state;
        if (state < 3) {
            throw new SocketException("Not connected");
        }
        if (state > 3) {
            throw new SocketException("Socket closed");
        }
    }

    private void park(FileDescriptor fd, int event, long nanos) throws IOException {
        Thread t = Thread.currentThread();
        if (t.isVirtual()) {
            Poller.poll(NioSocketImpl.fdVal(fd), event, nanos, this::isOpen);
            if (t.isInterrupted()) {
                throw new InterruptedIOException();
            }
        } else {
            long millis;
            if (nanos == 0L) {
                millis = -1L;
            } else {
                millis = TimeUnit.NANOSECONDS.toMillis(nanos);
                if (nanos > TimeUnit.MILLISECONDS.toNanos(millis)) {
                    ++millis;
                }
            }
            Net.poll(fd, event, millis);
        }
    }

    private void park(FileDescriptor fd, int event) throws IOException {
        this.park(fd, event, 0L);
    }

    private void configureNonBlockingIfNeeded(FileDescriptor fd, boolean timed) throws IOException {
        if (!this.nonBlocking && (timed || Thread.currentThread().isVirtual())) {
            assert (this.readLock.isHeldByCurrentThread() || this.writeLock.isHeldByCurrentThread());
            IOUtil.configureBlocking(fd, false);
            this.nonBlocking = true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private FileDescriptor beginRead() throws SocketException {
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpenAndConnected();
            this.readerThread = NativeThread.current();
            return this.fd;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void endRead(boolean completed) throws SocketException {
        Object object = this.stateLock;
        synchronized (object) {
            this.readerThread = 0L;
            int state = this.state;
            if (state == 4) {
                this.tryFinishClose();
            }
            if (!completed && state >= 4) {
                throw new SocketException("Socket closed");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int tryRead(FileDescriptor fd, byte[] b, int off, int len) throws IOException {
        ByteBuffer dst = Util.getTemporaryDirectBuffer(len);
        assert (dst.position() == 0);
        try {
            int n = nd.read(fd, ((DirectBuffer)((Object)dst)).address(), len);
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

    private int timedRead(FileDescriptor fd, byte[] b, int off, int len, long nanos) throws IOException {
        long startNanos = System.nanoTime();
        int n = this.tryRead(fd, b, off, len);
        while (n == -2 && this.isOpen()) {
            long remainingNanos = nanos - (System.nanoTime() - startNanos);
            if (remainingNanos <= 0L) {
                throw new SocketTimeoutException("Read timed out");
            }
            this.park(fd, Net.POLLIN, remainingNanos);
            n = this.tryRead(fd, b, off, len);
        }
        return n;
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private int implRead(byte[] b, int off, int len) throws IOException {
        int n;
        FileDescriptor fd;
        int n2;
        block11: {
            n2 = 0;
            fd = this.beginRead();
            if (this.connectionReset) {
                throw new SocketException("Connection reset");
            }
            if (!this.isInputClosed) break block11;
            int n3 = -1;
            this.endRead(n2 > 0);
            return n3;
        }
        try {
            int timeout = this.timeout;
            this.configureNonBlockingIfNeeded(fd, timeout > 0);
            if (timeout > 0) {
                n2 = this.timedRead(fd, b, off, len, TimeUnit.MILLISECONDS.toNanos(timeout));
            } else {
                n2 = this.tryRead(fd, b, off, len);
                while (IOStatus.okayToRetry(n2) && this.isOpen()) {
                    this.park(fd, Net.POLLIN);
                    n2 = this.tryRead(fd, b, off, len);
                }
            }
            n = n2;
            this.endRead(n2 > 0);
        }
        catch (InterruptedIOException e) {
            try {
                throw e;
                catch (ConnectionResetException e2) {
                    this.connectionReset = true;
                    throw new SocketException("Connection reset");
                }
                catch (IOException ioe) {
                    throw NioSocketImpl.asSocketException(ioe);
                }
            }
            catch (Throwable throwable) {
                this.endRead(n2 > 0);
                throw throwable;
            }
        }
        return n;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int read(byte[] b, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, b.length);
        if (len == 0) {
            return 0;
        }
        this.readLock.lock();
        try {
            if (this.readEOF) {
                int n = -1;
                return n;
            }
            int size = Math.min(len, 131072);
            int n = this.implRead(b, off, size);
            if (n == -1) {
                this.readEOF = true;
            }
            int n2 = n;
            return n2;
        }
        finally {
            this.readLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private FileDescriptor beginWrite() throws SocketException {
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpenAndConnected();
            this.writerThread = NativeThread.current();
            return this.fd;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void endWrite(boolean completed) throws SocketException {
        Object object = this.stateLock;
        synchronized (object) {
            this.writerThread = 0L;
            int state = this.state;
            if (state == 4) {
                this.tryFinishClose();
            }
            if (!completed && state >= 4) {
                throw new SocketException("Socket closed");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int tryWrite(FileDescriptor fd, byte[] b, int off, int len) throws IOException {
        ByteBuffer src = Util.getTemporaryDirectBuffer(len);
        assert (src.position() == 0);
        try {
            src.put(b, off, len);
            int n = nd.write(fd, ((DirectBuffer)((Object)src)).address(), len);
            return n;
        }
        finally {
            Util.offerFirstTemporaryDirectBuffer(src);
        }
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private int implWrite(byte[] b, int off, int len) throws IOException {
        boolean bl;
        int n;
        int n2 = 0;
        FileDescriptor fd = this.beginWrite();
        try {
            this.configureNonBlockingIfNeeded(fd, false);
            n2 = this.tryWrite(fd, b, off, len);
            while (IOStatus.okayToRetry(n2) && this.isOpen()) {
                this.park(fd, Net.POLLOUT);
                n2 = this.tryWrite(fd, b, off, len);
            }
            n = n2;
            bl = n2 > 0;
        }
        catch (InterruptedIOException e) {
            try {
                throw e;
                catch (IOException ioe) {
                    throw NioSocketImpl.asSocketException(ioe);
                }
            }
            catch (Throwable throwable) {
                this.endWrite(n2 > 0);
                throw throwable;
            }
        }
        this.endWrite(bl);
        return n;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void write(byte[] b, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, b.length);
        if (len > 0) {
            this.writeLock.lock();
            try {
                int n;
                int end = off + len;
                for (int pos = off; pos < end; pos += n) {
                    int size = Math.min(end - pos, 131072);
                    n = this.implWrite(b, pos, size);
                }
            }
            finally {
                this.writeLock.unlock();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void create(boolean stream) throws IOException {
        Object object = this.stateLock;
        synchronized (object) {
            FileDescriptor fd;
            if (this.state != 0) {
                throw new IOException("Already created");
            }
            if (!stream) {
                ResourceManager.beforeUdpCreate();
            }
            try {
                if (this.server) {
                    assert (stream);
                    fd = Net.serverSocket(true);
                } else {
                    fd = Net.socket(stream);
                }
            }
            catch (IOException ioe) {
                if (!stream) {
                    ResourceManager.afterUdpClose();
                }
                throw ioe;
            }
            Runnable closer = NioSocketImpl.closerFor(fd, stream);
            this.fd = fd;
            this.stream = stream;
            this.cleaner = CleanerFactory.cleaner().register(this, closer);
            this.state = 1;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private FileDescriptor beginConnect(InetAddress address, int port) throws IOException {
        Object object = this.stateLock;
        synchronized (object) {
            int state = this.state;
            if (state != 1) {
                if (state == 0) {
                    throw new SocketException("Not created");
                }
                if (state == 2) {
                    throw new SocketException("Connection in progress");
                }
                if (state == 3) {
                    throw new SocketException("Already connected");
                }
                if (state >= 4) {
                    throw new SocketException("Socket closed");
                }
                assert (false);
            }
            this.state = 2;
            if (this.localport == 0) {
                NetHooks.beforeTcpConnect(this.fd, address, port);
            }
            this.address = address;
            this.port = port;
            this.readerThread = NativeThread.current();
            return this.fd;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void endConnect(FileDescriptor fd, boolean completed) throws IOException {
        Object object = this.stateLock;
        synchronized (object) {
            this.readerThread = 0L;
            int state = this.state;
            if (state == 4) {
                this.tryFinishClose();
            }
            if (completed && state == 2) {
                this.state = 3;
                this.localport = Net.localAddress(fd).getPort();
            } else if (!completed && state >= 4) {
                throw new SocketException("Socket closed");
            }
        }
    }

    private boolean timedFinishConnect(FileDescriptor fd, long nanos) throws IOException {
        long startNanos = System.nanoTime();
        boolean polled = Net.pollConnectNow(fd);
        while (!polled && this.isOpen()) {
            long remainingNanos = nanos - (System.nanoTime() - startNanos);
            if (remainingNanos <= 0L) {
                throw new SocketTimeoutException("Connect timed out");
            }
            this.park(fd, Net.POLLOUT, remainingNanos);
            polled = Net.pollConnectNow(fd);
        }
        return polled && this.isOpen();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void connect(SocketAddress remote, int millis) throws IOException {
        if (!(remote instanceof InetSocketAddress)) {
            throw new IOException("Unsupported address type");
        }
        InetSocketAddress isa = (InetSocketAddress)remote;
        if (isa.isUnresolved()) {
            throw new UnknownHostException(isa.getHostName());
        }
        InetAddress address = isa.getAddress();
        if (address.isAnyLocalAddress()) {
            address = InetAddress.getLocalHost();
        }
        int port = isa.getPort();
        ReentrantLock connectLock = this.readLock;
        try {
            connectLock.lock();
            try {
                boolean connected = false;
                FileDescriptor fd = this.beginConnect(address, port);
                try {
                    this.configureNonBlockingIfNeeded(fd, millis > 0);
                    int n = Net.connect(fd, address, port);
                    if (n > 0) {
                        connected = true;
                    } else {
                        assert (IOStatus.okayToRetry(n));
                        if (millis > 0) {
                            long nanos = TimeUnit.MILLISECONDS.toNanos(millis);
                            connected = this.timedFinishConnect(fd, nanos);
                        } else {
                            boolean polled = false;
                            while (!polled && this.isOpen()) {
                                this.park(fd, Net.POLLOUT);
                                polled = Net.pollConnectNow(fd);
                            }
                            connected = polled && this.isOpen();
                        }
                    }
                }
                finally {
                    this.endConnect(fd, connected);
                }
            }
            finally {
                connectLock.unlock();
            }
        }
        catch (IOException ioe) {
            this.close();
            if (ioe instanceof InterruptedIOException) {
                throw ioe;
            }
            throw SocketExceptions.of(ioe, isa);
        }
    }

    @Override
    protected void connect(String host, int port) throws IOException {
        this.connect(new InetSocketAddress(host, port), 0);
    }

    @Override
    protected void connect(InetAddress address, int port) throws IOException {
        this.connect(new InetSocketAddress(address, port), 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void bind(InetAddress host, int port) throws IOException {
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpen();
            if (this.localport != 0) {
                throw new SocketException("Already bound");
            }
            NetHooks.beforeTcpBind(this.fd, host, port);
            Net.bind(this.fd, host, port);
            this.address = host;
            this.localport = Net.localAddress(this.fd).getPort();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void listen(int backlog) throws IOException {
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpen();
            if (this.localport == 0) {
                throw new SocketException("Not bound");
            }
            Net.listen(this.fd, backlog < 1 ? 50 : backlog);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private FileDescriptor beginAccept() throws SocketException {
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpen();
            if (!this.stream) {
                throw new SocketException("Not a stream socket");
            }
            if (this.localport == 0) {
                throw new SocketException("Not bound");
            }
            this.readerThread = NativeThread.current();
            return this.fd;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void endAccept(boolean completed) throws SocketException {
        Object object = this.stateLock;
        synchronized (object) {
            int state = this.state;
            this.readerThread = 0L;
            if (state == 4) {
                this.tryFinishClose();
            }
            if (!completed && state >= 4) {
                throw new SocketException("Socket closed");
            }
        }
    }

    private int timedAccept(FileDescriptor fd, FileDescriptor newfd, InetSocketAddress[] isaa, long nanos) throws IOException {
        long startNanos = System.nanoTime();
        int n = Net.accept(fd, newfd, isaa);
        while (n == -2 && this.isOpen()) {
            long remainingNanos = nanos - (System.nanoTime() - startNanos);
            if (remainingNanos <= 0L) {
                throw new SocketTimeoutException("Accept timed out");
            }
            this.park(fd, Net.POLLIN, remainingNanos);
            n = Net.accept(fd, newfd, isaa);
        }
        return n;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void accept(SocketImpl si) throws IOException {
        InetSocketAddress localAddress;
        NioSocketImpl nsi = (NioSocketImpl)si;
        if (nsi.state != 0) {
            throw new SocketException("Not a newly created SocketImpl");
        }
        FileDescriptor newfd = new FileDescriptor();
        InetSocketAddress[] isaa = new InetSocketAddress[1];
        ReentrantLock acceptLock = this.readLock;
        int timeout = this.timeout;
        long remainingNanos = 0L;
        if (timeout > 0) {
            remainingNanos = NioSocketImpl.tryLock(acceptLock, timeout, TimeUnit.MILLISECONDS);
            if (remainingNanos <= 0L) {
                assert (!acceptLock.isHeldByCurrentThread());
                throw new SocketTimeoutException("Accept timed out");
            }
        } else {
            acceptLock.lock();
        }
        try {
            int n = 0;
            FileDescriptor fd = this.beginAccept();
            try {
                this.configureNonBlockingIfNeeded(fd, remainingNanos > 0L);
                if (remainingNanos > 0L) {
                    n = this.timedAccept(fd, newfd, isaa, remainingNanos);
                } else {
                    n = Net.accept(fd, newfd, isaa);
                    while (IOStatus.okayToRetry(n) && this.isOpen()) {
                        this.park(fd, Net.POLLIN);
                        n = Net.accept(fd, newfd, isaa);
                    }
                }
                this.endAccept(n > 0);
            }
            catch (Throwable throwable) {
                this.endAccept(n > 0);
                assert (IOStatus.check(n));
                throw throwable;
            }
            assert (IOStatus.check(n));
        }
        finally {
            acceptLock.unlock();
        }
        try {
            localAddress = Net.localAddress(newfd);
            IOUtil.configureBlocking(newfd, true);
        }
        catch (IOException ioe) {
            nd.close(newfd);
            throw ioe;
        }
        Runnable closer = NioSocketImpl.closerFor(newfd, true);
        Object object = nsi.stateLock;
        synchronized (object) {
            nsi.fd = newfd;
            nsi.stream = true;
            nsi.cleaner = CleanerFactory.cleaner().register(nsi, closer);
            nsi.localport = localAddress.getPort();
            nsi.address = isaa[0].getAddress();
            nsi.port = isaa[0].getPort();
            nsi.state = 3;
        }
    }

    @Override
    protected InputStream getInputStream() {
        return new InputStream(){

            @Override
            public int read() throws IOException {
                byte[] a = new byte[1];
                int n = this.read(a, 0, 1);
                return n > 0 ? a[0] & 0xFF : -1;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return NioSocketImpl.this.read(b, off, len);
            }

            @Override
            public int available() throws IOException {
                return NioSocketImpl.this.available();
            }

            @Override
            public void close() throws IOException {
                NioSocketImpl.this.close();
            }
        };
    }

    @Override
    protected OutputStream getOutputStream() {
        return new OutputStream(){

            @Override
            public void write(int b) throws IOException {
                byte[] a = new byte[]{(byte)b};
                this.write(a, 0, 1);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                NioSocketImpl.this.write(b, off, len);
            }

            @Override
            public void close() throws IOException {
                NioSocketImpl.this.close();
            }
        };
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected int available() throws IOException {
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpenAndConnected();
            if (this.isInputClosed) {
                return 0;
            }
            return Net.available(this.fd);
        }
    }

    private boolean tryClose() throws IOException {
        assert (Thread.holdsLock(this.stateLock) && this.state == 4);
        if (this.readerThread == 0L && this.writerThread == 0L) {
            try {
                this.cleaner.clean();
            }
            catch (UncheckedIOException ioe) {
                throw ioe.getCause();
            }
            finally {
                this.state = 5;
            }
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
    @Override
    protected void close() throws IOException {
        Object object = this.stateLock;
        synchronized (object) {
            int state = this.state;
            if (state >= 4) {
                return;
            }
            if (state == 0) {
                this.state = 5;
                return;
            }
            boolean connected = state == 3;
            this.state = 4;
            if (connected) {
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
                    Poller.stopPoll(NioSocketImpl.fdVal(this.fd));
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

    @Override
    protected Set<SocketOption<?>> supportedOptions() {
        Set<SocketOption<?>> options;
        Set<SocketOption<?>> set = options = this.server ? serverSocketOptions : clientSocketOptions;
        if (options == null) {
            options = new HashSet();
            options.add(StandardSocketOptions.SO_RCVBUF);
            options.add(StandardSocketOptions.SO_REUSEADDR);
            if (this.server) {
                options.add(StandardSocketOptions.IP_TOS);
                options.addAll(ExtendedSocketOptions.serverSocketOptions());
            } else {
                options.add(StandardSocketOptions.IP_TOS);
                options.add(StandardSocketOptions.SO_KEEPALIVE);
                options.add(StandardSocketOptions.SO_SNDBUF);
                options.add(StandardSocketOptions.SO_LINGER);
                options.add(StandardSocketOptions.TCP_NODELAY);
                options.addAll(ExtendedSocketOptions.clientSocketOptions());
            }
            if (Net.isReusePortAvailable()) {
                options.add(StandardSocketOptions.SO_REUSEPORT);
            }
            options = Collections.unmodifiableSet(options);
            if (this.server) {
                serverSocketOptions = options;
            } else {
                clientSocketOptions = options;
            }
        }
        return options;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected <T> void setOption(SocketOption<T> opt, T value) throws IOException {
        if (!this.supportedOptions().contains(opt)) {
            throw new UnsupportedOperationException("'" + opt + "' not supported");
        }
        if (!opt.type().isInstance(value)) {
            throw new IllegalArgumentException("Invalid value '" + value + "'");
        }
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpen();
            if (opt == StandardSocketOptions.IP_TOS) {
                Net.setIpSocketOption(this.fd, NioSocketImpl.family(), opt, value);
            } else if (opt == StandardSocketOptions.SO_REUSEADDR) {
                boolean b = (Boolean)value;
                if (Net.useExclusiveBind()) {
                    this.isReuseAddress = b;
                } else {
                    Net.setSocketOption(this.fd, opt, b);
                }
            } else {
                Net.setSocketOption(this.fd, opt, value);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected <T> T getOption(SocketOption<T> opt) throws IOException {
        if (!this.supportedOptions().contains(opt)) {
            throw new UnsupportedOperationException("'" + opt + "' not supported");
        }
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpen();
            if (opt == StandardSocketOptions.IP_TOS) {
                return (T)Net.getSocketOption(this.fd, NioSocketImpl.family(), opt);
            }
            if (opt == StandardSocketOptions.SO_REUSEADDR) {
                if (Net.useExclusiveBind()) {
                    return (T)Boolean.valueOf(this.isReuseAddress);
                }
                return (T)Net.getSocketOption(this.fd, opt);
            }
            return (T)Net.getSocketOption(this.fd, opt);
        }
    }

    private boolean booleanValue(Object value, String desc) throws SocketException {
        if (!(value instanceof Boolean)) {
            throw new SocketException("Bad value for " + desc);
        }
        return (Boolean)value;
    }

    private int intValue(Object value, String desc) throws SocketException {
        if (!(value instanceof Integer)) {
            throw new SocketException("Bad value for " + desc);
        }
        return (Integer)value;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setOption(int opt, Object value) throws SocketException {
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpen();
            try {
                switch (opt) {
                    case 128: {
                        int i = value instanceof Boolean && (Boolean)value == false ? -1 : this.intValue(value, "SO_LINGER");
                        Net.setSocketOption(this.fd, StandardSocketOptions.SO_LINGER, i);
                        break;
                    }
                    case 4102: {
                        int i = this.intValue(value, "SO_TIMEOUT");
                        if (i < 0) {
                            throw new IllegalArgumentException("timeout < 0");
                        }
                        this.timeout = i;
                        break;
                    }
                    case 3: {
                        int i = this.intValue(value, "IP_TOS");
                        Net.setIpSocketOption(this.fd, NioSocketImpl.family(), StandardSocketOptions.IP_TOS, i);
                        break;
                    }
                    case 1: {
                        boolean b = this.booleanValue(value, "TCP_NODELAY");
                        Net.setSocketOption(this.fd, StandardSocketOptions.TCP_NODELAY, b);
                        break;
                    }
                    case 4097: {
                        int i = this.intValue(value, "SO_SNDBUF");
                        if (i <= 0) {
                            throw new SocketException("SO_SNDBUF <= 0");
                        }
                        Net.setSocketOption(this.fd, StandardSocketOptions.SO_SNDBUF, i);
                        break;
                    }
                    case 4098: {
                        int i = this.intValue(value, "SO_RCVBUF");
                        if (i <= 0) {
                            throw new SocketException("SO_RCVBUF <= 0");
                        }
                        Net.setSocketOption(this.fd, StandardSocketOptions.SO_RCVBUF, i);
                        break;
                    }
                    case 8: {
                        boolean b = this.booleanValue(value, "SO_KEEPALIVE");
                        Net.setSocketOption(this.fd, StandardSocketOptions.SO_KEEPALIVE, b);
                        break;
                    }
                    case 4099: {
                        boolean b = this.booleanValue(value, "SO_OOBINLINE");
                        Net.setSocketOption(this.fd, ExtendedSocketOption.SO_OOBINLINE, b);
                        break;
                    }
                    case 4: {
                        boolean b = this.booleanValue(value, "SO_REUSEADDR");
                        if (Net.useExclusiveBind()) {
                            this.isReuseAddress = b;
                            break;
                        }
                        Net.setSocketOption(this.fd, StandardSocketOptions.SO_REUSEADDR, b);
                        break;
                    }
                    case 14: {
                        if (!Net.isReusePortAvailable()) {
                            throw new SocketException("SO_REUSEPORT not supported");
                        }
                        boolean b = this.booleanValue(value, "SO_REUSEPORT");
                        Net.setSocketOption(this.fd, StandardSocketOptions.SO_REUSEPORT, b);
                        break;
                    }
                    default: {
                        throw new SocketException("Unknown option " + opt);
                    }
                }
            }
            catch (SocketException e) {
                throw e;
            }
            catch (IOException | IllegalArgumentException e) {
                throw NioSocketImpl.asSocketException(e);
            }
        }
    }

    @Override
    public Object getOption(int opt) throws SocketException {
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpen();
            try {
                switch (opt) {
                    case 4102: {
                        return this.timeout;
                    }
                    case 1: {
                        return Net.getSocketOption(this.fd, StandardSocketOptions.TCP_NODELAY);
                    }
                    case 4099: {
                        return Net.getSocketOption(this.fd, ExtendedSocketOption.SO_OOBINLINE);
                    }
                    case 128: {
                        int i = (Integer)Net.getSocketOption(this.fd, StandardSocketOptions.SO_LINGER);
                        if (i == -1) {
                            return Boolean.FALSE;
                        }
                        return i;
                    }
                    case 4: {
                        if (Net.useExclusiveBind()) {
                            return this.isReuseAddress;
                        }
                        return Net.getSocketOption(this.fd, StandardSocketOptions.SO_REUSEADDR);
                    }
                    case 15: {
                        return Net.localAddress(this.fd).getAddress();
                    }
                    case 4097: {
                        return Net.getSocketOption(this.fd, StandardSocketOptions.SO_SNDBUF);
                    }
                    case 4098: {
                        return Net.getSocketOption(this.fd, StandardSocketOptions.SO_RCVBUF);
                    }
                    case 3: {
                        return Net.getSocketOption(this.fd, NioSocketImpl.family(), StandardSocketOptions.IP_TOS);
                    }
                    case 8: {
                        return Net.getSocketOption(this.fd, StandardSocketOptions.SO_KEEPALIVE);
                    }
                    case 14: {
                        if (!Net.isReusePortAvailable()) {
                            throw new SocketException("SO_REUSEPORT not supported");
                        }
                        return Net.getSocketOption(this.fd, StandardSocketOptions.SO_REUSEPORT);
                    }
                }
                throw new SocketException("Unknown option " + opt);
            }
            catch (SocketException e) {
                throw e;
            }
            catch (IOException | IllegalArgumentException e) {
                throw NioSocketImpl.asSocketException(e);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void shutdownInput() throws IOException {
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpenAndConnected();
            if (!this.isInputClosed) {
                Net.shutdown(this.fd, 0);
                if (NativeThread.isVirtualThread(this.readerThread)) {
                    Poller.stopPoll(NioSocketImpl.fdVal(this.fd), Net.POLLIN);
                }
                this.isInputClosed = true;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void shutdownOutput() throws IOException {
        Object object = this.stateLock;
        synchronized (object) {
            this.ensureOpenAndConnected();
            if (!this.isOutputClosed) {
                Net.shutdown(this.fd, 1);
                if (NativeThread.isVirtualThread(this.writerThread)) {
                    Poller.stopPoll(NioSocketImpl.fdVal(this.fd), Net.POLLOUT);
                }
                this.isOutputClosed = true;
            }
        }
    }

    @Override
    protected boolean supportsUrgentData() {
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void sendUrgentData(int data) throws IOException {
        this.writeLock.lock();
        try {
            int n = 0;
            FileDescriptor fd = this.beginWrite();
            try {
                this.configureNonBlockingIfNeeded(fd, false);
                while ((n = Net.sendOOB(fd, (byte)data)) == -3 && this.isOpen()) {
                }
                if (n == -2) {
                    throw new SocketException("No buffer space available");
                }
                this.endWrite(n > 0);
            }
            catch (Throwable throwable) {
                this.endWrite(n > 0);
                throw throwable;
            }
        }
        finally {
            this.writeLock.unlock();
        }
    }

    private static Runnable closerFor(FileDescriptor fd, boolean stream) {
        if (stream) {
            return () -> {
                try {
                    nd.close(fd);
                }
                catch (IOException ioe) {
                    throw new UncheckedIOException(ioe);
                }
            };
        }
        return () -> {
            try {
                nd.close(fd);
            }
            catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
            finally {
                ResourceManager.afterUdpClose();
            }
        };
    }

    private static long tryLock(ReentrantLock lock, long timeout, TimeUnit unit) {
        long nanos;
        assert (timeout > 0L);
        boolean interrupted = false;
        long remainingNanos = nanos = unit.toNanos(timeout);
        long startNanos = System.nanoTime();
        boolean acquired = false;
        while (!acquired && remainingNanos > 0L) {
            try {
                acquired = lock.tryLock(remainingNanos, TimeUnit.NANOSECONDS);
            }
            catch (InterruptedException e) {
                interrupted = true;
            }
            remainingNanos = nanos - (System.nanoTime() - startNanos);
        }
        if (acquired && remainingNanos <= 0L) {
            lock.unlock();
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        return remainingNanos;
    }

    private static SocketException asSocketException(Exception e) {
        if (e instanceof SocketException) {
            SocketException se = (SocketException)e;
            return se;
        }
        SocketException se = new SocketException(e.getMessage());
        se.setStackTrace(e.getStackTrace());
        return se;
    }

    private static ProtocolFamily family() {
        if (Net.isIPv6Available()) {
            return StandardProtocolFamily.INET6;
        }
        return StandardProtocolFamily.INET;
    }

    private static int fdVal(FileDescriptor fd) {
        return JIOFDA.get(fd);
    }

    static {
        JIOFDA = SharedSecrets.getJavaIOFileDescriptorAccess();
    }
}

