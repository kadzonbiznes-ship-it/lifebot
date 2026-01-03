/*
 * Decompiled with CFR 0.152.
 */
package java.sql;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalTime;

public class Time
extends java.util.Date {
    static final long serialVersionUID = 8397324403548013681L;

    @Deprecated(since="1.2")
    public Time(int hour, int minute, int second) {
        super(70, 0, 1, hour, minute, second);
    }

    public Time(long time) {
        super(time);
    }

    @Override
    public void setTime(long time) {
        super.setTime(time);
    }

    public static Time valueOf(String s) {
        if (s == null) {
            throw new IllegalArgumentException();
        }
        int firstColon = s.indexOf(58);
        int secondColon = s.indexOf(58, firstColon + 1);
        int len = s.length();
        if (firstColon <= 0 || secondColon <= 0 || secondColon >= len - 1) {
            throw new IllegalArgumentException();
        }
        int hour = Integer.parseInt(s, 0, firstColon, 10);
        int minute = Integer.parseInt(s, firstColon + 1, secondColon, 10);
        int second = Integer.parseInt(s, secondColon + 1, len, 10);
        return new Time(hour, minute, second);
    }

    @Override
    public String toString() {
        int hour = super.getHours();
        int minute = super.getMinutes();
        int second = super.getSeconds();
        char[] buf = new char[8];
        Date.formatDecimalInt(hour, buf, 0, 2);
        buf[2] = 58;
        Date.formatDecimalInt(minute, buf, 3, 2);
        buf[5] = 58;
        Date.formatDecimalInt(second, buf, 6, 2);
        return new String(buf);
    }

    @Override
    @Deprecated(since="1.2")
    public int getYear() {
        throw new IllegalArgumentException();
    }

    @Override
    @Deprecated(since="1.2")
    public int getMonth() {
        throw new IllegalArgumentException();
    }

    @Override
    @Deprecated(since="1.2")
    public int getDay() {
        throw new IllegalArgumentException();
    }

    @Override
    @Deprecated(since="1.2")
    public int getDate() {
        throw new IllegalArgumentException();
    }

    @Override
    @Deprecated(since="1.2")
    public void setYear(int i) {
        throw new IllegalArgumentException();
    }

    @Override
    @Deprecated(since="1.2")
    public void setMonth(int i) {
        throw new IllegalArgumentException();
    }

    @Override
    @Deprecated(since="1.2")
    public void setDate(int i) {
        throw new IllegalArgumentException();
    }

    public static Time valueOf(LocalTime time) {
        return new Time(time.getHour(), time.getMinute(), time.getSecond());
    }

    public LocalTime toLocalTime() {
        return LocalTime.of(this.getHours(), this.getMinutes(), this.getSeconds());
    }

    @Override
    public Instant toInstant() {
        throw new UnsupportedOperationException();
    }
}

