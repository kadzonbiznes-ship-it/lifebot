/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import jdk.internal.misc.Unsafe;

public class RenderBuffer {
    protected static final long SIZEOF_BYTE = 1L;
    protected static final long SIZEOF_SHORT = 2L;
    protected static final long SIZEOF_INT = 4L;
    protected static final long SIZEOF_FLOAT = 4L;
    protected static final long SIZEOF_LONG = 8L;
    protected static final long SIZEOF_DOUBLE = 8L;
    private static final int COPY_FROM_ARRAY_THRESHOLD = 6;
    protected final Unsafe unsafe = Unsafe.getUnsafe();
    protected final long baseAddress;
    protected final long endAddress;
    protected long curAddress;
    protected final int capacity;

    protected RenderBuffer(int numBytes) {
        this.curAddress = this.baseAddress = this.unsafe.allocateMemory(numBytes);
        this.endAddress = this.baseAddress + (long)numBytes;
        this.capacity = numBytes;
    }

    public static RenderBuffer allocate(int numBytes) {
        return new RenderBuffer(numBytes);
    }

    public final long getAddress() {
        return this.baseAddress;
    }

    public final int capacity() {
        return this.capacity;
    }

    public final int remaining() {
        return (int)(this.endAddress - this.curAddress);
    }

    public final int position() {
        return (int)(this.curAddress - this.baseAddress);
    }

    public final void position(long numBytes) {
        this.curAddress = this.baseAddress + numBytes;
    }

    public final void clear() {
        this.curAddress = this.baseAddress;
    }

    public final RenderBuffer skip(long numBytes) {
        this.curAddress += numBytes;
        return this;
    }

    public final RenderBuffer putByte(byte x) {
        this.unsafe.putByte(this.curAddress, x);
        ++this.curAddress;
        return this;
    }

    public RenderBuffer put(byte[] x) {
        return this.put(x, 0, x.length);
    }

    public RenderBuffer put(byte[] x, int offset, int length) {
        if (length > 6) {
            long offsetInBytes = (long)offset * 1L + (long)Unsafe.ARRAY_BYTE_BASE_OFFSET;
            long lengthInBytes = (long)length * 1L;
            this.unsafe.copyMemory(x, offsetInBytes, null, this.curAddress, lengthInBytes);
            this.position((long)this.position() + lengthInBytes);
        } else {
            int end = offset + length;
            for (int i = offset; i < end; ++i) {
                this.putByte(x[i]);
            }
        }
        return this;
    }

    public final RenderBuffer putShort(short x) {
        this.unsafe.putShort(this.curAddress, x);
        this.curAddress += 2L;
        return this;
    }

    public RenderBuffer put(short[] x) {
        return this.put(x, 0, x.length);
    }

    public RenderBuffer put(short[] x, int offset, int length) {
        if (length > 6) {
            long offsetInBytes = (long)offset * 2L + (long)Unsafe.ARRAY_SHORT_BASE_OFFSET;
            long lengthInBytes = (long)length * 2L;
            this.unsafe.copyMemory(x, offsetInBytes, null, this.curAddress, lengthInBytes);
            this.position((long)this.position() + lengthInBytes);
        } else {
            int end = offset + length;
            for (int i = offset; i < end; ++i) {
                this.putShort(x[i]);
            }
        }
        return this;
    }

    public final RenderBuffer putInt(int pos, int x) {
        this.unsafe.putInt(this.baseAddress + (long)pos, x);
        return this;
    }

    public final RenderBuffer putInt(int x) {
        this.unsafe.putInt(this.curAddress, x);
        this.curAddress += 4L;
        return this;
    }

    public RenderBuffer put(int[] x) {
        return this.put(x, 0, x.length);
    }

    public RenderBuffer put(int[] x, int offset, int length) {
        if (length > 6) {
            long offsetInBytes = (long)offset * 4L + (long)Unsafe.ARRAY_INT_BASE_OFFSET;
            long lengthInBytes = (long)length * 4L;
            this.unsafe.copyMemory(x, offsetInBytes, null, this.curAddress, lengthInBytes);
            this.position((long)this.position() + lengthInBytes);
        } else {
            int end = offset + length;
            for (int i = offset; i < end; ++i) {
                this.putInt(x[i]);
            }
        }
        return this;
    }

    public final RenderBuffer putFloat(float x) {
        this.unsafe.putFloat(this.curAddress, x);
        this.curAddress += 4L;
        return this;
    }

    public RenderBuffer put(float[] x) {
        return this.put(x, 0, x.length);
    }

    public RenderBuffer put(float[] x, int offset, int length) {
        if (length > 6) {
            long offsetInBytes = (long)offset * 4L + (long)Unsafe.ARRAY_FLOAT_BASE_OFFSET;
            long lengthInBytes = (long)length * 4L;
            this.unsafe.copyMemory(x, offsetInBytes, null, this.curAddress, lengthInBytes);
            this.position((long)this.position() + lengthInBytes);
        } else {
            int end = offset + length;
            for (int i = offset; i < end; ++i) {
                this.putFloat(x[i]);
            }
        }
        return this;
    }

    public final RenderBuffer putLong(long x) {
        this.unsafe.putLong(this.curAddress, x);
        this.curAddress += 8L;
        return this;
    }

    public RenderBuffer put(long[] x) {
        return this.put(x, 0, x.length);
    }

    public RenderBuffer put(long[] x, int offset, int length) {
        if (length > 6) {
            long offsetInBytes = (long)offset * 8L + (long)Unsafe.ARRAY_LONG_BASE_OFFSET;
            long lengthInBytes = (long)length * 8L;
            this.unsafe.copyMemory(x, offsetInBytes, null, this.curAddress, lengthInBytes);
            this.position((long)this.position() + lengthInBytes);
        } else {
            int end = offset + length;
            for (int i = offset; i < end; ++i) {
                this.putLong(x[i]);
            }
        }
        return this;
    }

    public final RenderBuffer putDouble(double x) {
        this.unsafe.putDouble(this.curAddress, x);
        this.curAddress += 8L;
        return this;
    }
}

