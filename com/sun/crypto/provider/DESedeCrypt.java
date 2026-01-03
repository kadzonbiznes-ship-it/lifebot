/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import com.sun.crypto.provider.DESConstants;
import com.sun.crypto.provider.DESCrypt;
import java.security.InvalidKeyException;

final class DESedeCrypt
extends DESCrypt
implements DESConstants {
    private byte[] key1 = null;
    private byte[] key2 = null;
    private byte[] key3 = null;
    private byte[] buf1 = new byte[8];
    private byte[] buf2 = new byte[8];

    DESedeCrypt() {
    }

    @Override
    void init(boolean decrypting, String algorithm, byte[] keys) throws InvalidKeyException {
        if (!algorithm.equalsIgnoreCase("DESede") && !algorithm.equalsIgnoreCase("TripleDES")) {
            throw new InvalidKeyException("Wrong algorithm: DESede or TripleDES required");
        }
        if (keys.length != 24) {
            throw new InvalidKeyException("Wrong key size");
        }
        byte[] keybuf = new byte[8];
        this.key1 = new byte[128];
        System.arraycopy(keys, 0, keybuf, 0, 8);
        this.expandKey(keybuf);
        System.arraycopy(this.expandedKey, 0, this.key1, 0, 128);
        if (this.keyEquals(keybuf, 0, keys, 16, 8)) {
            this.key3 = this.key1;
        } else {
            this.key3 = new byte[128];
            System.arraycopy(keys, 16, keybuf, 0, 8);
            this.expandKey(keybuf);
            System.arraycopy(this.expandedKey, 0, this.key3, 0, 128);
        }
        this.key2 = new byte[128];
        System.arraycopy(keys, 8, keybuf, 0, 8);
        this.expandKey(keybuf);
        System.arraycopy(this.expandedKey, 0, this.key2, 0, 128);
    }

    @Override
    void encryptBlock(byte[] plain, int plainOffset, byte[] cipher, int cipherOffset) {
        this.expandedKey = this.key1;
        this.decrypting = false;
        this.cipherBlock(plain, plainOffset, this.buf1, 0);
        this.expandedKey = this.key2;
        this.decrypting = true;
        this.cipherBlock(this.buf1, 0, this.buf2, 0);
        this.expandedKey = this.key3;
        this.decrypting = false;
        this.cipherBlock(this.buf2, 0, cipher, cipherOffset);
    }

    @Override
    void decryptBlock(byte[] cipher, int cipherOffset, byte[] plain, int plainOffset) {
        this.expandedKey = this.key3;
        this.decrypting = true;
        this.cipherBlock(cipher, cipherOffset, this.buf1, 0);
        this.expandedKey = this.key2;
        this.decrypting = false;
        this.cipherBlock(this.buf1, 0, this.buf2, 0);
        this.expandedKey = this.key1;
        this.decrypting = true;
        this.cipherBlock(this.buf2, 0, plain, plainOffset);
    }

    private boolean keyEquals(byte[] key1, int off1, byte[] key2, int off2, int len) {
        for (int i = 0; i < len; ++i) {
            if (key1[i + off1] == key2[i + off2]) continue;
            return false;
        }
        return true;
    }
}

