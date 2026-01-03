/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.NativeType
 */
package org.lwjgl.system.libc;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.Library;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;

public class LibCStdlib {
    protected LibCStdlib() {
        throw new UnsupportedOperationException();
    }

    public static native long nmalloc(long var0);

    @NativeType(value="void *")
    public static @Nullable ByteBuffer malloc(@NativeType(value="size_t") long size) {
        long __result = LibCStdlib.nmalloc(size);
        return MemoryUtil.memByteBufferSafe(__result, (int)size);
    }

    public static native long ncalloc(long var0, long var2);

    @NativeType(value="void *")
    public static @Nullable ByteBuffer calloc(@NativeType(value="size_t") long nmemb, @NativeType(value="size_t") long size) {
        long __result = LibCStdlib.ncalloc(nmemb, size);
        return MemoryUtil.memByteBufferSafe(__result, (int)nmemb * (int)size);
    }

    public static native long nrealloc(long var0, long var2);

    @NativeType(value="void *")
    public static @Nullable ByteBuffer realloc(@NativeType(value="void *") @Nullable ByteBuffer ptr, @NativeType(value="size_t") long size) {
        long __result = LibCStdlib.nrealloc(MemoryUtil.memAddressSafe(ptr), size);
        return MemoryUtil.memByteBufferSafe(__result, (int)size);
    }

    public static native void nfree(long var0);

    public static void free(@NativeType(value="void *") @Nullable ByteBuffer ptr) {
        LibCStdlib.nfree(MemoryUtil.memAddressSafe(ptr));
    }

    public static void free(@NativeType(value="void *") @Nullable ShortBuffer ptr) {
        LibCStdlib.nfree(MemoryUtil.memAddressSafe(ptr));
    }

    public static void free(@NativeType(value="void *") @Nullable IntBuffer ptr) {
        LibCStdlib.nfree(MemoryUtil.memAddressSafe(ptr));
    }

    public static void free(@NativeType(value="void *") @Nullable LongBuffer ptr) {
        LibCStdlib.nfree(MemoryUtil.memAddressSafe(ptr));
    }

    public static void free(@NativeType(value="void *") @Nullable FloatBuffer ptr) {
        LibCStdlib.nfree(MemoryUtil.memAddressSafe(ptr));
    }

    public static void free(@NativeType(value="void *") @Nullable DoubleBuffer ptr) {
        LibCStdlib.nfree(MemoryUtil.memAddressSafe(ptr));
    }

    public static void free(@NativeType(value="void *") @Nullable PointerBuffer ptr) {
        LibCStdlib.nfree(MemoryUtil.memAddressSafe(ptr));
    }

    public static native long naligned_alloc(long var0, long var2);

    @NativeType(value="void *")
    public static @Nullable ByteBuffer aligned_alloc(@NativeType(value="size_t") long alignment, @NativeType(value="size_t") long size) {
        long __result = LibCStdlib.naligned_alloc(alignment, size);
        return MemoryUtil.memByteBufferSafe(__result, (int)size);
    }

    public static native void naligned_free(long var0);

    public static void aligned_free(@NativeType(value="void *") @Nullable ByteBuffer ptr) {
        LibCStdlib.naligned_free(MemoryUtil.memAddressSafe(ptr));
    }

    public static void aligned_free(@NativeType(value="void *") @Nullable ShortBuffer ptr) {
        LibCStdlib.naligned_free(MemoryUtil.memAddressSafe(ptr));
    }

    public static void aligned_free(@NativeType(value="void *") @Nullable IntBuffer ptr) {
        LibCStdlib.naligned_free(MemoryUtil.memAddressSafe(ptr));
    }

    public static void aligned_free(@NativeType(value="void *") @Nullable LongBuffer ptr) {
        LibCStdlib.naligned_free(MemoryUtil.memAddressSafe(ptr));
    }

    public static void aligned_free(@NativeType(value="void *") @Nullable FloatBuffer ptr) {
        LibCStdlib.naligned_free(MemoryUtil.memAddressSafe(ptr));
    }

    public static void aligned_free(@NativeType(value="void *") @Nullable DoubleBuffer ptr) {
        LibCStdlib.naligned_free(MemoryUtil.memAddressSafe(ptr));
    }

    public static void aligned_free(@NativeType(value="void *") @Nullable PointerBuffer ptr) {
        LibCStdlib.naligned_free(MemoryUtil.memAddressSafe(ptr));
    }

    static {
        Library.initialize();
    }
}

