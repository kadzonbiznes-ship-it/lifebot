/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.channels.SocketChannel;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import sun.nio.ch.DummySocketImpl;
import sun.nio.ch.ExtendedSocketOption;
import sun.nio.ch.Net;
import sun.nio.ch.SocketChannelImpl;
import sun.nio.ch.SocketInputStream;
import sun.nio.ch.SocketOutputStream;

class SocketAdaptor
extends Socket {
    private final SocketChannelImpl sc;
    private volatile int timeout;

    private SocketAdaptor(SocketChannelImpl sc) throws SocketException {
        super(DummySocketImpl.create());
        this.sc = sc;
    }

    static Socket create(SocketChannelImpl sc) {
        try {
            if (System.getSecurityManager() == null) {
                return new SocketAdaptor(sc);
            }
            PrivilegedExceptionAction<Socket> pa = () -> new SocketAdaptor(sc);
            return AccessController.doPrivileged(pa);
        }
        catch (SocketException | PrivilegedActionException e) {
            throw new InternalError(e);
        }
    }

    private InetSocketAddress localAddress() {
        return (InetSocketAddress)this.sc.localAddress();
    }

    private InetSocketAddress remoteAddress() {
        return (InetSocketAddress)this.sc.remoteAddress();
    }

    @Override
    public void connect(SocketAddress remote) throws IOException {
        this.connect(remote, 0);
    }

    @Override
    public void connect(SocketAddress remote, int timeout) throws IOException {
        if (remote == null) {
            throw new IllegalArgumentException("connect: The address can't be null");
        }
        if (timeout < 0) {
            throw new IllegalArgumentException("connect: timeout can't be negative");
        }
        try {
            if (timeout > 0) {
                long nanos = TimeUnit.MILLISECONDS.toNanos(timeout);
                this.sc.blockingConnect(remote, nanos);
            } else {
                this.sc.blockingConnect(remote, Long.MAX_VALUE);
            }
        }
        catch (Exception e) {
            Net.translateException(e, true);
        }
    }

    @Override
    public void bind(SocketAddress local) throws IOException {
        try {
            this.sc.bind(local);
        }
        catch (Exception x) {
            Net.translateException(x);
        }
    }

    @Override
    public InetAddress getInetAddress() {
        InetSocketAddress remote = this.remoteAddress();
        if (remote == null) {
            return null;
        }
        return remote.getAddress();
    }

    @Override
    public InetAddress getLocalAddress() {
        InetSocketAddress local;
        if (this.sc.isOpen() && (local = this.localAddress()) != null) {
            return Net.getRevealedLocalAddress(local).getAddress();
        }
        return new InetSocketAddress(0).getAddress();
    }

    @Override
    public int getPort() {
        InetSocketAddress remote = this.remoteAddress();
        if (remote == null) {
            return 0;
        }
        return remote.getPort();
    }

    @Override
    public int getLocalPort() {
        InetSocketAddress local = this.localAddress();
        if (local == null) {
            return -1;
        }
        return local.getPort();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return this.sc.remoteAddress();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return Net.getRevealedLocalAddress(this.sc.localAddress());
    }

    @Override
    public SocketChannel getChannel() {
        return this.sc;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (!this.sc.isOpen()) {
            throw new SocketException("Socket is closed");
        }
        if (!this.sc.isConnected()) {
            throw new SocketException("Socket is not connected");
        }
        if (!this.sc.isInputOpen()) {
            throw new SocketException("Socket input is shutdown");
        }
        return new SocketInputStream(this.sc, () -> this.timeout);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (!this.sc.isOpen()) {
            throw new SocketException("Socket is closed");
        }
        if (!this.sc.isConnected()) {
            throw new SocketException("Socket is not connected");
        }
        if (!this.sc.isOutputOpen()) {
            throw new SocketException("Socket output is shutdown");
        }
        return new SocketOutputStream(this.sc);
    }

    private void setBooleanOption(SocketOption<Boolean> name, boolean value) throws SocketException {
        try {
            this.sc.setOption((SocketOption)name, (Object)value);
        }
        catch (IOException x) {
            Net.translateToSocketException(x);
        }
    }

    private void setIntOption(SocketOption<Integer> name, int value) throws SocketException {
        try {
            this.sc.setOption((SocketOption)name, (Object)value);
        }
        catch (IOException x) {
            Net.translateToSocketException(x);
        }
    }

    private boolean getBooleanOption(SocketOption<Boolean> name) throws SocketException {
        try {
            return this.sc.getOption(name);
        }
        catch (IOException x) {
            Net.translateToSocketException(x);
            return false;
        }
    }

    private int getIntOption(SocketOption<Integer> name) throws SocketException {
        try {
            return this.sc.getOption(name);
        }
        catch (IOException x) {
            Net.translateToSocketException(x);
            return -1;
        }
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        this.setBooleanOption(StandardSocketOptions.TCP_NODELAY, on);
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return this.getBooleanOption(StandardSocketOptions.TCP_NODELAY);
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        if (!on) {
            linger = -1;
        }
        this.setIntOption(StandardSocketOptions.SO_LINGER, linger);
    }

    @Override
    public int getSoLinger() throws SocketException {
        return this.getIntOption(StandardSocketOptions.SO_LINGER);
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        int n = this.sc.sendOutOfBandData((byte)data);
        if (n == 0) {
            throw new IOException("Socket buffer full");
        }
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        this.setBooleanOption(ExtendedSocketOption.SO_OOBINLINE, on);
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        return this.getBooleanOption(ExtendedSocketOption.SO_OOBINLINE);
    }

    @Override
    public void setSoTimeout(int timeout) throws SocketException {
        if (!this.sc.isOpen()) {
            throw new SocketException("Socket is closed");
        }
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout < 0");
        }
        this.timeout = timeout;
    }

    @Override
    public int getSoTimeout() throws SocketException {
        if (!this.sc.isOpen()) {
            throw new SocketException("Socket is closed");
        }
        return this.timeout;
    }

    @Override
    public void setSendBufferSize(int size) throws SocketException {
        if (size <= 0) {
            throw new IllegalArgumentException("Invalid send size");
        }
        this.setIntOption(StandardSocketOptions.SO_SNDBUF, size);
    }

    @Override
    public int getSendBufferSize() throws SocketException {
        return this.getIntOption(StandardSocketOptions.SO_SNDBUF);
    }

    @Override
    public void setReceiveBufferSize(int size) throws SocketException {
        if (size <= 0) {
            throw new IllegalArgumentException("Invalid receive size");
        }
        this.setIntOption(StandardSocketOptions.SO_RCVBUF, size);
    }

    @Override
    public int getReceiveBufferSize() throws SocketException {
        return this.getIntOption(StandardSocketOptions.SO_RCVBUF);
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        this.setBooleanOption(StandardSocketOptions.SO_KEEPALIVE, on);
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return this.getBooleanOption(StandardSocketOptions.SO_KEEPALIVE);
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException {
        this.setIntOption(StandardSocketOptions.IP_TOS, tc);
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return this.getIntOption(StandardSocketOptions.IP_TOS);
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        this.setBooleanOption(StandardSocketOptions.SO_REUSEADDR, on);
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return this.getBooleanOption(StandardSocketOptions.SO_REUSEADDR);
    }

    @Override
    public void close() throws IOException {
        this.sc.close();
    }

    @Override
    public void shutdownInput() throws IOException {
        try {
            this.sc.shutdownInput();
        }
        catch (Exception x) {
            Net.translateException(x);
        }
    }

    @Override
    public void shutdownOutput() throws IOException {
        try {
            this.sc.shutdownOutput();
        }
        catch (Exception x) {
            Net.translateException(x);
        }
    }

    @Override
    public String toString() {
        if (this.sc.isConnected()) {
            return "Socket[addr=" + this.getInetAddress() + ",port=" + this.getPort() + ",localport=" + this.getLocalPort() + "]";
        }
        return "Socket[unconnected]";
    }

    @Override
    public boolean isConnected() {
        return this.sc.isConnected();
    }

    @Override
    public boolean isBound() {
        return this.sc.localAddress() != null;
    }

    @Override
    public boolean isClosed() {
        return !this.sc.isOpen();
    }

    @Override
    public boolean isInputShutdown() {
        return !this.sc.isInputOpen();
    }

    @Override
    public boolean isOutputShutdown() {
        return !this.sc.isOutputOpen();
    }

    @Override
    public <T> Socket setOption(SocketOption<T> name, T value) throws IOException {
        this.sc.setOption((SocketOption)name, (Object)value);
        return this;
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        return this.sc.getOption(name);
    }

    @Override
    public Set<SocketOption<?>> supportedOptions() {
        return this.sc.supportedOptions();
    }
}

