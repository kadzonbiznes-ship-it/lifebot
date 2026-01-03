/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.time.Instant;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import sun.util.calendar.BaseCalendar;
import sun.util.calendar.CalendarSystem;
import sun.util.calendar.CalendarUtils;
import sun.util.calendar.ZoneInfo;

public class Date
implements Serializable,
Cloneable,
Comparable<Date> {
    private static final BaseCalendar gcal = CalendarSystem.getGregorianCalendar();
    private static BaseCalendar jcal;
    private transient long fastTime;
    private transient BaseCalendar.Date cdate;
    private static int defaultCenturyStart;
    private static final long serialVersionUID = 7523967970034938905L;
    private static final String[] wtb;
    private static final int[] ttb;

    public Date() {
        this(System.currentTimeMillis());
    }

    public Date(long date) {
        this.fastTime = date;
    }

    @Deprecated
    public Date(int year, int month, int date) {
        this(year, month, date, 0, 0, 0);
    }

    @Deprecated
    public Date(int year, int month, int date, int hrs, int min) {
        this(year, month, date, hrs, min, 0);
    }

    @Deprecated
    public Date(int year, int month, int date, int hrs, int min, int sec) {
        int y = year + 1900;
        if (month >= 12) {
            y += month / 12;
            month %= 12;
        } else if (month < 0) {
            y += CalendarUtils.floorDivide(month, 12);
            month = CalendarUtils.mod(month, 12);
        }
        BaseCalendar cal = Date.getCalendarSystem(y);
        this.cdate = (BaseCalendar.Date)cal.newCalendarDate(TimeZone.getDefaultRef());
        this.cdate.setNormalizedDate(y, month + 1, date).setTimeOfDay(hrs, min, sec, 0);
        this.getTimeImpl();
        this.cdate = null;
    }

    @Deprecated
    public Date(String s) {
        this(Date.parse(s));
    }

    public Object clone() {
        Date d = null;
        try {
            d = (Date)super.clone();
            if (this.cdate != null) {
                d.cdate = (BaseCalendar.Date)this.cdate.clone();
            }
        }
        catch (CloneNotSupportedException cloneNotSupportedException) {
            // empty catch block
        }
        return d;
    }

    @Deprecated
    public static long UTC(int year, int month, int date, int hrs, int min, int sec) {
        int y = year + 1900;
        if (month >= 12) {
            y += month / 12;
            month %= 12;
        } else if (month < 0) {
            y += CalendarUtils.floorDivide(month, 12);
            month = CalendarUtils.mod(month, 12);
        }
        int m = month + 1;
        BaseCalendar cal = Date.getCalendarSystem(y);
        BaseCalendar.Date udate = (BaseCalendar.Date)cal.newCalendarDate(null);
        udate.setNormalizedDate(y, m, date).setTimeOfDay(hrs, min, sec, 0);
        Date d = new Date(0L);
        d.normalize(udate);
        return d.fastTime;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    @Deprecated
    public static long parse(String s) {
        int year = Integer.MIN_VALUE;
        int mon = -1;
        int mday = -1;
        int hour = -1;
        int min = -1;
        int sec = -1;
        int millis = -1;
        int c = -1;
        int i = 0;
        int n = -1;
        int wst = -1;
        int tzoffset = -1;
        int prevc = 0;
        if (s == null) {
            throw new IllegalArgumentException();
        }
        int limit = s.length();
        block2: while (true) {
            int st2;
            block52: {
                block51: {
                    block50: {
                        if (i >= limit) break block50;
                        c = s.charAt(i);
                        ++i;
                        if (c <= 32 || c == 44) continue;
                        if (c == 40) break block51;
                        if (48 <= c && c <= 57) {
                            n = c - 48;
                            while (i < limit) {
                                char c2 = s.charAt(i);
                                c = c2;
                                if ('0' > c2 || c > 57) break;
                                n = n * 10 + c - 48;
                                ++i;
                            }
                            if (prevc == 43 || prevc == 45 && year != Integer.MIN_VALUE) {
                                n = n < 24 ? (n *= 60) : n % 100 + n / 100 * 60;
                                if (prevc == 43) {
                                    n = -n;
                                }
                                if (tzoffset != 0 && tzoffset != -1) {
                                    throw new IllegalArgumentException();
                                }
                                tzoffset = n;
                            } else if (n >= 70) {
                                if (year != Integer.MIN_VALUE) {
                                    throw new IllegalArgumentException();
                                }
                                if (c > 32 && c != 44 && c != 47) {
                                    if (i < limit) throw new IllegalArgumentException();
                                }
                                year = n;
                            } else if (c == 58) {
                                if (hour < 0) {
                                    hour = (byte)n;
                                } else {
                                    if (min >= 0) throw new IllegalArgumentException();
                                    min = (byte)n;
                                }
                            } else if (c == 47) {
                                if (mon < 0) {
                                    mon = (byte)(n - 1);
                                } else {
                                    if (mday >= 0) throw new IllegalArgumentException();
                                    mday = (byte)n;
                                }
                            } else {
                                if (i < limit && c != 44 && c > 32 && c != 45) {
                                    throw new IllegalArgumentException();
                                }
                                if (hour >= 0 && min < 0) {
                                    min = (byte)n;
                                } else if (min >= 0 && sec < 0) {
                                    sec = (byte)n;
                                } else if (mday < 0) {
                                    mday = (byte)n;
                                } else {
                                    if (year != Integer.MIN_VALUE) throw new IllegalArgumentException();
                                    if (mon < 0) throw new IllegalArgumentException();
                                    if (mday < 0) throw new IllegalArgumentException();
                                    year = n;
                                }
                            }
                            prevc = 0;
                            continue;
                        }
                        if (c == 47 || c == 58 || c == 43 || c == 45) {
                            prevc = c;
                            continue;
                        }
                        st2 = i - 1;
                        break block52;
                    }
                    if (year == Integer.MIN_VALUE) throw new IllegalArgumentException();
                    if (mon < 0) throw new IllegalArgumentException();
                    if (mday < 0) {
                        throw new IllegalArgumentException();
                    }
                    if (year < 100) {
                        Class<Date> st2 = Date.class;
                        // MONITORENTER : java.util.Date.class
                        if (defaultCenturyStart == 0) {
                            defaultCenturyStart = gcal.getCalendarDate().getYear() - 80;
                        }
                        // MONITOREXIT : st2
                        if ((year += defaultCenturyStart / 100 * 100) < defaultCenturyStart) {
                            year += 100;
                        }
                    }
                    if (sec < 0) {
                        sec = 0;
                    }
                    if (min < 0) {
                        min = 0;
                    }
                    if (hour < 0) {
                        hour = 0;
                    }
                    BaseCalendar cal = Date.getCalendarSystem(year);
                    if (tzoffset == -1) {
                        BaseCalendar.Date ldate = (BaseCalendar.Date)cal.newCalendarDate(TimeZone.getDefaultRef());
                        ldate.setDate(year, mon + 1, mday);
                        ldate.setTimeOfDay(hour, min, sec, 0);
                        return cal.getTime(ldate);
                    }
                    BaseCalendar.Date udate = (BaseCalendar.Date)cal.newCalendarDate(null);
                    udate.setDate(year, mon + 1, mday);
                    udate.setTimeOfDay(hour, min, sec, 0);
                    return cal.getTime(udate) + (long)(tzoffset * 60000);
                }
                int depth = 1;
                while (true) {
                    if (i >= limit) continue block2;
                    c = s.charAt(i);
                    ++i;
                    if (c == 40) {
                        ++depth;
                        continue;
                    }
                    if (c == 41 && --depth <= 0) break;
                }
                continue;
            }
            while (i < limit && (65 <= (c = (int)s.charAt(i)) && c <= 90 || 97 <= c && c <= 122)) {
                ++i;
            }
            if (i <= st2 + 1) {
                throw new IllegalArgumentException();
            }
            int k = wtb.length;
            while (--k >= 0) {
                if (!wtb[k].regionMatches(true, 0, s, st2, i - st2)) continue;
                int action = ttb[k];
                if (action == 0) break;
                if (action == 1) {
                    if (hour > 12) throw new IllegalArgumentException();
                    if (hour < 1) {
                        throw new IllegalArgumentException();
                    }
                    if (hour >= 12) break;
                    hour += 12;
                    break;
                }
                if (action == 14) {
                    if (hour > 12) throw new IllegalArgumentException();
                    if (hour < 1) {
                        throw new IllegalArgumentException();
                    }
                    if (hour != 12) break;
                    hour = 0;
                    break;
                }
                if (action <= 13) {
                    if (mon >= 0) throw new IllegalArgumentException();
                    mon = (byte)(action - 2);
                    break;
                }
                tzoffset = action - 10000;
                break;
            }
            if (k < 0) {
                throw new IllegalArgumentException();
            }
            prevc = 0;
        }
    }

    @Deprecated
    public int getYear() {
        return this.normalize().getYear() - 1900;
    }

    @Deprecated
    public void setYear(int year) {
        this.getCalendarDate().setNormalizedYear(year + 1900);
    }

    @Deprecated
    public int getMonth() {
        return this.normalize().getMonth() - 1;
    }

    @Deprecated
    public void setMonth(int month) {
        int y = 0;
        if (month >= 12) {
            y = month / 12;
            month %= 12;
        } else if (month < 0) {
            y = CalendarUtils.floorDivide(month, 12);
            month = CalendarUtils.mod(month, 12);
        }
        BaseCalendar.Date d = this.getCalendarDate();
        if (y != 0) {
            d.setNormalizedYear(d.getNormalizedYear() + y);
        }
        d.setMonth(month + 1);
    }

    @Deprecated
    public int getDate() {
        return this.normalize().getDayOfMonth();
    }

    @Deprecated
    public void setDate(int date) {
        this.getCalendarDate().setDayOfMonth(date);
    }

    @Deprecated
    public int getDay() {
        return this.normalize().getDayOfWeek() - 1;
    }

    @Deprecated
    public int getHours() {
        return this.normalize().getHours();
    }

    @Deprecated
    public void setHours(int hours) {
        this.getCalendarDate().setHours(hours);
    }

    @Deprecated
    public int getMinutes() {
        return this.normalize().getMinutes();
    }

    @Deprecated
    public void setMinutes(int minutes) {
        this.getCalendarDate().setMinutes(minutes);
    }

    @Deprecated
    public int getSeconds() {
        return this.normalize().getSeconds();
    }

    @Deprecated
    public void setSeconds(int seconds) {
        this.getCalendarDate().setSeconds(seconds);
    }

    public long getTime() {
        return this.getTimeImpl();
    }

    private final long getTimeImpl() {
        if (this.cdate != null && !this.cdate.isNormalized()) {
            this.normalize();
        }
        return this.fastTime;
    }

    public void setTime(long time) {
        this.fastTime = time;
        this.cdate = null;
    }

    public boolean before(Date when) {
        return Date.getMillisOf(this) < Date.getMillisOf(when);
    }

    public boolean after(Date when) {
        return Date.getMillisOf(this) > Date.getMillisOf(when);
    }

    public boolean equals(Object obj) {
        return obj instanceof Date && this.getTime() == ((Date)obj).getTime();
    }

    static final long getMillisOf(Date date) {
        if (date.getClass() != Date.class) {
            return date.getTime();
        }
        if (date.cdate == null || date.cdate.isNormalized()) {
            return date.fastTime;
        }
        BaseCalendar.Date d = (BaseCalendar.Date)date.cdate.clone();
        return gcal.getTime(d);
    }

    @Override
    public int compareTo(Date anotherDate) {
        return Long.compare(Date.getMillisOf(this), Date.getMillisOf(anotherDate));
    }

    public int hashCode() {
        long ht = this.getTime();
        return (int)ht ^ (int)(ht >> 32);
    }

    public String toString() {
        BaseCalendar.Date date = this.normalize();
        StringBuilder sb = new StringBuilder(28);
        int index = date.getDayOfWeek();
        if (index == 1) {
            index = 8;
        }
        Date.convertToAbbr(sb, wtb[index]).append(' ');
        Date.convertToAbbr(sb, wtb[date.getMonth() - 1 + 2 + 7]).append(' ');
        CalendarUtils.sprintf0d(sb, date.getDayOfMonth(), 2).append(' ');
        CalendarUtils.sprintf0d(sb, date.getHours(), 2).append(':');
        CalendarUtils.sprintf0d(sb, date.getMinutes(), 2).append(':');
        CalendarUtils.sprintf0d(sb, date.getSeconds(), 2).append(' ');
        TimeZone zi = date.getZone();
        if (zi != null) {
            sb.append(zi.getDisplayName(date.isDaylightTime(), 0, Locale.US));
        } else {
            sb.append("GMT");
        }
        sb.append(' ').append(date.getYear());
        return sb.toString();
    }

    private static final StringBuilder convertToAbbr(StringBuilder sb, String name) {
        sb.append(Character.toUpperCase(name.charAt(0)));
        sb.append(name.charAt(1)).append(name.charAt(2));
        return sb;
    }

    @Deprecated
    public String toLocaleString() {
        DateFormat formatter = DateFormat.getDateTimeInstance();
        return formatter.format(this);
    }

    @Deprecated
    public String toGMTString() {
        long t = this.getTime();
        BaseCalendar cal = Date.getCalendarSystem(t);
        BaseCalendar.Date date = (BaseCalendar.Date)cal.getCalendarDate(this.getTime(), (TimeZone)null);
        StringBuilder sb = new StringBuilder(32);
        CalendarUtils.sprintf0d(sb, date.getDayOfMonth(), 1).append(' ');
        Date.convertToAbbr(sb, wtb[date.getMonth() - 1 + 2 + 7]).append(' ');
        sb.append(date.getYear()).append(' ');
        CalendarUtils.sprintf0d(sb, date.getHours(), 2).append(':');
        CalendarUtils.sprintf0d(sb, date.getMinutes(), 2).append(':');
        CalendarUtils.sprintf0d(sb, date.getSeconds(), 2);
        sb.append(" GMT");
        return sb.toString();
    }

    @Deprecated
    public int getTimezoneOffset() {
        int zoneOffset;
        if (this.cdate == null) {
            TimeZone tz = TimeZone.getDefaultRef();
            zoneOffset = tz instanceof ZoneInfo ? ((ZoneInfo)tz).getOffsets(this.fastTime, null) : tz.getOffset(this.fastTime);
        } else {
            this.normalize();
            zoneOffset = this.cdate.getZoneOffset();
        }
        return -zoneOffset / 60000;
    }

    private final BaseCalendar.Date getCalendarDate() {
        if (this.cdate == null) {
            BaseCalendar cal = Date.getCalendarSystem(this.fastTime);
            this.cdate = (BaseCalendar.Date)cal.getCalendarDate(this.fastTime, TimeZone.getDefaultRef());
        }
        return this.cdate;
    }

    private final BaseCalendar.Date normalize() {
        TimeZone tz;
        if (this.cdate == null) {
            BaseCalendar cal = Date.getCalendarSystem(this.fastTime);
            this.cdate = (BaseCalendar.Date)cal.getCalendarDate(this.fastTime, TimeZone.getDefaultRef());
            return this.cdate;
        }
        if (!this.cdate.isNormalized()) {
            this.cdate = this.normalize(this.cdate);
        }
        if ((tz = TimeZone.getDefaultRef()) != this.cdate.getZone()) {
            this.cdate.setZone(tz);
            BaseCalendar cal = Date.getCalendarSystem(this.cdate);
            ((CalendarSystem)cal).getCalendarDate(this.fastTime, this.cdate);
        }
        return this.cdate;
    }

    private final BaseCalendar.Date normalize(BaseCalendar.Date date) {
        int y = date.getNormalizedYear();
        int m = date.getMonth();
        int d = date.getDayOfMonth();
        int hh = date.getHours();
        int mm = date.getMinutes();
        int ss = date.getSeconds();
        int ms = date.getMillis();
        TimeZone tz = date.getZone();
        if (y == 1582 || y > 280000000 || y < -280000000) {
            if (tz == null) {
                tz = TimeZone.getTimeZone("GMT");
            }
            GregorianCalendar gc = new GregorianCalendar(tz);
            gc.clear();
            gc.set(14, ms);
            gc.set(y, m - 1, d, hh, mm, ss);
            this.fastTime = gc.getTimeInMillis();
            BaseCalendar cal = Date.getCalendarSystem(this.fastTime);
            date = (BaseCalendar.Date)cal.getCalendarDate(this.fastTime, tz);
            return date;
        }
        BaseCalendar cal = Date.getCalendarSystem(y);
        if (cal != Date.getCalendarSystem(date)) {
            date = (BaseCalendar.Date)cal.newCalendarDate(tz);
            date.setNormalizedDate(y, m, d).setTimeOfDay(hh, mm, ss, ms);
        }
        this.fastTime = cal.getTime(date);
        BaseCalendar ncal = Date.getCalendarSystem(this.fastTime);
        if (ncal != cal) {
            date = (BaseCalendar.Date)ncal.newCalendarDate(tz);
            date.setNormalizedDate(y, m, d).setTimeOfDay(hh, mm, ss, ms);
            this.fastTime = ncal.getTime(date);
        }
        return date;
    }

    private static final BaseCalendar getCalendarSystem(int year) {
        if (year >= 1582) {
            return gcal;
        }
        return Date.getJulianCalendar();
    }

    private static final BaseCalendar getCalendarSystem(long utc) {
        if (utc >= 0L || utc >= -12219292800000L - (long)TimeZone.getDefaultRef().getOffset(utc)) {
            return gcal;
        }
        return Date.getJulianCalendar();
    }

    private static final BaseCalendar getCalendarSystem(BaseCalendar.Date cdate) {
        if (jcal == null) {
            return gcal;
        }
        if (cdate.getEra() != null) {
            return jcal;
        }
        return gcal;
    }

    private static final synchronized BaseCalendar getJulianCalendar() {
        if (jcal == null) {
            jcal = (BaseCalendar)CalendarSystem.forName("julian");
        }
        return jcal;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeLong(this.getTimeImpl());
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.fastTime = s.readLong();
    }

    public static Date from(Instant instant) {
        try {
            return new Date(instant.toEpochMilli());
        }
        catch (ArithmeticException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public Instant toInstant() {
        return Instant.ofEpochMilli(this.getTime());
    }

    static {
        wtb = new String[]{"am", "pm", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december", "gmt", "ut", "utc", "est", "edt", "cst", "cdt", "mst", "mdt", "pst", "pdt"};
        ttb = new int[]{14, 1, 0, 0, 0, 0, 0, 0, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 10000, 10000, 10000, 10300, 10240, 10360, 10300, 10420, 10360, 10480, 10420};
    }
}

