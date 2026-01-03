/*
 * Decompiled with CFR 0.152.
 */
package javax.crypto;

import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

public abstract class CipherSpi {
    protected abstract void engineSetMode(String var1) throws NoSuchAlgorithmException;

    protected abstract void engineSetPadding(String var1) throws NoSuchPaddingException;

    protected abstract int engineGetBlockSize();

    protected abstract int engineGetOutputSize(int var1);

    protected abstract byte[] engineGetIV();

    protected abstract AlgorithmParameters engineGetParameters();

    protected abstract void engineInit(int var1, Key var2, SecureRandom var3) throws InvalidKeyException;

    protected abstract void engineInit(int var1, Key var2, AlgorithmParameterSpec var3, SecureRandom var4) throws InvalidKeyException, InvalidAlgorithmParameterException;

    protected abstract void engineInit(int var1, Key var2, AlgorithmParameters var3, SecureRandom var4) throws InvalidKeyException, InvalidAlgorithmParameterException;

    protected abstract byte[] engineUpdate(byte[] var1, int var2, int var3);

    protected abstract int engineUpdate(byte[] var1, int var2, int var3, byte[] var4, int var5) throws ShortBufferException;

    protected int engineUpdate(ByteBuffer input, ByteBuffer output) throws ShortBufferException {
        try {
            return this.bufferCrypt(input, output, true);
        }
        catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new ProviderException("Internal error in update()");
        }
    }

    protected abstract byte[] engineDoFinal(byte[] var1, int var2, int var3) throws IllegalBlockSizeException, BadPaddingException;

    protected abstract int engineDoFinal(byte[] var1, int var2, int var3, byte[] var4, int var5) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException;

    protected int engineDoFinal(ByteBuffer input, ByteBuffer output) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        return this.bufferCrypt(input, output, false);
    }

    static int getTempArraySize(int totalSize) {
        return Math.min(4096, totalSize);
    }

    private int bufferCrypt(ByteBuffer input, ByteBuffer output, boolean isUpdate) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        if (input == null || output == null) {
            throw new NullPointerException("Input and output buffers must not be null");
        }
        int inPos = input.position();
        int inLimit = input.limit();
        int inLen = inLimit - inPos;
        if (isUpdate && inLen == 0) {
            return 0;
        }
        int outLenNeeded = this.engineGetOutputSize(inLen);
        if (output.remaining() < outLenNeeded) {
            throw new ShortBufferException("Need at least " + outLenNeeded + " bytes of space in output buffer");
        }
        boolean a1 = input.hasArray();
        boolean a2 = output.hasArray();
        int total = 0;
        if (a1) {
            byte[] inArray = input.array();
            int inOfs = input.arrayOffset() + inPos;
            if (a2) {
                byte[] outArray = output.array();
                int outPos = output.position();
                int outOfs = output.arrayOffset() + outPos;
                boolean useTempOut = false;
                if (inArray == outArray && inOfs < outOfs && outOfs < inOfs + inLen) {
                    useTempOut = true;
                    outArray = new byte[outLenNeeded];
                    outOfs = 0;
                }
                total = isUpdate ? this.engineUpdate(inArray, inOfs, inLen, outArray, outOfs) : this.engineDoFinal(inArray, inOfs, inLen, outArray, outOfs);
                if (useTempOut) {
                    output.put(outArray, outOfs, total);
                } else {
                    output.position(outPos + total);
                }
            } else {
                byte[] outArray = isUpdate ? this.engineUpdate(inArray, inOfs, inLen) : this.engineDoFinal(inArray, inOfs, inLen);
                if (outArray != null && outArray.length != 0) {
                    output.put(outArray);
                    total = outArray.length;
                }
            }
            input.position(inLimit);
        } else {
            int chunk;
            byte[] tempOut = new byte[outLenNeeded];
            int outOfs = 0;
            byte[] tempIn = new byte[CipherSpi.getTempArraySize(inLen)];
            do {
                if ((chunk = Math.min(inLen, tempIn.length)) > 0) {
                    input.get(tempIn, 0, chunk);
                }
                int n = isUpdate || inLen > chunk ? this.engineUpdate(tempIn, 0, chunk, tempOut, outOfs) : this.engineDoFinal(tempIn, 0, chunk, tempOut, outOfs);
                outOfs += n;
                total += n;
            } while ((inLen -= chunk) > 0);
            if (total > 0) {
                output.put(tempOut, 0, total);
            }
        }
        return total;
    }

    protected byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
        throw new UnsupportedOperationException();
    }

    protected Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType) throws InvalidKeyException, NoSuchAlgorithmException {
        throw new UnsupportedOperationException();
    }

    protected int engineGetKeySize(Key key) throws InvalidKeyException {
        throw new UnsupportedOperationException();
    }

    protected void engineUpdateAAD(byte[] src, int offset, int len) {
        throw new UnsupportedOperationException("The underlying Cipher implementation does not support this method");
    }

    protected void engineUpdateAAD(ByteBuffer src) {
        throw new UnsupportedOperationException("The underlying Cipher implementation does not support this method");
    }
}

