/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.spi.CurrencyNameProvider;
import java.util.stream.Collectors;
import jdk.internal.util.StaticProperty;
import sun.util.locale.provider.CalendarDataUtility;
import sun.util.locale.provider.LocaleServiceProviderPool;
import sun.util.logging.PlatformLogger;

public final class Currency
implements Serializable {
    private static final long serialVersionUID = -158308464356906721L;
    private final String currencyCode;
    private final transient int defaultFractionDigits;
    private final transient int numericCode;
    private static ConcurrentMap<String, Currency> instances = new ConcurrentHashMap<String, Currency>(7);
    private static HashSet<Currency> available;
    static int formatVersion;
    static int dataVersion;
    static int[] mainTable;
    static List<SpecialCaseEntry> specialCasesList;
    static List<OtherCurrencyEntry> otherCurrenciesList;
    private static final int MAGIC_NUMBER = 1131770436;
    private static final int A_TO_Z = 26;
    private static final int INVALID_COUNTRY_ENTRY = 127;
    private static final int COUNTRY_WITHOUT_CURRENCY_ENTRY = 512;
    private static final int SIMPLE_CASE_COUNTRY_MASK = 0;
    private static final int SIMPLE_CASE_COUNTRY_FINAL_CHAR_MASK = 31;
    private static final int SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_MASK = 480;
    private static final int SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_SHIFT = 5;
    private static final int SIMPLE_CASE_COUNTRY_MAX_DEFAULT_DIGITS = 9;
    private static final int SPECIAL_CASE_COUNTRY_MASK = 512;
    private static final int SPECIAL_CASE_COUNTRY_INDEX_MASK = 31;
    private static final int SPECIAL_CASE_COUNTRY_INDEX_DELTA = 1;
    private static final int COUNTRY_TYPE_MASK = 512;
    private static final int NUMERIC_CODE_MASK = 1047552;
    private static final int NUMERIC_CODE_SHIFT = 10;
    private static final int VALID_FORMAT_VERSION = 3;
    private static final int SYMBOL = 0;
    private static final int DISPLAYNAME = 1;

    private static void initStatic() {
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Void run() {
                block20: {
                    try (InputStream in = this.getClass().getResourceAsStream("/java/util/currency.data");){
                        if (in == null) {
                            throw new InternalError("Currency data not found");
                        }
                        DataInputStream dis = new DataInputStream(new BufferedInputStream(in));
                        if (dis.readInt() != 1131770436) {
                            throw new InternalError("Currency data is possibly corrupted");
                        }
                        formatVersion = dis.readInt();
                        if (formatVersion != 3) {
                            throw new InternalError("Currency data format is incorrect");
                        }
                        dataVersion = dis.readInt();
                        mainTable = Currency.readIntArray(dis, 676);
                        int scCount = dis.readInt();
                        specialCasesList = Currency.readSpecialCases(dis, scCount);
                        int ocCount = dis.readInt();
                        otherCurrenciesList = Currency.readOtherCurrencies(dis, ocCount);
                    }
                    catch (IOException e) {
                        throw new InternalError(e);
                    }
                    String propsFile = System.getProperty("java.util.currency.data");
                    if (propsFile == null) {
                        propsFile = StaticProperty.javaHome() + File.separator + "lib" + File.separator + "currency.properties";
                    }
                    try {
                        File propFile = new File(propsFile);
                        if (!propFile.exists()) break block20;
                        Properties props = new Properties();
                        try (FileReader fr = new FileReader(propFile);){
                            props.load(fr);
                        }
                        Pattern propertiesPattern = Pattern.compile("([A-Z]{3})\\s*,\\s*(\\d{3})\\s*,\\s*(\\d+)\\s*,?\\s*(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})?");
                        List<CurrencyProperty> currencyEntries = Currency.getValidCurrencyData(props, propertiesPattern);
                        currencyEntries.forEach(Currency::replaceCurrencyData);
                    }
                    catch (IOException e) {
                        CurrencyProperty.info("currency.properties is ignored because of an IOException", e);
                    }
                }
                return null;
            }
        });
    }

    private Currency(String currencyCode, int defaultFractionDigits, int numericCode) {
        this.currencyCode = currencyCode;
        this.defaultFractionDigits = defaultFractionDigits;
        this.numericCode = numericCode;
    }

    public static Currency getInstance(String currencyCode) {
        return Currency.getInstance(currencyCode, Integer.MIN_VALUE, 0);
    }

    private static Currency getInstance(String currencyCode, int defaultFractionDigits, int numericCode) {
        Currency currencyVal;
        Currency instance = (Currency)instances.get(currencyCode);
        if (instance != null) {
            return instance;
        }
        if (defaultFractionDigits == Integer.MIN_VALUE) {
            char char2;
            boolean found = false;
            if (currencyCode.length() != 3) {
                throw new IllegalArgumentException("The input currency code: \"%s\" must have a length of 3 characters".formatted(currencyCode));
            }
            char char1 = currencyCode.charAt(0);
            int tableEntry = Currency.getMainTableEntry(char1, char2 = currencyCode.charAt(1));
            if ((tableEntry & 0x200) == 0 && tableEntry != 127 && currencyCode.charAt(2) - 65 == (tableEntry & 0x1F)) {
                defaultFractionDigits = (tableEntry & 0x1E0) >> 5;
                numericCode = (tableEntry & 0xFFC00) >> 10;
                found = true;
            } else {
                int[] fractionAndNumericCode = SpecialCaseEntry.findEntry(currencyCode);
                if (fractionAndNumericCode != null) {
                    defaultFractionDigits = fractionAndNumericCode[0];
                    numericCode = fractionAndNumericCode[1];
                    found = true;
                }
            }
            if (!found) {
                OtherCurrencyEntry ocEntry = OtherCurrencyEntry.findEntry(currencyCode);
                if (ocEntry == null) {
                    throw new IllegalArgumentException("The input currency code: \"%s\" is not a valid ISO 4217 code".formatted(currencyCode));
                }
                defaultFractionDigits = ocEntry.fraction;
                numericCode = ocEntry.numericCode;
            }
        }
        return (instance = instances.putIfAbsent(currencyCode, currencyVal = new Currency(currencyCode, defaultFractionDigits, numericCode))) != null ? instance : currencyVal;
    }

    public static Currency getInstance(Locale locale) {
        char char2;
        String country;
        String override = locale.getUnicodeLocaleType("cu");
        if (override != null) {
            try {
                return Currency.getInstance(override.toUpperCase(Locale.ROOT));
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        if ((country = CalendarDataUtility.findRegionOverride(locale).getCountry()) == null || !country.matches("^[a-zA-Z]{2}$")) {
            throw new IllegalArgumentException("The country of the input locale: \"%s\" is not a valid ISO 3166 country code".formatted(locale));
        }
        char char1 = country.charAt(0);
        int tableEntry = Currency.getMainTableEntry(char1, char2 = country.charAt(1));
        if ((tableEntry & 0x200) == 0 && tableEntry != 127) {
            char finalChar = (char)((tableEntry & 0x1F) + 65);
            int defaultFractionDigits = (tableEntry & 0x1E0) >> 5;
            int numericCode = (tableEntry & 0xFFC00) >> 10;
            StringBuilder sb = new StringBuilder(country);
            sb.append(finalChar);
            return Currency.getInstance(sb.toString(), defaultFractionDigits, numericCode);
        }
        if (tableEntry == 127) {
            throw new IllegalArgumentException("The country of the input locale: \"%s\" is not a valid ISO 3166 country code".formatted(locale));
        }
        if (tableEntry == 512) {
            return null;
        }
        int index = SpecialCaseEntry.toIndex(tableEntry);
        SpecialCaseEntry scEntry = specialCasesList.get(index);
        if (scEntry.cutOverTime == Long.MAX_VALUE || System.currentTimeMillis() < scEntry.cutOverTime) {
            return Currency.getInstance(scEntry.oldCurrency, scEntry.oldCurrencyFraction, scEntry.oldCurrencyNumericCode);
        }
        return Currency.getInstance(scEntry.newCurrency, scEntry.newCurrencyFraction, scEntry.newCurrencyNumericCode);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Set<Currency> getAvailableCurrencies() {
        Class<Currency> clazz = Currency.class;
        synchronized (Currency.class) {
            if (available == null) {
                available = new HashSet(256);
                for (char c1 = 'A'; c1 <= 'Z'; c1 = (char)(c1 + '\u0001')) {
                    for (char c2 = 'A'; c2 <= 'Z'; c2 = (char)(c2 + '\u0001')) {
                        int tableEntry = Currency.getMainTableEntry(c1, c2);
                        if ((tableEntry & 0x200) == 0 && tableEntry != 127) {
                            char finalChar = (char)((tableEntry & 0x1F) + 65);
                            int defaultFractionDigits = (tableEntry & 0x1E0) >> 5;
                            int numericCode = (tableEntry & 0xFFC00) >> 10;
                            StringBuilder sb = new StringBuilder();
                            sb.append(c1);
                            sb.append(c2);
                            sb.append(finalChar);
                            available.add(Currency.getInstance(sb.toString(), defaultFractionDigits, numericCode));
                            continue;
                        }
                        if ((tableEntry & 0x200) != 512 || tableEntry == 127 || tableEntry == 512) continue;
                        int index = SpecialCaseEntry.toIndex(tableEntry);
                        SpecialCaseEntry scEntry = specialCasesList.get(index);
                        if (scEntry.cutOverTime == Long.MAX_VALUE || System.currentTimeMillis() < scEntry.cutOverTime) {
                            available.add(Currency.getInstance(scEntry.oldCurrency, scEntry.oldCurrencyFraction, scEntry.oldCurrencyNumericCode));
                            continue;
                        }
                        available.add(Currency.getInstance(scEntry.newCurrency, scEntry.newCurrencyFraction, scEntry.newCurrencyNumericCode));
                    }
                }
                for (OtherCurrencyEntry entry : otherCurrenciesList) {
                    available.add(Currency.getInstance(entry.currencyCode));
                }
            }
            // ** MonitorExit[var0] (shouldn't be in output)
            Set result = (Set)available.clone();
            return result;
        }
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public String getSymbol() {
        return this.getSymbol(Locale.getDefault(Locale.Category.DISPLAY));
    }

    public String getSymbol(Locale locale) {
        LocaleServiceProviderPool pool = LocaleServiceProviderPool.getPool(CurrencyNameProvider.class);
        locale = CalendarDataUtility.findRegionOverride(locale);
        String symbol = pool.getLocalizedObject(CurrencyNameGetter.INSTANCE, locale, this.currencyCode, 0);
        if (symbol != null) {
            return symbol;
        }
        return this.currencyCode;
    }

    public int getDefaultFractionDigits() {
        return this.defaultFractionDigits;
    }

    public int getNumericCode() {
        return this.numericCode;
    }

    public String getNumericCodeAsString() {
        if (this.numericCode < 100) {
            StringBuilder sb = new StringBuilder();
            sb.append('0');
            if (this.numericCode < 10) {
                sb.append('0');
            }
            return sb.append(this.numericCode).toString();
        }
        return String.valueOf(this.numericCode);
    }

    public String getDisplayName() {
        return this.getDisplayName(Locale.getDefault(Locale.Category.DISPLAY));
    }

    public String getDisplayName(Locale locale) {
        LocaleServiceProviderPool pool = LocaleServiceProviderPool.getPool(CurrencyNameProvider.class);
        String result = pool.getLocalizedObject(CurrencyNameGetter.INSTANCE, locale, this.currencyCode, 1);
        if (result != null) {
            return result;
        }
        return this.currencyCode;
    }

    public String toString() {
        return this.currencyCode;
    }

    private Object readResolve() {
        return Currency.getInstance(this.currencyCode);
    }

    private static int getMainTableEntry(char char1, char char2) {
        if (char1 < 'A' || char1 > 'Z' || char2 < 'A' || char2 > 'Z') {
            throw new IllegalArgumentException("The country code: \"%c%c\" is not a valid ISO 3166 code".formatted(Character.valueOf(char1), Character.valueOf(char2)));
        }
        return mainTable[(char1 - 65) * 26 + (char2 - 65)];
    }

    private static void setMainTableEntry(char char1, char char2, int entry) {
        if (char1 < 'A' || char1 > 'Z' || char2 < 'A' || char2 > 'Z') {
            throw new IllegalArgumentException("The country code: \"%c%c\" is not a valid ISO 3166 code".formatted(Character.valueOf(char1), Character.valueOf(char2)));
        }
        Currency.mainTable[(char1 - 65) * 26 + (char2 - 65)] = entry;
    }

    private static int[] readIntArray(DataInputStream dis, int count) throws IOException {
        int[] ret = new int[count];
        for (int i = 0; i < count; ++i) {
            ret[i] = dis.readInt();
        }
        return ret;
    }

    private static List<SpecialCaseEntry> readSpecialCases(DataInputStream dis, int count) throws IOException {
        ArrayList<SpecialCaseEntry> list = new ArrayList<SpecialCaseEntry>(count);
        for (int i = 0; i < count; ++i) {
            long cutOverTime = dis.readLong();
            String oldCurrency = dis.readUTF();
            String newCurrency = dis.readUTF();
            int oldCurrencyFraction = dis.readInt();
            int newCurrencyFraction = dis.readInt();
            int oldCurrencyNumericCode = dis.readInt();
            int newCurrencyNumericCode = dis.readInt();
            SpecialCaseEntry sc = new SpecialCaseEntry(cutOverTime, oldCurrency, newCurrency, oldCurrencyFraction, newCurrencyFraction, oldCurrencyNumericCode, newCurrencyNumericCode);
            list.add(sc);
        }
        return list;
    }

    private static List<OtherCurrencyEntry> readOtherCurrencies(DataInputStream dis, int count) throws IOException {
        ArrayList<OtherCurrencyEntry> list = new ArrayList<OtherCurrencyEntry>(count);
        for (int i = 0; i < count; ++i) {
            String currencyCode = dis.readUTF();
            int fraction = dis.readInt();
            int numericCode = dis.readInt();
            OtherCurrencyEntry oc = new OtherCurrencyEntry(currencyCode, fraction, numericCode);
            list.add(oc);
        }
        return list;
    }

    private static List<CurrencyProperty> getValidCurrencyData(Properties props, Pattern pattern) {
        Set<String> keys = props.stringPropertyNames();
        ArrayList<CurrencyProperty> propertyEntries = new ArrayList<CurrencyProperty>();
        Map<String, List<CurrencyProperty>> currencyCodeGroup = keys.stream().map(k -> CurrencyProperty.getValidEntry(k.toUpperCase(Locale.ROOT), props.getProperty((String)k).toUpperCase(Locale.ROOT), pattern)).flatMap(o -> o.stream()).collect(Collectors.groupingBy(entry -> entry.currencyCode));
        currencyCodeGroup.forEach((curCode, list) -> {
            boolean inconsistent = CurrencyProperty.containsInconsistentInstances(list);
            if (inconsistent) {
                list.forEach(prop -> CurrencyProperty.info("The property entry for " + prop.country + " is inconsistent. Ignored.", null));
            } else {
                propertyEntries.addAll((Collection<CurrencyProperty>)list);
            }
        });
        return propertyEntries;
    }

    private static void replaceCurrencyData(CurrencyProperty prop) {
        String ctry = prop.country;
        String code = prop.currencyCode;
        int numeric = prop.numericCode;
        int fraction = prop.fraction;
        int entry = numeric << 10;
        int index = SpecialCaseEntry.indexOf(code, fraction, numeric);
        int scCurrencyCodeIndex = -1;
        if (index == -1 && (scCurrencyCodeIndex = SpecialCaseEntry.currencyCodeIndex(code)) != -1) {
            specialCasesList.set(scCurrencyCodeIndex, new SpecialCaseEntry(code, fraction, numeric));
            OtherCurrencyEntry oe = OtherCurrencyEntry.findEntry(code);
            if (oe != null) {
                int oIndex = otherCurrenciesList.indexOf(oe);
                otherCurrenciesList.set(oIndex, new OtherCurrencyEntry(code, fraction, numeric));
            }
        }
        if (index == -1 && (ctry.charAt(0) != code.charAt(0) || ctry.charAt(1) != code.charAt(1))) {
            if (scCurrencyCodeIndex == -1) {
                specialCasesList.add(new SpecialCaseEntry(code, fraction, numeric));
                index = specialCasesList.size() - 1;
            } else {
                index = scCurrencyCodeIndex;
            }
            Currency.updateMainTableEntry(code, fraction, numeric);
        }
        entry = index == -1 ? (entry |= fraction << 5 | code.charAt(2) - 65) : 0x200 | index + 1;
        Currency.setMainTableEntry(ctry.charAt(0), ctry.charAt(1), entry);
    }

    private static void updateMainTableEntry(String code, int fraction, int numeric) {
        int tableEntry = Currency.getMainTableEntry(code.charAt(0), code.charAt(1));
        int entry = numeric << 10;
        if ((tableEntry & 0x200) == 0 && tableEntry != 127 && code.charAt(2) - 65 == (tableEntry & 0x1F)) {
            int numericCode = (tableEntry & 0xFFC00) >> 10;
            int defaultFractionDigits = (tableEntry & 0x1E0) >> 5;
            if (numeric != numericCode || fraction != defaultFractionDigits) {
                Currency.setMainTableEntry(code.charAt(0), code.charAt(1), entry |= fraction << 5 | code.charAt(2) - 65);
            }
        }
    }

    static {
        Currency.initStatic();
    }

    private static class SpecialCaseEntry {
        private final long cutOverTime;
        private final String oldCurrency;
        private final String newCurrency;
        private final int oldCurrencyFraction;
        private final int newCurrencyFraction;
        private final int oldCurrencyNumericCode;
        private final int newCurrencyNumericCode;

        private SpecialCaseEntry(long cutOverTime, String oldCurrency, String newCurrency, int oldCurrencyFraction, int newCurrencyFraction, int oldCurrencyNumericCode, int newCurrencyNumericCode) {
            this.cutOverTime = cutOverTime;
            this.oldCurrency = oldCurrency;
            this.newCurrency = newCurrency;
            this.oldCurrencyFraction = oldCurrencyFraction;
            this.newCurrencyFraction = newCurrencyFraction;
            this.oldCurrencyNumericCode = oldCurrencyNumericCode;
            this.newCurrencyNumericCode = newCurrencyNumericCode;
        }

        private SpecialCaseEntry(String currencyCode, int fraction, int numericCode) {
            this(Long.MAX_VALUE, currencyCode, "", fraction, 0, numericCode, 0);
        }

        private static int indexOf(String code, int fraction, int numeric) {
            int size = specialCasesList.size();
            for (int index = 0; index < size; ++index) {
                SpecialCaseEntry scEntry = specialCasesList.get(index);
                if (!scEntry.oldCurrency.equals(code) || scEntry.oldCurrencyFraction != fraction || scEntry.oldCurrencyNumericCode != numeric || scEntry.cutOverTime != Long.MAX_VALUE) continue;
                return index;
            }
            return -1;
        }

        private static int[] findEntry(String code) {
            int[] fractionAndNumericCode = null;
            int size = specialCasesList.size();
            for (int index = 0; index < size; ++index) {
                SpecialCaseEntry scEntry = specialCasesList.get(index);
                if (scEntry.oldCurrency.equals(code) && (scEntry.cutOverTime == Long.MAX_VALUE || System.currentTimeMillis() < scEntry.cutOverTime)) {
                    fractionAndNumericCode = new int[]{scEntry.oldCurrencyFraction, scEntry.oldCurrencyNumericCode};
                    break;
                }
                if (!scEntry.newCurrency.equals(code) || System.currentTimeMillis() < scEntry.cutOverTime) continue;
                fractionAndNumericCode = new int[]{scEntry.newCurrencyFraction, scEntry.newCurrencyNumericCode};
                break;
            }
            return fractionAndNumericCode;
        }

        private static int currencyCodeIndex(String code) {
            int size = specialCasesList.size();
            for (int index = 0; index < size; ++index) {
                SpecialCaseEntry scEntry = specialCasesList.get(index);
                if (scEntry.oldCurrency.equals(code) && (scEntry.cutOverTime == Long.MAX_VALUE || System.currentTimeMillis() < scEntry.cutOverTime)) {
                    return index;
                }
                if (!scEntry.newCurrency.equals(code) || System.currentTimeMillis() < scEntry.cutOverTime) continue;
                return index;
            }
            return -1;
        }

        private static int toIndex(int tableEntry) {
            return (tableEntry & 0x1F) - 1;
        }
    }

    private static class OtherCurrencyEntry {
        private final String currencyCode;
        private final int fraction;
        private final int numericCode;

        private OtherCurrencyEntry(String currencyCode, int fraction, int numericCode) {
            this.currencyCode = currencyCode;
            this.fraction = fraction;
            this.numericCode = numericCode;
        }

        private static OtherCurrencyEntry findEntry(String code) {
            int size = otherCurrenciesList.size();
            for (int index = 0; index < size; ++index) {
                OtherCurrencyEntry ocEntry = otherCurrenciesList.get(index);
                if (!ocEntry.currencyCode.equalsIgnoreCase(code)) continue;
                return ocEntry;
            }
            return null;
        }
    }

    private static class CurrencyNameGetter
    implements LocaleServiceProviderPool.LocalizedObjectGetter<CurrencyNameProvider, String> {
        private static final CurrencyNameGetter INSTANCE = new CurrencyNameGetter();

        private CurrencyNameGetter() {
        }

        @Override
        public String getObject(CurrencyNameProvider currencyNameProvider, Locale locale, String key, Object ... params) {
            assert (params.length == 1);
            int type = (Integer)params[0];
            switch (type) {
                case 0: {
                    return currencyNameProvider.getSymbol(key, locale);
                }
                case 1: {
                    return currencyNameProvider.getDisplayName(key, locale);
                }
            }
            assert (false);
            return null;
        }
    }

    private static class CurrencyProperty {
        private final String country;
        private final String currencyCode;
        private final int fraction;
        private final int numericCode;
        private final String date;

        private CurrencyProperty(String country, String currencyCode, int fraction, int numericCode, String date) {
            this.country = country;
            this.currencyCode = currencyCode;
            this.fraction = fraction;
            this.numericCode = numericCode;
            this.date = date;
        }

        private static Optional<CurrencyProperty> getValidEntry(String ctry, String curData, Pattern pattern) {
            CurrencyProperty prop = null;
            if (ctry.length() == 2) {
                prop = CurrencyProperty.parseProperty(ctry, curData, pattern);
                if (prop == null || prop.date == null && curData.chars().map(c -> c == 44 ? 1 : 0).sum() >= 3) {
                    prop = null;
                } else if (prop.fraction > 9) {
                    prop = null;
                } else {
                    try {
                        if (prop.date != null && !CurrencyProperty.isPastCutoverDate(prop.date)) {
                            prop = null;
                        }
                    }
                    catch (ParseException ex) {
                        prop = null;
                    }
                }
            }
            if (prop == null) {
                CurrencyProperty.info("The property entry for " + ctry + " is invalid. Ignored.", null);
            }
            return Optional.ofNullable(prop);
        }

        private static CurrencyProperty parseProperty(String ctry, String curData, Pattern pattern) {
            Matcher m = pattern.matcher(curData);
            if (!m.find()) {
                return null;
            }
            return new CurrencyProperty(ctry, m.group(1), Integer.parseInt(m.group(3)), Integer.parseInt(m.group(2)), m.group(4));
        }

        private static boolean containsInconsistentInstances(List<CurrencyProperty> list) {
            int numCode = list.get((int)0).numericCode;
            int fractionDigit = list.get((int)0).fraction;
            return list.stream().anyMatch(prop -> prop.numericCode != numCode || prop.fraction != fractionDigit);
        }

        private static boolean isPastCutoverDate(String s) throws ParseException {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            format.setLenient(false);
            long time = format.parse(s.trim()).getTime();
            return System.currentTimeMillis() > time;
        }

        private static void info(String message, Throwable t) {
            PlatformLogger logger = PlatformLogger.getLogger("java.util.Currency");
            if (logger.isLoggable(PlatformLogger.Level.INFO)) {
                if (t != null) {
                    logger.info(message, t);
                } else {
                    logger.info(message);
                }
            }
        }
    }
}

