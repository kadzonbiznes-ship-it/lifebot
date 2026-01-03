/*
 * Decompiled with CFR 0.152.
 */
package java.text;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIteratorFieldDelegate;
import java.text.DecimalFormatSymbols;
import java.text.DigitList;
import java.text.DontCareFieldPosition;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.spi.NumberFormatProvider;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.ResourceBundleBasedAdapter;

public class DecimalFormat
extends NumberFormat {
    private transient BigInteger bigIntegerMultiplier;
    private transient BigDecimal bigDecimalMultiplier;
    private static final int STATUS_INFINITE = 0;
    private static final int STATUS_POSITIVE = 1;
    private static final int STATUS_LENGTH = 2;
    private transient DigitList digitList = new DigitList();
    private String positivePrefix = "";
    private String positiveSuffix = "";
    private String negativePrefix = "-";
    private String negativeSuffix = "";
    private String posPrefixPattern;
    private String posSuffixPattern;
    private String negPrefixPattern;
    private String negSuffixPattern;
    private int multiplier = 1;
    private byte groupingSize = (byte)3;
    private boolean decimalSeparatorAlwaysShown = false;
    private boolean parseBigDecimal = false;
    private transient boolean isCurrencyFormat = false;
    private DecimalFormatSymbols symbols = null;
    private boolean useExponentialNotation;
    private transient FieldPosition[] positivePrefixFieldPositions;
    private transient FieldPosition[] positiveSuffixFieldPositions;
    private transient FieldPosition[] negativePrefixFieldPositions;
    private transient FieldPosition[] negativeSuffixFieldPositions;
    private byte minExponentDigits;
    private int maximumIntegerDigits = super.getMaximumIntegerDigits();
    private int minimumIntegerDigits = super.getMinimumIntegerDigits();
    private int maximumFractionDigits = super.getMaximumFractionDigits();
    private int minimumFractionDigits = super.getMinimumFractionDigits();
    private RoundingMode roundingMode = RoundingMode.HALF_EVEN;
    private transient boolean isFastPath = false;
    private transient boolean fastPathCheckNeeded = true;
    private transient FastPathData fastPathData;
    static final int currentSerialVersion = 4;
    private int serialVersionOnStream = 4;
    private static final double MAX_INT_AS_DOUBLE = 2.147483647E9;
    private static final char PATTERN_ZERO_DIGIT = '0';
    private static final char PATTERN_GROUPING_SEPARATOR = ',';
    private static final char PATTERN_DECIMAL_SEPARATOR = '.';
    private static final char PATTERN_PER_MILLE = '\u2030';
    private static final char PATTERN_PERCENT = '%';
    private static final char PATTERN_DIGIT = '#';
    private static final char PATTERN_SEPARATOR = ';';
    private static final String PATTERN_EXPONENT = "E";
    private static final char PATTERN_MINUS = '-';
    private static final char CURRENCY_SIGN = '\u00a4';
    private static final char QUOTE = '\'';
    private static FieldPosition[] EmptyFieldPositionArray = new FieldPosition[0];
    static final int DOUBLE_INTEGER_DIGITS = 309;
    static final int DOUBLE_FRACTION_DIGITS = 340;
    static final int MAXIMUM_INTEGER_DIGITS = Integer.MAX_VALUE;
    static final int MAXIMUM_FRACTION_DIGITS = Integer.MAX_VALUE;
    static final long serialVersionUID = 864413376551465018L;

    public DecimalFormat() {
        Locale def = Locale.getDefault(Locale.Category.FORMAT);
        LocaleProviderAdapter adapter = LocaleProviderAdapter.getAdapter(NumberFormatProvider.class, def);
        if (!(adapter instanceof ResourceBundleBasedAdapter)) {
            adapter = LocaleProviderAdapter.getResourceBundleBased();
        }
        String[] all = adapter.getLocaleResources(def).getNumberPatterns();
        this.symbols = DecimalFormatSymbols.getInstance(def);
        this.applyPattern(all[0], false);
    }

    public DecimalFormat(String pattern) {
        this.symbols = DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT));
        this.applyPattern(pattern, false);
    }

    public DecimalFormat(String pattern, DecimalFormatSymbols symbols) {
        this.symbols = (DecimalFormatSymbols)symbols.clone();
        this.applyPattern(pattern, false);
    }

    @Override
    public final StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos) {
        if (number instanceof Long || number instanceof Integer || number instanceof Short || number instanceof Byte || number instanceof AtomicInteger || number instanceof AtomicLong || number instanceof BigInteger && ((BigInteger)number).bitLength() < 64) {
            return this.format(((Number)number).longValue(), toAppendTo, pos);
        }
        if (number instanceof BigDecimal) {
            return this.format((BigDecimal)number, toAppendTo, pos);
        }
        if (number instanceof BigInteger) {
            return this.format((BigInteger)number, toAppendTo, pos);
        }
        if (number instanceof Number) {
            return this.format(((Number)number).doubleValue(), toAppendTo, pos);
        }
        throw new IllegalArgumentException("Cannot format given Object as a Number");
    }

    @Override
    public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
        String tempResult;
        boolean tryFastPath = false;
        if (fieldPosition == DontCareFieldPosition.INSTANCE) {
            tryFastPath = true;
        } else {
            fieldPosition.setBeginIndex(0);
            fieldPosition.setEndIndex(0);
        }
        if (tryFastPath && (tempResult = this.fastFormat(number)) != null) {
            result.append(tempResult);
            return result;
        }
        return this.format(number, result, fieldPosition.getFieldDelegate());
    }

    StringBuffer format(double number, StringBuffer result, Format.FieldDelegate delegate) {
        boolean nanOrInfinity = this.handleNaN(number, result, delegate);
        if (nanOrInfinity) {
            return result;
        }
        boolean isNegative = (number < 0.0 || number == 0.0 && 1.0 / number < 0.0) ^ this.multiplier < 0;
        if (this.multiplier != 1) {
            number *= (double)this.multiplier;
        }
        if (nanOrInfinity = this.handleInfinity(number, result, delegate, isNegative)) {
            return result;
        }
        if (isNegative) {
            number = -number;
        }
        assert (number >= 0.0 && !Double.isInfinite(number));
        return this.doubleSubformat(number, result, delegate, isNegative);
    }

    boolean handleNaN(double number, StringBuffer result, Format.FieldDelegate delegate) {
        if (Double.isNaN(number) || Double.isInfinite(number) && this.multiplier == 0) {
            int iFieldStart = result.length();
            result.append(this.symbols.getNaN());
            delegate.formatted(0, NumberFormat.Field.INTEGER, NumberFormat.Field.INTEGER, iFieldStart, result.length(), result);
            return true;
        }
        return false;
    }

    boolean handleInfinity(double number, StringBuffer result, Format.FieldDelegate delegate, boolean isNegative) {
        if (Double.isInfinite(number)) {
            if (isNegative) {
                this.append(result, this.negativePrefix, delegate, this.getNegativePrefixFieldPositions(), NumberFormat.Field.SIGN);
            } else {
                this.append(result, this.positivePrefix, delegate, this.getPositivePrefixFieldPositions(), NumberFormat.Field.SIGN);
            }
            int iFieldStart = result.length();
            result.append(this.symbols.getInfinity());
            delegate.formatted(0, NumberFormat.Field.INTEGER, NumberFormat.Field.INTEGER, iFieldStart, result.length(), result);
            if (isNegative) {
                this.append(result, this.negativeSuffix, delegate, this.getNegativeSuffixFieldPositions(), NumberFormat.Field.SIGN);
            } else {
                this.append(result, this.positiveSuffix, delegate, this.getPositiveSuffixFieldPositions(), NumberFormat.Field.SIGN);
            }
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    StringBuffer doubleSubformat(double number, StringBuffer result, Format.FieldDelegate delegate, boolean isNegative) {
        DigitList digitList = this.digitList;
        synchronized (digitList) {
            int maxIntDigits = super.getMaximumIntegerDigits();
            int minIntDigits = super.getMinimumIntegerDigits();
            int maxFraDigits = super.getMaximumFractionDigits();
            int minFraDigits = super.getMinimumFractionDigits();
            this.digitList.set(isNegative, number, this.useExponentialNotation ? maxIntDigits + maxFraDigits : maxFraDigits, !this.useExponentialNotation);
            return this.subformat(result, delegate, isNegative, false, maxIntDigits, minIntDigits, maxFraDigits, minFraDigits);
        }
    }

    @Override
    public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
        fieldPosition.setBeginIndex(0);
        fieldPosition.setEndIndex(0);
        return this.format(number, result, fieldPosition.getFieldDelegate());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    StringBuffer format(long number, StringBuffer result, Format.FieldDelegate delegate) {
        boolean isNegative;
        boolean bl = isNegative = number < 0L;
        if (isNegative) {
            number = -number;
        }
        boolean useBigInteger = false;
        if (number < 0L) {
            if (this.multiplier != 0) {
                useBigInteger = true;
            }
        } else if (this.multiplier != 1 && this.multiplier != 0) {
            long cutoff = Long.MAX_VALUE / (long)this.multiplier;
            if (cutoff < 0L) {
                cutoff = -cutoff;
            }
            boolean bl2 = useBigInteger = number > cutoff;
        }
        if (useBigInteger) {
            if (isNegative) {
                number = -number;
            }
            BigInteger bigIntegerValue = BigInteger.valueOf(number);
            return this.format(bigIntegerValue, result, delegate, true);
        }
        if ((number *= (long)this.multiplier) == 0L) {
            isNegative = false;
        } else if (this.multiplier < 0) {
            number = -number;
            isNegative = !isNegative;
        }
        DigitList digitList = this.digitList;
        synchronized (digitList) {
            int maxIntDigits = super.getMaximumIntegerDigits();
            int minIntDigits = super.getMinimumIntegerDigits();
            int maxFraDigits = super.getMaximumFractionDigits();
            int minFraDigits = super.getMinimumFractionDigits();
            this.digitList.set(isNegative, number, this.useExponentialNotation ? maxIntDigits + maxFraDigits : 0);
            return this.subformat(result, delegate, isNegative, true, maxIntDigits, minIntDigits, maxFraDigits, minFraDigits);
        }
    }

    private StringBuffer format(BigDecimal number, StringBuffer result, FieldPosition fieldPosition) {
        fieldPosition.setBeginIndex(0);
        fieldPosition.setEndIndex(0);
        return this.format(number, result, fieldPosition.getFieldDelegate());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    StringBuffer format(BigDecimal number, StringBuffer result, Format.FieldDelegate delegate) {
        boolean isNegative;
        if (this.multiplier != 1) {
            number = number.multiply(this.getBigDecimalMultiplier());
        }
        boolean bl = isNegative = number.signum() == -1;
        if (isNegative) {
            number = number.negate();
        }
        DigitList digitList = this.digitList;
        synchronized (digitList) {
            int maxIntDigits = this.getMaximumIntegerDigits();
            int minIntDigits = this.getMinimumIntegerDigits();
            int maxFraDigits = this.getMaximumFractionDigits();
            int minFraDigits = this.getMinimumFractionDigits();
            int maximumDigits = maxIntDigits + maxFraDigits;
            this.digitList.set(isNegative, number, this.useExponentialNotation ? (maximumDigits < 0 ? Integer.MAX_VALUE : maximumDigits) : maxFraDigits, !this.useExponentialNotation);
            return this.subformat(result, delegate, isNegative, false, maxIntDigits, minIntDigits, maxFraDigits, minFraDigits);
        }
    }

    private StringBuffer format(BigInteger number, StringBuffer result, FieldPosition fieldPosition) {
        fieldPosition.setBeginIndex(0);
        fieldPosition.setEndIndex(0);
        return this.format(number, result, fieldPosition.getFieldDelegate(), false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    StringBuffer format(BigInteger number, StringBuffer result, Format.FieldDelegate delegate, boolean formatLong) {
        boolean isNegative;
        if (this.multiplier != 1) {
            number = number.multiply(this.getBigIntegerMultiplier());
        }
        boolean bl = isNegative = number.signum() == -1;
        if (isNegative) {
            number = number.negate();
        }
        DigitList digitList = this.digitList;
        synchronized (digitList) {
            int maximumDigits;
            int minFraDigits;
            int maxFraDigits;
            int minIntDigits;
            int maxIntDigits;
            if (formatLong) {
                maxIntDigits = super.getMaximumIntegerDigits();
                minIntDigits = super.getMinimumIntegerDigits();
                maxFraDigits = super.getMaximumFractionDigits();
                minFraDigits = super.getMinimumFractionDigits();
                maximumDigits = maxIntDigits + maxFraDigits;
            } else {
                maxIntDigits = this.getMaximumIntegerDigits();
                minIntDigits = this.getMinimumIntegerDigits();
                maxFraDigits = this.getMaximumFractionDigits();
                minFraDigits = this.getMinimumFractionDigits();
                maximumDigits = maxIntDigits + maxFraDigits;
                if (maximumDigits < 0) {
                    maximumDigits = Integer.MAX_VALUE;
                }
            }
            this.digitList.set(isNegative, number, this.useExponentialNotation ? maximumDigits : 0);
            return this.subformat(result, delegate, isNegative, true, maxIntDigits, minIntDigits, maxFraDigits, minFraDigits);
        }
    }

    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        CharacterIteratorFieldDelegate delegate = new CharacterIteratorFieldDelegate();
        StringBuffer sb = new StringBuffer();
        if (obj instanceof Double || obj instanceof Float) {
            this.format(((Number)obj).doubleValue(), sb, (Format.FieldDelegate)delegate);
        } else if (obj instanceof Long || obj instanceof Integer || obj instanceof Short || obj instanceof Byte || obj instanceof AtomicInteger || obj instanceof AtomicLong) {
            this.format(((Number)obj).longValue(), sb, (Format.FieldDelegate)delegate);
        } else if (obj instanceof BigDecimal) {
            this.format((BigDecimal)obj, sb, (Format.FieldDelegate)delegate);
        } else if (obj instanceof BigInteger) {
            this.format((BigInteger)obj, sb, delegate, false);
        } else {
            if (obj == null) {
                throw new NullPointerException("formatToCharacterIterator must be passed non-null object");
            }
            throw new IllegalArgumentException("Cannot format given Object as a Number");
        }
        return delegate.getIterator(sb.toString());
    }

    private boolean checkAndSetFastPathStatus() {
        boolean fastPathWasOn = this.isFastPath;
        if (this.roundingMode == RoundingMode.HALF_EVEN && this.isGroupingUsed() && this.groupingSize == 3 && this.multiplier == 1 && !this.decimalSeparatorAlwaysShown && !this.useExponentialNotation) {
            boolean bl = this.isFastPath = this.minimumIntegerDigits == 1 && this.maximumIntegerDigits >= 10;
            if (this.isFastPath) {
                if (this.isCurrencyFormat) {
                    if (this.minimumFractionDigits != 2 || this.maximumFractionDigits != 2) {
                        this.isFastPath = false;
                    }
                } else if (this.minimumFractionDigits != 0 || this.maximumFractionDigits != 3) {
                    this.isFastPath = false;
                }
            }
        } else {
            this.isFastPath = false;
        }
        this.resetFastPathData(fastPathWasOn);
        this.fastPathCheckNeeded = false;
        return true;
    }

    private void resetFastPathData(boolean fastPathWasOn) {
        if (this.isFastPath) {
            if (this.fastPathData == null) {
                this.fastPathData = new FastPathData();
            }
            this.fastPathData.zeroDelta = this.symbols.getZeroDigit() - 48;
            this.fastPathData.groupingChar = this.isCurrencyFormat ? this.symbols.getMonetaryGroupingSeparator() : this.symbols.getGroupingSeparator();
            this.fastPathData.fractionalMaxIntBound = this.isCurrencyFormat ? 99 : 999;
            this.fastPathData.fractionalScaleFactor = this.isCurrencyFormat ? 100.0 : 1000.0;
            this.fastPathData.positiveAffixesRequired = !this.positivePrefix.isEmpty() || !this.positiveSuffix.isEmpty();
            this.fastPathData.negativeAffixesRequired = !this.negativePrefix.isEmpty() || !this.negativeSuffix.isEmpty();
            int maxNbIntegralDigits = 10;
            int maxNbGroups = 3;
            int containerSize = Math.max(this.positivePrefix.length(), this.negativePrefix.length()) + maxNbIntegralDigits + maxNbGroups + 1 + this.maximumFractionDigits + Math.max(this.positiveSuffix.length(), this.negativeSuffix.length());
            this.fastPathData.fastPathContainer = new char[containerSize];
            this.fastPathData.charsPositiveSuffix = this.positiveSuffix.toCharArray();
            this.fastPathData.charsNegativeSuffix = this.negativeSuffix.toCharArray();
            this.fastPathData.charsPositivePrefix = this.positivePrefix.toCharArray();
            this.fastPathData.charsNegativePrefix = this.negativePrefix.toCharArray();
            int longestPrefixLength = Math.max(this.positivePrefix.length(), this.negativePrefix.length());
            int decimalPointIndex = maxNbIntegralDigits + maxNbGroups + longestPrefixLength;
            this.fastPathData.integralLastIndex = decimalPointIndex - 1;
            this.fastPathData.fractionalFirstIndex = decimalPointIndex + 1;
            this.fastPathData.fastPathContainer[decimalPointIndex] = this.isCurrencyFormat ? this.symbols.getMonetaryDecimalSeparator() : this.symbols.getDecimalSeparator();
        } else if (fastPathWasOn) {
            this.fastPathData.fastPathContainer = null;
            this.fastPathData.charsPositiveSuffix = null;
            this.fastPathData.charsNegativeSuffix = null;
            this.fastPathData.charsPositivePrefix = null;
            this.fastPathData.charsNegativePrefix = null;
        }
    }

    private boolean exactRoundUp(double fractionalPart, int scaledFractionalPartAsInt) {
        double approxMin;
        double approxMedium;
        double approxMax;
        double fastTwoSumApproximation = 0.0;
        double fastTwoSumRoundOff = 0.0;
        double bVirtual = 0.0;
        if (this.isCurrencyFormat) {
            approxMax = fractionalPart * 128.0;
            approxMedium = -(fractionalPart * 32.0);
            approxMin = fractionalPart * 4.0;
        } else {
            approxMax = fractionalPart * 1024.0;
            approxMedium = -(fractionalPart * 16.0);
            approxMin = -(fractionalPart * 8.0);
        }
        assert (-approxMedium >= Math.abs(approxMin));
        fastTwoSumApproximation = approxMedium + approxMin;
        bVirtual = fastTwoSumApproximation - approxMedium;
        fastTwoSumRoundOff = approxMin - bVirtual;
        double approxS1 = fastTwoSumApproximation;
        double roundoffS1 = fastTwoSumRoundOff;
        assert (approxMax >= Math.abs(approxS1));
        fastTwoSumApproximation = approxMax + approxS1;
        bVirtual = fastTwoSumApproximation - approxMax;
        double roundoff1000 = fastTwoSumRoundOff = approxS1 - bVirtual;
        double approx1000 = fastTwoSumApproximation;
        double roundoffTotal = roundoffS1 + roundoff1000;
        assert (approx1000 >= Math.abs(roundoffTotal));
        fastTwoSumApproximation = approx1000 + roundoffTotal;
        bVirtual = fastTwoSumApproximation - approx1000;
        double scaledFractionalRoundoff = roundoffTotal - bVirtual;
        if (scaledFractionalRoundoff > 0.0) {
            return true;
        }
        if (scaledFractionalRoundoff < 0.0) {
            return false;
        }
        return (scaledFractionalPartAsInt & 1) != 0;
    }

    private void collectIntegralDigits(int number, char[] digitsBuffer, int backwardIndex) {
        int index = backwardIndex;
        while (number > 999) {
            int q = number / 1000;
            int r = number - (q << 10) + (q << 4) + (q << 3);
            number = q;
            digitsBuffer[index--] = DigitArrays.DigitOnes1000[r];
            digitsBuffer[index--] = DigitArrays.DigitTens1000[r];
            digitsBuffer[index--] = DigitArrays.DigitHundreds1000[r];
            digitsBuffer[index--] = this.fastPathData.groupingChar;
        }
        digitsBuffer[index] = DigitArrays.DigitOnes1000[number];
        if (number > 9) {
            digitsBuffer[--index] = DigitArrays.DigitTens1000[number];
            if (number > 99) {
                digitsBuffer[--index] = DigitArrays.DigitHundreds1000[number];
            }
        }
        this.fastPathData.firstUsedIndex = index;
    }

    private void collectFractionalDigits(int number, char[] digitsBuffer, int startIndex) {
        int index = startIndex;
        char digitOnes = DigitArrays.DigitOnes1000[number];
        char digitTens = DigitArrays.DigitTens1000[number];
        if (this.isCurrencyFormat) {
            digitsBuffer[index++] = digitTens;
            digitsBuffer[index++] = digitOnes;
        } else if (number != 0) {
            digitsBuffer[index++] = DigitArrays.DigitHundreds1000[number];
            if (digitOnes != '0') {
                digitsBuffer[index++] = digitTens;
                digitsBuffer[index++] = digitOnes;
            } else if (digitTens != '0') {
                digitsBuffer[index++] = digitTens;
            }
        } else {
            --index;
        }
        this.fastPathData.lastFreeIndex = index;
    }

    private void addAffixes(char[] container, char[] prefix, char[] suffix) {
        int pl = prefix.length;
        int sl = suffix.length;
        if (pl != 0) {
            this.prependPrefix(prefix, pl, container);
        }
        if (sl != 0) {
            this.appendSuffix(suffix, sl, container);
        }
    }

    private void prependPrefix(char[] prefix, int len, char[] container) {
        this.fastPathData.firstUsedIndex -= len;
        int startIndex = this.fastPathData.firstUsedIndex;
        if (len == 1) {
            container[startIndex] = prefix[0];
        } else if (len <= 4) {
            int dstLower = startIndex;
            int dstUpper = dstLower + len - 1;
            int srcUpper = len - 1;
            container[dstLower] = prefix[0];
            container[dstUpper] = prefix[srcUpper];
            if (len > 2) {
                container[++dstLower] = prefix[1];
            }
            if (len == 4) {
                container[--dstUpper] = prefix[2];
            }
        } else {
            System.arraycopy(prefix, 0, container, startIndex, len);
        }
    }

    private void appendSuffix(char[] suffix, int len, char[] container) {
        int startIndex = this.fastPathData.lastFreeIndex;
        if (len == 1) {
            container[startIndex] = suffix[0];
        } else if (len <= 4) {
            int dstLower = startIndex;
            int dstUpper = dstLower + len - 1;
            int srcUpper = len - 1;
            container[dstLower] = suffix[0];
            container[dstUpper] = suffix[srcUpper];
            if (len > 2) {
                container[++dstLower] = suffix[1];
            }
            if (len == 4) {
                container[--dstUpper] = suffix[2];
            }
        } else {
            System.arraycopy(suffix, 0, container, startIndex, len);
        }
        this.fastPathData.lastFreeIndex += len;
    }

    private void localizeDigits(char[] digitsBuffer) {
        int digitsCounter = this.fastPathData.lastFreeIndex - this.fastPathData.fractionalFirstIndex;
        if (digitsCounter < 0) {
            digitsCounter = this.groupingSize;
        }
        for (int cursor = this.fastPathData.lastFreeIndex - 1; cursor >= this.fastPathData.firstUsedIndex; --cursor) {
            if (digitsCounter != 0) {
                int n = cursor;
                digitsBuffer[n] = (char)(digitsBuffer[n] + (char)this.fastPathData.zeroDelta);
                --digitsCounter;
                continue;
            }
            digitsCounter = this.groupingSize;
        }
    }

    private void fastDoubleFormat(double d, boolean negative) {
        char[] container = this.fastPathData.fastPathContainer;
        int integralPartAsInt = (int)d;
        double exactFractionalPart = d - (double)integralPartAsInt;
        double scaledFractional = exactFractionalPart * this.fastPathData.fractionalScaleFactor;
        int fractionalPartAsInt = (int)scaledFractional;
        boolean roundItUp = false;
        if (scaledFractional >= 0.5 && (roundItUp = (scaledFractional -= (double)fractionalPartAsInt) == 0.5 ? this.exactRoundUp(exactFractionalPart, fractionalPartAsInt) : true)) {
            if (fractionalPartAsInt < this.fastPathData.fractionalMaxIntBound) {
                ++fractionalPartAsInt;
            } else {
                fractionalPartAsInt = 0;
                ++integralPartAsInt;
            }
        }
        this.collectFractionalDigits(fractionalPartAsInt, container, this.fastPathData.fractionalFirstIndex);
        this.collectIntegralDigits(integralPartAsInt, container, this.fastPathData.integralLastIndex);
        if (this.fastPathData.zeroDelta != 0) {
            this.localizeDigits(container);
        }
        if (negative) {
            if (this.fastPathData.negativeAffixesRequired) {
                this.addAffixes(container, this.fastPathData.charsNegativePrefix, this.fastPathData.charsNegativeSuffix);
            }
        } else if (this.fastPathData.positiveAffixesRequired) {
            this.addAffixes(container, this.fastPathData.charsPositivePrefix, this.fastPathData.charsPositiveSuffix);
        }
    }

    @Override
    String fastFormat(double d) {
        boolean isDataSet = false;
        if (this.fastPathCheckNeeded) {
            isDataSet = this.checkAndSetFastPathStatus();
        }
        if (!this.isFastPath) {
            return null;
        }
        if (!Double.isFinite(d)) {
            return null;
        }
        boolean negative = false;
        if (d < 0.0) {
            negative = true;
            d = -d;
        } else if (d == 0.0) {
            negative = Math.copySign(1.0, d) == -1.0;
            d = 0.0;
        }
        if (d > 2.147483647E9) {
            return null;
        }
        if (!isDataSet) {
            this.resetFastPathData(this.isFastPath);
        }
        this.fastDoubleFormat(d, negative);
        return new String(this.fastPathData.fastPathContainer, this.fastPathData.firstUsedIndex, this.fastPathData.lastFreeIndex - this.fastPathData.firstUsedIndex);
    }

    void setDigitList(Number number, boolean isNegative, int maxDigits) {
        if (number instanceof Double) {
            this.digitList.set(isNegative, (Double)number, maxDigits, true);
        } else if (number instanceof BigDecimal) {
            this.digitList.set(isNegative, (BigDecimal)number, maxDigits, true);
        } else if (number instanceof Long) {
            this.digitList.set(isNegative, (Long)number, maxDigits);
        } else if (number instanceof BigInteger) {
            this.digitList.set(isNegative, (BigInteger)number, maxDigits);
        }
    }

    private StringBuffer subformat(StringBuffer result, Format.FieldDelegate delegate, boolean isNegative, boolean isInteger, int maxIntDigits, int minIntDigits, int maxFraDigits, int minFraDigits) {
        if (isNegative) {
            this.append(result, this.negativePrefix, delegate, this.getNegativePrefixFieldPositions(), NumberFormat.Field.SIGN);
        } else {
            this.append(result, this.positivePrefix, delegate, this.getPositivePrefixFieldPositions(), NumberFormat.Field.SIGN);
        }
        this.subformatNumber(result, delegate, isNegative, isInteger, maxIntDigits, minIntDigits, maxFraDigits, minFraDigits);
        if (isNegative) {
            this.append(result, this.negativeSuffix, delegate, this.getNegativeSuffixFieldPositions(), NumberFormat.Field.SIGN);
        } else {
            this.append(result, this.positiveSuffix, delegate, this.getPositiveSuffixFieldPositions(), NumberFormat.Field.SIGN);
        }
        return result;
    }

    void subformatNumber(StringBuffer result, Format.FieldDelegate delegate, boolean isNegative, boolean isInteger, int maxIntDigits, int minIntDigits, int maxFraDigits, int minFraDigits) {
        char decimal;
        char grouping = this.isCurrencyFormat ? this.symbols.getMonetaryGroupingSeparator() : this.symbols.getGroupingSeparator();
        char zero = this.symbols.getZeroDigit();
        int zeroDelta = zero - 48;
        char c = decimal = this.isCurrencyFormat ? this.symbols.getMonetaryDecimalSeparator() : this.symbols.getDecimalSeparator();
        if (this.digitList.isZero()) {
            this.digitList.decimalAt = 0;
        }
        if (this.useExponentialNotation) {
            int i;
            boolean negativeExponent;
            int totalDigits;
            int integerDigits;
            int iFieldStart = result.length();
            int iFieldEnd = -1;
            int fFieldStart = -1;
            int exponent = this.digitList.decimalAt;
            int repeat = maxIntDigits;
            int minimumIntegerDigits = minIntDigits;
            if (repeat > 1 && repeat > minIntDigits) {
                exponent = exponent >= 1 ? (exponent - 1) / repeat * repeat : (exponent - repeat) / repeat * repeat;
                minimumIntegerDigits = 1;
            } else {
                exponent -= minimumIntegerDigits;
            }
            int minimumDigits = minIntDigits + minFraDigits;
            if (minimumDigits < 0) {
                minimumDigits = Integer.MAX_VALUE;
            }
            int n = integerDigits = this.digitList.isZero() ? minimumIntegerDigits : this.digitList.decimalAt - exponent;
            if (minimumDigits < integerDigits) {
                minimumDigits = integerDigits;
            }
            if (minimumDigits > (totalDigits = this.digitList.count)) {
                totalDigits = minimumDigits;
            }
            boolean addedDecimalSeparator = false;
            for (int i2 = 0; i2 < totalDigits; ++i2) {
                if (i2 == integerDigits) {
                    iFieldEnd = result.length();
                    result.append(decimal);
                    addedDecimalSeparator = true;
                    fFieldStart = result.length();
                }
                result.append(i2 < this.digitList.count ? (char)(this.digitList.digits[i2] + zeroDelta) : zero);
            }
            if (this.decimalSeparatorAlwaysShown && totalDigits == integerDigits) {
                iFieldEnd = result.length();
                result.append(decimal);
                addedDecimalSeparator = true;
                fFieldStart = result.length();
            }
            if (iFieldEnd == -1) {
                iFieldEnd = result.length();
            }
            delegate.formatted(0, NumberFormat.Field.INTEGER, NumberFormat.Field.INTEGER, iFieldStart, iFieldEnd, result);
            if (addedDecimalSeparator) {
                delegate.formatted(NumberFormat.Field.DECIMAL_SEPARATOR, NumberFormat.Field.DECIMAL_SEPARATOR, iFieldEnd, fFieldStart, result);
            }
            if (fFieldStart == -1) {
                fFieldStart = result.length();
            }
            delegate.formatted(1, NumberFormat.Field.FRACTION, NumberFormat.Field.FRACTION, fFieldStart, result.length(), result);
            int fieldStart = result.length();
            result.append(this.symbols.getExponentSeparator());
            delegate.formatted(NumberFormat.Field.EXPONENT_SYMBOL, NumberFormat.Field.EXPONENT_SYMBOL, fieldStart, result.length(), result);
            if (this.digitList.isZero()) {
                exponent = 0;
            }
            boolean bl = negativeExponent = exponent < 0;
            if (negativeExponent) {
                exponent = -exponent;
                fieldStart = result.length();
                result.append(this.symbols.getMinusSignText());
                delegate.formatted(NumberFormat.Field.EXPONENT_SIGN, NumberFormat.Field.EXPONENT_SIGN, fieldStart, result.length(), result);
            }
            this.digitList.set(negativeExponent, exponent);
            int eFieldStart = result.length();
            for (i = this.digitList.decimalAt; i < this.minExponentDigits; ++i) {
                result.append(zero);
            }
            for (i = 0; i < this.digitList.decimalAt; ++i) {
                result.append(i < this.digitList.count ? (char)(this.digitList.digits[i] + zeroDelta) : zero);
            }
            delegate.formatted(NumberFormat.Field.EXPONENT, NumberFormat.Field.EXPONENT, eFieldStart, result.length(), result);
        } else {
            boolean fractionPresent;
            int iFieldStart = result.length();
            int count = minIntDigits;
            int digitIndex = 0;
            if (this.digitList.decimalAt > 0 && count < this.digitList.decimalAt) {
                count = this.digitList.decimalAt;
            }
            if (count > maxIntDigits) {
                count = maxIntDigits;
                digitIndex = this.digitList.decimalAt - count;
            }
            int sizeBeforeIntegerPart = result.length();
            for (int i = count - 1; i >= 0; --i) {
                if (i < this.digitList.decimalAt && digitIndex < this.digitList.count) {
                    result.append((char)(this.digitList.digits[digitIndex++] + zeroDelta));
                } else {
                    result.append(zero);
                }
                if (!this.isGroupingUsed() || i <= 0 || this.groupingSize == 0 || i % this.groupingSize != 0) continue;
                int gStart = result.length();
                result.append(grouping);
                delegate.formatted(NumberFormat.Field.GROUPING_SEPARATOR, NumberFormat.Field.GROUPING_SEPARATOR, gStart, result.length(), result);
            }
            boolean bl = fractionPresent = minFraDigits > 0 || !isInteger && digitIndex < this.digitList.count;
            if (!fractionPresent && result.length() == sizeBeforeIntegerPart) {
                result.append(zero);
            }
            delegate.formatted(0, NumberFormat.Field.INTEGER, NumberFormat.Field.INTEGER, iFieldStart, result.length(), result);
            int sStart = result.length();
            if (this.decimalSeparatorAlwaysShown || fractionPresent) {
                result.append(decimal);
            }
            if (sStart != result.length()) {
                delegate.formatted(NumberFormat.Field.DECIMAL_SEPARATOR, NumberFormat.Field.DECIMAL_SEPARATOR, sStart, result.length(), result);
            }
            int fFieldStart = result.length();
            for (int i = 0; i < maxFraDigits && (i < minFraDigits || !isInteger && digitIndex < this.digitList.count); ++i) {
                if (-1 - i > this.digitList.decimalAt - 1) {
                    result.append(zero);
                    continue;
                }
                if (!isInteger && digitIndex < this.digitList.count) {
                    result.append((char)(this.digitList.digits[digitIndex++] + zeroDelta));
                    continue;
                }
                result.append(zero);
            }
            delegate.formatted(1, NumberFormat.Field.FRACTION, NumberFormat.Field.FRACTION, fFieldStart, result.length(), result);
        }
    }

    private void append(StringBuffer result, String string, Format.FieldDelegate delegate, FieldPosition[] positions, Format.Field signAttribute) {
        int start = result.length();
        if (!string.isEmpty()) {
            result.append(string);
            for (FieldPosition fp : positions) {
                Format.Field attribute = fp.getFieldAttribute();
                if (attribute == NumberFormat.Field.SIGN) {
                    attribute = signAttribute;
                }
                delegate.formatted(attribute, attribute, start + fp.getBeginIndex(), start + fp.getEndIndex(), result);
            }
        }
    }

    @Override
    public Number parse(String text, ParsePosition pos) {
        if (text.regionMatches(pos.index, this.symbols.getNaN(), 0, this.symbols.getNaN().length())) {
            pos.index += this.symbols.getNaN().length();
            return Double.NaN;
        }
        boolean[] status = new boolean[2];
        if (!this.subparse(text, pos, this.positivePrefix, this.negativePrefix, this.digitList, false, status)) {
            return null;
        }
        if (status[0]) {
            if (status[1] == this.multiplier >= 0) {
                return Double.POSITIVE_INFINITY;
            }
            return Double.NEGATIVE_INFINITY;
        }
        if (this.multiplier == 0) {
            if (this.digitList.isZero()) {
                return Double.NaN;
            }
            if (status[1]) {
                return Double.POSITIVE_INFINITY;
            }
            return Double.NEGATIVE_INFINITY;
        }
        if (this.isParseBigDecimal()) {
            BigDecimal bigDecimalResult = this.digitList.getBigDecimal();
            if (this.multiplier != 1) {
                try {
                    bigDecimalResult = bigDecimalResult.divide(this.getBigDecimalMultiplier());
                }
                catch (ArithmeticException e) {
                    bigDecimalResult = bigDecimalResult.divide(this.getBigDecimalMultiplier(), this.roundingMode);
                }
            }
            if (!status[1]) {
                bigDecimalResult = bigDecimalResult.negate();
            }
            return bigDecimalResult;
        }
        boolean gotDouble = true;
        boolean gotLongMinimum = false;
        double doubleResult = 0.0;
        long longResult = 0L;
        if (this.digitList.fitsIntoLong(status[1], this.isParseIntegerOnly())) {
            gotDouble = false;
            longResult = this.digitList.getLong();
            if (longResult < 0L) {
                gotLongMinimum = true;
            }
        } else {
            doubleResult = this.digitList.getDouble();
        }
        if (this.multiplier != 1) {
            if (gotDouble) {
                doubleResult /= (double)this.multiplier;
            } else if (longResult % (long)this.multiplier == 0L) {
                longResult /= (long)this.multiplier;
            } else {
                doubleResult = (double)longResult / (double)this.multiplier;
                gotDouble = true;
            }
        }
        if (!status[1] && !gotLongMinimum) {
            doubleResult = -doubleResult;
            longResult = -longResult;
        }
        if (this.multiplier != 1 && gotDouble) {
            longResult = (long)doubleResult;
            gotDouble = (doubleResult != (double)longResult || doubleResult == 0.0 && 1.0 / doubleResult < 0.0) && !this.isParseIntegerOnly();
        }
        return gotDouble ? (Number)doubleResult : (Number)longResult;
    }

    private BigInteger getBigIntegerMultiplier() {
        if (this.bigIntegerMultiplier == null) {
            this.bigIntegerMultiplier = BigInteger.valueOf(this.multiplier);
        }
        return this.bigIntegerMultiplier;
    }

    private BigDecimal getBigDecimalMultiplier() {
        if (this.bigDecimalMultiplier == null) {
            this.bigDecimalMultiplier = new BigDecimal(this.multiplier);
        }
        return this.bigDecimalMultiplier;
    }

    private final boolean subparse(String text, ParsePosition parsePosition, String positivePrefix, String negativePrefix, DigitList digits, boolean isExponent, boolean[] status) {
        int position = parsePosition.index;
        int oldStart = parsePosition.index;
        boolean gotPositive = text.regionMatches(position, positivePrefix, 0, positivePrefix.length());
        boolean gotNegative = text.regionMatches(position, negativePrefix, 0, negativePrefix.length());
        if (gotPositive && gotNegative) {
            if (positivePrefix.length() > negativePrefix.length()) {
                gotNegative = false;
            } else if (positivePrefix.length() < negativePrefix.length()) {
                gotPositive = false;
            }
        }
        if (gotPositive) {
            position += positivePrefix.length();
        } else if (gotNegative) {
            position += negativePrefix.length();
        } else {
            parsePosition.errorIndex = position;
            return false;
        }
        position = this.subparseNumber(text, position, digits, true, isExponent, status);
        if (position == -1) {
            parsePosition.index = oldStart;
            parsePosition.errorIndex = oldStart;
            return false;
        }
        if (!isExponent) {
            if (gotPositive) {
                gotPositive = text.regionMatches(position, this.positiveSuffix, 0, this.positiveSuffix.length());
            }
            if (gotNegative) {
                gotNegative = text.regionMatches(position, this.negativeSuffix, 0, this.negativeSuffix.length());
            }
            if (gotPositive && gotNegative) {
                if (this.positiveSuffix.length() > this.negativeSuffix.length()) {
                    gotNegative = false;
                } else if (this.positiveSuffix.length() < this.negativeSuffix.length()) {
                    gotPositive = false;
                }
            }
            if (gotPositive == gotNegative) {
                parsePosition.errorIndex = position;
                return false;
            }
            parsePosition.index = position + (gotPositive ? this.positiveSuffix.length() : this.negativeSuffix.length());
        } else {
            parsePosition.index = position;
        }
        status[1] = gotPositive;
        if (parsePosition.index == oldStart) {
            parsePosition.errorIndex = position;
            return false;
        }
        return true;
    }

    int subparseNumber(String text, int position, DigitList digits, boolean checkExponent, boolean isExponent, boolean[] status) {
        status[0] = false;
        if (!isExponent && text.regionMatches(position, this.symbols.getInfinity(), 0, this.symbols.getInfinity().length())) {
            position += this.symbols.getInfinity().length();
            status[0] = true;
        } else {
            digits.count = 0;
            digits.decimalAt = 0;
            char zero = this.symbols.getZeroDigit();
            char decimal = this.isCurrencyFormat ? this.symbols.getMonetaryDecimalSeparator() : this.symbols.getDecimalSeparator();
            char grouping = this.isCurrencyFormat ? this.symbols.getMonetaryGroupingSeparator() : this.symbols.getGroupingSeparator();
            String exponentString = this.symbols.getExponentSeparator();
            boolean sawDecimal = false;
            boolean sawExponent = false;
            boolean sawDigit = false;
            int exponent = 0;
            int digitCount = 0;
            int backup = -1;
            while (position < text.length()) {
                char ch = text.charAt(position);
                int digit = ch - zero;
                if (digit < 0 || digit > 9) {
                    digit = Character.digit(ch, 10);
                }
                if (digit == 0) {
                    backup = -1;
                    sawDigit = true;
                    if (digits.count == 0) {
                        if (sawDecimal) {
                            --digits.decimalAt;
                        }
                    } else {
                        ++digitCount;
                        digits.append((char)(digit + 48));
                    }
                } else if (digit > 0 && digit <= 9) {
                    sawDigit = true;
                    ++digitCount;
                    digits.append((char)(digit + 48));
                    backup = -1;
                } else if (!isExponent && ch == decimal) {
                    if (this.isParseIntegerOnly() || sawDecimal) break;
                    digits.decimalAt = digitCount;
                    sawDecimal = true;
                } else if (!isExponent && ch == grouping && this.isGroupingUsed()) {
                    if (sawDecimal) break;
                    backup = position;
                } else {
                    if (!checkExponent || isExponent || !text.regionMatches(position, exponentString, 0, exponentString.length()) || sawExponent) break;
                    ParsePosition pos = new ParsePosition(position + exponentString.length());
                    boolean[] stat = new boolean[2];
                    DigitList exponentDigits = new DigitList();
                    if (!this.subparse(text, pos, "", this.symbols.getMinusSignText(), exponentDigits, true, stat) || !exponentDigits.fitsIntoLong(stat[1], true)) break;
                    position = pos.index;
                    exponent = (int)exponentDigits.getLong();
                    if (!stat[1]) {
                        exponent = -exponent;
                    }
                    sawExponent = true;
                    break;
                }
                ++position;
            }
            if (backup != -1) {
                position = backup;
            }
            if (!sawDecimal) {
                digits.decimalAt = digitCount;
            }
            digits.decimalAt += exponent;
            if (!sawDigit && digitCount == 0) {
                return -1;
            }
        }
        return position;
    }

    public DecimalFormatSymbols getDecimalFormatSymbols() {
        try {
            return (DecimalFormatSymbols)this.symbols.clone();
        }
        catch (Exception foo) {
            return null;
        }
    }

    public void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols) {
        try {
            this.symbols = (DecimalFormatSymbols)newSymbols.clone();
            this.expandAffixes();
            this.fastPathCheckNeeded = true;
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public String getPositivePrefix() {
        return this.positivePrefix;
    }

    public void setPositivePrefix(String newValue) {
        this.positivePrefix = newValue;
        this.posPrefixPattern = null;
        this.positivePrefixFieldPositions = null;
        this.fastPathCheckNeeded = true;
    }

    private FieldPosition[] getPositivePrefixFieldPositions() {
        if (this.positivePrefixFieldPositions == null) {
            this.positivePrefixFieldPositions = this.posPrefixPattern != null ? this.expandAffix(this.posPrefixPattern) : EmptyFieldPositionArray;
        }
        return this.positivePrefixFieldPositions;
    }

    public String getNegativePrefix() {
        return this.negativePrefix;
    }

    public void setNegativePrefix(String newValue) {
        this.negativePrefix = newValue;
        this.negPrefixPattern = null;
        this.fastPathCheckNeeded = true;
    }

    private FieldPosition[] getNegativePrefixFieldPositions() {
        if (this.negativePrefixFieldPositions == null) {
            this.negativePrefixFieldPositions = this.negPrefixPattern != null ? this.expandAffix(this.negPrefixPattern) : EmptyFieldPositionArray;
        }
        return this.negativePrefixFieldPositions;
    }

    public String getPositiveSuffix() {
        return this.positiveSuffix;
    }

    public void setPositiveSuffix(String newValue) {
        this.positiveSuffix = newValue;
        this.posSuffixPattern = null;
        this.fastPathCheckNeeded = true;
    }

    private FieldPosition[] getPositiveSuffixFieldPositions() {
        if (this.positiveSuffixFieldPositions == null) {
            this.positiveSuffixFieldPositions = this.posSuffixPattern != null ? this.expandAffix(this.posSuffixPattern) : EmptyFieldPositionArray;
        }
        return this.positiveSuffixFieldPositions;
    }

    public String getNegativeSuffix() {
        return this.negativeSuffix;
    }

    public void setNegativeSuffix(String newValue) {
        this.negativeSuffix = newValue;
        this.negSuffixPattern = null;
        this.fastPathCheckNeeded = true;
    }

    private FieldPosition[] getNegativeSuffixFieldPositions() {
        if (this.negativeSuffixFieldPositions == null) {
            this.negativeSuffixFieldPositions = this.negSuffixPattern != null ? this.expandAffix(this.negSuffixPattern) : EmptyFieldPositionArray;
        }
        return this.negativeSuffixFieldPositions;
    }

    public int getMultiplier() {
        return this.multiplier;
    }

    public void setMultiplier(int newValue) {
        this.multiplier = newValue;
        this.bigDecimalMultiplier = null;
        this.bigIntegerMultiplier = null;
        this.fastPathCheckNeeded = true;
    }

    @Override
    public void setGroupingUsed(boolean newValue) {
        super.setGroupingUsed(newValue);
        this.fastPathCheckNeeded = true;
    }

    public int getGroupingSize() {
        return this.groupingSize;
    }

    public void setGroupingSize(int newValue) {
        if (newValue < 0 || newValue > 127) {
            throw new IllegalArgumentException("newValue is out of valid range. value: " + newValue);
        }
        this.groupingSize = (byte)newValue;
        this.fastPathCheckNeeded = true;
    }

    public boolean isDecimalSeparatorAlwaysShown() {
        return this.decimalSeparatorAlwaysShown;
    }

    public void setDecimalSeparatorAlwaysShown(boolean newValue) {
        this.decimalSeparatorAlwaysShown = newValue;
        this.fastPathCheckNeeded = true;
    }

    public boolean isParseBigDecimal() {
        return this.parseBigDecimal;
    }

    public void setParseBigDecimal(boolean newValue) {
        this.parseBigDecimal = newValue;
    }

    @Override
    public Object clone() {
        DecimalFormat other = (DecimalFormat)super.clone();
        other.symbols = (DecimalFormatSymbols)this.symbols.clone();
        other.digitList = (DigitList)this.digitList.clone();
        other.fastPathCheckNeeded = true;
        other.isFastPath = false;
        other.fastPathData = null;
        return other;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        DecimalFormat other = (DecimalFormat)obj;
        return (this.posPrefixPattern == other.posPrefixPattern && this.positivePrefix.equals(other.positivePrefix) || this.posPrefixPattern != null && this.posPrefixPattern.equals(other.posPrefixPattern)) && (this.posSuffixPattern == other.posSuffixPattern && this.positiveSuffix.equals(other.positiveSuffix) || this.posSuffixPattern != null && this.posSuffixPattern.equals(other.posSuffixPattern)) && (this.negPrefixPattern == other.negPrefixPattern && this.negativePrefix.equals(other.negativePrefix) || this.negPrefixPattern != null && this.negPrefixPattern.equals(other.negPrefixPattern)) && (this.negSuffixPattern == other.negSuffixPattern && this.negativeSuffix.equals(other.negativeSuffix) || this.negSuffixPattern != null && this.negSuffixPattern.equals(other.negSuffixPattern)) && this.multiplier == other.multiplier && this.groupingSize == other.groupingSize && this.decimalSeparatorAlwaysShown == other.decimalSeparatorAlwaysShown && this.parseBigDecimal == other.parseBigDecimal && this.useExponentialNotation == other.useExponentialNotation && (!this.useExponentialNotation || this.minExponentDigits == other.minExponentDigits) && this.maximumIntegerDigits == other.maximumIntegerDigits && this.minimumIntegerDigits == other.minimumIntegerDigits && this.maximumFractionDigits == other.maximumFractionDigits && this.minimumFractionDigits == other.minimumFractionDigits && this.roundingMode == other.roundingMode && this.symbols.equals(other.symbols);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 37 + this.positivePrefix.hashCode();
    }

    public String toPattern() {
        return this.toPattern(false);
    }

    public String toLocalizedPattern() {
        return this.toPattern(true);
    }

    private void expandAffixes() {
        StringBuilder buffer = new StringBuilder();
        if (this.posPrefixPattern != null) {
            this.positivePrefix = this.expandAffix(this.posPrefixPattern, buffer);
            this.positivePrefixFieldPositions = null;
        }
        if (this.posSuffixPattern != null) {
            this.positiveSuffix = this.expandAffix(this.posSuffixPattern, buffer);
            this.positiveSuffixFieldPositions = null;
        }
        if (this.negPrefixPattern != null) {
            this.negativePrefix = this.expandAffix(this.negPrefixPattern, buffer);
            this.negativePrefixFieldPositions = null;
        }
        if (this.negSuffixPattern != null) {
            this.negativeSuffix = this.expandAffix(this.negSuffixPattern, buffer);
            this.negativeSuffixFieldPositions = null;
        }
    }

    private String expandAffix(String pattern, StringBuilder buffer) {
        buffer.setLength(0);
        int i = 0;
        block6: while (i < pattern.length()) {
            char c;
            if ((c = pattern.charAt(i++)) == '\'') {
                c = pattern.charAt(i++);
                switch (c) {
                    case '\u00a4': {
                        if (i < pattern.length() && pattern.charAt(i) == '\u00a4') {
                            ++i;
                            buffer.append(this.symbols.getInternationalCurrencySymbol());
                            continue block6;
                        }
                        buffer.append(this.symbols.getCurrencySymbol());
                        continue block6;
                    }
                    case '%': {
                        buffer.append(this.symbols.getPercentText());
                        continue block6;
                    }
                    case '\u2030': {
                        buffer.append(this.symbols.getPerMillText());
                        continue block6;
                    }
                    case '-': {
                        buffer.append(this.symbols.getMinusSignText());
                        continue block6;
                    }
                }
            }
            buffer.append(c);
        }
        return buffer.toString();
    }

    private FieldPosition[] expandAffix(String pattern) {
        ArrayList<FieldPosition> positions = null;
        int stringIndex = 0;
        int i = 0;
        while (i < pattern.length()) {
            char c;
            if ((c = pattern.charAt(i++)) == '\'') {
                NumberFormat.Field fieldID = null;
                String string = null;
                c = pattern.charAt(i++);
                switch (c) {
                    case '\u00a4': {
                        if (i < pattern.length() && pattern.charAt(i) == '\u00a4') {
                            ++i;
                            string = this.symbols.getInternationalCurrencySymbol();
                        } else {
                            string = this.symbols.getCurrencySymbol();
                        }
                        fieldID = NumberFormat.Field.CURRENCY;
                        break;
                    }
                    case '%': {
                        string = this.symbols.getPercentText();
                        fieldID = NumberFormat.Field.PERCENT;
                        break;
                    }
                    case '\u2030': {
                        string = this.symbols.getPerMillText();
                        fieldID = NumberFormat.Field.PERMILLE;
                        break;
                    }
                    case '-': {
                        string = this.symbols.getMinusSignText();
                        fieldID = NumberFormat.Field.SIGN;
                    }
                }
                if (fieldID != null && !string.isEmpty()) {
                    if (positions == null) {
                        positions = new ArrayList<FieldPosition>(2);
                    }
                    FieldPosition fp = new FieldPosition(fieldID);
                    fp.setBeginIndex(stringIndex);
                    fp.setEndIndex(stringIndex + string.length());
                    positions.add(fp);
                    stringIndex += string.length();
                    continue;
                }
            }
            ++stringIndex;
        }
        if (positions != null) {
            return positions.toArray(EmptyFieldPositionArray);
        }
        return EmptyFieldPositionArray;
    }

    /*
     * Unable to fully structure code
     */
    private void appendAffix(StringBuilder buffer, String affixPattern, String expAffix, boolean localized) {
        block9: {
            block8: {
                if (affixPattern != null) break block8;
                this.appendAffix(buffer, expAffix, localized);
                break block9;
            }
            pos = 0;
            while (pos < affixPattern.length()) {
                block11: {
                    block10: {
                        i = affixPattern.indexOf(39, pos);
                        if (i < 0) {
                            this.appendAffix(buffer, affixPattern.substring(pos), localized);
                            break;
                        }
                        if (i > pos) {
                            this.appendAffix(buffer, affixPattern.substring(pos, i), localized);
                        }
                        c = affixPattern.charAt(++i);
                        ++i;
                        if (c != '\'') break block10;
                        buffer.append(c);
                        ** GOTO lbl-1000
                    }
                    if (c != '\u00a4' || i >= affixPattern.length() || affixPattern.charAt(i) != '\u00a4') break block11;
                    buffer.append(c);
                    ** GOTO lbl-1000
                }
                if (!localized) ** GOTO lbl-1000
                switch (c) {
                    case '%': {
                        buffer.append(this.symbols.getPercentText());
                        break;
                    }
                    case '\u2030': {
                        buffer.append(this.symbols.getPerMillText());
                        break;
                    }
                    case '-': {
                        buffer.append(this.symbols.getMinusSignText());
                        break;
                    }
                    default: lbl-1000:
                    // 4 sources

                    {
                        buffer.append(c);
                    }
                }
                pos = ++i;
            }
        }
    }

    private void appendAffix(StringBuilder buffer, String affix, boolean localized) {
        boolean needQuote;
        if (localized) {
            needQuote = affix.indexOf(this.symbols.getZeroDigit()) >= 0 || affix.indexOf(this.symbols.getGroupingSeparator()) >= 0 || affix.indexOf(this.symbols.getDecimalSeparator()) >= 0 || affix.indexOf(this.symbols.getPercentText()) >= 0 || affix.indexOf(this.symbols.getPerMillText()) >= 0 || affix.indexOf(this.symbols.getDigit()) >= 0 || affix.indexOf(this.symbols.getPatternSeparator()) >= 0 || affix.indexOf(this.symbols.getMinusSignText()) >= 0 || affix.indexOf(164) >= 0;
        } else {
            boolean bl = needQuote = affix.indexOf(48) >= 0 || affix.indexOf(44) >= 0 || affix.indexOf(46) >= 0 || affix.indexOf(37) >= 0 || affix.indexOf(8240) >= 0 || affix.indexOf(35) >= 0 || affix.indexOf(59) >= 0 || affix.indexOf(45) >= 0 || affix.indexOf(164) >= 0;
        }
        if (needQuote) {
            buffer.append('\'');
        }
        if (affix.indexOf(39) < 0) {
            buffer.append(affix);
        } else {
            for (int j = 0; j < affix.length(); ++j) {
                char c = affix.charAt(j);
                buffer.append(c);
                if (c != '\'') continue;
                buffer.append(c);
            }
        }
        if (needQuote) {
            buffer.append('\'');
        }
    }

    private String toPattern(boolean localized) {
        StringBuilder result = new StringBuilder();
        for (int j = 1; j >= 0; --j) {
            int digitCount;
            int i;
            if (j == 1) {
                this.appendAffix(result, this.posPrefixPattern, this.positivePrefix, localized);
            } else {
                this.appendAffix(result, this.negPrefixPattern, this.negativePrefix, localized);
            }
            for (i = digitCount = this.useExponentialNotation ? this.getMaximumIntegerDigits() : Math.max(this.groupingSize, this.getMinimumIntegerDigits()) + 1; i > 0; --i) {
                if (i != digitCount && this.isGroupingUsed() && this.groupingSize != 0 && i % this.groupingSize == 0) {
                    result.append(localized ? (this.isCurrencyFormat ? this.symbols.getMonetaryGroupingSeparator() : this.symbols.getGroupingSeparator()) : (char)',');
                }
                result.append(i <= this.getMinimumIntegerDigits() ? (localized ? this.symbols.getZeroDigit() : (char)'0') : (localized ? this.symbols.getDigit() : (char)'#'));
            }
            if (this.getMaximumFractionDigits() > 0 || this.decimalSeparatorAlwaysShown) {
                result.append(localized ? (this.isCurrencyFormat ? this.symbols.getMonetaryDecimalSeparator() : this.symbols.getDecimalSeparator()) : (char)'.');
            }
            for (i = 0; i < this.getMaximumFractionDigits(); ++i) {
                if (i < this.getMinimumFractionDigits()) {
                    result.append(localized ? this.symbols.getZeroDigit() : (char)'0');
                    continue;
                }
                result.append(localized ? this.symbols.getDigit() : (char)'#');
            }
            if (this.useExponentialNotation) {
                result.append(localized ? this.symbols.getExponentSeparator() : PATTERN_EXPONENT);
                for (i = 0; i < this.minExponentDigits; ++i) {
                    result.append(localized ? this.symbols.getZeroDigit() : (char)'0');
                }
            }
            if (j == 1) {
                this.appendAffix(result, this.posSuffixPattern, this.positiveSuffix, localized);
                if ((this.negSuffixPattern == this.posSuffixPattern && this.negativeSuffix.equals(this.positiveSuffix) || this.negSuffixPattern != null && this.negSuffixPattern.equals(this.posSuffixPattern)) && (this.negPrefixPattern != null && this.posPrefixPattern != null && this.negPrefixPattern.equals("'-" + this.posPrefixPattern) || this.negPrefixPattern == this.posPrefixPattern && this.negativePrefix.equals(this.symbols.getMinusSignText() + this.positivePrefix))) break;
                result.append(localized ? this.symbols.getPatternSeparator() : (char)';');
                continue;
            }
            this.appendAffix(result, this.negSuffixPattern, this.negativeSuffix, localized);
        }
        return result.toString();
    }

    public void applyPattern(String pattern) {
        this.applyPattern(pattern, false);
    }

    public void applyLocalizedPattern(String pattern) {
        this.applyPattern(pattern, true);
    }

    private void applyPattern(String pattern, boolean localized) {
        char zeroDigit = '0';
        char groupingSeparator = ',';
        char decimalSeparator = '.';
        char percent = '%';
        char perMill = '\u2030';
        char digit = '#';
        char separator = ';';
        String exponent = PATTERN_EXPONENT;
        char minus = '-';
        if (localized) {
            zeroDigit = this.symbols.getZeroDigit();
            groupingSeparator = this.symbols.getGroupingSeparator();
            decimalSeparator = this.symbols.getDecimalSeparator();
            percent = this.symbols.getPercent();
            perMill = this.symbols.getPerMill();
            digit = this.symbols.getDigit();
            separator = this.symbols.getPatternSeparator();
            exponent = this.symbols.getExponentSeparator();
            minus = this.symbols.getMinusSign();
        }
        boolean gotNegative = false;
        this.decimalSeparatorAlwaysShown = false;
        this.isCurrencyFormat = false;
        this.useExponentialNotation = false;
        int start = 0;
        for (int j = 1; j >= 0 && start < pattern.length(); --j) {
            boolean inQuote = false;
            StringBuilder prefix = new StringBuilder();
            StringBuilder suffix = new StringBuilder();
            int decimalPos = -1;
            int multiplier = 1;
            int digitLeftCount = 0;
            int zeroDigitCount = 0;
            int digitRightCount = 0;
            int groupingCount = -1;
            int phase = 0;
            StringBuilder affix = prefix;
            block5: for (int pos = start; pos < pattern.length(); ++pos) {
                char ch = pattern.charAt(pos);
                switch (phase) {
                    case 0: 
                    case 2: {
                        if (inQuote) {
                            if (ch == '\'') {
                                if (pos + 1 < pattern.length() && pattern.charAt(pos + 1) == '\'') {
                                    ++pos;
                                    affix.append("''");
                                    continue block5;
                                }
                                inQuote = false;
                                continue block5;
                            }
                        } else {
                            if (ch == digit || ch == zeroDigit || ch == groupingSeparator || ch == decimalSeparator) {
                                phase = 1;
                                --pos;
                                continue block5;
                            }
                            if (ch == '\u00a4') {
                                boolean doubled;
                                boolean bl = doubled = pos + 1 < pattern.length() && pattern.charAt(pos + 1) == '\u00a4';
                                if (doubled) {
                                    ++pos;
                                }
                                this.isCurrencyFormat = true;
                                affix.append(doubled ? "'\u00a4\u00a4" : "'\u00a4");
                                continue block5;
                            }
                            if (ch == '\'') {
                                if (pos + 1 < pattern.length() && pattern.charAt(pos + 1) == '\'') {
                                    ++pos;
                                    affix.append("''");
                                    continue block5;
                                }
                                inQuote = true;
                                continue block5;
                            }
                            if (ch == separator) {
                                if (phase == 0 || j == 0) {
                                    throw new IllegalArgumentException("Unquoted special character '" + ch + "' in pattern \"" + pattern + '\"');
                                }
                                start = pos + 1;
                                pos = pattern.length();
                                continue block5;
                            }
                            if (ch == percent) {
                                if (multiplier != 1) {
                                    throw new IllegalArgumentException("Too many percent/per mille characters in pattern \"" + pattern + '\"');
                                }
                                multiplier = 100;
                                affix.append("'%");
                                continue block5;
                            }
                            if (ch == perMill) {
                                if (multiplier != 1) {
                                    throw new IllegalArgumentException("Too many percent/per mille characters in pattern \"" + pattern + '\"');
                                }
                                multiplier = 1000;
                                affix.append("'\u2030");
                                continue block5;
                            }
                            if (ch == minus) {
                                affix.append("'-");
                                continue block5;
                            }
                        }
                        affix.append(ch);
                        continue block5;
                    }
                    case 1: {
                        if (j == 0) {
                            while (pos < pattern.length()) {
                                char negPatternChar = pattern.charAt(pos);
                                if (negPatternChar == digit || negPatternChar == zeroDigit || negPatternChar == groupingSeparator || negPatternChar == decimalSeparator) {
                                    ++pos;
                                    continue;
                                }
                                if (pattern.regionMatches(pos, exponent, 0, exponent.length())) {
                                    pos += exponent.length();
                                    continue;
                                }
                                --pos;
                                phase = 2;
                                affix = suffix;
                                continue block5;
                            }
                            continue block5;
                        }
                        if (ch == digit) {
                            if (zeroDigitCount > 0) {
                                ++digitRightCount;
                            } else {
                                ++digitLeftCount;
                            }
                            if (groupingCount < 0 || decimalPos >= 0) continue block5;
                            groupingCount = (byte)(groupingCount + 1);
                            continue block5;
                        }
                        if (ch == zeroDigit) {
                            if (digitRightCount > 0) {
                                throw new IllegalArgumentException("Unexpected '0' in pattern \"" + pattern + '\"');
                            }
                            ++zeroDigitCount;
                            if (groupingCount < 0 || decimalPos >= 0) continue block5;
                            groupingCount = (byte)(groupingCount + 1);
                            continue block5;
                        }
                        if (ch == groupingSeparator) {
                            groupingCount = 0;
                            continue block5;
                        }
                        if (ch == decimalSeparator) {
                            if (decimalPos >= 0) {
                                throw new IllegalArgumentException("Multiple decimal separators in pattern \"" + pattern + '\"');
                            }
                            decimalPos = digitLeftCount + zeroDigitCount + digitRightCount;
                            continue block5;
                        }
                        if (pattern.regionMatches(pos, exponent, 0, exponent.length())) {
                            if (this.useExponentialNotation) {
                                throw new IllegalArgumentException("Multiple exponential symbols in pattern \"" + pattern + '\"');
                            }
                            this.useExponentialNotation = true;
                            this.minExponentDigits = 0;
                            pos += exponent.length();
                            while (pos < pattern.length() && pattern.charAt(pos) == zeroDigit) {
                                this.minExponentDigits = (byte)(this.minExponentDigits + 1);
                                ++pos;
                            }
                            if (digitLeftCount + zeroDigitCount < 1 || this.minExponentDigits < 1) {
                                throw new IllegalArgumentException("Malformed exponential pattern \"" + pattern + '\"');
                            }
                            phase = 2;
                            affix = suffix;
                            --pos;
                            continue block5;
                        }
                        phase = 2;
                        affix = suffix;
                        --pos;
                        continue block5;
                    }
                }
            }
            if (zeroDigitCount == 0 && digitLeftCount > 0 && decimalPos >= 0) {
                int n = decimalPos;
                if (n == 0) {
                    ++n;
                }
                digitRightCount = digitLeftCount - n;
                digitLeftCount = n - 1;
                zeroDigitCount = 1;
            }
            if (decimalPos < 0 && digitRightCount > 0 || decimalPos >= 0 && (decimalPos < digitLeftCount || decimalPos > digitLeftCount + zeroDigitCount) || groupingCount == 0 || inQuote) {
                throw new IllegalArgumentException("Malformed pattern \"" + pattern + '\"');
            }
            if (j == 1) {
                this.posPrefixPattern = prefix.toString();
                this.posSuffixPattern = suffix.toString();
                this.negPrefixPattern = this.posPrefixPattern;
                this.negSuffixPattern = this.posSuffixPattern;
                int digitTotalCount = digitLeftCount + zeroDigitCount + digitRightCount;
                int effectiveDecimalPos = decimalPos >= 0 ? decimalPos : digitTotalCount;
                this.setMinimumIntegerDigits(effectiveDecimalPos - digitLeftCount);
                this.setMaximumIntegerDigits(this.useExponentialNotation ? digitLeftCount + this.getMinimumIntegerDigits() : Integer.MAX_VALUE);
                this.setMaximumFractionDigits(decimalPos >= 0 ? digitTotalCount - decimalPos : 0);
                this.setMinimumFractionDigits(decimalPos >= 0 ? digitLeftCount + zeroDigitCount - decimalPos : 0);
                this.setGroupingUsed(groupingCount > 0);
                this.groupingSize = (byte)(groupingCount > 0 ? groupingCount : 0);
                this.multiplier = multiplier;
                this.setDecimalSeparatorAlwaysShown(decimalPos == 0 || decimalPos == digitTotalCount);
                continue;
            }
            this.negPrefixPattern = prefix.toString();
            this.negSuffixPattern = suffix.toString();
            gotNegative = true;
        }
        if (pattern.isEmpty()) {
            this.posSuffixPattern = "";
            this.posPrefixPattern = "";
            this.setMinimumIntegerDigits(0);
            this.setMaximumIntegerDigits(Integer.MAX_VALUE);
            this.setMinimumFractionDigits(0);
            this.setMaximumFractionDigits(Integer.MAX_VALUE);
        }
        if (!gotNegative || this.negPrefixPattern.equals(this.posPrefixPattern) && this.negSuffixPattern.equals(this.posSuffixPattern)) {
            this.negSuffixPattern = this.posSuffixPattern;
            this.negPrefixPattern = "'-" + this.posPrefixPattern;
        }
        this.expandAffixes();
    }

    @Override
    public void setMaximumIntegerDigits(int newValue) {
        this.maximumIntegerDigits = Math.clamp((long)newValue, 0, Integer.MAX_VALUE);
        super.setMaximumIntegerDigits(Math.min(this.maximumIntegerDigits, 309));
        if (this.minimumIntegerDigits > this.maximumIntegerDigits) {
            this.minimumIntegerDigits = this.maximumIntegerDigits;
            super.setMinimumIntegerDigits(Math.min(this.minimumIntegerDigits, 309));
        }
        this.fastPathCheckNeeded = true;
    }

    @Override
    public void setMinimumIntegerDigits(int newValue) {
        this.minimumIntegerDigits = Math.clamp((long)newValue, 0, Integer.MAX_VALUE);
        super.setMinimumIntegerDigits(Math.min(this.minimumIntegerDigits, 309));
        if (this.minimumIntegerDigits > this.maximumIntegerDigits) {
            this.maximumIntegerDigits = this.minimumIntegerDigits;
            super.setMaximumIntegerDigits(Math.min(this.maximumIntegerDigits, 309));
        }
        this.fastPathCheckNeeded = true;
    }

    @Override
    public void setMaximumFractionDigits(int newValue) {
        this.maximumFractionDigits = Math.clamp((long)newValue, 0, Integer.MAX_VALUE);
        super.setMaximumFractionDigits(Math.min(this.maximumFractionDigits, 340));
        if (this.minimumFractionDigits > this.maximumFractionDigits) {
            this.minimumFractionDigits = this.maximumFractionDigits;
            super.setMinimumFractionDigits(Math.min(this.minimumFractionDigits, 340));
        }
        this.fastPathCheckNeeded = true;
    }

    @Override
    public void setMinimumFractionDigits(int newValue) {
        this.minimumFractionDigits = Math.clamp((long)newValue, 0, Integer.MAX_VALUE);
        super.setMinimumFractionDigits(Math.min(this.minimumFractionDigits, 340));
        if (this.minimumFractionDigits > this.maximumFractionDigits) {
            this.maximumFractionDigits = this.minimumFractionDigits;
            super.setMaximumFractionDigits(Math.min(this.maximumFractionDigits, 340));
        }
        this.fastPathCheckNeeded = true;
    }

    @Override
    public int getMaximumIntegerDigits() {
        return this.maximumIntegerDigits;
    }

    @Override
    public int getMinimumIntegerDigits() {
        return this.minimumIntegerDigits;
    }

    @Override
    public int getMaximumFractionDigits() {
        return this.maximumFractionDigits;
    }

    @Override
    public int getMinimumFractionDigits() {
        return this.minimumFractionDigits;
    }

    @Override
    public Currency getCurrency() {
        return this.symbols.getCurrency();
    }

    @Override
    public void setCurrency(Currency currency) {
        if (currency != this.symbols.getCurrency()) {
            this.symbols.setCurrency(currency);
            if (this.isCurrencyFormat) {
                this.expandAffixes();
            }
        }
        this.fastPathCheckNeeded = true;
    }

    @Override
    public RoundingMode getRoundingMode() {
        return this.roundingMode;
    }

    @Override
    public void setRoundingMode(RoundingMode roundingMode) {
        if (roundingMode == null) {
            throw new NullPointerException();
        }
        this.roundingMode = roundingMode;
        this.digitList.setRoundingMode(roundingMode);
        this.fastPathCheckNeeded = true;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.digitList = new DigitList();
        this.fastPathCheckNeeded = true;
        this.isFastPath = false;
        this.fastPathData = null;
        if (this.serialVersionOnStream < 4) {
            this.setRoundingMode(RoundingMode.HALF_EVEN);
        } else {
            this.setRoundingMode(this.getRoundingMode());
        }
        if (super.getMaximumIntegerDigits() > 309 || super.getMaximumFractionDigits() > 340) {
            throw new InvalidObjectException("Digit count out of range");
        }
        if (this.serialVersionOnStream < 3) {
            this.setMaximumIntegerDigits(super.getMaximumIntegerDigits());
            this.setMinimumIntegerDigits(super.getMinimumIntegerDigits());
            this.setMaximumFractionDigits(super.getMaximumFractionDigits());
            this.setMinimumFractionDigits(super.getMinimumFractionDigits());
        }
        if (this.serialVersionOnStream < 1) {
            this.useExponentialNotation = false;
        }
        if (this.groupingSize < 0) {
            this.groupingSize = (byte)3;
        }
        this.serialVersionOnStream = 4;
    }

    private static class FastPathData {
        int lastFreeIndex;
        int firstUsedIndex;
        int zeroDelta;
        char groupingChar;
        int integralLastIndex;
        int fractionalFirstIndex;
        double fractionalScaleFactor;
        int fractionalMaxIntBound;
        char[] fastPathContainer;
        char[] charsPositivePrefix;
        char[] charsNegativePrefix;
        char[] charsPositiveSuffix;
        char[] charsNegativeSuffix;
        boolean positiveAffixesRequired = true;
        boolean negativeAffixesRequired = true;

        private FastPathData() {
        }
    }

    private static class DigitArrays {
        static final char[] DigitOnes1000 = new char[1000];
        static final char[] DigitTens1000 = new char[1000];
        static final char[] DigitHundreds1000 = new char[1000];

        private DigitArrays() {
        }

        static {
            int tenIndex = 0;
            int hundredIndex = 0;
            int digitOne = 48;
            int digitTen = 48;
            int digitHundred = 48;
            for (int i = 0; i < 1000; ++i) {
                DigitArrays.DigitOnes1000[i] = digitOne;
                digitOne = digitOne == 57 ? 48 : (int)((char)(digitOne + 1));
                DigitArrays.DigitTens1000[i] = digitTen;
                if (i == tenIndex + 9) {
                    tenIndex += 10;
                    digitTen = digitTen == 57 ? 48 : (int)((char)(digitTen + 1));
                }
                DigitArrays.DigitHundreds1000[i] = digitHundred;
                if (i != hundredIndex + 99) continue;
                digitHundred = (char)(digitHundred + 1);
                hundredIndex += 100;
            }
        }
    }
}

