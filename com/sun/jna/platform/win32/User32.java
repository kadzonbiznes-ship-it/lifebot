/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface User32
extends StdCallLibrary,
WinUser,
WinNT {
    public static final User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);
    public static final WinDef.HWND HWND_MESSAGE = new WinDef.HWND(Pointer.createConstant(-3));
    public static final int CS_GLOBALCLASS = 16384;
    public static final int WS_EX_TOPMOST = 8;
    public static final int DEVICE_NOTIFY_WINDOW_HANDLE = 0;
    public static final int DEVICE_NOTIFY_SERVICE_HANDLE = 1;
    public static final int DEVICE_NOTIFY_ALL_INTERFACE_CLASSES = 4;
    public static final int SW_SHOWDEFAULT = 10;

    public WinDef.HDC GetDC(WinDef.HWND var1);

    public int ReleaseDC(WinDef.HWND var1, WinDef.HDC var2);

    public WinDef.HWND FindWindow(String var1, String var2);

    public int GetClassName(WinDef.HWND var1, char[] var2, int var3);

    public boolean GetGUIThreadInfo(int var1, WinUser.GUITHREADINFO var2);

    public boolean GetWindowInfo(WinDef.HWND var1, WinUser.WINDOWINFO var2);

    public boolean GetWindowRect(WinDef.HWND var1, WinDef.RECT var2);

    public boolean GetClientRect(WinDef.HWND var1, WinDef.RECT var2);

    public int GetWindowText(WinDef.HWND var1, char[] var2, int var3);

    public int GetWindowTextLength(WinDef.HWND var1);

    public int GetWindowModuleFileName(WinDef.HWND var1, char[] var2, int var3);

    public int GetWindowThreadProcessId(WinDef.HWND var1, IntByReference var2);

    public boolean EnumWindows(WinUser.WNDENUMPROC var1, Pointer var2);

    public boolean EnumChildWindows(WinDef.HWND var1, WinUser.WNDENUMPROC var2, Pointer var3);

    public boolean EnumThreadWindows(int var1, WinUser.WNDENUMPROC var2, Pointer var3);

    public boolean BringWindowToTop(WinDef.HWND var1);

    public boolean FlashWindowEx(WinUser.FLASHWINFO var1);

    public WinDef.HICON LoadIcon(WinDef.HINSTANCE var1, String var2);

    public WinNT.HANDLE LoadImage(WinDef.HINSTANCE var1, String var2, int var3, int var4, int var5, int var6);

    public boolean DestroyIcon(WinDef.HICON var1);

    public int GetWindowLong(WinDef.HWND var1, int var2);

    public int SetWindowLong(WinDef.HWND var1, int var2, int var3);

    public BaseTSD.LONG_PTR GetWindowLongPtr(WinDef.HWND var1, int var2);

    public Pointer SetWindowLongPtr(WinDef.HWND var1, int var2, Pointer var3);

    public boolean SetLayeredWindowAttributes(WinDef.HWND var1, int var2, byte var3, int var4);

    public boolean GetLayeredWindowAttributes(WinDef.HWND var1, IntByReference var2, ByteByReference var3, IntByReference var4);

    public boolean UpdateLayeredWindow(WinDef.HWND var1, WinDef.HDC var2, WinDef.POINT var3, WinUser.SIZE var4, WinDef.HDC var5, WinDef.POINT var6, int var7, WinUser.BLENDFUNCTION var8, int var9);

    public int SetWindowRgn(WinDef.HWND var1, WinDef.HRGN var2, boolean var3);

    public boolean GetKeyboardState(byte[] var1);

    public short GetAsyncKeyState(int var1);

    public WinUser.HHOOK SetWindowsHookEx(int var1, WinUser.HOOKPROC var2, WinDef.HINSTANCE var3, int var4);

    public WinDef.LRESULT CallNextHookEx(WinUser.HHOOK var1, int var2, WinDef.WPARAM var3, WinDef.LPARAM var4);

    public boolean UnhookWindowsHookEx(WinUser.HHOOK var1);

    public int GetMessage(WinUser.MSG var1, WinDef.HWND var2, int var3, int var4);

    public boolean PeekMessage(WinUser.MSG var1, WinDef.HWND var2, int var3, int var4, int var5);

    public boolean TranslateMessage(WinUser.MSG var1);

    public WinDef.LRESULT DispatchMessage(WinUser.MSG var1);

    public void PostMessage(WinDef.HWND var1, int var2, WinDef.WPARAM var3, WinDef.LPARAM var4);

    public int PostThreadMessage(int var1, int var2, WinDef.WPARAM var3, WinDef.LPARAM var4);

    public void PostQuitMessage(int var1);

    public int GetSystemMetrics(int var1);

    public WinDef.HWND SetParent(WinDef.HWND var1, WinDef.HWND var2);

    public boolean IsWindowVisible(WinDef.HWND var1);

    public boolean MoveWindow(WinDef.HWND var1, int var2, int var3, int var4, int var5, boolean var6);

    public boolean SetWindowPos(WinDef.HWND var1, WinDef.HWND var2, int var3, int var4, int var5, int var6, int var7);

    public boolean AttachThreadInput(WinDef.DWORD var1, WinDef.DWORD var2, boolean var3);

    public boolean SetForegroundWindow(WinDef.HWND var1);

    public WinDef.HWND GetForegroundWindow();

    public WinDef.HWND SetFocus(WinDef.HWND var1);

    public WinDef.DWORD SendInput(WinDef.DWORD var1, WinUser.INPUT[] var2, int var3);

    public WinDef.DWORD WaitForInputIdle(WinNT.HANDLE var1, WinDef.DWORD var2);

    public boolean InvalidateRect(WinDef.HWND var1, WinDef.RECT var2, boolean var3);

    public boolean RedrawWindow(WinDef.HWND var1, WinDef.RECT var2, WinDef.HRGN var3, WinDef.DWORD var4);

    public WinDef.HWND GetWindow(WinDef.HWND var1, WinDef.DWORD var2);

    public boolean UpdateWindow(WinDef.HWND var1);

    public boolean ShowWindow(WinDef.HWND var1, int var2);

    public boolean CloseWindow(WinDef.HWND var1);

    public boolean RegisterHotKey(WinDef.HWND var1, int var2, int var3, int var4);

    public boolean UnregisterHotKey(Pointer var1, int var2);

    public boolean GetLastInputInfo(WinUser.LASTINPUTINFO var1);

    public WinDef.ATOM RegisterClassEx(WinUser.WNDCLASSEX var1);

    public boolean UnregisterClass(String var1, WinDef.HINSTANCE var2);

    public WinDef.HWND CreateWindowEx(int var1, String var2, String var3, int var4, int var5, int var6, int var7, int var8, WinDef.HWND var9, WinDef.HMENU var10, WinDef.HINSTANCE var11, WinDef.LPVOID var12);

    public boolean DestroyWindow(WinDef.HWND var1);

    public boolean GetClassInfoEx(WinDef.HINSTANCE var1, String var2, WinUser.WNDCLASSEX var3);

    public WinDef.LRESULT CallWindowProc(Pointer var1, WinDef.HWND var2, int var3, WinDef.WPARAM var4, WinDef.LPARAM var5);

    public WinDef.LRESULT DefWindowProc(WinDef.HWND var1, int var2, WinDef.WPARAM var3, WinDef.LPARAM var4);

    public WinUser.HDEVNOTIFY RegisterDeviceNotification(WinNT.HANDLE var1, Structure var2, int var3);

    public boolean UnregisterDeviceNotification(WinUser.HDEVNOTIFY var1);

    public int RegisterWindowMessage(String var1);

    public WinUser.HMONITOR MonitorFromPoint(WinDef.POINT.ByValue var1, int var2);

    public WinUser.HMONITOR MonitorFromRect(WinDef.RECT var1, int var2);

    public WinUser.HMONITOR MonitorFromWindow(WinDef.HWND var1, int var2);

    public WinDef.BOOL GetMonitorInfo(WinUser.HMONITOR var1, WinUser.MONITORINFO var2);

    public WinDef.BOOL GetMonitorInfo(WinUser.HMONITOR var1, WinUser.MONITORINFOEX var2);

    public WinDef.BOOL EnumDisplayMonitors(WinDef.HDC var1, WinDef.RECT var2, WinUser.MONITORENUMPROC var3, WinDef.LPARAM var4);

    public WinDef.BOOL GetWindowPlacement(WinDef.HWND var1, WinUser.WINDOWPLACEMENT var2);

    public WinDef.BOOL SetWindowPlacement(WinDef.HWND var1, WinUser.WINDOWPLACEMENT var2);

    public WinDef.BOOL AdjustWindowRect(WinDef.RECT var1, WinDef.DWORD var2, WinDef.BOOL var3);

    public WinDef.BOOL AdjustWindowRectEx(WinDef.RECT var1, WinDef.DWORD var2, WinDef.BOOL var3, WinDef.DWORD var4);

    public WinDef.BOOL ExitWindowsEx(WinDef.UINT var1, WinDef.DWORD var2);

    public WinDef.BOOL LockWorkStation();

    public boolean GetIconInfo(WinDef.HICON var1, WinGDI.ICONINFO var2);

    public WinDef.LRESULT SendMessageTimeout(WinDef.HWND var1, int var2, WinDef.WPARAM var3, WinDef.LPARAM var4, int var5, int var6, WinDef.DWORDByReference var7);

    public BaseTSD.ULONG_PTR GetClassLongPtr(WinDef.HWND var1, int var2);

    public int GetRawInputDeviceList(WinUser.RAWINPUTDEVICELIST[] var1, IntByReference var2, int var3);

    public WinDef.HWND GetDesktopWindow();

    public boolean PrintWindow(WinDef.HWND var1, WinDef.HDC var2, int var3);

    public boolean IsWindowEnabled(WinDef.HWND var1);

    public boolean IsWindow(WinDef.HWND var1);

    public WinDef.HWND FindWindowEx(WinDef.HWND var1, WinDef.HWND var2, String var3, String var4);

    public WinDef.HWND GetAncestor(WinDef.HWND var1, int var2);

    public WinDef.HWND GetParent(WinDef.HWND var1);

    public boolean GetCursorPos(WinDef.POINT var1);

    public boolean SetCursorPos(long var1, long var3);

    public WinNT.HANDLE SetWinEventHook(int var1, int var2, WinDef.HMODULE var3, WinUser.WinEventProc var4, int var5, int var6, int var7);

    public boolean UnhookWinEvent(WinNT.HANDLE var1);

    public WinDef.HICON CopyIcon(WinDef.HICON var1);

    public int GetClassLong(WinDef.HWND var1, int var2);

    public int RegisterClipboardFormat(String var1);

    public WinDef.HWND GetActiveWindow();

    public WinDef.LRESULT SendMessage(WinDef.HWND var1, int var2, WinDef.WPARAM var3, WinDef.LPARAM var4);

    public int GetKeyboardLayoutList(int var1, WinDef.HKL[] var2);

    public WinDef.HKL GetKeyboardLayout(int var1);

    public boolean GetKeyboardLayoutName(char[] var1);

    public short VkKeyScanExA(byte var1, WinDef.HKL var2);

    public short VkKeyScanExW(char var1, WinDef.HKL var2);

    public int MapVirtualKeyEx(int var1, int var2, WinDef.HKL var3);

    public int ToUnicodeEx(int var1, int var2, byte[] var3, char[] var4, int var5, int var6, WinDef.HKL var7);

    public int LoadString(WinDef.HINSTANCE var1, int var2, Pointer var3, int var4);
}

