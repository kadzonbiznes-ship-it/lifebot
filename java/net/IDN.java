/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.ParseException;
import jdk.internal.icu.impl.Punycode;
import jdk.internal.icu.text.StringPrep;
import jdk.internal.icu.text.UCharacterIterator;

public final class IDN {
    public static final int ALLOW_UNASSIGNED = 1;
    public static final int USE_STD3_ASCII_RULES = 2;
    private static final String ACE_PREFIX = "xn--";
    private static final int ACE_PREFIX_LENGTH;
    private static final int MAX_LABEL_LENGTH = 63;
    private static StringPrep namePrep;

    public static String toASCII(String input, int flag) {
        int p = 0;
        int q = 0;
        StringBuilder out = new StringBuilder();
        if (IDN.isRootLabel(input)) {
            return ".";
        }
        while (p < input.length()) {
            q = IDN.searchDots(input, p);
            out.append(IDN.toASCIIInternal(input.substring(p, q), flag));
            if (q != input.length()) {
                out.append('.');
            }
            p = q + 1;
        }
        return out.toString();
    }

    public static String toASCII(String input) {
        return IDN.toASCII(input, 0);
    }

    public static String toUnicode(String input, int flag) {
        int p = 0;
        int q = 0;
        StringBuilder out = new StringBuilder();
        if (IDN.isRootLabel(input)) {
            return ".";
        }
        while (p < input.length()) {
            q = IDN.searchDots(input, p);
            out.append(IDN.toUnicodeInternal(input.substring(p, q), flag));
            if (q != input.length()) {
                out.append('.');
            }
            p = q + 1;
        }
        return out.toString();
    }

    public static String toUnicode(String input) {
        return IDN.toUnicode(input, 0);
    }

    private IDN() {
    }

    private static String toASCIIInternal(String label, int flag) {
        boolean useSTD3ASCIIRules;
        StringBuffer dest;
        boolean isASCII = IDN.isAllASCII(label);
        if (!isASCII) {
            UCharacterIterator iter = UCharacterIterator.getInstance(label);
            try {
                dest = namePrep.prepare(iter, flag);
            }
            catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            dest = new StringBuffer(label);
        }
        if (dest.length() == 0) {
            throw new IllegalArgumentException("Empty label is not a legal name");
        }
        boolean bl = useSTD3ASCIIRules = (flag & 2) != 0;
        if (useSTD3ASCIIRules) {
            for (int i = 0; i < dest.length(); ++i) {
                char c = dest.charAt(i);
                if (!IDN.isNonLDHAsciiCodePoint(c)) continue;
                throw new IllegalArgumentException("Contains non-LDH ASCII characters");
            }
            if (dest.charAt(0) == '-' || dest.charAt(dest.length() - 1) == '-') {
                throw new IllegalArgumentException("Has leading or trailing hyphen");
            }
        }
        if (!isASCII && !IDN.isAllASCII(dest.toString())) {
            if (!IDN.startsWithACEPrefix(dest)) {
                try {
                    dest = Punycode.encode(dest, null);
                }
                catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
                dest = IDN.toASCIILower(dest);
                dest.insert(0, ACE_PREFIX);
            } else {
                throw new IllegalArgumentException("The input starts with the ACE Prefix");
            }
        }
        if (dest.length() > 63) {
            throw new IllegalArgumentException("The label in the input is too long");
        }
        return dest.toString();
    }

    private static String toUnicodeInternal(String label, int flag) {
        StringBuffer dest;
        Object caseFlags = null;
        boolean isASCII = IDN.isAllASCII(label);
        if (!isASCII) {
            try {
                UCharacterIterator iter = UCharacterIterator.getInstance(label);
                dest = namePrep.prepare(iter, flag);
            }
            catch (Exception e) {
                return label;
            }
        } else {
            dest = new StringBuffer(label);
        }
        if (IDN.startsWithACEPrefix(dest)) {
            String temp = dest.substring(ACE_PREFIX_LENGTH, dest.length());
            try {
                StringBuffer decodeOut = Punycode.decode(new StringBuffer(temp), null);
                String toASCIIOut = IDN.toASCII(decodeOut.toString(), flag);
                if (toASCIIOut.equalsIgnoreCase(dest.toString())) {
                    return decodeOut.toString();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return label;
    }

    private static boolean isNonLDHAsciiCodePoint(int ch) {
        return 0 <= ch && ch <= 44 || 46 <= ch && ch <= 47 || 58 <= ch && ch <= 64 || 91 <= ch && ch <= 96 || 123 <= ch && ch <= 127;
    }

    private static int searchDots(String s, int start) {
        int i;
        for (i = start; i < s.length() && !IDN.isLabelSeparator(s.charAt(i)); ++i) {
        }
        return i;
    }

    private static boolean isRootLabel(String s) {
        return s.length() == 1 && IDN.isLabelSeparator(s.charAt(0));
    }

    private static boolean isLabelSeparator(char c) {
        return c == '.' || c == '\u3002' || c == '\uff0e' || c == '\uff61';
    }

    private static boolean isAllASCII(String input) {
        boolean isASCII = true;
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            if (c <= '\u007f') continue;
            isASCII = false;
            break;
        }
        return isASCII;
    }

    private static boolean startsWithACEPrefix(StringBuffer input) {
        boolean startsWithPrefix = true;
        if (input.length() < ACE_PREFIX_LENGTH) {
            return false;
        }
        for (int i = 0; i < ACE_PREFIX_LENGTH; ++i) {
            if (IDN.toASCIILower(input.charAt(i)) == ACE_PREFIX.charAt(i)) continue;
            startsWithPrefix = false;
        }
        return startsWithPrefix;
    }

    private static char toASCIILower(char ch) {
        if ('A' <= ch && ch <= 'Z') {
            return (char)(ch + 97 - 65);
        }
        return ch;
    }

    private static StringBuffer toASCIILower(StringBuffer input) {
        StringBuffer dest = new StringBuffer();
        for (int i = 0; i < input.length(); ++i) {
            dest.append(IDN.toASCIILower(input.charAt(i)));
        }
        return dest;
    }

    static {
        block2: {
            ACE_PREFIX_LENGTH = ACE_PREFIX.length();
            namePrep = null;
            try {
                String IDN_PROFILE = "/sun/net/idn/uidna.spp";
                InputStream stream = System.getSecurityManager() != null ? AccessController.doPrivileged(new PrivilegedAction<InputStream>(){

                    @Override
                    public InputStream run() {
                        return StringPrep.class.getResourceAsStream("/sun/net/idn/uidna.spp");
                    }
                }) : StringPrep.class.getResourceAsStream("/sun/net/idn/uidna.spp");
                namePrep = new StringPrep(stream);
                stream.close();
            }
            catch (IOException e) {
                if ($assertionsDisabled) break block2;
                throw new AssertionError();
            }
        }
    }
}

