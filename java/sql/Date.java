/*
 * Decompiled with CFR 0.152.
 */
package java.sql;

import java.time.Instant;
import java.time.LocalDate;

public class Date
extends java.util.Date {
    static final long serialVersionUID = 1511598038487230103L;

    @Deprecated(since="1.2")
    public Date(int year, int month, int day) {
        super(year, month, day);
    }

    public Date(long date) {
        super(date);
    }

    @Override
    public void setTime(long date) {
        super.setTime(date);
    }

    public static Date valueOf(String s) {
        if (s == null) {
            throw new IllegalArgumentException();
        }
        int YEAR_LENGTH = 4;
        int MONTH_LENGTH = 2;
        int DAY_LENGTH = 2;
        int MAX_MONTH = 12;
        int MAX_DAY = 31;
        Date d = null;
        int firstDash = s.indexOf(45);
        int secondDash = s.indexOf(45, firstDash + 1);
        int len = s.length();
        if (firstDash > 0 && secondDash > 0 && secondDash < len - 1 && firstDash == 4 && secondDash - firstDash > 1 && secondDash - firstDash <= 3 && len - secondDash > 1 && len - secondDash <= 3) {
            int year = Integer.parseInt(s, 0, firstDash, 10);
            int month = Integer.parseInt(s, firstDash + 1, secondDash, 10);
            int day = Integer.parseInt(s, secondDash + 1, len, 10);
            if (month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                d = new Date(year - 1900, month - 1, day);
            }
        }
        if (d == null) {
            throw new IllegalArgumentException();
        }
        return d;
    }

    @Override
    public String toString() {
        int year = super.getYear() + 1900;
        int month = super.getMonth() + 1;
        int day = super.getDate();
        char[] buf = new char[10];
        Date.formatDecimalInt(year, buf, 0, 4);
        buf[4] = 45;
        Date.formatDecimalInt(month, buf, 5, 2);
        buf[7] = 45;
        Date.formatDecimalInt(day, buf, 8, 2);
        return new String(buf);
    }

    static void formatDecimalInt(int val, char[] buf, int offset, int len) {
        int charPos = offset + len;
        do {
            buf[--charPos] = (char)(48 + val % 10);
            val /= 10;
        } while (charPos > offset);
    }

    @Override
    @Deprecated(since="1.2")
    public int getHours() {
        throw new IllegalArgumentException();
    }

    @Override
    @Deprecated(since="1.2")
    public int getMinutes() {
        throw new IllegalArgumentException();
    }

    @Override
    @Deprecated(since="1.2")
    public int getSeconds() {
        throw new IllegalArgumentException();
    }

    @Override
    @Deprecated(since="1.2")
    public void setHours(int i) {
        throw new IllegalArgumentException();
    }

    @Override
    @Deprecated(since="1.2")
    public void setMinutes(int i) {
        throw new IllegalArgumentException();
    }

    @Override
    @Deprecated(since="1.2")
    public void setSeconds(int i) {
        throw new IllegalArgumentException();
    }

    public static Date valueOf(LocalDate date) {
        return new Date(date.getYear() - 1900, date.getMonthValue() - 1, date.getDayOfMonth());
    }

    public LocalDate toLocalDate() {
        return LocalDate.of(this.getYear() + 1900, this.getMonth() + 1, this.getDate());
    }

    @Override
    public Instant toInstant() {
        throw new UnsupportedOperationException();
    }
}

