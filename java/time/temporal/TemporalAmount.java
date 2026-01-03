/*
 * Decompiled with CFR 0.152.
 */
package java.time.temporal;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.List;

public interface TemporalAmount {
    public long get(TemporalUnit var1);

    public List<TemporalUnit> getUnits();

    public Temporal addTo(Temporal var1);

    public Temporal subtractFrom(Temporal var1);
}

