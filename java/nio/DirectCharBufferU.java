/*
 * Decompiled with CFR 0.152.
 */
package java.nio;

import java.lang.foreign.MemorySegment;
import java.lang.ref.Reference;
import java.nio.Bits;
import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DirectCharBufferRU;
import java.util.Objects;
import jdk.internal.foreign.MemorySessionImpl;
import jdk.internal.ref.Cleaner;
import sun.nio.ch.DirectBuffer;

sealed class DirectCharBufferU
extends CharBuffer
implements DirectBuffer
permits DirectCharBufferRU {
    protected static final boolean UNALIGNED = Bits.unaligned();
    private final Object att;
    private static final int APPEND_BUF_SIZE = 1024;

    @Override
    public Object attachment() {
        return this.att;
    }

    @Override
    public Cleaner cleaner() {
        return null;
    }

    DirectCharBufferU(DirectBuffer db, int mark, int pos, int lim, int cap, int off, MemorySegment segment) {
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
    public CharBuffer slice() {
        int lim;
        int pos = this.position();
        int rem = pos <= (lim = this.limit()) ? lim - pos : 0;
        int off = pos << 1;
        assert (off >= 0);
        return new DirectCharBufferU(this, -1, 0, rem, rem, off, this.segment);
    }

    @Override
    public CharBuffer slice(int index, int length) {
        Objects.checkFromIndexSize(index, length, this.limit());
        return new DirectCharBufferU(this, -1, 0, length, length, index << 1, this.segment);
    }

    @Override
    public CharBuffer duplicate() {
        return new DirectCharBufferU(this, this.markValue(), this.position(), this.limit(), this.capacity(), 0, this.segment);
    }

    @Override
    public CharBuffer asReadOnlyBuffer() {
        return new DirectCharBufferRU(this, this.markValue(), this.position(), this.limit(), this.capacity(), 0, this.segment);
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
    public char get() {
        try {
            char c = SCOPED_MEMORY_ACCESS.getChar(this.session(), null, this.ix(this.nextGetIndex()));
            return c;
        }
        finally {
            Reference.reachabilityFence(this);
        }
    }

    @Override
    public char get(int i) {
        try {
            char c = SCOPED_MEMORY_ACCESS.getChar(this.session(), null, this.ix(this.checkIndex(i)));
            return c;
        }
        finally {
            Reference.reachabilityFence(this);
        }
    }

    @Override
    char getUnchecked(int i) {
        try {
            char c = SCOPED_MEMORY_ACCESS.getChar(null, null, this.ix(i));
            return c;
        }
        finally {
            Reference.reachabilityFence(this);
        }
    }

    @Override
    public CharBuffer put(char x) {
        try {
            SCOPED_MEMORY_ACCESS.putChar(this.session(), null, this.ix(this.nextPutIndex()), x);
        }
        finally {
            Reference.reachabilityFence(this);
        }
        return this;
    }

    @Override
    public CharBuffer put(int i, char x) {
        try {
            SCOPED_MEMORY_ACCESS.putChar(this.session(), null, this.ix(this.checkIndex(i)), x);
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
    public CharBuffer compact() {
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

    private CharBuffer appendChars(CharSequence csq, int start, int end) {
        Objects.checkFromToIndex(start, end, csq.length());
        int pos = this.position();
        int lim = this.limit();
        int rem = pos <= lim ? lim - pos : 0;
        int length = end - start;
        if (length > rem) {
            throw new BufferOverflowException();
        }
        char[] buf = new char[Math.min(1024, length)];
        int index = pos;
        while (start < end) {
            int count = end - start;
            if (count > buf.length) {
                count = buf.length;
            }
            if (csq instanceof String) {
                String str = (String)csq;
                str.getChars(start, start + count, buf, 0);
            } else if (csq instanceof StringBuilder) {
                StringBuilder sb = (StringBuilder)csq;
                sb.getChars(start, start + count, buf, 0);
            } else if (csq instanceof StringBuffer) {
                StringBuffer sb = (StringBuffer)csq;
                sb.getChars(start, start + count, buf, 0);
            }
            this.putArray(index, buf, 0, count);
            start += count;
            index += count;
        }
        this.position(pos + length);
        return this;
    }

    @Override
    public CharBuffer append(CharSequence csq) {
        if (csq instanceof StringBuilder) {
            return this.appendChars(csq, 0, csq.length());
        }
        return super.append(csq);
    }

    @Override
    public CharBuffer append(CharSequence csq, int start, int end) {
        if (csq instanceof String || csq instanceof StringBuffer || csq instanceof StringBuilder) {
            return this.appendChars(csq, start, end);
        }
        return super.append(csq, start, end);
    }

    @Override
    public CharBuffer subSequence(int start, int end) {
        int pos = this.position();
        int lim = this.limit();
        assert (pos <= lim);
        pos = pos <= lim ? pos : lim;
        int len = lim - pos;
        Objects.checkFromToIndex(start, end, len);
        return new DirectCharBufferU(this, -1, pos + start, pos + end, this.capacity(), this.offset, this.segment);
    }

    @Override
    public ByteOrder order() {
        return ByteOrder.nativeOrder() != ByteOrder.BIG_ENDIAN ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
    }

    @Override
    ByteOrder charRegionOrder() {
        return this.order();
    }
}

