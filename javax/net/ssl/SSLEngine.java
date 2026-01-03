/*
 * Decompiled with CFR 0.152.
 */
package javax.net.ssl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiFunction;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

public abstract class SSLEngine {
    private String peerHost = null;
    private int peerPort = -1;

    protected SSLEngine() {
    }

    protected SSLEngine(String peerHost, int peerPort) {
        this.peerHost = peerHost;
        this.peerPort = peerPort;
    }

    public String getPeerHost() {
        return this.peerHost;
    }

    public int getPeerPort() {
        return this.peerPort;
    }

    public SSLEngineResult wrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        return this.wrap(new ByteBuffer[]{src}, 0, 1, dst);
    }

    public SSLEngineResult wrap(ByteBuffer[] srcs, ByteBuffer dst) throws SSLException {
        if (srcs == null) {
            throw new IllegalArgumentException("src == null");
        }
        return this.wrap(srcs, 0, srcs.length, dst);
    }

    public abstract SSLEngineResult wrap(ByteBuffer[] var1, int var2, int var3, ByteBuffer var4) throws SSLException;

    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        return this.unwrap(src, new ByteBuffer[]{dst}, 0, 1);
    }

    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts) throws SSLException {
        if (dsts == null) {
            throw new IllegalArgumentException("dsts == null");
        }
        return this.unwrap(src, dsts, 0, dsts.length);
    }

    public abstract SSLEngineResult unwrap(ByteBuffer var1, ByteBuffer[] var2, int var3, int var4) throws SSLException;

    public abstract Runnable getDelegatedTask();

    public abstract void closeInbound() throws SSLException;

    public abstract boolean isInboundDone();

    public abstract void closeOutbound();

    public abstract boolean isOutboundDone();

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

    public abstract void beginHandshake() throws SSLException;

    public abstract SSLEngineResult.HandshakeStatus getHandshakeStatus();

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

    public void setHandshakeApplicationProtocolSelector(BiFunction<SSLEngine, List<String>, String> selector) {
        throw new UnsupportedOperationException();
    }

    public BiFunction<SSLEngine, List<String>, String> getHandshakeApplicationProtocolSelector() {
        throw new UnsupportedOperationException();
    }
}

