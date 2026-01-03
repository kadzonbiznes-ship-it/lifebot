/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.util.internal.shaded.org.jctools.util.UnsafeLongArrayAccess
 */
package io.netty.util.internal.shaded.org.jctools.queues;

import io.netty.util.internal.shaded.org.jctools.queues.ConcurrentCircularArrayQueue;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeLongArrayAccess;

public abstract class ConcurrentSequencedCircularArrayQueue<E>
extends ConcurrentCircularArrayQueue<E> {
    protected final long[] sequenceBuffer;

    public ConcurrentSequencedCircularArrayQueue(int capacity) {
        super(capacity);
        int actualCapacity = (int)(this.mask + 1L);
        this.sequenceBuffer = UnsafeLongArrayAccess.allocateLongArray((int)actualCapacity);
        for (long i = 0L; i < (long)actualCapacity; ++i) {
            UnsafeLongArrayAccess.soLongElement((long[])this.sequenceBuffer, (long)UnsafeLongArrayAccess.calcCircularLongElementOffset((long)i, (long)this.mask), (long)i);
        }
    }
}

