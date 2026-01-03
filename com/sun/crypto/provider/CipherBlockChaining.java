/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import com.sun.crypto.provider.FeedbackCipher;
import com.sun.crypto.provider.SymmetricCipher;
import java.security.InvalidKeyException;
import jdk.internal.vm.annotation.IntrinsicCandidate;
import sun.security.util.ArrayUtil;

class CipherBlockChaining
extends FeedbackCipher {
    protected byte[] r;
    private byte[] k = new byte[this.blockSize];
    private byte[] rSave = null;

    CipherBlockChaining(SymmetricCipher embeddedCipher) {
        super(embeddedCipher);
        this.r = new byte[this.blockSize];
    }

    @Override
    String getFeedback() {
        return "CBC";
    }

    @Override
    void init(boolean decrypting, String algorithm, byte[] key, byte[] iv) throws InvalidKeyException {
        if (key == null || iv == null || iv.length != this.blockSize) {
            throw new InvalidKeyException("Internal error");
        }
        this.iv = iv;
        this.reset();
        this.embeddedCipher.init(decrypting, algorithm, key);
    }

    @Override
    void reset() {
        System.arraycopy(this.iv, 0, this.r, 0, this.blockSize);
    }

    @Override
    void save() {
        if (this.rSave == null) {
            this.rSave = new byte[this.blockSize];
        }
        System.arraycopy(this.r, 0, this.rSave, 0, this.blockSize);
    }

    @Override
    void restore() {
        System.arraycopy(this.rSave, 0, this.r, 0, this.blockSize);
    }

    @Override
    int encrypt(byte[] plain, int plainOffset, int plainLen, byte[] cipher, int cipherOffset) {
        if (plainLen <= 0) {
            return plainLen;
        }
        ArrayUtil.blockSizeCheck(plainLen, this.blockSize);
        ArrayUtil.nullAndBoundsCheck(plain, plainOffset, plainLen);
        ArrayUtil.nullAndBoundsCheck(cipher, cipherOffset, plainLen);
        return this.implEncrypt(plain, plainOffset, plainLen, cipher, cipherOffset);
    }

    @IntrinsicCandidate
    private int implEncrypt(byte[] plain, int plainOffset, int plainLen, byte[] cipher, int cipherOffset) {
        int endIndex = plainOffset + plainLen;
        while (plainOffset < endIndex) {
            for (int i = 0; i < this.blockSize; ++i) {
                this.k[i] = (byte)(plain[i + plainOffset] ^ this.r[i]);
            }
            this.embeddedCipher.encryptBlock(this.k, 0, cipher, cipherOffset);
            System.arraycopy(cipher, cipherOffset, this.r, 0, this.blockSize);
            plainOffset += this.blockSize;
            cipherOffset += this.blockSize;
        }
        return plainLen;
    }

    @Override
    int decrypt(byte[] cipher, int cipherOffset, int cipherLen, byte[] plain, int plainOffset) {
        if (cipherLen <= 0) {
            return cipherLen;
        }
        ArrayUtil.blockSizeCheck(cipherLen, this.blockSize);
        ArrayUtil.nullAndBoundsCheck(cipher, cipherOffset, cipherLen);
        ArrayUtil.nullAndBoundsCheck(plain, plainOffset, cipherLen);
        return this.implDecrypt(cipher, cipherOffset, cipherLen, plain, plainOffset);
    }

    @IntrinsicCandidate
    private int implDecrypt(byte[] cipher, int cipherOffset, int cipherLen, byte[] plain, int plainOffset) {
        int endIndex = cipherOffset + cipherLen;
        while (cipherOffset < endIndex) {
            this.embeddedCipher.decryptBlock(cipher, cipherOffset, this.k, 0);
            for (int i = 0; i < this.blockSize; ++i) {
                plain[i + plainOffset] = (byte)(this.k[i] ^ this.r[i]);
            }
            System.arraycopy(cipher, cipherOffset, this.r, 0, this.blockSize);
            cipherOffset += this.blockSize;
            plainOffset += this.blockSize;
        }
        return cipherLen;
    }
}

