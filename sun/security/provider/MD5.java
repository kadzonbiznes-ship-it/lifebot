/*
 * Decompiled with CFR 0.152.
 */
package sun.security.provider;

import java.util.Objects;
import jdk.internal.util.Preconditions;
import jdk.internal.vm.annotation.IntrinsicCandidate;
import sun.security.provider.ByteArrayAccess;
import sun.security.provider.DigestBase;

public final class MD5
extends DigestBase {
    private int[] state = new int[4];
    private static final int S11 = 7;
    private static final int S12 = 12;
    private static final int S13 = 17;
    private static final int S14 = 22;
    private static final int S21 = 5;
    private static final int S22 = 9;
    private static final int S23 = 14;
    private static final int S24 = 20;
    private static final int S31 = 4;
    private static final int S32 = 11;
    private static final int S33 = 16;
    private static final int S34 = 23;
    private static final int S41 = 6;
    private static final int S42 = 10;
    private static final int S43 = 15;
    private static final int S44 = 21;

    public MD5() {
        super("MD5", 16, 64);
        this.implReset();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        MD5 copy = (MD5)super.clone();
        copy.state = (int[])copy.state.clone();
        return copy;
    }

    @Override
    void implReset() {
        this.state[0] = 1732584193;
        this.state[1] = -271733879;
        this.state[2] = -1732584194;
        this.state[3] = 271733878;
    }

    @Override
    void implDigest(byte[] out, int ofs) {
        long bitsProcessed = this.bytesProcessed << 3;
        int index = (int)this.bytesProcessed & 0x3F;
        int padLen = index < 56 ? 56 - index : 120 - index;
        this.engineUpdate(padding, 0, padLen);
        ByteArrayAccess.i2bLittle4((int)bitsProcessed, this.buffer, 56);
        ByteArrayAccess.i2bLittle4((int)(bitsProcessed >>> 32), this.buffer, 60);
        this.implCompress(this.buffer, 0);
        ByteArrayAccess.i2bLittle(this.state, 0, out, ofs, 16);
    }

    private static int FF(int a, int b, int c, int d, int x, int s, int ac) {
        return Integer.rotateLeft(a += (b & c | ~b & d) + x + ac, s) + b;
    }

    private static int GG(int a, int b, int c, int d, int x, int s, int ac) {
        return Integer.rotateLeft(a += (b & d | c & ~d) + x + ac, s) + b;
    }

    private static int HH(int a, int b, int c, int d, int x, int s, int ac) {
        return Integer.rotateLeft(a += (b ^ c ^ d) + x + ac, s) + b;
    }

    private static int II(int a, int b, int c, int d, int x, int s, int ac) {
        return Integer.rotateLeft(a += (c ^ (b | ~d)) + x + ac, s) + b;
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
    void implCompress0(byte[] buf, int ofs) {
        int a = this.state[0];
        int b = this.state[1];
        int c = this.state[2];
        int d = this.state[3];
        int x0 = ByteArrayAccess.LE.INT_ARRAY.get(buf, ofs);
        int x1 = ByteArrayAccess.LE.INT_ARRAY.get(buf, ofs + 4);
        int x2 = ByteArrayAccess.LE.INT_ARRAY.get(buf, ofs + 8);
        int x3 = ByteArrayAccess.LE.INT_ARRAY.get(buf, ofs + 12);
        int x4 = ByteArrayAccess.LE.INT_ARRAY.get(buf, ofs + 16);
        int x5 = ByteArrayAccess.LE.INT_ARRAY.get(buf, ofs + 20);
        int x6 = ByteArrayAccess.LE.INT_ARRAY.get(buf, ofs + 24);
        int x7 = ByteArrayAccess.LE.INT_ARRAY.get(buf, ofs + 28);
        int x8 = ByteArrayAccess.LE.INT_ARRAY.get(buf, ofs + 32);
        int x9 = ByteArrayAccess.LE.INT_ARRAY.get(buf, ofs + 36);
        int x10 = ByteArrayAccess.LE.INT_ARRAY.get(buf, ofs + 40);
        int x11 = ByteArrayAccess.LE.INT_ARRAY.get(buf, ofs + 44);
        int x12 = ByteArrayAccess.LE.INT_ARRAY.get(buf, ofs + 48);
        int x13 = ByteArrayAccess.LE.INT_ARRAY.get(buf, ofs + 52);
        int x14 = ByteArrayAccess.LE.INT_ARRAY.get(buf, ofs + 56);
        int x15 = ByteArrayAccess.LE.INT_ARRAY.get(buf, ofs + 60);
        a = MD5.FF(a, b, c, d, x0, 7, -680876936);
        d = MD5.FF(d, a, b, c, x1, 12, -389564586);
        c = MD5.FF(c, d, a, b, x2, 17, 606105819);
        b = MD5.FF(b, c, d, a, x3, 22, -1044525330);
        a = MD5.FF(a, b, c, d, x4, 7, -176418897);
        d = MD5.FF(d, a, b, c, x5, 12, 1200080426);
        c = MD5.FF(c, d, a, b, x6, 17, -1473231341);
        b = MD5.FF(b, c, d, a, x7, 22, -45705983);
        a = MD5.FF(a, b, c, d, x8, 7, 1770035416);
        d = MD5.FF(d, a, b, c, x9, 12, -1958414417);
        c = MD5.FF(c, d, a, b, x10, 17, -42063);
        b = MD5.FF(b, c, d, a, x11, 22, -1990404162);
        a = MD5.FF(a, b, c, d, x12, 7, 1804603682);
        d = MD5.FF(d, a, b, c, x13, 12, -40341101);
        c = MD5.FF(c, d, a, b, x14, 17, -1502002290);
        b = MD5.FF(b, c, d, a, x15, 22, 1236535329);
        a = MD5.GG(a, b, c, d, x1, 5, -165796510);
        d = MD5.GG(d, a, b, c, x6, 9, -1069501632);
        c = MD5.GG(c, d, a, b, x11, 14, 643717713);
        b = MD5.GG(b, c, d, a, x0, 20, -373897302);
        a = MD5.GG(a, b, c, d, x5, 5, -701558691);
        d = MD5.GG(d, a, b, c, x10, 9, 38016083);
        c = MD5.GG(c, d, a, b, x15, 14, -660478335);
        b = MD5.GG(b, c, d, a, x4, 20, -405537848);
        a = MD5.GG(a, b, c, d, x9, 5, 568446438);
        d = MD5.GG(d, a, b, c, x14, 9, -1019803690);
        c = MD5.GG(c, d, a, b, x3, 14, -187363961);
        b = MD5.GG(b, c, d, a, x8, 20, 1163531501);
        a = MD5.GG(a, b, c, d, x13, 5, -1444681467);
        d = MD5.GG(d, a, b, c, x2, 9, -51403784);
        c = MD5.GG(c, d, a, b, x7, 14, 1735328473);
        b = MD5.GG(b, c, d, a, x12, 20, -1926607734);
        a = MD5.HH(a, b, c, d, x5, 4, -378558);
        d = MD5.HH(d, a, b, c, x8, 11, -2022574463);
        c = MD5.HH(c, d, a, b, x11, 16, 1839030562);
        b = MD5.HH(b, c, d, a, x14, 23, -35309556);
        a = MD5.HH(a, b, c, d, x1, 4, -1530992060);
        d = MD5.HH(d, a, b, c, x4, 11, 1272893353);
        c = MD5.HH(c, d, a, b, x7, 16, -155497632);
        b = MD5.HH(b, c, d, a, x10, 23, -1094730640);
        a = MD5.HH(a, b, c, d, x13, 4, 681279174);
        d = MD5.HH(d, a, b, c, x0, 11, -358537222);
        c = MD5.HH(c, d, a, b, x3, 16, -722521979);
        b = MD5.HH(b, c, d, a, x6, 23, 76029189);
        a = MD5.HH(a, b, c, d, x9, 4, -640364487);
        d = MD5.HH(d, a, b, c, x12, 11, -421815835);
        c = MD5.HH(c, d, a, b, x15, 16, 530742520);
        b = MD5.HH(b, c, d, a, x2, 23, -995338651);
        a = MD5.II(a, b, c, d, x0, 6, -198630844);
        d = MD5.II(d, a, b, c, x7, 10, 1126891415);
        c = MD5.II(c, d, a, b, x14, 15, -1416354905);
        b = MD5.II(b, c, d, a, x5, 21, -57434055);
        a = MD5.II(a, b, c, d, x12, 6, 1700485571);
        d = MD5.II(d, a, b, c, x3, 10, -1894986606);
        c = MD5.II(c, d, a, b, x10, 15, -1051523);
        b = MD5.II(b, c, d, a, x1, 21, -2054922799);
        a = MD5.II(a, b, c, d, x8, 6, 1873313359);
        d = MD5.II(d, a, b, c, x15, 10, -30611744);
        c = MD5.II(c, d, a, b, x6, 15, -1560198380);
        b = MD5.II(b, c, d, a, x13, 21, 1309151649);
        a = MD5.II(a, b, c, d, x4, 6, -145523070);
        d = MD5.II(d, a, b, c, x11, 10, -1120210379);
        c = MD5.II(c, d, a, b, x2, 15, 718787259);
        b = MD5.II(b, c, d, a, x9, 21, -343485551);
        this.state[0] = this.state[0] + a;
        this.state[1] = this.state[1] + b;
        this.state[2] = this.state[2] + c;
        this.state[3] = this.state[3] + d;
    }
}

