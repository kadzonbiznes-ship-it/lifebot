/*
 * Decompiled with CFR 0.152.
 */
package java.text;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DontCareFieldPosition;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.spi.NumberFormatProvider;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleServiceProviderPool;

public abstract class NumberFormat
extends Format {
    public static final int INTEGER_FIELD = 0;
    public static final int FRACTION_FIELD = 1;
    private static final int NUMBERSTYLE = 0;
    private static final int CURRENCYSTYLE = 1;
    private static final int PERCENTSTYLE = 2;
    private static final int SCIENTIFICSTYLE = 3;
    private static final int INTEGERSTYLE = 4;
    private static final int COMPACTSTYLE = 5;
    private boolean groupingUsed = true;
    private byte maxIntegerDigits = (byte)40;
    private byte minIntegerDigits = 1;
    private byte maxFractionDigits = (byte)3;
    private byte minFractionDigits = 0;
    private boolean parseIntegerOnly = false;
    private int maximumIntegerDigits = 40;
    private int minimumIntegerDigits = 1;
    private int maximumFractionDigits = 3;
    private int minimumFractionDigits = 0;
    static final int currentSerialVersion = 1;
    private int serialVersionOnStream = 1;
    static final long serialVersionUID = -2308460125733713944L;

    protected NumberFormat() {
    }

    @Override
    public StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos) {
        if (number instanceof Long || number instanceof Integer || number instanceof Short || number instanceof Byte || number instanceof AtomicInteger || number instanceof AtomicLong || number instanceof BigInteger && ((BigInteger)number).bitLength() < 64) {
            return this.format(((Number)number).longValue(), toAppendTo, pos);
        }
        if (number instanceof Number) {
            return this.format(((Number)number).doubleValue(), toAppendTo, pos);
        }
        throw new IllegalArgumentException("Cannot format given Object as a Number");
    }

    @Override
    public final Object parseObject(String source, ParsePosition pos) {
        return this.parse(source, pos);
    }

    public final String format(double number) {
        String result = this.fastFormat(number);
        if (result != null) {
            return result;
        }
        return this.format(number, new StringBuffer(), DontCareFieldPosition.INSTANCE).toString();
    }

    String fastFormat(double number) {
        return null;
    }

    public final String format(long number) {
        return this.format(number, new StringBuffer(), DontCareFieldPosition.INSTANCE).toString();
    }

    public abstract StringBuffer format(double var1, StringBuffer var3, FieldPosition var4);

    public abstract StringBuffer format(long var1, StringBuffer var3, FieldPosition var4);

    public abstract Number parse(String var1, ParsePosition var2);

    public Number parse(String source) throws ParseException {
        ParsePosition parsePosition = new ParsePosition(0);
        Number result = this.parse(source, parsePosition);
        if (parsePosition.index == 0) {
            throw new ParseException("Unparseable number: \"" + source + "\"", parsePosition.errorIndex);
        }
        return result;
    }

    public boolean isParseIntegerOnly() {
        return this.parseIntegerOnly;
    }

    public void setParseIntegerOnly(boolean value) {
        this.parseIntegerOnly = value;
    }

    public static final NumberFormat getInstance() {
        return NumberFormat.getInstance(Locale.getDefault(Locale.Category.FORMAT), null, 0);
    }

    public static NumberFormat getInstance(Locale inLocale) {
        return NumberFormat.getInstance(inLocale, null, 0);
    }

    public static final NumberFormat getNumberInstance() {
        return NumberFormat.getInstance(Locale.getDefault(Locale.Category.FORMAT), null, 0);
    }

    public static NumberFormat getNumberInstance(Locale inLocale) {
        return NumberFormat.getInstance(inLocale, null, 0);
    }

    public static final NumberFormat getIntegerInstance() {
        return NumberFormat.getInstance(Locale.getDefault(Locale.Category.FORMAT), null, 4);
    }

    public static NumberFormat getIntegerInstance(Locale inLocale) {
        return NumberFormat.getInstance(inLocale, null, 4);
    }

    public static final NumberFormat getCurrencyInstance() {
        return NumberFormat.getInstance(Locale.getDefault(Locale.Category.FORMAT), null, 1);
    }

    public static NumberFormat getCurrencyInstance(Locale inLocale) {
        return NumberFormat.getInstance(inLocale, null, 1);
    }

    public static final NumberFormat getPercentInstance() {
        return NumberFormat.getInstance(Locale.getDefault(Locale.Category.FORMAT), null, 2);
    }

    public static NumberFormat getPercentInstance(Locale inLocale) {
        return NumberFormat.getInstance(inLocale, null, 2);
    }

    static final NumberFormat getScientificInstance() {
        return NumberFormat.getInstance(Locale.getDefault(Locale.Category.FORMAT), null, 3);
    }

    static NumberFormat getScientificInstance(Locale inLocale) {
        return NumberFormat.getInstance(inLocale, null, 3);
    }

    public static NumberFormat getCompactNumberInstance() {
        return NumberFormat.getInstance(Locale.getDefault(Locale.Category.FORMAT), Style.SHORT, 5);
    }

    public static NumberFormat getCompactNumberInstance(Locale locale, Style formatStyle) {
        Objects.requireNonNull(locale);
        Objects.requireNonNull(formatStyle);
        return NumberFormat.getInstance(locale, formatStyle, 5);
    }

    public static Locale[] getAvailableLocales() {
        LocaleServiceProviderPool pool = LocaleServiceProviderPool.getPool(NumberFormatProvider.class);
        return pool.getAvailableLocales();
    }

    public int hashCode() {
        return this.maximumIntegerDigits * 37 + this.maxFractionDigits;
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
        NumberFormat other = (NumberFormat)obj;
        return this.maximumIntegerDigits == other.maximumIntegerDigits && this.minimumIntegerDigits == other.minimumIntegerDigits && this.maximumFractionDigits == other.maximumFractionDigits && this.minimumFractionDigits == other.minimumFractionDigits && this.groupingUsed == other.groupingUsed && this.parseIntegerOnly == other.parseIntegerOnly;
    }

    @Override
    public Object clone() {
        NumberFormat other = (NumberFormat)super.clone();
        return other;
    }

    public boolean isGroupingUsed() {
        return this.groupingUsed;
    }

    public void setGroupingUsed(boolean newValue) {
        this.groupingUsed = newValue;
    }

    public int getMaximumIntegerDigits() {
        return this.maximumIntegerDigits;
    }

    public void setMaximumIntegerDigits(int newValue) {
        this.maximumIntegerDigits = Math.max(0, newValue);
        if (this.minimumIntegerDigits > this.maximumIntegerDigits) {
            this.minimumIntegerDigits = this.maximumIntegerDigits;
        }
    }

    public int getMinimumIntegerDigits() {
        return this.minimumIntegerDigits;
    }

    public void setMinimumIntegerDigits(int newValue) {
        this.minimumIntegerDigits = Math.max(0, newValue);
        if (this.minimumIntegerDigits > this.maximumIntegerDigits) {
            this.maximumIntegerDigits = this.minimumIntegerDigits;
        }
    }

    public int getMaximumFractionDigits() {
        return this.maximumFractionDigits;
    }

    public void setMaximumFractionDigits(int newValue) {
        this.maximumFractionDigits = Math.max(0, newValue);
        if (this.maximumFractionDigits < this.minimumFractionDigits) {
            this.minimumFractionDigits = this.maximumFractionDigits;
        }
    }

    public int getMinimumFractionDigits() {
        return this.minimumFractionDigits;
    }

    public void setMinimumFractionDigits(int newValue) {
        this.minimumFractionDigits = Math.max(0, newValue);
        if (this.maximumFractionDigits < this.minimumFractionDigits) {
            this.maximumFractionDigits = this.minimumFractionDigits;
        }
    }

    public Currency getCurrency() {
        throw new UnsupportedOperationException();
    }

    public void setCurrency(Currency currency) {
        throw new UnsupportedOperationException();
    }

    public RoundingMode getRoundingMode() {
        throw new UnsupportedOperationException();
    }

    public void setRoundingMode(RoundingMode roundingMode) {
        throw new UnsupportedOperationException();
    }

    private static NumberFormat getInstance(Locale desiredLocale, Style formatStyle, int choice) {
        LocaleProviderAdapter adapter = LocaleProviderAdapter.getAdapter(NumberFormatProvider.class, desiredLocale);
        NumberFormat numberFormat = NumberFormat.getInstance(adapter, desiredLocale, formatStyle, choice);
        if (numberFormat == null) {
            numberFormat = NumberFormat.getInstance(LocaleProviderAdapter.forJRE(), desiredLocale, formatStyle, choice);
        }
        return numberFormat;
    }

    private static NumberFormat getInstance(LocaleProviderAdapter adapter, Locale locale, Style formatStyle, int choice) {
        NumberFormatProvider provider = adapter.getNumberFormatProvider();
        return switch (choice) {
            case 0 -> provider.getNumberInstance(locale);
            case 2 -> provider.getPercentInstance(locale);
            case 1 -> provider.getCurrencyInstance(locale);
            case 4 -> provider.getIntegerInstance(locale);
            case 5 -> provider.getCompactNumberInstance(locale, formatStyle);
            default -> null;
        };
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (this.serialVersionOnStream < 1) {
            this.maximumIntegerDigits = this.maxIntegerDigits;
            this.minimumIntegerDigits = this.minIntegerDigits;
            this.maximumFractionDigits = this.maxFractionDigits;
            this.minimumFractionDigits = this.minFractionDigits;
        }
        if (this.minimumIntegerDigits > this.maximumIntegerDigits || this.minimumFractionDigits > this.maximumFractionDigits || this.minimumIntegerDigits < 0 || this.minimumFractionDigits < 0) {
            throw new InvalidObjectException("Digit count range invalid");
        }
        this.serialVersionOnStream = 1;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        this.maxIntegerDigits = (byte)(this.maximumIntegerDigits > 127 ? 127 : (byte)this.maximumIntegerDigits);
        this.minIntegerDigits = (byte)(this.minimumIntegerDigits > 127 ? 127 : (byte)this.minimumIntegerDigits);
        this.maxFractionDigits = (byte)(this.maximumFractionDigits > 127 ? 127 : (byte)this.maximumFractionDigits);
        this.minFractionDigits = (byte)(this.minimumFractionDigits > 127 ? 127 : (byte)this.minimumFractionDigits);
        stream.defaultWriteObject();
    }

    public static enum Style {
        SHORT,
        LONG;

    }

    public static class Field
    extends Format.Field {
        private static final long serialVersionUID = 7494728892700160890L;
        private static final Map<String, Field> instanceMap = new HashMap<String, Field>(11);
        public static final Field INTEGER = new Field("integer");
        public static final Field FRACTION = new Field("fraction");
        public static final Field EXPONENT = new Field("exponent");
        public static final Field DECIMAL_SEPARATOR = new Field("decimal separator");
        public static final Field SIGN = new Field("sign");
        public static final Field GROUPING_SEPARATOR = new Field("grouping separator");
        public static final Field EXPONENT_SYMBOL = new Field("exponent symbol");
        public static final Field PERCENT = new Field("percent");
        public static final Field PERMILLE = new Field("per mille");
        public static final Field CURRENCY = new Field("currency");
        public static final Field EXPONENT_SIGN = new Field("exponent sign");
        public static final Field PREFIX = new Field("prefix");
        public static final Field SUFFIX = new Field("suffix");

        protected Field(String name) {
            super(name);
            if (this.getClass() == Field.class) {
                instanceMap.put(name, this);
            }
        }

        @Override
        protected Object readResolve() throws InvalidObjectException {
            if (this.getClass() != Field.class) {
                throw new InvalidObjectException("subclass didn't correctly implement readResolve");
            }
            Field instance = instanceMap.get(this.getName());
            if (instance != null) {
                return instance;
            }
            throw new InvalidObjectException("unknown attribute name");
        }
    }
}

