/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DllEncryptorUtil {
    public static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_SIZE = 16;

    public static byte[] encrypt(byte[] data, SecretKey key) throws Exception {
        byte[] iv = DllEncryptorUtil.generateIV();
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(1, (Key)key, ivSpec);
        byte[] encryptedData = cipher.doFinal(data);
        byte[] ivAndEncryptedData = new byte[16 + encryptedData.length];
        System.arraycopy(iv, 0, ivAndEncryptedData, 0, 16);
        System.arraycopy(encryptedData, 0, ivAndEncryptedData, 16, encryptedData.length);
        return ivAndEncryptedData;
    }

    public static byte[] decrypt(byte[] ivAndEncryptedData, SecretKey key) throws Exception {
        if (ivAndEncryptedData.length < 16) {
            throw new IllegalArgumentException("Encrypted data is too short to contain IV.");
        }
        byte[] iv = new byte[16];
        System.arraycopy(ivAndEncryptedData, 0, iv, 0, 16);
        int encryptedDataLength = ivAndEncryptedData.length - 16;
        byte[] encryptedData = new byte[encryptedDataLength];
        System.arraycopy(ivAndEncryptedData, 16, encryptedData, 0, encryptedDataLength);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(2, (Key)key, ivSpec);
        return cipher.doFinal(encryptedData);
    }

    private static byte[] generateIV() {
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }

    public static SecretKey getKeyFromBase64(String base64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(decodedKey, ALGORITHM);
    }

    public static String getBase64Key(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}

