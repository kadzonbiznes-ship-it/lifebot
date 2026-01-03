/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.util.internal.shaded.org.jctools.queues.IndexedQueueSizeUtil
 *  io.netty.util.internal.shaded.org.jctools.queues.atomic.AtomicReferenceArrayQueue$WeakIterator
 */
package io.netty.util.internal.shaded.org.jctools.queues.atomic;

import io.netty.util.internal.shaded.org.jctools.queues.IndexedQueueSizeUtil;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import io.netty.util.internal.shaded.org.jctools.queues.QueueProgressIndicators;
import io.netty.util.internal.shaded.org.jctools.queues.SupportsIterator;
import io.netty.util.internal.shaded.org.jctools.queues.atomic.AtomicReferenceArrayQueue;
import io.netty.util.internal.shaded.org.jctools.util.Pow2;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReferenceArray;

public abstract class AtomicReferenceArrayQueue<E>
extends AbstractQueue<E>
implements IndexedQueueSizeUtil.IndexedQueue,
QueueProgressIndicators,
MessagePassingQueue<E>,
SupportsIterator {
    protected final AtomicReferenceArray<E> buffer;
    protected final int mask;

    public AtomicReferenceArrayQueue(int capacity) {
        int actualCapacity = Pow2.roundToPowerOfTwo(capacity);
        this.mask = actualCapacity - 1;
        this.buffer = new AtomicReferenceArray(actualCapacity);
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
    public final int capacity() {
        return this.mask + 1;
    }

    @Override
    public final int size() {
        return IndexedQueueSizeUtil.size((IndexedQueueSizeUtil.IndexedQueue)this, (int)1);
    }

    @Override
    public final boolean isEmpty() {
        return IndexedQueueSizeUtil.isEmpty((IndexedQueueSizeUtil.IndexedQueue)this);
    }

    @Override
    public final long currentProducerIndex() {
        return this.lvProducerIndex();
    }

    @Override
    public final long currentConsumerIndex() {
        return this.lvConsumerIndex();
    }

    @Override
    public final Iterator<E> iterator() {
        long cIndex = this.lvConsumerIndex();
        long pIndex = this.lvProducerIndex();
        return new WeakIterator(cIndex, pIndex, this.mask, this.buffer);
    }
}

