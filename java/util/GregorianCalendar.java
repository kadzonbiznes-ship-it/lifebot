/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import sun.util.calendar.BaseCalendar;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.CalendarSystem;
import sun.util.calendar.CalendarUtils;
import sun.util.calendar.Era;
import sun.util.calendar.Gregorian;
import sun.util.calendar.JulianCalendar;
import sun.util.calendar.ZoneInfo;

public class GregorianCalendar
extends Calendar {
    public static final int BC = 0;
    static final int BCE = 0;
    public static final int AD = 1;
    static final int CE = 1;
    private static final int EPOCH_OFFSET = 719163;
    private static final int EPOCH_YEAR = 1970;
    static final int[] MONTH_LENGTH = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    static final int[] LEAP_MONTH_LENGTH = new int[]{31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final int ONE_SECOND = 1000;
    private static final int ONE_MINUTE = 60000;
    private static final int ONE_HOUR = 3600000;
    private static final long ONE_DAY = 86400000L;
    private static final long ONE_WEEK = 604800000L;
    static final int[] MIN_VALUES = new int[]{0, 1, 0, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, -46800000, 0};
    static final int[] LEAST_MAX_VALUES = new int[]{1, 292269054, 11, 52, 4, 28, 365, 7, 4, 1, 11, 23, 59, 59, 999, 50400000, 1200000};
    static final int[] MAX_VALUES = new int[]{1, 292278994, 11, 53, 6, 31, 366, 7, 6, 1, 11, 23, 59, 59, 999, 50400000, 0x6DDD00};
    static final long serialVersionUID = -8125100834729963327L;
    private static final Gregorian gcal = CalendarSystem.getGregorianCalendar();
    private static JulianCalendar jcal;
    private static Era[] jeras;
    static final long DEFAULT_GREGORIAN_CUTOVER = -12219292800000L;
    private long gregorianCutover = -12219292800000L;
    private transient long gregorianCutoverDate = 577736L;
    private transient int gregorianCutoverYear = 1582;
    private transient int gregorianCutoverYearJulian = 1582;
    private transient BaseCalendar.Date gdate;
    private transient BaseCalendar.Date cdate;
    private transient BaseCalendar calsys;
    private transient int[] zoneOffsets;
    private transient int[] originalFields;
    private transient long cachedFixedDate = Long.MIN_VALUE;

    public GregorianCalendar() {
        this(TimeZone.getDefaultRef(), Locale.getDefault(Locale.Category.FORMAT));
        this.setZoneShared(true);
    }

    public GregorianCalendar(TimeZone zone) {
        this(zone, Locale.getDefault(Locale.Category.FORMAT));
    }

    public GregorianCalendar(Locale aLocale) {
        this(TimeZone.getDefaultRef(), aLocale);
        this.setZoneShared(true);
    }

    public GregorianCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
        this.gdate = gcal.newCalendarDate(zone);
        this.setTimeInMillis(System.currentTimeMillis());
    }

    public GregorianCalendar(int year, int month, int dayOfMonth) {
        this(year, month, dayOfMonth, 0, 0, 0, 0);
    }

    public GregorianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
        this(year, month, dayOfMonth, hourOfDay, minute, 0, 0);
    }

    public GregorianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
        this(year, month, dayOfMonth, hourOfDay, minute, second, 0);
    }

    GregorianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second, int millis) {
        this.gdate = gcal.newCalendarDate(this.getZone());
        this.set(1, year);
        this.set(2, month);
        this.set(5, dayOfMonth);
        if (hourOfDay >= 12 && hourOfDay <= 23) {
            this.internalSet(9, 1);
            this.internalSet(10, hourOfDay - 12);
        } else {
            this.internalSet(10, hourOfDay);
        }
        this.setFieldsComputed(1536);
        this.set(11, hourOfDay);
        this.set(12, minute);
        this.set(13, second);
        this.internalSet(14, millis);
    }

    GregorianCalendar(TimeZone zone, Locale locale, boolean flag) {
        super(zone, locale);
        this.gdate = gcal.newCalendarDate(this.getZone());
    }

    public void setGregorianChange(Date date) {
        long cutoverTime = date.getTime();
        if (cutoverTime == this.gregorianCutover) {
            return;
        }
        this.complete();
        this.setGregorianChange(cutoverTime);
    }

    private void setGregorianChange(long cutoverTime) {
        this.gregorianCutover = cutoverTime;
        this.gregorianCutoverDate = CalendarUtils.floorDivide(cutoverTime, 86400000L) + 719163L;
        if (cutoverTime == Long.MAX_VALUE) {
            ++this.gregorianCutoverDate;
        }
        BaseCalendar.Date d = this.getGregorianCutoverDate();
        this.gregorianCutoverYear = d.getYear();
        BaseCalendar julianCal = GregorianCalendar.getJulianCalendarSystem();
        d = (BaseCalendar.Date)julianCal.newCalendarDate(TimeZone.NO_TIMEZONE);
        julianCal.getCalendarDateFromFixedDate(d, this.gregorianCutoverDate - 1L);
        this.gregorianCutoverYearJulian = d.getNormalizedYear();
        if (this.time < this.gregorianCutover) {
            this.setUnnormalized();
        }
    }

    public final Date getGregorianChange() {
        return new Date(this.gregorianCutover);
    }

    public boolean isLeapYear(int year) {
        boolean gregorian;
        if ((year & 3) != 0) {
            return false;
        }
        if (year > this.gregorianCutoverYear) {
            return year % 100 != 0 || year % 400 == 0;
        }
        if (year < this.gregorianCutoverYearJulian) {
            return true;
        }
        if (this.gregorianCutoverYear == this.gregorianCutoverYearJulian) {
            BaseCalendar.Date d = this.getCalendarDate(this.gregorianCutoverDate);
            gregorian = d.getMonth() < 3;
        } else {
            boolean bl = gregorian = year == this.gregorianCutoverYear;
        }
        return gregorian ? year % 100 != 0 || year % 400 == 0 : true;
    }

    @Override
    public String getCalendarType() {
        return "gregory";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GregorianCalendar && super.equals(obj) && this.gregorianCutover == ((GregorianCalendar)obj).gregorianCutover;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ (int)this.gregorianCutoverDate;
    }

    @Override
    public void add(int field, int amount) {
        if (amount == 0) {
            return;
        }
        if (field < 0 || field >= 15) {
            throw new IllegalArgumentException();
        }
        this.complete();
        if (field == 1) {
            int year = this.internalGet(1);
            if (this.internalGetEra() == 1) {
                if ((year += amount) > 0) {
                    this.set(1, year);
                } else {
                    this.set(1, 1 - year);
                    this.set(0, 0);
                }
            } else if ((year -= amount) > 0) {
                this.set(1, year);
            } else {
                this.set(1, 1 - year);
                this.set(0, 1);
            }
            this.pinDayOfMonth();
        } else if (field == 2) {
            int month = this.internalGet(2) + amount;
            int year = this.internalGet(1);
            int y_amount = month >= 0 ? month / 12 : (month + 1) / 12 - 1;
            if (y_amount != 0) {
                if (this.internalGetEra() == 1) {
                    if ((year += y_amount) > 0) {
                        this.set(1, year);
                    } else {
                        this.set(1, 1 - year);
                        this.set(0, 0);
                    }
                } else if ((year -= y_amount) > 0) {
                    this.set(1, year);
                } else {
                    this.set(1, 1 - year);
                    this.set(0, 1);
                }
            }
            if (month >= 0) {
                this.set(2, month % 12);
            } else {
                if ((month %= 12) < 0) {
                    month += 12;
                }
                this.set(2, 0 + month);
            }
            this.pinDayOfMonth();
        } else if (field == 0) {
            int era = this.internalGet(0) + amount;
            if (era < 0) {
                era = 0;
            }
            if (era > 1) {
                era = 1;
            }
            this.set(0, era);
        } else {
            long delta = amount;
            long timeOfDay = 0L;
            switch (field) {
                case 10: 
                case 11: {
                    delta *= 3600000L;
                    break;
                }
                case 12: {
                    delta *= 60000L;
                    break;
                }
                case 13: {
                    delta *= 1000L;
                    break;
                }
                case 14: {
                    break;
                }
                case 3: 
                case 4: 
                case 8: {
                    delta *= 7L;
                    break;
                }
                case 5: 
                case 6: 
                case 7: {
                    break;
                }
                case 9: {
                    delta = amount / 2;
                    timeOfDay = 12 * (amount % 2);
                }
            }
            if (field >= 10) {
                this.setTimeInMillis(this.time + delta);
                return;
            }
            long fd = this.getCurrentFixedDate();
            timeOfDay += (long)this.internalGet(11);
            timeOfDay *= 60L;
            timeOfDay += (long)this.internalGet(12);
            timeOfDay *= 60L;
            timeOfDay += (long)this.internalGet(13);
            timeOfDay *= 1000L;
            if ((timeOfDay += (long)this.internalGet(14)) >= 86400000L) {
                ++fd;
                timeOfDay -= 86400000L;
            } else if (timeOfDay < 0L) {
                --fd;
                timeOfDay += 86400000L;
            }
            int zoneOffset = this.internalGet(15) + this.internalGet(16);
            this.setTimeInMillis(((fd += delta) - 719163L) * 86400000L + timeOfDay - (long)zoneOffset);
            if ((zoneOffset -= this.internalGet(15) + this.internalGet(16)) != 0) {
                this.setTimeInMillis(this.time + (long)zoneOffset);
                long fd2 = this.getCurrentFixedDate();
                if (fd2 != fd) {
                    this.setTimeInMillis(this.time - (long)zoneOffset);
                }
            }
        }
    }

    @Override
    public void roll(int field, boolean up) {
        this.roll(field, up ? 1 : -1);
    }

    @Override
    public void roll(int field, int amount) {
        if (amount == 0) {
            return;
        }
        if (field < 0 || field >= 15) {
            throw new IllegalArgumentException();
        }
        this.complete();
        int min = this.getMinimum(field);
        int max = this.getMaximum(field);
        switch (field) {
            case 0: 
            case 1: 
            case 9: 
            case 12: 
            case 13: 
            case 14: {
                break;
            }
            case 10: 
            case 11: {
                int rolledValue;
                int hourOfDay = rolledValue = GregorianCalendar.getRolledValue(this.internalGet(field), amount, min, max);
                if (field == 10 && this.internalGet(9) == 1) {
                    hourOfDay += 12;
                }
                CalendarDate d = this.calsys.getCalendarDate(this.time, this.getZone());
                d.setHours(hourOfDay);
                this.time = this.calsys.getTime(d);
                if (this.internalGet(11) == d.getHours()) {
                    hourOfDay = GregorianCalendar.getRolledValue(rolledValue, amount > 0 ? 1 : -1, min, max);
                    if (field == 10 && this.internalGet(9) == 1) {
                        hourOfDay += 12;
                    }
                    d.setHours(hourOfDay);
                    this.time = this.calsys.getTime(d);
                }
                hourOfDay = d.getHours();
                this.internalSet(11, hourOfDay);
                this.internalSet(9, hourOfDay / 12);
                this.internalSet(10, hourOfDay % 12);
                int zoneOffset = d.getZoneOffset();
                int saving = d.getDaylightSaving();
                this.internalSet(15, zoneOffset - saving);
                this.internalSet(16, saving);
                return;
            }
            case 2: {
                if (!this.isCutoverYear(this.cdate.getNormalizedYear())) {
                    int mon = (this.internalGet(2) + amount) % 12;
                    if (mon < 0) {
                        mon += 12;
                    }
                    this.set(2, mon);
                    int monthLen = this.monthLength(mon);
                    if (this.internalGet(5) > monthLen) {
                        this.set(5, monthLen);
                    }
                } else {
                    int yearLength = this.getActualMaximum(2) + 1;
                    int mon = (this.internalGet(2) + amount) % yearLength;
                    if (mon < 0) {
                        mon += yearLength;
                    }
                    this.set(2, mon);
                    int monthLen = this.getActualMaximum(5);
                    if (this.internalGet(5) > monthLen) {
                        this.set(5, monthLen);
                    }
                }
                return;
            }
            case 3: {
                long fd;
                long day1;
                int y = this.cdate.getNormalizedYear();
                max = this.getActualMaximum(3);
                this.set(7, this.internalGet(7));
                int woy = this.internalGet(3);
                int value = woy + amount;
                if (!this.isCutoverYear(y)) {
                    int weekYear = this.getWeekYear();
                    if (weekYear == y) {
                        if (value > min && value < max) {
                            this.set(3, value);
                            return;
                        }
                        long fd2 = this.getCurrentFixedDate();
                        long day12 = fd2 - (long)(7 * (woy - min));
                        if (this.calsys.getYearFromFixedDate(day12) != y) {
                            ++min;
                        }
                        if (this.calsys.getYearFromFixedDate(fd2 += (long)(7 * (max - this.internalGet(3)))) != y) {
                            --max;
                        }
                    } else if (weekYear > y) {
                        if (amount < 0) {
                            ++amount;
                        }
                        woy = max;
                    } else {
                        if (amount > 0) {
                            amount -= woy - max;
                        }
                        woy = min;
                    }
                    int newWeekOfYear = GregorianCalendar.getRolledValue(woy, amount, min, max);
                    if (newWeekOfYear == 1 && this.isInvalidWeek1() && amount > 0) {
                        ++newWeekOfYear;
                    }
                    this.set(field, newWeekOfYear);
                    return;
                }
                BaseCalendar cal = this.gregorianCutoverYear == this.gregorianCutoverYearJulian ? this.getCutoverCalendarSystem() : (y == this.gregorianCutoverYear ? gcal : GregorianCalendar.getJulianCalendarSystem());
                if (cal.getYearFromFixedDate(day1 = (fd = this.getCurrentFixedDate()) - (long)(7 * (woy - min))) != y) {
                    ++min;
                }
                BaseCalendar baseCalendar = cal = (fd += (long)(7 * (max - woy))) >= this.gregorianCutoverDate ? gcal : GregorianCalendar.getJulianCalendarSystem();
                if (cal.getYearFromFixedDate(fd) != y) {
                    --max;
                }
                value = GregorianCalendar.getRolledValue(woy, amount, min, max) - 1;
                BaseCalendar.Date d = this.getCalendarDate(day1 + (long)(value * 7));
                this.set(2, d.getMonth() - 1);
                this.set(5, d.getDayOfMonth());
                return;
            }
            case 4: {
                int dayOfMonth;
                int monthLength;
                long month1;
                boolean isCutoverYear = this.isCutoverYear(this.cdate.getNormalizedYear());
                int dow = this.internalGet(7) - this.getFirstDayOfWeek();
                if (dow < 0) {
                    dow += 7;
                }
                long fd = this.getCurrentFixedDate();
                if (isCutoverYear) {
                    month1 = this.getFixedDateMonth1(this.cdate, fd);
                    monthLength = this.actualMonthLength();
                } else {
                    month1 = fd - (long)this.internalGet(5) + 1L;
                    monthLength = this.calsys.getMonthLength(this.cdate);
                }
                long monthDay1st = BaseCalendar.getDayOfWeekDateOnOrBefore(month1 + 6L, this.getFirstDayOfWeek());
                if ((int)(monthDay1st - month1) >= this.getMinimalDaysInFirstWeek()) {
                    monthDay1st -= 7L;
                }
                max = this.getActualMaximum(field);
                int value = GregorianCalendar.getRolledValue(this.internalGet(field), amount, 1, max) - 1;
                long nfd = monthDay1st + (long)(value * 7) + (long)dow;
                if (nfd < month1) {
                    nfd = month1;
                } else if (nfd >= month1 + (long)monthLength) {
                    nfd = month1 + (long)monthLength - 1L;
                }
                if (isCutoverYear) {
                    BaseCalendar.Date d = this.getCalendarDate(nfd);
                    dayOfMonth = d.getDayOfMonth();
                } else {
                    dayOfMonth = (int)(nfd - month1) + 1;
                }
                this.set(5, dayOfMonth);
                return;
            }
            case 5: {
                if (!this.isCutoverYear(this.cdate.getNormalizedYear())) {
                    max = this.calsys.getMonthLength(this.cdate);
                    break;
                }
                long fd = this.getCurrentFixedDate();
                long month1 = this.getFixedDateMonth1(this.cdate, fd);
                int value = GregorianCalendar.getRolledValue((int)(fd - month1), amount, 0, this.actualMonthLength() - 1);
                BaseCalendar.Date d = this.getCalendarDate(month1 + (long)value);
                assert (d.getMonth() - 1 == this.internalGet(2));
                this.set(5, d.getDayOfMonth());
                return;
            }
            case 6: {
                max = this.getActualMaximum(field);
                if (!this.isCutoverYear(this.cdate.getNormalizedYear())) break;
                long fd = this.getCurrentFixedDate();
                long jan1 = fd - (long)this.internalGet(6) + 1L;
                int value = GregorianCalendar.getRolledValue((int)(fd - jan1) + 1, amount, min, max);
                BaseCalendar.Date d = this.getCalendarDate(jan1 + (long)value - 1L);
                this.set(2, d.getMonth() - 1);
                this.set(5, d.getDayOfMonth());
                return;
            }
            case 7: {
                long dowFirst;
                int weekOfYear;
                if (!this.isCutoverYear(this.cdate.getNormalizedYear()) && (weekOfYear = this.internalGet(3)) > 1 && weekOfYear < 52) {
                    this.set(3, weekOfYear);
                    max = 7;
                    break;
                }
                if ((amount %= 7) == 0) {
                    return;
                }
                long fd = this.getCurrentFixedDate();
                if ((fd += (long)amount) < (dowFirst = BaseCalendar.getDayOfWeekDateOnOrBefore(fd, this.getFirstDayOfWeek()))) {
                    fd += 7L;
                } else if (fd >= dowFirst + 7L) {
                    fd -= 7L;
                }
                BaseCalendar.Date d = this.getCalendarDate(fd);
                this.set(0, d.getNormalizedYear() <= 0 ? 0 : 1);
                this.set(d.getYear(), d.getMonth() - 1, d.getDayOfMonth());
                return;
            }
            case 8: {
                int value;
                min = 1;
                if (!this.isCutoverYear(this.cdate.getNormalizedYear())) {
                    int dom = this.internalGet(5);
                    int monthLength = this.calsys.getMonthLength(this.cdate);
                    int lastDays = monthLength % 7;
                    max = monthLength / 7;
                    int x = (dom - 1) % 7;
                    if (x < lastDays) {
                        ++max;
                    }
                    this.set(7, this.internalGet(7));
                    break;
                }
                long fd = this.getCurrentFixedDate();
                long month1 = this.getFixedDateMonth1(this.cdate, fd);
                int monthLength = this.actualMonthLength();
                int lastDays = monthLength % 7;
                max = monthLength / 7;
                int x = (int)(fd - month1) % 7;
                if (x < lastDays) {
                    ++max;
                }
                BaseCalendar cal = (fd = month1 + (long)((value = GregorianCalendar.getRolledValue(this.internalGet(field), amount, min, max) - 1) * 7) + (long)x) >= this.gregorianCutoverDate ? gcal : GregorianCalendar.getJulianCalendarSystem();
                BaseCalendar.Date d = (BaseCalendar.Date)cal.newCalendarDate(TimeZone.NO_TIMEZONE);
                cal.getCalendarDateFromFixedDate(d, fd);
                this.set(5, d.getDayOfMonth());
                return;
            }
        }
        this.set(field, GregorianCalendar.getRolledValue(this.internalGet(field), amount, min, max));
    }

    @Override
    public int getMinimum(int field) {
        return MIN_VALUES[field];
    }

    @Override
    public int getMaximum(int field) {
        switch (field) {
            case 1: 
            case 2: 
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 8: {
                if (this.gregorianCutoverYear > 200) break;
                GregorianCalendar gc = (GregorianCalendar)this.clone();
                gc.setLenient(true);
                gc.setTimeInMillis(this.gregorianCutover);
                int v1 = gc.getActualMaximum(field);
                gc.setTimeInMillis(this.gregorianCutover - 1L);
                int v2 = gc.getActualMaximum(field);
                return Math.max(MAX_VALUES[field], Math.max(v1, v2));
            }
        }
        return MAX_VALUES[field];
    }

    @Override
    public int getGreatestMinimum(int field) {
        if (field == 5) {
            BaseCalendar.Date d = this.getGregorianCutoverDate();
            long mon1 = this.getFixedDateMonth1(d, this.gregorianCutoverDate);
            d = this.getCalendarDate(mon1);
            return Math.max(MIN_VALUES[field], d.getDayOfMonth());
        }
        return MIN_VALUES[field];
    }

    @Override
    public int getLeastMaximum(int field) {
        switch (field) {
            case 1: 
            case 2: 
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 8: {
                GregorianCalendar gc = (GregorianCalendar)this.clone();
                gc.setLenient(true);
                gc.setTimeInMillis(this.gregorianCutover);
                int v1 = gc.getActualMaximum(field);
                gc.setTimeInMillis(this.gregorianCutover - 1L);
                int v2 = gc.getActualMaximum(field);
                return Math.min(LEAST_MAX_VALUES[field], Math.min(v1, v2));
            }
        }
        return LEAST_MAX_VALUES[field];
    }

    @Override
    public int getActualMinimum(int field) {
        if (field == 5) {
            GregorianCalendar gc = this.getNormalizedCalendar();
            int year = gc.cdate.getNormalizedYear();
            if (year == this.gregorianCutoverYear || year == this.gregorianCutoverYearJulian) {
                long month1 = this.getFixedDateMonth1(gc.cdate, gc.calsys.getFixedDate(gc.cdate));
                BaseCalendar.Date d = this.getCalendarDate(month1);
                return d.getDayOfMonth();
            }
        }
        return this.getMinimum(field);
    }

    @Override
    public int getActualMaximum(int field) {
        int fieldsForFixedMax = 130689;
        if ((0x1FE81 & 1 << field) != 0) {
            return this.getMaximum(field);
        }
        GregorianCalendar gc = this.getNormalizedCalendar();
        BaseCalendar.Date date = gc.cdate;
        BaseCalendar cal = gc.calsys;
        int normalizedYear = date.getNormalizedYear();
        int value = -1;
        switch (field) {
            case 2: {
                long nextJan1;
                if (!gc.isCutoverYear(normalizedYear)) {
                    value = 11;
                    break;
                }
                while ((nextJan1 = gcal.getFixedDate(++normalizedYear, 1, 1, null)) < this.gregorianCutoverDate) {
                }
                BaseCalendar.Date d = (BaseCalendar.Date)date.clone();
                cal.getCalendarDateFromFixedDate(d, nextJan1 - 1L);
                value = d.getMonth() - 1;
                break;
            }
            case 5: {
                long fd;
                value = cal.getMonthLength(date);
                if (!gc.isCutoverYear(normalizedYear) || date.getDayOfMonth() == value || (fd = gc.getCurrentFixedDate()) >= this.gregorianCutoverDate) break;
                int monthLength = gc.actualMonthLength();
                long monthEnd = gc.getFixedDateMonth1(gc.cdate, fd) + (long)monthLength - 1L;
                BaseCalendar.Date d = gc.getCalendarDate(monthEnd);
                value = d.getDayOfMonth();
                break;
            }
            case 6: {
                long jan1;
                if (!gc.isCutoverYear(normalizedYear)) {
                    value = cal.getYearLength(date);
                    break;
                }
                if (this.gregorianCutoverYear == this.gregorianCutoverYearJulian) {
                    BaseCalendar cocal = gc.getCutoverCalendarSystem();
                    jan1 = cocal.getFixedDate(normalizedYear, 1, 1, null);
                } else {
                    jan1 = normalizedYear == this.gregorianCutoverYearJulian ? cal.getFixedDate(normalizedYear, 1, 1, null) : this.gregorianCutoverDate;
                }
                long nextJan1 = gcal.getFixedDate(++normalizedYear, 1, 1, null);
                if (nextJan1 < this.gregorianCutoverDate) {
                    nextJan1 = this.gregorianCutoverDate;
                }
                assert (jan1 <= cal.getFixedDate(date.getNormalizedYear(), date.getMonth(), date.getDayOfMonth(), date));
                assert (nextJan1 >= cal.getFixedDate(date.getNormalizedYear(), date.getMonth(), date.getDayOfMonth(), date));
                value = (int)(nextJan1 - jan1);
                break;
            }
            case 3: {
                if (!gc.isCutoverYear(normalizedYear)) {
                    CalendarDate d = cal.newCalendarDate(TimeZone.NO_TIMEZONE);
                    d.setDate(date.getYear(), 1, 1);
                    int dayOfWeek = cal.getDayOfWeek(d);
                    if ((dayOfWeek -= this.getFirstDayOfWeek()) < 0) {
                        dayOfWeek += 7;
                    }
                    value = 52;
                    int magic = dayOfWeek + this.getMinimalDaysInFirstWeek() - 1;
                    if (magic != 6 && (!date.isLeapYear() || magic != 5 && magic != 12)) break;
                    ++value;
                    break;
                }
                if (gc == this) {
                    gc = (GregorianCalendar)gc.clone();
                }
                int maxDayOfYear = this.getActualMaximum(6);
                gc.set(6, maxDayOfYear);
                value = gc.get(3);
                if (this.internalGet(1) == gc.getWeekYear()) break;
                gc.set(6, maxDayOfYear - 7);
                value = gc.get(3);
                break;
            }
            case 4: {
                if (!gc.isCutoverYear(normalizedYear)) {
                    CalendarDate d = cal.newCalendarDate(null);
                    d.setDate(date.getYear(), date.getMonth(), 1);
                    int dayOfWeek = cal.getDayOfWeek(d);
                    int monthLength = cal.getMonthLength(d);
                    if ((dayOfWeek -= this.getFirstDayOfWeek()) < 0) {
                        dayOfWeek += 7;
                    }
                    int nDaysFirstWeek = 7 - dayOfWeek;
                    value = 3;
                    if (nDaysFirstWeek >= this.getMinimalDaysInFirstWeek()) {
                        ++value;
                    }
                    if ((monthLength -= nDaysFirstWeek + 21) <= 0) break;
                    ++value;
                    if (monthLength <= 7) break;
                    ++value;
                    break;
                }
                if (gc == this) {
                    gc = (GregorianCalendar)gc.clone();
                }
                int y = gc.internalGet(1);
                int m = gc.internalGet(2);
                do {
                    value = gc.get(4);
                    gc.add(4, 1);
                } while (gc.get(1) == y && gc.get(2) == m);
                break;
            }
            case 8: {
                int dow1;
                int dow = date.getDayOfWeek();
                if (!gc.isCutoverYear(normalizedYear)) {
                    BaseCalendar.Date d = (BaseCalendar.Date)date.clone();
                    ndays = cal.getMonthLength(d);
                    d.setDayOfMonth(1);
                    cal.normalize(d);
                    dow1 = d.getDayOfWeek();
                } else {
                    if (gc == this) {
                        gc = (GregorianCalendar)this.clone();
                    }
                    ndays = gc.actualMonthLength();
                    gc.set(5, gc.getActualMinimum(5));
                    dow1 = gc.get(7);
                }
                int x = dow - dow1;
                if (x < 0) {
                    x += 7;
                }
                value = ((ndays -= x) + 6) / 7;
                break;
            }
            case 1: {
                if (gc == this) {
                    gc = (GregorianCalendar)this.clone();
                }
                long current = gc.getYearOffsetInMillis();
                if (gc.internalGetEra() == 1) {
                    gc.setTimeInMillis(Long.MAX_VALUE);
                    value = gc.get(1);
                    long maxEnd = gc.getYearOffsetInMillis();
                    if (current <= maxEnd) break;
                    --value;
                    break;
                }
                BaseCalendar mincal = gc.getTimeInMillis() >= this.gregorianCutover ? gcal : GregorianCalendar.getJulianCalendarSystem();
                CalendarDate d = ((CalendarSystem)mincal).getCalendarDate(Long.MIN_VALUE, this.getZone());
                long maxEnd = (cal.getDayOfYear(d) - 1L) * 24L + (long)d.getHours();
                maxEnd *= 60L;
                maxEnd += (long)d.getMinutes();
                maxEnd *= 60L;
                maxEnd += (long)d.getSeconds();
                maxEnd *= 1000L;
                maxEnd += (long)d.getMillis();
                value = d.getYear();
                if (value <= 0) {
                    assert (mincal == gcal);
                    value = 1 - value;
                }
                if (current >= maxEnd) break;
                --value;
                break;
            }
            default: {
                throw new ArrayIndexOutOfBoundsException(field);
            }
        }
        return value;
    }

    private long getYearOffsetInMillis() {
        long t = (this.internalGet(6) - 1) * 24;
        t += (long)this.internalGet(11);
        t *= 60L;
        t += (long)this.internalGet(12);
        t *= 60L;
        t += (long)this.internalGet(13);
        return (t *= 1000L) + (long)this.internalGet(14) - (long)(this.internalGet(15) + this.internalGet(16));
    }

    @Override
    public Object clone() {
        GregorianCalendar other = (GregorianCalendar)super.clone();
        other.gdate = (BaseCalendar.Date)this.gdate.clone();
        if (this.cdate != null) {
            other.cdate = this.cdate != this.gdate ? (BaseCalendar.Date)this.cdate.clone() : other.gdate;
        }
        other.originalFields = null;
        other.zoneOffsets = null;
        return other;
    }

    @Override
    public TimeZone getTimeZone() {
        TimeZone zone = super.getTimeZone();
        this.gdate.setZone(zone);
        if (this.cdate != null && this.cdate != this.gdate) {
            this.cdate.setZone(zone);
        }
        return zone;
    }

    @Override
    public void setTimeZone(TimeZone zone) {
        super.setTimeZone(zone);
        this.gdate.setZone(zone);
        if (this.cdate != null && this.cdate != this.gdate) {
            this.cdate.setZone(zone);
        }
    }

    @Override
    public final boolean isWeekDateSupported() {
        return true;
    }

    @Override
    public int getWeekYear() {
        int minDayOfYear;
        int year = this.get(1);
        if (this.internalGetEra() == 0) {
            year = 1 - year;
        }
        if (year > this.gregorianCutoverYear + 1) {
            int weekOfYear = this.internalGet(3);
            if (this.internalGet(2) == 0) {
                if (weekOfYear >= 52) {
                    --year;
                }
            } else if (weekOfYear == 1) {
                ++year;
            }
            return year;
        }
        int dayOfYear = this.internalGet(6);
        int maxDayOfYear = this.getActualMaximum(6);
        int minimalDays = this.getMinimalDaysInFirstWeek();
        if (dayOfYear > minimalDays && dayOfYear < maxDayOfYear - 6) {
            return year;
        }
        GregorianCalendar cal = (GregorianCalendar)this.clone();
        cal.setLenient(true);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(6, 1);
        cal.complete();
        int delta = this.getFirstDayOfWeek() - cal.get(7);
        if (delta != 0) {
            if (delta < 0) {
                delta += 7;
            }
            cal.add(6, delta);
        }
        if (dayOfYear < (minDayOfYear = cal.get(6))) {
            if (minDayOfYear <= minimalDays) {
                --year;
            }
        } else {
            int days;
            cal.set(1, year + 1);
            cal.set(6, 1);
            cal.complete();
            int del = this.getFirstDayOfWeek() - cal.get(7);
            if (del != 0) {
                if (del < 0) {
                    del += 7;
                }
                cal.add(6, del);
            }
            if ((minDayOfYear = cal.get(6) - 1) == 0) {
                minDayOfYear = 7;
            }
            if (minDayOfYear >= minimalDays && (days = maxDayOfYear - dayOfYear + 1) <= 7 - minDayOfYear) {
                ++year;
            }
        }
        return year;
    }

    @Override
    public void setWeekDate(int weekYear, int weekOfYear, int dayOfWeek) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("invalid dayOfWeek: " + dayOfWeek);
        }
        GregorianCalendar gc = (GregorianCalendar)this.clone();
        gc.setLenient(true);
        int era = gc.get(0);
        gc.clear();
        gc.setTimeZone(TimeZone.getTimeZone("GMT"));
        gc.set(0, era);
        gc.set(1, weekYear);
        gc.set(3, 1);
        gc.set(7, this.getFirstDayOfWeek());
        int days = dayOfWeek - this.getFirstDayOfWeek();
        if (days < 0) {
            days += 7;
        }
        if ((days += 7 * (weekOfYear - 1)) != 0) {
            gc.add(6, days);
        } else {
            gc.complete();
        }
        if (!(this.isLenient() || gc.getWeekYear() == weekYear && gc.internalGet(3) == weekOfYear && gc.internalGet(7) == dayOfWeek)) {
            throw new IllegalArgumentException();
        }
        this.set(0, gc.internalGet(0));
        this.set(1, gc.internalGet(1));
        this.set(2, gc.internalGet(2));
        this.set(5, gc.internalGet(5));
        this.internalSet(3, weekOfYear);
        this.complete();
    }

    @Override
    public int getWeeksInWeekYear() {
        GregorianCalendar gc = this.getNormalizedCalendar();
        int weekYear = gc.getWeekYear();
        if (weekYear == gc.internalGet(1)) {
            return gc.getActualMaximum(3);
        }
        if (gc == this) {
            gc = (GregorianCalendar)gc.clone();
        }
        gc.setWeekDate(weekYear, 2, this.internalGet(7));
        return gc.getActualMaximum(3);
    }

    @Override
    protected void computeFields() {
        int mask;
        if (this.isPartiallyNormalized()) {
            mask = this.getSetStateFields();
            int fieldMask = ~mask & 0x1FFFF;
            if (fieldMask != 0 || this.calsys == null) {
                mask |= this.computeFields(fieldMask, mask & 0x18000);
                assert (mask == 131071);
            }
        } else {
            mask = 131071;
            this.computeFields(mask, 0);
        }
        this.setFieldsComputed(mask);
    }

    private int computeFields(int fieldMask, int tzMask) {
        int year;
        int zoneOffset = 0;
        TimeZone tz = this.getZone();
        if (this.zoneOffsets == null) {
            this.zoneOffsets = new int[2];
        }
        if (tzMask != 98304) {
            if (tz instanceof ZoneInfo) {
                zoneOffset = ((ZoneInfo)tz).getOffsets(this.time, this.zoneOffsets);
            } else {
                zoneOffset = tz.getOffset(this.time);
                this.zoneOffsets[0] = tz.getRawOffset();
                this.zoneOffsets[1] = zoneOffset - this.zoneOffsets[0];
            }
        }
        if (tzMask != 0) {
            if (GregorianCalendar.isFieldSet(tzMask, 15)) {
                this.zoneOffsets[0] = this.internalGet(15);
            }
            if (GregorianCalendar.isFieldSet(tzMask, 16)) {
                this.zoneOffsets[1] = this.internalGet(16);
            }
            zoneOffset = this.zoneOffsets[0] + this.zoneOffsets[1];
        }
        long fixedDate = (long)zoneOffset / 86400000L;
        int timeOfDay = zoneOffset % 86400000;
        fixedDate += this.time / 86400000L;
        if ((long)(timeOfDay += (int)(this.time % 86400000L)) >= 86400000L) {
            timeOfDay -= 86400000;
            ++fixedDate;
        } else {
            while (timeOfDay < 0) {
                timeOfDay += 86400000;
                --fixedDate;
            }
        }
        int era = 1;
        if ((fixedDate += 719163L) >= this.gregorianCutoverDate) {
            assert (this.cachedFixedDate == Long.MIN_VALUE || this.gdate.isNormalized()) : "cache control: not normalized";
            assert (this.cachedFixedDate == Long.MIN_VALUE || gcal.getFixedDate(this.gdate.getNormalizedYear(), this.gdate.getMonth(), this.gdate.getDayOfMonth(), this.gdate) == this.cachedFixedDate) : "cache control: inconsictency, cachedFixedDate=" + this.cachedFixedDate + ", computed=" + gcal.getFixedDate(this.gdate.getNormalizedYear(), this.gdate.getMonth(), this.gdate.getDayOfMonth(), this.gdate) + ", date=" + this.gdate;
            if (fixedDate != this.cachedFixedDate) {
                gcal.getCalendarDateFromFixedDate(this.gdate, fixedDate);
                this.cachedFixedDate = fixedDate;
            }
            if ((year = this.gdate.getYear()) <= 0) {
                year = 1 - year;
                era = 0;
            }
            this.calsys = gcal;
            this.cdate = this.gdate;
            assert (this.cdate.getDayOfWeek() > 0) : "dow=" + this.cdate.getDayOfWeek() + ", date=" + this.cdate;
        } else {
            this.calsys = GregorianCalendar.getJulianCalendarSystem();
            this.cdate = jcal.newCalendarDate(this.getZone());
            jcal.getCalendarDateFromFixedDate(this.cdate, fixedDate);
            Era e = this.cdate.getEra();
            if (e == jeras[0]) {
                era = 0;
            }
            year = this.cdate.getYear();
        }
        this.internalSet(0, era);
        this.internalSet(1, year);
        int mask = fieldMask | 3;
        int month = this.cdate.getMonth() - 1;
        int dayOfMonth = this.cdate.getDayOfMonth();
        if ((fieldMask & 0xA4) != 0) {
            this.internalSet(2, month);
            this.internalSet(5, dayOfMonth);
            this.internalSet(7, this.cdate.getDayOfWeek());
            mask |= 0xA4;
        }
        if ((fieldMask & 0x7E00) != 0) {
            if (timeOfDay != 0) {
                int hours = timeOfDay / 3600000;
                this.internalSet(11, hours);
                this.internalSet(9, hours / 12);
                this.internalSet(10, hours % 12);
                int r = timeOfDay % 3600000;
                this.internalSet(12, r / 60000);
                this.internalSet(13, (r %= 60000) / 1000);
                this.internalSet(14, r % 1000);
            } else {
                this.internalSet(11, 0);
                this.internalSet(9, 0);
                this.internalSet(10, 0);
                this.internalSet(12, 0);
                this.internalSet(13, 0);
                this.internalSet(14, 0);
            }
            mask |= 0x7E00;
        }
        if ((fieldMask & 0x18000) != 0) {
            this.internalSet(15, this.zoneOffsets[0]);
            this.internalSet(16, this.zoneOffsets[1]);
            mask |= 0x18000;
        }
        if ((fieldMask & 0x158) != 0) {
            int normalizedYear = this.cdate.getNormalizedYear();
            long fixedDateJan1 = this.calsys.getFixedDate(normalizedYear, 1, 1, this.cdate);
            int dayOfYear = (int)(fixedDate - fixedDateJan1) + 1;
            long fixedDateMonth1 = fixedDate - (long)dayOfMonth + 1L;
            int cutoverYear = this.calsys == gcal ? this.gregorianCutoverYear : this.gregorianCutoverYearJulian;
            int relativeDayOfMonth = dayOfMonth - 1;
            if (normalizedYear == cutoverYear) {
                if (this.gregorianCutoverYearJulian <= this.gregorianCutoverYear) {
                    fixedDateJan1 = this.getFixedDateJan1(this.cdate, fixedDate);
                    if (fixedDate >= this.gregorianCutoverDate) {
                        fixedDateMonth1 = this.getFixedDateMonth1(this.cdate, fixedDate);
                    }
                }
                dayOfYear = (int)(fixedDate - fixedDateJan1) + 1;
                relativeDayOfMonth = (int)(fixedDate - fixedDateMonth1);
            }
            this.internalSet(6, dayOfYear);
            this.internalSet(8, relativeDayOfMonth / 7 + 1);
            int weekOfYear = this.getWeekNumber(fixedDateJan1, fixedDate);
            if (weekOfYear == 0) {
                long fixedDec31 = fixedDateJan1 - 1L;
                long prevJan1 = fixedDateJan1 - 365L;
                if (normalizedYear > cutoverYear + 1) {
                    if (CalendarUtils.isGregorianLeapYear(normalizedYear - 1)) {
                        --prevJan1;
                    }
                } else if (normalizedYear <= this.gregorianCutoverYearJulian) {
                    if (CalendarUtils.isJulianLeapYear(normalizedYear - 1)) {
                        --prevJan1;
                    }
                } else {
                    BaseCalendar calForJan1 = this.calsys;
                    int prevYear = this.getCalendarDate(fixedDec31).getNormalizedYear();
                    if (prevYear == this.gregorianCutoverYear) {
                        calForJan1 = this.getCutoverCalendarSystem();
                        if (calForJan1 == jcal) {
                            prevJan1 = calForJan1.getFixedDate(prevYear, 1, 1, null);
                        } else {
                            prevJan1 = this.gregorianCutoverDate;
                            calForJan1 = gcal;
                        }
                    } else if (prevYear <= this.gregorianCutoverYearJulian) {
                        calForJan1 = GregorianCalendar.getJulianCalendarSystem();
                        prevJan1 = calForJan1.getFixedDate(prevYear, 1, 1, null);
                    }
                }
                weekOfYear = this.getWeekNumber(prevJan1, fixedDec31);
            } else if (normalizedYear > this.gregorianCutoverYear || normalizedYear < this.gregorianCutoverYearJulian - 1) {
                if (weekOfYear >= 52) {
                    long nextJan1st;
                    int ndays;
                    long nextJan1 = fixedDateJan1 + 365L;
                    if (this.cdate.isLeapYear()) {
                        ++nextJan1;
                    }
                    if ((ndays = (int)((nextJan1st = BaseCalendar.getDayOfWeekDateOnOrBefore(nextJan1 + 6L, this.getFirstDayOfWeek())) - nextJan1)) >= this.getMinimalDaysInFirstWeek() && fixedDate >= nextJan1st - 7L) {
                        weekOfYear = 1;
                    }
                }
            } else {
                long nextJan1;
                BaseCalendar calForJan1 = this.calsys;
                int nextYear = normalizedYear + 1;
                if (nextYear == this.gregorianCutoverYearJulian + 1 && nextYear < this.gregorianCutoverYear) {
                    nextYear = this.gregorianCutoverYear;
                }
                if (nextYear == this.gregorianCutoverYear) {
                    calForJan1 = this.getCutoverCalendarSystem();
                }
                if (nextYear > this.gregorianCutoverYear || this.gregorianCutoverYearJulian == this.gregorianCutoverYear || nextYear == this.gregorianCutoverYearJulian) {
                    nextJan1 = calForJan1.getFixedDate(nextYear, 1, 1, null);
                } else {
                    nextJan1 = this.gregorianCutoverDate;
                    calForJan1 = gcal;
                }
                long nextJan1st = BaseCalendar.getDayOfWeekDateOnOrBefore(nextJan1 + 6L, this.getFirstDayOfWeek());
                int ndays = (int)(nextJan1st - nextJan1);
                if (ndays >= this.getMinimalDaysInFirstWeek() && fixedDate >= nextJan1st - 7L) {
                    weekOfYear = 1;
                }
            }
            this.internalSet(3, weekOfYear);
            this.internalSet(4, this.getWeekNumber(fixedDateMonth1, fixedDate));
            mask |= 0x158;
        }
        return mask;
    }

    private int getWeekNumber(long fixedDay1, long fixedDate) {
        int normalizedDayOfPeriod;
        long fixedDay1st = Gregorian.getDayOfWeekDateOnOrBefore(fixedDay1 + 6L, this.getFirstDayOfWeek());
        int ndays = (int)(fixedDay1st - fixedDay1);
        assert (ndays <= 7);
        if (ndays >= this.getMinimalDaysInFirstWeek()) {
            fixedDay1st -= 7L;
        }
        if ((normalizedDayOfPeriod = (int)(fixedDate - fixedDay1st)) >= 0) {
            return normalizedDayOfPeriod / 7 + 1;
        }
        return CalendarUtils.floorDivide(normalizedDayOfPeriod, 7) + 1;
    }

    /*
     * Unable to fully structure code
     */
    @Override
    protected void computeTime() {
        block32: {
            block33: {
                block34: {
                    block35: {
                        block30: {
                            block31: {
                                if (!this.isLenient()) {
                                    if (this.originalFields == null) {
                                        this.originalFields = new int[17];
                                    }
                                    for (field = 0; field < 17; ++field) {
                                        value = this.internalGet(field);
                                        if (this.isExternallySet(field) && (value < this.getMinimum(field) || value > this.getMaximum(field))) {
                                            throw new IllegalArgumentException(GregorianCalendar.getFieldName(field));
                                        }
                                        this.originalFields[field] = value;
                                    }
                                }
                                fieldMask = this.selectFields();
                                year = this.isSet(1) != false ? this.internalGet(1) : 1970;
                                era = this.internalGetEra();
                                if (era == 0) {
                                    year = 1 - year;
                                } else if (era != 1) {
                                    throw new IllegalArgumentException("Invalid era");
                                }
                                if (year <= 0 && !this.isSet(0)) {
                                    fieldMask |= 1;
                                    this.setFieldsComputed(1);
                                }
                                timeOfDay = 0L;
                                if (GregorianCalendar.isFieldSet(fieldMask, 11)) {
                                    timeOfDay += (long)this.internalGet(11);
                                } else {
                                    timeOfDay += (long)this.internalGet(10);
                                    if (GregorianCalendar.isFieldSet(fieldMask, 9)) {
                                        timeOfDay += (long)(12 * this.internalGet(9));
                                    }
                                }
                                timeOfDay *= 60L;
                                timeOfDay += (long)this.internalGet(12);
                                timeOfDay *= 60L;
                                timeOfDay += (long)this.internalGet(13);
                                timeOfDay *= 1000L;
                                fixedDate = (timeOfDay += (long)this.internalGet(14)) / 86400000L;
                                timeOfDay %= 86400000L;
                                while (timeOfDay < 0L) {
                                    timeOfDay += 86400000L;
                                    --fixedDate;
                                }
                                if (year <= this.gregorianCutoverYear || year <= this.gregorianCutoverYearJulian) break block30;
                                gfd = fixedDate + this.getFixedDate(GregorianCalendar.gcal, year, fieldMask);
                                if (gfd < this.gregorianCutoverDate) break block31;
                                fixedDate = gfd;
                                break block32;
                            }
                            jfd = fixedDate + this.getFixedDate(GregorianCalendar.getJulianCalendarSystem(), year, fieldMask);
                            break block33;
                        }
                        if (year >= this.gregorianCutoverYear || year >= this.gregorianCutoverYearJulian) break block34;
                        jfd = fixedDate + this.getFixedDate(GregorianCalendar.getJulianCalendarSystem(), year, fieldMask);
                        if (jfd >= this.gregorianCutoverDate) break block35;
                        fixedDate = jfd;
                        break block32;
                    }
                    gfd = jfd;
                    break block33;
                }
                jfd = fixedDate + this.getFixedDate(GregorianCalendar.getJulianCalendarSystem(), year, fieldMask);
                gfd = fixedDate + this.getFixedDate(GregorianCalendar.gcal, year, fieldMask);
            }
            if (!GregorianCalendar.isFieldSet(fieldMask, 6) && !GregorianCalendar.isFieldSet(fieldMask, 3)) ** GOTO lbl-1000
            if (this.gregorianCutoverYear == this.gregorianCutoverYearJulian) {
                fixedDate = jfd;
            } else if (year == this.gregorianCutoverYear) {
                fixedDate = gfd;
            } else if (gfd >= this.gregorianCutoverDate) {
                fixedDate = jfd >= this.gregorianCutoverDate ? gfd : (this.calsys == GregorianCalendar.gcal || this.calsys == null ? gfd : jfd);
            } else if (jfd < this.gregorianCutoverDate) {
                fixedDate = jfd;
            } else {
                if (!this.isLenient()) {
                    throw new IllegalArgumentException("the specified date doesn't exist");
                }
                fixedDate = jfd;
            }
        }
        millis = (fixedDate - 719163L) * 86400000L + timeOfDay;
        zone = this.getZone();
        if (this.zoneOffsets == null) {
            this.zoneOffsets = new int[2];
        }
        if ((tzMask = fieldMask & 98304) != 98304) {
            if (zone instanceof ZoneInfo) {
                ((ZoneInfo)zone).getOffsetsByWall(millis, this.zoneOffsets);
            } else {
                gmtOffset = GregorianCalendar.isFieldSet(fieldMask, 15) != false ? this.internalGet(15) : zone.getRawOffset();
                zone.getOffsets(millis - (long)gmtOffset, this.zoneOffsets);
            }
        }
        if (tzMask != 0) {
            if (GregorianCalendar.isFieldSet(tzMask, 15)) {
                this.zoneOffsets[0] = this.internalGet(15);
            }
            if (GregorianCalendar.isFieldSet(tzMask, 16)) {
                this.zoneOffsets[1] = this.internalGet(16);
            }
        }
        this.time = millis -= (long)(this.zoneOffsets[0] + this.zoneOffsets[1]);
        mask = this.computeFields(fieldMask | this.getSetStateFields(), tzMask);
        if (!this.isLenient()) {
            for (field = 0; field < 17; ++field) {
                if (!this.isExternallySet(field) || this.originalFields[field] == this.internalGet(field)) continue;
                s = this.originalFields[field] + " -> " + this.internalGet(field);
                System.arraycopy(this.originalFields, 0, this.fields, 0, this.fields.length);
                throw new IllegalArgumentException(GregorianCalendar.getFieldName(field) + ": " + s);
            }
        }
        this.setFieldsNormalized(mask);
    }

    private long getFixedDate(BaseCalendar cal, int year, int fieldMask) {
        int month = 0;
        if (GregorianCalendar.isFieldSet(fieldMask, 2)) {
            month = this.internalGet(2);
            if (month > 11) {
                year += month / 12;
                month %= 12;
            } else if (month < 0) {
                int[] rem = new int[1];
                year += CalendarUtils.floorDivide(month, 12, rem);
                month = rem[0];
            }
        }
        long fixedDate = cal.getFixedDate(year, month + 1, 1, cal == gcal ? this.gdate : null);
        if (GregorianCalendar.isFieldSet(fieldMask, 2)) {
            if (GregorianCalendar.isFieldSet(fieldMask, 5)) {
                if (this.isSet(5)) {
                    fixedDate += (long)this.internalGet(5);
                    --fixedDate;
                }
            } else if (GregorianCalendar.isFieldSet(fieldMask, 4)) {
                long firstDayOfWeek = BaseCalendar.getDayOfWeekDateOnOrBefore(fixedDate + 6L, this.getFirstDayOfWeek());
                if (firstDayOfWeek - fixedDate >= (long)this.getMinimalDaysInFirstWeek()) {
                    firstDayOfWeek -= 7L;
                }
                if (GregorianCalendar.isFieldSet(fieldMask, 7)) {
                    firstDayOfWeek = BaseCalendar.getDayOfWeekDateOnOrBefore(firstDayOfWeek + 6L, this.internalGet(7));
                }
                fixedDate = firstDayOfWeek + (long)(7 * (this.internalGet(4) - 1));
            } else {
                int dayOfWeek = GregorianCalendar.isFieldSet(fieldMask, 7) ? this.internalGet(7) : this.getFirstDayOfWeek();
                int dowim = GregorianCalendar.isFieldSet(fieldMask, 8) ? this.internalGet(8) : 1;
                if (dowim >= 0) {
                    fixedDate = BaseCalendar.getDayOfWeekDateOnOrBefore(fixedDate + (long)(7 * dowim) - 1L, dayOfWeek);
                } else {
                    int lastDate = this.monthLength(month, year) + 7 * (dowim + 1);
                    fixedDate = BaseCalendar.getDayOfWeekDateOnOrBefore(fixedDate + (long)lastDate - 1L, dayOfWeek);
                }
            }
        } else {
            if (year == this.gregorianCutoverYear && cal == gcal && fixedDate < this.gregorianCutoverDate && this.gregorianCutoverYear != this.gregorianCutoverYearJulian) {
                fixedDate = this.gregorianCutoverDate;
            }
            if (GregorianCalendar.isFieldSet(fieldMask, 6)) {
                fixedDate += (long)this.internalGet(6);
                --fixedDate;
            } else {
                int dayOfWeek;
                long firstDayOfWeek = BaseCalendar.getDayOfWeekDateOnOrBefore(fixedDate + 6L, this.getFirstDayOfWeek());
                if (firstDayOfWeek - fixedDate >= (long)this.getMinimalDaysInFirstWeek()) {
                    firstDayOfWeek -= 7L;
                }
                if (GregorianCalendar.isFieldSet(fieldMask, 7) && (dayOfWeek = this.internalGet(7)) != this.getFirstDayOfWeek()) {
                    firstDayOfWeek = BaseCalendar.getDayOfWeekDateOnOrBefore(firstDayOfWeek + 6L, dayOfWeek);
                }
                fixedDate = firstDayOfWeek + 7L * ((long)this.internalGet(3) - 1L);
            }
        }
        return fixedDate;
    }

    private GregorianCalendar getNormalizedCalendar() {
        GregorianCalendar gc;
        if (this.isFullyNormalized()) {
            gc = this;
        } else {
            gc = (GregorianCalendar)this.clone();
            gc.setLenient(true);
            gc.complete();
        }
        return gc;
    }

    private static synchronized BaseCalendar getJulianCalendarSystem() {
        if (jcal == null) {
            jcal = (JulianCalendar)CalendarSystem.forName("julian");
            jeras = jcal.getEras();
        }
        return jcal;
    }

    private BaseCalendar getCutoverCalendarSystem() {
        if (this.gregorianCutoverYearJulian < this.gregorianCutoverYear) {
            return gcal;
        }
        return GregorianCalendar.getJulianCalendarSystem();
    }

    private boolean isCutoverYear(int normalizedYear) {
        int cutoverYear = this.calsys == gcal ? this.gregorianCutoverYear : this.gregorianCutoverYearJulian;
        return normalizedYear == cutoverYear;
    }

    private boolean isInvalidWeek1() {
        long jan1Fd = gcal.getFixedDate(this.internalGet(1), 1, 1, null);
        int jan1Dow = BaseCalendar.getDayOfWeekFromFixedDate(jan1Fd);
        int daysInFirstWeek = this.getFirstDayOfWeek() <= jan1Dow ? 7 - jan1Dow + this.getFirstDayOfWeek() : this.getFirstDayOfWeek() - jan1Dow;
        int endDow = this.getFirstDayOfWeek() - 1 == 0 ? 7 : this.getFirstDayOfWeek() - 1;
        return daysInFirstWeek >= this.getMinimalDaysInFirstWeek() && !this.dayInMinWeek(this.internalGet(7), jan1Dow, endDow);
    }

    private boolean dayInMinWeek(int day, int startDay, int endDay) {
        if (endDay >= startDay) {
            return day >= startDay && day <= endDay;
        }
        return day >= startDay || day <= endDay;
    }

    private long getFixedDateJan1(BaseCalendar.Date date, long fixedDate) {
        assert (date.getNormalizedYear() == this.gregorianCutoverYear || date.getNormalizedYear() == this.gregorianCutoverYearJulian);
        if (this.gregorianCutoverYear != this.gregorianCutoverYearJulian && fixedDate >= this.gregorianCutoverDate) {
            return this.gregorianCutoverDate;
        }
        BaseCalendar juliancal = GregorianCalendar.getJulianCalendarSystem();
        return juliancal.getFixedDate(date.getNormalizedYear(), 1, 1, null);
    }

    private long getFixedDateMonth1(BaseCalendar.Date date, long fixedDate) {
        long fixedDateMonth1;
        assert (date.getNormalizedYear() == this.gregorianCutoverYear || date.getNormalizedYear() == this.gregorianCutoverYearJulian);
        BaseCalendar.Date gCutover = this.getGregorianCutoverDate();
        if (gCutover.getMonth() == 1 && gCutover.getDayOfMonth() == 1) {
            return fixedDate - (long)date.getDayOfMonth() + 1L;
        }
        if (date.getMonth() == gCutover.getMonth()) {
            BaseCalendar.Date jLastDate = this.getLastJulianDate();
            fixedDateMonth1 = this.gregorianCutoverYear == this.gregorianCutoverYearJulian && gCutover.getMonth() == jLastDate.getMonth() ? jcal.getFixedDate(date.getNormalizedYear(), date.getMonth(), 1, null) : this.gregorianCutoverDate;
        } else {
            fixedDateMonth1 = fixedDate - (long)date.getDayOfMonth() + 1L;
        }
        return fixedDateMonth1;
    }

    private BaseCalendar.Date getCalendarDate(long fd) {
        BaseCalendar cal = fd >= this.gregorianCutoverDate ? gcal : GregorianCalendar.getJulianCalendarSystem();
        BaseCalendar.Date d = (BaseCalendar.Date)cal.newCalendarDate(TimeZone.NO_TIMEZONE);
        cal.getCalendarDateFromFixedDate(d, fd);
        return d;
    }

    private BaseCalendar.Date getGregorianCutoverDate() {
        return this.getCalendarDate(this.gregorianCutoverDate);
    }

    private BaseCalendar.Date getLastJulianDate() {
        return this.getCalendarDate(this.gregorianCutoverDate - 1L);
    }

    private int monthLength(int month, int year) {
        return this.isLeapYear(year) ? LEAP_MONTH_LENGTH[month] : MONTH_LENGTH[month];
    }

    private int monthLength(int month) {
        int year = this.internalGet(1);
        if (this.internalGetEra() == 0) {
            year = 1 - year;
        }
        return this.monthLength(month, year);
    }

    private int actualMonthLength() {
        long fd;
        int year = this.cdate.getNormalizedYear();
        if (year != this.gregorianCutoverYear && year != this.gregorianCutoverYearJulian) {
            return this.calsys.getMonthLength(this.cdate);
        }
        BaseCalendar.Date date = (BaseCalendar.Date)this.cdate.clone();
        long month1 = this.getFixedDateMonth1(date, fd = this.calsys.getFixedDate(date));
        long next1 = month1 + (long)this.calsys.getMonthLength(date);
        if (next1 < this.gregorianCutoverDate) {
            return (int)(next1 - month1);
        }
        if (this.cdate != this.gdate) {
            date = gcal.newCalendarDate(TimeZone.NO_TIMEZONE);
        }
        gcal.getCalendarDateFromFixedDate(date, next1);
        next1 = this.getFixedDateMonth1(date, next1);
        return (int)(next1 - month1);
    }

    private int yearLength(int year) {
        return this.isLeapYear(year) ? 366 : 365;
    }

    private int yearLength() {
        int year = this.internalGet(1);
        if (this.internalGetEra() == 0) {
            year = 1 - year;
        }
        return this.yearLength(year);
    }

    private void pinDayOfMonth() {
        int monthLen;
        int year = this.internalGet(1);
        if (year > this.gregorianCutoverYear || year < this.gregorianCutoverYearJulian) {
            monthLen = this.monthLength(this.internalGet(2));
        } else {
            GregorianCalendar gc = this.getNormalizedCalendar();
            monthLen = gc.getActualMaximum(5);
        }
        int dom = this.internalGet(5);
        if (dom > monthLen) {
            this.set(5, monthLen);
        }
    }

    private long getCurrentFixedDate() {
        return this.calsys == gcal ? this.cachedFixedDate : this.calsys.getFixedDate(this.cdate);
    }

    private static int getRolledValue(int value, int amount, int min, int max) {
        assert (value >= min && value <= max);
        int range = max - min + 1;
        int n = value + (amount %= range);
        if (n > max) {
            n -= range;
        } else if (n < min) {
            n += range;
        }
        assert (n >= min && n <= max);
        return n;
    }

    private int internalGetEra() {
        return this.isSet(0) ? this.internalGet(0) : 1;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (this.gdate == null) {
            this.gdate = gcal.newCalendarDate(this.getZone());
            this.cachedFixedDate = Long.MIN_VALUE;
        }
        this.setGregorianChange(this.gregorianCutover);
    }

    public ZonedDateTime toZonedDateTime() {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(this.getTimeInMillis()), this.getTimeZone().toZoneId());
    }

    public static GregorianCalendar from(ZonedDateTime zdt) {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone(zdt.getZone()));
        cal.setGregorianChange(new Date(Long.MIN_VALUE));
        cal.setFirstDayOfWeek(2);
        cal.setMinimalDaysInFirstWeek(4);
        try {
            cal.setTimeInMillis(Math.addExact(Math.multiplyExact(zdt.toEpochSecond(), 1000), (long)zdt.get(ChronoField.MILLI_OF_SECOND)));
        }
        catch (ArithmeticException ex) {
            throw new IllegalArgumentException(ex);
        }
        return cal;
    }
}

