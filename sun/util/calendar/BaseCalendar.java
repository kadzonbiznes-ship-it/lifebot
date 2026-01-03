/*
 * Decompiled with CFR 0.152.
 */
package sun.util.calendar;

import java.util.TimeZone;
import sun.util.calendar.AbstractCalendar;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.CalendarUtils;

public abstract class BaseCalendar
extends AbstractCalendar {
    public static final int JANUARY = 1;
    public static final int FEBRUARY = 2;
    public static final int MARCH = 3;
    public static final int APRIL = 4;
    public static final int MAY = 5;
    public static final int JUNE = 6;
    public static final int JULY = 7;
    public static final int AUGUST = 8;
    public static final int SEPTEMBER = 9;
    public static final int OCTOBER = 10;
    public static final int NOVEMBER = 11;
    public static final int DECEMBER = 12;
    public static final int SUNDAY = 1;
    public static final int MONDAY = 2;
    public static final int TUESDAY = 3;
    public static final int WEDNESDAY = 4;
    public static final int THURSDAY = 5;
    public static final int FRIDAY = 6;
    public static final int SATURDAY = 7;
    private static final int BASE_YEAR = 1970;
    private static final int[] FIXED_DATES = new int[]{719163, 719528, 719893, 720259, 720624, 720989, 721354, 721720, 722085, 722450, 722815, 723181, 723546, 723911, 724276, 724642, 725007, 725372, 725737, 726103, 726468, 726833, 727198, 727564, 727929, 728294, 728659, 729025, 729390, 729755, 730120, 730486, 730851, 731216, 731581, 731947, 732312, 732677, 733042, 733408, 733773, 734138, 734503, 734869, 735234, 735599, 735964, 736330, 736695, 737060, 737425, 737791, 738156, 738521, 738886, 739252, 739617, 739982, 740347, 740713, 741078, 741443, 741808, 742174, 742539, 742904, 743269, 743635, 744000, 744365};
    static final int[] DAYS_IN_MONTH = new int[]{31, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    static final int[] ACCUMULATED_DAYS_IN_MONTH = new int[]{-30, 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
    static final int[] ACCUMULATED_DAYS_IN_MONTH_LEAP = new int[]{-30, 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335};

    @Override
    public boolean validate(CalendarDate date) {
        Date bdate = (Date)date;
        if (bdate.isNormalized()) {
            return true;
        }
        int month = bdate.getMonth();
        if (month < 1 || month > 12) {
            return false;
        }
        int d = bdate.getDayOfMonth();
        if (d <= 0 || d > this.getMonthLength(bdate.getNormalizedYear(), month)) {
            return false;
        }
        int dow = bdate.getDayOfWeek();
        if (dow != Integer.MIN_VALUE && dow != this.getDayOfWeek(bdate)) {
            return false;
        }
        if (!this.validateTime(date)) {
            return false;
        }
        bdate.setNormalized(true);
        return true;
    }

    @Override
    public boolean normalize(CalendarDate date) {
        if (date.isNormalized()) {
            return true;
        }
        Date bdate = (Date)date;
        TimeZone zi = bdate.getZone();
        if (zi != null) {
            this.getTime(date);
            return true;
        }
        int days = this.normalizeTime(bdate);
        this.normalizeMonth(bdate);
        long d = (long)bdate.getDayOfMonth() + (long)days;
        int m = bdate.getMonth();
        int y = bdate.getNormalizedYear();
        int ml = this.getMonthLength(y, m);
        if (d <= 0L || d > (long)ml) {
            if (d <= 0L && d > -28L) {
                ml = this.getMonthLength(y, --m);
                bdate.setDayOfMonth((int)(d += (long)ml));
                if (m == 0) {
                    m = 12;
                    bdate.setNormalizedYear(y - 1);
                }
                bdate.setMonth(m);
            } else if (d > (long)ml && d < (long)(ml + 28)) {
                bdate.setDayOfMonth((int)(d -= (long)ml));
                if (++m > 12) {
                    bdate.setNormalizedYear(y + 1);
                    m = 1;
                }
                bdate.setMonth(m);
            } else {
                long fixedDate = d + this.getFixedDate(y, m, 1, bdate) - 1L;
                this.getCalendarDateFromFixedDate(bdate, fixedDate);
            }
        } else {
            bdate.setDayOfWeek(this.getDayOfWeek(bdate));
        }
        date.setLeapYear(this.isLeapYear(bdate.getNormalizedYear()));
        date.setZoneOffset(0);
        date.setDaylightSaving(0);
        bdate.setNormalized(true);
        return true;
    }

    void normalizeMonth(CalendarDate date) {
        Date bdate = (Date)date;
        int year = bdate.getNormalizedYear();
        long month = bdate.getMonth();
        if (month <= 0L) {
            long xm = 1L - month;
            year -= (int)(xm / 12L + 1L);
            month = 13L - xm % 12L;
            if (month == 13L) {
                ++year;
                month = 1L;
            }
            bdate.setNormalizedYear(year);
            bdate.setMonth((int)month);
        } else if (month > 12L) {
            year += (int)((month - 1L) / 12L);
            month = (month - 1L) % 12L + 1L;
            bdate.setNormalizedYear(year);
            bdate.setMonth((int)month);
        }
    }

    @Override
    public int getYearLength(CalendarDate date) {
        return this.isLeapYear(((Date)date).getNormalizedYear()) ? 366 : 365;
    }

    @Override
    public int getMonthLength(CalendarDate date) {
        Date gdate = (Date)date;
        int month = gdate.getMonth();
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Illegal month value: " + month);
        }
        return this.getMonthLength(gdate.getNormalizedYear(), month);
    }

    private int getMonthLength(int year, int month) {
        int days = DAYS_IN_MONTH[month];
        if (month == 2 && this.isLeapYear(year)) {
            ++days;
        }
        return days;
    }

    public long getDayOfYear(CalendarDate date) {
        return this.getDayOfYear(((Date)date).getNormalizedYear(), date.getMonth(), date.getDayOfMonth());
    }

    final long getDayOfYear(int year, int month, int dayOfMonth) {
        return (long)dayOfMonth + (long)(this.isLeapYear(year) ? ACCUMULATED_DAYS_IN_MONTH_LEAP[month] : ACCUMULATED_DAYS_IN_MONTH[month]);
    }

    @Override
    public long getFixedDate(CalendarDate date) {
        if (!date.isNormalized()) {
            this.normalizeMonth(date);
        }
        return this.getFixedDate(((Date)date).getNormalizedYear(), date.getMonth(), date.getDayOfMonth(), (Date)date);
    }

    public long getFixedDate(int year, int month, int dayOfMonth, Date cache) {
        boolean isJan1;
        boolean bl = isJan1 = month == 1 && dayOfMonth == 1;
        if (cache != null && cache.hit(year)) {
            if (isJan1) {
                return cache.getCachedJan1();
            }
            return cache.getCachedJan1() + this.getDayOfYear(year, month, dayOfMonth) - 1L;
        }
        int n = year - 1970;
        if (n >= 0 && n < FIXED_DATES.length) {
            long jan1 = FIXED_DATES[n];
            if (cache != null) {
                cache.setCache(year, jan1, this.isLeapYear(year) ? 366 : 365);
            }
            return isJan1 ? jan1 : jan1 + this.getDayOfYear(year, month, dayOfMonth) - 1L;
        }
        long prevyear = (long)year - 1L;
        long days = dayOfMonth;
        days = prevyear >= 0L ? (days += 365L * prevyear + prevyear / 4L - prevyear / 100L + prevyear / 400L + (long)((367 * month - 362) / 12)) : (days += 365L * prevyear + CalendarUtils.floorDivide(prevyear, 4L) - CalendarUtils.floorDivide(prevyear, 100L) + CalendarUtils.floorDivide(prevyear, 400L) + (long)CalendarUtils.floorDivide(367 * month - 362, 12));
        if (month > 2) {
            days -= this.isLeapYear(year) ? 1L : 2L;
        }
        if (cache != null && isJan1) {
            cache.setCache(year, days, this.isLeapYear(year) ? 366 : 365);
        }
        return days;
    }

    @Override
    public void getCalendarDateFromFixedDate(CalendarDate date, long fixedDate) {
        int month;
        boolean isLeap;
        long jan1;
        int year;
        Date gdate = (Date)date;
        if (gdate.hit(fixedDate)) {
            year = gdate.getCachedYear();
            jan1 = gdate.getCachedJan1();
            isLeap = this.isLeapYear(year);
        } else {
            year = this.getGregorianYearFromFixedDate(fixedDate);
            jan1 = this.getFixedDate(year, 1, 1, null);
            isLeap = this.isLeapYear(year);
            gdate.setCache(year, jan1, isLeap ? 366 : 365);
        }
        int priorDays = (int)(fixedDate - jan1);
        long mar1 = jan1 + 31L + 28L;
        if (isLeap) {
            ++mar1;
        }
        if (fixedDate >= mar1) {
            priorDays += isLeap ? 1 : 2;
        }
        month = (month = 12 * priorDays + 373) > 0 ? (month /= 367) : CalendarUtils.floorDivide(month, 367);
        long month1 = jan1 + (long)ACCUMULATED_DAYS_IN_MONTH[month];
        if (isLeap && month >= 3) {
            ++month1;
        }
        int dayOfMonth = (int)(fixedDate - month1) + 1;
        int dayOfWeek = BaseCalendar.getDayOfWeekFromFixedDate(fixedDate);
        assert (dayOfWeek > 0) : "negative day of week " + dayOfWeek;
        gdate.setNormalizedYear(year);
        gdate.setMonth(month);
        gdate.setDayOfMonth(dayOfMonth);
        gdate.setDayOfWeek(dayOfWeek);
        gdate.setLeapYear(isLeap);
        gdate.setNormalized(true);
    }

    public int getDayOfWeek(CalendarDate date) {
        long fixedDate = this.getFixedDate(date);
        return BaseCalendar.getDayOfWeekFromFixedDate(fixedDate);
    }

    public static final int getDayOfWeekFromFixedDate(long fixedDate) {
        if (fixedDate >= 0L) {
            return (int)(fixedDate % 7L) + 1;
        }
        return (int)CalendarUtils.mod(fixedDate, 7L) + 1;
    }

    public int getYearFromFixedDate(long fixedDate) {
        return this.getGregorianYearFromFixedDate(fixedDate);
    }

    final int getGregorianYearFromFixedDate(long fixedDate) {
        int n1;
        int n4;
        int n100;
        int n400;
        if (fixedDate > 0L) {
            long d0 = fixedDate - 1L;
            n400 = (int)(d0 / 146097L);
            int d1 = (int)(d0 % 146097L);
            n100 = d1 / 36524;
            int d2 = d1 % 36524;
            n4 = d2 / 1461;
            int d3 = d2 % 1461;
            n1 = d3 / 365;
        } else {
            long d0 = fixedDate - 1L;
            n400 = (int)CalendarUtils.floorDivide(d0, 146097L);
            int d1 = (int)CalendarUtils.mod(d0, 146097L);
            n100 = CalendarUtils.floorDivide(d1, 36524);
            int d2 = CalendarUtils.mod(d1, 36524);
            n4 = CalendarUtils.floorDivide(d2, 1461);
            int d3 = CalendarUtils.mod(d2, 1461);
            n1 = CalendarUtils.floorDivide(d3, 365);
        }
        int year = 400 * n400 + 100 * n100 + 4 * n4 + n1;
        if (n100 != 4 && n1 != 4) {
            ++year;
        }
        return year;
    }

    @Override
    protected boolean isLeapYear(CalendarDate date) {
        return this.isLeapYear(((Date)date).getNormalizedYear());
    }

    boolean isLeapYear(int normalizedYear) {
        return CalendarUtils.isGregorianLeapYear(normalizedYear);
    }

    public static abstract class Date
    extends CalendarDate {
        int cachedYear = 2004;
        long cachedFixedDateJan1 = 731581L;
        long cachedFixedDateNextJan1 = this.cachedFixedDateJan1 + 366L;

        protected Date() {
        }

        protected Date(TimeZone zone) {
            super(zone);
        }

        public Date setNormalizedDate(int normalizedYear, int month, int dayOfMonth) {
            this.setNormalizedYear(normalizedYear);
            this.setMonth(month).setDayOfMonth(dayOfMonth);
            return this;
        }

        public abstract int getNormalizedYear();

        public abstract void setNormalizedYear(int var1);

        protected final boolean hit(int year) {
            return year == this.cachedYear;
        }

        protected final boolean hit(long fixedDate) {
            return fixedDate >= this.cachedFixedDateJan1 && fixedDate < this.cachedFixedDateNextJan1;
        }

        protected int getCachedYear() {
            return this.cachedYear;
        }

        protected long getCachedJan1() {
            return this.cachedFixedDateJan1;
        }

        protected void setCache(int year, long jan1, int len) {
            this.cachedYear = year;
            this.cachedFixedDateJan1 = jan1;
            this.cachedFixedDateNextJan1 = jan1 + (long)len;
        }
    }
}

