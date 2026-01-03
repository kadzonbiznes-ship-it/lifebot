/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

public class HWID {
    private static final String CACHE_KEY = "StartupConfig";

    public static String generate() {
        String regPath = "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\Accessibility";
        try {
            byte[] encrypted;
            String cached;
            if (Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, regPath, CACHE_KEY) && (cached = HWID.decrypt(encrypted = Advapi32Util.registryGetBinaryValue(WinReg.HKEY_LOCAL_MACHINE, regPath, CACHE_KEY))) != null && cached.length() == 32) {
                return cached;
            }
        }
        catch (Exception encrypted) {
            // empty catch block
        }
        String hwid = HWID.hash(HWID.getRawHWID());
        try {
            byte[] encrypted;
            if (Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, regPath) && (encrypted = HWID.encrypt(hwid)) != null) {
                Advapi32Util.registrySetBinaryValue(WinReg.HKEY_LOCAL_MACHINE, regPath, CACHE_KEY, encrypted);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return hwid;
    }

    private static byte[] getLocalKey() {
        try {
            String cpuId;
            StringBuilder sb = new StringBuilder();
            String computerName = System.getenv("COMPUTERNAME");
            if (computerName != null) {
                sb.append(computerName);
            }
            if ((cpuId = System.getenv("PROCESSOR_IDENTIFIER")) != null) {
                sb.append(cpuId);
            }
            sb.append(Runtime.getRuntime().availableProcessors());
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            return Arrays.copyOf(sha.digest(sb.toString().getBytes(StandardCharsets.UTF_8)), 16);
        }
        catch (Exception e) {
            return new byte[16];
        }
    }

    private static byte[] encrypt(String data) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(HWID.getLocalKey(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(1, secretKey);
            return cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String decrypt(byte[] data) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(HWID.getLocalKey(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(2, secretKey);
            return new String(cipher.doFinal(data), StandardCharsets.UTF_8);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String getRawHWID() {
        String cpuId;
        String boardSerial;
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        StringBuilder sb = new StringBuilder();
        String systemUUID = hal.getComputerSystem().getHardwareUUID();
        if (HWID.isValid(systemUUID)) {
            sb.append(systemUUID);
        }
        if (HWID.isValid(boardSerial = hal.getComputerSystem().getBaseboard().getSerialNumber())) {
            sb.append(boardSerial);
        }
        if (HWID.isValid(cpuId = hal.getProcessor().getProcessorIdentifier().getProcessorID())) {
            sb.append(cpuId);
        }
        try {
            List<HWDiskStore> disks = hal.getDiskStores();
            disks.sort(Comparator.comparing(HWDiskStore::getName));
            for (HWDiskStore disk : disks) {
                String serial;
                if (disk.getSize() <= 0x280000000L || !HWID.isValid(serial = disk.getSerial())) continue;
                sb.append(serial.trim());
                break;
            }
        }
        catch (Exception e) {
            System.err.println("Failed to retrieve disk serial: " + e.getMessage());
        }
        if (sb.length() == 0) {
            return "UNKNOWN_HWID";
        }
        return sb.toString();
    }

    private static boolean isValid(String s) {
        return s != null && !s.isEmpty() && !"unknown".equalsIgnoreCase(s) && !"n/a".equalsIgnoreCase(s);
    }

    private static String hash(String hwid) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(hwid.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}

