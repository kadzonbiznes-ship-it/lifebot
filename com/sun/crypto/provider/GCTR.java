/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import com.sun.crypto.provider.CounterMode;
import com.sun.crypto.provider.GCM;
import com.sun.crypto.provider.GaloisCounterMode;
import com.sun.crypto.provider.SymmetricCipher;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

final class GCTR
extends CounterMode
implements GCM {
    private static final int MAX_LEN = 1024;
    private byte[] block;

    GCTR(SymmetricCipher cipher, byte[] initialCounterBlk) {
        super(cipher);
        if (initialCounterBlk.length != this.blockSize) {
            throw new RuntimeException("length of initial counter block (" + initialCounterBlk.length + ") not equal to blockSize (" + this.blockSize + ")");
        }
        this.iv = initialCounterBlk;
        this.reset();
    }

    @Override
    String getFeedback() {
        return "GCTR";
    }

    private long blocksUntilRollover() {
        ByteBuffer buf = ByteBuffer.wrap(this.counter, this.counter.length - 4, 4);
        buf.order(ByteOrder.BIG_ENDIAN);
        long ctr32 = 0xFFFFFFFFL & (long)buf.getInt();
        long blocksLeft = 0x100000000L - ctr32;
        return blocksLeft;
    }

    private void checkBlock() {
        if (this.block == null) {
            this.block = new byte[this.blockSize];
        } else {
            Arrays.fill(this.block, (byte)0);
        }
    }

    @Override
    public int update(byte[] in, int inOfs, int inLen, byte[] out, int outOfs) {
        int numOfCompleteBlocks;
        if (inLen == 0) {
            return 0;
        }
        if (inLen - inOfs > in.length) {
            throw new RuntimeException("input length out of bound");
        }
        if (inLen < 0) {
            throw new RuntimeException("input length unsupported");
        }
        if (out.length - outOfs < inLen - inLen % this.blockSize) {
            throw new RuntimeException("output buffer too small");
        }
        long blocksLeft = this.blocksUntilRollover();
        if ((long)(numOfCompleteBlocks = (inLen -= inLen % this.blockSize) / this.blockSize) >= blocksLeft) {
            this.checkBlock();
            for (int i = 0; i < numOfCompleteBlocks; ++i) {
                this.embeddedCipher.encryptBlock(this.counter, 0, this.block, 0);
                for (int n = 0; n < this.blockSize; ++n) {
                    int index = i * this.blockSize + n;
                    out[outOfs + index] = (byte)(in[inOfs + index] ^ this.block[n]);
                }
                GaloisCounterMode.increment32(this.counter);
            }
            return inLen;
        }
        return this.encrypt(in, inOfs, inLen, out, outOfs);
    }

    @Override
    public int update(byte[] in, int inOfs, int inLen, ByteBuffer dst) {
        int len;
        if (!dst.isDirect()) {
            int len2 = this.update(in, inOfs, inLen, dst.array(), dst.arrayOffset() + dst.position());
            dst.position(dst.position() + len2);
            return len2;
        }
        if (inLen - inOfs > in.length) {
            throw new RuntimeException("input length out of bound");
        }
        if (inLen < 0) {
            throw new RuntimeException("input length unsupported");
        }
        int numOfCompleteBlocks = inLen / this.blockSize;
        long blocksLeft = this.blocksUntilRollover();
        if ((long)numOfCompleteBlocks >= blocksLeft) {
            this.checkBlock();
            for (int i = 0; i < numOfCompleteBlocks; ++i) {
                this.embeddedCipher.encryptBlock(this.counter, 0, this.block, 0);
                for (int n = 0; n < this.blockSize; ++n) {
                    int index = i * this.blockSize + n;
                    dst.put((byte)(in[inOfs + index] ^ this.block[n]));
                }
                GaloisCounterMode.increment32(this.counter);
            }
            return inLen;
        }
        int processed = len = inLen - inLen % this.blockSize;
        byte[] out = new byte[Math.min(1024, len)];
        int offset = inOfs;
        while (processed > 1024) {
            this.encrypt(in, offset, 1024, out, 0);
            dst.put(out, 0, 1024);
            processed -= 1024;
            offset += 1024;
        }
        this.encrypt(in, offset, processed, out, 0);
        dst.put(out, 0, Math.min(dst.remaining(), processed));
        return len;
    }

    @Override
    public int update(ByteBuffer src, ByteBuffer dst) {
        int processed;
        int len;
        if (src.hasArray() && dst.hasArray()) {
            int len2 = this.update(src.array(), src.arrayOffset() + src.position(), src.remaining() - src.remaining() % this.blockSize, dst.array(), dst.arrayOffset() + dst.position());
            src.position(src.position() + len2);
            dst.position(dst.position() + len2);
            return len2;
        }
        long blocksLeft = this.blocksUntilRollover();
        int numOfCompleteBlocks = src.remaining() / this.blockSize;
        if ((long)numOfCompleteBlocks >= blocksLeft) {
            this.checkBlock();
            for (int i = 0; i < numOfCompleteBlocks; ++i) {
                this.embeddedCipher.encryptBlock(this.counter, 0, this.block, 0);
                for (int n = 0; n < this.blockSize; ++n) {
                    dst.put((byte)(src.get() ^ this.block[n]));
                }
                GaloisCounterMode.increment32(this.counter);
            }
            return numOfCompleteBlocks * this.blockSize;
        }
        byte[] in = new byte[Math.min(1024, len)];
        for (processed = len = src.remaining() - src.remaining() % this.blockSize; processed > 1024; processed -= 1024) {
            src.get(in, 0, 1024);
            this.encrypt(in, 0, 1024, in, 0);
            dst.put(in, 0, 1024);
        }
        src.get(in, 0, processed);
        this.encrypt(in, 0, processed, in, 0);
        dst.put(in, 0, processed);
        return len;
    }

    @Override
    public int doFinal(byte[] in, int inOfs, int inLen, byte[] out, int outOfs) {
        if (inLen == 0) {
            return 0;
        }
        int lastBlockSize = inLen % this.blockSize;
        int completeBlkLen = inLen - lastBlockSize;
        this.update(in, inOfs, completeBlkLen, out, outOfs);
        if (lastBlockSize != 0) {
            this.checkBlock();
            this.embeddedCipher.encryptBlock(this.counter, 0, this.block, 0);
            for (int n = 0; n < lastBlockSize; ++n) {
                out[outOfs + completeBlkLen + n] = (byte)(in[inOfs + completeBlkLen + n] ^ this.block[n]);
            }
        }
        return inLen;
    }

    @Override
    public int doFinal(ByteBuffer src, ByteBuffer dst) {
        if (src.hasArray() && dst.hasArray()) {
            int len = this.doFinal(src.array(), src.arrayOffset() + src.position(), src.remaining(), dst.array(), dst.arrayOffset() + dst.position());
            src.position(src.position() + len);
            dst.position(dst.position() + len);
            return len;
        }
        int len = src.remaining();
        int lastBlockSize = len % this.blockSize;
        this.update(src, dst);
        if (lastBlockSize != 0) {
            this.checkBlock();
            this.embeddedCipher.encryptBlock(this.counter, 0, this.block, 0);
            for (int n = 0; n < lastBlockSize; ++n) {
                dst.put((byte)(src.get() ^ this.block[n]));
            }
        }
        return len;
    }
}

