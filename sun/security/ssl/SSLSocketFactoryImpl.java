/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import javax.net.ssl.SSLSocketFactory;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.SSLContextImpl;
import sun.security.ssl.SSLSocketImpl;

public final class SSLSocketFactoryImpl
extends SSLSocketFactory {
    private final SSLContextImpl context;

    public SSLSocketFactoryImpl() throws Exception {
        this.context = SSLContextImpl.DefaultSSLContext.getDefaultImpl();
    }

    SSLSocketFactoryImpl(SSLContextImpl context) {
        this.context = context;
    }

    @Override
    public Socket createSocket() {
        return new SSLSocketImpl(this.context);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return new SSLSocketImpl(this.context, host, port);
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return new SSLSocketImpl(this.context, s, host, port, autoClose);
    }

    @Override
    public Socket createSocket(Socket s, InputStream consumed, boolean autoClose) throws IOException {
        if (s == null) {
            throw new NullPointerException("the existing socket cannot be null");
        }
        return new SSLSocketImpl(this.context, s, consumed, autoClose);
    }

    @Override
    public Socket createSocket(InetAddress address, int port) throws IOException {
        return new SSLSocketImpl(this.context, address, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress clientAddress, int clientPort) throws IOException {
        return new SSLSocketImpl(this.context, host, port, clientAddress, clientPort);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort) throws IOException {
        return new SSLSocketImpl(this.context, address, port, clientAddress, clientPort);
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return CipherSuite.namesOf(this.context.getDefaultCipherSuites(false));
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return CipherSuite.namesOf(this.context.getSupportedCipherSuites());
    }
}

