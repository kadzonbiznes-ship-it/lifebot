/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.windows;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.W32APIOptions;

public interface User32Extended
extends User32 {
    public static final User32Extended INSTANCE = Native.load("user32", User32Extended.class, W32APIOptions.DEFAULT_OPTIONS);

    public boolean SetProcessDPIAware();

    @Override
    public WinDef.HWND GetForegroundWindow();

    @Override
    public int GetWindowText(WinDef.HWND var1, char[] var2, int var3);

    @Override
    public boolean GetWindowRect(WinDef.HWND var1, WinDef.RECT var2);

    @Override
    public boolean GetClientRect(WinDef.HWND var1, WinDef.RECT var2);

    public boolean ClientToScreen(WinDef.HWND var1, WinDef.POINT var2);

    @Override
    public WinDef.HWND FindWindow(String var1, String var2);

    public short VkKeyScanA(char var1);

    public int MapVirtualKeyA(int var1, int var2);

    public WinDef.LRESULT CallNextHookEx(WinUser.HHOOK var1, int var2, WinDef.WPARAM var3, Pointer var4);
}

