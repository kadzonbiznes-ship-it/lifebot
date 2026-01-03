/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.NativeType
 */
package org.lwjgl.glfw;

import org.lwjgl.system.APIUtil;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.libffi.FFICIF;
import org.lwjgl.system.libffi.LibFFI;

@FunctionalInterface
@NativeType(value="GLFWcursorposfun")
public interface GLFWCursorPosCallbackI
extends CallbackI {
    public static final FFICIF CIF = APIUtil.apiCreateCIF(LibFFI.FFI_DEFAULT_ABI, LibFFI.ffi_type_void, LibFFI.ffi_type_pointer, LibFFI.ffi_type_double, LibFFI.ffi_type_double);

    @Override
    default public FFICIF getCallInterface() {
        return CIF;
    }

    @Override
    default public void callback(long ret, long args) {
        this.invoke(MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args)), MemoryUtil.memGetDouble(MemoryUtil.memGetAddress(args + (long)POINTER_SIZE)), MemoryUtil.memGetDouble(MemoryUtil.memGetAddress(args + (long)(2 * POINTER_SIZE))));
    }

    public void invoke(@NativeType(value="GLFWwindow *") long var1, double var3, double var5);
}

