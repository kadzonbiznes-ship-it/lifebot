/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import jdk.internal.access.JavaLangAccess;
import jdk.internal.access.SharedSecrets;

public final class UUID
implements Serializable,
Comparable<UUID> {
    private static final long serialVersionUID = -4856846361193249489L;
    private final long mostSigBits;
    private final long leastSigBits;
    private static final JavaLangAccess jla = SharedSecrets.getJavaLangAccess();
    private static final byte[] NIBBLES;

    private UUID(byte[] data) {
        int i;
        long msb = 0L;
        long lsb = 0L;
        assert (data.length == 16) : "data must be 16 bytes in length";
        for (i = 0; i < 8; ++i) {
            msb = msb << 8 | (long)(data[i] & 0xFF);
        }
        for (i = 8; i < 16; ++i) {
            lsb = lsb << 8 | (long)(data[i] & 0xFF);
        }
        this.mostSigBits = msb;
        this.leastSigBits = lsb;
    }

    public UUID(long mostSigBits, long leastSigBits) {
        this.mostSigBits = mostSigBits;
        this.leastSigBits = leastSigBits;
    }

    public static UUID randomUUID() {
        SecureRandom ng = Holder.numberGenerator;
        byte[] randomBytes = new byte[16];
        ng.nextBytes(randomBytes);
        randomBytes[6] = (byte)(randomBytes[6] & 0xF);
        randomBytes[6] = (byte)(randomBytes[6] | 0x40);
        randomBytes[8] = (byte)(randomBytes[8] & 0x3F);
        randomBytes[8] = (byte)(randomBytes[8] | 0xFFFFFF80);
        return new UUID(randomBytes);
    }

    public static UUID nameUUIDFromBytes(byte[] name) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new InternalError("MD5 not supported", nsae);
        }
        byte[] md5Bytes = md.digest(name);
        md5Bytes[6] = (byte)(md5Bytes[6] & 0xF);
        md5Bytes[6] = (byte)(md5Bytes[6] | 0x30);
        md5Bytes[8] = (byte)(md5Bytes[8] & 0x3F);
        md5Bytes[8] = (byte)(md5Bytes[8] | 0xFFFFFF80);
        return new UUID(md5Bytes);
    }

    private static long parse4Nibbles(String name, int pos) {
        char ch4;
        char ch3;
        char ch2;
        byte[] ns = NIBBLES;
        char ch1 = name.charAt(pos);
        return (ch1 | (ch2 = name.charAt(pos + 1)) | (ch3 = name.charAt(pos + 2)) | (ch4 = name.charAt(pos + 3))) > 255 ? -1L : (long)(ns[ch1] << 12 | ns[ch2] << 8 | ns[ch3] << 4 | ns[ch4]);
    }

    public static UUID fromString(String name) {
        if (name.length() == 36) {
            long lsb4;
            long lsb3;
            long lsb2;
            long lsb1;
            long msb4;
            long msb3;
            long msb2;
            long msb1;
            char ch1 = name.charAt(8);
            char ch2 = name.charAt(13);
            char ch3 = name.charAt(18);
            char ch4 = name.charAt(23);
            if (ch1 == '-' && ch2 == '-' && ch3 == '-' && ch4 == '-' && ((msb1 = UUID.parse4Nibbles(name, 0)) | (msb2 = UUID.parse4Nibbles(name, 4)) | (msb3 = UUID.parse4Nibbles(name, 9)) | (msb4 = UUID.parse4Nibbles(name, 14)) | (lsb1 = UUID.parse4Nibbles(name, 19)) | (lsb2 = UUID.parse4Nibbles(name, 24)) | (lsb3 = UUID.parse4Nibbles(name, 28)) | (lsb4 = UUID.parse4Nibbles(name, 32))) >= 0L) {
                return new UUID(msb1 << 48 | msb2 << 32 | msb3 << 16 | msb4, lsb1 << 48 | lsb2 << 32 | lsb3 << 16 | lsb4);
            }
        }
        return UUID.fromString1(name);
    }

    private static UUID fromString1(String name) {
        int len = name.length();
        if (len > 36) {
            throw new IllegalArgumentException("UUID string too large");
        }
        int dash1 = name.indexOf(45);
        int dash2 = name.indexOf(45, dash1 + 1);
        int dash3 = name.indexOf(45, dash2 + 1);
        int dash4 = name.indexOf(45, dash3 + 1);
        int dash5 = name.indexOf(45, dash4 + 1);
        if (dash4 < 0 || dash5 >= 0) {
            throw new IllegalArgumentException("Invalid UUID string: " + name);
        }
        long mostSigBits = Long.parseLong(name, 0, dash1, 16) & 0xFFFFFFFFL;
        mostSigBits <<= 16;
        mostSigBits |= Long.parseLong(name, dash1 + 1, dash2, 16) & 0xFFFFL;
        mostSigBits <<= 16;
        long leastSigBits = Long.parseLong(name, dash3 + 1, dash4, 16) & 0xFFFFL;
        leastSigBits <<= 48;
        return new UUID(mostSigBits |= Long.parseLong(name, dash2 + 1, dash3, 16) & 0xFFFFL, leastSigBits |= Long.parseLong(name, dash4 + 1, len, 16) & 0xFFFFFFFFFFFFL);
    }

    public long getLeastSignificantBits() {
        return this.leastSigBits;
    }

    public long getMostSignificantBits() {
        return this.mostSigBits;
    }

    public int version() {
        return (int)(this.mostSigBits >> 12 & 0xFL);
    }

    public int variant() {
        return (int)(this.leastSigBits >>> (int)(64L - (this.leastSigBits >>> 62)) & this.leastSigBits >> 63);
    }

    public long timestamp() {
        if (this.version() != 1) {
            throw new UnsupportedOperationException("Not a time-based UUID");
        }
        return (this.mostSigBits & 0xFFFL) << 48 | (this.mostSigBits >> 16 & 0xFFFFL) << 32 | this.mostSigBits >>> 32;
    }

    public int clockSequence() {
        if (this.version() != 1) {
            throw new UnsupportedOperationException("Not a time-based UUID");
        }
        return (int)((this.leastSigBits & 0x3FFF000000000000L) >>> 48);
    }

    public long node() {
        if (this.version() != 1) {
            throw new UnsupportedOperationException("Not a time-based UUID");
        }
        return this.leastSigBits & 0xFFFFFFFFFFFFL;
    }

    public String toString() {
        return jla.fastUUID(this.leastSigBits, this.mostSigBits);
    }

    public int hashCode() {
        return Long.hashCode(this.mostSigBits ^ this.leastSigBits);
    }

    public boolean equals(Object obj) {
        if (null == obj || obj.getClass() != UUID.class) {
            return false;
        }
        UUID id = (UUID)obj;
        return this.mostSigBits == id.mostSigBits && this.leastSigBits == id.leastSigBits;
    }

    @Override
    public int compareTo(UUID val) {
        int mostSigBits = Long.compare(this.mostSigBits, val.mostSigBits);
        return mostSigBits != 0 ? mostSigBits : Long.compare(this.leastSigBits, val.leastSigBits);
    }

    static {
        byte[] ns = new byte[256];
        Arrays.fill(ns, (byte)-1);
        ns[48] = 0;
        ns[49] = 1;
        ns[50] = 2;
        ns[51] = 3;
        ns[52] = 4;
        ns[53] = 5;
        ns[54] = 6;
        ns[55] = 7;
        ns[56] = 8;
        ns[57] = 9;
        ns[65] = 10;
        ns[66] = 11;
        ns[67] = 12;
        ns[68] = 13;
        ns[69] = 14;
        ns[70] = 15;
        ns[97] = 10;
        ns[98] = 11;
        ns[99] = 12;
        ns[100] = 13;
        ns[101] = 14;
        ns[102] = 15;
        NIBBLES = ns;
    }

    private static class Holder {
        static final SecureRandom numberGenerator = new SecureRandom();

        private Holder() {
        }
    }
}

