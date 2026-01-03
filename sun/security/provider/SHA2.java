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

abstract class SHA2
extends DigestBase {
    private static final int ITERATION = 64;
    private static final int BLOCKSIZE = 64;
    private static final int[] ROUND_CONSTS = new int[]{1116352408, 1899447441, -1245643825, -373957723, 961987163, 1508970993, -1841331548, -1424204075, -670586216, 310598401, 607225278, 1426881987, 1925078388, -2132889090, -1680079193, -1046744716, -459576895, -272742522, 264347078, 604807628, 770255983, 1249150122, 1555081692, 1996064986, -1740746414, -1473132947, -1341970488, -1084653625, -958395405, -710438585, 113926993, 338241895, 666307205, 773529912, 1294757372, 1396182291, 1695183700, 1986661051, -2117940946, -1838011259, -1564481375, -1474664885, -1035236496, -949202525, -778901479, -694614492, -200395387, 275423344, 430227734, 506948616, 659060556, 883997877, 958139571, 1322822218, 1537002063, 1747873779, 1955562222, 2024104815, -2067236844, -1933114872, -1866530822, -1538233109, -1090935817, -965641998};
    private int[] W;
    private int[] state;
    private final int[] initialHashes;

    SHA2(String name, int digestLength, int[] initialHashes) {
        super(name, digestLength, 64);
        this.initialHashes = initialHashes;
        this.state = new int[8];
        this.resetHashes();
    }

    @Override
    void implReset() {
        this.resetHashes();
        if (this.W != null) {
            Arrays.fill(this.W, 0);
        }
    }

    private void resetHashes() {
        System.arraycopy(this.initialHashes, 0, this.state, 0, this.state.length);
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
        ByteArrayAccess.i2bBig(this.state, 0, out, ofs, this.engineGetDigestLength());
    }

    protected void implDigestFixedLengthPreprocessed(byte[] input, int inLen, byte[] output, int outOffset, int outLen) {
        this.implReset();
        for (int ofs = 0; ofs < inLen; ofs += 64) {
            this.implCompress0(input, ofs);
        }
        ByteArrayAccess.i2bBig(this.state, 0, output, outOffset, outLen);
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
        if (this.W == null) {
            this.W = new int[64];
        }
        ByteArrayAccess.b2iBig64(buf, ofs, this.W);
        for (int t = 16; t < 64; ++t) {
            int W_t2 = this.W[t - 2];
            int W_t15 = this.W[t - 15];
            int delta0_W_t15 = Integer.rotateRight(W_t15, 7) ^ Integer.rotateRight(W_t15, 18) ^ W_t15 >>> 3;
            int delta1_W_t2 = Integer.rotateRight(W_t2, 17) ^ Integer.rotateRight(W_t2, 19) ^ W_t2 >>> 10;
            this.W[t] = delta0_W_t15 + delta1_W_t2 + this.W[t - 7] + this.W[t - 16];
        }
        int a = this.state[0];
        int b = this.state[1];
        int c = this.state[2];
        int d = this.state[3];
        int e = this.state[4];
        int f = this.state[5];
        int g = this.state[6];
        int h = this.state[7];
        for (int i = 0; i < 64; ++i) {
            int sigma0_a = Integer.rotateRight(a, 2) ^ Integer.rotateRight(a, 13) ^ Integer.rotateRight(a, 22);
            int sigma1_e = Integer.rotateRight(e, 6) ^ Integer.rotateRight(e, 11) ^ Integer.rotateRight(e, 25);
            int ch_efg = g ^ e & (f ^ g);
            int maj_abc = a & b ^ (a ^ b) & c;
            int T1 = h + sigma1_e + ch_efg + ROUND_CONSTS[i] + this.W[i];
            int T2 = sigma0_a + maj_abc;
            h = g;
            g = f;
            f = e;
            e = d + T1;
            d = c;
            c = b;
            b = a;
            a = T1 + T2;
        }
        this.state[0] = this.state[0] + a;
        this.state[1] = this.state[1] + b;
        this.state[2] = this.state[2] + c;
        this.state[3] = this.state[3] + d;
        this.state[4] = this.state[4] + e;
        this.state[5] = this.state[5] + f;
        this.state[6] = this.state[6] + g;
        this.state[7] = this.state[7] + h;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        SHA2 copy = (SHA2)super.clone();
        copy.state = (int[])copy.state.clone();
        copy.W = null;
        return copy;
    }

    public static final class SHA256
    extends SHA2 {
        private static final int[] INITIAL_HASHES = new int[]{1779033703, -1150833019, 1013904242, -1521486534, 1359893119, -1694144372, 528734635, 1541459225};

        public SHA256() {
            super("SHA-256", 32, INITIAL_HASHES);
        }
    }

    public static final class SHA224
    extends SHA2 {
        private static final int[] INITIAL_HASHES = new int[]{-1056596264, 914150663, 812702999, -150054599, -4191439, 1750603025, 1694076839, -1090891868};

        public SHA224() {
            super("SHA-224", 28, INITIAL_HASHES);
        }
    }
}

