/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketImpl;
import java.net.SocketOption;
import java.util.Set;

class DummySocketImpl
extends SocketImpl {
    private DummySocketImpl() {
    }

    static SocketImpl create() {
        return new DummySocketImpl();
    }

    private static <T> T shouldNotGetHere() {
        throw new InternalError("Should not get here");
    }

    @Override
    protected void create(boolean stream) {
        DummySocketImpl.shouldNotGetHere();
    }

    @Override
    protected void connect(SocketAddress remote, int millis) {
        DummySocketImpl.shouldNotGetHere();
    }

    @Override
    protected void connect(String host, int port) {
        DummySocketImpl.shouldNotGetHere();
    }

    @Override
    protected void connect(InetAddress address, int port) {
        DummySocketImpl.shouldNotGetHere();
    }

    @Override
    protected void bind(InetAddress host, int port) {
        DummySocketImpl.shouldNotGetHere();
    }

    @Override
    protected void listen(int backlog) {
        DummySocketImpl.shouldNotGetHere();
    }

    @Override
    protected void accept(SocketImpl si) {
        DummySocketImpl.shouldNotGetHere();
    }

    @Override
    protected InputStream getInputStream() {
        return (InputStream)DummySocketImpl.shouldNotGetHere();
    }

    @Override
    protected OutputStream getOutputStream() {
        return (OutputStream)DummySocketImpl.shouldNotGetHere();
    }

    @Override
    protected int available() {
        return (Integer)DummySocketImpl.shouldNotGetHere();
    }

    @Override
    protected void close() {
        DummySocketImpl.shouldNotGetHere();
    }

    @Override
    protected Set<SocketOption<?>> supportedOptions() {
        return (Set)DummySocketImpl.shouldNotGetHere();
    }

    @Override
    protected <T> void setOption(SocketOption<T> opt, T value) {
        DummySocketImpl.shouldNotGetHere();
    }

    @Override
    protected <T> T getOption(SocketOption<T> opt) {
        return DummySocketImpl.shouldNotGetHere();
    }

    @Override
    public void setOption(int opt, Object value) {
        DummySocketImpl.shouldNotGetHere();
    }

    @Override
    public Object getOption(int opt) {
        return DummySocketImpl.shouldNotGetHere();
    }

    @Override
    protected void shutdownInput() {
        DummySocketImpl.shouldNotGetHere();
    }

    @Override
    protected void shutdownOutput() {
        DummySocketImpl.shouldNotGetHere();
    }

    @Override
    protected boolean supportsUrgentData() {
        return (Boolean)DummySocketImpl.shouldNotGetHere();
    }

    @Override
    protected void sendUrgentData(int data) {
        DummySocketImpl.shouldNotGetHere();
    }
}

