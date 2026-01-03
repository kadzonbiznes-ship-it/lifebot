/*
 * Decompiled with CFR 0.152.
 */
package jdk.proxy1;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import org.pbrands.windows.User32Extended;

public final class $Proxy16
extends Proxy
implements User32Extended {
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
    private static final Method m33;
    private static final Method m34;
    private static final Method m35;
    private static final Method m36;
    private static final Method m37;
    private static final Method m38;
    private static final Method m39;
    private static final Method m40;
    private static final Method m41;
    private static final Method m42;
    private static final Method m43;
    private static final Method m44;
    private static final Method m45;
    private static final Method m46;
    private static final Method m47;
    private static final Method m48;
    private static final Method m49;
    private static final Method m50;
    private static final Method m51;
    private static final Method m52;
    private static final Method m53;
    private static final Method m54;
    private static final Method m55;
    private static final Method m56;
    private static final Method m57;
    private static final Method m58;
    private static final Method m59;
    private static final Method m60;
    private static final Method m61;
    private static final Method m62;
    private static final Method m63;
    private static final Method m64;
    private static final Method m65;
    private static final Method m66;
    private static final Method m67;
    private static final Method m68;
    private static final Method m69;
    private static final Method m70;
    private static final Method m71;
    private static final Method m72;
    private static final Method m73;
    private static final Method m74;
    private static final Method m75;
    private static final Method m76;
    private static final Method m77;
    private static final Method m78;
    private static final Method m79;
    private static final Method m80;
    private static final Method m81;
    private static final Method m82;
    private static final Method m83;
    private static final Method m84;
    private static final Method m85;
    private static final Method m86;
    private static final Method m87;
    private static final Method m88;
    private static final Method m89;
    private static final Method m90;
    private static final Method m91;
    private static final Method m92;
    private static final Method m93;
    private static final Method m94;
    private static final Method m95;
    private static final Method m96;
    private static final Method m97;
    private static final Method m98;
    private static final Method m99;
    private static final Method m100;
    private static final Method m101;
    private static final Method m102;
    private static final Method m103;
    private static final Method m104;
    private static final Method m105;
    private static final Method m106;
    private static final Method m107;
    private static final Method m108;
    private static final Method m109;
    private static final Method m110;
    private static final Method m111;
    private static final Method m112;
    private static final Method m113;
    private static final Method m114;
    private static final Method m115;
    private static final Method m116;
    private static final Method m117;

    public $Proxy16(InvocationHandler invocationHandler) {
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
    public final WinDef.HWND GetForegroundWindow() {
        try {
            return (WinDef.HWND)this.h.invoke(this, m3, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean ClientToScreen(WinDef.HWND hWND, WinDef.POINT pOINT) {
        try {
            return (Boolean)this.h.invoke(this, m4, new Object[]{hWND, pOINT});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int MapVirtualKeyA(int n, int n2) {
        try {
            return (Integer)this.h.invoke(this, m5, new Object[]{n, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetProcessDPIAware() {
        try {
            return (Boolean)this.h.invoke(this, m6, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetWindowText(WinDef.HWND hWND, char[] cArray, int n) {
        try {
            return (Integer)this.h.invoke(this, m7, new Object[]{hWND, cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.LRESULT CallNextHookEx(WinUser.HHOOK hHOOK, int n, WinDef.WPARAM wPARAM, Pointer pointer) {
        try {
            return (WinDef.LRESULT)this.h.invoke(this, m8, new Object[]{hHOOK, n, wPARAM, pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetWindowRect(WinDef.HWND hWND, WinDef.RECT rECT) {
        try {
            return (Boolean)this.h.invoke(this, m9, new Object[]{hWND, rECT});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetClientRect(WinDef.HWND hWND, WinDef.RECT rECT) {
        try {
            return (Boolean)this.h.invoke(this, m10, new Object[]{hWND, rECT});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.HWND FindWindow(String string, String string2) {
        try {
            return (WinDef.HWND)this.h.invoke(this, m11, new Object[]{string, string2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final short VkKeyScanA(char c) {
        try {
            return (Short)this.h.invoke(this, m12, new Object[]{Character.valueOf(c)});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean UnregisterDeviceNotification(WinUser.HDEVNOTIFY hDEVNOTIFY) {
        try {
            return (Boolean)this.h.invoke(this, m13, new Object[]{hDEVNOTIFY});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetCursorPos(long l, long l2) {
        try {
            return (Boolean)this.h.invoke(this, m14, new Object[]{l, l2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean TranslateMessage(WinUser.MSG mSG) {
        try {
            return (Boolean)this.h.invoke(this, m15, new Object[]{mSG});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.HWND FindWindowEx(WinDef.HWND hWND, WinDef.HWND hWND2, String string, String string2) {
        try {
            return (WinDef.HWND)this.h.invoke(this, m16, new Object[]{hWND, hWND2, string, string2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetLastInputInfo(WinUser.LASTINPUTINFO lASTINPUTINFO) {
        try {
            return (Boolean)this.h.invoke(this, m17, new Object[]{lASTINPUTINFO});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean UnregisterClass(String string, WinDef.HINSTANCE hINSTANCE) {
        try {
            return (Boolean)this.h.invoke(this, m18, new Object[]{string, hINSTANCE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.HWND GetAncestor(WinDef.HWND hWND, int n) {
        try {
            return (WinDef.HWND)this.h.invoke(this, m19, new Object[]{hWND, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.BOOL GetMonitorInfo(WinUser.HMONITOR hMONITOR, WinUser.MONITORINFO mONITORINFO) {
        try {
            return (WinDef.BOOL)this.h.invoke(this, m20, new Object[]{hMONITOR, mONITORINFO});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.BOOL GetMonitorInfo(WinUser.HMONITOR hMONITOR, WinUser.MONITORINFOEX mONITORINFOEX) {
        try {
            return (WinDef.BOOL)this.h.invoke(this, m21, new Object[]{hMONITOR, mONITORINFOEX});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetCursorPos(WinDef.POINT pOINT) {
        try {
            return (Boolean)this.h.invoke(this, m22, new Object[]{pOINT});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.DWORD WaitForInputIdle(WinNT.HANDLE hANDLE, WinDef.DWORD dWORD) {
        try {
            return (WinDef.DWORD)this.h.invoke(this, m23, new Object[]{hANDLE, dWORD});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.HWND CreateWindowEx(int n, String string, String string2, int n2, int n3, int n4, int n5, int n6, WinDef.HWND hWND, WinDef.HMENU hMENU, WinDef.HINSTANCE hINSTANCE, WinDef.LPVOID lPVOID) {
        try {
            return (WinDef.HWND)this.h.invoke(this, m24, new Object[]{n, string, string2, n2, n3, n4, n5, n6, hWND, hMENU, hINSTANCE, lPVOID});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean UnhookWinEvent(WinNT.HANDLE hANDLE) {
        try {
            return (Boolean)this.h.invoke(this, m25, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetClassInfoEx(WinDef.HINSTANCE hINSTANCE, String string, WinUser.WNDCLASSEX wNDCLASSEX) {
        try {
            return (Boolean)this.h.invoke(this, m26, new Object[]{hINSTANCE, string, wNDCLASSEX});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinUser.HMONITOR MonitorFromPoint(WinDef.POINT.ByValue byValue, int n) {
        try {
            return (WinUser.HMONITOR)this.h.invoke(this, m27, new Object[]{byValue, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.LRESULT SendMessageTimeout(WinDef.HWND hWND, int n, WinDef.WPARAM wPARAM, WinDef.LPARAM lPARAM, int n2, int n3, WinDef.DWORDByReference dWORDByReference) {
        try {
            return (WinDef.LRESULT)this.h.invoke(this, m28, new Object[]{hWND, n, wPARAM, lPARAM, n2, n3, dWORDByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetClassLong(WinDef.HWND hWND, int n) {
        try {
            return (Integer)this.h.invoke(this, m29, new Object[]{hWND, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.HWND GetActiveWindow() {
        try {
            return (WinDef.HWND)this.h.invoke(this, m30, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.LRESULT DispatchMessage(WinUser.MSG mSG) {
        try {
            return (WinDef.LRESULT)this.h.invoke(this, m31, new Object[]{mSG});
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
            return (WinDef.HKL)this.h.invoke(this, m32, new Object[]{n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final short VkKeyScanExA(byte by, WinDef.HKL hKL) {
        try {
            return (Short)this.h.invoke(this, m33, new Object[]{by, hKL});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinUser.HMONITOR MonitorFromWindow(WinDef.HWND hWND, int n) {
        try {
            return (WinUser.HMONITOR)this.h.invoke(this, m34, new Object[]{hWND, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean RegisterHotKey(WinDef.HWND hWND, int n, int n2, int n3) {
        try {
            return (Boolean)this.h.invoke(this, m35, new Object[]{hWND, n, n2, n3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean DestroyWindow(WinDef.HWND hWND) {
        try {
            return (Boolean)this.h.invoke(this, m36, new Object[]{hWND});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.BOOL GetWindowPlacement(WinDef.HWND hWND, WinUser.WINDOWPLACEMENT wINDOWPLACEMENT) {
        try {
            return (WinDef.BOOL)this.h.invoke(this, m37, new Object[]{hWND, wINDOWPLACEMENT});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.BOOL AdjustWindowRect(WinDef.RECT rECT, WinDef.DWORD dWORD, WinDef.BOOL bOOL) {
        try {
            return (WinDef.BOOL)this.h.invoke(this, m38, new Object[]{rECT, dWORD, bOOL});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetIconInfo(WinDef.HICON hICON, WinGDI.ICONINFO iCONINFO) {
        try {
            return (Boolean)this.h.invoke(this, m39, new Object[]{hICON, iCONINFO});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean IsWindowVisible(WinDef.HWND hWND) {
        try {
            return (Boolean)this.h.invoke(this, m40, new Object[]{hWND});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean PrintWindow(WinDef.HWND hWND, WinDef.HDC hDC, int n) {
        try {
            return (Boolean)this.h.invoke(this, m41, new Object[]{hWND, hDC, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean IsWindowEnabled(WinDef.HWND hWND) {
        try {
            return (Boolean)this.h.invoke(this, m42, new Object[]{hWND});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE SetWinEventHook(int n, int n2, WinDef.HMODULE hMODULE, WinUser.WinEventProc winEventProc, int n3, int n4, int n5) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m43, new Object[]{n, n2, hMODULE, winEventProc, n3, n4, n5});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.LRESULT SendMessage(WinDef.HWND hWND, int n, WinDef.WPARAM wPARAM, WinDef.LPARAM lPARAM) {
        try {
            return (WinDef.LRESULT)this.h.invoke(this, m44, new Object[]{hWND, n, wPARAM, lPARAM});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final short VkKeyScanExW(char c, WinDef.HKL hKL) {
        try {
            return (Short)this.h.invoke(this, m45, new Object[]{Character.valueOf(c), hKL});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int MapVirtualKeyEx(int n, int n2, WinDef.HKL hKL) {
        try {
            return (Integer)this.h.invoke(this, m46, new Object[]{n, n2, hKL});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean CloseWindow(WinDef.HWND hWND) {
        try {
            return (Boolean)this.h.invoke(this, m47, new Object[]{hWND});
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
            return (Integer)this.h.invoke(this, m48, new Object[]{n, n2, byArray, cArray, n3, n4, hKL});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int PostThreadMessage(int n, int n2, WinDef.WPARAM wPARAM, WinDef.LPARAM lPARAM) {
        try {
            return (Integer)this.h.invoke(this, m49, new Object[]{n, n2, wPARAM, lPARAM});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean RedrawWindow(WinDef.HWND hWND, WinDef.RECT rECT, WinDef.HRGN hRGN, WinDef.DWORD dWORD) {
        try {
            return (Boolean)this.h.invoke(this, m50, new Object[]{hWND, rECT, hRGN, dWORD});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.ATOM RegisterClassEx(WinUser.WNDCLASSEX wNDCLASSEX) {
        try {
            return (WinDef.ATOM)this.h.invoke(this, m51, new Object[]{wNDCLASSEX});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean AttachThreadInput(WinDef.DWORD dWORD, WinDef.DWORD dWORD2, boolean bl) {
        try {
            return (Boolean)this.h.invoke(this, m52, new Object[]{dWORD, dWORD2, bl});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean InvalidateRect(WinDef.HWND hWND, WinDef.RECT rECT, boolean bl) {
        try {
            return (Boolean)this.h.invoke(this, m53, new Object[]{hWND, rECT, bl});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetSystemMetrics(int n) {
        try {
            return (Integer)this.h.invoke(this, m54, new Object[]{n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinUser.HHOOK SetWindowsHookEx(int n, WinUser.HOOKPROC hOOKPROC, WinDef.HINSTANCE hINSTANCE, int n2) {
        try {
            return (WinUser.HHOOK)this.h.invoke(this, m55, new Object[]{n, hOOKPROC, hINSTANCE, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void PostMessage(WinDef.HWND hWND, int n, WinDef.WPARAM wPARAM, WinDef.LPARAM lPARAM) {
        try {
            this.h.invoke(this, m56, new Object[]{hWND, n, wPARAM, lPARAM});
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
    public final void PostQuitMessage(int n) {
        try {
            this.h.invoke(this, m57, new Object[]{n});
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
    public final boolean PeekMessage(WinUser.MSG mSG, WinDef.HWND hWND, int n, int n2, int n3) {
        try {
            return (Boolean)this.h.invoke(this, m58, new Object[]{mSG, hWND, n, n2, n3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetWindowPos(WinDef.HWND hWND, WinDef.HWND hWND2, int n, int n2, int n3, int n4, int n5) {
        try {
            return (Boolean)this.h.invoke(this, m59, new Object[]{hWND, hWND2, n, n2, n3, n4, n5});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean UnregisterHotKey(Pointer pointer, int n) {
        try {
            return (Boolean)this.h.invoke(this, m60, new Object[]{pointer, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.LRESULT CallWindowProc(Pointer pointer, WinDef.HWND hWND, int n, WinDef.WPARAM wPARAM, WinDef.LPARAM lPARAM) {
        try {
            return (WinDef.LRESULT)this.h.invoke(this, m61, new Object[]{pointer, hWND, n, wPARAM, lPARAM});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.LRESULT DefWindowProc(WinDef.HWND hWND, int n, WinDef.WPARAM wPARAM, WinDef.LPARAM lPARAM) {
        try {
            return (WinDef.LRESULT)this.h.invoke(this, m62, new Object[]{hWND, n, wPARAM, lPARAM});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.BOOL SetWindowPlacement(WinDef.HWND hWND, WinUser.WINDOWPLACEMENT wINDOWPLACEMENT) {
        try {
            return (WinDef.BOOL)this.h.invoke(this, m63, new Object[]{hWND, wINDOWPLACEMENT});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.BOOL ExitWindowsEx(WinDef.UINT uINT, WinDef.DWORD dWORD) {
        try {
            return (WinDef.BOOL)this.h.invoke(this, m64, new Object[]{uINT, dWORD});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.BOOL AdjustWindowRectEx(WinDef.RECT rECT, WinDef.DWORD dWORD, WinDef.BOOL bOOL, WinDef.DWORD dWORD2) {
        try {
            return (WinDef.BOOL)this.h.invoke(this, m65, new Object[]{rECT, dWORD, bOOL, dWORD2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.BOOL LockWorkStation() {
        try {
            return (WinDef.BOOL)this.h.invoke(this, m66, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean UpdateWindow(WinDef.HWND hWND) {
        try {
            return (Boolean)this.h.invoke(this, m67, new Object[]{hWND});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinUser.HMONITOR MonitorFromRect(WinDef.RECT rECT, int n) {
        try {
            return (WinUser.HMONITOR)this.h.invoke(this, m68, new Object[]{rECT, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final BaseTSD.ULONG_PTR GetClassLongPtr(WinDef.HWND hWND, int n) {
        try {
            return (BaseTSD.ULONG_PTR)this.h.invoke(this, m69, new Object[]{hWND, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.HWND GetDesktopWindow() {
        try {
            return (WinDef.HWND)this.h.invoke(this, m70, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetWindowThreadProcessId(WinDef.HWND hWND, IntByReference intByReference) {
        try {
            return (Integer)this.h.invoke(this, m71, new Object[]{hWND, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetWindowTextLength(WinDef.HWND hWND) {
        try {
            return (Integer)this.h.invoke(this, m72, new Object[]{hWND});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean UpdateLayeredWindow(WinDef.HWND hWND, WinDef.HDC hDC, WinDef.POINT pOINT, WinUser.SIZE sIZE, WinDef.HDC hDC2, WinDef.POINT pOINT2, int n, WinUser.BLENDFUNCTION bLENDFUNCTION, int n2) {
        try {
            return (Boolean)this.h.invoke(this, m73, new Object[]{hWND, hDC, pOINT, sIZE, hDC2, pOINT2, n, bLENDFUNCTION, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean UnhookWindowsHookEx(WinUser.HHOOK hHOOK) {
        try {
            return (Boolean)this.h.invoke(this, m74, new Object[]{hHOOK});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinUser.HDEVNOTIFY RegisterDeviceNotification(WinNT.HANDLE hANDLE, Structure structure, int n) {
        try {
            return (WinUser.HDEVNOTIFY)this.h.invoke(this, m75, new Object[]{hANDLE, structure, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetWindowModuleFileName(WinDef.HWND hWND, char[] cArray, int n) {
        try {
            return (Integer)this.h.invoke(this, m76, new Object[]{hWND, cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetLayeredWindowAttributes(WinDef.HWND hWND, int n, byte by, int n2) {
        try {
            return (Boolean)this.h.invoke(this, m77, new Object[]{hWND, n, by, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetLayeredWindowAttributes(WinDef.HWND hWND, IntByReference intByReference, ByteByReference byteByReference, IntByReference intByReference2) {
        try {
            return (Boolean)this.h.invoke(this, m78, new Object[]{hWND, intByReference, byteByReference, intByReference2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetForegroundWindow(WinDef.HWND hWND) {
        try {
            return (Boolean)this.h.invoke(this, m79, new Object[]{hWND});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegisterWindowMessage(String string) {
        try {
            return (Integer)this.h.invoke(this, m80, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.BOOL EnumDisplayMonitors(WinDef.HDC hDC, WinDef.RECT rECT, WinUser.MONITORENUMPROC mONITORENUMPROC, WinDef.LPARAM lPARAM) {
        try {
            return (WinDef.BOOL)this.h.invoke(this, m81, new Object[]{hDC, rECT, mONITORENUMPROC, lPARAM});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetWindowInfo(WinDef.HWND hWND, WinUser.WINDOWINFO wINDOWINFO) {
        try {
            return (Boolean)this.h.invoke(this, m82, new Object[]{hWND, wINDOWINFO});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetGUIThreadInfo(int n, WinUser.GUITHREADINFO gUITHREADINFO) {
        try {
            return (Boolean)this.h.invoke(this, m83, new Object[]{n, gUITHREADINFO});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean EnumWindows(WinUser.WNDENUMPROC wNDENUMPROC, Pointer pointer) {
        try {
            return (Boolean)this.h.invoke(this, m84, new Object[]{wNDENUMPROC, pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean EnumThreadWindows(int n, WinUser.WNDENUMPROC wNDENUMPROC, Pointer pointer) {
        try {
            return (Boolean)this.h.invoke(this, m85, new Object[]{n, wNDENUMPROC, pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetWindowLong(WinDef.HWND hWND, int n) {
        try {
            return (Integer)this.h.invoke(this, m86, new Object[]{hWND, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int SetWindowLong(WinDef.HWND hWND, int n, int n2) {
        try {
            return (Integer)this.h.invoke(this, m87, new Object[]{hWND, n, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer SetWindowLongPtr(WinDef.HWND hWND, int n, Pointer pointer) {
        try {
            return (Pointer)this.h.invoke(this, m88, new Object[]{hWND, n, pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int SetWindowRgn(WinDef.HWND hWND, WinDef.HRGN hRGN, boolean bl) {
        try {
            return (Integer)this.h.invoke(this, m89, new Object[]{hWND, hRGN, bl});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean EnumChildWindows(WinDef.HWND hWND, WinUser.WNDENUMPROC wNDENUMPROC, Pointer pointer) {
        try {
            return (Boolean)this.h.invoke(this, m90, new Object[]{hWND, wNDENUMPROC, pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean FlashWindowEx(WinUser.FLASHWINFO fLASHWINFO) {
        try {
            return (Boolean)this.h.invoke(this, m91, new Object[]{fLASHWINFO});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean BringWindowToTop(WinDef.HWND hWND) {
        try {
            return (Boolean)this.h.invoke(this, m92, new Object[]{hWND});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean DestroyIcon(WinDef.HICON hICON) {
        try {
            return (Boolean)this.h.invoke(this, m93, new Object[]{hICON});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final BaseTSD.LONG_PTR GetWindowLongPtr(WinDef.HWND hWND, int n) {
        try {
            return (BaseTSD.LONG_PTR)this.h.invoke(this, m94, new Object[]{hWND, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetKeyboardState(byte[] byArray) {
        try {
            return (Boolean)this.h.invoke(this, m95, new Object[]{byArray});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetClassName(WinDef.HWND hWND, char[] cArray, int n) {
        try {
            return (Integer)this.h.invoke(this, m96, new Object[]{hWND, cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final short GetAsyncKeyState(int n) {
        try {
            return (Short)this.h.invoke(this, m97, new Object[]{n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.LRESULT CallNextHookEx(WinUser.HHOOK hHOOK, int n, WinDef.WPARAM wPARAM, WinDef.LPARAM lPARAM) {
        try {
            return (WinDef.LRESULT)this.h.invoke(this, m98, new Object[]{hHOOK, n, wPARAM, lPARAM});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegisterClipboardFormat(String string) {
        try {
            return (Integer)this.h.invoke(this, m99, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetRawInputDeviceList(WinUser.RAWINPUTDEVICELIST[] rAWINPUTDEVICELISTArray, IntByReference intByReference, int n) {
        try {
            return (Integer)this.h.invoke(this, m100, new Object[]{rAWINPUTDEVICELISTArray, intByReference, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetKeyboardLayoutList(int n, WinDef.HKL[] hKLArray) {
        try {
            return (Integer)this.h.invoke(this, m101, new Object[]{n, hKLArray});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetKeyboardLayoutName(char[] cArray) {
        try {
            return (Boolean)this.h.invoke(this, m102, new Object[]{cArray});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.HWND SetFocus(WinDef.HWND hWND) {
        try {
            return (WinDef.HWND)this.h.invoke(this, m103, new Object[]{hWND});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean IsWindow(WinDef.HWND hWND) {
        try {
            return (Boolean)this.h.invoke(this, m104, new Object[]{hWND});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean ShowWindow(WinDef.HWND hWND, int n) {
        try {
            return (Boolean)this.h.invoke(this, m105, new Object[]{hWND, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.HWND GetParent(WinDef.HWND hWND) {
        try {
            return (WinDef.HWND)this.h.invoke(this, m106, new Object[]{hWND});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.HICON CopyIcon(WinDef.HICON hICON) {
        try {
            return (WinDef.HICON)this.h.invoke(this, m107, new Object[]{hICON});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.HWND GetWindow(WinDef.HWND hWND, WinDef.DWORD dWORD) {
        try {
            return (WinDef.HWND)this.h.invoke(this, m108, new Object[]{hWND, dWORD});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int LoadString(WinDef.HINSTANCE hINSTANCE, int n, Pointer pointer, int n2) {
        try {
            return (Integer)this.h.invoke(this, m109, new Object[]{hINSTANCE, n, pointer, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.DWORD SendInput(WinDef.DWORD dWORD, WinUser.INPUT[] iNPUTArray, int n) {
        try {
            return (WinDef.DWORD)this.h.invoke(this, m110, new Object[]{dWORD, iNPUTArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.HDC GetDC(WinDef.HWND hWND) {
        try {
            return (WinDef.HDC)this.h.invoke(this, m111, new Object[]{hWND});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int ReleaseDC(WinDef.HWND hWND, WinDef.HDC hDC) {
        try {
            return (Integer)this.h.invoke(this, m112, new Object[]{hWND, hDC});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetMessage(WinUser.MSG mSG, WinDef.HWND hWND, int n, int n2) {
        try {
            return (Integer)this.h.invoke(this, m113, new Object[]{mSG, hWND, n, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.HICON LoadIcon(WinDef.HINSTANCE hINSTANCE, String string) {
        try {
            return (WinDef.HICON)this.h.invoke(this, m114, new Object[]{hINSTANCE, string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean MoveWindow(WinDef.HWND hWND, int n, int n2, int n3, int n4, boolean bl) {
        try {
            return (Boolean)this.h.invoke(this, m115, new Object[]{hWND, n, n2, n3, n4, bl});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.HWND SetParent(WinDef.HWND hWND, WinDef.HWND hWND2) {
        try {
            return (WinDef.HWND)this.h.invoke(this, m116, new Object[]{hWND, hWND2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE LoadImage(WinDef.HINSTANCE hINSTANCE, String string, int n, int n2, int n3, int n4) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m117, new Object[]{hINSTANCE, string, n, n2, n3, n4});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    static {
        ClassLoader classLoader = $Proxy16.class.getClassLoader();
        try {
            m0 = Class.forName("java.lang.Object", false, classLoader).getMethod("hashCode", new Class[0]);
            m1 = Class.forName("java.lang.Object", false, classLoader).getMethod("equals", Class.forName("java.lang.Object", false, classLoader));
            m2 = Class.forName("java.lang.Object", false, classLoader).getMethod("toString", new Class[0]);
            m3 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetForegroundWindow", new Class[0]);
            m4 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("ClientToScreen", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$POINT", false, classLoader));
            m5 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("MapVirtualKeyA", Integer.TYPE, Integer.TYPE);
            m6 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("SetProcessDPIAware", new Class[0]);
            m7 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetWindowText", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE);
            m8 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("CallNextHookEx", Class.forName("com.sun.jna.platform.win32.WinUser$HHOOK", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$WPARAM", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m9 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetWindowRect", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$RECT", false, classLoader));
            m10 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetClientRect", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$RECT", false, classLoader));
            m11 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("FindWindow", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m12 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("VkKeyScanA", Character.TYPE);
            m13 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("UnregisterDeviceNotification", Class.forName("com.sun.jna.platform.win32.WinUser$HDEVNOTIFY", false, classLoader));
            m14 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("SetCursorPos", Long.TYPE, Long.TYPE);
            m15 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("TranslateMessage", Class.forName("com.sun.jna.platform.win32.WinUser$MSG", false, classLoader));
            m16 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("FindWindowEx", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m17 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetLastInputInfo", Class.forName("com.sun.jna.platform.win32.WinUser$LASTINPUTINFO", false, classLoader));
            m18 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("UnregisterClass", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$HINSTANCE", false, classLoader));
            m19 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetAncestor", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE);
            m20 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetMonitorInfo", Class.forName("com.sun.jna.platform.win32.WinUser$HMONITOR", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinUser$MONITORINFO", false, classLoader));
            m21 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetMonitorInfo", Class.forName("com.sun.jna.platform.win32.WinUser$HMONITOR", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinUser$MONITORINFOEX", false, classLoader));
            m22 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetCursorPos", Class.forName("com.sun.jna.platform.win32.WinDef$POINT", false, classLoader));
            m23 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("WaitForInputIdle", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader));
            m24 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("CreateWindowEx", Integer.TYPE, Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$HMENU", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$HINSTANCE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$LPVOID", false, classLoader));
            m25 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("UnhookWinEvent", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m26 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetClassInfoEx", Class.forName("com.sun.jna.platform.win32.WinDef$HINSTANCE", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinUser$WNDCLASSEX", false, classLoader));
            m27 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("MonitorFromPoint", Class.forName("com.sun.jna.platform.win32.WinDef$POINT$ByValue", false, classLoader), Integer.TYPE);
            m28 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("SendMessageTimeout", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$WPARAM", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$LPARAM", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$DWORDByReference", false, classLoader));
            m29 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetClassLong", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE);
            m30 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetActiveWindow", new Class[0]);
            m31 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("DispatchMessage", Class.forName("com.sun.jna.platform.win32.WinUser$MSG", false, classLoader));
            m32 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetKeyboardLayout", Integer.TYPE);
            m33 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("VkKeyScanExA", Byte.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$HKL", false, classLoader));
            m34 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("MonitorFromWindow", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE);
            m35 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("RegisterHotKey", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE, Integer.TYPE, Integer.TYPE);
            m36 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("DestroyWindow", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader));
            m37 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetWindowPlacement", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinUser$WINDOWPLACEMENT", false, classLoader));
            m38 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("AdjustWindowRect", Class.forName("com.sun.jna.platform.win32.WinDef$RECT", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$BOOL", false, classLoader));
            m39 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetIconInfo", Class.forName("com.sun.jna.platform.win32.WinDef$HICON", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinGDI$ICONINFO", false, classLoader));
            m40 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("IsWindowVisible", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader));
            m41 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("PrintWindow", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$HDC", false, classLoader), Integer.TYPE);
            m42 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("IsWindowEnabled", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader));
            m43 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("SetWinEventHook", Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$HMODULE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinUser$WinEventProc", false, classLoader), Integer.TYPE, Integer.TYPE, Integer.TYPE);
            m44 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("SendMessage", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$WPARAM", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$LPARAM", false, classLoader));
            m45 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("VkKeyScanExW", Character.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$HKL", false, classLoader));
            m46 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("MapVirtualKeyEx", Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$HKL", false, classLoader));
            m47 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("CloseWindow", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader));
            m48 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("ToUnicodeEx", Integer.TYPE, Integer.TYPE, Class.forName("[B", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$HKL", false, classLoader));
            m49 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("PostThreadMessage", Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$WPARAM", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$LPARAM", false, classLoader));
            m50 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("RedrawWindow", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$RECT", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$HRGN", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader));
            m51 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("RegisterClassEx", Class.forName("com.sun.jna.platform.win32.WinUser$WNDCLASSEX", false, classLoader));
            m52 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("AttachThreadInput", Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader), Boolean.TYPE);
            m53 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("InvalidateRect", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$RECT", false, classLoader), Boolean.TYPE);
            m54 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetSystemMetrics", Integer.TYPE);
            m55 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("SetWindowsHookEx", Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinUser$HOOKPROC", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$HINSTANCE", false, classLoader), Integer.TYPE);
            m56 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("PostMessage", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$WPARAM", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$LPARAM", false, classLoader));
            m57 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("PostQuitMessage", Integer.TYPE);
            m58 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("PeekMessage", Class.forName("com.sun.jna.platform.win32.WinUser$MSG", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE, Integer.TYPE, Integer.TYPE);
            m59 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("SetWindowPos", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            m60 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("UnregisterHotKey", Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE);
            m61 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("CallWindowProc", Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$WPARAM", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$LPARAM", false, classLoader));
            m62 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("DefWindowProc", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$WPARAM", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$LPARAM", false, classLoader));
            m63 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("SetWindowPlacement", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinUser$WINDOWPLACEMENT", false, classLoader));
            m64 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("ExitWindowsEx", Class.forName("com.sun.jna.platform.win32.WinDef$UINT", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader));
            m65 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("AdjustWindowRectEx", Class.forName("com.sun.jna.platform.win32.WinDef$RECT", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$BOOL", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader));
            m66 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("LockWorkStation", new Class[0]);
            m67 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("UpdateWindow", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader));
            m68 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("MonitorFromRect", Class.forName("com.sun.jna.platform.win32.WinDef$RECT", false, classLoader), Integer.TYPE);
            m69 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetClassLongPtr", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE);
            m70 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetDesktopWindow", new Class[0]);
            m71 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetWindowThreadProcessId", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m72 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetWindowTextLength", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader));
            m73 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("UpdateLayeredWindow", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$HDC", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$POINT", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinUser$SIZE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$HDC", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$POINT", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinUser$BLENDFUNCTION", false, classLoader), Integer.TYPE);
            m74 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("UnhookWindowsHookEx", Class.forName("com.sun.jna.platform.win32.WinUser$HHOOK", false, classLoader));
            m75 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("RegisterDeviceNotification", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.Structure", false, classLoader), Integer.TYPE);
            m76 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetWindowModuleFileName", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE);
            m77 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("SetLayeredWindowAttributes", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE, Byte.TYPE, Integer.TYPE);
            m78 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetLayeredWindowAttributes", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.ByteByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m79 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("SetForegroundWindow", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader));
            m80 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("RegisterWindowMessage", Class.forName("java.lang.String", false, classLoader));
            m81 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("EnumDisplayMonitors", Class.forName("com.sun.jna.platform.win32.WinDef$HDC", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$RECT", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinUser$MONITORENUMPROC", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$LPARAM", false, classLoader));
            m82 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetWindowInfo", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinUser$WINDOWINFO", false, classLoader));
            m83 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetGUIThreadInfo", Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinUser$GUITHREADINFO", false, classLoader));
            m84 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("EnumWindows", Class.forName("com.sun.jna.platform.win32.WinUser$WNDENUMPROC", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m85 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("EnumThreadWindows", Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinUser$WNDENUMPROC", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m86 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetWindowLong", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE);
            m87 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("SetWindowLong", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE, Integer.TYPE);
            m88 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("SetWindowLongPtr", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader));
            m89 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("SetWindowRgn", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$HRGN", false, classLoader), Boolean.TYPE);
            m90 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("EnumChildWindows", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinUser$WNDENUMPROC", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m91 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("FlashWindowEx", Class.forName("com.sun.jna.platform.win32.WinUser$FLASHWINFO", false, classLoader));
            m92 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("BringWindowToTop", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader));
            m93 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("DestroyIcon", Class.forName("com.sun.jna.platform.win32.WinDef$HICON", false, classLoader));
            m94 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetWindowLongPtr", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE);
            m95 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetKeyboardState", Class.forName("[B", false, classLoader));
            m96 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetClassName", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE);
            m97 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetAsyncKeyState", Integer.TYPE);
            m98 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("CallNextHookEx", Class.forName("com.sun.jna.platform.win32.WinUser$HHOOK", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$WPARAM", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$LPARAM", false, classLoader));
            m99 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("RegisterClipboardFormat", Class.forName("java.lang.String", false, classLoader));
            m100 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetRawInputDeviceList", Class.forName("[Lcom.sun.jna.platform.win32.WinUser$RAWINPUTDEVICELIST;", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Integer.TYPE);
            m101 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetKeyboardLayoutList", Integer.TYPE, Class.forName("[Lcom.sun.jna.platform.win32.WinDef$HKL;", false, classLoader));
            m102 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetKeyboardLayoutName", Class.forName("[C", false, classLoader));
            m103 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("SetFocus", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader));
            m104 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("IsWindow", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader));
            m105 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("ShowWindow", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE);
            m106 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetParent", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader));
            m107 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("CopyIcon", Class.forName("com.sun.jna.platform.win32.WinDef$HICON", false, classLoader));
            m108 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetWindow", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader));
            m109 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("LoadString", Class.forName("com.sun.jna.platform.win32.WinDef$HINSTANCE", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE);
            m110 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("SendInput", Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader), Class.forName("[Lcom.sun.jna.platform.win32.WinUser$INPUT;", false, classLoader), Integer.TYPE);
            m111 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetDC", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader));
            m112 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("ReleaseDC", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$HDC", false, classLoader));
            m113 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("GetMessage", Class.forName("com.sun.jna.platform.win32.WinUser$MSG", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE, Integer.TYPE);
            m114 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("LoadIcon", Class.forName("com.sun.jna.platform.win32.WinDef$HINSTANCE", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m115 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("MoveWindow", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Boolean.TYPE);
            m116 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("SetParent", Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$HWND", false, classLoader));
            m117 = Class.forName("org.pbrands.windows.User32Extended", false, classLoader).getMethod("LoadImage", Class.forName("com.sun.jna.platform.win32.WinDef$HINSTANCE", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
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

