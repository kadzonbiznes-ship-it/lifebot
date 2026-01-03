/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.CLongBuffer
 *  org.lwjgl.system.MemoryManage$DebugAllocator
 *  org.lwjgl.system.MemoryUtil$MemoryAllocationReport
 *  org.lwjgl.system.MemoryUtil$MemoryAllocationReport$Aggregate
 */
package org.lwjgl.system;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.LongPredicate;
import org.jspecify.annotations.Nullable;
import org.lwjgl.CLongBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Checks;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.Library;
import org.lwjgl.system.MathUtil;
import org.lwjgl.system.MemoryManage;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.MultiReleaseMemCopy;
import org.lwjgl.system.MultiReleaseTextDecoding;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.Struct;
import org.lwjgl.system.jni.JNINativeInterface;
import org.lwjgl.system.libc.LibCString;
import sun.misc.Unsafe;

public final class MemoryUtil {
    public static final long NULL = 0L;
    public static final int PAGE_SIZE;
    public static final int CACHE_LINE_SIZE;
    static final int ARRAY_TLC_SIZE;
    static final ThreadLocal<byte[]> ARRAY_TLC_BYTE;
    static final ThreadLocal<char[]> ARRAY_TLC_CHAR;
    static final Unsafe UNSAFE;
    static final ByteOrder NATIVE_ORDER;
    private static final Charset UTF16;
    static final Class<? extends ByteBuffer> BUFFER_BYTE;
    static final Class<? extends ShortBuffer> BUFFER_SHORT;
    static final Class<? extends CharBuffer> BUFFER_CHAR;
    static final Class<? extends IntBuffer> BUFFER_INT;
    static final Class<? extends LongBuffer> BUFFER_LONG;
    static final Class<? extends FloatBuffer> BUFFER_FLOAT;
    static final Class<? extends DoubleBuffer> BUFFER_DOUBLE;
    private static final long MARK;
    private static final long POSITION;
    private static final long LIMIT;
    private static final long CAPACITY;
    private static final long ADDRESS;
    private static final long PARENT_BYTE;
    private static final long PARENT_SHORT;
    private static final long PARENT_CHAR;
    private static final long PARENT_INT;
    private static final long PARENT_LONG;
    private static final long PARENT_FLOAT;
    private static final long PARENT_DOUBLE;
    private static final int FILL_PATTERN_32;
    private static final long FILL_PATTERN_64;
    private static final int MAGIC_CAPACITY = 219540062;
    private static final int MAGIC_POSITION = 16435934;

    private MemoryUtil() {
    }

    public static MemoryAllocator getAllocator() {
        return MemoryUtil.getAllocator(false);
    }

    public static MemoryAllocator getAllocator(boolean tracked) {
        return tracked ? LazyInit.ALLOCATOR : LazyInit.ALLOCATOR_IMPL;
    }

    public static long nmemAlloc(long size) {
        return LazyInit.ALLOCATOR.malloc(size);
    }

    public static long nmemAllocChecked(long size) {
        long address = MemoryUtil.nmemAlloc(size != 0L ? size : 1L);
        if (Checks.CHECKS && address == 0L) {
            throw new OutOfMemoryError();
        }
        return address;
    }

    private static long getAllocationSize(int elements, int elementShift) {
        return APIUtil.apiCheckAllocation(elements, Integer.toUnsignedLong(elements) << elementShift, Pointer.BITS64 ? Long.MAX_VALUE : 0xFFFFFFFFL);
    }

    public static ByteBuffer memAlloc(int size) {
        return MemoryUtil.wrapBufferByte(MemoryUtil.nmemAllocChecked(size), size);
    }

    public static ShortBuffer memAllocShort(int size) {
        return MemoryUtil.wrapBufferShort(MemoryUtil.nmemAllocChecked(MemoryUtil.getAllocationSize(size, 1)), size);
    }

    public static IntBuffer memAllocInt(int size) {
        return MemoryUtil.wrapBufferInt(MemoryUtil.nmemAllocChecked(MemoryUtil.getAllocationSize(size, 2)), size);
    }

    public static FloatBuffer memAllocFloat(int size) {
        return MemoryUtil.wrapBufferFloat(MemoryUtil.nmemAllocChecked(MemoryUtil.getAllocationSize(size, 2)), size);
    }

    public static LongBuffer memAllocLong(int size) {
        return MemoryUtil.wrapBufferLong(MemoryUtil.nmemAllocChecked(MemoryUtil.getAllocationSize(size, 3)), size);
    }

    public static CLongBuffer memAllocCLong(int size) {
        return CLongBuffer.create((long)MemoryUtil.nmemAllocChecked(MemoryUtil.getAllocationSize(size, Pointer.CLONG_SHIFT)), (int)size);
    }

    public static DoubleBuffer memAllocDouble(int size) {
        return MemoryUtil.wrapBufferDouble(MemoryUtil.nmemAllocChecked(MemoryUtil.getAllocationSize(size, 3)), size);
    }

    public static PointerBuffer memAllocPointer(int size) {
        return PointerBuffer.create(MemoryUtil.nmemAllocChecked(MemoryUtil.getAllocationSize(size, Pointer.POINTER_SHIFT)), size);
    }

    public static void nmemFree(long ptr) {
        LazyInit.ALLOCATOR.free(ptr);
    }

    public static void memFree(@Nullable Buffer ptr) {
        if (ptr != null) {
            MemoryUtil.nmemFree(UNSAFE.getLong(ptr, ADDRESS));
        }
    }

    public static void memFree(@Nullable ByteBuffer ptr) {
        if (ptr != null) {
            MemoryUtil.nmemFree(UNSAFE.getLong(ptr, ADDRESS));
        }
    }

    public static void memFree(@Nullable ShortBuffer ptr) {
        if (ptr != null) {
            MemoryUtil.nmemFree(UNSAFE.getLong(ptr, ADDRESS));
        }
    }

    public static void memFree(@Nullable CharBuffer ptr) {
        if (ptr != null) {
            MemoryUtil.nmemFree(UNSAFE.getLong(ptr, ADDRESS));
        }
    }

    public static void memFree(@Nullable IntBuffer ptr) {
        if (ptr != null) {
            MemoryUtil.nmemFree(UNSAFE.getLong(ptr, ADDRESS));
        }
    }

    public static void memFree(@Nullable LongBuffer ptr) {
        if (ptr != null) {
            MemoryUtil.nmemFree(UNSAFE.getLong(ptr, ADDRESS));
        }
    }

    public static void memFree(@Nullable FloatBuffer ptr) {
        if (ptr != null) {
            MemoryUtil.nmemFree(UNSAFE.getLong(ptr, ADDRESS));
        }
    }

    public static void memFree(@Nullable DoubleBuffer ptr) {
        if (ptr != null) {
            MemoryUtil.nmemFree(UNSAFE.getLong(ptr, ADDRESS));
        }
    }

    public static void memFree(@Nullable CustomBuffer<?> ptr) {
        if (ptr != null) {
            MemoryUtil.nmemFree(ptr.address);
        }
    }

    public static long nmemCalloc(long num, long size) {
        return LazyInit.ALLOCATOR.calloc(num, size);
    }

    public static long nmemCallocChecked(long num, long size) {
        if (num == 0L || size == 0L) {
            num = 1L;
            size = 1L;
        }
        long address = MemoryUtil.nmemCalloc(num, size);
        if (Checks.CHECKS && address == 0L) {
            throw new OutOfMemoryError();
        }
        return address;
    }

    public static ByteBuffer memCalloc(int num, int size) {
        return MemoryUtil.wrapBufferByte(MemoryUtil.nmemCallocChecked(num, size), num * size);
    }

    public static ByteBuffer memCalloc(int num) {
        return MemoryUtil.wrapBufferByte(MemoryUtil.nmemCallocChecked(num, 1L), num);
    }

    public static ShortBuffer memCallocShort(int num) {
        return MemoryUtil.wrapBufferShort(MemoryUtil.nmemCallocChecked(num, 2L), num);
    }

    public static IntBuffer memCallocInt(int num) {
        return MemoryUtil.wrapBufferInt(MemoryUtil.nmemCallocChecked(num, 4L), num);
    }

    public static FloatBuffer memCallocFloat(int num) {
        return MemoryUtil.wrapBufferFloat(MemoryUtil.nmemCallocChecked(num, 4L), num);
    }

    public static LongBuffer memCallocLong(int num) {
        return MemoryUtil.wrapBufferLong(MemoryUtil.nmemCallocChecked(num, 8L), num);
    }

    public static CLongBuffer memCallocCLong(int num) {
        return CLongBuffer.create((long)MemoryUtil.nmemCallocChecked(num, Pointer.CLONG_SIZE), (int)num);
    }

    public static DoubleBuffer memCallocDouble(int num) {
        return MemoryUtil.wrapBufferDouble(MemoryUtil.nmemCallocChecked(num, 8L), num);
    }

    public static PointerBuffer memCallocPointer(int num) {
        return PointerBuffer.create(MemoryUtil.nmemCallocChecked(num, Pointer.POINTER_SIZE), num);
    }

    public static long nmemRealloc(long ptr, long size) {
        return LazyInit.ALLOCATOR.realloc(ptr, size);
    }

    public static long nmemReallocChecked(long ptr, long size) {
        long address = MemoryUtil.nmemRealloc(ptr, size != 0L ? size : 1L);
        if (Checks.CHECKS && address == 0L) {
            throw new OutOfMemoryError();
        }
        return address;
    }

    private static <T extends Buffer> T realloc(@Nullable T old_p, T new_p, int size) {
        if (old_p != null) {
            new_p.position(Math.min(old_p.position(), size));
        }
        return new_p;
    }

    public static ByteBuffer memRealloc(@Nullable ByteBuffer ptr, int size) {
        return MemoryUtil.realloc(ptr, MemoryUtil.memByteBuffer(MemoryUtil.nmemReallocChecked(ptr == null ? 0L : UNSAFE.getLong(ptr, ADDRESS), size), size), size);
    }

    public static ShortBuffer memRealloc(@Nullable ShortBuffer ptr, int size) {
        return MemoryUtil.realloc(ptr, MemoryUtil.memShortBuffer(MemoryUtil.nmemReallocChecked(ptr == null ? 0L : UNSAFE.getLong(ptr, ADDRESS), MemoryUtil.getAllocationSize(size, 1)), size), size);
    }

    public static IntBuffer memRealloc(@Nullable IntBuffer ptr, int size) {
        return MemoryUtil.realloc(ptr, MemoryUtil.memIntBuffer(MemoryUtil.nmemReallocChecked(ptr == null ? 0L : UNSAFE.getLong(ptr, ADDRESS), MemoryUtil.getAllocationSize(size, 2)), size), size);
    }

    public static LongBuffer memRealloc(@Nullable LongBuffer ptr, int size) {
        return MemoryUtil.realloc(ptr, MemoryUtil.memLongBuffer(MemoryUtil.nmemReallocChecked(ptr == null ? 0L : UNSAFE.getLong(ptr, ADDRESS), MemoryUtil.getAllocationSize(size, 3)), size), size);
    }

    public static CLongBuffer memRealloc(@Nullable CLongBuffer ptr, int size) {
        CLongBuffer buffer = MemoryUtil.memCLongBuffer(MemoryUtil.nmemReallocChecked(ptr == null ? 0L : ptr.address, MemoryUtil.getAllocationSize(size, Pointer.CLONG_SIZE)), size);
        if (ptr != null) {
            buffer.position(Math.min(ptr.position(), size));
        }
        return buffer;
    }

    public static FloatBuffer memRealloc(@Nullable FloatBuffer ptr, int size) {
        return MemoryUtil.realloc(ptr, MemoryUtil.memFloatBuffer(MemoryUtil.nmemReallocChecked(ptr == null ? 0L : UNSAFE.getLong(ptr, ADDRESS), MemoryUtil.getAllocationSize(size, 2)), size), size);
    }

    public static DoubleBuffer memRealloc(@Nullable DoubleBuffer ptr, int size) {
        return MemoryUtil.realloc(ptr, MemoryUtil.memDoubleBuffer(MemoryUtil.nmemReallocChecked(ptr == null ? 0L : UNSAFE.getLong(ptr, ADDRESS), MemoryUtil.getAllocationSize(size, 3)), size), size);
    }

    public static PointerBuffer memRealloc(@Nullable PointerBuffer ptr, int size) {
        PointerBuffer buffer = MemoryUtil.memPointerBuffer(MemoryUtil.nmemReallocChecked(ptr == null ? 0L : ptr.address, MemoryUtil.getAllocationSize(size, Pointer.POINTER_SHIFT)), size);
        if (ptr != null) {
            buffer.position(Math.min(ptr.position(), size));
        }
        return buffer;
    }

    public static long nmemAlignedAlloc(long alignment, long size) {
        return LazyInit.ALLOCATOR.aligned_alloc(alignment, size);
    }

    public static long nmemAlignedAllocChecked(long alignment, long size) {
        long address = MemoryUtil.nmemAlignedAlloc(alignment, size != 0L ? size : 1L);
        if (Checks.CHECKS && address == 0L) {
            throw new OutOfMemoryError();
        }
        return address;
    }

    public static ByteBuffer memAlignedAlloc(int alignment, int size) {
        return MemoryUtil.wrapBufferByte(MemoryUtil.nmemAlignedAllocChecked(alignment, size), size);
    }

    public static void nmemAlignedFree(long ptr) {
        LazyInit.ALLOCATOR.aligned_free(ptr);
    }

    public static void memAlignedFree(@Nullable ByteBuffer ptr) {
        if (ptr != null) {
            MemoryUtil.nmemAlignedFree(UNSAFE.getLong(ptr, ADDRESS));
        }
    }

    public static void memReport(MemoryAllocationReport report) {
        MemoryManage.DebugAllocator.report((MemoryAllocationReport)report);
    }

    public static void memReport(MemoryAllocationReport report, MemoryAllocationReport.Aggregate groupByStackTrace, boolean groupByThread) {
        MemoryManage.DebugAllocator.report((MemoryAllocationReport)report, (MemoryAllocationReport.Aggregate)groupByStackTrace, (boolean)groupByThread);
    }

    public static long memAddress0(Buffer buffer) {
        return UNSAFE.getLong(buffer, ADDRESS);
    }

    public static long memAddress0(ByteBuffer buffer) {
        return UNSAFE.getLong(buffer, ADDRESS);
    }

    public static long memAddress0(ShortBuffer buffer) {
        return UNSAFE.getLong(buffer, ADDRESS);
    }

    public static long memAddress0(CharBuffer buffer) {
        return UNSAFE.getLong(buffer, ADDRESS);
    }

    public static long memAddress0(IntBuffer buffer) {
        return UNSAFE.getLong(buffer, ADDRESS);
    }

    public static long memAddress0(LongBuffer buffer) {
        return UNSAFE.getLong(buffer, ADDRESS);
    }

    public static long memAddress0(FloatBuffer buffer) {
        return UNSAFE.getLong(buffer, ADDRESS);
    }

    public static long memAddress0(DoubleBuffer buffer) {
        return UNSAFE.getLong(buffer, ADDRESS);
    }

    public static long memAddress(ByteBuffer buffer) {
        return (long)buffer.position() + MemoryUtil.memAddress0(buffer);
    }

    public static long memAddress(ByteBuffer buffer, int position) {
        Objects.requireNonNull(buffer);
        return MemoryUtil.memAddress0(buffer) + Integer.toUnsignedLong(position);
    }

    private static long address(int position, int elementShift, long address) {
        return address + (((long)position & 0xFFFFFFFFL) << elementShift);
    }

    public static long memAddress(ShortBuffer buffer) {
        return MemoryUtil.address(buffer.position(), 1, MemoryUtil.memAddress0(buffer));
    }

    public static long memAddress(ShortBuffer buffer, int position) {
        Objects.requireNonNull(buffer);
        return MemoryUtil.address(position, 1, MemoryUtil.memAddress0(buffer));
    }

    public static long memAddress(CharBuffer buffer) {
        return MemoryUtil.address(buffer.position(), 1, MemoryUtil.memAddress0(buffer));
    }

    public static long memAddress(CharBuffer buffer, int position) {
        Objects.requireNonNull(buffer);
        return MemoryUtil.address(position, 1, MemoryUtil.memAddress0(buffer));
    }

    public static long memAddress(IntBuffer buffer) {
        return MemoryUtil.address(buffer.position(), 2, MemoryUtil.memAddress0(buffer));
    }

    public static long memAddress(IntBuffer buffer, int position) {
        Objects.requireNonNull(buffer);
        return MemoryUtil.address(position, 2, MemoryUtil.memAddress0(buffer));
    }

    public static long memAddress(FloatBuffer buffer) {
        return MemoryUtil.address(buffer.position(), 2, MemoryUtil.memAddress0(buffer));
    }

    public static long memAddress(FloatBuffer buffer, int position) {
        Objects.requireNonNull(buffer);
        return MemoryUtil.address(position, 2, MemoryUtil.memAddress0(buffer));
    }

    public static long memAddress(LongBuffer buffer) {
        return MemoryUtil.address(buffer.position(), 3, MemoryUtil.memAddress0(buffer));
    }

    public static long memAddress(LongBuffer buffer, int position) {
        Objects.requireNonNull(buffer);
        return MemoryUtil.address(position, 3, MemoryUtil.memAddress0(buffer));
    }

    public static long memAddress(DoubleBuffer buffer) {
        return MemoryUtil.address(buffer.position(), 3, MemoryUtil.memAddress0(buffer));
    }

    public static long memAddress(DoubleBuffer buffer, int position) {
        Objects.requireNonNull(buffer);
        return MemoryUtil.address(position, 3, MemoryUtil.memAddress0(buffer));
    }

    public static long memAddress(Buffer buffer) {
        int elementShift = buffer instanceof ByteBuffer ? 0 : (buffer instanceof ShortBuffer || buffer instanceof CharBuffer ? 1 : (buffer instanceof IntBuffer || buffer instanceof FloatBuffer ? 2 : 3));
        return MemoryUtil.address(buffer.position(), elementShift, UNSAFE.getLong(buffer, ADDRESS));
    }

    public static long memAddress(CustomBuffer<?> buffer) {
        return buffer.address();
    }

    public static long memAddress(CustomBuffer<?> buffer, int position) {
        return buffer.address(position);
    }

    public static long memAddressSafe(@Nullable ByteBuffer buffer) {
        return buffer == null ? 0L : MemoryUtil.memAddress0(buffer) + (long)buffer.position();
    }

    public static long memAddressSafe(@Nullable ShortBuffer buffer) {
        return buffer == null ? 0L : MemoryUtil.address(buffer.position(), 1, MemoryUtil.memAddress0(buffer));
    }

    public static long memAddressSafe(@Nullable CharBuffer buffer) {
        return buffer == null ? 0L : MemoryUtil.address(buffer.position(), 1, MemoryUtil.memAddress0(buffer));
    }

    public static long memAddressSafe(@Nullable IntBuffer buffer) {
        return buffer == null ? 0L : MemoryUtil.address(buffer.position(), 2, MemoryUtil.memAddress0(buffer));
    }

    public static long memAddressSafe(@Nullable FloatBuffer buffer) {
        return buffer == null ? 0L : MemoryUtil.address(buffer.position(), 2, MemoryUtil.memAddress0(buffer));
    }

    public static long memAddressSafe(@Nullable LongBuffer buffer) {
        return buffer == null ? 0L : MemoryUtil.address(buffer.position(), 3, MemoryUtil.memAddress0(buffer));
    }

    public static long memAddressSafe(@Nullable DoubleBuffer buffer) {
        return buffer == null ? 0L : MemoryUtil.address(buffer.position(), 3, MemoryUtil.memAddress0(buffer));
    }

    public static long memAddressSafe(@Nullable Pointer pointer) {
        return pointer == null ? 0L : pointer.address();
    }

    public static ByteBuffer memByteBuffer(long address, int capacity) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return MemoryUtil.wrapBufferByte(address, capacity);
    }

    public static @Nullable ByteBuffer memByteBufferSafe(long address, int capacity) {
        return address == 0L ? null : MemoryUtil.wrapBufferByte(address, capacity);
    }

    public static ByteBuffer memByteBuffer(ShortBuffer buffer) {
        if (Checks.CHECKS && 0x3FFFFFFF < buffer.remaining()) {
            throw new IllegalStateException("The source buffer range is too wide");
        }
        return MemoryUtil.wrapBufferByte(MemoryUtil.memAddress(buffer), buffer.remaining() << 1);
    }

    public static ByteBuffer memByteBuffer(CharBuffer buffer) {
        if (Checks.CHECKS && 0x3FFFFFFF < buffer.remaining()) {
            throw new IllegalStateException("The source buffer range is too wide");
        }
        return MemoryUtil.wrapBufferByte(MemoryUtil.memAddress(buffer), buffer.remaining() << 1);
    }

    public static ByteBuffer memByteBuffer(IntBuffer buffer) {
        if (Checks.CHECKS && 0x1FFFFFFF < buffer.remaining()) {
            throw new IllegalStateException("The source buffer range is too wide");
        }
        return MemoryUtil.wrapBufferByte(MemoryUtil.memAddress(buffer), buffer.remaining() << 2);
    }

    public static ByteBuffer memByteBuffer(LongBuffer buffer) {
        if (Checks.CHECKS && 0xFFFFFFF < buffer.remaining()) {
            throw new IllegalStateException("The source buffer range is too wide");
        }
        return MemoryUtil.wrapBufferByte(MemoryUtil.memAddress(buffer), buffer.remaining() << 3);
    }

    public static ByteBuffer memByteBuffer(FloatBuffer buffer) {
        if (Checks.CHECKS && 0x1FFFFFFF < buffer.remaining()) {
            throw new IllegalStateException("The source buffer range is too wide");
        }
        return MemoryUtil.wrapBufferByte(MemoryUtil.memAddress(buffer), buffer.remaining() << 2);
    }

    public static ByteBuffer memByteBuffer(DoubleBuffer buffer) {
        if (Checks.CHECKS && 0xFFFFFFF < buffer.remaining()) {
            throw new IllegalStateException("The source buffer range is too wide");
        }
        return MemoryUtil.wrapBufferByte(MemoryUtil.memAddress(buffer), buffer.remaining() << 3);
    }

    public static ByteBuffer memByteBuffer(CustomBuffer<?> buffer) {
        if (Checks.CHECKS && Integer.MAX_VALUE / buffer.sizeof() < buffer.remaining()) {
            throw new IllegalStateException("The source buffer range is too wide");
        }
        return MemoryUtil.wrapBufferByte(MemoryUtil.memAddress(buffer), buffer.remaining() * buffer.sizeof());
    }

    public static <T extends Struct<T>> ByteBuffer memByteBuffer(T value) {
        return MemoryUtil.wrapBufferByte(value.address, value.sizeof());
    }

    public static ShortBuffer memShortBuffer(long address, int capacity) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return MemoryUtil.wrapBufferShort(address, capacity);
    }

    public static @Nullable ShortBuffer memShortBufferSafe(long address, int capacity) {
        return address == 0L ? null : MemoryUtil.wrapBufferShort(address, capacity);
    }

    public static CharBuffer memCharBuffer(long address, int capacity) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return MemoryUtil.wrapBufferChar(address, capacity);
    }

    public static @Nullable CharBuffer memCharBufferSafe(long address, int capacity) {
        return address == 0L ? null : MemoryUtil.wrapBufferChar(address, capacity);
    }

    public static IntBuffer memIntBuffer(long address, int capacity) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return MemoryUtil.wrapBufferInt(address, capacity);
    }

    public static @Nullable IntBuffer memIntBufferSafe(long address, int capacity) {
        return address == 0L ? null : MemoryUtil.wrapBufferInt(address, capacity);
    }

    public static LongBuffer memLongBuffer(long address, int capacity) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return MemoryUtil.wrapBufferLong(address, capacity);
    }

    public static @Nullable LongBuffer memLongBufferSafe(long address, int capacity) {
        return address == 0L ? null : MemoryUtil.wrapBufferLong(address, capacity);
    }

    public static CLongBuffer memCLongBuffer(long address, int capacity) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return CLongBuffer.create((long)address, (int)capacity);
    }

    public static @Nullable CLongBuffer memCLongBufferSafe(long address, int capacity) {
        return address == 0L ? null : CLongBuffer.create((long)address, (int)capacity);
    }

    public static FloatBuffer memFloatBuffer(long address, int capacity) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return MemoryUtil.wrapBufferFloat(address, capacity);
    }

    public static @Nullable FloatBuffer memFloatBufferSafe(long address, int capacity) {
        return address == 0L ? null : MemoryUtil.wrapBufferFloat(address, capacity);
    }

    public static DoubleBuffer memDoubleBuffer(long address, int capacity) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return MemoryUtil.wrapBufferDouble(address, capacity);
    }

    public static @Nullable DoubleBuffer memDoubleBufferSafe(long address, int capacity) {
        return address == 0L ? null : MemoryUtil.wrapBufferDouble(address, capacity);
    }

    public static PointerBuffer memPointerBuffer(long address, int capacity) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return PointerBuffer.create(address, capacity);
    }

    public static @Nullable PointerBuffer memPointerBufferSafe(long address, int capacity) {
        return address == 0L ? null : PointerBuffer.create(address, capacity);
    }

    public static ByteBuffer memDuplicate(ByteBuffer buffer) {
        ByteBuffer target;
        try {
            target = (ByteBuffer)UNSAFE.allocateInstance(BUFFER_BYTE);
        }
        catch (InstantiationException e) {
            throw new UnsupportedOperationException(e);
        }
        UNSAFE.putLong(target, ADDRESS, UNSAFE.getLong(buffer, ADDRESS));
        UNSAFE.putInt(target, MARK, UNSAFE.getInt(buffer, MARK));
        UNSAFE.putInt(target, POSITION, UNSAFE.getInt(buffer, POSITION));
        UNSAFE.putInt(target, LIMIT, UNSAFE.getInt(buffer, LIMIT));
        UNSAFE.putInt(target, CAPACITY, UNSAFE.getInt(buffer, CAPACITY));
        Object attachment = UNSAFE.getObject(buffer, PARENT_BYTE);
        UNSAFE.putObject(target, PARENT_BYTE, attachment == null ? buffer : attachment);
        return target.order(buffer.order());
    }

    public static ShortBuffer memDuplicate(ShortBuffer buffer) {
        return MemoryUtil.duplicate(BUFFER_SHORT, buffer, PARENT_SHORT);
    }

    public static CharBuffer memDuplicate(CharBuffer buffer) {
        return MemoryUtil.duplicate(BUFFER_CHAR, buffer, PARENT_CHAR);
    }

    public static IntBuffer memDuplicate(IntBuffer buffer) {
        return MemoryUtil.duplicate(BUFFER_INT, buffer, PARENT_INT);
    }

    public static LongBuffer memDuplicate(LongBuffer buffer) {
        return MemoryUtil.duplicate(BUFFER_LONG, buffer, PARENT_LONG);
    }

    public static FloatBuffer memDuplicate(FloatBuffer buffer) {
        return MemoryUtil.duplicate(BUFFER_FLOAT, buffer, PARENT_FLOAT);
    }

    public static DoubleBuffer memDuplicate(DoubleBuffer buffer) {
        return MemoryUtil.duplicate(BUFFER_DOUBLE, buffer, PARENT_DOUBLE);
    }

    public static ByteBuffer memSlice(ByteBuffer buffer) {
        return MemoryUtil.slice(buffer, MemoryUtil.memAddress0(buffer) + (long)buffer.position(), buffer.remaining());
    }

    public static ShortBuffer memSlice(ShortBuffer buffer) {
        return MemoryUtil.slice(BUFFER_SHORT, buffer, MemoryUtil.address(buffer.position(), 1, MemoryUtil.memAddress0(buffer)), buffer.remaining(), PARENT_SHORT);
    }

    public static CharBuffer memSlice(CharBuffer buffer) {
        return MemoryUtil.slice(BUFFER_CHAR, buffer, MemoryUtil.address(buffer.position(), 1, MemoryUtil.memAddress0(buffer)), buffer.remaining(), PARENT_CHAR);
    }

    public static IntBuffer memSlice(IntBuffer buffer) {
        return MemoryUtil.slice(BUFFER_INT, buffer, MemoryUtil.address(buffer.position(), 2, MemoryUtil.memAddress0(buffer)), buffer.remaining(), PARENT_INT);
    }

    public static LongBuffer memSlice(LongBuffer buffer) {
        return MemoryUtil.slice(BUFFER_LONG, buffer, MemoryUtil.address(buffer.position(), 3, MemoryUtil.memAddress0(buffer)), buffer.remaining(), PARENT_LONG);
    }

    public static FloatBuffer memSlice(FloatBuffer buffer) {
        return MemoryUtil.slice(BUFFER_FLOAT, buffer, MemoryUtil.address(buffer.position(), 2, MemoryUtil.memAddress0(buffer)), buffer.remaining(), PARENT_FLOAT);
    }

    public static DoubleBuffer memSlice(DoubleBuffer buffer) {
        return MemoryUtil.slice(BUFFER_DOUBLE, buffer, MemoryUtil.address(buffer.position(), 3, MemoryUtil.memAddress0(buffer)), buffer.remaining(), PARENT_DOUBLE);
    }

    public static ByteBuffer memSlice(ByteBuffer buffer, int offset, int capacity) {
        int position = buffer.position() + offset;
        if (offset < 0 || buffer.limit() < position) {
            throw new IllegalArgumentException();
        }
        if (capacity < 0 || buffer.capacity() - position < capacity) {
            throw new IllegalArgumentException();
        }
        return MemoryUtil.slice(buffer, MemoryUtil.memAddress0(buffer) + (long)position, capacity);
    }

    public static ShortBuffer memSlice(ShortBuffer buffer, int offset, int capacity) {
        int position = buffer.position() + offset;
        if (offset < 0 || buffer.limit() < position) {
            throw new IllegalArgumentException();
        }
        if (capacity < 0 || buffer.capacity() - position < capacity) {
            throw new IllegalArgumentException();
        }
        return MemoryUtil.slice(BUFFER_SHORT, buffer, MemoryUtil.address(position, 1, MemoryUtil.memAddress0(buffer)), capacity, PARENT_SHORT);
    }

    public static CharBuffer memSlice(CharBuffer buffer, int offset, int capacity) {
        int position = buffer.position() + offset;
        if (offset < 0 || buffer.limit() < position) {
            throw new IllegalArgumentException();
        }
        if (capacity < 0 || buffer.capacity() - position < capacity) {
            throw new IllegalArgumentException();
        }
        return MemoryUtil.slice(BUFFER_CHAR, buffer, MemoryUtil.address(position, 1, MemoryUtil.memAddress0(buffer)), capacity, PARENT_CHAR);
    }

    public static IntBuffer memSlice(IntBuffer buffer, int offset, int capacity) {
        int position = buffer.position() + offset;
        if (offset < 0 || buffer.limit() < position) {
            throw new IllegalArgumentException();
        }
        if (capacity < 0 || buffer.capacity() - position < capacity) {
            throw new IllegalArgumentException();
        }
        return MemoryUtil.slice(BUFFER_INT, buffer, MemoryUtil.address(position, 2, MemoryUtil.memAddress0(buffer)), capacity, PARENT_INT);
    }

    public static LongBuffer memSlice(LongBuffer buffer, int offset, int capacity) {
        int position = buffer.position() + offset;
        if (offset < 0 || buffer.limit() < position) {
            throw new IllegalArgumentException();
        }
        if (capacity < 0 || buffer.capacity() - position < capacity) {
            throw new IllegalArgumentException();
        }
        return MemoryUtil.slice(BUFFER_LONG, buffer, MemoryUtil.address(position, 3, MemoryUtil.memAddress0(buffer)), capacity, PARENT_LONG);
    }

    public static FloatBuffer memSlice(FloatBuffer buffer, int offset, int capacity) {
        int position = buffer.position() + offset;
        if (offset < 0 || buffer.limit() < position) {
            throw new IllegalArgumentException();
        }
        if (capacity < 0 || buffer.capacity() - position < capacity) {
            throw new IllegalArgumentException();
        }
        return MemoryUtil.slice(BUFFER_FLOAT, buffer, MemoryUtil.address(position, 2, MemoryUtil.memAddress0(buffer)), capacity, PARENT_FLOAT);
    }

    public static DoubleBuffer memSlice(DoubleBuffer buffer, int offset, int capacity) {
        int position = buffer.position() + offset;
        if (offset < 0 || buffer.limit() < position) {
            throw new IllegalArgumentException();
        }
        if (capacity < 0 || buffer.capacity() - position < capacity) {
            throw new IllegalArgumentException();
        }
        return MemoryUtil.slice(BUFFER_DOUBLE, buffer, MemoryUtil.address(position, 3, MemoryUtil.memAddress0(buffer)), capacity, PARENT_DOUBLE);
    }

    public static <T extends CustomBuffer<T>> T memSlice(T buffer, int offset, int capacity) {
        return buffer.slice(offset, capacity);
    }

    public static void memSet(ByteBuffer ptr, int value) {
        MemoryUtil.memSet(MemoryUtil.memAddress(ptr), value, ptr.remaining());
    }

    public static void memSet(ShortBuffer ptr, int value) {
        MemoryUtil.memSet(MemoryUtil.memAddress(ptr), value, APIUtil.apiGetBytes(ptr.remaining(), 1));
    }

    public static void memSet(CharBuffer ptr, int value) {
        MemoryUtil.memSet(MemoryUtil.memAddress(ptr), value, APIUtil.apiGetBytes(ptr.remaining(), 1));
    }

    public static void memSet(IntBuffer ptr, int value) {
        MemoryUtil.memSet(MemoryUtil.memAddress(ptr), value, APIUtil.apiGetBytes(ptr.remaining(), 2));
    }

    public static void memSet(LongBuffer ptr, int value) {
        MemoryUtil.memSet(MemoryUtil.memAddress(ptr), value, APIUtil.apiGetBytes(ptr.remaining(), 3));
    }

    public static void memSet(FloatBuffer ptr, int value) {
        MemoryUtil.memSet(MemoryUtil.memAddress(ptr), value, APIUtil.apiGetBytes(ptr.remaining(), 2));
    }

    public static void memSet(DoubleBuffer ptr, int value) {
        MemoryUtil.memSet(MemoryUtil.memAddress(ptr), value, APIUtil.apiGetBytes(ptr.remaining(), 3));
    }

    public static <T extends CustomBuffer<T>> void memSet(T ptr, int value) {
        MemoryUtil.memSet(MemoryUtil.memAddress(ptr), value, Integer.toUnsignedLong(ptr.remaining()) * (long)ptr.sizeof());
    }

    public static <T extends Struct<T>> void memSet(T ptr, int value) {
        MemoryUtil.memSet(ptr.address, value, ptr.sizeof());
    }

    public static void memCopy(ByteBuffer src, ByteBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.remaining());
        }
        MultiReleaseMemCopy.copy(MemoryUtil.memAddress(src), MemoryUtil.memAddress(dst), src.remaining());
    }

    public static void memCopy(ShortBuffer src, ShortBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.remaining());
        }
        MultiReleaseMemCopy.copy(MemoryUtil.memAddress(src), MemoryUtil.memAddress(dst), APIUtil.apiGetBytes(src.remaining(), 1));
    }

    public static void memCopy(CharBuffer src, CharBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.remaining());
        }
        MultiReleaseMemCopy.copy(MemoryUtil.memAddress(src), MemoryUtil.memAddress(dst), APIUtil.apiGetBytes(src.remaining(), 1));
    }

    public static void memCopy(IntBuffer src, IntBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.remaining());
        }
        MultiReleaseMemCopy.copy(MemoryUtil.memAddress(src), MemoryUtil.memAddress(dst), APIUtil.apiGetBytes(src.remaining(), 2));
    }

    public static void memCopy(LongBuffer src, LongBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.remaining());
        }
        MultiReleaseMemCopy.copy(MemoryUtil.memAddress(src), MemoryUtil.memAddress(dst), APIUtil.apiGetBytes(src.remaining(), 3));
    }

    public static void memCopy(FloatBuffer src, FloatBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.remaining());
        }
        MultiReleaseMemCopy.copy(MemoryUtil.memAddress(src), MemoryUtil.memAddress(dst), APIUtil.apiGetBytes(src.remaining(), 2));
    }

    public static void memCopy(DoubleBuffer src, DoubleBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.remaining());
        }
        MultiReleaseMemCopy.copy(MemoryUtil.memAddress(src), MemoryUtil.memAddress(dst), APIUtil.apiGetBytes(src.remaining(), 3));
    }

    public static <T extends CustomBuffer<T>> void memCopy(T src, T dst) {
        if (Checks.CHECKS) {
            Checks.check(dst, src.remaining());
        }
        MultiReleaseMemCopy.copy(MemoryUtil.memAddress(src), MemoryUtil.memAddress(dst), Integer.toUnsignedLong(src.remaining()) * (long)src.sizeof());
    }

    public static <T extends Struct<T>> void memCopy(T src, T dst) {
        MultiReleaseMemCopy.copy(src.address, dst.address, src.sizeof());
    }

    public static void memSet(long ptr, int value, long bytes) {
        if (Checks.DEBUG && (ptr == 0L || bytes < 0L)) {
            throw new IllegalArgumentException();
        }
        if (bytes < 256L) {
            int p = (int)ptr;
            if (Pointer.BITS64) {
                if ((p & 7) == 0) {
                    MemoryUtil.memSet64(ptr, value, (int)bytes & 0xFF);
                    return;
                }
            } else if ((p & 3) == 0) {
                MemoryUtil.memSet32(p, value, (int)bytes & 0xFF);
                return;
            }
        }
        LibCString.nmemset(ptr, value, bytes);
    }

    private static void memSet64(long ptr, int value, int bytes) {
        int aligned = bytes & 0xFFFFFFF8;
        long valuel = (long)(value & 0xFF) * FILL_PATTERN_64;
        for (int i = 0; i < aligned; i += 8) {
            UNSAFE.putLong(null, ptr + (long)i, valuel);
        }
        byte valueb = (byte)(value & 0xFF);
        for (int i = aligned; i < bytes; ++i) {
            UNSAFE.putByte(null, ptr + (long)i, valueb);
        }
    }

    private static void memSet32(int ptr, int value, int bytes) {
        int aligned = bytes & 0xFFFFFFFC;
        int vi = (value & 0xFF) * FILL_PATTERN_32;
        for (int i = 0; i < aligned; i += 4) {
            UNSAFE.putInt(null, (long)(ptr + i) & 0xFFFFFFFFL, vi);
        }
        byte vb = (byte)(value & 0xFF);
        for (int i = aligned; i < bytes; ++i) {
            UNSAFE.putByte(null, (long)(ptr + i) & 0xFFFFFFFFL, vb);
        }
    }

    public static void memCopy(long src, long dst, long bytes) {
        if (Checks.DEBUG && (src == 0L || dst == 0L || bytes < 0L)) {
            throw new IllegalArgumentException();
        }
        MultiReleaseMemCopy.copy(src, dst, bytes);
    }

    static void memCopyAligned64(long src, long dst, int bytes) {
        int i;
        int aligned = bytes & 0xFFFFFFF8;
        for (i = 0; i < aligned; i += 8) {
            UNSAFE.putLong(null, dst + (long)i, UNSAFE.getLong(null, src + (long)i));
        }
        for (i = aligned; i < bytes; ++i) {
            UNSAFE.putByte(null, dst + (long)i, UNSAFE.getByte(null, src + (long)i));
        }
    }

    static void memCopyAligned32(int src, int dst, int bytes) {
        int i;
        int aligned = bytes & 0xFFFFFFFC;
        for (i = 0; i < aligned; i += 4) {
            UNSAFE.putInt(null, (long)(dst + i) & 0xFFFFFFFFL, UNSAFE.getInt(null, (long)(src + i) & 0xFFFFFFFFL));
        }
        for (i = aligned; i < bytes; ++i) {
            UNSAFE.putByte(null, (long)(dst + i) & 0xFFFFFFFFL, UNSAFE.getByte(null, (long)(src + i) & 0xFFFFFFFFL));
        }
    }

    public static boolean memGetBoolean(long ptr) {
        return UNSAFE.getByte(null, ptr) != 0;
    }

    public static byte memGetByte(long ptr) {
        return UNSAFE.getByte(null, ptr);
    }

    public static short memGetShort(long ptr) {
        return UNSAFE.getShort(null, ptr);
    }

    public static int memGetInt(long ptr) {
        return UNSAFE.getInt(null, ptr);
    }

    public static long memGetLong(long ptr) {
        return UNSAFE.getLong(null, ptr);
    }

    public static float memGetFloat(long ptr) {
        return UNSAFE.getFloat(null, ptr);
    }

    public static double memGetDouble(long ptr) {
        return UNSAFE.getDouble(null, ptr);
    }

    public static long memGetCLong(long ptr) {
        return Pointer.CLONG_SIZE == 8 ? UNSAFE.getLong(null, ptr) : (long)UNSAFE.getInt(null, ptr);
    }

    public static long memGetAddress(long ptr) {
        return Pointer.BITS64 ? UNSAFE.getLong(null, ptr) : (long)UNSAFE.getInt(null, ptr) & 0xFFFFFFFFL;
    }

    public static void memPutByte(long ptr, byte value) {
        UNSAFE.putByte(null, ptr, value);
    }

    public static void memPutShort(long ptr, short value) {
        UNSAFE.putShort(null, ptr, value);
    }

    public static void memPutInt(long ptr, int value) {
        UNSAFE.putInt(null, ptr, value);
    }

    public static void memPutLong(long ptr, long value) {
        UNSAFE.putLong(null, ptr, value);
    }

    public static void memPutFloat(long ptr, float value) {
        UNSAFE.putFloat(null, ptr, value);
    }

    public static void memPutDouble(long ptr, double value) {
        UNSAFE.putDouble(null, ptr, value);
    }

    public static void memPutCLong(long ptr, long value) {
        if (Pointer.CLONG_SIZE == 8) {
            UNSAFE.putLong(null, ptr, value);
        } else {
            UNSAFE.putInt(null, ptr, (int)value);
        }
    }

    public static void memPutAddress(long ptr, long value) {
        if (Pointer.BITS64) {
            UNSAFE.putLong(null, ptr, value);
        } else {
            UNSAFE.putInt(null, ptr, (int)value);
        }
    }

    public static native <T> T memGlobalRefToObject(long var0);

    private static int write8(long target, int offset, int value) {
        UNSAFE.putByte(null, target + Integer.toUnsignedLong(offset), (byte)value);
        return offset + 1;
    }

    private static int write8Safe(long target, int offset, int maxLength, int value) {
        if (offset == maxLength) {
            throw new BufferOverflowException();
        }
        UNSAFE.putByte(null, target + Integer.toUnsignedLong(offset), (byte)value);
        return offset + 1;
    }

    private static int write16(long target, int offset, char value) {
        UNSAFE.putShort(null, target + Integer.toUnsignedLong(offset), (short)value);
        return offset + 2;
    }

    public static ByteBuffer memASCII(CharSequence text) {
        return MemoryUtil.memASCII(text, true);
    }

    public static @Nullable ByteBuffer memASCIISafe(@Nullable CharSequence text) {
        return text == null ? null : MemoryUtil.memASCII(text, true);
    }

    public static ByteBuffer memASCII(CharSequence text, boolean nullTerminated) {
        int length = MemoryUtil.memLengthASCII(text, nullTerminated);
        long target = MemoryUtil.nmemAlloc(length);
        if (Checks.CHECKS && target == 0L) {
            throw new OutOfMemoryError();
        }
        MemoryUtil.encodeASCIIUnsafe(text, nullTerminated, target);
        return MemoryUtil.wrapBufferByte(target, length);
    }

    public static @Nullable ByteBuffer memASCIISafe(@Nullable CharSequence text, boolean nullTerminated) {
        return text == null ? null : MemoryUtil.memASCII(text, nullTerminated);
    }

    public static int memASCII(CharSequence text, boolean nullTerminated, ByteBuffer target) {
        if (target.remaining() < MemoryUtil.memLengthASCII(text, nullTerminated)) {
            throw new BufferOverflowException();
        }
        long address = MemoryUtil.memAddress(target);
        return MemoryUtil.encodeASCIIUnsafe(text, nullTerminated, address);
    }

    public static int memASCII(CharSequence text, boolean nullTerminated, ByteBuffer target, int offset) {
        if (target.capacity() - offset < MemoryUtil.memLengthASCII(text, nullTerminated)) {
            throw new BufferOverflowException();
        }
        return MemoryUtil.encodeASCIIUnsafe(text, nullTerminated, MemoryUtil.memAddress(target, offset));
    }

    static int encodeASCIIUnsafe(CharSequence text, boolean nullTerminated, long target) {
        int i = 0;
        int len = text.length();
        while (i < len) {
            i = MemoryUtil.write8(target, i, text.charAt(i));
        }
        if (nullTerminated) {
            i = MemoryUtil.write8(target, i, 0);
        }
        return i;
    }

    public static int memLengthASCII(CharSequence value, boolean nullTerminated) {
        int len = value.length() + (nullTerminated ? 1 : 0);
        if (len < 0) {
            throw new BufferOverflowException();
        }
        return len;
    }

    public static ByteBuffer memUTF8(CharSequence text) {
        return MemoryUtil.memUTF8(text, true);
    }

    public static @Nullable ByteBuffer memUTF8Safe(@Nullable CharSequence text) {
        return text == null ? null : MemoryUtil.memUTF8(text, true);
    }

    public static ByteBuffer memUTF8(CharSequence text, boolean nullTerminated) {
        int length = MemoryUtil.memLengthUTF8(text, nullTerminated);
        long target = MemoryUtil.nmemAlloc(length);
        if (Checks.CHECKS && target == 0L) {
            throw new OutOfMemoryError();
        }
        MemoryUtil.encodeUTF8Unsafe(text, nullTerminated, target);
        return MemoryUtil.wrapBufferByte(target, length);
    }

    public static @Nullable ByteBuffer memUTF8Safe(@Nullable CharSequence text, boolean nullTerminated) {
        return text == null ? null : MemoryUtil.memUTF8(text, nullTerminated);
    }

    public static int memUTF8(CharSequence text, boolean nullTerminated, ByteBuffer target) {
        if (target.remaining() < MemoryUtil.memLengthASCII(text, nullTerminated)) {
            throw new BufferOverflowException();
        }
        return MemoryUtil.encodeUTF8Safe(text, nullTerminated, MemoryUtil.memAddress(target), target.remaining());
    }

    public static int memUTF8(CharSequence text, boolean nullTerminated, ByteBuffer target, int offset) {
        if (target.capacity() - offset < MemoryUtil.memLengthASCII(text, nullTerminated)) {
            throw new BufferOverflowException();
        }
        return MemoryUtil.encodeUTF8Safe(text, nullTerminated, MemoryUtil.memAddress(target, offset), target.capacity() - offset);
    }

    static int encodeUTF8Unsafe(CharSequence text, boolean nullTerminated, long target) {
        int p = 0;
        int i = 0;
        int len = text.length();
        while (i < len) {
            int c;
            if ((c = text.charAt(i++)) < 128) {
                p = MemoryUtil.write8(target, p, c);
                continue;
            }
            int cp = c;
            if (c < 2048) {
                p = MemoryUtil.write8(target, p, 0xC0 | cp >> 6);
            } else {
                if (!Character.isHighSurrogate((char)c)) {
                    p = MemoryUtil.write8(target, p, 0xE0 | cp >> 12);
                } else {
                    cp = Character.toCodePoint((char)c, text.charAt(i++));
                    p = MemoryUtil.write8(target, p, 0xF0 | cp >> 18);
                    p = MemoryUtil.write8(target, p, 0x80 | cp >> 12 & 0x3F);
                }
                p = MemoryUtil.write8(target, p, 0x80 | cp >> 6 & 0x3F);
            }
            p = MemoryUtil.write8(target, p, 0x80 | cp & 0x3F);
        }
        if (nullTerminated) {
            p = MemoryUtil.write8(target, p, 0);
        }
        return p;
    }

    static int encodeUTF8Safe(CharSequence text, boolean nullTerminated, long target, int maxLength) {
        int c;
        int i;
        int p = 0;
        int length = text.length();
        for (i = 0; i < length && 128 > (c = text.charAt(i)); ++i) {
            p = MemoryUtil.write8(target, p, c);
        }
        while (i < length) {
            if ((c = text.charAt(i++)) < 128) {
                p = MemoryUtil.write8Safe(target, p, maxLength, c);
                continue;
            }
            int cp = c;
            if (c < 2048) {
                p = MemoryUtil.write8Safe(target, p, maxLength, 0xC0 | cp >> 6);
            } else {
                if (!Character.isHighSurrogate((char)c)) {
                    p = MemoryUtil.write8Safe(target, p, maxLength, 0xE0 | cp >> 12);
                } else {
                    cp = Character.toCodePoint((char)c, text.charAt(i++));
                    p = MemoryUtil.write8Safe(target, p, maxLength, 0xF0 | cp >> 18);
                    p = MemoryUtil.write8Safe(target, p, maxLength, 0x80 | cp >> 12 & 0x3F);
                }
                p = MemoryUtil.write8Safe(target, p, maxLength, 0x80 | cp >> 6 & 0x3F);
            }
            p = MemoryUtil.write8Safe(target, p, maxLength, 0x80 | cp & 0x3F);
        }
        if (nullTerminated) {
            p = MemoryUtil.write8Safe(target, p, maxLength, 0);
        }
        return p;
    }

    public static int memLengthUTF8(CharSequence value, boolean nullTerminated) {
        int len = value.length();
        int bytes = len + (nullTerminated ? 1 : 0);
        for (int i = 0; i < len; ++i) {
            char c = value.charAt(i);
            if (c < '\u0080') continue;
            if (c < '\u0800') {
                bytes += 127 - c >>> 31;
            } else {
                bytes += 2;
                if (Character.isHighSurrogate(c)) {
                    ++i;
                }
            }
            if (bytes >= 0) continue;
            throw new BufferOverflowException();
        }
        if (bytes < 0) {
            throw new BufferOverflowException();
        }
        return bytes;
    }

    public static ByteBuffer memUTF16(CharSequence text) {
        return MemoryUtil.memUTF16(text, true);
    }

    public static @Nullable ByteBuffer memUTF16Safe(@Nullable CharSequence text) {
        return text == null ? null : MemoryUtil.memUTF16(text, true);
    }

    public static ByteBuffer memUTF16(CharSequence text, boolean nullTerminated) {
        int length = MemoryUtil.memLengthUTF16(text, nullTerminated);
        long target = MemoryUtil.nmemAlloc(length);
        if (Checks.CHECKS && target == 0L) {
            throw new OutOfMemoryError();
        }
        MemoryUtil.encodeUTF16Unsafe(text, nullTerminated, target);
        return MemoryUtil.wrapBufferByte(target, length);
    }

    public static @Nullable ByteBuffer memUTF16Safe(@Nullable CharSequence text, boolean nullTerminated) {
        return text == null ? null : MemoryUtil.memUTF16(text, nullTerminated);
    }

    public static int memUTF16(CharSequence text, boolean nullTerminated, ByteBuffer target) {
        if (target.remaining() < MemoryUtil.memLengthUTF16(text, nullTerminated)) {
            throw new BufferOverflowException();
        }
        long address = MemoryUtil.memAddress(target);
        return MemoryUtil.encodeUTF16Unsafe(text, nullTerminated, address);
    }

    public static int memUTF16(CharSequence text, boolean nullTerminated, ByteBuffer target, int offset) {
        if (target.capacity() - offset < MemoryUtil.memLengthUTF16(text, nullTerminated)) {
            throw new BufferOverflowException();
        }
        long address = MemoryUtil.memAddress(target, offset);
        return MemoryUtil.encodeUTF16Unsafe(text, nullTerminated, address);
    }

    static int encodeUTF16Unsafe(CharSequence text, boolean nullTerminated, long target) {
        int p = 0;
        int i = 0;
        int len = text.length();
        while (i < len) {
            p = MemoryUtil.write16(target, p, text.charAt(i++));
        }
        if (nullTerminated) {
            p = MemoryUtil.write16(target, p, '\u0000');
        }
        return p;
    }

    public static int memLengthUTF16(CharSequence value, boolean nullTerminated) {
        int len = value.length() + (nullTerminated ? 1 : 0);
        if (len < 0 || 0x3FFFFFFF < len) {
            throw new BufferOverflowException();
        }
        return len << 1;
    }

    private static int memLengthNT1(long address, int maxLength) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return Pointer.BITS64 ? MemoryUtil.strlen64NT1(address, maxLength) : MemoryUtil.strlen32NT1(address, maxLength);
    }

    private static int strlen64NT1(long address, int maxLength) {
        int i;
        if (8 <= maxLength) {
            int misalignment = (int)address & 7;
            if (misalignment != 0) {
                int len = 8 - misalignment;
                for (i = 0; i < len; ++i) {
                    if (UNSAFE.getByte(null, address + (long)i) != 0) continue;
                    return i;
                }
            }
            while (i <= maxLength - 8 && !MathUtil.mathHasZeroByte(UNSAFE.getLong(null, address + (long)i))) {
                i += 8;
            }
        }
        while (i < maxLength && UNSAFE.getByte(null, address + (long)i) != 0) {
            ++i;
        }
        return i;
    }

    private static int strlen32NT1(long address, int maxLength) {
        int i;
        if (4 <= maxLength) {
            int misalignment = (int)address & 3;
            if (misalignment != 0) {
                int len = 4 - misalignment;
                for (i = 0; i < len; ++i) {
                    if (UNSAFE.getByte(null, address + (long)i) != 0) continue;
                    return i;
                }
            }
            while (i <= maxLength - 4 && !MathUtil.mathHasZeroByte(UNSAFE.getInt(null, address + (long)i))) {
                i += 4;
            }
        }
        while (i < maxLength && UNSAFE.getByte(null, address + (long)i) != 0) {
            ++i;
        }
        return i;
    }

    public static int memLengthNT1(ByteBuffer buffer) {
        return MemoryUtil.memLengthNT1(MemoryUtil.memAddress(buffer), buffer.remaining());
    }

    private static int memLengthNT2(long address, int maxLength) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return Pointer.BITS64 ? MemoryUtil.strlen64NT2(address, maxLength) : MemoryUtil.strlen32NT2((int)address, maxLength);
    }

    private static int strlen64NT2(long address, int maxLength) {
        int i;
        if (8 <= maxLength) {
            int misalignment = (int)address & 7;
            if (misalignment != 0) {
                int len = 8 - misalignment;
                for (i = 0; i < len; i += 2) {
                    if (UNSAFE.getShort(null, address + (long)i) != 0) continue;
                    return i;
                }
            }
            while (i <= maxLength - 8 && !MathUtil.mathHasZeroShort(UNSAFE.getLong(null, address + (long)i))) {
                i += 8;
            }
        }
        while (i < maxLength && UNSAFE.getShort(null, address + (long)i) != 0) {
            i += 2;
        }
        return i;
    }

    private static int strlen32NT2(long address, int maxLength) {
        int i;
        if (4 <= maxLength) {
            int misalignment = (int)address & 3;
            if (misalignment != 0) {
                int len = 4 - misalignment;
                for (i = 0; i < len; i += 2) {
                    if (UNSAFE.getShort(null, address + (long)i) != 0) continue;
                    return i;
                }
            }
            while (i <= maxLength - 4 && !MathUtil.mathHasZeroShort(UNSAFE.getInt(null, address + (long)i))) {
                i += 4;
            }
        }
        while (i < maxLength && UNSAFE.getShort(null, address + (long)i) != 0) {
            i += 2;
        }
        return i;
    }

    public static int memLengthNT2(ByteBuffer buffer) {
        return MemoryUtil.memLengthNT2(MemoryUtil.memAddress(buffer), buffer.remaining());
    }

    public static ByteBuffer memByteBufferNT1(long address) {
        return MemoryUtil.memByteBuffer(address, MemoryUtil.memLengthNT1(address, Integer.MAX_VALUE));
    }

    public static ByteBuffer memByteBufferNT1(long address, int maxLength) {
        return MemoryUtil.memByteBuffer(address, MemoryUtil.memLengthNT1(address, maxLength));
    }

    public static @Nullable ByteBuffer memByteBufferNT1Safe(long address) {
        return address == 0L ? null : MemoryUtil.memByteBuffer(address, MemoryUtil.memLengthNT1(address, Integer.MAX_VALUE));
    }

    public static @Nullable ByteBuffer memByteBufferNT1Safe(long address, int maxLength) {
        return address == 0L ? null : MemoryUtil.memByteBuffer(address, MemoryUtil.memLengthNT1(address, maxLength));
    }

    public static ByteBuffer memByteBufferNT2(long address) {
        return MemoryUtil.memByteBufferNT2(address, 0x7FFFFFFE);
    }

    public static ByteBuffer memByteBufferNT2(long address, int maxLength) {
        if (Checks.DEBUG && (maxLength & 1) != 0) {
            throw new IllegalArgumentException("The maximum length must be an even number.");
        }
        return MemoryUtil.memByteBuffer(address, MemoryUtil.memLengthNT2(address, maxLength));
    }

    public static @Nullable ByteBuffer memByteBufferNT2Safe(long address) {
        return address == 0L ? null : MemoryUtil.memByteBufferNT2(address, 0x7FFFFFFE);
    }

    public static @Nullable ByteBuffer memByteBufferNT2Safe(long address, int maxLength) {
        return address == 0L ? null : MemoryUtil.memByteBufferNT2(address, maxLength);
    }

    public static String memASCII(long address) {
        return MemoryUtil.memASCII(address, MemoryUtil.memLengthNT1(address, Integer.MAX_VALUE));
    }

    public static String memASCII(long address, int length) {
        if (length <= 0) {
            return "";
        }
        byte[] ascii = length <= ARRAY_TLC_SIZE ? ARRAY_TLC_BYTE.get() : new byte[length];
        MemoryUtil.memByteBuffer(address, length).get(ascii, 0, length);
        return new String(ascii, 0, 0, length);
    }

    public static String memASCII(ByteBuffer buffer) {
        return MemoryUtil.memASCII(MemoryUtil.memAddress(buffer), buffer.remaining());
    }

    public static @Nullable String memASCIISafe(long address) {
        return address == 0L ? null : MemoryUtil.memASCII(address, MemoryUtil.memLengthNT1(address, Integer.MAX_VALUE));
    }

    public static @Nullable String memASCIISafe(long address, int length) {
        return address == 0L ? null : MemoryUtil.memASCII(address, length);
    }

    public static @Nullable String memASCIISafe(@Nullable ByteBuffer buffer) {
        return buffer == null ? null : MemoryUtil.memASCII(MemoryUtil.memAddress(buffer), buffer.remaining());
    }

    public static String memASCII(ByteBuffer buffer, int length) {
        return MemoryUtil.memASCII(MemoryUtil.memAddress(buffer), length);
    }

    public static String memASCII(ByteBuffer buffer, int length, int offset) {
        return MemoryUtil.memASCII(MemoryUtil.memAddress(buffer, offset), length);
    }

    public static String memUTF8(long address) {
        return MultiReleaseTextDecoding.decodeUTF8(address, MemoryUtil.memLengthNT1(address, Integer.MAX_VALUE));
    }

    public static String memUTF8(long address, int length) {
        return MultiReleaseTextDecoding.decodeUTF8(address, length);
    }

    public static String memUTF8(ByteBuffer buffer) {
        return MultiReleaseTextDecoding.decodeUTF8(MemoryUtil.memAddress(buffer), buffer.remaining());
    }

    public static @Nullable String memUTF8Safe(long address) {
        return address == 0L ? null : MultiReleaseTextDecoding.decodeUTF8(address, MemoryUtil.memLengthNT1(address, Integer.MAX_VALUE));
    }

    public static @Nullable String memUTF8Safe(long address, int length) {
        return address == 0L ? null : MultiReleaseTextDecoding.decodeUTF8(address, length);
    }

    public static @Nullable String memUTF8Safe(@Nullable ByteBuffer buffer) {
        return buffer == null ? null : MultiReleaseTextDecoding.decodeUTF8(MemoryUtil.memAddress(buffer), buffer.remaining());
    }

    public static String memUTF8(ByteBuffer buffer, int length) {
        return MultiReleaseTextDecoding.decodeUTF8(MemoryUtil.memAddress(buffer), length);
    }

    public static String memUTF8(ByteBuffer buffer, int length, int offset) {
        return MultiReleaseTextDecoding.decodeUTF8(MemoryUtil.memAddress(buffer, offset), length);
    }

    public static String memUTF16(long address) {
        return MemoryUtil.memUTF16(address, MemoryUtil.memLengthNT2(address, 0x7FFFFFFE) >> 1);
    }

    public static String memUTF16(long address, int length) {
        if (length <= 0) {
            return "";
        }
        if (Checks.DEBUG) {
            int len = length << 1;
            byte[] bytes = len <= ARRAY_TLC_SIZE ? ARRAY_TLC_BYTE.get() : new byte[len];
            MemoryUtil.memByteBuffer(address, len).get(bytes, 0, len);
            return new String(bytes, 0, len, UTF16);
        }
        char[] chars = length <= ARRAY_TLC_SIZE ? ARRAY_TLC_CHAR.get() : new char[length];
        MemoryUtil.memCharBuffer(address, length).get(chars, 0, length);
        return new String(chars, 0, length);
    }

    public static String memUTF16(ByteBuffer buffer) {
        return MemoryUtil.memUTF16(MemoryUtil.memAddress(buffer), buffer.remaining() >> 1);
    }

    public static @Nullable String memUTF16Safe(long address) {
        return address == 0L ? null : MemoryUtil.memUTF16(address, MemoryUtil.memLengthNT2(address, 0x7FFFFFFE) >> 1);
    }

    public static @Nullable String memUTF16Safe(long address, int length) {
        return address == 0L ? null : MemoryUtil.memUTF16(address, length);
    }

    public static @Nullable String memUTF16Safe(@Nullable ByteBuffer buffer) {
        return buffer == null ? null : MemoryUtil.memUTF16(MemoryUtil.memAddress(buffer), buffer.remaining() >> 1);
    }

    public static String memUTF16(ByteBuffer buffer, int length) {
        return MemoryUtil.memUTF16(MemoryUtil.memAddress(buffer), length);
    }

    public static String memUTF16(ByteBuffer buffer, int length, int offset) {
        return MemoryUtil.memUTF16(MemoryUtil.memAddress(buffer, offset), length);
    }

    private static Unsafe getUnsafeInstance() {
        Field[] fields;
        for (Field field : fields = Unsafe.class.getDeclaredFields()) {
            int modifiers;
            if (!field.getType().equals(Unsafe.class) || !Modifier.isStatic(modifiers = field.getModifiers()) || !Modifier.isFinal(modifiers)) continue;
            try {
                field.setAccessible(true);
                return (Unsafe)field.get(null);
            }
            catch (Exception exception) {
                break;
            }
        }
        throw new UnsupportedOperationException("LWJGL requires sun.misc.Unsafe to be available.");
    }

    private static long getFieldOffset(Class<?> containerType, Class<?> fieldType, LongPredicate predicate) {
        for (Class<?> c = containerType; c != Object.class; c = c.getSuperclass()) {
            Field[] fields;
            for (Field field : fields = c.getDeclaredFields()) {
                long offset;
                if (!field.getType().isAssignableFrom(fieldType) || Modifier.isStatic(field.getModifiers()) || field.isSynthetic() || !predicate.test(offset = UNSAFE.objectFieldOffset(field))) continue;
                return offset;
            }
        }
        throw new UnsupportedOperationException("Failed to find field offset in class.");
    }

    private static long getFieldOffsetInt(Object container, int value) {
        return MemoryUtil.getFieldOffset(container.getClass(), Integer.TYPE, offset -> UNSAFE.getInt(container, offset) == value);
    }

    private static long getFieldOffsetObject(Object container, Object value) {
        return MemoryUtil.getFieldOffset(container.getClass(), value.getClass(), offset -> UNSAFE.getObject(container, offset) == value);
    }

    private static long getAddressOffset() {
        long MAGIC_ADDRESS = 0xDEADBEEF8BADF00DL & (Pointer.BITS32 ? 0xFFFFFFFFL : -1L);
        ByteBuffer bb = Objects.requireNonNull(JNINativeInterface.NewDirectByteBuffer(MAGIC_ADDRESS, 0L));
        return MemoryUtil.getFieldOffset(bb.getClass(), Long.TYPE, offset -> UNSAFE.getLong(bb, offset) == MAGIC_ADDRESS);
    }

    private static long getMarkOffset() {
        ByteBuffer bb = Objects.requireNonNull(JNINativeInterface.NewDirectByteBuffer(1L, 0L));
        return MemoryUtil.getFieldOffsetInt(bb, -1);
    }

    private static long getPositionOffset() {
        ByteBuffer bb = Objects.requireNonNull(JNINativeInterface.NewDirectByteBuffer(-1L, 219540062L));
        bb.position(16435934);
        return MemoryUtil.getFieldOffsetInt(bb, 16435934);
    }

    private static long getLimitOffset() {
        ByteBuffer bb = Objects.requireNonNull(JNINativeInterface.NewDirectByteBuffer(-1L, 219540062L));
        bb.limit(16435934);
        return MemoryUtil.getFieldOffsetInt(bb, 16435934);
    }

    private static long getCapacityOffset() {
        ByteBuffer bb = Objects.requireNonNull(JNINativeInterface.NewDirectByteBuffer(-1L, 219540062L));
        bb.limit(0);
        return MemoryUtil.getFieldOffsetInt(bb, 219540062);
    }

    static ByteBuffer wrapBufferByte(long address, int capacity) {
        ByteBuffer buffer;
        try {
            buffer = (ByteBuffer)UNSAFE.allocateInstance(BUFFER_BYTE);
        }
        catch (InstantiationException e) {
            throw new UnsupportedOperationException(e);
        }
        UNSAFE.putLong(buffer, ADDRESS, address);
        UNSAFE.putInt(buffer, MARK, -1);
        UNSAFE.putInt(buffer, LIMIT, capacity);
        UNSAFE.putInt(buffer, CAPACITY, capacity);
        return buffer.order(NATIVE_ORDER);
    }

    static ShortBuffer wrapBufferShort(long address, int capacity) {
        ShortBuffer buffer;
        try {
            buffer = (ShortBuffer)UNSAFE.allocateInstance(BUFFER_SHORT);
        }
        catch (InstantiationException e) {
            throw new UnsupportedOperationException(e);
        }
        UNSAFE.putLong(buffer, ADDRESS, address);
        UNSAFE.putInt(buffer, MARK, -1);
        UNSAFE.putInt(buffer, LIMIT, capacity);
        UNSAFE.putInt(buffer, CAPACITY, capacity);
        return buffer;
    }

    static CharBuffer wrapBufferChar(long address, int capacity) {
        CharBuffer buffer;
        try {
            buffer = (CharBuffer)UNSAFE.allocateInstance(BUFFER_CHAR);
        }
        catch (InstantiationException e) {
            throw new UnsupportedOperationException(e);
        }
        UNSAFE.putLong(buffer, ADDRESS, address);
        UNSAFE.putInt(buffer, MARK, -1);
        UNSAFE.putInt(buffer, LIMIT, capacity);
        UNSAFE.putInt(buffer, CAPACITY, capacity);
        return buffer;
    }

    static IntBuffer wrapBufferInt(long address, int capacity) {
        IntBuffer buffer;
        try {
            buffer = (IntBuffer)UNSAFE.allocateInstance(BUFFER_INT);
        }
        catch (InstantiationException e) {
            throw new UnsupportedOperationException(e);
        }
        UNSAFE.putLong(buffer, ADDRESS, address);
        UNSAFE.putInt(buffer, MARK, -1);
        UNSAFE.putInt(buffer, LIMIT, capacity);
        UNSAFE.putInt(buffer, CAPACITY, capacity);
        return buffer;
    }

    static LongBuffer wrapBufferLong(long address, int capacity) {
        LongBuffer buffer;
        try {
            buffer = (LongBuffer)UNSAFE.allocateInstance(BUFFER_LONG);
        }
        catch (InstantiationException e) {
            throw new UnsupportedOperationException(e);
        }
        UNSAFE.putLong(buffer, ADDRESS, address);
        UNSAFE.putInt(buffer, MARK, -1);
        UNSAFE.putInt(buffer, LIMIT, capacity);
        UNSAFE.putInt(buffer, CAPACITY, capacity);
        return buffer;
    }

    static FloatBuffer wrapBufferFloat(long address, int capacity) {
        FloatBuffer buffer;
        try {
            buffer = (FloatBuffer)UNSAFE.allocateInstance(BUFFER_FLOAT);
        }
        catch (InstantiationException e) {
            throw new UnsupportedOperationException(e);
        }
        UNSAFE.putLong(buffer, ADDRESS, address);
        UNSAFE.putInt(buffer, MARK, -1);
        UNSAFE.putInt(buffer, LIMIT, capacity);
        UNSAFE.putInt(buffer, CAPACITY, capacity);
        return buffer;
    }

    static DoubleBuffer wrapBufferDouble(long address, int capacity) {
        DoubleBuffer buffer;
        try {
            buffer = (DoubleBuffer)UNSAFE.allocateInstance(BUFFER_DOUBLE);
        }
        catch (InstantiationException e) {
            throw new UnsupportedOperationException(e);
        }
        UNSAFE.putLong(buffer, ADDRESS, address);
        UNSAFE.putInt(buffer, MARK, -1);
        UNSAFE.putInt(buffer, LIMIT, capacity);
        UNSAFE.putInt(buffer, CAPACITY, capacity);
        return buffer;
    }

    static ByteBuffer slice(ByteBuffer source, long address, int capacity) {
        ByteBuffer target;
        try {
            target = (ByteBuffer)UNSAFE.allocateInstance(BUFFER_BYTE);
        }
        catch (InstantiationException e) {
            throw new UnsupportedOperationException(e);
        }
        UNSAFE.putLong(target, ADDRESS, address);
        UNSAFE.putInt(target, MARK, -1);
        UNSAFE.putInt(target, LIMIT, capacity);
        UNSAFE.putInt(target, CAPACITY, capacity);
        Object attachment = UNSAFE.getObject(source, PARENT_BYTE);
        UNSAFE.putObject(target, PARENT_BYTE, attachment == null ? source : attachment);
        return target.order(source.order());
    }

    static <T extends Buffer> T slice(Class<? extends T> clazz, T source, long address, int capacity, long attachmentOffset) {
        Buffer target;
        try {
            target = (Buffer)UNSAFE.allocateInstance(clazz);
        }
        catch (InstantiationException e) {
            throw new UnsupportedOperationException(e);
        }
        UNSAFE.putLong(target, ADDRESS, address);
        UNSAFE.putInt(target, MARK, -1);
        UNSAFE.putInt(target, LIMIT, capacity);
        UNSAFE.putInt(target, CAPACITY, capacity);
        UNSAFE.putObject(target, attachmentOffset, UNSAFE.getObject(source, attachmentOffset));
        return (T)target;
    }

    static <T extends Buffer> T duplicate(Class<? extends T> clazz, T source, long attachmentOffset) {
        Buffer target;
        try {
            target = (Buffer)UNSAFE.allocateInstance(clazz);
        }
        catch (InstantiationException e) {
            throw new UnsupportedOperationException(e);
        }
        UNSAFE.putLong(target, ADDRESS, UNSAFE.getLong(source, ADDRESS));
        UNSAFE.putInt(target, MARK, UNSAFE.getInt(source, MARK));
        UNSAFE.putInt(target, POSITION, UNSAFE.getInt(source, POSITION));
        UNSAFE.putInt(target, LIMIT, UNSAFE.getInt(source, LIMIT));
        UNSAFE.putInt(target, CAPACITY, UNSAFE.getInt(source, CAPACITY));
        UNSAFE.putObject(target, attachmentOffset, UNSAFE.getObject(source, attachmentOffset));
        return (T)target;
    }

    static {
        ARRAY_TLC_SIZE = Configuration.ARRAY_TLC_SIZE.get(8192);
        ARRAY_TLC_BYTE = ThreadLocal.withInitial(() -> new byte[ARRAY_TLC_SIZE]);
        ARRAY_TLC_CHAR = ThreadLocal.withInitial(() -> new char[ARRAY_TLC_SIZE]);
        NATIVE_ORDER = ByteOrder.nativeOrder();
        UTF16 = NATIVE_ORDER == ByteOrder.LITTLE_ENDIAN ? StandardCharsets.UTF_16LE : StandardCharsets.UTF_16BE;
        Library.initialize();
        ByteBuffer bb = ByteBuffer.allocateDirect(0).order(NATIVE_ORDER);
        BUFFER_BYTE = bb.getClass();
        BUFFER_SHORT = bb.asShortBuffer().getClass();
        BUFFER_CHAR = bb.asCharBuffer().getClass();
        BUFFER_INT = bb.asIntBuffer().getClass();
        BUFFER_LONG = bb.asLongBuffer().getClass();
        BUFFER_FLOAT = bb.asFloatBuffer().getClass();
        BUFFER_DOUBLE = bb.asDoubleBuffer().getClass();
        UNSAFE = MemoryUtil.getUnsafeInstance();
        try {
            MARK = MemoryUtil.getMarkOffset();
            POSITION = MemoryUtil.getPositionOffset();
            LIMIT = MemoryUtil.getLimitOffset();
            CAPACITY = MemoryUtil.getCapacityOffset();
            ADDRESS = MemoryUtil.getAddressOffset();
            PARENT_BYTE = MemoryUtil.getFieldOffsetObject(bb.duplicate().order(bb.order()), bb);
            PARENT_SHORT = MemoryUtil.getFieldOffsetObject(bb.asShortBuffer(), bb);
            PARENT_CHAR = MemoryUtil.getFieldOffsetObject(bb.asCharBuffer(), bb);
            PARENT_INT = MemoryUtil.getFieldOffsetObject(bb.asIntBuffer(), bb);
            PARENT_LONG = MemoryUtil.getFieldOffsetObject(bb.asLongBuffer(), bb);
            PARENT_FLOAT = MemoryUtil.getFieldOffsetObject(bb.asFloatBuffer(), bb);
            PARENT_DOUBLE = MemoryUtil.getFieldOffsetObject(bb.asDoubleBuffer(), bb);
        }
        catch (Throwable t) {
            throw new UnsupportedOperationException(t);
        }
        PAGE_SIZE = UNSAFE.pageSize();
        CACHE_LINE_SIZE = 64;
        FILL_PATTERN_32 = Integer.divideUnsigned(-1, 255);
        FILL_PATTERN_64 = Long.divideUnsigned(-1L, 255L);
    }

    public static interface MemoryAllocator {
        public long getMalloc();

        public long getCalloc();

        public long getRealloc();

        public long getFree();

        public long getAlignedAlloc();

        public long getAlignedFree();

        public long malloc(long var1);

        public long calloc(long var1, long var3);

        public long realloc(long var1, long var3);

        public void free(long var1);

        public long aligned_alloc(long var1, long var3);

        public void aligned_free(long var1);
    }

    static final class LazyInit {
        static final MemoryAllocator ALLOCATOR_IMPL;
        static final MemoryAllocator ALLOCATOR;

        private LazyInit() {
        }

        static {
            boolean debug = Configuration.DEBUG_MEMORY_ALLOCATOR.get(false);
            ALLOCATOR_IMPL = MemoryManage.getInstance();
            ALLOCATOR = debug ? new MemoryManage.DebugAllocator(ALLOCATOR_IMPL) : ALLOCATOR_IMPL;
            APIUtil.apiLog("MemoryUtil allocator: " + ALLOCATOR.getClass().getSimpleName());
            if (debug && !Configuration.DEBUG_MEMORY_ALLOCATOR_FAST.get(false).booleanValue()) {
                APIUtil.apiLogMore("Reminder: enable Configuration.DEBUG_MEMORY_ALLOCATOR_FAST for low overhead allocation tracking.");
            }
        }
    }
}

