/*
 * Decompiled with CFR 0.152.
 */
package jdk.proxy1;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import org.pbrands.hid.kfc.api.KfcLibrary;

public final class $Proxy5
extends Proxy
implements KfcLibrary {
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

    public $Proxy5(InvocationHandler invocationHandler) {
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
    public final WinDef.BOOL kfc_responsive() {
        try {
            return (WinDef.BOOL)this.h.invoke(this, m3, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int kfc_receive(KfcLibrary.KfcContext kfcContext, KfcLibrary.KfcDevice kfcDevice, Pointer pointer, int n) {
        try {
            return (Integer)this.h.invoke(this, m4, new Object[]{kfcContext, kfcDevice, pointer, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void kfc_set_filter(KfcLibrary.KfcContext kfcContext, KfcLibrary.KfcPredicate kfcPredicate, KfcLibrary.KfcFilter kfcFilter) {
        try {
            this.h.invoke(this, m5, new Object[]{kfcContext, kfcPredicate, kfcFilter});
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
    public final void kfc_set_precedence(KfcLibrary.KfcContext kfcContext, KfcLibrary.KfcDevice kfcDevice, KfcLibrary.KfcPrecedence kfcPrecedence) {
        try {
            this.h.invoke(this, m6, new Object[]{kfcContext, kfcDevice, kfcPrecedence});
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
    public final KfcLibrary.KfcFilter kfc_get_filter(KfcLibrary.KfcContext kfcContext, KfcLibrary.KfcDevice kfcDevice) {
        try {
            return (KfcLibrary.KfcFilter)this.h.invoke(this, m7, new Object[]{kfcContext, kfcDevice});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final KfcLibrary.KfcPrecedence kfc_get_precedence(KfcLibrary.KfcContext kfcContext, KfcLibrary.KfcDevice kfcDevice) {
        try {
            return (KfcLibrary.KfcPrecedence)this.h.invoke(this, m8, new Object[]{kfcContext, kfcDevice});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int kfc_is_keyboard(KfcLibrary.KfcDevice kfcDevice) {
        try {
            return (Integer)this.h.invoke(this, m9, new Object[]{kfcDevice});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final KfcLibrary.KfcContext kfc_create_context() {
        try {
            return (KfcLibrary.KfcContext)this.h.invoke(this, m10, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int kfc_is_invalid(KfcLibrary.KfcDevice kfcDevice) {
        try {
            return (Integer)this.h.invoke(this, m11, new Object[]{kfcDevice});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int kfc_is_mouse(KfcLibrary.KfcDevice kfcDevice) {
        try {
            return (Integer)this.h.invoke(this, m12, new Object[]{kfcDevice});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final KfcLibrary.KfcDevice kfc_wait_with_timeout(KfcLibrary.KfcContext kfcContext, long l) {
        try {
            return (KfcLibrary.KfcDevice)this.h.invoke(this, m13, new Object[]{kfcContext, l});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int kfc_get_hardware_id(KfcLibrary.KfcContext kfcContext, KfcLibrary.KfcDevice kfcDevice, Pointer pointer, int n) {
        try {
            return (Integer)this.h.invoke(this, m14, new Object[]{kfcContext, kfcDevice, pointer, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void kfc_destroy_context(KfcLibrary.KfcContext kfcContext) {
        try {
            this.h.invoke(this, m15, new Object[]{kfcContext});
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
    public final int kfc_send(KfcLibrary.KfcContext kfcContext, KfcLibrary.KfcDevice kfcDevice, Pointer pointer, int n) {
        try {
            return (Integer)this.h.invoke(this, m16, new Object[]{kfcContext, kfcDevice, pointer, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final KfcLibrary.KfcDevice kfc_wait(KfcLibrary.KfcContext kfcContext) {
        try {
            return (KfcLibrary.KfcDevice)this.h.invoke(this, m17, new Object[]{kfcContext});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final byte LoadKfc(byte[] byArray, int n) {
        try {
            return (Byte)this.h.invoke(this, m18, new Object[]{byArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    static {
        ClassLoader classLoader = $Proxy5.class.getClassLoader();
        try {
            m0 = Class.forName("java.lang.Object", false, classLoader).getMethod("hashCode", new Class[0]);
            m1 = Class.forName("java.lang.Object", false, classLoader).getMethod("equals", Class.forName("java.lang.Object", false, classLoader));
            m2 = Class.forName("java.lang.Object", false, classLoader).getMethod("toString", new Class[0]);
            m3 = Class.forName("org.pbrands.hid.kfc.api.KfcLibrary", false, classLoader).getMethod("kfc_responsive", new Class[0]);
            m4 = Class.forName("org.pbrands.hid.kfc.api.KfcLibrary", false, classLoader).getMethod("kfc_receive", Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcContext", false, classLoader), Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcDevice", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE);
            m5 = Class.forName("org.pbrands.hid.kfc.api.KfcLibrary", false, classLoader).getMethod("kfc_set_filter", Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcContext", false, classLoader), Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcPredicate", false, classLoader), Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcFilter", false, classLoader));
            m6 = Class.forName("org.pbrands.hid.kfc.api.KfcLibrary", false, classLoader).getMethod("kfc_set_precedence", Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcContext", false, classLoader), Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcDevice", false, classLoader), Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcPrecedence", false, classLoader));
            m7 = Class.forName("org.pbrands.hid.kfc.api.KfcLibrary", false, classLoader).getMethod("kfc_get_filter", Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcContext", false, classLoader), Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcDevice", false, classLoader));
            m8 = Class.forName("org.pbrands.hid.kfc.api.KfcLibrary", false, classLoader).getMethod("kfc_get_precedence", Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcContext", false, classLoader), Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcDevice", false, classLoader));
            m9 = Class.forName("org.pbrands.hid.kfc.api.KfcLibrary", false, classLoader).getMethod("kfc_is_keyboard", Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcDevice", false, classLoader));
            m10 = Class.forName("org.pbrands.hid.kfc.api.KfcLibrary", false, classLoader).getMethod("kfc_create_context", new Class[0]);
            m11 = Class.forName("org.pbrands.hid.kfc.api.KfcLibrary", false, classLoader).getMethod("kfc_is_invalid", Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcDevice", false, classLoader));
            m12 = Class.forName("org.pbrands.hid.kfc.api.KfcLibrary", false, classLoader).getMethod("kfc_is_mouse", Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcDevice", false, classLoader));
            m13 = Class.forName("org.pbrands.hid.kfc.api.KfcLibrary", false, classLoader).getMethod("kfc_wait_with_timeout", Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcContext", false, classLoader), Long.TYPE);
            m14 = Class.forName("org.pbrands.hid.kfc.api.KfcLibrary", false, classLoader).getMethod("kfc_get_hardware_id", Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcContext", false, classLoader), Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcDevice", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE);
            m15 = Class.forName("org.pbrands.hid.kfc.api.KfcLibrary", false, classLoader).getMethod("kfc_destroy_context", Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcContext", false, classLoader));
            m16 = Class.forName("org.pbrands.hid.kfc.api.KfcLibrary", false, classLoader).getMethod("kfc_send", Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcContext", false, classLoader), Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcDevice", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE);
            m17 = Class.forName("org.pbrands.hid.kfc.api.KfcLibrary", false, classLoader).getMethod("kfc_wait", Class.forName("org.pbrands.hid.kfc.api.KfcLibrary$KfcContext", false, classLoader));
            m18 = Class.forName("org.pbrands.hid.kfc.api.KfcLibrary", false, classLoader).getMethod("LoadKfc", Class.forName("[B", false, classLoader), Integer.TYPE);
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

