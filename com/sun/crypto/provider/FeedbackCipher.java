/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import com.sun.crypto.provider.SymmetricCipher;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

abstract class FeedbackCipher {
    final SymmetricCipher embeddedCipher;
    final int blockSize;
    byte[] iv;

    FeedbackCipher(SymmetricCipher embeddedCipher) {
        this.embeddedCipher = embeddedCipher;
        this.blockSize = embeddedCipher.getBlockSize();
    }

    final SymmetricCipher getEmbeddedCipher() {
        return this.embeddedCipher;
    }

    final int getBlockSize() {
        return this.blockSize;
    }

    abstract String getFeedback();

    abstract void save();

    abstract void restore();

    abstract void init(boolean var1, String var2, byte[] var3, byte[] var4) throws InvalidKeyException, InvalidAlgorithmParameterException;

    final byte[] getIV() {
        return this.iv;
    }

    abstract void reset();

    abstract int encrypt(byte[] var1, int var2, int var3, byte[] var4, int var5);

    int encryptFinal(byte[] plain, int plainOffset, int plainLen, byte[] cipher, int cipherOffset) throws IllegalBlockSizeException, ShortBufferException {
        return this.encrypt(plain, plainOffset, plainLen, cipher, cipherOffset);
    }

    abstract int decrypt(byte[] var1, int var2, int var3, byte[] var4, int var5);

    int decryptFinal(byte[] cipher, int cipherOffset, int cipherLen, byte[] plain, int plainOffset) throws IllegalBlockSizeException, ShortBufferException {
        return this.decrypt(cipher, cipherOffset, cipherLen, plain, plainOffset);
    }
}

