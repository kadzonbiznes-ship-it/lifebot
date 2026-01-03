/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.platform.win32.Advapi32Util$1
 *  com.sun.jna.platform.win32.Advapi32Util$2
 *  com.sun.jna.platform.win32.Advapi32Util$AccessCheckPermission
 *  com.sun.jna.platform.win32.Advapi32Util$Account
 *  com.sun.jna.platform.win32.Advapi32Util$EnumKey
 *  com.sun.jna.platform.win32.Advapi32Util$EventLogType
 *  com.sun.jna.platform.win32.Advapi32Util$InfoKey
 *  com.sun.jna.platform.win32.WinNT$ACCESS_ACEStructure
 *  com.sun.jna.platform.win32.WinNT$ACCESS_ALLOWED_ACE
 *  com.sun.jna.platform.win32.WinNT$ACE_HEADER
 *  com.sun.jna.platform.win32.WinNT$SID_AND_ATTRIBUTES
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.W32APITypeMapper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public abstract class Advapi32Util {
    public static String getUserName() {
        char[] buffer = new char[128];
        IntByReference len = new IntByReference(buffer.length);
        boolean result = Advapi32.INSTANCE.GetUserNameW(buffer, len);
        if (!result) {
            switch (Kernel32.INSTANCE.GetLastError()) {
                case 122: {
                    buffer = new char[len.getValue()];
                    break;
                }
                default: {
                    throw new Win32Exception(Native.getLastError());
                }
            }
            result = Advapi32.INSTANCE.GetUserNameW(buffer, len);
        }
        if (!result) {
            throw new Win32Exception(Native.getLastError());
        }
        return Native.toString(buffer);
    }

    public static Account getAccountByName(String accountName) {
        return Advapi32Util.getAccountByName(null, accountName);
    }

    public static Account getAccountByName(String systemName, String accountName) {
        char[] referencedDomainName;
        IntByReference pSid = new IntByReference(0);
        IntByReference cchDomainName = new IntByReference(0);
        PointerByReference peUse = new PointerByReference();
        if (Advapi32.INSTANCE.LookupAccountName(systemName, accountName, null, pSid, null, cchDomainName, peUse)) {
            throw new RuntimeException("LookupAccountNameW was expected to fail with ERROR_INSUFFICIENT_BUFFER");
        }
        int rc = Kernel32.INSTANCE.GetLastError();
        if (pSid.getValue() == 0 || rc != 122) {
            throw new Win32Exception(rc);
        }
        Memory sidMemory = new Memory(pSid.getValue());
        WinNT.PSID result = new WinNT.PSID(sidMemory);
        if (!Advapi32.INSTANCE.LookupAccountName(systemName, accountName, result, pSid, referencedDomainName = new char[cchDomainName.getValue() + 1], cchDomainName, peUse)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        Account account = new Account();
        account.accountType = peUse.getPointer().getInt(0L);
        String[] accountNamePartsBs = accountName.split("\\\\", 2);
        String[] accountNamePartsAt = accountName.split("@", 2);
        account.name = accountNamePartsBs.length == 2 ? accountNamePartsBs[1] : (accountNamePartsAt.length == 2 ? accountNamePartsAt[0] : accountName);
        if (cchDomainName.getValue() > 0) {
            account.domain = Native.toString(referencedDomainName);
            account.fqn = account.domain + "\\" + account.name;
        } else {
            account.fqn = account.name;
        }
        account.sid = result.getBytes();
        account.sidString = Advapi32Util.convertSidToStringSid(new WinNT.PSID(account.sid));
        return account;
    }

    public static Account getAccountBySid(WinNT.PSID sid) {
        return Advapi32Util.getAccountBySid(null, sid);
    }

    public static Account getAccountBySid(String systemName, WinNT.PSID sid) {
        IntByReference cchName = new IntByReference(257);
        IntByReference cchDomainName = new IntByReference(256);
        PointerByReference peUse = new PointerByReference();
        char[] domainName = new char[cchDomainName.getValue()];
        char[] name = new char[cchName.getValue()];
        int rc = 0;
        if (!Advapi32.INSTANCE.LookupAccountSid(systemName, sid, name, cchName, domainName, cchDomainName, peUse) && (rc = Kernel32.INSTANCE.GetLastError()) != 1332) {
            throw new Win32Exception(rc);
        }
        Account account = new Account();
        if (rc == 1332) {
            account.accountType = 8;
            account.name = "NONE_MAPPED";
        } else {
            account.accountType = peUse.getPointer().getInt(0L);
            account.name = Native.toString(name);
        }
        account.domain = Native.toString(domainName);
        account.fqn = account.domain.isEmpty() ? account.name : account.domain + "\\" + account.name;
        account.sid = sid.getBytes();
        account.sidString = Advapi32Util.convertSidToStringSid(sid);
        return account;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String convertSidToStringSid(WinNT.PSID sid) {
        PointerByReference stringSid = new PointerByReference();
        if (!Advapi32.INSTANCE.ConvertSidToStringSid(sid, stringSid)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        Pointer ptr = stringSid.getValue();
        try {
            String string = ptr.getWideString(0L);
            return string;
        }
        finally {
            Kernel32Util.freeLocalMemory(ptr);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static byte[] convertStringSidToSid(String sidString) {
        WinNT.PSIDByReference pSID = new WinNT.PSIDByReference();
        if (!Advapi32.INSTANCE.ConvertStringSidToSid(sidString, pSID)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        WinNT.PSID value = pSID.getValue();
        try {
            byte[] byArray = value.getBytes();
            return byArray;
        }
        finally {
            Kernel32Util.freeLocalMemory(value.getPointer());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean isWellKnownSid(String sidString, int wellKnownSidType) {
        WinNT.PSIDByReference pSID = new WinNT.PSIDByReference();
        if (!Advapi32.INSTANCE.ConvertStringSidToSid(sidString, pSID)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        WinNT.PSID value = pSID.getValue();
        try {
            boolean bl = Advapi32.INSTANCE.IsWellKnownSid(value, wellKnownSidType);
            return bl;
        }
        finally {
            Kernel32Util.freeLocalMemory(value.getPointer());
        }
    }

    public static boolean isWellKnownSid(byte[] sidBytes, int wellKnownSidType) {
        WinNT.PSID pSID = new WinNT.PSID(sidBytes);
        return Advapi32.INSTANCE.IsWellKnownSid(pSID, wellKnownSidType);
    }

    public static int alignOnDWORD(int cbAcl) {
        return cbAcl + 3 & 0xFFFFFFFC;
    }

    public static int getAceSize(int sidLength) {
        return Native.getNativeSize(WinNT.ACCESS_ALLOWED_ACE.class, null) + sidLength - 4;
    }

    public static Account getAccountBySid(String sidString) {
        return Advapi32Util.getAccountBySid(null, sidString);
    }

    public static Account getAccountBySid(String systemName, String sidString) {
        return Advapi32Util.getAccountBySid(systemName, new WinNT.PSID(Advapi32Util.convertStringSidToSid(sidString)));
    }

    public static Account[] getTokenGroups(WinNT.HANDLE hToken) {
        IntByReference tokenInformationLength = new IntByReference();
        if (Advapi32.INSTANCE.GetTokenInformation(hToken, 2, null, 0, tokenInformationLength)) {
            throw new RuntimeException("Expected GetTokenInformation to fail with ERROR_INSUFFICIENT_BUFFER");
        }
        int rc = Kernel32.INSTANCE.GetLastError();
        if (rc != 122) {
            throw new Win32Exception(rc);
        }
        WinNT.TOKEN_GROUPS groups = new WinNT.TOKEN_GROUPS(tokenInformationLength.getValue());
        if (!Advapi32.INSTANCE.GetTokenInformation(hToken, 2, groups, tokenInformationLength.getValue(), tokenInformationLength)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        ArrayList<Account> userGroups = new ArrayList<Account>();
        for (WinNT.SID_AND_ATTRIBUTES sidAndAttribute : groups.getGroups()) {
            Account group;
            try {
                group = Advapi32Util.getAccountBySid(sidAndAttribute.Sid);
            }
            catch (Exception e) {
                group = new Account();
                group.sid = sidAndAttribute.Sid.getBytes();
                group.name = group.sidString = Advapi32Util.convertSidToStringSid(sidAndAttribute.Sid);
                group.fqn = group.sidString;
                group.accountType = 2;
            }
            userGroups.add(group);
        }
        return userGroups.toArray(new Account[0]);
    }

    public static Account getTokenPrimaryGroup(WinNT.HANDLE hToken) {
        Account group;
        IntByReference tokenInformationLength = new IntByReference();
        if (Advapi32.INSTANCE.GetTokenInformation(hToken, 5, null, 0, tokenInformationLength)) {
            throw new RuntimeException("Expected GetTokenInformation to fail with ERROR_INSUFFICIENT_BUFFER");
        }
        int rc = Kernel32.INSTANCE.GetLastError();
        if (rc != 122) {
            throw new Win32Exception(rc);
        }
        WinNT.TOKEN_PRIMARY_GROUP primaryGroup = new WinNT.TOKEN_PRIMARY_GROUP(tokenInformationLength.getValue());
        if (!Advapi32.INSTANCE.GetTokenInformation(hToken, 5, primaryGroup, tokenInformationLength.getValue(), tokenInformationLength)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        try {
            group = Advapi32Util.getAccountBySid(primaryGroup.PrimaryGroup);
        }
        catch (Exception e) {
            group = new Account();
            group.sid = primaryGroup.PrimaryGroup.getBytes();
            group.name = group.sidString = Advapi32Util.convertSidToStringSid(primaryGroup.PrimaryGroup);
            group.fqn = group.sidString;
            group.accountType = 2;
        }
        return group;
    }

    public static Account getTokenAccount(WinNT.HANDLE hToken) {
        IntByReference tokenInformationLength = new IntByReference();
        if (Advapi32.INSTANCE.GetTokenInformation(hToken, 1, null, 0, tokenInformationLength)) {
            throw new RuntimeException("Expected GetTokenInformation to fail with ERROR_INSUFFICIENT_BUFFER");
        }
        int rc = Kernel32.INSTANCE.GetLastError();
        if (rc != 122) {
            throw new Win32Exception(rc);
        }
        WinNT.TOKEN_USER user = new WinNT.TOKEN_USER(tokenInformationLength.getValue());
        if (!Advapi32.INSTANCE.GetTokenInformation(hToken, 1, user, tokenInformationLength.getValue(), tokenInformationLength)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        return Advapi32Util.getAccountBySid(user.User.Sid);
    }

    public static Account[] getCurrentUserGroups() {
        WinNT.HANDLEByReference phToken = new WinNT.HANDLEByReference();
        Win32Exception err = null;
        try {
            WinNT.HANDLE threadHandle = Kernel32.INSTANCE.GetCurrentThread();
            if (!Advapi32.INSTANCE.OpenThreadToken(threadHandle, 10, true, phToken)) {
                int rc = Kernel32.INSTANCE.GetLastError();
                if (rc != 1008) {
                    throw new Win32Exception(rc);
                }
                WinNT.HANDLE processHandle = Kernel32.INSTANCE.GetCurrentProcess();
                if (!Advapi32.INSTANCE.OpenProcessToken(processHandle, 10, phToken)) {
                    throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
                }
            }
            Account[] accountArray = Advapi32Util.getTokenGroups(phToken.getValue());
            return accountArray;
        }
        catch (Win32Exception e) {
            err = e;
            throw err;
        }
        finally {
            WinNT.HANDLE hToken = phToken.getValue();
            if (!WinBase.INVALID_HANDLE_VALUE.equals(hToken)) {
                try {
                    Kernel32Util.closeHandle(hToken);
                }
                catch (Win32Exception e) {
                    if (err == null) {
                        err = e;
                    }
                    err.addSuppressedReflected(e);
                }
            }
            if (err != null) {
                throw err;
            }
        }
    }

    public static boolean registryKeyExists(WinReg.HKEY root, String key) {
        return Advapi32Util.registryKeyExists(root, key, 0);
    }

    public static boolean registryKeyExists(WinReg.HKEY root, String key, int samDesiredExtra) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, key, 0, 0x20019 | samDesiredExtra, phkKey);
        switch (rc) {
            case 0: {
                Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
                return true;
            }
            case 2: {
                return false;
            }
        }
        throw new Win32Exception(rc);
    }

    public static boolean registryValueExists(WinReg.HKEY root, String key, String value) {
        return Advapi32Util.registryValueExists(root, key, value, 0);
    }

    public static boolean registryValueExists(WinReg.HKEY root, String key, String value, int samDesiredExtra) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, key, 0, 0x20019 | samDesiredExtra, phkKey);
        switch (rc) {
            case 0: {
                break;
            }
            case 2: {
                return false;
            }
            default: {
                throw new Win32Exception(rc);
            }
        }
        try {
            IntByReference lpcbData = new IntByReference();
            IntByReference lpType = new IntByReference();
            rc = Advapi32.INSTANCE.RegQueryValueEx(phkKey.getValue(), value, 0, lpType, (Pointer)null, lpcbData);
            switch (rc) {
                case 0: 
                case 122: 
                case 234: {
                    boolean bl = true;
                    return bl;
                }
                case 2: {
                    boolean bl = false;
                    return bl;
                }
            }
            throw new Win32Exception(rc);
        }
        finally {
            if (!WinBase.INVALID_HANDLE_VALUE.equals(phkKey.getValue()) && (rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue())) != 0) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static String registryGetStringValue(WinReg.HKEY root, String key, String value) {
        return Advapi32Util.registryGetStringValue(root, key, value, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String registryGetStringValue(WinReg.HKEY root, String key, String value, int samDesiredExtra) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, key, 0, 0x20019 | samDesiredExtra, phkKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        try {
            String string = Advapi32Util.registryGetStringValue(phkKey.getValue(), value);
            return string;
        }
        finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static String registryGetStringValue(WinReg.HKEY hKey, String value) {
        IntByReference lpType = new IntByReference();
        IntByReference lpcbData = new IntByReference();
        int rc = Advapi32.INSTANCE.RegQueryValueEx(hKey, value, 0, lpType, (Pointer)null, lpcbData);
        if (rc != 0 && rc != 122) {
            throw new Win32Exception(rc);
        }
        if (lpType.getValue() != 1 && lpType.getValue() != 2) {
            throw new RuntimeException("Unexpected registry type " + lpType.getValue() + ", expected REG_SZ or REG_EXPAND_SZ");
        }
        if (lpcbData.getValue() == 0) {
            return "";
        }
        Memory mem = new Memory(lpcbData.getValue() + Native.WCHAR_SIZE);
        mem.clear();
        rc = Advapi32.INSTANCE.RegQueryValueEx(hKey, value, 0, lpType, mem, lpcbData);
        if (rc != 0 && rc != 122) {
            throw new Win32Exception(rc);
        }
        if (W32APITypeMapper.DEFAULT == W32APITypeMapper.UNICODE) {
            return mem.getWideString(0L);
        }
        return mem.getString(0L);
    }

    public static String registryGetExpandableStringValue(WinReg.HKEY root, String key, String value) {
        return Advapi32Util.registryGetExpandableStringValue(root, key, value, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String registryGetExpandableStringValue(WinReg.HKEY root, String key, String value, int samDesiredExtra) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, key, 0, 0x20019 | samDesiredExtra, phkKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        try {
            String string = Advapi32Util.registryGetExpandableStringValue(phkKey.getValue(), value);
            return string;
        }
        finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static String registryGetExpandableStringValue(WinReg.HKEY hKey, String value) {
        IntByReference lpType = new IntByReference();
        IntByReference lpcbData = new IntByReference();
        int rc = Advapi32.INSTANCE.RegQueryValueEx(hKey, value, 0, lpType, (char[])null, lpcbData);
        if (rc != 0 && rc != 122) {
            throw new Win32Exception(rc);
        }
        if (lpType.getValue() != 2) {
            throw new RuntimeException("Unexpected registry type " + lpType.getValue() + ", expected REG_SZ");
        }
        if (lpcbData.getValue() == 0) {
            return "";
        }
        Memory mem = new Memory(lpcbData.getValue() + Native.WCHAR_SIZE);
        mem.clear();
        rc = Advapi32.INSTANCE.RegQueryValueEx(hKey, value, 0, lpType, mem, lpcbData);
        if (rc != 0 && rc != 122) {
            throw new Win32Exception(rc);
        }
        if (W32APITypeMapper.DEFAULT == W32APITypeMapper.UNICODE) {
            return mem.getWideString(0L);
        }
        return mem.getString(0L);
    }

    public static String[] registryGetStringArray(WinReg.HKEY root, String key, String value) {
        return Advapi32Util.registryGetStringArray(root, key, value, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String[] registryGetStringArray(WinReg.HKEY root, String key, String value, int samDesiredExtra) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, key, 0, 0x20019 | samDesiredExtra, phkKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        try {
            String[] stringArray = Advapi32Util.registryGetStringArray(phkKey.getValue(), value);
            return stringArray;
        }
        finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static String[] registryGetStringArray(WinReg.HKEY hKey, String value) {
        IntByReference lpType = new IntByReference();
        IntByReference lpcbData = new IntByReference();
        int rc = Advapi32.INSTANCE.RegQueryValueEx(hKey, value, 0, lpType, (char[])null, lpcbData);
        if (rc != 0 && rc != 122) {
            throw new Win32Exception(rc);
        }
        if (lpType.getValue() != 7) {
            throw new RuntimeException("Unexpected registry type " + lpType.getValue() + ", expected REG_SZ");
        }
        Memory data = new Memory(lpcbData.getValue() + 2 * Native.WCHAR_SIZE);
        data.clear();
        rc = Advapi32.INSTANCE.RegQueryValueEx(hKey, value, 0, lpType, data, lpcbData);
        if (rc != 0 && rc != 122) {
            throw new Win32Exception(rc);
        }
        return Advapi32Util.regMultiSzBufferToStringArray(data);
    }

    static String[] regMultiSzBufferToStringArray(Memory data) {
        ArrayList<String> result = new ArrayList<String>();
        int offset = 0;
        while ((long)offset < data.size()) {
            String s;
            if (W32APITypeMapper.DEFAULT == W32APITypeMapper.UNICODE) {
                s = data.getWideString(offset);
                offset += s.length() * Native.WCHAR_SIZE;
                offset += Native.WCHAR_SIZE;
            } else {
                s = data.getString(offset);
                offset += s.length();
                ++offset;
            }
            if (s.length() == 0) break;
            result.add(s);
        }
        return result.toArray(new String[0]);
    }

    public static byte[] registryGetBinaryValue(WinReg.HKEY root, String key, String value) {
        return Advapi32Util.registryGetBinaryValue(root, key, value, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static byte[] registryGetBinaryValue(WinReg.HKEY root, String key, String value, int samDesiredExtra) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, key, 0, 0x20019 | samDesiredExtra, phkKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        try {
            byte[] byArray = Advapi32Util.registryGetBinaryValue(phkKey.getValue(), value);
            return byArray;
        }
        finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static byte[] registryGetBinaryValue(WinReg.HKEY hKey, String value) {
        IntByReference lpType = new IntByReference();
        IntByReference lpcbData = new IntByReference();
        int rc = Advapi32.INSTANCE.RegQueryValueEx(hKey, value, 0, lpType, (Pointer)null, lpcbData);
        if (rc != 0 && rc != 122) {
            throw new Win32Exception(rc);
        }
        if (lpType.getValue() != 3) {
            throw new RuntimeException("Unexpected registry type " + lpType.getValue() + ", expected REG_BINARY");
        }
        byte[] data = new byte[lpcbData.getValue()];
        rc = Advapi32.INSTANCE.RegQueryValueEx(hKey, value, 0, lpType, data, lpcbData);
        if (rc != 0 && rc != 122) {
            throw new Win32Exception(rc);
        }
        return data;
    }

    public static int registryGetIntValue(WinReg.HKEY root, String key, String value) {
        return Advapi32Util.registryGetIntValue(root, key, value, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int registryGetIntValue(WinReg.HKEY root, String key, String value, int samDesiredExtra) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, key, 0, 0x20019 | samDesiredExtra, phkKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        try {
            int n = Advapi32Util.registryGetIntValue(phkKey.getValue(), value);
            return n;
        }
        finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static int registryGetIntValue(WinReg.HKEY hKey, String value) {
        IntByReference lpType = new IntByReference();
        IntByReference lpcbData = new IntByReference();
        int rc = Advapi32.INSTANCE.RegQueryValueEx(hKey, value, 0, lpType, (char[])null, lpcbData);
        if (rc != 0 && rc != 122) {
            throw new Win32Exception(rc);
        }
        if (lpType.getValue() != 4) {
            throw new RuntimeException("Unexpected registry type " + lpType.getValue() + ", expected REG_DWORD");
        }
        IntByReference data = new IntByReference();
        rc = Advapi32.INSTANCE.RegQueryValueEx(hKey, value, 0, lpType, data, lpcbData);
        if (rc != 0 && rc != 122) {
            throw new Win32Exception(rc);
        }
        return data.getValue();
    }

    public static long registryGetLongValue(WinReg.HKEY root, String key, String value) {
        return Advapi32Util.registryGetLongValue(root, key, value, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static long registryGetLongValue(WinReg.HKEY root, String key, String value, int samDesiredExtra) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, key, 0, 0x20019 | samDesiredExtra, phkKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        try {
            long l = Advapi32Util.registryGetLongValue(phkKey.getValue(), value);
            return l;
        }
        finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static long registryGetLongValue(WinReg.HKEY hKey, String value) {
        IntByReference lpType = new IntByReference();
        IntByReference lpcbData = new IntByReference();
        int rc = Advapi32.INSTANCE.RegQueryValueEx(hKey, value, 0, lpType, (char[])null, lpcbData);
        if (rc != 0 && rc != 122) {
            throw new Win32Exception(rc);
        }
        if (lpType.getValue() != 11) {
            throw new RuntimeException("Unexpected registry type " + lpType.getValue() + ", expected REG_QWORD");
        }
        LongByReference data = new LongByReference();
        rc = Advapi32.INSTANCE.RegQueryValueEx(hKey, value, 0, lpType, data, lpcbData);
        if (rc != 0 && rc != 122) {
            throw new Win32Exception(rc);
        }
        return data.getValue();
    }

    public static Object registryGetValue(WinReg.HKEY hkKey, String subKey, String lpValueName) {
        Object result = null;
        IntByReference lpType = new IntByReference();
        IntByReference lpcbData = new IntByReference();
        int rc = Advapi32.INSTANCE.RegGetValue(hkKey, subKey, lpValueName, 65535, lpType, (Pointer)null, lpcbData);
        if (lpType.getValue() == 0) {
            return null;
        }
        if (rc != 0 && rc != 122) {
            throw new Win32Exception(rc);
        }
        Memory byteData = new Memory(lpcbData.getValue() + Native.WCHAR_SIZE);
        byteData.clear();
        rc = Advapi32.INSTANCE.RegGetValue(hkKey, subKey, lpValueName, 65535, lpType, byteData, lpcbData);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        if (lpType.getValue() == 4) {
            result = byteData.getInt(0L);
        } else if (lpType.getValue() == 11) {
            result = byteData.getLong(0L);
        } else if (lpType.getValue() == 3) {
            result = byteData.getByteArray(0L, lpcbData.getValue());
        } else if (lpType.getValue() == 1 || lpType.getValue() == 2) {
            result = W32APITypeMapper.DEFAULT == W32APITypeMapper.UNICODE ? byteData.getWideString(0L) : byteData.getString(0L);
        }
        return result;
    }

    public static boolean registryCreateKey(WinReg.HKEY hKey, String keyName) {
        return Advapi32Util.registryCreateKey(hKey, keyName, 0);
    }

    public static boolean registryCreateKey(WinReg.HKEY hKey, String keyName, int samDesiredExtra) {
        WinReg.HKEYByReference phkResult = new WinReg.HKEYByReference();
        IntByReference lpdwDisposition = new IntByReference();
        int rc = Advapi32.INSTANCE.RegCreateKeyEx(hKey, keyName, 0, null, 0, 0x20019 | samDesiredExtra, null, phkResult, lpdwDisposition);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        rc = Advapi32.INSTANCE.RegCloseKey(phkResult.getValue());
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        return 1 == lpdwDisposition.getValue();
    }

    public static boolean registryCreateKey(WinReg.HKEY root, String parentPath, String keyName) {
        return Advapi32Util.registryCreateKey(root, parentPath, keyName, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean registryCreateKey(WinReg.HKEY root, String parentPath, String keyName, int samDesiredExtra) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, parentPath, 0, 4 | samDesiredExtra, phkKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        try {
            boolean bl = Advapi32Util.registryCreateKey(phkKey.getValue(), keyName);
            return bl;
        }
        finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static void registrySetIntValue(WinReg.HKEY hKey, String name, int value) {
        byte[] data = new byte[]{(byte)(value & 0xFF), (byte)(value >> 8 & 0xFF), (byte)(value >> 16 & 0xFF), (byte)(value >> 24 & 0xFF)};
        int rc = Advapi32.INSTANCE.RegSetValueEx(hKey, name, 0, 4, data, 4);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
    }

    public static void registrySetIntValue(WinReg.HKEY root, String keyPath, String name, int value) {
        Advapi32Util.registrySetIntValue(root, keyPath, name, value, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void registrySetIntValue(WinReg.HKEY root, String keyPath, String name, int value, int samDesiredExtra) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, 0x2001F | samDesiredExtra, phkKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        try {
            Advapi32Util.registrySetIntValue(phkKey.getValue(), name, value);
        }
        finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static void registrySetLongValue(WinReg.HKEY hKey, String name, long value) {
        byte[] data = new byte[]{(byte)(value & 0xFFL), (byte)(value >> 8 & 0xFFL), (byte)(value >> 16 & 0xFFL), (byte)(value >> 24 & 0xFFL), (byte)(value >> 32 & 0xFFL), (byte)(value >> 40 & 0xFFL), (byte)(value >> 48 & 0xFFL), (byte)(value >> 56 & 0xFFL)};
        int rc = Advapi32.INSTANCE.RegSetValueEx(hKey, name, 0, 11, data, 8);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
    }

    public static void registrySetLongValue(WinReg.HKEY root, String keyPath, String name, long value) {
        Advapi32Util.registrySetLongValue(root, keyPath, name, value, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void registrySetLongValue(WinReg.HKEY root, String keyPath, String name, long value, int samDesiredExtra) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, 0x2001F | samDesiredExtra, phkKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        try {
            Advapi32Util.registrySetLongValue(phkKey.getValue(), name, value);
        }
        finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static void registrySetStringValue(WinReg.HKEY hKey, String name, String value) {
        Memory data;
        if (value == null) {
            value = "";
        }
        if (W32APITypeMapper.DEFAULT == W32APITypeMapper.UNICODE) {
            data = new Memory((value.length() + 1) * Native.WCHAR_SIZE);
            data.setWideString(0L, value);
        } else {
            data = new Memory(value.length() + 1);
            data.setString(0L, value);
        }
        int rc = Advapi32.INSTANCE.RegSetValueEx(hKey, name, 0, 1, data, (int)data.size());
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
    }

    public static void registrySetStringValue(WinReg.HKEY root, String keyPath, String name, String value) {
        Advapi32Util.registrySetStringValue(root, keyPath, name, value, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void registrySetStringValue(WinReg.HKEY root, String keyPath, String name, String value, int samDesiredExtra) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, 0x2001F | samDesiredExtra, phkKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        try {
            Advapi32Util.registrySetStringValue(phkKey.getValue(), name, value);
        }
        finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static void registrySetExpandableStringValue(WinReg.HKEY hKey, String name, String value) {
        Memory data;
        if (W32APITypeMapper.DEFAULT == W32APITypeMapper.UNICODE) {
            data = new Memory((value.length() + 1) * Native.WCHAR_SIZE);
            data.setWideString(0L, value);
        } else {
            data = new Memory(value.length() + 1);
            data.setString(0L, value);
        }
        int rc = Advapi32.INSTANCE.RegSetValueEx(hKey, name, 0, 2, data, (int)data.size());
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
    }

    public static void registrySetExpandableStringValue(WinReg.HKEY root, String keyPath, String name, String value) {
        Advapi32Util.registrySetExpandableStringValue(root, keyPath, name, value, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void registrySetExpandableStringValue(WinReg.HKEY root, String keyPath, String name, String value, int samDesiredExtra) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, 0x2001F | samDesiredExtra, phkKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        try {
            Advapi32Util.registrySetExpandableStringValue(phkKey.getValue(), name, value);
        }
        finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static void registrySetStringArray(WinReg.HKEY hKey, String name, String[] arr) {
        int charwidth = W32APITypeMapper.DEFAULT == W32APITypeMapper.UNICODE ? Native.WCHAR_SIZE : 1;
        int size = 0;
        for (String s : arr) {
            size += s.length() * charwidth;
            size += charwidth;
        }
        int offset = 0;
        Memory data = new Memory(size += charwidth);
        data.clear();
        for (String s : arr) {
            if (W32APITypeMapper.DEFAULT == W32APITypeMapper.UNICODE) {
                data.setWideString(offset, s);
            } else {
                data.setString((long)offset, s);
            }
            offset += s.length() * charwidth;
            offset += charwidth;
        }
        int rc = Advapi32.INSTANCE.RegSetValueEx(hKey, name, 0, 7, data, size);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
    }

    public static void registrySetStringArray(WinReg.HKEY root, String keyPath, String name, String[] arr) {
        Advapi32Util.registrySetStringArray(root, keyPath, name, arr, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void registrySetStringArray(WinReg.HKEY root, String keyPath, String name, String[] arr, int samDesiredExtra) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, 0x2001F | samDesiredExtra, phkKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        try {
            Advapi32Util.registrySetStringArray(phkKey.getValue(), name, arr);
        }
        finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static void registrySetBinaryValue(WinReg.HKEY hKey, String name, byte[] data) {
        int rc = Advapi32.INSTANCE.RegSetValueEx(hKey, name, 0, 3, data, data.length);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
    }

    public static void registrySetBinaryValue(WinReg.HKEY root, String keyPath, String name, byte[] data) {
        Advapi32Util.registrySetBinaryValue(root, keyPath, name, data, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void registrySetBinaryValue(WinReg.HKEY root, String keyPath, String name, byte[] data, int samDesiredExtra) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, 0x2001F | samDesiredExtra, phkKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        try {
            Advapi32Util.registrySetBinaryValue(phkKey.getValue(), name, data);
        }
        finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static void registryDeleteKey(WinReg.HKEY hKey, String keyName) {
        int rc = Advapi32.INSTANCE.RegDeleteKey(hKey, keyName);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
    }

    public static void registryDeleteKey(WinReg.HKEY root, String keyPath, String keyName) {
        Advapi32Util.registryDeleteKey(root, keyPath, keyName, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void registryDeleteKey(WinReg.HKEY root, String keyPath, String keyName, int samDesiredExtra) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, 0x2001F | samDesiredExtra, phkKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        try {
            Advapi32Util.registryDeleteKey(phkKey.getValue(), keyName);
        }
        finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static void registryDeleteValue(WinReg.HKEY hKey, String valueName) {
        int rc = Advapi32.INSTANCE.RegDeleteValue(hKey, valueName);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
    }

    public static void registryDeleteValue(WinReg.HKEY root, String keyPath, String valueName) {
        Advapi32Util.registryDeleteValue(root, keyPath, valueName, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void registryDeleteValue(WinReg.HKEY root, String keyPath, String valueName, int samDesiredExtra) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, 0x2001F | samDesiredExtra, phkKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        try {
            Advapi32Util.registryDeleteValue(phkKey.getValue(), valueName);
        }
        finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static String[] registryGetKeys(WinReg.HKEY hKey) {
        IntByReference lpcSubKeys = new IntByReference();
        IntByReference lpcMaxSubKeyLen = new IntByReference();
        int rc = Advapi32.INSTANCE.RegQueryInfoKey(hKey, null, null, null, lpcSubKeys, lpcMaxSubKeyLen, null, null, null, null, null, null);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        ArrayList<String> keys = new ArrayList<String>(lpcSubKeys.getValue());
        char[] name = new char[lpcMaxSubKeyLen.getValue() + 1];
        for (int i = 0; i < lpcSubKeys.getValue(); ++i) {
            IntByReference lpcchValueName = new IntByReference(lpcMaxSubKeyLen.getValue() + 1);
            rc = Advapi32.INSTANCE.RegEnumKeyEx(hKey, i, name, lpcchValueName, null, null, null, null);
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
            keys.add(Native.toString(name));
        }
        return keys.toArray(new String[0]);
    }

    public static String[] registryGetKeys(WinReg.HKEY root, String keyPath) {
        return Advapi32Util.registryGetKeys(root, keyPath, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String[] registryGetKeys(WinReg.HKEY root, String keyPath, int samDesiredExtra) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, 0x20019 | samDesiredExtra, phkKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        try {
            String[] stringArray = Advapi32Util.registryGetKeys(phkKey.getValue());
            return stringArray;
        }
        finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static WinReg.HKEYByReference registryGetKey(WinReg.HKEY root, String keyPath, int samDesired) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, samDesired, phkKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        return phkKey;
    }

    public static WinReg.HKEYByReference registryLoadAppKey(String fileName, int samDesired, int dwOptions) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegLoadAppKey(fileName, phkKey, samDesired, dwOptions, 0);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        return phkKey;
    }

    public static void registryCloseKey(WinReg.HKEY hKey) {
        int rc = Advapi32.INSTANCE.RegCloseKey(hKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
    }

    public static TreeMap<String, Object> registryGetValues(WinReg.HKEY hKey) {
        IntByReference lpcValues = new IntByReference();
        IntByReference lpcMaxValueNameLen = new IntByReference();
        IntByReference lpcMaxValueLen = new IntByReference();
        int rc = Advapi32.INSTANCE.RegQueryInfoKey(hKey, null, null, null, null, null, null, lpcValues, lpcMaxValueNameLen, lpcMaxValueLen, null, null);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        TreeMap<String, Object> keyValues = new TreeMap<String, Object>();
        char[] name = new char[lpcMaxValueNameLen.getValue() + 1];
        Memory byteData = new Memory(lpcMaxValueLen.getValue() + 2 * Native.WCHAR_SIZE);
        block13: for (int i = 0; i < lpcValues.getValue(); ++i) {
            byteData.clear();
            IntByReference lpcchValueName = new IntByReference(lpcMaxValueNameLen.getValue() + 1);
            IntByReference lpcbData = new IntByReference(lpcMaxValueLen.getValue());
            IntByReference lpType = new IntByReference();
            rc = Advapi32.INSTANCE.RegEnumValue(hKey, i, name, lpcchValueName, null, lpType, byteData, lpcbData);
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
            String nameString = Native.toString(name);
            if (lpcbData.getValue() == 0) {
                switch (lpType.getValue()) {
                    case 3: {
                        keyValues.put(nameString, new byte[0]);
                        continue block13;
                    }
                    case 1: 
                    case 2: {
                        keyValues.put(nameString, new char[0]);
                        continue block13;
                    }
                    case 7: {
                        keyValues.put(nameString, new String[0]);
                        continue block13;
                    }
                    case 0: {
                        keyValues.put(nameString, null);
                        continue block13;
                    }
                    default: {
                        throw new RuntimeException("Unsupported empty type: " + lpType.getValue());
                    }
                }
            }
            switch (lpType.getValue()) {
                case 11: {
                    keyValues.put(nameString, byteData.getLong(0L));
                    continue block13;
                }
                case 4: {
                    keyValues.put(nameString, byteData.getInt(0L));
                    continue block13;
                }
                case 1: 
                case 2: {
                    if (W32APITypeMapper.DEFAULT == W32APITypeMapper.UNICODE) {
                        keyValues.put(nameString, byteData.getWideString(0L));
                        continue block13;
                    }
                    keyValues.put(nameString, byteData.getString(0L));
                    continue block13;
                }
                case 3: {
                    keyValues.put(nameString, byteData.getByteArray(0L, lpcbData.getValue()));
                    continue block13;
                }
                case 7: {
                    ArrayList<String> result = new ArrayList<String>();
                    int offset = 0;
                    while ((long)offset < byteData.size()) {
                        String s;
                        if (W32APITypeMapper.DEFAULT == W32APITypeMapper.UNICODE) {
                            s = byteData.getWideString(offset);
                            offset += s.length() * Native.WCHAR_SIZE;
                            offset += Native.WCHAR_SIZE;
                        } else {
                            s = byteData.getString(offset);
                            offset += s.length();
                            ++offset;
                        }
                        if (s.length() == 0) break;
                        result.add(s);
                    }
                    keyValues.put(nameString, result.toArray(new String[0]));
                    continue block13;
                }
                default: {
                    throw new RuntimeException("Unsupported type: " + lpType.getValue());
                }
            }
        }
        return keyValues;
    }

    public static TreeMap<String, Object> registryGetValues(WinReg.HKEY root, String keyPath) {
        return Advapi32Util.registryGetValues(root, keyPath, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static TreeMap<String, Object> registryGetValues(WinReg.HKEY root, String keyPath, int samDesiredExtra) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0, 0x20019 | samDesiredExtra, phkKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        try {
            TreeMap<String, Object> treeMap = Advapi32Util.registryGetValues(phkKey.getValue());
            return treeMap;
        }
        finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != 0) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static InfoKey registryQueryInfoKey(WinReg.HKEY hKey, int lpcbSecurityDescriptor) {
        InfoKey infoKey = new InfoKey(hKey, lpcbSecurityDescriptor);
        int rc = Advapi32.INSTANCE.RegQueryInfoKey(hKey, infoKey.lpClass, infoKey.lpcClass, null, infoKey.lpcSubKeys, infoKey.lpcMaxSubKeyLen, infoKey.lpcMaxClassLen, infoKey.lpcValues, infoKey.lpcMaxValueNameLen, infoKey.lpcMaxValueLen, infoKey.lpcbSecurityDescriptor, infoKey.lpftLastWriteTime);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        return infoKey;
    }

    public static EnumKey registryRegEnumKey(WinReg.HKEY hKey, int dwIndex) {
        EnumKey enumKey = new EnumKey(hKey, dwIndex);
        int rc = Advapi32.INSTANCE.RegEnumKeyEx(hKey, enumKey.dwIndex, enumKey.lpName, enumKey.lpcName, null, enumKey.lpClass, enumKey.lpcbClass, enumKey.lpftLastWriteTime);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        return enumKey;
    }

    public static String getEnvironmentBlock(Map<String, String> environment) {
        StringBuilder out = new StringBuilder(environment.size() * 32);
        for (Map.Entry<String, String> entry : environment.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value == null) continue;
            out.append(key).append("=").append(value).append('\u0000');
        }
        return out.append('\u0000').toString();
    }

    public static WinNT.ACE_HEADER[] getFileSecurity(String fileName, boolean compact) {
        Memory memory;
        boolean repeat;
        int infoType = 4;
        int nLength = 1024;
        do {
            int lengthNeeded;
            repeat = false;
            memory = new Memory(nLength);
            IntByReference lpnSize = new IntByReference();
            boolean succeded = Advapi32.INSTANCE.GetFileSecurity(fileName, infoType, memory, nLength, lpnSize);
            if (!succeded) {
                int lastError = Kernel32.INSTANCE.GetLastError();
                memory.clear();
                if (122 != lastError) {
                    throw new Win32Exception(lastError);
                }
            }
            if (nLength >= (lengthNeeded = lpnSize.getValue())) continue;
            repeat = true;
            nLength = lengthNeeded;
            memory.clear();
        } while (repeat);
        WinNT.SECURITY_DESCRIPTOR_RELATIVE sdr = new WinNT.SECURITY_DESCRIPTOR_RELATIVE(memory);
        WinNT.ACL dacl = sdr.getDiscretionaryACL();
        WinNT.ACE_HEADER[] aceStructures = dacl.getACEs();
        if (compact) {
            ArrayList<Object> result = new ArrayList<Object>();
            HashMap<String, WinNT.ACCESS_ACEStructure> aceMap = new HashMap<String, WinNT.ACCESS_ACEStructure>();
            for (WinNT.ACE_HEADER aceStructure : aceStructures) {
                if (aceStructure instanceof WinNT.ACCESS_ACEStructure) {
                    WinNT.ACCESS_ACEStructure accessACEStructure = (WinNT.ACCESS_ACEStructure)aceStructure;
                    boolean inherted = (aceStructure.AceFlags & 0x1F) != 0;
                    String key = accessACEStructure.getSidString() + "/" + inherted + "/" + aceStructure.getClass().getName();
                    WinNT.ACCESS_ACEStructure aceStructure2 = (WinNT.ACCESS_ACEStructure)aceMap.get(key);
                    if (aceStructure2 != null) {
                        int accessMask = aceStructure2.Mask;
                        aceStructure2.Mask = accessMask |= accessACEStructure.Mask;
                        continue;
                    }
                    aceMap.put(key, accessACEStructure);
                    result.add(aceStructure2);
                    continue;
                }
                result.add(aceStructure);
            }
            return result.toArray(new WinNT.ACE_HEADER[0]);
        }
        return aceStructures;
    }

    private static Memory getSecurityDescriptorForFile(String absoluteFilePath) {
        int lastError;
        int infoType = 7;
        IntByReference lpnSize = new IntByReference();
        boolean succeeded = Advapi32.INSTANCE.GetFileSecurity(absoluteFilePath, 7, null, 0, lpnSize);
        if (!succeeded && 122 != (lastError = Kernel32.INSTANCE.GetLastError())) {
            throw new Win32Exception(lastError);
        }
        int nLength = lpnSize.getValue();
        Memory securityDescriptorMemoryPointer = new Memory(nLength);
        succeeded = Advapi32.INSTANCE.GetFileSecurity(absoluteFilePath, 7, securityDescriptorMemoryPointer, nLength, lpnSize);
        if (!succeeded) {
            securityDescriptorMemoryPointer.clear();
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        return securityDescriptorMemoryPointer;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Memory getSecurityDescriptorForObject(String absoluteObjectPath, int objectType, boolean getSACL) {
        PointerByReference ppSecurityDescriptor;
        int infoType = 7 | (getSACL ? 8 : 0);
        int lastError = Advapi32.INSTANCE.GetNamedSecurityInfo(absoluteObjectPath, objectType, infoType, null, null, null, null, ppSecurityDescriptor = new PointerByReference());
        if (lastError != 0) {
            throw new Win32Exception(lastError);
        }
        int nLength = Advapi32.INSTANCE.GetSecurityDescriptorLength(ppSecurityDescriptor.getValue());
        Memory memory = new Memory(nLength);
        Pointer secValue = ppSecurityDescriptor.getValue();
        try {
            byte[] data = secValue.getByteArray(0L, nLength);
            memory.write(0L, data, 0, nLength);
            Memory memory2 = memory;
            return memory2;
        }
        finally {
            Kernel32Util.freeLocalMemory(secValue);
        }
    }

    public static void setSecurityDescriptorForObject(String absoluteObjectPath, int objectType, WinNT.SECURITY_DESCRIPTOR_RELATIVE securityDescriptor, boolean setOwner, boolean setGroup, boolean setDACL, boolean setSACL, boolean setDACLProtectedStatus, boolean setSACLProtectedStatus) {
        int lastError;
        WinNT.PSID psidOwner = securityDescriptor.getOwner();
        WinNT.PSID psidGroup = securityDescriptor.getGroup();
        WinNT.ACL dacl = securityDescriptor.getDiscretionaryACL();
        WinNT.ACL sacl = securityDescriptor.getSystemACL();
        int infoType = 0;
        if (setOwner) {
            if (psidOwner == null) {
                throw new IllegalArgumentException("SECURITY_DESCRIPTOR_RELATIVE does not contain owner");
            }
            if (!Advapi32.INSTANCE.IsValidSid(psidOwner)) {
                throw new IllegalArgumentException("Owner PSID is invalid");
            }
            infoType |= 1;
        }
        if (setGroup) {
            if (psidGroup == null) {
                throw new IllegalArgumentException("SECURITY_DESCRIPTOR_RELATIVE does not contain group");
            }
            if (!Advapi32.INSTANCE.IsValidSid(psidGroup)) {
                throw new IllegalArgumentException("Group PSID is invalid");
            }
            infoType |= 2;
        }
        if (setDACL) {
            if (dacl == null) {
                throw new IllegalArgumentException("SECURITY_DESCRIPTOR_RELATIVE does not contain DACL");
            }
            if (!Advapi32.INSTANCE.IsValidAcl(dacl.getPointer())) {
                throw new IllegalArgumentException("DACL is invalid");
            }
            infoType |= 4;
        }
        if (setSACL) {
            if (sacl == null) {
                throw new IllegalArgumentException("SECURITY_DESCRIPTOR_RELATIVE does not contain SACL");
            }
            if (!Advapi32.INSTANCE.IsValidAcl(sacl.getPointer())) {
                throw new IllegalArgumentException("SACL is invalid");
            }
            infoType |= 8;
        }
        if (setDACLProtectedStatus) {
            if ((securityDescriptor.Control & 0x1000) != 0) {
                infoType |= Integer.MIN_VALUE;
            } else if ((securityDescriptor.Control & 0x1000) == 0) {
                infoType |= 0x20000000;
            }
        }
        if (setSACLProtectedStatus) {
            if ((securityDescriptor.Control & 0x2000) != 0) {
                infoType |= 0x40000000;
            } else if ((securityDescriptor.Control & 0x2000) == 0) {
                infoType |= 0x10000000;
            }
        }
        if ((lastError = Advapi32.INSTANCE.SetNamedSecurityInfo(absoluteObjectPath, objectType, infoType, setOwner ? psidOwner.getPointer() : null, setGroup ? psidGroup.getPointer() : null, setDACL ? dacl.getPointer() : null, setSACL ? sacl.getPointer() : null)) != 0) {
            throw new Win32Exception(lastError);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static boolean accessCheck(File file, AccessCheckPermission permissionToCheck) {
        boolean bl;
        Memory securityDescriptorMemoryPointer = Advapi32Util.getSecurityDescriptorForFile(file.getAbsolutePath().replace('/', '\\'));
        WinNT.HANDLEByReference openedAccessToken = new WinNT.HANDLEByReference();
        WinNT.HANDLEByReference duplicatedToken = new WinNT.HANDLEByReference();
        Win32Exception err = null;
        try {
            int desireAccess = 131086;
            WinNT.HANDLE hProcess = Kernel32.INSTANCE.GetCurrentProcess();
            if (!Advapi32.INSTANCE.OpenProcessToken(hProcess, desireAccess, openedAccessToken)) {
                throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
            }
            if (!Advapi32.INSTANCE.DuplicateToken(openedAccessToken.getValue(), 2, duplicatedToken)) {
                throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
            }
            WinNT.GENERIC_MAPPING mapping = new WinNT.GENERIC_MAPPING();
            mapping.genericRead = new WinDef.DWORD(1179785L);
            mapping.genericWrite = new WinDef.DWORD(1179926L);
            mapping.genericExecute = new WinDef.DWORD(1179808L);
            mapping.genericAll = new WinDef.DWORD(0x1F01FFL);
            WinDef.DWORDByReference rights = new WinDef.DWORDByReference(new WinDef.DWORD((long)permissionToCheck.getCode()));
            Advapi32.INSTANCE.MapGenericMask(rights, mapping);
            WinNT.PRIVILEGE_SET privileges = new WinNT.PRIVILEGE_SET(1);
            privileges.PrivilegeCount = new WinDef.DWORD(0L);
            WinDef.DWORDByReference privilegeLength = new WinDef.DWORDByReference(new WinDef.DWORD((long)privileges.size()));
            WinDef.DWORDByReference grantedAccess = new WinDef.DWORDByReference();
            WinDef.BOOLByReference result = new WinDef.BOOLByReference();
            if (!Advapi32.INSTANCE.AccessCheck(securityDescriptorMemoryPointer, duplicatedToken.getValue(), rights.getValue(), mapping, privileges, privilegeLength, grantedAccess, result)) {
                throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
            }
            bl = result.getValue().booleanValue();
        }
        catch (Win32Exception e) {
            try {
                err = e;
                throw err;
            }
            catch (Throwable throwable) {
                block17: {
                    try {
                        Kernel32Util.closeHandleRefs(openedAccessToken, duplicatedToken);
                    }
                    catch (Win32Exception e2) {
                        if (err == null) {
                            err = e2;
                            break block17;
                        }
                        err.addSuppressedReflected(e2);
                    }
                }
                if (securityDescriptorMemoryPointer != null) {
                    securityDescriptorMemoryPointer.clear();
                }
                if (err != null) {
                    throw err;
                }
                throw throwable;
            }
        }
        try {
            Kernel32Util.closeHandleRefs(openedAccessToken, duplicatedToken);
        }
        catch (Win32Exception e) {
            if (err == null) {
                err = e;
            }
            err.addSuppressedReflected(e);
        }
        if (securityDescriptorMemoryPointer != null) {
            securityDescriptorMemoryPointer.clear();
        }
        if (err != null) {
            throw err;
        }
        return bl;
    }

    public static WinNT.SECURITY_DESCRIPTOR_RELATIVE getFileSecurityDescriptor(File file, boolean getSACL) {
        Memory securityDesc = Advapi32Util.getSecurityDescriptorForObject(file.getAbsolutePath().replaceAll("/", "\\"), 1, getSACL);
        WinNT.SECURITY_DESCRIPTOR_RELATIVE sdr = new WinNT.SECURITY_DESCRIPTOR_RELATIVE(securityDesc);
        return sdr;
    }

    public static void setFileSecurityDescriptor(File file, WinNT.SECURITY_DESCRIPTOR_RELATIVE securityDescriptor, boolean setOwner, boolean setGroup, boolean setDACL, boolean setSACL, boolean setDACLProtectedStatus, boolean setSACLProtectedStatus) {
        Advapi32Util.setSecurityDescriptorForObject(file.getAbsolutePath().replaceAll("/", "\\"), 1, securityDescriptor, setOwner, setGroup, setDACL, setSACL, setDACLProtectedStatus, setSACLProtectedStatus);
    }

    public static void encryptFile(File file) {
        String lpFileName = file.getAbsolutePath();
        if (!Advapi32.INSTANCE.EncryptFile(lpFileName)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
    }

    public static void decryptFile(File file) {
        String lpFileName = file.getAbsolutePath();
        if (!Advapi32.INSTANCE.DecryptFile(lpFileName, new WinDef.DWORD(0L))) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
    }

    public static int fileEncryptionStatus(File file) {
        WinDef.DWORDByReference status = new WinDef.DWORDByReference();
        String lpFileName = file.getAbsolutePath();
        if (!Advapi32.INSTANCE.FileEncryptionStatus(lpFileName, status)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        return status.getValue().intValue();
    }

    public static void disableEncryption(File directory, boolean disable) {
        String dirPath = directory.getAbsolutePath();
        if (!Advapi32.INSTANCE.EncryptionDisable(dirPath, disable)) {
            throw new Win32Exception(Native.getLastError());
        }
    }

    public static void backupEncryptedFile(File src, File destDir) {
        PointerByReference pvContext;
        String srcFileName;
        if (!destDir.isDirectory()) {
            throw new IllegalArgumentException("destDir must be a directory.");
        }
        WinDef.ULONG readFlag = new WinDef.ULONG(0L);
        WinDef.ULONG writeFlag = new WinDef.ULONG(1L);
        if (src.isDirectory()) {
            writeFlag.setValue(3L);
        }
        if (Advapi32.INSTANCE.OpenEncryptedFileRaw(srcFileName = src.getAbsolutePath(), readFlag, pvContext = new PointerByReference()) != 0) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        1 pfExportCallback = new /* Unavailable Anonymous Inner Class!! */;
        if (Advapi32.INSTANCE.ReadEncryptedFileRaw((WinBase.FE_EXPORT_FUNC)pfExportCallback, null, pvContext.getValue()) != 0) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        try {
            outputStream.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        Advapi32.INSTANCE.CloseEncryptedFileRaw(pvContext.getValue());
        String destFileName = destDir.getAbsolutePath() + File.separator + src.getName();
        pvContext = new PointerByReference();
        if (Advapi32.INSTANCE.OpenEncryptedFileRaw(destFileName, writeFlag, pvContext) != 0) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        IntByReference elementsReadWrapper = new IntByReference(0);
        2 pfImportCallback = new /* Unavailable Anonymous Inner Class!! */;
        if (Advapi32.INSTANCE.WriteEncryptedFileRaw((WinBase.FE_IMPORT_FUNC)pfImportCallback, null, pvContext.getValue()) != 0) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        Advapi32.INSTANCE.CloseEncryptedFileRaw(pvContext.getValue());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean isCurrentProcessElevated() {
        WinNT.HANDLEByReference hToken = new WinNT.HANDLEByReference();
        IntByReference returnLength = new IntByReference();
        if (Advapi32.INSTANCE.OpenProcessToken(Kernel32.INSTANCE.GetCurrentProcess(), 8, hToken)) {
            try {
                WinNT.TOKEN_ELEVATION elevation = new WinNT.TOKEN_ELEVATION();
                if (Advapi32.INSTANCE.GetTokenInformation(hToken.getValue(), 20, elevation, elevation.size(), returnLength)) {
                    boolean bl = elevation.TokenIsElevated > 0;
                    return bl;
                }
            }
            finally {
                Kernel32.INSTANCE.CloseHandle(hToken.getValue());
            }
        }
        return false;
    }

    public static class EventLogIterator
    implements Iterable<EventLogRecord>,
    Iterator<EventLogRecord> {
        private WinNT.HANDLE _h;
        private Memory _buffer = new Memory(65536L);
        private boolean _done = false;
        private int _dwRead = 0;
        private Pointer _pevlr = null;
        private int _flags;

        public EventLogIterator(String sourceName) {
            this(null, sourceName, 4);
        }

        public EventLogIterator(String serverName, String sourceName, int flags) {
            this._flags = flags;
            this._h = Advapi32.INSTANCE.OpenEventLog(serverName, sourceName);
            if (this._h == null) {
                throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
            }
        }

        private boolean read() {
            if (this._done || this._dwRead > 0) {
                return false;
            }
            IntByReference pnBytesRead = new IntByReference();
            IntByReference pnMinNumberOfBytesNeeded = new IntByReference();
            if (!Advapi32.INSTANCE.ReadEventLog(this._h, 1 | this._flags, 0, this._buffer, (int)this._buffer.size(), pnBytesRead, pnMinNumberOfBytesNeeded)) {
                int rc = Kernel32.INSTANCE.GetLastError();
                if (rc == 122) {
                    this._buffer = new Memory(pnMinNumberOfBytesNeeded.getValue());
                    if (!Advapi32.INSTANCE.ReadEventLog(this._h, 1 | this._flags, 0, this._buffer, (int)this._buffer.size(), pnBytesRead, pnMinNumberOfBytesNeeded)) {
                        throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
                    }
                } else {
                    this.close();
                    if (rc != 38) {
                        throw new Win32Exception(rc);
                    }
                    return false;
                }
            }
            this._dwRead = pnBytesRead.getValue();
            this._pevlr = this._buffer;
            return true;
        }

        public void close() {
            this._done = true;
            if (this._h != null) {
                if (!Advapi32.INSTANCE.CloseEventLog(this._h)) {
                    throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
                }
                this._h = null;
            }
        }

        @Override
        public Iterator<EventLogRecord> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            this.read();
            return !this._done;
        }

        @Override
        public EventLogRecord next() {
            this.read();
            EventLogRecord record = new EventLogRecord(this._pevlr);
            this._dwRead -= record.getLength();
            this._pevlr = this._pevlr.share(record.getLength());
            return record;
        }

        @Override
        public void remove() {
        }
    }

    public static class EventLogRecord {
        private WinNT.EVENTLOGRECORD _record;
        private String _source;
        private byte[] _data;
        private String[] _strings;

        public WinNT.EVENTLOGRECORD getRecord() {
            return this._record;
        }

        public int getInstanceId() {
            return this._record.EventID.intValue();
        }

        @Deprecated
        public int getEventId() {
            return this._record.EventID.intValue();
        }

        public String getSource() {
            return this._source;
        }

        public int getStatusCode() {
            return this._record.EventID.intValue() & 0xFFFF;
        }

        public int getRecordNumber() {
            return this._record.RecordNumber.intValue();
        }

        public int getLength() {
            return this._record.Length.intValue();
        }

        public String[] getStrings() {
            return this._strings;
        }

        public EventLogType getType() {
            switch (this._record.EventType.intValue()) {
                case 0: 
                case 4: {
                    return EventLogType.Informational;
                }
                case 16: {
                    return EventLogType.AuditFailure;
                }
                case 8: {
                    return EventLogType.AuditSuccess;
                }
                case 1: {
                    return EventLogType.Error;
                }
                case 2: {
                    return EventLogType.Warning;
                }
            }
            throw new RuntimeException("Invalid type: " + this._record.EventType.intValue());
        }

        public byte[] getData() {
            return this._data;
        }

        public EventLogRecord(Pointer pevlr) {
            this._record = new WinNT.EVENTLOGRECORD(pevlr);
            this._source = pevlr.getWideString(this._record.size());
            if (this._record.DataLength.intValue() > 0) {
                this._data = pevlr.getByteArray(this._record.DataOffset.intValue(), this._record.DataLength.intValue());
            }
            if (this._record.NumStrings.intValue() > 0) {
                ArrayList<String> strings = new ArrayList<String>();
                long offset = this._record.StringOffset.intValue();
                for (int count = this._record.NumStrings.intValue(); count > 0; --count) {
                    String s = pevlr.getWideString(offset);
                    strings.add(s);
                    offset += (long)(s.length() * Native.WCHAR_SIZE);
                    offset += (long)Native.WCHAR_SIZE;
                }
                this._strings = strings.toArray(new String[0]);
            }
        }
    }
}

