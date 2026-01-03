/*
 * Decompiled with CFR 0.152.
 */
package java.util.concurrent.atomic;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import jdk.internal.misc.Unsafe;
import jdk.internal.reflect.CallerSensitive;
import jdk.internal.reflect.Reflection;
import sun.reflect.misc.ReflectUtil;

public abstract class AtomicReferenceFieldUpdater<T, V> {
    @CallerSensitive
    public static <U, W> AtomicReferenceFieldUpdater<U, W> newUpdater(Class<U> tclass, Class<W> vclass, String fieldName) {
        return new AtomicReferenceFieldUpdaterImpl<U, W>(tclass, vclass, fieldName, Reflection.getCallerClass());
    }

    protected AtomicReferenceFieldUpdater() {
    }

    public abstract boolean compareAndSet(T var1, V var2, V var3);

    public abstract boolean weakCompareAndSet(T var1, V var2, V var3);

    public abstract void set(T var1, V var2);

    public abstract void lazySet(T var1, V var2);

    public abstract V get(T var1);

    public V getAndSet(T obj, V newValue) {
        V prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), newValue)) {
        }
        return prev;
    }

    public final V getAndUpdate(T obj, UnaryOperator<V> updateFunction) {
        Object next;
        V prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = updateFunction.apply(prev))) {
        }
        return prev;
    }

    public final V updateAndGet(T obj, UnaryOperator<V> updateFunction) {
        Object next;
        V prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = updateFunction.apply(prev))) {
        }
        return (V)next;
    }

    public final V getAndAccumulate(T obj, V x, BinaryOperator<V> accumulatorFunction) {
        Object next;
        V prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = accumulatorFunction.apply(prev, x))) {
        }
        return prev;
    }

    public final V accumulateAndGet(T obj, V x, BinaryOperator<V> accumulatorFunction) {
        Object next;
        V prev;
        while (!this.compareAndSet(obj, prev = this.get(obj), next = accumulatorFunction.apply(prev, x))) {
        }
        return (V)next;
    }

    private static final class AtomicReferenceFieldUpdaterImpl<T, V>
    extends AtomicReferenceFieldUpdater<T, V> {
        private static final Unsafe U = Unsafe.getUnsafe();
        private final long offset;
        private final Class<?> cclass;
        private final Class<T> tclass;
        private final Class<V> vclass;

        AtomicReferenceFieldUpdaterImpl(final Class<T> tclass, Class<V> vclass, final String fieldName, Class<?> caller) {
            Class<?> fieldClass;
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
                if (!(ccl == null || ccl == cl || cl != null && AtomicReferenceFieldUpdaterImpl.isAncestor(cl, ccl))) {
                    ReflectUtil.checkPackageAccess(tclass);
                }
                fieldClass = field.getType();
            }
            catch (PrivilegedActionException pae) {
                throw new RuntimeException(pae.getException());
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            if (vclass != fieldClass) {
                throw new ClassCastException();
            }
            if (vclass.isPrimitive()) {
                throw new IllegalArgumentException("Must be reference type");
            }
            if (!Modifier.isVolatile(modifiers)) {
                throw new IllegalArgumentException("Must be volatile type");
            }
            this.cclass = Modifier.isProtected(modifiers) && tclass.isAssignableFrom(caller) && !AtomicReferenceFieldUpdaterImpl.isSamePackage(tclass, caller) ? caller : tclass;
            this.tclass = tclass;
            this.vclass = vclass;
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

        private final void valueCheck(V v) {
            if (v != null && !this.vclass.isInstance(v)) {
                AtomicReferenceFieldUpdaterImpl.throwCCE();
            }
        }

        static void throwCCE() {
            throw new ClassCastException();
        }

        @Override
        public final boolean compareAndSet(T obj, V expect, V update) {
            this.accessCheck(obj);
            this.valueCheck(update);
            return U.compareAndSetReference(obj, this.offset, expect, update);
        }

        @Override
        public final boolean weakCompareAndSet(T obj, V expect, V update) {
            this.accessCheck(obj);
            this.valueCheck(update);
            return U.compareAndSetReference(obj, this.offset, expect, update);
        }

        @Override
        public final void set(T obj, V newValue) {
            this.accessCheck(obj);
            this.valueCheck(newValue);
            U.putReferenceVolatile(obj, this.offset, newValue);
        }

        @Override
        public final void lazySet(T obj, V newValue) {
            this.accessCheck(obj);
            this.valueCheck(newValue);
            U.putReferenceRelease(obj, this.offset, newValue);
        }

        @Override
        public final V get(T obj) {
            this.accessCheck(obj);
            return (V)U.getReferenceVolatile(obj, this.offset);
        }

        @Override
        public final V getAndSet(T obj, V newValue) {
            this.accessCheck(obj);
            this.valueCheck(newValue);
            return (V)U.getAndSetReference(obj, this.offset, newValue);
        }
    }
}

