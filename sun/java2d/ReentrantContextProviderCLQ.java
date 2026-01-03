/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d;

import java.lang.ref.Reference;
import java.util.concurrent.ConcurrentLinkedQueue;
import sun.java2d.ReentrantContext;
import sun.java2d.ReentrantContextProvider;

public abstract class ReentrantContextProviderCLQ<K extends ReentrantContext>
extends ReentrantContextProvider<K> {
    private final ConcurrentLinkedQueue<Reference<K>> ctxQueue = new ConcurrentLinkedQueue();

    public ReentrantContextProviderCLQ(int refType) {
        super(refType);
    }

    @Override
    public final K acquire() {
        Reference<K> ref;
        ReentrantContext ctx = null;
        while (ctx == null && (ref = this.ctxQueue.poll()) != null) {
            ctx = (ReentrantContext)ref.get();
        }
        if (ctx == null) {
            ctx = (ReentrantContext)this.newContext();
            ctx.usage = (byte)2;
        }
        return (K)ctx;
    }

    @Override
    public final void release(K ctx) {
        if (((ReentrantContext)ctx).usage == 2) {
            this.ctxQueue.offer(this.getOrCreateReference(ctx));
        }
    }
}

