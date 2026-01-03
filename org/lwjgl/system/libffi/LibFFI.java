/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.NativeType
 */
package org.lwjgl.system.libffi;

import java.nio.ByteBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.Checks;
import org.lwjgl.system.Library;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.libffi.FFICIF;
import org.lwjgl.system.libffi.FFIClosure;
import org.lwjgl.system.libffi.FFIType;

public class LibFFI {
    public static final short FFI_TYPE_VOID = 0;
    public static final short FFI_TYPE_INT = 1;
    public static final short FFI_TYPE_FLOAT = 2;
    public static final short FFI_TYPE_DOUBLE = 3;
    public static final short FFI_TYPE_LONGDOUBLE;
    public static final short FFI_TYPE_UINT8 = 5;
    public static final short FFI_TYPE_SINT8 = 6;
    public static final short FFI_TYPE_UINT16 = 7;
    public static final short FFI_TYPE_SINT16 = 8;
    public static final short FFI_TYPE_UINT32 = 9;
    public static final short FFI_TYPE_SINT32 = 10;
    public static final short FFI_TYPE_UINT64 = 11;
    public static final short FFI_TYPE_SINT64 = 12;
    public static final short FFI_TYPE_STRUCT = 13;
    public static final short FFI_TYPE_POINTER = 14;
    public static final int FFI_FIRST_ABI;
    public static final int FFI_WIN64;
    public static final int FFI_GNUW64;
    public static final int FFI_UNIX64;
    public static final int FFI_EFI64;
    public static final int FFI_SYSV;
    public static final int FFI_STDCALL;
    public static final int FFI_THISCALL;
    public static final int FFI_FASTCALL;
    public static final int FFI_MS_CDECL;
    public static final int FFI_PASCAL;
    public static final int FFI_REGISTER;
    public static final int FFI_VFP;
    public static final int FFI_LAST_ABI;
    public static final int FFI_DEFAULT_ABI;
    public static final int FFI_OK = 0;
    public static final int FFI_BAD_TYPEDEF = 1;
    public static final int FFI_BAD_ABI = 2;
    public static final int FFI_BAD_ARGTYPE = 3;
    public static final FFIType ffi_type_void;
    public static final FFIType ffi_type_uint8;
    public static final FFIType ffi_type_sint8;
    public static final FFIType ffi_type_uint16;
    public static final FFIType ffi_type_sint16;
    public static final FFIType ffi_type_uint32;
    public static final FFIType ffi_type_sint32;
    public static final FFIType ffi_type_uint64;
    public static final FFIType ffi_type_sint64;
    public static final FFIType ffi_type_uchar;
    public static final FFIType ffi_type_schar;
    public static final FFIType ffi_type_ushort;
    public static final FFIType ffi_type_sshort;
    public static final FFIType ffi_type_uint;
    public static final FFIType ffi_type_sint;
    public static final FFIType ffi_type_ulong;
    public static final FFIType ffi_type_slong;
    public static final FFIType ffi_type_float;
    public static final FFIType ffi_type_double;
    public static final FFIType ffi_type_longdouble;
    public static final FFIType ffi_type_pointer;

    protected LibFFI() {
        throw new UnsupportedOperationException();
    }

    private static native short FFI_TYPE_DOUBLE();

    private static native int FFI_WIN64();

    private static native int FFI_GNUW64();

    private static native int FFI_UNIX64();

    private static native int FFI_EFI64();

    private static native int FFI_SYSV();

    private static native int FFI_STDCALL();

    private static native int FFI_THISCALL();

    private static native int FFI_FASTCALL();

    private static native int FFI_MS_CDECL();

    private static native int FFI_PASCAL();

    private static native int FFI_REGISTER();

    private static native int FFI_VFP();

    private static native int FFI_FIRST_ABI();

    private static native int FFI_LAST_ABI();

    private static native int FFI_DEFAULT_ABI();

    private static native long nffi_type_void();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_void() {
        long __result = LibFFI.nffi_type_void();
        return FFIType.create(__result);
    }

    private static native long nffi_type_uint8();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_uint8() {
        long __result = LibFFI.nffi_type_uint8();
        return FFIType.create(__result);
    }

    private static native long nffi_type_sint8();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_sint8() {
        long __result = LibFFI.nffi_type_sint8();
        return FFIType.create(__result);
    }

    private static native long nffi_type_uint16();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_uint16() {
        long __result = LibFFI.nffi_type_uint16();
        return FFIType.create(__result);
    }

    private static native long nffi_type_sint16();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_sint16() {
        long __result = LibFFI.nffi_type_sint16();
        return FFIType.create(__result);
    }

    private static native long nffi_type_uint32();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_uint32() {
        long __result = LibFFI.nffi_type_uint32();
        return FFIType.create(__result);
    }

    private static native long nffi_type_sint32();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_sint32() {
        long __result = LibFFI.nffi_type_sint32();
        return FFIType.create(__result);
    }

    private static native long nffi_type_uint64();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_uint64() {
        long __result = LibFFI.nffi_type_uint64();
        return FFIType.create(__result);
    }

    private static native long nffi_type_sint64();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_sint64() {
        long __result = LibFFI.nffi_type_sint64();
        return FFIType.create(__result);
    }

    private static native long nffi_type_uchar();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_uchar() {
        long __result = LibFFI.nffi_type_uchar();
        return FFIType.create(__result);
    }

    private static native long nffi_type_schar();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_schar() {
        long __result = LibFFI.nffi_type_schar();
        return FFIType.create(__result);
    }

    private static native long nffi_type_ushort();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_ushort() {
        long __result = LibFFI.nffi_type_ushort();
        return FFIType.create(__result);
    }

    private static native long nffi_type_sshort();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_sshort() {
        long __result = LibFFI.nffi_type_sshort();
        return FFIType.create(__result);
    }

    private static native long nffi_type_uint();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_uint() {
        long __result = LibFFI.nffi_type_uint();
        return FFIType.create(__result);
    }

    private static native long nffi_type_sint();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_sint() {
        long __result = LibFFI.nffi_type_sint();
        return FFIType.create(__result);
    }

    private static native long nffi_type_ulong();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_ulong() {
        long __result = LibFFI.nffi_type_ulong();
        return FFIType.create(__result);
    }

    private static native long nffi_type_slong();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_slong() {
        long __result = LibFFI.nffi_type_slong();
        return FFIType.create(__result);
    }

    private static native long nffi_type_float();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_float() {
        long __result = LibFFI.nffi_type_float();
        return FFIType.create(__result);
    }

    private static native long nffi_type_double();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_double() {
        long __result = LibFFI.nffi_type_double();
        return FFIType.create(__result);
    }

    private static native long nffi_type_longdouble();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_longdouble() {
        long __result = LibFFI.nffi_type_longdouble();
        return FFIType.create(__result);
    }

    private static native long nffi_type_pointer();

    @NativeType(value="ffi_type *")
    private static FFIType ffi_type_pointer() {
        long __result = LibFFI.nffi_type_pointer();
        return FFIType.create(__result);
    }

    public static native int nffi_prep_cif(long var0, int var2, int var3, long var4, long var6);

    @NativeType(value="ffi_status")
    public static int ffi_prep_cif(@NativeType(value="ffi_cif *") FFICIF cif, @NativeType(value="ffi_abi") int abi, @NativeType(value="ffi_type *") FFIType rtype, @NativeType(value="ffi_type **") @Nullable PointerBuffer atypes) {
        return LibFFI.nffi_prep_cif(cif.address(), abi, Checks.remainingSafe(atypes), rtype.address(), MemoryUtil.memAddressSafe(atypes));
    }

    public static native int nffi_prep_cif_var(long var0, int var2, int var3, int var4, long var5, long var7);

    @NativeType(value="ffi_status")
    public static int ffi_prep_cif_var(@NativeType(value="ffi_cif *") FFICIF cif, @NativeType(value="ffi_abi") int abi, @NativeType(value="unsigned int") int nfixedargs, @NativeType(value="ffi_type *") FFIType rtype, @NativeType(value="ffi_type **") PointerBuffer atypes) {
        return LibFFI.nffi_prep_cif_var(cif.address(), abi, nfixedargs, atypes.remaining(), rtype.address(), MemoryUtil.memAddress(atypes));
    }

    public static native void nffi_call(long var0, long var2, long var4, long var6);

    public static void ffi_call(@NativeType(value="ffi_cif *") FFICIF cif, @NativeType(value="FFI_FN_TYPE") long fn, @NativeType(value="void *") @Nullable ByteBuffer rvalue, @NativeType(value="void **") @Nullable PointerBuffer avalues) {
        if (Checks.CHECKS) {
            Checks.check(fn);
        }
        LibFFI.nffi_call(cif.address(), fn, MemoryUtil.memAddressSafe(rvalue), MemoryUtil.memAddressSafe(avalues));
    }

    public static native int nffi_get_struct_offsets(int var0, long var1, long var3);

    @NativeType(value="ffi_status")
    public static int ffi_get_struct_offsets(@NativeType(value="ffi_abi") int abi, @NativeType(value="ffi_type *") FFIType struct_type, @NativeType(value="size_t *") @Nullable PointerBuffer offsets) {
        return LibFFI.nffi_get_struct_offsets(abi, struct_type.address(), MemoryUtil.memAddressSafe(offsets));
    }

    public static native long nffi_closure_alloc(long var0, long var2);

    @NativeType(value="ffi_closure *")
    public static @Nullable FFIClosure ffi_closure_alloc(@NativeType(value="size_t") long size, @NativeType(value="void **") PointerBuffer code) {
        if (Checks.CHECKS) {
            Checks.check(code, 1);
        }
        long __result = LibFFI.nffi_closure_alloc(size, MemoryUtil.memAddress(code));
        return FFIClosure.createSafe(__result);
    }

    public static native void nffi_closure_free(long var0);

    public static void ffi_closure_free(@NativeType(value="ffi_closure *") FFIClosure writable) {
        LibFFI.nffi_closure_free(writable.address());
    }

    public static native int nffi_prep_closure_loc(long var0, long var2, long var4, long var6, long var8);

    @NativeType(value="ffi_status")
    public static int ffi_prep_closure_loc(@NativeType(value="ffi_closure *") FFIClosure closure, @NativeType(value="ffi_cif *") FFICIF cif, @NativeType(value="FFI_CLOSURE_FUN") long fun, @NativeType(value="void *") long user_data, @NativeType(value="void *") long codeloc) {
        if (Checks.CHECKS) {
            Checks.check(fun);
            Checks.check(codeloc);
        }
        return LibFFI.nffi_prep_closure_loc(closure.address(), cif.address(), fun, user_data, codeloc);
    }

    static {
        Library.initialize();
        FFI_TYPE_LONGDOUBLE = LibFFI.FFI_TYPE_DOUBLE();
        FFI_FIRST_ABI = LibFFI.FFI_FIRST_ABI();
        FFI_WIN64 = LibFFI.FFI_WIN64();
        FFI_GNUW64 = LibFFI.FFI_GNUW64();
        FFI_UNIX64 = LibFFI.FFI_UNIX64();
        FFI_EFI64 = LibFFI.FFI_EFI64();
        FFI_SYSV = LibFFI.FFI_SYSV();
        FFI_STDCALL = LibFFI.FFI_STDCALL();
        FFI_THISCALL = LibFFI.FFI_THISCALL();
        FFI_FASTCALL = LibFFI.FFI_FASTCALL();
        FFI_MS_CDECL = LibFFI.FFI_MS_CDECL();
        FFI_PASCAL = LibFFI.FFI_PASCAL();
        FFI_REGISTER = LibFFI.FFI_REGISTER();
        FFI_VFP = LibFFI.FFI_VFP();
        FFI_LAST_ABI = LibFFI.FFI_LAST_ABI();
        FFI_DEFAULT_ABI = LibFFI.FFI_DEFAULT_ABI();
        ffi_type_void = LibFFI.ffi_type_void();
        ffi_type_uint8 = LibFFI.ffi_type_uint8();
        ffi_type_sint8 = LibFFI.ffi_type_sint8();
        ffi_type_uint16 = LibFFI.ffi_type_uint16();
        ffi_type_sint16 = LibFFI.ffi_type_sint16();
        ffi_type_uint32 = LibFFI.ffi_type_uint32();
        ffi_type_sint32 = LibFFI.ffi_type_sint32();
        ffi_type_uint64 = LibFFI.ffi_type_uint64();
        ffi_type_sint64 = LibFFI.ffi_type_sint64();
        ffi_type_uchar = LibFFI.ffi_type_uchar();
        ffi_type_schar = LibFFI.ffi_type_schar();
        ffi_type_ushort = LibFFI.ffi_type_ushort();
        ffi_type_sshort = LibFFI.ffi_type_sshort();
        ffi_type_uint = LibFFI.ffi_type_uint();
        ffi_type_sint = LibFFI.ffi_type_sint();
        ffi_type_ulong = LibFFI.ffi_type_ulong();
        ffi_type_slong = LibFFI.ffi_type_slong();
        ffi_type_float = LibFFI.ffi_type_float();
        ffi_type_double = LibFFI.ffi_type_double();
        ffi_type_longdouble = LibFFI.ffi_type_longdouble();
        ffi_type_pointer = LibFFI.ffi_type_pointer();
    }
}

