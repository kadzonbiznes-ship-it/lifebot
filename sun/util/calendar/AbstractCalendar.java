/*
 * Decompiled with CFR 0.152.
 */
package sun.util.calendar;

import java.util.TimeZone;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.CalendarSystem;
import sun.util.calendar.CalendarUtils;
import sun.util.calendar.Era;
import sun.util.calendar.ZoneInfo;

public abstract class AbstractCalendar
extends CalendarSystem {
    static final int SECOND_IN_MILLIS = 1000;
    static final int MINUTE_IN_MILLIS = 60000;
    static final int HOUR_IN_MILLIS = 3600000;
    static final int DAY_IN_MILLIS = 86400000;
    static final int EPOCH_OFFSET = 719163;
    private Era[] eras;

    protected AbstractCalendar() {
    }

    @Override
    public Era getEra(String eraName) {
        if (this.eras != null) {
            for (Era era : this.eras) {
                if (!era.getName().equals(eraName)) continue;
                return era;
            }
        }
        return null;
    }

    @Override
    public Era[] getEras() {
        Era[] e = null;
        if (this.eras != null) {
            e = new Era[this.eras.length];
            System.arraycopy(this.eras, 0, e, 0, this.eras.length);
        }
        return e;
    }

    protected void setEras(Era[] eras) {
        this.eras = eras;
    }

    @Override
    public CalendarDate getCalendarDate() {
        return this.getCalendarDate(System.currentTimeMillis(), this.newCalendarDate());
    }

    @Override
    public CalendarDate getCalendarDate(long millis) {
        return this.getCalendarDate(millis, this.newCalendarDate());
    }

    @Override
    public CalendarDate getCalendarDate(long millis, TimeZone zone) {
        CalendarDate date = this.newCalendarDate(zone);
        return this.getCalendarDate(millis, date);
    }

    @Override
    public CalendarDate getCalendarDate(long millis, CalendarDate date) {
        int ms = 0;
        int zoneOffset = 0;
        int saving = 0;
        long days = 0L;
        TimeZone zi = date.getZone();
        if (zi != null) {
            int[] offsets = new int[2];
            if (zi instanceof ZoneInfo) {
                zoneOffset = ((ZoneInfo)zi).getOffsets(millis, offsets);
            } else {
                zoneOffset = zi.getOffset(millis);
                offsets[0] = zi.getRawOffset();
                offsets[1] = zoneOffset - offsets[0];
            }
            days = zoneOffset / 86400000;
            ms = zoneOffset % 86400000;
            saving = offsets[1];
        }
        date.setZoneOffset(zoneOffset);
        date.setDaylightSaving(saving);
        days += millis / 86400000L;
        if ((ms += (int)(millis % 86400000L)) >= 86400000) {
            ms -= 86400000;
            ++days;
        } else {
            while (ms < 0) {
                ms += 86400000;
                --days;
            }
        }
        this.getCalendarDateFromFixedDate(date, days += 719163L);
        this.setTimeOfDay(date, ms);
        date.setLeapYear(this.isLeapYear(date));
        date.setNormalized(true);
        return date;
    }

    @Override
    public long getTime(CalendarDate date) {
        long gd = this.getFixedDate(date);
        long ms = (gd - 719163L) * 86400000L + this.getTimeOfDay(date);
        int zoneOffset = 0;
        TimeZone zi = date.getZone();
        if (zi != null) {
            if (date.isNormalized()) {
                return ms - (long)date.getZoneOffset();
            }
            int[] offsets = new int[2];
            if (date.isStandardTime()) {
                if (zi instanceof ZoneInfo) {
                    ((ZoneInfo)zi).getOffsetsByStandard(ms, offsets);
                    zoneOffset = offsets[0];
                } else {
                    zoneOffset = zi.getOffset(ms - (long)zi.getRawOffset());
                }
            } else {
                zoneOffset = zi instanceof ZoneInfo ? ((ZoneInfo)zi).getOffsetsByWall(ms, offsets) : zi.getOffset(ms - (long)zi.getRawOffset());
            }
        }
        this.getCalendarDate(ms -= (long)zoneOffset, date);
        return ms;
    }

    protected long getTimeOfDay(CalendarDate date) {
        long fraction = date.getTimeOfDay();
        if (fraction != Long.MIN_VALUE) {
            return fraction;
        }
        fraction = this.getTimeOfDayValue(date);
        date.setTimeOfDay(fraction);
        return fraction;
    }

    public long getTimeOfDayValue(CalendarDate date) {
        long fraction = date.getHours();
        fraction *= 60L;
        fraction += (long)date.getMinutes();
        fraction *= 60L;
        fraction += (long)date.getSeconds();
        fraction *= 1000L;
        return fraction += (long)date.getMillis();
    }

    @Override
    public CalendarDate setTimeOfDay(CalendarDate cdate, int fraction) {
        if (fraction < 0) {
            throw new IllegalArgumentException();
        }
        boolean normalizedState = cdate.isNormalized();
        int time = fraction;
        int hours = time / 3600000;
        int minutes = (time %= 3600000) / 60000;
        int seconds = (time %= 60000) / 1000;
        time %= 1000;
        cdate.setHours(hours);
        cdate.setMinutes(minutes);
        cdate.setSeconds(seconds);
        cdate.setMillis(time);
        cdate.setTimeOfDay(fraction);
        if (hours < 24 && normalizedState) {
            cdate.setNormalized(normalizedState);
        }
        return cdate;
    }

    protected abstract boolean isLeapYear(CalendarDate var1);

    @Override
    public CalendarDate getNthDayOfWeek(int nth, int dayOfWeek, CalendarDate date) {
        CalendarDate ndate = (CalendarDate)date.clone();
        this.normalize(ndate);
        long fd = this.getFixedDate(ndate);
        long nfd = nth > 0 ? (long)(7 * nth) + AbstractCalendar.getDayOfWeekDateBefore(fd, dayOfWeek) : (long)(7 * nth) + AbstractCalendar.getDayOfWeekDateAfter(fd, dayOfWeek);
        this.getCalendarDateFromFixedDate(ndate, nfd);
        return ndate;
    }

    static long getDayOfWeekDateBefore(long fixedDate, int dayOfWeek) {
        return AbstractCalendar.getDayOfWeekDateOnOrBefore(fixedDate - 1L, dayOfWeek);
    }

    static long getDayOfWeekDateAfter(long fixedDate, int dayOfWeek) {
        return AbstractCalendar.getDayOfWeekDateOnOrBefore(fixedDate + 7L, dayOfWeek);
    }

    public static long getDayOfWeekDateOnOrBefore(long fixedDate, int dayOfWeek) {
        long fd = fixedDate - (long)(dayOfWeek - 1);
        if (fd >= 0L) {
            return fixedDate - fd % 7L;
        }
        return fixedDate - CalendarUtils.mod(fd, 7L);
    }

    protected abstract long getFixedDate(CalendarDate var1);

    protected abstract void getCalendarDateFromFixedDate(CalendarDate var1, long var2);

    public boolean validateTime(CalendarDate date) {
        int t = date.getHours();
        if (t < 0 || t >= 24) {
            return false;
        }
        t = date.getMinutes();
        if (t < 0 || t >= 60) {
            return false;
        }
        t = date.getSeconds();
        if (t < 0 || t >= 60) {
            return false;
        }
        t = date.getMillis();
        return t >= 0 && t < 1000;
    }

    int normalizeTime(CalendarDate date) {
        long fraction = this.getTimeOfDay(date);
        long days = 0L;
        if (fraction >= 86400000L) {
            days = fraction / 86400000L;
            fraction %= 86400000L;
        } else if (fraction < 0L && (days = CalendarUtils.floorDivide(fraction, 86400000L)) != 0L) {
            fraction -= 86400000L * days;
        }
        if (days != 0L) {
            date.setTimeOfDay(fraction);
        }
        date.setMillis((int)(fraction % 1000L));
        date.setSeconds((int)((fraction /= 1000L) % 60L));
        date.setMinutes((int)((fraction /= 60L) % 60L));
        date.setHours((int)(fraction / 60L));
        return (int)days;
    }
}

