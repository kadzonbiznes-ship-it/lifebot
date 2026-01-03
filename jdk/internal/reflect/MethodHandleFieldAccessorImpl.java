/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import jdk.internal.reflect.FieldAccessorImpl;

abstract class MethodHandleFieldAccessorImpl
extends FieldAccessorImpl {
    private static final int IS_READ_ONLY_BIT = 1;
    private static final int IS_STATIC_BIT = 2;
    private static final int NONZERO_BIT = 32768;
    private final int fieldFlags;
    protected final MethodHandle getter;
    protected final MethodHandle setter;

    protected MethodHandleFieldAccessorImpl(Field field, MethodHandle getter, MethodHandle setter, boolean isReadOnly, boolean isStatic) {
        super(field);
        this.fieldFlags = (isReadOnly ? 1 : 0) | (isStatic ? 2 : 0) | 0x8000;
        this.getter = getter;
        this.setter = setter;
    }

    protected final boolean isReadOnly() {
        return (this.fieldFlags & 1) == 1;
    }

    protected final boolean isStatic() {
        return (this.fieldFlags & 2) == 2;
    }

    @Override
    protected final void ensureObj(Object o) {
        if (!this.isStatic() && !this.field.getDeclaringClass().isAssignableFrom(o.getClass())) {
            this.throwSetIllegalArgumentException(o);
        }
    }

    private String getMessage(boolean getter, Class<?> type) {
        String err = "Can not " + (getter ? "get" : "set");
        if (Modifier.isStatic(this.field.getModifiers())) {
            err = err + " static";
        }
        if (Modifier.isFinal(this.field.getModifiers())) {
            err = err + " final";
        }
        err = err + " " + this.field.getType().getName() + " field " + this.getQualifiedFieldName();
        if (type != null) {
            err = err + " on " + type.getName();
        }
        return err;
    }

    protected IllegalArgumentException newGetIllegalArgumentException(Object o) {
        return new IllegalArgumentException(this.getMessage(true, o != null ? o.getClass() : null));
    }

    protected IllegalArgumentException newSetIllegalArgumentException(Object o) {
        return new IllegalArgumentException(this.getMessage(false, o != null ? o.getClass() : null));
    }
}

