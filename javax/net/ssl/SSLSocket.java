/*
 * Decompiled with CFR 0.152.
 */
package javax.net.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.function.BiFunction;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

public abstract class SSLSocket
extends Socket {
    protected SSLSocket() {
    }

    protected SSLSocket(String host, int port) throws IOException, UnknownHostException {
        super(host, port);
    }

    protected SSLSocket(InetAddress address, int port) throws IOException {
        super(address, port);
    }

    protected SSLSocket(String host, int port, InetAddress clientAddress, int clientPort) throws IOException, UnknownHostException {
        super(host, port, clientAddress, clientPort);
    }

    protected SSLSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort) throws IOException {
        super(address, port, clientAddress, clientPort);
    }

    public abstract String[] getSupportedCipherSuites();

    public abstract String[] getEnabledCipherSuites();

    public abstract void setEnabledCipherSuites(String[] var1);

    public abstract String[] getSupportedProtocols();

    public abstract String[] getEnabledProtocols();

    public abstract void setEnabledProtocols(String[] var1);

    public abstract SSLSession getSession();

    public SSLSession getHandshakeSession() {
        throw new UnsupportedOperationException();
    }

    public abstract void addHandshakeCompletedListener(HandshakeCompletedListener var1);

    public abstract void removeHandshakeCompletedListener(HandshakeCompletedListener var1);

    public abstract void startHandshake() throws IOException;

    public abstract void setUseClientMode(boolean var1);

    public abstract boolean getUseClientMode();

    public abstract void setNeedClientAuth(boolean var1);

    public abstract boolean getNeedClientAuth();

    public abstract void setWantClientAuth(boolean var1);

    public abstract boolean getWantClientAuth();

    public abstract void setEnableSessionCreation(boolean var1);

    public abstract boolean getEnableSessionCreation();

    public SSLParameters getSSLParameters() {
        SSLParameters params = new SSLParameters();
        params.setCipherSuites(this.getEnabledCipherSuites());
        params.setProtocols(this.getEnabledProtocols());
        if (this.getNeedClientAuth()) {
            params.setNeedClientAuth(true);
        } else if (this.getWantClientAuth()) {
            params.setWantClientAuth(true);
        }
        return params;
    }

    public void setSSLParameters(SSLParameters params) {
        String[] s = params.getCipherSuites();
        if (s != null) {
            this.setEnabledCipherSuites(s);
        }
        if ((s = params.getProtocols()) != null) {
            this.setEnabledProtocols(s);
        }
        if (params.getNeedClientAuth()) {
            this.setNeedClientAuth(true);
        } else {
            this.setWantClientAuth(params.getWantClientAuth());
        }
    }

    public String getApplicationProtocol() {
        throw new UnsupportedOperationException();
    }

    public String getHandshakeApplicationProtocol() {
        throw new UnsupportedOperationException();
    }

    public void setHandshakeApplicationProtocolSelector(BiFunction<SSLSocket, List<String>, String> selector) {
        throw new UnsupportedOperationException();
    }

    public BiFunction<SSLSocket, List<String>, String> getHandshakeApplicationProtocolSelector() {
        throw new UnsupportedOperationException();
    }
}

