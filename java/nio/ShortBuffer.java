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
import java.nio.ByteBufferAsShortBufferB;
import java.nio.ByteBufferAsShortBufferL;
import java.nio.ByteOrder;
import java.nio.DirectShortBufferS;
import java.nio.DirectShortBufferU;
import java.nio.HeapShortBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Objects;

public abstract sealed class ShortBuffer
extends Buffer
implements Comparable<ShortBuffer>
permits HeapShortBuffer, DirectShortBufferS, DirectShortBufferU, ByteBufferAsShortBufferB, ByteBufferAsShortBufferL {
    private static final long ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(short[].class);
    final short[] hb;
    final int offset;
    boolean isReadOnly;

    ShortBuffer(int mark, int pos, int lim, int cap, short[] hb, int offset, MemorySegment segment) {
        super(mark, pos, lim, cap, segment);
        this.hb = hb;
        this.offset = offset;
    }

    ShortBuffer(int mark, int pos, int lim, int cap, MemorySegment segment) {
        this(mark, pos, lim, cap, null, 0, segment);
    }

    ShortBuffer(short[] hb, long addr, int cap, MemorySegment segment) {
        super(addr, cap, segment);
        this.hb = hb;
        this.offset = 0;
    }

    @Override
    Object base() {
        return this.hb;
    }

    public static ShortBuffer allocate(int capacity) {
        if (capacity < 0) {
            throw ShortBuffer.createCapacityException(capacity);
        }
        return new HeapShortBuffer(capacity, capacity, null);
    }

    public static ShortBuffer wrap(short[] array, int offset, int length) {
        try {
            return new HeapShortBuffer(array, offset, length, null);
        }
        catch (IllegalArgumentException x) {
            throw new IndexOutOfBoundsException();
        }
    }

    public static ShortBuffer wrap(short[] array) {
        return ShortBuffer.wrap(array, 0, array.length);
    }

    @Override
    public abstract ShortBuffer slice();

    @Override
    public abstract ShortBuffer slice(int var1, int var2);

    @Override
    public abstract ShortBuffer duplicate();

    public abstract ShortBuffer asReadOnlyBuffer();

    public abstract short get();

    public abstract ShortBuffer put(short var1);

    public abstract short get(int var1);

    public abstract ShortBuffer put(int var1, short var2);

    public ShortBuffer get(short[] dst, int offset, int length) {
        Objects.checkFromIndexSize(offset, length, dst.length);
        int pos = this.position();
        if (length > this.limit() - pos) {
            throw new BufferUnderflowException();
        }
        this.getArray(pos, dst, offset, length);
        this.position(pos + length);
        return this;
    }

    public ShortBuffer get(short[] dst) {
        return this.get(dst, 0, dst.length);
    }

    public ShortBuffer get(int index, short[] dst, int offset, int length) {
        Objects.checkFromIndexSize(index, length, this.limit());
        Objects.checkFromIndexSize(offset, length, dst.length);
        this.getArray(index, dst, offset, length);
        return this;
    }

    public ShortBuffer get(int index, short[] dst) {
        return this.get(index, dst, 0, dst.length);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private ShortBuffer getArray(int index, short[] dst, int offset, int length) {
        if ((long)length << 1 > 6L) {
            long bufAddr = this.address + ((long)index << 1);
            long dstOffset = ARRAY_BASE_OFFSET + ((long)offset << 1);
            long len = (long)length << 1;
            try {
                if (this.order() != ByteOrder.nativeOrder()) {
                    SCOPED_MEMORY_ACCESS.copySwapMemory(this.session(), null, this.base(), bufAddr, dst, dstOffset, len, 2L);
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

    public ShortBuffer put(ShortBuffer src) {
        int rem;
        int srcLim;
        if (src == this) {
            throw ShortBuffer.createSameBufferException();
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

    public ShortBuffer put(int index, ShortBuffer src, int offset, int length) {
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
    void putBuffer(int pos, ShortBuffer src, int srcPos, int n) {
        Object srcBase = src.base();
        assert (srcBase != null || src.isDirect());
        Object base = this.base();
        assert (base != null || this.isDirect());
        long srcAddr = src.address + ((long)srcPos << 1);
        long addr = this.address + ((long)pos << 1);
        long len = (long)n << 1;
        try {
            if (this.order() != src.order()) {
                SCOPED_MEMORY_ACCESS.copySwapMemory(src.session(), this.session(), srcBase, srcAddr, base, addr, len, 2L);
            } else {
                SCOPED_MEMORY_ACCESS.copyMemory(src.session(), this.session(), srcBase, srcAddr, base, addr, len);
            }
        }
        finally {
            Reference.reachabilityFence(src);
            Reference.reachabilityFence(this);
        }
    }

    public ShortBuffer put(short[] src, int offset, int length) {
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

    public final ShortBuffer put(short[] src) {
        return this.put(src, 0, src.length);
    }

    public ShortBuffer put(int index, short[] src, int offset, int length) {
        if (this.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        Objects.checkFromIndexSize(index, length, this.limit());
        Objects.checkFromIndexSize(offset, length, src.length);
        this.putArray(index, src, offset, length);
        return this;
    }

    public ShortBuffer put(int index, short[] src) {
        return this.put(index, src, 0, src.length);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    ShortBuffer putArray(int index, short[] src, int offset, int length) {
        if ((long)length << 1 > 6L) {
            long bufAddr = this.address + ((long)index << 1);
            long srcOffset = ARRAY_BASE_OFFSET + ((long)offset << 1);
            long len = (long)length << 1;
            try {
                if (this.order() != ByteOrder.nativeOrder()) {
                    SCOPED_MEMORY_ACCESS.copySwapMemory(null, this.session(), src, srcOffset, this.base(), bufAddr, len, 2L);
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

    public final short[] array() {
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
    public final ShortBuffer position(int newPosition) {
        super.position(newPosition);
        return this;
    }

    @Override
    public final ShortBuffer limit(int newLimit) {
        super.limit(newLimit);
        return this;
    }

    @Override
    public final ShortBuffer mark() {
        super.mark();
        return this;
    }

    @Override
    public final ShortBuffer reset() {
        super.reset();
        return this;
    }

    @Override
    public final ShortBuffer clear() {
        super.clear();
        return this;
    }

    @Override
    public final ShortBuffer flip() {
        super.flip();
        return this;
    }

    @Override
    public final ShortBuffer rewind() {
        super.rewind();
        return this;
    }

    public abstract ShortBuffer compact();

    @Override
    public abstract boolean isDirect();

    public String toString() {
        return this.getClass().getName() + "[pos=" + this.position() + " lim=" + this.limit() + " cap=" + this.capacity() + "]";
    }

    public int hashCode() {
        int h = 1;
        int p = this.position();
        for (int i = this.limit() - 1; i >= p; --i) {
            h = 31 * h + this.get(i);
        }
        return h;
    }

    public boolean equals(Object ob) {
        if (this == ob) {
            return true;
        }
        if (!(ob instanceof ShortBuffer)) {
            return false;
        }
        ShortBuffer that = (ShortBuffer)ob;
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
    public int compareTo(ShortBuffer that) {
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
            return ShortBuffer.compare(this.get(thisPos + i), that.get(thatPos + i));
        }
        return thisRem - thatRem;
    }

    private static int compare(short x, short y) {
        return Short.compare(x, y);
    }

    public int mismatch(ShortBuffer that) {
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

