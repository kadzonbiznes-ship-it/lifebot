/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObfuscatedStorage {
    private static final Logger logger = LoggerFactory.getLogger(ObfuscatedStorage.class);
    public static final String UI_CONFIG_NAME = "cache.dat";
    public static final String LEARNING_NAME = "profile.dat";
    public static final String SETTINGS_NAME = "preferences.dat";
    private static byte[] encryptionKey = null;
    private static boolean initialized = false;

    public static void initialize(byte[] key) {
        if (key == null || key.length == 0) {
            logger.error("Invalid encryption key provided");
            return;
        }
        encryptionKey = (byte[])key.clone();
        initialized = true;
        logger.debug("ObfuscatedStorage initialized");
    }

    private static byte[] xorCrypt(byte[] data) {
        if (encryptionKey == null) {
            throw new IllegalStateException("ObfuscatedStorage not initialized. Call initialize() first.");
        }
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; ++i) {
            result[i] = (byte)(data[i] ^ encryptionKey[i % encryptionKey.length]);
        }
        return result;
    }

    public static void writeEncrypted(File file, String content) throws IOException {
        if (!initialized) {
            logger.warn("ObfuscatedStorage not initialized, writing plain text to {}", (Object)file.getName());
            try (FileWriter writer = new FileWriter(file);){
                writer.write(content);
            }
            return;
        }
        byte[] data = content.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = ObfuscatedStorage.xorCrypt(data);
        try (FileOutputStream fos = new FileOutputStream(file);){
            fos.write(76);
            fos.write(66);
            fos.write(88);
            fos.write(1);
            fos.write(encrypted);
        }
        logger.debug("Wrote encrypted file: {} ({} bytes)", (Object)file.getName(), (Object)encrypted.length);
    }

    public static String readEncrypted(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        try (FileInputStream fis = new FileInputStream(file);){
            String string;
            int m1 = fis.read();
            int m2 = fis.read();
            int m3 = fis.read();
            int version = fis.read();
            if (m1 == 76 && m2 == 66 && m3 == 88) {
                if (!initialized) {
                    throw new IOException("Cannot read encrypted file - ObfuscatedStorage not initialized");
                }
                byte[] encrypted = fis.readAllBytes();
                byte[] decrypted = ObfuscatedStorage.xorCrypt(encrypted);
                String string2 = new String(decrypted, StandardCharsets.UTF_8);
                return string2;
            }
            fis.close();
            try (BufferedReader reader = new BufferedReader(new FileReader(file));){
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                string = sb.toString();
            }
            return string;
        }
    }

    public static boolean migrateToEncrypted(File oldFile, File newFile) {
        if (!oldFile.exists()) {
            return false;
        }
        if (!initialized) {
            logger.warn("Cannot migrate - ObfuscatedStorage not initialized");
            return false;
        }
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(oldFile));){
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            ObfuscatedStorage.writeEncrypted(newFile, sb.toString());
            if (!oldFile.equals(newFile) && oldFile.exists()) {
                oldFile.delete();
            }
            logger.info("Migrated {} to encrypted {}", (Object)oldFile.getName(), (Object)newFile.getName());
            return true;
        }
        catch (IOException e) {
            logger.error("Failed to migrate file to encrypted format", e);
            return false;
        }
    }

    public static File getObfuscatedFile(File folder, String purpose) {
        return new File(folder, purpose);
    }

    @Generated
    public static boolean isInitialized() {
        return initialized;
    }
}

