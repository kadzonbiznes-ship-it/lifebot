/*
 * Decompiled with CFR 0.152.
 */
package java.nio;

import java.lang.foreign.MemorySegment;
import java.lang.ref.Reference;
import java.nio.Bits;
import java.nio.Buffer;
import java.nio.ByteOrder;
import java.nio.DirectShortBufferRU;
import java.nio.ShortBuffer;
import java.util.Objects;
import jdk.internal.foreign.MemorySessionImpl;
import jdk.internal.ref.Cleaner;
import sun.nio.ch.DirectBuffer;

sealed class DirectShortBufferU
extends ShortBuffer
implements DirectBuffer
permits DirectShortBufferRU {
    protected static final boolean UNALIGNED = Bits.unaligned();
    private final Object att;

    @Override
    public Object attachment() {
        return this.att;
    }

    @Override
    public Cleaner cleaner() {
        return null;
    }

    DirectShortBufferU(DirectBuffer db, int mark, int pos, int lim, int cap, int off, MemorySegment segment) {
        super(mark, pos, lim, cap, segment);
        this.address = ((Buffer)((Object)db)).address + (long)off;
        Object attachment = db.attachment();
        this.att = attachment == null ? db : attachment;
    }

    @Override
    Object base() {
        return null;
    }

    @Override
    public ShortBuffer slice() {
        int lim;
        int pos = this.position();
        int rem = pos <= (lim = this.limit()) ? lim - pos : 0;
        int off = pos << 1;
        assert (off >= 0);
        return new DirectShortBufferU(this, -1, 0, rem, rem, off, this.segment);
    }

    @Override
    public ShortBuffer slice(int index, int length) {
        Objects.checkFromIndexSize(index, length, this.limit());
        return new DirectShortBufferU(this, -1, 0, length, length, index << 1, this.segment);
    }

    @Override
    public ShortBuffer duplicate() {
        return new DirectShortBufferU(this, this.markValue(), this.position(), this.limit(), this.capacity(), 0, this.segment);
    }

    @Override
    public ShortBuffer asReadOnlyBuffer() {
        return new DirectShortBufferRU(this, this.markValue(), this.position(), this.limit(), this.capacity(), 0, this.segment);
    }

    @Override
    public long address() {
        MemorySessionImpl session = this.session();
        if (session != null) {
            if (session.ownerThread() == null && session.isCloseable()) {
                throw new UnsupportedOperationException("ByteBuffer derived from closeable shared sessions not supported");
            }
            session.checkValidState();
        }
        return this.address;
    }

    private long ix(int i) {
        return this.address + ((long)i << 1);
    }

    @Override
    public short get() {
        try {
            short s = SCOPED_MEMORY_ACCESS.getShort(this.session(), null, this.ix(this.nextGetIndex()));
            return s;
        }
        finally {
            Reference.reachabilityFence(this);
        }
    }

    @Override
    public short get(int i) {
        try {
            short s = SCOPED_MEMORY_ACCESS.getShort(this.session(), null, this.ix(this.checkIndex(i)));
            return s;
        }
        finally {
            Reference.reachabilityFence(this);
        }
    }

    @Override
    public ShortBuffer put(short x) {
        try {
            SCOPED_MEMORY_ACCESS.putShort(this.session(), null, this.ix(this.nextPutIndex()), x);
        }
        finally {
            Reference.reachabilityFence(this);
        }
        return this;
    }

    @Override
    public ShortBuffer put(int i, short x) {
        try {
            SCOPED_MEMORY_ACCESS.putShort(this.session(), null, this.ix(this.checkIndex(i)), x);
        }
        finally {
            Reference.reachabilityFence(this);
        }
        return this;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ShortBuffer compact() {
        int pos = this.position();
        int lim = this.limit();
        assert (pos <= lim);
        int rem = pos <= lim ? lim - pos : 0;
        try {
            SCOPED_MEMORY_ACCESS.copyMemory(this.session(), null, null, this.ix(pos), null, this.ix(0), (long)rem << 1);
        }
        finally {
            Reference.reachabilityFence(this);
        }
        this.position(rem);
        this.limit(this.capacity());
        this.discardMark();
        return this;
    }

    @Override
    public boolean isDirect() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public ByteOrder order() {
        return ByteOrder.nativeOrder() != ByteOrder.BIG_ENDIAN ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
    }
}

