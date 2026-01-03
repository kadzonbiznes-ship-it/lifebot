/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.net.SocketException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import sun.security.ssl.Alert;
import sun.security.ssl.CipherSuite;
import sun.security.ssl.ClientHandshakeContext;
import sun.security.ssl.ConnectionContext;
import sun.security.ssl.ContentType;
import sun.security.ssl.DTLSInputRecord;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.InputRecord;
import sun.security.ssl.NamedGroup;
import sun.security.ssl.OutputRecord;
import sun.security.ssl.Plaintext;
import sun.security.ssl.PostHandshakeContext;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.SSLConfiguration;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLContextImpl;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLSessionImpl;
import sun.security.ssl.SSLSocketImpl;
import sun.security.ssl.SSLTransport;
import sun.security.ssl.ServerHandshakeContext;

final class TransportContext
implements ConnectionContext {
    final SSLTransport transport;
    final Map<Byte, SSLConsumer> consumers;
    final AccessControlContext acc;
    final SSLContextImpl sslContext;
    final SSLConfiguration sslConfig;
    final InputRecord inputRecord;
    final OutputRecord outputRecord;
    boolean isUnsureMode;
    boolean isNegotiated = false;
    boolean isBroken = false;
    boolean isInputCloseNotified = false;
    boolean peerUserCanceled = false;
    Exception closeReason = null;
    Exception delegatedThrown = null;
    boolean needHandshakeFinishedStatus = false;
    boolean hasDelegatedFinished = false;
    SSLSessionImpl conSession;
    ProtocolVersion protocolVersion;
    String applicationProtocol = null;
    HandshakeContext handshakeContext = null;
    boolean secureRenegotiation = false;
    byte[] clientVerifyData;
    byte[] serverVerifyData;
    List<NamedGroup> serverRequestedNamedGroups;
    CipherSuite cipherSuite;
    private static final byte[] emptyByteArray = new byte[0];

    TransportContext(SSLContextImpl sslContext, SSLTransport transport, InputRecord inputRecord, OutputRecord outputRecord) {
        this(sslContext, transport, new SSLConfiguration(sslContext, false), inputRecord, outputRecord, true);
    }

    TransportContext(SSLContextImpl sslContext, SSLTransport transport, InputRecord inputRecord, OutputRecord outputRecord, boolean isClientMode) {
        this(sslContext, transport, new SSLConfiguration(sslContext, isClientMode), inputRecord, outputRecord, false);
    }

    TransportContext(SSLContextImpl sslContext, SSLTransport transport, SSLConfiguration sslConfig, InputRecord inputRecord, OutputRecord outputRecord) {
        this(sslContext, transport, (SSLConfiguration)sslConfig.clone(), inputRecord, outputRecord, false);
    }

    private TransportContext(SSLContextImpl sslContext, SSLTransport transport, SSLConfiguration sslConfig, InputRecord inputRecord, OutputRecord outputRecord, boolean isUnsureMode) {
        this.transport = transport;
        this.sslContext = sslContext;
        this.inputRecord = inputRecord;
        this.outputRecord = outputRecord;
        this.sslConfig = sslConfig;
        if (this.sslConfig.maximumPacketSize == 0) {
            this.sslConfig.maximumPacketSize = outputRecord.getMaxPacketSize();
        }
        this.isUnsureMode = isUnsureMode;
        this.conSession = new SSLSessionImpl();
        this.protocolVersion = this.sslConfig.maximumProtocolVersion;
        this.clientVerifyData = emptyByteArray;
        this.serverVerifyData = emptyByteArray;
        this.acc = AccessController.getContext();
        this.consumers = new HashMap<Byte, SSLConsumer>();
        if (inputRecord instanceof DTLSInputRecord) {
            DTLSInputRecord dtlsInputRecord = (DTLSInputRecord)inputRecord;
            dtlsInputRecord.setTransportContext(this);
            dtlsInputRecord.setSSLContext(this.sslContext);
        }
    }

    void dispatch(Plaintext plaintext) throws IOException {
        if (plaintext == null) {
            return;
        }
        ContentType ct = ContentType.valueOf(plaintext.contentType);
        if (ct == null) {
            throw this.fatal(Alert.UNEXPECTED_MESSAGE, "Unknown content type: " + plaintext.contentType);
        }
        switch (ct) {
            case HANDSHAKE: {
                byte type = HandshakeContext.getHandshakeType(this, plaintext);
                if (this.handshakeContext == null) {
                    if (type == SSLHandshake.KEY_UPDATE.id || type == SSLHandshake.NEW_SESSION_TICKET.id) {
                        if (!this.isNegotiated) {
                            throw this.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected unnegotiated post-handshake message: " + SSLHandshake.nameOf(type));
                        }
                        if (!PostHandshakeContext.isConsumable(this, type)) {
                            throw this.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected post-handshake message: " + SSLHandshake.nameOf(type));
                        }
                        this.handshakeContext = new PostHandshakeContext(this);
                    } else {
                        this.handshakeContext = this.sslConfig.isClientMode ? new ClientHandshakeContext(this.sslContext, this) : new ServerHandshakeContext(this.sslContext, this);
                        this.outputRecord.initHandshaker();
                    }
                }
                this.handshakeContext.dispatch(type, plaintext);
                break;
            }
            case ALERT: {
                Alert.alertConsumer.consume(this, plaintext.fragment);
                break;
            }
            default: {
                SSLConsumer consumer = this.consumers.get(plaintext.contentType);
                if (consumer != null) {
                    consumer.consume(this, plaintext.fragment);
                    break;
                }
                throw this.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected content: " + plaintext.contentType);
            }
        }
    }

    void kickstart() throws IOException {
        boolean isNotUsable;
        if (this.isUnsureMode) {
            throw new IllegalStateException("Client/Server mode not yet set.");
        }
        boolean bl = this.outputRecord.writeCipher.atKeyLimit() ? this.outputRecord.isClosed() || this.isBroken : (isNotUsable = this.outputRecord.isClosed() || this.inputRecord.isClosed() || this.isBroken);
        if (isNotUsable) {
            if (this.closeReason != null) {
                throw new SSLException("Cannot kickstart, the connection is broken or closed", this.closeReason);
            }
            throw new SSLException("Cannot kickstart, the connection is broken or closed");
        }
        if (this.handshakeContext == null) {
            if (this.isNegotiated && this.protocolVersion.useTLS13PlusSpec()) {
                this.handshakeContext = new PostHandshakeContext(this);
            } else {
                this.handshakeContext = this.sslConfig.isClientMode ? new ClientHandshakeContext(this.sslContext, this) : new ServerHandshakeContext(this.sslContext, this);
                this.outputRecord.initHandshaker();
            }
        }
        if (this.isNegotiated || this.sslConfig.isClientMode) {
            this.handshakeContext.kickstart();
        }
    }

    boolean isPostHandshakeContext() {
        return this.handshakeContext != null && this.handshakeContext instanceof PostHandshakeContext;
    }

    void warning(Alert alert) {
        block3: {
            if (this.isNegotiated || this.handshakeContext != null) {
                try {
                    this.outputRecord.encodeAlert(Alert.Level.WARNING.level, alert.id);
                }
                catch (IOException ioe) {
                    if (!SSLLogger.isOn || !SSLLogger.isOn("ssl")) break block3;
                    SSLLogger.warning("Warning: failed to send warning alert " + (Object)((Object)alert), ioe);
                }
            }
        }
    }

    void closeNotify(boolean isUserCanceled) throws IOException {
        if (this.transport instanceof SSLSocketImpl) {
            ((SSLSocketImpl)this.transport).closeNotify(isUserCanceled);
        } else {
            this.outputRecord.recordLock.lock();
            try {
                try {
                    if (isUserCanceled) {
                        this.warning(Alert.USER_CANCELED);
                    }
                    this.warning(Alert.CLOSE_NOTIFY);
                }
                finally {
                    this.outputRecord.close();
                }
            }
            finally {
                this.outputRecord.recordLock.unlock();
            }
        }
    }

    SSLException fatal(Alert alert, String diagnostic) throws SSLException {
        return this.fatal(alert, diagnostic, null);
    }

    SSLException fatal(Alert alert, Throwable cause) throws SSLException {
        return this.fatal(alert, null, cause);
    }

    SSLException fatal(Alert alert, String diagnostic, Throwable cause) throws SSLException {
        return this.fatal(alert, diagnostic, false, cause);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    SSLException fatal(Alert alert, String diagnostic, boolean recvFatalAlert, Throwable cause) throws SSLException {
        if (this.closeReason != null) {
            if (cause == null) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.warning("Closed transport, general or untracked problem", new Object[0]);
                }
                throw alert.createSSLException("Closed transport, general or untracked problem");
            }
            if (cause instanceof SSLException) {
                throw (SSLException)cause;
            }
            if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                SSLLogger.warning("Closed transport, unexpected rethrowing", cause);
            }
            throw alert.createSSLException("Unexpected rethrowing", cause);
        }
        if (diagnostic == null) {
            diagnostic = cause == null ? "General/Untracked problem" : cause.getMessage();
        }
        if (cause == null) {
            cause = alert.createSSLException(diagnostic);
        }
        if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
            SSLLogger.severe("Fatal (" + (Object)((Object)alert) + "): " + diagnostic, cause);
        }
        this.closeReason = cause instanceof SSLException ? (SSLException)cause : alert.createSSLException(diagnostic, cause);
        try {
            this.inputRecord.close();
        }
        catch (IOException ioe) {
            if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                SSLLogger.warning("Fatal: input record closure failed", ioe);
            }
            this.closeReason.addSuppressed(ioe);
        }
        if (this.conSession != null && !(cause instanceof SocketException)) {
            this.conSession.invalidate();
        }
        if (this.handshakeContext != null && this.handshakeContext.handshakeSession != null) {
            this.handshakeContext.handshakeSession.invalidate();
        }
        if (!(recvFatalAlert || this.isOutboundClosed() || this.isBroken || !this.isNegotiated && this.handshakeContext == null)) {
            try {
                this.outputRecord.encodeAlert(Alert.Level.FATAL.level, alert.id);
            }
            catch (IOException ioe) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.warning("Fatal: failed to send fatal alert " + (Object)((Object)alert), ioe);
                }
                this.closeReason.addSuppressed(ioe);
            }
        }
        try {
            this.outputRecord.close();
        }
        catch (IOException ioe) {
            if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                SSLLogger.warning("Fatal: output record closure failed", ioe);
            }
            this.closeReason.addSuppressed(ioe);
        }
        if (this.handshakeContext != null) {
            this.handshakeContext = null;
        }
        try {
            this.transport.shutdown();
        }
        catch (IOException ioe) {
            if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                SSLLogger.warning("Fatal: transport closure failed", ioe);
            }
            this.closeReason.addSuppressed(ioe);
        }
        finally {
            this.isBroken = true;
        }
        if (this.closeReason instanceof SSLException) {
            throw (SSLException)this.closeReason;
        }
        throw (RuntimeException)this.closeReason;
    }

    void setUseClientMode(boolean useClientMode) {
        if (this.handshakeContext != null || this.isNegotiated) {
            throw new IllegalArgumentException("Cannot change mode after SSL traffic has started");
        }
        if (this.sslConfig.isClientMode != useClientMode) {
            if (this.sslContext.isDefaultProtocolVesions(this.sslConfig.enabledProtocols)) {
                this.sslConfig.enabledProtocols = this.sslContext.getDefaultProtocolVersions(!useClientMode);
            }
            if (this.sslContext.isDefaultCipherSuiteList(this.sslConfig.enabledCipherSuites)) {
                this.sslConfig.enabledCipherSuites = this.sslContext.getDefaultCipherSuites(!useClientMode);
            }
            this.sslConfig.toggleClientMode();
        }
        this.isUnsureMode = false;
    }

    boolean isOutboundDone() {
        return this.outputRecord.isClosed() && this.outputRecord.isEmpty();
    }

    boolean isOutboundClosed() {
        return this.outputRecord.isClosed();
    }

    boolean isInboundClosed() {
        return this.inputRecord.isClosed();
    }

    void closeInbound() {
        block5: {
            if (this.isInboundClosed()) {
                return;
            }
            try {
                if (!this.isInputCloseNotified) {
                    this.initiateInboundClose();
                } else {
                    this.passiveInboundClose();
                }
            }
            catch (IOException ioe) {
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl")) break block5;
                SSLLogger.warning("inbound closure failed", ioe);
            }
        }
    }

    private void passiveInboundClose() throws IOException {
        if (!this.isInboundClosed()) {
            this.inputRecord.close();
        }
        if (!this.isOutboundClosed()) {
            boolean needCloseNotify = SSLConfiguration.acknowledgeCloseNotify;
            if (!needCloseNotify) {
                ProtocolVersion pv;
                if (this.isNegotiated) {
                    if (!this.protocolVersion.useTLS13PlusSpec()) {
                        needCloseNotify = true;
                    }
                } else if (!(this.handshakeContext == null || (pv = this.handshakeContext.negotiatedProtocol) != null && pv.useTLS13PlusSpec())) {
                    needCloseNotify = true;
                }
            }
            if (needCloseNotify) {
                this.closeNotify(false);
            }
        }
    }

    private void initiateInboundClose() throws IOException {
        if (!this.isInboundClosed()) {
            this.inputRecord.close();
        }
    }

    void closeOutbound() {
        block3: {
            if (this.isOutboundClosed()) {
                return;
            }
            try {
                this.initiateOutboundClose();
            }
            catch (IOException ioe) {
                if (!SSLLogger.isOn || !SSLLogger.isOn("ssl")) break block3;
                SSLLogger.warning("outbound closure failed", ioe);
            }
        }
    }

    private void initiateOutboundClose() throws IOException {
        boolean useUserCanceled = !this.isNegotiated && this.handshakeContext != null && !this.peerUserCanceled;
        this.closeNotify(useUserCanceled);
    }

    SSLEngineResult.HandshakeStatus getHandshakeStatus() {
        if (!this.outputRecord.isEmpty()) {
            return SSLEngineResult.HandshakeStatus.NEED_WRAP;
        }
        if (this.isOutboundClosed() && this.isInboundClosed()) {
            return SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
        }
        if (this.handshakeContext != null) {
            if (!this.handshakeContext.delegatedActions.isEmpty()) {
                return SSLEngineResult.HandshakeStatus.NEED_TASK;
            }
            if (!this.isInboundClosed()) {
                if (this.sslContext.isDTLS() && !this.inputRecord.isEmpty()) {
                    return SSLEngineResult.HandshakeStatus.NEED_UNWRAP_AGAIN;
                }
                return SSLEngineResult.HandshakeStatus.NEED_UNWRAP;
            }
            if (!this.isOutboundClosed()) {
                return SSLEngineResult.HandshakeStatus.NEED_WRAP;
            }
        } else if (this.needHandshakeFinishedStatus) {
            return SSLEngineResult.HandshakeStatus.NEED_WRAP;
        }
        return SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
    }

    SSLEngineResult.HandshakeStatus finishHandshake() {
        if (this.protocolVersion.useTLS13PlusSpec()) {
            this.outputRecord.tc = this;
            this.inputRecord.tc = this;
            this.cipherSuite = this.handshakeContext.negotiatedCipherSuite;
            this.inputRecord.readCipher.baseSecret = this.handshakeContext.baseReadSecret;
            this.outputRecord.writeCipher.baseSecret = this.handshakeContext.baseWriteSecret;
        }
        this.handshakeContext = null;
        this.outputRecord.handshakeHash.finish();
        this.inputRecord.finishHandshake();
        this.outputRecord.finishHandshake();
        this.isNegotiated = true;
        if (this.transport instanceof SSLSocket && this.sslConfig.handshakeListeners != null && !this.sslConfig.handshakeListeners.isEmpty()) {
            HandshakeCompletedEvent hce = new HandshakeCompletedEvent((SSLSocket)((Object)this.transport), this.conSession);
            Thread thread = new Thread(null, new NotifyHandshake(this.sslConfig.handshakeListeners, hce), "HandshakeCompletedNotify-Thread", 0L, false);
            thread.start();
        }
        return SSLEngineResult.HandshakeStatus.FINISHED;
    }

    SSLEngineResult.HandshakeStatus finishPostHandshake() {
        this.handshakeContext = null;
        return SSLEngineResult.HandshakeStatus.FINISHED;
    }

    private static class NotifyHandshake
    implements Runnable {
        private final Set<Map.Entry<HandshakeCompletedListener, AccessControlContext>> targets;
        private final HandshakeCompletedEvent event;

        NotifyHandshake(Map<HandshakeCompletedListener, AccessControlContext> listeners, HandshakeCompletedEvent event) {
            this.targets = new HashSet<Map.Entry<HandshakeCompletedListener, AccessControlContext>>(listeners.entrySet());
            this.event = event;
        }

        @Override
        public void run() {
            for (Map.Entry<HandshakeCompletedListener, AccessControlContext> entry : this.targets) {
                final HandshakeCompletedListener listener = entry.getKey();
                AccessControlContext acc = entry.getValue();
                AccessController.doPrivileged(new PrivilegedAction<Void>(){

                    @Override
                    public Void run() {
                        listener.handshakeCompleted(event);
                        return null;
                    }
                }, acc);
            }
        }
    }
}

