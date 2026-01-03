/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue$ExitCondition
 *  io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue$Supplier
 *  io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue$WaitStrategy
 *  io.netty.util.internal.shaded.org.jctools.queues.atomic.AtomicQueueUtil
 */
package io.netty.util.internal.shaded.org.jctools.queues.atomic;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueueUtil;
import io.netty.util.internal.shaded.org.jctools.queues.atomic.AtomicQueueUtil;
import io.netty.util.internal.shaded.org.jctools.queues.atomic.MpmcAtomicArrayQueueL3Pad;
import io.netty.util.internal.shaded.org.jctools.util.RangeUtil;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class MpmcAtomicArrayQueue<E>
extends MpmcAtomicArrayQueueL3Pad<E> {
    public static final int MAX_LOOK_AHEAD_STEP = Integer.getInteger("jctools.mpmc.max.lookahead.step", 4096);
    private final int lookAheadStep = Math.max(2, Math.min(this.capacity() / 4, MAX_LOOK_AHEAD_STEP));

    public MpmcAtomicArrayQueue(int capacity) {
        super(RangeUtil.checkGreaterThanOrEqual(capacity, 2, "capacity"));
    }

    @Override
    public boolean offer(E e) {
        int seqOffset;
        long pIndex;
        long seq;
        if (null == e) {
            throw new NullPointerException();
        }
        int mask = this.mask;
        long capacity = mask + 1;
        AtomicLongArray sBuffer = this.sequenceBuffer;
        long cIndex = Long.MIN_VALUE;
        do {
            if ((seq = AtomicQueueUtil.lvLongElement((AtomicLongArray)sBuffer, (int)(seqOffset = AtomicQueueUtil.calcCircularLongElementOffset((long)(pIndex = this.lvProducerIndex()), (int)mask)))) >= pIndex) continue;
            if (pIndex - capacity >= cIndex && pIndex - capacity >= (cIndex = this.lvConsumerIndex())) {
                return false;
            }
            seq = pIndex + 1L;
        } while (seq > pIndex || !this.casProducerIndex(pIndex, pIndex + 1L));
        AtomicQueueUtil.spRefElement((AtomicReferenceArray)this.buffer, (int)AtomicQueueUtil.calcCircularRefElementOffset((long)pIndex, (long)mask), e);
        AtomicQueueUtil.soLongElement((AtomicLongArray)sBuffer, (int)seqOffset, (long)(pIndex + 1L));
        return true;
    }

    @Override
    public E poll() {
        int seqOffset;
        long cIndex;
        long expectedSeq;
        long seq;
        AtomicLongArray sBuffer = this.sequenceBuffer;
        int mask = this.mask;
        long pIndex = -1L;
        do {
            if ((seq = AtomicQueueUtil.lvLongElement((AtomicLongArray)sBuffer, (int)(seqOffset = AtomicQueueUtil.calcCircularLongElementOffset((long)(cIndex = this.lvConsumerIndex()), (int)mask)))) >= (expectedSeq = cIndex + 1L)) continue;
            if (cIndex >= pIndex && cIndex == (pIndex = this.lvProducerIndex())) {
                return null;
            }
            seq = expectedSeq + 1L;
        } while (seq > expectedSeq || !this.casConsumerIndex(cIndex, cIndex + 1L));
        int offset = AtomicQueueUtil.calcCircularRefElementOffset((long)cIndex, (long)mask);
        Object e = AtomicQueueUtil.lpRefElement((AtomicReferenceArray)this.buffer, (int)offset);
        AtomicQueueUtil.spRefElement((AtomicReferenceArray)this.buffer, (int)offset, null);
        AtomicQueueUtil.soLongElement((AtomicLongArray)sBuffer, (int)seqOffset, (long)(cIndex + (long)mask + 1L));
        return (E)e;
    }

    @Override
    public E peek() {
        Object e;
        AtomicLongArray sBuffer = this.sequenceBuffer;
        int mask = this.mask;
        long pIndex = -1L;
        while (true) {
            long expectedSeq;
            long cIndex;
            int seqOffset;
            long seq;
            if ((seq = AtomicQueueUtil.lvLongElement((AtomicLongArray)sBuffer, (int)(seqOffset = AtomicQueueUtil.calcCircularLongElementOffset((long)(cIndex = this.lvConsumerIndex()), (int)mask)))) < (expectedSeq = cIndex + 1L)) {
                if (cIndex < pIndex || cIndex != (pIndex = this.lvProducerIndex())) continue;
                return null;
            }
            if (seq != expectedSeq) continue;
            int offset = AtomicQueueUtil.calcCircularRefElementOffset((long)cIndex, (long)mask);
            e = AtomicQueueUtil.lvRefElement((AtomicReferenceArray)this.buffer, (int)offset);
            if (this.lvConsumerIndex() == cIndex) break;
        }
        return (E)e;
    }

    @Override
    public boolean relaxedOffer(E e) {
        int seqOffset;
        long pIndex;
        long seq;
        if (null == e) {
            throw new NullPointerException();
        }
        int mask = this.mask;
        AtomicLongArray sBuffer = this.sequenceBuffer;
        do {
            if ((seq = AtomicQueueUtil.lvLongElement((AtomicLongArray)sBuffer, (int)(seqOffset = AtomicQueueUtil.calcCircularLongElementOffset((long)(pIndex = this.lvProducerIndex()), (int)mask)))) >= pIndex) continue;
            return false;
        } while (seq > pIndex || !this.casProducerIndex(pIndex, pIndex + 1L));
        AtomicQueueUtil.spRefElement((AtomicReferenceArray)this.buffer, (int)AtomicQueueUtil.calcCircularRefElementOffset((long)pIndex, (long)mask), e);
        AtomicQueueUtil.soLongElement((AtomicLongArray)sBuffer, (int)seqOffset, (long)(pIndex + 1L));
        return true;
    }

    @Override
    public E relaxedPoll() {
        int seqOffset;
        long cIndex;
        long expectedSeq;
        long seq;
        AtomicLongArray sBuffer = this.sequenceBuffer;
        int mask = this.mask;
        do {
            if ((seq = AtomicQueueUtil.lvLongElement((AtomicLongArray)sBuffer, (int)(seqOffset = AtomicQueueUtil.calcCircularLongElementOffset((long)(cIndex = this.lvConsumerIndex()), (int)mask)))) >= (expectedSeq = cIndex + 1L)) continue;
            return null;
        } while (seq > expectedSeq || !this.casConsumerIndex(cIndex, cIndex + 1L));
        int offset = AtomicQueueUtil.calcCircularRefElementOffset((long)cIndex, (long)mask);
        Object e = AtomicQueueUtil.lpRefElement((AtomicReferenceArray)this.buffer, (int)offset);
        AtomicQueueUtil.spRefElement((AtomicReferenceArray)this.buffer, (int)offset, null);
        AtomicQueueUtil.soLongElement((AtomicLongArray)sBuffer, (int)seqOffset, (long)(cIndex + (long)mask + 1L));
        return (E)e;
    }

    @Override
    public E relaxedPeek() {
        Object e;
        AtomicLongArray sBuffer = this.sequenceBuffer;
        int mask = this.mask;
        while (true) {
            long expectedSeq;
            long cIndex;
            int seqOffset;
            long seq;
            if ((seq = AtomicQueueUtil.lvLongElement((AtomicLongArray)sBuffer, (int)(seqOffset = AtomicQueueUtil.calcCircularLongElementOffset((long)(cIndex = this.lvConsumerIndex()), (int)mask)))) < (expectedSeq = cIndex + 1L)) {
                return null;
            }
            if (seq != expectedSeq) continue;
            int offset = AtomicQueueUtil.calcCircularRefElementOffset((long)cIndex, (long)mask);
            e = AtomicQueueUtil.lvRefElement((AtomicReferenceArray)this.buffer, (int)offset);
            if (this.lvConsumerIndex() == cIndex) break;
        }
        return (E)e;
    }

    @Override
    public int drain(MessagePassingQueue.Consumer<E> c, int limit) {
        int lookAheadStep;
        if (null == c) {
            throw new IllegalArgumentException("c is null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("limit is negative: " + limit);
        }
        if (limit == 0) {
            return 0;
        }
        AtomicLongArray sBuffer = this.sequenceBuffer;
        int mask = this.mask;
        AtomicReferenceArray buffer = this.buffer;
        int maxLookAheadStep = Math.min(this.lookAheadStep, limit);
        for (int consumed = 0; consumed < limit; consumed += lookAheadStep) {
            long expectedLookAheadSeq;
            int remaining = limit - consumed;
            lookAheadStep = Math.min(remaining, maxLookAheadStep);
            long cIndex = this.lvConsumerIndex();
            long lookAheadIndex = cIndex + (long)lookAheadStep - 1L;
            int lookAheadSeqOffset = AtomicQueueUtil.calcCircularLongElementOffset((long)lookAheadIndex, (int)mask);
            long lookAheadSeq = AtomicQueueUtil.lvLongElement((AtomicLongArray)sBuffer, (int)lookAheadSeqOffset);
            if (lookAheadSeq == (expectedLookAheadSeq = lookAheadIndex + 1L) && this.casConsumerIndex(cIndex, expectedLookAheadSeq)) {
                for (int i = 0; i < lookAheadStep; ++i) {
                    long index = cIndex + (long)i;
                    int seqOffset = AtomicQueueUtil.calcCircularLongElementOffset((long)index, (int)mask);
                    int offset = AtomicQueueUtil.calcCircularRefElementOffset((long)index, (long)mask);
                    long expectedSeq = index + 1L;
                    while (AtomicQueueUtil.lvLongElement((AtomicLongArray)sBuffer, (int)seqOffset) != expectedSeq) {
                    }
                    Object e = AtomicQueueUtil.lpRefElement((AtomicReferenceArray)buffer, (int)offset);
                    AtomicQueueUtil.spRefElement((AtomicReferenceArray)buffer, (int)offset, null);
                    AtomicQueueUtil.soLongElement((AtomicLongArray)sBuffer, (int)seqOffset, (long)(index + (long)mask + 1L));
                    c.accept(e);
                }
                continue;
            }
            if (lookAheadSeq < expectedLookAheadSeq && this.notAvailable(cIndex, mask, sBuffer, cIndex + 1L)) {
                return consumed;
            }
            return consumed + this.drainOneByOne(c, remaining);
        }
        return limit;
    }

    private int drainOneByOne(MessagePassingQueue.Consumer<E> c, int limit) {
        AtomicLongArray sBuffer = this.sequenceBuffer;
        int mask = this.mask;
        AtomicReferenceArray buffer = this.buffer;
        for (int i = 0; i < limit; ++i) {
            int seqOffset;
            long cIndex;
            long expectedSeq;
            long seq;
            do {
                if ((seq = AtomicQueueUtil.lvLongElement((AtomicLongArray)sBuffer, (int)(seqOffset = AtomicQueueUtil.calcCircularLongElementOffset((long)(cIndex = this.lvConsumerIndex()), (int)mask)))) >= (expectedSeq = cIndex + 1L)) continue;
                return i;
            } while (seq > expectedSeq || !this.casConsumerIndex(cIndex, cIndex + 1L));
            int offset = AtomicQueueUtil.calcCircularRefElementOffset((long)cIndex, (long)mask);
            Object e = AtomicQueueUtil.lpRefElement((AtomicReferenceArray)buffer, (int)offset);
            AtomicQueueUtil.spRefElement((AtomicReferenceArray)buffer, (int)offset, null);
            AtomicQueueUtil.soLongElement((AtomicLongArray)sBuffer, (int)seqOffset, (long)(cIndex + (long)mask + 1L));
            c.accept(e);
        }
        return limit;
    }

    @Override
    public int fill(MessagePassingQueue.Supplier<E> s, int limit) {
        int lookAheadStep;
        if (null == s) {
            throw new IllegalArgumentException("supplier is null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("limit is negative:" + limit);
        }
        if (limit == 0) {
            return 0;
        }
        AtomicLongArray sBuffer = this.sequenceBuffer;
        int mask = this.mask;
        AtomicReferenceArray buffer = this.buffer;
        int maxLookAheadStep = Math.min(this.lookAheadStep, limit);
        for (int produced = 0; produced < limit; produced += lookAheadStep) {
            long expectedLookAheadSeq;
            int remaining = limit - produced;
            lookAheadStep = Math.min(remaining, maxLookAheadStep);
            long pIndex = this.lvProducerIndex();
            long lookAheadIndex = pIndex + (long)lookAheadStep - 1L;
            int lookAheadSeqOffset = AtomicQueueUtil.calcCircularLongElementOffset((long)lookAheadIndex, (int)mask);
            long lookAheadSeq = AtomicQueueUtil.lvLongElement((AtomicLongArray)sBuffer, (int)lookAheadSeqOffset);
            if (lookAheadSeq == (expectedLookAheadSeq = lookAheadIndex) && this.casProducerIndex(pIndex, expectedLookAheadSeq + 1L)) {
                for (int i = 0; i < lookAheadStep; ++i) {
                    long index = pIndex + (long)i;
                    int seqOffset = AtomicQueueUtil.calcCircularLongElementOffset((long)index, (int)mask);
                    int offset = AtomicQueueUtil.calcCircularRefElementOffset((long)index, (long)mask);
                    while (AtomicQueueUtil.lvLongElement((AtomicLongArray)sBuffer, (int)seqOffset) != index) {
                    }
                    AtomicQueueUtil.soRefElement((AtomicReferenceArray)buffer, (int)offset, (Object)s.get());
                    AtomicQueueUtil.soLongElement((AtomicLongArray)sBuffer, (int)seqOffset, (long)(index + 1L));
                }
                continue;
            }
            if (lookAheadSeq < expectedLookAheadSeq && this.notAvailable(pIndex, mask, sBuffer, pIndex)) {
                return produced;
            }
            return produced + this.fillOneByOne(s, remaining);
        }
        return limit;
    }

    private boolean notAvailable(long index, int mask, AtomicLongArray sBuffer, long expectedSeq) {
        int seqOffset = AtomicQueueUtil.calcCircularLongElementOffset((long)index, (int)mask);
        long seq = AtomicQueueUtil.lvLongElement((AtomicLongArray)sBuffer, (int)seqOffset);
        return seq < expectedSeq;
    }

    private int fillOneByOne(MessagePassingQueue.Supplier<E> s, int limit) {
        AtomicLongArray sBuffer = this.sequenceBuffer;
        int mask = this.mask;
        AtomicReferenceArray buffer = this.buffer;
        for (int i = 0; i < limit; ++i) {
            int seqOffset;
            long pIndex;
            long seq;
            do {
                if ((seq = AtomicQueueUtil.lvLongElement((AtomicLongArray)sBuffer, (int)(seqOffset = AtomicQueueUtil.calcCircularLongElementOffset((long)(pIndex = this.lvProducerIndex()), (int)mask)))) >= pIndex) continue;
                return i;
            } while (seq > pIndex || !this.casProducerIndex(pIndex, pIndex + 1L));
            AtomicQueueUtil.soRefElement((AtomicReferenceArray)buffer, (int)AtomicQueueUtil.calcCircularRefElementOffset((long)pIndex, (long)mask), (Object)s.get());
            AtomicQueueUtil.soLongElement((AtomicLongArray)sBuffer, (int)seqOffset, (long)(pIndex + 1L));
        }
        return limit;
    }

    @Override
    public int drain(MessagePassingQueue.Consumer<E> c) {
        return MessagePassingQueueUtil.drain(this, c);
    }

    @Override
    public int fill(MessagePassingQueue.Supplier<E> s) {
        return MessagePassingQueueUtil.fillBounded(this, s);
    }

    @Override
    public void drain(MessagePassingQueue.Consumer<E> c, MessagePassingQueue.WaitStrategy w, MessagePassingQueue.ExitCondition exit) {
        MessagePassingQueueUtil.drain(this, c, w, exit);
    }

    @Override
    public void fill(MessagePassingQueue.Supplier<E> s, MessagePassingQueue.WaitStrategy wait, MessagePassingQueue.ExitCondition exit) {
        MessagePassingQueueUtil.fill(this, s, wait, exit);
    }
}

