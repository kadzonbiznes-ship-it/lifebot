/*
 * Decompiled with CFR 0.152.
 */
package sun.security.x509;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import sun.security.action.GetBooleanAction;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.util.Debug;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AVAKeyword;
import sun.security.x509.X500Name;

public class AVA
implements DerEncoder {
    private static final Debug debug = Debug.getInstance("x509", "\t[AVA]");
    private static final boolean PRESERVE_OLD_DC_ENCODING = GetBooleanAction.privilegedGetProperty("com.sun.security.preserveOldDCEncoding");
    static final int DEFAULT = 1;
    static final int RFC1779 = 2;
    static final int RFC2253 = 3;
    final ObjectIdentifier oid;
    final DerValue value;
    private static final String specialChars1779 = ",=\n+<>#;\\\"";
    private static final String specialChars2253 = ",=+<>#;\\\"";
    private static final String specialCharsDefault = ",=\n+<>#;\\\" ";
    private static final String escapedDefault = ",+<>;\"";

    public AVA(ObjectIdentifier type, DerValue val) {
        if (type == null || val == null) {
            throw new NullPointerException();
        }
        this.oid = type;
        this.value = val;
    }

    AVA(Reader in) throws IOException {
        this(in, 1);
    }

    AVA(Reader in, Map<String, String> keywordMap) throws IOException {
        this(in, 1, keywordMap);
    }

    AVA(Reader in, int format) throws IOException {
        this(in, format, Collections.emptyMap());
    }

    AVA(Reader in, int format, Map<String, String> keywordMap) throws IOException {
        int c;
        StringBuilder temp = new StringBuilder();
        while ((c = AVA.readChar(in, "Incorrect AVA format")) != 61) {
            temp.append((char)c);
        }
        this.oid = AVAKeyword.getOID(temp.toString(), format, keywordMap);
        temp.setLength(0);
        if (format == 3) {
            c = in.read();
            if (c == 32) {
                throw new IOException("Incorrect AVA RFC2253 format - leading space must be escaped");
            }
        } else {
            while ((c = in.read()) == 32 || c == 10) {
            }
        }
        if (c == -1) {
            this.value = new DerValue("");
            return;
        }
        this.value = c == 35 ? AVA.parseHexString(in, format) : (c == 34 && format != 3 ? this.parseQuotedString(in, temp) : this.parseString(in, c, format, temp));
    }

    public ObjectIdentifier getObjectIdentifier() {
        return this.oid;
    }

    public DerValue getDerValue() {
        return this.value;
    }

    public String getValueString() {
        try {
            String s = this.value.getAsString();
            if (s == null) {
                throw new RuntimeException("AVA string is null");
            }
            return s;
        }
        catch (IOException e) {
            throw new RuntimeException("AVA error: " + e, e);
        }
    }

    private static DerValue parseHexString(Reader in, int format) throws IOException {
        int c;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b = 0;
        int cNdx = 0;
        while (!AVA.isTerminator(c = in.read(), format)) {
            try {
                int cVal = HexFormat.fromHexDigit(c);
                if (cNdx % 2 == 1) {
                    b = (byte)(b * 16 + (byte)cVal);
                    baos.write(b);
                } else {
                    b = (byte)cVal;
                }
                ++cNdx;
            }
            catch (NumberFormatException nfe) {
                throw new IOException("AVA parse, invalid hex digit: " + (char)c);
            }
        }
        if (cNdx == 0) {
            throw new IOException("AVA parse, zero hex digits");
        }
        if (cNdx % 2 == 1) {
            throw new IOException("AVA parse, odd number of hex digits");
        }
        return new DerValue(baos.toByteArray());
    }

    private DerValue parseQuotedString(Reader in, StringBuilder temp) throws IOException {
        String hexString;
        int c = AVA.readChar(in, "Quoted string did not end in quote");
        ArrayList<Byte> embeddedHex = new ArrayList<Byte>();
        boolean isPrintableString = true;
        while (c != 34) {
            if (c == 92) {
                c = AVA.readChar(in, "Quoted string did not end in quote");
                Byte hexByte = AVA.getEmbeddedHexPair(c, in);
                if (hexByte != null) {
                    isPrintableString = false;
                    embeddedHex.add(hexByte);
                    c = in.read();
                    continue;
                }
                if (specialChars1779.indexOf((char)c) < 0) {
                    throw new IOException("Invalid escaped character in AVA: " + (char)c);
                }
            }
            if (embeddedHex.size() > 0) {
                hexString = AVA.getEmbeddedHexString(embeddedHex);
                temp.append(hexString);
                embeddedHex.clear();
            }
            isPrintableString &= DerValue.isPrintableStringChar((char)c);
            temp.append((char)c);
            c = AVA.readChar(in, "Quoted string did not end in quote");
        }
        if (embeddedHex.size() > 0) {
            hexString = AVA.getEmbeddedHexString(embeddedHex);
            temp.append(hexString);
            embeddedHex.clear();
        }
        while ((c = in.read()) == 10 || c == 32) {
        }
        if (c != -1) {
            throw new IOException("AVA had characters other than whitespace after terminating quote");
        }
        if (this.oid.equals(PKCS9Attribute.EMAIL_ADDRESS_OID) || this.oid.equals(X500Name.DOMAIN_COMPONENT_OID) && !PRESERVE_OLD_DC_ENCODING) {
            return new DerValue(22, temp.toString().trim());
        }
        if (isPrintableString) {
            return new DerValue(temp.toString().trim());
        }
        return new DerValue(12, temp.toString().trim());
    }

    private DerValue parseString(Reader in, int c, int format, StringBuilder temp) throws IOException {
        String hexString;
        ArrayList<Byte> embeddedHex = new ArrayList<Byte>();
        boolean isPrintableString = true;
        boolean leadingChar = true;
        int spaceCount = 0;
        do {
            boolean escape = false;
            if (c == 92) {
                escape = true;
                c = AVA.readChar(in, "Invalid trailing backslash");
                Byte hexByte = AVA.getEmbeddedHexPair(c, in);
                if (hexByte != null) {
                    isPrintableString = false;
                    embeddedHex.add(hexByte);
                    c = in.read();
                    leadingChar = false;
                    continue;
                }
                if (format == 1 && specialCharsDefault.indexOf((char)c) == -1) {
                    throw new IOException("Invalid escaped character in AVA: '" + (char)c + "'");
                }
                if (format == 3) {
                    if (c == 32) {
                        if (!leadingChar && !AVA.trailingSpace(in)) {
                            throw new IOException("Invalid escaped space character in AVA.  Only a leading or trailing space character can be escaped.");
                        }
                    } else if (c == 35) {
                        if (!leadingChar) {
                            throw new IOException("Invalid escaped '#' character in AVA.  Only a leading '#' can be escaped.");
                        }
                    } else if (specialChars2253.indexOf((char)c) == -1) {
                        throw new IOException("Invalid escaped character in AVA: '" + (char)c + "'");
                    }
                }
            } else if (format == 3 ? specialChars2253.indexOf((char)c) != -1 : escapedDefault.indexOf((char)c) != -1) {
                throw new IOException("Character '" + (char)c + "' in AVA appears without escape");
            }
            if (embeddedHex.size() > 0) {
                temp.append(" ".repeat(spaceCount));
                spaceCount = 0;
                hexString = AVA.getEmbeddedHexString(embeddedHex);
                temp.append(hexString);
                embeddedHex.clear();
            }
            isPrintableString &= DerValue.isPrintableStringChar((char)c);
            if (c == 32 && !escape) {
                ++spaceCount;
            } else {
                temp.append(" ".repeat(spaceCount));
                spaceCount = 0;
                temp.append((char)c);
            }
            c = in.read();
            leadingChar = false;
        } while (!AVA.isTerminator(c, format));
        if (format == 3 && spaceCount > 0) {
            throw new IOException("Incorrect AVA RFC2253 format - trailing space must be escaped");
        }
        if (embeddedHex.size() > 0) {
            hexString = AVA.getEmbeddedHexString(embeddedHex);
            temp.append(hexString);
            embeddedHex.clear();
        }
        if (this.oid.equals(PKCS9Attribute.EMAIL_ADDRESS_OID) || this.oid.equals(X500Name.DOMAIN_COMPONENT_OID) && !PRESERVE_OLD_DC_ENCODING) {
            return new DerValue(22, temp.toString());
        }
        if (isPrintableString) {
            return new DerValue(temp.toString());
        }
        return new DerValue(12, temp.toString());
    }

    private static Byte getEmbeddedHexPair(int c1, Reader in) throws IOException {
        if (HexFormat.isHexDigit(c1)) {
            int c2 = AVA.readChar(in, "unexpected EOF - escaped hex value must include two valid digits");
            if (HexFormat.isHexDigit(c2)) {
                int hi = HexFormat.fromHexDigit(c1);
                int lo = HexFormat.fromHexDigit(c2);
                return (byte)((hi << 4) + lo);
            }
            throw new IOException("escaped hex value must include two valid digits");
        }
        return null;
    }

    private static String getEmbeddedHexString(List<Byte> hexList) {
        int n = hexList.size();
        byte[] hexBytes = new byte[n];
        for (int i = 0; i < n; ++i) {
            hexBytes[i] = hexList.get(i);
        }
        return new String(hexBytes, StandardCharsets.UTF_8);
    }

    private static boolean isTerminator(int ch, int format) {
        switch (ch) {
            case -1: 
            case 43: 
            case 44: {
                return true;
            }
            case 59: {
                return format != 3;
            }
        }
        return false;
    }

    private static int readChar(Reader in, String errMsg) throws IOException {
        int c = in.read();
        if (c == -1) {
            throw new IOException(errMsg);
        }
        return c;
    }

    private static boolean trailingSpace(Reader in) throws IOException {
        boolean trailing;
        block3: {
            block4: {
                if (!in.markSupported()) {
                    return true;
                }
                in.mark(9999);
                while (true) {
                    int nextChar;
                    if ((nextChar = in.read()) == -1) {
                        trailing = true;
                        break block3;
                    }
                    if (nextChar == 32) continue;
                    if (nextChar != 92) break block4;
                    int followingChar = in.read();
                    if (followingChar != 32) break;
                }
                trailing = false;
                break block3;
            }
            trailing = false;
        }
        in.reset();
        return trailing;
    }

    AVA(DerValue derval) throws IOException {
        if (derval.tag != 48) {
            throw new IOException("AVA not a sequence");
        }
        this.oid = derval.data.getOID();
        this.value = derval.data.getDerValue();
        if (derval.data.available() != 0) {
            throw new IOException("AVA, extra bytes = " + derval.data.available());
        }
        if (this.value.tag == 30) {
            this.value.validateBMPString();
        }
    }

    AVA(DerInputStream in) throws IOException {
        this(in.getDerValue());
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AVA)) {
            return false;
        }
        AVA other = (AVA)obj;
        return this.toRFC2253CanonicalString().equals(other.toRFC2253CanonicalString());
    }

    public int hashCode() {
        return this.toRFC2253CanonicalString().hashCode();
    }

    @Override
    public void encode(DerOutputStream out) {
        DerOutputStream tmp = new DerOutputStream();
        tmp.putOID(this.oid);
        this.value.encode(tmp);
        out.write((byte)48, tmp);
    }

    private String toKeyword(int format, Map<String, String> oidMap) {
        return AVAKeyword.getKeyword(this.oid, format, oidMap);
    }

    public String toString() {
        return this.toKeywordValueString(this.toKeyword(1, Collections.emptyMap()));
    }

    public String toRFC1779String() {
        return this.toRFC1779String(Collections.emptyMap());
    }

    public String toRFC1779String(Map<String, String> oidMap) {
        return this.toKeywordValueString(this.toKeyword(2, oidMap));
    }

    public String toRFC2253String() {
        return this.toRFC2253String(Collections.emptyMap());
    }

    public String toRFC2253String(Map<String, String> oidMap) {
        StringBuilder typeAndValue = new StringBuilder(100);
        typeAndValue.append(this.toKeyword(3, oidMap));
        typeAndValue.append('=');
        if (typeAndValue.charAt(0) >= '0' && typeAndValue.charAt(0) <= '9' || !AVA.isDerString(this.value, false)) {
            byte[] data = this.value.toByteArray();
            typeAndValue.append('#');
            HexFormat.of().formatHex(typeAndValue, data);
        } else {
            int trail;
            int lead;
            String valStr = new String(this.value.getDataBytes(), AVA.getCharset(this.value, false));
            String escapees = ",=+<>#;\"\\";
            StringBuilder sbuffer = new StringBuilder();
            for (int i = 0; i < valStr.length(); ++i) {
                char c = valStr.charAt(i);
                if (DerValue.isPrintableStringChar(c) || ",=+<>#;\"\\".indexOf(c) >= 0) {
                    if (",=+<>#;\"\\".indexOf(c) >= 0) {
                        sbuffer.append('\\');
                    }
                    sbuffer.append(c);
                    continue;
                }
                if (c == '\u0000') {
                    sbuffer.append("\\00");
                    continue;
                }
                if (debug != null && Debug.isOn("ava")) {
                    byte[] valueBytes = Character.toString(c).getBytes(StandardCharsets.UTF_8);
                    HexFormat.of().withPrefix("\\").withUpperCase().formatHex(sbuffer, valueBytes);
                    continue;
                }
                sbuffer.append(c);
            }
            char[] chars = sbuffer.toString().toCharArray();
            sbuffer = new StringBuilder();
            for (lead = 0; lead < chars.length && (chars[lead] == ' ' || chars[lead] == '\r'); ++lead) {
            }
            for (trail = chars.length - 1; trail >= 0 && (chars[trail] == ' ' || chars[trail] == '\r'); --trail) {
            }
            for (int i = 0; i < chars.length; ++i) {
                char c = chars[i];
                if (i < lead || i > trail) {
                    sbuffer.append('\\');
                }
                sbuffer.append(c);
            }
            typeAndValue.append((CharSequence)sbuffer);
        }
        return typeAndValue.toString();
    }

    public String toRFC2253CanonicalString() {
        StringBuilder typeAndValue = new StringBuilder(40);
        typeAndValue.append(this.toKeyword(3, Collections.emptyMap()));
        typeAndValue.append('=');
        if (typeAndValue.charAt(0) >= '0' && typeAndValue.charAt(0) <= '9' || !AVA.isDerString(this.value, true)) {
            byte[] data = this.value.toByteArray();
            typeAndValue.append('#');
            HexFormat.of().formatHex(typeAndValue, data);
        } else {
            String valStr = new String(this.value.getDataBytes(), AVA.getCharset(this.value, true));
            String escapees = ",+<>;\"\\";
            StringBuilder sbuffer = new StringBuilder();
            boolean previousWhite = false;
            for (int i = 0; i < valStr.length(); ++i) {
                char c = valStr.charAt(i);
                if (DerValue.isPrintableStringChar(c) || ",+<>;\"\\".indexOf(c) >= 0 || i == 0 && c == '#') {
                    if (i == 0 && c == '#' || ",+<>;\"\\".indexOf(c) >= 0) {
                        sbuffer.append('\\');
                    }
                    if (!Character.isWhitespace(c)) {
                        previousWhite = false;
                        sbuffer.append(c);
                        continue;
                    }
                    if (previousWhite) continue;
                    previousWhite = true;
                    sbuffer.append(c);
                    continue;
                }
                if (debug != null && Debug.isOn("ava")) {
                    previousWhite = false;
                    byte[] valueBytes = Character.toString(c).getBytes(StandardCharsets.UTF_8);
                    HexFormat.of().withPrefix("\\").withUpperCase().formatHex(sbuffer, valueBytes);
                    continue;
                }
                previousWhite = false;
                sbuffer.append(c);
            }
            typeAndValue.append(sbuffer.toString().trim());
        }
        String canon = typeAndValue.toString();
        canon = canon.toUpperCase(Locale.US).toLowerCase(Locale.US);
        return Normalizer.normalize(canon, Normalizer.Form.NFKD);
    }

    private static boolean isDerString(DerValue value, boolean canonical) {
        if (canonical) {
            switch (value.tag) {
                case 12: 
                case 19: {
                    return true;
                }
            }
            return false;
        }
        switch (value.tag) {
            case 12: 
            case 19: 
            case 20: 
            case 22: 
            case 27: 
            case 30: {
                return true;
            }
        }
        return false;
    }

    private static Charset getCharset(DerValue value, boolean canonical) {
        if (canonical) {
            return switch (value.tag) {
                case 19 -> StandardCharsets.ISO_8859_1;
                case 12 -> StandardCharsets.UTF_8;
                default -> throw new Error("unexpected tag: " + value.tag);
            };
        }
        return switch (value.tag) {
            case 19, 20, 22, 27 -> StandardCharsets.ISO_8859_1;
            case 30 -> StandardCharsets.UTF_16BE;
            case 12 -> StandardCharsets.UTF_8;
            default -> throw new Error("unexpected tag: " + value.tag);
        };
    }

    boolean hasRFC2253Keyword() {
        return AVAKeyword.hasKeyword(this.oid, 3);
    }

    private String toKeywordValueString(String keyword) {
        StringBuilder retval = new StringBuilder(40);
        retval.append(keyword);
        retval.append('=');
        try {
            String valStr = this.value.getAsString();
            if (valStr == null) {
                byte[] data = this.value.toByteArray();
                retval.append('#');
                HexFormat.of().formatHex(retval, data);
            } else {
                char trailChar;
                boolean quoteNeeded = false;
                StringBuilder sbuffer = new StringBuilder();
                boolean previousWhite = false;
                String escapees = ",+=\n<>#;\\\"";
                int length = valStr.length();
                boolean alreadyQuoted = length > 1 && valStr.charAt(0) == '\"' && valStr.charAt(length - 1) == '\"';
                for (int i = 0; i < length; ++i) {
                    char c = valStr.charAt(i);
                    if (alreadyQuoted && (i == 0 || i == length - 1)) {
                        sbuffer.append(c);
                        continue;
                    }
                    if (DerValue.isPrintableStringChar(c) || ",+=\n<>#;\\\"".indexOf(c) >= 0) {
                        if (!(quoteNeeded || (i != 0 || c != ' ' && c != '\n') && ",+=\n<>#;\\\"".indexOf(c) < 0)) {
                            quoteNeeded = true;
                        }
                        if (c != ' ' && c != '\n') {
                            if (c == '\"' || c == '\\') {
                                sbuffer.append('\\');
                            }
                            previousWhite = false;
                        } else {
                            if (!quoteNeeded && previousWhite) {
                                quoteNeeded = true;
                            }
                            previousWhite = true;
                        }
                        sbuffer.append(c);
                        continue;
                    }
                    if (debug != null && Debug.isOn("ava")) {
                        previousWhite = false;
                        byte[] valueBytes = Character.toString(c).getBytes(StandardCharsets.UTF_8);
                        HexFormat.of().withPrefix("\\").withUpperCase().formatHex(sbuffer, valueBytes);
                        continue;
                    }
                    previousWhite = false;
                    sbuffer.append(c);
                }
                if (sbuffer.length() > 0 && ((trailChar = sbuffer.charAt(sbuffer.length() - 1)) == ' ' || trailChar == '\n')) {
                    quoteNeeded = true;
                }
                if (!alreadyQuoted && quoteNeeded) {
                    retval.append('\"').append((CharSequence)sbuffer).append('\"');
                } else {
                    retval.append((CharSequence)sbuffer);
                }
            }
        }
        catch (IOException e) {
            throw new IllegalArgumentException("DER Value conversion");
        }
        return retval.toString();
    }
}

