/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;
import sun.security.ssl.Authenticator;
import sun.security.ssl.Ciphertext;
import sun.security.ssl.ContentType;
import sun.security.ssl.HandshakeHash;
import sun.security.ssl.ProtocolVersion;
import sun.security.ssl.Record;
import sun.security.ssl.SSLCipher;
import sun.security.ssl.SSLHandshake;
import sun.security.ssl.SSLLogger;
import sun.security.ssl.TransportContext;

abstract class OutputRecord
extends ByteArrayOutputStream
implements Record,
Closeable {
    SSLCipher.SSLWriteCipher writeCipher;
    TransportContext tc;
    final HandshakeHash handshakeHash;
    boolean firstMessage;
    ProtocolVersion protocolVersion;
    ProtocolVersion helloVersion;
    boolean isFirstAppOutputRecord = true;
    int packetSize;
    private int fragmentSize;
    volatile boolean isClosed;
    final ReentrantLock recordLock = new ReentrantLock();
    private static final int[] V3toV2CipherMap1 = new int[]{-1, -1, -1, 2, 1, -1, 4, 5, -1, 6, 7};
    private static final int[] V3toV2CipherMap3 = new int[]{-1, -1, -1, 128, 128, -1, 128, 128, -1, 64, 192};
    private static final byte[] HANDSHAKE_MESSAGE_KEY_UPDATE = new byte[]{SSLHandshake.KEY_UPDATE.id, 0, 0, 1, 0};

    OutputRecord(HandshakeHash handshakeHash, SSLCipher.SSLWriteCipher writeCipher) {
        this.writeCipher = writeCipher;
        this.firstMessage = true;
        this.fragmentSize = 16384;
        this.handshakeHash = handshakeHash;
    }

    void setVersion(ProtocolVersion protocolVersion) {
        this.recordLock.lock();
        try {
            this.protocolVersion = protocolVersion;
        }
        finally {
            this.recordLock.unlock();
        }
    }

    void setHelloVersion(ProtocolVersion helloVersion) {
        this.recordLock.lock();
        try {
            this.helloVersion = helloVersion;
        }
        finally {
            this.recordLock.unlock();
        }
    }

    boolean isEmpty() {
        return false;
    }

    boolean seqNumIsHuge() {
        this.recordLock.lock();
        try {
            boolean bl = this.writeCipher.authenticator != null && this.writeCipher.authenticator.seqNumIsHuge();
            return bl;
        }
        finally {
            this.recordLock.unlock();
        }
    }

    abstract void encodeAlert(byte var1, byte var2) throws IOException;

    abstract void encodeHandshake(byte[] var1, int var2, int var3) throws IOException;

    abstract void encodeChangeCipherSpec() throws IOException;

    void disposeWriteCipher() {
        throw new UnsupportedOperationException();
    }

    Ciphertext encode(ByteBuffer[] srcs, int srcsOffset, int srcsLength, ByteBuffer[] dsts, int dstsOffset, int dstsLength) throws IOException {
        throw new UnsupportedOperationException();
    }

    void encodeV2NoCipher() throws IOException {
        throw new UnsupportedOperationException();
    }

    void deliver(byte[] source, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    void setDeliverStream(OutputStream outputStream) {
        throw new UnsupportedOperationException();
    }

    void changeWriteCiphers(SSLCipher.SSLWriteCipher writeCipher, boolean useChangeCipherSpec) throws IOException {
        this.recordLock.lock();
        try {
            if (this.isClosed()) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.warning("outbound has closed, ignore outbound change_cipher_spec message", new Object[0]);
                }
                return;
            }
            if (useChangeCipherSpec) {
                this.encodeChangeCipherSpec();
            }
            this.disposeWriteCipher();
            this.writeCipher = writeCipher;
            this.isFirstAppOutputRecord = true;
        }
        finally {
            this.recordLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void changeWriteCiphers(SSLCipher.SSLWriteCipher writeCipher, byte keyUpdateRequest) throws IOException {
        this.recordLock.lock();
        try {
            if (this.isClosed()) {
                if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                    SSLLogger.warning("outbound has closed, ignore outbound key_update handshake message", new Object[0]);
                }
                return;
            }
            byte[] hm = (byte[])HANDSHAKE_MESSAGE_KEY_UPDATE.clone();
            hm[hm.length - 1] = keyUpdateRequest;
            this.encodeHandshake(hm, 0, hm.length);
            this.flush();
            this.disposeWriteCipher();
            this.writeCipher = writeCipher;
            this.isFirstAppOutputRecord = true;
        }
        finally {
            this.recordLock.unlock();
        }
    }

    void changePacketSize(int packetSize) {
        this.recordLock.lock();
        try {
            this.packetSize = packetSize;
        }
        finally {
            this.recordLock.unlock();
        }
    }

    void changeFragmentSize(int fragmentSize) {
        this.recordLock.lock();
        try {
            this.fragmentSize = fragmentSize;
        }
        finally {
            this.recordLock.unlock();
        }
    }

    int getMaxPacketSize() {
        this.recordLock.lock();
        try {
            int n = this.packetSize;
            return n;
        }
        finally {
            this.recordLock.unlock();
        }
    }

    void initHandshaker() {
    }

    void finishHandshake() {
    }

    void launchRetransmission() {
    }

    @Override
    public void close() throws IOException {
        this.recordLock.lock();
        try {
            if (this.isClosed) {
                return;
            }
            this.isClosed = true;
            this.writeCipher.dispose();
        }
        finally {
            this.recordLock.unlock();
        }
    }

    boolean isClosed() {
        return this.isClosed;
    }

    int calculateFragmentSize(int fragmentLimit) {
        if (this.fragmentSize > 0) {
            fragmentLimit = Math.min(fragmentLimit, this.fragmentSize);
        }
        if (this.protocolVersion.useTLS13PlusSpec()) {
            return fragmentLimit - T13PaddingHolder.zeros.length - 1;
        }
        return fragmentLimit;
    }

    static long encrypt(SSLCipher.SSLWriteCipher encCipher, byte contentType, ByteBuffer destination, int headerOffset, int dstLim, int headerSize, ProtocolVersion protocolVersion) {
        boolean isDTLS = protocolVersion.isDTLS;
        if (isDTLS) {
            if (protocolVersion.useTLS13PlusSpec()) {
                return OutputRecord.d13Encrypt(encCipher, contentType, destination, headerOffset, dstLim, headerSize, protocolVersion);
            }
            return OutputRecord.d10Encrypt(encCipher, contentType, destination, headerOffset, dstLim, headerSize, protocolVersion);
        }
        if (protocolVersion.useTLS13PlusSpec()) {
            return OutputRecord.t13Encrypt(encCipher, contentType, destination, headerOffset, dstLim, headerSize, protocolVersion);
        }
        return OutputRecord.t10Encrypt(encCipher, contentType, destination, headerOffset, dstLim, headerSize, protocolVersion);
    }

    private static long d13Encrypt(SSLCipher.SSLWriteCipher encCipher, byte contentType, ByteBuffer destination, int headerOffset, int dstLim, int headerSize, ProtocolVersion protocolVersion) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static long d10Encrypt(SSLCipher.SSLWriteCipher encCipher, byte contentType, ByteBuffer destination, int headerOffset, int dstLim, int headerSize, ProtocolVersion protocolVersion) {
        byte[] sequenceNumber = encCipher.authenticator.sequenceNumber();
        encCipher.encrypt(contentType, destination);
        int fragLen = destination.limit() - headerOffset - headerSize;
        destination.put(headerOffset, contentType);
        destination.put(headerOffset + 1, protocolVersion.major);
        destination.put(headerOffset + 2, protocolVersion.minor);
        destination.put(headerOffset + 3, sequenceNumber[0]);
        destination.put(headerOffset + 4, sequenceNumber[1]);
        destination.put(headerOffset + 5, sequenceNumber[2]);
        destination.put(headerOffset + 6, sequenceNumber[3]);
        destination.put(headerOffset + 7, sequenceNumber[4]);
        destination.put(headerOffset + 8, sequenceNumber[5]);
        destination.put(headerOffset + 9, sequenceNumber[6]);
        destination.put(headerOffset + 10, sequenceNumber[7]);
        destination.put(headerOffset + 11, (byte)(fragLen >> 8));
        destination.put(headerOffset + 12, (byte)fragLen);
        destination.position(destination.limit());
        return Authenticator.toLong(sequenceNumber);
    }

    private static long t13Encrypt(SSLCipher.SSLWriteCipher encCipher, byte contentType, ByteBuffer destination, int headerOffset, int dstLim, int headerSize, ProtocolVersion protocolVersion) {
        if (!encCipher.isNullCipher()) {
            int endOfPt = destination.limit();
            int startOfPt = destination.position();
            destination.position(endOfPt);
            destination.limit(endOfPt + 1 + T13PaddingHolder.zeros.length);
            destination.put(contentType);
            destination.put(T13PaddingHolder.zeros);
            destination.position(startOfPt);
        }
        ProtocolVersion pv = protocolVersion;
        if (!encCipher.isNullCipher()) {
            pv = ProtocolVersion.TLS12;
            contentType = ContentType.APPLICATION_DATA.id;
        } else if (protocolVersion.useTLS13PlusSpec()) {
            pv = ProtocolVersion.TLS12;
        }
        byte[] sequenceNumber = encCipher.authenticator.sequenceNumber();
        encCipher.encrypt(contentType, destination);
        int fragLen = destination.limit() - headerOffset - headerSize;
        destination.put(headerOffset, contentType);
        destination.put(headerOffset + 1, pv.major);
        destination.put(headerOffset + 2, pv.minor);
        destination.put(headerOffset + 3, (byte)(fragLen >> 8));
        destination.put(headerOffset + 4, (byte)fragLen);
        destination.position(destination.limit());
        return Authenticator.toLong(sequenceNumber);
    }

    private static long t10Encrypt(SSLCipher.SSLWriteCipher encCipher, byte contentType, ByteBuffer destination, int headerOffset, int dstLim, int headerSize, ProtocolVersion protocolVersion) {
        byte[] sequenceNumber = encCipher.authenticator.sequenceNumber();
        encCipher.encrypt(contentType, destination);
        int fragLen = destination.limit() - headerOffset - headerSize;
        destination.put(headerOffset, contentType);
        destination.put(headerOffset + 1, protocolVersion.major);
        destination.put(headerOffset + 2, protocolVersion.minor);
        destination.put(headerOffset + 3, (byte)(fragLen >> 8));
        destination.put(headerOffset + 4, (byte)fragLen);
        destination.position(destination.limit());
        return Authenticator.toLong(sequenceNumber);
    }

    long encrypt(SSLCipher.SSLWriteCipher encCipher, byte contentType, int headerSize) {
        if (this.protocolVersion.useTLS13PlusSpec()) {
            return this.t13Encrypt(encCipher, contentType, headerSize);
        }
        return this.t10Encrypt(encCipher, contentType, headerSize);
    }

    private long t13Encrypt(SSLCipher.SSLWriteCipher encCipher, byte contentType, int headerSize) {
        ProtocolVersion pv;
        if (!encCipher.isNullCipher()) {
            this.write(contentType);
            this.write(T13PaddingHolder.zeros, 0, T13PaddingHolder.zeros.length);
        }
        byte[] sequenceNumber = encCipher.authenticator.sequenceNumber();
        int contentLen = this.count - headerSize;
        int requiredPacketSize = encCipher.calculatePacketSize(contentLen, headerSize);
        if (requiredPacketSize > this.buf.length) {
            byte[] newBuf = new byte[requiredPacketSize];
            System.arraycopy(this.buf, 0, newBuf, 0, this.count);
            this.buf = newBuf;
        }
        if (!encCipher.isNullCipher()) {
            pv = ProtocolVersion.TLS12;
            contentType = ContentType.APPLICATION_DATA.id;
        } else {
            pv = ProtocolVersion.TLS12;
        }
        ByteBuffer destination = ByteBuffer.wrap(this.buf, headerSize, contentLen);
        this.count = headerSize + encCipher.encrypt(contentType, destination);
        int fragLen = this.count - headerSize;
        this.buf[0] = contentType;
        this.buf[1] = pv.major;
        this.buf[2] = pv.minor;
        this.buf[3] = (byte)(fragLen >> 8 & 0xFF);
        this.buf[4] = (byte)(fragLen & 0xFF);
        return Authenticator.toLong(sequenceNumber);
    }

    private long t10Encrypt(SSLCipher.SSLWriteCipher encCipher, byte contentType, int headerSize) {
        byte[] sequenceNumber = encCipher.authenticator.sequenceNumber();
        int position = headerSize + this.writeCipher.getExplicitNonceSize();
        int contentLen = this.count - position;
        int requiredPacketSize = encCipher.calculatePacketSize(contentLen, headerSize);
        if (requiredPacketSize > this.buf.length) {
            byte[] newBuf = new byte[requiredPacketSize];
            System.arraycopy(this.buf, 0, newBuf, 0, this.count);
            this.buf = newBuf;
        }
        ByteBuffer destination = ByteBuffer.wrap(this.buf, position, contentLen);
        this.count = headerSize + encCipher.encrypt(contentType, destination);
        int fragLen = this.count - headerSize;
        this.buf[0] = contentType;
        this.buf[1] = this.protocolVersion.major;
        this.buf[2] = this.protocolVersion.minor;
        this.buf[3] = (byte)(fragLen >> 8 & 0xFF);
        this.buf[4] = (byte)(fragLen & 0xFF);
        return Authenticator.toLong(sequenceNumber);
    }

    static ByteBuffer encodeV2ClientHello(byte[] fragment, int offset, int length) {
        int v3SessIdLenOffset = offset + 34;
        byte v3SessIdLen = fragment[v3SessIdLenOffset];
        int v3CSLenOffset = v3SessIdLenOffset + 1 + v3SessIdLen;
        int v3CSLen = ((fragment[v3CSLenOffset] & 0xFF) << 8) + (fragment[v3CSLenOffset + 1] & 0xFF);
        int cipherSpecs = v3CSLen / 2;
        int v2MaxMsgLen = 11 + cipherSpecs * 6 + 3 + 32;
        byte[] dstBytes = new byte[v2MaxMsgLen];
        ByteBuffer dstBuf = ByteBuffer.wrap(dstBytes);
        int v3CSOffset = v3CSLenOffset + 2;
        int v2CSLen = 0;
        dstBuf.position(11);
        boolean containsRenegoInfoSCSV = false;
        for (int i = 0; i < cipherSpecs; ++i) {
            byte byte1 = fragment[v3CSOffset++];
            byte byte2 = fragment[v3CSOffset++];
            v2CSLen += OutputRecord.V3toV2CipherSuite(dstBuf, byte1, byte2);
            if (containsRenegoInfoSCSV || byte1 != 0 || byte2 != -1) continue;
            containsRenegoInfoSCSV = true;
        }
        if (!containsRenegoInfoSCSV) {
            v2CSLen += OutputRecord.V3toV2CipherSuite(dstBuf, (byte)0, (byte)-1);
        }
        dstBuf.put(fragment, offset + 2, 32);
        int msgLen = dstBuf.position() - 2;
        dstBuf.position(0);
        dstBuf.put((byte)(0x80 | msgLen >>> 8 & 0xFF));
        dstBuf.put((byte)(msgLen & 0xFF));
        dstBuf.put(SSLHandshake.CLIENT_HELLO.id);
        dstBuf.put(fragment[offset]);
        dstBuf.put(fragment[offset + 1]);
        dstBuf.put((byte)(v2CSLen >>> 8));
        dstBuf.put((byte)(v2CSLen & 0xFF));
        dstBuf.put((byte)0);
        dstBuf.put((byte)0);
        dstBuf.put((byte)0);
        dstBuf.put((byte)32);
        dstBuf.position(0);
        dstBuf.limit(msgLen + 2);
        return dstBuf;
    }

    private static int V3toV2CipherSuite(ByteBuffer dstBuf, byte byte1, byte byte2) {
        dstBuf.put((byte)0);
        dstBuf.put(byte1);
        dstBuf.put(byte2);
        if ((byte2 & 0xFF) > 10 || V3toV2CipherMap1[byte2] == -1) {
            return 3;
        }
        dstBuf.put((byte)V3toV2CipherMap1[byte2]);
        dstBuf.put((byte)0);
        dstBuf.put((byte)V3toV2CipherMap3[byte2]);
        return 6;
    }

    private static final class T13PaddingHolder {
        private static final byte[] zeros = new byte[16];

        private T13PaddingHolder() {
        }
    }
}

