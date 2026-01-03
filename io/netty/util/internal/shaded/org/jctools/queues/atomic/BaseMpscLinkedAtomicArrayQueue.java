/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.util.internal.shaded.org.jctools.queues.IndexedQueueSizeUtil
 *  io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue$ExitCondition
 *  io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue$Supplier
 *  io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue$WaitStrategy
 *  io.netty.util.internal.shaded.org.jctools.queues.atomic.AtomicQueueUtil
 *  io.netty.util.internal.shaded.org.jctools.queues.atomic.BaseMpscLinkedAtomicArrayQueue$WeakIterator
 *  io.netty.util.internal.shaded.org.jctools.util.PortableJvmInfo
 */
package io.netty.util.internal.shaded.org.jctools.queues.atomic;

import io.netty.util.internal.shaded.org.jctools.queues.IndexedQueueSizeUtil;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueueUtil;
import io.netty.util.internal.shaded.org.jctools.queues.QueueProgressIndicators;
import io.netty.util.internal.shaded.org.jctools.queues.atomic.AtomicQueueUtil;
import io.netty.util.internal.shaded.org.jctools.queues.atomic.BaseMpscLinkedAtomicArrayQueue;
import io.netty.util.internal.shaded.org.jctools.queues.atomic.BaseMpscLinkedAtomicArrayQueueColdProducerFields;
import io.netty.util.internal.shaded.org.jctools.util.PortableJvmInfo;
import io.netty.util.internal.shaded.org.jctools.util.Pow2;
import io.netty.util.internal.shaded.org.jctools.util.RangeUtil;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReferenceArray;

abstract class BaseMpscLinkedAtomicArrayQueue<E>
extends BaseMpscLinkedAtomicArrayQueueColdProducerFields<E>
implements MessagePassingQueue<E>,
QueueProgressIndicators {
    private static final Object JUMP = new Object();
    private static final Object BUFFER_CONSUMED = new Object();
    private static final int CONTINUE_TO_P_INDEX_CAS = 0;
    private static final int RETRY = 1;
    private static final int QUEUE_FULL = 2;
    private static final int QUEUE_RESIZE = 3;

    public BaseMpscLinkedAtomicArrayQueue(int initialCapacity) {
        AtomicReferenceArray buffer;
        RangeUtil.checkGreaterThanOrEqual(initialCapacity, 2, "initialCapacity");
        int p2capacity = Pow2.roundToPowerOfTwo(initialCapacity);
        long mask = p2capacity - 1 << 1;
        this.producerBuffer = buffer = AtomicQueueUtil.allocateRefArray((int)(p2capacity + 1));
        this.producerMask = mask;
        this.consumerBuffer = buffer;
        this.consumerMask = mask;
        this.soProducerLimit(mask);
    }

    @Override
    public int size() {
        return IndexedQueueSizeUtil.size((IndexedQueueSizeUtil.IndexedQueue)this, (int)2);
    }

    @Override
    public boolean isEmpty() {
        return (this.lvConsumerIndex() - this.lvProducerIndex()) / 2L == 0L;
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

    @Override
    public boolean offer(E e) {
        AtomicReferenceArray buffer;
        long mask;
        long pIndex;
        if (null == e) {
            throw new NullPointerException();
        }
        block6: while (true) {
            long producerLimit = this.lvProducerLimit();
            pIndex = this.lvProducerIndex();
            if ((pIndex & 1L) == 1L) continue;
            mask = this.producerMask;
            buffer = this.producerBuffer;
            if (producerLimit <= pIndex) {
                int result = this.offerSlowPath(mask, pIndex, producerLimit);
                switch (result) {
                    case 0: {
                        break;
                    }
                    case 1: {
                        continue block6;
                    }
                    case 2: {
                        return false;
                    }
                    case 3: {
                        this.resize(mask, buffer, pIndex, e, null);
                        return true;
                    }
                }
            }
            if (this.casProducerIndex(pIndex, pIndex + 2L)) break;
        }
        int offset = AtomicQueueUtil.modifiedCalcCircularRefElementOffset((long)pIndex, (long)mask);
        AtomicQueueUtil.soRefElement((AtomicReferenceArray)buffer, (int)offset, e);
        return true;
    }

    @Override
    public E poll() {
        long mask;
        AtomicReferenceArray buffer = this.consumerBuffer;
        long cIndex = this.lpConsumerIndex();
        int offset = AtomicQueueUtil.modifiedCalcCircularRefElementOffset((long)cIndex, (long)(mask = this.consumerMask));
        Object e = AtomicQueueUtil.lvRefElement((AtomicReferenceArray)buffer, (int)offset);
        if (e == null) {
            long pIndex = this.lvProducerIndex();
            if ((cIndex - pIndex) / 2L == 0L) {
                return null;
            }
            while ((e = AtomicQueueUtil.lvRefElement((AtomicReferenceArray)buffer, (int)offset)) == null) {
            }
        }
        if (e == JUMP) {
            AtomicReferenceArray<E> nextBuffer = this.nextBuffer(buffer, mask);
            return this.newBufferPoll(nextBuffer, cIndex);
        }
        AtomicQueueUtil.soRefElement((AtomicReferenceArray)buffer, (int)offset, null);
        this.soConsumerIndex(cIndex + 2L);
        return (E)e;
    }

    @Override
    public E peek() {
        long mask;
        AtomicReferenceArray buffer = this.consumerBuffer;
        long cIndex = this.lpConsumerIndex();
        int offset = AtomicQueueUtil.modifiedCalcCircularRefElementOffset((long)cIndex, (long)(mask = this.consumerMask));
        Object e = AtomicQueueUtil.lvRefElement((AtomicReferenceArray)buffer, (int)offset);
        if (e == null) {
            long pIndex = this.lvProducerIndex();
            if ((cIndex - pIndex) / 2L == 0L) {
                return null;
            }
            while ((e = AtomicQueueUtil.lvRefElement((AtomicReferenceArray)buffer, (int)offset)) == null) {
            }
        }
        if (e == JUMP) {
            return this.newBufferPeek(this.nextBuffer(buffer, mask), cIndex);
        }
        return (E)e;
    }

    private int offerSlowPath(long mask, long pIndex, long producerLimit) {
        long bufferCapacity;
        long cIndex = this.lvConsumerIndex();
        if (cIndex + (bufferCapacity = this.getCurrentBufferCapacity(mask)) > pIndex) {
            if (!this.casProducerLimit(producerLimit, cIndex + bufferCapacity)) {
                return 1;
            }
            return 0;
        }
        if (this.availableInQueue(pIndex, cIndex) <= 0L) {
            return 2;
        }
        if (this.casProducerIndex(pIndex, pIndex + 1L)) {
            return 3;
        }
        return 1;
    }

    protected abstract long availableInQueue(long var1, long var3);

    private AtomicReferenceArray<E> nextBuffer(AtomicReferenceArray<E> buffer, long mask) {
        AtomicReferenceArray nextBuffer;
        int offset = BaseMpscLinkedAtomicArrayQueue.nextArrayOffset(mask);
        this.consumerBuffer = nextBuffer = (AtomicReferenceArray)AtomicQueueUtil.lvRefElement(buffer, (int)offset);
        this.consumerMask = AtomicQueueUtil.length((AtomicReferenceArray)nextBuffer) - 2 << 1;
        AtomicQueueUtil.soRefElement(buffer, (int)offset, (Object)BUFFER_CONSUMED);
        return nextBuffer;
    }

    private static int nextArrayOffset(long mask) {
        return AtomicQueueUtil.modifiedCalcCircularRefElementOffset((long)(mask + 2L), (long)Long.MAX_VALUE);
    }

    private E newBufferPoll(AtomicReferenceArray<E> nextBuffer, long cIndex) {
        int offset = AtomicQueueUtil.modifiedCalcCircularRefElementOffset((long)cIndex, (long)this.consumerMask);
        Object n = AtomicQueueUtil.lvRefElement(nextBuffer, (int)offset);
        if (n == null) {
            throw new IllegalStateException("new buffer must have at least one element");
        }
        AtomicQueueUtil.soRefElement(nextBuffer, (int)offset, null);
        this.soConsumerIndex(cIndex + 2L);
        return (E)n;
    }

    private E newBufferPeek(AtomicReferenceArray<E> nextBuffer, long cIndex) {
        int offset = AtomicQueueUtil.modifiedCalcCircularRefElementOffset((long)cIndex, (long)this.consumerMask);
        Object n = AtomicQueueUtil.lvRefElement(nextBuffer, (int)offset);
        if (null == n) {
            throw new IllegalStateException("new buffer must have at least one element");
        }
        return (E)n;
    }

    @Override
    public long currentProducerIndex() {
        return this.lvProducerIndex() / 2L;
    }

    @Override
    public long currentConsumerIndex() {
        return this.lvConsumerIndex() / 2L;
    }

    @Override
    public abstract int capacity();

    @Override
    public boolean relaxedOffer(E e) {
        return this.offer(e);
    }

    @Override
    public E relaxedPoll() {
        long mask;
        AtomicReferenceArray buffer = this.consumerBuffer;
        long cIndex = this.lpConsumerIndex();
        int offset = AtomicQueueUtil.modifiedCalcCircularRefElementOffset((long)cIndex, (long)(mask = this.consumerMask));
        Object e = AtomicQueueUtil.lvRefElement((AtomicReferenceArray)buffer, (int)offset);
        if (e == null) {
            return null;
        }
        if (e == JUMP) {
            AtomicReferenceArray<E> nextBuffer = this.nextBuffer(buffer, mask);
            return this.newBufferPoll(nextBuffer, cIndex);
        }
        AtomicQueueUtil.soRefElement((AtomicReferenceArray)buffer, (int)offset, null);
        this.soConsumerIndex(cIndex + 2L);
        return (E)e;
    }

    @Override
    public E relaxedPeek() {
        long mask;
        AtomicReferenceArray buffer = this.consumerBuffer;
        long cIndex = this.lpConsumerIndex();
        int offset = AtomicQueueUtil.modifiedCalcCircularRefElementOffset((long)cIndex, (long)(mask = this.consumerMask));
        Object e = AtomicQueueUtil.lvRefElement((AtomicReferenceArray)buffer, (int)offset);
        if (e == JUMP) {
            return this.newBufferPeek(this.nextBuffer(buffer, mask), cIndex);
        }
        return (E)e;
    }

    @Override
    public int fill(MessagePassingQueue.Supplier<E> s) {
        int filled;
        long result = 0L;
        int capacity = this.capacity();
        do {
            if ((filled = this.fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH)) != 0) continue;
            return (int)result;
        } while ((result += (long)filled) <= (long)capacity);
        return (int)result;
    }

    @Override
    public int fill(MessagePassingQueue.Supplier<E> s, int limit) {
        long batchIndex;
        AtomicReferenceArray buffer;
        long mask;
        long pIndex;
        if (null == s) {
            throw new IllegalArgumentException("supplier is null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("limit is negative:" + limit);
        }
        if (limit == 0) {
            return 0;
        }
        block5: while (true) {
            long producerLimit = this.lvProducerLimit();
            pIndex = this.lvProducerIndex();
            if ((pIndex & 1L) == 1L) continue;
            mask = this.producerMask;
            buffer = this.producerBuffer;
            batchIndex = Math.min(producerLimit, pIndex + 2L * (long)limit);
            if (pIndex >= producerLimit) {
                int result = this.offerSlowPath(mask, pIndex, producerLimit);
                switch (result) {
                    case 0: 
                    case 1: {
                        continue block5;
                    }
                    case 2: {
                        return 0;
                    }
                    case 3: {
                        this.resize(mask, buffer, pIndex, null, s);
                        return 1;
                    }
                }
            }
            if (this.casProducerIndex(pIndex, batchIndex)) break;
        }
        int claimedSlots = (int)((batchIndex - pIndex) / 2L);
        for (int i = 0; i < claimedSlots; ++i) {
            int offset = AtomicQueueUtil.modifiedCalcCircularRefElementOffset((long)(pIndex + 2L * (long)i), (long)mask);
            AtomicQueueUtil.soRefElement((AtomicReferenceArray)buffer, (int)offset, (Object)s.get());
        }
        return claimedSlots;
    }

    @Override
    public void fill(MessagePassingQueue.Supplier<E> s, MessagePassingQueue.WaitStrategy wait, MessagePassingQueue.ExitCondition exit) {
        MessagePassingQueueUtil.fill(this, s, wait, exit);
    }

    @Override
    public int drain(MessagePassingQueue.Consumer<E> c) {
        return this.drain(c, this.capacity());
    }

    @Override
    public int drain(MessagePassingQueue.Consumer<E> c, int limit) {
        return MessagePassingQueueUtil.drain(this, c, limit);
    }

    @Override
    public void drain(MessagePassingQueue.Consumer<E> c, MessagePassingQueue.WaitStrategy wait, MessagePassingQueue.ExitCondition exit) {
        MessagePassingQueueUtil.drain(this, c, wait, exit);
    }

    @Override
    public Iterator<E> iterator() {
        return new WeakIterator(this.consumerBuffer, this.lvConsumerIndex(), this.lvProducerIndex());
    }

    private void resize(long oldMask, AtomicReferenceArray<E> oldBuffer, long pIndex, E e, MessagePassingQueue.Supplier<E> s) {
        AtomicReferenceArray newBuffer;
        assert (e != null && s == null || e == null || s != null);
        int newBufferLength = this.getNextBufferSize(oldBuffer);
        try {
            newBuffer = AtomicQueueUtil.allocateRefArray((int)newBufferLength);
        }
        catch (OutOfMemoryError oom) {
            assert (this.lvProducerIndex() == pIndex + 1L);
            this.soProducerIndex(pIndex);
            throw oom;
        }
        this.producerBuffer = newBuffer;
        int newMask = newBufferLength - 2 << 1;
        this.producerMask = newMask;
        int offsetInOld = AtomicQueueUtil.modifiedCalcCircularRefElementOffset((long)pIndex, (long)oldMask);
        int offsetInNew = AtomicQueueUtil.modifiedCalcCircularRefElementOffset((long)pIndex, (long)newMask);
        AtomicQueueUtil.soRefElement((AtomicReferenceArray)newBuffer, (int)offsetInNew, e == null ? s.get() : e);
        AtomicQueueUtil.soRefElement(oldBuffer, (int)BaseMpscLinkedAtomicArrayQueue.nextArrayOffset(oldMask), (Object)newBuffer);
        long cIndex = this.lvConsumerIndex();
        long availableInQueue = this.availableInQueue(pIndex, cIndex);
        RangeUtil.checkPositive(availableInQueue, "availableInQueue");
        this.soProducerLimit(pIndex + Math.min((long)newMask, availableInQueue));
        this.soProducerIndex(pIndex + 2L);
        AtomicQueueUtil.soRefElement(oldBuffer, (int)offsetInOld, (Object)JUMP);
    }

    protected abstract int getNextBufferSize(AtomicReferenceArray<E> var1);

    protected abstract long getCurrentBufferCapacity(long var1);

    static /* synthetic */ Object access$000() {
        return JUMP;
    }

    static /* synthetic */ Object access$100() {
        return BUFFER_CONSUMED;
    }
}

