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
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.Ser;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDateTime;
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
import java.time.temporal.ValueRange;
import java.time.zone.ZoneRules;
import java.util.Objects;
import jdk.internal.ValueBased;

@ValueBased
public final class LocalDateTime
implements Temporal,
TemporalAdjuster,
ChronoLocalDateTime<LocalDate>,
Serializable {
    public static final LocalDateTime MIN = LocalDateTime.of(LocalDate.MIN, LocalTime.MIN);
    public static final LocalDateTime MAX = LocalDateTime.of(LocalDate.MAX, LocalTime.MAX);
    private static final long serialVersionUID = 6207766400415563566L;
    private final LocalDate date;
    private final LocalTime time;

    public static LocalDateTime now() {
        return LocalDateTime.now(Clock.systemDefaultZone());
    }

    public static LocalDateTime now(ZoneId zone) {
        return LocalDateTime.now(Clock.system(zone));
    }

    public static LocalDateTime now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        Instant now = clock.instant();
        ZoneOffset offset = clock.getZone().getRules().getOffset(now);
        return LocalDateTime.ofEpochSecond(now.getEpochSecond(), now.getNano(), offset);
    }

    public static LocalDateTime of(int year, Month month, int dayOfMonth, int hour, int minute) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        LocalTime time = LocalTime.of(hour, minute);
        return new LocalDateTime(date, time);
    }

    public static LocalDateTime of(int year, Month month, int dayOfMonth, int hour, int minute, int second) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        LocalTime time = LocalTime.of(hour, minute, second);
        return new LocalDateTime(date, time);
    }

    public static LocalDateTime of(int year, Month month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        LocalTime time = LocalTime.of(hour, minute, second, nanoOfSecond);
        return new LocalDateTime(date, time);
    }

    public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        LocalTime time = LocalTime.of(hour, minute);
        return new LocalDateTime(date, time);
    }

    public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        LocalTime time = LocalTime.of(hour, minute, second);
        return new LocalDateTime(date, time);
    }

    public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        LocalTime time = LocalTime.of(hour, minute, second, nanoOfSecond);
        return new LocalDateTime(date, time);
    }

    public static LocalDateTime of(LocalDate date, LocalTime time) {
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(time, "time");
        return new LocalDateTime(date, time);
    }

    public static LocalDateTime ofInstant(Instant instant, ZoneId zone) {
        Objects.requireNonNull(instant, "instant");
        Objects.requireNonNull(zone, "zone");
        ZoneRules rules = zone.getRules();
        ZoneOffset offset = rules.getOffset(instant);
        return LocalDateTime.ofEpochSecond(instant.getEpochSecond(), instant.getNano(), offset);
    }

    public static LocalDateTime ofEpochSecond(long epochSecond, int nanoOfSecond, ZoneOffset offset) {
        Objects.requireNonNull(offset, "offset");
        ChronoField.NANO_OF_SECOND.checkValidValue(nanoOfSecond);
        long localSecond = epochSecond + (long)offset.getTotalSeconds();
        long localEpochDay = Math.floorDiv(localSecond, 86400);
        int secsOfDay = Math.floorMod(localSecond, 86400);
        LocalDate date = LocalDate.ofEpochDay(localEpochDay);
        LocalTime time = LocalTime.ofNanoOfDay((long)secsOfDay * 1000000000L + (long)nanoOfSecond);
        return new LocalDateTime(date, time);
    }

    public static LocalDateTime from(TemporalAccessor temporal) {
        if (temporal instanceof LocalDateTime) {
            return (LocalDateTime)temporal;
        }
        if (temporal instanceof ZonedDateTime) {
            return ((ZonedDateTime)temporal).toLocalDateTime();
        }
        if (temporal instanceof OffsetDateTime) {
            return ((OffsetDateTime)temporal).toLocalDateTime();
        }
        try {
            LocalDate date = LocalDate.from(temporal);
            LocalTime time = LocalTime.from(temporal);
            return new LocalDateTime(date, time);
        }
        catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain LocalDateTime from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName(), ex);
        }
    }

    public static LocalDateTime parse(CharSequence text) {
        return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static LocalDateTime parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(text, LocalDateTime::from);
    }

    private LocalDateTime(LocalDate date, LocalTime time) {
        this.date = date;
        this.time = time;
    }

    private LocalDateTime with(LocalDate newDate, LocalTime newTime) {
        if (this.date == newDate && this.time == newTime) {
            return this;
        }
        return new LocalDateTime(newDate, newTime);
    }

    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            ChronoField chronoField = (ChronoField)field;
            return chronoField.isDateBased() || chronoField.isTimeBased();
        }
        return field != null && field.isSupportedBy(this);
    }

    @Override
    public boolean isSupported(TemporalUnit unit) {
        return ChronoLocalDateTime.super.isSupported(unit);
    }

    @Override
    public ValueRange range(TemporalField field) {
        if (field instanceof ChronoField) {
            ChronoField chronoField = (ChronoField)field;
            return chronoField.isTimeBased() ? this.time.range(field) : this.date.range(field);
        }
        return field.rangeRefinedBy(this);
    }

    @Override
    public int get(TemporalField field) {
        if (field instanceof ChronoField) {
            ChronoField chronoField = (ChronoField)field;
            return chronoField.isTimeBased() ? this.time.get(field) : this.date.get(field);
        }
        return ChronoLocalDateTime.super.get(field);
    }

    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            ChronoField chronoField = (ChronoField)field;
            return chronoField.isTimeBased() ? this.time.getLong(field) : this.date.getLong(field);
        }
        return field.getFrom(this);
    }

    @Override
    public LocalDate toLocalDate() {
        return this.date;
    }

    public int getYear() {
        return this.date.getYear();
    }

    public int getMonthValue() {
        return this.date.getMonthValue();
    }

    public Month getMonth() {
        return this.date.getMonth();
    }

    public int getDayOfMonth() {
        return this.date.getDayOfMonth();
    }

    public int getDayOfYear() {
        return this.date.getDayOfYear();
    }

    public DayOfWeek getDayOfWeek() {
        return this.date.getDayOfWeek();
    }

    @Override
    public LocalTime toLocalTime() {
        return this.time;
    }

    public int getHour() {
        return this.time.getHour();
    }

    public int getMinute() {
        return this.time.getMinute();
    }

    public int getSecond() {
        return this.time.getSecond();
    }

    public int getNano() {
        return this.time.getNano();
    }

    @Override
    public LocalDateTime with(TemporalAdjuster adjuster) {
        if (adjuster instanceof LocalDate) {
            return this.with((LocalDate)adjuster, this.time);
        }
        if (adjuster instanceof LocalTime) {
            return this.with(this.date, (LocalTime)adjuster);
        }
        if (adjuster instanceof LocalDateTime) {
            return (LocalDateTime)adjuster;
        }
        return (LocalDateTime)adjuster.adjustInto(this);
    }

    @Override
    public LocalDateTime with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField chronoField = (ChronoField)field;
            if (chronoField.isTimeBased()) {
                return this.with(this.date, this.time.with(field, newValue));
            }
            return this.with(this.date.with(field, newValue), this.time);
        }
        return field.adjustInto(this, newValue);
    }

    public LocalDateTime withYear(int year) {
        return this.with(this.date.withYear(year), this.time);
    }

    public LocalDateTime withMonth(int month) {
        return this.with(this.date.withMonth(month), this.time);
    }

    public LocalDateTime withDayOfMonth(int dayOfMonth) {
        return this.with(this.date.withDayOfMonth(dayOfMonth), this.time);
    }

    public LocalDateTime withDayOfYear(int dayOfYear) {
        return this.with(this.date.withDayOfYear(dayOfYear), this.time);
    }

    public LocalDateTime withHour(int hour) {
        LocalTime newTime = this.time.withHour(hour);
        return this.with(this.date, newTime);
    }

    public LocalDateTime withMinute(int minute) {
        LocalTime newTime = this.time.withMinute(minute);
        return this.with(this.date, newTime);
    }

    public LocalDateTime withSecond(int second) {
        LocalTime newTime = this.time.withSecond(second);
        return this.with(this.date, newTime);
    }

    public LocalDateTime withNano(int nanoOfSecond) {
        LocalTime newTime = this.time.withNano(nanoOfSecond);
        return this.with(this.date, newTime);
    }

    public LocalDateTime truncatedTo(TemporalUnit unit) {
        return this.with(this.date, this.time.truncatedTo(unit));
    }

    @Override
    public LocalDateTime plus(TemporalAmount amountToAdd) {
        if (amountToAdd instanceof Period) {
            Period periodToAdd = (Period)amountToAdd;
            return this.with(this.date.plus(periodToAdd), this.time);
        }
        Objects.requireNonNull(amountToAdd, "amountToAdd");
        return (LocalDateTime)amountToAdd.addTo(this);
    }

    @Override
    public LocalDateTime plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            ChronoUnit chronoUnit = (ChronoUnit)unit;
            return switch (chronoUnit) {
                case ChronoUnit.NANOS -> this.plusNanos(amountToAdd);
                case ChronoUnit.MICROS -> this.plusDays(amountToAdd / 86400000000L).plusNanos(amountToAdd % 86400000000L * 1000L);
                case ChronoUnit.MILLIS -> this.plusDays(amountToAdd / 86400000L).plusNanos(amountToAdd % 86400000L * 1000000L);
                case ChronoUnit.SECONDS -> this.plusSeconds(amountToAdd);
                case ChronoUnit.MINUTES -> this.plusMinutes(amountToAdd);
                case ChronoUnit.HOURS -> this.plusHours(amountToAdd);
                case ChronoUnit.HALF_DAYS -> this.plusDays(amountToAdd / 256L).plusHours(amountToAdd % 256L * 12L);
                default -> this.with(this.date.plus(amountToAdd, unit), this.time);
            };
        }
        return unit.addTo(this, amountToAdd);
    }

    public LocalDateTime plusYears(long years) {
        LocalDate newDate = this.date.plusYears(years);
        return this.with(newDate, this.time);
    }

    public LocalDateTime plusMonths(long months) {
        LocalDate newDate = this.date.plusMonths(months);
        return this.with(newDate, this.time);
    }

    public LocalDateTime plusWeeks(long weeks) {
        LocalDate newDate = this.date.plusWeeks(weeks);
        return this.with(newDate, this.time);
    }

    public LocalDateTime plusDays(long days) {
        LocalDate newDate = this.date.plusDays(days);
        return this.with(newDate, this.time);
    }

    public LocalDateTime plusHours(long hours) {
        return this.plusWithOverflow(this.date, hours, 0L, 0L, 0L, 1);
    }

    public LocalDateTime plusMinutes(long minutes) {
        return this.plusWithOverflow(this.date, 0L, minutes, 0L, 0L, 1);
    }

    public LocalDateTime plusSeconds(long seconds) {
        return this.plusWithOverflow(this.date, 0L, 0L, seconds, 0L, 1);
    }

    public LocalDateTime plusNanos(long nanos) {
        return this.plusWithOverflow(this.date, 0L, 0L, 0L, nanos, 1);
    }

    @Override
    public LocalDateTime minus(TemporalAmount amountToSubtract) {
        if (amountToSubtract instanceof Period) {
            Period periodToSubtract = (Period)amountToSubtract;
            return this.with(this.date.minus(periodToSubtract), this.time);
        }
        Objects.requireNonNull(amountToSubtract, "amountToSubtract");
        return (LocalDateTime)amountToSubtract.subtractFrom(this);
    }

    @Override
    public LocalDateTime minus(long amountToSubtract, TemporalUnit unit) {
        return amountToSubtract == Long.MIN_VALUE ? this.plus(Long.MAX_VALUE, unit).plus(1L, unit) : this.plus(-amountToSubtract, unit);
    }

    public LocalDateTime minusYears(long years) {
        return years == Long.MIN_VALUE ? this.plusYears(Long.MAX_VALUE).plusYears(1L) : this.plusYears(-years);
    }

    public LocalDateTime minusMonths(long months) {
        return months == Long.MIN_VALUE ? this.plusMonths(Long.MAX_VALUE).plusMonths(1L) : this.plusMonths(-months);
    }

    public LocalDateTime minusWeeks(long weeks) {
        return weeks == Long.MIN_VALUE ? this.plusWeeks(Long.MAX_VALUE).plusWeeks(1L) : this.plusWeeks(-weeks);
    }

    public LocalDateTime minusDays(long days) {
        return days == Long.MIN_VALUE ? this.plusDays(Long.MAX_VALUE).plusDays(1L) : this.plusDays(-days);
    }

    public LocalDateTime minusHours(long hours) {
        return this.plusWithOverflow(this.date, hours, 0L, 0L, 0L, -1);
    }

    public LocalDateTime minusMinutes(long minutes) {
        return this.plusWithOverflow(this.date, 0L, minutes, 0L, 0L, -1);
    }

    public LocalDateTime minusSeconds(long seconds) {
        return this.plusWithOverflow(this.date, 0L, 0L, seconds, 0L, -1);
    }

    public LocalDateTime minusNanos(long nanos) {
        return this.plusWithOverflow(this.date, 0L, 0L, 0L, nanos, -1);
    }

    private LocalDateTime plusWithOverflow(LocalDate newDate, long hours, long minutes, long seconds, long nanos, int sign) {
        if ((hours | minutes | seconds | nanos) == 0L) {
            return this.with(newDate, this.time);
        }
        long totDays = nanos / 86400000000000L + seconds / 86400L + minutes / 1440L + hours / 24L;
        totDays *= (long)sign;
        long totNanos = nanos % 86400000000000L + seconds % 86400L * 1000000000L + minutes % 1440L * 60000000000L + hours % 24L * 3600000000000L;
        long curNoD = this.time.toNanoOfDay();
        totNanos = totNanos * (long)sign + curNoD;
        long newNoD = Math.floorMod(totNanos, 86400000000000L);
        LocalTime newTime = newNoD == curNoD ? this.time : LocalTime.ofNanoOfDay(newNoD);
        return this.with(newDate.plusDays(totDays += Math.floorDiv(totNanos, 86400000000000L)), newTime);
    }

    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.localDate()) {
            return (R)this.date;
        }
        return ChronoLocalDateTime.super.query(query);
    }

    @Override
    public Temporal adjustInto(Temporal temporal) {
        return ChronoLocalDateTime.super.adjustInto(temporal);
    }

    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        LocalDateTime end = LocalDateTime.from(endExclusive);
        if (unit instanceof ChronoUnit) {
            ChronoUnit chronoUnit = (ChronoUnit)unit;
            if (unit.isTimeBased()) {
                long amount = this.date.daysUntil(end.date);
                if (amount == 0L) {
                    return this.time.until(end.time, unit);
                }
                long timePart = end.time.toNanoOfDay() - this.time.toNanoOfDay();
                if (amount > 0L) {
                    --amount;
                    timePart += 86400000000000L;
                } else {
                    ++amount;
                    timePart -= 86400000000000L;
                }
                switch (chronoUnit) {
                    case NANOS: {
                        amount = Math.multiplyExact(amount, 86400000000000L);
                        break;
                    }
                    case MICROS: {
                        amount = Math.multiplyExact(amount, 86400000000L);
                        timePart /= 1000L;
                        break;
                    }
                    case MILLIS: {
                        amount = Math.multiplyExact(amount, 86400000L);
                        timePart /= 1000000L;
                        break;
                    }
                    case SECONDS: {
                        amount = Math.multiplyExact(amount, 86400);
                        timePart /= 1000000000L;
                        break;
                    }
                    case MINUTES: {
                        amount = Math.multiplyExact(amount, 1440);
                        timePart /= 60000000000L;
                        break;
                    }
                    case HOURS: {
                        amount = Math.multiplyExact(amount, 24);
                        timePart /= 3600000000000L;
                        break;
                    }
                    case HALF_DAYS: {
                        amount = Math.multiplyExact(amount, 2);
                        timePart /= 43200000000000L;
                    }
                }
                return Math.addExact(amount, timePart);
            }
            LocalDate endDate = end.date;
            if (endDate.isAfter(this.date) && end.time.isBefore(this.time)) {
                endDate = endDate.minusDays(1L);
            } else if (endDate.isBefore(this.date) && end.time.isAfter(this.time)) {
                endDate = endDate.plusDays(1L);
            }
            return this.date.until(endDate, unit);
        }
        return unit.between(this, end);
    }

    @Override
    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    public OffsetDateTime atOffset(ZoneOffset offset) {
        return OffsetDateTime.of(this, offset);
    }

    public ZonedDateTime atZone(ZoneId zone) {
        return ZonedDateTime.of(this, zone);
    }

    @Override
    public int compareTo(ChronoLocalDateTime<?> other) {
        if (other instanceof LocalDateTime) {
            return this.compareTo0((LocalDateTime)other);
        }
        return ChronoLocalDateTime.super.compareTo(other);
    }

    private int compareTo0(LocalDateTime other) {
        int cmp = this.date.compareTo0(other.toLocalDate());
        if (cmp == 0) {
            cmp = this.time.compareTo(other.toLocalTime());
        }
        return cmp;
    }

    @Override
    public boolean isAfter(ChronoLocalDateTime<?> other) {
        if (other instanceof LocalDateTime) {
            return this.compareTo0((LocalDateTime)other) > 0;
        }
        return ChronoLocalDateTime.super.isAfter(other);
    }

    @Override
    public boolean isBefore(ChronoLocalDateTime<?> other) {
        if (other instanceof LocalDateTime) {
            return this.compareTo0((LocalDateTime)other) < 0;
        }
        return ChronoLocalDateTime.super.isBefore(other);
    }

    @Override
    public boolean isEqual(ChronoLocalDateTime<?> other) {
        if (other instanceof LocalDateTime) {
            return this.compareTo0((LocalDateTime)other) == 0;
        }
        return ChronoLocalDateTime.super.isEqual(other);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LocalDateTime)) return false;
        LocalDateTime other = (LocalDateTime)obj;
        if (!this.date.equals(other.date)) return false;
        if (!this.time.equals(other.time)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.date.hashCode() ^ this.time.hashCode();
    }

    @Override
    public String toString() {
        return this.date.toString() + 'T' + this.time.toString();
    }

    private Object writeReplace() {
        return new Ser(5, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException {
        this.date.writeExternal(out);
        this.time.writeExternal(out);
    }

    static LocalDateTime readExternal(DataInput in) throws IOException {
        LocalDate date = LocalDate.readExternal(in);
        LocalTime time = LocalTime.readExternal(in);
        return LocalDateTime.of(date, time);
    }
}

