/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

public class Symbol
extends Charset {
    public Symbol() {
        super("Symbol", null);
    }

    @Override
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }

    @Override
    public CharsetDecoder newDecoder() {
        throw new Error("Decoder is not implemented for Symbol Charset");
    }

    @Override
    public boolean contains(Charset cs) {
        return cs instanceof Symbol;
    }

    private static class Encoder
    extends CharsetEncoder {
        private static byte[] table_math = new byte[]{34, 0, 100, 36, 0, -58, 68, -47, -50, -49, 0, 0, 0, 39, 0, 80, 0, -27, 45, 0, 0, -92, 0, 42, -80, -73, -42, 0, 0, -75, -91, 0, 0, 0, 0, -67, 0, 0, 0, -39, -38, -57, -56, -14, 0, 0, 0, 0, 0, 0, 0, 0, 92, 0, 0, 0, 0, 0, 0, 0, 126, 0, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, -69, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -71, -70, 0, 0, -93, -77, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -52, -55, -53, 0, -51, -54, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -59, 0, -60, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 94, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -32, -41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -68};
        private static byte[] table_greek = new byte[]{65, 66, 71, 68, 69, 90, 72, 81, 73, 75, 76, 77, 78, 88, 79, 80, 82, 0, 83, 84, 85, 70, 67, 89, 87, 0, 0, 0, 0, 0, 0, 0, 97, 98, 103, 100, 101, 122, 104, 113, 105, 107, 108, 109, 110, 120, 111, 112, 114, 86, 115, 116, 117, 102, 99, 121, 119, 0, 0, 0, 0, 0, 0, 0, 74, -95, 0, 0, 106, 118};

        public Encoder(Charset cs) {
            super(cs, 1.0f, 1.0f);
        }

        @Override
        public boolean canEncode(char c) {
            return c >= '\u2200' && c <= '\u22ef' ? table_math[c - 8704] != 0 : c >= '\u0391' && c <= '\u03d6' && table_greek[c - 913] != 0;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
            char[] sa = src.array();
            int sp = src.arrayOffset() + src.position();
            int sl = src.arrayOffset() + src.limit();
            assert (sp <= sl);
            byte[] da = dst.array();
            int dp = dst.arrayOffset() + dst.position();
            int dl = dst.arrayOffset() + dst.limit();
            assert (dp <= dl);
            dp = dp <= dl ? dp : dl;
            try {
                for (sp = sp <= sl ? sp : sl; sp < sl; ++sp) {
                    char c = sa[sp];
                    if (dl - dp < 1) {
                        CoderResult coderResult = CoderResult.OVERFLOW;
                        return coderResult;
                    }
                    if (!this.canEncode(c)) {
                        CoderResult coderResult = CoderResult.unmappableForLength(1);
                        return coderResult;
                    }
                    if (c < '\u2200' || c > '\u22ef') continue;
                    da[dp++] = table_math[c - 8704];
                }
                CoderResult coderResult = CoderResult.UNDERFLOW;
                return coderResult;
            }
            finally {
                src.position(sp - src.arrayOffset());
                dst.position(dp - dst.arrayOffset());
            }
        }

        @Override
        public boolean isLegalReplacement(byte[] repl) {
            return true;
        }
    }
}

