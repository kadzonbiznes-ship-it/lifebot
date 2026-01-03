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

abstract class SHA5
extends DigestBase {
    private static final int ITERATION = 80;
    private static final long[] ROUND_CONSTS = new long[]{4794697086780616226L, 8158064640168781261L, -5349999486874862801L, -1606136188198331460L, 4131703408338449720L, 6480981068601479193L, -7908458776815382629L, -6116909921290321640L, -2880145864133508542L, 1334009975649890238L, 2608012711638119052L, 6128411473006802146L, 8268148722764581231L, -9160688886553864527L, -7215885187991268811L, -4495734319001033068L, -1973867731355612462L, -1171420211273849373L, 1135362057144423861L, 2597628984639134821L, 3308224258029322869L, 5365058923640841347L, 6679025012923562964L, 8573033837759648693L, -7476448914759557205L, -6327057829258317296L, -5763719355590565569L, -4658551843659510044L, -4116276920077217854L, -3051310485924567259L, 489312712824947311L, 1452737877330783856L, 2861767655752347644L, 3322285676063803686L, 5560940570517711597L, 5996557281743188959L, 7280758554555802590L, 8532644243296465576L, -9096487096722542874L, -7894198246740708037L, -6719396339535248540L, -6333637450476146687L, -4446306890439682159L, -4076793802049405392L, -3345356375505022440L, -2983346525034927856L, -860691631967231958L, 1182934255886127544L, 1847814050463011016L, 2177327727835720531L, 2830643537854262169L, 3796741975233480872L, 4115178125766777443L, 5681478168544905931L, 6601373596472566643L, 7507060721942968483L, 8399075790359081724L, 8693463985226723168L, -8878714635349349518L, -8302665154208450068L, -8016688836872298968L, -6606660893046293015L, -4685533653050689259L, -4147400797238176981L, -3880063495543823972L, -3348786107499101689L, -1523767162380948706L, -757361751448694408L, 500013540394364858L, 748580250866718886L, 1242879168328830382L, 1977374033974150939L, 2944078676154940804L, 3659926193048069267L, 4368137639120453308L, 4836135668995329356L, 5532061633213252278L, 6448918945643986474L, 6902733635092675308L, 7801388544844847127L};
    private long[] W;
    private long[] state;
    private final long[] initialHashes;

    SHA5(String name, int digestLength, long[] initialHashes) {
        super(name, digestLength, 128);
        this.initialHashes = initialHashes;
        this.state = new long[8];
        this.resetHashes();
    }

    @Override
    final void implReset() {
        this.resetHashes();
        if (this.W != null) {
            Arrays.fill(this.W, 0L);
        }
    }

    private void resetHashes() {
        System.arraycopy(this.initialHashes, 0, this.state, 0, this.state.length);
    }

    @Override
    final void implDigest(byte[] out, int ofs) {
        long bitsProcessed = this.bytesProcessed << 3;
        int index = (int)this.bytesProcessed & 0x7F;
        int padLen = index < 112 ? 112 - index : 240 - index;
        this.engineUpdate(padding, 0, padLen + 8);
        ByteArrayAccess.i2bBig4((int)(bitsProcessed >>> 32), this.buffer, 120);
        ByteArrayAccess.i2bBig4((int)bitsProcessed, this.buffer, 124);
        this.implCompress(this.buffer, 0);
        int len = this.engineGetDigestLength();
        if (len == 28) {
            ByteArrayAccess.l2bBig(this.state, 0, out, ofs, 24);
            ByteArrayAccess.i2bBig4((int)(this.state[3] >> 32), out, ofs + 24);
        } else {
            ByteArrayAccess.l2bBig(this.state, 0, out, ofs, len);
        }
    }

    private static long lf_ch(long x, long y, long z) {
        return x & y ^ (x ^ 0xFFFFFFFFFFFFFFFFL) & z;
    }

    private static long lf_maj(long x, long y, long z) {
        return x & y ^ x & z ^ y & z;
    }

    private static long lf_R(long x, int s) {
        return x >>> s;
    }

    private static long lf_S(long x, int s) {
        return Long.rotateRight(x, s);
    }

    private static long lf_sigma0(long x) {
        return SHA5.lf_S(x, 28) ^ SHA5.lf_S(x, 34) ^ SHA5.lf_S(x, 39);
    }

    private static long lf_sigma1(long x) {
        return SHA5.lf_S(x, 14) ^ SHA5.lf_S(x, 18) ^ SHA5.lf_S(x, 41);
    }

    private static long lf_delta0(long x) {
        return SHA5.lf_S(x, 1) ^ SHA5.lf_S(x, 8) ^ SHA5.lf_R(x, 7);
    }

    private static long lf_delta1(long x) {
        return SHA5.lf_S(x, 19) ^ SHA5.lf_S(x, 61) ^ SHA5.lf_R(x, 6);
    }

    @Override
    final void implCompress(byte[] buf, int ofs) {
        this.implCompressCheck(buf, ofs);
        this.implCompress0(buf, ofs);
    }

    private void implCompressCheck(byte[] buf, int ofs) {
        Objects.requireNonNull(buf);
        Preconditions.checkFromIndexSize(ofs, 128, buf.length, Preconditions.AIOOBE_FORMATTER);
    }

    @IntrinsicCandidate
    private void implCompress0(byte[] buf, int ofs) {
        if (this.W == null) {
            this.W = new long[80];
        }
        ByteArrayAccess.b2lBig128(buf, ofs, this.W);
        for (int t = 16; t < 80; ++t) {
            this.W[t] = SHA5.lf_delta1(this.W[t - 2]) + this.W[t - 7] + SHA5.lf_delta0(this.W[t - 15]) + this.W[t - 16];
        }
        long a = this.state[0];
        long b = this.state[1];
        long c = this.state[2];
        long d = this.state[3];
        long e = this.state[4];
        long f = this.state[5];
        long g = this.state[6];
        long h = this.state[7];
        for (int i = 0; i < 80; ++i) {
            long T1 = h + SHA5.lf_sigma1(e) + SHA5.lf_ch(e, f, g) + ROUND_CONSTS[i] + this.W[i];
            long T2 = SHA5.lf_sigma0(a) + SHA5.lf_maj(a, b, c);
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
        SHA5 copy = (SHA5)super.clone();
        copy.state = (long[])copy.state.clone();
        copy.W = null;
        return copy;
    }

    public static final class SHA512_256
    extends SHA5 {
        private static final long[] INITIAL_HASHES = new long[]{2463787394917988140L, -6965556091613846334L, 2563595384472711505L, -7622211418569250115L, -7626776825740460061L, -4729309413028513390L, 3098927326965381290L, 1060366662362279074L};

        public SHA512_256() {
            super("SHA-512/256", 32, INITIAL_HASHES);
        }
    }

    public static final class SHA512_224
    extends SHA5 {
        private static final long[] INITIAL_HASHES = new long[]{-8341449602262348382L, 8350123849800275158L, 2160240930085379202L, 7466358040605728719L, 1111592415079452072L, 8638871050018654530L, 4583966954114332360L, 1230299281376055969L};

        public SHA512_224() {
            super("SHA-512/224", 28, INITIAL_HASHES);
        }
    }

    public static final class SHA384
    extends SHA5 {
        private static final long[] INITIAL_HASHES = new long[]{-3766243637369397544L, 7105036623409894663L, -7973340178411365097L, 1526699215303891257L, 7436329637833083697L, -8163818279084223215L, -2662702644619276377L, 5167115440072839076L};

        public SHA384() {
            super("SHA-384", 48, INITIAL_HASHES);
        }
    }

    public static final class SHA512
    extends SHA5 {
        private static final long[] INITIAL_HASHES = new long[]{7640891576956012808L, -4942790177534073029L, 4354685564936845355L, -6534734903238641935L, 5840696475078001361L, -7276294671716946913L, 2270897969802886507L, 6620516959819538809L};

        public SHA512() {
            super("SHA-512", 64, INITIAL_HASHES);
        }
    }
}

