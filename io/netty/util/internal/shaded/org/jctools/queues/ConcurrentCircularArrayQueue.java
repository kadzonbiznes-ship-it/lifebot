/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.util.internal.shaded.org.jctools.queues.ConcurrentCircularArrayQueue$WeakIterator
 *  io.netty.util.internal.shaded.org.jctools.queues.IndexedQueueSizeUtil
 */
package io.netty.util.internal.shaded.org.jctools.queues;

import io.netty.util.internal.shaded.org.jctools.queues.ConcurrentCircularArrayQueue;
import io.netty.util.internal.shaded.org.jctools.queues.ConcurrentCircularArrayQueueL0Pad;
import io.netty.util.internal.shaded.org.jctools.queues.IndexedQueueSizeUtil;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import io.netty.util.internal.shaded.org.jctools.queues.QueueProgressIndicators;
import io.netty.util.internal.shaded.org.jctools.queues.SupportsIterator;
import io.netty.util.internal.shaded.org.jctools.util.Pow2;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeRefArrayAccess;
import java.util.Iterator;

abstract class ConcurrentCircularArrayQueue<E>
extends ConcurrentCircularArrayQueueL0Pad<E>
implements MessagePassingQueue<E>,
IndexedQueueSizeUtil.IndexedQueue,
QueueProgressIndicators,
SupportsIterator {
    protected final long mask;
    protected final E[] buffer;

    ConcurrentCircularArrayQueue(int capacity) {
        int actualCapacity = Pow2.roundToPowerOfTwo(capacity);
        this.mask = actualCapacity - 1;
        this.buffer = UnsafeRefArrayAccess.allocateRefArray(actualCapacity);
    }

    @Override
    public int size() {
        return IndexedQueueSizeUtil.size((IndexedQueueSizeUtil.IndexedQueue)this, (int)1);
    }

    @Override
    public boolean isEmpty() {
        return IndexedQueueSizeUtil.isEmpty((IndexedQueueSizeUtil.IndexedQueue)this);
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

    @Override
    public void clear() {
        while (this.poll() != null) {
        }
    }

    @Override
    public int capacity() {
        return (int)(this.mask + 1L);
    }

    @Override
    public long currentProducerIndex() {
        return this.lvProducerIndex();
    }

    @Override
    public long currentConsumerIndex() {
        return this.lvConsumerIndex();
    }

    @Override
    public Iterator<E> iterator() {
        long cIndex = this.lvConsumerIndex();
        long pIndex = this.lvProducerIndex();
        return new WeakIterator(cIndex, pIndex, this.mask, (Object[])this.buffer);
    }
}

