/*
 * Decompiled with CFR 0.152.
 */
package java.time;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.Ser;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
import java.util.Objects;
import jdk.internal.ValueBased;

@ValueBased
public final class Instant
implements Temporal,
TemporalAdjuster,
Comparable<Instant>,
Serializable {
    public static final Instant EPOCH = new Instant(0L, 0);
    private static final long MIN_SECOND = -31557014167219200L;
    private static final long MAX_SECOND = 31556889864403199L;
    public static final Instant MIN = Instant.ofEpochSecond(-31557014167219200L, 0L);
    public static final Instant MAX = Instant.ofEpochSecond(31556889864403199L, 999999999L);
    private static final long serialVersionUID = -665713676816604388L;
    private final long seconds;
    private final int nanos;

    public static Instant now() {
        return Clock.currentInstant();
    }

    public static Instant now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        return clock.instant();
    }

    public static Instant ofEpochSecond(long epochSecond) {
        return Instant.create(epochSecond, 0);
    }

    public static Instant ofEpochSecond(long epochSecond, long nanoAdjustment) {
        long secs = Math.addExact(epochSecond, Math.floorDiv(nanoAdjustment, 1000000000L));
        int nos = (int)Math.floorMod(nanoAdjustment, 1000000000L);
        return Instant.create(secs, nos);
    }

    public static Instant ofEpochMilli(long epochMilli) {
        long secs = Math.floorDiv(epochMilli, 1000);
        int mos = Math.floorMod(epochMilli, 1000);
        return Instant.create(secs, mos * 1000000);
    }

    public static Instant from(TemporalAccessor temporal) {
        if (temporal instanceof Instant) {
            return (Instant)temporal;
        }
        Objects.requireNonNull(temporal, "temporal");
        try {
            long instantSecs = temporal.getLong(ChronoField.INSTANT_SECONDS);
            int nanoOfSecond = temporal.get(ChronoField.NANO_OF_SECOND);
            return Instant.ofEpochSecond(instantSecs, nanoOfSecond);
        }
        catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain Instant from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName(), ex);
        }
    }

    public static Instant parse(CharSequence text) {
        return DateTimeFormatter.ISO_INSTANT.parse(text, Instant::from);
    }

    private static Instant create(long seconds, int nanoOfSecond) {
        if ((seconds | (long)nanoOfSecond) == 0L) {
            return EPOCH;
        }
        if (seconds < -31557014167219200L || seconds > 31556889864403199L) {
            throw new DateTimeException("Instant exceeds minimum or maximum instant");
        }
        return new Instant(seconds, nanoOfSecond);
    }

    private Instant(long epochSecond, int nanos) {
        this.seconds = epochSecond;
        this.nanos = nanos;
    }

    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field == ChronoField.INSTANT_SECONDS || field == ChronoField.NANO_OF_SECOND || field == ChronoField.MICRO_OF_SECOND || field == ChronoField.MILLI_OF_SECOND;
        }
        return field != null && field.isSupportedBy(this);
    }

    @Override
    public boolean isSupported(TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return unit.isTimeBased() || unit == ChronoUnit.DAYS;
        }
        return unit != null && unit.isSupportedBy(this);
    }

    @Override
    public ValueRange range(TemporalField field) {
        return Temporal.super.range(field);
    }

    @Override
    public int get(TemporalField field) {
        if (field instanceof ChronoField) {
            ChronoField chronoField = (ChronoField)field;
            return switch (chronoField) {
                case ChronoField.NANO_OF_SECOND -> this.nanos;
                case ChronoField.MICRO_OF_SECOND -> this.nanos / 1000;
                case ChronoField.MILLI_OF_SECOND -> this.nanos / 1000000;
                default -> throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            };
        }
        return this.range(field).checkValidIntValue(field.getFrom(this), field);
    }

    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            ChronoField chronoField = (ChronoField)field;
            return switch (chronoField) {
                case ChronoField.NANO_OF_SECOND -> this.nanos;
                case ChronoField.MICRO_OF_SECOND -> this.nanos / 1000;
                case ChronoField.MILLI_OF_SECOND -> this.nanos / 1000000;
                case ChronoField.INSTANT_SECONDS -> this.seconds;
                default -> throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            };
        }
        return field.getFrom(this);
    }

    public long getEpochSecond() {
        return this.seconds;
    }

    public int getNano() {
        return this.nanos;
    }

    @Override
    public Instant with(TemporalAdjuster adjuster) {
        return (Instant)adjuster.adjustInto(this);
    }

    @Override
    public Instant with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField chronoField = (ChronoField)field;
            chronoField.checkValidValue(newValue);
            return switch (chronoField) {
                case ChronoField.MILLI_OF_SECOND -> {
                    int nval = (int)newValue * 1000000;
                    if (nval != this.nanos) {
                        yield Instant.create(this.seconds, nval);
                    }
                    yield this;
                }
                case ChronoField.MICRO_OF_SECOND -> {
                    int nval = (int)newValue * 1000;
                    if (nval != this.nanos) {
                        yield Instant.create(this.seconds, nval);
                    }
                    yield this;
                }
                case ChronoField.NANO_OF_SECOND -> {
                    if (newValue != (long)this.nanos) {
                        yield Instant.create(this.seconds, (int)newValue);
                    }
                    yield this;
                }
                case ChronoField.INSTANT_SECONDS -> {
                    if (newValue != this.seconds) {
                        yield Instant.create(newValue, this.nanos);
                    }
                    yield this;
                }
                default -> throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            };
        }
        return field.adjustInto(this, newValue);
    }

    public Instant truncatedTo(TemporalUnit unit) {
        if (unit == ChronoUnit.NANOS) {
            return this;
        }
        Duration unitDur = unit.getDuration();
        if (unitDur.getSeconds() > 86400L) {
            throw new UnsupportedTemporalTypeException("Unit is too large to be used for truncation");
        }
        long dur = unitDur.toNanos();
        if (86400000000000L % dur != 0L) {
            throw new UnsupportedTemporalTypeException("Unit must divide into a standard day without remainder");
        }
        long nod = this.seconds % 86400L * 1000000000L + (long)this.nanos;
        long result = Math.floorDiv(nod, dur) * dur;
        return this.plusNanos(result - nod);
    }

    @Override
    public Instant plus(TemporalAmount amountToAdd) {
        return (Instant)amountToAdd.addTo(this);
    }

    @Override
    public Instant plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            ChronoUnit chronoUnit = (ChronoUnit)unit;
            return switch (chronoUnit) {
                case ChronoUnit.NANOS -> this.plusNanos(amountToAdd);
                case ChronoUnit.MICROS -> this.plus(amountToAdd / 1000000L, amountToAdd % 1000000L * 1000L);
                case ChronoUnit.MILLIS -> this.plusMillis(amountToAdd);
                case ChronoUnit.SECONDS -> this.plusSeconds(amountToAdd);
                case ChronoUnit.MINUTES -> this.plusSeconds(Math.multiplyExact(amountToAdd, 60));
                case ChronoUnit.HOURS -> this.plusSeconds(Math.multiplyExact(amountToAdd, 3600));
                case ChronoUnit.HALF_DAYS -> this.plusSeconds(Math.multiplyExact(amountToAdd, 43200));
                case ChronoUnit.DAYS -> this.plusSeconds(Math.multiplyExact(amountToAdd, 86400));
                default -> throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
            };
        }
        return unit.addTo(this, amountToAdd);
    }

    public Instant plusSeconds(long secondsToAdd) {
        if (secondsToAdd == 0L) {
            return this;
        }
        long epochSec = Math.addExact(this.seconds, secondsToAdd);
        return Instant.create(epochSec, this.nanos);
    }

    public Instant plusMillis(long millisToAdd) {
        return this.plus(millisToAdd / 1000L, millisToAdd % 1000L * 1000000L);
    }

    public Instant plusNanos(long nanosToAdd) {
        return this.plus(0L, nanosToAdd);
    }

    private Instant plus(long secondsToAdd, long nanosToAdd) {
        if ((secondsToAdd | nanosToAdd) == 0L) {
            return this;
        }
        long epochSec = Math.addExact(this.seconds, secondsToAdd);
        epochSec = Math.addExact(epochSec, nanosToAdd / 1000000000L);
        long nanoAdjustment = (long)this.nanos + (nanosToAdd %= 1000000000L);
        return Instant.ofEpochSecond(epochSec, nanoAdjustment);
    }

    @Override
    public Instant minus(TemporalAmount amountToSubtract) {
        return (Instant)amountToSubtract.subtractFrom(this);
    }

    @Override
    public Instant minus(long amountToSubtract, TemporalUnit unit) {
        return amountToSubtract == Long.MIN_VALUE ? this.plus(Long.MAX_VALUE, unit).plus(1L, unit) : this.plus(-amountToSubtract, unit);
    }

    public Instant minusSeconds(long secondsToSubtract) {
        if (secondsToSubtract == Long.MIN_VALUE) {
            return this.plusSeconds(Long.MAX_VALUE).plusSeconds(1L);
        }
        return this.plusSeconds(-secondsToSubtract);
    }

    public Instant minusMillis(long millisToSubtract) {
        if (millisToSubtract == Long.MIN_VALUE) {
            return this.plusMillis(Long.MAX_VALUE).plusMillis(1L);
        }
        return this.plusMillis(-millisToSubtract);
    }

    public Instant minusNanos(long nanosToSubtract) {
        if (nanosToSubtract == Long.MIN_VALUE) {
            return this.plusNanos(Long.MAX_VALUE).plusNanos(1L);
        }
        return this.plusNanos(-nanosToSubtract);
    }

    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.precision()) {
            return (R)ChronoUnit.NANOS;
        }
        if (query == TemporalQueries.chronology() || query == TemporalQueries.zoneId() || query == TemporalQueries.zone() || query == TemporalQueries.offset() || query == TemporalQueries.localDate() || query == TemporalQueries.localTime()) {
            return null;
        }
        return query.queryFrom(this);
    }

    @Override
    public Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.INSTANT_SECONDS, this.seconds).with(ChronoField.NANO_OF_SECOND, this.nanos);
    }

    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        Instant end = Instant.from(endExclusive);
        if (unit instanceof ChronoUnit) {
            ChronoUnit chronoUnit = (ChronoUnit)unit;
            return switch (chronoUnit) {
                case ChronoUnit.NANOS -> this.nanosUntil(end);
                case ChronoUnit.MICROS -> this.microsUntil(end);
                case ChronoUnit.MILLIS -> this.millisUntil(end);
                case ChronoUnit.SECONDS -> this.secondsUntil(end);
                case ChronoUnit.MINUTES -> this.secondsUntil(end) / 60L;
                case ChronoUnit.HOURS -> this.secondsUntil(end) / 3600L;
                case ChronoUnit.HALF_DAYS -> this.secondsUntil(end) / 43200L;
                case ChronoUnit.DAYS -> this.secondsUntil(end) / 86400L;
                default -> throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
            };
        }
        return unit.between(this, end);
    }

    private long nanosUntil(Instant end) {
        long secsDiff = Math.subtractExact(end.seconds, this.seconds);
        long totalNanos = Math.multiplyExact(secsDiff, 1000000000L);
        return Math.addExact(totalNanos, (long)(end.nanos - this.nanos));
    }

    private long microsUntil(Instant end) {
        long microsDiff = Math.multiplyExact(end.seconds - this.seconds, 1000000L);
        int nanosDiff = end.nanos - this.nanos;
        if (microsDiff > 0L && nanosDiff < 0) {
            return microsDiff - 1000000L + (long)((nanosDiff + 1000000000) / 1000);
        }
        if (microsDiff < 0L && nanosDiff > 0) {
            return microsDiff + 1000000L + (long)((nanosDiff - 1000000000) / 1000);
        }
        return Math.addExact(microsDiff, (long)(nanosDiff / 1000));
    }

    private long millisUntil(Instant end) {
        long millisDiff = Math.multiplyExact(end.seconds - this.seconds, 1000L);
        int nanosDiff = end.nanos - this.nanos;
        if (millisDiff > 0L && nanosDiff < 0) {
            return millisDiff - 1000L + (long)((nanosDiff + 1000000000) / 1000000);
        }
        if (millisDiff < 0L && nanosDiff > 0) {
            return millisDiff + 1000L + (long)((nanosDiff - 1000000000) / 1000000);
        }
        return Math.addExact(millisDiff, (long)(nanosDiff / 1000000));
    }

    private long secondsUntil(Instant end) {
        long secsDiff = Math.subtractExact(end.seconds, this.seconds);
        int nanosDiff = end.nanos - this.nanos;
        if (secsDiff > 0L && nanosDiff < 0) {
            --secsDiff;
        } else if (secsDiff < 0L && nanosDiff > 0) {
            ++secsDiff;
        }
        return secsDiff;
    }

    public OffsetDateTime atOffset(ZoneOffset offset) {
        return OffsetDateTime.ofInstant(this, offset);
    }

    public ZonedDateTime atZone(ZoneId zone) {
        return ZonedDateTime.ofInstant(this, zone);
    }

    public long toEpochMilli() {
        if (this.seconds < 0L && this.nanos > 0) {
            long millis = Math.multiplyExact(this.seconds + 1L, 1000);
            long adjustment = this.nanos / 1000000 - 1000;
            return Math.addExact(millis, adjustment);
        }
        long millis = Math.multiplyExact(this.seconds, 1000);
        return Math.addExact(millis, (long)(this.nanos / 1000000));
    }

    @Override
    public int compareTo(Instant otherInstant) {
        int cmp = Long.compare(this.seconds, otherInstant.seconds);
        if (cmp != 0) {
            return cmp;
        }
        return this.nanos - otherInstant.nanos;
    }

    public boolean isAfter(Instant otherInstant) {
        return this.compareTo(otherInstant) > 0;
    }

    public boolean isBefore(Instant otherInstant) {
        return this.compareTo(otherInstant) < 0;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Instant)) return false;
        Instant otherInstant = (Instant)other;
        if (this.seconds != otherInstant.seconds) return false;
        if (this.nanos != otherInstant.nanos) return false;
        return true;
    }

    public int hashCode() {
        return (int)(this.seconds ^ this.seconds >>> 32) + 51 * this.nanos;
    }

    public String toString() {
        return DateTimeFormatter.ISO_INSTANT.format(this);
    }

    private Object writeReplace() {
        return new Ser(2, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeLong(this.seconds);
        out.writeInt(this.nanos);
    }

    static Instant readExternal(DataInput in) throws IOException {
        long seconds = in.readLong();
        int nanos = in.readInt();
        return Instant.ofEpochSecond(seconds, nanos);
    }
}

