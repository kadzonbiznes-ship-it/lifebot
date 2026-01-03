/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.stream;

import java.io.DataOutput;
import java.io.IOException;
import javax.imageio.stream.ImageInputStream;

public interface ImageOutputStream
extends ImageInputStream,
DataOutput {
    @Override
    public void write(int var1) throws IOException;

    @Override
    public void write(byte[] var1) throws IOException;

    @Override
    public void write(byte[] var1, int var2, int var3) throws IOException;

    @Override
    public void writeBoolean(boolean var1) throws IOException;

    @Override
    public void writeByte(int var1) throws IOException;

    @Override
    public void writeShort(int var1) throws IOException;

    @Override
    public void writeChar(int var1) throws IOException;

    @Override
    public void writeInt(int var1) throws IOException;

    @Override
    public void writeLong(long var1) throws IOException;

    @Override
    public void writeFloat(float var1) throws IOException;

    @Override
    public void writeDouble(double var1) throws IOException;

    @Override
    public void writeBytes(String var1) throws IOException;

    @Override
    public void writeChars(String var1) throws IOException;

    @Override
    public void writeUTF(String var1) throws IOException;

    public void writeShorts(short[] var1, int var2, int var3) throws IOException;

    public void writeChars(char[] var1, int var2, int var3) throws IOException;

    public void writeInts(int[] var1, int var2, int var3) throws IOException;

    public void writeLongs(long[] var1, int var2, int var3) throws IOException;

    public void writeFloats(float[] var1, int var2, int var3) throws IOException;

    public void writeDoubles(double[] var1, int var2, int var3) throws IOException;

    public void writeBit(int var1) throws IOException;

    public void writeBits(long var1, int var3) throws IOException;

    @Override
    public void flushBefore(long var1) throws IOException;
}

