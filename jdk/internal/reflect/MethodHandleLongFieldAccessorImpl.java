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

class MethodHandleLongFieldAccessorImpl
extends MethodHandleFieldAccessorImpl {
    static FieldAccessorImpl fieldAccessor(Field field, MethodHandle getter, MethodHandle setter, boolean isReadOnly) {
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        if (isStatic) {
            getter = getter.asType(MethodType.methodType(Long.TYPE));
            if (setter != null) {
                setter = setter.asType(MethodType.methodType(Void.TYPE, Long.TYPE));
            }
        } else {
            getter = getter.asType(MethodType.methodType(Long.TYPE, Object.class));
            if (setter != null) {
                setter = setter.asType(MethodType.methodType(Void.TYPE, Object.class, Long.TYPE));
            }
        }
        return new MethodHandleLongFieldAccessorImpl(field, getter, setter, isReadOnly, isStatic);
    }

    MethodHandleLongFieldAccessorImpl(Field field, MethodHandle getter, MethodHandle setter, boolean isReadOnly, boolean isStatic) {
        super(field, getter, setter, isReadOnly, isStatic);
    }

    @Override
    public Object get(Object obj) throws IllegalArgumentException {
        return this.getLong(obj);
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
        try {
            if (this.isStatic()) {
                return this.getter.invokeExact();
            }
            return this.getter.invokeExact(obj);
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
    public float getFloat(Object obj) throws IllegalArgumentException {
        return this.getLong(obj);
    }

    @Override
    public double getDouble(Object obj) throws IllegalArgumentException {
        return this.getLong(obj);
    }

    @Override
    public void set(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException {
        this.ensureObj(obj);
        if (this.isReadOnly()) {
            this.throwFinalFieldIllegalAccessException(value);
        }
        if (value == null) {
            this.throwSetIllegalArgumentException(value);
        }
        if (value instanceof Byte) {
            Byte b = (Byte)value;
            this.setLong(obj, b.byteValue());
        } else if (value instanceof Short) {
            Short s = (Short)value;
            this.setLong(obj, s.shortValue());
        } else if (value instanceof Character) {
            Character c = (Character)value;
            this.setLong(obj, c.charValue());
        } else if (value instanceof Integer) {
            Integer i = (Integer)value;
            this.setLong(obj, i.intValue());
        } else if (value instanceof Long) {
            Long l = (Long)value;
            this.setLong(obj, l);
        } else {
            this.throwSetIllegalArgumentException(value);
        }
    }

    @Override
    public void setBoolean(Object obj, boolean z) throws IllegalArgumentException, IllegalAccessException {
        this.throwSetIllegalArgumentException(z);
    }

    @Override
    public void setByte(Object obj, byte b) throws IllegalArgumentException, IllegalAccessException {
        this.setLong(obj, b);
    }

    @Override
    public void setChar(Object obj, char c) throws IllegalArgumentException, IllegalAccessException {
        this.setLong(obj, c);
    }

    @Override
    public void setShort(Object obj, short s) throws IllegalArgumentException, IllegalAccessException {
        this.setLong(obj, s);
    }

    @Override
    public void setInt(Object obj, int i) throws IllegalArgumentException, IllegalAccessException {
        this.setLong(obj, i);
    }

    @Override
    public void setLong(Object obj, long l) throws IllegalArgumentException, IllegalAccessException {
        if (this.isReadOnly()) {
            this.ensureObj(obj);
            this.throwFinalFieldIllegalAccessException(l);
        }
        try {
            if (this.isStatic()) {
                this.setter.invokeExact(l);
            } else {
                this.setter.invokeExact(obj, l);
            }
        }
        catch (IllegalArgumentException | NullPointerException e) {
            throw e;
        }
        catch (ClassCastException e) {
            throw this.newSetIllegalArgumentException(obj);
        }
        catch (Throwable e) {
            throw new InternalError(e);
        }
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

