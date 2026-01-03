/*
 * Decompiled with CFR 0.152.
 */
package java.time.format;

import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class DecimalStyle {
    public static final DecimalStyle STANDARD = new DecimalStyle('0', '+', '-', '.');
    private static final ConcurrentMap<Locale, DecimalStyle> CACHE = new ConcurrentHashMap<Locale, DecimalStyle>(16, 0.75f, 2);
    private final char zeroDigit;
    private final char positiveSign;
    private final char negativeSign;
    private final char decimalSeparator;

    public static Set<Locale> getAvailableLocales() {
        Locale[] l = DecimalFormatSymbols.getAvailableLocales();
        HashSet<Locale> locales = HashSet.newHashSet(l.length);
        Collections.addAll(locales, l);
        return locales;
    }

    public static DecimalStyle ofDefaultLocale() {
        return DecimalStyle.of(Locale.getDefault(Locale.Category.FORMAT));
    }

    public static DecimalStyle of(Locale locale) {
        Objects.requireNonNull(locale, "locale");
        return CACHE.computeIfAbsent(locale, DecimalStyle::create);
    }

    private static DecimalStyle create(Locale locale) {
        DecimalFormatSymbols oldSymbols = DecimalFormatSymbols.getInstance(locale);
        char zeroDigit = oldSymbols.getZeroDigit();
        char positiveSign = '+';
        char negativeSign = oldSymbols.getMinusSign();
        char decimalSeparator = oldSymbols.getDecimalSeparator();
        if (zeroDigit == '0' && negativeSign == '-' && decimalSeparator == '.') {
            return STANDARD;
        }
        return new DecimalStyle(zeroDigit, positiveSign, negativeSign, decimalSeparator);
    }

    private DecimalStyle(char zeroChar, char positiveSignChar, char negativeSignChar, char decimalPointChar) {
        this.zeroDigit = zeroChar;
        this.positiveSign = positiveSignChar;
        this.negativeSign = negativeSignChar;
        this.decimalSeparator = decimalPointChar;
    }

    public char getZeroDigit() {
        return this.zeroDigit;
    }

    public DecimalStyle withZeroDigit(char zeroDigit) {
        if (zeroDigit == this.zeroDigit) {
            return this;
        }
        return new DecimalStyle(zeroDigit, this.positiveSign, this.negativeSign, this.decimalSeparator);
    }

    public char getPositiveSign() {
        return this.positiveSign;
    }

    public DecimalStyle withPositiveSign(char positiveSign) {
        if (positiveSign == this.positiveSign) {
            return this;
        }
        return new DecimalStyle(this.zeroDigit, positiveSign, this.negativeSign, this.decimalSeparator);
    }

    public char getNegativeSign() {
        return this.negativeSign;
    }

    public DecimalStyle withNegativeSign(char negativeSign) {
        if (negativeSign == this.negativeSign) {
            return this;
        }
        return new DecimalStyle(this.zeroDigit, this.positiveSign, negativeSign, this.decimalSeparator);
    }

    public char getDecimalSeparator() {
        return this.decimalSeparator;
    }

    public DecimalStyle withDecimalSeparator(char decimalSeparator) {
        if (decimalSeparator == this.decimalSeparator) {
            return this;
        }
        return new DecimalStyle(this.zeroDigit, this.positiveSign, this.negativeSign, decimalSeparator);
    }

    int convertToDigit(char ch) {
        int val = ch - this.zeroDigit;
        return val >= 0 && val <= 9 ? val : -1;
    }

    String convertNumberToI18N(String numericText) {
        if (this.zeroDigit == '0') {
            return numericText;
        }
        int diff = this.zeroDigit - 48;
        char[] array = numericText.toCharArray();
        for (int i = 0; i < array.length; ++i) {
            array[i] = (char)(array[i] + diff);
        }
        return new String(array);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DecimalStyle)) return false;
        DecimalStyle other = (DecimalStyle)obj;
        if (this.zeroDigit != other.zeroDigit) return false;
        if (this.positiveSign != other.positiveSign) return false;
        if (this.negativeSign != other.negativeSign) return false;
        if (this.decimalSeparator != other.decimalSeparator) return false;
        return true;
    }

    public int hashCode() {
        return this.zeroDigit + this.positiveSign + this.negativeSign + this.decimalSeparator;
    }

    public String toString() {
        return "DecimalStyle[" + this.zeroDigit + this.positiveSign + this.negativeSign + this.decimalSeparator + "]";
    }
}

