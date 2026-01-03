/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.stream;

import java.io.Closeable;
import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteOrder;
import javax.imageio.stream.IIOByteBuffer;

public interface ImageInputStream
extends DataInput,
Closeable {
    public void setByteOrder(ByteOrder var1);

    public ByteOrder getByteOrder();

    public int read() throws IOException;

    public int read(byte[] var1) throws IOException;

    public int read(byte[] var1, int var2, int var3) throws IOException;

    public void readBytes(IIOByteBuffer var1, int var2) throws IOException;

    @Override
    public boolean readBoolean() throws IOException;

    @Override
    public byte readByte() throws IOException;

    @Override
    public int readUnsignedByte() throws IOException;

    @Override
    public short readShort() throws IOException;

    @Override
    public int readUnsignedShort() throws IOException;

    @Override
    public char readChar() throws IOException;

    @Override
    public int readInt() throws IOException;

    public long readUnsignedInt() throws IOException;

    @Override
    public long readLong() throws IOException;

    @Override
    public float readFloat() throws IOException;

    @Override
    public double readDouble() throws IOException;

    @Override
    public String readLine() throws IOException;

    @Override
    public String readUTF() throws IOException;

    @Override
    public void readFully(byte[] var1, int var2, int var3) throws IOException;

    @Override
    public void readFully(byte[] var1) throws IOException;

    public void readFully(short[] var1, int var2, int var3) throws IOException;

    public void readFully(char[] var1, int var2, int var3) throws IOException;

    public void readFully(int[] var1, int var2, int var3) throws IOException;

    public void readFully(long[] var1, int var2, int var3) throws IOException;

    public void readFully(float[] var1, int var2, int var3) throws IOException;

    public void readFully(double[] var1, int var2, int var3) throws IOException;

    public long getStreamPosition() throws IOException;

    public int getBitOffset() throws IOException;

    public void setBitOffset(int var1) throws IOException;

    public int readBit() throws IOException;

    public long readBits(int var1) throws IOException;

    public long length() throws IOException;

    @Override
    public int skipBytes(int var1) throws IOException;

    public long skipBytes(long var1) throws IOException;

    public void seek(long var1) throws IOException;

    public void mark();

    public void reset() throws IOException;

    public void flushBefore(long var1) throws IOException;

    public void flush() throws IOException;

    public long getFlushedPosition();

    public boolean isCached();

    public boolean isCachedMemory();

    public boolean isCachedFile();

    @Override
    public void close() throws IOException;
}

