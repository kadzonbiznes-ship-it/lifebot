/*
 * Decompiled with CFR 0.152.
 */
package jdk.proxy1;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.OaIdl;
import com.sun.jna.platform.win32.OleAuto;
import com.sun.jna.platform.win32.Variant;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.PointerByReference;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

public final class $Proxy12
extends Proxy
implements OleAuto {
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
    private static final Method m25;
    private static final Method m26;
    private static final Method m27;
    private static final Method m28;
    private static final Method m29;
    private static final Method m30;
    private static final Method m31;
    private static final Method m32;

    public $Proxy12(InvocationHandler invocationHandler) {
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
    public final int VariantTimeToSystemTime(double d, WinBase.SYSTEMTIME sYSTEMTIME) {
        try {
            return (Integer)this.h.invoke(this, m3, new Object[]{d, sYSTEMTIME});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.UINT SafeArrayGetElemsize(OaIdl.SAFEARRAY sAFEARRAY) {
        try {
            return (WinDef.UINT)this.h.invoke(this, m4, new Object[]{sAFEARRAY});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT SafeArrayGetElement(OaIdl.SAFEARRAY sAFEARRAY, WinDef.LONG[] lONGArray, Pointer pointer) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m5, new Object[]{sAFEARRAY, lONGArray, pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT SafeArrayPtrOfIndex(OaIdl.SAFEARRAY sAFEARRAY, WinDef.LONG[] lONGArray, PointerByReference pointerByReference) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m6, new Object[]{sAFEARRAY, lONGArray, pointerByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT SafeArrayPutElement(OaIdl.SAFEARRAY sAFEARRAY, WinDef.LONG[] lONGArray, Pointer pointer) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m7, new Object[]{sAFEARRAY, lONGArray, pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int SystemTimeToVariantTime(WinBase.SYSTEMTIME sYSTEMTIME, DoubleByReference doubleByReference) {
        try {
            return (Integer)this.h.invoke(this, m8, new Object[]{sYSTEMTIME, doubleByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT SafeArrayAccessData(OaIdl.SAFEARRAY sAFEARRAY, PointerByReference pointerByReference) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m9, new Object[]{sAFEARRAY, pointerByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT SafeArrayUnaccessData(OaIdl.SAFEARRAY sAFEARRAY) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m10, new Object[]{sAFEARRAY});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT SafeArrayGetVartype(OaIdl.SAFEARRAY sAFEARRAY, WTypes.VARTYPEByReference vARTYPEByReference) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m11, new Object[]{sAFEARRAY, vARTYPEByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT VariantClear(Variant.VARIANT vARIANT) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m12, new Object[]{vARIANT});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WTypes.BSTR SysAllocString(String string) {
        try {
            return (WTypes.BSTR)this.h.invoke(this, m13, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void SysFreeString(WTypes.BSTR bSTR) {
        try {
            this.h.invoke(this, m14, new Object[]{bSTR});
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
    public final WinNT.HRESULT SafeArrayLock(OaIdl.SAFEARRAY sAFEARRAY) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m15, new Object[]{sAFEARRAY});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT VariantChangeType(Variant.VARIANT vARIANT, Variant.VARIANT vARIANT2, short s, WTypes.VARTYPE vARTYPE) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m16, new Object[]{vARIANT, vARIANT2, s, vARTYPE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT VariantChangeType(Variant.VARIANT.ByReference byReference, Variant.VARIANT.ByReference byReference2, short s, WTypes.VARTYPE vARTYPE) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m17, new Object[]{byReference, byReference2, s, vARTYPE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.UINT SafeArrayGetDim(OaIdl.SAFEARRAY sAFEARRAY) {
        try {
            return (WinDef.UINT)this.h.invoke(this, m18, new Object[]{sAFEARRAY});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT LoadRegTypeLib(Guid.GUID gUID, int n, int n2, WinDef.LCID lCID, PointerByReference pointerByReference) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m19, new Object[]{gUID, n, n2, lCID, pointerByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT GetActiveObject(Guid.GUID gUID, WinDef.PVOID pVOID, PointerByReference pointerByReference) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m20, new Object[]{gUID, pVOID, pointerByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int SysStringByteLen(WTypes.BSTR bSTR) {
        try {
            return (Integer)this.h.invoke(this, m21, new Object[]{bSTR});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final OaIdl.SAFEARRAY.ByReference SafeArrayCreate(WTypes.VARTYPE vARTYPE, WinDef.UINT uINT, OaIdl.SAFEARRAYBOUND[] sAFEARRAYBOUNDArray) {
        try {
            return (OaIdl.SAFEARRAY.ByReference)this.h.invoke(this, m22, new Object[]{vARTYPE, uINT, sAFEARRAYBOUNDArray});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT SafeArrayDestroy(OaIdl.SAFEARRAY sAFEARRAY) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m23, new Object[]{sAFEARRAY});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void VariantInit(Variant.VARIANT.ByReference byReference) {
        try {
            this.h.invoke(this, m24, new Object[]{byReference});
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
    public final void VariantInit(Variant.VARIANT vARIANT) {
        try {
            this.h.invoke(this, m25, new Object[]{vARIANT});
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
    public final WinNT.HRESULT LoadTypeLib(String string, PointerByReference pointerByReference) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m26, new Object[]{string, pointerByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT VariantCopy(Pointer pointer, Variant.VARIANT vARIANT) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m27, new Object[]{pointer, vARIANT});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT SafeArrayGetLBound(OaIdl.SAFEARRAY sAFEARRAY, WinDef.UINT uINT, WinDef.LONGByReference lONGByReference) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m28, new Object[]{sAFEARRAY, uINT, lONGByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT SafeArrayUnlock(OaIdl.SAFEARRAY sAFEARRAY) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m29, new Object[]{sAFEARRAY});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT SafeArrayRedim(OaIdl.SAFEARRAY sAFEARRAY, OaIdl.SAFEARRAYBOUND sAFEARRAYBOUND) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m30, new Object[]{sAFEARRAY, sAFEARRAYBOUND});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT SafeArrayGetUBound(OaIdl.SAFEARRAY sAFEARRAY, WinDef.UINT uINT, WinDef.LONGByReference lONGByReference) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m31, new Object[]{sAFEARRAY, uINT, lONGByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int SysStringLen(WTypes.BSTR bSTR) {
        try {
            return (Integer)this.h.invoke(this, m32, new Object[]{bSTR});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    static {
        ClassLoader classLoader = $Proxy12.class.getClassLoader();
        try {
            m0 = Class.forName("java.lang.Object", false, classLoader).getMethod("hashCode", new Class[0]);
            m1 = Class.forName("java.lang.Object", false, classLoader).getMethod("equals", Class.forName("java.lang.Object", false, classLoader));
            m2 = Class.forName("java.lang.Object", false, classLoader).getMethod("toString", new Class[0]);
            m3 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("VariantTimeToSystemTime", Double.TYPE, Class.forName("com.sun.jna.platform.win32.WinBase$SYSTEMTIME", false, classLoader));
            m4 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SafeArrayGetElemsize", Class.forName("com.sun.jna.platform.win32.OaIdl$SAFEARRAY", false, classLoader));
            m5 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SafeArrayGetElement", Class.forName("com.sun.jna.platform.win32.OaIdl$SAFEARRAY", false, classLoader), Class.forName("[Lcom.sun.jna.platform.win32.WinDef$LONG;", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m6 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SafeArrayPtrOfIndex", Class.forName("com.sun.jna.platform.win32.OaIdl$SAFEARRAY", false, classLoader), Class.forName("[Lcom.sun.jna.platform.win32.WinDef$LONG;", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m7 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SafeArrayPutElement", Class.forName("com.sun.jna.platform.win32.OaIdl$SAFEARRAY", false, classLoader), Class.forName("[Lcom.sun.jna.platform.win32.WinDef$LONG;", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m8 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SystemTimeToVariantTime", Class.forName("com.sun.jna.platform.win32.WinBase$SYSTEMTIME", false, classLoader), Class.forName("com.sun.jna.ptr.DoubleByReference", false, classLoader));
            m9 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SafeArrayAccessData", Class.forName("com.sun.jna.platform.win32.OaIdl$SAFEARRAY", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m10 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SafeArrayUnaccessData", Class.forName("com.sun.jna.platform.win32.OaIdl$SAFEARRAY", false, classLoader));
            m11 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SafeArrayGetVartype", Class.forName("com.sun.jna.platform.win32.OaIdl$SAFEARRAY", false, classLoader), Class.forName("com.sun.jna.platform.win32.WTypes$VARTYPEByReference", false, classLoader));
            m12 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("VariantClear", Class.forName("com.sun.jna.platform.win32.Variant$VARIANT", false, classLoader));
            m13 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SysAllocString", Class.forName("java.lang.String", false, classLoader));
            m14 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SysFreeString", Class.forName("com.sun.jna.platform.win32.WTypes$BSTR", false, classLoader));
            m15 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SafeArrayLock", Class.forName("com.sun.jna.platform.win32.OaIdl$SAFEARRAY", false, classLoader));
            m16 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("VariantChangeType", Class.forName("com.sun.jna.platform.win32.Variant$VARIANT", false, classLoader), Class.forName("com.sun.jna.platform.win32.Variant$VARIANT", false, classLoader), Short.TYPE, Class.forName("com.sun.jna.platform.win32.WTypes$VARTYPE", false, classLoader));
            m17 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("VariantChangeType", Class.forName("com.sun.jna.platform.win32.Variant$VARIANT$ByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.Variant$VARIANT$ByReference", false, classLoader), Short.TYPE, Class.forName("com.sun.jna.platform.win32.WTypes$VARTYPE", false, classLoader));
            m18 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SafeArrayGetDim", Class.forName("com.sun.jna.platform.win32.OaIdl$SAFEARRAY", false, classLoader));
            m19 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("LoadRegTypeLib", Class.forName("com.sun.jna.platform.win32.Guid$GUID", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$LCID", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m20 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("GetActiveObject", Class.forName("com.sun.jna.platform.win32.Guid$GUID", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$PVOID", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m21 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SysStringByteLen", Class.forName("com.sun.jna.platform.win32.WTypes$BSTR", false, classLoader));
            m22 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SafeArrayCreate", Class.forName("com.sun.jna.platform.win32.WTypes$VARTYPE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$UINT", false, classLoader), Class.forName("[Lcom.sun.jna.platform.win32.OaIdl$SAFEARRAYBOUND;", false, classLoader));
            m23 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SafeArrayDestroy", Class.forName("com.sun.jna.platform.win32.OaIdl$SAFEARRAY", false, classLoader));
            m24 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("VariantInit", Class.forName("com.sun.jna.platform.win32.Variant$VARIANT$ByReference", false, classLoader));
            m25 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("VariantInit", Class.forName("com.sun.jna.platform.win32.Variant$VARIANT", false, classLoader));
            m26 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("LoadTypeLib", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m27 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("VariantCopy", Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.platform.win32.Variant$VARIANT", false, classLoader));
            m28 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SafeArrayGetLBound", Class.forName("com.sun.jna.platform.win32.OaIdl$SAFEARRAY", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$UINT", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$LONGByReference", false, classLoader));
            m29 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SafeArrayUnlock", Class.forName("com.sun.jna.platform.win32.OaIdl$SAFEARRAY", false, classLoader));
            m30 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SafeArrayRedim", Class.forName("com.sun.jna.platform.win32.OaIdl$SAFEARRAY", false, classLoader), Class.forName("com.sun.jna.platform.win32.OaIdl$SAFEARRAYBOUND", false, classLoader));
            m31 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SafeArrayGetUBound", Class.forName("com.sun.jna.platform.win32.OaIdl$SAFEARRAY", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$UINT", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$LONGByReference", false, classLoader));
            m32 = Class.forName("com.sun.jna.platform.win32.OleAuto", false, classLoader).getMethod("SysStringLen", Class.forName("com.sun.jna.platform.win32.WTypes$BSTR", false, classLoader));
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

