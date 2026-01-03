/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

import java.awt.Desktop;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;
import org.pbrands.hid.HIDDeviceType;

public class RegistryUtil {
    private static final Preferences prefs = Preferences.userRoot().node("org/pbrands/lifebot");
    private static final String KEY_AUTH_TOKEN = "authToken";
    private static final String KEY_DEVICE_TYPE = "deviceType";
    private static final String KEY_FOLDER_NAME = "folderName";
    private static final String KEY_HID_DEVICE_TYPE = "hidDeviceType";
    private static final String KEY_LAST_SELECTED_PROGRAM_ID = "lastSelectedProgramId";
    public static final Path DIR_PATH = Paths.get(System.getenv("LOCALAPPDATA"), "LifeBot");

    public static Path getLogsDir() {
        return DIR_PATH.resolve("logs");
    }

    public static Path getScreenshotsDir() {
        return DIR_PATH.resolve("screenshots");
    }

    public static void ensureDirectoriesExist() {
        RegistryUtil.getLogsDir().toFile().mkdirs();
        RegistryUtil.getScreenshotsDir().toFile().mkdirs();
    }

    public static void openInExplorer(Path path) {
        try {
            path.toFile().mkdirs();
            Desktop.getDesktop().open(path.toFile());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getAuthToken() {
        return prefs.get(KEY_AUTH_TOKEN, null);
    }

    public static void setAuthToken(String authToken) {
        if (authToken == null || authToken.isEmpty()) {
            prefs.remove(KEY_AUTH_TOKEN);
        } else {
            prefs.put(KEY_AUTH_TOKEN, authToken);
        }
    }

    public static void clearAuthToken() {
        prefs.remove(KEY_AUTH_TOKEN);
    }

    public static String getDeviceType() {
        return prefs.get(KEY_DEVICE_TYPE, null);
    }

    public static void setDeviceType(String deviceType) {
        if (deviceType == null || deviceType.isEmpty()) {
            prefs.remove(KEY_DEVICE_TYPE);
        } else {
            prefs.put(KEY_DEVICE_TYPE, deviceType);
        }
    }

    public static String getFolderName() {
        return prefs.get(KEY_FOLDER_NAME, null);
    }

    public static void setFolderName(String folderName) {
        if (folderName == null || folderName.isEmpty()) {
            prefs.remove(KEY_FOLDER_NAME);
        } else {
            prefs.put(KEY_FOLDER_NAME, folderName);
        }
    }

    public static HIDDeviceType getHIDDeviceType() {
        String value = prefs.get(KEY_HID_DEVICE_TYPE, HIDDeviceType.UBER.name());
        try {
            return HIDDeviceType.valueOf(value);
        }
        catch (IllegalArgumentException e) {
            RegistryUtil.setHIDDeviceType(HIDDeviceType.UBER);
            return HIDDeviceType.UBER;
        }
    }

    public static void setHIDDeviceType(HIDDeviceType type) {
        prefs.put(KEY_HID_DEVICE_TYPE, type.name());
    }

    public static int getLastSelectedProgramId() {
        return prefs.getInt(KEY_LAST_SELECTED_PROGRAM_ID, 0);
    }

    public static void setLastSelectedProgramId(int programId) {
        prefs.putInt(KEY_LAST_SELECTED_PROGRAM_ID, programId);
    }

    public static void clearAll() {
        try {
            prefs.clear();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isFirstRun() {
        return RegistryUtil.getAuthToken() == null;
    }
}

