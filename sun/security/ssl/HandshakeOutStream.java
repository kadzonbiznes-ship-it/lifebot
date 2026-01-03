/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import sun.security.ssl.OutputRecord;
import sun.security.ssl.SSLLogger;

public class HandshakeOutStream
extends ByteArrayOutputStream {
    OutputRecord outputRecord;

    HandshakeOutStream(OutputRecord outputRecord) {
        this.outputRecord = outputRecord;
    }

    void complete() throws IOException {
        if (this.size() < 4) {
            throw new RuntimeException("handshake message is not available");
        }
        if (this.outputRecord != null) {
            if (!this.outputRecord.isClosed()) {
                this.outputRecord.encodeHandshake(this.buf, 0, this.count);
            } else if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                SSLLogger.warning("outbound has closed, ignore outbound handshake messages", ByteBuffer.wrap(this.buf, 0, this.count));
            }
            this.reset();
        }
    }

    @Override
    public void write(byte[] b, int off, int len) {
        HandshakeOutStream.checkOverflow(len, 0x1000000);
        super.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        if (this.outputRecord != null) {
            this.outputRecord.flush();
        }
    }

    void putInt8(int i) throws IOException {
        HandshakeOutStream.checkOverflow(i, 256);
        super.write(i);
    }

    void putInt16(int i) throws IOException {
        HandshakeOutStream.checkOverflow(i, 65536);
        super.write(i >> 8);
        super.write(i);
    }

    void putInt24(int i) {
        HandshakeOutStream.checkOverflow(i, 0x1000000);
        super.write(i >> 16);
        super.write(i >> 8);
        super.write(i);
    }

    void putInt32(int i) {
        super.write(i >> 24);
        super.write(i >> 16);
        super.write(i >> 8);
        super.write(i);
    }

    void putBytes8(byte[] b) throws IOException {
        if (b == null) {
            this.putInt8(0);
        } else {
            this.putInt8(b.length);
            super.write(b, 0, b.length);
        }
    }

    public void putBytes16(byte[] b) throws IOException {
        if (b == null) {
            this.putInt16(0);
        } else {
            this.putInt16(b.length);
            super.write(b, 0, b.length);
        }
    }

    void putBytes24(byte[] b) {
        if (b == null) {
            this.putInt24(0);
        } else {
            this.putInt24(b.length);
            super.write(b, 0, b.length);
        }
    }

    private static void checkOverflow(int length, int limit) {
        if (length >= limit) {
            throw new RuntimeException("Field length overflow, the field length (" + length + ") should be less than " + limit);
        }
    }
}

