/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.Winsvc;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface Advapi32
extends StdCallLibrary {
    public static final Advapi32 INSTANCE = Native.load("Advapi32", Advapi32.class, W32APIOptions.DEFAULT_OPTIONS);
    public static final int MAX_KEY_LENGTH = 255;
    public static final int MAX_VALUE_NAME = 16383;
    public static final int RRF_RT_ANY = 65535;
    public static final int RRF_RT_DWORD = 24;
    public static final int RRF_RT_QWORD = 72;
    public static final int RRF_RT_REG_BINARY = 8;
    public static final int RRF_RT_REG_DWORD = 16;
    public static final int RRF_RT_REG_EXPAND_SZ = 4;
    public static final int RRF_RT_REG_MULTI_SZ = 32;
    public static final int RRF_RT_REG_NONE = 1;
    public static final int RRF_RT_REG_QWORD = 64;
    public static final int RRF_RT_REG_SZ = 2;
    public static final int REG_PROCESS_APPKEY = 1;
    public static final int LOGON_WITH_PROFILE = 1;
    public static final int LOGON_NETCREDENTIALS_ONLY = 2;

    public boolean GetUserNameW(char[] var1, IntByReference var2);

    public boolean LookupAccountName(String var1, String var2, WinNT.PSID var3, IntByReference var4, char[] var5, IntByReference var6, PointerByReference var7);

    public boolean LookupAccountSid(String var1, WinNT.PSID var2, char[] var3, IntByReference var4, char[] var5, IntByReference var6, PointerByReference var7);

    public boolean ConvertSidToStringSid(WinNT.PSID var1, PointerByReference var2);

    public boolean ConvertStringSidToSid(String var1, WinNT.PSIDByReference var2);

    public int GetLengthSid(WinNT.PSID var1);

    public boolean IsValidSid(WinNT.PSID var1);

    public boolean EqualSid(WinNT.PSID var1, WinNT.PSID var2);

    public boolean IsWellKnownSid(WinNT.PSID var1, int var2);

    public boolean CreateWellKnownSid(int var1, WinNT.PSID var2, WinNT.PSID var3, IntByReference var4);

    public boolean InitializeSecurityDescriptor(WinNT.SECURITY_DESCRIPTOR var1, int var2);

    public boolean GetSecurityDescriptorControl(WinNT.SECURITY_DESCRIPTOR var1, ShortByReference var2, IntByReference var3);

    public boolean SetSecurityDescriptorControl(WinNT.SECURITY_DESCRIPTOR var1, short var2, short var3);

    public boolean GetSecurityDescriptorOwner(WinNT.SECURITY_DESCRIPTOR var1, WinNT.PSIDByReference var2, WinDef.BOOLByReference var3);

    public boolean SetSecurityDescriptorOwner(WinNT.SECURITY_DESCRIPTOR var1, WinNT.PSID var2, boolean var3);

    public boolean GetSecurityDescriptorGroup(WinNT.SECURITY_DESCRIPTOR var1, WinNT.PSIDByReference var2, WinDef.BOOLByReference var3);

    public boolean SetSecurityDescriptorGroup(WinNT.SECURITY_DESCRIPTOR var1, WinNT.PSID var2, boolean var3);

    public boolean GetSecurityDescriptorDacl(WinNT.SECURITY_DESCRIPTOR var1, WinDef.BOOLByReference var2, WinNT.PACLByReference var3, WinDef.BOOLByReference var4);

    public boolean SetSecurityDescriptorDacl(WinNT.SECURITY_DESCRIPTOR var1, boolean var2, WinNT.ACL var3, boolean var4);

    public boolean InitializeAcl(WinNT.ACL var1, int var2, int var3);

    public boolean AddAce(WinNT.ACL var1, int var2, int var3, Pointer var4, int var5);

    public boolean AddAccessAllowedAce(WinNT.ACL var1, int var2, int var3, WinNT.PSID var4);

    public boolean AddAccessAllowedAceEx(WinNT.ACL var1, int var2, int var3, int var4, WinNT.PSID var5);

    public boolean GetAce(WinNT.ACL var1, int var2, PointerByReference var3);

    public boolean LogonUser(String var1, String var2, String var3, int var4, int var5, WinNT.HANDLEByReference var6);

    public boolean OpenThreadToken(WinNT.HANDLE var1, int var2, boolean var3, WinNT.HANDLEByReference var4);

    public boolean SetThreadToken(WinNT.HANDLEByReference var1, WinNT.HANDLE var2);

    public boolean OpenProcessToken(WinNT.HANDLE var1, int var2, WinNT.HANDLEByReference var3);

    public boolean DuplicateToken(WinNT.HANDLE var1, int var2, WinNT.HANDLEByReference var3);

    public boolean DuplicateTokenEx(WinNT.HANDLE var1, int var2, WinBase.SECURITY_ATTRIBUTES var3, int var4, int var5, WinNT.HANDLEByReference var6);

    public boolean GetTokenInformation(WinNT.HANDLE var1, int var2, Structure var3, int var4, IntByReference var5);

    public boolean ImpersonateLoggedOnUser(WinNT.HANDLE var1);

    public boolean ImpersonateSelf(int var1);

    public boolean RevertToSelf();

    public int RegOpenKeyEx(WinReg.HKEY var1, String var2, int var3, int var4, WinReg.HKEYByReference var5);

    public int RegLoadAppKey(String var1, WinReg.HKEYByReference var2, int var3, int var4, int var5);

    public int RegConnectRegistry(String var1, WinReg.HKEY var2, WinReg.HKEYByReference var3);

    public int RegQueryValueEx(WinReg.HKEY var1, String var2, int var3, IntByReference var4, char[] var5, IntByReference var6);

    public int RegQueryValueEx(WinReg.HKEY var1, String var2, int var3, IntByReference var4, byte[] var5, IntByReference var6);

    public int RegQueryValueEx(WinReg.HKEY var1, String var2, int var3, IntByReference var4, IntByReference var5, IntByReference var6);

    public int RegQueryValueEx(WinReg.HKEY var1, String var2, int var3, IntByReference var4, LongByReference var5, IntByReference var6);

    public int RegQueryValueEx(WinReg.HKEY var1, String var2, int var3, IntByReference var4, Pointer var5, IntByReference var6);

    public int RegCloseKey(WinReg.HKEY var1);

    public int RegDeleteValue(WinReg.HKEY var1, String var2);

    public int RegSetValueEx(WinReg.HKEY var1, String var2, int var3, int var4, Pointer var5, int var6);

    public int RegSetValueEx(WinReg.HKEY var1, String var2, int var3, int var4, char[] var5, int var6);

    public int RegSetValueEx(WinReg.HKEY var1, String var2, int var3, int var4, byte[] var5, int var6);

    public int RegCreateKeyEx(WinReg.HKEY var1, String var2, int var3, String var4, int var5, int var6, WinBase.SECURITY_ATTRIBUTES var7, WinReg.HKEYByReference var8, IntByReference var9);

    public int RegDeleteKey(WinReg.HKEY var1, String var2);

    public int RegEnumKeyEx(WinReg.HKEY var1, int var2, char[] var3, IntByReference var4, IntByReference var5, char[] var6, IntByReference var7, WinBase.FILETIME var8);

    public int RegEnumValue(WinReg.HKEY var1, int var2, char[] var3, IntByReference var4, IntByReference var5, IntByReference var6, Pointer var7, IntByReference var8);

    public int RegEnumValue(WinReg.HKEY var1, int var2, char[] var3, IntByReference var4, IntByReference var5, IntByReference var6, byte[] var7, IntByReference var8);

    public int RegQueryInfoKey(WinReg.HKEY var1, char[] var2, IntByReference var3, IntByReference var4, IntByReference var5, IntByReference var6, IntByReference var7, IntByReference var8, IntByReference var9, IntByReference var10, IntByReference var11, WinBase.FILETIME var12);

    public int RegGetValue(WinReg.HKEY var1, String var2, String var3, int var4, IntByReference var5, Pointer var6, IntByReference var7);

    public int RegGetValue(WinReg.HKEY var1, String var2, String var3, int var4, IntByReference var5, byte[] var6, IntByReference var7);

    public int RegNotifyChangeKeyValue(WinReg.HKEY var1, boolean var2, int var3, WinNT.HANDLE var4, boolean var5);

    public WinNT.HANDLE RegisterEventSource(String var1, String var2);

    public boolean DeregisterEventSource(WinNT.HANDLE var1);

    public WinNT.HANDLE OpenEventLog(String var1, String var2);

    public boolean CloseEventLog(WinNT.HANDLE var1);

    public boolean GetNumberOfEventLogRecords(WinNT.HANDLE var1, IntByReference var2);

    public boolean ReportEvent(WinNT.HANDLE var1, int var2, int var3, int var4, WinNT.PSID var5, int var6, int var7, String[] var8, Pointer var9);

    public boolean ClearEventLog(WinNT.HANDLE var1, String var2);

    public boolean BackupEventLog(WinNT.HANDLE var1, String var2);

    public WinNT.HANDLE OpenBackupEventLog(String var1, String var2);

    public boolean ReadEventLog(WinNT.HANDLE var1, int var2, int var3, Pointer var4, int var5, IntByReference var6, IntByReference var7);

    public boolean GetOldestEventLogRecord(WinNT.HANDLE var1, IntByReference var2);

    public boolean ChangeServiceConfig2(Winsvc.SC_HANDLE var1, int var2, Winsvc.ChangeServiceConfig2Info var3);

    public boolean QueryServiceConfig2(Winsvc.SC_HANDLE var1, int var2, Pointer var3, int var4, IntByReference var5);

    public boolean QueryServiceStatusEx(Winsvc.SC_HANDLE var1, int var2, Winsvc.SERVICE_STATUS_PROCESS var3, int var4, IntByReference var5);

    public boolean QueryServiceStatus(Winsvc.SC_HANDLE var1, Winsvc.SERVICE_STATUS var2);

    public boolean ControlService(Winsvc.SC_HANDLE var1, int var2, Winsvc.SERVICE_STATUS var3);

    public boolean StartService(Winsvc.SC_HANDLE var1, int var2, String[] var3);

    public boolean CloseServiceHandle(Winsvc.SC_HANDLE var1);

    public Winsvc.SC_HANDLE OpenService(Winsvc.SC_HANDLE var1, String var2, int var3);

    public Winsvc.SC_HANDLE OpenSCManager(String var1, String var2, int var3);

    public boolean EnumDependentServices(Winsvc.SC_HANDLE var1, int var2, Pointer var3, int var4, IntByReference var5, IntByReference var6);

    public boolean EnumServicesStatusEx(Winsvc.SC_HANDLE var1, int var2, int var3, int var4, Pointer var5, int var6, IntByReference var7, IntByReference var8, IntByReference var9, String var10);

    public boolean CreateProcessAsUser(WinNT.HANDLE var1, String var2, String var3, WinBase.SECURITY_ATTRIBUTES var4, WinBase.SECURITY_ATTRIBUTES var5, boolean var6, int var7, String var8, String var9, WinBase.STARTUPINFO var10, WinBase.PROCESS_INFORMATION var11);

    public boolean AdjustTokenPrivileges(WinNT.HANDLE var1, boolean var2, WinNT.TOKEN_PRIVILEGES var3, int var4, WinNT.TOKEN_PRIVILEGES var5, IntByReference var6);

    public boolean LookupPrivilegeName(String var1, WinNT.LUID var2, char[] var3, IntByReference var4);

    public boolean LookupPrivilegeValue(String var1, String var2, WinNT.LUID var3);

    public boolean GetFileSecurity(String var1, int var2, Pointer var3, int var4, IntByReference var5);

    public boolean SetFileSecurity(String var1, int var2, Pointer var3);

    public int GetSecurityInfo(WinNT.HANDLE var1, int var2, int var3, PointerByReference var4, PointerByReference var5, PointerByReference var6, PointerByReference var7, PointerByReference var8);

    public int SetSecurityInfo(WinNT.HANDLE var1, int var2, int var3, Pointer var4, Pointer var5, Pointer var6, Pointer var7);

    public int GetNamedSecurityInfo(String var1, int var2, int var3, PointerByReference var4, PointerByReference var5, PointerByReference var6, PointerByReference var7, PointerByReference var8);

    public int SetNamedSecurityInfo(String var1, int var2, int var3, Pointer var4, Pointer var5, Pointer var6, Pointer var7);

    public int GetSecurityDescriptorLength(Pointer var1);

    public boolean IsValidSecurityDescriptor(Pointer var1);

    public boolean MakeSelfRelativeSD(WinNT.SECURITY_DESCRIPTOR var1, WinNT.SECURITY_DESCRIPTOR_RELATIVE var2, IntByReference var3);

    public boolean MakeAbsoluteSD(WinNT.SECURITY_DESCRIPTOR_RELATIVE var1, WinNT.SECURITY_DESCRIPTOR var2, IntByReference var3, WinNT.ACL var4, IntByReference var5, WinNT.ACL var6, IntByReference var7, WinNT.PSID var8, IntByReference var9, WinNT.PSID var10, IntByReference var11);

    public boolean IsValidAcl(Pointer var1);

    public void MapGenericMask(WinDef.DWORDByReference var1, WinNT.GENERIC_MAPPING var2);

    public boolean AccessCheck(Pointer var1, WinNT.HANDLE var2, WinDef.DWORD var3, WinNT.GENERIC_MAPPING var4, WinNT.PRIVILEGE_SET var5, WinDef.DWORDByReference var6, WinDef.DWORDByReference var7, WinDef.BOOLByReference var8);

    public boolean EncryptFile(String var1);

    public boolean DecryptFile(String var1, WinDef.DWORD var2);

    public boolean FileEncryptionStatus(String var1, WinDef.DWORDByReference var2);

    public boolean EncryptionDisable(String var1, boolean var2);

    public int OpenEncryptedFileRaw(String var1, WinDef.ULONG var2, PointerByReference var3);

    public int ReadEncryptedFileRaw(WinBase.FE_EXPORT_FUNC var1, Pointer var2, Pointer var3);

    public int WriteEncryptedFileRaw(WinBase.FE_IMPORT_FUNC var1, Pointer var2, Pointer var3);

    public void CloseEncryptedFileRaw(Pointer var1);

    public boolean CreateProcessWithLogonW(String var1, String var2, String var3, int var4, String var5, String var6, int var7, Pointer var8, String var9, WinBase.STARTUPINFO var10, WinBase.PROCESS_INFORMATION var11);

    public boolean StartServiceCtrlDispatcher(Winsvc.SERVICE_TABLE_ENTRY[] var1);

    public Winsvc.SERVICE_STATUS_HANDLE RegisterServiceCtrlHandler(String var1, Library.Handler var2);

    public Winsvc.SERVICE_STATUS_HANDLE RegisterServiceCtrlHandlerEx(String var1, Winsvc.HandlerEx var2, Pointer var3);

    public boolean SetServiceStatus(Winsvc.SERVICE_STATUS_HANDLE var1, Winsvc.SERVICE_STATUS var2);

    public Winsvc.SC_HANDLE CreateService(Winsvc.SC_HANDLE var1, String var2, String var3, int var4, int var5, int var6, int var7, String var8, String var9, IntByReference var10, String var11, String var12, String var13);

    public boolean DeleteService(Winsvc.SC_HANDLE var1);
}

