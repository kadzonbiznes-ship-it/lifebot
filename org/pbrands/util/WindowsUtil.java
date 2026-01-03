/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import java.io.File;
import org.pbrands.windows.User32Extended;

public class WindowsUtil {
    public static final String MTA_WINDOW_NAME = "MTA: San Andreas";
    public static final String MTA_PROCESS_NAME = "Multi Theft Auto.exe";
    public static final String MTA_PROXY_PROCESS_NAME = "proxy_sa.exe";

    public static boolean isMtaSanAndreasFocused() {
        WinDef.HWND hwnd = User32Extended.INSTANCE.GetForegroundWindow();
        if (hwnd != null) {
            char[] buffer = new char[1024];
            User32Extended.INSTANCE.GetWindowText(hwnd, buffer, buffer.length);
            String title = Native.toString(buffer).trim();
            return MTA_WINDOW_NAME.equals(title);
        }
        return false;
    }

    public static boolean doesWindowExist(String windowTitle) {
        WinDef.HWND hwnd = User32Extended.INSTANCE.FindWindow(null, windowTitle);
        return hwnd != null;
    }

    public static WinDef.RECT getWindowResolutionByTitle(String windowTitle) {
        WinDef.HWND hwnd = User32Extended.INSTANCE.FindWindow(null, windowTitle);
        if (hwnd != null) {
            WinDef.RECT rect = new WinDef.RECT();
            User32Extended.INSTANCE.GetWindowRect(hwnd, rect);
            return rect;
        }
        return null;
    }

    public static WinDef.RECT getWindowResolution() {
        WinDef.HWND hwnd = User32Extended.INSTANCE.GetForegroundWindow();
        if (hwnd != null) {
            WinDef.RECT rect = new WinDef.RECT();
            User32Extended.INSTANCE.GetWindowRect(hwnd, rect);
            return rect;
        }
        return null;
    }

    public static WinDef.RECT getClientRectByTitle(String windowTitle) {
        WinDef.RECT clientRect;
        WinDef.HWND hwnd = User32Extended.INSTANCE.FindWindow(null, windowTitle);
        if (hwnd != null && User32Extended.INSTANCE.GetClientRect(hwnd, clientRect = new WinDef.RECT())) {
            WinDef.POINT topLeft = new WinDef.POINT(clientRect.left, clientRect.top);
            WinDef.POINT bottomRight = new WinDef.POINT(clientRect.right, clientRect.bottom);
            User32Extended.INSTANCE.ClientToScreen(hwnd, topLeft);
            User32Extended.INSTANCE.ClientToScreen(hwnd, bottomRight);
            WinDef.RECT screenRect = new WinDef.RECT();
            screenRect.left = topLeft.x;
            screenRect.top = topLeft.y;
            screenRect.right = bottomRight.x;
            screenRect.bottom = bottomRight.y;
            return screenRect;
        }
        return null;
    }

    public static int[] getWindowBorderSizes(String windowTitle) {
        WinDef.RECT windowRect = WindowsUtil.getWindowResolutionByTitle(windowTitle);
        WinDef.RECT clientRect = WindowsUtil.getClientRectByTitle(windowTitle);
        if (windowRect == null || clientRect == null) {
            return new int[]{0, 0, 0, 0};
        }
        int leftBorder = clientRect.left - windowRect.left;
        int topBorder = clientRect.top - windowRect.top;
        int rightBorder = windowRect.right - clientRect.right;
        int bottomBorder = windowRect.bottom - clientRect.bottom;
        return new int[]{leftBorder, topBorder, rightBorder, bottomBorder};
    }

    public static File detectMtaDirectoryFromProcess() {
        File mtaDir;
        WinDef.HWND hwnd = User32Extended.INSTANCE.FindWindow(null, MTA_WINDOW_NAME);
        if (hwnd != null && (mtaDir = WindowsUtil.getMtaDirectoryFromWindow(hwnd)) != null) {
            return mtaDir;
        }
        return WindowsUtil.detectMtaDirectoryFromProcessList();
    }

    private static File getMtaDirectoryFromWindow(WinDef.HWND hwnd) {
        File exeFile;
        File parentDir;
        String exePath;
        IntByReference pidRef = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(hwnd, pidRef);
        int pid = pidRef.getValue();
        if (pid != 0 && (exePath = WindowsUtil.getProcessExecutablePath(pid)) != null && (parentDir = (exeFile = new File(exePath)).getParentFile()) != null && WindowsUtil.isValidMtaDirectory(parentDir)) {
            return parentDir;
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static File detectMtaDirectoryFromProcessList() {
        Tlhelp32.PROCESSENTRY32.ByReference processEntry = new Tlhelp32.PROCESSENTRY32.ByReference();
        WinNT.HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0L));
        try {
            if (Kernel32.INSTANCE.Process32First(snapshot, processEntry)) {
                do {
                    File exeFile;
                    File parentDir;
                    int pid;
                    String exePath;
                    String processName;
                    if (!MTA_PROCESS_NAME.equalsIgnoreCase(processName = Native.toString(processEntry.szExeFile)) && !MTA_PROXY_PROCESS_NAME.equalsIgnoreCase(processName) || (exePath = WindowsUtil.getProcessExecutablePath(pid = processEntry.th32ProcessID.intValue())) == null || (parentDir = (exeFile = new File(exePath)).getParentFile()) == null || !WindowsUtil.isValidMtaDirectory(parentDir)) continue;
                    File file = parentDir;
                    return file;
                } while (Kernel32.INSTANCE.Process32Next(snapshot, processEntry));
            }
        }
        finally {
            Kernel32.INSTANCE.CloseHandle(snapshot);
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String getProcessExecutablePath(int pid) {
        WinNT.HANDLE processHandle = Kernel32.INSTANCE.OpenProcess(4096, false, pid);
        if (processHandle == null) {
            return null;
        }
        try {
            char[] path = new char[1024];
            IntByReference size = new IntByReference(path.length);
            if (Kernel32.INSTANCE.QueryFullProcessImageName(processHandle, 0, path, size)) {
                String string = Native.toString(path).trim();
                return string;
            }
        }
        finally {
            Kernel32.INSTANCE.CloseHandle(processHandle);
        }
        return null;
    }

    private static boolean isValidMtaDirectory(File dir) {
        File exeFile = new File(dir, MTA_PROCESS_NAME);
        return exeFile.exists() && exeFile.isFile();
    }

    public static boolean isMtaRunning() {
        return WindowsUtil.doesWindowExist(MTA_WINDOW_NAME) || WindowsUtil.isMtaProcessRunning();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean isMtaProcessRunning() {
        Tlhelp32.PROCESSENTRY32.ByReference processEntry = new Tlhelp32.PROCESSENTRY32.ByReference();
        WinNT.HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0L));
        try {
            if (Kernel32.INSTANCE.Process32First(snapshot, processEntry)) {
                do {
                    String processName;
                    if (!MTA_PROCESS_NAME.equalsIgnoreCase(processName = Native.toString(processEntry.szExeFile)) && !MTA_PROXY_PROCESS_NAME.equalsIgnoreCase(processName)) continue;
                    boolean bl = true;
                    return bl;
                } while (Kernel32.INSTANCE.Process32Next(snapshot, processEntry));
            }
        }
        finally {
            Kernel32.INSTANCE.CloseHandle(snapshot);
        }
        return false;
    }
}

