/*
 * Decompiled with CFR 0.152.
 */
package java.math;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.io.StreamCorruptedException;
import java.math.BitSieve;
import java.math.MutableBigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ThreadLocalRandom;
import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;
import jdk.internal.vm.annotation.IntrinsicCandidate;
import jdk.internal.vm.annotation.Stable;

public class BigInteger
extends Number
implements Comparable<BigInteger> {
    final int signum;
    final int[] mag;
    private int bitCountPlusOne;
    private int bitLengthPlusOne;
    private int lowestSetBitPlusTwo;
    private int firstNonzeroIntNumPlusTwo;
    static final long LONG_MASK = 0xFFFFFFFFL;
    private static final int MAX_MAG_LENGTH = 0x4000000;
    private static final int PRIME_SEARCH_BIT_LENGTH_LIMIT = 500000000;
    private static final int KARATSUBA_THRESHOLD = 80;
    private static final int TOOM_COOK_THRESHOLD = 240;
    private static final int KARATSUBA_SQUARE_THRESHOLD = 128;
    private static final int TOOM_COOK_SQUARE_THRESHOLD = 216;
    static final int BURNIKEL_ZIEGLER_THRESHOLD = 80;
    static final int BURNIKEL_ZIEGLER_OFFSET = 40;
    private static final int SCHOENHAGE_BASE_CONVERSION_THRESHOLD = 20;
    private static final int MULTIPLY_SQUARE_THRESHOLD = 20;
    private static final int MONTGOMERY_INTRINSIC_THRESHOLD = 512;
    private static long[] bitsPerDigit = new long[]{0L, 0L, 1024L, 1624L, 2048L, 2378L, 2648L, 2875L, 3072L, 3247L, 3402L, 3543L, 3672L, 3790L, 3899L, 4001L, 4096L, 4186L, 4271L, 4350L, 4426L, 4498L, 4567L, 4633L, 4696L, 4756L, 4814L, 4870L, 4923L, 4975L, 5025L, 5074L, 5120L, 5166L, 5210L, 5253L, 5295L};
    private static final int SMALL_PRIME_THRESHOLD = 95;
    private static final int DEFAULT_PRIME_CERTAINTY = 100;
    private static final BigInteger SMALL_PRIME_PRODUCT = BigInteger.valueOf(152125131763605L);
    private static final int MAX_CONSTANT = 16;
    @Stable
    private static final BigInteger[] posConst = new BigInteger[17];
    @Stable
    private static final BigInteger[] negConst = new BigInteger[17];
    private static volatile BigInteger[][] powerCache;
    private static final double[] logCache;
    private static final double LOG_TWO;
    public static final BigInteger ZERO;
    public static final BigInteger ONE;
    public static final BigInteger TWO;
    private static final BigInteger NEGATIVE_ONE;
    public static final BigInteger TEN;
    static int[] bnExpModThreshTable;
    private static int NUM_ZEROS;
    private static final String ZEROS;
    private static int[] digitsPerLong;
    private static BigInteger[] longRadix;
    private static int[] digitsPerInt;
    private static int[] intRadix;
    private static final long serialVersionUID = -8287574255936472291L;
    private static final ObjectStreamField[] serialPersistentFields;

    public BigInteger(byte[] val, int off, int len) {
        if (val.length == 0) {
            throw new NumberFormatException("Zero length BigInteger");
        }
        Objects.checkFromIndexSize(off, len, val.length);
        if (len == 0) {
            this.mag = BigInteger.ZERO.mag;
            this.signum = BigInteger.ZERO.signum;
            return;
        }
        byte b = val[off];
        if (b < 0) {
            this.mag = BigInteger.makePositive(b, val, off, len);
            this.signum = -1;
        } else {
            this.mag = BigInteger.stripLeadingZeroBytes(b, val, off, len);
            int n = this.signum = this.mag.length == 0 ? 0 : 1;
        }
        if (this.mag.length >= 0x4000000) {
            this.checkRange();
        }
    }

    public BigInteger(byte[] val) {
        this(val, 0, val.length);
    }

    private BigInteger(int[] val) {
        if (val.length == 0) {
            throw new NumberFormatException("Zero length BigInteger");
        }
        if (val[0] < 0) {
            this.mag = BigInteger.makePositive(val);
            this.signum = -1;
        } else {
            this.mag = BigInteger.trustedStripLeadingZeroInts(val);
            int n = this.signum = this.mag.length == 0 ? 0 : 1;
        }
        if (this.mag.length >= 0x4000000) {
            this.checkRange();
        }
    }

    public BigInteger(int signum, byte[] magnitude, int off, int len) {
        if (signum < -1 || signum > 1) {
            throw new NumberFormatException("Invalid signum value");
        }
        Objects.checkFromIndexSize(off, len, magnitude.length);
        this.mag = BigInteger.stripLeadingZeroBytes(magnitude, off, len);
        if (this.mag.length == 0) {
            this.signum = 0;
        } else {
            if (signum == 0) {
                throw new NumberFormatException("signum-magnitude mismatch");
            }
            this.signum = signum;
        }
        if (this.mag.length >= 0x4000000) {
            this.checkRange();
        }
    }

    public BigInteger(int signum, byte[] magnitude) {
        this(signum, magnitude, 0, magnitude.length);
    }

    private BigInteger(int signum, int[] magnitude) {
        this.mag = BigInteger.stripLeadingZeroInts(magnitude);
        if (signum < -1 || signum > 1) {
            throw new NumberFormatException("Invalid signum value");
        }
        if (this.mag.length == 0) {
            this.signum = 0;
        } else {
            if (signum == 0) {
                throw new NumberFormatException("signum-magnitude mismatch");
            }
            this.signum = signum;
        }
        if (this.mag.length >= 0x4000000) {
            this.checkRange();
        }
    }

    public BigInteger(String val, int radix) {
        int cursor = 0;
        int len = val.length();
        if (radix < 2 || radix > 36) {
            throw new NumberFormatException("Radix out of range");
        }
        if (len == 0) {
            throw new NumberFormatException("Zero length BigInteger");
        }
        int sign = 1;
        int index1 = val.lastIndexOf(45);
        int index2 = val.lastIndexOf(43);
        if (index1 >= 0) {
            if (index1 != 0 || index2 >= 0) {
                throw new NumberFormatException("Illegal embedded sign character");
            }
            sign = -1;
            cursor = 1;
        } else if (index2 >= 0) {
            if (index2 != 0) {
                throw new NumberFormatException("Illegal embedded sign character");
            }
            cursor = 1;
        }
        if (cursor == len) {
            throw new NumberFormatException("Zero length BigInteger");
        }
        while (cursor < len && Character.digit(val.charAt(cursor), radix) == 0) {
            ++cursor;
        }
        if (cursor == len) {
            this.signum = 0;
            this.mag = BigInteger.ZERO.mag;
            return;
        }
        int numDigits = len - cursor;
        this.signum = sign;
        long numBits = ((long)numDigits * bitsPerDigit[radix] >>> 10) + 1L;
        if (numBits + 31L >= 0x100000000L) {
            BigInteger.reportOverflow();
        }
        int numWords = (int)(numBits + 31L) >>> 5;
        int[] magnitude = new int[numWords];
        int firstGroupLen = numDigits % digitsPerInt[radix];
        if (firstGroupLen == 0) {
            firstGroupLen = digitsPerInt[radix];
        }
        String group = val.substring(cursor, cursor += firstGroupLen);
        magnitude[numWords - 1] = Integer.parseInt(group, radix);
        if (magnitude[numWords - 1] < 0) {
            throw new NumberFormatException("Illegal digit");
        }
        int superRadix = intRadix[radix];
        int groupVal = 0;
        while (cursor < len) {
            if ((groupVal = Integer.parseInt(group = val.substring(cursor, cursor += digitsPerInt[radix]), radix)) < 0) {
                throw new NumberFormatException("Illegal digit");
            }
            BigInteger.destructiveMulAdd(magnitude, superRadix, groupVal);
        }
        this.mag = BigInteger.trustedStripLeadingZeroInts(magnitude);
        if (this.mag.length >= 0x4000000) {
            this.checkRange();
        }
    }

    BigInteger(char[] val, int sign, int len) {
        int numWords;
        int cursor;
        for (cursor = 0; cursor < len && Character.digit(val[cursor], 10) == 0; ++cursor) {
        }
        if (cursor == len) {
            this.signum = 0;
            this.mag = BigInteger.ZERO.mag;
            return;
        }
        int numDigits = len - cursor;
        this.signum = sign;
        if (len < 10) {
            numWords = 1;
        } else {
            long numBits = ((long)numDigits * bitsPerDigit[10] >>> 10) + 1L;
            if (numBits + 31L >= 0x100000000L) {
                BigInteger.reportOverflow();
            }
            numWords = (int)(numBits + 31L) >>> 5;
        }
        int[] magnitude = new int[numWords];
        int firstGroupLen = numDigits % digitsPerInt[10];
        if (firstGroupLen == 0) {
            firstGroupLen = digitsPerInt[10];
        }
        magnitude[numWords - 1] = this.parseInt(val, cursor, cursor += firstGroupLen);
        while (cursor < len) {
            int groupVal = this.parseInt(val, cursor, cursor += digitsPerInt[10]);
            BigInteger.destructiveMulAdd(magnitude, intRadix[10], groupVal);
        }
        this.mag = BigInteger.trustedStripLeadingZeroInts(magnitude);
        if (this.mag.length >= 0x4000000) {
            this.checkRange();
        }
    }

    private int parseInt(char[] source, int start, int end) {
        int result;
        if ((result = Character.digit(source[start++], 10)) == -1) {
            throw new NumberFormatException(new String(source));
        }
        for (int index = start; index < end; ++index) {
            int nextVal = Character.digit(source[index], 10);
            if (nextVal == -1) {
                throw new NumberFormatException(new String(source));
            }
            result = 10 * result + nextVal;
        }
        return result;
    }

    private static void destructiveMulAdd(int[] x, int y, int z) {
        long ylong = (long)y & 0xFFFFFFFFL;
        long zlong = (long)z & 0xFFFFFFFFL;
        int len = x.length;
        long product = 0L;
        long carry = 0L;
        for (int i = len - 1; i >= 0; --i) {
            product = ylong * ((long)x[i] & 0xFFFFFFFFL) + carry;
            x[i] = (int)product;
            carry = product >>> 32;
        }
        long sum = ((long)x[len - 1] & 0xFFFFFFFFL) + zlong;
        x[len - 1] = (int)sum;
        carry = sum >>> 32;
        for (int i = len - 2; i >= 0; --i) {
            sum = ((long)x[i] & 0xFFFFFFFFL) + carry;
            x[i] = (int)sum;
            carry = sum >>> 32;
        }
    }

    public BigInteger(String val) {
        this(val, 10);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public BigInteger(int numBits, Random rnd) {
        byte[] magnitude = BigInteger.randomBits(numBits, rnd);
        try {
            this.mag = BigInteger.stripLeadingZeroBytes(magnitude, 0, magnitude.length);
            this.signum = this.mag.length == 0 ? 0 : 1;
            if (this.mag.length >= 0x4000000) {
                this.checkRange();
            }
        }
        finally {
            Arrays.fill(magnitude, (byte)0);
        }
    }

    private static byte[] randomBits(int numBits, Random rnd) {
        if (numBits < 0) {
            throw new IllegalArgumentException("numBits must be non-negative");
        }
        int numBytes = (int)(((long)numBits + 7L) / 8L);
        byte[] randomBits = new byte[numBytes];
        if (numBytes > 0) {
            rnd.nextBytes(randomBits);
            int excessBits = 8 * numBytes - numBits;
            randomBits[0] = (byte)(randomBits[0] & (byte)((1 << 8 - excessBits) - 1));
        }
        return randomBits;
    }

    public BigInteger(int bitLength, int certainty, Random rnd) {
        if (bitLength < 2) {
            throw new ArithmeticException("bitLength < 2");
        }
        BigInteger prime = bitLength < 95 ? BigInteger.smallPrime(bitLength, certainty, rnd) : BigInteger.largePrime(bitLength, certainty, rnd);
        this.signum = 1;
        this.mag = prime.mag;
    }

    public static BigInteger probablePrime(int bitLength, Random rnd) {
        if (bitLength < 2) {
            throw new ArithmeticException("bitLength < 2");
        }
        return bitLength < 95 ? BigInteger.smallPrime(bitLength, 100, rnd) : BigInteger.largePrime(bitLength, 100, rnd);
    }

    /*
     * Unable to fully structure code
     */
    private static BigInteger smallPrime(int bitLength, int certainty, Random rnd) {
        magLen = bitLength + 31 >>> 5;
        temp = new int[magLen];
        highBit = 1 << (bitLength + 31 & 31);
        highMask = (highBit << 1) - 1;
        while (true) {
            for (i = 0; i < magLen; ++i) {
                temp[i] = rnd.nextInt();
            }
            temp[0] = temp[0] & highMask | highBit;
            if (bitLength > 2) {
                v0 = magLen - 1;
                temp[v0] = temp[v0] | 1;
            }
            p = new BigInteger(temp, 1);
            if (bitLength > 6 && ((r = p.remainder(BigInteger.SMALL_PRIME_PRODUCT).longValue()) % 3L == 0L || r % 5L == 0L || r % 7L == 0L || r % 11L == 0L || r % 13L == 0L || r % 17L == 0L || r % 19L == 0L || r % 23L == 0L || r % 29L == 0L || r % 31L == 0L || r % 37L == 0L || r % 41L == 0L)) ** continue;
            if (bitLength < 4) {
                return p;
            }
            if (p.primeToCertainty(certainty, rnd)) break;
        }
        return p;
    }

    private static BigInteger largePrime(int bitLength, int certainty, Random rnd) {
        BigInteger p = new BigInteger(bitLength, rnd).setBit(bitLength - 1);
        int n = p.mag.length - 1;
        p.mag[n] = p.mag[n] & 0xFFFFFFFE;
        int searchLen = BigInteger.getPrimeSearchLen(bitLength);
        BitSieve searchSieve = new BitSieve(p, searchLen);
        BigInteger candidate = searchSieve.retrieve(p, certainty, rnd);
        while (candidate == null || candidate.bitLength() != bitLength) {
            if ((p = p.add(BigInteger.valueOf(2 * searchLen))).bitLength() != bitLength) {
                p = new BigInteger(bitLength, rnd).setBit(bitLength - 1);
            }
            int n2 = p.mag.length - 1;
            p.mag[n2] = p.mag[n2] & 0xFFFFFFFE;
            searchSieve = new BitSieve(p, searchLen);
            candidate = searchSieve.retrieve(p, certainty, rnd);
        }
        return candidate;
    }

    public BigInteger nextProbablePrime() {
        if (this.signum < 0) {
            throw new ArithmeticException("start < 0: " + this);
        }
        if (this.signum == 0 || this.equals(ONE)) {
            return TWO;
        }
        BigInteger result = this.add(ONE);
        if (result.bitLength() < 95) {
            if (!result.testBit(0)) {
                result = result.add(ONE);
            }
            while (true) {
                long r;
                if (result.bitLength() > 6 && ((r = result.remainder(SMALL_PRIME_PRODUCT).longValue()) % 3L == 0L || r % 5L == 0L || r % 7L == 0L || r % 11L == 0L || r % 13L == 0L || r % 17L == 0L || r % 19L == 0L || r % 23L == 0L || r % 29L == 0L || r % 31L == 0L || r % 37L == 0L || r % 41L == 0L)) {
                    result = result.add(TWO);
                    continue;
                }
                if (result.bitLength() < 4) {
                    return result;
                }
                if (result.primeToCertainty(100, null)) {
                    return result;
                }
                result = result.add(TWO);
            }
        }
        if (result.testBit(0)) {
            result = result.subtract(ONE);
        }
        int searchLen = BigInteger.getPrimeSearchLen(result.bitLength());
        BitSieve searchSieve;
        BigInteger candidate;
        while ((candidate = (searchSieve = new BitSieve(result, searchLen)).retrieve(result, 100, null)) == null) {
            result = result.add(BigInteger.valueOf(2 * searchLen));
        }
        return candidate;
    }

    private static int getPrimeSearchLen(int bitLength) {
        if (bitLength > 500000001) {
            throw new ArithmeticException("Prime search implementation restriction on bitLength");
        }
        return bitLength / 20 * 64;
    }

    boolean primeToCertainty(int certainty, Random random) {
        int rounds = 0;
        int n = (Math.min(certainty, 0x7FFFFFFE) + 1) / 2;
        int sizeInBits = this.bitLength();
        if (sizeInBits < 100) {
            rounds = 50;
            rounds = n < rounds ? n : rounds;
            return this.passesMillerRabin(rounds, random);
        }
        rounds = sizeInBits < 256 ? 27 : (sizeInBits < 512 ? 15 : (sizeInBits < 768 ? 8 : (sizeInBits < 1024 ? 4 : 2)));
        rounds = n < rounds ? n : rounds;
        return this.passesMillerRabin(rounds, random) && this.passesLucasLehmer();
    }

    private boolean passesLucasLehmer() {
        BigInteger thisPlusOne = this.add(ONE);
        int d = 5;
        while (BigInteger.jacobiSymbol(d, this) != -1) {
            d = d < 0 ? Math.abs(d) + 2 : -(d + 2);
        }
        BigInteger u = BigInteger.lucasLehmerSequence(d, thisPlusOne, this);
        return u.mod(this).equals(ZERO);
    }

    private static int jacobiSymbol(int p, BigInteger n) {
        if (p == 0) {
            return 0;
        }
        int j = 1;
        int u = n.mag[n.mag.length - 1];
        if (p < 0) {
            p = -p;
            int n8 = u & 7;
            if (n8 == 3 || n8 == 7) {
                j = -j;
            }
        }
        while ((p & 3) == 0) {
            p >>= 2;
        }
        if ((p & 1) == 0) {
            p >>= 1;
            if (((u ^ u >> 1) & 2) != 0) {
                j = -j;
            }
        }
        if (p == 1) {
            return j;
        }
        if ((p & u & 2) != 0) {
            j = -j;
        }
        for (u = n.mod(BigInteger.valueOf(p)).intValue(); u != 0; u %= p) {
            while ((u & 3) == 0) {
                u >>= 2;
            }
            if ((u & 1) == 0) {
                u >>= 1;
                if (((p ^ p >> 1) & 2) != 0) {
                    j = -j;
                }
            }
            if (u == 1) {
                return j;
            }
            assert (u < p);
            u = p;
            int t = u;
            if ((u & (p = t) & 2) == 0) continue;
            j = -j;
        }
        return 0;
    }

    private static BigInteger lucasLehmerSequence(int z, BigInteger k, BigInteger n) {
        BigInteger d = BigInteger.valueOf(z);
        BigInteger u = ONE;
        BigInteger v = ONE;
        for (int i = k.bitLength() - 2; i >= 0; --i) {
            BigInteger u2 = u.multiply(v).mod(n);
            BigInteger v2 = v.square().add(d.multiply(u.square())).mod(n);
            if (v2.testBit(0)) {
                v2 = v2.subtract(n);
            }
            v2 = v2.shiftRight(1);
            u = u2;
            v = v2;
            if (!k.testBit(i)) continue;
            u2 = u.add(v).mod(n);
            if (u2.testBit(0)) {
                u2 = u2.subtract(n);
            }
            u2 = u2.shiftRight(1);
            v2 = v.add(d.multiply(u)).mod(n);
            if (v2.testBit(0)) {
                v2 = v2.subtract(n);
            }
            v2 = v2.shiftRight(1);
            u = u2;
            v = v2;
        }
        return u;
    }

    private boolean passesMillerRabin(int iterations, Random rnd) {
        BigInteger thisMinusOne;
        BigInteger m = thisMinusOne = this.subtract(ONE);
        int a = m.getLowestSetBit();
        m = m.shiftRight(a);
        if (rnd == null) {
            rnd = ThreadLocalRandom.current();
        }
        for (int i = 0; i < iterations; ++i) {
            BigInteger b;
            while ((b = new BigInteger(this.bitLength(), rnd)).compareTo(ONE) <= 0 || b.compareTo(this) >= 0) {
            }
            int j = 0;
            BigInteger z = b.modPow(m, this);
            while (!(j == 0 && z.equals(ONE) || z.equals(thisMinusOne))) {
                if (j > 0 && z.equals(ONE) || ++j == a) {
                    return false;
                }
                z = z.modPow(TWO, this);
            }
        }
        return true;
    }

    BigInteger(int[] magnitude, int signum) {
        this.signum = magnitude.length == 0 ? 0 : signum;
        this.mag = magnitude;
        if (this.mag.length >= 0x4000000) {
            this.checkRange();
        }
    }

    private BigInteger(byte[] magnitude, int signum) {
        this.signum = magnitude.length == 0 ? 0 : signum;
        this.mag = BigInteger.stripLeadingZeroBytes(magnitude, 0, magnitude.length);
        if (this.mag.length >= 0x4000000) {
            this.checkRange();
        }
    }

    private void checkRange() {
        if (this.mag.length > 0x4000000 || this.mag.length == 0x4000000 && this.mag[0] < 0) {
            BigInteger.reportOverflow();
        }
    }

    private static void reportOverflow() {
        throw new ArithmeticException("BigInteger would overflow supported range");
    }

    public static BigInteger valueOf(long val) {
        if (val == 0L) {
            return ZERO;
        }
        if (val > 0L && val <= 16L) {
            return posConst[(int)val];
        }
        if (val < 0L && val >= -16L) {
            return negConst[(int)(-val)];
        }
        return new BigInteger(val);
    }

    private BigInteger(long val) {
        if (val < 0L) {
            val = -val;
            this.signum = -1;
        } else {
            this.signum = 1;
        }
        int highWord = (int)(val >>> 32);
        if (highWord == 0) {
            this.mag = new int[1];
            this.mag[0] = (int)val;
        } else {
            this.mag = new int[2];
            this.mag[0] = highWord;
            this.mag[1] = (int)val;
        }
    }

    private static BigInteger valueOf(int[] val) {
        return val[0] > 0 ? new BigInteger(val, 1) : new BigInteger(val);
    }

    public BigInteger add(BigInteger val) {
        if (val.signum == 0) {
            return this;
        }
        if (this.signum == 0) {
            return val;
        }
        if (val.signum == this.signum) {
            return new BigInteger(BigInteger.add(this.mag, val.mag), this.signum);
        }
        int cmp = this.compareMagnitude(val);
        if (cmp == 0) {
            return ZERO;
        }
        int[] resultMag = cmp > 0 ? BigInteger.subtract(this.mag, val.mag) : BigInteger.subtract(val.mag, this.mag);
        resultMag = BigInteger.trustedStripLeadingZeroInts(resultMag);
        return new BigInteger(resultMag, cmp == this.signum ? 1 : -1);
    }

    BigInteger add(long val) {
        if (val == 0L) {
            return this;
        }
        if (this.signum == 0) {
            return BigInteger.valueOf(val);
        }
        if (Long.signum(val) == this.signum) {
            return new BigInteger(BigInteger.add(this.mag, Math.abs(val)), this.signum);
        }
        int cmp = this.compareMagnitude(val);
        if (cmp == 0) {
            return ZERO;
        }
        int[] resultMag = cmp > 0 ? BigInteger.subtract(this.mag, Math.abs(val)) : BigInteger.subtract(Math.abs(val), this.mag);
        resultMag = BigInteger.trustedStripLeadingZeroInts(resultMag);
        return new BigInteger(resultMag, cmp == this.signum ? 1 : -1);
    }

    private static int[] add(int[] x, long val) {
        boolean carry;
        int[] result;
        long sum = 0L;
        int xIndex = x.length;
        int highWord = (int)(val >>> 32);
        if (highWord == 0) {
            result = new int[xIndex];
            sum = ((long)x[--xIndex] & 0xFFFFFFFFL) + val;
            result[xIndex] = (int)sum;
        } else {
            if (xIndex == 1) {
                int[] result2 = new int[2];
                sum = val + ((long)x[0] & 0xFFFFFFFFL);
                result2[1] = (int)sum;
                result2[0] = (int)(sum >>> 32);
                return result2;
            }
            result = new int[xIndex];
            sum = ((long)x[--xIndex] & 0xFFFFFFFFL) + (val & 0xFFFFFFFFL);
            result[xIndex] = (int)sum;
            sum = ((long)x[--xIndex] & 0xFFFFFFFFL) + ((long)highWord & 0xFFFFFFFFL) + (sum >>> 32);
            result[xIndex] = (int)sum;
        }
        boolean bl = carry = sum >>> 32 != 0L;
        while (xIndex > 0 && carry) {
            result[--xIndex] = x[xIndex] + 1;
            carry = result[--xIndex] == 0;
        }
        while (xIndex > 0) {
            result[--xIndex] = x[xIndex];
        }
        if (carry) {
            int[] bigger = new int[result.length + 1];
            System.arraycopy(result, 0, bigger, 1, result.length);
            bigger[0] = 1;
            return bigger;
        }
        return result;
    }

    private static int[] add(int[] x, int[] y) {
        boolean carry;
        if (x.length < y.length) {
            int[] tmp = x;
            x = y;
            y = tmp;
        }
        int xIndex = x.length;
        int yIndex = y.length;
        int[] result = new int[xIndex];
        long sum = 0L;
        if (yIndex == 1) {
            sum = ((long)x[--xIndex] & 0xFFFFFFFFL) + ((long)y[0] & 0xFFFFFFFFL);
            result[xIndex] = (int)sum;
        } else {
            while (yIndex > 0) {
                sum = ((long)x[--xIndex] & 0xFFFFFFFFL) + ((long)y[--yIndex] & 0xFFFFFFFFL) + (sum >>> 32);
                result[xIndex] = (int)sum;
            }
        }
        boolean bl = carry = sum >>> 32 != 0L;
        while (xIndex > 0 && carry) {
            result[--xIndex] = x[xIndex] + 1;
            carry = result[--xIndex] == 0;
        }
        while (xIndex > 0) {
            result[--xIndex] = x[xIndex];
        }
        if (carry) {
            int[] bigger = new int[result.length + 1];
            System.arraycopy(result, 0, bigger, 1, result.length);
            bigger[0] = 1;
            return bigger;
        }
        return result;
    }

    private static int[] subtract(long val, int[] little) {
        int highWord = (int)(val >>> 32);
        if (highWord == 0) {
            int[] result = new int[]{(int)(val - ((long)little[0] & 0xFFFFFFFFL))};
            return result;
        }
        int[] result = new int[2];
        if (little.length == 1) {
            long difference = ((long)((int)val) & 0xFFFFFFFFL) - ((long)little[0] & 0xFFFFFFFFL);
            result[1] = (int)difference;
            boolean borrow = difference >> 32 != 0L;
            result[0] = borrow ? highWord - 1 : highWord;
            return result;
        }
        long difference = ((long)((int)val) & 0xFFFFFFFFL) - ((long)little[1] & 0xFFFFFFFFL);
        result[1] = (int)difference;
        difference = ((long)highWord & 0xFFFFFFFFL) - ((long)little[0] & 0xFFFFFFFFL) + (difference >> 32);
        result[0] = (int)difference;
        return result;
    }

    private static int[] subtract(int[] big, long val) {
        boolean borrow;
        int highWord = (int)(val >>> 32);
        int bigIndex = big.length;
        int[] result = new int[bigIndex];
        long difference = 0L;
        if (highWord == 0) {
            difference = ((long)big[--bigIndex] & 0xFFFFFFFFL) - val;
            result[bigIndex] = (int)difference;
        } else {
            difference = ((long)big[--bigIndex] & 0xFFFFFFFFL) - (val & 0xFFFFFFFFL);
            result[bigIndex] = (int)difference;
            difference = ((long)big[--bigIndex] & 0xFFFFFFFFL) - ((long)highWord & 0xFFFFFFFFL) + (difference >> 32);
            result[bigIndex] = (int)difference;
        }
        boolean bl = borrow = difference >> 32 != 0L;
        while (bigIndex > 0 && borrow) {
            result[--bigIndex] = big[bigIndex] - 1;
            borrow = result[--bigIndex] == -1;
        }
        while (bigIndex > 0) {
            result[--bigIndex] = big[bigIndex];
        }
        return result;
    }

    public BigInteger subtract(BigInteger val) {
        if (val.signum == 0) {
            return this;
        }
        if (this.signum == 0) {
            return val.negate();
        }
        if (val.signum != this.signum) {
            return new BigInteger(BigInteger.add(this.mag, val.mag), this.signum);
        }
        int cmp = this.compareMagnitude(val);
        if (cmp == 0) {
            return ZERO;
        }
        int[] resultMag = cmp > 0 ? BigInteger.subtract(this.mag, val.mag) : BigInteger.subtract(val.mag, this.mag);
        resultMag = BigInteger.trustedStripLeadingZeroInts(resultMag);
        return new BigInteger(resultMag, cmp == this.signum ? 1 : -1);
    }

    private static int[] subtract(int[] big, int[] little) {
        boolean borrow;
        int bigIndex = big.length;
        int[] result = new int[bigIndex];
        int littleIndex = little.length;
        long difference = 0L;
        while (littleIndex > 0) {
            difference = ((long)big[--bigIndex] & 0xFFFFFFFFL) - ((long)little[--littleIndex] & 0xFFFFFFFFL) + (difference >> 32);
            result[bigIndex] = (int)difference;
        }
        boolean bl = borrow = difference >> 32 != 0L;
        while (bigIndex > 0 && borrow) {
            result[--bigIndex] = big[bigIndex] - 1;
            borrow = result[--bigIndex] == -1;
        }
        while (bigIndex > 0) {
            result[--bigIndex] = big[bigIndex];
        }
        return result;
    }

    public BigInteger multiply(BigInteger val) {
        return this.multiply(val, false, false, 0);
    }

    public BigInteger parallelMultiply(BigInteger val) {
        return this.multiply(val, false, true, 0);
    }

    private BigInteger multiply(BigInteger val, boolean isRecursion, boolean parallel, int depth) {
        if (val.signum == 0 || this.signum == 0) {
            return ZERO;
        }
        int xlen = this.mag.length;
        if (val == this && xlen > 20) {
            return this.square(true, parallel, depth);
        }
        int ylen = val.mag.length;
        if (xlen < 80 || ylen < 80) {
            int resultSign;
            int n = resultSign = this.signum == val.signum ? 1 : -1;
            if (val.mag.length == 1) {
                return BigInteger.multiplyByInt(this.mag, val.mag[0], resultSign);
            }
            if (this.mag.length == 1) {
                return BigInteger.multiplyByInt(val.mag, this.mag[0], resultSign);
            }
            int[] result = BigInteger.multiplyToLen(this.mag, xlen, val.mag, ylen, null);
            result = BigInteger.trustedStripLeadingZeroInts(result);
            return new BigInteger(result, resultSign);
        }
        if (xlen < 240 && ylen < 240) {
            return BigInteger.multiplyKaratsuba(this, val);
        }
        if (!isRecursion && (long)BigInteger.bitLength(this.mag, this.mag.length) + (long)BigInteger.bitLength(val.mag, val.mag.length) > 0x80000000L) {
            BigInteger.reportOverflow();
        }
        return BigInteger.multiplyToomCook3(this, val, parallel, depth);
    }

    private static BigInteger multiplyByInt(int[] x, int y, int sign) {
        if (Integer.bitCount(y) == 1) {
            return new BigInteger(BigInteger.shiftLeft(x, Integer.numberOfTrailingZeros(y)), sign);
        }
        int xlen = x.length;
        int[] rmag = new int[xlen + 1];
        long carry = 0L;
        long yl = (long)y & 0xFFFFFFFFL;
        int rstart = rmag.length - 1;
        for (int i = xlen - 1; i >= 0; --i) {
            long product = ((long)x[i] & 0xFFFFFFFFL) * yl + carry;
            rmag[rstart--] = (int)product;
            carry = product >>> 32;
        }
        if (carry == 0L) {
            rmag = Arrays.copyOfRange(rmag, 1, rmag.length);
        } else {
            rmag[rstart] = (int)carry;
        }
        return new BigInteger(rmag, sign);
    }

    BigInteger multiply(long v) {
        long product;
        int i;
        int rsign;
        if (v == 0L || this.signum == 0) {
            return ZERO;
        }
        if (v == Long.MIN_VALUE) {
            return this.multiply(BigInteger.valueOf(v));
        }
        int n = rsign = v > 0L ? this.signum : -this.signum;
        if (v < 0L) {
            v = -v;
        }
        long dh = v >>> 32;
        long dl = v & 0xFFFFFFFFL;
        int xlen = this.mag.length;
        int[] value = this.mag;
        int[] rmag = dh == 0L ? new int[xlen + 1] : new int[xlen + 2];
        long carry = 0L;
        int rstart = rmag.length - 1;
        for (i = xlen - 1; i >= 0; --i) {
            product = ((long)value[i] & 0xFFFFFFFFL) * dl + carry;
            rmag[rstart--] = (int)product;
            carry = product >>> 32;
        }
        rmag[rstart] = (int)carry;
        if (dh != 0L) {
            carry = 0L;
            rstart = rmag.length - 2;
            for (i = xlen - 1; i >= 0; --i) {
                product = ((long)value[i] & 0xFFFFFFFFL) * dh + ((long)rmag[rstart] & 0xFFFFFFFFL) + carry;
                rmag[rstart--] = (int)product;
                carry = product >>> 32;
            }
            rmag[0] = (int)carry;
        }
        if (carry == 0L) {
            rmag = Arrays.copyOfRange(rmag, 1, rmag.length);
        }
        return new BigInteger(rmag, rsign);
    }

    private static int[] multiplyToLen(int[] x, int xlen, int[] y, int ylen, int[] z) {
        BigInteger.multiplyToLenCheck(x, xlen);
        BigInteger.multiplyToLenCheck(y, ylen);
        return BigInteger.implMultiplyToLen(x, xlen, y, ylen, z);
    }

    @IntrinsicCandidate
    private static int[] implMultiplyToLen(int[] x, int xlen, int[] y, int ylen, int[] z) {
        int xstart = xlen - 1;
        int ystart = ylen - 1;
        if (z == null || z.length < xlen + ylen) {
            z = new int[xlen + ylen];
        }
        long carry = 0L;
        int j = ystart;
        int k = ystart + 1 + xstart;
        while (j >= 0) {
            long product = ((long)y[j] & 0xFFFFFFFFL) * ((long)x[xstart] & 0xFFFFFFFFL) + carry;
            z[k] = (int)product;
            carry = product >>> 32;
            --j;
            --k;
        }
        z[xstart] = (int)carry;
        for (int i = xstart - 1; i >= 0; --i) {
            carry = 0L;
            int j2 = ystart;
            int k2 = ystart + 1 + i;
            while (j2 >= 0) {
                long product = ((long)y[j2] & 0xFFFFFFFFL) * ((long)x[i] & 0xFFFFFFFFL) + ((long)z[k2] & 0xFFFFFFFFL) + carry;
                z[k2] = (int)product;
                carry = product >>> 32;
                --j2;
                --k2;
            }
            z[i] = (int)carry;
        }
        return z;
    }

    private static void multiplyToLenCheck(int[] array, int length) {
        if (length <= 0) {
            return;
        }
        Objects.requireNonNull(array);
        if (length > array.length) {
            throw new ArrayIndexOutOfBoundsException(length - 1);
        }
    }

    private static BigInteger multiplyKaratsuba(BigInteger x, BigInteger y) {
        int xlen = x.mag.length;
        int ylen = y.mag.length;
        int half = (Math.max(xlen, ylen) + 1) / 2;
        BigInteger xl = x.getLower(half);
        BigInteger xh = x.getUpper(half);
        BigInteger yl = y.getLower(half);
        BigInteger yh = y.getUpper(half);
        BigInteger p1 = xh.multiply(yh);
        BigInteger p2 = xl.multiply(yl);
        BigInteger p3 = xh.add(xl).multiply(yh.add(yl));
        BigInteger result = p1.shiftLeft(32 * half).add(p3.subtract(p1).subtract(p2)).shiftLeft(32 * half).add(p2);
        if (x.signum != y.signum) {
            return result.negate();
        }
        return result;
    }

    private static BigInteger multiplyToomCook3(BigInteger a, BigInteger b, boolean parallel, int depth) {
        int alen = a.mag.length;
        int blen = b.mag.length;
        int largest = Math.max(alen, blen);
        int k = (largest + 2) / 3;
        int r = largest - 2 * k;
        BigInteger a2 = a.getToomSlice(k, r, 0, largest);
        BigInteger a1 = a.getToomSlice(k, r, 1, largest);
        BigInteger a0 = a.getToomSlice(k, r, 2, largest);
        BigInteger b2 = b.getToomSlice(k, r, 0, largest);
        BigInteger b1 = b.getToomSlice(k, r, 1, largest);
        BigInteger b0 = b.getToomSlice(k, r, 2, largest);
        RecursiveTask<BigInteger> v0_task = RecursiveOp.multiply(a0, b0, parallel, ++depth);
        BigInteger da1 = a2.add(a0);
        BigInteger db1 = b2.add(b0);
        RecursiveTask<BigInteger> vm1_task = RecursiveOp.multiply(da1.subtract(a1), db1.subtract(b1), parallel, depth);
        da1 = da1.add(a1);
        db1 = db1.add(b1);
        RecursiveTask<BigInteger> v1_task = RecursiveOp.multiply(da1, db1, parallel, depth);
        BigInteger v2 = da1.add(a2).shiftLeft(1).subtract(a0).multiply(db1.add(b2).shiftLeft(1).subtract(b0), true, parallel, depth);
        BigInteger vinf = a2.multiply(b2, true, parallel, depth);
        BigInteger v0 = (BigInteger)v0_task.join();
        BigInteger vm1 = (BigInteger)vm1_task.join();
        BigInteger v1 = (BigInteger)v1_task.join();
        BigInteger t2 = v2.subtract(vm1).exactDivideBy3();
        BigInteger tm1 = v1.subtract(vm1).shiftRight(1);
        BigInteger t1 = v1.subtract(v0);
        t2 = t2.subtract(t1).shiftRight(1);
        t1 = t1.subtract(tm1).subtract(vinf);
        t2 = t2.subtract(vinf.shiftLeft(1));
        tm1 = tm1.subtract(t2);
        int ss = k * 32;
        BigInteger result = vinf.shiftLeft(ss).add(t2).shiftLeft(ss).add(t1).shiftLeft(ss).add(tm1).shiftLeft(ss).add(v0);
        if (a.signum != b.signum) {
            return result.negate();
        }
        return result;
    }

    private BigInteger getToomSlice(int lowerSize, int upperSize, int slice, int fullsize) {
        int end;
        int start;
        int len = this.mag.length;
        int offset = fullsize - len;
        if (slice == 0) {
            start = 0 - offset;
            end = upperSize - 1 - offset;
        } else {
            start = upperSize + (slice - 1) * lowerSize - offset;
            end = start + lowerSize - 1;
        }
        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            return ZERO;
        }
        int sliceSize = end - start + 1;
        if (sliceSize <= 0) {
            return ZERO;
        }
        if (start == 0 && sliceSize >= len) {
            return this.abs();
        }
        int[] intSlice = new int[sliceSize];
        System.arraycopy(this.mag, start, intSlice, 0, sliceSize);
        return new BigInteger(BigInteger.trustedStripLeadingZeroInts(intSlice), 1);
    }

    private BigInteger exactDivideBy3() {
        int len = this.mag.length;
        int[] result = new int[len];
        long borrow = 0L;
        for (int i = len - 1; i >= 0; --i) {
            long x = (long)this.mag[i] & 0xFFFFFFFFL;
            long w = x - borrow;
            borrow = borrow > x ? 1L : 0L;
            long q = w * 0xAAAAAAABL & 0xFFFFFFFFL;
            result[i] = (int)q;
            if (q < 0x55555556L) continue;
            ++borrow;
            if (q < 0xAAAAAAABL) continue;
            ++borrow;
        }
        result = BigInteger.trustedStripLeadingZeroInts(result);
        return new BigInteger(result, this.signum);
    }

    private BigInteger getLower(int n) {
        int len = this.mag.length;
        if (len <= n) {
            return this.abs();
        }
        int[] lowerInts = new int[n];
        System.arraycopy(this.mag, len - n, lowerInts, 0, n);
        return new BigInteger(BigInteger.trustedStripLeadingZeroInts(lowerInts), 1);
    }

    private BigInteger getUpper(int n) {
        int len = this.mag.length;
        if (len <= n) {
            return ZERO;
        }
        int upperLen = len - n;
        int[] upperInts = new int[upperLen];
        System.arraycopy(this.mag, 0, upperInts, 0, upperLen);
        return new BigInteger(BigInteger.trustedStripLeadingZeroInts(upperInts), 1);
    }

    private BigInteger square() {
        return this.square(false, false, 0);
    }

    private BigInteger square(boolean isRecursion, boolean parallel, int depth) {
        if (this.signum == 0) {
            return ZERO;
        }
        int len = this.mag.length;
        if (len < 128) {
            int[] z = BigInteger.squareToLen(this.mag, len, null);
            return new BigInteger(BigInteger.trustedStripLeadingZeroInts(z), 1);
        }
        if (len < 216) {
            return this.squareKaratsuba();
        }
        if (!isRecursion && (long)BigInteger.bitLength(this.mag, this.mag.length) > 0x40000000L) {
            BigInteger.reportOverflow();
        }
        return this.squareToomCook3(parallel, depth);
    }

    private static final int[] squareToLen(int[] x, int len, int[] z) {
        int zlen = len << 1;
        if (z == null || z.length < zlen) {
            z = new int[zlen];
        }
        BigInteger.implSquareToLenChecks(x, len, z, zlen);
        return BigInteger.implSquareToLen(x, len, z, zlen);
    }

    private static void implSquareToLenChecks(int[] x, int len, int[] z, int zlen) throws RuntimeException {
        if (len < 1) {
            throw new IllegalArgumentException("invalid input length: " + len);
        }
        if (len > x.length) {
            throw new IllegalArgumentException("input length out of bound: " + len + " > " + x.length);
        }
        if (len * 2 > z.length) {
            throw new IllegalArgumentException("input length out of bound: " + len * 2 + " > " + z.length);
        }
        if (zlen < 1) {
            throw new IllegalArgumentException("invalid input length: " + zlen);
        }
        if (zlen > z.length) {
            throw new IllegalArgumentException("input length out of bound: " + len + " > " + z.length);
        }
    }

    @IntrinsicCandidate
    private static final int[] implSquareToLen(int[] x, int len, int[] z, int zlen) {
        int lastProductLowWord = 0;
        int i = 0;
        for (int j = 0; j < len; ++j) {
            long piece = (long)x[j] & 0xFFFFFFFFL;
            long product = piece * piece;
            z[i++] = lastProductLowWord << 31 | (int)(product >>> 33);
            z[i++] = (int)(product >>> 1);
            lastProductLowWord = (int)product;
        }
        int i2 = len;
        int offset = 1;
        while (i2 > 0) {
            int t = x[i2 - 1];
            t = BigInteger.mulAdd(z, x, offset, i2 - 1, t);
            BigInteger.addOne(z, offset - 1, i2, t);
            --i2;
            offset += 2;
        }
        BigInteger.primitiveLeftShift(z, zlen, 1);
        int n = zlen - 1;
        z[n] = z[n] | x[len - 1] & 1;
        return z;
    }

    private BigInteger squareKaratsuba() {
        int half = (this.mag.length + 1) / 2;
        BigInteger xl = this.getLower(half);
        BigInteger xh = this.getUpper(half);
        BigInteger xhs = xh.square();
        BigInteger xls = xl.square();
        return xhs.shiftLeft(half * 32).add(xl.add(xh).square().subtract(xhs.add(xls))).shiftLeft(half * 32).add(xls);
    }

    private BigInteger squareToomCook3(boolean parallel, int depth) {
        int len = this.mag.length;
        int k = (len + 2) / 3;
        int r = len - 2 * k;
        BigInteger a2 = this.getToomSlice(k, r, 0, len);
        BigInteger a1 = this.getToomSlice(k, r, 1, len);
        BigInteger a0 = this.getToomSlice(k, r, 2, len);
        RecursiveTask<BigInteger> v0_fork = RecursiveOp.square(a0, parallel, ++depth);
        BigInteger da1 = a2.add(a0);
        RecursiveTask<BigInteger> vm1_fork = RecursiveOp.square(da1.subtract(a1), parallel, depth);
        da1 = da1.add(a1);
        RecursiveTask<BigInteger> v1_fork = RecursiveOp.square(da1, parallel, depth);
        BigInteger vinf = a2.square(true, parallel, depth);
        BigInteger v2 = da1.add(a2).shiftLeft(1).subtract(a0).square(true, parallel, depth);
        BigInteger v0 = (BigInteger)v0_fork.join();
        BigInteger vm1 = (BigInteger)vm1_fork.join();
        BigInteger v1 = (BigInteger)v1_fork.join();
        BigInteger t2 = v2.subtract(vm1).exactDivideBy3();
        BigInteger tm1 = v1.subtract(vm1).shiftRight(1);
        BigInteger t1 = v1.subtract(v0);
        t2 = t2.subtract(t1).shiftRight(1);
        t1 = t1.subtract(tm1).subtract(vinf);
        t2 = t2.subtract(vinf.shiftLeft(1));
        tm1 = tm1.subtract(t2);
        int ss = k * 32;
        return vinf.shiftLeft(ss).add(t2).shiftLeft(ss).add(t1).shiftLeft(ss).add(tm1).shiftLeft(ss).add(v0);
    }

    public BigInteger divide(BigInteger val) {
        if (val.mag.length < 80 || this.mag.length - val.mag.length < 40) {
            return this.divideKnuth(val);
        }
        return this.divideBurnikelZiegler(val);
    }

    private BigInteger divideKnuth(BigInteger val) {
        MutableBigInteger q = new MutableBigInteger();
        MutableBigInteger a = new MutableBigInteger(this.mag);
        MutableBigInteger b = new MutableBigInteger(val.mag);
        a.divideKnuth(b, q, false);
        return q.toBigInteger(this.signum * val.signum);
    }

    public BigInteger[] divideAndRemainder(BigInteger val) {
        if (val.mag.length < 80 || this.mag.length - val.mag.length < 40) {
            return this.divideAndRemainderKnuth(val);
        }
        return this.divideAndRemainderBurnikelZiegler(val);
    }

    private BigInteger[] divideAndRemainderKnuth(BigInteger val) {
        BigInteger[] result = new BigInteger[2];
        MutableBigInteger q = new MutableBigInteger();
        MutableBigInteger a = new MutableBigInteger(this.mag);
        MutableBigInteger b = new MutableBigInteger(val.mag);
        MutableBigInteger r = a.divideKnuth(b, q);
        result[0] = q.toBigInteger(this.signum == val.signum ? 1 : -1);
        result[1] = r.toBigInteger(this.signum);
        return result;
    }

    public BigInteger remainder(BigInteger val) {
        if (val.mag.length < 80 || this.mag.length - val.mag.length < 40) {
            return this.remainderKnuth(val);
        }
        return this.remainderBurnikelZiegler(val);
    }

    private BigInteger remainderKnuth(BigInteger val) {
        MutableBigInteger q = new MutableBigInteger();
        MutableBigInteger a = new MutableBigInteger(this.mag);
        MutableBigInteger b = new MutableBigInteger(val.mag);
        return a.divideKnuth(b, q).toBigInteger(this.signum);
    }

    private BigInteger divideBurnikelZiegler(BigInteger val) {
        return this.divideAndRemainderBurnikelZiegler(val)[0];
    }

    private BigInteger remainderBurnikelZiegler(BigInteger val) {
        return this.divideAndRemainderBurnikelZiegler(val)[1];
    }

    private BigInteger[] divideAndRemainderBurnikelZiegler(BigInteger val) {
        MutableBigInteger q = new MutableBigInteger();
        MutableBigInteger r = new MutableBigInteger(this).divideAndRemainderBurnikelZiegler(new MutableBigInteger(val), q);
        BigInteger qBigInt = q.isZero() ? ZERO : q.toBigInteger(this.signum * val.signum);
        BigInteger rBigInt = r.isZero() ? ZERO : r.toBigInteger(this.signum);
        return new BigInteger[]{qBigInt, rBigInt};
    }

    public BigInteger pow(int exponent) {
        int remainingBits;
        if (exponent < 0) {
            throw new ArithmeticException("Negative exponent");
        }
        if (this.signum == 0) {
            return exponent == 0 ? ONE : this;
        }
        BigInteger partToSquare = this.abs();
        int powersOfTwo = partToSquare.getLowestSetBit();
        long bitsToShiftLong = (long)powersOfTwo * (long)exponent;
        if (bitsToShiftLong > Integer.MAX_VALUE) {
            BigInteger.reportOverflow();
        }
        int bitsToShift = (int)bitsToShiftLong;
        if (powersOfTwo > 0) {
            remainingBits = (partToSquare = partToSquare.shiftRight(powersOfTwo)).bitLength();
            if (remainingBits == 1) {
                if (this.signum < 0 && (exponent & 1) == 1) {
                    return NEGATIVE_ONE.shiftLeft(bitsToShift);
                }
                return ONE.shiftLeft(bitsToShift);
            }
        } else {
            remainingBits = partToSquare.bitLength();
            if (remainingBits == 1) {
                if (this.signum < 0 && (exponent & 1) == 1) {
                    return NEGATIVE_ONE;
                }
                return ONE;
            }
        }
        long scaleFactor = (long)remainingBits * (long)exponent;
        if (partToSquare.mag.length == 1 && scaleFactor <= 62L) {
            int newSign = this.signum < 0 && (exponent & 1) == 1 ? -1 : 1;
            long result = 1L;
            long baseToPow2 = (long)partToSquare.mag[0] & 0xFFFFFFFFL;
            int workingExponent = exponent;
            while (workingExponent != 0) {
                if ((workingExponent & 1) == 1) {
                    result *= baseToPow2;
                }
                if ((workingExponent >>>= 1) == 0) continue;
                baseToPow2 *= baseToPow2;
            }
            if (powersOfTwo > 0) {
                if ((long)bitsToShift + scaleFactor <= 62L) {
                    return BigInteger.valueOf((result << bitsToShift) * (long)newSign);
                }
                return BigInteger.valueOf(result * (long)newSign).shiftLeft(bitsToShift);
            }
            return BigInteger.valueOf(result * (long)newSign);
        }
        if ((long)this.bitLength() * (long)exponent / 32L > 0x4000000L) {
            BigInteger.reportOverflow();
        }
        BigInteger answer = ONE;
        int workingExponent = exponent;
        while (workingExponent != 0) {
            if ((workingExponent & 1) == 1) {
                answer = answer.multiply(partToSquare);
            }
            if ((workingExponent >>>= 1) == 0) continue;
            partToSquare = partToSquare.square();
        }
        if (powersOfTwo > 0) {
            answer = answer.shiftLeft(bitsToShift);
        }
        if (this.signum < 0 && (exponent & 1) == 1) {
            return answer.negate();
        }
        return answer;
    }

    public BigInteger sqrt() {
        if (this.signum < 0) {
            throw new ArithmeticException("Negative BigInteger");
        }
        return new MutableBigInteger(this.mag).sqrt().toBigInteger();
    }

    public BigInteger[] sqrtAndRemainder() {
        BigInteger s = this.sqrt();
        BigInteger r = this.subtract(s.square());
        assert (r.compareTo(ZERO) >= 0);
        return new BigInteger[]{s, r};
    }

    public BigInteger gcd(BigInteger val) {
        if (val.signum == 0) {
            return this.abs();
        }
        if (this.signum == 0) {
            return val.abs();
        }
        MutableBigInteger a = new MutableBigInteger(this);
        MutableBigInteger b = new MutableBigInteger(val);
        MutableBigInteger result = a.hybridGCD(b);
        return result.toBigInteger(1);
    }

    static int bitLengthForInt(int n) {
        return 32 - Integer.numberOfLeadingZeros(n);
    }

    private static int[] leftShift(int[] a, int len, int n) {
        int nInts = n >>> 5;
        int nBits = n & 0x1F;
        int bitsInHighWord = BigInteger.bitLengthForInt(a[0]);
        if (n <= 32 - bitsInHighWord) {
            BigInteger.primitiveLeftShift(a, len, nBits);
            return a;
        }
        if (nBits <= 32 - bitsInHighWord) {
            int[] result = new int[nInts + len];
            System.arraycopy(a, 0, result, 0, len);
            BigInteger.primitiveLeftShift(result, result.length, nBits);
            return result;
        }
        int[] result = new int[nInts + len + 1];
        System.arraycopy(a, 0, result, 0, len);
        BigInteger.primitiveRightShift(result, result.length, 32 - nBits);
        return result;
    }

    static void primitiveRightShift(int[] a, int len, int n) {
        Objects.checkFromToIndex(0, len, a.length);
        BigInteger.shiftRightImplWorker(a, a, 1, n, len - 1);
        a[0] = a[0] >>> n;
    }

    static void primitiveLeftShift(int[] a, int len, int n) {
        if (len == 0 || n == 0) {
            return;
        }
        Objects.checkFromToIndex(0, len, a.length);
        BigInteger.shiftLeftImplWorker(a, a, 0, n, len - 1);
        int n2 = len - 1;
        a[n2] = a[n2] << n;
    }

    private static int bitLength(int[] val, int len) {
        if (len == 0) {
            return 0;
        }
        return (len - 1 << 5) + BigInteger.bitLengthForInt(val[0]);
    }

    public BigInteger abs() {
        return this.signum >= 0 ? this : this.negate();
    }

    public BigInteger negate() {
        return new BigInteger(this.mag, -this.signum);
    }

    public int signum() {
        return this.signum;
    }

    public BigInteger mod(BigInteger m) {
        if (m.signum <= 0) {
            throw new ArithmeticException("BigInteger: modulus not positive");
        }
        BigInteger result = this.remainder(m);
        return result.signum >= 0 ? result : result.add(m);
    }

    public BigInteger modPow(BigInteger exponent, BigInteger m) {
        BigInteger result;
        BigInteger base;
        if (m.signum <= 0) {
            throw new ArithmeticException("BigInteger: modulus not positive");
        }
        if (exponent.signum == 0) {
            return m.equals(ONE) ? ZERO : ONE;
        }
        if (this.equals(ONE)) {
            return m.equals(ONE) ? ZERO : ONE;
        }
        if (this.equals(ZERO) && exponent.signum >= 0) {
            return ZERO;
        }
        if (this.equals(negConst[1]) && !exponent.testBit(0)) {
            return m.equals(ONE) ? ZERO : ONE;
        }
        boolean invertResult = exponent.signum < 0;
        if (invertResult) {
            exponent = exponent.negate();
        }
        BigInteger bigInteger = base = this.signum < 0 || this.compareTo(m) >= 0 ? this.mod(m) : this;
        if (m.testBit(0)) {
            result = base.oddModPow(exponent, m);
        } else {
            int p = m.getLowestSetBit();
            BigInteger m1 = m.shiftRight(p);
            BigInteger m2 = ONE.shiftLeft(p);
            BigInteger base2 = this.signum < 0 || this.compareTo(m1) >= 0 ? this.mod(m1) : this;
            BigInteger a1 = m1.equals(ONE) ? ZERO : base2.oddModPow(exponent, m1);
            BigInteger a2 = base.modPow2(exponent, p);
            BigInteger y1 = m2.modInverse(m1);
            BigInteger y2 = m1.modInverse(m2);
            if (m.mag.length < 0x2000000) {
                result = a1.multiply(m2).multiply(y1).add(a2.multiply(m1).multiply(y2)).mod(m);
            } else {
                MutableBigInteger t1 = new MutableBigInteger();
                new MutableBigInteger(a1.multiply(m2)).multiply(new MutableBigInteger(y1), t1);
                MutableBigInteger t2 = new MutableBigInteger();
                new MutableBigInteger(a2.multiply(m1)).multiply(new MutableBigInteger(y2), t2);
                t1.add(t2);
                MutableBigInteger q = new MutableBigInteger();
                result = t1.divide(new MutableBigInteger(m), q).toBigInteger();
            }
        }
        return invertResult ? result.modInverse(m) : result;
    }

    private static int[] montgomeryMultiply(int[] a, int[] b, int[] n, int len, long inv, int[] product) {
        BigInteger.implMontgomeryMultiplyChecks(a, b, n, len, product);
        if (len > 512) {
            product = BigInteger.multiplyToLen(a, len, b, len, product);
            return BigInteger.montReduce(product, n, len, (int)inv);
        }
        return BigInteger.implMontgomeryMultiply(a, b, n, len, inv, BigInteger.materialize(product, len));
    }

    private static int[] montgomerySquare(int[] a, int[] n, int len, long inv, int[] product) {
        BigInteger.implMontgomeryMultiplyChecks(a, a, n, len, product);
        if (len > 512) {
            product = BigInteger.squareToLen(a, len, product);
            return BigInteger.montReduce(product, n, len, (int)inv);
        }
        return BigInteger.implMontgomerySquare(a, n, len, inv, BigInteger.materialize(product, len));
    }

    private static void implMontgomeryMultiplyChecks(int[] a, int[] b, int[] n, int len, int[] product) throws RuntimeException {
        if (len % 2 != 0) {
            throw new IllegalArgumentException("input array length must be even: " + len);
        }
        if (len < 1) {
            throw new IllegalArgumentException("invalid input length: " + len);
        }
        if (len > a.length || len > b.length || len > n.length || product != null && len > product.length) {
            throw new IllegalArgumentException("input array length out of bound: " + len);
        }
    }

    private static int[] materialize(int[] z, int len) {
        if (z == null || z.length < len) {
            z = new int[len];
        }
        return z;
    }

    @IntrinsicCandidate
    private static int[] implMontgomeryMultiply(int[] a, int[] b, int[] n, int len, long inv, int[] product) {
        product = BigInteger.multiplyToLen(a, len, b, len, product);
        return BigInteger.montReduce(product, n, len, (int)inv);
    }

    @IntrinsicCandidate
    private static int[] implMontgomerySquare(int[] a, int[] n, int len, long inv, int[] product) {
        product = BigInteger.squareToLen(a, len, product);
        return BigInteger.montReduce(product, n, len, (int)inv);
    }

    private BigInteger oddModPow(BigInteger y, BigInteger z) {
        if (y.equals(ONE)) {
            return this;
        }
        if (this.signum == 0) {
            return ZERO;
        }
        int[] base = (int[])this.mag.clone();
        int[] exp = y.mag;
        int[] mod = z.mag;
        int modLen = mod.length;
        if ((modLen & 1) != 0) {
            int[] x = new int[modLen + 1];
            System.arraycopy(mod, 0, x, 1, modLen);
            mod = x;
            ++modLen;
        }
        int wbits = 0;
        int ebits = BigInteger.bitLength(exp, exp.length);
        if (ebits != 17 || exp[0] != 65537) {
            while (ebits > bnExpModThreshTable[wbits]) {
                ++wbits;
            }
        }
        int tblmask = 1 << wbits;
        int[][] table = new int[tblmask][];
        for (int i = 0; i < tblmask; ++i) {
            table[i] = new int[modLen];
        }
        long n0 = ((long)mod[modLen - 1] & 0xFFFFFFFFL) + (((long)mod[modLen - 2] & 0xFFFFFFFFL) << 32);
        long inv = -MutableBigInteger.inverseMod64(n0);
        int[] a = BigInteger.leftShift(base, base.length, modLen << 5);
        MutableBigInteger q = new MutableBigInteger();
        MutableBigInteger a2 = new MutableBigInteger(a);
        MutableBigInteger b2 = new MutableBigInteger(mod);
        b2.normalize();
        MutableBigInteger r = a2.divide(b2, q);
        table[0] = r.toIntArray();
        if (table[0].length < modLen) {
            int offset = modLen - table[0].length;
            int[] t2 = new int[modLen];
            System.arraycopy(table[0], 0, t2, offset, table[0].length);
            table[0] = t2;
        }
        int[] b = BigInteger.montgomerySquare(table[0], mod, modLen, inv, null);
        int[] t = Arrays.copyOf(b, modLen);
        for (int i = 1; i < tblmask; ++i) {
            table[i] = BigInteger.montgomeryMultiply(t, table[i - 1], mod, modLen, inv, null);
        }
        int bitpos = 1 << (ebits - 1 & 0x1F);
        int buf = 0;
        int elen = exp.length;
        int eIndex = 0;
        for (int i = 0; i <= wbits; ++i) {
            buf = buf << 1 | ((exp[eIndex] & bitpos) != 0 ? 1 : 0);
            if ((bitpos >>>= 1) != 0) continue;
            ++eIndex;
            bitpos = Integer.MIN_VALUE;
            --elen;
        }
        int multpos = ebits--;
        boolean isone = true;
        multpos = ebits - wbits;
        while ((buf & 1) == 0) {
            buf >>>= 1;
            ++multpos;
        }
        int[] mult = table[buf >>> 1];
        buf = 0;
        if (multpos == ebits) {
            isone = false;
        }
        while (true) {
            --ebits;
            buf <<= 1;
            if (elen != 0) {
                buf |= (exp[eIndex] & bitpos) != 0 ? 1 : 0;
                if ((bitpos >>>= 1) == 0) {
                    ++eIndex;
                    bitpos = Integer.MIN_VALUE;
                    --elen;
                }
            }
            if ((buf & tblmask) != 0) {
                multpos = ebits - wbits;
                while ((buf & 1) == 0) {
                    buf >>>= 1;
                    ++multpos;
                }
                mult = table[buf >>> 1];
                buf = 0;
            }
            if (ebits == multpos) {
                if (isone) {
                    b = (int[])mult.clone();
                    isone = false;
                } else {
                    t = b;
                    a = BigInteger.montgomeryMultiply(t, mult, mod, modLen, inv, a);
                    t = a;
                    a = b;
                    b = t;
                }
            }
            if (ebits == 0) break;
            if (isone) continue;
            t = b;
            a = BigInteger.montgomerySquare(t, mod, modLen, inv, a);
            t = a;
            a = b;
            b = t;
        }
        int[] t2 = new int[2 * modLen];
        System.arraycopy(b, 0, t2, modLen, modLen);
        b = BigInteger.montReduce(t2, mod, modLen, (int)inv);
        t2 = Arrays.copyOf(b, modLen);
        return new BigInteger(1, t2);
    }

    private static int[] montReduce(int[] n, int[] mod, int mlen, int inv) {
        int c = 0;
        int len = mlen;
        int offset = 0;
        do {
            int nEnd = n[n.length - 1 - offset];
            int carry = BigInteger.mulAdd(n, mod, offset, mlen, inv * nEnd);
            c += BigInteger.addOne(n, offset, mlen, carry);
            ++offset;
        } while (--len > 0);
        while (c > 0) {
            c += BigInteger.subN(n, mod, mlen);
        }
        while (BigInteger.intArrayCmpToLen(n, mod, mlen) >= 0) {
            BigInteger.subN(n, mod, mlen);
        }
        return n;
    }

    private static int intArrayCmpToLen(int[] arg1, int[] arg2, int len) {
        for (int i = 0; i < len; ++i) {
            long b1 = (long)arg1[i] & 0xFFFFFFFFL;
            long b2 = (long)arg2[i] & 0xFFFFFFFFL;
            if (b1 < b2) {
                return -1;
            }
            if (b1 <= b2) continue;
            return 1;
        }
        return 0;
    }

    private static int subN(int[] a, int[] b, int len) {
        long sum = 0L;
        while (--len >= 0) {
            sum = ((long)a[len] & 0xFFFFFFFFL) - ((long)b[len] & 0xFFFFFFFFL) + (sum >> 32);
            a[len] = (int)sum;
        }
        return (int)(sum >> 32);
    }

    static int mulAdd(int[] out, int[] in, int offset, int len, int k) {
        BigInteger.implMulAddCheck(out, in, offset, len, k);
        return BigInteger.implMulAdd(out, in, offset, len, k);
    }

    private static void implMulAddCheck(int[] out, int[] in, int offset, int len, int k) {
        if (len > in.length) {
            throw new IllegalArgumentException("input length is out of bound: " + len + " > " + in.length);
        }
        if (offset < 0) {
            throw new IllegalArgumentException("input offset is invalid: " + offset);
        }
        if (offset > out.length - 1) {
            throw new IllegalArgumentException("input offset is out of bound: " + offset + " > " + (out.length - 1));
        }
        if (len > out.length - offset) {
            throw new IllegalArgumentException("input len is out of bound: " + len + " > " + (out.length - offset));
        }
    }

    @IntrinsicCandidate
    private static int implMulAdd(int[] out, int[] in, int offset, int len, int k) {
        long kLong = (long)k & 0xFFFFFFFFL;
        long carry = 0L;
        offset = out.length - offset - 1;
        for (int j = len - 1; j >= 0; --j) {
            long product = ((long)in[j] & 0xFFFFFFFFL) * kLong + ((long)out[offset] & 0xFFFFFFFFL) + carry;
            out[offset--] = (int)product;
            carry = product >>> 32;
        }
        return (int)carry;
    }

    static int addOne(int[] a, int offset, int mlen, int carry) {
        offset = a.length - 1 - mlen - offset;
        long t = ((long)a[offset] & 0xFFFFFFFFL) + ((long)carry & 0xFFFFFFFFL);
        a[offset] = (int)t;
        if (t >>> 32 == 0L) {
            return 0;
        }
        while (--mlen >= 0) {
            if (--offset < 0) {
                return 1;
            }
            int n = offset;
            a[n] = a[n] + 1;
            if (a[offset] == 0) continue;
            return 0;
        }
        return 1;
    }

    private BigInteger modPow2(BigInteger exponent, int p) {
        BigInteger result = ONE;
        BigInteger baseToPow2 = this.mod2(p);
        int expOffset = 0;
        int limit = exponent.bitLength();
        if (this.testBit(0)) {
            int n = limit = p - 1 < limit ? p - 1 : limit;
        }
        while (expOffset < limit) {
            if (exponent.testBit(expOffset)) {
                result = result.multiply(baseToPow2).mod2(p);
            }
            if (++expOffset >= limit) continue;
            baseToPow2 = baseToPow2.square().mod2(p);
        }
        return result;
    }

    private BigInteger mod2(int p) {
        if (this.bitLength() <= p) {
            return this;
        }
        int numInts = p + 31 >>> 5;
        int[] mag = new int[numInts];
        System.arraycopy(this.mag, this.mag.length - numInts, mag, 0, numInts);
        int excessBits = (numInts << 5) - p;
        mag[0] = mag[0] & (int)((1L << 32 - excessBits) - 1L);
        return mag[0] == 0 ? new BigInteger(1, mag) : new BigInteger(mag, 1);
    }

    public BigInteger modInverse(BigInteger m) {
        if (m.signum != 1) {
            throw new ArithmeticException("BigInteger: modulus not positive");
        }
        if (m.equals(ONE)) {
            return ZERO;
        }
        BigInteger modVal = this;
        if (this.signum < 0 || this.compareMagnitude(m) >= 0) {
            modVal = this.mod(m);
        }
        if (modVal.equals(ONE)) {
            return ONE;
        }
        MutableBigInteger a = new MutableBigInteger(modVal);
        MutableBigInteger b = new MutableBigInteger(m);
        MutableBigInteger result = a.mutableModInverse(b);
        return result.toBigInteger(1);
    }

    public BigInteger shiftLeft(int n) {
        if (this.signum == 0) {
            return ZERO;
        }
        if (n > 0) {
            return new BigInteger(BigInteger.shiftLeft(this.mag, n), this.signum);
        }
        if (n == 0) {
            return this;
        }
        return this.shiftRightImpl(-n);
    }

    private static int[] shiftLeft(int[] mag, int n) {
        int nInts = n >>> 5;
        int nBits = n & 0x1F;
        int magLen = mag.length;
        int[] newMag = null;
        if (nBits == 0) {
            newMag = new int[magLen + nInts];
            System.arraycopy(mag, 0, newMag, 0, magLen);
        } else {
            int i = 0;
            int nBits2 = 32 - nBits;
            int highBits = mag[0] >>> nBits2;
            if (highBits != 0) {
                newMag = new int[magLen + nInts + 1];
                newMag[i++] = highBits;
            } else {
                newMag = new int[magLen + nInts];
            }
            int numIter = magLen - 1;
            Objects.checkFromToIndex(0, numIter + 1, mag.length);
            Objects.checkFromToIndex(i, numIter + i + 1, newMag.length);
            BigInteger.shiftLeftImplWorker(newMag, mag, i, nBits, numIter);
            newMag[numIter + i] = mag[numIter] << nBits;
        }
        return newMag;
    }

    @ForceInline
    @IntrinsicCandidate
    private static void shiftLeftImplWorker(int[] newArr, int[] oldArr, int newIdx, int shiftCount, int numIter) {
        int shiftCountRight = 32 - shiftCount;
        int oldIdx = 0;
        while (oldIdx < numIter) {
            newArr[newIdx++] = oldArr[oldIdx++] << shiftCount | oldArr[oldIdx] >>> shiftCountRight;
        }
    }

    public BigInteger shiftRight(int n) {
        if (this.signum == 0) {
            return ZERO;
        }
        if (n > 0) {
            return this.shiftRightImpl(n);
        }
        if (n == 0) {
            return this;
        }
        return new BigInteger(BigInteger.shiftLeft(this.mag, -n), this.signum);
    }

    private BigInteger shiftRightImpl(int n) {
        int nInts = n >>> 5;
        int nBits = n & 0x1F;
        int magLen = this.mag.length;
        int[] newMag = null;
        if (nInts >= magLen) {
            return this.signum >= 0 ? ZERO : negConst[1];
        }
        if (nBits == 0) {
            int newMagLen = magLen - nInts;
            newMag = Arrays.copyOf(this.mag, newMagLen);
        } else {
            int i = 0;
            int highBits = this.mag[0] >>> nBits;
            if (highBits != 0) {
                newMag = new int[magLen - nInts];
                newMag[i++] = highBits;
            } else {
                newMag = new int[magLen - nInts - 1];
            }
            int numIter = magLen - nInts - 1;
            Objects.checkFromToIndex(0, numIter + 1, this.mag.length);
            Objects.checkFromToIndex(i, numIter + i, newMag.length);
            BigInteger.shiftRightImplWorker(newMag, this.mag, i, nBits, numIter);
        }
        if (this.signum < 0) {
            boolean onesLost = false;
            int j = magLen - nInts;
            for (int i = magLen - 1; i >= j && !onesLost; --i) {
                onesLost = this.mag[i] != 0;
            }
            if (!onesLost && nBits != 0) {
                boolean bl = onesLost = this.mag[magLen - nInts - 1] << 32 - nBits != 0;
            }
            if (onesLost) {
                newMag = this.javaIncrement(newMag);
            }
        }
        return new BigInteger(newMag, this.signum);
    }

    @ForceInline
    @IntrinsicCandidate
    private static void shiftRightImplWorker(int[] newArr, int[] oldArr, int newIdx, int shiftCount, int numIter) {
        int nidx;
        int shiftCountLeft = 32 - shiftCount;
        int idx = numIter;
        int n = nidx = newIdx == 0 ? numIter - 1 : numIter;
        while (nidx >= newIdx) {
            newArr[nidx--] = oldArr[idx--] >>> shiftCount | oldArr[idx] << shiftCountLeft;
        }
    }

    int[] javaIncrement(int[] val) {
        int lastSum = 0;
        int i = val.length - 1;
        while (i >= 0 && lastSum == 0) {
            int n = i--;
            int n2 = val[n] + 1;
            val[n] = n2;
            lastSum = n2;
        }
        if (lastSum == 0) {
            val = new int[val.length + 1];
            val[0] = 1;
        }
        return val;
    }

    public BigInteger and(BigInteger val) {
        int[] result = new int[Math.max(this.intLength(), val.intLength())];
        for (int i = 0; i < result.length; ++i) {
            result[i] = this.getInt(result.length - i - 1) & val.getInt(result.length - i - 1);
        }
        return BigInteger.valueOf(result);
    }

    public BigInteger or(BigInteger val) {
        int[] result = new int[Math.max(this.intLength(), val.intLength())];
        for (int i = 0; i < result.length; ++i) {
            result[i] = this.getInt(result.length - i - 1) | val.getInt(result.length - i - 1);
        }
        return BigInteger.valueOf(result);
    }

    public BigInteger xor(BigInteger val) {
        int[] result = new int[Math.max(this.intLength(), val.intLength())];
        for (int i = 0; i < result.length; ++i) {
            result[i] = this.getInt(result.length - i - 1) ^ val.getInt(result.length - i - 1);
        }
        return BigInteger.valueOf(result);
    }

    public BigInteger not() {
        int[] result = new int[this.intLength()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = ~this.getInt(result.length - i - 1);
        }
        return BigInteger.valueOf(result);
    }

    public BigInteger andNot(BigInteger val) {
        int[] result = new int[Math.max(this.intLength(), val.intLength())];
        for (int i = 0; i < result.length; ++i) {
            result[i] = this.getInt(result.length - i - 1) & ~val.getInt(result.length - i - 1);
        }
        return BigInteger.valueOf(result);
    }

    public boolean testBit(int n) {
        if (n < 0) {
            throw new ArithmeticException("Negative bit address");
        }
        return (this.getInt(n >>> 5) & 1 << (n & 0x1F)) != 0;
    }

    public BigInteger setBit(int n) {
        if (n < 0) {
            throw new ArithmeticException("Negative bit address");
        }
        int intNum = n >>> 5;
        int[] result = new int[Math.max(this.intLength(), intNum + 2)];
        for (int i = 0; i < result.length; ++i) {
            result[result.length - i - 1] = this.getInt(i);
        }
        int n2 = result.length - intNum - 1;
        result[n2] = result[n2] | 1 << (n & 0x1F);
        return BigInteger.valueOf(result);
    }

    public BigInteger clearBit(int n) {
        if (n < 0) {
            throw new ArithmeticException("Negative bit address");
        }
        int intNum = n >>> 5;
        int[] result = new int[Math.max(this.intLength(), (n + 1 >>> 5) + 1)];
        for (int i = 0; i < result.length; ++i) {
            result[result.length - i - 1] = this.getInt(i);
        }
        int n2 = result.length - intNum - 1;
        result[n2] = result[n2] & ~(1 << (n & 0x1F));
        return BigInteger.valueOf(result);
    }

    public BigInteger flipBit(int n) {
        if (n < 0) {
            throw new ArithmeticException("Negative bit address");
        }
        int intNum = n >>> 5;
        int[] result = new int[Math.max(this.intLength(), intNum + 2)];
        for (int i = 0; i < result.length; ++i) {
            result[result.length - i - 1] = this.getInt(i);
        }
        int n2 = result.length - intNum - 1;
        result[n2] = result[n2] ^ 1 << (n & 0x1F);
        return BigInteger.valueOf(result);
    }

    public int getLowestSetBit() {
        int lsb = this.lowestSetBitPlusTwo - 2;
        if (lsb == -2) {
            lsb = 0;
            if (this.signum == 0) {
                --lsb;
            } else {
                int b;
                int i = 0;
                while ((b = this.getInt(i)) == 0) {
                    ++i;
                }
                lsb += (i << 5) + Integer.numberOfTrailingZeros(b);
            }
            this.lowestSetBitPlusTwo = lsb + 2;
        }
        return lsb;
    }

    public int bitLength() {
        int n = this.bitLengthPlusOne - 1;
        if (n == -1) {
            int[] m = this.mag;
            int len = m.length;
            if (len == 0) {
                n = 0;
            } else {
                int magBitLength = (len - 1 << 5) + BigInteger.bitLengthForInt(this.mag[0]);
                if (this.signum < 0) {
                    boolean pow2 = Integer.bitCount(this.mag[0]) == 1;
                    for (int i = 1; i < len && pow2; ++i) {
                        pow2 = this.mag[i] == 0;
                    }
                    n = pow2 ? magBitLength - 1 : magBitLength;
                } else {
                    n = magBitLength;
                }
            }
            this.bitLengthPlusOne = n + 1;
        }
        return n;
    }

    public int bitCount() {
        int bc = this.bitCountPlusOne - 1;
        if (bc == -1) {
            bc = 0;
            for (int i = 0; i < this.mag.length; ++i) {
                bc += Integer.bitCount(this.mag[i]);
            }
            if (this.signum < 0) {
                int magTrailingZeroCount = 0;
                int j = this.mag.length - 1;
                while (this.mag[j] == 0) {
                    magTrailingZeroCount += 32;
                    --j;
                }
                bc += (magTrailingZeroCount += Integer.numberOfTrailingZeros(this.mag[j])) - 1;
            }
            this.bitCountPlusOne = bc + 1;
        }
        return bc;
    }

    public boolean isProbablePrime(int certainty) {
        if (certainty <= 0) {
            return true;
        }
        BigInteger w = this.abs();
        if (w.equals(TWO)) {
            return true;
        }
        if (!w.testBit(0) || w.equals(ONE)) {
            return false;
        }
        if (w.bitLength() > 500000001) {
            throw new ArithmeticException("Primality test implementation restriction on bitLength");
        }
        return w.primeToCertainty(certainty, null);
    }

    @Override
    public int compareTo(BigInteger val) {
        if (this.signum == val.signum) {
            return switch (this.signum) {
                case 1 -> this.compareMagnitude(val);
                case -1 -> val.compareMagnitude(this);
                default -> 0;
            };
        }
        return this.signum > val.signum ? 1 : -1;
    }

    final int compareMagnitude(BigInteger val) {
        int[] m1 = this.mag;
        int len1 = m1.length;
        int[] m2 = val.mag;
        int len2 = m2.length;
        if (len1 < len2) {
            return -1;
        }
        if (len1 > len2) {
            return 1;
        }
        for (int i = 0; i < len1; ++i) {
            int a = m1[i];
            int b = m2[i];
            if (a == b) continue;
            return ((long)a & 0xFFFFFFFFL) < ((long)b & 0xFFFFFFFFL) ? -1 : 1;
        }
        return 0;
    }

    final int compareMagnitude(long val) {
        int highWord;
        assert (val != Long.MIN_VALUE);
        int[] m1 = this.mag;
        int len = m1.length;
        if (len > 2) {
            return 1;
        }
        if (val < 0L) {
            val = -val;
        }
        if ((highWord = (int)(val >>> 32)) == 0) {
            if (len < 1) {
                return -1;
            }
            if (len > 1) {
                return 1;
            }
            int a = m1[0];
            int b = (int)val;
            if (a != b) {
                return ((long)a & 0xFFFFFFFFL) < ((long)b & 0xFFFFFFFFL) ? -1 : 1;
            }
            return 0;
        }
        if (len < 2) {
            return -1;
        }
        int a = m1[0];
        int b = highWord;
        if (a != b) {
            return ((long)a & 0xFFFFFFFFL) < ((long)b & 0xFFFFFFFFL) ? -1 : 1;
        }
        a = m1[1];
        b = (int)val;
        if (a != b) {
            return ((long)a & 0xFFFFFFFFL) < ((long)b & 0xFFFFFFFFL) ? -1 : 1;
        }
        return 0;
    }

    public boolean equals(Object x) {
        if (x == this) {
            return true;
        }
        if (!(x instanceof BigInteger)) {
            return false;
        }
        BigInteger xInt = (BigInteger)x;
        if (xInt.signum != this.signum) {
            return false;
        }
        int[] m = this.mag;
        int len = m.length;
        int[] xm = xInt.mag;
        if (len != xm.length) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            if (xm[i] == m[i]) continue;
            return false;
        }
        return true;
    }

    public BigInteger min(BigInteger val) {
        return this.compareTo(val) < 0 ? this : val;
    }

    public BigInteger max(BigInteger val) {
        return this.compareTo(val) > 0 ? this : val;
    }

    public int hashCode() {
        int hashCode = 0;
        for (int i = 0; i < this.mag.length; ++i) {
            hashCode = (int)((long)(31 * hashCode) + ((long)this.mag[i] & 0xFFFFFFFFL));
        }
        return hashCode * this.signum;
    }

    public String toString(int radix) {
        if (this.signum == 0) {
            return "0";
        }
        if (radix < 2 || radix > 36) {
            radix = 10;
        }
        BigInteger abs = this.abs();
        int b = abs.bitLength();
        int numChars = (int)(Math.floor((double)b * LOG_TWO / logCache[radix]) + 1.0) + (this.signum < 0 ? 1 : 0);
        StringBuilder sb = new StringBuilder(numChars);
        if (this.signum < 0) {
            sb.append('-');
        }
        BigInteger.toString(abs, sb, radix, 0);
        return sb.toString();
    }

    private static void padWithZeros(StringBuilder buf, int numZeros) {
        while (numZeros >= NUM_ZEROS) {
            buf.append(ZEROS);
            numZeros -= NUM_ZEROS;
        }
        if (numZeros > 0) {
            buf.append(ZEROS, 0, numZeros);
        }
    }

    private void smallToString(int radix, StringBuilder buf, int digits) {
        assert (this.signum >= 0);
        if (this.signum == 0) {
            BigInteger.padWithZeros(buf, digits);
            return;
        }
        int maxNumDigitGroups = (4 * this.mag.length + 6) / 7;
        long[] digitGroups = new long[maxNumDigitGroups];
        BigInteger tmp = this;
        int numGroups = 0;
        while (tmp.signum != 0) {
            BigInteger d = longRadix[radix];
            MutableBigInteger q = new MutableBigInteger();
            MutableBigInteger a = new MutableBigInteger(tmp.mag);
            MutableBigInteger b = new MutableBigInteger(d.mag);
            MutableBigInteger r = a.divide(b, q);
            BigInteger q2 = q.toBigInteger(tmp.signum * d.signum);
            BigInteger r2 = r.toBigInteger(tmp.signum * d.signum);
            digitGroups[numGroups++] = r2.longValue();
            tmp = q2;
        }
        String s = Long.toString(digitGroups[numGroups - 1], radix);
        BigInteger.padWithZeros(buf, digits - (s.length() + (numGroups - 1) * digitsPerLong[radix]));
        buf.append(s);
        for (int i = numGroups - 2; i >= 0; --i) {
            s = Long.toString(digitGroups[i], radix);
            int numLeadingZeros = digitsPerLong[radix] - s.length();
            if (numLeadingZeros != 0) {
                buf.append(ZEROS, 0, numLeadingZeros);
            }
            buf.append(s);
        }
    }

    private static void toString(BigInteger u, StringBuilder sb, int radix, int digits) {
        assert (u.signum() >= 0);
        if (u.mag.length <= 20) {
            u.smallToString(radix, sb, digits);
            return;
        }
        int b = u.bitLength();
        int n = (int)Math.round(Math.log((double)b * LOG_TWO / logCache[radix]) / LOG_TWO - 1.0);
        BigInteger v = BigInteger.getRadixConversionCache(radix, n);
        BigInteger[] results = u.divideAndRemainder(v);
        int expectedDigits = 1 << n;
        BigInteger.toString(results[0], sb, radix, digits - expectedDigits);
        BigInteger.toString(results[1], sb, radix, expectedDigits);
    }

    private static BigInteger getRadixConversionCache(int radix, int exponent) {
        BigInteger[] cacheLine = powerCache[radix];
        if (exponent < cacheLine.length) {
            return cacheLine[exponent];
        }
        int oldLength = cacheLine.length;
        cacheLine = Arrays.copyOf(cacheLine, exponent + 1);
        for (int i = oldLength; i <= exponent; ++i) {
            cacheLine[i] = cacheLine[i - 1].pow(2);
        }
        BigInteger[][] pc = powerCache;
        if (exponent >= pc[radix].length) {
            pc = (BigInteger[][])pc.clone();
            pc[radix] = cacheLine;
            powerCache = pc;
        }
        return cacheLine[exponent];
    }

    public String toString() {
        return this.toString(10);
    }

    public byte[] toByteArray() {
        int byteLen = this.bitLength() / 8 + 1;
        byte[] byteArray = new byte[byteLen];
        int bytesCopied = 4;
        int nextInt = 0;
        int intIndex = 0;
        for (int i = byteLen - 1; i >= 0; --i) {
            if (bytesCopied == 4) {
                nextInt = this.getInt(intIndex++);
                bytesCopied = 1;
            } else {
                nextInt >>>= 8;
                ++bytesCopied;
            }
            byteArray[i] = (byte)nextInt;
        }
        return byteArray;
    }

    @Override
    public int intValue() {
        int result = 0;
        result = this.getInt(0);
        return result;
    }

    @Override
    public long longValue() {
        long result = 0L;
        for (int i = 1; i >= 0; --i) {
            result = (result << 32) + ((long)this.getInt(i) & 0xFFFFFFFFL);
        }
        return result;
    }

    @Override
    public float floatValue() {
        int twiceSignifFloor;
        if (this.signum == 0) {
            return 0.0f;
        }
        int exponent = (this.mag.length - 1 << 5) + BigInteger.bitLengthForInt(this.mag[0]) - 1;
        if (exponent < 63) {
            return this.longValue();
        }
        if (exponent > 127) {
            return this.signum > 0 ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
        }
        int shift = exponent - 24;
        int nBits = shift & 0x1F;
        int nBits2 = 32 - nBits;
        if (nBits == 0) {
            twiceSignifFloor = this.mag[0];
        } else {
            twiceSignifFloor = this.mag[0] >>> nBits;
            if (twiceSignifFloor == 0) {
                twiceSignifFloor = this.mag[0] << nBits2 | this.mag[1] >>> nBits;
            }
        }
        int signifFloor = twiceSignifFloor >> 1;
        boolean increment = (twiceSignifFloor & 1) != 0 && (((signifFloor &= 0x7FFFFF) & 1) != 0 || this.abs().getLowestSetBit() < shift);
        int signifRounded = increment ? signifFloor + 1 : signifFloor;
        int bits = exponent + 127 << 23;
        bits += signifRounded;
        return Float.intBitsToFloat(bits |= this.signum & Integer.MIN_VALUE);
    }

    @Override
    public double doubleValue() {
        int lowBits;
        int highBits;
        if (this.signum == 0) {
            return 0.0;
        }
        int exponent = (this.mag.length - 1 << 5) + BigInteger.bitLengthForInt(this.mag[0]) - 1;
        if (exponent < 63) {
            return this.longValue();
        }
        if (exponent > 1023) {
            return this.signum > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }
        int shift = exponent - 53;
        int nBits = shift & 0x1F;
        int nBits2 = 32 - nBits;
        if (nBits == 0) {
            highBits = this.mag[0];
            lowBits = this.mag[1];
        } else {
            highBits = this.mag[0] >>> nBits;
            lowBits = this.mag[0] << nBits2 | this.mag[1] >>> nBits;
            if (highBits == 0) {
                highBits = lowBits;
                lowBits = this.mag[1] << nBits2 | this.mag[2] >>> nBits;
            }
        }
        long twiceSignifFloor = ((long)highBits & 0xFFFFFFFFL) << 32 | (long)lowBits & 0xFFFFFFFFL;
        long signifFloor = twiceSignifFloor >> 1;
        boolean increment = (twiceSignifFloor & 1L) != 0L && (((signifFloor &= 0xFFFFFFFFFFFFFL) & 1L) != 0L || this.abs().getLowestSetBit() < shift);
        long signifRounded = increment ? signifFloor + 1L : signifFloor;
        long bits = (long)(exponent + 1023) << 52;
        bits += signifRounded;
        return Double.longBitsToDouble(bits |= (long)this.signum & Long.MIN_VALUE);
    }

    private static int[] stripLeadingZeroInts(int[] val) {
        int keep;
        int vlen = val.length;
        for (keep = 0; keep < vlen && val[keep] == 0; ++keep) {
        }
        return Arrays.copyOfRange(val, keep, vlen);
    }

    private static int[] trustedStripLeadingZeroInts(int[] val) {
        int keep;
        int vlen = val.length;
        for (keep = 0; keep < vlen && val[keep] == 0; ++keep) {
        }
        return keep == 0 ? val : Arrays.copyOfRange(val, keep, vlen);
    }

    private static int[] stripLeadingZeroBytes(byte[] a, int from, int len) {
        return BigInteger.stripLeadingZeroBytes(Integer.MIN_VALUE, a, from, len);
    }

    private static int[] stripLeadingZeroBytes(int b, byte[] a, int from, int len) {
        if (len == 0) {
            return BigInteger.ZERO.mag;
        }
        int to = from + len;
        if (b < -128) {
            b = a[from];
        }
        ++from;
        while (b == 0 && from < to) {
            b = a[from++];
        }
        if (b == 0) {
            return BigInteger.ZERO.mag;
        }
        int[] res = new int[(to - from >> 2) + 1];
        int d0 = b & 0xFF;
        while ((to - from & 3) != 0) {
            d0 = d0 << 8 | a[from++] & 0xFF;
        }
        res[0] = d0;
        int i = 1;
        while (from < to) {
            res[i++] = a[from++] << 24 | (a[from++] & 0xFF) << 16 | (a[from++] & 0xFF) << 8 | a[from++] & 0xFF;
        }
        return res;
    }

    private static int[] makePositive(int b, byte[] a, int from, int len) {
        int to = from + len;
        ++from;
        while (b == -1 && from < to) {
            b = a[from++];
        }
        int d0 = 0xFFFFFF00 | b & 0xFF;
        while ((to - from & 3) != 0) {
            b = a[from++];
            d0 = d0 << 8 | b & 0xFF;
        }
        int f = from;
        while (b == 0 && from < to) {
            b = a[from++];
        }
        int d = b & 0xFF;
        while ((to - from & 3) != 0) {
            d = d << 8 | a[from++] & 0xFF;
        }
        int c = (to - from | d0 | d) == 0 ? 1 : 0;
        int[] res = new int[c + 1 + (to - f >> 2)];
        res[0] = c == 0 ? d0 : -1;
        int i = res.length - (to - from >> 2);
        if (i > 1) {
            res[i - 1] = d;
        }
        while (from < to) {
            res[i++] = a[from++] << 24 | (a[from++] & 0xFF) << 16 | (a[from++] & 0xFF) << 8 | a[from++] & 0xFF;
        }
        while (--i >= 0 && res[i] == 0) {
        }
        res[i] = -res[i];
        while (--i >= 0) {
            res[i] = ~res[i];
        }
        return res;
    }

    private static int[] makePositive(int[] a) {
        int n;
        int i;
        int j;
        int keep;
        for (keep = 0; keep < a.length && a[keep] == -1; ++keep) {
        }
        for (j = keep; j < a.length && a[j] == 0; ++j) {
        }
        int extraInt = j == a.length ? 1 : 0;
        int[] result = new int[a.length - keep + extraInt];
        for (i = keep; i < a.length; ++i) {
            result[i - keep + extraInt] = ~a[i];
        }
        i = result.length - 1;
        do {
            n = i--;
        } while ((result[n] = result[n] + 1) == 0);
        return result;
    }

    private int intLength() {
        return (this.bitLength() >>> 5) + 1;
    }

    private int signBit() {
        return this.signum < 0 ? 1 : 0;
    }

    private int signInt() {
        return this.signum < 0 ? -1 : 0;
    }

    private int getInt(int n) {
        if (n < 0) {
            return 0;
        }
        if (n >= this.mag.length) {
            return this.signInt();
        }
        int magInt = this.mag[this.mag.length - n - 1];
        return this.signum >= 0 ? magInt : (n <= this.firstNonzeroIntNum() ? -magInt : ~magInt);
    }

    private int firstNonzeroIntNum() {
        int fn = this.firstNonzeroIntNumPlusTwo - 2;
        if (fn == -2) {
            int i;
            int mlen = this.mag.length;
            for (i = mlen - 1; i >= 0 && this.mag[i] == 0; --i) {
            }
            fn = mlen - i - 1;
            this.firstNonzeroIntNumPlusTwo = fn + 2;
        }
        return fn;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = s.readFields();
        int sign = fields.get("signum", -2);
        if (sign < -1 || sign > 1) {
            String message = "BigInteger: Invalid signum value";
            if (fields.defaulted("signum")) {
                message = "BigInteger: Signum not present in stream";
            }
            throw new StreamCorruptedException(message);
        }
        byte[] magnitude = (byte[])fields.get("magnitude", null);
        int[] mag = BigInteger.stripLeadingZeroBytes(magnitude = (byte[])magnitude.clone(), 0, magnitude.length);
        if (mag.length == 0 != (sign == 0)) {
            String message = "BigInteger: signum-magnitude mismatch";
            if (fields.defaulted("magnitude")) {
                message = "BigInteger: Magnitude not present in stream";
            }
            throw new StreamCorruptedException(message);
        }
        if (mag.length > 0x4000000 || mag.length == 0x4000000 && mag[0] < 0) {
            throw new StreamCorruptedException("BigInteger: Out of the supported range");
        }
        UnsafeHolder.putSignAndMag(this, sign, mag);
    }

    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("Deserialized BigInteger objects need data");
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        ObjectOutputStream.PutField fields = s.putFields();
        fields.put("signum", this.signum);
        fields.put("magnitude", this.magSerializedForm());
        fields.put("bitCount", -1);
        fields.put("bitLength", -1);
        fields.put("lowestSetBit", -2);
        fields.put("firstNonzeroByteNum", -2);
        s.writeFields();
    }

    private byte[] magSerializedForm() {
        int len = this.mag.length;
        int bitLen = len == 0 ? 0 : (len - 1 << 5) + BigInteger.bitLengthForInt(this.mag[0]);
        int byteLen = bitLen + 7 >>> 3;
        byte[] result = new byte[byteLen];
        int bytesCopied = 4;
        int intIndex = len - 1;
        int nextInt = 0;
        for (int i = byteLen - 1; i >= 0; --i) {
            if (bytesCopied == 4) {
                nextInt = this.mag[intIndex--];
                bytesCopied = 1;
            } else {
                nextInt >>>= 8;
                ++bytesCopied;
            }
            result[i] = (byte)nextInt;
        }
        return result;
    }

    public long longValueExact() {
        if (this.mag.length <= 2 && this.bitLength() <= 63) {
            return this.longValue();
        }
        throw new ArithmeticException("BigInteger out of long range");
    }

    public int intValueExact() {
        if (this.mag.length <= 1 && this.bitLength() <= 31) {
            return this.intValue();
        }
        throw new ArithmeticException("BigInteger out of int range");
    }

    public short shortValueExact() {
        int value;
        if (this.mag.length <= 1 && this.bitLength() <= 31 && (value = this.intValue()) >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            return this.shortValue();
        }
        throw new ArithmeticException("BigInteger out of short range");
    }

    public byte byteValueExact() {
        int value;
        if (this.mag.length <= 1 && this.bitLength() <= 31 && (value = this.intValue()) >= -128 && value <= 127) {
            return this.byteValue();
        }
        throw new ArithmeticException("BigInteger out of byte range");
    }

    static {
        LOG_TWO = Math.log(2.0);
        for (int i = 1; i <= 16; ++i) {
            int[] magnitude = new int[]{i};
            BigInteger.posConst[i] = new BigInteger(magnitude, 1);
            BigInteger.negConst[i] = new BigInteger(magnitude, -1);
        }
        BigInteger[][] cache = new BigInteger[37][];
        logCache = new double[37];
        for (int i = 2; i <= 36; ++i) {
            cache[i] = new BigInteger[]{BigInteger.valueOf(i)};
            BigInteger.logCache[i] = Math.log(i);
        }
        powerCache = cache;
        ZERO = new BigInteger(new int[0], 0);
        ONE = BigInteger.valueOf(1L);
        TWO = BigInteger.valueOf(2L);
        NEGATIVE_ONE = BigInteger.valueOf(-1L);
        TEN = BigInteger.valueOf(10L);
        bnExpModThreshTable = new int[]{7, 25, 81, 241, 673, 1793, Integer.MAX_VALUE};
        NUM_ZEROS = 63;
        ZEROS = "0".repeat(NUM_ZEROS);
        digitsPerLong = new int[]{0, 0, 62, 39, 31, 27, 24, 22, 20, 19, 18, 18, 17, 17, 16, 16, 15, 15, 15, 14, 14, 14, 14, 13, 13, 13, 13, 13, 13, 12, 12, 12, 12, 12, 12, 12, 12};
        longRadix = new BigInteger[]{null, null, BigInteger.valueOf(0x4000000000000000L), BigInteger.valueOf(4052555153018976267L), BigInteger.valueOf(0x4000000000000000L), BigInteger.valueOf(7450580596923828125L), BigInteger.valueOf(4738381338321616896L), BigInteger.valueOf(3909821048582988049L), BigInteger.valueOf(0x1000000000000000L), BigInteger.valueOf(1350851717672992089L), BigInteger.valueOf(1000000000000000000L), BigInteger.valueOf(5559917313492231481L), BigInteger.valueOf(2218611106740436992L), BigInteger.valueOf(8650415919381337933L), BigInteger.valueOf(2177953337809371136L), BigInteger.valueOf(6568408355712890625L), BigInteger.valueOf(0x1000000000000000L), BigInteger.valueOf(2862423051509815793L), BigInteger.valueOf(6746640616477458432L), BigInteger.valueOf(799006685782884121L), BigInteger.valueOf(1638400000000000000L), BigInteger.valueOf(3243919932521508681L), BigInteger.valueOf(6221821273427820544L), BigInteger.valueOf(504036361936467383L), BigInteger.valueOf(876488338465357824L), BigInteger.valueOf(1490116119384765625L), BigInteger.valueOf(2481152873203736576L), BigInteger.valueOf(4052555153018976267L), BigInteger.valueOf(6502111422497947648L), BigInteger.valueOf(353814783205469041L), BigInteger.valueOf(531441000000000000L), BigInteger.valueOf(787662783788549761L), BigInteger.valueOf(0x1000000000000000L), BigInteger.valueOf(1667889514952984961L), BigInteger.valueOf(2386420683693101056L), BigInteger.valueOf(3379220508056640625L), BigInteger.valueOf(4738381338321616896L)};
        digitsPerInt = new int[]{0, 0, 30, 19, 15, 13, 11, 11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5};
        intRadix = new int[]{0, 0, 0x40000000, 1162261467, 0x40000000, 1220703125, 362797056, 1977326743, 0x40000000, 387420489, 1000000000, 214358881, 429981696, 815730721, 1475789056, 170859375, 0x10000000, 410338673, 612220032, 893871739, 1280000000, 1801088541, 113379904, 148035889, 191102976, 244140625, 308915776, 387420489, 481890304, 594823321, 729000000, 887503681, 0x40000000, 1291467969, 1544804416, 1838265625, 60466176};
        serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("signum", Integer.TYPE), new ObjectStreamField("magnitude", byte[].class), new ObjectStreamField("bitCount", Integer.TYPE), new ObjectStreamField("bitLength", Integer.TYPE), new ObjectStreamField("firstNonzeroByteNum", Integer.TYPE), new ObjectStreamField("lowestSetBit", Integer.TYPE)};
    }

    private static abstract sealed class RecursiveOp
    extends RecursiveTask<BigInteger> {
        private static final int PARALLEL_FORK_DEPTH_THRESHOLD = RecursiveOp.calculateMaximumDepth(ForkJoinPool.getCommonPoolParallelism());
        final boolean parallel;
        final byte depth;

        private static final int calculateMaximumDepth(int parallelism) {
            return 32 - Integer.numberOfLeadingZeros(parallelism);
        }

        private RecursiveOp(boolean parallel, int depth) {
            this.parallel = parallel;
            this.depth = (byte)depth;
        }

        private static int getParallelForkDepthThreshold() {
            Thread thread = Thread.currentThread();
            if (thread instanceof ForkJoinWorkerThread) {
                ForkJoinWorkerThread fjwt = (ForkJoinWorkerThread)thread;
                return RecursiveOp.calculateMaximumDepth(fjwt.getPool().getParallelism());
            }
            return PARALLEL_FORK_DEPTH_THRESHOLD;
        }

        protected RecursiveTask<BigInteger> forkOrInvoke() {
            if (this.parallel && this.depth <= RecursiveOp.getParallelForkDepthThreshold()) {
                this.fork();
            } else {
                this.invoke();
            }
            return this;
        }

        private static RecursiveTask<BigInteger> multiply(BigInteger a, BigInteger b, boolean parallel, int depth) {
            return new RecursiveMultiply(a, b, parallel, depth).forkOrInvoke();
        }

        private static RecursiveTask<BigInteger> square(BigInteger a, boolean parallel, int depth) {
            return new RecursiveSquare(a, parallel, depth).forkOrInvoke();
        }

        private static final class RecursiveMultiply
        extends RecursiveOp {
            private final BigInteger a;
            private final BigInteger b;

            public RecursiveMultiply(BigInteger a, BigInteger b, boolean parallel, int depth) {
                super(parallel, depth);
                this.a = a;
                this.b = b;
            }

            @Override
            public BigInteger compute() {
                return this.a.multiply(this.b, true, this.parallel, this.depth);
            }
        }

        private static final class RecursiveSquare
        extends RecursiveOp {
            private final BigInteger a;

            public RecursiveSquare(BigInteger a, boolean parallel, int depth) {
                super(parallel, depth);
                this.a = a;
            }

            @Override
            public BigInteger compute() {
                return this.a.square(true, this.parallel, this.depth);
            }
        }
    }

    private static class UnsafeHolder {
        private static final Unsafe unsafe = Unsafe.getUnsafe();
        private static final long signumOffset = unsafe.objectFieldOffset(BigInteger.class, "signum");
        private static final long magOffset = unsafe.objectFieldOffset(BigInteger.class, "mag");

        private UnsafeHolder() {
        }

        static void putSignAndMag(BigInteger bi, int sign, int[] magnitude) {
            unsafe.putInt(bi, signumOffset, sign);
            unsafe.putReference(bi, magOffset, magnitude);
        }
    }
}

