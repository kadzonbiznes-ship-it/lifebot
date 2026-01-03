/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketOption;
import java.util.Objects;
import java.util.Set;
import sun.net.PlatformSocketImpl;

class DelegatingSocketImpl
extends SocketImpl {
    protected final SocketImpl delegate;

    DelegatingSocketImpl(SocketImpl delegate) {
        assert (delegate instanceof PlatformSocketImpl);
        this.delegate = Objects.requireNonNull(delegate);
    }

    final SocketImpl delegate() {
        return this.delegate;
    }

    @Override
    protected FileDescriptor getFileDescriptor() {
        return this.delegate.getFileDescriptor();
    }

    @Override
    protected InetAddress getInetAddress() {
        return this.delegate.getInetAddress();
    }

    @Override
    protected int getPort() {
        return this.delegate.getPort();
    }

    @Override
    protected int getLocalPort() {
        return this.delegate.getLocalPort();
    }

    @Override
    protected void create(boolean stream) throws IOException {
        this.delegate.create(stream);
    }

    @Override
    protected void connect(String host, int port) throws IOException {
        this.delegate.connect(host, port);
    }

    @Override
    protected void connect(InetAddress address, int port) throws IOException {
        this.delegate.connect(address, port);
    }

    @Override
    protected void connect(SocketAddress address, int timeout) throws IOException {
        this.delegate.connect(address, timeout);
    }

    @Override
    protected void bind(InetAddress host, int port) throws IOException {
        this.delegate.bind(host, port);
    }

    @Override
    protected void listen(int backlog) throws IOException {
        this.delegate.listen(backlog);
    }

    @Override
    protected void accept(SocketImpl s) throws IOException {
        this.delegate.accept(s);
    }

    @Override
    protected InputStream getInputStream() throws IOException {
        return this.delegate.getInputStream();
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        return this.delegate.getOutputStream();
    }

    @Override
    protected int available() throws IOException {
        return this.delegate.available();
    }

    @Override
    protected void close() throws IOException {
        this.delegate.close();
    }

    @Override
    protected boolean supportsUrgentData() {
        return this.delegate.supportsUrgentData();
    }

    @Override
    protected void sendUrgentData(int data) throws IOException {
        this.delegate.sendUrgentData(data);
    }

    @Override
    protected Set<SocketOption<?>> supportedOptions() {
        return this.delegate.supportedOptions();
    }

    @Override
    protected <T> void setOption(SocketOption<T> opt, T value) throws IOException {
        this.delegate.setOption(opt, value);
    }

    @Override
    protected <T> T getOption(SocketOption<T> opt) throws IOException {
        return this.delegate.getOption(opt);
    }

    @Override
    public void setOption(int optID, Object value) throws SocketException {
        this.delegate.setOption(optID, value);
    }

    @Override
    public Object getOption(int optID) throws SocketException {
        return this.delegate.getOption(optID);
    }

    @Override
    protected void shutdownInput() throws IOException {
        this.delegate.shutdownInput();
    }

    @Override
    protected void shutdownOutput() throws IOException {
        this.delegate.shutdownOutput();
    }
}

