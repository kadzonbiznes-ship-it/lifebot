/*
 * Decompiled with CFR 0.152.
 */
package jdk.proxy1;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.Pdh;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

public final class $Proxy14
extends Proxy
implements Pdh {
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
    private static final Method m12;
    private static final Method m13;
    private static final Method m14;
    private static final Method m15;
    private static final Method m16;
    private static final Method m17;
    private static final Method m18;
    private static final Method m19;

    public $Proxy14(InvocationHandler invocationHandler) {
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
    public final int PdhCollectQueryDataEx(WinNT.HANDLE hANDLE, int n, WinNT.HANDLE hANDLE2) {
        try {
            return (Integer)this.h.invoke(this, m3, new Object[]{hANDLE, n, hANDLE2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int PdhSetQueryTimeRange(WinNT.HANDLE hANDLE, Pdh.PDH_TIME_INFO pDH_TIME_INFO) {
        try {
            return (Integer)this.h.invoke(this, m4, new Object[]{hANDLE, pDH_TIME_INFO});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int PdhGetRawCounterValue(WinNT.HANDLE hANDLE, WinDef.DWORDByReference dWORDByReference, Pdh.PDH_RAW_COUNTER pDH_RAW_COUNTER) {
        try {
            return (Integer)this.h.invoke(this, m5, new Object[]{hANDLE, dWORDByReference, pDH_RAW_COUNTER});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int PdhCollectQueryData(WinNT.HANDLE hANDLE) {
        try {
            return (Integer)this.h.invoke(this, m6, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int PdhLookupPerfIndexByName(String string, String string2, WinDef.DWORDByReference dWORDByReference) {
        try {
            return (Integer)this.h.invoke(this, m7, new Object[]{string, string2, dWORDByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int PdhAddEnglishCounter(WinNT.HANDLE hANDLE, String string, BaseTSD.DWORD_PTR dWORD_PTR, WinNT.HANDLEByReference hANDLEByReference) {
        try {
            return (Integer)this.h.invoke(this, m8, new Object[]{hANDLE, string, dWORD_PTR, hANDLEByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int PdhLookupPerfNameByIndex(String string, int n, Pointer pointer, WinDef.DWORDByReference dWORDByReference) {
        try {
            return (Integer)this.h.invoke(this, m9, new Object[]{string, n, pointer, dWORDByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int PdhCollectQueryDataWithTime(WinNT.HANDLE hANDLE, WinDef.LONGLONGByReference lONGLONGByReference) {
        try {
            return (Integer)this.h.invoke(this, m10, new Object[]{hANDLE, lONGLONGByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int PdhEnumObjectItems(String string, String string2, String string3, Pointer pointer, WinDef.DWORDByReference dWORDByReference, Pointer pointer2, WinDef.DWORDByReference dWORDByReference2, int n, int n2) {
        try {
            return (Integer)this.h.invoke(this, m11, new Object[]{string, string2, string3, pointer, dWORDByReference, pointer2, dWORDByReference2, n, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int PdhConnectMachine(String string) {
        try {
            return (Integer)this.h.invoke(this, m12, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int PdhCloseQuery(WinNT.HANDLE hANDLE) {
        try {
            return (Integer)this.h.invoke(this, m13, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int PdhAddCounter(WinNT.HANDLE hANDLE, String string, BaseTSD.DWORD_PTR dWORD_PTR, WinNT.HANDLEByReference hANDLEByReference) {
        try {
            return (Integer)this.h.invoke(this, m14, new Object[]{hANDLE, string, dWORD_PTR, hANDLEByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int PdhGetDllVersion(WinDef.DWORDByReference dWORDByReference) {
        try {
            return (Integer)this.h.invoke(this, m15, new Object[]{dWORDByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int PdhMakeCounterPath(Pdh.PDH_COUNTER_PATH_ELEMENTS pDH_COUNTER_PATH_ELEMENTS, char[] cArray, WinDef.DWORDByReference dWORDByReference, int n) {
        try {
            return (Integer)this.h.invoke(this, m16, new Object[]{pDH_COUNTER_PATH_ELEMENTS, cArray, dWORDByReference, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int PdhRemoveCounter(WinNT.HANDLE hANDLE) {
        try {
            return (Integer)this.h.invoke(this, m17, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int PdhValidatePath(String string) {
        try {
            return (Integer)this.h.invoke(this, m18, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int PdhOpenQuery(String string, BaseTSD.DWORD_PTR dWORD_PTR, WinNT.HANDLEByReference hANDLEByReference) {
        try {
            return (Integer)this.h.invoke(this, m19, new Object[]{string, dWORD_PTR, hANDLEByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    static {
        ClassLoader classLoader = $Proxy14.class.getClassLoader();
        try {
            m0 = Class.forName("java.lang.Object", false, classLoader).getMethod("hashCode", new Class[0]);
            m1 = Class.forName("java.lang.Object", false, classLoader).getMethod("equals", Class.forName("java.lang.Object", false, classLoader));
            m2 = Class.forName("java.lang.Object", false, classLoader).getMethod("toString", new Class[0]);
            m3 = Class.forName("com.sun.jna.platform.win32.Pdh", false, classLoader).getMethod("PdhCollectQueryDataEx", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m4 = Class.forName("com.sun.jna.platform.win32.Pdh", false, classLoader).getMethod("PdhSetQueryTimeRange", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.Pdh$PDH_TIME_INFO", false, classLoader));
            m5 = Class.forName("com.sun.jna.platform.win32.Pdh", false, classLoader).getMethod("PdhGetRawCounterValue", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORDByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.Pdh$PDH_RAW_COUNTER", false, classLoader));
            m6 = Class.forName("com.sun.jna.platform.win32.Pdh", false, classLoader).getMethod("PdhCollectQueryData", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m7 = Class.forName("com.sun.jna.platform.win32.Pdh", false, classLoader).getMethod("PdhLookupPerfIndexByName", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORDByReference", false, classLoader));
            m8 = Class.forName("com.sun.jna.platform.win32.Pdh", false, classLoader).getMethod("PdhAddEnglishCounter", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.BaseTSD$DWORD_PTR", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$HANDLEByReference", false, classLoader));
            m9 = Class.forName("com.sun.jna.platform.win32.Pdh", false, classLoader).getMethod("PdhLookupPerfNameByIndex", Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORDByReference", false, classLoader));
            m10 = Class.forName("com.sun.jna.platform.win32.Pdh", false, classLoader).getMethod("PdhCollectQueryDataWithTime", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$LONGLONGByReference", false, classLoader));
            m11 = Class.forName("com.sun.jna.platform.win32.Pdh", false, classLoader).getMethod("PdhEnumObjectItems", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORDByReference", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORDByReference", false, classLoader), Integer.TYPE, Integer.TYPE);
            m12 = Class.forName("com.sun.jna.platform.win32.Pdh", false, classLoader).getMethod("PdhConnectMachine", Class.forName("java.lang.String", false, classLoader));
            m13 = Class.forName("com.sun.jna.platform.win32.Pdh", false, classLoader).getMethod("PdhCloseQuery", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m14 = Class.forName("com.sun.jna.platform.win32.Pdh", false, classLoader).getMethod("PdhAddCounter", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.BaseTSD$DWORD_PTR", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$HANDLEByReference", false, classLoader));
            m15 = Class.forName("com.sun.jna.platform.win32.Pdh", false, classLoader).getMethod("PdhGetDllVersion", Class.forName("com.sun.jna.platform.win32.WinDef$DWORDByReference", false, classLoader));
            m16 = Class.forName("com.sun.jna.platform.win32.Pdh", false, classLoader).getMethod("PdhMakeCounterPath", Class.forName("com.sun.jna.platform.win32.Pdh$PDH_COUNTER_PATH_ELEMENTS", false, classLoader), Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORDByReference", false, classLoader), Integer.TYPE);
            m17 = Class.forName("com.sun.jna.platform.win32.Pdh", false, classLoader).getMethod("PdhRemoveCounter", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m18 = Class.forName("com.sun.jna.platform.win32.Pdh", false, classLoader).getMethod("PdhValidatePath", Class.forName("java.lang.String", false, classLoader));
            m19 = Class.forName("com.sun.jna.platform.win32.Pdh", false, classLoader).getMethod("PdhOpenQuery", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.BaseTSD$DWORD_PTR", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$HANDLEByReference", false, classLoader));
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

