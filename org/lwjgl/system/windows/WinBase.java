/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.NativeType
 */
package org.lwjgl.system.windows;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.Checks;
import org.lwjgl.system.Library;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;

public class WinBase {
    public static final int FALSE = 0;
    public static final int TRUE = 1;

    protected WinBase() {
        throw new UnsupportedOperationException();
    }

    public static native long nLocalFree(long var0, long var2);

    @NativeType(value="HLOCAL")
    public static long LocalFree(@NativeType(value="DWORD *") @Nullable IntBuffer _GetLastError, @NativeType(value="HLOCAL") long hMem) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)_GetLastError, 1);
            Checks.check(hMem);
        }
        return WinBase.nLocalFree(MemoryUtil.memAddressSafe(_GetLastError), hMem);
    }

    @NativeType(value="DWORD")
    public static native int GetLastError();

    public static native long nGetModuleHandle(long var0, long var2);

    @NativeType(value="HMODULE")
    public static long GetModuleHandle(@NativeType(value="DWORD *") @Nullable IntBuffer _GetLastError, @NativeType(value="LPCTSTR") @Nullable ByteBuffer moduleName) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)_GetLastError, 1);
            Checks.checkNT2Safe(moduleName);
        }
        return WinBase.nGetModuleHandle(MemoryUtil.memAddressSafe(_GetLastError), MemoryUtil.memAddressSafe(moduleName));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="HMODULE")
    public static long GetModuleHandle(@NativeType(value="DWORD *") @Nullable IntBuffer _GetLastError, @NativeType(value="LPCTSTR") @Nullable CharSequence moduleName) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)_GetLastError, 1);
        }
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF16Safe(moduleName, true);
            long moduleNameEncoded = moduleName == null ? 0L : stack.getPointerAddress();
            long l = WinBase.nGetModuleHandle(MemoryUtil.memAddressSafe(_GetLastError), moduleNameEncoded);
            return l;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native int nGetModuleFileName(long var0, long var2, long var4, int var6);

    @NativeType(value="DWORD")
    public static int GetModuleFileName(@NativeType(value="DWORD *") @Nullable IntBuffer _GetLastError, @NativeType(value="HMODULE") long hModule, @NativeType(value="LPTSTR") ByteBuffer lpFilename) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)_GetLastError, 1);
        }
        return WinBase.nGetModuleFileName(MemoryUtil.memAddressSafe(_GetLastError), hModule, MemoryUtil.memAddress(lpFilename), lpFilename.remaining() >> 1);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="DWORD")
    public static String GetModuleFileName(@NativeType(value="DWORD *") @Nullable IntBuffer _GetLastError, @NativeType(value="HMODULE") long hModule, @NativeType(value="DWORD") int nSize) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)_GetLastError, 1);
        }
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            ByteBuffer lpFilename = stack.malloc(nSize << 1);
            int __result = WinBase.nGetModuleFileName(MemoryUtil.memAddressSafe(_GetLastError), hModule, MemoryUtil.memAddress(lpFilename), nSize);
            String string = MemoryUtil.memUTF16(lpFilename, __result);
            return string;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native long nLoadLibrary(long var0, long var2);

    @NativeType(value="HMODULE")
    public static long LoadLibrary(@NativeType(value="DWORD *") @Nullable IntBuffer _GetLastError, @NativeType(value="LPCTSTR") ByteBuffer name) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)_GetLastError, 1);
            Checks.checkNT2(name);
        }
        return WinBase.nLoadLibrary(MemoryUtil.memAddressSafe(_GetLastError), MemoryUtil.memAddress(name));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="HMODULE")
    public static long LoadLibrary(@NativeType(value="DWORD *") @Nullable IntBuffer _GetLastError, @NativeType(value="LPCTSTR") CharSequence name) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)_GetLastError, 1);
        }
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF16(name, true);
            long nameEncoded = stack.getPointerAddress();
            long l = WinBase.nLoadLibrary(MemoryUtil.memAddressSafe(_GetLastError), nameEncoded);
            return l;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native long nGetProcAddress(long var0, long var2, long var4);

    @NativeType(value="FARPROC")
    public static long GetProcAddress(@NativeType(value="DWORD *") @Nullable IntBuffer _GetLastError, @NativeType(value="HMODULE") long handle, @NativeType(value="LPCSTR") ByteBuffer name) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)_GetLastError, 1);
            Checks.check(handle);
            Checks.checkNT1(name);
        }
        return WinBase.nGetProcAddress(MemoryUtil.memAddressSafe(_GetLastError), handle, MemoryUtil.memAddress(name));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="FARPROC")
    public static long GetProcAddress(@NativeType(value="DWORD *") @Nullable IntBuffer _GetLastError, @NativeType(value="HMODULE") long handle, @NativeType(value="LPCSTR") CharSequence name) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)_GetLastError, 1);
            Checks.check(handle);
        }
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nASCII(name, true);
            long nameEncoded = stack.getPointerAddress();
            long l = WinBase.nGetProcAddress(MemoryUtil.memAddressSafe(_GetLastError), handle, nameEncoded);
            return l;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native int nFreeLibrary(long var0, long var2);

    @NativeType(value="BOOL")
    public static boolean FreeLibrary(@NativeType(value="DWORD *") @Nullable IntBuffer _GetLastError, @NativeType(value="HMODULE") long handle) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)_GetLastError, 1);
            Checks.check(handle);
        }
        return WinBase.nFreeLibrary(MemoryUtil.memAddressSafe(_GetLastError), handle) != 0;
    }

    static {
        Library.initialize();
    }
}

