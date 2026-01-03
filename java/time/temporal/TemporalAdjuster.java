/*
 * Decompiled with CFR 0.152.
 */
package java.time.temporal;

import java.time.temporal.Temporal;

@FunctionalInterface
public interface TemporalAdjuster {
    public Temporal adjustInto(Temporal var1);
}

