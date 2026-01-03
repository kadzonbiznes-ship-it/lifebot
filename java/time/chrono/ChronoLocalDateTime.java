/*
 * Decompiled with CFR 0.152.
 */
package java.time.chrono;

import java.io.Serializable;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTimeImpl;
import java.time.chrono.ChronoZonedDateTime;
import java.time.chrono.Chronology;
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
import java.util.Comparator;
import java.util.Objects;

public interface ChronoLocalDateTime<D extends ChronoLocalDate>
extends Temporal,
TemporalAdjuster,
Comparable<ChronoLocalDateTime<?>> {
    public static Comparator<ChronoLocalDateTime<?>> timeLineOrder() {
        return (Comparator & Serializable)(dateTime1, dateTime2) -> {
            int cmp = Long.compare(dateTime1.toLocalDate().toEpochDay(), dateTime2.toLocalDate().toEpochDay());
            if (cmp == 0) {
                cmp = Long.compare(dateTime1.toLocalTime().toNanoOfDay(), dateTime2.toLocalTime().toNanoOfDay());
            }
            return cmp;
        };
    }

    public static ChronoLocalDateTime<?> from(TemporalAccessor temporal) {
        if (temporal instanceof ChronoLocalDateTime) {
            return (ChronoLocalDateTime)temporal;
        }
        Objects.requireNonNull(temporal, "temporal");
        Chronology chrono = temporal.query(TemporalQueries.chronology());
        if (chrono == null) {
            throw new DateTimeException("Unable to obtain ChronoLocalDateTime from TemporalAccessor: " + temporal.getClass());
        }
        return chrono.localDateTime(temporal);
    }

    default public Chronology getChronology() {
        return this.toLocalDate().getChronology();
    }

    public D toLocalDate();

    public LocalTime toLocalTime();

    @Override
    public boolean isSupported(TemporalField var1);

    @Override
    default public boolean isSupported(TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return unit != ChronoUnit.FOREVER;
        }
        return unit != null && unit.isSupportedBy(this);
    }

    @Override
    default public ChronoLocalDateTime<D> with(TemporalAdjuster adjuster) {
        return ChronoLocalDateTimeImpl.ensureValid(this.getChronology(), Temporal.super.with(adjuster));
    }

    @Override
    public ChronoLocalDateTime<D> with(TemporalField var1, long var2);

    @Override
    default public ChronoLocalDateTime<D> plus(TemporalAmount amount) {
        return ChronoLocalDateTimeImpl.ensureValid(this.getChronology(), Temporal.super.plus(amount));
    }

    @Override
    public ChronoLocalDateTime<D> plus(long var1, TemporalUnit var3);

    @Override
    default public ChronoLocalDateTime<D> minus(TemporalAmount amount) {
        return ChronoLocalDateTimeImpl.ensureValid(this.getChronology(), Temporal.super.minus(amount));
    }

    @Override
    default public ChronoLocalDateTime<D> minus(long amountToSubtract, TemporalUnit unit) {
        return ChronoLocalDateTimeImpl.ensureValid(this.getChronology(), Temporal.super.minus(amountToSubtract, unit));
    }

    @Override
    default public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.zoneId() || query == TemporalQueries.zone() || query == TemporalQueries.offset()) {
            return null;
        }
        if (query == TemporalQueries.localTime()) {
            return (R)this.toLocalTime();
        }
        if (query == TemporalQueries.chronology()) {
            return (R)this.getChronology();
        }
        if (query == TemporalQueries.precision()) {
            return (R)ChronoUnit.NANOS;
        }
        return query.queryFrom(this);
    }

    @Override
    default public Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.EPOCH_DAY, this.toLocalDate().toEpochDay()).with(ChronoField.NANO_OF_DAY, this.toLocalTime().toNanoOfDay());
    }

    default public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    public ChronoZonedDateTime<D> atZone(ZoneId var1);

    default public Instant toInstant(ZoneOffset offset) {
        return Instant.ofEpochSecond(this.toEpochSecond(offset), this.toLocalTime().getNano());
    }

    default public long toEpochSecond(ZoneOffset offset) {
        Objects.requireNonNull(offset, "offset");
        long epochDay = this.toLocalDate().toEpochDay();
        long secs = epochDay * 86400L + (long)this.toLocalTime().toSecondOfDay();
        return secs -= (long)offset.getTotalSeconds();
    }

    @Override
    default public int compareTo(ChronoLocalDateTime<?> other) {
        int cmp = this.toLocalDate().compareTo((ChronoLocalDate)other.toLocalDate());
        if (cmp == 0 && (cmp = this.toLocalTime().compareTo(other.toLocalTime())) == 0) {
            cmp = this.getChronology().compareTo(other.getChronology());
        }
        return cmp;
    }

    default public boolean isAfter(ChronoLocalDateTime<?> other) {
        long otherEpDay;
        long thisEpDay = this.toLocalDate().toEpochDay();
        return thisEpDay > (otherEpDay = other.toLocalDate().toEpochDay()) || thisEpDay == otherEpDay && this.toLocalTime().toNanoOfDay() > other.toLocalTime().toNanoOfDay();
    }

    default public boolean isBefore(ChronoLocalDateTime<?> other) {
        long otherEpDay;
        long thisEpDay = this.toLocalDate().toEpochDay();
        return thisEpDay < (otherEpDay = other.toLocalDate().toEpochDay()) || thisEpDay == otherEpDay && this.toLocalTime().toNanoOfDay() < other.toLocalTime().toNanoOfDay();
    }

    default public boolean isEqual(ChronoLocalDateTime<?> other) {
        return this.toLocalTime().toNanoOfDay() == other.toLocalTime().toNanoOfDay() && this.toLocalDate().toEpochDay() == other.toLocalDate().toEpochDay();
    }

    public boolean equals(Object var1);

    public int hashCode();

    public String toString();
}

