/*
 * Decompiled with CFR 0.152.
 */
package java.time.format;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DecimalStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.ValueRange;
import java.util.Locale;
import java.util.Objects;

final class DateTimePrintContext {
    private final TemporalAccessor temporal;
    private final DateTimeFormatter formatter;
    private int optional;

    DateTimePrintContext(TemporalAccessor temporal, DateTimeFormatter formatter) {
        this.temporal = DateTimePrintContext.adjust(temporal, formatter);
        this.formatter = formatter;
    }

    private static TemporalAccessor adjust(final TemporalAccessor temporal, DateTimeFormatter formatter) {
        ChronoLocalDate effectiveDate;
        ZoneId effectiveZone;
        Chronology effectiveChrono;
        Chronology overrideChrono = formatter.getChronology();
        ZoneId overrideZone = formatter.getZone();
        if (overrideChrono == null && overrideZone == null) {
            return temporal;
        }
        Chronology temporalChrono = temporal.query(TemporalQueries.chronology());
        ZoneId temporalZone = temporal.query(TemporalQueries.zoneId());
        if (Objects.equals(overrideChrono, temporalChrono)) {
            overrideChrono = null;
        }
        if (Objects.equals(overrideZone, temporalZone)) {
            overrideZone = null;
        }
        if (overrideChrono == null && overrideZone == null) {
            return temporal;
        }
        Chronology chronology = effectiveChrono = overrideChrono != null ? overrideChrono : temporalChrono;
        if (overrideZone != null) {
            if (temporal.isSupported(ChronoField.INSTANT_SECONDS)) {
                Chronology chrono = Objects.requireNonNullElse(effectiveChrono, IsoChronology.INSTANCE);
                return chrono.zonedDateTime(Instant.from(temporal), overrideZone);
            }
            if (overrideZone.normalized() instanceof ZoneOffset && temporal.isSupported(ChronoField.OFFSET_SECONDS) && temporal.get(ChronoField.OFFSET_SECONDS) != overrideZone.getRules().getOffset(Instant.EPOCH).getTotalSeconds()) {
                throw new DateTimeException("Unable to apply override zone '" + overrideZone + "' because the temporal object being formatted has a different offset but does not represent an instant: " + temporal);
            }
        }
        ZoneId zoneId = effectiveZone = overrideZone != null ? overrideZone : temporalZone;
        if (overrideChrono != null) {
            if (temporal.isSupported(ChronoField.EPOCH_DAY)) {
                effectiveDate = effectiveChrono.date(temporal);
            } else {
                if (overrideChrono != IsoChronology.INSTANCE || temporalChrono != null) {
                    for (ChronoField f : ChronoField.values()) {
                        if (!f.isDateBased() || !temporal.isSupported(f)) continue;
                        throw new DateTimeException("Unable to apply override chronology '" + overrideChrono + "' because the temporal object being formatted contains date fields but does not represent a whole date: " + temporal);
                    }
                }
                effectiveDate = null;
            }
        } else {
            effectiveDate = null;
        }
        return new TemporalAccessor(){

            @Override
            public boolean isSupported(TemporalField field) {
                if (effectiveDate != null && field.isDateBased()) {
                    return effectiveDate.isSupported(field);
                }
                return temporal.isSupported(field);
            }

            @Override
            public ValueRange range(TemporalField field) {
                if (effectiveDate != null && field.isDateBased()) {
                    return effectiveDate.range(field);
                }
                return temporal.range(field);
            }

            @Override
            public long getLong(TemporalField field) {
                if (effectiveDate != null && field.isDateBased()) {
                    return effectiveDate.getLong(field);
                }
                return temporal.getLong(field);
            }

            @Override
            public <R> R query(TemporalQuery<R> query) {
                if (query == TemporalQueries.chronology()) {
                    return (R)effectiveChrono;
                }
                if (query == TemporalQueries.zoneId()) {
                    return (R)effectiveZone;
                }
                if (query == TemporalQueries.precision()) {
                    return temporal.query(query);
                }
                return query.queryFrom(this);
            }

            public String toString() {
                return temporal + (effectiveChrono != null ? " with chronology " + effectiveChrono : "") + (effectiveZone != null ? " with zone " + effectiveZone : "");
            }
        };
    }

    TemporalAccessor getTemporal() {
        return this.temporal;
    }

    Locale getLocale() {
        return this.formatter.getLocale();
    }

    DecimalStyle getDecimalStyle() {
        return this.formatter.getDecimalStyle();
    }

    void startOptional() {
        ++this.optional;
    }

    void endOptional() {
        --this.optional;
    }

    <R> R getValue(TemporalQuery<R> query) {
        R result = this.temporal.query(query);
        if (result == null && this.optional == 0) {
            throw new DateTimeException("Unable to extract " + query + " from temporal " + this.temporal);
        }
        return result;
    }

    Long getValue(TemporalField field) {
        if (this.optional > 0 && !this.temporal.isSupported(field)) {
            return null;
        }
        return this.temporal.getLong(field);
    }

    public String toString() {
        return this.temporal.toString();
    }
}

