/*
 * Decompiled with CFR 0.152.
 */
package java.nio;

import java.lang.foreign.MemorySegment;
import java.lang.ref.Reference;
import java.nio.Buffer;
import java.nio.BufferMismatch;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBufferAsFloatBufferB;
import java.nio.ByteBufferAsFloatBufferL;
import java.nio.ByteOrder;
import java.nio.DirectFloatBufferS;
import java.nio.DirectFloatBufferU;
import java.nio.HeapFloatBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Objects;

public abstract sealed class FloatBuffer
extends Buffer
implements Comparable<FloatBuffer>
permits HeapFloatBuffer, DirectFloatBufferS, DirectFloatBufferU, ByteBufferAsFloatBufferB, ByteBufferAsFloatBufferL {
    private static final long ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(float[].class);
    final float[] hb;
    final int offset;
    boolean isReadOnly;

    FloatBuffer(int mark, int pos, int lim, int cap, float[] hb, int offset, MemorySegment segment) {
        super(mark, pos, lim, cap, segment);
        this.hb = hb;
        this.offset = offset;
    }

    FloatBuffer(int mark, int pos, int lim, int cap, MemorySegment segment) {
        this(mark, pos, lim, cap, null, 0, segment);
    }

    FloatBuffer(float[] hb, long addr, int cap, MemorySegment segment) {
        super(addr, cap, segment);
        this.hb = hb;
        this.offset = 0;
    }

    @Override
    Object base() {
        return this.hb;
    }

    public static FloatBuffer allocate(int capacity) {
        if (capacity < 0) {
            throw FloatBuffer.createCapacityException(capacity);
        }
        return new HeapFloatBuffer(capacity, capacity, null);
    }

    public static FloatBuffer wrap(float[] array, int offset, int length) {
        try {
            return new HeapFloatBuffer(array, offset, length, null);
        }
        catch (IllegalArgumentException x) {
            throw new IndexOutOfBoundsException();
        }
    }

    public static FloatBuffer wrap(float[] array) {
        return FloatBuffer.wrap(array, 0, array.length);
    }

    @Override
    public abstract FloatBuffer slice();

    @Override
    public abstract FloatBuffer slice(int var1, int var2);

    @Override
    public abstract FloatBuffer duplicate();

    public abstract FloatBuffer asReadOnlyBuffer();

    public abstract float get();

    public abstract FloatBuffer put(float var1);

    public abstract float get(int var1);

    public abstract FloatBuffer put(int var1, float var2);

    public FloatBuffer get(float[] dst, int offset, int length) {
        Objects.checkFromIndexSize(offset, length, dst.length);
        int pos = this.position();
        if (length > this.limit() - pos) {
            throw new BufferUnderflowException();
        }
        this.getArray(pos, dst, offset, length);
        this.position(pos + length);
        return this;
    }

    public FloatBuffer get(float[] dst) {
        return this.get(dst, 0, dst.length);
    }

    public FloatBuffer get(int index, float[] dst, int offset, int length) {
        Objects.checkFromIndexSize(index, length, this.limit());
        Objects.checkFromIndexSize(offset, length, dst.length);
        this.getArray(index, dst, offset, length);
        return this;
    }

    public FloatBuffer get(int index, float[] dst) {
        return this.get(index, dst, 0, dst.length);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private FloatBuffer getArray(int index, float[] dst, int offset, int length) {
        if ((long)length << 2 > 6L) {
            long bufAddr = this.address + ((long)index << 2);
            long dstOffset = ARRAY_BASE_OFFSET + ((long)offset << 2);
            long len = (long)length << 2;
            try {
                if (this.order() != ByteOrder.nativeOrder()) {
                    SCOPED_MEMORY_ACCESS.copySwapMemory(this.session(), null, this.base(), bufAddr, dst, dstOffset, len, 4L);
                }
                SCOPED_MEMORY_ACCESS.copyMemory(this.session(), null, this.base(), bufAddr, dst, dstOffset, len);
            }
            finally {
                Reference.reachabilityFence(this);
            }
        } else {
            int end = offset + length;
            int i = offset;
            int j = index;
            while (i < end) {
                dst[i] = this.get(j);
                ++i;
                ++j;
            }
        }
        return this;
    }

    public FloatBuffer put(FloatBuffer src) {
        int rem;
        int srcLim;
        if (src == this) {
            throw FloatBuffer.createSameBufferException();
        }
        if (this.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        int srcPos = src.position();
        int srcRem = srcPos <= (srcLim = src.limit()) ? srcLim - srcPos : 0;
        int pos = this.position();
        int lim = this.limit();
        int n = rem = pos <= lim ? lim - pos : 0;
        if (srcRem > rem) {
            throw new BufferOverflowException();
        }
        this.putBuffer(pos, src, srcPos, srcRem);
        this.position(pos + srcRem);
        src.position(srcPos + srcRem);
        return this;
    }

    public FloatBuffer put(int index, FloatBuffer src, int offset, int length) {
        Objects.checkFromIndexSize(index, length, this.limit());
        Objects.checkFromIndexSize(offset, length, src.limit());
        if (this.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        this.putBuffer(index, src, offset, length);
        return this;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void putBuffer(int pos, FloatBuffer src, int srcPos, int n) {
        Object srcBase = src.base();
        assert (srcBase != null || src.isDirect());
        Object base = this.base();
        assert (base != null || this.isDirect());
        long srcAddr = src.address + ((long)srcPos << 2);
        long addr = this.address + ((long)pos << 2);
        long len = (long)n << 2;
        try {
            if (this.order() != src.order()) {
                SCOPED_MEMORY_ACCESS.copySwapMemory(src.session(), this.session(), srcBase, srcAddr, base, addr, len, 4L);
            } else {
                SCOPED_MEMORY_ACCESS.copyMemory(src.session(), this.session(), srcBase, srcAddr, base, addr, len);
            }
        }
        finally {
            Reference.reachabilityFence(src);
            Reference.reachabilityFence(this);
        }
    }

    public FloatBuffer put(float[] src, int offset, int length) {
        if (this.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        Objects.checkFromIndexSize(offset, length, src.length);
        int pos = this.position();
        if (length > this.limit() - pos) {
            throw new BufferOverflowException();
        }
        this.putArray(pos, src, offset, length);
        this.position(pos + length);
        return this;
    }

    public final FloatBuffer put(float[] src) {
        return this.put(src, 0, src.length);
    }

    public FloatBuffer put(int index, float[] src, int offset, int length) {
        if (this.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        Objects.checkFromIndexSize(index, length, this.limit());
        Objects.checkFromIndexSize(offset, length, src.length);
        this.putArray(index, src, offset, length);
        return this;
    }

    public FloatBuffer put(int index, float[] src) {
        return this.put(index, src, 0, src.length);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    FloatBuffer putArray(int index, float[] src, int offset, int length) {
        if ((long)length << 2 > 6L) {
            long bufAddr = this.address + ((long)index << 2);
            long srcOffset = ARRAY_BASE_OFFSET + ((long)offset << 2);
            long len = (long)length << 2;
            try {
                if (this.order() != ByteOrder.nativeOrder()) {
                    SCOPED_MEMORY_ACCESS.copySwapMemory(null, this.session(), src, srcOffset, this.base(), bufAddr, len, 4L);
                }
                SCOPED_MEMORY_ACCESS.copyMemory(null, this.session(), src, srcOffset, this.base(), bufAddr, len);
            }
            finally {
                Reference.reachabilityFence(this);
            }
        } else {
            int end = offset + length;
            int i = offset;
            int j = index;
            while (i < end) {
                this.put(j, src[i]);
                ++i;
                ++j;
            }
        }
        return this;
    }

    @Override
    public final boolean hasArray() {
        return this.hb != null && !this.isReadOnly;
    }

    public final float[] array() {
        if (this.hb == null) {
            throw new UnsupportedOperationException();
        }
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        return this.hb;
    }

    @Override
    public final int arrayOffset() {
        if (this.hb == null) {
            throw new UnsupportedOperationException();
        }
        if (this.isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        return this.offset;
    }

    @Override
    public final FloatBuffer position(int newPosition) {
        super.position(newPosition);
        return this;
    }

    @Override
    public final FloatBuffer limit(int newLimit) {
        super.limit(newLimit);
        return this;
    }

    @Override
    public final FloatBuffer mark() {
        super.mark();
        return this;
    }

    @Override
    public final FloatBuffer reset() {
        super.reset();
        return this;
    }

    @Override
    public final FloatBuffer clear() {
        super.clear();
        return this;
    }

    @Override
    public final FloatBuffer flip() {
        super.flip();
        return this;
    }

    @Override
    public final FloatBuffer rewind() {
        super.rewind();
        return this;
    }

    public abstract FloatBuffer compact();

    @Override
    public abstract boolean isDirect();

    public String toString() {
        return this.getClass().getName() + "[pos=" + this.position() + " lim=" + this.limit() + " cap=" + this.capacity() + "]";
    }

    public int hashCode() {
        int h = 1;
        int p = this.position();
        for (int i = this.limit() - 1; i >= p; --i) {
            h = 31 * h + (int)this.get(i);
        }
        return h;
    }

    public boolean equals(Object ob) {
        if (this == ob) {
            return true;
        }
        if (!(ob instanceof FloatBuffer)) {
            return false;
        }
        FloatBuffer that = (FloatBuffer)ob;
        int thisPos = this.position();
        int thisRem = this.limit() - thisPos;
        int thatPos = that.position();
        int thatRem = that.limit() - thatPos;
        if (thisRem < 0 || thisRem != thatRem) {
            return false;
        }
        return BufferMismatch.mismatch(this, thisPos, that, thatPos, thisRem) < 0;
    }

    @Override
    public int compareTo(FloatBuffer that) {
        int thisPos = this.position();
        int thisRem = this.limit() - thisPos;
        int thatPos = that.position();
        int thatRem = that.limit() - thatPos;
        int length = Math.min(thisRem, thatRem);
        if (length < 0) {
            return -1;
        }
        int i = BufferMismatch.mismatch(this, thisPos, that, thatPos, length);
        if (i >= 0) {
            return FloatBuffer.compare(this.get(thisPos + i), that.get(thatPos + i));
        }
        return thisRem - thatRem;
    }

    private static int compare(float x, float y) {
        return x < y ? -1 : (x > y ? 1 : (x == y ? 0 : (Float.isNaN(x) ? (Float.isNaN(y) ? 0 : 1) : -1)));
    }

    public int mismatch(FloatBuffer that) {
        int thisPos = this.position();
        int thisRem = this.limit() - thisPos;
        int thatPos = that.position();
        int thatRem = that.limit() - thatPos;
        int length = Math.min(thisRem, thatRem);
        if (length < 0) {
            return -1;
        }
        int r = BufferMismatch.mismatch(this, thisPos, that, thatPos, length);
        return r == -1 && thisRem != thatRem ? length : r;
    }

    public abstract ByteOrder order();
}

