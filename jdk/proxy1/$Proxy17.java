/*
 * Decompiled with CFR 0.152.
 */
package jdk.proxy1;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

public final class $Proxy17
extends Proxy
implements Psapi {
    private static final Method m0;
    private static final Method m1;
    private static final Method m2;
    private static final Method m3;
    private static final Method m4;
    private static final Method m5;
    private static final Method m6;
    private static final Method m7;
    private static final Method m8;
    private static final Method m9;
    private static final Method m10;
    private static final Method m11;

    public $Proxy17(InvocationHandler invocationHandler) {
        super(invocationHandler);
    }

    public final int hashCode() {
        try {
            return (Integer)this.h.invoke(this, m0, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    public final boolean equals(Object object) {
        try {
            return (Boolean)this.h.invoke(this, m1, new Object[]{object});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    public final String toString() {
        try {
            return (String)this.h.invoke(this, m2, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetPerformanceInfo(Psapi.PERFORMANCE_INFORMATION pERFORMANCE_INFORMATION, int n) {
        try {
            return (Boolean)this.h.invoke(this, m3, new Object[]{pERFORMANCE_INFORMATION, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean QueryWorkingSetEx(WinNT.HANDLE hANDLE, Pointer pointer, int n) {
        try {
            return (Boolean)this.h.invoke(this, m4, new Object[]{hANDLE, pointer, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean EnumProcesses(int[] nArray, int n, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m5, new Object[]{nArray, n, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean EnumProcessModules(WinNT.HANDLE hANDLE, WinDef.HMODULE[] hMODULEArray, int n, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m6, new Object[]{hANDLE, hMODULEArray, n, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetProcessImageFileName(WinNT.HANDLE hANDLE, char[] cArray, int n) {
        try {
            return (Integer)this.h.invoke(this, m7, new Object[]{hANDLE, cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetModuleFileNameEx(WinNT.HANDLE hANDLE, WinNT.HANDLE hANDLE2, Pointer pointer, int n) {
        try {
            return (Integer)this.h.invoke(this, m8, new Object[]{hANDLE, hANDLE2, pointer, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetModuleFileNameExA(WinNT.HANDLE hANDLE, WinNT.HANDLE hANDLE2, byte[] byArray, int n) {
        try {
            return (Integer)this.h.invoke(this, m9, new Object[]{hANDLE, hANDLE2, byArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetModuleFileNameExW(WinNT.HANDLE hANDLE, WinNT.HANDLE hANDLE2, char[] cArray, int n) {
        try {
            return (Integer)this.h.invoke(this, m10, new Object[]{hANDLE, hANDLE2, cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetModuleInformation(WinNT.HANDLE hANDLE, WinDef.HMODULE hMODULE, Psapi.MODULEINFO mODULEINFO, int n) {
        try {
            return (Boolean)this.h.invoke(this, m11, new Object[]{hANDLE, hMODULE, mODULEINFO, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    static {
        ClassLoader classLoader = $Proxy17.class.getClassLoader();
        try {
            m0 = Class.forName("java.lang.Object", false, classLoader).getMethod("hashCode", new Class[0]);
            m1 = Class.forName("java.lang.Object", false, classLoader).getMethod("equals", Class.forName("java.lang.Object", false, classLoader));
            m2 = Class.forName("java.lang.Object", false, classLoader).getMethod("toString", new Class[0]);
            m3 = Class.forName("com.sun.jna.platform.win32.Psapi", false, classLoader).getMethod("GetPerformanceInfo", Class.forName("com.sun.jna.platform.win32.Psapi$PERFORMANCE_INFORMATION", false, classLoader), Integer.TYPE);
            m4 = Class.forName("com.sun.jna.platform.win32.Psapi", false, classLoader).getMethod("QueryWorkingSetEx", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE);
            m5 = Class.forName("com.sun.jna.platform.win32.Psapi", false, classLoader).getMethod("EnumProcesses", Class.forName("[I", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m6 = Class.forName("com.sun.jna.platform.win32.Psapi", false, classLoader).getMethod("EnumProcessModules", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("[Lcom.sun.jna.platform.win32.WinDef$HMODULE;", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m7 = Class.forName("com.sun.jna.platform.win32.Psapi", false, classLoader).getMethod("GetProcessImageFileName", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE);
            m8 = Class.forName("com.sun.jna.platform.win32.Psapi", false, classLoader).getMethod("GetModuleFileNameEx", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE);
            m9 = Class.forName("com.sun.jna.platform.win32.Psapi", false, classLoader).getMethod("GetModuleFileNameExA", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("[B", false, classLoader), Integer.TYPE);
            m10 = Class.forName("com.sun.jna.platform.win32.Psapi", false, classLoader).getMethod("GetModuleFileNameExW", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE);
            m11 = Class.forName("com.sun.jna.platform.win32.Psapi", false, classLoader).getMethod("GetModuleInformation", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$HMODULE", false, classLoader), Class.forName("com.sun.jna.platform.win32.Psapi$MODULEINFO", false, classLoader), Integer.TYPE);
            return;
        }
        catch (NoSuchMethodException noSuchMethodException) {
            throw new NoSuchMethodError(noSuchMethodException.getMessage());
        }
        catch (ClassNotFoundException classNotFoundException) {
            throw new NoClassDefFoundError(classNotFoundException.getMessage());
        }
    }

    private static MethodHandles.Lookup proxyClassLookup(MethodHandles.Lookup lookup) throws IllegalAccessException {
        if (lookup.lookupClass() == Proxy.class && lookup.hasFullPrivilegeAccess()) {
            return MethodHandles.lookup();
        }
        throw new IllegalAccessException(lookup.toString());
    }
}

