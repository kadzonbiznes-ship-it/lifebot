/*
 * Decompiled with CFR 0.152.
 */
package java.text;

import java.io.InvalidObjectException;
import java.text.DontCareFieldPosition;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.spi.DateFormatProvider;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleServiceProviderPool;

public abstract class DateFormat
extends Format {
    protected Calendar calendar;
    protected NumberFormat numberFormat;
    public static final int ERA_FIELD = 0;
    public static final int YEAR_FIELD = 1;
    public static final int MONTH_FIELD = 2;
    public static final int DATE_FIELD = 3;
    public static final int HOUR_OF_DAY1_FIELD = 4;
    public static final int HOUR_OF_DAY0_FIELD = 5;
    public static final int MINUTE_FIELD = 6;
    public static final int SECOND_FIELD = 7;
    public static final int MILLISECOND_FIELD = 8;
    public static final int DAY_OF_WEEK_FIELD = 9;
    public static final int DAY_OF_YEAR_FIELD = 10;
    public static final int DAY_OF_WEEK_IN_MONTH_FIELD = 11;
    public static final int WEEK_OF_YEAR_FIELD = 12;
    public static final int WEEK_OF_MONTH_FIELD = 13;
    public static final int AM_PM_FIELD = 14;
    public static final int HOUR1_FIELD = 15;
    public static final int HOUR0_FIELD = 16;
    public static final int TIMEZONE_FIELD = 17;
    private static final long serialVersionUID = 7218322306649953788L;
    public static final int FULL = 0;
    public static final int LONG = 1;
    public static final int MEDIUM = 2;
    public static final int SHORT = 3;
    public static final int DEFAULT = 2;

    @Override
    public final StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        if (obj instanceof Date) {
            return this.format((Date)obj, toAppendTo, fieldPosition);
        }
        if (obj instanceof Number) {
            return this.format(new Date(((Number)obj).longValue()), toAppendTo, fieldPosition);
        }
        throw new IllegalArgumentException("Cannot format given Object as a Date");
    }

    public abstract StringBuffer format(Date var1, StringBuffer var2, FieldPosition var3);

    public final String format(Date date) {
        return this.format(date, new StringBuffer(), DontCareFieldPosition.INSTANCE).toString();
    }

    public Date parse(String source) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Date result = this.parse(source, pos);
        if (pos.index == 0) {
            throw new ParseException("Unparseable date: \"" + source + "\"", pos.errorIndex);
        }
        return result;
    }

    public abstract Date parse(String var1, ParsePosition var2);

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        return this.parse(source, pos);
    }

    public static final DateFormat getTimeInstance() {
        return DateFormat.get(2, 0, 1, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static final DateFormat getTimeInstance(int style) {
        return DateFormat.get(style, 0, 1, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static final DateFormat getTimeInstance(int style, Locale aLocale) {
        return DateFormat.get(style, 0, 1, aLocale);
    }

    public static final DateFormat getDateInstance() {
        return DateFormat.get(0, 2, 2, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static final DateFormat getDateInstance(int style) {
        return DateFormat.get(0, style, 2, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static final DateFormat getDateInstance(int style, Locale aLocale) {
        return DateFormat.get(0, style, 2, aLocale);
    }

    public static final DateFormat getDateTimeInstance() {
        return DateFormat.get(2, 2, 3, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static final DateFormat getDateTimeInstance(int dateStyle, int timeStyle) {
        return DateFormat.get(timeStyle, dateStyle, 3, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static final DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale aLocale) {
        return DateFormat.get(timeStyle, dateStyle, 3, aLocale);
    }

    public static final DateFormat getInstance() {
        return DateFormat.getDateTimeInstance(3, 3);
    }

    public static Locale[] getAvailableLocales() {
        LocaleServiceProviderPool pool = LocaleServiceProviderPool.getPool(DateFormatProvider.class);
        return pool.getAvailableLocales();
    }

    public void setCalendar(Calendar newCalendar) {
        this.calendar = newCalendar;
    }

    public Calendar getCalendar() {
        return this.calendar;
    }

    public void setNumberFormat(NumberFormat newNumberFormat) {
        this.numberFormat = newNumberFormat;
    }

    public NumberFormat getNumberFormat() {
        return this.numberFormat;
    }

    public void setTimeZone(TimeZone zone) {
        this.calendar.setTimeZone(zone);
    }

    public TimeZone getTimeZone() {
        return this.calendar.getTimeZone();
    }

    public void setLenient(boolean lenient) {
        this.calendar.setLenient(lenient);
    }

    public boolean isLenient() {
        return this.calendar.isLenient();
    }

    public int hashCode() {
        return this.numberFormat.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        DateFormat other = (DateFormat)obj;
        return this.calendar.getFirstDayOfWeek() == other.calendar.getFirstDayOfWeek() && this.calendar.getMinimalDaysInFirstWeek() == other.calendar.getMinimalDaysInFirstWeek() && this.calendar.isLenient() == other.calendar.isLenient() && this.calendar.getTimeZone().equals(other.calendar.getTimeZone()) && this.numberFormat.equals(other.numberFormat);
    }

    @Override
    public Object clone() {
        DateFormat other = (DateFormat)super.clone();
        other.calendar = (Calendar)this.calendar.clone();
        other.numberFormat = (NumberFormat)this.numberFormat.clone();
        return other;
    }

    private static DateFormat get(int timeStyle, int dateStyle, int flags, Locale loc) {
        LocaleProviderAdapter adapter;
        DateFormat dateFormat;
        if ((flags & 1) != 0) {
            if (timeStyle < 0 || timeStyle > 3) {
                throw new IllegalArgumentException("Illegal time style " + timeStyle);
            }
        } else {
            timeStyle = -1;
        }
        if ((flags & 2) != 0) {
            if (dateStyle < 0 || dateStyle > 3) {
                throw new IllegalArgumentException("Illegal date style " + dateStyle);
            }
        } else {
            dateStyle = -1;
        }
        if ((dateFormat = DateFormat.get(adapter = LocaleProviderAdapter.getAdapter(DateFormatProvider.class, loc), timeStyle, dateStyle, loc)) == null) {
            dateFormat = DateFormat.get(LocaleProviderAdapter.forJRE(), timeStyle, dateStyle, loc);
        }
        return dateFormat;
    }

    private static DateFormat get(LocaleProviderAdapter adapter, int timeStyle, int dateStyle, Locale loc) {
        DateFormatProvider provider = adapter.getDateFormatProvider();
        DateFormat dateFormat = timeStyle == -1 ? provider.getDateInstance(dateStyle, loc) : (dateStyle == -1 ? provider.getTimeInstance(timeStyle, loc) : provider.getDateTimeInstance(dateStyle, timeStyle, loc));
        return dateFormat;
    }

    protected DateFormat() {
    }

    public static class Field
    extends Format.Field {
        private static final long serialVersionUID = 7441350119349544720L;
        private static final Map<String, Field> instanceMap = new HashMap<String, Field>(18);
        private static final Field[] calendarToFieldMapping = new Field[17];
        private int calendarField;
        public static final Field ERA = new Field("era", 0);
        public static final Field YEAR = new Field("year", 1);
        public static final Field MONTH = new Field("month", 2);
        public static final Field DAY_OF_MONTH = new Field("day of month", 5);
        public static final Field HOUR_OF_DAY1 = new Field("hour of day 1", -1);
        public static final Field HOUR_OF_DAY0 = new Field("hour of day", 11);
        public static final Field MINUTE = new Field("minute", 12);
        public static final Field SECOND = new Field("second", 13);
        public static final Field MILLISECOND = new Field("millisecond", 14);
        public static final Field DAY_OF_WEEK = new Field("day of week", 7);
        public static final Field DAY_OF_YEAR = new Field("day of year", 6);
        public static final Field DAY_OF_WEEK_IN_MONTH = new Field("day of week in month", 8);
        public static final Field WEEK_OF_YEAR = new Field("week of year", 3);
        public static final Field WEEK_OF_MONTH = new Field("week of month", 4);
        public static final Field AM_PM = new Field("am pm", 9);
        public static final Field HOUR1 = new Field("hour 1", -1);
        public static final Field HOUR0 = new Field("hour", 10);
        public static final Field TIME_ZONE = new Field("time zone", -1);

        public static Field ofCalendarField(int calendarField) {
            if (calendarField < 0 || calendarField >= calendarToFieldMapping.length) {
                throw new IllegalArgumentException("Unknown Calendar constant " + calendarField);
            }
            return calendarToFieldMapping[calendarField];
        }

        protected Field(String name, int calendarField) {
            super(name);
            this.calendarField = calendarField;
            if (this.getClass() == Field.class) {
                instanceMap.put(name, this);
                if (calendarField >= 0) {
                    Field.calendarToFieldMapping[calendarField] = this;
                }
            }
        }

        public int getCalendarField() {
            return this.calendarField;
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

