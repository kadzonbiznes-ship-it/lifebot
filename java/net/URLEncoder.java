/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.io.CharArrayWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.BitSet;
import java.util.Objects;
import jdk.internal.util.StaticProperty;

public class URLEncoder {
    private static final BitSet DONT_NEED_ENCODING = new BitSet(128);
    private static final int CASE_DIFF = 32;
    private static final String DEFAULT_ENCODING_NAME;

    private URLEncoder() {
    }

    @Deprecated
    public static String encode(String s) {
        String str = null;
        try {
            str = URLEncoder.encode(s, DEFAULT_ENCODING_NAME);
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            // empty catch block
        }
        return str;
    }

    public static String encode(String s, String enc) throws UnsupportedEncodingException {
        if (enc == null) {
            throw new NullPointerException("charsetName");
        }
        try {
            Charset charset = Charset.forName(enc);
            return URLEncoder.encode(s, charset);
        }
        catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            throw new UnsupportedEncodingException(enc);
        }
    }

    public static String encode(String s, Charset charset) {
        Objects.requireNonNull(charset, "charset");
        boolean needToChange = false;
        StringBuilder out = new StringBuilder(s.length());
        CharArrayWriter charArrayWriter = new CharArrayWriter();
        int i = 0;
        while (i < s.length()) {
            byte[] ba;
            char c;
            int c2 = s.charAt(i);
            if (DONT_NEED_ENCODING.get(c2)) {
                if (c2 == 32) {
                    c2 = 43;
                    needToChange = true;
                }
                out.append((char)c2);
                ++i;
                continue;
            }
            do {
                char d;
                charArrayWriter.write(c2);
                if (c2 >= 55296 && c2 <= 56319 && i + 1 < s.length() && (d = s.charAt(i + 1)) >= '\udc00' && d <= '\udfff') {
                    charArrayWriter.write(d);
                    ++i;
                }
                if (++i >= s.length()) break;
                c = s.charAt(i);
                c2 = c;
            } while (!DONT_NEED_ENCODING.get(c));
            charArrayWriter.flush();
            String str = charArrayWriter.toString();
            for (byte b : ba = str.getBytes(charset)) {
                out.append('%');
                char ch = Character.forDigit(b >> 4 & 0xF, 16);
                if (Character.isLetter(ch)) {
                    ch = (char)(ch - 32);
                }
                out.append(ch);
                ch = Character.forDigit(b & 0xF, 16);
                if (Character.isLetter(ch)) {
                    ch = (char)(ch - 32);
                }
                out.append(ch);
            }
            charArrayWriter.reset();
            needToChange = true;
        }
        return needToChange ? out.toString() : s;
    }

    static {
        DONT_NEED_ENCODING.set(97, 123);
        DONT_NEED_ENCODING.set(65, 91);
        DONT_NEED_ENCODING.set(48, 58);
        DONT_NEED_ENCODING.set(32);
        DONT_NEED_ENCODING.set(45);
        DONT_NEED_ENCODING.set(95);
        DONT_NEED_ENCODING.set(46);
        DONT_NEED_ENCODING.set(42);
        DEFAULT_ENCODING_NAME = StaticProperty.fileEncoding();
    }
}

