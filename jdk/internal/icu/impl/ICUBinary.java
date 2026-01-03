/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.icu.impl;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import jdk.internal.icu.util.VersionInfo;

public final class ICUBinary {
    private static final byte BIG_ENDIAN_ = 1;
    private static final byte MAGIC1 = -38;
    private static final byte MAGIC2 = 39;
    private static final byte CHAR_SET_ = 0;
    private static final byte CHAR_SIZE_ = 2;
    private static final String MAGIC_NUMBER_AUTHENTICATION_FAILED_ = "ICUBinary data file error: Magic number authentication failed";
    private static final String HEADER_AUTHENTICATION_FAILED_ = "ICUBinary data file error: Header authentication failed";

    public static ByteBuffer getRequiredData(final String itemPath) {
        ByteBuffer byteBuffer;
        block13: {
            final Class<ICUBinary> root = ICUBinary.class;
            InputStream is = AccessController.doPrivileged(new PrivilegedAction<InputStream>(){

                @Override
                public InputStream run() {
                    return root.getResourceAsStream(itemPath);
                }
            });
            try {
                int avail = is.available();
                byte[] bytes = avail > 32 ? new byte[avail] : new byte[128];
                int length = 0;
                while (true) {
                    if (length < bytes.length) {
                        int numRead = is.read(bytes, length, bytes.length - length);
                        if (numRead < 0) break;
                        length += numRead;
                        continue;
                    }
                    int nextByte = is.read();
                    if (nextByte < 0) break;
                    int capacity = 2 * bytes.length;
                    if (capacity < 128) {
                        capacity = 128;
                    } else if (capacity < 16384) {
                        capacity *= 2;
                    }
                    bytes = Arrays.copyOf(bytes, capacity);
                    bytes[length++] = (byte)nextByte;
                }
                byteBuffer = ByteBuffer.wrap(bytes, 0, length);
                if (is == null) break block13;
            }
            catch (Throwable throwable) {
                try {
                    if (is != null) {
                        try {
                            is.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            is.close();
        }
        return byteBuffer;
    }

    public static VersionInfo readHeaderAndDataVersion(ByteBuffer bytes, int dataFormat, Authenticate authenticate) throws IOException {
        return ICUBinary.getVersionInfoFromCompactInt(ICUBinary.readHeader(bytes, dataFormat, authenticate));
    }

    public static final byte[] readHeader(InputStream inputStream, byte[] dataFormatIDExpected, Authenticate authenticate) throws IOException {
        DataInputStream input = new DataInputStream(inputStream);
        char headersize = input.readChar();
        char readcount = '\u0002';
        byte magic1 = input.readByte();
        ++readcount;
        byte magic2 = input.readByte();
        ++readcount;
        if (magic1 != -38 || magic2 != 39) {
            throw new IOException(MAGIC_NUMBER_AUTHENTICATION_FAILED_);
        }
        input.readChar();
        readcount += 2;
        input.readChar();
        readcount += 2;
        byte bigendian = input.readByte();
        ++readcount;
        byte charset = input.readByte();
        ++readcount;
        byte charsize = input.readByte();
        ++readcount;
        input.readByte();
        ++readcount;
        byte[] dataFormatID = new byte[4];
        input.readFully(dataFormatID);
        readcount += 4;
        byte[] dataVersion = new byte[4];
        input.readFully(dataVersion);
        readcount += 4;
        byte[] unicodeVersion = new byte[4];
        input.readFully(unicodeVersion);
        if (headersize < (readcount += 4)) {
            throw new IOException("Internal Error: Header size error");
        }
        input.skipBytes(headersize - readcount);
        if (bigendian != 1 || charset != 0 || charsize != 2 || !Arrays.equals(dataFormatIDExpected, dataFormatID) || authenticate != null && !authenticate.isDataVersionAcceptable(dataVersion)) {
            throw new IOException(HEADER_AUTHENTICATION_FAILED_);
        }
        return unicodeVersion;
    }

    public static int readHeader(ByteBuffer bytes, int dataFormat, Authenticate authenticate) throws IOException {
        assert (bytes.position() == 0);
        byte magic1 = bytes.get(2);
        byte magic2 = bytes.get(3);
        if (magic1 != -38 || magic2 != 39) {
            throw new IOException(MAGIC_NUMBER_AUTHENTICATION_FAILED_);
        }
        byte isBigEndian = bytes.get(8);
        byte charsetFamily = bytes.get(9);
        byte sizeofUChar = bytes.get(10);
        if (isBigEndian < 0 || 1 < isBigEndian || charsetFamily != 0 || sizeofUChar != 2) {
            throw new IOException(HEADER_AUTHENTICATION_FAILED_);
        }
        bytes.order(isBigEndian != 0 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        char headerSize = bytes.getChar(0);
        char sizeofUDataInfo = bytes.getChar(4);
        if (sizeofUDataInfo < '\u0014' || headerSize < sizeofUDataInfo + 4) {
            throw new IOException("Internal Error: Header size error");
        }
        byte[] formatVersion = new byte[]{bytes.get(16), bytes.get(17), bytes.get(18), bytes.get(19)};
        if (bytes.get(12) != (byte)(dataFormat >> 24) || bytes.get(13) != (byte)(dataFormat >> 16) || bytes.get(14) != (byte)(dataFormat >> 8) || bytes.get(15) != (byte)dataFormat || authenticate != null && !authenticate.isDataVersionAcceptable(formatVersion)) {
            throw new IOException(HEADER_AUTHENTICATION_FAILED_ + String.format("; data format %02x%02x%02x%02x, format version %d.%d.%d.%d", bytes.get(12), bytes.get(13), bytes.get(14), bytes.get(15), formatVersion[0] & 0xFF, formatVersion[1] & 0xFF, formatVersion[2] & 0xFF, formatVersion[3] & 0xFF));
        }
        bytes.position(headerSize);
        return bytes.get(20) << 24 | (bytes.get(21) & 0xFF) << 16 | (bytes.get(22) & 0xFF) << 8 | bytes.get(23) & 0xFF;
    }

    public static void skipBytes(ByteBuffer bytes, int skipLength) {
        if (skipLength > 0) {
            bytes.position(bytes.position() + skipLength);
        }
    }

    public static byte[] getBytes(ByteBuffer bytes, int length, int additionalSkipLength) {
        byte[] dest = new byte[length];
        bytes.get(dest);
        if (additionalSkipLength > 0) {
            ICUBinary.skipBytes(bytes, additionalSkipLength);
        }
        return dest;
    }

    public static String getString(ByteBuffer bytes, int length, int additionalSkipLength) {
        CharBuffer cs = bytes.asCharBuffer();
        String s = cs.subSequence(0, length).toString();
        ICUBinary.skipBytes(bytes, length * 2 + additionalSkipLength);
        return s;
    }

    public static char[] getChars(ByteBuffer bytes, int length, int additionalSkipLength) {
        char[] dest = new char[length];
        bytes.asCharBuffer().get(dest);
        ICUBinary.skipBytes(bytes, length * 2 + additionalSkipLength);
        return dest;
    }

    public static int[] getInts(ByteBuffer bytes, int length, int additionalSkipLength) {
        int[] dest = new int[length];
        bytes.asIntBuffer().get(dest);
        ICUBinary.skipBytes(bytes, length * 4 + additionalSkipLength);
        return dest;
    }

    public static VersionInfo getVersionInfoFromCompactInt(int version) {
        return VersionInfo.getInstance(version >>> 24, version >> 16 & 0xFF, version >> 8 & 0xFF, version & 0xFF);
    }

    public static interface Authenticate {
        public boolean isDataVersionAcceptable(byte[] var1);
    }

    private static final class IsAcceptable
    implements Authenticate {
        private IsAcceptable() {
        }

        @Override
        public boolean isDataVersionAcceptable(byte[] version) {
            return version[0] == 1;
        }
    }
}

