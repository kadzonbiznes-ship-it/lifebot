/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d;

import java.lang.ref.Reference;
import sun.java2d.ReentrantContext;
import sun.java2d.ReentrantContextProvider;
import sun.java2d.ReentrantContextProviderCLQ;

public abstract class ReentrantContextProviderTL<K extends ReentrantContext>
extends ReentrantContextProvider<K> {
    private final ThreadLocal<Reference<K>> ctxTL = new ThreadLocal();
    private final ReentrantContextProviderCLQ<K> ctxProviderCLQ;

    public ReentrantContextProviderTL(int refType) {
        this(refType, 2);
    }

    public ReentrantContextProviderTL(int refTypeTL, int refTypeCLQ) {
        super(refTypeTL);
        final ReentrantContextProviderTL parent = this;
        this.ctxProviderCLQ = new ReentrantContextProviderCLQ<K>(this, refTypeCLQ){

            @Override
            protected K newContext() {
                return parent.newContext();
            }
        };
    }

    @Override
    public final K acquire() {
        ReentrantContext ctx = null;
        Reference<K> ref = this.ctxTL.get();
        if (ref != null) {
            ctx = (ReentrantContext)ref.get();
        }
        if (ctx == null) {
            ctx = this.newContext();
            this.ctxTL.set(this.getOrCreateReference(ctx));
        }
        if (ctx.usage == 0) {
            ctx.usage = 1;
        } else {
            ctx = this.ctxProviderCLQ.acquire();
        }
        return (K)ctx;
    }

    @Override
    public final void release(K ctx) {
        if (((ReentrantContext)ctx).usage == 1) {
            ((ReentrantContext)ctx).usage = 0;
        } else {
            this.ctxProviderCLQ.release(ctx);
        }
    }
}

