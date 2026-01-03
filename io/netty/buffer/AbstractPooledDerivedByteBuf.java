/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.AbstractPooledDerivedByteBuf$PooledNonRetainedDuplicateByteBuf
 *  io.netty.buffer.AbstractPooledDerivedByteBuf$PooledNonRetainedSlicedByteBuf
 */
package io.netty.buffer;

import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.AbstractPooledDerivedByteBuf;
import io.netty.buffer.AbstractReferenceCountedByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.SimpleLeakAwareByteBuf;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.Recycler;
import io.netty.util.internal.ObjectPool;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

abstract class AbstractPooledDerivedByteBuf
extends AbstractReferenceCountedByteBuf {
    private final Recycler.EnhancedHandle<AbstractPooledDerivedByteBuf> recyclerHandle;
    private AbstractByteBuf rootParent;
    private ByteBuf parent;

    AbstractPooledDerivedByteBuf(ObjectPool.Handle<? extends AbstractPooledDerivedByteBuf> recyclerHandle) {
        super(0);
        this.recyclerHandle = (Recycler.EnhancedHandle)recyclerHandle;
    }

    final void parent(ByteBuf newParent) {
        assert (newParent instanceof SimpleLeakAwareByteBuf);
        this.parent = newParent;
    }

    @Override
    public final AbstractByteBuf unwrap() {
        AbstractByteBuf rootParent = this.rootParent;
        if (rootParent == null) {
            throw new IllegalReferenceCountException();
        }
        return rootParent;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final <U extends AbstractPooledDerivedByteBuf> U init(AbstractByteBuf unwrapped, ByteBuf wrapped, int readerIndex, int writerIndex, int maxCapacity) {
        wrapped.retain();
        this.parent = wrapped;
        this.rootParent = unwrapped;
        try {
            this.maxCapacity(maxCapacity);
            this.setIndex0(readerIndex, writerIndex);
            this.resetRefCnt();
            AbstractPooledDerivedByteBuf castThis = this;
            wrapped = null;
            AbstractPooledDerivedByteBuf abstractPooledDerivedByteBuf = castThis;
            return (U)abstractPooledDerivedByteBuf;
        }
        finally {
            if (wrapped != null) {
                this.rootParent = null;
                this.parent = null;
                wrapped.release();
            }
        }
    }

    @Override
    protected final void deallocate() {
        ByteBuf parent = this.parent;
        this.rootParent = null;
        this.parent = null;
        this.recyclerHandle.unguardedRecycle(this);
        parent.release();
    }

    @Override
    public final ByteBufAllocator alloc() {
        return this.unwrap().alloc();
    }

    @Override
    @Deprecated
    public final ByteOrder order() {
        return this.unwrap().order();
    }

    @Override
    public boolean isReadOnly() {
        return this.unwrap().isReadOnly();
    }

    @Override
    public final boolean isDirect() {
        return this.unwrap().isDirect();
    }

    @Override
    public boolean hasArray() {
        return this.unwrap().hasArray();
    }

    @Override
    public byte[] array() {
        return this.unwrap().array();
    }

    @Override
    public boolean hasMemoryAddress() {
        return this.unwrap().hasMemoryAddress();
    }

    @Override
    public boolean isContiguous() {
        return this.unwrap().isContiguous();
    }

    @Override
    public final int nioBufferCount() {
        return this.unwrap().nioBufferCount();
    }

    @Override
    public final ByteBuffer internalNioBuffer(int index, int length) {
        return this.nioBuffer(index, length);
    }

    @Override
    public final ByteBuf retainedSlice() {
        int index = this.readerIndex();
        return this.retainedSlice(index, this.writerIndex() - index);
    }

    @Override
    public ByteBuf slice(int index, int length) {
        this.ensureAccessible();
        return new PooledNonRetainedSlicedByteBuf((ByteBuf)this, this.unwrap(), index, length);
    }

    final ByteBuf duplicate0() {
        this.ensureAccessible();
        return new PooledNonRetainedDuplicateByteBuf((ByteBuf)this, this.unwrap());
    }
}

