/*
 * Decompiled with CFR 0.152.
 */
package java.time;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;

public interface InstantSource {
    public static InstantSource system() {
        return Clock.SystemInstantSource.INSTANCE;
    }

    public static InstantSource tick(InstantSource baseSource, Duration tickDuration) {
        Objects.requireNonNull(baseSource, "baseSource");
        return Clock.tick(baseSource.withZone(ZoneOffset.UTC), tickDuration);
    }

    public static InstantSource fixed(Instant fixedInstant) {
        return Clock.fixed(fixedInstant, ZoneOffset.UTC);
    }

    public static InstantSource offset(InstantSource baseSource, Duration offsetDuration) {
        Objects.requireNonNull(baseSource, "baseSource");
        return Clock.offset(baseSource.withZone(ZoneOffset.UTC), offsetDuration);
    }

    public Instant instant();

    default public long millis() {
        return this.instant().toEpochMilli();
    }

    default public Clock withZone(ZoneId zone) {
        return new Clock.SourceClock(this, zone);
    }
}

