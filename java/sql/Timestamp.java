/*
 * Decompiled with CFR 0.152.
 */
package java.sql;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;

public class Timestamp
extends java.util.Date {
    private int nanos;
    static final long serialVersionUID = 2745179027874758501L;
    private static final int MILLIS_PER_SECOND = 1000;

    @Deprecated(since="1.2")
    public Timestamp(int year, int month, int date, int hour, int minute, int second, int nano) {
        super(year, month, date, hour, minute, second);
        if (nano > 999999999 || nano < 0) {
            throw new IllegalArgumentException("nanos > 999999999 or < 0");
        }
        this.nanos = nano;
    }

    public Timestamp(long time) {
        super(time / 1000L * 1000L);
        this.nanos = (int)(time % 1000L * 1000000L);
        if (this.nanos < 0) {
            this.nanos = 1000000000 + this.nanos;
            super.setTime((time / 1000L - 1L) * 1000L);
        }
    }

    @Override
    public void setTime(long time) {
        super.setTime(time / 1000L * 1000L);
        this.nanos = (int)(time % 1000L * 1000000L);
        if (this.nanos < 0) {
            this.nanos = 1000000000 + this.nanos;
            super.setTime((time / 1000L - 1L) * 1000L);
        }
    }

    @Override
    public long getTime() {
        long time = super.getTime();
        return time + (long)(this.nanos / 1000000);
    }

    public static Timestamp valueOf(String s) {
        int second;
        int minute;
        int hour;
        int YEAR_LENGTH = 4;
        int MONTH_LENGTH = 2;
        int DAY_LENGTH = 2;
        int MAX_MONTH = 12;
        int MAX_DAY = 31;
        int year = 0;
        int month = 0;
        int day = 0;
        int a_nanos = 0;
        String formatError = "Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]";
        if (s == null) {
            throw new IllegalArgumentException("null string");
        }
        int dividingSpace = (s = s.trim()).indexOf(32);
        if (dividingSpace < 0) {
            throw new IllegalArgumentException(formatError);
        }
        int firstDash = s.indexOf(45);
        int secondDash = s.indexOf(45, firstDash + 1);
        int firstColon = s.indexOf(58, dividingSpace + 1);
        int secondColon = s.indexOf(58, firstColon + 1);
        int period = s.indexOf(46, secondColon + 1);
        boolean parsedDate = false;
        if (firstDash > 0 && secondDash > 0 && secondDash < dividingSpace - 1 && firstDash == 4 && secondDash - firstDash > 1 && secondDash - firstDash <= 3 && dividingSpace - secondDash > 1 && dividingSpace - secondDash <= 3) {
            year = Integer.parseInt(s, 0, firstDash, 10);
            month = Integer.parseInt(s, firstDash + 1, secondDash, 10);
            day = Integer.parseInt(s, secondDash + 1, dividingSpace, 10);
            if (month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                parsedDate = true;
            }
        }
        if (!parsedDate) {
            throw new IllegalArgumentException(formatError);
        }
        int len = s.length();
        if (firstColon > 0 && secondColon > 0 && secondColon < len - 1) {
            hour = Integer.parseInt(s, dividingSpace + 1, firstColon, 10);
            minute = Integer.parseInt(s, firstColon + 1, secondColon, 10);
            if (period > 0 && period < len - 1) {
                second = Integer.parseInt(s, secondColon + 1, period, 10);
                int nanoPrecision = len - (period + 1);
                if (nanoPrecision > 9) {
                    throw new IllegalArgumentException(formatError);
                }
                if (!Character.isDigit(s.charAt(period + 1))) {
                    throw new IllegalArgumentException(formatError);
                }
                int tmpNanos = Integer.parseInt(s, period + 1, len, 10);
                while (nanoPrecision < 9) {
                    tmpNanos *= 10;
                    ++nanoPrecision;
                }
                a_nanos = tmpNanos;
            } else {
                if (period > 0) {
                    throw new IllegalArgumentException(formatError);
                }
                second = Integer.parseInt(s, secondColon + 1, len, 10);
            }
        } else {
            throw new IllegalArgumentException(formatError);
        }
        return new Timestamp(year - 1900, month - 1, day, hour, minute, second, a_nanos);
    }

    @Override
    public String toString() {
        int year = super.getYear() + 1900;
        int month = super.getMonth() + 1;
        int day = super.getDate();
        int hour = super.getHours();
        int minute = super.getMinutes();
        int second = super.getSeconds();
        int trailingZeros = 0;
        int tmpNanos = this.nanos;
        if (tmpNanos == 0) {
            trailingZeros = 8;
        } else {
            while (tmpNanos % 10 == 0) {
                tmpNanos /= 10;
                ++trailingZeros;
            }
        }
        int count = 10000;
        int yearSize = 4;
        while (year >= count) {
            ++yearSize;
            if ((count *= 10) < 1000000000) continue;
        }
        char[] buf = new char[25 + yearSize - trailingZeros];
        Date.formatDecimalInt(year, buf, 0, yearSize);
        buf[yearSize] = 45;
        Date.formatDecimalInt(month, buf, yearSize + 1, 2);
        buf[yearSize + 3] = 45;
        Date.formatDecimalInt(day, buf, yearSize + 4, 2);
        buf[yearSize + 6] = 32;
        Date.formatDecimalInt(hour, buf, yearSize + 7, 2);
        buf[yearSize + 9] = 58;
        Date.formatDecimalInt(minute, buf, yearSize + 10, 2);
        buf[yearSize + 12] = 58;
        Date.formatDecimalInt(second, buf, yearSize + 13, 2);
        buf[yearSize + 15] = 46;
        Date.formatDecimalInt(tmpNanos, buf, yearSize + 16, 9 - trailingZeros);
        return new String(buf);
    }

    public int getNanos() {
        return this.nanos;
    }

    public void setNanos(int n) {
        if (n > 999999999 || n < 0) {
            throw new IllegalArgumentException("nanos > 999999999 or < 0");
        }
        this.nanos = n;
    }

    public boolean equals(Timestamp ts) {
        if (super.equals(ts)) {
            return this.nanos == ts.nanos;
        }
        return false;
    }

    @Override
    public boolean equals(Object ts) {
        if (ts instanceof Timestamp) {
            return this.equals((Timestamp)ts);
        }
        return false;
    }

    public boolean before(Timestamp ts) {
        return this.compareTo(ts) < 0;
    }

    public boolean after(Timestamp ts) {
        return this.compareTo(ts) > 0;
    }

    @Override
    public int compareTo(Timestamp ts) {
        int i;
        long anotherTime;
        long thisTime = this.getTime();
        int n = thisTime < (anotherTime = ts.getTime()) ? -1 : (i = thisTime == anotherTime ? 0 : 1);
        if (i == 0) {
            if (this.nanos > ts.nanos) {
                return 1;
            }
            if (this.nanos < ts.nanos) {
                return -1;
            }
        }
        return i;
    }

    @Override
    public int compareTo(java.util.Date o) {
        if (o instanceof Timestamp) {
            return this.compareTo((Timestamp)o);
        }
        Timestamp ts = new Timestamp(o.getTime());
        return this.compareTo(ts);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public static Timestamp valueOf(LocalDateTime dateTime) {
        return new Timestamp(dateTime.getYear() - 1900, dateTime.getMonthValue() - 1, dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond(), dateTime.getNano());
    }

    public LocalDateTime toLocalDateTime() {
        return LocalDateTime.of(this.getYear() + 1900, this.getMonth() + 1, this.getDate(), this.getHours(), this.getMinutes(), this.getSeconds(), this.getNanos());
    }

    public static Timestamp from(Instant instant) {
        try {
            Timestamp stamp = new Timestamp(instant.getEpochSecond() * 1000L);
            stamp.nanos = instant.getNano();
            return stamp;
        }
        catch (ArithmeticException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public Instant toInstant() {
        return Instant.ofEpochSecond(super.getTime() / 1000L, this.nanos);
    }
}

