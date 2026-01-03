/*
 * Decompiled with CFR 0.152.
 */
package java.util.concurrent.atomic;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

public class AtomicLongArray
implements Serializable {
    private static final long serialVersionUID = -2308431214976778248L;
    private static final VarHandle AA = MethodHandles.arrayElementVarHandle(long[].class);
    private final long[] array;

    public AtomicLongArray(int length) {
        this.array = new long[length];
    }

    public AtomicLongArray(long[] array) {
        this.array = (long[])array.clone();
    }

    public final int length() {
        return this.array.length;
    }

    public final long get(int i) {
        return AA.getVolatile(this.array, i);
    }

    public final void set(int i, long newValue) {
        AA.setVolatile(this.array, i, newValue);
    }

    public final void lazySet(int i, long newValue) {
        AA.setRelease(this.array, i, newValue);
    }

    public final long getAndSet(int i, long newValue) {
        return AA.getAndSet(this.array, i, newValue);
    }

    public final boolean compareAndSet(int i, long expectedValue, long newValue) {
        return AA.compareAndSet(this.array, i, expectedValue, newValue);
    }

    @Deprecated(since="9")
    public final boolean weakCompareAndSet(int i, long expectedValue, long newValue) {
        return AA.weakCompareAndSetPlain(this.array, i, expectedValue, newValue);
    }

    public final boolean weakCompareAndSetPlain(int i, long expectedValue, long newValue) {
        return AA.weakCompareAndSetPlain(this.array, i, expectedValue, newValue);
    }

    public final long getAndIncrement(int i) {
        return AA.getAndAdd(this.array, i, 1L);
    }

    public final long getAndDecrement(int i) {
        return AA.getAndAdd(this.array, i, -1L);
    }

    public final long getAndAdd(int i, long delta) {
        return AA.getAndAdd(this.array, i, delta);
    }

    public final long incrementAndGet(int i) {
        return AA.getAndAdd(this.array, i, 1L) + 1L;
    }

    public final long decrementAndGet(int i) {
        return AA.getAndAdd(this.array, i, -1L) - 1L;
    }

    public long addAndGet(int i, long delta) {
        return AA.getAndAdd(this.array, i, delta) + delta;
    }

    public final long getAndUpdate(int i, LongUnaryOperator updateFunction) {
        long prev = this.get(i);
        long next = 0L;
        boolean haveNext = false;
        while (true) {
            if (!haveNext) {
                next = updateFunction.applyAsLong(prev);
            }
            if (this.weakCompareAndSetVolatile(i, prev, next)) {
                return prev;
            }
            haveNext = prev == (prev = this.get(i));
        }
    }

    public final long updateAndGet(int i, LongUnaryOperator updateFunction) {
        long prev = this.get(i);
        long next = 0L;
        boolean haveNext = false;
        while (true) {
            if (!haveNext) {
                next = updateFunction.applyAsLong(prev);
            }
            if (this.weakCompareAndSetVolatile(i, prev, next)) {
                return next;
            }
            haveNext = prev == (prev = this.get(i));
        }
    }

    public final long getAndAccumulate(int i, long x, LongBinaryOperator accumulatorFunction) {
        long prev = this.get(i);
        long next = 0L;
        boolean haveNext = false;
        while (true) {
            if (!haveNext) {
                next = accumulatorFunction.applyAsLong(prev, x);
            }
            if (this.weakCompareAndSetVolatile(i, prev, next)) {
                return prev;
            }
            haveNext = prev == (prev = this.get(i));
        }
    }

    public final long accumulateAndGet(int i, long x, LongBinaryOperator accumulatorFunction) {
        long prev = this.get(i);
        long next = 0L;
        boolean haveNext = false;
        while (true) {
            if (!haveNext) {
                next = accumulatorFunction.applyAsLong(prev, x);
            }
            if (this.weakCompareAndSetVolatile(i, prev, next)) {
                return next;
            }
            haveNext = prev == (prev = this.get(i));
        }
    }

    public String toString() {
        int iMax = this.array.length - 1;
        if (iMax == -1) {
            return "[]";
        }
        StringBuilder b = new StringBuilder();
        b.append('[');
        int i = 0;
        while (true) {
            b.append(this.get(i));
            if (i == iMax) {
                return b.append(']').toString();
            }
            b.append(',').append(' ');
            ++i;
        }
    }

    public final long getPlain(int i) {
        return AA.get(this.array, i);
    }

    public final void setPlain(int i, long newValue) {
        AA.set(this.array, i, newValue);
    }

    public final long getOpaque(int i) {
        return AA.getOpaque(this.array, i);
    }

    public final void setOpaque(int i, long newValue) {
        AA.setOpaque(this.array, i, newValue);
    }

    public final long getAcquire(int i) {
        return AA.getAcquire(this.array, i);
    }

    public final void setRelease(int i, long newValue) {
        AA.setRelease(this.array, i, newValue);
    }

    public final long compareAndExchange(int i, long expectedValue, long newValue) {
        return AA.compareAndExchange(this.array, i, expectedValue, newValue);
    }

    public final long compareAndExchangeAcquire(int i, long expectedValue, long newValue) {
        return AA.compareAndExchangeAcquire(this.array, i, expectedValue, newValue);
    }

    public final long compareAndExchangeRelease(int i, long expectedValue, long newValue) {
        return AA.compareAndExchangeRelease(this.array, i, expectedValue, newValue);
    }

    public final boolean weakCompareAndSetVolatile(int i, long expectedValue, long newValue) {
        return AA.weakCompareAndSet(this.array, i, expectedValue, newValue);
    }

    public final boolean weakCompareAndSetAcquire(int i, long expectedValue, long newValue) {
        return AA.weakCompareAndSetAcquire(this.array, i, expectedValue, newValue);
    }

    public final boolean weakCompareAndSetRelease(int i, long expectedValue, long newValue) {
        return AA.weakCompareAndSetRelease(this.array, i, expectedValue, newValue);
    }
}

