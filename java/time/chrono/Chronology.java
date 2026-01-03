/*
 * Decompiled with CFR 0.152.
 */
package java.time.chrono;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.AbstractChronology;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoLocalDateTimeImpl;
import java.time.chrono.ChronoPeriod;
import java.time.chrono.ChronoPeriodImpl;
import java.time.chrono.ChronoZonedDateTime;
import java.time.chrono.ChronoZonedDateTimeImpl;
import java.time.chrono.Era;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public interface Chronology
extends Comparable<Chronology> {
    public static Chronology from(TemporalAccessor temporal) {
        Objects.requireNonNull(temporal, "temporal");
        Chronology obj = temporal.query(TemporalQueries.chronology());
        return Objects.requireNonNullElse(obj, IsoChronology.INSTANCE);
    }

    public static Chronology ofLocale(Locale locale) {
        return AbstractChronology.ofLocale(locale);
    }

    public static Chronology of(String id) {
        return AbstractChronology.of(id);
    }

    public static Set<Chronology> getAvailableChronologies() {
        return AbstractChronology.getAvailableChronologies();
    }

    public String getId();

    public String getCalendarType();

    default public ChronoLocalDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
        return this.date(this.prolepticYear(era, yearOfEra), month, dayOfMonth);
    }

    public ChronoLocalDate date(int var1, int var2, int var3);

    default public ChronoLocalDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
        return this.dateYearDay(this.prolepticYear(era, yearOfEra), dayOfYear);
    }

    public ChronoLocalDate dateYearDay(int var1, int var2);

    public ChronoLocalDate dateEpochDay(long var1);

    default public ChronoLocalDate dateNow() {
        return this.dateNow(Clock.systemDefaultZone());
    }

    default public ChronoLocalDate dateNow(ZoneId zone) {
        return this.dateNow(Clock.system(zone));
    }

    default public ChronoLocalDate dateNow(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        return this.date(LocalDate.now(clock));
    }

    public ChronoLocalDate date(TemporalAccessor var1);

    default public ChronoLocalDateTime<? extends ChronoLocalDate> localDateTime(TemporalAccessor temporal) {
        try {
            return this.date(temporal).atTime(LocalTime.from(temporal));
        }
        catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain ChronoLocalDateTime from TemporalAccessor: " + temporal.getClass(), ex);
        }
    }

    default public ChronoZonedDateTime<? extends ChronoLocalDate> zonedDateTime(TemporalAccessor temporal) {
        try {
            ZoneId zone = ZoneId.from(temporal);
            try {
                Instant instant = Instant.from(temporal);
                return this.zonedDateTime(instant, zone);
            }
            catch (DateTimeException ex1) {
                ChronoLocalDateTimeImpl cldt = ChronoLocalDateTimeImpl.ensureValid(this, this.localDateTime(temporal));
                return ChronoZonedDateTimeImpl.ofBest(cldt, zone, null);
            }
        }
        catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain ChronoZonedDateTime from TemporalAccessor: " + temporal.getClass(), ex);
        }
    }

    default public ChronoZonedDateTime<? extends ChronoLocalDate> zonedDateTime(Instant instant, ZoneId zone) {
        return ChronoZonedDateTimeImpl.ofInstant(this, instant, zone);
    }

    public boolean isLeapYear(long var1);

    public int prolepticYear(Era var1, int var2);

    public Era eraOf(int var1);

    public List<Era> eras();

    public ValueRange range(ChronoField var1);

    default public String getDisplayName(TextStyle style, Locale locale) {
        TemporalAccessor temporal = new TemporalAccessor(){

            @Override
            public boolean isSupported(TemporalField field) {
                return false;
            }

            @Override
            public long getLong(TemporalField field) {
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            }

            @Override
            public <R> R query(TemporalQuery<R> query) {
                if (query == TemporalQueries.chronology()) {
                    return (R)Chronology.this;
                }
                return TemporalAccessor.super.query(query);
            }
        };
        return new DateTimeFormatterBuilder().appendChronologyText(style).toFormatter(locale).format(temporal);
    }

    public ChronoLocalDate resolveDate(Map<TemporalField, Long> var1, ResolverStyle var2);

    default public ChronoPeriod period(int years, int months, int days) {
        return new ChronoPeriodImpl(this, years, months, days);
    }

    default public long epochSecond(int prolepticYear, int month, int dayOfMonth, int hour, int minute, int second, ZoneOffset zoneOffset) {
        Objects.requireNonNull(zoneOffset, "zoneOffset");
        ChronoField.HOUR_OF_DAY.checkValidValue(hour);
        ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
        ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
        long daysInSec = Math.multiplyExact(this.date(prolepticYear, month, dayOfMonth).toEpochDay(), 86400);
        long timeinSec = (hour * 60 + minute) * 60 + second;
        return Math.addExact(daysInSec, timeinSec - (long)zoneOffset.getTotalSeconds());
    }

    default public long epochSecond(Era era, int yearOfEra, int month, int dayOfMonth, int hour, int minute, int second, ZoneOffset zoneOffset) {
        Objects.requireNonNull(era, "era");
        return this.epochSecond(this.prolepticYear(era, yearOfEra), month, dayOfMonth, hour, minute, second, zoneOffset);
    }

    default public boolean isIsoBased() {
        return false;
    }

    @Override
    public int compareTo(Chronology var1);

    public boolean equals(Object var1);

    public int hashCode();

    public String toString();
}

