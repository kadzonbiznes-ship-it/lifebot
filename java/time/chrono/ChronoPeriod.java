/*
 * Decompiled with CFR 0.152.
 */
package java.time.chrono;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Objects;

public interface ChronoPeriod
extends TemporalAmount {
    public static ChronoPeriod between(ChronoLocalDate startDateInclusive, ChronoLocalDate endDateExclusive) {
        Objects.requireNonNull(startDateInclusive, "startDateInclusive");
        Objects.requireNonNull(endDateExclusive, "endDateExclusive");
        return startDateInclusive.until(endDateExclusive);
    }

    @Override
    public long get(TemporalUnit var1);

    @Override
    public List<TemporalUnit> getUnits();

    public Chronology getChronology();

    default public boolean isZero() {
        for (TemporalUnit unit : this.getUnits()) {
            if (this.get(unit) == 0L) continue;
            return false;
        }
        return true;
    }

    default public boolean isNegative() {
        for (TemporalUnit unit : this.getUnits()) {
            if (this.get(unit) >= 0L) continue;
            return true;
        }
        return false;
    }

    public ChronoPeriod plus(TemporalAmount var1);

    public ChronoPeriod minus(TemporalAmount var1);

    public ChronoPeriod multipliedBy(int var1);

    default public ChronoPeriod negated() {
        return this.multipliedBy(-1);
    }

    public ChronoPeriod normalized();

    @Override
    public Temporal addTo(Temporal var1);

    @Override
    public Temporal subtractFrom(Temporal var1);

    public boolean equals(Object var1);

    public int hashCode();

    public String toString();
}

