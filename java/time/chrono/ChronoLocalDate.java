/*
 * Decompiled with CFR 0.152.
 */
package java.time.chrono;

import java.io.Serializable;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDateImpl;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoLocalDateTimeImpl;
import java.time.chrono.ChronoPeriod;
import java.time.chrono.Chronology;
import java.time.chrono.Era;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Comparator;
import java.util.Objects;

public interface ChronoLocalDate
extends Temporal,
TemporalAdjuster,
Comparable<ChronoLocalDate> {
    public static Comparator<ChronoLocalDate> timeLineOrder() {
        return (Comparator & Serializable)(date1, date2) -> Long.compare(date1.toEpochDay(), date2.toEpochDay());
    }

    public static ChronoLocalDate from(TemporalAccessor temporal) {
        if (temporal instanceof ChronoLocalDate) {
            return (ChronoLocalDate)temporal;
        }
        Objects.requireNonNull(temporal, "temporal");
        Chronology chrono = temporal.query(TemporalQueries.chronology());
        if (chrono == null) {
            throw new DateTimeException("Unable to obtain ChronoLocalDate from TemporalAccessor: " + temporal.getClass());
        }
        return chrono.date(temporal);
    }

    public Chronology getChronology();

    default public Era getEra() {
        return this.getChronology().eraOf(this.get(ChronoField.ERA));
    }

    default public boolean isLeapYear() {
        return this.getChronology().isLeapYear(this.getLong(ChronoField.YEAR));
    }

    public int lengthOfMonth();

    default public int lengthOfYear() {
        return this.isLeapYear() ? 366 : 365;
    }

    @Override
    default public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field.isDateBased();
        }
        return field != null && field.isSupportedBy(this);
    }

    @Override
    default public boolean isSupported(TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return unit.isDateBased();
        }
        return unit != null && unit.isSupportedBy(this);
    }

    @Override
    default public ChronoLocalDate with(TemporalAdjuster adjuster) {
        return ChronoLocalDateImpl.ensureValid(this.getChronology(), Temporal.super.with(adjuster));
    }

    @Override
    default public ChronoLocalDate with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return ChronoLocalDateImpl.ensureValid(this.getChronology(), field.adjustInto(this, newValue));
    }

    @Override
    default public ChronoLocalDate plus(TemporalAmount amount) {
        return ChronoLocalDateImpl.ensureValid(this.getChronology(), Temporal.super.plus(amount));
    }

    @Override
    default public ChronoLocalDate plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
        return ChronoLocalDateImpl.ensureValid(this.getChronology(), unit.addTo(this, amountToAdd));
    }

    @Override
    default public ChronoLocalDate minus(TemporalAmount amount) {
        return ChronoLocalDateImpl.ensureValid(this.getChronology(), Temporal.super.minus(amount));
    }

    @Override
    default public ChronoLocalDate minus(long amountToSubtract, TemporalUnit unit) {
        return ChronoLocalDateImpl.ensureValid(this.getChronology(), Temporal.super.minus(amountToSubtract, unit));
    }

    @Override
    default public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.zoneId() || query == TemporalQueries.zone() || query == TemporalQueries.offset()) {
            return null;
        }
        if (query == TemporalQueries.localTime()) {
            return null;
        }
        if (query == TemporalQueries.chronology()) {
            return (R)this.getChronology();
        }
        if (query == TemporalQueries.precision()) {
            return (R)ChronoUnit.DAYS;
        }
        return query.queryFrom(this);
    }

    @Override
    default public Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.EPOCH_DAY, this.toEpochDay());
    }

    @Override
    public long until(Temporal var1, TemporalUnit var2);

    public ChronoPeriod until(ChronoLocalDate var1);

    default public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    default public ChronoLocalDateTime<?> atTime(LocalTime localTime) {
        return ChronoLocalDateTimeImpl.of(this, localTime);
    }

    default public long toEpochDay() {
        return this.getLong(ChronoField.EPOCH_DAY);
    }

    @Override
    default public int compareTo(ChronoLocalDate other) {
        int cmp = Long.compare(this.toEpochDay(), other.toEpochDay());
        if (cmp == 0) {
            cmp = this.getChronology().compareTo(other.getChronology());
        }
        return cmp;
    }

    default public boolean isAfter(ChronoLocalDate other) {
        return this.toEpochDay() > other.toEpochDay();
    }

    default public boolean isBefore(ChronoLocalDate other) {
        return this.toEpochDay() < other.toEpochDay();
    }

    default public boolean isEqual(ChronoLocalDate other) {
        return this.toEpochDay() == other.toEpochDay();
    }

    public boolean equals(Object var1);

    public int hashCode();

    public String toString();
}

