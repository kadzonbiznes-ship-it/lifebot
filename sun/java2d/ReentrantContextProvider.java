/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import sun.java2d.ReentrantContext;

public abstract class ReentrantContextProvider<K extends ReentrantContext> {
    static final byte USAGE_TL_INACTIVE = 0;
    static final byte USAGE_TL_IN_USE = 1;
    static final byte USAGE_CLQ = 2;
    public static final int REF_HARD = 0;
    public static final int REF_SOFT = 1;
    public static final int REF_WEAK = 2;
    private final int refType;

    protected ReentrantContextProvider(int refType) {
        this.refType = refType;
    }

    protected abstract K newContext();

    public abstract K acquire();

    public abstract void release(K var1);

    protected final Reference<K> getOrCreateReference(K ctx) {
        if (((ReentrantContext)ctx).reference == null) {
            switch (this.refType) {
                case 0: {
                    ((ReentrantContext)ctx).reference = new HardReference<K>(ctx);
                    break;
                }
                case 1: {
                    ((ReentrantContext)ctx).reference = new SoftReference<K>(ctx);
                    break;
                }
                default: {
                    ((ReentrantContext)ctx).reference = new WeakReference<K>(ctx);
                }
            }
        }
        return ((ReentrantContext)ctx).reference;
    }

    static final class HardReference<V>
    extends WeakReference<V> {
        private final V strongRef;

        HardReference(V referent) {
            super(null);
            this.strongRef = referent;
        }

        @Override
        public V get() {
            return this.strongRef;
        }
    }
}

