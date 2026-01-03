/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.KnownOIDs;

public final class ObjectIdentifier
implements Serializable {
    private static final int MAXIMUM_OID_SIZE = 4096;
    private byte[] encoding;
    private volatile transient String stringForm;
    private static final long serialVersionUID = 8697030238860181294L;
    private Object components = null;
    private int componentLen = -1;
    private transient boolean componentsCalculated = false;
    private static final ConcurrentHashMap<String, ObjectIdentifier> oidTable = new ConcurrentHashMap();

    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
        is.defaultReadObject();
        if (this.encoding == null) {
            if (this.components == null) {
                throw new InvalidObjectException("OID components is null");
            }
            int[] comp = (int[])((int[])this.components).clone();
            if (this.componentLen > comp.length) {
                this.componentLen = comp.length;
            }
            ObjectIdentifier.checkOidSize(this.componentLen);
            this.init(comp, this.componentLen);
            this.components = comp;
        } else {
            this.encoding = (byte[])this.encoding.clone();
            ObjectIdentifier.checkOidSize(this.encoding.length);
            ObjectIdentifier.check(this.encoding);
        }
    }

    private void writeObject(ObjectOutputStream os) throws IOException {
        if (!this.componentsCalculated) {
            int[] comps = this.toIntArray();
            if (comps != null) {
                this.components = comps;
                this.componentLen = comps.length;
            } else {
                this.components = HugeOidNotSupportedByOldJDK.theOne;
            }
            this.componentsCalculated = true;
        }
        os.defaultWriteObject();
    }

    private ObjectIdentifier(String oid) throws IOException {
        int ch = 46;
        int start = 0;
        int pos = 0;
        byte[] tmp = new byte[oid.length()];
        int first = 0;
        int count = 0;
        try {
            int end;
            do {
                int length;
                String comp;
                if ((end = oid.indexOf(ch, start)) == -1) {
                    comp = oid.substring(start);
                    length = oid.length() - start;
                } else {
                    comp = oid.substring(start, end);
                    length = end - start;
                }
                if (length > 9) {
                    BigInteger bignum = new BigInteger(comp);
                    if (count == 0) {
                        ObjectIdentifier.checkFirstComponent(bignum);
                        first = bignum.intValue();
                    } else {
                        if (count == 1) {
                            ObjectIdentifier.checkSecondComponent(first, bignum);
                            bignum = bignum.add(BigInteger.valueOf(40L * (long)first));
                        } else {
                            ObjectIdentifier.checkOtherComponent(count, bignum);
                        }
                        pos += ObjectIdentifier.pack7Oid(bignum, tmp, pos);
                    }
                } else {
                    int num = Integer.parseInt(comp);
                    if (count == 0) {
                        ObjectIdentifier.checkFirstComponent(num);
                        first = num;
                    } else {
                        if (count == 1) {
                            ObjectIdentifier.checkSecondComponent(first, num);
                            num += 40 * first;
                        } else {
                            ObjectIdentifier.checkOtherComponent(count, num);
                        }
                        pos += ObjectIdentifier.pack7Oid(num, tmp, pos);
                    }
                }
                start = end + 1;
                ++count;
                ObjectIdentifier.checkOidSize(pos);
            } while (end != -1);
            ObjectIdentifier.checkCount(count);
            this.encoding = new byte[pos];
            System.arraycopy(tmp, 0, this.encoding, 0, pos);
            this.stringForm = oid;
        }
        catch (IOException ioe) {
            throw ioe;
        }
        catch (Exception e) {
            throw new IOException("ObjectIdentifier() -- Invalid format: " + e.toString(), e);
        }
    }

    ObjectIdentifier(byte[] encoding) throws IOException {
        ObjectIdentifier.checkOidSize(encoding.length);
        ObjectIdentifier.check(encoding);
        this.encoding = encoding;
    }

    public ObjectIdentifier(DerInputStream in) throws IOException {
        this.encoding = in.getDerValue().getOID().encoding;
    }

    private void init(int[] components, int length) throws IOException {
        int pos = 0;
        byte[] tmp = new byte[length * 5 + 1];
        if (components[1] < Integer.MAX_VALUE - components[0] * 40) {
            pos += ObjectIdentifier.pack7Oid(components[0] * 40 + components[1], tmp, pos);
        } else {
            BigInteger big = BigInteger.valueOf(components[1]);
            big = big.add(BigInteger.valueOf((long)components[0] * 40L));
            pos += ObjectIdentifier.pack7Oid(big, tmp, pos);
        }
        for (int i = 2; i < length; ++i) {
            pos += ObjectIdentifier.pack7Oid(components[i], tmp, pos);
            ObjectIdentifier.checkOidSize(pos);
        }
        this.encoding = new byte[pos];
        System.arraycopy(tmp, 0, this.encoding, 0, pos);
    }

    public static ObjectIdentifier of(String oidStr) throws IOException {
        ObjectIdentifier oid = oidTable.get(oidStr);
        if (oid == null) {
            oid = new ObjectIdentifier(oidStr);
            oidTable.put(oidStr, oid);
        }
        return oid;
    }

    public static ObjectIdentifier of(KnownOIDs o) {
        String oidStr = o.value();
        ObjectIdentifier oid = oidTable.get(oidStr);
        if (oid == null) {
            try {
                oid = new ObjectIdentifier(oidStr);
            }
            catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
            oidTable.put(oidStr, oid);
        }
        return oid;
    }

    void encode(DerOutputStream out) {
        out.write((byte)6, this.encoding);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ObjectIdentifier)) {
            return false;
        }
        ObjectIdentifier other = (ObjectIdentifier)obj;
        return Arrays.equals(this.encoding, other.encoding);
    }

    public int hashCode() {
        return Arrays.hashCode(this.encoding);
    }

    private int[] toIntArray() {
        int length = this.encoding.length;
        int[] result = new int[20];
        int which = 0;
        int fromPos = 0;
        for (int i = 0; i < length; ++i) {
            if ((this.encoding[i] & 0x80) == 0) {
                if (i - fromPos + 1 > 4) {
                    BigInteger big = new BigInteger(1, ObjectIdentifier.pack(this.encoding, fromPos, i - fromPos + 1, 7, 8));
                    if (fromPos == 0) {
                        result[which++] = 2;
                        BigInteger second = big.subtract(BigInteger.valueOf(80L));
                        if (second.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 1) {
                            return null;
                        }
                        result[which++] = second.intValue();
                    } else {
                        if (big.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 1) {
                            return null;
                        }
                        result[which++] = big.intValue();
                    }
                } else {
                    int retval = 0;
                    for (int j = fromPos; j <= i; ++j) {
                        retval <<= 7;
                        byte tmp = this.encoding[j];
                        retval |= tmp & 0x7F;
                    }
                    if (fromPos == 0) {
                        if (retval < 80) {
                            result[which++] = retval / 40;
                            result[which++] = retval % 40;
                        } else {
                            result[which++] = 2;
                            result[which++] = retval - 80;
                        }
                    } else {
                        result[which++] = retval;
                    }
                }
                fromPos = i + 1;
            }
            if (which < result.length) continue;
            result = Arrays.copyOf(result, which + 10);
        }
        return Arrays.copyOf(result, which);
    }

    public String toString() {
        String s = this.stringForm;
        if (s == null) {
            int length = this.encoding.length;
            StringBuilder sb = new StringBuilder(length * 4);
            int fromPos = 0;
            for (int i = 0; i < length; ++i) {
                if ((this.encoding[i] & 0x80) != 0) continue;
                if (fromPos != 0) {
                    sb.append('.');
                }
                if (i - fromPos + 1 > 4) {
                    BigInteger big = new BigInteger(1, ObjectIdentifier.pack(this.encoding, fromPos, i - fromPos + 1, 7, 8));
                    if (fromPos == 0) {
                        sb.append("2.");
                        sb.append(big.subtract(BigInteger.valueOf(80L)));
                    } else {
                        sb.append(big);
                    }
                } else {
                    int retval = 0;
                    for (int j = fromPos; j <= i; ++j) {
                        retval <<= 7;
                        byte tmp = this.encoding[j];
                        retval |= tmp & 0x7F;
                    }
                    if (fromPos == 0) {
                        if (retval < 80) {
                            sb.append(retval / 40);
                            sb.append('.');
                            sb.append(retval % 40);
                        } else {
                            sb.append("2.");
                            sb.append(retval - 80);
                        }
                    } else {
                        sb.append(retval);
                    }
                }
                fromPos = i + 1;
            }
            this.stringForm = s = sb.toString();
        }
        return s;
    }

    private static byte[] pack(byte[] in, int ioffset, int ilength, int iw, int ow) {
        assert (iw > 0 && iw <= 8) : "input NUB must be between 1 and 8";
        assert (ow > 0 && ow <= 8) : "output NUB must be between 1 and 8";
        if (iw == ow) {
            return (byte[])in.clone();
        }
        int bits = ilength * iw;
        byte[] out = new byte[(bits + ow - 1) / ow];
        int ipos = 0;
        int opos = (bits + ow - 1) / ow * ow - bits;
        while (ipos < bits) {
            int count = iw - ipos % iw;
            if (count > ow - opos % ow) {
                count = ow - opos % ow;
            }
            int n = opos / ow;
            out[n] = (byte)(out[n] | (byte)((in[ioffset + ipos / iw] + 256 >> iw - ipos % iw - count & (1 << count) - 1) << ow - opos % ow - count));
            ipos += count;
            opos += count;
        }
        return out;
    }

    private static int pack7Oid(byte[] in, int ioffset, int ilength, byte[] out, int ooffset) {
        byte[] pack = ObjectIdentifier.pack(in, ioffset, ilength, 8, 7);
        int firstNonZero = pack.length - 1;
        int i = pack.length - 2;
        while (i >= 0) {
            if (pack[i] != 0) {
                firstNonZero = i;
            }
            int n = i--;
            pack[n] = (byte)(pack[n] | 0xFFFFFF80);
        }
        System.arraycopy(pack, firstNonZero, out, ooffset, pack.length - firstNonZero);
        return pack.length - firstNonZero;
    }

    private static int pack8(byte[] in, int ioffset, int ilength, byte[] out, int ooffset) {
        byte[] pack = ObjectIdentifier.pack(in, ioffset, ilength, 7, 8);
        int firstNonZero = pack.length - 1;
        for (int i = pack.length - 2; i >= 0; --i) {
            if (pack[i] == 0) continue;
            firstNonZero = i;
        }
        System.arraycopy(pack, firstNonZero, out, ooffset, pack.length - firstNonZero);
        return pack.length - firstNonZero;
    }

    private static int pack7Oid(int input, byte[] out, int ooffset) {
        byte[] b = new byte[]{(byte)(input >> 24), (byte)(input >> 16), (byte)(input >> 8), (byte)input};
        return ObjectIdentifier.pack7Oid(b, 0, 4, out, ooffset);
    }

    private static int pack7Oid(BigInteger input, byte[] out, int ooffset) {
        byte[] b = input.toByteArray();
        return ObjectIdentifier.pack7Oid(b, 0, b.length, out, ooffset);
    }

    private static void check(byte[] encoding) throws IOException {
        int length = encoding.length;
        if (length < 1 || (encoding[length - 1] & 0x80) != 0) {
            throw new IOException("ObjectIdentifier() -- Invalid DER encoding, not ended");
        }
        for (int i = 0; i < length; ++i) {
            if (encoding[i] != -128 || i != 0 && (encoding[i - 1] & 0x80) != 0) continue;
            throw new IOException("ObjectIdentifier() -- Invalid DER encoding, useless extra octet detected");
        }
    }

    private static void checkCount(int count) throws IOException {
        if (count < 2) {
            throw new IOException("ObjectIdentifier() -- Must be at least two oid components ");
        }
    }

    private static void checkFirstComponent(int first) throws IOException {
        if (first < 0 || first > 2) {
            throw new IOException("ObjectIdentifier() -- First oid component is invalid ");
        }
    }

    private static void checkFirstComponent(BigInteger first) throws IOException {
        if (first.signum() == -1 || first.compareTo(BigInteger.TWO) > 0) {
            throw new IOException("ObjectIdentifier() -- First oid component is invalid ");
        }
    }

    private static void checkSecondComponent(int first, int second) throws IOException {
        if (second < 0 || first != 2 && second > 39) {
            throw new IOException("ObjectIdentifier() -- Second oid component is invalid ");
        }
    }

    private static void checkSecondComponent(int first, BigInteger second) throws IOException {
        if (second.signum() == -1 || first != 2 && second.compareTo(BigInteger.valueOf(39L)) == 1) {
            throw new IOException("ObjectIdentifier() -- Second oid component is invalid ");
        }
    }

    private static void checkOtherComponent(int i, int num) throws IOException {
        if (num < 0) {
            throw new IOException("ObjectIdentifier() -- oid component #" + (i + 1) + " must be non-negative ");
        }
    }

    private static void checkOtherComponent(int i, BigInteger num) throws IOException {
        if (num.signum() == -1) {
            throw new IOException("ObjectIdentifier() -- oid component #" + (i + 1) + " must be non-negative ");
        }
    }

    private static void checkOidSize(int oidLength) throws IOException {
        if (oidLength < 0) {
            throw new IOException("ObjectIdentifier encoded length was negative: " + oidLength);
        }
        if (oidLength > 4096) {
            throw new IOException("ObjectIdentifier encoded length exceeds the restriction in JDK (OId length(>=): " + oidLength + ", Restriction: " + 4096 + ")");
        }
    }

    static class HugeOidNotSupportedByOldJDK
    implements Serializable {
        private static final long serialVersionUID = 1L;
        static HugeOidNotSupportedByOldJDK theOne = new HugeOidNotSupportedByOldJDK();

        HugeOidNotSupportedByOldJDK() {
        }
    }
}

