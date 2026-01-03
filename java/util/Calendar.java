/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PermissionCollection;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.time.Instant;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.JapaneseImperialCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.util.BuddhistCalendar;
import sun.util.calendar.ZoneInfo;
import sun.util.locale.provider.CalendarDataUtility;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.TimeZoneNameUtility;
import sun.util.spi.CalendarProvider;

public abstract class Calendar
implements Serializable,
Cloneable,
Comparable<Calendar> {
    public static final int ERA = 0;
    public static final int YEAR = 1;
    public static final int MONTH = 2;
    public static final int WEEK_OF_YEAR = 3;
    public static final int WEEK_OF_MONTH = 4;
    public static final int DATE = 5;
    public static final int DAY_OF_MONTH = 5;
    public static final int DAY_OF_YEAR = 6;
    public static final int DAY_OF_WEEK = 7;
    public static final int DAY_OF_WEEK_IN_MONTH = 8;
    public static final int AM_PM = 9;
    public static final int HOUR = 10;
    public static final int HOUR_OF_DAY = 11;
    public static final int MINUTE = 12;
    public static final int SECOND = 13;
    public static final int MILLISECOND = 14;
    public static final int ZONE_OFFSET = 15;
    public static final int DST_OFFSET = 16;
    public static final int FIELD_COUNT = 17;
    public static final int SUNDAY = 1;
    public static final int MONDAY = 2;
    public static final int TUESDAY = 3;
    public static final int WEDNESDAY = 4;
    public static final int THURSDAY = 5;
    public static final int FRIDAY = 6;
    public static final int SATURDAY = 7;
    public static final int JANUARY = 0;
    public static final int FEBRUARY = 1;
    public static final int MARCH = 2;
    public static final int APRIL = 3;
    public static final int MAY = 4;
    public static final int JUNE = 5;
    public static final int JULY = 6;
    public static final int AUGUST = 7;
    public static final int SEPTEMBER = 8;
    public static final int OCTOBER = 9;
    public static final int NOVEMBER = 10;
    public static final int DECEMBER = 11;
    public static final int UNDECIMBER = 12;
    public static final int AM = 0;
    public static final int PM = 1;
    public static final int ALL_STYLES = 0;
    static final int STANDALONE_MASK = 32768;
    public static final int SHORT = 1;
    public static final int LONG = 2;
    public static final int NARROW_FORMAT = 4;
    public static final int NARROW_STANDALONE = 32772;
    public static final int SHORT_FORMAT = 1;
    public static final int LONG_FORMAT = 2;
    public static final int SHORT_STANDALONE = 32769;
    public static final int LONG_STANDALONE = 32770;
    protected int[] fields = new int[17];
    protected boolean[] isSet = new boolean[17];
    private transient int[] stamp = new int[17];
    protected long time;
    protected boolean isTimeSet;
    protected boolean areFieldsSet;
    transient boolean areAllFieldsSet;
    private boolean lenient = true;
    private TimeZone zone;
    private transient boolean sharedZone = false;
    private int firstDayOfWeek;
    private int minimalDaysInFirstWeek;
    private static final ConcurrentMap<Locale, int[]> cachedLocaleData = new ConcurrentHashMap<Locale, int[]>(3);
    private static final int UNSET = 0;
    private static final int COMPUTED = 1;
    private static final int MINIMUM_USER_STAMP = 2;
    static final int ALL_FIELDS = 131071;
    private int nextStamp = 2;
    static final int currentSerialVersion = 1;
    private int serialVersionOnStream = 1;
    static final long serialVersionUID = -1807547505821590642L;
    static final int ERA_MASK = 1;
    static final int YEAR_MASK = 2;
    static final int MONTH_MASK = 4;
    static final int WEEK_OF_YEAR_MASK = 8;
    static final int WEEK_OF_MONTH_MASK = 16;
    static final int DAY_OF_MONTH_MASK = 32;
    static final int DAY_OF_YEAR_MASK = 64;
    static final int DAY_OF_WEEK_MASK = 128;
    static final int DAY_OF_WEEK_IN_MONTH_MASK = 256;
    static final int AM_PM_MASK = 512;
    static final int HOUR_MASK = 1024;
    static final int HOUR_OF_DAY_MASK = 2048;
    static final int MINUTE_MASK = 4096;
    static final int SECOND_MASK = 8192;
    static final int MILLISECOND_MASK = 16384;
    static final int ZONE_OFFSET_MASK = 32768;
    static final int DST_OFFSET_MASK = 65536;
    private static final String[] FIELD_NAME = new String[]{"ERA", "YEAR", "MONTH", "WEEK_OF_YEAR", "WEEK_OF_MONTH", "DAY_OF_MONTH", "DAY_OF_YEAR", "DAY_OF_WEEK", "DAY_OF_WEEK_IN_MONTH", "AM_PM", "HOUR", "HOUR_OF_DAY", "MINUTE", "SECOND", "MILLISECOND", "ZONE_OFFSET", "DST_OFFSET"};

    protected Calendar() {
        this(TimeZone.getDefaultRef(), Locale.getDefault(Locale.Category.FORMAT));
        this.sharedZone = true;
    }

    protected Calendar(TimeZone zone, Locale aLocale) {
        this.zone = zone;
        this.setWeekCountData(aLocale);
    }

    public static Calendar getInstance() {
        Locale aLocale = Locale.getDefault(Locale.Category.FORMAT);
        return Calendar.createCalendar(Calendar.defaultTimeZone(aLocale), aLocale);
    }

    public static Calendar getInstance(TimeZone zone) {
        return Calendar.createCalendar(zone, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static Calendar getInstance(Locale aLocale) {
        return Calendar.createCalendar(Calendar.defaultTimeZone(aLocale), aLocale);
    }

    public static Calendar getInstance(TimeZone zone, Locale aLocale) {
        return Calendar.createCalendar(zone, aLocale);
    }

    private static TimeZone defaultTimeZone(Locale l) {
        TimeZone defaultTZ = TimeZone.getDefault();
        String shortTZID = l.getUnicodeLocaleType("tz");
        return shortTZID != null ? TimeZoneNameUtility.convertLDMLShortID(shortTZID).map(TimeZone::getTimeZone).orElse(defaultTZ) : defaultTZ;
    }

    private static Calendar createCalendar(TimeZone zone, Locale aLocale) {
        String caltype;
        CalendarProvider provider = LocaleProviderAdapter.getAdapter(CalendarProvider.class, aLocale).getCalendarProvider();
        if (provider != null) {
            try {
                return provider.getInstance(zone, aLocale);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        Calendar cal = null;
        if (aLocale.hasExtensions() && (caltype = aLocale.getUnicodeLocaleType("ca")) != null) {
            switch (caltype) {
                case "buddhist": {
                    Calendar calendar = new BuddhistCalendar(zone, aLocale);
                    break;
                }
                case "japanese": {
                    Calendar calendar = new JapaneseImperialCalendar(zone, aLocale);
                    break;
                }
                case "gregory": {
                    Calendar calendar = new GregorianCalendar(zone, aLocale);
                    break;
                }
                default: {
                    Calendar calendar = cal = null;
                }
            }
        }
        if (cal == null) {
            cal = aLocale.getLanguage() == "th" && aLocale.getCountry() == "TH" ? new BuddhistCalendar(zone, aLocale) : (aLocale.getVariant() == "JP" && aLocale.getLanguage() == "ja" && aLocale.getCountry() == "JP" ? new JapaneseImperialCalendar(zone, aLocale) : new GregorianCalendar(zone, aLocale));
        }
        return cal;
    }

    public static synchronized Locale[] getAvailableLocales() {
        return DateFormat.getAvailableLocales();
    }

    protected abstract void computeTime();

    protected abstract void computeFields();

    public final Date getTime() {
        return new Date(this.getTimeInMillis());
    }

    public final void setTime(Date date) {
        Objects.requireNonNull(date, "date must not be null");
        this.setTimeInMillis(date.getTime());
    }

    public long getTimeInMillis() {
        if (!this.isTimeSet) {
            this.updateTime();
        }
        return this.time;
    }

    public void setTimeInMillis(long millis) {
        if (this.time == millis && this.isTimeSet && this.areFieldsSet && this.areAllFieldsSet && this.zone instanceof ZoneInfo && !((ZoneInfo)this.zone).isDirty()) {
            return;
        }
        this.time = millis;
        this.isTimeSet = true;
        this.areFieldsSet = false;
        this.computeFields();
        this.areFieldsSet = true;
        this.areAllFieldsSet = true;
    }

    public int get(int field) {
        this.complete();
        return this.internalGet(field);
    }

    protected final int internalGet(int field) {
        return this.fields[field];
    }

    final void internalSet(int field, int value) {
        this.fields[field] = value;
    }

    public void set(int field, int value) {
        if (this.areFieldsSet && !this.areAllFieldsSet) {
            this.computeFields();
        }
        this.internalSet(field, value);
        this.isTimeSet = false;
        this.areFieldsSet = false;
        this.isSet[field] = true;
        ++this.nextStamp;
        if (this.nextStamp == Integer.MAX_VALUE) {
            this.adjustStamp();
        }
    }

    public final void set(int year, int month, int date) {
        this.set(1, year);
        this.set(2, month);
        this.set(5, date);
    }

    public final void set(int year, int month, int date, int hourOfDay, int minute) {
        this.set(1, year);
        this.set(2, month);
        this.set(5, date);
        this.set(11, hourOfDay);
        this.set(12, minute);
    }

    public final void set(int year, int month, int date, int hourOfDay, int minute, int second) {
        this.set(1, year);
        this.set(2, month);
        this.set(5, date);
        this.set(11, hourOfDay);
        this.set(12, minute);
        this.set(13, second);
    }

    public final void clear() {
        int i = 0;
        while (i < this.fields.length) {
            this.fields[i] = 0;
            this.stamp[i] = 0;
            this.isSet[i++] = false;
        }
        this.areFieldsSet = false;
        this.areAllFieldsSet = false;
        this.isTimeSet = false;
    }

    public final void clear(int field) {
        this.fields[field] = 0;
        this.stamp[field] = 0;
        this.isSet[field] = false;
        this.areFieldsSet = false;
        this.areAllFieldsSet = false;
        this.isTimeSet = false;
    }

    public final boolean isSet(int field) {
        return this.stamp[field] != 0;
    }

    public String getDisplayName(int field, int style, Locale locale) {
        if (!this.checkDisplayNameParams(field, style, 1, 4, locale, 645)) {
            return null;
        }
        String calendarType = this.getCalendarType();
        int fieldValue = this.get(field);
        if (this.isStandaloneStyle(style) || this.isNarrowFormatStyle(style) || field == 0 && (style & 1) == 1) {
            String val = CalendarDataUtility.retrieveFieldValueName(calendarType, field, fieldValue, style, locale);
            if (val == null) {
                if (this.isNarrowFormatStyle(style)) {
                    val = CalendarDataUtility.retrieveFieldValueName(calendarType, field, fieldValue, this.toStandaloneStyle(style), locale);
                } else if (this.isStandaloneStyle(style)) {
                    val = CalendarDataUtility.retrieveFieldValueName(calendarType, field, fieldValue, this.getBaseStyle(style), locale);
                }
            }
            return val;
        }
        DateFormatSymbols symbols = DateFormatSymbols.getInstance(locale);
        String[] strings = this.getFieldStrings(field, style, symbols);
        if (strings != null && fieldValue < strings.length) {
            return strings[fieldValue];
        }
        return null;
    }

    public Map<String, Integer> getDisplayNames(int field, int style, Locale locale) {
        if (!this.checkDisplayNameParams(field, style, 0, 4, locale, 645)) {
            return null;
        }
        String calendarType = this.getCalendarType();
        if (style == 0 || this.isStandaloneStyle(style) || this.isNarrowFormatStyle(style) || field == 0 && (style & 1) == 1) {
            Map<String, Integer> map = CalendarDataUtility.retrieveFieldValueNames(calendarType, field, style, locale);
            if (map == null) {
                if (this.isNarrowFormatStyle(style)) {
                    map = CalendarDataUtility.retrieveFieldValueNames(calendarType, field, this.toStandaloneStyle(style), locale);
                } else if (style != 0) {
                    map = CalendarDataUtility.retrieveFieldValueNames(calendarType, field, this.getBaseStyle(style), locale);
                }
            }
            return map;
        }
        return this.getDisplayNamesImpl(field, style, locale);
    }

    private Map<String, Integer> getDisplayNamesImpl(int field, int style, Locale locale) {
        DateFormatSymbols symbols = DateFormatSymbols.getInstance(locale);
        String[] strings = this.getFieldStrings(field, style, symbols);
        if (strings != null) {
            HashMap<String, Integer> names = new HashMap<String, Integer>();
            for (int i = 0; i < strings.length; ++i) {
                if (strings[i].isEmpty()) continue;
                names.put(strings[i], i);
            }
            return names;
        }
        return null;
    }

    boolean checkDisplayNameParams(int field, int style, int minStyle, int maxStyle, Locale locale, int fieldMask) {
        int baseStyle = this.getBaseStyle(style);
        if (field < 0 || field >= this.fields.length || baseStyle < minStyle || baseStyle > maxStyle || baseStyle == 3) {
            throw new IllegalArgumentException();
        }
        if (locale == null) {
            throw new NullPointerException();
        }
        return Calendar.isFieldSet(fieldMask, field);
    }

    private String[] getFieldStrings(int field, int style, DateFormatSymbols symbols) {
        int baseStyle = this.getBaseStyle(style);
        if (baseStyle == 4) {
            return null;
        }
        return switch (field) {
            case 0 -> symbols.getEras();
            case 2 -> {
                if (baseStyle == 2) {
                    yield symbols.getMonths();
                }
                yield symbols.getShortMonths();
            }
            case 7 -> {
                if (baseStyle == 2) {
                    yield symbols.getWeekdays();
                }
                yield symbols.getShortWeekdays();
            }
            case 9 -> symbols.getAmPmStrings();
            default -> null;
        };
    }

    protected void complete() {
        if (!this.isTimeSet) {
            this.updateTime();
        }
        if (!this.areFieldsSet || !this.areAllFieldsSet) {
            this.computeFields();
            this.areFieldsSet = true;
            this.areAllFieldsSet = true;
        }
    }

    final boolean isExternallySet(int field) {
        return this.stamp[field] >= 2;
    }

    final int getSetStateFields() {
        int mask = 0;
        for (int i = 0; i < this.fields.length; ++i) {
            if (this.stamp[i] == 0) continue;
            mask |= 1 << i;
        }
        return mask;
    }

    final void setFieldsComputed(int fieldMask) {
        if (fieldMask == 131071) {
            for (int i = 0; i < this.fields.length; ++i) {
                this.stamp[i] = 1;
                this.isSet[i] = true;
            }
            this.areAllFieldsSet = true;
            this.areFieldsSet = true;
        } else {
            for (int i = 0; i < this.fields.length; ++i) {
                if ((fieldMask & 1) == 1) {
                    this.stamp[i] = 1;
                    this.isSet[i] = true;
                } else if (this.areAllFieldsSet && !this.isSet[i]) {
                    this.areAllFieldsSet = false;
                }
                fieldMask >>>= 1;
            }
        }
    }

    final void setFieldsNormalized(int fieldMask) {
        if (fieldMask != 131071) {
            for (int i = 0; i < this.fields.length; ++i) {
                if ((fieldMask & 1) == 0) {
                    this.fields[i] = 0;
                    this.stamp[i] = 0;
                    this.isSet[i] = false;
                }
                fieldMask >>= 1;
            }
        }
        this.areFieldsSet = true;
        this.areAllFieldsSet = false;
    }

    final boolean isPartiallyNormalized() {
        return this.areFieldsSet && !this.areAllFieldsSet;
    }

    final boolean isFullyNormalized() {
        return this.areFieldsSet && this.areAllFieldsSet;
    }

    final void setUnnormalized() {
        this.areAllFieldsSet = false;
        this.areFieldsSet = false;
    }

    static boolean isFieldSet(int fieldMask, int field) {
        return (fieldMask & 1 << field) != 0;
    }

    final int selectFields() {
        int fieldMask = 2;
        if (this.stamp[0] != 0) {
            fieldMask |= 1;
        }
        int dowStamp = this.stamp[7];
        int monthStamp = this.stamp[2];
        int domStamp = this.stamp[5];
        int womStamp = Calendar.aggregateStamp(this.stamp[4], dowStamp);
        int dowimStamp = Calendar.aggregateStamp(this.stamp[8], dowStamp);
        int doyStamp = this.stamp[6];
        int woyStamp = Calendar.aggregateStamp(this.stamp[3], dowStamp);
        int bestStamp = domStamp;
        if (womStamp > bestStamp) {
            bestStamp = womStamp;
        }
        if (dowimStamp > bestStamp) {
            bestStamp = dowimStamp;
        }
        if (doyStamp > bestStamp) {
            bestStamp = doyStamp;
        }
        if (woyStamp > bestStamp) {
            bestStamp = woyStamp;
        }
        if (bestStamp == 0) {
            womStamp = this.stamp[4];
            dowimStamp = Math.max(this.stamp[8], dowStamp);
            woyStamp = this.stamp[3];
            bestStamp = Math.max(Math.max(womStamp, dowimStamp), woyStamp);
            if (bestStamp == 0) {
                bestStamp = domStamp = monthStamp;
            }
        }
        if (bestStamp == domStamp || bestStamp == womStamp && this.stamp[4] >= this.stamp[3] || bestStamp == dowimStamp && this.stamp[8] >= this.stamp[3]) {
            fieldMask |= 4;
            if (bestStamp == domStamp) {
                fieldMask |= 0x20;
            } else {
                assert (bestStamp == womStamp || bestStamp == dowimStamp);
                if (dowStamp != 0) {
                    fieldMask |= 0x80;
                }
                if (womStamp == dowimStamp) {
                    fieldMask = this.stamp[4] >= this.stamp[8] ? (fieldMask |= 0x10) : (fieldMask |= 0x100);
                } else if (bestStamp == womStamp) {
                    fieldMask |= 0x10;
                } else {
                    assert (bestStamp == dowimStamp);
                    if (this.stamp[8] != 0) {
                        fieldMask |= 0x100;
                    }
                }
            }
        } else {
            assert (bestStamp == doyStamp || bestStamp == woyStamp || bestStamp == 0);
            if (bestStamp == doyStamp) {
                fieldMask |= 0x40;
            } else {
                assert (bestStamp == woyStamp);
                if (dowStamp != 0) {
                    fieldMask |= 0x80;
                }
                fieldMask |= 8;
            }
        }
        int hourOfDayStamp = this.stamp[11];
        int hourStamp = Calendar.aggregateStamp(this.stamp[10], this.stamp[9]);
        int n = bestStamp = hourStamp > hourOfDayStamp ? hourStamp : hourOfDayStamp;
        if (bestStamp == 0) {
            bestStamp = Math.max(this.stamp[10], this.stamp[9]);
        }
        if (bestStamp != 0) {
            if (bestStamp == hourOfDayStamp) {
                fieldMask |= 0x800;
            } else {
                fieldMask |= 0x400;
                if (this.stamp[9] != 0) {
                    fieldMask |= 0x200;
                }
            }
        }
        if (this.stamp[12] != 0) {
            fieldMask |= 0x1000;
        }
        if (this.stamp[13] != 0) {
            fieldMask |= 0x2000;
        }
        if (this.stamp[14] != 0) {
            fieldMask |= 0x4000;
        }
        if (this.stamp[15] >= 2) {
            fieldMask |= 0x8000;
        }
        if (this.stamp[16] >= 2) {
            fieldMask |= 0x10000;
        }
        return fieldMask;
    }

    int getBaseStyle(int style) {
        return style & 0xFFFF7FFF;
    }

    private int toStandaloneStyle(int style) {
        return style | 0x8000;
    }

    private boolean isStandaloneStyle(int style) {
        return (style & 0x8000) != 0;
    }

    private boolean isNarrowStyle(int style) {
        return style == 4 || style == 32772;
    }

    private boolean isNarrowFormatStyle(int style) {
        return style == 4;
    }

    private static int aggregateStamp(int stamp_a, int stamp_b) {
        if (stamp_a == 0 || stamp_b == 0) {
            return 0;
        }
        return Math.max(stamp_a, stamp_b);
    }

    public static Set<String> getAvailableCalendarTypes() {
        return AvailableCalendarTypes.SET;
    }

    public String getCalendarType() {
        return this.getClass().getName();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        try {
            Calendar that = (Calendar)obj;
            return this.compareTo(Calendar.getMillisOf(that)) == 0 && this.lenient == that.lenient && this.firstDayOfWeek == that.firstDayOfWeek && this.minimalDaysInFirstWeek == that.minimalDaysInFirstWeek && (this.zone instanceof ZoneInfo ? this.zone.equals(that.zone) : this.zone.equals(that.getTimeZone()));
        }
        catch (Exception exception) {
            return false;
        }
    }

    public int hashCode() {
        int otheritems = (this.lenient ? 1 : 0) | this.firstDayOfWeek << 1 | this.minimalDaysInFirstWeek << 4 | this.zone.hashCode() << 7;
        long t = Calendar.getMillisOf(this);
        return (int)t ^ (int)(t >> 32) ^ otheritems;
    }

    public boolean before(Object when) {
        return when instanceof Calendar && this.compareTo((Calendar)when) < 0;
    }

    public boolean after(Object when) {
        return when instanceof Calendar && this.compareTo((Calendar)when) > 0;
    }

    @Override
    public int compareTo(Calendar anotherCalendar) {
        return this.compareTo(Calendar.getMillisOf(anotherCalendar));
    }

    public abstract void add(int var1, int var2);

    public abstract void roll(int var1, boolean var2);

    public void roll(int field, int amount) {
        while (amount > 0) {
            this.roll(field, true);
            --amount;
        }
        while (amount < 0) {
            this.roll(field, false);
            ++amount;
        }
    }

    public void setTimeZone(TimeZone value) {
        this.zone = value;
        this.sharedZone = false;
        this.areFieldsSet = false;
        this.areAllFieldsSet = false;
    }

    public TimeZone getTimeZone() {
        if (this.sharedZone) {
            this.zone = (TimeZone)this.zone.clone();
            this.sharedZone = false;
        }
        return this.zone;
    }

    TimeZone getZone() {
        return this.zone;
    }

    void setZoneShared(boolean shared) {
        this.sharedZone = shared;
    }

    public void setLenient(boolean lenient) {
        this.lenient = lenient;
    }

    public boolean isLenient() {
        return this.lenient;
    }

    public void setFirstDayOfWeek(int value) {
        if (this.firstDayOfWeek == value) {
            return;
        }
        this.firstDayOfWeek = value;
        this.invalidateWeekFields();
    }

    public int getFirstDayOfWeek() {
        return this.firstDayOfWeek;
    }

    public void setMinimalDaysInFirstWeek(int value) {
        if (this.minimalDaysInFirstWeek == value) {
            return;
        }
        this.minimalDaysInFirstWeek = value;
        this.invalidateWeekFields();
    }

    public int getMinimalDaysInFirstWeek() {
        return this.minimalDaysInFirstWeek;
    }

    public boolean isWeekDateSupported() {
        return false;
    }

    public int getWeekYear() {
        throw new UnsupportedOperationException();
    }

    public void setWeekDate(int weekYear, int weekOfYear, int dayOfWeek) {
        throw new UnsupportedOperationException();
    }

    public int getWeeksInWeekYear() {
        throw new UnsupportedOperationException();
    }

    public abstract int getMinimum(int var1);

    public abstract int getMaximum(int var1);

    public abstract int getGreatestMinimum(int var1);

    public abstract int getLeastMaximum(int var1);

    public int getActualMinimum(int field) {
        int endValue;
        int fieldValue = this.getGreatestMinimum(field);
        if (fieldValue == (endValue = this.getMinimum(field))) {
            return fieldValue;
        }
        Calendar work = (Calendar)this.clone();
        work.setLenient(true);
        int result = fieldValue;
        do {
            work.set(field, fieldValue);
            if (work.get(field) != fieldValue) break;
            result = fieldValue--;
        } while (fieldValue >= endValue);
        return result;
    }

    public int getActualMaximum(int field) {
        int endValue;
        int fieldValue = this.getLeastMaximum(field);
        if (fieldValue == (endValue = this.getMaximum(field))) {
            return fieldValue;
        }
        Calendar work = (Calendar)this.clone();
        work.setLenient(true);
        if (field == 3 || field == 4) {
            work.set(7, this.firstDayOfWeek);
        }
        int result = fieldValue;
        do {
            work.set(field, fieldValue);
            if (work.get(field) != fieldValue) break;
            result = fieldValue++;
        } while (fieldValue <= endValue);
        return result;
    }

    public Object clone() {
        try {
            Calendar other = (Calendar)super.clone();
            other.fields = new int[17];
            other.isSet = new boolean[17];
            other.stamp = new int[17];
            for (int i = 0; i < 17; ++i) {
                other.fields[i] = this.fields[i];
                other.stamp[i] = this.stamp[i];
                other.isSet[i] = this.isSet[i];
            }
            if (!this.sharedZone) {
                other.zone = (TimeZone)this.zone.clone();
            }
            return other;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    static String getFieldName(int field) {
        return FIELD_NAME[field];
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(800);
        buffer.append(this.getClass().getName()).append('[');
        Calendar.appendValue(buffer, "time", this.isTimeSet, this.time);
        buffer.append(",areFieldsSet=").append(this.areFieldsSet);
        buffer.append(",areAllFieldsSet=").append(this.areAllFieldsSet);
        buffer.append(",lenient=").append(this.lenient);
        buffer.append(",zone=").append(this.zone);
        Calendar.appendValue(buffer, ",firstDayOfWeek", true, this.firstDayOfWeek);
        Calendar.appendValue(buffer, ",minimalDaysInFirstWeek", true, this.minimalDaysInFirstWeek);
        for (int i = 0; i < 17; ++i) {
            buffer.append(',');
            Calendar.appendValue(buffer, FIELD_NAME[i], this.isSet(i), this.fields[i]);
        }
        buffer.append(']');
        return buffer.toString();
    }

    private static void appendValue(StringBuilder sb, String item, boolean valid, long value) {
        sb.append(item).append('=');
        if (valid) {
            sb.append(value);
        } else {
            sb.append('?');
        }
    }

    private void setWeekCountData(Locale desiredLocale) {
        int[] data = (int[])cachedLocaleData.get(desiredLocale);
        if (data == null) {
            data = new int[]{CalendarDataUtility.retrieveFirstDayOfWeek(desiredLocale), CalendarDataUtility.retrieveMinimalDaysInFirstWeek(desiredLocale)};
            cachedLocaleData.putIfAbsent(desiredLocale, data);
        }
        this.firstDayOfWeek = data[0];
        this.minimalDaysInFirstWeek = data[1];
    }

    private void updateTime() {
        this.computeTime();
        this.isTimeSet = true;
    }

    @Override
    private int compareTo(long t) {
        return Long.compare(Calendar.getMillisOf(this), t);
    }

    private static long getMillisOf(Calendar calendar) {
        if (calendar.isTimeSet) {
            return calendar.time;
        }
        Calendar cal = (Calendar)calendar.clone();
        cal.setLenient(true);
        return cal.getTimeInMillis();
    }

    private void adjustStamp() {
        int min;
        int max = 2;
        int newStamp = 2;
        do {
            min = Integer.MAX_VALUE;
            for (int v : this.stamp) {
                if (v >= newStamp && min > v) {
                    min = v;
                }
                if (max >= v) continue;
                max = v;
            }
            if (max != min && min == Integer.MAX_VALUE) break;
            for (int i = 0; i < this.stamp.length; ++i) {
                if (this.stamp[i] != min) continue;
                this.stamp[i] = newStamp;
            }
            ++newStamp;
        } while (min != max);
        this.nextStamp = newStamp;
    }

    private void invalidateWeekFields() {
        int weekOfYear;
        int weekOfMonth;
        if (this.stamp[4] != 1 && this.stamp[3] != 1) {
            return;
        }
        Calendar cal = (Calendar)this.clone();
        cal.setLenient(true);
        cal.clear(4);
        cal.clear(3);
        if (this.stamp[4] == 1 && this.fields[4] != (weekOfMonth = cal.get(4))) {
            this.fields[4] = weekOfMonth;
        }
        if (this.stamp[3] == 1 && this.fields[3] != (weekOfYear = cal.get(3))) {
            this.fields[3] = weekOfYear;
        }
    }

    private synchronized void writeObject(ObjectOutputStream stream) throws IOException {
        if (!this.isTimeSet) {
            try {
                this.updateTime();
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        TimeZone savedZone = null;
        if (this.zone instanceof ZoneInfo) {
            SimpleTimeZone stz = ((ZoneInfo)this.zone).getLastRuleInstance();
            if (stz == null) {
                stz = new SimpleTimeZone(this.zone.getRawOffset(), this.zone.getID());
            }
            savedZone = this.zone;
            this.zone = stz;
        }
        stream.defaultWriteObject();
        stream.writeObject(savedZone);
        if (savedZone != null) {
            this.zone = savedZone;
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        String id;
        TimeZone tz;
        ZoneInfo zi;
        block13: {
            final ObjectInputStream input = stream;
            input.defaultReadObject();
            this.stamp = new int[17];
            if (this.serialVersionOnStream >= 2) {
                this.isTimeSet = true;
                if (this.fields == null) {
                    this.fields = new int[17];
                }
                if (this.isSet == null) {
                    this.isSet = new boolean[17];
                }
            } else if (this.serialVersionOnStream >= 0) {
                for (int i = 0; i < 17; ++i) {
                    this.stamp[i] = this.isSet[i] ? 1 : 0;
                }
            }
            this.serialVersionOnStream = 1;
            zi = null;
            try {
                zi = AccessController.doPrivileged(new PrivilegedExceptionAction<ZoneInfo>(this){

                    @Override
                    public ZoneInfo run() throws Exception {
                        return (ZoneInfo)input.readObject();
                    }
                }, CalendarAccessControlContext.INSTANCE);
            }
            catch (PrivilegedActionException pae) {
                Exception e = pae.getException();
                if (e instanceof OptionalDataException) break block13;
                if (e instanceof RuntimeException) {
                    throw (RuntimeException)e;
                }
                if (e instanceof IOException) {
                    throw (IOException)e;
                }
                if (e instanceof ClassNotFoundException) {
                    throw (ClassNotFoundException)e;
                }
                throw new RuntimeException(e);
            }
        }
        if (zi != null) {
            this.zone = zi;
        }
        if (this.zone instanceof SimpleTimeZone && (tz = TimeZone.getTimeZone(id = this.zone.getID())) != null && tz.hasSameRules(this.zone) && tz.getID().equals(id)) {
            this.zone = tz;
        }
    }

    public final Instant toInstant() {
        return Instant.ofEpochMilli(this.getTimeInMillis());
    }

    private static class AvailableCalendarTypes {
        private static final Set<String> SET = Set.of("gregory", "buddhist", "japanese");

        private AvailableCalendarTypes() {
        }
    }

    private static class CalendarAccessControlContext {
        private static final AccessControlContext INSTANCE;

        private CalendarAccessControlContext() {
        }

        static {
            RuntimePermission perm = new RuntimePermission("accessClassInPackage.sun.util.calendar");
            PermissionCollection perms = perm.newPermissionCollection();
            perms.add(perm);
            INSTANCE = new AccessControlContext(new ProtectionDomain[]{new ProtectionDomain(null, perms)});
        }
    }

    public static class Builder {
        private static final int NFIELDS = 18;
        private static final int WEEK_YEAR = 17;
        private long instant;
        private int[] fields;
        private int nextStamp;
        private int maxFieldIndex;
        private String type;
        private TimeZone zone;
        private boolean lenient = true;
        private Locale locale;
        private int firstDayOfWeek;
        private int minimalDaysInFirstWeek;

        public Builder setInstant(long instant) {
            if (this.fields != null) {
                throw new IllegalStateException();
            }
            this.instant = instant;
            this.nextStamp = 1;
            return this;
        }

        public Builder setInstant(Date instant) {
            return this.setInstant(instant.getTime());
        }

        public Builder set(int field, int value) {
            if (field < 0 || field >= 17) {
                throw new IllegalArgumentException("field is invalid");
            }
            if (this.isInstantSet()) {
                throw new IllegalStateException("instant has been set");
            }
            this.allocateFields();
            this.internalSet(field, value);
            return this;
        }

        public Builder setFields(int ... fieldValuePairs) {
            int len = fieldValuePairs.length;
            if (len % 2 != 0) {
                throw new IllegalArgumentException();
            }
            if (this.isInstantSet()) {
                throw new IllegalStateException("instant has been set");
            }
            if (this.nextStamp + len / 2 < 0) {
                throw new IllegalStateException("stamp counter overflow");
            }
            this.allocateFields();
            int i = 0;
            while (i < len) {
                int field;
                if ((field = fieldValuePairs[i++]) < 0 || field >= 17) {
                    throw new IllegalArgumentException("field is invalid");
                }
                this.internalSet(field, fieldValuePairs[i++]);
            }
            return this;
        }

        public Builder setDate(int year, int month, int dayOfMonth) {
            return this.setFields(1, year, 2, month, 5, dayOfMonth);
        }

        public Builder setTimeOfDay(int hourOfDay, int minute, int second) {
            return this.setTimeOfDay(hourOfDay, minute, second, 0);
        }

        public Builder setTimeOfDay(int hourOfDay, int minute, int second, int millis) {
            return this.setFields(11, hourOfDay, 12, minute, 13, second, 14, millis);
        }

        public Builder setWeekDate(int weekYear, int weekOfYear, int dayOfWeek) {
            this.allocateFields();
            this.internalSet(17, weekYear);
            this.internalSet(3, weekOfYear);
            this.internalSet(7, dayOfWeek);
            return this;
        }

        public Builder setTimeZone(TimeZone zone) {
            if (zone == null) {
                throw new NullPointerException();
            }
            this.zone = zone;
            return this;
        }

        public Builder setLenient(boolean lenient) {
            this.lenient = lenient;
            return this;
        }

        public Builder setCalendarType(String type) {
            if (type.equals("gregorian")) {
                type = "gregory";
            }
            if (!Calendar.getAvailableCalendarTypes().contains(type) && !type.equals("iso8601")) {
                throw new IllegalArgumentException("unknown calendar type: " + type);
            }
            if (this.type == null) {
                this.type = type;
            } else if (!this.type.equals(type)) {
                throw new IllegalStateException("calendar type override");
            }
            return this;
        }

        public Builder setLocale(Locale locale) {
            if (locale == null) {
                throw new NullPointerException();
            }
            this.locale = locale;
            return this;
        }

        public Builder setWeekDefinition(int firstDayOfWeek, int minimalDaysInFirstWeek) {
            if (!this.isValidWeekParameter(firstDayOfWeek) || !this.isValidWeekParameter(minimalDaysInFirstWeek)) {
                throw new IllegalArgumentException();
            }
            this.firstDayOfWeek = firstDayOfWeek;
            this.minimalDaysInFirstWeek = minimalDaysInFirstWeek;
            return this;
        }

        public Calendar build() {
            if (this.locale == null) {
                this.locale = Locale.getDefault();
            }
            if (this.zone == null) {
                this.zone = Calendar.defaultTimeZone(this.locale);
            }
            if (this.type == null) {
                this.type = this.locale.getUnicodeLocaleType("ca");
            }
            if (this.type == null) {
                this.type = this.locale.getCountry() == "TH" && this.locale.getLanguage() == "th" ? "buddhist" : "gregory";
            }
            Calendar cal = switch (this.type) {
                case "gregory" -> new GregorianCalendar(this.zone, this.locale, true);
                case "iso8601" -> {
                    GregorianCalendar gcal = new GregorianCalendar(this.zone, this.locale, true);
                    gcal.setGregorianChange(new Date(Long.MIN_VALUE));
                    this.setWeekDefinition(2, 4);
                    yield gcal;
                }
                case "buddhist" -> {
                    BuddhistCalendar buddhistCalendar = new BuddhistCalendar(this.zone, this.locale);
                    buddhistCalendar.clear();
                    yield buddhistCalendar;
                }
                case "japanese" -> new JapaneseImperialCalendar(this.zone, this.locale, true);
                default -> throw new IllegalArgumentException("unknown calendar type: " + this.type);
            };
            cal.setLenient(this.lenient);
            if (this.firstDayOfWeek != 0) {
                cal.setFirstDayOfWeek(this.firstDayOfWeek);
                cal.setMinimalDaysInFirstWeek(this.minimalDaysInFirstWeek);
            }
            if (this.isInstantSet()) {
                cal.setTimeInMillis(this.instant);
                cal.complete();
                return cal;
            }
            if (this.fields != null) {
                boolean weekDate;
                boolean bl = weekDate = this.isSet(17) && this.fields[17] > this.fields[1];
                if (weekDate && !cal.isWeekDateSupported()) {
                    throw new IllegalArgumentException("week date is unsupported by " + this.type);
                }
                block12: for (int stamp = 2; stamp < this.nextStamp; ++stamp) {
                    for (int index = 0; index <= this.maxFieldIndex; ++index) {
                        if (this.fields[index] != stamp) continue;
                        cal.set(index, this.fields[18 + index]);
                        continue block12;
                    }
                }
                if (weekDate) {
                    int weekOfYear = this.isSet(3) ? this.fields[21] : 1;
                    int dayOfWeek = this.isSet(7) ? this.fields[25] : cal.getFirstDayOfWeek();
                    cal.setWeekDate(this.fields[35], weekOfYear, dayOfWeek);
                }
                cal.complete();
            }
            return cal;
        }

        private void allocateFields() {
            if (this.fields == null) {
                this.fields = new int[36];
                this.nextStamp = 2;
                this.maxFieldIndex = -1;
            }
        }

        private void internalSet(int field, int value) {
            ++this.nextStamp;
            if (this.nextStamp < 0) {
                throw new IllegalStateException("stamp counter overflow");
            }
            this.fields[18 + field] = value;
            if (field > this.maxFieldIndex && field < 17) {
                this.maxFieldIndex = field;
            }
        }

        private boolean isInstantSet() {
            return this.nextStamp == 1;
        }

        private boolean isSet(int index) {
            return this.fields != null && this.fields[index] > 0;
        }

        private boolean isValidWeekParameter(int value) {
            return value > 0 && value <= 7;
        }
    }
}

