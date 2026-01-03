/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

public final class ByteArray {
    private static final VarHandle SHORT = ByteArray.create(short[].class);
    private static final VarHandle CHAR = ByteArray.create(char[].class);
    private static final VarHandle INT = ByteArray.create(int[].class);
    private static final VarHandle FLOAT = ByteArray.create(float[].class);
    private static final VarHandle LONG = ByteArray.create(long[].class);
    private static final VarHandle DOUBLE = ByteArray.create(double[].class);

    private ByteArray() {
    }

    public static boolean getBoolean(byte[] array, int offset) {
        return array[offset] != 0;
    }

    public static char getChar(byte[] array, int offset) {
        return CHAR.get(array, offset);
    }

    public static short getShort(byte[] array, int offset) {
        return SHORT.get(array, offset);
    }

    public static int getUnsignedShort(byte[] array, int offset) {
        return Short.toUnsignedInt(SHORT.get(array, offset));
    }

    public static int getInt(byte[] array, int offset) {
        return INT.get(array, offset);
    }

    public static float getFloat(byte[] array, int offset) {
        return Float.intBitsToFloat(INT.get(array, offset));
    }

    public static float getFloatRaw(byte[] array, int offset) {
        return FLOAT.get(array, offset);
    }

    public static long getLong(byte[] array, int offset) {
        return LONG.get(array, offset);
    }

    public static double getDouble(byte[] array, int offset) {
        return Double.longBitsToDouble(LONG.get(array, offset));
    }

    public static double getDoubleRaw(byte[] array, int offset) {
        return DOUBLE.get(array, offset);
    }

    public static void setBoolean(byte[] array, int offset, boolean value) {
        array[offset] = (byte)(value ? 1 : 0);
    }

    public static void setChar(byte[] array, int offset, char value) {
        CHAR.set(array, offset, value);
    }

    public static void setShort(byte[] array, int offset, short value) {
        SHORT.set(array, offset, value);
    }

    public static void setUnsignedShort(byte[] array, int offset, int value) {
        SHORT.set(array, offset, (short)value);
    }

    public static void setInt(byte[] array, int offset, int value) {
        INT.set(array, offset, value);
    }

    public static void setFloat(byte[] array, int offset, float value) {
        INT.set(array, offset, Float.floatToIntBits(value));
    }

    public static void setFloatRaw(byte[] array, int offset, float value) {
        FLOAT.set(array, offset, value);
    }

    public static void setLong(byte[] array, int offset, long value) {
        LONG.set(array, offset, value);
    }

    public static void setDouble(byte[] array, int offset, double value) {
        LONG.set(array, offset, Double.doubleToLongBits(value));
    }

    public static void setDoubleRaw(byte[] array, int offset, double value) {
        DOUBLE.set(array, offset, value);
    }

    private static VarHandle create(Class<?> viewArrayClass) {
        return MethodHandles.byteArrayViewVarHandle(viewArrayClass, ByteOrder.BIG_ENDIAN);
    }
}

