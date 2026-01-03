/*
 * Decompiled with CFR 0.152.
 */
package java.util.concurrent.atomic;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.Striped64;

public class LongAdder
extends Striped64
implements Serializable {
    private static final long serialVersionUID = 7249069246863182397L;

    public void add(long x) {
        long b;
        Striped64.Cell[] cs = this.cells;
        if (this.cells != null || !this.casBase(b = this.base, b + x)) {
            long v;
            Striped64.Cell c;
            int m;
            int index = LongAdder.getProbe();
            boolean uncontended = true;
            if (cs == null || (m = cs.length - 1) < 0 || (c = cs[index & m]) == null || !(uncontended = c.cas(v = c.value, v + x))) {
                this.longAccumulate(x, null, uncontended, index);
            }
        }
    }

    public void increment() {
        this.add(1L);
    }

    public void decrement() {
        this.add(-1L);
    }

    public long sum() {
        Striped64.Cell[] cs = this.cells;
        long sum = this.base;
        if (cs != null) {
            for (Striped64.Cell c : cs) {
                if (c == null) continue;
                sum += c.value;
            }
        }
        return sum;
    }

    public void reset() {
        Striped64.Cell[] cs = this.cells;
        this.base = 0L;
        if (cs != null) {
            for (Striped64.Cell c : cs) {
                if (c == null) continue;
                c.reset();
            }
        }
    }

    public long sumThenReset() {
        Striped64.Cell[] cs = this.cells;
        long sum = this.getAndSetBase(0L);
        if (cs != null) {
            for (Striped64.Cell c : cs) {
                if (c == null) continue;
                sum += c.getAndSet(0L);
            }
        }
        return sum;
    }

    public String toString() {
        return Long.toString(this.sum());
    }

    @Override
    public long longValue() {
        return this.sum();
    }

    @Override
    public int intValue() {
        return (int)this.sum();
    }

    @Override
    public float floatValue() {
        return this.sum();
    }

    @Override
    public double doubleValue() {
        return this.sum();
    }

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private static class SerializationProxy
    implements Serializable {
        private static final long serialVersionUID = 7249069246863182397L;
        private final long value;

        SerializationProxy(LongAdder a) {
            this.value = a.sum();
        }

        private Object readResolve() {
            LongAdder a = new LongAdder();
            a.base = this.value;
            return a;
        }
    }
}

