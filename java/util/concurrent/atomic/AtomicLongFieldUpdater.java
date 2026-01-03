/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.util.concurrent.atomic.AtomicLongFieldUpdater$LockedUpdater
 */
package java.util.concurrent.atomic;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;
import jdk.internal.misc.Unsafe;
import jdk.internal.reflect.CallerSensitive;
import jdk.internal.reflect.Reflection;
import sun.reflect.misc.ReflectUtil;

public abstract class AtomicLongFieldUpdater<T> {
    @CallerSensitive
    public static <U> AtomicLongFieldUpdater<U> newUpdater(Class<U> tclass, String fieldName) {
        Class<?> caller = Reflection.getCallerClass();
        if (AtomicLong.VM_SUPPORTS_LONG_CAS) {
            return new CASUpdater<U>(tclass, fieldName, caller);
        }
        return new LockedUpdater(tclass, fieldName, caller);
    }

    protected AtomicLongFieldUpdater() {
    }

    public abstract boolean compareAndSet(T var1, long var2, long var4);

    public abstract boolean weakCompareAndSet(T var1, long var2, long var4);

    public abstract void set(T var1, long var2);

    public abstract void lazySet(T var1, long var2);

    public abstract long get(T var1);

    public long getAndSet(T obj, long newValue) {
        long prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), newValue)) {
        }
        return prev;
    }

    public long getAndIncrement(T obj) {
        long next;
        long prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = prev + 1L)) {
        }
        return prev;
    }

    public long getAndDecrement(T obj) {
        long next;
        long prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = prev - 1L)) {
        }
        return prev;
    }

    public long getAndAdd(T obj, long delta) {
        long next;
        long prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = prev + delta)) {
        }
        return prev;
    }

    public long incrementAndGet(T obj) {
        long next;
        long prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = prev + 1L)) {
        }
        return next;
    }

    public long decrementAndGet(T obj) {
        long next;
        long prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = prev - 1L)) {
        }
        return next;
    }

    public long addAndGet(T obj, long delta) {
        long next;
        long prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = prev + delta)) {
        }
        return next;
    }

    public final long getAndUpdate(T obj, LongUnaryOperator updateFunction) {
        long next;
        long prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = updateFunction.applyAsLong(prev))) {
        }
        return prev;
    }

    public final long updateAndGet(T obj, LongUnaryOperator updateFunction) {
        long next;
        long prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = updateFunction.applyAsLong(prev))) {
        }
        return next;
    }

    public final long getAndAccumulate(T obj, long x, LongBinaryOperator accumulatorFunction) {
        long next;
        long prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = accumulatorFunction.applyAsLong(prev, x))) {
        }
        return prev;
    }

    public final long accumulateAndGet(T obj, long x, LongBinaryOperator accumulatorFunction) {
        long next;
        long prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = accumulatorFunction.applyAsLong(prev, x))) {
        }
        return next;
    }

    static boolean isAncestor(ClassLoader first, ClassLoader second) {
        ClassLoader acl = first;
        do {
            if (second != (acl = acl.getParent())) continue;
            return true;
        } while (acl != null);
        return false;
    }

    static boolean isSamePackage(Class<?> class1, Class<?> class2) {
        return class1.getClassLoader() == class2.getClassLoader() && class1.getPackageName() == class2.getPackageName();
    }

    private static final class CASUpdater<T>
    extends AtomicLongFieldUpdater<T> {
        private static final Unsafe U = Unsafe.getUnsafe();
        private final long offset;
        private final Class<?> cclass;
        private final Class<T> tclass;

        CASUpdater(final Class<T> tclass, final String fieldName, Class<?> caller) {
            int modifiers;
            Field field;
            try {
                field = AccessController.doPrivileged(new PrivilegedExceptionAction<Field>(){

                    @Override
                    public Field run() throws NoSuchFieldException {
                        return tclass.getDeclaredField(fieldName);
                    }
                });
                modifiers = field.getModifiers();
                ReflectUtil.ensureMemberAccess(caller, tclass, null, modifiers);
                ClassLoader cl = tclass.getClassLoader();
                ClassLoader ccl = caller.getClassLoader();
                if (!(ccl == null || ccl == cl || cl != null && CASUpdater.isAncestor(cl, ccl))) {
                    ReflectUtil.checkPackageAccess(tclass);
                }
            }
            catch (PrivilegedActionException pae) {
                throw new RuntimeException(pae.getException());
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            if (field.getType() != Long.TYPE) {
                throw new IllegalArgumentException("Must be long type");
            }
            if (!Modifier.isVolatile(modifiers)) {
                throw new IllegalArgumentException("Must be volatile type");
            }
            this.cclass = Modifier.isProtected(modifiers) && tclass.isAssignableFrom(caller) && !CASUpdater.isSamePackage(tclass, caller) ? caller : tclass;
            this.tclass = tclass;
            this.offset = U.objectFieldOffset(field);
        }

        private final void accessCheck(T obj) {
            if (!this.cclass.isInstance(obj)) {
                this.throwAccessCheckException(obj);
            }
        }

        private final void throwAccessCheckException(T obj) {
            if (this.cclass == this.tclass) {
                throw new ClassCastException();
            }
            throw new RuntimeException(new IllegalAccessException("Class " + this.cclass.getName() + " can not access a protected member of class " + this.tclass.getName() + " using an instance of " + obj.getClass().getName()));
        }

        @Override
        public final boolean compareAndSet(T obj, long expect, long update) {
            this.accessCheck(obj);
            return U.compareAndSetLong(obj, this.offset, expect, update);
        }

        @Override
        public final boolean weakCompareAndSet(T obj, long expect, long update) {
            this.accessCheck(obj);
            return U.compareAndSetLong(obj, this.offset, expect, update);
        }

        @Override
        public final void set(T obj, long newValue) {
            this.accessCheck(obj);
            U.putLongVolatile(obj, this.offset, newValue);
        }

        @Override
        public final void lazySet(T obj, long newValue) {
            this.accessCheck(obj);
            U.putLongRelease(obj, this.offset, newValue);
        }

        @Override
        public final long get(T obj) {
            this.accessCheck(obj);
            return U.getLongVolatile(obj, this.offset);
        }

        @Override
        public final long getAndSet(T obj, long newValue) {
            this.accessCheck(obj);
            return U.getAndSetLong(obj, this.offset, newValue);
        }

        @Override
        public final long getAndAdd(T obj, long delta) {
            this.accessCheck(obj);
            return U.getAndAddLong(obj, this.offset, delta);
        }

        @Override
        public final long getAndIncrement(T obj) {
            return this.getAndAdd(obj, 1L);
        }

        @Override
        public final long getAndDecrement(T obj) {
            return this.getAndAdd(obj, -1L);
        }

        @Override
        public final long incrementAndGet(T obj) {
            return this.getAndAdd(obj, 1L) + 1L;
        }

        @Override
        public final long decrementAndGet(T obj) {
            return this.getAndAdd(obj, -1L) - 1L;
        }

        @Override
        public final long addAndGet(T obj, long delta) {
            return this.getAndAdd(obj, delta) + delta;
        }
    }
}

