/*
 * Decompiled with CFR 0.152.
 */
package sun.util.calendar;

import java.util.TimeZone;
import sun.util.calendar.BaseCalendar;
import sun.util.calendar.CalendarDate;

public class Gregorian
extends BaseCalendar {
    Gregorian() {
    }

    @Override
    public String getName() {
        return "gregorian";
    }

    @Override
    public Date getCalendarDate() {
        return this.getCalendarDate(System.currentTimeMillis(), this.newCalendarDate());
    }

    @Override
    public Date getCalendarDate(long millis) {
        return this.getCalendarDate(millis, this.newCalendarDate());
    }

    @Override
    public Date getCalendarDate(long millis, CalendarDate date) {
        return (Date)super.getCalendarDate(millis, date);
    }

    @Override
    public Date getCalendarDate(long millis, TimeZone zone) {
        return this.getCalendarDate(millis, this.newCalendarDate(zone));
    }

    @Override
    public Date newCalendarDate() {
        return new Date();
    }

    @Override
    public Date newCalendarDate(TimeZone zone) {
        return new Date(zone);
    }

    static class Date
    extends BaseCalendar.Date {
        protected Date() {
        }

        protected Date(TimeZone zone) {
            super(zone);
        }

        @Override
        public int getNormalizedYear() {
            return this.getYear();
        }

        @Override
        public void setNormalizedYear(int normalizedYear) {
            this.setYear(normalizedYear);
        }
    }
}

