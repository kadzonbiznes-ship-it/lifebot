/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

import com.sun.jna.Native;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import org.pbrands.hid.kfc.api.KfcLibrary;
import org.pbrands.hid.uber.api.FortniteHook;
import org.pbrands.util.CoreConstants;

public class LoaderUtil {
    public static KfcLibrary KFC_INSTANCE;
    public static FortniteHook FORTNITE_INSTANCE;

    private static File getLoaderFile() {
        File nativeDir = CoreConstants.NATIVE_FOLDER;
        nativeDir.mkdirs();
        return new File(nativeDir, "loader.dll");
    }

    public static void load() {
        File file = LoaderUtil.getLoaderFile();
        String absolutePath = file.getAbsolutePath();
        System.out.println("[DEBUG] Loading library from: " + absolutePath);
        if (KFC_INSTANCE == null) {
            try {
                KFC_INSTANCE = Native.load(absolutePath, KfcLibrary.class);
            }
            catch (UnsatisfiedLinkError e) {
                System.err.println("[ERROR] Failed to load loader.dll (KFC). Missing dependencies? " + e.getMessage());
                throw e;
            }
        }
        if (FORTNITE_INSTANCE == null) {
            try {
                FORTNITE_INSTANCE = Native.load(absolutePath, FortniteHook.class);
            }
            catch (UnsatisfiedLinkError e) {
                System.err.println("[ERROR] Failed to load loader.dll (Fortnite). Missing dependencies? " + e.getMessage());
                throw e;
            }
        }
    }

    public static void load(byte[] dllBytes) {
        File loaderFile = LoaderUtil.getLoaderFile();
        if (loaderFile.exists()) {
            LoaderUtil.load();
            return;
        }
        try {
            loaderFile.getParentFile().mkdirs();
            Files.write(loaderFile.toPath(), dllBytes, new OpenOption[0]);
        }
        catch (IOException e) {
            if (loaderFile.exists()) {
                LoaderUtil.load();
                return;
            }
            throw new RuntimeException(e);
        }
        LoaderUtil.load();
    }

    public static String getLoaderChecksum() {
        File file = LoaderUtil.getLoaderFile();
        if (!file.exists()) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (InputStream is = Files.newInputStream(file.toPath(), new OpenOption[0]);
                 DigestInputStream dis = new DigestInputStream(is, md);){
                while (dis.read() != -1) {
                }
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}

