/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLProtocolException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import jdk.internal.access.JavaNetInetAddressAccess;
import jdk.internal.access.SharedSecrets;
import sun.security.ssl.Alert;
import sun.security.ssl.BaseSSLSocketImpl;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.ClientAuthType;
import sun.security.ssl.ContentType;
import sun.security.ssl.HandshakeHash;
import sun.security.ssl.InputRecord;
import sun.security.ssl.NewSessionTicket;
import sun.security.ssl.Plaintext;
import sun.security.ssl.PostHandshakeContext;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.SSLConfiguration;
import sun.security.ssl.SSLContextImpl;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLSessionImpl;
import sun.security.ssl.SSLSocketInputRecord;
import sun.security.ssl.SSLSocketOutputRecord;
import sun.security.ssl.SSLTransport;
import sun.security.ssl.TransportContext;
import sun.security.ssl.Utilities;

public final class SSLSocketImpl
extends BaseSSLSocketImpl
implements SSLTransport {
    final SSLContextImpl sslContext;
    final TransportContext conContext;
    private final AppInputStream appInput = new AppInputStream();
    private final AppOutputStream appOutput = new AppOutputStream();
    private String peerHost;
    private boolean autoClose;
    private boolean isConnected;
    private volatile boolean tlsIsClosed;
    private final ReentrantLock socketLock = new ReentrantLock();
    private final ReentrantLock handshakeLock = new ReentrantLock();
    private static final boolean trustNameService = Utilities.getBooleanProperty("jdk.tls.trustNameService", false);
    private static final int DEFAULT_SKIP_TIMEOUT = 1;

    SSLSocketImpl(SSLContextImpl sslContext) {
        this.sslContext = sslContext;
        HandshakeHash handshakeHash = new HandshakeHash();
        this.conContext = new TransportContext(sslContext, (SSLTransport)this, new SSLSocketInputRecord(handshakeHash), new SSLSocketOutputRecord(handshakeHash), true);
    }

    SSLSocketImpl(SSLContextImpl sslContext, SSLConfiguration sslConfig) {
        this.sslContext = sslContext;
        HandshakeHash handshakeHash = new HandshakeHash();
        this.conContext = new TransportContext(sslContext, (SSLTransport)this, sslConfig, new SSLSocketInputRecord(handshakeHash), new SSLSocketOutputRecord(handshakeHash));
    }

    SSLSocketImpl(SSLContextImpl sslContext, String peerHost, int peerPort) throws IOException {
        this.sslContext = sslContext;
        HandshakeHash handshakeHash = new HandshakeHash();
        this.conContext = new TransportContext(sslContext, (SSLTransport)this, new SSLSocketInputRecord(handshakeHash), new SSLSocketOutputRecord(handshakeHash), true);
        this.peerHost = peerHost;
        InetSocketAddress socketAddress = peerHost != null ? new InetSocketAddress(peerHost, peerPort) : new InetSocketAddress(InetAddress.getByName(null), peerPort);
        this.connect(socketAddress, 0);
    }

    SSLSocketImpl(SSLContextImpl sslContext, InetAddress address, int peerPort) throws IOException {
        this.sslContext = sslContext;
        HandshakeHash handshakeHash = new HandshakeHash();
        this.conContext = new TransportContext(sslContext, (SSLTransport)this, new SSLSocketInputRecord(handshakeHash), new SSLSocketOutputRecord(handshakeHash), true);
        InetSocketAddress socketAddress = new InetSocketAddress(address, peerPort);
        this.connect(socketAddress, 0);
    }

    SSLSocketImpl(SSLContextImpl sslContext, String peerHost, int peerPort, InetAddress localAddr, int localPort) throws IOException {
        this.sslContext = sslContext;
        HandshakeHash handshakeHash = new HandshakeHash();
        this.conContext = new TransportContext(sslContext, (SSLTransport)this, new SSLSocketInputRecord(handshakeHash), new SSLSocketOutputRecord(handshakeHash), true);
        this.peerHost = peerHost;
        this.bind(new InetSocketAddress(localAddr, localPort));
        InetSocketAddress socketAddress = peerHost != null ? new InetSocketAddress(peerHost, peerPort) : new InetSocketAddress(InetAddress.getByName(null), peerPort);
        this.connect(socketAddress, 0);
    }

    SSLSocketImpl(SSLContextImpl sslContext, InetAddress peerAddr, int peerPort, InetAddress localAddr, int localPort) throws IOException {
        this.sslContext = sslContext;
        HandshakeHash handshakeHash = new HandshakeHash();
        this.conContext = new TransportContext(sslContext, (SSLTransport)this, new SSLSocketInputRecord(handshakeHash), new SSLSocketOutputRecord(handshakeHash), true);
        this.bind(new InetSocketAddress(localAddr, localPort));
        InetSocketAddress socketAddress = new InetSocketAddress(peerAddr, peerPort);
        this.connect(socketAddress, 0);
    }

    SSLSocketImpl(SSLContextImpl sslContext, Socket sock, InputStream consumed, boolean autoClose) throws IOException {
        super(sock, consumed);
        if (!sock.isConnected()) {
            throw new SocketException("Underlying socket is not connected");
        }
        this.sslContext = sslContext;
        HandshakeHash handshakeHash = new HandshakeHash();
        this.conContext = new TransportContext(sslContext, (SSLTransport)this, new SSLSocketInputRecord(handshakeHash), new SSLSocketOutputRecord(handshakeHash), false);
        this.autoClose = autoClose;
        this.doneConnect();
    }

    SSLSocketImpl(SSLContextImpl sslContext, Socket sock, String peerHost, int port, boolean autoClose) throws IOException {
        super(sock);
        if (!sock.isConnected()) {
            throw new SocketException("Underlying socket is not connected");
        }
        this.sslContext = sslContext;
        HandshakeHash handshakeHash = new HandshakeHash();
        this.conContext = new TransportContext(sslContext, (SSLTransport)this, new SSLSocketInputRecord(handshakeHash), new SSLSocketOutputRecord(handshakeHash), true);
        this.peerHost = peerHost;
        this.autoClose = autoClose;
        this.doneConnect();
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        if (this.isLayered()) {
            throw new SocketException("Already connected");
        }
        if (!(endpoint instanceof InetSocketAddress)) {
            throw new SocketException("Cannot handle non-Inet socket addresses.");
        }
        super.connect(endpoint, timeout);
        this.doneConnect();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return CipherSuite.namesOf(this.sslContext.getSupportedCipherSuites());
    }

    @Override
    public String[] getEnabledCipherSuites() {
        this.socketLock.lock();
        try {
            String[] stringArray = CipherSuite.namesOf(this.conContext.sslConfig.enabledCipherSuites);
            return stringArray;
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public void setEnabledCipherSuites(String[] suites) {
        this.socketLock.lock();
        try {
            this.conContext.sslConfig.enabledCipherSuites = CipherSuite.validValuesOf(suites);
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public String[] getSupportedProtocols() {
        return ProtocolVersion.toStringArray(this.sslContext.getSupportedProtocolVersions());
    }

    @Override
    public String[] getEnabledProtocols() {
        this.socketLock.lock();
        try {
            String[] stringArray = ProtocolVersion.toStringArray(this.conContext.sslConfig.enabledProtocols);
            return stringArray;
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public void setEnabledProtocols(String[] protocols) {
        if (protocols == null) {
            throw new IllegalArgumentException("Protocols cannot be null");
        }
        this.socketLock.lock();
        try {
            this.conContext.sslConfig.enabledProtocols = ProtocolVersion.namesOf(protocols);
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public SSLSession getSession() {
        try {
            this.ensureNegotiated(false);
        }
        catch (IOException ioe) {
            if (SSLLogger.isOn && SSLLogger.isOn("handshake")) {
                SSLLogger.severe("handshake failed", ioe);
            }
            return new SSLSessionImpl();
        }
        return this.conContext.conSession;
    }

    @Override
    public SSLSession getHandshakeSession() {
        this.socketLock.lock();
        try {
            SSLSessionImpl sSLSessionImpl = this.conContext.handshakeContext == null ? null : this.conContext.handshakeContext.handshakeSession;
            return sSLSessionImpl;
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        this.socketLock.lock();
        try {
            this.conContext.sslConfig.addHandshakeCompletedListener(listener);
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        this.socketLock.lock();
        try {
            this.conContext.sslConfig.removeHandshakeCompletedListener(listener);
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public void startHandshake() throws IOException {
        this.startHandshake(true);
    }

    private void startHandshake(boolean resumable) throws IOException {
        block13: {
            if (!this.isConnected) {
                throw new SocketException("Socket is not connected");
            }
            if (this.conContext.isBroken || this.conContext.isInboundClosed() || this.conContext.isOutboundClosed()) {
                throw new SocketException("Socket has been closed or broken");
            }
            this.handshakeLock.lock();
            try {
                if (this.conContext.isBroken || this.conContext.isInboundClosed() || this.conContext.isOutboundClosed()) {
                    throw new SocketException("Socket has been closed or broken");
                }
                try {
                    this.conContext.kickstart();
                    if (!this.conContext.isNegotiated) {
                        this.readHandshakeRecord();
                    }
                }
                catch (InterruptedIOException iioe) {
                    if (resumable) {
                        this.handleException(iioe);
                        break block13;
                    }
                    throw this.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Couldn't kickstart handshaking", iioe);
                }
                catch (SocketException se) {
                    this.handleException(se);
                }
                catch (IOException ioe) {
                    throw this.conContext.fatal(Alert.HANDSHAKE_FAILURE, "Couldn't kickstart handshaking", ioe);
                }
                catch (Exception oe) {
                    this.handleException(oe);
                }
            }
            finally {
                this.handshakeLock.unlock();
            }
        }
    }

    @Override
    public void setUseClientMode(boolean mode) {
        this.socketLock.lock();
        try {
            this.conContext.setUseClientMode(mode);
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public boolean getUseClientMode() {
        this.socketLock.lock();
        try {
            boolean bl = this.conContext.sslConfig.isClientMode;
            return bl;
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public void setNeedClientAuth(boolean need) {
        this.socketLock.lock();
        try {
            this.conContext.sslConfig.clientAuthType = need ? ClientAuthType.CLIENT_AUTH_REQUIRED : ClientAuthType.CLIENT_AUTH_NONE;
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public boolean getNeedClientAuth() {
        this.socketLock.lock();
        try {
            boolean bl = this.conContext.sslConfig.clientAuthType == ClientAuthType.CLIENT_AUTH_REQUIRED;
            return bl;
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public void setWantClientAuth(boolean want) {
        this.socketLock.lock();
        try {
            this.conContext.sslConfig.clientAuthType = want ? ClientAuthType.CLIENT_AUTH_REQUESTED : ClientAuthType.CLIENT_AUTH_NONE;
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public boolean getWantClientAuth() {
        this.socketLock.lock();
        try {
            boolean bl = this.conContext.sslConfig.clientAuthType == ClientAuthType.CLIENT_AUTH_REQUESTED;
            return bl;
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public void setEnableSessionCreation(boolean flag) {
        this.socketLock.lock();
        try {
            this.conContext.sslConfig.enableSessionCreation = flag;
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public boolean getEnableSessionCreation() {
        this.socketLock.lock();
        try {
            boolean bl = this.conContext.sslConfig.enableSessionCreation;
            return bl;
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public boolean isClosed() {
        return this.tlsIsClosed;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void close() throws IOException {
        if (this.isClosed()) {
            return;
        }
        if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
            SSLLogger.fine("duplex close of SSLSocket", new Object[0]);
        }
        if (this.isConnected()) {
            if (!this.isOutputShutdown()) {
                this.duplexCloseOutput();
            }
            if (!this.isInputShutdown()) {
                this.duplexCloseInput();
            }
        }
        if (this.isClosed()) return;
        try {
            this.closeSocket(false);
            return;
        }
        catch (IOException ioe) {
            if (!SSLLogger.isOn || !SSLLogger.isOn("ssl")) return;
            SSLLogger.warning("SSLSocket close failed. Debug info only. Exception details:", ioe);
            return;
        }
        finally {
            this.tlsIsClosed = true;
        }
        catch (IOException ioe) {
            try {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.warning("SSLSocket duplex close failed. Debug info only. Exception details:", ioe);
                }
                if (this.isClosed()) return;
            }
            catch (Throwable throwable) {
                if (this.isClosed()) throw throwable;
                try {
                    this.closeSocket(false);
                    throw throwable;
                }
                catch (IOException ioe2) {
                    if (!SSLLogger.isOn || !SSLLogger.isOn("ssl")) throw throwable;
                    SSLLogger.warning("SSLSocket close failed. Debug info only. Exception details:", ioe2);
                    throw throwable;
                }
                finally {
                    this.tlsIsClosed = true;
                }
            }
            try {
                this.closeSocket(false);
                return;
            }
            catch (IOException ioe3) {
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl")) return;
                SSLLogger.warning("SSLSocket close failed. Debug info only. Exception details:", ioe3);
                return;
            }
            finally {
                this.tlsIsClosed = true;
            }
        }
    }

    private void duplexCloseOutput() throws IOException {
        boolean useUserCanceled = false;
        boolean hasCloseReceipt = false;
        if (this.conContext.isNegotiated) {
            if (!this.conContext.protocolVersion.useTLS13PlusSpec()) {
                hasCloseReceipt = true;
            } else if (!this.conContext.isInboundClosed()) {
                useUserCanceled = true;
            }
        } else if (this.conContext.handshakeContext != null) {
            useUserCanceled = true;
            ProtocolVersion pv = this.conContext.handshakeContext.negotiatedProtocol;
            if (pv == null || !pv.useTLS13PlusSpec()) {
                hasCloseReceipt = true;
            }
        }
        this.closeNotify(useUserCanceled);
        if (!this.isInputShutdown()) {
            this.bruteForceCloseInput(hasCloseReceipt);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void closeNotify(boolean useUserCanceled) throws IOException {
        int linger = this.getSoLinger();
        if (linger >= 0) {
            boolean interrupted;
            block16: {
                interrupted = Thread.interrupted();
                try {
                    if (this.conContext.outputRecord.recordLock.tryLock() || this.conContext.outputRecord.recordLock.tryLock(linger, TimeUnit.SECONDS)) {
                        try {
                            this.deliverClosedNotify(useUserCanceled);
                            break block16;
                        }
                        finally {
                            this.conContext.outputRecord.recordLock.unlock();
                        }
                    }
                    if (!super.isOutputShutdown()) {
                        if (this.isLayered() && !this.autoClose) {
                            throw new SSLException("SO_LINGER timeout, close_notify message cannot be sent.");
                        }
                        super.shutdownOutput();
                        if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                            SSLLogger.warning("SSLSocket output duplex close failed: SO_LINGER timeout, close_notify message cannot be sent.", new Object[0]);
                        }
                    }
                    this.conContext.conSession.invalidate();
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                        SSLLogger.warning("Invalidate the session: SO_LINGER timeout, close_notify message cannot be sent.", new Object[0]);
                    }
                }
                catch (InterruptedException ex) {
                    interrupted = true;
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        } else {
            this.conContext.outputRecord.recordLock.lock();
            try {
                this.deliverClosedNotify(useUserCanceled);
            }
            finally {
                this.conContext.outputRecord.recordLock.unlock();
            }
        }
    }

    private void deliverClosedNotify(boolean useUserCanceled) throws IOException {
        try {
            if (useUserCanceled) {
                this.conContext.warning(Alert.USER_CANCELED);
            }
            this.conContext.warning(Alert.CLOSE_NOTIFY);
        }
        finally {
            if (!this.conContext.isOutboundClosed()) {
                this.conContext.outputRecord.close();
            }
            if (!(super.isOutputShutdown() || !this.autoClose && this.isLayered())) {
                super.shutdownOutput();
            }
        }
    }

    private void duplexCloseInput() throws IOException {
        boolean hasCloseReceipt = this.conContext.isNegotiated && !this.conContext.protocolVersion.useTLS13PlusSpec();
        this.bruteForceCloseInput(hasCloseReceipt);
    }

    private void bruteForceCloseInput(boolean hasCloseReceipt) throws IOException {
        if (hasCloseReceipt) {
            try {
                this.shutdown();
            }
            finally {
                if (!this.isInputShutdown()) {
                    this.shutdownInput(false);
                }
            }
        }
        if (!this.conContext.isInboundClosed()) {
            try (InputRecord inputRecord = this.conContext.inputRecord;){
                this.appInput.deplete();
            }
        }
        if (!(!this.autoClose && this.isLayered() || super.isInputShutdown())) {
            super.shutdownInput();
        }
    }

    @Override
    public void shutdownInput() throws IOException {
        this.shutdownInput(true);
    }

    private void shutdownInput(boolean checkCloseNotify) throws IOException {
        if (this.isInputShutdown()) {
            return;
        }
        if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
            SSLLogger.fine("close inbound of SSLSocket", new Object[0]);
        }
        try {
            if (checkCloseNotify && !this.conContext.isInputCloseNotified && (this.conContext.isNegotiated || this.conContext.handshakeContext != null)) {
                throw new SSLException("closing inbound before receiving peer's close_notify");
            }
        }
        finally {
            this.conContext.closeInbound();
            if (!(!this.autoClose && this.isLayered() || super.isInputShutdown())) {
                super.shutdownInput();
            }
        }
    }

    @Override
    public boolean isInputShutdown() {
        return this.conContext.isInboundClosed() && (!this.autoClose && this.isLayered() || super.isInputShutdown());
    }

    @Override
    public void shutdownOutput() throws IOException {
        if (this.isOutputShutdown()) {
            return;
        }
        if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
            SSLLogger.fine("close outbound of SSLSocket", new Object[0]);
        }
        this.conContext.closeOutbound();
        if (!(!this.autoClose && this.isLayered() || super.isOutputShutdown())) {
            super.shutdownOutput();
        }
    }

    @Override
    public boolean isOutputShutdown() {
        return this.conContext.isOutboundClosed() && (!this.autoClose && this.isLayered() || super.isOutputShutdown());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        this.socketLock.lock();
        try {
            if (this.isClosed()) {
                throw new SocketException("Socket is closed");
            }
            if (!this.isConnected) {
                throw new SocketException("Socket is not connected");
            }
            if (this.conContext.isInboundClosed() || this.isInputShutdown()) {
                throw new SocketException("Socket input is already shutdown");
            }
            AppInputStream appInputStream = this.appInput;
            return appInputStream;
        }
        finally {
            this.socketLock.unlock();
        }
    }

    private void ensureNegotiated(boolean resumable) throws IOException {
        if (this.conContext.isNegotiated || this.conContext.isBroken || this.conContext.isInboundClosed() || this.conContext.isOutboundClosed()) {
            return;
        }
        this.handshakeLock.lock();
        try {
            if (this.conContext.isNegotiated || this.conContext.isBroken || this.conContext.isInboundClosed() || this.conContext.isOutboundClosed()) {
                return;
            }
            this.startHandshake(resumable);
        }
        finally {
            this.handshakeLock.unlock();
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        this.socketLock.lock();
        try {
            if (this.isClosed()) {
                throw new SocketException("Socket is closed");
            }
            if (!this.isConnected) {
                throw new SocketException("Socket is not connected");
            }
            if (this.conContext.isOutboundDone() || this.isOutputShutdown()) {
                throw new SocketException("Socket output is already shutdown");
            }
            AppOutputStream appOutputStream = this.appOutput;
            return appOutputStream;
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public SSLParameters getSSLParameters() {
        this.socketLock.lock();
        try {
            SSLParameters sSLParameters = this.conContext.sslConfig.getSSLParameters();
            return sSLParameters;
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public void setSSLParameters(SSLParameters params) {
        this.socketLock.lock();
        try {
            this.conContext.sslConfig.setSSLParameters(params);
            if (this.conContext.sslConfig.maximumPacketSize != 0) {
                this.conContext.outputRecord.changePacketSize(this.conContext.sslConfig.maximumPacketSize);
            }
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public String getApplicationProtocol() {
        this.socketLock.lock();
        try {
            String string = this.conContext.applicationProtocol;
            return string;
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public String getHandshakeApplicationProtocol() {
        this.socketLock.lock();
        try {
            if (this.conContext.handshakeContext != null) {
                String string = this.conContext.handshakeContext.applicationProtocol;
                return string;
            }
        }
        finally {
            this.socketLock.unlock();
        }
        return null;
    }

    @Override
    public void setHandshakeApplicationProtocolSelector(BiFunction<SSLSocket, List<String>, String> selector) {
        this.socketLock.lock();
        try {
            this.conContext.sslConfig.socketAPSelector = selector;
        }
        finally {
            this.socketLock.unlock();
        }
    }

    @Override
    public BiFunction<SSLSocket, List<String>, String> getHandshakeApplicationProtocolSelector() {
        this.socketLock.lock();
        try {
            BiFunction<SSLSocket, List<String>, String> biFunction = this.conContext.sslConfig.socketAPSelector;
            return biFunction;
        }
        finally {
            this.socketLock.unlock();
        }
    }

    private int readHandshakeRecord() throws IOException {
        while (!this.conContext.isInboundClosed()) {
            try {
                Plaintext plainText = this.decode(null);
                if (plainText.contentType != ContentType.HANDSHAKE.id || !this.conContext.isNegotiated) continue;
                return 0;
            }
            catch (InterruptedIOException | SocketException | SSLException se) {
                throw se;
            }
            catch (IOException ioe) {
                throw new SSLException("readHandshakeRecord", ioe);
            }
        }
        return -1;
    }

    private ByteBuffer readApplicationRecord(ByteBuffer buffer) throws IOException {
        while (!this.conContext.isInboundClosed()) {
            buffer.clear();
            int inLen = this.conContext.inputRecord.bytesInCompletePacket();
            if (inLen < 0) {
                this.handleEOF(null);
                return null;
            }
            if (inLen > 33093) {
                throw new SSLProtocolException("Illegal packet size: " + inLen);
            }
            if (inLen > buffer.remaining()) {
                buffer = ByteBuffer.allocate(inLen);
            }
            try {
                Plaintext plainText = this.decode(buffer);
                if (plainText.contentType != ContentType.APPLICATION_DATA.id || buffer.position() <= 0) continue;
                return buffer;
            }
            catch (InterruptedIOException | SocketException | SSLException se) {
                throw se;
            }
            catch (IOException ioe) {
                throw new SSLException("readApplicationRecord", ioe);
            }
        }
        return null;
    }

    private Plaintext decode(ByteBuffer destination) throws IOException {
        Plaintext plainText;
        try {
            plainText = destination == null ? SSLTransport.decode(this.conContext, null, 0, 0, null, 0, 0) : SSLTransport.decode(this.conContext, null, 0, 0, new ByteBuffer[]{destination}, 0, 1);
        }
        catch (EOFException eofe) {
            plainText = this.handleEOF(eofe);
        }
        if (plainText != Plaintext.PLAINTEXT_NULL && (this.conContext.inputRecord.seqNumIsHuge() || this.conContext.inputRecord.readCipher.atKeyLimit())) {
            this.tryKeyUpdate();
        }
        return plainText;
    }

    private void tryKeyUpdate() throws IOException {
        if (this.conContext.handshakeContext == null && !this.conContext.isOutboundClosed() && !this.conContext.isBroken) {
            if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                SSLLogger.finest("trigger key update", new Object[0]);
            }
            this.startHandshake();
        }
    }

    private void tryNewSessionTicket() throws IOException {
        if (!(this.conContext.sslConfig.isClientMode || !this.conContext.protocolVersion.useTLS13PlusSpec() || this.conContext.handshakeContext != null || this.conContext.isOutboundClosed() || this.conContext.isInboundClosed() || this.conContext.isBroken)) {
            if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                SSLLogger.finest("trigger new session ticket", new Object[0]);
            }
            NewSessionTicket.t13PosthandshakeProducer.produce(new PostHandshakeContext(this.conContext));
        }
    }

    void doneConnect() throws IOException {
        this.socketLock.lock();
        try {
            if (this.peerHost == null || this.peerHost.isEmpty()) {
                boolean useNameService = trustNameService && this.conContext.sslConfig.isClientMode;
                this.useImplicitHost(useNameService);
            } else {
                this.conContext.sslConfig.serverNames = Utilities.addToSNIServerNameList(this.conContext.sslConfig.serverNames, this.peerHost);
            }
            InputStream sockInput = super.getInputStream();
            this.conContext.inputRecord.setReceiverStream(sockInput);
            OutputStream sockOutput = super.getOutputStream();
            this.conContext.inputRecord.setDeliverStream(sockOutput);
            this.conContext.outputRecord.setDeliverStream(sockOutput);
            this.isConnected = true;
        }
        finally {
            this.socketLock.unlock();
        }
    }

    private void useImplicitHost(boolean useNameService) {
        InetAddress inetAddress = this.getInetAddress();
        if (inetAddress == null) {
            return;
        }
        JavaNetInetAddressAccess jna = SharedSecrets.getJavaNetInetAddressAccess();
        String originalHostname = jna.getOriginalHostName(inetAddress);
        if (originalHostname != null && !originalHostname.isEmpty()) {
            this.peerHost = originalHostname;
            if (this.conContext.sslConfig.serverNames.isEmpty() && !this.conContext.sslConfig.noSniExtension) {
                this.conContext.sslConfig.serverNames = Utilities.addToSNIServerNameList(this.conContext.sslConfig.serverNames, this.peerHost);
            }
            return;
        }
        this.peerHost = !useNameService ? inetAddress.getHostAddress() : this.getInetAddress().getHostName();
    }

    public void setHost(String host) {
        this.socketLock.lock();
        try {
            this.peerHost = host;
            this.conContext.sslConfig.serverNames = Utilities.addToSNIServerNameList(this.conContext.sslConfig.serverNames, host);
        }
        finally {
            this.socketLock.unlock();
        }
    }

    private void handleException(Exception cause) throws IOException {
        if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
            SSLLogger.warning("handling exception", cause);
        }
        if (cause instanceof InterruptedIOException) {
            throw (IOException)cause;
        }
        boolean isSSLException = cause instanceof SSLException;
        Alert alert = isSSLException ? (cause instanceof SSLHandshakeException ? Alert.HANDSHAKE_FAILURE : Alert.UNEXPECTED_MESSAGE) : (cause instanceof IOException ? Alert.UNEXPECTED_MESSAGE : Alert.INTERNAL_ERROR);
        if (cause instanceof SocketException) {
            try {
                throw this.conContext.fatal(alert, cause);
            }
            catch (Exception exception) {
                throw (SocketException)cause;
            }
        }
        throw this.conContext.fatal(alert, cause);
    }

    private Plaintext handleEOF(EOFException eofe) throws IOException {
        if (requireCloseNotify || this.conContext.handshakeContext != null) {
            if (this.conContext.handshakeContext != null) {
                throw new SSLHandshakeException("Remote host terminated the handshake", eofe);
            }
            throw new SSLProtocolException("Remote host terminated the connection", eofe);
        }
        this.conContext.isInputCloseNotified = true;
        this.shutdownInput();
        return Plaintext.PLAINTEXT_NULL;
    }

    @Override
    public String getPeerHost() {
        return this.peerHost;
    }

    @Override
    public int getPeerPort() {
        return this.getPort();
    }

    @Override
    public boolean useDelegatedTask() {
        return false;
    }

    @Override
    public void shutdown() throws IOException {
        if (!this.isClosed()) {
            if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                SSLLogger.fine("close the underlying socket", new Object[0]);
            }
            try {
                this.closeSocket(this.conContext.isNegotiated && !this.conContext.isInputCloseNotified);
            }
            finally {
                this.tlsIsClosed = true;
            }
        }
    }

    @Override
    public String toString() {
        return "SSLSocket[hostname=" + this.getPeerHost() + ", port=" + this.getPeerPort() + ", " + this.conContext.conSession + "]";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void closeSocket(boolean selfInitiated) throws IOException {
        block13: {
            if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                SSLLogger.fine("close the SSL connection " + (selfInitiated ? "(initiative)" : "(passive)"), new Object[0]);
            }
            if (!this.autoClose && this.isLayered()) {
                if (!selfInitiated) return;
                if (this.conContext.isInboundClosed()) return;
                if (this.isInputShutdown()) return;
                this.waitForClose();
                return;
            }
            InputRecord inputRecord = this.conContext.inputRecord;
            if (inputRecord instanceof SSLSocketInputRecord) {
                SSLSocketInputRecord inputRecord2 = (SSLSocketInputRecord)inputRecord;
                if (this.isConnected && this.appInput.readLock.tryLock()) {
                    try {
                        int soTimeout = this.getSoTimeout();
                        try {
                            if (soTimeout == 0) {
                                this.setSoTimeout(1);
                            }
                            inputRecord2.deplete(false);
                            if (soTimeout != 0) break block13;
                        }
                        catch (SocketTimeoutException socketTimeoutException) {
                            if (soTimeout == 0) {
                                this.setSoTimeout(soTimeout);
                            }
                            break block13;
                            catch (Throwable throwable) {
                                if (soTimeout != 0) throw throwable;
                                this.setSoTimeout(soTimeout);
                                throw throwable;
                            }
                        }
                        this.setSoTimeout(soTimeout);
                    }
                    finally {
                        this.appInput.readLock.unlock();
                    }
                }
            }
        }
        super.close();
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void waitForClose() throws IOException {
        if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
            SSLLogger.fine("wait for close_notify or alert", new Object[0]);
        }
        this.appInput.readLock.lock();
        try {
            while (!this.conContext.isInboundClosed()) {
                try {
                    Plaintext plainText = this.decode(null);
                    if (!SSLLogger.isOn || !SSLLogger.isOn("ssl")) continue;
                    SSLLogger.finest("discard plaintext while waiting for close", plainText);
                }
                catch (Exception e) {
                    this.handleException(e);
                }
            }
            return;
        }
        finally {
            this.appInput.readLock.unlock();
        }
    }

    private class AppInputStream
    extends InputStream {
        private final byte[] oneByte = new byte[1];
        private ByteBuffer buffer;
        private volatile boolean appDataIsAvailable = false;
        private final ReentrantLock readLock = new ReentrantLock();
        private volatile boolean isClosing;
        private volatile boolean hasDepleted;

        AppInputStream() {
            this.buffer = ByteBuffer.allocate(4096);
        }

        @Override
        public int available() throws IOException {
            if (!this.appDataIsAvailable || this.checkEOF()) {
                return 0;
            }
            return this.buffer.remaining();
        }

        @Override
        public int read() throws IOException {
            int n = this.read(this.oneByte, 0, 1);
            if (n <= 0) {
                return -1;
            }
            return this.oneByte[0] & 0xFF;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException("the target buffer is null");
            }
            if (off < 0 || len < 0 || len > b.length - off) {
                throw new IndexOutOfBoundsException("buffer length: " + b.length + ", offset; " + off + ", bytes to read:" + len);
            }
            if (len == 0) {
                return 0;
            }
            if (this.checkEOF()) {
                return -1;
            }
            if (!(SSLSocketImpl.this.conContext.isNegotiated || SSLSocketImpl.this.conContext.isBroken || SSLSocketImpl.this.conContext.isInboundClosed() || SSLSocketImpl.this.conContext.isOutboundClosed())) {
                SSLSocketImpl.this.ensureNegotiated(true);
            }
            if (!SSLSocketImpl.this.conContext.isNegotiated || SSLSocketImpl.this.conContext.isBroken || SSLSocketImpl.this.conContext.isInboundClosed()) {
                throw new SocketException("Connection or inbound has closed");
            }
            if (this.hasDepleted) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.fine("The input stream has been depleted", new Object[0]);
                }
                return -1;
            }
            this.readLock.lock();
            try {
                ByteBuffer bb;
                if (SSLSocketImpl.this.conContext.isBroken || SSLSocketImpl.this.conContext.isInboundClosed()) {
                    throw new SocketException("Connection or inbound has closed");
                }
                if (this.hasDepleted) {
                    if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                        SSLLogger.fine("The input stream is closing", new Object[0]);
                    }
                    int n = -1;
                    return n;
                }
                int remains = this.available();
                if (remains > 0) {
                    int howmany = Math.min(remains, len);
                    this.buffer.get(b, off, howmany);
                    int n = howmany;
                    return n;
                }
                this.appDataIsAvailable = false;
                try {
                    bb = SSLSocketImpl.this.readApplicationRecord(this.buffer);
                    if (bb == null) {
                        int n = -1;
                        return n;
                    }
                    this.buffer = bb;
                    bb.flip();
                }
                catch (Exception e) {
                    SSLSocketImpl.this.handleException(e);
                    int n = -1;
                    return n;
                }
                int volume = Math.min(len, bb.remaining());
                this.buffer.get(b, off, volume);
                this.appDataIsAvailable = true;
                int n = volume;
                return n;
            }
            finally {
                try {
                    if (this.isClosing) {
                        this.readLockedDeplete();
                    }
                }
                finally {
                    this.readLock.unlock();
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public long skip(long n) throws IOException {
            byte[] skipArray = new byte[256];
            long skipped = 0L;
            this.readLock.lock();
            try {
                while (n > 0L) {
                    int len = (int)Math.min(n, (long)skipArray.length);
                    int r = this.read(skipArray, 0, len);
                    if (r <= 0) {
                        break;
                    }
                    n -= (long)r;
                    skipped += (long)r;
                }
            }
            finally {
                this.readLock.unlock();
            }
            return skipped;
        }

        @Override
        public void close() throws IOException {
            block3: {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.finest("Closing input stream", new Object[0]);
                }
                try {
                    SSLSocketImpl.this.close();
                }
                catch (IOException ioe) {
                    if (!SSLLogger.isOn || !SSLLogger.isOn("ssl")) break block3;
                    SSLLogger.warning("input stream close failed. Debug info only. Exception details:", ioe);
                }
            }
        }

        private boolean checkEOF() throws IOException {
            if (SSLSocketImpl.this.conContext.isBroken) {
                if (SSLSocketImpl.this.conContext.closeReason == null) {
                    return true;
                }
                throw new SSLException("Connection has closed: " + SSLSocketImpl.this.conContext.closeReason, SSLSocketImpl.this.conContext.closeReason);
            }
            if (SSLSocketImpl.this.conContext.isInboundClosed()) {
                return true;
            }
            if (SSLSocketImpl.this.conContext.isInputCloseNotified) {
                if (SSLSocketImpl.this.conContext.closeReason == null) {
                    return true;
                }
                throw new SSLException("Connection has closed: " + SSLSocketImpl.this.conContext.closeReason, SSLSocketImpl.this.conContext.closeReason);
            }
            return false;
        }

        private void deplete() {
            if (SSLSocketImpl.this.conContext.isInboundClosed() || this.isClosing) {
                return;
            }
            this.isClosing = true;
            if (this.readLock.tryLock()) {
                try {
                    this.readLockedDeplete();
                }
                finally {
                    this.readLock.unlock();
                }
            }
        }

        private void readLockedDeplete() {
            if (this.hasDepleted || SSLSocketImpl.this.conContext.isInboundClosed()) {
                return;
            }
            InputRecord inputRecord = SSLSocketImpl.this.conContext.inputRecord;
            if (!(inputRecord instanceof SSLSocketInputRecord)) {
                return;
            }
            SSLSocketInputRecord socketInputRecord = (SSLSocketInputRecord)inputRecord;
            try {
                socketInputRecord.deplete(SSLSocketImpl.this.conContext.isNegotiated && SSLSocketImpl.this.getSoTimeout() > 0);
            }
            catch (Exception ex) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.warning("input stream close depletion failed", ex);
                }
            }
            finally {
                this.hasDepleted = true;
            }
        }
    }

    private class AppOutputStream
    extends OutputStream {
        private final byte[] oneByte = new byte[1];

        private AppOutputStream() {
        }

        @Override
        public void write(int i) throws IOException {
            this.oneByte[0] = (byte)i;
            this.write(this.oneByte, 0, 1);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException("the source buffer is null");
            }
            if (off < 0 || len < 0 || len > b.length - off) {
                throw new IndexOutOfBoundsException("buffer length: " + b.length + ", offset; " + off + ", bytes to read:" + len);
            }
            if (len == 0) {
                return;
            }
            if (!(SSLSocketImpl.this.conContext.isNegotiated || SSLSocketImpl.this.conContext.isBroken || SSLSocketImpl.this.conContext.isInboundClosed() || SSLSocketImpl.this.conContext.isOutboundClosed())) {
                SSLSocketImpl.this.ensureNegotiated(true);
            }
            if (!SSLSocketImpl.this.conContext.isNegotiated || SSLSocketImpl.this.conContext.isBroken || SSLSocketImpl.this.conContext.isOutboundClosed()) {
                throw new SocketException("Connection or outbound has closed");
            }
            try {
                SSLSocketImpl.this.conContext.outputRecord.deliver(b, off, len);
            }
            catch (SSLHandshakeException she) {
                throw SSLSocketImpl.this.conContext.fatal(Alert.HANDSHAKE_FAILURE, she);
            }
            catch (SSLException ssle) {
                throw SSLSocketImpl.this.conContext.fatal(Alert.UNEXPECTED_MESSAGE, ssle);
            }
            if (SSLSocketImpl.this.conContext.outputRecord.seqNumIsHuge() || SSLSocketImpl.this.conContext.outputRecord.writeCipher.atKeyLimit()) {
                SSLSocketImpl.this.tryKeyUpdate();
            }
            if (SSLSocketImpl.this.conContext.conSession.updateNST) {
                SSLSocketImpl.this.conContext.conSession.updateNST = false;
                SSLSocketImpl.this.tryNewSessionTicket();
            }
        }

        @Override
        public void close() throws IOException {
            block3: {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.finest("Closing output stream", new Object[0]);
                }
                try {
                    SSLSocketImpl.this.close();
                }
                catch (IOException ioe) {
                    if (!SSLLogger.isOn || !SSLLogger.isOn("ssl")) break block3;
                    SSLLogger.warning("output stream close failed. Debug info only. Exception details:", ioe);
                }
            }
        }
    }
}

