/*
 * Decompiled with CFR 0.152.
 */
package java.io;

import java.io.DataInput;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.UTFDataFormatException;
import java.util.Objects;
import jdk.internal.util.ByteArray;

public class DataInputStream
extends FilterInputStream
implements DataInput {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];
    private final byte[] readBuffer = new byte[8];
    private byte[] bytearr = EMPTY_BYTE_ARRAY;
    private char[] chararr = EMPTY_CHAR_ARRAY;
    private char[] lineBuffer;

    public DataInputStream(InputStream in) {
        super(in);
    }

    @Override
    public final int read(byte[] b) throws IOException {
        return this.in.read(b, 0, b.length);
    }

    @Override
    public final int read(byte[] b, int off, int len) throws IOException {
        return this.in.read(b, off, len);
    }

    @Override
    public final void readFully(byte[] b) throws IOException {
        this.readFully(b, 0, b.length);
    }

    @Override
    public final void readFully(byte[] b, int off, int len) throws IOException {
        int count;
        Objects.checkFromIndexSize(off, len, b.length);
        for (int n = 0; n < len; n += count) {
            count = this.in.read(b, off + n, len - n);
            if (count >= 0) continue;
            throw new EOFException();
        }
    }

    @Override
    public final int skipBytes(int n) throws IOException {
        int total;
        int cur = 0;
        for (total = 0; total < n && (cur = (int)this.in.skip(n - total)) > 0; total += cur) {
        }
        return total;
    }

    @Override
    public final boolean readBoolean() throws IOException {
        return this.readUnsignedByte() != 0;
    }

    @Override
    public final byte readByte() throws IOException {
        return (byte)this.readUnsignedByte();
    }

    @Override
    public final int readUnsignedByte() throws IOException {
        int ch = this.in.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return ch;
    }

    @Override
    public final short readShort() throws IOException {
        this.readFully(this.readBuffer, 0, 2);
        return ByteArray.getShort(this.readBuffer, 0);
    }

    @Override
    public final int readUnsignedShort() throws IOException {
        this.readFully(this.readBuffer, 0, 2);
        return ByteArray.getUnsignedShort(this.readBuffer, 0);
    }

    @Override
    public final char readChar() throws IOException {
        this.readFully(this.readBuffer, 0, 2);
        return ByteArray.getChar(this.readBuffer, 0);
    }

    @Override
    public final int readInt() throws IOException {
        this.readFully(this.readBuffer, 0, 4);
        return ByteArray.getInt(this.readBuffer, 0);
    }

    @Override
    public final long readLong() throws IOException {
        this.readFully(this.readBuffer, 0, 8);
        return ByteArray.getLong(this.readBuffer, 0);
    }

    @Override
    public final float readFloat() throws IOException {
        this.readFully(this.readBuffer, 0, 4);
        return ByteArray.getFloat(this.readBuffer, 0);
    }

    @Override
    public final double readDouble() throws IOException {
        this.readFully(this.readBuffer, 0, 8);
        return ByteArray.getDouble(this.readBuffer, 0);
    }

    @Override
    @Deprecated
    public final String readLine() throws IOException {
        int c;
        char[] buf = this.lineBuffer;
        if (buf == null) {
            buf = this.lineBuffer = new char[128];
        }
        int room = buf.length;
        int offset = 0;
        block4: while (true) {
            c = this.in.read();
            switch (c) {
                case -1: 
                case 10: {
                    break block4;
                }
                case 13: {
                    int c2 = this.in.read();
                    if (c2 == 10 || c2 == -1) break block4;
                    if (!(this.in instanceof PushbackInputStream)) {
                        this.in = new PushbackInputStream(this.in);
                    }
                    ((PushbackInputStream)this.in).unread(c2);
                    break block4;
                }
                default: {
                    if (--room < 0) {
                        buf = new char[offset + 128];
                        room = buf.length - offset - 1;
                        System.arraycopy(this.lineBuffer, 0, buf, 0, offset);
                        this.lineBuffer = buf;
                    }
                    buf[offset++] = (char)c;
                    continue block4;
                }
            }
            break;
        }
        if (c == -1 && offset == 0) {
            return null;
        }
        return String.copyValueOf(buf, 0, offset);
    }

    @Override
    public final String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }

    public static final String readUTF(DataInput in) throws IOException {
        int c;
        int count;
        int utflen = in.readUnsignedShort();
        byte[] bytearr = null;
        char[] chararr = null;
        if (in instanceof DataInputStream) {
            DataInputStream dis = (DataInputStream)in;
            if (dis.bytearr.length < utflen) {
                dis.bytearr = new byte[utflen * 2];
                dis.chararr = new char[utflen * 2];
            }
            chararr = dis.chararr;
            bytearr = dis.bytearr;
        } else {
            bytearr = new byte[utflen];
            chararr = new char[utflen];
        }
        int chararr_count = 0;
        in.readFully(bytearr, 0, utflen);
        for (count = 0; count < utflen && (c = bytearr[count] & 0xFF) <= 127; ++count) {
            chararr[chararr_count++] = (char)c;
        }
        block6: while (count < utflen) {
            int c2 = bytearr[count] & 0xFF;
            switch (c2 >> 4) {
                case 0: 
                case 1: 
                case 2: 
                case 3: 
                case 4: 
                case 5: 
                case 6: 
                case 7: {
                    ++count;
                    chararr[chararr_count++] = (char)c2;
                    continue block6;
                }
                case 12: 
                case 13: {
                    if ((count += 2) > utflen) {
                        throw new UTFDataFormatException("malformed input: partial character at end");
                    }
                    byte char2 = bytearr[count - 1];
                    if ((char2 & 0xC0) != 128) {
                        throw new UTFDataFormatException("malformed input around byte " + count);
                    }
                    chararr[chararr_count++] = (char)((c2 & 0x1F) << 6 | char2 & 0x3F);
                    continue block6;
                }
                case 14: {
                    if ((count += 3) > utflen) {
                        throw new UTFDataFormatException("malformed input: partial character at end");
                    }
                    byte char2 = bytearr[count - 2];
                    byte char3 = bytearr[count - 1];
                    if ((char2 & 0xC0) != 128 || (char3 & 0xC0) != 128) {
                        throw new UTFDataFormatException("malformed input around byte " + (count - 1));
                    }
                    chararr[chararr_count++] = (char)((c2 & 0xF) << 12 | (char2 & 0x3F) << 6 | (char3 & 0x3F) << 0);
                    continue block6;
                }
            }
            throw new UTFDataFormatException("malformed input around byte " + count);
        }
        return new String(chararr, 0, chararr_count);
    }
}

