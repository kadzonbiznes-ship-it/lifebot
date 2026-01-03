/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLException;
import sun.security.ssl.Utilities;

interface Record {
    public static final int maxMacSize = 48;
    public static final int maxDataSize = 16384;
    public static final int maxPadding = 256;
    public static final int maxIVLength = 16;
    public static final int maxFragmentSize = 18432;
    public static final boolean enableCBCProtection = Utilities.getBooleanProperty("jsse.enableCBCProtection", true);
    public static final int OVERFLOW_OF_INT08 = 256;
    public static final int OVERFLOW_OF_INT16 = 65536;
    public static final int OVERFLOW_OF_INT24 = 0x1000000;

    public static int getInt8(ByteBuffer m) throws IOException {
        Record.verifyLength(m, 1);
        return m.get() & 0xFF;
    }

    public static int getInt16(ByteBuffer m) throws IOException {
        Record.verifyLength(m, 2);
        return (m.get() & 0xFF) << 8 | m.get() & 0xFF;
    }

    public static int getInt24(ByteBuffer m) throws IOException {
        Record.verifyLength(m, 3);
        return (m.get() & 0xFF) << 16 | (m.get() & 0xFF) << 8 | m.get() & 0xFF;
    }

    public static int getInt32(ByteBuffer m) throws IOException {
        Record.verifyLength(m, 4);
        return (m.get() & 0xFF) << 24 | (m.get() & 0xFF) << 16 | (m.get() & 0xFF) << 8 | m.get() & 0xFF;
    }

    public static byte[] getBytes8(ByteBuffer m) throws IOException {
        int len = Record.getInt8(m);
        Record.verifyLength(m, len);
        byte[] b = new byte[len];
        m.get(b);
        return b;
    }

    public static byte[] getBytes16(ByteBuffer m) throws IOException {
        int len = Record.getInt16(m);
        Record.verifyLength(m, len);
        byte[] b = new byte[len];
        m.get(b);
        return b;
    }

    public static byte[] getBytes24(ByteBuffer m) throws IOException {
        int len = Record.getInt24(m);
        Record.verifyLength(m, len);
        byte[] b = new byte[len];
        m.get(b);
        return b;
    }

    public static void putInt8(ByteBuffer m, int i) throws IOException {
        Record.verifyLength(m, 1);
        m.put((byte)(i & 0xFF));
    }

    public static void putInt16(ByteBuffer m, int i) throws IOException {
        Record.verifyLength(m, 2);
        m.put((byte)(i >> 8 & 0xFF));
        m.put((byte)(i & 0xFF));
    }

    public static void putInt24(ByteBuffer m, int i) throws IOException {
        Record.verifyLength(m, 3);
        m.put((byte)(i >> 16 & 0xFF));
        m.put((byte)(i >> 8 & 0xFF));
        m.put((byte)(i & 0xFF));
    }

    public static void putInt32(ByteBuffer m, int i) throws IOException {
        m.put((byte)(i >> 24 & 0xFF));
        m.put((byte)(i >> 16 & 0xFF));
        m.put((byte)(i >> 8 & 0xFF));
        m.put((byte)(i & 0xFF));
    }

    public static void putBytes8(ByteBuffer m, byte[] s) throws IOException {
        if (s == null || s.length == 0) {
            Record.verifyLength(m, 1);
            Record.putInt8(m, 0);
        } else {
            Record.verifyLength(m, 1 + s.length);
            Record.putInt8(m, s.length);
            m.put(s);
        }
    }

    public static void putBytes16(ByteBuffer m, byte[] s) throws IOException {
        if (s == null || s.length == 0) {
            Record.verifyLength(m, 2);
            Record.putInt16(m, 0);
        } else {
            Record.verifyLength(m, 2 + s.length);
            Record.putInt16(m, s.length);
            m.put(s);
        }
    }

    public static void putBytes24(ByteBuffer m, byte[] s) throws IOException {
        if (s == null || s.length == 0) {
            Record.verifyLength(m, 3);
            Record.putInt24(m, 0);
        } else {
            Record.verifyLength(m, 3 + s.length);
            Record.putInt24(m, s.length);
            m.put(s);
        }
    }

    public static void verifyLength(ByteBuffer m, int len) throws SSLException {
        if (len > m.remaining()) {
            throw new SSLException("Insufficient space in the buffer, may be cause by an unexpected end of handshake data.");
        }
    }
}

