/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider;

import java.util.Arrays;
import java.util.Objects;
import jdk.internal.util.Preconditions;
import jdk.internal.vm.annotation.IntrinsicCandidate;
import sun.security.provider.ByteArrayAccess;
import sun.security.provider.DigestBase;

public final class SHA
extends DigestBase {
    private int[] W;
    private int[] state = new int[5];
    private static final int round1_kt = 1518500249;
    private static final int round2_kt = 1859775393;
    private static final int round3_kt = -1894007588;
    private static final int round4_kt = -899497514;

    public SHA() {
        super("SHA-1", 20, 64);
        this.resetHashes();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        SHA copy = (SHA)super.clone();
        copy.state = (int[])copy.state.clone();
        copy.W = null;
        return copy;
    }

    @Override
    void implReset() {
        this.resetHashes();
        if (this.W != null) {
            Arrays.fill(this.W, 0);
        }
    }

    private void resetHashes() {
        this.state[0] = 1732584193;
        this.state[1] = -271733879;
        this.state[2] = -1732584194;
        this.state[3] = 271733878;
        this.state[4] = -1009589776;
    }

    @Override
    void implDigest(byte[] out, int ofs) {
        long bitsProcessed = this.bytesProcessed << 3;
        int index = (int)this.bytesProcessed & 0x3F;
        int padLen = index < 56 ? 56 - index : 120 - index;
        this.engineUpdate(padding, 0, padLen);
        ByteArrayAccess.i2bBig4((int)(bitsProcessed >>> 32), this.buffer, 56);
        ByteArrayAccess.i2bBig4((int)bitsProcessed, this.buffer, 60);
        this.implCompress(this.buffer, 0);
        ByteArrayAccess.i2bBig(this.state, 0, out, ofs, 20);
    }

    @Override
    void implCompress(byte[] buf, int ofs) {
        this.implCompressCheck(buf, ofs);
        this.implCompress0(buf, ofs);
    }

    private void implCompressCheck(byte[] buf, int ofs) {
        Objects.requireNonNull(buf);
        Preconditions.checkFromIndexSize(ofs, 64, buf.length, Preconditions.AIOOBE_FORMATTER);
    }

    @IntrinsicCandidate
    private void implCompress0(byte[] buf, int ofs) {
        int temp;
        int i;
        if (this.W == null) {
            this.W = new int[80];
        }
        ByteArrayAccess.b2iBig64(buf, ofs, this.W);
        for (int t = 16; t <= 79; ++t) {
            int temp2 = this.W[t - 3] ^ this.W[t - 8] ^ this.W[t - 14] ^ this.W[t - 16];
            this.W[t] = Integer.rotateLeft(temp2, 1);
        }
        int a = this.state[0];
        int b = this.state[1];
        int c = this.state[2];
        int d = this.state[3];
        int e = this.state[4];
        for (i = 0; i < 20; ++i) {
            temp = Integer.rotateLeft(a, 5) + (b & c | ~b & d) + e + this.W[i] + 1518500249;
            e = d;
            d = c;
            c = Integer.rotateLeft(b, 30);
            b = a;
            a = temp;
        }
        for (i = 20; i < 40; ++i) {
            temp = Integer.rotateLeft(a, 5) + (b ^ c ^ d) + e + this.W[i] + 1859775393;
            e = d;
            d = c;
            c = Integer.rotateLeft(b, 30);
            b = a;
            a = temp;
        }
        for (i = 40; i < 60; ++i) {
            temp = Integer.rotateLeft(a, 5) + (b & c | b & d | c & d) + e + this.W[i] + -1894007588;
            e = d;
            d = c;
            c = Integer.rotateLeft(b, 30);
            b = a;
            a = temp;
        }
        for (i = 60; i < 80; ++i) {
            temp = Integer.rotateLeft(a, 5) + (b ^ c ^ d) + e + this.W[i] + -899497514;
            e = d;
            d = c;
            c = Integer.rotateLeft(b, 30);
            b = a;
            a = temp;
        }
        this.state[0] = this.state[0] + a;
        this.state[1] = this.state[1] + b;
        this.state[2] = this.state[2] + c;
        this.state[3] = this.state[3] + d;
        this.state[4] = this.state[4] + e;
    }
}

