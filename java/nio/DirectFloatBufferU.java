/*
 * Decompiled with CFR 0.152.
 */
package java.nio;

import java.lang.foreign.MemorySegment;
import java.lang.ref.Reference;
import java.nio.Bits;
import java.nio.Buffer;
import java.nio.ByteOrder;
import java.nio.DirectFloatBufferRU;
import java.nio.FloatBuffer;
import java.util.Objects;
import jdk.internal.foreign.MemorySessionImpl;
import jdk.internal.ref.Cleaner;
import sun.nio.ch.DirectBuffer;

sealed class DirectFloatBufferU
extends FloatBuffer
implements DirectBuffer
permits DirectFloatBufferRU {
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

    DirectFloatBufferU(DirectBuffer db, int mark, int pos, int lim, int cap, int off, MemorySegment segment) {
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
    public FloatBuffer slice() {
        int lim;
        int pos = this.position();
        int rem = pos <= (lim = this.limit()) ? lim - pos : 0;
        int off = pos << 2;
        assert (off >= 0);
        return new DirectFloatBufferU(this, -1, 0, rem, rem, off, this.segment);
    }

    @Override
    public FloatBuffer slice(int index, int length) {
        Objects.checkFromIndexSize(index, length, this.limit());
        return new DirectFloatBufferU(this, -1, 0, length, length, index << 2, this.segment);
    }

    @Override
    public FloatBuffer duplicate() {
        return new DirectFloatBufferU(this, this.markValue(), this.position(), this.limit(), this.capacity(), 0, this.segment);
    }

    @Override
    public FloatBuffer asReadOnlyBuffer() {
        return new DirectFloatBufferRU(this, this.markValue(), this.position(), this.limit(), this.capacity(), 0, this.segment);
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
        return this.address + ((long)i << 2);
    }

    @Override
    public float get() {
        try {
            float f = SCOPED_MEMORY_ACCESS.getFloat(this.session(), null, this.ix(this.nextGetIndex()));
            return f;
        }
        finally {
            Reference.reachabilityFence(this);
        }
    }

    @Override
    public float get(int i) {
        try {
            float f = SCOPED_MEMORY_ACCESS.getFloat(this.session(), null, this.ix(this.checkIndex(i)));
            return f;
        }
        finally {
            Reference.reachabilityFence(this);
        }
    }

    @Override
    public FloatBuffer put(float x) {
        try {
            SCOPED_MEMORY_ACCESS.putFloat(this.session(), null, this.ix(this.nextPutIndex()), x);
        }
        finally {
            Reference.reachabilityFence(this);
        }
        return this;
    }

    @Override
    public FloatBuffer put(int i, float x) {
        try {
            SCOPED_MEMORY_ACCESS.putFloat(this.session(), null, this.ix(this.checkIndex(i)), x);
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
    public FloatBuffer compact() {
        int pos = this.position();
        int lim = this.limit();
        assert (pos <= lim);
        int rem = pos <= lim ? lim - pos : 0;
        try {
            SCOPED_MEMORY_ACCESS.copyMemory(this.session(), null, null, this.ix(pos), null, this.ix(0), (long)rem << 2);
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

