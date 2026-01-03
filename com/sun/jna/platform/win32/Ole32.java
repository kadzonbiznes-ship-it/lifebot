/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.Unknown;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface Ole32
extends StdCallLibrary {
    public static final Ole32 INSTANCE = Native.load("Ole32", Ole32.class, W32APIOptions.DEFAULT_OPTIONS);
    public static final int COINIT_APARTMENTTHREADED = 2;
    public static final int COINIT_MULTITHREADED = 0;
    public static final int COINIT_DISABLE_OLE1DDE = 4;
    public static final int COINIT_SPEED_OVER_MEMORY = 8;
    public static final int RPC_C_AUTHN_LEVEL_DEFAULT = 0;
    public static final int RPC_C_AUTHN_WINNT = 10;
    public static final int RPC_C_IMP_LEVEL_IMPERSONATE = 3;
    public static final int RPC_C_AUTHZ_NONE = 0;
    public static final int RPC_C_AUTHN_LEVEL_CALL = 3;
    public static final int EOAC_NONE = 0;

    public WinNT.HRESULT CoCreateGuid(Guid.GUID var1);

    public int StringFromGUID2(Guid.GUID var1, char[] var2, int var3);

    public WinNT.HRESULT IIDFromString(String var1, Guid.GUID var2);

    public WinNT.HRESULT CoInitialize(WinDef.LPVOID var1);

    public WinNT.HRESULT CoInitializeEx(Pointer var1, int var2);

    public WinNT.HRESULT CoInitializeSecurity(WinNT.SECURITY_DESCRIPTOR var1, int var2, Pointer var3, Pointer var4, int var5, int var6, Pointer var7, int var8, Pointer var9);

    public WinNT.HRESULT CoSetProxyBlanket(Unknown var1, int var2, int var3, WTypes.LPOLESTR var4, int var5, int var6, Pointer var7, int var8);

    public void CoUninitialize();

    public WinNT.HRESULT CoCreateInstance(Guid.GUID var1, Pointer var2, int var3, Guid.GUID var4, PointerByReference var5);

    public WinNT.HRESULT CLSIDFromProgID(String var1, Guid.CLSID.ByReference var2);

    public WinNT.HRESULT CLSIDFromString(String var1, Guid.CLSID.ByReference var2);

    public Pointer CoTaskMemAlloc(long var1);

    public Pointer CoTaskMemRealloc(Pointer var1, long var2);

    public void CoTaskMemFree(Pointer var1);

    public WinNT.HRESULT CoGetMalloc(WinDef.DWORD var1, PointerByReference var2);

    public WinNT.HRESULT GetRunningObjectTable(WinDef.DWORD var1, PointerByReference var2);

    public WinNT.HRESULT CreateBindCtx(WinDef.DWORD var1, PointerByReference var2);

    public boolean CoIsHandlerConnected(Pointer var1);

    public WinNT.HRESULT OleInitialize(Pointer var1);

    public void OleUninitialize();

    public WinNT.HRESULT OleFlushClipboard();

    public WinNT.HRESULT OleRun(Pointer var1);
}

