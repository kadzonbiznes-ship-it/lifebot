/*
 * Decompiled with CFR 0.152.
 */
package jdk.proxy1;

import com.sun.jna.LastErrorException;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Wincon;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import oshi.jna.platform.windows.Kernel32;

public final class $Proxy13
extends Proxy
implements Kernel32 {
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
    private static final Method m118;
    private static final Method m119;
    private static final Method m120;
    private static final Method m121;
    private static final Method m122;
    private static final Method m123;
    private static final Method m124;
    private static final Method m125;
    private static final Method m126;
    private static final Method m127;
    private static final Method m128;
    private static final Method m129;
    private static final Method m130;
    private static final Method m131;
    private static final Method m132;
    private static final Method m133;
    private static final Method m134;
    private static final Method m135;
    private static final Method m136;
    private static final Method m137;
    private static final Method m138;
    private static final Method m139;
    private static final Method m140;
    private static final Method m141;
    private static final Method m142;
    private static final Method m143;
    private static final Method m144;
    private static final Method m145;
    private static final Method m146;
    private static final Method m147;
    private static final Method m148;
    private static final Method m149;
    private static final Method m150;
    private static final Method m151;
    private static final Method m152;
    private static final Method m153;
    private static final Method m154;
    private static final Method m155;
    private static final Method m156;
    private static final Method m157;
    private static final Method m158;
    private static final Method m159;
    private static final Method m160;
    private static final Method m161;
    private static final Method m162;
    private static final Method m163;
    private static final Method m164;
    private static final Method m165;
    private static final Method m166;
    private static final Method m167;
    private static final Method m168;
    private static final Method m169;
    private static final Method m170;
    private static final Method m171;
    private static final Method m172;
    private static final Method m173;
    private static final Method m174;
    private static final Method m175;
    private static final Method m176;
    private static final Method m177;
    private static final Method m178;
    private static final Method m179;
    private static final Method m180;
    private static final Method m181;
    private static final Method m182;
    private static final Method m183;
    private static final Method m184;
    private static final Method m185;
    private static final Method m186;
    private static final Method m187;
    private static final Method m188;
    private static final Method m189;
    private static final Method m190;
    private static final Method m191;
    private static final Method m192;
    private static final Method m193;
    private static final Method m194;
    private static final Method m195;
    private static final Method m196;
    private static final Method m197;
    private static final Method m198;
    private static final Method m199;
    private static final Method m200;
    private static final Method m201;
    private static final Method m202;
    private static final Method m203;
    private static final Method m204;
    private static final Method m205;
    private static final Method m206;
    private static final Method m207;

    public $Proxy13(InvocationHandler invocationHandler) {
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
    public final WinNT.HANDLE CreateFile(String string, int n, int n2, WinBase.SECURITY_ATTRIBUTES sECURITY_ATTRIBUTES, int n3, int n4, WinNT.HANDLE hANDLE) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m3, new Object[]{string, n, n2, sECURITY_ATTRIBUTES, n3, n4, hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int SetFileTime(WinNT.HANDLE hANDLE, WinBase.FILETIME fILETIME, WinBase.FILETIME fILETIME2, WinBase.FILETIME fILETIME3) {
        try {
            return (Integer)this.h.invoke(this, m4, new Object[]{hANDLE, fILETIME, fILETIME2, fILETIME3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean CloseHandle(WinNT.HANDLE hANDLE) {
        try {
            return (Boolean)this.h.invoke(this, m5, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE FindFirstFile(String string, Pointer pointer) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m6, new Object[]{string, pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean FindClose(WinNT.HANDLE hANDLE) {
        try {
            return (Boolean)this.h.invoke(this, m7, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean DeleteFile(String string) {
        try {
            return (Boolean)this.h.invoke(this, m8, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean CreateDirectory(String string, WinBase.SECURITY_ATTRIBUTES sECURITY_ATTRIBUTES) {
        try {
            return (Boolean)this.h.invoke(this, m9, new Object[]{string, sECURITY_ATTRIBUTES});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE CreateEvent(WinBase.SECURITY_ATTRIBUTES sECURITY_ATTRIBUTES, boolean bl, boolean bl2, String string) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m10, new Object[]{sECURITY_ATTRIBUTES, bl, bl2, string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean FindNextFile(WinNT.HANDLE hANDLE, Pointer pointer) {
        try {
            return (Boolean)this.h.invoke(this, m11, new Object[]{hANDLE, pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean MoveFileEx(String string, String string2, WinDef.DWORD dWORD) {
        try {
            return (Boolean)this.h.invoke(this, m12, new Object[]{string, string2, dWORD});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetFileAttributes(String string) {
        try {
            return (Integer)this.h.invoke(this, m13, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetFileAttributes(String string, WinDef.DWORD dWORD) {
        try {
            return (Boolean)this.h.invoke(this, m14, new Object[]{string, dWORD});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetVolumeInformation(String string, char[] cArray, int n, IntByReference intByReference, IntByReference intByReference2, IntByReference intByReference3, char[] cArray2, int n2) {
        try {
            return (Boolean)this.h.invoke(this, m15, new Object[]{string, cArray, n, intByReference, intByReference2, intByReference3, cArray2, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetDriveType(String string) {
        try {
            return (Integer)this.h.invoke(this, m16, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetDiskFreeSpaceEx(String string, WinNT.LARGE_INTEGER lARGE_INTEGER, WinNT.LARGE_INTEGER lARGE_INTEGER2, WinNT.LARGE_INTEGER lARGE_INTEGER3) {
        try {
            return (Boolean)this.h.invoke(this, m17, new Object[]{string, lARGE_INTEGER, lARGE_INTEGER2, lARGE_INTEGER3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetDiskFreeSpace(String string, WinDef.DWORDByReference dWORDByReference, WinDef.DWORDByReference dWORDByReference2, WinDef.DWORDByReference dWORDByReference3, WinDef.DWORDByReference dWORDByReference4) {
        try {
            return (Boolean)this.h.invoke(this, m18, new Object[]{string, dWORDByReference, dWORDByReference2, dWORDByReference3, dWORDByReference4});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetVolumePathName(String string, char[] cArray, int n) {
        try {
            return (Boolean)this.h.invoke(this, m19, new Object[]{string, cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE GetCurrentProcess() {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m20, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE GetCurrentThread() {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m21, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int FormatMessage(int n, Pointer pointer, int n2, int n3, PointerByReference pointerByReference, int n4, Pointer pointer2) {
        try {
            return (Integer)this.h.invoke(this, m22, new Object[]{n, pointer, n2, n3, pointerByReference, n4, pointer2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer LocalFree(Pointer pointer) {
        try {
            return (Pointer)this.h.invoke(this, m23, new Object[]{pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE CreateIoCompletionPort(WinNT.HANDLE hANDLE, WinNT.HANDLE hANDLE2, Pointer pointer, int n) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m24, new Object[]{hANDLE, hANDLE2, pointer, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetQueuedCompletionStatus(WinNT.HANDLE hANDLE, IntByReference intByReference, BaseTSD.ULONG_PTRByReference uLONG_PTRByReference, PointerByReference pointerByReference, int n) {
        try {
            return (Boolean)this.h.invoke(this, m25, new Object[]{hANDLE, intByReference, uLONG_PTRByReference, pointerByReference, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean PostQueuedCompletionStatus(WinNT.HANDLE hANDLE, int n, Pointer pointer, WinBase.OVERLAPPED oVERLAPPED) {
        try {
            return (Boolean)this.h.invoke(this, m26, new Object[]{hANDLE, n, pointer, oVERLAPPED});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean ReadDirectoryChangesW(WinNT.HANDLE hANDLE, WinNT.FILE_NOTIFY_INFORMATION fILE_NOTIFY_INFORMATION, int n, boolean bl, int n2, IntByReference intByReference, WinBase.OVERLAPPED oVERLAPPED, WinNT.OVERLAPPED_COMPLETION_ROUTINE oVERLAPPED_COMPLETION_ROUTINE) {
        try {
            return (Boolean)this.h.invoke(this, m27, new Object[]{hANDLE, fILE_NOTIFY_INFORMATION, n, bl, n2, intByReference, oVERLAPPED, oVERLAPPED_COMPLETION_ROUTINE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT UnregisterApplicationRestart() {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m28, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetVolumePathNamesForVolumeName(String string, char[] cArray, int n, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m29, new Object[]{string, cArray, n, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetNamedPipeServerSessionId(WinNT.HANDLE hANDLE, WinDef.ULONGByReference uLONGByReference) {
        try {
            return (Boolean)this.h.invoke(this, m30, new Object[]{hANDLE, uLONGByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.DWORD GetPrivateProfileSectionNames(char[] cArray, WinDef.DWORD dWORD, String string) {
        try {
            return (WinDef.DWORD)this.h.invoke(this, m31, new Object[]{cArray, dWORD, string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetNamedPipeClientSessionId(WinNT.HANDLE hANDLE, WinDef.ULONGByReference uLONGByReference) {
        try {
            return (Boolean)this.h.invoke(this, m32, new Object[]{hANDLE, uLONGByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetNamedPipeClientComputerName(WinNT.HANDLE hANDLE, char[] cArray, int n) {
        try {
            return (Boolean)this.h.invoke(this, m33, new Object[]{hANDLE, cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetLogicalProcessorInformationEx(int n, Pointer pointer, WinDef.DWORDByReference dWORDByReference) {
        try {
            return (Boolean)this.h.invoke(this, m34, new Object[]{n, pointer, dWORDByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetVolumeNameForVolumeMountPoint(String string, char[] cArray, int n) {
        try {
            return (Boolean)this.h.invoke(this, m35, new Object[]{string, cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SystemTimeToTzSpecificLocalTime(WinBase.TIME_ZONE_INFORMATION tIME_ZONE_INFORMATION, WinBase.SYSTEMTIME sYSTEMTIME, WinBase.SYSTEMTIME sYSTEMTIME2) {
        try {
            return (Boolean)this.h.invoke(this, m36, new Object[]{tIME_ZONE_INFORMATION, sYSTEMTIME, sYSTEMTIME2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT GetApplicationRestartSettings(WinNT.HANDLE hANDLE, char[] cArray, IntByReference intByReference, IntByReference intByReference2) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m37, new Object[]{hANDLE, cArray, intByReference, intByReference2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetLogicalProcessorInformation(Pointer pointer, WinDef.DWORDByReference dWORDByReference) {
        try {
            return (Boolean)this.h.invoke(this, m38, new Object[]{pointer, dWORDByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetFileInformationByHandleEx(WinNT.HANDLE hANDLE, int n, Pointer pointer, WinDef.DWORD dWORD) {
        try {
            return (Boolean)this.h.invoke(this, m39, new Object[]{hANDLE, n, pointer, dWORD});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetNamedPipeClientProcessId(WinNT.HANDLE hANDLE, WinDef.ULONGByReference uLONGByReference) {
        try {
            return (Boolean)this.h.invoke(this, m40, new Object[]{hANDLE, uLONGByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetNamedPipeServerProcessId(WinNT.HANDLE hANDLE, WinDef.ULONGByReference uLONGByReference) {
        try {
            return (Boolean)this.h.invoke(this, m41, new Object[]{hANDLE, uLONGByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean VerifyVersionInfoW(WinNT.OSVERSIONINFOEX oSVERSIONINFOEX, int n, long l) {
        try {
            return (Boolean)this.h.invoke(this, m42, new Object[]{oSVERSIONINFOEX, n, l});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer MapViewOfFile(WinNT.HANDLE hANDLE, int n, int n2, int n3, int n4) {
        try {
            return (Pointer)this.h.invoke(this, m43, new Object[]{hANDLE, n, n2, n3, n4});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean TerminateProcess(WinNT.HANDLE hANDLE, int n) {
        try {
            return (Boolean)this.h.invoke(this, m44, new Object[]{hANDLE, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetComputerName(char[] cArray, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m45, new Object[]{cArray, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE CreateFileMapping(WinNT.HANDLE hANDLE, WinBase.SECURITY_ATTRIBUTES sECURITY_ATTRIBUTES, int n, int n2, int n3, String string) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m46, new Object[]{hANDLE, sECURITY_ATTRIBUTES, n, n2, n3, string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.HMODULE GetModuleHandle(String string) {
        try {
            return (WinDef.HMODULE)this.h.invoke(this, m47, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void GetSystemTime(WinBase.SYSTEMTIME sYSTEMTIME) {
        try {
            this.h.invoke(this, m48, new Object[]{sYSTEMTIME});
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
    public final boolean DuplicateHandle(WinNT.HANDLE hANDLE, WinNT.HANDLE hANDLE2, WinNT.HANDLE hANDLE3, WinNT.HANDLEByReference hANDLEByReference, int n, boolean bl, int n2) {
        try {
            return (Boolean)this.h.invoke(this, m49, new Object[]{hANDLE, hANDLE2, hANDLE3, hANDLEByReference, n, bl, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetShortPathName(String string, char[] cArray, int n) {
        try {
            return (Integer)this.h.invoke(this, m50, new Object[]{string, cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetSystemTime(WinBase.SYSTEMTIME sYSTEMTIME) {
        try {
            return (Boolean)this.h.invoke(this, m51, new Object[]{sYSTEMTIME});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetComputerNameEx(int n, char[] cArray, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m52, new Object[]{n, cArray, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean CreateProcess(String string, String string2, WinBase.SECURITY_ATTRIBUTES sECURITY_ATTRIBUTES, WinBase.SECURITY_ATTRIBUTES sECURITY_ATTRIBUTES2, boolean bl, WinDef.DWORD dWORD, Pointer pointer, String string3, WinBase.STARTUPINFO sTARTUPINFO, WinBase.PROCESS_INFORMATION pROCESS_INFORMATION) {
        try {
            return (Boolean)this.h.invoke(this, m53, new Object[]{string, string2, sECURITY_ATTRIBUTES, sECURITY_ATTRIBUTES2, bl, dWORD, pointer, string3, sTARTUPINFO, pROCESS_INFORMATION});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE OpenProcess(int n, boolean bl, int n2) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m54, new Object[]{n, bl, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean FlushFileBuffers(WinNT.HANDLE hANDLE) {
        try {
            return (Boolean)this.h.invoke(this, m55, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE OpenFileMapping(int n, boolean bl, String string) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m56, new Object[]{n, bl, string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.DWORD GetTempPath(WinDef.DWORD dWORD, char[] cArray) {
        try {
            return (WinDef.DWORD)this.h.invoke(this, m57, new Object[]{dWORD, cArray});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetVersionEx(WinNT.OSVERSIONINFO oSVERSIONINFO) {
        try {
            return (Boolean)this.h.invoke(this, m58, new Object[]{oSVERSIONINFO});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetVersionEx(WinNT.OSVERSIONINFOEX oSVERSIONINFOEX) {
        try {
            return (Boolean)this.h.invoke(this, m59, new Object[]{oSVERSIONINFOEX});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void GetSystemInfo(WinBase.SYSTEM_INFO sYSTEM_INFO) {
        try {
            this.h.invoke(this, m60, new Object[]{sYSTEM_INFO});
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
    public final boolean GetFileTime(WinNT.HANDLE hANDLE, WinBase.FILETIME fILETIME, WinBase.FILETIME fILETIME2, WinBase.FILETIME fILETIME3) {
        try {
            return (Boolean)this.h.invoke(this, m61, new Object[]{hANDLE, fILETIME, fILETIME2, fILETIME3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean CallNamedPipe(String string, byte[] byArray, int n, byte[] byArray2, int n2, IntByReference intByReference, int n3) {
        try {
            return (Boolean)this.h.invoke(this, m62, new Object[]{string, byArray, n, byArray2, n2, intByReference, n3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetExitCodeProcess(WinNT.HANDLE hANDLE, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m63, new Object[]{hANDLE, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean ConnectNamedPipe(WinNT.HANDLE hANDLE, WinBase.OVERLAPPED oVERLAPPED) {
        try {
            return (Boolean)this.h.invoke(this, m64, new Object[]{hANDLE, oVERLAPPED});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetLocalTime(WinBase.SYSTEMTIME sYSTEMTIME) {
        try {
            return (Boolean)this.h.invoke(this, m65, new Object[]{sYSTEMTIME});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetProcessId(WinNT.HANDLE hANDLE) {
        try {
            return (Integer)this.h.invoke(this, m66, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetSystemTimes(WinBase.FILETIME fILETIME, WinBase.FILETIME fILETIME2, WinBase.FILETIME fILETIME3) {
        try {
            return (Boolean)this.h.invoke(this, m67, new Object[]{fILETIME, fILETIME2, fILETIME3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean UnmapViewOfFile(Pointer pointer) {
        try {
            return (Boolean)this.h.invoke(this, m68, new Object[]{pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetNamedPipeInfo(WinNT.HANDLE hANDLE, IntByReference intByReference, IntByReference intByReference2, IntByReference intByReference3, IntByReference intByReference4) {
        try {
            return (Boolean)this.h.invoke(this, m69, new Object[]{hANDLE, intByReference, intByReference2, intByReference3, intByReference4});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean PeekNamedPipe(WinNT.HANDLE hANDLE, byte[] byArray, int n, IntByReference intByReference, IntByReference intByReference2, IntByReference intByReference3) {
        try {
            return (Boolean)this.h.invoke(this, m70, new Object[]{hANDLE, byArray, n, intByReference, intByReference2, intByReference3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean TransactNamedPipe(WinNT.HANDLE hANDLE, byte[] byArray, int n, byte[] byArray2, int n2, IntByReference intByReference, WinBase.OVERLAPPED oVERLAPPED) {
        try {
            return (Boolean)this.h.invoke(this, m71, new Object[]{hANDLE, byArray, n, byArray2, n2, intByReference, oVERLAPPED});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean WaitNamedPipe(String string, int n) {
        try {
            return (Boolean)this.h.invoke(this, m72, new Object[]{string, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void SetLastError(int n) {
        try {
            this.h.invoke(this, m73, new Object[]{n});
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
    public final int GetFileType(WinNT.HANDLE hANDLE) {
        try {
            return (Integer)this.h.invoke(this, m74, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE CreateNamedPipe(String string, int n, int n2, int n3, int n4, int n5, int n6, WinBase.SECURITY_ATTRIBUTES sECURITY_ATTRIBUTES) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m75, new Object[]{string, n, n2, n3, n4, n5, n6, sECURITY_ATTRIBUTES});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean DeviceIoControl(WinNT.HANDLE hANDLE, int n, Pointer pointer, int n2, Pointer pointer2, int n3, IntByReference intByReference, Pointer pointer3) {
        try {
            return (Boolean)this.h.invoke(this, m76, new Object[]{hANDLE, n, pointer, n2, pointer2, n3, intByReference, pointer3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean Process32First(WinNT.HANDLE hANDLE, Tlhelp32.PROCESSENTRY32 pROCESSENTRY32) {
        try {
            return (Boolean)this.h.invoke(this, m77, new Object[]{hANDLE, pROCESSENTRY32});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean CreateProcessW(String string, char[] cArray, WinBase.SECURITY_ATTRIBUTES sECURITY_ATTRIBUTES, WinBase.SECURITY_ATTRIBUTES sECURITY_ATTRIBUTES2, boolean bl, WinDef.DWORD dWORD, Pointer pointer, String string2, WinBase.STARTUPINFO sTARTUPINFO, WinBase.PROCESS_INFORMATION pROCESS_INFORMATION) {
        try {
            return (Boolean)this.h.invoke(this, m78, new Object[]{string, cArray, sECURITY_ATTRIBUTES, sECURITY_ATTRIBUTES2, bl, dWORD, pointer, string2, sTARTUPINFO, pROCESS_INFORMATION});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean Thread32First(WinNT.HANDLE hANDLE, Tlhelp32.THREADENTRY32 tHREADENTRY32) {
        try {
            return (Boolean)this.h.invoke(this, m79, new Object[]{hANDLE, tHREADENTRY32});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean Thread32Next(WinNT.HANDLE hANDLE, Tlhelp32.THREADENTRY32 tHREADENTRY32) {
        try {
            return (Boolean)this.h.invoke(this, m80, new Object[]{hANDLE, tHREADENTRY32});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.LCID GetUserDefaultLCID() {
        try {
            return (WinDef.LCID)this.h.invoke(this, m81, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void GetLocalTime(WinBase.SYSTEMTIME sYSTEMTIME) {
        try {
            this.h.invoke(this, m82, new Object[]{sYSTEMTIME});
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
    public final int GetProcessVersion(int n) {
        try {
            return (Integer)this.h.invoke(this, m83, new Object[]{n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetThreadPriority(WinNT.HANDLE hANDLE) {
        try {
            return (Integer)this.h.invoke(this, m84, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean DefineDosDevice(int n, String string, String string2) {
        try {
            return (Boolean)this.h.invoke(this, m85, new Object[]{n, string, string2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean WriteProcessMemory(WinNT.HANDLE hANDLE, Pointer pointer, Pointer pointer2, int n, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m86, new Object[]{hANDLE, pointer, pointer2, n, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean FindNextVolume(WinNT.HANDLE hANDLE, char[] cArray, int n) {
        try {
            return (Boolean)this.h.invoke(this, m87, new Object[]{hANDLE, cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetExitCodeThread(WinNT.HANDLE hANDLE, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m88, new Object[]{hANDLE, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void ExitProcess(int n) {
        try {
            this.h.invoke(this, m89, new Object[]{n});
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
    public final boolean SetPriorityClass(WinNT.HANDLE hANDLE, WinDef.DWORD dWORD) {
        try {
            return (Boolean)this.h.invoke(this, m90, new Object[]{hANDLE, dWORD});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean EnumResourceNames(WinDef.HMODULE hMODULE, Pointer pointer, WinBase.EnumResNameProc enumResNameProc, Pointer pointer2) {
        try {
            return (Boolean)this.h.invoke(this, m91, new Object[]{hMODULE, pointer, enumResNameProc, pointer2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE LoadResource(WinDef.HMODULE hMODULE, WinDef.HRSRC hRSRC) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m92, new Object[]{hMODULE, hRSRC});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean EnumResourceTypes(WinDef.HMODULE hMODULE, WinBase.EnumResTypeProc enumResTypeProc, Pointer pointer) {
        try {
            return (Boolean)this.h.invoke(this, m93, new Object[]{hMODULE, enumResTypeProc, pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE FindFirstFileEx(String string, int n, Pointer pointer, int n2, Pointer pointer2, WinDef.DWORD dWORD) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m94, new Object[]{string, n, pointer, n2, pointer2, dWORD});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer GetProcAddress(WinDef.HMODULE hMODULE, int n) throws LastErrorException {
        try {
            return (Pointer)this.h.invoke(this, m95, new Object[]{hMODULE, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean ReleaseMutex(WinNT.HANDLE hANDLE) {
        try {
            return (Boolean)this.h.invoke(this, m96, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int QueryDosDevice(String string, char[] cArray, int n) {
        try {
            return (Integer)this.h.invoke(this, m97, new Object[]{string, cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetProcessTimes(WinNT.HANDLE hANDLE, WinBase.FILETIME fILETIME, WinBase.FILETIME fILETIME2, WinBase.FILETIME fILETIME3, WinBase.FILETIME fILETIME4) {
        try {
            return (Boolean)this.h.invoke(this, m98, new Object[]{hANDLE, fILETIME, fILETIME2, fILETIME3, fILETIME4});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE FindFirstVolume(char[] cArray, int n) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m99, new Object[]{cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer VirtualAllocEx(WinNT.HANDLE hANDLE, Pointer pointer, BaseTSD.SIZE_T sIZE_T, int n, int n2) {
        try {
            return (Pointer)this.h.invoke(this, m100, new Object[]{hANDLE, pointer, sIZE_T, n, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean VirtualLock(Pointer pointer, BaseTSD.SIZE_T sIZE_T) {
        try {
            return (Boolean)this.h.invoke(this, m101, new Object[]{pointer, sIZE_T});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetCommState(WinNT.HANDLE hANDLE, WinBase.DCB dCB) {
        try {
            return (Boolean)this.h.invoke(this, m102, new Object[]{hANDLE, dCB});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetCommTimeouts(WinNT.HANDLE hANDLE, WinBase.COMMTIMEOUTS cOMMTIMEOUTS) {
        try {
            return (Boolean)this.h.invoke(this, m103, new Object[]{hANDLE, cOMMTIMEOUTS});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean FreeLibrary(WinDef.HMODULE hMODULE) {
        try {
            return (Boolean)this.h.invoke(this, m104, new Object[]{hMODULE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean Module32FirstW(WinNT.HANDLE hANDLE, Tlhelp32.MODULEENTRY32W mODULEENTRY32W) {
        try {
            return (Boolean)this.h.invoke(this, m105, new Object[]{hANDLE, mODULEENTRY32W});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetVolumeLabel(String string, String string2) {
        try {
            return (Boolean)this.h.invoke(this, m106, new Object[]{string, string2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer LockResource(WinNT.HANDLE hANDLE) {
        try {
            return (Pointer)this.h.invoke(this, m107, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int SizeofResource(WinDef.HMODULE hMODULE, WinNT.HANDLE hANDLE) {
        try {
            return (Integer)this.h.invoke(this, m108, new Object[]{hMODULE, hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetThreadPriority(WinNT.HANDLE hANDLE, int n) {
        try {
            return (Boolean)this.h.invoke(this, m109, new Object[]{hANDLE, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.HRSRC FindResource(WinDef.HMODULE hMODULE, Pointer pointer, Pointer pointer2) {
        try {
            return (WinDef.HRSRC)this.h.invoke(this, m110, new Object[]{hMODULE, pointer, pointer2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int SetErrorMode(int n) {
        try {
            return (Integer)this.h.invoke(this, m111, new Object[]{n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean VirtualFreeEx(WinNT.HANDLE hANDLE, Pointer pointer, BaseTSD.SIZE_T sIZE_T, int n) {
        try {
            return (Boolean)this.h.invoke(this, m112, new Object[]{hANDLE, pointer, sIZE_T, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetCommTimeouts(WinNT.HANDLE hANDLE, WinBase.COMMTIMEOUTS cOMMTIMEOUTS) {
        try {
            return (Boolean)this.h.invoke(this, m113, new Object[]{hANDLE, cOMMTIMEOUTS});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final BaseTSD.SIZE_T VirtualQueryEx(WinNT.HANDLE hANDLE, Pointer pointer, WinNT.MEMORY_BASIC_INFORMATION mEMORY_BASIC_INFORMATION, BaseTSD.SIZE_T sIZE_T) {
        try {
            return (BaseTSD.SIZE_T)this.h.invoke(this, m114, new Object[]{hANDLE, pointer, mEMORY_BASIC_INFORMATION, sIZE_T});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean FindVolumeClose(WinNT.HANDLE hANDLE) {
        try {
            return (Boolean)this.h.invoke(this, m115, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean ReadProcessMemory(WinNT.HANDLE hANDLE, Pointer pointer, Pointer pointer2, int n, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m116, new Object[]{hANDLE, pointer, pointer2, n, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetCommState(WinNT.HANDLE hANDLE, WinBase.DCB dCB) {
        try {
            return (Boolean)this.h.invoke(this, m117, new Object[]{hANDLE, dCB});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE CreateMutex(WinBase.SECURITY_ATTRIBUTES sECURITY_ATTRIBUTES, boolean bl, String string) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m118, new Object[]{sECURITY_ATTRIBUTES, bl, string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.DWORD GetPriorityClass(WinNT.HANDLE hANDLE) {
        try {
            return (WinDef.DWORD)this.h.invoke(this, m119, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.HMODULE LoadLibraryEx(String string, WinNT.HANDLE hANDLE, int n) {
        try {
            return (WinDef.HMODULE)this.h.invoke(this, m120, new Object[]{string, hANDLE, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE CreateRemoteThread(WinNT.HANDLE hANDLE, WinBase.SECURITY_ATTRIBUTES sECURITY_ATTRIBUTES, int n, Pointer pointer, Pointer pointer2, int n2, WinDef.DWORDByReference dWORDByReference) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m121, new Object[]{hANDLE, sECURITY_ATTRIBUTES, n, pointer, pointer2, n2, dWORDByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE CreateRemoteThread(WinNT.HANDLE hANDLE, WinBase.SECURITY_ATTRIBUTES sECURITY_ATTRIBUTES, int n, WinBase.FOREIGN_THREAD_START_ROUTINE fOREIGN_THREAD_START_ROUTINE, Pointer pointer, WinDef.DWORD dWORD, Pointer pointer2) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m122, new Object[]{hANDLE, sECURITY_ATTRIBUTES, n, fOREIGN_THREAD_START_ROUTINE, pointer, dWORD, pointer2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean Module32NextW(WinNT.HANDLE hANDLE, Tlhelp32.MODULEENTRY32W mODULEENTRY32W) {
        try {
            return (Boolean)this.h.invoke(this, m123, new Object[]{hANDLE, mODULEENTRY32W});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean VirtualUnlock(Pointer pointer, BaseTSD.SIZE_T sIZE_T) {
        try {
            return (Boolean)this.h.invoke(this, m124, new Object[]{pointer, sIZE_T});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int WaitForSingleObject(WinNT.HANDLE hANDLE, int n) {
        try {
            return (Integer)this.h.invoke(this, m125, new Object[]{hANDLE, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int WaitForMultipleObjects(int n, WinNT.HANDLE[] hANDLEArray, boolean bl, int n2) {
        try {
            return (Integer)this.h.invoke(this, m126, new Object[]{n, hANDLEArray, bl, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetProcessAffinityMask(WinNT.HANDLE hANDLE, BaseTSD.ULONG_PTR uLONG_PTR) {
        try {
            return (Boolean)this.h.invoke(this, m127, new Object[]{hANDLE, uLONG_PTR});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetProcessAffinityMask(WinNT.HANDLE hANDLE, BaseTSD.ULONG_PTRByReference uLONG_PTRByReference, BaseTSD.ULONG_PTRByReference uLONG_PTRByReference2) {
        try {
            return (Boolean)this.h.invoke(this, m128, new Object[]{hANDLE, uLONG_PTRByReference, uLONG_PTRByReference2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GlobalMemoryStatusEx(WinBase.MEMORYSTATUSEX mEMORYSTATUSEX) {
        try {
            return (Boolean)this.h.invoke(this, m129, new Object[]{mEMORYSTATUSEX});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetFileInformationByHandle(WinNT.HANDLE hANDLE, int n, Pointer pointer, WinDef.DWORD dWORD) {
        try {
            return (Boolean)this.h.invoke(this, m130, new Object[]{hANDLE, n, pointer, dWORD});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.DWORD GetLogicalDriveStrings(WinDef.DWORD dWORD, char[] cArray) {
        try {
            return (WinDef.DWORD)this.h.invoke(this, m131, new Object[]{dWORD, cArray});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean QueryFullProcessImageName(WinNT.HANDLE hANDLE, int n, char[] cArray, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m132, new Object[]{hANDLE, n, cArray, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean DisconnectNamedPipe(WinNT.HANDLE hANDLE) {
        try {
            return (Boolean)this.h.invoke(this, m133, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetNamedPipeHandleState(WinNT.HANDLE hANDLE, IntByReference intByReference, IntByReference intByReference2, IntByReference intByReference3, IntByReference intByReference4, char[] cArray, int n) {
        try {
            return (Boolean)this.h.invoke(this, m134, new Object[]{hANDLE, intByReference, intByReference2, intByReference3, intByReference4, cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetEnvironmentVariable(String string, String string2) {
        try {
            return (Boolean)this.h.invoke(this, m135, new Object[]{string, string2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.DWORD GetPrivateProfileSection(String string, char[] cArray, WinDef.DWORD dWORD, String string2) {
        try {
            return (WinDef.DWORD)this.h.invoke(this, m136, new Object[]{string, cArray, dWORD, string2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int SetThreadExecutionState(int n) {
        try {
            return (Integer)this.h.invoke(this, m137, new Object[]{n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean WritePrivateProfileString(String string, String string2, String string3, String string4) {
        try {
            return (Boolean)this.h.invoke(this, m138, new Object[]{string, string2, string3, string4});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean FileTimeToSystemTime(WinBase.FILETIME fILETIME, WinBase.SYSTEMTIME sYSTEMTIME) {
        try {
            return (Boolean)this.h.invoke(this, m139, new Object[]{fILETIME, sYSTEMTIME});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HRESULT RegisterApplicationRestart(char[] cArray, int n) {
        try {
            return (WinNT.HRESULT)this.h.invoke(this, m140, new Object[]{cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean FileTimeToLocalFileTime(WinBase.FILETIME fILETIME, WinBase.FILETIME fILETIME2) {
        try {
            return (Boolean)this.h.invoke(this, m141, new Object[]{fILETIME, fILETIME2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int ExpandEnvironmentStrings(String string, Pointer pointer, int n) {
        try {
            return (Integer)this.h.invoke(this, m142, new Object[]{string, pointer, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.LCID GetSystemDefaultLCID() {
        try {
            return (WinDef.LCID)this.h.invoke(this, m143, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean WritePrivateProfileSection(String string, String string2, String string3) {
        try {
            return (Boolean)this.h.invoke(this, m144, new Object[]{string, string2, string3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean FindNextVolumeMountPoint(WinNT.HANDLE hANDLE, char[] cArray, int n) {
        try {
            return (Boolean)this.h.invoke(this, m145, new Object[]{hANDLE, cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetVolumeMountPoint(String string, String string2) {
        try {
            return (Boolean)this.h.invoke(this, m146, new Object[]{string, string2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean FindVolumeMountPointClose(WinNT.HANDLE hANDLE) {
        try {
            return (Boolean)this.h.invoke(this, m147, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean DeleteVolumeMountPoint(String string) {
        try {
            return (Boolean)this.h.invoke(this, m148, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean IsProcessorFeaturePresent(int n) {
        try {
            return (Boolean)this.h.invoke(this, m149, new Object[]{n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE FindFirstVolumeMountPoint(String string, char[] cArray, int n) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m150, new Object[]{string, cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetEnvironmentVariable(String string, char[] cArray, int n) {
        try {
            return (Integer)this.h.invoke(this, m151, new Object[]{string, cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SystemTimeToFileTime(WinBase.SYSTEMTIME sYSTEMTIME, WinBase.FILETIME fILETIME) {
        try {
            return (Boolean)this.h.invoke(this, m152, new Object[]{sYSTEMTIME, fILETIME});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetPrivateProfileInt(String string, String string2, int n, String string3) {
        try {
            return (Integer)this.h.invoke(this, m153, new Object[]{string, string2, n, string3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetHandleInformation(WinNT.HANDLE hANDLE, int n, int n2) {
        try {
            return (Boolean)this.h.invoke(this, m154, new Object[]{hANDLE, n, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.DWORD GetPrivateProfileString(String string, String string2, String string3, char[] cArray, WinDef.DWORD dWORD, String string4) {
        try {
            return (WinDef.DWORD)this.h.invoke(this, m155, new Object[]{string, string2, string3, cArray, dWORD, string4});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetProcessIoCounters(WinNT.HANDLE hANDLE, WinNT.IO_COUNTERS iO_COUNTERS) {
        try {
            return (Boolean)this.h.invoke(this, m156, new Object[]{hANDLE, iO_COUNTERS});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetNamedPipeHandleState(WinNT.HANDLE hANDLE, IntByReference intByReference, IntByReference intByReference2, IntByReference intByReference3) {
        try {
            return (Boolean)this.h.invoke(this, m157, new Object[]{hANDLE, intByReference, intByReference2, intByReference3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer GetEnvironmentStrings() {
        try {
            return (Pointer)this.h.invoke(this, m158, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean FreeEnvironmentStrings(Pointer pointer) {
        try {
            return (Boolean)this.h.invoke(this, m159, new Object[]{pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean ProcessIdToSessionId(int n, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m160, new Object[]{n, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE OpenMutex(int n, boolean bl, String string) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m161, new Object[]{n, bl, string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean CopyFile(String string, String string2, boolean bl) {
        try {
            return (Boolean)this.h.invoke(this, m162, new Object[]{string, string2, bl});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.DWORD GetVersion() {
        try {
            return (WinDef.DWORD)this.h.invoke(this, m163, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean MoveFile(String string, String string2) {
        try {
            return (Boolean)this.h.invoke(this, m164, new Object[]{string, string2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetEvent(WinNT.HANDLE hANDLE) {
        try {
            return (Boolean)this.h.invoke(this, m165, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer LocalAlloc(int n, int n2) {
        try {
            return (Pointer)this.h.invoke(this, m166, new Object[]{n, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE OpenEvent(int n, boolean bl, String string) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m167, new Object[]{n, bl, string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE OpenThread(int n, boolean bl, int n2) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m168, new Object[]{n, bl, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean CreatePipe(WinNT.HANDLEByReference hANDLEByReference, WinNT.HANDLEByReference hANDLEByReference2, WinBase.SECURITY_ATTRIBUTES sECURITY_ATTRIBUTES, int n) {
        try {
            return (Boolean)this.h.invoke(this, m169, new Object[]{hANDLEByReference, hANDLEByReference2, sECURITY_ATTRIBUTES, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean ReadFile(WinNT.HANDLE hANDLE, byte[] byArray, int n, IntByReference intByReference, WinBase.OVERLAPPED oVERLAPPED) {
        try {
            return (Boolean)this.h.invoke(this, m170, new Object[]{hANDLE, byArray, n, intByReference, oVERLAPPED});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean WriteFile(WinNT.HANDLE hANDLE, byte[] byArray, int n, IntByReference intByReference, WinBase.OVERLAPPED oVERLAPPED) {
        try {
            return (Boolean)this.h.invoke(this, m171, new Object[]{hANDLE, byArray, n, intByReference, oVERLAPPED});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean ResetEvent(WinNT.HANDLE hANDLE) {
        try {
            return (Boolean)this.h.invoke(this, m172, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean PulseEvent(WinNT.HANDLE hANDLE) {
        try {
            return (Boolean)this.h.invoke(this, m173, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Pointer GlobalFree(Pointer pointer) {
        try {
            return (Pointer)this.h.invoke(this, m174, new Object[]{pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE CreateToolhelp32Snapshot(WinDef.DWORD dWORD, WinDef.DWORD dWORD2) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m175, new Object[]{dWORD, dWORD2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void GetNativeSystemInfo(WinBase.SYSTEM_INFO sYSTEM_INFO) {
        try {
            this.h.invoke(this, m176, new Object[]{sYSTEM_INFO});
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
    public final int GetCurrentProcessId() {
        try {
            return (Integer)this.h.invoke(this, m177, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final long GetTickCount64() {
        try {
            return (Long)this.h.invoke(this, m178, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetTickCount() {
        try {
            return (Integer)this.h.invoke(this, m179, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean Process32Next(WinNT.HANDLE hANDLE, Tlhelp32.PROCESSENTRY32 pROCESSENTRY32) {
        try {
            return (Boolean)this.h.invoke(this, m180, new Object[]{hANDLE, pROCESSENTRY32});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean IsWow64Process(WinNT.HANDLE hANDLE, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m181, new Object[]{hANDLE, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetCurrentThreadId() {
        try {
            return (Integer)this.h.invoke(this, m182, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetLastError() {
        try {
            return (Integer)this.h.invoke(this, m183, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final long VerSetConditionMask(long l, int n, byte by) {
        try {
            return (Long)this.h.invoke(this, m184, new Object[]{l, n, by});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetNumberOfConsoleInputEvents(WinNT.HANDLE hANDLE, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m185, new Object[]{hANDLE, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetNumberOfConsoleMouseButtons(IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m186, new Object[]{intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean AttachConsole(int n) {
        try {
            return (Boolean)this.h.invoke(this, m187, new Object[]{n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetConsoleCP() {
        try {
            return (Integer)this.h.invoke(this, m188, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetConsoleCP(int n) {
        try {
            return (Boolean)this.h.invoke(this, m189, new Object[]{n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinDef.HWND GetConsoleWindow() {
        try {
            return (WinDef.HWND)this.h.invoke(this, m190, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean AllocConsole() {
        try {
            return (Boolean)this.h.invoke(this, m191, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE GetStdHandle(int n) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m192, new Object[]{n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetConsoleOutputCP() {
        try {
            return (Integer)this.h.invoke(this, m193, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetStdHandle(int n, WinNT.HANDLE hANDLE) {
        try {
            return (Boolean)this.h.invoke(this, m194, new Object[]{n, hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetConsoleOutputCP(int n) {
        try {
            return (Boolean)this.h.invoke(this, m195, new Object[]{n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean FreeConsole() {
        try {
            return (Boolean)this.h.invoke(this, m196, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetConsoleMode(WinNT.HANDLE hANDLE, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m197, new Object[]{hANDLE, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean ReadConsoleInput(WinNT.HANDLE hANDLE, Wincon.INPUT_RECORD[] iNPUT_RECORDArray, int n, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m198, new Object[]{hANDLE, iNPUT_RECORDArray, n, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetConsoleTitle(char[] cArray, int n) {
        try {
            return (Integer)this.h.invoke(this, m199, new Object[]{cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetConsoleTitle(String string) {
        try {
            return (Boolean)this.h.invoke(this, m200, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean WriteConsole(WinNT.HANDLE hANDLE, String string, int n, IntByReference intByReference, WinDef.LPVOID lPVOID) {
        try {
            return (Boolean)this.h.invoke(this, m201, new Object[]{hANDLE, string, n, intByReference, lPVOID});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetConsoleMode(WinNT.HANDLE hANDLE, int n) {
        try {
            return (Boolean)this.h.invoke(this, m202, new Object[]{hANDLE, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean FlushConsoleInputBuffer(WinNT.HANDLE hANDLE) {
        try {
            return (Boolean)this.h.invoke(this, m203, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetConsoleDisplayMode(IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m204, new Object[]{intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GenerateConsoleCtrlEvent(int n, int n2) {
        try {
            return (Boolean)this.h.invoke(this, m205, new Object[]{n, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetConsoleOriginalTitle(char[] cArray, int n) {
        try {
            return (Integer)this.h.invoke(this, m206, new Object[]{cArray, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetConsoleScreenBufferInfo(WinNT.HANDLE hANDLE, Wincon.CONSOLE_SCREEN_BUFFER_INFO cONSOLE_SCREEN_BUFFER_INFO) {
        try {
            return (Boolean)this.h.invoke(this, m207, new Object[]{hANDLE, cONSOLE_SCREEN_BUFFER_INFO});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    static {
        ClassLoader classLoader = $Proxy13.class.getClassLoader();
        try {
            m0 = Class.forName("java.lang.Object", false, classLoader).getMethod("hashCode", new Class[0]);
            m1 = Class.forName("java.lang.Object", false, classLoader).getMethod("equals", Class.forName("java.lang.Object", false, classLoader));
            m2 = Class.forName("java.lang.Object", false, classLoader).getMethod("toString", new Class[0]);
            m3 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("CreateFile", Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinBase$SECURITY_ATTRIBUTES", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m4 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetFileTime", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$FILETIME", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$FILETIME", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$FILETIME", false, classLoader));
            m5 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("CloseHandle", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m6 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("FindFirstFile", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m7 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("FindClose", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m8 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("DeleteFile", Class.forName("java.lang.String", false, classLoader));
            m9 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("CreateDirectory", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$SECURITY_ATTRIBUTES", false, classLoader));
            m10 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("CreateEvent", Class.forName("com.sun.jna.platform.win32.WinBase$SECURITY_ATTRIBUTES", false, classLoader), Boolean.TYPE, Boolean.TYPE, Class.forName("java.lang.String", false, classLoader));
            m11 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("FindNextFile", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m12 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("MoveFileEx", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader));
            m13 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetFileAttributes", Class.forName("java.lang.String", false, classLoader));
            m14 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetFileAttributes", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader));
            m15 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetVolumeInformation", Class.forName("java.lang.String", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE);
            m16 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetDriveType", Class.forName("java.lang.String", false, classLoader));
            m17 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetDiskFreeSpaceEx", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$LARGE_INTEGER", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$LARGE_INTEGER", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$LARGE_INTEGER", false, classLoader));
            m18 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetDiskFreeSpace", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORDByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORDByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORDByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORDByReference", false, classLoader));
            m19 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetVolumePathName", Class.forName("java.lang.String", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE);
            m20 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetCurrentProcess", new Class[0]);
            m21 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetCurrentThread", new Class[0]);
            m22 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("FormatMessage", Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader));
            m23 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("LocalFree", Class.forName("com.sun.jna.Pointer", false, classLoader));
            m24 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("CreateIoCompletionPort", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE);
            m25 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetQueuedCompletionStatus", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.BaseTSD$ULONG_PTRByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Integer.TYPE);
            m26 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("PostQueuedCompletionStatus", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$OVERLAPPED", false, classLoader));
            m27 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("ReadDirectoryChangesW", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$FILE_NOTIFY_INFORMATION", false, classLoader), Integer.TYPE, Boolean.TYPE, Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$OVERLAPPED", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$OVERLAPPED_COMPLETION_ROUTINE", false, classLoader));
            m28 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("UnregisterApplicationRestart", new Class[0]);
            m29 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetVolumePathNamesForVolumeName", Class.forName("java.lang.String", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m30 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetNamedPipeServerSessionId", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$ULONGByReference", false, classLoader));
            m31 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetPrivateProfileSectionNames", Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m32 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetNamedPipeClientSessionId", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$ULONGByReference", false, classLoader));
            m33 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetNamedPipeClientComputerName", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE);
            m34 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetLogicalProcessorInformationEx", Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORDByReference", false, classLoader));
            m35 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetVolumeNameForVolumeMountPoint", Class.forName("java.lang.String", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE);
            m36 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SystemTimeToTzSpecificLocalTime", Class.forName("com.sun.jna.platform.win32.WinBase$TIME_ZONE_INFORMATION", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$SYSTEMTIME", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$SYSTEMTIME", false, classLoader));
            m37 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetApplicationRestartSettings", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m38 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetLogicalProcessorInformation", Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORDByReference", false, classLoader));
            m39 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetFileInformationByHandleEx", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader));
            m40 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetNamedPipeClientProcessId", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$ULONGByReference", false, classLoader));
            m41 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetNamedPipeServerProcessId", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$ULONGByReference", false, classLoader));
            m42 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("VerifyVersionInfoW", Class.forName("com.sun.jna.platform.win32.WinNT$OSVERSIONINFOEX", false, classLoader), Integer.TYPE, Long.TYPE);
            m43 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("MapViewOfFile", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            m44 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("TerminateProcess", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE);
            m45 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetComputerName", Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m46 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("CreateFileMapping", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$SECURITY_ATTRIBUTES", false, classLoader), Integer.TYPE, Integer.TYPE, Integer.TYPE, Class.forName("java.lang.String", false, classLoader));
            m47 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetModuleHandle", Class.forName("java.lang.String", false, classLoader));
            m48 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetSystemTime", Class.forName("com.sun.jna.platform.win32.WinBase$SYSTEMTIME", false, classLoader));
            m49 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("DuplicateHandle", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$HANDLEByReference", false, classLoader), Integer.TYPE, Boolean.TYPE, Integer.TYPE);
            m50 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetShortPathName", Class.forName("java.lang.String", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE);
            m51 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetSystemTime", Class.forName("com.sun.jna.platform.win32.WinBase$SYSTEMTIME", false, classLoader));
            m52 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetComputerNameEx", Integer.TYPE, Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m53 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("CreateProcess", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$SECURITY_ATTRIBUTES", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$SECURITY_ATTRIBUTES", false, classLoader), Boolean.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$STARTUPINFO", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$PROCESS_INFORMATION", false, classLoader));
            m54 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("OpenProcess", Integer.TYPE, Boolean.TYPE, Integer.TYPE);
            m55 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("FlushFileBuffers", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m56 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("OpenFileMapping", Integer.TYPE, Boolean.TYPE, Class.forName("java.lang.String", false, classLoader));
            m57 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetTempPath", Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader), Class.forName("[C", false, classLoader));
            m58 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetVersionEx", Class.forName("com.sun.jna.platform.win32.WinNT$OSVERSIONINFO", false, classLoader));
            m59 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetVersionEx", Class.forName("com.sun.jna.platform.win32.WinNT$OSVERSIONINFOEX", false, classLoader));
            m60 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetSystemInfo", Class.forName("com.sun.jna.platform.win32.WinBase$SYSTEM_INFO", false, classLoader));
            m61 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetFileTime", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$FILETIME", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$FILETIME", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$FILETIME", false, classLoader));
            m62 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("CallNamedPipe", Class.forName("java.lang.String", false, classLoader), Class.forName("[B", false, classLoader), Integer.TYPE, Class.forName("[B", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Integer.TYPE);
            m63 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetExitCodeProcess", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m64 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("ConnectNamedPipe", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$OVERLAPPED", false, classLoader));
            m65 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetLocalTime", Class.forName("com.sun.jna.platform.win32.WinBase$SYSTEMTIME", false, classLoader));
            m66 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetProcessId", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m67 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetSystemTimes", Class.forName("com.sun.jna.platform.win32.WinBase$FILETIME", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$FILETIME", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$FILETIME", false, classLoader));
            m68 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("UnmapViewOfFile", Class.forName("com.sun.jna.Pointer", false, classLoader));
            m69 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetNamedPipeInfo", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m70 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("PeekNamedPipe", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("[B", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m71 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("TransactNamedPipe", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("[B", false, classLoader), Integer.TYPE, Class.forName("[B", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$OVERLAPPED", false, classLoader));
            m72 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("WaitNamedPipe", Class.forName("java.lang.String", false, classLoader), Integer.TYPE);
            m73 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetLastError", Integer.TYPE);
            m74 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetFileType", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m75 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("CreateNamedPipe", Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinBase$SECURITY_ATTRIBUTES", false, classLoader));
            m76 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("DeviceIoControl", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m77 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("Process32First", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.Tlhelp32$PROCESSENTRY32", false, classLoader));
            m78 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("CreateProcessW", Class.forName("java.lang.String", false, classLoader), Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$SECURITY_ATTRIBUTES", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$SECURITY_ATTRIBUTES", false, classLoader), Boolean.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$STARTUPINFO", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$PROCESS_INFORMATION", false, classLoader));
            m79 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("Thread32First", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.Tlhelp32$THREADENTRY32", false, classLoader));
            m80 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("Thread32Next", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.Tlhelp32$THREADENTRY32", false, classLoader));
            m81 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetUserDefaultLCID", new Class[0]);
            m82 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetLocalTime", Class.forName("com.sun.jna.platform.win32.WinBase$SYSTEMTIME", false, classLoader));
            m83 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetProcessVersion", Integer.TYPE);
            m84 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetThreadPriority", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m85 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("DefineDosDevice", Integer.TYPE, Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m86 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("WriteProcessMemory", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m87 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("FindNextVolume", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE);
            m88 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetExitCodeThread", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m89 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("ExitProcess", Integer.TYPE);
            m90 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetPriorityClass", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader));
            m91 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("EnumResourceNames", Class.forName("com.sun.jna.platform.win32.WinDef$HMODULE", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$EnumResNameProc", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m92 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("LoadResource", Class.forName("com.sun.jna.platform.win32.WinDef$HMODULE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$HRSRC", false, classLoader));
            m93 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("EnumResourceTypes", Class.forName("com.sun.jna.platform.win32.WinDef$HMODULE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$EnumResTypeProc", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m94 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("FindFirstFileEx", Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader));
            m95 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetProcAddress", Class.forName("com.sun.jna.platform.win32.WinDef$HMODULE", false, classLoader), Integer.TYPE);
            m96 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("ReleaseMutex", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m97 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("QueryDosDevice", Class.forName("java.lang.String", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE);
            m98 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetProcessTimes", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$FILETIME", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$FILETIME", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$FILETIME", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$FILETIME", false, classLoader));
            m99 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("FindFirstVolume", Class.forName("[C", false, classLoader), Integer.TYPE);
            m100 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("VirtualAllocEx", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.platform.win32.BaseTSD$SIZE_T", false, classLoader), Integer.TYPE, Integer.TYPE);
            m101 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("VirtualLock", Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.platform.win32.BaseTSD$SIZE_T", false, classLoader));
            m102 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetCommState", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$DCB", false, classLoader));
            m103 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetCommTimeouts", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$COMMTIMEOUTS", false, classLoader));
            m104 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("FreeLibrary", Class.forName("com.sun.jna.platform.win32.WinDef$HMODULE", false, classLoader));
            m105 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("Module32FirstW", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.Tlhelp32$MODULEENTRY32W", false, classLoader));
            m106 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetVolumeLabel", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m107 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("LockResource", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m108 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SizeofResource", Class.forName("com.sun.jna.platform.win32.WinDef$HMODULE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m109 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetThreadPriority", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE);
            m110 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("FindResource", Class.forName("com.sun.jna.platform.win32.WinDef$HMODULE", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m111 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetErrorMode", Integer.TYPE);
            m112 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("VirtualFreeEx", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.platform.win32.BaseTSD$SIZE_T", false, classLoader), Integer.TYPE);
            m113 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetCommTimeouts", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$COMMTIMEOUTS", false, classLoader));
            m114 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("VirtualQueryEx", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$MEMORY_BASIC_INFORMATION", false, classLoader), Class.forName("com.sun.jna.platform.win32.BaseTSD$SIZE_T", false, classLoader));
            m115 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("FindVolumeClose", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m116 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("ReadProcessMemory", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m117 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetCommState", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$DCB", false, classLoader));
            m118 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("CreateMutex", Class.forName("com.sun.jna.platform.win32.WinBase$SECURITY_ATTRIBUTES", false, classLoader), Boolean.TYPE, Class.forName("java.lang.String", false, classLoader));
            m119 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetPriorityClass", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m120 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("LoadLibraryEx", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE);
            m121 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("CreateRemoteThread", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$SECURITY_ATTRIBUTES", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinDef$DWORDByReference", false, classLoader));
            m122 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("CreateRemoteThread", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$SECURITY_ATTRIBUTES", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinBase$FOREIGN_THREAD_START_ROUTINE", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m123 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("Module32NextW", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.Tlhelp32$MODULEENTRY32W", false, classLoader));
            m124 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("VirtualUnlock", Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.platform.win32.BaseTSD$SIZE_T", false, classLoader));
            m125 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("WaitForSingleObject", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE);
            m126 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("WaitForMultipleObjects", Integer.TYPE, Class.forName("[Lcom.sun.jna.platform.win32.WinNT$HANDLE;", false, classLoader), Boolean.TYPE, Integer.TYPE);
            m127 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetProcessAffinityMask", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.BaseTSD$ULONG_PTR", false, classLoader));
            m128 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetProcessAffinityMask", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.BaseTSD$ULONG_PTRByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.BaseTSD$ULONG_PTRByReference", false, classLoader));
            m129 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GlobalMemoryStatusEx", Class.forName("com.sun.jna.platform.win32.WinBase$MEMORYSTATUSEX", false, classLoader));
            m130 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetFileInformationByHandle", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader));
            m131 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetLogicalDriveStrings", Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader), Class.forName("[C", false, classLoader));
            m132 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("QueryFullProcessImageName", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE, Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m133 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("DisconnectNamedPipe", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m134 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetNamedPipeHandleState", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE);
            m135 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetEnvironmentVariable", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m136 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetPrivateProfileSection", Class.forName("java.lang.String", false, classLoader), Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m137 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetThreadExecutionState", Integer.TYPE);
            m138 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("WritePrivateProfileString", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m139 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("FileTimeToSystemTime", Class.forName("com.sun.jna.platform.win32.WinBase$FILETIME", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$SYSTEMTIME", false, classLoader));
            m140 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("RegisterApplicationRestart", Class.forName("[C", false, classLoader), Integer.TYPE);
            m141 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("FileTimeToLocalFileTime", Class.forName("com.sun.jna.platform.win32.WinBase$FILETIME", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$FILETIME", false, classLoader));
            m142 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("ExpandEnvironmentStrings", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE);
            m143 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetSystemDefaultLCID", new Class[0]);
            m144 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("WritePrivateProfileSection", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m145 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("FindNextVolumeMountPoint", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE);
            m146 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetVolumeMountPoint", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m147 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("FindVolumeMountPointClose", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m148 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("DeleteVolumeMountPoint", Class.forName("java.lang.String", false, classLoader));
            m149 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("IsProcessorFeaturePresent", Integer.TYPE);
            m150 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("FindFirstVolumeMountPoint", Class.forName("java.lang.String", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE);
            m151 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetEnvironmentVariable", Class.forName("java.lang.String", false, classLoader), Class.forName("[C", false, classLoader), Integer.TYPE);
            m152 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SystemTimeToFileTime", Class.forName("com.sun.jna.platform.win32.WinBase$SYSTEMTIME", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$FILETIME", false, classLoader));
            m153 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetPrivateProfileInt", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("java.lang.String", false, classLoader));
            m154 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetHandleInformation", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE, Integer.TYPE);
            m155 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetPrivateProfileString", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m156 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetProcessIoCounters", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$IO_COUNTERS", false, classLoader));
            m157 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetNamedPipeHandleState", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m158 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetEnvironmentStrings", new Class[0]);
            m159 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("FreeEnvironmentStrings", Class.forName("com.sun.jna.Pointer", false, classLoader));
            m160 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("ProcessIdToSessionId", Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m161 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("OpenMutex", Integer.TYPE, Boolean.TYPE, Class.forName("java.lang.String", false, classLoader));
            m162 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("CopyFile", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Boolean.TYPE);
            m163 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetVersion", new Class[0]);
            m164 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("MoveFile", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m165 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetEvent", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m166 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("LocalAlloc", Integer.TYPE, Integer.TYPE);
            m167 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("OpenEvent", Integer.TYPE, Boolean.TYPE, Class.forName("java.lang.String", false, classLoader));
            m168 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("OpenThread", Integer.TYPE, Boolean.TYPE, Integer.TYPE);
            m169 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("CreatePipe", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLEByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$HANDLEByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$SECURITY_ATTRIBUTES", false, classLoader), Integer.TYPE);
            m170 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("ReadFile", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("[B", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$OVERLAPPED", false, classLoader));
            m171 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("WriteFile", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("[B", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$OVERLAPPED", false, classLoader));
            m172 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("ResetEvent", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m173 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("PulseEvent", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m174 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GlobalFree", Class.forName("com.sun.jna.Pointer", false, classLoader));
            m175 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("CreateToolhelp32Snapshot", Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader));
            m176 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetNativeSystemInfo", Class.forName("com.sun.jna.platform.win32.WinBase$SYSTEM_INFO", false, classLoader));
            m177 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetCurrentProcessId", new Class[0]);
            m178 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetTickCount64", new Class[0]);
            m179 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetTickCount", new Class[0]);
            m180 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("Process32Next", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.Tlhelp32$PROCESSENTRY32", false, classLoader));
            m181 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("IsWow64Process", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m182 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetCurrentThreadId", new Class[0]);
            m183 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetLastError", new Class[0]);
            m184 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("VerSetConditionMask", Long.TYPE, Integer.TYPE, Byte.TYPE);
            m185 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetNumberOfConsoleInputEvents", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m186 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetNumberOfConsoleMouseButtons", Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m187 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("AttachConsole", Integer.TYPE);
            m188 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetConsoleCP", new Class[0]);
            m189 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetConsoleCP", Integer.TYPE);
            m190 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetConsoleWindow", new Class[0]);
            m191 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("AllocConsole", new Class[0]);
            m192 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetStdHandle", Integer.TYPE);
            m193 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetConsoleOutputCP", new Class[0]);
            m194 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetStdHandle", Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m195 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetConsoleOutputCP", Integer.TYPE);
            m196 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("FreeConsole", new Class[0]);
            m197 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetConsoleMode", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m198 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("ReadConsoleInput", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("[Lcom.sun.jna.platform.win32.Wincon$INPUT_RECORD;", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m199 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetConsoleTitle", Class.forName("[C", false, classLoader), Integer.TYPE);
            m200 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetConsoleTitle", Class.forName("java.lang.String", false, classLoader));
            m201 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("WriteConsole", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$LPVOID", false, classLoader));
            m202 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("SetConsoleMode", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE);
            m203 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("FlushConsoleInputBuffer", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m204 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetConsoleDisplayMode", Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m205 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GenerateConsoleCtrlEvent", Integer.TYPE, Integer.TYPE);
            m206 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetConsoleOriginalTitle", Class.forName("[C", false, classLoader), Integer.TYPE);
            m207 = Class.forName("oshi.jna.platform.windows.Kernel32", false, classLoader).getMethod("GetConsoleScreenBufferInfo", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.Wincon$CONSOLE_SCREEN_BUFFER_INFO", false, classLoader));
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

