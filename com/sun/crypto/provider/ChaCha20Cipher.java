/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import com.sun.crypto.provider.AEADBufferedStream;
import com.sun.crypto.provider.Poly1305;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.Objects;
import javax.crypto.AEADBadTagException;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.ChaCha20ParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import jdk.internal.vm.annotation.ForceInline;
import jdk.internal.vm.annotation.IntrinsicCandidate;
import sun.security.util.DerValue;

abstract class ChaCha20Cipher
extends CipherSpi {
    private static final int MODE_NONE = 0;
    private static final int MODE_AEAD = 1;
    private static final int STATE_CONST_0 = 1634760805;
    private static final int STATE_CONST_1 = 857760878;
    private static final int STATE_CONST_2 = 2036477234;
    private static final int STATE_CONST_3 = 1797285236;
    private static final int KS_MAX_LEN = 1024;
    private static final int KS_BLK_SIZE = 64;
    private static final int KS_SIZE_INTS = 16;
    private static final int CIPHERBUF_BASE = 1024;
    private boolean initialized;
    protected int mode;
    private int direction;
    private boolean aadDone = false;
    private byte[] keyBytes;
    private byte[] nonce;
    private static final long MAX_UINT32 = 0xFFFFFFFFL;
    private long finalCounterValue;
    private long initCounterValue;
    private long counter;
    private final int[] startState = new int[16];
    private final byte[] keyStream = new byte[1024];
    private int keyStrLimit;
    private int keyStrOffset;
    private static final int TAG_LENGTH = 16;
    private long aadLen;
    private long dataLen;
    private static final byte[] padBuf = new byte[16];
    private final byte[] lenBuf = new byte[16];
    protected String authAlgName;
    private Poly1305 authenticator;
    private ChaChaEngine engine;
    private static final VarHandle asIntLittleEndian = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN);
    private static final VarHandle asLongLittleEndian = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);
    private static final VarHandle asLongView = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.nativeOrder());

    protected ChaCha20Cipher() {
    }

    @Override
    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
        if (!mode.equalsIgnoreCase("None")) {
            throw new NoSuchAlgorithmException("Mode must be None");
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
        return this.engine.getOutputSize(inputLen, true);
    }

    @Override
    protected byte[] engineGetIV() {
        return this.nonce != null ? (byte[])this.nonce.clone() : null;
    }

    @Override
    protected AlgorithmParameters engineGetParameters() {
        AlgorithmParameters params = null;
        if (this.mode == 1) {
            byte[] nonceData = this.initialized || this.nonce != null ? this.nonce : ChaCha20Cipher.createRandomNonce(null);
            try {
                params = AlgorithmParameters.getInstance("ChaCha20-Poly1305");
                params.init(new DerValue(4, nonceData).toByteArray());
            }
            catch (IOException | NoSuchAlgorithmException exc) {
                throw new RuntimeException(exc);
            }
        }
        return params;
    }

    @Override
    protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        if (opmode == 2) {
            throw new InvalidKeyException("Default parameter generation disallowed in DECRYPT and UNWRAP modes");
        }
        byte[] newNonce = ChaCha20Cipher.createRandomNonce(random);
        this.counter = 1L;
        this.init(opmode, key, newNonce);
    }

    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params == null) {
            this.engineInit(opmode, key, random);
            return;
        }
        byte[] newNonce = null;
        switch (this.mode) {
            case 0: {
                if (!(params instanceof ChaCha20ParameterSpec)) {
                    throw new InvalidAlgorithmParameterException("ChaCha20 algorithm requires ChaCha20ParameterSpec");
                }
                ChaCha20ParameterSpec chaParams = (ChaCha20ParameterSpec)params;
                newNonce = chaParams.getNonce();
                this.counter = this.initCounterValue = (long)chaParams.getCounter() & 0xFFFFFFFFL;
                break;
            }
            case 1: {
                if (!(params instanceof IvParameterSpec)) {
                    throw new InvalidAlgorithmParameterException("ChaCha20-Poly1305 requires IvParameterSpec");
                }
                IvParameterSpec ivParams = (IvParameterSpec)params;
                newNonce = ivParams.getIV();
                if (newNonce.length == 12) break;
                throw new InvalidAlgorithmParameterException("ChaCha20-Poly1305 nonce must be 12 bytes in length");
            }
            default: {
                throw new RuntimeException("ChaCha20 in unsupported mode");
            }
        }
        this.init(opmode, key, newNonce);
    }

    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        byte[] newNonce;
        if (params == null) {
            this.engineInit(opmode, key, random);
            return;
        }
        switch (this.mode) {
            case 0: {
                throw new InvalidAlgorithmParameterException("AlgorithmParameters not supported");
            }
            case 1: {
                String paramAlg = params.getAlgorithm();
                if (!paramAlg.equalsIgnoreCase("ChaCha20-Poly1305")) {
                    throw new InvalidAlgorithmParameterException("Invalid parameter type: " + paramAlg);
                }
                try {
                    DerValue dv = new DerValue(params.getEncoded());
                    newNonce = dv.getOctetString();
                    if (newNonce.length != 12) {
                        throw new InvalidAlgorithmParameterException("ChaCha20-Poly1305 nonce must be 12 bytes in length");
                    }
                    break;
                }
                catch (IOException ioe) {
                    throw new InvalidAlgorithmParameterException(ioe);
                }
            }
            default: {
                throw new RuntimeException("Invalid mode: " + this.mode);
            }
        }
        this.init(opmode, key, newNonce);
    }

    @Override
    protected void engineUpdateAAD(byte[] src, int offset, int len) {
        if (!this.initialized) {
            throw new IllegalStateException("Attempted to update AAD on uninitialized Cipher");
        }
        if (this.aadDone) {
            throw new IllegalStateException("Attempted to update AAD on Cipher after plaintext/ciphertext update");
        }
        if (this.mode != 1) {
            throw new IllegalStateException("Cipher is running in non-AEAD mode");
        }
        try {
            this.aadLen = Math.addExact(this.aadLen, (long)len);
            this.authUpdate(src, offset, len);
        }
        catch (ArithmeticException ae) {
            throw new IllegalStateException("AAD overflow", ae);
        }
    }

    @Override
    protected void engineUpdateAAD(ByteBuffer src) {
        if (!this.initialized) {
            throw new IllegalStateException("Attempted to update AAD on uninitialized Cipher");
        }
        if (this.aadDone) {
            throw new IllegalStateException("Attempted to update AAD on Cipher after plaintext/ciphertext update");
        }
        if (this.mode != 1) {
            throw new IllegalStateException("Cipher is running in non-AEAD mode");
        }
        try {
            this.aadLen = Math.addExact(this.aadLen, (long)(src.limit() - src.position()));
            this.authenticator.engineUpdate(src);
        }
        catch (ArithmeticException ae) {
            throw new IllegalStateException("AAD overflow", ae);
        }
    }

    private static byte[] createRandomNonce(SecureRandom random) {
        byte[] newNonce = new byte[12];
        SecureRandom rand = random != null ? random : new SecureRandom();
        rand.nextBytes(newNonce);
        return newNonce;
    }

    private void init(int opmode, Key key, byte[] newNonce) throws InvalidKeyException {
        if (opmode == 3 || opmode == 4) {
            throw new UnsupportedOperationException("WRAP_MODE and UNWRAP_MODE are not currently supported");
        }
        byte[] newKeyBytes = ChaCha20Cipher.getEncodedKey(key);
        if (opmode == 1) {
            this.checkKeyAndNonce(newKeyBytes, newNonce);
        }
        if (this.keyBytes != null) {
            Arrays.fill(this.keyBytes, (byte)0);
        }
        this.keyBytes = newKeyBytes;
        this.nonce = newNonce;
        this.setInitialState();
        if (this.mode == 0) {
            this.engine = new EngineStreamOnly();
        } else if (this.mode == 1) {
            if (opmode == 1) {
                this.engine = new EngineAEADEnc();
            } else if (opmode == 2) {
                this.engine = new EngineAEADDec();
            } else {
                throw new InvalidKeyException("Not encrypt or decrypt mode");
            }
        }
        this.finalCounterValue = this.counter + 0xFFFFFFFFL;
        this.keyStrLimit = ChaCha20Cipher.chaCha20Block(this.startState, this.counter, this.keyStream);
        this.keyStrOffset = 0;
        this.counter += (long)(this.keyStrLimit / 64);
        this.direction = opmode;
        this.aadDone = false;
        this.initialized = true;
    }

    private void checkKeyAndNonce(byte[] newKeyBytes, byte[] newNonce) throws InvalidKeyException {
        if (MessageDigest.isEqual(newKeyBytes, this.keyBytes) && MessageDigest.isEqual(newNonce, this.nonce)) {
            throw new InvalidKeyException("Matching key and nonce from previous initialization");
        }
    }

    private static byte[] getEncodedKey(Key key) throws InvalidKeyException {
        if (!"RAW".equals(key.getFormat())) {
            throw new InvalidKeyException("Key encoding format must be RAW");
        }
        byte[] encodedKey = key.getEncoded();
        if (encodedKey == null || encodedKey.length != 32) {
            if (encodedKey != null) {
                Arrays.fill(encodedKey, (byte)0);
            }
            throw new InvalidKeyException("Key length must be 256 bits");
        }
        return encodedKey;
    }

    @Override
    protected byte[] engineUpdate(byte[] in, int inOfs, int inLen) {
        byte[] out = new byte[this.engine.getOutputSize(inLen, false)];
        try {
            this.engine.doUpdate(in, inOfs, inLen, out, 0);
        }
        catch (KeyException | ShortBufferException exc) {
            throw new RuntimeException(exc);
        }
        return out;
    }

    @Override
    protected int engineUpdate(byte[] in, int inOfs, int inLen, byte[] out, int outOfs) throws ShortBufferException {
        int bytesUpdated = 0;
        try {
            bytesUpdated = this.engine.doUpdate(in, inOfs, inLen, out, outOfs);
        }
        catch (KeyException ke) {
            throw new RuntimeException(ke);
        }
        return bytesUpdated;
    }

    @Override
    protected byte[] engineDoFinal(byte[] in, int inOfs, int inLen) throws AEADBadTagException {
        byte[] output = new byte[this.engine.getOutputSize(inLen, true)];
        try {
            this.engine.doFinal(in, inOfs, inLen, output, 0);
        }
        catch (KeyException | ShortBufferException exc) {
            throw new RuntimeException(exc);
        }
        finally {
            this.resetStartState();
        }
        return output;
    }

    @Override
    protected int engineDoFinal(byte[] in, int inOfs, int inLen, byte[] out, int outOfs) throws ShortBufferException, AEADBadTagException {
        int bytesUpdated = 0;
        try {
            bytesUpdated = this.engine.doFinal(in, inOfs, inLen, out, outOfs);
        }
        catch (KeyException ke) {
            throw new RuntimeException(ke);
        }
        finally {
            this.resetStartState();
        }
        return bytesUpdated;
    }

    @Override
    protected byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
        throw new UnsupportedOperationException("Wrap operations are not supported");
    }

    @Override
    protected Key engineUnwrap(byte[] wrappedKey, String algorithm, int type) throws InvalidKeyException, NoSuchAlgorithmException {
        throw new UnsupportedOperationException("Unwrap operations are not supported");
    }

    @Override
    protected int engineGetKeySize(Key key) throws InvalidKeyException {
        byte[] encodedKey = ChaCha20Cipher.getEncodedKey(key);
        Arrays.fill(encodedKey, (byte)0);
        return encodedKey.length << 3;
    }

    private void setInitialState() throws InvalidKeyException {
        int i;
        this.startState[0] = 1634760805;
        this.startState[1] = 857760878;
        this.startState[2] = 2036477234;
        this.startState[3] = 1797285236;
        for (i = 0; i < 32; i += 4) {
            this.startState[i / 4 + 4] = this.keyBytes[i] & 0xFF | this.keyBytes[i + 1] << 8 & 0xFF00 | this.keyBytes[i + 2] << 16 & 0xFF0000 | this.keyBytes[i + 3] << 24 & 0xFF000000;
        }
        this.startState[12] = 0;
        for (i = 0; i < 12; i += 4) {
            this.startState[i / 4 + 13] = this.nonce[i] & 0xFF | this.nonce[i + 1] << 8 & 0xFF00 | this.nonce[i + 2] << 16 & 0xFF0000 | this.nonce[i + 3] << 24 & 0xFF000000;
        }
    }

    @ForceInline
    private static int chaCha20Block(int[] initState, long counter, byte[] result) {
        if (initState.length != 16 || result.length != 1024) {
            throw new IllegalArgumentException("Illegal state or keystream buffer length");
        }
        initState[12] = (int)counter;
        return ChaCha20Cipher.implChaCha20Block(initState, result);
    }

    @IntrinsicCandidate
    private static int implChaCha20Block(int[] initState, byte[] result) {
        int ws00 = 1634760805;
        int ws01 = 857760878;
        int ws02 = 2036477234;
        int ws03 = 1797285236;
        int ws04 = initState[4];
        int ws05 = initState[5];
        int ws06 = initState[6];
        int ws07 = initState[7];
        int ws08 = initState[8];
        int ws09 = initState[9];
        int ws10 = initState[10];
        int ws11 = initState[11];
        int ws12 = initState[12];
        int ws13 = initState[13];
        int ws14 = initState[14];
        int ws15 = initState[15];
        for (int round = 0; round < 10; ++round) {
            ws12 = Integer.rotateLeft(ws12 ^ (ws00 += ws04), 16);
            ws04 = Integer.rotateLeft(ws04 ^ (ws08 += ws12), 12);
            ws12 = Integer.rotateLeft(ws12 ^ (ws00 += ws04), 8);
            ws04 = Integer.rotateLeft(ws04 ^ (ws08 += ws12), 7);
            ws13 = Integer.rotateLeft(ws13 ^ (ws01 += ws05), 16);
            ws05 = Integer.rotateLeft(ws05 ^ (ws09 += ws13), 12);
            ws13 = Integer.rotateLeft(ws13 ^ (ws01 += ws05), 8);
            ws05 = Integer.rotateLeft(ws05 ^ (ws09 += ws13), 7);
            ws14 = Integer.rotateLeft(ws14 ^ (ws02 += ws06), 16);
            ws06 = Integer.rotateLeft(ws06 ^ (ws10 += ws14), 12);
            ws14 = Integer.rotateLeft(ws14 ^ (ws02 += ws06), 8);
            ws06 = Integer.rotateLeft(ws06 ^ (ws10 += ws14), 7);
            ws15 = Integer.rotateLeft(ws15 ^ (ws03 += ws07), 16);
            ws07 = Integer.rotateLeft(ws07 ^ (ws11 += ws15), 12);
            ws15 = Integer.rotateLeft(ws15 ^ (ws03 += ws07), 8);
            ws07 = Integer.rotateLeft(ws07 ^ (ws11 += ws15), 7);
            ws15 = Integer.rotateLeft(ws15 ^ (ws00 += ws05), 16);
            ws05 = Integer.rotateLeft(ws05 ^ (ws10 += ws15), 12);
            ws15 = Integer.rotateLeft(ws15 ^ (ws00 += ws05), 8);
            ws05 = Integer.rotateLeft(ws05 ^ (ws10 += ws15), 7);
            ws12 = Integer.rotateLeft(ws12 ^ (ws01 += ws06), 16);
            ws06 = Integer.rotateLeft(ws06 ^ (ws11 += ws12), 12);
            ws12 = Integer.rotateLeft(ws12 ^ (ws01 += ws06), 8);
            ws06 = Integer.rotateLeft(ws06 ^ (ws11 += ws12), 7);
            ws13 = Integer.rotateLeft(ws13 ^ (ws02 += ws07), 16);
            ws07 = Integer.rotateLeft(ws07 ^ (ws08 += ws13), 12);
            ws13 = Integer.rotateLeft(ws13 ^ (ws02 += ws07), 8);
            ws07 = Integer.rotateLeft(ws07 ^ (ws08 += ws13), 7);
            ws14 = Integer.rotateLeft(ws14 ^ (ws03 += ws04), 16);
            ws04 = Integer.rotateLeft(ws04 ^ (ws09 += ws14), 12);
            ws14 = Integer.rotateLeft(ws14 ^ (ws03 += ws04), 8);
            ws04 = Integer.rotateLeft(ws04 ^ (ws09 += ws14), 7);
        }
        asIntLittleEndian.set(result, 0, ws00 + 1634760805);
        asIntLittleEndian.set(result, 4, ws01 + 857760878);
        asIntLittleEndian.set(result, 8, ws02 + 2036477234);
        asIntLittleEndian.set(result, 12, ws03 + 1797285236);
        asIntLittleEndian.set(result, 16, ws04 + initState[4]);
        asIntLittleEndian.set(result, 20, ws05 + initState[5]);
        asIntLittleEndian.set(result, 24, ws06 + initState[6]);
        asIntLittleEndian.set(result, 28, ws07 + initState[7]);
        asIntLittleEndian.set(result, 32, ws08 + initState[8]);
        asIntLittleEndian.set(result, 36, ws09 + initState[9]);
        asIntLittleEndian.set(result, 40, ws10 + initState[10]);
        asIntLittleEndian.set(result, 44, ws11 + initState[11]);
        asIntLittleEndian.set(result, 48, ws12 + initState[12]);
        asIntLittleEndian.set(result, 52, ws13 + initState[13]);
        asIntLittleEndian.set(result, 56, ws14 + initState[14]);
        asIntLittleEndian.set(result, 60, ws15 + initState[15]);
        return 64;
    }

    private void chaCha20Transform(byte[] in, int inOff, int inLen, byte[] out, int outOff) throws KeyException {
        int xformLen;
        for (int remainingData = inLen; remainingData > 0; remainingData -= xformLen) {
            int ksRemain = this.keyStrLimit - this.keyStrOffset;
            if (ksRemain <= 0) {
                if (this.counter <= this.finalCounterValue) {
                    this.keyStrLimit = ChaCha20Cipher.chaCha20Block(this.startState, this.counter, this.keyStream);
                    this.counter += (long)(this.keyStrLimit / 64);
                    if (this.counter > this.finalCounterValue) {
                        this.keyStrLimit -= (int)(this.counter - this.finalCounterValue) * 64;
                    }
                    this.keyStrOffset = 0;
                    ksRemain = this.keyStrLimit;
                } else {
                    throw new KeyException("Counter exhausted.  Reinitialize with new key and/or nonce");
                }
            }
            xformLen = Math.min(remainingData, ksRemain);
            ChaCha20Cipher.xor(this.keyStream, this.keyStrOffset, in, inOff, out, outOff, xformLen);
            outOff += xformLen;
            inOff += xformLen;
            this.keyStrOffset += xformLen;
        }
    }

    private static void xor(byte[] in1, int off1, byte[] in2, int off2, byte[] out, int outOff, int len) {
        while (len >= 8) {
            long v1 = asLongView.get(in1, off1);
            long v2 = asLongView.get(in2, off2);
            asLongView.set(out, outOff, v1 ^ v2);
            off1 += 8;
            off2 += 8;
            outOff += 8;
            len -= 8;
        }
        while (len > 0) {
            out[outOff] = (byte)(in1[off1] ^ in2[off2]);
            ++off1;
            ++off2;
            ++outOff;
            --len;
        }
    }

    private void initAuthenticator() throws InvalidKeyException {
        this.authenticator = new Poly1305();
        byte[] serializedKey = new byte[1024];
        ChaCha20Cipher.chaCha20Block(this.startState, 0L, serializedKey);
        this.authenticator.engineInit(new SecretKeySpec(serializedKey, 0, 32, this.authAlgName), null);
        this.aadLen = 0L;
        this.dataLen = 0L;
    }

    private int authUpdate(byte[] data, int offset, int length) {
        Objects.checkFromIndexSize(offset, length, data.length);
        this.authenticator.engineUpdate(data, offset, length);
        return length;
    }

    private void authFinalizeData(byte[] data, int dataOff, int length, byte[] out, int outOff) throws ShortBufferException {
        if (data != null) {
            this.dataLen += (long)this.authUpdate(data, dataOff, length);
        }
        this.authPad16(this.dataLen);
        this.authWriteLengths(this.aadLen, this.dataLen, this.lenBuf);
        this.authenticator.engineUpdate(this.lenBuf, 0, this.lenBuf.length);
        byte[] tag = this.authenticator.engineDoFinal();
        Objects.checkFromIndexSize(outOff, tag.length, out.length);
        System.arraycopy(tag, 0, out, outOff, tag.length);
        this.aadLen = 0L;
        this.dataLen = 0L;
    }

    private void authPad16(long dataLen) {
        this.authenticator.engineUpdate(padBuf, 0, 16 - ((int)dataLen & 0xF) & 0xF);
    }

    private void authWriteLengths(long aLen, long dLen, byte[] buf) {
        asLongLittleEndian.set(buf, 0, aLen);
        asLongLittleEndian.set(buf, 8, dLen);
    }

    private void resetStartState() {
        this.keyStrLimit = 0;
        this.keyStrOffset = 0;
        this.counter = this.initCounterValue;
        this.aadDone = false;
        this.initialized = this.direction == 2;
    }

    static interface ChaChaEngine {
        public int getOutputSize(int var1, boolean var2);

        public int doUpdate(byte[] var1, int var2, int var3, byte[] var4, int var5) throws ShortBufferException, KeyException;

        public int doFinal(byte[] var1, int var2, int var3, byte[] var4, int var5) throws ShortBufferException, AEADBadTagException, KeyException;

        public int doUpdate(ByteBuffer var1, ByteBuffer var2) throws ShortBufferException, KeyException;

        public int doFinal(ByteBuffer var1, ByteBuffer var2) throws ShortBufferException, KeyException, AEADBadTagException;
    }

    private final class EngineStreamOnly
    implements ChaChaEngine {
        private EngineStreamOnly() {
        }

        @Override
        public int getOutputSize(int inLength, boolean isFinal) {
            return inLength;
        }

        @Override
        public int doUpdate(byte[] in, int inOff, int inLen, byte[] out, int outOff) throws ShortBufferException, KeyException {
            if (ChaCha20Cipher.this.initialized) {
                try {
                    if (out == null) {
                        throw new ShortBufferException("Output buffer too small");
                    }
                    Objects.checkFromIndexSize(outOff, inLen, out.length);
                }
                catch (IndexOutOfBoundsException iobe) {
                    throw new ShortBufferException("Output buffer too small");
                }
                if (in != null) {
                    Objects.checkFromIndexSize(inOff, inLen, in.length);
                    ChaCha20Cipher.this.chaCha20Transform(in, inOff, inLen, out, outOff);
                }
                return inLen;
            }
            throw new IllegalStateException("Must use either a different key or iv.");
        }

        @Override
        public int doFinal(byte[] in, int inOff, int inLen, byte[] out, int outOff) throws ShortBufferException, KeyException {
            return this.doUpdate(in, inOff, inLen, out, outOff);
        }

        @Override
        public int doUpdate(ByteBuffer input, ByteBuffer output) throws ShortBufferException, KeyException {
            byte[] data = new byte[input.remaining()];
            input.get(data);
            this.doUpdate(data, 0, data.length, data, 0);
            output.put(data);
            return data.length;
        }

        @Override
        public int doFinal(ByteBuffer input, ByteBuffer output) throws ShortBufferException, KeyException {
            return this.doUpdate(input, output);
        }
    }

    private final class EngineAEADEnc
    implements ChaChaEngine {
        @Override
        public int getOutputSize(int inLength, boolean isFinal) {
            return isFinal ? Math.addExact(inLength, 16) : inLength;
        }

        private EngineAEADEnc() throws InvalidKeyException {
            ChaCha20Cipher.this.initAuthenticator();
            ChaCha20Cipher.this.counter = ChaCha20Cipher.this.initCounterValue = 1L;
        }

        @Override
        public int doUpdate(byte[] in, int inOff, int inLen, byte[] out, int outOff) throws ShortBufferException, KeyException {
            if (ChaCha20Cipher.this.initialized) {
                if (!ChaCha20Cipher.this.aadDone) {
                    ChaCha20Cipher.this.authPad16(ChaCha20Cipher.this.aadLen);
                    ChaCha20Cipher.this.aadDone = true;
                }
                try {
                    if (out == null) {
                        throw new ShortBufferException("Output buffer too small");
                    }
                    Objects.checkFromIndexSize(outOff, inLen, out.length);
                }
                catch (IndexOutOfBoundsException iobe) {
                    throw new ShortBufferException("Output buffer too small");
                }
                if (in != null) {
                    Objects.checkFromIndexSize(inOff, inLen, in.length);
                    ChaCha20Cipher.this.chaCha20Transform(in, inOff, inLen, out, outOff);
                    ChaCha20Cipher.this.dataLen += (long)ChaCha20Cipher.this.authUpdate(out, outOff, inLen);
                }
                return inLen;
            }
            throw new IllegalStateException("Must use either a different key or iv.");
        }

        @Override
        public int doFinal(byte[] in, int inOff, int inLen, byte[] out, int outOff) throws ShortBufferException, KeyException {
            if (inLen + 16 > out.length - outOff) {
                throw new ShortBufferException("Output buffer too small");
            }
            this.doUpdate(in, inOff, inLen, out, outOff);
            ChaCha20Cipher.this.authFinalizeData(null, 0, 0, out, outOff + inLen);
            ChaCha20Cipher.this.aadDone = false;
            return inLen + 16;
        }

        @Override
        public int doUpdate(ByteBuffer input, ByteBuffer output) throws ShortBufferException, KeyException {
            byte[] data = new byte[input.remaining()];
            input.get(data);
            this.doUpdate(data, 0, data.length, data, 0);
            output.put(data);
            return data.length;
        }

        @Override
        public int doFinal(ByteBuffer input, ByteBuffer output) throws ShortBufferException, KeyException {
            int len = input.remaining();
            byte[] data = new byte[len + 16];
            input.get(data, 0, len);
            this.doFinal(data, 0, len, data, 0);
            output.put(data);
            return data.length;
        }
    }

    private final class EngineAEADDec
    implements ChaChaEngine {
        private AEADBufferedStream cipherBuf = null;
        private final byte[] tag;

        @Override
        public int getOutputSize(int inLen, boolean isFinal) {
            return isFinal ? Integer.max(Math.addExact(inLen - 16, this.getBufferedLength()), 0) : 0;
        }

        private void initBuffer(int len) {
            if (this.cipherBuf == null) {
                this.cipherBuf = new AEADBufferedStream(len);
            }
        }

        private int getBufferedLength() {
            if (this.cipherBuf != null) {
                return this.cipherBuf.size();
            }
            return 0;
        }

        private EngineAEADDec() throws InvalidKeyException {
            ChaCha20Cipher.this.initAuthenticator();
            ChaCha20Cipher.this.counter = ChaCha20Cipher.this.initCounterValue = 1L;
            this.tag = new byte[16];
        }

        @Override
        public int doUpdate(byte[] in, int inOff, int inLen, byte[] out, int outOff) {
            if (ChaCha20Cipher.this.initialized) {
                if (!ChaCha20Cipher.this.aadDone) {
                    ChaCha20Cipher.this.authPad16(ChaCha20Cipher.this.aadLen);
                    ChaCha20Cipher.this.aadDone = true;
                }
                if (in != null) {
                    Objects.checkFromIndexSize(inOff, inLen, in.length);
                    this.initBuffer(inLen);
                    this.cipherBuf.write(in, inOff, inLen);
                }
            } else {
                throw new IllegalStateException("Must use either a different key or iv.");
            }
            return 0;
        }

        @Override
        public int doUpdate(ByteBuffer input, ByteBuffer output) {
            this.initBuffer(input.remaining());
            this.cipherBuf.write(input);
            return 0;
        }

        @Override
        public int doFinal(byte[] in, int inOff, int inLen, byte[] out, int outOff) throws ShortBufferException, AEADBadTagException, KeyException {
            int ctPlusTagLen;
            byte[] ctPlusTag;
            if (this.getBufferedLength() == 0) {
                this.doUpdate(null, inOff, inLen, out, outOff);
                ctPlusTag = in;
                ctPlusTagLen = inLen;
            } else {
                this.doUpdate(in, inOff, inLen, out, outOff);
                ctPlusTag = this.cipherBuf.getBuffer();
                inOff = 0;
                ctPlusTagLen = this.cipherBuf.size();
                this.cipherBuf.reset();
            }
            if (ctPlusTagLen < 16) {
                throw new AEADBadTagException("Input too short - need tag");
            }
            int ctLen = ctPlusTagLen - 16;
            try {
                Objects.checkFromIndexSize(outOff, ctLen, out.length);
            }
            catch (IndexOutOfBoundsException ioobe) {
                throw new ShortBufferException("Output buffer too small");
            }
            ChaCha20Cipher.this.authFinalizeData(ctPlusTag, inOff, ctLen, this.tag, 0);
            long tagCompare = asLongView.get(ctPlusTag, ctLen + inOff) ^ asLongView.get(this.tag, 0) | asLongView.get(ctPlusTag, ctLen + inOff + 8) ^ asLongView.get(this.tag, 8);
            if (tagCompare != 0L) {
                throw new AEADBadTagException("Tag mismatch");
            }
            ChaCha20Cipher.this.chaCha20Transform(ctPlusTag, inOff, ctLen, out, outOff);
            ChaCha20Cipher.this.aadDone = false;
            return ctLen;
        }

        @Override
        public int doFinal(ByteBuffer input, ByteBuffer output) throws ShortBufferException, AEADBadTagException, KeyException {
            int len;
            int inLen = input.remaining();
            byte[] ct = null;
            byte[] buf = null;
            int bufLen = 0;
            int ctLen = this.getBufferedLength() + inLen;
            if (ctLen < 16) {
                throw new AEADBadTagException("Input too short - need tag");
            }
            if (inLen < 16) {
                if (inLen > 0) {
                    this.doUpdate(input, output);
                }
                if (this.cipherBuf != null) {
                    ct = this.cipherBuf.getBuffer();
                }
                len = ctLen;
            } else {
                if (this.cipherBuf != null) {
                    buf = this.cipherBuf.getBuffer();
                    bufLen = this.cipherBuf.size();
                }
                ct = new byte[inLen];
                input.get(ct, 0, inLen);
                len = inLen;
            }
            this.doUpdate(null, 0, 0, null, 0);
            if (buf != null) {
                ChaCha20Cipher.this.dataLen = ChaCha20Cipher.this.authUpdate(buf, 0, bufLen);
            }
            ChaCha20Cipher.this.authFinalizeData(ct, 0, len -= 16, this.tag, 0);
            if ((asLongView.get(ct, len) ^ asLongView.get(this.tag, 0) | asLongView.get(ct, len + 8) ^ asLongView.get(this.tag, 8)) != 0L) {
                throw new AEADBadTagException("Tag mismatch");
            }
            if (buf != null) {
                ChaCha20Cipher.this.chaCha20Transform(buf, 0, bufLen, buf, 0);
                output.put(buf, 0, bufLen);
            }
            ChaCha20Cipher.this.chaCha20Transform(ct, 0, len, ct, 0);
            output.put(ct, 0, len);
            ChaCha20Cipher.this.aadDone = false;
            return ctLen - 16;
        }
    }

    public static final class ChaCha20Poly1305
    extends ChaCha20Cipher {
        public ChaCha20Poly1305() {
            this.mode = 1;
            this.authAlgName = "Poly1305";
        }
    }

    public static final class ChaCha20Only
    extends ChaCha20Cipher {
        public ChaCha20Only() {
            this.mode = 0;
        }
    }
}

