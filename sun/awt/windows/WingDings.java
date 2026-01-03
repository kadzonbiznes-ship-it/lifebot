/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.windows;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

public final class WingDings
extends Charset {
    public WingDings() {
        super("WingDings", null);
    }

    @Override
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }

    @Override
    public CharsetDecoder newDecoder() {
        throw new Error("Decoder isn't implemented for WingDings Charset");
    }

    @Override
    public boolean contains(Charset cs) {
        return cs instanceof WingDings;
    }

    private static class Encoder
    extends CharsetEncoder {
        private static byte[] table = new byte[]{0, 35, 34, 0, 0, 0, 41, 62, 81, 42, 0, 0, 65, 63, 0, 0, 0, 0, 0, -4, 0, 0, 0, -5, 0, 0, 0, 0, 0, 0, 86, 0, 88, 89, 0, 0, 0, 0, 0, 0, 0, 0, -75, 0, 0, 0, 0, 0, -74, 0, 0, 0, -83, -81, -84, 0, 0, 0, 0, 0, 0, 0, 0, 124, 123, 0, 0, 0, 84, 0, 0, 0, 0, 0, 0, 0, 0, -90, 0, 0, 0, 113, 114, 0, 0, 0, 117, 0, 0, 0, 0, 0, 0, 125, 126, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -116, -115, -114, -113, -112, -111, -110, -109, -108, -107, -127, -126, -125, -124, -123, -122, -121, -120, -119, -118, -116, -115, -114, -113, -112, -111, -110, -109, -108, -107, -24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -24, -40, 0, 0, -60, -58, 0, 0, -16, 0, 0, 0, 0, 0, 0, 0, 0, 0, -36, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        public Encoder(Charset cs) {
            super(cs, 1.0f, 1.0f);
        }

        @Override
        public boolean canEncode(char c) {
            if (c >= '\u2701' && c <= '\u27be') {
                return table[c - 9984] != 0;
            }
            return false;
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
                    da[dp++] = table[c - 9984];
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

