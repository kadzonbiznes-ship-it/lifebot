/*
 * Decompiled with CFR 0.152.
 */
package java.time.temporal;

import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;

public interface Temporal
extends TemporalAccessor {
    public boolean isSupported(TemporalUnit var1);

    default public Temporal with(TemporalAdjuster adjuster) {
        return adjuster.adjustInto(this);
    }

    public Temporal with(TemporalField var1, long var2);

    default public Temporal plus(TemporalAmount amount) {
        return amount.addTo(this);
    }

    public Temporal plus(long var1, TemporalUnit var3);

    default public Temporal minus(TemporalAmount amount) {
        return amount.subtractFrom(this);
    }

    default public Temporal minus(long amountToSubtract, TemporalUnit unit) {
        return amountToSubtract == Long.MIN_VALUE ? this.plus(Long.MAX_VALUE, unit).plus(1L, unit) : this.plus(-amountToSubtract, unit);
    }

    public long until(Temporal var1, TemporalUnit var2);
}

