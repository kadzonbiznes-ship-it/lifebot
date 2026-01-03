/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.io.ByteArrayOutputStream;
import jdk.internal.util.Preconditions;

public class BitArray {
    private final byte[] repn;
    private final int length;
    private static final int BITS_PER_UNIT = 8;
    private static final byte[][] NYBBLE = new byte[][]{{48, 48, 48, 48}, {48, 48, 48, 49}, {48, 48, 49, 48}, {48, 48, 49, 49}, {48, 49, 48, 48}, {48, 49, 48, 49}, {48, 49, 49, 48}, {48, 49, 49, 49}, {49, 48, 48, 48}, {49, 48, 48, 49}, {49, 48, 49, 48}, {49, 48, 49, 49}, {49, 49, 48, 48}, {49, 49, 48, 49}, {49, 49, 49, 48}, {49, 49, 49, 49}};
    private static final int BYTES_PER_LINE = 8;

    private static int subscript(int idx) {
        return idx / 8;
    }

    private static int position(int idx) {
        return 1 << 7 - idx % 8;
    }

    public BitArray(int length) throws IllegalArgumentException {
        if (length < 0) {
            throw new IllegalArgumentException("Negative length for BitArray");
        }
        this.length = length;
        this.repn = new byte[(length + 8 - 1) / 8];
    }

    public BitArray(int length, byte[] a) throws IllegalArgumentException {
        this(length, a, 0);
    }

    public BitArray(int length, byte[] a, int ofs) throws IllegalArgumentException {
        if (length < 0) {
            throw new IllegalArgumentException("Negative length for BitArray");
        }
        if ((a.length - ofs) * 8 < length) {
            throw new IllegalArgumentException("Byte array too short to represent " + length + "-bit array");
        }
        this.length = length;
        int repLength = (length + 8 - 1) / 8;
        int unusedBits = repLength * 8 - length;
        byte bitMask = (byte)(255 << unusedBits);
        this.repn = new byte[repLength];
        System.arraycopy(a, ofs, this.repn, 0, repLength);
        if (repLength > 0) {
            int n = repLength - 1;
            this.repn[n] = (byte)(this.repn[n] & bitMask);
        }
    }

    public BitArray(boolean[] bits) {
        this.length = bits.length;
        this.repn = new byte[(this.length + 7) / 8];
        for (int i = 0; i < this.length; ++i) {
            this.set(i, bits[i]);
        }
    }

    private BitArray(BitArray ba) {
        this.length = ba.length;
        this.repn = (byte[])ba.repn.clone();
    }

    public boolean get(int index) throws ArrayIndexOutOfBoundsException {
        Preconditions.checkIndex(index, this.length, Preconditions.AIOOBE_FORMATTER);
        return (this.repn[BitArray.subscript(index)] & BitArray.position(index)) != 0;
    }

    public void set(int index, boolean value) throws ArrayIndexOutOfBoundsException {
        Preconditions.checkIndex(index, this.length, Preconditions.AIOOBE_FORMATTER);
        int idx = BitArray.subscript(index);
        int bit = BitArray.position(index);
        if (value) {
            int n = idx;
            this.repn[n] = (byte)(this.repn[n] | (byte)bit);
        } else {
            int n = idx;
            this.repn[n] = (byte)(this.repn[n] & (byte)(~bit));
        }
    }

    public int length() {
        return this.length;
    }

    public byte[] toByteArray() {
        return (byte[])this.repn.clone();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BitArray)) {
            return false;
        }
        BitArray ba = (BitArray)obj;
        if (ba.length != this.length) {
            return false;
        }
        for (int i = 0; i < this.repn.length; ++i) {
            if (this.repn[i] == ba.repn[i]) continue;
            return false;
        }
        return true;
    }

    public boolean[] toBooleanArray() {
        boolean[] bits = new boolean[this.length];
        for (int i = 0; i < this.length; ++i) {
            bits[i] = this.get(i);
        }
        return bits;
    }

    public int hashCode() {
        int hashCode = 0;
        for (int i = 0; i < this.repn.length; ++i) {
            hashCode = 31 * hashCode + this.repn[i];
        }
        return hashCode ^ this.length;
    }

    public Object clone() {
        return new BitArray(this);
    }

    public String toString() {
        int i;
        if (this.length == 0) {
            return "";
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (i = 0; i < this.repn.length - 1; ++i) {
            out.write(NYBBLE[this.repn[i] >> 4 & 0xF], 0, 4);
            out.write(NYBBLE[this.repn[i] & 0xF], 0, 4);
            if (i % 8 == 7) {
                out.write(10);
                continue;
            }
            out.write(32);
        }
        for (i = 8 * (this.repn.length - 1); i < this.length; ++i) {
            out.write(this.get(i) ? 49 : 48);
        }
        return out.toString();
    }

    public BitArray truncate() {
        for (int i = this.length - 1; i >= 0; --i) {
            if (!this.get(i)) continue;
            return new BitArray(i + 1, this.repn, 0);
        }
        return new BitArray(1);
    }
}

