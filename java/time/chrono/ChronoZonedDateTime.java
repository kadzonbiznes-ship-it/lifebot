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
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTimeImpl;
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
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Comparator;
import java.util.Objects;

public interface ChronoZonedDateTime<D extends ChronoLocalDate>
extends Temporal,
Comparable<ChronoZonedDateTime<?>> {
    public static Comparator<ChronoZonedDateTime<?>> timeLineOrder() {
        return (Comparator & Serializable)(dateTime1, dateTime2) -> {
            int cmp = Long.compare(dateTime1.toEpochSecond(), dateTime2.toEpochSecond());
            if (cmp == 0) {
                cmp = Long.compare(dateTime1.toLocalTime().getNano(), dateTime2.toLocalTime().getNano());
            }
            return cmp;
        };
    }

    public static ChronoZonedDateTime<?> from(TemporalAccessor temporal) {
        if (temporal instanceof ChronoZonedDateTime) {
            return (ChronoZonedDateTime)temporal;
        }
        Objects.requireNonNull(temporal, "temporal");
        Chronology chrono = temporal.query(TemporalQueries.chronology());
        if (chrono == null) {
            throw new DateTimeException("Unable to obtain ChronoZonedDateTime from TemporalAccessor: " + temporal.getClass());
        }
        return chrono.zonedDateTime(temporal);
    }

    @Override
    default public ValueRange range(TemporalField field) {
        if (field instanceof ChronoField) {
            if (field == ChronoField.INSTANT_SECONDS || field == ChronoField.OFFSET_SECONDS) {
                return field.range();
            }
            return this.toLocalDateTime().range(field);
        }
        return field.rangeRefinedBy(this);
    }

    @Override
    default public int get(TemporalField field) {
        if (field instanceof ChronoField) {
            ChronoField chronoField = (ChronoField)field;
            return switch (chronoField) {
                case ChronoField.INSTANT_SECONDS -> throw new UnsupportedTemporalTypeException("Invalid field 'InstantSeconds' for get() method, use getLong() instead");
                case ChronoField.OFFSET_SECONDS -> this.getOffset().getTotalSeconds();
                default -> this.toLocalDateTime().get(field);
            };
        }
        return Temporal.super.get(field);
    }

    @Override
    default public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            ChronoField chronoField = (ChronoField)field;
            return switch (chronoField) {
                case ChronoField.INSTANT_SECONDS -> this.toEpochSecond();
                case ChronoField.OFFSET_SECONDS -> this.getOffset().getTotalSeconds();
                default -> this.toLocalDateTime().getLong(field);
            };
        }
        return field.getFrom(this);
    }

    default public D toLocalDate() {
        return this.toLocalDateTime().toLocalDate();
    }

    default public LocalTime toLocalTime() {
        return this.toLocalDateTime().toLocalTime();
    }

    public ChronoLocalDateTime<D> toLocalDateTime();

    default public Chronology getChronology() {
        return this.toLocalDate().getChronology();
    }

    public ZoneOffset getOffset();

    public ZoneId getZone();

    public ChronoZonedDateTime<D> withEarlierOffsetAtOverlap();

    public ChronoZonedDateTime<D> withLaterOffsetAtOverlap();

    public ChronoZonedDateTime<D> withZoneSameLocal(ZoneId var1);

    public ChronoZonedDateTime<D> withZoneSameInstant(ZoneId var1);

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
    default public ChronoZonedDateTime<D> with(TemporalAdjuster adjuster) {
        return ChronoZonedDateTimeImpl.ensureValid(this.getChronology(), Temporal.super.with(adjuster));
    }

    @Override
    public ChronoZonedDateTime<D> with(TemporalField var1, long var2);

    @Override
    default public ChronoZonedDateTime<D> plus(TemporalAmount amount) {
        return ChronoZonedDateTimeImpl.ensureValid(this.getChronology(), Temporal.super.plus(amount));
    }

    @Override
    public ChronoZonedDateTime<D> plus(long var1, TemporalUnit var3);

    @Override
    default public ChronoZonedDateTime<D> minus(TemporalAmount amount) {
        return ChronoZonedDateTimeImpl.ensureValid(this.getChronology(), Temporal.super.minus(amount));
    }

    @Override
    default public ChronoZonedDateTime<D> minus(long amountToSubtract, TemporalUnit unit) {
        return ChronoZonedDateTimeImpl.ensureValid(this.getChronology(), Temporal.super.minus(amountToSubtract, unit));
    }

    @Override
    default public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.zone() || query == TemporalQueries.zoneId()) {
            return (R)this.getZone();
        }
        if (query == TemporalQueries.offset()) {
            return (R)this.getOffset();
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

    default public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    default public Instant toInstant() {
        return Instant.ofEpochSecond(this.toEpochSecond(), this.toLocalTime().getNano());
    }

    default public long toEpochSecond() {
        long epochDay = this.toLocalDate().toEpochDay();
        long secs = epochDay * 86400L + (long)this.toLocalTime().toSecondOfDay();
        return secs -= (long)this.getOffset().getTotalSeconds();
    }

    @Override
    default public int compareTo(ChronoZonedDateTime<?> other) {
        int cmp = Long.compare(this.toEpochSecond(), other.toEpochSecond());
        if (cmp == 0 && (cmp = this.toLocalTime().getNano() - other.toLocalTime().getNano()) == 0 && (cmp = this.toLocalDateTime().compareTo(other.toLocalDateTime())) == 0 && (cmp = this.getZone().getId().compareTo(other.getZone().getId())) == 0) {
            cmp = this.getChronology().compareTo(other.getChronology());
        }
        return cmp;
    }

    default public boolean isBefore(ChronoZonedDateTime<?> other) {
        long otherEpochSec;
        long thisEpochSec = this.toEpochSecond();
        return thisEpochSec < (otherEpochSec = other.toEpochSecond()) || thisEpochSec == otherEpochSec && this.toLocalTime().getNano() < other.toLocalTime().getNano();
    }

    default public boolean isAfter(ChronoZonedDateTime<?> other) {
        long otherEpochSec;
        long thisEpochSec = this.toEpochSecond();
        return thisEpochSec > (otherEpochSec = other.toEpochSecond()) || thisEpochSec == otherEpochSec && this.toLocalTime().getNano() > other.toLocalTime().getNano();
    }

    default public boolean isEqual(ChronoZonedDateTime<?> other) {
        return this.toEpochSecond() == other.toEpochSecond() && this.toLocalTime().getNano() == other.toLocalTime().getNano();
    }

    public boolean equals(Object var1);

    public int hashCode();

    public String toString();
}

