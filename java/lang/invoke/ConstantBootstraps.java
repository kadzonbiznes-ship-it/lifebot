/*
 * Decompiled with CFR 0.152.
 */
package java.lang.invoke;

import java.lang.invoke.BootstrapMethodInvoker;
import java.lang.invoke.MemberName;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleNatives;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import sun.invoke.util.Wrapper;

public final class ConstantBootstraps {
    private ConstantBootstraps() {
        throw new AssertionError();
    }

    static Object makeConstant(MethodHandle bootstrapMethod, String name, Class<?> type, Object info, Class<?> callerClass) {
        MethodType mt = bootstrapMethod.type();
        if (mt.parameterCount() < 2 || !MethodHandles.Lookup.class.isAssignableFrom((Class<?>)mt.parameterType(0))) {
            throw new BootstrapMethodError("Invalid bootstrap method declared for resolving a dynamic constant: " + bootstrapMethod);
        }
        return BootstrapMethodInvoker.invoke(type, bootstrapMethod, name, type, info, callerClass);
    }

    public static Object nullConstant(MethodHandles.Lookup lookup, String name, Class<?> type) {
        if (Objects.requireNonNull(type).isPrimitive()) {
            throw new IllegalArgumentException(String.format("not reference: %s", type));
        }
        return null;
    }

    public static Class<?> primitiveClass(MethodHandles.Lookup lookup, String name, Class<?> type) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
        if (type != Class.class) {
            throw new IllegalArgumentException();
        }
        if (name.length() != 1) {
            throw new IllegalArgumentException(String.format("not primitive: %s", name));
        }
        return Wrapper.forPrimitiveType(name.charAt(0)).primitiveType();
    }

    public static <E extends Enum<E>> E enumConstant(MethodHandles.Lookup lookup, String name, Class<E> type) {
        Objects.requireNonNull(lookup);
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
        ConstantBootstraps.validateClassAccess(lookup, type);
        return Enum.valueOf(type, name);
    }

    public static Object getStaticFinal(MethodHandles.Lookup lookup, String name, Class<?> type, Class<?> declaringClass) {
        MethodHandle mh;
        Objects.requireNonNull(lookup);
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
        Objects.requireNonNull(declaringClass);
        try {
            mh = lookup.findStaticGetter(declaringClass, name, type);
            MemberName member = mh.internalMemberName();
            if (!member.isFinal()) {
                throw new IncompatibleClassChangeError("not a final field: " + name);
            }
        }
        catch (ReflectiveOperationException ex) {
            throw MethodHandleNatives.mapLookupExceptionToError(ex);
        }
        try {
            return mh.invoke();
        }
        catch (Error | RuntimeException e) {
            throw e;
        }
        catch (Throwable e) {
            throw new LinkageError("Unexpected throwable", e);
        }
    }

    public static Object getStaticFinal(MethodHandles.Lookup lookup, String name, Class<?> type) {
        Objects.requireNonNull(type);
        Class<?> declaring = type.isPrimitive() ? Wrapper.forPrimitiveType(type).wrapperType() : type;
        return ConstantBootstraps.getStaticFinal(lookup, name, type, declaring);
    }

    public static Object invoke(MethodHandles.Lookup lookup, String name, Class<?> type, MethodHandle handle, Object ... args) throws Throwable {
        Objects.requireNonNull(type);
        Objects.requireNonNull(handle);
        Objects.requireNonNull(args);
        if (type != handle.type().returnType()) {
            handle = handle.asType(handle.type().changeReturnType(type)).withVarargs(handle.isVarargsCollector());
        }
        return handle.invokeWithArguments(args);
    }

    public static VarHandle fieldVarHandle(MethodHandles.Lookup lookup, String name, Class<VarHandle> type, Class<?> declaringClass, Class<?> fieldType) {
        Objects.requireNonNull(lookup);
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
        Objects.requireNonNull(declaringClass);
        Objects.requireNonNull(fieldType);
        if (type != VarHandle.class) {
            throw new IllegalArgumentException();
        }
        try {
            return lookup.findVarHandle(declaringClass, name, fieldType);
        }
        catch (ReflectiveOperationException e) {
            throw MethodHandleNatives.mapLookupExceptionToError(e);
        }
    }

    public static VarHandle staticFieldVarHandle(MethodHandles.Lookup lookup, String name, Class<VarHandle> type, Class<?> declaringClass, Class<?> fieldType) {
        Objects.requireNonNull(lookup);
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
        Objects.requireNonNull(declaringClass);
        Objects.requireNonNull(fieldType);
        if (type != VarHandle.class) {
            throw new IllegalArgumentException();
        }
        try {
            return lookup.findStaticVarHandle(declaringClass, name, fieldType);
        }
        catch (ReflectiveOperationException e) {
            throw MethodHandleNatives.mapLookupExceptionToError(e);
        }
    }

    public static VarHandle arrayVarHandle(MethodHandles.Lookup lookup, String name, Class<VarHandle> type, Class<?> arrayClass) {
        Objects.requireNonNull(lookup);
        Objects.requireNonNull(type);
        Objects.requireNonNull(arrayClass);
        if (type != VarHandle.class) {
            throw new IllegalArgumentException();
        }
        return MethodHandles.arrayElementVarHandle(ConstantBootstraps.validateClassAccess(lookup, arrayClass));
    }

    public static Object explicitCast(MethodHandles.Lookup lookup, String name, Class<?> dstType, Object value) throws ClassCastException {
        if (dstType == Void.TYPE) {
            throw new ClassCastException("Can not convert to void");
        }
        if (dstType == Object.class) {
            return value;
        }
        MethodHandle id = MethodHandles.identity(dstType);
        MethodType mt = MethodType.methodType(dstType, Object.class);
        MethodHandle conv = MethodHandles.explicitCastArguments(id, mt);
        try {
            return conv.invoke(value);
        }
        catch (Error | RuntimeException e) {
            throw e;
        }
        catch (Throwable throwable) {
            throw new InternalError(throwable);
        }
    }

    private static <T> Class<T> validateClassAccess(MethodHandles.Lookup lookup, Class<T> type) {
        try {
            return lookup.accessClass(type);
        }
        catch (ReflectiveOperationException ex) {
            throw MethodHandleNatives.mapLookupExceptionToError(ex);
        }
    }
}

