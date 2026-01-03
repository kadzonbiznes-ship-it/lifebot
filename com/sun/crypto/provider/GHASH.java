/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import com.sun.crypto.provider.GCM;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.ProviderException;
import jdk.internal.vm.annotation.IntrinsicCandidate;

final class GHASH
implements Cloneable,
GCM {
    private static final int AES_BLOCK_SIZE = 16;
    private static final VarHandle asLongView = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN);
    private static final int MAX_LEN = 1024;
    private long[] subkeyHtbl;
    private final long[] state;

    private static void blockMult(long[] st, long[] subH) {
        long carry;
        long mask;
        int i;
        long Z0 = 0L;
        long Z1 = 0L;
        long V0 = subH[0];
        long V1 = subH[1];
        long X = st[0];
        for (i = 0; i < 64; ++i) {
            mask = X >> 63;
            Z0 ^= V0 & mask;
            Z1 ^= V1 & mask;
            mask = V1 << 63 >> 63;
            carry = V0 & 1L;
            V0 >>>= 1;
            V1 = V1 >>> 1 | carry << 63;
            V0 ^= 0xE100000000000000L & mask;
            X <<= 1;
        }
        X = st[1];
        for (i = 64; i < 127; ++i) {
            mask = X >> 63;
            Z0 ^= V0 & mask;
            Z1 ^= V1 & mask;
            mask = V1 << 63 >> 63;
            carry = V0 & 1L;
            V0 >>>= 1;
            V1 = V1 >>> 1 | carry << 63;
            V0 ^= 0xE100000000000000L & mask;
            X <<= 1;
        }
        long mask2 = X >> 63;
        st[0] = Z0 ^= V0 & mask2;
        st[1] = Z1 ^= V1 & mask2;
    }

    GHASH(byte[] subkeyH) throws ProviderException {
        if (subkeyH == null || subkeyH.length != 16) {
            throw new ProviderException("Internal error");
        }
        this.state = new long[2];
        this.subkeyHtbl = new long[18];
        this.subkeyHtbl[0] = asLongView.get(subkeyH, 0);
        this.subkeyHtbl[1] = asLongView.get(subkeyH, 8);
    }

    private GHASH(GHASH g) {
        this.state = (long[])g.state.clone();
        this.subkeyHtbl = (long[])g.subkeyHtbl.clone();
    }

    public GHASH clone() {
        return new GHASH(this);
    }

    private static void processBlock(byte[] data, int ofs, long[] st, long[] subH) {
        st[0] = st[0] ^ asLongView.get(data, ofs);
        st[1] = st[1] ^ asLongView.get(data, ofs + 8);
        GHASH.blockMult(st, subH);
    }

    int update(byte[] in) {
        return this.update(in, 0, in.length);
    }

    int update(byte[] in, int inOfs, int inLen) {
        if (inLen == 0) {
            return 0;
        }
        int len = inLen - inLen % 16;
        GHASH.ghashRangeCheck(in, inOfs, len, this.state, this.subkeyHtbl);
        GHASH.processBlocks(in, inOfs, len / 16, this.state, this.subkeyHtbl);
        return len;
    }

    int update(ByteBuffer ct, int inLen) {
        int to_process;
        if ((inLen -= inLen % 16) == 0) {
            return 0;
        }
        if (ct.isDirect()) {
            int processed = inLen;
            this.processBlocksDirect(ct, inLen);
            return processed;
        }
        if (!ct.isReadOnly()) {
            int processed = this.update(ct.array(), ct.arrayOffset() + ct.position(), inLen);
            ct.position(ct.position() + processed);
            return processed;
        }
        byte[] in = new byte[Math.min(1024, inLen)];
        for (to_process = inLen; to_process > 1024; to_process -= 1024) {
            ct.get(in, 0, 1024);
            this.update(in, 0, 1024);
        }
        ct.get(in, 0, to_process);
        this.update(in, 0, to_process);
        return inLen;
    }

    int doFinal(ByteBuffer src, int inLen) {
        int processed = 0;
        if (inLen >= 16) {
            processed = this.update(src, inLen);
        }
        if (inLen == processed) {
            return processed;
        }
        byte[] block = new byte[16];
        src.get(block, 0, inLen - processed);
        this.update(block, 0, 16);
        return inLen;
    }

    int doFinal(byte[] in, int inOfs, int inLen) {
        int remainder = inLen % 16;
        inOfs += this.update(in, inOfs, inLen - remainder);
        if (remainder > 0) {
            byte[] block = new byte[16];
            System.arraycopy(in, inOfs, block, 0, remainder);
            this.update(block, 0, 16);
        }
        return inLen;
    }

    private static void ghashRangeCheck(byte[] in, int inOfs, int inLen, long[] st, long[] subH) {
        if (inLen < 0) {
            throw new RuntimeException("invalid input length: " + inLen);
        }
        if (inOfs < 0) {
            throw new RuntimeException("invalid offset: " + inOfs);
        }
        if (inLen > in.length - inOfs) {
            throw new RuntimeException("input length out of bound: " + inLen + " > " + (in.length - inOfs));
        }
        if (inLen % 16 != 0) {
            throw new RuntimeException("input length/block size mismatch: " + inLen);
        }
        if (st.length != 2) {
            throw new RuntimeException("internal state has invalid length: " + st.length);
        }
        if (subH.length != 18) {
            throw new RuntimeException("internal subkeyHtbl has invalid length: " + subH.length);
        }
    }

    @IntrinsicCandidate
    private static void processBlocks(byte[] data, int inOfs, int blocks, long[] st, long[] subH) {
        int offset = inOfs;
        while (blocks > 0) {
            GHASH.processBlock(data, offset, st, subH);
            --blocks;
            offset += 16;
        }
    }

    private void processBlocksDirect(ByteBuffer ct, int inLen) {
        byte[] data = new byte[Math.min(1024, inLen)];
        while (inLen > 1024) {
            ct.get(data, 0, 1024);
            GHASH.processBlocks(data, 0, 64, this.state, this.subkeyHtbl);
            inLen -= 1024;
        }
        if (inLen >= 16) {
            int len = inLen - inLen % 16;
            ct.get(data, 0, len);
            GHASH.processBlocks(data, 0, len / 16, this.state, this.subkeyHtbl);
        }
    }

    byte[] digest() {
        byte[] result = new byte[16];
        asLongView.set(result, 0, this.state[0]);
        asLongView.set(result, 8, this.state[1]);
        this.state[0] = 0L;
        this.state[1] = 0L;
        return result;
    }

    @Override
    public int update(byte[] in, int inOfs, int inLen, byte[] out, int outOfs) {
        return this.update(in, inOfs, inLen);
    }

    @Override
    public int update(byte[] in, int inOfs, int inLen, ByteBuffer dst) {
        return this.update(in, inOfs, inLen);
    }

    @Override
    public int update(ByteBuffer src, ByteBuffer dst) {
        return this.update(src, src.remaining());
    }

    @Override
    public int doFinal(byte[] in, int inOfs, int inLen, byte[] out, int outOfs) {
        return this.doFinal(in, inOfs, inLen);
    }

    @Override
    public int doFinal(ByteBuffer src, ByteBuffer dst) {
        return this.doFinal(src, src.remaining());
    }
}

