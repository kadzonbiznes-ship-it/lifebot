/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import com.sun.crypto.provider.Padding;
import java.util.Arrays;
import javax.crypto.ShortBufferException;

final class PKCS5Padding
implements Padding {
    private int blockSize;

    PKCS5Padding(int blockSize) {
        this.blockSize = blockSize;
    }

    @Override
    public void padWithLen(byte[] in, int off, int len) throws ShortBufferException {
        if (in == null) {
            return;
        }
        int idx = Math.addExact(off, len);
        if (idx > in.length) {
            throw new ShortBufferException("Buffer too small to hold padding");
        }
        byte paddingOctet = (byte)(len & 0xFF);
        Arrays.fill(in, off, idx, paddingOctet);
    }

    @Override
    public int unpad(byte[] in, int off, int len) {
        if (in == null || len == 0) {
            return 0;
        }
        int idx = Math.addExact(off, len);
        byte lastByte = in[idx - 1];
        int padValue = lastByte & 0xFF;
        if (padValue < 1 || padValue > this.blockSize) {
            return -1;
        }
        int start = idx - padValue;
        if (start < off) {
            return -1;
        }
        for (int i = start; i < idx; ++i) {
            if (in[i] == lastByte) continue;
            return -1;
        }
        return start;
    }

    @Override
    public int padLength(int len) {
        int paddingOctet = this.blockSize - len % this.blockSize;
        return paddingOctet;
    }
}

