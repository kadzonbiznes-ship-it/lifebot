/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import com.sun.crypto.provider.CipherBlockChaining;
import com.sun.crypto.provider.CipherFeedback;
import com.sun.crypto.provider.CipherTextStealing;
import com.sun.crypto.provider.ConstructKeys;
import com.sun.crypto.provider.CounterMode;
import com.sun.crypto.provider.ElectronicCodeBook;
import com.sun.crypto.provider.FeedbackCipher;
import com.sun.crypto.provider.ISO10126Padding;
import com.sun.crypto.provider.OutputFeedback;
import com.sun.crypto.provider.PCBC;
import com.sun.crypto.provider.PKCS5Padding;
import com.sun.crypto.provider.Padding;
import com.sun.crypto.provider.RC2Crypt;
import com.sun.crypto.provider.SunJCE;
import com.sun.crypto.provider.SymmetricCipher;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import java.util.Locale;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.RC2ParameterSpec;

final class CipherCore {
    private byte[] buffer = null;
    private int blockSize = 0;
    private int unitBytes = 0;
    private int buffered = 0;
    private int minBytes = 0;
    private int diffBlocksize = 0;
    private Padding padding = null;
    private FeedbackCipher cipher = null;
    private int cipherMode = 0;
    private boolean decrypting = false;
    private static final int ECB_MODE = 0;
    private static final int CBC_MODE = 1;
    private static final int CFB_MODE = 2;
    private static final int OFB_MODE = 3;
    private static final int PCBC_MODE = 4;
    private static final int CTR_MODE = 5;
    private static final int CTS_MODE = 6;

    CipherCore(SymmetricCipher impl, int blkSize) {
        this.blockSize = blkSize;
        this.unitBytes = blkSize;
        this.diffBlocksize = blkSize;
        this.buffer = new byte[this.blockSize * 2];
        this.cipher = new ElectronicCodeBook(impl);
        this.padding = new PKCS5Padding(this.blockSize);
    }

    void setMode(String mode) throws NoSuchAlgorithmException {
        if (mode == null) {
            throw new NoSuchAlgorithmException("null mode");
        }
        String modeUpperCase = mode.toUpperCase(Locale.ENGLISH);
        if (modeUpperCase.equals("ECB")) {
            return;
        }
        SymmetricCipher rawImpl = this.cipher.getEmbeddedCipher();
        if (modeUpperCase.equals("CBC")) {
            this.cipherMode = 1;
            this.cipher = new CipherBlockChaining(rawImpl);
        } else if (modeUpperCase.equals("CTS")) {
            this.cipherMode = 6;
            this.cipher = new CipherTextStealing(rawImpl);
            this.minBytes = this.blockSize + 1;
            this.padding = null;
        } else if (modeUpperCase.equals("CTR")) {
            this.cipherMode = 5;
            this.cipher = new CounterMode(rawImpl);
            this.unitBytes = 1;
            this.padding = null;
        } else if (modeUpperCase.startsWith("CFB")) {
            this.cipherMode = 2;
            this.unitBytes = CipherCore.getNumOfUnit(mode, "CFB".length(), this.blockSize);
            this.cipher = new CipherFeedback(rawImpl, this.unitBytes);
        } else if (modeUpperCase.startsWith("OFB")) {
            this.cipherMode = 3;
            this.unitBytes = CipherCore.getNumOfUnit(mode, "OFB".length(), this.blockSize);
            this.cipher = new OutputFeedback(rawImpl, this.unitBytes);
        } else if (modeUpperCase.equals("PCBC")) {
            this.cipherMode = 4;
            this.cipher = new PCBC(rawImpl);
        } else {
            throw new NoSuchAlgorithmException("Cipher mode: " + mode + " not found");
        }
    }

    private static int getNumOfUnit(String mode, int offset, int blockSize) throws NoSuchAlgorithmException {
        int result = blockSize;
        if (mode.length() > offset) {
            int numInt;
            try {
                numInt = Integer.parseInt(mode.substring(offset));
                result = numInt >> 3;
            }
            catch (NumberFormatException e) {
                throw new NoSuchAlgorithmException("Algorithm mode: " + mode + " not implemented");
            }
            if (numInt % 8 != 0 || result > blockSize) {
                throw new NoSuchAlgorithmException("Invalid algorithm mode: " + mode);
            }
        }
        return result;
    }

    void setPadding(String paddingScheme) throws NoSuchPaddingException {
        if (paddingScheme == null) {
            throw new NoSuchPaddingException("null padding");
        }
        if (paddingScheme.equalsIgnoreCase("NoPadding")) {
            this.padding = null;
        } else if (paddingScheme.equalsIgnoreCase("ISO10126Padding")) {
            this.padding = new ISO10126Padding(this.blockSize);
        } else if (paddingScheme.equalsIgnoreCase("PKCS5Padding")) {
            this.padding = new PKCS5Padding(this.blockSize);
        } else {
            throw new NoSuchPaddingException("Padding: " + paddingScheme + " not implemented");
        }
        if (this.padding != null && (this.cipherMode == 5 || this.cipherMode == 6)) {
            this.padding = null;
            String modeStr = null;
            switch (this.cipherMode) {
                case 5: {
                    modeStr = "CTR";
                    break;
                }
                case 6: {
                    modeStr = "CTS";
                    break;
                }
            }
            if (modeStr != null) {
                throw new NoSuchPaddingException(modeStr + " mode must be used with NoPadding");
            }
        }
    }

    int getOutputSize(int inputLen) {
        return this.getOutputSizeByOperation(inputLen, true);
    }

    private int getOutputSizeByOperation(int inputLen, boolean isDoFinal) {
        int totalLen = this.buffered;
        totalLen = Math.addExact(totalLen, inputLen);
        if (this.padding != null && !this.decrypting) {
            if (this.unitBytes != this.blockSize) {
                if (totalLen < this.diffBlocksize) {
                    totalLen = this.diffBlocksize;
                } else {
                    int residue = (totalLen - this.diffBlocksize) % this.blockSize;
                    totalLen = Math.addExact(totalLen, this.blockSize - residue);
                }
            } else {
                totalLen = Math.addExact(totalLen, this.padding.padLength(totalLen));
            }
        }
        return totalLen;
    }

    byte[] getIV() {
        byte[] iv = this.cipher.getIV();
        return iv == null ? null : (byte[])iv.clone();
    }

    AlgorithmParameters getParameters(String algName) {
        AlgorithmParameterSpec spec;
        if (this.cipherMode == 0) {
            return null;
        }
        AlgorithmParameters params = null;
        byte[] iv = this.getIV();
        if (iv == null) {
            iv = new byte[this.blockSize];
            SunJCE.getRandom().nextBytes(iv);
        }
        if (algName.equals("RC2")) {
            RC2Crypt rawImpl = (RC2Crypt)this.cipher.getEmbeddedCipher();
            spec = new RC2ParameterSpec(rawImpl.getEffectiveKeyBits(), iv);
        } else {
            spec = new IvParameterSpec(iv);
        }
        try {
            params = AlgorithmParameters.getInstance(algName, SunJCE.getInstance());
            params.init(spec);
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException("Cannot find " + algName + " AlgorithmParameters implementation in SunJCE provider");
        }
        catch (InvalidParameterSpecException ipse) {
            throw new RuntimeException(spec.getClass() + " not supported");
        }
        return params;
    }

    void init(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        try {
            this.init(opmode, key, (AlgorithmParameterSpec)null, random);
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException(e.getMessage());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void init(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.decrypting = opmode == 2 || opmode == 4;
        byte[] keyBytes = CipherCore.getKeyBytes(key);
        byte[] ivBytes = null;
        try {
            if (params != null) {
                if (params instanceof IvParameterSpec) {
                    ivBytes = ((IvParameterSpec)params).getIV();
                    if (ivBytes == null || ivBytes.length != this.blockSize) {
                        throw new InvalidAlgorithmParameterException("Wrong IV length: must be " + this.blockSize + " bytes long");
                    }
                } else if (params instanceof RC2ParameterSpec) {
                    ivBytes = ((RC2ParameterSpec)params).getIV();
                    if (ivBytes != null && ivBytes.length != this.blockSize) {
                        throw new InvalidAlgorithmParameterException("Wrong IV length: must be " + this.blockSize + " bytes long");
                    }
                } else {
                    throw new InvalidAlgorithmParameterException("Unsupported parameter: " + params);
                }
            }
            if (this.cipherMode == 0) {
                if (ivBytes != null) {
                    throw new InvalidAlgorithmParameterException("ECB mode cannot use IV");
                }
            } else if (ivBytes == null) {
                if (this.decrypting) {
                    throw new InvalidAlgorithmParameterException("Parameters missing");
                }
                if (random == null) {
                    random = SunJCE.getRandom();
                }
                ivBytes = new byte[this.blockSize];
                random.nextBytes(ivBytes);
            }
            this.buffered = 0;
            this.diffBlocksize = this.blockSize;
            String algorithm = key.getAlgorithm();
            this.cipher.init(this.decrypting, algorithm, keyBytes, ivBytes);
        }
        finally {
            Arrays.fill(keyBytes, (byte)0);
        }
    }

    void init(int opmode, Key key, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        IvParameterSpec spec = null;
        String paramType = null;
        if (params != null) {
            try {
                paramType = "IV";
                spec = params.getParameterSpec(IvParameterSpec.class);
            }
            catch (InvalidParameterSpecException ipse) {
                throw new InvalidAlgorithmParameterException("Wrong parameter type: " + paramType + " expected");
            }
        }
        this.init(opmode, key, spec, random);
    }

    static byte[] getKeyBytes(Key key) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("No key given");
        }
        if (!"RAW".equalsIgnoreCase(key.getFormat())) {
            throw new InvalidKeyException("Wrong format: RAW bytes needed");
        }
        byte[] keyBytes = key.getEncoded();
        if (keyBytes == null) {
            throw new InvalidKeyException("RAW key bytes missing");
        }
        return keyBytes;
    }

    byte[] update(byte[] input, int inputOffset, int inputLen) {
        byte[] output = null;
        try {
            output = new byte[this.getOutputSizeByOperation(inputLen, false)];
            int len = this.update(input, inputOffset, inputLen, output, 0);
            if (len == output.length) {
                return output;
            }
            byte[] copy = Arrays.copyOf(output, len);
            if (this.decrypting) {
                Arrays.fill(output, (byte)0);
            }
            return copy;
        }
        catch (ShortBufferException e) {
            throw new ProviderException("Unexpected exception", e);
        }
    }

    int update(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException {
        int len = Math.addExact(this.buffered, inputLen);
        len -= this.minBytes;
        if (this.padding != null && this.decrypting) {
            len -= this.blockSize;
        }
        int n = len = len > 0 ? len - len % this.unitBytes : 0;
        if (output == null || output.length - outputOffset < len) {
            throw new ShortBufferException("Output buffer must be (at least) " + len + " bytes long");
        }
        int outLen = 0;
        if (len != 0) {
            if (input == output && outputOffset - inputOffset < inputLen && inputOffset - outputOffset < this.buffer.length) {
                input = Arrays.copyOfRange(input, inputOffset, Math.addExact(inputOffset, inputLen));
                inputOffset = 0;
            }
            if (len <= this.buffered) {
                outLen = this.decrypting ? this.cipher.decrypt(this.buffer, 0, len, output, outputOffset) : this.cipher.encrypt(this.buffer, 0, len, output, outputOffset);
                this.buffered -= len;
                if (this.buffered != 0) {
                    System.arraycopy(this.buffer, len, this.buffer, 0, this.buffered);
                }
            } else {
                int inputConsumed = len - this.buffered;
                if (this.buffered > 0) {
                    int bufferCapacity = this.buffer.length - this.buffered;
                    if (bufferCapacity != 0) {
                        int temp = Math.min(bufferCapacity, inputConsumed);
                        if (this.unitBytes != this.blockSize) {
                            temp -= Math.addExact(this.buffered, temp) % this.unitBytes;
                        }
                        System.arraycopy(input, inputOffset, this.buffer, this.buffered, temp);
                        inputOffset = Math.addExact(inputOffset, temp);
                        inputConsumed -= temp;
                        inputLen -= temp;
                        this.buffered = Math.addExact(this.buffered, temp);
                    }
                    if (this.decrypting) {
                        outLen = this.cipher.decrypt(this.buffer, 0, this.buffered, output, outputOffset);
                    } else {
                        outLen = this.cipher.encrypt(this.buffer, 0, this.buffered, output, outputOffset);
                        Arrays.fill(this.buffer, (byte)0);
                    }
                    outputOffset = Math.addExact(outputOffset, outLen);
                    this.buffered = 0;
                }
                if (inputConsumed > 0) {
                    outLen = this.decrypting ? (outLen += this.cipher.decrypt(input, inputOffset, inputConsumed, output, outputOffset)) : (outLen += this.cipher.encrypt(input, inputOffset, inputConsumed, output, outputOffset));
                    inputOffset += inputConsumed;
                    inputLen -= inputConsumed;
                }
            }
            if (this.unitBytes != this.blockSize) {
                this.diffBlocksize = len < this.diffBlocksize ? (this.diffBlocksize -= len) : this.blockSize - (len - this.diffBlocksize) % this.blockSize;
            }
        }
        if (inputLen > 0) {
            System.arraycopy(input, inputOffset, this.buffer, this.buffered, inputLen);
            this.buffered = Math.addExact(this.buffered, inputLen);
        }
        return outLen;
    }

    byte[] doFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException, BadPaddingException {
        try {
            byte[] output = new byte[this.getOutputSizeByOperation(inputLen, true)];
            byte[] finalBuf = this.prepareInputBuffer(input, inputOffset, inputLen, output, 0);
            int finalOffset = finalBuf == input ? inputOffset : 0;
            int finalBufLen = finalBuf == input ? inputLen : finalBuf.length;
            int outLen = this.fillOutputBuffer(finalBuf, finalOffset, output, 0, finalBufLen, input);
            this.endDoFinal();
            if (outLen < output.length) {
                byte[] copy = Arrays.copyOf(output, outLen);
                if (this.decrypting) {
                    Arrays.fill(output, (byte)0);
                }
                return copy;
            }
            return output;
        }
        catch (ShortBufferException e) {
            throw new ProviderException("Unexpected exception", e);
        }
    }

    int doFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws IllegalBlockSizeException, ShortBufferException, BadPaddingException {
        int finalBufLen;
        int estOutSize = this.getOutputSizeByOperation(inputLen, true);
        int outputCapacity = this.checkOutputCapacity(output, outputOffset, estOutSize);
        int offset = outputOffset;
        byte[] finalBuf = this.prepareInputBuffer(input, inputOffset, inputLen, output, outputOffset);
        byte[] internalOutput = null;
        int finalOffset = finalBuf == input ? inputOffset : 0;
        int n = finalBufLen = finalBuf == input ? inputLen : finalBuf.length;
        if (this.decrypting) {
            if (outputCapacity < estOutSize) {
                this.cipher.save();
            }
            if (outputCapacity < estOutSize || this.padding != null) {
                internalOutput = new byte[estOutSize];
                offset = 0;
            }
        }
        byte[] outBuffer = internalOutput != null ? internalOutput : output;
        int outLen = this.fillOutputBuffer(finalBuf, finalOffset, outBuffer, offset, finalBufLen, input);
        if (this.decrypting) {
            if (outputCapacity < outLen) {
                this.cipher.restore();
                throw new ShortBufferException("Output buffer too short: " + outputCapacity + " bytes given, " + outLen + " bytes needed");
            }
            if (internalOutput != null) {
                System.arraycopy(internalOutput, 0, output, outputOffset, outLen);
                Arrays.fill(internalOutput, (byte)0);
            }
        }
        this.endDoFinal();
        return outLen;
    }

    private void endDoFinal() {
        this.buffered = 0;
        this.diffBlocksize = this.blockSize;
        if (this.cipherMode != 0) {
            this.cipher.reset();
        }
    }

    private int unpad(int outLen, int off, byte[] outWithPadding) throws BadPaddingException {
        int padStart = this.padding.unpad(outWithPadding, off, outLen);
        if (padStart < 0) {
            throw new BadPaddingException("Given final block not properly padded. Such issues can arise if a bad key is used during decryption.");
        }
        return padStart - off;
    }

    private byte[] prepareInputBuffer(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws IllegalBlockSizeException, ShortBufferException {
        int len;
        int totalLen = len = Math.addExact(this.buffered, inputLen);
        int paddingLen = 0;
        if (this.unitBytes != this.blockSize) {
            paddingLen = totalLen < this.diffBlocksize ? this.diffBlocksize - totalLen : this.blockSize - (totalLen - this.diffBlocksize) % this.blockSize;
        } else if (this.padding != null) {
            paddingLen = this.padding.padLength(totalLen);
        }
        if (this.decrypting && this.padding != null && paddingLen > 0 && paddingLen != this.blockSize) {
            throw new IllegalBlockSizeException("Input length must be multiple of " + this.blockSize + " when decrypting with padded cipher");
        }
        if (this.buffered != 0 || !this.decrypting && this.padding != null || input == output && outputOffset - inputOffset < inputLen && inputOffset - outputOffset < this.buffer.length) {
            if (this.decrypting || this.padding == null) {
                paddingLen = 0;
            }
            byte[] finalBuf = new byte[Math.addExact(len, paddingLen)];
            if (this.buffered != 0) {
                System.arraycopy(this.buffer, 0, finalBuf, 0, this.buffered);
                if (!this.decrypting) {
                    Arrays.fill(this.buffer, (byte)0);
                }
            }
            if (inputLen != 0) {
                System.arraycopy(input, inputOffset, finalBuf, this.buffered, inputLen);
            }
            if (paddingLen != 0) {
                this.padding.padWithLen(finalBuf, Math.addExact(this.buffered, inputLen), paddingLen);
            }
            return finalBuf;
        }
        return input;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int fillOutputBuffer(byte[] finalBuf, int finalOffset, byte[] output, int outOfs, int finalBufLen, byte[] input) throws ShortBufferException, BadPaddingException, IllegalBlockSizeException {
        try {
            int len = this.finalNoPadding(finalBuf, finalOffset, output, outOfs, finalBufLen);
            if (this.decrypting && this.padding != null) {
                len = this.unpad(len, outOfs, output);
            }
            int n = len;
            return n;
        }
        finally {
            if (!this.decrypting && finalBuf != input) {
                Arrays.fill(finalBuf, (byte)0);
            }
        }
    }

    private int checkOutputCapacity(byte[] output, int outputOffset, int estOutSize) throws ShortBufferException {
        int minOutSize;
        int outputCapacity = output.length - outputOffset;
        int n = minOutSize = this.decrypting ? estOutSize - this.blockSize : estOutSize;
        if (output == null || outputCapacity < minOutSize) {
            throw new ShortBufferException("Output buffer must be (at least) " + minOutSize + " bytes long");
        }
        return outputCapacity;
    }

    private int finalNoPadding(byte[] in, int inOfs, byte[] out, int outOfs, int len) throws IllegalBlockSizeException, ShortBufferException {
        if (in == null || len == 0) {
            return 0;
        }
        if (this.cipherMode != 2 && this.cipherMode != 3 && len % this.unitBytes != 0 && this.cipherMode != 6) {
            if (this.padding != null) {
                throw new IllegalBlockSizeException("Input length (with padding) not multiple of " + this.unitBytes + " bytes");
            }
            throw new IllegalBlockSizeException("Input length not multiple of " + this.unitBytes + " bytes");
        }
        int outLen = this.decrypting ? this.cipher.decryptFinal(in, inOfs, len, out, outOfs) : this.cipher.encryptFinal(in, inOfs, len, out, outOfs);
        return outLen;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    byte[] wrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
        byte[] result = null;
        try {
            byte[] encodedKey = key.getEncoded();
            if (encodedKey == null || encodedKey.length == 0) {
                throw new InvalidKeyException("Cannot get an encoding of the key to be wrapped");
            }
            try {
                result = this.doFinal(encodedKey, 0, encodedKey.length);
            }
            finally {
                Arrays.fill(encodedKey, (byte)0);
            }
        }
        catch (BadPaddingException badPaddingException) {
            // empty catch block
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    Key unwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] encodedKey;
        try {
            encodedKey = this.doFinal(wrappedKey, 0, wrappedKey.length);
        }
        catch (BadPaddingException ePadding) {
            throw new InvalidKeyException("The wrapped key is not padded correctly");
        }
        catch (IllegalBlockSizeException eBlockSize) {
            throw new InvalidKeyException("The wrapped key does not have the correct length");
        }
        try {
            Key key = ConstructKeys.constructKey(encodedKey, wrappedKeyAlgorithm, wrappedKeyType);
            return key;
        }
        finally {
            Arrays.fill(encodedKey, (byte)0);
        }
    }
}

