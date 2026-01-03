/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import jdk.internal.util.Preconditions;
import jdk.internal.vm.annotation.IntrinsicCandidate;
import sun.nio.cs.ISO_8859_1;

public class Base64 {
    private Base64() {
    }

    public static Encoder getEncoder() {
        return Encoder.RFC4648;
    }

    public static Encoder getUrlEncoder() {
        return Encoder.RFC4648_URLSAFE;
    }

    public static Encoder getMimeEncoder() {
        return Encoder.RFC2045;
    }

    public static Encoder getMimeEncoder(int lineLength, byte[] lineSeparator) {
        Objects.requireNonNull(lineSeparator);
        int[] base64 = Decoder.fromBase64;
        for (byte b : lineSeparator) {
            if (base64[b & 0xFF] == -1) continue;
            throw new IllegalArgumentException("Illegal base64 line separator character 0x" + Integer.toString(b, 16));
        }
        if ((lineLength &= 0xFFFFFFFC) <= 0) {
            return Encoder.RFC4648;
        }
        return new Encoder(false, lineSeparator, lineLength, true);
    }

    public static Decoder getDecoder() {
        return Decoder.RFC4648;
    }

    public static Decoder getUrlDecoder() {
        return Decoder.RFC4648_URLSAFE;
    }

    public static Decoder getMimeDecoder() {
        return Decoder.RFC2045;
    }

    public static class Encoder {
        private final byte[] newline;
        private final int linemax;
        private final boolean isURL;
        private final boolean doPadding;
        private static final char[] toBase64 = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
        private static final char[] toBase64URL = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};
        private static final int MIMELINEMAX = 76;
        private static final byte[] CRLF = new byte[]{13, 10};
        static final Encoder RFC4648 = new Encoder(false, null, -1, true);
        static final Encoder RFC4648_URLSAFE = new Encoder(true, null, -1, true);
        static final Encoder RFC2045 = new Encoder(false, CRLF, 76, true);

        private Encoder(boolean isURL, byte[] newline, int linemax, boolean doPadding) {
            this.isURL = isURL;
            this.newline = newline;
            this.linemax = linemax;
            this.doPadding = doPadding;
        }

        private final int encodedOutLength(int srclen, boolean throwOOME) {
            int len = 0;
            try {
                if (this.doPadding) {
                    len = Math.multiplyExact(4, Math.addExact(srclen, 2) / 3);
                } else {
                    int n = srclen % 3;
                    len = Math.addExact(Math.multiplyExact(4, srclen / 3), n == 0 ? 0 : n + 1);
                }
                if (this.linemax > 0) {
                    len = Math.addExact(len, (len - 1) / this.linemax * this.newline.length);
                }
            }
            catch (ArithmeticException ex) {
                if (throwOOME) {
                    throw new OutOfMemoryError("Encoded size is too large");
                }
                len = -1;
            }
            return len;
        }

        public byte[] encode(byte[] src) {
            int len = this.encodedOutLength(src.length, true);
            byte[] dst = new byte[len];
            int ret = this.encode0(src, 0, src.length, dst);
            if (ret != dst.length) {
                return Arrays.copyOf(dst, ret);
            }
            return dst;
        }

        public int encode(byte[] src, byte[] dst) {
            int len = this.encodedOutLength(src.length, false);
            if (dst.length < len || len == -1) {
                throw new IllegalArgumentException("Output byte array is too small for encoding all input bytes");
            }
            return this.encode0(src, 0, src.length, dst);
        }

        public String encodeToString(byte[] src) {
            byte[] encoded = this.encode(src);
            return new String(encoded, 0, 0, encoded.length);
        }

        public ByteBuffer encode(ByteBuffer buffer) {
            int len = this.encodedOutLength(buffer.remaining(), true);
            byte[] dst = new byte[len];
            int ret = 0;
            if (buffer.hasArray()) {
                ret = this.encode0(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.arrayOffset() + buffer.limit(), dst);
                buffer.position(buffer.limit());
            } else {
                byte[] src = new byte[buffer.remaining()];
                buffer.get(src);
                ret = this.encode0(src, 0, src.length, dst);
            }
            if (ret != dst.length) {
                dst = Arrays.copyOf(dst, ret);
            }
            return ByteBuffer.wrap(dst);
        }

        public OutputStream wrap(OutputStream os) {
            Objects.requireNonNull(os);
            return new EncOutputStream(os, this.isURL ? toBase64URL : toBase64, this.newline, this.linemax, this.doPadding);
        }

        public Encoder withoutPadding() {
            if (!this.doPadding) {
                return this;
            }
            return new Encoder(this.isURL, this.newline, this.linemax, false);
        }

        @IntrinsicCandidate
        private void encodeBlock(byte[] src, int sp, int sl, byte[] dst, int dp, boolean isURL) {
            char[] base64 = isURL ? toBase64URL : toBase64;
            int sp0 = sp;
            int dp0 = dp;
            while (sp0 < sl) {
                int bits = (src[sp0++] & 0xFF) << 16 | (src[sp0++] & 0xFF) << 8 | src[sp0++] & 0xFF;
                dst[dp0++] = (byte)base64[bits >>> 18 & 0x3F];
                dst[dp0++] = (byte)base64[bits >>> 12 & 0x3F];
                dst[dp0++] = (byte)base64[bits >>> 6 & 0x3F];
                dst[dp0++] = (byte)base64[bits & 0x3F];
            }
        }

        private int encode0(byte[] src, int off, int end, byte[] dst) {
            char[] base64 = this.isURL ? toBase64URL : toBase64;
            int sp = off;
            int slen = (end - off) / 3 * 3;
            int sl = off + slen;
            if (this.linemax > 0 && slen > this.linemax / 4 * 3) {
                slen = this.linemax / 4 * 3;
            }
            int dp = 0;
            while (sp < sl) {
                int sl0 = Math.min(sp + slen, sl);
                this.encodeBlock(src, sp, sl0, dst, dp, this.isURL);
                int dlen = (sl0 - sp) / 3 * 4;
                dp += dlen;
                sp = sl0;
                if (dlen != this.linemax || sp >= end) continue;
                for (byte b : this.newline) {
                    dst[dp++] = b;
                }
            }
            if (sp < end) {
                int b0 = src[sp++] & 0xFF;
                dst[dp++] = (byte)base64[b0 >> 2];
                if (sp == end) {
                    dst[dp++] = (byte)base64[b0 << 4 & 0x3F];
                    if (this.doPadding) {
                        dst[dp++] = 61;
                        dst[dp++] = 61;
                    }
                } else {
                    int b1 = src[sp++] & 0xFF;
                    dst[dp++] = (byte)base64[b0 << 4 & 0x3F | b1 >> 4];
                    dst[dp++] = (byte)base64[b1 << 2 & 0x3F];
                    if (this.doPadding) {
                        dst[dp++] = 61;
                    }
                }
            }
            return dp;
        }
    }

    public static class Decoder {
        private final boolean isURL;
        private final boolean isMIME;
        private static final int[] fromBase64;
        private static final int[] fromBase64URL;
        static final Decoder RFC4648;
        static final Decoder RFC4648_URLSAFE;
        static final Decoder RFC2045;

        private Decoder(boolean isURL, boolean isMIME) {
            this.isURL = isURL;
            this.isMIME = isMIME;
        }

        public byte[] decode(byte[] src) {
            byte[] dst = new byte[this.decodedOutLength(src, 0, src.length)];
            int ret = this.decode0(src, 0, src.length, dst);
            if (ret != dst.length) {
                dst = Arrays.copyOf(dst, ret);
            }
            return dst;
        }

        public byte[] decode(String src) {
            return this.decode(src.getBytes(ISO_8859_1.INSTANCE));
        }

        public int decode(byte[] src, byte[] dst) {
            int len = this.decodedOutLength(src, 0, src.length);
            if (dst.length < len || len == -1) {
                throw new IllegalArgumentException("Output byte array is too small for decoding all input bytes");
            }
            return this.decode0(src, 0, src.length, dst);
        }

        public ByteBuffer decode(ByteBuffer buffer) {
            int pos0 = buffer.position();
            try {
                int sl;
                int sp;
                byte[] src;
                if (buffer.hasArray()) {
                    src = buffer.array();
                    sp = buffer.arrayOffset() + buffer.position();
                    sl = buffer.arrayOffset() + buffer.limit();
                    buffer.position(buffer.limit());
                } else {
                    src = new byte[buffer.remaining()];
                    buffer.get(src);
                    sp = 0;
                    sl = src.length;
                }
                byte[] dst = new byte[this.decodedOutLength(src, sp, sl)];
                return ByteBuffer.wrap(dst, 0, this.decode0(src, sp, sl, dst));
            }
            catch (IllegalArgumentException iae) {
                buffer.position(pos0);
                throw iae;
            }
        }

        public InputStream wrap(InputStream is) {
            Objects.requireNonNull(is);
            return new DecInputStream(is, this.isURL ? fromBase64URL : fromBase64, this.isMIME);
        }

        private int decodedOutLength(byte[] src, int sp, int sl) {
            int[] base64 = this.isURL ? fromBase64URL : fromBase64;
            int paddings = 0;
            int len = sl - sp;
            if (len == 0) {
                return 0;
            }
            if (len < 2) {
                if (this.isMIME && base64[0] == -1) {
                    return 0;
                }
                throw new IllegalArgumentException("Input byte[] should at least have 2 bytes for base64 bytes");
            }
            if (this.isMIME) {
                int n = 0;
                while (sp < sl) {
                    int b;
                    if ((b = src[sp++] & 0xFF) == 61) {
                        len -= sl - sp + 1;
                        break;
                    }
                    if ((b = base64[b]) != -1) continue;
                    ++n;
                }
                len -= n;
            } else if (src[sl - 1] == 61) {
                ++paddings;
                if (src[sl - 2] == 61) {
                    ++paddings;
                }
            }
            if (paddings == 0 && (len & 3) != 0) {
                paddings = 4 - (len & 3);
            }
            return 3 * (int)(((long)len + 3L) / 4L) - paddings;
        }

        @IntrinsicCandidate
        private int decodeBlock(byte[] src, int sp, int sl, byte[] dst, int dp, boolean isURL, boolean isMIME) {
            int[] base64 = isURL ? fromBase64URL : fromBase64;
            int sl0 = sp + (sl - sp & 0xFFFFFFFC);
            int new_dp = dp;
            while (sp < sl0) {
                int b4;
                int b3;
                int b1 = base64[src[sp++] & 0xFF];
                int b2 = base64[src[sp++] & 0xFF];
                if ((b1 | b2 | (b3 = base64[src[sp++] & 0xFF]) | (b4 = base64[src[sp++] & 0xFF])) < 0) {
                    return new_dp - dp;
                }
                int bits0 = b1 << 18 | b2 << 12 | b3 << 6 | b4;
                dst[new_dp++] = (byte)(bits0 >> 16);
                dst[new_dp++] = (byte)(bits0 >> 8);
                dst[new_dp++] = (byte)bits0;
            }
            return new_dp - dp;
        }

        private int decode0(byte[] src, int sp, int sl, byte[] dst) {
            int[] base64 = this.isURL ? fromBase64URL : fromBase64;
            int dp = 0;
            int bits = 0;
            int shiftto = 18;
            while (sp < sl) {
                if (shiftto == 18 && sp < sl - 4) {
                    int dl = this.decodeBlock(src, sp, sl, dst, dp, this.isURL, this.isMIME);
                    int chars_decoded = (dl + 2) / 3 * 4;
                    sp += chars_decoded;
                    dp += dl;
                }
                if (sp >= sl) break;
                int b = src[sp++] & 0xFF;
                if ((b = base64[b]) < 0) {
                    if (b == -2) {
                        if ((shiftto != 6 || sp != sl && src[sp++] == 61) && shiftto != 18) break;
                        throw new IllegalArgumentException("Input byte array has wrong 4-byte ending unit");
                    }
                    if (this.isMIME) continue;
                    throw new IllegalArgumentException("Illegal base64 character " + Integer.toString(src[sp - 1], 16));
                }
                bits |= b << shiftto;
                if ((shiftto -= 6) >= 0) continue;
                dst[dp++] = (byte)(bits >> 16);
                dst[dp++] = (byte)(bits >> 8);
                dst[dp++] = (byte)bits;
                shiftto = 18;
                bits = 0;
            }
            if (shiftto == 6) {
                dst[dp++] = (byte)(bits >> 16);
            } else if (shiftto == 0) {
                dst[dp++] = (byte)(bits >> 16);
                dst[dp++] = (byte)(bits >> 8);
            } else if (shiftto == 12) {
                throw new IllegalArgumentException("Last unit does not have enough valid bits");
            }
            while (sp < sl) {
                if (this.isMIME && base64[src[sp++] & 0xFF] < 0) continue;
                throw new IllegalArgumentException("Input byte array has incorrect ending byte at " + sp);
            }
            return dp;
        }

        static {
            int i;
            fromBase64 = new int[256];
            Arrays.fill(fromBase64, -1);
            for (i = 0; i < Encoder.toBase64.length; ++i) {
                Decoder.fromBase64[Encoder.toBase64[i]] = i;
            }
            Decoder.fromBase64[61] = -2;
            fromBase64URL = new int[256];
            Arrays.fill(fromBase64URL, -1);
            for (i = 0; i < Encoder.toBase64URL.length; ++i) {
                Decoder.fromBase64URL[Encoder.toBase64URL[i]] = i;
            }
            Decoder.fromBase64URL[61] = -2;
            RFC4648 = new Decoder(false, false);
            RFC4648_URLSAFE = new Decoder(true, false);
            RFC2045 = new Decoder(false, true);
        }
    }

    private static class DecInputStream
    extends InputStream {
        private final InputStream is;
        private final boolean isMIME;
        private final int[] base64;
        private int bits = 0;
        private int wpos = 0;
        private int rpos = 0;
        private boolean eof = false;
        private boolean closed = false;
        private byte[] sbBuf = new byte[1];

        DecInputStream(InputStream is, int[] base64, boolean isMIME) {
            this.is = is;
            this.base64 = base64;
            this.isMIME = isMIME;
        }

        @Override
        public int read() throws IOException {
            return this.read(this.sbBuf, 0, 1) == -1 ? -1 : this.sbBuf[0] & 0xFF;
        }

        private int leftovers(byte[] b, int off, int pos, int limit) {
            this.eof = true;
            while (this.rpos - 8 >= this.wpos && pos != limit) {
                this.rpos -= 8;
                b[pos++] = (byte)(this.bits >> this.rpos);
            }
            return pos - off != 0 || this.rpos - 8 >= this.wpos ? pos - off : -1;
        }

        private int eof(byte[] b, int off, int pos, int limit) throws IOException {
            if (this.wpos == 18) {
                throw new IOException("Base64 stream has one un-decoded dangling byte.");
            }
            this.rpos = 24;
            return this.leftovers(b, off, pos, limit);
        }

        private int padding(byte[] b, int off, int pos, int limit) throws IOException {
            if (this.wpos >= 18 || this.wpos == 12 && this.is.read() != 61) {
                throw new IOException("Illegal base64 ending sequence:" + this.wpos);
            }
            this.rpos = 24;
            return this.leftovers(b, off, pos, limit);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int limit;
            int pos;
            block12: {
                if (this.closed) {
                    throw new IOException("Stream is closed");
                }
                Objects.checkFromIndexSize(off, len, b.length);
                if (len == 0) {
                    return 0;
                }
                pos = off;
                limit = off + len;
                if (this.eof) {
                    return this.leftovers(b, off, pos, limit);
                }
                if (this.rpos == 16) {
                    b[pos++] = (byte)(this.bits >> 8);
                    this.rpos = 8;
                    if (pos == limit) {
                        return len;
                    }
                }
                if (this.rpos == 8) {
                    b[pos++] = (byte)this.bits;
                    this.rpos = 0;
                    if (pos == limit) {
                        return len;
                    }
                }
                this.bits = 0;
                this.wpos = 24;
                while (true) {
                    int i;
                    if ((i = this.is.read()) < 0) {
                        return this.eof(b, off, pos, limit);
                    }
                    int v = this.base64[i];
                    if (v < 0) {
                        if (v == -1) {
                            if (this.isMIME) continue;
                            throw new IOException("Illegal base64 character 0x" + Integer.toHexString(i));
                        }
                        return this.padding(b, off, pos, limit);
                    }
                    this.wpos -= 6;
                    this.bits |= v << this.wpos;
                    if (this.wpos != 0) continue;
                    if (limit - pos < 3) break block12;
                    b[pos++] = (byte)(this.bits >> 16);
                    b[pos++] = (byte)(this.bits >> 8);
                    b[pos++] = (byte)this.bits;
                    this.bits = 0;
                    this.wpos = 24;
                    if (pos == limit) break;
                }
                return len;
            }
            b[pos++] = (byte)(this.bits >> 16);
            if (pos == limit) {
                this.rpos = 16;
                return len;
            }
            b[pos++] = (byte)(this.bits >> 8);
            this.rpos = 8;
            return len;
        }

        @Override
        public int available() throws IOException {
            if (this.closed) {
                throw new IOException("Stream is closed");
            }
            return this.is.available();
        }

        @Override
        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                this.is.close();
            }
        }
    }

    private static class EncOutputStream
    extends FilterOutputStream {
        private int leftover = 0;
        private int b0;
        private int b1;
        private int b2;
        private boolean closed = false;
        private final char[] base64;
        private final byte[] newline;
        private final int linemax;
        private final boolean doPadding;
        private int linepos = 0;
        private byte[] buf;

        EncOutputStream(OutputStream os, char[] base64, byte[] newline, int linemax, boolean doPadding) {
            super(os);
            this.base64 = base64;
            this.newline = newline;
            this.linemax = linemax;
            this.doPadding = doPadding;
            this.buf = new byte[linemax <= 0 ? 8124 : linemax];
        }

        @Override
        public void write(int b) throws IOException {
            byte[] buf = new byte[]{(byte)(b & 0xFF)};
            this.write(buf, 0, 1);
        }

        private void checkNewline() throws IOException {
            if (this.linepos == this.linemax) {
                this.out.write(this.newline);
                this.linepos = 0;
            }
        }

        private void writeb4(char b1, char b2, char b3, char b4) throws IOException {
            this.buf[0] = (byte)b1;
            this.buf[1] = (byte)b2;
            this.buf[2] = (byte)b3;
            this.buf[3] = (byte)b4;
            this.out.write(this.buf, 0, 4);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            int dp;
            int nBits24;
            if (this.closed) {
                throw new IOException("Stream is closed");
            }
            Preconditions.checkFromIndexSize(off, len, b.length, Preconditions.AIOOBE_FORMATTER);
            if (len == 0) {
                return;
            }
            if (this.leftover != 0) {
                if (this.leftover == 1) {
                    this.b1 = b[off++] & 0xFF;
                    if (--len == 0) {
                        ++this.leftover;
                        return;
                    }
                }
                this.b2 = b[off++] & 0xFF;
                --len;
                this.checkNewline();
                this.writeb4(this.base64[this.b0 >> 2], this.base64[this.b0 << 4 & 0x3F | this.b1 >> 4], this.base64[this.b1 << 2 & 0x3F | this.b2 >> 6], this.base64[this.b2 & 0x3F]);
                this.linepos += 4;
            }
            this.leftover = len - nBits24 * 3;
            for (nBits24 = len / 3; nBits24 > 0; nBits24 -= dp / 4) {
                this.checkNewline();
                int dl = this.linemax <= 0 ? this.buf.length : this.buf.length - this.linepos;
                int sl = off + Math.min(nBits24, dl / 4) * 3;
                dp = 0;
                int sp = off;
                while (sp < sl) {
                    int bits = (b[sp++] & 0xFF) << 16 | (b[sp++] & 0xFF) << 8 | b[sp++] & 0xFF;
                    this.buf[dp++] = (byte)this.base64[bits >>> 18 & 0x3F];
                    this.buf[dp++] = (byte)this.base64[bits >>> 12 & 0x3F];
                    this.buf[dp++] = (byte)this.base64[bits >>> 6 & 0x3F];
                    this.buf[dp++] = (byte)this.base64[bits & 0x3F];
                }
                this.out.write(this.buf, 0, dp);
                off = sl;
                this.linepos += dp;
            }
            if (this.leftover == 1) {
                this.b0 = b[off++] & 0xFF;
            } else if (this.leftover == 2) {
                this.b0 = b[off++] & 0xFF;
                this.b1 = b[off++] & 0xFF;
            }
        }

        @Override
        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                if (this.leftover == 1) {
                    this.checkNewline();
                    this.out.write(this.base64[this.b0 >> 2]);
                    this.out.write(this.base64[this.b0 << 4 & 0x3F]);
                    if (this.doPadding) {
                        this.out.write(61);
                        this.out.write(61);
                    }
                } else if (this.leftover == 2) {
                    this.checkNewline();
                    this.out.write(this.base64[this.b0 >> 2]);
                    this.out.write(this.base64[this.b0 << 4 & 0x3F | this.b1 >> 4]);
                    this.out.write(this.base64[this.b1 << 2 & 0x3F]);
                    if (this.doPadding) {
                        this.out.write(61);
                    }
                }
                this.leftover = 0;
                this.out.close();
            }
        }
    }
}

