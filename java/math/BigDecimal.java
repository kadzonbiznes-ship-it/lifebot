/*
 * Decompiled with CFR 0.152.
 */
package java.math;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.StreamCorruptedException;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.MutableBigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Objects;
import jdk.internal.misc.Unsafe;

public class BigDecimal
extends Number
implements Comparable<BigDecimal> {
    private static final double L = 3.321928094887362;
    private static final int P_F = 24;
    private static final int Q_MIN_F = -149;
    private static final int Q_MAX_F = 104;
    private static final int P_D = 53;
    private static final int Q_MIN_D = -1074;
    private static final int Q_MAX_D = 971;
    private final BigInteger intVal;
    private final int scale;
    private transient int precision;
    private transient String stringCache;
    static final long INFLATED = Long.MIN_VALUE;
    private static final BigInteger INFLATED_BIGINT = BigInteger.valueOf(Long.MIN_VALUE);
    private final transient long intCompact;
    private static final int MAX_COMPACT_DIGITS = 18;
    private static final long serialVersionUID = 6108874887143696463L;
    private static final BigDecimal[] ZERO_THROUGH_TEN = new BigDecimal[]{new BigDecimal(BigInteger.ZERO, 0L, 0, 1), new BigDecimal(BigInteger.ONE, 1L, 0, 1), new BigDecimal(BigInteger.TWO, 2L, 0, 1), new BigDecimal(BigInteger.valueOf(3L), 3L, 0, 1), new BigDecimal(BigInteger.valueOf(4L), 4L, 0, 1), new BigDecimal(BigInteger.valueOf(5L), 5L, 0, 1), new BigDecimal(BigInteger.valueOf(6L), 6L, 0, 1), new BigDecimal(BigInteger.valueOf(7L), 7L, 0, 1), new BigDecimal(BigInteger.valueOf(8L), 8L, 0, 1), new BigDecimal(BigInteger.valueOf(9L), 9L, 0, 1), new BigDecimal(BigInteger.TEN, 10L, 0, 2)};
    private static final BigDecimal[] ZERO_SCALED_BY = new BigDecimal[]{ZERO_THROUGH_TEN[0], new BigDecimal(BigInteger.ZERO, 0L, 1, 1), new BigDecimal(BigInteger.ZERO, 0L, 2, 1), new BigDecimal(BigInteger.ZERO, 0L, 3, 1), new BigDecimal(BigInteger.ZERO, 0L, 4, 1), new BigDecimal(BigInteger.ZERO, 0L, 5, 1), new BigDecimal(BigInteger.ZERO, 0L, 6, 1), new BigDecimal(BigInteger.ZERO, 0L, 7, 1), new BigDecimal(BigInteger.ZERO, 0L, 8, 1), new BigDecimal(BigInteger.ZERO, 0L, 9, 1), new BigDecimal(BigInteger.ZERO, 0L, 10, 1), new BigDecimal(BigInteger.ZERO, 0L, 11, 1), new BigDecimal(BigInteger.ZERO, 0L, 12, 1), new BigDecimal(BigInteger.ZERO, 0L, 13, 1), new BigDecimal(BigInteger.ZERO, 0L, 14, 1), new BigDecimal(BigInteger.ZERO, 0L, 15, 1)};
    private static final long HALF_LONG_MAX_VALUE = 0x3FFFFFFFFFFFFFFFL;
    private static final long HALF_LONG_MIN_VALUE = -4611686018427387904L;
    public static final BigDecimal ZERO = ZERO_THROUGH_TEN[0];
    public static final BigDecimal ONE = ZERO_THROUGH_TEN[1];
    public static final BigDecimal TWO = ZERO_THROUGH_TEN[2];
    public static final BigDecimal TEN = ZERO_THROUGH_TEN[10];
    private static final BigDecimal ONE_TENTH = BigDecimal.valueOf(1L, 1);
    private static final BigDecimal ONE_HALF = BigDecimal.valueOf(5L, 1);
    @Deprecated(since="9")
    public static final int ROUND_UP = 0;
    @Deprecated(since="9")
    public static final int ROUND_DOWN = 1;
    @Deprecated(since="9")
    public static final int ROUND_CEILING = 2;
    @Deprecated(since="9")
    public static final int ROUND_FLOOR = 3;
    @Deprecated(since="9")
    public static final int ROUND_HALF_UP = 4;
    @Deprecated(since="9")
    public static final int ROUND_HALF_DOWN = 5;
    @Deprecated(since="9")
    public static final int ROUND_HALF_EVEN = 6;
    @Deprecated(since="9")
    public static final int ROUND_UNNECESSARY = 7;
    private static final double[] DOUBLE_10_POW = new double[]{1.0, 10.0, 100.0, 1000.0, 10000.0, 100000.0, 1000000.0, 1.0E7, 1.0E8, 1.0E9, 1.0E10, 1.0E11, 1.0E12, 1.0E13, 1.0E14, 1.0E15, 1.0E16, 1.0E17, 1.0E18, 1.0E19, 1.0E20, 1.0E21, 1.0E22};
    private static final float[] FLOAT_10_POW = new float[]{1.0f, 10.0f, 100.0f, 1000.0f, 10000.0f, 100000.0f, 1000000.0f, 1.0E7f, 1.0E8f, 1.0E9f, 1.0E10f};
    private static final long[] LONG_TEN_POWERS_TABLE = new long[]{1L, 10L, 100L, 1000L, 10000L, 100000L, 1000000L, 10000000L, 100000000L, 1000000000L, 10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 100000000000000L, 1000000000000000L, 10000000000000000L, 100000000000000000L, 1000000000000000000L};
    private static volatile BigInteger[] BIG_TEN_POWERS_TABLE = new BigInteger[]{BigInteger.ONE, BigInteger.valueOf(10L), BigInteger.valueOf(100L), BigInteger.valueOf(1000L), BigInteger.valueOf(10000L), BigInteger.valueOf(100000L), BigInteger.valueOf(1000000L), BigInteger.valueOf(10000000L), BigInteger.valueOf(100000000L), BigInteger.valueOf(1000000000L), BigInteger.valueOf(10000000000L), BigInteger.valueOf(100000000000L), BigInteger.valueOf(1000000000000L), BigInteger.valueOf(10000000000000L), BigInteger.valueOf(100000000000000L), BigInteger.valueOf(1000000000000000L), BigInteger.valueOf(10000000000000000L), BigInteger.valueOf(100000000000000000L), BigInteger.valueOf(1000000000000000000L)};
    private static final int BIG_TEN_POWERS_TABLE_INITLEN = BIG_TEN_POWERS_TABLE.length;
    private static final int BIG_TEN_POWERS_TABLE_MAX = 16 * BIG_TEN_POWERS_TABLE_INITLEN;
    private static final long[] THRESHOLDS_TABLE = new long[]{Long.MAX_VALUE, 0xCCCCCCCCCCCCCCCL, 92233720368547758L, 9223372036854775L, 922337203685477L, 92233720368547L, 9223372036854L, 922337203685L, 92233720368L, 9223372036L, 922337203L, 92233720L, 0x8CBCCCL, 922337L, 92233L, 9223L, 922L, 92L, 9L};
    private static final long DIV_NUM_BASE = 0x100000000L;
    private static final long[][] LONGLONG_TEN_POWERS_TABLE = new long[][]{{0L, -8446744073709551616L}, {5L, 7766279631452241920L}, {54L, 3875820019684212736L}, {542L, 1864712049423024128L}, {5421L, 200376420520689664L}, {54210L, 2003764205206896640L}, {542101L, 1590897978359414784L}, {5421010L, -2537764290115403776L}, {54210108L, -6930898827444486144L}, {542101086L, 4477988020393345024L}, {5421010862L, 7886392056514347008L}, {54210108624L, 5076944270305263616L}, {542101086242L, -4570789518076018688L}, {5421010862427L, -8814407033341083648L}, {54210108624275L, 4089650035136921600L}, {542101086242752L, 4003012203950112768L}, {5421010862427522L, 3136633892082024448L}, {54210108624275221L, -5527149226598858752L}, {542101086242752217L, 68739955140067328L}, {5421010862427522170L, 687399551400673280L}};

    BigDecimal(BigInteger intVal, long val, int scale, int prec) {
        this.scale = scale;
        this.precision = prec;
        this.intCompact = val;
        this.intVal = intVal;
    }

    public BigDecimal(char[] in, int offset, int len) {
        this(in, offset, len, MathContext.UNLIMITED);
    }

    public BigDecimal(char[] in, int offset, int len, MathContext mc) {
        try {
            Objects.checkFromIndexSize(offset, len, in.length);
        }
        catch (IndexOutOfBoundsException e) {
            throw new NumberFormatException("Bad offset or len arguments for char[] input.");
        }
        int prec = 0;
        long scl = 0L;
        long rs = 0L;
        BigInteger rb = null;
        try {
            boolean isneg = false;
            if (in[offset] == '-') {
                isneg = true;
                ++offset;
                --len;
            } else if (in[offset] == '+') {
                ++offset;
                --len;
            }
            boolean dot = false;
            boolean isCompact = len <= 18;
            int idx = 0;
            if (isCompact) {
                while (len > 0) {
                    char c = in[offset];
                    if (c == '0') {
                        if (prec == 0) {
                            prec = 1;
                        } else if (rs != 0L) {
                            rs *= 10L;
                            ++prec;
                        }
                        if (dot) {
                            ++scl;
                        }
                    } else if (c >= '1' && c <= '9') {
                        digit = c - 48;
                        if (prec != 1 || rs != 0L) {
                            ++prec;
                        }
                        rs = rs * 10L + (long)digit;
                        if (dot) {
                            ++scl;
                        }
                    } else if (c == '.') {
                        if (dot) {
                            throw new NumberFormatException("Character array contains more than one decimal point.");
                        }
                        dot = true;
                    } else if (Character.isDigit(c)) {
                        digit = Character.digit(c, 10);
                        if (digit == 0) {
                            if (prec == 0) {
                                prec = 1;
                            } else if (rs != 0L) {
                                rs *= 10L;
                                ++prec;
                            }
                        } else {
                            if (prec != 1 || rs != 0L) {
                                ++prec;
                            }
                            rs = rs * 10L + (long)digit;
                        }
                        if (dot) {
                            ++scl;
                        }
                    } else {
                        if (c == 'e' || c == 'E') {
                            scl -= BigDecimal.parseExp(in, offset, len);
                            break;
                        }
                        throw new NumberFormatException("Character " + c + " is neither a decimal digit number, decimal point, nor \"e\" notation exponential mark.");
                    }
                    ++offset;
                    --len;
                }
                if (prec == 0) {
                    throw new NumberFormatException("No digits found.");
                }
                rs = isneg ? -rs : rs;
                int mcp = mc.precision;
                int drop = prec - mcp;
                if (mcp > 0 && drop > 0) {
                    while (drop > 0) {
                        scl -= (long)drop;
                        rs = BigDecimal.divideAndRound(rs, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                        prec = BigDecimal.longDigitLength(rs);
                        drop = prec - mcp;
                    }
                }
            } else {
                char[] coeff = new char[len];
                while (len > 0) {
                    char c = in[offset];
                    if (c >= '0' && c <= '9' || Character.isDigit(c)) {
                        if (c == '0' || Character.digit(c, 10) == 0) {
                            if (prec == 0) {
                                coeff[idx] = c;
                                prec = 1;
                            } else if (idx != 0) {
                                coeff[idx++] = c;
                                ++prec;
                            }
                        } else {
                            if (prec != 1 || idx != 0) {
                                ++prec;
                            }
                            coeff[idx++] = c;
                        }
                        if (dot) {
                            ++scl;
                        }
                    } else if (c == '.') {
                        if (dot) {
                            throw new NumberFormatException("Character array contains more than one decimal point.");
                        }
                        dot = true;
                    } else {
                        if (c != 'e' && c != 'E') {
                            throw new NumberFormatException("Character array is missing \"e\" notation exponential mark.");
                        }
                        scl -= BigDecimal.parseExp(in, offset, len);
                        break;
                    }
                    ++offset;
                    --len;
                }
                if (prec == 0) {
                    throw new NumberFormatException("No digits found.");
                }
                rb = new BigInteger(coeff, isneg ? -1 : 1, prec);
                rs = BigDecimal.compactValFor(rb);
                int mcp = mc.precision;
                if (mcp > 0 && prec > mcp) {
                    int drop;
                    if (rs == Long.MIN_VALUE) {
                        drop = prec - mcp;
                        while (drop > 0) {
                            scl -= (long)drop;
                            rs = BigDecimal.compactValFor(rb = BigDecimal.divideAndRoundByTenPow(rb, drop, mc.roundingMode.oldMode));
                            if (rs != Long.MIN_VALUE) {
                                prec = BigDecimal.longDigitLength(rs);
                                break;
                            }
                            prec = BigDecimal.bigDigitLength(rb);
                            drop = prec - mcp;
                        }
                    }
                    if (rs != Long.MIN_VALUE) {
                        drop = prec - mcp;
                        while (drop > 0) {
                            scl -= (long)drop;
                            rs = BigDecimal.divideAndRound(rs, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                            prec = BigDecimal.longDigitLength(rs);
                            drop = prec - mcp;
                        }
                        rb = null;
                    }
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException | NegativeArraySizeException e) {
            NumberFormatException nfe = new NumberFormatException();
            nfe.initCause(e);
            throw nfe;
        }
        if ((long)((int)scl) != scl) {
            throw new NumberFormatException("Exponent overflow.");
        }
        this.scale = (int)scl;
        this.precision = prec;
        this.intCompact = rs;
        this.intVal = rb;
    }

    private static long parseExp(char[] in, int offset, int len) {
        boolean negexp;
        long exp = 0L;
        char c = in[++offset];
        --len;
        boolean bl = negexp = c == '-';
        if (negexp || c == '+') {
            c = in[++offset];
            --len;
        }
        if (len <= 0) {
            throw new NumberFormatException("No exponent digits.");
        }
        while (len > 10 && (c == '0' || Character.digit(c, 10) == 0)) {
            c = in[++offset];
            --len;
        }
        if (len > 10) {
            throw new NumberFormatException("Too many nonzero exponent digits.");
        }
        while (true) {
            int v;
            if (c >= '0' && c <= '9') {
                v = c - 48;
            } else {
                v = Character.digit(c, 10);
                if (v < 0) {
                    throw new NumberFormatException("Not a digit.");
                }
            }
            exp = exp * 10L + (long)v;
            if (len == 1) break;
            c = in[++offset];
            --len;
        }
        if (negexp) {
            exp = -exp;
        }
        return exp;
    }

    public BigDecimal(char[] in) {
        this(in, 0, in.length);
    }

    public BigDecimal(char[] in, MathContext mc) {
        this(in, 0, in.length, mc);
    }

    public BigDecimal(String val) {
        this(val.toCharArray(), 0, val.length());
    }

    public BigDecimal(String val, MathContext mc) {
        this(val.toCharArray(), 0, val.length(), mc);
    }

    public BigDecimal(double val) {
        this(val, MathContext.UNLIMITED);
    }

    public BigDecimal(double val, MathContext mc) {
        BigInteger rb;
        if (Double.isInfinite(val) || Double.isNaN(val)) {
            throw new NumberFormatException("Infinite or NaN");
        }
        long valBits = Double.doubleToLongBits(val);
        int sign = valBits >> 63 == 0L ? 1 : -1;
        int exponent = (int)(valBits >> 52 & 0x7FFL);
        long significand = exponent == 0 ? (valBits & 0xFFFFFFFFFFFFFL) << 1 : valBits & 0xFFFFFFFFFFFFFL | 0x10000000000000L;
        exponent -= 1075;
        if (significand == 0L) {
            this.intVal = BigInteger.ZERO;
            this.scale = 0;
            this.intCompact = 0L;
            this.precision = 1;
            return;
        }
        while ((significand & 1L) == 0L) {
            significand >>= 1;
            ++exponent;
        }
        int scl = 0;
        long compactVal = (long)sign * significand;
        if (exponent == 0) {
            rb = compactVal == Long.MIN_VALUE ? INFLATED_BIGINT : null;
        } else {
            if (exponent < 0) {
                rb = BigInteger.valueOf(5L).pow(-exponent).multiply(compactVal);
                scl = -exponent;
            } else {
                rb = BigInteger.TWO.pow(exponent).multiply(compactVal);
            }
            compactVal = BigDecimal.compactValFor(rb);
        }
        int prec = 0;
        int mcp = mc.precision;
        if (mcp > 0) {
            int drop;
            int mode = mc.roundingMode.oldMode;
            if (compactVal == Long.MIN_VALUE) {
                prec = BigDecimal.bigDigitLength(rb);
                drop = prec - mcp;
                while (drop > 0) {
                    scl = BigDecimal.checkScaleNonZero((long)scl - (long)drop);
                    compactVal = BigDecimal.compactValFor(rb = BigDecimal.divideAndRoundByTenPow(rb, drop, mode));
                    if (compactVal != Long.MIN_VALUE) break;
                    prec = BigDecimal.bigDigitLength(rb);
                    drop = prec - mcp;
                }
            }
            if (compactVal != Long.MIN_VALUE) {
                prec = BigDecimal.longDigitLength(compactVal);
                drop = prec - mcp;
                while (drop > 0) {
                    scl = BigDecimal.checkScaleNonZero((long)scl - (long)drop);
                    compactVal = BigDecimal.divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                    prec = BigDecimal.longDigitLength(compactVal);
                    drop = prec - mcp;
                }
                rb = null;
            }
        }
        this.intVal = rb;
        this.intCompact = compactVal;
        this.scale = scl;
        this.precision = prec;
    }

    private static BigInteger toStrictBigInteger(BigInteger val) {
        return val.getClass() == BigInteger.class ? val : new BigInteger((byte[])val.toByteArray().clone());
    }

    public BigDecimal(BigInteger val) {
        this.scale = 0;
        this.intVal = BigDecimal.toStrictBigInteger(val);
        this.intCompact = BigDecimal.compactValFor(this.intVal);
    }

    public BigDecimal(BigInteger val, MathContext mc) {
        this(BigDecimal.toStrictBigInteger(val), 0, mc);
    }

    public BigDecimal(BigInteger unscaledVal, int scale) {
        this.intVal = BigDecimal.toStrictBigInteger(unscaledVal);
        this.intCompact = BigDecimal.compactValFor(this.intVal);
        this.scale = scale;
    }

    public BigDecimal(BigInteger unscaledVal, int scale, MathContext mc) {
        unscaledVal = BigDecimal.toStrictBigInteger(unscaledVal);
        long compactVal = BigDecimal.compactValFor(unscaledVal);
        int mcp = mc.precision;
        int prec = 0;
        if (mcp > 0) {
            int drop;
            int mode = mc.roundingMode.oldMode;
            if (compactVal == Long.MIN_VALUE) {
                prec = BigDecimal.bigDigitLength(unscaledVal);
                drop = prec - mcp;
                while (drop > 0) {
                    scale = BigDecimal.checkScaleNonZero((long)scale - (long)drop);
                    compactVal = BigDecimal.compactValFor(unscaledVal = BigDecimal.divideAndRoundByTenPow(unscaledVal, drop, mode));
                    if (compactVal != Long.MIN_VALUE) break;
                    prec = BigDecimal.bigDigitLength(unscaledVal);
                    drop = prec - mcp;
                }
            }
            if (compactVal != Long.MIN_VALUE) {
                prec = BigDecimal.longDigitLength(compactVal);
                drop = prec - mcp;
                while (drop > 0) {
                    scale = BigDecimal.checkScaleNonZero((long)scale - (long)drop);
                    compactVal = BigDecimal.divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop], mode);
                    prec = BigDecimal.longDigitLength(compactVal);
                    drop = prec - mcp;
                }
                unscaledVal = null;
            }
        }
        this.intVal = unscaledVal;
        this.intCompact = compactVal;
        this.scale = scale;
        this.precision = prec;
    }

    public BigDecimal(int val) {
        this.intCompact = val;
        this.scale = 0;
        this.intVal = null;
    }

    public BigDecimal(int val, MathContext mc) {
        int mcp = mc.precision;
        long compactVal = val;
        int scl = 0;
        int prec = 0;
        if (mcp > 0) {
            prec = BigDecimal.longDigitLength(compactVal);
            int drop = prec - mcp;
            while (drop > 0) {
                scl = BigDecimal.checkScaleNonZero((long)scl - (long)drop);
                compactVal = BigDecimal.divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                prec = BigDecimal.longDigitLength(compactVal);
                drop = prec - mcp;
            }
        }
        this.intVal = null;
        this.intCompact = compactVal;
        this.scale = scl;
        this.precision = prec;
    }

    public BigDecimal(long val) {
        this.intCompact = val;
        this.intVal = val == Long.MIN_VALUE ? INFLATED_BIGINT : null;
        this.scale = 0;
    }

    public BigDecimal(long val, MathContext mc) {
        BigInteger rb;
        int mcp = mc.precision;
        int mode = mc.roundingMode.oldMode;
        int prec = 0;
        int scl = 0;
        BigInteger bigInteger = rb = val == Long.MIN_VALUE ? INFLATED_BIGINT : null;
        if (mcp > 0) {
            int drop;
            if (val == Long.MIN_VALUE) {
                prec = 19;
                drop = prec - mcp;
                while (drop > 0) {
                    scl = BigDecimal.checkScaleNonZero((long)scl - (long)drop);
                    val = BigDecimal.compactValFor(rb = BigDecimal.divideAndRoundByTenPow(rb, drop, mode));
                    if (val != Long.MIN_VALUE) break;
                    prec = BigDecimal.bigDigitLength(rb);
                    drop = prec - mcp;
                }
            }
            if (val != Long.MIN_VALUE) {
                prec = BigDecimal.longDigitLength(val);
                drop = prec - mcp;
                while (drop > 0) {
                    scl = BigDecimal.checkScaleNonZero((long)scl - (long)drop);
                    val = BigDecimal.divideAndRound(val, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                    prec = BigDecimal.longDigitLength(val);
                    drop = prec - mcp;
                }
                rb = null;
            }
        }
        this.intVal = rb;
        this.intCompact = val;
        this.scale = scl;
        this.precision = prec;
    }

    public static BigDecimal valueOf(long unscaledVal, int scale) {
        if (scale == 0) {
            return BigDecimal.valueOf(unscaledVal);
        }
        if (unscaledVal == 0L) {
            return BigDecimal.zeroValueOf(scale);
        }
        return new BigDecimal(unscaledVal == Long.MIN_VALUE ? INFLATED_BIGINT : null, unscaledVal, scale, 0);
    }

    public static BigDecimal valueOf(long val) {
        if (val >= 0L && val < (long)ZERO_THROUGH_TEN.length) {
            return ZERO_THROUGH_TEN[(int)val];
        }
        if (val != Long.MIN_VALUE) {
            return new BigDecimal(null, val, 0, 0);
        }
        return new BigDecimal(INFLATED_BIGINT, val, 0, 0);
    }

    static BigDecimal valueOf(long unscaledVal, int scale, int prec) {
        if (scale == 0 && unscaledVal >= 0L && unscaledVal < (long)ZERO_THROUGH_TEN.length) {
            return ZERO_THROUGH_TEN[(int)unscaledVal];
        }
        if (unscaledVal == 0L) {
            return BigDecimal.zeroValueOf(scale);
        }
        return new BigDecimal(unscaledVal == Long.MIN_VALUE ? INFLATED_BIGINT : null, unscaledVal, scale, prec);
    }

    static BigDecimal valueOf(BigInteger intVal, int scale, int prec) {
        long val = BigDecimal.compactValFor(intVal);
        if (val == 0L) {
            return BigDecimal.zeroValueOf(scale);
        }
        if (scale == 0 && val >= 0L && val < (long)ZERO_THROUGH_TEN.length) {
            return ZERO_THROUGH_TEN[(int)val];
        }
        return new BigDecimal(intVal, val, scale, prec);
    }

    static BigDecimal zeroValueOf(int scale) {
        if (scale >= 0 && scale < ZERO_SCALED_BY.length) {
            return ZERO_SCALED_BY[scale];
        }
        return new BigDecimal(BigInteger.ZERO, 0L, scale, 1);
    }

    public static BigDecimal valueOf(double val) {
        return new BigDecimal(Double.toString(val));
    }

    public BigDecimal add(BigDecimal augend) {
        if (this.intCompact != Long.MIN_VALUE) {
            if (augend.intCompact != Long.MIN_VALUE) {
                return BigDecimal.add(this.intCompact, this.scale, augend.intCompact, augend.scale);
            }
            return BigDecimal.add(this.intCompact, this.scale, augend.intVal, augend.scale);
        }
        if (augend.intCompact != Long.MIN_VALUE) {
            return BigDecimal.add(augend.intCompact, augend.scale, this.intVal, this.scale);
        }
        return BigDecimal.add(this.intVal, this.scale, augend.intVal, augend.scale);
    }

    public BigDecimal add(BigDecimal augend, MathContext mc) {
        boolean augendIsZero;
        if (mc.precision == 0) {
            return this.add(augend);
        }
        BigDecimal lhs = this;
        boolean lhsIsZero = lhs.signum() == 0;
        boolean bl = augendIsZero = augend.signum() == 0;
        if (lhsIsZero || augendIsZero) {
            int scaleDiff;
            BigDecimal result;
            int preferredScale = Math.max(lhs.scale(), augend.scale());
            if (lhsIsZero && augendIsZero) {
                return BigDecimal.zeroValueOf(preferredScale);
            }
            BigDecimal bigDecimal = result = lhsIsZero ? BigDecimal.doRound(augend, mc) : BigDecimal.doRound(lhs, mc);
            if (result.scale() == preferredScale) {
                return result;
            }
            if (result.scale() > preferredScale) {
                return BigDecimal.stripZerosToMatchScale(result.intVal, result.intCompact, result.scale, preferredScale);
            }
            int precisionDiff = mc.precision - result.precision();
            if (precisionDiff >= (scaleDiff = preferredScale - result.scale())) {
                return result.setScale(preferredScale);
            }
            return result.setScale(result.scale() + precisionDiff);
        }
        long padding = (long)lhs.scale - (long)augend.scale;
        if (padding != 0L) {
            BigDecimal[] arg = this.preAlign(lhs, augend, padding, mc);
            BigDecimal.matchScale(arg);
            lhs = arg[0];
            augend = arg[1];
        }
        return BigDecimal.doRound(lhs.inflated().add(augend.inflated()), lhs.scale, mc);
    }

    private BigDecimal[] preAlign(BigDecimal lhs, BigDecimal augend, long padding, MathContext mc) {
        BigDecimal small;
        BigDecimal big;
        assert (padding != 0L);
        if (padding < 0L) {
            big = lhs;
            small = augend;
        } else {
            big = augend;
            small = lhs;
        }
        long estResultUlpScale = (long)big.scale - (long)big.precision() + (long)mc.precision;
        long smallHighDigitPos = (long)small.scale - (long)small.precision() + 1L;
        if (smallHighDigitPos > (long)(big.scale + 2) && smallHighDigitPos > estResultUlpScale + 2L) {
            small = BigDecimal.valueOf(small.signum(), this.checkScale(Math.max((long)big.scale, estResultUlpScale) + 3L));
        }
        BigDecimal[] result = new BigDecimal[]{big, small};
        return result;
    }

    public BigDecimal subtract(BigDecimal subtrahend) {
        if (this.intCompact != Long.MIN_VALUE) {
            if (subtrahend.intCompact != Long.MIN_VALUE) {
                return BigDecimal.add(this.intCompact, this.scale, -subtrahend.intCompact, subtrahend.scale);
            }
            return BigDecimal.add(this.intCompact, this.scale, subtrahend.intVal.negate(), subtrahend.scale);
        }
        if (subtrahend.intCompact != Long.MIN_VALUE) {
            return BigDecimal.add(-subtrahend.intCompact, subtrahend.scale, this.intVal, this.scale);
        }
        return BigDecimal.add(this.intVal, this.scale, subtrahend.intVal.negate(), subtrahend.scale);
    }

    public BigDecimal subtract(BigDecimal subtrahend, MathContext mc) {
        if (mc.precision == 0) {
            return this.subtract(subtrahend);
        }
        return this.add(subtrahend.negate(), mc);
    }

    public BigDecimal multiply(BigDecimal multiplicand) {
        int productScale = this.checkScale((long)this.scale + (long)multiplicand.scale);
        if (this.intCompact != Long.MIN_VALUE) {
            if (multiplicand.intCompact != Long.MIN_VALUE) {
                return BigDecimal.multiply(this.intCompact, multiplicand.intCompact, productScale);
            }
            return BigDecimal.multiply(this.intCompact, multiplicand.intVal, productScale);
        }
        if (multiplicand.intCompact != Long.MIN_VALUE) {
            return BigDecimal.multiply(multiplicand.intCompact, this.intVal, productScale);
        }
        return BigDecimal.multiply(this.intVal, multiplicand.intVal, productScale);
    }

    public BigDecimal multiply(BigDecimal multiplicand, MathContext mc) {
        if (mc.precision == 0) {
            return this.multiply(multiplicand);
        }
        int productScale = this.checkScale((long)this.scale + (long)multiplicand.scale);
        if (this.intCompact != Long.MIN_VALUE) {
            if (multiplicand.intCompact != Long.MIN_VALUE) {
                return BigDecimal.multiplyAndRound(this.intCompact, multiplicand.intCompact, productScale, mc);
            }
            return BigDecimal.multiplyAndRound(this.intCompact, multiplicand.intVal, productScale, mc);
        }
        if (multiplicand.intCompact != Long.MIN_VALUE) {
            return BigDecimal.multiplyAndRound(multiplicand.intCompact, this.intVal, productScale, mc);
        }
        return BigDecimal.multiplyAndRound(this.intVal, multiplicand.intVal, productScale, mc);
    }

    @Deprecated(since="9")
    public BigDecimal divide(BigDecimal divisor, int scale, int roundingMode) {
        if (roundingMode < 0 || roundingMode > 7) {
            throw new IllegalArgumentException("Invalid rounding mode");
        }
        if (this.intCompact != Long.MIN_VALUE) {
            if (divisor.intCompact != Long.MIN_VALUE) {
                return BigDecimal.divide(this.intCompact, this.scale, divisor.intCompact, divisor.scale, scale, roundingMode);
            }
            return BigDecimal.divide(this.intCompact, this.scale, divisor.intVal, divisor.scale, scale, roundingMode);
        }
        if (divisor.intCompact != Long.MIN_VALUE) {
            return BigDecimal.divide(this.intVal, this.scale, divisor.intCompact, divisor.scale, scale, roundingMode);
        }
        return BigDecimal.divide(this.intVal, this.scale, divisor.intVal, divisor.scale, scale, roundingMode);
    }

    public BigDecimal divide(BigDecimal divisor, int scale, RoundingMode roundingMode) {
        return this.divide(divisor, scale, roundingMode.oldMode);
    }

    @Deprecated(since="9")
    public BigDecimal divide(BigDecimal divisor, int roundingMode) {
        return this.divide(divisor, this.scale, roundingMode);
    }

    public BigDecimal divide(BigDecimal divisor, RoundingMode roundingMode) {
        return this.divide(divisor, this.scale, roundingMode.oldMode);
    }

    public BigDecimal divide(BigDecimal divisor) {
        BigDecimal quotient;
        if (divisor.signum() == 0) {
            if (this.signum() == 0) {
                throw new ArithmeticException("Division undefined");
            }
            throw new ArithmeticException("Division by zero");
        }
        int preferredScale = BigDecimal.saturateLong((long)this.scale - (long)divisor.scale);
        if (this.signum() == 0) {
            return BigDecimal.zeroValueOf(preferredScale);
        }
        MathContext mc = new MathContext((int)Math.min((long)this.precision() + (long)Math.ceil(10.0 * (double)divisor.precision() / 3.0), Integer.MAX_VALUE), RoundingMode.UNNECESSARY);
        try {
            quotient = this.divide(divisor, mc);
        }
        catch (ArithmeticException e) {
            throw new ArithmeticException("Non-terminating decimal expansion; no exact representable decimal result.");
        }
        int quotientScale = quotient.scale();
        if (preferredScale > quotientScale) {
            return quotient.setScale(preferredScale, 7);
        }
        return quotient;
    }

    public BigDecimal divide(BigDecimal divisor, MathContext mc) {
        int mcp = mc.precision;
        if (mcp == 0) {
            return this.divide(divisor);
        }
        BigDecimal dividend = this;
        long preferredScale = (long)dividend.scale - (long)divisor.scale;
        if (divisor.signum() == 0) {
            if (dividend.signum() == 0) {
                throw new ArithmeticException("Division undefined");
            }
            throw new ArithmeticException("Division by zero");
        }
        if (dividend.signum() == 0) {
            return BigDecimal.zeroValueOf(BigDecimal.saturateLong(preferredScale));
        }
        int xscale = dividend.precision();
        int yscale = divisor.precision();
        if (dividend.intCompact != Long.MIN_VALUE) {
            if (divisor.intCompact != Long.MIN_VALUE) {
                return BigDecimal.divide(dividend.intCompact, xscale, divisor.intCompact, yscale, preferredScale, mc);
            }
            return BigDecimal.divide(dividend.intCompact, xscale, divisor.intVal, yscale, preferredScale, mc);
        }
        if (divisor.intCompact != Long.MIN_VALUE) {
            return BigDecimal.divide(dividend.intVal, xscale, divisor.intCompact, yscale, preferredScale, mc);
        }
        return BigDecimal.divide(dividend.intVal, xscale, divisor.intVal, yscale, preferredScale, mc);
    }

    public BigDecimal divideToIntegralValue(BigDecimal divisor) {
        int preferredScale = BigDecimal.saturateLong((long)this.scale - (long)divisor.scale);
        if (this.compareMagnitude(divisor) < 0) {
            return BigDecimal.zeroValueOf(preferredScale);
        }
        if (this.signum() == 0 && divisor.signum() != 0) {
            return this.setScale(preferredScale, 7);
        }
        int maxDigits = (int)Math.min((long)this.precision() + (long)Math.ceil(10.0 * (double)divisor.precision() / 3.0) + Math.abs((long)this.scale() - (long)divisor.scale()) + 2L, Integer.MAX_VALUE);
        BigDecimal quotient = this.divide(divisor, new MathContext(maxDigits, RoundingMode.DOWN));
        if (quotient.scale > 0) {
            quotient = quotient.setScale(0, RoundingMode.DOWN);
            quotient = BigDecimal.stripZerosToMatchScale(quotient.intVal, quotient.intCompact, quotient.scale, preferredScale);
        }
        if (quotient.scale < preferredScale) {
            quotient = quotient.setScale(preferredScale, 7);
        }
        return quotient;
    }

    public BigDecimal divideToIntegralValue(BigDecimal divisor, MathContext mc) {
        int precisionDiff;
        if (mc.precision == 0 || this.compareMagnitude(divisor) < 0) {
            return this.divideToIntegralValue(divisor);
        }
        int preferredScale = BigDecimal.saturateLong((long)this.scale - (long)divisor.scale);
        BigDecimal result = this.divide(divisor, new MathContext(mc.precision, RoundingMode.DOWN));
        if (result.scale() < 0) {
            BigDecimal product = result.multiply(divisor);
            if (this.subtract(product).compareMagnitude(divisor) >= 0) {
                throw new ArithmeticException("Division impossible");
            }
        } else if (result.scale() > 0) {
            result = result.setScale(0, RoundingMode.DOWN);
        }
        if (preferredScale > result.scale() && (precisionDiff = mc.precision - result.precision()) > 0) {
            return result.setScale(result.scale() + Math.min(precisionDiff, preferredScale - result.scale));
        }
        return BigDecimal.stripZerosToMatchScale(result.intVal, result.intCompact, result.scale, preferredScale);
    }

    public BigDecimal remainder(BigDecimal divisor) {
        BigDecimal[] divrem = this.divideAndRemainder(divisor);
        return divrem[1];
    }

    public BigDecimal remainder(BigDecimal divisor, MathContext mc) {
        BigDecimal[] divrem = this.divideAndRemainder(divisor, mc);
        return divrem[1];
    }

    public BigDecimal[] divideAndRemainder(BigDecimal divisor) {
        BigDecimal[] result;
        result = new BigDecimal[]{this.divideToIntegralValue(divisor), this.subtract(result[0].multiply(divisor))};
        return result;
    }

    public BigDecimal[] divideAndRemainder(BigDecimal divisor, MathContext mc) {
        if (mc.precision == 0) {
            return this.divideAndRemainder(divisor);
        }
        BigDecimal[] result = new BigDecimal[2];
        BigDecimal lhs = this;
        result[0] = lhs.divideToIntegralValue(divisor, mc);
        result[1] = lhs.subtract(result[0].multiply(divisor));
        return result;
    }

    public BigDecimal sqrt(MathContext mc) {
        int signum = this.signum();
        if (signum == 1) {
            BigDecimal result;
            int targetPrecision;
            int preferredScale = this.scale() / 2;
            BigDecimal zeroWithFinalPreferredScale = BigDecimal.valueOf(0L, preferredScale);
            BigDecimal stripped = this.stripTrailingZeros();
            int strippedScale = stripped.scale();
            if (stripped.isPowerOfTen() && strippedScale % 2 == 0) {
                BigDecimal result2 = BigDecimal.valueOf(1L, strippedScale / 2);
                if (result2.scale() != preferredScale) {
                    result2 = result2.add(zeroWithFinalPreferredScale, mc);
                }
                return result2;
            }
            int scaleAdjust = 0;
            int scale = stripped.scale() - stripped.precision() + 1;
            scaleAdjust = scale % 2 == 0 ? scale : scale - 1;
            BigDecimal working = stripped.scaleByPowerOfTen(scaleAdjust);
            assert (ONE_TENTH.compareTo(working) <= 0 && working.compareTo(TEN) < 0);
            BigDecimal guess = new BigDecimal(Math.sqrt(working.doubleValue()));
            int guessPrecision = 15;
            int originalPrecision = mc.getPrecision();
            if (originalPrecision == 0) {
                targetPrecision = stripped.precision() / 2 + 1;
            } else {
                switch (mc.getRoundingMode()) {
                    case HALF_UP: 
                    case HALF_DOWN: 
                    case HALF_EVEN: {
                        targetPrecision = 2 * originalPrecision;
                        if (targetPrecision >= 0) break;
                        targetPrecision = 0x7FFFFFFD;
                        break;
                    }
                    default: {
                        targetPrecision = originalPrecision;
                    }
                }
            }
            BigDecimal approx = guess;
            int workingPrecision = working.precision();
            do {
                int tmpPrecision = Math.max(Math.max(guessPrecision, targetPrecision + 2), workingPrecision);
                MathContext mcTmp = new MathContext(tmpPrecision, RoundingMode.HALF_EVEN);
                approx = ONE_HALF.multiply(approx.add(working.divide(approx, mcTmp), mcTmp));
            } while ((guessPrecision *= 2) < targetPrecision + 2);
            RoundingMode targetRm = mc.getRoundingMode();
            if (targetRm == RoundingMode.UNNECESSARY || originalPrecision == 0) {
                RoundingMode tmpRm = targetRm == RoundingMode.UNNECESSARY ? RoundingMode.DOWN : targetRm;
                MathContext mcTmp = new MathContext(targetPrecision, tmpRm);
                result = approx.scaleByPowerOfTen(-scaleAdjust / 2).round(mcTmp);
                if (this.subtract(result.square()).compareTo(ZERO) != 0) {
                    throw new ArithmeticException("Computed square root not exact.");
                }
            } else {
                result = approx.scaleByPowerOfTen(-scaleAdjust / 2).round(mc);
                switch (targetRm) {
                    case DOWN: 
                    case FLOOR: {
                        if (result.square().compareTo(this) <= 0) break;
                        BigDecimal ulp = result.ulp();
                        if (approx.compareTo(ONE) == 0) {
                            ulp = ulp.multiply(ONE_TENTH);
                        }
                        result = result.subtract(ulp);
                        break;
                    }
                    case UP: 
                    case CEILING: {
                        if (result.square().compareTo(this) >= 0) break;
                        result = result.add(result.ulp());
                        break;
                    }
                }
            }
            assert (this.squareRootResultAssertions(result, mc));
            if (result.scale() != preferredScale) {
                result = result.stripTrailingZeros().add(zeroWithFinalPreferredScale, new MathContext(originalPrecision, RoundingMode.UNNECESSARY));
            }
            return result;
        }
        BigDecimal result = null;
        switch (signum) {
            case -1: {
                throw new ArithmeticException("Attempted square root of negative BigDecimal");
            }
            case 0: {
                result = BigDecimal.valueOf(0L, this.scale() / 2);
                assert (this.squareRootResultAssertions(result, mc));
                return result;
            }
        }
        throw new AssertionError((Object)"Bad value from signum");
    }

    private BigDecimal square() {
        return this.multiply(this);
    }

    private boolean isPowerOfTen() {
        return BigInteger.ONE.equals(this.unscaledValue());
    }

    private boolean squareRootResultAssertions(BigDecimal result, MathContext mc) {
        if (result.signum() == 0) {
            return this.squareRootZeroResultAssertions(result, mc);
        }
        RoundingMode rm = mc.getRoundingMode();
        BigDecimal ulp = result.ulp();
        BigDecimal neighborUp = result.add(ulp);
        if (result.isPowerOfTen()) {
            ulp = ulp.divide(TEN);
        }
        BigDecimal neighborDown = result.subtract(ulp);
        assert (result.signum() == 1 && this.signum() == 1) : "Bad signum of this and/or its sqrt.";
        switch (rm) {
            case DOWN: 
            case FLOOR: {
                assert (result.square().compareTo(this) <= 0 && neighborUp.square().compareTo(this) > 0) : "Square of result out for bounds rounding " + (Object)((Object)rm);
                return true;
            }
            case UP: 
            case CEILING: {
                assert (result.square().compareTo(this) >= 0 && neighborDown.square().compareTo(this) < 0) : "Square of result out for bounds rounding " + (Object)((Object)rm);
                return true;
            }
            case HALF_UP: 
            case HALF_DOWN: 
            case HALF_EVEN: {
                BigDecimal err = result.square().subtract(this).abs();
                BigDecimal errUp = neighborUp.square().subtract(this);
                BigDecimal errDown = this.subtract(neighborDown.square());
                int err_comp_errUp = err.compareTo(errUp);
                int err_comp_errDown = err.compareTo(errDown);
                assert (errUp.signum() == 1 && errDown.signum() == 1) : "Errors of neighbors squared don't have correct signs";
                assert (err_comp_errUp <= 0 || err_comp_errDown <= 0) : "Computed square root has larger error than neighbors for " + (Object)((Object)rm);
                assert (!(err_comp_errUp == 0 && err_comp_errDown >= 0 || err_comp_errDown == 0 && err_comp_errUp >= 0)) : "Incorrect error relationships";
                return true;
            }
        }
        return true;
    }

    private boolean squareRootZeroResultAssertions(BigDecimal result, MathContext mc) {
        return this.compareTo(ZERO) == 0;
    }

    public BigDecimal pow(int n) {
        if (n < 0 || n > 999999999) {
            throw new ArithmeticException("Invalid operation");
        }
        int newScale = this.checkScale((long)this.scale * (long)n);
        return new BigDecimal(this.inflated().pow(n), newScale);
    }

    public BigDecimal pow(int n, MathContext mc) {
        if (mc.precision == 0) {
            return this.pow(n);
        }
        if (n < -999999999 || n > 999999999) {
            throw new ArithmeticException("Invalid operation");
        }
        if (n == 0) {
            return ONE;
        }
        BigDecimal lhs = this;
        MathContext workmc = mc;
        int mag = Math.abs(n);
        if (mc.precision > 0) {
            int elength = BigDecimal.longDigitLength(mag);
            if (elength > mc.precision) {
                throw new ArithmeticException("Invalid operation");
            }
            workmc = new MathContext(mc.precision + elength + 1, mc.roundingMode);
        }
        BigDecimal acc = ONE;
        boolean seenbit = false;
        int i = 1;
        while (true) {
            if ((mag += mag) < 0) {
                seenbit = true;
                acc = acc.multiply(lhs, workmc);
            }
            if (i == 31) break;
            if (seenbit) {
                acc = acc.multiply(acc, workmc);
            }
            ++i;
        }
        if (n < 0) {
            acc = ONE.divide(acc, workmc);
        }
        return BigDecimal.doRound(acc, mc);
    }

    public BigDecimal abs() {
        return this.signum() < 0 ? this.negate() : this;
    }

    public BigDecimal abs(MathContext mc) {
        return this.signum() < 0 ? this.negate(mc) : this.plus(mc);
    }

    public BigDecimal negate() {
        if (this.intCompact == Long.MIN_VALUE) {
            return new BigDecimal(this.intVal.negate(), Long.MIN_VALUE, this.scale, this.precision);
        }
        return BigDecimal.valueOf(-this.intCompact, this.scale, this.precision);
    }

    public BigDecimal negate(MathContext mc) {
        return this.negate().plus(mc);
    }

    public BigDecimal plus() {
        return this;
    }

    public BigDecimal plus(MathContext mc) {
        if (mc.precision == 0) {
            return this;
        }
        return BigDecimal.doRound(this, mc);
    }

    public int signum() {
        return this.intCompact != Long.MIN_VALUE ? Long.signum(this.intCompact) : this.intVal.signum();
    }

    public int scale() {
        return this.scale;
    }

    public int precision() {
        int result = this.precision;
        if (result == 0) {
            long s = this.intCompact;
            result = s != Long.MIN_VALUE ? BigDecimal.longDigitLength(s) : BigDecimal.bigDigitLength(this.intVal);
            this.precision = result;
        }
        return result;
    }

    public BigInteger unscaledValue() {
        return this.inflated();
    }

    public BigDecimal round(MathContext mc) {
        return this.plus(mc);
    }

    public BigDecimal setScale(int newScale, RoundingMode roundingMode) {
        return this.setScale(newScale, roundingMode.oldMode);
    }

    @Deprecated(since="9")
    public BigDecimal setScale(int newScale, int roundingMode) {
        if (roundingMode < 0 || roundingMode > 7) {
            throw new IllegalArgumentException("Invalid rounding mode");
        }
        int oldScale = this.scale;
        if (newScale == oldScale) {
            return this;
        }
        if (this.signum() == 0) {
            return BigDecimal.zeroValueOf(newScale);
        }
        if (this.intCompact != Long.MIN_VALUE) {
            long rs = this.intCompact;
            if (newScale > oldScale) {
                int raise = this.checkScale((long)newScale - (long)oldScale);
                if ((rs = BigDecimal.longMultiplyPowerTen(rs, raise)) != Long.MIN_VALUE) {
                    return BigDecimal.valueOf(rs, newScale);
                }
                BigInteger rb = this.bigMultiplyPowerTen(raise);
                return new BigDecimal(rb, Long.MIN_VALUE, newScale, this.precision > 0 ? this.precision + raise : 0);
            }
            int drop = this.checkScale((long)oldScale - (long)newScale);
            if (drop < LONG_TEN_POWERS_TABLE.length) {
                return BigDecimal.divideAndRound(rs, LONG_TEN_POWERS_TABLE[drop], newScale, roundingMode, newScale);
            }
            return BigDecimal.divideAndRound(this.inflated(), BigDecimal.bigTenToThe(drop), newScale, roundingMode, newScale);
        }
        if (newScale > oldScale) {
            int raise = this.checkScale((long)newScale - (long)oldScale);
            BigInteger rb = BigDecimal.bigMultiplyPowerTen(this.intVal, raise);
            return new BigDecimal(rb, Long.MIN_VALUE, newScale, this.precision > 0 ? this.precision + raise : 0);
        }
        int drop = this.checkScale((long)oldScale - (long)newScale);
        if (drop < LONG_TEN_POWERS_TABLE.length) {
            return BigDecimal.divideAndRound(this.intVal, LONG_TEN_POWERS_TABLE[drop], newScale, roundingMode, newScale);
        }
        return BigDecimal.divideAndRound(this.intVal, BigDecimal.bigTenToThe(drop), newScale, roundingMode, newScale);
    }

    public BigDecimal setScale(int newScale) {
        return this.setScale(newScale, 7);
    }

    public BigDecimal movePointLeft(int n) {
        if (n == 0 && this.scale >= 0) {
            return this;
        }
        int newScale = this.checkScale((long)this.scale + (long)n);
        BigDecimal num = new BigDecimal(this.intVal, this.intCompact, newScale, 0);
        return num.scale < 0 ? num.setScale(0, 7) : num;
    }

    public BigDecimal movePointRight(int n) {
        if (n == 0 && this.scale >= 0) {
            return this;
        }
        int newScale = this.checkScale((long)this.scale - (long)n);
        BigDecimal num = new BigDecimal(this.intVal, this.intCompact, newScale, 0);
        return num.scale < 0 ? num.setScale(0, 7) : num;
    }

    public BigDecimal scaleByPowerOfTen(int n) {
        return new BigDecimal(this.intVal, this.intCompact, this.checkScale((long)this.scale - (long)n), this.precision);
    }

    public BigDecimal stripTrailingZeros() {
        if (this.intCompact == 0L || this.intVal != null && this.intVal.signum() == 0) {
            return ZERO;
        }
        if (this.intCompact != Long.MIN_VALUE) {
            return BigDecimal.createAndStripZerosToMatchScale(this.intCompact, this.scale, Long.MIN_VALUE);
        }
        return BigDecimal.createAndStripZerosToMatchScale(this.intVal, this.scale, Long.MIN_VALUE);
    }

    @Override
    public int compareTo(BigDecimal val) {
        int ysign;
        int xsign;
        if (this.scale == val.scale) {
            long xs = this.intCompact;
            long ys = val.intCompact;
            if (xs != Long.MIN_VALUE && ys != Long.MIN_VALUE) {
                return xs != ys ? (xs > ys ? 1 : -1) : 0;
            }
        }
        if ((xsign = this.signum()) != (ysign = val.signum())) {
            return xsign > ysign ? 1 : -1;
        }
        if (xsign == 0) {
            return 0;
        }
        int cmp = this.compareMagnitude(val);
        return xsign > 0 ? cmp : -cmp;
    }

    private int compareMagnitude(BigDecimal val) {
        long ys = val.intCompact;
        long xs = this.intCompact;
        if (xs == 0L) {
            return ys == 0L ? 0 : -1;
        }
        if (ys == 0L) {
            return 1;
        }
        long sdiff = (long)this.scale - (long)val.scale;
        if (sdiff != 0L) {
            long yae;
            long xae = (long)this.precision() - (long)this.scale;
            if (xae < (yae = (long)val.precision() - (long)val.scale)) {
                return -1;
            }
            if (xae > yae) {
                return 1;
            }
            if (sdiff < 0L) {
                if (sdiff > Integer.MIN_VALUE && (xs == Long.MIN_VALUE || (xs = BigDecimal.longMultiplyPowerTen(xs, (int)(-sdiff))) == Long.MIN_VALUE) && ys == Long.MIN_VALUE) {
                    BigInteger rb = this.bigMultiplyPowerTen((int)(-sdiff));
                    return rb.compareMagnitude(val.intVal);
                }
            } else if (sdiff <= Integer.MAX_VALUE && (ys == Long.MIN_VALUE || (ys = BigDecimal.longMultiplyPowerTen(ys, (int)sdiff)) == Long.MIN_VALUE) && xs == Long.MIN_VALUE) {
                BigInteger rb = val.bigMultiplyPowerTen((int)sdiff);
                return this.intVal.compareMagnitude(rb);
            }
        }
        if (xs != Long.MIN_VALUE) {
            return ys != Long.MIN_VALUE ? BigDecimal.longCompareMagnitude(xs, ys) : -1;
        }
        if (ys != Long.MIN_VALUE) {
            return 1;
        }
        return this.intVal.compareMagnitude(val.intVal);
    }

    public boolean equals(Object x) {
        if (!(x instanceof BigDecimal)) {
            return false;
        }
        BigDecimal xDec = (BigDecimal)x;
        if (x == this) {
            return true;
        }
        if (this.scale != xDec.scale) {
            return false;
        }
        long s = this.intCompact;
        long xs = xDec.intCompact;
        if (s != Long.MIN_VALUE) {
            if (xs == Long.MIN_VALUE) {
                xs = BigDecimal.compactValFor(xDec.intVal);
            }
            return xs == s;
        }
        if (xs != Long.MIN_VALUE) {
            return xs == BigDecimal.compactValFor(this.intVal);
        }
        return this.inflated().equals(xDec.inflated());
    }

    public BigDecimal min(BigDecimal val) {
        return this.compareTo(val) <= 0 ? this : val;
    }

    public BigDecimal max(BigDecimal val) {
        return this.compareTo(val) >= 0 ? this : val;
    }

    public int hashCode() {
        if (this.intCompact != Long.MIN_VALUE) {
            long val2 = this.intCompact < 0L ? -this.intCompact : this.intCompact;
            int temp = (int)((long)((int)(val2 >>> 32) * 31) + (val2 & 0xFFFFFFFFL));
            return 31 * (this.intCompact < 0L ? -temp : temp) + this.scale;
        }
        return 31 * this.intVal.hashCode() + this.scale;
    }

    public String toString() {
        String sc = this.stringCache;
        if (sc == null) {
            this.stringCache = sc = this.layoutChars(true);
        }
        return sc;
    }

    public String toEngineeringString() {
        return this.layoutChars(false);
    }

    public String toPlainString() {
        if (this.scale == 0) {
            if (this.intCompact != Long.MIN_VALUE) {
                return Long.toString(this.intCompact);
            }
            return this.intVal.toString();
        }
        if (this.scale < 0) {
            if (this.signum() == 0) {
                return "0";
            }
            int trailingZeros = BigDecimal.checkScaleNonZero(-((long)this.scale));
            String str = this.intCompact != Long.MIN_VALUE ? Long.toString(this.intCompact) : this.intVal.toString();
            int len = str.length() + trailingZeros;
            if (len < 0) {
                throw new OutOfMemoryError("too large to fit in a String");
            }
            StringBuilder buf = new StringBuilder(len);
            buf.append(str);
            buf.repeat(48, trailingZeros);
            return buf.toString();
        }
        String str = this.intCompact != Long.MIN_VALUE ? Long.toString(Math.abs(this.intCompact)) : this.intVal.abs().toString();
        return BigDecimal.getValueString(this.signum(), str, this.scale);
    }

    private static String getValueString(int signum, String intString, int scale) {
        StringBuilder buf;
        int insertionPoint = intString.length() - scale;
        if (insertionPoint == 0) {
            return (signum < 0 ? "-0." : "0.") + intString;
        }
        if (insertionPoint > 0) {
            buf = new StringBuilder(intString);
            buf.insert(insertionPoint, '.');
            if (signum < 0) {
                buf.insert(0, '-');
            }
        } else {
            int len = (signum < 0 ? 3 : 2) + scale;
            if (len < 0) {
                throw new OutOfMemoryError("too large to fit in a String");
            }
            buf = new StringBuilder(len);
            buf.append(signum < 0 ? "-0." : "0.");
            buf.repeat(48, -insertionPoint);
            buf.append(intString);
        }
        return buf.toString();
    }

    public BigInteger toBigInteger() {
        return this.setScale(0, 1).inflated();
    }

    public BigInteger toBigIntegerExact() {
        return this.setScale(0, 7).inflated();
    }

    @Override
    public long longValue() {
        if (this.intCompact != Long.MIN_VALUE && this.scale == 0) {
            return this.intCompact;
        }
        if (this.signum() == 0 || this.fractionOnly() || this.scale <= -64) {
            return 0L;
        }
        return this.toBigInteger().longValue();
    }

    private boolean fractionOnly() {
        assert (this.signum() != 0);
        return this.precision() <= this.scale;
    }

    public long longValueExact() {
        if (this.intCompact != Long.MIN_VALUE && this.scale == 0) {
            return this.intCompact;
        }
        if (this.signum() == 0) {
            return 0L;
        }
        if (this.fractionOnly()) {
            throw new ArithmeticException("Rounding necessary");
        }
        if (this.precision() - 19 > this.scale) {
            throw new ArithmeticException("Overflow");
        }
        BigDecimal num = this.setScale(0, 7);
        if (num.precision() >= 19) {
            LongOverflow.check(num);
        }
        return num.inflated().longValue();
    }

    @Override
    public int intValue() {
        return this.intCompact != Long.MIN_VALUE && this.scale == 0 ? (int)this.intCompact : (int)this.longValue();
    }

    public int intValueExact() {
        long num = this.longValueExact();
        if ((long)((int)num) != num) {
            throw new ArithmeticException("Overflow");
        }
        return (int)num;
    }

    public short shortValueExact() {
        long num = this.longValueExact();
        if ((long)((short)num) != num) {
            throw new ArithmeticException("Overflow");
        }
        return (short)num;
    }

    public byte byteValueExact() {
        long num = this.longValueExact();
        if ((long)((byte)num) != num) {
            throw new ArithmeticException("Overflow");
        }
        return (byte)num;
    }

    @Override
    public float floatValue() {
        if (this.intCompact != Long.MIN_VALUE) {
            float v = this.intCompact;
            if (this.scale == 0) {
                return v;
            }
            if ((long)v == this.intCompact) {
                if (0 < this.scale && this.scale < FLOAT_10_POW.length) {
                    return v / FLOAT_10_POW[this.scale];
                }
                if (0 > this.scale && this.scale > -FLOAT_10_POW.length) {
                    return v * FLOAT_10_POW[-this.scale];
                }
            }
        }
        return this.fullFloatValue();
    }

    private float fullFloatValue() {
        BigInteger n;
        BigInteger m;
        if (this.intCompact == 0L) {
            return 0.0f;
        }
        BigInteger w = this.unscaledValue().abs();
        long qb = (long)w.bitLength() - (long)Math.ceil((double)this.scale * 3.321928094887362);
        if (qb < -151L) {
            return (float)this.signum() * 0.0f;
        }
        if (qb > 129L) {
            return (float)this.signum() * Float.POSITIVE_INFINITY;
        }
        if (this.scale < 0) {
            return (float)this.signum() * w.multiply(BigDecimal.bigTenToThe(-this.scale)).floatValue();
        }
        if (this.scale == 0) {
            return (float)this.signum() * w.floatValue();
        }
        int ql = (int)qb - 27;
        BigInteger pow10 = BigDecimal.bigTenToThe(this.scale);
        if (ql <= 0) {
            m = w.shiftLeft(-ql);
            n = pow10;
        } else {
            m = w;
            n = pow10.shiftLeft(ql);
        }
        BigInteger[] qr = m.divideAndRemainder(n);
        int i = qr[0].intValue();
        int sb = qr[1].signum();
        int dq = 6 - Integer.numberOfLeadingZeros(i);
        int eq = -151 - ql;
        if (dq >= eq) {
            return (float)this.signum() * Math.scalb(i | sb, ql);
        }
        int mask = (1 << eq) - 1;
        int j = i >> eq | Integer.signum(i & mask) | sb;
        return (float)this.signum() * Math.scalb(j, -151);
    }

    @Override
    public double doubleValue() {
        if (this.intCompact != Long.MIN_VALUE) {
            double v = this.intCompact;
            if (this.scale == 0) {
                return v;
            }
            if ((long)v == this.intCompact) {
                if (0 < this.scale && this.scale < DOUBLE_10_POW.length) {
                    return v / DOUBLE_10_POW[this.scale];
                }
                if (0 > this.scale && this.scale > -DOUBLE_10_POW.length) {
                    return v * DOUBLE_10_POW[-this.scale];
                }
            }
        }
        return this.fullDoubleValue();
    }

    private double fullDoubleValue() {
        BigInteger n;
        BigInteger m;
        if (this.intCompact == 0L) {
            return 0.0;
        }
        BigInteger w = this.unscaledValue().abs();
        long qb = (long)w.bitLength() - (long)Math.ceil((double)this.scale * 3.321928094887362);
        if (qb < -1076L) {
            return (double)this.signum() * 0.0;
        }
        if (qb > 1025L) {
            return (double)this.signum() * Double.POSITIVE_INFINITY;
        }
        if (this.scale < 0) {
            return (double)this.signum() * w.multiply(BigDecimal.bigTenToThe(-this.scale)).doubleValue();
        }
        if (this.scale == 0) {
            return (double)this.signum() * w.doubleValue();
        }
        int ql = (int)qb - 56;
        BigInteger pow10 = BigDecimal.bigTenToThe(this.scale);
        if (ql <= 0) {
            m = w.shiftLeft(-ql);
            n = pow10;
        } else {
            m = w;
            n = pow10.shiftLeft(ql);
        }
        BigInteger[] qr = m.divideAndRemainder(n);
        long i = qr[0].longValue();
        int sb = qr[1].signum();
        int dq = 9 - Long.numberOfLeadingZeros(i);
        int eq = -1076 - ql;
        if (dq >= eq) {
            return (double)this.signum() * Math.scalb((double)(i | (long)sb), ql);
        }
        long mask = (1L << eq) - 1L;
        long j = i >> eq | (long)Long.signum(i & mask) | (long)sb;
        return (double)this.signum() * Math.scalb((double)j, -1076);
    }

    public BigDecimal ulp() {
        return BigDecimal.valueOf(1L, this.scale(), 1);
    }

    private String layoutChars(boolean sci) {
        StringBuilder buf;
        block21: {
            long adjusted;
            block23: {
                int sig;
                int coeffLen;
                char[] coeff;
                int offset;
                block24: {
                    block22: {
                        block20: {
                            if (this.scale == 0) {
                                return this.intCompact != Long.MIN_VALUE ? Long.toString(this.intCompact) : this.intVal.toString();
                            }
                            if (this.scale == 2 && this.intCompact >= 0L && this.intCompact < Integer.MAX_VALUE) {
                                int lowInt = (int)this.intCompact % 100;
                                int highInt = (int)this.intCompact / 100;
                                return Integer.toString(highInt) + '.' + StringBuilderHelper.DIGIT_TENS[lowInt] + StringBuilderHelper.DIGIT_ONES[lowInt];
                            }
                            StringBuilderHelper sbHelper = new StringBuilderHelper();
                            if (this.intCompact != Long.MIN_VALUE) {
                                offset = sbHelper.putIntCompact(Math.abs(this.intCompact));
                                coeff = sbHelper.getCompactCharArray();
                            } else {
                                offset = 0;
                                coeff = this.intVal.abs().toString().toCharArray();
                            }
                            buf = sbHelper.getStringBuilder();
                            if (this.signum() < 0) {
                                buf.append('-');
                            }
                            coeffLen = coeff.length - offset;
                            adjusted = -((long)this.scale) + (long)(coeffLen - 1);
                            if (this.scale < 0 || adjusted < -6L) break block20;
                            int pad = this.scale - coeffLen;
                            if (pad >= 0) {
                                buf.append('0');
                                buf.append('.');
                                while (pad > 0) {
                                    buf.append('0');
                                    --pad;
                                }
                                buf.append(coeff, offset, coeffLen);
                            } else {
                                buf.append(coeff, offset, -pad);
                                buf.append('.');
                                buf.append(coeff, -pad + offset, this.scale);
                            }
                            break block21;
                        }
                        if (!sci) break block22;
                        buf.append(coeff[offset]);
                        if (coeffLen > 1) {
                            buf.append('.');
                            buf.append(coeff, offset + 1, coeffLen - 1);
                        }
                        break block23;
                    }
                    sig = (int)(adjusted % 3L);
                    if (sig < 0) {
                        sig += 3;
                    }
                    adjusted -= (long)sig;
                    ++sig;
                    if (this.signum() != 0) break block24;
                    switch (sig) {
                        case 1: {
                            buf.append('0');
                            break block23;
                        }
                        case 2: {
                            buf.append("0.00");
                            adjusted += 3L;
                            break block23;
                        }
                        case 3: {
                            buf.append("0.0");
                            adjusted += 3L;
                            break block23;
                        }
                        default: {
                            throw new AssertionError((Object)("Unexpected sig value " + sig));
                        }
                    }
                }
                if (sig >= coeffLen) {
                    buf.append(coeff, offset, coeffLen);
                    for (int i = sig - coeffLen; i > 0; --i) {
                        buf.append('0');
                    }
                } else {
                    buf.append(coeff, offset, sig);
                    buf.append('.');
                    buf.append(coeff, offset + sig, coeffLen - sig);
                }
            }
            if (adjusted != 0L) {
                buf.append('E');
                if (adjusted > 0L) {
                    buf.append('+');
                }
                buf.append(adjusted);
            }
        }
        return buf.toString();
    }

    private static BigInteger bigTenToThe(int n) {
        if (n < 0) {
            return BigInteger.ZERO;
        }
        if (n < BIG_TEN_POWERS_TABLE_MAX) {
            BigInteger[] pows = BIG_TEN_POWERS_TABLE;
            if (n < pows.length) {
                return pows[n];
            }
            return BigDecimal.expandBigIntegerTenPowers(n);
        }
        return BigInteger.TEN.pow(n);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static BigInteger expandBigIntegerTenPowers(int n) {
        Class<BigDecimal> clazz = BigDecimal.class;
        synchronized (BigDecimal.class) {
            BigInteger[] pows = BIG_TEN_POWERS_TABLE;
            int curLen = pows.length;
            if (curLen <= n) {
                int newLen;
                for (newLen = curLen << 1; newLen <= n; newLen <<= 1) {
                }
                pows = Arrays.copyOf(pows, newLen);
                for (int i = curLen; i < newLen; ++i) {
                    pows[i] = pows[i - 1].multiply(BigInteger.TEN);
                }
                BIG_TEN_POWERS_TABLE = pows;
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return pows[n];
        }
    }

    private static long longMultiplyPowerTen(long val, int n) {
        if (val == 0L || n <= 0) {
            return val;
        }
        long[] tab = LONG_TEN_POWERS_TABLE;
        long[] bounds = THRESHOLDS_TABLE;
        if (n < tab.length && n < bounds.length) {
            long tenpower = tab[n];
            if (val == 1L) {
                return tenpower;
            }
            if (Math.abs(val) <= bounds[n]) {
                return val * tenpower;
            }
        }
        return Long.MIN_VALUE;
    }

    private BigInteger bigMultiplyPowerTen(int n) {
        if (n <= 0) {
            return this.inflated();
        }
        if (this.intCompact != Long.MIN_VALUE) {
            return BigDecimal.bigTenToThe(n).multiply(this.intCompact);
        }
        return this.intVal.multiply(BigDecimal.bigTenToThe(n));
    }

    private BigInteger inflated() {
        if (this.intVal == null) {
            return BigInteger.valueOf(this.intCompact);
        }
        return this.intVal;
    }

    private static void matchScale(BigDecimal[] val) {
        if (val[0].scale < val[1].scale) {
            val[0] = val[0].setScale(val[1].scale, 7);
        } else if (val[1].scale < val[0].scale) {
            val[1] = val[1].setScale(val[0].scale, 7);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = s.readFields();
        BigInteger serialIntVal = (BigInteger)fields.get("intVal", null);
        if (serialIntVal == null) {
            throw new StreamCorruptedException("Null or missing intVal in BigDecimal stream");
        }
        serialIntVal = BigDecimal.toStrictBigInteger(serialIntVal);
        int serialScale = fields.get("scale", 0);
        UnsafeHolder.setIntValAndScale(this, serialIntVal, serialScale);
    }

    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("Deserialized BigDecimal objects need data");
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        if (this.intVal == null) {
            UnsafeHolder.setIntValVolatile(this, BigInteger.valueOf(this.intCompact));
        }
        s.defaultWriteObject();
    }

    static int longDigitLength(long x) {
        long[] tab;
        assert (x != Long.MIN_VALUE);
        if (x < 0L) {
            x = -x;
        }
        if (x < 10L) {
            return 1;
        }
        int r = (64 - Long.numberOfLeadingZeros(x) + 1) * 1233 >>> 12;
        return r >= (tab = LONG_TEN_POWERS_TABLE).length || x < tab[r] ? r : r + 1;
    }

    private static int bigDigitLength(BigInteger b) {
        if (b.signum == 0) {
            return 1;
        }
        int r = (int)(((long)b.bitLength() + 1L) * 646456993L >>> 31);
        return b.compareMagnitude(BigDecimal.bigTenToThe(r)) < 0 ? r : r + 1;
    }

    private int checkScale(long val) {
        int asInt = (int)val;
        if ((long)asInt != val) {
            BigInteger b;
            int n = asInt = val > Integer.MAX_VALUE ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            if (this.intCompact != 0L && ((b = this.intVal) == null || b.signum() != 0)) {
                throw new ArithmeticException(asInt > 0 ? "Underflow" : "Overflow");
            }
        }
        return asInt;
    }

    private static long compactValFor(BigInteger b) {
        int[] m = b.mag;
        int len = m.length;
        if (len == 0) {
            return 0L;
        }
        int d = m[0];
        if (len > 2 || len == 2 && d < 0) {
            return Long.MIN_VALUE;
        }
        long u = len == 2 ? ((long)m[1] & 0xFFFFFFFFL) + ((long)d << 32) : (long)d & 0xFFFFFFFFL;
        return b.signum < 0 ? -u : u;
    }

    private static int longCompareMagnitude(long x, long y) {
        if (x < 0L) {
            x = -x;
        }
        if (y < 0L) {
            y = -y;
        }
        return Long.compare(x, y);
    }

    private static int saturateLong(long s) {
        int i = (int)s;
        return s == (long)i ? i : (s < 0L ? Integer.MIN_VALUE : Integer.MAX_VALUE);
    }

    private static void print(String name, BigDecimal bd) {
        System.err.format("%s:\tintCompact %d\tintVal %d\tscale %d\tprecision %d%n", name, bd.intCompact, bd.intVal, bd.scale, bd.precision);
    }

    private BigDecimal audit() {
        if (this.intCompact == Long.MIN_VALUE) {
            if (this.intVal == null) {
                BigDecimal.print("audit", this);
                throw new AssertionError((Object)"null intVal");
            }
            if (this.precision > 0 && this.precision != BigDecimal.bigDigitLength(this.intVal)) {
                BigDecimal.print("audit", this);
                throw new AssertionError((Object)"precision mismatch");
            }
        } else {
            long val;
            if (this.intVal != null && (val = this.intVal.longValue()) != this.intCompact) {
                BigDecimal.print("audit", this);
                throw new AssertionError((Object)("Inconsistent state, intCompact=" + this.intCompact + "\t intVal=" + val));
            }
            if (this.precision > 0 && this.precision != BigDecimal.longDigitLength(this.intCompact)) {
                BigDecimal.print("audit", this);
                throw new AssertionError((Object)"precision mismatch");
            }
        }
        return this;
    }

    private static int checkScaleNonZero(long val) {
        int asInt = (int)val;
        if ((long)asInt != val) {
            throw new ArithmeticException(asInt > 0 ? "Underflow" : "Overflow");
        }
        return asInt;
    }

    private static int checkScale(long intCompact, long val) {
        int asInt = (int)val;
        if ((long)asInt != val) {
            int n = asInt = val > Integer.MAX_VALUE ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            if (intCompact != 0L) {
                throw new ArithmeticException(asInt > 0 ? "Underflow" : "Overflow");
            }
        }
        return asInt;
    }

    private static int checkScale(BigInteger intVal, long val) {
        int asInt = (int)val;
        if ((long)asInt != val) {
            int n = asInt = val > Integer.MAX_VALUE ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            if (intVal.signum() != 0) {
                throw new ArithmeticException(asInt > 0 ? "Underflow" : "Overflow");
            }
        }
        return asInt;
    }

    private static BigDecimal doRound(BigDecimal val, MathContext mc) {
        int mcp = mc.precision;
        boolean wasDivided = false;
        if (mcp > 0) {
            int drop;
            BigInteger intVal = val.intVal;
            long compactVal = val.intCompact;
            int scale = val.scale;
            int prec = val.precision();
            int mode = mc.roundingMode.oldMode;
            if (compactVal == Long.MIN_VALUE) {
                drop = prec - mcp;
                while (drop > 0) {
                    scale = BigDecimal.checkScaleNonZero((long)scale - (long)drop);
                    intVal = BigDecimal.divideAndRoundByTenPow(intVal, drop, mode);
                    wasDivided = true;
                    compactVal = BigDecimal.compactValFor(intVal);
                    if (compactVal != Long.MIN_VALUE) {
                        prec = BigDecimal.longDigitLength(compactVal);
                        break;
                    }
                    prec = BigDecimal.bigDigitLength(intVal);
                    drop = prec - mcp;
                }
            }
            if (compactVal != Long.MIN_VALUE) {
                drop = prec - mcp;
                while (drop > 0) {
                    scale = BigDecimal.checkScaleNonZero((long)scale - (long)drop);
                    compactVal = BigDecimal.divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                    wasDivided = true;
                    prec = BigDecimal.longDigitLength(compactVal);
                    drop = prec - mcp;
                    intVal = null;
                }
            }
            return wasDivided ? new BigDecimal(intVal, compactVal, scale, prec) : val;
        }
        return val;
    }

    private static BigDecimal doRound(long compactVal, int scale, MathContext mc) {
        int mcp = mc.precision;
        if (mcp > 0 && mcp < 19) {
            int prec = BigDecimal.longDigitLength(compactVal);
            int drop = prec - mcp;
            while (drop > 0) {
                scale = BigDecimal.checkScaleNonZero((long)scale - (long)drop);
                compactVal = BigDecimal.divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                prec = BigDecimal.longDigitLength(compactVal);
                drop = prec - mcp;
            }
            return BigDecimal.valueOf(compactVal, scale, prec);
        }
        return BigDecimal.valueOf(compactVal, scale);
    }

    private static BigDecimal doRound(BigInteger intVal, int scale, MathContext mc) {
        int mcp = mc.precision;
        int prec = 0;
        if (mcp > 0) {
            int drop;
            long compactVal = BigDecimal.compactValFor(intVal);
            int mode = mc.roundingMode.oldMode;
            if (compactVal == Long.MIN_VALUE) {
                prec = BigDecimal.bigDigitLength(intVal);
                drop = prec - mcp;
                while (drop > 0) {
                    scale = BigDecimal.checkScaleNonZero((long)scale - (long)drop);
                    compactVal = BigDecimal.compactValFor(intVal = BigDecimal.divideAndRoundByTenPow(intVal, drop, mode));
                    if (compactVal != Long.MIN_VALUE) break;
                    prec = BigDecimal.bigDigitLength(intVal);
                    drop = prec - mcp;
                }
            }
            if (compactVal != Long.MIN_VALUE) {
                prec = BigDecimal.longDigitLength(compactVal);
                drop = prec - mcp;
                while (drop > 0) {
                    scale = BigDecimal.checkScaleNonZero((long)scale - (long)drop);
                    compactVal = BigDecimal.divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                    prec = BigDecimal.longDigitLength(compactVal);
                    drop = prec - mcp;
                }
                return BigDecimal.valueOf(compactVal, scale, prec);
            }
        }
        return new BigDecimal(intVal, Long.MIN_VALUE, scale, prec);
    }

    private static BigInteger divideAndRoundByTenPow(BigInteger intVal, int tenPow, int roundingMode) {
        intVal = tenPow < LONG_TEN_POWERS_TABLE.length ? BigDecimal.divideAndRound(intVal, LONG_TEN_POWERS_TABLE[tenPow], roundingMode) : BigDecimal.divideAndRound(intVal, BigDecimal.bigTenToThe(tenPow), roundingMode);
        return intVal;
    }

    private static BigDecimal divideAndRound(long ldividend, long ldivisor, int scale, int roundingMode, int preferredScale) {
        int qsign;
        long q = ldividend / ldivisor;
        if (roundingMode == 1 && scale == preferredScale) {
            return BigDecimal.valueOf(q, scale);
        }
        long r = ldividend % ldivisor;
        int n = qsign = ldividend < 0L == ldivisor < 0L ? 1 : -1;
        if (r != 0L) {
            boolean increment = BigDecimal.needIncrement(ldivisor, roundingMode, qsign, q, r);
            return BigDecimal.valueOf(increment ? q + (long)qsign : q, scale);
        }
        if (preferredScale != scale) {
            return BigDecimal.createAndStripZerosToMatchScale(q, scale, (long)preferredScale);
        }
        return BigDecimal.valueOf(q, scale);
    }

    private static long divideAndRound(long ldividend, long ldivisor, int roundingMode) {
        int qsign;
        long q = ldividend / ldivisor;
        if (roundingMode == 1) {
            return q;
        }
        long r = ldividend % ldivisor;
        int n = qsign = ldividend < 0L == ldivisor < 0L ? 1 : -1;
        if (r != 0L) {
            boolean increment = BigDecimal.needIncrement(ldivisor, roundingMode, qsign, q, r);
            return increment ? q + (long)qsign : q;
        }
        return q;
    }

    private static boolean commonNeedIncrement(int roundingMode, int qsign, int cmpFracHalf, boolean oddQuot) {
        switch (roundingMode) {
            case 7: {
                throw new ArithmeticException("Rounding necessary");
            }
            case 0: {
                return true;
            }
            case 1: {
                return false;
            }
            case 2: {
                return qsign > 0;
            }
            case 3: {
                return qsign < 0;
            }
        }
        assert (roundingMode >= 4 && roundingMode <= 6) : "Unexpected rounding mode" + (Object)((Object)RoundingMode.valueOf(roundingMode));
        if (cmpFracHalf < 0) {
            return false;
        }
        if (cmpFracHalf > 0) {
            return true;
        }
        assert (cmpFracHalf == 0);
        return switch (roundingMode) {
            case 5 -> false;
            case 4 -> true;
            case 6 -> oddQuot;
            default -> throw new AssertionError((Object)("Unexpected rounding mode" + roundingMode));
        };
    }

    private static boolean needIncrement(long ldivisor, int roundingMode, int qsign, long q, long r) {
        assert (r != 0L);
        int cmpFracHalf = r <= -4611686018427387904L || r > 0x3FFFFFFFFFFFFFFFL ? 1 : BigDecimal.longCompareMagnitude(2L * r, ldivisor);
        return BigDecimal.commonNeedIncrement(roundingMode, qsign, cmpFracHalf, (q & 1L) != 0L);
    }

    private static BigInteger divideAndRound(BigInteger bdividend, long ldivisor, int roundingMode) {
        int qsign;
        MutableBigInteger mdividend = new MutableBigInteger(bdividend.mag);
        MutableBigInteger mq = new MutableBigInteger();
        long r = mdividend.divide(ldivisor, mq);
        boolean isRemainderZero = r == 0L;
        int n = qsign = ldivisor < 0L ? -bdividend.signum : bdividend.signum;
        if (!isRemainderZero && BigDecimal.needIncrement(ldivisor, roundingMode, qsign, mq, r)) {
            mq.add(MutableBigInteger.ONE);
        }
        return mq.toBigInteger(qsign);
    }

    private static BigDecimal divideAndRound(BigInteger bdividend, long ldivisor, int scale, int roundingMode, int preferredScale) {
        int qsign;
        MutableBigInteger mdividend = new MutableBigInteger(bdividend.mag);
        MutableBigInteger mq = new MutableBigInteger();
        long r = mdividend.divide(ldivisor, mq);
        boolean isRemainderZero = r == 0L;
        int n = qsign = ldivisor < 0L ? -bdividend.signum : bdividend.signum;
        if (!isRemainderZero) {
            if (BigDecimal.needIncrement(ldivisor, roundingMode, qsign, mq, r)) {
                mq.add(MutableBigInteger.ONE);
            }
            return mq.toBigDecimal(qsign, scale);
        }
        if (preferredScale != scale) {
            long compactVal = mq.toCompactValue(qsign);
            if (compactVal != Long.MIN_VALUE) {
                return BigDecimal.createAndStripZerosToMatchScale(compactVal, scale, (long)preferredScale);
            }
            BigInteger intVal = mq.toBigInteger(qsign);
            return BigDecimal.createAndStripZerosToMatchScale(intVal, scale, (long)preferredScale);
        }
        return mq.toBigDecimal(qsign, scale);
    }

    private static boolean needIncrement(long ldivisor, int roundingMode, int qsign, MutableBigInteger mq, long r) {
        assert (r != 0L);
        int cmpFracHalf = r <= -4611686018427387904L || r > 0x3FFFFFFFFFFFFFFFL ? 1 : BigDecimal.longCompareMagnitude(2L * r, ldivisor);
        return BigDecimal.commonNeedIncrement(roundingMode, qsign, cmpFracHalf, mq.isOdd());
    }

    private static BigInteger divideAndRound(BigInteger bdividend, BigInteger bdivisor, int roundingMode) {
        int qsign;
        MutableBigInteger mdividend = new MutableBigInteger(bdividend.mag);
        MutableBigInteger mq = new MutableBigInteger();
        MutableBigInteger mdivisor = new MutableBigInteger(bdivisor.mag);
        MutableBigInteger mr = mdividend.divide(mdivisor, mq);
        boolean isRemainderZero = mr.isZero();
        int n = qsign = bdividend.signum != bdivisor.signum ? -1 : 1;
        if (!isRemainderZero && BigDecimal.needIncrement(mdivisor, roundingMode, qsign, mq, mr)) {
            mq.add(MutableBigInteger.ONE);
        }
        return mq.toBigInteger(qsign);
    }

    private static BigDecimal divideAndRound(BigInteger bdividend, BigInteger bdivisor, int scale, int roundingMode, int preferredScale) {
        int qsign;
        MutableBigInteger mdividend = new MutableBigInteger(bdividend.mag);
        MutableBigInteger mq = new MutableBigInteger();
        MutableBigInteger mdivisor = new MutableBigInteger(bdivisor.mag);
        MutableBigInteger mr = mdividend.divide(mdivisor, mq);
        boolean isRemainderZero = mr.isZero();
        int n = qsign = bdividend.signum != bdivisor.signum ? -1 : 1;
        if (!isRemainderZero) {
            if (BigDecimal.needIncrement(mdivisor, roundingMode, qsign, mq, mr)) {
                mq.add(MutableBigInteger.ONE);
            }
            return mq.toBigDecimal(qsign, scale);
        }
        if (preferredScale != scale) {
            long compactVal = mq.toCompactValue(qsign);
            if (compactVal != Long.MIN_VALUE) {
                return BigDecimal.createAndStripZerosToMatchScale(compactVal, scale, (long)preferredScale);
            }
            BigInteger intVal = mq.toBigInteger(qsign);
            return BigDecimal.createAndStripZerosToMatchScale(intVal, scale, (long)preferredScale);
        }
        return mq.toBigDecimal(qsign, scale);
    }

    private static boolean needIncrement(MutableBigInteger mdivisor, int roundingMode, int qsign, MutableBigInteger mq, MutableBigInteger mr) {
        assert (!mr.isZero());
        int cmpFracHalf = mr.compareHalf(mdivisor);
        return BigDecimal.commonNeedIncrement(roundingMode, qsign, cmpFracHalf, mq.isOdd());
    }

    private static BigDecimal createAndStripZerosToMatchScale(BigInteger intVal, int scale, long preferredScale) {
        BigInteger[] qr;
        while (intVal.compareMagnitude(BigInteger.TEN) >= 0 && (long)scale > preferredScale && !intVal.testBit(0) && (qr = intVal.divideAndRemainder(BigInteger.TEN))[1].signum() == 0) {
            intVal = qr[0];
            scale = BigDecimal.checkScale(intVal, (long)scale - 1L);
        }
        return BigDecimal.valueOf(intVal, scale, 0);
    }

    private static BigDecimal createAndStripZerosToMatchScale(long compactVal, int scale, long preferredScale) {
        long r;
        while (Math.abs(compactVal) >= 10L && (long)scale > preferredScale && (compactVal & 1L) == 0L && (r = compactVal % 10L) == 0L) {
            scale = BigDecimal.checkScale(compactVal /= 10L, (long)scale - 1L);
        }
        return BigDecimal.valueOf(compactVal, scale);
    }

    private static BigDecimal stripZerosToMatchScale(BigInteger intVal, long intCompact, int scale, int preferredScale) {
        if (intCompact != Long.MIN_VALUE) {
            return BigDecimal.createAndStripZerosToMatchScale(intCompact, scale, (long)preferredScale);
        }
        return BigDecimal.createAndStripZerosToMatchScale(intVal == null ? INFLATED_BIGINT : intVal, scale, (long)preferredScale);
    }

    private static long add(long xs, long ys) {
        long sum = xs + ys;
        if (((sum ^ xs) & (sum ^ ys)) >= 0L) {
            return sum;
        }
        return Long.MIN_VALUE;
    }

    private static BigDecimal add(long xs, long ys, int scale) {
        long sum = BigDecimal.add(xs, ys);
        if (sum != Long.MIN_VALUE) {
            return BigDecimal.valueOf(sum, scale);
        }
        return new BigDecimal(BigInteger.valueOf(xs).add(ys), scale);
    }

    private static BigDecimal add(long xs, int scale1, long ys, int scale2) {
        long sdiff = (long)scale1 - (long)scale2;
        if (sdiff == 0L) {
            return BigDecimal.add(xs, ys, scale1);
        }
        if (sdiff < 0L) {
            int raise = BigDecimal.checkScale(xs, -sdiff);
            long scaledX = BigDecimal.longMultiplyPowerTen(xs, raise);
            if (scaledX != Long.MIN_VALUE) {
                return BigDecimal.add(scaledX, ys, scale2);
            }
            BigInteger bigsum = BigDecimal.bigMultiplyPowerTen(xs, raise).add(ys);
            return (xs ^ ys) >= 0L ? new BigDecimal(bigsum, Long.MIN_VALUE, scale2, 0) : BigDecimal.valueOf(bigsum, scale2, 0);
        }
        int raise = BigDecimal.checkScale(ys, sdiff);
        long scaledY = BigDecimal.longMultiplyPowerTen(ys, raise);
        if (scaledY != Long.MIN_VALUE) {
            return BigDecimal.add(xs, scaledY, scale1);
        }
        BigInteger bigsum = BigDecimal.bigMultiplyPowerTen(ys, raise).add(xs);
        return (xs ^ ys) >= 0L ? new BigDecimal(bigsum, Long.MIN_VALUE, scale1, 0) : BigDecimal.valueOf(bigsum, scale1, 0);
    }

    private static BigDecimal add(long xs, int scale1, BigInteger snd, int scale2) {
        BigInteger sum;
        boolean sameSigns;
        int rscale = scale1;
        long sdiff = (long)rscale - (long)scale2;
        boolean bl = sameSigns = Long.signum(xs) == snd.signum;
        if (sdiff < 0L) {
            int raise = BigDecimal.checkScale(xs, -sdiff);
            rscale = scale2;
            long scaledX = BigDecimal.longMultiplyPowerTen(xs, raise);
            sum = scaledX == Long.MIN_VALUE ? snd.add(BigDecimal.bigMultiplyPowerTen(xs, raise)) : snd.add(scaledX);
        } else {
            int raise = BigDecimal.checkScale(snd, sdiff);
            snd = BigDecimal.bigMultiplyPowerTen(snd, raise);
            sum = snd.add(xs);
        }
        return sameSigns ? new BigDecimal(sum, Long.MIN_VALUE, rscale, 0) : BigDecimal.valueOf(sum, rscale, 0);
    }

    private static BigDecimal add(BigInteger fst, int scale1, BigInteger snd, int scale2) {
        int rscale = scale1;
        long sdiff = (long)rscale - (long)scale2;
        if (sdiff != 0L) {
            if (sdiff < 0L) {
                raise = BigDecimal.checkScale(fst, -sdiff);
                rscale = scale2;
                fst = BigDecimal.bigMultiplyPowerTen(fst, raise);
            } else {
                raise = BigDecimal.checkScale(snd, sdiff);
                snd = BigDecimal.bigMultiplyPowerTen(snd, raise);
            }
        }
        BigInteger sum = fst.add(snd);
        return fst.signum == snd.signum ? new BigDecimal(sum, Long.MIN_VALUE, rscale, 0) : BigDecimal.valueOf(sum, rscale, 0);
    }

    private static BigInteger bigMultiplyPowerTen(long value, int n) {
        if (n <= 0) {
            return BigInteger.valueOf(value);
        }
        return BigDecimal.bigTenToThe(n).multiply(value);
    }

    private static BigInteger bigMultiplyPowerTen(BigInteger value, int n) {
        if (n <= 0) {
            return value;
        }
        if (n < LONG_TEN_POWERS_TABLE.length) {
            return value.multiply(LONG_TEN_POWERS_TABLE[n]);
        }
        return value.multiply(BigDecimal.bigTenToThe(n));
    }

    private static BigDecimal divideSmallFastPath(long xs, int xscale, long ys, int yscale, long preferredScale, MathContext mc) {
        BigDecimal quotient;
        int mcp = mc.precision;
        int roundingMode = mc.roundingMode.oldMode;
        assert (xscale <= yscale && yscale < 18 && mcp < 18);
        int xraise = yscale - xscale;
        long scaledX = xraise == 0 ? xs : BigDecimal.longMultiplyPowerTen(xs, xraise);
        int cmp = BigDecimal.longCompareMagnitude(scaledX, ys);
        if (cmp > 0) {
            int scl = BigDecimal.checkScaleNonZero(preferredScale + (long)(--yscale) - (long)xscale + (long)mcp);
            if (BigDecimal.checkScaleNonZero((long)mcp + (long)yscale - (long)xscale) > 0) {
                int raise = BigDecimal.checkScaleNonZero((long)mcp + (long)yscale - (long)xscale);
                long scaledXs = BigDecimal.longMultiplyPowerTen(xs, raise);
                if (scaledXs == Long.MIN_VALUE) {
                    quotient = null;
                    if (mcp - 1 >= 0 && mcp - 1 < LONG_TEN_POWERS_TABLE.length) {
                        quotient = BigDecimal.multiplyDivideAndRound(LONG_TEN_POWERS_TABLE[mcp - 1], scaledX, ys, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
                    }
                    if (quotient == null) {
                        BigInteger rb = BigDecimal.bigMultiplyPowerTen(scaledX, mcp - 1);
                        quotient = BigDecimal.divideAndRound(rb, ys, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
                    }
                } else {
                    quotient = BigDecimal.divideAndRound(scaledXs, ys, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
                }
            } else {
                int newScale = BigDecimal.checkScaleNonZero((long)xscale - (long)mcp);
                if (newScale == yscale) {
                    quotient = BigDecimal.divideAndRound(xs, ys, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
                } else {
                    int raise = BigDecimal.checkScaleNonZero((long)newScale - (long)yscale);
                    long scaledYs = BigDecimal.longMultiplyPowerTen(ys, raise);
                    if (scaledYs == Long.MIN_VALUE) {
                        BigInteger rb = BigDecimal.bigMultiplyPowerTen(ys, raise);
                        quotient = BigDecimal.divideAndRound(BigInteger.valueOf(xs), rb, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
                    } else {
                        quotient = BigDecimal.divideAndRound(xs, scaledYs, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
                    }
                }
            }
        } else {
            int scl = BigDecimal.checkScaleNonZero(preferredScale + (long)yscale - (long)xscale + (long)mcp);
            if (cmp == 0) {
                quotient = BigDecimal.roundedTenPower(scaledX < 0L == ys < 0L ? 1 : -1, mcp, scl, BigDecimal.checkScaleNonZero(preferredScale));
            } else {
                long scaledXs = BigDecimal.longMultiplyPowerTen(scaledX, mcp);
                if (scaledXs == Long.MIN_VALUE) {
                    quotient = null;
                    if (mcp < LONG_TEN_POWERS_TABLE.length) {
                        quotient = BigDecimal.multiplyDivideAndRound(LONG_TEN_POWERS_TABLE[mcp], scaledX, ys, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
                    }
                    if (quotient == null) {
                        BigInteger rb = BigDecimal.bigMultiplyPowerTen(scaledX, mcp);
                        quotient = BigDecimal.divideAndRound(rb, ys, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
                    }
                } else {
                    quotient = BigDecimal.divideAndRound(scaledXs, ys, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
                }
            }
        }
        return BigDecimal.doRound(quotient, mc);
    }

    private static BigDecimal divide(long xs, int xscale, long ys, int yscale, long preferredScale, MathContext mc) {
        BigDecimal quotient;
        int mcp = mc.precision;
        if (xscale <= yscale && yscale < 18 && mcp < 18) {
            return BigDecimal.divideSmallFastPath(xs, xscale, ys, yscale, preferredScale, mc);
        }
        if (BigDecimal.compareMagnitudeNormalized(xs, xscale, ys, yscale) > 0) {
            --yscale;
        }
        int roundingMode = mc.roundingMode.oldMode;
        int scl = BigDecimal.checkScaleNonZero(preferredScale + (long)yscale - (long)xscale + (long)mcp);
        if (BigDecimal.checkScaleNonZero((long)mcp + (long)yscale - (long)xscale) > 0) {
            int raise = BigDecimal.checkScaleNonZero((long)mcp + (long)yscale - (long)xscale);
            long scaledXs = BigDecimal.longMultiplyPowerTen(xs, raise);
            if (scaledXs == Long.MIN_VALUE) {
                BigInteger rb = BigDecimal.bigMultiplyPowerTen(xs, raise);
                quotient = BigDecimal.divideAndRound(rb, ys, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
            } else {
                quotient = BigDecimal.divideAndRound(scaledXs, ys, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
            }
        } else {
            int newScale = BigDecimal.checkScaleNonZero((long)xscale - (long)mcp);
            if (newScale == yscale) {
                quotient = BigDecimal.divideAndRound(xs, ys, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
            } else {
                int raise = BigDecimal.checkScaleNonZero((long)newScale - (long)yscale);
                long scaledYs = BigDecimal.longMultiplyPowerTen(ys, raise);
                if (scaledYs == Long.MIN_VALUE) {
                    BigInteger rb = BigDecimal.bigMultiplyPowerTen(ys, raise);
                    quotient = BigDecimal.divideAndRound(BigInteger.valueOf(xs), rb, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
                } else {
                    quotient = BigDecimal.divideAndRound(xs, scaledYs, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
                }
            }
        }
        return BigDecimal.doRound(quotient, mc);
    }

    private static BigDecimal divide(BigInteger xs, int xscale, long ys, int yscale, long preferredScale, MathContext mc) {
        BigDecimal quotient;
        if (-BigDecimal.compareMagnitudeNormalized(ys, yscale, xs, xscale) > 0) {
            --yscale;
        }
        int mcp = mc.precision;
        int roundingMode = mc.roundingMode.oldMode;
        int scl = BigDecimal.checkScaleNonZero(preferredScale + (long)yscale - (long)xscale + (long)mcp);
        if (BigDecimal.checkScaleNonZero((long)mcp + (long)yscale - (long)xscale) > 0) {
            int raise = BigDecimal.checkScaleNonZero((long)mcp + (long)yscale - (long)xscale);
            BigInteger rb = BigDecimal.bigMultiplyPowerTen(xs, raise);
            quotient = BigDecimal.divideAndRound(rb, ys, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
        } else {
            int newScale = BigDecimal.checkScaleNonZero((long)xscale - (long)mcp);
            if (newScale == yscale) {
                quotient = BigDecimal.divideAndRound(xs, ys, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
            } else {
                int raise = BigDecimal.checkScaleNonZero((long)newScale - (long)yscale);
                long scaledYs = BigDecimal.longMultiplyPowerTen(ys, raise);
                if (scaledYs == Long.MIN_VALUE) {
                    BigInteger rb = BigDecimal.bigMultiplyPowerTen(ys, raise);
                    quotient = BigDecimal.divideAndRound(xs, rb, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
                } else {
                    quotient = BigDecimal.divideAndRound(xs, scaledYs, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
                }
            }
        }
        return BigDecimal.doRound(quotient, mc);
    }

    private static BigDecimal divide(long xs, int xscale, BigInteger ys, int yscale, long preferredScale, MathContext mc) {
        BigDecimal quotient;
        if (BigDecimal.compareMagnitudeNormalized(xs, xscale, ys, yscale) > 0) {
            --yscale;
        }
        int mcp = mc.precision;
        int roundingMode = mc.roundingMode.oldMode;
        int scl = BigDecimal.checkScaleNonZero(preferredScale + (long)yscale - (long)xscale + (long)mcp);
        if (BigDecimal.checkScaleNonZero((long)mcp + (long)yscale - (long)xscale) > 0) {
            int raise = BigDecimal.checkScaleNonZero((long)mcp + (long)yscale - (long)xscale);
            BigInteger rb = BigDecimal.bigMultiplyPowerTen(xs, raise);
            quotient = BigDecimal.divideAndRound(rb, ys, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
        } else {
            int newScale = BigDecimal.checkScaleNonZero((long)xscale - (long)mcp);
            int raise = BigDecimal.checkScaleNonZero((long)newScale - (long)yscale);
            BigInteger rb = BigDecimal.bigMultiplyPowerTen(ys, raise);
            quotient = BigDecimal.divideAndRound(BigInteger.valueOf(xs), rb, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
        }
        return BigDecimal.doRound(quotient, mc);
    }

    private static BigDecimal divide(BigInteger xs, int xscale, BigInteger ys, int yscale, long preferredScale, MathContext mc) {
        BigDecimal quotient;
        if (BigDecimal.compareMagnitudeNormalized(xs, xscale, ys, yscale) > 0) {
            --yscale;
        }
        int mcp = mc.precision;
        int roundingMode = mc.roundingMode.oldMode;
        int scl = BigDecimal.checkScaleNonZero(preferredScale + (long)yscale - (long)xscale + (long)mcp);
        if (BigDecimal.checkScaleNonZero((long)mcp + (long)yscale - (long)xscale) > 0) {
            int raise = BigDecimal.checkScaleNonZero((long)mcp + (long)yscale - (long)xscale);
            BigInteger rb = BigDecimal.bigMultiplyPowerTen(xs, raise);
            quotient = BigDecimal.divideAndRound(rb, ys, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
        } else {
            int newScale = BigDecimal.checkScaleNonZero((long)xscale - (long)mcp);
            int raise = BigDecimal.checkScaleNonZero((long)newScale - (long)yscale);
            BigInteger rb = BigDecimal.bigMultiplyPowerTen(ys, raise);
            quotient = BigDecimal.divideAndRound(xs, rb, scl, roundingMode, BigDecimal.checkScaleNonZero(preferredScale));
        }
        return BigDecimal.doRound(quotient, mc);
    }

    private static BigDecimal multiplyDivideAndRound(long dividend0, long dividend1, long divisor, int scale, int roundingMode, int preferredScale) {
        int qsign = Long.signum(dividend0) * Long.signum(dividend1) * Long.signum(divisor);
        dividend0 = Math.abs(dividend0);
        dividend1 = Math.abs(dividend1);
        divisor = Math.abs(divisor);
        long d0_hi = dividend0 >>> 32;
        long d0_lo = dividend0 & 0xFFFFFFFFL;
        long d1_hi = dividend1 >>> 32;
        long d1_lo = dividend1 & 0xFFFFFFFFL;
        long product = d0_lo * d1_lo;
        long d0 = product & 0xFFFFFFFFL;
        long d1 = product >>> 32;
        product = d0_hi * d1_lo + d1;
        d1 = product & 0xFFFFFFFFL;
        long d2 = product >>> 32;
        product = d0_lo * d1_hi + d1;
        d1 = product & 0xFFFFFFFFL;
        long d3 = (d2 += product >>> 32) >>> 32;
        product = d0_hi * d1_hi + (d2 &= 0xFFFFFFFFL);
        d2 = product & 0xFFFFFFFFL;
        d3 = (product >>> 32) + d3 & 0xFFFFFFFFL;
        long dividendHi = BigDecimal.make64(d3, d2);
        long dividendLo = BigDecimal.make64(d1, d0);
        return BigDecimal.divideAndRound128(dividendHi, dividendLo, divisor, qsign, scale, roundingMode, preferredScale);
    }

    private static BigDecimal divideAndRound128(long dividendHi, long dividendLo, long divisor, int sign, int scale, int roundingMode, int preferredScale) {
        long q0;
        long r_tmp;
        long q1;
        if (dividendHi >= divisor) {
            return null;
        }
        int shift = Long.numberOfLeadingZeros(divisor);
        long v1 = (divisor <<= shift) >>> 32;
        long v0 = divisor & 0xFFFFFFFFL;
        long tmp = dividendLo << shift;
        long u1 = tmp >>> 32;
        long u0 = tmp & 0xFFFFFFFFL;
        tmp = dividendHi << shift | dividendLo >>> 64 - shift;
        long u2 = tmp & 0xFFFFFFFFL;
        if (v1 == 1L) {
            q1 = tmp;
            r_tmp = 0L;
        } else if (tmp >= 0L) {
            q1 = tmp / v1;
            r_tmp = tmp - q1 * v1;
        } else {
            long[] rq = BigDecimal.divRemNegativeLong(tmp, v1);
            q1 = rq[1];
            r_tmp = rq[0];
        }
        while (q1 >= 0x100000000L || BigDecimal.unsignedLongCompare(q1 * v0, BigDecimal.make64(r_tmp, u1))) {
            --q1;
            if ((r_tmp += v1) < 0x100000000L) continue;
        }
        tmp = BigDecimal.mulsub(u2, u1, v1, v0, q1);
        u1 = tmp & 0xFFFFFFFFL;
        if (v1 == 1L) {
            q0 = tmp;
            r_tmp = 0L;
        } else if (tmp >= 0L) {
            q0 = tmp / v1;
            r_tmp = tmp - q0 * v1;
        } else {
            long[] rq = BigDecimal.divRemNegativeLong(tmp, v1);
            q0 = rq[1];
            r_tmp = rq[0];
        }
        while (q0 >= 0x100000000L || BigDecimal.unsignedLongCompare(q0 * v0, BigDecimal.make64(r_tmp, u0))) {
            --q0;
            if ((r_tmp += v1) < 0x100000000L) continue;
        }
        if ((int)q1 < 0) {
            MutableBigInteger mq = new MutableBigInteger(new int[]{(int)q1, (int)q0});
            if (roundingMode == 1 && scale == preferredScale) {
                return mq.toBigDecimal(sign, scale);
            }
            long r = BigDecimal.mulsub(u1, u0, v1, v0, q0) >>> shift;
            if (r != 0L) {
                if (BigDecimal.needIncrement(divisor >>> shift, roundingMode, sign, mq, r)) {
                    mq.add(MutableBigInteger.ONE);
                }
                return mq.toBigDecimal(sign, scale);
            }
            if (preferredScale != scale) {
                BigInteger intVal = mq.toBigInteger(sign);
                return BigDecimal.createAndStripZerosToMatchScale(intVal, scale, (long)preferredScale);
            }
            return mq.toBigDecimal(sign, scale);
        }
        long q = BigDecimal.make64(q1, q0);
        q *= (long)sign;
        if (roundingMode == 1 && scale == preferredScale) {
            return BigDecimal.valueOf(q, scale);
        }
        long r = BigDecimal.mulsub(u1, u0, v1, v0, q0) >>> shift;
        if (r != 0L) {
            boolean increment = BigDecimal.needIncrement(divisor >>> shift, roundingMode, sign, q, r);
            return BigDecimal.valueOf(increment ? q + (long)sign : q, scale);
        }
        if (preferredScale != scale) {
            return BigDecimal.createAndStripZerosToMatchScale(q, scale, (long)preferredScale);
        }
        return BigDecimal.valueOf(q, scale);
    }

    private static BigDecimal roundedTenPower(int qsign, int raise, int scale, int preferredScale) {
        if (scale > preferredScale) {
            int diff = scale - preferredScale;
            if (diff < raise) {
                return BigDecimal.scaledTenPow(raise - diff, qsign, preferredScale);
            }
            return BigDecimal.valueOf(qsign, scale - raise);
        }
        return BigDecimal.scaledTenPow(raise, qsign, scale);
    }

    static BigDecimal scaledTenPow(int n, int sign, int scale) {
        if (n < LONG_TEN_POWERS_TABLE.length) {
            return BigDecimal.valueOf((long)sign * LONG_TEN_POWERS_TABLE[n], scale);
        }
        BigInteger unscaledVal = BigDecimal.bigTenToThe(n);
        if (sign == -1) {
            unscaledVal = unscaledVal.negate();
        }
        return new BigDecimal(unscaledVal, Long.MIN_VALUE, scale, n + 1);
    }

    private static long[] divRemNegativeLong(long n, long d) {
        assert (n < 0L) : "Non-negative numerator " + n;
        assert (d != 1L) : "Unity denominator";
        long q = (n >>> 1) / (d >>> 1);
        long r = n - q * d;
        while (r < 0L) {
            r += d;
            --q;
        }
        while (r >= d) {
            r -= d;
            ++q;
        }
        return new long[]{r, q};
    }

    private static long make64(long hi, long lo) {
        return hi << 32 | lo;
    }

    private static long mulsub(long u1, long u0, long v1, long v0, long q0) {
        long tmp = u0 - q0 * v0;
        return BigDecimal.make64(u1 + (tmp >>> 32) - q0 * v1, tmp & 0xFFFFFFFFL);
    }

    private static boolean unsignedLongCompare(long one, long two) {
        return one + Long.MIN_VALUE > two + Long.MIN_VALUE;
    }

    private static boolean unsignedLongCompareEq(long one, long two) {
        return one + Long.MIN_VALUE >= two + Long.MIN_VALUE;
    }

    private static int compareMagnitudeNormalized(long xs, int xscale, long ys, int yscale) {
        int sdiff = xscale - yscale;
        if (sdiff != 0) {
            if (sdiff < 0) {
                xs = BigDecimal.longMultiplyPowerTen(xs, -sdiff);
            } else {
                ys = BigDecimal.longMultiplyPowerTen(ys, sdiff);
            }
        }
        if (xs != Long.MIN_VALUE) {
            return ys != Long.MIN_VALUE ? BigDecimal.longCompareMagnitude(xs, ys) : -1;
        }
        return 1;
    }

    private static int compareMagnitudeNormalized(long xs, int xscale, BigInteger ys, int yscale) {
        if (xs == 0L) {
            return -1;
        }
        int sdiff = xscale - yscale;
        if (sdiff < 0 && BigDecimal.longMultiplyPowerTen(xs, -sdiff) == Long.MIN_VALUE) {
            return BigDecimal.bigMultiplyPowerTen(xs, -sdiff).compareMagnitude(ys);
        }
        return -1;
    }

    private static int compareMagnitudeNormalized(BigInteger xs, int xscale, BigInteger ys, int yscale) {
        int sdiff = xscale - yscale;
        if (sdiff < 0) {
            return BigDecimal.bigMultiplyPowerTen(xs, -sdiff).compareMagnitude(ys);
        }
        return xs.compareMagnitude(BigDecimal.bigMultiplyPowerTen(ys, sdiff));
    }

    private static long multiply(long x, long y) {
        long ay;
        long product = x * y;
        long ax = Math.abs(x);
        if ((ax | (ay = Math.abs(y))) >>> 31 == 0L || y == 0L || product / y == x) {
            return product;
        }
        return Long.MIN_VALUE;
    }

    private static BigDecimal multiply(long x, long y, int scale) {
        long product = BigDecimal.multiply(x, y);
        if (product != Long.MIN_VALUE) {
            return BigDecimal.valueOf(product, scale);
        }
        return new BigDecimal(BigInteger.valueOf(x).multiply(y), Long.MIN_VALUE, scale, 0);
    }

    private static BigDecimal multiply(long x, BigInteger y, int scale) {
        if (x == 0L) {
            return BigDecimal.zeroValueOf(scale);
        }
        return new BigDecimal(y.multiply(x), Long.MIN_VALUE, scale, 0);
    }

    private static BigDecimal multiply(BigInteger x, BigInteger y, int scale) {
        return new BigDecimal(x.multiply(y), Long.MIN_VALUE, scale, 0);
    }

    private static BigDecimal multiplyAndRound(long x, long y, int scale, MathContext mc) {
        long mLo;
        long product = BigDecimal.multiply(x, y);
        if (product != Long.MIN_VALUE) {
            return BigDecimal.doRound(product, scale, mc);
        }
        int rsign = 1;
        if (x < 0L) {
            x = -x;
            rsign = -1;
        }
        if (y < 0L) {
            y = -y;
            rsign *= -1;
        }
        long m0_hi = x >>> 32;
        long m0_lo = x & 0xFFFFFFFFL;
        long m1_hi = y >>> 32;
        long m1_lo = y & 0xFFFFFFFFL;
        product = m0_lo * m1_lo;
        long m0 = product & 0xFFFFFFFFL;
        long m1 = product >>> 32;
        product = m0_hi * m1_lo + m1;
        m1 = product & 0xFFFFFFFFL;
        long m2 = product >>> 32;
        product = m0_lo * m1_hi + m1;
        m1 = product & 0xFFFFFFFFL;
        long m3 = (m2 += product >>> 32) >>> 32;
        m2 &= 0xFFFFFFFFL;
        product = m0_hi * m1_hi + m2;
        long mHi = BigDecimal.make64(m3 = (product >>> 32) + m3 & 0xFFFFFFFFL, m2 = product & 0xFFFFFFFFL);
        BigDecimal res = BigDecimal.doRound128(mHi, mLo = BigDecimal.make64(m1, m0), rsign, scale, mc);
        if (res != null) {
            return res;
        }
        res = new BigDecimal(BigInteger.valueOf(x).multiply(y * (long)rsign), Long.MIN_VALUE, scale, 0);
        return BigDecimal.doRound(res, mc);
    }

    private static BigDecimal multiplyAndRound(long x, BigInteger y, int scale, MathContext mc) {
        if (x == 0L) {
            return BigDecimal.zeroValueOf(scale);
        }
        return BigDecimal.doRound(y.multiply(x), scale, mc);
    }

    private static BigDecimal multiplyAndRound(BigInteger x, BigInteger y, int scale, MathContext mc) {
        return BigDecimal.doRound(x.multiply(y), scale, mc);
    }

    private static BigDecimal doRound128(long hi, long lo, int sign, int scale, MathContext mc) {
        int mcp = mc.precision;
        BigDecimal res = null;
        int drop = BigDecimal.precision(hi, lo) - mcp;
        if (drop > 0 && drop < LONG_TEN_POWERS_TABLE.length) {
            scale = BigDecimal.checkScaleNonZero((long)scale - (long)drop);
            res = BigDecimal.divideAndRound128(hi, lo, LONG_TEN_POWERS_TABLE[drop], sign, scale, mc.roundingMode.oldMode, scale);
        }
        if (res != null) {
            return BigDecimal.doRound(res, mc);
        }
        return null;
    }

    private static int precision(long hi, long lo) {
        if (hi == 0L) {
            if (lo >= 0L) {
                return BigDecimal.longDigitLength(lo);
            }
            return BigDecimal.unsignedLongCompareEq(lo, LONGLONG_TEN_POWERS_TABLE[0][1]) ? 20 : 19;
        }
        int r = (128 - Long.numberOfLeadingZeros(hi) + 1) * 1233 >>> 12;
        int idx = r - 19;
        return idx >= LONGLONG_TEN_POWERS_TABLE.length || BigDecimal.longLongCompareMagnitude(hi, lo, LONGLONG_TEN_POWERS_TABLE[idx][0], LONGLONG_TEN_POWERS_TABLE[idx][1]) ? r : r + 1;
    }

    private static boolean longLongCompareMagnitude(long hi0, long lo0, long hi1, long lo1) {
        if (hi0 != hi1) {
            return hi0 < hi1;
        }
        return lo0 + Long.MIN_VALUE < lo1 + Long.MIN_VALUE;
    }

    private static BigDecimal divide(long dividend, int dividendScale, long divisor, int divisorScale, int scale, int roundingMode) {
        if (BigDecimal.checkScale(dividend, (long)scale + (long)divisorScale) > dividendScale) {
            int newScale = scale + divisorScale;
            int raise = newScale - dividendScale;
            if (raise < LONG_TEN_POWERS_TABLE.length) {
                long xs = dividend;
                if ((xs = BigDecimal.longMultiplyPowerTen(xs, raise)) != Long.MIN_VALUE) {
                    return BigDecimal.divideAndRound(xs, divisor, scale, roundingMode, scale);
                }
                BigDecimal q = BigDecimal.multiplyDivideAndRound(LONG_TEN_POWERS_TABLE[raise], dividend, divisor, scale, roundingMode, scale);
                if (q != null) {
                    return q;
                }
            }
            BigInteger scaledDividend = BigDecimal.bigMultiplyPowerTen(dividend, raise);
            return BigDecimal.divideAndRound(scaledDividend, divisor, scale, roundingMode, scale);
        }
        int newScale = BigDecimal.checkScale(divisor, (long)dividendScale - (long)scale);
        int raise = newScale - divisorScale;
        if (raise < LONG_TEN_POWERS_TABLE.length) {
            long ys = divisor;
            if ((ys = BigDecimal.longMultiplyPowerTen(ys, raise)) != Long.MIN_VALUE) {
                return BigDecimal.divideAndRound(dividend, ys, scale, roundingMode, scale);
            }
        }
        BigInteger scaledDivisor = BigDecimal.bigMultiplyPowerTen(divisor, raise);
        return BigDecimal.divideAndRound(BigInteger.valueOf(dividend), scaledDivisor, scale, roundingMode, scale);
    }

    private static BigDecimal divide(BigInteger dividend, int dividendScale, long divisor, int divisorScale, int scale, int roundingMode) {
        if (BigDecimal.checkScale(dividend, (long)scale + (long)divisorScale) > dividendScale) {
            int newScale = scale + divisorScale;
            int raise = newScale - dividendScale;
            BigInteger scaledDividend = BigDecimal.bigMultiplyPowerTen(dividend, raise);
            return BigDecimal.divideAndRound(scaledDividend, divisor, scale, roundingMode, scale);
        }
        int newScale = BigDecimal.checkScale(divisor, (long)dividendScale - (long)scale);
        int raise = newScale - divisorScale;
        if (raise < LONG_TEN_POWERS_TABLE.length) {
            long ys = divisor;
            if ((ys = BigDecimal.longMultiplyPowerTen(ys, raise)) != Long.MIN_VALUE) {
                return BigDecimal.divideAndRound(dividend, ys, scale, roundingMode, scale);
            }
        }
        BigInteger scaledDivisor = BigDecimal.bigMultiplyPowerTen(divisor, raise);
        return BigDecimal.divideAndRound(dividend, scaledDivisor, scale, roundingMode, scale);
    }

    private static BigDecimal divide(long dividend, int dividendScale, BigInteger divisor, int divisorScale, int scale, int roundingMode) {
        if (BigDecimal.checkScale(dividend, (long)scale + (long)divisorScale) > dividendScale) {
            int newScale = scale + divisorScale;
            int raise = newScale - dividendScale;
            BigInteger scaledDividend = BigDecimal.bigMultiplyPowerTen(dividend, raise);
            return BigDecimal.divideAndRound(scaledDividend, divisor, scale, roundingMode, scale);
        }
        int newScale = BigDecimal.checkScale(divisor, (long)dividendScale - (long)scale);
        int raise = newScale - divisorScale;
        BigInteger scaledDivisor = BigDecimal.bigMultiplyPowerTen(divisor, raise);
        return BigDecimal.divideAndRound(BigInteger.valueOf(dividend), scaledDivisor, scale, roundingMode, scale);
    }

    private static BigDecimal divide(BigInteger dividend, int dividendScale, BigInteger divisor, int divisorScale, int scale, int roundingMode) {
        if (BigDecimal.checkScale(dividend, (long)scale + (long)divisorScale) > dividendScale) {
            int newScale = scale + divisorScale;
            int raise = newScale - dividendScale;
            BigInteger scaledDividend = BigDecimal.bigMultiplyPowerTen(dividend, raise);
            return BigDecimal.divideAndRound(scaledDividend, divisor, scale, roundingMode, scale);
        }
        int newScale = BigDecimal.checkScale(divisor, (long)dividendScale - (long)scale);
        int raise = newScale - divisorScale;
        BigInteger scaledDivisor = BigDecimal.bigMultiplyPowerTen(divisor, raise);
        return BigDecimal.divideAndRound(dividend, scaledDivisor, scale, roundingMode, scale);
    }

    private static class LongOverflow {
        private static final BigInteger LONGMIN = BigInteger.valueOf(Long.MIN_VALUE);
        private static final BigInteger LONGMAX = BigInteger.valueOf(Long.MAX_VALUE);

        private LongOverflow() {
        }

        public static void check(BigDecimal num) {
            BigInteger intVal = num.inflated();
            if (intVal.compareTo(LONGMIN) < 0 || intVal.compareTo(LONGMAX) > 0) {
                throw new ArithmeticException("Overflow");
            }
        }
    }

    static class StringBuilderHelper {
        final StringBuilder sb = new StringBuilder(32);
        final char[] cmpCharArray = new char[19];
        static final char[] DIGIT_TENS = new char[]{'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9'};
        static final char[] DIGIT_ONES = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

        StringBuilderHelper() {
        }

        StringBuilder getStringBuilder() {
            this.sb.setLength(0);
            return this.sb;
        }

        char[] getCompactCharArray() {
            return this.cmpCharArray;
        }

        int putIntCompact(long intCompact) {
            int r;
            assert (intCompact >= 0L);
            int charPos = this.cmpCharArray.length;
            while (intCompact > Integer.MAX_VALUE) {
                long q = intCompact / 100L;
                r = (int)(intCompact - q * 100L);
                intCompact = q;
                this.cmpCharArray[--charPos] = DIGIT_ONES[r];
                this.cmpCharArray[--charPos] = DIGIT_TENS[r];
            }
            int i2 = (int)intCompact;
            while (i2 >= 100) {
                int q2 = i2 / 100;
                r = i2 - q2 * 100;
                i2 = q2;
                this.cmpCharArray[--charPos] = DIGIT_ONES[r];
                this.cmpCharArray[--charPos] = DIGIT_TENS[r];
            }
            this.cmpCharArray[--charPos] = DIGIT_ONES[i2];
            if (i2 >= 10) {
                this.cmpCharArray[--charPos] = DIGIT_TENS[i2];
            }
            return charPos;
        }
    }

    private static class UnsafeHolder {
        private static final Unsafe unsafe = Unsafe.getUnsafe();
        private static final long intCompactOffset = unsafe.objectFieldOffset(BigDecimal.class, "intCompact");
        private static final long intValOffset = unsafe.objectFieldOffset(BigDecimal.class, "intVal");
        private static final long scaleOffset = unsafe.objectFieldOffset(BigDecimal.class, "scale");

        private UnsafeHolder() {
        }

        static void setIntValAndScale(BigDecimal bd, BigInteger intVal, int scale) {
            unsafe.putReference(bd, intValOffset, intVal);
            unsafe.putInt(bd, scaleOffset, scale);
            unsafe.putLong(bd, intCompactOffset, BigDecimal.compactValFor(intVal));
        }

        static void setIntValVolatile(BigDecimal bd, BigInteger val) {
            unsafe.putReferenceVolatile(bd, intValOffset, val);
        }
    }
}

