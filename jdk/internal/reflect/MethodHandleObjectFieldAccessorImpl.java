/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import jdk.internal.reflect.FieldAccessorImpl;
import jdk.internal.reflect.MethodHandleFieldAccessorImpl;

class MethodHandleObjectFieldAccessorImpl
extends MethodHandleFieldAccessorImpl {
    static FieldAccessorImpl fieldAccessor(Field field, MethodHandle getter, MethodHandle setter, boolean isReadOnly) {
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        if (isStatic) {
            getter = getter.asType(MethodType.methodType(Object.class));
            if (setter != null) {
                setter = setter.asType(MethodType.methodType(Void.TYPE, Object.class));
            }
        } else {
            getter = getter.asType(MethodType.methodType(Object.class, Object.class));
            if (setter != null) {
                setter = setter.asType(MethodType.methodType(Void.TYPE, Object.class, Object.class));
            }
        }
        return new MethodHandleObjectFieldAccessorImpl(field, getter, setter, isReadOnly, isStatic);
    }

    MethodHandleObjectFieldAccessorImpl(Field field, MethodHandle getter, MethodHandle setter, boolean isReadOnly, boolean isStatic) {
        super(field, getter, setter, isReadOnly, isStatic);
    }

    @Override
    public Object get(Object obj) throws IllegalArgumentException {
        try {
            return this.isStatic() ? this.getter.invokeExact() : this.getter.invokeExact(obj);
        }
        catch (IllegalArgumentException | NullPointerException e) {
            throw e;
        }
        catch (ClassCastException e) {
            throw this.newGetIllegalArgumentException(obj);
        }
        catch (Throwable e) {
            throw new InternalError(e);
        }
    }

    @Override
    public boolean getBoolean(Object obj) throws IllegalArgumentException {
        throw this.newGetBooleanIllegalArgumentException();
    }

    @Override
    public byte getByte(Object obj) throws IllegalArgumentException {
        throw this.newGetByteIllegalArgumentException();
    }

    @Override
    public char getChar(Object obj) throws IllegalArgumentException {
        throw this.newGetCharIllegalArgumentException();
    }

    @Override
    public short getShort(Object obj) throws IllegalArgumentException {
        throw this.newGetShortIllegalArgumentException();
    }

    @Override
    public int getInt(Object obj) throws IllegalArgumentException {
        throw this.newGetIntIllegalArgumentException();
    }

    @Override
    public long getLong(Object obj) throws IllegalArgumentException {
        throw this.newGetLongIllegalArgumentException();
    }

    @Override
    public float getFloat(Object obj) throws IllegalArgumentException {
        throw this.newGetFloatIllegalArgumentException();
    }

    @Override
    public double getDouble(Object obj) throws IllegalArgumentException {
        throw this.newGetDoubleIllegalArgumentException();
    }

    @Override
    public void set(Object obj, Object value) throws IllegalAccessException {
        this.ensureObj(obj);
        if (this.isReadOnly()) {
            this.throwFinalFieldIllegalAccessException(value);
        }
        try {
            if (this.isStatic()) {
                this.setter.invokeExact(value);
            } else {
                this.setter.invokeExact(obj, value);
            }
        }
        catch (IllegalArgumentException | NullPointerException e) {
            throw e;
        }
        catch (ClassCastException e) {
            this.throwSetIllegalArgumentException(value);
        }
        catch (Throwable e) {
            throw new InternalError(e);
        }
    }

    @Override
    public void setBoolean(Object obj, boolean z) throws IllegalArgumentException, IllegalAccessException {
        this.throwSetIllegalArgumentException(z);
    }

    @Override
    public void setByte(Object obj, byte b) throws IllegalArgumentException, IllegalAccessException {
        this.throwSetIllegalArgumentException(b);
    }

    @Override
    public void setChar(Object obj, char c) throws IllegalArgumentException, IllegalAccessException {
        this.throwSetIllegalArgumentException(c);
    }

    @Override
    public void setShort(Object obj, short s) throws IllegalArgumentException, IllegalAccessException {
        this.throwSetIllegalArgumentException(s);
    }

    @Override
    public void setInt(Object obj, int i) throws IllegalArgumentException, IllegalAccessException {
        this.throwSetIllegalArgumentException(i);
    }

    @Override
    public void setLong(Object obj, long l) throws IllegalArgumentException, IllegalAccessException {
        this.throwSetIllegalArgumentException(l);
    }

    @Override
    public void setFloat(Object obj, float f) throws IllegalArgumentException, IllegalAccessException {
        this.throwSetIllegalArgumentException(f);
    }

    @Override
    public void setDouble(Object obj, double d) throws IllegalArgumentException, IllegalAccessException {
        this.throwSetIllegalArgumentException(d);
    }
}

