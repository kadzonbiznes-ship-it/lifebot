/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import com.sun.crypto.provider.CipherCore;
import com.sun.crypto.provider.DESedeCrypt;
import com.sun.crypto.provider.PKCS12PBECipherCore;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

public sealed class DESedeCipher
extends CipherSpi
permits PKCS12PBECipherCore.PBEWithSHA1AndDESede {
    private CipherCore core = new CipherCore(new DESedeCrypt(), 8);

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
        return 8;
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
    protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        this.core.init(opmode, key, random);
    }

    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.core.init(opmode, key, params, random);
    }

    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
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
    protected AlgorithmParameters engineGetParameters() {
        return this.core.getParameters("DESede");
    }

    @Override
    protected int engineGetKeySize(Key key) throws InvalidKeyException {
        byte[] encoded = key.getEncoded();
        Arrays.fill(encoded, (byte)0);
        if (encoded.length != 24) {
            throw new InvalidKeyException("Invalid key length: " + encoded.length + " bytes");
        }
        return 112;
    }

    @Override
    protected byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
        return this.core.wrap(key);
    }

    @Override
    protected Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType) throws InvalidKeyException, NoSuchAlgorithmException {
        return this.core.unwrap(wrappedKey, wrappedKeyAlgorithm, wrappedKeyType);
    }
}

