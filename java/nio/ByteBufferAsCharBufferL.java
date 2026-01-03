/*
 * Decompiled with CFR 0.152.
 */
package java.nio;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.ByteBufferAsCharBufferRL;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.util.Objects;

sealed class ByteBufferAsCharBufferL
extends CharBuffer
permits ByteBufferAsCharBufferRL {
    protected final ByteBuffer bb;

    ByteBufferAsCharBufferL(ByteBuffer bb, MemorySegment segment) {
        super(-1, 0, bb.remaining() >> 1, bb.remaining() >> 1, segment);
        this.bb = bb;
        int cap = this.capacity();
        this.limit(cap);
        int pos = this.position();
        assert (pos <= cap);
        this.address = bb.address;
    }

    ByteBufferAsCharBufferL(ByteBuffer bb, int mark, int pos, int lim, int cap, long addr, MemorySegment segment) {
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
    public CharBuffer slice() {
        int lim;
        int pos = this.position();
        int rem = pos <= (lim = this.limit()) ? lim - pos : 0;
        long addr = this.byteOffset(pos);
        return new ByteBufferAsCharBufferL(this.bb, -1, 0, rem, rem, addr, this.segment);
    }

    @Override
    public CharBuffer slice(int index, int length) {
        Objects.checkFromIndexSize(index, length, this.limit());
        return new ByteBufferAsCharBufferL(this.bb, -1, 0, length, length, this.byteOffset(index), this.segment);
    }

    @Override
    public CharBuffer duplicate() {
        return new ByteBufferAsCharBufferL(this.bb, this.markValue(), this.position(), this.limit(), this.capacity(), this.address, this.segment);
    }

    @Override
    public CharBuffer asReadOnlyBuffer() {
        return new ByteBufferAsCharBufferRL(this.bb, this.markValue(), this.position(), this.limit(), this.capacity(), this.address, this.segment);
    }

    private int ix(int i) {
        int off = (int)(this.address - this.bb.address);
        return (i << 1) + off;
    }

    protected long byteOffset(long i) {
        return (i << 1) + this.address;
    }

    @Override
    public char get() {
        char x = SCOPED_MEMORY_ACCESS.getCharUnaligned(this.session(), this.bb.hb, this.byteOffset(this.nextGetIndex()), false);
        return x;
    }

    @Override
    public char get(int i) {
        char x = SCOPED_MEMORY_ACCESS.getCharUnaligned(this.session(), this.bb.hb, this.byteOffset(this.checkIndex(i)), false);
        return x;
    }

    @Override
    char getUnchecked(int i) {
        char x = SCOPED_MEMORY_ACCESS.getCharUnaligned(null, this.bb.hb, this.byteOffset(i), false);
        return x;
    }

    @Override
    public CharBuffer put(char x) {
        char y = x;
        SCOPED_MEMORY_ACCESS.putCharUnaligned(this.session(), this.bb.hb, this.byteOffset(this.nextPutIndex()), y, false);
        return this;
    }

    @Override
    public CharBuffer put(int i, char x) {
        char y = x;
        SCOPED_MEMORY_ACCESS.putCharUnaligned(this.session(), this.bb.hb, this.byteOffset(this.checkIndex(i)), y, false);
        return this;
    }

    @Override
    public CharBuffer compact() {
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
    public String toString(int start, int end) {
        Objects.checkFromToIndex(start, end, this.limit());
        int len = end - start;
        char[] ca = new char[len];
        CharBuffer cb = CharBuffer.wrap(ca);
        CharBuffer db = this.duplicate();
        db.position(start);
        db.limit(end);
        cb.put(db);
        return new String(ca);
    }

    @Override
    public CharBuffer subSequence(int start, int end) {
        int pos = this.position();
        int lim = this.limit();
        assert (pos <= lim);
        pos = pos <= lim ? pos : lim;
        int len = lim - pos;
        Objects.checkFromToIndex(start, end, len);
        return new ByteBufferAsCharBufferL(this.bb, -1, pos + start, pos + end, this.capacity(), this.address, this.segment);
    }

    @Override
    public ByteOrder order() {
        return ByteOrder.LITTLE_ENDIAN;
    }

    @Override
    ByteOrder charRegionOrder() {
        return this.order();
    }
}

