/*
 * Decompiled with CFR 0.152.
 */
package sun.util.calendar;

import java.util.Locale;
import java.util.TimeZone;
import sun.util.calendar.CalendarUtils;
import sun.util.calendar.Era;

public abstract class CalendarDate
implements Cloneable {
    public static final int FIELD_UNDEFINED = Integer.MIN_VALUE;
    public static final long TIME_UNDEFINED = Long.MIN_VALUE;
    private Era era;
    private int year;
    private int month;
    private int dayOfMonth;
    private int dayOfWeek = Integer.MIN_VALUE;
    private boolean leapYear;
    private int hours;
    private int minutes;
    private int seconds;
    private int millis;
    private long fraction;
    private boolean normalized;
    private TimeZone zoneinfo;
    private int zoneOffset;
    private int daylightSaving;
    private boolean forceStandardTime;
    private Locale locale;

    protected CalendarDate() {
        this(TimeZone.getDefault());
    }

    protected CalendarDate(TimeZone zone) {
        this.zoneinfo = zone;
    }

    public Era getEra() {
        return this.era;
    }

    public CalendarDate setEra(Era era) {
        if (this.era == era) {
            return this;
        }
        this.era = era;
        this.normalized = false;
        return this;
    }

    public int getYear() {
        return this.year;
    }

    public CalendarDate setYear(int year) {
        if (this.year != year) {
            this.year = year;
            this.normalized = false;
        }
        return this;
    }

    public CalendarDate addYear(int n) {
        if (n != 0) {
            this.year += n;
            this.normalized = false;
        }
        return this;
    }

    public boolean isLeapYear() {
        return this.leapYear;
    }

    void setLeapYear(boolean leapYear) {
        this.leapYear = leapYear;
    }

    public int getMonth() {
        return this.month;
    }

    public CalendarDate setMonth(int month) {
        if (this.month != month) {
            this.month = month;
            this.normalized = false;
        }
        return this;
    }

    public CalendarDate addMonth(int n) {
        if (n != 0) {
            this.month += n;
            this.normalized = false;
        }
        return this;
    }

    public int getDayOfMonth() {
        return this.dayOfMonth;
    }

    public CalendarDate setDayOfMonth(int date) {
        if (this.dayOfMonth != date) {
            this.dayOfMonth = date;
            this.normalized = false;
        }
        return this;
    }

    public int getDayOfWeek() {
        if (!this.isNormalized()) {
            this.dayOfWeek = Integer.MIN_VALUE;
        }
        return this.dayOfWeek;
    }

    public int getHours() {
        return this.hours;
    }

    public CalendarDate setHours(int hours) {
        if (this.hours != hours) {
            this.hours = hours;
            this.normalized = false;
        }
        return this;
    }

    public CalendarDate addHours(int n) {
        if (n != 0) {
            this.hours += n;
            this.normalized = false;
        }
        return this;
    }

    public int getMinutes() {
        return this.minutes;
    }

    public CalendarDate setMinutes(int minutes) {
        if (this.minutes != minutes) {
            this.minutes = minutes;
            this.normalized = false;
        }
        return this;
    }

    public int getSeconds() {
        return this.seconds;
    }

    public CalendarDate setSeconds(int seconds) {
        if (this.seconds != seconds) {
            this.seconds = seconds;
            this.normalized = false;
        }
        return this;
    }

    public int getMillis() {
        return this.millis;
    }

    public CalendarDate setMillis(int millis) {
        if (this.millis != millis) {
            this.millis = millis;
            this.normalized = false;
        }
        return this;
    }

    public long getTimeOfDay() {
        if (!this.isNormalized()) {
            this.fraction = Long.MIN_VALUE;
            return Long.MIN_VALUE;
        }
        return this.fraction;
    }

    public CalendarDate setDate(int year, int month, int dayOfMonth) {
        this.setYear(year);
        this.setMonth(month);
        this.setDayOfMonth(dayOfMonth);
        return this;
    }

    public CalendarDate setTimeOfDay(int hours, int minutes, int seconds, int millis) {
        this.setHours(hours);
        this.setMinutes(minutes);
        this.setSeconds(seconds);
        this.setMillis(millis);
        return this;
    }

    protected void setTimeOfDay(long fraction) {
        this.fraction = fraction;
    }

    public boolean isNormalized() {
        return this.normalized;
    }

    public boolean isStandardTime() {
        return this.forceStandardTime;
    }

    public boolean isDaylightTime() {
        if (this.isStandardTime()) {
            return false;
        }
        return this.daylightSaving != 0;
    }

    protected void setLocale(Locale loc) {
        this.locale = loc;
    }

    public TimeZone getZone() {
        return this.zoneinfo;
    }

    public CalendarDate setZone(TimeZone zoneinfo) {
        this.zoneinfo = zoneinfo;
        return this;
    }

    public boolean isSameDate(CalendarDate date) {
        return this.getDayOfWeek() == date.getDayOfWeek() && this.getMonth() == date.getMonth() && this.getYear() == date.getYear() && this.getEra() == date.getEra();
    }

    public boolean equals(Object obj) {
        boolean thatHasZone;
        if (!(obj instanceof CalendarDate)) {
            return false;
        }
        CalendarDate that = (CalendarDate)obj;
        if (this.isNormalized() != that.isNormalized()) {
            return false;
        }
        boolean hasZone = this.zoneinfo != null;
        boolean bl = thatHasZone = that.zoneinfo != null;
        if (hasZone != thatHasZone) {
            return false;
        }
        if (hasZone && !this.zoneinfo.equals(that.zoneinfo)) {
            return false;
        }
        return this.getEra() == that.getEra() && this.year == that.year && this.month == that.month && this.dayOfMonth == that.dayOfMonth && this.hours == that.hours && this.minutes == that.minutes && this.seconds == that.seconds && this.millis == that.millis && this.zoneOffset == that.zoneOffset;
    }

    public int hashCode() {
        long hash = ((((long)this.year - 1970L) * 12L + (long)(this.month - 1)) * 30L + (long)this.dayOfMonth) * 24L;
        hash = (((hash + (long)this.hours) * 60L + (long)this.minutes) * 60L + (long)this.seconds) * 1000L + (long)this.millis;
        hash -= (long)this.zoneOffset;
        int normalized = this.isNormalized() ? 1 : 0;
        int era = 0;
        Era e = this.getEra();
        if (e != null) {
            era = e.hashCode();
        }
        int zone = this.zoneinfo != null ? this.zoneinfo.hashCode() : 0;
        return (int)hash * (int)(hash >> 32) ^ era ^ normalized ^ zone;
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        CalendarUtils.sprintf0d(sb, this.year, 4).append('-');
        CalendarUtils.sprintf0d(sb, this.month, 2).append('-');
        CalendarUtils.sprintf0d(sb, this.dayOfMonth, 2).append('T');
        CalendarUtils.sprintf0d(sb, this.hours, 2).append(':');
        CalendarUtils.sprintf0d(sb, this.minutes, 2).append(':');
        CalendarUtils.sprintf0d(sb, this.seconds, 2).append('.');
        CalendarUtils.sprintf0d(sb, this.millis, 3);
        if (this.zoneOffset == 0) {
            sb.append('Z');
        } else if (this.zoneOffset != Integer.MIN_VALUE) {
            char sign;
            int offset;
            if (this.zoneOffset > 0) {
                offset = this.zoneOffset;
                sign = '+';
            } else {
                offset = -this.zoneOffset;
                sign = '-';
            }
            sb.append(sign);
            CalendarUtils.sprintf0d(sb, (offset /= 60000) / 60, 2);
            CalendarUtils.sprintf0d(sb, offset % 60, 2);
        } else {
            sb.append(" local time");
        }
        return sb.toString();
    }

    protected void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    protected void setNormalized(boolean normalized) {
        this.normalized = normalized;
    }

    public int getZoneOffset() {
        return this.zoneOffset;
    }

    protected void setZoneOffset(int offset) {
        this.zoneOffset = offset;
    }

    public int getDaylightSaving() {
        return this.daylightSaving;
    }

    protected void setDaylightSaving(int daylightSaving) {
        this.daylightSaving = daylightSaving;
    }
}

