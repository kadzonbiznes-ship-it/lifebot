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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetTime;
import java.time.Ser;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
public final class LocalTime
implements Temporal,
TemporalAdjuster,
Comparable<LocalTime>,
Serializable {
    public static final LocalTime MIN;
    public static final LocalTime MAX;
    public static final LocalTime MIDNIGHT;
    public static final LocalTime NOON;
    private static final LocalTime[] HOURS;
    static final int HOURS_PER_DAY = 24;
    static final int MINUTES_PER_HOUR = 60;
    static final int MINUTES_PER_DAY = 1440;
    static final int SECONDS_PER_MINUTE = 60;
    static final int SECONDS_PER_HOUR = 3600;
    static final int SECONDS_PER_DAY = 86400;
    static final long MILLIS_PER_SECOND = 1000L;
    static final long MILLIS_PER_DAY = 86400000L;
    static final long MICROS_PER_SECOND = 1000000L;
    static final long MICROS_PER_DAY = 86400000000L;
    static final long NANOS_PER_MILLI = 1000000L;
    static final long NANOS_PER_SECOND = 1000000000L;
    static final long NANOS_PER_MINUTE = 60000000000L;
    static final long NANOS_PER_HOUR = 3600000000000L;
    static final long NANOS_PER_DAY = 86400000000000L;
    private static final long serialVersionUID = 6414437269572265201L;
    private final byte hour;
    private final byte minute;
    private final byte second;
    private final int nano;

    public static LocalTime now() {
        return LocalTime.now(Clock.systemDefaultZone());
    }

    public static LocalTime now(ZoneId zone) {
        return LocalTime.now(Clock.system(zone));
    }

    public static LocalTime now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        Instant now = clock.instant();
        return LocalTime.ofInstant(now, clock.getZone());
    }

    public static LocalTime of(int hour, int minute) {
        ChronoField.HOUR_OF_DAY.checkValidValue(hour);
        if (minute == 0) {
            return HOURS[hour];
        }
        ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
        return new LocalTime(hour, minute, 0, 0);
    }

    public static LocalTime of(int hour, int minute, int second) {
        ChronoField.HOUR_OF_DAY.checkValidValue(hour);
        if ((minute | second) == 0) {
            return HOURS[hour];
        }
        ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
        ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
        return new LocalTime(hour, minute, second, 0);
    }

    public static LocalTime of(int hour, int minute, int second, int nanoOfSecond) {
        ChronoField.HOUR_OF_DAY.checkValidValue(hour);
        ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
        ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
        ChronoField.NANO_OF_SECOND.checkValidValue(nanoOfSecond);
        return LocalTime.create(hour, minute, second, nanoOfSecond);
    }

    public static LocalTime ofInstant(Instant instant, ZoneId zone) {
        Objects.requireNonNull(instant, "instant");
        Objects.requireNonNull(zone, "zone");
        ZoneOffset offset = zone.getRules().getOffset(instant);
        long localSecond = instant.getEpochSecond() + (long)offset.getTotalSeconds();
        int secsOfDay = Math.floorMod(localSecond, 86400);
        return LocalTime.ofNanoOfDay((long)secsOfDay * 1000000000L + (long)instant.getNano());
    }

    public static LocalTime ofSecondOfDay(long secondOfDay) {
        ChronoField.SECOND_OF_DAY.checkValidValue(secondOfDay);
        int hours = (int)(secondOfDay / 3600L);
        int minutes = (int)((secondOfDay -= (long)(hours * 3600)) / 60L);
        return LocalTime.create(hours, minutes, (int)(secondOfDay -= (long)(minutes * 60)), 0);
    }

    public static LocalTime ofNanoOfDay(long nanoOfDay) {
        ChronoField.NANO_OF_DAY.checkValidValue(nanoOfDay);
        int hours = (int)(nanoOfDay / 3600000000000L);
        int minutes = (int)((nanoOfDay -= (long)hours * 3600000000000L) / 60000000000L);
        int seconds = (int)((nanoOfDay -= (long)minutes * 60000000000L) / 1000000000L);
        return LocalTime.create(hours, minutes, seconds, (int)(nanoOfDay -= (long)seconds * 1000000000L));
    }

    public static LocalTime from(TemporalAccessor temporal) {
        Objects.requireNonNull(temporal, "temporal");
        LocalTime time = temporal.query(TemporalQueries.localTime());
        if (time == null) {
            throw new DateTimeException("Unable to obtain LocalTime from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName());
        }
        return time;
    }

    public static LocalTime parse(CharSequence text) {
        return LocalTime.parse(text, DateTimeFormatter.ISO_LOCAL_TIME);
    }

    public static LocalTime parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(text, LocalTime::from);
    }

    private static LocalTime create(int hour, int minute, int second, int nanoOfSecond) {
        if ((minute | second | nanoOfSecond) == 0) {
            return HOURS[hour];
        }
        return new LocalTime(hour, minute, second, nanoOfSecond);
    }

    private LocalTime(int hour, int minute, int second, int nanoOfSecond) {
        this.hour = (byte)hour;
        this.minute = (byte)minute;
        this.second = (byte)second;
        this.nano = nanoOfSecond;
    }

    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field.isTimeBased();
        }
        return field != null && field.isSupportedBy(this);
    }

    @Override
    public boolean isSupported(TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            return unit.isTimeBased();
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
            return this.get0(field);
        }
        return Temporal.super.get(field);
    }

    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            if (field == ChronoField.NANO_OF_DAY) {
                return this.toNanoOfDay();
            }
            if (field == ChronoField.MICRO_OF_DAY) {
                return this.toNanoOfDay() / 1000L;
            }
            return this.get0(field);
        }
        return field.getFrom(this);
    }

    private int get0(TemporalField field) {
        return switch ((ChronoField)field) {
            case ChronoField.NANO_OF_SECOND -> this.nano;
            case ChronoField.NANO_OF_DAY -> throw new UnsupportedTemporalTypeException("Invalid field 'NanoOfDay' for get() method, use getLong() instead");
            case ChronoField.MICRO_OF_SECOND -> this.nano / 1000;
            case ChronoField.MICRO_OF_DAY -> throw new UnsupportedTemporalTypeException("Invalid field 'MicroOfDay' for get() method, use getLong() instead");
            case ChronoField.MILLI_OF_SECOND -> this.nano / 1000000;
            case ChronoField.MILLI_OF_DAY -> (int)(this.toNanoOfDay() / 1000000L);
            case ChronoField.SECOND_OF_MINUTE -> this.second;
            case ChronoField.SECOND_OF_DAY -> this.toSecondOfDay();
            case ChronoField.MINUTE_OF_HOUR -> this.minute;
            case ChronoField.MINUTE_OF_DAY -> this.hour * 60 + this.minute;
            case ChronoField.HOUR_OF_AMPM -> this.hour % 12;
            case ChronoField.CLOCK_HOUR_OF_AMPM -> {
                int ham = this.hour % 12;
                if (ham % 12 == 0) {
                    yield 12;
                }
                yield ham;
            }
            case ChronoField.HOUR_OF_DAY -> this.hour;
            case ChronoField.CLOCK_HOUR_OF_DAY -> {
                if (this.hour == 0) {
                    yield 24;
                }
                yield this.hour;
            }
            case ChronoField.AMPM_OF_DAY -> this.hour / 12;
            default -> throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        };
    }

    public int getHour() {
        return this.hour;
    }

    public int getMinute() {
        return this.minute;
    }

    public int getSecond() {
        return this.second;
    }

    public int getNano() {
        return this.nano;
    }

    @Override
    public LocalTime with(TemporalAdjuster adjuster) {
        if (adjuster instanceof LocalTime) {
            return (LocalTime)adjuster;
        }
        return (LocalTime)adjuster.adjustInto(this);
    }

    @Override
    public LocalTime with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField chronoField = (ChronoField)field;
            chronoField.checkValidValue(newValue);
            return switch (chronoField) {
                case ChronoField.NANO_OF_SECOND -> this.withNano((int)newValue);
                case ChronoField.NANO_OF_DAY -> LocalTime.ofNanoOfDay(newValue);
                case ChronoField.MICRO_OF_SECOND -> this.withNano((int)newValue * 1000);
                case ChronoField.MICRO_OF_DAY -> LocalTime.ofNanoOfDay(newValue * 1000L);
                case ChronoField.MILLI_OF_SECOND -> this.withNano((int)newValue * 1000000);
                case ChronoField.MILLI_OF_DAY -> LocalTime.ofNanoOfDay(newValue * 1000000L);
                case ChronoField.SECOND_OF_MINUTE -> this.withSecond((int)newValue);
                case ChronoField.SECOND_OF_DAY -> this.plusSeconds(newValue - (long)this.toSecondOfDay());
                case ChronoField.MINUTE_OF_HOUR -> this.withMinute((int)newValue);
                case ChronoField.MINUTE_OF_DAY -> this.plusMinutes(newValue - (long)(this.hour * 60 + this.minute));
                case ChronoField.HOUR_OF_AMPM -> this.plusHours(newValue - (long)(this.hour % 12));
                case ChronoField.CLOCK_HOUR_OF_AMPM -> this.plusHours((newValue == 12L ? 0L : newValue) - (long)(this.hour % 12));
                case ChronoField.HOUR_OF_DAY -> this.withHour((int)newValue);
                case ChronoField.CLOCK_HOUR_OF_DAY -> this.withHour((int)(newValue == 24L ? 0L : newValue));
                case ChronoField.AMPM_OF_DAY -> this.plusHours((newValue - (long)(this.hour / 12)) * 12L);
                default -> throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            };
        }
        return field.adjustInto(this, newValue);
    }

    public LocalTime withHour(int hour) {
        if (this.hour == hour) {
            return this;
        }
        ChronoField.HOUR_OF_DAY.checkValidValue(hour);
        return LocalTime.create(hour, this.minute, this.second, this.nano);
    }

    public LocalTime withMinute(int minute) {
        if (this.minute == minute) {
            return this;
        }
        ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
        return LocalTime.create(this.hour, minute, this.second, this.nano);
    }

    public LocalTime withSecond(int second) {
        if (this.second == second) {
            return this;
        }
        ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
        return LocalTime.create(this.hour, this.minute, second, this.nano);
    }

    public LocalTime withNano(int nanoOfSecond) {
        if (this.nano == nanoOfSecond) {
            return this;
        }
        ChronoField.NANO_OF_SECOND.checkValidValue(nanoOfSecond);
        return LocalTime.create(this.hour, this.minute, this.second, nanoOfSecond);
    }

    public LocalTime truncatedTo(TemporalUnit unit) {
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
        long nod = this.toNanoOfDay();
        return LocalTime.ofNanoOfDay(nod / dur * dur);
    }

    @Override
    public LocalTime plus(TemporalAmount amountToAdd) {
        return (LocalTime)amountToAdd.addTo(this);
    }

    @Override
    public LocalTime plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            ChronoUnit chronoUnit = (ChronoUnit)unit;
            return switch (chronoUnit) {
                case ChronoUnit.NANOS -> this.plusNanos(amountToAdd);
                case ChronoUnit.MICROS -> this.plusNanos(amountToAdd % 86400000000L * 1000L);
                case ChronoUnit.MILLIS -> this.plusNanos(amountToAdd % 86400000L * 1000000L);
                case ChronoUnit.SECONDS -> this.plusSeconds(amountToAdd);
                case ChronoUnit.MINUTES -> this.plusMinutes(amountToAdd);
                case ChronoUnit.HOURS -> this.plusHours(amountToAdd);
                case ChronoUnit.HALF_DAYS -> this.plusHours(amountToAdd % 2L * 12L);
                default -> throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
            };
        }
        return unit.addTo(this, amountToAdd);
    }

    public LocalTime plusHours(long hoursToAdd) {
        if (hoursToAdd == 0L) {
            return this;
        }
        int newHour = ((int)(hoursToAdd % 24L) + this.hour + 24) % 24;
        return LocalTime.create(newHour, this.minute, this.second, this.nano);
    }

    public LocalTime plusMinutes(long minutesToAdd) {
        if (minutesToAdd == 0L) {
            return this;
        }
        int mofd = this.hour * 60 + this.minute;
        int newMofd = ((int)(minutesToAdd % 1440L) + mofd + 1440) % 1440;
        if (mofd == newMofd) {
            return this;
        }
        int newHour = newMofd / 60;
        int newMinute = newMofd % 60;
        return LocalTime.create(newHour, newMinute, this.second, this.nano);
    }

    public LocalTime plusSeconds(long secondstoAdd) {
        if (secondstoAdd == 0L) {
            return this;
        }
        int sofd = this.hour * 3600 + this.minute * 60 + this.second;
        int newSofd = ((int)(secondstoAdd % 86400L) + sofd + 86400) % 86400;
        if (sofd == newSofd) {
            return this;
        }
        int newHour = newSofd / 3600;
        int newMinute = newSofd / 60 % 60;
        int newSecond = newSofd % 60;
        return LocalTime.create(newHour, newMinute, newSecond, this.nano);
    }

    public LocalTime plusNanos(long nanosToAdd) {
        long newNofd;
        if (nanosToAdd == 0L) {
            return this;
        }
        long nofd = this.toNanoOfDay();
        if (nofd == (newNofd = (nanosToAdd % 86400000000000L + nofd + 86400000000000L) % 86400000000000L)) {
            return this;
        }
        int newHour = (int)(newNofd / 3600000000000L);
        int newMinute = (int)(newNofd / 60000000000L % 60L);
        int newSecond = (int)(newNofd / 1000000000L % 60L);
        int newNano = (int)(newNofd % 1000000000L);
        return LocalTime.create(newHour, newMinute, newSecond, newNano);
    }

    @Override
    public LocalTime minus(TemporalAmount amountToSubtract) {
        return (LocalTime)amountToSubtract.subtractFrom(this);
    }

    @Override
    public LocalTime minus(long amountToSubtract, TemporalUnit unit) {
        return amountToSubtract == Long.MIN_VALUE ? this.plus(Long.MAX_VALUE, unit).plus(1L, unit) : this.plus(-amountToSubtract, unit);
    }

    public LocalTime minusHours(long hoursToSubtract) {
        return this.plusHours(-(hoursToSubtract % 24L));
    }

    public LocalTime minusMinutes(long minutesToSubtract) {
        return this.plusMinutes(-(minutesToSubtract % 1440L));
    }

    public LocalTime minusSeconds(long secondsToSubtract) {
        return this.plusSeconds(-(secondsToSubtract % 86400L));
    }

    public LocalTime minusNanos(long nanosToSubtract) {
        return this.plusNanos(-(nanosToSubtract % 86400000000000L));
    }

    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.chronology() || query == TemporalQueries.zoneId() || query == TemporalQueries.zone() || query == TemporalQueries.offset()) {
            return null;
        }
        if (query == TemporalQueries.localTime()) {
            return (R)this;
        }
        if (query == TemporalQueries.localDate()) {
            return null;
        }
        if (query == TemporalQueries.precision()) {
            return (R)ChronoUnit.NANOS;
        }
        return query.queryFrom(this);
    }

    @Override
    public Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.NANO_OF_DAY, this.toNanoOfDay());
    }

    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        LocalTime end = LocalTime.from(endExclusive);
        if (unit instanceof ChronoUnit) {
            ChronoUnit chronoUnit = (ChronoUnit)unit;
            long nanosUntil = end.toNanoOfDay() - this.toNanoOfDay();
            return switch (chronoUnit) {
                case ChronoUnit.NANOS -> nanosUntil;
                case ChronoUnit.MICROS -> nanosUntil / 1000L;
                case ChronoUnit.MILLIS -> nanosUntil / 1000000L;
                case ChronoUnit.SECONDS -> nanosUntil / 1000000000L;
                case ChronoUnit.MINUTES -> nanosUntil / 60000000000L;
                case ChronoUnit.HOURS -> nanosUntil / 3600000000000L;
                case ChronoUnit.HALF_DAYS -> nanosUntil / 43200000000000L;
                default -> throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
            };
        }
        return unit.between(this, end);
    }

    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    public LocalDateTime atDate(LocalDate date) {
        return LocalDateTime.of(date, this);
    }

    public OffsetTime atOffset(ZoneOffset offset) {
        return OffsetTime.of(this, offset);
    }

    public int toSecondOfDay() {
        int total = this.hour * 3600;
        total += this.minute * 60;
        return total += this.second;
    }

    public long toNanoOfDay() {
        long total = (long)this.hour * 3600000000000L;
        total += (long)this.minute * 60000000000L;
        total += (long)this.second * 1000000000L;
        return total += (long)this.nano;
    }

    public long toEpochSecond(LocalDate date, ZoneOffset offset) {
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(offset, "offset");
        long epochDay = date.toEpochDay();
        long secs = epochDay * 86400L + (long)this.toSecondOfDay();
        return secs -= (long)offset.getTotalSeconds();
    }

    @Override
    public int compareTo(LocalTime other) {
        int cmp = Integer.compare(this.hour, other.hour);
        if (cmp == 0 && (cmp = Integer.compare(this.minute, other.minute)) == 0 && (cmp = Integer.compare(this.second, other.second)) == 0) {
            cmp = Integer.compare(this.nano, other.nano);
        }
        return cmp;
    }

    public boolean isAfter(LocalTime other) {
        return this.compareTo(other) > 0;
    }

    public boolean isBefore(LocalTime other) {
        return this.compareTo(other) < 0;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LocalTime)) return false;
        LocalTime other = (LocalTime)obj;
        if (this.hour != other.hour) return false;
        if (this.minute != other.minute) return false;
        if (this.second != other.second) return false;
        if (this.nano != other.nano) return false;
        return true;
    }

    public int hashCode() {
        return Long.hashCode(this.toNanoOfDay());
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(18);
        byte hourValue = this.hour;
        byte minuteValue = this.minute;
        byte secondValue = this.second;
        int nanoValue = this.nano;
        buf.append(hourValue < 10 ? "0" : "").append(hourValue).append(minuteValue < 10 ? ":0" : ":").append(minuteValue);
        if (secondValue > 0 || nanoValue > 0) {
            buf.append(secondValue < 10 ? ":0" : ":").append(secondValue);
            if (nanoValue > 0) {
                buf.append('.');
                if (nanoValue % 1000000 == 0) {
                    buf.append(Integer.toString(nanoValue / 1000000 + 1000).substring(1));
                } else if (nanoValue % 1000 == 0) {
                    buf.append(Integer.toString(nanoValue / 1000 + 1000000).substring(1));
                } else {
                    buf.append(Integer.toString(nanoValue + 1000000000).substring(1));
                }
            }
        }
        return buf.toString();
    }

    private Object writeReplace() {
        return new Ser(4, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException {
        if (this.nano == 0) {
            if (this.second == 0) {
                if (this.minute == 0) {
                    out.writeByte(~this.hour);
                } else {
                    out.writeByte(this.hour);
                    out.writeByte(~this.minute);
                }
            } else {
                out.writeByte(this.hour);
                out.writeByte(this.minute);
                out.writeByte(~this.second);
            }
        } else {
            out.writeByte(this.hour);
            out.writeByte(this.minute);
            out.writeByte(this.second);
            out.writeInt(this.nano);
        }
    }

    static LocalTime readExternal(DataInput in) throws IOException {
        int hour = in.readByte();
        int minute = 0;
        int second = 0;
        int nano = 0;
        if (hour < 0) {
            hour ^= 0xFFFFFFFF;
        } else {
            minute = in.readByte();
            if (minute < 0) {
                minute ^= 0xFFFFFFFF;
            } else {
                second = in.readByte();
                if (second < 0) {
                    second ^= 0xFFFFFFFF;
                } else {
                    nano = in.readInt();
                }
            }
        }
        return LocalTime.of(hour, minute, second, nano);
    }

    static {
        HOURS = new LocalTime[24];
        for (int i = 0; i < HOURS.length; ++i) {
            LocalTime.HOURS[i] = new LocalTime(i, 0, 0, 0);
        }
        MIDNIGHT = HOURS[0];
        NOON = HOURS[12];
        MIN = HOURS[0];
        MAX = new LocalTime(23, 59, 59, 999999999);
    }
}

