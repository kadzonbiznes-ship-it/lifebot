/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import com.sun.crypto.provider.FeedbackCipher;
import com.sun.crypto.provider.SymmetricCipher;
import java.security.InvalidKeyException;
import jdk.internal.vm.annotation.IntrinsicCandidate;
import sun.security.util.ArrayUtil;

class CounterMode
extends FeedbackCipher {
    final byte[] counter = new byte[this.blockSize];
    private final byte[] encryptedCounter = new byte[this.blockSize];
    private int used;
    private byte[] counterSave = null;
    private byte[] encryptedCounterSave = null;
    private int usedSave = 0;

    CounterMode(SymmetricCipher embeddedCipher) {
        super(embeddedCipher);
    }

    @Override
    String getFeedback() {
        return "CTR";
    }

    @Override
    void reset() {
        System.arraycopy(this.iv, 0, this.counter, 0, this.blockSize);
        this.used = this.blockSize;
    }

    @Override
    void save() {
        if (this.counterSave == null) {
            this.counterSave = new byte[this.blockSize];
            this.encryptedCounterSave = new byte[this.blockSize];
        }
        System.arraycopy(this.counter, 0, this.counterSave, 0, this.blockSize);
        System.arraycopy(this.encryptedCounter, 0, this.encryptedCounterSave, 0, this.blockSize);
        this.usedSave = this.used;
    }

    @Override
    void restore() {
        System.arraycopy(this.counterSave, 0, this.counter, 0, this.blockSize);
        System.arraycopy(this.encryptedCounterSave, 0, this.encryptedCounter, 0, this.blockSize);
        this.used = this.usedSave;
    }

    @Override
    void init(boolean decrypting, String algorithm, byte[] key, byte[] iv) throws InvalidKeyException {
        if (key == null || iv == null || iv.length != this.blockSize) {
            throw new InvalidKeyException("Internal error");
        }
        this.iv = iv;
        this.reset();
        this.embeddedCipher.init(false, algorithm, key);
    }

    @Override
    int encrypt(byte[] in, int inOff, int len, byte[] out, int outOff) {
        return this.crypt(in, inOff, len, out, outOff);
    }

    @Override
    int decrypt(byte[] in, int inOff, int len, byte[] out, int outOff) {
        return this.crypt(in, inOff, len, out, outOff);
    }

    private static void increment(byte[] b) {
        int n = b.length - 1;
        while (n >= 0) {
            int n2 = n--;
            b[n2] = (byte)(b[n2] + 1);
            if (b[n2] == 0) continue;
        }
    }

    private int crypt(byte[] in, int inOff, int len, byte[] out, int outOff) {
        if (len == 0) {
            return 0;
        }
        ArrayUtil.nullAndBoundsCheck(in, inOff, len);
        ArrayUtil.nullAndBoundsCheck(out, outOff, len);
        return this.implCrypt(in, inOff, len, out, outOff);
    }

    @IntrinsicCandidate
    private int implCrypt(byte[] in, int inOff, int len, byte[] out, int outOff) {
        int result = len;
        while (len-- > 0) {
            if (this.used >= this.blockSize) {
                this.embeddedCipher.encryptBlock(this.counter, 0, this.encryptedCounter, 0);
                CounterMode.increment(this.counter);
                this.used = 0;
            }
            out[outOff++] = (byte)(in[inOff++] ^ this.encryptedCounter[this.used++]);
        }
        return result;
    }
}

