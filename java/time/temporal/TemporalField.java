/*
 * Decompiled with CFR 0.152.
 */
package java.time.temporal;

import java.time.format.ResolverStyle;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalUnit;
import java.time.temporal.ValueRange;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public interface TemporalField {
    default public String getDisplayName(Locale locale) {
        Objects.requireNonNull(locale, "locale");
        return this.toString();
    }

    public TemporalUnit getBaseUnit();

    public TemporalUnit getRangeUnit();

    public ValueRange range();

    public boolean isDateBased();

    public boolean isTimeBased();

    public boolean isSupportedBy(TemporalAccessor var1);

    public ValueRange rangeRefinedBy(TemporalAccessor var1);

    public long getFrom(TemporalAccessor var1);

    public <R extends Temporal> R adjustInto(R var1, long var2);

    default public TemporalAccessor resolve(Map<TemporalField, Long> fieldValues, TemporalAccessor partialTemporal, ResolverStyle resolverStyle) {
        return null;
    }

    public String toString();
}

