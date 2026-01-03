/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class JarCrypto {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 12;
    private static final int TAG_SIZE = 128;

    public static String generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, new SecureRandom());
        SecretKey key = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static byte[] encrypt(byte[] data, String base64Key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(1, (Key)keySpec, gcmSpec);
        byte[] ciphertext = cipher.doFinal(data);
        byte[] result = new byte[12 + ciphertext.length];
        System.arraycopy(iv, 0, result, 0, 12);
        System.arraycopy(ciphertext, 0, result, 12, ciphertext.length);
        return result;
    }

    public static byte[] decrypt(byte[] encryptedData, String base64Key) throws Exception {
        if (encryptedData.length < 28) {
            throw new IllegalArgumentException("Encrypted data too short");
        }
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        byte[] iv = new byte[12];
        System.arraycopy(encryptedData, 0, iv, 0, 12);
        byte[] ciphertext = new byte[encryptedData.length - 12];
        System.arraycopy(encryptedData, 12, ciphertext, 0, ciphertext.length);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(2, (Key)keySpec, gcmSpec);
        return cipher.doFinal(ciphertext);
    }

    public static void main(String[] args) throws Exception {
        String key = JarCrypto.generateKey();
        System.out.println("Generated key: " + key);
        byte[] original = "Hello, encrypted world!".getBytes();
        byte[] encrypted = JarCrypto.encrypt(original, key);
        byte[] decrypted = JarCrypto.decrypt(encrypted, key);
        System.out.println("Original: " + new String(original));
        System.out.println("Encrypted size: " + encrypted.length);
        System.out.println("Decrypted: " + new String(decrypted));
        System.out.println("Match: " + Arrays.equals(original, decrypted));
    }
}

