/*
 * Decompiled with CFR 0.152.
 */
package java.nio;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.ByteBufferAsShortBufferRB;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Objects;

sealed class ByteBufferAsShortBufferB
extends ShortBuffer
permits ByteBufferAsShortBufferRB {
    protected final ByteBuffer bb;

    ByteBufferAsShortBufferB(ByteBuffer bb, MemorySegment segment) {
        super(-1, 0, bb.remaining() >> 1, bb.remaining() >> 1, segment);
        this.bb = bb;
        int cap = this.capacity();
        this.limit(cap);
        int pos = this.position();
        assert (pos <= cap);
        this.address = bb.address;
    }

    ByteBufferAsShortBufferB(ByteBuffer bb, int mark, int pos, int lim, int cap, long addr, MemorySegment segment) {
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
    public ShortBuffer slice() {
        int lim;
        int pos = this.position();
        int rem = pos <= (lim = this.limit()) ? lim - pos : 0;
        long addr = this.byteOffset(pos);
        return new ByteBufferAsShortBufferB(this.bb, -1, 0, rem, rem, addr, this.segment);
    }

    @Override
    public ShortBuffer slice(int index, int length) {
        Objects.checkFromIndexSize(index, length, this.limit());
        return new ByteBufferAsShortBufferB(this.bb, -1, 0, length, length, this.byteOffset(index), this.segment);
    }

    @Override
    public ShortBuffer duplicate() {
        return new ByteBufferAsShortBufferB(this.bb, this.markValue(), this.position(), this.limit(), this.capacity(), this.address, this.segment);
    }

    @Override
    public ShortBuffer asReadOnlyBuffer() {
        return new ByteBufferAsShortBufferRB(this.bb, this.markValue(), this.position(), this.limit(), this.capacity(), this.address, this.segment);
    }

    private int ix(int i) {
        int off = (int)(this.address - this.bb.address);
        return (i << 1) + off;
    }

    protected long byteOffset(long i) {
        return (i << 1) + this.address;
    }

    @Override
    public short get() {
        short x = SCOPED_MEMORY_ACCESS.getShortUnaligned(this.session(), this.bb.hb, this.byteOffset(this.nextGetIndex()), true);
        return x;
    }

    @Override
    public short get(int i) {
        short x = SCOPED_MEMORY_ACCESS.getShortUnaligned(this.session(), this.bb.hb, this.byteOffset(this.checkIndex(i)), true);
        return x;
    }

    @Override
    public ShortBuffer put(short x) {
        short y = x;
        SCOPED_MEMORY_ACCESS.putShortUnaligned(this.session(), this.bb.hb, this.byteOffset(this.nextPutIndex()), y, true);
        return this;
    }

    @Override
    public ShortBuffer put(int i, short x) {
        short y = x;
        SCOPED_MEMORY_ACCESS.putShortUnaligned(this.session(), this.bb.hb, this.byteOffset(this.checkIndex(i)), y, true);
        return this;
    }

    @Override
    public ShortBuffer compact() {
        int pos = this.position();
        int lim = this.limit();
        assert (pos <= lim);
        int rem = pos <= lim ? lim - pos : 0;
        ByteBuffer db = this.bb.duplicate();
        db.limit(this.ix(lim));
        db.position(this.ix(0));
        ByteBuffer sb = db.slice();
        sb.position(pos << 1);
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

