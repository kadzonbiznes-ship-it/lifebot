/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import sun.security.util.SecurityConstants;
import sun.security.util.SecurityProviderConstants;

public final class SunJCE
extends Provider {
    private static final long serialVersionUID = 6812507587804302833L;
    private static final String info = "SunJCE Provider (implements RSA, DES, Triple DES, AES, Blowfish, ARCFOUR, RC2, PBE, Diffie-Hellman, HMAC, ChaCha20)";
    static final boolean debug = false;
    private static volatile SunJCE instance;

    static SecureRandom getRandom() {
        return SecureRandomHolder.RANDOM;
    }

    private void ps(String type, String algo, String cn) {
        this.putService(new Provider.Service(this, type, algo, cn, null, null));
    }

    private void ps(String type, String algo, String cn, List<String> als, HashMap<String, String> attrs) {
        this.putService(new Provider.Service(this, type, algo, cn, als, attrs));
    }

    private void psA(String type, String algo, String cn, HashMap<String, String> attrs) {
        this.putService(new Provider.Service(this, type, algo, cn, SecurityProviderConstants.getAliases(algo), attrs));
    }

    public SunJCE() {
        super("SunJCE", SecurityConstants.PROVIDER_VER, info);
        if (System.getSecurityManager() == null) {
            this.putEntries();
        } else {
            AccessController.doPrivileged(new PrivilegedAction<Void>(){

                @Override
                public Void run() {
                    SunJCE.this.putEntries();
                    return null;
                }
            });
        }
        if (instance == null) {
            instance = this;
        }
    }

    void putEntries() {
        HashMap<String, String> attrs = new HashMap<String, String>(3);
        attrs.put("SupportedModes", "ECB");
        attrs.put("SupportedPaddings", "NOPADDING|PKCS1PADDING|OAEPPADDING|OAEPWITHMD5ANDMGF1PADDING|OAEPWITHSHA1ANDMGF1PADDING|OAEPWITHSHA-1ANDMGF1PADDING|OAEPWITHSHA-224ANDMGF1PADDING|OAEPWITHSHA-256ANDMGF1PADDING|OAEPWITHSHA-384ANDMGF1PADDING|OAEPWITHSHA-512ANDMGF1PADDING|OAEPWITHSHA-512/224ANDMGF1PADDING|OAEPWITHSHA-512/256ANDMGF1PADDING");
        attrs.put("SupportedKeyClasses", "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey");
        this.ps("Cipher", "RSA", "com.sun.crypto.provider.RSACipher", null, attrs);
        String BLOCK_MODES = "ECB|CBC|PCBC|CTR|CTS|CFB|OFB|CFB8|CFB16|CFB24|CFB32|CFB40|CFB48|CFB56|CFB64|OFB8|OFB16|OFB24|OFB32|OFB40|OFB48|OFB56|OFB64";
        String BLOCK_MODES128 = "ECB|CBC|PCBC|CTR|CTS|CFB|OFB|CFB8|CFB16|CFB24|CFB32|CFB40|CFB48|CFB56|CFB64|OFB8|OFB16|OFB24|OFB32|OFB40|OFB48|OFB56|OFB64|CFB72|CFB80|CFB88|CFB96|CFB104|CFB112|CFB120|CFB128|OFB72|OFB80|OFB88|OFB96|OFB104|OFB112|OFB120|OFB128";
        String BLOCK_PADS = "NOPADDING|PKCS5PADDING|ISO10126PADDING";
        attrs.clear();
        attrs.put("SupportedModes", "ECB|CBC|PCBC|CTR|CTS|CFB|OFB|CFB8|CFB16|CFB24|CFB32|CFB40|CFB48|CFB56|CFB64|OFB8|OFB16|OFB24|OFB32|OFB40|OFB48|OFB56|OFB64");
        attrs.put("SupportedPaddings", "NOPADDING|PKCS5PADDING|ISO10126PADDING");
        attrs.put("SupportedKeyFormats", "RAW");
        this.ps("Cipher", "DES", "com.sun.crypto.provider.DESCipher", null, attrs);
        this.psA("Cipher", "DESede", "com.sun.crypto.provider.DESedeCipher", attrs);
        this.ps("Cipher", "Blowfish", "com.sun.crypto.provider.BlowfishCipher", null, attrs);
        this.ps("Cipher", "RC2", "com.sun.crypto.provider.RC2Cipher", null, attrs);
        attrs.clear();
        attrs.put("SupportedModes", "ECB|CBC|PCBC|CTR|CTS|CFB|OFB|CFB8|CFB16|CFB24|CFB32|CFB40|CFB48|CFB56|CFB64|OFB8|OFB16|OFB24|OFB32|OFB40|OFB48|OFB56|OFB64|CFB72|CFB80|CFB88|CFB96|CFB104|CFB112|CFB120|CFB128|OFB72|OFB80|OFB88|OFB96|OFB104|OFB112|OFB120|OFB128");
        attrs.put("SupportedPaddings", "NOPADDING|PKCS5PADDING|ISO10126PADDING");
        attrs.put("SupportedKeyFormats", "RAW");
        this.psA("Cipher", "AES", "com.sun.crypto.provider.AESCipher$General", attrs);
        attrs.clear();
        attrs.put("SupportedKeyFormats", "RAW");
        this.psA("Cipher", "AES/KW/NoPadding", "com.sun.crypto.provider.KeyWrapCipher$AES_KW_NoPadding", attrs);
        this.ps("Cipher", "AES/KW/PKCS5Padding", "com.sun.crypto.provider.KeyWrapCipher$AES_KW_PKCS5Padding", null, attrs);
        this.psA("Cipher", "AES/KWP/NoPadding", "com.sun.crypto.provider.KeyWrapCipher$AES_KWP_NoPadding", attrs);
        this.psA("Cipher", "AES_128/ECB/NoPadding", "com.sun.crypto.provider.AESCipher$AES128_ECB_NoPadding", attrs);
        this.psA("Cipher", "AES_128/CBC/NoPadding", "com.sun.crypto.provider.AESCipher$AES128_CBC_NoPadding", attrs);
        this.psA("Cipher", "AES_128/OFB/NoPadding", "com.sun.crypto.provider.AESCipher$AES128_OFB_NoPadding", attrs);
        this.psA("Cipher", "AES_128/CFB/NoPadding", "com.sun.crypto.provider.AESCipher$AES128_CFB_NoPadding", attrs);
        this.psA("Cipher", "AES_128/KW/NoPadding", "com.sun.crypto.provider.KeyWrapCipher$AES128_KW_NoPadding", attrs);
        this.ps("Cipher", "AES_128/KW/PKCS5Padding", "com.sun.crypto.provider.KeyWrapCipher$AES128_KW_PKCS5Padding", null, attrs);
        this.psA("Cipher", "AES_128/KWP/NoPadding", "com.sun.crypto.provider.KeyWrapCipher$AES128_KWP_NoPadding", attrs);
        this.psA("Cipher", "AES_192/ECB/NoPadding", "com.sun.crypto.provider.AESCipher$AES192_ECB_NoPadding", attrs);
        this.psA("Cipher", "AES_192/CBC/NoPadding", "com.sun.crypto.provider.AESCipher$AES192_CBC_NoPadding", attrs);
        this.psA("Cipher", "AES_192/OFB/NoPadding", "com.sun.crypto.provider.AESCipher$AES192_OFB_NoPadding", attrs);
        this.psA("Cipher", "AES_192/CFB/NoPadding", "com.sun.crypto.provider.AESCipher$AES192_CFB_NoPadding", attrs);
        this.psA("Cipher", "AES_192/KW/NoPadding", "com.sun.crypto.provider.KeyWrapCipher$AES192_KW_NoPadding", attrs);
        this.ps("Cipher", "AES_192/KW/PKCS5Padding", "com.sun.crypto.provider.KeyWrapCipher$AES192_KW_PKCS5Padding", null, attrs);
        this.psA("Cipher", "AES_192/KWP/NoPadding", "com.sun.crypto.provider.KeyWrapCipher$AES192_KWP_NoPadding", attrs);
        this.psA("Cipher", "AES_256/ECB/NoPadding", "com.sun.crypto.provider.AESCipher$AES256_ECB_NoPadding", attrs);
        this.psA("Cipher", "AES_256/CBC/NoPadding", "com.sun.crypto.provider.AESCipher$AES256_CBC_NoPadding", attrs);
        this.psA("Cipher", "AES_256/OFB/NoPadding", "com.sun.crypto.provider.AESCipher$AES256_OFB_NoPadding", attrs);
        this.psA("Cipher", "AES_256/CFB/NoPadding", "com.sun.crypto.provider.AESCipher$AES256_CFB_NoPadding", attrs);
        this.psA("Cipher", "AES_256/KW/NoPadding", "com.sun.crypto.provider.KeyWrapCipher$AES256_KW_NoPadding", attrs);
        this.ps("Cipher", "AES_256/KW/PKCS5Padding", "com.sun.crypto.provider.KeyWrapCipher$AES256_KW_PKCS5Padding", null, attrs);
        this.psA("Cipher", "AES_256/KWP/NoPadding", "com.sun.crypto.provider.KeyWrapCipher$AES256_KWP_NoPadding", attrs);
        attrs.clear();
        attrs.put("SupportedModes", "GCM");
        attrs.put("SupportedKeyFormats", "RAW");
        this.ps("Cipher", "AES/GCM/NoPadding", "com.sun.crypto.provider.GaloisCounterMode$AESGCM", null, attrs);
        this.psA("Cipher", "AES_128/GCM/NoPadding", "com.sun.crypto.provider.GaloisCounterMode$AES128", attrs);
        this.psA("Cipher", "AES_192/GCM/NoPadding", "com.sun.crypto.provider.GaloisCounterMode$AES192", attrs);
        this.psA("Cipher", "AES_256/GCM/NoPadding", "com.sun.crypto.provider.GaloisCounterMode$AES256", attrs);
        attrs.clear();
        attrs.put("SupportedModes", "CBC");
        attrs.put("SupportedPaddings", "NOPADDING");
        attrs.put("SupportedKeyFormats", "RAW");
        this.ps("Cipher", "DESedeWrap", "com.sun.crypto.provider.DESedeWrapCipher", null, attrs);
        attrs.clear();
        attrs.put("SupportedModes", "ECB");
        attrs.put("SupportedPaddings", "NOPADDING");
        attrs.put("SupportedKeyFormats", "RAW");
        this.psA("Cipher", "ARCFOUR", "com.sun.crypto.provider.ARCFOURCipher", attrs);
        attrs.clear();
        attrs.put("SupportedKeyFormats", "RAW");
        this.ps("Cipher", "ChaCha20", "com.sun.crypto.provider.ChaCha20Cipher$ChaCha20Only", null, attrs);
        this.psA("Cipher", "ChaCha20-Poly1305", "com.sun.crypto.provider.ChaCha20Cipher$ChaCha20Poly1305", attrs);
        this.psA("Cipher", "PBEWithMD5AndDES", "com.sun.crypto.provider.PBEWithMD5AndDESCipher", null);
        this.ps("Cipher", "PBEWithMD5AndTripleDES", "com.sun.crypto.provider.PBEWithMD5AndTripleDESCipher");
        this.psA("Cipher", "PBEWithSHA1AndDESede", "com.sun.crypto.provider.PKCS12PBECipherCore$PBEWithSHA1AndDESede", null);
        this.psA("Cipher", "PBEWithSHA1AndRC2_40", "com.sun.crypto.provider.PKCS12PBECipherCore$PBEWithSHA1AndRC2_40", null);
        this.psA("Cipher", "PBEWithSHA1AndRC2_128", "com.sun.crypto.provider.PKCS12PBECipherCore$PBEWithSHA1AndRC2_128", null);
        this.psA("Cipher", "PBEWithSHA1AndRC4_40", "com.sun.crypto.provider.PKCS12PBECipherCore$PBEWithSHA1AndRC4_40", null);
        this.psA("Cipher", "PBEWithSHA1AndRC4_128", "com.sun.crypto.provider.PKCS12PBECipherCore$PBEWithSHA1AndRC4_128", null);
        this.ps("Cipher", "PBEWithHmacSHA1AndAES_128", "com.sun.crypto.provider.PBES2Core$HmacSHA1AndAES_128");
        this.ps("Cipher", "PBEWithHmacSHA224AndAES_128", "com.sun.crypto.provider.PBES2Core$HmacSHA224AndAES_128");
        this.ps("Cipher", "PBEWithHmacSHA256AndAES_128", "com.sun.crypto.provider.PBES2Core$HmacSHA256AndAES_128");
        this.ps("Cipher", "PBEWithHmacSHA384AndAES_128", "com.sun.crypto.provider.PBES2Core$HmacSHA384AndAES_128");
        this.ps("Cipher", "PBEWithHmacSHA512AndAES_128", "com.sun.crypto.provider.PBES2Core$HmacSHA512AndAES_128");
        this.ps("Cipher", "PBEWithHmacSHA512/224AndAES_128", "com.sun.crypto.provider.PBES2Core$HmacSHA512_224AndAES_128");
        this.ps("Cipher", "PBEWithHmacSHA512/256AndAES_128", "com.sun.crypto.provider.PBES2Core$HmacSHA512_256AndAES_128");
        this.ps("Cipher", "PBEWithHmacSHA1AndAES_256", "com.sun.crypto.provider.PBES2Core$HmacSHA1AndAES_256");
        this.ps("Cipher", "PBEWithHmacSHA224AndAES_256", "com.sun.crypto.provider.PBES2Core$HmacSHA224AndAES_256");
        this.ps("Cipher", "PBEWithHmacSHA256AndAES_256", "com.sun.crypto.provider.PBES2Core$HmacSHA256AndAES_256");
        this.ps("Cipher", "PBEWithHmacSHA384AndAES_256", "com.sun.crypto.provider.PBES2Core$HmacSHA384AndAES_256");
        this.ps("Cipher", "PBEWithHmacSHA512AndAES_256", "com.sun.crypto.provider.PBES2Core$HmacSHA512AndAES_256");
        this.ps("Cipher", "PBEWithHmacSHA512/224AndAES_256", "com.sun.crypto.provider.PBES2Core$HmacSHA512_224AndAES_256");
        this.ps("Cipher", "PBEWithHmacSHA512/256AndAES_256", "com.sun.crypto.provider.PBES2Core$HmacSHA512_256AndAES_256");
        this.ps("KeyGenerator", "DES", "com.sun.crypto.provider.DESKeyGenerator");
        this.psA("KeyGenerator", "DESede", "com.sun.crypto.provider.DESedeKeyGenerator", null);
        this.ps("KeyGenerator", "Blowfish", "com.sun.crypto.provider.BlowfishKeyGenerator");
        this.psA("KeyGenerator", "AES", "com.sun.crypto.provider.AESKeyGenerator", null);
        this.ps("KeyGenerator", "RC2", "com.sun.crypto.provider.KeyGeneratorCore$RC2KeyGenerator");
        this.psA("KeyGenerator", "ARCFOUR", "com.sun.crypto.provider.KeyGeneratorCore$ARCFOURKeyGenerator", null);
        this.ps("KeyGenerator", "ChaCha20", "com.sun.crypto.provider.KeyGeneratorCore$ChaCha20KeyGenerator");
        this.ps("KeyGenerator", "HmacMD5", "com.sun.crypto.provider.HmacMD5KeyGenerator");
        this.psA("KeyGenerator", "HmacSHA1", "com.sun.crypto.provider.HmacSHA1KeyGenerator", null);
        this.psA("KeyGenerator", "HmacSHA224", "com.sun.crypto.provider.KeyGeneratorCore$HmacKG$SHA224", null);
        this.psA("KeyGenerator", "HmacSHA256", "com.sun.crypto.provider.KeyGeneratorCore$HmacKG$SHA256", null);
        this.psA("KeyGenerator", "HmacSHA384", "com.sun.crypto.provider.KeyGeneratorCore$HmacKG$SHA384", null);
        this.psA("KeyGenerator", "HmacSHA512", "com.sun.crypto.provider.KeyGeneratorCore$HmacKG$SHA512", null);
        this.psA("KeyGenerator", "HmacSHA512/224", "com.sun.crypto.provider.KeyGeneratorCore$HmacKG$SHA512_224", null);
        this.psA("KeyGenerator", "HmacSHA512/256", "com.sun.crypto.provider.KeyGeneratorCore$HmacKG$SHA512_256", null);
        this.psA("KeyGenerator", "HmacSHA3-224", "com.sun.crypto.provider.KeyGeneratorCore$HmacKG$SHA3_224", null);
        this.psA("KeyGenerator", "HmacSHA3-256", "com.sun.crypto.provider.KeyGeneratorCore$HmacKG$SHA3_256", null);
        this.psA("KeyGenerator", "HmacSHA3-384", "com.sun.crypto.provider.KeyGeneratorCore$HmacKG$SHA3_384", null);
        this.psA("KeyGenerator", "HmacSHA3-512", "com.sun.crypto.provider.KeyGeneratorCore$HmacKG$SHA3_512", null);
        this.psA("KeyPairGenerator", "DiffieHellman", "com.sun.crypto.provider.DHKeyPairGenerator", null);
        this.psA("AlgorithmParameterGenerator", "DiffieHellman", "com.sun.crypto.provider.DHParameterGenerator", null);
        attrs.clear();
        attrs.put("SupportedKeyClasses", "javax.crypto.interfaces.DHPublicKey|javax.crypto.interfaces.DHPrivateKey");
        this.psA("KeyAgreement", "DiffieHellman", "com.sun.crypto.provider.DHKeyAgreement", attrs);
        this.psA("AlgorithmParameters", "DiffieHellman", "com.sun.crypto.provider.DHParameters", null);
        this.ps("AlgorithmParameters", "DES", "com.sun.crypto.provider.DESParameters");
        this.psA("AlgorithmParameters", "DESede", "com.sun.crypto.provider.DESedeParameters", null);
        this.psA("AlgorithmParameters", "PBEWithMD5AndDES", "com.sun.crypto.provider.PBEParameters", null);
        this.ps("AlgorithmParameters", "PBEWithMD5AndTripleDES", "com.sun.crypto.provider.PBEParameters");
        this.psA("AlgorithmParameters", "PBEWithSHA1AndDESede", "com.sun.crypto.provider.PBEParameters", null);
        this.psA("AlgorithmParameters", "PBEWithSHA1AndRC2_40", "com.sun.crypto.provider.PBEParameters", null);
        this.psA("AlgorithmParameters", "PBEWithSHA1AndRC2_128", "com.sun.crypto.provider.PBEParameters", null);
        this.psA("AlgorithmParameters", "PBEWithSHA1AndRC4_40", "com.sun.crypto.provider.PBEParameters", null);
        this.psA("AlgorithmParameters", "PBEWithSHA1AndRC4_128", "com.sun.crypto.provider.PBEParameters", null);
        this.psA("AlgorithmParameters", "PBES2", "com.sun.crypto.provider.PBES2Parameters$General", null);
        this.ps("AlgorithmParameters", "PBEWithHmacSHA1AndAES_128", "com.sun.crypto.provider.PBES2Parameters$HmacSHA1AndAES_128");
        this.ps("AlgorithmParameters", "PBEWithHmacSHA224AndAES_128", "com.sun.crypto.provider.PBES2Parameters$HmacSHA224AndAES_128");
        this.ps("AlgorithmParameters", "PBEWithHmacSHA256AndAES_128", "com.sun.crypto.provider.PBES2Parameters$HmacSHA256AndAES_128");
        this.ps("AlgorithmParameters", "PBEWithHmacSHA384AndAES_128", "com.sun.crypto.provider.PBES2Parameters$HmacSHA384AndAES_128");
        this.ps("AlgorithmParameters", "PBEWithHmacSHA512AndAES_128", "com.sun.crypto.provider.PBES2Parameters$HmacSHA512AndAES_128");
        this.ps("AlgorithmParameters", "PBEWithHmacSHA512/224AndAES_128", "com.sun.crypto.provider.PBES2Parameters$HmacSHA512_224AndAES_128");
        this.ps("AlgorithmParameters", "PBEWithHmacSHA512/256AndAES_128", "com.sun.crypto.provider.PBES2Parameters$HmacSHA512_256AndAES_128");
        this.ps("AlgorithmParameters", "PBEWithHmacSHA1AndAES_256", "com.sun.crypto.provider.PBES2Parameters$HmacSHA1AndAES_256");
        this.ps("AlgorithmParameters", "PBEWithHmacSHA224AndAES_256", "com.sun.crypto.provider.PBES2Parameters$HmacSHA224AndAES_256");
        this.ps("AlgorithmParameters", "PBEWithHmacSHA256AndAES_256", "com.sun.crypto.provider.PBES2Parameters$HmacSHA256AndAES_256");
        this.ps("AlgorithmParameters", "PBEWithHmacSHA384AndAES_256", "com.sun.crypto.provider.PBES2Parameters$HmacSHA384AndAES_256");
        this.ps("AlgorithmParameters", "PBEWithHmacSHA512AndAES_256", "com.sun.crypto.provider.PBES2Parameters$HmacSHA512AndAES_256");
        this.ps("AlgorithmParameters", "PBEWithHmacSHA512/224AndAES_256", "com.sun.crypto.provider.PBES2Parameters$HmacSHA512_224AndAES_256");
        this.ps("AlgorithmParameters", "PBEWithHmacSHA512/256AndAES_256", "com.sun.crypto.provider.PBES2Parameters$HmacSHA512_256AndAES_256");
        this.ps("AlgorithmParameters", "Blowfish", "com.sun.crypto.provider.BlowfishParameters");
        this.psA("AlgorithmParameters", "AES", "com.sun.crypto.provider.AESParameters", null);
        this.ps("AlgorithmParameters", "GCM", "com.sun.crypto.provider.GCMParameters");
        this.ps("AlgorithmParameters", "RC2", "com.sun.crypto.provider.RC2Parameters");
        this.psA("AlgorithmParameters", "OAEP", "com.sun.crypto.provider.OAEPParameters", null);
        this.psA("AlgorithmParameters", "ChaCha20-Poly1305", "com.sun.crypto.provider.ChaCha20Poly1305Parameters", null);
        this.psA("KeyFactory", "DiffieHellman", "com.sun.crypto.provider.DHKeyFactory", null);
        this.ps("SecretKeyFactory", "DES", "com.sun.crypto.provider.DESKeyFactory");
        this.psA("SecretKeyFactory", "DESede", "com.sun.crypto.provider.DESedeKeyFactory", null);
        this.psA("SecretKeyFactory", "PBEWithMD5AndDES", "com.sun.crypto.provider.PBEKeyFactory$PBEWithMD5AndDES", null);
        this.ps("SecretKeyFactory", "PBEWithMD5AndTripleDES", "com.sun.crypto.provider.PBEKeyFactory$PBEWithMD5AndTripleDES");
        this.psA("SecretKeyFactory", "PBEWithSHA1AndDESede", "com.sun.crypto.provider.PBEKeyFactory$PBEWithSHA1AndDESede", null);
        this.psA("SecretKeyFactory", "PBEWithSHA1AndRC2_40", "com.sun.crypto.provider.PBEKeyFactory$PBEWithSHA1AndRC2_40", null);
        this.psA("SecretKeyFactory", "PBEWithSHA1AndRC2_128", "com.sun.crypto.provider.PBEKeyFactory$PBEWithSHA1AndRC2_128", null);
        this.psA("SecretKeyFactory", "PBEWithSHA1AndRC4_40", "com.sun.crypto.provider.PBEKeyFactory$PBEWithSHA1AndRC4_40", null);
        this.psA("SecretKeyFactory", "PBEWithSHA1AndRC4_128", "com.sun.crypto.provider.PBEKeyFactory$PBEWithSHA1AndRC4_128", null);
        this.ps("SecretKeyFactory", "PBEWithHmacSHA1AndAES_128", "com.sun.crypto.provider.PBEKeyFactory$PBEWithHmacSHA1AndAES_128");
        this.ps("SecretKeyFactory", "PBEWithHmacSHA224AndAES_128", "com.sun.crypto.provider.PBEKeyFactory$PBEWithHmacSHA224AndAES_128");
        this.ps("SecretKeyFactory", "PBEWithHmacSHA256AndAES_128", "com.sun.crypto.provider.PBEKeyFactory$PBEWithHmacSHA256AndAES_128");
        this.ps("SecretKeyFactory", "PBEWithHmacSHA384AndAES_128", "com.sun.crypto.provider.PBEKeyFactory$PBEWithHmacSHA384AndAES_128");
        this.ps("SecretKeyFactory", "PBEWithHmacSHA512AndAES_128", "com.sun.crypto.provider.PBEKeyFactory$PBEWithHmacSHA512AndAES_128");
        this.ps("SecretKeyFactory", "PBEWithHmacSHA512/224AndAES_128", "com.sun.crypto.provider.PBEKeyFactory$PBEWithHmacSHA512_224AndAES_128");
        this.ps("SecretKeyFactory", "PBEWithHmacSHA512/256AndAES_128", "com.sun.crypto.provider.PBEKeyFactory$PBEWithHmacSHA512_256AndAES_128");
        this.ps("SecretKeyFactory", "PBEWithHmacSHA1AndAES_256", "com.sun.crypto.provider.PBEKeyFactory$PBEWithHmacSHA1AndAES_256");
        this.ps("SecretKeyFactory", "PBEWithHmacSHA224AndAES_256", "com.sun.crypto.provider.PBEKeyFactory$PBEWithHmacSHA224AndAES_256");
        this.ps("SecretKeyFactory", "PBEWithHmacSHA256AndAES_256", "com.sun.crypto.provider.PBEKeyFactory$PBEWithHmacSHA256AndAES_256");
        this.ps("SecretKeyFactory", "PBEWithHmacSHA384AndAES_256", "com.sun.crypto.provider.PBEKeyFactory$PBEWithHmacSHA384AndAES_256");
        this.ps("SecretKeyFactory", "PBEWithHmacSHA512AndAES_256", "com.sun.crypto.provider.PBEKeyFactory$PBEWithHmacSHA512AndAES_256");
        this.ps("SecretKeyFactory", "PBEWithHmacSHA512/224AndAES_256", "com.sun.crypto.provider.PBEKeyFactory$PBEWithHmacSHA512_224AndAES_256");
        this.ps("SecretKeyFactory", "PBEWithHmacSHA512/256AndAES_256", "com.sun.crypto.provider.PBEKeyFactory$PBEWithHmacSHA512_256AndAES_256");
        this.psA("SecretKeyFactory", "PBKDF2WithHmacSHA1", "com.sun.crypto.provider.PBKDF2Core$HmacSHA1", null);
        this.ps("SecretKeyFactory", "PBKDF2WithHmacSHA224", "com.sun.crypto.provider.PBKDF2Core$HmacSHA224");
        this.ps("SecretKeyFactory", "PBKDF2WithHmacSHA256", "com.sun.crypto.provider.PBKDF2Core$HmacSHA256");
        this.ps("SecretKeyFactory", "PBKDF2WithHmacSHA384", "com.sun.crypto.provider.PBKDF2Core$HmacSHA384");
        this.ps("SecretKeyFactory", "PBKDF2WithHmacSHA512", "com.sun.crypto.provider.PBKDF2Core$HmacSHA512");
        this.ps("SecretKeyFactory", "PBKDF2WithHmacSHA512/224", "com.sun.crypto.provider.PBKDF2Core$HmacSHA512_224");
        this.ps("SecretKeyFactory", "PBKDF2WithHmacSHA512/256", "com.sun.crypto.provider.PBKDF2Core$HmacSHA512_256");
        attrs.clear();
        attrs.put("SupportedKeyFormats", "RAW");
        this.ps("Mac", "HmacMD5", "com.sun.crypto.provider.HmacMD5", null, attrs);
        this.psA("Mac", "HmacSHA1", "com.sun.crypto.provider.HmacSHA1", attrs);
        this.psA("Mac", "HmacSHA224", "com.sun.crypto.provider.HmacCore$HmacSHA224", attrs);
        this.psA("Mac", "HmacSHA256", "com.sun.crypto.provider.HmacCore$HmacSHA256", attrs);
        this.psA("Mac", "HmacSHA384", "com.sun.crypto.provider.HmacCore$HmacSHA384", attrs);
        this.psA("Mac", "HmacSHA512", "com.sun.crypto.provider.HmacCore$HmacSHA512", attrs);
        this.psA("Mac", "HmacSHA512/224", "com.sun.crypto.provider.HmacCore$HmacSHA512_224", attrs);
        this.psA("Mac", "HmacSHA512/256", "com.sun.crypto.provider.HmacCore$HmacSHA512_256", attrs);
        this.psA("Mac", "HmacSHA3-224", "com.sun.crypto.provider.HmacCore$HmacSHA3_224", attrs);
        this.psA("Mac", "HmacSHA3-256", "com.sun.crypto.provider.HmacCore$HmacSHA3_256", attrs);
        this.psA("Mac", "HmacSHA3-384", "com.sun.crypto.provider.HmacCore$HmacSHA3_384", attrs);
        this.psA("Mac", "HmacSHA3-512", "com.sun.crypto.provider.HmacCore$HmacSHA3_512", attrs);
        this.ps("Mac", "HmacPBESHA1", "com.sun.crypto.provider.HmacPKCS12PBECore$HmacPKCS12PBE_SHA1", null, attrs);
        this.ps("Mac", "HmacPBESHA224", "com.sun.crypto.provider.HmacPKCS12PBECore$HmacPKCS12PBE_SHA224", null, attrs);
        this.ps("Mac", "HmacPBESHA256", "com.sun.crypto.provider.HmacPKCS12PBECore$HmacPKCS12PBE_SHA256", null, attrs);
        this.ps("Mac", "HmacPBESHA384", "com.sun.crypto.provider.HmacPKCS12PBECore$HmacPKCS12PBE_SHA384", null, attrs);
        this.ps("Mac", "HmacPBESHA512", "com.sun.crypto.provider.HmacPKCS12PBECore$HmacPKCS12PBE_SHA512", null, attrs);
        this.ps("Mac", "HmacPBESHA512/224", "com.sun.crypto.provider.HmacPKCS12PBECore$HmacPKCS12PBE_SHA512_224", null, attrs);
        this.ps("Mac", "HmacPBESHA512/256", "com.sun.crypto.provider.HmacPKCS12PBECore$HmacPKCS12PBE_SHA512_256", null, attrs);
        this.ps("Mac", "PBEWithHmacSHA1", "com.sun.crypto.provider.PBMAC1Core$HmacSHA1", null, attrs);
        this.ps("Mac", "PBEWithHmacSHA224", "com.sun.crypto.provider.PBMAC1Core$HmacSHA224", null, attrs);
        this.ps("Mac", "PBEWithHmacSHA256", "com.sun.crypto.provider.PBMAC1Core$HmacSHA256", null, attrs);
        this.ps("Mac", "PBEWithHmacSHA384", "com.sun.crypto.provider.PBMAC1Core$HmacSHA384", null, attrs);
        this.ps("Mac", "PBEWithHmacSHA512", "com.sun.crypto.provider.PBMAC1Core$HmacSHA512", null, attrs);
        this.ps("Mac", "PBEWithHmacSHA512/224", "com.sun.crypto.provider.PBMAC1Core$HmacSHA512_224", null, attrs);
        this.ps("Mac", "PBEWithHmacSHA512/256", "com.sun.crypto.provider.PBMAC1Core$HmacSHA512_256", null, attrs);
        this.ps("Mac", "SslMacMD5", "com.sun.crypto.provider.SslMacCore$SslMacMD5", null, attrs);
        this.ps("Mac", "SslMacSHA1", "com.sun.crypto.provider.SslMacCore$SslMacSHA1", null, attrs);
        this.ps("KeyStore", "JCEKS", "com.sun.crypto.provider.JceKeyStore");
        attrs.clear();
        attrs.put("ImplementedIn", "Software");
        attrs.put("SupportedKeyClasses", "java.security.interfaces.ECKey|java.security.interfaces.XECKey");
        this.ps("KEM", "DHKEM", "com.sun.crypto.provider.DHKEM", null, attrs);
        this.ps("KeyGenerator", "SunTlsPrf", "com.sun.crypto.provider.TlsPrfGenerator$V10");
        this.ps("KeyGenerator", "SunTls12Prf", "com.sun.crypto.provider.TlsPrfGenerator$V12");
        this.ps("KeyGenerator", "SunTlsMasterSecret", "com.sun.crypto.provider.TlsMasterSecretGenerator", List.of("SunTls12MasterSecret", "SunTlsExtendedMasterSecret"), null);
        this.ps("KeyGenerator", "SunTlsKeyMaterial", "com.sun.crypto.provider.TlsKeyMaterialGenerator", List.of("SunTls12KeyMaterial"), null);
        this.ps("KeyGenerator", "SunTlsRsaPremasterSecret", "com.sun.crypto.provider.TlsRsaPremasterSecretGenerator", List.of("SunTls12RsaPremasterSecret"), null);
    }

    static SunJCE getInstance() {
        if (instance == null) {
            return new SunJCE();
        }
        return instance;
    }

    private static class SecureRandomHolder {
        static final SecureRandom RANDOM = new SecureRandom();

        private SecureRandomHolder() {
        }
    }
}

