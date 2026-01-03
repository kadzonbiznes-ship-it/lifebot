/*
 * Decompiled with CFR 0.152.
 */
package java.time.temporal;

import java.time.DateTimeException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Objects;

public interface TemporalAccessor {
    public boolean isSupported(TemporalField var1);

    default public ValueRange range(TemporalField field) {
        if (field instanceof ChronoField) {
            if (this.isSupported(field)) {
                return field.range();
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        Objects.requireNonNull(field, "field");
        return field.rangeRefinedBy(this);
    }

    default public int get(TemporalField field) {
        ValueRange range = this.range(field);
        if (!range.isIntValue()) {
            throw new UnsupportedTemporalTypeException("Invalid field " + field + " for get() method, use getLong() instead");
        }
        long value = this.getLong(field);
        if (!range.isValidValue(value)) {
            throw new DateTimeException("Invalid value for " + field + " (valid values " + range + "): " + value);
        }
        return (int)value;
    }

    public long getLong(TemporalField var1);

    default public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.zoneId() || query == TemporalQueries.chronology() || query == TemporalQueries.precision()) {
            return null;
        }
        return query.queryFrom(this);
    }
}

