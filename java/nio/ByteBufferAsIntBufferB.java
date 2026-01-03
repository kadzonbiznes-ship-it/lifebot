/*
 * Decompiled with CFR 0.152.
 */
package java.nio;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.ByteBufferAsIntBufferRB;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Objects;

sealed class ByteBufferAsIntBufferB
extends IntBuffer
permits ByteBufferAsIntBufferRB {
    protected final ByteBuffer bb;

    ByteBufferAsIntBufferB(ByteBuffer bb, MemorySegment segment) {
        super(-1, 0, bb.remaining() >> 2, bb.remaining() >> 2, segment);
        this.bb = bb;
        int cap = this.capacity();
        this.limit(cap);
        int pos = this.position();
        assert (pos <= cap);
        this.address = bb.address;
    }

    ByteBufferAsIntBufferB(ByteBuffer bb, int mark, int pos, int lim, int cap, long addr, MemorySegment segment) {
        super(mark, pos, lim, cap, segment);
        this.bb = bb;
        this.address = addr;
        assert (this.address >= bb.address);
    }

    @Override
    Object base() {
        return this.bb.hb;
    }

    @Override
    public IntBuffer slice() {
        int lim;
        int pos = this.position();
        int rem = pos <= (lim = this.limit()) ? lim - pos : 0;
        long addr = this.byteOffset(pos);
        return new ByteBufferAsIntBufferB(this.bb, -1, 0, rem, rem, addr, this.segment);
    }

    @Override
    public IntBuffer slice(int index, int length) {
        Objects.checkFromIndexSize(index, length, this.limit());
        return new ByteBufferAsIntBufferB(this.bb, -1, 0, length, length, this.byteOffset(index), this.segment);
    }

    @Override
    public IntBuffer duplicate() {
        return new ByteBufferAsIntBufferB(this.bb, this.markValue(), this.position(), this.limit(), this.capacity(), this.address, this.segment);
    }

    @Override
    public IntBuffer asReadOnlyBuffer() {
        return new ByteBufferAsIntBufferRB(this.bb, this.markValue(), this.position(), this.limit(), this.capacity(), this.address, this.segment);
    }

    private int ix(int i) {
        int off = (int)(this.address - this.bb.address);
        return (i << 2) + off;
    }

    protected long byteOffset(long i) {
        return (i << 2) + this.address;
    }

    @Override
    public int get() {
        int x = SCOPED_MEMORY_ACCESS.getIntUnaligned(this.session(), this.bb.hb, this.byteOffset(this.nextGetIndex()), true);
        return x;
    }

    @Override
    public int get(int i) {
        int x = SCOPED_MEMORY_ACCESS.getIntUnaligned(this.session(), this.bb.hb, this.byteOffset(this.checkIndex(i)), true);
        return x;
    }

    @Override
    public IntBuffer put(int x) {
        int y = x;
        SCOPED_MEMORY_ACCESS.putIntUnaligned(this.session(), this.bb.hb, this.byteOffset(this.nextPutIndex()), y, true);
        return this;
    }

    @Override
    public IntBuffer put(int i, int x) {
        int y = x;
        SCOPED_MEMORY_ACCESS.putIntUnaligned(this.session(), this.bb.hb, this.byteOffset(this.checkIndex(i)), y, true);
        return this;
    }

    @Override
    public IntBuffer compact() {
        int pos = this.position();
        int lim = this.limit();
        assert (pos <= lim);
        int rem = pos <= lim ? lim - pos : 0;
        ByteBuffer db = this.bb.duplicate();
        db.limit(this.ix(lim));
        db.position(this.ix(0));
        ByteBuffer sb = db.slice();
        sb.position(pos << 2);
        sb.compact();
        this.position(rem);
        this.limit(this.capacity());
        this.discardMark();
        return this;
    }

    @Override
    public boolean isDirect() {
        return this.bb.isDirect();
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public ByteOrder order() {
        return ByteOrder.BIG_ENDIAN;
    }
}

