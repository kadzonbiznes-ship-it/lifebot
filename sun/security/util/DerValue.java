/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import jdk.internal.util.ArraysSupport;
import sun.nio.cs.UTF_32BE;
import sun.security.util.BitArray;
import sun.security.util.DerIndefLenConverter;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.IOUtils;
import sun.security.util.ObjectIdentifier;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.CalendarSystem;
import sun.util.calendar.Gregorian;

public class DerValue {
    public static final byte TAG_UNIVERSAL = 0;
    public static final byte TAG_APPLICATION = 64;
    public static final byte TAG_CONTEXT = -128;
    public static final byte TAG_PRIVATE = -64;
    public static final byte tag_Boolean = 1;
    public static final byte tag_Integer = 2;
    public static final byte tag_BitString = 3;
    public static final byte tag_OctetString = 4;
    public static final byte tag_Null = 5;
    public static final byte tag_ObjectId = 6;
    public static final byte tag_Enumerated = 10;
    public static final byte tag_UTF8String = 12;
    public static final byte tag_PrintableString = 19;
    public static final byte tag_T61String = 20;
    public static final byte tag_IA5String = 22;
    public static final byte tag_UtcTime = 23;
    public static final byte tag_GeneralizedTime = 24;
    public static final byte tag_GeneralString = 27;
    public static final byte tag_UniversalString = 28;
    public static final byte tag_BMPString = 30;
    public static final byte tag_Sequence = 48;
    public static final byte tag_SequenceOf = 48;
    public static final byte tag_Set = 49;
    public static final byte tag_SetOf = 49;
    public byte tag;
    final byte[] buffer;
    private final int start;
    final int end;
    private final boolean allowBER;
    public final DerInputStream data;

    public boolean isUniversal() {
        return (this.tag & 0xC0) == 0;
    }

    public boolean isApplication() {
        return (this.tag & 0xC0) == 64;
    }

    public boolean isContextSpecific() {
        return (this.tag & 0xC0) == 128;
    }

    public boolean isContextSpecific(byte cntxtTag) {
        if (!this.isContextSpecific()) {
            return false;
        }
        return (this.tag & 0x1F) == cntxtTag;
    }

    boolean isPrivate() {
        return (this.tag & 0xC0) == 192;
    }

    public boolean isConstructed() {
        return (this.tag & 0x20) == 32;
    }

    public boolean isConstructed(byte constructedTag) {
        if (!this.isConstructed()) {
            return false;
        }
        return (this.tag & 0x1F) == constructedTag;
    }

    DerValue(byte tag, byte[] buffer, int start, int end, boolean allowBER) {
        if ((tag & 0x1F) == 31) {
            throw new IllegalArgumentException("Tag number over 30 is not supported");
        }
        this.tag = tag;
        this.buffer = buffer;
        this.start = start;
        this.end = end;
        this.allowBER = allowBER;
        this.data = this.data();
    }

    public DerValue(String value) {
        this(DerValue.isPrintableString(value) ? (byte)19 : 12, value);
    }

    private static boolean isPrintableString(String value) {
        for (int i = 0; i < value.length(); ++i) {
            if (DerValue.isPrintableStringChar(value.charAt(i))) continue;
            return false;
        }
        return true;
    }

    public DerValue(byte stringTag, String value) {
        this(stringTag, DerValue.string2bytes(stringTag, value), false);
    }

    private static byte[] string2bytes(byte stringTag, String value) {
        Charset charset = switch (stringTag) {
            case 19, 22, 27 -> StandardCharsets.US_ASCII;
            case 20 -> StandardCharsets.ISO_8859_1;
            case 30 -> StandardCharsets.UTF_16BE;
            case 12 -> StandardCharsets.UTF_8;
            case 28 -> Charset.forName("UTF_32BE");
            default -> throw new IllegalArgumentException("Unsupported DER string type");
        };
        return value.getBytes(charset);
    }

    DerValue(byte tag, byte[] buffer, boolean allowBER) {
        this(tag, buffer, 0, buffer.length, allowBER);
    }

    public DerValue(byte tag, byte[] buffer) {
        this(tag, (byte[])buffer.clone(), true);
    }

    public static DerValue wrap(byte tag, DerOutputStream out) {
        return new DerValue(tag, out.buf(), 0, out.size(), false);
    }

    public static DerValue wrap(byte[] buf) throws IOException {
        return DerValue.wrap(buf, 0, buf.length);
    }

    public static DerValue wrap(byte[] buf, int offset, int len) throws IOException {
        return new DerValue(buf, offset, len, true, false);
    }

    public DerValue(byte[] encoding) throws IOException {
        this((byte[])encoding.clone(), 0, encoding.length, true, false);
    }

    DerValue(byte[] buf, int offset, int len, boolean allowBER, boolean allowMore) throws IOException {
        int length;
        int lenByte;
        if (len < 2) {
            throw new IOException("Too short");
        }
        int pos = offset;
        this.tag = buf[pos++];
        if ((this.tag & 0x1F) == 31) {
            throw new IOException("Tag number over 30 at " + offset + " is not supported");
        }
        if ((lenByte = buf[pos++]) == -128) {
            if (!allowBER) {
                throw new IOException("Indefinite length encoding not supported with DER");
            }
            if (!this.isConstructed()) {
                throw new IOException("Indefinite length encoding not supported with non-constructed data");
            }
            buf = DerIndefLenConverter.convertStream(new ByteArrayInputStream(buf, pos, len - (pos - offset)), this.tag);
            offset = 0;
            len = buf.length;
            pos = 2;
            if (this.tag != buf[0]) {
                throw new IOException("Indefinite length encoding not supported");
            }
            lenByte = buf[1];
            if (lenByte == -128) {
                throw new IOException("Indefinite len conversion failed");
            }
        }
        if ((lenByte & 0x80) == 0) {
            length = lenByte;
        } else {
            if ((lenByte &= 0x7F) > 4) {
                throw new IOException("Invalid lenByte");
            }
            if (len < 2 + lenByte) {
                throw new IOException("Not enough length bytes");
            }
            length = 0xFF & buf[pos++];
            --lenByte;
            if (length == 0 && !allowBER) {
                throw new IOException("Redundant length bytes found");
            }
            while (lenByte-- > 0) {
                length <<= 8;
                length += 0xFF & buf[pos++];
            }
            if (length < 0) {
                throw new IOException("Invalid length bytes");
            }
            if (length <= 127 && !allowBER) {
                throw new IOException("Should use short form for length");
            }
        }
        if (len - length < pos - offset) {
            throw new EOFException("not enough content");
        }
        if (len - length > pos - offset && !allowMore) {
            throw new IOException("extra data at the end");
        }
        this.buffer = buf;
        this.start = pos;
        this.end = pos + length;
        this.allowBER = allowBER;
        this.data = this.data();
    }

    DerValue(InputStream in, boolean allowBER) throws IOException {
        this.tag = (byte)in.read();
        if ((this.tag & 0x1F) == 31) {
            throw new IOException("Tag number over 30 is not supported");
        }
        int length = DerInputStream.getLength(in);
        if (length == -1) {
            if (!allowBER) {
                throw new IOException("Indefinite length encoding not supported with DER");
            }
            if (!this.isConstructed()) {
                throw new IOException("Indefinite length encoding not supported with non-constructed data");
            }
            this.buffer = DerIndefLenConverter.convertStream(in, this.tag);
            ByteArrayInputStream bin = new ByteArrayInputStream(this.buffer);
            if (this.tag != bin.read()) {
                throw new IOException("Indefinite length encoding not supported");
            }
            length = DerInputStream.getDefiniteLength(bin);
            this.start = this.buffer.length - bin.available();
            this.end = this.start + length;
        } else {
            this.buffer = IOUtils.readExactlyNBytes(in, length);
            this.start = 0;
            this.end = length;
        }
        this.allowBER = allowBER;
        this.data = this.data();
    }

    public DerValue(InputStream in) throws IOException {
        this(in, true);
    }

    public void encode(DerOutputStream out) {
        out.write(this.tag);
        out.putLength(this.end - this.start);
        out.write(this.buffer, this.start, this.end - this.start);
        this.data.pos = this.data.end;
    }

    public final DerInputStream data() {
        return new DerInputStream(this.buffer, this.start, this.end - this.start, this.allowBER);
    }

    public final DerInputStream getData() {
        return this.data;
    }

    public final byte getTag() {
        return this.tag;
    }

    public boolean getBoolean() throws IOException {
        if (this.tag != 1) {
            throw new IOException("DerValue.getBoolean, not a BOOLEAN " + this.tag);
        }
        if (this.end - this.start != 1) {
            throw new IOException("DerValue.getBoolean, invalid length " + (this.end - this.start));
        }
        this.data.pos = this.data.end;
        return this.buffer[this.start] != 0;
    }

    public ObjectIdentifier getOID() throws IOException {
        if (this.tag != 6) {
            throw new IOException("DerValue.getOID, not an OID " + this.tag);
        }
        this.data.pos = this.data.end;
        return new ObjectIdentifier(Arrays.copyOfRange(this.buffer, this.start, this.end));
    }

    public byte[] getOctetString() throws IOException {
        if (this.tag != 4 && !this.isConstructed((byte)4)) {
            throw new IOException("DerValue.getOctetString, not an Octet String: " + this.tag);
        }
        if (this.end - this.start == 0) {
            return new byte[0];
        }
        this.data.pos = this.data.end;
        if (!this.isConstructed()) {
            return Arrays.copyOfRange(this.buffer, this.start, this.end);
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DerInputStream dis = this.data();
        while (dis.available() > 0) {
            bout.write(dis.getDerValue().getOctetString());
        }
        return bout.toByteArray();
    }

    public int getInteger() throws IOException {
        return this.getIntegerInternal((byte)2);
    }

    private int getIntegerInternal(byte expectedTag) throws IOException {
        BigInteger result = this.getBigIntegerInternal(expectedTag, false);
        if (result.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0) {
            throw new IOException("Integer below minimum valid value");
        }
        if (result.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            throw new IOException("Integer exceeds maximum valid value");
        }
        return result.intValue();
    }

    public BigInteger getBigInteger() throws IOException {
        return this.getBigIntegerInternal((byte)2, false);
    }

    public BigInteger getPositiveBigInteger() throws IOException {
        return this.getBigIntegerInternal((byte)2, true);
    }

    private BigInteger getBigIntegerInternal(byte expectedTag, boolean makePositive) throws IOException {
        if (this.tag != expectedTag) {
            throw new IOException("DerValue.getBigIntegerInternal, not expected " + this.tag);
        }
        if (this.end == this.start) {
            throw new IOException("Invalid encoding: zero length Int value");
        }
        this.data.pos = this.data.end;
        if (!this.allowBER && this.end - this.start >= 2 && this.buffer[this.start] == 0 && this.buffer[this.start + 1] >= 0) {
            throw new IOException("Invalid encoding: redundant leading 0s");
        }
        return makePositive ? new BigInteger(1, this.buffer, this.start, this.end - this.start) : new BigInteger(this.buffer, this.start, this.end - this.start);
    }

    public int getEnumerated() throws IOException {
        return this.getIntegerInternal((byte)10);
    }

    public byte[] getBitString() throws IOException {
        return this.getBitString(false);
    }

    public BitArray getUnalignedBitString() throws IOException {
        return this.getUnalignedBitString(false);
    }

    public String getAsString() throws IOException {
        return switch (this.tag) {
            case 12 -> this.getUTF8String();
            case 19 -> this.getPrintableString();
            case 20 -> this.getT61String();
            case 22 -> this.getIA5String();
            case 28 -> this.getUniversalString();
            case 30 -> this.getBMPString();
            case 27 -> this.getGeneralString();
            default -> null;
        };
    }

    private static int checkPaddedBits(int numOfPadBits, byte[] data, int start, int end, boolean allowBER) throws IOException {
        if (numOfPadBits < 0 || numOfPadBits > 7) {
            throw new IOException("Invalid number of padding bits");
        }
        int lenInBits = (end - start << 3) - numOfPadBits;
        if (lenInBits < 0) {
            throw new IOException("Not enough bytes in BitString");
        }
        if (!allowBER && numOfPadBits != 0 && (data[end - 1] & 255 >>> 8 - numOfPadBits) != 0) {
            throw new IOException("Invalid value of padding bits");
        }
        return lenInBits;
    }

    public byte[] getBitString(boolean tagImplicit) throws IOException {
        if (!tagImplicit && this.tag != 3) {
            throw new IOException("DerValue.getBitString, not a bit string " + this.tag);
        }
        if (this.end == this.start) {
            throw new IOException("Invalid encoding: zero length bit string");
        }
        this.data.pos = this.data.end;
        byte numOfPadBits = this.buffer[this.start];
        DerValue.checkPaddedBits(numOfPadBits, this.buffer, this.start + 1, this.end, this.allowBER);
        byte[] retval = Arrays.copyOfRange(this.buffer, this.start + 1, this.end);
        if (this.allowBER && numOfPadBits != 0) {
            int n = retval.length - 1;
            retval[n] = (byte)(retval[n] & (byte)(255 << numOfPadBits));
        }
        return retval;
    }

    public BitArray getUnalignedBitString(boolean tagImplicit) throws IOException {
        if (!tagImplicit && this.tag != 3) {
            throw new IOException("DerValue.getBitString, not a bit string " + this.tag);
        }
        if (this.end == this.start) {
            throw new IOException("Invalid encoding: zero length bit string");
        }
        this.data.pos = this.data.end;
        byte numOfPadBits = this.buffer[this.start];
        int len = DerValue.checkPaddedBits(numOfPadBits, this.buffer, this.start + 1, this.end, this.allowBER);
        return new BitArray(len, this.buffer, this.start + 1);
    }

    public byte[] getDataBytes() {
        this.data.pos = this.data.end;
        return Arrays.copyOfRange(this.buffer, this.start, this.end);
    }

    private String readStringInternal(byte expectedTag, Charset cs) throws IOException {
        if (this.tag != expectedTag) {
            throw new IOException("Incorrect string type " + this.tag + " is not " + expectedTag);
        }
        this.data.pos = this.data.end;
        return new String(this.buffer, this.start, this.end - this.start, cs);
    }

    public String getPrintableString() throws IOException {
        return this.readStringInternal((byte)19, StandardCharsets.US_ASCII);
    }

    public String getT61String() throws IOException {
        return this.readStringInternal((byte)20, StandardCharsets.ISO_8859_1);
    }

    public String getIA5String() throws IOException {
        return this.readStringInternal((byte)22, StandardCharsets.US_ASCII);
    }

    public String getBMPString() throws IOException {
        return this.readStringInternal((byte)30, StandardCharsets.UTF_16BE);
    }

    public String getUTF8String() throws IOException {
        return this.readStringInternal((byte)12, StandardCharsets.UTF_8);
    }

    public String getGeneralString() throws IOException {
        return this.readStringInternal((byte)27, StandardCharsets.US_ASCII);
    }

    public String getUniversalString() throws IOException {
        return this.readStringInternal((byte)28, new UTF_32BE());
    }

    public void validateBMPString() throws IOException {
        String bmpString = this.getBMPString();
        for (int i = 0; i < bmpString.length(); ++i) {
            if (!Character.isSurrogate(bmpString.charAt(i))) continue;
            throw new IOException("Illegal character in BMPString, index: " + i);
        }
    }

    public void getNull() throws IOException {
        if (this.tag != 5) {
            throw new IOException("DerValue.getNull, not NULL: " + this.tag);
        }
        if (this.end != this.start) {
            throw new IOException("NULL should contain no data");
        }
    }

    private Date getTimeInternal(boolean generalized) throws IOException {
        int second;
        int year;
        String type;
        int pos = this.start;
        int len = this.end - this.start;
        if (generalized) {
            type = "Generalized";
            year = 1000 * DerValue.toDigit(this.buffer[pos++], type);
            year += 100 * DerValue.toDigit(this.buffer[pos++], type);
            year += 10 * DerValue.toDigit(this.buffer[pos++], type);
            year += DerValue.toDigit(this.buffer[pos++], type);
            len -= 2;
        } else {
            type = "UTC";
            year = 10 * DerValue.toDigit(this.buffer[pos++], type);
            year = (year += DerValue.toDigit(this.buffer[pos++], type)) < 50 ? (year += 2000) : (year += 1900);
        }
        int month = 10 * DerValue.toDigit(this.buffer[pos++], type);
        month += DerValue.toDigit(this.buffer[pos++], type);
        int day = 10 * DerValue.toDigit(this.buffer[pos++], type);
        day += DerValue.toDigit(this.buffer[pos++], type);
        int hour = 10 * DerValue.toDigit(this.buffer[pos++], type);
        hour += DerValue.toDigit(this.buffer[pos++], type);
        int minute = 10 * DerValue.toDigit(this.buffer[pos++], type);
        minute += DerValue.toDigit(this.buffer[pos++], type);
        int millis = 0;
        if ((len -= 10) > 2) {
            second = 10 * DerValue.toDigit(this.buffer[pos++], type);
            second += DerValue.toDigit(this.buffer[pos++], type);
            len -= 2;
            if (generalized && (this.buffer[pos] == 46 || this.buffer[pos] == 44)) {
                if (--len == 0) {
                    throw new IOException("Parse " + type + " time, empty fractional part");
                }
                ++pos;
                int precision = 0;
                while (this.buffer[pos] != 90 && this.buffer[pos] != 43 && this.buffer[pos] != 45) {
                    int thisDigit = DerValue.toDigit(this.buffer[pos], type);
                    ++precision;
                    if (--len == 0) {
                        throw new IOException("Parse " + type + " time, invalid fractional part");
                    }
                    ++pos;
                    switch (precision) {
                        case 1: {
                            millis += 100 * thisDigit;
                            break;
                        }
                        case 2: {
                            millis += 10 * thisDigit;
                            break;
                        }
                        case 3: {
                            millis += thisDigit;
                        }
                    }
                }
                if (precision == 0) {
                    throw new IOException("Parse " + type + " time, empty fractional part");
                }
            }
        } else {
            second = 0;
        }
        if (month == 0 || day == 0 || month > 12 || day > 31 || hour >= 24 || minute >= 60 || second >= 60) {
            throw new IOException("Parse " + type + " time, invalid format");
        }
        Gregorian gcal = CalendarSystem.getGregorianCalendar();
        CalendarDate date = ((CalendarSystem)gcal).newCalendarDate(null);
        date.setDate(year, month, day);
        date.setTimeOfDay(hour, minute, second, millis);
        long time = ((CalendarSystem)gcal).getTime(date);
        if (len != 1 && len != 5) {
            throw new IOException("Parse " + type + " time, invalid offset");
        }
        switch (this.buffer[pos++]) {
            case 43: {
                if (len != 5) {
                    throw new IOException("Parse " + type + " time, invalid offset");
                }
                int hr = 10 * DerValue.toDigit(this.buffer[pos++], type);
                int n = pos++;
                int min = 10 * DerValue.toDigit(this.buffer[pos++], type);
                if ((hr += DerValue.toDigit(this.buffer[n], type)) >= 24 || (min += DerValue.toDigit(this.buffer[pos++], type)) >= 60) {
                    throw new IOException("Parse " + type + " time, +hhmm");
                }
                time -= ((long)hr * 60L + (long)min) * 60L * 1000L;
                break;
            }
            case 45: {
                if (len != 5) {
                    throw new IOException("Parse " + type + " time, invalid offset");
                }
                int hr = 10 * DerValue.toDigit(this.buffer[pos++], type);
                int n = pos++;
                int min = 10 * DerValue.toDigit(this.buffer[pos++], type);
                if ((hr += DerValue.toDigit(this.buffer[n], type)) >= 24 || (min += DerValue.toDigit(this.buffer[pos++], type)) >= 60) {
                    throw new IOException("Parse " + type + " time, -hhmm");
                }
                time += ((long)hr * 60L + (long)min) * 60L * 1000L;
                break;
            }
            case 90: {
                if (len == 1) break;
                throw new IOException("Parse " + type + " time, invalid format");
            }
            default: {
                throw new IOException("Parse " + type + " time, garbage offset");
            }
        }
        return new Date(time);
    }

    private static int toDigit(byte b, String type) throws IOException {
        if (b < 48 || b > 57) {
            throw new IOException("Parse " + type + " time, invalid format");
        }
        return b - 48;
    }

    public Date getUTCTime() throws IOException {
        if (this.tag != 23) {
            throw new IOException("DerValue.getUTCTime, not a UtcTime: " + this.tag);
        }
        if (this.end - this.start < 11 || this.end - this.start > 17) {
            throw new IOException("DER UTC Time length error");
        }
        this.data.pos = this.data.end;
        return this.getTimeInternal(false);
    }

    public Date getGeneralizedTime() throws IOException {
        if (this.tag != 24) {
            throw new IOException("DerValue.getGeneralizedTime, not a GeneralizedTime: " + this.tag);
        }
        if (this.end - this.start < 13) {
            throw new IOException("DER Generalized Time length error");
        }
        this.data.pos = this.data.end;
        return this.getTimeInternal(true);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DerValue)) {
            return false;
        }
        DerValue other = (DerValue)o;
        if (this.tag != other.tag) {
            return false;
        }
        if (this.buffer == other.buffer && this.start == other.start && this.end == other.end) {
            return true;
        }
        return Arrays.equals(this.buffer, this.start, this.end, other.buffer, other.start, other.end);
    }

    public String toString() {
        return String.format("DerValue(%02x, %s, %d, %d)", 0xFF & this.tag, this.buffer, this.start, this.end);
    }

    public byte[] toByteArray() {
        this.data.pos = this.data.start;
        DerOutputStream out = new DerOutputStream();
        out.write(this.tag);
        out.putLength(this.end - this.start);
        int headLen = out.size();
        byte[] result = Arrays.copyOf(out.buf(), this.end - this.start + headLen);
        System.arraycopy(this.buffer, this.start, result, headLen, this.end - this.start);
        return result;
    }

    public DerInputStream toDerInputStream() throws IOException {
        if (this.tag == 48 || this.tag == 49) {
            return this.data;
        }
        throw new IOException("toDerInputStream rejects tag type " + this.tag);
    }

    public int length() {
        return this.end - this.start;
    }

    public static boolean isPrintableStringChar(char ch) {
        if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9') {
            return true;
        }
        switch (ch) {
            case ' ': 
            case '\'': 
            case '(': 
            case ')': 
            case '+': 
            case ',': 
            case '-': 
            case '.': 
            case '/': 
            case ':': 
            case '=': 
            case '?': {
                return true;
            }
        }
        return false;
    }

    public static byte createTag(byte tagClass, boolean form, byte val) {
        if (val < 0 || val > 30) {
            throw new IllegalArgumentException("Tag number over 30 is not supported");
        }
        byte tag = (byte)(tagClass | val);
        if (form) {
            tag = (byte)(tag | 0x20);
        }
        return tag;
    }

    public void resetTag(byte tag) {
        this.tag = tag;
    }

    public DerValue withTag(byte newTag) {
        return new DerValue(newTag, this.buffer, this.start, this.end, this.allowBER);
    }

    public int hashCode() {
        return ArraysSupport.vectorizedHashCode(this.buffer, this.start, this.end - this.start, this.tag, 8);
    }

    public DerValue[] subs(byte expectedTag, int startLen) throws IOException {
        if (expectedTag != 0 && expectedTag != this.tag) {
            throw new IOException("Not the correct tag");
        }
        ArrayList<DerValue> result = new ArrayList<DerValue>(startLen);
        DerInputStream dis = this.data();
        while (dis.available() > 0) {
            result.add(dis.getDerValue());
        }
        return result.toArray(new DerValue[0]);
    }

    public void clear() {
        Arrays.fill(this.buffer, this.start, this.end, (byte)0);
    }
}

