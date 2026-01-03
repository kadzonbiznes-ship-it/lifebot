/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import com.sun.crypto.provider.AESCrypt;
import com.sun.crypto.provider.CipherCore;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

class AESCipher
extends CipherSpi {
    private final CipherCore core = new CipherCore(new AESCrypt(), 16);
    private final int fixedKeySize;

    static void checkKeySize(Key key, int fixedKeySize) throws InvalidKeyException {
        if (fixedKeySize != -1) {
            if (key == null) {
                throw new InvalidKeyException("The key must not be null");
            }
            byte[] value = key.getEncoded();
            if (value == null) {
                throw new InvalidKeyException("Key encoding must not be null");
            }
            Arrays.fill(value, (byte)0);
            if (value.length != fixedKeySize) {
                throw new InvalidKeyException("The key must be " + fixedKeySize + " bytes");
            }
        }
    }

    protected AESCipher(int keySize) {
        this.fixedKeySize = keySize;
    }

    @Override
    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
        this.core.setMode(mode);
    }

    @Override
    protected void engineSetPadding(String paddingScheme) throws NoSuchPaddingException {
        this.core.setPadding(paddingScheme);
    }

    @Override
    protected int engineGetBlockSize() {
        return 16;
    }

    @Override
    protected int engineGetOutputSize(int inputLen) {
        return this.core.getOutputSize(inputLen);
    }

    @Override
    protected byte[] engineGetIV() {
        return this.core.getIV();
    }

    @Override
    protected AlgorithmParameters engineGetParameters() {
        return this.core.getParameters("AES");
    }

    @Override
    protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        AESCipher.checkKeySize(key, this.fixedKeySize);
        this.core.init(opmode, key, random);
    }

    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        AESCipher.checkKeySize(key, this.fixedKeySize);
        this.core.init(opmode, key, params, random);
    }

    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        AESCipher.checkKeySize(key, this.fixedKeySize);
        this.core.init(opmode, key, params, random);
    }

    @Override
    protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        return this.core.update(input, inputOffset, inputLen);
    }

    @Override
    protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException {
        return this.core.update(input, inputOffset, inputLen, output, outputOffset);
    }

    @Override
    protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException, BadPaddingException {
        return this.core.doFinal(input, inputOffset, inputLen);
    }

    @Override
    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws IllegalBlockSizeException, ShortBufferException, BadPaddingException {
        return this.core.doFinal(input, inputOffset, inputLen, output, outputOffset);
    }

    @Override
    protected int engineGetKeySize(Key key) throws InvalidKeyException {
        byte[] encoded = key.getEncoded();
        Arrays.fill(encoded, (byte)0);
        if (!AESCrypt.isKeySizeValid(encoded.length)) {
            throw new InvalidKeyException("Invalid AES key length: " + encoded.length + " bytes");
        }
        return Math.multiplyExact(encoded.length, 8);
    }

    @Override
    protected byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
        return this.core.wrap(key);
    }

    @Override
    protected Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType) throws InvalidKeyException, NoSuchAlgorithmException {
        return this.core.unwrap(wrappedKey, wrappedKeyAlgorithm, wrappedKeyType);
    }

    public static final class AES256_CFB_NoPadding
    extends OidImpl {
        public AES256_CFB_NoPadding() {
            super(32, "CFB", "NOPADDING");
        }
    }

    public static final class AES192_CFB_NoPadding
    extends OidImpl {
        public AES192_CFB_NoPadding() {
            super(24, "CFB", "NOPADDING");
        }
    }

    public static final class AES128_CFB_NoPadding
    extends OidImpl {
        public AES128_CFB_NoPadding() {
            super(16, "CFB", "NOPADDING");
        }
    }

    public static final class AES256_OFB_NoPadding
    extends OidImpl {
        public AES256_OFB_NoPadding() {
            super(32, "OFB", "NOPADDING");
        }
    }

    public static final class AES192_OFB_NoPadding
    extends OidImpl {
        public AES192_OFB_NoPadding() {
            super(24, "OFB", "NOPADDING");
        }
    }

    public static final class AES128_OFB_NoPadding
    extends OidImpl {
        public AES128_OFB_NoPadding() {
            super(16, "OFB", "NOPADDING");
        }
    }

    public static final class AES256_CBC_NoPadding
    extends OidImpl {
        public AES256_CBC_NoPadding() {
            super(32, "CBC", "NOPADDING");
        }
    }

    public static final class AES192_CBC_NoPadding
    extends OidImpl {
        public AES192_CBC_NoPadding() {
            super(24, "CBC", "NOPADDING");
        }
    }

    public static final class AES128_CBC_NoPadding
    extends OidImpl {
        public AES128_CBC_NoPadding() {
            super(16, "CBC", "NOPADDING");
        }
    }

    public static final class AES256_ECB_NoPadding
    extends OidImpl {
        public AES256_ECB_NoPadding() {
            super(32, "ECB", "NOPADDING");
        }
    }

    public static final class AES192_ECB_NoPadding
    extends OidImpl {
        public AES192_ECB_NoPadding() {
            super(24, "ECB", "NOPADDING");
        }
    }

    public static final class AES128_ECB_NoPadding
    extends OidImpl {
        public AES128_ECB_NoPadding() {
            super(16, "ECB", "NOPADDING");
        }
    }

    static class OidImpl
    extends AESCipher {
        protected OidImpl(int keySize, String mode, String padding) {
            super(keySize);
            try {
                this.engineSetMode(mode);
                this.engineSetPadding(padding);
            }
            catch (GeneralSecurityException gse) {
                throw new ProviderException("Internal Error", gse);
            }
        }
    }

    public static final class General
    extends AESCipher {
        public General() {
            super(-1);
        }
    }
}

