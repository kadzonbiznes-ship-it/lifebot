/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import com.sun.crypto.provider.AESCrypt;
import com.sun.crypto.provider.ConstructKeys;
import com.sun.crypto.provider.GCTR;
import com.sun.crypto.provider.GHASH;
import com.sun.crypto.provider.SunJCE;
import com.sun.crypto.provider.SymmetricCipher;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.GCMParameterSpec;
import jdk.internal.access.JavaNioAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.IntrinsicCandidate;
import sun.nio.ch.DirectBuffer;
import sun.security.jca.JCAUtil;
import sun.security.util.ArrayUtil;

abstract class GaloisCounterMode
extends CipherSpi {
    static int DEFAULT_IV_LEN = 12;
    static int DEFAULT_TAG_LEN = 16;
    private static final int MAX_BUF_SIZE = Integer.MAX_VALUE;
    private static final int TRIGGERLEN = 65536;
    private static final int PARALLEL_LEN = 7680;
    private static final int SPLIT_LEN = 0x100000;
    static final byte[] EMPTY_BUF = new byte[0];
    private static final JavaNioAccess NIO_ACCESS = SharedSecrets.getJavaNioAccess();
    private boolean initialized = false;
    SymmetricCipher blockCipher;
    private GCMEngine engine;
    private boolean encryption = true;
    int tagLenBytes = DEFAULT_TAG_LEN;
    int keySize;
    boolean reInit = false;
    byte[] lastKey = EMPTY_BUF;
    byte[] lastIv = EMPTY_BUF;
    byte[] iv = null;
    SecureRandom random = null;
    private static final VarHandle wrapToByteArray = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN);

    GaloisCounterMode(int keySize, SymmetricCipher embeddedCipher) {
        this.blockCipher = embeddedCipher;
        this.keySize = keySize;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void init(int opmode, Key key, GCMParameterSpec spec) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.encryption = opmode == 1 || opmode == 3;
        int tagLen = spec.getTLen();
        if (tagLen < 96 || tagLen > 128 || (tagLen & 7) != 0) {
            throw new InvalidAlgorithmParameterException("Unsupported TLen value.  Must be one of {128, 120, 112, 104, 96}");
        }
        this.tagLenBytes = tagLen >> 3;
        if (key == null) {
            throw new InvalidKeyException("The key must not be null");
        }
        byte[] keyValue = key.getEncoded();
        if (keyValue == null) {
            throw new InvalidKeyException("Key encoding must not be null");
        }
        if (this.keySize != -1 && keyValue.length != this.keySize) {
            Arrays.fill(keyValue, (byte)0);
            throw new InvalidKeyException("The key must be " + this.keySize + " bytes");
        }
        if (this.encryption) {
            if (MessageDigest.isEqual(keyValue, this.lastKey) && MessageDigest.isEqual(this.iv, this.lastIv)) {
                Arrays.fill(keyValue, (byte)0);
                throw new InvalidAlgorithmParameterException("Cannot reuse iv for GCM encryption");
            }
            if (this.lastKey != null) {
                Arrays.fill(this.lastKey, (byte)0);
            }
            this.lastKey = keyValue;
            this.lastIv = this.iv;
        }
        this.reInit = false;
        try {
            this.blockCipher.init(false, key.getAlgorithm(), keyValue);
        }
        finally {
            if (!this.encryption) {
                Arrays.fill(keyValue, (byte)0);
            }
        }
    }

    @Override
    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
        if (!mode.equalsIgnoreCase("GCM")) {
            throw new NoSuchAlgorithmException("Mode must be GCM");
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
        return this.blockCipher.getBlockSize();
    }

    @Override
    protected int engineGetOutputSize(int inputLen) {
        this.checkInit();
        return this.engine.getOutputSize(inputLen, true);
    }

    @Override
    protected int engineGetKeySize(Key key) throws InvalidKeyException {
        byte[] encoded = key.getEncoded();
        Arrays.fill(encoded, (byte)0);
        if (!AESCrypt.isKeySizeValid(encoded.length)) {
            throw new InvalidKeyException("Invalid key length: " + encoded.length + " bytes");
        }
        return Math.multiplyExact(encoded.length, 8);
    }

    @Override
    protected byte[] engineGetIV() {
        if (this.iv == null) {
            return null;
        }
        return (byte[])this.iv.clone();
    }

    private static byte[] createIv(SecureRandom rand) {
        byte[] iv = new byte[DEFAULT_IV_LEN];
        if (rand == null) {
            rand = JCAUtil.getDefSecureRandom();
        }
        rand.nextBytes(iv);
        return iv;
    }

    @Override
    protected AlgorithmParameters engineGetParameters() {
        GCMParameterSpec spec = new GCMParameterSpec(this.tagLenBytes * 8, this.iv == null ? GaloisCounterMode.createIv(this.random) : (byte[])this.iv.clone());
        try {
            AlgorithmParameters params = AlgorithmParameters.getInstance("GCM", SunJCE.getInstance());
            params.init(spec);
            return params;
        }
        catch (NoSuchAlgorithmException | InvalidParameterSpecException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        this.engine = null;
        if (opmode == 2 || opmode == 4) {
            throw new InvalidKeyException("No GCMParameterSpec specified");
        }
        try {
            this.engineInit(opmode, key, (AlgorithmParameterSpec)null, random);
        }
        catch (InvalidAlgorithmParameterException invalidAlgorithmParameterException) {
            // empty catch block
        }
    }

    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        GCMParameterSpec spec;
        this.random = random;
        this.engine = null;
        if (params == null) {
            this.iv = GaloisCounterMode.createIv(random);
            spec = new GCMParameterSpec(DEFAULT_TAG_LEN * 8, this.iv);
        } else {
            if (!(params instanceof GCMParameterSpec)) {
                throw new InvalidAlgorithmParameterException("AlgorithmParameterSpec not of GCMParameterSpec");
            }
            spec = (GCMParameterSpec)params;
            this.iv = spec.getIV();
            if (this.iv == null) {
                throw new InvalidAlgorithmParameterException("IV is null");
            }
            if (this.iv.length == 0) {
                throw new InvalidAlgorithmParameterException("IV is empty");
            }
        }
        this.init(opmode, key, spec);
        this.initialized = true;
    }

    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        GCMParameterSpec spec = null;
        this.engine = null;
        if (params != null) {
            try {
                spec = params.getParameterSpec(GCMParameterSpec.class);
            }
            catch (InvalidParameterSpecException e) {
                throw new InvalidAlgorithmParameterException(e);
            }
        }
        this.engineInit(opmode, key, spec, random);
    }

    void checkInit() {
        if (!this.initialized) {
            throw new IllegalStateException("Operation not initialized.");
        }
        if (this.engine == null) {
            this.engine = this.encryption ? new GCMEncrypt(this.blockCipher) : new GCMDecrypt(this.blockCipher);
        }
    }

    void checkReInit() {
        if (this.reInit) {
            throw new IllegalStateException("Must use either different key or  iv for GCM encryption");
        }
    }

    @Override
    protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        this.checkInit();
        ArrayUtil.nullAndBoundsCheck(input, inputOffset, inputLen);
        return this.engine.doUpdate(input, inputOffset, inputLen);
    }

    @Override
    protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException {
        this.checkInit();
        ArrayUtil.nullAndBoundsCheck(input, inputOffset, inputLen);
        ArrayUtil.nullAndBoundsCheck(output, outputOffset, output.length - outputOffset);
        int len = this.engine.getOutputSize(inputLen, false);
        if (len > output.length - outputOffset) {
            throw new ShortBufferException("Output buffer too small, must be at least " + len + " bytes long");
        }
        return this.engine.doUpdate(input, inputOffset, inputLen, output, outputOffset);
    }

    @Override
    protected int engineUpdate(ByteBuffer src, ByteBuffer dst) throws ShortBufferException {
        this.checkInit();
        int len = this.engine.getOutputSize(src.remaining(), false);
        if (len > dst.remaining()) {
            throw new ShortBufferException("Output buffer must be at least " + len + " bytes long");
        }
        return this.engine.doUpdate(src, dst);
    }

    @Override
    protected void engineUpdateAAD(byte[] src, int offset, int len) {
        this.checkInit();
        this.engine.updateAAD(src, offset, len);
    }

    @Override
    protected void engineUpdateAAD(ByteBuffer src) {
        this.checkInit();
        if (src.hasArray()) {
            int pos = src.position();
            int len = src.remaining();
            this.engine.updateAAD(src.array(), src.arrayOffset() + pos, len);
            src.position(pos + len);
        } else {
            byte[] aad = new byte[src.remaining()];
            src.get(aad);
            this.engine.updateAAD(aad, 0, aad.length);
        }
    }

    @Override
    protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException, BadPaddingException {
        if (input == null) {
            input = EMPTY_BUF;
        }
        try {
            ArrayUtil.nullAndBoundsCheck(input, inputOffset, inputLen);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalBlockSizeException("input array invalid");
        }
        this.checkInit();
        byte[] output = new byte[this.engine.getOutputSize(inputLen, true)];
        try {
            this.engine.doFinal(input, inputOffset, inputLen, output, 0);
        }
        catch (ShortBufferException e) {
            throw new ProviderException(e);
        }
        finally {
            this.engine = null;
        }
        return output;
    }

    @Override
    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        if (input == null) {
            input = EMPTY_BUF;
        }
        try {
            ArrayUtil.nullAndBoundsCheck(input, inputOffset, inputLen);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            this.engine = null;
            throw new IllegalBlockSizeException("input array invalid");
        }
        this.checkInit();
        int len = this.engine.doFinal(input, inputOffset, inputLen, output, outputOffset);
        this.engine = null;
        return len;
    }

    @Override
    protected int engineDoFinal(ByteBuffer src, ByteBuffer dst) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        this.checkInit();
        int len = this.engine.doFinal(src, dst);
        this.engine = null;
        return len;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
        byte[] encodedKey = null;
        this.checkInit();
        try {
            encodedKey = key.getEncoded();
            if (encodedKey == null || encodedKey.length == 0) {
                throw new InvalidKeyException("Cannot get an encoding of the key to be wrapped");
            }
            byte[] byArray = this.engineDoFinal(encodedKey, 0, encodedKey.length);
            return byArray;
        }
        catch (BadPaddingException badPaddingException) {
        }
        finally {
            this.engine = null;
            if (encodedKey != null) {
                Arrays.fill(encodedKey, (byte)0);
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] encodedKey;
        this.checkInit();
        try {
            encodedKey = this.engineDoFinal(wrappedKey, 0, wrappedKey.length);
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

    static void increment32(byte[] value) {
        int n = value.length - 1;
        while (n >= value.length - 4) {
            int n2 = n--;
            value[n2] = (byte)(value[n2] + 1);
            if (value[n2] == 0) continue;
        }
    }

    private static byte[] getLengthBlock(int ivLenInBytes) {
        byte[] out = new byte[16];
        wrapToByteArray.set(out, 8, ((long)ivLenInBytes & 0xFFFFFFFFL) << 3);
        return out;
    }

    private static byte[] getLengthBlock(int aLenInBytes, int cLenInBytes) {
        byte[] out = new byte[16];
        wrapToByteArray.set(out, 0, ((long)aLenInBytes & 0xFFFFFFFFL) << 3);
        wrapToByteArray.set(out, 8, ((long)cLenInBytes & 0xFFFFFFFFL) << 3);
        return out;
    }

    private static byte[] expandToOneBlock(byte[] in, int inOfs, int len, int blockSize) {
        if (len > blockSize) {
            throw new ProviderException("input " + len + " too long");
        }
        if (len == blockSize && inOfs == 0) {
            return in;
        }
        byte[] paddedIn = new byte[blockSize];
        System.arraycopy(in, inOfs, paddedIn, 0, len);
        return paddedIn;
    }

    private static byte[] getJ0(byte[] iv, byte[] subkeyH, int blockSize) {
        byte[] j0;
        if (iv.length == 12) {
            j0 = GaloisCounterMode.expandToOneBlock(iv, 0, iv.length, blockSize);
            j0[blockSize - 1] = 1;
        } else {
            GHASH g = new GHASH(subkeyH);
            int lastLen = iv.length % blockSize;
            if (lastLen != 0) {
                g.update(iv, 0, iv.length - lastLen);
                byte[] padded = GaloisCounterMode.expandToOneBlock(iv, iv.length - lastLen, lastLen, blockSize);
                g.update(padded);
            } else {
                g.update(iv);
            }
            g.update(GaloisCounterMode.getLengthBlock(iv.length));
            j0 = g.digest();
        }
        return j0;
    }

    private static int implGCMCrypt(byte[] in, int inOfs, int inLen, byte[] ct, int ctOfs, byte[] out, int outOfs, GCTR gctr, GHASH ghash) {
        int len = 0;
        if (inLen > 0x100000 && ct != null) {
            while (inLen >= 0x100000) {
                int partlen = GaloisCounterMode.implGCMCrypt0(in, inOfs + len, 0x100000, ct, ctOfs + len, out, outOfs + len, gctr, ghash);
                len += partlen;
                inLen -= partlen;
            }
        }
        if (inLen > 0) {
            if (ct == null) {
                ghash.update(in, inOfs + len, inLen);
                len += gctr.update(in, inOfs + len, inLen, out, outOfs);
            } else {
                len += GaloisCounterMode.implGCMCrypt0(in, inOfs + len, inLen, ct, ctOfs + len, out, outOfs + len, gctr, ghash);
            }
        }
        return len;
    }

    @IntrinsicCandidate
    private static int implGCMCrypt0(byte[] in, int inOfs, int inLen, byte[] ct, int ctOfs, byte[] out, int outOfs, GCTR gctr, GHASH ghash) {
        inLen -= inLen % 7680;
        int len = 0;
        int cOfs = ctOfs;
        if (inLen >= 65536) {
            int i = 0;
            int segments = inLen / 6;
            segments -= segments % gctr.blockSize;
            do {
                len += gctr.update(in, inOfs + len, segments, out, outOfs + len);
                ghash.update(ct, cOfs, segments);
                cOfs = ctOfs + len;
            } while (++i < 5);
            inLen -= len;
        }
        len += gctr.update(in, inOfs + len, inLen, out, outOfs + len);
        ghash.update(ct, cOfs, inLen);
        return len;
    }

    abstract class GCMEngine {
        byte[] preCounterBlock;
        GCTR gctr;
        GHASH ghash;
        final int blockSize;
        ByteArrayOutputStream aadBuffer = null;
        int sizeOfAAD = 0;
        boolean aadProcessed = false;
        ByteArrayOutputStream ibuffer = null;
        ByteBuffer originalDst = null;
        byte[] originalOut = null;
        int originalOutOfs = 0;
        boolean inPlaceArray = false;

        GCMEngine(SymmetricCipher blockCipher) {
            this.blockSize = blockCipher.getBlockSize();
            byte[] subkeyH = new byte[this.blockSize];
            blockCipher.encryptBlock(subkeyH, 0, subkeyH, 0);
            this.preCounterBlock = GaloisCounterMode.getJ0(GaloisCounterMode.this.iv, subkeyH, this.blockSize);
            byte[] j0Plus1 = (byte[])this.preCounterBlock.clone();
            GaloisCounterMode.increment32(j0Plus1);
            this.gctr = new GCTR(blockCipher, j0Plus1);
            this.ghash = new GHASH(subkeyH);
        }

        abstract int getOutputSize(int var1, boolean var2);

        abstract byte[] doUpdate(byte[] var1, int var2, int var3);

        abstract int doUpdate(byte[] var1, int var2, int var3, byte[] var4, int var5) throws ShortBufferException;

        abstract int doUpdate(ByteBuffer var1, ByteBuffer var2) throws ShortBufferException;

        abstract int doFinal(byte[] var1, int var2, int var3, byte[] var4, int var5) throws IllegalBlockSizeException, AEADBadTagException, ShortBufferException;

        abstract int doFinal(ByteBuffer var1, ByteBuffer var2) throws IllegalBlockSizeException, AEADBadTagException, ShortBufferException;

        void initBuffer(int len) {
            if (this.ibuffer == null) {
                this.ibuffer = new ByteArrayOutputStream(len);
            }
        }

        int getBufferedLength() {
            return this.ibuffer == null ? 0 : this.ibuffer.size();
        }

        int implGCMCrypt(ByteBuffer src, ByteBuffer dst) {
            int srcLen = src.remaining() - src.remaining() % 7680;
            if (srcLen < 7680) {
                return 0;
            }
            if (src.hasArray() && dst.hasArray()) {
                ByteBuffer ct = GaloisCounterMode.this.encryption ? dst : src;
                int len = GaloisCounterMode.implGCMCrypt(src.array(), src.arrayOffset() + src.position(), srcLen, this.inPlaceArray ? null : ct.array(), ct.arrayOffset() + ct.position(), dst.array(), dst.arrayOffset() + dst.position(), this.gctr, this.ghash);
                src.position(src.position() + len);
                dst.position(dst.position() + len);
                return len;
            }
            byte[] bin = new byte[7680];
            byte[] bout = new byte[7680];
            byte[] ct = GaloisCounterMode.this.encryption ? bout : bin;
            int len = srcLen;
            do {
                src.get(bin, 0, 7680);
                dst.put(bout, 0, 7680);
            } while ((len -= GaloisCounterMode.implGCMCrypt(bin, 0, 7680, ct, 0, bout, 0, this.gctr, this.ghash)) >= 7680);
            return srcLen - len;
        }

        int mergeBlock(byte[] buffer, int bufOfs, byte[] in, int inOfs, int inLen, byte[] block) {
            return this.mergeBlock(buffer, bufOfs, buffer.length - bufOfs, in, inOfs, inLen, block);
        }

        int mergeBlock(byte[] buffer, int bufOfs, int bufLen, byte[] in, int inOfs, int inLen, byte[] block) {
            if (bufLen > this.blockSize) {
                throw new RuntimeException("mergeBlock called on an ibuffer too big:  " + bufLen + " bytes");
            }
            System.arraycopy(buffer, bufOfs, block, 0, bufLen);
            int inUsed = Math.min(block.length - bufLen, inLen);
            System.arraycopy(in, inOfs, block, bufLen, inUsed);
            return inUsed;
        }

        void updateAAD(byte[] src, int offset, int len) {
            if (GaloisCounterMode.this.encryption) {
                GaloisCounterMode.this.checkReInit();
            }
            if (this.aadBuffer == null) {
                if (this.sizeOfAAD == 0 && !this.aadProcessed) {
                    this.aadBuffer = new ByteArrayOutputStream(len);
                } else {
                    throw new IllegalStateException("Update has been called; no more AAD data");
                }
            }
            this.aadBuffer.write(src, offset, len);
        }

        void processAAD() {
            if (this.aadBuffer != null) {
                if (this.aadBuffer.size() > 0) {
                    byte[] aad = this.aadBuffer.toByteArray();
                    this.sizeOfAAD = aad.length;
                    int lastLen = aad.length % this.blockSize;
                    if (lastLen != 0) {
                        this.ghash.update(aad, 0, aad.length - lastLen);
                        byte[] padded = GaloisCounterMode.expandToOneBlock(aad, aad.length - lastLen, lastLen, this.blockSize);
                        this.ghash.update(padded);
                    } else {
                        this.ghash.update(aad);
                    }
                }
                this.aadBuffer = null;
            }
            this.aadProcessed = true;
        }

        int doLastBlock(GCMOperation op, ByteBuffer buffer, ByteBuffer src, ByteBuffer dst) {
            int bLen;
            int len = 0;
            int n = bLen = buffer != null ? buffer.remaining() : 0;
            if (bLen > 0) {
                if (bLen >= 7680) {
                    len = this.implGCMCrypt(buffer, dst);
                    bLen -= len;
                }
                if (bLen >= this.blockSize) {
                    int resultLen = op.update(buffer, dst);
                    bLen -= resultLen;
                    len += resultLen;
                }
                if (bLen > 0) {
                    int l;
                    byte[] block = new byte[this.blockSize];
                    int over = buffer.remaining();
                    buffer.get(block, 0, over);
                    int slen = Math.min(src.remaining(), this.blockSize - over);
                    if (slen > 0) {
                        src.get(block, over, slen);
                    }
                    if ((l = slen + over) == this.blockSize) {
                        len += op.update(block, 0, this.blockSize, dst);
                    } else {
                        len += op.doFinal(block, 0, l, block, 0);
                        if (dst != null) {
                            dst.put(block, 0, l);
                        }
                        return len;
                    }
                }
            }
            if (src.remaining() >= 7680) {
                len += this.implGCMCrypt(src, dst);
            }
            return len + op.doFinal(src, dst);
        }

        ByteBuffer overlapDetection(ByteBuffer src, ByteBuffer dst) {
            if (src.isDirect() && dst.isDirect()) {
                DirectBuffer dsrc = (DirectBuffer)((Object)src);
                DirectBuffer ddst = (DirectBuffer)((Object)dst);
                long srcaddr = dsrc.address();
                long dstaddr = ddst.address();
                while (dsrc.attachment() != null) {
                    srcaddr = ((DirectBuffer)dsrc.attachment()).address();
                    dsrc = (DirectBuffer)dsrc.attachment();
                }
                while (ddst.attachment() != null) {
                    dstaddr = ((DirectBuffer)ddst.attachment()).address();
                    ddst = (DirectBuffer)ddst.attachment();
                }
                if (srcaddr != dstaddr) {
                    return dst;
                }
                if (((DirectBuffer)((Object)src)).address() - srcaddr + (long)src.position() >= ((DirectBuffer)((Object)dst)).address() - dstaddr + (long)dst.position()) {
                    return dst;
                }
            } else if (!src.isDirect() && !dst.isDirect()) {
                if (!src.isReadOnly()) {
                    if (src.array() != dst.array()) {
                        return dst;
                    }
                    if (src.position() + src.arrayOffset() >= dst.position() + dst.arrayOffset()) {
                        this.inPlaceArray = !GaloisCounterMode.this.encryption;
                        return dst;
                    }
                }
            } else {
                return dst;
            }
            ByteBuffer tmp = dst.duplicate();
            ByteBuffer bb = ByteBuffer.allocate(dst.remaining());
            tmp.limit(dst.limit());
            tmp.position(dst.position());
            bb.put(tmp);
            bb.flip();
            this.originalDst = dst;
            return bb;
        }

        byte[] overlapDetection(byte[] in, int inOfs, byte[] out, int outOfs) {
            if (in == out) {
                if (inOfs < outOfs) {
                    this.originalOut = out;
                    this.originalOutOfs = outOfs;
                    return new byte[out.length];
                }
                this.inPlaceArray = !GaloisCounterMode.this.encryption;
            }
            return out;
        }

        void restoreDst(ByteBuffer dst) {
            if (this.originalDst == null) {
                return;
            }
            dst.flip();
            this.originalDst.put(dst);
            this.originalDst = null;
        }

        void restoreOut(byte[] out, int len) {
            if (this.originalOut == null) {
                return;
            }
            System.arraycopy(out, this.originalOutOfs, this.originalOut, this.originalOutOfs, len);
            this.originalOut = null;
        }
    }

    class GCMEncrypt
    extends GCMEngine {
        GCMOperation op;
        int processed;

        GCMEncrypt(SymmetricCipher blockCipher) {
            super(blockCipher);
            this.processed = 0;
            this.op = new EncryptOp(this.gctr, this.ghash);
        }

        private void checkDataLength(int ... lengths) {
            int max = Integer.MAX_VALUE;
            for (int len : lengths) {
                if (this.processed <= (max = Math.subtractExact(max, len))) continue;
                throw new ProviderException("SunJCE provider only supports input size up to 2147483647 bytes");
            }
        }

        @Override
        public int getOutputSize(int inLen, boolean isFinal) {
            int len = this.getBufferedLength();
            if (isFinal) {
                return len + inLen + GaloisCounterMode.this.tagLenBytes;
            }
            return (len += inLen) - len % GaloisCounterMode.this.blockCipher.getBlockSize();
        }

        @Override
        byte[] doUpdate(byte[] in, int inOff, int inLen) {
            GaloisCounterMode.this.checkReInit();
            byte[] output = new byte[this.getOutputSize(inLen, false)];
            try {
                this.doUpdate(in, inOff, inLen, output, 0);
            }
            catch (ShortBufferException e) {
                throw new ProviderException("output buffer creation failed", e);
            }
            return output;
        }

        @Override
        public int doUpdate(byte[] in, int inOfs, int inLen, byte[] out, int outOfs) throws ShortBufferException {
            int remainder;
            GaloisCounterMode.this.checkReInit();
            int len = 0;
            int bLen = this.getBufferedLength();
            this.checkDataLength(inLen, bLen);
            this.processAAD();
            out = this.overlapDetection(in, inOfs, out, outOfs);
            if (bLen > 0) {
                byte[] buffer = this.ibuffer.toByteArray();
                int remainder2 = this.blockSize - bLen;
                if (inLen + bLen >= this.blockSize) {
                    byte[] block = new byte[this.blockSize];
                    System.arraycopy(buffer, 0, block, 0, bLen);
                    System.arraycopy(in, inOfs, block, bLen, remainder2);
                    len = this.op.update(block, 0, this.blockSize, out, outOfs);
                    inOfs += remainder2;
                    inLen -= remainder2;
                    outOfs += this.blockSize;
                    this.ibuffer.reset();
                }
            }
            if (inLen >= 7680) {
                int r = GaloisCounterMode.implGCMCrypt(in, inOfs, inLen, out, outOfs, out, outOfs, this.gctr, this.ghash);
                len += r;
                inOfs += r;
                inLen -= r;
                outOfs += r;
            }
            if (inLen >= this.blockSize) {
                int r = this.op.update(in, inOfs, inLen, out, outOfs);
                len += r;
                inOfs += r;
                inLen -= r;
            }
            if ((remainder = inLen % this.blockSize) > 0) {
                this.initBuffer(remainder);
                this.ibuffer.write(in, inOfs + (inLen -= remainder), remainder);
            }
            this.restoreOut(out, len);
            this.processed += len;
            return len;
        }

        @Override
        public int doUpdate(ByteBuffer src, ByteBuffer dst) throws ShortBufferException {
            int srcLen;
            GaloisCounterMode.this.checkReInit();
            int bLen = this.getBufferedLength();
            this.checkDataLength(src.remaining(), bLen);
            int len = 0;
            this.processAAD();
            dst = this.overlapDetection(src, dst);
            if (bLen > 0) {
                int remainder = this.blockSize - bLen;
                if (src.remaining() >= remainder) {
                    byte[] block = new byte[this.blockSize];
                    ByteBuffer buffer = ByteBuffer.wrap(this.ibuffer.toByteArray());
                    buffer.get(block, 0, bLen);
                    src.get(block, bLen, remainder);
                    len += this.op.update(ByteBuffer.wrap(block, 0, this.blockSize), dst);
                    this.ibuffer.reset();
                }
            }
            if ((srcLen = src.remaining()) >= 7680) {
                int resultLen = this.implGCMCrypt(src, dst);
                srcLen -= resultLen;
                len += resultLen;
            }
            if (srcLen >= this.blockSize) {
                int resultLen = this.op.update(src, dst);
                srcLen -= resultLen;
                len += resultLen;
            }
            if (srcLen > 0) {
                this.initBuffer(srcLen);
                byte[] b = new byte[srcLen];
                src.get(b);
                try {
                    this.ibuffer.write(b);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            this.restoreDst(dst);
            this.processed += len;
            return len;
        }

        @Override
        public int doFinal(byte[] in, int inOfs, int inLen, byte[] out, int outOfs) throws IllegalBlockSizeException, ShortBufferException {
            byte[] block;
            GaloisCounterMode.this.checkReInit();
            try {
                ArrayUtil.nullAndBoundsCheck(out, outOfs, this.getOutputSize(inLen, true));
            }
            catch (ArrayIndexOutOfBoundsException e) {
                throw new ShortBufferException("Output buffer invalid");
            }
            int bLen = this.getBufferedLength();
            this.checkDataLength(inLen, bLen, GaloisCounterMode.this.tagLenBytes);
            this.processAAD();
            out = this.overlapDetection(in, inOfs, out, outOfs);
            int len = 0;
            if (bLen > 0) {
                byte[] buffer = this.ibuffer.toByteArray();
                if (bLen + inLen >= this.blockSize) {
                    block = new byte[this.blockSize];
                    int r = this.mergeBlock(buffer, 0, in, inOfs, inLen, block);
                    inOfs += r;
                    inLen -= r;
                    this.op.update(block, 0, this.blockSize, out, outOfs);
                    outOfs += this.blockSize;
                    len += this.blockSize;
                } else {
                    block = new byte[bLen + inLen];
                    System.arraycopy(buffer, 0, block, 0, bLen);
                    System.arraycopy(in, inOfs, block, bLen, inLen);
                    inLen += bLen;
                    in = block;
                    inOfs = 0;
                }
            }
            len += this.op.doFinal(in, inOfs, inLen, out, outOfs);
            outOfs += inLen;
            block = GaloisCounterMode.getLengthBlock(this.sizeOfAAD, this.processed + len);
            this.ghash.update(block);
            block = this.ghash.digest();
            new GCTR(GaloisCounterMode.this.blockCipher, this.preCounterBlock).doFinal(block, 0, GaloisCounterMode.this.tagLenBytes, block, 0);
            System.arraycopy(block, 0, out, outOfs, GaloisCounterMode.this.tagLenBytes);
            this.restoreOut(out, len += GaloisCounterMode.this.tagLenBytes);
            GaloisCounterMode.this.reInit = true;
            return len;
        }

        @Override
        public int doFinal(ByteBuffer src, ByteBuffer dst) throws IllegalBlockSizeException, ShortBufferException {
            GaloisCounterMode.this.checkReInit();
            dst = this.overlapDetection(src, dst);
            int len = src.remaining() + this.getBufferedLength();
            this.checkDataLength(len, GaloisCounterMode.this.tagLenBytes);
            if (dst.remaining() < len + GaloisCounterMode.this.tagLenBytes) {
                throw new ShortBufferException("Output buffer too small, must be at least " + (len + GaloisCounterMode.this.tagLenBytes) + " bytes long");
            }
            this.processAAD();
            if (len > 0) {
                this.processed += this.doLastBlock(this.op, this.ibuffer == null || this.ibuffer.size() == 0 ? null : ByteBuffer.wrap(this.ibuffer.toByteArray()), src, dst);
            }
            if (this.ibuffer != null) {
                this.ibuffer.reset();
            }
            byte[] block = GaloisCounterMode.getLengthBlock(this.sizeOfAAD, this.processed);
            this.ghash.update(block);
            block = this.ghash.digest();
            new GCTR(GaloisCounterMode.this.blockCipher, this.preCounterBlock).doFinal(block, 0, GaloisCounterMode.this.tagLenBytes, block, 0);
            dst.put(block, 0, GaloisCounterMode.this.tagLenBytes);
            this.restoreDst(dst);
            GaloisCounterMode.this.reInit = true;
            return len + GaloisCounterMode.this.tagLenBytes;
        }
    }

    class GCMDecrypt
    extends GCMEngine {
        byte[] tag;
        int tagOfs;

        GCMDecrypt(SymmetricCipher blockCipher) {
            super(blockCipher);
            this.tagOfs = 0;
        }

        private void checkDataLength(int ... lengths) {
            int max = Integer.MAX_VALUE;
            for (int len : lengths) {
                if ((max = Math.subtractExact(max, len)) >= 0) continue;
                throw new ProviderException("SunJCE provider only supports input size up to 2147483647 bytes");
            }
        }

        @Override
        public int getOutputSize(int inLen, boolean isFinal) {
            if (!isFinal) {
                return 0;
            }
            return Math.max(inLen + this.getBufferedLength() - GaloisCounterMode.this.tagLenBytes, 0);
        }

        void findTag(byte[] in, int inOfs, int inLen) {
            this.tag = new byte[GaloisCounterMode.this.tagLenBytes];
            if (inLen >= GaloisCounterMode.this.tagLenBytes) {
                this.tagOfs = inLen - GaloisCounterMode.this.tagLenBytes;
                System.arraycopy(in, inOfs + this.tagOfs, this.tag, 0, GaloisCounterMode.this.tagLenBytes);
            } else {
                byte[] buffer = this.ibuffer.toByteArray();
                this.tagOfs = this.mergeBlock(buffer, buffer.length - (GaloisCounterMode.this.tagLenBytes - inLen), in, inOfs, inLen, this.tag) - GaloisCounterMode.this.tagLenBytes;
            }
        }

        @Override
        byte[] doUpdate(byte[] in, int inOff, int inLen) {
            try {
                this.doUpdate(in, inOff, inLen, null, 0);
            }
            catch (ShortBufferException shortBufferException) {
                // empty catch block
            }
            return new byte[0];
        }

        @Override
        public int doUpdate(byte[] in, int inOfs, int inLen, byte[] out, int outOfs) throws ShortBufferException {
            this.processAAD();
            if (inLen > 0) {
                this.initBuffer(inLen);
                this.ibuffer.write(in, inOfs, inLen);
            }
            return 0;
        }

        @Override
        public int doUpdate(ByteBuffer src, ByteBuffer dst) throws ShortBufferException {
            this.processAAD();
            if (src.remaining() > 0) {
                if (src.hasArray()) {
                    this.doUpdate(src.array(), src.arrayOffset() + src.position(), src.remaining(), null, 0);
                    src.position(src.limit());
                } else {
                    byte[] b = new byte[src.remaining()];
                    src.get(b);
                    this.initBuffer(b.length);
                    try {
                        this.ibuffer.write(b);
                    }
                    catch (IOException e) {
                        throw new ProviderException("Unable to add remaining input to the buffer", e);
                    }
                }
            }
            return 0;
        }

        @Override
        public int doFinal(byte[] in, int inOfs, int inLen, byte[] out, int outOfs) throws IllegalBlockSizeException, AEADBadTagException, ShortBufferException {
            int len = inLen + this.getBufferedLength();
            if (len < GaloisCounterMode.this.tagLenBytes) {
                throw new AEADBadTagException("Input data too short to contain an expected tag length of " + GaloisCounterMode.this.tagLenBytes + "bytes");
            }
            try {
                ArrayUtil.nullAndBoundsCheck(out, outOfs, len - GaloisCounterMode.this.tagLenBytes);
            }
            catch (ArrayIndexOutOfBoundsException e) {
                throw new ShortBufferException("Output buffer invalid");
            }
            if (len - GaloisCounterMode.this.tagLenBytes > out.length - outOfs) {
                throw new ShortBufferException("Output buffer too small, must be at least " + (len - GaloisCounterMode.this.tagLenBytes) + " bytes long");
            }
            this.checkDataLength(len - GaloisCounterMode.this.tagLenBytes);
            this.processAAD();
            this.findTag(in, inOfs, inLen);
            out = this.overlapDetection(in, inOfs, out, outOfs);
            len = this.decryptBlocks(new DecryptOp(this.gctr, this.ghash), in, inOfs, inLen, out, outOfs);
            byte[] block = GaloisCounterMode.getLengthBlock(this.sizeOfAAD, len);
            this.ghash.update(block);
            block = this.ghash.digest();
            new GCTR(GaloisCounterMode.this.blockCipher, this.preCounterBlock).doFinal(block, 0, GaloisCounterMode.this.tagLenBytes, block, 0);
            int mismatch = 0;
            for (int i = 0; i < GaloisCounterMode.this.tagLenBytes; ++i) {
                mismatch |= this.tag[i] ^ block[i];
            }
            if (mismatch != 0) {
                if (!this.inPlaceArray) {
                    Arrays.fill(out, outOfs, outOfs + len, (byte)0);
                }
                throw new AEADBadTagException("Tag mismatch");
            }
            this.restoreOut(out, len);
            return len;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public int doFinal(ByteBuffer src, ByteBuffer dst) throws IllegalBlockSizeException, AEADBadTagException, ShortBufferException {
            ByteBuffer tag;
            ByteBuffer ct = src.duplicate();
            ByteBuffer buffer = null;
            int len = ct.remaining() - GaloisCounterMode.this.tagLenBytes;
            if (this.getBufferedLength() != 0) {
                buffer = ByteBuffer.wrap(this.ibuffer.toByteArray());
                len += buffer.remaining();
            }
            if (len < 0) {
                throw new AEADBadTagException("Input data too short to contain an expected tag length of " + GaloisCounterMode.this.tagLenBytes + "bytes");
            }
            this.checkDataLength(len);
            if (len > dst.remaining()) {
                throw new ShortBufferException("Output buffer too small, must be at least " + len + " bytes long");
            }
            if (ct.remaining() >= GaloisCounterMode.this.tagLenBytes) {
                tag = src.duplicate();
                tag.position(ct.limit() - GaloisCounterMode.this.tagLenBytes);
                ct.limit(ct.limit() - GaloisCounterMode.this.tagLenBytes);
            } else if (buffer != null) {
                tag = ByteBuffer.allocate(GaloisCounterMode.this.tagLenBytes);
                int limit = buffer.remaining() - (GaloisCounterMode.this.tagLenBytes - ct.remaining());
                buffer.mark();
                buffer.position(limit);
                tag.put(buffer);
                buffer.reset();
                buffer.limit(limit);
                tag.put(ct);
                tag.flip();
            } else {
                throw new AEADBadTagException("Input data too short to contain an expected tag length of " + GaloisCounterMode.this.tagLenBytes + "bytes");
            }
            dst = this.overlapDetection(src, dst);
            dst.mark();
            this.processAAD();
            len = this.doLastBlock(new DecryptOp(this.gctr, this.ghash), buffer, ct, dst);
            byte[] block = GaloisCounterMode.getLengthBlock(this.sizeOfAAD, len);
            this.ghash.update(block);
            block = this.ghash.digest();
            new GCTR(GaloisCounterMode.this.blockCipher, this.preCounterBlock).doFinal(block, 0, GaloisCounterMode.this.tagLenBytes, block, 0);
            int mismatch = 0;
            for (int i = 0; i < GaloisCounterMode.this.tagLenBytes; ++i) {
                mismatch |= tag.get() ^ block[i];
            }
            if (mismatch != 0) {
                dst.reset();
                if (!this.inPlaceArray) {
                    if (dst.hasArray()) {
                        int ofs = dst.arrayOffset() + dst.position();
                        Arrays.fill(dst.array(), ofs, ofs + len, (byte)0);
                    } else {
                        NIO_ACCESS.acquireSession(dst);
                        try {
                            Unsafe.getUnsafe().setMemory(((DirectBuffer)((Object)dst)).address(), len + dst.position(), (byte)0);
                        }
                        finally {
                            NIO_ACCESS.releaseSession(dst);
                        }
                    }
                }
                throw new AEADBadTagException("Tag mismatch");
            }
            src.position(src.limit());
            GaloisCounterMode.this.engine = null;
            this.restoreDst(dst);
            return len;
        }

        int decryptBlocks(GCMOperation op, byte[] in, int inOfs, int inLen, byte[] out, int outOfs) {
            int len = 0;
            int bLen = this.getBufferedLength();
            if (this.tagOfs < 0) {
                inLen = 0;
                bLen += this.tagOfs;
            } else {
                inLen -= GaloisCounterMode.this.tagLenBytes;
            }
            if (bLen > 0) {
                int resultLen;
                int bufRemainder;
                byte[] buffer = this.ibuffer.toByteArray();
                if (bLen >= 7680) {
                    len = GaloisCounterMode.implGCMCrypt(buffer, 0, bLen, buffer, 0, out, outOfs, this.gctr, this.ghash);
                    outOfs += len;
                }
                if ((bufRemainder = bLen - len) >= this.blockSize) {
                    resultLen = op.update(buffer, len, bufRemainder, out, outOfs);
                    len += resultLen;
                    outOfs += resultLen;
                    bufRemainder -= resultLen;
                }
                if (bufRemainder > 0) {
                    byte[] block = new byte[this.blockSize];
                    int inUsed = this.mergeBlock(buffer, len, bufRemainder, in, inOfs, inLen, block);
                    inOfs += inUsed;
                    if ((inLen -= inUsed) > 0) {
                        resultLen = op.update(block, 0, this.blockSize, out, outOfs);
                        outOfs += resultLen;
                        len += resultLen;
                    } else {
                        in = block;
                        inOfs = 0;
                        inLen = inUsed + bufRemainder;
                    }
                }
            }
            return len + op.doFinal(in, inOfs, inLen, out, outOfs);
        }
    }

    public static interface GCMOperation {
        public int update(byte[] var1, int var2, int var3, byte[] var4, int var5);

        public int update(byte[] var1, int var2, int var3, ByteBuffer var4);

        public int update(ByteBuffer var1, ByteBuffer var2);

        public int doFinal(byte[] var1, int var2, int var3, byte[] var4, int var5);

        public int doFinal(ByteBuffer var1, ByteBuffer var2);
    }

    static final class DecryptOp
    implements GCMOperation {
        GCTR gctr;
        GHASH ghash;

        DecryptOp(GCTR c, GHASH g) {
            this.gctr = c;
            this.ghash = g;
        }

        @Override
        public int update(byte[] in, int inOfs, int inLen, byte[] out, int outOfs) {
            this.ghash.update(in, inOfs, inLen);
            return this.gctr.update(in, inOfs, inLen, out, outOfs);
        }

        @Override
        public int update(byte[] in, int inOfs, int inLen, ByteBuffer dst) {
            this.ghash.update(in, inOfs, inLen);
            return this.gctr.update(in, inOfs, inLen, dst);
        }

        @Override
        public int update(ByteBuffer src, ByteBuffer dst) {
            src.mark();
            this.ghash.update(src, src.remaining());
            src.reset();
            return this.gctr.update(src, dst);
        }

        @Override
        public int doFinal(byte[] in, int inOfs, int inLen, byte[] out, int outOfs) {
            int len = 0;
            if (inLen >= 7680) {
                len += GaloisCounterMode.implGCMCrypt(in, inOfs, inLen, in == out ? null : in, inOfs, out, outOfs, this.gctr, this.ghash);
            }
            this.ghash.doFinal(in, inOfs + len, inLen - len);
            return len + this.gctr.doFinal(in, inOfs + len, inLen - len, out, outOfs + len);
        }

        @Override
        public int doFinal(ByteBuffer src, ByteBuffer dst) {
            src.mark();
            this.ghash.doFinal(src, src.remaining());
            src.reset();
            return this.gctr.doFinal(src, dst);
        }
    }

    static final class EncryptOp
    implements GCMOperation {
        GCTR gctr;
        GHASH ghash;

        EncryptOp(GCTR c, GHASH g) {
            this.gctr = c;
            this.ghash = g;
        }

        @Override
        public int update(byte[] in, int inOfs, int inLen, byte[] out, int outOfs) {
            int len = this.gctr.update(in, inOfs, inLen, out, outOfs);
            this.ghash.update(out, outOfs, len);
            return len;
        }

        @Override
        public int update(byte[] in, int inOfs, int inLen, ByteBuffer dst) {
            dst.mark();
            int len = this.gctr.update(in, inOfs, inLen, dst);
            dst.reset();
            this.ghash.update(dst, len);
            return len;
        }

        @Override
        public int update(ByteBuffer src, ByteBuffer dst) {
            dst.mark();
            int len = this.gctr.update(src, dst);
            dst.reset();
            this.ghash.update(dst, len);
            return len;
        }

        @Override
        public int doFinal(byte[] in, int inOfs, int inLen, byte[] out, int outOfs) {
            int len = 0;
            if (inLen >= 7680) {
                len = GaloisCounterMode.implGCMCrypt(in, inOfs, inLen, out, outOfs, out, outOfs, this.gctr, this.ghash);
                inLen -= len;
                outOfs += len;
            }
            this.gctr.doFinal(in, inOfs + len, inLen, out, outOfs);
            return len + this.ghash.doFinal(out, outOfs, inLen);
        }

        @Override
        public int doFinal(ByteBuffer src, ByteBuffer dst) {
            dst.mark();
            int len = this.gctr.doFinal(src, dst);
            dst.reset();
            this.ghash.doFinal(dst, len);
            return len;
        }
    }

    public static final class AES256
    extends GaloisCounterMode {
        public AES256() {
            super(32, new AESCrypt());
        }
    }

    public static final class AES192
    extends GaloisCounterMode {
        public AES192() {
            super(24, new AESCrypt());
        }
    }

    public static final class AES128
    extends GaloisCounterMode {
        public AES128() {
            super(16, new AESCrypt());
        }
    }

    public static final class AESGCM
    extends GaloisCounterMode {
        public AESGCM() {
            super(-1, new AESCrypt());
        }
    }
}

