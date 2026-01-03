/*
 * Decompiled with CFR 0.152.
 */
package java.time;

import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.Ser;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.ChronoZonedDateTime;
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
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.List;
import java.util.Objects;
import jdk.internal.ValueBased;

@ValueBased
public final class ZonedDateTime
implements Temporal,
ChronoZonedDateTime<LocalDate>,
Serializable {
    private static final long serialVersionUID = -6260982410461394882L;
    private final LocalDateTime dateTime;
    private final ZoneOffset offset;
    private final ZoneId zone;

    public static ZonedDateTime now() {
        return ZonedDateTime.now(Clock.systemDefaultZone());
    }

    public static ZonedDateTime now(ZoneId zone) {
        return ZonedDateTime.now(Clock.system(zone));
    }

    public static ZonedDateTime now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        Instant now = clock.instant();
        return ZonedDateTime.ofInstant(now, clock.getZone());
    }

    public static ZonedDateTime of(LocalDate date, LocalTime time, ZoneId zone) {
        return ZonedDateTime.of(LocalDateTime.of(date, time), zone);
    }

    public static ZonedDateTime of(LocalDateTime localDateTime, ZoneId zone) {
        return ZonedDateTime.ofLocal(localDateTime, zone, null);
    }

    public static ZonedDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond, ZoneId zone) {
        LocalDateTime dt = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond);
        return ZonedDateTime.ofLocal(dt, zone, null);
    }

    public static ZonedDateTime ofLocal(LocalDateTime localDateTime, ZoneId zone, ZoneOffset preferredOffset) {
        ZoneOffset offset;
        Objects.requireNonNull(localDateTime, "localDateTime");
        Objects.requireNonNull(zone, "zone");
        if (zone instanceof ZoneOffset) {
            return new ZonedDateTime(localDateTime, (ZoneOffset)zone, zone);
        }
        ZoneRules rules = zone.getRules();
        List<ZoneOffset> validOffsets = rules.getValidOffsets(localDateTime);
        if (validOffsets.size() == 1) {
            offset = validOffsets.get(0);
        } else if (validOffsets.size() == 0) {
            ZoneOffsetTransition trans = rules.getTransition(localDateTime);
            localDateTime = localDateTime.plusSeconds(trans.getDuration().getSeconds());
            offset = trans.getOffsetAfter();
        } else {
            offset = preferredOffset != null && validOffsets.contains(preferredOffset) ? preferredOffset : Objects.requireNonNull(validOffsets.get(0), "offset");
        }
        return new ZonedDateTime(localDateTime, offset, zone);
    }

    public static ZonedDateTime ofInstant(Instant instant, ZoneId zone) {
        Objects.requireNonNull(instant, "instant");
        Objects.requireNonNull(zone, "zone");
        return ZonedDateTime.create(instant.getEpochSecond(), instant.getNano(), zone);
    }

    public static ZonedDateTime ofInstant(LocalDateTime localDateTime, ZoneOffset offset, ZoneId zone) {
        Objects.requireNonNull(localDateTime, "localDateTime");
        Objects.requireNonNull(offset, "offset");
        Objects.requireNonNull(zone, "zone");
        if (zone.getRules().isValidOffset(localDateTime, offset)) {
            return new ZonedDateTime(localDateTime, offset, zone);
        }
        return ZonedDateTime.create(localDateTime.toEpochSecond(offset), localDateTime.getNano(), zone);
    }

    private static ZonedDateTime create(long epochSecond, int nanoOfSecond, ZoneId zone) {
        ZoneOffset offset = zone.getOffset(epochSecond);
        LocalDateTime ldt = LocalDateTime.ofEpochSecond(epochSecond, nanoOfSecond, offset);
        return new ZonedDateTime(ldt, offset, zone);
    }

    public static ZonedDateTime ofStrict(LocalDateTime localDateTime, ZoneOffset offset, ZoneId zone) {
        Objects.requireNonNull(localDateTime, "localDateTime");
        Objects.requireNonNull(offset, "offset");
        Objects.requireNonNull(zone, "zone");
        ZoneRules rules = zone.getRules();
        if (!rules.isValidOffset(localDateTime, offset)) {
            ZoneOffsetTransition trans = rules.getTransition(localDateTime);
            if (trans != null && trans.isGap()) {
                throw new DateTimeException("LocalDateTime '" + localDateTime + "' does not exist in zone '" + zone + "' due to a gap in the local time-line, typically caused by daylight savings");
            }
            throw new DateTimeException("ZoneOffset '" + offset + "' is not valid for LocalDateTime '" + localDateTime + "' in zone '" + zone + "'");
        }
        return new ZonedDateTime(localDateTime, offset, zone);
    }

    private static ZonedDateTime ofLenient(LocalDateTime localDateTime, ZoneOffset offset, ZoneId zone) {
        Objects.requireNonNull(localDateTime, "localDateTime");
        Objects.requireNonNull(offset, "offset");
        Objects.requireNonNull(zone, "zone");
        if (zone instanceof ZoneOffset && !offset.equals(zone)) {
            throw new IllegalArgumentException("ZoneId must match ZoneOffset");
        }
        return new ZonedDateTime(localDateTime, offset, zone);
    }

    public static ZonedDateTime from(TemporalAccessor temporal) {
        if (temporal instanceof ZonedDateTime) {
            return (ZonedDateTime)temporal;
        }
        try {
            ZoneId zone = ZoneId.from(temporal);
            if (temporal.isSupported(ChronoField.INSTANT_SECONDS)) {
                long epochSecond = temporal.getLong(ChronoField.INSTANT_SECONDS);
                int nanoOfSecond = temporal.get(ChronoField.NANO_OF_SECOND);
                return ZonedDateTime.create(epochSecond, nanoOfSecond, zone);
            }
            LocalDate date = LocalDate.from(temporal);
            LocalTime time = LocalTime.from(temporal);
            return ZonedDateTime.of(date, time, zone);
        }
        catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain ZonedDateTime from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName(), ex);
        }
    }

    public static ZonedDateTime parse(CharSequence text) {
        return ZonedDateTime.parse(text, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    public static ZonedDateTime parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(text, ZonedDateTime::from);
    }

    private ZonedDateTime(LocalDateTime dateTime, ZoneOffset offset, ZoneId zone) {
        this.dateTime = dateTime;
        this.offset = offset;
        this.zone = zone;
    }

    private ZonedDateTime resolveLocal(LocalDateTime newDateTime) {
        return ZonedDateTime.ofLocal(newDateTime, this.zone, this.offset);
    }

    private ZonedDateTime resolveInstant(LocalDateTime newDateTime) {
        return ZonedDateTime.ofInstant(newDateTime, this.offset, this.zone);
    }

    private ZonedDateTime resolveOffset(ZoneOffset offset) {
        if (!offset.equals(this.offset) && this.zone.getRules().isValidOffset(this.dateTime, offset)) {
            return new ZonedDateTime(this.dateTime, offset, this.zone);
        }
        return this;
    }

    @Override
    public boolean isSupported(TemporalField field) {
        return field instanceof ChronoField || field != null && field.isSupportedBy(this);
    }

    @Override
    public boolean isSupported(TemporalUnit unit) {
        return ChronoZonedDateTime.super.isSupported(unit);
    }

    @Override
    public ValueRange range(TemporalField field) {
        if (field instanceof ChronoField) {
            if (field == ChronoField.INSTANT_SECONDS || field == ChronoField.OFFSET_SECONDS) {
                return field.range();
            }
            return this.dateTime.range(field);
        }
        return field.rangeRefinedBy(this);
    }

    @Override
    public int get(TemporalField field) {
        if (field instanceof ChronoField) {
            ChronoField chronoField = (ChronoField)field;
            return switch (chronoField) {
                case ChronoField.INSTANT_SECONDS -> throw new UnsupportedTemporalTypeException("Invalid field 'InstantSeconds' for get() method, use getLong() instead");
                case ChronoField.OFFSET_SECONDS -> this.getOffset().getTotalSeconds();
                default -> this.dateTime.get(field);
            };
        }
        return ChronoZonedDateTime.super.get(field);
    }

    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            ChronoField chronoField = (ChronoField)field;
            return switch (chronoField) {
                case ChronoField.INSTANT_SECONDS -> this.toEpochSecond();
                case ChronoField.OFFSET_SECONDS -> this.getOffset().getTotalSeconds();
                default -> this.dateTime.getLong(field);
            };
        }
        return field.getFrom(this);
    }

    @Override
    public ZoneOffset getOffset() {
        return this.offset;
    }

    public ZonedDateTime withEarlierOffsetAtOverlap() {
        ZoneOffset earlierOffset;
        ZoneOffsetTransition trans = this.getZone().getRules().getTransition(this.dateTime);
        if (trans != null && trans.isOverlap() && !(earlierOffset = trans.getOffsetBefore()).equals(this.offset)) {
            return new ZonedDateTime(this.dateTime, earlierOffset, this.zone);
        }
        return this;
    }

    public ZonedDateTime withLaterOffsetAtOverlap() {
        ZoneOffset laterOffset;
        ZoneOffsetTransition trans = this.getZone().getRules().getTransition(this.toLocalDateTime());
        if (trans != null && !(laterOffset = trans.getOffsetAfter()).equals(this.offset)) {
            return new ZonedDateTime(this.dateTime, laterOffset, this.zone);
        }
        return this;
    }

    @Override
    public ZoneId getZone() {
        return this.zone;
    }

    public ZonedDateTime withZoneSameLocal(ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        return this.zone.equals(zone) ? this : ZonedDateTime.ofLocal(this.dateTime, zone, this.offset);
    }

    public ZonedDateTime withZoneSameInstant(ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        return this.zone.equals(zone) ? this : ZonedDateTime.create(this.dateTime.toEpochSecond(this.offset), this.dateTime.getNano(), zone);
    }

    public ZonedDateTime withFixedOffsetZone() {
        return this.zone.equals(this.offset) ? this : new ZonedDateTime(this.dateTime, this.offset, this.offset);
    }

    public LocalDateTime toLocalDateTime() {
        return this.dateTime;
    }

    @Override
    public LocalDate toLocalDate() {
        return this.dateTime.toLocalDate();
    }

    public int getYear() {
        return this.dateTime.getYear();
    }

    public int getMonthValue() {
        return this.dateTime.getMonthValue();
    }

    public Month getMonth() {
        return this.dateTime.getMonth();
    }

    public int getDayOfMonth() {
        return this.dateTime.getDayOfMonth();
    }

    public int getDayOfYear() {
        return this.dateTime.getDayOfYear();
    }

    public DayOfWeek getDayOfWeek() {
        return this.dateTime.getDayOfWeek();
    }

    @Override
    public LocalTime toLocalTime() {
        return this.dateTime.toLocalTime();
    }

    public int getHour() {
        return this.dateTime.getHour();
    }

    public int getMinute() {
        return this.dateTime.getMinute();
    }

    public int getSecond() {
        return this.dateTime.getSecond();
    }

    public int getNano() {
        return this.dateTime.getNano();
    }

    @Override
    public ZonedDateTime with(TemporalAdjuster adjuster) {
        if (adjuster instanceof LocalDate) {
            return this.resolveLocal(LocalDateTime.of((LocalDate)adjuster, this.dateTime.toLocalTime()));
        }
        if (adjuster instanceof LocalTime) {
            return this.resolveLocal(LocalDateTime.of(this.dateTime.toLocalDate(), (LocalTime)adjuster));
        }
        if (adjuster instanceof LocalDateTime) {
            return this.resolveLocal((LocalDateTime)adjuster);
        }
        if (adjuster instanceof OffsetDateTime) {
            OffsetDateTime odt = (OffsetDateTime)adjuster;
            return ZonedDateTime.ofLocal(odt.toLocalDateTime(), this.zone, odt.getOffset());
        }
        if (adjuster instanceof Instant) {
            Instant instant = (Instant)adjuster;
            return ZonedDateTime.create(instant.getEpochSecond(), instant.getNano(), this.zone);
        }
        if (adjuster instanceof ZoneOffset) {
            return this.resolveOffset((ZoneOffset)adjuster);
        }
        return (ZonedDateTime)adjuster.adjustInto(this);
    }

    @Override
    public ZonedDateTime with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField chronoField = (ChronoField)field;
            return switch (chronoField) {
                case ChronoField.INSTANT_SECONDS -> ZonedDateTime.create(newValue, this.getNano(), this.zone);
                case ChronoField.OFFSET_SECONDS -> {
                    ZoneOffset offset = ZoneOffset.ofTotalSeconds(chronoField.checkValidIntValue(newValue));
                    yield this.resolveOffset(offset);
                }
                default -> this.resolveLocal(this.dateTime.with(field, newValue));
            };
        }
        return field.adjustInto(this, newValue);
    }

    public ZonedDateTime withYear(int year) {
        return this.resolveLocal(this.dateTime.withYear(year));
    }

    public ZonedDateTime withMonth(int month) {
        return this.resolveLocal(this.dateTime.withMonth(month));
    }

    public ZonedDateTime withDayOfMonth(int dayOfMonth) {
        return this.resolveLocal(this.dateTime.withDayOfMonth(dayOfMonth));
    }

    public ZonedDateTime withDayOfYear(int dayOfYear) {
        return this.resolveLocal(this.dateTime.withDayOfYear(dayOfYear));
    }

    public ZonedDateTime withHour(int hour) {
        return this.resolveLocal(this.dateTime.withHour(hour));
    }

    public ZonedDateTime withMinute(int minute) {
        return this.resolveLocal(this.dateTime.withMinute(minute));
    }

    public ZonedDateTime withSecond(int second) {
        return this.resolveLocal(this.dateTime.withSecond(second));
    }

    public ZonedDateTime withNano(int nanoOfSecond) {
        return this.resolveLocal(this.dateTime.withNano(nanoOfSecond));
    }

    public ZonedDateTime truncatedTo(TemporalUnit unit) {
        return this.resolveLocal(this.dateTime.truncatedTo(unit));
    }

    @Override
    public ZonedDateTime plus(TemporalAmount amountToAdd) {
        if (amountToAdd instanceof Period) {
            Period periodToAdd = (Period)amountToAdd;
            return this.resolveLocal(this.dateTime.plus(periodToAdd));
        }
        Objects.requireNonNull(amountToAdd, "amountToAdd");
        return (ZonedDateTime)amountToAdd.addTo(this);
    }

    @Override
    public ZonedDateTime plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            if (unit.isDateBased()) {
                return this.resolveLocal(this.dateTime.plus(amountToAdd, unit));
            }
            return this.resolveInstant(this.dateTime.plus(amountToAdd, unit));
        }
        return unit.addTo(this, amountToAdd);
    }

    public ZonedDateTime plusYears(long years) {
        return this.resolveLocal(this.dateTime.plusYears(years));
    }

    public ZonedDateTime plusMonths(long months) {
        return this.resolveLocal(this.dateTime.plusMonths(months));
    }

    public ZonedDateTime plusWeeks(long weeks) {
        return this.resolveLocal(this.dateTime.plusWeeks(weeks));
    }

    public ZonedDateTime plusDays(long days) {
        return this.resolveLocal(this.dateTime.plusDays(days));
    }

    public ZonedDateTime plusHours(long hours) {
        return this.resolveInstant(this.dateTime.plusHours(hours));
    }

    public ZonedDateTime plusMinutes(long minutes) {
        return this.resolveInstant(this.dateTime.plusMinutes(minutes));
    }

    public ZonedDateTime plusSeconds(long seconds) {
        return this.resolveInstant(this.dateTime.plusSeconds(seconds));
    }

    public ZonedDateTime plusNanos(long nanos) {
        return this.resolveInstant(this.dateTime.plusNanos(nanos));
    }

    @Override
    public ZonedDateTime minus(TemporalAmount amountToSubtract) {
        if (amountToSubtract instanceof Period) {
            Period periodToSubtract = (Period)amountToSubtract;
            return this.resolveLocal(this.dateTime.minus(periodToSubtract));
        }
        Objects.requireNonNull(amountToSubtract, "amountToSubtract");
        return (ZonedDateTime)amountToSubtract.subtractFrom(this);
    }

    @Override
    public ZonedDateTime minus(long amountToSubtract, TemporalUnit unit) {
        return amountToSubtract == Long.MIN_VALUE ? this.plus(Long.MAX_VALUE, unit).plus(1L, unit) : this.plus(-amountToSubtract, unit);
    }

    public ZonedDateTime minusYears(long years) {
        return years == Long.MIN_VALUE ? this.plusYears(Long.MAX_VALUE).plusYears(1L) : this.plusYears(-years);
    }

    public ZonedDateTime minusMonths(long months) {
        return months == Long.MIN_VALUE ? this.plusMonths(Long.MAX_VALUE).plusMonths(1L) : this.plusMonths(-months);
    }

    public ZonedDateTime minusWeeks(long weeks) {
        return weeks == Long.MIN_VALUE ? this.plusWeeks(Long.MAX_VALUE).plusWeeks(1L) : this.plusWeeks(-weeks);
    }

    public ZonedDateTime minusDays(long days) {
        return days == Long.MIN_VALUE ? this.plusDays(Long.MAX_VALUE).plusDays(1L) : this.plusDays(-days);
    }

    public ZonedDateTime minusHours(long hours) {
        return hours == Long.MIN_VALUE ? this.plusHours(Long.MAX_VALUE).plusHours(1L) : this.plusHours(-hours);
    }

    public ZonedDateTime minusMinutes(long minutes) {
        return minutes == Long.MIN_VALUE ? this.plusMinutes(Long.MAX_VALUE).plusMinutes(1L) : this.plusMinutes(-minutes);
    }

    public ZonedDateTime minusSeconds(long seconds) {
        return seconds == Long.MIN_VALUE ? this.plusSeconds(Long.MAX_VALUE).plusSeconds(1L) : this.plusSeconds(-seconds);
    }

    public ZonedDateTime minusNanos(long nanos) {
        return nanos == Long.MIN_VALUE ? this.plusNanos(Long.MAX_VALUE).plusNanos(1L) : this.plusNanos(-nanos);
    }

    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.localDate()) {
            return (R)this.toLocalDate();
        }
        return ChronoZonedDateTime.super.query(query);
    }

    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        ZonedDateTime end = ZonedDateTime.from(endExclusive);
        if (unit instanceof ChronoUnit) {
            ZonedDateTime start = this;
            try {
                end = end.withZoneSameInstant(this.zone);
            }
            catch (DateTimeException ex) {
                start = this.withZoneSameInstant(end.zone);
            }
            if (unit.isDateBased()) {
                return start.dateTime.until(end.dateTime, unit);
            }
            return start.toOffsetDateTime().until(end.toOffsetDateTime(), unit);
        }
        return unit.between(this, end);
    }

    @Override
    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    public OffsetDateTime toOffsetDateTime() {
        return OffsetDateTime.of(this.dateTime, this.offset);
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
        if (!(obj instanceof ZonedDateTime)) return false;
        ZonedDateTime other = (ZonedDateTime)obj;
        if (!this.dateTime.equals(other.dateTime)) return false;
        if (!this.offset.equals(other.offset)) return false;
        if (!this.zone.equals(other.zone)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.dateTime.hashCode() ^ this.offset.hashCode() ^ Integer.rotateLeft(this.zone.hashCode(), 3);
    }

    @Override
    public String toString() {
        String str = this.dateTime.toString() + this.offset.toString();
        if (this.offset != this.zone) {
            str = str + '[' + this.zone.toString() + ']';
        }
        return str;
    }

    private Object writeReplace() {
        return new Ser(6, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException {
        this.dateTime.writeExternal(out);
        this.offset.writeExternal(out);
        this.zone.write(out);
    }

    static ZonedDateTime readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        LocalDateTime dateTime = LocalDateTime.readExternal(in);
        ZoneOffset offset = ZoneOffset.readExternal(in);
        ZoneId zone = (ZoneId)Ser.read(in);
        return ZonedDateTime.ofLenient(dateTime, offset, zone);
    }
}

