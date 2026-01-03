/*
 * Decompiled with CFR 0.152.
 */
package java.time;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;
import jdk.internal.misc.VM;

public abstract class Clock
implements InstantSource {
    private static final long OFFSET_SEED;
    private static long offset;

    public static Clock systemUTC() {
        return SystemClock.UTC;
    }

    public static Clock systemDefaultZone() {
        return new SystemClock(ZoneId.systemDefault());
    }

    public static Clock system(ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        if (zone == ZoneOffset.UTC) {
            return SystemClock.UTC;
        }
        return new SystemClock(zone);
    }

    public static Clock tickMillis(ZoneId zone) {
        return new TickClock(Clock.system(zone), 1000000L);
    }

    public static Clock tickSeconds(ZoneId zone) {
        return new TickClock(Clock.system(zone), 1000000000L);
    }

    public static Clock tickMinutes(ZoneId zone) {
        return new TickClock(Clock.system(zone), 60000000000L);
    }

    public static Clock tick(Clock baseClock, Duration tickDuration) {
        Objects.requireNonNull(baseClock, "baseClock");
        Objects.requireNonNull(tickDuration, "tickDuration");
        if (tickDuration.isNegative()) {
            throw new IllegalArgumentException("Tick duration must not be negative");
        }
        long tickNanos = tickDuration.toNanos();
        if (tickNanos % 1000000L != 0L && 1000000000L % tickNanos != 0L) {
            throw new IllegalArgumentException("Invalid tick duration");
        }
        if (tickNanos <= 1L) {
            return baseClock;
        }
        return new TickClock(baseClock, tickNanos);
    }

    public static Clock fixed(Instant fixedInstant, ZoneId zone) {
        Objects.requireNonNull(fixedInstant, "fixedInstant");
        Objects.requireNonNull(zone, "zone");
        return new FixedClock(fixedInstant, zone);
    }

    public static Clock offset(Clock baseClock, Duration offsetDuration) {
        Objects.requireNonNull(baseClock, "baseClock");
        Objects.requireNonNull(offsetDuration, "offsetDuration");
        if (offsetDuration.equals(Duration.ZERO)) {
            return baseClock;
        }
        return new OffsetClock(baseClock, offsetDuration);
    }

    protected Clock() {
    }

    public abstract ZoneId getZone();

    @Override
    public abstract Clock withZone(ZoneId var1);

    @Override
    public long millis() {
        return this.instant().toEpochMilli();
    }

    @Override
    public abstract Instant instant();

    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public int hashCode() {
        return super.hashCode();
    }

    static Instant currentInstant() {
        long localOffset = offset;
        long adjustment = VM.getNanoTimeAdjustment(localOffset);
        if (adjustment == -1L) {
            localOffset = System.currentTimeMillis() / 1000L - 1024L;
            adjustment = VM.getNanoTimeAdjustment(localOffset);
            if (adjustment == -1L) {
                throw new InternalError("Offset " + localOffset + " is not in range");
            }
            offset = localOffset;
        }
        return Instant.ofEpochSecond(localOffset, adjustment);
    }

    static {
        offset = OFFSET_SEED = System.currentTimeMillis() / 1000L - 1024L;
    }

    static final class SystemClock
    extends Clock
    implements Serializable {
        private static final long serialVersionUID = 6740630888130243051L;
        static final SystemClock UTC = new SystemClock(ZoneOffset.UTC);
        private final ZoneId zone;

        SystemClock(ZoneId zone) {
            this.zone = zone;
        }

        @Override
        public ZoneId getZone() {
            return this.zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            if (zone.equals(this.zone)) {
                return this;
            }
            return new SystemClock(zone);
        }

        @Override
        public long millis() {
            return System.currentTimeMillis();
        }

        @Override
        public Instant instant() {
            return SystemClock.currentInstant();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SystemClock) {
                return this.zone.equals(((SystemClock)obj).zone);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.zone.hashCode() + 1;
        }

        public String toString() {
            return "SystemClock[" + this.zone + "]";
        }
    }

    static final class TickClock
    extends Clock
    implements Serializable {
        private static final long serialVersionUID = 6504659149906368850L;
        private final Clock baseClock;
        private final long tickNanos;

        TickClock(Clock baseClock, long tickNanos) {
            this.baseClock = baseClock;
            this.tickNanos = tickNanos;
        }

        @Override
        public ZoneId getZone() {
            return this.baseClock.getZone();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            if (zone.equals(this.baseClock.getZone())) {
                return this;
            }
            return new TickClock(this.baseClock.withZone(zone), this.tickNanos);
        }

        @Override
        public long millis() {
            long millis = this.baseClock.millis();
            return this.tickNanos < 1000000L ? millis : millis - Math.floorMod(millis, this.tickNanos / 1000000L);
        }

        @Override
        public Instant instant() {
            if (this.tickNanos % 1000000L == 0L) {
                long millis = this.baseClock.millis();
                return Instant.ofEpochMilli(millis - Math.floorMod(millis, this.tickNanos / 1000000L));
            }
            Instant instant = this.baseClock.instant();
            long nanos = instant.getNano();
            long adjust = Math.floorMod(nanos, this.tickNanos);
            return instant.minusNanos(adjust);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TickClock)) return false;
            TickClock other = (TickClock)obj;
            if (this.tickNanos != other.tickNanos) return false;
            if (!this.baseClock.equals(other.baseClock)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return this.baseClock.hashCode() ^ (int)(this.tickNanos ^ this.tickNanos >>> 32);
        }

        public String toString() {
            return "TickClock[" + this.baseClock + "," + Duration.ofNanos(this.tickNanos) + "]";
        }
    }

    static final class FixedClock
    extends Clock
    implements Serializable {
        private static final long serialVersionUID = 7430389292664866958L;
        private final Instant instant;
        private final ZoneId zone;

        FixedClock(Instant fixedInstant, ZoneId zone) {
            this.instant = fixedInstant;
            this.zone = zone;
        }

        @Override
        public ZoneId getZone() {
            return this.zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            if (zone.equals(this.zone)) {
                return this;
            }
            return new FixedClock(this.instant, zone);
        }

        @Override
        public long millis() {
            return this.instant.toEpochMilli();
        }

        @Override
        public Instant instant() {
            return this.instant;
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof FixedClock)) return false;
            FixedClock other = (FixedClock)obj;
            if (!this.instant.equals(other.instant)) return false;
            if (!this.zone.equals(other.zone)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return this.instant.hashCode() ^ this.zone.hashCode();
        }

        public String toString() {
            return "FixedClock[" + this.instant + "," + this.zone + "]";
        }
    }

    static final class OffsetClock
    extends Clock
    implements Serializable {
        private static final long serialVersionUID = 2007484719125426256L;
        private final Clock baseClock;
        private final Duration offset;

        OffsetClock(Clock baseClock, Duration offset) {
            this.baseClock = baseClock;
            this.offset = offset;
        }

        @Override
        public ZoneId getZone() {
            return this.baseClock.getZone();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            if (zone.equals(this.baseClock.getZone())) {
                return this;
            }
            return new OffsetClock(this.baseClock.withZone(zone), this.offset);
        }

        @Override
        public long millis() {
            return Math.addExact(this.baseClock.millis(), this.offset.toMillis());
        }

        @Override
        public Instant instant() {
            return this.baseClock.instant().plus(this.offset);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof OffsetClock)) return false;
            OffsetClock other = (OffsetClock)obj;
            if (!this.baseClock.equals(other.baseClock)) return false;
            if (!this.offset.equals(other.offset)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return this.baseClock.hashCode() ^ this.offset.hashCode();
        }

        public String toString() {
            return "OffsetClock[" + this.baseClock + "," + this.offset + "]";
        }
    }

    static final class SourceClock
    extends Clock
    implements Serializable {
        private static final long serialVersionUID = 235386528762398L;
        private final InstantSource baseSource;
        private final ZoneId zone;

        SourceClock(InstantSource baseSource, ZoneId zone) {
            this.baseSource = baseSource;
            this.zone = zone;
        }

        @Override
        public ZoneId getZone() {
            return this.zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            if (zone.equals(this.zone)) {
                return this;
            }
            return new SourceClock(this.baseSource, zone);
        }

        @Override
        public long millis() {
            return this.baseSource.millis();
        }

        @Override
        public Instant instant() {
            return this.baseSource.instant();
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SourceClock)) return false;
            SourceClock other = (SourceClock)obj;
            if (!this.zone.equals(other.zone)) return false;
            if (!this.baseSource.equals(other.baseSource)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return this.baseSource.hashCode() ^ this.zone.hashCode();
        }

        public String toString() {
            return "SourceClock[" + this.baseSource + "," + this.zone + "]";
        }
    }

    static final class SystemInstantSource
    implements InstantSource,
    Serializable {
        private static final long serialVersionUID = 3232399674412L;
        static final SystemInstantSource INSTANCE = new SystemInstantSource();

        SystemInstantSource() {
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.system(zone);
        }

        @Override
        public long millis() {
            return System.currentTimeMillis();
        }

        @Override
        public Instant instant() {
            return Clock.currentInstant();
        }

        public boolean equals(Object obj) {
            return obj instanceof SystemInstantSource;
        }

        public int hashCode() {
            return SystemInstantSource.class.hashCode();
        }

        public String toString() {
            return "SystemInstantSource";
        }

        private Object readResolve() throws ObjectStreamException {
            return INSTANCE;
        }
    }
}

