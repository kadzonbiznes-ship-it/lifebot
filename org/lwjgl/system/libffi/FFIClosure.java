/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.NativeType
 *  org.lwjgl.system.libffi.FFIClosure$Buffer
 */
package org.lwjgl.system.libffi;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.libffi.FFICIF;
import org.lwjgl.system.libffi.FFIClosure;

@NativeType(value="struct ffi_closure")
public class FFIClosure
extends Struct<FFIClosure>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int CIF;
    public static final int FUN;
    public static final int USER_DATA;

    private static native int offsets(long var0);

    protected FFIClosure(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected FFIClosure create(long address, @Nullable ByteBuffer container) {
        return new FFIClosure(address, container);
    }

    public FFIClosure(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), FFIClosure.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="ffi_cif *")
    public FFICIF cif() {
        return FFIClosure.ncif(this.address());
    }

    @NativeType(value="void (*)(ffi_cif*,void*,void**,void*)")
    public long fun() {
        return FFIClosure.nfun(this.address());
    }

    @NativeType(value="void *")
    public long user_data() {
        return FFIClosure.nuser_data(this.address());
    }

    public static FFIClosure malloc() {
        return new FFIClosure(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static FFIClosure calloc() {
        return new FFIClosure(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static FFIClosure create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new FFIClosure(MemoryUtil.memAddress(container), container);
    }

    public static FFIClosure create(long address) {
        return new FFIClosure(address, null);
    }

    public static @Nullable FFIClosure createSafe(long address) {
        return address == 0L ? null : new FFIClosure(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(FFIClosure.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = FFIClosure.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static // Could not load outer class - annotation placement on inner may be incorrect
    @Nullable FFIClosure.Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static FFIClosure malloc(MemoryStack stack) {
        return new FFIClosure(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static FFIClosure calloc(MemoryStack stack) {
        return new FFIClosure(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static FFICIF ncif(long struct) {
        return FFICIF.create(MemoryUtil.memGetAddress(struct + (long)CIF));
    }

    public static long nfun(long struct) {
        return MemoryUtil.memGetAddress(struct + (long)FUN);
    }

    public static long nuser_data(long struct) {
        return MemoryUtil.memGetAddress(struct + (long)USER_DATA);
    }

    static {
        try (MemoryStack stack = MemoryStack.stackPush();){
            IntBuffer offsets = stack.mallocInt(4);
            SIZEOF = FFIClosure.offsets(MemoryUtil.memAddress(offsets));
            CIF = offsets.get(0);
            FUN = offsets.get(1);
            USER_DATA = offsets.get(2);
            ALIGNOF = offsets.get(3);
        }
    }
}

