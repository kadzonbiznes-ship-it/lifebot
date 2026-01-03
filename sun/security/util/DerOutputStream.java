/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import sun.security.util.BitArray;
import sun.security.util.ByteArrayLexOrder;
import sun.security.util.ByteArrayTagOrder;
import sun.security.util.DerEncoder;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public final class DerOutputStream
extends ByteArrayOutputStream
implements DerEncoder {
    private static final ByteArrayLexOrder lexOrder = new ByteArrayLexOrder();
    private static final ByteArrayTagOrder tagOrder = new ByteArrayTagOrder();

    public DerOutputStream(int size) {
        super(size);
    }

    public DerOutputStream() {
    }

    public DerOutputStream write(byte tag, byte[] buf) {
        this.write(tag);
        this.putLength(buf.length);
        this.writeBytes(buf);
        return this;
    }

    public DerOutputStream write(byte tag, DerOutputStream out) {
        this.write(tag);
        this.putLength(out.count);
        this.write(out.buf, 0, out.count);
        return this;
    }

    public DerOutputStream writeImplicit(byte tag, DerOutputStream value) {
        this.write(tag);
        this.write(value.buf, 1, value.count - 1);
        return this;
    }

    public DerOutputStream putDerValue(DerValue val) {
        val.encode(this);
        return this;
    }

    public DerOutputStream putBoolean(boolean val) {
        this.write(1);
        this.putLength(1);
        if (val) {
            this.write(255);
        } else {
            this.write(0);
        }
        return this;
    }

    public DerOutputStream putEnumerated(int i) {
        this.write(10);
        this.putIntegerContents(i);
        return this;
    }

    public DerOutputStream putInteger(BigInteger i) {
        this.write(2);
        byte[] buf = i.toByteArray();
        this.putLength(buf.length);
        this.writeBytes(buf);
        return this;
    }

    public DerOutputStream putInteger(byte[] buf) {
        this.write(2);
        this.putLength(buf.length);
        this.writeBytes(buf);
        return this;
    }

    public DerOutputStream putInteger(Integer i) {
        return this.putInteger((int)i);
    }

    public DerOutputStream putInteger(int i) {
        this.write(2);
        this.putIntegerContents(i);
        return this;
    }

    private void putIntegerContents(int i) {
        byte[] bytes = new byte[4];
        int start = 0;
        bytes[3] = (byte)(i & 0xFF);
        bytes[2] = (byte)((i & 0xFF00) >>> 8);
        bytes[1] = (byte)((i & 0xFF0000) >>> 16);
        bytes[0] = (byte)((i & 0xFF000000) >>> 24);
        if (bytes[0] == -1) {
            for (j = 0; j < 3 && bytes[j] == -1 && (bytes[j + 1] & 0x80) == 128; ++j) {
                ++start;
            }
        } else if (bytes[0] == 0) {
            for (j = 0; j < 3 && bytes[j] == 0 && (bytes[j + 1] & 0x80) == 0; ++j) {
                ++start;
            }
        }
        this.putLength(4 - start);
        for (int k = start; k < 4; ++k) {
            this.write(bytes[k]);
        }
    }

    public DerOutputStream putBitString(byte[] bits) {
        this.write(3);
        this.putLength(bits.length + 1);
        this.write(0);
        this.writeBytes(bits);
        return this;
    }

    public DerOutputStream putUnalignedBitString(BitArray ba) {
        byte[] bits = ba.toByteArray();
        this.write(3);
        this.putLength(bits.length + 1);
        this.write(bits.length * 8 - ba.length());
        this.writeBytes(bits);
        return this;
    }

    public DerOutputStream putTruncatedUnalignedBitString(BitArray ba) {
        return this.putUnalignedBitString(ba.truncate());
    }

    public DerOutputStream putOctetString(byte[] octets) {
        return this.write((byte)4, octets);
    }

    public DerOutputStream putNull() {
        this.write(5);
        this.putLength(0);
        return this;
    }

    public DerOutputStream putOID(ObjectIdentifier oid) {
        oid.encode(this);
        return this;
    }

    public DerOutputStream putSequence(DerValue[] seq) {
        DerOutputStream bytes = new DerOutputStream();
        for (int i = 0; i < seq.length; ++i) {
            seq[i].encode(bytes);
        }
        return this.write((byte)48, bytes);
    }

    public DerOutputStream putSet(DerValue[] set) {
        DerOutputStream bytes = new DerOutputStream();
        for (int i = 0; i < set.length; ++i) {
            set[i].encode(bytes);
        }
        return this.write((byte)49, bytes);
    }

    public DerOutputStream putOrderedSetOf(byte tag, DerEncoder[] set) {
        return this.putOrderedSet(tag, set, lexOrder);
    }

    public DerOutputStream putOrderedSet(byte tag, DerEncoder[] set) {
        return this.putOrderedSet(tag, set, tagOrder);
    }

    private DerOutputStream putOrderedSet(byte tag, DerEncoder[] set, Comparator<byte[]> order) {
        DerOutputStream[] streams = new DerOutputStream[set.length];
        for (int i = 0; i < set.length; ++i) {
            streams[i] = new DerOutputStream();
            set[i].encode(streams[i]);
        }
        byte[][] bufs = new byte[streams.length][];
        for (int i = 0; i < streams.length; ++i) {
            bufs[i] = streams[i].toByteArray();
        }
        Arrays.sort(bufs, order);
        DerOutputStream bytes = new DerOutputStream();
        for (int i = 0; i < streams.length; ++i) {
            bytes.writeBytes(bufs[i]);
        }
        return this.write(tag, bytes);
    }

    public DerOutputStream putUTF8String(String s) {
        return this.writeString(s, (byte)12, StandardCharsets.UTF_8);
    }

    public DerOutputStream putPrintableString(String s) {
        return this.writeString(s, (byte)19, StandardCharsets.US_ASCII);
    }

    public DerOutputStream putT61String(String s) {
        return this.writeString(s, (byte)20, StandardCharsets.ISO_8859_1);
    }

    public DerOutputStream putIA5String(String s) {
        return this.writeString(s, (byte)22, StandardCharsets.US_ASCII);
    }

    public DerOutputStream putBMPString(String s) {
        return this.writeString(s, (byte)30, StandardCharsets.UTF_16BE);
    }

    public DerOutputStream putGeneralString(String s) {
        return this.writeString(s, (byte)27, StandardCharsets.US_ASCII);
    }

    private DerOutputStream writeString(String s, byte stringTag, Charset charset) {
        byte[] data = s.getBytes(charset);
        this.write(stringTag);
        this.putLength(data.length);
        this.writeBytes(data);
        return this;
    }

    public DerOutputStream putUTCTime(Date d) {
        return this.putTime(d, (byte)23);
    }

    public DerOutputStream putGeneralizedTime(Date d) {
        return this.putTime(d, (byte)24);
    }

    private DerOutputStream putTime(Date d, byte tag) {
        String pattern;
        TimeZone tz = TimeZone.getTimeZone("GMT");
        if (tag == 23) {
            pattern = "yyMMddHHmmss'Z'";
        } else {
            tag = (byte)24;
            pattern = "yyyyMMddHHmmss'Z'";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
        sdf.setTimeZone(tz);
        byte[] time = sdf.format(d).getBytes(StandardCharsets.ISO_8859_1);
        this.write(tag);
        this.putLength(time.length);
        this.writeBytes(time);
        return this;
    }

    public void putLength(int len) {
        if (len < 128) {
            this.write((byte)len);
        } else if (len < 256) {
            this.write(-127);
            this.write((byte)len);
        } else if (len < 65536) {
            this.write(-126);
            this.write((byte)(len >> 8));
            this.write((byte)len);
        } else if (len < 0x1000000) {
            this.write(-125);
            this.write((byte)(len >> 16));
            this.write((byte)(len >> 8));
            this.write((byte)len);
        } else {
            this.write(-124);
            this.write((byte)(len >> 24));
            this.write((byte)(len >> 16));
            this.write((byte)(len >> 8));
            this.write((byte)len);
        }
    }

    @Override
    public void encode(DerOutputStream out) {
        out.writeBytes(this.toByteArray());
    }

    public DerOutputStream write(DerEncoder encoder) {
        encoder.encode(this);
        return this;
    }

    byte[] buf() {
        return this.buf;
    }
}

