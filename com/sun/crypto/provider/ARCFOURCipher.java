/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import com.sun.crypto.provider.ConstructKeys;
import com.sun.crypto.provider.PKCS12PBECipherCore;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

public sealed class ARCFOURCipher
extends CipherSpi
permits PKCS12PBECipherCore.PBEWithSHA1AndRC4 {
    private final int[] S = new int[256];
    private int is;
    private int js;
    private byte[] lastKey;

    private void init(byte[] key) {
        int i;
        for (i = 0; i < 256; ++i) {
            this.S[i] = i;
        }
        int j = 0;
        int ki = 0;
        for (i = 0; i < 256; ++i) {
            int Si = this.S[i];
            j = j + Si + key[ki] & 0xFF;
            this.S[i] = this.S[j];
            this.S[j] = Si;
            if (++ki != key.length) continue;
            ki = 0;
        }
        this.is = 0;
        this.js = 0;
    }

    private void crypt(byte[] in, int inOfs, int inLen, byte[] out, int outOfs) {
        if (this.is < 0) {
            this.init(this.lastKey);
        }
        while (inLen-- > 0) {
            int Sj;
            this.is = this.is + 1 & 0xFF;
            int Si = this.S[this.is];
            this.js = this.js + Si & 0xFF;
            this.S[this.is] = Sj = this.S[this.js];
            this.S[this.js] = Si;
            out[outOfs++] = (byte)(in[inOfs++] ^ this.S[Si + Sj & 0xFF]);
        }
    }

    @Override
    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
        if (!mode.equalsIgnoreCase("ECB")) {
            throw new NoSuchAlgorithmException("Unsupported mode " + mode);
        }
    }

    @Override
    protected void engineSetPadding(String padding) throws NoSuchPaddingException {
        if (!padding.equalsIgnoreCase("NoPadding")) {
            throw new NoSuchPaddingException("Padding must be NoPadding");
        }
    }

    @Override
    protected int engineGetBlockSize() {
        return 0;
    }

    @Override
    protected int engineGetOutputSize(int inputLen) {
        return inputLen;
    }

    @Override
    protected byte[] engineGetIV() {
        return null;
    }

    @Override
    protected AlgorithmParameters engineGetParameters() {
        return null;
    }

    @Override
    protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        this.init(opmode, key);
    }

    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException("Parameters not supported");
        }
        this.init(opmode, key);
    }

    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException("Parameters not supported");
        }
        this.init(opmode, key);
    }

    private void init(int opmode, Key key) throws InvalidKeyException {
        if (this.lastKey != null) {
            Arrays.fill(this.lastKey, (byte)0);
        }
        this.lastKey = ARCFOURCipher.getEncodedKey(key);
        this.init(this.lastKey);
    }

    private static byte[] getEncodedKey(Key key) throws InvalidKeyException {
        String keyAlg = key.getAlgorithm();
        if (!keyAlg.equals("RC4") && !keyAlg.equals("ARCFOUR")) {
            throw new InvalidKeyException("Not an ARCFOUR key: " + keyAlg);
        }
        if (!"RAW".equals(key.getFormat())) {
            throw new InvalidKeyException("Key encoding format must be RAW");
        }
        byte[] encodedKey = key.getEncoded();
        if (encodedKey.length < 5 || encodedKey.length > 128) {
            Arrays.fill(encodedKey, (byte)0);
            throw new InvalidKeyException("Key length must be between 40 and 1024 bit");
        }
        return encodedKey;
    }

    @Override
    protected byte[] engineUpdate(byte[] in, int inOfs, int inLen) {
        byte[] out = new byte[inLen];
        this.crypt(in, inOfs, inLen, out, 0);
        return out;
    }

    @Override
    protected int engineUpdate(byte[] in, int inOfs, int inLen, byte[] out, int outOfs) throws ShortBufferException {
        if (out.length - outOfs < inLen) {
            throw new ShortBufferException("Output buffer too small");
        }
        this.crypt(in, inOfs, inLen, out, outOfs);
        return inLen;
    }

    @Override
    protected byte[] engineDoFinal(byte[] in, int inOfs, int inLen) {
        byte[] out = this.engineUpdate(in, inOfs, inLen);
        this.is = -1;
        return out;
    }

    @Override
    protected int engineDoFinal(byte[] in, int inOfs, int inLen, byte[] out, int outOfs) throws ShortBufferException {
        int outLen = this.engineUpdate(in, inOfs, inLen, out, outOfs);
        this.is = -1;
        return outLen;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
        byte[] encoded = key.getEncoded();
        if (encoded == null || encoded.length == 0) {
            throw new InvalidKeyException("Could not obtain encoded key");
        }
        try {
            byte[] byArray = this.engineDoFinal(encoded, 0, encoded.length);
            return byArray;
        }
        finally {
            Arrays.fill(encoded, (byte)0);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected Key engineUnwrap(byte[] wrappedKey, String algorithm, int type) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] encoded = null;
        try {
            encoded = this.engineDoFinal(wrappedKey, 0, wrappedKey.length);
            Key key = ConstructKeys.constructKey(encoded, algorithm, type);
            return key;
        }
        finally {
            if (encoded != null) {
                Arrays.fill(encoded, (byte)0);
            }
        }
    }

    @Override
    protected int engineGetKeySize(Key key) throws InvalidKeyException {
        byte[] encodedKey = ARCFOURCipher.getEncodedKey(key);
        Arrays.fill(encodedKey, (byte)0);
        return Math.multiplyExact(encodedKey.length, 8);
    }
}

