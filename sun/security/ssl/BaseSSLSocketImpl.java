/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.nio.channels.SocketChannel;
import java.util.Set;
import javax.net.ssl.SSLSocket;
import sun.security.ssl.Utilities;

abstract class BaseSSLSocketImpl
extends SSLSocket {
    private final Socket self;
    private final InputStream consumedInput;
    private static final String PROP_NAME = "com.sun.net.ssl.requireCloseNotify";
    static final boolean requireCloseNotify = Utilities.getBooleanProperty("com.sun.net.ssl.requireCloseNotify", false);

    BaseSSLSocketImpl() {
        this.self = this;
        this.consumedInput = null;
    }

    BaseSSLSocketImpl(Socket socket) {
        this.self = socket;
        this.consumedInput = null;
    }

    BaseSSLSocketImpl(Socket socket, InputStream consumed) {
        this.self = socket;
        this.consumedInput = consumed;
    }

    @Override
    public final SocketChannel getChannel() {
        if (this.self == this) {
            return super.getChannel();
        }
        return this.self.getChannel();
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        if (this.self != this) {
            throw new IOException("Underlying socket should already be connected");
        }
        super.bind(bindpoint);
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        if (this.self == this) {
            return super.getLocalSocketAddress();
        }
        return this.self.getLocalSocketAddress();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        if (this.self == this) {
            return super.getRemoteSocketAddress();
        }
        return this.self.getRemoteSocketAddress();
    }

    @Override
    public final void connect(SocketAddress endpoint) throws IOException {
        this.connect(endpoint, 0);
    }

    @Override
    public final boolean isConnected() {
        if (this.self == this) {
            return super.isConnected();
        }
        return this.self.isConnected();
    }

    @Override
    public final boolean isBound() {
        if (this.self == this) {
            return super.isBound();
        }
        return this.self.isBound();
    }

    @Override
    public void shutdownInput() throws IOException {
        if (this.self == this) {
            super.shutdownInput();
        } else {
            this.self.shutdownInput();
        }
    }

    @Override
    public void shutdownOutput() throws IOException {
        if (this.self == this) {
            super.shutdownOutput();
        } else {
            this.self.shutdownOutput();
        }
    }

    @Override
    public boolean isInputShutdown() {
        if (this.self == this) {
            return super.isInputShutdown();
        }
        return this.self.isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        if (this.self == this) {
            return super.isOutputShutdown();
        }
        return this.self.isOutputShutdown();
    }

    @Override
    public final InetAddress getInetAddress() {
        if (this.self == this) {
            return super.getInetAddress();
        }
        return this.self.getInetAddress();
    }

    @Override
    public final InetAddress getLocalAddress() {
        if (this.self == this) {
            return super.getLocalAddress();
        }
        return this.self.getLocalAddress();
    }

    @Override
    public final int getPort() {
        if (this.self == this) {
            return super.getPort();
        }
        return this.self.getPort();
    }

    @Override
    public final int getLocalPort() {
        if (this.self == this) {
            return super.getLocalPort();
        }
        return this.self.getLocalPort();
    }

    @Override
    public final void setTcpNoDelay(boolean value) throws SocketException {
        if (this.self == this) {
            super.setTcpNoDelay(value);
        } else {
            this.self.setTcpNoDelay(value);
        }
    }

    @Override
    public final boolean getTcpNoDelay() throws SocketException {
        if (this.self == this) {
            return super.getTcpNoDelay();
        }
        return this.self.getTcpNoDelay();
    }

    @Override
    public final void setSoLinger(boolean flag, int linger) throws SocketException {
        if (this.self == this) {
            super.setSoLinger(flag, linger);
        } else {
            this.self.setSoLinger(flag, linger);
        }
    }

    @Override
    public final int getSoLinger() throws SocketException {
        if (this.self == this) {
            return super.getSoLinger();
        }
        return this.self.getSoLinger();
    }

    @Override
    public final void sendUrgentData(int data) throws SocketException {
        throw new SocketException("This method is not supported by SSLSockets");
    }

    @Override
    public final void setOOBInline(boolean on) throws SocketException {
        throw new SocketException("This method is ineffective, since sending urgent data is not supported by SSLSockets");
    }

    @Override
    public final boolean getOOBInline() throws SocketException {
        throw new SocketException("This method is ineffective, since sending urgent data is not supported by SSLSockets");
    }

    @Override
    public final int getSoTimeout() throws SocketException {
        if (this.self == this) {
            return super.getSoTimeout();
        }
        return this.self.getSoTimeout();
    }

    @Override
    public final void setSendBufferSize(int size) throws SocketException {
        if (this.self == this) {
            super.setSendBufferSize(size);
        } else {
            this.self.setSendBufferSize(size);
        }
    }

    @Override
    public final int getSendBufferSize() throws SocketException {
        if (this.self == this) {
            return super.getSendBufferSize();
        }
        return this.self.getSendBufferSize();
    }

    @Override
    public final void setReceiveBufferSize(int size) throws SocketException {
        if (this.self == this) {
            super.setReceiveBufferSize(size);
        } else {
            this.self.setReceiveBufferSize(size);
        }
    }

    @Override
    public final int getReceiveBufferSize() throws SocketException {
        if (this.self == this) {
            return super.getReceiveBufferSize();
        }
        return this.self.getReceiveBufferSize();
    }

    @Override
    public final void setKeepAlive(boolean on) throws SocketException {
        if (this.self == this) {
            super.setKeepAlive(on);
        } else {
            this.self.setKeepAlive(on);
        }
    }

    @Override
    public final boolean getKeepAlive() throws SocketException {
        if (this.self == this) {
            return super.getKeepAlive();
        }
        return this.self.getKeepAlive();
    }

    @Override
    public final void setTrafficClass(int tc) throws SocketException {
        if (this.self == this) {
            super.setTrafficClass(tc);
        } else {
            this.self.setTrafficClass(tc);
        }
    }

    @Override
    public final int getTrafficClass() throws SocketException {
        if (this.self == this) {
            return super.getTrafficClass();
        }
        return this.self.getTrafficClass();
    }

    @Override
    public final void setReuseAddress(boolean on) throws SocketException {
        if (this.self == this) {
            super.setReuseAddress(on);
        } else {
            this.self.setReuseAddress(on);
        }
    }

    @Override
    public final boolean getReuseAddress() throws SocketException {
        if (this.self == this) {
            return super.getReuseAddress();
        }
        return this.self.getReuseAddress();
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        if (this.self == this) {
            super.setPerformancePreferences(connectionTime, latency, bandwidth);
        } else {
            this.self.setPerformancePreferences(connectionTime, latency, bandwidth);
        }
    }

    @Override
    public String toString() {
        if (this.self == this) {
            return super.toString();
        }
        return this.self.toString();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (this.self == this) {
            return super.getInputStream();
        }
        if (this.consumedInput != null) {
            return new SequenceInputStream(this.consumedInput, this.self.getInputStream());
        }
        return this.self.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (this.self == this) {
            return super.getOutputStream();
        }
        return this.self.getOutputStream();
    }

    @Override
    public void close() throws IOException {
        if (this.self == this) {
            super.close();
        } else {
            this.self.close();
        }
    }

    @Override
    public void setSoTimeout(int timeout) throws SocketException {
        if (this.self == this) {
            super.setSoTimeout(timeout);
        } else {
            this.self.setSoTimeout(timeout);
        }
    }

    @Override
    public <T> Socket setOption(SocketOption<T> name, T value) throws IOException {
        if (this.self == this) {
            return super.setOption(name, value);
        }
        return this.self.setOption(name, value);
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        if (this.self == this) {
            return super.getOption(name);
        }
        return this.self.getOption(name);
    }

    @Override
    public Set<SocketOption<?>> supportedOptions() {
        if (this.self == this) {
            return super.supportedOptions();
        }
        return this.self.supportedOptions();
    }

    boolean isLayered() {
        return this.self != this;
    }
}

