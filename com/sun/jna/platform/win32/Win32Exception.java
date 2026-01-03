/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.platform.win32.W32Errors
 */
package com.sun.jna.platform.win32;

import com.sun.jna.LastErrorException;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.W32Errors;
import com.sun.jna.platform.win32.WinNT;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Win32Exception
extends LastErrorException {
    private static final long serialVersionUID = 1L;
    private WinNT.HRESULT _hr;
    private static Method addSuppressedMethod = null;

    public WinNT.HRESULT getHR() {
        return this._hr;
    }

    public Win32Exception(int code) {
        this(code, W32Errors.HRESULT_FROM_WIN32((int)code));
    }

    public Win32Exception(WinNT.HRESULT hr) {
        this(W32Errors.HRESULT_CODE((int)hr.intValue()), hr);
    }

    protected Win32Exception(int code, WinNT.HRESULT hr) {
        this(code, hr, Kernel32Util.formatMessage(hr));
    }

    protected Win32Exception(int code, WinNT.HRESULT hr, String msg) {
        super(code, msg);
        this._hr = hr;
    }

    void addSuppressedReflected(Throwable exception) {
        if (addSuppressedMethod == null) {
            return;
        }
        try {
            addSuppressedMethod.invoke((Object)this, exception);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException("Failed to call addSuppressedMethod", ex);
        }
    }

    static {
        try {
            addSuppressedMethod = Throwable.class.getMethod("addSuppressed", Throwable.class);
        }
        catch (NoSuchMethodException noSuchMethodException) {
        }
        catch (SecurityException ex) {
            Logger.getLogger(Win32Exception.class.getName()).log(Level.SEVERE, "Failed to initialize 'addSuppressed' method", ex);
        }
    }
}

