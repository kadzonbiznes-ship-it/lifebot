/*
 * Decompiled with CFR 0.152.
 */
package jdk.proxy1;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.Winsvc;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

public final class $Proxy0
extends Proxy
implements Advapi32 {
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

    public $Proxy0(InvocationHandler invocationHandler) {
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
    public final boolean InitializeSecurityDescriptor(WinNT.SECURITY_DESCRIPTOR sECURITY_DESCRIPTOR, int n) {
        try {
            return (Boolean)this.h.invoke(this, m3, new Object[]{sECURITY_DESCRIPTOR, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean InitializeAcl(WinNT.ACL aCL, int n, int n2) {
        try {
            return (Boolean)this.h.invoke(this, m4, new Object[]{aCL, n, n2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetFileSecurity(String string, int n, Pointer pointer, int n2, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m5, new Object[]{string, n, pointer, n2, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetFileSecurity(String string, int n, Pointer pointer) {
        try {
            return (Boolean)this.h.invoke(this, m6, new Object[]{string, n, pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetSecurityDescriptorOwner(WinNT.SECURITY_DESCRIPTOR sECURITY_DESCRIPTOR, WinNT.PSIDByReference pSIDByReference, WinDef.BOOLByReference bOOLByReference) {
        try {
            return (Boolean)this.h.invoke(this, m7, new Object[]{sECURITY_DESCRIPTOR, pSIDByReference, bOOLByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetSecurityDescriptorOwner(WinNT.SECURITY_DESCRIPTOR sECURITY_DESCRIPTOR, WinNT.PSID pSID, boolean bl) {
        try {
            return (Boolean)this.h.invoke(this, m8, new Object[]{sECURITY_DESCRIPTOR, pSID, bl});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetSecurityDescriptorDacl(WinNT.SECURITY_DESCRIPTOR sECURITY_DESCRIPTOR, WinDef.BOOLByReference bOOLByReference, WinNT.PACLByReference pACLByReference, WinDef.BOOLByReference bOOLByReference2) {
        try {
            return (Boolean)this.h.invoke(this, m9, new Object[]{sECURITY_DESCRIPTOR, bOOLByReference, pACLByReference, bOOLByReference2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetSecurityDescriptorDacl(WinNT.SECURITY_DESCRIPTOR sECURITY_DESCRIPTOR, boolean bl, WinNT.ACL aCL, boolean bl2) {
        try {
            return (Boolean)this.h.invoke(this, m10, new Object[]{sECURITY_DESCRIPTOR, bl, aCL, bl2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetAce(WinNT.ACL aCL, int n, PointerByReference pointerByReference) {
        try {
            return (Boolean)this.h.invoke(this, m11, new Object[]{aCL, n, pointerByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean AddAccessAllowedAceEx(WinNT.ACL aCL, int n, int n2, int n3, WinNT.PSID pSID) {
        try {
            return (Boolean)this.h.invoke(this, m12, new Object[]{aCL, n, n2, n3, pSID});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean LookupAccountSid(String string, WinNT.PSID pSID, char[] cArray, IntByReference intByReference, char[] cArray2, IntByReference intByReference2, PointerByReference pointerByReference) {
        try {
            return (Boolean)this.h.invoke(this, m13, new Object[]{string, pSID, cArray, intByReference, cArray2, intByReference2, pointerByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean LookupAccountName(String string, String string2, WinNT.PSID pSID, IntByReference intByReference, char[] cArray, IntByReference intByReference2, PointerByReference pointerByReference) {
        try {
            return (Boolean)this.h.invoke(this, m14, new Object[]{string, string2, pSID, intByReference, cArray, intByReference2, pointerByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetLengthSid(WinNT.PSID pSID) {
        try {
            return (Integer)this.h.invoke(this, m15, new Object[]{pSID});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean ConvertSidToStringSid(WinNT.PSID pSID, PointerByReference pointerByReference) {
        try {
            return (Boolean)this.h.invoke(this, m16, new Object[]{pSID, pointerByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean ConvertStringSidToSid(String string, WinNT.PSIDByReference pSIDByReference) {
        try {
            return (Boolean)this.h.invoke(this, m17, new Object[]{string, pSIDByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean OpenProcessToken(WinNT.HANDLE hANDLE, int n, WinNT.HANDLEByReference hANDLEByReference) {
        try {
            return (Boolean)this.h.invoke(this, m18, new Object[]{hANDLE, n, hANDLEByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean OpenThreadToken(WinNT.HANDLE hANDLE, int n, boolean bl, WinNT.HANDLEByReference hANDLEByReference) {
        try {
            return (Boolean)this.h.invoke(this, m19, new Object[]{hANDLE, n, bl, hANDLEByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean DuplicateTokenEx(WinNT.HANDLE hANDLE, int n, WinBase.SECURITY_ATTRIBUTES sECURITY_ATTRIBUTES, int n2, int n3, WinNT.HANDLEByReference hANDLEByReference) {
        try {
            return (Boolean)this.h.invoke(this, m20, new Object[]{hANDLE, n, sECURITY_ATTRIBUTES, n2, n3, hANDLEByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetThreadToken(WinNT.HANDLEByReference hANDLEByReference, WinNT.HANDLE hANDLE) {
        try {
            return (Boolean)this.h.invoke(this, m21, new Object[]{hANDLEByReference, hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetTokenInformation(WinNT.HANDLE hANDLE, int n, Structure structure, int n2, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m22, new Object[]{hANDLE, n, structure, n2, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean AdjustTokenPrivileges(WinNT.HANDLE hANDLE, boolean bl, WinNT.TOKEN_PRIVILEGES tOKEN_PRIVILEGES, int n, WinNT.TOKEN_PRIVILEGES tOKEN_PRIVILEGES2, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m23, new Object[]{hANDLE, bl, tOKEN_PRIVILEGES, n, tOKEN_PRIVILEGES2, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean AccessCheck(Pointer pointer, WinNT.HANDLE hANDLE, WinDef.DWORD dWORD, WinNT.GENERIC_MAPPING gENERIC_MAPPING, WinNT.PRIVILEGE_SET pRIVILEGE_SET, WinDef.DWORDByReference dWORDByReference, WinDef.DWORDByReference dWORDByReference2, WinDef.BOOLByReference bOOLByReference) {
        try {
            return (Boolean)this.h.invoke(this, m24, new Object[]{pointer, hANDLE, dWORD, gENERIC_MAPPING, pRIVILEGE_SET, dWORDByReference, dWORDByReference2, bOOLByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean LookupPrivilegeValue(String string, String string2, WinNT.LUID lUID) {
        try {
            return (Boolean)this.h.invoke(this, m25, new Object[]{string, string2, lUID});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetSecurityDescriptorGroup(WinNT.SECURITY_DESCRIPTOR sECURITY_DESCRIPTOR, WinNT.PSID pSID, boolean bl) {
        try {
            return (Boolean)this.h.invoke(this, m26, new Object[]{sECURITY_DESCRIPTOR, pSID, bl});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetSecurityDescriptorGroup(WinNT.SECURITY_DESCRIPTOR sECURITY_DESCRIPTOR, WinNT.PSIDByReference pSIDByReference, WinDef.BOOLByReference bOOLByReference) {
        try {
            return (Boolean)this.h.invoke(this, m27, new Object[]{sECURITY_DESCRIPTOR, pSIDByReference, bOOLByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void CloseEncryptedFileRaw(Pointer pointer) {
        try {
            this.h.invoke(this, m28, new Object[]{pointer});
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
    public final int OpenEncryptedFileRaw(String string, WinDef.ULONG uLONG, PointerByReference pointerByReference) {
        try {
            return (Integer)this.h.invoke(this, m29, new Object[]{string, uLONG, pointerByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int WriteEncryptedFileRaw(WinBase.FE_IMPORT_FUNC fE_IMPORT_FUNC, Pointer pointer, Pointer pointer2) {
        try {
            return (Integer)this.h.invoke(this, m30, new Object[]{fE_IMPORT_FUNC, pointer, pointer2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean FileEncryptionStatus(String string, WinDef.DWORDByReference dWORDByReference) {
        try {
            return (Boolean)this.h.invoke(this, m31, new Object[]{string, dWORDByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int SetNamedSecurityInfo(String string, int n, int n2, Pointer pointer, Pointer pointer2, Pointer pointer3, Pointer pointer4) {
        try {
            return (Integer)this.h.invoke(this, m32, new Object[]{string, n, n2, pointer, pointer2, pointer3, pointer4});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int ReadEncryptedFileRaw(WinBase.FE_EXPORT_FUNC fE_EXPORT_FUNC, Pointer pointer, Pointer pointer2) {
        try {
            return (Integer)this.h.invoke(this, m33, new Object[]{fE_EXPORT_FUNC, pointer, pointer2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetNamedSecurityInfo(String string, int n, int n2, PointerByReference pointerByReference, PointerByReference pointerByReference2, PointerByReference pointerByReference3, PointerByReference pointerByReference4, PointerByReference pointerByReference5) {
        try {
            return (Integer)this.h.invoke(this, m34, new Object[]{string, n, n2, pointerByReference, pointerByReference2, pointerByReference3, pointerByReference4, pointerByReference5});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean AddAce(WinNT.ACL aCL, int n, int n2, Pointer pointer, int n3) {
        try {
            return (Boolean)this.h.invoke(this, m35, new Object[]{aCL, n, n2, pointer, n3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean LogonUser(String string, String string2, String string3, int n, int n2, WinNT.HANDLEByReference hANDLEByReference) {
        try {
            return (Boolean)this.h.invoke(this, m36, new Object[]{string, string2, string3, n, n2, hANDLEByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean EqualSid(WinNT.PSID pSID, WinNT.PSID pSID2) {
        try {
            return (Boolean)this.h.invoke(this, m37, new Object[]{pSID, pSID2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetOldestEventLogRecord(WinNT.HANDLE hANDLE, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m38, new Object[]{hANDLE, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean ImpersonateLoggedOnUser(WinNT.HANDLE hANDLE) {
        try {
            return (Boolean)this.h.invoke(this, m39, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean QueryServiceStatusEx(Winsvc.SC_HANDLE sC_HANDLE, int n, Winsvc.SERVICE_STATUS_PROCESS sERVICE_STATUS_PROCESS, int n2, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m40, new Object[]{sC_HANDLE, n, sERVICE_STATUS_PROCESS, n2, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegNotifyChangeKeyValue(WinReg.HKEY hKEY, boolean bl, int n, WinNT.HANDLE hANDLE, boolean bl2) {
        try {
            return (Integer)this.h.invoke(this, m41, new Object[]{hKEY, bl, n, hANDLE, bl2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Winsvc.SERVICE_STATUS_HANDLE RegisterServiceCtrlHandler(String string, Library.Handler handler) {
        try {
            return (Winsvc.SERVICE_STATUS_HANDLE)this.h.invoke(this, m42, new Object[]{string, handler});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean IsValidSecurityDescriptor(Pointer pointer) {
        try {
            return (Boolean)this.h.invoke(this, m43, new Object[]{pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean LookupPrivilegeName(String string, WinNT.LUID lUID, char[] cArray, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m44, new Object[]{string, lUID, cArray, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean ChangeServiceConfig2(Winsvc.SC_HANDLE sC_HANDLE, int n, Winsvc.ChangeServiceConfig2Info changeServiceConfig2Info) {
        try {
            return (Boolean)this.h.invoke(this, m45, new Object[]{sC_HANDLE, n, changeServiceConfig2Info});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean CreateProcessWithLogonW(String string, String string2, String string3, int n, String string4, String string5, int n2, Pointer pointer, String string6, WinBase.STARTUPINFO sTARTUPINFO, WinBase.PROCESS_INFORMATION pROCESS_INFORMATION) {
        try {
            return (Boolean)this.h.invoke(this, m46, new Object[]{string, string2, string3, n, string4, string5, n2, pointer, string6, sTARTUPINFO, pROCESS_INFORMATION});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean DeregisterEventSource(WinNT.HANDLE hANDLE) {
        try {
            return (Boolean)this.h.invoke(this, m47, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetNumberOfEventLogRecords(WinNT.HANDLE hANDLE, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m48, new Object[]{hANDLE, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean EnumDependentServices(Winsvc.SC_HANDLE sC_HANDLE, int n, Pointer pointer, int n2, IntByReference intByReference, IntByReference intByReference2) {
        try {
            return (Boolean)this.h.invoke(this, m49, new Object[]{sC_HANDLE, n, pointer, n2, intByReference, intByReference2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean AddAccessAllowedAce(WinNT.ACL aCL, int n, int n2, WinNT.PSID pSID) {
        try {
            return (Boolean)this.h.invoke(this, m50, new Object[]{aCL, n, n2, pSID});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean EnumServicesStatusEx(Winsvc.SC_HANDLE sC_HANDLE, int n, int n2, int n3, Pointer pointer, int n4, IntByReference intByReference, IntByReference intByReference2, IntByReference intByReference3, String string) {
        try {
            return (Boolean)this.h.invoke(this, m51, new Object[]{sC_HANDLE, n, n2, n3, pointer, n4, intByReference, intByReference2, intByReference3, string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean QueryServiceConfig2(Winsvc.SC_HANDLE sC_HANDLE, int n, Pointer pointer, int n2, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m52, new Object[]{sC_HANDLE, n, pointer, n2, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean StartServiceCtrlDispatcher(Winsvc.SERVICE_TABLE_ENTRY[] sERVICE_TABLE_ENTRYArray) {
        try {
            return (Boolean)this.h.invoke(this, m53, new Object[]{sERVICE_TABLE_ENTRYArray});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE RegisterEventSource(String string, String string2) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m54, new Object[]{string, string2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean CreateProcessAsUser(WinNT.HANDLE hANDLE, String string, String string2, WinBase.SECURITY_ATTRIBUTES sECURITY_ATTRIBUTES, WinBase.SECURITY_ATTRIBUTES sECURITY_ATTRIBUTES2, boolean bl, int n, String string3, String string4, WinBase.STARTUPINFO sTARTUPINFO, WinBase.PROCESS_INFORMATION pROCESS_INFORMATION) {
        try {
            return (Boolean)this.h.invoke(this, m55, new Object[]{hANDLE, string, string2, sECURITY_ATTRIBUTES, sECURITY_ATTRIBUTES2, bl, n, string3, string4, sTARTUPINFO, pROCESS_INFORMATION});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegOpenKeyEx(WinReg.HKEY hKEY, String string, int n, int n2, WinReg.HKEYByReference hKEYByReference) {
        try {
            return (Integer)this.h.invoke(this, m56, new Object[]{hKEY, string, n, n2, hKEYByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegDeleteKey(WinReg.HKEY hKEY, String string) {
        try {
            return (Integer)this.h.invoke(this, m57, new Object[]{hKEY, string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean DuplicateToken(WinNT.HANDLE hANDLE, int n, WinNT.HANDLEByReference hANDLEByReference) {
        try {
            return (Boolean)this.h.invoke(this, m58, new Object[]{hANDLE, n, hANDLEByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean EncryptFile(String string) {
        try {
            return (Boolean)this.h.invoke(this, m59, new Object[]{string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetUserNameW(char[] cArray, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m60, new Object[]{cArray, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final void MapGenericMask(WinDef.DWORDByReference dWORDByReference, WinNT.GENERIC_MAPPING gENERIC_MAPPING) {
        try {
            this.h.invoke(this, m61, new Object[]{dWORDByReference, gENERIC_MAPPING});
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
    public final boolean DecryptFile(String string, WinDef.DWORD dWORD) {
        try {
            return (Boolean)this.h.invoke(this, m62, new Object[]{string, dWORD});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegQueryInfoKey(WinReg.HKEY hKEY, char[] cArray, IntByReference intByReference, IntByReference intByReference2, IntByReference intByReference3, IntByReference intByReference4, IntByReference intByReference5, IntByReference intByReference6, IntByReference intByReference7, IntByReference intByReference8, IntByReference intByReference9, WinBase.FILETIME fILETIME) {
        try {
            return (Integer)this.h.invoke(this, m63, new Object[]{hKEY, cArray, intByReference, intByReference2, intByReference3, intByReference4, intByReference5, intByReference6, intByReference7, intByReference8, intByReference9, fILETIME});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegLoadAppKey(String string, WinReg.HKEYByReference hKEYByReference, int n, int n2, int n3) {
        try {
            return (Integer)this.h.invoke(this, m64, new Object[]{string, hKEYByReference, n, n2, n3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegEnumKeyEx(WinReg.HKEY hKEY, int n, char[] cArray, IntByReference intByReference, IntByReference intByReference2, char[] cArray2, IntByReference intByReference3, WinBase.FILETIME fILETIME) {
        try {
            return (Integer)this.h.invoke(this, m65, new Object[]{hKEY, n, cArray, intByReference, intByReference2, cArray2, intByReference3, fILETIME});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegGetValue(WinReg.HKEY hKEY, String string, String string2, int n, IntByReference intByReference, Pointer pointer, IntByReference intByReference2) {
        try {
            return (Integer)this.h.invoke(this, m66, new Object[]{hKEY, string, string2, n, intByReference, pointer, intByReference2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegGetValue(WinReg.HKEY hKEY, String string, String string2, int n, IntByReference intByReference, byte[] byArray, IntByReference intByReference2) {
        try {
            return (Integer)this.h.invoke(this, m67, new Object[]{hKEY, string, string2, n, intByReference, byArray, intByReference2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegCloseKey(WinReg.HKEY hKEY) {
        try {
            return (Integer)this.h.invoke(this, m68, new Object[]{hKEY});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegDeleteValue(WinReg.HKEY hKEY, String string) {
        try {
            return (Integer)this.h.invoke(this, m69, new Object[]{hKEY, string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegQueryValueEx(WinReg.HKEY hKEY, String string, int n, IntByReference intByReference, LongByReference longByReference, IntByReference intByReference2) {
        try {
            return (Integer)this.h.invoke(this, m70, new Object[]{hKEY, string, n, intByReference, longByReference, intByReference2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegQueryValueEx(WinReg.HKEY hKEY, String string, int n, IntByReference intByReference, char[] cArray, IntByReference intByReference2) {
        try {
            return (Integer)this.h.invoke(this, m71, new Object[]{hKEY, string, n, intByReference, cArray, intByReference2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegQueryValueEx(WinReg.HKEY hKEY, String string, int n, IntByReference intByReference, IntByReference intByReference2, IntByReference intByReference3) {
        try {
            return (Integer)this.h.invoke(this, m72, new Object[]{hKEY, string, n, intByReference, intByReference2, intByReference3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegQueryValueEx(WinReg.HKEY hKEY, String string, int n, IntByReference intByReference, byte[] byArray, IntByReference intByReference2) {
        try {
            return (Integer)this.h.invoke(this, m73, new Object[]{hKEY, string, n, intByReference, byArray, intByReference2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegQueryValueEx(WinReg.HKEY hKEY, String string, int n, IntByReference intByReference, Pointer pointer, IntByReference intByReference2) {
        try {
            return (Integer)this.h.invoke(this, m74, new Object[]{hKEY, string, n, intByReference, pointer, intByReference2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegEnumValue(WinReg.HKEY hKEY, int n, char[] cArray, IntByReference intByReference, IntByReference intByReference2, IntByReference intByReference3, byte[] byArray, IntByReference intByReference4) {
        try {
            return (Integer)this.h.invoke(this, m75, new Object[]{hKEY, n, cArray, intByReference, intByReference2, intByReference3, byArray, intByReference4});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegEnumValue(WinReg.HKEY hKEY, int n, char[] cArray, IntByReference intByReference, IntByReference intByReference2, IntByReference intByReference3, Pointer pointer, IntByReference intByReference4) {
        try {
            return (Integer)this.h.invoke(this, m76, new Object[]{hKEY, n, cArray, intByReference, intByReference2, intByReference3, pointer, intByReference4});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean EncryptionDisable(String string, boolean bl) {
        try {
            return (Boolean)this.h.invoke(this, m77, new Object[]{string, bl});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean IsWellKnownSid(WinNT.PSID pSID, int n) {
        try {
            return (Boolean)this.h.invoke(this, m78, new Object[]{pSID, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegCreateKeyEx(WinReg.HKEY hKEY, String string, int n, String string2, int n2, int n3, WinBase.SECURITY_ATTRIBUTES sECURITY_ATTRIBUTES, WinReg.HKEYByReference hKEYByReference, IntByReference intByReference) {
        try {
            return (Integer)this.h.invoke(this, m79, new Object[]{hKEY, string, n, string2, n2, n3, sECURITY_ATTRIBUTES, hKEYByReference, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegSetValueEx(WinReg.HKEY hKEY, String string, int n, int n2, byte[] byArray, int n3) {
        try {
            return (Integer)this.h.invoke(this, m80, new Object[]{hKEY, string, n, n2, byArray, n3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegSetValueEx(WinReg.HKEY hKEY, String string, int n, int n2, char[] cArray, int n3) {
        try {
            return (Integer)this.h.invoke(this, m81, new Object[]{hKEY, string, n, n2, cArray, n3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegSetValueEx(WinReg.HKEY hKEY, String string, int n, int n2, Pointer pointer, int n3) {
        try {
            return (Integer)this.h.invoke(this, m82, new Object[]{hKEY, string, n, n2, pointer, n3});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean ControlService(Winsvc.SC_HANDLE sC_HANDLE, int n, Winsvc.SERVICE_STATUS sERVICE_STATUS) {
        try {
            return (Boolean)this.h.invoke(this, m83, new Object[]{sC_HANDLE, n, sERVICE_STATUS});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int RegConnectRegistry(String string, WinReg.HKEY hKEY, WinReg.HKEYByReference hKEYByReference) {
        try {
            return (Integer)this.h.invoke(this, m84, new Object[]{string, hKEY, hKEYByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetServiceStatus(Winsvc.SERVICE_STATUS_HANDLE sERVICE_STATUS_HANDLE, Winsvc.SERVICE_STATUS sERVICE_STATUS) {
        try {
            return (Boolean)this.h.invoke(this, m85, new Object[]{sERVICE_STATUS_HANDLE, sERVICE_STATUS});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean MakeSelfRelativeSD(WinNT.SECURITY_DESCRIPTOR sECURITY_DESCRIPTOR, WinNT.SECURITY_DESCRIPTOR_RELATIVE sECURITY_DESCRIPTOR_RELATIVE, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m86, new Object[]{sECURITY_DESCRIPTOR, sECURITY_DESCRIPTOR_RELATIVE, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean RevertToSelf() {
        try {
            return (Boolean)this.h.invoke(this, m87, null);
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Winsvc.SC_HANDLE CreateService(Winsvc.SC_HANDLE sC_HANDLE, String string, String string2, int n, int n2, int n3, int n4, String string3, String string4, IntByReference intByReference, String string5, String string6, String string7) {
        try {
            return (Winsvc.SC_HANDLE)this.h.invoke(this, m88, new Object[]{sC_HANDLE, string, string2, n, n2, n3, n4, string3, string4, intByReference, string5, string6, string7});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean CreateWellKnownSid(int n, WinNT.PSID pSID, WinNT.PSID pSID2, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m89, new Object[]{n, pSID, pSID2, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean ClearEventLog(WinNT.HANDLE hANDLE, String string) {
        try {
            return (Boolean)this.h.invoke(this, m90, new Object[]{hANDLE, string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean CloseServiceHandle(Winsvc.SC_HANDLE sC_HANDLE) {
        try {
            return (Boolean)this.h.invoke(this, m91, new Object[]{sC_HANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Winsvc.SC_HANDLE OpenSCManager(String string, String string2, int n) {
        try {
            return (Winsvc.SC_HANDLE)this.h.invoke(this, m92, new Object[]{string, string2, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE OpenEventLog(String string, String string2) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m93, new Object[]{string, string2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean BackupEventLog(WinNT.HANDLE hANDLE, String string) {
        try {
            return (Boolean)this.h.invoke(this, m94, new Object[]{hANDLE, string});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int SetSecurityInfo(WinNT.HANDLE hANDLE, int n, int n2, Pointer pointer, Pointer pointer2, Pointer pointer3, Pointer pointer4) {
        try {
            return (Integer)this.h.invoke(this, m95, new Object[]{hANDLE, n, n2, pointer, pointer2, pointer3, pointer4});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean MakeAbsoluteSD(WinNT.SECURITY_DESCRIPTOR_RELATIVE sECURITY_DESCRIPTOR_RELATIVE, WinNT.SECURITY_DESCRIPTOR sECURITY_DESCRIPTOR, IntByReference intByReference, WinNT.ACL aCL, IntByReference intByReference2, WinNT.ACL aCL2, IntByReference intByReference3, WinNT.PSID pSID, IntByReference intByReference4, WinNT.PSID pSID2, IntByReference intByReference5) {
        try {
            return (Boolean)this.h.invoke(this, m96, new Object[]{sECURITY_DESCRIPTOR_RELATIVE, sECURITY_DESCRIPTOR, intByReference, aCL, intByReference2, aCL2, intByReference3, pSID, intByReference4, pSID2, intByReference5});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean CloseEventLog(WinNT.HANDLE hANDLE) {
        try {
            return (Boolean)this.h.invoke(this, m97, new Object[]{hANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean ImpersonateSelf(int n) {
        try {
            return (Boolean)this.h.invoke(this, m98, new Object[]{n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean DeleteService(Winsvc.SC_HANDLE sC_HANDLE) {
        try {
            return (Boolean)this.h.invoke(this, m99, new Object[]{sC_HANDLE});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetSecurityInfo(WinNT.HANDLE hANDLE, int n, int n2, PointerByReference pointerByReference, PointerByReference pointerByReference2, PointerByReference pointerByReference3, PointerByReference pointerByReference4, PointerByReference pointerByReference5) {
        try {
            return (Integer)this.h.invoke(this, m100, new Object[]{hANDLE, n, n2, pointerByReference, pointerByReference2, pointerByReference3, pointerByReference4, pointerByReference5});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean StartService(Winsvc.SC_HANDLE sC_HANDLE, int n, String[] stringArray) {
        try {
            return (Boolean)this.h.invoke(this, m101, new Object[]{sC_HANDLE, n, stringArray});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final WinNT.HANDLE OpenBackupEventLog(String string, String string2) {
        try {
            return (WinNT.HANDLE)this.h.invoke(this, m102, new Object[]{string, string2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean QueryServiceStatus(Winsvc.SC_HANDLE sC_HANDLE, Winsvc.SERVICE_STATUS sERVICE_STATUS) {
        try {
            return (Boolean)this.h.invoke(this, m103, new Object[]{sC_HANDLE, sERVICE_STATUS});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean ReportEvent(WinNT.HANDLE hANDLE, int n, int n2, int n3, WinNT.PSID pSID, int n4, int n5, String[] stringArray, Pointer pointer) {
        try {
            return (Boolean)this.h.invoke(this, m104, new Object[]{hANDLE, n, n2, n3, pSID, n4, n5, stringArray, pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean ReadEventLog(WinNT.HANDLE hANDLE, int n, int n2, Pointer pointer, int n3, IntByReference intByReference, IntByReference intByReference2) {
        try {
            return (Boolean)this.h.invoke(this, m105, new Object[]{hANDLE, n, n2, pointer, n3, intByReference, intByReference2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Winsvc.SC_HANDLE OpenService(Winsvc.SC_HANDLE sC_HANDLE, String string, int n) {
        try {
            return (Winsvc.SC_HANDLE)this.h.invoke(this, m106, new Object[]{sC_HANDLE, string, n});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean IsValidSid(WinNT.PSID pSID) {
        try {
            return (Boolean)this.h.invoke(this, m107, new Object[]{pSID});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean IsValidAcl(Pointer pointer) {
        try {
            return (Boolean)this.h.invoke(this, m108, new Object[]{pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final int GetSecurityDescriptorLength(Pointer pointer) {
        try {
            return (Integer)this.h.invoke(this, m109, new Object[]{pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean SetSecurityDescriptorControl(WinNT.SECURITY_DESCRIPTOR sECURITY_DESCRIPTOR, short s, short s2) {
        try {
            return (Boolean)this.h.invoke(this, m110, new Object[]{sECURITY_DESCRIPTOR, s, s2});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final Winsvc.SERVICE_STATUS_HANDLE RegisterServiceCtrlHandlerEx(String string, Winsvc.HandlerEx handlerEx, Pointer pointer) {
        try {
            return (Winsvc.SERVICE_STATUS_HANDLE)this.h.invoke(this, m111, new Object[]{string, handlerEx, pointer});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    @Override
    public final boolean GetSecurityDescriptorControl(WinNT.SECURITY_DESCRIPTOR sECURITY_DESCRIPTOR, ShortByReference shortByReference, IntByReference intByReference) {
        try {
            return (Boolean)this.h.invoke(this, m112, new Object[]{sECURITY_DESCRIPTOR, shortByReference, intByReference});
        }
        catch (Error | RuntimeException throwable) {
            throw throwable;
        }
        catch (Throwable throwable) {
            throw new UndeclaredThrowableException(throwable);
        }
    }

    static {
        ClassLoader classLoader = $Proxy0.class.getClassLoader();
        try {
            m0 = Class.forName("java.lang.Object", false, classLoader).getMethod("hashCode", new Class[0]);
            m1 = Class.forName("java.lang.Object", false, classLoader).getMethod("equals", Class.forName("java.lang.Object", false, classLoader));
            m2 = Class.forName("java.lang.Object", false, classLoader).getMethod("toString", new Class[0]);
            m3 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("InitializeSecurityDescriptor", Class.forName("com.sun.jna.platform.win32.WinNT$SECURITY_DESCRIPTOR", false, classLoader), Integer.TYPE);
            m4 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("InitializeAcl", Class.forName("com.sun.jna.platform.win32.WinNT$ACL", false, classLoader), Integer.TYPE, Integer.TYPE);
            m5 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("GetFileSecurity", Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m6 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("SetFileSecurity", Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader));
            m7 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("GetSecurityDescriptorOwner", Class.forName("com.sun.jna.platform.win32.WinNT$SECURITY_DESCRIPTOR", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$PSIDByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$BOOLByReference", false, classLoader));
            m8 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("SetSecurityDescriptorOwner", Class.forName("com.sun.jna.platform.win32.WinNT$SECURITY_DESCRIPTOR", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$PSID", false, classLoader), Boolean.TYPE);
            m9 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("GetSecurityDescriptorDacl", Class.forName("com.sun.jna.platform.win32.WinNT$SECURITY_DESCRIPTOR", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$BOOLByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$PACLByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$BOOLByReference", false, classLoader));
            m10 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("SetSecurityDescriptorDacl", Class.forName("com.sun.jna.platform.win32.WinNT$SECURITY_DESCRIPTOR", false, classLoader), Boolean.TYPE, Class.forName("com.sun.jna.platform.win32.WinNT$ACL", false, classLoader), Boolean.TYPE);
            m11 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("GetAce", Class.forName("com.sun.jna.platform.win32.WinNT$ACL", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m12 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("AddAccessAllowedAceEx", Class.forName("com.sun.jna.platform.win32.WinNT$ACL", false, classLoader), Integer.TYPE, Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinNT$PSID", false, classLoader));
            m13 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("LookupAccountSid", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$PSID", false, classLoader), Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m14 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("LookupAccountName", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$PSID", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m15 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("GetLengthSid", Class.forName("com.sun.jna.platform.win32.WinNT$PSID", false, classLoader));
            m16 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("ConvertSidToStringSid", Class.forName("com.sun.jna.platform.win32.WinNT$PSID", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m17 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("ConvertStringSidToSid", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$PSIDByReference", false, classLoader));
            m18 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("OpenProcessToken", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinNT$HANDLEByReference", false, classLoader));
            m19 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("OpenThreadToken", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE, Boolean.TYPE, Class.forName("com.sun.jna.platform.win32.WinNT$HANDLEByReference", false, classLoader));
            m20 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("DuplicateTokenEx", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinBase$SECURITY_ATTRIBUTES", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinNT$HANDLEByReference", false, classLoader));
            m21 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("SetThreadToken", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLEByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m22 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("GetTokenInformation", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Structure", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m23 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("AdjustTokenPrivileges", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Boolean.TYPE, Class.forName("com.sun.jna.platform.win32.WinNT$TOKEN_PRIVILEGES", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinNT$TOKEN_PRIVILEGES", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m24 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("AccessCheck", Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$GENERIC_MAPPING", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$PRIVILEGE_SET", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORDByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORDByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$BOOLByReference", false, classLoader));
            m25 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("LookupPrivilegeValue", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$LUID", false, classLoader));
            m26 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("SetSecurityDescriptorGroup", Class.forName("com.sun.jna.platform.win32.WinNT$SECURITY_DESCRIPTOR", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$PSID", false, classLoader), Boolean.TYPE);
            m27 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("GetSecurityDescriptorGroup", Class.forName("com.sun.jna.platform.win32.WinNT$SECURITY_DESCRIPTOR", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$PSIDByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$BOOLByReference", false, classLoader));
            m28 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("CloseEncryptedFileRaw", Class.forName("com.sun.jna.Pointer", false, classLoader));
            m29 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("OpenEncryptedFileRaw", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$ULONG", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m30 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("WriteEncryptedFileRaw", Class.forName("com.sun.jna.platform.win32.WinBase$FE_IMPORT_FUNC", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m31 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("FileEncryptionStatus", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORDByReference", false, classLoader));
            m32 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("SetNamedSecurityInfo", Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m33 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("ReadEncryptedFileRaw", Class.forName("com.sun.jna.platform.win32.WinBase$FE_EXPORT_FUNC", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m34 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("GetNamedSecurityInfo", Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m35 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("AddAce", Class.forName("com.sun.jna.platform.win32.WinNT$ACL", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE);
            m36 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("LogonUser", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinNT$HANDLEByReference", false, classLoader));
            m37 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("EqualSid", Class.forName("com.sun.jna.platform.win32.WinNT$PSID", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$PSID", false, classLoader));
            m38 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("GetOldestEventLogRecord", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m39 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("ImpersonateLoggedOnUser", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m40 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("QueryServiceStatusEx", Class.forName("com.sun.jna.platform.win32.Winsvc$SC_HANDLE", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.platform.win32.Winsvc$SERVICE_STATUS_PROCESS", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m41 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegNotifyChangeKeyValue", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Boolean.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Boolean.TYPE);
            m42 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegisterServiceCtrlHandler", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.Library$Handler", false, classLoader));
            m43 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("IsValidSecurityDescriptor", Class.forName("com.sun.jna.Pointer", false, classLoader));
            m44 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("LookupPrivilegeName", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$LUID", false, classLoader), Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m45 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("ChangeServiceConfig2", Class.forName("com.sun.jna.platform.win32.Winsvc$SC_HANDLE", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.platform.win32.Winsvc$ChangeServiceConfig2Info", false, classLoader));
            m46 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("CreateProcessWithLogonW", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$STARTUPINFO", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$PROCESS_INFORMATION", false, classLoader));
            m47 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("DeregisterEventSource", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m48 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("GetNumberOfEventLogRecords", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m49 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("EnumDependentServices", Class.forName("com.sun.jna.platform.win32.Winsvc$SC_HANDLE", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m50 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("AddAccessAllowedAce", Class.forName("com.sun.jna.platform.win32.WinNT$ACL", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinNT$PSID", false, classLoader));
            m51 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("EnumServicesStatusEx", Class.forName("com.sun.jna.platform.win32.Winsvc$SC_HANDLE", false, classLoader), Integer.TYPE, Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m52 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("QueryServiceConfig2", Class.forName("com.sun.jna.platform.win32.Winsvc$SC_HANDLE", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m53 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("StartServiceCtrlDispatcher", Class.forName("[Lcom.sun.jna.platform.win32.Winsvc$SERVICE_TABLE_ENTRY;", false, classLoader));
            m54 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegisterEventSource", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m55 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("CreateProcessAsUser", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$SECURITY_ATTRIBUTES", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$SECURITY_ATTRIBUTES", false, classLoader), Boolean.TYPE, Integer.TYPE, Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$STARTUPINFO", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$PROCESS_INFORMATION", false, classLoader));
            m56 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegOpenKeyEx", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinReg$HKEYByReference", false, classLoader));
            m57 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegDeleteKey", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m58 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("DuplicateToken", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinNT$HANDLEByReference", false, classLoader));
            m59 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("EncryptFile", Class.forName("java.lang.String", false, classLoader));
            m60 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("GetUserNameW", Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m61 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("MapGenericMask", Class.forName("com.sun.jna.platform.win32.WinDef$DWORDByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$GENERIC_MAPPING", false, classLoader));
            m62 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("DecryptFile", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinDef$DWORD", false, classLoader));
            m63 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegQueryInfoKey", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$FILETIME", false, classLoader));
            m64 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegLoadAppKey", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinReg$HKEYByReference", false, classLoader), Integer.TYPE, Integer.TYPE, Integer.TYPE);
            m65 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegEnumKeyEx", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Integer.TYPE, Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinBase$FILETIME", false, classLoader));
            m66 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegGetValue", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m67 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegGetValue", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("[B", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m68 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegCloseKey", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader));
            m69 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegDeleteValue", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m70 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegQueryValueEx", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.LongByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m71 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegQueryValueEx", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m72 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegQueryValueEx", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m73 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegQueryValueEx", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("[B", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m74 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegQueryValueEx", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m75 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegEnumValue", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Integer.TYPE, Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("[B", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m76 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegEnumValue", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Integer.TYPE, Class.forName("[C", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m77 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("EncryptionDisable", Class.forName("java.lang.String", false, classLoader), Boolean.TYPE);
            m78 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("IsWellKnownSid", Class.forName("com.sun.jna.platform.win32.WinNT$PSID", false, classLoader), Integer.TYPE);
            m79 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegCreateKeyEx", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinBase$SECURITY_ATTRIBUTES", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinReg$HKEYByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m80 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegSetValueEx", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("[B", false, classLoader), Integer.TYPE);
            m81 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegSetValueEx", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("[C", false, classLoader), Integer.TYPE);
            m82 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegSetValueEx", Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE);
            m83 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("ControlService", Class.forName("com.sun.jna.platform.win32.Winsvc$SC_HANDLE", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.platform.win32.Winsvc$SERVICE_STATUS", false, classLoader));
            m84 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegConnectRegistry", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinReg$HKEY", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinReg$HKEYByReference", false, classLoader));
            m85 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("SetServiceStatus", Class.forName("com.sun.jna.platform.win32.Winsvc$SERVICE_STATUS_HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.Winsvc$SERVICE_STATUS", false, classLoader));
            m86 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("MakeSelfRelativeSD", Class.forName("com.sun.jna.platform.win32.WinNT$SECURITY_DESCRIPTOR", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$SECURITY_DESCRIPTOR_RELATIVE", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m87 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RevertToSelf", new Class[0]);
            m88 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("CreateService", Class.forName("com.sun.jna.platform.win32.Winsvc$SC_HANDLE", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m89 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("CreateWellKnownSid", Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinNT$PSID", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$PSID", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m90 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("ClearEventLog", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m91 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("CloseServiceHandle", Class.forName("com.sun.jna.platform.win32.Winsvc$SC_HANDLE", false, classLoader));
            m92 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("OpenSCManager", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE);
            m93 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("OpenEventLog", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m94 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("BackupEventLog", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m95 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("SetSecurityInfo", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m96 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("MakeAbsoluteSD", Class.forName("com.sun.jna.platform.win32.WinNT$SECURITY_DESCRIPTOR_RELATIVE", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$SECURITY_DESCRIPTOR", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$ACL", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$ACL", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$PSID", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.platform.win32.WinNT$PSID", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m97 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("CloseEventLog", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader));
            m98 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("ImpersonateSelf", Integer.TYPE);
            m99 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("DeleteService", Class.forName("com.sun.jna.platform.win32.Winsvc$SC_HANDLE", false, classLoader));
            m100 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("GetSecurityInfo", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader), Class.forName("com.sun.jna.ptr.PointerByReference", false, classLoader));
            m101 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("StartService", Class.forName("com.sun.jna.platform.win32.Winsvc$SC_HANDLE", false, classLoader), Integer.TYPE, Class.forName("[Ljava.lang.String;", false, classLoader));
            m102 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("OpenBackupEventLog", Class.forName("java.lang.String", false, classLoader), Class.forName("java.lang.String", false, classLoader));
            m103 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("QueryServiceStatus", Class.forName("com.sun.jna.platform.win32.Winsvc$SC_HANDLE", false, classLoader), Class.forName("com.sun.jna.platform.win32.Winsvc$SERVICE_STATUS", false, classLoader));
            m104 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("ReportEvent", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE, Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.platform.win32.WinNT$PSID", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("[Ljava.lang.String;", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m105 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("ReadEventLog", Class.forName("com.sun.jna.platform.win32.WinNT$HANDLE", false, classLoader), Integer.TYPE, Integer.TYPE, Class.forName("com.sun.jna.Pointer", false, classLoader), Integer.TYPE, Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
            m106 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("OpenService", Class.forName("com.sun.jna.platform.win32.Winsvc$SC_HANDLE", false, classLoader), Class.forName("java.lang.String", false, classLoader), Integer.TYPE);
            m107 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("IsValidSid", Class.forName("com.sun.jna.platform.win32.WinNT$PSID", false, classLoader));
            m108 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("IsValidAcl", Class.forName("com.sun.jna.Pointer", false, classLoader));
            m109 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("GetSecurityDescriptorLength", Class.forName("com.sun.jna.Pointer", false, classLoader));
            m110 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("SetSecurityDescriptorControl", Class.forName("com.sun.jna.platform.win32.WinNT$SECURITY_DESCRIPTOR", false, classLoader), Short.TYPE, Short.TYPE);
            m111 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("RegisterServiceCtrlHandlerEx", Class.forName("java.lang.String", false, classLoader), Class.forName("com.sun.jna.platform.win32.Winsvc$HandlerEx", false, classLoader), Class.forName("com.sun.jna.Pointer", false, classLoader));
            m112 = Class.forName("com.sun.jna.platform.win32.Advapi32", false, classLoader).getMethod("GetSecurityDescriptorControl", Class.forName("com.sun.jna.platform.win32.WinNT$SECURITY_DESCRIPTOR", false, classLoader), Class.forName("com.sun.jna.ptr.ShortByReference", false, classLoader), Class.forName("com.sun.jna.ptr.IntByReference", false, classLoader));
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

