/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.stream;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Stack;
import javax.imageio.IIOException;
import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageInputStream;
import jdk.internal.util.ByteArray;
import jdk.internal.util.ByteArrayLittleEndian;

public abstract class ImageInputStreamImpl
implements ImageInputStream {
    private final Stack<Long> markByteStack = new Stack();
    private final Stack<Integer> markBitStack = new Stack();
    private boolean isClosed = false;
    private static final int BYTE_BUF_LENGTH = 8192;
    byte[] byteBuf = new byte[8192];
    protected ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
    protected long streamPos;
    protected int bitOffset;
    protected long flushedPos = 0L;

    protected final void checkClosed() throws IOException {
        if (this.isClosed) {
            throw new IOException("closed");
        }
    }

    @Override
    public void setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    @Override
    public ByteOrder getByteOrder() {
        return this.byteOrder;
    }

    @Override
    public abstract int read() throws IOException;

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public abstract int read(byte[] var1, int var2, int var3) throws IOException;

    @Override
    public void readBytes(IIOByteBuffer buf, int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException("len < 0!");
        }
        if (buf == null) {
            throw new NullPointerException("buf == null!");
        }
        byte[] data = new byte[len];
        len = this.read(data, 0, len);
        buf.setData(data);
        buf.setOffset(0);
        buf.setLength(len);
    }

    @Override
    public boolean readBoolean() throws IOException {
        int ch = this.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return ch != 0;
    }

    @Override
    public byte readByte() throws IOException {
        int ch = this.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return (byte)ch;
    }

    @Override
    public int readUnsignedByte() throws IOException {
        int ch = this.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return ch;
    }

    @Override
    public short readShort() throws IOException {
        if (this.read(this.byteBuf, 0, 2) != 2) {
            throw new EOFException();
        }
        return this.byteOrder == ByteOrder.BIG_ENDIAN ? ByteArray.getShort(this.byteBuf, 0) : ByteArrayLittleEndian.getShort(this.byteBuf, 0);
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return this.readShort() & 0xFFFF;
    }

    @Override
    public char readChar() throws IOException {
        return (char)this.readShort();
    }

    @Override
    public int readInt() throws IOException {
        if (this.read(this.byteBuf, 0, 4) != 4) {
            throw new EOFException();
        }
        return this.byteOrder == ByteOrder.BIG_ENDIAN ? ByteArray.getInt(this.byteBuf, 0) : ByteArrayLittleEndian.getInt(this.byteBuf, 0);
    }

    @Override
    public long readUnsignedInt() throws IOException {
        return (long)this.readInt() & 0xFFFFFFFFL;
    }

    @Override
    public long readLong() throws IOException {
        int i1 = this.readInt();
        int i2 = this.readInt();
        if (this.byteOrder == ByteOrder.BIG_ENDIAN) {
            return ((long)i1 << 32) + ((long)i2 & 0xFFFFFFFFL);
        }
        return ((long)i2 << 32) + ((long)i1 & 0xFFFFFFFFL);
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(this.readInt());
    }

    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(this.readLong());
    }

    @Override
    public String readLine() throws IOException {
        StringBuilder input = new StringBuilder();
        int c = -1;
        boolean eol = false;
        block4: while (!eol) {
            c = this.read();
            switch (c) {
                case -1: 
                case 10: {
                    eol = true;
                    continue block4;
                }
                case 13: {
                    eol = true;
                    long cur = this.getStreamPosition();
                    if (this.read() == 10) continue block4;
                    this.seek(cur);
                    continue block4;
                }
            }
            input.append((char)c);
        }
        if (c == -1 && input.length() == 0) {
            return null;
        }
        return input.toString();
    }

    @Override
    public String readUTF() throws IOException {
        String ret;
        this.bitOffset = 0;
        ByteOrder oldByteOrder = this.getByteOrder();
        this.setByteOrder(ByteOrder.BIG_ENDIAN);
        try {
            ret = DataInputStream.readUTF(this);
        }
        catch (IOException e) {
            this.setByteOrder(oldByteOrder);
            throw e;
        }
        this.setByteOrder(oldByteOrder);
        return ret;
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > b.length!");
        }
        while (len > 0) {
            int nbytes = this.read(b, off, len);
            if (nbytes == -1) {
                throw new EOFException();
            }
            off += nbytes;
            len -= nbytes;
        }
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        this.readFully(b, 0, b.length);
    }

    @Override
    public void readFully(short[] s, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > s.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > s.length!");
        }
        while (len > 0) {
            int nelts = Math.min(len, this.byteBuf.length / 2);
            this.readFully(this.byteBuf, 0, nelts * 2);
            this.toShorts(this.byteBuf, s, off, nelts);
            off += nelts;
            len -= nelts;
        }
    }

    @Override
    public void readFully(char[] c, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > c.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > c.length!");
        }
        while (len > 0) {
            int nelts = Math.min(len, this.byteBuf.length / 2);
            this.readFully(this.byteBuf, 0, nelts * 2);
            this.toChars(this.byteBuf, c, off, nelts);
            off += nelts;
            len -= nelts;
        }
    }

    @Override
    public void readFully(int[] i, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!");
        }
        while (len > 0) {
            int nelts = Math.min(len, this.byteBuf.length / 4);
            this.readFully(this.byteBuf, 0, nelts * 4);
            this.toInts(this.byteBuf, i, off, nelts);
            off += nelts;
            len -= nelts;
        }
    }

    @Override
    public void readFully(long[] l, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > l.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > l.length!");
        }
        while (len > 0) {
            int nelts = Math.min(len, this.byteBuf.length / 8);
            this.readFully(this.byteBuf, 0, nelts * 8);
            this.toLongs(this.byteBuf, l, off, nelts);
            off += nelts;
            len -= nelts;
        }
    }

    @Override
    public void readFully(float[] f, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > f.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > f.length!");
        }
        while (len > 0) {
            int nelts = Math.min(len, this.byteBuf.length / 4);
            this.readFully(this.byteBuf, 0, nelts * 4);
            this.toFloats(this.byteBuf, f, off, nelts);
            off += nelts;
            len -= nelts;
        }
    }

    @Override
    public void readFully(double[] d, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > d.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > d.length!");
        }
        while (len > 0) {
            int nelts = Math.min(len, this.byteBuf.length / 8);
            this.readFully(this.byteBuf, 0, nelts * 8);
            this.toDoubles(this.byteBuf, d, off, nelts);
            off += nelts;
            len -= nelts;
        }
    }

    private void toShorts(byte[] b, short[] s, int off, int len) {
        int boff = 0;
        if (this.byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int j = 0; j < len; ++j) {
                s[off + j] = ByteArray.getShort(b, boff);
                boff += 2;
            }
        } else {
            for (int j = 0; j < len; ++j) {
                s[off + j] = ByteArrayLittleEndian.getShort(b, boff);
                boff += 2;
            }
        }
    }

    private void toChars(byte[] b, char[] c, int off, int len) {
        int boff = 0;
        if (this.byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int j = 0; j < len; ++j) {
                c[off + j] = ByteArray.getChar(b, boff);
                boff += 2;
            }
        } else {
            for (int j = 0; j < len; ++j) {
                c[off + j] = ByteArrayLittleEndian.getChar(b, boff);
                boff += 2;
            }
        }
    }

    private void toInts(byte[] b, int[] i, int off, int len) {
        int boff = 0;
        if (this.byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int j = 0; j < len; ++j) {
                i[off + j] = ByteArray.getInt(b, boff);
                boff += 4;
            }
        } else {
            for (int j = 0; j < len; ++j) {
                i[off + j] = ByteArrayLittleEndian.getInt(b, boff);
                boff += 4;
            }
        }
    }

    private void toLongs(byte[] b, long[] l, int off, int len) {
        int boff = 0;
        if (this.byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int j = 0; j < len; ++j) {
                l[off + j] = ByteArray.getLong(b, boff);
                boff += 8;
            }
        } else {
            for (int j = 0; j < len; ++j) {
                l[off + j] = ByteArrayLittleEndian.getLong(b, boff);
                boff += 8;
            }
        }
    }

    private void toFloats(byte[] b, float[] f, int off, int len) {
        int boff = 0;
        if (this.byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int j = 0; j < len; ++j) {
                f[off + j] = ByteArray.getFloat(b, boff);
                boff += 4;
            }
        } else {
            for (int j = 0; j < len; ++j) {
                f[off + j] = ByteArrayLittleEndian.getFloat(b, boff);
                boff += 4;
            }
        }
    }

    private void toDoubles(byte[] b, double[] d, int off, int len) {
        int boff = 0;
        if (this.byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int j = 0; j < len; ++j) {
                d[off + j] = ByteArray.getDouble(b, boff);
                boff += 8;
            }
        } else {
            for (int j = 0; j < len; ++j) {
                d[off + j] = ByteArrayLittleEndian.getDouble(b, boff);
                boff += 8;
            }
        }
    }

    @Override
    public long getStreamPosition() throws IOException {
        this.checkClosed();
        return this.streamPos;
    }

    @Override
    public int getBitOffset() throws IOException {
        this.checkClosed();
        return this.bitOffset;
    }

    @Override
    public void setBitOffset(int bitOffset) throws IOException {
        this.checkClosed();
        if (bitOffset < 0 || bitOffset > 7) {
            throw new IllegalArgumentException("bitOffset must be betwwen 0 and 7!");
        }
        this.bitOffset = bitOffset;
    }

    @Override
    public int readBit() throws IOException {
        this.checkClosed();
        int newBitOffset = this.bitOffset + 1 & 7;
        int val = this.read();
        if (val == -1) {
            throw new EOFException();
        }
        if (newBitOffset != 0) {
            this.seek(this.getStreamPosition() - 1L);
            val >>= 8 - newBitOffset;
        }
        this.bitOffset = newBitOffset;
        return val & 1;
    }

    @Override
    public long readBits(int numBits) throws IOException {
        int bitsToRead;
        this.checkClosed();
        if (numBits < 0 || numBits > 64) {
            throw new IllegalArgumentException();
        }
        if (numBits == 0) {
            return 0L;
        }
        int newBitOffset = this.bitOffset + numBits & 7;
        long accum = 0L;
        for (bitsToRead = numBits + this.bitOffset; bitsToRead > 0; bitsToRead -= 8) {
            int val = this.read();
            if (val == -1) {
                throw new EOFException();
            }
            accum <<= 8;
            accum |= (long)val;
        }
        if (newBitOffset != 0) {
            this.seek(this.getStreamPosition() - 1L);
        }
        this.bitOffset = newBitOffset;
        accum >>>= -bitsToRead;
        return accum &= -1L >>> 64 - numBits;
    }

    @Override
    public long length() {
        return -1L;
    }

    @Override
    public int skipBytes(int n) throws IOException {
        long pos = this.getStreamPosition();
        this.seek(pos + (long)n);
        return (int)(this.getStreamPosition() - pos);
    }

    @Override
    public long skipBytes(long n) throws IOException {
        long pos = this.getStreamPosition();
        this.seek(pos + n);
        return this.getStreamPosition() - pos;
    }

    @Override
    public void seek(long pos) throws IOException {
        this.checkClosed();
        if (pos < this.flushedPos) {
            throw new IndexOutOfBoundsException("pos < flushedPos!");
        }
        this.streamPos = pos;
        this.bitOffset = 0;
    }

    @Override
    public void mark() {
        try {
            this.markByteStack.push(this.getStreamPosition());
            this.markBitStack.push(this.getBitOffset());
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    @Override
    public void reset() throws IOException {
        if (this.markByteStack.empty()) {
            return;
        }
        long pos = this.markByteStack.pop();
        if (pos < this.flushedPos) {
            throw new IIOException("Previous marked position has been discarded!");
        }
        this.seek(pos);
        int offset = this.markBitStack.pop();
        this.setBitOffset(offset);
    }

    @Override
    public void flushBefore(long pos) throws IOException {
        this.checkClosed();
        if (pos < this.flushedPos) {
            throw new IndexOutOfBoundsException("pos < flushedPos!");
        }
        if (pos > this.getStreamPosition()) {
            throw new IndexOutOfBoundsException("pos > getStreamPosition()!");
        }
        this.flushedPos = pos;
    }

    @Override
    public void flush() throws IOException {
        this.flushBefore(this.getStreamPosition());
    }

    @Override
    public long getFlushedPosition() {
        return this.flushedPos;
    }

    @Override
    public boolean isCached() {
        return false;
    }

    @Override
    public boolean isCachedMemory() {
        return false;
    }

    @Override
    public boolean isCachedFile() {
        return false;
    }

    @Override
    public void close() throws IOException {
        this.checkClosed();
        this.isClosed = true;
    }

    @Deprecated(since="9", forRemoval=true)
    protected void finalize() throws Throwable {
        if (!this.isClosed) {
            try {
                this.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        super.finalize();
    }
}

