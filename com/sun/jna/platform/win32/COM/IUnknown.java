/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.platform.win32.Guid$IID
 *  com.sun.jna.platform.win32.Guid$REFIID
 */
package com.sun.jna.platform.win32.COM;

import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;

public interface IUnknown {
    public static final Guid.IID IID_IUNKNOWN = new Guid.IID("{00000000-0000-0000-C000-000000000046}");

    public WinNT.HRESULT QueryInterface(Guid.REFIID var1, PointerByReference var2);

    public int AddRef();

    public int Release();
}

