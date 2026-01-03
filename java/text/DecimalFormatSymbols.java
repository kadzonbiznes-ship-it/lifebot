/*
 * Decompiled with CFR 0.152.
 */
package java.text;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.spi.DecimalFormatSymbolsProvider;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;
import sun.util.locale.provider.CalendarDataUtility;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleServiceProviderPool;
import sun.util.locale.provider.ResourceBundleBasedAdapter;

public class DecimalFormatSymbols
implements Cloneable,
Serializable {
    private char zeroDigit;
    private char groupingSeparator;
    private char decimalSeparator;
    private char perMill;
    private char percent;
    private char digit;
    private char patternSeparator;
    private String infinity;
    private String NaN;
    private char minusSign;
    private String currencySymbol;
    private String intlCurrencySymbol;
    private char monetarySeparator;
    private char exponential;
    private String exponentialSeparator;
    private Locale locale;
    private String perMillText;
    private String percentText;
    private String minusSignText;
    private char monetaryGroupingSeparator;
    private transient Currency currency;
    private volatile transient boolean currencyInitialized;
    private volatile transient int hashCode;
    static final long serialVersionUID = 5772796243397350300L;
    private static final int currentSerialVersion = 5;
    private int serialVersionOnStream = 5;

    public DecimalFormatSymbols() {
        this.initialize(Locale.getDefault(Locale.Category.FORMAT));
    }

    public DecimalFormatSymbols(Locale locale) {
        this.initialize(locale);
    }

    public static Locale[] getAvailableLocales() {
        LocaleServiceProviderPool pool = LocaleServiceProviderPool.getPool(DecimalFormatSymbolsProvider.class);
        return pool.getAvailableLocales();
    }

    public static final DecimalFormatSymbols getInstance() {
        return DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT));
    }

    public static final DecimalFormatSymbols getInstance(Locale locale) {
        LocaleProviderAdapter adapter = LocaleProviderAdapter.getAdapter(DecimalFormatSymbolsProvider.class, locale);
        DecimalFormatSymbolsProvider provider = adapter.getDecimalFormatSymbolsProvider();
        DecimalFormatSymbols dfsyms = provider.getInstance(locale);
        if (dfsyms == null) {
            provider = LocaleProviderAdapter.forJRE().getDecimalFormatSymbolsProvider();
            dfsyms = provider.getInstance(locale);
        }
        return dfsyms;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public char getZeroDigit() {
        return this.zeroDigit;
    }

    public void setZeroDigit(char zeroDigit) {
        this.hashCode = 0;
        this.zeroDigit = zeroDigit;
    }

    public char getGroupingSeparator() {
        return this.groupingSeparator;
    }

    public void setGroupingSeparator(char groupingSeparator) {
        this.hashCode = 0;
        this.groupingSeparator = groupingSeparator;
    }

    public char getDecimalSeparator() {
        return this.decimalSeparator;
    }

    public void setDecimalSeparator(char decimalSeparator) {
        this.hashCode = 0;
        this.decimalSeparator = decimalSeparator;
    }

    public char getPerMill() {
        return this.perMill;
    }

    public void setPerMill(char perMill) {
        this.hashCode = 0;
        this.perMill = perMill;
        this.perMillText = Character.toString(perMill);
    }

    public char getPercent() {
        return this.percent;
    }

    public void setPercent(char percent) {
        this.hashCode = 0;
        this.percent = percent;
        this.percentText = Character.toString(percent);
    }

    public char getDigit() {
        return this.digit;
    }

    public void setDigit(char digit) {
        this.hashCode = 0;
        this.digit = digit;
    }

    public char getPatternSeparator() {
        return this.patternSeparator;
    }

    public void setPatternSeparator(char patternSeparator) {
        this.hashCode = 0;
        this.patternSeparator = patternSeparator;
    }

    public String getInfinity() {
        return this.infinity;
    }

    public void setInfinity(String infinity) {
        this.hashCode = 0;
        this.infinity = infinity;
    }

    public String getNaN() {
        return this.NaN;
    }

    public void setNaN(String NaN) {
        this.hashCode = 0;
        this.NaN = NaN;
    }

    public char getMinusSign() {
        return this.minusSign;
    }

    public void setMinusSign(char minusSign) {
        this.hashCode = 0;
        this.minusSign = minusSign;
        this.minusSignText = Character.toString(minusSign);
    }

    public String getCurrencySymbol() {
        this.initializeCurrency(this.locale);
        return this.currencySymbol;
    }

    public void setCurrencySymbol(String currency) {
        this.initializeCurrency(this.locale);
        this.hashCode = 0;
        this.currencySymbol = currency;
    }

    public String getInternationalCurrencySymbol() {
        this.initializeCurrency(this.locale);
        return this.intlCurrencySymbol;
    }

    public void setInternationalCurrencySymbol(String currencyCode) {
        this.initializeCurrency(this.locale);
        this.hashCode = 0;
        this.intlCurrencySymbol = currencyCode;
        this.currency = null;
        if (currencyCode != null) {
            try {
                this.currency = Currency.getInstance(currencyCode);
                this.currencySymbol = this.currency.getSymbol();
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
    }

    public Currency getCurrency() {
        this.initializeCurrency(this.locale);
        return this.currency;
    }

    public void setCurrency(Currency currency) {
        if (currency == null) {
            throw new NullPointerException();
        }
        this.initializeCurrency(this.locale);
        this.hashCode = 0;
        this.currency = currency;
        this.intlCurrencySymbol = currency.getCurrencyCode();
        this.currencySymbol = currency.getSymbol(this.locale);
    }

    public char getMonetaryDecimalSeparator() {
        return this.monetarySeparator;
    }

    public void setMonetaryDecimalSeparator(char sep) {
        this.hashCode = 0;
        this.monetarySeparator = sep;
    }

    public String getExponentSeparator() {
        return this.exponentialSeparator;
    }

    public void setExponentSeparator(String exp) {
        if (exp == null) {
            throw new NullPointerException();
        }
        this.hashCode = 0;
        this.exponentialSeparator = exp;
    }

    public char getMonetaryGroupingSeparator() {
        return this.monetaryGroupingSeparator;
    }

    public void setMonetaryGroupingSeparator(char monetaryGroupingSeparator) {
        this.hashCode = 0;
        this.monetaryGroupingSeparator = monetaryGroupingSeparator;
    }

    char getExponentialSymbol() {
        return this.exponential;
    }

    void setExponentialSymbol(char exp) {
        this.exponential = exp;
    }

    String getPerMillText() {
        return this.perMillText;
    }

    void setPerMillText(String perMillText) {
        Objects.requireNonNull(perMillText);
        if (perMillText.isEmpty()) {
            throw new IllegalArgumentException("Empty argument string");
        }
        this.hashCode = 0;
        this.perMillText = perMillText;
        this.perMill = this.findNonFormatChar(perMillText, '\u2030');
    }

    String getPercentText() {
        return this.percentText;
    }

    void setPercentText(String percentText) {
        Objects.requireNonNull(percentText);
        if (percentText.isEmpty()) {
            throw new IllegalArgumentException("Empty argument string");
        }
        this.hashCode = 0;
        this.percentText = percentText;
        this.percent = this.findNonFormatChar(percentText, '%');
    }

    String getMinusSignText() {
        return this.minusSignText;
    }

    void setMinusSignText(String minusSignText) {
        Objects.requireNonNull(minusSignText);
        if (minusSignText.isEmpty()) {
            throw new IllegalArgumentException("Empty argument string");
        }
        this.hashCode = 0;
        this.minusSignText = minusSignText;
        this.minusSign = this.findNonFormatChar(minusSignText, '-');
    }

    public Object clone() {
        try {
            return (DecimalFormatSymbols)super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        DecimalFormatSymbols other = (DecimalFormatSymbols)obj;
        return this.zeroDigit == other.zeroDigit && this.groupingSeparator == other.groupingSeparator && this.decimalSeparator == other.decimalSeparator && this.percent == other.percent && this.percentText.equals(other.percentText) && this.perMill == other.perMill && this.perMillText.equals(other.perMillText) && this.digit == other.digit && this.minusSign == other.minusSign && this.minusSignText.equals(other.minusSignText) && this.patternSeparator == other.patternSeparator && this.infinity.equals(other.infinity) && this.NaN.equals(other.NaN) && this.getCurrencySymbol().equals(other.getCurrencySymbol()) && this.intlCurrencySymbol.equals(other.intlCurrencySymbol) && this.currency == other.currency && this.monetarySeparator == other.monetarySeparator && this.monetaryGroupingSeparator == other.monetaryGroupingSeparator && this.exponentialSeparator.equals(other.exponentialSeparator) && this.locale.equals(other.locale);
    }

    public int hashCode() {
        if (this.hashCode == 0) {
            this.hashCode = Objects.hash(Character.valueOf(this.zeroDigit), Character.valueOf(this.groupingSeparator), Character.valueOf(this.decimalSeparator), Character.valueOf(this.percent), this.percentText, Character.valueOf(this.perMill), this.perMillText, Character.valueOf(this.digit), Character.valueOf(this.minusSign), this.minusSignText, Character.valueOf(this.patternSeparator), this.infinity, this.NaN, this.getCurrencySymbol(), this.intlCurrencySymbol, this.currency, Character.valueOf(this.monetarySeparator), Character.valueOf(this.monetaryGroupingSeparator), this.exponentialSeparator, this.locale);
        }
        return this.hashCode;
    }

    private void initialize(Locale locale) {
        this.locale = locale;
        Locale override = locale.getUnicodeLocaleType("nu") == null ? CalendarDataUtility.findRegionOverride(locale) : locale;
        LocaleProviderAdapter adapter = LocaleProviderAdapter.getAdapter(DecimalFormatSymbolsProvider.class, override);
        if (!(adapter instanceof ResourceBundleBasedAdapter)) {
            adapter = LocaleProviderAdapter.getResourceBundleBased();
        }
        Object[] data = adapter.getLocaleResources(override).getDecimalFormatSymbolsData();
        String[] numberElements = (String[])data[0];
        this.decimalSeparator = numberElements[0].charAt(0);
        this.groupingSeparator = numberElements[1].charAt(0);
        this.patternSeparator = numberElements[2].charAt(0);
        this.percentText = numberElements[3];
        this.percent = this.findNonFormatChar(this.percentText, '%');
        this.zeroDigit = numberElements[4].charAt(0);
        this.digit = numberElements[5].charAt(0);
        this.minusSignText = numberElements[6];
        this.minusSign = this.findNonFormatChar(this.minusSignText, '-');
        this.exponential = numberElements[7].charAt(0);
        this.exponentialSeparator = numberElements[7];
        this.perMillText = numberElements[8];
        this.perMill = this.findNonFormatChar(this.perMillText, '\u2030');
        this.infinity = numberElements[9];
        this.NaN = numberElements[10];
        this.monetarySeparator = numberElements.length < 12 || numberElements[11].isEmpty() ? this.decimalSeparator : numberElements[11].charAt(0);
        this.monetaryGroupingSeparator = numberElements.length < 13 || numberElements[12].isEmpty() ? this.groupingSeparator : numberElements[12].charAt(0);
        this.intlCurrencySymbol = (String)data[1];
        this.currencySymbol = (String)data[2];
    }

    private char findNonFormatChar(String src, char defChar) {
        return (char)src.chars().filter(c -> Character.getType(c) != 16).findFirst().orElse(defChar);
    }

    private void initializeCurrency(Locale locale) {
        if (this.currencyInitialized) {
            return;
        }
        if (!locale.getCountry().isEmpty()) {
            try {
                this.currency = Currency.getInstance(locale);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        if (this.currency != null) {
            LocaleProviderAdapter adapter = LocaleProviderAdapter.getAdapter(DecimalFormatSymbolsProvider.class, locale);
            if (!(adapter instanceof ResourceBundleBasedAdapter)) {
                adapter = LocaleProviderAdapter.getResourceBundleBased();
            }
            Object[] data = adapter.getLocaleResources(locale).getDecimalFormatSymbolsData();
            this.intlCurrencySymbol = this.currency.getCurrencyCode();
            if (data[1] != null && data[1] == this.intlCurrencySymbol) {
                this.currencySymbol = (String)data[2];
            } else {
                this.currencySymbol = this.currency.getSymbol(locale);
                data[1] = this.intlCurrencySymbol;
                data[2] = this.currencySymbol;
            }
        } else {
            this.intlCurrencySymbol = "XXX";
            try {
                this.currency = Currency.getInstance(this.intlCurrencySymbol);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
            this.currencySymbol = "\u00a4";
        }
        this.currencyInitialized = true;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (this.serialVersionOnStream < 1) {
            this.monetarySeparator = this.decimalSeparator;
            this.exponential = (char)69;
        }
        if (this.serialVersionOnStream < 2) {
            this.locale = Locale.ROOT;
        }
        if (this.serialVersionOnStream < 3) {
            this.exponentialSeparator = Character.toString(this.exponential);
        }
        if (this.serialVersionOnStream < 4) {
            this.perMillText = Character.toString(this.perMill);
            this.percentText = Character.toString(this.percent);
            this.minusSignText = Character.toString(this.minusSign);
        } else if (this.findNonFormatChar(this.perMillText, '\uffff') != this.perMill || this.findNonFormatChar(this.percentText, '\uffff') != this.percent || this.findNonFormatChar(this.minusSignText, '\uffff') != this.minusSign) {
            throw new InvalidObjectException("'char' and 'String' representations of either percent, per mille, and/or minus sign disagree.");
        }
        if (this.serialVersionOnStream < 5) {
            this.monetaryGroupingSeparator = this.groupingSeparator;
        }
        this.serialVersionOnStream = 5;
        if (this.intlCurrencySymbol != null) {
            try {
                this.currency = Currency.getInstance(this.intlCurrencySymbol);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
            this.currencyInitialized = true;
        }
    }
}

