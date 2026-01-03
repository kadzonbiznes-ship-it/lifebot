/*
 * Decompiled with CFR 0.152.
 */
package sun.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Objects;
import sun.nio.cs.ISO_8859_1;
import sun.nio.cs.UTF_8;

public class PropertyResourceBundleCharset
extends Charset {
    private boolean strictUTF8 = false;

    public PropertyResourceBundleCharset(boolean strictUTF8) {
        this(PropertyResourceBundleCharset.class.getCanonicalName(), null);
        this.strictUTF8 = strictUTF8;
    }

    public PropertyResourceBundleCharset(String canonicalName, String[] aliases) {
        super(canonicalName, aliases);
    }

    @Override
    public boolean contains(Charset cs) {
        return false;
    }

    @Override
    public CharsetDecoder newDecoder() {
        return new PropertiesFileDecoder(this, 1.0f, 1.0f);
    }

    @Override
    public CharsetEncoder newEncoder() {
        throw new UnsupportedOperationException("Encoding is not supported");
    }

    private final class PropertiesFileDecoder
    extends CharsetDecoder {
        private CharsetDecoder cdUTF_8;
        private CharsetDecoder cdISO_8859_1;

        protected PropertiesFileDecoder(Charset cs, float averageCharsPerByte, float maxCharsPerByte) {
            super(cs, averageCharsPerByte, maxCharsPerByte);
            this.cdUTF_8 = UTF_8.INSTANCE.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
            this.cdISO_8859_1 = null;
        }

        @Override
        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            if (Objects.nonNull(this.cdISO_8859_1)) {
                return this.cdISO_8859_1.decode(in, out, false);
            }
            in.mark();
            out.mark();
            CoderResult cr = this.cdUTF_8.decode(in, out, false);
            if (cr.isUnderflow() || cr.isOverflow() || PropertyResourceBundleCharset.this.strictUTF8) {
                return cr;
            }
            assert (cr.isMalformed() || cr.isUnmappable());
            in.reset();
            out.reset();
            this.cdISO_8859_1 = ISO_8859_1.INSTANCE.newDecoder();
            return this.cdISO_8859_1.decode(in, out, false);
        }
    }
}

