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
import java.net.SocketOption;
import java.net.SocketOptions;
import java.util.Objects;
import java.util.Set;
import sun.nio.ch.NioSocketImpl;

public abstract class SocketImpl
implements SocketOptions {
    protected FileDescriptor fd;
    protected InetAddress address;
    protected int port;
    protected int localport;

    static <S extends SocketImpl> S createPlatformSocketImpl(boolean server) {
        return (S)new NioSocketImpl(server);
    }

    protected abstract void create(boolean var1) throws IOException;

    protected abstract void connect(String var1, int var2) throws IOException;

    protected abstract void connect(InetAddress var1, int var2) throws IOException;

    protected abstract void connect(SocketAddress var1, int var2) throws IOException;

    protected abstract void bind(InetAddress var1, int var2) throws IOException;

    protected abstract void listen(int var1) throws IOException;

    protected abstract void accept(SocketImpl var1) throws IOException;

    protected abstract InputStream getInputStream() throws IOException;

    protected abstract OutputStream getOutputStream() throws IOException;

    protected abstract int available() throws IOException;

    protected abstract void close() throws IOException;

    void closeQuietly() {
        try {
            this.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    protected void shutdownInput() throws IOException {
        throw new IOException("Method not implemented!");
    }

    protected void shutdownOutput() throws IOException {
        throw new IOException("Method not implemented!");
    }

    protected FileDescriptor getFileDescriptor() {
        return this.fd;
    }

    protected InetAddress getInetAddress() {
        return this.address;
    }

    protected int getPort() {
        return this.port;
    }

    protected boolean supportsUrgentData() {
        return false;
    }

    protected abstract void sendUrgentData(int var1) throws IOException;

    protected int getLocalPort() {
        return this.localport;
    }

    public String toString() {
        return "Socket[addr=" + this.getInetAddress() + ",port=" + this.getPort() + ",localport=" + this.getLocalPort() + "]";
    }

    void reset() {
        this.fd = null;
        this.address = null;
        this.port = 0;
        this.localport = 0;
    }

    protected void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
    }

    protected <T> void setOption(SocketOption<T> name, T value) throws IOException {
        Objects.requireNonNull(name);
        throw new UnsupportedOperationException("'" + name + "' not supported");
    }

    protected <T> T getOption(SocketOption<T> name) throws IOException {
        Objects.requireNonNull(name);
        throw new UnsupportedOperationException("'" + name + "' not supported");
    }

    void copyOptionsTo(SocketImpl target) {
        try {
            Object timeout = this.getOption(4102);
            if (timeout instanceof Integer) {
                target.setOption(4102, timeout);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    protected Set<SocketOption<?>> supportedOptions() {
        return Set.of();
    }
}

