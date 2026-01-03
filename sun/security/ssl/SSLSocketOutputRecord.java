/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLHandshakeException;
import sun.security.ssl.Alert;
import sun.security.ssl.ContentType;
import sun.security.ssl.HandshakeHash;
import sun.security.ssl.OutputRecord;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.Record;
import sun.security.ssl.SSLCipher;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.SSLRecord;
import sun.security.ssl.TransportContext;

final class SSLSocketOutputRecord
extends OutputRecord
implements SSLRecord {
    private OutputStream deliverStream = null;

    SSLSocketOutputRecord(HandshakeHash handshakeHash) {
        this(handshakeHash, (TransportContext)null);
    }

    SSLSocketOutputRecord(HandshakeHash handshakeHash, TransportContext tc) {
        super(handshakeHash, SSLCipher.SSLWriteCipher.nullTlsWriteCipher());
        this.tc = tc;
        this.packetSize = 16709;
        this.protocolVersion = ProtocolVersion.NONE;
    }

    @Override
    void encodeAlert(byte level, byte description) throws IOException {
        this.recordLock.lock();
        try {
            if (this.isClosed()) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.warning("outbound has closed, ignore outbound alert message: " + Alert.nameOf(description), new Object[0]);
                }
                return;
            }
            this.count = 5 + this.writeCipher.getExplicitNonceSize();
            this.write(level);
            this.write(description);
            if (SSLLogger.isOn && SSLLogger.isOn("record")) {
                SSLLogger.fine("WRITE: " + this.protocolVersion.name + " " + ContentType.ALERT.name + "(" + Alert.nameOf(description) + "), length = " + (this.count - 5), new Object[0]);
            }
            this.encrypt(this.writeCipher, ContentType.ALERT.id, 5);
            this.deliverStream.write(this.buf, 0, this.count);
            this.deliverStream.flush();
            if (SSLLogger.isOn && SSLLogger.isOn("packet")) {
                SSLLogger.fine("Raw write", new ByteArrayInputStream(this.buf, 0, this.count));
            }
            this.count = 0;
        }
        finally {
            this.recordLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void encodeHandshake(byte[] source, int offset, int length) throws IOException {
        this.recordLock.lock();
        try {
            byte handshakeType;
            if (this.isClosed()) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.warning("outbound has closed, ignore outbound handshake message", ByteBuffer.wrap(source, offset, length));
                }
                return;
            }
            if (this.firstMessage) {
                this.firstMessage = false;
                if (this.helloVersion == ProtocolVersion.SSL20Hello && source[offset] == SSLHandshake.CLIENT_HELLO.id && source[offset + 4 + 2 + 32] == 0) {
                    ByteBuffer v2ClientHello = SSLSocketOutputRecord.encodeV2ClientHello(source, offset + 4, length - 4);
                    byte[] record = v2ClientHello.array();
                    int limit = v2ClientHello.limit();
                    this.handshakeHash.deliver(record, 2, limit - 2);
                    if (SSLLogger.isOn && SSLLogger.isOn("record")) {
                        SSLLogger.fine("WRITE: SSLv2 ClientHello message, length = " + limit, new Object[0]);
                    }
                    this.deliverStream.write(record, 0, limit);
                    this.deliverStream.flush();
                    if (SSLLogger.isOn && SSLLogger.isOn("packet")) {
                        SSLLogger.fine("Raw write", new ByteArrayInputStream(record, 0, limit));
                    }
                    return;
                }
            }
            if (this.handshakeHash.isHashable(handshakeType = source[0])) {
                this.handshakeHash.deliver(source, offset, length);
            }
            int fragLimit = this.getFragLimit();
            int position = 5 + this.writeCipher.getExplicitNonceSize();
            if (this.count == 0) {
                this.count = position;
            }
            if (this.count - position < fragLimit - length) {
                this.write(source, offset, length);
                return;
            }
            int limit = offset + length;
            while (offset < limit) {
                int remains = limit - offset + (this.count - position);
                int fragLen = Math.min(fragLimit, remains);
                this.write(source, offset, fragLen);
                if (remains < fragLimit) {
                    return;
                }
                if (SSLLogger.isOn && SSLLogger.isOn("record")) {
                    SSLLogger.fine("WRITE: " + this.protocolVersion.name + " " + ContentType.HANDSHAKE.name + ", length = " + (this.count - 5), new Object[0]);
                }
                this.encrypt(this.writeCipher, ContentType.HANDSHAKE.id, 5);
                this.deliverStream.write(this.buf, 0, this.count);
                this.deliverStream.flush();
                if (SSLLogger.isOn && SSLLogger.isOn("packet")) {
                    SSLLogger.fine("Raw write", new ByteArrayInputStream(this.buf, 0, this.count));
                }
                offset += fragLen;
                this.count = position;
            }
        }
        finally {
            this.recordLock.unlock();
        }
    }

    @Override
    void encodeChangeCipherSpec() throws IOException {
        this.recordLock.lock();
        try {
            if (this.isClosed()) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.warning("outbound has closed, ignore outbound change_cipher_spec message", new Object[0]);
                }
                return;
            }
            this.count = 5 + this.writeCipher.getExplicitNonceSize();
            this.write(1);
            this.encrypt(this.writeCipher, ContentType.CHANGE_CIPHER_SPEC.id, 5);
            this.deliverStream.write(this.buf, 0, this.count);
            if (SSLLogger.isOn && SSLLogger.isOn("packet")) {
                SSLLogger.fine("Raw write", new ByteArrayInputStream(this.buf, 0, this.count));
            }
            this.count = 0;
        }
        finally {
            this.recordLock.unlock();
        }
    }

    @Override
    void disposeWriteCipher() {
        this.writeCipher.dispose();
    }

    @Override
    public void flush() throws IOException {
        this.recordLock.lock();
        try {
            int position = 5 + this.writeCipher.getExplicitNonceSize();
            if (this.count <= position) {
                return;
            }
            if (SSLLogger.isOn && SSLLogger.isOn("record")) {
                SSLLogger.fine("WRITE: " + this.protocolVersion.name + " " + ContentType.HANDSHAKE.name + ", length = " + (this.count - 5), new Object[0]);
            }
            this.encrypt(this.writeCipher, ContentType.HANDSHAKE.id, 5);
            this.deliverStream.write(this.buf, 0, this.count);
            this.deliverStream.flush();
            if (SSLLogger.isOn && SSLLogger.isOn("packet")) {
                SSLLogger.fine("Raw write", new ByteArrayInputStream(this.buf, 0, this.count));
            }
            this.count = 0;
        }
        finally {
            this.recordLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void deliver(byte[] source, int offset, int length) throws IOException {
        this.recordLock.lock();
        try {
            if (this.isClosed()) {
                throw new SocketException("Connection or outbound has been closed");
            }
            if (this.writeCipher.authenticator.seqNumOverflow()) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.fine("sequence number extremely close to overflow (2^64-1 packets). Closing connection.", new Object[0]);
                }
                throw new SSLHandshakeException("sequence number overflow");
            }
            boolean isFirstRecordOfThePayload = true;
            int limit = offset + length;
            while (offset < limit) {
                int position;
                int fragLen;
                if (this.packetSize > 0) {
                    fragLen = Math.min(16709, this.packetSize);
                    fragLen = this.writeCipher.calculateFragmentSize(fragLen, 5);
                    fragLen = Math.min(fragLen, 16384);
                } else {
                    fragLen = 16384;
                }
                fragLen = this.calculateFragmentSize(fragLen);
                if (isFirstRecordOfThePayload && this.needToSplitPayload()) {
                    fragLen = 1;
                    isFirstRecordOfThePayload = false;
                } else {
                    fragLen = Math.min(fragLen, limit - offset);
                }
                this.count = position = 5 + this.writeCipher.getExplicitNonceSize();
                this.write(source, offset, fragLen);
                if (SSLLogger.isOn && SSLLogger.isOn("record")) {
                    SSLLogger.fine("WRITE: " + this.protocolVersion.name + " " + ContentType.APPLICATION_DATA.name + ", length = " + (this.count - position), new Object[0]);
                }
                this.encrypt(this.writeCipher, ContentType.APPLICATION_DATA.id, 5);
                this.deliverStream.write(this.buf, 0, this.count);
                this.deliverStream.flush();
                if (SSLLogger.isOn && SSLLogger.isOn("packet")) {
                    SSLLogger.fine("Raw write", new ByteArrayInputStream(this.buf, 0, this.count));
                }
                this.count = 0;
                if (this.isFirstAppOutputRecord) {
                    this.isFirstAppOutputRecord = false;
                }
                offset += fragLen;
            }
        }
        finally {
            this.recordLock.unlock();
        }
    }

    @Override
    void setDeliverStream(OutputStream outputStream) {
        this.recordLock.lock();
        try {
            this.deliverStream = outputStream;
        }
        finally {
            this.recordLock.unlock();
        }
    }

    private boolean needToSplitPayload() {
        return !this.protocolVersion.useTLS11PlusSpec() && this.writeCipher.isCBCMode() && !this.isFirstAppOutputRecord && Record.enableCBCProtection;
    }

    private int getFragLimit() {
        int fragLimit;
        if (this.packetSize > 0) {
            fragLimit = Math.min(16709, this.packetSize);
            fragLimit = this.writeCipher.calculateFragmentSize(fragLimit, 5);
            fragLimit = Math.min(fragLimit, 16384);
        } else {
            fragLimit = 16384;
        }
        fragLimit = this.calculateFragmentSize(fragLimit);
        return fragLimit;
    }
}

