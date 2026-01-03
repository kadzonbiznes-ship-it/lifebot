/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.NativeType
 *  org.lwjgl.system.libffi.FFICIF$Buffer
 */
package org.lwjgl.system.libffi;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.libffi.FFICIF;
import org.lwjgl.system.libffi.FFIType;

@NativeType(value="struct ffi_cif")
public class FFICIF
extends Struct<FFICIF>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int ABI;
    public static final int NARGS;
    public static final int ARG_TYPES;
    public static final int RTYPE;
    public static final int BYTES;
    public static final int FLAGS;

    private static native int offsets(long var0);

    protected FFICIF(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected FFICIF create(long address, @Nullable ByteBuffer container) {
        return new FFICIF(address, container);
    }

    public FFICIF(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), FFICIF.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="ffi_abi")
    public int abi() {
        return FFICIF.nabi(this.address());
    }

    @NativeType(value="unsigned")
    public int nargs() {
        return FFICIF.nnargs(this.address());
    }

    @NativeType(value="ffi_type **")
    public PointerBuffer arg_types(int capacity) {
        return FFICIF.narg_types(this.address(), capacity);
    }

    @NativeType(value="ffi_type *")
    public FFIType rtype() {
        return FFICIF.nrtype(this.address());
    }

    @NativeType(value="unsigned")
    public int bytes() {
        return FFICIF.nbytes(this.address());
    }

    @NativeType(value="unsigned")
    public int flags() {
        return FFICIF.nflags(this.address());
    }

    public static FFICIF malloc() {
        return new FFICIF(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static FFICIF calloc() {
        return new FFICIF(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static FFICIF create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new FFICIF(MemoryUtil.memAddress(container), container);
    }

    public static FFICIF create(long address) {
        return new FFICIF(address, null);
    }

    public static @Nullable FFICIF createSafe(long address) {
        return address == 0L ? null : new FFICIF(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(FFICIF.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = FFICIF.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static // Could not load outer class - annotation placement on inner may be incorrect
    @Nullable FFICIF.Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static FFICIF malloc(MemoryStack stack) {
        return new FFICIF(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static FFICIF calloc(MemoryStack stack) {
        return new FFICIF(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int nabi(long struct) {
        return MemoryUtil.memGetInt(struct + (long)ABI);
    }

    public static int nnargs(long struct) {
        return MemoryUtil.memGetInt(struct + (long)NARGS);
    }

    public static PointerBuffer narg_types(long struct, int capacity) {
        return MemoryUtil.memPointerBuffer(MemoryUtil.memGetAddress(struct + (long)ARG_TYPES), capacity);
    }

    public static FFIType nrtype(long struct) {
        return FFIType.create(MemoryUtil.memGetAddress(struct + (long)RTYPE));
    }

    public static int nbytes(long struct) {
        return MemoryUtil.memGetInt(struct + (long)BYTES);
    }

    public static int nflags(long struct) {
        return MemoryUtil.memGetInt(struct + (long)FLAGS);
    }

    static {
        try (MemoryStack stack = MemoryStack.stackPush();){
            IntBuffer offsets = stack.mallocInt(7);
            SIZEOF = FFICIF.offsets(MemoryUtil.memAddress(offsets));
            ABI = offsets.get(0);
            NARGS = offsets.get(1);
            ARG_TYPES = offsets.get(2);
            RTYPE = offsets.get(3);
            BYTES = offsets.get(4);
            FLAGS = offsets.get(5);
            ALIGNOF = offsets.get(6);
        }
    }
}

