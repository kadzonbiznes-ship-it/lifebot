/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.stream;

import java.io.IOException;
import java.io.UTFDataFormatException;
import java.nio.ByteOrder;
import javax.imageio.stream.ImageInputStreamImpl;
import javax.imageio.stream.ImageOutputStream;
import jdk.internal.util.ByteArray;
import jdk.internal.util.ByteArrayLittleEndian;

public abstract class ImageOutputStreamImpl
extends ImageInputStreamImpl
implements ImageOutputStream {
    @Override
    public abstract void write(int var1) throws IOException;

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    @Override
    public abstract void write(byte[] var1, int var2, int var3) throws IOException;

    @Override
    public void writeBoolean(boolean v) throws IOException {
        this.write(v ? 1 : 0);
    }

    @Override
    public void writeByte(int v) throws IOException {
        this.write(v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        if (this.byteOrder == ByteOrder.BIG_ENDIAN) {
            ByteArray.setUnsignedShort(this.byteBuf, 0, v);
        } else {
            ByteArrayLittleEndian.setUnsignedShort(this.byteBuf, 0, v);
        }
        this.write(this.byteBuf, 0, 2);
    }

    @Override
    public void writeChar(int v) throws IOException {
        this.writeShort(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        if (this.byteOrder == ByteOrder.BIG_ENDIAN) {
            ByteArray.setInt(this.byteBuf, 0, v);
        } else {
            ByteArrayLittleEndian.setInt(this.byteBuf, 0, v);
        }
        this.write(this.byteBuf, 0, 4);
    }

    @Override
    public void writeLong(long v) throws IOException {
        if (this.byteOrder == ByteOrder.BIG_ENDIAN) {
            ByteArray.setLong(this.byteBuf, 0, v);
        } else {
            ByteArrayLittleEndian.setLong(this.byteBuf, 0, v);
        }
        this.write(this.byteBuf, 0, 4);
        this.write(this.byteBuf, 4, 4);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        this.writeInt(Float.floatToIntBits(v));
    }

    @Override
    public void writeDouble(double v) throws IOException {
        this.writeLong(Double.doubleToLongBits(v));
    }

    @Override
    public void writeBytes(String s) throws IOException {
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            this.write((byte)s.charAt(i));
        }
    }

    @Override
    public void writeChars(String s) throws IOException {
        int len = s.length();
        byte[] b = new byte[len * 2];
        int boff = 0;
        if (this.byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < len; ++i) {
                char v = s.charAt(i);
                ByteArray.setChar(b, boff, v);
                boff += 2;
            }
        } else {
            for (int i = 0; i < len; ++i) {
                char v = s.charAt(i);
                ByteArrayLittleEndian.setChar(b, boff, v);
                boff += 2;
            }
        }
        this.write(b, 0, len * 2);
    }

    @Override
    public void writeUTF(String s) throws IOException {
        char c;
        int strlen = s.length();
        int utflen = 0;
        char[] charr = new char[strlen];
        int boff = 0;
        s.getChars(0, strlen, charr, 0);
        for (int i = 0; i < strlen; ++i) {
            c = charr[i];
            if (c >= '\u0001' && c <= '\u007f') {
                ++utflen;
                continue;
            }
            if (c > '\u07ff') {
                utflen += 3;
                continue;
            }
            utflen += 2;
        }
        if (utflen > 65535) {
            throw new UTFDataFormatException("utflen > 65536!");
        }
        byte[] b = new byte[utflen + 2];
        b[boff++] = (byte)(utflen >>> 8 & 0xFF);
        b[boff++] = (byte)(utflen >>> 0 & 0xFF);
        for (int i = 0; i < strlen; ++i) {
            c = charr[i];
            if (c >= '\u0001' && c <= '\u007f') {
                b[boff++] = (byte)c;
                continue;
            }
            if (c > '\u07ff') {
                b[boff++] = (byte)(0xE0 | c >> 12 & 0xF);
                b[boff++] = (byte)(0x80 | c >> 6 & 0x3F);
                b[boff++] = (byte)(0x80 | c >> 0 & 0x3F);
                continue;
            }
            b[boff++] = (byte)(0xC0 | c >> 6 & 0x1F);
            b[boff++] = (byte)(0x80 | c >> 0 & 0x3F);
        }
        this.write(b, 0, utflen + 2);
    }

    @Override
    public void writeShorts(short[] s, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > s.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > s.length!");
        }
        byte[] b = new byte[len * 2];
        int boff = 0;
        if (this.byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < len; ++i) {
                short v = s[off + i];
                ByteArray.setShort(b, boff, v);
                boff += 2;
            }
        } else {
            for (int i = 0; i < len; ++i) {
                short v = s[off + i];
                ByteArrayLittleEndian.setShort(b, boff, v);
                boff += 2;
            }
        }
        this.write(b, 0, len * 2);
    }

    @Override
    public void writeChars(char[] c, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > c.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > c.length!");
        }
        byte[] b = new byte[len * 2];
        int boff = 0;
        if (this.byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < len; ++i) {
                char v = c[off + i];
                ByteArray.setChar(b, boff, v);
                boff += 2;
            }
        } else {
            for (int i = 0; i < len; ++i) {
                char v = c[off + i];
                ByteArrayLittleEndian.setChar(b, boff, v);
                boff += 2;
            }
        }
        this.write(b, 0, len * 2);
    }

    @Override
    public void writeInts(int[] i, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!");
        }
        byte[] b = new byte[len * 4];
        int boff = 0;
        if (this.byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int j = 0; j < len; ++j) {
                int v = i[off + j];
                ByteArray.setInt(b, boff, v);
                boff += 4;
            }
        } else {
            for (int j = 0; j < len; ++j) {
                int v = i[off + j];
                ByteArrayLittleEndian.setInt(b, boff, v);
                boff += 4;
            }
        }
        this.write(b, 0, len * 4);
    }

    @Override
    public void writeLongs(long[] l, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > l.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > l.length!");
        }
        byte[] b = new byte[len * 8];
        int boff = 0;
        if (this.byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < len; ++i) {
                long v = l[off + i];
                ByteArray.setLong(b, boff, v);
                boff += 8;
            }
        } else {
            for (int i = 0; i < len; ++i) {
                long v = l[off + i];
                ByteArrayLittleEndian.setLong(b, boff, v);
                boff += 8;
            }
        }
        this.write(b, 0, len * 8);
    }

    @Override
    public void writeFloats(float[] f, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > f.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > f.length!");
        }
        byte[] b = new byte[len * 4];
        int boff = 0;
        if (this.byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < len; ++i) {
                float v = f[off + i];
                ByteArray.setFloat(b, boff, v);
                boff += 4;
            }
        } else {
            for (int i = 0; i < len; ++i) {
                float v = f[off + i];
                ByteArrayLittleEndian.setFloat(b, boff, v);
                boff += 4;
            }
        }
        this.write(b, 0, len * 4);
    }

    @Override
    public void writeDoubles(double[] d, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > d.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > d.length!");
        }
        byte[] b = new byte[len * 8];
        int boff = 0;
        if (this.byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < len; ++i) {
                double v = d[off + i];
                ByteArray.setDouble(b, boff, v);
                boff += 8;
            }
        } else {
            for (int i = 0; i < len; ++i) {
                double v = d[off + i];
                ByteArrayLittleEndian.setDouble(b, boff, v);
                boff += 8;
            }
        }
        this.write(b, 0, len * 8);
    }

    @Override
    public void writeBit(int bit) throws IOException {
        this.writeBits(1L & (long)bit, 1);
    }

    @Override
    public void writeBits(long bits, int numBits) throws IOException {
        int shift;
        this.checkClosed();
        if (numBits < 0 || numBits > 64) {
            throw new IllegalArgumentException("Bad value for numBits!");
        }
        if (numBits == 0) {
            return;
        }
        if (this.getStreamPosition() > 0L || this.bitOffset > 0) {
            int offset = this.bitOffset;
            int partialByte = this.read();
            if (partialByte != -1) {
                this.seek(this.getStreamPosition() - 1L);
            } else {
                partialByte = 0;
            }
            if (numBits + offset < 8) {
                shift = 8 - (offset + numBits);
                mask = -1 >>> 32 - numBits;
                partialByte &= ~(mask << shift);
                partialByte = (int)((long)partialByte | (bits & (long)mask) << shift);
                this.write(partialByte);
                this.seek(this.getStreamPosition() - 1L);
                this.bitOffset = offset + numBits;
                numBits = 0;
            } else {
                int num = 8 - offset;
                mask = -1 >>> 32 - num;
                partialByte &= ~mask;
                partialByte = (int)((long)partialByte | bits >> numBits - num & (long)mask);
                this.write(partialByte);
                numBits -= num;
            }
        }
        if (numBits > 7) {
            int extra = numBits % 8;
            for (int numBytes = numBits / 8; numBytes > 0; --numBytes) {
                shift = (numBytes - 1) * 8 + extra;
                int value = (int)(shift == 0 ? bits & 0xFFL : bits >> shift & 0xFFL);
                this.write(value);
            }
            numBits = extra;
        }
        if (numBits != 0) {
            int partialByte = 0;
            partialByte = this.read();
            if (partialByte != -1) {
                this.seek(this.getStreamPosition() - 1L);
            } else {
                partialByte = 0;
            }
            int shift2 = 8 - numBits;
            int mask = -1 >>> 32 - numBits;
            partialByte &= ~(mask << shift2);
            partialByte = (int)((long)partialByte | (bits & (long)mask) << shift2);
            this.write(partialByte);
            this.seek(this.getStreamPosition() - 1L);
            this.bitOffset = numBits;
        }
    }

    protected final void flushBits() throws IOException {
        this.checkClosed();
        if (this.bitOffset != 0) {
            int offset = this.bitOffset;
            int partialByte = this.read();
            if (partialByte < 0) {
                partialByte = 0;
                this.bitOffset = 0;
            } else {
                this.seek(this.getStreamPosition() - 1L);
                partialByte &= -1 << 8 - offset;
            }
            this.write(partialByte);
        }
    }
}

