/*
 * Decompiled with CFR 0.152.
 */
package java.util.concurrent.atomic;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleBinaryOperator;
import java.util.function.LongBinaryOperator;
import jdk.internal.vm.annotation.Contended;

abstract class Striped64
extends Number {
    static final int NCPU = Runtime.getRuntime().availableProcessors();
    volatile transient Cell[] cells;
    volatile transient long base;
    volatile transient int cellsBusy;
    private static final VarHandle BASE;
    private static final VarHandle CELLSBUSY;
    private static final VarHandle THREAD_PROBE;

    Striped64() {
    }

    final boolean casBase(long cmp, long val) {
        return BASE.weakCompareAndSetRelease(this, cmp, val);
    }

    final long getAndSetBase(long val) {
        return BASE.getAndSet(this, val);
    }

    final boolean casCellsBusy() {
        return CELLSBUSY.compareAndSet(this, 0, 1);
    }

    static final int getProbe() {
        return THREAD_PROBE.get(Thread.currentThread());
    }

    static final int advanceProbe(int probe) {
        probe ^= probe << 13;
        probe ^= probe >>> 17;
        probe ^= probe << 5;
        THREAD_PROBE.set(Thread.currentThread(), probe);
        return probe;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    final void longAccumulate(long x, LongBinaryOperator fn, boolean wasUncontended, int index) {
        if (index == 0) {
            ThreadLocalRandom.current();
            index = Striped64.getProbe();
            wasUncontended = true;
        }
        boolean collide = false;
        while (true) {
            long v;
            int n;
            Cell[] cs = this.cells;
            if (this.cells != null && (n = cs.length) > 0) {
                Cell c = cs[n - 1 & index];
                if (c == null) {
                    if (this.cellsBusy == 0) {
                        Cell r = new Cell(x);
                        if (this.cellsBusy == 0 && this.casCellsBusy()) {
                            try {
                                int j;
                                int m;
                                Cell[] rs = this.cells;
                                if (this.cells == null || (m = rs.length) <= 0 || rs[j = m - 1 & index] != null) continue;
                                rs[j] = r;
                                return;
                            }
                            finally {
                                this.cellsBusy = 0;
                                continue;
                            }
                        }
                    }
                    collide = false;
                } else if (!wasUncontended) {
                    wasUncontended = true;
                } else {
                    v = c.value;
                    if (c.cas(v, fn == null ? v + x : fn.applyAsLong(v, x))) return;
                    if (n >= NCPU || this.cells != cs) {
                        collide = false;
                    } else if (!collide) {
                        collide = true;
                    } else if (this.cellsBusy == 0 && this.casCellsBusy()) {
                        try {
                            if (this.cells == cs) {
                                this.cells = Arrays.copyOf(cs, n << 1);
                            }
                        }
                        finally {
                            this.cellsBusy = 0;
                        }
                        collide = false;
                        continue;
                    }
                }
                index = Striped64.advanceProbe(index);
                continue;
            }
            if (this.cellsBusy == 0 && this.cells == cs && this.casCellsBusy()) {
                try {
                    if (this.cells != cs) continue;
                    Cell[] rs = new Cell[2];
                    rs[index & 1] = new Cell(x);
                    this.cells = rs;
                    return;
                }
                finally {
                    this.cellsBusy = 0;
                    continue;
                }
            }
            v = this.base;
            if (this.casBase(v, fn == null ? v + x : fn.applyAsLong(v, x))) return;
        }
    }

    private static long apply(DoubleBinaryOperator fn, long v, double x) {
        double d = Double.longBitsToDouble(v);
        d = fn == null ? d + x : fn.applyAsDouble(d, x);
        return Double.doubleToRawLongBits(d);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    final void doubleAccumulate(double x, DoubleBinaryOperator fn, boolean wasUncontended, int index) {
        if (index == 0) {
            ThreadLocalRandom.current();
            index = Striped64.getProbe();
            wasUncontended = true;
        }
        boolean collide = false;
        while (true) {
            long v;
            int n;
            Cell[] cs = this.cells;
            if (this.cells != null && (n = cs.length) > 0) {
                Cell c = cs[n - 1 & index];
                if (c == null) {
                    if (this.cellsBusy == 0) {
                        Cell r = new Cell(Double.doubleToRawLongBits(x));
                        if (this.cellsBusy == 0 && this.casCellsBusy()) {
                            try {
                                int j;
                                int m;
                                Cell[] rs = this.cells;
                                if (this.cells == null || (m = rs.length) <= 0 || rs[j = m - 1 & index] != null) continue;
                                rs[j] = r;
                                return;
                            }
                            finally {
                                this.cellsBusy = 0;
                                continue;
                            }
                        }
                    }
                    collide = false;
                } else if (!wasUncontended) {
                    wasUncontended = true;
                } else {
                    v = c.value;
                    if (c.cas(v, Striped64.apply(fn, v, x))) return;
                    if (n >= NCPU || this.cells != cs) {
                        collide = false;
                    } else if (!collide) {
                        collide = true;
                    } else if (this.cellsBusy == 0 && this.casCellsBusy()) {
                        try {
                            if (this.cells == cs) {
                                this.cells = Arrays.copyOf(cs, n << 1);
                            }
                        }
                        finally {
                            this.cellsBusy = 0;
                        }
                        collide = false;
                        continue;
                    }
                }
                index = Striped64.advanceProbe(index);
                continue;
            }
            if (this.cellsBusy == 0 && this.cells == cs && this.casCellsBusy()) {
                try {
                    if (this.cells != cs) continue;
                    Cell[] rs = new Cell[2];
                    rs[index & 1] = new Cell(Double.doubleToRawLongBits(x));
                    this.cells = rs;
                    return;
                }
                finally {
                    this.cellsBusy = 0;
                    continue;
                }
            }
            v = this.base;
            if (this.casBase(v, Striped64.apply(fn, v, x))) return;
        }
    }

    static {
        try {
            MethodHandles.Lookup l1 = MethodHandles.lookup();
            BASE = l1.findVarHandle(Striped64.class, "base", Long.TYPE);
            CELLSBUSY = l1.findVarHandle(Striped64.class, "cellsBusy", Integer.TYPE);
            MethodHandles.Lookup l2 = AccessController.doPrivileged(new PrivilegedAction<MethodHandles.Lookup>(){

                @Override
                public MethodHandles.Lookup run() {
                    try {
                        return MethodHandles.privateLookupIn(Thread.class, MethodHandles.lookup());
                    }
                    catch (ReflectiveOperationException e) {
                        throw new ExceptionInInitializerError(e);
                    }
                }
            });
            THREAD_PROBE = l2.findVarHandle(Thread.class, "threadLocalRandomProbe", Integer.TYPE);
        }
        catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Contended
    static final class Cell {
        volatile long value;
        private static final VarHandle VALUE;

        Cell(long x) {
            this.value = x;
        }

        final boolean cas(long cmp, long val) {
            return VALUE.weakCompareAndSetRelease(this, cmp, val);
        }

        final void reset() {
            VALUE.setVolatile(this, 0L);
        }

        final void reset(long identity) {
            VALUE.setVolatile(this, identity);
        }

        final long getAndSet(long val) {
            return VALUE.getAndSet(this, val);
        }

        static {
            try {
                MethodHandles.Lookup l = MethodHandles.lookup();
                VALUE = l.findVarHandle(Cell.class, "value", Long.TYPE);
            }
            catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }
}

