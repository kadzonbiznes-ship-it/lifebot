/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;
import javax.crypto.BadPaddingException;
import sun.security.ssl.ContentType;
import sun.security.ssl.HandshakeHash;
import sun.security.ssl.Plaintext;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.Record;
import sun.security.ssl.SSLCipher;
import sun.security.ssl.TransportContext;

abstract class InputRecord
implements Record,
Closeable {
    SSLCipher.SSLReadCipher readCipher;
    TransportContext tc;
    final HandshakeHash handshakeHash;
    volatile boolean isClosed;
    ProtocolVersion helloVersion;
    int fragmentSize;
    final ReentrantLock recordLock = new ReentrantLock();

    InputRecord(HandshakeHash handshakeHash, SSLCipher.SSLReadCipher readCipher) {
        this.readCipher = readCipher;
        this.helloVersion = ProtocolVersion.TLS10;
        this.handshakeHash = handshakeHash;
        this.isClosed = false;
        this.fragmentSize = 16384;
    }

    void setHelloVersion(ProtocolVersion helloVersion) {
        this.helloVersion = helloVersion;
    }

    boolean seqNumIsHuge() {
        return this.readCipher.authenticator != null && this.readCipher.authenticator.seqNumIsHuge();
    }

    boolean isEmpty() {
        return false;
    }

    void expectingFinishFlight() {
    }

    void finishHandshake() {
    }

    @Override
    public void close() throws IOException {
        this.recordLock.lock();
        try {
            if (!this.isClosed) {
                this.isClosed = true;
                this.readCipher.dispose();
            }
        }
        finally {
            this.recordLock.unlock();
        }
    }

    boolean isClosed() {
        return this.isClosed;
    }

    void changeReadCiphers(SSLCipher.SSLReadCipher readCipher) {
        this.readCipher.dispose();
        this.readCipher = readCipher;
    }

    void changeFragmentSize(int fragmentSize) {
        this.fragmentSize = fragmentSize;
    }

    int bytesInCompletePacket(ByteBuffer[] srcs, int srcsOffset, int srcsLength) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    int bytesInCompletePacket() throws IOException {
        throw new UnsupportedOperationException();
    }

    void setReceiverStream(InputStream inputStream) {
        throw new UnsupportedOperationException();
    }

    Plaintext acquirePlaintext() throws IOException {
        throw new UnsupportedOperationException();
    }

    abstract Plaintext[] decode(ByteBuffer[] var1, int var2, int var3) throws IOException, BadPaddingException;

    void setDeliverStream(OutputStream outputStream) {
        throw new UnsupportedOperationException();
    }

    int estimateFragmentSize(int packetSize) {
        throw new UnsupportedOperationException();
    }

    static ByteBuffer convertToClientHello(ByteBuffer packet) {
        int srcPos = packet.position();
        byte firstByte = packet.get();
        byte secondByte = packet.get();
        int recordLen = ((firstByte & 0x7F) << 8 | secondByte & 0xFF) + 2;
        packet.position(srcPos + 3);
        byte majorVersion = packet.get();
        byte minorVersion = packet.get();
        int cipherSpecLen = ((packet.get() & 0xFF) << 8) + (packet.get() & 0xFF);
        int sessionIdLen = ((packet.get() & 0xFF) << 8) + (packet.get() & 0xFF);
        int nonceLen = ((packet.get() & 0xFF) << 8) + (packet.get() & 0xFF);
        int requiredSize = 48 + sessionIdLen + cipherSpecLen * 2 / 3;
        byte[] converted = new byte[requiredSize];
        converted[0] = ContentType.HANDSHAKE.id;
        converted[1] = majorVersion;
        converted[2] = minorVersion;
        converted[5] = 1;
        converted[9] = majorVersion;
        converted[10] = minorVersion;
        int pointer = 11;
        int offset = srcPos + 11 + cipherSpecLen + sessionIdLen;
        if (nonceLen < 32) {
            for (int i = 0; i < 32 - nonceLen; ++i) {
                converted[pointer++] = 0;
            }
            packet.position(offset);
            packet.get(converted, pointer, nonceLen);
            pointer += nonceLen;
        } else {
            packet.position(offset + nonceLen - 32);
            packet.get(converted, pointer, 32);
            pointer += 32;
        }
        converted[pointer++] = (byte)(sessionIdLen & 0xFF);
        packet.position(offset -= sessionIdLen);
        packet.get(converted, pointer, sessionIdLen);
        packet.position(offset -= cipherSpecLen);
        int j = pointer + 2;
        for (int i = 0; i < cipherSpecLen; i += 3) {
            if (packet.get() != 0) {
                packet.get();
                packet.get();
                continue;
            }
            converted[j++] = packet.get();
            converted[j++] = packet.get();
        }
        j -= pointer + 2;
        converted[pointer++] = (byte)(j >>> 8 & 0xFF);
        converted[pointer++] = (byte)(j & 0xFF);
        pointer += j;
        converted[pointer++] = 1;
        converted[pointer++] = 0;
        int fragLen = pointer - 5;
        converted[3] = (byte)(fragLen >>> 8 & 0xFF);
        converted[4] = (byte)(fragLen & 0xFF);
        fragLen = pointer - 9;
        converted[6] = (byte)(fragLen >>> 16 & 0xFF);
        converted[7] = (byte)(fragLen >>> 8 & 0xFF);
        converted[8] = (byte)(fragLen & 0xFF);
        packet.position(srcPos + recordLen);
        return ByteBuffer.wrap(converted, 5, pointer - 5);
    }

    static ByteBuffer extract(ByteBuffer[] buffers, int offset, int length, int headerSize) {
        boolean hasFullHeader = false;
        int contentLen = -1;
        int j = 0;
        block0: for (int i = offset; i < offset + length && j < headerSize; ++i) {
            int remains = buffers[i].remaining();
            int pos = buffers[i].position();
            for (int k = 0; k < remains && j < headerSize; ++j, ++k) {
                byte b = buffers[i].get(pos + k);
                if (j == headerSize - 2) {
                    contentLen = (b & 0xFF) << 8;
                    continue;
                }
                if (j != headerSize - 1) continue;
                contentLen |= b & 0xFF;
                hasFullHeader = true;
                continue block0;
            }
        }
        if (!hasFullHeader) {
            throw new BufferUnderflowException();
        }
        int packetLen = headerSize + contentLen;
        int remains = 0;
        for (int i = offset; i < offset + length && (remains += buffers[i].remaining()) < packetLen; ++i) {
        }
        if (remains < packetLen) {
            throw new BufferUnderflowException();
        }
        byte[] packet = new byte[packetLen];
        int packetOffset = 0;
        int packetSpaces = packetLen;
        for (int i = offset; i < offset + length; ++i) {
            if (buffers[i].hasRemaining()) {
                int len = Math.min(packetSpaces, buffers[i].remaining());
                buffers[i].get(packet, packetOffset, len);
                packetOffset += len;
                packetSpaces -= len;
            }
            if (packetSpaces <= 0) break;
        }
        return ByteBuffer.wrap(packet);
    }
}

