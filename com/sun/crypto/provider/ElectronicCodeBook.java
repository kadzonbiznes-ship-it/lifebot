/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import com.sun.crypto.provider.FeedbackCipher;
import com.sun.crypto.provider.SymmetricCipher;
import java.security.InvalidKeyException;
import jdk.internal.vm.annotation.IntrinsicCandidate;
import sun.security.util.ArrayUtil;

final class ElectronicCodeBook
extends FeedbackCipher {
    ElectronicCodeBook(SymmetricCipher embeddedCipher) {
        super(embeddedCipher);
    }

    @Override
    String getFeedback() {
        return "ECB";
    }

    @Override
    void reset() {
    }

    @Override
    void save() {
    }

    @Override
    void restore() {
    }

    @Override
    void init(boolean decrypting, String algorithm, byte[] key, byte[] iv) throws InvalidKeyException {
        if (key == null || iv != null) {
            throw new InvalidKeyException("Internal error");
        }
        this.embeddedCipher.init(decrypting, algorithm, key);
    }

    @IntrinsicCandidate
    private int implECBEncrypt(byte[] in, int inOff, int len, byte[] out, int outOff) {
        for (int i = len; i >= this.blockSize; i -= this.blockSize) {
            this.embeddedCipher.encryptBlock(in, inOff, out, outOff);
            inOff += this.blockSize;
            outOff += this.blockSize;
        }
        return len;
    }

    @Override
    int encrypt(byte[] in, int inOff, int len, byte[] out, int outOff) {
        ArrayUtil.blockSizeCheck(len, this.blockSize);
        ArrayUtil.nullAndBoundsCheck(in, inOff, len);
        ArrayUtil.nullAndBoundsCheck(out, outOff, len);
        return this.implECBEncrypt(in, inOff, len, out, outOff);
    }

    @IntrinsicCandidate
    private int implECBDecrypt(byte[] in, int inOff, int len, byte[] out, int outOff) {
        for (int i = len; i >= this.blockSize; i -= this.blockSize) {
            this.embeddedCipher.decryptBlock(in, inOff, out, outOff);
            inOff += this.blockSize;
            outOff += this.blockSize;
        }
        return len;
    }

    @Override
    int decrypt(byte[] in, int inOff, int len, byte[] out, int outOff) {
        ArrayUtil.blockSizeCheck(len, this.blockSize);
        ArrayUtil.nullAndBoundsCheck(in, inOff, len);
        ArrayUtil.nullAndBoundsCheck(out, outOff, len);
        return this.implECBDecrypt(in, inOff, len, out, outOff);
    }
}

