/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.FunctionProviderLocal
 */
package org.lwjgl.system;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.CheckIntrinsics;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.FunctionProvider;
import org.lwjgl.system.FunctionProviderLocal;

public final class Checks {
    public static final boolean CHECKS = Configuration.DISABLE_CHECKS.get(false) == false;
    public static final boolean DEBUG = Configuration.DEBUG.get(false);
    public static final boolean DEBUG_FUNCTIONS = Configuration.DEBUG_FUNCTIONS.get(false);

    private Checks() {
    }

    public static int lengthSafe(short @Nullable [] array) {
        return array == null ? 0 : array.length;
    }

    public static int lengthSafe(int @Nullable [] array) {
        return array == null ? 0 : array.length;
    }

    public static int lengthSafe(long @Nullable [] array) {
        return array == null ? 0 : array.length;
    }

    public static int lengthSafe(float @Nullable [] array) {
        return array == null ? 0 : array.length;
    }

    public static int lengthSafe(double @Nullable [] array) {
        return array == null ? 0 : array.length;
    }

    public static int remainingSafe(@Nullable Buffer buffer) {
        return buffer == null ? 0 : buffer.remaining();
    }

    public static int remainingSafe(@Nullable CustomBuffer<?> buffer) {
        return buffer == null ? 0 : buffer.remaining();
    }

    public static boolean checkFunctions(long ... functions) {
        for (long pointer : functions) {
            if (pointer != 0L) continue;
            return false;
        }
        return true;
    }

    public static boolean checkFunctions(FunctionProvider provider, PointerBuffer caps, int[] indices, String ... functions) {
        boolean available = true;
        for (int i = 0; i < indices.length; ++i) {
            int index = indices[i];
            if (index < 0 || caps.get(index) != 0L) continue;
            long address = provider.getFunctionAddress(functions[i]);
            if (address == 0L) {
                available = false;
                continue;
            }
            caps.put(index, address);
        }
        return available;
    }

    public static boolean checkFunctions(FunctionProviderLocal provider, long handle, PointerBuffer caps, int[] indices, String ... functions) {
        boolean available = true;
        for (int i = 0; i < indices.length; ++i) {
            int index = indices[i];
            if (index < 0 || caps.get(index) != 0L) continue;
            long address = provider.getFunctionAddress(handle, (CharSequence)functions[i]);
            if (address != 0L) {
                caps.put(index, address);
                continue;
            }
            available = false;
        }
        return available;
    }

    public static boolean checkFunctions(FunctionProvider provider, long[] caps, int[] indices, String ... functions) {
        boolean available = true;
        for (int i = 0; i < indices.length; ++i) {
            int index = indices[i];
            if (index < 0 || caps[index] != 0L) continue;
            long address = provider.getFunctionAddress(functions[i]);
            if (address == 0L) {
                available = false;
                continue;
            }
            caps[index] = address;
        }
        return available;
    }

    public static boolean reportMissing(String api, String extension) {
        APIUtil.apiLog("[" + api + "] " + extension + " was reported as available but an entry point is missing.");
        return false;
    }

    public static long check(long pointer) {
        if (pointer == 0L) {
            throw new NullPointerException();
        }
        return pointer;
    }

    private static void assertNT(boolean found) {
        if (!found) {
            throw new IllegalArgumentException("Missing termination");
        }
    }

    public static void checkNT(int[] buf) {
        Checks.checkBuffer(buf.length, 1);
        Checks.assertNT(buf[buf.length - 1] == 0);
    }

    public static void checkNT(int[] buf, int terminator) {
        Checks.checkBuffer(buf.length, 1);
        Checks.assertNT(buf[buf.length - 1] == terminator);
    }

    public static void checkNT(long[] buf) {
        Checks.checkBuffer(buf.length, 1);
        Checks.assertNT(buf[buf.length - 1] == 0L);
    }

    public static void checkNT(float[] buf) {
        Checks.checkBuffer(buf.length, 1);
        Checks.assertNT(buf[buf.length - 1] == 0.0f);
    }

    public static void checkNT1(ByteBuffer buf) {
        Checks.checkBuffer(buf.remaining(), 1);
        Checks.assertNT(buf.get(buf.limit() - 1) == 0);
    }

    public static void checkNT2(ByteBuffer buf) {
        Checks.checkBuffer(buf.remaining(), 2);
        Checks.assertNT(buf.get(buf.limit() - 2) == 0);
    }

    public static void checkNT(IntBuffer buf) {
        Checks.checkBuffer(buf.remaining(), 1);
        Checks.assertNT(buf.get(buf.limit() - 1) == 0);
    }

    public static void checkNT(IntBuffer buf, int terminator) {
        Checks.checkBuffer(buf.remaining(), 1);
        Checks.assertNT(buf.get(buf.limit() - 1) == terminator);
    }

    public static void checkNT(LongBuffer buf) {
        Checks.checkBuffer(buf.remaining(), 1);
        Checks.assertNT(buf.get(buf.limit() - 1) == 0L);
    }

    public static void checkNT(FloatBuffer buf) {
        Checks.checkBuffer(buf.remaining(), 1);
        Checks.assertNT(buf.get(buf.limit() - 1) == 0.0f);
    }

    public static void checkNT(PointerBuffer buf) {
        Checks.checkBuffer(buf.remaining(), 1);
        Checks.assertNT(buf.get(buf.limit() - 1) == 0L);
    }

    public static void checkNT(PointerBuffer buf, long terminator) {
        Checks.checkBuffer(buf.remaining(), 1);
        Checks.assertNT(buf.get(buf.limit() - 1) == terminator);
    }

    public static void checkNTSafe(int @Nullable [] buf) {
        if (buf != null) {
            Checks.checkBuffer(buf.length, 1);
            Checks.assertNT(buf[buf.length - 1] == 0);
        }
    }

    public static void checkNTSafe(int @Nullable [] buf, int terminator) {
        if (buf != null) {
            Checks.checkBuffer(buf.length, 1);
            Checks.assertNT(buf[buf.length - 1] == terminator);
        }
    }

    public static void checkNTSafe(long @Nullable [] buf) {
        if (buf != null) {
            Checks.checkBuffer(buf.length, 1);
            Checks.assertNT(buf[buf.length - 1] == 0L);
        }
    }

    public static void checkNTSafe(float @Nullable [] buf) {
        if (buf != null) {
            Checks.checkBuffer(buf.length, 1);
            Checks.assertNT(buf[buf.length - 1] == 0.0f);
        }
    }

    public static void checkNT1Safe(@Nullable ByteBuffer buf) {
        if (buf != null) {
            Checks.checkBuffer(buf.remaining(), 1);
            Checks.assertNT(buf.get(buf.limit() - 1) == 0);
        }
    }

    public static void checkNT2Safe(@Nullable ByteBuffer buf) {
        if (buf != null) {
            Checks.checkBuffer(buf.remaining(), 2);
            Checks.assertNT(buf.get(buf.limit() - 2) == 0);
        }
    }

    public static void checkNTSafe(@Nullable IntBuffer buf) {
        if (buf != null) {
            Checks.checkBuffer(buf.remaining(), 1);
            Checks.assertNT(buf.get(buf.limit() - 1) == 0);
        }
    }

    public static void checkNTSafe(@Nullable IntBuffer buf, int terminator) {
        if (buf != null) {
            Checks.checkBuffer(buf.remaining(), 1);
            Checks.assertNT(buf.get(buf.limit() - 1) == terminator);
        }
    }

    public static void checkNTSafe(@Nullable LongBuffer buf) {
        if (buf != null) {
            Checks.checkBuffer(buf.remaining(), 1);
            Checks.assertNT(buf.get(buf.limit() - 1) == 0L);
        }
    }

    public static void checkNTSafe(@Nullable FloatBuffer buf) {
        if (buf != null) {
            Checks.checkBuffer(buf.remaining(), 1);
            Checks.assertNT(buf.get(buf.limit() - 1) == 0.0f);
        }
    }

    public static void checkNTSafe(@Nullable PointerBuffer buf) {
        if (buf != null) {
            Checks.checkBuffer(buf.remaining(), 1);
            Checks.assertNT(buf.get(buf.limit() - 1) == 0L);
        }
    }

    public static void checkNTSafe(@Nullable PointerBuffer buf, long terminator) {
        if (buf != null) {
            Checks.checkBuffer(buf.remaining(), 1);
            Checks.assertNT(buf.get(buf.limit() - 1) == terminator);
        }
    }

    private static void checkBuffer(int bufferSize, int minimumSize) {
        if (bufferSize < minimumSize) {
            Checks.throwIAE(bufferSize, minimumSize);
        }
    }

    public static void check(byte[] buf, int size) {
        Checks.checkBuffer(buf.length, size);
    }

    public static void check(short[] buf, int size) {
        Checks.checkBuffer(buf.length, size);
    }

    public static void check(int[] buf, int size) {
        Checks.checkBuffer(buf.length, size);
    }

    public static void check(long[] buf, int size) {
        Checks.checkBuffer(buf.length, size);
    }

    public static void check(float[] buf, int size) {
        Checks.checkBuffer(buf.length, size);
    }

    public static void check(double[] buf, int size) {
        Checks.checkBuffer(buf.length, size);
    }

    public static void check(CharSequence text, int size) {
        Checks.checkBuffer(text.length(), size);
    }

    public static void check(Buffer buf, int size) {
        Checks.checkBuffer(buf.remaining(), size);
    }

    public static void check(Buffer buf, long size) {
        Checks.checkBuffer(buf.remaining(), (int)size);
    }

    public static void check(CustomBuffer<?> buf, int size) {
        Checks.checkBuffer(buf.remaining(), size);
    }

    public static void check(CustomBuffer<?> buf, long size) {
        Checks.checkBuffer(buf.remaining(), (int)size);
    }

    public static void checkSafe(short @Nullable [] buf, int size) {
        if (buf != null) {
            Checks.checkBuffer(buf.length, size);
        }
    }

    public static void checkSafe(int @Nullable [] buf, int size) {
        if (buf != null) {
            Checks.checkBuffer(buf.length, size);
        }
    }

    public static void checkSafe(long @Nullable [] buf, int size) {
        if (buf != null) {
            Checks.checkBuffer(buf.length, size);
        }
    }

    public static void checkSafe(float @Nullable [] buf, int size) {
        if (buf != null) {
            Checks.checkBuffer(buf.length, size);
        }
    }

    public static void checkSafe(double @Nullable [] buf, int size) {
        if (buf != null) {
            Checks.checkBuffer(buf.length, size);
        }
    }

    public static void checkSafe(@Nullable Buffer buf, int size) {
        if (buf != null) {
            Checks.checkBuffer(buf.remaining(), size);
        }
    }

    public static void checkSafe(@Nullable Buffer buf, long size) {
        if (buf != null) {
            Checks.checkBuffer(buf.remaining(), (int)size);
        }
    }

    public static void checkSafe(@Nullable CustomBuffer<?> buf, int size) {
        if (buf != null) {
            Checks.checkBuffer(buf.remaining(), size);
        }
    }

    public static void checkSafe(@Nullable CustomBuffer<?> buf, long size) {
        if (buf != null) {
            Checks.checkBuffer(buf.remaining(), (int)size);
        }
    }

    public static void check(Object[] array, int size) {
        Checks.checkBuffer(array.length, size);
    }

    private static void checkBufferGT(int bufferSize, int maximumSize) {
        if (maximumSize < bufferSize) {
            Checks.throwIAEGT(bufferSize, maximumSize);
        }
    }

    public static void checkGT(Buffer buf, int size) {
        Checks.checkBufferGT(buf.remaining(), size);
    }

    public static void checkGT(CustomBuffer<?> buf, int size) {
        Checks.checkBufferGT(buf.remaining(), size);
    }

    public static long check(int index, int length) {
        if (CHECKS) {
            CheckIntrinsics.checkIndex(index, length);
        }
        return Integer.toUnsignedLong(index);
    }

    private static void throwIAE(int bufferSize, int minimumSize) {
        throw new IllegalArgumentException("Number of remaining elements is " + bufferSize + ", must be at least " + minimumSize);
    }

    private static void throwIAEGT(int bufferSize, int maximumSize) {
        throw new IllegalArgumentException("Number of remaining buffer elements is " + bufferSize + ", must be at most " + maximumSize);
    }

    static {
        if (DEBUG_FUNCTIONS && !DEBUG) {
            APIUtil.DEBUG_STREAM.println("[LWJGL] The DEBUG_FUNCTIONS option requires DEBUG to produce output.");
        }
    }
}

