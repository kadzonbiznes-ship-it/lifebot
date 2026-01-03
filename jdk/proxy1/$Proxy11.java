/*
 * Decompiled with CFR 0.152.
 */
package jdk.proxy1;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.Unknown;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

public final class $Proxy11
extends Proxy
implements Ole32 {
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
    private static final Method m20;
    private static final Method m21;
    private static final Method m22;
    private static final Method m23;
    private static final Method m24;

    public $Proxy11(InvocationHandler invocationHandler) {
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
    public final WinNT.HRESULT OleRun(Pointer pointer) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m3, new Object[]{pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT CoInitializeSecurity(WinNT.SECURITY_DESCRIPTOR sECURITY_DESCRIPTOR, int n, Pointer pointer, Pointer pointer2, int n2, int n3, Pointer pointer3, int n4, Pointer pointer4) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m4, new Object[]{sECURITY_DESCRIPTOR, n, pointer, pointer2, n2, n3, pointer3, n4, pointer4});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean CoIsHandlerConnected(Pointer pointer) {
        try {
            return (Boolean)this.h.invoke(this, m5, new Object[]{pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT GetRunningObjectTable(WinDef.DWORD dWORD, PointerByReference pointerByReference) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m6, new Object[]{dWORD, pointerByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void OleUninitialize() {
        try {
            this.h.invoke(this, m7, null);
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer CoTaskMemAlloc(long l) {
        try {
            return (Pointer)this.h.invoke(this, m8, new Object[]{l});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT CLSIDFromProgID(String string, Guid.CLSID.ByReference byReference) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m9, new Object[]{string, byReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT CoGetMalloc(WinDef.DWORD dWORD, PointerByReference pointerByReference) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m10, new Object[]{dWORD, pointerByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT CreateBindCtx(WinDef.DWORD dWORD, PointerByReference pointerByReference) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m11, new Object[]{dWORD, pointerByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT OleInitialize(Pointer pointer) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m12, new Object[]{pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT OleFlushClipboard() {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m13, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void CoUninitialize() {
        try {
            this.h.invoke(this, m14, null);
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT CoCreateGuid(Guid.GUID gUID) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m15, new Object[]{gUID});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void CoTaskMemFree(Pointer pointer) {
        try {
            this.h.invoke(this, m16, new Object[]{pointer});
            return;
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer CoTaskMemRealloc(Pointer pointer, long l) {
        try {
            return (Pointer)this.h.invoke(this, m17, new Object[]{pointer, l});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT IIDFromString(String string, Guid.GUID gUID) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m18, new Object[]{string, gUID});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT CLSIDFromString(String string, Guid.CLSID.ByReference byReference) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m19, new Object[]{string, byReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT CoCreateInstance(Guid.GUID gUID, Pointer pointer, int n, Guid.GUID gUID2, PointerByReference pointerByReference) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m20, new Object[]{gUID, pointer, n, gUID2, pointerByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int StringFromGUID2(Guid.GUID gUID, char[] cArray, int n) {
        try {
            return (Integer)this.h.invoke(this, m21, new Object[]{gUID, cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT CoSetProxyBlanket(Unknown unknown, int n, int n2, WTypes.LPOLESTR lPOLESTR, int n3, int n4, Pointer pointer, int n5) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m22, new Object[]{unknown, n, n2, lPOLESTR, n3, n4, pointer, n5});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT CoInitializeEx(Pointer pointer, int n) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m23, new Object[]{pointer, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT CoInitialize(WinDef.LPVOID lPVOID) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m24, new Object[]{lPVOID});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    static {
        ClassLoader classLoader = $Proxy11.class.getClassLoader();
        try {
            m0 = Class.forName("java.lang.Object", false, classLoader).getMethod("hashCode", new Class[0]);
            m1 = Class.forName("java.lang.Object", false, classLoader).getMethod("equals", Class.forName("java.lang.Object", false, classLoader));
            m2 = Class.forName("java.lang.Object", false, classLoader).getMethod("toString", new Class[0]);
            m3 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("OleRun", Class.forName("com.sun.jna.Pointer", false, classLoader));
            m4 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("CoInitializeSecurity", Class.forName("com.sun.jna.platform.win32.WinNT$SECURITY_DESCRIPTOR", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader));
            m5 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("CoIsHandlerConnected", Class.forName("com.sun.jna.Pointer", false, classLoader));
            m6 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("GetRunningObjectTable", Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m7 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("OleUninitialize", new Class[0]);
            m8 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("CoTaskMemAlloc", Long.TYPE);
            m9 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("CLSIDFromProgID", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.Guid$CLSID$ByReference", false, classLoader));
            m10 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("CoGetMalloc", Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m11 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("CreateBindCtx", Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m12 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("OleInitialize", Class.forName("com.sun.jna.Pointer", false, classLoader));
            m13 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("OleFlushClipboard", new Class[0]);
            m14 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("CoUninitialize", new Class[0]);
            m15 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("CoCreateGuid", Class.forName("com.sun.jna.platform.win32.Guid$GUID", false, classLoader));
            m16 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("CoTaskMemFree", Class.forName("com.sun.jna.Pointer", false, classLoader));
            m17 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("CoTaskMemRealloc", Class.forName("com.sun.jna.Pointer", false, classLoader), Long.TYPE);
            m18 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("IIDFromString", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.Guid$GUID", false, classLoader));
            m19 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("CLSIDFromString", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.Guid$CLSID$ByReference", false, classLoader));
            m20 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("CoCreateInstance", Class.forName("com.sun.jna.platform.win32.Guid$GUID", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.platform.win32.Guid$GUID", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m21 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("StringFromGUID2", Class.forName("com.sun.jna.platform.win32.Guid$GUID", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE);
            m22 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("CoSetProxyBlanket", Class.forName("com.sun.jna.platform.win32.COM.Unknown", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WTypes$LPOLESTR", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE);
            m23 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("CoInitializeEx", Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE);
            m24 = Class.forName("com.sun.jna.platform.win32.Ole32", false, classLoader).getMethod("CoInitialize", Class.forName("com.sun.jna.platform.win32.WinDef$LPVOID", false, classLoader));
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

