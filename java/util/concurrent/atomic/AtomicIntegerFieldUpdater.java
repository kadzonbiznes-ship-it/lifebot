/*
 * Decompiled with CFR 0.152.
 */
package java.util.concurrent.atomic;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import jdk.internal.misc.Unsafe;
import jdk.internal.reflect.CallerSensitive;
import jdk.internal.reflect.Reflection;
import sun.reflect.misc.ReflectUtil;

public abstract class AtomicIntegerFieldUpdater<T> {
    @CallerSensitive
    public static <U> AtomicIntegerFieldUpdater<U> newUpdater(Class<U> tclass, String fieldName) {
        return new AtomicIntegerFieldUpdaterImpl<U>(tclass, fieldName, Reflection.getCallerClass());
    }

    protected AtomicIntegerFieldUpdater() {
    }

    public abstract boolean compareAndSet(T var1, int var2, int var3);

    public abstract boolean weakCompareAndSet(T var1, int var2, int var3);

    public abstract void set(T var1, int var2);

    public abstract void lazySet(T var1, int var2);

    public abstract int get(T var1);

    public int getAndSet(T obj, int newValue) {
        int prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), newValue)) {
        }
        return prev;
    }

    public int getAndIncrement(T obj) {
        int next;
        int prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = prev + 1)) {
        }
        return prev;
    }

    public int getAndDecrement(T obj) {
        int next;
        int prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = prev - 1)) {
        }
        return prev;
    }

    public int getAndAdd(T obj, int delta) {
        int next;
        int prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = prev + delta)) {
        }
        return prev;
    }

    public int incrementAndGet(T obj) {
        int next;
        int prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = prev + 1)) {
        }
        return next;
    }

    public int decrementAndGet(T obj) {
        int next;
        int prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = prev - 1)) {
        }
        return next;
    }

    public int addAndGet(T obj, int delta) {
        int next;
        int prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = prev + delta)) {
        }
        return next;
    }

    public final int getAndUpdate(T obj, IntUnaryOperator updateFunction) {
        int next;
        int prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = updateFunction.applyAsInt(prev))) {
        }
        return prev;
    }

    public final int updateAndGet(T obj, IntUnaryOperator updateFunction) {
        int next;
        int prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = updateFunction.applyAsInt(prev))) {
        }
        return next;
    }

    public final int getAndAccumulate(T obj, int x, IntBinaryOperator accumulatorFunction) {
        int next;
        int prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = accumulatorFunction.applyAsInt(prev, x))) {
        }
        return prev;
    }

    public final int accumulateAndGet(T obj, int x, IntBinaryOperator accumulatorFunction) {
        int next;
        int prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = accumulatorFunction.applyAsInt(prev, x))) {
        }
        return next;
    }

    private static final class AtomicIntegerFieldUpdaterImpl<T>
    extends AtomicIntegerFieldUpdater<T> {
        private static final Unsafe U = Unsafe.getUnsafe();
        private final long offset;
        private final Class<?> cclass;
        private final Class<T> tclass;

        AtomicIntegerFieldUpdaterImpl(final Class<T> tclass, final String fieldName, Class<?> caller) {
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
                if (!(ccl == null || ccl == cl || cl != null && AtomicIntegerFieldUpdaterImpl.isAncestor(cl, ccl))) {
                    ReflectUtil.checkPackageAccess(tclass);
                }
            }
            catch (PrivilegedActionException pae) {
                throw new RuntimeException(pae.getException());
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            if (field.getType() != Integer.TYPE) {
                throw new IllegalArgumentException("Must be integer type");
            }
            if (!Modifier.isVolatile(modifiers)) {
                throw new IllegalArgumentException("Must be volatile type");
            }
            this.cclass = Modifier.isProtected(modifiers) && tclass.isAssignableFrom(caller) && !AtomicIntegerFieldUpdaterImpl.isSamePackage(tclass, caller) ? caller : tclass;
            this.tclass = tclass;
            this.offset = U.objectFieldOffset(field);
        }

        private static boolean isAncestor(ClassLoader first, ClassLoader second) {
            ClassLoader acl = first;
            do {
                if (second != (acl = acl.getParent())) continue;
                return true;
            } while (acl != null);
            return false;
        }

        private static boolean isSamePackage(Class<?> class1, Class<?> class2) {
            return class1.getClassLoader() == class2.getClassLoader() && class1.getPackageName() == class2.getPackageName();
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
        public final boolean compareAndSet(T obj, int expect, int update) {
            this.accessCheck(obj);
            return U.compareAndSetInt(obj, this.offset, expect, update);
        }

        @Override
        public final boolean weakCompareAndSet(T obj, int expect, int update) {
            this.accessCheck(obj);
            return U.compareAndSetInt(obj, this.offset, expect, update);
        }

        @Override
        public final void set(T obj, int newValue) {
            this.accessCheck(obj);
            U.putIntVolatile(obj, this.offset, newValue);
        }

        @Override
        public final void lazySet(T obj, int newValue) {
            this.accessCheck(obj);
            U.putIntRelease(obj, this.offset, newValue);
        }

        @Override
        public final int get(T obj) {
            this.accessCheck(obj);
            return U.getIntVolatile(obj, this.offset);
        }

        @Override
        public final int getAndSet(T obj, int newValue) {
            this.accessCheck(obj);
            return U.getAndSetInt(obj, this.offset, newValue);
        }

        @Override
        public final int getAndAdd(T obj, int delta) {
            this.accessCheck(obj);
            return U.getAndAddInt(obj, this.offset, delta);
        }

        @Override
        public final int getAndIncrement(T obj) {
            return this.getAndAdd(obj, 1);
        }

        @Override
        public final int getAndDecrement(T obj) {
            return this.getAndAdd(obj, -1);
        }

        @Override
        public final int incrementAndGet(T obj) {
            return this.getAndAdd(obj, 1) + 1;
        }

        @Override
        public final int decrementAndGet(T obj) {
            return this.getAndAdd(obj, -1) - 1;
        }

        @Override
        public final int addAndGet(T obj, int delta) {
            return this.getAndAdd(obj, delta) + delta;
        }
    }
}

