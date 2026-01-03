/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.function.Predicate;
import sun.security.util.BitArray;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class DerInputStream {
    final byte[] data;
    final int start;
    final int end;
    final boolean allowBER;
    int pos;
    int mark;

    public DerInputStream(byte[] data, int start, int length, boolean allowBER) {
        this.data = data;
        this.start = start;
        this.end = start + length;
        this.allowBER = allowBER;
        this.pos = start;
        this.mark = start;
    }

    public DerInputStream(byte[] data) throws IOException {
        this(data, 0, data.length, true);
    }

    public DerInputStream(byte[] data, int offset, int len) throws IOException {
        this(data, offset, len, true);
    }

    public byte[] toByteArray() {
        return Arrays.copyOfRange(this.data, this.pos, this.end);
    }

    public DerValue getDerValue() throws IOException {
        DerValue result = new DerValue(this.data, this.pos, this.end - this.pos, this.allowBER, true);
        if (result.buffer != this.data) {
            int unused = result.buffer.length - result.end;
            this.pos = this.data.length - unused;
        } else {
            this.pos = result.end;
        }
        return result;
    }

    public int getInteger() throws IOException {
        return this.getDerValue().getInteger();
    }

    public BigInteger getBigInteger() throws IOException {
        return this.getDerValue().getBigInteger();
    }

    public BigInteger getPositiveBigInteger() throws IOException {
        return this.getDerValue().getPositiveBigInteger();
    }

    public int getEnumerated() throws IOException {
        return this.getDerValue().getEnumerated();
    }

    public byte[] getBitString() throws IOException {
        return this.getDerValue().getBitString();
    }

    public BitArray getUnalignedBitString() throws IOException {
        return this.getDerValue().getUnalignedBitString();
    }

    public byte[] getOctetString() throws IOException {
        DerValue v = this.getDerValue();
        if (v.tag != 4) {
            throw new IOException("DER input not an octet string");
        }
        return v.getOctetString();
    }

    public void getNull() throws IOException {
        this.getDerValue().getNull();
    }

    public ObjectIdentifier getOID() throws IOException {
        return this.getDerValue().getOID();
    }

    public String getUTF8String() throws IOException {
        return this.getDerValue().getUTF8String();
    }

    public String getPrintableString() throws IOException {
        return this.getDerValue().getPrintableString();
    }

    public String getT61String() throws IOException {
        return this.getDerValue().getT61String();
    }

    public String getBMPString() throws IOException {
        return this.getDerValue().getBMPString();
    }

    public String getIA5String() throws IOException {
        return this.getDerValue().getIA5String();
    }

    public String getGeneralString() throws IOException {
        return this.getDerValue().getGeneralString();
    }

    public Date getUTCTime() throws IOException {
        return this.getDerValue().getUTCTime();
    }

    public Date getGeneralizedTime() throws IOException {
        return this.getDerValue().getGeneralizedTime();
    }

    public DerValue[] getSequence(int startLen) throws IOException {
        return this.getDerValue().subs((byte)48, startLen);
    }

    public DerValue[] getSet(int startLen) throws IOException {
        return this.getDerValue().subs((byte)49, startLen);
    }

    public DerValue[] getSet(int startLen, boolean implicit) throws IOException {
        if (implicit) {
            return this.getDerValue().subs((byte)0, startLen);
        }
        return this.getSet(startLen);
    }

    public int peekByte() throws IOException {
        if (this.pos == this.end) {
            throw new IOException("At end");
        }
        return this.data[this.pos];
    }

    static int getLength(InputStream in) throws IOException {
        int value;
        int lenByte = in.read();
        if (lenByte == -1) {
            throw new IOException("Short read of DER length");
        }
        if (lenByte == 128) {
            return -1;
        }
        String mdName = "DerInputStream.getLength(): ";
        int tmp = lenByte;
        if ((tmp & 0x80) == 0) {
            value = tmp;
        } else {
            if ((tmp &= 0x7F) > 4) {
                throw new IOException(mdName + "lengthTag=" + tmp + ", too big.");
            }
            value = 0xFF & in.read();
            --tmp;
            if (value == 0) {
                throw new IOException(mdName + "Redundant length bytes found");
            }
            while (tmp-- > 0) {
                value <<= 8;
                value += 0xFF & in.read();
            }
            if (value < 0) {
                throw new IOException(mdName + "Invalid length bytes");
            }
            if (value <= 127) {
                throw new IOException(mdName + "Should use short form for length");
            }
        }
        return value;
    }

    static int getDefiniteLength(InputStream in) throws IOException {
        int len = DerInputStream.getLength(in);
        if (len < 0) {
            throw new IOException("Indefinite length encoding not supported");
        }
        return len;
    }

    public void mark(int readAheadLimit) {
        this.mark = this.pos;
    }

    public void reset() {
        this.pos = this.mark;
    }

    public int available() {
        return this.end - this.pos;
    }

    public void atEnd() throws IOException {
        if (this.available() != 0) {
            throw new IOException("Extra unused bytes");
        }
    }

    private boolean checkNextTag(Predicate<Byte> rule) {
        return this.available() > 0 && rule.test(this.data[this.pos]);
    }

    private boolean checkNextTag(byte tag) {
        return this.checkNextTag(t -> t == tag);
    }

    public Optional<DerValue> getOptional(byte tag) throws IOException {
        if (this.checkNextTag(tag)) {
            return Optional.of(this.getDerValue());
        }
        return Optional.empty();
    }

    public boolean seeOptionalContextSpecific(int n) throws IOException {
        return this.checkNextTag(t -> (t & 0xC0) == 128 && (t & 0x1F) == n);
    }

    public Optional<DerValue> getOptionalExplicitContextSpecific(int n) throws IOException {
        if (this.seeOptionalContextSpecific(n)) {
            DerInputStream sub = this.getDerValue().data();
            DerValue inner = sub.getDerValue();
            sub.atEnd();
            return Optional.of(inner);
        }
        return Optional.empty();
    }

    public Optional<DerValue> getOptionalImplicitContextSpecific(int n, byte tag) throws IOException {
        if (this.seeOptionalContextSpecific(n)) {
            DerValue v = this.getDerValue();
            return Optional.of(v.withTag(tag));
        }
        return Optional.empty();
    }
}

