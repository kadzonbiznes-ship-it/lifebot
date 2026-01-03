/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.platform.win32.Guid$IID
 *  com.sun.jna.platform.win32.Guid$REFIID
 *  com.sun.jna.platform.win32.OaIdl$DISPID
 *  com.sun.jna.platform.win32.OaIdl$DISPIDByReference
 *  com.sun.jna.platform.win32.OaIdl$EXCEPINFO$ByReference
 *  com.sun.jna.platform.win32.OleAuto$DISPPARAMS$ByReference
 */
package com.sun.jna.platform.win32.COM;

import com.sun.jna.WString;
import com.sun.jna.platform.win32.COM.IUnknown;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.OaIdl;
import com.sun.jna.platform.win32.OleAuto;
import com.sun.jna.platform.win32.Variant;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public interface IDispatch
extends IUnknown {
    public static final Guid.IID IID_IDISPATCH = new Guid.IID("00020400-0000-0000-C000-000000000046");

    public WinNT.HRESULT GetTypeInfoCount(WinDef.UINTByReference var1);

    public WinNT.HRESULT GetTypeInfo(WinDef.UINT var1, WinDef.LCID var2, PointerByReference var3);

    public WinNT.HRESULT GetIDsOfNames(Guid.REFIID var1, WString[] var2, int var3, WinDef.LCID var4, OaIdl.DISPIDByReference var5);

    public WinNT.HRESULT Invoke(OaIdl.DISPID var1, Guid.REFIID var2, WinDef.LCID var3, WinDef.WORD var4, OleAuto.DISPPARAMS.ByReference var5, Variant.VARIANT.ByReference var6, OaIdl.EXCEPINFO.ByReference var7, IntByReference var8);
}

