/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import sun.security.ssl.Alert;
import sun.security.ssl.HandshakeContext;
import sun.security.ssl.SSLConsumer;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SignatureScheme;
import sun.security.ssl.TransportContext;

final class PostHandshakeContext
extends HandshakeContext {
    PostHandshakeContext(TransportContext context) throws IOException {
        super(context);
        if (!this.negotiatedProtocol.useTLS13PlusSpec()) {
            throw this.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Post-handshake not supported in " + this.negotiatedProtocol.name);
        }
        this.localSupportedCertSignAlgs = new ArrayList<SignatureScheme>(context.conSession.getLocalSupportedSignatureSchemes());
        if (context.sslConfig.isClientMode) {
            this.handshakeConsumers.putIfAbsent(SSLHandshake.KEY_UPDATE.id, SSLHandshake.KEY_UPDATE);
            this.handshakeConsumers.putIfAbsent(SSLHandshake.NEW_SESSION_TICKET.id, SSLHandshake.NEW_SESSION_TICKET);
        } else {
            this.handshakeConsumers.putIfAbsent(SSLHandshake.KEY_UPDATE.id, SSLHandshake.KEY_UPDATE);
        }
        this.handshakeFinished = true;
        this.handshakeSession = context.conSession;
    }

    @Override
    void kickstart() throws IOException {
        SSLHandshake.kickstart(this);
    }

    @Override
    void dispatch(byte handshakeType, ByteBuffer fragment) throws IOException {
        SSLConsumer consumer = (SSLConsumer)this.handshakeConsumers.get(handshakeType);
        if (consumer == null) {
            throw this.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unexpected post-handshake message: " + SSLHandshake.nameOf(handshakeType));
        }
        try {
            consumer.consume(this, fragment);
        }
        catch (UnsupportedOperationException unsoe) {
            throw this.conContext.fatal(Alert.UNEXPECTED_MESSAGE, "Unsupported post-handshake message: " + SSLHandshake.nameOf(handshakeType), unsoe);
        }
        catch (BufferOverflowException | BufferUnderflowException be) {
            throw this.conContext.fatal(Alert.DECODE_ERROR, "Illegal handshake message: " + SSLHandshake.nameOf(handshakeType), be);
        }
    }

    static boolean isConsumable(TransportContext context, byte handshakeType) {
        if (handshakeType == SSLHandshake.KEY_UPDATE.id) {
            return context.protocolVersion.useTLS13PlusSpec();
        }
        if (handshakeType == SSLHandshake.NEW_SESSION_TICKET.id) {
            return context.sslConfig.isClientMode;
        }
        return false;
    }
}

