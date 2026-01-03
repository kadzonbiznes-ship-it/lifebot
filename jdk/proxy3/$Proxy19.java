/*
 * Decompiled with CFR 0.152.
 */
package jdk.proxy3;

import com.sun.jna.platform.win32.WinDef;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import org.pbrands.ui.overlay.ImGuiOverlay;

public final class $Proxy19
extends Proxy
implements ImGuiOverlay.User32Ext {
    private static final Method m0;
    private static final Method m1;
    private static final Method m2;
    private static final Method m3;
    private static final Method m4;
    private static final Method m5;
    private static final Method m6;
    private static final Method m7;

    public $Proxy19(InvocationHandler invocationHandler) {
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
    public final short GetKeyState(int n) {
        try {
            return (Short)this.h.invoke(this, m3, new Object[]{n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.HKL GetKeyboardLayout(int n) {
        try {
            return (WinDef.HKL)this.h.invoke(this, m4, new Object[]{n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int ToUnicodeEx(int n, int n2, byte[] byArray, char[] cArray, int n3, int n4, WinDef.HKL hKL) {
        try {
            return (Integer)this.h.invoke(this, m5, new Object[]{n, n2, byArray, cArray, n3, n4, hKL});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetWindowDisplayAffinity(WinDef.HWND hWND, int n) {
        try {
            return (Boolean)this.h.invoke(this, m6, new Object[]{hWND, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean IsIconic(WinDef.HWND hWND) {
        try {
            return (Boolean)this.h.invoke(this, m7, new Object[]{hWND});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    static {
        ClassLoader classLoader = $Proxy19.class.getClassLoader();
        try {
            m0 = Class.forName("java.lang.Object", false, classLoader).getMethod("hashCode", new Class[0]);
            m1 = Class.forName("java.lang.Object", false, classLoader).getMethod("equals", Class.forName("java.lang.Object", false, classLoader));
            m2 = Class.forName("java.lang.Object", false, classLoader).getMethod("toString", new Class[0]);
            m3 = Class.forName("org.pbrands.ui.overlay.ImGuiOverlay$User32Ext", false, classLoader).getMethod("GetKeyState", Integer.TYPE);
            m4 = Class.forName("org.pbrands.ui.overlay.ImGuiOverlay$User32Ext", false, classLoader).getMethod("GetKeyboardLayout", Integer.TYPE);
            m5 = Class.forName("org.pbrands.ui.overlay.ImGuiOverlay$User32Ext", false, classLoader).getMethod("ToUnicodeEx", Integer.TYPE, Integer.TYPE, Class.forName("[B", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$HKL", false, classLoader));
            m6 = Class.forName("org.pbrands.ui.overlay.ImGuiOverlay$User32Ext", false, classLoader).getMethod("SetWindowDisplayAffinity", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE);
            m7 = Class.forName("org.pbrands.ui.overlay.ImGuiOverlay$User32Ext", false, classLoader).getMethod("IsIconic", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader));
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

