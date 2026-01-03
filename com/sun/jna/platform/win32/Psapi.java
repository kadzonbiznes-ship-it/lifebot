/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface Psapi
extends StdCallLibrary {
    public static final Psapi INSTANCE = Native.load("psapi", Psapi.class, W32APIOptions.DEFAULT_OPTIONS);

    public int GetModuleFileNameExA(WinNT.HANDLE var1, WinNT.HANDLE var2, byte[] var3, int var4);

    public int GetModuleFileNameExW(WinNT.HANDLE var1, WinNT.HANDLE var2, char[] var3, int var4);

    public int GetModuleFileNameEx(WinNT.HANDLE var1, WinNT.HANDLE var2, Pointer var3, int var4);

    public boolean EnumProcessModules(WinNT.HANDLE var1, WinDef.HMODULE[] var2, int var3, IntByReference var4);

    public boolean GetModuleInformation(WinNT.HANDLE var1, WinDef.HMODULE var2, MODULEINFO var3, int var4);

    public int GetProcessImageFileName(WinNT.HANDLE var1, char[] var2, int var3);

    public boolean GetPerformanceInfo(PERFORMANCE_INFORMATION var1, int var2);

    public boolean EnumProcesses(int[] var1, int var2, IntByReference var3);

    public boolean QueryWorkingSetEx(WinNT.HANDLE var1, Pointer var2, int var3);

    @Structure.FieldOrder(value={"cb", "CommitTotal", "CommitLimit", "CommitPeak", "PhysicalTotal", "PhysicalAvailable", "SystemCache", "KernelTotal", "KernelPaged", "KernelNonpaged", "PageSize", "HandleCount", "ProcessCount", "ThreadCount"})
    public static class PERFORMANCE_INFORMATION
    extends Structure {
        public WinDef.DWORD cb;
        public BaseTSD.SIZE_T CommitTotal;
        public BaseTSD.SIZE_T CommitLimit;
        public BaseTSD.SIZE_T CommitPeak;
        public BaseTSD.SIZE_T PhysicalTotal;
        public BaseTSD.SIZE_T PhysicalAvailable;
        public BaseTSD.SIZE_T SystemCache;
        public BaseTSD.SIZE_T KernelTotal;
        public BaseTSD.SIZE_T KernelPaged;
        public BaseTSD.SIZE_T KernelNonpaged;
        public BaseTSD.SIZE_T PageSize;
        public WinDef.DWORD HandleCount;
        public WinDef.DWORD ProcessCount;
        public WinDef.DWORD ThreadCount;
    }

    @Structure.FieldOrder(value={"lpBaseOfDll", "SizeOfImage", "EntryPoint"})
    public static class MODULEINFO
    extends Structure {
        public Pointer EntryPoint;
        public Pointer lpBaseOfDll;
        public int SizeOfImage;
    }
}

